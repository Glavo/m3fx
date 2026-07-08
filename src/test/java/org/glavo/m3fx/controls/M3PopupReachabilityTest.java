// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies popup lifecycle behavior when owners leave the reachable scene graph.
@NotNullByDefault
final class M3PopupReachabilityTest {
    /// Starts JavaFX before popup reachability tests create windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes windows left by popup reachability tests.
    @AfterEach
    void closeWindows() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                window.hide();
            }
        });
    }

    /// Verifies that popup owners close detached surfaces when an ancestor becomes unreachable.
    @Test
    void popupOwnersHideDetachedSurfacesWhenOwnerBecomesUnreachable() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Archive"));
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Inbox"));
            M3MenuButton nestedMenuButton = new M3MenuButton("Nested submenu", subMenuItem);
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 25)
            );
            menuButton.setPrefWidth(160.0);
            nestedMenuButton.setPrefWidth(220.0);
            dateField.setPrefWidth(320.0);
            timeField.setPrefWidth(320.0);
            rangeField.setPrefWidth(420.0);

            StackPane menuOwner = new StackPane(menuButton);
            StackPane nestedMenuOwner = new StackPane(nestedMenuButton);
            StackPane dateOwner = new StackPane(dateField);
            StackPane timeOwner = new StackPane(timeField);
            StackPane rangeOwner = new StackPane(rangeField);
            VBox root = new VBox(16.0, menuOwner, nestedMenuOwner, dateOwner, timeOwner, rangeOwner);
            root.setPadding(new Insets(24.0));
            Stage stage = new Stage();

            try {
                Scene scene = new Scene(root, 820.0, 620.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                M3MotionSettings.setAnimationsEnabled(root, false);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertPopupClosesWhenOwnerAncestorBecomesUnreachable(
                        root,
                        menuOwner,
                        menuButton,
                        menuButton::showMenu,
                        menuButton::isShowing,
                        menuButton::hideMenu,
                        "menu popup"
                );
                assertPopupClosesWhenOwnerAncestorBecomesUnreachable(
                        root,
                        nestedMenuButton.getMenu(),
                        subMenuItem,
                        () -> {
                            nestedMenuButton.showMenu();
                            subMenuItem.showSubMenu();
                        },
                        subMenuItem::isSubMenuShowing,
                        () -> {
                            subMenuItem.hideSubMenu();
                            nestedMenuButton.hideMenu();
                        },
                        "submenu popup"
                );
                assertPopupClosesWhenOwnerAncestorBecomesUnreachable(
                        root,
                        dateOwner,
                        dateField,
                        dateField::showPicker,
                        dateField::isShowing,
                        dateField::hidePicker,
                        "date picker popup"
                );
                assertPopupClosesWhenOwnerAncestorBecomesUnreachable(
                        root,
                        timeOwner,
                        timeField,
                        timeField::showPicker,
                        timeField::isShowing,
                        timeField::hidePicker,
                        "time picker popup"
                );
                assertPopupClosesWhenOwnerAncestorBecomesUnreachable(
                        root,
                        rangeOwner,
                        rangeField,
                        rangeField::showPicker,
                        rangeField::isShowing,
                        rangeField::hidePicker,
                        "date range picker popup"
                );
            } finally {
                subMenuItem.hideSubMenu();
                nestedMenuButton.hideMenu();
                menuButton.hideMenu();
                dateField.hidePicker();
                timeField.hidePicker();
                rangeField.hidePicker();
                M3MotionSettings.clearAnimationsEnabled(root);
                stage.close();
            }
        });
    }

    /// Asserts that one popup closes when its owner or ancestor becomes unreachable.
    private static void assertPopupClosesWhenOwnerAncestorBecomesUnreachable(
            Pane root,
            Region ownerContainer,
            Node owner,
            Runnable showPopup,
            BooleanSupplier popupShowing,
            Runnable hidePopup,
            String popupName
    ) {
        try {
            assertPopupHidesAfterReachabilityChange(
                    root,
                    owner,
                    showPopup,
                    popupShowing,
                    () -> ownerContainer.setVisible(false),
                    popupName,
                    "owner ancestor becomes invisible"
            );
            ownerContainer.setVisible(true);
            root.applyCss();
            root.layout();

            assertPopupHidesAfterReachabilityChange(
                    root,
                    owner,
                    showPopup,
                    popupShowing,
                    () -> ownerContainer.setDisable(true),
                    popupName,
                    "owner ancestor becomes disabled"
            );
            ownerContainer.setDisable(false);
            root.applyCss();
            root.layout();

            if (ownerContainer.getParent() == root) {
                int ownerIndex = root.getChildren().indexOf(ownerContainer);
                assertPopupHidesAfterReachabilityChange(
                        root,
                        owner,
                        showPopup,
                        popupShowing,
                        () -> root.getChildren().remove(ownerContainer),
                        popupName,
                        "owner ancestor is removed from the scene"
                );
                root.getChildren().add(ownerIndex, ownerContainer);
                root.applyCss();
                root.layout();
            }

            assertPopupHidesAfterReachabilityChange(
                    root,
                    owner,
                    showPopup,
                    popupShowing,
                    () -> owner.setVisible(false),
                    popupName,
                    "owner becomes invisible"
            );
            owner.setVisible(true);
            root.applyCss();
            root.layout();

            assertPopupHidesAfterReachabilityChange(
                    root,
                    owner,
                    showPopup,
                    popupShowing,
                    () -> owner.setDisable(true),
                    popupName,
                    "owner becomes disabled"
            );
        } finally {
            ownerContainer.setVisible(true);
            ownerContainer.setDisable(false);
            owner.setVisible(true);
            owner.setDisable(false);
            hidePopup.run();
            root.applyCss();
            root.layout();
        }
    }

    /// Shows one popup and verifies that a reachability change closes it.
    private static void assertPopupHidesAfterReachabilityChange(
            Pane root,
            Node owner,
            Runnable showPopup,
            BooleanSupplier popupShowing,
            Runnable reachabilityChange,
            String popupName,
            String reason
    ) {
        showPopup.run();
        root.applyCss();
        root.layout();
        assertTrue(popupShowing.getAsBoolean(), popupName + " should show before " + reason);

        reachabilityChange.run();
        root.applyCss();
        root.layout();
        assertFalse(popupShowing.getAsBoolean(), popupName + " should hide when " + reason);
        assertEquals(false, owner.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
    }
}