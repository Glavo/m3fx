// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Switch;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Switch].
@NotNullByDefault
public class M3SwitchSkin extends M3SelectionControlSkinBase<M3Switch> {
    /// The switch track width.
    private static final double TRACK_WIDTH = 52.0;

    /// The switch track height.
    private static final double TRACK_HEIGHT = 32.0;

    /// The visual switch track.
    private final StackPane box = new StackPane();

    /// The visual switch thumb.
    private final StackPane thumb = new StackPane();

    /// Creates a switch skin.
    public M3SwitchSkin(M3Switch control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-switch-track");
        thumb.getStyleClass().addAll("thumb", "m3-switch-thumb");
        box.getChildren().add(thumb);
        indicatorSlot().getChildren().add(box);

        updateMetrics();
        control.touchTargetSizeProperty().addListener((observable, oldValue, newValue) -> updateMetrics());
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        double touchTargetHeight = Math.max(getSkinnable().getTouchTargetSize(), TRACK_HEIGHT);
        setIndicatorSlotSize(TRACK_WIDTH, touchTargetHeight);
        setFixedSize(box, TRACK_WIDTH, TRACK_HEIGHT);
    }
}
