// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNullByDefault;

/// Tracks keyboard-initiated focus visibility for Material state feedback.
@NotNullByDefault
final class M3FocusVisibleTracker {
    /// The pseudo-class used by m3fx CSS for keyboard-visible focus feedback.
    static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("m3-focus-visible");

    /// The owner node that receives the focus-visible pseudo-class.
    private final Node owner;

    /// Called after focus visibility changes.
    private final Runnable invalidation;

    /// Handles key presses as keyboard interaction.
    private final EventHandler<KeyEvent> keyPressedHandler = event -> {
        keyboardInteraction = true;
        updateFocusVisible();
    };

    /// Handles mouse presses as pointer interaction.
    private final EventHandler<MouseEvent> mousePressedHandler = event -> {
        keyboardInteraction = false;
        updateFocusVisible();
    };

    /// Handles owner focus changes.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> updateFocusVisible();

    /// Whether the latest relevant interaction was keyboard-driven.
    private boolean keyboardInteraction;

    /// Whether this tracker is currently installed.
    private boolean installed;

    /// Creates a focus-visible tracker.
    M3FocusVisibleTracker(Node owner, Runnable invalidation) {
        this.owner = owner;
        this.invalidation = invalidation;
    }

    /// Installs event and focus listeners.
    void install() {
        if (installed) {
            return;
        }

        installed = true;
        owner.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        owner.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        owner.focusedProperty().addListener(focusedListener);
        updateFocusVisible(false);
    }

    /// Uninstalls event and focus listeners.
    void uninstall() {
        if (!installed) {
            return;
        }

        installed = false;
        owner.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        owner.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        owner.focusedProperty().removeListener(focusedListener);
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, false);
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible() {
        updateFocusVisible(true);
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible(boolean notify) {
        boolean focusVisible = owner.isFocused() && keyboardInteraction;
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        if (notify) {
            invalidation.run();
        }
    }
}
