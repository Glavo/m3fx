// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Badge].
@NotNullByDefault
public class M3BadgeSkin extends SkinBase<M3Badge> {
    /// The initial scale used when badge content changes.
    private static final double CONTENT_CHANGE_START_SCALE = 0.86;

    /// The visible badge label.
    private final Label label = new Label();

    /// The badge content change animation.
    private final M3NodeTransition contentAnimation = new M3NodeTransition(label);

    /// Updates text and metrics after display text inputs change.
    private final InvalidationListener textInvalidation = observable -> updateTextAndMetrics();

    /// Applies size token changes to badge geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// The currently rendered display text.
    private String currentDisplayText = "";

    /// Creates a badge skin.
    ///
    /// @param control the badge controlled by this skin
    public M3BadgeSkin(M3Badge control) {
        super(control);
        label.setManaged(false);
        label.getStyleClass().add("m3-badge-label");
        getChildren().setAll(label);

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
        getChildren().remove(label);
        super.dispose();
    }

    /// Computes the minimum badge width from the active badge token state.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the minimum badge height from the active badge token state.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the preferred badge width from the active badge token state.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + computeBadgeWidth(height) + rightInset;
    }

    /// Computes the preferred badge height from the active badge token state.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + computeBadgeHeight() + bottomInset;
    }

    /// Computes the maximum badge width from the active badge token state.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the maximum badge height from the active badge token state.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Lays out the label with the explicit dot or text badge size instead of inheriting label text metrics.
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double labelWidth = snapSizeX(computeBadgeWidth(contentHeight));
        double labelHeight = snapSizeY(computeBadgeHeight());
        double labelX = snapPositionX(contentX + (contentWidth - labelWidth) / 2.0);
        double labelY = snapPositionY(contentY + (contentHeight - labelHeight) / 2.0);
        label.resizeRelocate(labelX, labelY, labelWidth, labelHeight);
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
        getSkinnable().requestLayout();
    }

    /// Computes the visual badge width for the current text state.
    private double computeBadgeWidth(double height) {
        M3Badge badge = getSkinnable();
        if (badge.getDisplayText().isEmpty()) {
            return badge.getSmallSize();
        }
        return Math.max(badge.getLargeMinWidth(), label.prefWidth(height));
    }

    /// Computes the visual badge height for the current text state.
    private double computeBadgeHeight() {
        M3Badge badge = getSkinnable();
        return badge.getDisplayText().isEmpty() ? badge.getSmallSize() : badge.getLargeHeight();
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
        M3MotionSpec spec = M3Animation.fastEffects(getSkinnable());
        contentAnimation.configure(spec, 1.0, 1.0, 1.0, label.getTranslateX(), label.getTranslateY());
        M3Animation.playFromStart(getSkinnable(), contentAnimation);
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
            return (long) value + "px";
        }
        return value + "px";
    }
}
