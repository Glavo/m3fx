// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3BreadcrumbsSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies breadcrumb hierarchy, overflow, layout direction, interaction, and accessibility contracts.
@NotNullByDefault
final class M3BreadcrumbsTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies item-list validation, current-location ownership, and public presentation properties.
    @Test
    void exposesStableHierarchyProperties() {
        FxTestUtils.runOnFxThread(() -> {
            M3BreadcrumbItem home = new M3BreadcrumbItem("Home");
            M3BreadcrumbItem projects = new M3BreadcrumbItem("Projects");
            M3BreadcrumbItem current = new M3BreadcrumbItem("M3FX");
            M3Breadcrumbs breadcrumbs = new M3Breadcrumbs(home, projects, current);

            assertEquals(List.of(home, projects, current), breadcrumbs.getItems());
            assertFalse(home.isCurrent());
            assertFalse(projects.isCurrent());
            assertTrue(current.isCurrent());
            assertEquals(4, breadcrumbs.getMaxVisibleItems());
            assertFalse(breadcrumbs.isCompact());
            assertFalse(breadcrumbs.isKeepRootVisible());
            assertEquals(AccessibleRole.TOOL_BAR, breadcrumbs.getAccessibleRole());
            assertEquals("Breadcrumbs", breadcrumbs.getAccessibleText());
            assertFalse(breadcrumbs.isFocusTraversable());
            assertEquals(AccessibleRole.HYPERLINK, current.getAccessibleRole());
            assertTrue(current.isFocusTraversable());
            assertEquals(true, current.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
            assertEquals("M3FX", current.getAccessibleHelp());

            breadcrumbs.getItems().remove(current);
            assertFalse(current.isCurrent());
            assertTrue(projects.isCurrent());
            breadcrumbs.getItems().clear();
            assertFalse(projects.isCurrent());

            breadcrumbs.setCompact(true);
            breadcrumbs.setKeepRootVisible(true);
            breadcrumbs.setMaxVisibleItems(5);
            assertTrue(breadcrumbs.isCompact());
            assertTrue(breadcrumbs.isKeepRootVisible());
            assertEquals(5, breadcrumbs.getMaxVisibleItems());
            assertTrue(breadcrumbs.getPseudoClassStates().stream()
                    .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("compact")));

            assertThrows(IllegalArgumentException.class, () -> breadcrumbs.setMaxVisibleItems(1));
            breadcrumbs.maxVisibleItemsProperty().set(0);
            assertEquals(0, breadcrumbs.maxVisibleItemsProperty().get());
            assertEquals(2, breadcrumbs.getMaxVisibleItems());
            assertThrows(NullPointerException.class, () -> breadcrumbs.getItems().add(null));
            assertThrows(IllegalArgumentException.class, () -> breadcrumbs.getItems().addAll(home, home));
            assertThrows(NullPointerException.class, () -> new M3Breadcrumbs((M3BreadcrumbItem[]) null));
        });
    }

    /// Verifies action delivery and disabled-item suppression.
    @Test
    void itemFiresActionEvents() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicInteger actions = new AtomicInteger();
            M3BreadcrumbItem item = new M3BreadcrumbItem("Projects");
            item.setOnAction(event -> actions.incrementAndGet());

            item.fire();
            assertEquals(1, actions.get());
            item.setDisable(true);
            item.fire();
            assertEquals(1, actions.get());
        });
    }

    /// Verifies logical overflow, root retention, menu mirroring, and skin replacement.
    @Test
    void skinCollapsesHierarchyIntoOverflowMenu() {
        FxTestUtils.runOnFxThread(() -> {
            M3BreadcrumbItem[] items = items("Home", "Projects", "Libraries", "JavaFX", "Controls", "M3FX");
            AtomicInteger homeActions = new AtomicInteger();
            items[0].setOnAction(event -> homeActions.incrementAndGet());
            M3Breadcrumbs breadcrumbs = new M3Breadcrumbs(items);
            StackPane root = themedRoot(breadcrumbs, 720.0);
            layout(root, 720.0, 120.0);

            assertInstanceOf(M3BreadcrumbsSkin.class, breadcrumbs.getSkin());
            assertNull(items[0].getParent());
            assertNull(items[1].getParent());
            assertNull(items[2].getParent());
            assertNotNull(items[3].getParent());
            assertNotNull(items[4].getParent());
            assertNotNull(items[5].getParent());

            M3MenuButton overflow = assertInstanceOf(
                    M3MenuButton.class,
                    breadcrumbs.lookup(".m3-breadcrumb-overflow")
            );
            assertEquals(6, overflow.getItems().size());
            assertEquals(1, overflow.getItems().stream()
                    .map(M3MenuItem.class::cast)
                    .filter(M3MenuItem::isSelected)
                    .count());
            assertTrue(assertInstanceOf(M3MenuItem.class, overflow.getItems().get(5)).isSelected());
            assertInstanceOf(M3MenuItem.class, overflow.getItems().get(0)).fire();
            assertEquals(1, homeActions.get());
            items[0].setText("Workspace");
            items[0].setDisable(true);
            M3MenuItem mirroredHome = assertInstanceOf(M3MenuItem.class, overflow.getItems().get(0));
            assertEquals("Workspace", mirroredHome.getHeadlineText());
            assertTrue(mirroredHome.isDisabled());

            breadcrumbs.setKeepRootVisible(true);
            layout(root, 720.0, 120.0);
            assertNotNull(items[0].getParent());
            assertNull(items[1].getParent());
            assertNull(items[2].getParent());
            assertNull(items[3].getParent());
            assertNotNull(items[4].getParent());
            assertNotNull(items[5].getParent());

            FxTestUtils.replaceSkin(breadcrumbs, M3BreadcrumbsSkin::new);
            layout(root, 720.0, 120.0);
            assertEquals(1, breadcrumbs.lookupAll(".m3-breadcrumbs-row").size());
            assertEquals(1, breadcrumbs.lookupAll(".m3-breadcrumb-overflow").size());
        });
    }

    /// Verifies width-driven collapse and compact vertical metrics.
    @Test
    void adaptsVisibleDepthAndHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3BreadcrumbItem[] items = items("Home", "Projects", "Libraries", "JavaFX", "Controls", "M3FX");
            M3Breadcrumbs breadcrumbs = new M3Breadcrumbs(items);
            breadcrumbs.setMaxVisibleItems(6);
            StackPane root = themedRoot(breadcrumbs, 720.0);
            layout(root, 720.0, 120.0);
            assertEquals(6, visibleItemCount(items));
            assertNull(breadcrumbs.lookup(".m3-breadcrumb-overflow"));
            assertEquals(40.0, breadcrumbs.getHeight(), 0.5);

            layout(root, 220.0, 120.0);
            assertTrue(visibleItemCount(items) < 6);
            assertNotNull(items[items.length - 1].getParent());
            assertNotNull(breadcrumbs.lookup(".m3-breadcrumb-overflow"));

            breadcrumbs.setCompact(true);
            layout(root, 220.0, 120.0);
            assertEquals(32.0, breadcrumbs.getHeight(), 0.5);
        });
    }

    /// Verifies hierarchy and separator direction in left-to-right and right-to-left layouts.
    @Test
    void mirrorsHierarchyAndSeparatorsInRightToLeftLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3BreadcrumbItem[] items = items("Home", "Projects", "M3FX");
            M3Breadcrumbs breadcrumbs = new M3Breadcrumbs(items);
            StackPane root = themedRoot(breadcrumbs, 520.0);
            layout(root, 520.0, 120.0);

            assertTrue(centerX(items[0]) < centerX(items[2]));
            assertEquals(
                    List.of("›", "›"),
                    breadcrumbs.lookupAll(".m3-breadcrumb-separator").stream()
                            .map(Label.class::cast)
                            .map(Label::getText)
                            .toList()
            );

            breadcrumbs.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout(root, 520.0, 120.0);
            assertTrue(centerX(items[0]) > centerX(items[2]));
            assertEquals(
                    List.of("‹", "‹"),
                    breadcrumbs.lookupAll(".m3-breadcrumb-separator").stream()
                            .map(Label.class::cast)
                            .map(Label::getText)
                            .toList()
            );
        });
    }

    /// Creates breadcrumb items for the specified labels.
    ///
    /// @param labels the ordered hierarchy labels
    /// @return the created item array
    private static M3BreadcrumbItem[] items(String... labels) {
        M3BreadcrumbItem[] items = new M3BreadcrumbItem[labels.length];
        for (int index = 0; index < labels.length; index++) {
            items[index] = new M3BreadcrumbItem(labels[index]);
        }
        return items;
    }

    /// Creates a themed scene root containing one breadcrumbs control.
    ///
    /// @param breadcrumbs the control to place in the root
    /// @param width the initial scene width
    /// @return the scene root
    private static StackPane themedRoot(M3Breadcrumbs breadcrumbs, double width) {
        StackPane root = new StackPane(breadcrumbs);
        Scene scene = new Scene(root, width, 120.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        return root;
    }

    /// Applies CSS and performs deterministic root and control layout.
    ///
    /// @param root the scene root
    /// @param width the layout width
    /// @param height the layout height
    private static void layout(StackPane root, double width, double height) {
        root.applyCss();
        root.resize(width, height);
        root.layout();
        root.applyCss();
        root.layout();
    }

    /// Counts items currently attached to the skin row.
    ///
    /// @param items the complete logical hierarchy
    /// @return the visible item count
    private static long visibleItemCount(M3BreadcrumbItem[] items) {
        return List.of(items).stream().filter(item -> item.getParent() != null).count();
    }

    /// Returns one item's horizontal center in scene coordinates.
    ///
    /// @param item the item to measure
    /// @return the scene-coordinate center
    private static double centerX(Node item) {
        return item.localToScene(item.getBoundsInLocal()).getCenterX();
    }
}
