// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3CheckBox;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3CheckBox].
@NotNullByDefault
public class M3CheckBoxSkin extends M3SelectionControlSkinBase<M3CheckBox> {
    /// The visual checkbox container size.
    private static final double BOX_SIZE = 18.0;

    /// The visual check mark width.
    private static final double MARK_WIDTH = 12.0;

    /// The visual check mark height.
    private static final double MARK_HEIGHT = 10.0;

    /// The visual checkbox container.
    private final StackPane box = new StackPane();

    /// The visual selected check mark.
    private final Region mark = new Region();

    /// Creates a checkbox skin.
    public M3CheckBoxSkin(M3CheckBox control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-checkbox-box");
        mark.getStyleClass().addAll("mark", "m3-checkbox-mark");
        box.getChildren().add(mark);
        indicatorSlot().getChildren().add(box);

        updateMetrics();
        control.touchTargetSizeProperty().addListener((observable, oldValue, newValue) -> updateMetrics());
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        setIndicatorSlotSize(touchTargetSize, touchTargetSize);
        setFixedSize(box, BOX_SIZE, BOX_SIZE);
        setFixedSize(mark, MARK_WIDTH, MARK_HEIGHT);
    }
}
