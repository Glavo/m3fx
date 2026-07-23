// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Account list secondary route.
@NotNullByDefault
final class HMCLAccountsView extends BorderPane {
    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final M3ListItem microsoftItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ACCOUNTS, null);
    private final M3ListItem offlineItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ADD, null);
    private final M3ListItem externalItem = HMCLDemoUi.navItem("", HMCLDemoIcons.GROUP, null);
    private final VBox accountCards = new VBox(8.0);

    /// Creates the accounts page.
    ///
    /// @param controller the application controller
    HMCLAccountsView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().add("hmcl-accounts-page");
        HMCLDemoUi.fill(this);

        VBox sidebar = HMCLDemoUi.sidebar(
                HMCLDemoUi.sectionLabel(""),
                microsoftItem,
                offlineItem,
                externalItem
        );
        microsoftItem.setOnAction(event -> showMicrosoftDialog());
        offlineItem.setOnAction(event -> showOfflineDialog());
        externalItem.setOnAction(event -> {
            state.addDummyAccount(HMCLDemoAccount.AccountType.EXTERNAL);
            controller.showMessageKey("snackbar.account_added");
        });

        accountCards.setPadding(new Insets(16.0, 20.0, 24.0, 20.0));
        accountCards.setFillWidth(true);
        setLeft(sidebar);
        setCenter(HMCLDemoUi.scroll(accountCards));

        state.getAccounts().addListener((ListChangeListener<HMCLDemoAccount>) change -> rebuildAccounts());
        state.selectedAccountProperty().addListener((observable, oldValue, newValue) -> rebuildAccounts());
        refreshLocale();
        rebuildAccounts();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        VBox sidebar = (VBox) getLeft();
        sidebar.getChildren().set(0, HMCLDemoUi.sectionLabel(strings.get("accounts.section.add")));
        microsoftItem.setHeadlineText(strings.get("accounts.add.microsoft"));
        offlineItem.setHeadlineText(strings.get("accounts.add.offline"));
        externalItem.setHeadlineText(strings.get("accounts.add.external"));
        rebuildAccounts();
    }

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
                if (state.removeSelectedAccount()) {
                    controller.showMessageKey("snackbar.account_removed");
                }
            });

            HBox trailing = new HBox(4.0, remove, selector);
            trailing.setAlignment(Pos.CENTER_RIGHT);

            M3ListItem row = new M3ListItem(account.displayName());
            row.getStyleClass().add("hmcl-account-row");
            row.setSupportingText(HMCLDemoUi.accountTypeLabel(strings, account.type()));
            row.setLeading(HMCLDemoUi.accountFace(account, 32.0));
            row.setTrailing(trailing);
            row.setSelected(isSelected);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setOnAction(event -> state.selectAccount(account.id()));
            accountCards.getChildren().add(row);
        }
    }

    private void showMicrosoftDialog() {
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("accounts.microsoft.title"));
        M3Banner warning = new M3Banner(strings.get("accounts.microsoft.warning"));
        warning.setIcon(HMCLDemoIcons.create(HMCLDemoIcons.INFO));
        dialog.getDialogPane().setContent(warning);
        dialog.getDialogPane().setContentText(strings.get("accounts.microsoft.body"));

        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button login = new M3Button(strings.get("accounts.microsoft.login"), M3ButtonVariant.TEXT);
        login.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, login);
        login.setOnAction(event -> {
            state.addDummyAccount(HMCLDemoAccount.AccountType.MICROSOFT);
            controller.showMessageKey("snackbar.account_added");
        });
        controller.overlay().showDialog(dialog);
    }

    private void showOfflineDialog() {
        M3TextField nameField = new M3TextField("Player");
        M3TextInputLayout layout = new M3TextInputLayout(nameField);
        layout.setLabelText(strings.get("accounts.add.offline"));
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("accounts.add.offline"));
        dialog.getDialogPane().setContent(layout);
        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button add = new M3Button(strings.get("accounts.add"), M3ButtonVariant.TEXT);
        add.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, add);
        add.setOnAction(event -> {
            state.addDummyAccount(HMCLDemoAccount.AccountType.OFFLINE);
            controller.showMessageKey("snackbar.account_added");
        });
        controller.overlay().showDialog(dialog);
    }
}
