// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SearchView].
///
/// The skin adopts the search bar and results container owned by the control and lays them out as a single vertical
/// surface. A divider participates in layout only while a divided search view is active; right-to-left orientation is
/// inherited from the skinned control.
@NotNullByDefault
public final class M3SearchViewSkin extends SkinBase<M3SearchView> {
    /// The internal content column style class.
    private static final String CONTENT_STYLE_CLASS = "m3-search-view-content";

    /// The divider style class.
    private static final String DIVIDER_STYLE_CLASS = "m3-search-view-divider";

    /// The internal vertical container.
    private final VBox container = new VBox();

    /// The search result surface owned by the skinned control.
    private final VBox resultsContainer;

    /// The retained clip that confines interactions to the contained result surface shape.
    private final Rectangle resultsClip = new Rectangle();

    /// The separator shown between the search header and results in divided views.
    private final Region divider = new Region();

    /// Updates divider participation when the active state changes.
    private final ChangeListener<Boolean> activeListener =
            (observable, oldValue, newValue) -> updateDividerVisibility();

    /// Updates divider participation when the visual treatment changes.
    private final ChangeListener<M3SearchViewStyle> styleListener =
            (observable, oldValue, newValue) -> {
                updateDividerVisibility();
                updateResultsClip();
            };

    /// Updates clip dimensions when the result surface bounds change.
    private final ChangeListener<Bounds> resultsBoundsListener =
            (observable, oldValue, newValue) -> updateResultsClipGeometry();

    /// Updates clip radii when CSS changes the result surface background shape.
    private final ChangeListener<Background> resultsBackgroundListener =
            (observable, oldValue, newValue) -> updateResultsClipGeometry();

    /// Creates a search view skin.
    ///
    /// @param control          the search view controlled by this skin
    /// @param searchBar        the search bar owned by the control
    /// @param resultsContainer the result container owned by the control
    public M3SearchViewSkin(M3SearchView control, M3SearchBar searchBar, VBox resultsContainer) {
        super(control);
        this.resultsContainer = resultsContainer;
        container.setManaged(false);
        container.getStyleClass().add(CONTENT_STYLE_CLASS);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        resultsClip.setMouseTransparent(true);
        divider.getStyleClass().add(DIVIDER_STYLE_CLASS);
        divider.setMouseTransparent(true);
        control.activeProperty().addListener(activeListener);
        control.viewStyleProperty().addListener(styleListener);
        resultsContainer.layoutBoundsProperty().addListener(resultsBoundsListener);
        resultsContainer.backgroundProperty().addListener(resultsBackgroundListener);
        updateDividerVisibility();
        updateResultsClip();
        container.getChildren().setAll(searchBar, divider, resultsContainer);
        getChildren().setAll(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        M3SearchView control = getSkinnable();
        control.activeProperty().removeListener(activeListener);
        control.viewStyleProperty().removeListener(styleListener);
        resultsContainer.layoutBoundsProperty().removeListener(resultsBoundsListener);
        resultsContainer.backgroundProperty().removeListener(resultsBackgroundListener);
        container.nodeOrientationProperty().unbind();
        resultsContainer.setClip(null);
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

    /// Clips contained results while leaving the search-view elevation shadow outside the clip hierarchy.
    private void updateResultsClip() {
        boolean contained = getSkinnable().getViewStyle() == M3SearchViewStyle.CONTAINED;
        resultsContainer.setClip(contained ? resultsClip : null);
        updateResultsClipGeometry();
        getSkinnable().requestLayout();
    }

    /// Synchronizes the retained clip with the CSS-resolved result surface bounds and shape.
    private void updateResultsClipGeometry() {
        double width = resultsContainer.getLayoutBounds().getWidth();
        double height = resultsContainer.getLayoutBounds().getHeight();
        resultsClip.setX(0.0);
        resultsClip.setY(0.0);
        resultsClip.setWidth(width);
        resultsClip.setHeight(height);

        Background background = resultsContainer.getBackground();
        if (background == null || background.getFills().isEmpty()) {
            resultsClip.setArcWidth(0.0);
            resultsClip.setArcHeight(0.0);
            return;
        }

        BackgroundFill fill = background.getFills().get(0);
        CornerRadii radii = fill.getRadii();
        double horizontalRadius = radii.getTopLeftHorizontalRadius();
        double verticalRadius = radii.getTopLeftVerticalRadius();
        if (radii.isTopLeftHorizontalRadiusAsPercentage()) {
            horizontalRadius *= width;
        }
        if (radii.isTopLeftVerticalRadiusAsPercentage()) {
            verticalRadius *= height;
        }
        resultsClip.setArcWidth(Math.min(width, Math.max(0.0, horizontalRadius * 2.0)));
        resultsClip.setArcHeight(Math.min(height, Math.max(0.0, verticalRadius * 2.0)));
    }
}
