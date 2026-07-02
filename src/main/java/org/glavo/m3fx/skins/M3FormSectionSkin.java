// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3FormSection].
@NotNullByDefault
public final class M3FormSectionSkin extends SkinBase<M3FormSection> {
    /// The root vertical container.
    private final VBox root = new VBox();

    /// The optional section header container.
    private final VBox header = new VBox();

    /// The label that renders section title text.
    private final Label titleLabel = new Label();

    /// The label that renders section supporting text.
    private final Label supportingLabel = new Label();

    /// The vertical section content container.
    private final VBox content = new VBox();

    /// Mirrors public content changes into the skin content container.
    private final ListChangeListener<Node> contentListener = change -> updateContent();

    /// Updates title, supporting text, and layout metrics when section properties change.
    private final InvalidationListener updateListener = observable -> updateView();

    /// Updates logical layout when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateNodeOrientationLayout();

    /// Creates a form section skin.
    ///
    /// @param control the form section controlled by this skin
    public M3FormSectionSkin(M3FormSection control) {
        super(control);
        root.setManaged(false);
        header.getStyleClass().add(M3FormSection.HEADER_STYLE_CLASS);
        titleLabel.getStyleClass().add(M3FormSection.TITLE_STYLE_CLASS);
        titleLabel.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        supportingLabel.getStyleClass().add(M3FormSection.SUPPORTING_TEXT_STYLE_CLASS);
        supportingLabel.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        content.getStyleClass().add(M3FormSection.CONTENT_STYLE_CLASS);
        root.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        header.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        content.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());

        titleLabel.setWrapText(true);
        supportingLabel.setWrapText(true);
        header.setMaxWidth(Double.MAX_VALUE);
        content.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        supportingLabel.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().addAll(titleLabel, supportingLabel);
        root.getChildren().addAll(header, content);
        getChildren().add(root);

        control.getContent().addListener(contentListener);
        control.titleTextProperty().addListener(updateListener);
        control.supportingTextProperty().addListener(updateListener);
        control.contentSpacingProperty().addListener(updateListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        updateContent();
        updateNodeOrientationLayout();
        updateView();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3FormSection control = getSkinnable();
        control.getContent().removeListener(contentListener);
        control.titleTextProperty().removeListener(updateListener);
        control.supportingTextProperty().removeListener(updateListener);
        control.contentSpacingProperty().removeListener(updateListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        root.nodeOrientationProperty().unbind();
        header.nodeOrientationProperty().unbind();
        content.nodeOrientationProperty().unbind();
        content.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the root container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + root.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the root container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + root.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the root container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + root.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the root container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + root.prefHeight(width) + bottomInset;
    }

    /// Lays out the root container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        root.resizeRelocate(x, y, width, height);
    }

    /// Mirrors public section content into the internal container.
    private void updateContent() {
        content.getChildren().setAll(getSkinnable().getContent());
        getSkinnable().requestLayout();
    }

    /// Applies current section text and styleable metrics.
    private void updateView() {
        M3FormSection control = getSkinnable();
        String title = control.getTitleText();
        String supporting = control.getSupportingText();

        titleLabel.setText(title);
        supportingLabel.setText(supporting);
        titleLabel.setManaged(!title.isBlank());
        titleLabel.setVisible(!title.isBlank());
        supportingLabel.setManaged(!supporting.isBlank());
        supportingLabel.setVisible(!supporting.isBlank());

        boolean headerVisible = !title.isBlank() || !supporting.isBlank();
        header.setManaged(headerVisible);
        header.setVisible(headerVisible);

        content.setSpacing(control.getContentSpacing());
        control.requestLayout();
    }

    /// Updates orientation-dependent text and content alignment.
    private void updateNodeOrientationLayout() {
        header.setAlignment(topAlignment());
        content.setAlignment(topAlignment());
        titleLabel.setAlignment(centerAlignment());
        titleLabel.setTextAlignment(textAlignment());
        supportingLabel.setAlignment(centerAlignment());
        supportingLabel.setTextAlignment(textAlignment());
        getSkinnable().requestLayout();
    }

    /// Returns the current logical top alignment for section containers.
    private Pos topAlignment() {
        return M3NodeLayout.logicalStartTopAlignment(getSkinnable());
    }

    /// Returns the current logical center alignment for section labels.
    private Pos centerAlignment() {
        return M3NodeLayout.logicalStartCenterAlignment(getSkinnable());
    }

    /// Returns the current logical multi-line text alignment.
    private TextAlignment textAlignment() {
        return M3NodeLayout.isRightToLeft(getSkinnable()) ? TextAlignment.RIGHT : TextAlignment.LEFT;
    }
}
