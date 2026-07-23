// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Shared layout helpers for the HMCL Material 3 demo pages.
@NotNullByDefault
final class HMCLDemoUi {
    /// Standard HMCL left-pane width in logical pixels.
    static final double SIDEBAR_WIDTH = 200.0;

    /// Prevents utility-class instantiation.
    private HMCLDemoUi() {
    }

    /// Creates a transparent vertical scroll host that grows with its parent.
    ///
    /// @param content the scrolled content
    /// @return the configured scroll pane
    static ScrollPane scroll(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("hmcl-page-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        M3ScrollPanes.style(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    /// Creates a fixed-width left sidebar shell.
    ///
    /// @param children the sidebar children
    /// @return the configured sidebar
    static VBox sidebar(Node... children) {
        VBox sidebar = new VBox(4.0, children);
        sidebar.getStyleClass().add("hmcl-context-sidebar");
        sidebar.setPadding(new Insets(10.0, 8.0, 10.0, 8.0));
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
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

    /// Creates a one-line navigation row for a contextual left pane.
    ///
    /// @param headline the row text
    /// @param iconPath the icon path
    /// @param action the activation handler, or `null`
    /// @return the list item
    static M3ListItem navItem(String headline, String iconPath, @Nullable Runnable action) {
        M3ListItem item = new M3ListItem(headline);
        item.getStyleClass().add("hmcl-sidebar-item");
        item.setLeading(HMCLDemoIcons.create(iconPath));
        if (action != null) {
            item.setOnAction(event -> action.run());
        }
        return item;
    }

    /// Creates a two-line navigation row for a contextual left pane.
    ///
    /// @param headline the primary text
    /// @param supporting the secondary text
    /// @param leading the leading graphic
    /// @param action the activation handler, or `null`
    /// @return the list item
    static M3ListItem navItem(
            String headline,
            String supporting,
            Node leading,
            @Nullable Runnable action
    ) {
        M3ListItem item = new M3ListItem(headline);
        item.getStyleClass().add("hmcl-sidebar-item");
        item.setSupportingText(supporting);
        item.setLeading(leading);
        if (action != null) {
            item.setOnAction(event -> action.run());
        }
        return item;
    }

    /// Creates a vertical growing spacer.
    ///
    /// @return the spacer region
    static Region vgrow() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /// Creates a horizontal growing spacer.
    ///
    /// @return the spacer region
    static Region hgrow() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /// Returns a Minecraft skin-face avatar.
    ///
    /// @param account the account
    /// @param size the logical size
    /// @return the face image
    static ImageView accountFace(HMCLDemoAccount account, double size) {
        return HMCLDemoAssets.skinFace(account.skinPath(), size);
    }

    /// Returns an instance icon image view.
    ///
    /// @param instance the instance
    /// @param size the logical size
    /// @return the icon image
    static ImageView instanceIcon(HMCLDemoInstance instance, double size) {
        return HMCLDemoAssets.imageView(instance.iconPath(), size, size);
    }

    /// Resolves the localized provider label for an account type.
    ///
    /// @param strings the localization service
    /// @param type the account type
    /// @return the localized label
    static String accountTypeLabel(HMCLDemoStrings strings, HMCLDemoAccount.AccountType type) {
        return strings.get(switch (type) {
            case MICROSOFT -> "accounts.type.microsoft";
            case OFFLINE -> "accounts.type.offline";
            case EXTERNAL -> "accounts.type.external";
        });
    }

    /// Resolves the localized channel label for a Minecraft version.
    ///
    /// @param strings the localization service
    /// @param channel the release channel
    /// @return the localized label
    static String channelLabel(HMCLDemoStrings strings, HMCLDemoMinecraftVersion.Channel channel) {
        return strings.get(switch (channel) {
            case RELEASE -> "download.channel.release";
            case SNAPSHOT -> "download.channel.snapshot";
            case OLD_BETA -> "download.channel.old_beta";
            case OLD_ALPHA -> "download.channel.old_alpha";
        });
    }

    /// Creates a dense content column with standard page padding.
    ///
    /// @param children the column children
    /// @return the content column
    static VBox contentColumn(Node... children) {
        VBox column = new VBox(12.0, children);
        column.getStyleClass().add("hmcl-page-body");
        column.setPadding(new Insets(16.0));
        column.setFillWidth(true);
        return column;
    }

    /// Creates a toolbar row used above list surfaces.
    ///
    /// @param children the toolbar children
    /// @return the toolbar
    static HBox toolbar(Node... children) {
        HBox bar = new HBox(8.0, children);
        bar.getStyleClass().add("hmcl-page-toolbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }
}
