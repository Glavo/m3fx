// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3RadioButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3RadioButton].
@NotNullByDefault
public class M3RadioButtonSkin extends M3SelectionControlSkinBase<M3RadioButton> {
    /// The visual radio indicator size.
    private static final double RADIO_SIZE = 20.0;

    /// The selected radio dot size.
    private static final double DOT_SIZE = 10.0;

    /// The visual radio indicator.
    private final StackPane radio = new StackPane();

    /// The selected radio dot.
    private final Region dot = new Region();

    /// Applies touch target token changes to radio geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Creates a radio button skin.
    public M3RadioButtonSkin(M3RadioButton control) {
        super(control);
        radio.getStyleClass().addAll("radio", "m3-radio");
        dot.getStyleClass().addAll("dot", "m3-radio-dot");
        radio.getChildren().add(dot);
        indicatorSlot().getChildren().add(radio);

        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        super.dispose();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        setIndicatorSlotSize(touchTargetSize, touchTargetSize);
        setFixedSize(radio, RADIO_SIZE, RADIO_SIZE);
        setFixedSize(dot, DOT_SIZE, DOT_SIZE);
    }
}
