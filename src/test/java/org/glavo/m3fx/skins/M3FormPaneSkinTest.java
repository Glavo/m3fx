// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3FormPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies form-item ownership, callback-driven synchronization, and independent padding layers.
@NotNullByDefault
final class M3FormPaneSkinTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Reconciles edits from a row's parent listener while the initial item list is being mounted.
    @ParameterizedTest
    @ValueSource(strings = {"append", "replace", "clear"})
    void reconcilesItemsChangedDuringInitialMount(String mutation) {
        FxTestUtils.runOnFxThread(() -> {
            M3FormPane form = new M3FormPane();
            Region first = new Region();
            Region second = new Region();
            form.getItems().add(first);
            AtomicBoolean update = new AtomicBoolean(true);
            first.parentProperty().addListener((observable, previous, parent) -> {
                if (parent != null && update.getAndSet(false)) {
                    switch (mutation) {
                        case "append" -> form.getItems().add(second);
                        case "replace" -> form.getItems().setAll(second);
                        case "clear" -> form.getItems().clear();
                        default -> throw new AssertionError(mutation);
                    }
                }
            });

            try {
                assertNoListenerFailures(() -> {
                    form.setSkin(new M3FormPaneSkin(form));
                    VBox container = assertInstanceOf(VBox.class, form.getChildrenUnmodifiable().get(0));
                    @Unmodifiable List<Node> expected = switch (mutation) {
                        case "append" -> List.of(first, second);
                        case "replace" -> List.of(second);
                        case "clear" -> List.of();
                        default -> throw new AssertionError(mutation);
                    };
                    assertEquals(expected, form.getItems());
                    assertEquals(expected, container.getChildren());
                });
            } finally {
                form.setSkin(null);
            }
        });
    }

    /// Releases item references and token listeners once while tolerating repeated disposal.
    @Test
    void disposalIsIdempotentAndReleasesContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3FormPane form = new M3FormPane();
            Region row = new Region();
            form.getItems().add(row);
            M3FormPaneSkin skin = new M3FormPaneSkin(form);
            form.setSkin(skin);
            VBox container = assertInstanceOf(VBox.class, row.getParent());
            form.setSkin(null);
            assertDoesNotThrow(skin::dispose);
            form.getItems().add(new Region());
            form.setContentPadding(30);
            form.setRowSpacing(12);

            assertNull(row.getParent());
            assertNull(container.getParent());
            assertTrue(container.getChildren().isEmpty());
            assertEquals(2, form.getItems().size());

            form.setSkin(new M3FormPaneSkin(form));
            VBox replacement = assertInstanceOf(VBox.class, row.getParent());
            assertNotSame(container, replacement);
            assertEquals(form.getItems(), replacement.getChildren());
            form.setRowSpacing(7);
            assertEquals(7, replacement.getSpacing());
            assertTrue(container.getChildren().isEmpty());
            form.setSkin(null);
        });
    }

    /// Tolerates removal of the skin from an observer of its content spacing.
    @Test
    void spacingObserverCanDisposeSkin() {
        FxTestUtils.runOnFxThread(() -> {
            M3FormPane form = new M3FormPane();
            form.getItems().add(new Region());
            form.setSkin(new M3FormPaneSkin(form));
            VBox container = assertInstanceOf(VBox.class, form.getChildrenUnmodifiable().get(0));
            container.spacingProperty().addListener(observable -> form.setSkin(null));

            assertNoListenerFailures(() -> form.setRowSpacing(12));

            assertNull(form.getSkin());
            assertTrue(container.getChildren().isEmpty());
        });
    }

    /// Adds authored container padding to token margins and outer insets without capping the form's maximum size.
    @Test
    void preservesPaddingLayersAndUnboundedMaximumSize() {
        FxTestUtils.runOnFxThread(() -> {
            M3FormPane form = new M3FormPane();
            form.setPadding(new Insets(5, 7, 11, 13));
            form.setContentPadding(12);
            Region row = new Region();
            row.setMinSize(50, 30);
            row.setPrefSize(70, 30);
            form.getItems().add(row);
            form.setSkin(new M3FormPaneSkin(form));
            VBox container = assertInstanceOf(VBox.class, row.getParent());
            container.setStyle("-fx-padding: 10px 20px 30px 40px;");
            container.setMaxSize(180, 80);
            Pane root = new Pane(form);
            form.setManaged(false);
            new Scene(root, 300, 200);
            root.applyCss();

            assertEquals(154, form.minWidth(-1));
            assertEquals(174, form.prefWidth(-1));
            assertEquals(110, form.minHeight(300));
            assertEquals(110, form.prefHeight(300));
            assertEquals(Double.MAX_VALUE, form.maxWidth(-1));
            assertEquals(Double.MAX_VALUE, form.maxHeight(-1));
            form.resize(300, 200);
            root.layout();
            assertEquals(65, container.getLayoutX() + row.getLayoutX());
            assertEquals(27, container.getLayoutY() + row.getLayoutY());
            assertEquals(196, row.getWidth());

            form.setContentPadding(16);
            form.layout();
            assertEquals(118, form.prefHeight(300));
            assertEquals(69, container.getLayoutX() + row.getLayoutX());
            assertEquals(31, container.getLayoutY() + row.getLayoutY());
            assertEquals(188, row.getWidth());
            form.setSkin(null);
        });
    }

    /// Captures exceptions JavaFX reports through the thread handler instead of propagating from a listener.
    private static void assertNoListenerFailures(Runnable action) {
        List<Throwable> failures = new ArrayList<>();
        Thread thread = Thread.currentThread();
        Thread.@Nullable UncaughtExceptionHandler previousHandler = thread.getUncaughtExceptionHandler();
        thread.setUncaughtExceptionHandler((ignored, failure) -> failures.add(failure));
        try {
            action.run();
            assertTrue(failures.isEmpty(), failures::toString);
        } finally {
            thread.setUncaughtExceptionHandler(previousHandler);
        }
    }
}
