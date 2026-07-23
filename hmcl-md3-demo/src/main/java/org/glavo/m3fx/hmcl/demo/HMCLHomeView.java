// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Displays HMCL's wallpaper-first launch page with its account, version, and launcher sidebar.
@NotNullByDefault
public final class HMCLHomeView extends StackPane {
    /// The localization source used by this page.
    private final HMCLDemoStrings strings;

    /// The shared deterministic state rendered by this page.
    private final HMCLDemoState state;

    /// The application-level command sink used by page actions.
    private final HMCLDemoActions actions;

    /// The full-bleed HMCL wallpaper behind page controls.
    private final ImageView wallpaperView = new ImageView();

    /// Whether the preview-channel notice remains visible for this demo session.
    private boolean updateNoticeVisible = true;

    /// Creates the wallpaper-first home page.
    ///
    /// @param strings the localization source
    /// @param state the shared demo state
    /// @param actions the application command sink
    public HMCLHomeView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        this.strings = Objects.requireNonNull(strings, "strings");
        this.state = Objects.requireNonNull(state, "state");
        this.actions = Objects.requireNonNull(actions, "actions");

        getStyleClass().add("hmcl-home-page");
        wallpaperView.getStyleClass().add("hmcl-home-wallpaper");
        wallpaperView.setPreserveRatio(false);
        wallpaperView.setSmooth(true);
        wallpaperView.fitWidthProperty().bind(widthProperty());
        wallpaperView.fitHeightProperty().bind(heightProperty());

        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> rebuildContent());
        state.selectedInstanceProperty().addListener((observable, oldInstance, newInstance) -> rebuildContent());
        state.selectedAccountProperty().addListener((observable, oldAccount, newAccount) -> rebuildContent());
        state.wallpaperProperty().addListener((observable, oldWallpaper, newWallpaper) -> updateWallpaper());
        state.getInstances().addListener((ListChangeListener<HMCLDemoInstance>) change -> rebuildContent());
        state.getAccounts().addListener((ListChangeListener<HMCLDemoAccount>) change -> rebuildContent());

        updateWallpaper();
        rebuildContent();
    }

    /// Rebuilds localized overlay controls while retaining the stable wallpaper view.
    private void rebuildContent() {
        BorderPane page = new BorderPane();
        page.getStyleClass().add("hmcl-home-overlay");
        page.setLeft(createSidebar());

        StackPane foreground = new StackPane();
        foreground.getStyleClass().add("hmcl-home-foreground");
        if (updateNoticeVisible) {
            M3Card update = createUpdateNotice();
            StackPane.setAlignment(update, Pos.TOP_CENTER);
            StackPane.setMargin(update, new Insets(20.0, 20.0, 0.0, 20.0));
            foreground.getChildren().add(update);
        }

        M3SplitButton launch = createLaunchButton();
        StackPane.setAlignment(launch, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(launch, new Insets(0.0, 20.0, 20.0, 0.0));
        foreground.getChildren().add(launch);
        page.setCenter(foreground);

        getChildren().setAll(wallpaperView, page);
    }

    /// Creates the fixed HMCL navigation sidebar shown only on Home.
    ///
    /// @return the localized sidebar
    private Node createSidebar() {
        VBox sidebar = new VBox(3.0);
        sidebar.getStyleClass().add("hmcl-home-sidebar");
        sidebar.setPadding(new Insets(10.0, 8.0, 10.0, 8.0));
        sidebar.setPrefWidth(200.0);
        sidebar.setMinWidth(200.0);
        sidebar.setMaxWidth(200.0);

        sidebar.getChildren().add(sectionLabel(text("home.section.account")));
        sidebar.getChildren().add(createAccountItem());

        sidebar.getChildren().add(sectionLabel(text("home.section.version")));
        sidebar.getChildren().add(createCurrentInstanceItem());
        sidebar.getChildren().add(navigationItem(
                text("home.all_instances"),
                HMCLDemoIcons.INSTANCES,
                () -> actions.navigate(HMCLDemoActions.ROUTE_INSTANCES)
        ));
        sidebar.getChildren().add(navigationItem(
                text("home.download"),
                HMCLDemoIcons.DISCOVER,
                () -> actions.navigate(HMCLDemoActions.ROUTE_DISCOVER)
        ));

        sidebar.getChildren().add(sectionLabel(text("home.section.launcher")));
        sidebar.getChildren().add(navigationItem(
                text("home.launcher_settings"),
                HMCLDemoIcons.SETTINGS,
                () -> actions.navigate(HMCLDemoActions.ROUTE_SETTINGS)
        ));
        sidebar.getChildren().add(navigationItem(
                text("home.multiplayer"),
                HMCLDemoIcons.HOME,
                () -> actions.dispatch("multiplayer")
        ));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);
        sidebar.getChildren().add(navigationItem(
                text("home.feedback"),
                HMCLDemoIcons.CHAT,
                () -> actions.dispatch("feedback")
        ));
        return sidebar;
    }

    /// Creates the current-account sidebar row.
    ///
    /// @return the account row
    private M3ListItem createAccountItem() {
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();
        String headline = account == null ? text("home.no_account") : account.displayName();
        String supporting = account == null ? text("accounts.add") : accountType(account.type());
        M3ListItem item = new M3ListItem(headline);
        item.getStyleClass().add("hmcl-home-navigation-item");
        item.setSupportingText(supporting);
        item.setLeading(account == null
                ? HMCLDemoIcons.create(HMCLDemoIcons.ACCOUNTS)
                : accountFace(account));
        item.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_ACCOUNTS));
        return item;
    }

    /// Creates the current-instance sidebar row.
    ///
    /// @return the selected-instance row
    private M3ListItem createCurrentInstanceItem() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        M3ListItem item = new M3ListItem(text("home.manage_current_instance"));
        item.getStyleClass().add("hmcl-home-navigation-item");
        item.setSupportingText(instance == null ? text("home.no_instance") : instance.name());
        item.setLeading(instance == null
                ? HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES)
                : instanceIcon());
        item.setDisable(instance == null);
        item.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_INSTANCE_DETAIL));
        return item;
    }

    /// Creates one action row in the Home sidebar.
    ///
    /// @param headline the row headline
    /// @param iconPath the row icon path
    /// @param action the action invoked by the row
    /// @return the configured list item
    private M3ListItem navigationItem(
            String headline,
            String iconPath,
            Runnable action
    ) {
        M3ListItem item = new M3ListItem(headline);
        item.getStyleClass().add("hmcl-home-navigation-item");
        item.setLeading(HMCLDemoIcons.create(iconPath));
        item.setOnAction(event -> action.run());
        return item;
    }

    /// Creates a compact uppercase sidebar section label.
    ///
    /// @param value the localized section name
    /// @return the label node
    private M3Text sectionLabel(String value) {
        M3Text label = new M3Text(value, M3TextRole.LABEL_SMALL);
        label.getStyleClass().add("hmcl-sidebar-section-label");
        label.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(label, new Insets(11.0, 12.0, 3.0, 12.0));
        return label;
    }

    /// Creates the preview-channel notice above the wallpaper content.
    ///
    /// @return the update card
    private M3Card createUpdateNotice() {
        M3Text title = new M3Text(text("home.preview.title"), M3TextRole.TITLE_MEDIUM);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        M3IconButton close = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.CLOSE));
        close.setAccessibleText(text("common.close"));
        close.setOnAction(event -> {
            updateNoticeVisible = false;
            rebuildContent();
        });
        HBox header = new HBox(8.0, title, headerSpacer, close);
        header.setAlignment(Pos.CENTER_LEFT);

        M3Text supporting = new M3Text(text("home.preview.body"), M3TextRole.BODY_MEDIUM);
        supporting.setWrapText(true);
        M3Text feedback = new M3Text(text("home.preview.feedback"), M3TextRole.BODY_SMALL);
        feedback.setWrapText(true);
        M3Button view = new M3Button(text("action.view_update"), M3ButtonVariant.TEXT);
        view.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH));

        VBox content = new VBox(8.0, header, supporting, feedback, view);
        M3Card card = new M3Card(content, M3CardVariant.ELEVATED);
        card.getStyleClass().add("hmcl-home-update");
        card.setPrefWidth(560.0);
        card.setMaxWidth(680.0);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        return card;
    }

    /// Creates the bottom-right launch and instance-switch split button.
    ///
    /// @return the launch control
    private M3SplitButton createLaunchButton() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();

        M3Text action = new M3Text(text("home.launch"), M3TextRole.LABEL_LARGE);
        M3Text instanceName = new M3Text(
                instance == null ? text("home.no_instance") : instance.name(),
                M3TextRole.BODY_SMALL
        );
        VBox label = new VBox(1.0, action, instanceName);
        label.setAlignment(Pos.CENTER_LEFT);

        M3SplitButton button = new M3SplitButton();
        button.getStyleClass().add("hmcl-home-launch");
        button.setVariant(M3ButtonVariant.FILLED);
        button.setSize(M3ButtonSize.MEDIUM);
        button.setGraphic(label);
        button.setPrefWidth(230.0);
        button.setMinWidth(230.0);
        button.setMaxWidth(230.0);
        button.setDisable(instance == null || account == null);
        button.setAccessibleText(text("home.launch"));
        button.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_PLAY));

        for (HMCLDemoInstance candidate : state.getInstances()) {
            M3ListItem item = new M3ListItem(candidate.name());
            item.setSupportingText(text(
                    "instances.card.details",
                    candidate.gameVersion(),
                    candidate.loader()
            ));
            item.setLeading(instanceIcon());
            item.setSelected(candidate.equals(instance));
            item.setOnAction(event -> {
                state.selectInstance(candidate.id());
                button.hideMenu();
                actions.dispatch(HMCLDemoActions.ACTION_SELECT_INSTANCE, candidate.id());
            });
            button.getItems().add(item);
        }
        return button;
    }

    /// Updates the stable wallpaper view from the selected HMCL artwork.
    private void updateWallpaper() {
        String path = switch (state.getWallpaper()) {
            case MEADOW -> "img/wallpapers/2021-08-26.jpg";
            case CAVES -> "img/wallpapers/2016-02-25.jpg";
            case SUNSET -> "img/wallpapers/2015-06-22.jpg";
        };
        wallpaperView.setImage(HMCLDemoAssets.image(path));
    }

    /// Returns the classic Minecraft grass icon used by the Home instance entry.
    ///
    /// @return the image view
    private ImageView instanceIcon() {
        return HMCLDemoAssets.imageView("img/grass.png", 32.0, 32.0);
    }

    /// Returns a deterministic Minecraft skin face for one account.
    ///
    /// @param account the represented account
    /// @return the 32-pixel face image
    private ImageView accountFace(HMCLDemoAccount account) {
        String path = switch (account.type()) {
            case MICROSOFT -> "img/skin/wide/steve.png";
            case OFFLINE -> "img/skin/slim/alex.png";
            case EXTERNAL -> "img/skin/wide/noor.png";
        };
        return HMCLDemoAssets.skinFace(path, 32.0);
    }

    /// Resolves the localized account provider label.
    ///
    /// @param type the account provider
    /// @return the localized type label
    private String accountType(HMCLDemoAccount.AccountType type) {
        return text(switch (type) {
            case MICROSOFT -> "accounts.type.microsoft";
            case OFFLINE -> "accounts.type.offline";
            case EXTERNAL -> "accounts.type.external";
        });
    }

    /// Resolves one localized string.
    ///
    /// @param key the resource key
    /// @param args optional formatting arguments
    /// @return the localized string
    private String text(String key, Object... args) {
        return args.length == 0 ? strings.get(key) : strings.format(key, args);
    }
}
