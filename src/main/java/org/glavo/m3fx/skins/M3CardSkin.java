// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Card].
@NotNullByDefault
public class M3CardSkin extends SkinBase<M3Card> {
    /// The transient activation pseudo-class shared with button behavior.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The container that hosts the card content.
    private final StackPane container = new StackPane();

    /// The bounded state layer for card feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// Animates CSS-resolved elevation changes on the card container.
    private final M3CssEffectTransition effectTransition;

    /// Handles primary mouse presses on the card surface.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary mouse releases on the card surface.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles pointer movement while a card press owns the gesture.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles keyboard activation feedback.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Handles keyboard activation release feedback.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Updates hosted card content after content changes.
    private final ChangeListener<Node> contentListener =
            (observable, oldValue, newValue) -> updateContent(newValue);

    /// Applies token changes to the card surface.
    private final InvalidationListener tokenInvalidation = observable -> updateTokenStyles();

    /// Updates interaction infrastructure when direct action or dragged state changes.
    private final InvalidationListener interactionStateInvalidation = observable -> updateInteractionState();

    /// Clears an active gesture when the card becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Whether the primary pointer currently owns card activation.
    private boolean pointerPressed;

    /// Whether a held space key currently owns card activation.
    private boolean spaceKeyPressed;

    /// Whether state and elevation transitions are currently installed.
    private boolean interactionTransitionsInstalled;

    /// Creates a card skin.
    ///
    /// @param control the card controlled by this skin
    public M3CardSkin(M3Card control) {
        super(control);
        effectTransition = new M3CssEffectTransition(control, container);
        container.getStyleClass().add("m3-card-container");
        getChildren().setAll(container);
        updateContent(control.getContent());
        updateTokenStyles();
        installInteractionHandlers(control);
        control.contentProperty().addListener(contentListener);
        control.containerShapeProperty().addListener(tokenInvalidation);
        control.contentPaddingProperty().addListener(tokenInvalidation);
        control.outlineWidthProperty().addListener(tokenInvalidation);
        control.onActionProperty().addListener(interactionStateInvalidation);
        control.draggedProperty().addListener(interactionStateInvalidation);
        control.disabledProperty().addListener(disabledListener);
        updateInteractionState();
    }

    /// Removes interaction handlers before the skin is disposed.
    @Override
    public void dispose() {
        M3Card card = getSkinnable();
        resetInteractionState();
        uninstallInteractionTransitions();
        card.contentProperty().removeListener(contentListener);
        card.containerShapeProperty().removeListener(tokenInvalidation);
        card.contentPaddingProperty().removeListener(tokenInvalidation);
        card.outlineWidthProperty().removeListener(tokenInvalidation);
        card.onActionProperty().removeListener(interactionStateInvalidation);
        card.draggedProperty().removeListener(interactionStateInvalidation);
        card.disabledProperty().removeListener(disabledListener);
        card.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        card.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        card.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        card.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        card.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Lays out the card surface and bounded state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
        stateLayer.layoutLayer(0.0, 0.0, width, height, getSkinnable().getContainerShape());
    }

    /// Updates the content hosted by this skin.
    private void updateContent(@Nullable Node content) {
        if (content == null) {
            container.getChildren().setAll(stateLayer);
        } else {
            container.getChildren().setAll(content, stateLayer);
        }
    }

    /// Applies styleable component tokens to the card container.
    private void updateTokenStyles() {
        M3Card card = getSkinnable();
        container.setPadding(new Insets(card.getContentPadding()));
        String shape = formatPixels(card.getContainerShape());
        String outlineWidth = formatPixels(card.getOutlineWidth());
        container.setStyle("-fx-background-radius: " + shape
                + "; -fx-border-radius: " + shape
                + "; -fx-border-width: " + outlineWidth + ";");
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }

    /// Installs feedback handlers for pointer and keyboard interactions.
    private void installInteractionHandlers(M3Card card) {
        card.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        card.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        card.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        card.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        card.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Plays card feedback for primary pointer presses on the card surface.
    private void handleMousePressed(MouseEvent event) {
        M3Card card = getSkinnable();
        if (card.isDisabled()
                || card.getOnAction() == null
                || event.getButton() != MouseButton.PRIMARY
                || !isCardActivationTarget(event.getTarget())) {
            return;
        }

        pointerPressed = true;
        card.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, true);
        M3FocusRequests.requestFocusIfTraversable(card);
        Point2D point = stateLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        stateLayer.playRipple(point.getX(), point.getY());
        event.consume();
    }

    /// Fades card feedback after primary pointer release.
    private void handleMouseReleased(MouseEvent event) {
        if (!pointerPressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        M3Card card = getSkinnable();
        Point2D point = card.sceneToLocal(event.getSceneX(), event.getSceneY());
        boolean fire = !card.isDisabled()
                && card.getOnAction() != null
                && card.contains(point.getX(), point.getY());
        pointerPressed = false;
        card.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, false);
        stateLayer.releaseRipple();
        if (fire) {
            card.fire();
        }
        event.consume();
    }

    /// Keeps the armed state synchronized with pointer containment during a drag gesture.
    private void handleMouseDragged(MouseEvent event) {
        if (!pointerPressed) {
            return;
        }

        M3Card card = getSkinnable();
        Point2D point = card.sceneToLocal(event.getSceneX(), event.getSceneY());
        card.pseudoClassStateChanged(
                ARMED_PSEUDO_CLASS,
                !card.isDisabled() && card.contains(point.getX(), point.getY())
        );
        event.consume();
    }

    /// Plays card feedback for enter and space keyboard presses.
    private void handleKeyPressed(KeyEvent event) {
        M3Card card = getSkinnable();
        KeyCode code = event.getCode();
        if (event.getTarget() != card
                || card.getOnAction() == null
                || card.isDisabled()
                || M3FocusGuards.focusOwnerInsideTextInput(card)) {
            return;
        }

        if (code == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                card.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, true);
                stateLayer.playCenteredRipple();
            }
            event.consume();
        } else if (code == KeyCode.ENTER) {
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            card.fire();
            event.consume();
        }
    }

    /// Fires a held space-key activation when the key is released.
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        M3Card card = getSkinnable();
        boolean fire = event.getTarget() == card && !card.isDisabled() && card.getOnAction() != null;
        spaceKeyPressed = false;
        card.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, false);
        stateLayer.releaseRipple();
        if (fire) {
            card.fire();
        }
        event.consume();
    }

    /// Returns whether an event target belongs to passive card content rather than a nested control action.
    private boolean isCardActivationTarget(Object eventTarget) {
        if (!(eventTarget instanceof Node target)) {
            return false;
        }

        M3Card card = getSkinnable();
        @Nullable Node current = target;
        while (current != null && current != card) {
            if (current instanceof Control nestedControl && nestedControl.isFocusTraversable()) {
                return false;
            }
            if (current.getOnMouseClicked() != null
                    || current.getOnMousePressed() != null
                    || current.getOnMouseReleased() != null) {
                return false;
            }
            current = current.getParent();
        }
        return current == card;
    }

    /// Installs or removes interaction transitions according to the card's semantic state.
    private void updateInteractionState() {
        M3Card card = getSkinnable();
        boolean needed = card.getOnAction() != null || card.isDragged();
        if (needed) {
            if (!interactionTransitionsInstalled) {
                stateLayer.installStateTransitions(card);
                effectTransition.install();
                interactionTransitionsInstalled = true;
            }
            effectTransition.animateEffectFromCss();
        } else {
            resetInteractionState();
            uninstallInteractionTransitions();
            card.applyCss();
            container.applyCss();
        }
    }

    /// Removes state and elevation transition listeners when no interactive state is exposed.
    private void uninstallInteractionTransitions() {
        if (!interactionTransitionsInstalled) {
            return;
        }
        stateLayer.uninstallStateTransitions();
        effectTransition.uninstall();
        interactionTransitionsInstalled = false;
    }

    /// Clears pointer, keyboard, pseudo-class, and ripple state.
    private void resetInteractionState() {
        pointerPressed = false;
        spaceKeyPressed = false;
        getSkinnable().pseudoClassStateChanged(ARMED_PSEUDO_CLASS, false);
        stateLayer.reset();
    }
}
