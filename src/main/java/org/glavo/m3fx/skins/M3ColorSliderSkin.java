// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorSlider;
import org.glavo.m3fx.controls.M3ColorSpace;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// The default skin for [M3ColorSlider].
///
/// The skin retains one checkerboard, gradient track, thumb, and focus layer. Gradient stops are rebuilt only when
/// the edited channel, orientation, or a non-edited channel changes; dragging the thumb does not allocate a new
/// gradient on every pointer event.
@NotNullByDefault
public final class M3ColorSliderSkin extends SkinBase<M3ColorSlider> {
    /// The default horizontal preferred length.
    private static final double DEFAULT_LENGTH = 240.0;

    /// The default cross-axis touch-target size.
    private static final double DEFAULT_CROSS_SIZE = 48.0;

    /// The fallback visible gradient-track thickness when CSS does not supply one.
    private static final double DEFAULT_TRACK_THICKNESS = 16.0;

    /// The fallback visible thumb diameter when CSS does not supply one.
    private static final double DEFAULT_THUMB_SIZE = 28.0;

    /// The keyboard-focus target diameter.
    private static final double STATE_LAYER_SIZE = 40.0;

    /// The number of intervals used to approximate non-linear channel gradients.
    private static final int GRADIENT_INTERVALS = 12;

    /// The retained pill radius used by track and thumb backgrounds.
    private static final CornerRadii PILL_RADII = new CornerRadii(999.0);

    /// The transparency checkerboard beneath the gradient.
    private final M3ColorCheckerboard checkerboard = new M3ColorCheckerboard();

    /// The visible channel-gradient track.
    private final Region track = new Region();

    /// The draggable color thumb.
    private final M3ColorThumb thumb = new M3ColorThumb("color-slider-thumb");

    /// The thumb-local keyboard-focus indicator.
    private final M3StateLayer stateLayer = new M3StateLayer(false);

    /// The color space represented by the cached gradient.
    private @Nullable M3ColorSpace gradientColorSpace;

    /// The channel represented by the cached gradient.
    private @Nullable M3ColorChannel gradientChannel;

    /// The orientation represented by the cached gradient.
    private @Nullable Orientation gradientOrientation;

    /// The first ordered color-space channel represented by the cached gradient.
    private double gradientFirst = Double.NaN;

    /// The second ordered color-space channel represented by the cached gradient.
    private double gradientSecond = Double.NaN;

    /// The third ordered color-space channel represented by the cached gradient.
    private double gradientThird = Double.NaN;

    /// Handles primary-button presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary-button drags.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles primary-button releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard value changes.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Updates the thumb and possibly the gradient after value changes.
    private final InvalidationListener valueInvalidation = observable -> {
        updateGradient();
        updateThumbPaint();
        getSkinnable().requestLayout();
    };

    /// Invalidates the cached gradient after channel or orientation changes.
    private final InvalidationListener configurationInvalidation = observable -> {
        invalidateGradient();
        getSkinnable().requestLayout();
    };

    /// Clears transient interaction state when the control becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> {
        if (getSkinnable().isDisabled()) {
            getSkinnable().setValueChanging(false);
        }
    };

    /// Creates a color-slider skin.
    ///
    /// @param control the slider controlled by this skin
    public M3ColorSliderSkin(M3ColorSlider control) {
        super(control);
        checkerboard.getStyleClass().add("color-slider-track");
        track.getStyleClass().add("color-slider-track");
        checkerboard.setManaged(false);
        track.setManaged(false);
        thumb.setManaged(false);
        stateLayer.setManaged(false);
        stateLayer.installStateTransitions(control);
        getChildren().setAll(checkerboard, track, stateLayer, thumb);

        control.valueProperty().addListener(valueInvalidation);
        control.channelProperty().addListener(configurationInvalidation);
        control.orientationProperty().addListener(configurationInvalidation);
        control.effectiveNodeOrientationProperty().addListener(configurationInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        updateGradient();
        updateThumbPaint();
    }

    /// Removes listeners and transient visual state.
    @Override
    public void dispose() {
        M3ColorSlider control = getSkinnable();
        control.valueProperty().removeListener(valueInvalidation);
        control.channelProperty().removeListener(configurationInvalidation);
        control.orientationProperty().removeListener(configurationInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(configurationInvalidation);
        control.disabledProperty().removeListener(disabledInvalidation);
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.setValueChanging(false);
        stateLayer.uninstallStateTransitions();
        getChildren().removeAll(checkerboard, track, stateLayer, thumb);
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
        double content = getSkinnable().getOrientation() == Orientation.HORIZONTAL
                ? 96.0
                : DEFAULT_CROSS_SIZE;
        return leftInset + content + rightInset;
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
        double content = getSkinnable().getOrientation() == Orientation.HORIZONTAL
                ? DEFAULT_CROSS_SIZE
                : 96.0;
        return topInset + content + bottomInset;
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
        double content = getSkinnable().getOrientation() == Orientation.HORIZONTAL
                ? DEFAULT_LENGTH
                : DEFAULT_CROSS_SIZE;
        return leftInset + content + rightInset;
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
        double content = getSkinnable().getOrientation() == Orientation.HORIZONTAL
                ? DEFAULT_CROSS_SIZE
                : DEFAULT_LENGTH;
        return topInset + content + bottomInset;
    }

    /// Lays out the gradient track, thumb, and thumb-local focus indicator.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ColorSlider control = getSkinnable();
        double position = valuePosition();
        double thumbSize = resolvedThumbSize();
        double trackThickness = resolvedTrackThickness();
        if (control.getOrientation() == Orientation.VERTICAL) {
            double trackX = x + (width - trackThickness) / 2.0;
            double trackY = y + thumbSize / 2.0;
            double trackHeight = Math.max(0.0, height - thumbSize);
            checkerboard.resizeRelocate(trackX, trackY, trackThickness, trackHeight);
            track.resizeRelocate(trackX, trackY, trackThickness, trackHeight);
            double centerX = x + width / 2.0;
            double centerY = trackY + trackHeight * (1.0 - position);
            layoutThumb(centerX, centerY, thumbSize);
        } else {
            double trackX = x + thumbSize / 2.0;
            double trackY = y + (height - trackThickness) / 2.0;
            double trackWidth = Math.max(0.0, width - thumbSize);
            checkerboard.resizeRelocate(trackX, trackY, trackWidth, trackThickness);
            track.resizeRelocate(trackX, trackY, trackWidth, trackThickness);
            double physicalPosition = isHorizontalRightToLeft() ? 1.0 - position : position;
            double centerX = trackX + trackWidth * physicalPosition;
            double centerY = y + height / 2.0;
            layoutThumb(centerX, centerY, thumbSize);
        }
    }

    /// Positions the thumb and focus layer around a center point.
    private void layoutThumb(double centerX, double centerY, double thumbSize) {
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

    /// Starts direct value manipulation.
    private void handleMousePressed(MouseEvent event) {
        M3ColorSlider control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        M3FocusRequests.requestFocusIfTraversable(control);
        control.setValueChanging(true);
        updateValueFromMouse(event);
        event.consume();
    }

    /// Continues direct value manipulation.
    private void handleMouseDragged(MouseEvent event) {
        if (getSkinnable().isDisabled() || !event.isPrimaryButtonDown()) {
            return;
        }
        updateValueFromMouse(event);
        event.consume();
    }

    /// Completes direct value manipulation.
    private void handleMouseReleased(MouseEvent event) {
        M3ColorSlider control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        updateValueFromMouse(event);
        control.setValueChanging(false);
        event.consume();
    }

    /// Handles keyboard channel adjustment.
    private void handleKeyPressed(KeyEvent event) {
        M3ColorSlider control = getSkinnable();
        if (control.isDisabled()) {
            return;
        }
        M3ColorChannel channel = control.getChannel();
        KeyCode code = event.getCode();
        double current = editingValue().getChannel(channel);
        double target;
        switch (code) {
            case HOME -> target = channel.getMinimum();
            case END -> target = channel.getMaximum();
            case PAGE_DOWN -> target = current - channel.getBlockIncrement();
            case PAGE_UP -> target = current + channel.getBlockIncrement();
            case LEFT -> target = current + (isHorizontalRightToLeft()
                    ? channel.getUnitIncrement()
                    : -channel.getUnitIncrement());
            case RIGHT -> target = current + (isHorizontalRightToLeft()
                    ? -channel.getUnitIncrement()
                    : channel.getUnitIncrement());
            case DOWN -> target = current - channel.getUnitIncrement();
            case UP -> target = current + channel.getUnitIncrement();
            default -> {
                return;
            }
        }
        setChannelValue(channel.constrain(target));
        event.consume();
    }

    /// Updates the edited channel from a local pointer coordinate.
    private void updateValueFromMouse(MouseEvent event) {
        M3ColorSlider control = getSkinnable();
        double thumbSize = resolvedThumbSize();
        double position;
        if (control.getOrientation() == Orientation.VERTICAL) {
            double length = Math.max(0.0, control.getHeight() - thumbSize);
            position = length == 0.0 ? 0.0 : 1.0 - (event.getY() - thumbSize / 2.0) / length;
        } else {
            double length = Math.max(0.0, control.getWidth() - thumbSize);
            position = length == 0.0 ? 0.0 : (event.getX() - thumbSize / 2.0) / length;
            if (isHorizontalRightToLeft()) {
                position = 1.0 - position;
            }
        }
        M3ColorChannel channel = control.getChannel();
        setChannelValue(channel.fromPosition(clamp(position)));
    }

    /// Returns the CSS-resolved thumb diameter or its fallback value.
    private double resolvedThumbSize() {
        double width = thumb.prefWidth(-1.0);
        double height = thumb.prefHeight(-1.0);
        double size = Math.max(width, height);
        return Double.isFinite(size) && size > 0.0 ? size : DEFAULT_THUMB_SIZE;
    }

    /// Returns the CSS-resolved track thickness or its fallback value.
    private double resolvedTrackThickness() {
        double width = track.prefWidth(-1.0);
        double height = track.prefHeight(-1.0);
        double thickness = Math.max(width, height);
        return Double.isFinite(thickness) && thickness > 0.0 ? thickness : DEFAULT_TRACK_THICKNESS;
    }

    /// Stores a channel value in its compatible editing color space.
    private void setChannelValue(double value) {
        M3ColorSlider control = getSkinnable();
        M3ColorChannel channel = control.getChannel();
        M3ColorSpace space = M3ColorMath.editingSpace(control.getValue(), channel);
        control.setValue(M3ColorMath.withChannel(control.getValue(), space, channel, value));
    }

    /// Returns the current value converted to the edited channel's compatible space.
    private M3Color editingValue() {
        M3ColorSlider control = getSkinnable();
        M3ColorSpace space = M3ColorMath.editingSpace(control.getValue(), control.getChannel());
        return control.getValue().toColorSpace(space);
    }

    /// Returns the edited channel's normalized thumb position.
    private double valuePosition() {
        M3ColorSlider control = getSkinnable();
        M3ColorChannel channel = control.getChannel();
        return channel.toPosition(editingValue().getChannel(channel));
    }

    /// Rebuilds the channel gradient when its non-edited state changed.
    private void updateGradient() {
        M3ColorSlider control = getSkinnable();
        M3ColorChannel channel = control.getChannel();
        M3ColorSpace colorSpace = M3ColorMath.editingSpace(control.getValue(), channel);
        M3Color converted = control.getValue().toColorSpace(colorSpace);
        List<M3ColorChannel> channels = colorSpace.getChannels();
        double first = gradientComponent(converted, channels.get(0), channel);
        double second = gradientComponent(converted, channels.get(1), channel);
        double third = gradientComponent(converted, channels.get(2), channel);
        Orientation orientation = control.getOrientation();
        if (channel == gradientChannel
                && orientation == gradientOrientation
                && colorSpace == gradientColorSpace
                && Double.compare(first, gradientFirst) == 0
                && Double.compare(second, gradientSecond) == 0
                && Double.compare(third, gradientThird) == 0) {
            return;
        }

        gradientColorSpace = colorSpace;
        gradientChannel = channel;
        gradientOrientation = orientation;
        gradientFirst = first;
        gradientSecond = second;
        gradientThird = third;
        M3Color gradientSource = channel == M3ColorChannel.ALPHA
                ? converted
                : converted.withChannel(M3ColorChannel.ALPHA, 1.0);
        List<Stop> stops = new ArrayList<>(GRADIENT_INTERVALS + 1);
        for (int index = 0; index <= GRADIENT_INTERVALS; index++) {
            double physicalPosition = (double) index / GRADIENT_INTERVALS;
            double logicalPosition = orientation == Orientation.HORIZONTAL && isHorizontalRightToLeft()
                    ? 1.0 - physicalPosition
                    : physicalPosition;
            M3Color color = gradientSource.withChannel(channel, channel.fromPosition(logicalPosition));
            stops.add(new Stop(physicalPosition, color.toFxColor()));
        }
        LinearGradient gradient = orientation == Orientation.VERTICAL
                ? new LinearGradient(0.0, 1.0, 0.0, 0.0, true, CycleMethod.NO_CYCLE, stops)
                : new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, stops);
        track.setBackground(new Background(
                new BackgroundFill(gradient, PILL_RADII, Insets.EMPTY)
        ));
    }

    /// Applies an opaque version of the current color to the visible thumb.
    private void updateThumbPaint() {
        thumb.setColor(getSkinnable().getValue());
    }

    /// Clears the gradient cache.
    private void invalidateGradient() {
        gradientColorSpace = null;
        gradientChannel = null;
        gradientOrientation = null;
        gradientFirst = Double.NaN;
        gradientSecond = Double.NaN;
        gradientThird = Double.NaN;
        updateGradient();
    }

    /// Returns one channel component used to identify the current gradient.
    ///
    /// @param color the current color in the gradient color space
    /// @param component the ordered color-space channel
    /// @param editedChannel the channel varied by the gradient
    /// @return the retained component value, or the edited-channel minimum
    private static double gradientComponent(
            M3Color color,
            M3ColorChannel component,
            M3ColorChannel editedChannel
    ) {
        return component == editedChannel
                ? editedChannel.getMinimum()
                : color.getChannel(component);
    }

    /// Returns whether horizontal keyboard navigation is mirrored.
    private boolean isHorizontalRightToLeft() {
        return getSkinnable().getOrientation() == Orientation.HORIZONTAL
                && M3NodeLayout.isRightToLeft(getSkinnable());
    }

    /// Restricts a normalized value to `0.0..1.0`.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
