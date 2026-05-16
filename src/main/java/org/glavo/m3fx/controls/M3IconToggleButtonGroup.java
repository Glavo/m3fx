// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 toggle icon button group with mutually exclusive selection.
@NotNullByDefault
public class M3IconToggleButtonGroup extends HBox {
    /// The base style class for M3FX toggle icon button groups.
    public static final String STYLE_CLASS = "m3-icon-toggle-button-group";

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
                    if (selectedButton.get() == button) {
                        selectedButton.set(null);
                    }
                    button.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3IconToggleButton button) {
                    installButton(button);
                    if (button.isSelected()) {
                        selectButton(button);
                    }
                }
            }
        }
        if (!isAllowEmptySelection()) {
            selectFirstButtonIfNeeded();
        }
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
        Objects.requireNonNull(buttons, "buttons");
        for (M3IconToggleButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
        getItems().addAll(buttons);
    }

    /// Returns the mutable child list used as toggle icon button group content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Returns the selected toggle icon button.
    public final @Nullable M3IconToggleButton getSelectedButton() {
        return selectedButton.get();
    }

    /// Returns the selected toggle icon button property.
    public final ReadOnlyObjectProperty<@Nullable M3IconToggleButton> selectedButtonProperty() {
        return selectedButton.getReadOnlyProperty();
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
        selectButton(button);
    }

    /// Selects the first toggle icon button when one exists.
    public final void selectFirst() {
        M3IconToggleButton firstButton = firstButton();
        if (firstButton != null) {
            selectButton(firstButton);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstButtonIfNeeded();
            return;
        }
        selectButton(null);
    }

    /// Returns the user-agent stylesheet for M3FX toggle icon button groups.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("icon-toggle-button.css");
    }

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8.0);
        getChildren().addListener(childrenListener);
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

    /// Keeps selected buttons mutually exclusive.
    private void handleButtonSelectedChanged(M3IconToggleButton button, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (selected) {
            selectButton(button);
        } else if (selectedButton.get() == button) {
            if (isAllowEmptySelection()) {
                selectedButton.set(null);
            } else {
                selectButton(button);
            }
        }
    }

    /// Selects the first button when the selection is empty and empty selection is disabled.
    private void selectFirstButtonIfNeeded() {
        if (selectedButton.get() != null) {
            return;
        }

        M3IconToggleButton firstButton = firstButton();
        if (firstButton != null) {
            selectButton(firstButton);
        }
    }

    /// Selects one button and clears selection from the remaining buttons.
    private void selectButton(@Nullable M3IconToggleButton button) {
        updatingSelection = true;
        try {
            for (Node child : getChildren()) {
                if (child instanceof M3IconToggleButton toggleButton) {
                    toggleButton.setSelected(toggleButton == button);
                }
            }
            selectedButton.set(button);
        } finally {
            updatingSelection = false;
        }
    }

    /// Returns the first toggle icon button child.
    private @Nullable M3IconToggleButton firstButton() {
        for (Node child : getChildren()) {
            if (child instanceof M3IconToggleButton button) {
                return button;
            }
        }
        return null;
    }
}
