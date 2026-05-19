// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.skins.M3FormPaneSkin;
import org.glavo.m3fx.skins.M3FormRowSkin;
import org.glavo.m3fx.skins.M3FormSectionSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests form helper controls, skins, and accessibility metadata.
@NotNullByDefault
final class M3FormControlsTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    /// Verifies that form panes expose items through the skin and accessibility metadata.
    @Test
    void formPaneMirrorsItemsIntoSkin() {
        runOnFxThread(() -> {
            M3FormRow first = new M3FormRow("Name", new Label("Content"));
            M3FormRow second = new M3FormRow("Email", new Label("Content"));
            M3FormPane form = new M3FormPane(first);
            form.addItem(second);
            form.setContentPadding(20.0);
            form.setRowSpacing(10.0);

            applyCss(form);

            assertInstanceOf(M3FormPaneSkin.class, form.getSkin());
            assertEquals(2, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(first, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(second, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

            Node content = form.lookup("." + M3FormPane.CONTENT_STYLE_CLASS);
            VBox contentBox = assertInstanceOf(VBox.class, content);
            assertEquals(2, contentBox.getChildren().size());
            assertEquals(10.0, contentBox.getSpacing());
        });
    }

    /// Verifies that form sections update title, supporting text, and content slots.
    @Test
    void formSectionMirrorsHeaderAndContent() {
        runOnFxThread(() -> {
            M3FormRow row = new M3FormRow("Field", new Label("Value"));
            M3FormSection section = new M3FormSection("Account", "Profile fields", row);
            section.setContentSpacing(18.0);

            applyCss(section);

            assertInstanceOf(M3FormSectionSkin.class, section.getSkin());
            assertEquals("Account", section.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(1, section.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(row, section.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

            Label title = assertInstanceOf(Label.class, section.lookup("." + M3FormSection.TITLE_STYLE_CLASS));
            Label supporting =
                    assertInstanceOf(Label.class, section.lookup("." + M3FormSection.SUPPORTING_TEXT_STYLE_CLASS));
            VBox content = assertInstanceOf(VBox.class, section.lookup("." + M3FormSection.CONTENT_STYLE_CLASS));

            assertEquals("Account", title.getText());
            assertEquals("Profile fields", supporting.getText());
            assertEquals(18.0, content.getSpacing());
            assertEquals(1, content.getChildren().size());
        });
    }

    /// Verifies that form rows update text, slots, metrics, and accessibility metadata.
    @Test
    void formRowMirrorsTextAndSlotsIntoSkin() {
        runOnFxThread(() -> {
            Label content = new Label("Content");
            M3Button trailing = new M3Button("Action");
            M3FormRow row = new M3FormRow("Display name", "Visible to collaborators", content, trailing);
            row.setLabelWidth(144.0);
            row.setColumnSpacing(12.0);
            row.setRowMinHeight(72.0);

            applyCss(row);

            assertInstanceOf(M3FormRowSkin.class, row.getSkin());
            assertEquals(AccessibleRole.PARENT, row.getAccessibleRole());
            assertEquals("Display name", row.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(2, row.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(content, row.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertSame(content, row.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(trailing, row.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

            Label label = assertInstanceOf(Label.class, row.lookup("." + M3FormRow.LABEL_STYLE_CLASS));
            Label supporting = assertInstanceOf(Label.class, row.lookup("." + M3FormRow.SUPPORTING_TEXT_STYLE_CLASS));
            VBox textColumn = assertInstanceOf(VBox.class, row.lookup("." + M3FormRow.TEXT_COLUMN_STYLE_CLASS));

            assertEquals("Display name", label.getText());
            assertEquals("Visible to collaborators", supporting.getText());
            assertEquals(144.0, textColumn.getPrefWidth());
            assertNotNull(content.getParent());
            assertNotNull(trailing.getParent());
        });
    }

    /// Verifies that form rows reject a node reused across both slots.
    @Test
    void formRowRejectsDuplicateSlotNode() {
        Label content = new Label("Content");
        M3FormRow row = new M3FormRow();
        row.setContent(content);

        assertThrows(IllegalArgumentException.class, () -> row.setTrailing(content));
    }

    /// Verifies that form helpers expose their split user-agent stylesheet.
    @Test
    void formControlsExposeUserAgentStylesheet() {
        assertTrue(new M3FormPane().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3FormSection().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3FormRow().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
    }

    /// Applies the M3FX theme to a node and creates its skin.
    private static void applyCss(Node node) {
        Pane root = new Pane(node);
        Scene scene = new Scene(root, 640.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }

    /// Runs one assertion block on the JavaFX application thread.
    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }

        @Nullable Throwable throwable = failure.get();
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        if (throwable != null) {
            throw new AssertionError(throwable);
        }
    }
}
