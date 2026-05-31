// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.internal.M3Animation;
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

    /// The thumb-bounded state layer used for hover, focus, pressed, and ripple feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The animated normalized thumb position.
    private final DoubleProperty displayedPosition = new SimpleDoubleProperty(this, "displayedPosition");

    /// The displayed value transition animation.
    private final Timeline valueAnimation = new Timeline();

    /// Handles mouse presses on the slider control.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles mouse drags on the slider control.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles mouse releases on the slider control.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard navigation while the slider is focused.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Requests layout after displayed position, orientation, or token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Updates the displayed position after value changes.
    private final InvalidationListener valueInvalidation = observable -> updateDisplayedPosition();

    /// Snaps the displayed position after range changes.
    private final InvalidationListener rangeInvalidation = observable -> snapDisplayedPosition();

    /// Clears transient interaction state when the slider becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> resetDisabledInteractionState();

    /// Settles running value transitions when runtime motion settings change.
    private final InvalidationListener motionSettingsInvalidation =
            observable -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), valueAnimation);

    /// Creates a slider skin.
    ///
    /// @param control the slider controlled by this skin
    public M3SliderSkin(M3Slider control) {
        super(control);
        track.getStyleClass().add("track");
        thumb.getStyleClass().add("thumb");
        track.setMouseTransparent(true);
        thumb.setMouseTransparent(true);
        getChildren().addAll(track, stateLayer, thumb);
        stateLayer.installStateTransitions(control);
        displayedPosition.set(valueToPosition(control.getValue()));
        displayedPosition.addListener(layoutInvalidation);

        control.valueProperty().addListener(valueInvalidation);
        control.minProperty().addListener(rangeInvalidation);
        control.maxProperty().addListener(rangeInvalidation);
        control.orientationProperty().addListener(layoutInvalidation);
        control.effectiveNodeOrientationProperty().addListener(layoutInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.thumbSizeProperty().addListener(layoutInvalidation);
        control.touchTargetSizeProperty().addListener(layoutInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);
        M3MotionSettings.addSettingsChangeListener(motionSettingsInvalidation);

        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
    }

    /// Releases event handlers installed by this skin.
    @Override
    public void dispose() {
        M3Slider control = getSkinnable();
        valueAnimation.stop();
        displayedPosition.removeListener(layoutInvalidation);
        control.valueProperty().removeListener(valueInvalidation);
        control.minProperty().removeListener(rangeInvalidation);
        control.maxProperty().removeListener(rangeInvalidation);
        control.orientationProperty().removeListener(layoutInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(layoutInvalidation);
        control.trackThicknessProperty().removeListener(layoutInvalidation);
        control.thumbSizeProperty().removeListener(layoutInvalidation);
        control.touchTargetSizeProperty().removeListener(layoutInvalidation);
        M3MotionSettings.removeSettingsChangeListener(motionSettingsInvalidation);
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.disabledProperty().removeListener(disabledInvalidation);
        stateLayer.uninstallStateTransitions();
        stateLayer.reset();
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
        double position = displayedPosition.get();

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
        double visualPosition = horizontalVisualPosition(position);
        double thumbX = trackX + trackLength * visualPosition - thumbSize / 2.0;
        double thumbY = y + (height - thumbSize) / 2.0;

        track.resizeRelocate(trackX, trackY, trackLength, trackThickness);
        stateLayer.layoutLayer(
                thumbX + thumbSize / 2.0 - getSkinnable().getTouchTargetSize() / 2.0,
                y + (height - getSkinnable().getTouchTargetSize()) / 2.0,
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize() / 2.0
        );
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
        stateLayer.layoutLayer(
                x + (width - getSkinnable().getTouchTargetSize()) / 2.0,
                thumbY + thumbSize / 2.0 - getSkinnable().getTouchTargetSize() / 2.0,
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize() / 2.0
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbSize);
    }

    /// Starts value adjustment from a primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        M3Slider slider = getSkinnable();
        if (slider.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        slider.requestFocus();
        slider.setValueChanging(true);
        updateValueFromMouse(event);
        stateLayer.playCenteredRipple();
        event.consume();
    }

    /// Continues value adjustment while the primary mouse button is held.
    private void handleMouseDragged(MouseEvent event) {
        if (getSkinnable().isDisabled() || !event.isPrimaryButtonDown()) {
            return;
        }

        updateValueFromMouse(event);
        event.consume();
    }

    /// Finishes value adjustment after a primary mouse release.
    private void handleMouseReleased(MouseEvent event) {
        M3Slider slider = getSkinnable();
        if (slider.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        updateValueFromMouse(event);
        stateLayer.releaseRipple();
        slider.setValueChanging(false);
        event.consume();
    }

    /// Adjusts the slider value for keyboard navigation.
    private void handleKeyPressed(KeyEvent event) {
        M3Slider slider = getSkinnable();
        if (slider.isDisabled()) {
            return;
        }

        KeyCode code = event.getCode();
        switch (code) {
            case HOME -> {
                slider.adjustValue(slider.getMin());
                playReleasedCenteredRipple();
                event.consume();
            }
            case END -> {
                slider.adjustValue(slider.getMax());
                playReleasedCenteredRipple();
                event.consume();
            }
            case LEFT -> {
                if (isHorizontalRightToLeft()) {
                    slider.increment();
                } else {
                    slider.decrement();
                }
                playReleasedCenteredRipple();
                event.consume();
            }
            case RIGHT -> {
                if (isHorizontalRightToLeft()) {
                    slider.decrement();
                } else {
                    slider.increment();
                }
                playReleasedCenteredRipple();
                event.consume();
            }
            case DOWN -> {
                slider.decrement();
                playReleasedCenteredRipple();
                event.consume();
            }
            case UP -> {
                slider.increment();
                playReleasedCenteredRipple();
                event.consume();
            }
            case PAGE_DOWN -> {
                slider.adjustValue(slider.getValue() - slider.getBlockIncrement());
                playReleasedCenteredRipple();
                event.consume();
            }
            case PAGE_UP -> {
                slider.adjustValue(slider.getValue() + slider.getBlockIncrement());
                playReleasedCenteredRipple();
                event.consume();
            }
            default -> {
            }
        }
    }

    /// Plays a centered ripple for an instantaneous keyboard adjustment.
    private void playReleasedCenteredRipple() {
        stateLayer.playCenteredRipple();
        stateLayer.releaseRipple();
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
        double physicalPosition = clamp((point.getX() - start) / length);
        return isHorizontalRightToLeft() ? 1.0 - physicalPosition : physicalPosition;
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

    /// Returns a horizontal position after applying right-to-left visual mirroring.
    private double horizontalVisualPosition(double position) {
        return isHorizontalRightToLeft() ? 1.0 - position : position;
    }

    /// Returns whether this slider currently mirrors horizontal value geometry.
    private boolean isHorizontalRightToLeft() {
        M3Slider slider = getSkinnable();
        return slider.getOrientation() == Orientation.HORIZONTAL
                && slider.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }

    /// Converts a normalized position to a slider value.
    private double positionToValue(double position) {
        M3Slider slider = getSkinnable();
        return slider.getMin() + (slider.getMax() - slider.getMin()) * clamp(position);
    }

    /// Resets interaction state when disabled during pointer or keyboard work.
    private void resetDisabledInteractionState() {
        if (getSkinnable().isDisabled()) {
            valueAnimation.stop();
            displayedPosition.set(valueToPosition(getSkinnable().getValue()));
            getSkinnable().setValueChanging(false);
            stateLayer.reset();
        }
    }

    /// Updates the displayed position with animation when the value is not being dragged.
    private void updateDisplayedPosition() {
        double targetPosition = valueToPosition(getSkinnable().getValue());
        if (getSkinnable().isValueChanging() || getSkinnable().getScene() == null) {
            setDisplayedPositionImmediately(targetPosition);
            return;
        }

        valueAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        valueAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(displayedPosition, targetPosition, spec.interpolator())
        ));
        M3Animation.playFromStart(getSkinnable(), valueAnimation);
    }

    /// Snaps the displayed position to the current value without animation.
    private void snapDisplayedPosition() {
        setDisplayedPositionImmediately(valueToPosition(getSkinnable().getValue()));
    }

    /// Sets the displayed position immediately and clears pending transitions.
    private void setDisplayedPositionImmediately(double position) {
        valueAnimation.stop();
        displayedPosition.set(position);
        getSkinnable().requestLayout();
    }

    /// Clamps a normalized value position to the supported range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
