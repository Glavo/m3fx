// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays a focused multiplayer page inspired by HMCL Terracotta integration.
@NotNullByDefault
final class HMCLMultiplayerView extends BorderPane {
    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// The status section label.
    private final M3Text statusSection = HMCLDemoUi.sectionLabel("");

    /// The status navigation row.
    private final M3ListItem statusItem = HMCLDemoUi.navItem("", HMCLDemoIcons.GROUP, null);

    /// The account summary row.
    private final M3ListItem accountItem = new M3ListItem();

    /// The launch-current-instance row.
    private final M3ListItem launchItem = new M3ListItem();

    /// The status card title.
    private final M3Text statusTitle = new M3Text("", M3TextRole.TITLE_LARGE);

    /// The status card body.
    private final M3Text statusBody = new M3Text("", M3TextRole.BODY_MEDIUM);

    /// The room-code field.
    private final M3TextInputLayout roomCode = new M3TextInputLayout(new M3TextField());

    /// The create-room action.
    private final M3Button createRoom = new M3Button("", M3ButtonVariant.FILLED);

    /// The join-room action.
    private final M3Button joinRoom = new M3Button("", M3ButtonVariant.TONAL);

    /// Creates the multiplayer page.
    ///
    /// @param strings the localization source
    /// @param state the shared state
    /// @param controller the application controller
    HMCLMultiplayerView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-secondary-page");
        HMCLDemoUi.fill(this);
        statusItem.setSelected(true);
        accountItem.getStyleClass().add("hmcl-sidebar-item");
        accountItem.setOnAction(event -> controller.openAccounts());
        launchItem.getStyleClass().add("hmcl-sidebar-item");
        launchItem.setOnAction(event -> controller.launchSelected());
        createRoom.setOnAction(event -> controller.showMessageKey("snackbar.multiplayer_create"));
        joinRoom.setOnAction(event -> controller.showMessageKey("snackbar.multiplayer_join"));

        VBox sidebar = HMCLDemoUi.sidebar(
                statusSection,
                statusItem,
                HMCLDemoUi.vgrow(),
                accountItem,
                launchItem
        );
        setLeft(sidebar);

        statusBody.setWrapText(true);
        HBox actions = new HBox(12.0, createRoom, joinRoom);
        actions.setAlignment(Pos.CENTER_LEFT);
        VBox cardContent = new VBox(16.0, statusTitle, statusBody, roomCode, actions);
        cardContent.setPadding(new Insets(8.0));
        M3Card card = new M3Card(cardContent, M3CardVariant.ELEVATED);
        card.setMaxWidth(560.0);
        VBox center = HMCLDemoUi.contentColumn(card);
        setCenter(HMCLDemoUi.scroll(center));

        state.selectedAccountProperty().addListener((observable, oldValue, newValue) -> refreshDynamic());
        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> refreshDynamic());
        refreshLocale();
    }

    /// Updates static and dynamic labels.
    void refreshLocale() {
        statusSection.setText(strings.get("multiplayer.section.status"));
        statusItem.setHeadlineText(strings.get("multiplayer.nav.status"));
        statusTitle.setText(strings.get("multiplayer.status.title"));
        statusBody.setText(strings.get("multiplayer.status.body"));
        roomCode.setLabelText(strings.get("multiplayer.room_code"));
        createRoom.setText(strings.get("multiplayer.create"));
        joinRoom.setText(strings.get("multiplayer.join"));
        refreshDynamic();
    }

    /// Synchronizes the account and launch summary rows.
    private void refreshDynamic() {
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();
        if (account == null) {
            accountItem.setHeadlineText(strings.get("home.no_account"));
            accountItem.setSupportingText(strings.get("accounts.add"));
            accountItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS));
        } else {
            accountItem.setHeadlineText(account.displayName());
            accountItem.setSupportingText(HMCLDemoUi.accountTypeLabel(strings, account.type()));
            accountItem.setLeading(HMCLDemoUi.accountFace(account, 28.0));
        }

        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        launchItem.setHeadlineText(strings.get("multiplayer.launch"));
        if (instance == null) {
            launchItem.setSupportingText(strings.get("home.no_instance"));
            launchItem.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.PLAY));
            launchItem.setDisable(true);
        } else {
            launchItem.setSupportingText(instance.name());
            launchItem.setLeading(HMCLDemoUi.instanceIcon(instance, 28.0));
            launchItem.setDisable(false);
        }
    }
}
