package org.glavo.m3fx.demo;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipVariant;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3PasswordField;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/// A demo application that showcases the first m3fx controls.
@NotNullByDefault
public final class M3FXDemoApp extends Application {
    /// Seed colors shown in the demo header.
    private static final @Unmodifiable List<Color> SEED_COLORS = List.of(
            Color.web("#6750a4"),
            Color.web("#006a6a"),
            Color.web("#b3261e"),
            Color.web("#386a20"),
            Color.web("#7d5260")
    );

    /// The current seed color used by the demo theme.
    private Color seedColor = M3Theme.DEFAULT_SEED_COLOR;

    /// The current Material Design profile.
    private M3Profile profile = M3Profile.BASELINE_2021;

    /// The current theme brightness.
    private Brightness brightness = Brightness.LIGHT;

    /// The active JavaFX scene.
    private @Nullable Scene scene;

    /// The persistent snackbar instance used by demo actions.
    private @Nullable M3Snackbar snackbar;

    /// Starts the demo application.
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("demo-root");

        M3Snackbar snackbar = new M3Snackbar();
        snackbar.setVisible(false);
        snackbar.setManaged(false);
        this.snackbar = snackbar;

        StackPane centerStack = new StackPane(createContent(), snackbar);
        StackPane.setAlignment(snackbar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(snackbar, new Insets(0.0, 0.0, 24.0, 0.0));

        root.setTop(createHeader());
        root.setCenter(centerStack);

        Scene scene = new Scene(root, 1120.0, 820.0);
        scene.getStylesheets().add(demoStylesheetUrl());
        this.scene = scene;
        applyTheme();

        stage.setTitle("m3fx Demo");
        stage.setMinWidth(900.0);
        stage.setMinHeight(640.0);
        stage.setScene(scene);
        stage.show();
    }

    /// Creates the header with theme controls.
    private Node createHeader() {
        VBox titleBox = new VBox(2.0);
        titleBox.getStyleClass().add("demo-title-box");

        Label title = new Label("m3fx");
        title.getStyleClass().add("demo-title");
        Label subtitle = new Label("Material Design 3 controls for JavaFX");
        subtitle.getStyleClass().add("demo-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox seedButtons = new HBox(8.0);
        seedButtons.getStyleClass().add("demo-seed-buttons");
        for (Color color : SEED_COLORS) {
            M3IconButton button = new M3IconButton();
            button.getStyleClass().add("demo-seed-button");
            button.setStyle("-fx-background-color: " + toHex(color) + ";");
            button.setOnAction(event -> {
                seedColor = color;
                applyTheme();
            });
            seedButtons.getChildren().add(button);
        }

        M3Button profileButton = new M3Button("Expressive");
        profileButton.setVariant(M3ButtonVariant.OUTLINED);
        profileButton.setOnAction(event -> {
            profile = profile == M3Profile.BASELINE_2021 ? M3Profile.EXPRESSIVE_2025 : M3Profile.BASELINE_2021;
            profileButton.setText(profile == M3Profile.BASELINE_2021 ? "Expressive" : "Baseline");
            applyTheme();
        });

        M3Button brightnessButton = new M3Button("Dark");
        brightnessButton.setVariant(M3ButtonVariant.TONAL);
        brightnessButton.setOnAction(event -> {
            brightness = brightness == Brightness.LIGHT ? Brightness.DARK : Brightness.LIGHT;
            brightnessButton.setText(brightness == Brightness.LIGHT ? "Dark" : "Light");
            applyTheme();
        });

        HBox header = new HBox(18.0, titleBox, spacer, seedButtons, profileButton, brightnessButton);
        header.getStyleClass().add("demo-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /// Creates the scrollable demo content.
    private Node createContent() {
        VBox content = new VBox(28.0);
        content.getStyleClass().add("demo-content");
        content.setFillWidth(true);
        content.getChildren().addAll(
                createButtonSection(),
                createInputSection(),
                createSelectionSection(),
                createUtilitySection(),
                createListSection(),
                createProgressSection(),
                createContainmentSection()
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("demo-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    /// Creates the button showcase section.
    private Node createButtonSection() {
        FlowPane controls = createFlow();
        controls.getChildren().addAll(
                createButton("Filled", M3ButtonVariant.FILLED),
                createButton("Tonal", M3ButtonVariant.TONAL),
                createButton("Outlined", M3ButtonVariant.OUTLINED),
                createButton("Text", M3ButtonVariant.TEXT),
                createButton("Elevated", M3ButtonVariant.ELEVATED),
                createIconButton(),
                createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR),
                createFab("+", M3FloatingActionButtonVariant.SECONDARY, M3FloatingActionButtonSize.SMALL),
                createFab("*", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.LARGE),
                createExtendedFab()
        );
        return createSection("Buttons", controls);
    }

    /// Creates the input showcase section.
    private Node createInputSection() {
        M3TextField filledField = new M3TextField();
        filledField.setPromptText("Filled text field");
        filledField.setPrefWidth(280.0);

        M3TextField outlinedField = new M3TextField();
        outlinedField.setPromptText("Outlined text field");
        outlinedField.setVariant(M3TextInputVariant.OUTLINED);
        outlinedField.setPrefWidth(280.0);

        M3PasswordField passwordField = new M3PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setVariant(M3TextInputVariant.OUTLINED);
        passwordField.setPrefWidth(280.0);

        FlowPane controls = createFlow();
        controls.getChildren().addAll(filledField, outlinedField, passwordField);
        return createSection("Inputs", controls);
    }

    /// Creates the selection control showcase section.
    private Node createSelectionSection() {
        M3CheckBox checkbox = new M3CheckBox("Checkbox");
        checkbox.setSelected(true);

        ToggleGroup radioGroup = new ToggleGroup();
        M3RadioButton radioOne = new M3RadioButton("Radio A");
        M3RadioButton radioTwo = new M3RadioButton("Radio B");
        radioOne.setToggleGroup(radioGroup);
        radioTwo.setToggleGroup(radioGroup);
        radioOne.setSelected(true);

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setSelected(true);

        M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
        slider.setPrefWidth(220.0);

        M3Chip assistChip = new M3Chip("Assist");
        M3Chip filterChip = new M3Chip("Filter");
        filterChip.setVariant(M3ChipVariant.FILTER);
        filterChip.setSelected(true);
        M3Chip inputChip = new M3Chip("Input");
        inputChip.setVariant(M3ChipVariant.INPUT);
        M3Chip suggestionChip = new M3Chip("Suggestion");
        suggestionChip.setVariant(M3ChipVariant.SUGGESTION);

        ToggleGroup segmentedGroup = new ToggleGroup();
        M3SegmentedButton daySegment = new M3SegmentedButton("Day");
        M3SegmentedButton weekSegment = new M3SegmentedButton("Week");
        M3SegmentedButton monthSegment = new M3SegmentedButton("Month");
        daySegment.setToggleGroup(segmentedGroup);
        weekSegment.setToggleGroup(segmentedGroup);
        monthSegment.setToggleGroup(segmentedGroup);
        weekSegment.setSelected(true);
        M3SegmentedButtonGroup segmentedButtons =
                new M3SegmentedButtonGroup(daySegment, weekSegment, monthSegment);

        FlowPane controls = createFlow();
        controls.getChildren().addAll(
                checkbox,
                radioOne,
                radioTwo,
                switchControl,
                slider,
                segmentedButtons,
                assistChip,
                filterChip,
                inputChip,
                suggestionChip
        );
        return createSection("Selection", controls);
    }

    /// Creates the badge and divider showcase section.
    private Node createUtilitySection() {
        M3Badge dotBadge = new M3Badge();
        M3Badge countBadge = new M3Badge("7");
        M3Badge overflowBadge = new M3Badge("1234");

        M3Divider fullDivider = new M3Divider();
        fullDivider.setPrefWidth(260.0);

        M3Divider insetDivider = new M3Divider();
        insetDivider.setInsetStart(24.0);
        insetDivider.setPrefWidth(260.0);

        M3Divider verticalDivider = new M3Divider(Orientation.VERTICAL);
        verticalDivider.setPrefHeight(48.0);

        FlowPane controls = createFlow();
        controls.getChildren().addAll(dotBadge, countBadge, overflowBadge, fullDivider, insetDivider, verticalDivider);
        return createSection("Utility", controls);
    }

    /// Creates the list item showcase section.
    private Node createListSection() {
        M3ListItem oneLineItem = new M3ListItem("One-line list item");
        oneLineItem.setLeading(new M3Badge());

        M3ListItem twoLineItem = new M3ListItem("Two-line list item");
        twoLineItem.setSupportingText("Supporting text describes the item.");
        twoLineItem.setTrailing(new M3Badge("3"));

        M3ListItem threeLineItem = new M3ListItem("Three-line list item");
        threeLineItem.setOverlineText("Overline");
        threeLineItem.setSupportingText("Supporting text can span a denser row while keeping token-driven height.");

        M3ListItem selectedItem = new M3ListItem("Selected list item");
        selectedItem.setSupportingText("Selected state uses the active theme colors.");
        selectedItem.setSelected(true);

        VBox list = new VBox();
        list.getStyleClass().add("demo-list");
        list.getChildren().addAll(
                oneLineItem,
                new M3Divider(),
                twoLineItem,
                new M3Divider(),
                threeLineItem,
                new M3Divider(),
                selectedItem
        );
        return createSection("List items", list);
    }

    /// Creates the progress showcase section.
    private Node createProgressSection() {
        M3ProgressBar progressBar = new M3ProgressBar(0.62);
        progressBar.setPrefWidth(360.0);

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.62);
        progressIndicator.setPrefSize(64.0, 64.0);

        M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
        indeterminateIndicator.setPrefSize(64.0, 64.0);

        FlowPane controls = createFlow();
        controls.getChildren().addAll(progressBar, progressIndicator, indeterminateIndicator);
        return createSection("Progress", controls);
    }

    /// Creates the card, dialog, and snackbar showcase section.
    private Node createContainmentSection() {
        M3Card filledCard = createSampleCard("Filled card", M3CardVariant.FILLED);
        M3Card outlinedCard = createSampleCard("Outlined card", M3CardVariant.OUTLINED);
        M3Card elevatedCard = createSampleCard("Elevated card", M3CardVariant.ELEVATED);

        M3Button dialogButton = new M3Button("Open dialog");
        dialogButton.setVariant(M3ButtonVariant.FILLED);
        dialogButton.setOnAction(event -> showDemoDialog());

        M3Button snackbarButton = new M3Button("Show snackbar");
        snackbarButton.setVariant(M3ButtonVariant.TONAL);
        snackbarButton.setOnAction(event -> showSnackbar());

        FlowPane controls = createFlow();
        controls.getChildren().addAll(filledCard, outlinedCard, elevatedCard, dialogButton, snackbarButton);
        return createSection("Containment", controls);
    }

    /// Creates a button configured with the requested variant.
    private M3Button createButton(String text, M3ButtonVariant variant) {
        M3Button button = new M3Button(text);
        button.setVariant(variant);
        return button;
    }

    /// Creates the sample icon button.
    private M3IconButton createIconButton() {
        Label label = new Label("i");
        label.getStyleClass().add("demo-icon-label");
        return new M3IconButton(label);
    }

    /// Creates a sample floating action button.
    private M3FloatingActionButton createFab(
            String iconText,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        Label label = new Label(iconText);
        label.getStyleClass().add("demo-fab-icon");
        M3FloatingActionButton button = new M3FloatingActionButton(label);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a sample extended floating action button.
    private M3FloatingActionButton createExtendedFab() {
        M3FloatingActionButton button = new M3FloatingActionButton("Create");
        button.setVariant(M3FloatingActionButtonVariant.SURFACE);
        return button;
    }

    /// Creates a sample card for the containment section.
    private M3Card createSampleCard(String title, M3CardVariant variant) {
        VBox content = new VBox(6.0);
        content.getStyleClass().add("demo-card-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");
        Label bodyLabel = new Label("Surface, shape, and state tokens are applied through m3fx CSS.");
        bodyLabel.getStyleClass().add("demo-card-body");
        bodyLabel.setWrapText(true);

        content.getChildren().addAll(titleLabel, bodyLabel);

        M3Card card = new M3Card(content);
        card.setVariant(variant);
        card.setPrefWidth(260.0);
        return card;
    }

    /// Creates a titled demo section.
    private Node createSection(String title, Node content) {
        VBox section = new VBox(14.0);
        section.getStyleClass().add("demo-section");
        section.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-section-title");

        section.getChildren().addAll(titleLabel, content);
        return section;
    }

    /// Creates a wrapping flow layout for controls.
    private FlowPane createFlow() {
        FlowPane flow = new FlowPane(16.0, 16.0);
        flow.getStyleClass().add("demo-flow");
        flow.setAlignment(Pos.CENTER_LEFT);
        flow.setMaxWidth(Double.MAX_VALUE);
        return flow;
    }

    /// Opens the demo dialog.
    private void showDemoDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("M3 Dialog");

        M3DialogPane pane = new M3DialogPane();
        pane.setHeaderText("Dialog title");
        pane.setContentText("This dialog uses the m3fx dialog pane style and active theme tokens.");
        pane.getButtonTypes().add(ButtonType.OK);
        pane.getStylesheets().add(M3ThemeManager.stylesheetUrl());
        pane.setStyle(createTheme().toRootStyleDeclarations());
        dialog.setDialogPane(pane);

        Scene activeScene = scene;
        if (activeScene != null) {
            dialog.initOwner(activeScene.getWindow());
        }
        dialog.showAndWait();
    }

    /// Shows the demo snackbar.
    private void showSnackbar() {
        M3Snackbar snackbar = this.snackbar;
        if (snackbar == null) {
            return;
        }

        snackbar.setText("Theme-aware snackbar");
        snackbar.setActionText("Action");
        snackbar.setOnAction(event -> snackbar.setText("Action pressed"));
        snackbar.setVisible(true);
        snackbar.setManaged(true);

        PauseTransition transition = new PauseTransition(Duration.seconds(3.0));
        transition.setOnFinished(event -> {
            snackbar.setVisible(false);
            snackbar.setManaged(false);
        });
        transition.playFromStart();
    }

    /// Applies the current theme to the scene.
    private void applyTheme() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3ThemeManager.install(activeScene, createTheme());
    }

    /// Creates a theme from the current demo controls.
    private M3Theme createTheme() {
        return M3Theme.fromSeed(seedColor, profile, brightness, M3Density.standard());
    }

    /// Returns the demo stylesheet URL.
    private static String demoStylesheetUrl() {
        URL url = M3FXDemoApp.class.getResource("/org/glavo/m3fx/demo/m3fx-demo.css");
        if (url == null) {
            throw new IllegalStateException("Missing demo stylesheet resource");
        }
        return url.toExternalForm();
    }

    /// Converts a color to a hexadecimal CSS value.
    private static String toHex(Color color) {
        Objects.requireNonNull(color, "color");
        return "#"
                + toHexChannel(color.getRed())
                + toHexChannel(color.getGreen())
                + toHexChannel(color.getBlue());
    }

    /// Converts a color channel to a two-character hexadecimal value.
    private static String toHexChannel(double value) {
        String hex = Integer.toHexString((int) Math.round(value * 255.0));
        return hex.length() == 1 ? "0" + hex : hex;
    }
}
