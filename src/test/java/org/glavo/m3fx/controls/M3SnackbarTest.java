// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the observable message contract of [M3Snackbar].
@NotNullByDefault
final class M3SnackbarTest {
    /// Verifies property identity, mutation, and text-driven action visibility.
    @Test
    void propertiesExposeObservableContentAndAffordances() {
        Runnable action = () -> {
        };
        M3Snackbar snackbar = new M3Snackbar("Saved");

        assertSame(snackbar, snackbar.textProperty().getBean());
        assertSame(snackbar, snackbar.actionTextProperty().getBean());
        assertSame(snackbar, snackbar.actionProperty().getBean());
        assertSame(snackbar, snackbar.closeButtonVisibleProperty().getBean());
        assertEquals("text", snackbar.textProperty().getName());
        assertEquals("actionText", snackbar.actionTextProperty().getName());
        assertEquals("action", snackbar.actionProperty().getName());
        assertEquals("closeButtonVisible", snackbar.closeButtonVisibleProperty().getName());

        assertEquals("Saved", snackbar.getText());
        assertEquals("", snackbar.getActionText());
        assertNull(snackbar.getAction());
        assertFalse(snackbar.hasAction());
        assertFalse(snackbar.isCloseButtonVisible());

        snackbar.setText("Deleted");
        snackbar.setActionText("Undo");
        snackbar.setAction(action);
        snackbar.setCloseButtonVisible(true);
        assertEquals("Deleted", snackbar.getText());
        assertEquals("Undo", snackbar.getActionText());
        assertSame(action, snackbar.getAction());
        assertTrue(snackbar.hasAction());
        assertTrue(snackbar.isCloseButtonVisible());

        snackbar.setActionText("  ");
        snackbar.setAction(null);
        assertFalse(snackbar.hasAction());
        assertNull(snackbar.getAction());
    }

    /// Verifies bound localized text updates the same message model without replacement.
    @Test
    void textPropertiesSupportLocalizationBindings() {
        StringProperty localizedText = new SimpleStringProperty("Project saved");
        StringProperty localizedAction = new SimpleStringProperty("Undo");
        M3Snackbar snackbar = new M3Snackbar("Placeholder");

        snackbar.textProperty().bind(localizedText);
        snackbar.actionTextProperty().bind(localizedAction);
        assertEquals("Project saved", snackbar.getText());
        assertEquals("Undo", snackbar.getActionText());
        assertTrue(snackbar.hasAction());

        localizedText.set("Projekt gespeichert");
        localizedAction.set("");
        assertEquals("Projekt gespeichert", snackbar.getText());
        assertEquals("", snackbar.getActionText());
        assertFalse(snackbar.hasAction());
    }

    /// Verifies setter-based action configuration and the nullable no-op action contract.
    @Test
    void settersPreserveActionSemantics() {
        Runnable action = () -> {
        };
        M3Snackbar plain = new M3Snackbar("Saved");
        M3Snackbar actionable = new M3Snackbar("Deleted");
        actionable.setActionText("Undo");
        actionable.setAction(action);
        M3Snackbar dismissOnlyAction = new M3Snackbar("Archived");
        dismissOnlyAction.setActionText("Dismiss");
        dismissOnlyAction.setAction(null);

        assertFalse(plain.hasAction());
        assertTrue(actionable.hasAction());
        assertSame(action, actionable.getAction());
        assertTrue(dismissOnlyAction.hasAction());
        assertNull(dismissOnlyAction.getAction());
    }

    /// Verifies that required message values reject null or blank input.
    @SuppressWarnings("DataFlowIssue")
    @Test
    void requiredValuesRejectNullAndBlankInput() {
        M3Snackbar snackbar = new M3Snackbar("Saved");

        assertThrows(NullPointerException.class, () -> new M3Snackbar(null));
        assertThrows(IllegalArgumentException.class, () -> new M3Snackbar(""));
        assertThrows(IllegalArgumentException.class, () -> new M3Snackbar(" \t\n"));
        assertThrows(NullPointerException.class, () -> snackbar.setText(null));
        assertThrows(IllegalArgumentException.class, () -> snackbar.setText(""));
        assertThrows(IllegalArgumentException.class, () -> snackbar.setText("  "));
        assertThrows(NullPointerException.class, () -> snackbar.setActionText(null));
    }
}
