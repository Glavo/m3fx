// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Tab].
@NotNullByDefault
public class M3TabSkin extends M3LabeledButtonSkinBase<M3Tab> {
    /// The active indicator style class.
    public static final String ACTIVE_INDICATOR_STYLE_CLASS = "m3-tab-active-indicator";

    /// The duration used by active indicator transitions.
    private static final Duration INDICATOR_DURATION = M3Motion.SHORT4;

    /// The selected indicator hidden scale.
    private static final double HIDDEN_INDICATOR_SCALE = 0.72;

    /// The active indicator region.
    private final Region activeIndicator = new Region();

    /// The active indicator animation timeline.
    private final Timeline indicatorAnimation = new Timeline();

    /// Animates the active indicator when selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateActiveIndicator(newValue);

    /// Requests layout when active indicator metrics change.
    private final InvalidationListener indicatorMetricsInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a tab skin.
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
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3Tab tab = getSkinnable();
        indicatorAnimation.stop();
        tab.selectedProperty().removeListener(selectedListener);
        tab.activeIndicatorHeightProperty().removeListener(indicatorMetricsInvalidation);
        tab.activeIndicatorShapeProperty().removeListener(indicatorMetricsInvalidation);
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
        activeIndicator.resizeRelocate(0.0, tabHeight - indicatorHeight, tabWidth, indicatorHeight);
        String radius = tab.getActiveIndicatorShape() + "px";
        activeIndicator.setStyle("-fx-background-radius: " + radius + " " + radius + " 0px 0px;");
    }

    /// Animates the active indicator to the requested state.
    private void animateActiveIndicator(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_INDICATOR_SCALE;
        indicatorAnimation.stop();
        indicatorAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        INDICATOR_DURATION,
                        new KeyValue(activeIndicator.opacityProperty(), targetOpacity, M3Motion.STANDARD),
                        new KeyValue(activeIndicator.scaleXProperty(), targetScale, M3Motion.STANDARD)
                )
        );
        M3Animation.playFromStart(getSkinnable(), indicatorAnimation);
    }

    /// Updates the active indicator without animation.
    private void updateActiveIndicatorImmediate(boolean selected) {
        activeIndicator.setOpacity(selected ? 1.0 : 0.0);
        activeIndicator.setScaleX(selected ? 1.0 : HIDDEN_INDICATOR_SCALE);
    }
}
