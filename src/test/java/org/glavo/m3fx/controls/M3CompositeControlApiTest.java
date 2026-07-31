// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.event.ActionEvent;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/// Verifies ownership and synchronization of properties exposed by composite M3FX controls.
@NotNullByDefault
final class M3CompositeControlApiTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that search controls own their public properties and action events.
    @Test
    void searchPropertiesAndEventsBelongToPublicControls() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar();
            assertOwnedBy(searchBar, searchBar.textProperty());
            assertOwnedBy(searchBar, searchBar.promptTextProperty());
            assertOwnedBy(searchBar, searchBar.leadingProperty());
            assertOwnedBy(searchBar, searchBar.activeProperty());
            assertOwnedBy(searchBar, searchBar.onActionProperty());

            M3SearchView searchView = new M3SearchView();
            assertOwnedBy(searchView, searchView.textProperty());
            assertOwnedBy(searchView, searchView.promptTextProperty());
            assertOwnedBy(searchView, searchView.leadingProperty());
            assertOwnedBy(searchView, searchView.activeProperty());
            assertOwnedBy(searchView, searchView.onActionProperty());

            AtomicReference<ActionEvent> firedEvent = new AtomicReference<>();
            searchView.setOnAction(firedEvent::set);
            searchView.fire();

            assertSame(searchView, firedEvent.get().getSource());
            assertSame(searchView, firedEvent.get().getTarget());
        });
    }

    /// Verifies that picker fields own public editor and decoration properties while synchronizing internal controls.
    @Test
    void pickerFieldPropertiesBelongToPickerField() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField field = new M3DatePickerField();
            assertOwnedBy(field, field.valueProperty());
            assertOwnedBy(field, field.textProperty());
            assertOwnedBy(field, field.variantProperty());
            assertOwnedBy(field, field.characterCounterVisibleProperty());
            assertOwnedBy(field, field.characterLimitEnforcedProperty());
            assertOwnedBy(field, field.characterLimitProperty());
            assertOwnedBy(field, field.labelTextProperty());
            assertOwnedBy(field, field.supportingTextProperty());
            assertOwnedBy(field, field.errorTextProperty());
            assertOwnedBy(field, field.invalidTextErrorTextProperty());
            assertOwnedBy(field, field.rangeErrorTextProperty());

            field.setText("2026-07-16");
            assertEquals("2026-07-16", field.getEditor().getText());
            field.getEditor().setText("2026-07-17");
            assertEquals("2026-07-17", field.getText());
        });
    }

    /// Verifies that a date-range field owns both endpoint property sets.
    @Test
    void dateRangeFieldPropertiesBelongToRangeField() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField();
            assertOwnedBy(field, field.startDateProperty());
            assertOwnedBy(field, field.endDateProperty());
            assertOwnedBy(field, field.selectionProperty());
            assertOwnedBy(field, field.startTextProperty());
            assertOwnedBy(field, field.endTextProperty());
            assertOwnedBy(field, field.startVariantProperty());
            assertOwnedBy(field, field.endVariantProperty());
            assertOwnedBy(field, field.startErrorTextProperty());
            assertOwnedBy(field, field.endErrorTextProperty());
            assertOwnedBy(field, field.startLabelTextProperty());
            assertOwnedBy(field, field.endLabelTextProperty());
            assertOwnedBy(field, field.startSupportingTextProperty());
            assertOwnedBy(field, field.endSupportingTextProperty());

            field.setStartText("2026-07-16");
            field.setEndText("2026-07-18");
            assertEquals("2026-07-16", field.getStartEditor().getText());
            assertEquals("2026-07-18", field.getEndEditor().getText());

            M3DateRangePicker picker = new M3DateRangePicker();
            assertOwnedBy(picker, picker.startDateProperty());
            assertOwnedBy(picker, picker.endDateProperty());
            assertOwnedBy(picker, picker.selectionProperty());
        });
    }

    /// Verifies that picker dialogs own selected-value properties while keeping their picker content synchronized.
    @Test
    void pickerDialogValuePropertiesBelongToDialogs() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerDialog dateDialog = new M3DatePickerDialog();
            M3TimePickerDialog timeDialog = new M3TimePickerDialog();
            assertOwnedBy(dateDialog, dateDialog.valueProperty());
            assertOwnedBy(timeDialog, timeDialog.valueProperty());

            LocalDate date = LocalDate.of(2026, 7, 16);
            LocalTime time = LocalTime.of(18, 30);
            dateDialog.getPicker().setValue(date);
            timeDialog.getPicker().setValue(time);
            assertEquals(date, dateDialog.getValue());
            assertEquals(time, timeDialog.getValue());
        });
    }

    /// Asserts that a JavaFX property reports the public control that exposes it as its bean.
    private static void assertOwnedBy(Object owner, ReadOnlyProperty<?> property) {
        assertSame(owner, property.getBean());
    }
}
