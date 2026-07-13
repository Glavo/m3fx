// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipStyle;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Chip].
///
/// The skin extends the JavaFX labeled skin foundation for the inherited leading graphic and text, then reserves
/// a second logical slot for trailing content. No wrapper layout is allocated per chip.
@NotNullByDefault
public class M3ChipSkin extends M3LabeledButtonSkinBase<M3Chip> {
    /// Keeps leading and trailing graphics synchronized with the control properties.
    private final InvalidationListener graphicsInvalidation = observable -> updateGraphics();

    /// Invalidates chip measurement when the trailing graphic changes size or visibility.
    private final InvalidationListener trailingMetricsInvalidation = observable -> getSkinnable().requestLayout();

    /// The currently installed leading graphic.
    private @Nullable Node leadingGraphic;

    /// The currently installed trailing graphic.
    private @Nullable Node trailingGraphic;

    /// Creates a chip skin.
    ///
    /// @param control the chip controlled by this skin
    public M3ChipSkin(M3Chip control) {
        super(control);
        control.graphicProperty().addListener(graphicsInvalidation);
        control.trailingGraphicProperty().addListener(graphicsInvalidation);
        updateGraphics();
    }

    /// Removes graphic listeners and nodes before this skin is discarded.
    @Override
    public void dispose() {
        M3Chip chip = getSkinnable();
        chip.graphicProperty().removeListener(graphicsInvalidation);
        chip.trailingGraphicProperty().removeListener(graphicsInvalidation);
        uninstallLeadingGraphic();
        uninstallTrailingGraphic();
        super.dispose();
    }

    /// Adds the logical trailing slot to the minimum chip width.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset)
                + trailingWidth(height) + trailingGap();
    }

    /// Includes the logical trailing slot in preferred chip width.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
                + trailingWidth(height) + trailingGap();
    }

    /// Includes the logical trailing slot in preferred chip height.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double labeledHeight = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        Node trailing = trailingGraphic;
        if (trailing == null || !trailing.isManaged()) {
            return labeledHeight;
        }
        return Math.max(labeledHeight, topInset + trailing.prefHeight(width) + bottomInset);
    }

    /// Lays out labeled content and the optional trailing graphic in logical reading order.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        Node trailing = trailingGraphic;
        if (trailing == null || !trailing.isManaged()) {
            super.layoutChildren(x, y, width, height);
            return;
        }

        double trailingWidth = Math.min(width, snapSizeX(trailing.prefWidth(height)));
        double trailingHeight = Math.min(height, snapSizeY(trailing.prefHeight(trailingWidth)));
        double gap = Math.min(Math.max(0.0, width - trailingWidth), trailingGap());
        double labeledWidth = Math.max(0.0, width - trailingWidth - gap);
        double trailingY = y + (height - trailingHeight) / 2.0;
        super.layoutChildren(x, y, labeledWidth, height);
        trailing.resizeRelocate(x + labeledWidth + gap, snapPositionY(trailingY), trailingWidth, trailingHeight);
    }

    /// Returns a pressed scale only for elevated chips that already own elevation.
    @Override
    protected double pressedScale(boolean pressed) {
        if (getSkinnable().getChipStyle() == M3ChipStyle.ELEVATED) {
            return depthPressedScale(pressed);
        }
        return 1.0;
    }

    /// Synchronizes logical graphic slots without allocating wrapper panes.
    private void updateGraphics() {
        M3Chip chip = getSkinnable();
        Node newLeading = chip.getGraphic();
        if (leadingGraphic != newLeading) {
            uninstallLeadingGraphic();
            leadingGraphic = newLeading;
            if (newLeading != null && !newLeading.getStyleClass().contains(M3Chip.LEADING_GRAPHIC_STYLE_CLASS)) {
                newLeading.getStyleClass().add(M3Chip.LEADING_GRAPHIC_STYLE_CLASS);
            }
        }

        Node newTrailing = chip.getTrailingGraphic();
        if (trailingGraphic != newTrailing) {
            uninstallTrailingGraphic();
            trailingGraphic = newTrailing;
            if (newTrailing != null) {
                if (!newTrailing.getStyleClass().contains(M3Chip.TRAILING_GRAPHIC_STYLE_CLASS)) {
                    newTrailing.getStyleClass().add(M3Chip.TRAILING_GRAPHIC_STYLE_CLASS);
                }
                newTrailing.layoutBoundsProperty().addListener(trailingMetricsInvalidation);
                newTrailing.managedProperty().addListener(trailingMetricsInvalidation);
                newTrailing.visibleProperty().addListener(trailingMetricsInvalidation);
                getChildren().add(newTrailing);
            }
        }
        chip.requestLayout();
    }

    /// Removes the style marker from the previously installed leading graphic.
    private void uninstallLeadingGraphic() {
        Node oldLeading = leadingGraphic;
        if (oldLeading != null) {
            oldLeading.getStyleClass().remove(M3Chip.LEADING_GRAPHIC_STYLE_CLASS);
            leadingGraphic = null;
        }
    }

    /// Detaches the previously installed trailing graphic and its metric listeners.
    private void uninstallTrailingGraphic() {
        Node oldTrailing = trailingGraphic;
        if (oldTrailing != null) {
            oldTrailing.layoutBoundsProperty().removeListener(trailingMetricsInvalidation);
            oldTrailing.managedProperty().removeListener(trailingMetricsInvalidation);
            oldTrailing.visibleProperty().removeListener(trailingMetricsInvalidation);
            oldTrailing.getStyleClass().remove(M3Chip.TRAILING_GRAPHIC_STYLE_CLASS);
            getChildren().remove(oldTrailing);
            trailingGraphic = null;
        }
    }

    /// Returns the measured width of the managed trailing graphic.
    private double trailingWidth(double height) {
        Node trailing = trailingGraphic;
        return trailing == null || !trailing.isManaged() ? 0.0 : snapSizeX(trailing.prefWidth(height));
    }

    /// Returns spacing before the trailing graphic when that slot is present.
    private double trailingGap() {
        Node trailing = trailingGraphic;
        return trailing == null || !trailing.isManaged() ? 0.0 : getSkinnable().getGraphicTextGap();
    }
}
