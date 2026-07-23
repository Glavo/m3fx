// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Displays account selection and dummy account-management operations.
@NotNullByDefault
public final class HMCLAccountsView extends HMCLDemoView {
    /// Creates the account page.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the application command sink
    public HMCLAccountsView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        state.getAccounts().addListener((javafx.collections.ListChangeListener<HMCLDemoAccount>) change ->
                refreshView());
        state.selectedAccountProperty().addListener((observable, oldAccount, newAccount) -> refreshView());
        initializeView();
    }

    /// Creates the localized account content.
    ///
    /// @return the account page tree
    @Override
    protected Node createContent() {
        M3Button microsoft = new M3Button(text("accounts.add.microsoft"), M3ButtonVariant.FILLED);
        microsoft.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_ACCOUNT, "microsoft"));
        M3Button offline = new M3Button(text("accounts.add.offline"), M3ButtonVariant.TONAL);
        offline.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_ACCOUNT, "offline"));
        M3Card addCard = card(
                M3CardVariant.FILLED,
                new M3Text(text("accounts.add.title"), M3TextRole.TITLE_LARGE),
                wrapped(text("accounts.add.subtitle"), M3TextRole.BODY_MEDIUM),
                new HBox(10.0, microsoft, offline)
        );

        FlowPane cards = flow();
        for (HMCLDemoAccount account : state.getAccounts()) {
            cards.getChildren().add(createAccountCard(account));
        }
        if (state.getAccounts().isEmpty()) {
            cards.getChildren().add(wrapped(text("accounts.empty"), M3TextRole.BODY_LARGE));
        }

        return page(
                heading(text("accounts.title"), text("accounts.subtitle")),
                addCard,
                sectionTitle(text("accounts.saved")),
                cards
        );
    }

    /// Creates one account summary card.
    ///
    /// @param account the represented account
    /// @return the account card
    private M3Card createAccountCard(HMCLDemoAccount account) {
        boolean selected = account.equals(state.getSelectedAccount());
        M3Avatar avatar = new M3Avatar(account.avatarGlyph());
        M3Text name = new M3Text(account.displayName(), M3TextRole.TITLE_LARGE);
        M3AssistChip provider = new M3AssistChip(text(
                "accounts.type." + account.type().name().toLowerCase(Locale.ROOT)
        ));
        provider.setDisable(true);

        M3Button select = new M3Button(
                selected ? text("accounts.selected") : text("accounts.select"),
                selected ? M3ButtonVariant.TONAL : M3ButtonVariant.OUTLINED
        );
        select.setDisable(selected);
        select.setOnAction(event -> {
            state.selectAccount(account.id());
            actions.dispatch(HMCLDemoActions.ACTION_SELECT_ACCOUNT, account.id());
        });

        M3Button refresh = new M3Button(text("accounts.refresh"), M3ButtonVariant.TEXT);
        refresh.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH, account.id()));

        M3Button remove = new M3Button(text("accounts.remove"), M3ButtonVariant.TEXT);
        remove.setOnAction(event -> {
            state.selectAccount(account.id());
            state.removeSelectedAccount();
            actions.dispatch("remove-account", account.id());
        });

        HBox identity = new HBox(12.0, avatar, name);
        identity.setAlignment(Pos.CENTER_LEFT);
        HBox actionRow = new HBox(6.0, select, refresh, remove);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        M3Card card = card(
                selected ? M3CardVariant.ELEVATED : M3CardVariant.OUTLINED,
                identity,
                provider,
                wrapped(HMCLDemoModelText.accountSubtitle(strings, account), M3TextRole.BODY_MEDIUM),
                actionRow
        );
        card.setPrefWidth(330.0);
        return card;
    }

    /// Creates a wrapping Material text node.
    ///
    /// @param value the displayed text
    /// @param role  the typography role
    /// @return the configured text node
    private static M3Text wrapped(String value, M3TextRole role) {
        M3Text text = new M3Text(value, role);
        text.setWrapText(true);
        return text;
    }
}
