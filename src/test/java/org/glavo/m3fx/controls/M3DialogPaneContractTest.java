// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies dialog content, actions, layout, and presentation configuration.
@NotNullByDefault
final class M3DialogPaneContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that dialog pane component token properties are styleable from CSS.
    @Test
    void dialogPaneTokensAreStyleable() {
        M3DialogPane dialogPane = new M3DialogPane();
        M3Icon graphic = new M3Icon("info");
        graphic.setStyle("-fx-opacity: 0.75;");
        dialogPane.setGraphic(graphic);
        M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancelAction.setCancelButton(true);
        M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
        okAction.setDefaultButton(true);
        dialogPane.getActions().setAll(cancelAction, okAction);
        String applicationStyle = "-m3-container-shape: 20px; -m3-content-padding: 28px; "
                + "-m3-container-min-width: 300px; -m3-container-max-width: 520px; "
                + "-m3-action-spacing: 12px; -m3-dialog-icon-size: 28px;";
        dialogPane.setStyle(applicationStyle);
        Pane root = new Pane(dialogPane);
        Scene scene = new Scene(root, 480.0, 240.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        dialogPane.resizeRelocate(0.0, 0.0, 420.0, 180.0);
        root.applyCss();
        root.layout();
        dialogPane.layout();

        assertEquals(20.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(28.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(40.0, dialogPane.getPadding().getTop(), 0.0001);
        assertEquals(40.0, dialogPane.getPadding().getRight(), 0.0001);
        assertEquals(40.0, dialogPane.getPadding().getBottom(), 0.0001);
        assertEquals(40.0, dialogPane.getPadding().getLeft(), 0.0001);
        assertEquals(300.0, dialogPane.getContainerMinWidth(), 0.0001);
        assertEquals(520.0, dialogPane.getContainerMaxWidth(), 0.0001);
        assertEquals(12.0, dialogPane.getActionSpacing(), 0.0001);
        assertEquals(28.0, dialogPane.getIconSize(), 0.0001);
        assertEquals(324.0, dialogPane.minWidth(-1.0), 0.0001);
        assertEquals(544.0, dialogPane.maxWidth(-1.0), 0.0001);
        assertEquals(28.0, graphic.getIconSize(), 0.0001);
        assertEquals(M3IconVariant.ON_SURFACE_VARIANT, graphic.getVariant());
        assertEquals("-fx-opacity: 0.75;", graphic.getStyle());
        assertEquals(M3Theme.defaultTheme().colorScheme().getColor(org.glavo.monetfx.ColorRole.SECONDARY),
                iconFill(graphic));
        HBox actionRow = assertInstanceOf(
                HBox.class,
                dialogPane.lookup("." + "m3-dialog-actions")
        );
        assertEquals(12.0, actionRow.getSpacing(), 0.0001);
        Region dialogSurface = lookupRegion(dialogPane, ".m3-dialog-surface");
        assertRegionRadii(dialogSurface, 20.0, 20.0, 20.0, 20.0);
        assertEquals(applicationStyle, dialogPane.getStyle());

        dialogPane.setContainerShape(12.0);
        root.applyCss();
        root.layout();
        dialogPane.layout();

        assertEquals(12.0, dialogPane.getContainerShape(), 0.0001);
        assertRegionRadii(dialogSurface, 12.0, 12.0, 12.0, 12.0);
        assertEquals(applicationStyle, dialogPane.getStyle());
    }

    /// Verifies that dialog panes retain, configure, and incrementally lay out caller-owned Material actions.
    @Test
    void dialogPaneUsesCallerOwnedMaterialActionButtons() {
        M3DialogPane dialogPane = new M3DialogPane();
        M3Button cancelButton = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancelButton.setCancelButton(true);
        M3Button okButton = new M3Button("OK", M3ButtonVariant.TEXT);
        okButton.setDefaultButton(true);
        dialogPane.getActions().setAll(cancelButton, okButton);
        Pane root = new Pane(dialogPane);
        Scene scene = new Scene(root, 480.0, 240.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        dialogPane.resizeRelocate(0.0, 0.0, 420.0, 180.0);
        root.applyCss();
        root.layout();
        dialogPane.layout();

        assertEquals(List.of(cancelButton, okButton), dialogPane.getActions());
        assertEquals(M3ButtonVariant.TEXT, cancelButton.getVariant());
        assertEquals(M3ButtonVariant.TEXT, okButton.getVariant());
        assertTrue(cancelButton.isCancelButton());
        assertTrue(okButton.isDefaultButton());
        assertSame(cancelButton, dialogPane.getCancelAction());
        assertSame(okButton, dialogPane.getDefaultAction());
        assertFalse(okButton.disableProperty().isBound());
        HBox actionRow = assertInstanceOf(
                HBox.class,
                dialogPane.lookup("." + "m3-dialog-actions")
        );
        assertSame(actionRow, cancelButton.getParent());
        assertSame(actionRow, okButton.getParent());
        List<String> buttonTexts = actionRow.getChildren().stream()
                .map(node -> assertInstanceOf(M3Button.class, node))
                .map(M3Button::getText)
                .toList();
        assertEquals(List.of("Cancel", "OK"), buttonTexts);

        okButton.setDisable(true);

        assertTrue(okButton.isDisabled());

        M3Button firstReplacement = new M3Button("First", M3ButtonVariant.TEXT);
        M3Button secondReplacement = new M3Button("Second", M3ButtonVariant.TEXT);
        dialogPane.getActions().setAll(firstReplacement, secondReplacement);
        root.applyCss();
        root.layout();
        dialogPane.layout();

        assertEquals(List.of(firstReplacement, secondReplacement), dialogPane.getActions());
        assertNull(dialogPane.getCancelAction());
        assertNull(dialogPane.getDefaultAction());
        assertNull(cancelButton.getParent());
        assertNull(okButton.getParent());
        assertSame(actionRow, firstReplacement.getParent());
        assertSame(actionRow, secondReplacement.getParent());
    }

    /// Verifies that dialog pane subnodes keep Material typography and colors.
    @Test
    void dialogPaneSubnodesUseMaterialStyles() {
        M3DialogPane dialogPane = new M3DialogPane();
        dialogPane.setHeaderText("Dialog title");
        dialogPane.setContentText("Dialog body");
        M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
        okAction.setDefaultButton(true);
        dialogPane.getActions().setAll(okAction);
        Pane root = new Pane(dialogPane);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        root.applyCss();

        Region dialogSurface = lookupRegion(dialogPane, ".m3-dialog-surface");
        assertRegionFill(dialogSurface, Color.rgb(19, 20, 21));
        assertDropShadow(dialogSurface);
        Region headerPanel = lookupRegion(dialogPane, ".header-panel");
        assertRegionFill(headerPanel, Color.TRANSPARENT);
        assertRegionFill(lookupRegion(headerPanel, ".label"), Color.TRANSPARENT);
        assertTrue(M3Accessible.containsNode(dialogPane, okAction));
        Region content = lookupRegion(dialogPane, ".content");
        assertRegionFill(content, Color.TRANSPARENT);
        Labeled contentLabel = assertInstanceOf(Labeled.class, dialogPane.lookup(".content-label"));
        assertEquals(Color.rgb(40, 41, 42), contentLabel.getTextFill());
        assertEquals("Dialog title Dialog body", dialogPane.getAccessibleText());
        assertEquals("Dialog title Dialog body", dialogPane.queryAccessibleAttribute(AccessibleAttribute.TEXT));
    }

    /// Verifies that dialog panes expose content and action buttons as indexed accessibility items.
    @Test
    void dialogPaneExposesIndexedAccessibleItems() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button contentAction = new M3Button("Content action");
            M3Button outsideAction = new M3Button("Outside action");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(contentAction);
            M3Button cancelButton = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelButton.setCancelButton(true);
            M3Button okButton = new M3Button("OK", M3ButtonVariant.TEXT);
            okButton.setDefaultButton(true);
            dialogPane.getActions().setAll(cancelButton, okButton);
            Pane root = new Pane(dialogPane, outsideAction);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 220.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();

                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(3, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(cancelButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
                assertNull(dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, -1));
                Object initialFocusNode = dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
                assertTrue(initialFocusNode == contentAction
                        || initialFocusNode == cancelButton
                        || initialFocusNode == okButton);

                outsideAction.requestFocus();

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(contentAction.isFocused());
                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, okButton);
                assertTrue(okButton.isFocused());
                assertEquals(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(okButton.isFocused());
                assertEquals(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(cancelButton.isFocused());
                assertEquals(cancelButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(cancelButton.isFocused());
                assertEquals(cancelButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, contentAction);
                assertTrue(contentAction.isFocused());
                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action rows support directional focus without stealing content editing keys.
    @Test
    void dialogPaneActionKeyboardTraversalFollowsDirectionWithoutStealingContentKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField content = new M3TextField("Editable content");
            M3Button cancelButton = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelButton.setCancelButton(true);
            M3Button skipButton = new M3Button("Skip", M3ButtonVariant.TEXT);
            M3Button okButton = new M3Button("OK", M3ButtonVariant.TEXT);
            okButton.setDefaultButton(true);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getActions().setAll(cancelButton, skipButton, okButton);
            Pane root = new Pane(dialogPane);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 260.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(24.0, 24.0, 460.0, 190.0);
                root.layout();

                skipButton.setDisable(true);

                content.requestFocus();
                content.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(content.isFocused());

                cancelButton.requestFocus();
                cancelButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(okButton.isFocused());

                okButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(cancelButton.isFocused());

                okButton.requestFocus();
                dialogPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(cancelButton.isFocused());

                cancelButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(okButton.isFocused());

                okButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertTrue(cancelButton.isFocused());

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                cancelButton.requestFocus();
                cancelButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(okButton.isFocused());

                okButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(cancelButton.isFocused());
                assertFalse(skipButton.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that hidden modal dialog panes release the scene focus trap to visible panes.
    @Test
    void hiddenModalDialogPaneReleasesFocusTrapToVisibleDialogPane() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            M3TextField firstContent = new M3TextField("First content");
            M3DialogPane firstPane = new M3DialogPane();
            firstPane.setContent(firstContent);
            M3Button firstOkButton = new M3Button("OK", M3ButtonVariant.TEXT);
            firstOkButton.setDefaultButton(true);
            firstPane.getActions().setAll(firstOkButton);
            firstPane.setModalActive(true);

            M3TextField hiddenContent = new M3TextField("Hidden content");
            M3DialogPane hiddenPane = new M3DialogPane();
            hiddenPane.setContent(hiddenContent);
            M3Button hiddenOkButton = new M3Button("OK", M3ButtonVariant.TEXT);
            hiddenOkButton.setDefaultButton(true);
            hiddenPane.getActions().setAll(hiddenOkButton);
            hiddenPane.setModalActive(true);

            VBox root = new VBox(firstPane, hiddenPane);
            Stage stage = new Stage();
            try {
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root, 520.0, 360.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                root.getChildren().remove(hiddenPane);
                root.getChildren().add(hiddenPane);
                root.applyCss();
                root.layout();

                firstContent.requestFocus();
                root.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB));
                assertTrue(hiddenContent.isFocused(), "re-attached dialog pane should own modal traversal");

                hiddenPane.setVisible(false);
                root.applyCss();
                root.layout();

                firstContent.requestFocus();
                root.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB));
                assertTrue(firstOkButton.isFocused(), "visible dialog pane should regain modal traversal");

                stage.hide();

                KeyEvent tab = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB);
                root.fireEvent(tab);
                assertFalse(tab.isConsumed(), "hidden modal windows should release scene traversal filters");
            } finally {
                stage.close();
            }
        }));
    }

    /// Verifies that dialog panes expose focused descendants from nested popups hosted by dialog content.
    @Test
    void dialogPaneFocusNodeTracksNestedContentPopups() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3MenuItem html = new M3MenuItem("HTML");
            M3SubMenuItem recent = new M3SubMenuItem("Recent", pdf, html);
            M3SubMenuItem export = new M3SubMenuItem("Export", recent);
            M3Button unrelatedAction = new M3Button("Unrelated");
            M3MenuButton menuButton = new M3MenuButton("More", export);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(menuButton);
            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            dialogPane.getActions().setAll(cancelAction, okAction);
            Pane root = new Pane(dialogPane);
            Stage stage = new Stage();
            try {
                menuButton.setAnimationsEnabled(false);
                export.setAnimationsEnabled(false);
                recent.setAnimationsEnabled(false);

                stage.setScene(new Scene(root, 480.0, 260.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(24.0, 24.0, 420.0, 190.0);
                root.layout();

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, List.of(unrelatedAction, html));

                assertTrue(menuButton.isShowing());
                assertTrue(export.isSubMenuShowing());
                assertTrue(recent.isSubMenuShowing());
                assertTrue(html.isFocused());
                assertSame(html, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(html, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(html.isFocused());
                assertSame(html, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pdf.requestFocus();

                assertTrue(pdf.isFocused());
                assertSame(pdf, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                recent.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertTrue(export.isSubMenuShowing());
                assertFalse(recent.isSubMenuShowing());
                assertTrue(recent.isFocused());
                assertSame(recent, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(recent, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertSame(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(export, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                recent.hideSubMenu();
                export.hideSubMenu();
                menuButton.hideMenu();
                menuButton.setAnimationsEnabled(true);
                export.setAnimationsEnabled(true);
                recent.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes fall back to the default action when content is not focusable.
    @Test
    void dialogPaneFocusNodeUsesDefaultActionWhenContentIsStatic() {
        FxTestUtils.runOnFxThread(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Label("Dialog body"));
            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button okButton = new M3Button("OK", M3ButtonVariant.TEXT);
            okButton.setDefaultButton(true);
            dialogPane.getActions().setAll(cancelAction, okButton);
            Pane root = new Pane(dialogPane);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 220.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();

                assertEquals(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(okButton.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that a dialog retains one Material pane and exposes its presentation configuration.
    @Test
    void dialogOwnsFixedMaterialPaneAndPresentationConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
            M3Dialog dialog = new M3Dialog();
            M3DialogPane pane = dialog.getDialogPane();
            pane.setHeaderText("Header");
            pane.setContentText("Body");
            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            pane.getActions().setAll(cancelAction, okAction);

            assertSame(pane, dialog.getDialogPane());
            assertEquals("Header", pane.getHeaderText());
            assertEquals("Body", pane.getContentText());
            assertEquals(List.of(cancelAction, okAction), pane.getActions());
            assertTrue(dialog.isDismissOnScrimClick());
            assertTrue(pane.getStyleClass().contains("m3-dialog-pane"));
            assertTrue(Objects.requireNonNull(pane.getUserAgentStylesheet(), "dialog stylesheet")
                    .endsWith("/styles/controls/dialog.css"));

            applyCss(pane);

            assertSame(okAction, pane.getDefaultAction());
        });
    }
}
