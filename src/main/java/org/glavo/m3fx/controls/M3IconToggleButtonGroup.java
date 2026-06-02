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
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3IconToggleButtonGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 toggle icon button group.
///
/// The group manages selected state for child [M3IconToggleButton] controls, applies
/// [M3IconToggleButtonSelectionMode], exposes selected-button views, and supports keyboard traversal. Use it
/// when a row or cluster of icon buttons represents one logical choice set.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public class M3IconToggleButtonGroup extends Control {
    /// The base style class for M3FX toggle icon button groups.
    public static final String STYLE_CLASS = "m3-icon-toggle-button-group";

    /// The default spacing between toggle icon buttons.
    private static final double DEFAULT_SPACING = 8.0;

    /// The mutable toggle icon button group content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between toggle icon buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(
                            this,
                            getItems(),
                            getSelectedButton(),
                            M3IconToggleButton.class
                    ));

    // The styleable spacing between toggle icon buttons.
    private @Nullable StyleableDoubleProperty spacing;

    // The icon toggle button selection mode.
    private final ObjectProperty<M3IconToggleButtonSelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3IconToggleButtonSelectionMode.SINGLE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3IconToggleButtonSelectionMode.SINGLE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// The selected icon toggle buttons in child order.
    private final ObservableList<M3IconToggleButton> selectedButtons = FXCollections.observableArrayList();

    /// The read-only selected icon toggle button view.
    private final @UnmodifiableView ObservableList<M3IconToggleButton> selectedButtonsView =
            FXCollections.unmodifiableObservableList(selectedButtons);

    // The currently selected toggle icon button.
    private final ReadOnlyObjectWrapper<@Nullable M3IconToggleButton> selectedButton =
            new ReadOnlyObjectWrapper<>(this, "selectedButton");

    // Whether the group allows all buttons to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected button when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstButtonIfNeeded();
            }
        }
    };

    /// The selected-state listeners installed on group buttons.
    private final Map<M3IconToggleButton, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

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

    /// Creates an empty toggle icon button group.
    public M3IconToggleButtonGroup() {
        initialize();
    }

    /// Creates a toggle icon button group containing the supplied buttons.
    ///
    /// @param buttons the initial toggle icon buttons
    public M3IconToggleButtonGroup(M3IconToggleButton... buttons) {
        initialize();
        addButtons(buttons);
    }

    /// Returns the mutable child list used as toggle icon button group content.
    ///
    /// @return the mutable child list used as toggle icon button group content
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Returns the spacing between toggle icon buttons.
    ///
    /// @return the child spacing in pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between toggle icon buttons.
    ///
    /// @param spacing the child spacing in pixels
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.nonNegative(spacing, "spacing"));
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
                    M3Css.nonNegative(get(), "spacing");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3IconToggleButtonGroup.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "spacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3IconToggleButtonGroup, Number> getCssMetaData() {
                    return StyleableProperties.SPACING;
                }
            };
        }
        return spacing;
    }

    /// Adds one toggle icon button.
    ///
    /// @param button the toggle icon button to add
    public final void addButton(M3IconToggleButton button) {
        getItems().add(Objects.requireNonNull(button, "button"));
    }

    /// Adds toggle icon buttons.
    ///
    /// @param buttons the toggle icon buttons to add
    public final void addButtons(M3IconToggleButton... buttons) {
        validateButtons(buttons);
        getItems().addAll(buttons);
    }

    /// Replaces all toggle icon buttons.
    ///
    /// @param buttons the replacement toggle icon buttons
    public final void setButtons(M3IconToggleButton... buttons) {
        validateButtons(buttons);
        getItems().setAll(buttons);
    }

    /// Removes all toggle icon button group content.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the icon toggle button selection mode.
    ///
    /// @return the icon toggle button selection mode
    public final M3IconToggleButtonSelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the icon toggle button selection mode.
    ///
    /// @param selectionMode the icon toggle button selection mode
    public final void setSelectionMode(M3IconToggleButtonSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the icon toggle button selection mode property.
    ///
    /// @return the icon toggle button selection mode property
    public final ObjectProperty<M3IconToggleButtonSelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns the selected icon toggle buttons in child order.
    ///
    /// @return the selected icon toggle buttons in child order
    public final @UnmodifiableView ObservableList<M3IconToggleButton> getSelectedButtons() {
        return selectedButtonsView;
    }

    /// Returns the selected toggle icon button.
    ///
    /// @return the selected toggle icon button, or `null` when there is no selected button
    public final @Nullable M3IconToggleButton getSelectedButton() {
        return selectedButton.get();
    }

    /// Returns the selected toggle icon button property.
    ///
    /// @return the selected toggle icon button property
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

    /// Returns the empty-selection policy property.
    ///
    /// @return the empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a toggle icon button that belongs to this group.
    ///
    /// @param button the toggle icon button to select
    public final void select(M3IconToggleButton button) {
        Objects.requireNonNull(button, "button");
        if (!getItems().contains(button)) {
            throw new IllegalArgumentException("button must belong to this toggle icon button group");
        }
        if (getSelectionMode() == M3IconToggleButtonSelectionMode.MULTIPLE) {
            setButtonSelected(button, true);
        } else {
            selectOnly(button);
        }
    }

    /// Selects the toggle icon button at the given child index.
    ///
    /// @param index the child index to select
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
        M3IconToggleButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Selects the last toggle icon button when one exists.
    public final void selectLast() {
        @Nullable M3IconToggleButton lastButton =
                M3SelectionNavigation.last(getItems(), M3IconToggleButton.class);
        if (lastButton != null) {
            select(lastButton);
        }
    }

    /// Selects the next toggle icon button after the current selected button, wrapping at the end.
    public final void selectNext() {
        @Nullable M3IconToggleButton nextButton =
                M3SelectionNavigation.next(getItems(), getSelectedButton(), M3IconToggleButton.class);
        if (nextButton != null) {
            select(nextButton);
        }
    }

    /// Selects the previous toggle icon button before the current selected button, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3IconToggleButton previousButton =
                M3SelectionNavigation.previous(getItems(), getSelectedButton(), M3IconToggleButton.class);
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
            case MULTIPLE_SELECTION -> getSelectionMode() == M3IconToggleButtonSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedButtonsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for toggle icon buttons.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedButton(),
                    M3IconToggleButton.class
            ));
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> M3Accessible.showItemOrDefault(M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedButton(),
                    M3IconToggleButton.class
            ), getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
        focusNotifier.start();
    }

    /// Applies keyboard navigation across enabled toggle icon buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3IconToggleButtonSelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(
                            getItems(),
                            getSelectedButton(),
                            M3IconToggleButton.class
                    ),
                    M3IconToggleButton.class,
                    true,
                    false,
                    M3SelectionNavigation.isRightToLeft(this)
            );
            return;
        }

        M3SelectionNavigation.handleKeySelection(
                event,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedButton(), M3IconToggleButton.class),
                M3IconToggleButton.class,
                true,
                false,
                M3SelectionNavigation.isRightToLeft(this),
                this::select
        );
    }

    /// Applies selected toggle icon buttons supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3IconToggleButtonSelectionMode.SINGLE) {
            @Nullable M3IconToggleButton button =
                    M3Accessible.firstSelectionTarget(getItems(), M3IconToggleButton.class, parameters);
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
                if (child instanceof M3IconToggleButton button) {
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
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleButtonSelectedChanged(button, newValue);
        selectedListeners.put(button, listener);
        button.selectedProperty().addListener(listener);
    }

    /// Removes the selected-state listener from a button.
    private void uninstallButton(M3IconToggleButton button) {
        ChangeListener<Boolean> listener = selectedListeners.remove(button);
        if (listener != null) {
            button.selectedProperty().removeListener(listener);
        }
    }

    /// Keeps selected buttons consistent with the current group policy.
    private void handleButtonSelectedChanged(M3IconToggleButton button, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (selected) {
            if (getSelectionMode() == M3IconToggleButtonSelectionMode.SINGLE) {
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
        if (getSelectionMode() == M3IconToggleButtonSelectionMode.SINGLE && selectedButtons.size() > 1) {
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

        M3IconToggleButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
        }
    }

    /// Sets one button's selected state and refreshes selected button state.
    private void setButtonSelected(M3IconToggleButton button, boolean selected) {
        updatingSelection = true;
        try {
            button.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
        refreshSelectedButtons();
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
        List<M3IconToggleButton> previousSelection = List.copyOf(selectedButtons);
        selectedButtons.clear();
        for (Node child : getItems()) {
            if (child instanceof M3IconToggleButton button && button.isSelected()) {
                selectedButtons.add(button);
            }
        }
        selectedButton.set(selectedButtons.isEmpty() ? null : selectedButtons.get(0));
        if (!selectedButtons.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Returns the first toggle icon button child.
    private @Nullable M3IconToggleButton firstButton() {
        return M3SelectionNavigation.first(getItems(), M3IconToggleButton.class);
    }

    /// Creates the default Material Design 3 toggle icon button group skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3IconToggleButtonGroupSkin(this);
    }

    /// Validates a toggle icon button array.
    private static void validateButtons(M3IconToggleButton... buttons) {
        Objects.requireNonNull(buttons, "buttons");
        for (M3IconToggleButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
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
