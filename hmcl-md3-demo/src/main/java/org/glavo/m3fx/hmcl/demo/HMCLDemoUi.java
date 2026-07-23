// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Shared layout helpers for the HMCL Material 3 demo pages.
///
/// HMCL pages never let list preferred heights drive the stage size. Scroll hosts and grow regions use
/// `min size = 0` so the decorator title bar keeps its fixed 40px allocation.
@NotNullByDefault
final class HMCLDemoUi {
    /// Standard HMCL left-pane width in logical pixels.
    static final double SIDEBAR_WIDTH = 200.0;

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
        // Styling and smooth wheel motion are separate M3FX opt-ins.
        M3ScrollPanes.style(scrollPane);
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    /// Creates a fixed-width left sidebar shell that can shrink vertically.
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
        spacer.setMinHeight(0.0);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /// Creates a horizontal growing spacer.
    ///
    /// @return the spacer region
    static Region hgrow() {
        Region spacer = new Region();
        spacer.setMinWidth(0.0);
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
        VBox column = fill(new VBox(12.0, children));
        column.getStyleClass().add("hmcl-page-body");
        column.setPadding(new Insets(16.0));
        column.setFillWidth(true);
        return column;
    }

    /// Alias for [#contentColumn(Node...)].
    ///
    /// @param children the column children
    /// @return the content column
    static VBox pageColumn(Node... children) {
        return contentColumn(children);
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

    /// Creates a toolbar row used above list surfaces.
    ///
    /// @param children the toolbar children
    /// @return the toolbar
    static HBox toolbar(Node... children) {
        HBox bar = new HBox(8.0, children);
        bar.getStyleClass().add("hmcl-page-toolbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setMinHeight(Region.USE_PREF_SIZE);
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        return bar;
    }

    /// Wraps a non-virtualized list so its full preferred height cannot resize the window.
    ///
    /// @param list the list node
    /// @return a scroll host with zero minimum height
    static ScrollPane listHost(Node list) {
        if (list instanceof Region region) {
            region.setMinHeight(0.0);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        return scroll(list);
    }

    /// Wraps page content so [org.glavo.m3fx.animation.M3AnimatedContent] stretches it to the host size.
    ///
    /// `M3AnimatedContent` sizes children to their preferred height. Pages with sidebars must claim the full host
    /// height; otherwise left panes only cover the content intrinsic height.
    ///
    /// @param content the page root
    /// @param hostWidth the animated host width
    /// @param hostHeight the animated host height
    /// @return a fill wrapper
    static Region fillHost(Node content, ReadOnlyDoubleProperty hostWidth, ReadOnlyDoubleProperty hostHeight) {
        return new PageFillHost(content, hostWidth, hostHeight);
    }

    /// Stack pane that reports the animated host size as its preferred size.
    private static final class PageFillHost extends StackPane {
        /// Width of the surrounding animated content host.
        private final ReadOnlyDoubleProperty hostWidth;

        /// Height of the surrounding animated content host.
        private final ReadOnlyDoubleProperty hostHeight;

        /// Creates a fill host around page content.
        ///
        /// @param content the page root
        /// @param hostWidth the host width
        /// @param hostHeight the host height
        PageFillHost(Node content, ReadOnlyDoubleProperty hostWidth, ReadOnlyDoubleProperty hostHeight) {
            this.hostWidth = hostWidth;
            this.hostHeight = hostHeight;
            getStyleClass().add("hmcl-page-fill-host");
            setMinSize(0.0, 0.0);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            if (content instanceof Region region) {
                fill(region);
            }
            getChildren().setAll(content);
            hostWidth.addListener((observable, oldValue, newValue) -> requestLayout());
            hostHeight.addListener((observable, oldValue, newValue) -> requestLayout());
        }

        @Override
        protected double computeMinWidth(double height) {
            return 0.0;
        }

        @Override
        protected double computeMinHeight(double width) {
            return 0.0;
        }

        @Override
        protected double computePrefWidth(double height) {
            double width = hostWidth.get();
            return width > 0.0 ? width : super.computePrefWidth(height);
        }

        @Override
        protected double computePrefHeight(double width) {
            double height = hostHeight.get();
            return height > 0.0 ? height : super.computePrefHeight(width);
        }
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