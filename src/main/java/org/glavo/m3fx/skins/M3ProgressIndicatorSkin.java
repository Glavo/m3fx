// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ProgressIndicator].
@NotNullByDefault
public class M3ProgressIndicatorSkin extends SkinBase<M3ProgressIndicator> {
    /// The shortest visible sweep used by indeterminate progress.
    private static final double INDETERMINATE_MIN_SWEEP = 42.0;

    /// The longest visible sweep used by indeterminate progress.
    private static final double INDETERMINATE_MAX_SWEEP = 96.0;

    /// The fixed sweep used by reduced indeterminate progress.
    private static final double BASIC_INDETERMINATE_SWEEP = 72.0;

    /// The phase used at both ends of an indeterminate progress cycle.
    private static final double INDETERMINATE_START_PHASE = 0.0;

    /// The fixed sample count for circular inactive track paths.
    private static final int CIRCULAR_TRACK_SAMPLE_STEPS = 72;

    /// The fixed sample count for circular active indicator paths.
    private static final int CIRCULAR_INDICATOR_SAMPLE_STEPS = 32;

    /// The track circle.
    private final Circle track = new Circle();

    /// The progress arc.
    private final Arc indicator = new Arc();

    /// The expressive circular track path with a gap around the active indicator.
    private final Path waveTrack = new Path();

    /// The expressive wavy active indicator path.
    private final Path waveIndicator = new Path();

    // The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition timeline.
    private final Timeline determinateAnimation = new Timeline();

    // The animated phase used by indeterminate progress.
    private final DoubleProperty indeterminatePhase =
            new SimpleDoubleProperty(this, "indeterminatePhase", INDETERMINATE_START_PHASE);

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after animation ticks.
    private final InvalidationListener animationInvalidation =
            observable -> getSkinnable().requestLayout();

    /// Updates animations when the public progress value changes.
    private final InvalidationListener progressInvalidation = observable -> updateProgressAnimation(true);

    /// Updates indeterminate animation state when global or node-local motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(getSkinnable(), () -> updateProgressAnimation(false));

    /// Requests layout after size-related token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a progress indicator skin.
    ///
    /// @param control the skinned progress indicator
    public M3ProgressIndicatorSkin(M3ProgressIndicator control) {
        super(control);
        track.getStyleClass().add("track");
        indicator.getStyleClass().add("indicator");
        waveTrack.getStyleClass().add("m3-progress-indicator-track-wave");
        waveIndicator.getStyleClass().add("m3-progress-indicator-wave");
        track.setManaged(false);
        indicator.setManaged(false);
        waveTrack.setManaged(false);
        waveIndicator.setManaged(false);
        track.setFill(Color.TRANSPARENT);
        indicator.setFill(Color.TRANSPARENT);
        waveTrack.setFill(null);
        waveIndicator.setFill(null);
        track.setStrokeLineCap(StrokeLineCap.ROUND);
        indicator.setStrokeLineCap(StrokeLineCap.ROUND);
        waveTrack.setStrokeLineCap(StrokeLineCap.ROUND);
        waveIndicator.setStrokeLineCap(StrokeLineCap.ROUND);
        indicator.setType(ArcType.OPEN);
        getChildren().addAll(track, indicator, waveTrack, waveIndicator);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePhase.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);

        control.progressProperty().addListener(progressInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        control.waveAmplitudeProperty().addListener(layoutInvalidation);
        control.wavelengthProperty().addListener(layoutInvalidation);
        control.trackGapProperty().addListener(layoutInvalidation);
        updateProgressAnimation(false);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressIndicator progressIndicator = getSkinnable();
        determinateAnimation.stop();
        indeterminateAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        indeterminatePhase.removeListener(animationInvalidation);
        progressIndicator.progressProperty().removeListener(progressInvalidation);
        motionSettingsObserver.dispose();
        progressIndicator.trackThicknessProperty().removeListener(layoutInvalidation);
        progressIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        progressIndicator.waveAmplitudeProperty().removeListener(layoutInvalidation);
        progressIndicator.wavelengthProperty().removeListener(layoutInvalidation);
        progressIndicator.trackGapProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the circular track and indicator arc.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double size = Math.min(width, height);
        double strokeWidth = Math.min(getSkinnable().getTrackThickness(), size);
        double radius = Math.max(0.0, (size - strokeWidth) / 2.0);
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        if (getSkinnable().getWaveAmplitude() > 0.0) {
            layoutWavyIndicator(centerX, centerY, radius, strokeWidth);
            return;
        }

        waveTrack.setVisible(false);
        waveIndicator.setVisible(false);
        track.setCenterX(centerX);
        track.setCenterY(centerY);
        track.setRadius(radius);
        track.setStrokeWidth(strokeWidth);

        indicator.setCenterX(centerX);
        indicator.setCenterY(centerY);
        indicator.setRadiusX(radius);
        indicator.setRadiusY(radius);
        indicator.setStrokeWidth(strokeWidth);
        updateIndicatorArc();
    }

    /// Lays out expressive wavy circular progress paths.
    private void layoutWavyIndicator(double centerX, double centerY, double radius, double strokeWidth) {
        M3ProgressIndicator progressIndicator = getSkinnable();
        track.setVisible(false);
        indicator.setVisible(false);
        waveTrack.setStrokeWidth(strokeWidth);
        waveIndicator.setStrokeWidth(strokeWidth);

        double progress = progressIndicator.getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            double sweepFraction = indeterminateSweep(
                    indeterminatePhase.get(),
                    !M3Animation.shouldReduceMotion(progressIndicator)
            ) / 360.0;
            double start = indeterminatePhase.get();
            layoutCircularTrackPath(waveTrack, centerX, centerY, radius, strokeWidth, start, start + sweepFraction);
            layoutCircularWavePath(
                    waveIndicator,
                    centerX,
                    centerY,
                    radius,
                    progressIndicator.getWaveAmplitude(),
                    progressIndicator.getWavelength(),
                    start,
                    start + sweepFraction,
                    indeterminatePhase.get()
            );
            return;
        }

        double displayed = displayedProgress.get();
        layoutCircularTrackPath(waveTrack, centerX, centerY, radius, strokeWidth, 0.0, displayed);
        layoutCircularWavePath(
                waveIndicator,
                centerX,
                centerY,
                radius,
                amplitudeForProgress(displayed) * progressIndicator.getWaveAmplitude(),
                progressIndicator.getWavelength(),
                0.0,
                displayed,
                0.0
        );
    }

    /// Updates the visible arc for determinate or indeterminate progress.
    private void updateIndicatorArc() {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            track.setVisible(false);
            indicator.setStartAngle(90.0 - 360.0 * indeterminatePhase.get());
            indicator.setLength(-indeterminateSweep(
                    indeterminatePhase.get(),
                    !M3Animation.shouldReduceMotion(getSkinnable())
            ));
            return;
        }

        track.setVisible(true);
        indicator.setStartAngle(90.0);
        indicator.setLength(-360.0 * displayedProgress.get());
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            startIndeterminateAnimation();
            getSkinnable().requestLayout();
        } else {
            indeterminateAnimation.stop();
            indeterminatePhase.set(INDETERMINATE_START_PHASE);
            animateDisplayedProgress(clamp(progress), animateDeterminateProgress);
        }
    }

    /// Starts the indeterminate linear phase loop.
    private void startIndeterminateAnimation() {
        indeterminateAnimation.stop();
        configureIndeterminateAnimation();
        indeterminateAnimation.playFromStart();
    }

    /// Configures the indeterminate sweep with the current owner behavior timing.
    private void configureIndeterminateAnimation() {
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePhase, INDETERMINATE_START_PHASE, M3Motion.LINEAR)
                ),
                new KeyFrame(
                        M3Animation.motionBehavior(getSkinnable()).circularProgressIndeterminateCycleDuration(),
                        new KeyValue(indeterminatePhase, 1.0, M3Motion.LINEAR)
                )
        );
    }

    /// Animates the displayed determinate progress value.
    private void animateDisplayedProgress(double targetProgress, boolean animate) {
        determinateAnimation.stop();
        if (!animate || M3Animation.shouldReduceMotion(getSkinnable())) {
            displayedProgress.set(targetProgress);
            return;
        }

        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        determinateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        spec.duration(),
                        new KeyValue(displayedProgress, targetProgress, spec.interpolator())
                )
        );
        M3Animation.playFromStart(getSkinnable(), determinateAnimation);
    }

    /// Returns the initial displayed progress value for a public progress value.
    private static double initialDisplayedProgress(double progress) {
        return progress == M3ProgressIndicator.INDETERMINATE_PROGRESS ? 0.0 : clamp(progress);
    }

    /// Returns the animated sweep for an indeterminate progress phase.
    private static double indeterminateSweep(double phase, boolean fullMotion) {
        if (!fullMotion) {
            return BASIC_INDETERMINATE_SWEEP;
        }

        double wave = 0.5 - Math.cos(phase * Math.PI * 2.0) / 2.0;
        return INDETERMINATE_MIN_SWEEP
                + (INDETERMINATE_MAX_SWEEP - INDETERMINATE_MIN_SWEEP) * wave;
    }

    /// Returns the resolved active wave amplitude for a determinate progress value.
    private static double amplitudeForProgress(double progress) {
        return progress <= 0.1 || progress >= 0.95 ? 0.0 : 1.0;
    }

    /// Lays out the circular track path outside the active indicator and gap.
    private void layoutCircularTrackPath(
            Path path,
            double centerX,
            double centerY,
            double radius,
            double strokeWidth,
            double activeStart,
            double activeEnd
    ) {
        double gapFraction = circularGapFraction(radius, getSkinnable().getTrackGap(), strokeWidth);
        double start = activeEnd + gapFraction;
        double end = activeStart + 1.0 - gapFraction;
        if (radius <= 0.0 || end <= start) {
            path.setVisible(false);
            return;
        }

        path.setVisible(true);
        writeCircularSegment(
                path,
                CIRCULAR_TRACK_SAMPLE_STEPS,
                centerX,
                centerY,
                radius,
                0.0,
                1.0,
                start,
                end,
                0.0
        );
    }

    /// Lays out a circular wavy active indicator path.
    private static void layoutCircularWavePath(
            Path path,
            double centerX,
            double centerY,
            double radius,
            double amplitude,
            double wavelength,
            double start,
            double end,
            double phase
    ) {
        if (radius <= 0.0 || end <= start) {
            path.setVisible(false);
            return;
        }

        path.setVisible(true);
        writeCircularSegment(
                path,
                CIRCULAR_INDICATOR_SAMPLE_STEPS,
                centerX,
                centerY,
                radius,
                amplitude,
                wavelength,
                start,
                end,
                phase
        );
    }

    /// Writes a sampled circular segment to a reusable path.
    private static void writeCircularSegment(
            Path path,
            int steps,
            double centerX,
            double centerY,
            double radius,
            double amplitude,
            double wavelength,
            double start,
            double end,
            double phase
    ) {
        double circumference = Math.max(1.0, Math.PI * 2.0 * radius);
        double waves = Math.max(1.0, circumference / Math.max(1.0, wavelength));
        int resolvedSteps = Math.max(4, steps);
        ObservableList<PathElement> elements = path.getElements();
        ensureSampledPathElements(elements, resolvedSteps + 1);
        for (int i = 0; i <= resolvedSteps; i++) {
            double fraction = (double) i / (double) resolvedSteps;
            double progress = start + (end - start) * fraction;
            double angle = Math.toRadians(90.0 - 360.0 * progress);
            double waveRadius = radius + Math.sin((progress * waves + phase) * Math.PI * 2.0) * amplitude;
            double x = centerX + Math.cos(angle) * waveRadius;
            double y = centerY - Math.sin(angle) * waveRadius;
            setSampledPathPoint(elements.get(i), x, y);
        }
    }

    /// Ensures that a sampled path contains one `MoveTo` followed by reusable `LineTo` elements.
    private static void ensureSampledPathElements(ObservableList<PathElement> elements, int count) {
        if (count < 1) {
            elements.clear();
            return;
        }

        if (elements.isEmpty()) {
            elements.add(new MoveTo());
        } else if (!(elements.get(0) instanceof MoveTo)) {
            elements.set(0, new MoveTo());
        }

        for (int i = 1; i < count; i++) {
            if (i >= elements.size()) {
                elements.add(new LineTo());
            } else if (!(elements.get(i) instanceof LineTo)) {
                elements.set(i, new LineTo());
            }
        }

        if (elements.size() > count) {
            elements.remove(count, elements.size());
        }
    }

    /// Updates the coordinates of a reusable sampled path element.
    private static void setSampledPathPoint(PathElement element, double x, double y) {
        if (element instanceof MoveTo moveTo) {
            moveTo.setX(x);
            moveTo.setY(y);
            return;
        }
        if (element instanceof LineTo lineTo) {
            lineTo.setX(x);
            lineTo.setY(y);
            return;
        }
        throw new IllegalArgumentException("Unsupported sampled path element: " + element);
    }

    /// Converts a circular gap in pixels to a normalized circumference fraction.
    private static double circularGapFraction(double radius, double gap, double strokeWidth) {
        if (radius <= 0.0 || gap <= 0.0) {
            return 0.0;
        }
        double capCompensatedGap = gap + Math.max(0.0, strokeWidth);
        return Math.min(0.20, capCompensatedGap / (Math.PI * 2.0 * radius));
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
