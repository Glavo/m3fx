// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Tab].
///
/// The skin augments labeled-button content with the selected tab indicator. Indicator width, height, corner radius,
/// opacity, and horizontal position follow the control properties and selected state without affecting tab content
/// measurement.
@NotNullByDefault
public class M3TabSkin extends M3LabeledButtonSkinBase<M3Tab> {
    /// The active indicator style class.
    private static final String ACTIVE_INDICATOR_STYLE_CLASS = "m3-tab-active-indicator";

    /// The selected indicator hidden scale.
    private static final double HIDDEN_INDICATOR_SCALE = 0.72;

    /// The pseudo-class inherited from a secondary tab bar.
    private static final PseudoClass SECONDARY_PSEUDO_CLASS = PseudoClass.getPseudoClass("secondary");

    /// The active indicator region.
    private final Region activeIndicator = new Region();

    /// The active indicator animation timeline.
    private final M3NodeTransition indicatorAnimation = new M3NodeTransition(activeIndicator);

    /// The last shape radius applied to the active indicator.
    private double appliedIndicatorShape = Double.NaN;

    /// The text node created by [javafx.scene.control.skin.LabeledSkinBase], cached after its first layout.
    private @Nullable Text labelText;

    /// Animates the active indicator when selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateActiveIndicator(newValue);

    /// Requests layout when active indicator metrics change.
    private final InvalidationListener indicatorMetricsInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a tab skin.
    ///
    /// @param control the tab controlled by this skin
    public M3TabSkin(M3Tab control) {
        super(control);
        activeIndicator.getStyleClass().add(ACTIVE_INDICATOR_STYLE_CLASS);
        activeIndicator.setManaged(false);
        activeIndicator.setMouseTransparent(true);
        getChildren().add(activeIndicator);

        updateActiveIndicatorImmediate(control.isSelected());
        control.selectedProperty().addListener(selectedListener);
        control.activeIndicatorHeightProperty().addListener(indicatorMetricsInvalidation);
        control.activeIndicatorShapeProperty().addListener(indicatorMetricsInvalidation);
        control.activeIndicatorMinWidthProperty().addListener(indicatorMetricsInvalidation);
        control.activeIndicatorHorizontalInsetProperty().addListener(indicatorMetricsInvalidation);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3Tab tab = getSkinnable();
        indicatorAnimation.stop();
        tab.selectedProperty().removeListener(selectedListener);
        tab.activeIndicatorHeightProperty().removeListener(indicatorMetricsInvalidation);
        tab.activeIndicatorShapeProperty().removeListener(indicatorMetricsInvalidation);
        tab.activeIndicatorMinWidthProperty().removeListener(indicatorMetricsInvalidation);
        tab.activeIndicatorHorizontalInsetProperty().removeListener(indicatorMetricsInvalidation);
        labelText = null;
        super.dispose();
    }

    /// Lays out tab content and the active indicator.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        M3Tab tab = getSkinnable();
        double tabWidth = tab.getWidth();
        double tabHeight = tab.getHeight();
        if (tabWidth <= 0.0) {
            tabWidth = tab.getLayoutBounds().getWidth();
        }
        if (tabHeight <= 0.0) {
            tabHeight = tab.getLayoutBounds().getHeight();
        }
        double indicatorHeight = tab.getActiveIndicatorHeight();
        double indicatorX = 0.0;
        double indicatorWidth = tabWidth;
        if (!tab.getPseudoClassStates().contains(SECONDARY_PSEUDO_CLASS)) {
            Text text = labelText;
            if (text == null || text.getParent() == null) {
                text = null;
                for (Node child : getChildren()) {
                    if (child instanceof Text candidate) {
                        text = candidate;
                        break;
                    }
                }
                labelText = text;
            }

            double contentMinX = Double.POSITIVE_INFINITY;
            double contentMaxX = Double.NEGATIVE_INFINITY;
            if (text != null && text.isVisible()) {
                Bounds bounds = text.getBoundsInParent();
                contentMinX = bounds.getMinX();
                contentMaxX = bounds.getMaxX();
            }
            @Nullable Node graphic = tab.getGraphic();
            if (graphic != null && graphic.isVisible()) {
                Bounds bounds = graphic.getBoundsInParent();
                contentMinX = Math.min(contentMinX, bounds.getMinX());
                contentMaxX = Math.max(contentMaxX, bounds.getMaxX());
            }

            boolean hasContentBounds = Double.isFinite(contentMinX) && Double.isFinite(contentMaxX);
            double contentCenterX = hasContentBounds
                    ? (contentMinX + contentMaxX) / 2.0
                    : tabWidth / 2.0;
            double contentWidth = hasContentBounds
                    ? Math.max(0.0, contentMaxX - contentMinX)
                    : 0.0;
            indicatorWidth = Math.min(
                    tabWidth,
                    Math.max(
                            tab.getActiveIndicatorMinWidth(),
                            contentWidth + tab.getActiveIndicatorHorizontalInset() * 2.0
                    )
            );
            indicatorX = Math.max(0.0, Math.min(tabWidth - indicatorWidth, contentCenterX - indicatorWidth / 2.0));
        }
        activeIndicator.resizeRelocate(
                snapPositionX(indicatorX),
                snapPositionY(tabHeight - indicatorHeight),
                snapSizeX(indicatorWidth),
                snapSizeY(indicatorHeight)
        );
        double indicatorShape = tab.getActiveIndicatorShape();
        if (Double.compare(appliedIndicatorShape, indicatorShape) != 0) {
            appliedIndicatorShape = indicatorShape;
            String radius = indicatorShape + "px";
            activeIndicator.setStyle("-fx-background-radius: " + radius + " " + radius + " 0px 0px;");
        }
    }

    /// Animates the active indicator to the requested state.
    private void animateActiveIndicator(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_INDICATOR_SCALE;
        indicatorAnimation.stop();
        M3MotionSpec spec = M3Animation.defaultEffects(getSkinnable());
        indicatorAnimation.configure(
                spec,
                targetOpacity,
                targetScale,
                activeIndicator.getScaleY(),
                activeIndicator.getTranslateX(),
                activeIndicator.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), indicatorAnimation);
    }

    /// Updates the active indicator without animation.
    private void updateActiveIndicatorImmediate(boolean selected) {
        activeIndicator.setOpacity(selected ? 1.0 : 0.0);
        activeIndicator.setScaleX(selected ? 1.0 : HIDDEN_INDICATOR_SCALE);
    }
}
