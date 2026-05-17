// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3Badge;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Badge].
@NotNullByDefault
public class M3BadgeSkin extends SkinBase<M3Badge> {
    /// The badge content change transition duration.
    private static final Duration CONTENT_CHANGE_DURATION = M3Motion.SHORT2;

    /// The initial scale used when badge content changes.
    private static final double CONTENT_CHANGE_START_SCALE = 0.86;

    /// The visible badge label.
    private final Label label = new Label();

    /// The badge content change animation.
    private final Timeline contentAnimation = new Timeline();

    /// Updates text and metrics after display text inputs change.
    private final InvalidationListener textInvalidation = observable -> updateTextAndMetrics();

    /// Applies size token changes to badge geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// The currently rendered display text.
    private String currentDisplayText = "";

    /// Creates a badge skin.
    public M3BadgeSkin(M3Badge control) {
        super(control);
        label.getStyleClass().add("m3-badge-label");
        getChildren().add(label);

        currentDisplayText = control.getDisplayText();
        updateText();
        updateMetrics();
        control.textProperty().addListener(textInvalidation);
        control.maxCharacterCountProperty().addListener(textInvalidation);
        control.smallSizeProperty().addListener(metricsInvalidation);
        control.largeHeightProperty().addListener(metricsInvalidation);
        control.largeMinWidthProperty().addListener(metricsInvalidation);
        control.containerShapeProperty().addListener(metricsInvalidation);
        control.horizontalPaddingProperty().addListener(metricsInvalidation);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3Badge badge = getSkinnable();
        contentAnimation.stop();
        badge.textProperty().removeListener(textInvalidation);
        badge.maxCharacterCountProperty().removeListener(textInvalidation);
        badge.smallSizeProperty().removeListener(metricsInvalidation);
        badge.largeHeightProperty().removeListener(metricsInvalidation);
        badge.largeMinWidthProperty().removeListener(metricsInvalidation);
        badge.containerShapeProperty().removeListener(metricsInvalidation);
        badge.horizontalPaddingProperty().removeListener(metricsInvalidation);
        super.dispose();
    }

    /// Updates text and layout together after display text changes.
    private void updateTextAndMetrics() {
        String oldDisplayText = currentDisplayText;
        currentDisplayText = getSkinnable().getDisplayText();
        updateText();
        updateMetrics();
        if (!oldDisplayText.equals(currentDisplayText)) {
            animateContentChange();
        }
    }

    /// Updates the rendered badge text.
    private void updateText() {
        label.setText(getSkinnable().getDisplayText());
    }

    /// Applies badge tokens to the skin layout.
    private void updateMetrics() {
        M3Badge badge = getSkinnable();
        if (badge.getDisplayText().isEmpty()) {
            double size = badge.getSmallSize();
            label.setMinSize(size, size);
            label.setPrefSize(size, size);
            label.setMaxSize(size, size);
            label.setPadding(Insets.EMPTY);
            label.setStyle("-fx-background-radius: " + formatPixels(size / 2.0) + ";");
        } else {
            double height = badge.getLargeHeight();
            label.setMinSize(badge.getLargeMinWidth(), height);
            label.setPrefHeight(height);
            label.setMaxHeight(height);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setPadding(new Insets(0.0, badge.getHorizontalPadding(), 0.0, badge.getHorizontalPadding()));
            label.setStyle("-fx-background-radius: " + formatPixels(badge.getContainerShape()) + ";");
        }
    }

    /// Animates badge content after the rendered display text changes.
    private void animateContentChange() {
        contentAnimation.stop();
        if (getSkinnable().getScene() == null) {
            applySettledContentState();
            return;
        }

        label.setOpacity(0.0);
        label.setScaleX(CONTENT_CHANGE_START_SCALE);
        label.setScaleY(CONTENT_CHANGE_START_SCALE);
        contentAnimation.getKeyFrames().setAll(new KeyFrame(
                CONTENT_CHANGE_DURATION,
                new KeyValue(label.opacityProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(label.scaleXProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(label.scaleYProperty(), 1.0, M3Motion.STANDARD_DECELERATE)
        ));
        contentAnimation.playFromStart();
    }

    /// Applies the settled badge content animation state.
    private void applySettledContentState() {
        label.setOpacity(1.0);
        label.setScaleX(1.0);
        label.setScaleY(1.0);
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
