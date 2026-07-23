// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays the account list with provider shortcuts in the left pane.
///
/// Center content is a full-height card list over an opaque surface, matching HMCL `AccountListPage`.
@NotNullByDefault
final class HMCLAccountsView extends BorderPane {
    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// The left-pane section label.
    private final M3Text addSection = HMCLDemoUi.sectionLabel("");

    /// The Microsoft add-account row.
    private final M3ListItem microsoftItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ACCOUNTS, null);

    /// The offline add-account row.
    private final M3ListItem offlineItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ACCOUNTS, null);

    /// The external-auth add-account row.
    private final M3ListItem externalItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ACCOUNTS, null);

    /// The add-auth-server row.
    private final M3ListItem addServerItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ADD, null);

    /// Full-height account card column.
    private final VBox accountCards = new VBox(10.0);

    /// Creates the accounts page.
    ///
    /// @param strings the localization source
    /// @param state the shared state
    /// @param controller the application controller
    HMCLAccountsView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-secondary-page");
        HMCLDemoUi.fill(this);

        microsoftItem.setOnAction(event -> showMicrosoftDialog());
        offlineItem.setOnAction(event -> state.addDummyAccount(HMCLDemoAccount.AccountType.OFFLINE));
        externalItem.setOnAction(event -> state.addDummyAccount(HMCLDemoAccount.AccountType.EXTERNAL));
        addServerItem.setOnAction(event -> {
        });

        VBox sidebar = HMCLDemoUi.sidebar(addSection, microsoftItem, offlineItem, externalItem, addServerItem);
        setLeft(sidebar);

        accountCards.getStyleClass().add("hmcl-card-list");
        accountCards.setFillWidth(true);
        accountCards.setMaxWidth(Double.MAX_VALUE);
        accountCards.setMinHeight(0.0);

        var scroll = HMCLDemoUi.scroll(accountCards);
        scroll.setFitToHeight(false);
        VBox center = HMCLDemoUi.fill(new VBox(scroll));
        center.getStyleClass().add("hmcl-page-center");
        center.setPadding(new Insets(10.0));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setCenter(center);

        state.selectedAccountProperty().addListener((observable, oldValue, newValue) -> rebuildAccounts());
        state.getAccounts().addListener((ListChangeListener<HMCLDemoAccount>) change -> rebuildAccounts());
        refreshLocale();
        rebuildAccounts();
    }

    /// Updates static labels.
    void refreshLocale() {
        addSection.setText(strings.get("accounts.section.add"));
        microsoftItem.setHeadlineText(strings.get("accounts.add.microsoft"));
        offlineItem.setHeadlineText(strings.get("accounts.add.offline"));
        externalItem.setHeadlineText(strings.get("accounts.add.external"));
        addServerItem.setHeadlineText(strings.get("accounts.add.server"));
        rebuildAccounts();
    }

    /// Rebuilds account cards from the current state.
    private void rebuildAccounts() {
        accountCards.getChildren().clear();
        @Nullable HMCLDemoAccount selected = state.getSelectedAccount();
        for (HMCLDemoAccount account : state.getAccounts()) {
            boolean isSelected = account.equals(selected);

            M3RadioButton selector = new M3RadioButton();
            selector.setSelected(isSelected);
            selector.setMouseTransparent(true);
            selector.setFocusTraversable(false);

            M3Button remove = new M3Button(strings.get("accounts.remove"), M3ButtonVariant.TEXT);
            remove.setOnAction(event -> {
                state.selectAccount(account.id());
                state.removeSelectedAccount();

            });

            HBox trailing = new HBox(4.0, remove, selector);
            trailing.setAlignment(Pos.CENTER_RIGHT);

            M3ListItem row = new M3ListItem(account.displayName());
            row.getStyleClass().addAll("hmcl-account-row", "hmcl-account-card");
            row.setSupportingText(HMCLDemoUi.accountTypeLabel(strings, account.type()));
            row.setLeading(HMCLDemoUi.accountFace(account, 32.0));
            row.setTrailing(trailing);
            row.setSelected(isSelected);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setOnAction(event -> state.selectAccount(account.id()));
            accountCards.getChildren().add(row);
        }
    }

    /// Shows the Microsoft login explanation dialog used by the offline demo.
    private void showMicrosoftDialog() {
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("accounts.microsoft.title"));

        M3Banner warning = new M3Banner(strings.get("accounts.microsoft.warning"));
        warning.setIcon(HMCLDemoIcons.create(HMCLDemoIcons.INFO));
        warning.getStyleClass().add("hmcl-dialog-warning-banner");

        M3Text body = new M3Text(strings.get("accounts.microsoft.body"), M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);

        VBox content = new VBox(12.0, warning, body);
        content.setPadding(new Insets(4.0, 0.0, 0.0, 0.0));
        content.setFillWidth(true);
        content.setMinWidth(0.0);
        dialog.getDialogPane().setContent(content);

        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button login = new M3Button(strings.get("accounts.microsoft.login"), M3ButtonVariant.TEXT);
        login.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, login);
        dialog.setOnHidden(event -> {
            if (event.getAction() == login) {
                state.addDummyAccount(HMCLDemoAccount.AccountType.MICROSOFT);
            }
        });
        controller.overlay().showDialog(dialog);
    }
}
