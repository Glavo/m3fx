// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3RangeSliderSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 range slider for selecting two ordered values from one numeric range.
///
/// M3RangeSlider renders two independently focusable handles. The active track spans from [#getLowValue()] to
/// [#getHighValue()], while the track outside that interval remains inactive. Pointer, keyboard, and accessibility
/// changes are clamped to the configured range and preserve the invariant lowValue <= highValue. A positive
/// [#getStepSize()] turns the control into a discrete range slider and snaps both handles to values measured from
/// [#getMin()].
///
/// Range sliders are normally horizontal. Vertical orientation is supported for application-specific layouts, but
/// Material Design recommends a horizontal range slider because two vertically arranged values impose additional
/// cognitive load. Inset track icons are intentionally unsupported because the specification excludes them from
/// range sliders.
///
/// See [Material Design sliders](https://m3.material.io/components/sliders/overview).
@NotNullByDefault
public final class M3RangeSlider extends Control {
    /// The range-slider-specific style class.
    public static final String STYLE_CLASS = "m3-range-slider";

    /// The default Material slider size.
    private static final M3SliderSize DEFAULT_SIZE = M3SliderSize.EXTRA_SMALL;

    /// The default minimum value.
    private static final double DEFAULT_MIN = 0.0;

    /// The default maximum value.
    private static final double DEFAULT_MAX = 100.0;

    /// The default lower selected value.
    private static final double DEFAULT_LOW_VALUE = 0.0;

    /// The default upper selected value.
    private static final double DEFAULT_HIGH_VALUE = 100.0;

    /// The default block increment.
    private static final double DEFAULT_BLOCK_INCREMENT = 10.0;

    /// The default continuous step size.
    private static final double DEFAULT_STEP_SIZE = 0.0;

    /// The default slider track thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 16.0;

    /// The default slider track shape radius.
    private static final double DEFAULT_TRACK_SHAPE = 8.0;

    /// The default stop-indicator diameter.
    private static final double DEFAULT_STOP_INDICATOR_SIZE = 4.0;

    /// The default distance between an inactive-track outer edge and its stop indicator.
    private static final double DEFAULT_STOP_INDICATOR_TRAILING_SPACE = 4.0;

    /// The default handle long-side size.
    private static final double DEFAULT_THUMB_SIZE = 44.0;

    /// The default enabled handle short-side width.
    private static final double DEFAULT_THUMB_WIDTH = 4.0;

    /// The default focused handle short-side width.
    private static final double DEFAULT_FOCUSED_THUMB_WIDTH = 2.0;

    /// The default pressed handle short-side width.
    private static final double DEFAULT_PRESSED_THUMB_WIDTH = 2.0;

    /// The default gap between a handle and an adjacent track segment.
    private static final double DEFAULT_THUMB_TRACK_GAP = 6.0;

    /// The default touch-target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The default distance between a handle and its value indicator.
    private static final double DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE = 12.0;

    /// The minimum value property.
    private @Nullable DoubleProperty min;

    /// The maximum value property.
    private @Nullable DoubleProperty max;

    /// The lower selected value property.
    private @Nullable DoubleProperty lowValue;

    /// The upper selected value property.
    private @Nullable DoubleProperty highValue;

    /// The lower-handle direct-manipulation property.
    private @Nullable BooleanProperty lowValueChanging;

    /// The upper-handle direct-manipulation property.
    private @Nullable BooleanProperty highValueChanging;

    /// The orientation property.
    private @Nullable ObjectProperty<Orientation> orientation;

    /// The keyboard block-increment property.
    private @Nullable DoubleProperty blockIncrement;

    /// The discrete step-size property.
    private @Nullable DoubleProperty stepSize;

    /// The Material slider size property.
    private final ObjectProperty<M3SliderSize> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
        /// Updates the size style class after a value change.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(DEFAULT_SIZE);
                return;
            }
            updateSizeStyle();
            requestLayout();
        }
    };

    /// Whether the active handle displays a value indicator during direct manipulation.
    private @Nullable BooleanProperty showValueIndicator;

    /// The optional value-label formatter.
    private @Nullable ObjectProperty<@Nullable StringConverter<Double>> labelFormatter;

    /// The styleable track-thickness token.
    private @Nullable StyleableDoubleProperty trackThickness;

    /// The styleable track-shape token.
    private @Nullable StyleableDoubleProperty trackShape;

    /// The styleable stop-indicator-size token.
    private @Nullable StyleableDoubleProperty stopIndicatorSize;

    /// The styleable stop-indicator trailing-space token.
    private @Nullable StyleableDoubleProperty stopIndicatorTrailingSpace;

    /// The styleable handle long-side token.
    private @Nullable StyleableDoubleProperty thumbSize;

    /// The styleable enabled handle short-side token.
    private @Nullable StyleableDoubleProperty thumbWidth;

    /// The styleable focused handle short-side token.
    private @Nullable StyleableDoubleProperty focusedThumbWidth;

    /// The styleable pressed handle short-side token.
    private @Nullable StyleableDoubleProperty pressedThumbWidth;

    /// The styleable handle-to-track-gap token.
    private @Nullable StyleableDoubleProperty thumbTrackGap;

    /// The styleable touch-target token.
    private @Nullable StyleableDoubleProperty touchTargetSize;

    /// The styleable value-indicator spacing token.
    private @Nullable StyleableDoubleProperty valueIndicatorBottomSpace;

    /// Prevents recursive value normalization while a range update is in progress.
    private boolean normalizingValues;

    /// Creates a range slider with the default range from 0 through 100.
    public M3RangeSlider() {
        initialize();
    }

    /// Creates a range slider with explicit bounds and selected values.
    ///
    /// Values outside the supplied range are clamped. If the selected values are reversed after clamping,
    /// lowValue is reduced to highValue.
    ///
    /// @param min       the minimum selectable value
    /// @param max       the maximum selectable value
    /// @param lowValue  the initial lower selected value
    /// @param highValue the initial upper selected value
    public M3RangeSlider(double min, double max, double lowValue, double highValue) {
        initialize();
        setMin(min);
        setMax(max);
        setHighValue(highValue);
        setLowValue(lowValue);
    }

    /// Returns the minimum selectable value.
    ///
    /// @return the lower range bound
    public final double getMin() {
        return min == null ? DEFAULT_MIN : min.get();
    }

    /// Sets the minimum selectable value and clamps existing selected values.
    ///
    /// @param value the lower range bound
    public final void setMin(double value) {
        minProperty().set(value);
    }

    public final DoubleProperty minProperty() {
        if (min == null) {
            min = new DoublePropertyBase(DEFAULT_MIN) {
                /// Normalizes selected values after the lower bound changes.
                @Override
                protected void invalidated() {
                    normalizeSelectedValues();
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "min";
                }
            };
        }
        return min;
    }

    /// Returns the maximum selectable value.
    ///
    /// @return the upper range bound
    public final double getMax() {
        return max == null ? DEFAULT_MAX : max.get();
    }

    /// Sets the maximum selectable value and clamps existing selected values.
    ///
    /// @param value the upper range bound
    public final void setMax(double value) {
        maxProperty().set(value);
    }

    public final DoubleProperty maxProperty() {
        if (max == null) {
            max = new DoublePropertyBase(DEFAULT_MAX) {
                /// Normalizes selected values after the upper bound changes.
                @Override
                protected void invalidated() {
                    normalizeSelectedValues();
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "max";
                }
            };
        }
        return max;
    }

    /// Returns the lower selected value.
    ///
    /// @return the selected range start
    public final double getLowValue() {
        return lowValue == null ? DEFAULT_LOW_VALUE : lowValue.get();
    }

    /// Sets the lower selected value.
    ///
    /// The value is snapped when the slider is discrete, clamped to the configured bounds, and never allowed to
    /// exceed [#getHighValue()].
    ///
    /// @param value the selected range start
    public final void setLowValue(double value) {
        lowValueProperty().set(value);
    }

    public final DoubleProperty lowValueProperty() {
        if (lowValue == null) {
            lowValue = new DoublePropertyBase(DEFAULT_LOW_VALUE) {
                /// Preserves the selected-value ordering invariant.
                @Override
                protected void invalidated() {
                    if (normalizingValues) {
                        return;
                    }
                    double normalized = Math.min(normalizeValue(get()), getHighValue());
                    if (Double.compare(normalized, get()) != 0) {
                        set(normalized);
                        return;
                    }
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "lowValue";
                }
            };
        }
        return lowValue;
    }

    /// Returns the upper selected value.
    ///
    /// @return the selected range end
    public final double getHighValue() {
        return highValue == null ? DEFAULT_HIGH_VALUE : highValue.get();
    }

    /// Sets the upper selected value.
    ///
    /// The value is snapped when the slider is discrete, clamped to the configured bounds, and never allowed to
    /// fall below [#getLowValue()].
    ///
    /// @param value the selected range end
    public final void setHighValue(double value) {
        highValueProperty().set(value);
    }

    public final DoubleProperty highValueProperty() {
        if (highValue == null) {
            highValue = new DoublePropertyBase(DEFAULT_HIGH_VALUE) {
                /// Preserves the selected-value ordering invariant.
                @Override
                protected void invalidated() {
                    if (normalizingValues) {
                        return;
                    }
                    double normalized = Math.max(normalizeValue(get()), getLowValue());
                    if (Double.compare(normalized, get()) != 0) {
                        set(normalized);
                        return;
                    }
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "highValue";
                }
            };
        }
        return highValue;
    }

    /// Applies a lower value after clamping and discrete snapping.
    ///
    /// @param value the requested lower selected value
    public final void adjustLowValue(double value) {
        setLowValue(Math.min(normalizeValue(value), getHighValue()));
    }

    /// Applies an upper value after clamping and discrete snapping.
    ///
    /// @param value the requested upper selected value
    public final void adjustHighValue(double value) {
        setHighValue(Math.max(normalizeValue(value), getLowValue()));
    }

    /// Returns whether direct manipulation is changing the lower value.
    ///
    /// @return true while the lower handle is being dragged
    public final boolean isLowValueChanging() {
        return lowValueChanging != null && lowValueChanging.get();
    }

    /// Sets whether direct manipulation is changing the lower value.
    ///
    /// @param changing whether the lower value is changing
    public final void setLowValueChanging(boolean changing) {
        lowValueChangingProperty().set(changing);
    }

    public final BooleanProperty lowValueChangingProperty() {
        if (lowValueChanging == null) {
            lowValueChanging = changingProperty("lowValueChanging");
        }
        return lowValueChanging;
    }

    /// Returns whether direct manipulation is changing the upper value.
    ///
    /// @return true while the upper handle is being dragged
    public final boolean isHighValueChanging() {
        return highValueChanging != null && highValueChanging.get();
    }

    /// Sets whether direct manipulation is changing the upper value.
    ///
    /// @param changing whether the upper value is changing
    public final void setHighValueChanging(boolean changing) {
        highValueChangingProperty().set(changing);
    }

    public final BooleanProperty highValueChangingProperty() {
        if (highValueChanging == null) {
            highValueChanging = changingProperty("highValueChanging");
        }
        return highValueChanging;
    }

    /// Returns the slider orientation.
    ///
    /// @return the current orientation
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    /// Sets the slider orientation.
    ///
    /// Material Design recommends horizontal orientation for range sliders.
    ///
    /// @param value the new orientation
    /// @throws NullPointerException if any required argument is `null`
    public final void setOrientation(Orientation value) {
        orientationProperty().set(Objects.requireNonNull(value, "value"));
    }

    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new ObjectPropertyBase<>(Orientation.HORIZONTAL) {
                /// Restores the default orientation when necessary.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(Orientation.HORIZONTAL);
                        return;
                    }
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "orientation";
                }
            };
        }
        return orientation;
    }

    /// Returns the page-key and continuous arrow-key adjustment amount.
    ///
    /// @return the non-negative block increment
    public final double getBlockIncrement() {
        return blockIncrement == null ? DEFAULT_BLOCK_INCREMENT : blockIncrement.get();
    }

    /// Sets the block increment.
    ///
    /// @param value the non-negative adjustment amount
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setBlockIncrement(double value) {
        blockIncrementProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final DoubleProperty blockIncrementProperty() {
        if (blockIncrement == null) {
            blockIncrement = new DoublePropertyBase(DEFAULT_BLOCK_INCREMENT) {
                /// Rejects invalid bound values.
                @Override
                protected void invalidated() {
                    double normalized = M3Css.nonNegative(get(), "blockIncrement");
                    if (Double.compare(normalized, get()) != 0) {
                        set(normalized);
                    }
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "blockIncrement";
                }
            };
        }
        return blockIncrement;
    }

    /// Returns the discrete interval.
    ///
    /// @return a positive interval, or zero for continuous behavior
    public final double getStepSize() {
        return stepSize == null ? DEFAULT_STEP_SIZE : stepSize.get();
    }

    /// Sets the discrete interval.
    ///
    /// @param value the non-negative interval, where zero selects continuous behavior
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setStepSize(double value) {
        stepSizeProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final DoubleProperty stepSizeProperty() {
        if (stepSize == null) {
            stepSize = new DoublePropertyBase(DEFAULT_STEP_SIZE) {
                /// Re-snaps selected values after the interval changes.
                @Override
                protected void invalidated() {
                    double normalized = M3Css.nonNegative(get(), "stepSize");
                    if (Double.compare(normalized, get()) != 0) {
                        set(normalized);
                        return;
                    }
                    normalizeSelectedValues();
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "stepSize";
                }
            };
        }
        return stepSize;
    }

    /// Returns the Material slider size.
    ///
    /// @return the selected size preset
    public final M3SliderSize getSize() {
        return size.get();
    }

    /// Sets the Material slider size.
    ///
    /// @param value the size preset
    /// @throws NullPointerException if any required argument is `null`
    public final void setSize(M3SliderSize value) {
        size.set(Objects.requireNonNull(value, "value"));
    }

    public final ObjectProperty<M3SliderSize> sizeProperty() {
        return size;
    }

    /// Returns whether the active handle shows a value indicator during direct manipulation.
    ///
    /// @return true when value indicators are enabled
    public final boolean isShowValueIndicator() {
        return showValueIndicator != null && showValueIndicator.get();
    }

    /// Sets whether direct manipulation displays the active handle's value.
    ///
    /// Only one value indicator is displayed, even when the handles overlap.
    ///
    /// @param show whether to show the indicator
    public final void setShowValueIndicator(boolean show) {
        showValueIndicatorProperty().set(show);
    }

    public final BooleanProperty showValueIndicatorProperty() {
        if (showValueIndicator == null) {
            showValueIndicator = new BooleanPropertyBase(false) {
                /// Requests space for the optional indicator.
                @Override
                protected void invalidated() {
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "showValueIndicator";
                }
            };
        }
        return showValueIndicator;
    }

    /// Returns the formatter used for value indicators and accessible value strings.
    ///
    /// @return the formatter, or null for compact decimal formatting
    public final @Nullable StringConverter<Double> getLabelFormatter() {
        return labelFormatter == null ? null : labelFormatter.get();
    }

    /// Sets the formatter used for value indicators and accessible value strings.
    ///
    /// @param formatter the formatter, or null for compact decimal formatting
    public final void setLabelFormatter(@Nullable StringConverter<Double> formatter) {
        labelFormatterProperty().set(formatter);
    }

    public final ObjectProperty<@Nullable StringConverter<Double>> labelFormatterProperty() {
        if (labelFormatter == null) {
            labelFormatter = new ObjectPropertyBase<>() {
                /// Refreshes the current value representation.
                @Override
                protected void invalidated() {
                    requestLayout();
                }

                /// Returns the owning control.
                @Override
                public Object getBean() {
                    return M3RangeSlider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "labelFormatter";
                }
            };
        }
        return labelFormatter;
    }

    /// Returns the track thickness token.
    ///
    /// @return the track thickness in pixels
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the track thickness token.
    ///
    /// @param value the non-negative thickness in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setTrackThickness(double value) {
        trackThicknessProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty trackThicknessProperty() {
        if (trackThickness == null) {
            trackThickness = styleableMetric(
                    DEFAULT_TRACK_THICKNESS, "trackThickness", StyleableProperties.TRACK_THICKNESS
            );
        }
        return trackThickness;
    }

    /// Returns the track outer-corner radius token.
    ///
    /// @return the radius in pixels
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the track outer-corner radius token.
    ///
    /// @param value the non-negative radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setTrackShape(double value) {
        trackShapeProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty trackShapeProperty() {
        if (trackShape == null) {
            trackShape = styleableMetric(DEFAULT_TRACK_SHAPE, "trackShape", StyleableProperties.TRACK_SHAPE);
        }
        return trackShape;
    }

    /// Returns the stop-indicator diameter token.
    ///
    /// @return the diameter in pixels
    public final double getStopIndicatorSize() {
        return stopIndicatorSize == null ? DEFAULT_STOP_INDICATOR_SIZE : stopIndicatorSize.get();
    }

    /// Sets the stop-indicator diameter token.
    ///
    /// @param value the non-negative diameter in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setStopIndicatorSize(double value) {
        stopIndicatorSizeProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty stopIndicatorSizeProperty() {
        if (stopIndicatorSize == null) {
            stopIndicatorSize = styleableMetric(
                    DEFAULT_STOP_INDICATOR_SIZE, "stopIndicatorSize", StyleableProperties.STOP_INDICATOR_SIZE
            );
        }
        return stopIndicatorSize;
    }

    /// Returns the distance between an inactive track's outer edge and its stop indicator.
    ///
    /// The distance is measured from the outer track edge to the nearest indicator edge and does not vary with the
    /// selected slider size.
    ///
    /// @return the trailing space in pixels
    public final double getStopIndicatorTrailingSpace() {
        return stopIndicatorTrailingSpace == null
                ? DEFAULT_STOP_INDICATOR_TRAILING_SPACE
                : stopIndicatorTrailingSpace.get();
    }

    /// Sets the distance between an inactive track's outer edge and its stop indicator.
    ///
    /// @param value the non-negative trailing space in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setStopIndicatorTrailingSpace(double value) {
        stopIndicatorTrailingSpaceProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty stopIndicatorTrailingSpaceProperty() {
        if (stopIndicatorTrailingSpace == null) {
            stopIndicatorTrailingSpace = styleableMetric(
                    DEFAULT_STOP_INDICATOR_TRAILING_SPACE,
                    "stopIndicatorTrailingSpace",
                    StyleableProperties.STOP_INDICATOR_TRAILING_SPACE
            );
        }
        return stopIndicatorTrailingSpace;
    }

    /// Returns the handle long-side size token.
    ///
    /// @return the size in pixels
    public final double getThumbSize() {
        return thumbSize == null ? DEFAULT_THUMB_SIZE : thumbSize.get();
    }

    /// Sets the handle long-side size token.
    ///
    /// @param value the non-negative size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setThumbSize(double value) {
        thumbSizeProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty thumbSizeProperty() {
        if (thumbSize == null) {
            thumbSize = styleableMetric(DEFAULT_THUMB_SIZE, "thumbSize", StyleableProperties.THUMB_SIZE);
        }
        return thumbSize;
    }

    /// Returns the enabled handle short-side width token.
    ///
    /// @return the width in pixels
    public final double getThumbWidth() {
        return thumbWidth == null ? DEFAULT_THUMB_WIDTH : thumbWidth.get();
    }

    /// Sets the enabled handle short-side width token.
    ///
    /// @param value the non-negative width in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setThumbWidth(double value) {
        thumbWidthProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty thumbWidthProperty() {
        if (thumbWidth == null) {
            thumbWidth = styleableMetric(DEFAULT_THUMB_WIDTH, "thumbWidth", StyleableProperties.THUMB_WIDTH);
        }
        return thumbWidth;
    }

    /// Returns the keyboard-focused handle short-side width token.
    ///
    /// @return the focused width in pixels
    public final double getFocusedThumbWidth() {
        return focusedThumbWidth == null ? DEFAULT_FOCUSED_THUMB_WIDTH : focusedThumbWidth.get();
    }

    /// Sets the keyboard-focused handle short-side width token.
    ///
    /// @param value the non-negative width in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setFocusedThumbWidth(double value) {
        focusedThumbWidthProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty focusedThumbWidthProperty() {
        if (focusedThumbWidth == null) {
            focusedThumbWidth = styleableMetric(
                    DEFAULT_FOCUSED_THUMB_WIDTH, "focusedThumbWidth", StyleableProperties.FOCUSED_THUMB_WIDTH
            );
        }
        return focusedThumbWidth;
    }

    /// Returns the pressed handle short-side width token.
    ///
    /// @return the pressed width in pixels
    public final double getPressedThumbWidth() {
        return pressedThumbWidth == null ? DEFAULT_PRESSED_THUMB_WIDTH : pressedThumbWidth.get();
    }

    /// Sets the pressed handle short-side width token.
    ///
    /// @param value the non-negative width in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setPressedThumbWidth(double value) {
        pressedThumbWidthProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty pressedThumbWidthProperty() {
        if (pressedThumbWidth == null) {
            pressedThumbWidth = styleableMetric(
                    DEFAULT_PRESSED_THUMB_WIDTH, "pressedThumbWidth", StyleableProperties.PRESSED_THUMB_WIDTH
            );
        }
        return pressedThumbWidth;
    }

    /// Returns the gap between each handle and adjacent track segments.
    ///
    /// @return the gap in pixels
    public final double getThumbTrackGap() {
        return thumbTrackGap == null ? DEFAULT_THUMB_TRACK_GAP : thumbTrackGap.get();
    }

    /// Sets the handle-to-track gap token.
    ///
    /// @param value the non-negative gap in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setThumbTrackGap(double value) {
        thumbTrackGapProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty thumbTrackGapProperty() {
        if (thumbTrackGap == null) {
            thumbTrackGap = styleableMetric(
                    DEFAULT_THUMB_TRACK_GAP, "thumbTrackGap", StyleableProperties.THUMB_TRACK_GAP
            );
        }
        return thumbTrackGap;
    }

    /// Returns the preferred touch-target size.
    ///
    /// @return the touch-target size in pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch-target size.
    ///
    /// @param value the non-negative size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setTouchTargetSize(double value) {
        touchTargetSizeProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = styleableMetric(
                    DEFAULT_TOUCH_TARGET_SIZE, "touchTargetSize", StyleableProperties.TOUCH_TARGET_SIZE
            );
        }
        return touchTargetSize;
    }

    /// Returns the distance between an active handle and its value indicator.
    ///
    /// @return the spacing in pixels
    public final double getValueIndicatorBottomSpace() {
        return valueIndicatorBottomSpace == null
                ? DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE
                : valueIndicatorBottomSpace.get();
    }

    /// Sets the value-indicator spacing token.
    ///
    /// @param value the non-negative spacing in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setValueIndicatorBottomSpace(double value) {
        valueIndicatorBottomSpaceProperty().set(M3Css.nonNegative(value, "value"));
    }

    public final StyleableDoubleProperty valueIndicatorBottomSpaceProperty() {
        if (valueIndicatorBottomSpace == null) {
            valueIndicatorBottomSpace = styleableMetric(
                    DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE,
                    "valueIndicatorBottomSpace",
                    StyleableProperties.VALUE_INDICATOR_BOTTOM_SPACE
            );
        }
        return valueIndicatorBottomSpace;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default range-slider skin.
    ///
    /// @return a new Material range-slider skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3RangeSliderSkin(this);
    }

    /// Returns the shared slider user-agent stylesheet.
    ///
    /// @return the slider stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("slider.css");
    }

    /// Initializes style, accessibility, and sizing state.
    private void initialize() {
        M3ControlStyles.initialize(this, M3Slider.STYLE_CLASS);
        M3ControlStyles.add(this, STYLE_CLASS);
        updateSizeStyle();
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
    }

    /// Updates the style class associated with the current size preset.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                M3Slider.sizeStyleClass(getSize()),
                M3Slider.sizeStyleClass(M3SliderSize.EXTRA_SMALL),
                M3Slider.sizeStyleClass(M3SliderSize.SMALL),
                M3Slider.sizeStyleClass(M3SliderSize.MEDIUM),
                M3Slider.sizeStyleClass(M3SliderSize.LARGE),
                M3Slider.sizeStyleClass(M3SliderSize.EXTRA_LARGE)
        );
    }

    /// Creates a lazily allocated non-negative styleable metric.
    ///
    /// @param initialValue the fallback value
    /// @param name         the JavaFX property name
    /// @param metadata     the CSS metadata
    /// @return the new styleable property
    private StyleableDoubleProperty styleableMetric(
            double initialValue,
            String name,
            CssMetaData<? extends Styleable, Number> metadata
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                this,
                name,
                metadata,
                this::requestLayout
        );
    }

    /// Normalizes both selected values after a bound or step-size change.
    private void normalizeSelectedValues() {
        if (normalizingValues) {
            return;
        }
        normalizingValues = true;
        try {
            double normalizedLow = normalizeValue(getLowValue());
            double normalizedHigh = normalizeValue(getHighValue());
            if (normalizedLow > normalizedHigh) {
                normalizedLow = normalizedHigh;
            }
            lowValueProperty().set(normalizedLow);
            highValueProperty().set(normalizedHigh);
        } finally {
            normalizingValues = false;
        }
    }

    /// Clamps and, when configured, snaps one value to the current range.
    ///
    /// @param value the requested value
    /// @return the normalized value
    private double normalizeValue(double value) {
        double min = getMin();
        double max = getMax();
        if (Double.isNaN(value) || max < min) {
            return min;
        }
        double clamped = Math.max(min, Math.min(max, value));
        double step = getStepSize();
        if (!(step > 0.0) || !(max > min)) {
            return clamped;
        }
        return Math.max(min, Math.min(max, min + Math.rint((clamped - min) / step) * step));
    }

    /// Creates a direct-manipulation state property.
    ///
    /// @param name the property name
    /// @return the new property
    private BooleanProperty changingProperty(String name) {
        return new BooleanPropertyBase(false) {
            /// Returns the owning control.
            @Override
            public Object getBean() {
                return M3RangeSlider.this;
            }

            /// Returns the property name.
            @Override
            public String getName() {
                return name;
            }
        };
    }

    /// CSS metadata for range-slider component metrics.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for track thickness.
        private static final CssMetaData<M3RangeSlider, Number> TRACK_THICKNESS =
                metric("-m3-track-thickness", DEFAULT_TRACK_THICKNESS, M3RangeSlider::trackThicknessProperty);

        /// CSS metadata for track shape.
        private static final CssMetaData<M3RangeSlider, Number> TRACK_SHAPE =
                metric("-m3-track-shape", DEFAULT_TRACK_SHAPE, M3RangeSlider::trackShapeProperty);

        /// CSS metadata for stop-indicator size.
        private static final CssMetaData<M3RangeSlider, Number> STOP_INDICATOR_SIZE =
                metric(
                        "-m3-stop-indicator-size",
                        DEFAULT_STOP_INDICATOR_SIZE,
                        M3RangeSlider::stopIndicatorSizeProperty
                );

        /// CSS metadata for stop-indicator trailing space.
        private static final CssMetaData<M3RangeSlider, Number> STOP_INDICATOR_TRAILING_SPACE =
                metric(
                        "-m3-stop-indicator-trailing-space",
                        DEFAULT_STOP_INDICATOR_TRAILING_SPACE,
                        M3RangeSlider::stopIndicatorTrailingSpaceProperty
                );

        /// CSS metadata for handle size.
        private static final CssMetaData<M3RangeSlider, Number> THUMB_SIZE =
                metric("-m3-thumb-size", DEFAULT_THUMB_SIZE, M3RangeSlider::thumbSizeProperty);

        /// CSS metadata for enabled handle width.
        private static final CssMetaData<M3RangeSlider, Number> THUMB_WIDTH =
                metric("-m3-thumb-width", DEFAULT_THUMB_WIDTH, M3RangeSlider::thumbWidthProperty);

        /// CSS metadata for focused handle width.
        private static final CssMetaData<M3RangeSlider, Number> FOCUSED_THUMB_WIDTH =
                metric(
                        "-m3-focused-thumb-width",
                        DEFAULT_FOCUSED_THUMB_WIDTH,
                        M3RangeSlider::focusedThumbWidthProperty
                );

        /// CSS metadata for pressed handle width.
        private static final CssMetaData<M3RangeSlider, Number> PRESSED_THUMB_WIDTH =
                metric(
                        "-m3-pressed-thumb-width",
                        DEFAULT_PRESSED_THUMB_WIDTH,
                        M3RangeSlider::pressedThumbWidthProperty
                );

        /// CSS metadata for handle-to-track gap.
        private static final CssMetaData<M3RangeSlider, Number> THUMB_TRACK_GAP =
                metric("-m3-thumb-track-gap", DEFAULT_THUMB_TRACK_GAP, M3RangeSlider::thumbTrackGapProperty);

        /// CSS metadata for touch-target size.
        private static final CssMetaData<M3RangeSlider, Number> TOUCH_TARGET_SIZE =
                metric("-m3-touch-target-size", DEFAULT_TOUCH_TARGET_SIZE, M3RangeSlider::touchTargetSizeProperty);

        /// CSS metadata for value-indicator spacing.
        private static final CssMetaData<M3RangeSlider, Number> VALUE_INDICATOR_BOTTOM_SPACE =
                metric(
                        "-m3-value-indicator-bottom-space",
                        DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE,
                        M3RangeSlider::valueIndicatorBottomSpaceProperty
                );

        /// The immutable metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(TRACK_SHAPE);
            styleables.add(STOP_INDICATOR_SIZE);
            styleables.add(STOP_INDICATOR_TRAILING_SPACE);
            styleables.add(THUMB_SIZE);
            styleables.add(THUMB_WIDTH);
            styleables.add(FOCUSED_THUMB_WIDTH);
            styleables.add(PRESSED_THUMB_WIDTH);
            styleables.add(THUMB_TRACK_GAP);
            styleables.add(TOUCH_TARGET_SIZE);
            styleables.add(VALUE_INDICATOR_BOTTOM_SPACE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for one range-slider metric.
        ///
        /// @param property the CSS property name
        /// @param fallback the fallback value
        /// @param accessor the styleable-property accessor
        /// @return the CSS metadata
        private static CssMetaData<M3RangeSlider, Number> metric(
                String property,
                double fallback,
                java.util.function.Function<M3RangeSlider, StyleableDoubleProperty> accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), fallback) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3RangeSlider control) {
                    return M3Css.isSettable(accessor.apply(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3RangeSlider control) {
                    return accessor.apply(control);
                }
            };
        }

        /// Prevents utility-class instantiation.
        private StyleableProperties() {
        }
    }
}
