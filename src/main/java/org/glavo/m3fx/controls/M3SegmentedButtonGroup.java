// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
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
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SegmentedButtonGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 segmented button group that lays out adjacent segments.
///
/// The group manages the selected state of child [M3SegmentedButton] instances, applies the configured
/// [M3SegmentedButtonSelectionMode], and renders the shared outline geometry expected by Material segmented
/// buttons. It also provides keyboard traversal and empty-selection control for groups that require at least one
/// selected segment.
///
/// See [Material Design segmented buttons](https://m3.material.io/components/segmented-buttons/overview).
@NotNullByDefault
public class M3SegmentedButtonGroup extends Control {
    /// The base style class for m3fx segmented button groups.
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
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between segmented buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentFocusTarget(this, getItems()));

    // Backing property for the styleable segment spacing token.
    private @Nullable StyleableDoubleProperty spacing;

    // Backing property for the public segmented button selection mode API.
    private final ObjectProperty<M3SegmentedButtonSelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3SegmentedButtonSelectionMode.SINGLE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SegmentedButtonSelectionMode.SINGLE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// The selected segmented buttons in child order.
    private final ObservableList<M3SegmentedButton> selectedButtons = FXCollections.observableArrayList();

    /// The read-only selected segmented button view.
    private final @UnmodifiableView ObservableList<M3SegmentedButton> selectedButtonsView =
            FXCollections.unmodifiableObservableList(selectedButtons);

    // Backing property for the public read-only selected segmented button API.
    private final ReadOnlyObjectWrapper<@Nullable M3SegmentedButton> selectedButton =
            new ReadOnlyObjectWrapper<>(this, "selectedButton");

    // Backing property for the public empty-selection policy API.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected button when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstButtonIfNeeded();
            }
        }
    };

    /// The selected-state listeners installed on segmented buttons.
    private final Map<M3SegmentedButton, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

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
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
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

    /// Creates a segmented button group with the supplied buttons.
    ///
    /// @param buttons the initial segmented buttons
    public M3SegmentedButtonGroup(M3SegmentedButton... buttons) {
        initialize();
        addButtons(buttons);
    }

    /// Returns the mutable child list used as segmented button group content.
    ///
    /// @return the mutable child list used as segmented button group content
    public final ObservableList<Node> getItems() {
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

    /// Returns the spacing property.
    ///
    /// @return the styleable child spacing property
    public final StyleableDoubleProperty spacingProperty() {
        if (spacing == null) {
            spacing = new StyleableDoubleProperty(DEFAULT_SPACING) {
                /// Validates updated spacing values.
                @Override
                protected void invalidated() {
                    M3Css.finite(get(), "spacing");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3SegmentedButtonGroup.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "spacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3SegmentedButtonGroup, Number> getCssMetaData() {
                    return StyleableProperties.SPACING;
                }
            };
        }
        return spacing;
    }

    /// Adds one segmented button.
    ///
    /// @param button the segmented button to add
    public final void addButton(M3SegmentedButton button) {
        getItems().add(Objects.requireNonNull(button, "button"));
    }

    /// Adds segmented buttons.
    ///
    /// @param buttons the segmented buttons to add
    public final void addButtons(M3SegmentedButton... buttons) {
        validateButtons(buttons);
        getItems().addAll(buttons);
    }

    /// Replaces all segmented buttons.
    ///
    /// @param buttons the replacement segmented buttons
    public final void setButtons(M3SegmentedButton... buttons) {
        validateButtons(buttons);
        getItems().setAll(buttons);
    }

    /// Removes all segmented button group content.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the segmented button selection mode.
    ///
    /// @return the segmented button selection mode
    public final M3SegmentedButtonSelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the segmented button selection mode.
    ///
    /// @param selectionMode the segmented button selection mode
    public final void setSelectionMode(M3SegmentedButtonSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the segmented button selection mode property.
    ///
    /// @return the segmented button selection mode property
    public final ObjectProperty<M3SegmentedButtonSelectionMode> selectionModeProperty() {
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

    /// Returns the selected segmented button property.
    ///
    /// @return the read-only selected segmented button property
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

    /// Returns the empty-selection policy property.
    ///
    /// @return the empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a segmented button that belongs to this group.
    ///
    /// @param button the segmented button to select
    /// @throws IllegalArgumentException if the button does not belong to this group
    public final void select(M3SegmentedButton button) {
        Objects.requireNonNull(button, "button");
        if (!getItems().contains(button)) {
            throw new IllegalArgumentException("button must belong to this segmented button group");
        }
        if (getSelectionMode() == M3SegmentedButtonSelectionMode.MULTIPLE) {
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
        M3SegmentedButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Selects the last segmented button when one exists.
    public final void selectLast() {
        @Nullable M3SegmentedButton lastButton =
                M3SelectionNavigation.last(getItems(), M3SegmentedButton.class);
        if (lastButton != null) {
            select(lastButton);
        }
    }

    /// Selects the next segmented button after the current selected button, wrapping at the end.
    public final void selectNext() {
        @Nullable M3SegmentedButton nextButton =
                M3SelectionNavigation.next(getItems(), getSelectedButton(), M3SegmentedButton.class);
        if (nextButton != null) {
            select(nextButton);
        }
    }

    /// Selects the previous segmented button before the current selected button, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3SegmentedButton previousButton =
                M3SelectionNavigation.previous(getItems(), getSelectedButton(), M3SegmentedButton.class);
        if (previousButton != null) {
            select(previousButton);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstButtonIfNeeded();
            return;
        }
        selectOnly(null);
    }

    /// Returns the user-agent stylesheet for m3fx segmented button groups.
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
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.focusTarget(M3SelectionNavigation.focusTarget(
                    getItems(),
                    getSelectedButton(),
                    M3SegmentedButton.class
            ));
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SegmentedButtonSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedButtonsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for segmented buttons.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3SelectionNavigation.focusTarget(
                    getItems(),
                    getSelectedButton(),
                    M3SegmentedButton.class
            ));
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> M3Accessible.showItem(getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        effectiveNodeOrientationProperty().addListener(effectiveNodeOrientationListener);
        getItems().addListener(childrenListener);
        focusNotifier.start();
        updateSegmentStyles();
    }

    /// Applies keyboard navigation across enabled segmented buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3SegmentedButtonSelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(
                            getItems(),
                            getSelectedButton(),
                            M3SegmentedButton.class
                    ),
                    M3SegmentedButton.class,
                    true,
                    false,
                    M3SelectionNavigation.isRightToLeft(this)
            );
            return;
        }

        M3SelectionNavigation.handleKeySelection(
                event,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedButton(), M3SegmentedButton.class),
                M3SegmentedButton.class,
                true,
                false,
                M3SelectionNavigation.isRightToLeft(this),
                this::select
        );
    }

    /// Applies selected segmented buttons supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3SegmentedButtonSelectionMode.SINGLE) {
            @Nullable M3SegmentedButton button =
                    M3Accessible.firstSelectionTarget(getItems(), M3SegmentedButton.class, parameters);
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
                if (child instanceof M3SegmentedButton button) {
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
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleButtonSelectedChanged(button, newValue);
        selectedListeners.put(button, listener);
        button.selectedProperty().addListener(listener);
    }

    /// Removes the selected-state listener from a segmented button.
    private void uninstallButton(M3SegmentedButton button) {
        ChangeListener<Boolean> listener = selectedListeners.remove(button);
        if (listener != null) {
            button.selectedProperty().removeListener(listener);
        }
    }

    /// Keeps selected segmented buttons mutually exclusive.
    private void handleButtonSelectedChanged(M3SegmentedButton button, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (selected) {
            if (getSelectionMode() == M3SegmentedButtonSelectionMode.SINGLE) {
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

    /// Enforces single-selection and non-empty selection invariants.
    private void enforceSelectionPolicy() {
        refreshSelectedButtons();
        if (getSelectionMode() == M3SegmentedButtonSelectionMode.SINGLE && selectedButtons.size() > 1) {
            selectOnly(selectedButtons.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstButtonIfNeeded();
        }
    }

    /// Selects the first button when the selection is empty and empty selection is disabled.
    private void selectFirstButtonIfNeeded() {
        if (!selectedButtons.isEmpty()) {
            return;
        }

        M3SegmentedButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Sets one button's selected state and refreshes selected button state.
    private void setButtonSelected(M3SegmentedButton button, boolean selected) {
        updatingSelection = true;
        try {
            button.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
        refreshSelectedButtons();
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
        List<M3SegmentedButton> previousSelection = List.copyOf(selectedButtons);
        selectedButtons.clear();
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton button && button.isSelected()) {
                selectedButtons.add(button);
            }
        }
        selectedButton.set(selectedButtons.isEmpty() ? null : selectedButtons.get(0));
        if (!selectedButtons.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            focusNotifier.refresh();
        }
    }

    /// Returns the first segmented button child.
    private @Nullable M3SegmentedButton firstButton() {
        return M3SelectionNavigation.first(getItems(), M3SegmentedButton.class);
    }

    /// Applies first, middle, last, or single segment style classes.
    private void updateSegmentStyles() {
        int segmentCount = 0;
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton) {
                segmentCount++;
            }
        }

        boolean rightToLeft = getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        int segmentIndex = 0;
        for (Node child : getItems()) {
            if (child instanceof M3SegmentedButton button) {
                int visualSegmentIndex = rightToLeft ? segmentCount - segmentIndex - 1 : segmentIndex;
                M3ControlStyles.replaceVariant(
                        button,
                        segmentStyleClass(visualSegmentIndex, segmentCount),
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

    /// Validates a segmented button array.
    private static void validateButtons(M3SegmentedButton... buttons) {
        Objects.requireNonNull(buttons, "buttons");
        for (M3SegmentedButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
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
