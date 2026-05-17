// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import org.glavo.m3fx.skins.M3BadgeSkin;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.glavo.m3fx.skins.M3CardSkin;
import org.glavo.m3fx.skins.M3CheckBoxSkin;
import org.glavo.m3fx.skins.M3ChipSkin;
import org.glavo.m3fx.skins.M3DividerSkin;
import org.glavo.m3fx.skins.M3FloatingActionButtonSkin;
import org.glavo.m3fx.skins.M3IconToggleButtonSkin;
import org.glavo.m3fx.skins.M3ListItemSkin;
import org.glavo.m3fx.skins.M3NavigationItemSkin;
import org.glavo.m3fx.skins.M3ProgressBarSkin;
import org.glavo.m3fx.skins.M3ProgressIndicatorSkin;
import org.glavo.m3fx.skins.M3RadioButtonSkin;
import org.glavo.m3fx.skins.M3SegmentedButtonSkin;
import org.glavo.m3fx.skins.M3SliderSkin;
import org.glavo.m3fx.skins.M3SnackbarSkin;
import org.glavo.m3fx.skins.M3SwitchSkin;
import org.glavo.m3fx.skins.M3TabSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests style classes and skins for m3fx controls.
@NotNullByDefault
final class M3ControlStyleTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    /// Verifies that button variants update their style classes.
    @Test
    void buttonVariantUpdatesStyleClass() {
        AtomicInteger actions = new AtomicInteger();
        M3Button button = M3Button.withVariant("Button", M3ButtonVariant.FILLED, event -> actions.incrementAndGet());

        assertTrue(button.getStyleClass().contains(M3Button.STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3ButtonVariant.FILLED.getStyleClass()));

        button.setVariant(M3ButtonVariant.OUTLINED);
        button.fire();

        assertEquals(1, actions.get());
        assertTrue(button.getStyleClass().contains(M3ButtonVariant.OUTLINED.getStyleClass()));
    }

    /// Verifies that m3fx buttons create the animated button skin.
    @Test
    void buttonCreatesAnimatedSkin() {
        M3Button button = new M3Button("Button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3ButtonSkin.class, button.getSkin());
    }

    /// Verifies that the button skin handles mouse and keyboard activation.
    @Test
    void buttonSkinHandlesActivationEvents() {
        M3Button button = new M3Button("Button");
        AtomicInteger fireCount = new AtomicInteger();
        button.setOnAction(event -> fireCount.incrementAndGet());

        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(100.0, 40.0);

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

    /// Verifies that button skins expose a bounded state layer and ripple.
    @Test
    void buttonSkinPlaysBoundedRippleOnPress() {
        M3Button button = new M3Button("Button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(100.0, 40.0);
        button.layout();

        assertInstanceOf(Region.class, button.lookup(".m3-state-layer"));
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));

        assertTrue(lookupRegion(button, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that button feedback layers use the same resolved shape as the button surface.
    @Test
    void buttonStateLayerUsesResolvedContainerShape() {
        runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            button.resize(100.0, 40.0);
            button.layout();
            root.applyCss();

            assertStateLayerShape(button, 20.0);

            button.setContainerShape(14.0);
            button.resize(120.0, 40.0);
            button.layout();
            root.applyCss();

            assertStateLayerShape(button, 14.0);
        });
    }

    /// Verifies that CSS reapplication after pressed pseudo-class changes does not hide ripples.
    @Test
    void buttonRippleSurvivesCssReapplicationAfterPress() {
        runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            button.resize(100.0, 40.0);
            button.layout();

            button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
            root.applyCss();

            Region ripple = lookupRegion(button, ".m3-ripple");
            assertTrue(ripple.getLayoutBounds().getWidth() > 0.0);
            assertTrue(ripple.getLayoutBounds().getHeight() > 0.0);
            assertTrue(ripple.getOpacity() > 0.0);
        });
    }

    /// Verifies that button skins clear transient interaction state when disabled.
    @Test
    void buttonSkinClearsPressedStateWhenDisabled() {
        M3Button button = new M3Button("Button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(100.0, 40.0);
        button.layout();

        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(button.isArmed());
        assertTrue(lookupRegion(button, ".m3-ripple").getOpacity() > 0.0);

        button.setDisable(true);

        assertFalse(button.isArmed());
        assertEquals(0.0, lookupRegion(button, ".m3-ripple").getOpacity(), 0.0001);
        assertEquals(1.0, button.getScaleX(), 0.0001);
        assertEquals(1.0, button.getScaleY(), 0.0001);
    }

    /// Verifies that disposed button skins no longer handle interaction events.
    @Test
    void buttonSkinRemovesInteractionHandlersWhenDisposed() {
        M3Button button = new M3Button("Button");
        AtomicInteger fireCount = new AtomicInteger();
        button.setOnAction(event -> fireCount.incrementAndGet());
        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(100.0, 40.0);

        Skin<?> skin = button.getSkin();
        skin.dispose();
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

        assertFalse(button.isArmed());
        assertEquals(0, fireCount.get());
    }

    /// Verifies that interactive button states keep Material variant colors.
    @Test
    void buttonStateStylesPreserveVariantColors() {
        M3Button button = new M3Button("Button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        applyInteractivePseudoClasses(button);

        button.setVariant(M3ButtonVariant.FILLED);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(1, 2, 3), Color.rgb(4, 5, 6));

        button.setVariant(M3ButtonVariant.TONAL);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(7, 8, 9), Color.rgb(10, 11, 12));

        button.setVariant(M3ButtonVariant.OUTLINED);
        root.applyCss();
        assertLabeledColors(button, Color.TRANSPARENT, Color.rgb(1, 2, 3));

        button.setVariant(M3ButtonVariant.TEXT);
        root.applyCss();
        assertLabeledColors(button, Color.TRANSPARENT, Color.rgb(1, 2, 3));

        button.setVariant(M3ButtonVariant.ELEVATED);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(16, 17, 18), Color.rgb(1, 2, 3));
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

        button.setVariant(M3FloatingActionButtonVariant.PRIMARY);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(22, 23, 24), Color.rgb(25, 26, 27));

        button.setVariant(M3FloatingActionButtonVariant.SECONDARY);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(7, 8, 9), Color.rgb(10, 11, 12));

        button.setVariant(M3FloatingActionButtonVariant.TERTIARY);
        root.applyCss();
        assertLabeledColors(button, Color.rgb(28, 29, 30), Color.rgb(31, 32, 33));
    }

    /// Verifies that button component token properties are styleable from CSS.
    @Test
    void buttonTokensAreStyleable() {
        M3Button button = new M3Button("Button");
        button.setStyle("-m3-container-height: 52px; -m3-container-shape: 14px; -m3-horizontal-padding: 18px;");

        applyCss(button);

        assertEquals(52.0, button.getContainerHeight(), 0.0001);
        assertEquals(14.0, button.getContainerShape(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(52.0, button.getPrefHeight(), 0.0001);
        assertEquals(18.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that icon buttons stay square when size tokens change.
    @Test
    void iconButtonSizeTracksContainerHeightToken() {
        M3IconButton button = new M3IconButton();
        button.setStyle("-m3-container-height: 48px;");

        applyCss(button);

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(48.0, button.getPrefWidth(), 0.0001);
        assertEquals(48.0, button.getPrefHeight(), 0.0001);
    }

    /// Verifies that floating action button component token properties are styleable from CSS.
    @Test
    void floatingActionButtonTokensAreStyleable() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        button.setStyle("-m3-container-size: 64px; -m3-container-shape: 20px; -m3-horizontal-padding: 22px;");

        applyCss(button);

        assertEquals(64.0, button.getContainerSize(), 0.0001);
        assertEquals(20.0, button.getContainerShape(), 0.0001);
        assertEquals(22.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, button.getPrefWidth(), 0.0001);
        assertEquals(64.0, button.getPrefHeight(), 0.0001);
        assertEquals(0.0, button.getPadding().getLeft(), 0.0001);

        button.setText("Create");
        applyCss(button);

        assertEquals(javafx.scene.layout.Region.USE_COMPUTED_SIZE, button.getPrefWidth(), 0.0001);
        assertEquals(64.0, button.getPrefHeight(), 0.0001);
        assertEquals(22.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that floating action button variants and sizes update style classes.
    @Test
    void floatingActionButtonVariantAndSizeUpdateStyleClasses() {
        M3FloatingActionButton button = M3FloatingActionButton.withVariant(
                "+",
                M3FloatingActionButtonVariant.PRIMARY,
                M3FloatingActionButtonSize.REGULAR
        );

        assertTrue(button.getStyleClass().contains(M3FloatingActionButton.STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonVariant.PRIMARY.getStyleClass()));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonSize.REGULAR.getStyleClass()));

        button.setVariant(M3FloatingActionButtonVariant.TERTIARY);
        button.setSize(M3FloatingActionButtonSize.LARGE);

        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonVariant.TERTIARY.getStyleClass()));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonSize.LARGE.getStyleClass()));
    }

    /// Verifies that card component token properties are styleable from CSS.
    @Test
    void cardTokensAreStyleable() {
        M3Card card = new M3Card();
        card.setStyle("-m3-container-shape: 18px; -m3-content-padding: 20px; -m3-outline-width: 2px;");

        applyCss(card);

        assertEquals(18.0, card.getContainerShape(), 0.0001);
        assertEquals(20.0, card.getContentPadding(), 0.0001);
        assertEquals(2.0, card.getOutlineWidth(), 0.0001);
    }

    /// Verifies that m3fx cards create the Material Design 3 skin.
    @Test
    void cardCreatesMaterialSkin() {
        M3Card card = new M3Card();

        applyCss(card);

        assertInstanceOf(M3CardSkin.class, card.getSkin());
    }

    /// Verifies that cards expose standard action handlers.
    @Test
    void cardFiresActionEvents() {
        AtomicInteger actionCount = new AtomicInteger();
        AtomicInteger eventCount = new AtomicInteger();
        M3Card card = new M3Card(new Label("Content"), M3CardVariant.OUTLINED, event -> actionCount.incrementAndGet());
        card.addEventHandler(ActionEvent.ACTION, event -> eventCount.incrementAndGet());

        card.fire();

        assertEquals(M3CardVariant.OUTLINED, card.getVariant());
        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());

        card.setDisable(true);
        card.fire();

        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());
    }

    /// Verifies that card skins expose bounded surface ripple feedback.
    @Test
    void cardSkinPlaysBoundedRippleOnSurfacePress() {
        M3Card card = new M3Card();
        Pane root = new Pane(card);
        Scene scene = new Scene(root, 220.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        card.resize(180.0, 80.0);
        card.layout();

        assertInstanceOf(Region.class, card.lookup(".m3-state-layer"));
        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 20.0, 20.0, true));

        assertTrue(lookupRegion(card, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that card skins route surface and keyboard activation to the card action.
    @Test
    void cardSkinRoutesSurfaceAndKeyboardActions() {
        M3Card card = new M3Card();
        AtomicInteger actionCount = new AtomicInteger();
        card.setOnAction(event -> actionCount.incrementAndGet());
        Pane root = new Pane(card);
        Scene scene = new Scene(root, 220.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        card.resize(180.0, 80.0);
        card.layout();

        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 20.0, 20.0, false));
        card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

        assertEquals(2, actionCount.get());

        card.setDisable(true);
        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 20.0, 20.0, false));
        card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));

        assertEquals(2, actionCount.get());
    }

    /// Verifies that snackbar component token properties are styleable from CSS.
    @Test
    void snackbarTokensAreStyleable() {
        M3Snackbar snackbar = new M3Snackbar("Message");
        snackbar.setStyle("-m3-container-shape: 10px; -m3-content-padding: 24px;");

        applyCss(snackbar);

        assertEquals(10.0, snackbar.getContainerShape(), 0.0001);
        assertEquals(24.0, snackbar.getContentPadding(), 0.0001);
    }

    /// Verifies that snackbars expose action constructors and programmatic action firing.
    @Test
    void snackbarActionConstructorsAndFireAction() {
        AtomicInteger actionCount = new AtomicInteger();
        AtomicInteger eventCount = new AtomicInteger();
        M3Snackbar snackbar = new M3Snackbar("Saved", "Undo", event -> actionCount.incrementAndGet());
        snackbar.addEventHandler(ActionEvent.ACTION, event -> eventCount.incrementAndGet());

        assertEquals("Saved", snackbar.getText());
        assertEquals("Undo", snackbar.getActionText());
        assertTrue(snackbar.hasAction());

        snackbar.fireAction();

        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());

        snackbar.setActionText("");
        snackbar.fireAction();

        assertFalse(snackbar.hasAction());
        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());

        snackbar.setActionText("Undo");
        snackbar.setDisable(true);
        snackbar.fireAction();

        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());
    }

    /// Verifies that m3fx snackbars create the Material Design 3 skin and action button.
    @Test
    void snackbarCreatesMaterialSkinAndActionButton() {
        M3Snackbar snackbar = new M3Snackbar("Saved");
        snackbar.setActionText("Undo");
        AtomicInteger actionCount = new AtomicInteger();
        snackbar.setOnAction(event -> actionCount.incrementAndGet());

        applyCss(snackbar);

        assertInstanceOf(M3SnackbarSkin.class, snackbar.getSkin());
        M3Button actionButton = assertInstanceOf(M3Button.class, snackbar.lookup(".m3-snackbar-action"));
        assertEquals(M3ButtonVariant.TEXT, actionButton.getVariant());
        assertTrue(actionButton.isVisible());
        assertTrue(actionButton.isManaged());

        actionButton.fire();

        assertEquals(1, actionCount.get());
    }

    /// Verifies that snackbar skins unbind internal nodes when disposed.
    @Test
    void snackbarSkinUnbindsInternalNodesWhenDisposed() {
        M3Snackbar snackbar = new M3Snackbar("Saved");
        snackbar.setActionText("Undo");

        applyCss(snackbar);

        M3SnackbarSkin skin = assertInstanceOf(M3SnackbarSkin.class, snackbar.getSkin());
        M3Button actionButton = assertInstanceOf(M3Button.class, snackbar.lookup(".m3-snackbar-action"));
        assertTrue(actionButton.textProperty().isBound());

        skin.dispose();

        assertFalse(actionButton.textProperty().isBound());
    }

    /// Verifies that snackbar colors override generic text button colors.
    @Test
    void snackbarActionUsesInverseColors() {
        M3Snackbar snackbar = new M3Snackbar("Saved");
        snackbar.setActionText("Undo");
        Pane root = new Pane(snackbar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + snackbarStateTestColors());
        root.applyCss();

        assertRegionFill(lookupRegion(snackbar, ".m3-snackbar-container"), Color.rgb(50, 51, 52));
        assertEquals(Color.rgb(53, 54, 55), ((Labeled) snackbar.lookup(".m3-snackbar-text")).getTextFill());
        M3Button actionButton = assertInstanceOf(M3Button.class, snackbar.lookup(".m3-snackbar-action"));
        assertEquals(Color.rgb(56, 57, 58), actionButton.getTextFill());
        assertRegionFill(lookupRegion(actionButton, ".m3-state-layer"), Color.rgb(56, 57, 58));
    }

    /// Verifies that snackbar hosts show action snackbars and route action events.
    @Test
    void snackbarHostShowsActionSnackbars() {
        runOnFxThread(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            host.setDisplayDuration(Duration.INDEFINITE);
            AtomicInteger actionCount = new AtomicInteger();
            Pane root = new Pane(host);
            Scene scene = new Scene(root, 320.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            host.show("Saved", "Undo", event -> actionCount.incrementAndGet());
            root.applyCss();

            M3Snackbar snackbar = assertInstanceOf(M3Snackbar.class, host.getSnackbar());
            assertTrue(host.getStyleClass().contains(M3SnackbarHost.STYLE_CLASS));
            assertTrue(host.isShowing());
            assertTrue(host.getChildren().contains(snackbar));
            assertTrue(snackbar.isVisible());
            assertTrue(snackbar.isManaged());

            M3Button actionButton = assertInstanceOf(M3Button.class, snackbar.lookup(".m3-snackbar-action"));
            actionButton.fire();

            assertEquals(1, actionCount.get());
            assertFalse(host.isShowing());
        });
    }

    /// Verifies that snackbar hosts remove dismissed snackbars after the exit animation.
    @Test
    void snackbarHostRemovesDismissedSnackbars() throws InterruptedException {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                M3SnackbarHost host = new M3SnackbarHost();
                host.setDisplayDuration(Duration.INDEFINITE);
                Pane root = new Pane(host);
                Scene scene = new Scene(root, 320.0, 120.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                host.show("Saved");
                root.applyCss();

                M3Snackbar snackbar = assertInstanceOf(M3Snackbar.class, host.getSnackbar());
                host.dismiss();

                PauseTransition pause = new PauseTransition(Duration.millis(180.0));
                pause.setOnFinished(event -> {
                    try {
                        assertFalse(host.getChildren().contains(snackbar));
                        assertFalse(snackbar.isVisible());
                    } catch (Throwable e) {
                        failure.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
    }

    /// Verifies that snackbar hosts display queued snackbars after the current snackbar is dismissed.
    @Test
    void snackbarHostQueuesSnackbars() throws InterruptedException {
        AtomicReference<M3SnackbarHost> hostReference = new AtomicReference<>();
        AtomicReference<M3Snackbar> firstReference = new AtomicReference<>();
        AtomicReference<M3Snackbar> secondReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(180.0),
                () -> {
                    M3SnackbarHost host = new M3SnackbarHost();
                    host.setDisplayDuration(Duration.INDEFINITE);
                    Pane root = new Pane(host);
                    Scene scene = new Scene(root, 320.0, 120.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    M3Snackbar first = new M3Snackbar("First");
                    M3Snackbar second = new M3Snackbar("Second");
                    host.enqueue(first);
                    host.enqueue(second);
                    root.applyCss();

                    assertEquals(first, host.getSnackbar());
                    assertEquals(java.util.List.of(second), host.getQueue());
                    assertTrue(host.getChildren().contains(first));

                    hostReference.set(host);
                    firstReference.set(first);
                    secondReference.set(second);
                    host.dismiss();
                },
                () -> {
                    M3SnackbarHost host = hostReference.get();
                    M3Snackbar first = firstReference.get();
                    M3Snackbar second = secondReference.get();

                    assertEquals(second, host.getSnackbar());
                    assertTrue(host.isShowing());
                    assertTrue(host.getQueue().isEmpty());
                    assertFalse(host.getChildren().contains(first));
                    assertTrue(host.getChildren().contains(second));
                    assertFalse(first.isVisible());
                }
        );
    }

    /// Verifies that snackbar hosts clear pending snackbars without dismissing the current snackbar.
    @Test
    void snackbarHostClearsQueuedSnackbars() {
        runOnFxThread(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            host.setDisplayDuration(Duration.INDEFINITE);
            M3Snackbar first = new M3Snackbar("First");
            M3Snackbar second = new M3Snackbar("Second");
            M3Snackbar third = new M3Snackbar("Third");

            host.enqueue(first);
            host.enqueue(second);
            host.enqueue(third);

            assertEquals(first, host.getSnackbar());
            assertEquals(java.util.List.of(second, third), host.getQueue());
            assertThrows(UnsupportedOperationException.class, () -> host.getQueue().add(new M3Snackbar("Fourth")));

            host.clearQueue();

            assertEquals(first, host.getSnackbar());
            assertTrue(host.isShowing());
            assertTrue(host.getQueue().isEmpty());
        });
    }

    /// Verifies that snackbar hosts reset a snackbar when it is replaced immediately.
    @Test
    void snackbarHostResetsReplacedSnackbar() {
        runOnFxThread(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            host.setDisplayDuration(Duration.INDEFINITE);
            M3Snackbar first = new M3Snackbar("First");
            M3Snackbar second = new M3Snackbar("Second");

            host.show(first);
            first.setOpacity(0.25);
            first.setTranslateY(8.0);
            host.show(second);

            assertEquals(second, host.getSnackbar());
            assertFalse(host.getChildren().contains(first));
            assertTrue(host.getChildren().contains(second));
            assertFalse(first.isVisible());
            assertFalse(first.isManaged());
            assertEquals(1.0, first.getOpacity(), 0.0001);
            assertEquals(0.0, first.getTranslateY(), 0.0001);
        });
    }

    /// Verifies that snackbar hosts can dismiss the current snackbar and clear the queue in one call.
    @Test
    void snackbarHostDismissAllClearsCurrentAndQueuedSnackbars() throws InterruptedException {
        AtomicReference<M3SnackbarHost> hostReference = new AtomicReference<>();
        AtomicReference<M3Snackbar> firstReference = new AtomicReference<>();
        AtomicReference<M3Snackbar> secondReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(180.0),
                () -> {
                    M3SnackbarHost host = new M3SnackbarHost();
                    host.setDisplayDuration(Duration.INDEFINITE);
                    M3Snackbar first = new M3Snackbar("First");
                    M3Snackbar second = new M3Snackbar("Second");

                    host.enqueue(first);
                    host.enqueue(second);
                    assertEquals(first, host.getSnackbar());
                    assertEquals(java.util.List.of(second), host.getQueue());

                    hostReference.set(host);
                    firstReference.set(first);
                    secondReference.set(second);
                    host.dismissAll();
                },
                () -> {
                    M3SnackbarHost host = hostReference.get();
                    M3Snackbar first = firstReference.get();
                    M3Snackbar second = secondReference.get();

                    assertNull(host.getSnackbar());
                    assertFalse(host.isShowing());
                    assertTrue(host.getQueue().isEmpty());
                    assertFalse(host.getChildren().contains(first));
                    assertFalse(host.getChildren().contains(second));
                    assertFalse(first.isVisible());
                }
        );
    }

    /// Verifies that dialog pane component token properties are styleable from CSS.
    @Test
    void dialogPaneTokensAreStyleable() {
        M3DialogPane dialogPane = new M3DialogPane();
        dialogPane.setStyle("-m3-container-shape: 20px; -m3-content-padding: 28px;");

        applyCss(dialogPane);

        assertEquals(20.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(28.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getTop(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getRight(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getBottom(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getLeft(), 0.0001);
    }

    /// Verifies that dialog pane action buttons use m3fx button controls.
    @Test
    void dialogPaneCreatesMaterialActionButtons() {
        M3DialogPane dialogPane = new M3DialogPane();
        dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        applyCss(dialogPane);

        Node cancelNode = dialogPane.lookupButton(ButtonType.CANCEL);
        Node okNode = dialogPane.lookupButton(ButtonType.OK);
        M3Button cancelButton = assertInstanceOf(M3Button.class, cancelNode);
        M3Button okButton = assertInstanceOf(M3Button.class, okNode);
        assertEquals(M3ButtonVariant.TEXT, cancelButton.getVariant());
        assertEquals(M3ButtonVariant.TEXT, okButton.getVariant());
        assertTrue(cancelButton.getStyleClass().contains(M3DialogPane.BUTTON_STYLE_CLASS));
        assertTrue(okButton.getStyleClass().contains(M3DialogPane.BUTTON_STYLE_CLASS));
        assertEquals(ButtonBar.ButtonData.CANCEL_CLOSE, ButtonBar.getButtonData(cancelButton));
        assertEquals(ButtonBar.ButtonData.OK_DONE, ButtonBar.getButtonData(okButton));
        assertTrue(cancelButton.isCancelButton());
        assertTrue(okButton.isDefaultButton());
        assertFalse(okButton.disableProperty().isBound());

        okButton.setDisable(true);

        assertTrue(okButton.isDisabled());
    }

    /// Verifies that dialog pane subnodes keep Material typography and colors.
    @Test
    void dialogPaneSubnodesUseMaterialStyles() {
        M3DialogPane dialogPane = new M3DialogPane();
        dialogPane.setHeaderText("Dialog title");
        dialogPane.setContentText("Dialog body");
        dialogPane.getButtonTypes().setAll(ButtonType.OK);
        Pane root = new Pane(dialogPane);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        root.applyCss();

        assertRegionFill(dialogPane, Color.rgb(19, 20, 21));
        assertInstanceOf(ButtonBar.class, dialogPane.lookup("." + M3DialogPane.BUTTON_BAR_STYLE_CLASS));
        assertEquals(Color.rgb(40, 41, 42), ((Labeled) dialogPane.lookup(".content")).getTextFill());
    }

    /// Verifies that Material dialogs install a Material dialog pane and stylesheet.
    @Test
    void dialogInstallsMaterialPaneAndStylesheet() {
        runOnFxThread(() -> {
            M3Dialog<ButtonType> dialog = new M3Dialog<>(
                    "Title",
                    "Header",
                    "Body",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );
            M3DialogPane pane = dialog.getM3DialogPane();

            assertEquals("Title", dialog.getTitle());
            assertEquals(pane, dialog.getDialogPane());
            assertEquals("Header", pane.getHeaderText());
            assertEquals("Body", pane.getContentText());
            assertEquals(java.util.List.of(ButtonType.CANCEL, ButtonType.OK), pane.getButtonTypes());
            assertTrue(pane.getStyleClass().contains(M3DialogPane.STYLE_CLASS));
            assertTrue(pane.getStylesheets().contains(M3ThemeManager.stylesheetUrl()));

            applyCss(pane);

            assertInstanceOf(M3Button.class, pane.lookupButton(ButtonType.OK));
        });
    }

    /// Verifies that Material dialogs can apply and clear inline theme declarations.
    @Test
    void dialogAppliesAndClearsTheme() {
        runOnFxThread(() -> {
            M3Dialog<Void> dialog = new M3Dialog<>();
            M3DialogPane pane = dialog.getM3DialogPane();
            M3Theme theme = M3Theme.defaultTheme();

            pane.setStyle("-fx-opacity: 0.9;");
            dialog.setTheme(theme);

            assertEquals(theme, dialog.getTheme());
            assertTrue(pane.getStyle().contains("-fx-opacity: 0.9;"));
            assertTrue(pane.getStyle().contains("-m3-color-primary"));

            dialog.setTheme(null);

            assertNull(dialog.getTheme());
            assertEquals("-fx-opacity: 0.9;", pane.getStyle());
        });
    }

    /// Verifies that Material dialog pane access rejects a replaced plain pane.
    @Test
    void dialogRejectsReplacedPlainPane() {
        runOnFxThread(() -> {
            M3Dialog<Void> dialog = new M3Dialog<>();

            dialog.setDialogPane(new DialogPane());

            assertThrows(IllegalStateException.class, dialog::getM3DialogPane);
        });
    }

    /// Verifies that text field component token properties are styleable from CSS.
    @Test
    void textFieldTokensAreStyleable() {
        M3TextField textField = M3TextField.withVariant("Content", M3TextInputVariant.OUTLINED);
        textField.setStyle("-m3-container-height: 64px; -m3-container-shape: 12px; -m3-horizontal-padding: 22px;");

        applyCss(textField);

        assertEquals("Content", textField.getText());
        assertEquals(M3TextInputVariant.OUTLINED, textField.getVariant());
        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(12.0, textField.getContainerShape(), 0.0001);
        assertEquals(22.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, textField.getPrefHeight(), 0.0001);
        assertEquals(22.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, textField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that password field component token properties are styleable from CSS.
    @Test
    void passwordFieldTokensAreStyleable() {
        M3PasswordField passwordField = M3PasswordField.withVariant("secret", M3TextInputVariant.OUTLINED);
        passwordField.setStyle("-m3-container-height: 60px; -m3-container-shape: 10px; -m3-horizontal-padding: 20px;");

        applyCss(passwordField);

        assertEquals("secret", passwordField.getText());
        assertEquals(M3TextInputVariant.OUTLINED, passwordField.getVariant());
        assertEquals(60.0, passwordField.getContainerHeight(), 0.0001);
        assertEquals(10.0, passwordField.getContainerShape(), 0.0001);
        assertEquals(20.0, passwordField.getHorizontalPadding(), 0.0001);
        assertEquals(60.0, passwordField.getPrefHeight(), 0.0001);
        assertEquals(20.0, passwordField.getPadding().getLeft(), 0.0001);
        assertEquals(20.0, passwordField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that text area component token properties are styleable from CSS.
    @Test
    void textAreaTokensAreStyleable() {
        M3TextArea textArea = M3TextArea.withVariant("Notes", M3TextInputVariant.OUTLINED);
        textArea.setStyle(
                "-m3-container-height: 140px; "
                        + "-m3-container-shape: 12px; "
                        + "-m3-horizontal-padding: 22px; "
                        + "-m3-vertical-padding: 18px;"
        );

        applyCss(textArea);

        assertEquals("Notes", textArea.getText());
        assertEquals(M3TextInputVariant.OUTLINED, textArea.getVariant());
        assertEquals(140.0, textArea.getContainerHeight(), 0.0001);
        assertEquals(12.0, textArea.getContainerShape(), 0.0001);
        assertEquals(22.0, textArea.getHorizontalPadding(), 0.0001);
        assertEquals(18.0, textArea.getVerticalPadding(), 0.0001);
        assertEquals(140.0, textArea.getPrefHeight(), 0.0001);
        assertEquals(22.0, textArea.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, textArea.getPadding().getRight(), 0.0001);
        assertEquals(18.0, textArea.getPadding().getTop(), 0.0001);
        assertEquals(18.0, textArea.getPadding().getBottom(), 0.0001);
    }

    /// Verifies that m3fx tooltips expose style and timing defaults.
    @Test
    void tooltipUsesMaterialDefaults() {
        M3Tooltip tooltip = new M3Tooltip("Details");

        assertTrue(tooltip.getStyleClass().contains(M3Tooltip.STYLE_CLASS));
        assertTrue(tooltip.isWrapText());
        assertEquals(Duration.millis(500.0), tooltip.getShowDelay());
        assertEquals(Duration.millis(0.0), tooltip.getHideDelay());
        assertEquals(Duration.seconds(5.0), tooltip.getShowDuration());
    }

    /// Verifies that tooltips provide Material Design 3 install helpers.
    @Test
    void tooltipInstallsOnNodes() {
        Label target = new Label("Target");
        M3Tooltip tooltip = M3Tooltip.install(target, "Installed");

        assertEquals("Installed", tooltip.getText());
        assertTrue(tooltip.getStyleClass().contains(M3Tooltip.STYLE_CLASS));

        M3Tooltip.uninstall(target, tooltip);
    }

    /// Verifies that avatars swap between text and graphic content.
    @Test
    void avatarOwnsTextAndGraphicContent() {
        M3Avatar avatar = new M3Avatar("AB");
        Label graphic = new Label("G");

        assertEquals("AB", avatar.getText());
        assertEquals(1, avatar.getChildren().size());

        avatar.setGraphic(graphic);

        assertEquals(graphic, avatar.getGraphic());
        assertEquals(graphic, avatar.getChildren().get(0));

        avatar.setGraphic(null);

        assertNull(avatar.getGraphic());
        assertEquals(1, avatar.getChildren().size());
    }

    /// Verifies that avatar component token metrics apply through the active theme.
    @Test
    void avatarAppliesTokenMetrics() {
        M3Avatar avatar = new M3Avatar("A");

        applyCss(avatar);

        assertEquals(40.0, avatar.getPrefWidth(), 0.0001);
        assertEquals(40.0, avatar.getPrefHeight(), 0.0001);
    }

    /// Verifies that avatar variants update style classes.
    @Test
    void avatarVariantUpdatesStyleClasses() {
        M3Avatar avatar = M3Avatar.withVariant("A", M3AvatarVariant.SECONDARY);

        assertEquals(M3AvatarVariant.SECONDARY, avatar.getVariant());
        assertTrue(avatar.getStyleClass().contains(M3AvatarVariant.SECONDARY.getStyleClass()));

        avatar.setVariant(M3AvatarVariant.TERTIARY);

        assertEquals(M3AvatarVariant.TERTIARY, avatar.getVariant());
        assertTrue(avatar.getStyleClass().contains(M3AvatarVariant.TERTIARY.getStyleClass()));
        assertFalse(avatar.getStyleClass().contains(M3AvatarVariant.SECONDARY.getStyleClass()));
    }

    /// Verifies that icon size and color variants update style classes.
    @Test
    void iconSizeAndVariantUpdateStyleClasses() {
        M3Icon icon = new M3Icon("A");

        assertEquals(M3IconSize.MEDIUM, icon.getSize());
        assertEquals(M3IconVariant.ON_SURFACE_VARIANT, icon.getVariant());
        assertTrue(icon.getStyleClass().contains(M3Icon.STYLE_CLASS));
        assertTrue(icon.getStyleClass().contains(M3IconSize.MEDIUM.getStyleClass()));
        assertTrue(icon.getStyleClass().contains(M3IconVariant.ON_SURFACE_VARIANT.getStyleClass()));

        icon.setSize(M3IconSize.LARGE);
        icon.setVariant(M3IconVariant.PRIMARY);

        assertEquals(M3IconSize.LARGE, icon.getSize());
        assertEquals(M3IconVariant.PRIMARY, icon.getVariant());
        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertTrue(icon.getStyleClass().contains(M3IconSize.LARGE.getStyleClass()));
        assertTrue(icon.getStyleClass().contains(M3IconVariant.PRIMARY.getStyleClass()));
        assertFalse(icon.getStyleClass().contains(M3IconSize.MEDIUM.getStyleClass()));
        assertFalse(icon.getStyleClass().contains(M3IconVariant.ON_SURFACE_VARIANT.getStyleClass()));
    }

    /// Verifies that icon font tokens apply through CSS.
    @Test
    void iconTokensAreStyleable() {
        M3Icon icon = new M3Icon("A", M3IconSize.EXTRA_LARGE, M3IconVariant.TERTIARY);
        icon.setStyle("-m3-icon-size: 28px; -m3-icon-font-weight: 700;");

        applyCss(icon);

        assertEquals(28.0, icon.getIconSize(), 0.0001);
        assertEquals(700.0, icon.getIconFontWeight(), 0.0001);
        assertEquals(28.0, icon.getFont().getSize(), 0.0001);
        assertEquals(28.0, icon.getPrefWidth(), 0.0001);
        assertEquals(28.0, icon.getPrefHeight(), 0.0001);
    }

    /// Verifies that icon size roles provide default size tokens through user-agent CSS.
    @Test
    void iconSizeRoleAppliesDefaultCssToken() {
        M3Icon icon = new M3Icon("A", M3IconSize.LARGE, M3IconVariant.PRIMARY);

        applyCss(icon);

        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(32.0, icon.getFont().getSize(), 0.0001);
    }

    /// Verifies that icon button factories create configured M3FX icon graphics.
    @Test
    void iconButtonFactoryCreatesIconGraphic() {
        M3IconButton button = M3IconButton.withIcon("A", M3IconSize.SMALL, M3IconVariant.ERROR);
        M3Icon icon = assertInstanceOf(M3Icon.class, button.getGraphic());

        assertEquals(M3IconSize.SMALL, icon.getSize());
        assertEquals(M3IconVariant.ERROR, icon.getVariant());
    }

    /// Verifies that toggle icon button variants and selected states update style classes.
    @Test
    void iconToggleButtonVariantAndSelectionUpdateState() {
        M3IconToggleButton button = M3IconToggleButton.withIcon(
                "A",
                M3IconToggleButtonVariant.TONAL,
                true
        );

        assertEquals(M3IconToggleButtonVariant.TONAL, button.getVariant());
        assertTrue(button.isSelected());
        assertTrue(button.getStyleClass().contains(M3IconToggleButton.STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3IconToggleButtonVariant.TONAL.getStyleClass()));

        button.setVariant(M3IconToggleButtonVariant.OUTLINED);
        button.setSelected(false);

        assertEquals(M3IconToggleButtonVariant.OUTLINED, button.getVariant());
        assertFalse(button.isSelected());
        assertTrue(button.getStyleClass().contains(M3IconToggleButtonVariant.OUTLINED.getStyleClass()));
        assertFalse(button.getStyleClass().contains(M3IconToggleButtonVariant.TONAL.getStyleClass()));
        assertFalse(button.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")));
    }

    /// Verifies that toggle icon buttons create the animated toggle icon button skin.
    @Test
    void iconToggleButtonCreatesAnimatedSkin() {
        M3IconToggleButton button = new M3IconToggleButton("A");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3IconToggleButtonSkin.class, button.getSkin());
    }

    /// Verifies that toggle icon buttons toggle selected state and fire action events.
    @Test
    void iconToggleButtonTogglesAndFiresAction() {
        M3IconToggleButton button = new M3IconToggleButton("A");
        AtomicInteger actionCount = new AtomicInteger();
        button.setOnAction(event -> actionCount.incrementAndGet());

        button.fire();

        assertTrue(button.isSelected());
        assertEquals(1, actionCount.get());

        button.fire();

        assertFalse(button.isSelected());
        assertEquals(2, actionCount.get());
    }

    /// Verifies that toggle icon button component token properties are styleable from CSS.
    @Test
    void iconToggleButtonTokensAreStyleable() {
        M3IconToggleButton button = new M3IconToggleButton("A");
        button.setStyle("-m3-container-height: 48px; -m3-container-shape: 12px;");

        applyCss(button);

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(12.0, button.getContainerShape(), 0.0001);
        assertEquals(48.0, button.getPrefWidth(), 0.0001);
        assertEquals(48.0, button.getPrefHeight(), 0.0001);
    }

    /// Verifies that toggle icon button groups keep selection mutually exclusive.
    @Test
    void iconToggleButtonGroupKeepsSelectionExclusive() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(first, second);

        first.setSelected(true);

        assertEquals(M3IconToggleButtonSelectionMode.SINGLE, group.getSelectionMode());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertEquals(0, group.getSelectedIndex());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        group.selectIndex(1);

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertEquals(1, group.getSelectedIndex());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertThrows(UnsupportedOperationException.class, () -> group.getSelectedButtons().add(first));

        group.clearSelection();

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertEquals(-1, group.getSelectedIndex());
        assertFalse(first.isSelected());
        assertFalse(second.isSelected());
    }

    /// Verifies that toggle icon button groups can use multiple selected button behavior.
    @Test
    void iconToggleButtonGroupCanUseMultipleSelection() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButton third = new M3IconToggleButton("C");
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(first, second, third);

        group.setSelectionMode(M3IconToggleButtonSelectionMode.MULTIPLE);
        first.fire();
        third.fire();

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertTrue(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());

        first.fire();

        assertFalse(first.isSelected());
        assertTrue(third.isSelected());
        assertEquals(third, group.getSelectedButton());
        assertEquals(java.util.List.of(third), group.getSelectedButtons());
    }

    /// Verifies that toggle icon button groups collapse multiple selection when switched to single selection.
    @Test
    void iconToggleButtonGroupCollapsesMultipleSelectionWhenModeChanges() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButton third = new M3IconToggleButton("C");
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(first, second, third);

        group.setSelectionMode(M3IconToggleButtonSelectionMode.MULTIPLE);
        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());

        group.setSelectionMode(M3IconToggleButtonSelectionMode.SINGLE);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertFalse(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
    }

    /// Verifies that toggle icon button groups can require a selected button.
    @Test
    void iconToggleButtonGroupCanRequireSelection() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(first, second);

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        group.clearSelection();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        first.fire();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertTrue(second.isSelected());
    }

    /// Verifies that toggle icon button groups clean selection when children are removed.
    @Test
    void iconToggleButtonGroupUpdatesSelectionWhenChildrenChange() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(first, second);

        second.setSelected(true);
        group.getItems().remove(second);

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertFalse(second.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());
    }

    /// Verifies that text typography roles update style classes.
    @Test
    void textRoleUpdatesStyleClasses() {
        M3Text text = new M3Text("Title");

        assertEquals(M3TextRole.BODY_LARGE, text.getRole());
        assertTrue(text.getStyleClass().contains(M3TextRole.BODY_LARGE.getStyleClass()));

        text.setRole(M3TextRole.TITLE_LARGE);

        assertEquals(M3TextRole.TITLE_LARGE, text.getRole());
        assertTrue(text.getStyleClass().contains(M3TextRole.TITLE_LARGE.getStyleClass()));
        assertFalse(text.getStyleClass().contains(M3TextRole.BODY_LARGE.getStyleClass()));

        text.setRole(M3TextRole.LABEL_SMALL);

        assertEquals(M3TextRole.LABEL_SMALL, text.getRole());
        assertTrue(text.getStyleClass().contains(M3TextRole.LABEL_SMALL.getStyleClass()));
        assertFalse(text.getStyleClass().contains(M3TextRole.TITLE_LARGE.getStyleClass()));
    }

    /// Verifies that text typography roles read font size tokens from the active theme.
    @Test
    void textRoleUsesTypographyTokens() {
        M3Text text = new M3Text("Display", M3TextRole.DISPLAY_LARGE);

        applyCss(text);

        assertEquals(57.0, text.getFont().getSize(), 0.0001);
        assertEquals(64.0, text.getTypographyLineHeight(), 0.0001);
        assertEquals(7.0, text.getLineSpacing(), 0.0001);
    }

    /// Verifies that typography font weight tokens support unitless CSS values.
    @Test
    void textFontWeightTokenAcceptsUnitlessCssValue() {
        M3Text text = new M3Text("Title", M3TextRole.TITLE_MEDIUM);

        applyCss(text);

        assertEquals(500.0, text.getTypographyFontWeight(), 0.0001);
    }

    /// Verifies that surfaces expose variant, elevation, and metric tokens.
    @Test
    void surfaceVariantElevationAndMetricsAreStyleable() {
        M3Surface surface = new M3Surface(new Label("Surface"));
        surface.setVariant(M3SurfaceVariant.PRIMARY_CONTAINER);
        surface.setElevation(M3SurfaceElevation.LEVEL3);
        surface.setStyle("-m3-container-shape: 20px; -m3-content-padding: 18px;");

        applyCss(surface);

        assertEquals(M3SurfaceVariant.PRIMARY_CONTAINER, surface.getVariant());
        assertEquals(M3SurfaceElevation.LEVEL3, surface.getElevation());
        assertTrue(surface.getStyleClass().contains(M3SurfaceVariant.PRIMARY_CONTAINER.getStyleClass()));
        assertTrue(surface.getStyleClass().contains(M3SurfaceElevation.LEVEL3.getStyleClass()));
        assertEquals(20.0, surface.getContainerShape(), 0.0001);
        assertEquals(18.0, surface.getContentPadding(), 0.0001);
        assertEquals(18.0, surface.getPadding().getTop(), 0.0001);
        assertEquals(1, surface.getChildren().size());
    }

    /// Verifies that menu tokens apply to menu surfaces and items.
    @Test
    void menuAppliesItemMetrics() {
        M3MenuItem open = new M3MenuItem("Open");
        M3Menu menu = new M3Menu(open);
        Pane root = new Pane(menu);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(8.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(48.0, open.getOneLineHeight(), 0.0001);
        assertEquals(4.0, open.getContainerShape(), 0.0001);
        assertEquals(12.0, open.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, open.getContentSpacing(), 0.0001);
    }

    /// Verifies that menu item action events bubble through the menu.
    @Test
    void menuItemActionsBubbleThroughMenu() {
        M3MenuItem open = new M3MenuItem("Open");
        M3Menu menu = new M3Menu(open);
        AtomicInteger actions = new AtomicInteger();
        menu.addEventHandler(javafx.event.ActionEvent.ACTION, event -> actions.incrementAndGet());

        open.fire();

        assertEquals(1, actions.get());
    }

    /// Verifies that menu item constructors can install slots and actions.
    @Test
    void menuItemConstructorInstallsSlotsAndAction() {
        Label leading = new Label("L");
        Label trailing = new Label("T");
        AtomicInteger actions = new AtomicInteger();
        M3MenuItem item = new M3MenuItem(
                "Open",
                leading,
                trailing,
                event -> actions.incrementAndGet()
        );

        item.fire();

        assertEquals("Open", item.getHeadlineText());
        assertEquals(leading, item.getLeading());
        assertEquals(trailing, item.getTrailing());
        assertEquals(1, actions.get());
    }

    /// Verifies that action menus do not auto-select items by default.
    @Test
    void menuDefaultSelectionModeDoesNotSelectOnAction() {
        M3MenuItem open = new M3MenuItem("Open");
        M3Menu menu = new M3Menu(open);

        open.fire();

        assertEquals(M3MenuSelectionMode.NONE, menu.getSelectionMode());
        assertFalse(open.isSelected());
        assertNull(menu.getSelectedItem());
        assertTrue(menu.getSelectedItems().isEmpty());
    }

    /// Verifies that menus track manual item selections in child order.
    @Test
    void menuTracksManualSelections() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3MenuItem third = new M3MenuItem("Third");
        M3Menu menu = new M3Menu(first, second, third);

        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), menu.getSelectedItems());
        assertEquals(first, menu.getSelectedItem());
        assertEquals(0, menu.getSelectedIndex());

        first.setSelected(false);

        assertEquals(java.util.List.of(third), menu.getSelectedItems());
        assertEquals(third, menu.getSelectedItem());
        assertEquals(2, menu.getSelectedIndex());
    }

    /// Verifies that menus can enforce single selected item behavior.
    @Test
    void menuCanUseSingleSelection() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        first.setSelected(true);
        second.setSelected(true);

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, menu.getSelectedItem());
        assertEquals(java.util.List.of(second), menu.getSelectedItems());

        menu.selectIndex(0);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertEquals(first, menu.getSelectedItem());
        assertEquals(0, menu.getSelectedIndex());
    }

    /// Verifies that menus can use multiple selected item behavior.
    @Test
    void menuCanUseMultipleSelection() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        menu.setSelectionMode(M3MenuSelectionMode.MULTIPLE);
        first.fire();
        second.fire();

        assertTrue(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(java.util.List.of(first, second), menu.getSelectedItems());

        first.fire();

        assertFalse(first.isSelected());
        assertEquals(java.util.List.of(second), menu.getSelectedItems());
    }

    /// Verifies that menus can require a selected item.
    @Test
    void menuCanRequireSelection() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(first, menu.getSelectedItem());
        assertTrue(first.isSelected());

        menu.clearSelection();

        assertEquals(first, menu.getSelectedItem());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, menu.getSelectedItem());
        assertTrue(second.isSelected());
    }

    /// Verifies that menus update selection when items are removed.
    @Test
    void menuUpdatesSelectionWhenChildrenChange() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        second.setSelected(true);
        menu.getItems().remove(second);

        assertTrue(menu.getSelectedItems().isEmpty());
        assertNull(menu.getSelectedItem());
        assertFalse(second.isSelected());

        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(first, menu.getSelectedItem());
        assertTrue(first.isSelected());
    }

    /// Verifies that menu buttons expose their menu and still fire action events.
    @Test
    void menuButtonOwnsMenuItemsAndFiresActions() {
        M3MenuItem item = new M3MenuItem("Open");
        M3MenuButton menuButton = new M3MenuButton("More", item);
        AtomicInteger actions = new AtomicInteger();
        menuButton.setOnAction(event -> actions.incrementAndGet());

        menuButton.fire();

        assertEquals(item, menuButton.getItems().get(0));
        assertEquals(1, actions.get());
        assertFalse(menuButton.isShowing());
    }

    /// Verifies that menu buttons expose menu selection APIs.
    @Test
    void menuButtonDelegatesMenuSelectionApis() {
        M3MenuButton menuButton = new M3MenuButton("More");
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        menuButton.getItems().addAll(first, second);

        menuButton.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menuButton.setAllowEmptySelection(false);
        menuButton.selectIndex(1);

        assertEquals(M3MenuSelectionMode.SINGLE, menuButton.getSelectionMode());
        assertFalse(menuButton.isAllowEmptySelection());
        assertEquals(second, menuButton.getSelectedItem());
        assertEquals(java.util.List.of(second), menuButton.getSelectedItems());
        assertEquals(1, menuButton.getSelectedIndex());

        menuButton.clearSelection();

        assertEquals(second, menuButton.getSelectedItem());
        assertTrue(second.isSelected());
    }

    /// Verifies that search bars delegate text and action APIs to their embedded editor.
    @Test
    void searchBarDelegatesTextAndActions() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        AtomicInteger actions = new AtomicInteger();
        searchBar.setOnAction(event -> actions.incrementAndGet());

        searchBar.setText("M3FX");
        searchBar.fire();

        assertEquals("M3FX", searchBar.getText());
        assertEquals("M3FX", searchBar.getEditor().getText());
        assertEquals("Search", searchBar.getPromptText());
        assertEquals(1, actions.get());
    }

    /// Verifies that search bars expose active state and clear actions.
    @Test
    void searchBarTracksActiveStateAndClearsText() {
        M3SearchBar searchBar = new M3SearchBar("Search");

        searchBar.setText("M3FX");
        searchBar.activate();

        assertTrue(searchBar.isActive());
        assertTrue(searchBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")));

        searchBar.clear();
        searchBar.deactivate();

        assertEquals("", searchBar.getText());
        assertFalse(searchBar.isActive());
        assertFalse(searchBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")));
    }

    /// Verifies that search component token metrics apply through the active theme.
    @Test
    void searchComponentsApplyTokenMetrics() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        M3SearchView searchView = new M3SearchView("Search");
        M3ListItem result = new M3ListItem("Result");
        searchView.getResults().add(result);
        Pane root = new Pane(searchBar, searchView);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(56.0, searchBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getRight(), 0.0001);
        assertEquals(12.0, searchBar.getSpacing(), 0.0001);
        assertEquals(56.0, result.getOneLineHeight(), 0.0001);
    }

    /// Verifies that search views own a search bar and mutable result list.
    @Test
    void searchViewOwnsSearchBarAndResults() {
        M3ListItem result = new M3ListItem("Result");
        M3SearchView searchView = new M3SearchView("Find", result);
        Label leading = new Label("S");
        M3IconButton trailing = M3IconButton.withIcon("C");
        AtomicInteger actions = new AtomicInteger();

        searchView.setLeading(leading);
        searchView.getTrailingActions().add(trailing);
        searchView.setOnAction(event -> actions.incrementAndGet());
        searchView.setText("button");
        searchView.fire();

        assertEquals("button", searchView.getSearchBar().getText());
        assertEquals("button", searchView.getEditor().getText());
        assertEquals("Find", searchView.getPromptText());
        assertEquals(leading, searchView.getLeading());
        assertEquals(leading, searchView.getSearchBar().getLeading());
        assertEquals(leading, searchView.leadingProperty().get());
        assertEquals(trailing, searchView.getTrailingActions().get(0));
        assertEquals(trailing, searchView.getSearchBar().getTrailingActions().get(0));
        assertEquals(result, searchView.getResults().get(0));
        assertEquals(1, actions.get());

        searchView.clear();

        assertEquals("", searchView.getText());
        assertEquals("", searchView.getEditor().getText());
    }

    /// Verifies that search views use active state to show or hide results.
    @Test
    void searchViewActiveStateControlsResultsVisibility() {
        M3SearchView searchView = new M3SearchView("Find");
        M3ListItem result = new M3ListItem("Result");
        searchView.getResults().add(result);
        applyCss(searchView);

        assertTrue(searchView.isActive());
        assertTrue(searchView.getSearchBar().isActive());

        searchView.deactivate();

        Node results = searchView.lookup("." + M3SearchView.RESULTS_STYLE_CLASS);
        assertFalse(searchView.isActive());
        assertFalse(results.isVisible());
        assertFalse(results.isManaged());

        searchView.activate();

        assertTrue(searchView.isActive());
        assertTrue(results.isVisible());
        assertTrue(results.isManaged());
    }

    /// Verifies that sheet controls own content, actions, and variants.
    @Test
    void sheetControlsOwnContentActionsAndVariants() {
        Label sideContent = new Label("Side content");
        M3IconButton closeAction = new M3IconButton();
        M3SideSheet sideSheet = new M3SideSheet("Details", sideContent, closeAction);
        sideSheet.setVariant(M3SheetVariant.MODAL);

        Label bottomContent = new Label("Bottom content");
        M3IconButton bottomAction = new M3IconButton();
        M3BottomSheet bottomSheet = new M3BottomSheet("Queue", bottomContent, bottomAction);
        bottomSheet.setDragHandleVisible(false);

        assertEquals("Details", sideSheet.getHeadline());
        assertEquals(sideContent, sideSheet.getContent());
        assertEquals(closeAction, sideSheet.getActions().get(0));
        assertEquals(M3SheetVariant.MODAL, sideSheet.getVariant());
        assertTrue(sideSheet.getStyleClass().contains(M3SheetVariant.MODAL.getStyleClass()));
        assertEquals("Queue", bottomSheet.getHeadline());
        assertEquals(bottomContent, bottomSheet.getContent());
        assertEquals(bottomAction, bottomSheet.getActions().get(0));
        assertFalse(bottomSheet.isDragHandleVisible());
    }

    /// Verifies that sheet component token metrics apply through the active theme.
    @Test
    void sheetComponentsApplyTokenMetrics() {
        M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Content"));
        M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Content"));
        Pane root = new Pane(sideSheet, bottomSheet);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(360.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(320.0, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(
                24.0,
                lookupRegion(sideSheet, "." + M3SideSheet.CONTENT_STYLE_CLASS).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                lookupRegion(bottomSheet, "." + M3BottomSheet.CONTENT_STYLE_CLASS).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                32.0,
                lookupRegion(bottomSheet, "." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS).getPrefWidth(),
                0.0001
        );
    }

    /// Verifies that scrims fire action events for programmatic and mouse activation.
    @Test
    void scrimFiresActionEvents() {
        M3Scrim scrim = new M3Scrim();
        AtomicInteger actions = new AtomicInteger();
        scrim.setOnAction(event -> actions.incrementAndGet());

        scrim.fire();
        scrim.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        scrim.setDisable(true);
        scrim.fire();

        assertEquals(2, actions.get());
    }

    /// Verifies that scrim opacity applies through the active theme.
    @Test
    void scrimAppliesTokenOpacity() {
        M3Scrim scrim = new M3Scrim();

        applyCss(scrim);

        assertEquals(0.32, scrim.getOpacity(), 0.0001);
    }

    /// Verifies that focused text input states keep Material field colors.
    @Test
    void textInputStateStylesPreserveVariantColors() {
        M3TextField filledField = new M3TextField();
        M3PasswordField outlinedField = new M3PasswordField();
        outlinedField.setVariant(M3TextInputVariant.OUTLINED);
        Pane root = new Pane(filledField, outlinedField);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        filledField.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        outlinedField.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();

        assertRegionFill(filledField, Color.rgb(37, 38, 39));
        assertBorderBottomColor(filledField, Color.rgb(1, 2, 3));
        assertEquals(2.0, filledField.getBorder().getStrokes().get(0).getWidths().getBottom(), 0.0001);
        assertRegionFill(outlinedField, Color.TRANSPARENT);
        assertBorderColor(outlinedField, Color.rgb(1, 2, 3));
        assertEquals(2.0, outlinedField.getBorder().getStrokes().get(0).getWidths().getTop(), 0.0001);
    }

    /// Verifies that chip component token properties are styleable from CSS.
    @Test
    void chipTokensAreStyleable() {
        M3Chip chip = new M3Chip("Chip");
        chip.setStyle("-m3-container-height: 36px; -m3-container-shape: 16px; -m3-horizontal-padding: 14px;");

        applyCss(chip);

        assertEquals(36.0, chip.getContainerHeight(), 0.0001);
        assertEquals(16.0, chip.getContainerShape(), 0.0001);
        assertEquals(14.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(36.0, chip.getPrefHeight(), 0.0001);
        assertEquals(14.0, chip.getPadding().getLeft(), 0.0001);
        assertEquals(14.0, chip.getPadding().getRight(), 0.0001);
    }

    /// Verifies that m3fx chips create the Material Design 3 skin.
    @Test
    void chipCreatesMaterialSkin() {
        M3Chip chip = new M3Chip("Filter");

        applyCss(chip);

        assertInstanceOf(M3ChipSkin.class, chip.getSkin());
    }

    /// Verifies that chips can be created with graphic content.
    @Test
    void chipSupportsGraphicContent() {
        M3Icon icon = new M3Icon("A");
        M3Chip chip = M3Chip.withVariant("Assist", icon, M3ChipVariant.INPUT, true);

        assertEquals("Assist", chip.getText());
        assertEquals(icon, chip.getGraphic());
        assertEquals(M3ChipVariant.INPUT, chip.getVariant());
        assertTrue(chip.isSelected());
    }

    /// Verifies that chip interaction states keep Material colors.
    @Test
    void chipStateStylesPreserveVariantColors() {
        M3Chip assistChip = new M3Chip("Assist");
        M3Chip filterChip = new M3Chip("Filter");
        filterChip.setVariant(M3ChipVariant.FILTER);
        filterChip.setSelected(true);
        Pane root = new Pane(assistChip, filterChip);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        applyInteractivePseudoClasses(assistChip);
        applyInteractivePseudoClasses(filterChip);
        root.applyCss();

        assertLabeledColors(assistChip, Color.TRANSPARENT, Color.rgb(34, 35, 36));
        assertBorderColor(assistChip, Color.rgb(13, 14, 15));
        assertLabeledColors(filterChip, Color.rgb(7, 8, 9), Color.rgb(10, 11, 12));
        assertBorderColor(filterChip, Color.rgb(7, 8, 9));
    }

    /// Verifies that chip groups track multiple selected chips in child order.
    @Test
    void chipGroupTracksMultipleSelections() {
        M3Chip first = new M3Chip("First");
        M3Chip second = new M3Chip("Second");
        M3Chip third = new M3Chip("Third");
        M3ChipGroup group = new M3ChipGroup(first, second, third);

        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), group.getSelectedChips());
        assertEquals(first, group.getSelectedChip());
        assertEquals(0, group.getSelectedIndex());

        first.setSelected(false);

        assertEquals(java.util.List.of(third), group.getSelectedChips());
        assertEquals(third, group.getSelectedChip());
        assertEquals(2, group.getSelectedIndex());

        group.selectIndex(1);

        assertEquals(java.util.List.of(second, third), group.getSelectedChips());
        assertEquals(second, group.getSelectedChip());
        assertEquals(1, group.getSelectedIndex());

        group.clearSelection();

        assertTrue(group.getSelectedChips().isEmpty());
        assertNull(group.getSelectedChip());
        assertEquals(-1, group.getSelectedIndex());
    }

    /// Verifies that chip groups can enforce single selection.
    @Test
    void chipGroupCanUseSingleSelection() {
        M3Chip first = new M3Chip("First");
        M3Chip second = new M3Chip("Second");
        M3ChipGroup group = new M3ChipGroup(first, second);

        group.setSelectionMode(M3ChipSelectionMode.SINGLE);
        first.setSelected(true);
        second.setSelected(true);

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, group.getSelectedChip());
        assertEquals(java.util.List.of(second), group.getSelectedChips());

        group.select(first);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertEquals(first, group.getSelectedChip());
    }

    /// Verifies that chip groups can require a selected chip.
    @Test
    void chipGroupCanRequireSelection() {
        M3Chip first = new M3Chip("First");
        M3Chip second = new M3Chip("Second");
        M3ChipGroup group = new M3ChipGroup(first, second);

        group.setSelectionMode(M3ChipSelectionMode.SINGLE);
        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedChip());
        assertTrue(first.isSelected());

        group.clearSelection();

        assertEquals(first, group.getSelectedChip());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, group.getSelectedChip());
        assertTrue(second.isSelected());
    }

    /// Verifies that chip groups update selection when chips are removed.
    @Test
    void chipGroupUpdatesSelectionWhenChildrenChange() {
        M3Chip first = new M3Chip("First");
        M3Chip second = new M3Chip("Second");
        M3ChipGroup group = new M3ChipGroup(first, second);

        second.setSelected(true);
        group.getItems().remove(second);

        assertTrue(group.getSelectedChips().isEmpty());
        assertNull(group.getSelectedChip());
        assertFalse(second.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedChip());
        assertTrue(first.isSelected());
    }

    /// Verifies that segmented button component token properties are styleable from CSS.
    @Test
    void segmentedButtonTokensAreStyleable() {
        M3SegmentedButton button = M3SegmentedButton.withSelected("Week", true);
        button.setStyle("-m3-container-height: 44px; -m3-container-shape: 12px; -m3-horizontal-padding: 18px;");

        applyCss(button);

        assertTrue(button.isSelected());
        assertEquals(44.0, button.getContainerHeight(), 0.0001);
        assertEquals(12.0, button.getContainerShape(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(44.0, button.getPrefHeight(), 0.0001);
        assertEquals(18.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that segmented button groups keep a single selected segment.
    @Test
    void segmentedButtonGroupKeepsSingleSelection() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second);

        first.setSelected(true);
        second.setSelected(true);

        assertEquals(M3SegmentedButtonSelectionMode.SINGLE, group.getSelectionMode());
        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertEquals(1, group.getSelectedIndex());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());

        group.selectIndex(0);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertEquals(0, group.getSelectedIndex());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        first.fire();

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertEquals(-1, group.getSelectedIndex());
        assertFalse(first.isSelected());
    }

    /// Verifies that segmented button groups can use multiple selected segment behavior.
    @Test
    void segmentedButtonGroupCanUseMultipleSelection() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButton third = new M3SegmentedButton("Third");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second, third);

        group.setSelectionMode(M3SegmentedButtonSelectionMode.MULTIPLE);
        first.fire();
        third.fire();

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertTrue(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());
        assertThrows(UnsupportedOperationException.class, () -> group.getSelectedButtons().add(second));

        first.fire();

        assertFalse(first.isSelected());
        assertTrue(third.isSelected());
        assertEquals(third, group.getSelectedButton());
        assertEquals(java.util.List.of(third), group.getSelectedButtons());
    }

    /// Verifies that segmented button groups collapse multiple selection when switched to single selection.
    @Test
    void segmentedButtonGroupCollapsesMultipleSelectionWhenModeChanges() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButton third = new M3SegmentedButton("Third");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second, third);

        group.setSelectionMode(M3SegmentedButtonSelectionMode.MULTIPLE);
        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());

        group.setSelectionMode(M3SegmentedButtonSelectionMode.SINGLE);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertFalse(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
    }

    /// Verifies that segmented button groups can require a selected segment.
    @Test
    void segmentedButtonGroupCanRequireSelection() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second);

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());

        first.setSelected(true);
        group.clearSelection();

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertFalse(first.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        first.fire();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertTrue(second.isSelected());
    }

    /// Verifies that segmented button groups update selection when children change.
    @Test
    void segmentedButtonGroupUpdatesSelectionWhenChildrenChange() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second);

        second.setSelected(true);
        group.getItems().remove(second);

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertFalse(second.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());
    }

    /// Verifies that m3fx segmented buttons create the Material Design 3 skin.
    @Test
    void segmentedButtonCreatesMaterialSkin() {
        M3SegmentedButton button = new M3SegmentedButton("Month");

        applyCss(button);

        assertInstanceOf(M3SegmentedButtonSkin.class, button.getSkin());
    }

    /// Verifies that selected segmented button states keep Material colors.
    @Test
    void segmentedButtonStateStylesPreserveSelectedColors() {
        M3SegmentedButton day = new M3SegmentedButton("Day");
        M3SegmentedButton week = new M3SegmentedButton("Week");
        M3SegmentedButton month = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(day, week, month);
        Pane root = new Pane(group);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        month.setSelected(true);
        applyInteractivePseudoClasses(month);
        root.applyCss();

        assertLabeledColors(day, Color.TRANSPARENT, Color.rgb(34, 35, 36));
        assertBorderColor(day, Color.rgb(13, 14, 15));
        assertLabeledColors(month, Color.rgb(7, 8, 9), Color.rgb(10, 11, 12));
        assertBorderColor(month, Color.rgb(7, 8, 9));
    }

    /// Verifies that segmented button groups assign segment position style classes.
    @Test
    void segmentedButtonGroupAssignsPositionStyleClasses() {
        M3SegmentedButton first = new M3SegmentedButton("Day");
        M3SegmentedButton second = new M3SegmentedButton("Week");
        M3SegmentedButton third = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second, third);

        assertTrue(group.getStyleClass().contains(M3SegmentedButtonGroup.STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(second.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));

        group.getChildren().remove(second);

        assertFalse(second.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));

        group.getChildren().remove(first);

        assertFalse(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS));
    }

    /// Verifies that segmented button surfaces and state layers follow segment position shapes.
    @Test
    void segmentedButtonGroupUsesPositionSpecificShapes() {
        runOnFxThread(() -> {
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(day, week, month);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 320.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            month.setSelected(true);
            week.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            root.applyCss();
            root.layout();
            group.layout();
            day.layout();
            week.layout();
            month.layout();
            root.applyCss();

            assertRegionRoundedCorners(day, true, false, false, true);
            assertRegionRoundedCorners(week, false, false, false, false);
            assertRegionRoundedCorners(month, false, true, true, false);
            assertStateLayerRadii(day, 20.0, 0.0, 0.0, 20.0);
            assertStateLayerRadii(week, 0.0, 0.0, 0.0, 0.0);
            assertStateLayerRadii(month, 0.0, 20.0, 20.0, 0.0);
        });
    }

    /// Verifies that tab component token properties are styleable from CSS.
    @Test
    void tabTokensAreStyleable() {
        M3Tab tab = M3Tab.withSelected("Overview", true);
        tab.setStyle(
                "-m3-container-height: 52px; "
                        + "-m3-tab-min-width: 120px; "
                        + "-m3-horizontal-padding: 20px; "
                        + "-m3-active-indicator-height: 4px; "
                        + "-m3-active-indicator-shape: 2px;"
        );

        applyCss(tab);

        assertTrue(tab.isSelected());
        assertEquals(52.0, tab.getContainerHeight(), 0.0001);
        assertEquals(120.0, tab.getTabMinWidth(), 0.0001);
        assertEquals(20.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(4.0, tab.getActiveIndicatorHeight(), 0.0001);
        assertEquals(2.0, tab.getActiveIndicatorShape(), 0.0001);
        assertEquals(120.0, tab.getMinWidth(), 0.0001);
        assertEquals(52.0, tab.getPrefHeight(), 0.0001);
        assertInstanceOf(M3TabSkin.class, tab.getSkin());
    }

    /// Verifies that tab bars group tabs and keep a selected tab.
    @Test
    void tabBarGroupsTabsAndKeepsSelection() {
        M3Tab overview = new M3Tab("Overview");
        M3Tab details = new M3Tab("Details");
        M3TabBar tabBar = new M3TabBar(overview, details);

        assertTrue(overview.isSelected());
        assertEquals(overview, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(overview), tabBar.getSelectedTabs());
        assertEquals(0, tabBar.getSelectedIndex());

        tabBar.selectIndex(1);

        assertFalse(overview.isSelected());
        assertTrue(details.isSelected());
        assertEquals(details, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(details), tabBar.getSelectedTabs());
        assertEquals(1, tabBar.getSelectedIndex());

        details.fire();

        assertTrue(details.isSelected());
        assertEquals(details, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(details), tabBar.getSelectedTabs());
        assertThrows(UnsupportedOperationException.class, () -> tabBar.getSelectedTabs().add(overview));

        tabBar.clearSelection();

        assertEquals(details, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(details), tabBar.getSelectedTabs());

        tabBar.setAllowEmptySelection(true);
        tabBar.clearSelection();

        assertNull(tabBar.getSelectedTab());
        assertTrue(tabBar.getSelectedTabs().isEmpty());
        assertEquals(-1, tabBar.getSelectedIndex());
        assertFalse(overview.isSelected());
        assertFalse(details.isSelected());

        tabBar.setAllowEmptySelection(false);

        assertEquals(overview, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(overview), tabBar.getSelectedTabs());
        assertTrue(overview.isSelected());
    }

    /// Verifies that tab skins expose the active indicator and ripple feedback.
    @Test
    void tabSkinLaysOutIndicatorAndRipple() {
        M3Tab tab = new M3Tab("Overview");
        tab.setSelected(true);
        Pane root = new Pane(tab);
        Scene scene = new Scene(root, 160.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        tab.resize(120.0, 48.0);
        tab.layout();

        Region indicator = lookupRegion(tab, ".m3-tab-active-indicator");
        assertEquals(120.0, indicator.getWidth(), 0.0001);
        assertEquals(3.0, indicator.getHeight(), 0.0001);
        assertEquals(1.0, indicator.getOpacity(), 0.0001);

        tab.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 40.0, 24.0, true));
        assertTrue(tab.isArmed());
        tab.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 40.0, 24.0, false));

        assertTrue(lookupRegion(tab, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that selection component token properties are styleable from CSS.
    @Test
    void selectionTokensAreStyleable() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        checkBox.setStyle("-m3-touch-target-size: 44px;");

        M3RadioButton radioButton = new M3RadioButton("Radio");
        radioButton.setStyle("-m3-touch-target-size: 46px;");

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setStyle("-m3-touch-target-size: 48px; -m3-track-shape: 18px;");

        applyCss(checkBox);
        applyCss(radioButton);
        applyCss(switchControl);

        assertEquals(44.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(44.0, checkBox.getPrefHeight(), 0.0001);
        assertEquals(46.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(46.0, radioButton.getPrefHeight(), 0.0001);
        assertEquals(48.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(18.0, switchControl.getTrackShape(), 0.0001);
        assertEquals(48.0, switchControl.getPrefHeight(), 0.0001);
    }

    /// Verifies that selection controls create Material Design 3 skins.
    @Test
    void selectionControlsCreateMaterialSkins() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        Pane root = new Pane(checkBox, radioButton, switchControl);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3CheckBoxSkin.class, checkBox.getSkin());
        assertInstanceOf(M3RadioButtonSkin.class, radioButton.getSkin());
        assertInstanceOf(M3SwitchSkin.class, switchControl.getSkin());
    }

    /// Verifies that selection skins initialize animated selected indicators from control state.
    @Test
    void selectionSkinsInitializeAnimatedSelectedIndicators() {
        M3CheckBox uncheckedCheckBox = new M3CheckBox("Unchecked");
        M3CheckBox checkedCheckBox = M3CheckBox.withSelected("Checked", true);
        M3RadioButton uncheckedRadioButton = new M3RadioButton("Unchecked");
        M3RadioButton checkedRadioButton = M3RadioButton.withSelected("Checked", true);
        Pane root = new Pane(uncheckedCheckBox, checkedCheckBox, uncheckedRadioButton, checkedRadioButton);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        Region uncheckedMark = lookupRegion(uncheckedCheckBox, ".mark");
        Region checkedMark = lookupRegion(checkedCheckBox, ".mark");
        Region uncheckedDot = lookupRegion(uncheckedRadioButton, ".dot");
        Region checkedDot = lookupRegion(checkedRadioButton, ".dot");
        assertEquals(0.0, uncheckedMark.getOpacity(), 0.0001);
        assertTrue(uncheckedMark.getScaleX() < 1.0);
        assertEquals(1.0, checkedMark.getOpacity(), 0.0001);
        assertEquals(1.0, checkedMark.getScaleX(), 0.0001);
        assertEquals(0.0, uncheckedDot.getOpacity(), 0.0001);
        assertTrue(uncheckedDot.getScaleX() < 1.0);
        assertEquals(1.0, checkedDot.getOpacity(), 0.0001);
        assertEquals(1.0, checkedDot.getScaleX(), 0.0001);
    }

    /// Verifies that selection control skins handle pointer and keyboard activation.
    @Test
    void selectionControlSkinsHandleActivationEvents() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        Pane root = new Pane(checkBox, radioButton, switchControl);
        Scene scene = new Scene(root, 320.0, 160.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        checkBox.resize(120.0, 40.0);
        radioButton.resize(120.0, 40.0);
        switchControl.resize(120.0, 40.0);

        checkBox.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(checkBox.isArmed());
        checkBox.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertTrue(checkBox.isSelected());

        radioButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(radioButton.isArmed());
        radioButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertTrue(radioButton.isSelected());

        switchControl.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        assertTrue(switchControl.isArmed());
        switchControl.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertTrue(switchControl.isSelected());
    }

    /// Verifies that selection control skins expose bounded indicator ripple feedback.
    @Test
    void selectionControlSkinsPlayBoundedRippleOnPress() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        Pane root = new Pane(checkBox);
        Scene scene = new Scene(root, 160.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        checkBox.resize(120.0, 40.0);
        checkBox.layout();

        assertInstanceOf(Region.class, checkBox.lookup(".m3-state-layer"));
        checkBox.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));

        assertTrue(lookupRegion(checkBox, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that selection skins clear transient interaction state when disabled.
    @Test
    void selectionControlSkinClearsPressedStateWhenDisabled() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        Pane root = new Pane(checkBox);
        Scene scene = new Scene(root, 160.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        checkBox.resize(120.0, 40.0);
        checkBox.layout();

        checkBox.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(checkBox.isArmed());
        assertTrue(lookupRegion(checkBox, ".m3-ripple").getOpacity() > 0.0);

        checkBox.setDisable(true);

        assertFalse(checkBox.isArmed());
        assertEquals(0.0, lookupRegion(checkBox, ".m3-ripple").getOpacity(), 0.0001);
    }

    /// Verifies that disposed selection skins unbind mirrored label properties.
    @Test
    void selectionControlSkinUnbindsLabelWhenDisposed() {
        M3CheckBox checkBox = new M3CheckBox("Check");

        applyCss(checkBox);

        Labeled label = assertInstanceOf(Labeled.class, checkBox.lookup(".m3-selection-label"));
        assertTrue(label.textProperty().isBound());

        checkBox.getSkin().dispose();

        assertFalse(label.textProperty().isBound());
    }

    /// Verifies that switch skins position the thumb from the selected state.
    @Test
    void switchSkinPositionsThumbFromSelectedState() {
        M3Switch offSwitch = new M3Switch("Off");
        M3Switch onSwitch = M3Switch.withSelected("On", true);
        Pane root = new Pane(offSwitch, onSwitch);
        Scene scene = new Scene(root, 260.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        offSwitch.resize(120.0, 40.0);
        onSwitch.resize(120.0, 40.0);
        offSwitch.layout();
        onSwitch.layout();

        Region offThumb = lookupRegion(offSwitch, ".thumb");
        Region onThumb = lookupRegion(onSwitch, ".thumb");
        assertFalse(offThumb.isManaged());
        assertFalse(onThumb.isManaged());
        assertTrue(onThumb.getLayoutX() > offThumb.getLayoutX());
        assertTrue(onThumb.getWidth() > offThumb.getWidth());
        assertEquals(8.0, offThumb.getLayoutX(), 0.0001);
        assertEquals(24.0, onThumb.getLayoutX(), 0.0001);
    }

    /// Verifies that switch skins apply the track shape token to the visual track.
    @Test
    void switchSkinAppliesTrackShapeToken() {
        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setStyle("-m3-track-shape: 12px;");
        Pane root = new Pane(switchControl);
        Scene scene = new Scene(root, 160.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        switchControl.resize(120.0, 40.0);
        switchControl.layout();

        Region track = lookupRegion(switchControl, ".box");
        assertEquals(12.0, track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
        assertEquals(12.0, track.getBorder().getStrokes().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
    }

    /// Verifies that selection factories apply initial selected state.
    @Test
    void selectionFactoriesApplyInitialSelection() {
        M3CheckBox checkBox = M3CheckBox.withSelected("Check", true);
        M3RadioButton radioButton = M3RadioButton.withSelected("Radio", true);
        M3Switch switchControl = M3Switch.withSelected("Switch", true);

        assertTrue(checkBox.isSelected());
        assertTrue(radioButton.isSelected());
        assertTrue(switchControl.isSelected());
    }

    /// Verifies that radio indicators use circular Material styling.
    @Test
    void radioButtonIndicatorUsesCircularMaterialShape() {
        M3RadioButton radioButton = new M3RadioButton("Radio");
        Pane root = new Pane(radioButton);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle()
                + " -m3-color-primary: rgb(1,2,3);"
                + " -m3-color-on-surface-variant: rgb(4,5,6);");
        applyInteractivePseudoClasses(radioButton);
        root.applyCss();

        Region radio = radioIndicator(radioButton);
        Region dot = radioDot(radioButton);
        assertRegionFill(radio, Color.TRANSPARENT);
        assertBorderColor(radio, Color.rgb(4, 5, 6));
        assertEquals(2.0, radio.getBorder().getStrokes().get(0).getWidths().getTop(), 0.0001);
        assertTrue(radio.getBorder().getStrokes().get(0).getRadii().getTopLeftHorizontalRadius() > 20.0);
        assertRegionFill(dot, Color.TRANSPARENT);

        radioButton.setSelected(true);
        root.applyCss();

        assertRegionFill(radio, Color.TRANSPARENT);
        assertBorderColor(radio, Color.rgb(1, 2, 3));
        assertRegionFill(dot, Color.rgb(1, 2, 3));
        assertTrue(dot.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius() > 20.0);
    }

    /// Verifies that slider component token properties are styleable from CSS.
    @Test
    void sliderTokensAreStyleable() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setStyle(
                "-m3-track-thickness: 8px; "
                        + "-m3-track-shape: 12px; "
                        + "-m3-thumb-size: 28px; "
                        + "-m3-touch-target-size: 56px;"
        );

        applyCss(slider);

        assertEquals(8.0, slider.getTrackThickness(), 0.0001);
        assertEquals(12.0, slider.getTrackShape(), 0.0001);
        assertEquals(28.0, slider.getThumbSize(), 0.0001);
        assertEquals(56.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(56.0, slider.getPrefHeight(), 0.0001);
    }

    /// Verifies that focused slider states keep Material track and thumb colors.
    @Test
    void sliderStateStylesPreserveMaterialColors() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        applyInteractivePseudoClasses(slider);
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region track = lookupRegion(slider, ".track");
        Region thumb = lookupRegion(slider, ".thumb");
        assertRegionFill(track, Color.rgb(37, 38, 39));
        assertNoBorder(track);
        assertRegionFill(thumb, Color.rgb(1, 2, 3));
        assertNoBorder(thumb);
    }

    /// Verifies that progress component token properties are styleable from CSS.
    @Test
    void progressTokensAreStyleable() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setStyle("-m3-track-thickness: 6px; -m3-track-shape: 18px;");

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setStyle("-m3-track-thickness: 6px; -m3-indicator-size: 72px;");

        applyCss(progressBar);
        applyCss(progressIndicator);

        assertEquals(6.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(18.0, progressBar.getTrackShape(), 0.0001);
        assertEquals(6.0, progressBar.getPrefHeight(), 0.0001);
        assertEquals(6.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(72.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(72.0, progressIndicator.getPrefWidth(), 0.0001);
        assertEquals(72.0, progressIndicator.getPrefHeight(), 0.0001);
    }

    /// Verifies that m3fx progress controls create Material Design 3 skins.
    @Test
    void progressControlsCreateMaterialSkins() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);

        applyCss(progressBar);
        applyCss(progressIndicator);

        assertInstanceOf(M3ProgressBarSkin.class, progressBar.getSkin());
        assertInstanceOf(M3ProgressIndicatorSkin.class, progressIndicator.getSkin());
    }

    /// Verifies that the progress bar skin lays out determinate progress without Modena internals.
    @Test
    void progressBarSkinLaysOutDeterminateProgress() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 240.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressBar.resize(200.0, 16.0);
        progressBar.layout();

        Rectangle track = (Rectangle) lookupShape(progressBar, ".track");
        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
        assertEquals(200.0, track.getWidth(), 0.0001);
        assertEquals(100.0, bar.getWidth(), 0.0001);
        assertEquals(4.0, track.getArcWidth(), 0.0001);
        assertEquals(4.0, track.getArcHeight(), 0.0001);
        assertEquals(4.0, bar.getArcWidth(), 0.0001);
        assertEquals(4.0, bar.getArcHeight(), 0.0001);
        assertTrue(bar.getBoundsInParent().getMaxX() <= track.getBoundsInParent().getMaxX() + 0.0001);
    }

    /// Verifies that the progress bar skin clips indeterminate progress inside the track.
    @Test
    void progressBarSkinClipsIndeterminateSegment() {
        M3ProgressBar progressBar = new M3ProgressBar();
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 240.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressBar.resize(200.0, 16.0);
        progressBar.layout();

        Pane container = assertInstanceOf(Pane.class, progressBar.lookup(".m3-progress-bar-container"));
        Rectangle clip = assertInstanceOf(Rectangle.class, container.getClip());
        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
        assertEquals(200.0, clip.getWidth(), 0.0001);
        assertEquals(4.0, clip.getHeight(), 0.0001);
        assertEquals(4.0, clip.getArcWidth(), 0.0001);
        assertEquals(4.0, clip.getArcHeight(), 0.0001);
        assertTrue(bar.getWidth() >= 24.0);
    }

    /// Verifies that determinate progress bar value changes are animated.
    @Test
    void progressBarSkinAnimatesDeterminateProgressChanges() throws InterruptedException {
        AtomicReference<M3ProgressBar> progressBarReference = new AtomicReference<>();
        AtomicReference<Rectangle> barReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(320.0),
                () -> {
                    M3ProgressBar progressBar = new M3ProgressBar(0.1);
                    Pane root = new Pane(progressBar);
                    Scene scene = new Scene(root, 240.0, 40.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    progressBar.resize(200.0, 16.0);
                    progressBar.layout();

                    Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
                    assertEquals(20.0, bar.getWidth(), 0.0001);

                    progressBarReference.set(progressBar);
                    barReference.set(bar);
                    progressBar.setProgress(0.9);
                    progressBar.layout();

                    assertTrue(bar.getWidth() < 180.0);
                },
                () -> {
                    M3ProgressBar progressBar = progressBarReference.get();
                    Rectangle bar = barReference.get();
                    progressBar.layout();

                    assertEquals(180.0, bar.getWidth(), 0.0001);
                }
        );
    }

    /// Verifies that indeterminate progress bar segments move over time.
    @Test
    void progressBarSkinAnimatesIndeterminateProgress() throws InterruptedException {
        AtomicReference<M3ProgressBar> progressBarReference = new AtomicReference<>();
        AtomicReference<Rectangle> barReference = new AtomicReference<>();
        AtomicReference<Double> initialX = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(180.0),
                () -> {
                    M3ProgressBar progressBar = new M3ProgressBar();
                    Pane root = new Pane(progressBar);
                    Scene scene = new Scene(root, 240.0, 40.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    progressBar.resize(200.0, 16.0);
                    progressBar.layout();

                    Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
                    progressBarReference.set(progressBar);
                    barReference.set(bar);
                    initialX.set(bar.getX());
                },
                () -> {
                    M3ProgressBar progressBar = progressBarReference.get();
                    Rectangle bar = barReference.get();
                    progressBar.layout();

                    assertTrue(Math.abs(bar.getX() - initialX.get()) > 0.1);
                }
        );
    }

    /// Verifies that progress bar subnodes keep Material colors.
    @Test
    void progressBarStateStylesPreserveMaterialColors() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 240.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        progressBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();

        Shape track = lookupShape(progressBar, ".track");
        Shape bar = lookupShape(progressBar, ".bar");
        assertEquals(Color.rgb(7, 8, 9), track.getFill());
        assertTrue(isTransparent(track.getStroke()));
        assertEquals(Color.rgb(1, 2, 3), bar.getFill());
        assertTrue(isTransparent(bar.getStroke()));
    }

    /// Verifies that progress indicator subnodes keep Material colors and determinate geometry.
    @Test
    void progressIndicatorStateStylesPreserveMaterialColors() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.25);
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 80.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        progressIndicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();
        progressIndicator.resize(48.0, 48.0);
        progressIndicator.layout();

        Shape track = lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        assertEquals(Color.rgb(7, 8, 9), track.getStroke());
        assertEquals(Color.rgb(1, 2, 3), indicator.getStroke());
        assertEquals(4.0, track.getStrokeWidth(), 0.0001);
        assertEquals(4.0, indicator.getStrokeWidth(), 0.0001);
        assertEquals(-90.0, indicator.getLength(), 0.0001);
    }

    /// Verifies that circular progress indicators can render at an explicitly allocated size.
    @Test
    void progressIndicatorSkinUsesAllocatedSize() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 80.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressIndicator.resize(64.0, 64.0);
        progressIndicator.layout();

        javafx.scene.shape.Circle track = (javafx.scene.shape.Circle) lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        assertEquals(30.0, track.getRadius(), 0.0001);
        assertEquals(30.0, indicator.getRadiusX(), 0.0001);
        assertEquals(30.0, indicator.getRadiusY(), 0.0001);
    }

    /// Verifies that indeterminate circular progress hides the determinate track.
    @Test
    void progressIndicatorSkinHidesTrackWhenIndeterminate() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 80.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressIndicator.resize(48.0, 48.0);
        progressIndicator.layout();

        Shape track = lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        assertFalse(track.isVisible());
        assertTrue(indicator.getLength() < 0.0);
    }

    /// Verifies that determinate circular progress value changes are animated.
    @Test
    void progressIndicatorSkinAnimatesDeterminateProgressChanges() throws InterruptedException {
        AtomicReference<M3ProgressIndicator> progressIndicatorReference = new AtomicReference<>();
        AtomicReference<Arc> indicatorReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(320.0),
                () -> {
                    M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.1);
                    Pane root = new Pane(progressIndicator);
                    Scene scene = new Scene(root, 80.0, 80.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    progressIndicator.resize(48.0, 48.0);
                    progressIndicator.layout();

                    Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
                    assertEquals(-36.0, indicator.getLength(), 0.0001);

                    progressIndicatorReference.set(progressIndicator);
                    indicatorReference.set(indicator);
                    progressIndicator.setProgress(0.9);
                    progressIndicator.layout();

                    assertTrue(indicator.getLength() > -324.0);
                },
                () -> {
                    M3ProgressIndicator progressIndicator = progressIndicatorReference.get();
                    Arc indicator = indicatorReference.get();
                    progressIndicator.layout();

                    assertEquals(-324.0, indicator.getLength(), 0.0001);
                }
        );
    }

    /// Verifies that indeterminate circular progress rotates without oversized sweeps.
    @Test
    void progressIndicatorSkinAnimatesIndeterminateProgress() throws InterruptedException {
        AtomicReference<M3ProgressIndicator> progressIndicatorReference = new AtomicReference<>();
        AtomicReference<Arc> indicatorReference = new AtomicReference<>();
        AtomicReference<Double> initialStartAngle = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(180.0),
                () -> {
                    M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
                    Pane root = new Pane(progressIndicator);
                    Scene scene = new Scene(root, 80.0, 80.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    progressIndicator.resize(48.0, 48.0);
                    progressIndicator.layout();

                    Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
                    progressIndicatorReference.set(progressIndicator);
                    indicatorReference.set(indicator);
                    initialStartAngle.set(indicator.getStartAngle());
                },
                () -> {
                    M3ProgressIndicator progressIndicator = progressIndicatorReference.get();
                    Arc indicator = indicatorReference.get();
                    progressIndicator.layout();

                    assertTrue(Math.abs(indicator.getStartAngle() - initialStartAngle.get()) > 0.1);
                    assertTrue(indicator.getLength() <= -42.0);
                    assertTrue(indicator.getLength() >= -96.0);
                }
        );
    }

    /// Verifies that divider component token properties are styleable from CSS.
    @Test
    void dividerTokensAreStyleable() {
        M3Divider divider = new M3Divider(Orientation.VERTICAL);
        divider.setStyle("-m3-thickness: 2px; -m3-inset-start: 12px; -m3-inset-end: 8px;");

        applyCss(divider);

        assertEquals(2.0, divider.getThickness(), 0.0001);
        assertEquals(12.0, divider.getInsetStart(), 0.0001);
        assertEquals(8.0, divider.getInsetEnd(), 0.0001);
        assertInstanceOf(M3DividerSkin.class, divider.getSkin());
    }

    /// Verifies that badge component token properties are styleable from CSS.
    @Test
    void badgeTokensAreStyleable() {
        M3Badge badge = new M3Badge("1234");
        badge.setMaxCharacterCount(2);
        badge.setStyle(
                "-m3-small-size: 8px; "
                        + "-m3-large-height: 18px; "
                        + "-m3-large-min-width: 20px; "
                        + "-m3-container-shape: 9px; "
                        + "-m3-horizontal-padding: 6px;"
        );

        applyCss(badge);

        assertEquals("12+", badge.getDisplayText());
        assertEquals(8.0, badge.getSmallSize(), 0.0001);
        assertEquals(18.0, badge.getLargeHeight(), 0.0001);
        assertEquals(20.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(9.0, badge.getContainerShape(), 0.0001);
        assertEquals(6.0, badge.getHorizontalPadding(), 0.0001);
        assertInstanceOf(M3BadgeSkin.class, badge.getSkin());
    }

    /// Verifies that badged boxes overlay badges on content.
    @Test
    void badgedBoxOwnsContentAndBadge() {
        Label content = new Label("Inbox");
        M3Badge badge = new M3Badge("3");
        M3BadgedBox badgedBox = new M3BadgedBox(content, badge);

        assertEquals(content, badgedBox.getContent());
        assertEquals(badge, badgedBox.getBadge());
        assertEquals(2, badgedBox.getChildren().size());
        assertEquals(Pos.TOP_RIGHT, StackPane.getAlignment(badge));

        badgedBox.setBadge(null);

        assertNull(badgedBox.getBadge());
        assertEquals(1, badgedBox.getChildren().size());
        assertEquals(content, badgedBox.getChildren().get(0));
    }

    /// Verifies that list item component token properties are styleable from CSS.
    @Test
    void listItemTokensAreStyleable() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setSupportingText("Supporting");
        listItem.setStyle(
                "-m3-one-line-height: 60px; "
                        + "-m3-two-line-height: 76px; "
                        + "-m3-three-line-height: 92px; "
                        + "-m3-container-shape: 12px; "
                        + "-m3-horizontal-padding: 20px; "
                        + "-m3-vertical-padding: 10px; "
                        + "-m3-content-spacing: 18px;"
        );

        applyCss(listItem);

        assertEquals(60.0, listItem.getOneLineHeight(), 0.0001);
        assertEquals(76.0, listItem.getTwoLineHeight(), 0.0001);
        assertEquals(92.0, listItem.getThreeLineHeight(), 0.0001);
        assertEquals(12.0, listItem.getContainerShape(), 0.0001);
        assertEquals(20.0, listItem.getHorizontalPadding(), 0.0001);
        assertEquals(10.0, listItem.getVerticalPadding(), 0.0001);
        assertEquals(18.0, listItem.getContentSpacing(), 0.0001);
        assertInstanceOf(M3ListItemSkin.class, listItem.getSkin());
    }

    /// Verifies that list item line count state follows text content.
    @Test
    void listItemLineCountTracksTextContent() {
        M3ListItem listItem = new M3ListItem("Headline");

        assertListItemLineCount(listItem, M3ListItemLineCount.ONE_LINE);

        listItem.setSupportingText("Supporting");
        assertListItemLineCount(listItem, M3ListItemLineCount.TWO_LINE);

        listItem.setOverlineText("Overline");
        assertListItemLineCount(listItem, M3ListItemLineCount.THREE_LINE);

        listItem.setSupportingText("");
        assertListItemLineCount(listItem, M3ListItemLineCount.TWO_LINE);

        listItem.setOverlineText("");
        assertListItemLineCount(listItem, M3ListItemLineCount.ONE_LINE);
    }

    /// Verifies that list item skins read the control line count when selecting height tokens.
    @Test
    void listItemLineCountDrivesSkinHeight() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setStyle(
                "-m3-one-line-height: 60px; "
                        + "-m3-two-line-height: 76px; "
                        + "-m3-three-line-height: 92px;"
        );
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());

        root.applyCss();
        assertEquals(60.0, listItemContainer(listItem).getPrefHeight(), 0.0001);

        listItem.setSupportingText("Supporting");
        root.applyCss();
        assertEquals(76.0, listItemContainer(listItem).getPrefHeight(), 0.0001);

        listItem.setOverlineText("Overline");
        root.applyCss();
        assertEquals(92.0, listItemContainer(listItem).getPrefHeight(), 0.0001);
    }

    /// Verifies that list items expose selected state and action behavior.
    @Test
    void listItemSupportsSelectionAndAction() {
        M3ListItem listItem = new M3ListItem("Headline");
        AtomicInteger fireCount = new AtomicInteger();
        listItem.setOnAction(event -> fireCount.incrementAndGet());
        listItem.setSelected(true);

        applyCss(listItem);
        listItem.resize(220.0, 56.0);
        listItem.layout();

        assertTrue(listItem.isSelected());
        listItem.fire();
        assertEquals(1, fireCount.get());
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertEquals(2, fireCount.get());
        listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertEquals(3, fireCount.get());
        listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        listItem.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertEquals(4, fireCount.get());
    }

    /// Verifies that lists expose item selection policies.
    @Test
    void listGroupsItemsAndKeepsSelectionPolicy() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3List list = new M3List(first, new M3Divider(), second, third);

        assertEquals(M3ListSelectionMode.NONE, list.getSelectionMode());
        first.fire();

        assertTrue(list.getSelectedItems().isEmpty());
        assertNull(list.getSelectedItem());

        list.setSelectionMode(M3ListSelectionMode.SINGLE);
        first.fire();
        second.fire();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, list.getSelectedItem());
        assertEquals(java.util.List.of(second), list.getSelectedItems());
        assertEquals(2, list.getSelectedIndex());
        assertThrows(UnsupportedOperationException.class, () -> list.getSelectedItems().add(third));
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(1));

        list.setAllowEmptySelection(false);
        list.clearSelection();

        assertEquals(second, list.getSelectedItem());
        assertTrue(second.isSelected());

        second.setSelected(false);

        assertEquals(second, list.getSelectedItem());
        assertTrue(second.isSelected());

        list.getItems().remove(second);

        assertFalse(second.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first), list.getSelectedItems());
        assertEquals(0, list.getSelectedIndex());
    }

    /// Verifies that lists can use multiple selected items.
    @Test
    void listCanUseMultipleSelection() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3List list = new M3List(first, second, third);

        list.setSelectionMode(M3ListSelectionMode.MULTIPLE);
        first.fire();
        third.fire();

        assertTrue(first.isSelected());
        assertTrue(third.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first, third), list.getSelectedItems());
        assertEquals(0, list.getSelectedIndex());

        first.fire();

        assertFalse(first.isSelected());
        assertEquals(java.util.List.of(third), list.getSelectedItems());
        assertEquals(2, list.getSelectedIndex());

        list.selectIndex(1);

        assertEquals(java.util.List.of(second, third), list.getSelectedItems());
        assertEquals(1, list.getSelectedIndex());
    }

    /// Verifies that lists collapse multiple selected items when switching to single selection.
    @Test
    void listCollapsesMultipleSelectionWhenModeChanges() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3List list = new M3List(first, second, third);

        list.setSelectionMode(M3ListSelectionMode.MULTIPLE);
        first.setSelected(true);
        second.setSelected(true);
        third.setSelected(true);

        list.setSelectionMode(M3ListSelectionMode.SINGLE);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertFalse(third.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first), list.getSelectedItems());
    }

    /// Verifies that list item skins expose bounded ripple feedback.
    @Test
    void listItemSkinPlaysBoundedRippleOnActivation() {
        M3ListItem listItem = new M3ListItem("Headline");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(220.0, 56.0);
        listItem.layout();

        assertInstanceOf(Region.class, listItem.lookup(".m3-state-layer"));
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));

        assertTrue(lookupRegion(listItem, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that one-line list item text is centered within its allocated row height.
    @Test
    void listItemSkinCentersOneLineText() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setLeading(new Label("H"));
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(220.0, 56.0);
        listItem.layout();

        Node textBoxNode = listItem.lookup(".m3-list-item-text");
        assertInstanceOf(VBox.class, textBoxNode);
        VBox textBox = (VBox) textBoxNode;
        Node headline = listItem.lookup(".m3-list-item-headline");
        assertInstanceOf(Label.class, headline);

        assertEquals(Pos.CENTER_LEFT, textBox.getAlignment());
        assertTrue(headline.getLayoutY() > 0.0);
    }

    /// Verifies that full list item shapes are resolved to the allocated row bounds.
    @Test
    void listItemSkinClampsFullContainerShape() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setSelected(true);
        listItem.setStyle("-m3-container-shape: 999px;");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(220.0, 56.0);
        listItem.layout();

        assertRegionRadii(listItemContainer(listItem), 28.0, 28.0, 28.0, 28.0);
    }

    /// Verifies that navigation item component token properties are styleable from CSS.
    @Test
    void navigationItemTokensAreStyleable() {
        M3NavigationItem item = new M3NavigationItem("Home");
        item.setStyle(
                "-m3-container-height: 84px; "
                        + "-m3-item-width: 92px; "
                        + "-m3-indicator-width: 70px; "
                        + "-m3-indicator-height: 34px; "
                        + "-m3-indicator-shape: 17px; "
                        + "-m3-content-spacing: 6px;"
        );

        applyCss(item);

        assertEquals(84.0, item.getContainerHeight(), 0.0001);
        assertEquals(92.0, item.getItemWidth(), 0.0001);
        assertEquals(70.0, item.getIndicatorWidth(), 0.0001);
        assertEquals(34.0, item.getIndicatorHeight(), 0.0001);
        assertEquals(17.0, item.getIndicatorShape(), 0.0001);
        assertEquals(6.0, item.getContentSpacing(), 0.0001);
        assertEquals(92.0, item.getPrefWidth(), 0.0001);
        assertEquals(84.0, item.getPrefHeight(), 0.0001);
        assertInstanceOf(M3NavigationItemSkin.class, item.getSkin());
    }

    /// Verifies that navigation items expose badge content in the graphic slot.
    @Test
    void navigationItemShowsBadge() {
        M3Badge badge = new M3Badge("3");
        M3NavigationItem item = M3NavigationItem.withSelected("Inbox", new M3Icon("I"), badge, true);

        applyCss(item);

        assertTrue(item.isSelected());
        assertEquals(badge, item.getBadge());
        assertEquals(badge, item.lookup(".m3-navigation-item-badge"));

        M3Badge replacement = new M3Badge();
        item.setBadge(replacement);

        assertEquals(replacement, item.getBadge());
        assertEquals(replacement, item.lookup(".m3-navigation-item-badge"));

        item.setBadge(null);

        assertNull(item.getBadge());
        assertNull(item.lookup(".m3-navigation-item-badge"));
    }

    /// Verifies that navigation bars group items and keep a selected item.
    @Test
    void navigationBarGroupsItemsAndKeepsSelection() {
        M3NavigationItem home = new M3NavigationItem("Home");
        M3NavigationItem search = new M3NavigationItem("Search");
        M3NavigationBar navigationBar = new M3NavigationBar(home, search);

        assertTrue(home.isSelected());
        assertEquals(home, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationBar.getSelectedItems());
        assertEquals(0, navigationBar.getSelectedIndex());

        navigationBar.selectIndex(1);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationBar.getSelectedItems());
        assertEquals(1, navigationBar.getSelectedIndex());

        search.fire();

        assertTrue(search.isSelected());
        assertEquals(search, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationBar.getSelectedItems());
        assertThrows(UnsupportedOperationException.class, () -> navigationBar.getSelectedItems().add(home));

        navigationBar.setAllowEmptySelection(true);
        navigationBar.clearSelection();

        assertNull(navigationBar.getSelectedItem());
        assertTrue(navigationBar.getSelectedItems().isEmpty());
        assertEquals(-1, navigationBar.getSelectedIndex());
        assertFalse(home.isSelected());
        assertFalse(search.isSelected());

        navigationBar.setAllowEmptySelection(false);

        assertEquals(home, navigationBar.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationBar.getSelectedItems());
    }

    /// Verifies that navigation rails group items and keep a selected item.
    @Test
    void navigationRailGroupsItemsAndKeepsSelection() {
        M3NavigationItem home = new M3NavigationItem("Home");
        M3NavigationItem search = new M3NavigationItem("Search");
        M3NavigationRail navigationRail = new M3NavigationRail(home, search);

        assertTrue(home.isSelected());
        assertEquals(home, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationRail.getSelectedItems());
        assertEquals(0, navigationRail.getSelectedIndex());

        navigationRail.selectIndex(1);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationRail.getSelectedItems());
        assertEquals(1, navigationRail.getSelectedIndex());

        search.fire();

        assertTrue(search.isSelected());
        assertEquals(search, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationRail.getSelectedItems());

        navigationRail.getItems().remove(search);

        assertFalse(search.isSelected());
        assertTrue(home.isSelected());
        assertEquals(home, navigationRail.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationRail.getSelectedItems());

        navigationRail.setAllowEmptySelection(true);
        navigationRail.clearSelection();

        assertNull(navigationRail.getSelectedItem());
        assertTrue(navigationRail.getSelectedItems().isEmpty());
        assertEquals(-1, navigationRail.getSelectedIndex());
        assertFalse(home.isSelected());
    }

    /// Verifies that navigation rail token rules override bar item metrics.
    @Test
    void navigationRailAppliesRailItemMetrics() {
        M3NavigationItem home = new M3NavigationItem("Home");
        M3NavigationRail navigationRail = new M3NavigationRail(home);
        Pane root = new Pane(navigationRail);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(96.0, navigationRail.getPrefWidth(), 0.0001);
        assertEquals(80.0, home.getItemWidth(), 0.0001);
        assertEquals(56.0, home.getIndicatorWidth(), 0.0001);
    }

    /// Verifies that top app bars expose navigation, title, and action slots.
    @Test
    void topAppBarExposesSlots() {
        Label navigation = new Label("Menu");
        Label search = new Label("Search");
        Label more = new Label("More");
        M3TopAppBar topAppBar = new M3TopAppBar(
                "Inbox",
                M3TopAppBarVariant.CENTER_ALIGNED,
                navigation,
                search,
                more
        );

        assertEquals("Inbox", topAppBar.getTitle());
        assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, topAppBar.getVariant());
        assertEquals(navigation, topAppBar.getNavigation());
        assertTrue(topAppBar.getActions().contains(search));
        assertTrue(topAppBar.getActions().contains(more));

        topAppBar.setTitle("Archive");
        topAppBar.setNavigation(null);

        assertEquals("Archive", topAppBar.getTitle());
        assertNull(topAppBar.getNavigation());
    }

    /// Verifies that top app bar variants update style classes and layout state.
    @Test
    void topAppBarVariantsUpdateStyleClassesAndLayout() {
        M3TopAppBar topAppBar = new M3TopAppBar("Inbox");

        assertEquals(M3TopAppBarVariant.SMALL, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.getStyleClass()));

        topAppBar.setVariant(M3TopAppBarVariant.CENTER_ALIGNED);

        assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.CENTER_ALIGNED.getStyleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.getStyleClass()));
        assertEquals(Region.USE_COMPUTED_SIZE, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Pos.CENTER_LEFT, topAppBar.getAlignment());

        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);

        assertEquals(M3TopAppBarVariant.MEDIUM, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM.getStyleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.CENTER_ALIGNED.getStyleClass()));
        assertEquals(112.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Pos.BOTTOM_LEFT, topAppBar.getAlignment());

        topAppBar.setVariant(M3TopAppBarVariant.LARGE);

        assertEquals(M3TopAppBarVariant.LARGE, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.LARGE.getStyleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM.getStyleClass()));
        assertEquals(152.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Pos.BOTTOM_LEFT, topAppBar.getAlignment());

        topAppBar.variantProperty().set(null);

        assertEquals(M3TopAppBarVariant.SMALL, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.getStyleClass()));
        assertEquals(Region.USE_COMPUTED_SIZE, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Pos.CENTER_LEFT, topAppBar.getAlignment());
    }

    /// Verifies that top app bar token rules apply container metrics.
    @Test
    void topAppBarAppliesMetrics() {
        M3TopAppBar topAppBar = new M3TopAppBar("Inbox");
        Pane root = new Pane(topAppBar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(64.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, topAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, topAppBar.getSpacing(), 0.0001);
    }

    /// Verifies that bottom app bars expose action and floating action slots.
    @Test
    void bottomAppBarExposesSlots() {
        Label search = new Label("Search");
        Label more = new Label("More");
        Label create = new Label("Create");
        M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                M3BottomAppBarFloatingActionAlignment.CENTER,
                create,
                search
        );

        bottomAppBar.getActions().add(more);

        assertEquals(M3BottomAppBarFloatingActionAlignment.CENTER, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getActions().contains(search));
        assertTrue(bottomAppBar.getActions().contains(more));
        assertEquals(create, bottomAppBar.getFloatingAction());

        bottomAppBar.setFloatingAction(null);

        assertNull(bottomAppBar.getFloatingAction());
    }

    /// Verifies that bottom app bars expose floating action alignment modes.
    @Test
    void bottomAppBarFloatingActionAlignmentUpdatesStyleClasses() {
        M3BottomAppBar bottomAppBar = new M3BottomAppBar(new Label("Search"));

        assertEquals(M3BottomAppBarFloatingActionAlignment.END, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.getStyleClass()
        ));

        bottomAppBar.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.CENTER);

        assertEquals(M3BottomAppBarFloatingActionAlignment.CENTER, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.CENTER.getStyleClass()
        ));
        assertFalse(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.getStyleClass()
        ));

        bottomAppBar.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.START);

        assertEquals(M3BottomAppBarFloatingActionAlignment.START, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.START.getStyleClass()
        ));
        assertFalse(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.CENTER.getStyleClass()
        ));

        bottomAppBar.floatingActionAlignmentProperty().set(null);

        assertEquals(M3BottomAppBarFloatingActionAlignment.END, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.getStyleClass()
        ));
    }

    /// Verifies that bottom app bar token rules apply container metrics.
    @Test
    void bottomAppBarAppliesMetrics() {
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        Pane root = new Pane(bottomAppBar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(80.0, bottomAppBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, bottomAppBar.getSpacing(), 0.0001);
    }

    /// Verifies that navigation drawers group list items and keep one selected item.
    @Test
    void navigationDrawerGroupsItemsAndKeepsSelection() {
        M3ListItem home = new M3ListItem("Home");
        M3ListItem search = new M3ListItem("Search");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(home, new M3Divider(), search);

        assertTrue(home.isSelected());
        assertEquals(home, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationDrawer.getSelectedItems());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.selectIndex(1));

        navigationDrawer.selectIndex(2);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationDrawer.getSelectedItems());
        assertEquals(2, navigationDrawer.getSelectedIndex());

        home.setSelected(true);

        assertTrue(home.isSelected());
        assertFalse(search.isSelected());
        assertEquals(home, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationDrawer.getSelectedItems());

        navigationDrawer.getItems().remove(home);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationDrawer.getSelectedItems());

        navigationDrawer.setAllowEmptySelection(true);
        navigationDrawer.clearSelection();

        assertNull(navigationDrawer.getSelectedItem());
        assertTrue(navigationDrawer.getSelectedItems().isEmpty());
        assertEquals(-1, navigationDrawer.getSelectedIndex());
        assertFalse(search.isSelected());
    }

    /// Verifies that navigation drawer token rules override list item metrics.
    @Test
    void navigationDrawerAppliesItemMetrics() {
        M3ListItem home = new M3ListItem("Home");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(home);
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(360.0, navigationDrawer.getPrefWidth(), 0.0001);
        assertEquals(56.0, home.getOneLineHeight(), 0.0001);
        assertEquals(999.0, home.getContainerShape(), 0.0001);
        assertEquals(12.0, home.getContentSpacing(), 0.0001);
    }

    /// Verifies that navigation drawer list items stay within the drawer padding.
    @Test
    void navigationDrawerConstrainsItemWidthToContentArea() {
        M3ListItem home = new M3ListItem("Home");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(home);
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root, 360.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        navigationDrawer.resize(320.0, 120.0);
        navigationDrawer.layout();
        home.layout();

        assertEquals(12.0, home.getLayoutX(), 0.0001);
        assertEquals(296.0, home.getWidth(), 0.0001);
        assertEquals(296.0, listItemContainer(home).getWidth(), 0.0001);

        Color selectedPixel = snapshotPixel(navigationDrawer, 30, 40);
        Color rightPaddingPixel = snapshotPixel(navigationDrawer, 318, 40);
        assertTrue(colorDistance(selectedPixel, rightPaddingPixel) > 0.01);

        Color roundedCornerPixel = snapshotPixel(navigationDrawer, 306, 14);
        assertTrue(colorDistance(selectedPixel, roundedCornerPixel) > 0.01);
    }

    /// Verifies that navigation item skins expose the selected indicator and ripple feedback.
    @Test
    void navigationItemSkinLaysOutIndicatorAndRipple() {
        M3NavigationItem item = new M3NavigationItem("Home");
        item.setSelected(true);
        Pane root = new Pane(item);
        Scene scene = new Scene(root, 120.0, 100.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        item.resize(80.0, 80.0);
        item.layout();

        Region indicator = lookupRegion(item, ".m3-navigation-item-indicator");
        assertEquals(64.0, indicator.getWidth(), 0.0001);
        assertEquals(32.0, indicator.getHeight(), 0.0001);
        assertEquals(1.0, indicator.getOpacity(), 0.0001);

        item.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 40.0, 40.0, true));
        assertTrue(item.isArmed());
        item.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 40.0, 40.0, false));

        assertTrue(lookupRegion(item, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that generated state layer rules apply beyond button-like controls.
    @Test
    void generatedStateLayerRulesApplyToInteractiveControls() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        M3Tab tab = new M3Tab("Overview");
        M3NavigationItem navigationItem = new M3NavigationItem("Home");
        M3ListItem listItem = new M3ListItem("Headline");
        M3Card card = new M3Card();
        M3Card disabledCard = new M3Card();
        Pane root = new Pane(checkBox, slider, tab, navigationItem, listItem, card, disabledCard);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        checkBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        slider.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        navigationItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        listItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        card.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        disabledCard.setDisable(true);
        root.applyCss();

        assertEquals(1.0, checkBox.getOpacity(), 0.0001);
        assertEquals(0.08, lookupRegion(checkBox, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, slider.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(slider, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, tab.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(tab, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, navigationItem.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(navigationItem, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, listItem.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(listItem, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, card.getOpacity(), 0.0001);
        assertEquals(0.08, lookupRegion(card, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.38, disabledCard.getOpacity(), 0.0001);
    }

    /// Verifies that the base stylesheet provides visible default state layer feedback.
    @Test
    void baseStylesheetProvidesDefaultStateLayerOpacity() {
        M3Button button = new M3Button("Button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        M3ThemeManager.uninstallThemeStylesheet(scene);
        button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();

        assertEquals(0.08, lookupRegion(button, ".m3-state-layer").getOpacity(), 0.0001);
    }

    /// Verifies that state layer feedback changes rendered button pixels.
    @Test
    void buttonStateLayerChangesRenderedPixels() {
        runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            button.resize(100.0, 40.0);
            button.layout();
            Color normal = snapshotPixel(button, 12, 20);

            button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            root.applyCss();
            Color hovered = snapshotPixel(button, 12, 20);

            Region stateLayer = lookupRegion(button, ".m3-state-layer");
            assertTrue(colorDistance(normal, hovered) > 0.01,
                    () -> "normal=" + normal
                            + ", hovered=" + hovered
                            + ", stateLayerOpacity=" + stateLayer.getOpacity()
                            + ", stateLayerBounds=" + stateLayer.getBoundsInParent()
                            + ", stateLayerBackground=" + stateLayer.getBackground());
        });
    }

    /// Verifies that m3fx sliders create the Material Design 3 slider skin.
    @Test
    void sliderCreatesMaterialSkin() {
        M3Slider slider = new M3Slider();

        applyCss(slider);

        assertInstanceOf(M3SliderSkin.class, slider.getSkin());
    }

    /// Verifies that slider track layout remains bounded by the slider control.
    @Test
    void sliderTrackStaysInsideControlBoundsInFlowLayout() {
        M3CheckBox checkBox = new M3CheckBox("Checkbox");
        M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
        slider.setPrefWidth(220.0);
        M3SegmentedButtonGroup segmentedButtons = new M3SegmentedButtonGroup(
                new M3SegmentedButton("Day"),
                new M3SegmentedButton("Week"),
                new M3SegmentedButton("Month")
        );
        FlowPane flow = new FlowPane(16.0, 16.0);
        flow.getChildren().addAll(checkBox, slider, segmentedButtons);
        Scene scene = new Scene(flow, 900.0, 160.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        flow.resize(900.0, 160.0);
        flow.applyCss();
        flow.layout();

        Node track = slider.lookup(".track");
        assertInstanceOf(Node.class, track);
        assertTrue(slider.getWidth() <= 220.0 + 0.0001);
        assertTrue(track.getBoundsInParent().getMinX() >= -0.0001);
        assertTrue(track.getBoundsInParent().getMaxX() <= slider.getWidth() + 0.0001);
    }

    /// Verifies that the slider skin updates values from pointer and keyboard input.
    @Test
    void sliderSkinHandlesPointerAndKeyboardInput() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        slider.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 210.0, 24.0, true));
        assertTrue(slider.getValue() > 95.0);
        slider.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 24.0, false));
        assertTrue(slider.getValue() < 5.0);

        slider.setValue(50.0);
        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(slider.getValue() > 50.0);
        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertEquals(50.0, slider.getValue(), 0.0001);
    }

    /// Verifies that drag interactions snap the displayed slider position without animation lag.
    @Test
    void sliderSkinSnapsDisplayedPositionWhileDragging() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region thumb = lookupRegion(slider, ".thumb");
        slider.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 210.0, 24.0, true));
        slider.layout();
        assertTrue(thumb.getLayoutX() > 190.0);

        slider.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_DRAGGED, 10.0, 24.0, true));
        slider.layout();
        assertTrue(thumb.getLayoutX() < 1.0);
    }

    /// Verifies that disabled slider skins ignore pointer and keyboard input.
    @Test
    void sliderSkinIgnoresInputWhileDisabled() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();
        slider.setValueChanging(true);

        slider.setDisable(true);
        slider.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 210.0, 24.0, true));
        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertEquals(50.0, slider.getValue(), 0.0001);
        assertFalse(slider.isValueChanging());
        assertEquals(0.0, lookupRegion(slider, ".m3-ripple").getOpacity(), 0.0001);
    }

    /// Verifies that disposed slider skins no longer receive disabled-state changes.
    @Test
    void sliderSkinRemovesDisabledListenerWhenDisposed() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);

        applyCss(slider);

        Skin<?> skin = slider.getSkin();
        slider.setValueChanging(true);
        skin.dispose();
        slider.setDisable(true);

        assertTrue(slider.isValueChanging());
    }

    /// Verifies that slider skins expose bounded thumb ripple feedback.
    @Test
    void sliderSkinPlaysBoundedRippleOnPress() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        assertInstanceOf(Region.class, slider.lookup(".m3-state-layer"));
        slider.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 110.0, 24.0, true));

        assertTrue(lookupRegion(slider, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies style classes for container controls.
    @Test
    void containerControlsExposeStyleClasses() {
        M3Card card = new M3Card();
        card.setVariant(M3CardVariant.OUTLINED);

        M3Snackbar snackbar = new M3Snackbar("Message");
        M3SnackbarHost snackbarHost = new M3SnackbarHost();
        M3TopAppBar topAppBar = new M3TopAppBar();
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        M3SideSheet sideSheet = new M3SideSheet();
        M3BottomSheet bottomSheet = new M3BottomSheet();
        M3Scrim scrim = new M3Scrim();
        M3NavigationBar navigationBar = new M3NavigationBar();
        M3NavigationRail navigationRail = new M3NavigationRail();
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();

        assertTrue(card.getStyleClass().contains(M3Card.STYLE_CLASS));
        assertTrue(card.getStyleClass().contains(M3CardVariant.OUTLINED.getStyleClass()));
        assertTrue(snackbar.getStyleClass().contains(M3Snackbar.STYLE_CLASS));
        assertTrue(snackbarHost.getStyleClass().contains(M3SnackbarHost.STYLE_CLASS));
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBar.STYLE_CLASS));
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.getStyleClass()));
        assertTrue(bottomAppBar.getStyleClass().contains(M3BottomAppBar.STYLE_CLASS));
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.getStyleClass()
        ));
        assertTrue(sideSheet.getStyleClass().contains(M3SideSheet.STYLE_CLASS));
        assertTrue(bottomSheet.getStyleClass().contains(M3BottomSheet.STYLE_CLASS));
        assertTrue(scrim.getStyleClass().contains(M3Scrim.STYLE_CLASS));
        assertTrue(navigationBar.getStyleClass().contains(M3NavigationBar.STYLE_CLASS));
        assertTrue(navigationRail.getStyleClass().contains(M3NavigationRail.STYLE_CLASS));
        assertTrue(navigationDrawer.getStyleClass().contains(M3NavigationDrawer.STYLE_CLASS));
    }

    /// Verifies style classes for input and selection controls.
    @Test
    void inputAndSelectionControlsExposeStyleClasses() {
        M3TextField textField = new M3TextField();
        textField.setVariant(M3TextInputVariant.OUTLINED);

        M3Chip chip = new M3Chip("Chip");
        chip.setVariant(M3ChipVariant.FILTER);

        assertTrue(textField.getStyleClass().contains(M3TextField.STYLE_CLASS));
        assertTrue(textField.getStyleClass().contains(M3TextInputVariant.OUTLINED.getStyleClass()));
        assertTrue(new M3TextArea().getStyleClass().contains(M3TextArea.STYLE_CLASS));
        assertTrue(new M3Tooltip().getStyleClass().contains(M3Tooltip.STYLE_CLASS));
        assertTrue(new M3Avatar("A").getStyleClass().contains(M3Avatar.STYLE_CLASS));
        assertTrue(new M3Icon("A").getStyleClass().contains(M3Icon.STYLE_CLASS));
        assertTrue(new M3IconToggleButton("A").getStyleClass().contains(M3IconToggleButton.STYLE_CLASS));
        assertTrue(new M3IconToggleButtonGroup().getStyleClass().contains(M3IconToggleButtonGroup.STYLE_CLASS));
        assertTrue(new M3Text("Text").getStyleClass().contains(M3Text.STYLE_CLASS));
        assertTrue(new M3Surface().getStyleClass().contains(M3Surface.STYLE_CLASS));
        assertTrue(new M3BadgedBox().getStyleClass().contains(M3BadgedBox.STYLE_CLASS));
        assertTrue(new M3Menu().getStyleClass().contains(M3Menu.STYLE_CLASS));
        assertTrue(new M3MenuItem("Open").getStyleClass().contains(M3MenuItem.STYLE_CLASS));
        assertTrue(new M3MenuButton("More").getStyleClass().contains(M3MenuButton.STYLE_CLASS));
        assertTrue(new M3SearchBar().getStyleClass().contains(M3SearchBar.STYLE_CLASS));
        assertTrue(new M3SearchView().getStyleClass().contains(M3SearchView.STYLE_CLASS));
        assertTrue(new M3CheckBox().getStyleClass().contains(M3CheckBox.STYLE_CLASS));
        assertTrue(new M3RadioButton().getStyleClass().contains(M3RadioButton.STYLE_CLASS));
        assertTrue(new M3Switch().getStyleClass().contains(M3Switch.STYLE_CLASS));
        assertTrue(new M3Slider().getStyleClass().contains(M3Slider.STYLE_CLASS));
        assertTrue(chip.getStyleClass().contains(M3Chip.STYLE_CLASS));
        assertTrue(chip.getStyleClass().contains(M3ChipVariant.FILTER.getStyleClass()));
        assertTrue(new M3ChipGroup().getStyleClass().contains(M3ChipGroup.STYLE_CLASS));
        assertTrue(new M3SegmentedButton("Day").getStyleClass().contains(M3SegmentedButton.STYLE_CLASS));
        assertTrue(new M3SegmentedButtonGroup().getStyleClass().contains(M3SegmentedButtonGroup.STYLE_CLASS));
        assertTrue(new M3Tab("Overview").getStyleClass().contains(M3Tab.STYLE_CLASS));
        assertTrue(new M3TabBar().getStyleClass().contains(M3TabBar.STYLE_CLASS));
        assertTrue(new M3Divider().getStyleClass().contains(M3Divider.STYLE_CLASS));
        assertTrue(new M3Badge("1").getStyleClass().contains(M3Badge.STYLE_CLASS));
        assertTrue(new M3NavigationItem("Home").getStyleClass().contains(M3NavigationItem.STYLE_CLASS));
        assertTrue(new M3List().getStyleClass().contains(M3List.STYLE_CLASS));
        assertTrue(new M3ListItem("Item").getStyleClass().contains(M3ListItem.STYLE_CLASS));
    }

    /// Verifies that M3FX selectable controls do not inherit JavaFX ToggleButton.
    @Test
    void selectableControlsDoNotExtendToggleButton() {
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3Chip.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3IconToggleButton.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3SegmentedButton.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3Tab.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3NavigationItem.class));
    }

    /// Verifies that controls expose their default styles through user-agent stylesheets.
    @Test
    void controlsExposeUserAgentStylesheets() {
        assertUserAgentStylesheet(new M3Button(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3IconButton(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3IconToggleButton(), "/styles/controls/icon-toggle-button.css");
        assertUserAgentStylesheet(new M3IconToggleButtonGroup(), "/styles/controls/icon-toggle-button.css");
        assertUserAgentStylesheet(new M3FloatingActionButton(), "/styles/controls/floating-action-button.css");
        assertUserAgentStylesheet(new M3TextField(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3PasswordField(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3TextArea(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3Avatar(), "/styles/controls/avatar.css");
        assertUserAgentStylesheet(new M3Icon(), "/styles/controls/icon.css");
        assertUserAgentStylesheet(new M3Text(), "/styles/controls/text.css");
        assertUserAgentStylesheet(new M3Surface(), "/styles/controls/surface.css");
        assertUserAgentStylesheet(new M3BadgedBox(), "/styles/controls/badge.css");
        assertUserAgentStylesheet(new M3Menu(), "/styles/controls/menu.css");
        assertUserAgentStylesheet(new M3SearchBar(), "/styles/controls/search.css");
        assertUserAgentStylesheet(new M3SearchView(), "/styles/controls/search.css");
        assertUserAgentStylesheet(new M3SideSheet(), "/styles/controls/sheet.css");
        assertUserAgentStylesheet(new M3BottomSheet(), "/styles/controls/sheet.css");
        assertUserAgentStylesheet(new M3Scrim(), "/styles/controls/scrim.css");
        assertUserAgentStylesheet(new M3CheckBox(), "/styles/controls/selection.css");
        assertUserAgentStylesheet(new M3RadioButton(), "/styles/controls/selection.css");
        assertUserAgentStylesheet(new M3Switch(), "/styles/controls/selection.css");
        assertUserAgentStylesheet(new M3Slider(), "/styles/controls/slider.css");
        assertUserAgentStylesheet(new M3Chip(), "/styles/controls/chip.css");
        assertUserAgentStylesheet(new M3ChipGroup(), "/styles/controls/chip.css");
        assertUserAgentStylesheet(new M3SegmentedButton(), "/styles/controls/segmented-button.css");
        assertUserAgentStylesheet(new M3SegmentedButtonGroup(), "/styles/controls/segmented-button.css");
        assertUserAgentStylesheet(new M3Tab(), "/styles/controls/tab.css");
        assertUserAgentStylesheet(new M3TabBar(), "/styles/controls/tab.css");
        assertUserAgentStylesheet(new M3ProgressBar(), "/styles/controls/progress.css");
        assertUserAgentStylesheet(new M3ProgressIndicator(), "/styles/controls/progress.css");
        assertUserAgentStylesheet(new M3Divider(), "/styles/controls/divider.css");
        assertUserAgentStylesheet(new M3Badge(), "/styles/controls/badge.css");
        assertUserAgentStylesheet(new M3TopAppBar(), "/styles/controls/top-app-bar.css");
        assertUserAgentStylesheet(new M3BottomAppBar(), "/styles/controls/bottom-app-bar.css");
        assertUserAgentStylesheet(new M3NavigationBar(), "/styles/controls/navigation-bar.css");
        assertUserAgentStylesheet(new M3NavigationRail(), "/styles/controls/navigation-rail.css");
        assertUserAgentStylesheet(new M3NavigationDrawer(), "/styles/controls/navigation-drawer.css");
        assertUserAgentStylesheet(new M3NavigationItem(), "/styles/controls/navigation-bar.css");
        assertUserAgentStylesheet(new M3List(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListItem(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3Card(), "/styles/controls/card.css");
        assertUserAgentStylesheet(new M3DialogPane(), "/styles/controls/dialog.css");
        assertUserAgentStylesheet(new M3Snackbar(), "/styles/controls/snackbar.css");
        assertUserAgentStylesheet(new M3SnackbarHost(), "/styles/controls/snackbar.css");
    }

    /// Applies the m3fx stylesheet to a control in a scene.
    private static void applyCss(javafx.scene.Node node) {
        Pane root = new Pane(node);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }

    /// Returns deterministic color tokens used by button state style tests.
    private static String buttonStateTestColors() {
        return "-m3-color-primary: rgb(1,2,3); "
                + "-m3-color-on-primary: rgb(4,5,6); "
                + "-m3-color-secondary-container: rgb(7,8,9); "
                + "-m3-color-on-secondary-container: rgb(10,11,12); "
                + "-m3-color-outline: rgb(13,14,15); "
                + "-m3-color-surface-container-low: rgb(16,17,18); "
                + "-m3-color-surface-container-high: rgb(19,20,21); "
                + "-m3-color-primary-container: rgb(22,23,24); "
                + "-m3-color-on-primary-container: rgb(25,26,27); "
                + "-m3-color-tertiary-container: rgb(28,29,30); "
                + "-m3-color-on-tertiary-container: rgb(31,32,33); "
                + "-m3-color-on-surface: rgb(34,35,36); "
                + "-m3-color-surface-container-highest: rgb(37,38,39); "
                + "-m3-color-on-surface-variant: rgb(40,41,42);";
    }

    /// Returns deterministic color tokens used by snackbar style tests.
    private static String snackbarStateTestColors() {
        return buttonStateTestColors()
                + " -m3-color-inverse-surface: rgb(50,51,52); "
                + "-m3-color-inverse-on-surface: rgb(53,54,55); "
                + "-m3-color-inverse-primary: rgb(56,57,58);";
    }

    /// Applies the pseudo-class combination that previously allowed Modena button styles to win.
    private static void applyInteractivePseudoClasses(Node node) {
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("armed"), true);
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
    }

    /// Verifies the first background fill and text fill for a labeled control.
    private static void assertLabeledColors(Labeled control, Color expectedBackground, Color expectedText) {
        assertEquals(1, control.getBackground().getFills().size());
        assertEquals(expectedBackground, control.getBackground().getFills().get(0).getFill());
        assertEquals(expectedText, control.getTextFill());
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(Node node, String selector) {
        Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
    }

    /// Verifies the resolved shape radius used by a control's state layer.
    private static void assertStateLayerShape(Node node, double expectedRadius) {
        Region container = lookupRegion(node, ".m3-state-layer-container");
        assertInstanceOf(Path.class, container.getClip());
        assertStateLayerRadii(node, expectedRadius, expectedRadius, expectedRadius, expectedRadius);
    }

    /// Verifies the resolved corner radii used by a control's state layer.
    private static void assertStateLayerRadii(
            Node node,
            double topLeft,
            double topRight,
            double bottomRight,
            double bottomLeft
    ) {
        Region overlay = lookupRegion(node, ".m3-state-layer");
        assertRegionRadii(overlay, topLeft, topRight, bottomRight, bottomLeft);
    }

    /// Verifies which corners are rounded on a region's background.
    private static void assertRegionRoundedCorners(
            Region region,
            boolean topLeft,
            boolean topRight,
            boolean bottomRight,
            boolean bottomLeft
    ) {
        javafx.scene.layout.CornerRadii radii = region.getBackground().getFills().get(0).getRadii();
        assertEquals(topLeft, radii.getTopLeftHorizontalRadius() > 0.0);
        assertEquals(topRight, radii.getTopRightHorizontalRadius() > 0.0);
        assertEquals(bottomRight, radii.getBottomRightHorizontalRadius() > 0.0);
        assertEquals(bottomLeft, radii.getBottomLeftHorizontalRadius() > 0.0);
    }

    /// Verifies a region's concrete background corner radii.
    private static void assertRegionRadii(
            Region region,
            double topLeft,
            double topRight,
            double bottomRight,
            double bottomLeft
    ) {
        javafx.scene.layout.CornerRadii radii = region.getBackground().getFills().get(0).getRadii();
        assertEquals(topLeft, radii.getTopLeftHorizontalRadius(), 0.0001);
        assertEquals(topRight, radii.getTopRightHorizontalRadius(), 0.0001);
        assertEquals(bottomRight, radii.getBottomRightHorizontalRadius(), 0.0001);
        assertEquals(bottomLeft, radii.getBottomLeftHorizontalRadius(), 0.0001);
    }

    /// Returns a shape looked up below a node.
    private static Shape lookupShape(Node node, String selector) {
        Node child = node.lookup(selector);
        assertInstanceOf(Shape.class, child);
        return (Shape) child;
    }

    /// Returns the radio indicator region.
    private static Region radioIndicator(M3RadioButton radioButton) {
        Node radio = radioButton.lookup(".radio");
        assertInstanceOf(Region.class, radio);
        return (Region) radio;
    }

    /// Returns the radio indicator dot region.
    private static Region radioDot(M3RadioButton radioButton) {
        Node dot = radioButton.lookup(".dot");
        assertInstanceOf(Region.class, dot);
        return (Region) dot;
    }

    /// Verifies the first background fill for a region.
    private static void assertRegionFill(Region region, Color expectedFill) {
        assertEquals(1, region.getBackground().getFills().size());
        assertEquals(expectedFill, region.getBackground().getFills().get(0).getFill());
    }

    /// Verifies that a region has no visible border strokes.
    private static void assertNoBorder(Region region) {
        if (region.getBorder() == null) {
            return;
        }

        for (javafx.scene.layout.BorderStroke stroke : region.getBorder().getStrokes()) {
            boolean zeroWidth = stroke.getWidths().getTop() == 0.0
                    && stroke.getWidths().getRight() == 0.0
                    && stroke.getWidths().getBottom() == 0.0
                    && stroke.getWidths().getLeft() == 0.0;
            boolean transparent = isTransparent(stroke.getTopStroke())
                    && isTransparent(stroke.getRightStroke())
                    && isTransparent(stroke.getBottomStroke())
                    && isTransparent(stroke.getLeftStroke());
            assertTrue(zeroWidth || transparent);
        }
    }

    /// Returns whether a paint is fully transparent.
    private static boolean isTransparent(Paint paint) {
        return paint instanceof Color color && color.getOpacity() == 0.0;
    }

    /// Verifies the first border stroke color for a region.
    private static void assertBorderColor(Region region, Color expectedColor) {
        assertEquals(1, region.getBorder().getStrokes().size());
        assertEquals(expectedColor, region.getBorder().getStrokes().get(0).getTopStroke());
    }

    /// Verifies the first bottom border stroke color for a region.
    private static void assertBorderBottomColor(Region region, Color expectedColor) {
        assertEquals(1, region.getBorder().getStrokes().size());
        assertEquals(expectedColor, region.getBorder().getStrokes().get(0).getBottomStroke());
    }

    /// Verifies a list item's line count property and pseudo-class state.
    private static void assertListItemLineCount(M3ListItem listItem, M3ListItemLineCount lineCount) {
        assertEquals(lineCount, listItem.getLineCount());
        assertEquals(lineCount, listItem.lineCountProperty().get());
        assertEquals(lineCount.getLineCount(), listItem.getLineCount().getLineCount());
        assertEquals(lineCount == M3ListItemLineCount.ONE_LINE,
                listItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("one-line")));
        assertEquals(lineCount == M3ListItemLineCount.TWO_LINE,
                listItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("two-line")));
        assertEquals(lineCount == M3ListItemLineCount.THREE_LINE,
                listItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("three-line")));
    }

    /// Returns the list item skin container region.
    private static javafx.scene.layout.Region listItemContainer(M3ListItem listItem) {
        javafx.scene.Node container = listItem.lookup(".m3-list-item-container");
        assertInstanceOf(javafx.scene.layout.Region.class, container);
        return (javafx.scene.layout.Region) container;
    }

    /// Verifies that a node user-agent stylesheet has the expected bundled suffix.
    private static void assertUserAgentStylesheet(Object node, String suffix) {
        try {
            Object stylesheet = node.getClass().getMethod("getUserAgentStylesheet").invoke(node);
            assertTrue(stylesheet instanceof String);
            assertTrue(((String) stylesheet).endsWith(suffix));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /// Returns a rendered pixel from a node snapshot.
    private static Color snapshotPixel(Node node, int x, int y) {
        if (Platform.isFxApplicationThread()) {
            return snapshotPixelOnFxThread(node, x, y);
        }

        AtomicReference<Color> color = new AtomicReference<>();
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                color.set(snapshotPixelOnFxThread(node, x, y));
            } catch (RuntimeException e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        RuntimeException exception = failure.get();
        if (exception != null) {
            throw exception;
        }
        return color.get();
    }

    /// Runs a task on the FX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
    }

    /// Runs setup on the FX thread and verifies the result after a JavaFX delay.
    private static void runOnFxThreadAfterDelay(
            Duration delay,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                setup.run();
                PauseTransition pause = new PauseTransition(delay);
                pause.setOnFinished(event -> {
                    try {
                        verification.run();
                    } catch (Throwable e) {
                        failure.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
    }

    /// Returns a rendered pixel from a node snapshot on the FX thread.
    private static Color snapshotPixelOnFxThread(Node node, int x, int y) {
        WritableImage image = new WritableImage(
                (int) Math.ceil(node.getLayoutBounds().getWidth()),
                (int) Math.ceil(node.getLayoutBounds().getHeight())
        );
        node.snapshot(null, image);
        return image.getPixelReader().getColor(x, y);
    }

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }

    /// Creates a primary mouse event for control behavior tests.
    private static MouseEvent primaryMouseEvent(
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        return new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
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
                false,
                null
        );
    }

    /// Creates a key event for control behavior tests.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }
}
