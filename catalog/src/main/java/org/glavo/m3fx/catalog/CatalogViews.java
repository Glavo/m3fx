// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/// Builds the three content views used by the Compose Material Catalog-style navigation hierarchy.
///
/// The builders are stateless. Each invocation creates a fresh node tree, restores browser state supplied by the
/// application, and reports navigation and state changes through callbacks. Layout nodes expose catalog-specific
/// style classes so spacing, shape, typography, and responsive refinements can be supplied by the Catalog stylesheet
/// without embedding theme colors here.
@NotNullByDefault
final class CatalogViews {
    /// The minimum adaptive-grid cell width, in logical pixels.
    private static final double HOME_CELL_MIN_WIDTH = 180.0;

    /// The fixed adaptive-grid cell height, in logical pixels.
    private static final double HOME_CELL_HEIGHT = 180.0;

    /// The outer spacing inset applied within each adaptive-grid cell.
    private static final double HOME_CELL_OUTER_PADDING = 4.0;

    /// The icon size used by component cards, in logical pixels.
    private static final double COMPONENT_CARD_ICON_SIZE = 80.0;

    /// The icon size used by a component detail page, in logical pixels.
    private static final double COMPONENT_PAGE_ICON_SIZE = 80.0;

    /// The minimum width of one component-page example cell, in logical pixels.
    private static final double EXAMPLE_CELL_MIN_WIDTH = 320.0;

    /// The fixed height of one component-page example cell, in logical pixels.
    private static final double EXAMPLE_CELL_HEIGHT = 112.0;

    /// The uniform inner padding used by Catalog cards.
    private static final double CARD_CONTENT_PADDING = 16.0;

    /// The diagonal Expressive banner size used by home cards.
    private static final double COMPONENT_BANNER_SIZE = 80.0;

    /// The diagonal Expressive banner size used by example cards.
    private static final double EXAMPLE_BANNER_SIZE = 64.0;

    /// The standard Material card corner radius, in logical pixels.
    private static final double CARD_CORNER_RADIUS = 12.0;

    /// Prevents utility class instantiation.
    private CatalogViews() {
    }

    /// Creates the searchable, filterable component browser displayed by the Catalog home route.
    ///
    /// Components without Expressive examples are omitted when `expressiveOnly` is `true`. Component cards use
    /// an outlined Material surface and navigate to the corresponding [CatalogRoute.Component] when activated. The
    /// All, Favorites, and Expressive segments filter persistent card nodes without rebuilding the adaptive grid.
    /// The returned scroll pane fits its content to the viewport width, allowing the grid to recompute its column
    /// count as the available width changes.
    ///
    /// @param components     the components available to the Catalog
    /// @param favoriteNames  the component names favorited when this view is created
    /// @param initialState   the search, filter, and scroll state to restore
    /// @param updateState    the consumer that retains subsequent browser-state changes
    /// @param navigate       the consumer that handles route changes
    /// @param expressiveOnly whether only components with Expressive examples are shown
    /// @param markExpressive whether components with Expressive examples display a corner marker
    /// @return a new scrollable home view
    /// @throws NullPointerException if `components`, `favoriteNames`, `initialState`, `updateState`, `navigate`,
    ///                              or either collection's elements is `null`
    static Node createHome(
            List<CatalogComponent> components,
            Set<String> favoriteNames,
            CatalogBrowserState initialState,
            Consumer<CatalogBrowserState> updateState,
            Consumer<CatalogRoute> navigate,
            boolean expressiveOnly,
            boolean markExpressive
    ) {
        CatalogBrowserState browserState = Objects.requireNonNull(initialState, "initialState");
        Consumer<CatalogBrowserState> stateConsumer = Objects.requireNonNull(updateState, "updateState");
        Consumer<CatalogRoute> routeConsumer = Objects.requireNonNull(navigate, "navigate");
        @Unmodifiable Set<String> favorites = Set.copyOf(
                Objects.requireNonNull(favoriteNames, "favoriteNames")
        );
        List<CatalogComponent> sortedComponents = List.copyOf(Objects.requireNonNull(components, "components"))
                .stream()
                .filter(component -> !expressiveOnly || component.hasExpressiveExamples())
                .sorted(Comparator.comparing(CatalogComponent::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(CatalogComponent::name))
                .toList();

        TilePane grid = new TilePane(Orientation.HORIZONTAL);
        grid.getStyleClass().add("catalog-component-grid");
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setTileAlignment(Pos.TOP_LEFT);
        grid.setPrefTileWidth(HOME_CELL_MIN_WIDTH);
        grid.setPrefTileHeight(HOME_CELL_HEIGHT);
        Map<CatalogComponent, StackPane> componentCells = new LinkedHashMap<>();
        for (CatalogComponent component : sortedComponents) {
            StackPane cell = createComponentCard(
                    component,
                    routeConsumer,
                    markExpressive,
                    favorites.contains(component.name())
            );
            componentCells.put(component, cell);
            grid.getChildren().add(cell);
        }
        grid.widthProperty().addListener(observable -> updateAdaptiveTileWidth(grid, HOME_CELL_MIN_WIDTH));

        M3Text title = new M3Text("Material components", M3TextRole.HEADLINE_MEDIUM);
        title.getStyleClass().add("catalog-home-title");
        M3Text description = new M3Text(
                "Browse the complete M3FX component set and open focused interactive scenarios.",
                M3TextRole.BODY_MEDIUM
        );
        description.getStyleClass().add("catalog-home-description");
        description.setWrapText(true);
        VBox heading = new VBox(title, description);
        heading.getStyleClass().add("catalog-home-heading");

        M3SearchBar componentSearch = new M3SearchBar("Search components");
        componentSearch.getStyleClass().add("catalog-home-search");
        componentSearch.setAccessibleText("Search Catalog components and scenarios");
        componentSearch.setMinWidth(0.0);
        componentSearch.setPrefWidth(320.0);
        componentSearch.setMaxWidth(400.0);
        componentSearch.setText(browserState.query());

        M3SegmentedButton allFilter = createHomeFilterButton("All", "catalog-home-filter-all");
        M3SegmentedButton favoritesFilter =
                createHomeFilterButton("Favorites", "catalog-home-filter-favorites");
        M3SegmentedButton expressiveFilter =
                createHomeFilterButton("Expressive", "catalog-home-filter-expressive");
        M3SegmentedButtonGroup filterGroup = new M3SegmentedButtonGroup();
        filterGroup.getStyleClass().add("catalog-home-filter-group");
        filterGroup.setAccessibleText("Component collection filter");
        filterGroup.getItems().addAll(allFilter, favoritesFilter, expressiveFilter);
        filterGroup.setAllowEmptySelection(false);
        filterGroup.selectIndex(browserState.filterIndex());

        FlowPane browserControls = new FlowPane(12.0, 12.0);
        browserControls.getStyleClass().add("catalog-home-browser-controls");
        browserControls.getChildren().addAll(componentSearch, filterGroup);

        M3Text resultSummary = new M3Text("", M3TextRole.LABEL_MEDIUM);
        resultSummary.getStyleClass().add("catalog-home-result-summary");
        VBox browser = new VBox(browserControls, resultSummary);
        browser.getStyleClass().add("catalog-home-browser");

        M3Text emptyTitle = new M3Text("No matching components", M3TextRole.TITLE_MEDIUM);
        emptyTitle.getStyleClass().add("catalog-home-empty-title");
        M3Text emptyDescription = new M3Text(
                "Try another search or choose a different collection.",
                M3TextRole.BODY_MEDIUM
        );
        emptyDescription.getStyleClass().add("catalog-home-empty-description");
        emptyDescription.setWrapText(true);
        VBox emptyState = new VBox(emptyTitle, emptyDescription);
        emptyState.getStyleClass().add("catalog-home-empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setVisible(false);
        emptyState.setManaged(false);

        Runnable updateFilter = () -> updateComponentVisibility(
                componentCells,
                favorites,
                componentSearch.getText(),
                filterGroup.getSelectedIndex(),
                resultSummary,
                grid,
                emptyState
        );
        VBox page = new VBox(heading, browser, grid, emptyState);
        page.getStyleClass().addAll("catalog-route-page", "catalog-home-page");
        page.setFillWidth(true);

        M3ScrollPane scrollPane = new M3ScrollPane(page);
        scrollPane.getStyleClass().addAll("catalog-route-scroll", "catalog-home-scroll");
        scrollPane.setMinSize(0.0, 0.0);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setVvalue(browserState.scrollPosition());
        componentSearch.textProperty().addListener(observable -> {
            updateFilter.run();
            publishBrowserState(componentSearch, filterGroup, scrollPane, stateConsumer);
        });
        filterGroup.selectedButtonProperty().addListener(observable -> {
            updateFilter.run();
            publishBrowserState(componentSearch, filterGroup, scrollPane, stateConsumer);
        });
        scrollPane.vvalueProperty().addListener(observable ->
                publishBrowserState(componentSearch, filterGroup, scrollPane, stateConsumer));
        updateFilter.run();
        return scrollPane;
    }

    /// Creates one segment for the Home component-collection filter.
    ///
    /// @param label the visible segment label
    /// @param styleClass the stable Catalog style class
    /// @return a new unselected filter segment
    private static M3SegmentedButton createHomeFilterButton(String label, String styleClass) {
        M3SegmentedButton button = new M3SegmentedButton(label);
        button.getStyleClass().add(styleClass);
        return button;
    }

    /// Applies Home search and collection filters without replacing component cells.
    ///
    /// Filter index `0` shows every component, index `1` shows favorites, and index `2` shows components with an
    /// Expressive scenario. An unknown or temporarily empty selection behaves like the All filter.
    ///
    /// @param cells the persistent cells indexed by their component descriptors
    /// @param favoriteNames the immutable set of favorite component names
    /// @param searchText the current search text
    /// @param filterIndex the selected collection-filter index
    /// @param resultSummary the label updated with visible and total component counts
    /// @param grid the adaptive component grid hidden while the result is empty
    /// @param emptyState the state shown when no components match
    private static void updateComponentVisibility(
            Map<CatalogComponent, StackPane> cells,
            @Unmodifiable Set<String> favoriteNames,
            String searchText,
            int filterIndex,
            M3Text resultSummary,
            TilePane grid,
            VBox emptyState
    ) {
        int visibleCount = 0;
        for (Map.Entry<CatalogComponent, StackPane> entry : cells.entrySet()) {
            CatalogComponent component = entry.getKey();
            boolean collectionMatches = switch (filterIndex) {
                case 1 -> favoriteNames.contains(component.name());
                case 2 -> component.hasExpressiveExamples();
                default -> true;
            };
            boolean visible = collectionMatches && component.matchesSearch(searchText);
            entry.getValue().setVisible(visible);
            entry.getValue().setManaged(visible);
            if (visible) {
                visibleCount++;
            }
        }
        resultSummary.setText(visibleCount + " of " + cells.size() + " components");
        boolean empty = visibleCount == 0;
        grid.setVisible(!empty);
        grid.setManaged(!empty);
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    /// Distributes adaptive-grid cells across the available row width.
    ///
    /// @param grid the adaptive tile grid
    /// @param minimumTileWidth the minimum width used to derive the column count
    private static void updateAdaptiveTileWidth(TilePane grid, double minimumTileWidth) {
        Insets insets = grid.getInsets();
        double availableWidth = grid.getWidth() - insets.getLeft() - insets.getRight();
        if (availableWidth <= 0.0) {
            return;
        }

        int columns = Math.max(1, (int) Math.floor(availableWidth / minimumTileWidth));
        double outputScale = grid.getScene() != null && grid.getScene().getWindow() != null
                ? grid.getScene().getWindow().getOutputScaleX()
                : 1.0;
        double tileWidth = Math.floor(availableWidth * outputScale / columns) / outputScale;
        if (Math.abs(grid.getPrefTileWidth() - tileWidth) >= 0.01) {
            grid.setPrefTileWidth(tileWidth);
        }
    }

    /// Creates a component reference page with overview, source links, and a navigable example matrix.
    ///
    /// The reference header contains the component identity, description, scenario count, and direct links to
    /// Material guidance, M3FX API documentation, and source. Examples use an adaptive multi-column matrix.
    /// Activating an example card reports a [CatalogRoute.Example] through `navigate`.
    ///
    /// @param component      the component described by the page
    /// @param initialState   the search, filter, and scroll state to restore
    /// @param updateState    the consumer that retains subsequent browser-state changes
    /// @param navigate       the consumer that handles route changes
    /// @param openExternal   the consumer that opens an absolute external URL
    /// @param markExpressive whether Expressive examples display a marker
    /// @return a new scrollable component detail view
    /// @throws NullPointerException if `component`, `initialState`, `updateState`, `navigate`, or `openExternal`
    ///                              is `null`
    static Node createComponent(
            CatalogComponent component,
            CatalogBrowserState initialState,
            Consumer<CatalogBrowserState> updateState,
            Consumer<CatalogRoute> navigate,
            Consumer<String> openExternal,
            boolean markExpressive
    ) {
        CatalogComponent target = Objects.requireNonNull(component, "component");
        CatalogBrowserState browserState = Objects.requireNonNull(initialState, "initialState");
        Consumer<CatalogBrowserState> stateConsumer = Objects.requireNonNull(updateState, "updateState");
        Consumer<CatalogRoute> routeConsumer = Objects.requireNonNull(navigate, "navigate");
        Consumer<String> externalConsumer = Objects.requireNonNull(openExternal, "openExternal");

        StackPane icon = createSizedIcon(
                target.iconPath(),
                COMPONENT_PAGE_ICON_SIZE,
                "catalog-component-page-icon"
        );

        M3Text title = new M3Text(target.name(), M3TextRole.HEADLINE_MEDIUM);
        title.getStyleClass().add("catalog-component-reference-title");

        M3Text description = new M3Text(target.description(), M3TextRole.BODY_MEDIUM);
        description.getStyleClass().add("catalog-component-description");
        description.setWrapText(true);
        description.setMaxWidth(Double.MAX_VALUE);

        M3Text scenarioCount = new M3Text(
                target.examples().size() + " interactive scenarios",
                M3TextRole.LABEL_LARGE
        );
        scenarioCount.getStyleClass().add("catalog-component-scenario-count");

        FlowPane referenceActions = new FlowPane(8.0, 8.0);
        referenceActions.getStyleClass().add("catalog-component-reference-actions");
        referenceActions.getChildren().addAll(
                createReferenceButton("Guidelines", target.guidelinesUrl(), externalConsumer),
                createReferenceButton("Source", target.sourceUrl(), externalConsumer),
                createReferenceButton("API documentation", target.docsUrl(), externalConsumer)
        );

        VBox referenceLabels = new VBox(title, description, scenarioCount, referenceActions);
        referenceLabels.getStyleClass().add("catalog-component-reference-labels");
        referenceLabels.setFillWidth(true);
        referenceLabels.setMaxWidth(Double.MAX_VALUE);

        HBox reference = new HBox(icon, referenceLabels);
        reference.getStyleClass().add("catalog-component-reference");
        reference.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(referenceLabels, Priority.ALWAYS);

        M3Text examplesHeading = new M3Text("Interactive examples", M3TextRole.TITLE_LARGE);
        examplesHeading.getStyleClass().addAll("catalog-component-section-title", "catalog-examples-title");

        M3Text examplesDescription = new M3Text(
                "Open a focused specimen to inspect behavior, states, and responsive layout.",
                M3TextRole.BODY_MEDIUM
        );
        examplesDescription.getStyleClass().add("catalog-examples-description");
        examplesDescription.setWrapText(true);

        TilePane examples = new TilePane(Orientation.HORIZONTAL);
        examples.getStyleClass().add("catalog-example-grid");
        examples.setAlignment(Pos.TOP_LEFT);
        examples.setTileAlignment(Pos.TOP_LEFT);
        examples.setPrefTileWidth(EXAMPLE_CELL_MIN_WIDTH);
        examples.setPrefTileHeight(EXAMPLE_CELL_HEIGHT);
        Map<CatalogExample, StackPane> exampleCells = new LinkedHashMap<>();
        for (CatalogExample example : target.examples()) {
            StackPane cell = createExampleCell(target, example, routeConsumer, markExpressive);
            exampleCells.put(example, cell);
            examples.getChildren().add(cell);
        }
        examples.widthProperty().addListener(observable ->
                updateAdaptiveTileWidth(examples, EXAMPLE_CELL_MIN_WIDTH));

        M3SearchBar exampleSearch = new M3SearchBar("Search examples");
        exampleSearch.getStyleClass().add("catalog-example-search");
        exampleSearch.setAccessibleText("Search examples for " + target.name());
        exampleSearch.setMinWidth(0.0);
        exampleSearch.setPrefWidth(320.0);
        exampleSearch.setMaxWidth(440.0);
        exampleSearch.setText(browserState.query());

        M3SegmentedButton allFilter = createExampleFilterButton("All", "catalog-example-filter-all");
        M3SegmentedButton baselineFilter =
                createExampleFilterButton("Baseline", "catalog-example-filter-baseline");
        M3SegmentedButton expressiveFilter =
                createExampleFilterButton("Expressive", "catalog-example-filter-expressive");
        M3SegmentedButtonGroup filterGroup = new M3SegmentedButtonGroup();
        filterGroup.getStyleClass().add("catalog-example-filter-group");
        filterGroup.setAccessibleText("Example profile filter");
        filterGroup.getItems().addAll(allFilter, baselineFilter, expressiveFilter);
        filterGroup.setAllowEmptySelection(false);
        filterGroup.selectIndex(browserState.filterIndex());

        FlowPane browserControls = new FlowPane(12.0, 12.0);
        browserControls.getStyleClass().add("catalog-example-browser-controls");
        browserControls.getChildren().addAll(exampleSearch, filterGroup);

        M3Text resultSummary = new M3Text("", M3TextRole.LABEL_MEDIUM);
        resultSummary.getStyleClass().add("catalog-example-result-summary");

        VBox browser = new VBox(browserControls, resultSummary);
        browser.getStyleClass().add("catalog-example-browser");
        browser.setFillWidth(true);

        M3Text emptyTitle = new M3Text("No matching examples", M3TextRole.TITLE_MEDIUM);
        emptyTitle.getStyleClass().add("catalog-example-empty-title");
        M3Text emptyDescription = new M3Text(
                "Try another search or choose a different profile.",
                M3TextRole.BODY_MEDIUM
        );
        emptyDescription.getStyleClass().add("catalog-example-empty-description");
        emptyDescription.setWrapText(true);
        VBox emptyState = new VBox(emptyTitle, emptyDescription);
        emptyState.getStyleClass().add("catalog-example-empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setVisible(false);
        emptyState.setManaged(false);

        Runnable updateFilter = () -> updateExampleVisibility(
                exampleCells,
                exampleSearch.getText(),
                filterGroup.getSelectedIndex(),
                resultSummary,
                emptyState
        );
        VBox page = new VBox(
                reference,
                examplesHeading,
                examplesDescription,
                browser,
                examples,
                emptyState
        );
        page.getStyleClass().addAll("catalog-route-page", "catalog-component-page");
        page.setFillWidth(true);

        M3ScrollPane scrollPane = new M3ScrollPane(page);
        scrollPane.getStyleClass().addAll("catalog-route-scroll", "catalog-component-scroll");
        scrollPane.setMinSize(0.0, 0.0);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setVvalue(browserState.scrollPosition());
        exampleSearch.textProperty().addListener(observable -> {
            updateFilter.run();
            publishBrowserState(exampleSearch, filterGroup, scrollPane, stateConsumer);
        });
        filterGroup.selectedButtonProperty().addListener(observable -> {
            updateFilter.run();
            publishBrowserState(exampleSearch, filterGroup, scrollPane, stateConsumer);
        });
        scrollPane.vvalueProperty().addListener(observable ->
                publishBrowserState(exampleSearch, filterGroup, scrollPane, stateConsumer));
        updateFilter.run();
        return scrollPane;
    }

    /// Creates one segment for the example profile filter.
    ///
    /// @param label the visible segment label
    /// @param styleClass the stable Catalog style class
    /// @return a new unselected filter segment
    private static M3SegmentedButton createExampleFilterButton(String label, String styleClass) {
        M3SegmentedButton button = new M3SegmentedButton(label);
        button.getStyleClass().add(styleClass);
        return button;
    }

    /// Applies example search and profile filters without replacing example cells.
    ///
    /// Filter index `0` shows every example, index `1` shows Baseline examples, and index `2` shows Expressive
    /// examples. An unknown or temporarily empty selection behaves like the All filter.
    ///
    /// @param cells the persistent cells indexed by their example descriptors
    /// @param searchText the current search text
    /// @param filterIndex the selected profile-filter index
    /// @param resultSummary the label updated with the visible and total scenario counts
    /// @param emptyState the state shown when no examples match
    private static void updateExampleVisibility(
            Map<CatalogExample, StackPane> cells,
            String searchText,
            int filterIndex,
            M3Text resultSummary,
            VBox emptyState
    ) {
        String query = searchText.strip().toLowerCase(Locale.ROOT);
        int visibleCount = 0;
        for (Map.Entry<CatalogExample, StackPane> entry : cells.entrySet()) {
            CatalogExample example = entry.getKey();
            boolean profileMatches = switch (filterIndex) {
                case 1 -> !example.expressive();
                case 2 -> example.expressive();
                default -> true;
            };
            boolean queryMatches = query.isEmpty()
                    || example.name().toLowerCase(Locale.ROOT).contains(query)
                    || example.description().toLowerCase(Locale.ROOT).contains(query);
            boolean visible = profileMatches && queryMatches;
            entry.getValue().setVisible(visible);
            entry.getValue().setManaged(visible);
            if (visible) {
                visibleCount++;
            }
        }
        resultSummary.setText(visibleCount + " of " + cells.size() + " scenarios");
        boolean empty = visibleCount == 0;
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    /// Creates an outlined external-reference action.
    ///
    /// @param label the visible action label
    /// @param url the absolute URL opened by the action
    /// @param openExternal the external-document callback
    /// @return the configured reference action
    private static M3Button createReferenceButton(
            String label,
            String url,
            Consumer<String> openExternal
    ) {
        M3Button button = new M3Button(label, M3ButtonVariant.OUTLINED);
        button.getStyleClass().add("catalog-component-reference-action");
        button.setOnAction(event -> openExternal.accept(url));
        return button;
    }

    /// Creates a contextual specimen page around one real, interactive example.
    ///
    /// The page identifies the owning component, describes the scenario, labels its Baseline or Expressive profile,
    /// and provides direct back and source actions. The example factory is invoked once and the returned node is
    /// placed unchanged inside a centered live-sample surface. The route fits compact examples to the viewport width
    /// and preserves vertical scrolling for examples whose intrinsic height exceeds the window.
    ///
    /// @param component            the component that owns `example`
    /// @param example              the example to instantiate
    /// @param scrollPosition       the vertical scroll position to restore
    /// @param updateScrollPosition the consumer that retains subsequent vertical scroll changes
    /// @param navigateBack         the action that returns to the preceding route
    /// @param openExternal         the consumer that opens an absolute external URL
    /// @return a new centered, scrollable example view
    /// @throws NullPointerException     if an argument is `null`, or if the example factory returns `null`
    /// @throws IllegalArgumentException if `example` does not belong to `component`, or if `scrollPosition` is
    ///                                  non-finite or outside the range from `0.0` through `1.0`
    static Node createExample(
            CatalogComponent component,
            CatalogExample example,
            double scrollPosition,
            DoubleConsumer updateScrollPosition,
            Runnable navigateBack,
            Consumer<String> openExternal
    ) {
        CatalogComponent owner = Objects.requireNonNull(component, "component");
        CatalogExample target = Objects.requireNonNull(example, "example");
        if (!Double.isFinite(scrollPosition) || scrollPosition < 0.0 || scrollPosition > 1.0) {
            throw new IllegalArgumentException("scrollPosition must be between 0.0 and 1.0");
        }
        DoubleConsumer scrollConsumer = Objects.requireNonNull(updateScrollPosition, "updateScrollPosition");
        Runnable backAction = Objects.requireNonNull(navigateBack, "navigateBack");
        Consumer<String> externalConsumer = Objects.requireNonNull(openExternal, "openExternal");
        if (!owner.examples().contains(target)) {
            throw new IllegalArgumentException("example does not belong to component");
        }

        M3Text componentLabel = new M3Text(owner.name() + " component", M3TextRole.LABEL_LARGE);
        componentLabel.getStyleClass().add("catalog-example-component-label");

        M3Text title = new M3Text(target.name(), M3TextRole.HEADLINE_MEDIUM);
        title.getStyleClass().add("catalog-example-detail-title");
        title.setWrapText(true);

        M3Text description = new M3Text(target.description(), M3TextRole.BODY_MEDIUM);
        description.getStyleClass().add("catalog-example-detail-description");
        description.setWrapText(true);

        M3Text profile = new M3Text(
                target.expressive() ? "Expressive" : "Baseline",
                M3TextRole.LABEL_MEDIUM
        );
        profile.getStyleClass().addAll(
                "catalog-example-profile",
                target.expressive()
                        ? "catalog-example-profile-expressive"
                        : "catalog-example-profile-baseline"
        );

        M3Button backButton = new M3Button(
                "Back to " + owner.name(),
                CatalogIcons.createDirectional(CatalogIcons.ARROW_BACK),
                M3ButtonVariant.TEXT
        );
        backButton.getStyleClass().add("catalog-example-back-action");
        backButton.setOnAction(event -> backAction.run());

        M3Button sourceButton = new M3Button("View source", M3ButtonVariant.OUTLINED);
        sourceButton.getStyleClass().add("catalog-example-source-action");
        sourceButton.setOnAction(event -> externalConsumer.accept(target.sourceUrl()));

        FlowPane actions = new FlowPane(8.0, 8.0, backButton, sourceButton);
        actions.getStyleClass().add("catalog-example-detail-actions");

        VBox header = new VBox(componentLabel, title, description, profile, actions);
        header.getStyleClass().add("catalog-example-detail-header");
        header.setFillWidth(true);

        M3Text specimenHeading = new M3Text("Live specimen", M3TextRole.TITLE_LARGE);
        specimenHeading.getStyleClass().add("catalog-example-specimen-title");

        StackPane sampleContent = new StackPane(target.createContent());
        sampleContent.getStyleClass().add("catalog-sample-content");
        sampleContent.setAlignment(Pos.CENTER);
        sampleContent.setMinWidth(0.0);
        sampleContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane sampleSurface = new StackPane(sampleContent);
        sampleSurface.getStyleClass().add("catalog-sample-surface");
        sampleSurface.setAlignment(Pos.CENTER);
        sampleSurface.setMinWidth(0.0);
        sampleSurface.setMaxWidth(Double.MAX_VALUE);

        VBox page = new VBox(header, specimenHeading, sampleSurface);
        page.getStyleClass().addAll("catalog-route-page", "catalog-example-page");
        page.setFillWidth(true);
        page.setMinWidth(0.0);

        M3ScrollPane scrollPane = new M3ScrollPane(page);
        scrollPane.getStyleClass().addAll("catalog-route-scroll", "catalog-example-scroll");
        scrollPane.setMinSize(0.0, 0.0);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setVvalue(scrollPosition);
        scrollPane.vvalueProperty().addListener(observable ->
                scrollConsumer.accept(scrollPane.getVvalue()));
        scrollPane.viewportBoundsProperty().addListener(
                (observable, oldBounds, newBounds) -> page.setMinHeight(newBounds.getHeight())
        );
        return scrollPane;
    }

    /// Reports the current controls and vertical position of a browser route.
    ///
    /// A temporarily empty segmented-button selection is ignored because it is an intermediate control state rather
    /// than a restorable user choice.
    ///
    /// @param search the route's search control
    /// @param filterGroup the route's segmented filter
    /// @param scrollPane the route's vertical viewport
    /// @param updateState the consumer that retains the state
    private static void publishBrowserState(
            M3SearchBar search,
            M3SegmentedButtonGroup filterGroup,
            ScrollPane scrollPane,
            Consumer<CatalogBrowserState> updateState
    ) {
        int filterIndex = filterGroup.getSelectedIndex();
        if (filterIndex >= 0) {
            updateState.accept(new CatalogBrowserState(
                    search.getText(),
                    filterIndex,
                    scrollPane.getVvalue()
            ));
        }
    }

    /// Creates one adaptive-grid cell containing an actionable component card.
    ///
    /// @param component      the component represented by the card
    /// @param navigate       the consumer that handles route changes
    /// @param markExpressive whether an Expressive marker may be shown
    /// @return a resizable grid cell containing a new outlined card
    private static StackPane createComponentCard(
            CatalogComponent component,
            Consumer<CatalogRoute> navigate,
            boolean markExpressive,
            boolean favorite
    ) {
        BorderPane body = new BorderPane();
        body.getStyleClass().add("catalog-component-card-body");
        body.setPadding(new Insets(CARD_CONTENT_PADDING));
        body.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane icon = createSizedIcon(
                component.iconPath(),
                COMPONENT_CARD_ICON_SIZE,
                "catalog-component-card-icon"
        );
        body.setCenter(icon);

        M3Text name = new M3Text(component.name(), M3TextRole.BODY_SMALL);
        name.getStyleClass().add("catalog-component-card-name");
        name.setWrapText(true);
        body.setBottom(name);
        BorderPane.setAlignment(name, Pos.BOTTOM_LEFT);

        StackPane content = new StackPane(body);
        content.getStyleClass().add("catalog-component-card-content");
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        clipToCardShape(content);
        if (favorite) {
            M3SVGIcon favoriteIcon = CatalogIcons.create(CatalogIcons.FAVORITE);
            favoriteIcon.getStyleClass().add("catalog-component-card-favorite-icon");
            favoriteIcon.setIconSize(20.0);
            StackPane favoriteMarker = new StackPane(favoriteIcon);
            favoriteMarker.getStyleClass().add("catalog-component-card-favorite-marker");
            favoriteMarker.setMinSize(40.0, 40.0);
            favoriteMarker.setPrefSize(40.0, 40.0);
            favoriteMarker.setMaxSize(40.0, 40.0);
            favoriteMarker.setMouseTransparent(true);
            content.getChildren().add(favoriteMarker);
            StackPane.setAlignment(favoriteMarker, Pos.TOP_LEFT);
            StackPane.setMargin(favoriteMarker, new Insets(12.0));
        }
        if (markExpressive && component.hasExpressiveExamples()) {
            StackPane marker = createExpressiveMarker(
                    COMPONENT_BANNER_SIZE,
                    "catalog-component-card-expressive-marker"
            );
            content.getChildren().add(marker);
            StackPane.setAlignment(marker, Pos.TOP_RIGHT);
        }

        M3Card card = new M3Card(content, M3CardVariant.OUTLINED);
        card.getStyleClass().add("catalog-component-card");
        card.setAccessibleText(component.name());
        card.setContentPadding(0.0);
        card.setMinSize(0.0, 0.0);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        card.setOnAction(event -> navigate.accept(new CatalogRoute.Component(component)));

        StackPane cell = new StackPane(card);
        cell.getStyleClass().add("catalog-component-cell");
        cell.setPadding(new Insets(HOME_CELL_OUTER_PADDING));
        cell.setMinSize(0.0, HOME_CELL_HEIGHT);
        cell.setPrefSize(HOME_CELL_MIN_WIDTH, HOME_CELL_HEIGHT);
        cell.setMaxSize(Double.MAX_VALUE, HOME_CELL_HEIGHT);
        return cell;
    }

    /// Creates one full-width example card for a component detail page.
    ///
    /// @param component      the component that owns the example
    /// @param example        the example represented by the card
    /// @param navigate       the consumer that handles route changes
    /// @param markExpressive whether an Expressive marker may be shown
    /// @return a new actionable outlined card
    private static M3Card createExampleCard(
            CatalogComponent component,
            CatalogExample example,
            Consumer<CatalogRoute> navigate,
            boolean markExpressive
    ) {
        M3Text title = new M3Text(example.name(), M3TextRole.TITLE_SMALL);
        title.getStyleClass().add("catalog-example-card-title");

        M3Text description = new M3Text(example.description(), M3TextRole.BODY_SMALL);
        description.getStyleClass().add("catalog-example-card-description");
        description.setWrapText(true);

        VBox labels = new VBox(title, description);
        labels.getStyleClass().add("catalog-example-card-labels");
        labels.setMaxWidth(Double.MAX_VALUE);

        M3SVGIcon arrow = CatalogIcons.createDirectional(CatalogIcons.ARROW_FORWARD);
        arrow.getStyleClass().add("catalog-example-card-arrow");

        HBox trailing = new HBox(arrow);
        trailing.getStyleClass().add("catalog-example-card-trailing");
        trailing.setAlignment(Pos.CENTER_RIGHT);

        BorderPane row = new BorderPane();
        row.getStyleClass().add("catalog-example-card-row");
        row.setPadding(new Insets(CARD_CONTENT_PADDING));
        row.setCenter(labels);
        row.setRight(trailing);
        BorderPane.setAlignment(trailing, Pos.CENTER_RIGHT);

        StackPane content = new StackPane(row);
        content.getStyleClass().add("catalog-example-card-content");
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        clipToCardShape(content);
        if (markExpressive && example.expressive()) {
            StackPane marker = createExpressiveMarker(
                    EXAMPLE_BANNER_SIZE,
                    "catalog-example-card-expressive-marker"
            );
            content.getChildren().add(marker);
            StackPane.setAlignment(marker, Pos.TOP_RIGHT);
        }

        M3Card card = new M3Card(content, M3CardVariant.OUTLINED);
        card.getStyleClass().add("catalog-example-card");
        card.setAccessibleText(example.name());
        card.setContentPadding(0.0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnAction(event -> navigate.accept(new CatalogRoute.Example(component, example)));
        return card;
    }

    /// Creates one adaptive example-grid cell around an actionable example card.
    ///
    /// @param component the component that owns the example
    /// @param example the example represented by the card
    /// @param navigate the consumer that handles route changes
    /// @param markExpressive whether an Expressive marker may be shown
    /// @return a fixed-height resizable example cell
    private static StackPane createExampleCell(
            CatalogComponent component,
            CatalogExample example,
            Consumer<CatalogRoute> navigate,
            boolean markExpressive
    ) {
        M3Card card = createExampleCard(component, example, navigate, markExpressive);
        card.setMinSize(0.0, 0.0);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane cell = new StackPane(card);
        cell.getStyleClass().add("catalog-example-cell");
        cell.setPadding(new Insets(HOME_CELL_OUTER_PADDING));
        cell.setMinSize(0.0, EXAMPLE_CELL_HEIGHT);
        cell.setPrefSize(EXAMPLE_CELL_MIN_WIDTH, EXAMPLE_CELL_HEIGHT);
        cell.setMaxSize(Double.MAX_VALUE, EXAMPLE_CELL_HEIGHT);
        return cell;
    }

    /// Creates a centered icon holder with a Material SVG icon rendered at the requested size.
    ///
    /// @param path       the SVG path content
    /// @param size       the width and height of the icon and its holder
    /// @param styleClass the view-specific style class added to the icon
    /// @return a fixed-size icon holder
    private static StackPane createSizedIcon(String path, double size, String styleClass) {
        M3SVGIcon icon = CatalogIcons.create(path);
        icon.getStyleClass().add(styleClass);
        icon.setIconSize(size);

        StackPane holder = new StackPane(icon);
        holder.getStyleClass().add(styleClass + "-holder");
        holder.setAlignment(Pos.CENTER);
        holder.setMinSize(size, size);
        holder.setPrefSize(size, size);
        holder.setMaxSize(size, size);
        return holder;
    }

    /// Creates the diagonal corner banner used to identify Expressive content.
    ///
    /// @param size              the square banner size before rotation
    /// @param contextStyleClass the location-specific style class added to the banner
    /// @return a new mouse-transparent Expressive banner
    private static StackPane createExpressiveMarker(double size, String contextStyleClass) {
        M3Text label = new M3Text("Expressive", M3TextRole.LABEL_SMALL);
        label.getStyleClass().add("catalog-expressive-marker-label");

        StackPane marker = new StackPane(label);
        marker.getStyleClass().addAll("catalog-expressive-marker", contextStyleClass);
        marker.setAlignment(Pos.BOTTOM_CENTER);
        marker.setMinSize(size, size);
        marker.setPrefSize(size, size);
        marker.setMaxSize(size, size);
        marker.setRotate(45.0);
        marker.setTranslateX(size / 2.0);
        marker.setTranslateY(-size / 2.0);
        marker.setMouseTransparent(true);
        return marker;
    }

    /// Clips card content to the same rounded rectangle used by an M3 Card container.
    ///
    /// JavaFX parents do not clip overflowing children by default. Catalog corner banners deliberately extend past
    /// their content bounds before clipping, matching the diagonal banner composition used by the Compose Catalog.
    ///
    /// @param content the card content layer to clip
    private static void clipToCardShape(StackPane content) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(content.widthProperty());
        clip.heightProperty().bind(content.heightProperty());
        clip.setArcWidth(CARD_CORNER_RADIUS * 2.0);
        clip.setArcHeight(CARD_CORNER_RADIUS * 2.0);
        content.setClip(clip);
    }

}
