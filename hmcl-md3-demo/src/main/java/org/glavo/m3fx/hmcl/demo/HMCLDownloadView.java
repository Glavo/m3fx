// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3SearchBar;
import org.jetbrains.annotations.NotNullByDefault;

/// Download center primary destination.
@NotNullByDefault
final class HMCLDownloadView extends BorderPane {
    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final M3ListItem gameItem = HMCLDemoUi.navItem(HMCLDemoIcons.HOME);
    private final M3ListItem modpackItem = HMCLDemoUi.navItem(HMCLDemoIcons.DOWNLOAD);
    private final M3ListItem modItem = HMCLDemoUi.navItem(HMCLDemoIcons.EXTENSION);
    private final M3ListItem resourcePackItem = HMCLDemoUi.navItem(HMCLDemoIcons.IMAGE);
    private final M3ListItem shaderItem = HMCLDemoUi.navItem(HMCLDemoIcons.IMAGE);
    private final M3ListItem worldItem = HMCLDemoUi.navItem(HMCLDemoIcons.WORLD);

    /// Scrollable contextual navigation content.
    private final VBox sidebar;

    private final M3AnimatedContent centerHost = new M3AnimatedContent();
    private HMCLDemoRoute.DownloadCategory category = HMCLDemoRoute.DownloadCategory.GAME;

    /// Creates the download page.
    ///
    /// @param controller the application controller
    HMCLDownloadView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().addAll("hmcl-download-page", "hmcl-secondary-page");
        HMCLDemoUi.fill(this);

        gameItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.GAME));
        modpackItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.MODPACK));
        modItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.MOD));
        resourcePackItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.RESOURCE_PACK));
        shaderItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.SHADER));
        worldItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.WORLD));

        sidebar = HMCLDemoUi.sidebar(
                HMCLDemoUi.sectionLabel(""),
                gameItem,
                modpackItem,
                HMCLDemoUi.sectionLabel(""),
                modItem,
                resourcePackItem,
                shaderItem,
                worldItem
        );

        HMCLDemoUi.fill(centerHost);
        centerHost.setFitToWidth(true);
        centerHost.setFitToHeight(true);
        setLeft(HMCLDemoUi.sidebarHost(sidebar));
        setCenter(centerHost);

        state.getFilteredMinecraftVersions().addListener(
                (ListChangeListener<HMCLDemoMinecraftVersion>) change -> render(false));
        state.getCatalogItems().addListener((ListChangeListener<HMCLDemoCatalogItem>) change -> render(false));
        state.versionSearchQueryProperty().addListener((observable, oldValue, newValue) -> render(false));
        state.catalogSearchQueryProperty().addListener((observable, oldValue, newValue) -> render(false));
        state.showReleaseVersionsProperty().addListener((observable, oldValue, newValue) -> render(false));
        state.showSnapshotVersionsProperty().addListener((observable, oldValue, newValue) -> render(false));
        state.showOldVersionsProperty().addListener((observable, oldValue, newValue) -> render(false));

        refreshLocale();
        render(false);
    }

    /// Shows a download category.
    ///
    /// @param next the category
    void showCategory(HMCLDemoRoute.DownloadCategory next) {
        boolean changed = category != next;
        category = next;
        render(changed);
        syncNav();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        sidebar.getChildren().set(0, HMCLDemoUi.sectionLabel(strings.get("download.section.game")));
        sidebar.getChildren().set(3, HMCLDemoUi.sectionLabel(strings.get("download.section.content")));
        gameItem.setHeadlineText(strings.get("download.nav.game"));
        modpackItem.setHeadlineText(strings.get("download.nav.modpack"));
        modItem.setHeadlineText(strings.get("download.nav.mod"));
        resourcePackItem.setHeadlineText(strings.get("download.nav.resource_pack"));
        shaderItem.setHeadlineText(strings.get("download.nav.shader"));
        worldItem.setHeadlineText(strings.get("download.nav.world"));
        render(false);
    }

    private void syncNav() {
        gameItem.setSelected(category == HMCLDemoRoute.DownloadCategory.GAME);
        modpackItem.setSelected(category == HMCLDemoRoute.DownloadCategory.MODPACK);
        modItem.setSelected(category == HMCLDemoRoute.DownloadCategory.MOD);
        resourcePackItem.setSelected(category == HMCLDemoRoute.DownloadCategory.RESOURCE_PACK);
        shaderItem.setSelected(category == HMCLDemoRoute.DownloadCategory.SHADER);
        worldItem.setSelected(category == HMCLDemoRoute.DownloadCategory.WORLD);
    }

    private void render(boolean animate) {
        syncNav();
        centerHost.setContentTransform(animate && !state.isAnimationDisabled()
                ? HMCLDemoTransitions.sectionUp()
                : HMCLDemoTransitions.none());
        centerHost.setContent(category == HMCLDemoRoute.DownloadCategory.GAME ? gameContent() : catalogContent());
        if (!animate || state.isAnimationDisabled()) {
            centerHost.snapToCurrentState();
        }
    }

    private VBox gameContent() {
        M3SearchBar search = new M3SearchBar(strings.get("download.search"));
        search.setText(state.getVersionSearchQuery());
        search.textProperty().addListener((observable, oldValue, newValue) ->
                state.setVersionSearchQuery(newValue == null ? "" : newValue));

        M3FilterChip release = new M3FilterChip(strings.get("download.filter.release"));
        M3FilterChip snapshot = new M3FilterChip(strings.get("download.filter.snapshot"));
        M3FilterChip old = new M3FilterChip(strings.get("download.filter.old"));
        release.setSelected(state.isShowReleaseVersions());
        snapshot.setSelected(state.isShowSnapshotVersions());
        old.setSelected(state.isShowOldVersions());
        release.setOnAction(event -> state.setShowReleaseVersions(release.isSelected()));
        snapshot.setOnAction(event -> state.setShowSnapshotVersions(snapshot.isSelected()));
        old.setOnAction(event -> state.setShowOldVersions(old.isSelected()));
        FlowPane filters = new FlowPane(8.0, 8.0, release, snapshot, old);

        VBox list = new VBox(4.0);
        for (HMCLDemoMinecraftVersion version : state.getFilteredMinecraftVersions()) {
            M3Button install = new M3Button(strings.get("download.install"), M3ButtonVariant.TEXT);
            install.getStyleClass().add("hmcl-row-action");
            install.setMinWidth(Region.USE_PREF_SIZE);
            install.setOnAction(event -> controller.startInstallWizard(version));
            M3ListItem row = new M3ListItem(version.name());
            row.setSupportingText(channelLabel(version.channel()) + " · " + version.releaseTime());
            row.setTrailing(install);
            row.setMaxWidth(Double.MAX_VALUE);
            list.getChildren().add(row);
        }
        if (list.getChildren().isEmpty()) {
            list.getChildren().add(HMCLDemoUi.emptyState(strings.get("download.placeholder")));
        }

        VBox column = HMCLDemoUi.pageColumn(search, filters, list);
        return wrapScroll(column);
    }

    private VBox catalogContent() {
        M3SearchBar search = new M3SearchBar(strings.get("download.search.catalog"));
        search.setText(state.getCatalogSearchQuery());
        search.textProperty().addListener((observable, oldValue, newValue) ->
                state.setCatalogSearchQuery(newValue == null ? "" : newValue));

        HMCLDemoCatalogItem.Kind kind = switch (category) {
            case MODPACK -> HMCLDemoCatalogItem.Kind.MODPACK;
            case MOD, GAME -> HMCLDemoCatalogItem.Kind.MOD;
            case RESOURCE_PACK -> HMCLDemoCatalogItem.Kind.RESOURCE_PACK;
            case SHADER -> HMCLDemoCatalogItem.Kind.SHADER;
            case WORLD -> HMCLDemoCatalogItem.Kind.WORLD;
        };

        VBox list = new VBox(4.0);
        for (HMCLDemoCatalogItem item : state.getCatalog(kind)) {
            M3Button install = new M3Button(strings.get("download.install"), M3ButtonVariant.TEXT);
            install.getStyleClass().add("hmcl-row-action");
            install.setMinWidth(Region.USE_PREF_SIZE);
            install.setOnAction(event -> controller.showMessageKey("snackbar.installed", item.title()));
            M3ListItem row = new M3ListItem(item.title());
            row.setSupportingText(strings.format(
                    "download.catalog.support",
                    item.author(),
                    item.downloads(),
                    item.summary()
            ));
            row.setTrailing(install);
            row.setMaxWidth(Double.MAX_VALUE);
            list.getChildren().add(row);
        }
        if (list.getChildren().isEmpty()) {
            list.getChildren().add(HMCLDemoUi.emptyState(strings.get("download.placeholder")));
        }
        return wrapScroll(HMCLDemoUi.pageColumn(search, list));
    }

    private String channelLabel(HMCLDemoMinecraftVersion.Channel channel) {
        return switch (channel) {
            case RELEASE -> strings.get("download.channel.release");
            case SNAPSHOT -> strings.get("download.channel.snapshot");
            case OLD_BETA -> strings.get("download.channel.old_beta");
            case OLD_ALPHA -> strings.get("download.channel.old_alpha");
        };
    }

    private static VBox wrapScroll(VBox column) {
        VBox host = HMCLDemoUi.fill(new VBox(HMCLDemoUi.scroll(column)));
        VBox.setVgrow(host.getChildren().get(0), Priority.ALWAYS);
        return host;
    }
}
