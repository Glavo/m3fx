// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies popup width recomputation without taking ownership of public minimum-width properties.
@NotNullByDefault
final class M3PopupWidthTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Recomputes the popup minimum after a previously wider anchor shrinks.
    @ParameterizedTest
    @ValueSource(strings = {"menu", "submenu", "setting", "date", "time", "range"})
    void popupMinimumShrinksWithAnchor(String kind) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            PopupCase fixture = createCase(kind);
            Stage stage = showOwner(fixture.owner());
            try {
                resizeOwner(fixture.owner(), 900.0);
                FxTestUtils.assertNoCssWarnings(fixture.show());
                Region content = fixture.content().get();
                assertTrue(content.getScene().getWindow().isShowing());
                double wideMinimum = content.minWidth(-1);
                assertTrue(wideMinimum >= 900.0);
                FxTestUtils.assertNoCssWarnings(fixture.hide());
                if (content instanceof M3Menu) {
                    assertEquals(112.0, content.minWidth(-1));
                }
                resizeOwner(fixture.owner(), 300.0);
                FxTestUtils.assertNoCssWarnings(fixture.show());
                assertTrue(content.getScene().getWindow().isShowing());
                assertTrue(content.minWidth(-1) < wideMinimum,
                        "Reopening must not retain the previous anchor width");
            } finally {
                FxTestUtils.assertNoCssWarnings(fixture.hide());
                stage.close();
            }
        });
    }

    /// Preserves an explicit minimum equal to the previous anchor-driven minimum.
    @ParameterizedTest
    @ValueSource(strings = {"menu", "submenu", "setting", "date", "time", "range"})
    void popupMinimumPreservesExplicitAssignment(String kind) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            PopupCase fixture = createCase(kind);
            Stage stage = showOwner(fixture.owner());
            try {
                resizeOwner(fixture.owner(), 900.0);
                FxTestUtils.assertNoCssWarnings(fixture.show());
                Region content = fixture.content().get();
                double minimum = content.minWidth(-1);
                content.setMinWidth(minimum);
                FxTestUtils.assertNoCssWarnings(fixture.hide());
                resizeOwner(fixture.owner(), 1100.0);
                FxTestUtils.assertNoCssWarnings(fixture.show());
                assertEquals(minimum, content.minWidth(-1));
            } finally {
                FxTestUtils.assertNoCssWarnings(fixture.hide());
                stage.close();
            }
        });
    }

    /// Reuses generated choices when reopening without changing the source list or converters.
    @Test
    void reopeningSelectSettingRetainsMenuItems() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SelectSettingItem<String> owner = new M3SelectSettingItem<>("Choice");
            owner.getItems().add("One");
            Stage stage = showOwner(owner);
            try {
                resizeOwner(owner, 300.0);
                FxTestUtils.assertNoCssWarnings(owner::showMenu);
                M3MenuItem item = assertInstanceOf(M3MenuItem.class, owner.getMenuItems().get(0));
                owner.hideMenu();
                FxTestUtils.assertNoCssWarnings(owner::showMenu);
                assertSame(item, owner.getMenuItems().get(0));
            } finally {
                owner.hideMenu();
                stage.close();
            }
        });
    }

    /// Shows an owner whose dimensions are controlled directly by the test.
    private static Stage showOwner(Control owner) {
        owner.setManaged(false);
        Pane root = new Pane(owner);
        Stage stage = new Stage();
        Scene scene = new Scene(root, 1200.0, 200.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        stage.setScene(scene);
        FxTestUtils.assertNoCssWarnings(stage::show);
        FxTestUtils.assertNoCssWarnings(root::applyCss);
        return stage;
    }

    /// Applies the requested width and lays out nested field controls before showing a popup.
    private static void resizeOwner(Control owner, double width) {
        owner.resizeRelocate(20.0, 20.0, width, 56.0);
        owner.layout();
    }

    /// Creates an owned menu or picker and its public show/hide operations.
    private static PopupCase createCase(String kind) {
        return switch (kind) {
            case "menu" -> {
                M3MenuButton owner = new M3MenuButton("Actions", new M3MenuItem("One"));
                yield new PopupCase(owner, owner::getMenu, owner::showMenu, owner::hideMenu);
            }
            case "submenu" -> {
                M3SubMenuItem owner = new M3SubMenuItem("Actions", new M3MenuItem("One"));
                yield new PopupCase(owner, owner::getSubMenu, owner::showSubMenu, owner::hideSubMenu);
            }
            case "setting" -> {
                M3SelectSettingItem<String> owner = new M3SelectSettingItem<>("Choice");
                owner.getItems().add("One");
                yield new PopupCase(owner, owner::getMenu, owner::showMenu, owner::hideMenu);
            }
            case "date" -> {
                M3DatePickerField owner = new M3DatePickerField();
                yield new PopupCase(owner, () -> assertInstanceOf(Region.class, owner.getPicker().getParent()), owner::showPicker, owner::hidePicker);
            }
            case "time" -> {
                M3TimePickerField owner = new M3TimePickerField();
                yield new PopupCase(owner, () -> assertInstanceOf(Region.class, owner.getPicker().getParent()), owner::showPicker, owner::hidePicker);
            }
            case "range" -> {
                M3DateRangePickerField owner = new M3DateRangePickerField();
                yield new PopupCase(owner, () -> assertInstanceOf(Region.class, owner.getPicker().getParent()), owner::showPicker, owner::hidePicker);
            }
            default -> throw new IllegalArgumentException("Unknown popup kind: " + kind);
        };
    }

    /// Supplies an anchor and access to its popup content and lifecycle.
    ///
    /// @param owner the control anchoring the popup
    /// @param content the popup region, available after showing
    /// @param show the show operation
    /// @param hide the hide operation
    @NotNullByDefault
    private record PopupCase(Control owner, Supplier<Region> content, Runnable show, Runnable hide) {
    }
}
