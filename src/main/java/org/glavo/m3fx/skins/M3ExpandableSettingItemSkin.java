// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3ExpandableSettingItem;
import org.glavo.m3fx.controls.M3ListItemBase;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Skin for [M3ExpandableSettingItem].
///
/// The header reuses the standard list-item presentation. Nested content is laid out directly under the header while
/// the control is expanded and is excluded from the preferred height while collapsed.
@NotNullByDefault
public final class M3ExpandableSettingItemSkin extends M3ListItemSkin {
    /// Host for nested expandable content.
    private final StackPane contentHost = new StackPane();

    /// Keeps the content host synchronized with the control content property.
    private final ChangeListener<@Nullable Node> contentListener = (observable, oldValue, newValue) -> {
        updateContentNode(newValue);
        getSkinnable().requestLayout();
    };

    /// Requests layout when expansion changes.
    private final ChangeListener<Boolean> expandedListener = (observable, oldValue, newValue) -> {
        updateContentVisibility();
        getSkinnable().requestLayout();
    };

    /// Creates an expandable setting-item skin.
    ///
    /// @param control the expandable setting row
    public M3ExpandableSettingItemSkin(M3ExpandableSettingItem control) {
        super(control);
        contentHost.getStyleClass().add("m3-expandable-setting-content");
        contentHost.setManaged(false);
        contentHost.setMouseTransparent(false);
        getChildren().add(contentHost);
        updateContentNode(control.getContent());
        updateContentVisibility();
        control.contentProperty().addListener(contentListener);
        control.expandedProperty().addListener(expandedListener);
    }

    /// Removes expandable listeners before disposal.
    @Override
    public void dispose() {
        M3ExpandableSettingItem control = expandable();
        control.contentProperty().removeListener(contentListener);
        control.expandedProperty().removeListener(expandedListener);
        contentHost.getChildren().clear();
        getChildren().remove(contentHost);
        super.dispose();
    }

    /// Includes expanded content in the preferred height.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + bottomInset + headerHeight() + contentHeight(width - leftInset - rightInset);
    }

    /// Includes expanded content in the minimum height.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Allows the expanded row to grow with available height when needed.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3ExpandableSettingItem control = expandable();
        if (!control.isExpanded() || control.getContent() == null) {
            return headerHeight() + topInset + bottomInset;
        }
        return Double.MAX_VALUE;
    }

    /// Lays out the header row and optional expanded content.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ExpandableSettingItem control = expandable();
        double headerH = headerHeight();
        double usedHeader = Math.min(headerH, Math.max(0.0, height));
        // Header presentation reuses the base list-item layers within the header band only.
        super.layoutChildren(x, y, width, usedHeader);

        if (control.isExpanded() && control.getContent() != null && height > usedHeader) {
            contentHost.setVisible(true);
            contentHost.resizeRelocate(x, y + usedHeader, width, height - usedHeader);
        } else {
            contentHost.setVisible(false);
            contentHost.resizeRelocate(x, y, 0.0, 0.0);
        }
    }

    /// Returns whether a local y coordinate falls inside the expanded content band.
    ///
    /// @param localY the y coordinate in control-local space
    /// @return `true` when `localY` is below the header while content is shown
    public boolean isInContentArea(double localY) {
        M3ExpandableSettingItem control = expandable();
        if (!control.isExpanded() || control.getContent() == null) {
            return false;
        }
        return localY >= headerHeight();
    }

    /// Updates the hosted content node.
    private void updateContentNode(@Nullable Node content) {
        if (content == null) {
            contentHost.getChildren().clear();
            return;
        }
        contentHost.getChildren().setAll(content);
    }

    /// Shows the content host only while expanded and content is present.
    private void updateContentVisibility() {
        M3ExpandableSettingItem control = expandable();
        boolean show = control.isExpanded() && control.getContent() != null;
        contentHost.setVisible(show);
    }

    /// Returns the preferred header height for the current line count.
    private double headerHeight() {
        M3ListItemBase item = getSkinnable();
        return switch (item.getLineCount()) {
            case ONE_LINE -> item.getOneLineHeight();
            case TWO_LINE -> item.getTwoLineHeight();
            case THREE_LINE -> item.getThreeLineHeight();
        };
    }

    /// Returns the preferred height of expanded content at the supplied width.
    private double contentHeight(double width) {
        M3ExpandableSettingItem control = expandable();
        if (!control.isExpanded()) {
            return 0.0;
        }
        @Nullable Node content = control.getContent();
        if (content == null) {
            return 0.0;
        }
        return Math.max(0.0, content.prefHeight(width));
    }

    /// Returns the expandable control.
    private M3ExpandableSettingItem expandable() {
        return (M3ExpandableSettingItem) getSkinnable();
    }
}
