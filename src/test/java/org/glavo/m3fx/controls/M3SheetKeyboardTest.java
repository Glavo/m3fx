// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.glavo.m3fx.M3TestControls.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies keyboard focus behavior shared by Material sheet controls.
@NotNullByDefault
final class M3SheetKeyboardTest {
    /// Starts the JavaFX toolkit for sheet keyboard tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real windows created by sheet keyboard tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that Escape from a modal bottom sheet action hides the sheet and restores prior focus.
    @Test
    void bottomSheetEscapeHidesModalSheetAndRestoresFocus() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3Button action = new M3Button("Close");
            M3BottomSheet sheet = bottomSheet("Queue", new Label("Bottom sheet content"));
            sheet.getActions().add(action);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);
            VBox root = new VBox(previousFocus, sheet);

            show(root, 360.0, 240.0);
            previousFocus.requestFocus();
            sheet.show();

            assertTrue(sheet.isShown(), "modal bottom sheet should be shown before Escape");
            assertTrue(previousFocus.isFocused(), "showing the sheet should preserve the previous focus owner until focus is requested");
            action.requestFocus();
            assertTrue(action.isFocused(), "a bottom sheet action should accept focus before Escape");

            action.fireEvent(keyEvent(KeyCode.ESCAPE));

            assertFalse(sheet.isShown(), "Escape should hide a modal bottom sheet");
            assertTrue(previousFocus.isFocused(), "hiding a modal bottom sheet should restore the previous focus owner");
        }));
    }

    /// Verifies that Escape from a modal side sheet action hides the sheet and restores prior focus.
    @Test
    void sideSheetEscapeHidesModalSheetAndRestoresFocus() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3Button action = new M3Button("Close");
            M3SideSheet sheet = sideSheet("Details", new Label("Side sheet content"));
            sheet.getActions().add(action);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);
            VBox root = new VBox(previousFocus, sheet);

            show(root, 360.0, 240.0);
            previousFocus.requestFocus();
            sheet.show();

            assertTrue(sheet.isShown(), "modal side sheet should be shown before Escape");
            assertTrue(previousFocus.isFocused(), "showing the sheet should preserve the previous focus owner until focus is requested");
            action.requestFocus();
            assertTrue(action.isFocused(), "a side sheet action should accept focus before Escape");

            action.fireEvent(keyEvent(KeyCode.ESCAPE));

            assertFalse(sheet.isShown(), "Escape should hide a modal side sheet");
            assertTrue(previousFocus.isFocused(), "hiding a modal side sheet should restore the previous focus owner");
        }));
    }

    /// Verifies that modal bottom sheets keep Tab and F6 traversal inside sheet content and actions.
    @Test
    void bottomSheetModalFocusCyclesInsideSheet() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3TextField editor = new M3TextField("Editable content");
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button outside = new M3Button("Outside");
            M3BottomSheet sheet = bottomSheet("Queue", editor);
            sheet.getActions().addAll(first, second);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);
            VBox root = new VBox(previousFocus, sheet, outside);

            show(root, 460.0, 280.0);
            previousFocus.requestFocus();
            sheet.show();
            root.applyCss();
            root.layout();

            assertModalSheetFocusCycle(previousFocus, editor, first, second, outside);
        }));
    }

    /// Verifies that modal side sheets keep Tab and F6 traversal inside sheet content and actions.
    @Test
    void sideSheetModalFocusCyclesInsideSheet() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3TextField editor = new M3TextField("Editable content");
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button outside = new M3Button("Outside");
            M3SideSheet sheet = sideSheet("Details", editor);
            sheet.getActions().addAll(first, second);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);
            VBox root = new VBox(previousFocus, sheet, outside);

            show(root, 460.0, 280.0);
            previousFocus.requestFocus();
            sheet.show();
            root.applyCss();
            root.layout();

            assertModalSheetFocusCycle(previousFocus, editor, first, second, outside);
        }));
    }

    /// Verifies that the most recently shown modal sheet owns the scene-level focus trap.
    @Test
    void latestShownModalSheetOwnsFocusTrapUntilHidden() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3TextField bottomContent = new M3TextField("Bottom content");
            M3Button bottomAction = new M3Button("Bottom action");
            M3BottomSheet bottomSheet = bottomSheet("Bottom", bottomContent);
            bottomSheet.getActions().add(bottomAction);
            bottomSheet.setVariant(M3SheetVariant.MODAL);
            bottomSheet.setShown(false);

            M3TextField sideContent = new M3TextField("Side content");
            M3Button sideAction = new M3Button("Side action");
            M3SideSheet sideSheet = sideSheet("Side", sideContent);
            sideSheet.getActions().add(sideAction);
            sideSheet.setVariant(M3SheetVariant.MODAL);
            sideSheet.setShown(false);

            VBox root = new VBox(previousFocus, bottomSheet, sideSheet);
            show(root, 520.0, 360.0);
            previousFocus.requestFocus();
            bottomSheet.show();
            bottomAction.requestFocus();
            sideSheet.show();
            root.applyCss();
            root.layout();

            assertTrue(bottomSheet.isShown(), "bottom sheet should remain shown behind the later sheet");
            assertTrue(sideSheet.isShown(), "side sheet should be the later modal surface");
            bottomAction.requestFocus();
            bottomAction.fireEvent(keyEvent(KeyCode.TAB));

            assertTrue(sideContent.isFocused(), "Tab from a background sheet should enter the topmost modal sheet");
            assertFalse(bottomContent.isFocused(), "the background sheet should not own modal traversal while covered");

            sideContent.fireEvent(keyEvent(KeyCode.ESCAPE));
            assertFalse(sideSheet.isShown(), "Escape should hide only the topmost modal sheet");
            assertTrue(bottomSheet.isShown(), "hiding the topmost modal sheet should leave the background sheet shown");
            assertTrue(bottomAction.isFocused(), "hiding the topmost sheet should restore focus to the previous sheet target");

            bottomAction.fireEvent(keyEvent(KeyCode.TAB));
            assertTrue(bottomContent.isFocused(), "the background sheet should resume modal traversal after the top sheet hides");

            bottomContent.fireEvent(keyEvent(KeyCode.ESCAPE));
            assertFalse(bottomSheet.isShown(), "Escape should then hide the remaining modal sheet");
            assertTrue(previousFocus.isFocused(), "hiding the remaining modal sheet should restore the original focus owner");
        }));
    }

    /// Verifies that a modal sheet shown over a modal dialog pane temporarily owns scene-level focus trapping.
    @Test
    void modalBottomSheetTemporarilyOwnsFocusTrapOverModalDialogPane() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField dialogContent = new M3TextField("Dialog content");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);

            M3TextField sheetContent = new M3TextField("Sheet content");
            M3Button sheetAction = new M3Button("Sheet action");
            M3BottomSheet sheet = bottomSheet("Sheet", sheetContent);
            sheet.getActions().add(sheetAction);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);

            VBox root = new VBox(dialogPane, sheet);
            showModal(root, 520.0, 360.0);
            Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
            dialogContent.requestFocus();
            assertTrue(dialogContent.isFocused(), "dialog content should own focus before the sheet opens");

            sheet.show();
            root.applyCss();
            root.layout();

            KeyEvent tabFromDialog = keyEvent(KeyCode.TAB);
            dialogContent.fireEvent(tabFromDialog);

            assertTrue(sheetContent.isFocused(), "Tab should enter the topmost modal sheet content");
            assertFalse(okButton.isFocused(), "background dialog actions should not receive focus while covered");

            sheetContent.fireEvent(keyEvent(KeyCode.ESCAPE));

            assertFalse(sheet.isShown(), "Escape should hide only the topmost modal sheet");
            assertTrue(dialogContent.isFocused(), "hiding the sheet should restore the covered dialog focus owner");

            KeyEvent tabAfterSheet = keyEvent(KeyCode.TAB);
            dialogContent.fireEvent(tabAfterSheet);

            assertTrue(okButton.isFocused(), "Tab should move from dialog content to the dialog action");
        }));
    }

    /// Verifies that a modal dialog pane shown over a modal sheet temporarily owns scene-level focus trapping.
    @Test
    void modalDialogPaneTemporarilyOwnsFocusTrapOverModalBottomSheet() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField sheetContent = new M3TextField("Sheet content");
            M3Button sheetAction = new M3Button("Sheet action");
            M3BottomSheet sheet = bottomSheet("Sheet", sheetContent);
            sheet.getActions().add(sheetAction);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(true);

            M3TextField dialogContent = new M3TextField("Dialog content");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            dialogPane.setVisible(false);

            VBox root = new VBox(sheet, dialogPane);
            showModal(root, 520.0, 360.0);
            sheetAction.requestFocus();
            assertTrue(sheetAction.isFocused(), "sheet action should own focus before the dialog appears");

            dialogPane.setVisible(true);
            root.applyCss();
            root.layout();

            KeyEvent tabFromSheet = keyEvent(KeyCode.TAB);
            sheetAction.fireEvent(tabFromSheet);

            assertTrue(dialogContent.isFocused(), "Tab should enter the topmost modal dialog content");
            assertFalse(sheetContent.isFocused(), "background sheet content should not receive focus while covered");

            dialogPane.setVisible(false);
            root.applyCss();
            root.layout();
            sheetAction.requestFocus();

            KeyEvent tabAfterDialog = keyEvent(KeyCode.TAB);
            sheetAction.fireEvent(tabAfterDialog);

            assertTrue(sheetContent.isFocused(), "Tab should wrap from the sheet action back to sheet content");
        }));
    }

    /// Verifies that a modal side sheet shown over a modal dialog pane temporarily owns scene-level focus trapping.
    @Test
    void modalSideSheetTemporarilyOwnsFocusTrapOverModalDialogPane() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField dialogContent = new M3TextField("Dialog content");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);

            M3TextField sheetContent = new M3TextField("Sheet content");
            M3Button sheetAction = new M3Button("Sheet action");
            M3SideSheet sheet = sideSheet("Sheet", sheetContent);
            sheet.getActions().add(sheetAction);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);

            VBox root = new VBox(dialogPane, sheet);
            showModal(root, 520.0, 360.0);
            Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
            dialogContent.requestFocus();
            assertTrue(dialogContent.isFocused(), "dialog content should own focus before the side sheet opens");

            sheet.show();
            root.applyCss();
            root.layout();

            dialogContent.fireEvent(keyEvent(KeyCode.F6));

            assertTrue(sheetContent.isFocused(), "F6 should enter the topmost modal side sheet content");
            assertFalse(okButton.isFocused(), "background dialog actions should not receive focus while covered");

            sheetContent.fireEvent(keyEvent(KeyCode.ESCAPE));

            assertFalse(sheet.isShown(), "Escape should hide only the topmost modal side sheet");
            assertTrue(dialogContent.isFocused(), "hiding the side sheet should restore the covered dialog focus owner");

            dialogContent.fireEvent(keyEvent(KeyCode.F6));

            assertTrue(okButton.isFocused(), "F6 should move from dialog content to the dialog action after the side sheet hides");
        }));
    }

    /// Verifies that a modal dialog pane shown over a modal side sheet temporarily owns scene-level focus trapping.
    @Test
    void modalDialogPaneTemporarilyOwnsFocusTrapOverModalSideSheet() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField sheetContent = new M3TextField("Sheet content");
            M3Button sheetAction = new M3Button("Sheet action");
            M3SideSheet sheet = sideSheet("Sheet", sheetContent);
            sheet.getActions().add(sheetAction);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(true);

            M3TextField dialogContent = new M3TextField("Dialog content");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            dialogPane.setVisible(false);

            VBox root = new VBox(sheet, dialogPane);
            showModal(root, 520.0, 360.0);
            sheetAction.requestFocus();
            assertTrue(sheetAction.isFocused(), "side sheet action should own focus before the dialog appears");

            dialogPane.setVisible(true);
            root.applyCss();
            root.layout();

            sheetAction.fireEvent(keyEvent(KeyCode.F6));

            assertTrue(dialogContent.isFocused(), "F6 should enter the topmost modal dialog content");
            assertFalse(sheetContent.isFocused(), "background side sheet content should not receive focus while covered");

            dialogPane.setVisible(false);
            root.applyCss();
            root.layout();
            sheetAction.requestFocus();

            sheetAction.fireEvent(keyEvent(KeyCode.F6));

            assertTrue(sheetContent.isFocused(), "F6 should wrap from the side sheet action back to side sheet content");
        }));
    }
    /// Verifies that modified Tab and F6 shortcuts remain available while a modal sheet is shown.
    @Test
    void modalSheetFocusTrapDoesNotConsumeModifiedTraversalShortcuts() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3TextField editor = new M3TextField("Editable content");
            M3Button action = new M3Button("Close");
            M3BottomSheet sheet = bottomSheet("Queue", editor);
            sheet.getActions().add(action);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);
            VBox root = new VBox(previousFocus, sheet);

            show(root, 420.0, 260.0);
            previousFocus.requestFocus();
            sheet.show();
            editor.requestFocus();
            assertTrue(editor.isFocused(), "sheet content should own focus before modified shortcuts");

            KeyEvent controlTab = keyEvent(KeyCode.TAB, false, true, false, false);
            sheet.fireEvent(controlTab);
            assertFalse(controlTab.isConsumed(), "Ctrl+Tab should remain available to application shortcuts");
            assertTrue(editor.isFocused(), "Ctrl+Tab should not be converted into modal focus traversal");

            KeyEvent altF6 = keyEvent(KeyCode.F6, false, false, true, false);
            sheet.fireEvent(altF6);
            assertFalse(altF6.isConsumed(), "Alt+F6 should remain available to platform or application shortcuts");
            assertTrue(editor.isFocused(), "Alt+F6 should not be converted into modal focus traversal");
        }));
    }

    /// Verifies that changing a shown bottom sheet to standard releases modal keyboard trapping.
    @Test
    void bottomSheetStandardVariantDoesNotUseModalFocusTrap() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3TextField editor = new M3TextField("Editable content");
            M3Button action = new M3Button("Close");
            M3BottomSheet sheet = bottomSheet("Queue", editor);
            sheet.getActions().add(action);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);

            VBox root = new VBox(previousFocus, sheet);
            show(root, 420.0, 260.0);
            previousFocus.requestFocus();
            sheet.show();
            root.applyCss();
            root.layout();
            action.requestFocus();
            assertTrue(action.isFocused(), "modal bottom sheet action should accept focus before changing variant");

            sheet.setVariant(M3SheetVariant.STANDARD);
            KeyEvent standardEscape = keyEvent(KeyCode.ESCAPE);
            action.fireEvent(standardEscape);

            assertFalse(standardEscape.isConsumed(), "standard bottom sheet should not consume Escape as modal dismissal");
            assertTrue(sheet.isShown(), "standard bottom sheet should remain shown after Escape");
            assertTrue(action.isFocused(), "standard bottom sheet should keep focus on the action after Escape");
        }));
    }

    /// Verifies that changing a shown side sheet to standard releases modal keyboard trapping.
    @Test
    void sideSheetStandardVariantDoesNotUseModalFocusTrap() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button previousFocus = new M3Button("Previous");
            M3TextField editor = new M3TextField("Editable content");
            M3Button action = new M3Button("Close");
            M3SideSheet sheet = sideSheet("Details", editor);
            sheet.getActions().add(action);
            sheet.setVariant(M3SheetVariant.MODAL);
            sheet.setShown(false);

            VBox root = new VBox(previousFocus, sheet);
            show(root, 420.0, 260.0);
            previousFocus.requestFocus();
            sheet.show();
            root.applyCss();
            root.layout();
            action.requestFocus();
            assertTrue(action.isFocused(), "modal side sheet action should accept focus before changing variant");

            sheet.setVariant(M3SheetVariant.STANDARD);
            KeyEvent standardEscape = keyEvent(KeyCode.ESCAPE);
            action.fireEvent(standardEscape);

            assertFalse(standardEscape.isConsumed(), "standard side sheet should not consume Escape as modal dismissal");
            assertTrue(sheet.isShown(), "standard side sheet should remain shown after Escape");
            assertTrue(action.isFocused(), "standard side sheet should keep focus on the action after Escape");
        }));
    }

    /// Verifies that arrow keys traverse visible bottom sheet action buttons.
    @Test
    void bottomSheetActionsSupportHorizontalKeyboardTraversal() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            M3BottomSheet sheet = bottomSheet("Queue", new Label("Bottom sheet content"));
            sheet.getActions().addAll(first, second, third);

            show(new VBox(sheet), 420.0, 260.0);
            first.requestFocus();
            assertTrue(first.isFocused(), "the first bottom sheet action should accept initial focus");

            first.fireEvent(keyEvent(KeyCode.RIGHT));
            assertTrue(second.isFocused(), "Right should move bottom sheet action focus forward");

            second.fireEvent(keyEvent(KeyCode.LEFT));
            assertTrue(first.isFocused(), "Left should move bottom sheet action focus backward");
        }));
    }

    /// Verifies that arrow keys traverse visible side sheet action buttons.
    @Test
    void sideSheetActionsSupportHorizontalKeyboardTraversal() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            M3SideSheet sheet = sideSheet("Details", new Label("Side sheet content"));
            sheet.getActions().addAll(first, second, third);

            show(new VBox(sheet), 420.0, 260.0);
            first.requestFocus();
            assertTrue(first.isFocused(), "the first side sheet action should accept initial focus");

            first.fireEvent(keyEvent(KeyCode.RIGHT));
            assertTrue(second.isFocused(), "Right should move side sheet action focus forward");

            second.fireEvent(keyEvent(KeyCode.LEFT));
            assertTrue(first.isFocused(), "Left should move side sheet action focus backward");
        }));
    }

    /// Verifies that bottom sheet action traversal does not steal horizontal keys from content editors.
    @Test
    void bottomSheetDoesNotStealHorizontalKeysFromContentEditor() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField editor = new M3TextField("Editable content");
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3BottomSheet sheet = bottomSheet("Queue", editor);
            sheet.getActions().addAll(first, second);

            show(new VBox(sheet), 420.0, 260.0);
            editor.requestFocus();
            assertTrue(editor.isFocused(), "the bottom sheet content editor should accept initial focus");

            editor.fireEvent(keyEvent(KeyCode.RIGHT));
            editor.fireEvent(keyEvent(KeyCode.LEFT));

            assertTrue(editor.isFocused(), "horizontal keys should stay with the bottom sheet content editor");
            assertFalse(first.isFocused(), "bottom sheet action traversal should not focus the first action from content editing");
            assertFalse(second.isFocused(), "bottom sheet action traversal should not focus the second action from content editing");
        }));
    }

    /// Verifies that side sheet action traversal does not steal horizontal keys from content editors.
    @Test
    void sideSheetDoesNotStealHorizontalKeysFromContentEditor() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField editor = new M3TextField("Editable content");
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3SideSheet sheet = sideSheet("Details", editor);
            sheet.getActions().addAll(first, second);

            show(new VBox(sheet), 420.0, 260.0);
            editor.requestFocus();
            assertTrue(editor.isFocused(), "the side sheet content editor should accept initial focus");

            editor.fireEvent(keyEvent(KeyCode.RIGHT));
            editor.fireEvent(keyEvent(KeyCode.LEFT));

            assertTrue(editor.isFocused(), "horizontal keys should stay with the side sheet content editor");
            assertFalse(first.isFocused(), "side sheet action traversal should not focus the first action from content editing");
            assertFalse(second.isFocused(), "side sheet action traversal should not focus the second action from content editing");
        }));
    }

    /// Asserts cyclic modal focus traversal from outside focus into sheet-owned targets.
    private static void assertModalSheetFocusCycle(
            Node previousFocus,
            Node content,
            Node firstAction,
            Node secondAction,
            Node outside
    ) {
        assertTrue(previousFocus.isFocused(), "modal sheet should preserve previous focus until traversal starts");

        previousFocus.fireEvent(keyEvent(KeyCode.TAB));
        assertTrue(content.isFocused(), "Tab from outside focus should enter the first modal sheet target");

        content.fireEvent(keyEvent(KeyCode.TAB));
        assertTrue(firstAction.isFocused(), "Tab should move from sheet content to the first action");

        firstAction.fireEvent(keyEvent(KeyCode.TAB));
        assertTrue(secondAction.isFocused(), "Tab should move between sheet actions");

        secondAction.fireEvent(keyEvent(KeyCode.TAB));
        assertTrue(content.isFocused(), "Tab should wrap from the last sheet action to content");

        content.fireEvent(keyEvent(KeyCode.TAB, true));
        assertTrue(secondAction.isFocused(), "Shift+Tab should wrap from content to the last sheet action");

        secondAction.fireEvent(keyEvent(KeyCode.F6));
        assertTrue(content.isFocused(), "F6 should use the same modal cycle as Tab");

        content.fireEvent(keyEvent(KeyCode.F6, true));
        assertTrue(secondAction.isFocused(), "Shift+F6 should cycle backward inside the modal sheet");
        assertFalse(outside.isFocused(), "modal focus traversal should not leave the sheet");
    }

    /// Shows a root node in a real JavaFX window and performs an initial layout pass.
    private static Scene show(Parent root, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        return scene;
    }

    /// Shows the supplied root in a modal JavaFX window and performs an initial layout pass.
    private static Scene showModal(Parent root, double width, double height) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        return scene;
    }

    /// Creates a key press event using the standard JavaFX test event constructor.
    private static KeyEvent keyEvent(KeyCode code) {
        return keyEvent(code, false);
    }

    /// Creates a key press event with an optional Shift modifier.
    private static KeyEvent keyEvent(KeyCode code, boolean shiftDown) {
        return keyEvent(code, shiftDown, false, false, false);
    }

    /// Creates a key press event with explicit keyboard modifiers.
    private static KeyEvent keyEvent(
            KeyCode code,
            boolean shiftDown,
            boolean controlDown,
            boolean altDown,
            boolean metaDown
    ) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shiftDown, controlDown, altDown, metaDown);
    }
}