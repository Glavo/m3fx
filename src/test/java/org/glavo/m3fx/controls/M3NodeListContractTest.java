// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the shared mutation contract of public lists whose elements become scene-graph children.
@NotNullByDefault
final class M3NodeListContractTest {
    /// Starts the JavaFX toolkit before controls are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that every node-backed list rejects repeated object identities without partial bulk mutations.
    @Test
    void nodeBackedListsRejectRepeatedIdentitiesAtomically() {
        FxTestUtils.runOnFxThread(() -> {
            assertIdentityDistinct(new M3BottomAppBar().getActions(), new Label("Action"));
            assertIdentityDistinct(new M3Banner().getActions(), new Label("Action"));
            assertIdentityDistinct(new M3BottomSheet().getActions(), new Label("Action"));
            assertIdentityDistinct(new M3ButtonGroup().getItems(), new M3Button("Action"));
            assertIdentityDistinct(new M3Carousel().getItems(), new Label("Item"));
            assertIdentityDistinct(new M3ChipGroup().getItems(), new M3AssistChip("Chip"));
            assertIdentityDistinct(new M3DialogPane().getActions(), new M3Button("Action"));
            assertIdentityDistinct(new M3FormPane().getItems(), new Label("Item"));
            assertIdentityDistinct(new M3FormSection().getContent(), new Label("Content"));
            assertIdentityDistinct(
                    new M3IconToggleButtonGroup().getItems(),
                    new M3IconToggleButton(new M3Icon("T"))
            );
            assertIdentityDistinct(new M3ListPane().getItems(), new M3ListItem("Item"));
            assertIdentityDistinct(new M3Menu().getItems(), new M3MenuItem("Item"));
            assertIdentityDistinct(new M3NavigationBar().getItems(), new M3NavigationItem("Item"));
            assertIdentityDistinct(new M3NavigationDrawer().getItems(), new M3ListItem("Item"));
            assertIdentityDistinct(
                    new M3NavigationDrawerGroup().getItems(),
                    new M3ListItem("Item")
            );
            assertIdentityDistinct(new M3NavigationRail().getItems(), new M3NavigationItem("Item"));
            assertIdentityDistinct(new M3SearchBar().getTrailingActions(), new Label("Action"));
            assertIdentityDistinct(
                    new M3SegmentedButtonGroup().getItems(),
                    new M3SegmentedButton("Item")
            );
            assertIdentityDistinct(new M3SideSheet().getHeaderActions(), new Label("Header action"));
            assertIdentityDistinct(new M3SideSheet().getActions(), new Label("Action"));
            assertIdentityDistinct(new M3Surface().getContent(), new Label("Content"));
            assertIdentityDistinct(new M3TabBar().getTabs(), new M3Tab("Tab"));
            assertIdentityDistinct(new M3Toolbar().getItems(), new Label("Item"));
            assertIdentityDistinct(new M3TopAppBar().getActions(), new Label("Action"));
        });
    }

    /// Verifies direct, bulk-addition, and replacement mutations for one node-backed list.
    ///
    /// @param list the list under test
    /// @param element the element whose identity must remain unique
    /// @param <E> the list element type
    private static <E> void assertIdentityDistinct(ObservableList<E> list, E element) {
        assertTrue(list.add(element));
        assertThrows(IllegalArgumentException.class, () -> list.add(element));
        assertEquals(List.of(element), list);

        list.clear();
        assertThrows(IllegalArgumentException.class, () -> list.addAll(List.of(element, element)));
        assertTrue(list.isEmpty());

        list.add(element);
        assertThrows(IllegalArgumentException.class, () -> list.setAll(List.of(element, element)));
        assertEquals(List.of(element), list);
    }
}
