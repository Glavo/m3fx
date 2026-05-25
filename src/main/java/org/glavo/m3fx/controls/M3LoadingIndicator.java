// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3LoadingIndicatorSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 loading indicator.
///
/// `M3LoadingIndicator` displays a compact sequence of animated shapes for indeterminate loading and a
/// shape-by-shape completion state for determinate progress values from `0.0` to `1.0`. It is intended for
/// loading affordances where Material Design 3 Expressive uses a dedicated loading indicator instead of a
/// circular progress indicator. See
/// [Material Design loading indicators](https://m3.material.io/components/loading-indicator/overview).
@NotNullByDefault
public class M3LoadingIndicator extends Control {
    /// The base style class for m3fx loading indicators.
    public static final String STYLE_CLASS = "m3-loading-indicator";

    /// The progress value that marks the control as indeterminate.
    public static final double INDETERMINATE_PROGRESS = -1.0;

    /// The pseudo class applied while progress is indeterminate.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("indeterminate");

    /// The default loading indicator layout size.
    private static final double DEFAULT_INDICATOR_SIZE = 64.0;

    /// The default size for each loading indicator shape.
    private static final double DEFAULT_SHAPE_SIZE = 18.0;

    /// The default gap between loading indicator shapes.
    private static final double DEFAULT_SHAPE_SPACING = 6.0;

    /// The minimum accessible progress value.
    private static final double ACCESSIBLE_MIN_VALUE = 0.0;

    /// The maximum accessible progress value.
    private static final double ACCESSIBLE_MAX_VALUE = 1.0;

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// The current progress value.
    private @Nullable DoubleProperty progress;

    /// The styleable loading indicator layout size token.
    private @Nullable StyleableDoubleProperty indicatorSize;

    /// The styleable loading indicator shape size token.
    private @Nullable StyleableDoubleProperty shapeSize;

    /// The styleable gap between loading indicator shapes.
    private @Nullable StyleableDoubleProperty shapeSpacing;

    /// Creates an indeterminate loading indicator.
    public M3LoadingIndicator() {
        initialize();
    }

    /// Creates a loading indicator with an initial progress value.
    ///
    /// @param progress the initial progress value, from `0.0` to `1.0`, or [INDETERMINATE_PROGRESS]
    public M3LoadingIndicator(double progress) {
        initialize();
        setProgress(progress);
    }

    /// Returns the current progress value.
    ///
    /// @return the current progress value, or [INDETERMINATE_PROGRESS]
    public final double getProgress() {
        return progress == null ? INDETERMINATE_PROGRESS : progress.get();
    }

    /// Sets the current progress value.
    ///
    /// @param progress the progress value, from `0.0` to `1.0`, or [INDETERMINATE_PROGRESS]
    public final void setProgress(double progress) {
        progressProperty().set(progress);
    }

    /// Returns the current progress value property.
    ///
    /// @return the writable progress value property
    public final DoubleProperty progressProperty() {
        if (progress == null) {
            progress = new DoublePropertyBase(INDETERMINATE_PROGRESS) {
                /// Normalizes progress and updates the indeterminate pseudo class.
                @Override
                protected void invalidated() {
                    double normalizedProgress = normalizeProgress(get());
                    if (Double.compare(normalizedProgress, get()) != 0) {
                        set(normalizedProgress);
                        return;
                    }
                    pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, isIndeterminate());
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3LoadingIndicator.this, VALUE_STRING_ATTRIBUTE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3LoadingIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "progress";
                }
            };
        }
        return progress;
    }

    /// Returns whether the current progress value is indeterminate.
    ///
    /// @return `true` when the current progress value is indeterminate
    public final boolean isIndeterminate() {
        return getProgress() == INDETERMINATE_PROGRESS;
    }

    /// Returns the loading indicator layout size token.
    ///
    /// @return the indicator layout size in pixels
    public final double getIndicatorSize() {
        return indicatorSize == null ? DEFAULT_INDICATOR_SIZE : indicatorSize.get();
    }

    /// Sets the loading indicator layout size token.
    ///
    /// @param indicatorSize the indicator layout size in pixels
    public final void setIndicatorSize(double indicatorSize) {
        indicatorSizeProperty().set(M3Css.nonNegative(indicatorSize, "indicatorSize"));
    }

    /// Returns the loading indicator layout size token property.
    ///
    /// @return the styleable indicator size property
    public final StyleableDoubleProperty indicatorSizeProperty() {
        if (indicatorSize == null) {
            indicatorSize = new StyleableDoubleProperty(DEFAULT_INDICATOR_SIZE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "indicatorSize");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3LoadingIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indicatorSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3LoadingIndicator, Number> getCssMetaData() {
                    return StyleableProperties.INDICATOR_SIZE;
                }
            };
        }
        return indicatorSize;
    }

    /// Returns the loading indicator shape size token.
    ///
    /// @return the shape size in pixels
    public final double getShapeSize() {
        return shapeSize == null ? DEFAULT_SHAPE_SIZE : shapeSize.get();
    }

    /// Sets the loading indicator shape size token.
    ///
    /// @param shapeSize the shape size in pixels
    public final void setShapeSize(double shapeSize) {
        shapeSizeProperty().set(M3Css.nonNegative(shapeSize, "shapeSize"));
    }

    /// Returns the loading indicator shape size token property.
    ///
    /// @return the styleable shape size property
    public final StyleableDoubleProperty shapeSizeProperty() {
        if (shapeSize == null) {
            shapeSize = new StyleableDoubleProperty(DEFAULT_SHAPE_SIZE) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "shapeSize");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3LoadingIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "shapeSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3LoadingIndicator, Number> getCssMetaData() {
                    return StyleableProperties.SHAPE_SIZE;
                }
            };
        }
        return shapeSize;
    }

    /// Returns the loading indicator shape spacing token.
    ///
    /// @return the spacing between shapes in pixels
    public final double getShapeSpacing() {
        return shapeSpacing == null ? DEFAULT_SHAPE_SPACING : shapeSpacing.get();
    }

    /// Sets the loading indicator shape spacing token.
    ///
    /// @param shapeSpacing the spacing between shapes in pixels
    public final void setShapeSpacing(double shapeSpacing) {
        shapeSpacingProperty().set(M3Css.nonNegative(shapeSpacing, "shapeSpacing"));
    }

    /// Returns the loading indicator shape spacing token property.
    ///
    /// @return the styleable shape spacing property
    public final StyleableDoubleProperty shapeSpacingProperty() {
        if (shapeSpacing == null) {
            shapeSpacing = new StyleableDoubleProperty(DEFAULT_SHAPE_SPACING) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "shapeSpacing");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3LoadingIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "shapeSpacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3LoadingIndicator, Number> getCssMetaData() {
                    return StyleableProperties.SHAPE_SPACING;
                }
            };
        }
        return shapeSpacing;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3LoadingIndicator`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default loading indicator skin.
    ///
    /// @return the default loading indicator skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3LoadingIndicatorSkin(this);
    }

    /// Returns accessibility attributes for the loading progress value.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            return accessibleValueString();
        }
        return switch (attribute) {
            case INDETERMINATE -> isIndeterminate();
            case MIN_VALUE -> ACCESSIBLE_MIN_VALUE;
            case MAX_VALUE -> ACCESSIBLE_MAX_VALUE;
            case VALUE -> getProgress();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Returns the user-agent stylesheet for m3fx loading indicators.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("loading-indicator.css");
    }

    /// Adds base style classes and accessibility metadata.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getIndicatorSize();
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
    }

    /// Normalizes progress values to the supported range.
    private static double normalizeProgress(double progress) {
        if (Double.isNaN(progress) || progress < 0.0) {
            return INDETERMINATE_PROGRESS;
        }
        return Math.min(1.0, progress);
    }

    /// Returns the accessible string representation of the current progress.
    private String accessibleValueString() {
        return isIndeterminate() ? "Indeterminate" : Math.round(getProgress() * 100.0) + "%";
    }

    /// CSS metadata for m3fx loading indicator component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the loading indicator layout size token.
        private static final CssMetaData<M3LoadingIndicator, Number> INDICATOR_SIZE =
                new CssMetaData<>("-m3-indicator-size", SizeConverter.getInstance(), DEFAULT_INDICATOR_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3LoadingIndicator control) {
                        return M3Css.isSettable(control.indicatorSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3LoadingIndicator control) {
                        return control.indicatorSizeProperty();
                    }
                };

        /// CSS metadata for the loading indicator shape size token.
        private static final CssMetaData<M3LoadingIndicator, Number> SHAPE_SIZE =
                new CssMetaData<>("-m3-shape-size", SizeConverter.getInstance(), DEFAULT_SHAPE_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3LoadingIndicator control) {
                        return M3Css.isSettable(control.shapeSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3LoadingIndicator control) {
                        return control.shapeSizeProperty();
                    }
                };

        /// CSS metadata for the loading indicator shape spacing token.
        private static final CssMetaData<M3LoadingIndicator, Number> SHAPE_SPACING =
                new CssMetaData<>("-m3-shape-spacing", SizeConverter.getInstance(), DEFAULT_SHAPE_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3LoadingIndicator control) {
                        return M3Css.isSettable(control.shapeSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3LoadingIndicator control) {
                        return control.shapeSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(INDICATOR_SIZE);
            styleables.add(SHAPE_SIZE);
            styleables.add(SHAPE_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
