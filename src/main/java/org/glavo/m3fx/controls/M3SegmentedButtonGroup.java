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
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.AccessibleRole;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
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
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
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
        addButtons(buttons);
    }

    /// Returns the mutable child list used as segmented button group content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Adds one segmented button.
    public final void addButton(M3SegmentedButton button) {
        getItems().add(Objects.requireNonNull(button, "button"));
    }

    /// Adds segmented buttons.
    public final void addButtons(M3SegmentedButton... buttons) {
        validateButtons(buttons);
        getItems().addAll(buttons);
    }

    /// Replaces all segmented buttons.
    public final void setButtons(M3SegmentedButton... buttons) {
        validateButtons(buttons);
        getItems().setAll(buttons);
    }

    /// Removes all segmented button group content.
    public final void clearItems() {
        getItems().clear();
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

    /// Returns the child index of the first selected segmented button, or `-1` when none is selected.
    public final int getSelectedIndex() {
        @Nullable M3SegmentedButton button = getSelectedButton();
        return button == null ? -1 : getChildren().indexOf(button);
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

    /// Selects the segmented button at the given child index.
    public final void selectIndex(int index) {
        Node child = getChildren().get(index);
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
                M3SelectionNavigation.last(getChildren(), M3SegmentedButton.class);
        if (lastButton != null) {
            select(lastButton);
        }
    }

    /// Selects the next segmented button after the current selected button, wrapping at the end.
    public final void selectNext() {
        @Nullable M3SegmentedButton nextButton =
                M3SelectionNavigation.next(getChildren(), getSelectedButton(), M3SegmentedButton.class);
        if (nextButton != null) {
            select(nextButton);
        }
    }

    /// Selects the previous segmented button before the current selected button, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3SegmentedButton previousButton =
                M3SelectionNavigation.previous(getChildren(), getSelectedButton(), M3SegmentedButton.class);
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
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("segmented-button.css");
    }

    /// Returns accessibility attributes for segmented button group content and selection state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SegmentedButtonSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedButtonsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for segmented buttons.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setSpacing(DEFAULT_SPACING);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getChildren().addListener(childrenListener);
        updateSegmentStyles();
    }

    /// Applies keyboard navigation across enabled segmented buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                getChildren(),
                getSelectedButton(),
                M3SegmentedButton.class,
                true,
                false,
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
            for (Node child : getChildren()) {
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
        List<M3SegmentedButton> previousSelection = List.copyOf(selectedButtons);
        selectedButtons.clear();
        for (Node child : getChildren()) {
            if (child instanceof M3SegmentedButton button && button.isSelected()) {
                selectedButtons.add(button);
            }
        }
        selectedButton.set(selectedButtons.isEmpty() ? null : selectedButtons.get(0));
        if (!selectedButtons.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        }
    }

    /// Returns the first segmented button child.
    private @Nullable M3SegmentedButton firstButton() {
        return M3SelectionNavigation.first(getChildren(), M3SegmentedButton.class);
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

    /// Validates a segmented button array.
    private static void validateButtons(M3SegmentedButton... buttons) {
        Objects.requireNonNull(buttons, "buttons");
        for (M3SegmentedButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
    }
}
