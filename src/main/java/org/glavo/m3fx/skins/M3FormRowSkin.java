// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3FormRow;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3FormRow].
@NotNullByDefault
public final class M3FormRowSkin extends SkinBase<M3FormRow> {
    /// The root horizontal row container.
    private final HBox container = new HBox();

    /// The label and supporting text column.
    private final VBox textColumn = new VBox();

    /// The label that renders primary row text.
    private final Label label = new Label();

    /// The label that renders row supporting text.
    private final Label supportingLabel = new Label();

    /// The slot that renders primary row content.
    private final StackPane contentSlot = new StackPane();

    /// The slot that renders optional trailing content.
    private final StackPane trailingSlot = new StackPane();

    /// Updates rendered text, slots, and metrics after row properties change.
    private final InvalidationListener updateListener = observable -> updateView();

    /// Creates a form row skin.
    ///
    /// @param control the form row controlled by this skin
    public M3FormRowSkin(M3FormRow control) {
        super(control);
        container.setManaged(false);
        container.getStyleClass().add(M3FormRow.CONTAINER_STYLE_CLASS);
        textColumn.getStyleClass().add(M3FormRow.TEXT_COLUMN_STYLE_CLASS);
        label.getStyleClass().add(M3FormRow.LABEL_STYLE_CLASS);
        supportingLabel.getStyleClass().add(M3FormRow.SUPPORTING_TEXT_STYLE_CLASS);
        contentSlot.getStyleClass().add(M3FormRow.CONTENT_STYLE_CLASS);
        trailingSlot.getStyleClass().add(M3FormRow.TRAILING_STYLE_CLASS);

        label.setWrapText(true);
        supportingLabel.setWrapText(true);
        contentSlot.setAlignment(Pos.CENTER_LEFT);
        trailingSlot.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(contentSlot, Priority.ALWAYS);

        textColumn.getChildren().addAll(label, supportingLabel);
        container.getChildren().addAll(textColumn, contentSlot, trailingSlot);
        getChildren().add(container);

        installListeners(control);
        updateView();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3FormRow control = getSkinnable();
        control.labelTextProperty().removeListener(updateListener);
        control.supportingTextProperty().removeListener(updateListener);
        control.contentProperty().removeListener(updateListener);
        control.trailingProperty().removeListener(updateListener);
        control.labelWidthProperty().removeListener(updateListener);
        control.columnSpacingProperty().removeListener(updateListener);
        control.rowMinHeightProperty().removeListener(updateListener);
        contentSlot.getChildren().clear();
        trailingSlot.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the row container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the row container and row minimum height token.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double rowHeight = Math.max(getSkinnable().getRowMinHeight(), container.minHeight(width));
        return topInset + rowHeight + bottomInset;
    }

    /// Computes the preferred width from the row container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the row container and row minimum height token.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double rowHeight = Math.max(getSkinnable().getRowMinHeight(), container.prefHeight(width));
        return topInset + rowHeight + bottomInset;
    }

    /// Lays out the row container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Installs listeners that keep skin content synchronized with the row model.
    private void installListeners(M3FormRow control) {
        control.labelTextProperty().addListener(updateListener);
        control.supportingTextProperty().addListener(updateListener);
        control.contentProperty().addListener(updateListener);
        control.trailingProperty().addListener(updateListener);
        control.labelWidthProperty().addListener(updateListener);
        control.columnSpacingProperty().addListener(updateListener);
        control.rowMinHeightProperty().addListener(updateListener);
    }

    /// Applies current text, slot nodes, and styleable metrics.
    private void updateView() {
        M3FormRow control = getSkinnable();
        String labelText = control.getLabelText();
        String supportingText = control.getSupportingText();

        label.setText(labelText);
        supportingLabel.setText(supportingText);
        label.setManaged(!labelText.isBlank());
        label.setVisible(!labelText.isBlank());
        supportingLabel.setManaged(!supportingText.isBlank());
        supportingLabel.setVisible(!supportingText.isBlank());

        boolean textVisible = !labelText.isBlank() || !supportingText.isBlank();
        textColumn.setManaged(textVisible);
        textColumn.setVisible(textVisible);
        textColumn.setPrefWidth(control.getLabelWidth());
        textColumn.setMinWidth(control.getLabelWidth());
        textColumn.setMaxWidth(control.getLabelWidth());

        @Nullable Node content = control.getContent();
        contentSlot.getChildren().setAll(content == null ? java.util.List.of() : java.util.List.of(content));
        contentSlot.setManaged(content != null);
        contentSlot.setVisible(content != null);

        @Nullable Node trailing = control.getTrailing();
        trailingSlot.getChildren().setAll(trailing == null ? java.util.List.of() : java.util.List.of(trailing));
        trailingSlot.setManaged(trailing != null);
        trailingSlot.setVisible(trailing != null);

        container.setSpacing(control.getColumnSpacing());
        container.setMinHeight(control.getRowMinHeight());
        control.requestLayout();
    }
}
