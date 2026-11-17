// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3PresentationActivity;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/// Verifies popup lifecycle behavior when owners leave the reachable scene graph.
@NotNullByDefault
@Tier2Test
final class M3PopupReachabilityTest {
    /// Stages owned by the current test, including hidden or incompletely initialized stages.
    private final List<Stage> stages = new ArrayList<>();

    /// Starts JavaFX before popup reachability tests create windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes stages owned by this test, including stages that are no longer visible.
    @AfterEach
    void closeWindows() {
        FxTestUtils.runOnFxThread(() -> {
            for (Stage stage : stages) {
                stage.close();
            }
            stages.clear();
        });
    }

    /// Verifies that popup owners reject show requests before their stage becomes visible.
    @Test
    void popupOwnersDoNotRetainOwnersFromHiddenWindows() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Archive"));
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Inbox"));
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 25)
            );
            M3Button tooltipOwner = new M3Button("Help");
            M3Tooltip tooltip = new M3Tooltip("Tooltip");
            VBox root = new VBox(
                    8.0,
                    menuButton,
                    subMenuItem,
                    dateField,
                    timeField,
                    rangeField,
                    tooltipOwner
            );
            Stage stage = new Stage();
            stages.add(stage);

            try {
                stage.setScene(new Scene(root, 640.0, 480.0));
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                subMenuItem.showSubMenu();
                dateField.showPicker();
                timeField.showPicker();
                rangeField.showPicker();
                tooltip.show(tooltipOwner, 0.0, 0.0);

                assertFalse(menuButton.isShowing());
                assertFalse(subMenuItem.isSubMenuShowing());
                assertFalse(dateField.isShowing());
                assertFalse(timeField.isShowing());
                assertFalse(rangeField.isShowing());
                assertFalse(tooltip.isShowing());
                assertNull(tooltip.getOwnerNode());
                assertNull(tooltip.getOwnerWindow());
            } finally {
                tooltip.hide();
                rangeField.hidePicker();
                timeField.hidePicker();
                dateField.hidePicker();
                subMenuItem.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Closes a menu when its SubScene becomes hidden and native visibility is available.
    @Test
    void menuClosesWhenSubSceneBecomesHidden() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Archive"));
            StackPane content = new StackPane(menuButton);
            SubScene subScene = new SubScene(content, 300.0, 100.0);
            StackPane root = new StackPane(subScene);
            Scene scene = new Scene(root, 300.0, 100.0);
            subScene.setVisible(false);
            assumeTrue(!M3PresentationActivity.isTreeVisible(menuButton),
                    "JavaFX does not expose visibility across the SubScene boundary");
            subScene.setVisible(true);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            menuButton.setAnimationsEnabled(false);
            Stage stage = new Stage();
            stages.add(stage);
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                content.applyCss();
                content.layout();

                assertPopupHidesAfterReachabilityChange(root, menuButton,
                        menuButton::showMenu, menuButton::isShowing,
                        () -> subScene.setVisible(false), "Menu", "SubScene becomes hidden");
                subScene.setVisible(true);
                assertPopupHidesAfterReachabilityChange(root, menuButton,
                        menuButton::showMenu, menuButton::isShowing,
                        () -> subScene.setDisable(true), "Menu", "SubScene becomes disabled");
            } finally {
                menuButton.hideMenu();
                stage.close();
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
            stages.add(stage);

            try {
                Scene scene = new Scene(root, 820.0, 620.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
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
                FxTestUtils.setCssAnimationsEnabled(root, true);
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
        var warnings = FxTestUtils.captureCssWarnings(() -> {
            showPopup.run();
            root.applyCss();
            root.layout();
            assertTrue(popupShowing.getAsBoolean(), popupName + " should show before " + reason);

            reachabilityChange.run();
            root.applyCss();
            root.layout();
            assertFalse(popupShowing.getAsBoolean(), popupName + " should hide when " + reason);
            assertEquals(false, owner.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        });
        assertTrue(
                warnings.stream().noneMatch(FxTestUtils::isM3CssTokenWarning),
                () -> popupName + " emitted CSS token warnings when " + reason + ": "
                        + warnings.stream().map(record -> record.getMessage()).toList()
        );
    }
}
