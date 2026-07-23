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
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays the account list with provider shortcuts in the left pane.
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

    /// The account list surface.
    private final M3ListPane accountList = new M3ListPane();

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
        accountList.setListStyle(M3ListStyle.SEGMENTED);
        accountList.setSelectionMode(M3SelectionMode.SINGLE);
        accountList.getStyleClass().add("hmcl-dense-list");

        microsoftItem.setOnAction(event -> showMicrosoftDialog());
        offlineItem.setOnAction(event -> {
            state.addDummyAccount(HMCLDemoAccount.AccountType.OFFLINE);
            controller.showMessageKey("snackbar.account_added");
            rebuildAccounts();
        });
        externalItem.setOnAction(event -> {
            state.addDummyAccount(HMCLDemoAccount.AccountType.EXTERNAL);
            controller.showMessageKey("snackbar.account_added");
            rebuildAccounts();
        });
        addServerItem.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));

        VBox sidebar = HMCLDemoUi.sidebar(addSection, microsoftItem, offlineItem, externalItem, addServerItem);
        setLeft(sidebar);

        VBox body = HMCLDemoUi.contentColumn(accountList);
        VBox.setVgrow(accountList, Priority.ALWAYS);
        setCenter(HMCLDemoUi.scroll(body));

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

    /// Rebuilds account rows from the current state.
    private void rebuildAccounts() {
        accountList.getItems().clear();
        @Nullable HMCLDemoAccount selected = state.getSelectedAccount();
        for (HMCLDemoAccount account : state.getAccounts()) {
            M3RadioButton selector = new M3RadioButton();
            selector.setSelected(account.equals(selected));
            selector.setMouseTransparent(true);
            selector.setFocusTraversable(false);

            M3Button remove = new M3Button(strings.get("accounts.remove"), M3ButtonVariant.TEXT);
            remove.setOnAction(event -> {
                state.selectAccount(account.id());
                state.removeSelectedAccount();
                controller.showMessageKey("snackbar.account_removed");
            });

            M3ListItem row = new M3ListItem(account.displayName());
            row.getStyleClass().add("hmcl-account-row");
            row.setSupportingText(HMCLDemoUi.accountTypeLabel(strings, account.type()));
            row.setLeading(HMCLDemoUi.accountFace(account, 36.0));
            row.setTrailing(new HBox(8.0, remove, selector));
            row.setSelected(account.equals(selected));
            row.setOnAction(event -> state.selectAccount(account.id()));
            accountList.getItems().add(row);
        }
    }

    /// Shows the Microsoft login explanation dialog used by the offline demo.
    private void showMicrosoftDialog() {
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("accounts.microsoft.title"));

        M3Text warning = new M3Text(strings.get("accounts.microsoft.warning"), M3TextRole.BODY_MEDIUM);
        warning.getStyleClass().add("hmcl-dialog-warning");
        warning.setWrapText(true);
        M3Text body = new M3Text(strings.get("accounts.microsoft.body"), M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);
        VBox content = new VBox(12.0, warning, body);
        content.setPadding(new Insets(4.0, 0.0, 0.0, 0.0));
        dialog.getDialogPane().setContent(content);

        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button login = new M3Button(strings.get("accounts.microsoft.login"), M3ButtonVariant.TEXT);
        login.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, login);
        dialog.setOnHidden(event -> {
            if (event.getAction() == login) {
                state.addDummyAccount(HMCLDemoAccount.AccountType.MICROSOFT);
                controller.showMessageKey("snackbar.account_added");
            }
        });
        controller.overlay().showDialog(dialog);
    }
}
