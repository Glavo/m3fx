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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3DialogPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Lays out the graphic, headline, content, and action row of an [M3DialogPane].
///
/// This skin contains no presentation, modality, or close behavior. It renders the same pane in an in-scene
/// overlay, a dedicated dialog window, or a static preview; [org.glavo.m3fx.controls.M3Dialog] coordinates
/// lifecycle when the pane is presented.
@NotNullByDefault
public final class M3DialogPaneSkin extends SkinBase<M3DialogPane> {
    /// The internal dialog-actions style class.
    private static final String ACTIONS_STYLE_CLASS = "m3-dialog-actions";

    /// The vertical layout containing all visible dialog sections.
    private final VBox layout = new VBox();

    /// The container used for an optional dialog graphic.
    private final StackPane graphicContainer = new StackPane();

    /// The container used for the optional headline label.
    private final StackPane headerPanel = new StackPane();

    /// The headline label bound to the pane's headline property.
    private final Label headerLabel = new Label();

    /// The container used for node or text content.
    private final StackPane contentContainer = new StackPane();

    /// The fallback body label bound to the pane's content-text property.
    private final Label contentLabel = new Label();

    /// The horizontal row containing the retained dialog actions.
    private final HBox actionBar = new HBox();

    /// The optional action supplied for the logical start of the action row.
    private final @Nullable Node leadingAction;

    /// The flexible space separating a leading action from trailing dialog actions.
    private final Region actionSpacer = new Region();

    /// Rebuilds visible sections after a slot or text-presence change.
    private final InvalidationListener structureInvalidation = observable -> rebuildSections();

    /// Synchronizes retained action nodes after the action list changes.
    private final InvalidationListener actionsInvalidation = observable -> {
        rebuildActions();
        rebuildSections();
    };

    /// Creates a skin around the actions owned by the supplied pane.
    ///
    /// @param control       the dialog pane being skinned
    /// @param leadingAction an optional action placed at the logical start of the action row
    /// @throws NullPointerException if `control` is `null`
    public M3DialogPaneSkin(M3DialogPane control, @Nullable Node leadingAction) {
        super(Objects.requireNonNull(control, "control"));
        this.leadingAction = leadingAction;

        layout.getStyleClass().add("m3-dialog-layout");
        layout.setFillWidth(true);
        graphicContainer.getStyleClass().add("graphic-container");
        graphicContainer.setAlignment(Pos.CENTER);
        headerPanel.getStyleClass().add("header-panel");
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerLabel.setWrapText(true);
        contentContainer.getStyleClass().add("content");
        contentContainer.setAlignment(Pos.TOP_LEFT);
        contentLabel.getStyleClass().add("content-label");
        contentLabel.setWrapText(true);
        actionBar.getStyleClass().add(ACTIONS_STYLE_CLASS);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.spacingProperty().bind(control.actionSpacingProperty());
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        headerLabel.textProperty().bind(control.headerTextProperty());
        contentLabel.textProperty().bind(control.contentTextProperty());
        control.graphicProperty().addListener(structureInvalidation);
        control.headerTextProperty().addListener(structureInvalidation);
        control.contentProperty().addListener(structureInvalidation);
        control.contentTextProperty().addListener(structureInvalidation);
        control.getActions().addListener(actionsInvalidation);

        getChildren().add(layout);
        rebuildActions();
        rebuildSections();
    }

    /// Releases bindings and listeners installed by this skin.
    @Override
    public void dispose() {
        M3DialogPane control = getSkinnable();
        control.graphicProperty().removeListener(structureInvalidation);
        control.headerTextProperty().removeListener(structureInvalidation);
        control.contentProperty().removeListener(structureInvalidation);
        control.contentTextProperty().removeListener(structureInvalidation);
        control.getActions().removeListener(actionsInvalidation);
        headerLabel.textProperty().unbind();
        contentLabel.textProperty().unbind();
        actionBar.spacingProperty().unbind();
        actionBar.getChildren().clear();
        layout.getChildren().clear();
        super.dispose();
    }

    /// Rebuilds the action row from the stable leading slot and current action list.
    private void rebuildActions() {
        actionBar.getChildren().clear();
        if (leadingAction != null) {
            actionBar.getChildren().addAll(leadingAction, actionSpacer);
        }
        actionBar.getChildren().addAll(getSkinnable().getActions());
    }

    /// Rebuilds the small fixed set of dialog sections from current slot values.
    private void rebuildSections() {
        M3DialogPane control = getSkinnable();
        layout.getChildren().clear();

        @Nullable Node graphic = control.getGraphic();
        graphicContainer.getChildren().clear();
        if (graphic != null) {
            graphicContainer.getChildren().add(graphic);
            layout.getChildren().add(graphicContainer);
        }

        headerPanel.getChildren().clear();
        if (!control.getHeaderText().isBlank()) {
            headerPanel.getChildren().add(headerLabel);
            layout.getChildren().add(headerPanel);
        }

        contentContainer.getChildren().clear();
        @Nullable Node content = control.getContent();
        if (content != null) {
            contentContainer.getChildren().add(content);
            layout.getChildren().add(contentContainer);
        } else if (!control.getContentText().isBlank()) {
            contentContainer.getChildren().add(contentLabel);
            layout.getChildren().add(contentContainer);
        }

        if (leadingAction != null || !control.getActions().isEmpty()) {
            layout.getChildren().add(actionBar);
        }
        control.requestLayout();
    }

    /// Computes the minimum width from the section layout and control insets.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + layout.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the section layout and control insets.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentWidth = width < 0.0 ? -1.0 : Math.max(0.0, width - leftInset - rightInset);
        return topInset + layout.minHeight(contentWidth) + bottomInset;
    }

    /// Computes the preferred width from the section layout and control insets.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + layout.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the section layout and control insets.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentWidth = width < 0.0 ? -1.0 : Math.max(0.0, width - leftInset - rightInset);
        return topInset + layout.prefHeight(contentWidth) + bottomInset;
    }

    /// Sizes the section layout to the dialog pane's available content area.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        layout.resizeRelocate(x, y, width, height);
    }
}
