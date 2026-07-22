// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/// Verifies the desktop pointer cursor contract for Material action targets.
///
/// Material desktop guidance reserves the hand cursor for links. Ordinary actions use the platform arrow even when
/// an ancestor supplies a different inherited cursor. Applications may still override the cursor explicitly when a
/// control represents a link.
///
/// See [Material Design input guidance](https://m3.material.io/foundations/interaction/inputs).
@NotNullByDefault
final class M3PointerCursorTest {
    /// Starts the JavaFX toolkit before creating styled controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies default, disabled, internal, inherited, and explicitly overridden cursor behavior.
    @Test
    void materialActionsUseTheDesktopArrowCursor() {
        FxTestUtils.runOnFxThread(() -> {
            M3Card card = new M3Card(new Label("Card"));
            card.setOnAction(event -> {
            });

            List<Node> materialTargets = List.of(
                    new M3Button("Button"),
                    new M3IconButton(new M3Icon("+")),
                    new M3IconToggleButton("T"),
                    new M3FloatingActionButton(new M3Icon("+")),
                    new M3AssistChip("Assist"),
                    new M3CheckBox("Check"),
                    new M3RadioButton("Radio"),
                    new M3Switch("Switch"),
                    new M3Slider(0.0, 100.0, 50.0),
                    new M3SegmentedButton("Segment"),
                    new M3ListItem("List item"),
                    new M3MenuItem("Menu item"),
                    new M3NavigationItem("Navigation"),
                    new M3Tab("Tab"),
                    card
            );

            M3Carousel carousel = new M3Carousel();
            carousel.getItems().setAll(
                    new M3Card(new Label("First")),
                    new M3Card(new Label("Second"))
            );
            carousel.setPrefSize(480.0, 220.0);

            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 7, 22));
            datePicker.setPrefSize(360.0, 520.0);
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));
            timePicker.setPrefSize(500.0, 600.0);

            M3TextField invalidField = new M3TextField();
            M3TextInputLayout invalidLayout = new M3TextInputLayout(invalidField);
            invalidLayout.setLabelText("Name");
            invalidLayout.setValidator(M3TextInputValidators.required("Required"));
            M3FormValidator validator = new M3FormValidator(invalidLayout);
            M3ValidationSummary validationSummary = new M3ValidationSummary(validator);
            assertFalse(validator.validate());

            M3Button linkedAction = new M3Button("Linked action");
            linkedAction.setCursor(Cursor.HAND);
            Label inheritedCursor = new Label("Inherited cursor");

            FlowPane root = new FlowPane(12.0, 12.0);
            root.setStyle("-fx-cursor: hand;");
            root.getChildren().addAll(materialTargets);
            root.getChildren().addAll(
                    carousel,
                    datePicker,
                    timePicker,
                    invalidLayout,
                    validationSummary,
                    linkedAction,
                    inheritedCursor
            );

            Scene scene = new Scene(root, 1600.0, 1400.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1600.0, 1400.0);
            root.layout();

            materialTargets.forEach(M3PointerCursorTest::assertDefaultCursor);
            assertDefaultCursor(root, "m3-carousel-item-container");
            assertDefaultCursor(root, M3DatePicker.DAY_CELL_STYLE_CLASS);
            assertDefaultCursor(root, M3TimePicker.HOUR_DISPLAY_STYLE_CLASS);
            assertDefaultCursor(root, M3TimePicker.MINUTE_DISPLAY_STYLE_CLASS);
            assertDefaultCursor(root, M3TimePicker.DIAL_STYLE_CLASS);
            assertDefaultCursor(root, M3TimePicker.CELL_STYLE_CLASS);
            assertDefaultCursor(root, M3TimePicker.PERIOD_CELL_STYLE_CLASS);
            assertDefaultCursor(root, M3ValidationSummary.ITEM_STYLE_CLASS);
            assertEquals(Cursor.HAND, effectiveCursor(linkedAction));
            assertEquals(Cursor.HAND, effectiveCursor(inheritedCursor));

            materialTargets.forEach(node -> node.setDisable(true));
            root.applyCss();
            materialTargets.forEach(M3PointerCursorTest::assertDefaultCursor);
            assertEquals(Cursor.HAND, effectiveCursor(linkedAction));
        });
    }

    /// Asserts that every rendered node with the supplied style class uses the platform arrow.
    private static void assertDefaultCursor(Parent root, String styleClass) {
        Set<Node> nodes = root.lookupAll("." + styleClass);
        assertFalse(nodes.isEmpty(), () -> "Missing rendered nodes for ." + styleClass);
        nodes.forEach(M3PointerCursorTest::assertDefaultCursor);
    }

    /// Asserts that one node uses the platform arrow.
    private static void assertDefaultCursor(Node node) {
        assertEquals(Cursor.DEFAULT, effectiveCursor(node), () -> "Unexpected cursor for " + node);
    }

    /// Returns the first cursor assigned to the node or one of its ancestors.
    private static Cursor effectiveCursor(Node node) {
        Node current = node;
        while (current != null) {
            Cursor cursor = current.getCursor();
            if (cursor != null) {
                return cursor;
            }
            current = current.getParent();
        }
        return Cursor.DEFAULT;
    }
}
