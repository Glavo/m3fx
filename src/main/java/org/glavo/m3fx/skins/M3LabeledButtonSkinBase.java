// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

/// A base animated skin for m3fx labeled button controls.
@NotNullByDefault
abstract class M3LabeledButtonSkinBase<C extends ButtonBase> extends LabeledSkinBase<C> {
    /// The scale applied while the button is pressed.
    private static final double PRESSED_SCALE = 0.98;

    /// The duration used when entering the pressed state.
    private static final Duration PRESS_DURATION = Duration.millis(80.0);

    /// The duration used when leaving the pressed state.
    private static final Duration RELEASE_DURATION = Duration.millis(140.0);

    /// The press animation timeline.
    private final Timeline animation = new Timeline();

    /// Handles primary mouse presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary mouse releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles pointer entry while a mouse press is active.
    private final EventHandler<MouseEvent> mouseEnteredHandler = this::handleMouseEntered;

    /// Handles pointer exit while a mouse press is active.
    private final EventHandler<MouseEvent> mouseExitedHandler = this::handleMouseExited;

    /// Handles keyboard activation presses.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Handles keyboard activation releases.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Creates an animated labeled button skin.
    M3LabeledButtonSkinBase(C control) {
        super(control);
        control.setScaleX(1.0);
        control.setScaleY(1.0);
        installInteractionHandlers(control);
        control.armedProperty().addListener((observable, oldValue, newValue) -> animatePressedState(newValue));
        control.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                animation.stop();
                control.disarm();
                control.setScaleX(1.0);
                control.setScaleY(1.0);
            }
        });
    }

    /// Stops the animation before the skin is disposed.
    @Override
    public void dispose() {
        animation.stop();
        uninstallInteractionHandlers(getSkinnable());
        super.dispose();
    }

    /// Installs mouse and keyboard behavior handlers.
    private void installInteractionHandlers(C button) {
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        button.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        button.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        button.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Removes mouse and keyboard behavior handlers.
    private void uninstallInteractionHandlers(C button) {
        button.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        button.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        button.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        button.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        button.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        button.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Arms the button on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        C button = getSkinnable();
        if (button.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        if (button.isFocusTraversable()) {
            button.requestFocus();
        }
        button.arm();
        event.consume();
    }

    /// Fires the button when a primary mouse press is released inside the control.
    private void handleMouseReleased(MouseEvent event) {
        C button = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = button.isArmed() && button.contains(event.getX(), event.getY());
        mousePressed = false;
        button.disarm();
        if (shouldFire) {
            button.fire();
        }
        event.consume();
    }

    /// Re-arms the button when a pressed pointer re-enters the control.
    private void handleMouseEntered(MouseEvent event) {
        C button = getSkinnable();
        if (mousePressed && !button.isDisabled()) {
            button.arm();
            event.consume();
        }
    }

    /// Disarms the button when a pressed pointer exits the control.
    private void handleMouseExited(MouseEvent event) {
        C button = getSkinnable();
        if (mousePressed && !button.isDisabled()) {
            button.disarm();
            event.consume();
        }
    }

    /// Handles keyboard activation for enter and space.
    private void handleKeyPressed(KeyEvent event) {
        C button = getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                button.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            button.fire();
            event.consume();
        }
    }

    /// Fires the button when a space key activation is released.
    private void handleKeyReleased(KeyEvent event) {
        C button = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = button.isArmed() && !button.isDisabled();
        spaceKeyPressed = false;
        button.disarm();
        if (shouldFire) {
            button.fire();
        }
        event.consume();
    }

    /// Animates the skinnable button into or out of the pressed state.
    private void animatePressedState(boolean pressed) {
        C button = getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        double scale = pressed ? PRESSED_SCALE : 1.0;
        Duration duration = pressed ? PRESS_DURATION : RELEASE_DURATION;
        animation.stop();
        animation.getKeyFrames().setAll(new KeyFrame(
                duration,
                new KeyValue(button.scaleXProperty(), scale, Interpolator.EASE_BOTH),
                new KeyValue(button.scaleYProperty(), scale, Interpolator.EASE_BOTH)
        ));
        animation.playFromStart();
    }
}
