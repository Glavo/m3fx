// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3ChipGroupSkin;
import org.glavo.m3fx.skins.M3IconToggleButtonGroupSkin;
import org.glavo.m3fx.skins.M3SegmentedButtonGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies selection invariants after application listeners change selection during group updates.
@NotNullByDefault
final class M3SelectionReentrancyTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains single selection when a deselection callback reselects the original button.
    @Test
    void iconToggleGroupPreservesSingleSelectionDuringReselection() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            M3IconToggleButton first = new M3IconToggleButton();
            M3IconToggleButton second = new M3IconToggleButton();
            group.getItems().setAll(first, second);
            group.select(first);
            AtomicBoolean reselect = new AtomicBoolean(true);
            first.selectedProperty().addListener((observable, oldValue, selected) -> {
                if (!selected && reselect.getAndSet(false)) {
                    first.setSelected(true);
                }
            });

            group.select(second);

            assertFalse(first.isSelected() && second.isSelected());
            assertEquals(1, group.getSelectedButtons().size());
            assertEquals(group.getSelectedButtons().get(0), group.getSelectedButton());
        });
    }

    /// Retains single selection when a deselection callback reselects the original segment.
    @Test
    void segmentedGroupPreservesSingleSelectionDuringReselection() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
            M3SegmentedButton first = new M3SegmentedButton();
            M3SegmentedButton second = new M3SegmentedButton();
            group.getItems().setAll(first, second);
            group.select(first);
            AtomicBoolean reselect = new AtomicBoolean(true);
            first.selectedProperty().addListener((observable, oldValue, selected) -> {
                if (!selected && reselect.getAndSet(false)) {
                    first.setSelected(true);
                }
            });

            group.select(second);

            assertFalse(first.isSelected() && second.isSelected());
            assertEquals(1, group.getSelectedButtons().size());
            assertSame(group.getSelectedButtons().get(0), group.getSelectedButton());
        });
    }

    /// Retains single selection when a deselection callback reselects the original chip.
    @Test
    void chipGroupPreservesSingleSelectionDuringReselection() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = new M3ChipGroup();
            group.setSelectionMode(M3SelectionMode.SINGLE);
            M3FilterChip first = new M3FilterChip();
            M3FilterChip second = new M3FilterChip();
            group.getItems().setAll(first, second);
            group.select(first);
            AtomicBoolean reselect = new AtomicBoolean(true);
            first.selectedProperty().addListener((observable, oldValue, selected) -> {
                if (!selected && reselect.getAndSet(false)) {
                    first.setSelected(true);
                }
            });

            group.select(second);

            assertFalse(first.isSelected() && second.isSelected());
            assertEquals(1, group.getSelectedChips().size());
            assertSame(group.getSelectedChips().get(0), group.getSelectedChip());
        });
    }

    /// Publishes the selection requested by a selected-list listener after the current notification completes.
    @Test
    void selectedListListenerCanReplaceSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            M3IconToggleButton first = new M3IconToggleButton();
            M3IconToggleButton second = new M3IconToggleButton();
            group.getItems().setAll(first, second);
            AtomicBoolean replace = new AtomicBoolean(true);
            group.getSelectedButtons().addListener((ListChangeListener<M3IconToggleButton>) change -> {
                if (replace.getAndSet(false)) {
                    group.select(second);
                }
            });

            group.select(first);

            assertSame(second, group.getSelectedButton());
            assertEquals(1, group.getSelectedButtons().size());
            assertSame(second, group.getSelectedButtons().get(0));
            assertFalse(first.isSelected());
            group.setAllowEmptySelection(false);
            second.setSelected(false);
            assertSame(second, group.getSelectedButton());
        });
    }

    /// Keeps icon selection valid when a callback replaces the requested item.
    @Test
    void iconSelectionSurvivesItemReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            assertSelectionSurvivesItemReplacement(group.getItems(), group::select,
                    M3IconToggleButton::selectedProperty,
                    new M3IconToggleButton(), new M3IconToggleButton(), new M3IconToggleButton());
            assertEquals(0, group.getSelectedButtons().size());
        });
    }

    /// Keeps segmented selection valid when a callback replaces the requested item.
    @Test
    void segmentedSelectionSurvivesItemReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
            assertSelectionSurvivesItemReplacement(group.getItems(), group::select,
                    M3SegmentedButton::selectedProperty,
                    new M3SegmentedButton(), new M3SegmentedButton(), new M3SegmentedButton());
            assertEquals(0, group.getSelectedButtons().size());
        });
    }

    /// Keeps chip selection valid when a callback replaces the requested item.
    @Test
    void chipSelectionSurvivesItemReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = new M3ChipGroup();
            group.setSelectionMode(M3SelectionMode.SINGLE);
            assertSelectionSurvivesItemReplacement(group.getItems(),
                    item -> group.select((M3SelectableChip) item),
                    item -> ((M3SelectableChip) item).selectedProperty(),
                    new M3FilterChip(), new M3FilterChip(), new M3FilterChip());
            assertEquals(0, group.getSelectedChips().size());
        });
    }

    /// Keeps accessible icon selection valid when a callback replaces a requested item.
    @Test
    void accessibleIconSelectionSurvivesItemReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            group.setSelectionMode(M3SelectionMode.MULTIPLE);
            assertSelectionSurvivesItemReplacement(group.getItems(),
                    item -> group.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, item),
                    M3IconToggleButton::selectedProperty,
                    new M3IconToggleButton(), new M3IconToggleButton(), new M3IconToggleButton());
            assertEquals(0, group.getSelectedButtons().size());
        });
    }

    /// Keeps accessible segmented selection valid when a callback replaces a requested item.
    @Test
    void accessibleSegmentedSelectionSurvivesItemReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
            group.setSelectionMode(M3SelectionMode.MULTIPLE);
            assertSelectionSurvivesItemReplacement(group.getItems(),
                    item -> group.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, item),
                    M3SegmentedButton::selectedProperty,
                    new M3SegmentedButton(), new M3SegmentedButton(), new M3SegmentedButton());
            assertEquals(0, group.getSelectedButtons().size());
        });
    }

    /// Keeps accessible chip selection valid when a callback replaces a requested item.
    @Test
    void accessibleChipSelectionSurvivesItemReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = new M3ChipGroup();
            group.setSelectionMode(M3SelectionMode.MULTIPLE);
            assertSelectionSurvivesItemReplacement(group.getItems(),
                    item -> group.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, item),
                    item -> ((M3SelectableChip) item).selectedProperty(),
                    new M3FilterChip(), new M3FilterChip(), new M3FilterChip());
            assertEquals(0, group.getSelectedChips().size());
        });
    }

    /// Preserves grouped styles and accessibility when deselection reinstalls a removed segment.
    @Test
    void removedSegmentCanRejoinItsGroupDuringDeselection() {
        assertSegmentReinstallation(true);
    }

    /// Leaves a new group's styles and accessibility intact when deselection transfers a removed segment.
    @Test
    void removedSegmentCanJoinAnotherGroupDuringDeselection() {
        assertSegmentReinstallation(false);
    }

    /// Reinstalls a segment from its deselection callback, then verifies ownership and eventual cleanup.
    private static void assertSegmentReinstallation(boolean sameGroup) {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButtonGroup source = new M3SegmentedButtonGroup();
            M3SegmentedButtonGroup target = sameGroup ? source : new M3SegmentedButtonGroup();
            target.setSelectionMode(M3SelectionMode.MULTIPLE);
            M3SegmentedButton button = new M3SegmentedButton();
            source.getItems().add(button);
            source.select(button);
            AtomicBoolean reinstall = new AtomicBoolean(true);
            button.selectedProperty().addListener((observable, oldValue, selected) -> {
                if (!selected && reinstall.getAndSet(false)) {
                    target.getItems().add(button);
                    button.setSelected(true);
                }
            });

            source.getItems().remove(button);

            assertEquals(1, target.getItems().size());
            assertSame(button, target.getItems().get(0));
            assertEquals(AccessibleRole.CHECK_BOX, button.getAccessibleRole());
            assertTrue(button.getStyleClass().contains("m3-segmented-button-single"));
            assertSame(button, target.getSelectedButton());
            target.getItems().remove(button);
            assertEquals(AccessibleRole.TOGGLE_BUTTON, button.getAccessibleRole());
            assertFalse(button.getStyleClass().contains("m3-segmented-button-single"));
            assertFalse(button.isSelected());
            assertTrue(target.getSelectedButtons().isEmpty());

            target.setSelectionMode(M3SelectionMode.SINGLE);
            M3SegmentedButton replacement = new M3SegmentedButton();
            target.getItems().add(replacement);
            target.select(replacement);
            button.setSelected(true);
            assertSame(replacement, target.getSelectedButton());
        });
    }

    /// Releases every icon listener after a removed item has been reinstalled from its deselection callback.
    @Test
    void reinstalledIconReleasesSelectionListenersOnRemoval() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            assertRemovalReleasesReinstalledItem(group.getItems(), group::select,
                    M3IconToggleButton::selectedProperty, new M3IconToggleButton(), new M3IconToggleButton());
        });
    }

    /// Releases every chip listener after a removed item has been reinstalled from its deselection callback.
    @Test
    void reinstalledChipReleasesSelectionListenersOnRemoval() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = new M3ChipGroup();
            group.setSelectionMode(M3SelectionMode.SINGLE);
            assertRemovalReleasesReinstalledItem(group.getItems(),
                    item -> group.select((M3SelectableChip) item),
                    item -> ((M3SelectableChip) item).selectedProperty(), new M3FilterChip(), new M3FilterChip());
        });
    }

    /// Releases pending icon additions removed or reinstalled by a replacement callback.
    @Test
    void iconReplacementCallbacksDoNotLeaveListeners() {
        for (boolean reinstall : new boolean[]{false, true}) {
            FxTestUtils.runOnFxThread(() -> {
                M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
                group.setSkin(new M3IconToggleButtonGroupSkin(group));
                assertReplacementReleasesPendingItem(group.getItems(), group::select,
                        M3IconToggleButton::selectedProperty,
                        new M3IconToggleButton(), new M3IconToggleButton(), new M3IconToggleButton(), reinstall);
                assertEquals(group.getItems(), ((Pane) group.getChildrenUnmodifiable().get(0)).getChildren());
            });
        }
    }

    /// Releases pending segment additions removed or reinstalled by a replacement callback.
    @Test
    void segmentedReplacementCallbacksDoNotLeaveListeners() {
        for (boolean reinstall : new boolean[]{false, true}) {
            FxTestUtils.runOnFxThread(() -> {
                M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
                group.setSkin(new M3SegmentedButtonGroupSkin(group));
                assertReplacementReleasesPendingItem(group.getItems(), group::select,
                        M3SegmentedButton::selectedProperty,
                        new M3SegmentedButton(), new M3SegmentedButton(), new M3SegmentedButton(), reinstall);
                assertEquals(group.getItems(), ((Pane) group.getChildrenUnmodifiable().get(0)).getChildren());
            });
        }
    }

    /// Releases pending chip additions removed or reinstalled by a replacement callback.
    @Test
    void chipReplacementCallbacksDoNotLeaveListeners() {
        for (boolean reinstall : new boolean[]{false, true}) {
            FxTestUtils.runOnFxThread(() -> {
                M3ChipGroup group = new M3ChipGroup();
                group.setSkin(new M3ChipGroupSkin(group));
                group.setSelectionMode(M3SelectionMode.SINGLE);
                assertReplacementReleasesPendingItem(group.getItems(),
                        item -> group.select((M3SelectableChip) item),
                        item -> ((M3SelectableChip) item).selectedProperty(),
                        new M3FilterChip(), new M3FilterChip(), new M3FilterChip(), reinstall);
                assertEquals(group.getItems(), ((Pane) group.getChildrenUnmodifiable().get(0)).getChildren());
            });
        }
    }

    /// Mirrors items added by a parent-change callback after the active child-list update completes.
    @Test
    void itemContainerReconcilesParentCallbacks() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            group.setSkin(new M3IconToggleButtonGroupSkin(group));
            M3IconToggleButton first = new M3IconToggleButton();
            M3IconToggleButton second = new M3IconToggleButton();
            AtomicBoolean addSecond = new AtomicBoolean(true);
            first.parentProperty().addListener((observable, oldParent, parent) -> {
                if (parent != null && addSecond.getAndSet(false)) {
                    group.getItems().add(second);
                }
            });

            group.getItems().add(first);

            Pane container = (Pane) group.getChildrenUnmodifiable().get(0);
            assertEquals(List.of(first, second), group.getItems());
            assertEquals(group.getItems(), container.getChildren());
            group.setSkin(null);
            group.getItems().clear();
            assertTrue(container.getChildren().isEmpty());
        });
    }

    /// Preserves retained icon selection without publishing a temporary deselection.
    @Test
    void iconReplacementRetainsSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
            assertReplacementRetainsSelection(group.getItems(), group::select,
                    M3IconToggleButton::selectedProperty, new M3IconToggleButton(), new M3IconToggleButton());
        });
    }

    /// Preserves retained segment selection without publishing a temporary deselection.
    @Test
    void segmentedReplacementRetainsSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
            assertReplacementRetainsSelection(group.getItems(), group::select,
                    M3SegmentedButton::selectedProperty, new M3SegmentedButton(), new M3SegmentedButton());
        });
    }

    /// Preserves retained chip selection without publishing a temporary deselection.
    @Test
    void chipReplacementRetainsSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = new M3ChipGroup();
            group.setSelectionMode(M3SelectionMode.SINGLE);
            assertReplacementRetainsSelection(group.getItems(),
                    item -> group.select((M3SelectableChip) item),
                    item -> ((M3SelectableChip) item).selectedProperty(), new M3FilterChip(), new M3FilterChip());
        });
    }

    /// Replaces a list with retained items, then verifies selection and listener continuity.
    private static <T extends Node> void assertReplacementRetainsSelection(
            ObservableList<T> items,
            Consumer<T> select,
            Function<T, BooleanProperty> selectedProperty,
            T first,
            T second
    ) {
        items.add(first);
        select.accept(first);
        AtomicInteger changes = new AtomicInteger();
        selectedProperty.apply(first).addListener((observable, oldValue, selected) -> changes.incrementAndGet());

        items.setAll(List.of(second, first));

        assertTrue(selectedProperty.apply(first).get());
        assertEquals(0, changes.get());
        select.accept(second);
        assertFalse(selectedProperty.apply(first).get());
        assertTrue(selectedProperty.apply(second).get());
        assertEquals(1, changes.get());
    }

    /// Changes a pending addition during bulk replacement and verifies eventual listener release.
    private static <T extends Node> void assertReplacementReleasesPendingItem(
            ObservableList<T> items,
            Consumer<T> select,
            Function<T, BooleanProperty> selectedProperty,
            T original,
            T pending,
            T replacement,
            boolean reinstall
    ) {
        items.add(original);
        select.accept(original);
        selectedProperty.apply(original).addListener((observable, oldValue, selected) -> {
            if (!selected) {
                items.remove(pending);
                items.add(reinstall ? pending : replacement);
            }
        });

        items.setAll(List.of(pending));
        if (reinstall) {
            items.remove(pending);
            items.add(replacement);
        }
        select.accept(replacement);
        selectedProperty.apply(pending).set(true);
        assertEquals(List.of(replacement), items);
        assertTrue(selectedProperty.apply(replacement).get());
    }

    /// Reinstalls an item once, removes it again, and checks that its former group no longer observes it.
    private static <T extends Node> void assertRemovalReleasesReinstalledItem(
            ObservableList<T> items,
            Consumer<T> select,
            Function<T, BooleanProperty> selectedProperty,
            T original,
            T replacement
    ) {
        items.add(original);
        select.accept(original);
        AtomicBoolean reinstall = new AtomicBoolean(true);
        selectedProperty.apply(original).addListener((observable, oldValue, selected) -> {
            if (!selected && reinstall.getAndSet(false)) {
                items.add(original);
                selectedProperty.apply(original).set(true);
            }
        });

        items.remove(original);
        assertSame(original, items.get(0));
        items.remove(original);
        items.add(replacement);
        select.accept(replacement);
        selectedProperty.apply(original).set(true);
        assertTrue(selectedProperty.apply(replacement).get());
    }

    /// Replaces the pending selection during deselection and verifies the removed item stays deselected.
    private static <T extends Node> void assertSelectionSurvivesItemReplacement(
            ObservableList<T> items,
            Consumer<T> select,
            Function<T, BooleanProperty> selectedProperty,
            T first,
            T second,
            T replacement
    ) {
        items.add(first);
        items.add(second);
        select.accept(first);
        selectedProperty.apply(first).addListener((observable, oldValue, selected) -> {
            if (!selected) {
                items.remove(second);
                items.add(replacement);
            }
        });

        select.accept(second);

        assertEquals(2, items.size());
        assertSame(replacement, items.get(1));
        assertFalse(selectedProperty.apply(first).get());
        assertFalse(selectedProperty.apply(second).get());
        assertFalse(selectedProperty.apply(replacement).get());
    }
}
