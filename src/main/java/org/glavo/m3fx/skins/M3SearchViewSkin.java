// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SearchView].
@NotNullByDefault
public final class M3SearchViewSkin extends SkinBase<M3SearchView> {
    /// The internal vertical container.
    private final VBox container = new VBox();

    /// The separator shown between the search header and results in divided views.
    private final Region divider = new Region();

    /// Updates divider participation when the active state changes.
    private final ChangeListener<Boolean> activeListener =
            (observable, oldValue, newValue) -> updateDividerVisibility();

    /// Updates divider participation when the visual treatment changes.
    private final ChangeListener<M3SearchViewStyle> styleListener =
            (observable, oldValue, newValue) -> updateDividerVisibility();

    /// Creates a search view skin.
    ///
    /// @param control          the search view controlled by this skin
    /// @param searchBar        the search bar owned by the control
    /// @param resultsContainer the result container owned by the control
    public M3SearchViewSkin(M3SearchView control, M3SearchBar searchBar, VBox resultsContainer) {
        super(control);
        container.setManaged(false);
        container.getStyleClass().add(M3SearchView.CONTENT_STYLE_CLASS);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        divider.getStyleClass().add(M3SearchView.DIVIDER_STYLE_CLASS);
        divider.setMouseTransparent(true);
        control.activeProperty().addListener(activeListener);
        control.viewStyleProperty().addListener(styleListener);
        updateDividerVisibility();
        container.getChildren().setAll(searchBar, divider, resultsContainer);
        getChildren().setAll(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        M3SearchView control = getSkinnable();
        control.activeProperty().removeListener(activeListener);
        control.viewStyleProperty().removeListener(styleListener);
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes the minimum width from the internal container.
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

    /// Computes the minimum height from the internal container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the internal container.
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

    /// Computes the preferred height from the internal container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Computes the maximum width from the internal container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height from the internal container.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the internal container in the full control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Includes the divider only while a divided search view is showing results.
    private void updateDividerVisibility() {
        M3SearchView control = getSkinnable();
        boolean visible = control.isActive() && control.getViewStyle() == M3SearchViewStyle.DIVIDED;
        divider.setVisible(visible);
        divider.setManaged(visible);
    }
}
