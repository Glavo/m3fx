// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3NavigationBarSkin;
import org.glavo.m3fx.skins.M3NavigationDrawerSkin;
import org.glavo.m3fx.skins.M3NavigationItemSkin;
import org.glavo.m3fx.skins.M3NavigationRailSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies navigation bar and rail layout, selection, and expansion.
@NotNullByDefault
final class M3NavigationContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that navigation item component token properties are styleable from CSS.
    @Test
    void navigationItemTokensAreStyleable() {
        M3NavigationItem item = new M3NavigationItem("Home");
        item.setStyle(
                "-m3-container-height: 84px; "
                        + "-m3-item-width: 92px; "
                        + "-m3-indicator-width: 70px; "
                        + "-m3-indicator-height: 34px; "
                        + "-m3-indicator-shape: 17px; "
                        + "-m3-content-spacing: 6px;"
        );

        applyCss(item);

        assertEquals(84.0, item.getContainerHeight(), 0.0001);
        assertEquals(92.0, item.getItemWidth(), 0.0001);
        assertEquals(70.0, item.getIndicatorWidth(), 0.0001);
        assertEquals(34.0, item.getIndicatorHeight(), 0.0001);
        assertEquals(17.0, item.getIndicatorShape(), 0.0001);
        assertEquals(6.0, item.getContentSpacing(), 0.0001);
        assertEquals(92.0, item.prefWidth(-1.0), 0.0001);
        assertEquals(84.0, item.prefHeight(-1.0), 0.0001);
        assertInstanceOf(M3NavigationItemSkin.class, item.getSkin());
    }

    /// Verifies that navigation items expose badge content in the graphic slot.
    @Test
    void navigationItemShowsBadge() {
        M3Badge badge = new M3Badge("3");
        badge.maxWidthProperty().bind(badge.prefWidthProperty());
        badge.maxHeightProperty().bind(badge.prefHeightProperty());
        M3Icon graphic = new M3Icon("I");
        M3NavigationItem item = createNavigationItem("Inbox", graphic, badge, true);

        applyCss(item);

        assertTrue(item.isSelected());
        assertEquals(badge, item.getBadge());
        StackPane badgeSlot = assertInstanceOf(StackPane.class, item.lookup(".m3-navigation-item-badge"));
        assertEquals(List.of(badge), badgeSlot.getChildren());
        assertTrue(badge.maxWidthProperty().isBound());
        assertTrue(badge.maxHeightProperty().isBound());
        assertFalse(badge.getStyleClass().contains("m3-navigation-item-badge"));

        M3Badge replacement = new M3Badge();
        item.setBadge(replacement);

        assertEquals(replacement, item.getBadge());
        assertSame(badgeSlot, item.lookup(".m3-navigation-item-badge"));
        assertEquals(List.of(replacement), badgeSlot.getChildren());

        item.setBadge(null);

        assertNull(item.getBadge());
        assertTrue(badgeSlot.getChildren().isEmpty());
        assertFalse(replacement.getStyleClass().contains("m3-navigation-item-badge"));

        M3Icon replacementGraphic = new M3Icon("R");
        item.setGraphic(replacementGraphic);
        assertFalse(graphic.getStyleClass().contains("m3-navigation-item-graphic-node"));
        item.setGraphic(null);
        assertFalse(replacementGraphic.getStyleClass().contains("m3-navigation-item-graphic-node"));

        M3Icon disposedGraphic = new M3Icon("D");
        M3Badge disposedBadge = new M3Badge("4");
        M3NavigationItem disposedItem = createNavigationItem("Disposed", disposedGraphic, disposedBadge, false);
        applyCss(disposedItem);
        disposedItem.getSkin().dispose();
        assertNull(disposedGraphic.getParent());
        assertNull(disposedBadge.getParent());
    }

    /// Verifies that navigation item badges stay anchored to logical end in right-to-left layouts.
    @Test
    void navigationItemMirrorsBadgeForRightToLeft() {
        M3Badge badge = new M3Badge("3");
        M3NavigationItem item = createNavigationItem("Inbox", new M3Icon("I"), badge, true);
        item.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(item);
        Scene scene = new Scene(root, 120.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        item.resize(80.0, 80.0);
        item.layout();

        StackPane badgeContainer = assertInstanceOf(
                StackPane.class,
                item.lookup(".m3-navigation-item-badge-container")
        );
        assertEquals(Pos.TOP_LEFT, badgeContainer.getAlignment());
        Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());
        Node indicator = Objects.requireNonNull(item.lookup(".m3-navigation-item-indicator"), "indicator");
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertTrue(badgeBounds.getCenterX() < indicatorBounds.getCenterX(),
                () -> "badge=" + badgeBounds + ", indicator=" + indicatorBounds);
        assertEquals(0.0, badge.getTranslateX(), 0.0001);
    }

    /// Verifies that navigation bars group items and keep a selected item.
    @Test
    void navigationBarGroupsItemsAndKeepsSelection() {
        M3NavigationItem home = new M3NavigationItem("Home");
        M3NavigationItem search = new M3NavigationItem("Search");
        M3NavigationBar navigationBar = navigationBar(home, search);

        assertTrue(home.isSelected());
        assertEquals(home, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationBar.getSelectedItems());
        assertEquals(0, navigationBar.getSelectedIndex());

        navigationBar.selectIndex(1);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationBar.getSelectedItems());
        assertEquals(1, navigationBar.getSelectedIndex());

        search.fire();

        assertTrue(search.isSelected());
        assertEquals(search, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationBar.getSelectedItems());
        assertThrows(UnsupportedOperationException.class, () -> navigationBar.getSelectedItems().add(home));

        navigationBar.selectNext();

        assertEquals(home, navigationBar.getSelectedItem());
        assertTrue(home.isSelected());
        assertFalse(search.isSelected());

        navigationBar.selectPrevious();

        assertEquals(search, navigationBar.getSelectedItem());
        assertFalse(home.isSelected());
        assertTrue(search.isSelected());

        navigationBar.setAllowEmptySelection(true);
        navigationBar.clearSelection();

        assertNull(navigationBar.getSelectedItem());
        assertTrue(navigationBar.getSelectedItems().isEmpty());
        assertEquals(-1, navigationBar.getSelectedIndex());
        assertFalse(home.isSelected());
        assertFalse(search.isSelected());

        navigationBar.setAllowEmptySelection(false);

        assertEquals(home, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationBar.getSelectedItems());
    }

    /// Verifies that adaptive navigation bar layouts propagate to existing and newly added destinations.
    @Test
    void navigationBarPropagatesAdaptiveItemLayout() {
        M3NavigationItem first = new M3NavigationItem("Home");
        M3NavigationItem second = new M3NavigationItem("Search");
        M3NavigationBar bar = new M3NavigationBar();
        bar.getItems().add(first);

        assertEquals(M3NavigationItemLayout.VERTICAL, first.getItemLayout());

        bar.setItemLayout(M3NavigationItemLayout.HORIZONTAL);
        bar.getItems().add(second);

        assertEquals(M3NavigationItemLayout.HORIZONTAL, first.getItemLayout());
        assertEquals(M3NavigationItemLayout.HORIZONTAL, second.getItemLayout());

        bar.setItemLayout(M3NavigationItemLayout.VERTICAL);

        assertEquals(M3NavigationItemLayout.VERTICAL, first.getItemLayout());
        assertEquals(M3NavigationItemLayout.VERTICAL, second.getItemLayout());
    }

    /// Verifies that expanded navigation rails propagate horizontal destination layouts and accessibility state.
    @Test
    void navigationRailPropagatesExpandedItemLayout() {
        M3NavigationItem first = new M3NavigationItem("Home");
        M3NavigationItem second = new M3NavigationItem("Search");
        M3NavigationRail rail = new M3NavigationRail();
        Label header = new Label("Rail actions");
        rail.getItems().add(first);

        assertFalse(rail.isExpanded());
        assertNull(rail.getHeader());
        assertFalse(rail.isFullWidthIndicator());
        assertFalse(rail.isNarrow());
        assertFalse(rail.isHideWhenCollapsed());
        assertFalse(rail.isItemsCentered());
        assertEquals(M3NavigationItemLayout.VERTICAL, first.getItemLayout());

        rail.setHeader(header);
        rail.setHeaderSpacing(48.0);
        rail.setFullWidthIndicator(true);
        rail.setNarrow(true);
        rail.setItemsCentered(true);

        assertSame(header, rail.getHeader());
        assertSame(header, rail.headerProperty().get());
        assertEquals(48.0, rail.getHeaderSpacing(), 0.0001);
        assertTrue(rail.isFullWidthIndicator());
        assertTrue(rail.isNarrow());
        assertTrue(rail.isItemsCentered());
        assertTrue(rail.getPseudoClassStates().contains(PseudoClass.getPseudoClass("full-width-indicator")));
        assertTrue(rail.getPseudoClassStates().contains(PseudoClass.getPseudoClass("narrow")));
        assertTrue(rail.getPseudoClassStates().contains(PseudoClass.getPseudoClass("items-centered")));

        rail.setExpanded(true);
        rail.getItems().add(second);

        assertTrue(rail.isExpanded());
        assertEquals(Boolean.TRUE, rail.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(M3NavigationItemLayout.HORIZONTAL, first.getItemLayout());
        assertEquals(M3NavigationItemLayout.HORIZONTAL, second.getItemLayout());

        rail.setExpanded(false);

        assertEquals(M3NavigationItemLayout.VERTICAL, first.getItemLayout());
        assertEquals(M3NavigationItemLayout.VERTICAL, second.getItemLayout());
    }

    /// Verifies that navigation rails group items and keep a selected item.
    @Test
    void navigationRailGroupsItemsAndKeepsSelection() {
        M3NavigationItem home = new M3NavigationItem("Home");
        M3NavigationItem search = new M3NavigationItem("Search");
        M3NavigationRail navigationRail = navigationRail(home, search);

        assertTrue(home.isSelected());
        assertEquals(home, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationRail.getSelectedItems());
        assertEquals(0, navigationRail.getSelectedIndex());

        navigationRail.selectIndex(1);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationRail.getSelectedItems());
        assertEquals(1, navigationRail.getSelectedIndex());

        search.fire();

        assertTrue(search.isSelected());
        assertEquals(search, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationRail.getSelectedItems());

        navigationRail.selectNext();

        assertEquals(home, navigationRail.getSelectedItem());
        assertTrue(home.isSelected());
        assertFalse(search.isSelected());

        navigationRail.selectPrevious();

        assertEquals(search, navigationRail.getSelectedItem());
        assertFalse(home.isSelected());
        assertTrue(search.isSelected());

        navigationRail.getItems().remove(search);

        assertFalse(search.isSelected());
        assertTrue(home.isSelected());
        assertEquals(home, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationRail.getSelectedItems());

        navigationRail.setAllowEmptySelection(true);
        navigationRail.clearSelection();

        assertNull(navigationRail.getSelectedItem());
        assertTrue(navigationRail.getSelectedItems().isEmpty());
        assertEquals(-1, navigationRail.getSelectedIndex());
        assertFalse(home.isSelected());
    }

    /// Verifies that navigation rail token rules override bar item metrics.
    @Test
    void navigationRailAppliesRailItemMetrics() {
        M3NavigationItem home = new M3NavigationItem("Home");
        M3NavigationRail navigationRail = navigationRail(home);
        navigationRail.setStyle("-m3-item-spacing: 12px;");
        Pane root = new Pane(navigationRail);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(80.0, navigationRail.getCollapsedContainerWidth(), 0.0001);
        assertEquals(12.0, navigationRail.getItemSpacing(), 0.0001);
        assertEquals(80.0, home.getItemWidth(), 0.0001);
        assertEquals(56.0, home.getIndicatorWidth(), 0.0001);
    }

    /// Verifies official expressive collapsed widths, expanded bounds, destination alignment, and immersive hiding.
    @Test
    void navigationRailSupportsExpressiveConfigurations() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationItem home = new M3NavigationItem("Home");
            M3NavigationItem search = new M3NavigationItem("Search");
            M3NavigationItem settings = new M3NavigationItem("Settings");
            M3NavigationRail rail = navigationRail(home, search, settings);
            Region header = new Region();
            header.setMinSize(40.0, 40.0);
            header.setPrefSize(40.0, 40.0);
            header.setMaxSize(40.0, 40.0);
            rail.setHeader(header);
            rail.setManaged(false);

            Pane root = new Pane(rail);
            Scene scene = new Scene(root, 480.0, 620.0);
            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            rail.setAnimationsEnabled(false);

            assertEquals(96.0, rail.getCollapsedContainerWidth(), 0.01);
            assertEquals(220.0, rail.getExpandedMinimumContainerWidth(), 0.01);
            assertEquals(280.0, rail.getExpandedContainerWidth(), 0.01);
            assertEquals(360.0, rail.getExpandedMaximumContainerWidth(), 0.01);
            assertEquals(44.0, rail.getPadding().getTop(), 0.01);
            assertEquals(0.0, rail.getPadding().getBottom(), 0.01);
            assertEquals(96.0, rail.prefWidth(-1.0), 0.01);

            rail.setNarrow(true);
            root.applyCss();
            assertEquals(80.0, rail.getCollapsedContainerWidth(), 0.01);
            assertEquals(80.0, rail.prefWidth(-1.0), 0.01);

            rail.setItemsCentered(true);
            rail.resizeRelocate(20.0, 20.0, 80.0, 520.0);
            root.layout();
            rail.layout();
            home.layout();
            search.layout();
            settings.layout();
            Bounds headerBounds = rail.sceneToLocal(header.localToScene(header.getLayoutBounds()));
            Bounds firstBounds = rail.sceneToLocal(home.localToScene(home.getLayoutBounds()));
            Bounds lastBounds = rail.sceneToLocal(settings.localToScene(settings.getLayoutBounds()));
            double destinationAreaStart = headerBounds.getMaxY() + rail.getHeaderSpacing();
            double destinationAreaEnd = rail.getHeight() - rail.getInsets().getBottom();
            assertEquals(
                    (destinationAreaStart + destinationAreaEnd) / 2.0,
                    (firstBounds.getMinY() + lastBounds.getMaxY()) / 2.0,
                    0.51
            );

            rail.setExpanded(true);
            root.applyCss();
            assertEquals(220.0, rail.minWidth(-1.0), 0.01);
            assertEquals(280.0, rail.prefWidth(-1.0), 0.01);
            assertEquals(360.0, rail.maxWidth(-1.0), 0.01);
            assertEquals(44.0, rail.getPadding().getTop(), 0.01);
            assertEquals(20.0, rail.getPadding().getBottom(), 0.01);

            rail.setExpandedContainerWidth(100.0);
            assertEquals(220.0, rail.prefWidth(-1.0), 0.01,
                    "preferred expanded width should be clamped to the official minimum");
            rail.setExpandedContainerWidth(500.0);
            assertEquals(360.0, rail.prefWidth(-1.0), 0.01,
                    "preferred expanded width should be clamped to the official maximum");
            rail.setExpandedContainerWidth(280.0);
            rail.setHideWhenCollapsed(true);
            rail.setExpanded(false);
            root.applyCss();
            root.layout();
            rail.layout();

            assertTrue(rail.getPseudoClassStates().contains(PseudoClass.getPseudoClass("hide-when-collapsed")));
            assertEquals(0.0, rail.minWidth(-1.0), 0.01);
            assertEquals(0.0, rail.prefWidth(-1.0), 0.01);
            assertEquals(0.0, rail.maxWidth(-1.0), 0.01);
            Parent contentLayer = Objects.requireNonNull(
                    Objects.requireNonNull(home.getParent(), "navigation container").getParent(),
                    "rail content layer"
            );
            assertFalse(contentLayer.isVisible(), "hidden rail content must leave traversal and rendering");

            rail.setExpanded(true);
            root.applyCss();
            assertEquals(280.0, rail.prefWidth(-1.0), 0.01);
            assertTrue(contentLayer.isVisible());
            rail.setAnimationsEnabled(true);
        });
    }

    /// Verifies standard and modal expanded navigation rail surface treatments.
    @Test
    void navigationRailResolvesExpandedVariantSurface() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationRail navigationRail = navigationRail(new M3NavigationItem("Home"));
            Pane root = new Pane(navigationRail);
            Scene scene = new Scene(root, 420.0, 360.0);
            M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT);
            double modalShape = theme.tokens().shapeTokens().large();

            M3ThemeManager.install(scene, theme);
            root.applyCss();

            PseudoClass standard = PseudoClass.getPseudoClass("standard");
            PseudoClass modal = PseudoClass.getPseudoClass("modal");
            assertEquals(M3NavigationRailVariant.STANDARD, navigationRail.getVariant());
            assertTrue(navigationRail.getPseudoClassStates().contains(standard));
            assertFalse(navigationRail.getPseudoClassStates().contains(modal));
            assertRegionFill(
                    navigationRail,
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE)
            );
            assertNull(navigationRail.getEffect());
            assertRegionRadii(navigationRail, 0.0, 0.0, 0.0, 0.0);

            navigationRail.setVariant(M3NavigationRailVariant.MODAL);
            root.applyCss();
            assertFalse(navigationRail.getPseudoClassStates().contains(standard));
            assertTrue(navigationRail.getPseudoClassStates().contains(modal));
            assertRegionFill(
                    navigationRail,
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE)
            );
            assertNull(navigationRail.getEffect());
            assertRegionRadii(navigationRail, 0.0, 0.0, 0.0, 0.0);

            navigationRail.setExpanded(true);
            root.applyCss();
            assertRegionFill(
                    navigationRail,
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE_CONTAINER)
            );
            DropShadow shadow = assertDropShadow(navigationRail);
            assertEquals(3.0, shadow.getRadius(), 0.01);
            assertEquals(2.0, shadow.getOffsetY(), 0.01);
            assertRegionRadii(navigationRail, modalShape, modalShape, modalShape, modalShape);

            navigationRail.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            assertTrue(navigationRail.getPseudoClassStates().contains(PseudoClass.getPseudoClass("rtl")));
            assertRegionRadii(navigationRail, modalShape, modalShape, modalShape, modalShape);

            navigationRail.variantProperty().set(null);
            assertEquals(M3NavigationRailVariant.STANDARD, navigationRail.getVariant());
        });
    }

    /// Verifies that an expanded navigation-rail header follows the logical start edge exactly once.
    @Test
    void navigationRailHeaderMirrorsOnceAcrossRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Region header = new Region();
            header.setMinSize(40.0, 40.0);
            header.setPrefSize(40.0, 40.0);
            header.setMaxSize(40.0, 40.0);
            M3NavigationRail rail = navigationRail(new M3NavigationItem("Home"));
            rail.setHeader(header);
            rail.setExpanded(true);
            rail.setManaged(false);
            Pane root = new Pane(rail);
            Scene scene = new Scene(root, 320.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            rail.resizeRelocate(20.0, 20.0, 220.0, 320.0);
            root.layout();
            rail.layout();

            Bounds railBounds = rail.localToScene(rail.getBoundsInLocal());
            Bounds headerBounds = header.localToScene(header.getBoundsInLocal());
            assertEquals(16.0, headerBounds.getMinX() - railBounds.getMinX(), 0.01);

            rail.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            rail.layout();

            railBounds = rail.localToScene(rail.getBoundsInLocal());
            headerBounds = header.localToScene(header.getBoundsInLocal());
            assertEquals(16.0, railBounds.getMaxX() - headerBounds.getMaxX(), 0.01);

            rail.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            root.layout();
            rail.layout();

            railBounds = rail.localToScene(rail.getBoundsInLocal());
            headerBounds = header.localToScene(header.getBoundsInLocal());
            assertEquals(16.0, headerBounds.getMinX() - railBounds.getMinX(), 0.01);
        });
    }

    /// Verifies compact equal distribution, medium fixed widths, and right-to-left ordering.
    @Test
    void navigationBarAppliesAdaptiveWidthDistribution() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationItem home = new M3NavigationItem("Home");
            M3NavigationItem search = new M3NavigationItem("Search");
            M3NavigationItem profile = new M3NavigationItem("Profile");
            M3NavigationItem settings = new M3NavigationItem("Settings");
            M3NavigationBar navigationBar = navigationBar(home, search, profile, settings);
            navigationBar.setManaged(false);
            Pane root = new Pane(navigationBar);
            Scene scene = new Scene(root, 840.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            navigationBar.resizeRelocate(20.0, 20.0, 640.0, 64.0);
            root.layout();

            Bounds navigationBounds = navigationBar.localToScene(navigationBar.getLayoutBounds());
            Bounds homeBounds = home.localToScene(home.getBoundsInLocal());
            Bounds searchBounds = search.localToScene(search.getBoundsInLocal());
            Bounds profileBounds = profile.localToScene(profile.getBoundsInLocal());
            Bounds settingsBounds = settings.localToScene(settings.getBoundsInLocal());
            assertEquals(6.0, navigationBar.getItemSpacing(), 0.01);
            double compactContentWidth = 640.0 - 16.0;
            double compactItemWidth = (compactContentWidth - 3.0 * navigationBar.getItemSpacing()) / 4.0;
            assertEquals(compactItemWidth, homeBounds.getWidth(), 0.51);
            assertEquals(compactItemWidth, searchBounds.getWidth(), 0.51);
            assertEquals(compactItemWidth, profileBounds.getWidth(), 0.51);
            assertEquals(compactItemWidth, settingsBounds.getWidth(), 0.51);
            assertEquals(8.0, homeBounds.getMinX() - navigationBounds.getMinX(), 0.01);
            assertEquals(8.0, navigationBounds.getMaxX() - settingsBounds.getMaxX(), 0.01);
            assertEquals(navigationBar.getItemSpacing(), searchBounds.getMinX() - homeBounds.getMaxX(), 0.01);
            assertEquals(navigationBar.getItemSpacing(), profileBounds.getMinX() - searchBounds.getMaxX(), 0.01);
            assertEquals(navigationBar.getItemSpacing(), settingsBounds.getMinX() - profileBounds.getMaxX(), 0.01);

            navigationBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            homeBounds = home.localToScene(home.getBoundsInLocal());
            settingsBounds = settings.localToScene(settings.getBoundsInLocal());
            assertTrue(homeBounds.getCenterX() > settingsBounds.getCenterX());

            navigationBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            navigationBar.setItemLayout(M3NavigationItemLayout.HORIZONTAL);
            navigationBar.resize(800.0, 64.0);
            root.applyCss();
            root.layout();
            assertEquals(0.0, navigationBar.getItemSpacing(), 0.01,
                    "horizontal flexible navigation items should not retain compact between-space");
            navigationBounds = navigationBar.localToScene(navigationBar.getLayoutBounds());
            homeBounds = home.localToScene(home.getBoundsInLocal());
            searchBounds = search.localToScene(search.getBoundsInLocal());
            profileBounds = profile.localToScene(profile.getBoundsInLocal());
            settingsBounds = settings.localToScene(settings.getBoundsInLocal());
            assertEquals(160.0, homeBounds.getWidth(), 0.01);
            assertEquals(160.0, searchBounds.getWidth(), 0.01);
            assertEquals(160.0, profileBounds.getWidth(), 0.01);
            assertEquals(160.0, settingsBounds.getWidth(), 0.01);
            assertEquals(80.0, homeBounds.getMinX() - navigationBounds.getMinX(), 0.01);
            assertEquals(80.0, navigationBounds.getMaxX() - settingsBounds.getMaxX(), 0.01);
        });
    }

    /// Verifies content-hugging and optional full-width expanded rail indicators at both specification limits.
    @Test
    void expandedNavigationRailSupportsContentAndFullWidthIndicators() {
        FxTestUtils.runOnFxThread(() -> {
            Region homeGraphic = new Region();
            homeGraphic.setMinSize(24.0, 24.0);
            homeGraphic.setPrefSize(24.0, 24.0);
            homeGraphic.setMaxSize(24.0, 24.0);
            Region searchGraphic = new Region();
            searchGraphic.setMinSize(24.0, 24.0);
            searchGraphic.setPrefSize(24.0, 24.0);
            searchGraphic.setMaxSize(24.0, 24.0);
            M3NavigationItem home = new M3NavigationItem("Home", homeGraphic);
            M3NavigationItem search = new M3NavigationItem("Search", searchGraphic);
            search.setBadge(new M3Badge("3"));
            M3NavigationRail navigationRail = navigationRail(home, search);
            navigationRail.setExpandedContainerWidth(220.0);
            navigationRail.setExpanded(true);
            navigationRail.setManaged(false);
            Pane root = new Pane(navigationRail);
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            navigationRail.resizeRelocate(20.0, 20.0, 220.0, 320.0);
            root.layout();
            navigationRail.layout();
            home.layout();

            assertEquals(220.0, navigationRail.getExpandedContainerWidth(), 0.01);
            assertEquals(220.0, navigationRail.getWidth(), 0.01);
            Region indicator = lookupRegion(home, ".m3-navigation-item-indicator");
            Region stateLayer = lookupRegion(home, ".m3-state-layer");
            assertFalse(navigationRail.isFullWidthIndicator());
            assertTrue(indicator.getWidth() < 188.0,
                    () -> "default expressive indicator should hug content: " + indicator.getWidth());
            assertEquals(indicator.getWidth(), stateLayer.getWidth(), 0.01);
            assertEquals(56.0, indicator.getHeight(), 0.01);

            Region label = lookupRegion(home, ".m3-navigation-item-label");
            Bounds labelBounds = home.sceneToLocal(label.localToScene(label.getLayoutBounds()));
            assertEquals(64.0, labelBounds.getMinX(), 0.01);

            Region searchLabel = lookupRegion(search, ".m3-navigation-item-label");
            Bounds searchLabelBounds = search.sceneToLocal(searchLabel.localToScene(searchLabel.getLayoutBounds()));
            Bounds badgeBounds = search.sceneToLocal(
                    Objects.requireNonNull(search.getBadge(), "search badge")
                            .localToScene(search.getBadge().getLayoutBounds())
            );
            assertTrue(badgeBounds.getMinX() >= searchLabelBounds.getMaxX() - 0.01,
                    () -> "expanded rail badge should follow the label: label="
                            + searchLabelBounds + ", badge=" + badgeBounds);

            navigationRail.setFullWidthIndicator(true);
            root.layout();
            navigationRail.layout();
            home.layout();
            assertTrue(navigationRail.getPseudoClassStates().contains(
                    PseudoClass.getPseudoClass("full-width-indicator")
            ));
            assertEquals(188.0, indicator.getWidth(), 0.01);
            assertEquals(188.0, stateLayer.getWidth(), 0.01);

            navigationRail.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            navigationRail.layout();
            home.layout();
            Bounds homeSceneBounds = home.localToScene(home.getLayoutBounds());
            Bounds labelSceneBounds = label.localToScene(label.getLayoutBounds());
            assertEquals(64.0, homeSceneBounds.getMaxX() - labelSceneBounds.getMaxX(), 0.01);

            navigationRail.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            navigationRail.setExpandedContainerWidth(360.0);
            navigationRail.resize(360.0, 320.0);
            navigationRail.requestLayout();
            root.layout();
            navigationRail.layout();
            home.layout();
            assertEquals(328.0, indicator.getWidth(), 0.01);
            assertEquals(328.0, stateLayer.getWidth(), 0.01);
            assertEquals(64.0, home.getContainerHeight(), 0.01);
            assertEquals(44.0, navigationRail.getPadding().getTop(), 0.01);
        });
    }

    /// Verifies profile-specific selected label and interaction-layer color roles.
    @Test
    void navigationItemsResolveBaselineAndExpressiveActiveColors() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationItem item = new M3NavigationItem("Home");
            item.setSelected(true);
            Pane root = new Pane(item);
            Scene scene = new Scene(root, 160.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.BASELINE_2021,
                    Brightness.LIGHT
            ));
            root.setStyle(root.getStyle()
                    + " -m3-color-on-surface: #010203;"
                    + " -m3-color-secondary: #040506;"
                    + " -m3-color-on-secondary-container: #070809;");
            root.applyCss();
            item.resize(120.0, 80.0);
            root.layout();
            Label label = assertInstanceOf(Label.class, item.lookup(".m3-navigation-item-label"));
            Region stateLayer = lookupRegion(item, ".m3-state-layer");
            assertEquals(Color.web("#010203"), label.getTextFill());
            assertEquals(Color.web("#010203"), stateLayer.getBackground().getFills().get(0).getFill());

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.setStyle(root.getStyle()
                    + " -m3-color-on-surface: #010203;"
                    + " -m3-color-secondary: #040506;"
                    + " -m3-color-on-secondary-container: #070809;");
            root.applyCss();
            root.layout();
            assertEquals(Color.web("#040506"), label.getTextFill());
            assertEquals(Color.web("#070809"), stateLayer.getBackground().getFills().get(0).getFill());
        });
    }

    /// Verifies that navigation containers delegate layout to Material Design 3 skins.
    @Test
    void navigationContainersCreateMaterialSkins() {
        M3NavigationItem barHome = new M3NavigationItem("Home");
        M3NavigationItem railHome = new M3NavigationItem("Home");
        M3ListItem drawerHome = new M3ListItem("Home");
        M3NavigationBar navigationBar = navigationBar(barHome);
        M3NavigationRail navigationRail = navigationRail(railHome);
        M3NavigationDrawer navigationDrawer = navigationDrawer(drawerHome);
        Pane root = new Pane(navigationBar, navigationRail, navigationDrawer);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3NavigationBarSkin.class, navigationBar.getSkin());
        assertInstanceOf(M3NavigationRailSkin.class, navigationRail.getSkin());
        assertInstanceOf(M3NavigationDrawerSkin.class, navigationDrawer.getSkin());
        assertSame(barHome, navigationBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertSame(railHome, navigationRail.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertSame(drawerHome, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
    }

    /// Verifies that navigation item skins expose the selected indicator and ripple feedback.
    @Test
    void navigationItemSkinLaysOutIndicatorAndRipple() {
        M3NavigationItem item = new M3NavigationItem("Home");
        item.setSelected(true);
        Pane root = new Pane(item);
        Scene scene = new Scene(root, 120.0, 100.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        item.resize(80.0, 80.0);
        item.layout();

        Region indicator = lookupRegion(item, ".m3-navigation-item-indicator");
        Region stateLayer = lookupRegion(item, ".m3-state-layer-container");
        assertEquals(64.0, indicator.getWidth(), 0.0001);
        assertEquals(32.0, indicator.getHeight(), 0.0001);
        assertEquals(1.0, indicator.getOpacity(), 0.0001);
        assertEquals(indicator.getWidth(), stateLayer.getWidth(), 0.0001);
        assertEquals(indicator.getHeight(), stateLayer.getHeight(), 0.0001);
        assertEquals(
                indicator.localToScene(indicator.getBoundsInLocal()).getCenterX(),
                stateLayer.localToScene(stateLayer.getBoundsInLocal()).getCenterX(),
                0.0001
        );
        assertEquals(
                indicator.localToScene(indicator.getBoundsInLocal()).getCenterY(),
                stateLayer.localToScene(stateLayer.getBoundsInLocal()).getCenterY(),
                0.0001
        );

        item.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 40.0, 40.0, true));
        assertTrue(item.isArmed());
        item.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 40.0, 40.0, false));

        assertTrue(lookupRegion(item, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that navigation selection indicators animate both outgoing and incoming selected states.
    @Tier2Test
    @Test
    void navigationIndicatorAnimationsRenderIntermediateAndFinalStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tab> overviewReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tab> detailsReference = new AtomicReference<>();
        AtomicReference<@Nullable M3NavigationItem> homeReference = new AtomicReference<>();
        AtomicReference<@Nullable M3NavigationItem> searchReference = new AtomicReference<>();
        AtomicReference<@Nullable M3NavigationItem> railHomeReference = new AtomicReference<>();
        AtomicReference<@Nullable M3NavigationItem> railSearchReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> {
                M3Tab overview = new M3Tab("Overview");
                M3Tab details = new M3Tab("Details");
                M3TabBar tabBar = tabBar(overview, details);

                M3NavigationItem home = new M3NavigationItem("Home", visualIcon("home"));
                M3NavigationItem search = new M3NavigationItem("Search", visualIcon("search"));
                M3NavigationBar navigationBar = navigationBar(home, search);

                M3NavigationItem railHome = new M3NavigationItem("Home", visualIcon("home"));
                M3NavigationItem railSearch = new M3NavigationItem("Search", visualIcon("search"));
                M3NavigationRail navigationRail = navigationRail(railHome, railSearch);

                VBox root = new VBox(18.0, tabBar, navigationBar, navigationRail);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                Scene scene = new Scene(root, 360.0, 320.0);
                Stage stage = new Stage();

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(360.0, 320.0);
                root.layout();

                assertVisualIconSlotsUseVectorGraphics(root);

                stageReference.set(stage);
                rootReference.set(root);
                overviewReference.set(overview);
                detailsReference.set(details);
                homeReference.set(home);
                searchReference.set(search);
                railHomeReference.set(railHome);
                railSearchReference.set(railSearch);

                tabBar.select(details);
                navigationBar.select(search);
                navigationRail.select(railSearch);
            });

            FxTestUtils.runOnFxThreadWhen(
                    () -> navigationIndicatorsAreTransitioning(
                            rootReference,
                            overviewReference,
                            detailsReference,
                            homeReference,
                            searchReference,
                            railHomeReference,
                            railSearchReference
                    ),
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3Tab overview = Objects.requireNonNull(overviewReference.get(), "overview");
                        M3Tab details = Objects.requireNonNull(detailsReference.get(), "details");
                        M3NavigationItem home = Objects.requireNonNull(homeReference.get(), "home");
                        M3NavigationItem search = Objects.requireNonNull(searchReference.get(), "search");
                        M3NavigationItem railHome = Objects.requireNonNull(railHomeReference.get(), "railHome");
                        M3NavigationItem railSearch = Objects.requireNonNull(railSearchReference.get(), "railSearch");
                        Region outgoingTabIndicator = lookupRegion(overview, ".m3-tab-active-indicator");
                        Region incomingTabIndicator = lookupRegion(details, ".m3-tab-active-indicator");
                        Region outgoingBarIndicator = lookupRegion(home, ".m3-navigation-item-indicator");
                        Region incomingBarIndicator = lookupRegion(search, ".m3-navigation-item-indicator");
                        Region outgoingRailIndicator = lookupRegion(railHome, ".m3-navigation-item-indicator");
                        Region incomingRailIndicator = lookupRegion(railSearch, ".m3-navigation-item-indicator");

                        assertBetween(outgoingTabIndicator.getOpacity(), 0.0, 1.0, "outgoing tab indicator opacity");
                        assertBetween(incomingTabIndicator.getOpacity(), 0.25, 1.0, "incoming tab indicator opacity");
                        assertBetween(
                                outgoingBarIndicator.getOpacity(),
                                0.0,
                                1.0,
                                "outgoing navigation bar indicator opacity"
                        );
                        assertBetween(
                                incomingBarIndicator.getOpacity(),
                                0.25,
                                1.0,
                                "incoming navigation bar indicator opacity"
                        );
                        assertBetween(
                                outgoingRailIndicator.getOpacity(),
                                0.0,
                                1.0,
                                "outgoing navigation rail indicator opacity"
                        );
                        assertBetween(
                                incomingRailIndicator.getOpacity(),
                                0.25,
                                1.0,
                                "incoming navigation rail indicator opacity"
                        );
                        assertBetween(outgoingTabIndicator.getScaleX(), 0.72, 1.0, "outgoing tab indicator scale");
                        assertBetween(incomingTabIndicator.getScaleX(), 0.72, 1.0, "incoming tab indicator scale");
                        assertBetween(
                                outgoingBarIndicator.getScaleX(),
                                0.72,
                                1.0,
                                "outgoing navigation bar indicator scale"
                        );
                        assertBetween(
                                incomingBarIndicator.getScaleX(),
                                0.72,
                                1.0,
                                "incoming navigation bar indicator scale"
                        );

                        WritableImage image = snapshotImageOnFxThread(root);
                        assertSnapshotNodeContainsContrast(image, incomingTabIndicator, Color.WHITE, 0.04);
                        assertSnapshotNodeContainsContrast(image, incomingBarIndicator, Color.WHITE, 0.04);
                        assertSnapshotNodeContainsContrast(image, incomingRailIndicator, Color.WHITE, 0.04);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-navigation-indicator-animation-frame.png"
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> navigationIndicatorsHaveSettled(
                            rootReference,
                            overviewReference,
                            detailsReference,
                            homeReference,
                            searchReference,
                            railHomeReference,
                            railSearchReference
                    ),
                    () -> {
                    },
                    () -> {
                        M3Tab overview = Objects.requireNonNull(overviewReference.get(), "overview");
                        M3Tab details = Objects.requireNonNull(detailsReference.get(), "details");
                        M3NavigationItem home = Objects.requireNonNull(homeReference.get(), "home");
                        M3NavigationItem search = Objects.requireNonNull(searchReference.get(), "search");
                        M3NavigationItem railHome = Objects.requireNonNull(railHomeReference.get(), "railHome");
                        M3NavigationItem railSearch = Objects.requireNonNull(railSearchReference.get(), "railSearch");
                        Region outgoingTabIndicator = lookupRegion(overview, ".m3-tab-active-indicator");
                        Region incomingTabIndicator = lookupRegion(details, ".m3-tab-active-indicator");
                        Region outgoingBarIndicator = lookupRegion(home, ".m3-navigation-item-indicator");
                        Region incomingBarIndicator = lookupRegion(search, ".m3-navigation-item-indicator");
                        Region outgoingRailIndicator = lookupRegion(railHome, ".m3-navigation-item-indicator");
                        Region incomingRailIndicator = lookupRegion(railSearch, ".m3-navigation-item-indicator");

                        assertEquals(0.0, outgoingTabIndicator.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingTabIndicator.getOpacity(), 0.0001);
                        assertEquals(0.0, outgoingBarIndicator.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingBarIndicator.getOpacity(), 0.0001);
                        assertEquals(0.0, outgoingRailIndicator.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingRailIndicator.getOpacity(), 0.0001);
                    }
            );
        } finally {
            closeObservableMotionWindow(stageReference, rootReference);
        }
    }

    /// Returns whether tab, navigation bar, and navigation rail indicators are in intermediate transitions.
    private static boolean navigationIndicatorsAreTransitioning(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3Tab> outgoingTabReference,
            AtomicReference<@Nullable M3Tab> incomingTabReference,
            AtomicReference<@Nullable M3NavigationItem> outgoingBarReference,
            AtomicReference<@Nullable M3NavigationItem> incomingBarReference,
            AtomicReference<@Nullable M3NavigationItem> outgoingRailReference,
            AtomicReference<@Nullable M3NavigationItem> incomingRailReference
    ) {
        @Nullable M3Tab outgoingTab = outgoingTabReference.get();
        @Nullable M3Tab incomingTab = incomingTabReference.get();
        @Nullable M3NavigationItem outgoingBar = outgoingBarReference.get();
        @Nullable M3NavigationItem incomingBar = incomingBarReference.get();
        @Nullable M3NavigationItem outgoingRail = outgoingRailReference.get();
        @Nullable M3NavigationItem incomingRail = incomingRailReference.get();
        if (outgoingTab == null
                || incomingTab == null
                || outgoingBar == null
                || incomingBar == null
                || outgoingRail == null
                || incomingRail == null
                || observableRootUnavailable(rootReference)) {
            return false;
        }

        Region outgoingTabIndicator = lookupRegion(outgoingTab, ".m3-tab-active-indicator");
        Region incomingTabIndicator = lookupRegion(incomingTab, ".m3-tab-active-indicator");
        Region outgoingBarIndicator = lookupRegion(outgoingBar, ".m3-navigation-item-indicator");
        Region incomingBarIndicator = lookupRegion(incomingBar, ".m3-navigation-item-indicator");
        Region outgoingRailIndicator = lookupRegion(outgoingRail, ".m3-navigation-item-indicator");
        Region incomingRailIndicator = lookupRegion(incomingRail, ".m3-navigation-item-indicator");
        return navigationIndicatorPairIsTransitioning(outgoingTabIndicator, incomingTabIndicator)
                && navigationIndicatorPairIsTransitioning(outgoingBarIndicator, incomingBarIndicator)
                && navigationIndicatorPairIsTransitioning(outgoingRailIndicator, incomingRailIndicator);
    }

    /// Returns whether tab, navigation bar, and navigation rail indicators have settled after selection.
    private static boolean navigationIndicatorsHaveSettled(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3Tab> outgoingTabReference,
            AtomicReference<@Nullable M3Tab> incomingTabReference,
            AtomicReference<@Nullable M3NavigationItem> outgoingBarReference,
            AtomicReference<@Nullable M3NavigationItem> incomingBarReference,
            AtomicReference<@Nullable M3NavigationItem> outgoingRailReference,
            AtomicReference<@Nullable M3NavigationItem> incomingRailReference
    ) {
        @Nullable M3Tab outgoingTab = outgoingTabReference.get();
        @Nullable M3Tab incomingTab = incomingTabReference.get();
        @Nullable M3NavigationItem outgoingBar = outgoingBarReference.get();
        @Nullable M3NavigationItem incomingBar = incomingBarReference.get();
        @Nullable M3NavigationItem outgoingRail = outgoingRailReference.get();
        @Nullable M3NavigationItem incomingRail = incomingRailReference.get();
        if (outgoingTab == null
                || incomingTab == null
                || outgoingBar == null
                || incomingBar == null
                || outgoingRail == null
                || incomingRail == null
                || observableRootUnavailable(rootReference)) {
            return false;
        }

        Region outgoingTabIndicator = lookupRegion(outgoingTab, ".m3-tab-active-indicator");
        Region incomingTabIndicator = lookupRegion(incomingTab, ".m3-tab-active-indicator");
        Region outgoingBarIndicator = lookupRegion(outgoingBar, ".m3-navigation-item-indicator");
        Region incomingBarIndicator = lookupRegion(incomingBar, ".m3-navigation-item-indicator");
        Region outgoingRailIndicator = lookupRegion(outgoingRail, ".m3-navigation-item-indicator");
        Region incomingRailIndicator = lookupRegion(incomingRail, ".m3-navigation-item-indicator");
        return navigationIndicatorPairHasSettled(outgoingTabIndicator, incomingTabIndicator)
                && navigationIndicatorPairHasSettled(outgoingBarIndicator, incomingBarIndicator)
                && navigationIndicatorPairHasSettled(outgoingRailIndicator, incomingRailIndicator);
    }

    /// Returns whether one outgoing and incoming navigation-indicator pair is in an intermediate transition.
    private static boolean navigationIndicatorPairIsTransitioning(Region outgoing, Region incoming) {
        return isBetweenExclusive(outgoing.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(incoming.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(outgoing.getScaleX(), 0.72, 1.0)
                && isBetweenExclusive(incoming.getScaleX(), 0.72, 1.0);
    }

    /// Returns whether one outgoing and incoming navigation-indicator pair has reached its final state.
    private static boolean navigationIndicatorPairHasSettled(Region outgoing, Region incoming) {
        return Math.abs(outgoing.getOpacity()) < 0.0001
                && Math.abs(incoming.getOpacity() - 1.0) < 0.0001;
    }
}
