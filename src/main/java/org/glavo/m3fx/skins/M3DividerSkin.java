// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Divider].
@NotNullByDefault
public class M3DividerSkin extends SkinBase<M3Divider> {
    /// The container that applies divider insets.
    private final StackPane container = new StackPane();

    /// The visible divider line.
    private final Region line = new Region();

    /// Applies token changes to the divider geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Creates a divider skin.
    ///
    /// @param control the divider controlled by this skin
    public M3DividerSkin(M3Divider control) {
        super(control);
        container.getStyleClass().add("m3-divider-container");
        line.getStyleClass().add("m3-divider-line");
        container.getChildren().add(line);
        getChildren().add(container);

        updateMetrics();
        control.orientationProperty().addListener(metricsInvalidation);
        control.thicknessProperty().addListener(metricsInvalidation);
        control.insetStartProperty().addListener(metricsInvalidation);
        control.insetEndProperty().addListener(metricsInvalidation);
        control.effectiveNodeOrientationProperty().addListener(metricsInvalidation);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3Divider divider = getSkinnable();
        divider.orientationProperty().removeListener(metricsInvalidation);
        divider.thicknessProperty().removeListener(metricsInvalidation);
        divider.insetStartProperty().removeListener(metricsInvalidation);
        divider.insetEndProperty().removeListener(metricsInvalidation);
        divider.effectiveNodeOrientationProperty().removeListener(metricsInvalidation);
        super.dispose();
    }

    /// Applies divider tokens to the skin layout.
    private void updateMetrics() {
        M3Divider divider = getSkinnable();
        double thickness = divider.getThickness();
        double insetStart = divider.getInsetStart();
        double insetEnd = divider.getInsetEnd();
        if (divider.getOrientation() == Orientation.VERTICAL) {
            container.setPadding(new Insets(insetStart, 0.0, insetEnd, 0.0));
            container.setMinHeight(0.0);
            container.setPrefHeight(Region.USE_COMPUTED_SIZE);
            container.setMaxHeight(Double.MAX_VALUE);
            container.setMinWidth(thickness);
            container.setPrefWidth(thickness);
            container.setMaxWidth(thickness);
            line.setMinWidth(thickness);
            line.setPrefWidth(thickness);
            line.setMaxWidth(thickness);
            line.setMinHeight(0.0);
            line.setPrefHeight(Region.USE_COMPUTED_SIZE);
            line.setMaxHeight(Double.MAX_VALUE);
        } else {
            container.setPadding(M3NodeLayout.logicalInsets(divider, 0.0, insetStart, 0.0, insetEnd));
            container.setMinHeight(thickness);
            container.setPrefHeight(thickness);
            container.setMaxHeight(thickness);
            container.setMinWidth(0.0);
            container.setPrefWidth(Region.USE_COMPUTED_SIZE);
            container.setMaxWidth(Double.MAX_VALUE);
            line.setMinHeight(thickness);
            line.setPrefHeight(thickness);
            line.setMaxHeight(thickness);
            line.setMinWidth(0.0);
            line.setPrefWidth(Region.USE_COMPUTED_SIZE);
            line.setMaxWidth(Double.MAX_VALUE);
        }
    }
}
