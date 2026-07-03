// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/// Provides keyboard traversal for picker field preset action columns.
@NotNullByDefault
final class M3PresetNavigation {
    /// Prevents utility class instantiation.
    private M3PresetNavigation() {
    }

    /// Installs preset action keyboard navigation on one preset list.
    ///
    /// @param presetList the vertical list that owns preset action buttons
    /// @param orientationOwner the control whose effective orientation defines logical-start handoff
    /// @param focusPicker the action that moves focus from the preset list into the adjacent picker
    static void install(VBox presetList, Node orientationOwner, BooleanSupplier focusPicker) {
        Objects.requireNonNull(presetList, "presetList");
        Objects.requireNonNull(orientationOwner, "orientationOwner");
        Objects.requireNonNull(focusPicker, "focusPicker");
        presetList.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> handlePresetKeyPressed(event, presetList, orientationOwner, focusPicker)
        );
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