// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Incrementally maintains the preset action container owned by one Material picker dialog.
///
/// The controller creates buttons only for added or replaced presets, preserves unaffected button nodes across
/// bounds and list changes, and shares one action handler across the complete container.
///
/// @param <P> the immutable picker preset type
@NotNullByDefault
public abstract class M3PickerPresetController<P>
        implements ListChangeListener<P>, EventHandler<ActionEvent> {
    /// The source preset list mirrored into the button container.
    private final ObservableList<P> presets;

    /// The private dialog-owned button container.
    private final Pane buttonContainer;

    /// The style class applied to every preset action button.
    private final String buttonStyleClass;

    /// Whether source observation and initial button creation have completed.
    private boolean installed;

    /// Whether the previous synchronized source state contained presets.
    private boolean hasPresets;

    /// Creates a controller for one picker dialog preset list.
    ///
    /// @param presets the non-null preset source list
    /// @param buttonContainer the private button container that mirrors the source list
    /// @param buttonStyleClass the style class applied to created action buttons
    protected M3PickerPresetController(
            ObservableList<P> presets,
            Pane buttonContainer,
            String buttonStyleClass
    ) {
        this.presets = Objects.requireNonNull(presets, "presets");
        this.buttonContainer = Objects.requireNonNull(buttonContainer, "buttonContainer");
        this.buttonStyleClass = Objects.requireNonNull(buttonStyleClass, "buttonStyleClass");
    }

    /// Creates the initial button set and starts observing preset list changes.
    public final void install() {
        if (installed) {
            return;
        }

        installed = true;
        try {
            for (P preset : presets) {
                buttonContainer.getChildren().add(createButton(preset));
            }
            hasPresets = !presets.isEmpty();
            presets.addListener(this);
            updateButtonContainerPresence(hasPresets);
        } catch (RuntimeException | Error exception) {
            presets.removeListener(this);
            detachButtons(0, buttonContainer.getChildren().size());
            buttonContainer.getChildren().clear();
            installed = false;
            hasPresets = false;
            throw exception;
        }
    }

    /// Refreshes disabled states without replacing buttons or reparenting the picker.
    public final void refreshDisabledStates() {
        if (!installed) {
            return;
        }

        List<Node> buttons = buttonContainer.getChildren();
        int count = Math.min(buttons.size(), presets.size());
        for (int index = 0; index < count; index++) {
            M3Button button = (M3Button) buttons.get(index);
            boolean disabled = isPresetDisabled(presets.get(index));
            if (button.isDisabled() != disabled) {
                button.setDisable(disabled);
            }
        }
    }

    /// Applies one source list change directly to the existing button column.
    ///
    /// @param change the source list change
    @Override
    public final void onChanged(Change<? extends P> change) {
        while (change.next()) {
            if (change.wasPermutated()) {
                applyPermutation(change);
            } else if (change.wasUpdated()) {
                replaceUpdatedButtons(change);
            } else {
                applyAddRemove(change);
            }
        }

        boolean nextHasPresets = !presets.isEmpty();
        if (hasPresets != nextHasPresets) {
            hasPresets = nextHasPresets;
            updateButtonContainerPresence(nextHasPresets);
        }
    }

    /// Applies a button action to the preset at the button's current list index.
    ///
    /// @param event the button action event
    @Override
    public final void handle(ActionEvent event) {
        if (!(event.getSource() instanceof M3Button button) || button.isDisabled()) {
            return;
        }

        int index = buttonContainer.getChildren().indexOf(button);
        if (index >= 0 && index < presets.size()) {
            applyPreset(presets.get(index));
        }
    }

    /// Returns the text rendered by one preset action button.
    ///
    /// @param preset the source preset
    /// @return the button text
    protected abstract String presetText(P preset);

    /// Applies one selected preset to the owning picker.
    ///
    /// @param preset the selected preset
    protected abstract void applyPreset(P preset);

    /// Returns whether one preset currently falls outside the picker bounds.
    ///
    /// @param preset the source preset
    /// @return whether the corresponding action button is disabled
    protected abstract boolean isPresetDisabled(P preset);

    /// Creates one action button for a newly inserted preset.
    private M3Button createButton(P preset) {
        M3Button button = new M3Button(presetText(preset), M3ButtonVariant.TEXT);
        button.getStyleClass().add(buttonStyleClass);
        button.setWrapText(true);
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(isPresetDisabled(preset));
        button.setOnAction(this);
        return button;
    }

    /// Applies one contiguous source removal and addition without touching unaffected buttons.
    private void applyAddRemove(Change<? extends P> change) {
        int from = change.getFrom();
        int removedSize = change.getRemovedSize();
        if (removedSize > 0) {
            detachButtons(from, from + removedSize);
            buttonContainer.getChildren().remove(from, from + removedSize);
        }

        int insertionIndex = from;
        for (P preset : change.getAddedSubList()) {
            buttonContainer.getChildren().add(insertionIndex++, createButton(preset));
        }
    }

    /// Replaces buttons whose source elements emitted an update event.
    private void replaceUpdatedButtons(Change<? extends P> change) {
        for (int index = change.getFrom(); index < change.getTo(); index++) {
            ((M3Button) buttonContainer.getChildren().get(index)).setOnAction(null);
            buttonContainer.getChildren().set(index, createButton(presets.get(index)));
        }
    }

    /// Clears controller references from buttons that are about to leave the private column.
    private void detachButtons(int from, int to) {
        List<Node> children = buttonContainer.getChildren();
        for (int index = from; index < to; index++) {
            ((M3Button) children.get(index)).setOnAction(null);
        }
    }

    /// Includes the preset container in layout only while it owns at least one action.
    private void updateButtonContainerPresence(boolean visible) {
        if (buttonContainer.isManaged() != visible) {
            buttonContainer.setManaged(visible);
        }
        if (buttonContainer.isVisible() != visible) {
            buttonContainer.setVisible(visible);
        }
    }

    /// Reorders existing buttons for a source permutation without creating replacement controls.
    private void applyPermutation(Change<? extends P> change) {
        int from = change.getFrom();
        int to = change.getTo();
        int count = to - from;
        if (count < 2) {
            return;
        }

        ObservableList<Node> children = buttonContainer.getChildren();
        ArrayList<Node> previousOrder = new ArrayList<>(children.subList(from, to));
        ArrayList<Node> nextOrder = new ArrayList<>(previousOrder);
        for (int oldIndex = from; oldIndex < to; oldIndex++) {
            int newIndex = change.getPermutation(oldIndex);
            nextOrder.set(newIndex - from, previousOrder.get(oldIndex - from));
        }
        children.remove(from, to);
        children.addAll(from, nextOrder);
    }
}
