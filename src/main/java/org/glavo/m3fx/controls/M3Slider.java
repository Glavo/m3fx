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
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SliderSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 slider for selecting a numeric value from a continuous range.
///
/// `M3Slider` exposes JavaFX-style `min`, `max`, `value`, orientation, and block-increment properties while
/// rendering a Material track, active range, end stop indicators, thumb, focus state, and keyboard-accessible
/// value changes. The `centered` property starts the active track at the range midpoint for positive and negative
/// values. The `stepSize` property turns the control into a discrete slider whose selectable values are rendered as
/// stop indicators and whose pointer, keyboard, programmatic, and accessibility value changes snap to valid steps.
/// The `valueChanging` property is set during direct pointer interaction so applications can distinguish committed
/// changes from in-progress drags.
///
/// Use sliders for approximate or relative numeric choices. See
/// [Material Design sliders](https://m3.material.io/components/sliders/overview).
@NotNullByDefault
public final class M3Slider extends Control {
    /// The base style class for M3FX sliders.
    public static final String STYLE_CLASS = "m3-slider";

    /// The pseudo-class applied while the slider uses a centered active track.
    private static final PseudoClass CENTERED_PSEUDO_CLASS = PseudoClass.getPseudoClass("centered");

    /// The default Material slider size.
    private static final M3SliderSize DEFAULT_SIZE = M3SliderSize.EXTRA_SMALL;

    /// The default minimum slider value.
    private static final double DEFAULT_MIN = 0.0;

    /// The default maximum slider value.
    private static final double DEFAULT_MAX = 100.0;

    /// The default slider value.
    private static final double DEFAULT_VALUE = 0.0;

    /// The default block increment used for page navigation and continuous single-step adjustments.
    private static final double DEFAULT_BLOCK_INCREMENT = 10.0;

    /// The default step size for continuous sliders.
    private static final double DEFAULT_STEP_SIZE = 0.0;

    /// The default slider track thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 16.0;

    /// The default slider track shape radius.
    private static final double DEFAULT_TRACK_SHAPE = 999.0;

    /// The default slider stop indicator diameter.
    private static final double DEFAULT_STOP_INDICATOR_SIZE = 4.0;

    /// The default distance between the inactive-track outer edge and the stop indicator.
    private static final double DEFAULT_STOP_INDICATOR_TRAILING_SPACE = 4.0;

    /// The default slider handle long-side size.
    private static final double DEFAULT_THUMB_SIZE = 44.0;

    /// The default slider handle short-side width.
    private static final double DEFAULT_THUMB_WIDTH = 4.0;

    /// The default gap between the handle and each adjacent track segment.
    private static final double DEFAULT_THUMB_TRACK_GAP = 6.0;

    /// The default slider touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The default inset-icon size for the extra-small slider.
    private static final double DEFAULT_ICON_SIZE = 0.0;

    /// The default inset-icon padding for the extra-small slider.
    private static final double DEFAULT_ICON_PADDING = 0.0;

    /// The default distance between the handle and the value indicator.
    private static final double DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE = 12.0;

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    // Backing property for the public minimum value API.
    private @Nullable DoubleProperty min;

    // Backing property for the public maximum value API.
    private @Nullable DoubleProperty max;

    // Backing property for the public current value API.
    private @Nullable DoubleProperty value;

    // Backing property for the public orientation API.
    private @Nullable ObjectProperty<Orientation> orientation;

    // Backing property for the public value-changing API.
    private @Nullable BooleanProperty valueChanging;

    // Backing property for the public block increment API.
    private @Nullable DoubleProperty blockIncrement;

    // Backing property for the public discrete step API.
    private @Nullable DoubleProperty stepSize;

    // Backing property for the public centered-track API.
    private @Nullable BooleanProperty centered;

    // The Material slider size property.
    private final ObjectProperty<M3SliderSize> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
        /// Updates the component token style class when the size changes.
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

    // The optional graphic inset into the active track.
    private @Nullable ObjectProperty<@Nullable Node> activeTrackGraphic;

    // The optional graphic inset into the inactive track.
    private @Nullable ObjectProperty<@Nullable Node> inactiveTrackGraphic;

    // Whether the value indicator is displayed during direct manipulation.
    private @Nullable BooleanProperty showValueIndicator;

    // The optional formatter used by the value indicator and accessibility value string.
    private @Nullable ObjectProperty<@Nullable StringConverter<Double>> labelFormatter;

    // Backing property for the public track thickness token API.
    private @Nullable StyleableDoubleProperty trackThickness;

    // Backing property for the public track shape token API.
    private @Nullable StyleableDoubleProperty trackShape;

    // Backing property for the public stop indicator size token API.
    private @Nullable StyleableDoubleProperty stopIndicatorSize;

    // Backing property for the public stop indicator trailing-space token API.
    private @Nullable StyleableDoubleProperty stopIndicatorTrailingSpace;

    // Backing property for the public thumb size token API.
    private @Nullable StyleableDoubleProperty thumbSize;

    // Backing property for the public thumb width token API.
    private @Nullable StyleableDoubleProperty thumbWidth;

    // Backing property for the public thumb track-gap token API.
    private @Nullable StyleableDoubleProperty thumbTrackGap;

    // Backing property for the public touch target size token API.
    private @Nullable StyleableDoubleProperty touchTargetSize;

    // The styleable inset-icon size token.
    private @Nullable StyleableDoubleProperty iconSize;

    // The styleable inset-icon outer padding token.
    private @Nullable StyleableDoubleProperty iconPadding;

    // The styleable value-indicator bottom-space token.
    private @Nullable StyleableDoubleProperty valueIndicatorBottomSpace;

    /// Creates a slider with the JavaFX default range.
    public M3Slider() {
        initialize();
    }

    /// Creates a slider with a range and initial value.
    ///
    /// @param min   the minimum slider value
    /// @param max   the maximum slider value
    /// @param value the initial slider value
    public M3Slider(double min, double max, double value) {
        initialize();
        setMin(min);
        setMax(max);
        setValue(value);
    }

    /// Returns the Material slider size.
    ///
    /// The size selects the published track height, track shape, handle height, and inset-icon metrics. It does not
    /// affect the numeric range, step size, orientation, or current value.
    ///
    /// @return the current Material slider size
    public final M3SliderSize getSize() {
        return size.get();
    }

    /// Sets the Material slider size.
    ///
    /// Explicitly selecting a size applies that size's Material component tokens. Applications may still override
    /// individual styleable metric properties after CSS resolution when a custom configuration is required.
    ///
    /// @param size the Material slider size
    public final void setSize(M3SliderSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the Material slider size property.
    ///
    /// @return the writable slider size property
    public final ObjectProperty<M3SliderSize> sizeProperty() {
        return size;
    }

    /// Returns the graphic inset into the active track.
    ///
    /// The graphic is rendered only when the active track is large enough for the current slider size's icon and
    /// padding tokens. When it does not fit, the inactive-track graphic is used instead. The two graphics are
    /// mutually exclusive visual variants of one inset icon; they are not rendered at the same time. Extra-small
    /// and small sliders define no inset-icon slot. A non-null graphic must not already be attached to another
    /// parent in the scene graph.
    ///
    /// @return the active-track graphic, or `null` when none is configured
    public final @Nullable Node getActiveTrackGraphic() {
        return activeTrackGraphic == null ? null : activeTrackGraphic.get();
    }

    /// Sets the graphic inset into the active track.
    ///
    /// @param graphic the active-track graphic, or `null` to remove it
    public final void setActiveTrackGraphic(@Nullable Node graphic) {
        activeTrackGraphicProperty().set(graphic);
    }

    /// Returns the active-track graphic property.
    ///
    /// @return the writable active-track graphic property
    public final ObjectProperty<@Nullable Node> activeTrackGraphicProperty() {
        if (activeTrackGraphic == null) {
            activeTrackGraphic = new ObjectPropertyBase<>() {
                /// Requests layout when the mounted graphic changes.
                @Override
                protected void invalidated() {
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "activeTrackGraphic";
                }
            };
        }
        return activeTrackGraphic;
    }

    /// Returns the graphic inset into the inactive track.
    ///
    /// This graphic is the fallback visual variant shown when the active segment cannot contain the active-track
    /// graphic. It follows the maximum-value outer edge and is hidden whenever the active-track graphic fits, when
    /// the current size does not define an inset-icon slot, or when the inactive segment cannot contain it.
    ///
    /// @return the inactive-track graphic, or `null` when none is configured
    public final @Nullable Node getInactiveTrackGraphic() {
        return inactiveTrackGraphic == null ? null : inactiveTrackGraphic.get();
    }

    /// Sets the graphic inset into the inactive track.
    ///
    /// @param graphic the inactive-track graphic, or `null` to remove it
    public final void setInactiveTrackGraphic(@Nullable Node graphic) {
        inactiveTrackGraphicProperty().set(graphic);
    }

    /// Returns the inactive-track graphic property.
    ///
    /// @return the writable inactive-track graphic property
    public final ObjectProperty<@Nullable Node> inactiveTrackGraphicProperty() {
        if (inactiveTrackGraphic == null) {
            inactiveTrackGraphic = new ObjectPropertyBase<>() {
                /// Requests layout when the mounted graphic changes.
                @Override
                protected void invalidated() {
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "inactiveTrackGraphic";
                }
            };
        }
        return inactiveTrackGraphic;
    }

    /// Returns whether a value indicator is shown during direct pointer manipulation.
    ///
    /// @return `true` when the slider reserves and displays a value indicator
    public final boolean isShowValueIndicator() {
        return showValueIndicator != null && showValueIndicator.get();
    }

    /// Sets whether a value indicator is shown during direct pointer manipulation.
    ///
    /// Enabling the indicator reserves space on the logical top side of a horizontal slider or beside a vertical
    /// slider so that showing the indicator does not move surrounding content.
    ///
    /// @param showValueIndicator whether the value indicator is available
    public final void setShowValueIndicator(boolean showValueIndicator) {
        showValueIndicatorProperty().set(showValueIndicator);
    }

    /// Returns the value-indicator visibility property.
    ///
    /// @return the writable value-indicator visibility property
    public final BooleanProperty showValueIndicatorProperty() {
        if (showValueIndicator == null) {
            showValueIndicator = new BooleanPropertyBase(false) {
                /// Updates the reserved indicator space.
                @Override
                protected void invalidated() {
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Returns the formatter used for the value indicator and accessible value string.
    ///
    /// A null formatter uses a compact decimal representation that preserves fractional values without adding
    /// locale-specific grouping.
    ///
    /// @return the value formatter, or `null` to use the default representation
    public final @Nullable StringConverter<Double> getLabelFormatter() {
        return labelFormatter == null ? null : labelFormatter.get();
    }

    /// Sets the formatter used for the value indicator and accessible value string.
    ///
    /// @param formatter the formatter, or `null` to use the default representation
    public final void setLabelFormatter(@Nullable StringConverter<Double> formatter) {
        labelFormatterProperty().set(formatter);
    }

    /// Returns the value formatter property.
    ///
    /// @return the writable value formatter property
    public final ObjectProperty<@Nullable StringConverter<Double>> labelFormatterProperty() {
        if (labelFormatter == null) {
            labelFormatter = new ObjectPropertyBase<>() {
                /// Refreshes the visual and accessible value representation.
                @Override
                protected void invalidated() {
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3Slider.this, VALUE_STRING_ATTRIBUTE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Returns the minimum slider value.
    ///
    /// @return the minimum slider value
    public final double getMin() {
        return min == null ? DEFAULT_MIN : min.get();
    }

    /// Sets the minimum slider value.
    ///
    /// @param min the minimum slider value
    public final void setMin(double min) {
        minProperty().set(min);
    }

    /// Returns the minimum slider value property.
    ///
    /// @return the minimum slider value property
    public final DoubleProperty minProperty() {
        if (min == null) {
            min = new DoublePropertyBase(DEFAULT_MIN) {
                /// Clamps the current value when the lower bound changes.
                @Override
                protected void invalidated() {
                    clampCurrentValue();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.MIN_VALUE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3Slider.this, VALUE_STRING_ATTRIBUTE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Returns the maximum slider value.
    ///
    /// @return the maximum slider value
    public final double getMax() {
        return max == null ? DEFAULT_MAX : max.get();
    }

    /// Sets the maximum slider value.
    ///
    /// @param max the maximum slider value
    public final void setMax(double max) {
        maxProperty().set(max);
    }

    /// Returns the maximum slider value property.
    ///
    /// @return the maximum slider value property
    public final DoubleProperty maxProperty() {
        if (max == null) {
            max = new DoublePropertyBase(DEFAULT_MAX) {
                /// Clamps the current value when the upper bound changes.
                @Override
                protected void invalidated() {
                    clampCurrentValue();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.MAX_VALUE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3Slider.this, VALUE_STRING_ATTRIBUTE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Returns the current slider value.
    ///
    /// @return the current slider value
    public final double getValue() {
        return value == null ? DEFAULT_VALUE : value.get();
    }

    /// Sets the current slider value.
    ///
    /// @param value the current slider value
    public final void setValue(double value) {
        valueProperty().set(value);
    }

    /// Returns the current slider value property.
    ///
    /// @return the current slider value property
    public final DoubleProperty valueProperty() {
        if (value == null) {
            value = new DoublePropertyBase(DEFAULT_VALUE) {
                /// Keeps the value inside the current range and requests visual updates.
                @Override
                protected void invalidated() {
                    double normalizedValue = normalizeValue(get());
                    if (Double.compare(normalizedValue, get()) != 0) {
                        set(normalizedValue);
                        return;
                    }
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3Slider.this, VALUE_STRING_ATTRIBUTE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "value";
                }
            };
        }
        return value;
    }

    /// Returns the slider orientation.
    ///
    /// @return the slider orientation
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    /// Sets the slider orientation.
    ///
    /// @param orientation the slider orientation
    public final void setOrientation(Orientation orientation) {
        orientationProperty().set(orientation);
    }

    /// Returns the slider orientation property.
    ///
    /// @return the slider orientation property
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new ObjectPropertyBase<>(Orientation.HORIZONTAL) {
                /// Falls back to horizontal orientation and requests layout.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(Orientation.HORIZONTAL);
                        return;
                    }
                    notifyAccessibleAttributeChanged(AccessibleAttribute.ORIENTATION);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Returns whether the value is being changed by a direct interaction.
    ///
    /// @return `true` while the value is being changed by direct interaction
    public final boolean isValueChanging() {
        return valueChanging != null && valueChanging.get();
    }

    /// Sets whether the value is being changed by a direct interaction.
    ///
    /// @param valueChanging whether the value is being changed by direct interaction
    public final void setValueChanging(boolean valueChanging) {
        valueChangingProperty().set(valueChanging);
    }

    /// Returns the value-changing property.
    ///
    /// @return the value-changing property
    public final BooleanProperty valueChangingProperty() {
        if (valueChanging == null) {
            valueChanging = new BooleanPropertyBase(false) {
                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "valueChanging";
                }
            };
        }
        return valueChanging;
    }

    /// Returns the amount changed by page navigation and continuous single-step navigation.
    ///
    /// @return the amount changed by page navigation and continuous single-step navigation
    public final double getBlockIncrement() {
        return blockIncrement == null ? DEFAULT_BLOCK_INCREMENT : blockIncrement.get();
    }

    /// Sets the amount changed by page navigation and continuous single-step navigation.
    ///
    /// @param blockIncrement the amount changed by page navigation and continuous single-step navigation
    public final void setBlockIncrement(double blockIncrement) {
        blockIncrementProperty().set(M3Css.nonNegative(blockIncrement, "blockIncrement"));
    }

    /// Returns the block increment property.
    ///
    /// @return the block increment property
    public final DoubleProperty blockIncrementProperty() {
        if (blockIncrement == null) {
            blockIncrement = new DoublePropertyBase(DEFAULT_BLOCK_INCREMENT) {
                /// Rejects negative or non-finite adjustment amounts.
                @Override
                protected void invalidated() {
                    double normalized = M3Css.nonNegative(get(), "blockIncrement");
                    if (Double.compare(normalized, get()) != 0) {
                        set(normalized);
                    }
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Moves the value by one unit increment.
    public final void increment() {
        adjustValue(getValue() + getUnitIncrement());
    }

    /// Moves the value down by one unit increment.
    public final void decrement() {
        adjustValue(getValue() - getUnitIncrement());
    }

    /// Sets the value after clamping it to the current slider range.
    ///
    /// @param value the value to clamp and apply
    public final void adjustValue(double value) {
        setValue(normalizeValue(value));
    }

    /// Returns the step size used by discrete sliders.
    ///
    /// @return the positive step size, or `0` when the slider is continuous
    public final double getStepSize() {
        return stepSize == null ? DEFAULT_STEP_SIZE : stepSize.get();
    }

    /// Sets the step size used by discrete sliders.
    ///
    /// A value of `0` makes the slider continuous. Positive values snap all direct, keyboard, programmatic,
    /// and accessibility value changes to the nearest step measured from `min`.
    ///
    /// @param stepSize the non-negative step size
    public final void setStepSize(double stepSize) {
        stepSizeProperty().set(M3Css.nonNegative(stepSize, "stepSize"));
    }

    /// Returns the step-size property.
    ///
    /// @return the step-size property
    public final DoubleProperty stepSizeProperty() {
        if (stepSize == null) {
            stepSize = new DoublePropertyBase(DEFAULT_STEP_SIZE) {
                /// Normalizes invalid values and re-snaps the current value when the step changes.
                @Override
                protected void invalidated() {
                    double normalizedStepSize = M3Css.nonNegative(get(), "stepSize");
                    if (Double.compare(normalizedStepSize, get()) != 0) {
                        set(normalizedStepSize);
                        return;
                    }
                    clampCurrentValue();
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
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

    /// Returns whether the active track starts at the center of the slider.
    ///
    /// Centered sliders are intended for ranges where zero or another neutral value is represented by the
    /// midpoint. The numeric range and value normalization remain unchanged; applications should normally use a
    /// range whose neutral value lies at its midpoint.
    ///
    /// @return `true` when the slider uses a centered active track
    public final boolean isCentered() {
        return centered != null && centered.get();
    }

    /// Sets whether the active track starts at the center of the slider.
    ///
    /// @param centered whether the slider uses a centered active track
    public final void setCentered(boolean centered) {
        centeredProperty().set(centered);
    }

    /// Returns the centered-track property.
    ///
    /// @return the centered-track property
    public final BooleanProperty centeredProperty() {
        if (centered == null) {
            centered = new BooleanPropertyBase(false) {
                /// Updates the centered pseudo-class and track geometry.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(CENTERED_PSEUDO_CLASS, get());
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "centered";
                }
            };
        }
        return centered;
    }

    /// Returns the slider track thickness token.
    ///
    /// @return the slider track thickness token in pixels
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the slider track thickness token.
    ///
    /// @param trackThickness the slider track thickness token in pixels
    public final void setTrackThickness(double trackThickness) {
        trackThicknessProperty().set(M3Css.nonNegative(trackThickness, "trackThickness"));
    }

    /// Returns the slider track thickness token property.
    ///
    /// @return the slider track thickness token property
    public final StyleableDoubleProperty trackThicknessProperty() {
        if (trackThickness == null) {
            trackThickness = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TRACK_THICKNESS,
                    this,
                    "trackThickness",
                    StyleableProperties.TRACK_THICKNESS,
                    this::requestLayout
            );
        }
        return trackThickness;
    }

    /// Returns the slider track shape radius token.
    ///
    /// @return the slider track shape radius token in pixels
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the slider track shape radius token.
    ///
    /// @param trackShape the slider track shape radius token in pixels
    public final void setTrackShape(double trackShape) {
        trackShapeProperty().set(M3Css.nonNegative(trackShape, "trackShape"));
    }

    /// Returns the slider track shape radius token property.
    ///
    /// @return the slider track shape radius token property
    public final StyleableDoubleProperty trackShapeProperty() {
        if (trackShape == null) {
            trackShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TRACK_SHAPE,
                    this,
                    "trackShape",
                    StyleableProperties.TRACK_SHAPE,
                    this::requestLayout
            );
        }
        return trackShape;
    }

    /// Returns the inactive-track stop indicator diameter token.
    ///
    /// @return the stop indicator diameter token in pixels
    public final double getStopIndicatorSize() {
        return stopIndicatorSize == null ? DEFAULT_STOP_INDICATOR_SIZE : stopIndicatorSize.get();
    }

    /// Sets the inactive-track stop indicator diameter token.
    ///
    /// A value of zero hides the stop indicator.
    ///
    /// @param stopIndicatorSize the non-negative stop indicator diameter token in pixels
    public final void setStopIndicatorSize(double stopIndicatorSize) {
        stopIndicatorSizeProperty().set(M3Css.nonNegative(stopIndicatorSize, "stopIndicatorSize"));
    }

    /// Returns the inactive-track stop indicator diameter token property.
    ///
    /// @return the stop indicator diameter token property
    public final StyleableDoubleProperty stopIndicatorSizeProperty() {
        if (stopIndicatorSize == null) {
            stopIndicatorSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_STOP_INDICATOR_SIZE,
                    this,
                    "stopIndicatorSize",
                    StyleableProperties.STOP_INDICATOR_SIZE,
                    this::requestLayout
            );
        }
        return stopIndicatorSize;
    }

    /// Returns the distance between an inactive track's outer edge and its stop indicator.
    ///
    /// The distance is measured from the outer track edge to the nearest edge of the indicator. It is independent
    /// of the track thickness and corner radius.
    ///
    /// @return the stop indicator trailing-space token in pixels
    public final double getStopIndicatorTrailingSpace() {
        return stopIndicatorTrailingSpace == null
                ? DEFAULT_STOP_INDICATOR_TRAILING_SPACE
                : stopIndicatorTrailingSpace.get();
    }

    /// Sets the distance between an inactive track's outer edge and its stop indicator.
    ///
    /// @param stopIndicatorTrailingSpace the non-negative trailing-space token in pixels
    public final void setStopIndicatorTrailingSpace(double stopIndicatorTrailingSpace) {
        stopIndicatorTrailingSpaceProperty().set(M3Css.nonNegative(
                stopIndicatorTrailingSpace,
                "stopIndicatorTrailingSpace"
        ));
    }

    /// Returns the stop indicator trailing-space token property.
    ///
    /// @return the stop indicator trailing-space token property
    public final StyleableDoubleProperty stopIndicatorTrailingSpaceProperty() {
        if (stopIndicatorTrailingSpace == null) {
            stopIndicatorTrailingSpace = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_STOP_INDICATOR_TRAILING_SPACE,
                    this,
                    "stopIndicatorTrailingSpace",
                    StyleableProperties.STOP_INDICATOR_TRAILING_SPACE,
                    this::requestLayout
            );
        }
        return stopIndicatorTrailingSpace;
    }

    /// Returns the slider handle long-side size token.
    ///
    /// @return the slider handle long-side size token in pixels
    public final double getThumbSize() {
        return thumbSize == null ? DEFAULT_THUMB_SIZE : thumbSize.get();
    }

    /// Sets the slider handle long-side size token.
    ///
    /// @param thumbSize the slider handle long-side size token in pixels
    public final void setThumbSize(double thumbSize) {
        thumbSizeProperty().set(M3Css.nonNegative(thumbSize, "thumbSize"));
    }

    /// Returns the slider handle long-side size token property.
    ///
    /// @return the slider handle long-side size token property
    public final StyleableDoubleProperty thumbSizeProperty() {
        if (thumbSize == null) {
            thumbSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_THUMB_SIZE,
                    this,
                    "thumbSize",
                    StyleableProperties.THUMB_SIZE,
                    this::requestLayout
            );
        }
        return thumbSize;
    }

    /// Returns the slider handle short-side width token.
    ///
    /// @return the slider handle short-side width token in pixels
    public final double getThumbWidth() {
        return thumbWidth == null ? DEFAULT_THUMB_WIDTH : thumbWidth.get();
    }

    /// Sets the slider handle short-side width token.
    ///
    /// @param thumbWidth the slider handle short-side width token in pixels
    public final void setThumbWidth(double thumbWidth) {
        thumbWidthProperty().set(M3Css.nonNegative(thumbWidth, "thumbWidth"));
    }

    /// Returns the slider handle short-side width token property.
    ///
    /// @return the slider handle short-side width token property
    public final StyleableDoubleProperty thumbWidthProperty() {
        if (thumbWidth == null) {
            thumbWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_THUMB_WIDTH,
                    this,
                    "thumbWidth",
                    StyleableProperties.THUMB_WIDTH,
                    this::requestLayout
            );
        }
        return thumbWidth;
    }

    /// Returns the gap between the handle and each adjacent track segment.
    ///
    /// @return the handle track-gap token in pixels
    public final double getThumbTrackGap() {
        return thumbTrackGap == null ? DEFAULT_THUMB_TRACK_GAP : thumbTrackGap.get();
    }

    /// Sets the gap between the handle and each adjacent track segment.
    ///
    /// @param thumbTrackGap the handle track-gap token in pixels
    public final void setThumbTrackGap(double thumbTrackGap) {
        thumbTrackGapProperty().set(M3Css.nonNegative(thumbTrackGap, "thumbTrackGap"));
    }

    /// Returns the handle track-gap token property.
    ///
    /// @return the handle track-gap token property
    public final StyleableDoubleProperty thumbTrackGapProperty() {
        if (thumbTrackGap == null) {
            thumbTrackGap = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_THUMB_TRACK_GAP,
                    this,
                    "thumbTrackGap",
                    StyleableProperties.THUMB_TRACK_GAP,
                    this::requestLayout
            );
        }
        return thumbTrackGap;
    }

    /// Returns the preferred touch target size token.
    ///
    /// @return the preferred touch target size token in pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    ///
    /// @param touchTargetSize the preferred touch target size token in pixels
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the preferred touch target size token property.
    ///
    /// @return the preferred touch target size token property
    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TOUCH_TARGET_SIZE,
                    this,
                    "touchTargetSize",
                    StyleableProperties.TOUCH_TARGET_SIZE,
                    this::requestLayout
            );
        }
        return touchTargetSize;
    }

    /// Returns the inset-icon size token.
    ///
    /// A value of zero disables inset icons for the current slider size.
    ///
    /// @return the inset-icon size in pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the inset-icon size token.
    ///
    /// @param iconSize the non-negative inset-icon size in pixels
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the inset-icon size token property.
    ///
    /// @return the styleable inset-icon size property
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::requestLayout
            );
        }
        return iconSize;
    }

    /// Returns the distance between an inset icon and the track's outer edge.
    ///
    /// @return the inset-icon padding in pixels
    public final double getIconPadding() {
        return iconPadding == null ? DEFAULT_ICON_PADDING : iconPadding.get();
    }

    /// Sets the distance between an inset icon and the track's outer edge.
    ///
    /// @param iconPadding the non-negative inset-icon padding in pixels
    public final void setIconPadding(double iconPadding) {
        iconPaddingProperty().set(M3Css.nonNegative(iconPadding, "iconPadding"));
    }

    /// Returns the inset-icon padding token property.
    ///
    /// @return the styleable inset-icon padding property
    public final StyleableDoubleProperty iconPaddingProperty() {
        if (iconPadding == null) {
            iconPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_PADDING,
                    this,
                    "iconPadding",
                    StyleableProperties.ICON_PADDING,
                    this::requestLayout
            );
        }
        return iconPadding;
    }

    /// Returns the distance between the handle and the value indicator.
    ///
    /// @return the value-indicator bottom space in pixels
    public final double getValueIndicatorBottomSpace() {
        return valueIndicatorBottomSpace == null
                ? DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE
                : valueIndicatorBottomSpace.get();
    }

    /// Sets the distance between the handle and the value indicator.
    ///
    /// @param bottomSpace the non-negative value-indicator bottom space in pixels
    public final void setValueIndicatorBottomSpace(double bottomSpace) {
        valueIndicatorBottomSpaceProperty().set(
                M3Css.nonNegative(bottomSpace, "bottomSpace")
        );
    }

    /// Returns the value-indicator bottom-space token property.
    ///
    /// @return the styleable value-indicator bottom-space property
    public final StyleableDoubleProperty valueIndicatorBottomSpaceProperty() {
        if (valueIndicatorBottomSpace == null) {
            valueIndicatorBottomSpace = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE,
                    this,
                    "valueIndicatorBottomSpace",
                    StyleableProperties.VALUE_INDICATOR_BOTTOM_SPACE,
                    this::requestLayout
            );
        }
        return valueIndicatorBottomSpace;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the CSS metadata for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default Material Design 3 slider skin.
    ///
    /// @return the default Material Design 3 slider skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SliderSkin(this);
    }

    /// Returns accessibility attributes for the current slider value.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            StringConverter<Double> formatter = getLabelFormatter();
            if (formatter != null) {
                @Nullable String text = formatter.toString(getValue());
                return text == null ? "" : text;
            }
            double value = getValue();
            return Math.rint(value) == value ? Long.toString((long) value) : Double.toString(value);
        }
        return switch (attribute) {
            case MIN_VALUE -> getMin();
            case MAX_VALUE -> getMax();
            case VALUE -> getValue();
            case ORIENTATION -> getOrientation();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for value adjustment.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showDirectItem(this, this);
            case INCREMENT -> increment();
            case DECREMENT -> decrement();
            case BLOCK_INCREMENT -> adjustValue(getValue() + getBlockIncrement());
            case BLOCK_DECREMENT -> adjustValue(getValue() - getBlockIncrement());
            case SET_VALUE -> setAccessibleValue(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the user-agent stylesheet for M3FX sliders.
    ///
    /// @return the slider user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("slider.css");
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        updateSizeStyle();
        setAccessibleRole(AccessibleRole.SLIDER);
        M3Accessible.installAccessibleActionRoute(this, () -> M3Accessible.showDirectItem(this, this), null);
        setFocusTraversable(true);
        requestLayout();
    }

    /// Applies the style class associated with the current Material size.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                sizeStyleClass(getSize()),
                sizeStyleClass(M3SliderSize.EXTRA_SMALL),
                sizeStyleClass(M3SliderSize.SMALL),
                sizeStyleClass(M3SliderSize.MEDIUM),
                sizeStyleClass(M3SliderSize.LARGE),
                sizeStyleClass(M3SliderSize.EXTRA_LARGE)
        );
    }

    /// Returns the control style class for a Material slider size.
    ///
    /// @param size the slider size
    /// @return the style class for the size
    static String sizeStyleClass(M3SliderSize size) {
        return "m3-slider-" + size.cssSuffix();
    }

    /// Clamps the current value after a range change.
    private void clampCurrentValue() {
        setValue(normalizeValue(getValue()));
    }

    /// Returns the unit amount used for single-step keyboard and accessibility adjustments.
    private double getUnitIncrement() {
        double stepSize = getStepSize();
        return stepSize > 0.0 ? stepSize : getBlockIncrement();
    }

    /// Clamps and snaps a value to the current slider range.
    private double normalizeValue(double value) {
        double clampedValue = clampToRange(value);
        double stepSize = getStepSize();
        if (stepSize <= 0.0 || getMax() <= getMin()) {
            return clampedValue;
        }

        double min = getMin();
        double snappedValue = min + Math.rint((clampedValue - min) / stepSize) * stepSize;
        return clampToRange(snappedValue);
    }

    /// Clamps a value to the current range.
    private double clampToRange(double value) {
        double min = getMin();
        double max = getMax();
        if (Double.isNaN(value)) {
            return min;
        }
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    /// Applies the first numeric value supplied by an accessibility client.
    private void setAccessibleValue(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (parameter instanceof Number number) {
                adjustValue(number.doubleValue());
                return;
            }
        }
    }


    /// CSS metadata for M3FX slider component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the track thickness token.
        private static final CssMetaData<M3Slider, Number> TRACK_THICKNESS =
                new CssMetaData<>("-m3-track-thickness", SizeConverter.getInstance(), DEFAULT_TRACK_THICKNESS) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.trackThicknessProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.trackThicknessProperty();
                    }
                };

        /// CSS metadata for the track shape token.
        private static final CssMetaData<M3Slider, Number> TRACK_SHAPE =
                new CssMetaData<>("-m3-track-shape", SizeConverter.getInstance(), DEFAULT_TRACK_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.trackShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.trackShapeProperty();
                    }
                };

        /// CSS metadata for the stop indicator diameter token.
        private static final CssMetaData<M3Slider, Number> STOP_INDICATOR_SIZE =
                new CssMetaData<>(
                        "-m3-stop-indicator-size",
                        SizeConverter.getInstance(),
                        DEFAULT_STOP_INDICATOR_SIZE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.stopIndicatorSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.stopIndicatorSizeProperty();
                    }
                };

        /// CSS metadata for the stop indicator trailing-space token.
        private static final CssMetaData<M3Slider, Number> STOP_INDICATOR_TRAILING_SPACE =
                new CssMetaData<>(
                        "-m3-stop-indicator-trailing-space",
                        SizeConverter.getInstance(),
                        DEFAULT_STOP_INDICATOR_TRAILING_SPACE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.stopIndicatorTrailingSpaceProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.stopIndicatorTrailingSpaceProperty();
                    }
                };

        /// CSS metadata for the thumb long-side size token.
        private static final CssMetaData<M3Slider, Number> THUMB_SIZE =
                new CssMetaData<>("-m3-thumb-size", SizeConverter.getInstance(), DEFAULT_THUMB_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.thumbSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.thumbSizeProperty();
                    }
                };

        /// CSS metadata for the thumb short-side width token.
        private static final CssMetaData<M3Slider, Number> THUMB_WIDTH =
                new CssMetaData<>("-m3-thumb-width", SizeConverter.getInstance(), DEFAULT_THUMB_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.thumbWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.thumbWidthProperty();
                    }
                };

        /// CSS metadata for the handle track-gap token.
        private static final CssMetaData<M3Slider, Number> THUMB_TRACK_GAP =
                new CssMetaData<>("-m3-thumb-track-gap", SizeConverter.getInstance(), DEFAULT_THUMB_TRACK_GAP) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.thumbTrackGapProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.thumbTrackGapProperty();
                    }
                };

        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3Slider, Number> TOUCH_TARGET_SIZE =
                new CssMetaData<>("-m3-touch-target-size", SizeConverter.getInstance(), DEFAULT_TOUCH_TARGET_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.touchTargetSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.touchTargetSizeProperty();
                    }
                };

        /// CSS metadata for the inset-icon size token.
        private static final CssMetaData<M3Slider, Number> ICON_SIZE =
                new CssMetaData<>("-m3-slider-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.iconSizeProperty();
                    }
                };

        /// CSS metadata for the inset-icon padding token.
        private static final CssMetaData<M3Slider, Number> ICON_PADDING =
                new CssMetaData<>("-m3-slider-icon-padding", SizeConverter.getInstance(), DEFAULT_ICON_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.iconPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.iconPaddingProperty();
                    }
                };

        /// CSS metadata for the value-indicator bottom-space token.
        private static final CssMetaData<M3Slider, Number> VALUE_INDICATOR_BOTTOM_SPACE =
                new CssMetaData<>(
                        "-m3-value-indicator-bottom-space",
                        SizeConverter.getInstance(),
                        DEFAULT_VALUE_INDICATOR_BOTTOM_SPACE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Slider control) {
                        return M3Css.isSettable(control.valueIndicatorBottomSpaceProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Slider control) {
                        return control.valueIndicatorBottomSpaceProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(TRACK_SHAPE);
            styleables.add(STOP_INDICATOR_SIZE);
            styleables.add(STOP_INDICATOR_TRAILING_SPACE);
            styleables.add(THUMB_SIZE);
            styleables.add(THUMB_WIDTH);
            styleables.add(THUMB_TRACK_GAP);
            styleables.add(TOUCH_TARGET_SIZE);
            styleables.add(ICON_SIZE);
            styleables.add(ICON_PADDING);
            styleables.add(VALUE_INDICATOR_BOTTOM_SPACE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
