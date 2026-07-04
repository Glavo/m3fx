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
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ProgressIndicatorSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 circular progress indicator.
///
/// `M3ProgressIndicator` displays determinate progress for values from `0.0` to `1.0` and indeterminate
/// progress when the value is [INDETERMINATE_PROGRESS]. The control exposes token-backed indicator size, track
/// thickness, active gap, and optional wave properties so the same API can render baseline and expressive
/// circular progress styles.
///
/// Use this control when loading feedback needs a compact circular affordance. Indeterminate progress keeps a
/// basic rotating sweep when full animations are disabled through [org.glavo.m3fx.animation.M3MotionSettings],
/// so reduced-motion mode still communicates activity. Positive wave amplitude and wavelength values enable
/// the M3 Expressive wavy circular geometry used by the generated component tokens. See
/// [Material Design progress indicators](https://m3.material.io/components/progress-indicators/overview).
@NotNullByDefault
public class M3ProgressIndicator extends Control {
    /// The base style class for M3FX progress indicators.
    public static final String STYLE_CLASS = "m3-progress-indicator";

    /// The progress value that marks the control as indeterminate.
    public static final double INDETERMINATE_PROGRESS = -1.0;

    /// The pseudo class applied while progress is indeterminate.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("indeterminate");

    /// The default circular indicator stroke thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 4.0;

    /// The default circular indicator size.
    private static final double DEFAULT_INDICATOR_SIZE = 40.0;

    /// The default circular wave amplitude.
    private static final double DEFAULT_WAVE_AMPLITUDE = 0.0;

    /// The default circular wavelength.
    private static final double DEFAULT_WAVELENGTH = 15.0;

    /// The default gap between active progress and track.
    private static final double DEFAULT_TRACK_GAP = 4.0;

    /// The minimum accessible progress value.
    private static final double ACCESSIBLE_MIN_VALUE = 0.0;

    /// The maximum accessible progress value.
    private static final double ACCESSIBLE_MAX_VALUE = 1.0;

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    // The current progress value.
    private @Nullable DoubleProperty progress;

    // The styleable circular indicator stroke thickness token.
    private @Nullable StyleableDoubleProperty trackThickness;

    // The styleable indicator size token.
    private @Nullable StyleableDoubleProperty indicatorSize;

    // The styleable wave amplitude token.
    private @Nullable StyleableDoubleProperty waveAmplitude;

    // The styleable wavelength token.
    private @Nullable StyleableDoubleProperty wavelength;

    // The styleable active-to-track gap token.
    private @Nullable StyleableDoubleProperty trackGap;

    /// Creates an indeterminate progress indicator.
    public M3ProgressIndicator() {
        initialize();
    }

    /// Creates a progress indicator with an initial progress value.
    ///
    /// @param progress the initial progress value, from `0.0` to `1.0`, or [INDETERMINATE_PROGRESS]
    public M3ProgressIndicator(double progress) {
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
                    M3Accessible.notifyAttribute(M3ProgressIndicator.this, VALUE_STRING_ATTRIBUTE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
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
    ///
    /// @return `true` when the current progress value is indeterminate
    public final boolean isIndeterminate() {
        return getProgress() == INDETERMINATE_PROGRESS;
    }

    /// Returns the circular indicator stroke thickness token.
    ///
    /// @return the circular indicator stroke thickness in pixels
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the circular indicator stroke thickness token.
    ///
    /// @param trackThickness the circular indicator stroke thickness in pixels
    public final void setTrackThickness(double trackThickness) {
        trackThicknessProperty().set(M3Css.nonNegative(trackThickness, "trackThickness"));
    }

    /// Returns the circular indicator stroke thickness token property.
    ///
    /// @return the styleable circular indicator stroke thickness property
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

    /// Returns the circular indicator size token.
    ///
    /// @return the circular indicator size in pixels
    public final double getIndicatorSize() {
        return indicatorSize == null ? DEFAULT_INDICATOR_SIZE : indicatorSize.get();
    }

    /// Sets the circular indicator size token.
    ///
    /// @param indicatorSize the circular indicator size in pixels
    public final void setIndicatorSize(double indicatorSize) {
        indicatorSizeProperty().set(M3Css.nonNegative(indicatorSize, "indicatorSize"));
    }

    /// Returns the circular indicator size token property.
    ///
    /// @return the styleable circular indicator size property
    public final StyleableDoubleProperty indicatorSizeProperty() {
        if (indicatorSize == null) {
            indicatorSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INDICATOR_SIZE,
                    this,
                    "indicatorSize",
                    StyleableProperties.INDICATOR_SIZE,
                    this::updateMetrics
            );
        }
        return indicatorSize;
    }

    /// Returns the wavy progress amplitude token.
    ///
    /// @return the wavy progress amplitude in pixels
    public final double getWaveAmplitude() {
        return waveAmplitude == null ? DEFAULT_WAVE_AMPLITUDE : waveAmplitude.get();
    }

    /// Sets the wavy progress amplitude token.
    ///
    /// @param waveAmplitude the wavy progress amplitude in pixels
    public final void setWaveAmplitude(double waveAmplitude) {
        waveAmplitudeProperty().set(M3Css.nonNegative(waveAmplitude, "waveAmplitude"));
    }

    /// Returns the wavy progress amplitude token property.
    ///
    /// @return the styleable wavy progress amplitude property
    public final StyleableDoubleProperty waveAmplitudeProperty() {
        if (waveAmplitude == null) {
            waveAmplitude = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_WAVE_AMPLITUDE,
                    this,
                    "waveAmplitude",
                    StyleableProperties.WAVE_AMPLITUDE,
                    this::requestLayout
            );
        }
        return waveAmplitude;
    }

    /// Returns the wavy progress wavelength token.
    ///
    /// @return the wavy progress wavelength in pixels
    public final double getWavelength() {
        return wavelength == null ? DEFAULT_WAVELENGTH : wavelength.get();
    }

    /// Sets the wavy progress wavelength token.
    ///
    /// @param wavelength the wavy progress wavelength in pixels
    public final void setWavelength(double wavelength) {
        wavelengthProperty().set(M3Css.nonNegative(wavelength, "wavelength"));
    }

    /// Returns the wavy progress wavelength token property.
    ///
    /// @return the styleable wavy progress wavelength property
    public final StyleableDoubleProperty wavelengthProperty() {
        if (wavelength == null) {
            wavelength = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_WAVELENGTH,
                    this,
                    "wavelength",
                    StyleableProperties.WAVELENGTH,
                    this::requestLayout
            );
        }
        return wavelength;
    }

    /// Returns the gap token between active progress and track.
    ///
    /// @return the gap between active progress and track in pixels
    public final double getTrackGap() {
        return trackGap == null ? DEFAULT_TRACK_GAP : trackGap.get();
    }

    /// Sets the gap token between active progress and track.
    ///
    /// @param trackGap the gap between active progress and track in pixels
    public final void setTrackGap(double trackGap) {
        trackGapProperty().set(M3Css.nonNegative(trackGap, "trackGap"));
    }

    /// Returns the gap token property between active progress and track.
    ///
    /// @return the styleable active-to-track gap property
    public final StyleableDoubleProperty trackGapProperty() {
        if (trackGap == null) {
            trackGap = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TRACK_GAP,
                    this,
                    "trackGap",
                    StyleableProperties.TRACK_GAP,
                    this::requestLayout
            );
        }
        return trackGap;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3ProgressIndicator`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default progress indicator skin.
    ///
    /// @return the default progress indicator skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ProgressIndicatorSkin(this);
    }

    /// Returns accessibility attributes for the progress value.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            return isIndeterminate() ? "Indeterminate" : Math.round(getProgress() * 100.0) + "%";
        }
        return switch (attribute) {
            case INDETERMINATE -> isIndeterminate();
            case MIN_VALUE -> ACCESSIBLE_MIN_VALUE;
            case MAX_VALUE -> ACCESSIBLE_MAX_VALUE;
            case VALUE -> getProgress();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Returns the user-agent stylesheet for M3FX progress controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("progress.css");
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        setFocusTraversable(false);
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

    /// CSS metadata for M3FX progress indicator component tokens.
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

        /// CSS metadata for the wave amplitude token.
        private static final CssMetaData<M3ProgressIndicator, Number> WAVE_AMPLITUDE =
                new CssMetaData<>("-m3-wave-amplitude", SizeConverter.getInstance(), DEFAULT_WAVE_AMPLITUDE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressIndicator control) {
                        return M3Css.isSettable(control.waveAmplitudeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressIndicator control) {
                        return control.waveAmplitudeProperty();
                    }
                };

        /// CSS metadata for the wavelength token.
        private static final CssMetaData<M3ProgressIndicator, Number> WAVELENGTH =
                new CssMetaData<>("-m3-wavelength", SizeConverter.getInstance(), DEFAULT_WAVELENGTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressIndicator control) {
                        return M3Css.isSettable(control.wavelengthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressIndicator control) {
                        return control.wavelengthProperty();
                    }
                };

        /// CSS metadata for the active-to-track gap token.
        private static final CssMetaData<M3ProgressIndicator, Number> TRACK_GAP =
                new CssMetaData<>("-m3-track-gap", SizeConverter.getInstance(), DEFAULT_TRACK_GAP) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressIndicator control) {
                        return M3Css.isSettable(control.trackGapProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressIndicator control) {
                        return control.trackGapProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(TRACK_THICKNESS);
            styleables.add(INDICATOR_SIZE);
            styleables.add(WAVE_AMPLITUDE);
            styleables.add(WAVELENGTH);
            styleables.add(TRACK_GAP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
