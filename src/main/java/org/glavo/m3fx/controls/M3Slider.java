// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SliderSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 slider.
@NotNullByDefault
public class M3Slider extends Control {
    /// The base style class for m3fx sliders.
    public static final String STYLE_CLASS = "m3-slider";

    /// The default minimum slider value.
    private static final double DEFAULT_MIN = 0.0;

    /// The default maximum slider value.
    private static final double DEFAULT_MAX = 100.0;

    /// The default slider value.
    private static final double DEFAULT_VALUE = 0.0;

    /// The default block increment used for keyboard navigation.
    private static final double DEFAULT_BLOCK_INCREMENT = 10.0;

    /// The default slider track thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 4.0;

    /// The default slider track shape radius.
    private static final double DEFAULT_TRACK_SHAPE = 999.0;

    /// The default slider thumb size.
    private static final double DEFAULT_THUMB_SIZE = 20.0;

    /// The default slider touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The minimum slider value.
    private @Nullable DoubleProperty min;

    /// The maximum slider value.
    private @Nullable DoubleProperty max;

    /// The current slider value.
    private @Nullable DoubleProperty value;

    /// The slider orientation.
    private @Nullable ObjectProperty<Orientation> orientation;

    /// Whether the value is currently being changed through direct interaction.
    private @Nullable BooleanProperty valueChanging;

    /// The amount changed by page and arrow navigation.
    private @Nullable DoubleProperty blockIncrement;

    /// The styleable track thickness token.
    private @Nullable StyleableDoubleProperty trackThickness;

    /// The styleable track shape token.
    private @Nullable StyleableDoubleProperty trackShape;

    /// The styleable thumb size token.
    private @Nullable StyleableDoubleProperty thumbSize;

    /// The styleable touch target size token.
    private @Nullable StyleableDoubleProperty touchTargetSize;

    /// Creates a slider with the JavaFX default range.
    public M3Slider() {
        initialize();
    }

    /// Creates a slider with a range and initial value.
    public M3Slider(double min, double max, double value) {
        initialize();
        setMin(min);
        setMax(max);
        setValue(value);
    }

    /// Returns the minimum slider value.
    public final double getMin() {
        return min == null ? DEFAULT_MIN : min.get();
    }

    /// Sets the minimum slider value.
    public final void setMin(double min) {
        minProperty().set(min);
    }

    /// Returns the minimum slider value property.
    public final DoubleProperty minProperty() {
        if (min == null) {
            min = new DoublePropertyBase(DEFAULT_MIN) {
                /// Clamps the current value when the lower bound changes.
                @Override
                protected void invalidated() {
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
                    return "min";
                }
            };
        }
        return min;
    }

    /// Returns the maximum slider value.
    public final double getMax() {
        return max == null ? DEFAULT_MAX : max.get();
    }

    /// Sets the maximum slider value.
    public final void setMax(double max) {
        maxProperty().set(max);
    }

    /// Returns the maximum slider value property.
    public final DoubleProperty maxProperty() {
        if (max == null) {
            max = new DoublePropertyBase(DEFAULT_MAX) {
                /// Clamps the current value when the upper bound changes.
                @Override
                protected void invalidated() {
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
                    return "max";
                }
            };
        }
        return max;
    }

    /// Returns the current slider value.
    public final double getValue() {
        return value == null ? DEFAULT_VALUE : value.get();
    }

    /// Sets the current slider value.
    public final void setValue(double value) {
        valueProperty().set(value);
    }

    /// Returns the current slider value property.
    public final DoubleProperty valueProperty() {
        if (value == null) {
            value = new DoublePropertyBase(DEFAULT_VALUE) {
                /// Keeps the value inside the current range and requests visual updates.
                @Override
                protected void invalidated() {
                    double clampedValue = clampToRange(get());
                    if (Double.compare(clampedValue, get()) != 0) {
                        set(clampedValue);
                        return;
                    }
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
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    /// Sets the slider orientation.
    public final void setOrientation(Orientation orientation) {
        orientationProperty().set(orientation);
    }

    /// Returns the slider orientation property.
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
    public final boolean isValueChanging() {
        return valueChanging != null && valueChanging.get();
    }

    /// Sets whether the value is being changed by a direct interaction.
    public final void setValueChanging(boolean valueChanging) {
        valueChangingProperty().set(valueChanging);
    }

    /// Returns the value-changing property.
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

    /// Returns the amount changed by page and arrow navigation.
    public final double getBlockIncrement() {
        return blockIncrement == null ? DEFAULT_BLOCK_INCREMENT : blockIncrement.get();
    }

    /// Sets the amount changed by page and arrow navigation.
    public final void setBlockIncrement(double blockIncrement) {
        blockIncrementProperty().set(blockIncrement);
    }

    /// Returns the block increment property.
    public final DoubleProperty blockIncrementProperty() {
        if (blockIncrement == null) {
            blockIncrement = new DoublePropertyBase(DEFAULT_BLOCK_INCREMENT) {
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

    /// Moves the value by one block increment.
    public final void increment() {
        adjustValue(getValue() + getBlockIncrement());
    }

    /// Moves the value down by one block increment.
    public final void decrement() {
        adjustValue(getValue() - getBlockIncrement());
    }

    /// Sets the value after clamping it to the current slider range.
    public final void adjustValue(double value) {
        setValue(clampToRange(value));
    }

    /// Returns the slider track thickness token.
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the slider track thickness token.
    public final void setTrackThickness(double trackThickness) {
        trackThicknessProperty().set(M3Css.nonNegative(trackThickness, "trackThickness"));
    }

    /// Returns the slider track thickness token property.
    public final StyleableDoubleProperty trackThicknessProperty() {
        if (trackThickness == null) {
            trackThickness = new StyleableDoubleProperty(DEFAULT_TRACK_THICKNESS) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "trackThickness"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "trackThickness";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Slider, Number> getCssMetaData() {
                    return StyleableProperties.TRACK_THICKNESS;
                }
            };
        }
        return trackThickness;
    }

    /// Returns the slider track shape radius token.
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the slider track shape radius token.
    public final void setTrackShape(double trackShape) {
        trackShapeProperty().set(M3Css.nonNegative(trackShape, "trackShape"));
    }

    /// Returns the slider track shape radius token property.
    public final StyleableDoubleProperty trackShapeProperty() {
        if (trackShape == null) {
            trackShape = new StyleableDoubleProperty(DEFAULT_TRACK_SHAPE) {
                /// Validates updated shape tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "trackShape"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "trackShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Slider, Number> getCssMetaData() {
                    return StyleableProperties.TRACK_SHAPE;
                }
            };
        }
        return trackShape;
    }

    /// Returns the slider thumb size token.
    public final double getThumbSize() {
        return thumbSize == null ? DEFAULT_THUMB_SIZE : thumbSize.get();
    }

    /// Sets the slider thumb size token.
    public final void setThumbSize(double thumbSize) {
        thumbSizeProperty().set(M3Css.nonNegative(thumbSize, "thumbSize"));
    }

    /// Returns the slider thumb size token property.
    public final StyleableDoubleProperty thumbSizeProperty() {
        if (thumbSize == null) {
            thumbSize = new StyleableDoubleProperty(DEFAULT_THUMB_SIZE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "thumbSize"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "thumbSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Slider, Number> getCssMetaData() {
                    return StyleableProperties.THUMB_SIZE;
                }
            };
        }
        return thumbSize;
    }

    /// Returns the preferred touch target size token.
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the preferred touch target size token property.
    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = new StyleableDoubleProperty(DEFAULT_TOUCH_TARGET_SIZE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "touchTargetSize"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Slider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "touchTargetSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Slider, Number> getCssMetaData() {
                    return StyleableProperties.TOUCH_TARGET_SIZE;
                }
            };
        }
        return touchTargetSize;
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default Material Design 3 slider skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SliderSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx sliders.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("slider.css");
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.SLIDER);
        setFocusTraversable(true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = Math.max(getTouchTargetSize(), Math.max(getThumbSize(), getTrackThickness()));
        setMinHeight(size);
        setPrefHeight(size);
    }

    /// Clamps the current value after a range change.
    private void clampCurrentValue() {
        setValue(clampToRange(getValue()));
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

    /// CSS metadata for m3fx slider component tokens.
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

        /// CSS metadata for the thumb size token.
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

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(TRACK_SHAPE);
            styleables.add(THUMB_SIZE);
            styleables.add(TOUCH_TARGET_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
