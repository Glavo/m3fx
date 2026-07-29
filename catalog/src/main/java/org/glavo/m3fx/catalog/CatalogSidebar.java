// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/// Provides Material Design 3 component-reference navigation for the Catalog.
///
/// The sidebar exposes Home and one persistent destination per component. Example routes keep their owning component
/// selected because detailed scenarios belong to the component page rather than the global application hierarchy.
/// Route synchronization changes selection and filtering in place; it does not rebuild drawer content or issue
/// deferred scroll corrections.
@NotNullByDefault
final class CatalogSidebar extends StackPane {
    /// The immutable complete component registry.
    private final @Unmodifiable List<CatalogComponent> components;

    /// The callback used for component navigation.
    private final Consumer<CatalogRoute> navigate;

    /// The callback used to clear history and navigate Home.
    private final Runnable navigateHome;

    /// The native Material navigation drawer providing layout, selection, accessibility, and scrolling.
    private final M3NavigationDrawer drawer = new M3NavigationDrawer();

    /// The persistent Home destination.
    private final M3ListItem homeItem = new M3ListItem("Home");

    /// The summary shown in the drawer header.
    private final M3Text summary = new M3Text("", M3TextRole.BODY_SMALL);

    /// The persistent search field used to filter component destinations.
    private final M3SearchBar componentSearch = new M3SearchBar("Search components");

    /// The message shown when no component matches the current filters.
    private final M3Text emptyMessage = new M3Text("No matching components", M3TextRole.BODY_MEDIUM);

    /// Persistent component destinations indexed in registry order.
    private final Map<CatalogComponent, M3ListItem> componentItems = new LinkedHashMap<>();

    /// The most recently synchronized route, or `null` before the first refresh.
    private @Nullable CatalogRoute currentRoute;

    /// Whether the application-wide Expressive-only filter is active.
    private boolean expressiveOnly;

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
        componentSearch.getStyleClass().add("catalog-sidebar-search");
        componentSearch.setAccessibleText("Search Catalog components");
        componentSearch.setMaxWidth(Double.MAX_VALUE);
        componentSearch.textProperty().addListener(observable -> updateItems());
        VBox header = new VBox(4.0, title, summary, componentSearch);
        header.getStyleClass().add("catalog-sidebar-header");

        homeItem.getStyleClass().addAll("catalog-sidebar-item", "catalog-sidebar-home");
        homeItem.setLeading(CatalogIcons.create(CatalogIcons.HOME));
        homeItem.setOnAction(event -> navigateHome.run());

        M3Text componentsHeading = new M3Text("Components", M3TextRole.LABEL_LARGE);
        componentsHeading.getStyleClass().add("catalog-sidebar-section-title");
        drawer.getItems().addAll(header, new M3Divider(), homeItem, componentsHeading);

        for (CatalogComponent component : components) {
            M3ListItem item = createComponentItem(component);
            componentItems.put(component, item);
            drawer.getItems().add(item);
        }

        emptyMessage.getStyleClass().add("catalog-sidebar-empty");
        emptyMessage.setVisible(false);
        emptyMessage.setManaged(false);
        drawer.getItems().add(emptyMessage);
    }

    /// Creates one persistent component destination.
    ///
    /// @param component the represented component
    /// @return the persistent component destination
    private M3ListItem createComponentItem(CatalogComponent component) {
        M3ListItem item = new M3ListItem(component.name());
        item.getStyleClass().addAll("catalog-sidebar-item", "catalog-sidebar-component");
        item.setLeading(CatalogIcons.create(component.iconPath()));
        item.setOnAction(event -> navigate.accept(new CatalogRoute.Component(component)));
        return item;
    }

    /// Synchronizes selection and filtering without replacing drawer items or changing scroll position.
    ///
    /// @param route the current Catalog route
    /// @param expressiveOnly whether components without Expressive examples are hidden
    void refresh(CatalogRoute route, boolean expressiveOnly) {
        currentRoute = Objects.requireNonNull(route, "route");
        this.expressiveOnly = expressiveOnly;
        updateItems();
    }

    /// Applies search and profile filtering while retaining every persistent destination node.
    private void updateItems() {
        @Nullable CatalogComponent activeComponent = currentRoute == null ? null : componentOf(currentRoute);
        String query = componentSearch.getText().strip().toLowerCase(Locale.ROOT);

        int profileComponentCount = 0;
        int visibleComponentCount = 0;
        int visibleExampleCount = 0;
        @Nullable M3ListItem selectedItem = activeComponent == null
                ? homeItem
                : componentItems.get(activeComponent);

        for (Map.Entry<CatalogComponent, M3ListItem> entry : componentItems.entrySet()) {
            CatalogComponent component = entry.getKey();
            M3ListItem item = entry.getValue();
            boolean includedByProfile = !expressiveOnly || component.hasExpressiveExamples();
            if (includedByProfile) {
                profileComponentCount++;
            }
            boolean visible = includedByProfile && matches(component, query);
            item.setVisible(visible);
            item.setManaged(visible);
            if (visible) {
                visibleComponentCount++;
                visibleExampleCount += component.examples().size();
            }
        }
        summary.setText(query.isEmpty()
                ? visibleComponentCount + " components · " + visibleExampleCount + " scenarios"
                : visibleComponentCount + " of " + profileComponentCount + " components · "
                        + visibleExampleCount + " scenarios");
        boolean empty = visibleComponentCount == 0;
        emptyMessage.setVisible(empty);
        emptyMessage.setManaged(empty);
        if (selectedItem != null && isEffectivelyReachable(selectedItem)) {
            drawer.select(selectedItem);
        } else if (selectedItem != homeItem && isEffectivelyReachable(homeItem)) {
            drawer.select(homeItem);
        }
    }

    /// Returns whether a component or one of its scenarios matches a normalized search query.
    ///
    /// @param component the component to inspect
    /// @param query the stripped lower-case query, or an empty string to match every component
    /// @return `true` when the component should remain visible
    private static boolean matches(CatalogComponent component, String query) {
        if (query.isEmpty()
                || component.name().toLowerCase(Locale.ROOT).contains(query)
                || component.description().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        return component.examples().stream().anyMatch(example ->
                example.name().toLowerCase(Locale.ROOT).contains(query)
                        || example.description().toLowerCase(Locale.ROOT).contains(query));
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

}
