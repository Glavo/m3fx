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
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ProgressBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 linear progress indicator.
///
/// `M3ProgressBar` displays determinate progress for values from `0.0` to `1.0` and indeterminate progress
/// when the value is [INDETERMINATE_PROGRESS]. The control exposes token-backed track thickness, shape, active
/// gap, stop indicator, and optional wave properties so the same API can render baseline and expressive
/// progress styles.
///
/// Use this control for horizontal loading feedback in a bounded area. Indeterminate progress keeps a basic
/// moving segment when reduced motion is requested through [org.glavo.m3fx.animation.M3MotionSettings], so
/// reduced-motion mode still communicates activity. A positive wave amplitude explicitly enables the M3
/// Expressive wavy geometry; the Flat configuration remains the default in every theme profile. See
/// [Material Design progress indicators](https://m3.material.io/components/progress-indicators/overview).
@NotNullByDefault
public final class M3ProgressBar extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-progress-bar";

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

    /// The default indeterminate linear wave length.
    private static final double DEFAULT_INDETERMINATE_WAVELENGTH = 20.0;

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

    /// Creates an indeterminate progress bar.
    public M3ProgressBar() {
        initialize();
    }

    /// Creates a progress bar with an initial progress value.
    ///
    /// Values greater than `1.0` are normalized to `1.0`; negative values and `NaN` select indeterminate mode.
    ///
    /// @param progress the initial progress value
    public M3ProgressBar(double progress) {
        initialize();
        setProgress(progress);
    }

    /// The current progress value.
    ///
    /// Values in the inclusive range `0.0` to `1.0` represent determinate completion. Values greater than `1.0`
    /// are normalized to `1.0`; negative values and `NaN` are normalized to [INDETERMINATE_PROGRESS].
    ///
    /// @defaultValue [INDETERMINATE_PROGRESS]
    private @Nullable DoubleProperty progress;

    /// Returns the current progress value.
    ///
    /// @return the current progress value, or [INDETERMINATE_PROGRESS]
    public final double getProgress() {
        return progress == null ? INDETERMINATE_PROGRESS : progress.get();
    }

    /// Sets the current progress value.
    ///
    /// Values greater than `1.0` are normalized to `1.0`; negative values and `NaN` select indeterminate mode.
    ///
    /// @param progress the progress value
    public final void setProgress(double progress) {
        progressProperty().set(progress);
    }

    /// Returns the observable, bindable progress property.
    ///
    /// The property is [INDETERMINATE_PROGRESS] by default. Directly assigned values above `1.0` are normalized to
    /// `1.0`; negative values and `NaN` are normalized to [INDETERMINATE_PROGRESS]. A binding source must provide
    /// either [INDETERMINATE_PROGRESS] or a value in `0.0..1.0`. Valid changes update accessibility,
    /// pseudo-class state, and layout.
    ///
    /// @return the progress property
    public final DoubleProperty progressProperty() {
        if (progress == null) {
            progress = new DoublePropertyBase(INDETERMINATE_PROGRESS) {
                /// Normalizes progress and updates the indeterminate pseudo class.
                @Override
                protected void invalidated() {
                    double normalizedProgress = normalizeProgress(get());
                    if (Double.compare(normalizedProgress, get()) != 0) {
                        if (isBound()) {
                            return;
                        }
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

    /// The track and active-indicator thickness, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-track-thickness`.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty trackThickness;

    /// Returns the progress track thickness token.
    ///
    /// @return the progress track thickness in logical pixels
    public final double getTrackThickness() {
        return trackThickness == null ? DEFAULT_TRACK_THICKNESS : trackThickness.get();
    }

    /// Sets the progress track thickness token.
    ///
    /// @param trackThickness the progress track thickness in logical pixels
    /// @throws IllegalArgumentException if `trackThickness` is negative or not finite
    public final void setTrackThickness(double trackThickness) {
        trackThicknessProperty().set(M3Css.nonNegative(trackThickness, "trackThickness"));
    }

    /// Returns the observable, bindable, CSS-styleable track-thickness property.
    ///
    /// The property is `4.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-track-thickness`.
    ///
    /// @return the track-thickness property
    public final StyleableDoubleProperty trackThicknessProperty() {
        if (trackThickness == null) {
            trackThickness = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TRACK_THICKNESS,
                    this,
                    "trackThickness",
                    StyleableProperties.TRACK_THICKNESS,
                    this::updateMetrics
            );
        }
        return trackThickness;
    }

    /// The track corner radius, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-track-shape`.
    ///
    /// @defaultValue `999.0`
    private @Nullable StyleableDoubleProperty trackShape;

    /// Returns the progress track shape radius token.
    ///
    /// @return the progress track corner radius in logical pixels
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the progress track shape radius token.
    ///
    /// @param trackShape the progress track corner radius in logical pixels
    /// @throws IllegalArgumentException if `trackShape` is negative or not finite
    public final void setTrackShape(double trackShape) {
        trackShapeProperty().set(M3Css.nonNegative(trackShape, "trackShape"));
    }

    /// Returns the observable, bindable, CSS-styleable track-shape property.
    ///
    /// The property is `999.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-track-shape`.
    ///
    /// @return the track-shape property
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

    /// The expressive wave amplitude, in logical pixels.
    ///
    /// A value of `0.0` selects flat geometry. The value must be finite and non-negative and is styleable through
    /// `-m3-wave-amplitude`.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty waveAmplitude;

    /// Returns the wavy progress amplitude token.
    ///
    /// @return the wavy progress amplitude in logical pixels
    public final double getWaveAmplitude() {
        return waveAmplitude == null ? DEFAULT_WAVE_AMPLITUDE : waveAmplitude.get();
    }

    /// Sets the wavy progress amplitude token.
    ///
    /// @param waveAmplitude the wavy progress amplitude in logical pixels
    /// @throws IllegalArgumentException if `waveAmplitude` is negative or not finite
    public final void setWaveAmplitude(double waveAmplitude) {
        waveAmplitudeProperty().set(M3Css.nonNegative(waveAmplitude, "waveAmplitude"));
    }

    /// Returns the observable, bindable, CSS-styleable wave-amplitude property.
    ///
    /// The property is `0.0` logical pixels by default, which selects flat geometry. It accepts only finite
    /// non-negative values and is styleable through `-m3-wave-amplitude`.
    ///
    /// @return the wave-amplitude property
    public final StyleableDoubleProperty waveAmplitudeProperty() {
        if (waveAmplitude == null) {
            waveAmplitude = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_WAVE_AMPLITUDE,
                    this,
                    "waveAmplitude",
                    StyleableProperties.WAVE_AMPLITUDE,
                    () -> {
                        updateMetrics();
                        requestLayout();
                    }
            );
        }
        return waveAmplitude;
    }

    /// The determinate wave length, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-wavelength`.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty wavelength;

    /// Returns the wavy progress wavelength token.
    ///
    /// @return the wavy progress wavelength in logical pixels
    public final double getWavelength() {
        return wavelength == null ? DEFAULT_WAVELENGTH : wavelength.get();
    }

    /// Sets the wavy progress wavelength token.
    ///
    /// @param wavelength the wavy progress wavelength in logical pixels
    /// @throws IllegalArgumentException if `wavelength` is negative or not finite
    public final void setWavelength(double wavelength) {
        wavelengthProperty().set(M3Css.nonNegative(wavelength, "wavelength"));
    }

    /// Returns the observable, bindable, CSS-styleable determinate wavelength property.
    ///
    /// The property is `40.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-wavelength`.
    ///
    /// @return the determinate wavelength property
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

    /// The indeterminate wave length, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-indeterminate-wavelength`.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty indeterminateWavelength;

    /// Returns the wavy indeterminate progress wavelength token.
    ///
    /// @return the wavy indeterminate progress wavelength in logical pixels
    public final double getIndeterminateWavelength() {
        return indeterminateWavelength == null
                ? DEFAULT_INDETERMINATE_WAVELENGTH
                : indeterminateWavelength.get();
    }

    /// Sets the wavy indeterminate progress wavelength token.
    ///
    /// @param indeterminateWavelength the wavy indeterminate progress wavelength in logical pixels
    /// @throws IllegalArgumentException if `indeterminateWavelength` is negative or not finite
    public final void setIndeterminateWavelength(double indeterminateWavelength) {
        indeterminateWavelengthProperty().set(M3Css.nonNegative(
                indeterminateWavelength,
                "indeterminateWavelength"
        ));
    }

    /// Returns the observable, bindable, CSS-styleable indeterminate wavelength property.
    ///
    /// The property is `20.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-indeterminate-wavelength`.
    ///
    /// @return the indeterminate wavelength property
    public final StyleableDoubleProperty indeterminateWavelengthProperty() {
        if (indeterminateWavelength == null) {
            indeterminateWavelength = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INDETERMINATE_WAVELENGTH,
                    this,
                    "indeterminateWavelength",
                    StyleableProperties.INDETERMINATE_WAVELENGTH,
                    this::requestLayout
            );
        }
        return indeterminateWavelength;
    }

    /// The visual gap between active progress and inactive track, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-track-gap`.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty trackGap;

    /// Returns the gap token between active progress and track.
    ///
    /// @return the gap between active progress and track in logical pixels
    public final double getTrackGap() {
        return trackGap == null ? DEFAULT_TRACK_GAP : trackGap.get();
    }

    /// Sets the gap token between active progress and track.
    ///
    /// @param trackGap the gap between active progress and track in logical pixels
    /// @throws IllegalArgumentException if `trackGap` is negative or not finite
    public final void setTrackGap(double trackGap) {
        trackGapProperty().set(M3Css.nonNegative(trackGap, "trackGap"));
    }

    /// Returns the observable, bindable, CSS-styleable track-gap property.
    ///
    /// The property is `4.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-track-gap`.
    ///
    /// @return the track-gap property
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

    /// The stop-indicator diameter, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-stop-size`.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty stopSize;

    /// Returns the stop indicator size token.
    ///
    /// @return the stop indicator size in logical pixels
    public final double getStopSize() {
        return stopSize == null ? DEFAULT_STOP_SIZE : stopSize.get();
    }

    /// Sets the stop indicator size token.
    ///
    /// @param stopSize the stop indicator size in logical pixels
    /// @throws IllegalArgumentException if `stopSize` is negative or not finite
    public final void setStopSize(double stopSize) {
        stopSizeProperty().set(M3Css.nonNegative(stopSize, "stopSize"));
    }

    /// Returns the observable, bindable, CSS-styleable stop-indicator size property.
    ///
    /// The property is `4.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-stop-size`.
    ///
    /// @return the stop-indicator size property
    public final StyleableDoubleProperty stopSizeProperty() {
        if (stopSize == null) {
            stopSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_STOP_SIZE,
                    this,
                    "stopSize",
                    StyleableProperties.STOP_SIZE,
                    this::requestLayout
            );
        }
        return stopSize;
    }

    /// Returns whether the current progress value is indeterminate.
    ///
    /// @return `true` when the current progress value is indeterminate
    public final boolean isIndeterminate() {
        return getProgress() == INDETERMINATE_PROGRESS;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3ProgressBar`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default progress bar skin.
    ///
    /// @return the default progress bar skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ProgressBarSkin(this);
    }

    /// Returns accessibility attributes for the progress value and orientation.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
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
            case ORIENTATION -> Orientation.HORIZONTAL;
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
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        setFocusTraversable(false);
        pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double thickness = getTrackThickness();
        double height = thickness + getWaveAmplitude() * 2.0;
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setMaxHeightIfUnbound(this, height);
    }

    /// Normalizes progress values to the supported range.
    private static double normalizeProgress(double progress) {
        if (Double.isNaN(progress) || progress < 0.0) {
            return INDETERMINATE_PROGRESS;
        }
        return Math.min(1.0, progress);
    }

    /// CSS metadata for M3FX progress bar component tokens.
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

        /// CSS metadata for the indeterminate wavelength token.
        private static final CssMetaData<M3ProgressBar, Number> INDETERMINATE_WAVELENGTH =
                new CssMetaData<>(
                        "-m3-indeterminate-wavelength",
                        SizeConverter.getInstance(),
                        DEFAULT_INDETERMINATE_WAVELENGTH
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ProgressBar control) {
                        return M3Css.isSettable(control.indeterminateWavelengthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ProgressBar control) {
                        return control.indeterminateWavelengthProperty();
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
            styleables.add(INDETERMINATE_WAVELENGTH);
            styleables.add(TRACK_GAP);
            styleables.add(STOP_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
