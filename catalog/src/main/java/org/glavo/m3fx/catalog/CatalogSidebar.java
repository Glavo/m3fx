// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/// Provides Material Design 3 component and example navigation for the Catalog.
///
/// The sidebar builds one persistent [M3NavigationDrawer] hierarchy and retains every destination node for its
/// lifetime. Route synchronization changes selection, visibility, and disclosure state in place; it does not rebuild
/// drawer content or issue deferred scroll corrections. Component groups retain user-controlled expansion state,
/// while the group associated with a route is expanded when necessary to expose the selected example.
@NotNullByDefault
final class CatalogSidebar extends StackPane {
    /// The immutable complete component registry.
    private final @Unmodifiable List<CatalogComponent> components;

    /// The callback used for component and example navigation.
    private final Consumer<CatalogRoute> navigate;

    /// The callback used to clear history and navigate Home.
    private final Runnable navigateHome;

    /// The native Material navigation drawer providing layout, selection, accessibility, and scrolling.
    private final M3NavigationDrawer drawer = new M3NavigationDrawer();

    /// The persistent Home destination.
    private final M3ListItem homeItem = new M3ListItem("Home");

    /// The summary shown in the drawer header.
    private final M3Text summary = new M3Text("", M3TextRole.BODY_SMALL);

    /// Persistent drawer entries indexed in registry order.
    private final Map<CatalogComponent, ComponentEntry> componentEntries = new LinkedHashMap<>();

    /// Whether route synchronization is currently mutating drawer state.
    private boolean synchronizing;

    /// Creates a sidebar for a Catalog component registry.
    ///
    /// @param components the complete component registry
    /// @param navigate the callback for component and example routes
    /// @param navigateHome the callback for the Home route
    /// @throws NullPointerException if an argument or component is `null`
    CatalogSidebar(
            List<CatalogComponent> components,
            Consumer<CatalogRoute> navigate,
            Runnable navigateHome
    ) {
        this.components = List.copyOf(Objects.requireNonNull(components, "components"));
        this.navigate = Objects.requireNonNull(navigate, "navigate");
        this.navigateHome = Objects.requireNonNull(navigateHome, "navigateHome");

        getStyleClass().add("catalog-sidebar");
        setMinSize(0.0, 0.0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        configureDrawer();
        getChildren().setAll(drawer);
    }

    /// Configures the persistent drawer header and destination hierarchy.
    private void configureDrawer() {
        drawer.getStyleClass().add("catalog-sidebar-drawer");
        drawer.setAllowEmptySelection(true);

        M3Text title = new M3Text("Catalog", M3TextRole.TITLE_LARGE);
        title.getStyleClass().add("catalog-sidebar-title");
        summary.getStyleClass().add("catalog-sidebar-summary");
        VBox header = new VBox(4.0, title, summary);
        header.getStyleClass().add("catalog-sidebar-header");

        homeItem.getStyleClass().addAll("catalog-sidebar-item", "catalog-sidebar-home");
        homeItem.setLeading(CatalogIcons.create(CatalogIcons.HOME));
        homeItem.setOnAction(event -> navigateHome.run());

        M3Text componentsHeading = new M3Text("Components", M3TextRole.LABEL_LARGE);
        componentsHeading.getStyleClass().add("catalog-sidebar-section-title");
        drawer.getItems().addAll(header, new M3Divider(), homeItem, componentsHeading);

        for (CatalogComponent component : components) {
            ComponentEntry entry = createComponentEntry(component);
            componentEntries.put(component, entry);
            drawer.getItems().add(entry.group());
        }
    }

    /// Creates one persistent component group and all of its example destinations.
    ///
    /// @param component the represented component
    /// @return the persistent component entry
    private ComponentEntry createComponentEntry(CatalogComponent component) {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup(component.name());
        group.getStyleClass().add("catalog-sidebar-component-group");

        M3ListItem componentItem = group.getHeaderItem();
        componentItem.getStyleClass().addAll("catalog-sidebar-item", "catalog-sidebar-component");
        componentItem.setLeading(CatalogIcons.create(component.iconPath()));
        componentItem.addEventHandler(ActionEvent.ACTION, event -> {
            if (!synchronizing) {
                navigate.accept(new CatalogRoute.Component(component));
            }
        });

        Map<CatalogExample, M3ListItem> exampleItems = new LinkedHashMap<>();
        for (CatalogExample example : component.examples()) {
            M3ListItem exampleItem = new M3ListItem(example.name());
            exampleItem.getStyleClass().addAll("catalog-sidebar-item", "catalog-sidebar-example");
            exampleItem.setOnAction(event -> navigate.accept(new CatalogRoute.Example(component, example)));
            exampleItems.put(example, exampleItem);
            group.getItems().add(exampleItem);
        }
        return new ComponentEntry(component, group, componentItem, Map.copyOf(exampleItems));
    }

    /// Synchronizes selection and filtering without replacing drawer items or changing scroll position.
    ///
    /// @param route the current Catalog route
    /// @param expressiveOnly whether components without Expressive examples are hidden
    void refresh(CatalogRoute route, boolean expressiveOnly) {
        Objects.requireNonNull(route, "route");
        @Nullable CatalogComponent activeComponent = componentOf(route);
        @Nullable CatalogExample activeExample = route instanceof CatalogRoute.Example exampleRoute
                ? exampleRoute.example()
                : null;

        int visibleComponentCount = 0;
        int visibleExampleCount = 0;
        @Nullable M3ListItem selectedItem = route instanceof CatalogRoute.Home ? homeItem : null;

        synchronizing = true;
        try {
            for (ComponentEntry entry : componentEntries.values()) {
                boolean visible = !expressiveOnly || entry.component().hasExpressiveExamples();
                entry.group().setVisible(visible);
                entry.group().setManaged(visible);
                if (visible) {
                    visibleComponentCount++;
                    visibleExampleCount += entry.component().examples().size();
                }

                if (entry.component().equals(activeComponent)) {
                    if (!entry.group().isExpanded()) {
                        entry.group().setExpanded(true);
                    }
                    selectedItem = activeExample == null
                            ? entry.componentItem()
                            : entry.exampleItems().get(activeExample);
                }
            }
            summary.setText(visibleComponentCount + " components · " + visibleExampleCount + " examples");
            if (selectedItem != null && isEffectivelyReachable(selectedItem)) {
                drawer.select(selectedItem);
            }
        } finally {
            synchronizing = false;
        }
    }

    /// Sets the Material drawer presentation used by this sidebar.
    ///
    /// @param variant the standard or modal drawer variant
    void setVariant(M3NavigationDrawerVariant variant) {
        drawer.setVariant(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the native drawer for package-level visual verification.
    ///
    /// @return the persistent Material navigation drawer
    M3NavigationDrawer drawer() {
        return drawer;
    }

    /// Returns whether a node and every ancestor are visible and enabled.
    ///
    /// @param node the node to inspect
    /// @return `true` when the node can participate in drawer selection
    private static boolean isEffectivelyReachable(Node node) {
        for (@Nullable Node current = node; current != null; current = current.getParent()) {
            if (!current.isVisible() || current.isDisabled()) {
                return false;
            }
        }
        return true;
    }

    /// Returns the component associated with a route.
    ///
    /// @param route the route to inspect
    /// @return the associated component, or `null` for Home
    private static @Nullable CatalogComponent componentOf(CatalogRoute route) {
        if (route instanceof CatalogRoute.Component componentRoute) {
            return componentRoute.component();
        }
        if (route instanceof CatalogRoute.Example exampleRoute) {
            return exampleRoute.component();
        }
        return null;
    }

    /// Stores the persistent nodes associated with one component.
    ///
    /// @param component the represented component
    /// @param group the disclosure group containing the component and its examples
    /// @param componentItem the group header destination
    /// @param exampleItems immutable example-to-destination mappings
    private record ComponentEntry(
            CatalogComponent component,
            M3NavigationDrawerGroup group,
            M3ListItem componentItem,
            @Unmodifiable Map<CatalogExample, M3ListItem> exampleItems
    ) {
        /// Validates the persistent entry.
        private ComponentEntry {
            Objects.requireNonNull(component, "component");
            Objects.requireNonNull(group, "group");
            Objects.requireNonNull(componentItem, "componentItem");
            exampleItems = Map.copyOf(Objects.requireNonNull(exampleItems, "exampleItems"));
        }
    }
}
