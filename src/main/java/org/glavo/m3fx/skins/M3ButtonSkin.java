// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.paint.Paint;
import org.glavo.m3fx.controls.M3ButtonBase;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for [M3ButtonBase].
@NotNullByDefault
public final class M3ButtonSkin extends M3LabeledButtonSkinBase<M3ButtonBase> {
    /// Reapplies concrete paints when a styleable button paint changes.
    private final InvalidationListener paintInvalidation = observable -> updatePaints();

    /// Creates a button skin.
    ///
    /// @param control the button controlled by this skin
    public M3ButtonSkin(M3ButtonBase control) {
        super(control);
        control.containerColorProperty().addListener(paintInvalidation);
        control.contentColorProperty().addListener(paintInvalidation);
        control.backgroundProperty().addListener(paintInvalidation);
        updatePaints();
    }

    /// Removes paint listeners before this skin is disposed.
    @Override
    public void dispose() {
        M3ButtonBase control = getSkinnable();
        control.containerColorProperty().removeListener(paintInvalidation);
        control.contentColorProperty().removeListener(paintInvalidation);
        control.backgroundProperty().removeListener(paintInvalidation);
        super.dispose();
    }

    /// Lays out the button and reapplies paints after the current CSS pass.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        updatePaints();
    }

    /// Returns a pressed scale only for elevated buttons that already own elevation.
    @Override
    protected double pressedScale(boolean pressed) {
        if (getSkinnable().getVariant() == M3ButtonVariant.ELEVATED) {
            return depthPressedScale(pressed);
        }
        return 1.0;
    }

    /// Applies the resolved container and content paints to the concrete skin nodes.
    private void updatePaints() {
        M3ButtonBase button = getSkinnable();
        Paint contentPaint = button.getContentColor();
        setContainerPaint(button.getContainerColor());
        setStateLayerPaint(contentPaint);
        if (!contentPaint.equals(button.getTextFill())) {
            button.setTextFill(contentPaint);
        }
    }
}
