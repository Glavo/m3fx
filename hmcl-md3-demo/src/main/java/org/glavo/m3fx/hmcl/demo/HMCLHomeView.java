// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Home destination with account/instance summaries and launch controls.
@NotNullByDefault
final class HMCLHomeView extends BorderPane {
    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final M3ListItem accountItem = new M3ListItem();
    private final M3ListItem instanceItem = new M3ListItem();
    private final M3ListItem allInstancesItem = new M3ListItem();
    private final M3ListItem downloadItem = new M3ListItem();
    private final M3ListItem multiplayerItem = new M3ListItem();
    private final M3ListItem feedbackItem = new M3ListItem();

    private final M3Text announcementTitle = new M3Text("", M3TextRole.TITLE_SMALL);
    private final M3Text announcementBody = new M3Text("", M3TextRole.BODY_MEDIUM);
    private final M3AnimatedVisibility announcementVisibility = new M3AnimatedVisibility();
    private final M3Card announcementCard = new M3Card();

    private final M3SplitButton launchButton = new M3SplitButton();
    private final M3Text launchLabel = new M3Text("", M3TextRole.TITLE_MEDIUM);
    private final M3Text launchInstance = new M3Text("", M3TextRole.BODY_SMALL);

    /// Creates the home page.
    ///
    /// @param controller the application controller
    HMCLHomeView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().add("hmcl-home-page");
        HMCLDemoUi.fill(this);

        VBox sidebar = HMCLDemoUi.sidebar(
                HMCLDemoUi.sectionLabel(""),
                accountItem,
                HMCLDemoUi.sectionLabel(""),
                instanceItem,
                allInstancesItem,
                downloadItem,
                multiplayerItem,
                feedbackItem
        );

        accountItem.setOnAction(event -> controller.openAccounts());
        instanceItem.setOnAction(event -> controller.openSelectedInstance());
        allInstancesItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES));
        allInstancesItem.setOnAction(event -> controller.openInstances());
        downloadItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.DOWNLOAD));
        downloadItem.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.GAME));
        multiplayerItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.GROUP));
        multiplayerItem.setOnAction(event -> controller.openMultiplayer());
        feedbackItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.CHAT));
        feedbackItem.setOnAction(event -> controller.showMessageKey("snackbar.feedback"));

        announcementCard.setVariant(M3CardVariant.ELEVATED);
        announcementCard.getStyleClass().add("hmcl-announcement-card");
        announcementBody.setWrapText(true);
        M3IconButton dismiss = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.CLOSE));
        dismiss.setOnAction(event -> announcementVisibility.setShowing(false));
        HBox announcementHeader = new HBox(8.0, announcementTitle, dismiss);
        HBox.setHgrow(announcementTitle, Priority.ALWAYS);
        announcementHeader.setAlignment(Pos.CENTER_LEFT);
        VBox announcementContent = new VBox(12.0, announcementHeader, announcementBody);
        announcementContent.setPadding(new Insets(16.0));
        announcementCard.setContent(announcementContent);
        announcementVisibility.setContent(announcementCard);
        announcementVisibility.setShowing(true);

        VBox launchGraphic = new VBox(2.0, launchLabel, launchInstance);
        launchGraphic.setAlignment(Pos.CENTER_LEFT);
        launchButton.setGraphic(launchGraphic);
        launchButton.setSize(M3ButtonSize.LARGE);
        launchButton.setVariant(M3ButtonVariant.FILLED);
        launchButton.setOnAction(event -> controller.launchSelected());
        launchButton.getStyleClass().add("hmcl-launch-button");

        StackPane center = HMCLDemoUi.fill(new StackPane());
        center.getStyleClass().add("hmcl-home-center");
        center.setPadding(new Insets(20.0));
        StackPane.setAlignment(announcementVisibility, Pos.TOP_CENTER);
        StackPane.setAlignment(launchButton, Pos.BOTTOM_RIGHT);
        center.getChildren().setAll(announcementVisibility, launchButton);

        setLeft(sidebar);
        setCenter(center);

        state.selectedAccountProperty().addListener((observable, oldValue, newValue) -> refreshAccount());
        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> {
            refreshInstance();
            rebuildLaunchMenu();
        });
        state.getAccounts().addListener((ListChangeListener<HMCLDemoAccount>) change -> refreshAccount());
        state.getInstances().addListener((ListChangeListener<HMCLDemoInstance>) change -> {
            refreshInstance();
            rebuildLaunchMenu();
        });

        refreshLocale();
        refreshAccount();
        refreshInstance();
        rebuildLaunchMenu();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        VBox sidebar = (VBox) getLeft();
        sidebar.getChildren().set(0, HMCLDemoUi.sectionLabel(strings.get("home.section.account")));
        sidebar.getChildren().set(2, HMCLDemoUi.sectionLabel(strings.get("home.section.version")));
        allInstancesItem.setHeadlineText(strings.get("home.all_instances"));
        downloadItem.setHeadlineText(strings.get("home.download"));
        multiplayerItem.setHeadlineText(strings.get("home.multiplayer"));
        feedbackItem.setHeadlineText(strings.get("home.feedback"));
        announcementTitle.setText(strings.get("home.preview.title"));
        announcementBody.setText(strings.get("home.preview.body"));
        launchLabel.setText(strings.get("home.launch"));
        refreshAccount();
        refreshInstance();
        rebuildLaunchMenu();
    }

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
    }

    private void refreshInstance() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance == null) {
            instanceItem.setHeadlineText(strings.get("home.no_instance"));
            instanceItem.setSupportingText(strings.get("home.manage_current_instance"));
            instanceItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES));
            launchInstance.setText(strings.get("home.launch_empty"));
        } else {
            instanceItem.setHeadlineText(instance.name());
            instanceItem.setSupportingText(instance.gameVersion() + " · " + instance.loader());
            instanceItem.setLeading(HMCLDemoAssets.imageView(instance.iconPath(), 32.0, 32.0));
            launchInstance.setText(instance.name());
        }
    }

    private void rebuildLaunchMenu() {
        launchButton.getItems().clear();
        for (HMCLDemoInstance instance : state.getInstances()) {
            M3MenuItem item = new M3MenuItem(instance.name());
            item.setOnAction(event -> {
                state.selectInstance(instance.id());
                controller.launchSelected();
            });
            launchButton.getItems().add(item);
        }
    }
}
