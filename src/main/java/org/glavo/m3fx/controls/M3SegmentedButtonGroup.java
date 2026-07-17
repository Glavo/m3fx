// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SegmentedButtonGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 segmented button group that lays out adjacent segments.
///
/// The group manages the selected state of child [M3SegmentedButton] instances, applies the configured
/// [M3SelectionMode], and renders the shared outline geometry expected by Material segmented
/// buttons. It also provides keyboard traversal and empty-selection control for groups that require at least one
/// selected segment.
///
/// See [Material Design segmented buttons](https://m3.material.io/components/segmented-buttons/overview).
@NotNullByDefault
public final class M3SegmentedButtonGroup extends Control {
    /// The base style class for M3FX segmented button groups.
    public static final String STYLE_CLASS = "m3-segmented-button-group";

    /// The style class applied when a segmented button is the only segment.
    public static final String SINGLE_SEGMENT_STYLE_CLASS = "m3-segmented-button-single";

    /// The style class applied to the first segmented button in a group.
    public static final String FIRST_SEGMENT_STYLE_CLASS = "m3-segmented-button-first";

    /// The style class applied to middle segmented buttons in a group.
    public static final String MIDDLE_SEGMENT_STYLE_CLASS = "m3-segmented-button-middle";

    /// The style class applied to the last segmented button in a group.
    public static final String LAST_SEGMENT_STYLE_CLASS = "m3-segmented-button-last";

    /// The default spacing that lets adjacent segmented button borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

    /// The mutable segmented button group content.
    private final ObservableList<M3SegmentedButton> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between segmented buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(
                            this,
                            getItems(),
                            getSelectedButton(),
                            M3SegmentedButton.class
                    ));

    /// Backing property for the styleable segment spacing token.
    private @Nullable StyleableDoubleProperty spacing;

    /// Backing property for the public segmented button selection mode API.
    private final ObjectProperty<M3SelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3SelectionMode.SINGLE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SelectionMode.SINGLE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// The selected segmented buttons in child order.
    private final ObservableList<M3SegmentedButton> selectedButtons = M3ObservableLists.nonNullElementList("selectedButton");

    /// The read-only selected segmented button view.
    private final @UnmodifiableView ObservableList<M3SegmentedButton> selectedButtonsView =
            FXCollections.unmodifiableObservableList(selectedButtons);

    /// Backing property for the public read-only selected segmented button API.
    private final ReadOnlyObjectWrapper<@Nullable M3SegmentedButton> selectedButton =
            new ReadOnlyObjectWrapper<>(this, "selectedButton");

    /// Backing property for the public empty-selection policy API.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected button when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstButtonIfNeeded();
            }
        }
    };

    /// Reusable storage for computing selected buttons without allocating on every refresh.
    private final List<M3SegmentedButton> selectedButtonsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed segmented button.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3SegmentedButton button) {
            handleButtonSelectedChanged(button, button.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed segmented button.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3SegmentedButton button) {
            handleButtonReachabilityChanged(button);
        }
    };

    /// Updates segment position style classes and selection listeners when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3SegmentedButton button) {
                    uninstallButton(button);
                    button.setSelected(false);
                    clearSegmentStyle(button);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3SegmentedButton button) {
                    installButton(button);
                }
            }
        }
        updateSegmentStyles();
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Updates physical edge style classes when the effective layout direction changes.
    private final ChangeListener<NodeOrientation> effectiveNodeOrientationListener =
            (observable, oldValue, newValue) -> updateSegmentStyles();

    /// Whether the group is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty segmented button group.
    public M3SegmentedButtonGroup() {
        initialize();
    }

    /// Returns the mutable child list used as segmented button group content.
    ///
    /// @return the mutable child list used as segmented button group content
    public final ObservableList<M3SegmentedButton> getItems() {
        return items;
    }

    /// Returns the spacing between segmented buttons.
    ///
    /// @return the child spacing in pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between segmented buttons.
    ///
    /// @param spacing the child spacing in pixels
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.finite(spacing, "spacing"));
    }

    public final StyleableDoubleProperty spacingProperty() {
        if (spacing == null) {
            spacing = M3Css.finiteStyleableDoubleProperty(
                    DEFAULT_SPACING,
                    this,
                    "spacing",
                    StyleableProperties.SPACING,
                    () -> {
                    }
            );
        }
        return spacing;
    }

    /// Returns the segmented button selection mode.
    ///
    /// @return the segmented button selection mode
    public final M3SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the segmented button selection mode.
    ///
    /// @param selectionMode the segmented button selection mode
    /// @throws NullPointerException if any required argument is `null`
    public final void setSelectionMode(M3SelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    public final ObjectProperty<M3SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns the selected segmented buttons in child order.
    ///
    /// @return the selected segmented buttons in child order
    public final @UnmodifiableView ObservableList<M3SegmentedButton> getSelectedButtons() {
        return selectedButtonsView;
    }

    /// Returns the selected segmented button.
    ///
    /// @return the first selected segmented button, or `null` when selection is empty
    public final @Nullable M3SegmentedButton getSelectedButton() {
        return selectedButton.get();
    }

    public final ReadOnlyObjectProperty<@Nullable M3SegmentedButton> selectedButtonProperty() {
        return selectedButton.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected segmented button, or `-1` when none is selected.
    ///
    /// @return the child index of the first selected segmented button, or `-1` when none is selected
    public final int getSelectedIndex() {
        @Nullable M3SegmentedButton button = getSelectedButton();
        return button == null ? -1 : getItems().indexOf(button);
    }

    /// Returns whether this group allows all segmented buttons to be unselected.
    ///
    /// @return `true` if this group allows all segmented buttons to be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this group allows all segmented buttons to be unselected.
    ///
    /// @param allowEmptySelection whether this group allows all segmented buttons to be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a segmented button that belongs to this group.
    ///
    /// @param button the segmented button to select
    /// @throws IllegalArgumentException if the button does not belong to this group
    /// @throws NullPointerException if any required argument is `null`
    public final void select(M3SegmentedButton button) {
        Objects.requireNonNull(button, "button");
        if (!getItems().contains(button)) {
            throw new IllegalArgumentException("button must belong to this segmented button group");
        }
        if (!isSelectableButton(button)) {
            throw new IllegalArgumentException("button must be selectable");
        }
        if (getSelectionMode() == M3SelectionMode.NONE) {
            return;
        }
        if (getSelectionMode() == M3SelectionMode.MULTIPLE) {
            setButtonSelected(button, true);
        } else {
            selectOnly(button);
        }
    }

    /// Selects the segmented button at the given child index.
    ///
    /// @param index the child index to select
    /// @throws IllegalArgumentException if the child at the index is not a segmented button
    public final void selectIndex(int index) {
        Node child = getItems().get(index);
        if (child instanceof M3SegmentedButton button) {
            select(button);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3SegmentedButton");
    }

    /// Selects the first segmented button when one exists.
    public final void selectFirst() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3SegmentedButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Selects the last segmented button when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3SegmentedButton lastButton =
                M3SelectionNavigation.last(getItems(), M3SegmentedButton.class);
        if (lastButton != null) {
            select(lastButton);
        }
    }

    /// Selects the next segmented button after the current selected button, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3SegmentedButton nextButton =
                M3SelectionNavigation.next(getItems(), getSelectedButton(), M3SegmentedButton.class);
        if (nextButton != null) {
            select(nextButton);
        }
    }

    /// Selects the previous segmented button before the current selected button, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3SegmentedButton previousButton =
                M3SelectionNavigation.previous(getItems(), getSelectedButton(), M3SegmentedButton.class);
        if (previousButton != null) {
            select(previousButton);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3SelectionMode.NONE) {
            selectFirstButtonIfNeeded();
            return;
        }
        selectOnly(null);
    }

    /// Returns the user-agent stylesheet for M3FX segmented button groups.
    ///
    /// @return the segmented button user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("segmented-button.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for segmented button group content and selection state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if any required argument is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedButton(),
                    M3SegmentedButton.class
            );
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedButtonsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for segmented buttons.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if any required argument is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleSelectionTarget();
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Requests focus on the current selected or focused accessibility target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleSelectionTarget() {
        if (M3Accessible.showItem(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedButton(),
                M3SegmentedButton.class
        ))) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showItemOrDefault(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedButton(),
                M3SegmentedButton.class
        ), getItems(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the group focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        effectiveNodeOrientationProperty().addListener(effectiveNodeOrientationListener);
        getItems().addListener(childrenListener);
        focusNotifier.start();
        updateSegmentStyles();
    }

    /// Applies keyboard navigation across enabled segmented buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            return;
        }
        if (getSelectionMode() == M3SelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    this,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(
                            getItems(),
                            getSelectedButton(),
                            M3SegmentedButton.class
                    ),
                    M3SegmentedButton.class,
                    true,
                    false,
                    M3NodeLayout.isRightToLeft(this)
            );
            return;
        }

        M3SelectionNavigation.handleKeySelection(
                event,
                this,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedButton(), M3SegmentedButton.class),
                M3SegmentedButton.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this),
                this::select
        );
    }

    /// Applies selected segmented buttons supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            selectOnly(null);
            return;
        }
        if (getSelectionMode() == M3SelectionMode.SINGLE) {
            @Nullable M3SegmentedButton button =
                    firstAccessibleSelectableButton(parameters);
            if (button == null) {
                clearSelection();
            } else {
                select(button);
            }
            return;
        }

        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3SegmentedButton button && isSelectableButton(button)) {
                    button.setSelected(M3Accessible.containsSelectionTarget(button, parameters));
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedButtons();
        if (!isAllowEmptySelection()) {
            selectFirstButtonIfNeeded();
        }
    }

    /// Installs a selected-state listener on a segmented button.
    private void installButton(M3SegmentedButton button) {
        button.selectedProperty().addListener(selectedInvalidation);
        button.disabledProperty().addListener(reachabilityInvalidation);
        button.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes the selected-state listener from a segmented button.
    private void uninstallButton(M3SegmentedButton button) {
        button.selectedProperty().removeListener(selectedInvalidation);
        button.disabledProperty().removeListener(reachabilityInvalidation);
        button.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Keeps selected segmented buttons mutually exclusive.
    private void handleButtonSelectedChanged(M3SegmentedButton button, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (getSelectionMode() == M3SelectionMode.NONE || !isSelectableButton(button)) {
            if (selected) {
                setButtonSelected(button, false);
                if (!isAllowEmptySelection()) {
                    selectFirstButtonIfNeeded();
                }
            }
            return;
        }

        if (selected) {
            if (getSelectionMode() == M3SelectionMode.SINGLE) {
                selectOnly(button);
            } else {
                refreshSelectedButtons();
            }
            return;
        }

        refreshSelectedButtons();
        if (!isAllowEmptySelection() && selectedButtons.isEmpty()) {
            select(button);
        }
    }

    /// Keeps selection and accessibility state consistent when a button becomes unreachable.
    private void handleButtonReachabilityChanged(M3SegmentedButton button) {
        if (button.isSelected() && !isSelectableButton(button)) {
            setButtonSelected(button, false);
        }
        enforceSelectionPolicy();
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Enforces single-selection and non-empty selection invariants.
    private void enforceSelectionPolicy() {
        refreshSelectedButtons();
        if (getSelectionMode() == M3SelectionMode.NONE) {
            selectOnly(null);
            return;
        }
        if (getSelectionMode() == M3SelectionMode.SINGLE && selectedButtons.size() > 1) {
            selectOnly(selectedButtons.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstButtonIfNeeded();
        }
    }

    /// Selects the first button when the selection is empty and empty selection is disabled.
    private void selectFirstButtonIfNeeded() {
        if (getSelectionMode() == M3SelectionMode.NONE || !selectedButtons.isEmpty()) {
            return;
        }

        M3SegmentedButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Sets one button's selected state and refreshes selected button state.
    private void setButtonSelected(M3SegmentedButton button, boolean selected) {
        setButtonSelectedWithoutRefresh(button, selected);
        refreshSelectedButtons();
    }

    /// Sets one button's selected state without refreshing the aggregate selected button list.
    private void setButtonSelectedWithoutRefresh(M3SegmentedButton button, boolean selected) {
        updatingSelection = true;
        try {
            button.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects one segmented button and clears selection from the remaining segments.
    private void selectOnly(@Nullable M3SegmentedButton button) {
        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3SegmentedButton segmentedButton) {
                    segmentedButton.setSelected(segmentedButton == button);
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedButtons();
    }

    /// Refreshes selected button state from current child states.
    private void refreshSelectedButtons() {
        selectedButtonsScratch.clear();
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton button && button.isSelected()) {
                if (isSelectableButton(button)) {
                    selectedButtonsScratch.add(button);
                } else {
                    setButtonSelectedWithoutRefresh(button, false);
                }
            }
        }
        boolean selectionChanged = !selectedButtons.equals(selectedButtonsScratch);
        if (selectionChanged) {
            selectedButtons.setAll(selectedButtonsScratch);
        }
        selectedButtonsScratch.clear();

        selectedButton.set(selectedButtons.isEmpty() ? null : selectedButtons.get(0));
        if (selectionChanged) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Returns the first segmented button child.
    private @Nullable M3SegmentedButton firstButton() {
        return M3SelectionNavigation.first(getItems(), M3SegmentedButton.class);
    }

    /// Returns the first selectable button referenced by accessibility parameters.
    private @Nullable M3SegmentedButton firstAccessibleSelectableButton(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton button
                    && isSelectableButton(button)
                    && M3Accessible.containsSelectionTarget(button, parameters)) {
                return button;
            }
        }
        return null;
    }

    /// Returns whether a segmented button can currently participate in selection.
    private boolean isSelectableButton(M3SegmentedButton button) {
        return M3Accessible.isEffectivelyReachable(this) && M3Accessible.isEffectivelyReachable(button);
    }

    /// Applies first, middle, last, or single segment style classes.
    private void updateSegmentStyles() {
        int segmentCount = 0;
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton) {
                segmentCount++;
            }
        }

        int segmentIndex = 0;
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton button) {
                M3ControlStyles.replaceVariant(
                        button,
                        segmentStyleClass(segmentIndex, segmentCount),
                        SINGLE_SEGMENT_STYLE_CLASS,
                        FIRST_SEGMENT_STYLE_CLASS,
                        MIDDLE_SEGMENT_STYLE_CLASS,
                        LAST_SEGMENT_STYLE_CLASS
                );
                segmentIndex++;
            }
        }
    }

    /// Returns the segment position style class for an index.
    private static String segmentStyleClass(int index, int count) {
        if (count == 1) {
            return SINGLE_SEGMENT_STYLE_CLASS;
        }
        if (index == 0) {
            return FIRST_SEGMENT_STYLE_CLASS;
        }
        if (index == count - 1) {
            return LAST_SEGMENT_STYLE_CLASS;
        }
        return MIDDLE_SEGMENT_STYLE_CLASS;
    }

    /// Removes all segment position style classes from a button.
    private static void clearSegmentStyle(M3SegmentedButton button) {
        button.getStyleClass().remove(SINGLE_SEGMENT_STYLE_CLASS);
        button.getStyleClass().remove(FIRST_SEGMENT_STYLE_CLASS);
        button.getStyleClass().remove(MIDDLE_SEGMENT_STYLE_CLASS);
        button.getStyleClass().remove(LAST_SEGMENT_STYLE_CLASS);
    }

    /// Creates the default Material Design 3 segmented button group skin.
    ///
    /// @return the default Material Design 3 segmented button group skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SegmentedButtonGroupSkin(this);
    }

    /// CSS metadata for segmented button group layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for segmented button spacing.
        private static final CssMetaData<M3SegmentedButtonGroup, Number> SPACING =
                new CssMetaData<>("-m3-segmented-button-group-spacing", SizeConverter.getInstance(), DEFAULT_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButtonGroup control) {
                        return M3Css.isSettable(control.spacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButtonGroup control) {
                        return control.spacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
