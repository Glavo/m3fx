// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3Meter;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default retained-mode skin for [M3Meter].
///
/// The skin retains separate label, value-label, track, and fill nodes. The fill begins at logical leading because
/// JavaFX mirrors the retained geometry with the control in right-to-left orientation.
@NotNullByDefault
public final class M3MeterSkin extends SkinBase<M3Meter> {
    /// The default track width in logical pixels.
    private static final double DEFAULT_TRACK_WIDTH = 192.0;

    /// The minimum usable track width in logical pixels.
    private static final double MIN_TRACK_WIDTH = 64.0;

    /// The minimum width of a top-label meter in logical pixels.
    private static final double MIN_TOP_LABEL_WIDTH = 96.0;

    /// The horizontal gap between label roles in logical pixels.
    private static final double LABEL_VALUE_GAP = 12.0;

    /// The retained descriptive label.
    private final Label label = new Label();

    /// The retained optional value label.
    private final Label valueLabel = new Label();

    /// The retained inactive track.
    private final Region track = new Region();

    /// The retained active fill.
    private final Region fill = new Region();

    /// Updates retained text after either text property changes.
    private final InvalidationListener textInvalidation = observable -> updateText();

    /// Requests layout after value, size, or placement changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a meter skin.
    ///
    /// @param control the meter controlled by this skin
    public M3MeterSkin(M3Meter control) {
        super(control);

        label.setManaged(false);
        label.setMouseTransparent(true);
        label.setWrapText(true);
        label.setTextOverrun(OverrunStyle.CLIP);
        label.getStyleClass().add("m3-meter-label");

        valueLabel.setManaged(false);
        valueLabel.setMouseTransparent(true);
        valueLabel.setWrapText(false);
        valueLabel.setTextOverrun(OverrunStyle.CLIP);
        valueLabel.getStyleClass().add("m3-meter-value-label");

        track.setManaged(false);
        track.setMouseTransparent(true);
        track.setMinSize(0.0, 0.0);
        track.getStyleClass().add("m3-meter-track");

        fill.setManaged(false);
        fill.setMouseTransparent(true);
        fill.setMinSize(0.0, 0.0);
        fill.getStyleClass().add("m3-meter-fill");

        getChildren().setAll(track, fill, label, valueLabel);

        control.labelProperty().addListener(textInvalidation);
        control.valueTextProperty().addListener(textInvalidation);
        control.valueProperty().addListener(layoutInvalidation);
        control.sizeProperty().addListener(layoutInvalidation);
        control.sideLabelProperty().addListener(layoutInvalidation);
        updateText();
    }

    /// Removes listeners and retained nodes before disposal.
    @Override
    public void dispose() {
        M3Meter control = getSkinnable();
        control.labelProperty().removeListener(textInvalidation);
        control.valueTextProperty().removeListener(textInvalidation);
        control.valueProperty().removeListener(layoutInvalidation);
        control.sizeProperty().removeListener(layoutInvalidation);
        control.sideLabelProperty().removeListener(layoutInvalidation);
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width needed to retain a useful track and complete value label.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double valueWidth = valueLabelWidth();
        boolean hasLabel = isLabelVisible();
        boolean hasValue = isValueLabelVisible();
        double contentWidth;
        if (getSkinnable().isSideLabel()) {
            double labelWidth = hasLabel ? label.minWidth(-1.0) : 0.0;
            contentWidth = labelWidth
                    + valueWidth
                    + MIN_TRACK_WIDTH
                    + sideGapCount(hasLabel, hasValue) * LABEL_VALUE_GAP;
        } else {
            double labelWidth = hasLabel ? label.minWidth(-1.0) : 0.0;
            double headerWidth = labelWidth
                    + valueWidth
                    + (hasLabel && hasValue ? LABEL_VALUE_GAP : 0.0);
            contentWidth = Math.max(MIN_TOP_LABEL_WIDTH, headerWidth);
        }
        return leftInset + contentWidth + rightInset;
    }

    /// Computes the minimum height for the current label placement.
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

    /// Computes the preferred width from the default Spectrum meter width and current labels.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        boolean hasLabel = isLabelVisible();
        boolean hasValue = isValueLabelVisible();
        double valueWidth = valueLabelWidth();
        double contentWidth;
        if (getSkinnable().isSideLabel()) {
            double labelWidth = hasLabel ? label.prefWidth(-1.0) : 0.0;
            contentWidth = labelWidth
                    + DEFAULT_TRACK_WIDTH
                    + valueWidth
                    + sideGapCount(hasLabel, hasValue) * LABEL_VALUE_GAP;
        } else {
            double headerWidth = valueWidth + (hasLabel && hasValue ? LABEL_VALUE_GAP : 0.0);
            contentWidth = Math.max(DEFAULT_TRACK_WIDTH, headerWidth);
        }
        return leftInset + contentWidth + rightInset;
    }

    /// Computes the preferred height with wrapped label text when a width is supplied.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentWidth = width < 0.0
                ? computePrefWidth(-1.0, 0.0, 0.0, 0.0, 0.0)
                : Math.max(0.0, width - leftInset - rightInset);
        double contentHeight = getSkinnable().isSideLabel()
                ? computeSideContentHeight(contentWidth)
                : computeTopContentHeight(contentWidth);
        return topInset + contentHeight + bottomInset;
    }

    /// Permits the meter to grow horizontally with its container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return Double.MAX_VALUE;
    }

    /// Prevents vertical growth beyond the height required by the current labels.
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

    /// Lays out labels and the measured fill using logical-leading geometry.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        if (getSkinnable().isSideLabel()) {
            layoutSideLabels(x, y, width, height);
        } else {
            layoutTopLabels(x, y, width);
        }
    }

    /// Updates retained text and visibility.
    private void updateText() {
        M3Meter control = getSkinnable();
        label.setText(control.getLabel());
        label.setVisible(!control.getLabel().isEmpty());

        @Nullable String valueText = control.getValueText();
        String effectiveValueText = valueText == null ? "" : valueText;
        valueLabel.setText(effectiveValueText);
        valueLabel.setVisible(!effectiveValueText.isEmpty());
        control.requestLayout();
    }

    /// Returns whether the descriptive label participates in layout.
    ///
    /// @return `true` when the label is non-empty
    private boolean isLabelVisible() {
        return label.isVisible();
    }

    /// Returns whether the value label participates in layout.
    ///
    /// @return `true` when value text is non-empty
    private boolean isValueLabelVisible() {
        return valueLabel.isVisible();
    }

    /// Returns the complete single-line value-label width.
    ///
    /// @return the value-label width, or zero when hidden
    private double valueLabelWidth() {
        return isValueLabelVisible() ? valueLabel.prefWidth(-1.0) : 0.0;
    }

    /// Returns the number of gaps surrounding the center track in side-label layout.
    ///
    /// @param hasLabel whether a leading label is present
    /// @param hasValue whether a trailing value label is present
    /// @return the gap count from zero to two
    private static int sideGapCount(boolean hasLabel, boolean hasValue) {
        return (hasLabel ? 1 : 0) + (hasValue ? 1 : 0);
    }

    /// Computes the content height for labels placed above the track.
    ///
    /// @param width the available content width
    /// @return the required content height
    private double computeTopContentHeight(double width) {
        double headerHeight = topHeaderHeight(width);
        double trackThickness = getSkinnable().getSize().getTrackThickness();
        double gap = headerHeight > 0.0 ? getSkinnable().getSize().getLabelGap() : 0.0;
        return headerHeight + gap + trackThickness;
    }

    /// Computes the content height for labels placed beside the track.
    ///
    /// @param width the available content width
    /// @return the required content height
    private double computeSideContentHeight(double width) {
        SideMetrics metrics = sideMetrics(width);
        double labelHeight = isLabelVisible() ? label.prefHeight(metrics.labelWidth()) : 0.0;
        double valueHeight = isValueLabelVisible() ? valueLabel.prefHeight(metrics.valueWidth()) : 0.0;
        return Math.max(getSkinnable().getSize().getTrackThickness(), Math.max(labelHeight, valueHeight));
    }

    /// Computes the label-row height for top-label layout.
    ///
    /// @param width the available content width
    /// @return the required label-row height
    private double topHeaderHeight(double width) {
        double valueWidth = Math.min(width, valueLabelWidth());
        double gap = isLabelVisible() && isValueLabelVisible() ? LABEL_VALUE_GAP : 0.0;
        double labelWidth = Math.max(0.0, width - valueWidth - gap);
        double labelHeight = isLabelVisible() ? label.prefHeight(labelWidth) : 0.0;
        double valueHeight = isValueLabelVisible() ? valueLabel.prefHeight(valueWidth) : 0.0;
        return Math.max(labelHeight, valueHeight);
    }

    /// Lays out a meter whose labels appear above the track.
    ///
    /// @param x the content x coordinate
    /// @param y the content y coordinate
    /// @param width the available content width
    private void layoutTopLabels(double x, double y, double width) {
        double valueWidth = Math.min(width, valueLabelWidth());
        double gap = isLabelVisible() && isValueLabelVisible() ? LABEL_VALUE_GAP : 0.0;
        double labelWidth = Math.max(0.0, width - valueWidth - gap);
        double headerHeight = topHeaderHeight(width);

        if (isLabelVisible()) {
            label.resizeRelocate(x, y, labelWidth, headerHeight);
        }
        if (isValueLabelVisible()) {
            valueLabel.resizeRelocate(x + width - valueWidth, y, valueWidth, headerHeight);
        }

        double trackY = y + headerHeight;
        if (headerHeight > 0.0) {
            trackY += getSkinnable().getSize().getLabelGap();
        }
        layoutTrack(x, trackY, width);
    }

    /// Lays out a meter whose labels appear beside the track.
    ///
    /// @param x the content x coordinate
    /// @param y the content y coordinate
    /// @param width the available content width
    /// @param height the available content height
    private void layoutSideLabels(double x, double y, double width, double height) {
        SideMetrics metrics = sideMetrics(width);
        double cursor = x;

        if (isLabelVisible()) {
            label.resizeRelocate(cursor, y, metrics.labelWidth(), height);
            cursor += metrics.labelWidth() + LABEL_VALUE_GAP;
        }

        double trackY = y + (height - getSkinnable().getSize().getTrackThickness()) / 2.0;
        layoutTrack(cursor, trackY, metrics.trackWidth());
        cursor += metrics.trackWidth();

        if (isValueLabelVisible()) {
            cursor += LABEL_VALUE_GAP;
            valueLabel.resizeRelocate(cursor, y, metrics.valueWidth(), height);
        }
    }

    /// Lays out the inactive track and active fill.
    ///
    /// @param x the track x coordinate
    /// @param y the track y coordinate
    /// @param width the track width
    private void layoutTrack(double x, double y, double width) {
        double trackThickness = getSkinnable().getSize().getTrackThickness();
        double effectiveWidth = Math.max(0.0, width);
        track.resizeRelocate(x, y, effectiveWidth, trackThickness);
        fill.resizeRelocate(
                x,
                y,
                effectiveWidth * getSkinnable().getEffectiveValue(),
                trackThickness
        );
    }

    /// Computes the allocated label, track, and value widths for side-label layout.
    ///
    /// @param width the available content width
    /// @return the immutable side-layout metrics
    private SideMetrics sideMetrics(double width) {
        boolean hasLabel = isLabelVisible();
        boolean hasValue = isValueLabelVisible();
        double valueWidth = Math.min(width, valueLabelWidth());
        double gaps = sideGapCount(hasLabel, hasValue) * LABEL_VALUE_GAP;
        double availableBeforeValue = Math.max(0.0, width - valueWidth - gaps);
        double labelWidth = hasLabel
                ? Math.min(label.prefWidth(-1.0), Math.max(0.0, availableBeforeValue - MIN_TRACK_WIDTH))
                : 0.0;
        double trackWidth = Math.max(0.0, availableBeforeValue - labelWidth);
        return new SideMetrics(labelWidth, trackWidth, valueWidth);
    }

    /// Stores allocated widths for side-label layout.
    ///
    /// @param labelWidth the descriptive-label width
    /// @param trackWidth the track width
    /// @param valueWidth the value-label width
    private record SideMetrics(double labelWidth, double trackWidth, double valueWidth) {
    }
}
