// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays the launch-focused home destination of the HMCL Material 3 demo.
@NotNullByDefault
public final class HMCLHomeView extends HMCLDemoView {
    /// Creates the home page.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the application command sink
    public HMCLHomeView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        state.selectedInstanceProperty().addListener((observable, oldInstance, newInstance) -> refreshView());
        state.selectedAccountProperty().addListener((observable, oldAccount, newAccount) -> refreshView());
        initializeView();
    }

    /// Creates the localized home content.
    ///
    /// @return the home page tree
    @Override
    protected Node createContent() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();

        M3Card launchCard = createLaunchCard(instance, account);
        M3Card updateCard = createUpdateCard();
        M3Card activityCard = createActivityCard(instance);

        return page(
                heading(text("home.title"), text("home.subtitle")),
                launchCard,
                sectionTitle(text("home.overview")),
                flow(updateCard, activityCard)
        );
    }

    /// Creates the selected-instance launch card.
    ///
    /// @param instance the selected instance, or `null`
    /// @param account  the selected account, or `null`
    /// @return the launch card
    private M3Card createLaunchCard(
            @Nullable HMCLDemoInstance instance,
            @Nullable HMCLDemoAccount account
    ) {
        String instanceName = instance == null ? text("home.no_instance") : instance.name();
        String instanceDetails = instance == null
                ? text("home.no_instance.supporting")
                : text("home.instance.details", instance.gameVersion(), instance.loader());

        M3Text eyebrow = new M3Text(text("home.ready_to_play"), M3TextRole.LABEL_LARGE);
        M3Text name = new M3Text(instanceName, M3TextRole.HEADLINE_MEDIUM);
        name.setWrapText(true);
        M3Text details = new M3Text(instanceDetails, M3TextRole.BODY_LARGE);
        details.setWrapText(true);

        HBox accountRow = new HBox(10.0);
        accountRow.setAlignment(Pos.CENTER_LEFT);
        if (account != null) {
            accountRow.getChildren().addAll(
                    new M3Avatar(account.avatarGlyph()),
                    new M3Text(text("home.playing_as", account.displayName()), M3TextRole.BODY_MEDIUM)
            );
        } else {
            accountRow.getChildren().add(
                    new M3Text(text("home.no_account"), M3TextRole.BODY_MEDIUM)
            );
        }

        M3Button play = new M3Button(text("action.play"), M3ButtonVariant.FILLED);
        play.setGraphic(HMCLDemoIcons.create(HMCLDemoIcons.PLAY));
        play.setDisable(instance == null || account == null);
        play.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_PLAY));

        M3Button switchInstance = new M3Button(text("action.switch_instance"), M3ButtonVariant.TONAL);
        switchInstance.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_INSTANCES));

        HBox actionsRow = new HBox(12.0, play, switchInstance);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        return card(
                M3CardVariant.ELEVATED,
                eyebrow,
                name,
                details,
                accountRow,
                actionsRow
        );
    }

    /// Creates the launcher update summary.
    ///
    /// @return the update card
    private M3Card createUpdateCard() {
        M3AssistChip channel = new M3AssistChip(text("home.update.channel"));
        M3Text title = new M3Text(text("home.update.title"), M3TextRole.TITLE_MEDIUM);
        M3Text supporting = new M3Text(text("home.update.supporting"), M3TextRole.BODY_MEDIUM);
        supporting.setWrapText(true);
        M3Button view = commandButton(
                text("action.view_update"),
                M3ButtonVariant.TEXT,
                HMCLDemoActions.ACTION_REFRESH
        );
        M3Card card = card(M3CardVariant.FILLED, channel, title, supporting, view);
        card.setPrefWidth(360.0);
        return card;
    }

    /// Creates the selected-instance activity summary.
    ///
    /// @param instance the selected instance, or `null`
    /// @return the activity card
    private M3Card createActivityCard(@Nullable HMCLDemoInstance instance) {
        M3Text title = new M3Text(text("home.activity.title"), M3TextRole.TITLE_MEDIUM);
        M3Text value = new M3Text(
                instance == null
                        ? text("home.activity.empty")
                        : HMCLDemoModelText.instanceLastPlayed(strings, instance),
                M3TextRole.HEADLINE_SMALL
        );
        value.setWrapText(true);
        M3Text supporting = new M3Text(text("home.activity.supporting"), M3TextRole.BODY_MEDIUM);
        supporting.setWrapText(true);
        M3Button manage = new M3Button(text("action.manage_instance"), M3ButtonVariant.TEXT);
        manage.setDisable(instance == null);
        manage.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_INSTANCE_DETAIL));
        M3Card card = card(M3CardVariant.OUTLINED, title, value, supporting, manage);
        card.setPrefWidth(360.0);
        return card;
    }
}
