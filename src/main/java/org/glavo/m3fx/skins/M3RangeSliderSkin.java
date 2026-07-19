// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Transform;
import javafx.util.StringConverter;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3RangeSlider;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// The default skin for [M3RangeSlider].
///
/// The skin presents two independently focusable accessible slider handles with leading, active, and trailing track
/// sections. Keyboard, pointer, and accessibility actions update the corresponding low or high value while
/// preserving the control's ordered range; horizontal value geometry mirrors under right-to-left orientation.
@NotNullByDefault
public class M3RangeSliderSkin extends SkinBase<M3RangeSlider> {
    /// The pseudo-class used for stop indicators inside the selected range.
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    /// The focus-visible pseudo-class maintained by the shared state-layer tracker.
    private static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// The default preferred horizontal length.
    private static final double DEFAULT_HORIZONTAL_LENGTH = 240.0;

    /// The default preferred vertical length.
    private static final double DEFAULT_VERTICAL_LENGTH = 240.0;

    /// The minimum value-indicator width.
    private static final double VALUE_INDICATOR_WIDTH = 48.0;

    /// The value-indicator height.
    private static final double VALUE_INDICATOR_HEIGHT = 44.0;

    /// The maximum number of retained discrete stop nodes.
    private static final int MAX_STEP_INDICATORS = 256;

    /// The inactive track before the first visual handle.
    private final Region leadingTrack = new Region();

    /// The selected track between the two handles.
    private final Region activeTrack = new Region();

    /// The inactive track after the second visual handle.
    private final Region trailingTrack = new Region();

    /// The stop indicator at the first visual track edge.
    private final Region leadingStopIndicator = new Region();

    /// The stop indicator at the second visual track edge.
    private final Region trailingStopIndicator = new Region();

    /// The pooled discrete stop-indicator layer.
    private final Pane stepIndicatorLayer = new Pane();

    /// The lower-value handle.
    private final RangeThumb lowThumb = new RangeThumb(Thumb.LOW);

    /// The upper-value handle.
    private final RangeThumb highThumb = new RangeThumb(Thumb.HIGH);

    /// The single value indicator used by the actively manipulated handle.
    private final Label valueIndicator = new Label();

    /// The animated normalized lower-handle position.
    private final DoubleProperty displayedLowPosition =
            new SimpleDoubleProperty(this, "displayedLowPosition");

    /// The animated normalized upper-handle position.
    private final DoubleProperty displayedHighPosition =
            new SimpleDoubleProperty(this, "displayedHighPosition");

    /// The reusable lower-handle transition.
    private final M3DoubleTransition lowValueAnimation = new M3DoubleTransition(displayedLowPosition);

    /// The reusable upper-handle transition.
    private final M3DoubleTransition highValueAnimation = new M3DoubleTransition(displayedHighPosition);

    /// The handle currently owned by a pointer drag.
    private Thumb activeThumb = Thumb.NONE;

    /// The most recently selected handle, used to resolve exact overlap.
    private Thumb lastSelectedThumb = Thumb.HIGH;

    /// Whether the current pointer gesture has produced a drag event.
    private boolean pointerDragged;

    /// The latest content x-coordinate.
    private double layoutX;

    /// The latest content y-coordinate.
    private double layoutY;

    /// The latest content width.
    private double layoutWidth;

    /// The latest content height.
    private double layoutHeight;

    /// Whether direct geometry updates have valid content bounds.
    private boolean hasLayoutBounds;

    /// Handles primary pointer presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary pointer drags.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles primary pointer releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard adjustment for either focused handle.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Requests layout after metric or orientation changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Updates range geometry without invalidating the surrounding layout.
    private final InvalidationListener displayedPositionInvalidation = observable -> updateDisplayedGeometry();

    /// Updates track corner geometry after shape or orientation changes.
    private final InvalidationListener trackStyleInvalidation = observable -> {
        updateTrackStyles();
        getSkinnable().requestLayout();
    };

    /// Updates the lower displayed position and accessibility state.
    private final InvalidationListener lowValueInvalidation = observable -> {
        updateDisplayedPosition(Thumb.LOW);
        lowThumb.notifyValueChanged();
        highThumb.notifyRangeChanged();
        updateValueIndicator();
    };

    /// Updates the upper displayed position and accessibility state.
    private final InvalidationListener highValueInvalidation = observable -> {
        updateDisplayedPosition(Thumb.HIGH);
        highThumb.notifyValueChanged();
        lowThumb.notifyRangeChanged();
        updateValueIndicator();
    };

    /// Snaps both displayed positions after bounds change.
    private final InvalidationListener rangeInvalidation = observable -> {
        setDisplayedPositionImmediately(Thumb.LOW, valueToPosition(getSkinnable().getLowValue()));
        setDisplayedPositionImmediately(Thumb.HIGH, valueToPosition(getSkinnable().getHighValue()));
        lowThumb.notifyRangeChanged();
        highThumb.notifyRangeChanged();
    };

    /// Refreshes the value indicator and reserved space.
    private final InvalidationListener valueIndicatorInvalidation = observable -> {
        updateValueIndicator();
        getSkinnable().requestLayout();
    };

    /// Clears transient state when the control becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> resetDisabledInteractionState();

    /// Creates a range-slider skin.
    ///
    /// @param control the range slider controlled by this skin
    public M3RangeSliderSkin(M3RangeSlider control) {
        super(control);
        leadingTrack.getStyleClass().addAll("track", "range-leading-track");
        activeTrack.getStyleClass().addAll("active-track", "range-active-track");
        trailingTrack.getStyleClass().addAll("track", "range-trailing-track");
        leadingStopIndicator.getStyleClass().add("stop-indicator");
        trailingStopIndicator.getStyleClass().add("stop-indicator");
        valueIndicator.getStyleClass().addAll("m3-slider-value-indicator", "m3-label-large-text");

        leadingTrack.setMouseTransparent(true);
        activeTrack.setMouseTransparent(true);
        trailingTrack.setMouseTransparent(true);
        leadingStopIndicator.setMouseTransparent(true);
        trailingStopIndicator.setMouseTransparent(true);
        stepIndicatorLayer.setManaged(false);
        stepIndicatorLayer.setMouseTransparent(true);
        lowThumb.setManaged(false);
        highThumb.setManaged(false);
        valueIndicator.setManaged(false);
        valueIndicator.setMouseTransparent(true);

        getChildren().setAll(
                leadingTrack,
                activeTrack,
                trailingTrack,
                stepIndicatorLayer,
                leadingStopIndicator,
                trailingStopIndicator,
                lowThumb,
                highThumb,
                valueIndicator
        );

        displayedLowPosition.set(valueToPosition(control.getLowValue()));
        displayedHighPosition.set(valueToPosition(control.getHighValue()));
        displayedLowPosition.addListener(displayedPositionInvalidation);
        displayedHighPosition.addListener(displayedPositionInvalidation);

        control.lowValueProperty().addListener(lowValueInvalidation);
        control.highValueProperty().addListener(highValueInvalidation);
        control.minProperty().addListener(rangeInvalidation);
        control.maxProperty().addListener(rangeInvalidation);
        control.orientationProperty().addListener(trackStyleInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.trackShapeProperty().addListener(trackStyleInvalidation);
        control.stopIndicatorSizeProperty().addListener(layoutInvalidation);
        control.stopIndicatorTrailingSpaceProperty().addListener(layoutInvalidation);
        control.thumbSizeProperty().addListener(layoutInvalidation);
        control.thumbWidthProperty().addListener(layoutInvalidation);
        control.focusedThumbWidthProperty().addListener(layoutInvalidation);
        control.pressedThumbWidthProperty().addListener(layoutInvalidation);
        control.thumbTrackGapProperty().addListener(layoutInvalidation);
        control.touchTargetSizeProperty().addListener(layoutInvalidation);
        control.valueIndicatorBottomSpaceProperty().addListener(layoutInvalidation);
        control.showValueIndicatorProperty().addListener(valueIndicatorInvalidation);
        control.labelFormatterProperty().addListener(valueIndicatorInvalidation);
        control.lowValueChangingProperty().addListener(valueIndicatorInvalidation);
        control.highValueChangingProperty().addListener(valueIndicatorInvalidation);
        control.disabledProperty().addListener(disabledInvalidation);

        updateTrackStyles();
        updateValueIndicator();
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
    }

    /// Releases listeners, animations, focus trackers, and transient interaction state.
    @Override
    public void dispose() {
        M3RangeSlider control = getSkinnable();
        lowValueAnimation.stop();
        highValueAnimation.stop();
        displayedLowPosition.removeListener(displayedPositionInvalidation);
        displayedHighPosition.removeListener(displayedPositionInvalidation);
        control.lowValueProperty().removeListener(lowValueInvalidation);
        control.highValueProperty().removeListener(highValueInvalidation);
        control.minProperty().removeListener(rangeInvalidation);
        control.maxProperty().removeListener(rangeInvalidation);
        control.orientationProperty().removeListener(trackStyleInvalidation);
        control.trackThicknessProperty().removeListener(layoutInvalidation);
        control.trackShapeProperty().removeListener(trackStyleInvalidation);
        control.stopIndicatorSizeProperty().removeListener(layoutInvalidation);
        control.stopIndicatorTrailingSpaceProperty().removeListener(layoutInvalidation);
        control.thumbSizeProperty().removeListener(layoutInvalidation);
        control.thumbWidthProperty().removeListener(layoutInvalidation);
        control.focusedThumbWidthProperty().removeListener(layoutInvalidation);
        control.pressedThumbWidthProperty().removeListener(layoutInvalidation);
        control.thumbTrackGapProperty().removeListener(layoutInvalidation);
        control.touchTargetSizeProperty().removeListener(layoutInvalidation);
        control.valueIndicatorBottomSpaceProperty().removeListener(layoutInvalidation);
        control.showValueIndicatorProperty().removeListener(valueIndicatorInvalidation);
        control.labelFormatterProperty().removeListener(valueIndicatorInvalidation);
        control.lowValueChangingProperty().removeListener(valueIndicatorInvalidation);
        control.highValueChangingProperty().removeListener(valueIndicatorInvalidation);
        control.disabledProperty().removeListener(disabledInvalidation);
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        lowThumb.dispose();
        highThumb.dispose();
        activeThumb = Thumb.NONE;
        pointerDragged = false;
        control.setLowValueChanging(false);
        control.setHighValueChanging(false);
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width for the current orientation and indicator configuration.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3RangeSlider slider = getSkinnable();
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
        return leftInset + slider.getTouchTargetSize() * 2.0 + rightInset;
    }

    /// Computes the minimum height for the current orientation and indicator configuration.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3RangeSlider slider = getSkinnable();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            return topInset + slider.getTouchTargetSize() * 2.0 + bottomInset;
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

    /// Computes the preferred width.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
            return computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
        }
        return leftInset + DEFAULT_HORIZONTAL_LENGTH + rightInset;
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
        if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
            return topInset + DEFAULT_VERTICAL_LENGTH + bottomInset;
        }
        return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Caches content bounds and positions the complete range-slider node tree.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        layoutX = x;
        layoutY = y;
        layoutWidth = width;
        layoutHeight = height;
        hasLayoutBounds = true;
        updateDisplayedGeometry();
    }

    /// Updates all geometry from cached content bounds.
    private void updateDisplayedGeometry() {
        if (!hasLayoutBounds) {
            return;
        }

        M3RangeSlider slider = getSkinnable();
        double indicatorReserve = slider.isShowValueIndicator()
                ? slider.getValueIndicatorBottomSpace()
                + (slider.getOrientation() == Orientation.VERTICAL
                ? VALUE_INDICATOR_WIDTH : VALUE_INDICATOR_HEIGHT)
                : 0.0;
        if (slider.getOrientation() == Orientation.VERTICAL) {
            layoutVerticalSlider(
                    layoutX + indicatorReserve,
                    layoutY,
                    Math.max(0.0, layoutWidth - indicatorReserve),
                    layoutHeight
            );
        } else {
            layoutHorizontalSlider(
                    layoutX,
                    layoutY + indicatorReserve,
                    layoutWidth,
                    Math.max(0.0, layoutHeight - indicatorReserve)
            );
        }
        layoutValueIndicator(indicatorReserve);
    }

    /// Positions a horizontal range slider.
    private void layoutHorizontalSlider(double x, double y, double width, double height) {
        M3RangeSlider slider = getSkinnable();
        double baseThumbWidth = slider.getThumbWidth();
        double trackStart = x + baseThumbWidth / 2.0;
        double trackEnd = x + width - baseThumbWidth / 2.0;
        double trackLength = Math.max(0.0, trackEnd - trackStart);
        double lowPosition = displayedLowPosition.get();
        double highPosition = displayedHighPosition.get();
        double firstCenter = trackStart + trackLength * Math.min(lowPosition, highPosition);
        double secondCenter = trackStart + trackLength * Math.max(lowPosition, highPosition);
        double lowCenter = trackStart + trackLength * lowPosition;
        double highCenter = trackStart + trackLength * highPosition;
        double gap = slider.getThumbTrackGap();
        double lowShortSide = lowThumb.visualShortSide();
        double highShortSide = highThumb.visualShortSide();
        double firstGapExtent = lowShortSide / 2.0 + gap;
        double secondGapExtent = highShortSide / 2.0 + gap;
        double firstGapEdge = clampToRange(firstCenter - firstGapExtent, trackStart, trackEnd);
        double secondGapEdge = clampToRange(firstCenter + firstGapExtent, trackStart, trackEnd);
        double thirdGapEdge = clampToRange(secondCenter - secondGapExtent, trackStart, trackEnd);
        double fourthGapEdge = clampToRange(secondCenter + secondGapExtent, trackStart, trackEnd);
        double trackThickness = slider.getTrackThickness();
        double trackY = y + (height - trackThickness) / 2.0;

        leadingTrack.resizeRelocate(trackStart, trackY, Math.max(0.0, firstGapEdge - trackStart), trackThickness);
        activeTrack.resizeRelocate(
                secondGapEdge,
                trackY,
                Math.max(0.0, thirdGapEdge - secondGapEdge),
                trackThickness
        );
        trailingTrack.resizeRelocate(
                fourthGapEdge,
                trackY,
                Math.max(0.0, trackEnd - fourthGapEdge),
                trackThickness
        );
        layoutHorizontalEndStop(
                leadingStopIndicator,
                true,
                trackStart,
                trackEnd,
                firstGapEdge - trackStart,
                trackY,
                trackThickness
        );
        layoutHorizontalEndStop(
                trailingStopIndicator,
                false,
                trackStart,
                trackEnd,
                trackEnd - fourthGapEdge,
                trackY,
                trackThickness
        );
        layoutStepIndicators(
                false,
                trackStart,
                trackEnd,
                trackY,
                trackThickness,
                lowCenter,
                highCenter,
                lowShortSide,
                highShortSide
        );
        lowThumb.layoutAt(lowCenter, y + height / 2.0, false, lowShortSide);
        highThumb.layoutAt(highCenter, y + height / 2.0, false, highShortSide);
    }

    /// Positions a vertical range slider.
    private void layoutVerticalSlider(double x, double y, double width, double height) {
        M3RangeSlider slider = getSkinnable();
        double baseThumbWidth = slider.getThumbWidth();
        double trackStart = y + baseThumbWidth / 2.0;
        double trackEnd = y + height - baseThumbWidth / 2.0;
        double trackLength = Math.max(0.0, trackEnd - trackStart);
        double lowCenter = trackEnd - trackLength * displayedLowPosition.get();
        double highCenter = trackEnd - trackLength * displayedHighPosition.get();
        double firstCenter = Math.min(lowCenter, highCenter);
        double secondCenter = Math.max(lowCenter, highCenter);
        double gap = slider.getThumbTrackGap();
        double lowShortSide = lowThumb.visualShortSide();
        double highShortSide = highThumb.visualShortSide();
        double firstGapExtent = highShortSide / 2.0 + gap;
        double secondGapExtent = lowShortSide / 2.0 + gap;
        double firstGapEdge = clampToRange(firstCenter - firstGapExtent, trackStart, trackEnd);
        double secondGapEdge = clampToRange(firstCenter + firstGapExtent, trackStart, trackEnd);
        double thirdGapEdge = clampToRange(secondCenter - secondGapExtent, trackStart, trackEnd);
        double fourthGapEdge = clampToRange(secondCenter + secondGapExtent, trackStart, trackEnd);
        double trackThickness = slider.getTrackThickness();
        double trackX = x + (width - trackThickness) / 2.0;

        leadingTrack.resizeRelocate(trackX, trackStart, trackThickness, Math.max(0.0, firstGapEdge - trackStart));
        activeTrack.resizeRelocate(
                trackX,
                secondGapEdge,
                trackThickness,
                Math.max(0.0, thirdGapEdge - secondGapEdge)
        );
        trailingTrack.resizeRelocate(
                trackX,
                fourthGapEdge,
                trackThickness,
                Math.max(0.0, trackEnd - fourthGapEdge)
        );
        layoutVerticalEndStop(
                leadingStopIndicator,
                true,
                trackX,
                trackStart,
                trackEnd,
                firstGapEdge - trackStart,
                trackThickness
        );
        layoutVerticalEndStop(
                trailingStopIndicator,
                false,
                trackX,
                trackStart,
                trackEnd,
                trackEnd - fourthGapEdge,
                trackThickness
        );
        layoutStepIndicators(
                true,
                trackStart,
                trackEnd,
                trackX,
                trackThickness,
                lowCenter,
                highCenter,
                lowShortSide,
                highShortSide
        );
        lowThumb.layoutAt(x + width / 2.0, lowCenter, true, lowShortSide);
        highThumb.layoutAt(x + width / 2.0, highCenter, true, highShortSide);
    }

    /// Positions one horizontal end stop when its inactive track segment is long enough.
    private void layoutHorizontalEndStop(
            Region indicator,
            boolean leading,
            double trackStart,
            double trackEnd,
            double inactiveLength,
            double trackY,
            double trackThickness
    ) {
        M3RangeSlider slider = getSkinnable();
        double size = slider.getStopIndicatorSize();
        double radius = size / 2.0;
        double trailingSpace = slider.getStopIndicatorTrailingSpace();
        boolean visible = size > 0.0 && inactiveLength >= trailingSpace + size;
        indicator.setVisible(visible);
        if (visible) {
            double center = leading
                    ? trackStart + trailingSpace + radius
                    : trackEnd - trailingSpace - radius;
            indicator.resizeRelocate(center - radius, trackY + (trackThickness - size) / 2.0, size, size);
        }
    }

    /// Positions one vertical end stop when its inactive track segment is long enough.
    private void layoutVerticalEndStop(
            Region indicator,
            boolean leading,
            double trackX,
            double trackStart,
            double trackEnd,
            double inactiveLength,
            double trackThickness
    ) {
        M3RangeSlider slider = getSkinnable();
        double size = slider.getStopIndicatorSize();
        double radius = size / 2.0;
        double trailingSpace = slider.getStopIndicatorTrailingSpace();
        boolean visible = size > 0.0 && inactiveLength >= trailingSpace + size;
        indicator.setVisible(visible);
        if (visible) {
            double center = leading
                    ? trackStart + trailingSpace + radius
                    : trackEnd - trailingSpace - radius;
            indicator.resizeRelocate(trackX + (trackThickness - size) / 2.0, center - radius, size, size);
        }
    }

    /// Positions pooled stop indicators for discrete values.
    private void layoutStepIndicators(
            boolean vertical,
            double trackStart,
            double trackEnd,
            double trackCrossStart,
            double trackThickness,
            double lowCenter,
            double highCenter,
            double lowShortSide,
            double highShortSide
    ) {
        M3RangeSlider slider = getSkinnable();
        stepIndicatorLayer.resizeRelocate(0.0, 0.0, slider.getWidth(), slider.getHeight());
        double range = slider.getMax() - slider.getMin();
        double step = slider.getStepSize();
        double size = slider.getStopIndicatorSize();
        double trackLength = trackEnd - trackStart;
        if (!(range > 0.0) || !(step > 0.0) || !(size > 0.0) || !(trackLength > 0.0)) {
            hideUnusedStepIndicators(0);
            return;
        }

        double stepCountValue = Math.floor(range / step + 1.0e-9);
        if (!Double.isFinite(stepCountValue) || stepCountValue > MAX_STEP_INDICATORS + 1.0) {
            hideUnusedStepIndicators(0);
            return;
        }
        int maximumStepIndex = (int) stepCountValue;
        double spacing = trackLength * step / range;
        if (spacing < size * 2.0) {
            hideUnusedStepIndicators(0);
            return;
        }

        double radius = size / 2.0;
        double lowExclusion = lowShortSide / 2.0 + slider.getThumbTrackGap() + radius;
        double highExclusion = highShortSide / 2.0 + slider.getThumbTrackGap() + radius;
        int used = 0;
        for (int index = 1; index < maximumStepIndex; index++) {
            double fraction = Math.min(1.0, index * step / range);
            double center;
            if (vertical) {
                center = trackEnd - trackLength * fraction;
            } else {
                center = trackStart + trackLength * fraction;
            }
            if (Math.abs(center - lowCenter) <= lowExclusion
                    || Math.abs(center - highCenter) <= highExclusion) {
                continue;
            }

            Region indicator = stepIndicator(used++);
            double value = slider.getMin() + range * fraction;
            indicator.pseudoClassStateChanged(
                    ACTIVE_PSEUDO_CLASS,
                    value >= slider.getLowValue() && value <= slider.getHighValue()
            );
            if (vertical) {
                indicator.resizeRelocate(
                        trackCrossStart + (trackThickness - size) / 2.0,
                        center - radius,
                        size,
                        size
                );
            } else {
                indicator.resizeRelocate(
                        center - radius,
                        trackCrossStart + (trackThickness - size) / 2.0,
                        size,
                        size
                );
            }
            indicator.setVisible(true);
        }
        hideUnusedStepIndicators(used);
    }

    /// Returns one pooled discrete stop indicator.
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

    /// Hides retained stop nodes that are unused by the current layout.
    private void hideUnusedStepIndicators(int used) {
        for (int index = used; index < stepIndicatorLayer.getChildren().size(); index++) {
            stepIndicatorLayer.getChildren().get(index).setVisible(false);
        }
    }

    /// Applies logical outer corners and flat handle-adjacent corners to track segments.
    private void updateTrackStyles() {
        M3RangeSlider slider = getSkinnable();
        String shape = formatPixels(slider.getTrackShape());
        if (slider.getOrientation() == Orientation.VERTICAL) {
            leadingTrack.setStyle("-fx-background-radius: " + shape + " " + shape + " 0px 0px;");
            trailingTrack.setStyle("-fx-background-radius: 0px 0px " + shape + " " + shape + ";");
        } else {
            leadingTrack.setStyle("-fx-background-radius: " + shape + " 0px 0px " + shape + ";");
            trailingTrack.setStyle("-fx-background-radius: 0px " + shape + " " + shape + " 0px;");
        }
        activeTrack.setStyle("-fx-background-radius: 0px;");
    }

    /// Positions and updates the value indicator for the active handle.
    private void layoutValueIndicator(double indicatorReserve) {
        M3RangeSlider slider = getSkinnable();
        if (!slider.isShowValueIndicator() || activeThumb == Thumb.NONE) {
            valueIndicator.setVisible(false);
            return;
        }

        RangeThumb thumb = activeThumb == Thumb.LOW ? lowThumb : highThumb;
        double indicatorWidth = Math.max(VALUE_INDICATOR_WIDTH, valueIndicator.prefWidth(VALUE_INDICATOR_HEIGHT));
        if (slider.getOrientation() == Orientation.VERTICAL) {
            double centerY = thumb.getLayoutY() + thumb.getHeight() / 2.0;
            valueIndicator.resizeRelocate(
                    layoutX + Math.max(
                            0.0,
                            indicatorReserve - slider.getValueIndicatorBottomSpace() - indicatorWidth
                    ),
                    clampToRange(
                            centerY - VALUE_INDICATOR_HEIGHT / 2.0,
                            layoutY,
                            Math.max(layoutY, layoutY + layoutHeight - VALUE_INDICATOR_HEIGHT)
                    ),
                    indicatorWidth,
                    VALUE_INDICATOR_HEIGHT
            );
        } else {
            double centerX = thumb.getLayoutX() + thumb.getWidth() / 2.0;
            valueIndicator.resizeRelocate(
                    clampToRange(
                            centerX - indicatorWidth / 2.0,
                            layoutX,
                            Math.max(layoutX, layoutX + layoutWidth - indicatorWidth)
                    ),
                    layoutY + Math.max(
                            0.0,
                            indicatorReserve - slider.getValueIndicatorBottomSpace() - VALUE_INDICATOR_HEIGHT
                    ),
                    indicatorWidth,
                    VALUE_INDICATOR_HEIGHT
            );
        }
    }

    /// Refreshes value-indicator content and transient visibility.
    private void updateValueIndicator() {
        M3RangeSlider slider = getSkinnable();
        if (activeThumb == Thumb.NONE) {
            valueIndicator.setVisible(false);
            return;
        }
        double value = activeThumb == Thumb.LOW ? slider.getLowValue() : slider.getHighValue();
        StringConverter<Double> formatter = slider.getLabelFormatter();
        if (formatter != null) {
            @Nullable String text = formatter.toString(value);
            valueIndicator.setText(text == null ? "" : text);
        } else {
            valueIndicator.setText(formatValue(value));
        }
        valueIndicator.setVisible(
                slider.isShowValueIndicator()
                        && !slider.isDisabled()
                        && (slider.isLowValueChanging() || slider.isHighValueChanging())
        );
        if (hasLayoutBounds) {
            layoutValueIndicator(
                    slider.isShowValueIndicator()
                            ? slider.getValueIndicatorBottomSpace()
                            + (slider.getOrientation() == Orientation.VERTICAL
                            ? VALUE_INDICATOR_WIDTH : VALUE_INDICATOR_HEIGHT)
                            : 0.0
            );
        }
    }

    /// Selects and begins dragging the nearest handle.
    private void handleMousePressed(MouseEvent event) {
        M3RangeSlider slider = getSkinnable();
        if (slider.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        Thumb pressedThumb = Thumb.NONE;
        if (event.getTarget() instanceof Node target) {
            for (Node node = target; node != null && node != slider; node = node.getParent()) {
                if (node == lowThumb) {
                    pressedThumb = Thumb.LOW;
                    break;
                }
                if (node == highThumb) {
                    pressedThumb = Thumb.HIGH;
                    break;
                }
            }
        }
        double position = pressedThumb == Thumb.NONE
                ? mousePositionToValuePosition(event)
                : valueToPosition(valueForThumb(pressedThumb));
        activeThumb = pressedThumb == Thumb.NONE ? nearestThumb(position) : pressedThumb;
        pointerDragged = false;
        lastSelectedThumb = activeThumb;
        RangeThumb thumb = activeThumb == Thumb.LOW ? lowThumb : highThumb;
        M3FocusRequests.requestFocusIfTraversable(thumb);
        if (activeThumb == Thumb.LOW) {
            slider.setLowValueChanging(true);
        } else {
            slider.setHighValueChanging(true);
        }
        updateValueFromPosition(position);
        updateDisplayedGeometry();
        updateValueIndicator();
        event.consume();
    }

    /// Continues dragging the active handle.
    private void handleMouseDragged(MouseEvent event) {
        if (getSkinnable().isDisabled()
                || activeThumb == Thumb.NONE
                || !event.isPrimaryButtonDown()) {
            return;
        }
        pointerDragged = true;
        updateValueFromPosition(mousePositionToValuePosition(event));
        event.consume();
    }

    /// Commits the active handle after pointer release.
    private void handleMouseReleased(MouseEvent event) {
        M3RangeSlider slider = getSkinnable();
        if (activeThumb == Thumb.NONE || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (!slider.isDisabled() && pointerDragged) {
            updateValueFromPosition(mousePositionToValuePosition(event));
        }
        slider.setLowValueChanging(false);
        slider.setHighValueChanging(false);
        activeThumb = Thumb.NONE;
        pointerDragged = false;
        updateValueIndicator();
        event.consume();
    }

    /// Adjusts whichever internal handle owns keyboard focus.
    private void handleKeyPressed(KeyEvent event) {
        if (getSkinnable().isDisabled()) {
            return;
        }
        Thumb thumb = lowThumb.isFocused() ? Thumb.LOW : highThumb.isFocused() ? Thumb.HIGH : Thumb.NONE;
        if (thumb == Thumb.NONE) {
            return;
        }

        KeyCode code = event.getCode();
        double increment = unitIncrement();
        switch (code) {
            case HOME -> adjustThumb(thumb, getSkinnable().getMin());
            case END -> adjustThumb(thumb, getSkinnable().getMax());
            case LEFT -> adjustThumb(thumb, valueForThumb(thumb)
                    + (isHorizontalRightToLeft() ? increment : -increment));
            case RIGHT -> adjustThumb(thumb, valueForThumb(thumb)
                    + (isHorizontalRightToLeft() ? -increment : increment));
            case DOWN -> adjustThumb(thumb, valueForThumb(thumb) - increment);
            case UP -> adjustThumb(thumb, valueForThumb(thumb) + increment);
            case PAGE_DOWN -> adjustThumb(thumb, valueForThumb(thumb) - getSkinnable().getBlockIncrement());
            case PAGE_UP -> adjustThumb(thumb, valueForThumb(thumb) + getSkinnable().getBlockIncrement());
            default -> {
                return;
            }
        }
        lastSelectedThumb = thumb;
        event.consume();
    }

    /// Returns the nearest handle for a normalized pointer position.
    private Thumb nearestThumb(double position) {
        double lowPosition = valueToPosition(getSkinnable().getLowValue());
        double highPosition = valueToPosition(getSkinnable().getHighValue());
        double lowDistance = Math.abs(position - lowPosition);
        double highDistance = Math.abs(position - highPosition);
        if (Math.abs(lowDistance - highDistance) > 1.0e-9) {
            return lowDistance < highDistance ? Thumb.LOW : Thumb.HIGH;
        }
        if (Math.abs(lowPosition - highPosition) < 1.0e-9) {
            if (position < lowPosition) {
                return Thumb.LOW;
            }
            if (position > highPosition) {
                return Thumb.HIGH;
            }
            return lastSelectedThumb == Thumb.LOW ? Thumb.HIGH : Thumb.LOW;
        }
        return lastSelectedThumb;
    }

    /// Applies a normalized pointer position to the active handle.
    private void updateValueFromPosition(double position) {
        adjustThumb(activeThumb, positionToValue(position));
    }

    /// Adjusts one handle while preserving selected-value ordering.
    private void adjustThumb(Thumb thumb, double value) {
        M3RangeSlider slider = getSkinnable();
        if (thumb == Thumb.LOW) {
            slider.adjustLowValue(Math.min(value, slider.getHighValue()));
        } else if (thumb == Thumb.HIGH) {
            slider.adjustHighValue(Math.max(value, slider.getLowValue()));
        }
    }

    /// Returns the current value for one handle.
    private double valueForThumb(Thumb thumb) {
        return thumb == Thumb.LOW ? getSkinnable().getLowValue() : getSkinnable().getHighValue();
    }

    /// Converts a pointer event to a normalized numeric position in the range slider's coordinate system.
    private double mousePositionToValuePosition(MouseEvent event) {
        M3RangeSlider slider = getSkinnable();
        double x = event.getX();
        double y = event.getY();
        if (event.getSource() != slider) {
            Transform transform = slider.getLocalToSceneTransform();
            double determinant = transform.getMxx() * transform.getMyy()
                    - transform.getMxy() * transform.getMyx();
            if (Math.abs(determinant) > 1.0e-12) {
                double sceneX = event.getSceneX() - transform.getTx();
                double sceneY = event.getSceneY() - transform.getTy();
                x = (sceneX * transform.getMyy() - sceneY * transform.getMxy()) / determinant;
                y = (sceneY * transform.getMxx() - sceneX * transform.getMyx()) / determinant;
            }
        }
        double baseThumbWidth = slider.getThumbWidth();
        if (slider.getOrientation() == Orientation.VERTICAL) {
            double length = Math.max(0.0, layoutHeight - baseThumbWidth);
            if (length == 0.0) {
                return 0.0;
            }
            return clamp(1.0 - (y - layoutY - baseThumbWidth / 2.0) / length);
        }

        double length = Math.max(0.0, layoutWidth - baseThumbWidth);
        if (length == 0.0) {
            return 0.0;
        }
        double position = clamp((x - layoutX - baseThumbWidth / 2.0) / length);
        return isHorizontalRightToLeft() ? 1.0 - position : position;
    }

    /// Converts a numeric value to a normalized position.
    private double valueToPosition(double value) {
        M3RangeSlider slider = getSkinnable();
        double range = slider.getMax() - slider.getMin();
        if (!(range > 0.0)) {
            return 0.0;
        }
        return clamp((value - slider.getMin()) / range);
    }

    /// Converts a normalized position to a numeric value.
    private double positionToValue(double position) {
        M3RangeSlider slider = getSkinnable();
        return slider.getMin() + (slider.getMax() - slider.getMin()) * clamp(position);
    }

    /// Returns whether horizontal value geometry is mirrored.
    private boolean isHorizontalRightToLeft() {
        M3RangeSlider slider = getSkinnable();
        return slider.getOrientation() == Orientation.HORIZONTAL && M3NodeLayout.isRightToLeft(slider);
    }

    /// Returns the arrow-key increment.
    private double unitIncrement() {
        double step = getSkinnable().getStepSize();
        return step > 0.0 ? step : getSkinnable().getBlockIncrement();
    }

    /// Starts or snaps one displayed-position transition after a value change.
    private void updateDisplayedPosition(Thumb thumb) {
        M3RangeSlider slider = getSkinnable();
        double target = valueToPosition(valueForThumb(thumb));
        boolean changing = thumb == Thumb.LOW ? slider.isLowValueChanging() : slider.isHighValueChanging();
        if (changing || slider.getScene() == null || !M3Animation.areAnimationsEnabled(slider)) {
            setDisplayedPositionImmediately(thumb, target);
            return;
        }

        M3DoubleTransition transition = thumb == Thumb.LOW ? lowValueAnimation : highValueAnimation;
        M3MotionSpec spec = M3Animation.fastSpatial(slider);
        transition.configure(spec, target);
        M3Animation.playFromStart(slider, transition);
    }

    /// Sets one displayed position without animation.
    private void setDisplayedPositionImmediately(Thumb thumb, double position) {
        if (thumb == Thumb.LOW) {
            lowValueAnimation.stop();
            displayedLowPosition.set(position);
        } else {
            highValueAnimation.stop();
            displayedHighPosition.set(position);
        }
    }

    /// Clears interaction and animation state after disabling the control.
    private void resetDisabledInteractionState() {
        if (!getSkinnable().isDisabled()) {
            return;
        }
        lowValueAnimation.stop();
        highValueAnimation.stop();
        displayedLowPosition.set(valueToPosition(getSkinnable().getLowValue()));
        displayedHighPosition.set(valueToPosition(getSkinnable().getHighValue()));
        getSkinnable().setLowValueChanging(false);
        getSkinnable().setHighValueChanging(false);
        lowThumb.resetInteraction();
        highThumb.resetInteraction();
        activeThumb = Thumb.NONE;
        pointerDragged = false;
        updateValueIndicator();
    }

    /// Formats a value with the active label formatter.
    private String formatAccessibleValue(double value) {
        StringConverter<Double> formatter = getSkinnable().getLabelFormatter();
        if (formatter == null) {
            return formatValue(value);
        }
        @Nullable String text = formatter.toString(value);
        return text == null ? "" : text;
    }

    /// Formats a compact decimal value.
    private static String formatValue(double value) {
        return Math.rint(value) == value ? Long.toString((long) value) : Double.toString(value);
    }

    /// Clamps a normalized value.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Clamps a coordinate to a local range.
    private static double clampToRange(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        return Math.rint(value) == value ? (long) value + "px" : value + "px";
    }

    /// Identifies an internal range-slider handle.
    private enum Thumb {
        /// No active handle.
        NONE,

        /// The lower selected-value handle.
        LOW,

        /// The upper selected-value handle.
        HIGH
    }

    /// One independently focusable, accessible range-slider handle.
    @NotNullByDefault
    private final class RangeThumb extends Pane {
        /// The selected-value endpoint represented by this handle.
        private final Thumb thumb;

        /// The Material keyboard focus indicator behind the visual handle.
        private final M3StateLayer stateLayer = new M3StateLayer(false);

        /// The visible handle line.
        private final Region handle = new Region();

        /// Requests geometry updates after focus or pressed-state changes.
        private final InvalidationListener geometryInvalidation = observable -> getSkinnable().requestLayout();

        /// Requests geometry updates when focus-visible state changes.
        private final SetChangeListener<PseudoClass> pseudoClassListener = change -> {
            if (FOCUS_VISIBLE_PSEUDO_CLASS.equals(change.getElementAdded())
                    || FOCUS_VISIBLE_PSEUDO_CLASS.equals(change.getElementRemoved())) {
                updateDisplayedGeometry();
            }
        };

        /// Creates one range-slider handle.
        ///
        /// @param thumb the represented selected-value endpoint
        private RangeThumb(Thumb thumb) {
            this.thumb = thumb;
            getStyleClass().addAll(
                    "range-thumb",
                    thumb == Thumb.LOW ? "range-low-thumb" : "range-high-thumb"
            );
            handle.getStyleClass().add("thumb");
            handle.setManaged(false);
            handle.setMouseTransparent(true);
            stateLayer.setManaged(false);
            stateLayer.setMouseTransparent(true);
            getChildren().addAll(stateLayer, handle);
            setFocusTraversable(true);
            setPickOnBounds(true);
            setAccessibleRole(AccessibleRole.SLIDER);
            setAccessibleText(thumb == Thumb.LOW ? "Minimum value" : "Maximum value");
            stateLayer.installStateTransitions(this);
            getPseudoClassStates().addListener(pseudoClassListener);
            focusedProperty().addListener(geometryInvalidation);
            pressedProperty().addListener(geometryInvalidation);
        }

        /// Returns the handle short side for its current pressed and focus-visible state.
        private double visualShortSide() {
            M3RangeSlider slider = getSkinnable();
            boolean pointerActive = activeThumb == thumb
                    && (slider.isLowValueChanging() || slider.isHighValueChanging());
            if (pointerActive || isPressed()) {
                return slider.getPressedThumbWidth();
            }
            if (getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS)) {
                return slider.getFocusedThumbWidth();
            }
            return slider.getThumbWidth();
        }

        /// Positions the touch target, state layer, and visible handle around one value coordinate.
        private void layoutAt(double centerX, double centerY, boolean vertical, double shortSide) {
            M3RangeSlider slider = getSkinnable();
            double touchSize = slider.getTouchTargetSize();
            double paneSize = Math.max(touchSize, slider.getThumbSize());
            resizeRelocate(centerX - paneSize / 2.0, centerY - paneSize / 2.0, paneSize, paneSize);
            double touchOffset = (paneSize - touchSize) / 2.0;
            stateLayer.layoutLayer(touchOffset, touchOffset, touchSize, touchSize, touchSize / 2.0);

            if (vertical) {
                stateLayer.layoutFocusIndicator(
                        (touchSize - slider.getThumbSize()) / 2.0,
                        (touchSize - shortSide) / 2.0,
                        slider.getThumbSize(),
                        shortSide,
                        shortSide / 2.0
                );
                handle.resizeRelocate(
                        (paneSize - slider.getThumbSize()) / 2.0,
                        (paneSize - shortSide) / 2.0,
                        slider.getThumbSize(),
                        shortSide
                );
            } else {
                stateLayer.layoutFocusIndicator(
                        (touchSize - shortSide) / 2.0,
                        (touchSize - slider.getThumbSize()) / 2.0,
                        shortSide,
                        slider.getThumbSize(),
                        shortSide / 2.0
                );
                handle.resizeRelocate(
                        (paneSize - shortSide) / 2.0,
                        (paneSize - slider.getThumbSize()) / 2.0,
                        shortSide,
                        slider.getThumbSize()
                );
            }
            setViewOrder(activeThumb == thumb || isFocused() ? -1.0 : 0.0);
        }

        /// Clears state-layer feedback.
        private void resetInteraction() {
            stateLayer.reset();
            updateDisplayedGeometry();
        }

        /// Notifies assistive technology that this handle's value changed.
        private void notifyValueChanged() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
            M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
        }

        /// Notifies assistive technology that this handle's constrained range changed.
        private void notifyRangeChanged() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.MIN_VALUE);
            notifyAccessibleAttributeChanged(AccessibleAttribute.MAX_VALUE);
        }

        /// Returns accessibility attributes for this selected-value endpoint.
        @Override
        public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            Objects.requireNonNull(attribute, "attribute");
            if (attribute == VALUE_STRING_ATTRIBUTE) {
                return formatAccessibleValue(valueForThumb(thumb));
            }
            M3RangeSlider slider = getSkinnable();
            return switch (attribute) {
                case MIN_VALUE -> thumb == Thumb.LOW ? slider.getMin() : slider.getLowValue();
                case MAX_VALUE -> thumb == Thumb.LOW ? slider.getHighValue() : slider.getMax();
                case VALUE -> valueForThumb(thumb);
                case ORIENTATION -> slider.getOrientation();
                default -> super.queryAccessibleAttribute(attribute, parameters);
            };
        }

        /// Executes accessibility actions for this selected-value endpoint.
        @Override
        public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
            Objects.requireNonNull(action, "action");
            if (getSkinnable().isDisabled()) {
                super.executeAccessibleAction(action, parameters);
                return;
            }
            switch (action) {
                case REQUEST_FOCUS -> requestFocus();
                case INCREMENT -> adjustThumb(thumb, valueForThumb(thumb) + unitIncrement());
                case DECREMENT -> adjustThumb(thumb, valueForThumb(thumb) - unitIncrement());
                case BLOCK_INCREMENT -> adjustThumb(thumb, valueForThumb(thumb) + getSkinnable().getBlockIncrement());
                case BLOCK_DECREMENT -> adjustThumb(thumb, valueForThumb(thumb) - getSkinnable().getBlockIncrement());
                case SET_VALUE -> setAccessibleValue(parameters);
                default -> super.executeAccessibleAction(action, parameters);
            }
        }

        /// Applies the first numeric value supplied by an accessibility client.
        private void setAccessibleValue(Object... parameters) {
            Objects.requireNonNull(parameters, "parameters");
            for (Object parameter : parameters) {
                if (parameter instanceof Number number) {
                    adjustThumb(thumb, number.doubleValue());
                    return;
                }
            }
        }

        /// Releases focus-state tracking and state-layer animations.
        private void dispose() {
            focusedProperty().removeListener(geometryInvalidation);
            pressedProperty().removeListener(geometryInvalidation);
            getPseudoClassStates().removeListener(pseudoClassListener);
            stateLayer.uninstallStateTransitions();
            stateLayer.reset();
        }
    }
}
