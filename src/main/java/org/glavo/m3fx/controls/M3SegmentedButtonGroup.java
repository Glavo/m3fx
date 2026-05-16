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
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 segmented button group that lays out adjacent segments.
@NotNullByDefault
public class M3SegmentedButtonGroup extends HBox {
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

    /// The default spacing that lets adjacent segment borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

    /// The segmented button selection mode.
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

    /// The currently selected segmented button.
    private final ReadOnlyObjectWrapper<@Nullable M3SegmentedButton> selectedButton =
            new ReadOnlyObjectWrapper<>(this, "selectedButton");

    /// Whether the group allows all segmented buttons to be unselected.
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
    };

    /// Whether the group is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty segmented button group.
    public M3SegmentedButtonGroup() {
        initialize();
    }

    /// Creates a segmented button group with the supplied buttons.
    public M3SegmentedButtonGroup(M3SegmentedButton... buttons) {
        initialize();
        Objects.requireNonNull(buttons, "buttons");
        for (M3SegmentedButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
        getItems().addAll(buttons);
    }

    /// Returns the mutable child list used as segmented button group content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Returns the segmented button selection mode.
    public final M3SegmentedButtonSelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the segmented button selection mode.
    public final void setSelectionMode(M3SegmentedButtonSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the segmented button selection mode property.
    public final ObjectProperty<M3SegmentedButtonSelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns the selected segmented buttons in child order.
    public final @UnmodifiableView ObservableList<M3SegmentedButton> getSelectedButtons() {
        return selectedButtonsView;
    }

    /// Returns the selected segmented button.
    public final @Nullable M3SegmentedButton getSelectedButton() {
        return selectedButton.get();
    }

    /// Returns the selected segmented button property.
    public final ReadOnlyObjectProperty<@Nullable M3SegmentedButton> selectedButtonProperty() {
        return selectedButton.getReadOnlyProperty();
    }

    /// Returns whether this group allows all segmented buttons to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this group allows all segmented buttons to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a segmented button that belongs to this group.
    public final void select(M3SegmentedButton button) {
        Objects.requireNonNull(button, "button");
        if (!getChildren().contains(button)) {
            throw new IllegalArgumentException("button must belong to this segmented button group");
        }
        if (getSelectionMode() == M3SegmentedButtonSelectionMode.MULTIPLE) {
            setButtonSelected(button, true);
        } else {
            selectOnly(button);
        }
    }

    /// Selects the first segmented button when one exists.
    public final void selectFirst() {
        M3SegmentedButton firstButton = firstButton();
        if (firstButton != null) {
            select(firstButton);
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
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("segmented-button.css");
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setSpacing(DEFAULT_SPACING);
        getChildren().addListener(childrenListener);
        updateSegmentStyles();
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
            for (Node child : getChildren()) {
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
        selectedButtons.clear();
        for (Node child : getChildren()) {
            if (child instanceof M3SegmentedButton button && button.isSelected()) {
                selectedButtons.add(button);
            }
        }
        selectedButton.set(selectedButtons.isEmpty() ? null : selectedButtons.get(0));
    }

    /// Returns the first segmented button child.
    private @Nullable M3SegmentedButton firstButton() {
        for (Node child : getChildren()) {
            if (child instanceof M3SegmentedButton button) {
                return button;
            }
        }
        return null;
    }

    /// Applies first, middle, last, or single segment style classes.
    private void updateSegmentStyles() {
        int segmentCount = 0;
        for (Node child : getChildren()) {
            if (child instanceof M3SegmentedButton) {
                segmentCount++;
            }
        }

        int segmentIndex = 0;
        for (Node child : getChildren()) {
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
}
