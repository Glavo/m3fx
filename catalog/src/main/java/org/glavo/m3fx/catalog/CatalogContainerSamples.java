// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3DropZone;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3OverscrollInputMode;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3SurfaceElevation;
import org.glavo.m3fx.controls.M3SurfaceVariant;
import org.glavo.m3fx.controls.M3StretchOverscrollEffect;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TreeView;
import org.glavo.m3fx.controls.M3TreeViewSize;
import org.glavo.m3fx.controls.M3TreeViewStyle;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3Breakpoint;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Creates focused samples for structural, surface, and adaptive-layout Catalog entries.
@NotNullByDefault
final class CatalogContainerSamples {
    /// Prevents instantiation of this factory class.
    private CatalogContainerSamples() {
    }

    /// Creates an adaptive scaffold preview for one breakpoint and pane topology.
    ///
    /// @param breakpoint the breakpoint used by the preview
    /// @param paneLayout the requested pane topology
    /// @param navigationLayout the requested navigation presentation
    /// @return the configured adaptive scaffold
    static Node adaptiveScaffold(
            M3Breakpoint breakpoint,
            M3PaneLayout paneLayout,
            M3NavigationLayout navigationLayout
    ) {
        M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
        scaffold.setBreakpointOverride(breakpoint);
        scaffold.setPaneLayout(paneLayout);
        scaffold.setNavigationLayout(navigationLayout);
        scaffold.setSplitPosition(0.36);
        scaffold.setTopBar(new M3TopAppBar("Adaptive layout"));
        scaffold.setNavigationBar(navigationBar());
        scaffold.setNavigationRail(navigationRail());
        scaffold.setLeadingPane(pane("List", M3SurfaceVariant.CONTAINER_LOW));
        scaffold.setMainPane(pane("Detail", M3SurfaceVariant.SURFACE));
        scaffold.setTrailingPane(pane("Supporting", M3SurfaceVariant.CONTAINER_HIGH));
        scaffold.setPrefSize(720.0, 360.0);
        scaffold.setMinSize(0.0, 320.0);
        scaffold.setMaxWidth(Double.MAX_VALUE);
        return scaffold;
    }

    /// Creates one semantic avatar variant using text or graphic content.
    ///
    /// @param variant the avatar color variant
    /// @param graphic whether to use a vector graphic instead of initials
    /// @return the configured avatar
    static Node avatar(M3AvatarVariant variant, boolean graphic) {
        M3Avatar avatar = graphic
                ? new M3Avatar(CatalogSamples.icon(CatalogIcons.AVATAR))
                : new M3Avatar("AB");
        avatar.setVariant(variant);
        return avatar;
    }

    /// Creates an avatar in the leading slot of a list item.
    ///
    /// @return the list-row avatar example
    static Node avatarListItem() {
        M3ListItem item = new M3ListItem("Account");
        item.setSupportingText("Avatar as leading content");
        item.setLeading(new M3Avatar("A"));
        return responsiveList(item);
    }

    /// Creates a dot or text badge.
    ///
    /// @param text the badge text, or an empty string for a dot badge
    /// @return the configured badge
    static Node badge(String text) {
        return text.isEmpty() ? new M3Badge() : new M3Badge(text);
    }

    /// Creates a badge attached to a tonal action.
    ///
    /// @return the attached badge example
    static Node attachedBadge() {
        M3Button button = new M3Button("Inbox", M3ButtonVariant.TONAL);
        return new M3BadgedBox(button, new M3Badge("9"));
    }

    /// Creates a persistent banner with optional icon and actions.
    ///
    /// @param iconVisible whether the leading icon is shown
    /// @param actionCount the number of text actions from zero through two
    /// @param narrow whether the banner uses a narrow preferred width
    /// @return the configured banner
    static Node banner(boolean iconVisible, int actionCount, boolean narrow) {
        M3Banner banner = new M3Banner(
                narrow
                        ? "A narrow banner wraps longer text while keeping actions reachable."
                        : "M3FX can keep contextual information visible without interrupting the current task."
        );
        if (iconVisible) {
            banner.setIcon(CatalogSamples.icon(CatalogIcons.NOTIFICATIONS));
        }
        if (actionCount >= 1) {
            banner.getActions().add(new M3Button("Review", M3ButtonVariant.TEXT));
        }
        if (actionCount >= 2) {
            banner.getActions().add(new M3Button("Dismiss", M3ButtonVariant.TEXT));
        }
        CatalogSamples.configureResponsiveWidth(banner, narrow ? 420.0 : 680.0);
        return banner;
    }

    /// Creates a bottom app bar with one floating-action alignment.
    ///
    /// @param alignment the floating action placement
    /// @return the configured bottom app bar
    static Node bottomAppBar(M3BottomAppBarFloatingActionAlignment alignment) {
        M3BottomAppBar bar = new M3BottomAppBar();
        bar.setFloatingActionAlignment(alignment);
        bar.setFloatingAction(new M3FloatingActionButton(CatalogSamples.icon(CatalogIcons.ADD)));
        bar.getActions().addAll(
                CatalogSamples.iconButton(CatalogIcons.SEARCH, "Search"),
                CatalogSamples.iconButton(CatalogIcons.FAVORITE, "Favorite")
        );
        return CatalogSamples.configureResponsiveWidth(bar, 620.0);
    }

    /// Creates a standard or modal bottom sheet.
    ///
    /// @param variant the sheet presentation variant
    /// @param dragHandleVisible whether the drag handle is visible
    /// @return the configured bottom sheet
    static Node bottomSheet(M3SheetVariant variant, boolean dragHandleVisible) {
        M3BottomSheet sheet = new M3BottomSheet(
                variant == M3SheetVariant.MODAL ? "Filters" : "Now playing",
                sheetContent()
        );
        sheet.setVariant(variant);
        sheet.setDragHandleVisible(dragHandleVisible);
        sheet.getActions().add(CatalogSamples.iconButton(CatalogIcons.CLOSE, "Close"));
        return CatalogSamples.configureResponsiveWidth(sheet, 520.0);
    }

    /// Creates one card variant in an ordinary, dragged, disabled, or locally colored state.
    ///
    /// @param variant the card separation treatment
    /// @param dragged whether the card is in its dragged state
    /// @param disabled whether the card is disabled
    /// @param localColor whether a local container color is applied
    /// @return the configured card
    static Node card(
            M3CardVariant variant,
            boolean dragged,
            boolean disabled,
            boolean localColor
    ) {
        VBox content = new VBox(
                4.0,
                new M3Text(cardVariantLabel(variant), M3TextRole.TITLE_MEDIUM),
                new M3Text("Grouped supporting content", M3TextRole.BODY_MEDIUM)
        );
        M3Card card = new M3Card(content, variant);
        card.setContentPadding(20.0);
        card.setPrefSize(280.0, 152.0);
        card.setDragged(dragged);
        card.setDisable(disabled);
        if (localColor) {
            card.setContainerColor(Color.web("#FFF3E0"));
        }
        return card;
    }

    /// Creates a passive card with independent nested actions.
    ///
    /// @return the passive card example
    static Node passiveCard() {
        M3Button details = new M3Button("Details", M3ButtonVariant.TEXT);
        M3Button open = new M3Button("Open", M3ButtonVariant.TONAL);
        VBox content = new VBox(
                12.0,
                new M3Text("Project preview", M3TextRole.TITLE_MEDIUM),
                new M3Text("Nested controls remain separate action targets.", M3TextRole.BODY_MEDIUM),
                CatalogSamples.row(details, open)
        );
        M3Card card = new M3Card(content, M3CardVariant.FILLED);
        card.setContentPadding(20.0);
        card.setPrefWidth(360.0);
        return card;
    }

    /// Creates one carousel layout with representative authored items.
    ///
    /// @param layout the carousel layout strategy
    /// @return the configured carousel
    static Node carousel(M3CarouselLayout layout) {
        M3Carousel carousel = new M3Carousel();
        carousel.setCarouselLayout(layout);
        for (int index = 1; index <= 6; index++) {
            M3Card card = new M3Card(
                    new M3Text("Item " + index, M3TextRole.TITLE_MEDIUM),
                    index % 2 == 0 ? M3CardVariant.ELEVATED : M3CardVariant.FILLED
            );
            double width = layout == M3CarouselLayout.UNCONTAINED_MULTI_ASPECT_RATIO
                    ? 150.0 + index * 22.0
                    : layout == M3CarouselLayout.UNCONTAINED ? 180.0 : 260.0;
            card.setPrefSize(width, layout == M3CarouselLayout.FULL_SCREEN ? 320.0 : 152.0);
            carousel.getItems().add(card);
        }
        carousel.selectIndex(layout == M3CarouselLayout.CENTER_ALIGNED_HERO ? 2 : 0);
        carousel.setPrefHeight(layout == M3CarouselLayout.FULL_SCREEN ? 320.0 : 180.0);
        carousel.setMinWidth(0.0);
        carousel.setMaxWidth(Double.MAX_VALUE);
        return carousel;
    }

    /// Creates a basic, settings, or scrolling inline dialog pane.
    ///
    /// @param settings whether to include form controls
    /// @param scrolling whether to include a scrollable body
    /// @return the configured dialog pane
    static Node dialog(boolean settings, boolean scrolling) {
        M3DialogPane pane = new M3DialogPane();
        pane.setHeaderText(settings ? "Project settings" : scrolling ? "Release notes" : "Discard draft?");
        pane.setMinWidth(0.0);
        pane.setMaxWidth(560.0);
        if (settings) {
            M3TextField name = new M3TextField("M3FX");
            name.setVariant(M3TextInputVariant.OUTLINED);
            M3TextInputLayout layout = new M3TextInputLayout(name);
            layout.setLabelText("Project name");
            layout.setSupportingText("Shown in generated artifacts");
            pane.setContent(new VBox(12.0, layout, new M3Switch("Notify contributors"), new M3CheckBox("Remember")));
        } else if (scrolling) {
            VBox body = new VBox(8.0);
            for (int index = 1; index <= 8; index++) {
                M3Text paragraph = new M3Text(
                        "Release note " + index + " describes one observable component change.",
                        M3TextRole.BODY_MEDIUM
                );
                paragraph.setWrapText(true);
                body.getChildren().add(paragraph);
            }
            M3ScrollPane scrollPane = new M3ScrollPane(body);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(150.0);
            pane.setContent(scrollPane);
        } else {
            pane.setContentText("This action cannot be undone.");
        }
        M3Button cancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button confirm = new M3Button(settings ? "Apply" : "OK", M3ButtonVariant.TEXT);
        confirm.setDefaultButton(true);
        pane.getActions().addAll(cancel, confirm);
        return pane;
    }

    /// Creates one horizontal divider inset configuration.
    ///
    /// @param insetStart the leading inset
    /// @param insetEnd the trailing inset
    /// @return the configured divider
    static Node horizontalDivider(double insetStart, double insetEnd) {
        M3Divider divider = new M3Divider();
        divider.setInsetStart(insetStart);
        divider.setInsetEnd(insetEnd);
        divider.setPrefWidth(360.0);
        return divider;
    }

    /// Creates a vertical divider between two labels.
    ///
    /// @return the vertical divider example
    static Node verticalDivider() {
        M3Divider divider = new M3Divider(Orientation.VERTICAL);
        divider.setPrefHeight(72.0);
        return CatalogSamples.row(
                new M3Text("Before", M3TextRole.BODY_MEDIUM),
                divider,
                new M3Text("After", M3TextRole.BODY_MEDIUM)
        );
    }

    /// Creates an interactive file drop zone in an empty, filled, or disabled state.
    ///
    /// @param filled whether the sample begins with imported content
    /// @param disabled whether drag and keyboard interaction is disabled
    /// @return the configured drop zone
    static Node dropZone(boolean filled, boolean disabled) {
        M3Text title = new M3Text(
                filled ? "Launcher profile imported" : "Drop a launcher profile here",
                M3TextRole.TITLE_MEDIUM
        );
        M3Text supporting = new M3Text(
                filled ? "profile.json is ready to use" : "JSON files up to 10 MB",
                M3TextRole.BODY_MEDIUM
        );
        M3Button browse = new M3Button("Choose file", M3ButtonVariant.TONAL);
        browse.setOnAction(event -> supporting.setText("A file chooser would open in the host application"));

        VBox message = new VBox(10.0, CatalogSamples.icon(CatalogIcons.DROP_ZONE), title, supporting, browse);
        message.setAlignment(Pos.CENTER);

        M3DropZone dropZone = new M3DropZone(message);
        dropZone.setAcceptancePredicate(event -> event.getDragboard().hasFiles());
        dropZone.setFilled(filled);
        dropZone.setDisable(disabled);
        dropZone.setPrefWidth(480.0);
        dropZone.setMaxWidth(480.0);
        dropZone.setOnDragDropped(event -> {
            int fileCount = event.getDragboard().getFiles().size();
            boolean accepted = fileCount > 0;
            if (accepted) {
                title.setText(fileCount == 1 ? "1 file imported" : fileCount + " files imported");
                supporting.setText("Drop completed successfully");
                dropZone.setFilled(true);
            }
            event.setDropCompleted(accepted);
        });
        return dropZone;
    }

    /// Creates a list focused on one row-line count and leading slot.
    ///
    /// @param lineCount the number of text lines from one through three
    /// @param slotSize the leading slot size
    /// @param disabled whether the row is disabled
    /// @return the configured list
    static Node listItem(int lineCount, M3ListItemSlotSize slotSize, boolean disabled) {
        M3ListItem item = new M3ListItem(lineCount + "-line item");
        if (lineCount >= 2) {
            item.setSupportingText("Supporting text");
        }
        if (lineCount >= 3) {
            item.setOverlineText("Overline");
        }
        if (slotSize == M3ListItemSlotSize.AVATAR) {
            item.setLeadingAvatar("A");
        } else if (slotSize == M3ListItemSlotSize.THUMBNAIL
                || slotSize == M3ListItemSlotSize.WIDE_THUMBNAIL) {
            item.setLeadingMedia(thumbnail(), slotSize);
        } else {
            item.setLeading(CatalogSamples.icon(CatalogIcons.LIST));
        }
        item.setTrailingSupportingText("Now");
        item.setDisable(disabled);
        return responsiveList(item);
    }

    /// Creates a multi-row standard or segmented list.
    ///
    /// @param style the list containment style
    /// @param sectioned whether a section header is included
    /// @param selected whether one item begins selected
    /// @return the configured list
    static Node list(M3ListStyle style, boolean sectioned, boolean selected) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(style);
        list.setSelectionMode(M3SelectionMode.SINGLE);
        if (sectioned) {
            list.getItems().add(new M3ListSectionHeader("Recent"));
        }
        M3ListItem inbox = listRow("Inbox", "12 unread", CatalogIcons.NOTIFICATIONS);
        M3ListItem drafts = listRow("Drafts", "3 items", CatalogIcons.EDIT);
        M3ListItem archive = listRow("Archive", "Updated yesterday", CatalogIcons.BOTTOM_SHEET);
        list.getItems().addAll(inbox, drafts, archive);
        if (selected) {
            list.select(drafts);
        }
        return CatalogSamples.configureResponsiveWidth(list, 520.0);
    }

    /// Creates an expandable tree-view sample.
    ///
    /// @param size the tree row size
    /// @param style the row containment style
    /// @param graphics whether tree items include graphics
    /// @param multipleSelection whether multiple selection is enabled and demonstrated
    /// @return the configured tree view
    static Node treeView(
            M3TreeViewSize size,
            M3TreeViewStyle style,
            boolean graphics,
            boolean multipleSelection
    ) {
        TreeItem<String> workspace = treeItem("Workspace", graphics, CatalogIcons.SURFACE);
        TreeItem<String> applications = treeItem("Applications", graphics, CatalogIcons.ADAPTIVE);
        applications.getChildren().addAll(List.of(
                treeItem("Catalog", graphics, CatalogIcons.LIST),
                treeItem("Demo", graphics, CatalogIcons.TOUCH_APP)
        ));
        workspace.getChildren().addAll(List.of(
                applications,
                treeItem("Libraries", graphics, CatalogIcons.CARD),
                treeItem("Documentation", graphics, CatalogIcons.TYPOGRAPHY)
        ));
        workspace.setExpanded(true);
        applications.setExpanded(true);

        M3TreeView<String> treeView = new M3TreeView<>(workspace);
        treeView.setSize(size);
        treeView.setTreeStyle(style);
        treeView.setPrefHeight(280.0);
        if (multipleSelection) {
            treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            treeView.getSelectionModel().selectIndices(1, 3);
        } else {
            treeView.getSelectionModel().select(2);
        }
        return CatalogSamples.configureResponsiveWidth(treeView, 480.0);
    }

    /// Creates a tree item with an optional catalog graphic.
    ///
    /// @param text the item text
    /// @param graphic whether to create a graphic
    /// @param iconPath the catalog icon path used when graphics are enabled
    /// @return the tree item
    private static TreeItem<String> treeItem(String text, boolean graphic, String iconPath) {
        return new TreeItem<>(text, graphic ? CatalogSamples.icon(iconPath) : null);
    }

    /// Creates a plain or actionable scrim inside a bounded preview.
    ///
    /// @param actionable whether clicking the shown scrim dismisses it
    /// @return the scrim preview
    static Node scrim(boolean actionable) {
        StackPane content = new StackPane(new M3Text("Background content", M3TextRole.TITLE_MEDIUM));
        content.setPrefSize(420.0, 180.0);
        M3Scrim scrim = new M3Scrim();
        scrim.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrim.setShown(true);
        if (actionable) {
            scrim.setOnAction(event -> scrim.setShown(false));
        }
        StackPane preview = new StackPane(content, scrim);
        preview.setPrefSize(420.0, 180.0);
        return CatalogSamples.configureResponsiveWidth(preview, 420.0);
    }

    /// Creates a standard, modal, or detached side sheet.
    ///
    /// @param variant the sheet presentation variant
    /// @param detached whether the sheet uses detached geometry
    /// @return the configured side sheet
    static Node sideSheet(M3SheetVariant variant, boolean detached) {
        M3SideSheet sheet = new M3SideSheet(
                detached ? "Detached" : variant == M3SheetVariant.MODAL ? "Filters" : "Details",
                sheetContent()
        );
        sheet.setVariant(variant);
        sheet.setDetached(detached);
        sheet.getHeaderActions().add(CatalogSamples.iconButton(CatalogIcons.CLOSE, "Close"));
        sheet.getActions().addAll(
                new M3Button("Cancel", M3ButtonVariant.TEXT),
                new M3Button("Apply", M3ButtonVariant.FILLED)
        );
        return sheet;
    }

    /// Creates an interactive scroll-pane specimen with optional two-axis content and stretch configuration.
    ///
    /// @param bidirectional whether content exceeds the viewport on both axes
    /// @param overscrollInputMode the accepted overscroll inputs, or `null` to remove the effect
    /// @param pronouncedStretch whether the stretch uses a stronger custom configuration
    /// @return the configured scroll pane
    static Node scrollPane(
            boolean bidirectional,
            @Nullable M3OverscrollInputMode overscrollInputMode,
            boolean pronouncedStretch
    ) {
        String edgeGuidance;
        if (overscrollInputMode == null) {
            edgeGuidance = "Edge input remains bounded without a stretch effect";
        } else {
            edgeGuidance = switch (overscrollInputMode) {
                case DIRECT -> "Use direct touch input and continue past an edge";
                case CONTINUOUS -> "Use touch or a continuous precision gesture past an edge";
                case ALL -> "Mouse-wheel input also stretches after reaching an edge";
            };
        }
        M3ListPane list = new M3ListPane();
        list.setSelectionMode(M3SelectionMode.NONE);
        for (int index = 1; index <= 12; index++) {
            M3ListItem item = new M3ListItem("Scrollable item " + index);
            item.setSupportingText(index == 1
                    ? edgeGuidance
                    : "Bounded content remains aligned with the native viewport");
            item.setLeading(CatalogSamples.icon(index % 2 == 0 ? CatalogIcons.FAVORITE : CatalogIcons.TOUCH_APP));
            list.getItems().add(item);
        }
        if (bidirectional) {
            list.setMinWidth(640.0);
            list.setPrefWidth(640.0);
        }

        M3ScrollPane scrollPane = new M3ScrollPane(list);
        scrollPane.setFitToWidth(!bidirectional);
        scrollPane.setPannable(true);
        scrollPane.setPrefSize(420.0, 280.0);
        scrollPane.setMaxSize(420.0, 280.0);
        if (overscrollInputMode == null) {
            scrollPane.setOverscrollEffect(null);
        } else {
            scrollPane.setOverscrollInputMode(overscrollInputMode);
            if (pronouncedStretch) {
                M3StretchOverscrollEffect effect = new M3StretchOverscrollEffect();
                effect.setMaximumStretch(0.16);
                effect.setResistance(0.38);
                scrollPane.setOverscrollEffect(effect);
            }
        }
        return scrollPane;
    }

    /// Creates one surface color and elevation combination.
    ///
    /// @param variant the surface color role
    /// @param elevation the surface elevation level
    /// @param localColor whether a local container color is applied
    /// @return the configured surface
    static Node surface(
            M3SurfaceVariant variant,
            M3SurfaceElevation elevation,
            boolean localColor
    ) {
        M3Surface surface = new M3Surface();
        surface.setVariant(variant);
        surface.setElevation(elevation);
        surface.getContent().add(new M3Text(surfaceVariantLabel(variant), M3TextRole.TITLE_MEDIUM));
        surface.setPrefSize(220.0, 112.0);
        if (localColor) {
            surface.setContainerColor(Color.web("#E8F5E9"));
        }
        return surface;
    }

    /// Creates one adaptive scaffold content pane.
    ///
    /// @param text the pane label
    /// @param variant the pane surface role
    /// @return the configured pane
    private static M3Surface pane(String text, M3SurfaceVariant variant) {
        M3Surface surface = new M3Surface();
        surface.setVariant(variant);
        surface.getContent().add(new M3Text(text, M3TextRole.TITLE_MEDIUM));
        surface.setMinSize(0.0, 0.0);
        surface.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return surface;
    }

    /// Creates the compact navigation bar used by adaptive previews.
    ///
    /// @return the configured navigation bar
    private static M3NavigationBar navigationBar() {
        M3NavigationBar bar = new M3NavigationBar();
        bar.getItems().addAll(
                CatalogSamples.navigationItem("Home", CatalogIcons.HOME),
                CatalogSamples.navigationItem("Search", CatalogIcons.SEARCH),
                CatalogSamples.navigationItem("Settings", CatalogIcons.SETTINGS)
        );
        bar.selectIndex(0);
        return bar;
    }

    /// Creates the navigation rail used by adaptive previews.
    ///
    /// @return the configured navigation rail
    private static M3NavigationRail navigationRail() {
        M3NavigationRail rail = new M3NavigationRail();
        rail.getItems().addAll(
                CatalogSamples.navigationItem("Home", CatalogIcons.HOME),
                CatalogSamples.navigationItem("Search", CatalogIcons.SEARCH),
                CatalogSamples.navigationItem("Settings", CatalogIcons.SETTINGS)
        );
        rail.selectIndex(0);
        return rail;
    }

    /// Creates simple sheet body content.
    ///
    /// @return the sheet body
    private static Node sheetContent() {
        return new VBox(
                10.0,
                new M3Text("Supplementary content", M3TextRole.TITLE_MEDIUM),
                new M3Text("The sheet remains responsive within this route.", M3TextRole.BODY_MEDIUM)
        );
    }

    /// Wraps one item in a responsive list pane.
    ///
    /// @param item the row to show
    /// @return the containing list
    private static M3ListPane responsiveList(M3ListItem item) {
        M3ListPane list = new M3ListPane();
        list.getItems().add(item);
        return CatalogSamples.configureResponsiveWidth(list, 520.0);
    }

    /// Creates a representative list row.
    ///
    /// @param headline the row headline
    /// @param supporting the supporting text
    /// @param iconPath the leading icon path
    /// @return the configured row
    private static M3ListItem listRow(String headline, String supporting, String iconPath) {
        M3ListItem item = new M3ListItem(headline);
        item.setSupportingText(supporting);
        item.setLeading(CatalogSamples.icon(iconPath));
        return item;
    }

    /// Creates a square media placeholder for list rows.
    ///
    /// @return the thumbnail node
    private static Node thumbnail() {
        Rectangle rectangle = new Rectangle(64.0, 56.0, Color.web("#D8E2FF"));
        rectangle.setArcWidth(12.0);
        rectangle.setArcHeight(12.0);
        return rectangle;
    }

    /// Returns a display label for one card variant.
    ///
    /// @param variant the card variant
    /// @return the display label
    private static String cardVariantLabel(M3CardVariant variant) {
        return switch (variant) {
            case FILLED -> "Filled card";
            case OUTLINED -> "Outlined card";
            case ELEVATED -> "Elevated card";
        };
    }

    /// Returns a display label for one surface variant.
    ///
    /// @param variant the surface variant
    /// @return the display label
    private static String surfaceVariantLabel(M3SurfaceVariant variant) {
        return switch (variant) {
            case SURFACE -> "Surface";
            case CONTAINER_LOWEST -> "Lowest container";
            case CONTAINER_LOW -> "Low container";
            case CONTAINER -> "Container";
            case CONTAINER_HIGH -> "High container";
            case CONTAINER_HIGHEST -> "Highest container";
            case PRIMARY_CONTAINER -> "Primary container";
            case SECONDARY_CONTAINER -> "Secondary container";
            case TERTIARY_CONTAINER -> "Tertiary container";
        };
    }
}
