// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3BreadcrumbItem;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for [M3BreadcrumbItem].
@NotNullByDefault
public final class M3BreadcrumbItemSkin extends M3LabeledButtonSkinBase<M3BreadcrumbItem> {
    /// Refreshes the state-layer paint when CSS changes the item text color.
    private final InvalidationListener textFillInvalidation = observable -> updatePaints();

    /// Creates a breadcrumb-item skin.
    ///
    /// @param control the breadcrumb item controlled by this skin
    public M3BreadcrumbItemSkin(M3BreadcrumbItem control) {
        super(control);
        control.textFillProperty().addListener(textFillInvalidation);
        updatePaints();
    }

    /// Removes paint listeners before disposal.
    @Override
    public void dispose() {
        getSkinnable().textFillProperty().removeListener(textFillInvalidation);
        super.dispose();
    }

    /// Reapplies CSS-resolved paints after layout.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        updatePaints();
    }

    /// Applies transparent container and current text paints to interaction feedback.
    private void updatePaints() {
        setContainerPaint(Color.TRANSPARENT);
        setStateLayerPaint(getSkinnable().getTextFill());
    }
}
