// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

/// Simplified multiplayer secondary route.
@NotNullByDefault
final class HMCLMultiplayerView extends BorderPane {
    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final M3Text statusTitle = new M3Text("", M3TextRole.TITLE_MEDIUM);
    private final M3Text statusBody = new M3Text("", M3TextRole.BODY_MEDIUM);
    private final M3Text roomLabel = new M3Text("", M3TextRole.LABEL_LARGE);
    private final M3TextField joinField = new M3TextField();
    private final M3TextInputLayout joinLayout = new M3TextInputLayout(joinField);
    private final M3Button createButton = new M3Button();
    private final M3Button joinButton = new M3Button();
    private final M3Button launchButton = new M3Button();
    private final M3Button copyButton = new M3Button();
    private final M3Button leaveButton = new M3Button();
    private final VBox players = new VBox(4.0);

    /// Creates the multiplayer page.
    ///
    /// @param controller the application controller
    HMCLMultiplayerView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().addAll("hmcl-multiplayer-page", "hmcl-secondary-page");
        HMCLDemoUi.fill(this);

        statusBody.setWrapText(true);
        statusBody.setMaxWidth(Double.MAX_VALUE);
        createButton.setVariant(M3ButtonVariant.FILLED);
        joinButton.setVariant(M3ButtonVariant.TONAL);
        launchButton.setVariant(M3ButtonVariant.TEXT);
        copyButton.setVariant(M3ButtonVariant.TEXT);
        leaveButton.setVariant(M3ButtonVariant.TEXT);

        createButton.setOnAction(event -> {
            state.startHost();
            controller.showMessageKey("snackbar.multiplayer_create");
            refresh();
        });
        joinButton.setOnAction(event -> {
            state.startJoin(joinField.getText().isBlank() ? "DEMO-ROOM" : joinField.getText().strip());
            controller.showMessageKey("snackbar.multiplayer_join");
            refresh();
        });
        launchButton.setOnAction(event -> controller.launchSelected());
        copyButton.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        leaveButton.setOnAction(event -> {
            state.resetMultiplayer();
            refresh();
        });

        M3Card card = new M3Card();
        card.setVariant(M3CardVariant.ELEVATED);
        VBox cardBody = new VBox(
                12.0,
                statusTitle,
                statusBody,
                roomLabel,
                joinLayout,
                new HBox(8.0, createButton, joinButton, launchButton, copyButton, leaveButton),
                new M3Text("", M3TextRole.TITLE_SMALL),
                players
        );
        cardBody.setPadding(new Insets(20.0));
        card.setContent(cardBody);

        VBox page = HMCLDemoUi.pageColumn(card);
        setCenter(HMCLDemoUi.scroll(page));

        state.multiplayerPhaseProperty().addListener((observable, oldValue, newValue) -> refresh());
        state.multiplayerRoomCodeProperty().addListener((observable, oldValue, newValue) -> refresh());
        refreshLocale();
        refresh();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        joinLayout.setLabelText(strings.get("multiplayer.room_code"));
        createButton.setText(strings.get("multiplayer.create"));
        joinButton.setText(strings.get("multiplayer.join"));
        launchButton.setText(strings.get("multiplayer.launch"));
        copyButton.setText(strings.get("multiplayer.copy_code"));
        leaveButton.setText(strings.get("multiplayer.back"));
        refresh();
    }

    private void refresh() {
        HMCLDemoState.MultiplayerPhase phase = state.getMultiplayerPhase();
        switch (phase) {
            case WAITING -> {
                statusTitle.setText(strings.get("multiplayer.status.title"));
                statusBody.setText(strings.get("multiplayer.status.body"));
                roomLabel.setText("");
                joinLayout.setVisible(true);
                joinLayout.setManaged(true);
                createButton.setDisable(false);
                joinButton.setDisable(false);
                copyButton.setDisable(true);
                leaveButton.setDisable(true);
            }
            case HOSTING -> {
                statusTitle.setText(strings.get("multiplayer.hosting.title"));
                statusBody.setText(strings.format("multiplayer.hosting.body", state.getMultiplayerRoomCode()));
                roomLabel.setText(state.getMultiplayerRoomCode());
                joinLayout.setVisible(false);
                joinLayout.setManaged(false);
                createButton.setDisable(true);
                joinButton.setDisable(true);
                copyButton.setDisable(false);
                leaveButton.setDisable(false);
            }
            case JOINING -> {
                statusTitle.setText(strings.get("multiplayer.joining.title"));
                statusBody.setText(strings.format("multiplayer.joining.body", state.getMultiplayerRoomCode()));
                roomLabel.setText(state.getMultiplayerRoomCode());
                joinLayout.setVisible(false);
                joinLayout.setManaged(false);
                createButton.setDisable(true);
                joinButton.setDisable(true);
                copyButton.setDisable(false);
                leaveButton.setDisable(false);
            }
        }

        players.getChildren().clear();
        if (phase != HMCLDemoState.MultiplayerPhase.WAITING) {
            M3Text heading = new M3Text(strings.get("multiplayer.players"), M3TextRole.TITLE_SMALL);
            players.getChildren().add(heading);
            players.getChildren().add(playerRow("Glavo", strings.get("multiplayer.player.host")));
            players.getChildren().add(playerRow("Alex", strings.get("multiplayer.player.member")));
        }
    }

    private static M3ListItem playerRow(String name, String role) {
        M3ListItem item = new M3ListItem(name);
        item.setSupportingText(role);
        item.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS));
        item.setMaxWidth(Double.MAX_VALUE);
        item.setMouseTransparent(true);
        return item;
    }
}
