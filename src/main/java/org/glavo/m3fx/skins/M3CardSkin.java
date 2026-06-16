// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Card].
@NotNullByDefault
public class M3CardSkin extends SkinBase<M3Card> {
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

    /// Handles keyboard activation feedback.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Updates hosted card content after content changes.
    private final ChangeListener<Node> contentListener =
            (observable, oldValue, newValue) -> updateContent(newValue);

    /// Applies token changes to the card surface.
    private final InvalidationListener tokenInvalidation = observable -> updateTokenStyles();

    /// Creates a card skin.
    ///
    /// @param control the card controlled by this skin
    public M3CardSkin(M3Card control) {
        super(control);
        effectTransition = new M3CssEffectTransition(control, container);
        container.getStyleClass().add("m3-card-container");
        getChildren().add(container);
        updateContent(control.getContent());
        updateTokenStyles();
        stateLayer.installStateTransitions(control);
        effectTransition.install();
        installInteractionHandlers(control);
        control.contentProperty().addListener(contentListener);
        control.containerShapeProperty().addListener(tokenInvalidation);
        control.contentPaddingProperty().addListener(tokenInvalidation);
        control.outlineWidthProperty().addListener(tokenInvalidation);
    }

    /// Removes interaction handlers before the skin is disposed.
    @Override
    public void dispose() {
        M3Card card = getSkinnable();
        stateLayer.uninstallStateTransitions();
        effectTransition.uninstall();
        stateLayer.reset();
        card.contentProperty().removeListener(contentListener);
        card.containerShapeProperty().removeListener(tokenInvalidation);
        card.contentPaddingProperty().removeListener(tokenInvalidation);
        card.outlineWidthProperty().removeListener(tokenInvalidation);
        card.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        card.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        card.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
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
            container.getChildren().setAll(stateLayer, content);
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
        card.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
    }

    /// Plays card feedback for primary pointer presses on the card surface.
    private void handleMousePressed(MouseEvent event) {
        if (getSkinnable().isDisabled() || event.getButton() != MouseButton.PRIMARY || !isCardSurfaceEvent(event)) {
            return;
        }
        stateLayer.playRipple(event.getX(), event.getY());
    }

    /// Fades card feedback after primary pointer release.
    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            stateLayer.releaseRipple();
            if (!getSkinnable().isDisabled() && isCardSurfaceEvent(event)) {
                getSkinnable().fire();
                event.consume();
            }
        }
    }

    /// Plays card feedback for enter and space keyboard presses.
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if ((code == KeyCode.ENTER || code == KeyCode.SPACE)
                && !getSkinnable().isDisabled()
                && !M3FocusGuards.focusOwnerInsideTextInput(getSkinnable())) {
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            getSkinnable().fire();
            event.consume();
        }
    }

    /// Returns whether the event originated from the card surface instead of hosted content.
    private boolean isCardSurfaceEvent(MouseEvent event) {
        Object target = event.getTarget();
        return target == getSkinnable() || target == container;
    }
}
