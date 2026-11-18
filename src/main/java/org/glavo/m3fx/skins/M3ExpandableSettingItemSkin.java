// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3ExpandableSettingItem;
import org.glavo.m3fx.controls.M3ListItemBase;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.animation.M3DoubleTransition;
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

    /// Clips the body without resizing its children during a reveal.
    private final Rectangle bodyClip = new Rectangle();

    /// Fraction of the preferred body height reserved below the header.
    private final DoubleProperty reveal = new SimpleDoubleProperty();

    /// Retargetable motion shared by expansion and collapse.
    private final M3DoubleTransition transition = new M3DoubleTransition(
            reveal, M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD, 0.0, 1.0);

    /// Keeps the content host synchronized with the control content property.
    private final ChangeListener<@Nullable Node> contentListener = (observable, oldValue, newValue) -> {
        updateContentNode(newValue);
        getSkinnable().requestLayout();
    };

    /// Reveals or conceals the body from its current visible extent.
    private final ChangeListener<Boolean> expandedListener = (observable, oldValue, newValue) -> {
        M3ExpandableSettingItem control = expandable();
        double target = newValue ? 1.0 : 0.0;
        if (control.getContent() == null || control.getScene() == null || control.getScene().getWindow() == null) {
            transition.stop();
            reveal.set(target);
        } else {
            transition.configure(M3Animation.fastSpatial(control), target);
            M3Animation.playFromStart(control, transition);
        }
    };

    /// Creates an expandable setting-item skin.
    ///
    /// @param control the expandable setting row
    public M3ExpandableSettingItemSkin(M3ExpandableSettingItem control) {
        super(control);
        contentHost.getStyleClass().add("m3-expandable-setting-content");
        contentHost.setManaged(control.isExpanded());
        getChildren().add(contentHost);
        reveal.set(control.isExpanded() ? 1.0 : 0.0);
        reveal.addListener(observable -> {
            contentHost.setManaged(reveal.get() > 0.0);
            control.requestLayout();
        });
        updateContentNode(control.getContent());
        control.contentProperty().addListener(contentListener);
        control.expandedProperty().addListener(expandedListener);
    }

    /// Removes expandable listeners before disposal.
    @Override
    public void dispose() {
        M3ExpandableSettingItem control = expandable();
        transition.stop();
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
        double height = topInset + headerHeight() + bottomInset;
        if (reveal.get() == 0.0 || expandable().getContent() == null) {
            return height;
        }
        double bodyWidth = width < 0.0 ? -1.0 : Math.max(0.0, width - leftInset - rightInset);
        return height + reveal.get() * contentHost.prefHeight(bodyWidth);
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
        if (reveal.get() < 1.0 || control.getContent() == null) {
            return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
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

        if (reveal.get() > 0.0 && control.getContent() != null && height > usedHeader) {
            double natural = contentHost.prefHeight(width);
            double available = height - usedHeader;
            double visible = reveal.get() == 1.0 ? available : Math.min(available, natural * reveal.get());
            if (visible < natural) {
                bodyClip.setWidth(width);
                bodyClip.setHeight(visible);
                contentHost.setClip(bodyClip);
            } else {
                contentHost.setClip(null);
            }
            contentHost.setVisible(true);
            contentHost.resizeRelocate(x, y + usedHeader, width, Math.max(natural, visible));
        } else {
            contentHost.setVisible(false);
        }
    }

    /// Returns whether a local y coordinate falls inside the expanded content band.
    ///
    /// @param localY the y coordinate in control-local space
    /// @return `true` when `localY` is below the header while content is shown
    public boolean isInContentArea(double localY) {
        double bodyY = localY - contentHost.getLayoutY();
        double visibleHeight = contentHost.getClip() == null ? contentHost.getHeight() : bodyClip.getHeight();
        return contentHost.isVisible() && bodyY >= 0.0 && bodyY < visibleHeight;
    }

    /// Updates the hosted content node.
    private void updateContentNode(@Nullable Node content) {
        if (content == null) {
            contentHost.getChildren().clear();
            contentHost.setVisible(false);
            transition.stop();
            reveal.set(expandable().isExpanded() ? 1.0 : 0.0);
            return;
        }
        contentHost.getChildren().setAll(content);
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

    /// Returns the expandable control.
    private M3ExpandableSettingItem expandable() {
        return (M3ExpandableSettingItem) getSkinnable();
    }
}
