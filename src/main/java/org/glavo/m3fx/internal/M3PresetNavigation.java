// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/// Provides keyboard traversal for picker dialog preset action containers.
@NotNullByDefault
public final class M3PresetNavigation {
    /// Prevents utility class instantiation.
    private M3PresetNavigation() {
    }

    /// Installs preset action keyboard navigation on one preset list.
    ///
    /// @param presetList the vertical list that owns preset action buttons
    /// @param orientationOwner the control whose effective orientation defines logical-start handoff
    /// @param focusPicker the action that moves focus from the preset list into the adjacent picker
    public static void installColumn(VBox presetList, Node orientationOwner, BooleanSupplier focusPicker) {
        Objects.requireNonNull(presetList, "presetList");
        Objects.requireNonNull(orientationOwner, "orientationOwner");
        Objects.requireNonNull(focusPicker, "focusPicker");
        presetList.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> handlePresetKeyPressed(event, presetList, orientationOwner, focusPicker)
        );
    }

    /// Installs wrapped-grid keyboard navigation on one preset action container.
    ///
    /// @param presetList the flow pane that owns preset action buttons
    /// @param orientationOwner the control whose effective orientation defines horizontal movement
    /// @param focusPicker the action that moves focus from the bottom preset row into the picker
    public static void installGrid(FlowPane presetList, Node orientationOwner, BooleanSupplier focusPicker) {
        Objects.requireNonNull(presetList, "presetList");
        Objects.requireNonNull(orientationOwner, "orientationOwner");
        Objects.requireNonNull(focusPicker, "focusPicker");
        presetList.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> handlePresetGridKeyPressed(event, presetList, orientationOwner, focusPicker)
        );
    }

    /// Handles keyboard navigation for one wrapped preset grid key event.
    private static void handlePresetGridKeyPressed(
            KeyEvent event,
            FlowPane presetList,
            Node orientationOwner,
            BooleanSupplier focusPicker
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(presetList, "presetList");
        Objects.requireNonNull(orientationOwner, "orientationOwner");
        Objects.requireNonNull(focusPicker, "focusPicker");
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        ObservableList<Node> children = presetList.getChildren();
        @Nullable M3Button focusedButton = M3SelectionNavigation.focused(children, M3Button.class);
        int columns = presetGridColumnCount(presetList, children);
        if (event.getCode() == KeyCode.UP) {
            if (focusGridOffset(presetList, children, focusedButton, -columns)) {
                event.consume();
            }
            return;
        }
        if (event.getCode() == KeyCode.DOWN) {
            if (focusGridOffset(presetList, children, focusedButton, columns) || focusPicker.getAsBoolean()) {
                event.consume();
            }
            return;
        }

        if (M3SelectionNavigation.handleKeyFocus(
                event,
                presetList,
                children,
                focusedButton,
                M3Button.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(orientationOwner)
        )) {
            return;
        }

        M3SelectionNavigation.handlePageKeyFocus(event, presetList, children, focusedButton, M3Button.class);
    }

    /// Moves focus by a whole visual grid row without allocating a temporary child list.
    private static boolean focusGridOffset(
            FlowPane presetList,
            ObservableList<Node> children,
            @Nullable M3Button focusedButton,
            int offset
    ) {
        if (children.isEmpty() || offset == 0) {
            return false;
        }

        if (focusedButton == null) {
            @Nullable M3Button target = offset > 0
                    ? M3SelectionNavigation.first(children, M3Button.class)
                    : M3SelectionNavigation.last(children, M3Button.class);
            return target != null && M3Accessible.showItem(presetList, target);
        }

        int index = children.indexOf(focusedButton);
        for (int targetIndex = index + offset;
             targetIndex >= 0 && targetIndex < children.size();
             targetIndex += offset) {
            Node target = children.get(targetIndex);
            if (target instanceof M3Button button && M3Accessible.isEffectivelyReachable(button)) {
                return M3Accessible.showItem(presetList, button);
            }
        }
        return false;
    }

    /// Returns the number of laid-out buttons in the first visual row.
    private static int presetGridColumnCount(FlowPane presetList, ObservableList<Node> children) {
        @Nullable Node first = null;
        for (Node child : children) {
            if (child.isManaged() && child.isVisible()) {
                first = child;
                break;
            }
        }
        if (first == null || first.getLayoutBounds().isEmpty()) {
            return Math.max(1, children.size());
        }

        double firstRowY = first.getLayoutY();
        int columns = 0;
        for (Node child : children) {
            if (child.isManaged() && child.isVisible() && Math.abs(child.getLayoutY() - firstRowY) < 0.5) {
                columns++;
            }
        }
        return Math.max(1, columns);
    }

    /// Handles keyboard navigation for one preset list key event.
    private static void handlePresetKeyPressed(
            KeyEvent event,
            VBox presetList,
            Node orientationOwner,
            BooleanSupplier focusPicker
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(presetList, "presetList");
        Objects.requireNonNull(orientationOwner, "orientationOwner");
        Objects.requireNonNull(focusPicker, "focusPicker");

        if (!M3KeyEvents.hasNavigationModifier(event)
                && event.getCode() == (M3NodeLayout.isRightToLeft(orientationOwner) ? KeyCode.LEFT : KeyCode.RIGHT)) {
            if (focusPicker.getAsBoolean()) {
                event.consume();
            }
            return;
        }

        ObservableList<Node> children = presetList.getChildren();
        @Nullable M3Button focusedButton = M3SelectionNavigation.focused(children, M3Button.class);
        if (M3SelectionNavigation.handleKeyFocus(
                event,
                presetList,
                children,
                focusedButton,
                M3Button.class,
                false,
                true
        )) {
            return;
        }

        M3SelectionNavigation.handlePageKeyFocus(event, presetList, children, focusedButton, M3Button.class);
    }

}
