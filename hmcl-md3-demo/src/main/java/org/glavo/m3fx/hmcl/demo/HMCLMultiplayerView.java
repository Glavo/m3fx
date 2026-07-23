// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
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

    /// Hosts the phase-dependent status card.
    private final StackPane cardHost = new StackPane();

    /// The room-code editor used while waiting to join.
    private final M3TextField roomCodeField = new M3TextField();

    /// The room-code input layout.
    private final M3TextInputLayout roomCodeLayout = new M3TextInputLayout(roomCodeField);

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
        HMCLDemoUi.fill(cardHost);
        statusItem.setSelected(true);

        accountItem.getStyleClass().add("hmcl-sidebar-item");
        accountItem.setOnAction(event -> controller.openAccounts());
        launchItem.getStyleClass().add("hmcl-sidebar-item");
        launchItem.setOnAction(event -> controller.launchSelected());

        VBox sidebar = HMCLDemoUi.sidebar(
                statusSection,
                statusItem,
                HMCLDemoUi.vgrow(),
                accountItem,
                launchItem
        );
        setLeft(sidebar);

        VBox center = HMCLDemoUi.contentColumn(cardHost);
        setCenter(HMCLDemoUi.scroll(center));

        state.selectedAccountProperty().addListener((observable, oldValue, newValue) -> refreshDynamic());
        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> refreshDynamic());
        state.multiplayerPhaseProperty().addListener((observable, oldValue, newValue) -> renderCard());
        state.multiplayerRoomCodeProperty().addListener((observable, oldValue, newValue) -> {
            if (state.getMultiplayerPhase() != HMCLDemoState.MultiplayerPhase.WAITING) {
                renderCard();
            }
        });

        refreshLocale();
    }

    /// Updates static and dynamic labels.
    void refreshLocale() {
        statusSection.setText(strings.get("multiplayer.section.status"));
        statusItem.setHeadlineText(strings.get("multiplayer.nav.status"));
        roomCodeLayout.setLabelText(strings.get("multiplayer.room_code"));
        refreshDynamic();
        renderCard();
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

    /// Rebuilds the center card for the active multiplayer phase.
    private void renderCard() {
        Node content = switch (state.getMultiplayerPhase()) {
            case WAITING -> waitingContent();
            case HOSTING -> hostingContent();
            case JOINING -> joiningContent();
        };
        M3Card card = new M3Card(content, M3CardVariant.ELEVATED);
        card.setMaxWidth(560.0);
        card.setMinWidth(0.0);
        cardHost.getChildren().setAll(card);
    }

    /// Builds the idle create/join card.
    ///
    /// @return the card content
    private Node waitingContent() {
        M3Text title = new M3Text(strings.get("multiplayer.status.title"), M3TextRole.TITLE_LARGE);
        M3Text body = new M3Text(strings.get("multiplayer.status.body"), M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);

        M3Button createRoom = new M3Button(strings.get("multiplayer.create"), M3ButtonVariant.FILLED);
        createRoom.setOnAction(event -> state.startHost());

        M3Button joinRoom = new M3Button(strings.get("multiplayer.join"), M3ButtonVariant.TONAL);
        joinRoom.setOnAction(event -> {
            String code = roomCodeField.getText() == null ? "" : roomCodeField.getText().strip();
            if (code.isEmpty()) {
                code = "HMCL-JOIN";
            }
            state.startJoin(code);
        });

        HBox actions = new HBox(12.0, createRoom, joinRoom);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setMinWidth(0.0);

        VBox box = new VBox(16.0, title, body, roomCodeLayout, actions);
        box.setPadding(new Insets(8.0));
        box.setFillWidth(true);
        box.setMinWidth(0.0);
        return box;
    }

    /// Builds the hosting card with room code and fake players.
    ///
    /// @return the card content
    private Node hostingContent() {
        String code = state.getMultiplayerRoomCode();
        M3Text title = new M3Text(strings.get("multiplayer.hosting.title"), M3TextRole.TITLE_LARGE);
        M3Text body = new M3Text(strings.format("multiplayer.hosting.body", code), M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);

        M3Text codeLabel = new M3Text(code, M3TextRole.HEADLINE_SMALL);
        codeLabel.getStyleClass().add("hmcl-multiplayer-code");

        M3Button copy = new M3Button(strings.get("multiplayer.copy_code"), M3ButtonVariant.TONAL);
        copy.setOnAction(event -> {
        });

        M3Button back = new M3Button(strings.get("multiplayer.back"), M3ButtonVariant.TEXT);
        back.setOnAction(event -> state.resetMultiplayer());

        HBox actions = new HBox(12.0, copy, back);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setMinWidth(0.0);

        M3Text playersTitle = new M3Text(strings.get("multiplayer.players"), M3TextRole.TITLE_SMALL);
        M3ListPane players = new M3ListPane();
        players.setListStyle(M3ListStyle.SEGMENTED);
        players.setSelectionMode(M3SelectionMode.NONE);
        players.setMinHeight(0.0);
        players.getItems().setAll(fakePlayers());

        VBox box = new VBox(16.0, title, body, codeLabel, actions, playersTitle, players);
        box.setPadding(new Insets(8.0));
        box.setFillWidth(true);
        box.setMinWidth(0.0);
        return box;
    }

    /// Builds the joining/connecting card.
    ///
    /// @return the card content
    private Node joiningContent() {
        String code = state.getMultiplayerRoomCode();
        M3Text title = new M3Text(strings.get("multiplayer.joining.title"), M3TextRole.TITLE_LARGE);
        M3Text body = new M3Text(strings.format("multiplayer.joining.body", code), M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);

        M3Button back = new M3Button(strings.get("multiplayer.back"), M3ButtonVariant.TEXT);
        back.setOnAction(event -> state.resetMultiplayer());

        HBox actions = new HBox(back);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(16.0, title, body, actions);
        box.setPadding(new Insets(8.0));
        box.setFillWidth(true);
        box.setMinWidth(0.0);
        return box;
    }

    /// Builds a small deterministic fake player list for the hosting card.
    ///
    /// @return the player rows
    private M3ListItem[] fakePlayers() {
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();
        M3ListItem host = new M3ListItem(account == null ? strings.get("home.no_account") : account.displayName());
        host.setSupportingText(strings.get("multiplayer.player.host"));
        host.setLeading(account == null
                ? HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS)
                : HMCLDemoUi.accountFace(account, 28.0));

        M3ListItem guestA = new M3ListItem("Guest Alpha");
        guestA.setSupportingText(strings.get("multiplayer.player.member"));
        guestA.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS));

        M3ListItem guestB = new M3ListItem("Guest Beta");
        guestB.setSupportingText(strings.get("multiplayer.player.member"));
        guestB.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS));

        return new M3ListItem[] {host, guestA, guestB};
    }
}
