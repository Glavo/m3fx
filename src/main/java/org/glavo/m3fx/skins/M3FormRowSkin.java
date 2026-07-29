// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3FormRow].
@NotNullByDefault
public final class M3FormRowSkin extends SkinBase<M3FormRow> {
    /// The minimum width preserved for primary content before the row stacks vertically.
    private static final double MIN_WIDE_CONTENT_WIDTH = 200.0;

    /// The vertical gap between the text and content rows in the compact layout.
    private static final double COMPACT_ROW_SPACING = 8.0;

    /// The internal row-container style class.
    private static final String CONTAINER_STYLE_CLASS = "m3-form-row-container";

    /// The internal label-column style class.
    private static final String TEXT_COLUMN_STYLE_CLASS = "m3-form-row-text-column";

    /// The internal primary-label style class.
    private static final String LABEL_STYLE_CLASS = "m3-form-row-label";

    /// The internal supporting-text style class.
    private static final String SUPPORTING_TEXT_STYLE_CLASS = "m3-form-row-supporting-text";

    /// The internal content-slot style class.
    private static final String CONTENT_STYLE_CLASS = "m3-form-row-content";

    /// The internal trailing-slot style class.
    private static final String TRAILING_STYLE_CLASS = "m3-form-row-trailing";

    /// The responsive row container.
    private final GridPane container = new GridPane();

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

    /// The fixed label column used by the wide layout.
    private final ColumnConstraints labelColumn = new ColumnConstraints();

    /// The growing primary content column.
    private final ColumnConstraints contentColumn = new ColumnConstraints();

    /// The trailing content column.
    private final ColumnConstraints trailingColumn = new ColumnConstraints();

    /// Whether the text column is currently stacked above the content row.
    private boolean compactLayout;

    /// Updates rendered text, slots, and metrics after row properties change.
    private final InvalidationListener updateListener = observable -> updateView();

    /// Updates multiline text alignment when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateTextAlignment();

    /// Creates a form row skin.
    ///
    /// @param control the form row controlled by this skin
    public M3FormRowSkin(M3FormRow control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.getStyleClass().add(CONTAINER_STYLE_CLASS);
        textColumn.getStyleClass().add(TEXT_COLUMN_STYLE_CLASS);
        label.getStyleClass().add(LABEL_STYLE_CLASS);
        supportingLabel.getStyleClass().add(SUPPORTING_TEXT_STYLE_CLASS);
        contentSlot.getStyleClass().add(CONTENT_STYLE_CLASS);
        trailingSlot.getStyleClass().add(TRAILING_STYLE_CLASS);

        label.setWrapText(true);
        supportingLabel.setWrapText(true);
        GridPane.setHgrow(contentSlot, Priority.ALWAYS);
        textColumn.setAlignment(Pos.CENTER_LEFT);
        label.setAlignment(Pos.CENTER_LEFT);
        supportingLabel.setAlignment(Pos.CENTER_LEFT);
        contentSlot.setAlignment(Pos.CENTER_LEFT);
        trailingSlot.setAlignment(Pos.CENTER_RIGHT);
        contentSlot.setMaxWidth(Double.MAX_VALUE);
        contentColumn.setMinWidth(0.0);
        contentColumn.setHgrow(Priority.ALWAYS);
        contentColumn.setFillWidth(true);
        trailingColumn.setHgrow(Priority.NEVER);

        textColumn.getChildren().addAll(label, supportingLabel);
        container.getChildren().addAll(textColumn, contentSlot, trailingSlot);
        configureGrid(false);
        getChildren().setAll(container);

        installListeners(control);
        updateTextAlignment();
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
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        contentSlot.getChildren().clear();
        trailingSlot.getChildren().clear();
        getChildren().remove(container);
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
        configureLayout(1.0);
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
        double contentWidth = contentWidth(width, leftInset, rightInset);
        configureLayout(contentWidth);
        double rowHeight = Math.max(getSkinnable().getRowMinHeight(), container.minHeight(contentWidth));
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
        configureLayout(Double.POSITIVE_INFINITY);
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
        double contentWidth = contentWidth(width, leftInset, rightInset);
        configureLayout(contentWidth);
        double rowHeight = Math.max(getSkinnable().getRowMinHeight(), container.prefHeight(contentWidth));
        return topInset + rowHeight + bottomInset;
    }

    /// Lays out the row container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        configureLayout(width);
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
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
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
        @Nullable Node content = control.getContent();
        contentSlot.getChildren().setAll(content == null ? java.util.List.of() : java.util.List.of(content));
        contentSlot.setManaged(content != null);
        contentSlot.setVisible(content != null);

        @Nullable Node trailing = control.getTrailing();
        trailingSlot.getChildren().setAll(trailing == null ? java.util.List.of() : java.util.List.of(trailing));
        trailingSlot.setManaged(trailing != null);
        trailingSlot.setVisible(trailing != null);

        configureLayout(getSkinnable().getWidth());
        updateContentColumnSpan();
        container.setMinHeight(control.getRowMinHeight());
        control.requestLayout();
    }

    /// Returns the available content width after subtracting the control's horizontal insets.
    private static double contentWidth(double width, double leftInset, double rightInset) {
        return width < 0.0 ? width : Math.max(0.0, width - leftInset - rightInset);
    }

    /// Updates grid constraints and metrics for the layout selected by the available width.
    private void configureLayout(double width) {
        M3FormRow control = getSkinnable();
        boolean useCompactLayout = shouldUseCompactLayout(width);
        if (compactLayout != useCompactLayout) {
            configureGrid(useCompactLayout);
        }

        if (compactLayout) {
            textColumn.setMinWidth(0.0);
            textColumn.setPrefWidth(VBox.USE_COMPUTED_SIZE);
            textColumn.setMaxWidth(Double.MAX_VALUE);
        } else {
            double labelWidth = control.getLabelWidth();
            textColumn.setMinWidth(labelWidth);
            textColumn.setPrefWidth(labelWidth);
            textColumn.setMaxWidth(labelWidth);
            labelColumn.setMinWidth(labelWidth);
            labelColumn.setPrefWidth(labelWidth);
            labelColumn.setMaxWidth(labelWidth);
        }

        container.setHgap(control.getColumnSpacing());
        container.setVgap(compactLayout ? COMPACT_ROW_SPACING : 0.0);
    }

    /// Returns whether the row must stack to preserve a usable primary content width.
    private boolean shouldUseCompactLayout(double width) {
        if (width <= 0.0 || !Double.isFinite(width)) {
            return false;
        }

        M3FormRow control = getSkinnable();
        double requiredWidth = 0.0;
        if (textColumn.isManaged()) {
            requiredWidth = control.getLabelWidth();
        }
        if (contentSlot.isManaged()) {
            if (requiredWidth > 0.0) {
                requiredWidth += control.getColumnSpacing();
            }
            requiredWidth += MIN_WIDE_CONTENT_WIDTH;
        }
        if (trailingSlot.isManaged()) {
            if (requiredWidth > 0.0) {
                requiredWidth += control.getColumnSpacing();
            }
            requiredWidth += trailingSlot.prefWidth(-1.0);
        }
        return width < requiredWidth;
    }

    /// Places the text, content, and trailing slots in wide or compact grid positions.
    private void configureGrid(boolean compact) {
        compactLayout = compact;
        if (compact) {
            container.getColumnConstraints().setAll(contentColumn, trailingColumn);
            GridPane.setConstraints(textColumn, 0, 0, 2, 1);
            GridPane.setConstraints(contentSlot, 0, 1);
            GridPane.setConstraints(trailingSlot, 1, 1);
        } else {
            container.getColumnConstraints().setAll(labelColumn, contentColumn, trailingColumn);
            GridPane.setConstraints(textColumn, 0, 0);
            GridPane.setConstraints(contentSlot, 1, 0);
            GridPane.setConstraints(trailingSlot, 2, 0);
        }
        updateContentColumnSpan();
    }

    /// Updates the compact content span after trailing-slot visibility changes.
    private void updateContentColumnSpan() {
        GridPane.setColumnSpan(
                contentSlot,
                compactLayout && !trailingSlot.isManaged() ? 2 : 1
        );
    }

    /// Updates multiline text alignment from the current logical direction.
    private void updateTextAlignment() {
        TextAlignment textAlignment = M3NodeLayout.isRightToLeft(getSkinnable())
                ? TextAlignment.RIGHT
                : TextAlignment.LEFT;
        label.setTextAlignment(textAlignment);
        supportingLabel.setTextAlignment(textAlignment);
    }
}
