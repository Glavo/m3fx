// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Lays out a demo page title, supporting text, and documentation action across available widths.
///
/// The heading and action share one row while enough width remains for a useful heading column. Otherwise the
/// action moves below the heading instead of compressing or clipping either text. The action remains at the logical
/// end in both left-to-right and right-to-left layouts.
@NotNullByDefault
final class DemoPageHeader extends Region {
    /// The horizontal gap between the inline heading and documentation action.
    private static final double HORIZONTAL_GAP = 16.0;

    /// The vertical gap between a stacked heading and documentation action.
    private static final double VERTICAL_GAP = 12.0;

    /// The minimum useful inline width retained for the heading before the action moves to a second row.
    private static final double MINIMUM_INLINE_HEADING_WIDTH = 248.0;

    /// The title and supporting text column.
    private final VBox heading;

    /// The action that opens the matching Material documentation page.
    private final M3Button documentationButton;

    /// Creates a responsive header for one demo page.
    ///
    /// @param title               the page title
    /// @param subtitle            the page supporting text
    /// @param documentationAction the action run when the documentation button is activated
    /// @throws NullPointerException if any argument is `null`
    DemoPageHeader(String title, String subtitle, Runnable documentationAction) {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(subtitle, "subtitle");
        Objects.requireNonNull(documentationAction, "documentationAction");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-page-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinWidth(0.0);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("demo-page-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMinWidth(0.0);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);

        heading = new VBox(8.0, titleLabel, subtitleLabel);
        heading.getStyleClass().add("demo-page-heading");
        heading.setFillWidth(true);
        heading.setMinWidth(0.0);
        heading.setMaxWidth(Double.MAX_VALUE);

        documentationButton = new M3Button("Material docs");
        documentationButton.setVariant(M3ButtonVariant.OUTLINED);
        documentationButton.getStyleClass().add("demo-page-doc-link");
        documentationButton.setOnAction(event -> documentationAction.run());

        getStyleClass().add("demo-page-header");
        getChildren().setAll(heading, documentationButton);
        setMinWidth(0.0);
        setMaxWidth(Double.MAX_VALUE);
        effectiveNodeOrientationProperty().addListener((observable, oldOrientation, newOrientation) -> requestLayout());
    }

    /// Returns whether the current width requires the documentation action to occupy a second row.
    ///
    /// @param availableWidth the width inside this region's horizontal insets
    /// @return whether a stacked layout is required
    private boolean usesStackedLayout(double availableWidth) {
        return availableWidth < documentationButton.prefWidth(-1.0)
                + HORIZONTAL_GAP
                + MINIMUM_INLINE_HEADING_WIDTH;
    }

    /// Computes the minimum width needed for the standalone documentation action.
    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + documentationButton.minWidth(-1.0) + snappedRightInset();
    }

    /// Computes the preferred width of the inline header arrangement.
    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset()
                + heading.prefWidth(-1.0)
                + HORIZONTAL_GAP
                + documentationButton.prefWidth(-1.0)
                + snappedRightInset();
    }

    /// Computes the height required by the active responsive arrangement.
    @Override
    protected double computePrefHeight(double width) {
        double availableWidth = Math.max(0.0, width - snappedLeftInset() - snappedRightInset());
        double buttonWidth = documentationButton.prefWidth(-1.0);
        if (usesStackedLayout(availableWidth)) {
            return snappedTopInset()
                    + heading.prefHeight(availableWidth)
                    + VERTICAL_GAP
                    + documentationButton.prefHeight(buttonWidth)
                    + snappedBottomInset();
        }

        double headingWidth = Math.max(0.0, availableWidth - HORIZONTAL_GAP - buttonWidth);
        return snappedTopInset()
                + Math.max(heading.prefHeight(headingWidth), documentationButton.prefHeight(buttonWidth))
                + snappedBottomInset();
    }

    /// Lays out the heading and documentation action at their logical start and end edges.
    @Override
    protected void layoutChildren() {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        double right = snappedRightInset();
        double availableWidth = Math.max(0.0, getWidth() - left - right);
        double buttonWidth = Math.min(availableWidth, documentationButton.prefWidth(-1.0));
        double buttonHeight = documentationButton.prefHeight(buttonWidth);
        boolean rightToLeft = getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        heading.setAlignment(rightToLeft ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        if (usesStackedLayout(availableWidth)) {
            double headingHeight = heading.prefHeight(availableWidth);
            heading.resizeRelocate(left, top, availableWidth, headingHeight);
            // Parent coordinate mirroring supplies the RTL physical placement. Reversing this coordinate here
            // would apply the direction twice and move the logical-end action back to the physical right edge.
            double buttonX = left + availableWidth - buttonWidth;
            documentationButton.resizeRelocate(buttonX, top + headingHeight + VERTICAL_GAP, buttonWidth, buttonHeight);
            return;
        }

        double headingWidth = Math.max(0.0, availableWidth - HORIZONTAL_GAP - buttonWidth);
        double headingHeight = heading.prefHeight(headingWidth);
        double rowHeight = Math.max(headingHeight, buttonHeight);
        heading.resizeRelocate(left, top + (rowHeight - headingHeight) / 2.0, headingWidth, headingHeight);
        double buttonX = left + availableWidth - buttonWidth;
        documentationButton.resizeRelocate(buttonX, top + (rowHeight - buttonHeight) / 2.0, buttonWidth, buttonHeight);
    }
}
