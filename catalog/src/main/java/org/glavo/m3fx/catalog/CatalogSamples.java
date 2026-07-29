// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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

    /// Creates text and graphic avatars across the semantic color variants.
    ///
    /// @return the avatar example
    static Node avatars() {
        M3Avatar initials = new M3Avatar("AB");

        M3Avatar secondary = new M3Avatar("M");
        secondary.setVariant(M3AvatarVariant.SECONDARY);

        M3Avatar graphic = new M3Avatar(icon(CatalogIcons.AVATAR));
        graphic.setVariant(M3AvatarVariant.TERTIARY);

        M3Avatar surface = new M3Avatar("S");
        surface.setVariant(M3AvatarVariant.SURFACE);
        return row(initials, secondary, graphic, surface);
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

    /// Creates a persistent informational banner with local action feedback.
    ///
    /// @return the banner example
    static Node banners() {
        M3Text result = new M3Text("Choose a banner action", M3TextRole.BODY_MEDIUM);
        M3Banner banner = new M3Banner(
                "A new M3FX preview is available with updated components and theme tokens."
        );
        banner.setIcon(icon(CatalogIcons.NOTIFICATIONS));

        M3Button later = new M3Button("Later", M3ButtonVariant.TEXT);
        later.setOnAction(event -> result.setText("Reminder deferred"));
        M3Button review = new M3Button("Review", M3ButtonVariant.TEXT);
        review.setOnAction(event -> result.setText("Release notes opened"));
        banner.getActions().addAll(later, review);
        configureResponsiveWidth(banner, 640.0);
        return column(banner, result);
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
        configureResponsiveWidth(bar, 520.0);
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
        FlowPane buttons = row(
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
        carousel.setMinWidth(0.0);
        carousel.setPrefSize(620.0, 190.0);
        carousel.setMaxWidth(620.0);

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

    /// Creates a complete color picker with a wheel and preset palette.
    ///
    /// @return the color-picker example
    static Node colorPicker() {
        M3ColorPicker picker = new M3ColorPicker(new M3HsbColor(268.0, 0.62, 0.76));
        picker.setShowColorWheel(true);
        picker.getPresets().setAll(
                new M3HsbColor(4.0, 0.72, 0.85),
                new M3HsbColor(38.0, 0.82, 0.94),
                new M3HsbColor(126.0, 0.59, 0.67),
                new M3HsbColor(216.0, 0.68, 0.86),
                new M3HsbColor(268.0, 0.62, 0.76),
                new M3HsbColor(326.0, 0.60, 0.82)
        );
        configureResponsiveWidth(picker, 520.0);
        return picker;
    }

    /// Creates an inline date picker initialized to the current date.
    ///
    /// @return the date picker example
    static Node datePicker() {
        M3DatePicker picker = new M3DatePicker(LocalDate.now());
        configureResponsiveWidth(picker, 420.0);
        picker.setMaxHeight(Region.USE_PREF_SIZE);
        return picker;
    }

    /// Creates an inline date range picker with bounded representative dates.
    ///
    /// @return the date range picker example
    static Node dateRangePicker() {
        LocalDate today = LocalDate.now();
        M3DateRangePicker picker = new M3DateRangePicker(today.plusDays(2), today.plusDays(8));
        picker.setMinDate(today.minusWeeks(1));
        picker.setMaxDate(today.plusMonths(2));
        configureResponsiveWidth(picker, 420.0);
        picker.setMaxHeight(Region.USE_PREF_SIZE);
        return picker;
    }

    /// Creates an inline Material dialog pane with standard actions.
    ///
    /// @return the dialog pane example
    static Node dialog() {
        M3DialogPane pane = new M3DialogPane();
        pane.setHeaderText("Discard draft?");
        pane.setContentText("This action cannot be undone.");

        M3Button cancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button confirm = new M3Button("OK", M3ButtonVariant.TEXT);
        confirm.setDefaultButton(true);
        pane.getActions().addAll(cancel, confirm);
        return pane;
    }

    /// Creates horizontal full-width and inset dividers together with a vertical divider.
    ///
    /// @return the divider example
    static Node dividers() {
        M3Divider full = new M3Divider();
        M3Divider inset = new M3Divider();
        inset.setInsetStart(40.0);
        M3Divider middleInset = new M3Divider();
        middleInset.setInsetStart(40.0);
        middleInset.setInsetEnd(40.0);

        VBox horizontal = new VBox(28.0, full, inset, middleInset);
        horizontal.setPrefWidth(420.0);

        M3Divider vertical = new M3Divider(Orientation.VERTICAL);
        vertical.setPrefHeight(72.0);
        FlowPane verticalExample = row(
                new M3Text("Before", M3TextRole.BODY_MEDIUM),
                vertical,
                new M3Text("After", M3TextRole.BODY_MEDIUM)
        );
        return column(horizontal, verticalExample);
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

    /// Creates a structured form section containing text and boolean inputs.
    ///
    /// @return the form example
    static Node forms() {
        M3TextField displayName = new M3TextField("Avery");
        displayName.setVariant(M3TextInputVariant.OUTLINED);
        M3TextInputLayout displayNameLayout = new M3TextInputLayout(displayName);
        displayNameLayout.setLabelText("Display name");
        displayNameLayout.setSupportingText("Public profile");
        configureResponsiveWidth(displayNameLayout, 360.0);

        M3Switch notifications = new M3Switch("");
        notifications.setSelected(true);

        M3FormSection profile = new M3FormSection(
                "Profile",
                "Aligned labels and controls."
        );
        profile.getContent().addAll(
                new M3FormRow("Name", "Primary profile label", displayNameLayout),
                new M3FormRow("Notifications", "Receive project updates", notifications)
        );

        M3FormPane form = new M3FormPane();
        form.setContentPadding(16.0);
        form.getItems().add(profile);
        configureResponsiveWidth(form, 680.0);
        return form;
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

    /// Creates scalable SVG icons using semantic size and color roles.
    ///
    /// @return the icon example
    static Node icons() {
        return row(
                sampleIcon(CatalogIcons.SEARCH, M3IconSize.SMALL, M3IconVariant.PRIMARY),
                sampleIcon(CatalogIcons.FAVORITE, M3IconSize.MEDIUM, M3IconVariant.SECONDARY),
                sampleIcon(CatalogIcons.NOTIFICATIONS, M3IconSize.LARGE, M3IconVariant.TERTIARY),
                sampleIcon(CatalogIcons.SETTINGS, M3IconSize.EXTRA_LARGE, M3IconVariant.ERROR)
        );
    }

    /// Creates a continuous selectable list with one- and two-line items.
    ///
    /// @return the standard list example
    static Node standardList() {
        return createList(M3ListStyle.STANDARD);
    }

    /// Creates a segmented selectable list with one- and two-line items.
    ///
    /// @return the segmented list example
    static Node segmentedList() {
        return createList(M3ListStyle.SEGMENTED);
    }

    /// Creates standard side-sheet content with local show and hide actions.
    ///
    /// @return the standard side-sheet example
    static Node standardSideSheet() {
        M3SideSheet sheet = new M3SideSheet("Details", sideSheetContent());
        sheet.setPrefSize(360.0, 360.0);
        sheet.setMaxSize(360.0, 360.0);

        M3IconButton close = iconButton(CatalogIcons.CLOSE, "Close details");
        close.setOnAction(event -> sheet.hide());
        sheet.getHeaderActions().add(close);
        sheet.getActions().add(new M3Button("Save", M3ButtonVariant.FILLED));

        M3Button show = new M3Button("Show side sheet", M3ButtonVariant.TONAL);
        show.setOnAction(event -> sheet.show());
        return column(show, sheet);
    }

    /// Creates a modal side sheet coordinated with a dismissible scrim.
    ///
    /// @return the modal side-sheet example
    static Node modalSideSheet() {
        M3SideSheet sheet = new M3SideSheet("Filters", sideSheetContent());
        sheet.setVariant(M3SheetVariant.MODAL);
        sheet.setPrefSize(320.0, 380.0);
        sheet.setMaxSize(320.0, 380.0);

        M3Scrim scrim = new M3Scrim();
        M3Button show = new M3Button("Show modal sheet", M3ButtonVariant.FILLED);
        M3Text backgroundMessage = new M3Text(
                "The modal sheet blocks this content until dismissed.",
                M3TextRole.BODY_MEDIUM
        );
        backgroundMessage.setWrapText(true);
        VBox background = new VBox(
                12.0,
                new M3Text("Catalog content", M3TextRole.TITLE_MEDIUM),
                backgroundMessage,
                show
        );
        background.getStyleClass().add("catalog-side-sheet-preview-content");
        background.setAlignment(Pos.CENTER);
        background.setMaxWidth(240.0);

        Runnable hide = () -> {
            sheet.hide();
            scrim.hide();
        };
        M3IconButton close = iconButton(CatalogIcons.CLOSE, "Close filters");
        close.setOnAction(event -> hide.run());
        sheet.getHeaderActions().add(close);
        M3Button cancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancel.setOnAction(event -> hide.run());
        M3Button apply = new M3Button("Apply", M3ButtonVariant.FILLED);
        apply.setOnAction(event -> hide.run());
        sheet.getActions().addAll(cancel, apply);
        scrim.setOnAction(event -> hide.run());
        show.setOnAction(event -> {
            scrim.show();
            sheet.show();
        });

        StackPane preview = new StackPane(background, scrim, sheet);
        preview.getStyleClass().add("catalog-side-sheet-preview");
        preview.setMinWidth(0.0);
        preview.setPrefSize(640.0, 380.0);
        preview.setMaxSize(640.0, 380.0);
        StackPane.setAlignment(sheet, Pos.CENTER_RIGHT);
        StackPane.setAlignment(background, Pos.CENTER_LEFT);
        StackPane.setMargin(background, new Insets(32.0));
        return preview;
    }

    /// Creates a selectable list using the requested Material containment style.
    ///
    /// @param listStyle the Material list style
    /// @return the configured list
    private static M3ListPane createList(M3ListStyle listStyle) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(listStyle);
        list.setSelectionMode(M3SelectionMode.SINGLE);
        M3ListItem inbox = new M3ListItem("Inbox");
        inbox.setSupportingText("12 unread messages");
        inbox.setLeadingIcon("I");
        inbox.setTrailingSupportingText("12");
        M3ListItem drafts = new M3ListItem("Drafts");
        drafts.setSupportingText("3 saved drafts");
        drafts.setLeadingIcon("D");
        drafts.setTrailingSupportingText("3");
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
        configureResponsiveWidth(bar, 560.0);
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
        configureResponsiveWidth(search, 420.0);
        return column(search, result);
    }

    /// Creates an active docked search view with switchable contained and divided treatments.
    ///
    /// @return the search-view example
    static Node searchView() {
        M3SearchView search = new M3SearchView("Search components");
        configureResponsiveWidth(search, 520.0);
        search.getResults().addAll(
                searchResult("Buttons", "Filled, tonal, outlined, text, and elevated actions"),
                searchResult("Menus", "Menu surfaces, submenus, and selection"),
                searchResult("Navigation", "Bars, rails, drawers, and destinations")
        );

        M3SegmentedButtonGroup styles = new M3SegmentedButtonGroup();
        styles.setSelectionMode(M3SelectionMode.SINGLE);
        M3SegmentedButton contained = new M3SegmentedButton("Contained");
        contained.setSelected(true);
        contained.setOnAction(event -> search.setViewStyle(M3SearchViewStyle.CONTAINED));
        M3SegmentedButton divided = new M3SegmentedButton("Divided");
        divided.setOnAction(event -> search.setViewStyle(M3SearchViewStyle.DIVIDED));
        styles.getItems().addAll(contained, divided);
        styles.setMaxWidth(Region.USE_PREF_SIZE);
        return column(styles, search);
    }

    /// Creates a local modal preview with a dismissible scrim.
    ///
    /// @return the scrim example
    static Node scrims() {
        VBox modalContent = new VBox(
                8.0,
                new M3Text("Background content", M3TextRole.TITLE_MEDIUM),
                new M3Text("Activate the scrim to dismiss the modal state.", M3TextRole.BODY_MEDIUM)
        );
        modalContent.setAlignment(Pos.CENTER);

        M3Surface background = new M3Surface();
        background.setVariant(M3SurfaceVariant.CONTAINER);
        background.setContentPadding(24.0);
        background.getContent().add(modalContent);
        background.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        M3Scrim scrim = new M3Scrim();
        scrim.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrim.setOnAction(event -> scrim.hide());

        StackPane preview = new StackPane(background, scrim);
        preview.getStyleClass().add("catalog-scrim-preview");
        preview.setMinWidth(0.0);
        preview.setPrefSize(520.0, 220.0);
        preview.setMaxSize(520.0, 220.0);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(preview.widthProperty());
        clip.heightProperty().bind(preview.heightProperty());
        clip.setArcWidth(56.0);
        clip.setArcHeight(56.0);
        preview.setClip(clip);

        M3Button show = new M3Button("Show scrim", M3ButtonVariant.TONAL);
        show.disableProperty().bind(scrim.shownProperty());
        show.setOnAction(event -> scrim.show());
        return column(show, preview);
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

    /// Creates action and selection settings in a segmented list.
    ///
    /// @return the settings-list example
    static Node settings() {
        M3SettingItem account = new M3SettingItem("Account");
        account.setSupportingText("Profile, security, and linked devices");
        account.setLeading(icon(CatalogIcons.AVATAR));
        account.setTrailing(icon(CatalogIcons.ARROW_FORWARD));

        M3SwitchSettingItem updates = new M3SwitchSettingItem("Automatic updates");
        updates.setSupportingText("Install updates while the device is idle");
        updates.setLeading(icon(CatalogIcons.RESET));
        updates.setSelected(true);

        M3CheckBoxSettingItem diagnostics = new M3CheckBoxSettingItem("Diagnostic reports");
        diagnostics.setSupportingText("Share anonymous reliability information");
        diagnostics.setLeading(icon(CatalogIcons.SETTINGS));

        M3ListPane settings = new M3ListPane();
        settings.setListStyle(M3ListStyle.SEGMENTED);
        settings.setSelectionMode(M3SelectionMode.NONE);
        settings.getItems().addAll(account, updates, diagnostics);
        configureResponsiveWidth(settings, 620.0);
        return settings;
    }

    /// Creates single-value and range sliders.
    ///
    /// @return the slider example
    static Node sliders() {
        M3Slider slider = new M3Slider(0.0, 100.0, 48.0);
        configureResponsiveWidth(slider, 380.0);
        M3RangeSlider range = new M3RangeSlider(0.0, 100.0, 24.0, 76.0);
        configureResponsiveWidth(range, 380.0);
        return column(slider, range);
    }

    /// Creates a local overlay pane and an action that displays a fresh snackbar.
    ///
    /// @return the snackbar example
    static Node snackbars() {
        M3OverlayPane overlayPane = new M3OverlayPane();
        overlayPane.setMinWidth(0.0);
        overlayPane.setPrefSize(420.0, 120.0);
        overlayPane.setMaxWidth(420.0);
        M3Button show = new M3Button("Show snackbar", M3ButtonVariant.FILLED);
        show.setOnAction(event -> {
            M3Snackbar snackbar = new M3Snackbar("Message archived");
            snackbar.setActionText("Undo");
            snackbar.setAction(() -> overlayPane.enqueueSnackbar(new M3Snackbar("Archive restored")));
            overlayPane.showSnackbar(snackbar);
        });
        overlayPane.setContent(column(show));
        return overlayPane;
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

    /// Creates representative surface color and elevation variants.
    ///
    /// @return the surface example
    static Node surfaces() {
        return row(
                sampleSurface("Surface", M3SurfaceVariant.SURFACE, M3SurfaceElevation.LEVEL0),
                sampleSurface("Container", M3SurfaceVariant.CONTAINER, M3SurfaceElevation.LEVEL1),
                sampleSurface("Primary", M3SurfaceVariant.PRIMARY_CONTAINER, M3SurfaceElevation.LEVEL3)
        );
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
        configureResponsiveWidth(bar, 520.0);
        return bar;
    }

    /// Creates filled and outlined text fields with labels and supporting text.
    ///
    /// @return the text field example
    static Node textFields() {
        M3TextField name = new M3TextField("M3FX");
        M3TextInputLayout filled = new M3TextInputLayout(name);
        filled.setLabelText("Project name");
        filled.setSupportingText("Visible to collaborators");

        M3TextField email = new M3TextField("support@example.com");
        email.setVariant(M3TextInputVariant.OUTLINED);
        M3TextInputLayout outlined = new M3TextInputLayout(email);
        outlined.setLabelText("Email");
        outlined.setSupportingText("Used for notifications");
        configureResponsiveWidth(filled, 360.0);
        configureResponsiveWidth(outlined, 360.0);
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
        configureResponsiveWidth(bar, 620.0);
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
    /// @param text    the button label
    /// @param variant the Material button variant
    /// @param result  the text node that receives the action result
    /// @return the configured button
    private static M3Button actionButton(String text, M3ButtonVariant variant, M3Text result) {
        M3Button button = new M3Button(text, variant);
        button.setOnAction(event -> result.setText(text + " activated"));
        return button;
    }

    /// Creates a fixed-size content card for card and carousel examples.
    ///
    /// @param title          the card title
    /// @param supportingText the card supporting text
    /// @param variant        the Material card variant
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

    /// Creates a fixed-size surface sample.
    ///
    /// @param title     the surface label
    /// @param variant   the surface color variant
    /// @param elevation the surface elevation
    /// @return the configured surface
    private static M3Surface sampleSurface(
            String title,
            M3SurfaceVariant variant,
            M3SurfaceElevation elevation
    ) {
        M3Surface surface = new M3Surface();
        surface.setVariant(variant);
        surface.setElevation(elevation);
        surface.setContentPadding(20.0);
        surface.getContent().add(new M3Text(title, M3TextRole.TITLE_MEDIUM));
        surface.setPrefSize(168.0, 104.0);
        surface.setMaxSize(168.0, 104.0);
        return surface;
    }

    /// Creates an SVG icon with semantic size and color roles.
    ///
    /// @param path    the SVG path content
    /// @param size    the semantic icon size
    /// @param variant the semantic icon color
    /// @return the configured icon
    private static M3SVGIcon sampleIcon(String path, M3IconSize size, M3IconVariant variant) {
        M3SVGIcon icon = CatalogIcons.create(path);
        icon.setSize(size);
        icon.setVariant(variant);
        return icon;
    }

    /// Creates the content shared by side-sheet examples.
    ///
    /// @return a fresh side-sheet content node
    private static Node sideSheetContent() {
        M3CheckBox notifications = new M3CheckBox("Notifications");
        notifications.setSelected(true);
        M3CheckBox activity = new M3CheckBox("Recent activity");
        return new VBox(
                12.0,
                new M3Text("Choose which information appears in this panel.", M3TextRole.BODY_MEDIUM),
                notifications,
                activity
        );
    }

    /// Creates a two-line result row for a search view.
    ///
    /// @param title          the result title
    /// @param supportingText the result supporting text
    /// @return the configured result item
    private static M3ListItem searchResult(String title, String supportingText) {
        M3ListItem item = new M3ListItem(title);
        item.setSupportingText(supportingText);
        return item;
    }

    /// Creates an icon button with accessible text.
    ///
    /// @param path           the SVG path content
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

    /// Gives a resizable sample a preferred width while allowing it to contract in compact windows.
    ///
    /// @param <T>            the region type
    /// @param region         the region to configure
    /// @param preferredWidth the preferred and maximum width
    /// @return `region`
    private static <T extends Region> T configureResponsiveWidth(T region, double preferredWidth) {
        region.setMinWidth(0.0);
        region.setPrefWidth(preferredWidth);
        region.setMaxWidth(preferredWidth);
        return region;
    }

    /// Arranges controls in a centered row that wraps when horizontal space is constrained.
    ///
    /// @param children the nodes to arrange
    /// @return the centered wrapping layout
    private static FlowPane row(Node... children) {
        FlowPane row = new FlowPane(SAMPLE_SPACING, SAMPLE_SPACING);
        row.getChildren().addAll(children);
        row.setAlignment(Pos.CENTER);
        row.setMinWidth(0.0);
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
        column.setMinWidth(0.0);
        return column;
    }
}
