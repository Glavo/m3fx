// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Shared layout helpers for the HMCL Material 3 demo pages.
@NotNullByDefault
final class HMCLDemoUi {
    /// Standard secondary navigation width in logical pixels.
    static final double SIDEBAR_WIDTH = 220.0;

    /// Prevents utility-class instantiation.
    private HMCLDemoUi() {
    }

    /// Makes a region fill its parent without contributing a content-driven minimum size.
    ///
    /// @param region the region to configure
    /// @param <T> the region type
    /// @return `region`
    static <T extends Region> T fill(T region) {
        region.setMinSize(0.0, 0.0);
        region.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return region;
    }

    /// Creates a vertical scroll host that can shrink below its content height.
    ///
    /// @param content the scrolled content
    /// @return the configured scroll pane
    static ScrollPane scroll(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("hmcl-page-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinSize(0.0, 0.0);
        scrollPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        M3ScrollPanes.style(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    /// Creates a fixed-width secondary navigation column.
    ///
    /// @param children the sidebar children
    /// @return the configured sidebar
    static VBox sidebar(Node... children) {
        VBox sidebar = new VBox(0.0, children);
        sidebar.getStyleClass().add("hmcl-context-sidebar");
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        sidebar.setMinHeight(0.0);
        sidebar.setMaxHeight(Double.MAX_VALUE);
        return sidebar;
    }

    /// Creates a compact section label used above sidebar groups.
    ///
    /// @param text the label text
    /// @return the label node
    static M3Text sectionLabel(String text) {
        M3Text label = new M3Text(text, M3TextRole.LABEL_SMALL);
        label.getStyleClass().add("hmcl-sidebar-section-label");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /// Creates a navigation row for a contextual left pane.
    ///
    /// @param headline the row text
    /// @param iconPath the icon path
    /// @param action the activation handler, or `null`
    /// @return the list item
    static M3ListItem navItem(String headline, String iconPath, @Nullable Runnable action) {
        M3ListItem item = new M3ListItem(headline);
        item.setLeading(HMCLDemoIcons.create(iconPath));
        item.setMaxWidth(Double.MAX_VALUE);
        if (action != null) {
            item.setOnAction(event -> action.run());
        }
        return item;
    }

    /// Creates a page root column with standard padding.
    ///
    /// @param children the page children
    /// @return the configured column
    static VBox pageColumn(Node... children) {
        VBox column = fill(new VBox(16.0, children));
        column.getStyleClass().add("hmcl-page-column");
        column.setPadding(new Insets(16.0, 20.0, 24.0, 20.0));
        column.setAlignment(Pos.TOP_LEFT);
        return column;
    }

    /// Creates a toolbar row that grows trailing content.
    ///
    /// @param children the toolbar children
    /// @return the configured toolbar
    static HBox toolbar(Node... children) {
        HBox toolbar = new HBox(8.0, children);
        toolbar.getStyleClass().add("hmcl-page-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setMaxWidth(Double.MAX_VALUE);
        return toolbar;
    }

    /// Creates an empty-state label.
    ///
    /// @param text the empty-state text
    /// @return the label
    static M3Text emptyState(String text) {
        M3Text label = new M3Text(text, M3TextRole.BODY_MEDIUM);
        label.getStyleClass().add("hmcl-empty-state");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /// Returns the localized account type label.
    ///
    /// @param strings the localization service
    /// @param type the account type
    /// @return the type label
    static String accountTypeLabel(HMCLDemoStrings strings, HMCLDemoAccount.AccountType type) {
        return switch (type) {
            case MICROSOFT -> strings.get("accounts.type.microsoft");
            case OFFLINE -> strings.get("accounts.type.offline");
            case EXTERNAL -> strings.get("accounts.type.external");
        };
    }

    /// Creates a skin-face avatar view for an account.
    ///
    /// @param account the account
    /// @param size the logical size
    /// @return the avatar node
    static Node accountFace(HMCLDemoAccount account, double size) {
        return HMCLDemoAssets.skinFace(account.skinPath(), size);
    }

    /// Creates a settings action row with headline and supporting text.
    ///
    /// @param headline the primary text
    /// @param supporting the secondary text, or empty
    /// @return the configured setting item
    static M3SettingItem settingItem(String headline, String supporting) {
        M3SettingItem item = new M3SettingItem(headline);
        if (!supporting.isEmpty()) {
            item.setSupportingText(supporting);
        }
        item.setMaxWidth(Double.MAX_VALUE);
        return item;
    }
}
