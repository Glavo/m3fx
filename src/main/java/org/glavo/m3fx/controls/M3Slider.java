package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Slider;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 slider.
@NotNullByDefault
public class M3Slider extends Slider {
    /// The base style class for m3fx sliders.
    public static final String STYLE_CLASS = "m3-slider";

    /// The default slider track thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 4.0;

    /// The default slider track shape radius.
    private static final double DEFAULT_TRACK_SHAPE = 999.0;

    /// The default slider thumb size.
    private static final double DEFAULT_THUMB_SIZE = 20.0;

    /// The default slider touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The styleable track thickness token.
    private StyleableDoubleProperty trackThickness;

    /// The styleable track shape token.
    private StyleableDoubleProperty trackShape;

    /// The styleable thumb size token.
    private StyleableDoubleProperty thumbSize;

    /// The styleable touch target size token.
    private StyleableDoubleProperty touchTargetSize;

    /// Creates a slider with the JavaFX default range.
    public M3Slider() {
        initialize();
    }

    /// Creates a slider with a range and initial value.
    public M3Slider(double min, double max, double value) {
        super(min, max, value);
        initialize();
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

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = Math.max(getTouchTargetSize(), Math.max(getThumbSize(), getTrackThickness()));
        setMinHeight(size);
        setPrefHeight(size);
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
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Slider.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(TRACK_SHAPE);
            styleables.add(THUMB_SIZE);
            styleables.add(TOUCH_TARGET_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
