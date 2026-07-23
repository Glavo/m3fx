// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
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
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays the download center with category navigation, version list, and installer selection.
@NotNullByDefault
final class HMCLDownloadView extends BorderPane {
    /// Download center categories.
    private enum Category {
        /// Official game versions.
        GAME,

        /// Modpacks.
        MODPACK,

        /// Mods.
        MOD,

        /// Resource packs.
        RESOURCE_PACK,

        /// Shader packs.
        SHADER,

        /// Worlds.
        WORLD
    }

    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// The game section label.
    private final M3Text gameSection = HMCLDemoUi.sectionLabel("");

    /// The content section label.
    private final M3Text contentSection = HMCLDemoUi.sectionLabel("");

    /// Category navigation rows.
    private final M3ListItem gameItem = HMCLDemoUi.navItem("", HMCLDemoIcons.HOME, null);
    private final M3ListItem modpackItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);
    private final M3ListItem modItem = HMCLDemoUi.navItem("", HMCLDemoIcons.EXTENSION, null);
    private final M3ListItem resourcePackItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem shaderItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem worldItem = HMCLDemoUi.navItem("", HMCLDemoIcons.WORLD, null);

    /// The center host for list or installer mode.
    private final StackPane centerHost = new StackPane();

    /// The currently selected category.
    private Category category = Category.GAME;

    /// Whether the installer chooser is open.
    private boolean installerOpen;

    /// The Minecraft version currently being installed, or `null`.
    private @Nullable HMCLDemoMinecraftVersion installingVersion;

    /// Creates the download page.
    ///
    /// @param strings the localization source
    /// @param state the shared state
    /// @param controller the application controller
    HMCLDownloadView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-secondary-page");
        gameItem.setOnAction(event -> showCategory(Category.GAME));
        modpackItem.setOnAction(event -> showCategory(Category.MODPACK));
        modItem.setOnAction(event -> showCategory(Category.MOD));
        resourcePackItem.setOnAction(event -> showCategory(Category.RESOURCE_PACK));
        shaderItem.setOnAction(event -> showCategory(Category.SHADER));
        worldItem.setOnAction(event -> showCategory(Category.WORLD));

        VBox sidebar = HMCLDemoUi.sidebar(
                gameSection,
                gameItem,
                modpackItem,
                contentSection,
                modItem,
                resourcePackItem,
                shaderItem,
                worldItem
        );
        setLeft(sidebar);
        setCenter(centerHost);

        state.getFilteredMinecraftVersions().addListener(
                (ListChangeListener<HMCLDemoMinecraftVersion>) change -> {
                    if (!installerOpen && category == Category.GAME) {
                        renderCenter();
                    }
                });
        refreshLocale();
        showCategory(Category.GAME);
    }

    /// Returns the title bar text for the current download sub-mode.
    ///
    /// @return the title text
    String titleText() {
        if (installerOpen && installingVersion != null) {
            return strings.format("download.install.title", installingVersion.name());
        }
        return strings.get("download.title");
    }

    /// Consumes Back while the installer chooser is open.
    ///
    /// @return `true` when Back was handled locally
    boolean consumeBack() {
        if (!installerOpen) {
            return false;
        }
        installerOpen = false;
        installingVersion = null;
        renderCenter();
        controller.refreshChrome();
        return true;
    }

    /// Updates static labels.
    void refreshLocale() {
        gameSection.setText(strings.get("download.section.game"));
        contentSection.setText(strings.get("download.section.content"));
        gameItem.setHeadlineText(strings.get("download.nav.game"));
        modpackItem.setHeadlineText(strings.get("download.nav.modpack"));
        modItem.setHeadlineText(strings.get("download.nav.mod"));
        resourcePackItem.setHeadlineText(strings.get("download.nav.resource_pack"));
        shaderItem.setHeadlineText(strings.get("download.nav.shader"));
        worldItem.setHeadlineText(strings.get("download.nav.world"));
        renderCenter();
    }

    /// Selects a download category.
    ///
    /// @param next the category
    private void showCategory(Category next) {
        category = next;
        installerOpen = false;
        installingVersion = null;
        gameItem.setSelected(next == Category.GAME);
        modpackItem.setSelected(next == Category.MODPACK);
        modItem.setSelected(next == Category.MOD);
        resourcePackItem.setSelected(next == Category.RESOURCE_PACK);
        shaderItem.setSelected(next == Category.SHADER);
        worldItem.setSelected(next == Category.WORLD);
        renderCenter();
    }

    /// Rebuilds the center pane for the current mode.
    private void renderCenter() {
        if (installerOpen && installingVersion != null) {
            centerHost.getChildren().setAll(installerContent(installingVersion));
            return;
        }
        centerHost.getChildren().setAll(switch (category) {
            case GAME -> gameVersionContent();
            case MODPACK, MOD, RESOURCE_PACK, SHADER, WORLD -> categoryPlaceholder();
        });
    }

    /// Creates the searchable Minecraft version list.
    ///
    /// @return the game-version content
    private Node gameVersionContent() {
        M3SearchBar searchBar = new M3SearchBar();
        searchBar.setPromptText(strings.get("download.search"));
        searchBar.textProperty().bindBidirectional(state.versionSearchQueryProperty());

        M3FilterChip release = filterChip(strings.get("download.filter.release"), state.isShowReleaseVersions());
        release.setOnAction(event -> state.setShowReleaseVersions(release.isSelected()));
        M3FilterChip snapshot = filterChip(strings.get("download.filter.snapshot"), state.isShowSnapshotVersions());
        snapshot.setOnAction(event -> state.setShowSnapshotVersions(snapshot.isSelected()));
        M3FilterChip old = filterChip(strings.get("download.filter.old"), state.isShowOldVersions());
        old.setOnAction(event -> state.setShowOldVersions(old.isSelected()));

        M3IconButton refresh = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.REFRESH));
        refresh.setOnAction(event -> controller.showMessageKey("snackbar.refreshed"));

        HBox toolbar = HMCLDemoUi.toolbar(searchBar, release, snapshot, old, HMCLDemoUi.hgrow(), refresh);
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        for (HMCLDemoMinecraftVersion version : state.getFilteredMinecraftVersions()) {
            list.getItems().add(versionRow(version));
        }

        VBox body = new VBox(toolbar, list);
        body.getStyleClass().add("hmcl-list-surface");
        VBox.setVgrow(list, Priority.ALWAYS);
        VBox column = HMCLDemoUi.contentColumn(body);
        VBox.setVgrow(body, Priority.ALWAYS);
        return column;
    }

    /// Creates one Minecraft version row.
    ///
    /// @param version the version
    /// @return the list item
    private M3ListItem versionRow(HMCLDemoMinecraftVersion version) {
        M3Text tag = new M3Text(HMCLDemoUi.channelLabel(strings, version.channel()), M3TextRole.LABEL_SMALL);
        tag.getStyleClass().add("hmcl-version-tag");

        M3Button install = new M3Button(strings.get("download.install"), M3ButtonVariant.TONAL);
        install.setOnAction(event -> openInstaller(version));

        HBox trailing = new HBox(8.0, tag, install);
        trailing.setAlignment(Pos.CENTER_RIGHT);

        M3ListItem row = new M3ListItem(version.name());
        row.getStyleClass().add("hmcl-version-row");
        row.setSupportingText(version.releaseTime());
        row.setLeading(HMCLDemoAssets.imageView("img/grass.png", 28.0, 28.0));
        row.setTrailing(trailing);
        return row;
    }

    /// Opens the installer chooser for a Minecraft version.
    ///
    /// @param version the selected version
    private void openInstaller(HMCLDemoMinecraftVersion version) {
        installingVersion = version;
        installerOpen = true;
        renderCenter();
        controller.refreshChrome();
    }

    /// Creates the installer selection grid.
    ///
    /// @param version the Minecraft version
    /// @return the installer content
    private Node installerContent(HMCLDemoMinecraftVersion version) {
        M3TextInputLayout name = new M3TextInputLayout(new M3TextField(version.name()));
        name.setLabelText(strings.get("download.install.name"));
        M3Card nameCard = new M3Card(name, M3CardVariant.FILLED);
        nameCard.getStyleClass().add("hmcl-installer-name-card");

        FlowPane grid = new FlowPane(12.0, 12.0);
        grid.getStyleClass().add("hmcl-installer-grid");
        grid.getChildren().setAll(
                installerCard("Vanilla", strings.get("download.installer.vanilla"), version),
                installerCard("Forge", strings.get("download.installer.forge"), version),
                installerCard("NeoForge", strings.get("download.installer.neoforge"), version),
                installerCard("Fabric", strings.get("download.installer.fabric"), version),
                installerCard("Quilt", strings.get("download.installer.quilt"), version),
                installerCard("OptiFine", strings.get("download.installer.optifine"), version),
                installerCard("LiteLoader", strings.get("download.installer.liteloader"), version),
                installerCard("Cleanroom", strings.get("download.installer.cleanroom"), version)
        );

        VBox column = HMCLDemoUi.contentColumn(nameCard, grid);
        return HMCLDemoUi.scroll(column);
    }

    /// Creates one installer tile.
    ///
    /// @param title the installer title
    /// @param body the installer description
    /// @param version the Minecraft version
    /// @return the card
    private M3Card installerCard(String title, String body, HMCLDemoMinecraftVersion version) {
        M3Text titleText = new M3Text(title, M3TextRole.TITLE_SMALL);
        M3Text bodyText = new M3Text(body, M3TextRole.BODY_SMALL);
        bodyText.setWrapText(true);
        VBox content = new VBox(8.0, titleText, bodyText);
        content.setPrefWidth(150.0);
        content.setMinHeight(96.0);
        M3Card card = new M3Card(content, M3CardVariant.OUTLINED);
        card.getStyleClass().add("hmcl-installer-card");
        card.setOnAction(event -> startInstall(version, title));
        return card;
    }

    /// Starts the dummy installation progress dialog.
    ///
    /// @param version the Minecraft version
    /// @param installer the installer name
    private void startInstall(HMCLDemoMinecraftVersion version, String installer) {
        String title = version.name() + " + " + installer;
        state.beginInstallation(title);

        M3Text heading = new M3Text(strings.get("download.progress.title"), M3TextRole.TITLE_MEDIUM);
        M3Text detail = new M3Text(strings.format("download.progress.detail", title), M3TextRole.BODY_MEDIUM);
        detail.setWrapText(true);
        M3ProgressBar progress = new M3ProgressBar();
        progress.progressProperty().bind(state.installProgressProperty());
        M3Text percent = new M3Text("0%", M3TextRole.LABEL_LARGE);
        state.installProgressProperty().addListener((observable, oldValue, newValue) ->
                percent.setText(Math.round(newValue.doubleValue() * 100.0) + "%"));

        VBox content = new VBox(14.0, heading, detail, progress, percent);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(8.0, 0.0, 0.0, 0.0));
        content.setPrefWidth(360.0);

        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("download.progress.header"));
        dialog.getDialogPane().setContent(content);
        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        dialog.getDialogPane().getActions().setAll(cancel);
        dialog.setOnHidden(event -> {
            state.cancelInstallation();
            installerOpen = false;
            installingVersion = null;
            renderCenter();
            controller.refreshChrome();
        });
        controller.overlay().showDialog(dialog);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(120.0), event -> {
            if (state.getInstallingTitle() == null) {
                timeline.stop();
                return;
            }
            double next = Math.min(1.0, state.getInstallProgress() + 0.08);
            state.setInstallProgress(next);
            if (next >= 1.0) {
                timeline.stop();
                controller.showMessageKey("snackbar.installed", title);
                state.addDemoInstance();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /// Creates a placeholder for non-game download categories.
    ///
    /// @return the placeholder node
    private Node categoryPlaceholder() {
        String title = switch (category) {
            case GAME -> strings.get("download.nav.game");
            case MODPACK -> strings.get("download.nav.modpack");
            case MOD -> strings.get("download.nav.mod");
            case RESOURCE_PACK -> strings.get("download.nav.resource_pack");
            case SHADER -> strings.get("download.nav.shader");
            case WORLD -> strings.get("download.nav.world");
        };
        M3Text titleText = new M3Text(title, M3TextRole.TITLE_LARGE);
        M3Text bodyText = new M3Text(strings.get("download.placeholder"), M3TextRole.BODY_MEDIUM);
        bodyText.setWrapText(true);
        VBox box = new VBox(12.0, titleText, bodyText);
        box.setPadding(new Insets(24.0));
        box.setMaxWidth(520.0);
        return HMCLDemoUi.scroll(box);
    }

    /// Creates a selected filter chip.
    ///
    /// @param text the chip text
    /// @param selected the initial selection
    /// @return the chip
    private static M3FilterChip filterChip(String text, boolean selected) {
        M3FilterChip chip = new M3FilterChip(text);
        chip.setSelected(selected);
        return chip;
    }
}
