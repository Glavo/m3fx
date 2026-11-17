// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3BottomSheetSkin;
import org.glavo.m3fx.skins.M3SideSheetSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies sheet content, visibility, scrims, and adaptive layout.
@NotNullByDefault
final class M3SheetContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that sheet controls own content, actions, and variants.
    @Test
    void sheetControlsOwnContentActionsAndVariants() {
        Label sideContent = new Label("Side content");
        M3IconButton closeAction = new M3IconButton();
        M3SideSheet sideSheet = sideSheet("Details", sideContent, closeAction);
        sideSheet.setVariant(M3SheetVariant.MODAL);

        Label bottomContent = new Label("Bottom content");
        M3IconButton bottomAction = new M3IconButton();
        M3BottomSheet bottomSheet = bottomSheet("Queue", bottomContent, bottomAction);
        bottomSheet.setDragHandleVisible(false);
        AtomicInteger dragHandleActions = new AtomicInteger();
        bottomSheet.setOnDragHandleAction(event -> dragHandleActions.incrementAndGet());

        assertEquals("Details", sideSheet.getHeadline());
        assertEquals(sideContent, sideSheet.getContent());
        assertEquals(closeAction, sideSheet.getActions().get(0));
        assertEquals(M3SheetVariant.MODAL, sideSheet.getVariant());
        assertTrue(sideSheet.getStyleClass().contains(M3SheetVariant.MODAL.styleClass()));
        assertFalse(sideSheet.isDetached());

        sideSheet.setDetached(true);

        assertTrue(sideSheet.isDetached());
        assertTrue(sideSheet.getPseudoClassStates().contains(PseudoClass.getPseudoClass("detached")));
        assertEquals("Queue", bottomSheet.getHeadline());
        assertEquals(bottomContent, bottomSheet.getContent());
        assertEquals(bottomAction, bottomSheet.getActions().get(0));
        assertFalse(bottomSheet.isDragHandleVisible());
        assertNotNull(bottomSheet.getOnDragHandleAction());
        bottomSheet.fireDragHandleAction();
        assertEquals(0, dragHandleActions.get(), "a hidden drag handle must not fire its action");
        bottomSheet.setDragHandleVisible(true);
        bottomSheet.fireDragHandleAction();
        assertEquals(1, dragHandleActions.get(), "a visible drag handle should fire its action");
    }

    /// Verifies that modal sheets dismiss from the Escape key.
    @Test
    void modalSheetsHideFromEscapeKey() {
        M3SideSheet sideSheet = sideSheet("Details", new Label("Side"));
        M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom"));
        M3SideSheet standardSideSheet = sideSheet("Pinned", new Label("Side"));
        M3BottomSheet standardBottomSheet = bottomSheet("Pinned", new Label("Bottom"));
        sideSheet.setVariant(M3SheetVariant.MODAL);
        bottomSheet.setVariant(M3SheetVariant.MODAL);

        sideSheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
        bottomSheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
        standardSideSheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
        standardBottomSheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertFalse(sideSheet.isShown());
        assertFalse(bottomSheet.isShown());
        assertTrue(standardSideSheet.isShown());
        assertTrue(standardBottomSheet.isShown());
    }

    /// Verifies that modal sheets restore focus to the trigger after keyboard dismissal.
    @Test
    void modalSheetsRestoreFocusAfterEscapeKey() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button trigger = new M3Button("Open");
            M3Button sideAction = new M3Button("Side action");
            M3Button bottomAction = new M3Button("Bottom action");
            M3SideSheet sideSheet = sideSheet("Details", new Label("Side"), sideAction);
            M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom"), bottomAction);
            Stage stage = new Stage();
            try {
                sideSheet.setVariant(M3SheetVariant.MODAL);
                bottomSheet.setVariant(M3SheetVariant.MODAL);
                sideSheet.hide();
                bottomSheet.hide();
                stage.setScene(new Scene(new VBox(trigger, sideSheet, bottomSheet), 480.0, 360.0));
                stage.show();

                trigger.requestFocus();
                sideSheet.show();
                sideAction.requestFocus();
                KeyEvent sideEscape = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE);
                sideAction.fireEvent(sideEscape);

                assertFalse(sideSheet.isShown());
                assertTrue(trigger.isFocused());

                trigger.requestFocus();
                bottomSheet.show();
                bottomAction.requestFocus();
                KeyEvent bottomEscape = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE);
                bottomAction.fireEvent(bottomEscape);

                assertFalse(bottomSheet.isShown());
                assertTrue(trigger.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that sheet action rows traverse horizontally without stealing content key events.
    @Test
    void sheetActionKeyboardTraversalFollowsDirectionWithoutStealingContentKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button outside = new M3Button("Outside");
            TextArea sideContent = new TextArea("Side content");
            M3Button sideHidden = new M3Button("Side hidden");
            M3Button sideDisabled = new M3Button("Side disabled");
            M3Button sideFirst = new M3Button("Side first");
            M3Button sideSecond = new M3Button("Side second");
            M3Button sideThird = new M3Button("Side third");
            M3SideSheet sideSheet = sideSheet(
                    "Details",
                    sideContent,
                    sideHidden,
                    sideDisabled,
                    sideFirst,
                    sideSecond,
                    sideThird
            );
            TextArea bottomContent = new TextArea("Bottom content");
            M3Button bottomHidden = new M3Button("Bottom hidden");
            M3Button bottomDisabled = new M3Button("Bottom disabled");
            M3Button bottomFirst = new M3Button("Bottom first");
            M3Button bottomSecond = new M3Button("Bottom second");
            M3Button bottomThird = new M3Button("Bottom third");
            M3BottomSheet bottomSheet = bottomSheet(
                    "Queue",
                    bottomContent,
                    bottomHidden,
                    bottomDisabled,
                    bottomFirst,
                    bottomSecond,
                    bottomThird
            );
            VBox root = new VBox(8.0, outside, sideSheet, bottomSheet);
            Stage stage = new Stage();

            sideHidden.setVisible(false);
            bottomHidden.setVisible(false);
            sideDisabled.setDisable(true);
            bottomDisabled.setDisable(true);
            sideContent.setPrefRowCount(1);
            bottomContent.setPrefRowCount(1);
            sideSheet.setPrefSize(360.0, 160.0);
            bottomSheet.setPrefSize(480.0, 180.0);
            try {
                stage.setScene(new Scene(root, 720.0, 420.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                assertSheetActionKeyboardTraversal(
                        sideSheet,
                        sideContent,
                        outside,
                        sideFirst,
                        sideSecond,
                        sideThird,
                        sideHidden,
                        sideDisabled
                );
                assertSheetActionKeyboardTraversal(
                        bottomSheet,
                        bottomContent,
                        outside,
                        bottomFirst,
                        bottomSecond,
                        bottomThird,
                        bottomHidden,
                        bottomDisabled
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Asserts horizontal keyboard traversal for one sheet action row.
    private static void assertSheetActionKeyboardTraversal(
            Control sheet,
            Node content,
            Node outside,
            M3Button first,
            M3Button second,
            M3Button third,
            M3Button hidden,
            M3Button disabled
    ) {
        sheet.applyCss();
        sheet.layout();
        content.requestFocus();
        assertTrue(content.isFocused());
        content.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(content.isFocused());

        outside.requestFocus();
        sheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(first.isFocused());

        first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(second.isFocused());
        second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(third.isFocused());
        third.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(first.isFocused());
        second.requestFocus();
        second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertTrue(first.isFocused());

        sheet.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        sheet.applyCss();
        sheet.layout();
        first.requestFocus();

        first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(third.isFocused());
        third.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertTrue(first.isFocused());

        outside.requestFocus();
        sheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
        assertTrue(third.isFocused());
        sheet.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
        assertTrue(first.isFocused());
        assertSame(first, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertFalse(hidden.isFocused());
        assertFalse(disabled.isFocused());
    }

    /// Verifies that accessible modal sheet collapse restores focus and hides sheet focus targets.
    @Test
    void modalSheetsAccessibleCollapseRestoresFocusAndClearsFocusNode() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button trigger = new M3Button("Open");
            M3Button sideAction = new M3Button("Side action");
            M3Button bottomAction = new M3Button("Bottom action");
            M3SideSheet sideSheet = sideSheet("Details", new Label("Side"), sideAction);
            M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom"), bottomAction);
            VBox root = new VBox(trigger, sideSheet, bottomSheet);
            Stage stage = new Stage();
            try {
                sideSheet.setVariant(M3SheetVariant.MODAL);
                bottomSheet.setVariant(M3SheetVariant.MODAL);
                sideSheet.hide();
                bottomSheet.hide();
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.setScene(new Scene(root, 480.0, 360.0));
                stage.show();
                root.applyCss();
                root.layout();

                assertNull(sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertNull(bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                trigger.requestFocus();
                sideSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, sideAction);
                assertTrue(sideSheet.isShown());
                assertTrue(sideAction.isFocused());
                assertSame(sideAction, sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sideSheet.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(sideSheet.isShown());
                assertTrue(trigger.isFocused());
                assertNull(sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                trigger.requestFocus();
                bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomAction);
                assertTrue(bottomSheet.isShown());
                assertTrue(bottomAction.isFocused());
                assertSame(bottomAction, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomSheet.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(bottomSheet.isShown());
                assertTrue(trigger.isFocused());
                assertNull(bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that sheets expose accessible state, content, and visibility actions.
    @Test
    void sheetsExposeAccessibleStateAndActions() {
        Label sideContent = new Label("Side");
        Label bottomContent = new Label("Bottom");
        M3SideSheet sideSheet = sideSheet("Details", sideContent);
        M3BottomSheet bottomSheet = bottomSheet("Queue", bottomContent);

        assertEquals("Details", sideSheet.getAccessibleText());
        assertEquals("Details", sideSheet.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(sideContent, sideSheet.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(true, sideSheet.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(1, sideSheet.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(sideContent, sideSheet.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        sideSheet.setHeadline("Updated");
        Label replacementContent = new Label("Replacement");
        sideSheet.setContent(replacementContent);

        assertEquals("Updated", sideSheet.getAccessibleText());
        assertEquals("Updated", sideSheet.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(replacementContent, sideSheet.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(replacementContent, sideSheet.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        sideSheet.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertFalse(sideSheet.isShown());
        assertEquals(false, sideSheet.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        sideSheet.executeAccessibleAction(AccessibleAction.EXPAND);
        assertTrue(sideSheet.isShown());

        assertEquals("Queue", bottomSheet.getAccessibleText());
        assertEquals("Queue", bottomSheet.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(bottomContent, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(1, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(bottomContent, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        bottomSheet.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertFalse(bottomSheet.isShown());
        bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
        assertTrue(bottomSheet.isShown());
        assertEquals(true, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
    }

    /// Verifies that sheet accessibility show-item actions focus content and trailing actions.
    @Test
    void sheetsFocusIndexedAccessibleItems() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button sideContent = new M3Button("Side content");
            M3Button sideAction = new M3Button("Side action");
            M3SideSheet sideSheet = sideSheet("Details", sideContent, sideAction);

            M3Button bottomContent = new M3Button("Bottom content");
            M3Button bottomAction = new M3Button("Bottom action");
            M3BottomSheet bottomSheet = bottomSheet("Queue", bottomContent, bottomAction);

            Stage stage = new Stage();
            try {
                sideSheet.hide();
                bottomSheet.hide();
                stage.setScene(new Scene(new VBox(sideSheet, bottomSheet), 480.0, 360.0));
                stage.show();

                assertNull(sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertNull(bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sideSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(sideSheet.isShown());
                assertTrue(sideContent.isFocused());

                sideSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, sideAction);
                assertTrue(sideAction.isFocused());

                bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(bottomSheet.isShown());
                assertTrue(bottomContent.isFocused());

                bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomAction);
                assertTrue(bottomAction.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that sheet controls expose animated shown state changes.
    @Tier2Test
    @Test
    void sheetControlsSupportAnimatedShownState() throws InterruptedException {
        M3SideSheet sideSheet = sideSheet("Details", new Label("Side"));
        M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom"));
        Pane root = new Pane(sideSheet, bottomSheet);
        Scene scene = new Scene(root, 720.0, 480.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        sideSheet.resize(360.0, 320.0);
        bottomSheet.resize(360.0, 320.0);
        sideSheet.layout();
        bottomSheet.layout();

        assertTrue(sideSheet.isShown());
        assertTrue(bottomSheet.isShown());

        sideSheet.hide();
        bottomSheet.hide();

        assertFalse(sideSheet.isShown());
        assertFalse(bottomSheet.isShown());
        assertTrue(sideSheet.isVisible());
        assertTrue(bottomSheet.isVisible());

        FxTestUtils.runOnFxThreadWhen(() -> !sideSheet.isVisible()
                        && !sideSheet.isManaged()
                        && sideSheet.getTranslateX() > 0.0
                        && Math.abs(sideSheet.getOpacity()) < 0.0001
                        && !bottomSheet.isVisible()
                        && !bottomSheet.isManaged()
                        && bottomSheet.getTranslateY() > 0.0
                        && Math.abs(bottomSheet.getOpacity()) < 0.0001,
                () -> describeSheetPairState(
                        sideSheet,
                        bottomSheet,
                        "Sheets did not settle hidden after hide()"
                ), () -> {
                }, () -> {
                    assertFalse(sideSheet.isVisible());
                    assertFalse(sideSheet.isManaged());
                    assertTrue(sideSheet.getTranslateX() > 0.0);
                    assertEquals(0.0, sideSheet.getOpacity(), 0.0001);
                    assertFalse(bottomSheet.isVisible());
                    assertFalse(bottomSheet.isManaged());
                    assertTrue(bottomSheet.getTranslateY() > 0.0);
                    assertEquals(0.0, bottomSheet.getOpacity(), 0.0001);
                });

        sideSheet.show();
        bottomSheet.show();

        assertTrue(sideSheet.isShown());
        assertTrue(bottomSheet.isShown());
        assertTrue(sideSheet.isVisible());
        assertTrue(bottomSheet.isVisible());

        FxTestUtils.runOnFxThreadWhen(() -> sideSheet.isManaged()
                        && Math.abs(sideSheet.getTranslateX()) < 0.0001
                        && Math.abs(sideSheet.getOpacity() - 1.0) < 0.0001
                        && bottomSheet.isManaged()
                        && Math.abs(bottomSheet.getTranslateY()) < 0.0001
                        && Math.abs(bottomSheet.getOpacity() - 1.0) < 0.0001,
                () -> describeSheetPairState(
                        sideSheet,
                        bottomSheet,
                        "Sheets did not settle shown after show()"
                ), () -> {
                }, () -> {
                    assertTrue(sideSheet.isManaged());
                    assertEquals(0.0, sideSheet.getTranslateX(), 0.0001);
                    assertEquals(1.0, sideSheet.getOpacity(), 0.0001);
                    assertTrue(bottomSheet.isManaged());
                    assertEquals(0.0, bottomSheet.getTranslateY(), 0.0001);
                    assertEquals(1.0, bottomSheet.getOpacity(), 0.0001);
                });
    }

    /// Verifies that sheet component token metrics apply through the active theme.
    @Test
    void sheetComponentsApplyTokenMetrics() {
        M3SideSheet sideSheet = sideSheet("Details", new Label("Content"));
        M3SideSheet modalSideSheet = sideSheet("Filters", new Label("Content"));
        modalSideSheet.setVariant(M3SheetVariant.MODAL);
        M3SideSheet detachedSideSheet = sideSheet("Detached", new Label("Content"));
        detachedSideSheet.setDetached(true);
        M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Content"));
        AtomicInteger dragHandleActions = new AtomicInteger();
        bottomSheet.setOnDragHandleAction(event -> dragHandleActions.incrementAndGet());
        M3BottomSheet modalBottomSheet = bottomSheet("Actions", new Label("Content"));
        modalBottomSheet.setVariant(M3SheetVariant.MODAL);
        Pane root = new Pane(sideSheet, modalSideSheet, detachedSideSheet, bottomSheet, modalBottomSheet);
        Scene scene = new Scene(root);
        M3Theme baselineTheme = M3Theme.defaultTheme();

        M3ThemeManager.install(scene, baselineTheme);
        root.applyCss();

        assertEquals(256.0, sideSheet.getMinWidth(), 0.0001);
        assertEquals(256.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(400.0, sideSheet.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(640.0, bottomSheet.getMaxWidth(), 0.0001);
        assertInstanceOf(M3SideSheetSkin.class, sideSheet.getSkin());
        assertInstanceOf(M3BottomSheetSkin.class, bottomSheet.getSkin());
        assertRegionFill(
                sideSheet,
                baselineTheme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE)
        );
        assertNull(sideSheet.getEffect(), "standard side sheet elevation");
        assertRegionRadii(sideSheet, 0.0, 0.0, 0.0, 0.0);
        assertRegionFill(
                modalSideSheet,
                baselineTheme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE_CONTAINER_LOW)
        );
        assertDropShadow(modalSideSheet);
        assertRegionRadii(modalSideSheet, 16.0, 0.0, 0.0, 16.0);
        assertRegionRadii(detachedSideSheet, 16.0, 16.0, 16.0, 16.0);
        assertRegionFill(
                bottomSheet,
                baselineTheme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE_CONTAINER_LOW)
        );
        assertDropShadow(bottomSheet);
        assertDropShadow(modalBottomSheet);
        assertRegionRadii(bottomSheet, 28.0, 28.0, 0.0, 0.0);
        assertEquals(
                24.0,
                lookupRegion(sideSheet, "." + "m3-sheet-content").getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                lookupRegion(bottomSheet, "." + "m3-sheet-content").getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                32.0,
                lookupRegion(bottomSheet, "." + "m3-bottom-sheet-drag-handle").getPrefWidth(),
                0.0001
        );
        Region dragHandleContainer = lookupRegion(
                bottomSheet,
                "." + "m3-bottom-sheet-drag-handle-container"
        );
        assertEquals(22.0, dragHandleContainer.getPadding().getTop(), 0.0001);
        assertEquals(22.0, dragHandleContainer.getPadding().getBottom(), 0.0001);
        assertEquals(AccessibleRole.BUTTON, dragHandleContainer.getAccessibleRole());
        assertEquals("Resize bottom sheet", dragHandleContainer.getAccessibleText());
        assertTrue(dragHandleContainer.isFocusTraversable());
        dragHandleContainer.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, dragHandleActions.get());
        dragHandleContainer.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        root.applyCss();
        assertFalse(
                lookupRegion(bottomSheet, "." + "m3-bottom-sheet-drag-handle")
                        .getBorder()
                        .getStrokes()
                        .isEmpty(),
                "keyboard focus should render the drag-handle focus indicator"
        );

        modalSideSheet.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        root.layout();

        assertTrue(modalSideSheet.getPseudoClassStates().contains(PseudoClass.getPseudoClass("rtl")));
        assertRegionRadii(modalSideSheet, 0.0, 16.0, 16.0, 0.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(256.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(400.0, sideSheet.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(640.0, bottomSheet.getMaxWidth(), 0.0001);
        assertEquals(
                24.0,
                lookupRegion(sideSheet, "." + "m3-sheet-content").getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                lookupRegion(bottomSheet, "." + "m3-sheet-content").getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                32.0,
                lookupRegion(bottomSheet, "." + "m3-bottom-sheet-drag-handle").getPrefWidth(),
                0.0001
        );
        assertEquals(
                4.0,
                lookupRegion(bottomSheet, "." + "m3-bottom-sheet-drag-handle").getPrefHeight(),
                0.0001
        );
        assertRegionRadii(detachedSideSheet, 16.0, 16.0, 16.0, 16.0);
        assertRegionRadii(bottomSheet, 28.0, 28.0, 0.0, 0.0);
    }

    /// Verifies that sheets, form sections, and validation summaries mirror logical slots in right-to-left layout.
    @Test
    void sheetFormAndValidationSkinsPropagateRightToLeftOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            Label sideContent = new Label("Side content");
            M3Button sideAction = createButton("Close", M3ButtonVariant.TEXT);
            M3SideSheet sideSheet = sideSheet("Details", sideContent, sideAction);
            M3IconButton sideHeaderAction = createIconButton("C");
            sideSheet.getHeaderActions().add(sideHeaderAction);
            sideSheet.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            sideSheet.setPrefSize(300.0, 160.0);

            Label bottomContent = new Label("Bottom content");
            M3Button bottomAction = createButton("Done", M3ButtonVariant.TEXT);
            M3BottomSheet bottomSheet = bottomSheet("Queue", bottomContent, bottomAction);
            bottomSheet.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            bottomSheet.setPrefSize(360.0, 160.0);

            Label sectionContent = new Label("Section content");
            M3FormSection formSection = formSection("Account", sectionContent);
            formSection.setSupportingText("Preferences and profile settings");
            formSection.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            formSection.setPrefWidth(360.0);

            M3TextField invalidField = new M3TextField();
            M3TextInputLayout invalidLayout = new M3TextInputLayout(invalidField);
            invalidLayout.setLabelText("Display name");
            invalidLayout.setSupportingText("Required");
            invalidLayout.setValidator(M3TextInputValidators.required("Display name is required"));
            M3FormValidator validator = new M3FormValidator(invalidLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            summary.setPrefWidth(360.0);
            assertFalse(validator.validate());

            Pane root = new Pane(sideSheet, bottomSheet, formSection, invalidLayout, summary);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 520.0, 760.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(520.0, 760.0);
            sideSheet.resizeRelocate(20.0, 20.0, 300.0, 160.0);
            bottomSheet.resizeRelocate(20.0, 200.0, 360.0, 160.0);
            formSection.resizeRelocate(20.0, 380.0, 360.0, 110.0);
            invalidLayout.resizeRelocate(20.0, 510.0, 360.0, 88.0);
            summary.resizeRelocate(20.0, 620.0, 360.0, 110.0);
            root.layout();
            sideSheet.layout();
            bottomSheet.layout();
            formSection.layout();
            invalidLayout.layout();
            summary.layout();

            HBox sideHeader = assertInstanceOf(
                    HBox.class,
                    sideSheet.lookup("." + "m3-sheet-header")
            );
            StackPane sideContentSlot = assertInstanceOf(
                    StackPane.class,
                    sideSheet.lookup("." + "m3-sheet-content")
            );
            HBox sideActions = assertInstanceOf(
                    HBox.class,
                    sideSheet.lookup("." + "m3-side-sheet-actions")
            );
            HBox sideHeaderActions = assertInstanceOf(
                    HBox.class,
                    sideSheet.lookup("." + "m3-side-sheet-header-actions")
            );
            HBox bottomHeader = assertInstanceOf(
                    HBox.class,
                    bottomSheet.lookup("." + "m3-sheet-header")
            );
            StackPane bottomContentSlot = assertInstanceOf(
                    StackPane.class,
                    bottomSheet.lookup("." + "m3-sheet-content")
            );
            HBox bottomActions = assertInstanceOf(
                    HBox.class,
                    bottomSheet.lookup("." + "m3-sheet-actions")
            );
            VBox formHeader = assertInstanceOf(
                    VBox.class,
                    formSection.lookup("." + "m3-form-section-header")
            );
            Label sectionTitle = assertInstanceOf(
                    Label.class,
                    formSection.lookup("." + "m3-form-section-title")
            );
            Label sectionSupporting = assertInstanceOf(
                    Label.class,
                    formSection.lookup("." + "m3-form-section-supporting-text")
            );
            VBox formContent = assertInstanceOf(
                    VBox.class,
                    formSection.lookup("." + "m3-form-section-content")
            );
            VBox summaryItems = assertInstanceOf(
                    VBox.class,
                    summary.lookup("." + "m3-validation-summary-items")
            );
            Label summaryTitle = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-title")
            );
            Label summaryItemLabel = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-item-label")
            );
            Label summaryItemError = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-item-error")
            );

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, sideHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, sideContent.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, bottomHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, bottomContent.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, formHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, formContent.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, summaryItems.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, sectionTitle.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, sectionSupporting.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, summaryTitle.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, summaryItemLabel.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, summaryItemError.getEffectiveNodeOrientation());
            assertEquals(Pos.CENTER_LEFT, sideHeader.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, sideHeaderActions.getAlignment());
            assertEquals(Pos.CENTER_LEFT, sideActions.getAlignment());
            assertEquals(Pos.TOP_RIGHT, sideContentSlot.getAlignment());
            assertEquals(sideHeaderAction, sideHeaderActions.getChildren().get(0));
            assertEquals(sideAction, sideActions.getChildren().get(0));
            assertEquals(Pos.CENTER_LEFT, bottomHeader.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, bottomActions.getAlignment());
            assertEquals(Pos.TOP_RIGHT, bottomContentSlot.getAlignment());
            assertEquals(Pos.TOP_LEFT, formHeader.getAlignment());
            assertEquals(Pos.TOP_LEFT, formContent.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, sectionTitle.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, sectionSupporting.getAlignment());
            assertEquals(TextAlignment.RIGHT, sectionTitle.getTextAlignment());
            assertEquals(TextAlignment.RIGHT, sectionSupporting.getTextAlignment());
            assertEquals(Pos.CENTER_LEFT, summaryItems.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, summaryTitle.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, summaryItemLabel.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, summaryItemError.getAlignment());
            assertEquals(TextAlignment.RIGHT, summaryTitle.getTextAlignment());
            assertEquals(TextAlignment.RIGHT, summaryItemLabel.getTextAlignment());
            assertEquals(TextAlignment.RIGHT, summaryItemError.getTextAlignment());

            Bounds formBounds = formSection.localToScene(formSection.getBoundsInLocal());
            Node sectionTitleText = Objects.requireNonNull(sectionTitle.lookup(".text"));
            Bounds sectionTitleBounds = sectionTitleText.localToScene(sectionTitleText.getBoundsInLocal());
            assertTrue(sectionTitleBounds.getCenterX() > formBounds.getCenterX(),
                    () -> "sectionTitleBounds=" + sectionTitleBounds + ", formBounds=" + formBounds);
            Bounds summaryBounds = summary.localToScene(summary.getBoundsInLocal());
            Node summaryTitleText = Objects.requireNonNull(summaryTitle.lookup(".text"));
            Node summaryItemLabelText = Objects.requireNonNull(summaryItemLabel.lookup(".text"));
            Bounds summaryTitleBounds = summaryTitleText.localToScene(summaryTitleText.getBoundsInLocal());
            Bounds summaryItemLabelBounds = summaryItemLabelText.localToScene(summaryItemLabelText.getBoundsInLocal());
            assertTrue(summaryTitleBounds.getCenterX() > summaryBounds.getCenterX(),
                    () -> "summaryTitleBounds=" + summaryTitleBounds + ", summaryBounds=" + summaryBounds);
            assertTrue(summaryItemLabelBounds.getCenterX() > summaryBounds.getCenterX(),
                    () -> "summaryItemLabelBounds=" + summaryItemLabelBounds + ", summaryBounds=" + summaryBounds);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, sideSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, formSection, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, summary, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-sheets-form-validation-rtl.png"
            ));
        });
    }

    /// Describes a side and bottom sheet pair for timeout diagnostics.
    private static String describeSheetPairState(
            M3SideSheet sideSheet,
            M3BottomSheet bottomSheet,
            String description
    ) {
        return description
                + ": sideShown=" + sideSheet.isShown()
                + ", side=" + describeNodeState(sideSheet)
                + ", bottomShown=" + bottomSheet.isShown()
                + ", bottom=" + describeNodeState(bottomSheet);
    }
}
