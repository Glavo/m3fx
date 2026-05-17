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
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ProgressIndicatorSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 circular progress indicator.
@NotNullByDefault
public class M3ProgressIndicator extends Control {
    /// The base style class for m3fx progress indicators.
    public static final String STYLE_CLASS = "m3-progress-indicator";

    /// The progress value that marks the control as indeterminate.
    public static final double INDETERMINATE_PROGRESS = -1.0;

    /// The pseudo class applied while progress is indeterminate.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("indeterminate");

    /// The default circular indicator stroke thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 4.0;

    /// The default circular indicator size.
    private static final double DEFAULT_INDICATOR_SIZE = 48.0;

    /// The current progress value.
    private DoubleProperty progress;

    /// The styleable circular indicator stroke thickness token.
    private StyleableDoubleProperty trackThickness;

    /// The styleable indicator size token.
    private StyleableDoubleProperty indicatorSize;

    /// Creates an indeterminate progress indicator.
    public M3ProgressIndicator() {
        initialize();
    }

    /// Creates a progress indicator with an initial progress value.
    public M3ProgressIndicator(double progress) {
        initialize();
        setProgress(progress);
    }

    /// Returns the current progress value.
    public final double getProgress() {
        return progress == null ? INDETERMINATE_PROGRESS : progress.get();
    }

    /// Sets the current progress value.
    public final void setProgress(double progress) {
        progressProperty().set(progress);
    }

    /// Returns the current progress value property.
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
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressIndicator.this;
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
    public final boolean isIndeterminate() {
        return getProgress() == INDETERMINATE_PROGRESS;
    }

    /// Returns the circular indicator stroke thickness token.
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the circular indicator stroke thickness token.
    public final void setTrackThickness(double trackThickness) {
        trackThicknessProperty().set(M3Css.nonNegative(trackThickness, "trackThickness"));
    }

    /// Returns the circular indicator stroke thickness token property.
    public final StyleableDoubleProperty trackThicknessProperty() {
        if (trackThickness == null) {
            trackThickness = new StyleableDoubleProperty(DEFAULT_TRACK_THICKNESS) {
                /// Requests layout when the stroke thickness token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "trackThickness");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "trackThickness";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressIndicator, Number> getCssMetaData() {
                    return StyleableProperties.TRACK_THICKNESS;
                }
            };
        }
        return trackThickness;
    }

    /// Returns the circular indicator size token.
    public final double getIndicatorSize() {
        return indicatorSize == null ? DEFAULT_INDICATOR_SIZE : indicatorSize.get();
    }

    /// Sets the circular indicator size token.
    public final void setIndicatorSize(double indicatorSize) {
        indicatorSizeProperty().set(M3Css.nonNegative(indicatorSize, "indicatorSize"));
    }

    /// Returns the circular indicator size token property.
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
                    return M3ProgressIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indicatorSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressIndicator, Number> getCssMetaData() {
                    return StyleableProperties.INDICATOR_SIZE;
                }
            };
        }
        return indicatorSize;
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

    /// Creates the default progress indicator skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ProgressIndicatorSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx progress controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("progress.css");
    }

    /// Adds base style classes.
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

    /// CSS metadata for m3fx progress indicator component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the circular indicator stroke thickness token.
        private static final CssMetaData<M3ProgressIndicator, Number> TRACK_THICKNESS =
                new CssMetaData<>("-m3-track-thickness", SizeConverter.getInstance(), DEFAULT_TRACK_THICKNESS) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressIndicator control) {
                        return M3Css.isSettable(control.trackThicknessProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressIndicator control) {
                        return control.trackThicknessProperty();
                    }
                };

        /// CSS metadata for the circular indicator size token.
        private static final CssMetaData<M3ProgressIndicator, Number> INDICATOR_SIZE =
                new CssMetaData<>("-m3-indicator-size", SizeConverter.getInstance(), DEFAULT_INDICATOR_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressIndicator control) {
                        return M3Css.isSettable(control.indicatorSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressIndicator control) {
                        return control.indicatorSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(INDICATOR_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
