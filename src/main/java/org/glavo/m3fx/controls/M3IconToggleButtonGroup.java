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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3IconToggleButtonGroupSkin;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 toggle icon button group.
///
/// The group displays an ordered, live list of [M3IconToggleButton] controls and coordinates their selection.
/// The default [selectionModeProperty] is [M3SelectionMode#SINGLE], but empty selection is initially allowed.
/// Arrow keys move through reachable buttons, while activation remains owned by each button.
///
/// [getSelectedButtons] is an unmodifiable observable view in item order. [selectedButtonProperty] identifies the
/// first selected button, and is `null` when selection is empty. Removing a button clears its selected state.
/// Item nodes are displayed by this control and consequently must not simultaneously belong to another parent.
///
/// ```java
/// M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
/// M3IconToggleButton gridButton = new M3IconToggleButton("G");
/// M3IconToggleButton listButton = new M3IconToggleButton("L");
/// group.getItems().addAll(gridButton, listButton);
/// group.select(gridButton);
/// ```
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public final class M3IconToggleButtonGroup extends Control {
    /// The base style class for M3FX toggle icon button groups.
    public static final String STYLE_CLASS = "m3-icon-toggle-button-group";

    /// The default spacing between toggle icon buttons.
    private static final double DEFAULT_SPACING = 8.0;

    /// The live, mutable, ordered list of buttons displayed by this group.
    ///
    /// The list rejects `null` elements and reports mutations through the `ObservableList` change API. Adding,
    /// removing, or reordering buttons immediately updates selection and keyboard traversal. A button node may
    /// occur only once because a JavaFX node can occupy only one position in a parent.
    private final ObservableList<M3IconToggleButton> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between toggle icon buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(
                            this,
                            getItems(),
                            getSelectedButton(),
                            M3IconToggleButton.class
                    ));

    /// The horizontal spacing between adjacent buttons in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty spacing;

    /// The policy used to coordinate selected buttons.
    ///
    /// [M3SelectionMode#NONE] clears managed selection, [M3SelectionMode#SINGLE] retains at most one selected
    /// button, and [M3SelectionMode#MULTIPLE] permits any number. A direct assignment of `null` is replaced with
    /// [M3SelectionMode#SINGLE].
    ///
    /// @defaultValue [M3SelectionMode#SINGLE]
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

    /// The selected icon toggle buttons in child order.
    private final ObservableList<M3IconToggleButton> selectedButtons = M3ObservableLists.nonNullElementList("selectedButton");

    /// The read-only selected icon toggle button view.
    private final @UnmodifiableView ObservableList<M3IconToggleButton> selectedButtonsView =
            FXCollections.unmodifiableObservableList(selectedButtons);

    /// The currently selected toggle icon button.
    private final ReadOnlyObjectWrapper<@Nullable M3IconToggleButton> selectedButton =
            new ReadOnlyObjectWrapper<>(this, "selectedButton");

    /// Whether the managed selection may be empty.
    ///
    /// Setting the value to `false` immediately selects the first enabled, visible button when the selection mode
    /// is not [M3SelectionMode#NONE] and no button is selected.
    ///
    /// @defaultValue `true`
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
    private final List<M3IconToggleButton> selectedButtonsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed button.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3IconToggleButton button) {
            handleButtonSelectedChanged(button, button.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed button.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3IconToggleButton button) {
            handleButtonReachabilityChanged(button);
        }
    };

    /// Updates button listeners when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3IconToggleButton button) {
                    uninstallButton(button);
                    button.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3IconToggleButton button) {
                    installButton(button);
                }
            }
        }
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Whether the group is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty group using single selection, allowing an empty selection, with `8.0` logical pixels of
    /// spacing.
    public M3IconToggleButtonGroup() {
        initialize();
    }

    /// Returns the live mutable list of buttons displayed by this group.
    ///
    /// Mutations are observed immediately and insertion order determines layout and keyboard traversal. The list
    /// rejects `null`. It does not perform an explicit duplicate check, but each button is a JavaFX node and must
    /// occur only once and must not simultaneously belong to another parent.
    ///
    /// @return the live mutable item list
    public final ObservableList<M3IconToggleButton> getItems() {
        return items;
    }

    /// Returns the spacing between toggle icon buttons.
    ///
    /// @return the child spacing in logical pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between toggle icon buttons.
    ///
    /// @param spacing the child spacing in logical pixels
    /// @throws IllegalArgumentException if `spacing` is negative or not finite
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.nonNegative(spacing, "spacing"));
    }

    public final StyleableDoubleProperty spacingProperty() {
        if (spacing == null) {
            spacing = M3Css.nonNegativeStyleableDoubleProperty(
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





    /// Returns the icon toggle button selection mode.
    ///
    /// @return the icon toggle button selection mode
    public final M3SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the icon toggle button selection mode.
    ///
    /// @param selectionMode the icon toggle button selection mode
    /// @throws NullPointerException if `selectionMode` is `null`
    public final void setSelectionMode(M3SelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    public final ObjectProperty<M3SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns an unmodifiable observable view of the selected buttons in item order.
    ///
    /// The returned list is live: listeners are notified when item selection or item order changes.
    ///
    /// @return the live unmodifiable selected-button view
    public final @UnmodifiableView ObservableList<M3IconToggleButton> getSelectedButtons() {
        return selectedButtonsView;
    }

    /// Returns the selected toggle icon button.
    ///
    /// @return the selected toggle icon button, or `null` when there is no selected button
    public final @Nullable M3IconToggleButton getSelectedButton() {
        return selectedButton.get();
    }

    public final ReadOnlyObjectProperty<@Nullable M3IconToggleButton> selectedButtonProperty() {
        return selectedButton.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected toggle icon button, or `-1` when none is selected.
    ///
    /// @return the child index of the first selected toggle icon button, or `-1` when none is selected
    public final int getSelectedIndex() {
        @Nullable M3IconToggleButton button = getSelectedButton();
        return button == null ? -1 : getItems().indexOf(button);
    }

    /// Returns whether this group allows all buttons to be unselected.
    ///
    /// @return `true` when this group allows all buttons to be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this group allows all buttons to be unselected.
    ///
    /// @param allowEmptySelection whether this group allows all buttons to be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a toggle icon button that belongs to this group.
    ///
    /// @param button the toggle icon button to select
    /// @throws NullPointerException if `button` is `null`
    /// @throws IllegalArgumentException if `button` is not an effectively reachable member of this group
    public final void select(M3IconToggleButton button) {
        Objects.requireNonNull(button, "button");
        if (!getItems().contains(button)) {
            throw new IllegalArgumentException("button must belong to this toggle icon button group");
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

    /// Selects the toggle icon button at the given child index.
    ///
    /// @param index the item index to select
    /// @throws IndexOutOfBoundsException if `index` is outside the item list
    /// @throws IllegalArgumentException if the indexed button is not selectable
    public final void selectIndex(int index) {
        Node child = getItems().get(index);
        if (child instanceof M3IconToggleButton button) {
            select(button);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3IconToggleButton");
    }

    /// Selects the first toggle icon button when one exists.
    public final void selectFirst() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3IconToggleButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Selects the last toggle icon button when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3IconToggleButton lastButton =
                M3SelectionNavigation.last(getItems(), M3IconToggleButton.class);
        if (lastButton != null) {
            select(lastButton);
        }
    }

    /// Selects the next toggle icon button after the current selected button, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3IconToggleButton nextButton =
                M3SelectionNavigation.next(getItems(), getSelectedButton(), M3IconToggleButton.class);
        if (nextButton != null) {
            select(nextButton);
        }
    }

    /// Selects the previous toggle icon button before the current selected button, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3IconToggleButton previousButton =
                M3SelectionNavigation.previous(getItems(), getSelectedButton(), M3IconToggleButton.class);
        if (previousButton != null) {
            select(previousButton);
        }
    }

    /// Clears the current selection if the active policy allows it.
    ///
    /// If empty selection is disallowed, this method preserves the current selection or selects the first
    /// selectable button. In [M3SelectionMode#NONE], the selection is always cleared.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3SelectionMode.NONE) {
            selectFirstButtonIfNeeded();
            return;
        }
        selectOnly(null);
    }

    /// Returns the user-agent stylesheet for M3FX toggle icon button groups.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("icon-toggle-button.css");
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

    /// Returns accessibility attributes for toggle icon button group content and selection state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` if the attribute is not supported
    /// @throws NullPointerException if `attribute` is `null`
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
                    M3IconToggleButton.class
            );
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedButtonsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for toggle icon buttons.
    ///
    /// @param action the accessibility action to execute
    /// @param parameters optional action-specific parameters
    /// @throws NullPointerException if `action` is `null`
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
                M3IconToggleButton.class
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
                M3IconToggleButton.class
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

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
        focusNotifier.start();
    }

    /// Applies keyboard navigation across enabled toggle icon buttons.
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
                            M3IconToggleButton.class
                    ),
                    M3IconToggleButton.class,
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
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedButton(), M3IconToggleButton.class),
                M3IconToggleButton.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this),
                this::select
        );
    }

    /// Applies selected toggle icon buttons supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            selectOnly(null);
            return;
        }
        if (getSelectionMode() == M3SelectionMode.SINGLE) {
            @Nullable M3IconToggleButton button =
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
                if (child instanceof M3IconToggleButton button && isSelectableButton(button)) {
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

    /// Installs a selected-state listener on a button.
    private void installButton(M3IconToggleButton button) {
        button.selectedProperty().addListener(selectedInvalidation);
        button.disabledProperty().addListener(reachabilityInvalidation);
        button.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes the selected-state listener from a button.
    private void uninstallButton(M3IconToggleButton button) {
        button.selectedProperty().removeListener(selectedInvalidation);
        button.disabledProperty().removeListener(reachabilityInvalidation);
        button.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Keeps selected buttons consistent with the current group policy.
    private void handleButtonSelectedChanged(M3IconToggleButton button, boolean selected) {
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
    private void handleButtonReachabilityChanged(M3IconToggleButton button) {
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

        M3IconToggleButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Sets one button's selected state and refreshes selected button state.
    private void setButtonSelected(M3IconToggleButton button, boolean selected) {
        setButtonSelectedWithoutRefresh(button, selected);
        refreshSelectedButtons();
    }

    /// Sets one button's selected state without refreshing the aggregate selected button list.
    private void setButtonSelectedWithoutRefresh(M3IconToggleButton button, boolean selected) {
        updatingSelection = true;
        try {
            button.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects one button and clears selection from the remaining buttons.
    private void selectOnly(@Nullable M3IconToggleButton button) {
        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3IconToggleButton toggleButton) {
                    toggleButton.setSelected(toggleButton == button);
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
            if (child instanceof M3IconToggleButton button && button.isSelected()) {
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

    /// Returns the first toggle icon button child.
    private @Nullable M3IconToggleButton firstButton() {
        return M3SelectionNavigation.first(getItems(), M3IconToggleButton.class);
    }

    /// Returns the first selectable button referenced by accessibility parameters.
    private @Nullable M3IconToggleButton firstAccessibleSelectableButton(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            if (child instanceof M3IconToggleButton button
                    && isSelectableButton(button)
                    && M3Accessible.containsSelectionTarget(button, parameters)) {
                return button;
            }
        }
        return null;
    }

    /// Returns whether an icon toggle button can currently participate in selection.
    private boolean isSelectableButton(M3IconToggleButton button) {
        return M3Accessible.isEffectivelyReachable(this) && M3Accessible.isEffectivelyReachable(button);
    }

    /// Creates the default Material Design 3 toggle icon button group skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3IconToggleButtonGroupSkin(this);
    }


    /// CSS metadata for icon toggle button group layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for icon toggle button spacing.
        private static final CssMetaData<M3IconToggleButtonGroup, Number> SPACING =
                new CssMetaData<>(
                        "-m3-icon-toggle-button-group-spacing",
                        SizeConverter.getInstance(),
                        DEFAULT_SPACING
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconToggleButtonGroup control) {
                        return M3Css.isSettable(control.spacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconToggleButtonGroup control) {
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
