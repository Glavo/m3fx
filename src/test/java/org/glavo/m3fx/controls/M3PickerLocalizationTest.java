// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.localization.M3Localizer;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests inherited picker localization, application overrides, localized input, and skin disposal.
@NotNullByDefault
final class M3PickerLocalizationTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Dialog titles and actions resolve context from their pane and refresh on reparenting.
    @Test
    void dialogDefaultsFollowInheritedPaneContext() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerDialog date = new M3DatePickerDialog();
            M3DateRangePickerDialog range = new M3DateRangePickerDialog();
            M3TimePickerDialog time = new M3TimePickerDialog();
            Pane firstRoot = new Pane(date.getDialogPane(), range.getDialogPane(), time.getDialogPane());
            TestLocalizer first = new TestLocalizer("First");
            TestLocalizer second = new TestLocalizer("Second");
            M3Localizer.install(firstRoot, first);

            assertEquals(first.getSelectDateText(), date.getDialogPane().getHeaderText());
            assertEquals(first.getSelectDateRangeText(), range.getDialogPane().getHeaderText());
            assertEquals(first.getSelectTimeText(), time.getDialogPane().getHeaderText());
            for (M3Dialog dialog : List.of(date, range, time)) {
                assertEquals(first.getCancelText(), dialog.getDialogPane().getActions().get(0).getText());
                assertEquals(first.getConfirmText(), dialog.getDialogPane().getActions().get(1).getText());
            }

            M3Localizer.install(time.getDialogPane(), second);
            assertEquals(second.getSelectTimeText(), time.getDialogPane().getHeaderText());
            M3Localizer.uninstall(time.getDialogPane());
            assertEquals(first.getSelectTimeText(), time.getDialogPane().getHeaderText());

            Pane secondRoot = new Pane();
            M3Localizer.install(secondRoot, second);
            firstRoot.getChildren().remove(date.getDialogPane());
            secondRoot.getChildren().add(date.getDialogPane());
            assertEquals(second.getSelectDateText(), date.getDialogPane().getHeaderText());
            assertEquals(first.getSelectDateRangeText(), range.getDialogPane().getHeaderText());
        });
    }

    /// Existing pane properties retain same-value assignments and bound headlines across localization changes.
    @Test
    void panePropertiesPreserveEqualDefaultsAndBoundText() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerDialog date = new M3DatePickerDialog();
            M3DateRangePickerDialog range = new M3DateRangePickerDialog();
            M3TimePickerDialog time = new M3TimePickerDialog();
            String dateTitle = date.getDialogPane().getHeaderText();
            String rangeTitle = range.getDialogPane().getHeaderText();
            String timeTitle = time.getDialogPane().getHeaderText();
            date.getDialogPane().setHeaderText(dateTitle);
            range.getDialogPane().headerTextProperty().set(rangeTitle);
            SimpleStringProperty source = new SimpleStringProperty(timeTitle);
            time.getDialogPane().headerTextProperty().bind(source);
            TestLocalizer localizer = new TestLocalizer("Changed");
            for (M3Dialog dialog : List.of(date, range, time)) {
                M3Localizer.install(dialog.getDialogPane(), localizer);
            }

            assertEquals(dateTitle, date.getDialogPane().getHeaderText());
            assertEquals(rangeTitle, range.getDialogPane().getHeaderText());
            assertEquals(timeTitle, time.getDialogPane().getHeaderText());
            source.set("Appointment");
            assertEquals("Appointment", time.getDialogPane().getHeaderText());
            time.getDialogPane().headerTextProperty().unbind();
            M3Localizer.install(time.getDialogPane(), new TestLocalizer("Next"));
            assertEquals("Appointment", time.getDialogPane().getHeaderText());
        });
    }

    /// Existing pane and button customization paths are not overwritten by localization.
    @Test
    void existingPresentationOverridesAndBindingsArePreserved() {
        FxTestUtils.runOnFxThread(() -> {
            for (M3Dialog dialog : List.of(
                    new M3DatePickerDialog(), new M3DateRangePickerDialog(), new M3TimePickerDialog())) {
                M3DialogPane pane = dialog.getDialogPane();
                pane.setHeaderText("Appointment");
                M3Button cancel = pane.getActions().get(0);
                String originalCancel = cancel.getText();
                cancel.setText("Dismiss");
                cancel.setText(originalCancel);
                SimpleStringProperty confirmation = new SimpleStringProperty("Save");
                pane.getActions().get(1).textProperty().bind(confirmation);
                M3Localizer.install(pane, new TestLocalizer("Changed"));
                assertEquals("Appointment", pane.getHeaderText());
                assertEquals(originalCancel, cancel.getText());
                assertEquals("Save", pane.getActions().get(1).getText());
                confirmation.set("Save again");
                assertEquals("Save again", pane.getActions().get(1).getText());
            }
        });
    }

    /// Binding existing headers and actions to their current value permanently stops default updates.
    @Test
    void sameValueBindingsRemainApplicationOwnedAfterUnbinding() {
        FxTestUtils.runOnFxThread(() -> {
            for (M3Dialog dialog : List.of(
                    new M3DatePickerDialog(), new M3DateRangePickerDialog(), new M3TimePickerDialog())) {
                M3DialogPane pane = dialog.getDialogPane();
                String header = pane.getHeaderText();
                String cancel = pane.getActions().get(0).getText();
                String confirm = pane.getActions().get(1).getText();
                pane.headerTextProperty().bind(new SimpleStringProperty(header));
                pane.headerTextProperty().unbind();
                for (M3Button action : pane.getActions()) {
                    action.textProperty().bind(new SimpleStringProperty(action.getText()));
                    action.textProperty().unbind();
                }
                M3Localizer.install(pane, new TestLocalizer("Changed"));
                assertEquals(header, pane.getHeaderText());
                assertEquals(cancel, pane.getActions().get(0).getText());
                assertEquals(confirm, pane.getActions().get(1).getText());
            }
        });
    }

    /// Unobservable same-value action assignments retain native JavaFX default-following behavior.
    @Test
    void sameValueActionAssignmentsContinueFollowingLocalization() {
        FxTestUtils.runOnFxThread(() -> {
            for (M3Dialog dialog : List.of(
                    new M3DatePickerDialog(), new M3DateRangePickerDialog(), new M3TimePickerDialog())) {
                M3DialogPane pane = dialog.getDialogPane();
                for (M3Button action : pane.getActions()) {
                    action.setText(action.getText());
                }
                TestLocalizer first = new TestLocalizer("First");
                M3Localizer.install(pane, first);
                assertEquals(first.getCancelText(), pane.getActions().get(0).getText());
                assertEquals(first.getConfirmText(), pane.getActions().get(1).getText());
                TestLocalizer second = new TestLocalizer("Second");
                M3Localizer.install(pane, second);
                assertEquals(second.getCancelText(), pane.getActions().get(0).getText());
                assertEquals(second.getConfirmText(), pane.getActions().get(1).getText());
            }
        });
    }

    /// Localized labels and digits update in place and decimal input commits without ASCII assumptions.
    @Test
    void timeSkinRefreshesLabelsAndAcceptsLocalizedDigits() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(9, 15));
            Pane root = attachPicker(picker);
            ButtonBase hour = node(picker, ".m3-time-picker-hour-display", ButtonBase.class);
            ButtonBase minute = node(picker, ".m3-time-picker-minute-display", ButtonBase.class);
            TestLocalizer localizer = new TestLocalizer("Custom");
            M3Localizer.install(root, localizer);
            assertSame(hour, node(picker, ".m3-time-picker-hour-display", ButtonBase.class));
            assertEquals(localizer.getHourText(), hour.getAccessibleText());
            assertEquals(localizer.formatHour(9), hour.getText());
            assertEquals(localizer.formatMinute(15), minute.getText());
            assertEquals(localizer.getClockDialText(), node(picker, ".m3-time-picker-dial", Node.class).getAccessibleText());
            ButtonBase mode = node(picker, ".m3-time-picker-mode-button", ButtonBase.class);
            assertEquals(localizer.getUseKeyboardInputText(), mode.getAccessibleText());
            picker.setInputMode(true);
            assertEquals(localizer.getUseClockDialText(), mode.getAccessibleText());
            for (Node label : picker.lookupAll(".m3-time-picker-input-label")) {
                String text = assertInstanceOf(Label.class, label).getText();
                assertTrue(text.equals(localizer.getHourText()) || text.equals(localizer.getMinuteText()));
            }
            TextField hourInput = input(picker, localizer.getHourText());
            TextField minuteInput = input(picker, localizer.getMinuteText());
            hourInput.setText(localizer.formatHour(8));
            minuteInput.setText(localizer.formatMinute(30));
            minuteInput.fireEvent(new ActionEvent());
            assertEquals(LocalTime.of(8, 30), picker.getValue());
        });
    }

    /// Disposed skins release observers while replacement skins receive the current context.
    @Test
    void disposedSkinStopsRefreshingAndReplacementUsesCurrentContext() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.NOON);
            Pane root = attachPicker(picker);
            M3Localizer.install(root, new TestLocalizer("First"));
            ButtonBase oldHour = node(picker, ".m3-time-picker-hour-display", ButtonBase.class);
            picker.setSkin(null);
            TestLocalizer second = new TestLocalizer("Second");
            M3Localizer.install(root, second);
            assertEquals("First hour", oldHour.getAccessibleText());
            picker.setSkin(new M3TimePickerSkin(picker));
            assertEquals(second.getHourText(), node(picker, ".m3-time-picker-hour-display", ButtonBase.class).getAccessibleText());
        });
    }

    /// Dialog-owned mode descriptions resolve from the pane even without a picker skin.
    @Test
    void dialogModeActionUsesPaneContext() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePickerDialog dialog = new M3TimePickerDialog();
            TestLocalizer localizer = new TestLocalizer("Dialog");
            M3Localizer.install(dialog.getDialogPane(), localizer);
            dialog.getDialogPane().setSkin(dialog.getDialogPane().createDefaultSkin());
            Node mode = assertInstanceOf(Node.class,
                    dialog.getDialogPane().lookup(".m3-time-picker-mode-button"));
            assertEquals(localizer.getUseKeyboardInputText(), mode.getAccessibleText());
            dialog.getPicker().setInputMode(true);
            assertEquals(localizer.getUseClockDialText(), mode.getAccessibleText());
            mode.accessibleTextProperty().bind(new SimpleStringProperty("Custom mode"));
            M3Localizer.install(dialog.getDialogPane(), new TestLocalizer("Next"));
            assertEquals("Custom mode", mode.getAccessibleText());
        });
    }

    /// Installs a skin in a scene without requiring a native window.
    private static Pane attachPicker(M3TimePicker picker) {
        Pane root = new Pane(picker);
        new Scene(root, 400, 500);
        picker.setSkin(new M3TimePickerSkin(picker));
        return root;
    }

    /// Returns a required retained presentation node.
    private static <T extends Node> T node(M3TimePicker picker, String selector, Class<T> type) {
        return assertInstanceOf(type, picker.lookup(selector));
    }

    /// Returns the editor with the supplied localized accessibility description.
    private static TextField input(M3TimePicker picker, String description) {
        for (Node node : picker.lookupAll(".m3-time-picker-input-field")) {
            if (description.equals(node.getAccessibleText())) {
                return assertInstanceOf(TextField.class, node);
            }
        }
        throw new AssertionError("Missing time editor: " + description);
    }

    /// Supplies recognizable translations and Arabic-Indic digits independently of JVM defaults.
    @NotNullByDefault
    private static final class TestLocalizer extends M3Localizer {
        /// Distinguishes localizer identities without changing the locale.
        private final String prefix;

        /// Creates one test context with a fixed locale.
        private TestLocalizer(String prefix) {
            super(Locale.US);
            this.prefix = prefix;
        }

        /// Returns the cancellation translation.
        @Override public String getCancelText() { return prefix + " cancel"; }

        /// Returns the confirmation translation.
        @Override public String getConfirmText() { return prefix + " confirm"; }

        /// Returns the single-date title.
        @Override public String getSelectDateText() { return prefix + " date"; }

        /// Returns the range title.
        @Override public String getSelectDateRangeText() { return prefix + " range"; }

        /// Returns the time title.
        @Override public String getSelectTimeText() { return prefix + " time"; }

        /// Returns the hour description.
        @Override public String getHourText() { return prefix + " hour"; }

        /// Returns the minute description.
        @Override public String getMinuteText() { return prefix + " minute"; }

        /// Returns the keyboard mode description.
        @Override public String getUseKeyboardInputText() { return prefix + " keyboard"; }

        /// Returns the dial mode description.
        @Override public String getUseClockDialText() { return prefix + " dial mode"; }

        /// Returns the dial description.
        @Override public String getClockDialText() { return prefix + " dial"; }

        /// Returns the morning label.
        @Override public String getAmText() { return prefix + " morning"; }

        /// Returns the afternoon label.
        @Override public String getPmText() { return prefix + " afternoon"; }

        /// Formats two Arabic-Indic hour digits.
        @Override public String formatHour(int hour) { return digits(hour); }

        /// Formats two Arabic-Indic minute digits.
        @Override public String formatMinute(int minute) { return digits(minute); }

        /// Formats the two digits used by this test context.
        private static String digits(int value) {
            return "" + (char) ('\u0660' + value / 10) + (char) ('\u0660' + value % 10);
        }
    }
}
