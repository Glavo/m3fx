// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3Slider;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Slider].
@NotNullByDefault
public class M3SliderSkin extends SkinBase<M3Slider> {
    /// The default horizontal preferred length for sliders without an explicit width.
    private static final double DEFAULT_HORIZONTAL_LENGTH = 200.0;

    /// The default vertical preferred length for sliders without an explicit height.
    private static final double DEFAULT_VERTICAL_LENGTH = 200.0;

    /// The visible slider track.
    private final Region track = new Region();

    /// The draggable slider thumb.
    private final Region thumb = new Region();

    /// Handles mouse presses on the slider control.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles mouse drags on the slider control.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles mouse releases on the slider control.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard navigation while the slider is focused.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Creates a slider skin.
    public M3SliderSkin(M3Slider control) {
        super(control);
        track.getStyleClass().add("track");
        thumb.getStyleClass().add("thumb");
        track.setMouseTransparent(true);
        thumb.setMouseTransparent(true);
        getChildren().addAll(track, thumb);

        control.valueProperty().addListener(observable -> getSkinnable().requestLayout());
        control.minProperty().addListener(observable -> getSkinnable().requestLayout());
        control.maxProperty().addListener(observable -> getSkinnable().requestLayout());
        control.orientationProperty().addListener(observable -> getSkinnable().requestLayout());
        control.trackThicknessProperty().addListener(observable -> getSkinnable().requestLayout());
        control.thumbSizeProperty().addListener(observable -> getSkinnable().requestLayout());
        control.touchTargetSizeProperty().addListener(observable -> getSkinnable().requestLayout());

        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
    }

    /// Releases event handlers installed by this skin.
    @Override
    public void dispose() {
        M3Slider control = getSkinnable();
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        super.dispose();
    }

    /// Computes the minimum width needed to show the touch target.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3Slider slider = getSkinnable();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            return leftInset + slider.getTouchTargetSize() + rightInset;
        }
        return leftInset + slider.getThumbSize() + rightInset;
    }

    /// Computes the minimum height needed to show the touch target.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3Slider slider = getSkinnable();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            return topInset + slider.getThumbSize() + bottomInset;
        }
        return topInset + slider.getTouchTargetSize() + bottomInset;
    }

    /// Computes the preferred width for the slider orientation.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3Slider slider = getSkinnable();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            return leftInset + slider.getTouchTargetSize() + rightInset;
        }
        return leftInset + DEFAULT_HORIZONTAL_LENGTH + rightInset;
    }

    /// Computes the preferred height for the slider orientation.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3Slider slider = getSkinnable();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            return topInset + DEFAULT_VERTICAL_LENGTH + bottomInset;
        }
        return topInset + slider.getTouchTargetSize() + bottomInset;
    }

    /// Positions the track and thumb inside the control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3Slider slider = getSkinnable();
        double thumbSize = slider.getThumbSize();
        double trackThickness = slider.getTrackThickness();
        double position = valueToPosition(slider.getValue());

        if (slider.getOrientation() == Orientation.VERTICAL) {
            layoutVerticalSlider(x, y, width, height, thumbSize, trackThickness, position);
        } else {
            layoutHorizontalSlider(x, y, width, height, thumbSize, trackThickness, position);
        }
    }

    /// Positions horizontal slider nodes.
    private void layoutHorizontalSlider(
            double x,
            double y,
            double width,
            double height,
            double thumbSize,
            double trackThickness,
            double position
    ) {
        double trackLength = Math.max(0.0, width - thumbSize);
        double trackX = x + thumbSize / 2.0;
        double trackY = y + (height - trackThickness) / 2.0;
        double thumbX = trackX + trackLength * position - thumbSize / 2.0;
        double thumbY = y + (height - thumbSize) / 2.0;

        track.resizeRelocate(trackX, trackY, trackLength, trackThickness);
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbSize);
    }

    /// Positions vertical slider nodes.
    private void layoutVerticalSlider(
            double x,
            double y,
            double width,
            double height,
            double thumbSize,
            double trackThickness,
            double position
    ) {
        double trackLength = Math.max(0.0, height - thumbSize);
        double trackX = x + (width - trackThickness) / 2.0;
        double trackY = y + thumbSize / 2.0;
        double thumbX = x + (width - thumbSize) / 2.0;
        double thumbY = trackY + trackLength * (1.0 - position) - thumbSize / 2.0;

        track.resizeRelocate(trackX, trackY, trackThickness, trackLength);
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbSize);
    }

    /// Starts value adjustment from a primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        M3Slider slider = getSkinnable();
        slider.requestFocus();
        slider.setValueChanging(true);
        updateValueFromMouse(event);
        event.consume();
    }

    /// Continues value adjustment while the primary mouse button is held.
    private void handleMouseDragged(MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            return;
        }

        updateValueFromMouse(event);
        event.consume();
    }

    /// Finishes value adjustment after a primary mouse release.
    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        updateValueFromMouse(event);
        getSkinnable().setValueChanging(false);
        event.consume();
    }

    /// Adjusts the slider value for keyboard navigation.
    private void handleKeyPressed(KeyEvent event) {
        M3Slider slider = getSkinnable();
        KeyCode code = event.getCode();
        switch (code) {
            case HOME -> {
                slider.adjustValue(slider.getMin());
                event.consume();
            }
            case END -> {
                slider.adjustValue(slider.getMax());
                event.consume();
            }
            case LEFT, DOWN -> {
                slider.decrement();
                event.consume();
            }
            case RIGHT, UP -> {
                slider.increment();
                event.consume();
            }
            case PAGE_DOWN -> {
                slider.adjustValue(slider.getValue() - slider.getBlockIncrement());
                event.consume();
            }
            case PAGE_UP -> {
                slider.adjustValue(slider.getValue() + slider.getBlockIncrement());
                event.consume();
            }
            default -> {
            }
        }
    }

    /// Updates the value from a mouse event in the control coordinate space.
    private void updateValueFromMouse(MouseEvent event) {
        M3Slider slider = getSkinnable();
        Point2D point = slider.sceneToLocal(event.getSceneX(), event.getSceneY());
        double position = mousePositionToValuePosition(point);
        slider.adjustValue(positionToValue(position));
    }

    /// Converts a mouse coordinate to a normalized value position.
    private double mousePositionToValuePosition(Point2D point) {
        M3Slider slider = getSkinnable();
        double thumbSize = slider.getThumbSize();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            double length = Math.max(0.0, slider.getHeight() - thumbSize);
            if (length == 0.0) {
                return 0.0;
            }
            double start = thumbSize / 2.0;
            return clamp(1.0 - (point.getY() - start) / length);
        }

        double length = Math.max(0.0, slider.getWidth() - thumbSize);
        if (length == 0.0) {
            return 0.0;
        }
        double start = thumbSize / 2.0;
        return clamp((point.getX() - start) / length);
    }

    /// Converts a slider value to a normalized position.
    private double valueToPosition(double value) {
        M3Slider slider = getSkinnable();
        double range = slider.getMax() - slider.getMin();
        if (range == 0.0) {
            return 0.0;
        }
        return clamp((value - slider.getMin()) / range);
    }

    /// Converts a normalized position to a slider value.
    private double positionToValue(double position) {
        M3Slider slider = getSkinnable();
        return slider.getMin() + (slider.getMax() - slider.getMin()) * clamp(position);
    }

    /// Clamps a normalized value position to the supported range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
