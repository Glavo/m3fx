// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.util.Duration;

import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Builds the Progress component showcase page.
@NotNullByDefault
final class ProgressDemoPage extends DemoPageSupport {
    /// Progress track heights shown by the width matrix.
    private static final @Unmodifiable List<Double> PROGRESS_TRACK_HEIGHTS =
            List.of(2.0, 4.0, 6.0, 8.0, 12.0);

    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ProgressDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the progress component page.
    Node createContent() {
        M3ProgressBar determinateBar = new M3ProgressBar(0.32);
        configureResponsiveWidth(determinateBar, 380.0);
        applyBaselineProgress(determinateBar);
        M3ProgressBar indeterminateBar = new M3ProgressBar();
        configureResponsiveWidth(indeterminateBar, 380.0);
        applyBaselineProgress(indeterminateBar);
        M3ProgressIndicator determinateIndicator = new M3ProgressIndicator(0.32);
        applyBaselineProgress(determinateIndicator);
        M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
        applyBaselineProgress(indeterminateIndicator);

        M3ProgressBar expressiveDeterminateBar = new M3ProgressBar(0.32);
        configureResponsiveWidth(expressiveDeterminateBar, 380.0);
        applyExpressiveLinearProgress(expressiveDeterminateBar);
        M3ProgressBar expressiveIndeterminateBar = new M3ProgressBar();
        configureResponsiveWidth(expressiveIndeterminateBar, 380.0);
        applyExpressiveLinearProgress(expressiveIndeterminateBar);
        M3ProgressIndicator expressiveDeterminateIndicator = new M3ProgressIndicator(0.32);
        applyExpressiveCircularProgress(expressiveDeterminateIndicator);
        M3ProgressIndicator expressiveIndeterminateIndicator = new M3ProgressIndicator();
        applyExpressiveCircularProgress(expressiveIndeterminateIndicator);

        playProgressShowcaseAnimation(determinateBar, determinateIndicator);
        playProgressShowcaseAnimation(expressiveDeterminateBar, expressiveDeterminateIndicator);

        return createGallery(
                createShowcaseGroup("Standard Linear", determinateBar, indeterminateBar),
                createShowcaseGroup("Standard Circular", determinateIndicator, indeterminateIndicator),
                createShowcaseGroup("Expressive Wavy Linear", expressiveDeterminateBar, expressiveIndeterminateBar),
                createShowcaseGroup("Track Heights", createProgressTrackHeightMatrix()),
                createShowcaseGroup(
                        "Expressive Wavy Circular",
                        expressiveDeterminateIndicator,
                        expressiveIndeterminateIndicator
                )
        );
    }

    /// Applies baseline linear progress geometry to a single demo progress bar.
    private static void applyBaselineProgress(M3ProgressBar progressBar) {
        progressBar.setStyle("-m3-wave-amplitude: 0px;");
    }

    /// Applies expressive wavy linear progress geometry to a single demo progress bar.
    private static void applyExpressiveLinearProgress(M3ProgressBar progressBar) {
        progressBar.setStyle("-m3-wave-amplitude: 3px; "
                + "-m3-wavelength: 40px; "
                + "-m3-indeterminate-wavelength: 20px; "
                + "-m3-track-gap: 4px; "
                + "-m3-stop-size: 4px;");
    }

    /// Applies expressive wavy circular progress geometry to a single demo progress indicator.
    private static void applyExpressiveCircularProgress(M3ProgressIndicator progressIndicator) {
        progressIndicator.setStyle("-m3-wave-amplitude: 1.6px; "
                + "-m3-wave-indicator-size: 48px; "
                + "-m3-wavelength: 15px; "
                + "-m3-track-gap: 4px;");
    }

    /// Creates the track height comparison matrix for progress indicators.
    private static VBox createProgressTrackHeightMatrix() {
        VBox matrix = new VBox(
                14.0,
                createProgressTrackHeightRow("Linear standard determinate", false, false, false),
                createProgressTrackHeightRow("Linear standard indeterminate", false, false, true),
                createProgressTrackHeightRow("Linear expressive determinate", false, true, false),
                createProgressTrackHeightRow("Linear expressive indeterminate", false, true, true),
                createProgressTrackHeightRow("Circular standard determinate", true, false, false),
                createProgressTrackHeightRow("Circular standard indeterminate", true, false, true),
                createProgressTrackHeightRow("Circular expressive determinate", true, true, false),
                createProgressTrackHeightRow("Circular expressive indeterminate", true, true, true)
        );
        matrix.setFillWidth(true);
        matrix.setMinWidth(0.0);
        matrix.setMaxWidth(Double.MAX_VALUE);
        return matrix;
    }

    /// Creates one row in the progress track height comparison matrix.
    private static VBox createProgressTrackHeightRow(
            String title,
            boolean circular,
            boolean expressive,
            boolean indeterminate
    ) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");

        FlowPane indicators = new FlowPane(16.0, 12.0);
        indicators.setAlignment(Pos.CENTER_LEFT);
        indicators.setMinWidth(0.0);
        indicators.setMaxWidth(Double.MAX_VALUE);
        for (double trackHeight : PROGRESS_TRACK_HEIGHTS) {
            indicators.getChildren().add(createProgressTrackHeightSample(
                    trackHeight,
                    circular,
                    expressive,
                    indeterminate
            ));
        }

        VBox row = new VBox(8.0, label, indicators);
        row.setMinWidth(0.0);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    /// Creates one labeled progress sample for a requested track height.
    private static VBox createProgressTrackHeightSample(
            double trackHeight,
            boolean circular,
            boolean expressive,
            boolean indeterminate
    ) {
        Node indicator;
        double sampleWidth;
        if (circular) {
            M3ProgressIndicator progressIndicator = indeterminate
                    ? new M3ProgressIndicator()
                    : new M3ProgressIndicator(0.62);
            if (expressive) {
                applyExpressiveCircularProgress(progressIndicator);
            } else {
                applyBaselineProgress(progressIndicator);
            }
            appendInlineStyle(progressIndicator, "-m3-track-thickness: " + trackHeight + "px;");
            indicator = progressIndicator;
            sampleWidth = 64.0;
        } else {
            M3ProgressBar progressBar = indeterminate ? new M3ProgressBar() : new M3ProgressBar(0.62);
            configureResponsiveWidth(progressBar, 180.0);
            if (expressive) {
                applyExpressiveLinearProgress(progressBar);
            } else {
                applyBaselineProgress(progressBar);
            }
            appendInlineStyle(progressBar, "-m3-track-thickness: " + trackHeight + "px;");
            indicator = progressBar;
            sampleWidth = 180.0;
        }

        Label heightLabel = new Label((int) trackHeight + " px");
        heightLabel.getStyleClass().add("demo-progress-track-height-label");
        VBox sample = new VBox(6.0, heightLabel, indicator);
        sample.setAlignment(Pos.CENTER_LEFT);
        sample.setMinWidth(0.0);
        sample.setPrefWidth(sampleWidth);
        sample.setMaxWidth(Double.MAX_VALUE);
        return sample;
    }

    /// Appends inline CSS to a node while preserving styles already applied by demo helpers.
    private static void appendInlineStyle(Node node, String style) {
        String currentStyle = node.getStyle();
        node.setStyle(currentStyle.isBlank() ? style : currentStyle + " " + style);
    }

    /// Plays the determinate progress showcase animation.
    private void playProgressShowcaseAnimation(M3ProgressBar progressBar, M3ProgressIndicator progressIndicator) {
        Timeline animation = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    progressBar.setProgress(0.08);
                    progressIndicator.setProgress(0.08);
                }),
                new KeyFrame(Duration.seconds(1.4), event -> {
                    progressBar.setProgress(0.86);
                    progressIndicator.setProgress(0.86);
                }),
                new KeyFrame(Duration.seconds(2.8), event -> {
                    progressBar.setProgress(0.24);
                    progressIndicator.setProgress(0.24);
                }),
                new KeyFrame(Duration.seconds(4.2), event -> {
                    progressBar.setProgress(0.68);
                    progressIndicator.setProgress(0.68);
                }),
                new KeyFrame(Duration.seconds(5.6), event -> {
                    progressBar.setProgress(0.08);
                    progressIndicator.setProgress(0.08);
                })
        );
        animation.setCycleCount(Animation.INDEFINITE);
        context.registerAnimation(animation);
    }

    /// Applies baseline circular progress geometry to a single demo progress indicator.
    private static void applyBaselineProgress(M3ProgressIndicator progressIndicator) {
        progressIndicator.setStyle("-m3-wave-amplitude: 0px;");
    }
}
