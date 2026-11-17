// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.skins.M3FabMenuSkin;
import org.glavo.m3fx.skins.M3FloatingActionButtonSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies floating action buttons and expanding action menus.
@NotNullByDefault
final class M3FloatingActionContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that floating action buttons keep depth-style pressed scaling.
    @Tier2Test
    @Test
    void floatingActionButtonKeepsDepthPressedScaling() throws InterruptedException {
        assertPressedScaleAfterRealPulse(
                () -> new M3FloatingActionButton("Create"),
                96.0,
                56.0,
                0.98,
                "floating action button"
        );
    }

    /// Verifies that m3fx floating action buttons create the animated floating action button skin.
    @Test
    void floatingActionButtonCreatesAnimatedSkin() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3FloatingActionButtonSkin.class, button.getSkin());
    }

    /// Verifies that the floating action button skin handles mouse and keyboard activation.
    @Test
    void floatingActionButtonSkinHandlesActivationEvents() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        AtomicInteger fireCount = new AtomicInteger();
        button.setOnAction(event -> fireCount.incrementAndGet());

        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(56.0, 56.0);

        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(button.isArmed());
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertEquals(1, fireCount.get());

        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        assertTrue(button.isArmed());
        button.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertEquals(2, fireCount.get());

        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertEquals(3, fireCount.get());
    }

    /// Verifies that floating action button graphics stay centered in fixed-size FAB containers.
    @Test
    void floatingActionButtonCentersGraphicContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton small = createGraphicFab(
                    visualIcon("add"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.SMALL
            );
            M3FloatingActionButton regular = createGraphicFab(
                    visualIcon("star"),
                    M3FloatingActionButtonVariant.SECONDARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3FloatingActionButton large = createGraphicFab(
                    visualIcon("done"),
                    M3FloatingActionButtonVariant.TERTIARY,
                    M3FloatingActionButtonSize.LARGE
            );
            FlowPane row = new FlowPane(16.0, 16.0, small, regular, large);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 360.0, 160.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(360.0, 160.0);
            row.layout();

            assertFixedTargetControlsKeepCenteredContent(row);
            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, small, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, regular, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, large, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-fab-centering.png"
            ));
        });
    }

    /// Verifies that interactive floating action button states keep Material variant colors.
    @Test
    void floatingActionButtonStateStylesPreserveVariantColors() {
        M3FloatingActionButton button = new M3FloatingActionButton("Create");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        applyInteractivePseudoClasses(button);

        button.setVariant(M3FloatingActionButtonVariant.SURFACE);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(19, 20, 21), Color.rgb(1, 2, 3));

        button.setVariant(M3FloatingActionButtonVariant.PRIMARY_CONTAINER);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(22, 23, 24), Color.rgb(25, 26, 27));

        button.setVariant(M3FloatingActionButtonVariant.SECONDARY_CONTAINER);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(7, 8, 9), Color.rgb(10, 11, 12));

        button.setVariant(M3FloatingActionButtonVariant.TERTIARY_CONTAINER);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(28, 29, 30), Color.rgb(31, 32, 33));

        button.setVariant(M3FloatingActionButtonVariant.PRIMARY);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(1, 2, 3), Color.rgb(4, 5, 6));

        button.setVariant(M3FloatingActionButtonVariant.SECONDARY);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(46, 47, 48), Color.rgb(49, 50, 51));

        button.setVariant(M3FloatingActionButtonVariant.TERTIARY);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(52, 53, 54), Color.rgb(55, 56, 57));
    }

    /// Verifies that floating action button component token properties are styleable from CSS.
    @Test
    void floatingActionButtonTokensAreStyleable() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        button.setStyle("-m3-container-size: 64px; -m3-container-shape: 20px; "
                + "-m3-horizontal-padding: 22px; -m3-trailing-padding: 30px;");

        applyCss(button);

        assertEquals(64.0, button.getContainerSize(), 0.0001);
        assertEquals(20.0, button.getContainerShape(), 0.0001);
        assertEquals(22.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(30.0, button.getTrailingPadding(), 0.0001);
        assertEquals(64.0, button.prefWidth(-1.0), 0.0001);
        assertEquals(64.0, button.prefHeight(-1.0), 0.0001);
        assertEquals(0.0, button.getPadding().getLeft(), 0.0001);

        button.setText("Create");
        applyCss(button);

        assertEquals(javafx.scene.layout.Region.USE_COMPUTED_SIZE, button.getPrefWidth(), 0.0001);
        assertEquals(64.0, button.prefHeight(-1.0), 0.0001);
        assertEquals(22.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(30.0, button.getPadding().getRight(), 0.0001);

        button.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        applyCss(button);

        assertEquals(22.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(30.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that floating action button variants and sizes update style classes.
    @Test
    void floatingActionButtonVariantAndSizeUpdateStyleClasses() {
        M3FloatingActionButton button = createFab(
                "+",
                null,
                M3FloatingActionButtonVariant.PRIMARY,
                M3FloatingActionButtonSize.REGULAR
        );

        assertTrue(button.getStyleClass().contains("m3-fab"));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonVariant.PRIMARY.styleClass()));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonSize.REGULAR.styleClass()));

        button.setVariant(M3FloatingActionButtonVariant.TERTIARY);
        button.setSize(M3FloatingActionButtonSize.LARGE);

        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonVariant.TERTIARY.styleClass()));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonSize.LARGE.styleClass()));
    }

    /// Verifies that floating action button menus expand, collapse, and manage action item state.
    @Test
    void fabMenuExpandsAndCollapsesActionItems() {
        M3FloatingActionButton first = new M3FloatingActionButton("A");
        M3FloatingActionButton second = new M3FloatingActionButton("B");
        M3FabMenu menu = new M3FabMenu();
        menu.getItems().addAll(first, second);

        assertTrue(menu.getStyleClass().contains("m3-fab-menu"));
        assertTrue(menu.getToggleButton().getStyleClass().contains("m3-fab-menu-toggle"));
        assertTrue(menu.getCloseButton().getStyleClass().contains("m3-fab-menu-close"));
        M3InternalIcon closeIcon = assertInstanceOf(
                M3InternalIcon.class,
                menu.getCloseButton().getGraphic(),
                "FAB menu close icon"
        );
        assertEquals(M3FloatingActionButtonVariant.PRIMARY, menu.getCloseButton().getVariant());
        assertInternalIconColorRole(closeIcon, M3InternalIcon.ColorRole.ON_PRIMARY);

        menu.getToggleButton().setVariant(M3FloatingActionButtonVariant.SECONDARY_CONTAINER);

        assertEquals(M3FloatingActionButtonVariant.SECONDARY, menu.getCloseButton().getVariant());
        assertInternalIconColorRole(closeIcon, M3InternalIcon.ColorRole.ON_SECONDARY);

        menu.getToggleButton().setVariant(M3FloatingActionButtonVariant.TERTIARY_CONTAINER);

        assertEquals(M3FloatingActionButtonVariant.TERTIARY, menu.getCloseButton().getVariant());
        assertInternalIconColorRole(closeIcon, M3InternalIcon.ColorRole.ON_TERTIARY);

        menu.getToggleButton().setVariant(M3FloatingActionButtonVariant.PRIMARY_CONTAINER);
        assertTrue(menu.getToggleButton().isVisible());
        assertFalse(menu.getCloseButton().isVisible());
        assertTrue(first.getStyleClass().contains("m3-fab-menu-action"));
        assertFalse(menu.isExpanded());
        assertFalse(first.isVisible());
        assertFalse(first.isManaged());
        assertEquals(2, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        menu.show();

        assertTrue(menu.isExpanded());
        assertTrue(first.isVisible());
        assertTrue(first.isManaged());
        assertEquals(1.0, first.getOpacity(), 0.0001);
        assertFalse(menu.getToggleButton().isVisible());
        assertTrue(menu.getCloseButton().isVisible());

        first.fire();

        assertFalse(menu.isExpanded());

        assertTrue(menu.getToggleButton().isVisible());
        assertFalse(menu.getCloseButton().isVisible());
        menu.show();

        menu.hide();

        assertFalse(menu.isExpanded());
        assertFalse(first.isVisible());
        assertFalse(first.isManaged());

        menu.getItems().remove(first);

        assertFalse(first.getStyleClass().contains("m3-fab-menu-action"));
        assertTrue(first.isVisible());
        assertTrue(first.isManaged());
    }

    /// Verifies that FAB-menu actions and activators follow the logical end edge without rewriting layout data.
    @Test
    void fabMenuAnchorsToLogicalEndAcrossRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton toggle = new M3FloatingActionButton(new M3Icon("+"));
            M3FloatingActionButton action = new M3FloatingActionButton("Create");
            M3FabMenu menu = new M3FabMenu(toggle);
            menu.getItems().add(action);
            menu.show();
            menu.setManaged(false);
            Pane root = new Pane(menu);
            Scene scene = new Scene(root, 280.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            menu.resizeRelocate(20.0, 20.0, 220.0, 180.0);
            root.layout();
            menu.layout();

            Bounds menuBounds = menu.localToScene(menu.getBoundsInLocal());
            Bounds toggleBounds = toggle.localToScene(toggle.getBoundsInLocal());
            Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
            assertEquals(0.0, menuBounds.getMaxX() - toggleBounds.getMaxX(), 0.01);
            assertEquals(0.0, menuBounds.getMaxX() - actionBounds.getMaxX(), 0.01);

            menu.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            menu.layout();

            menuBounds = menu.localToScene(menu.getBoundsInLocal());
            toggleBounds = toggle.localToScene(toggle.getBoundsInLocal());
            actionBounds = action.localToScene(action.getBoundsInLocal());
            assertEquals(0.0, toggleBounds.getMinX() - menuBounds.getMinX(), 0.01);
            assertEquals(0.0, actionBounds.getMinX() - menuBounds.getMinX(), 0.01);

            menu.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            root.layout();
            menu.layout();

            menuBounds = menu.localToScene(menu.getBoundsInLocal());
            toggleBounds = toggle.localToScene(toggle.getBoundsInLocal());
            actionBounds = action.localToScene(action.getBoundsInLocal());
            assertEquals(0.0, menuBounds.getMaxX() - toggleBounds.getMaxX(), 0.01);
            assertEquals(0.0, menuBounds.getMaxX() - actionBounds.getMaxX(), 0.01);
        });
    }

    /// Verifies that floating action button menus toggle through the main button and accessibility actions.
    @Test
    void fabMenuTogglesFromButtonAndAccessibilityActions() {
        M3FloatingActionButton customToggle = new M3FloatingActionButton("+");
        M3FloatingActionButton action = new M3FloatingActionButton("A");
        M3FabMenu menu = new M3FabMenu(customToggle);
        menu.getItems().add(action);

        applyCss(menu);

        assertInstanceOf(M3FabMenuSkin.class, menu.getSkin());
        assertSame(customToggle, menu.getToggleButton());
        assertEquals(false, menu.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));

        menu.getToggleButton().fire();

        assertTrue(menu.isExpanded());
        assertEquals(true, menu.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));

        menu.executeAccessibleAction(AccessibleAction.FIRE);

        assertFalse(menu.isExpanded());

        menu.executeAccessibleAction(AccessibleAction.EXPAND);

        assertTrue(menu.isExpanded());

        menu.executeAccessibleAction(AccessibleAction.COLLAPSE);

        assertFalse(menu.isExpanded());
    }

    /// Verifies that FAB menu keyboard navigation, dismissal, and actions keep focus reachable.
    @Test
    void fabMenuRestoresToggleFocusWhenCollapsedFromFocusedAction() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton firstAction = new M3FloatingActionButton("A");
            M3FloatingActionButton disabledAction = new M3FloatingActionButton("D");
            M3FloatingActionButton secondAction = new M3FloatingActionButton("B");
            M3Button outside = new M3Button("Outside");
            M3FabMenu menu = new M3FabMenu();
            disabledAction.setDisable(true);
            menu.getItems().addAll(firstAction, disabledAction, secondAction);
            Pane root = new Pane(outside, menu);
            Stage stage = new Stage();

            try {
                menu.setAnimationsEnabled(false);
                Scene scene = new Scene(root, 220.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menu.resizeRelocate(24.0, 24.0, 120.0, 160.0);
                root.layout();

                menu.show();
                outside.requestFocus();
                assertTrue(outside.isFocused());
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(firstAction.isFocused());
                outside.requestFocus();
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(secondAction.isFocused());
                firstAction.requestFocus();
                assertTrue(firstAction.isFocused());
                assertSame(firstAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                firstAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(secondAction.isFocused());
                assertFalse(disabledAction.isFocused());
                assertSame(secondAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                secondAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                assertTrue(menu.getCloseButton().isFocused());
                assertSame(menu.getCloseButton(), menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                menu.getCloseButton().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(secondAction.isFocused());
                assertSame(secondAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                secondAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(menu.isExpanded());
                assertTrue(menu.getToggleButton().isFocused());
                assertSame(menu.getToggleButton(), menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.show();
                firstAction.requestFocus();
                assertTrue(firstAction.isFocused());
                assertSame(firstAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                firstAction.fire();

                assertFalse(menu.isExpanded());
                assertTrue(menu.getToggleButton().isFocused());
                assertSame(menu.getToggleButton(), menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menu.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that FAB menu accessibility actions route focus through visible action items.
    @Test
    void fabMenuRoutesAccessibleFocusThroughActionItems() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton firstAction = new M3FloatingActionButton("A");
            M3FloatingActionButton secondAction = new M3FloatingActionButton("B");
            M3FabMenu menu = new M3FabMenu();
            menu.getItems().addAll(firstAction, secondAction);
            Pane root = new Pane(menu);
            Stage stage = new Stage();

            try {
                menu.setAnimationsEnabled(false);
                Scene scene = new Scene(root, 220.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menu.resizeRelocate(24.0, 24.0, 120.0, 160.0);
                root.layout();

                assertSame(menu.getToggleButton(), menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(menu.isExpanded());
                assertTrue(firstAction.isFocused());
                assertSame(firstAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, secondAction);

                assertTrue(menu.isExpanded());
                assertTrue(secondAction.isFocused());
                assertSame(secondAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(secondAction.isFocused());
                assertSame(secondAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(secondAction.isFocused());
                assertSame(secondAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(menu.isExpanded());
                assertTrue(menu.getToggleButton().isFocused());
                assertSame(menu.getToggleButton(), menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menu.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that FAB menu accessibility reveal rejects unrevealable explicit child targets before expansion.
    @Test
    void fabMenuRejectsUnrevealableAccessibleActionTargetsBeforeExpanding() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button visibleNestedAction = new M3Button("Visible nested");
            M3Button hiddenNestedAction = new M3Button("Hidden nested");
            hiddenNestedAction.setVisible(false);
            StackPane compositeAction = new StackPane(visibleNestedAction, hiddenNestedAction);
            compositeAction.setPrefSize(144.0, 64.0);

            M3FloatingActionButton disabledAction = new M3FloatingActionButton("D");
            disabledAction.setDisable(true);
            M3FloatingActionButton tooltipOwnerAction = new M3FloatingActionButton("T");
            M3Button disabledTooltipAction = new M3Button("Tooltip disabled");
            disabledTooltipAction.setDisable(true);
            M3RichTooltip tooltip = installRichTooltip(
                    tooltipOwnerAction,
                    "Tooltip action",
                    "Disabled tooltip actions cannot be revealed.",
                    disabledTooltipAction
            );

            M3Button outside = new M3Button("Outside");
            M3FabMenu menu = new M3FabMenu();
            menu.getItems().addAll(compositeAction, disabledAction, tooltipOwnerAction);
            Pane root = new Pane(outside, menu);
            Stage stage = new Stage();

            try {
                menu.setAnimationsEnabled(false);
                Scene scene = new Scene(root, 280.0, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menu.resizeRelocate(24.0, 24.0, 180.0, 180.0);
                root.layout();

                outside.requestFocus();
                assertTrue(outside.isFocused());

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenNestedAction);

                assertFalse(menu.isExpanded());
                assertFalse(hiddenNestedAction.isFocused());
                assertTrue(outside.isFocused());

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledAction);

                assertFalse(menu.isExpanded());
                assertFalse(disabledAction.isFocused());
                assertTrue(outside.isFocused());

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledTooltipAction);

                assertFalse(menu.isExpanded());
                assertFalse(tooltip.isShowing());
                assertFalse(disabledTooltipAction.isFocused());
                assertTrue(outside.isFocused());

                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, visibleNestedAction);

                assertTrue(menu.isExpanded());
                assertTrue(visibleNestedAction.isFocused());
                assertSame(visibleNestedAction, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(tooltipOwnerAction, tooltip);
                menu.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that FAB menu action spacing is styleable from CSS.
    @Test
    void fabMenuActionSpacingTokenIsStyleable() {
        M3FabMenu menu = new M3FabMenu(new M3FloatingActionButton());
        menu.setStyle("-m3-fab-menu-action-spacing: 18px; -m3-fab-menu-close-spacing: 20px;");

        applyCss(menu);

        assertEquals(18.0, menu.getActionSpacing(), 0.0001);
        assertEquals(20.0, menu.getCloseSpacing(), 0.0001);
    }

    /// Verifies that FAB menu expand and collapse animations reach observable public states.
    @Tier2Test
    @Test
    void fabMenuExpandAndCollapseAnimationsReachObservableStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FabMenu> menuReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FloatingActionButton> actionReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> fabMenuActionIsTransitioning(actionReference),
                    () -> describeFabMenuActionState(actionReference, "FAB menu action never reached an intermediate expand frame"),
                    () -> {
                        M3FloatingActionButton action = new M3FloatingActionButton("A");
                        M3FabMenu menu = new M3FabMenu();
                        menu.getItems().add(action);
                        Pane root = new Pane(menu);
                        Scene scene = new Scene(root, 220.0, 220.0);
                        Stage stage = new Stage();

                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        menu.resizeRelocate(24.0, 24.0, 120.0, 160.0);
                        root.layout();

                        stageReference.set(stage);
                        rootReference.set(root);
                        menuReference.set(menu);
                        actionReference.set(action);
                        menu.show();
                    },
                    () -> {
                        M3FloatingActionButton action = Objects.requireNonNull(actionReference.get(), "action");
                        assertFabActionEnterNodeIntermediate(action);
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> fabMenuExpandedStateHasSettled(menuReference, actionReference),
                    () -> describeFabMenuState(
                            menuReference,
                            actionReference,
                            "FAB menu never settled expanded"
                    ),
                    () -> {
                    },
                    () -> {
                        M3FabMenu menu = Objects.requireNonNull(menuReference.get(), "menu");
                        M3FloatingActionButton action = Objects.requireNonNull(actionReference.get(), "action");
                        assertFabMenuExpandedStateSettled(menu, action);
                        menu.hide();
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> fabMenuActionIsTransitioning(actionReference),
                    () -> describeFabMenuActionState(actionReference, "FAB menu action never reached an intermediate collapse frame"),
                    () -> {
                    },
                    () -> {
                        M3FloatingActionButton action = Objects.requireNonNull(actionReference.get(), "action");
                        assertFabActionEnterNodeIntermediate(action);
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> fabMenuCollapsedStateHasSettled(menuReference, actionReference),
                    () -> describeFabMenuState(
                            menuReference,
                            actionReference,
                            "FAB menu never settled collapsed"
                    ),
                    () -> {
                    },
                    () -> {
                        M3FabMenu menu = Objects.requireNonNull(menuReference.get(), "menu");
                        M3FloatingActionButton action = Objects.requireNonNull(actionReference.get(), "action");
                        assertFabMenuCollapsedStateSettled(menu, action);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Pane root = rootReference.get();
                if (root != null) {
                    FxTestUtils.setCssAnimationsEnabled(root, true);
                    FxTestUtils.clearMotionScheme(root);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Returns whether a tested FAB menu action is rendering an intermediate transition frame.
    private static boolean fabMenuActionIsTransitioning(
            AtomicReference<@Nullable M3FloatingActionButton> actionReference
    ) {
        @Nullable M3FloatingActionButton action = actionReference.get();
        if (action == null) {
            return false;
        }

        layoutNodeTree(action);
        return fabActionEnterNodeIsTransitioning(action);
    }

    /// Returns whether a tested FAB menu has reached the final expanded state.
    private static boolean fabMenuExpandedStateHasSettled(
            AtomicReference<@Nullable M3FabMenu> menuReference,
            AtomicReference<@Nullable M3FloatingActionButton> actionReference
    ) {
        @Nullable M3FabMenu menu = menuReference.get();
        @Nullable M3FloatingActionButton action = actionReference.get();
        if (menu == null || action == null) {
            return false;
        }

        layoutNodeTree(menu);
        return menu.isExpanded()
                && action.isVisible()
                && action.isManaged()
                && Math.abs(action.getOpacity() - 1.0) < 0.0001
                && Math.abs(action.getScaleX() - 1.0) < 0.0001
                && Math.abs(action.getScaleY() - 1.0) < 0.0001
                && Math.abs(action.getTranslateY()) < 0.0001;
    }

    /// Returns whether a tested FAB menu has reached the final collapsed state.
    private static boolean fabMenuCollapsedStateHasSettled(
            AtomicReference<@Nullable M3FabMenu> menuReference,
            AtomicReference<@Nullable M3FloatingActionButton> actionReference
    ) {
        @Nullable M3FabMenu menu = menuReference.get();
        @Nullable M3FloatingActionButton action = actionReference.get();
        if (menu == null || action == null) {
            return false;
        }

        layoutNodeTree(menu);
        return !menu.isExpanded()
                && !action.isVisible()
                && !action.isManaged()
                && Math.abs(action.getOpacity()) < 0.0001
                && Math.abs(action.getScaleX() - 0.86) < 0.0001
                && Math.abs(action.getScaleY() - 0.86) < 0.0001
                && Math.abs(action.getTranslateY() - 16.0) < 0.0001;
    }

    /// Describes the current tested FAB menu action state for timeout diagnostics.
    private static String describeFabMenuActionState(
            AtomicReference<@Nullable M3FloatingActionButton> actionReference,
            String description
    ) {
        return description + ": " + describeNodeState(actionReference.get());
    }

    /// Describes the current tested FAB menu and action state for timeout diagnostics.
    private static String describeFabMenuState(
            AtomicReference<@Nullable M3FabMenu> menuReference,
            AtomicReference<@Nullable M3FloatingActionButton> actionReference,
            String description
    ) {
        @Nullable M3FabMenu menu = menuReference.get();
        return description
                + ": menuExpanded=" + (menu == null ? "<null>" : menu.isExpanded())
                + ", menu=" + describeNodeState(menu)
                + ", action=" + describeNodeState(actionReference.get());
    }

    /// Verifies that a FAB menu and action reached the final expanded state.
    private static void assertFabMenuExpandedStateSettled(M3FabMenu menu, M3FloatingActionButton action) {
        layoutNodeTree(menu);
        assertTrue(menu.isExpanded());
        assertEquals(true, menu.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertSame(action, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertTrue(action.isVisible());
        assertTrue(action.isManaged());
        assertEquals(1.0, action.getOpacity(), 0.0001);
        assertEquals(1.0, action.getScaleX(), 0.0001);
        assertEquals(1.0, action.getScaleY(), 0.0001);
        assertEquals(0.0, action.getTranslateY(), 0.0001);
    }

    /// Verifies that a FAB menu and action reached the final collapsed state.
    private static void assertFabMenuCollapsedStateSettled(M3FabMenu menu, M3FloatingActionButton action) {
        layoutNodeTree(menu);
        assertFalse(menu.isExpanded());
        assertEquals(false, menu.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertSame(menu.getToggleButton(), menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertFalse(action.isVisible());
        assertFalse(action.isManaged());
        assertEquals(0.0, action.getOpacity(), 0.0001);
        assertEquals(0.86, action.getScaleX(), 0.0001);
        assertEquals(0.86, action.getScaleY(), 0.0001);
        assertEquals(16.0, action.getTranslateY(), 0.0001);
    }

    /// Applies CSS and layouts the visible parent chain for a tested node.
    private static void layoutNodeTree(Node node) {
        Node current = node;
        while (current != null) {
            current.applyCss();
            if (current instanceof Parent parent) {
                parent.layout();
            }
            current = current.getParent();
        }
    }
}
