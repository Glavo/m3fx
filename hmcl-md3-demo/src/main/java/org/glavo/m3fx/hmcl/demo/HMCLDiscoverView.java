// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Locale;

/// Displays HMCL's download categories, Minecraft version list, and a dummy new-game installation workflow.
@NotNullByDefault
public final class HMCLDiscoverView extends HMCLDemoView {
    /// The Minecraft version selected for the installation workflow, or `null` while browsing.
    private @Nullable String selectedVersion;

    /// Whether the page currently displays the installer-choice workflow.
    private boolean installerMode;

    /// Creates the download page.
    ///
    /// @param strings the localization source
    /// @param state the shared demo state
    /// @param actions the application command sink
    public HMCLDiscoverView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        initializeView();
    }

    /// Creates either the version browser or the installer-choice workflow.
    ///
    /// @return the current download page tree
    @Override
    protected Node createContent() {
        return installerMode && selectedVersion != null
                ? createInstallerPage(selectedVersion)
                : createDownloadPage();
    }

    /// Returns the shell title appropriate for the current workflow step.
    ///
    /// @return the localized page title
    String getPageTitle() {
        return installerMode && selectedVersion != null
                ? text("download.install.title", selectedVersion)
                : text("discover.title");
    }

    /// Returns whether the page is inside the installer-choice workflow.
    ///
    /// @return `true` while the installer workflow is visible
    boolean isInstallerMode() {
        return installerMode;
    }

    /// Leaves the installer workflow and restores the Minecraft version list.
    void exitInstallerMode() {
        installerMode = false;
        selectedVersion = null;
        refreshView();
    }

    /// Creates the contextual download sidebar and dense Minecraft version browser.
    ///
    /// @return the download browser
    private Node createDownloadPage() {
        return contextualPage(createDownloadSidebar(), createVersionSurface());
    }

    /// Creates the fixed download-category sidebar used by HMCL.
    ///
    /// @return the category sidebar
    private Node createDownloadSidebar() {
        VBox sidebar = new VBox(5.0);
        sidebar.setPadding(new Insets(12.0, 8.0, 10.0, 8.0));
        sidebar.setPrefWidth(200.0);
        sidebar.setMinWidth(200.0);
        sidebar.setMaxWidth(200.0);

        M3Text newGame = new M3Text(text("download.section.new_game"), M3TextRole.LABEL_MEDIUM);
        newGame.getStyleClass().add("hmcl-sidebar-section-label");
        M3ListPane gameCategories = sidebarList();
        M3ListItem minecraft = categoryItem(
                text("download.game.minecraft"),
                HMCLDemoAssets.imageView("img/grass.png", 24.0, 24.0)
        );
        minecraft.setSelected(true);
        gameCategories.getItems().addAll(
                minecraft,
                categoryItem(text("discover.filter.modpacks"), HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES))
        );

        M3Text content = new M3Text(text("download.section.content"), M3TextRole.LABEL_MEDIUM);
        content.getStyleClass().add("hmcl-sidebar-section-label");
        M3ListPane contentCategories = sidebarList();
        contentCategories.getItems().addAll(
                categoryItem(text("discover.filter.mods"), HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES)),
                categoryItem(text("discover.filter.resources"), HMCLDemoIcons.create(HMCLDemoIcons.HOME)),
                categoryItem(text("discover.filter.shaders"), HMCLDemoIcons.create(HMCLDemoIcons.DISCOVER)),
                categoryItem(text("instance.navigation.worlds"), HMCLDemoIcons.create(HMCLDemoIcons.CHAT))
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(newGame, gameCategories, content, contentCategories, spacer);
        return sidebar;
    }

    /// Creates one download-category row.
    ///
    /// @param label the localized category label
    /// @param graphic the category icon
    /// @return the configured category item
    private M3ListItem categoryItem(String label, Node graphic) {
        M3ListItem item = new M3ListItem(label);
        item.getStyleClass().add("hmcl-sidebar-item");
        item.setLeading(graphic);
        item.setOneLineHeight(40.0);
        item.setOnAction(event -> actions.dispatch("download-category"));
        return item;
    }

    /// Creates a non-empty category list for the contextual sidebar.
    ///
    /// @return the configured sidebar list
    private static M3ListPane sidebarList() {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.SINGLE);
        list.setAllowEmptySelection(false);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }

    /// Creates the version filters, refresh action, and dense release rows.
    ///
    /// @return the version-list surface
    private Node createVersionSurface() {
        M3TextField search = new M3TextField();
        search.setPromptText(text("download.search_versions"));
        search.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(search, Priority.ALWAYS);

        M3FilterChip all = new M3FilterChip(text("discover.filter.all"));
        M3FilterChip releases = new M3FilterChip(text("download.filter.releases"));
        M3FilterChip snapshots = new M3FilterChip(text("download.filter.snapshots"));
        all.setSelected(true);

        M3IconButton refresh = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.REFRESH));
        refresh.setAccessibleText(text("common.refresh"));
        refresh.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH));

        HBox toolbar = new HBox(6.0, search, all, releases, snapshots, refresh);
        toolbar.getStyleClass().add("hmcl-list-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        M3ListPane versions = denseList();
        Runnable update = () -> populateVersions(
                versions,
                search.getText(),
                releases.isSelected() ? "release" : snapshots.isSelected() ? "snapshot" : null
        );
        all.setOnAction(event -> {
            selectOnly(all, releases, snapshots);
            update.run();
        });
        releases.setOnAction(event -> {
            selectOnly(releases, all, snapshots);
            update.run();
        });
        snapshots.setOnAction(event -> {
            selectOnly(snapshots, all, releases);
            update.run();
        });
        search.textProperty().addListener((observable, oldText, newText) -> update.run());
        update.run();

        ScrollPane listScroll = new ScrollPane(versions);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        M3ScrollPanes.style(listScroll);

        VBox surface = new VBox(0.0, toolbar, listScroll);
        surface.getStyleClass().add("hmcl-list-surface");
        surface.setMinWidth(0.0);
        surface.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        return surface;
    }

    /// Selects one filter chip and clears the supplied alternatives.
    ///
    /// @param selected the chip that becomes selected
    /// @param others the chips that become unselected
    private static void selectOnly(M3FilterChip selected, M3FilterChip... others) {
        selected.setSelected(true);
        for (M3FilterChip other : others) {
            other.setSelected(false);
        }
    }

    /// Populates the Minecraft version list from deterministic demo fixtures.
    ///
    /// @param target the list that receives version rows
    /// @param query the user-entered search text
    /// @param channel the optional release channel filter
    private void populateVersions(M3ListPane target, String query, @Nullable String channel) {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        target.getItems().clear();
        for (VersionEntry entry : versionEntries()) {
            if ((channel == null || entry.channel().equals(channel))
                    && (normalized.isEmpty() || entry.version().toLowerCase(Locale.ROOT).contains(normalized))) {
                target.getItems().add(createVersionRow(entry));
            }
        }
        if (target.getItems().isEmpty()) {
            target.getItems().add(new M3ListItem(text("download.versions.empty")));
        }
    }

    /// Returns immutable deterministic version rows that resemble HMCL's remote version feed.
    ///
    /// @return the version fixtures in display order
    private static @Unmodifiable List<VersionEntry> versionEntries() {
        return List.of(
                new VersionEntry("26.2", "release", "2026-06-18"),
                new VersionEntry("26.1", "release", "2026-05-28"),
                new VersionEntry("26.3-snapshot-4", "snapshot", "2026-07-17"),
                new VersionEntry("1.21.11", "release", "2025-12-09"),
                new VersionEntry("1.21.10", "release", "2025-10-07"),
                new VersionEntry("25w37a", "snapshot", "2025-09-10"),
                new VersionEntry("1.20.6", "release", "2024-04-29")
        );
    }

    /// Creates one compact Minecraft version row.
    ///
    /// @param entry the represented version fixture
    /// @return the configured version row
    private M3ListItem createVersionRow(VersionEntry entry) {
        M3ListItem row = new M3ListItem(entry.version());
        row.getStyleClass().add("hmcl-version-row");
        row.setLeading(HMCLDemoAssets.imageView("img/grass.png", 32.0, 32.0));
        row.setSupportingText(text(
                "download.version.meta",
                text("download.channel." + entry.channel()),
                entry.date()
        ));
        row.setOneLineHeight(50.0);
        row.setTwoLineHeight(50.0);

        M3Text channel = new M3Text(
                text("download.channel." + entry.channel()),
                M3TextRole.LABEL_SMALL
        );
        channel.getStyleClass().add("hmcl-version-tag");
        M3Button install = new M3Button(text("common.install"), M3ButtonVariant.TEXT);
        install.setOnAction(event -> openInstaller(entry.version()));
        HBox trailing = new HBox(4.0, channel, install);
        trailing.setAlignment(Pos.CENTER_RIGHT);
        row.setTrailing(trailing);
        row.setOnAction(event -> openInstaller(entry.version()));
        return row;
    }

    /// Opens the installer-choice step for one Minecraft version.
    ///
    /// @param version the selected version identifier
    private void openInstaller(String version) {
        selectedVersion = version;
        installerMode = true;
        refreshView();
        actions.dispatch("discover-mode");
    }

    /// Creates HMCL's no-sidebar new-game installation workflow.
    ///
    /// @param version the selected Minecraft version
    /// @return the installer-choice page
    private Node createInstallerPage(String version) {
        M3TextField name = new M3TextField(text("download.install.default_name", version));
        name.setPromptText(text("download.install.name"));
        name.setMaxWidth(Double.MAX_VALUE);
        M3Text duplicateWarning = new M3Text(
                text("download.install.duplicate_warning"),
                M3TextRole.BODY_SMALL
        );
        duplicateWarning.getStyleClass().add("hmcl-install-validation");
        duplicateWarning.setWrapText(true);
        VBox nameContent = new VBox(6.0, name, duplicateWarning);
        M3Card nameCard = new M3Card(nameContent, M3CardVariant.FILLED);
        nameCard.getStyleClass().add("hmcl-installer-name-card");

        FlowPane installers = new FlowPane(16.0, 16.0);
        installers.getStyleClass().add("hmcl-installer-grid");
        installers.setAlignment(Pos.TOP_LEFT);
        installers.getChildren().addAll(
                createInstallerCard("Minecraft", "img/grass.png", version),
                createInstallerCard("Forge", "img/forge.png", "61.0.3"),
                createInstallerCard("NeoForge", "img/neoforge.png", "21.8.42"),
                createInstallerCard("OptiFine", "img/optifine.png", "HD U J6"),
                createInstallerCard("Fabric", "img/fabric.png", "0.17.2"),
                createInstallerCard("Fabric API", "img/fabric.png", "0.133.0"),
                createInstallerCard("Quilt", "img/quilt.png", "0.29.1"),
                createInstallerCard("QSL / QFAPI", "img/quilt.png", "10.0.0")
        );

        M3Button install = new M3Button(text("common.install"), M3ButtonVariant.FILLED);
        install.setPrefWidth(100.0);
        install.setOnAction(event -> showInstallProgressDialog(version));
        HBox actionRow = new HBox(install);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        BorderPane page = new BorderPane();
        page.getStyleClass().add("hmcl-installer-page");
        page.setTop(nameCard);
        page.setCenter(installers);
        page.setBottom(actionRow);
        BorderPane.setMargin(nameCard, new Insets(16.0, 16.0, 12.0, 16.0));
        BorderPane.setMargin(installers, new Insets(0.0, 16.0, 12.0, 16.0));
        BorderPane.setMargin(actionRow, new Insets(0.0, 16.0, 16.0, 16.0));
        return page;
    }

    /// Creates one actionable installer choice card.
    ///
    /// @param title the installer name
    /// @param imagePath the HMCL image resource
    /// @param version the deterministic installer version
    /// @return the configured installer card
    private M3Card createInstallerCard(String title, String imagePath, String version) {
        ImageView icon = HMCLDemoAssets.imageView(imagePath, 32.0, 32.0);
        M3Text titleText = new M3Text(title, M3TextRole.TITLE_SMALL);
        M3Text versionText = new M3Text(version, M3TextRole.BODY_SMALL);
        VBox content = new VBox(8.0, icon, titleText, versionText);
        M3Card card = new M3Card(content, M3CardVariant.OUTLINED);
        card.getStyleClass().add("hmcl-installer-card");
        card.setPrefWidth(172.0);
        card.setMinWidth(172.0);
        card.setMaxWidth(172.0);
        card.setPrefHeight(116.0);
        card.setOnAction(event -> actions.dispatch("select-installer"));
        return card;
    }

    /// Shows a deterministic Material progress dialog over the installer workflow.
    ///
    /// @param version the Minecraft version being installed
    private void showInstallProgressDialog(String version) {
        @Nullable M3OverlayPane overlay = overlay();
        if (overlay == null) {
            actions.dispatch(HMCLDemoActions.ACTION_INSTALL, version);
            return;
        }

        M3Text prepare = new M3Text(text("download.progress.prepare"), M3TextRole.BODY_MEDIUM);
        M3Text libraries = new M3Text(text("download.progress.libraries"), M3TextRole.BODY_MEDIUM);
        M3Text assets = new M3Text(text("download.progress.assets"), M3TextRole.BODY_MEDIUM);
        M3ProgressBar progress = new M3ProgressBar(0.64);
        M3Text status = new M3Text(text("download.progress.status", 64), M3TextRole.BODY_SMALL);
        VBox progressContent = new VBox(12.0, prepare, libraries, assets, progress, status);
        progressContent.setPrefWidth(452.0);
        progressContent.setPrefHeight(210.0);

        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(text("download.progress.title", version));
        dialog.getDialogPane().setContent(progressContent);
        dialog.getDialogPane().setPrefWidth(500.0);

        M3Button cancel = new M3Button(text("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        dialog.getDialogPane().getActions().setAll(cancel);
        overlay.showDialog(dialog);
    }

    /// Returns the root overlay that can host an in-scene Material dialog.
    ///
    /// @return the current overlay root, or `null` before attachment
    private @Nullable M3OverlayPane overlay() {
        @Nullable Scene scene = getScene();
        if (scene == null) {
            return null;
        }
        Parent root = scene.getRoot();
        return root instanceof M3OverlayPane overlay ? overlay : null;
    }

    /// Creates a non-selecting dense list for version rows.
    ///
    /// @return the configured list pane
    private static M3ListPane denseList() {
        M3ListPane list = new M3ListPane();
        list.getStyleClass().add("hmcl-dense-list");
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }

    /// Describes one deterministic Minecraft version feed row.
    ///
    /// @param version the displayed Minecraft version
    /// @param channel the localization suffix for its release channel
    /// @param date the displayed release date
    @NotNullByDefault
    private record VersionEntry(String version, String channel, String date) {
    }
}
