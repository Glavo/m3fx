// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

/// A single line of informational text tags. When the tags do not fit, a menu button
/// replaces the trailing tags and shows their count. Its menu lists all tags in source order.
/// Tags do not represent actions or selection. Duplicate strings represent separate tags.
@NotNullByDefault
public final class M3TagList extends Region {
    /// Spacing between adjacent tags or the overflow button.
    private static final double GAP = 4;
    /// Text in logical display order.
    private final ObservableList<String> tags = FXCollections.observableArrayList();

    /// Returns the live list of tag labels. Changes update the row and overflow menu.
    public ObservableList<String> getTags() {
        return tags;
    }

    /// The disclosure may change the line height when horizontal space is limited.
    @Override public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    /// The keyboard-accessible disclosure displayed in constrained layouts.
    private final M3MenuButton overflow = new M3MenuButton();

    /// Creates an empty tag list.
    public M3TagList() {
        M3ControlStyles.initialize(this, "m3-tag-list");
        overflow.setVariant(M3ButtonVariant.TEXT);
        overflow.setVisible(false);
        getChildren().add(overflow);
        tags.addListener((ListChangeListener<String>) change -> {
            overflow.hideMenu();
            overflow.getItems().clear();
            getChildren().clear();
            for (String text : tags) {
                var label = new Label(text);
                label.getStyleClass().add("m3-tag-list-label");
                getChildren().add(label);
            }
            overflow.setText("+" + tags.size());
            getChildren().add(overflow);
            requestLayout();
        });
    }

    /// Returns the stylesheet for the tag labels.
    @Override public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("tag-list.css");
    }

    /// Reserves enough width for either the whole line or its disclosure.
    @Override protected double computeMinWidth(double height) {
        return Math.min(prefWidth(height), snappedLeftInset() + overflow.prefWidth(-1) + snappedRightInset());
    }

    /// Measures all tags without reserving space for an unnecessary disclosure.
    @Override protected double computePrefWidth(double height) {
        double width = snappedLeftInset() + snappedRightInset();
        for (int i = 0; i < tags.size(); i++) {
            width += (i == 0 ? 0 : GAP) + snapSizeX(getChildren().get(i).prefWidth(-1));
        }
        return width;
    }

    /// Measures a single line, accounting for the disclosure only when constrained.
    @Override protected double computePrefHeight(double width) {
        double height = width >= 0 && width < prefWidth(-1) ? overflow.prefHeight(-1) : 0;
        for (int i = 0; i < tags.size(); i++) height = Math.max(height, getChildren().get(i).prefHeight(-1));
        return snappedTopInset() + snapSizeY(height) + snappedBottomInset();
    }

    /// Fits as many complete tags as possible and leaves the remainder reachable through the menu.
    @Override protected void layoutChildren() {
        int total = tags.size();
        double available = Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset());
        double used = prefWidth(-1) - snappedLeftInset() - snappedRightInset();
        int visible = total;
        if (used > available) {
            double buttonWidth = snapSizeX(overflow.prefWidth(-1));
            do {
                used -= snapSizeX(getChildren().get(--visible).prefWidth(-1)) + (visible == 0 ? 0 : GAP);
            } while (visible > 0 && used + GAP + buttonWidth > available);
            String count = "+" + (total - visible);
            if (!count.equals(overflow.getText())) overflow.setText(count);
        }
        double x = snappedLeftInset();
        for (int i = 0; i < total; i++) {
            var tag = getChildren().get(i);
            tag.setVisible(i < visible);
            if (i < visible) {
                double w = snapSizeX(tag.prefWidth(-1));
                double h = snapSizeY(tag.prefHeight(w));
                tag.resizeRelocate(x, snapPositionY((getHeight() - h) / 2), w, h);
                x += w + GAP;
            }
        }
        boolean more = visible < total;
        overflow.setVisible(more);
        if (more) {
            if (overflow.getItems().isEmpty()) {
                overflow.getItems().setAll(tags.stream().map(M3MenuItem::new).toList());
            }
            double w = Math.min(Math.max(0, available - x + snappedLeftInset()), snapSizeX(overflow.prefWidth(-1)));
            double h = snapSizeY(overflow.prefHeight(w));
            overflow.resizeRelocate(x, snapPositionY((getHeight() - h) / 2), w, h);
        } else if (overflow.isShowing()) {
            overflow.hideMenu();
        }
    }
}
