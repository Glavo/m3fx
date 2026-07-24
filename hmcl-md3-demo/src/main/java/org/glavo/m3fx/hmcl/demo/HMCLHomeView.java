// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// HMCL home page modeled on `MainPage` content.
///
/// Primary destinations live on the shell's adaptive navigation rail/bar. This page owns the wallpaper-facing
/// launch surface plus compact account and current-instance shortcuts.
@NotNullByDefault
final class HMCLHomeView extends BorderPane {
    /// The localization source used by this page.
    private final HMCLDemoStrings strings;

    /// The shared deterministic state rendered by this page.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// Compact shortcuts for the selected account and instance.
    private final VBox shortcuts = new VBox(4.0);

    private final M3ListItem accountItem = new M3ListItem();
    private final M3ListItem currentInstanceItem = new M3ListItem();

    /// Preview-channel announcement card.
    private final VBox announcementCard = new VBox(16.0);

    /// Announcement title.
    private final M3Text announcementTitle = new M3Text("", M3TextRole.TITLE_SMALL);

    /// Announcement body.
    private final M3Text announcementBody = new M3Text("", M3TextRole.BODY_MEDIUM);

    /// Whether the preview notice remains visible for this session.
    private boolean announcementVisible = true;

    /// Material 3 split launch control (primary action + instance menu).
    private final M3SplitButton launchButton = new M3SplitButton();

    /// Launch label ("启动").
    private final M3Text launchLabel = new M3Text("", M3TextRole.TITLE_MEDIUM);

    /// Current instance name under the launch label.
    private final M3Text launchInstance = new M3Text("", M3TextRole.BODY_SMALL);

    /// Graphic stack for the launch button.
    private final VBox launchGraphic = new VBox(1.0);

    /// Creates the wallpaper-first home page.
    ///
    /// @param controller the application controller
    HMCLHomeView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().add("hmcl-home-page");
        HMCLDemoUi.fill(this);
        configureShortcuts();
        configureAnnouncement();
        configureLaunchPane();

        StackPane center = HMCLDemoUi.fill(new StackPane());
        center.getStyleClass().add("hmcl-home-center");
        center.setPadding(new Insets(20.0));
        StackPane.setAlignment(shortcuts, Pos.TOP_LEFT);
        StackPane.setAlignment(announcementCard, Pos.TOP_CENTER);
        StackPane.setAlignment(launchButton, Pos.BOTTOM_RIGHT);
        center.getChildren().setAll(shortcuts, announcementCard, launchButton);

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
    }

    /// Updates every static label owned by this page.
    void refreshLocale() {
        announcementTitle.setText(strings.get("home.preview.title"));
        announcementBody.setText(strings.get("home.preview.body") + "\n" + strings.get("home.preview.feedback"));
        launchLabel.setText(strings.get("home.launch"));
        launchButton.setAccessibleText(strings.get("home.launch"));
        refreshAccount();
        refreshInstance();
    }

    /// Builds the compact account/instance shortcuts once.
    private void configureShortcuts() {
        shortcuts.getStyleClass().add("hmcl-home-shortcuts");
        shortcuts.setMaxWidth(320.0);
        shortcuts.setFillWidth(true);
        shortcuts.setPadding(new Insets(0.0, 0.0, 12.0, 0.0));

        styleShortcut(accountItem);
        styleShortcut(currentInstanceItem);
        accountItem.setOnAction(event -> controller.openAccounts());
        currentInstanceItem.setOnAction(event -> controller.openSelectedInstance());
        shortcuts.getChildren().setAll(accountItem, currentInstanceItem);
    }

    /// Builds the preview announcement once.
    private void configureAnnouncement() {
        announcementCard.getStyleClass().addAll("hmcl-card", "hmcl-announcement");
        announcementCard.setMaxWidth(560.0);
        announcementCard.setMaxHeight(Region.USE_PREF_SIZE);
        announcementCard.setPadding(new Insets(16.0, 20.0, 16.0, 20.0));
        announcementCard.setSpacing(8.0);
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

    /// Builds the Material 3 split launch control once.
    private void configureLaunchPane() {
        launchLabel.getStyleClass().add("hmcl-launch-label");
        launchInstance.getStyleClass().add("hmcl-launch-instance");
        launchGraphic.setAlignment(Pos.CENTER_LEFT);
        launchGraphic.setMouseTransparent(true);
        launchGraphic.getChildren().setAll(launchLabel, launchInstance);

        launchButton.getStyleClass().add("hmcl-home-launch");
        launchButton.setVariant(M3ButtonVariant.FILLED);
        launchButton.setSize(M3ButtonSize.MEDIUM);
        launchButton.setGraphic(launchGraphic);
        launchButton.setOnAction(event -> controller.launchSelected());
        rebuildLaunchMenu();
    }

    /// Rebuilds the instance-switch menu attached to the split button.
    private void rebuildLaunchMenu() {
        launchButton.getItems().clear();
        @Nullable HMCLDemoInstance selected = state.getSelectedInstance();
        for (HMCLDemoInstance instance : state.getInstances()) {
            M3MenuItem item = new M3MenuItem(instance.name());
            item.setSupportingText(instance.gameVersion() + " · " + instance.loader());
            item.setLeading(HMCLDemoUi.instanceIcon(instance, 24.0));
            item.setSelected(instance.equals(selected));
            item.setOnAction(event -> {
                state.selectInstance(instance.id());
                launchButton.hideMenu();
            });
            launchButton.getItems().add(item);
        }
    }

    /// Synchronizes the account shortcut row.
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

    /// Synchronizes the current-instance shortcut and launch subtitle.
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
    }

    /// Applies compact list styling to a home shortcut row.
    ///
    /// @param item the shortcut row
    private static void styleShortcut(M3ListItem item) {
        item.getStyleClass().addAll("hmcl-advanced-list-item", "hmcl-home-shortcut");
        item.setMaxWidth(Double.MAX_VALUE);
    }
}
