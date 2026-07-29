// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/// Provides persistent component and example navigation for the Catalog.
///
/// The sidebar shows every visible component and expands the component associated with the current route to expose
/// its examples. It retains a single scroll pane while route changes rebuild the navigation items, and positions the
/// selected item after layout so the active route and following examples remain available.
@NotNullByDefault
final class CatalogSidebar extends VBox {
    /// The pseudo-class applied to the item associated with the current route.
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    /// The immutable complete component registry.
    private final @Unmodifiable List<CatalogComponent> components;

    /// The callback used for component and example navigation.
    private final Consumer<CatalogRoute> navigate;

    /// The callback used to clear history and navigate home.
    private final Runnable navigateHome;

    /// The summary shown below the sidebar heading.
    private final M3Text summary = new M3Text("", M3TextRole.BODY_SMALL);

    /// The vertical container holding route navigation items.
    private final VBox items = new VBox();

    /// The persistent smoothly scrolling navigation viewport.
    private final ScrollPane scrollPane = new ScrollPane(items);

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

        M3Text title = new M3Text("Catalog", M3TextRole.TITLE_LARGE);
        title.getStyleClass().add("catalog-sidebar-title");
        summary.getStyleClass().add("catalog-sidebar-summary");

        VBox header = new VBox(4.0, title, summary);
        header.getStyleClass().add("catalog-sidebar-header");

        items.getStyleClass().add("catalog-sidebar-items");
        items.setFillWidth(true);

        scrollPane.getStyleClass().add("catalog-sidebar-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setMinSize(0.0, 0.0);
        M3ScrollPanes.style(scrollPane);
        M3ScrollPanes.enableSmoothScrolling(scrollPane);

        getChildren().setAll(header, new M3Divider(), scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    /// Rebuilds navigation items for the current route and component filter.
    ///
    /// @param route the current Catalog route
    /// @param expressiveOnly whether components without Expressive examples are hidden
    void refresh(CatalogRoute route, boolean expressiveOnly) {
        Objects.requireNonNull(route, "route");
        @Nullable CatalogComponent activeComponent = componentOf(route);
        @Nullable CatalogExample activeExample = route instanceof CatalogRoute.Example exampleRoute
                ? exampleRoute.example()
                : null;

        List<CatalogComponent> visibleComponents = components.stream()
                .filter(component -> !expressiveOnly || component.hasExpressiveExamples())
                .toList();
        int exampleCount = visibleComponents.stream().mapToInt(component -> component.examples().size()).sum();
        summary.setText(visibleComponents.size() + " components · " + exampleCount + " examples");

        ArrayList<Node> navigationItems = new ArrayList<>(visibleComponents.size() + 2);
        M3Button home = navigationButton(
                "Home",
                CatalogIcons.create(CatalogIcons.HOME),
                navigateHome,
                route instanceof CatalogRoute.Home,
                "catalog-sidebar-home"
        );
        navigationItems.add(home);

        M3Text componentsHeading = new M3Text("Components", M3TextRole.LABEL_LARGE);
        componentsHeading.getStyleClass().add("catalog-sidebar-section-title");
        navigationItems.add(componentsHeading);

        @Nullable Node selectedItem = route instanceof CatalogRoute.Home ? home : null;
        for (CatalogComponent component : visibleComponents) {
            boolean componentSelected = component.equals(activeComponent);
            M3Button componentButton = navigationButton(
                    component.name(),
                    CatalogIcons.create(component.iconPath()),
                    () -> navigate.accept(new CatalogRoute.Component(component)),
                    componentSelected && activeExample == null,
                    "catalog-sidebar-component"
            );
            navigationItems.add(componentButton);
            if (componentSelected && activeExample == null) {
                selectedItem = componentButton;
            }

            if (componentSelected) {
                for (CatalogExample example : component.examples()) {
                    boolean exampleSelected = example.equals(activeExample);
                    M3Button exampleButton = navigationButton(
                            example.name(),
                            null,
                            () -> navigate.accept(new CatalogRoute.Example(component, example)),
                            exampleSelected,
                            "catalog-sidebar-example"
                    );
                    navigationItems.add(exampleButton);
                    if (exampleSelected) {
                        selectedItem = exampleButton;
                    }
                }
            }
        }

        items.getChildren().setAll(navigationItems);
        if (selectedItem != null) {
            revealAfterLayout(selectedItem, activeComponent != null && activeExample == null);
        }
    }

    /// Creates one full-width sidebar navigation button.
    ///
    /// @param text the visible item label
    /// @param graphic the optional leading graphic
    /// @param action the action invoked when the item is activated
    /// @param selected whether the item represents the current route
    /// @param styleClass the route-kind style class
    /// @return the configured navigation button
    private static M3Button navigationButton(
            String text,
            @Nullable Node graphic,
            Runnable action,
            boolean selected,
            String styleClass
    ) {
        M3Button button = new M3Button(text, graphic, M3ButtonVariant.TEXT);
        button.getStyleClass().addAll("catalog-sidebar-item", styleClass);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.pseudoClassStateChanged(SELECTED, selected);
        button.setOnAction(event -> action.run());
        return button;
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

    /// Schedules selected-item visibility adjustment after CSS and layout have established item bounds.
    ///
    /// @param selectedItem the selected navigation item
    /// @param alignToTop whether the item is aligned to the viewport top to expose following examples
    private void revealAfterLayout(Node selectedItem, boolean alignToTop) {
        Platform.runLater(() -> Platform.runLater(() -> reveal(selectedItem, alignToTop)));
    }

    /// Positions the selected item according to the active route while avoiding unnecessary example-route movement.
    ///
    /// @param selectedItem the selected navigation item
    /// @param alignToTop whether the item is aligned to the viewport top
    private void reveal(Node selectedItem, boolean alignToTop) {
        if (selectedItem.getScene() == null || scrollPane.getViewportBounds().getHeight() <= 0.0) {
            return;
        }

        scrollPane.applyCss();
        scrollPane.layout();
        items.layout();
        scrollPane.layout();

        @Nullable Node viewport = scrollPane.lookup(".viewport");
        if (viewport == null) {
            return;
        }
        Bounds itemBounds = selectedItem.localToScene(selectedItem.getBoundsInLocal());
        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double contentHeight = items.getLayoutBounds().getHeight();
        double scrollableHeight = Math.max(0.0, contentHeight - viewportHeight);
        if (scrollableHeight <= 0.0) {
            scrollPane.setVvalue(scrollPane.getVmin());
            return;
        }

        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double currentOffset = valueRange <= 0.0
                ? 0.0
                : (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange * scrollableHeight;
        double targetOffset = currentOffset;
        if (alignToTop) {
            targetOffset += itemBounds.getMinY() - viewportBounds.getMinY();
        } else if (itemBounds.getMinY() < viewportBounds.getMinY()) {
            targetOffset += itemBounds.getMinY() - viewportBounds.getMinY();
        } else if (itemBounds.getMaxY() > viewportBounds.getMaxY()) {
            targetOffset += itemBounds.getMaxY() - viewportBounds.getMaxY();
        }

        if (Double.compare(targetOffset, currentOffset) != 0) {
            double targetFraction = Math.max(0.0, Math.min(1.0, targetOffset / scrollableHeight));
            scrollPane.setVvalue(scrollPane.getVmin() + targetFraction * valueRange);
        }
    }
}
