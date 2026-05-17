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
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 toggle icon button group.
@NotNullByDefault
public class M3IconToggleButtonGroup extends HBox {
    /// The base style class for M3FX toggle icon button groups.
    public static final String STYLE_CLASS = "m3-icon-toggle-button-group";

    /// The icon toggle button selection mode.
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

    /// The currently selected toggle icon button.
    private final ReadOnlyObjectWrapper<@Nullable M3IconToggleButton> selectedButton =
            new ReadOnlyObjectWrapper<>(this, "selectedButton");

    /// Whether the group allows all buttons to be unselected.
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
    };

    /// Whether the group is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty toggle icon button group.
    public M3IconToggleButtonGroup() {
        initialize();
    }

    /// Creates a toggle icon button group containing the supplied buttons.
    public M3IconToggleButtonGroup(M3IconToggleButton... buttons) {
        initialize();
        addButtons(buttons);
    }

    /// Returns the mutable child list used as toggle icon button group content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Adds one toggle icon button.
    public final void addButton(M3IconToggleButton button) {
        getItems().add(Objects.requireNonNull(button, "button"));
    }

    /// Adds toggle icon buttons.
    public final void addButtons(M3IconToggleButton... buttons) {
        validateButtons(buttons);
        getItems().addAll(buttons);
    }

    /// Replaces all toggle icon buttons.
    public final void setButtons(M3IconToggleButton... buttons) {
        validateButtons(buttons);
        getItems().setAll(buttons);
    }

    /// Removes all toggle icon button group content.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the icon toggle button selection mode.
    public final M3IconToggleButtonSelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the icon toggle button selection mode.
    public final void setSelectionMode(M3IconToggleButtonSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the icon toggle button selection mode property.
    public final ObjectProperty<M3IconToggleButtonSelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns the selected icon toggle buttons in child order.
    public final @UnmodifiableView ObservableList<M3IconToggleButton> getSelectedButtons() {
        return selectedButtonsView;
    }

    /// Returns the selected toggle icon button.
    public final @Nullable M3IconToggleButton getSelectedButton() {
        return selectedButton.get();
    }

    /// Returns the selected toggle icon button property.
    public final ReadOnlyObjectProperty<@Nullable M3IconToggleButton> selectedButtonProperty() {
        return selectedButton.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected toggle icon button, or `-1` when none is selected.
    public final int getSelectedIndex() {
        @Nullable M3IconToggleButton button = getSelectedButton();
        return button == null ? -1 : getChildren().indexOf(button);
    }

    /// Returns whether this group allows all buttons to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this group allows all buttons to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a toggle icon button that belongs to this group.
    public final void select(M3IconToggleButton button) {
        Objects.requireNonNull(button, "button");
        if (!getChildren().contains(button)) {
            throw new IllegalArgumentException("button must belong to this toggle icon button group");
        }
        if (getSelectionMode() == M3IconToggleButtonSelectionMode.MULTIPLE) {
            setButtonSelected(button, true);
        } else {
            selectOnly(button);
        }
    }

    /// Selects the toggle icon button at the given child index.
    public final void selectIndex(int index) {
        Node child = getChildren().get(index);
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
                M3SelectionNavigation.last(getChildren(), M3IconToggleButton.class);
        if (lastButton != null) {
            select(lastButton);
        }
    }

    /// Selects the next toggle icon button after the current selected button, wrapping at the end.
    public final void selectNext() {
        @Nullable M3IconToggleButton nextButton =
                M3SelectionNavigation.next(getChildren(), getSelectedButton(), M3IconToggleButton.class);
        if (nextButton != null) {
            select(nextButton);
        }
    }

    /// Selects the previous toggle icon button before the current selected button, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3IconToggleButton previousButton =
                M3SelectionNavigation.previous(getChildren(), getSelectedButton(), M3IconToggleButton.class);
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

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8.0);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getChildren().addListener(childrenListener);
    }

    /// Applies keyboard navigation across enabled toggle icon buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                getChildren(),
                getSelectedButton(),
                M3IconToggleButton.class,
                true,
                false,
                this::select
        );
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
            for (Node child : getChildren()) {
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
        selectedButtons.clear();
        for (Node child : getChildren()) {
            if (child instanceof M3IconToggleButton button && button.isSelected()) {
                selectedButtons.add(button);
            }
        }
        selectedButton.set(selectedButtons.isEmpty() ? null : selectedButtons.get(0));
    }

    /// Returns the first toggle icon button child.
    private @Nullable M3IconToggleButton firstButton() {
        return M3SelectionNavigation.first(getChildren(), M3IconToggleButton.class);
    }

    /// Validates a toggle icon button array.
    private static void validateButtons(M3IconToggleButton... buttons) {
        Objects.requireNonNull(buttons, "buttons");
        for (M3IconToggleButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
    }
}
