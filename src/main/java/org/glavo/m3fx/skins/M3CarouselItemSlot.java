// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3Card;
import org.jetbrains.annotations.NotNullByDefault;

/// Internal mask container that preserves authored carousel content geometry while exposing a dynamic keyline width.
///
/// Material carousel items are measured at their focal width and revealed through a smaller rounded mask as they
/// move through medium and small keylines. The container owns only the mask; it does not replace an application
/// clip installed on the content node.
@NotNullByDefault
final class M3CarouselItemSlot extends Region {
    /// The internal style class used for rendered carousel item masks.
    static final String STYLE_CLASS = "m3-carousel-item-container";

    /// The style class applied to the item-level Material state layer.
    static final String STATE_LAYER_STYLE_CLASS = "m3-carousel-item-state-layer";

    /// The pseudo-class applied while this slot occupies a Material small-item keyline.
    private static final PseudoClass SMALL_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("small-item");

    /// The pseudo-class applied while pointer or keyboard activation is held.
    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    /// The application-owned item rendered inside this slot.
    private final Node content;

    /// Hosts focal-width content behind the keyline mask without replacing an application-owned clip.
    private final Group maskedContent = new Group();

    /// The reusable rounded rectangle that clips item content to the current keyline width.
    private final Rectangle mask = new Rectangle();

    /// Renders hover, focus, pressed, and ripple feedback at the visible keyline bounds.
    private final M3StateLayer stateLayer;

    /// Starts pointer ripple feedback for primary presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Releases pointer ripple feedback.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Starts centered keyboard ripple feedback for activation keys.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Releases centered keyboard ripple feedback.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// The width assigned to content before the keyline mask is applied.
    private double contentWidth;

    /// The current Material item corner radius.
    private double shapeRadius;

    /// Whether this slot currently occupies a Material small-item keyline.
    private boolean smallItem;

    /// Whether an activation key currently owns the centered ripple.
    private boolean keyboardRippleActive;

    /// Creates a mask slot for one application-owned item.
    ///
    /// @param content the carousel item content
    M3CarouselItemSlot(Node content) {
        this.content = content;
        stateLayer = new M3StateLayer(!(content instanceof M3Card));
        getStyleClass().add(STYLE_CLASS);
        setPickOnBounds(false);
        maskedContent.setAutoSizeChildren(false);
        maskedContent.setManaged(false);
        maskedContent.setClip(mask);
        maskedContent.getChildren().add(content);
        stateLayer.getStyleClass().add(STATE_LAYER_STYLE_CLASS);
        stateLayer.installStateTransitions(content);
        disableProperty().bind(content.disableProperty());
        content.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        content.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        content.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        getChildren().addAll(maskedContent, stateLayer);
    }

    /// Returns whether this item participates in keyline layout.
    ///
    /// @return `true` when the content is visible and managed
    boolean participatesInLayout() {
        return content.isVisible() && content.isManaged();
    }

    /// Updates the focal content width and mask shape without allocating new scene-graph nodes.
    ///
    /// @param contentWidth the width at which content is laid out before masking
    /// @param shapeRadius the Material mask corner radius
    /// @param smallItem whether the slot currently occupies a small-item keyline
    void configure(double contentWidth, double shapeRadius, boolean smallItem) {
        double normalizedContentWidth = Math.max(0.0, contentWidth);
        double normalizedShapeRadius = Math.max(0.0, shapeRadius);
        if (Double.compare(this.contentWidth, normalizedContentWidth) != 0
                || Double.compare(this.shapeRadius, normalizedShapeRadius) != 0) {
            this.contentWidth = normalizedContentWidth;
            this.shapeRadius = normalizedShapeRadius;
            requestLayout();
        }
        if (this.smallItem != smallItem) {
            this.smallItem = smallItem;
            pseudoClassStateChanged(SMALL_ITEM_PSEUDO_CLASS, smallItem);
        }
    }

    /// Detaches the application-owned content before this slot is discarded.
    void dispose() {
        content.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        content.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        content.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        disableProperty().unbind();
        pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, false);
        keyboardRippleActive = false;
        stateLayer.uninstallStateTransitions();
        stateLayer.reset();
        maskedContent.setClip(null);
        maskedContent.getChildren().clear();
        getChildren().clear();
    }

    /// Computes minimum width from the authored item.
    @Override
    protected double computeMinWidth(double height) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getWidth();
        }
        return boundedSize(region.minWidth(height));
    }

    /// Computes preferred width from the authored item.
    @Override
    protected double computePrefWidth(double height) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getWidth();
        }
        return boundedSize(region.prefWidth(height), region.minWidth(height), region.maxWidth(height));
    }

    /// Computes minimum height from the authored item.
    @Override
    protected double computeMinHeight(double width) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getHeight();
        }
        return boundedSize(region.minHeight(width));
    }

    /// Computes preferred height from the authored item at its unmasked focal width.
    @Override
    protected double computePrefHeight(double width) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getHeight();
        }
        double effectiveWidth = contentWidth > 0.0 ? contentWidth : width;
        return boundedSize(
                region.prefHeight(effectiveWidth),
                region.minHeight(effectiveWidth),
                region.maxHeight(effectiveWidth)
        );
    }

    /// Lays out focal-width content behind the current rounded keyline mask.
    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        double radius = Math.min(shapeRadius, Math.min(width, height) / 2.0);
        mask.setWidth(width);
        mask.setHeight(height);
        mask.setArcWidth(radius * 2.0);
        mask.setArcHeight(radius * 2.0);
        maskedContent.setLayoutX(0.0);
        maskedContent.setLayoutY(0.0);

        double resolvedContentWidth = Math.max(width, contentWidth);
        if (content.isResizable()) {
            content.resize(resolvedContentWidth, height);
            content.relocate((width - resolvedContentWidth) / 2.0, 0.0);
        } else {
            double childWidth = content.getLayoutBounds().getWidth();
            double childHeight = content.getLayoutBounds().getHeight();
            content.relocate((width - childWidth) / 2.0, (height - childHeight) / 2.0);
        }
        stateLayer.layoutLayer(0.0, 0.0, width, height, radius);
    }

    /// Starts a ripple at the pointer position inside the visible mask.
    private void handleMousePressed(MouseEvent event) {
        if (isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        Point2D localPoint = stateLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, true);
        stateLayer.playRipple(localPoint.getX(), localPoint.getY());
    }

    /// Releases pointer ripple feedback after the primary button is released.
    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, keyboardRippleActive);
        stateLayer.releaseRipple();
    }

    /// Starts one centered ripple while Enter or Space is held.
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (isDisabled() || keyboardRippleActive || code != KeyCode.ENTER && code != KeyCode.SPACE) {
            return;
        }
        keyboardRippleActive = true;
        pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, true);
        stateLayer.playCenteredRipple();
    }

    /// Releases centered ripple feedback after an activation key is released.
    private void handleKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();
        if (!keyboardRippleActive || code != KeyCode.ENTER && code != KeyCode.SPACE) {
            return;
        }
        keyboardRippleActive = false;
        pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, false);
        stateLayer.releaseRipple();
    }

    /// Returns a finite non-negative size.
    ///
    /// @param value the candidate size
    /// @return the normalized size
    private static double boundedSize(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    /// Clamps a preferred size between finite minimum and maximum constraints.
    ///
    /// @param preferred the preferred size
    /// @param minimum the minimum size
    /// @param maximum the maximum size
    /// @return the bounded size
    private static double boundedSize(double preferred, double minimum, double maximum) {
        double finitePreferred = boundedSize(preferred);
        double finiteMinimum = boundedSize(minimum);
        double finiteMaximum = Double.isFinite(maximum) ? Math.max(0.0, maximum) : Double.MAX_VALUE;
        return Math.max(finiteMinimum, Math.min(finitePreferred, finiteMaximum));
    }
}
