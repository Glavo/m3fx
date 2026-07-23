// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// HMCL home page modeled on `RootPage` + `MainPage`.
///
/// The wallpaper is owned by the decorator shell. This page only contributes the left navigation
/// (`AdvancedListBox` equivalent) and the bottom-right launch pane.
@NotNullByDefault
final class HMCLHomeView extends BorderPane {
    /// The localization source used by this page.
    private final HMCLDemoStrings strings;

    /// The shared deterministic state rendered by this page.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// Left navigation column matching HMCL's 200px sidebar.
    private final VBox sidebar = new VBox(0.0);

    private final M3Text accountSection = sectionLabel();
    private final M3Text versionSection = sectionLabel();
    private final M3Text launcherSection = sectionLabel();
    private final M3ListItem accountItem = new M3ListItem();
    private final M3ListItem currentInstanceItem = new M3ListItem();
    private final M3ListItem allInstancesItem = new M3ListItem();
    private final M3ListItem downloadItem = new M3ListItem();
    private final M3ListItem settingsItem = new M3ListItem();
    private final M3ListItem multiplayerItem = new M3ListItem();
    private final M3ListItem feedbackItem = new M3ListItem();

    /// Preview-channel announcement card.
    private final VBox announcementCard = new VBox(16.0);

    /// Announcement title.
    private final M3Text announcementTitle = new M3Text("", M3TextRole.TITLE_SMALL);

    /// Announcement body.
    private final M3Text announcementBody = new M3Text("", M3TextRole.BODY_MEDIUM);

    /// Whether the preview notice remains visible for this session.
    private boolean announcementVisible = true;

    /// HMCL `.launch-pane` container.
    private final HBox launchPane = new HBox();

    /// Primary launch button.
    private final M3Button launchButton = new M3Button();

    /// Version-switch menu button.
    private final M3Button menuButton = new M3Button();

    /// Launch label ("启动").
    private final M3Text launchLabel = new M3Text("", M3TextRole.TITLE_MEDIUM);

    /// Current instance name under the launch label.
    private final M3Text launchInstance = new M3Text("", M3TextRole.BODY_SMALL);

    /// Graphic stack for the launch button.
    private final VBox launchGraphic = new VBox();

    /// Creates the wallpaper-first home page.
    ///
    /// @param strings the localization source
    /// @param state the shared demo state
    /// @param controller the application controller
    HMCLHomeView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-home-page");
        HMCLDemoUi.fill(this);
        configureSidebar();
        configureAnnouncement();
        configureLaunchPane();

        StackPane center = new StackPane();
        center.getStyleClass().add("hmcl-home-center");
        center.setPadding(new Insets(20.0));
        StackPane.setAlignment(announcementCard, Pos.TOP_CENTER);
        StackPane.setAlignment(launchPane, Pos.BOTTOM_RIGHT);
        center.getChildren().setAll(announcementCard, launchPane);

        setLeft(sidebar);
        setCenter(center);

        state.selectedAccountProperty().addListener((observable, oldValue, newValue) -> refreshAccount());
        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> refreshInstance());
        state.getAccounts().addListener((ListChangeListener<HMCLDemoAccount>) change -> refreshAccount());
        state.getInstances().addListener((ListChangeListener<HMCLDemoInstance>) change -> refreshInstance());

        refreshLocale();
        refreshAccount();
        refreshInstance();
    }

    /// Updates every static label owned by this page.
    void refreshLocale() {
        accountSection.setText(strings.get("home.section.account"));
        versionSection.setText(strings.get("home.section.version"));
        launcherSection.setText(strings.get("home.section.launcher"));
        allInstancesItem.setHeadlineText(strings.get("home.all_instances"));
        downloadItem.setHeadlineText(strings.get("home.download"));
        settingsItem.setHeadlineText(strings.get("home.launcher_settings"));
        multiplayerItem.setHeadlineText(strings.get("home.multiplayer"));
        feedbackItem.setHeadlineText(strings.get("home.feedback"));
        announcementTitle.setText(strings.get("home.preview.title"));
        announcementBody.setText(strings.get("home.preview.body") + "\n" + strings.get("home.preview.feedback"));
        launchLabel.setText(strings.get("home.launch"));
        menuButton.setAccessibleText(strings.get("home.switch_instance"));
        refreshAccount();
        refreshInstance();
    }

    /// Builds the fixed left navigation once.
    private void configureSidebar() {
        sidebar.getStyleClass().add("hmcl-home-sidebar");
        sidebar.setPrefWidth(HMCLDemoUi.SIDEBAR_WIDTH);
        sidebar.setMinWidth(HMCLDemoUi.SIDEBAR_WIDTH);
        sidebar.setMaxWidth(HMCLDemoUi.SIDEBAR_WIDTH);
        sidebar.setPadding(new Insets(12.0, 0.0, 0.0, 0.0));

        styleNav(accountItem);
        styleNav(currentInstanceItem);
        styleNav(allInstancesItem);
        styleNav(downloadItem);
        styleNav(settingsItem);
        styleNav(multiplayerItem);
        styleNav(feedbackItem);

        accountItem.setOnAction(event -> controller.openAccounts());
        currentInstanceItem.setOnAction(event -> controller.openSelectedInstance());
        allInstancesItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES));
        allInstancesItem.setOnAction(event -> controller.openInstances());
        downloadItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.DOWNLOAD));
        downloadItem.setOnAction(event -> controller.openDownload());
        settingsItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.SETTINGS));
        settingsItem.setOnAction(event -> controller.openSettings());
        multiplayerItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.GROUP));
        multiplayerItem.setOnAction(event -> controller.openMultiplayer());
        feedbackItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.CHAT));
        feedbackItem.setOnAction(event -> controller.showMessageKey("snackbar.feedback"));

        sidebar.getChildren().setAll(
                accountSection,
                accountItem,
                versionSection,
                currentInstanceItem,
                allInstancesItem,
                downloadItem,
                launcherSection,
                settingsItem,
                multiplayerItem,
                HMCLDemoUi.vgrow(),
                feedbackItem
        );
    }

    /// Builds the preview announcement once.
    private void configureAnnouncement() {
        announcementCard.getStyleClass().addAll("hmcl-card", "hmcl-announcement");
        announcementCard.setMaxWidth(560.0);
        announcementCard.setMaxHeight(Region.USE_PREF_SIZE);
        announcementBody.setWrapText(true);

        M3IconButton close = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.CLOSE));
        close.getStyleClass().add("hmcl-announcement-close");
        close.setOnAction(event -> {
            announcementVisible = false;
            announcementCard.setVisible(false);
            announcementCard.setManaged(false);
        });
        BorderPane header = new BorderPane();
        header.setLeft(announcementTitle);
        header.setRight(close);
        BorderPane.setAlignment(announcementTitle, Pos.CENTER_LEFT);
        announcementCard.getChildren().setAll(header, announcementBody);
        announcementCard.setVisible(announcementVisible);
        announcementCard.setManaged(announcementVisible);
    }

    /// Builds the HMCL launch pane once.
    private void configureLaunchPane() {
        launchPane.getStyleClass().add("hmcl-launch-pane");
        launchPane.setMinSize(230.0, 57.0);
        launchPane.setPrefSize(230.0, 57.0);
        launchPane.setMaxSize(230.0, 57.0);

        launchLabel.getStyleClass().add("hmcl-launch-label");
        launchInstance.getStyleClass().add("hmcl-launch-instance");
        launchGraphic.setAlignment(Pos.CENTER);
        launchGraphic.getChildren().setAll(launchLabel, launchInstance);

        launchButton.getStyleClass().add("hmcl-launch-button");
        launchButton.setVariant(M3ButtonVariant.FILLED);
        launchButton.setGraphic(launchGraphic);
        launchButton.setMinSize(200.0, 55.0);
        launchButton.setPrefSize(200.0, 55.0);
        launchButton.setMaxSize(200.0, 55.0);
        launchButton.setOnAction(event -> controller.launchSelected());

        menuButton.getStyleClass().add("hmcl-launch-menu-button");
        menuButton.setVariant(M3ButtonVariant.FILLED);
        menuButton.setGraphic(HMCLDemoIcons.create(HMCLDemoIcons.ARROW_DROP_UP));
        menuButton.setMinSize(27.0, 55.0);
        menuButton.setPrefSize(27.0, 55.0);
        menuButton.setMaxSize(27.0, 55.0);
        menuButton.setOnAction(event -> showInstanceMenu());

        launchPane.getChildren().setAll(launchButton, menuButton);
    }

    /// Shows the instance-switch popup anchored to the menu button.
    private void showInstanceMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("hmcl-instance-menu");
        @Nullable HMCLDemoInstance selected = state.getSelectedInstance();
        for (HMCLDemoInstance instance : state.getInstances()) {
            M3ListItem item = new M3ListItem(instance.name());
            item.getStyleClass().add("hmcl-instance-menu-item");
            item.setSupportingText(instance.gameVersion() + " · " + instance.loader());
            item.setLeading(HMCLDemoUi.instanceIcon(instance, 24.0));
            item.setSelected(instance.equals(selected));
            item.setOnAction(event -> {
                state.selectInstance(instance.id());
                menu.hide();
            });
            CustomMenuItem menuItem = new CustomMenuItem(item);
            menuItem.setHideOnClick(false);
            menu.getItems().add(menuItem);
        }
        menu.show(menuButton, Side.TOP, 0.0, -menuButton.getHeight());
    }

    /// Synchronizes the account sidebar row.
    private void refreshAccount() {
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();
        if (account == null) {
            accountItem.setHeadlineText(strings.get("home.no_account"));
            accountItem.setSupportingText(strings.get("accounts.add"));
            accountItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS));
        } else {
            accountItem.setHeadlineText(account.displayName());
            accountItem.setSupportingText(HMCLDemoUi.accountTypeLabel(strings, account.type()));
            accountItem.setLeading(HMCLDemoUi.accountFace(account, 32.0));
        }
        refreshLaunchEnabled();
    }

    /// Synchronizes the current-instance sidebar row and launch subtitle.
    private void refreshInstance() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        currentInstanceItem.setHeadlineText(
                instance == null ? strings.get("home.manage_current_instance") : instance.name());
        if (instance == null) {
            currentInstanceItem.setSupportingText(strings.get("home.no_instance"));
            currentInstanceItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES));
            currentInstanceItem.setDisable(true);
            launchLabel.setText(strings.get("home.launch_empty"));
            launchInstance.setText("");
            launchGraphic.getChildren().setAll(launchLabel);
        } else {
            currentInstanceItem.setSupportingText(instance.gameVersion());
            currentInstanceItem.setLeading(HMCLDemoUi.instanceIcon(instance, 32.0));
            currentInstanceItem.setDisable(false);
            launchLabel.setText(strings.get("home.launch"));
            launchInstance.setText(instance.name());
            launchGraphic.getChildren().setAll(launchLabel, launchInstance);
        }
        refreshLaunchEnabled();
    }

    /// Enables launch only when both an account and an instance are selected.
    private void refreshLaunchEnabled() {
        boolean ready = state.getSelectedAccount() != null && state.getSelectedInstance() != null;
        launchButton.setDisable(!ready);
        menuButton.setDisable(state.getInstances().isEmpty());
    }

    /// Applies HMCL advanced-list-item styling hooks.
    ///
    /// @param item the navigation row
    private static void styleNav(M3ListItem item) {
        item.getStyleClass().add("hmcl-advanced-list-item");
    }

    /// Creates a compact sidebar section label.
    ///
    /// @return the label
    private static M3Text sectionLabel() {
        M3Text label = new M3Text("", M3TextRole.LABEL_SMALL);
        label.getStyleClass().add("hmcl-class-title");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
