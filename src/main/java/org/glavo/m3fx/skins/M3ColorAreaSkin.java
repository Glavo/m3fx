// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorArea;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorPlane;
import org.glavo.m3fx.controls.M3HsbColor;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ColorArea].
///
/// The common HSB saturation/brightness plane uses three retained paint layers and does not rasterize while hue is
/// dragged. Other valid planes use one reusable 192-by-192 image, regenerated only when the fixed channel changes.
@NotNullByDefault
public final class M3ColorAreaSkin extends SkinBase<M3ColorArea> {
    /// The default preferred area width.
    private static final double DEFAULT_WIDTH = 280.0;

    /// The default preferred area height.
    private static final double DEFAULT_HEIGHT = 180.0;

    /// The minimum gradient width.
    private static final double MINIMUM_WIDTH = 120.0;

    /// The minimum gradient height.
    private static final double MINIMUM_HEIGHT = 96.0;

    /// The gradient corner radius.
    private static final double TRACK_RADIUS = 12.0;

    /// The fallback visible thumb diameter when CSS does not supply one.
    private static final double DEFAULT_THUMB_SIZE = 28.0;

    /// The thumb-local focus target diameter.
    private static final double STATE_LAYER_SIZE = 40.0;

    /// The retained generic-gradient raster size.
    private static final int GRADIENT_RESOLUTION = 192;

    /// The left-to-right saturation overlay used by the optimized HSB plane.
    private static final Background HORIZONTAL_LTR_BACKGROUND = new Background(new BackgroundFill(
            new LinearGradient(
                    0.0,
                    0.0,
                    1.0,
                    0.0,
                    true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.WHITE),
                    new Stop(1.0, Color.TRANSPARENT)
            ),
            CornerRadii.EMPTY,
            Insets.EMPTY
    ));

    /// The right-to-left saturation overlay used by the optimized HSB plane.
    private static final Background HORIZONTAL_RTL_BACKGROUND = new Background(new BackgroundFill(
            new LinearGradient(
                    0.0,
                    0.0,
                    1.0,
                    0.0,
                    true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.TRANSPARENT),
                    new Stop(1.0, Color.WHITE)
            ),
            CornerRadii.EMPTY,
            Insets.EMPTY
    ));

    /// The brightness overlay shared by both horizontal orientations.
    private static final Background VERTICAL_BACKGROUND = new Background(new BackgroundFill(
            new LinearGradient(
                    0.0,
                    0.0,
                    0.0,
                    1.0,
                    true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.TRANSPARENT),
                    new Stop(1.0, Color.BLACK)
            ),
            CornerRadii.EMPTY,
            Insets.EMPTY
    ));

    /// The clipped layer that contains the gradient presentation.
    private final Pane gradientPane = new Pane();

    /// The clipping rectangle for the gradient presentation.
    private final Rectangle gradientClip = new Rectangle();

    /// The fixed-hue base layer used by the optimized HSB plane.
    private final Region colorBase = new Region();

    /// The horizontal saturation overlay used by the optimized HSB plane.
    private final Region horizontalOverlay = new Region();

    /// The vertical brightness overlay used by the optimized HSB plane.
    private final Region verticalOverlay = new Region();

    /// The canvas used for non-HSB color planes.
    private final Canvas genericCanvas = new Canvas();

    /// The reusable raster used for non-HSB color planes.
    private final WritableImage genericImage =
            new WritableImage(GRADIENT_RESOLUTION, GRADIENT_RESOLUTION);

    /// The visible color thumb.
    private final M3ColorThumb thumb = new M3ColorThumb("color-area-thumb");

    /// The thumb-local keyboard-focus indicator.
    private final M3StateLayer stateLayer = new M3StateLayer(false);

    /// The plane represented by the cached gradient.
    private @Nullable M3ColorPlane gradientPlane;

    /// The fixed-channel value represented by the cached gradient.
    private double gradientFixedValue = Double.NaN;

    /// The width at which the generic gradient image was last drawn.
    private double renderedGenericWidth = Double.NaN;

    /// The height at which the generic gradient image was last drawn.
    private double renderedGenericHeight = Double.NaN;

    /// Whether the reusable generic-gradient image has changed since it was drawn.
    private boolean genericImageDirty;

    /// The latest physical gradient x-coordinate.
    private double trackX;

    /// The latest physical gradient y-coordinate.
    private double trackY;

    /// The latest gradient width.
    private double trackWidth;

    /// The latest gradient height.
    private double trackHeight;

    /// Handles primary-button presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary-button drags.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles primary-button releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard channel changes.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Updates the thumb and fixed-channel gradient state after value changes.
    private final InvalidationListener valueInvalidation = observable -> {
        updateGradient();
        updateThumbPaint();
        getSkinnable().requestLayout();
    };

    /// Invalidates the gradient after a plane change.
    private final InvalidationListener planeInvalidation = observable -> {
        invalidateGradient();
        updateGradient();
        getSkinnable().requestLayout();
    };

    /// Ends an active drag when the control becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> {
        if (getSkinnable().isDisabled()) {
            getSkinnable().setValueChanging(false);
        }
    };

    /// Creates a color-area skin.
    ///
    /// @param control the area controlled by this skin
    public M3ColorAreaSkin(M3ColorArea control) {
        super(control);
        gradientPane.getStyleClass().add("color-area-track");
        colorBase.getStyleClass().add("color-area-color-base");
        horizontalOverlay.getStyleClass().add("color-area-horizontal-overlay");
        verticalOverlay.getStyleClass().add("color-area-vertical-overlay");
        genericCanvas.getStyleClass().add("color-area-generic-gradient");
        gradientPane.setManaged(false);
        colorBase.setManaged(false);
        horizontalOverlay.setManaged(false);
        verticalOverlay.setManaged(false);
        genericCanvas.setManaged(false);
        thumb.setManaged(false);
        stateLayer.setManaged(false);
        gradientPane.setClip(gradientClip);
        gradientPane.getChildren().setAll(colorBase, horizontalOverlay, verticalOverlay, genericCanvas);
        stateLayer.installStateTransitions(control);
        getChildren().setAll(gradientPane, stateLayer, thumb);

        control.valueProperty().addListener(valueInvalidation);
        control.planeProperty().addListener(planeInvalidation);
        control.effectiveNodeOrientationProperty().addListener(planeInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        updateGradient();
        updateThumbPaint();
    }

    /// Removes listeners and retained presentation state.
    @Override
    public void dispose() {
        M3ColorArea control = getSkinnable();
        control.valueProperty().removeListener(valueInvalidation);
        control.planeProperty().removeListener(planeInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(planeInvalidation);
        control.disabledProperty().removeListener(disabledInvalidation);
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.setValueChanging(false);
        stateLayer.uninstallStateTransitions();
        gradientPane.getChildren().clear();
        getChildren().removeAll(gradientPane, stateLayer, thumb);
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
        return leftInset + MINIMUM_WIDTH + STATE_LAYER_SIZE + rightInset;
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
        return topInset + MINIMUM_HEIGHT + STATE_LAYER_SIZE + bottomInset;
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
        return leftInset + DEFAULT_WIDTH + STATE_LAYER_SIZE + rightInset;
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
        return topInset + DEFAULT_HEIGHT + STATE_LAYER_SIZE + bottomInset;
    }

    /// Lays out the gradient and thumb while retaining a focus-indicator margin.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double margin = STATE_LAYER_SIZE / 2.0;
        trackX = x + margin;
        trackY = y + margin;
        trackWidth = Math.max(0.0, width - STATE_LAYER_SIZE);
        trackHeight = Math.max(0.0, height - STATE_LAYER_SIZE);
        gradientPane.resizeRelocate(trackX, trackY, trackWidth, trackHeight);
        gradientClip.setWidth(trackWidth);
        gradientClip.setHeight(trackHeight);
        gradientClip.setArcWidth(TRACK_RADIUS * 2.0);
        gradientClip.setArcHeight(TRACK_RADIUS * 2.0);
        colorBase.resizeRelocate(0.0, 0.0, trackWidth, trackHeight);
        horizontalOverlay.resizeRelocate(0.0, 0.0, trackWidth, trackHeight);
        verticalOverlay.resizeRelocate(0.0, 0.0, trackWidth, trackHeight);
        genericCanvas.setWidth(trackWidth);
        genericCanvas.setHeight(trackHeight);
        if (genericCanvas.isVisible()) {
            drawGenericGradient();
        }
        layoutThumb();
    }

    /// Positions the current color thumb.
    private void layoutThumb() {
        M3ColorArea control = getSkinnable();
        M3ColorPlane plane = control.getPlane();
        M3Color converted = control.getValue().toColorSpace(plane.colorSpace());
        double xPosition = plane.xChannel().toPosition(converted.getChannel(plane.xChannel()));
        double yPosition = plane.yChannel().toPosition(converted.getChannel(plane.yChannel()));
        double physicalXPosition = M3NodeLayout.isRightToLeft(control)
                ? 1.0 - xPosition
                : xPosition;
        double centerX = trackX + trackWidth * physicalXPosition;
        double centerY = trackY + trackHeight * (1.0 - yPosition);
        double thumbSize = resolvedThumbSize();
        thumb.resizeRelocate(
                centerX - thumbSize / 2.0,
                centerY - thumbSize / 2.0,
                thumbSize,
                thumbSize
        );
        stateLayer.layoutLayer(
                centerX - STATE_LAYER_SIZE / 2.0,
                centerY - STATE_LAYER_SIZE / 2.0,
                STATE_LAYER_SIZE,
                STATE_LAYER_SIZE,
                STATE_LAYER_SIZE / 2.0
        );
    }

    /// Returns the CSS-resolved thumb diameter or its fallback value.
    private double resolvedThumbSize() {
        double width = thumb.prefWidth(-1.0);
        double height = thumb.prefHeight(-1.0);
        double size = Math.max(width, height);
        return Double.isFinite(size) && size > 0.0 ? size : DEFAULT_THUMB_SIZE;
    }

    /// Starts direct two-axis manipulation.
    private void handleMousePressed(MouseEvent event) {
        M3ColorArea control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        M3FocusRequests.requestFocusIfTraversable(control);
        control.setValueChanging(true);
        updateValueFromMouse(event);
        event.consume();
    }

    /// Continues direct two-axis manipulation.
    private void handleMouseDragged(MouseEvent event) {
        if (getSkinnable().isDisabled() || !event.isPrimaryButtonDown()) {
            return;
        }
        updateValueFromMouse(event);
        event.consume();
    }

    /// Completes direct two-axis manipulation.
    private void handleMouseReleased(MouseEvent event) {
        M3ColorArea control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        updateValueFromMouse(event);
        control.setValueChanging(false);
        event.consume();
    }

    /// Handles keyboard channel changes.
    private void handleKeyPressed(KeyEvent event) {
        M3ColorArea control = getSkinnable();
        if (control.isDisabled()) {
            return;
        }
        M3ColorPlane plane = control.getPlane();
        boolean block = event.isShiftDown();
        M3ColorChannel xChannel = plane.xChannel();
        M3ColorChannel yChannel = plane.yChannel();
        double xIncrement = block ? xChannel.getBlockIncrement() : xChannel.getUnitIncrement();
        double yIncrement = block ? yChannel.getBlockIncrement() : yChannel.getUnitIncrement();
        M3Color converted = control.getValue().toColorSpace(plane.colorSpace());
        switch (event.getCode()) {
            case LEFT -> setOneChannel(
                    xChannel,
                    converted.getChannel(xChannel) + (M3NodeLayout.isRightToLeft(control)
                            ? xIncrement
                            : -xIncrement)
            );
            case RIGHT -> setOneChannel(
                    xChannel,
                    converted.getChannel(xChannel) + (M3NodeLayout.isRightToLeft(control)
                            ? -xIncrement
                            : xIncrement)
            );
            case DOWN -> setOneChannel(yChannel, converted.getChannel(yChannel) - yIncrement);
            case UP -> setOneChannel(yChannel, converted.getChannel(yChannel) + yIncrement);
            case PAGE_DOWN -> setOneChannel(yChannel, converted.getChannel(yChannel) - yChannel.getBlockIncrement());
            case PAGE_UP -> setOneChannel(yChannel, converted.getChannel(yChannel) + yChannel.getBlockIncrement());
            case HOME -> setOneChannel(xChannel, xChannel.getMinimum());
            case END -> setOneChannel(xChannel, xChannel.getMaximum());
            default -> {
                return;
            }
        }
        event.consume();
    }

    /// Updates both configured channels from a local pointer coordinate.
    private void updateValueFromMouse(MouseEvent event) {
        M3ColorArea control = getSkinnable();
        M3ColorPlane plane = control.getPlane();
        double physicalXPosition = trackWidth == 0.0 ? 0.0 : (event.getX() - trackX) / trackWidth;
        double xPosition = M3NodeLayout.isRightToLeft(control)
                ? 1.0 - physicalXPosition
                : physicalXPosition;
        double yPosition = trackHeight == 0.0 ? 0.0 : 1.0 - (event.getY() - trackY) / trackHeight;
        M3Color converted = control.getValue().toColorSpace(plane.colorSpace());
        M3Color updated = converted
                .withChannel(plane.xChannel(), plane.xChannel().fromPosition(clamp(xPosition)))
                .withChannel(plane.yChannel(), plane.yChannel().fromPosition(clamp(yPosition)));
        control.setValue(updated);
    }

    /// Updates one configured channel while preserving all others.
    private void setOneChannel(M3ColorChannel channel, double value) {
        M3ColorArea control = getSkinnable();
        M3ColorPlane plane = control.getPlane();
        M3Color converted = control.getValue().toColorSpace(plane.colorSpace());
        control.setValue(converted.withChannel(channel, channel.constrain(value)));
    }

    /// Updates the optimized or generic gradient for the current fixed-channel state.
    private void updateGradient() {
        M3ColorArea control = getSkinnable();
        M3ColorPlane plane = control.getPlane();
        M3Color converted = control.getValue().toColorSpace(plane.colorSpace());
        double fixedValue = converted.getChannel(plane.fixedChannel());
        if (plane.equals(gradientPlane) && Double.compare(fixedValue, gradientFixedValue) == 0) {
            return;
        }
        gradientPlane = plane;
        gradientFixedValue = fixedValue;
        M3Color opaque = converted.getAlpha() == 1.0
                ? converted
                : converted.withChannel(M3ColorChannel.ALPHA, 1.0);

        if (M3ColorPlane.HSB_SATURATION_BRIGHTNESS.equals(plane)) {
            configureOptimizedHsbGradient((M3HsbColor) opaque);
        } else {
            colorBase.setVisible(false);
            horizontalOverlay.setVisible(false);
            verticalOverlay.setVisible(false);
            genericCanvas.setVisible(true);
            rasterizeGenericGradient(opaque, plane);
            drawGenericGradient();
        }
    }

    /// Configures the allocation-free layered HSB saturation/brightness gradient.
    private void configureOptimizedHsbGradient(M3HsbColor color) {
        colorBase.setVisible(true);
        horizontalOverlay.setVisible(true);
        verticalOverlay.setVisible(true);
        genericCanvas.setVisible(false);
        colorBase.setBackground(new Background(new BackgroundFill(
                Color.hsb(color.hue(), 1.0, 1.0),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        boolean rightToLeft = M3NodeLayout.isRightToLeft(getSkinnable());
        horizontalOverlay.setBackground(rightToLeft ? HORIZONTAL_RTL_BACKGROUND : HORIZONTAL_LTR_BACKGROUND);
        verticalOverlay.setBackground(VERTICAL_BACKGROUND);
    }

    /// Rasterizes a non-HSB plane into the reusable image without allocating per-pixel color objects.
    private void rasterizeGenericGradient(M3Color converted, M3ColorPlane plane) {
        PixelWriter writer = genericImage.getPixelWriter();
        M3ColorChannel xChannel = plane.xChannel();
        M3ColorChannel yChannel = plane.yChannel();
        M3ColorChannel firstChannel = plane.colorSpace().getChannels().get(0);
        M3ColorChannel secondChannel = plane.colorSpace().getChannels().get(1);
        M3ColorChannel thirdChannel = plane.colorSpace().getChannels().get(2);
        double fixedValue = converted.getChannel(plane.fixedChannel());
        double xMinimum = xChannel.getMinimum();
        double xRange = xChannel.getMaximum() - xMinimum;
        double yMinimum = yChannel.getMinimum();
        double yRange = yChannel.getMaximum() - yMinimum;
        boolean rightToLeft = M3NodeLayout.isRightToLeft(getSkinnable());
        int last = GRADIENT_RESOLUTION - 1;
        for (int pixelY = 0; pixelY < GRADIENT_RESOLUTION; pixelY++) {
            double yPosition = 1.0 - (double) pixelY / last;
            double yValue = yMinimum + yRange * yPosition;
            for (int pixelX = 0; pixelX < GRADIENT_RESOLUTION; pixelX++) {
                double physicalXPosition = (double) pixelX / last;
                double xPosition = rightToLeft
                        ? 1.0 - physicalXPosition
                        : physicalXPosition;
                double xValue = xMinimum + xRange * xPosition;
                writer.setArgb(
                        pixelX,
                        pixelY,
                        M3ColorMath.toArgb(
                                plane.colorSpace(),
                                channelSample(firstChannel, xChannel, xValue, yChannel, yValue, fixedValue),
                                channelSample(secondChannel, xChannel, xValue, yChannel, yValue, fixedValue),
                                channelSample(thirdChannel, xChannel, xValue, yChannel, yValue, fixedValue),
                                1.0
                        )
                );
            }
        }
        genericImageDirty = true;
    }

    /// Returns one ordered color-space channel sample for the current raster position.
    private static double channelSample(
            M3ColorChannel channel,
            M3ColorChannel xChannel,
            double xValue,
            M3ColorChannel yChannel,
            double yValue,
            double fixedValue
    ) {
        if (channel == xChannel) {
            return xValue;
        }
        if (channel == yChannel) {
            return yValue;
        }
        return fixedValue;
    }

    /// Draws the reusable generic gradient image at the current logical size.
    private void drawGenericGradient() {
        double width = genericCanvas.getWidth();
        double height = genericCanvas.getHeight();
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        if (!genericImageDirty
                && Double.compare(width, renderedGenericWidth) == 0
                && Double.compare(height, renderedGenericHeight) == 0) {
            return;
        }
        GraphicsContext graphics = genericCanvas.getGraphicsContext2D();
        graphics.setImageSmoothing(true);
        graphics.clearRect(0.0, 0.0, width, height);
        graphics.drawImage(genericImage, 0.0, 0.0, width, height);
        renderedGenericWidth = width;
        renderedGenericHeight = height;
        genericImageDirty = false;
    }

    /// Applies an opaque version of the current color to the visible thumb.
    private void updateThumbPaint() {
        thumb.setColor(getSkinnable().getValue());
    }

    /// Clears the fixed-channel gradient cache.
    private void invalidateGradient() {
        gradientPlane = null;
        gradientFixedValue = Double.NaN;
    }

    /// Restricts a normalized value to `0.0..1.0`.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
