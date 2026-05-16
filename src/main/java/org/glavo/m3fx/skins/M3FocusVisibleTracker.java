// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/// Tracks focus-visible changes for Material state feedback.
@NotNullByDefault
final class M3FocusVisibleTracker {
    /// Resolves JavaFX native focus-visible support when it is available at runtime.
    private static final @Nullable MethodHandle FOCUS_VISIBLE_PROPERTY_HANDLE = focusVisiblePropertyHandle();

    /// The pseudo-class used by m3fx CSS for keyboard-visible focus feedback.
    static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The owner node whose focus-visible state is tracked.
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

    /// Handles native JavaFX focus-visible changes.
    private final ChangeListener<Boolean> nativeFocusVisibleListener;

    /// The native JavaFX focus-visible property, or null when the runtime does not expose it.
    private final @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty;

    /// Whether the latest relevant interaction was keyboard-driven.
    private boolean keyboardInteraction;

    /// Whether this tracker is currently installed.
    private boolean installed;

    /// Creates a focus-visible tracker.
    M3FocusVisibleTracker(Node owner, Runnable invalidation) {
        this.owner = owner;
        this.invalidation = invalidation;
        this.nativeFocusVisibleListener = (observable, oldValue, newValue) -> invalidation.run();
        this.nativeFocusVisibleProperty = nativeFocusVisibleProperty(owner);
    }

    /// Installs native focus-visible or fallback event listeners.
    void install() {
        if (installed) {
            return;
        }

        installed = true;
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        if (nativeProperty != null) {
            nativeProperty.addListener(nativeFocusVisibleListener);
            return;
        }

        owner.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        owner.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        owner.focusedProperty().addListener(focusedListener);
        updateFocusVisible(false);
    }

    /// Uninstalls native focus-visible or fallback event listeners.
    void uninstall() {
        if (!installed) {
            return;
        }

        installed = false;
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        if (nativeProperty != null) {
            nativeProperty.removeListener(nativeFocusVisibleListener);
            return;
        }

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

    /// Returns a method handle for JavaFX native focus-visible support when the runtime exposes it.
    private static @Nullable MethodHandle focusVisiblePropertyHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    Node.class,
                    "focusVisibleProperty",
                    MethodType.methodType(ReadOnlyBooleanProperty.class)
            );
        } catch (IllegalAccessException | NoSuchMethodException ignored) {
            return null;
        }
    }

    /// Returns the native JavaFX focus-visible property for the owner when it is available.
    private static @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty(Node owner) {
        MethodHandle handle = FOCUS_VISIBLE_PROPERTY_HANDLE;
        if (handle == null) {
            return null;
        }

        try {
            return (ReadOnlyBooleanProperty) handle.invoke(owner);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
