// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies select-setting commits and coherent menu content under reentrant application callbacks.
@NotNullByDefault
final class M3SelectSettingContentTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps previously obtained list handles and listeners connected after backing-list replacement.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void itemsHandleFollowsReplacement(boolean popup) {
        withChoices(popup, owner -> {
            ObservableList<String> items = owner.getItems();
            @Nullable ObservableList<String> previous = owner.itemsProperty().get();
            AtomicInteger changes = new AtomicInteger();
            items.addListener((ListChangeListener<String>) change -> changes.incrementAndGet());
            ObservableList<String> replacement = FXCollections.observableArrayList("three", "four");
            owner.itemsProperty().set(replacement);
            assertSame(items, owner.getItems());
            assertEquals(List.of("three", "four"), items);
            assertEquals(List.of("three", "four"), labels(owner));
            assertEquals(1, changes.get());
            items.add("five");
            assertEquals(List.of("three", "four", "five"), replacement);
            assertEquals(List.of("three", "four", "five"), labels(owner));
            assertEquals(2, changes.get());
            if (previous != null) {
                previous.add("retired");
            }
            assertEquals(2, changes.get());
            assertEquals(List.of("three", "four", "five"), items);
        });
    }

    /// Clears generated content for a null backing list and accepts a later replacement.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullItemsClearMenuAndRecover(boolean popup) {
        withChoices(popup, owner -> {
            ObservableList<String> items = owner.getItems();
            M3MenuItem retired = menuItem(owner, 1);
            owner.itemsProperty().set(null);
            assertSame(items, owner.getItems());
            assertTrue(items.isEmpty());
            assertTrue(owner.getMenuItems().isEmpty());
            assertTrue(owner.getMenu().getSelectedItems().isEmpty());
            assertEquals(0, owner.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals("one", owner.getValue());
            assertThrows(UnsupportedOperationException.class, () -> items.add("two"));
            retired.fire();
            assertEquals("one", owner.getValue());
            owner.itemsProperty().set(FXCollections.observableArrayList("one", "three"));
            assertEquals(List.of("one", "three"), items);
            assertEquals(List.of(menuItem(owner, 0)), owner.getMenu().getSelectedItems());
            menuItem(owner, 1).fire();
            assertEquals("three", owner.getValue());
        });
    }

    /// Follows null and non-null bound sources without replacing the property binding.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void boundItemsFollowSourceLifecycle(boolean popup) {
        withChoices(popup, owner -> {
            ObservableList<String> items = owner.getItems();
            SimpleObjectProperty<@Nullable ObservableList<String>> source = new SimpleObjectProperty<>();
            owner.itemsProperty().bind(source);
            assertTrue(items.isEmpty());
            assertTrue(owner.getMenuItems().isEmpty());
            ObservableList<String> choices = FXCollections.observableArrayList("one", "two");
            source.set(choices);
            assertEquals(List.of("one", "two"), items);
            items.add("three");
            assertEquals(List.of("one", "two", "three"), choices);
            assertEquals(List.of("one", "two", "three"), labels(owner));
            source.set(null);
            assertTrue(items.isEmpty());
            assertTrue(owner.getMenuItems().isEmpty());
            source.set(FXCollections.observableArrayList("four"));
            assertEquals(List.of("four"), labels(owner));
            assertTrue(owner.itemsProperty().isBound());
        });
    }

    /// Rejects popup edits to a bound value without exceptions, row actions, or a stale menu selection.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void boundValuesRejectMenuCommits(boolean popup) {
        withChoices(popup, owner -> {
            SimpleObjectProperty<String> source = new SimpleObjectProperty<>("one");
            owner.valueProperty().bind(source);
            AtomicInteger actions = new AtomicInteger();
            owner.setOnAction(event -> actions.incrementAndGet());
            AtomicInteger selectionChanges = new AtomicInteger();
            owner.getMenu().getSelectedItems().addListener(
                    (ListChangeListener<M3MenuItem>) change -> selectionChanges.incrementAndGet());
            menuItem(owner, 1).fire();
            assertEquals("one", owner.getValue());
            assertEquals(List.of(menuItem(owner, 0)), owner.getMenu().getSelectedItems());
            assertEquals(0, actions.get());
            assertEquals(0, selectionChanges.get());
            if (popup) {
                assertTrue(owner.isShowing());
            }
            source.set("two");
            assertEquals(List.of(menuItem(owner, 1)), owner.getMenu().getSelectedItems());
            owner.valueProperty().unbind();
            menuItem(owner, 0).fire();
            assertEquals("one", owner.getValue());
            assertEquals(List.of(menuItem(owner, 0)), owner.getMenu().getSelectedItems());
            assertEquals(1, actions.get());
            assertTrue(!owner.isShowing());
        });
    }

    /// Does not allow a previously generated, removed node to commit its former choice.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void removedItemsCannotCommitChoices(boolean popup) {
        withChoices(popup, owner -> {
            M3MenuItem retired = menuItem(owner, 1);
            AtomicInteger actions = new AtomicInteger();
            owner.setOnAction(event -> actions.incrementAndGet());
            owner.getItems().setAll("three", "four");
            retired.fire();
            assertEquals("one", owner.getValue());
            assertEquals(0, actions.get());
        });
    }

    /// Rejects a direct menu action after the owner becomes disabled.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void disabledOwnerCannotCommitChoices(boolean popup) {
        withChoices(popup, owner -> {
            AtomicInteger actions = new AtomicInteger();
            owner.setOnAction(event -> actions.incrementAndGet());
            owner.setDisable(true);
            menuItem(owner, 1).fire();
            assertEquals("one", owner.getValue());
            assertEquals(List.of(menuItem(owner, 0)), owner.getMenu().getSelectedItems());
            assertEquals(0, actions.get());
        });
    }

    /// Keeps row-action changes authoritative after the original menu action finishes bubbling.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rowActionCanReplaceChoicesAndValue(boolean popup) {
        withChoices(popup, owner -> {
            AtomicInteger actions = new AtomicInteger();
            owner.setOnAction(event -> {
                actions.incrementAndGet();
                owner.getItems().setAll("three", "four");
                owner.setValue("four");
            });
            menuItem(owner, 1).fire();
            assertEquals("four", owner.getValue());
            assertEquals("four", owner.getTrailingSupportingText());
            assertEquals(List.of(menuItem(owner, 1)), owner.getMenu().getSelectedItems());
            assertEquals(1, actions.get());
        });
    }

    /// Selects the first equal choice consistently for programmatic values and popup actions.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void duplicateValuesKeepSelectionConsistent(boolean popup) {
        withChoices(popup, owner -> {
            owner.getItems().setAll("one", "one");
            menuItem(owner, 1).fire();
            assertEquals("one", owner.getValue());
            assertEquals(List.of(menuItem(owner, 0)), owner.getMenu().getSelectedItems());
        });
    }

    /// Retains the latest selected value when its converter changes the value during row formatting.
    @Test
    void valueConverterCannotRestoreStalePresentation() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>();
            owner.setConverter(value -> {
                if (value.equals("one")) {
                    owner.setValue("two");
                }
                return value.toUpperCase(Locale.ROOT);
            });
            owner.setValue("one");
            assertEquals("two", owner.getValue());
            assertEquals("TWO", owner.getTrailingSupportingText());
        });
    }

    /// Preserves nullable choices and normalizes null converter results to empty presentation.
    @Test
    void nullableChoicesAndConverterResultsRemainSupported() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<@Nullable String> owner = new M3SelectSettingItem<>();
            owner.getItems().add(null);
            owner.getItems().add("one");
            owner.setConverter(value -> null);
            owner.setDescriptionConverter(value -> null);
            assertEquals(List.of("", ""), labels(owner));
            assertEquals("", menuItem(owner, 0).getSupportingText());
        });
    }

    /// Reconciles choices changed by a text converter without iterating a mutated source list.
    @Test
    void converterCanChangeChoices() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>();
            owner.getItems().setAll("one", "two");
            AtomicBoolean first = new AtomicBoolean(true);
            owner.setConverter(value -> {
                if (first.getAndSet(false)) {
                    owner.getItems().add("three");
                }
                return value.toUpperCase(Locale.ROOT);
            });
            assertEquals(List.of("ONE", "TWO", "THREE"), labels(owner));
        });
    }

    /// Restarts formatting when a description converter changes the active text converter.
    @Test
    void converterReplacementUsesOneConsistentSnapshot() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>();
            owner.getItems().setAll("one", "two");
            AtomicBoolean first = new AtomicBoolean(true);
            owner.setDescriptionConverter(value -> {
                if (first.getAndSet(false)) {
                    owner.setConverter(text -> text.toUpperCase(Locale.ROOT));
                }
                return "Details: " + value;
            });
            assertEquals(List.of("ONE", "TWO"), labels(owner));
            assertEquals("Details: one", menuItem(owner, 0).getSupportingText());
        });
    }

    /// Defers source changes triggered while a generated menu list is being published.
    @Test
    void menuObserverCanReplaceChoices() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>();
            AtomicBoolean first = new AtomicBoolean(true);
            owner.getMenuItems().addListener((ListChangeListener<Node>) change -> {
                if (first.getAndSet(false)) {
                    owner.getItems().setAll("three", "four", "five");
                }
            });
            owner.getItems().setAll("one", "two");
            assertEquals(List.of("three", "four", "five"), labels(owner));
            menuItem(owner, 2).fire();
            assertEquals("five", owner.getValue());
        });
    }

    /// Updates labels in place without rebuilding menu nodes or changing the generated content list.
    @Test
    void converterChangesRetainMenuItems() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>();
            owner.getItems().setAll("one", "two");
            M3MenuItem first = menuItem(owner, 0);
            owner.setValue("one");
            AtomicInteger listChanges = new AtomicInteger();
            owner.getMenuItems().addListener((ListChangeListener<Node>) change -> listChanges.incrementAndGet());
            owner.setConverter(value -> value.toUpperCase(Locale.ROOT));
            owner.setDescriptionConverter(value -> "Details: " + value);
            assertSame(first, menuItem(owner, 0));
            assertEquals(0, listChanges.get());
            assertEquals("ONE", first.getHeadlineText());
            assertEquals("Details: one", first.getSupportingText());
            assertTrue(first.isSelected());
            owner.setDescriptionConverter(null);
            assertEquals("", first.getSupportingText());
            assertSame(first, menuItem(owner, 0));
        });
    }

    /// Updates an already skinned popup without creating detached, unresolved replacement nodes.
    @Test
    void visibleMenuConverterChangesKeepNodesAndStyles() {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>("Choice");
            owner.getItems().setAll("one", "two");
            StackPane root = new StackPane(owner);
            Scene scene = new Scene(root, 400.0, 200.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            stage.setScene(scene);
            try {
                stage.show();
                root.applyCss();
                root.layout();
                owner.showMenu();
                assertTrue(owner.isShowing());
                M3MenuItem first = menuItem(owner, 0);
                FxTestUtils.assertNoCssWarnings(() -> {
                    owner.setConverter(value -> value.toUpperCase(Locale.ROOT));
                    owner.getMenu().applyCss();
                });
                assertSame(first, menuItem(owner, 0));
                assertEquals("ONE", first.getHeadlineText());
            } finally {
                owner.hideMenu();
                stage.close();
            }
        });
    }

    /// Exercises the same owned menu before skin installation or in a showing native popup.
    private static void withChoices(boolean popup, Consumer<M3SelectSettingItem<String>> task) {
        runWithoutListenerFailures(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>("Choice");
            owner.getItems().setAll("one", "two");
            owner.setValue("one");
            if (!popup) {
                task.accept(owner);
                return;
            }
            StackPane root = new StackPane(owner);
            Scene scene = new Scene(root, 400.0, 200.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            stage.setScene(scene);
            try {
                stage.show();
                root.applyCss();
                root.layout();
                owner.showMenu();
                assertTrue(owner.isShowing());
                task.accept(owner);
            } finally {
                owner.hideMenu();
                stage.close();
            }
        });
    }

    /// Returns the generated item at an index.
    private static M3MenuItem menuItem(M3SelectSettingItem<?> owner, int index) {
        return assertInstanceOf(M3MenuItem.class, owner.getMenuItems().get(index));
    }

    /// Returns the current menu labels in source order.
    private static @Unmodifiable List<String> labels(M3SelectSettingItem<?> owner) {
        return owner.getMenuItems().stream().map(node -> ((M3MenuItem) node).getHeadlineText()).toList();
    }

    /// Captures JavaFX listener failures in addition to exceptions propagated to the caller.
    private static void runWithoutListenerFailures(Runnable task) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Thread thread = Thread.currentThread();
            @Nullable Thread.UncaughtExceptionHandler previous = thread.getUncaughtExceptionHandler();
            List<Throwable> failures = new ArrayList<>();
            thread.setUncaughtExceptionHandler((ignored, failure) -> failures.add(failure));
            try {
                task.run();
                assertTrue(failures.isEmpty(), failures::toString);
            } finally {
                thread.setUncaughtExceptionHandler(previous);
            }
        });
    }
}
