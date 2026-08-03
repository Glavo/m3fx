// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3BreadcrumbItem;
import org.glavo.m3fx.controls.M3Breadcrumbs;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// The default responsive overflow skin for [M3Breadcrumbs].
///
/// The skin retains original item nodes while they are visible and mirrors the full logical hierarchy into the
/// overflow menu. Width-aware layout progressively collapses earlier levels, always retaining the current item.
@NotNullByDefault
public final class M3BreadcrumbsSkin extends SkinBase<M3Breadcrumbs> {
    /// The row containing visible items, separators, and the optional overflow control.
    private final HBox row = new HBox();

    /// The menu button representing collapsed hierarchy levels.
    private final M3MenuButton overflowButton = new M3MenuButton("…");

    /// The logical entries currently mirrored into the row, excluding separators.
    private final List<Node> visibleEntries = new ArrayList<>();

    /// The breadcrumb items currently observed for content and state changes.
    private final List<M3BreadcrumbItem> observedItems = new ArrayList<>();

    /// Rebuilds item observation, menu content, and row entries after hierarchy changes.
    private final ListChangeListener<M3BreadcrumbItem> itemsListener = change -> rebuildHierarchy();

    /// Rebuilds the overflow menu and width-dependent presentation after an item changes.
    private final InvalidationListener itemInvalidation = observable -> {
        rebuildOverflowMenu();
        getSkinnable().requestLayout();
    };

    /// Requests a new entry selection after overflow policy changes.
    private final InvalidationListener overflowPolicyInvalidation = observable -> getSkinnable().requestLayout();

    /// Updates compact overflow-button geometry before requesting layout.
    private final InvalidationListener compactInvalidation = observable -> {
        updateOverflowButtonMetrics();
        getSkinnable().requestLayout();
    };

    /// Mirrors separator direction after effective orientation changes.
    private final InvalidationListener orientationInvalidation = observable -> updateSeparatorDirections();

    /// Creates a responsive breadcrumbs skin.
    ///
    /// @param control the breadcrumbs control represented by this skin
    public M3BreadcrumbsSkin(M3Breadcrumbs control) {
        super(control);
        row.getStyleClass().add("m3-breadcrumbs-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setFillHeight(true);
        row.setManaged(false);

        overflowButton.getStyleClass().add("m3-breadcrumb-overflow");
        overflowButton.setAccessibleText("More items");
        overflowButton.setMnemonicParsing(false);
        overflowButton.setVariant(M3ButtonVariant.TEXT);
        overflowButton.setSize(M3ButtonSize.EXTRA_SMALL);
        overflowButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
        updateOverflowButtonMetrics();

        getChildren().setAll(row);
        control.getItems().addListener(itemsListener);
        control.maxVisibleItemsProperty().addListener(overflowPolicyInvalidation);
        control.keepRootVisibleProperty().addListener(overflowPolicyInvalidation);
        control.compactProperty().addListener(compactInvalidation);
        control.effectiveNodeOrientationProperty().addListener(orientationInvalidation);
        rebuildHierarchy();
    }

    /// Removes listeners and retained child references before disposal.
    @Override
    public void dispose() {
        M3Breadcrumbs control = getSkinnable();
        control.getItems().removeListener(itemsListener);
        control.maxVisibleItemsProperty().removeListener(overflowPolicyInvalidation);
        control.keepRootVisibleProperty().removeListener(overflowPolicyInvalidation);
        control.compactProperty().removeListener(compactInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(orientationInvalidation);
        clearItemObservation();
        row.getChildren().clear();
        visibleEntries.clear();
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width required by an overflow control and current item.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        List<Node> entries = entriesForLimit(2);
        return leftInset + contentWidth(entries, height, true) + rightInset;
    }

    /// Computes the minimum height from the active presentation metrics.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + contentHeight() + bottomInset;
    }

    /// Computes the preferred width from the configured logical overflow limit.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        List<Node> entries = entriesForLimit(getSkinnable().getMaxVisibleItems());
        return leftInset + contentWidth(entries, height, false) + rightInset;
    }

    /// Computes the preferred height from the active presentation metrics.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + contentHeight() + bottomInset;
    }

    /// Allows breadcrumbs to contract and expand with their containing layout.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return Double.MAX_VALUE;
    }

    /// Caps breadcrumbs at their preferred height.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Selects the deepest fitting visible suffix and lays out the retained row.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        int limit = Math.min(getSkinnable().getMaxVisibleItems(), Math.max(2, getSkinnable().getItems().size()));
        List<Node> entries = entriesForLimit(limit);
        while (limit > 2 && contentWidth(entries, height, false) > width) {
            limit--;
            entries = entriesForLimit(limit);
        }
        applyVisibleEntries(entries);
        row.resizeRelocate(x, y, width, height);
    }

    /// Reinstalls item listeners and refreshes all derived hierarchy presentation.
    private void rebuildHierarchy() {
        clearItemObservation();
        for (M3BreadcrumbItem item : getSkinnable().getItems()) {
            item.textProperty().addListener(itemInvalidation);
            item.disableProperty().addListener(itemInvalidation);
            item.currentProperty().addListener(itemInvalidation);
            observedItems.add(item);
        }
        rebuildOverflowMenu();
        applyVisibleEntries(entriesForLimit(getSkinnable().getMaxVisibleItems()));
        getSkinnable().requestLayout();
    }

    /// Removes listeners from all previously observed breadcrumb items.
    private void clearItemObservation() {
        for (M3BreadcrumbItem item : observedItems) {
            item.textProperty().removeListener(itemInvalidation);
            item.disableProperty().removeListener(itemInvalidation);
            item.currentProperty().removeListener(itemInvalidation);
        }
        observedItems.clear();
    }

    /// Mirrors the complete logical hierarchy into the overflow menu.
    private void rebuildOverflowMenu() {
        List<Node> menuItems = new ArrayList<>(getSkinnable().getItems().size());
        for (M3BreadcrumbItem item : getSkinnable().getItems()) {
            M3MenuItem menuItem = new M3MenuItem(Objects.requireNonNullElse(item.getText(), ""));
            menuItem.setDisable(item.isDisabled());
            menuItem.setSelected(item.isCurrent());
            menuItem.setOnAction(event -> item.fire());
            menuItems.add(menuItem);
        }
        overflowButton.getItems().setAll(menuItems);
    }

    /// Applies the compact or default interaction target used by the overflow control.
    private void updateOverflowButtonMetrics() {
        double size = getSkinnable().isCompact() ? 32.0 : 40.0;
        overflowButton.setMinSize(size, size);
        overflowButton.setPrefSize(size, size);
        overflowButton.setMaxSize(size, size);
    }

    /// Returns logical row entries for one maximum-visible-entry limit.
    ///
    /// @param requestedLimit the requested entry limit
    /// @return the entries in root-to-current order, including overflow when required
    private List<Node> entriesForLimit(int requestedLimit) {
        List<M3BreadcrumbItem> items = getSkinnable().getItems();
        int count = items.size();
        if (count == 0) {
            return List.of();
        }

        int limit = Math.max(2, requestedLimit);
        if (count <= limit) {
            return new ArrayList<>(items);
        }

        List<Node> entries = new ArrayList<>(limit);
        if (getSkinnable().isKeepRootVisible() && limit >= 3) {
            entries.add(items.get(0));
            entries.add(overflowButton);
            int suffixCount = limit - 2;
            entries.addAll(items.subList(count - suffixCount, count));
        } else {
            entries.add(overflowButton);
            int suffixCount = limit - 1;
            entries.addAll(items.subList(count - suffixCount, count));
        }
        return entries;
    }

    /// Rebuilds row children when the logical entry identity or order changes.
    ///
    /// @param entries the logical entries to display
    private void applyVisibleEntries(List<Node> entries) {
        if (visibleEntries.equals(entries)) {
            updateSeparatorDirections();
            return;
        }

        visibleEntries.clear();
        visibleEntries.addAll(entries);
        List<Node> children = new ArrayList<>(Math.max(0, entries.size() * 2 - 1));
        for (int index = 0; index < entries.size(); index++) {
            if (index != 0) {
                children.add(createSeparator());
            }
            children.add(entries.get(index));
        }
        row.getChildren().setAll(children);
    }

    /// Creates one mouse-transparent directional separator.
    ///
    /// @return the separator label
    private Label createSeparator() {
        Label separator = new Label(separatorText());
        separator.getStyleClass().add("m3-breadcrumb-separator");
        separator.setFocusTraversable(false);
        separator.setMouseTransparent(true);
        return separator;
    }

    /// Updates retained separator glyphs for the current effective orientation.
    private void updateSeparatorDirections() {
        String text = separatorText();
        for (Node child : row.getChildren()) {
            if (child instanceof Label label && label.getStyleClass().contains("m3-breadcrumb-separator")) {
                label.setText(text);
            }
        }
    }

    /// Returns the separator glyph that points toward the current location.
    ///
    /// @return a right-pointing LTR or left-pointing RTL separator
    private String separatorText() {
        return getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT ? "‹" : "›";
    }

    /// Computes row width for one logical entry set.
    ///
    /// @param entries the entries to measure
    /// @param height the available height, or a negative value when unconstrained
    /// @param minimum whether minimum rather than preferred widths are requested
    /// @return the measured row width
    private double contentWidth(List<Node> entries, double height, boolean minimum) {
        double width = 0.0;
        for (Node entry : entries) {
            if (entry.isResizable()) {
                width += minimum ? entry.minWidth(height) : entry.prefWidth(height);
            } else {
                width += entry.getLayoutBounds().getWidth();
            }
        }
        return width + separatorWidth() * Math.max(0, entries.size() - 1);
    }

    /// Returns the row height for the active presentation.
    ///
    /// @return zero for an empty hierarchy, otherwise the compact or default row height
    private double contentHeight() {
        if (getSkinnable().getItems().isEmpty()) {
            return 0.0;
        }
        return getSkinnable().isCompact() ? 32.0 : 40.0;
    }

    /// Returns the stable logical width reserved for one separator.
    ///
    /// @return the separator width in logical pixels
    private double separatorWidth() {
        return getSkinnable().isCompact() ? 16.0 : 20.0;
    }
}
