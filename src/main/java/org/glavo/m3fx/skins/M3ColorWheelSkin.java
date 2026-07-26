// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorSpace;
import org.glavo.m3fx.controls.M3ColorWheel;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

/// The default skin for [M3ColorWheel].
///
/// The wheel is rendered in an explicitly left-to-right visual plane so its physical hue arrangement does not
/// mirror with the control's effective node orientation. The retained canvas is redrawn only when its geometry or
/// track thickness changes; color-value changes reposition and repaint only the thumb.
@NotNullByDefault
public final class M3ColorWheelSkin extends SkinBase<M3ColorWheel> {
    /// The minimum logical side length.
    private static final double MINIMUM_SIZE = 96.0;

    /// The default preferred logical side length.
    private static final double DEFAULT_SIZE = 192.0;

    /// The fallback visible thumb diameter when CSS does not supply one.
    private static final double DEFAULT_THUMB_SIZE = 28.0;

    /// The diameter of the thumb-local interaction and keyboard-focus target.
    private static final double STATE_LAYER_SIZE = 40.0;

    /// The number of retained one-degree hue paints used to draw the ring.
    private static final int HUE_SEGMENT_COUNT = 360;

    /// The angular overdraw used to prevent antialiasing seams between adjacent hue segments.
    private static final double SEGMENT_OVERDRAW = 0.25;

    /// The retained hue paints sampled at the center of each one-degree segment.
    private static final Color @Unmodifiable [] HUE_PAINTS = createHuePaints();

    /// The physical, non-mirroring visual plane.
    private final Pane visual = new Pane();

    /// The retained raster surface used to draw the hue ring.
    private final Canvas ringCanvas = new Canvas();

    /// The visible color thumb.
    private final M3ColorThumb thumb = new M3ColorThumb("color-wheel-thumb");

    /// The thumb-local interaction and keyboard-focus layer.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The latest physical wheel center x-coordinate.
    private double centerX;

    /// The latest physical wheel center y-coordinate.
    private double centerY;

    /// The latest radius followed by the thumb center.
    private double thumbPathRadius;

    /// The canvas width represented by the retained ring raster.
    private double renderedWidth = Double.NaN;

    /// The canvas height represented by the retained ring raster.
    private double renderedHeight = Double.NaN;

    /// The visible track thickness represented by the retained ring raster.
    private double renderedTrackThickness = Double.NaN;

    /// Handles primary-button presses in the physical visual plane.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary-button drags in the physical visual plane.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles primary-button releases in the physical visual plane.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard hue changes on the control.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Repositions and repaints the thumb after the color value changes.
    private final InvalidationListener valueInvalidation =
            observable -> getSkinnable().requestLayout();

    /// Invalidates retained ring geometry after track-thickness changes.
    private final InvalidationListener trackThicknessInvalidation = observable -> {
        invalidateRing();
        getSkinnable().requestLayout();
    };

    /// Clears direct-manipulation state when the control becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> {
        M3ColorWheel control = getSkinnable();
        if (control.isDisabled()) {
            control.setValueChanging(false);
            stateLayer.cancelRipple();
        }
    };

    /// Creates a color-wheel skin.
    ///
    /// @param control the wheel controlled by this skin
    public M3ColorWheelSkin(M3ColorWheel control) {
        super(control);

        visual.getStyleClass().add("color-wheel-visual");
        ringCanvas.getStyleClass().add("color-wheel-track");
        visual.setManaged(false);
        visual.setPickOnBounds(true);
        visual.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        ringCanvas.setManaged(false);
        ringCanvas.setMouseTransparent(true);
        thumb.setManaged(false);
        thumb.setMouseTransparent(true);
        stateLayer.setManaged(false);
        stateLayer.setMouseTransparent(true);
        stateLayer.installStateTransitions(control);
        visual.getChildren().setAll(ringCanvas, stateLayer, thumb);
        getChildren().setAll(visual);

        control.valueProperty().addListener(valueInvalidation);
        control.trackThicknessProperty().addListener(trackThicknessInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);
        visual.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        visual.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        visual.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
    }

    /// Removes listeners and retained interaction state.
    @Override
    public void dispose() {
        M3ColorWheel control = getSkinnable();
        control.valueProperty().removeListener(valueInvalidation);
        control.trackThicknessProperty().removeListener(trackThicknessInvalidation);
        control.disabledProperty().removeListener(disabledInvalidation);
        visual.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        visual.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        visual.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.setValueChanging(false);
        stateLayer.uninstallStateTransitions();
        stateLayer.cancelRipple();
        visual.getChildren().clear();
        getChildren().remove(visual);
        super.dispose();
    }

    /// Computes the minimum width.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + MINIMUM_SIZE + rightInset;
    }

    /// Computes the minimum height.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + MINIMUM_SIZE + bottomInset;
    }

    /// Computes the preferred width.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + DEFAULT_SIZE + rightInset;
    }

    /// Computes the preferred height.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + DEFAULT_SIZE + bottomInset;
    }

    /// Lays out the retained hue ring, thumb, and thumb-local state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        visual.resizeRelocate(x, y, width, height);
        centerX = width / 2.0;
        centerY = height / 2.0;
        thumbPathRadius = Math.max(0.0, Math.min(width, height) / 2.0 - STATE_LAYER_SIZE / 2.0);
        double trackThickness = Math.min(
                getSkinnable().getTrackThickness(),
                thumbPathRadius * 2.0
        );
        updateRing(width, height, trackThickness);
        layoutThumb();
    }

    /// Positions the thumb and its state layer for the current hue.
    private void layoutThumb() {
        double hue = editingValue().getChannel(M3ColorChannel.HUE);
        double angle = Math.toRadians(hue - 90.0);
        double thumbCenterX = centerX + thumbPathRadius * Math.cos(angle);
        double thumbCenterY = centerY + thumbPathRadius * Math.sin(angle);
        double thumbSize = resolvedThumbSize();

        thumb.resizeRelocate(
                thumbCenterX - thumbSize / 2.0,
                thumbCenterY - thumbSize / 2.0,
                thumbSize,
                thumbSize
        );
        stateLayer.layoutLayer(
                thumbCenterX - STATE_LAYER_SIZE / 2.0,
                thumbCenterY - STATE_LAYER_SIZE / 2.0,
                STATE_LAYER_SIZE,
                STATE_LAYER_SIZE,
                STATE_LAYER_SIZE / 2.0
        );
        updateThumbPaint();
    }

    /// Starts direct hue manipulation and its bounded thumb ripple.
    private void handleMousePressed(MouseEvent event) {
        M3ColorWheel control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        M3FocusRequests.requestFocusIfTraversable(control);
        control.setValueChanging(true);
        updateValueFromPointer(event.getX(), event.getY());
        layoutThumb();
        stateLayer.playCenteredRipple();
        event.consume();
    }

    /// Continues direct hue manipulation.
    private void handleMouseDragged(MouseEvent event) {
        M3ColorWheel control = getSkinnable();
        if (control.isDisabled() || !control.isValueChanging() || !event.isPrimaryButtonDown()) {
            return;
        }

        updateValueFromPointer(event.getX(), event.getY());
        event.consume();
    }

    /// Completes direct hue manipulation and releases its bounded thumb ripple.
    private void handleMouseReleased(MouseEvent event) {
        M3ColorWheel control = getSkinnable();
        if (event.getButton() != MouseButton.PRIMARY || !control.isValueChanging()) {
            return;
        }

        if (!control.isDisabled()) {
            updateValueFromPointer(event.getX(), event.getY());
        }
        control.setValueChanging(false);
        stateLayer.releaseRipple();
        event.consume();
    }

    /// Handles keyboard hue changes without mirroring physical direction in right-to-left layouts.
    private void handleKeyPressed(KeyEvent event) {
        M3ColorWheel control = getSkinnable();
        if (control.isDisabled()) {
            return;
        }

        KeyCode code = event.getCode();
        double currentHue = editingValue().getChannel(M3ColorChannel.HUE);
        double arrowIncrement = event.isShiftDown() ? 10.0 : 1.0;
        double targetHue;
        switch (code) {
            case LEFT, DOWN -> targetHue = M3ColorMath.wrapHue(currentHue - arrowIncrement);
            case RIGHT, UP -> targetHue = M3ColorMath.wrapHue(currentHue + arrowIncrement);
            case PAGE_DOWN -> targetHue = M3ColorMath.wrapHue(currentHue - 10.0);
            case PAGE_UP -> targetHue = M3ColorMath.wrapHue(currentHue + 10.0);
            case HOME -> targetHue = 0.0;
            case END -> targetHue = 360.0;
            default -> {
                return;
            }
        }

        setHue(targetHue);
        event.consume();
    }

    /// Updates hue from a point in the physical, left-to-right visual plane.
    private void updateValueFromPointer(double x, double y) {
        double deltaX = x - centerX;
        double deltaY = y - centerY;
        if (deltaX == 0.0 && deltaY == 0.0) {
            return;
        }

        double hue = Math.toDegrees(Math.atan2(deltaY, deltaX)) + 90.0;
        setHue(M3ColorMath.wrapHue(hue));
    }

    /// Stores hue in HSL when the current value is HSL and in HSB otherwise.
    private void setHue(double hue) {
        M3ColorWheel control = getSkinnable();
        M3Color value = control.getValue();
        M3ColorSpace editingSpace = value.getColorSpace() == M3ColorSpace.HSL
                ? M3ColorSpace.HSL
                : M3ColorSpace.HSB;
        control.setValue(M3ColorMath.withChannel(
                value,
                editingSpace,
                M3ColorChannel.HUE,
                hue
        ));
    }

    /// Returns the current value converted to the wheel's editing color space.
    private M3Color editingValue() {
        M3Color value = getSkinnable().getValue();
        return value.toColorSpace(value.getColorSpace() == M3ColorSpace.HSL
                ? M3ColorSpace.HSL
                : M3ColorSpace.HSB);
    }

    /// Redraws the retained hue ring only when represented geometry changes.
    private void updateRing(double width, double height, double trackThickness) {
        if (Double.compare(width, renderedWidth) == 0
                && Double.compare(height, renderedHeight) == 0
                && Double.compare(trackThickness, renderedTrackThickness) == 0) {
            return;
        }

        renderedWidth = width;
        renderedHeight = height;
        renderedTrackThickness = trackThickness;
        if (Double.compare(ringCanvas.getWidth(), width) != 0) {
            ringCanvas.setWidth(width);
        }
        if (Double.compare(ringCanvas.getHeight(), height) != 0) {
            ringCanvas.setHeight(height);
        }

        GraphicsContext graphics = ringCanvas.getGraphicsContext2D();
        graphics.clearRect(0.0, 0.0, width, height);
        if (width <= 0.0 || height <= 0.0 || trackThickness <= 0.0 || thumbPathRadius <= 0.0) {
            return;
        }

        double diameter = thumbPathRadius * 2.0;
        double ringX = centerX - thumbPathRadius;
        double ringY = centerY - thumbPathRadius;
        graphics.setLineWidth(trackThickness);
        graphics.setLineCap(StrokeLineCap.BUTT);
        double extent = -(1.0 + SEGMENT_OVERDRAW);
        double halfOverdraw = SEGMENT_OVERDRAW / 2.0;
        for (int index = 0; index < HUE_SEGMENT_COUNT; index++) {
            graphics.setStroke(HUE_PAINTS[index]);
            graphics.strokeArc(
                    ringX,
                    ringY,
                    diameter,
                    diameter,
                    90.0 - index + halfOverdraw,
                    extent,
                    ArcType.OPEN
            );
        }
    }

    /// Applies an opaque representation of the current color to the thumb.
    private void updateThumbPaint() {
        thumb.setColor(getSkinnable().getValue());
    }

    /// Returns the CSS-resolved thumb diameter or its fallback value.
    private double resolvedThumbSize() {
        double width = thumb.prefWidth(-1.0);
        double height = thumb.prefHeight(-1.0);
        double size = Math.max(width, height);
        return Double.isFinite(size) && size > 0.0 ? size : DEFAULT_THUMB_SIZE;
    }

    /// Marks the retained ring raster as stale.
    private void invalidateRing() {
        renderedWidth = Double.NaN;
        renderedHeight = Double.NaN;
        renderedTrackThickness = Double.NaN;
    }

    /// Creates the immutable set of one-degree hue paints.
    private static Color @Unmodifiable [] createHuePaints() {
        Color[] paints = new Color[HUE_SEGMENT_COUNT];
        for (int index = 0; index < HUE_SEGMENT_COUNT; index++) {
            paints[index] = Color.hsb(index + 0.5, 1.0, 1.0);
        }
        return paints;
    }
}
