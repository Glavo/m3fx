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
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ProgressBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 linear progress indicator.
@NotNullByDefault
public class M3ProgressBar extends Control {
    /// The base style class for m3fx progress bars.
    public static final String STYLE_CLASS = "m3-progress-bar";

    /// The progress value that marks the control as indeterminate.
    public static final double INDETERMINATE_PROGRESS = -1.0;

    /// The pseudo class applied while progress is indeterminate.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("indeterminate");

    /// The default progress track thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 4.0;

    /// The default progress track shape radius.
    private static final double DEFAULT_TRACK_SHAPE = 999.0;

    /// The default linear wave amplitude.
    private static final double DEFAULT_WAVE_AMPLITUDE = 0.0;

    /// The default linear wave length.
    private static final double DEFAULT_WAVELENGTH = 40.0;

    /// The default gap between active progress and track.
    private static final double DEFAULT_TRACK_GAP = 4.0;

    /// The default linear stop indicator size.
    private static final double DEFAULT_STOP_SIZE = 4.0;

    /// The minimum accessible progress value.
    private static final double ACCESSIBLE_MIN_VALUE = 0.0;

    /// The maximum accessible progress value.
    private static final double ACCESSIBLE_MAX_VALUE = 1.0;

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// The current progress value.
    private @Nullable DoubleProperty progress;

    /// The styleable track thickness token.
    private @Nullable StyleableDoubleProperty trackThickness;

    /// The styleable track shape token.
    private @Nullable StyleableDoubleProperty trackShape;

    /// The styleable wave amplitude token.
    private @Nullable StyleableDoubleProperty waveAmplitude;

    /// The styleable wavelength token.
    private @Nullable StyleableDoubleProperty wavelength;

    /// The styleable track gap token.
    private @Nullable StyleableDoubleProperty trackGap;

    /// The styleable stop indicator size token.
    private @Nullable StyleableDoubleProperty stopSize;

    /// Creates an indeterminate progress bar.
    public M3ProgressBar() {
        initialize();
    }

    /// Creates a progress bar with an initial progress value.
    public M3ProgressBar(double progress) {
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
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3ProgressBar.this, VALUE_STRING_ATTRIBUTE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
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

    /// Returns the progress track thickness token.
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the progress track thickness token.
    public final void setTrackThickness(double trackThickness) {
        trackThicknessProperty().set(M3Css.nonNegative(trackThickness, "trackThickness"));
    }

    /// Returns the progress track thickness token property.
    public final StyleableDoubleProperty trackThicknessProperty() {
        if (trackThickness == null) {
            trackThickness = new StyleableDoubleProperty(DEFAULT_TRACK_THICKNESS) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "trackThickness");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "trackThickness";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.TRACK_THICKNESS;
                }
            };
        }
        return trackThickness;
    }

    /// Returns the progress track shape radius token.
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the progress track shape radius token.
    public final void setTrackShape(double trackShape) {
        trackShapeProperty().set(M3Css.nonNegative(trackShape, "trackShape"));
    }

    /// Returns the progress track shape radius token property.
    public final StyleableDoubleProperty trackShapeProperty() {
        if (trackShape == null) {
            trackShape = new StyleableDoubleProperty(DEFAULT_TRACK_SHAPE) {
                /// Validates updated shape tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "trackShape");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "trackShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.TRACK_SHAPE;
                }
            };
        }
        return trackShape;
    }

    /// Returns the wavy progress amplitude token.
    public final double getWaveAmplitude() {
        return waveAmplitude == null ? DEFAULT_WAVE_AMPLITUDE : waveAmplitude.get();
    }

    /// Sets the wavy progress amplitude token.
    public final void setWaveAmplitude(double waveAmplitude) {
        waveAmplitudeProperty().set(M3Css.nonNegative(waveAmplitude, "waveAmplitude"));
    }

    /// Returns the wavy progress amplitude token property.
    public final StyleableDoubleProperty waveAmplitudeProperty() {
        if (waveAmplitude == null) {
            waveAmplitude = new StyleableDoubleProperty(DEFAULT_WAVE_AMPLITUDE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "waveAmplitude");
                    updateMetrics();
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "waveAmplitude";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.WAVE_AMPLITUDE;
                }
            };
        }
        return waveAmplitude;
    }

    /// Returns the wavy progress wavelength token.
    public final double getWavelength() {
        return wavelength == null ? DEFAULT_WAVELENGTH : wavelength.get();
    }

    /// Sets the wavy progress wavelength token.
    public final void setWavelength(double wavelength) {
        wavelengthProperty().set(M3Css.nonNegative(wavelength, "wavelength"));
    }

    /// Returns the wavy progress wavelength token property.
    public final StyleableDoubleProperty wavelengthProperty() {
        if (wavelength == null) {
            wavelength = new StyleableDoubleProperty(DEFAULT_WAVELENGTH) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "wavelength");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "wavelength";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.WAVELENGTH;
                }
            };
        }
        return wavelength;
    }

    /// Returns the gap token between active progress and track.
    public final double getTrackGap() {
        return trackGap == null ? DEFAULT_TRACK_GAP : trackGap.get();
    }

    /// Sets the gap token between active progress and track.
    public final void setTrackGap(double trackGap) {
        trackGapProperty().set(M3Css.nonNegative(trackGap, "trackGap"));
    }

    /// Returns the gap token property between active progress and track.
    public final StyleableDoubleProperty trackGapProperty() {
        if (trackGap == null) {
            trackGap = new StyleableDoubleProperty(DEFAULT_TRACK_GAP) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "trackGap");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "trackGap";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.TRACK_GAP;
                }
            };
        }
        return trackGap;
    }

    /// Returns the stop indicator size token.
    public final double getStopSize() {
        return stopSize == null ? DEFAULT_STOP_SIZE : stopSize.get();
    }

    /// Sets the stop indicator size token.
    public final void setStopSize(double stopSize) {
        stopSizeProperty().set(M3Css.nonNegative(stopSize, "stopSize"));
    }

    /// Returns the stop indicator size token property.
    public final StyleableDoubleProperty stopSizeProperty() {
        if (stopSize == null) {
            stopSize = new StyleableDoubleProperty(DEFAULT_STOP_SIZE) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "stopSize");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ProgressBar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "stopSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.STOP_SIZE;
                }
            };
        }
        return stopSize;
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

    /// Creates the default progress bar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ProgressBarSkin(this);
    }

    /// Returns accessibility attributes for the progress value and orientation.
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
            case ORIENTATION -> Orientation.HORIZONTAL;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
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
        double thickness = getTrackThickness();
        double height = thickness + getWaveAmplitude() * 2.0;
        setMinHeight(height);
        setPrefHeight(height);
        setMaxHeight(height);
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

    /// CSS metadata for m3fx progress bar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the track thickness token.
        private static final CssMetaData<M3ProgressBar, Number> TRACK_THICKNESS =
                new CssMetaData<>("-m3-track-thickness", SizeConverter.getInstance(), DEFAULT_TRACK_THICKNESS) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.trackThicknessProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.trackThicknessProperty();
                    }
                };

        /// CSS metadata for the track shape token.
        private static final CssMetaData<M3ProgressBar, Number> TRACK_SHAPE =
                new CssMetaData<>("-m3-track-shape", SizeConverter.getInstance(), DEFAULT_TRACK_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.trackShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.trackShapeProperty();
                    }
                };

        /// CSS metadata for the wave amplitude token.
        private static final CssMetaData<M3ProgressBar, Number> WAVE_AMPLITUDE =
                new CssMetaData<>("-m3-wave-amplitude", SizeConverter.getInstance(), DEFAULT_WAVE_AMPLITUDE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.waveAmplitudeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.waveAmplitudeProperty();
                    }
                };

        /// CSS metadata for the wavelength token.
        private static final CssMetaData<M3ProgressBar, Number> WAVELENGTH =
                new CssMetaData<>("-m3-wavelength", SizeConverter.getInstance(), DEFAULT_WAVELENGTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.wavelengthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.wavelengthProperty();
                    }
                };

        /// CSS metadata for the active-to-track gap token.
        private static final CssMetaData<M3ProgressBar, Number> TRACK_GAP =
                new CssMetaData<>("-m3-track-gap", SizeConverter.getInstance(), DEFAULT_TRACK_GAP) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.trackGapProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.trackGapProperty();
                    }
                };

        /// CSS metadata for the stop indicator size token.
        private static final CssMetaData<M3ProgressBar, Number> STOP_SIZE =
                new CssMetaData<>("-m3-stop-size", SizeConverter.getInstance(), DEFAULT_STOP_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.stopSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.stopSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(TRACK_SHAPE);
            styleables.add(WAVE_AMPLITUDE);
            styleables.add(WAVELENGTH);
            styleables.add(TRACK_GAP);
            styleables.add(STOP_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
