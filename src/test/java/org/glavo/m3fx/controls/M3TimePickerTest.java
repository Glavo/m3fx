// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotAreaChanged;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotHasColorVariety;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotNodeContainsContrast;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.snapshotImageOnFxThread;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.visualTestColors;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.writeVisualSnapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3TimePicker] API behavior, Dial/Input interaction, geometry, lifecycle, and rendering.
@NotNullByDefault
final class M3TimePickerTest {
    /// The selected pseudo-class used by internal selector nodes.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The landscape pseudo-class applied by the adaptive skin.
    private static final PseudoClass LANDSCAPE_PSEUDO_CLASS = PseudoClass.getPseudoClass("landscape");

    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies value normalization, bounds, minute-step validation, and variant pseudo-classes.
    @Test
    void timePickerPropertiesNormalizeAndValidateRange() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15, 42, 12));

        assertEquals(LocalTime.of(10, 15), picker.getValue());
        assertFalse(picker.isInputMode());
        assertTrue(picker.getPseudoClassStates().contains(PseudoClass.getPseudoClass("dial-mode")));
        assertEquals(24.0, picker.getContainerSpacing(), 0.0001);
        assertEquals(48.0, picker.getDialHandleSize(), 0.0001);
        assertEquals(8.0, picker.getDialCenterSize(), 0.0001);

        picker.setContainerSpacing(20.0);
        picker.setDialHandleSize(40.0);
        picker.setDialCenterSize(6.0);
        assertEquals(20.0, picker.getContainerSpacing(), 0.0001);
        assertEquals(40.0, picker.getDialHandleSize(), 0.0001);
        assertEquals(6.0, picker.getDialCenterSize(), 0.0001);
        assertThrows(IllegalArgumentException.class, () -> picker.setContainerSpacing(-1.0));
        assertThrows(IllegalArgumentException.class, () -> picker.setDialHandleSize(-1.0));
        assertThrows(IllegalArgumentException.class, () -> picker.setDialCenterSize(-1.0));

        picker.setInputMode(true);
        assertTrue(picker.isInputMode());
        assertTrue(picker.getPseudoClassStates().contains(PseudoClass.getPseudoClass("input-mode")));

        picker.setMinTime(LocalTime.of(9, 0, 5));
        picker.setMaxTime(LocalTime.of(17, 30, 5));
        assertEquals(LocalTime.of(9, 0), picker.getMinTime());
        assertEquals(LocalTime.of(17, 30), picker.getMaxTime());
        assertThrows(IllegalArgumentException.class, () -> picker.setValue(LocalTime.of(8, 45)));
        assertThrows(IllegalArgumentException.class, () -> picker.setMinuteStep(7));
        assertThrows(IllegalArgumentException.class, () -> picker.setMinTime(LocalTime.of(18, 0)));

        picker.setMaxTime(LocalTime.of(9, 30));
        assertNull(picker.getValue());
    }

    /// Verifies hour, minute, and period selection through the official 12-hour dial.
    @Test
    void dialSelectsHourMinuteAndPeriod() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            picker.setMinuteStep(15);
            layoutPicker(picker, 328.0, 472.0);

            assertInstanceOf(M3TimePickerSkin.class, picker.getSkin());
            assertEquals(12, visibleDialLabels(picker).size());
            assertDialMetrics(picker);

            buttonByText(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS, "11").fire();
            assertEquals(LocalTime.of(11, 15), picker.getValue());
            assertEquals(12, visibleDialLabels(picker).size());

            buttonByText(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS, "30").fire();
            assertEquals(LocalTime.of(11, 30), picker.getValue());

            buttonByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "PM").fire();
            assertEquals(LocalTime.of(23, 30), picker.getValue());
        });
    }

    /// Verifies that value, format, step, and variant changes retain one fixed 24-label node pool.
    @Test
    void dialRetainsFixedLabelPoolAcrossStructuralChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            Pane root = layoutPicker(picker, 328.0, 472.0);
            Set<Node> originalLabels = identitySet(picker.lookupAll("." + M3TimePicker.DIAL_LABEL_STYLE_CLASS));

            assertEquals(24, originalLabels.size());

            picker.setValue(LocalTime.of(11, 30));
            picker.setUse24HourClock(true);
            picker.setMinuteStep(10);
            picker.setInputMode(true);
            picker.setInputMode(false);
            root.applyCss();
            picker.layout();

            assertEquals(originalLabels, identitySet(
                    picker.lookupAll("." + M3TimePicker.DIAL_LABEL_STYLE_CLASS)
            ));
        });
    }

    /// Verifies 24-hour outer and inner rings, selected state, and bounded-hour availability.
    @Test
    void twentyFourHourDialUsesConcentricRingsAndBounds() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(20, 0));
            picker.setUse24HourClock(true);
            picker.setMinTime(LocalTime.of(9, 0));
            picker.setMaxTime(LocalTime.of(21, 30));
            layoutPicker(picker, 328.0, 472.0);

            List<Node> labels = visibleDialLabels(picker);
            assertEquals(24, labels.size());
            ButtonBase outerEight = buttonByText(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS, "08");
            ButtonBase innerTwenty = buttonByText(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS, "20");
            ButtonBase firstEnabled = buttonByText(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS, "09");
            ButtonBase firstDisabledAfterRange = buttonByText(
                    picker,
                    M3TimePicker.DIAL_LABEL_STYLE_CLASS,
                    "22"
            );
            Node dial = requireNode(picker, M3TimePicker.DIAL_STYLE_CLASS);
            double centerX = dial.getBoundsInLocal().getWidth() / 2.0;
            double centerY = dial.getBoundsInLocal().getHeight() / 2.0;

            assertTrue(outerEight.isDisabled());
            assertFalse(firstEnabled.isDisabled());
            assertTrue(firstDisabledAfterRange.isDisabled());
            assertTrue(innerTwenty.getPseudoClassStates().contains(SELECTED_PSEUDO_CLASS));
            assertTrue(distanceFromDialCenter(outerEight, centerX, centerY)
                    > distanceFromDialCenter(innerTwenty, centerX, centerY) + 30.0);
            assertEquals(0, visibleNodes(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS).size());
        });
    }

    /// Verifies pointer press and release selection, including hour-to-minute progression.
    @Test
    void dialPointerSelectsByAngleAndAdvancesToMinutes() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            layoutPicker(picker, 328.0, 472.0);
            Node dial = requireNode(picker, M3TimePicker.DIAL_STYLE_CLASS);

            dial.fireEvent(primaryMouseEvent(dial, MouseEvent.MOUSE_PRESSED, 232.0, 128.0, true));
            dial.fireEvent(primaryMouseEvent(dial, MouseEvent.MOUSE_RELEASED, 232.0, 128.0, false));
            assertEquals(LocalTime.of(3, 15), picker.getValue());

            dial.fireEvent(primaryMouseEvent(dial, MouseEvent.MOUSE_PRESSED, 128.0, 232.0, true));
            dial.fireEvent(primaryMouseEvent(dial, MouseEvent.MOUSE_RELEASED, 128.0, 232.0, false));
            assertEquals(LocalTime.of(3, 30), picker.getValue());
        });
    }

    /// Verifies keyboard-input fields, validation, period selection, and mode toggle behavior.
    @Test
    void inputModeCommitsFieldsAndReportsInvalidValues() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setInputMode(true);
            layoutPicker(picker, 328.0, 208.0);

            TextField hour = inputByAccessibleText(picker, "Hour");
            TextField minute = inputByAccessibleText(picker, "Minute");
            assertEquals(96.0, hour.getWidth(), 0.5);
            assertEquals(72.0, hour.getHeight(), 0.5);
            assertEquals(96.0, minute.getWidth(), 0.5);
            assertEquals(72.0, minute.getHeight(), 0.5);

            hour.setText("07");
            minute.setText("45");
            minute.fireEvent(new ActionEvent(minute, minute));
            assertEquals(LocalTime.of(7, 45), picker.getValue());

            buttonByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "PM").fire();
            assertEquals(LocalTime.of(19, 45), picker.getValue());

            hour.setText("25");
            hour.fireEvent(new ActionEvent(hour, hour));
            assertEquals(LocalTime.of(19, 45), picker.getValue());
            assertTrue(hour.getPseudoClassStates().contains(PseudoClass.getPseudoClass("invalid")));

            ButtonBase modeButton = buttonByStyle(picker, M3TimePicker.MODE_BUTTON_STYLE_CLASS);
            modeButton.fire();
            assertFalse(picker.isInputMode());
            assertTrue(requireNode(picker, M3TimePicker.DIAL_STYLE_CLASS).isVisible());
        });
    }

    /// Verifies active-unit keyboard navigation, shortcut preservation, and RTL mirroring.
    @Test
    void keyboardNavigationTracksActiveUnitAndOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setMinuteStep(15);
            layoutPicker(picker, 328.0, 472.0);

            picker.fireEvent(keyEvent(KeyCode.RIGHT));
            assertEquals(LocalTime.of(11, 30), picker.getValue());

            buttonByStyle(picker, M3TimePicker.MINUTE_DISPLAY_STYLE_CLASS).fire();
            picker.fireEvent(keyEvent(KeyCode.UP));
            assertEquals(LocalTime.of(11, 45), picker.getValue());

            picker.fireEvent(modifiedKeyEvent(KeyCode.RIGHT, false, true));
            assertEquals(LocalTime.of(11, 45), picker.getValue());

            buttonByStyle(picker, M3TimePicker.HOUR_DISPLAY_STYLE_CLASS).fire();
            picker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            picker.fireEvent(keyEvent(KeyCode.RIGHT));
            assertEquals(LocalTime.of(10, 45), picker.getValue());
        });
    }

    /// Verifies accessibility adjustment, indexed children, selection, and focus routing.
    @Test
    void accessibilityRoutesThroughVisibleDialLabels() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setMinTime(LocalTime.of(9, 0));
            picker.setMaxTime(LocalTime.of(12, 45));
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 420.0, 540.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                picker.resizeRelocate(24.0, 24.0, 328.0, 472.0);
                root.layout();
                picker.layout();

                ButtonBase label = buttonByText(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS, "11");
                label.requestFocus();
                assertTrue(label.isFocused());
                assertSame(label, picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(14, picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

                picker.executeAccessibleAction(
                        AccessibleAction.SET_SELECTED_ITEMS,
                        List.of(LocalTime.of(12, 45))
                );
                assertEquals(LocalTime.of(12, 45), picker.getValue());
                assertEquals(
                        List.of(LocalTime.of(12, 45)),
                        picker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS)
                );

                picker.executeAccessibleAction(
                        AccessibleAction.SET_SELECTED_ITEMS,
                        LocalTime.of(18, 0)
                );
                assertEquals(LocalTime.of(12, 45), picker.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies portrait, landscape, Input, and RTL geometry against the official component measurements.
    @Test
    void adaptiveLayoutUsesOfficialMeasurementsAndMirrorsInRtl() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(7, 0));
            Pane root = layoutPicker(picker, 328.0, 472.0);
            Node dial = requireNode(picker, M3TimePicker.DIAL_STYLE_CLASS);
            Node hour = requireNode(picker, M3TimePicker.HOUR_DISPLAY_STYLE_CLASS);
            Node minute = requireNode(picker, M3TimePicker.MINUTE_DISPLAY_STYLE_CLASS);
            Node period = requireNode(picker, M3TimePicker.PERIOD_ROW_STYLE_CLASS);

            assertEquals(256.0, dial.getBoundsInParent().getWidth(), 0.5);
            assertEquals(256.0, dial.getBoundsInParent().getHeight(), 0.5);
            assertEquals(96.0, hour.getBoundsInParent().getWidth(), 0.5);
            assertEquals(80.0, hour.getBoundsInParent().getHeight(), 0.5);
            assertEquals(96.0, minute.getBoundsInParent().getWidth(), 0.5);
            assertEquals(52.0, period.getBoundsInParent().getWidth(), 0.5);
            assertEquals(80.0, period.getBoundsInParent().getHeight(), 0.5);
            assertFalse(hour.localToScene(hour.getBoundsInLocal())
                    .intersects(period.localToScene(period.getBoundsInLocal())));

            double defaultMinHeight = picker.minHeight(328.0);
            Circle handle = assertInstanceOf(
                    Circle.class,
                    requireNode(picker, M3TimePicker.DIAL_HANDLE_STYLE_CLASS)
            );
            Circle center = assertInstanceOf(
                    Circle.class,
                    requireNode(picker, M3TimePicker.DIAL_CENTER_STYLE_CLASS)
            );
            picker.setContainerSpacing(12.0);
            picker.setDialHandleSize(40.0);
            picker.setDialCenterSize(6.0);
            root.layout();
            picker.layout();
            assertEquals(defaultMinHeight - 24.0, picker.minHeight(328.0), 0.5);
            assertEquals(20.0, handle.getRadius(), 0.0001);
            assertEquals(3.0, center.getRadius(), 0.0001);

            picker.setContainerSpacing(24.0);
            picker.setDialHandleSize(48.0);
            picker.setDialCenterSize(8.0);
            picker.setPrefSize(608.0, 304.0);
            root.layout();
            picker.layout();
            assertTrue(picker.getPseudoClassStates().contains(LANDSCAPE_PSEUDO_CLASS));
            Bounds ltrDial = dial.localToScene(dial.getBoundsInLocal());
            Bounds ltrDisplay = hour.localToScene(hour.getBoundsInLocal());
            assertTrue(ltrDial.getMinX() > ltrDisplay.getMaxX());

            picker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.layout();
            picker.layout();
            Bounds rtlDial = dial.localToScene(dial.getBoundsInLocal());
            Bounds rtlDisplay = hour.localToScene(hour.getBoundsInLocal());
            assertTrue(rtlDial.getMaxX() < rtlDisplay.getMinX());

            picker.setInputMode(true);
            picker.setPrefSize(328.0, 208.0);
            root.layout();
            picker.layout();
            assertEquals(96.0, inputByAccessibleText(picker, "Hour").getWidth(), 0.5);
            assertEquals(72.0, inputByAccessibleText(picker, "Hour").getHeight(), 0.5);
        });
    }

    /// Verifies that replacing the skin detaches old value listeners and dial actions.
    @Test
    void replacingSkinDetachesRetiredNodes() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            layoutPicker(picker, 328.0, 472.0);
            ButtonBase retiredSelected = buttonByText(
                    picker,
                    M3TimePicker.DIAL_LABEL_STYLE_CLASS,
                    "10"
            );
            ButtonBase retiredTarget = buttonByText(
                    picker,
                    M3TimePicker.DIAL_LABEL_STYLE_CLASS,
                    "11"
            );
            ButtonBase retiredPeriod = buttonByText(
                    picker,
                    M3TimePicker.PERIOD_CELL_STYLE_CLASS,
                    "PM"
            );
            assertTrue(retiredSelected.getPseudoClassStates().contains(SELECTED_PSEUDO_CLASS));

            picker.setSkin(new M3TimePickerSkin(picker));
            picker.applyCss();
            picker.layout();
            picker.setValue(LocalTime.of(11, 15));

            assertTrue(retiredSelected.getPseudoClassStates().contains(SELECTED_PSEUDO_CLASS));
            assertFalse(retiredTarget.getPseudoClassStates().contains(SELECTED_PSEUDO_CLASS));

            picker.setValue(LocalTime.of(9, 15));
            retiredTarget.fire();
            retiredPeriod.fire();
            assertEquals(LocalTime.of(9, 15), picker.getValue());
        });
    }

    /// Verifies that selector movement changes rendered handle geometry when animation is disabled.
    @Test
    void selectorGeometrySettlesImmediatelyWithReducedMotion() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(12, 0));
            Pane root = layoutPicker(picker, 328.0, 472.0);
            Circle handle = assertInstanceOf(
                    Circle.class,
                    requireNode(picker, M3TimePicker.DIAL_HANDLE_STYLE_CLASS)
            );
            double startX = handle.getCenterX();
            double startY = handle.getCenterY();

            picker.setValue(LocalTime.of(3, 0));
            root.layout();
            picker.layout();

            assertTrue(Math.abs(handle.getCenterX() - startX) > 80.0);
            assertTrue(Math.abs(handle.getCenterY() - startY) > 80.0);
        });
    }

    /// Verifies selected, 24-hour, Input, and bounded states in a rendered snapshot.
    @Test
    void timePickerSnapshotRendersDialAndInputVariants() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker dialPicker = new M3TimePicker(LocalTime.of(20, 0));
            dialPicker.setUse24HourClock(true);
            dialPicker.setMinTime(LocalTime.of(9, 0));
            dialPicker.setMaxTime(LocalTime.of(21, 30));

            M3TimePicker inputPicker = new M3TimePicker(LocalTime.of(7, 45));
            inputPicker.setInputMode(true);

            HBox row = new HBox(24.0, dialPicker, inputPicker);
            row.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(row, 760.0, 540.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(760.0, 540.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotHasColorVariety(image, 10);
            assertSnapshotNodeContainsContrast(image, dialPicker, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, inputPicker, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(
                    image,
                    requireNode(dialPicker, M3TimePicker.DIAL_HANDLE_STYLE_CLASS),
                    Color.WHITE,
                    0.10
            );
            writeVisualSnapshot(
                    image,
                    java.nio.file.Path.of(
                            "build",
                            "reports",
                            "m3fx-visual",
                            "visual-time-picker.png"
                    )
            );
        });
    }

    /// Verifies that selector buttons retain Material state-layer feedback.
    @Test
    void selectorButtonShowsArmedStateLayer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            Pane root = layoutPicker(picker, 328.0, 472.0);
            ButtonBase selector = buttonByStyle(picker, M3TimePicker.HOUR_DISPLAY_STYLE_CLASS);
            WritableImage normal = snapshotImageOnFxThread(root);

            double centerX = selector.getWidth() / 2.0;
            double centerY = selector.getHeight() / 2.0;
            selector.fireEvent(primaryMouseEvent(
                    selector,
                    MouseEvent.MOUSE_PRESSED,
                    centerX,
                    centerY,
                    true
            ));
            root.layout();

            Node stateLayer = selector.lookup(".m3-state-layer");
            assertNotNull(stateLayer);
            assertTrue(stateLayer.getOpacity() >= 0.09);
            WritableImage armed = snapshotImageOnFxThread(root);
            assertTrue(stateLayer.getOpacity() >= 0.09);
            assertSnapshotAreaChanged(normal, armed, selector, 16);
            selector.fireEvent(primaryMouseEvent(
                    selector,
                    MouseEvent.MOUSE_RELEASED,
                    centerX,
                    centerY,
                    false
            ));
        });
    }

    /// Creates, themes, sizes, and lays out one picker in a retained root.
    private static Pane layoutPicker(M3TimePicker picker, double width, double height) {
        picker.setPrefSize(width, height);
        Pane root = new Pane(picker);
        root.setStyle("-fx-background-color: white; " + visualTestColors());
        Scene scene = new Scene(root, Math.max(width + 48.0, 420.0), Math.max(height + 48.0, 300.0));
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        picker.resizeRelocate(24.0, 24.0, width, height);
        root.layout();
        picker.layout();
        root.applyCss();
        root.layout();
        picker.layout();
        return root;
    }

    /// Verifies official dial diameter, handle diameter, center diameter, and track width.
    private static void assertDialMetrics(M3TimePicker picker) {
        Node dial = requireNode(picker, M3TimePicker.DIAL_STYLE_CLASS);
        Circle handle = assertInstanceOf(
                Circle.class,
                requireNode(picker, M3TimePicker.DIAL_HANDLE_STYLE_CLASS)
        );
        Circle center = assertInstanceOf(
                Circle.class,
                requireNode(picker, M3TimePicker.DIAL_CENTER_STYLE_CLASS)
        );
        Line track = assertInstanceOf(
                Line.class,
                requireNode(picker, M3TimePicker.DIAL_TRACK_STYLE_CLASS)
        );

        assertEquals(256.0, dial.getBoundsInParent().getWidth(), 0.5);
        assertEquals(256.0, dial.getBoundsInParent().getHeight(), 0.5);
        assertEquals(48.0, handle.getRadius() * 2.0, 0.5);
        assertEquals(8.0, center.getRadius() * 2.0, 0.5);
        assertEquals(2.0, track.getStrokeWidth(), 0.1);
    }

    /// Returns visible managed dial-label nodes.
    private static List<Node> visibleDialLabels(M3TimePicker picker) {
        return visibleNodes(picker, M3TimePicker.DIAL_LABEL_STYLE_CLASS);
    }

    /// Returns visible managed nodes with one style class.
    private static List<Node> visibleNodes(Node root, String styleClass) {
        return root.lookupAll("." + styleClass).stream()
                .filter(M3TimePickerTest::isEffectivelyVisible)
                .toList();
    }

    /// Returns whether a node and all ancestors participate in visible layout.
    private static boolean isEffectivelyVisible(Node node) {
        @org.jetbrains.annotations.Nullable Node current = node;
        while (current != null) {
            if (!current.isVisible() || !current.isManaged()) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    /// Returns one button with a style class and visible text.
    private static ButtonBase buttonByText(Node root, String styleClass, String text) {
        for (Node node : visibleNodes(root, styleClass)) {
            if (node instanceof ButtonBase button && text.equals(button.getText())) {
                return button;
            }
        }
        throw new AssertionError("No visible button found for " + styleClass + " with text " + text);
    }

    /// Returns the first button carrying one style class.
    private static ButtonBase buttonByStyle(Node root, String styleClass) {
        Node node = requireNode(root, styleClass);
        return assertInstanceOf(ButtonBase.class, node);
    }

    /// Returns one numeric input by accessible text.
    private static TextField inputByAccessibleText(Node root, String accessibleText) {
        for (Node node : visibleNodes(root, M3TimePicker.INPUT_FIELD_STYLE_CLASS)) {
            if (node instanceof TextField field && accessibleText.equals(field.getAccessibleText())) {
                return field;
            }
        }
        throw new AssertionError("No time input found for " + accessibleText);
    }

    /// Returns one required node carrying a style class.
    private static Node requireNode(Node root, String styleClass) {
        Node node = root.lookup("." + styleClass);
        if (node == null) {
            throw new AssertionError("Missing node with style class " + styleClass);
        }
        return node;
    }

    /// Creates an identity-based set from a node set.
    private static Set<Node> identitySet(Set<Node> nodes) {
        Set<Node> identities = Collections.newSetFromMap(new IdentityHashMap<>());
        identities.addAll(nodes);
        return identities;
    }

    /// Returns a direct dial label's distance from the dial center.
    private static double distanceFromDialCenter(Node label, double centerX, double centerY) {
        double labelCenterX = label.getLayoutX() + label.getBoundsInParent().getWidth() / 2.0;
        double labelCenterY = label.getLayoutY() + label.getBoundsInParent().getHeight() / 2.0;
        return Math.hypot(labelCenterX - centerX, labelCenterY - centerY);
    }

    /// Creates one primary pointer event at a node-local coordinate.
    private static MouseEvent primaryMouseEvent(
            Node node,
            javafx.event.EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                scenePoint.getX(),
                scenePoint.getY(),
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                true,
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }

    /// Creates a modified key event for shortcut-preservation tests.
    private static KeyEvent modifiedKeyEvent(
            KeyCode code,
            boolean shiftDown,
            boolean controlDown
    ) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shiftDown, controlDown, false, false);
    }

    /// Creates an unmodified key-pressed event.
    private static KeyEvent keyEvent(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
}
