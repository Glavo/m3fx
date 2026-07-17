// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.*;
import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.time.LocalTime;

/// Creates fresh, self-contained control examples for the M3FX Catalog.
///
/// Each factory returns a new JavaFX node tree so that an example may be revisited without violating JavaFX's
/// single-parent rule. Layout nodes in this class only arrange the demonstrated controls; they do not add Catalog
/// cards, page chrome, or other presentation surfaces around an example.
@NotNullByDefault
final class CatalogSamples {
    /// The spacing used between controls that form one example.
    private static final double SAMPLE_SPACING = 16.0;

    /// Prevents instantiation of this factory class.
    private CatalogSamples() {
    }

    /// Creates an attached count badge whose icon button increments the displayed count.
    ///
    /// @return the badge example
    static Node badge() {
        M3Badge badge = new M3Badge(7);
        M3IconButton notifications = iconButton(CatalogIcons.NOTIFICATIONS, "Increment notification count");
        notifications.setOnAction(event -> badge.setText(Integer.toString(Integer.parseInt(badge.getText()) + 1)));
        return row(new M3BadgedBox(notifications, badge));
    }

    /// Creates a bottom app bar with regular actions and a floating primary action.
    ///
    /// @return the bottom app bar example
    static Node bottomAppBar() {
        M3BottomAppBar bar = new M3BottomAppBar();
        bar.getActions().addAll(
                iconButton(CatalogIcons.EDIT, "Edit"),
                iconButton(CatalogIcons.FAVORITE, "Favorite"),
                iconButton(CatalogIcons.SETTINGS, "Settings")
        );
        bar.setFloatingAction(new M3FloatingActionButton(icon(CatalogIcons.ADD)));
        bar.setPrefWidth(520.0);
        return bar;
    }

    /// Creates a standard bottom sheet with local show and hide controls.
    ///
    /// @return the bottom sheet example
    static Node bottomSheet() {
        M3BottomSheet sheet = new M3BottomSheet(
                "Choose an account",
                new M3Text("Sheet content remains part of this example route.", M3TextRole.BODY_MEDIUM)
        );
        M3Button close = new M3Button("Close", M3ButtonVariant.TEXT);
        close.setOnAction(event -> sheet.hide());
        sheet.getActions().add(close);

        M3Button show = new M3Button("Show sheet", M3ButtonVariant.TONAL);
        show.setOnAction(event -> sheet.show());
        return column(show, sheet);
    }

    /// Creates standard and connected button groups.
    ///
    /// @return the button group example
    static Node buttonGroups() {
        M3ButtonGroup standard = new M3ButtonGroup();
        standard.getItems().addAll(
                new M3Button("Archive", M3ButtonVariant.TONAL),
                new M3Button("Share", M3ButtonVariant.TONAL),
                new M3Button("Edit", M3ButtonVariant.TONAL)
        );

        M3ButtonGroup connected = new M3ButtonGroup();
        connected.setVariant(M3ButtonGroupVariant.CONNECTED);
        connected.getItems().addAll(
                new M3Button("Day", M3ButtonVariant.OUTLINED),
                new M3Button("Week", M3ButtonVariant.OUTLINED),
                new M3Button("Month", M3ButtonVariant.OUTLINED)
        );
        return column(standard, connected);
    }

    /// Creates the five Material button emphasis variants with a shared action result.
    ///
    /// @return the button example
    static Node buttons() {
        M3Text result = new M3Text("Choose an action", M3TextRole.BODY_MEDIUM);
        HBox buttons = row(
                actionButton("Filled", M3ButtonVariant.FILLED, result),
                actionButton("Tonal", M3ButtonVariant.TONAL, result),
                actionButton("Outlined", M3ButtonVariant.OUTLINED, result),
                actionButton("Text", M3ButtonVariant.TEXT, result),
                actionButton("Elevated", M3ButtonVariant.ELEVATED, result)
        );
        return column(buttons, result);
    }

    /// Creates filled, outlined, and elevated cards, including an actionable card.
    ///
    /// @return the card example
    static Node cards() {
        M3Text status = new M3Text("Select a card", M3TextRole.BODY_MEDIUM);
        M3Card filled = sampleCard("Filled", "Tonal grouping", M3CardVariant.FILLED);
        M3Card outlined = sampleCard("Outlined", "Bounded grouping", M3CardVariant.OUTLINED);
        M3Card elevated = sampleCard("Elevated", "Raised grouping", M3CardVariant.ELEVATED);
        elevated.setOnAction(event -> status.setText("Elevated card selected"));
        return column(row(filled, outlined, elevated), status);
    }

    /// Creates a multi-item carousel with explicit previous and next actions.
    ///
    /// @return the carousel example
    static Node carousel() {
        M3Carousel carousel = new M3Carousel();
        carousel.getItems().addAll(
                sampleCard("Morning", "Deep work", M3CardVariant.FILLED),
                sampleCard("Review", "Design notes", M3CardVariant.ELEVATED),
                sampleCard("Release", "Packaging", M3CardVariant.OUTLINED),
                sampleCard("Archive", "History", M3CardVariant.FILLED)
        );
        carousel.setWrapAround(true);
        carousel.setPrefSize(620.0, 190.0);

        M3Button previous = new M3Button("Previous", M3ButtonVariant.OUTLINED);
        previous.setOnAction(event -> carousel.selectPrevious());
        M3Button next = new M3Button("Next", M3ButtonVariant.FILLED);
        next.setOnAction(event -> carousel.selectNext());
        return column(carousel, row(previous, next));
    }

    /// Creates determinate and three-state checkboxes.
    ///
    /// @return the checkbox example
    static Node checkboxes() {
        M3CheckBox enabled = new M3CheckBox("Notifications");
        enabled.setSelected(true);

        M3CheckBox threeState = new M3CheckBox("Sync all folders");
        threeState.setAllowIndeterminate(true);
        threeState.setIndeterminate(true);

        M3CheckBox unchecked = new M3CheckBox("Share diagnostics");
        return column(enabled, threeState, unchecked);
    }

    /// Creates assist chips for compact contextual actions.
    ///
    /// @return the assist chip example
    static Node assistChips() {
        return row(
                new M3AssistChip("Add to calendar", icon(CatalogIcons.ADD)),
                new M3AssistChip("Directions", icon(CatalogIcons.NAVIGATION)),
                new M3AssistChip("Edit", icon(CatalogIcons.EDIT))
        );
    }

    /// Creates selectable filter chips.
    ///
    /// @return the filter chip example
    static Node filterChips() {
        M3ChipGroup group = new M3ChipGroup();
        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        M3FilterChip nearby = new M3FilterChip("Nearby");
        nearby.setSelected(true);
        group.getItems().addAll(nearby, new M3FilterChip("Open now"), new M3FilterChip("Top rated"));
        return group;
    }

    /// Creates input chips with removable-value affordances.
    ///
    /// @return the input chip example
    static Node inputChips() {
        M3InputChip first = new M3InputChip("Morgan");
        first.setTrailingGraphic(iconButton(CatalogIcons.ADD, "Remove Morgan"));
        M3InputChip second = new M3InputChip("Taylor");
        second.setSelected(true);
        return row(first, second);
    }

    /// Creates suggestion chips that issue stateless actions.
    ///
    /// @return the suggestion chip example
    static Node suggestionChips() {
        M3Text response = new M3Text("Choose a suggestion", M3TextRole.BODY_MEDIUM);
        M3SuggestionChip yes = new M3SuggestionChip("Yes");
        yes.setOnAction(event -> response.setText("Yes selected"));
        M3SuggestionChip later = new M3SuggestionChip("Remind me later");
        later.setOnAction(event -> response.setText("Reminder deferred"));
        return column(row(yes, later), response);
    }

    /// Creates an inline date picker initialized to the current date.
    ///
    /// @return the date picker example
    static Node datePicker() {
        M3DatePicker picker = new M3DatePicker(LocalDate.now());
        picker.setPrefWidth(420.0);
        return picker;
    }

    /// Creates an inline Material dialog pane with standard actions.
    ///
    /// @return the dialog pane example
    static Node dialog() {
        M3DialogPane pane = new M3DialogPane();
        pane.setHeaderText("Discard draft?");
        pane.setContentText("This action cannot be undone.");
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        return pane;
    }

    /// Creates extended floating action buttons with icon and label content.
    ///
    /// @return the extended FAB example
    static Node extendedFabs() {
        M3FloatingActionButton create = new M3FloatingActionButton("Create", icon(CatalogIcons.ADD));
        M3FloatingActionButton edit = new M3FloatingActionButton("Edit", icon(CatalogIcons.EDIT));
        edit.setVariant(M3FloatingActionButtonVariant.TERTIARY_CONTAINER);
        return row(create, edit);
    }

    /// Creates an expanded floating action button menu with direct action items.
    ///
    /// @return the FAB menu example
    static Node fabMenu() {
        M3FabMenu menu = new M3FabMenu(new M3FloatingActionButton(icon(CatalogIcons.ADD)));
        menu.getItems().addAll(
                new M3FloatingActionButton("New document", icon(CatalogIcons.EDIT)),
                new M3FloatingActionButton("Favorite", icon(CatalogIcons.FAVORITE))
        );
        menu.setExpanded(true);
        return menu;
    }

    /// Creates floating action buttons across the Material size scale.
    ///
    /// @return the FAB example
    static Node floatingActionButtons() {
        M3FloatingActionButton small = new M3FloatingActionButton(icon(CatalogIcons.ADD));
        small.setSize(M3FloatingActionButtonSize.SMALL);
        M3FloatingActionButton regular = new M3FloatingActionButton(icon(CatalogIcons.EDIT));
        M3FloatingActionButton medium = new M3FloatingActionButton(icon(CatalogIcons.FAVORITE));
        medium.setSize(M3FloatingActionButtonSize.MEDIUM);
        M3FloatingActionButton large = new M3FloatingActionButton(icon(CatalogIcons.SETTINGS));
        large.setSize(M3FloatingActionButtonSize.LARGE);
        return row(small, regular, medium, large);
    }

    /// Creates a floating toolbar containing common icon actions.
    ///
    /// @return the floating toolbar example
    static Node floatingToolbar() {
        M3Toolbar toolbar = new M3Toolbar();
        toolbar.setVariant(M3ToolbarVariant.FLOATING);
        toolbar.getItems().addAll(
                iconButton(CatalogIcons.EDIT, "Edit"),
                iconButton(CatalogIcons.FAVORITE, "Favorite"),
                iconButton(CatalogIcons.SETTINGS, "Settings")
        );
        return toolbar;
    }

    /// Creates regular and toggle icon buttons.
    ///
    /// @return the icon button example
    static Node iconButtons() {
        M3IconToggleButton favorite = new M3IconToggleButton(icon(CatalogIcons.FAVORITE));
        favorite.setVariant(M3IconToggleButtonVariant.TONAL);
        favorite.setSelected(true);
        return row(
                iconButton(CatalogIcons.EDIT, "Edit"),
                iconButton(CatalogIcons.SETTINGS, "Settings"),
                favorite
        );
    }

    /// Creates a selectable list with one- and two-line items.
    ///
    /// @return the list example
    static Node lists() {
        M3ListPane list = new M3ListPane();
        list.setSelectionMode(M3SelectionMode.SINGLE);
        M3ListItem inbox = new M3ListItem("Inbox");
        inbox.setSupportingText("12 unread messages");
        inbox.setLeadingIcon("I");
        M3ListItem drafts = new M3ListItem("Drafts");
        drafts.setSupportingText("3 saved drafts");
        drafts.setLeadingIcon("D");
        M3ListItem archive = new M3ListItem("Archive");
        archive.setLeadingIcon("A");
        list.getItems().addAll(inbox, drafts, archive);
        list.setMaxWidth(420.0);
        return list;
    }

    /// Creates default and contained loading indicators.
    ///
    /// @return the loading indicator example
    static Node loadingIndicators() {
        M3LoadingIndicator defaultIndicator = new M3LoadingIndicator();
        M3LoadingIndicator containedIndicator = new M3LoadingIndicator();
        containedIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);
        return row(defaultIndicator, containedIndicator);
    }

    /// Creates a menu button with regular actions and a submenu.
    ///
    /// @return the menu example
    static Node menus() {
        M3SubMenuItem moveTo = new M3SubMenuItem(
                "Move to",
                new M3MenuItem("Archive"),
                new M3MenuItem("Projects")
        );
        return new M3MenuButton(
                "Open menu",
                new M3MenuItem("Duplicate"),
                moveTo,
                new M3MenuItem("Delete")
        );
    }

    /// Creates a medium-window horizontal navigation bar.
    ///
    /// @return the navigation bar example
    static Node navigationBar() {
        M3NavigationBar bar = new M3NavigationBar();
        bar.setItemLayout(M3NavigationItemLayout.HORIZONTAL);
        bar.getItems().addAll(
                navigationItem("Home", CatalogIcons.HOME),
                navigationItem("Favorites", CatalogIcons.FAVORITE),
                navigationItem("Settings", CatalogIcons.SETTINGS)
        );
        bar.setPrefWidth(560.0);
        return bar;
    }

    /// Creates a standard navigation drawer with selectable destinations.
    ///
    /// @return the navigation drawer example
    static Node navigationDrawer() {
        M3NavigationDrawer drawer = new M3NavigationDrawer();
        M3ListItem inbox = new M3ListItem("Inbox");
        inbox.setLeadingIcon("I");
        M3ListItem starred = new M3ListItem("Starred");
        starred.setLeadingIcon("S");
        M3ListItem settings = new M3ListItem("Settings");
        settings.setLeadingIcon("G");
        drawer.getItems().addAll(inbox, starred, settings);
        drawer.setPrefSize(320.0, 280.0);
        return drawer;
    }

    /// Creates an expandable navigation rail with three destinations.
    ///
    /// @return the navigation rail example
    static Node navigationRail() {
        M3NavigationRail rail = new M3NavigationRail();
        rail.getItems().addAll(
                navigationItem("Home", CatalogIcons.HOME),
                navigationItem("Favorites", CatalogIcons.FAVORITE),
                navigationItem("Settings", CatalogIcons.SETTINGS)
        );
        M3Button toggle = new M3Button("Collapse rail", M3ButtonVariant.TONAL);
        toggle.setOnAction(event -> {
            rail.setExpanded(!rail.isExpanded());
            toggle.setText(rail.isExpanded() ? "Collapse rail" : "Expand rail");
        });
        rail.setHeader(toggle);
        rail.setExpanded(true);
        rail.setPrefHeight(340.0);
        return rail;
    }

    /// Creates determinate and indeterminate linear and circular progress indicators.
    ///
    /// @return the progress indicator example
    static Node progressIndicators() {
        M3ProgressBar determinateBar = new M3ProgressBar(0.64);
        determinateBar.setPrefWidth(320.0);
        M3ProgressBar indeterminateBar = new M3ProgressBar(-1.0);
        indeterminateBar.setPrefWidth(320.0);
        return column(
                determinateBar,
                indeterminateBar,
                row(new M3ProgressIndicator(0.64), new M3ProgressIndicator(-1.0))
        );
    }

    /// Creates a mutually exclusive radio button group.
    ///
    /// @return the radio button example
    static Node radioButtons() {
        ToggleGroup group = new ToggleGroup();
        M3RadioButton day = new M3RadioButton("Day");
        M3RadioButton week = new M3RadioButton("Week");
        M3RadioButton month = new M3RadioButton("Month");
        day.setToggleGroup(group);
        week.setToggleGroup(group);
        month.setToggleGroup(group);
        week.setSelected(true);
        return row(day, week, month);
    }

    /// Creates a search bar whose submission result is shown below it.
    ///
    /// @return the search bar example
    static Node searchBar() {
        M3SearchBar search = new M3SearchBar("Search components");
        search.setLeading(icon(CatalogIcons.NAVIGATION));
        M3Text result = new M3Text("Enter a query", M3TextRole.BODY_MEDIUM);
        search.setOnAction(event -> result.setText(
                search.getText().isBlank() ? "No query entered" : "Searching for “" + search.getText() + "”"
        ));
        search.setPrefWidth(420.0);
        return column(search, result);
    }

    /// Creates a single-select segmented button group.
    ///
    /// @return the segmented button example
    static Node segmentedButtons() {
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
        group.setSelectionMode(M3SelectionMode.SINGLE);
        M3SegmentedButton day = new M3SegmentedButton("Day");
        M3SegmentedButton week = new M3SegmentedButton("Week");
        M3SegmentedButton month = new M3SegmentedButton("Month");
        week.setSelected(true);
        group.getItems().addAll(day, week, month);
        return group;
    }

    /// Creates single-value and range sliders.
    ///
    /// @return the slider example
    static Node sliders() {
        M3Slider slider = new M3Slider(0.0, 100.0, 48.0);
        slider.setPrefWidth(380.0);
        M3RangeSlider range = new M3RangeSlider(0.0, 100.0, 24.0, 76.0);
        range.setPrefWidth(380.0);
        return column(slider, range);
    }

    /// Creates a local snackbar host and an action that displays a fresh snackbar.
    ///
    /// @return the snackbar example
    static Node snackbars() {
        M3SnackbarHost host = new M3SnackbarHost();
        host.setPrefWidth(420.0);
        M3Button show = new M3Button("Show snackbar", M3ButtonVariant.FILLED);
        show.setOnAction(event -> {
            M3Snackbar snackbar = new M3Snackbar("Message archived", "Undo");
            snackbar.setOnAction(actionEvent -> host.dismiss());
            host.show(snackbar);
        });
        return column(show, host);
    }

    /// Creates filled and outlined split buttons with attached menus.
    ///
    /// @return the split button example
    static Node splitButtons() {
        M3SplitButton save = new M3SplitButton("Save");
        save.getItems().addAll(new M3MenuItem("Save as copy"), new M3MenuItem("Save template"));
        M3SplitButton export = new M3SplitButton("Export");
        export.setVariant(M3ButtonVariant.OUTLINED);
        export.getItems().addAll(new M3MenuItem("PDF"), new M3MenuItem("Image"));
        return row(save, export);
    }

    /// Creates switches in their on and off states.
    ///
    /// @return the switch example
    static Node switches() {
        M3Switch notifications = new M3Switch("Notifications");
        notifications.setSelected(true);
        M3Switch location = new M3Switch("Location sharing");
        return column(notifications, location);
    }

    /// Creates a primary tab bar with three selectable tabs.
    ///
    /// @return the tab example
    static Node tabs() {
        M3TabBar bar = new M3TabBar();
        M3Tab overview = new M3Tab("Overview");
        M3Tab activity = new M3Tab("Activity");
        M3Tab settings = new M3Tab("Settings");
        overview.setSelected(true);
        bar.getTabs().addAll(overview, activity, settings);
        bar.setPrefWidth(520.0);
        return bar;
    }

    /// Creates filled and outlined text fields with labels and supporting text.
    ///
    /// @return the text field example
    static Node textFields() {
        M3TextField name = new M3TextField("M3FX");
        M3TextInputLayout filled = new M3TextInputLayout(name, "Project name", "Visible to collaborators");

        M3TextField email = new M3TextField("support@example.com");
        email.setVariant(M3TextInputVariant.OUTLINED);
        M3TextInputLayout outlined = new M3TextInputLayout(email, "Email", "Used for notifications");
        filled.setPrefWidth(360.0);
        outlined.setPrefWidth(360.0);
        return column(filled, outlined);
    }

    /// Creates an inline time picker initialized to a representative time.
    ///
    /// @return the time picker example
    static Node timePicker() {
        return new M3TimePicker(LocalTime.of(10, 30));
    }

    /// Creates plain and rich tooltips attached to visible buttons.
    ///
    /// @return the tooltip example
    static Node tooltips() {
        M3Button plainAnchor = new M3Button("Point at me", M3ButtonVariant.OUTLINED);
        M3Tooltip.install(plainAnchor, new M3Tooltip("Plain tooltip"));

        M3Button richAnchor = new M3Button("More details", M3ButtonVariant.TONAL);
        M3RichTooltip rich = new M3RichTooltip(
                "Rich tooltip",
                "Rich tooltips can hold longer contextual guidance."
        );
        M3Tooltip.install(richAnchor, rich);
        return row(plainAnchor, richAnchor);
    }

    /// Creates a small top app bar with navigation and trailing actions.
    ///
    /// @return the top app bar example
    static Node topAppBar() {
        M3TopAppBar bar = new M3TopAppBar("Inbox");
        bar.setNavigation(iconButton(CatalogIcons.NAVIGATION, "Open navigation"));
        bar.getActions().addAll(
                iconButton(CatalogIcons.FAVORITE, "Favorite"),
                iconButton(CatalogIcons.SETTINGS, "Settings")
        );
        bar.setPrefWidth(620.0);
        return bar;
    }

    /// Creates representative text from the Material type scale.
    ///
    /// @return the typography example
    static Node typography() {
        return column(
                new M3Text("Display small", M3TextRole.DISPLAY_SMALL),
                new M3Text("Headline medium", M3TextRole.HEADLINE_MEDIUM),
                new M3Text("Title large", M3TextRole.TITLE_LARGE),
                new M3Text("Body medium communicates supporting content.", M3TextRole.BODY_MEDIUM),
                new M3Text("Label large", M3TextRole.LABEL_LARGE)
        );
    }

    /// Creates a button that reports its variant when activated.
    ///
    /// @param text the button label
    /// @param variant the Material button variant
    /// @param result the text node that receives the action result
    /// @return the configured button
    private static M3Button actionButton(String text, M3ButtonVariant variant, M3Text result) {
        M3Button button = new M3Button(text, variant);
        button.setOnAction(event -> result.setText(text + " activated"));
        return button;
    }

    /// Creates a fixed-size content card for card and carousel examples.
    ///
    /// @param title the card title
    /// @param supportingText the card supporting text
    /// @param variant the Material card variant
    /// @return the configured card
    private static M3Card sampleCard(String title, String supportingText, M3CardVariant variant) {
        VBox content = new VBox(
                8.0,
                new M3Text(title, M3TextRole.TITLE_MEDIUM),
                new M3Text(supportingText, M3TextRole.BODY_MEDIUM)
        );
        content.setPadding(new Insets(20.0));
        M3Card card = new M3Card(content, variant);
        card.setPrefSize(180.0, 124.0);
        return card;
    }

    /// Creates an icon button with accessible text.
    ///
    /// @param path the SVG path content
    /// @param accessibleText the accessible action description
    /// @return the configured icon button
    private static M3IconButton iconButton(String path, String accessibleText) {
        M3IconButton button = new M3IconButton(icon(path));
        button.setAccessibleText(accessibleText);
        return button;
    }

    /// Creates a navigation destination with an SVG icon.
    ///
    /// @param text the destination label
    /// @param path the SVG path content
    /// @return the configured navigation item
    private static M3NavigationItem navigationItem(String text, String path) {
        return new M3NavigationItem(text, icon(path));
    }

    /// Creates a Catalog SVG node for a control graphic slot.
    ///
    /// @param path the SVG path content
    /// @return the vector icon node
    private static Node icon(String path) {
        return CatalogIcons.create(path);
    }

    /// Arranges controls horizontally without adding a presentation surface.
    ///
    /// @param children the nodes to arrange
    /// @return the centered horizontal layout
    private static HBox row(Node... children) {
        HBox row = new HBox(SAMPLE_SPACING, children);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    /// Arranges controls vertically without adding a presentation surface.
    ///
    /// @param children the nodes to arrange
    /// @return the centered vertical layout
    private static VBox column(Node... children) {
        VBox column = new VBox(SAMPLE_SPACING, children);
        column.setAlignment(Pos.CENTER);
        column.setPadding(new Insets(SAMPLE_SPACING));
        for (Node child : children) {
            if (child instanceof Region region) {
                region.setMinWidth(Region.USE_PREF_SIZE);
            }
        }
        return column;
    }
}
