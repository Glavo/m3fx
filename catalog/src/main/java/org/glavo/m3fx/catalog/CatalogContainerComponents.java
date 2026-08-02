// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3OverscrollInputMode;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3SurfaceElevation;
import org.glavo.m3fx.controls.M3SurfaceVariant;
import org.glavo.m3fx.layout.M3Breakpoint;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Supplies container and structural entries for the Catalog registry.
@NotNullByDefault
final class CatalogContainerComponents {
    /// Prevents utility class instantiation.
    private CatalogContainerComponents() {
    }

    /// Creates the container component descriptors.
    ///
    /// @return the immutable descriptor list
    static @Unmodifiable List<CatalogComponent> create() {
        return List.of(
                CatalogComponents.layoutComponent(
                        "Adaptive",
                        "Adaptive scaffolds coordinate pane topology and navigation as the available width changes.",
                        CatalogIcons.ADAPTIVE,
                        "https://m3.material.io/foundations/layout/applying-layout/window-size-classes",
                        "M3AdaptiveScaffold",
                        CatalogComponents.example(
                                "Compact single pane",
                                "A compact scaffold with one active pane and bottom navigation.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.COMPACT,
                                        M3PaneLayout.ADAPTIVE,
                                        M3NavigationLayout.ADAPTIVE
                                )
                        ),
                        CatalogComponents.example(
                                "Medium single pane",
                                "A medium-width scaffold retaining one active content pane.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.MEDIUM,
                                        M3PaneLayout.ADAPTIVE,
                                        M3NavigationLayout.ADAPTIVE
                                )
                        ),
                        CatalogComponents.example(
                                "Expanded two pane",
                                "An expanded scaffold resolving its adaptive topology to two panes.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.EXPANDED,
                                        M3PaneLayout.ADAPTIVE,
                                        M3NavigationLayout.ADAPTIVE
                                )
                        ),
                        CatalogComponents.example(
                                "Fixed leading pane",
                                "A fixed-width leading pane paired with flexible primary content.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.EXPANDED,
                                        M3PaneLayout.FIXED_LEADING,
                                        M3NavigationLayout.RAIL
                                )
                        ),
                        CatalogComponents.example(
                                "Fixed trailing pane",
                                "Flexible primary content paired with a fixed supporting pane.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.LARGE,
                                        M3PaneLayout.FIXED_TRAILING,
                                        M3NavigationLayout.RAIL
                                )
                        ),
                        CatalogComponents.example(
                                "Three pane",
                                "An extra-large scaffold with leading, primary, and supporting panes.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.EXTRA_LARGE,
                                        M3PaneLayout.THREE_PANE,
                                        M3NavigationLayout.RAIL
                                )
                        ),
                        CatalogComponents.example(
                                "Navigation suite",
                                "The same destinations presented through the breakpoint-appropriate bar or rail.",
                                false,
                                () -> CatalogContainerSamples.adaptiveScaffold(
                                        M3Breakpoint.EXPANDED,
                                        M3PaneLayout.SINGLE,
                                        M3NavigationLayout.ADAPTIVE
                                )
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Avatars",
                        "Avatars provide compact text or graphic representations of people, entities, and objects.",
                        CatalogIcons.AVATAR,
                        "https://m3.material.io/styles/color/roles",
                        "M3Avatar",
                        CatalogComponents.example(
                                "Primary avatar",
                                "Initials using the primary avatar color role.",
                                false,
                                () -> CatalogContainerSamples.avatar(M3AvatarVariant.PRIMARY, false)
                        ),
                        CatalogComponents.example(
                                "Secondary avatar",
                                "Initials using the secondary avatar color role.",
                                false,
                                () -> CatalogContainerSamples.avatar(M3AvatarVariant.SECONDARY, false)
                        ),
                        CatalogComponents.example(
                                "Tertiary avatar",
                                "Initials using the tertiary avatar color role.",
                                false,
                                () -> CatalogContainerSamples.avatar(M3AvatarVariant.TERTIARY, false)
                        ),
                        CatalogComponents.example(
                                "Surface avatar",
                                "Initials using the neutral surface avatar treatment.",
                                false,
                                () -> CatalogContainerSamples.avatar(M3AvatarVariant.SURFACE, false)
                        ),
                        CatalogComponents.example(
                                "Graphic avatar",
                                "A vector graphic clipped into a tertiary avatar.",
                                false,
                                () -> CatalogContainerSamples.avatar(M3AvatarVariant.TERTIARY, true)
                        ),
                        CatalogComponents.example(
                                "Avatar in list",
                                "An avatar placed in a list item's leading slot.",
                                false,
                                CatalogContainerSamples::avatarListItem
                        )
                ),
                CatalogComponents.component(
                        "Badges",
                        "Badges show notifications, counts, or compact status information attached to another element.",
                        CatalogIcons.NOTIFICATIONS,
                        "badges",
                        "M3Badge",
                        CatalogComponents.example(
                                "Dot badge",
                                "A compact badge without text.",
                                false,
                                () -> CatalogContainerSamples.badge("")
                        ),
                        CatalogComponents.example(
                                "Single-digit badge",
                                "A compact count badge containing one digit.",
                                false,
                                () -> CatalogContainerSamples.badge("7")
                        ),
                        CatalogComponents.example(
                                "Large-count badge",
                                "A badge whose container expands for a longer count.",
                                false,
                                () -> CatalogContainerSamples.badge("1234")
                        ),
                        CatalogComponents.example(
                                "Attached badge",
                                "A count badge attached to an actionable control.",
                                false,
                                CatalogContainerSamples::attachedBadge
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Banners",
                        "Banners keep contextual messages and related actions visible within the current layout.",
                        CatalogIcons.BANNER,
                        "https://m3.material.io/components",
                        "M3Banner",
                        CatalogComponents.example(
                                "Banner with actions",
                                "A persistent message with an icon and two actions.",
                                false,
                                () -> CatalogContainerSamples.banner(true, 2, false)
                        ),
                        CatalogComponents.example(
                                "Banner without icon",
                                "A contextual message whose surrounding content supplies sufficient context.",
                                false,
                                () -> CatalogContainerSamples.banner(false, 1, false)
                        ),
                        CatalogComponents.example(
                                "Passive banner",
                                "Persistent information without an action request.",
                                false,
                                () -> CatalogContainerSamples.banner(false, 0, false)
                        ),
                        CatalogComponents.example(
                                "Responsive banner",
                                "A narrow banner that wraps its message while preserving actions.",
                                false,
                                () -> CatalogContainerSamples.banner(true, 2, true)
                        )
                ),
                CatalogComponents.component(
                        "Bottom app bars",
                        "Bottom app bars place navigation and key actions at the lower edge of a window.",
                        CatalogIcons.BOTTOM_APP_BAR,
                        "bottom-app-bar",
                        "M3BottomAppBar",
                        CatalogComponents.example(
                                "End-aligned FAB",
                                "A bottom app bar with its floating action at the trailing edge.",
                                false,
                                () -> CatalogContainerSamples.bottomAppBar(
                                        M3BottomAppBarFloatingActionAlignment.END
                                )
                        ),
                        CatalogComponents.example(
                                "Center-aligned FAB",
                                "A bottom app bar with a centered floating action.",
                                false,
                                () -> CatalogContainerSamples.bottomAppBar(
                                        M3BottomAppBarFloatingActionAlignment.CENTER
                                )
                        ),
                        CatalogComponents.example(
                                "Start-aligned FAB",
                                "A bottom app bar with its floating action at the leading edge.",
                                false,
                                () -> CatalogContainerSamples.bottomAppBar(
                                        M3BottomAppBarFloatingActionAlignment.START
                                )
                        )
                ),
                CatalogComponents.component(
                        "Bottom sheets",
                        "Bottom sheets contain supplementary content anchored to the bottom edge.",
                        CatalogIcons.BOTTOM_SHEET,
                        "bottom-sheets",
                        "M3BottomSheet",
                        CatalogComponents.example(
                                "Standard bottom sheet",
                                "A persistent bottom sheet with a visible drag handle.",
                                false,
                                () -> CatalogContainerSamples.bottomSheet(M3SheetVariant.STANDARD, true)
                        ),
                        CatalogComponents.example(
                                "Standard sheet without handle",
                                "A persistent bottom sheet whose handle is omitted.",
                                false,
                                () -> CatalogContainerSamples.bottomSheet(M3SheetVariant.STANDARD, false)
                        ),
                        CatalogComponents.example(
                                "Modal bottom sheet",
                                "A modal bottom sheet with actions and no drag handle.",
                                false,
                                () -> CatalogContainerSamples.bottomSheet(M3SheetVariant.MODAL, false)
                        )
                ),
                CatalogComponents.component(
                        "Cards",
                        "Cards group related information and may optionally act as a single action target.",
                        CatalogIcons.CARD,
                        "cards",
                        "M3Card",
                        CatalogComponents.example(
                                "Filled card",
                                "A card separated from its surroundings by a tonal container.",
                                false,
                                () -> CatalogContainerSamples.card(
                                        M3CardVariant.FILLED,
                                        false,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Outlined card",
                                "A card separated from its surroundings by a boundary stroke.",
                                false,
                                () -> CatalogContainerSamples.card(
                                        M3CardVariant.OUTLINED,
                                        false,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Elevated card",
                                "A card separated from its surroundings by shadow and tonal elevation.",
                                false,
                                () -> CatalogContainerSamples.card(
                                        M3CardVariant.ELEVATED,
                                        false,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Passive card with actions",
                                "A passive container whose nested buttons remain independent targets.",
                                false,
                                CatalogContainerSamples::passiveCard
                        ),
                        CatalogComponents.example(
                                "Dragged card",
                                "An elevated card using its dragged interaction state.",
                                false,
                                () -> CatalogContainerSamples.card(
                                        M3CardVariant.ELEVATED,
                                        true,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Disabled card",
                                "An outlined card with interaction disabled.",
                                false,
                                () -> CatalogContainerSamples.card(
                                        M3CardVariant.OUTLINED,
                                        false,
                                        true,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Locally colored card",
                                "A filled card with a local container paint override.",
                                false,
                                () -> CatalogContainerSamples.card(
                                        M3CardVariant.FILLED,
                                        false,
                                        false,
                                        true
                                )
                        )
                ),
                CatalogComponents.component(
                        "Carousel",
                        "Carousels present a horizontally browsable sequence with selection and snapping.",
                        CatalogIcons.CAROUSEL,
                        "carousel",
                        "M3Carousel",
                        CatalogComponents.example(
                                "Multi-browse carousel",
                                "Large, medium, and small contained items around the focal item.",
                                false,
                                () -> CatalogContainerSamples.carousel(M3CarouselLayout.MULTI_BROWSE)
                        ),
                        CatalogComponents.example(
                                "Hero carousel",
                                "One large focal item with a trailing preview.",
                                false,
                                () -> CatalogContainerSamples.carousel(M3CarouselLayout.HERO)
                        ),
                        CatalogComponents.example(
                                "Center-aligned hero carousel",
                                "A centered focal item between leading and trailing previews.",
                                false,
                                () -> CatalogContainerSamples.carousel(M3CarouselLayout.CENTER_ALIGNED_HERO)
                        ),
                        CatalogComponents.example(
                                "Uncontained carousel",
                                "A horizontal sequence that preserves authored item widths.",
                                false,
                                () -> CatalogContainerSamples.carousel(M3CarouselLayout.UNCONTAINED)
                        ),
                        CatalogComponents.example(
                                "Multi-aspect carousel",
                                "Uncontained items with independently authored aspect ratios.",
                                false,
                                () -> CatalogContainerSamples.carousel(
                                        M3CarouselLayout.UNCONTAINED_MULTI_ASPECT_RATIO
                                )
                        ),
                        CatalogComponents.example(
                                "Full-screen carousel",
                                "A vertical feed that snaps between viewport-sized items.",
                                false,
                                () -> CatalogContainerSamples.carousel(M3CarouselLayout.FULL_SCREEN)
                        )
                ),
                CatalogComponents.component(
                        "Dialogs",
                        "Dialogs interrupt a workflow to request a decision or present focused information.",
                        CatalogIcons.DIALOG,
                        "dialogs",
                        "M3DialogPane",
                        CatalogComponents.example(
                                "Basic dialog",
                                "An inline preview of dialog content and actions.",
                                false,
                                () -> CatalogContainerSamples.dialog(false, false)
                        ),
                        CatalogComponents.example(
                                "Settings dialog",
                                "An inline dialog pane containing form controls.",
                                false,
                                () -> CatalogContainerSamples.dialog(true, false)
                        ),
                        CatalogComponents.example(
                                "Scrollable dialog",
                                "A compact dialog pane whose long body scrolls independently.",
                                false,
                                () -> CatalogContainerSamples.dialog(false, true)
                        )
                ),
                CatalogComponents.component(
                        "Dividers",
                        "Dividers group related content with a subtle horizontal or vertical boundary.",
                        CatalogIcons.DIVIDER,
                        "divider",
                        "M3Divider",
                        CatalogComponents.example(
                                "Full-width divider",
                                "A horizontal divider spanning its complete allocated width.",
                                false,
                                () -> CatalogContainerSamples.horizontalDivider(0.0, 0.0)
                        ),
                        CatalogComponents.example(
                                "Inset divider",
                                "A horizontal divider with a leading content inset.",
                                false,
                                () -> CatalogContainerSamples.horizontalDivider(40.0, 0.0)
                        ),
                        CatalogComponents.example(
                                "Middle-inset divider",
                                "A horizontal divider inset from both content edges.",
                                false,
                                () -> CatalogContainerSamples.horizontalDivider(40.0, 40.0)
                        ),
                        CatalogComponents.example(
                                "Vertical divider",
                                "A vertical separator between adjacent content.",
                                false,
                                CatalogContainerSamples::verticalDivider
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Drop zones",
                        "Drop zones accept files or other dragged content within a clearly designated target.",
                        CatalogIcons.DROP_ZONE,
                        "https://opensource.adobe.com/spectrum-web-components/components/dropzone/",
                        "M3DropZone",
                        CatalogComponents.example(
                                "File drop zone",
                                "An interactive target that accepts files dragged from the desktop.",
                                false,
                                () -> CatalogContainerSamples.dropZone(false, false)
                        ),
                        CatalogComponents.example(
                                "Filled drop zone",
                                "A target presenting content from an earlier successful import.",
                                false,
                                () -> CatalogContainerSamples.dropZone(true, false)
                        ),
                        CatalogComponents.example(
                                "Disabled drop zone",
                                "A target that rejects drag gestures while its workflow is unavailable.",
                                false,
                                () -> CatalogContainerSamples.dropZone(false, true)
                        )
                ),
                CatalogComponents.component(
                        "Lists",
                        "Lists present vertically arranged rows of related content and actions.",
                        CatalogIcons.LIST,
                        "lists",
                        "M3ListPane",
                        CatalogComponents.example(
                                "One-line list item",
                                "A compact row with leading icon and trailing metadata.",
                                false,
                                () -> CatalogContainerSamples.listItem(
                                        1,
                                        M3ListItemSlotSize.ICON,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Two-line list item",
                                "A row with headline and supporting text.",
                                false,
                                () -> CatalogContainerSamples.listItem(
                                        2,
                                        M3ListItemSlotSize.ICON,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Three-line list item",
                                "A row with overline, headline, supporting text, and avatar.",
                                false,
                                () -> CatalogContainerSamples.listItem(
                                        3,
                                        M3ListItemSlotSize.AVATAR,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Thumbnail list item",
                                "A row using the standard square thumbnail slot.",
                                false,
                                () -> CatalogContainerSamples.listItem(
                                        2,
                                        M3ListItemSlotSize.THUMBNAIL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Wide-thumbnail list item",
                                "A row using the wide media slot.",
                                false,
                                () -> CatalogContainerSamples.listItem(
                                        2,
                                        M3ListItemSlotSize.WIDE_THUMBNAIL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Disabled list item",
                                "A two-line row in its disabled state.",
                                false,
                                () -> CatalogContainerSamples.listItem(
                                        2,
                                        M3ListItemSlotSize.ICON,
                                        true
                                )
                        ),
                        CatalogComponents.example(
                                "Standard list",
                                "A continuous sectioned list with supporting content.",
                                false,
                                () -> CatalogContainerSamples.list(M3ListStyle.STANDARD, true, false)
                        ),
                        CatalogComponents.example(
                                "Segmented list",
                                "Contained list rows separated by the Material segmented gap.",
                                true,
                                () -> CatalogContainerSamples.list(M3ListStyle.SEGMENTED, false, true)
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Scrims",
                        "Scrims dim content behind a modal surface and may provide a dismiss action.",
                        CatalogIcons.SCRIM,
                        "https://m3.material.io/foundations/interaction/states/overview",
                        "M3Scrim",
                        CatalogComponents.example(
                                "Plain scrim",
                                "A shown modal overlay without an action handler.",
                                false,
                                () -> CatalogContainerSamples.scrim(false)
                        ),
                        CatalogComponents.example(
                                "Dismissible scrim",
                                "A local modal overlay dismissed by its action event.",
                                false,
                                () -> CatalogContainerSamples.scrim(true)
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Scroll panes",
                        "Scroll panes keep logical values bounded while smooth input and edge effects "
                                + "decorate movement.",
                        CatalogIcons.SCROLL_PANE,
                        "https://developer.android.com/develop/ui/compose/touch-input/scroll",
                        "M3ScrollPane",
                        CatalogComponents.example(
                                "Default vertical stretch",
                                "The default resistant stretch for direct and lifecycle-delimited continuous input.",
                                false,
                                () -> CatalogContainerSamples.scrollPane(
                                        false,
                                        M3OverscrollInputMode.CONTINUOUS,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Bidirectional stretch",
                                "Content larger on both axes with independently anchored horizontal and vertical "
                                        + "stretch.",
                                false,
                                () -> CatalogContainerSamples.scrollPane(
                                        true,
                                        M3OverscrollInputMode.CONTINUOUS,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Pronounced custom stretch",
                                "A custom effect with a larger maximum and lower pull resistance.",
                                true,
                                () -> CatalogContainerSamples.scrollPane(
                                        false,
                                        M3OverscrollInputMode.CONTINUOUS,
                                        true
                                )
                        ),
                        CatalogComponents.example(
                                "Direct-input stretch",
                                "An edge effect restricted to JavaFX direct manipulation, normally touchscreen input.",
                                false,
                                () -> CatalogContainerSamples.scrollPane(
                                        false,
                                        M3OverscrollInputMode.DIRECT,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Wheel-inclusive stretch",
                                "All-input mode also decorates isolated mouse-wheel events for desktop inspection.",
                                false,
                                () -> CatalogContainerSamples.scrollPane(
                                        false,
                                        M3OverscrollInputMode.ALL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Overscroll disabled",
                                "The same bounded scrolling behavior with the optional edge effect removed.",
                                false,
                                () -> CatalogContainerSamples.scrollPane(false, null, false)
                        )
                ),
                CatalogComponents.component(
                        "Side sheets",
                        "Side sheets present supplementary content from a side edge without replacing the page.",
                        CatalogIcons.SIDE_SHEET,
                        "side-sheets",
                        "M3SideSheet",
                        CatalogComponents.example(
                                "Standard side sheet",
                                "A persistent supplementary surface with actions.",
                                false,
                                () -> CatalogContainerSamples.sideSheet(M3SheetVariant.STANDARD, false)
                        ),
                        CatalogComponents.example(
                                "Modal side sheet",
                                "A dismissible modal sheet coordinated with a scrim.",
                                false,
                                CatalogSamples::modalSideSheet
                        ),
                        CatalogComponents.example(
                                "Detached side sheet",
                                "A supplementary sheet using detached container geometry.",
                                true,
                                () -> CatalogContainerSamples.sideSheet(M3SheetVariant.STANDARD, true)
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Surfaces",
                        "Surfaces apply Material container colors, padding, shape, and elevation to grouped content.",
                        CatalogIcons.SURFACE,
                        "https://m3.material.io/styles/elevation/overview",
                        "M3Surface",
                        CatalogComponents.example(
                                "Base surface",
                                "The base surface color at elevation level zero.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.SURFACE,
                                        M3SurfaceElevation.LEVEL0,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Low container",
                                "A low surface-container tone at elevation level one.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.CONTAINER_LOW,
                                        M3SurfaceElevation.LEVEL1,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "High container",
                                "A high surface-container tone at elevation level three.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.CONTAINER_HIGH,
                                        M3SurfaceElevation.LEVEL3,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Primary container",
                                "A semantic primary-container surface.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.PRIMARY_CONTAINER,
                                        M3SurfaceElevation.LEVEL2,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Secondary container",
                                "A semantic secondary-container surface.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.SECONDARY_CONTAINER,
                                        M3SurfaceElevation.LEVEL2,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Tertiary container",
                                "A semantic tertiary-container surface.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.TERTIARY_CONTAINER,
                                        M3SurfaceElevation.LEVEL2,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Local container paint",
                                "A surface with a local container-color override.",
                                false,
                                () -> CatalogContainerSamples.surface(
                                        M3SurfaceVariant.SURFACE,
                                        M3SurfaceElevation.LEVEL1,
                                        true
                                )
                        )
                )
        );
    }
}
