// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Slider].
///
/// The skin renders inactive, active, and optional secondary tracks together with the handle, stop indicator, tick
/// marks, and value label. Its visual state follows value changes from pointer, keyboard, programmatic, and
/// accessibility input; horizontal value geometry mirrors under right-to-left orientation.
@NotNullByDefault
public class M3SliderSkin extends SkinBase<M3Slider> {
    /// The pseudo-class used for stop indicators on the active track.
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    /// The default horizontal preferred length for sliders without an explicit width.
    private static final double DEFAULT_HORIZONTAL_LENGTH = 200.0;

    /// The default vertical preferred length for sliders without an explicit height.
    private static final double DEFAULT_VERTICAL_LENGTH = 200.0;

    /// The minimum Material value-indicator width.
    private static final double VALUE_INDICATOR_WIDTH = 48.0;

    /// The Material value-indicator height.
    private static final double VALUE_INDICATOR_HEIGHT = 44.0;

    /// The maximum number of pooled discrete stop nodes retained by one slider skin.
    private static final int MAX_STEP_INDICATORS = 256;

    /// The visible slider track.
    private final Region track = new Region();

    /// The second inactive track segment used by centered sliders.
    private final Region secondaryTrack = new Region();

    /// The active slider track from the minimum value to the current value.
    private final Region activeTrack = new Region();

    /// The circular marker near the maximum-value end of the inactive track.
    private final Region stopIndicator = new Region();

    /// The circular marker near the minimum-value end of a centered slider.
    private final Region secondaryStopIndicator = new Region();

    /// The pooled stop indicators for discrete slider values.
    private final Pane stepIndicatorLayer = new Pane();

    /// The draggable slider thumb.
    private final Region thumb = new Region();

    /// The slot for an optional icon inset into the active track.
    private final StackPane activeTrackGraphicSlot = new StackPane();

    /// The slot for an optional icon inset into the inactive track.
    private final StackPane inactiveTrackGraphicSlot = new StackPane();

    /// The optional value indicator shown during direct manipulation.
    private final Label valueIndicator = new Label();

    /// The active-track graphic currently mounted by this skin.
    private @Nullable Node displayedActiveTrackGraphic;

    /// Whether this skin added the active-track graphic style class.
    private boolean activeTrackGraphicStyleClassAdded;

    /// The inactive-track graphic currently mounted by this skin.
    private @Nullable Node displayedInactiveTrackGraphic;

    /// Whether this skin added the inactive-track graphic style class.
    private boolean inactiveTrackGraphicStyleClassAdded;

    /// The thumb-bounded keyboard focus indicator.
    ///
    /// Current slider tokens express pointer interaction by changing the handle width and value indicator rather
    /// than by rendering the deprecated circular state layer and ripple.
    private final M3StateLayer stateLayer = new M3StateLayer(false);

    /// The animated normalized thumb position.
    private final DoubleProperty displayedPosition = new SimpleDoubleProperty(this, "displayedPosition");

    /// The displayed value transition animation.
    private final M3DoubleTransition valueAnimation = new M3DoubleTransition(
            displayedPosition,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

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

    /// Applies orientation, mode, or track-shape changes to the visible track segments.
    private final InvalidationListener trackStyleInvalidation = observable -> {
        updateTrackStyle();
        getSkinnable().requestLayout();
    };

    /// Applies thumb width token changes to the visible thumb shape.
    private final InvalidationListener thumbStyleInvalidation = observable -> updateThumbStyle();

    /// Updates the displayed position and value indicator after value changes.
    private final InvalidationListener valueInvalidation = observable -> {
        updateDisplayedPosition();
        updateValueIndicator();
    };

    /// Replaces the mounted active-track graphic.
    private final InvalidationListener activeTrackGraphicInvalidation = observable -> updateActiveTrackGraphic();

    /// Replaces the mounted inactive-track graphic.
    private final InvalidationListener inactiveTrackGraphicInvalidation = observable -> updateInactiveTrackGraphic();

    /// Refreshes value-indicator visibility, text, and reserved layout space.
    private final InvalidationListener valueIndicatorInvalidation = observable -> {
        updateValueIndicator();
        getSkinnable().requestLayout();
    };

    /// Snaps the displayed position after range changes.
    private final InvalidationListener rangeInvalidation =
            observable -> setDisplayedPositionImmediately(valueToPosition(getSkinnable().getValue()));

    /// Clears transient interaction state when the slider becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> resetDisabledInteractionState();

    /// Creates a slider skin.
    ///
    /// @param control the slider controlled by this skin
    public M3SliderSkin(M3Slider control) {
        super(control);
        track.getStyleClass().add("track");
        secondaryTrack.getStyleClass().add("track");
        activeTrack.getStyleClass().add("active-track");
        stopIndicator.getStyleClass().add("stop-indicator");
        secondaryStopIndicator.getStyleClass().add("stop-indicator");
        thumb.getStyleClass().add("thumb");
        activeTrackGraphicSlot.getStyleClass().add("m3-slider-active-track-graphic-slot");
        inactiveTrackGraphicSlot.getStyleClass().add("m3-slider-inactive-track-graphic-slot");
        valueIndicator.getStyleClass().addAll("m3-slider-value-indicator", "m3-label-large-text");
        track.setMouseTransparent(true);
        secondaryTrack.setMouseTransparent(true);
        activeTrack.setMouseTransparent(true);
        stopIndicator.setMouseTransparent(true);
        secondaryStopIndicator.setMouseTransparent(true);
        stepIndicatorLayer.setMouseTransparent(true);
        stepIndicatorLayer.setManaged(false);
        thumb.setMouseTransparent(true);
        activeTrackGraphicSlot.setManaged(false);
        activeTrackGraphicSlot.setMouseTransparent(true);
        inactiveTrackGraphicSlot.setManaged(false);
        inactiveTrackGraphicSlot.setMouseTransparent(true);
        valueIndicator.setManaged(false);
        valueIndicator.setMouseTransparent(true);
        getChildren().setAll(
                track,
                secondaryTrack,
                activeTrack,
                stepIndicatorLayer,
                activeTrackGraphicSlot,
                inactiveTrackGraphicSlot,
                stopIndicator,
                secondaryStopIndicator,
                stateLayer,
                thumb,
                valueIndicator
        );
        stateLayer.installStateTransitions(control);
        displayedPosition.set(valueToPosition(control.getValue()));
        displayedPosition.addListener(displayedPositionInvalidation);

        control.valueProperty().addListener(valueInvalidation);
        control.minProperty().addListener(rangeInvalidation);
        control.maxProperty().addListener(rangeInvalidation);
        control.orientationProperty().addListener(trackStyleInvalidation);
        control.effectiveNodeOrientationProperty().addListener(layoutInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.trackShapeProperty().addListener(trackStyleInvalidation);
        control.stopIndicatorSizeProperty().addListener(layoutInvalidation);
        control.stopIndicatorTrailingSpaceProperty().addListener(layoutInvalidation);
        control.centeredProperty().addListener(trackStyleInvalidation);
        control.thumbSizeProperty().addListener(layoutInvalidation);
        control.thumbWidthProperty().addListener(layoutInvalidation);
        control.thumbWidthProperty().addListener(thumbStyleInvalidation);
        control.thumbTrackGapProperty().addListener(layoutInvalidation);
        control.touchTargetSizeProperty().addListener(layoutInvalidation);
        control.iconSizeProperty().addListener(layoutInvalidation);
        control.iconPaddingProperty().addListener(layoutInvalidation);
        control.valueIndicatorBottomSpaceProperty().addListener(layoutInvalidation);
        control.activeTrackGraphicProperty().addListener(activeTrackGraphicInvalidation);
        control.inactiveTrackGraphicProperty().addListener(inactiveTrackGraphicInvalidation);
        control.showValueIndicatorProperty().addListener(valueIndicatorInvalidation);
        control.labelFormatterProperty().addListener(valueIndicatorInvalidation);
        control.valueChangingProperty().addListener(valueIndicatorInvalidation);
        control.pressedProperty().addListener(valueIndicatorInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);

        updateTrackStyle();
        updateThumbStyle();
        updateActiveTrackGraphic();
        updateInactiveTrackGraphic();
        updateValueIndicator();
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
        control.orientationProperty().removeListener(trackStyleInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(layoutInvalidation);
        control.trackThicknessProperty().removeListener(layoutInvalidation);
        control.trackShapeProperty().removeListener(trackStyleInvalidation);
        control.stopIndicatorSizeProperty().removeListener(layoutInvalidation);
        control.stopIndicatorTrailingSpaceProperty().removeListener(layoutInvalidation);
        control.centeredProperty().removeListener(trackStyleInvalidation);
        control.thumbSizeProperty().removeListener(layoutInvalidation);
        control.thumbWidthProperty().removeListener(layoutInvalidation);
        control.thumbWidthProperty().removeListener(thumbStyleInvalidation);
        control.thumbTrackGapProperty().removeListener(layoutInvalidation);
        control.touchTargetSizeProperty().removeListener(layoutInvalidation);
        control.iconSizeProperty().removeListener(layoutInvalidation);
        control.iconPaddingProperty().removeListener(layoutInvalidation);
        control.valueIndicatorBottomSpaceProperty().removeListener(layoutInvalidation);
        control.activeTrackGraphicProperty().removeListener(activeTrackGraphicInvalidation);
        control.inactiveTrackGraphicProperty().removeListener(inactiveTrackGraphicInvalidation);
        control.showValueIndicatorProperty().removeListener(valueIndicatorInvalidation);
        control.labelFormatterProperty().removeListener(valueIndicatorInvalidation);
        control.valueChangingProperty().removeListener(valueIndicatorInvalidation);
        control.pressedProperty().removeListener(valueIndicatorInvalidation);
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.disabledProperty().removeListener(disabledInvalidation);
        stateLayer.uninstallStateTransitions();
        stateLayer.reset();
        getChildren().removeAll(
                track,
                secondaryTrack,
                activeTrack,
                stepIndicatorLayer,
                activeTrackGraphicSlot,
                inactiveTrackGraphicSlot,
                stopIndicator,
                secondaryStopIndicator,
                stateLayer,
                thumb,
                valueIndicator
        );
        if (displayedActiveTrackGraphic != null && activeTrackGraphicStyleClassAdded) {
            displayedActiveTrackGraphic.getStyleClass().remove("m3-slider-active-track-graphic");
        }
        if (displayedInactiveTrackGraphic != null && inactiveTrackGraphicStyleClassAdded) {
            displayedInactiveTrackGraphic.getStyleClass().remove("m3-slider-inactive-track-graphic");
        }
        activeTrackGraphicSlot.getChildren().clear();
        inactiveTrackGraphicSlot.getChildren().clear();
        displayedActiveTrackGraphic = null;
        displayedInactiveTrackGraphic = null;
        activeTrackGraphicStyleClassAdded = false;
        inactiveTrackGraphicStyleClassAdded = false;
        super.dispose();
    }

    /// Applies the slider track shape token to both track segments.
    private void updateTrackStyle() {
        M3Slider slider = getSkinnable();
        String shape = formatPixels(slider.getTrackShape());
        String leadingRadii;
        String trailingRadii;
        if (slider.getOrientation() == Orientation.VERTICAL) {
            leadingRadii = "0px 0px " + shape + " " + shape;
            trailingRadii = shape + " " + shape + " 0px 0px";
        } else {
            leadingRadii = shape + " 0px 0px " + shape;
            trailingRadii = "0px " + shape + " " + shape + " 0px";
        }
        track.setStyle("-fx-background-radius: " + trailingRadii + ";");
        secondaryTrack.setStyle("-fx-background-radius: " + leadingRadii + ";");
        activeTrack.setStyle("-fx-background-radius: " + (slider.isCentered() ? "0px" : leadingRadii) + ";");
    }

    /// Applies the slider thumb shape from the current short-side width token.
    private void updateThumbStyle() {
        String radius = formatPixels(getSkinnable().getThumbWidth() / 2.0);
        thumb.setStyle(
                "-fx-background-radius: " + radius + ";"
        );
    }

    /// Computes the minimum width needed for the configured size and orientation.
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
            double crossSize = Math.max(
                    slider.getTouchTargetSize(),
                    Math.max(slider.getThumbSize(), slider.getTrackThickness())
            );
            if (slider.isShowValueIndicator()) {
                crossSize += VALUE_INDICATOR_WIDTH + slider.getValueIndicatorBottomSpace();
            }
            return leftInset + crossSize + rightInset;
        }
        return leftInset + slider.getTouchTargetSize() + rightInset;
    }

    /// Computes the minimum height needed for the configured size and orientation.
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
            return topInset + slider.getTouchTargetSize() + bottomInset;
        }
        double crossSize = Math.max(
                slider.getTouchTargetSize(),
                Math.max(slider.getThumbSize(), slider.getTrackThickness())
        );
        if (slider.isShowValueIndicator()) {
            crossSize += VALUE_INDICATOR_HEIGHT + slider.getValueIndicatorBottomSpace();
        }
        return topInset + crossSize + bottomInset;
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
            return computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
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
        return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
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
            double indicatorReserve = slider.isShowValueIndicator()
                    ? VALUE_INDICATOR_WIDTH + slider.getValueIndicatorBottomSpace()
                    : 0.0;
            layoutVerticalSlider(
                    x + indicatorReserve,
                    y,
                    Math.max(0.0, width - indicatorReserve),
                    height,
                    thumbSize,
                    thumbWidth,
                    thumbTrackGap,
                    trackThickness,
                    position
            );
            layoutVerticalValueIndicator(x, y, indicatorReserve, height, thumbWidth, position);
        } else {
            double indicatorReserve = slider.isShowValueIndicator()
                    ? VALUE_INDICATOR_HEIGHT + slider.getValueIndicatorBottomSpace()
                    : 0.0;
            layoutHorizontalSlider(
                    x,
                    y + indicatorReserve,
                    width,
                    Math.max(0.0, height - indicatorReserve),
                    thumbSize,
                    thumbWidth,
                    thumbTrackGap,
                    trackThickness,
                    position
            );
            layoutHorizontalValueIndicator(x, y, width, indicatorReserve, thumbWidth, position);
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

        if (getSkinnable().isCentered()) {
            layoutHorizontalCenteredTrack(
                    trackStart,
                    trackEnd,
                    trackY,
                    trackThickness,
                    thumbCenterX,
                    thumbTrackGap
            );
        } else {
            double inactiveTrackStart = layoutHorizontalStandardTrack(
                    trackStart,
                    trackEnd,
                    trackY,
                    trackThickness,
                    thumbCenterX,
                    thumbTrackGap
            );
            layoutHorizontalEndStop(
                    stopIndicator,
                    false,
                    trackStart,
                    trackEnd,
                    inactiveTrackStart,
                    trackEnd,
                    trackY,
                    trackThickness
            );
            secondaryStopIndicator.setVisible(false);
        }
        layoutStepIndicators(false, trackStart, trackEnd, trackY, trackThickness, thumbCenterX);
        layoutTrackGraphics(false, trackStart, trackEnd, trackY, trackThickness, thumbCenterX);
        stateLayer.layoutLayer(
                thumbCenterX - getSkinnable().getTouchTargetSize() / 2.0,
                y + (height - getSkinnable().getTouchTargetSize()) / 2.0,
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize() / 2.0
        );
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        stateLayer.layoutFocusIndicator(
                (touchTargetSize - thumbWidth) / 2.0,
                (touchTargetSize - thumbSize) / 2.0,
                thumbWidth,
                thumbSize,
                thumbWidth / 2.0
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbWidth, thumbSize);
    }

    /// Positions horizontal active and inactive track segments around the handle gap.
    ///
    /// @return the leading edge of the inactive track segment
    private double layoutHorizontalStandardTrack(
            double trackStart,
            double trackEnd,
            double trackY,
            double trackThickness,
            double thumbCenterX,
            double thumbTrackGap
    ) {
        secondaryTrack.setVisible(false);
        double gapExtent = getSkinnable().getThumbWidth() / 2.0 + thumbTrackGap;
        double leadingGapEdge = clampToRange(thumbCenterX - gapExtent, trackStart, trackEnd);
        double trailingGapEdge = clampToRange(thumbCenterX + gapExtent, trackStart, trackEnd);
        activeTrack.resizeRelocate(trackStart, trackY, leadingGapEdge - trackStart, trackThickness);
        track.resizeRelocate(trailingGapEdge, trackY, trackEnd - trailingGapEdge, trackThickness);
        return trailingGapEdge;
    }

    /// Positions centered horizontal active and inactive track segments around the handle gap.
    private void layoutHorizontalCenteredTrack(
            double trackStart,
            double trackEnd,
            double trackY,
            double trackThickness,
            double thumbCenterX,
            double thumbTrackGap
    ) {
        secondaryTrack.setVisible(true);
        double centerX = (trackStart + trackEnd) / 2.0;
        double gapExtent = getSkinnable().getThumbWidth() / 2.0 + thumbTrackGap;
        double leadingGapEdge = clampToRange(thumbCenterX - gapExtent, trackStart, trackEnd);
        double trailingGapEdge = clampToRange(thumbCenterX + gapExtent, trackStart, trackEnd);
        double leadingInactiveEnd;
        double trailingInactiveStart;
        if (trailingGapEdge < centerX) {
            leadingInactiveEnd = leadingGapEdge;
            trailingInactiveStart = centerX;
            secondaryTrack.resizeRelocate(trackStart, trackY, leadingInactiveEnd - trackStart, trackThickness);
            activeTrack.resizeRelocate(trailingGapEdge, trackY, centerX - trailingGapEdge, trackThickness);
            track.resizeRelocate(centerX, trackY, trackEnd - centerX, trackThickness);
        } else if (leadingGapEdge > centerX) {
            leadingInactiveEnd = centerX;
            trailingInactiveStart = trailingGapEdge;
            secondaryTrack.resizeRelocate(trackStart, trackY, centerX - trackStart, trackThickness);
            activeTrack.resizeRelocate(centerX, trackY, leadingGapEdge - centerX, trackThickness);
            track.resizeRelocate(trailingInactiveStart, trackY, trackEnd - trailingInactiveStart, trackThickness);
        } else {
            leadingInactiveEnd = leadingGapEdge;
            trailingInactiveStart = trailingGapEdge;
            secondaryTrack.resizeRelocate(trackStart, trackY, leadingInactiveEnd - trackStart, trackThickness);
            activeTrack.resizeRelocate(centerX, trackY, 0.0, trackThickness);
            track.resizeRelocate(trailingInactiveStart, trackY, trackEnd - trailingInactiveStart, trackThickness);
        }
        layoutHorizontalEndStop(
                secondaryStopIndicator,
                true,
                trackStart,
                trackEnd,
                trackStart,
                leadingInactiveEnd,
                trackY,
                trackThickness
        );
        layoutHorizontalEndStop(
                stopIndicator,
                false,
                trackStart,
                trackEnd,
                trailingInactiveStart,
                trackEnd,
                trackY,
                trackThickness
        );
    }

    /// Positions one horizontal end stop when enough adjacent inactive track remains visible.
    private void layoutHorizontalEndStop(
            Region indicator,
            boolean leading,
            double trackStart,
            double trackEnd,
            double inactiveStart,
            double inactiveEnd,
            double trackY,
            double trackThickness
    ) {
        M3Slider slider = getSkinnable();
        double size = slider.getStopIndicatorSize();
        double radius = size / 2.0;
        double trailingSpace = slider.getStopIndicatorTrailingSpace();
        boolean visible = size > 0.0 && inactiveEnd - inactiveStart >= trailingSpace + size;
        indicator.setVisible(visible);
        if (visible) {
            double centerX = leading
                    ? trackStart + trailingSpace + radius
                    : trackEnd - trailingSpace - radius;
            indicator.resizeRelocate(
                    centerX - radius,
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

        if (getSkinnable().isCentered()) {
            layoutVerticalCenteredTrack(
                    trackX,
                    trackStart,
                    trackEnd,
                    trackThickness,
                    thumbCenterY,
                    thumbTrackGap
            );
        } else {
            double inactiveTrackEnd = layoutVerticalStandardTrack(
                    trackX,
                    trackStart,
                    trackEnd,
                    trackThickness,
                    thumbCenterY,
                    thumbTrackGap
            );
            layoutVerticalEndStop(
                    stopIndicator,
                    true,
                    trackX,
                    trackStart,
                    trackEnd,
                    trackStart,
                    inactiveTrackEnd,
                    trackThickness
            );
            secondaryStopIndicator.setVisible(false);
        }
        layoutStepIndicators(true, trackStart, trackEnd, trackX, trackThickness, thumbCenterY);
        layoutTrackGraphics(true, trackStart, trackEnd, trackX, trackThickness, thumbCenterY);
        stateLayer.layoutLayer(
                x + (width - getSkinnable().getTouchTargetSize()) / 2.0,
                thumbCenterY - getSkinnable().getTouchTargetSize() / 2.0,
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize(),
                getSkinnable().getTouchTargetSize() / 2.0
        );
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        stateLayer.layoutFocusIndicator(
                (touchTargetSize - thumbSize) / 2.0,
                (touchTargetSize - thumbWidth) / 2.0,
                thumbSize,
                thumbWidth,
                thumbWidth / 2.0
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbWidth);
    }

    /// Positions vertical active and inactive track segments around the handle gap.
    ///
    /// @return the trailing edge of the inactive track segment
    private double layoutVerticalStandardTrack(
            double trackX,
            double trackStart,
            double trackEnd,
            double trackThickness,
            double thumbCenterY,
            double thumbTrackGap
    ) {
        secondaryTrack.setVisible(false);
        double gapExtent = getSkinnable().getThumbWidth() / 2.0 + thumbTrackGap;
        double upperGapEdge = clampToRange(thumbCenterY - gapExtent, trackStart, trackEnd);
        double lowerGapEdge = clampToRange(thumbCenterY + gapExtent, trackStart, trackEnd);
        track.resizeRelocate(trackX, trackStart, trackThickness, upperGapEdge - trackStart);
        activeTrack.resizeRelocate(trackX, lowerGapEdge, trackThickness, trackEnd - lowerGapEdge);
        return upperGapEdge;
    }

    /// Positions centered vertical active and inactive track segments around the handle gap.
    private void layoutVerticalCenteredTrack(
            double trackX,
            double trackStart,
            double trackEnd,
            double trackThickness,
            double thumbCenterY,
            double thumbTrackGap
    ) {
        secondaryTrack.setVisible(true);
        double centerY = (trackStart + trackEnd) / 2.0;
        double gapExtent = getSkinnable().getThumbWidth() / 2.0 + thumbTrackGap;
        double upperGapEdge = clampToRange(thumbCenterY - gapExtent, trackStart, trackEnd);
        double lowerGapEdge = clampToRange(thumbCenterY + gapExtent, trackStart, trackEnd);
        double upperInactiveEnd;
        double lowerInactiveStart;
        if (lowerGapEdge < centerY) {
            upperInactiveEnd = upperGapEdge;
            lowerInactiveStart = centerY;
            track.resizeRelocate(trackX, trackStart, trackThickness, upperInactiveEnd - trackStart);
            activeTrack.resizeRelocate(trackX, lowerGapEdge, trackThickness, centerY - lowerGapEdge);
            secondaryTrack.resizeRelocate(trackX, centerY, trackThickness, trackEnd - centerY);
        } else if (upperGapEdge > centerY) {
            upperInactiveEnd = centerY;
            lowerInactiveStart = lowerGapEdge;
            track.resizeRelocate(trackX, trackStart, trackThickness, centerY - trackStart);
            activeTrack.resizeRelocate(trackX, centerY, trackThickness, upperGapEdge - centerY);
            secondaryTrack.resizeRelocate(trackX, lowerInactiveStart, trackThickness, trackEnd - lowerInactiveStart);
        } else {
            upperInactiveEnd = upperGapEdge;
            lowerInactiveStart = lowerGapEdge;
            track.resizeRelocate(trackX, trackStart, trackThickness, upperInactiveEnd - trackStart);
            activeTrack.resizeRelocate(trackX, centerY, trackThickness, 0.0);
            secondaryTrack.resizeRelocate(trackX, lowerInactiveStart, trackThickness, trackEnd - lowerInactiveStart);
        }
        layoutVerticalEndStop(
                stopIndicator,
                true,
                trackX,
                trackStart,
                trackEnd,
                trackStart,
                upperInactiveEnd,
                trackThickness
        );
        layoutVerticalEndStop(
                secondaryStopIndicator,
                false,
                trackX,
                trackStart,
                trackEnd,
                lowerInactiveStart,
                trackEnd,
                trackThickness
        );
    }

    /// Positions one vertical end stop when enough adjacent inactive track remains visible.
    private void layoutVerticalEndStop(
            Region indicator,
            boolean upper,
            double trackX,
            double trackStart,
            double trackEnd,
            double inactiveStart,
            double inactiveEnd,
            double trackThickness
    ) {
        M3Slider slider = getSkinnable();
        double size = slider.getStopIndicatorSize();
        double radius = size / 2.0;
        double trailingSpace = slider.getStopIndicatorTrailingSpace();
        boolean visible = size > 0.0 && inactiveEnd - inactiveStart >= trailingSpace + size;
        indicator.setVisible(visible);
        if (visible) {
            double centerY = upper
                    ? trackStart + trailingSpace + radius
                    : trackEnd - trailingSpace - radius;
            indicator.resizeRelocate(
                    trackX + (trackThickness - size) / 2.0,
                    centerY - radius,
                    size,
                    size
            );
        }
    }

    /// Positions optional icons inside the outer ends of standard slider track segments.
    private void layoutTrackGraphics(
            boolean vertical,
            double trackStart,
            double trackEnd,
            double trackCrossStart,
            double trackThickness,
            double thumbCenter
    ) {
        M3Slider slider = getSkinnable();
        double iconSize = slider.getIconSize();
        double iconPadding = slider.getIconPadding();
        if (slider.isCentered() || !(iconSize > 0.0)) {
            activeTrackGraphicSlot.setVisible(false);
            inactiveTrackGraphicSlot.setVisible(false);
            return;
        }

        double activeCenter = vertical
                ? trackEnd - iconPadding - iconSize / 2.0
                : trackStart + iconPadding + iconSize / 2.0;
        double inactiveCenter = vertical
                ? trackStart + iconPadding + iconSize / 2.0
                : trackEnd - iconPadding - iconSize / 2.0;
        double gapExtent = slider.getThumbWidth() / 2.0 + slider.getThumbTrackGap();
        double leadingGapEdge = thumbCenter - gapExtent;
        double trailingGapEdge = thumbCenter + gapExtent;
        boolean activeVisible = displayedActiveTrackGraphic != null && (vertical
                ? activeCenter - iconSize / 2.0 >= trailingGapEdge
                : activeCenter + iconSize / 2.0 <= leadingGapEdge);
        boolean inactiveVisible = !activeVisible && displayedInactiveTrackGraphic != null && (vertical
                ? inactiveCenter + iconSize / 2.0 <= leadingGapEdge
                : inactiveCenter - iconSize / 2.0 >= trailingGapEdge);

        activeTrackGraphicSlot.setVisible(activeVisible);
        inactiveTrackGraphicSlot.setVisible(inactiveVisible);
        if (vertical) {
            if (activeVisible) {
                activeTrackGraphicSlot.resizeRelocate(
                        trackCrossStart + (trackThickness - iconSize) / 2.0,
                        activeCenter - iconSize / 2.0,
                        iconSize,
                        iconSize
                );
            }
            if (inactiveVisible) {
                inactiveTrackGraphicSlot.resizeRelocate(
                        trackCrossStart + (trackThickness - iconSize) / 2.0,
                        inactiveCenter - iconSize / 2.0,
                        iconSize,
                        iconSize
                );
            }
        } else {
            if (activeVisible) {
                activeTrackGraphicSlot.resizeRelocate(
                        activeCenter - iconSize / 2.0,
                        trackCrossStart + (trackThickness - iconSize) / 2.0,
                        iconSize,
                        iconSize
                );
            }
            if (inactiveVisible) {
                inactiveTrackGraphicSlot.resizeRelocate(
                        inactiveCenter - iconSize / 2.0,
                        trackCrossStart + (trackThickness - iconSize) / 2.0,
                        iconSize,
                        iconSize
                );
            }
        }
    }

    /// Positions pooled stop indicators for selectable discrete values.
    private void layoutStepIndicators(
            boolean vertical,
            double trackStart,
            double trackEnd,
            double trackCrossStart,
            double trackThickness,
            double thumbCenter
    ) {
        M3Slider slider = getSkinnable();
        stepIndicatorLayer.resizeRelocate(0.0, 0.0, slider.getWidth(), slider.getHeight());
        double range = slider.getMax() - slider.getMin();
        double stepSize = slider.getStepSize();
        double indicatorSize = slider.getStopIndicatorSize();
        double trackLength = trackEnd - trackStart;
        if (!(range > 0.0) || !(stepSize > 0.0) || !(indicatorSize > 0.0) || !(trackLength > 0.0)) {
            hideUnusedStepIndicators(0);
            return;
        }

        double stepCountValue = Math.floor(range / stepSize + 1.0e-9);
        if (!Double.isFinite(stepCountValue) || stepCountValue > MAX_STEP_INDICATORS + 1.0) {
            hideUnusedStepIndicators(0);
            return;
        }
        int maximumStepIndex = (int) stepCountValue;
        double spacing = trackLength * stepSize / range;
        if (spacing < indicatorSize * 2.0) {
            hideUnusedStepIndicators(0);
            return;
        }

        double displayed = displayedPosition.get();
        double activeStart = slider.isCentered() ? Math.min(0.5, displayed) : 0.0;
        double activeEnd = slider.isCentered() ? Math.max(0.5, displayed) : displayed;
        double indicatorRadius = indicatorSize / 2.0;
        double thumbExclusionRadius = slider.getThumbWidth() / 2.0
                + slider.getThumbTrackGap()
                + indicatorRadius;
        int used = 0;
        for (int stepIndex = 0; stepIndex <= maximumStepIndex; stepIndex++) {
            double fraction = Math.min(1.0, stepIndex * stepSize / range);
            boolean atMinimum = stepIndex == 0;
            boolean atMaximum = Math.abs(1.0 - fraction) < 1.0e-9;
            if (atMaximum || slider.isCentered() && atMinimum) {
                continue;
            }

            double center = vertical
                    ? trackEnd - trackLength * fraction
                    : trackStart + trackLength * fraction;
            if (Math.abs(center - thumbCenter) <= thumbExclusionRadius) {
                continue;
            }

            Region indicator = stepIndicator(used++);
            indicator.pseudoClassStateChanged(
                    ACTIVE_PSEUDO_CLASS,
                    fraction >= activeStart && fraction <= activeEnd
            );
            if (vertical) {
                indicator.resizeRelocate(
                        trackCrossStart + (trackThickness - indicatorSize) / 2.0,
                        center - indicatorRadius,
                        indicatorSize,
                        indicatorSize
                );
            } else {
                indicator.resizeRelocate(
                        center - indicatorRadius,
                        trackCrossStart + (trackThickness - indicatorSize) / 2.0,
                        indicatorSize,
                        indicatorSize
                );
            }
            indicator.setVisible(true);
        }
        hideUnusedStepIndicators(used);
    }

    /// Returns one pooled discrete stop indicator, creating it when necessary.
    private Region stepIndicator(int index) {
        while (stepIndicatorLayer.getChildren().size() <= index) {
            Region indicator = new Region();
            indicator.getStyleClass().addAll("stop-indicator", "step-indicator");
            indicator.setManaged(false);
            indicator.setMouseTransparent(true);
            stepIndicatorLayer.getChildren().add(indicator);
        }
        return (Region) stepIndicatorLayer.getChildren().get(index);
    }

    /// Hides pooled discrete stop indicators that are not used by the current layout.
    private void hideUnusedStepIndicators(int used) {
        for (int index = used; index < stepIndicatorLayer.getChildren().size(); index++) {
            stepIndicatorLayer.getChildren().get(index).setVisible(false);
        }
    }

    /// Mounts the current active-track graphic and applies its Material color role.
    private void updateActiveTrackGraphic() {
        @Nullable Node graphic = getSkinnable().getActiveTrackGraphic();
        if (displayedActiveTrackGraphic == graphic) {
            return;
        }
        if (displayedActiveTrackGraphic != null && activeTrackGraphicStyleClassAdded) {
            displayedActiveTrackGraphic.getStyleClass().remove("m3-slider-active-track-graphic");
        }
        activeTrackGraphicSlot.getChildren().clear();
        displayedActiveTrackGraphic = graphic;
        activeTrackGraphicStyleClassAdded = false;
        if (graphic != null) {
            if (!graphic.getStyleClass().contains("m3-slider-active-track-graphic")) {
                graphic.getStyleClass().add("m3-slider-active-track-graphic");
                activeTrackGraphicStyleClassAdded = true;
            }
            activeTrackGraphicSlot.getChildren().add(graphic);
        }
        getSkinnable().requestLayout();
    }

    /// Mounts the current inactive-track graphic and applies its Material color role.
    private void updateInactiveTrackGraphic() {
        @Nullable Node graphic = getSkinnable().getInactiveTrackGraphic();
        if (displayedInactiveTrackGraphic == graphic) {
            return;
        }
        if (displayedInactiveTrackGraphic != null && inactiveTrackGraphicStyleClassAdded) {
            displayedInactiveTrackGraphic.getStyleClass().remove("m3-slider-inactive-track-graphic");
        }
        inactiveTrackGraphicSlot.getChildren().clear();
        displayedInactiveTrackGraphic = graphic;
        inactiveTrackGraphicStyleClassAdded = false;
        if (graphic != null) {
            if (!graphic.getStyleClass().contains("m3-slider-inactive-track-graphic")) {
                graphic.getStyleClass().add("m3-slider-inactive-track-graphic");
                inactiveTrackGraphicStyleClassAdded = true;
            }
            inactiveTrackGraphicSlot.getChildren().add(graphic);
        }
        getSkinnable().requestLayout();
    }

    /// Updates value-indicator content and transient visibility.
    private void updateValueIndicator() {
        M3Slider slider = getSkinnable();
        StringConverter<Double> formatter = slider.getLabelFormatter();
        if (formatter != null) {
            @Nullable String text = formatter.toString(slider.getValue());
            valueIndicator.setText(text == null ? "" : text);
        } else {
            double value = slider.getValue();
            valueIndicator.setText(
                    Math.rint(value) == value ? Long.toString((long) value) : Double.toString(value)
            );
        }
        valueIndicator.setVisible(
                slider.isShowValueIndicator()
                        && !slider.isDisabled()
                        && (slider.isValueChanging() || slider.isPressed())
        );
    }

    /// Positions a horizontal slider's value indicator above the handle.
    private void layoutHorizontalValueIndicator(
            double x,
            double y,
            double width,
            double indicatorReserve,
            double thumbWidth,
            double position
    ) {
        if (!getSkinnable().isShowValueIndicator()) {
            valueIndicator.setVisible(false);
            return;
        }
        double trackLength = Math.max(0.0, width - thumbWidth);
        double centerX = x + thumbWidth / 2.0 + trackLength * position;
        double indicatorWidth = Math.max(VALUE_INDICATOR_WIDTH, valueIndicator.prefWidth(VALUE_INDICATOR_HEIGHT));
        double indicatorX = clampToRange(
                centerX - indicatorWidth / 2.0,
                x,
                Math.max(x, x + width - indicatorWidth)
        );
        valueIndicator.resizeRelocate(
                indicatorX,
                y + Math.max(0.0, indicatorReserve - getSkinnable().getValueIndicatorBottomSpace()
                        - VALUE_INDICATOR_HEIGHT),
                indicatorWidth,
                VALUE_INDICATOR_HEIGHT
        );
    }

    /// Positions a vertical slider's value indicator beside the handle.
    private void layoutVerticalValueIndicator(
            double x,
            double y,
            double indicatorReserve,
            double height,
            double thumbWidth,
            double position
    ) {
        if (!getSkinnable().isShowValueIndicator()) {
            valueIndicator.setVisible(false);
            return;
        }
        double trackLength = Math.max(0.0, height - thumbWidth);
        double centerY = y + thumbWidth / 2.0 + trackLength * (1.0 - position);
        double indicatorWidth = Math.max(VALUE_INDICATOR_WIDTH, valueIndicator.prefWidth(VALUE_INDICATOR_HEIGHT));
        valueIndicator.resizeRelocate(
                x + Math.max(0.0, indicatorReserve - getSkinnable().getValueIndicatorBottomSpace()
                        - indicatorWidth),
                clampToRange(
                        centerY - VALUE_INDICATOR_HEIGHT / 2.0,
                        y,
                        Math.max(y, y + height - VALUE_INDICATOR_HEIGHT)
                ),
                indicatorWidth,
                VALUE_INDICATOR_HEIGHT
        );
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
                event.consume();
            }
            case END -> {
                slider.adjustValue(slider.getMax());
                event.consume();
            }
            case LEFT -> {
                if (isHorizontalRightToLeft()) {
                    slider.increment();
                } else {
                    slider.decrement();
                }
                event.consume();
            }
            case RIGHT -> {
                if (isHorizontalRightToLeft()) {
                    slider.decrement();
                } else {
                    slider.increment();
                }
                event.consume();
            }
            case DOWN -> {
                slider.decrement();
                event.consume();
            }
            case UP -> {
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
        double position = mousePositionToValuePosition(event.getX(), event.getY());
        getSkinnable().adjustValue(positionToValue(position));
    }

    /// Converts a local mouse coordinate to a normalized value position.
    private double mousePositionToValuePosition(double x, double y) {
        M3Slider slider = getSkinnable();
        double thumbWidth = slider.getThumbWidth();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            double length = Math.max(0.0, slider.getHeight() - thumbWidth);
            if (length == 0.0) {
                return 0.0;
            }
            double start = thumbWidth / 2.0;
            return clamp(1.0 - (y - start) / length);
        }

        double length = Math.max(0.0, slider.getWidth() - thumbWidth);
        if (length == 0.0) {
            return 0.0;
        }
        double start = thumbWidth / 2.0;
        double position = clamp((x - start) / length);
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
        updateValueIndicator();
    }

    /// Updates the displayed position with animation when the value is not being dragged.
    private void updateDisplayedPosition() {
        M3Slider slider = getSkinnable();
        double targetPosition = valueToPosition(slider.getValue());
        if (slider.isValueChanging()
                || slider.getScene() == null
                || !M3Animation.areAnimationsEnabled(slider)) {
            setDisplayedPositionImmediately(targetPosition);
            return;
        }

        M3MotionSpec spec = M3Animation.fastSpatial(slider);
        valueAnimation.configure(spec, targetPosition);
        M3Animation.playFromStart(slider, valueAnimation);
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
