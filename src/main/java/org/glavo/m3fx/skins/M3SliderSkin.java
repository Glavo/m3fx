// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
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

    /// The active slider track from the minimum value to the current value.
    private final Region activeTrack = new Region();

    /// The circular marker near the maximum-value end of the inactive track.
    private final Region stopIndicator = new Region();

    /// The draggable slider thumb.
    private final Region thumb = new Region();

    /// The thumb-bounded state layer used for hover, focus, pressed, and ripple feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The animated normalized thumb position.
    private final DoubleProperty displayedPosition = new SimpleDoubleProperty(this, "displayedPosition");

    /// The displayed value transition animation.
    private final M3DoubleTransition valueAnimation = new M3DoubleTransition(displayedPosition);

    /// The latest content x-coordinate supplied by the skin layout pass.
    private double layoutX;

    /// The latest content y-coordinate supplied by the skin layout pass.
    private double layoutY;

    /// The latest content width supplied by the skin layout pass.
    private double layoutWidth;

    /// The latest content height supplied by the skin layout pass.
    private double layoutHeight;

    /// Whether cached content bounds are available for direct position updates.
    private boolean hasLayoutBounds;

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

    /// Applies displayed-position changes directly to the slider's internal nodes.
    private final InvalidationListener displayedPositionInvalidation = observable -> updateDisplayedGeometry();

    /// Applies track shape token changes to the visible track segments.
    private final InvalidationListener trackShapeInvalidation = observable -> updateTrackStyle();

    /// Applies thumb width token changes to the visible thumb shape.
    private final InvalidationListener thumbStyleInvalidation = observable -> updateThumbStyle();

    /// Updates the displayed position after value changes.
    private final InvalidationListener valueInvalidation = observable -> updateDisplayedPosition();

    /// Snaps the displayed position after range changes.
    private final InvalidationListener rangeInvalidation =
            observable -> setDisplayedPositionImmediately(valueToPosition(getSkinnable().getValue()));

    /// Clears transient interaction state when the slider becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> resetDisabledInteractionState();

    /// Settles running value transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), valueAnimation)
            );

    /// Creates a slider skin.
    ///
    /// @param control the slider controlled by this skin
    public M3SliderSkin(M3Slider control) {
        super(control);
        track.getStyleClass().add("track");
        activeTrack.getStyleClass().add("active-track");
        stopIndicator.getStyleClass().add("stop-indicator");
        thumb.getStyleClass().add("thumb");
        track.setMouseTransparent(true);
        activeTrack.setMouseTransparent(true);
        stopIndicator.setMouseTransparent(true);
        thumb.setMouseTransparent(true);
        getChildren().addAll(track, activeTrack, stopIndicator, stateLayer, thumb);
        stateLayer.installStateTransitions(control);
        displayedPosition.set(valueToPosition(control.getValue()));
        displayedPosition.addListener(displayedPositionInvalidation);

        control.valueProperty().addListener(valueInvalidation);
        control.minProperty().addListener(rangeInvalidation);
        control.maxProperty().addListener(rangeInvalidation);
        control.orientationProperty().addListener(layoutInvalidation);
        control.effectiveNodeOrientationProperty().addListener(layoutInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.trackShapeProperty().addListener(trackShapeInvalidation);
        control.stopIndicatorSizeProperty().addListener(layoutInvalidation);
        control.thumbSizeProperty().addListener(layoutInvalidation);
        control.thumbWidthProperty().addListener(layoutInvalidation);
        control.thumbWidthProperty().addListener(thumbStyleInvalidation);
        control.thumbTrackGapProperty().addListener(layoutInvalidation);
        control.touchTargetSizeProperty().addListener(layoutInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);

        updateTrackStyle();
        updateThumbStyle();
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
        displayedPosition.removeListener(displayedPositionInvalidation);
        control.valueProperty().removeListener(valueInvalidation);
        control.minProperty().removeListener(rangeInvalidation);
        control.maxProperty().removeListener(rangeInvalidation);
        control.orientationProperty().removeListener(layoutInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(layoutInvalidation);
        control.trackThicknessProperty().removeListener(layoutInvalidation);
        control.trackShapeProperty().removeListener(trackShapeInvalidation);
        control.stopIndicatorSizeProperty().removeListener(layoutInvalidation);
        control.thumbSizeProperty().removeListener(layoutInvalidation);
        control.thumbWidthProperty().removeListener(layoutInvalidation);
        control.thumbWidthProperty().removeListener(thumbStyleInvalidation);
        control.thumbTrackGapProperty().removeListener(layoutInvalidation);
        control.touchTargetSizeProperty().removeListener(layoutInvalidation);
        motionSettingsObserver.dispose();
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.disabledProperty().removeListener(disabledInvalidation);
        stateLayer.uninstallStateTransitions();
        stateLayer.reset();
        super.dispose();
    }

    /// Applies the slider track shape token to both track segments.
    private void updateTrackStyle() {
        String shape = formatPixels(getSkinnable().getTrackShape());
        track.setStyle(
                "-fx-background-color: -m3-color-secondary-container;"
                        + " -fx-background-insets: 0;"
                        + " -fx-background-radius: " + shape + ";"
                        + " -fx-border-color: transparent;"
                        + " -fx-border-insets: 0;"
                        + " -fx-border-radius: " + shape + ";"
                        + " -fx-border-width: 0px;"
        );
        activeTrack.setStyle(
                "-fx-background-color: -m3-color-primary;"
                        + " -fx-background-insets: 0;"
                        + " -fx-background-radius: " + shape + ";"
                        + " -fx-border-color: transparent;"
                        + " -fx-border-insets: 0;"
                        + " -fx-border-radius: " + shape + ";"
                        + " -fx-border-width: 0px;"
        );
    }

    /// Applies the slider thumb shape from the current short-side width token.
    private void updateThumbStyle() {
        String radius = formatPixels(getSkinnable().getThumbWidth() / 2.0);
        thumb.setStyle(
                "-fx-background-color: -m3-color-primary;"
                        + " -fx-background-insets: 0;"
                        + " -fx-background-radius: " + radius + ";"
                        + " -fx-border-color: transparent;"
                        + " -fx-border-insets: 0;"
                        + " -fx-border-width: 0px;"
                        + " -fx-effect: null;"
                        + " -fx-padding: 0px;"
        );
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
        return leftInset + slider.getThumbWidth() + rightInset;
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
            return topInset + slider.getThumbWidth() + bottomInset;
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
        layoutX = x;
        layoutY = y;
        layoutWidth = width;
        layoutHeight = height;
        hasLayoutBounds = true;
        layoutSliderGeometry(x, y, width, height);
    }

    /// Positions the slider nodes using the supplied content bounds and current displayed position.
    private void layoutSliderGeometry(double x, double y, double width, double height) {
        M3Slider slider = getSkinnable();
        double thumbSize = slider.getThumbSize();
        double thumbWidth = slider.getThumbWidth();
        double thumbTrackGap = slider.getThumbTrackGap();
        double trackThickness = slider.getTrackThickness();
        double position = displayedPosition.get();

        if (slider.getOrientation() == Orientation.VERTICAL) {
            layoutVerticalSlider(x, y, width, height, thumbSize, thumbWidth, thumbTrackGap, trackThickness, position);
        } else {
            layoutHorizontalSlider(x, y, width, height, thumbSize, thumbWidth, thumbTrackGap, trackThickness, position);
        }
    }

    /// Updates slider geometry without propagating an animation pulse into parent layout.
    private void updateDisplayedGeometry() {
        if (!hasLayoutBounds) {
            getSkinnable().requestLayout();
            return;
        }
        layoutSliderGeometry(layoutX, layoutY, layoutWidth, layoutHeight);
    }

    /// Positions horizontal slider nodes.
    private void layoutHorizontalSlider(
            double x,
            double y,
            double width,
            double height,
            double thumbSize,
            double thumbWidth,
            double thumbTrackGap,
            double trackThickness,
            double position
    ) {
        double trackLength = Math.max(0.0, width - thumbWidth);
        double trackStart = x + thumbWidth / 2.0;
        double trackEnd = trackStart + trackLength;
        double trackY = y + (height - trackThickness) / 2.0;
        double thumbCenterX = trackStart + trackLength * position;
        double thumbX = thumbCenterX - thumbWidth / 2.0;
        double thumbY = y + (height - thumbSize) / 2.0;

        double inactiveTrackStart = layoutHorizontalTrackSegments(
                trackStart,
                trackEnd,
                trackY,
                trackThickness,
                thumbCenterX,
                thumbTrackGap
        );
        layoutHorizontalStopIndicator(trackEnd, inactiveTrackStart, trackY, trackThickness);
        stateLayer.layoutLayer(
                thumbCenterX - getSkinnable().getTouchTargetSize() / 2.0,
                y + (height - getSkinnable().getTouchTargetSize()) / 2.0,
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize() / 2.0
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbWidth, thumbSize);
    }

    /// Positions horizontal active and inactive track segments around the handle gap.
    ///
    /// @return the leading edge of the inactive track segment
    private double layoutHorizontalTrackSegments(
            double trackStart,
            double trackEnd,
            double trackY,
            double trackThickness,
            double thumbCenterX,
            double thumbTrackGap
    ) {
        double leadingGapEdge = clampToRange(thumbCenterX - thumbTrackGap, trackStart, trackEnd);
        double trailingGapEdge = clampToRange(thumbCenterX + thumbTrackGap, trackStart, trackEnd);
        activeTrack.resizeRelocate(trackStart, trackY, leadingGapEdge - trackStart, trackThickness);
        track.resizeRelocate(trailingGapEdge, trackY, trackEnd - trailingGapEdge, trackThickness);
        return trailingGapEdge;
    }

    /// Positions the horizontal inactive-track stop indicator when enough track remains visible.
    private void layoutHorizontalStopIndicator(
            double trackEnd,
            double inactiveTrackStart,
            double trackY,
            double trackThickness
    ) {
        M3Slider slider = getSkinnable();
        double size = slider.getStopIndicatorSize();
        double radius = size / 2.0;
        double trackCornerRadius = Math.max(
                radius,
                Math.min(trackThickness / 2.0, slider.getTrackShape())
        );
        boolean visible = size > 0.0 && trackEnd - inactiveTrackStart > trackCornerRadius + radius;
        stopIndicator.setVisible(visible);
        if (visible) {
            stopIndicator.resizeRelocate(
                    trackEnd - trackCornerRadius - radius,
                    trackY + (trackThickness - size) / 2.0,
                    size,
                    size
            );
        }
    }

    /// Positions vertical slider nodes.
    @SuppressWarnings("SuspiciousNameCombination")
    private void layoutVerticalSlider(
            double x,
            double y,
            double width,
            double height,
            double thumbSize,
            double thumbWidth,
            double thumbTrackGap,
            double trackThickness,
            double position
    ) {
        double trackLength = Math.max(0.0, height - thumbWidth);
        double trackStart = y + thumbWidth / 2.0;
        double trackEnd = trackStart + trackLength;
        double trackX = x + (width - trackThickness) / 2.0;
        double thumbCenterY = trackEnd - trackLength * position;
        double thumbX = x + (width - thumbSize) / 2.0;
        double thumbY = thumbCenterY - thumbWidth / 2.0;

        double inactiveTrackEnd = layoutVerticalTrackSegments(
                trackX,
                trackStart,
                trackEnd,
                trackThickness,
                thumbCenterY,
                thumbTrackGap
        );
        layoutVerticalStopIndicator(trackX, trackStart, inactiveTrackEnd, trackThickness);
        stateLayer.layoutLayer(
                x + (width - getSkinnable().getTouchTargetSize()) / 2.0,
                thumbCenterY - getSkinnable().getTouchTargetSize() / 2.0,
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize() / 2.0
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbWidth);
    }

    /// Positions vertical active and inactive track segments around the handle gap.
    ///
    /// @return the trailing edge of the inactive track segment
    private double layoutVerticalTrackSegments(
            double trackX,
            double trackStart,
            double trackEnd,
            double trackThickness,
            double thumbCenterY,
            double thumbTrackGap
    ) {
        double upperGapEdge = clampToRange(thumbCenterY - thumbTrackGap, trackStart, trackEnd);
        double lowerGapEdge = clampToRange(thumbCenterY + thumbTrackGap, trackStart, trackEnd);
        track.resizeRelocate(trackX, trackStart, trackThickness, upperGapEdge - trackStart);
        activeTrack.resizeRelocate(trackX, lowerGapEdge, trackThickness, trackEnd - lowerGapEdge);
        return upperGapEdge;
    }

    /// Positions the vertical inactive-track stop indicator when enough track remains visible.
    private void layoutVerticalStopIndicator(
            double trackX,
            double trackStart,
            double inactiveTrackEnd,
            double trackThickness
    ) {
        M3Slider slider = getSkinnable();
        double size = slider.getStopIndicatorSize();
        double radius = size / 2.0;
        double trackCornerRadius = Math.max(
                radius,
                Math.min(trackThickness / 2.0, slider.getTrackShape())
        );
        boolean visible = size > 0.0 && inactiveTrackEnd - trackStart > trackCornerRadius + radius;
        stopIndicator.setVisible(visible);
        if (visible) {
            stopIndicator.resizeRelocate(
                    trackX + (trackThickness - size) / 2.0,
                    trackStart + trackCornerRadius - radius,
                    size,
                    size
            );
        }
    }

    /// Starts value adjustment from a primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        M3Slider slider = getSkinnable();
        if (slider.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        M3FocusRequests.requestFocusIfTraversable(slider);
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
        double thumbWidth = slider.getThumbWidth();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            double length = Math.max(0.0, slider.getHeight() - thumbWidth);
            if (length == 0.0) {
                return 0.0;
            }
            double start = thumbWidth / 2.0;
            return clamp(1.0 - (point.getY() - start) / length);
        }

        double length = Math.max(0.0, slider.getWidth() - thumbWidth);
        if (length == 0.0) {
            return 0.0;
        }
        double start = thumbWidth / 2.0;
        double position = clamp((point.getX() - start) / length);
        return isHorizontalRightToLeft() ? 1.0 - position : position;
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

    /// Returns whether this slider currently mirrors horizontal value geometry.
    private boolean isHorizontalRightToLeft() {
        M3Slider slider = getSkinnable();
        if (slider.getOrientation() != Orientation.HORIZONTAL) {
            return false;
        }
        return M3NodeLayout.isRightToLeft(slider);
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
        valueAnimation.configure(spec, targetPosition);
        M3Animation.playFromStart(getSkinnable(), valueAnimation);
    }

    /// Sets the displayed position immediately and clears pending transitions.
    private void setDisplayedPositionImmediately(double position) {
        valueAnimation.stop();
        displayedPosition.set(position);
    }

    /// Clamps a normalized value position to the supported range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Clamps a coordinate to a local range.
    private static double clampToRange(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }
}
