// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/// Displays the saved-account list with contextual account-provider actions.
///
/// The page follows the two-pane structure used by HMCL's account management: the fixed sidebar starts each
/// provider flow, while the main pane keeps account selection and maintenance in compact list rows. Microsoft
/// account creation is presented in an in-scene Material dialog when the page is hosted by [M3OverlayPane].
@NotNullByDefault
public final class HMCLAccountsView extends HMCLDemoView {
    /// The preferred width of the provider sidebar in logical pixels.
    private static final double SIDEBAR_WIDTH = 200.0;

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

    /// Creates the two-pane account-management content without a dashboard heading.
    ///
    /// @return the account page tree
    @Override
    protected Node createContent() {
        M3ListPane accounts = createList();
        for (HMCLDemoAccount account : state.getAccounts()) {
            accounts.getItems().add(createAccountRow(account));
        }
        if (accounts.getItems().isEmpty()) {
            accounts.getItems().add(new M3ListItem(text("accounts.empty")));
        }

        M3Text label = new M3Text(text("accounts.saved"), M3TextRole.LABEL_LARGE);
        VBox center = new VBox(8.0, label, accounts);
        center.getStyleClass().add("hmcl-list-surface");
        center.setPadding(new Insets(10.0));
        center.setMinWidth(0.0);
        center.setMaxWidth(Double.MAX_VALUE);
        return contextualPage(createSidebar(), center);
    }

    /// Creates the fixed provider sidebar used to start account workflows.
    ///
    /// @return the provider sidebar
    private Node createSidebar() {
        M3Text title = new M3Text(text("accounts.add.title"), M3TextRole.TITLE_MEDIUM);

        M3ListPane providers = createList();
        providers.getItems().addAll(
                providerItem("accounts.add.microsoft", HMCLDemoIcons.ACCOUNTS, this::showMicrosoftLoginDialog),
                providerItem(
                        "accounts.add.offline",
                        HMCLDemoIcons.ADD,
                        () -> actions.dispatch(HMCLDemoActions.ACTION_ADD_ACCOUNT, "offline")
                ),
                providerItem(
                        "accounts.type.external",
                        HMCLDemoIcons.SETTINGS,
                        () -> actions.dispatch("add-auth-server")
                )
        );

        M3Button addAuthenticationServer = new M3Button(
                text("accounts.add.authentication_server"),
                M3ButtonVariant.TEXT
        );
        addAuthenticationServer.setGraphic(HMCLDemoIcons.create(HMCLDemoIcons.ADD));
        addAuthenticationServer.setMaxWidth(Double.MAX_VALUE);
        addAuthenticationServer.setOnAction(event -> actions.dispatch("add-auth-server"));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox sidebar = new VBox(8.0, title, providers, spacer, addAuthenticationServer);
        sidebar.setPadding(new Insets(12.0, 8.0, 10.0, 8.0));
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        return sidebar;
    }

    /// Creates one account-provider entry for the contextual sidebar.
    ///
    /// @param textKey the localized provider name
    /// @param iconPath the Material icon path
    /// @param action the provider workflow entry action
    /// @return the configured provider row
    private M3ListItem providerItem(String textKey, String iconPath, Runnable action) {
        M3ListItem item = new M3ListItem(text(textKey));
        item.getStyleClass().add("hmcl-sidebar-item");
        item.setLeading(HMCLDemoIcons.create(iconPath));
        item.setOnAction(event -> action.run());
        return item;
    }

    /// Creates one compact saved-account row.
    ///
    /// @param account the represented account
    /// @return the configured row
    private M3ListItem createAccountRow(HMCLDemoAccount account) {
        boolean selected = account.equals(state.getSelectedAccount());
        M3ListItem row = new M3ListItem(account.displayName());
        row.getStyleClass().add("hmcl-account-row");
        row.setLeading(accountFace(account));
        row.setSupportingText(accountSubtitle(account));
        row.setSelected(selected);

        M3IconButton refresh = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.REFRESH));
        refresh.setAccessibleText(text("accounts.refresh"));
        refresh.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH, account.id()));

        M3IconButton remove = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.CLOSE));
        remove.setAccessibleText(text("accounts.remove"));
        remove.setOnAction(event -> remove(account));

        HBox actions = new HBox(2.0, refresh, remove);
        actions.setAlignment(Pos.CENTER_RIGHT);
        row.setTrailing(actions);
        row.setOnAction(event -> select(account));
        return row;
    }

    /// Returns the localized provider and model-specific summary for one saved account.
    ///
    /// @param account the represented account
    /// @return the compact supporting text
    private String accountSubtitle(HMCLDemoAccount account) {
        return text("accounts.type." + account.type().name().toLowerCase(Locale.ROOT));
    }

    /// Returns a deterministic Minecraft skin face for one account row.
    ///
    /// @param account the represented account
    /// @return the square face image
    private static ImageView accountFace(HMCLDemoAccount account) {
        String path = switch (account.type()) {
            case MICROSOFT -> "img/skin/wide/steve.png";
            case OFFLINE -> "img/skin/slim/alex.png";
            case EXTERNAL -> "img/skin/wide/noor.png";
        };
        return HMCLDemoAssets.skinFace(path, 32.0);
    }

    /// Selects an account and reports the selection to the application shell.
    ///
    /// @param account the account to select
    private void select(HMCLDemoAccount account) {
        state.selectAccount(account.id());
        actions.dispatch(HMCLDemoActions.ACTION_SELECT_ACCOUNT, account.id());
    }

    /// Requests removal of one account through the application shell.
    ///
    /// @param account the account to remove
    private void remove(HMCLDemoAccount account) {
        actions.dispatch("remove-account", account.id());
    }

    /// Shows the Microsoft account entry dialog in the hosting overlay.
    ///
    /// The current deterministic demo has no live Microsoft authorization exchange, so confirmation only creates
    /// its dummy Microsoft account through the existing application command.
    private void showMicrosoftLoginDialog() {
        @Nullable M3OverlayPane overlay = overlay();
        if (overlay == null) {
            actions.dispatch(HMCLDemoActions.ACTION_ADD_ACCOUNT, "microsoft");
            return;
        }

        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(text("accounts.microsoft_login.title"));

        M3Text warning = new M3Text(text("accounts.microsoft_login.warning"), M3TextRole.BODY_SMALL);
        warning.getStyleClass().addAll("hmcl-dialog-banner", "hmcl-dialog-warning");
        warning.setWrapText(true);
        warning.setMaxWidth(Double.MAX_VALUE);
        M3Text guidance = new M3Text(text("accounts.microsoft_login.body"), M3TextRole.BODY_MEDIUM);
        guidance.getStyleClass().add("hmcl-dialog-banner");
        guidance.setWrapText(true);
        guidance.setMaxWidth(Double.MAX_VALUE);
        M3Text helper = new M3Text(text("accounts.microsoft_login.helper"), M3TextRole.BODY_SMALL);
        helper.setWrapText(true);
        VBox dialogContent = new VBox(10.0, warning, guidance, helper);
        dialogContent.setPrefWidth(500.0);
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().setPrefWidth(548.0);

        M3Button cancel = new M3Button(text("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button continueButton = new M3Button(text("accounts.microsoft_login.continue"), M3ButtonVariant.TEXT);
        continueButton.setDefaultButton(true);
        continueButton.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_ACCOUNT, "microsoft"));
        dialog.getDialogPane().getActions().setAll(cancel, continueButton);
        overlay.showDialog(dialog);
    }

    /// Returns the root overlay that can host an in-scene Material dialog.
    ///
    /// @return the current overlay root, or `null` before attachment or under a different host
    private @Nullable M3OverlayPane overlay() {
        @Nullable Scene scene = getScene();
        if (scene == null) {
            return null;
        }
        Parent root = scene.getRoot();
        return root instanceof M3OverlayPane overlay ? overlay : null;
    }

    /// Creates a non-selecting segmented list for compact contextual rows.
    ///
    /// @return the configured list pane
    private static M3ListPane createList() {
        M3ListPane list = new M3ListPane();
        list.getStyleClass().add("hmcl-dense-list");
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }
}
