// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/// Builds the three content views used by the Compose Material Catalog-style navigation hierarchy.
///
/// The builders are stateless. Each invocation creates a fresh node tree and reports navigation requests through
/// the supplied route consumer. Layout nodes expose catalog-specific style classes so spacing, shape, typography,
/// and responsive refinements can be supplied by the Catalog stylesheet without embedding theme colors here.
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
    private static final double COMPONENT_PAGE_ICON_SIZE = 108.0;

    /// The uniform inner padding used by Catalog cards.
    private static final double CARD_CONTENT_PADDING = 16.0;

    /// The diagonal Expressive banner size used by home cards.
    private static final double COMPONENT_BANNER_SIZE = 80.0;

    /// The diagonal Expressive banner size used by example cards.
    private static final double EXAMPLE_BANNER_SIZE = 64.0;

    /// The standard Material card corner radius, in logical pixels.
    private static final double CARD_CORNER_RADIUS = 12.0;

    /// The coordinate size expected by the Material SVG paths used by the Catalog.
    private static final double MATERIAL_ICON_VIEWPORT_SIZE = 24.0;

    /// Prevents utility class instantiation.
    private CatalogViews() {
    }

    /// Creates the alphabetical, adaptive component grid displayed by the Catalog home route.
    ///
    /// Components without Expressive examples are omitted when `expressiveOnly` is `true`. Component cards use
    /// an outlined Material surface and navigate to the corresponding [CatalogRoute.Component] when activated.
    /// The returned scroll pane fits its content to the viewport width, allowing the tile pane to recompute its
    /// column count as the available width changes.
    ///
    /// @param components the components available to the Catalog
    /// @param navigate the consumer that handles route changes
    /// @param expressiveOnly whether only components with Expressive examples are shown
    /// @param markExpressive whether components with Expressive examples display a corner marker
    /// @return a new scrollable home view
    /// @throws NullPointerException if `components`, an element of `components`, or `navigate` is `null`
    static Node createHome(
            List<CatalogComponent> components,
            Consumer<CatalogRoute> navigate,
            boolean expressiveOnly,
            boolean markExpressive
    ) {
        Consumer<CatalogRoute> routeConsumer = Objects.requireNonNull(navigate, "navigate");
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
        for (CatalogComponent component : sortedComponents) {
            grid.getChildren().add(createComponentCard(component, routeConsumer, markExpressive));
        }
        grid.widthProperty().addListener(observable -> updateHomeTileWidth(grid));

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.getStyleClass().addAll("catalog-route-scroll", "catalog-home-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        M3ScrollPanes.style(scrollPane);
        return scrollPane;
    }

    /// Distributes adaptive-grid cells across the available row width.
    ///
    /// @param grid the home component grid
    private static void updateHomeTileWidth(TilePane grid) {
        Insets insets = grid.getInsets();
        double availableWidth = grid.getWidth() - insets.getLeft() - insets.getRight();
        if (availableWidth <= 0.0) {
            return;
        }

        int columns = Math.max(1, (int) Math.floor(availableWidth / HOME_CELL_MIN_WIDTH));
        double outputScale = grid.getScene() != null && grid.getScene().getWindow() != null
                ? grid.getScene().getWindow().getOutputScaleX()
                : 1.0;
        double tileWidth = Math.floor(availableWidth * outputScale / columns) / outputScale;
        if (Math.abs(grid.getPrefTileWidth() - tileWidth) >= 0.01) {
            grid.setPrefTileWidth(tileWidth);
        }
    }

    /// Creates a component detail page with its overview and navigable example list.
    ///
    /// The page contains the component icon, a description section, and one full-width outlined card for each
    /// example. Activating an example card reports a [CatalogRoute.Example] through `navigate`.
    ///
    /// @param component the component described by the page
    /// @param navigate the consumer that handles route changes
    /// @param markExpressive whether Expressive examples display a marker
    /// @return a new scrollable component detail view
    /// @throws NullPointerException if `component` or `navigate` is `null`
    static Node createComponent(
            CatalogComponent component,
            Consumer<CatalogRoute> navigate,
            boolean markExpressive
    ) {
        CatalogComponent target = Objects.requireNonNull(component, "component");
        Consumer<CatalogRoute> routeConsumer = Objects.requireNonNull(navigate, "navigate");

        StackPane iconRow = new StackPane(createSizedIcon(
                target.iconPath(),
                COMPONENT_PAGE_ICON_SIZE,
                "catalog-component-page-icon"
        ));
        iconRow.getStyleClass().add("catalog-component-page-icon-row");
        iconRow.setAlignment(Pos.CENTER);
        iconRow.setMaxWidth(Double.MAX_VALUE);

        M3Text descriptionHeading = new M3Text("Description", M3TextRole.TITLE_MEDIUM);
        descriptionHeading.getStyleClass().addAll("catalog-component-section-title", "catalog-description-title");

        M3Text description = new M3Text(target.description(), M3TextRole.BODY_MEDIUM);
        description.getStyleClass().add("catalog-component-description");
        description.setWrapText(true);
        description.setMaxWidth(Double.MAX_VALUE);

        M3Text examplesHeading = new M3Text("Examples", M3TextRole.TITLE_MEDIUM);
        examplesHeading.getStyleClass().addAll("catalog-component-section-title", "catalog-examples-title");

        VBox examples = new VBox();
        examples.getStyleClass().add("catalog-example-card-list");
        examples.setFillWidth(true);
        for (CatalogExample example : target.examples()) {
            examples.getChildren().add(createExampleCard(target, example, routeConsumer, markExpressive));
        }

        VBox page = new VBox(iconRow, descriptionHeading, description, examplesHeading, examples);
        page.getStyleClass().addAll("catalog-route-page", "catalog-component-page");
        page.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.getStyleClass().addAll("catalog-route-scroll", "catalog-component-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        M3ScrollPanes.style(scrollPane);
        return scrollPane;
    }

    /// Creates an example route that displays one real, interactive example centered in the available area.
    ///
    /// No card, sample surface, title, or explanatory wrapper is added around the example. The returned stack pane
    /// exists only to provide centering and a stable route-level style hook.
    ///
    /// @param component the component that owns `example`
    /// @param example the example to instantiate
    /// @return a new centered example view
    /// @throws NullPointerException if `component` or `example` is `null`, or if the example factory returns `null`
    /// @throws IllegalArgumentException if `example` does not belong to `component`
    static Node createExample(CatalogComponent component, CatalogExample example) {
        CatalogComponent owner = Objects.requireNonNull(component, "component");
        CatalogExample target = Objects.requireNonNull(example, "example");
        if (!owner.examples().contains(target)) {
            throw new IllegalArgumentException("example does not belong to component");
        }

        StackPane page = new StackPane(target.createContent());
        page.getStyleClass().addAll("catalog-route-page", "catalog-example-page");
        page.setAlignment(Pos.CENTER);
        return page;
    }

    /// Creates one adaptive-grid cell containing an actionable component card.
    ///
    /// @param component the component represented by the card
    /// @param navigate the consumer that handles route changes
    /// @param markExpressive whether an Expressive marker may be shown
    /// @return a resizable grid cell containing a new outlined card
    private static StackPane createComponentCard(
            CatalogComponent component,
            Consumer<CatalogRoute> navigate,
            boolean markExpressive
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
    /// @param component the component that owns the example
    /// @param example the example represented by the card
    /// @param navigate the consumer that handles route changes
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

        SVGPath arrow = CatalogIcons.create(CatalogIcons.ARROW_FORWARD);
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

    /// Creates a centered icon holder with a Material SVG path scaled to the requested size.
    ///
    /// @param path the SVG path content
    /// @param size the width and height of the icon and its holder
    /// @param styleClass the view-specific style class added to the icon
    /// @return a fixed-size icon holder
    private static StackPane createSizedIcon(String path, double size, String styleClass) {
        SVGPath icon = CatalogIcons.create(path);
        icon.getStyleClass().add(styleClass);
        double scale = size / MATERIAL_ICON_VIEWPORT_SIZE;
        icon.setScaleX(scale);
        icon.setScaleY(scale);

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
    /// @param size the square banner size before rotation
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
