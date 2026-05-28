// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.skins.M3AvatarSkin;
import org.glavo.m3fx.skins.M3BadgeSkin;
import org.glavo.m3fx.skins.M3BadgedBoxSkin;
import org.glavo.m3fx.skins.M3BannerSkin;
import org.glavo.m3fx.skins.M3BottomAppBarSkin;
import org.glavo.m3fx.skins.M3BottomSheetSkin;
import org.glavo.m3fx.skins.M3ButtonGroupSkin;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.glavo.m3fx.skins.M3CardSkin;
import org.glavo.m3fx.skins.M3CarouselSkin;
import org.glavo.m3fx.skins.M3CheckBoxSkin;
import org.glavo.m3fx.skins.M3ChipGroupSkin;
import org.glavo.m3fx.skins.M3ChipSkin;
import org.glavo.m3fx.skins.M3DividerSkin;
import org.glavo.m3fx.skins.M3DisclosureIconSkin;
import org.glavo.m3fx.skins.M3FabMenuSkin;
import org.glavo.m3fx.skins.M3FloatingActionButtonSkin;
import org.glavo.m3fx.skins.M3IconSkin;
import org.glavo.m3fx.skins.M3IconToggleButtonGroupSkin;
import org.glavo.m3fx.skins.M3IconToggleButtonSkin;
import org.glavo.m3fx.skins.M3ListItemSkin;
import org.glavo.m3fx.skins.M3ListPaneSkin;
import org.glavo.m3fx.skins.M3ListViewCellSkin;
import org.glavo.m3fx.skins.M3ListViewSkin;
import org.glavo.m3fx.skins.M3LoadingIndicatorSkin;
import org.glavo.m3fx.skins.M3MenuSkin;
import org.glavo.m3fx.skins.M3NavigationBarSkin;
import org.glavo.m3fx.skins.M3NavigationDrawerGroupSkin;
import org.glavo.m3fx.skins.M3NavigationDrawerSkin;
import org.glavo.m3fx.skins.M3NavigationItemSkin;
import org.glavo.m3fx.skins.M3NavigationRailSkin;
import org.glavo.m3fx.skins.M3ProgressBarSkin;
import org.glavo.m3fx.skins.M3ProgressIndicatorSkin;
import org.glavo.m3fx.skins.M3RadioButtonSkin;
import org.glavo.m3fx.skins.M3SegmentedButtonGroupSkin;
import org.glavo.m3fx.skins.M3SegmentedButtonSkin;
import org.glavo.m3fx.skins.M3SearchBarSkin;
import org.glavo.m3fx.skins.M3SearchViewSkin;
import org.glavo.m3fx.skins.M3SliderSkin;
import org.glavo.m3fx.skins.M3SplitButtonSkin;
import org.glavo.m3fx.skins.M3SnackbarHostSkin;
import org.glavo.m3fx.skins.M3SnackbarSkin;
import org.glavo.m3fx.skins.M3SideSheetSkin;
import org.glavo.m3fx.skins.M3SurfaceSkin;
import org.glavo.m3fx.skins.M3SwitchSkin;
import org.glavo.m3fx.skins.M3TabBarSkin;
import org.glavo.m3fx.skins.M3TabSkin;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.glavo.m3fx.skins.M3TextSkin;
import org.glavo.m3fx.skins.M3TopAppBarSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        Platform.setImplicitExit(false);
    }

    /// Verifies that standalone control stylesheets provide fallback color tokens without requiring a theme.
    @Test
    void standaloneControlStylesheetsResolveFallbackColorTokens() {
        Logger cssLogger = Logger.getLogger("javafx.css");
        Logger cssStyleHelperLogger = Logger.getLogger("javafx.scene.CssStyleHelper");
        List<LogRecord> cssWarnings = new ArrayList<>();
        Handler handler = new Handler() {
            /// Captures JavaFX CSS warnings emitted while fallback token styles are resolved.
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    cssWarnings.add(record);
                }
            }

            /// Flushes captured records.
            @Override
            public void flush() {
            }

            /// Closes this in-memory handler.
            @Override
            public void close() {
            }
        };
        cssLogger.addHandler(handler);
        cssStyleHelperLogger.addHandler(handler);
        try {
            runOnFxThread(() -> {
                M3Button button = new M3Button("Button");
                M3ListItem listItem = new M3ListItem("List item");
                M3Menu menu = new M3Menu(new M3MenuItem("Menu item"));
                FlowPane root = new FlowPane(12.0, 12.0, button, listItem, menu);
                new Scene(root, 480.0, 220.0);

                root.applyCss();
                root.layout();
            });
        } finally {
            cssLogger.removeHandler(handler);
            cssStyleHelperLogger.removeHandler(handler);
        }

        assertTrue(cssWarnings.stream().noneMatch(M3ControlStyleTest::isColorTokenCssWarning),
                () -> cssWarnings.stream()
                        .map(record -> record.getLevel() + ": " + record.getMessage())
                        .collect(Collectors.joining("\n")));
    }

    /// Verifies that button variants update their style classes.
    @Test
    void buttonVariantUpdatesStyleClass() {
        AtomicInteger actions = new AtomicInteger();
        M3Button button = createButton("Button", M3ButtonVariant.FILLED, event -> actions.incrementAndGet());

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

    /// Verifies that only the elevated button variant owns button elevation.
    @Test
    void buttonElevationDoesNotLeakIntoNonElevatedVariants() {
        runOnFxThread(() -> {
            M3Button button = createButton("Button", M3ButtonVariant.ELEVATED);
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();

            assertDropShadow(button);
            applyInteractivePseudoClasses(button);

            for (M3ButtonVariant variant : java.util.List.of(
                    M3ButtonVariant.FILLED,
                    M3ButtonVariant.TONAL,
                    M3ButtonVariant.OUTLINED,
                    M3ButtonVariant.TEXT
            )) {
                button.setVariant(M3ButtonVariant.ELEVATED);
                root.applyCss();
                assertDropShadow(button);

                button.setVariant(variant);
                root.applyCss();

                assertNull(button.getEffect(), () -> variant + " button should not keep elevation");
            }

            button.getStyleClass().add("m3-snackbar-action");
            root.applyCss();

            assertNull(button.getEffect(), "snackbar action button should not keep elevation");
        });
    }

    /// Verifies that non-elevated button variants do not use pressed container scaling.
    @Test
    void nonElevatedButtonVariantsDoNotScaleWhenPressed() {
        runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            button.resize(120.0, 40.0);
            button.layout();

            for (M3ButtonVariant variant : java.util.List.of(
                    M3ButtonVariant.FILLED,
                    M3ButtonVariant.TONAL,
                    M3ButtonVariant.OUTLINED,
                    M3ButtonVariant.TEXT
            )) {
                button.setVariant(variant);
                root.applyCss();
                pressButtonAndJumpToPressedFrame(button);

                assertEquals(1.0, button.getScaleX(), 0.0001, () -> variant + " button scaleX");
                assertEquals(1.0, button.getScaleY(), 0.0001, () -> variant + " button scaleY");
                button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
            }

            button.setVariant(M3ButtonVariant.ELEVATED);
            root.applyCss();
            pressButtonAndJumpToPressedFrame(button);

            assertEquals(0.98, button.getScaleX(), 0.0001);
            assertEquals(0.98, button.getScaleY(), 0.0001);
        });
    }

    /// Verifies that flat labeled button controls do not use depth-style pressed scaling.
    @Test
    void flatLabeledButtonControlsDoNotScaleWhenPressed() {
        runOnFxThread(() -> {
            M3Chip chip = new M3Chip("Chip");
            M3SegmentedButton segmentedButton = new M3SegmentedButton("Segment");
            M3Tab tab = new M3Tab("Tab");
            M3IconToggleButton iconToggleButton = new M3IconToggleButton("star");
            FlowPane root = new FlowPane(12.0, 12.0, chip, segmentedButton, tab, iconToggleButton);
            Scene scene = new Scene(root, 420.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            for (ButtonBase button : java.util.List.of(chip, segmentedButton, tab, iconToggleButton)) {
                button.resize(120.0, 40.0);
                button.layout();
                pressButtonAndJumpToPressedFrame(button);

                assertEquals(1.0, button.getScaleX(), 0.0001,
                        () -> button.getClass().getSimpleName() + " scaleX");
                assertEquals(1.0, button.getScaleY(), 0.0001,
                        () -> button.getClass().getSimpleName() + " scaleY");
                button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
            }
        });
    }

    /// Verifies that floating action buttons keep depth-style pressed scaling.
    @Test
    void floatingActionButtonKeepsDepthPressedScaling() {
        runOnFxThread(() -> {
            M3FloatingActionButton button = new M3FloatingActionButton("Create");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            button.resize(96.0, 56.0);
            button.layout();
            pressButtonAndJumpToPressedFrame(button);

            assertEquals(0.98, button.getScaleX(), 0.0001);
            assertEquals(0.98, button.getScaleY(), 0.0001);
        });
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
        assertEquals(48.0, button.getMaxWidth(), 0.0001);
        assertEquals(48.0, button.getMaxHeight(), 0.0001);
    }

    /// Verifies that button groups assign connected position style classes.
    @Test
    void buttonGroupAssignsPositionStyleClasses() {
        M3Button first = new M3Button("First");
        M3Button second = new M3Button("Second");
        M3Button third = new M3Button("Third");
        M3ButtonGroup group = new M3ButtonGroup(first, second, third);

        assertTrue(group.getStyleClass().contains(M3ButtonGroup.STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3ButtonGroup.GROUPED_BUTTON_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3ButtonGroup.FIRST_BUTTON_STYLE_CLASS));
        assertTrue(second.getStyleClass().contains(M3ButtonGroup.MIDDLE_BUTTON_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3ButtonGroup.LAST_BUTTON_STYLE_CLASS));
        assertEquals(3, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        group.getItems().remove(second);

        assertFalse(second.getStyleClass().contains(M3ButtonGroup.GROUPED_BUTTON_STYLE_CLASS));
        assertFalse(second.getStyleClass().contains(M3ButtonGroup.MIDDLE_BUTTON_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3ButtonGroup.FIRST_BUTTON_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3ButtonGroup.LAST_BUTTON_STYLE_CLASS));

        group.getItems().remove(first);

        assertFalse(first.getStyleClass().contains(M3ButtonGroup.GROUPED_BUTTON_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3ButtonGroup.SINGLE_BUTTON_STYLE_CLASS));
    }

    /// Verifies that button groups mirror physical edge style classes for right-to-left layout.
    @Test
    void buttonGroupMirrorsPositionStyleClassesForRightToLeft() {
        M3Button first = new M3Button("Archive");
        M3Button second = new M3Button("Share");
        M3Button third = new M3Button("Edit");
        M3ButtonGroup group = new M3ButtonGroup(first, second, third);

        group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        assertTrue(first.getStyleClass().contains(M3ButtonGroup.LAST_BUTTON_STYLE_CLASS));
        assertTrue(second.getStyleClass().contains(M3ButtonGroup.MIDDLE_BUTTON_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3ButtonGroup.FIRST_BUTTON_STYLE_CLASS));

        group.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        assertTrue(first.getStyleClass().contains(M3ButtonGroup.FIRST_BUTTON_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3ButtonGroup.LAST_BUTTON_STYLE_CLASS));
    }

    /// Verifies that button group spacing is styleable from CSS.
    @Test
    void buttonGroupSpacingTokenIsStyleable() {
        M3ButtonGroup group = new M3ButtonGroup(new M3Button("A"), new M3Button("B"));
        group.setStyle("-m3-button-group-spacing: -2px;");

        applyCss(group);

        assertEquals(-2.0, group.getSpacing(), 0.0001);
    }

    /// Verifies that button group corners and feedback layers match right-to-left visual order.
    @Test
    void buttonGroupUsesRightToLeftPositionSpecificShapes() {
        runOnFxThread(() -> {
            M3Button first = new M3Button("Archive");
            M3Button second = new M3Button("Share");
            M3Button third = new M3Button("Edit");
            M3ButtonGroup group = new M3ButtonGroup(first, second, third);
            group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 360.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();
            group.resize(group.prefWidth(-1.0), group.prefHeight(-1.0));
            group.layout();
            first.layout();
            second.layout();
            third.layout();
            root.applyCss();

            assertRegionRoundedCorners(first, false, true, true, false);
            assertRegionRoundedCorners(second, false, false, false, false);
            assertRegionRoundedCorners(third, true, false, false, true);
            assertStateLayerRadii(first, 0.0, 20.0, 20.0, 0.0);
            assertStateLayerRadii(second, 0.0, 0.0, 0.0, 0.0);
            assertStateLayerRadii(third, 20.0, 0.0, 0.0, 20.0);
        });
    }

    /// Verifies that grouped selection containers create Material Design 3 skins.
    @Test
    void groupedSelectionContainersCreateMaterialSkins() {
        M3ButtonGroup buttonGroup = new M3ButtonGroup(new M3Button("A"), new M3Button("B"));
        M3IconToggleButtonGroup iconGroup =
                new M3IconToggleButtonGroup(new M3IconToggleButton("A"), new M3IconToggleButton("B"));
        M3ChipGroup chipGroup = new M3ChipGroup(new M3Chip("A"), new M3Chip("B"));
        M3SegmentedButtonGroup segmentedGroup =
                new M3SegmentedButtonGroup(new M3SegmentedButton("A"), new M3SegmentedButton("B"));
        M3TabBar tabBar = new M3TabBar(new M3Tab("A"), new M3Tab("B"));
        Pane root = new Pane(buttonGroup, iconGroup, chipGroup, segmentedGroup, tabBar);
        Scene scene = new Scene(root);

        chipGroup.setPrefWrapLength(240.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3ButtonGroupSkin.class, buttonGroup.getSkin());
        assertInstanceOf(M3IconToggleButtonGroupSkin.class, iconGroup.getSkin());
        assertInstanceOf(M3ChipGroupSkin.class, chipGroup.getSkin());
        assertInstanceOf(M3SegmentedButtonGroupSkin.class, segmentedGroup.getSkin());
        assertInstanceOf(M3TabBarSkin.class, tabBar.getSkin());
        assertEquals(240.0, chipGroup.getPrefWrapLength(), 0.0001);
    }

    /// Verifies that split buttons delegate action and menu APIs to their child buttons.
    @Test
    void splitButtonDelegatesActionAndMenuApis() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3SplitButton splitButton = new M3SplitButton("Create", first, second);
        AtomicInteger actions = new AtomicInteger();

        splitButton.setOnAction(event -> actions.incrementAndGet());
        splitButton.fire();
        splitButton.setVariant(M3ButtonVariant.OUTLINED);
        splitButton.setSelectionMode(M3MenuSelectionMode.SINGLE);
        splitButton.setAllowEmptySelection(false);
        splitButton.selectIndex(1);

        assertEquals(1, actions.get());
        assertEquals("Create", splitButton.getText());
        assertEquals(splitButton.getActionButton().getText(), splitButton.textProperty().get());
        assertEquals(M3ButtonVariant.OUTLINED, splitButton.getActionButton().getVariant());
        assertEquals(M3ButtonVariant.OUTLINED, splitButton.getMenuButton().getVariant());
        assertEquals(second, splitButton.getSelectedItem());
        assertEquals(1, splitButton.getSelectedIndex());
        assertEquals(splitButton.getMenu(), splitButton.queryAccessibleAttribute(AccessibleAttribute.SUBMENU));
        assertEquals(2, splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(splitButton.getMenuButton(), splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertFalse(splitButton.isShowing());
    }

    /// Verifies that split buttons apply stable part style classes and internal edge pseudo-classes.
    @Test
    void splitButtonAppliesPartStyleClasses() {
        M3SplitButton splitButton = createSplitButton(
                "Export",
                M3ButtonVariant.TONAL,
                new M3MenuItem("PDF")
        );
        PseudoClass leftEdge = PseudoClass.getPseudoClass("left-edge");
        PseudoClass rightEdge = PseudoClass.getPseudoClass("right-edge");

        assertTrue(splitButton.getStyleClass().contains(M3SplitButton.STYLE_CLASS));
        assertTrue(splitButton.getActionButton().getStyleClass().contains(M3SplitButton.ACTION_BUTTON_STYLE_CLASS));
        assertTrue(splitButton.getMenuButton().getStyleClass().contains(M3SplitButton.MENU_BUTTON_STYLE_CLASS));
        assertTrue(splitButton.getActionButton().getPseudoClassStates().contains(leftEdge));
        assertTrue(splitButton.getMenuButton().getPseudoClassStates().contains(rightEdge));
        assertFalse(splitButton.getActionButton().getStyleClass().contains("m3-split-button-left"));
        assertFalse(splitButton.getActionButton().getStyleClass().contains("m3-split-button-right"));
        assertFalse(splitButton.getMenuButton().getStyleClass().contains("m3-split-button-left"));
        assertFalse(splitButton.getMenuButton().getStyleClass().contains("m3-split-button-right"));

        applyCss(splitButton);

        assertInstanceOf(M3SplitButtonSkin.class, splitButton.getSkin());
        assertEquals(48.0, splitButton.getMenuButton().getPrefWidth(), 0.0001);
        assertEquals(0.0, splitButton.getMenuButton().getHorizontalPadding(), 0.0001);
    }

    /// Verifies that split button part shapes mirror under right-to-left layout.
    @Test
    void splitButtonMirrorsPartShapesForRightToLeft() {
        runOnFxThread(() -> {
            M3SplitButton splitButton = createSplitButton(
                    "Export",
                    M3ButtonVariant.TONAL,
                    new M3MenuItem("PDF")
            );
            Pane root = new Pane(splitButton);
            Scene scene = new Scene(root, 240.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            splitButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            splitButton.resize(splitButton.prefWidth(-1.0), splitButton.prefHeight(-1.0));
            splitButton.layout();
            splitButton.getActionButton().layout();
            splitButton.getMenuButton().layout();
            root.applyCss();

            PseudoClass leftEdge = PseudoClass.getPseudoClass("left-edge");
            PseudoClass rightEdge = PseudoClass.getPseudoClass("right-edge");
            assertFalse(splitButton.getActionButton().getPseudoClassStates().contains(leftEdge));
            assertTrue(splitButton.getActionButton().getPseudoClassStates().contains(rightEdge));
            assertTrue(splitButton.getMenuButton().getPseudoClassStates().contains(leftEdge));
            assertFalse(splitButton.getMenuButton().getPseudoClassStates().contains(rightEdge));
            assertFalse(splitButton.getActionButton().getStyleClass().contains("m3-split-button-left"));
            assertFalse(splitButton.getActionButton().getStyleClass().contains("m3-split-button-right"));
            assertFalse(splitButton.getMenuButton().getStyleClass().contains("m3-split-button-left"));
            assertFalse(splitButton.getMenuButton().getStyleClass().contains("m3-split-button-right"));
            assertRegionRoundedCorners(splitButton.getActionButton(), false, true, true, false);
            assertRegionRoundedCorners(splitButton.getMenuButton(), true, false, false, true);
            assertStateLayerRadii(splitButton.getActionButton(), 0.0, 20.0, 20.0, 0.0);
            assertStateLayerRadii(splitButton.getMenuButton(), 20.0, 0.0, 0.0, 20.0);
        });
    }

    /// Verifies that right-to-left joined buttons render mirrored edge shapes in snapshots.
    @Test
    void rightToLeftJoinedButtonsRenderMirroredCornersInSnapshot() {
        runOnFxThread(() -> {
            M3Button firstGroupButton = createButton("Archive", M3ButtonVariant.TONAL);
            M3Button secondGroupButton = createButton("Share", M3ButtonVariant.TONAL);
            M3Button thirdGroupButton = createButton("Edit", M3ButtonVariant.TONAL);
            M3ButtonGroup buttonGroup =
                    new M3ButtonGroup(firstGroupButton, secondGroupButton, thirdGroupButton);
            buttonGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segmentedGroup = new M3SegmentedButtonGroup(day, week, month);
            segmentedGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            month.setSelected(true);

            M3SplitButton splitButton = createSplitButton(
                    "Export",
                    M3ButtonVariant.TONAL,
                    new M3MenuItem("PDF")
            );
            splitButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            VBox root = new VBox(16.0, buttonGroup, segmentedGroup, splitButton);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(420.0, 220.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-rtl-joined-buttons.png"
            ));
            assertSnapshotEdgeCorners(image, firstGroupButton, false, true);
            assertSnapshotEdgeCorners(image, thirdGroupButton, true, false);
            assertSnapshotNodeBorderContainsContrast(image, day, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, month, Color.WHITE, 0.04);
            assertSnapshotEdgeCorners(image, splitButton.getActionButton(), false, true);
            assertSnapshotEdgeCorners(image, splitButton.getMenuButton(), true, false);
        });
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
        M3FloatingActionButton button = createFab(
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

    /// Verifies that floating action button menus expand, collapse, and manage action item state.
    @Test
    void fabMenuExpandsAndCollapsesActionItems() {
        M3FloatingActionButton first = new M3FloatingActionButton("A");
        M3FloatingActionButton second = new M3FloatingActionButton("B");
        M3FabMenu menu = new M3FabMenu();
        menu.addItems(first, second);

        assertTrue(menu.getStyleClass().contains(M3FabMenu.STYLE_CLASS));
        assertTrue(menu.getToggleButton().getStyleClass().contains(M3FabMenu.TOGGLE_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3FabMenu.ACTION_STYLE_CLASS));
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

        first.fire();

        assertFalse(menu.isExpanded());

        menu.show();

        menu.hide();

        assertFalse(menu.isExpanded());
        assertFalse(first.isVisible());
        assertFalse(first.isManaged());

        menu.getItems().remove(first);

        assertFalse(first.getStyleClass().contains(M3FabMenu.ACTION_STYLE_CLASS));
        assertTrue(first.isVisible());
        assertTrue(first.isManaged());
    }

    /// Verifies that floating action button menus toggle through the main button and accessibility actions.
    @Test
    void fabMenuTogglesFromButtonAndAccessibilityActions() {
        M3FloatingActionButton customToggle = new M3FloatingActionButton("+");
        M3FloatingActionButton action = new M3FloatingActionButton("A");
        M3FabMenu menu = new M3FabMenu(customToggle);
        menu.addItem(action);

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
        runOnFxThread(() -> {
            M3FloatingActionButton firstAction = new M3FloatingActionButton("A");
            M3FloatingActionButton secondAction = new M3FloatingActionButton("B");
            M3FabMenu menu = new M3FabMenu();
            menu.addItems(firstAction, secondAction);
            Pane root = new Pane(menu);
            Stage stage = new Stage();

            try {
                M3MotionSettings.setAnimationsEnabled(menu, false);
                Scene scene = new Scene(root, 220.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menu.resizeRelocate(24.0, 24.0, 120.0, 160.0);
                root.layout();

                menu.show();
                firstAction.requestFocus();
                assertTrue(firstAction.isFocused());

                firstAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(secondAction.isFocused());
                secondAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                assertTrue(menu.getToggleButton().isFocused());
                menu.getToggleButton().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(secondAction.isFocused());

                secondAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(menu.isExpanded());
                assertTrue(menu.getToggleButton().isFocused());

                menu.show();
                firstAction.requestFocus();
                assertTrue(firstAction.isFocused());

                firstAction.fire();

                assertFalse(menu.isExpanded());
                assertTrue(menu.getToggleButton().isFocused());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(menu);
                stage.close();
            }
        });
    }

    /// Verifies that FAB menu action spacing is styleable from CSS.
    @Test
    void fabMenuActionSpacingTokenIsStyleable() {
        M3FabMenu menu = new M3FabMenu(new M3FloatingActionButton());
        menu.setStyle("-m3-fab-menu-action-spacing: 18px;");

        applyCss(menu);

        assertEquals(18.0, menu.getActionSpacing(), 0.0001);
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

    /// Verifies that carousels manage items, selected state, and item click selection.
    @Test
    void carouselManagesItemsSelectionAndItemClicks() {
        Label first = new Label("First");
        Label second = new Label("Second");
        Label third = new Label("Third");
        third.setDisable(true);
        M3Carousel carousel = new M3Carousel(first, second, third);

        assertTrue(first.getStyleClass().contains(M3Carousel.ITEM_STYLE_CLASS));
        assertEquals(-1, carousel.getSelectedIndex());
        assertNull(carousel.getSelectedItem());

        carousel.selectIndex(1);

        assertSame(second, carousel.getSelectedItem());
        assertEquals(1, carousel.getSelectedIndex());
        assertEquals(carousel.getSelectedItems(), carousel.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
        assertTrue(second.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS));

        carousel.selectNext();

        assertSame(first, carousel.getSelectedItem());
        assertFalse(second.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS));

        second.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 4.0, 4.0, false));

        assertSame(second, carousel.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> carousel.select(new Label("Detached")));

        carousel.getItems().remove(second);

        assertFalse(second.getStyleClass().contains(M3Carousel.ITEM_STYLE_CLASS));
        assertFalse(second.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS));
        assertSame(third, carousel.getSelectedItem());

        carousel.clearItems();

        assertEquals(-1, carousel.getSelectedIndex());
        assertNull(carousel.getSelectedItem());
    }

    /// Verifies that carousels support keyboard navigation and accessibility selection.
    @Test
    void carouselHandlesKeyboardAndAccessibilitySelection() {
        Label first = new Label("First");
        Label second = new Label("Second");
        Label third = new Label("Third");
        M3Carousel carousel = new M3Carousel(first, second, third);

        carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertSame(first, carousel.getSelectedItem());

        carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertSame(second, carousel.getSelectedItem());
        assertEquals(false, carousel.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(third, carousel.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

        carousel.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, third);

        assertSame(third, carousel.getSelectedItem());

        carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

        assertSame(first, carousel.getSelectedItem());

        carousel.setWrapAround(false);
        carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

        assertSame(first, carousel.getSelectedItem());
    }

    /// Verifies that carousel skins create an internal viewport and reveal selected items.
    @Test
    void carouselCreatesMaterialSkinAndScrollsSelectedItemIntoView() {
        runOnFxThread(() -> {
            M3Carousel carousel = new M3Carousel(
                    carouselTestItem("A"),
                    carouselTestItem("B"),
                    carouselTestItem("C"),
                    carouselTestItem("D"),
                    carouselTestItem("E")
            );
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(260.0, 100.0);
            Pane root = new Pane(carousel);
            Scene scene = new Scene(root, 280.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            carousel.resizeRelocate(0.0, 0.0, 260.0, 100.0);
            root.layout();

            assertInstanceOf(M3CarouselSkin.class, carousel.getSkin());

            carousel.selectIndex(4);
            root.layout();

            ScrollPane viewport = assertInstanceOf(ScrollPane.class, carousel.lookup("." + M3Carousel.VIEWPORT_STYLE_CLASS));
            assertTrue(viewport.getHvalue() > 0.5, () -> "hvalue=" + viewport.getHvalue());
            assertTrue(M3ScrollPanes.isSmoothScrollingEnabled(viewport));

            viewport.setHvalue(0.0);
            M3MotionSettings.setAnimationsEnabled(viewport, false);
            ScrollEvent event = scrollEvent(viewport, 0.0, -80.0);
            viewport.fireEvent(event);

            assertTrue(event.isConsumed());
            assertTrue(viewport.getHvalue() > 0.0, () -> "hvalue=" + viewport.getHvalue());

            M3MotionSettings.clearAnimationsEnabled(viewport);
        });
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

    /// Verifies that banners expose text, icon, actions, and accessibility state.
    @Test
    void bannerExposesTextIconActionsAndAccessibility() {
        Label icon = new Label("i");
        Label firstAction = new Label("First");
        Label secondAction = new Label("Second");
        M3Banner banner = createBanner("Message", icon, firstAction);

        assertTrue(banner.getStyleClass().contains(M3Banner.STYLE_CLASS));
        assertEquals("Message", banner.getText());
        assertEquals(icon, banner.getIcon());
        assertEquals(firstAction, banner.getActions().get(0));
        assertEquals("Message", banner.getAccessibleText());
        assertEquals("Message", banner.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(2, banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(icon, banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(firstAction, banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        banner.setText("Updated");
        banner.setIcon(null);
        banner.setActions(secondAction);

        assertEquals("Updated", banner.getAccessibleText());
        assertEquals(1, banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(secondAction, banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertNull(banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        banner.clearActions();

        assertEquals(0, banner.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertThrows(NullPointerException.class, () -> banner.setText(null));
        assertThrows(NullPointerException.class, () -> banner.addAction(null));
        assertThrows(NullPointerException.class, () -> createBanner("Message", null));
    }

    /// Verifies that banner component tokens apply profile-specific layout metrics.
    @Test
    void bannerAppliesProfileMetrics() {
        M3Banner banner = createBanner("Message", new Label("i"), new M3Button("Action"));
        Pane root = new Pane(banner);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(88.0, banner.getMinHeight(), 0.0001);
        assertEquals(20.0, banner.getPadding().getTop(), 0.0001);
        assertEquals(28.0, banner.getPadding().getLeft(), 0.0001);
        assertEquals(20.0, assertInstanceOf(HBox.class, banner.lookup("." + M3Banner.CONTAINER_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(12.0, assertInstanceOf(HBox.class, banner.lookup("." + M3Banner.ACTIONS_STYLE_CLASS)).getSpacing(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(80.0, banner.getMinHeight(), 0.0001);
        assertEquals(16.0, banner.getPadding().getTop(), 0.0001);
        assertEquals(24.0, banner.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, assertInstanceOf(HBox.class, banner.lookup("." + M3Banner.CONTAINER_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(8.0, assertInstanceOf(HBox.class, banner.lookup("." + M3Banner.ACTIONS_STYLE_CLASS)).getSpacing(), 0.0001);
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

    /// Verifies that snackbars expose their rendered action button to accessibility clients.
    @Test
    void snackbarExposesAccessibleActionButton() {
        runOnFxThread(() -> {
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            Pane root = new Pane(snackbar);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 320.0, 120.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                M3Button actionButton = assertInstanceOf(
                        M3Button.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0)
                );

                assertEquals("Saved Undo", snackbar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                assertEquals(1, snackbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                snackbar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(actionButton.isFocused());

                snackbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, actionButton);
                assertTrue(actionButton.isFocused());

                snackbar.setActionText("");

                assertEquals(0, snackbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertNull(snackbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertNull(snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that snackbar internal padding changes when the action slot is visible.
    @Test
    void snackbarSkinUsesSymmetricPaddingWithoutAction() {
        M3Snackbar snackbar = new M3Snackbar("Saved");
        snackbar.setContentPadding(16.0);

        applyCss(snackbar);

        Region container = lookupRegion(snackbar, ".m3-snackbar-container");
        assertEquals(16.0, container.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, container.getPadding().getRight(), 0.0001);
        assertEquals(8.0, container.getPadding().getTop(), 0.0001);
        assertEquals(8.0, container.getPadding().getBottom(), 0.0001);

        snackbar.setActionText("Undo");

        assertEquals(16.0, container.getPadding().getLeft(), 0.0001);
        assertEquals(8.0, container.getPadding().getRight(), 0.0001);
        assertEquals(8.0, container.getPadding().getTop(), 0.0001);
        assertEquals(8.0, container.getPadding().getBottom(), 0.0001);
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
            assertInstanceOf(M3SnackbarHostSkin.class, host.getSkin());
            assertTrue(host.isShowing());
            assertTrue(snackbar.getParent() != null);
            assertTrue(snackbar.isVisible());
            assertTrue(snackbar.isManaged());

            M3Button actionButton = assertInstanceOf(M3Button.class, snackbar.lookup(".m3-snackbar-action"));
            actionButton.fire();

            assertEquals(1, actionCount.get());
            assertFalse(host.isShowing());
        });
    }

    /// Verifies that snackbar hosts do not stretch snackbars to the full overlay size.
    @Test
    void snackbarHostKeepsSnackbarAtPreferredSizeInLargeOverlay() {
        runOnFxThread(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            host.setDisplayDuration(Duration.INDEFINITE);
            StackPane root = new StackPane(new Region(), host);
            StackPane.setAlignment(host, Pos.BOTTOM_CENTER);
            Scene scene = new Scene(root, 800.0, 500.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            host.show("Saved");
            root.applyCss();
            root.resize(800.0, 500.0);
            root.layout();

            M3Snackbar snackbar = assertInstanceOf(M3Snackbar.class, host.getSnackbar());
            var bounds = snackbar.getBoundsInParent();

            assertTrue(host.getWidth() >= 790.0);
            assertTrue(snackbar.getWidth() < host.getWidth() / 2.0);
            assertTrue(snackbar.getHeight() < host.getHeight() / 3.0);
            assertEquals((host.getWidth() - snackbar.getWidth()) / 2.0, bounds.getMinX(), 1.0);
            assertTrue(snackbar.getLayoutY() > host.getHeight() - 120.0);
            assertTrue(snackbar.getLayoutY() + snackbar.getHeight()
                    <= host.getHeight() - host.getPadding().getBottom() + 0.0001);
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
                        assertNull(snackbar.getParent());
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
                    assertTrue(first.getParent() != null);

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
                    assertNull(first.getParent());
                    assertTrue(second.getParent() != null);
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
            applyCss(host);

            assertEquals(second, host.getSnackbar());
            assertNull(first.getParent());
            assertTrue(second.getParent() != null);
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
                    assertNull(first.getParent());
                    assertNull(second.getParent());
                    assertFalse(first.isVisible());
                }
        );
    }

    /// Verifies that snackbar hosts expose the current snackbar and queue to accessibility clients.
    @Test
    void snackbarHostExposesAccessibleStateAndActions() {
        runOnFxThread(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar first = new M3Snackbar("Saved", "Undo");
            M3Snackbar second = new M3Snackbar("Deleted");

            host.setDisplayDuration(Duration.ZERO);
            host.show(first);
            host.enqueue(second);

            assertEquals(true, host.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            assertEquals(first, host.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertEquals(2, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals(first, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertEquals(second, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
            assertEquals("Saved Undo", host.queryAccessibleAttribute(AccessibleAttribute.TEXT));

            host.executeAccessibleAction(AccessibleAction.COLLAPSE);

            assertFalse(host.isShowing());
            assertEquals(false, host.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        });
    }

    /// Verifies that snackbar hosts route focus to the current snackbar action and support Escape dismissal.
    @Test
    void snackbarHostSupportsAccessibleFocusAndKeyboardDismissal() {
        runOnFxThread(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            host.setDisplayDuration(Duration.INDEFINITE);
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            Pane root = new Pane(host);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 360.0, 140.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                host.show(snackbar);
                root.applyCss();
                root.layout();

                M3Button actionButton = assertInstanceOf(M3Button.class, snackbar.lookup(".m3-snackbar-action"));

                assertEquals(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                host.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(actionButton.isFocused());

                actionButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(host.isShowing());
                assertEquals(false, host.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that snackbar host default display duration resolves from motion behavior.
    @Test
    void snackbarHostDisplayDurationUsesMotionBehaviorDefault() {
        M3SnackbarHost host = new M3SnackbarHost();
        M3MotionBehavior behavior = M3MotionBehavior.create(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                Duration.millis(1234.0),
                Duration.millis(200.0),
                Duration.millis(1000.0),
                Duration.millis(200.0),
                Duration.millis(1400.0),
                Duration.millis(1332.0),
                Duration.millis(650.0),
                Duration.millis(4666.0)
        );

        M3MotionSettings.setMotionBehavior(host, behavior);

        assertEquals(Duration.millis(1234.0), host.getDisplayDuration());

        host.setDisplayDuration(Duration.INDEFINITE);

        assertEquals(Duration.INDEFINITE, host.getDisplayDuration());

        host.displayDurationProperty().set(null);

        assertEquals(Duration.millis(1234.0), host.getDisplayDuration());
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
        assertEquals("Dialog title Dialog body", dialogPane.getAccessibleText());
        assertEquals("Dialog title Dialog body", dialogPane.queryAccessibleAttribute(AccessibleAttribute.TEXT));
    }

    /// Verifies that dialog panes expose content and action buttons as indexed accessibility items.
    @Test
    void dialogPaneExposesIndexedAccessibleItems() {
        runOnFxThread(() -> {
            M3Button contentAction = new M3Button("Content action");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(contentAction);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Pane root = new Pane(dialogPane);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 220.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();

                Node cancelButton = dialogPane.lookupButton(ButtonType.CANCEL);
                Node okButton = dialogPane.lookupButton(ButtonType.OK);

                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(3, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(cancelButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
                assertNull(dialogPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, -1));
                assertEquals(contentAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(contentAction.isFocused());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, ButtonType.OK);
                assertTrue(okButton.isFocused());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(cancelButton.isFocused());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, contentAction);
                assertTrue(contentAction.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes fall back to the default action when content is not focusable.
    @Test
    void dialogPaneFocusNodeUsesDefaultActionWhenContentIsStatic() {
        runOnFxThread(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Label("Dialog body"));
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Pane root = new Pane(dialogPane);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 220.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();

                Node okButton = dialogPane.lookupButton(ButtonType.OK);

                assertEquals(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(okButton.isFocused());
            } finally {
                stage.close();
            }
        });
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
            assertEquals(M3ThemeManager.stylesheetUrl(), pane.getStylesheets().get(0));
            assertEquals(M3ThemeManager.themeStylesheetUrl(theme), pane.getStylesheets().get(1));

            dialog.setTheme(null);

            assertNull(dialog.getTheme());
            assertEquals("-fx-opacity: 0.9;", pane.getStyle());
            assertEquals(java.util.List.of(M3ThemeManager.stylesheetUrl()), pane.getStylesheets());
        });
    }

    /// Verifies that Material dialogs inherit the owner scene theme when they are shown.
    @Test
    void dialogInheritsOwnerSceneTheme() {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            try {
                Pane root = new Pane();
                Scene scene = new Scene(root);
                M3Theme theme = M3Theme.defaultTheme();
                M3Dialog<Void> dialog = new M3Dialog<>();
                M3DialogPane pane = dialog.getM3DialogPane();

                M3ThemeManager.install(scene, theme);
                owner.setScene(scene);
                dialog.initOwner(owner);
                Event.fireEvent(dialog, new DialogEvent(dialog, DialogEvent.DIALOG_SHOWING));

                assertNull(dialog.getTheme());
                assertTrue(pane.getStyle().contains("-m3-color-primary"));
                assertEquals(M3ThemeManager.themeStylesheetUrl(theme), pane.getStylesheets().get(1));
            } finally {
                owner.close();
            }
        });
    }

    /// Verifies that Material dialogs inherit a local owner node theme when they are shown.
    @Test
    void dialogInheritsOwnerNodeLocalTheme() {
        runOnFxThread(() -> {
            Label ownerNode = new Label("Open dialog");
            Pane localRoot = new Pane(ownerNode);
            Pane root = new Pane(localRoot);
            new Scene(root);
            M3Theme localTheme = M3Theme.defaultTheme();
            M3Dialog<Void> dialog = new M3Dialog<>();
            M3DialogPane pane = dialog.getM3DialogPane();

            M3ThemeManager.install(localRoot, localTheme);
            dialog.initOwner(ownerNode);
            Event.fireEvent(dialog, new DialogEvent(dialog, DialogEvent.DIALOG_SHOWING));

            assertNull(dialog.getTheme());
            assertTrue(pane.getStyle().contains("-m3-color-primary"));
            assertSame(localTheme, M3ThemeManager.getTheme(localRoot));
            assertEquals(M3ThemeManager.themeStylesheetUrl(localTheme), pane.getStylesheets().get(1));
        });
    }

    /// Verifies that dialogs initialized from detached owner nodes resolve the window owner before showing.
    @Test
    void dialogRefreshesDetachedOwnerNodeWindowBeforeShowing() {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            try {
                Label ownerNode = new Label("Open dialog");
                Pane root = new Pane(ownerNode);
                M3Dialog<Void> dialog = new M3Dialog<>();

                dialog.initOwner(ownerNode);

                assertNull(dialog.getOwner());

                owner.setScene(new Scene(root));
                Event.fireEvent(dialog, new DialogEvent(dialog, DialogEvent.DIALOG_SHOWING));

                assertSame(owner, dialog.getOwner());
            } finally {
                owner.close();
            }
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
        M3TextField textField = createTextField("Content", M3TextInputVariant.OUTLINED);
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
        M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
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
        M3TextArea textArea = createTextArea("Notes", M3TextInputVariant.OUTLINED);
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

    /// Verifies that text inputs expose an error pseudo-class state.
    @Test
    void textInputsExposeErrorState() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField();
        M3PasswordField passwordField = new M3PasswordField();
        M3TextArea textArea = new M3TextArea();

        assertInstanceOf(M3TextInput.class, textField);
        assertInstanceOf(M3TextInput.class, passwordField);
        assertInstanceOf(M3TextInput.class, textArea);
        assertFalse(textField.isError());
        assertFalse(passwordField.isError());
        assertFalse(textArea.isError());

        textField.setError(true);
        passwordField.setError(true);
        textArea.setError(true);

        assertTrue(textField.isError());
        assertTrue(passwordField.isError());
        assertTrue(textArea.isError());
        assertTrue(textField.getPseudoClassStates().contains(error));
        assertTrue(passwordField.getPseudoClassStates().contains(error));
        assertTrue(textArea.getPseudoClassStates().contains(error));

        textField.errorProperty().set(false);
        passwordField.errorProperty().set(false);
        textArea.errorProperty().set(false);

        assertFalse(textField.getPseudoClassStates().contains(error));
        assertFalse(passwordField.getPseudoClassStates().contains(error));
        assertFalse(textArea.getPseudoClassStates().contains(error));
    }

    /// Verifies that the shared text input API exposes common metric tokens.
    @Test
    void textInputInterfaceExposesSharedMetricTokens() {
        M3TextInput[] inputs = {
                new M3TextField(),
                new M3PasswordField(),
                new M3TextArea()
        };

        for (M3TextInput input : inputs) {
            input.setContainerHeight(72.0);
            input.setContainerShape(14.0);
            input.setHorizontalPadding(24.0);

            TextInputControl control = assertInstanceOf(TextInputControl.class, input);
            assertEquals(72.0, input.getContainerHeight(), 0.0001);
            assertEquals(14.0, input.getContainerShape(), 0.0001);
            assertEquals(24.0, input.getHorizontalPadding(), 0.0001);
            assertEquals(72.0, control.getPrefHeight(), 0.0001);
            assertEquals(24.0, control.getPadding().getLeft(), 0.0001);
            assertEquals(24.0, control.getPadding().getRight(), 0.0001);
            assertThrows(IllegalArgumentException.class, () -> input.setContainerHeight(-1.0));
            assertThrows(IllegalArgumentException.class, () -> input.setContainerShape(-1.0));
            assertThrows(IllegalArgumentException.class, () -> input.setHorizontalPadding(-1.0));
        }
    }

    /// Verifies that text input layouts expose supporting text, counters, and wrapped input state.
    @Test
    void textInputLayoutDisplaysSupportingTextAndCounter() {
        M3TextField textField = new M3TextField("abc");
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(5);

        applyCss(layout);

        assertInstanceOf(M3TextInputLayoutSkin.class, layout.getSkin());
        assertFalse(VBox.class.isAssignableFrom(M3TextInputLayout.class));
        assertEquals(textField, layout.getInput());
        assertEquals(textField, layout.getTextInput());
        assertEquals(3, layout.getCharacterCount());
        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertTrue(textField.getStyleClass().contains(M3TextInputLayout.INPUT_STYLE_CLASS));

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS)
        );
        Label counter = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.COUNTER_STYLE_CLASS));
        assertEquals("Helper text", supportingText.getText());
        assertEquals("3 / 5", counter.getText());
        assertTrue(supportingText.isManaged());
        assertTrue(counter.isManaged());

        textField.setDisable(true);

        assertTrue(assertInstanceOf(
                Node.class,
                layout.lookup("." + M3TextInputLayout.SUPPORTING_ROW_STYLE_CLASS)
        ).isDisable());
    }

    /// Verifies that text input layouts expose labels and update floating label state.
    @Test
    void textInputLayoutDisplaysFloatingLabel() {
        PseudoClass floating = PseudoClass.getPseudoClass("floating");
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Email", "Helper text");

        applyCss(layout);

        Label label = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS));
        assertEquals("Email", layout.getLabelText());
        assertEquals("Email", label.getText());
        assertEquals("Email Helper text", layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertFalse(layout.isLabelFloating());
        assertFalse(label.getPseudoClassStates().contains(floating));
        assertEquals(Pos.CENTER_LEFT, StackPane.getAlignment(label));
        assertEquals(8.0, textField.getPadding().getTop(), 0.0001);
        assertTrue(layout.getStyleClass().contains(M3TextInputVariant.FILLED.getStyleClass()));

        textField.setText("support@example.com");

        assertTrue(layout.isLabelFloating());
        assertTrue(label.getPseudoClassStates().contains(floating));
        assertEquals(Pos.TOP_LEFT, StackPane.getAlignment(label));
        assertEquals(20.0, textField.getPadding().getTop(), 0.0001);

        textField.setVariant(M3TextInputVariant.OUTLINED);

        assertTrue(layout.getStyleClass().contains(M3TextInputVariant.OUTLINED.getStyleClass()));

        textField.clear();

        assertFalse(layout.isLabelFloating());
        assertEquals(8.0, textField.getPadding().getTop(), 0.0001);
    }

    /// Verifies that filled text input labels and text keep stable vertical placement in a shown window.
    @Test
    void filledTextInputLayoutKeepsFloatingLabelAndTextAlignedInWindow() {
        runOnFxThread(() -> {
            M3TextField textField = createTextField("support@example.com", M3TextInputVariant.FILLED);
            textField.setPrefWidth(340.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField, "Email address");
            layout.setLabelText("Filled with text");
            layout.setLeading(new M3Icon("E"));
            layout.setClearButtonEnabled(true);
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(32);
            layout.setPrefWidth(340.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 130.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.resize(420.0, 130.0);
                root.layout();

                Region inputContainer = lookupRegion(layout, "." + M3TextInputLayout.INPUT_CONTAINER_STYLE_CLASS);
                Label label = assertInstanceOf(
                        Label.class,
                        layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS)
                );
                Text inputText = renderedTextNode(textField, "support@example.com");

                Bounds containerBounds = inputContainer.localToScene(inputContainer.getLayoutBounds());
                Bounds labelBounds = label.localToScene(label.getLayoutBounds());
                Bounds textBounds = inputText.localToScene(inputText.getLayoutBounds());

                assertEquals(containerBounds.getMinY() + 4.0, labelBounds.getMinY(), 1.0);
                assertTrue(labelBounds.getMaxY() + 3.0 <= textBounds.getMinY(),
                        () -> "labelBounds=" + labelBounds + ", textBounds=" + textBounds);
                assertTrue(textBounds.getMaxY() <= containerBounds.getMaxY() - 6.0,
                        () -> "textBounds=" + textBounds + ", containerBounds=" + containerBounds);

                WritableImage image = snapshotImageOnFxThread(root);
                assertSnapshotNodeContainsContrast(image, label, Color.WHITE, 0.04);
                assertSnapshotNodeContainsContrast(image, inputText, Color.WHITE, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-text-field-filled-label-alignment.png"
                ));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layout presentation changes use Material motion timelines.
    @Test
    void textInputLayoutAnimatesLabelClearButtonAndSupportingRow() {
        runOnFxThread(() -> {
            M3TextField textField = createTextField("", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(260.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setLabelText("Email");
            layout.setClearButtonEnabled(true);
            layout.setPrefWidth(260.0);

            Pane root = new Pane(layout);
            layout.resizeRelocate(20.0, 20.0, 260.0, 96.0);
            Scene scene = new Scene(root, 320.0, 140.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            Label label = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS));
            Path outline = assertInstanceOf(Path.class, layout.lookup("." + M3TextInputLayout.OUTLINE_STYLE_CLASS));
            assertEquals(0.0, outlineNotchGap(outline), 0.0001);

            textField.setText("alpha");

            Timeline labelAnimation = controlTimeline(layout, "labelAnimation");
            Timeline trailingAnimation = controlTimeline(layout, "trailingAnimation");
            M3IconButton clearButton = layout.getClearButton();
            labelAnimation.jumpTo(Duration.millis(75.0));
            trailingAnimation.jumpTo(Duration.millis(50.0));

            assertBetween(label.getOpacity(), 0.72, 1.0, "floating label opacity");
            assertBetween(Math.abs(label.getTranslateY()), 0.0, 4.0, "floating label translateY");
            assertTrue(outlineNotchGap(outline) > 0.5, () -> "outlineNotchGap=" + outlineNotchGap(outline));
            assertBetween(clearButton.getOpacity(), 0.0, 1.0, "clear button opacity");
            assertBetween(clearButton.getScaleX(), 0.86, 1.0, "clear button scaleX");
            assertBetween(clearButton.getScaleY(), 0.86, 1.0, "clear button scaleY");

            layout.setSupportingText("Helper text");

            Timeline supportingRowAnimation = controlTimeline(layout, "supportingRowAnimation");
            HBox supportingRow = assertInstanceOf(
                    HBox.class,
                    layout.lookup("." + M3TextInputLayout.SUPPORTING_ROW_STYLE_CLASS)
            );
            supportingRowAnimation.jumpTo(Duration.millis(50.0));

            assertBetween(supportingRow.getOpacity(), 0.0, 1.0, "supporting row opacity");
            assertBetween(Math.abs(supportingRow.getTranslateY()), 0.0, 4.0, "supporting row translateY");
            stopTimelines(labelAnimation, trailingAnimation, supportingRowAnimation);
        });
    }

    /// Verifies that text input layouts manage leading and trailing adornment slots.
    @Test
    void textInputLayoutManagesLeadingAndTrailingAdornments() {
        M3TextField textField = new M3TextField("abc");
        M3Icon leading = new M3Icon("S");
        M3IconButton trailing = new M3IconButton(new M3Icon("C"));
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Helper text");
        layout.setLeading(leading);
        layout.setTrailing(trailing);

        applyCss(layout);

        assertEquals(leading, layout.getLeading());
        assertEquals(trailing, layout.getTrailing());
        assertEquals(3, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(leading, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(textField, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(trailing, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
        assertEquals(leading, assertInstanceOf(
                StackPane.class,
                layout.lookup("." + M3TextInputLayout.LEADING_STYLE_CLASS)
        ).getChildren().get(0));
        assertEquals(trailing, assertInstanceOf(
                StackPane.class,
                layout.lookup("." + M3TextInputLayout.TRAILING_STYLE_CLASS)
        ).getChildren().get(0));
        assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

        layout.setTrailing(null);

        assertNull(layout.getTrailing());
        assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

        layout.setLeading(null);

        assertNull(layout.getLeading());
        assertEquals(1, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

        M3TextField widePaddingField = new M3TextField("wide");
        widePaddingField.setStyle("-m3-horizontal-padding: 64px;");
        M3TextInputLayout widePaddingLayout = new M3TextInputLayout(widePaddingField);
        widePaddingLayout.setLeading(new M3Icon("W"));

        applyCss(widePaddingLayout);

        assertEquals(64.0, widePaddingField.getPadding().getLeft(), 0.0001);
        assertEquals(64.0, widePaddingField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that text input layouts mirror logical adornments and floating label geometry in right-to-left mode.
    @Test
    void textInputLayoutMirrorsAdornmentsAndFloatingLabelForRightToLeft() {
        runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField, "Project name");
            layout.setLabelText("Outlined with text");
            layout.setLeading(new M3Icon("T"));
            layout.setTrailing(createIconButton("V"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(430.0, 140.0);
            root.layout();
            layout.layout();

            Region inputContainer = lookupRegion(layout, "." + M3TextInputLayout.INPUT_CONTAINER_STYLE_CLASS);
            Region leadingSlot = lookupRegion(layout, "." + M3TextInputLayout.LEADING_STYLE_CLASS);
            Region trailingSlot = lookupRegion(layout, "." + M3TextInputLayout.TRAILING_STYLE_CLASS);
            Label label = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS));
            Path outline = assertInstanceOf(Path.class, layout.lookup("." + M3TextInputLayout.OUTLINE_STYLE_CLASS));

            Bounds containerBounds = inputContainer.localToScene(inputContainer.getBoundsInLocal());
            Bounds leadingBounds = leadingSlot.localToScene(leadingSlot.getBoundsInLocal());
            Bounds trailingBounds = trailingSlot.localToScene(trailingSlot.getBoundsInLocal());
            Bounds labelBounds = label.localToScene(label.getBoundsInLocal());

            assertTrue(leadingBounds.getMinX() > containerBounds.getCenterX(),
                    () -> "leadingBounds=" + leadingBounds + ", containerBounds=" + containerBounds);
            assertTrue(trailingBounds.getMaxX() < containerBounds.getCenterX(),
                    () -> "trailingBounds=" + trailingBounds + ", containerBounds=" + containerBounds);
            assertTrue(labelBounds.getCenterX() > containerBounds.getCenterX(),
                    () -> "labelBounds=" + labelBounds + ", containerBounds=" + containerBounds);
            assertEquals(Pos.TOP_LEFT, StackPane.getAlignment(label));
            assertTrue(outlineNotchGap(outline) >= labelBounds.getWidth() - 1.0,
                    () -> "outlineNotchGap=" + outlineNotchGap(outline) + ", labelWidth=" + labelBounds.getWidth());

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, label, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-text-field-rtl.png"
            ));
        });
    }

    /// Verifies that text input trailing icon buttons keep square state layers and visible ripples.
    @Test
    void textInputLayoutTrailingIconButtonKeepsSquareRipple() {
        runOnFxThread(() -> {
            M3PasswordField passwordField = createPasswordField("Hello", M3TextInputVariant.OUTLINED);
            passwordField.setPrefWidth(320.0);
            M3IconButton trailingButton = createIconButton("V");

            M3TextInputLayout layout = new M3TextInputLayout(passwordField, "At least 8 characters");
            layout.setTrailing(trailingButton);
            layout.setPrefWidth(320.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 390.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(390.0, 140.0);
            root.layout();

            Region stateLayer = lookupRegion(trailingButton, ".m3-state-layer-container");
            assertEquals(40.0, trailingButton.getWidth(), 0.0001);
            assertEquals(40.0, trailingButton.getHeight(), 0.0001);
            assertEquals(40.0, stateLayer.getWidth(), 0.0001);
            assertEquals(40.0, stateLayer.getHeight(), 0.0001);

            trailingButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 20.0, 20.0, true));

            Region ripple = lookupRegion(trailingButton, ".m3-ripple");
            Timeline rippleAnimation = controlTimeline(stateLayer, "rippleAnimation");
            rippleAnimation.jumpTo(Duration.millis(120.0));
            assertTrue(ripple.getOpacity() > 0.0, () -> "ripple opacity=" + ripple.getOpacity());
            assertTrue(ripple.getScaleX() > 0.0, () -> "ripple scaleX=" + ripple.getScaleX());
            assertTrue(ripple.getScaleY() > 0.0, () -> "ripple scaleY=" + ripple.getScaleY());
            writeVisualSnapshot(snapshotImageOnFxThread(root), java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-text-field-trailing-ripple.png"
            ));

            trailingButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 20.0, 20.0, false));
        });
    }

    /// Verifies that text input layouts apply error state from error text and character overflow.
    @Test
    void textInputLayoutAppliesErrorStateFromTextAndCharacterLimit() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField("abcdef");
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(4);

        applyCss(layout);

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS)
        );
        Label counter = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.COUNTER_STYLE_CLASS));
        assertTrue(layout.isCharacterLimitExceeded());
        assertTrue(textField.isError());
        assertTrue(counter.getPseudoClassStates().contains(error));
        assertEquals("Helper text", supportingText.getText());
        assertEquals("6 / 4", counter.getText());

        layout.setErrorText("Too long");

        assertTrue(textField.isError());
        assertTrue(supportingText.getPseudoClassStates().contains(error));
        assertEquals("Too long", supportingText.getText());

        layout.setCharacterLimit(10);
        layout.setErrorText("");

        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertFalse(supportingText.getPseudoClassStates().contains(error));
        assertFalse(counter.getPseudoClassStates().contains(error));
        assertEquals("Helper text", supportingText.getText());
        assertEquals("6 / 10", counter.getText());
    }

    /// Verifies that text input layouts run configured validators and refresh active validation on edits.
    @Test
    void textInputLayoutValidatesWithConfiguredValidator() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Email", "Helper text");
        layout.setValidator((input, text) -> text.isBlank()
                ? "Email is required"
                : text.contains("@") ? null : "Use an email address");

        applyCss(layout);

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS)
        );
        assertTrue(layout.isValidateOnFocusLost());
        assertTrue(layout.isValidateOnTextChange());
        assertFalse(layout.isValidationActive());
        assertFalse(layout.isValidationError());
        assertEquals("", layout.getValidationErrorText());
        assertEquals("Helper text", supportingText.getText());
        assertFalse(textField.isError());

        assertFalse(layout.validate());

        assertTrue(layout.isValidationActive());
        assertTrue(layout.validationActiveProperty().get());
        assertTrue(layout.isValidationError());
        assertEquals("Email is required", layout.getValidationErrorText());
        assertEquals("Email is required", layout.validationErrorTextProperty().get());
        assertEquals("Email Email is required", layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals("Email is required", supportingText.getText());
        assertTrue(supportingText.getPseudoClassStates().contains(error));
        assertTrue(textField.isError());

        textField.setText("support");

        assertEquals("Use an email address", layout.getValidationErrorText());
        assertEquals("Use an email address", supportingText.getText());
        assertTrue(textField.isError());

        textField.setText("support@example.com");

        assertFalse(layout.isValidationError());
        assertEquals("", layout.getValidationErrorText());
        assertEquals("Helper text", supportingText.getText());
        assertFalse(supportingText.getPseudoClassStates().contains(error));
        assertFalse(textField.isError());

        layout.clearValidation();

        assertFalse(layout.isValidationActive());
        assertFalse(layout.isValidationError());
        assertEquals("Email Helper text", layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));
    }

    /// Verifies that visible supporting rows do not replay entry motion while validation refreshes during edits.
    @Test
    void textInputLayoutDoesNotFlickerVisibleValidationFeedbackOnEdits() {
        runOnFxThread(() -> {
            M3TextField textField = new M3TextField();
            textField.setPrefWidth(280.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField, "Email", "Helper text");
            layout.setValidator((input, text) -> text.contains("@") ? null : "Use an email address");
            layout.setPrefWidth(280.0);

            StackPane root = new StackPane(layout);
            Scene scene = new Scene(root, 360.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(360.0, 120.0);
            root.layout();

            HBox supportingRow = assertInstanceOf(
                    HBox.class,
                    layout.lookup("." + M3TextInputLayout.SUPPORTING_ROW_STYLE_CLASS)
            );

            assertFalse(layout.validate());
            assertEquals("Use an email address", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);

            textField.setText("support");

            assertEquals("Use an email address", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);

            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(32);
            assertVisibleSupportingRowIsStable(supportingRow);

            textField.setText("supportx");

            assertEquals("Use an email address", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);

            textField.setText("support@example.com");

            assertEquals("", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);
        });
    }

    /// Verifies that reusable text input validators cover common validation rules.
    @Test
    void textInputValidatorsProvideReusableRules() {
        M3TextField textField = new M3TextField();
        M3TextInputValidator validator = M3TextInputValidators.all(
                M3TextInputValidators.required("Required"),
                M3TextInputValidators.lengthBetween(3, 5, "Too short", "Too long"),
                M3TextInputValidators.pattern(Pattern.compile("[a-z]+"), "Letters only")
        );

        assertNull(M3TextInputValidators.none().validate(textField, ""));
        assertEquals("Required", validator.validate(textField, ""));
        assertEquals("Too short", validator.validate(textField, "ab"));
        assertEquals("Too long", validator.validate(textField, "abcdef"));
        assertEquals("Letters only", validator.validate(textField, "abc1"));
        assertNull(validator.validate(textField, "abc"));
        assertEquals("Predicate failed", M3TextInputValidators.predicate(
                (input, text) -> text.equals(input.getText()),
                "Predicate failed"
        ).validate(textField, "other"));
        assertThrows(IllegalArgumentException.class, () ->
                M3TextInputValidators.lengthBetween(4, 3, "Too short", "Too long"));
        assertThrows(IllegalArgumentException.class, () ->
                M3TextInputValidators.minLength(-1, "Too short"));
        assertThrows(NullPointerException.class, () ->
                M3TextInputValidators.all(M3TextInputValidators.none(), null));
    }

    /// Verifies that text input layouts evaluate an additional validation pipeline.
    @Test
    void textInputLayoutRunsAdditionalValidators() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Email", "Helper text");
        M3TextInputValidator emailValidator = M3TextInputValidators.pattern(
                Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                "Use an email address"
        );
        M3TextInputValidator maxLengthValidator = M3TextInputValidators.maxLength(12, "Email is too long");

        layout.setValidator(M3TextInputValidators.required("Email is required"));
        layout.addValidator(emailValidator);
        layout.addValidator(maxLengthValidator);

        applyCss(layout);

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS)
        );
        assertEquals(java.util.List.of(emailValidator, maxLengthValidator), layout.getValidators());

        assertFalse(layout.validate());

        assertEquals("Email is required", layout.getValidationErrorText());
        assertEquals("Email is required", supportingText.getText());
        assertTrue(supportingText.getPseudoClassStates().contains(error));
        assertTrue(textField.isError());

        textField.setText("support");

        assertEquals("Use an email address", layout.getValidationErrorText());

        textField.setText("support@example.com");

        assertEquals("Email is too long", layout.getValidationErrorText());

        layout.removeValidator(maxLengthValidator);

        assertEquals("", layout.getValidationErrorText());
        assertFalse(textField.isError());
        assertEquals("Helper text", supportingText.getText());

        layout.setValidators(maxLengthValidator);

        assertEquals("Email is too long", layout.getValidationErrorText());

        layout.clearValidators();

        assertEquals("", layout.getValidationErrorText());
        assertTrue(layout.getValidators().isEmpty());
    }

    /// Verifies that text input layouts can keep validation errors stable while editing.
    @Test
    void textInputLayoutCanDisableTextChangeValidationRefresh() {
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Email", "Helper text");
        layout.setValidateOnTextChange(false);
        layout.setValidator((input, text) -> text.contains("@") ? null : "Use an email address");

        applyCss(layout);

        assertFalse(layout.validate());
        assertEquals("Use an email address", layout.getValidationErrorText());

        textField.setText("support@example.com");

        assertEquals("Use an email address", layout.getValidationErrorText());
        assertTrue(textField.isError());

        assertTrue(layout.validate());

        assertEquals("", layout.getValidationErrorText());
        assertFalse(textField.isError());
    }

    /// Verifies that focus-loss validation activates configured validators.
    @Test
    void textInputLayoutValidatesOnFocusLoss() {
        runOnFxThread(() -> {
            M3TextField textField = new M3TextField();
            M3Button nextButton = new M3Button("Next");
            M3TextInputLayout layout = new M3TextInputLayout(textField, "Email", "Helper text");
            layout.setValidator((input, text) -> text.isBlank() ? "Email is required" : null);

            Pane root = new Pane(layout, nextButton);
            Scene scene = new Scene(root, 420.0, 160.0);
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.show();
                root.applyCss();

                textField.requestFocus();
                assertTrue(textField.isFocused());
                assertFalse(layout.isValidationActive());

                nextButton.requestFocus();

                assertFalse(textField.isFocused());
                assertTrue(layout.isValidationActive());
                assertEquals("Email is required", layout.getValidationErrorText());
                assertTrue(textField.isError());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts can enforce character limits and expose a clear button.
    @Test
    void textInputLayoutEnforcesCharacterLimitAndClearButton() {
        M3TextField textField = new M3TextField("abcdef");
        M3TextInputLayout layout = new M3TextInputLayout(textField, "Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(4);
        layout.setCharacterLimitEnforced(true);
        layout.setClearButtonEnabled(true);

        applyCss(layout);

        Label counter = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.COUNTER_STYLE_CLASS));
        StackPane trailingSlot = assertInstanceOf(
                StackPane.class,
                layout.lookup("." + M3TextInputLayout.TRAILING_STYLE_CLASS)
        );
        M3IconButton clearButton = layout.getClearButton();

        assertEquals("abcd", textField.getText());
        assertEquals(4, layout.getCharacterCount());
        assertTrue(layout.isCharacterLimitEnforced());
        assertTrue(layout.isClearButtonEnabled());
        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertEquals("4 / 4", counter.getText());
        assertTrue(clearButton.getStyleClass().contains(M3TextInputLayout.CLEAR_BUTTON_STYLE_CLASS));
        assertEquals("Clear text", clearButton.getAccessibleText());
        assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(textField, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(clearButton, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(clearButton, trailingSlot.getChildren().get(0));
        assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

        clearButton.fire();

        assertEquals("", textField.getText());
        assertEquals(1, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertTrue(trailingSlot.getChildren().isEmpty());
        assertEquals(16.0, textField.getPadding().getRight(), 0.0001);
        assertEquals("0 / 4", counter.getText());

        textField.setText("123456");

        assertEquals("1234", textField.getText());
        assertFalse(textField.isError());
        assertEquals("4 / 4", counter.getText());

        M3Icon customTrailing = new M3Icon("T");
        layout.setTrailing(customTrailing);

        assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(customTrailing, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(customTrailing, trailingSlot.getChildren().get(0));
    }

    /// Verifies that text input layouts reject unsupported input nodes and invalid limits.
    @Test
    void textInputLayoutValidatesInputAndCharacterLimit() {
        M3TextInputLayout layout = new M3TextInputLayout();

        assertThrows(IllegalArgumentException.class, () -> layout.setInput(new javafx.scene.control.TextField()));
        assertThrows(IllegalArgumentException.class, () -> layout.setCharacterLimit(-2));
    }

    /// Verifies that text input error styles resolve to the Material error color token.
    @Test
    void textInputErrorStylesUseErrorColor() {
        runOnFxThread(() -> {
            Color errorColor = Color.rgb(186, 26, 26);
            M3TextField filledField = new M3TextField("Filled error");
            filledField.setError(true);
            filledField.setPrefWidth(180.0);
            M3TextField outlinedField = createTextField("Outlined error", M3TextInputVariant.OUTLINED);
            outlinedField.setError(true);
            outlinedField.setPrefWidth(190.0);
            M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
            passwordField.setError(true);
            passwordField.setPrefWidth(160.0);
            M3TextArea textArea = createTextArea("Multiline\nerror", M3TextInputVariant.FILLED);
            textArea.setError(true);
            textArea.setPrefSize(240.0, 96.0);

            FlowPane row = new FlowPane(16.0, 16.0, filledField, outlinedField, passwordField, textArea);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 840.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(840.0, 240.0);
            row.layout();

            assertBorderBottomColor(filledField, errorColor);
            assertBorderColor(outlinedField, errorColor);
            assertBorderColor(passwordField, errorColor);
            assertBorderBottomColor(textArea, errorColor);

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeBorderContainsContrast(image, filledField, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, passwordField, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, textArea, Color.WHITE, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-input-errors.png"
            ));
        });
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
        assertEquals("Installed", target.getAccessibleHelp());

        tooltip.setText("Updated");
        assertEquals("Updated", target.getAccessibleHelp());

        M3Tooltip.uninstall(target, tooltip);
        assertNull(target.getAccessibleHelp());

        Label targetWithHelp = new Label("Target");
        targetWithHelp.setAccessibleHelp("Existing help");
        M3Tooltip restoredTooltip = M3Tooltip.install(targetWithHelp, "Temporary help");

        assertEquals("Temporary help", targetWithHelp.getAccessibleHelp());

        M3Tooltip.uninstall(targetWithHelp, restoredTooltip);
        assertEquals("Existing help", targetWithHelp.getAccessibleHelp());
    }

    /// Verifies that installed tooltips open from keyboard focus and close from Escape.
    @Test
    void tooltipInstalledOnFocusSupportsKeyboardDismissal() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<Label> targetReference = new AtomicReference<>();
        AtomicReference<M3Tooltip> tooltipReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(40.0),
                () -> {
                    Label target = new Label("Target");
                    target.setFocusTraversable(true);
                    M3Tooltip tooltip = M3Tooltip.install(target, "Installed");
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Pane(target), 240.0, 120.0));
                    stage.show();
                    target.requestFocus();

                    stageReference.set(stage);
                    targetReference.set(target);
                    tooltipReference.set(tooltip);
                },
                () -> {
                    Stage stage = stageReference.get();
                    Label target = targetReference.get();
                    M3Tooltip tooltip = tooltipReference.get();
                    try {
                        assertTrue(target.isFocused());
                        assertTrue(tooltip.isShowing());

                        target.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                        assertFalse(tooltip.isShowing());
                    } finally {
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that installed tooltip popups inherit the owner's local motion settings.
    @Test
    void tooltipPopupInheritsOwnerMotionSettings() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<M3Button> actionReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(80.0),
                () -> {
                    Stage stage = new Stage();
                    Label target = new Label("Target");
                    M3Button action = createButton("Action", M3ButtonVariant.TEXT);
                    M3RichTooltip tooltip = M3RichTooltip.install(
                            target,
                            "Title",
                            "Supporting text",
                            action
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);

                    Pane root = new Pane(target);
                    M3MotionSettings.setAnimationsEnabled(root, false);
                    M3MotionSettings.setMotionScheme(root, M3MotionScheme.expressive());
                    M3MotionSettings.setMotionBehavior(root, M3MotionBehavior.expressive());
                    stage.setScene(new Scene(root, 240.0, 120.0));
                    stage.show();
                    root.applyCss();
                    root.layout();

                    target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));

                    stageReference.set(stage);
                    tooltipReference.set(tooltip);
                    actionReference.set(action);
                },
                () -> {
                    Stage stage = stageReference.get();
                    M3RichTooltip tooltip = tooltipReference.get();
                    M3Button action = actionReference.get();
                    try {
                        assertTrue(tooltip.isShowing());
                        Node tooltipRoot = tooltip.getScene().getRoot();

                        assertFalse(M3MotionSettings.areAnimationsEnabled(tooltipRoot));
                        assertEquals(
                                M3MotionScheme.expressive().defaultEffects().easing(),
                                M3Animation.defaultEffects(action).easing()
                        );
                        assertEquals(
                                M3MotionBehavior.expressive().richTooltipShowDuration(),
                                M3Animation.motionBehavior(action).richTooltipShowDuration()
                        );
                    } finally {
                        tooltip.hide();
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that rich tooltips expose graphic-only Material content.
    @Test
    void richTooltipUsesMaterialGraphicContent() {
        Label action = new Label("Action");
        M3RichTooltip tooltip = new M3RichTooltip("Title", "Supporting text", action);

        assertTrue(tooltip.getStyleClass().contains(M3Tooltip.STYLE_CLASS));
        assertTrue(tooltip.getStyleClass().contains(M3RichTooltip.STYLE_CLASS));
        assertEquals(ContentDisplay.GRAPHIC_ONLY, tooltip.getContentDisplay());
        assertEquals("Title", tooltip.getTitle());
        assertEquals("Supporting text", tooltip.getSupportingText());
        assertEquals("Title Supporting text", tooltip.getText());
        assertEquals(action, tooltip.getActions().get(0));
        assertInstanceOf(VBox.class, tooltip.getGraphic());

        tooltip.setTitle("");
        assertEquals("Supporting text", tooltip.getText());
        tooltip.setSupportingText("");
        assertEquals("", tooltip.getText());

        Label replacement = new Label("Replacement");
        tooltip.setActions(replacement);
        assertEquals(replacement, tooltip.getActions().get(0));
        tooltip.clearActions();
        assertTrue(tooltip.getActions().isEmpty());

        assertThrows(NullPointerException.class, () -> tooltip.setTitle(null));
        assertThrows(NullPointerException.class, () -> tooltip.setSupportingText(null));
        assertThrows(NullPointerException.class, () -> tooltip.addAction(null));
    }

    /// Verifies that tooltip component tokens apply profile-specific popup metrics.
    @Test
    void tooltipAppliesProfileMetrics() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            Stage stage = new Stage();
            M3Tooltip plainTooltip = new M3Tooltip("Details");
            M3RichTooltip richTooltip = new M3RichTooltip("Title", "Supporting text", new M3Button("Action"));
            try {
                Label owner = new Label("Owner");
                StackPane ownerRoot = new StackPane(owner);
                Scene scene = new Scene(ownerRoot, 320.0, 160.0);
                M3Theme expressiveTheme = M3Theme.fromSeed(
                        Color.web("#006a6a"),
                        M3Profile.EXPRESSIVE_2025,
                        Brightness.LIGHT
                );
                M3ThemeManager.install(scene, expressiveTheme);
                stage.setScene(scene);
                stage.show();

                plainTooltip.setTheme(expressiveTheme);
                plainTooltip.show(owner, stage.getX() + 24.0, stage.getY() + 48.0);
                plainTooltip.getScene().getRoot().applyCss();
                Region plainRoot = assertInstanceOf(Region.class, plainTooltip.getSkin().getNode());
                assertEquals(8.0, plainRoot.getPadding().getTop(), 0.0001);
                assertEquals(12.0, plainRoot.getPadding().getLeft(), 0.0001);
                assertEquals(
                        10.0,
                        plainRoot.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                        0.0001
                );

                richTooltip.setTheme(expressiveTheme);
                richTooltip.show(owner, stage.getX() + 24.0, stage.getY() + 88.0);
                richTooltip.getScene().getRoot().applyCss();
                Parent richRoot = assertInstanceOf(Parent.class, richTooltip.getSkin().getNode());
                VBox richContainer = assertInstanceOf(
                        VBox.class,
                        richRoot.lookup("." + M3RichTooltip.CONTAINER_STYLE_CLASS)
                );
                assertEquals(16.0, richContainer.getPadding().getTop(), 0.0001);
                assertEquals(20.0, richContainer.getPadding().getLeft(), 0.0001);
                assertEquals(12.0, richContainer.getSpacing(), 0.0001);
                assertEquals(360.0, richContainer.getPrefWidth(), 0.0001);
                assertEquals(
                        16.0,
                        richContainer.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                        0.0001
                );
                HBox actions = assertInstanceOf(HBox.class, richContainer.lookup("." + M3RichTooltip.ACTIONS_STYLE_CLASS));
                M3Button actionButton = assertInstanceOf(M3Button.class, richTooltip.getActions().get(0));
                assertEquals(12.0, actions.getSpacing(), 0.0001);
                assertEquals(36.0, actionButton.getContainerHeight(), 0.0001);
                assertEquals(16.0, actionButton.getHorizontalPadding(), 0.0001);
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                plainTooltip.hide();
                richTooltip.hide();
                stage.close();
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Throwable throwable = failure.get();
        if (throwable != null) {
            throw new AssertionError(throwable);
        }
    }

    /// Verifies that rich tooltips install on nodes and expose combined accessible help.
    @Test
    void richTooltipInstallsOnNodes() {
        Label target = new Label("Target");
        M3RichTooltip tooltip = M3RichTooltip.install(target, "Title", "Supporting text");

        assertEquals("Title Supporting text", target.getAccessibleHelp());

        tooltip.setTitle("Updated");
        assertEquals("Updated Supporting text", target.getAccessibleHelp());

        tooltip.setSupportingText("");
        assertEquals("Updated", target.getAccessibleHelp());

        M3RichTooltip.uninstall(target, tooltip);
        assertNull(target.getAccessibleHelp());
    }

    /// Verifies that rich tooltip popup hover keeps action content reachable.
    @Test
    void richTooltipStaysOpenWhilePopupIsHovered() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3RichTooltip> tooltipReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(260.0),
                () -> {
                    Stage stage = new Stage();
                    Label target = new Label("Target");
                    M3RichTooltip tooltip = M3RichTooltip.install(
                            target,
                            "Title",
                            "Supporting text",
                            createButton("Action", M3ButtonVariant.TEXT)
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);

                    Pane root = new Pane(target);
                    stage.setScene(new Scene(root, 240.0, 120.0));
                    stage.show();
                    root.applyCss();
                    root.layout();

                    tooltip.show(target, stage.getX() + 32.0, stage.getY() + 72.0);
                    Node tooltipRoot = tooltip.getScene().getRoot();
                    target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 4.0, 4.0, false));
                    tooltipRoot.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));

                    stageReference.set(stage);
                    tooltipReference.set(tooltip);
                },
                () -> {
                    Stage stage = stageReference.get();
                    M3RichTooltip tooltip = tooltipReference.get();
                    try {
                        assertTrue(tooltip.isShowing());
                    } finally {
                        tooltip.hide();
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that tooltips can apply and clear inline theme declarations.
    @Test
    void tooltipAppliesAndClearsTheme() {
        M3Tooltip tooltip = new M3Tooltip("Details");
        M3Theme theme = M3Theme.defaultTheme();

        tooltip.setStyle("-fx-opacity: 0.9;");
        tooltip.setTheme(theme);

        assertEquals(theme, tooltip.getTheme());
        assertTrue(tooltip.getStyle().contains("-fx-opacity: 0.9;"));
        assertTrue(tooltip.getStyle().contains("-m3-color-primary"));
        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));

        tooltip.setTheme(null);

        assertNull(tooltip.getTheme());
        assertEquals("-fx-opacity: 0.9;", tooltip.getStyle());
        assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
    }

    /// Verifies that installed tooltips inherit the target node scene theme.
    @Test
    void tooltipInheritsInstalledSceneTheme() {
        Label attachedTarget = new Label("Attached target");
        Label delayedTarget = new Label("Delayed target");
        Label uninstalledTarget = new Label("Uninstalled target");
        Pane root = new Pane(attachedTarget);
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        M3ThemeManager.install(scene, theme);

        M3Tooltip attachedTooltip = M3Tooltip.install(attachedTarget, "Installed");
        M3Tooltip delayedTooltip = M3Tooltip.install(delayedTarget, "Delayed");
        M3Tooltip uninstalledTooltip = M3Tooltip.install(uninstalledTarget, "Uninstalled");
        M3Tooltip.uninstall(uninstalledTarget, uninstalledTooltip);

        assertEquals(theme, attachedTooltip.getTheme());
        assertTrue(attachedTooltip.getStyle().contains("-m3-color-primary"));
        assertTrue(attachedTooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(attachedTooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertNull(delayedTooltip.getTheme());

        root.getChildren().add(delayedTarget);
        root.getChildren().add(uninstalledTarget);

        assertEquals(theme, delayedTooltip.getTheme());
        assertTrue(delayedTooltip.getStyle().contains("-m3-color-primary"));
        assertTrue(delayedTooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(delayedTooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertNull(uninstalledTooltip.getTheme());
    }

    /// Verifies that installed tooltips inherit local parent themes when the scene has no theme.
    @Test
    void tooltipInheritsLocalParentTheme() {
        Label target = new Label("Target");
        Pane localRoot = new Pane(target);
        Pane root = new Pane(localRoot);
        new Scene(root);
        M3Theme localTheme = M3Theme.defaultTheme();

        M3ThemeManager.install(localRoot, localTheme);

        M3Tooltip tooltip = M3Tooltip.install(target, "Installed");

        assertSame(localTheme, tooltip.getTheme());
        assertTrue(tooltip.getStyle().contains("-m3-color-primary"));
        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
    }

    /// Verifies that installed tooltips keep detached local themes after the target enters a scene.
    @Test
    void tooltipInheritsLocalParentThemeAfterSceneAttachment() {
        Label target = new Label("Target");
        Pane localRoot = new Pane(target);
        M3Theme localTheme = M3Theme.defaultTheme();

        M3ThemeManager.install(localRoot, localTheme);

        M3Tooltip tooltip = M3Tooltip.install(target, "Installed");

        assertSame(localTheme, tooltip.getTheme());

        Pane root = new Pane(localRoot);
        new Scene(root);

        assertSame(localTheme, tooltip.getTheme());
        assertTrue(tooltip.getStyle().contains("-m3-color-primary"));
    }

    /// Verifies that avatars swap between text and graphic content.
    @Test
    void avatarOwnsTextAndGraphicContent() {
        M3Avatar avatar = new M3Avatar("AB");
        Label graphic = new Label("G");
        graphic.setAccessibleText("Graphic avatar");

        applyCss(avatar);

        assertEquals("AB", avatar.getText());
        assertEquals("AB", avatar.getAccessibleText());
        assertInstanceOf(M3AvatarSkin.class, avatar.getSkin());
        assertInstanceOf(Label.class, avatar.lookup("." + M3Avatar.LABEL_STYLE_CLASS));

        avatar.setText("CD");
        assertEquals("CD", avatar.getAccessibleText());

        avatar.setGraphic(graphic);

        assertEquals(graphic, avatar.getGraphic());
        assertInstanceOf(StackPane.class, graphic.getParent());
        assertNull(avatar.lookup("." + M3Avatar.LABEL_STYLE_CLASS));
        assertEquals("Graphic avatar", avatar.getAccessibleText());

        avatar.setGraphic(null);

        assertNull(avatar.getGraphic());
        assertNull(graphic.getParent());
        assertInstanceOf(Label.class, avatar.lookup("." + M3Avatar.LABEL_STYLE_CLASS));
        assertEquals("CD", avatar.getAccessibleText());
    }

    /// Verifies that avatar component token metrics apply through the active theme.
    @Test
    void avatarAppliesTokenMetrics() {
        M3Avatar avatar = new M3Avatar("A");
        avatar.setStyle("-m3-container-size: 48px;");

        applyCss(avatar);

        double preferredWidth = avatar.prefWidth(-1.0);
        double preferredHeight = avatar.prefHeight(-1.0);

        assertEquals(48.0, avatar.getContainerSize(), 0.0001);
        assertEquals(48.0, preferredWidth, 0.0001,
                () -> "containerSize=" + avatar.getContainerSize() + ", prefWidthProperty=" + avatar.getPrefWidth());
        assertEquals(48.0, preferredHeight, 0.0001,
                () -> "containerSize=" + avatar.getContainerSize() + ", prefHeightProperty=" + avatar.getPrefHeight());

        avatar.setContainerSize(32.0);

        assertEquals(32.0, avatar.prefWidth(-1.0), 0.0001);
        assertEquals(32.0, avatar.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that avatar variants update style classes.
    @Test
    void avatarVariantUpdatesStyleClasses() {
        M3Avatar avatar = createAvatar("A", M3AvatarVariant.SECONDARY);

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
        applyCss(icon);

        assertEquals(M3IconSize.LARGE, icon.getSize());
        assertEquals(M3IconVariant.PRIMARY, icon.getVariant());
        assertInstanceOf(M3IconSkin.class, icon.getSkin());
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
        assertEquals(35.0, icon.getPrefWidth(), 0.0001);
        assertEquals(35.0, icon.getPrefHeight(), 0.0001);
        assertTrue(icon.getPrefHeight() > icon.getIconSize());
    }

    /// Verifies that fallback text glyphs keep a padded line box instead of touching clipped edges.
    @Test
    void iconFallbackGlyphKeepsClearSnapshotEdges() {
        runOnFxThread(() -> {
            M3Icon icon = new M3Icon("M", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            StackPane root = new StackPane(icon);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 80.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(80.0, 80.0);
            root.layout();

            assertEquals(18.0, icon.getIconSize(), 0.0001);
            assertEquals(23.0, icon.getPrefWidth(), 0.0001);
            assertEquals(23.0, icon.getPrefHeight(), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, icon, Color.WHITE, 0.05);
            assertSnapshotNodeEdgesClear(image, icon, Color.WHITE, 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-icon-fallback-glyph.png"
            ));
        });
    }

    /// Verifies that icon size roles provide default size tokens through user-agent CSS.
    @Test
    void iconSizeRoleAppliesDefaultCssToken() {
        M3Icon icon = new M3Icon("A", M3IconSize.LARGE, M3IconVariant.PRIMARY);

        applyCss(icon);

        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(32.0, icon.getFont().getSize(), 0.0001);
    }

    /// Verifies that disclosure icons expose expanded state and animate their arrow rotation.
    @Test
    void disclosureIconAnimatesExpandedState() throws InterruptedException {
        AtomicReference<SVGPath> shape = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(220.0),
                () -> {
                    M3DisclosureIcon icon = new M3DisclosureIcon();
                    Pane root = new Pane(icon);
                    Scene scene = new Scene(root, 80.0, 80.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    icon.resize(24.0, 24.0);
                    icon.layout();

                    assertFalse(icon.isExpanded());
                    assertTrue(icon.getStyleClass().contains(M3DisclosureIcon.STYLE_CLASS));
                    assertInstanceOf(M3DisclosureIconSkin.class, icon.getSkin());
                    assertEquals(24.0, icon.prefWidth(-1), 0.0001);
                    assertEquals(24.0, icon.prefHeight(-1), 0.0001);

                    SVGPath arrow = assertInstanceOf(SVGPath.class, icon.lookup(".m3-disclosure-icon-shape"));
                    assertEquals(-90.0, arrow.getRotate(), 0.0001);
                    shape.set(arrow);

                    icon.setExpanded(true);
                },
                () -> assertEquals(0.0, shape.get().getRotate(), 0.01)
        );
    }

    /// Verifies that collapsed disclosure icons point toward logical child content in right-to-left layouts.
    @Test
    void disclosureIconMirrorsCollapsedDirectionForRightToLeft() {
        M3DisclosureIcon leftToRightIcon = new M3DisclosureIcon();
        M3DisclosureIcon rightToLeftIcon = new M3DisclosureIcon();
        rightToLeftIcon.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        HBox root = new HBox(leftToRightIcon, rightToLeftIcon);
        Scene scene = new Scene(root, 80.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.layout();

        SVGPath leftToRightArrow = assertInstanceOf(
                SVGPath.class,
                leftToRightIcon.lookup(".m3-disclosure-icon-shape")
        );
        SVGPath rightToLeftArrow = assertInstanceOf(
                SVGPath.class,
                rightToLeftIcon.lookup(".m3-disclosure-icon-shape")
        );
        assertEquals(-90.0, leftToRightArrow.getRotate(), 0.0001);
        assertEquals(90.0, rightToLeftArrow.getRotate(), 0.0001);
    }

    /// Verifies that disclosure icons render inside fixed list item icon slots without clipping.
    @Test
    void disclosureIconFitsNavigationDrawerSlot() {
        runOnFxThread(() -> {
            M3DisclosureIcon disclosure = new M3DisclosureIcon(true);
            M3ListItem groupItem = new M3ListItem("Date & time pickers");
            groupItem.setTrailingMedia(disclosure, M3ListItemSlotSize.ICON);
            groupItem.setSelected(true);
            groupItem.setStyle("-m3-one-line-height: 56px; -m3-container-shape: 28px; -m3-horizontal-padding: 24px;");

            M3ListItem firstChild = new M3ListItem("Date pickers");
            firstChild.setStyle("-m3-one-line-height: 56px; -m3-container-shape: 28px; -m3-horizontal-padding: 32px;");
            M3ListItem secondChild = new M3ListItem("Time pickers");
            secondChild.setStyle("-m3-one-line-height: 56px; -m3-container-shape: 28px; -m3-horizontal-padding: 32px;");

            M3NavigationDrawer drawer = new M3NavigationDrawer(groupItem, firstChild, secondChild);
            drawer.setAllowEmptySelection(true);
            drawer.setPrefWidth(320.0);

            Pane root = new Pane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 220.0);
            root.layout();

            StackPane trailingSlot = assertInstanceOf(
                    StackPane.class,
                    groupItem.lookup(".m3-list-item-trailing")
            );
            SVGPath arrow = assertInstanceOf(
                    SVGPath.class,
                    disclosure.lookup(".m3-disclosure-icon-shape")
            );

            assertEquals(24.0, trailingSlot.getLayoutBounds().getWidth(), 0.0001);
            assertEquals(24.0, trailingSlot.getLayoutBounds().getHeight(), 0.0001);
            assertEquals(0.0, arrow.getRotate(), 0.0001);
            assertNodeInsideParent(trailingSlot, arrow);

            Node groupHeadline = Objects.requireNonNull(groupItem.lookup(".m3-list-item-headline"));
            Node childHeadline = Objects.requireNonNull(firstChild.lookup(".m3-list-item-headline"));
            assertTrue(
                    childHeadline.localToScene(childHeadline.getBoundsInLocal()).getMinX()
                            > groupHeadline.localToScene(groupHeadline.getBoundsInLocal()).getMinX() + 6.0
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, disclosure, Color.WHITE, 0.05);
        });
    }

    /// Verifies that icon button factories create configured M3FX icon graphics.
    @Test
    void iconButtonFactoryCreatesIconGraphic() {
        M3IconButton button = createIconButton("A", M3IconSize.SMALL, M3IconVariant.ERROR);
        M3Icon icon = assertInstanceOf(M3Icon.class, button.getGraphic());

        assertEquals(M3IconSize.SMALL, icon.getSize());
        assertEquals(M3IconVariant.ERROR, icon.getVariant());
    }

    /// Verifies that toggle icon button variants and selected states update style classes.
    @Test
    void iconToggleButtonVariantAndSelectionUpdateState() {
        M3IconToggleButton button = createIconToggleButton(
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

    /// Verifies that toggle icon button graphics stay centered in their fixed touch target.
    @Test
    void iconToggleButtonCentersGraphicContent() {
        runOnFxThread(() -> {
            M3IconToggleButton standard = new M3IconToggleButton("S");
            M3IconToggleButton tonal = createIconToggleButton("B", M3IconToggleButtonVariant.TONAL, true);
            M3IconToggleButton outlined = createIconToggleButton("O", M3IconToggleButtonVariant.OUTLINED, false);
            FlowPane row = new FlowPane(12.0, 12.0, standard, tonal, outlined);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 220.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(220.0, 96.0);
            row.layout();

            for (M3IconToggleButton button : java.util.List.of(standard, tonal, outlined)) {
                assertEquals(Pos.CENTER, button.getAlignment());
                assertEquals(ContentDisplay.GRAPHIC_ONLY, button.getContentDisplay());
                assertNodeCentersAligned(button, Objects.requireNonNull(button.getGraphic()), 0.75);
            }

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, standard, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, tonal, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, outlined, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-icon-toggle-centering.png"
            ));
        });
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

        group.selectPrevious();

        assertEquals(first, group.getSelectedButton());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        group.selectNext();

        assertEquals(second, group.getSelectedButton());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());

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

    /// Verifies that toggle icon button group spacing is styleable from CSS.
    @Test
    void iconToggleButtonGroupSpacingTokenIsStyleable() {
        M3IconToggleButtonGroup group =
                new M3IconToggleButtonGroup(new M3IconToggleButton("A"), new M3IconToggleButton("B"));
        group.setStyle("-m3-icon-toggle-button-group-spacing: 12px;");

        applyCss(group);

        assertEquals(12.0, group.getSpacing(), 0.0001);
    }

    /// Verifies that text typography roles update style classes.
    @Test
    void textRoleUpdatesStyleClasses() {
        M3Text text = new M3Text("Title");

        applyCss(text);

        assertEquals(M3TextRole.BODY_LARGE, text.getRole());
        assertTrue(text.getStyleClass().contains(M3Text.STYLE_CLASS));
        assertInstanceOf(M3TextSkin.class, text.getSkin());
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
        assertEquals(1, surface.getContent().size());
        assertInstanceOf(M3SurfaceSkin.class, surface.getSkin());
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

        assertInstanceOf(M3MenuSkin.class, menu.getSkin());
        assertEquals(8.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(48.0, open.getOneLineHeight(), 0.0001);
        assertEquals(4.0, open.getContainerShape(), 0.0001);
        assertEquals(12.0, open.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, open.getContentSpacing(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(10.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(56.0, open.getOneLineHeight(), 0.0001);
        assertEquals(10.0, open.getContainerShape(), 0.0001);
        assertEquals(16.0, open.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, open.getContentSpacing(), 0.0001);
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

    /// Verifies that menu section headers are non-selectable menu content.
    @Test
    void menuSectionHeadersAreNonSelectableContent() {
        M3MenuSectionHeader fileHeader = new M3MenuSectionHeader("File");
        M3Divider divider = new M3Divider();
        M3MenuItem open = new M3MenuItem("Open");
        M3MenuItem save = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(fileHeader, open, divider, save);

        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals("File", fileHeader.getText());
        assertFalse(fileHeader.isFocusTraversable());
        assertEquals(4, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(fileHeader, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(open, menu.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(2));
        menu.selectIndex(1);
        assertEquals(open, menu.getSelectedItem());
        menu.selectIndex(3);
        assertEquals(save, menu.getSelectedItem());

        menu.selectNext();

        assertEquals(open, menu.getSelectedItem());

        menu.selectPrevious();

        assertEquals(save, menu.getSelectedItem());
    }

    /// Verifies that menu section headers receive menu-specific metrics.
    @Test
    void menuSectionHeaderUsesMenuStylesheet() {
        M3MenuSectionHeader header = new M3MenuSectionHeader("Recent");
        M3Menu menu = new M3Menu(header);
        Pane root = new Pane(menu);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertTrue(header.getStyleClass().contains(M3MenuSectionHeader.STYLE_CLASS));
        assertEquals(32.0, header.getPrefHeight(), 0.0001);
        assertEquals(12.0, header.getPadding().getTop(), 0.0001);
        assertEquals(4.0, header.getPadding().getBottom(), 0.0001);
    }

    /// Verifies that menu section headers and dividers render as grouped menu content.
    @Test
    void menuSectionHeaderSnapshotRendersGroupedContent() {
        runOnFxThread(() -> {
            M3MenuSectionHeader fileHeader = new M3MenuSectionHeader("File");
            M3MenuItem open = new M3MenuItem("Open", new M3Icon("O"));
            M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
            M3MenuItem save = new M3MenuItem("Save", new M3Icon("S"));
            M3MenuSectionHeader recentHeader = new M3MenuSectionHeader("Recent");
            M3MenuItem project = new M3MenuItem("Project Alpha", new M3Icon("A"));
            project.setSelected(true);
            M3Menu menu = new M3Menu(
                    fileHeader,
                    open,
                    export,
                    save,
                    new M3Divider(),
                    recentHeader,
                    project
            );
            menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
            menu.setPrefWidth(280.0);
            FlowPane root = new FlowPane(menu);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 340.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(340.0, 260.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, fileHeader, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, export, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, recentHeader, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, project, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-menu-section-headers.png"
            ));
        });
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

        menu.selectNext();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, menu.getSelectedItem());
        assertEquals(1, menu.getSelectedIndex());

        menu.selectPrevious();

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

    /// Verifies that submenu items own nested menu content and expose submenu accessibility state.
    @Test
    void subMenuItemOwnsNestedMenuContentAndAccessibleState() {
        M3MenuItem exportPdf = new M3MenuItem("PDF");
        M3MenuItem exportHtml = new M3MenuItem("HTML");
        M3SubMenuItem export = new M3SubMenuItem("Export", exportPdf, exportHtml);

        assertTrue(export.getStyleClass().contains(M3SubMenuItem.STYLE_CLASS));
        assertEquals(2, export.getItems().size());
        assertEquals(exportPdf, export.getItems().get(0));
        assertEquals(export.getSubMenu(), export.queryAccessibleAttribute(AccessibleAttribute.SUBMENU));
        assertEquals(false, export.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(2, export.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(exportHtml, export.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertInstanceOf(M3Icon.class, export.getTrailing());

        export.clearItems();

        assertTrue(export.getItems().isEmpty());
    }

    /// Verifies that the default submenu indicator mirrors in right-to-left layouts.
    @Test
    void subMenuItemMirrorsDefaultIndicatorForRightToLeft() {
        M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
        export.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        applyCss(export);

        M3Icon indicator = assertInstanceOf(M3Icon.class, export.getTrailing());
        assertEquals("<", indicator.getText());
    }

    /// Verifies that submenu items are focusable menu items but do not participate in parent menu selection.
    @Test
    void menuSelectionSkipsSubMenuItems() {
        M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
        M3MenuItem open = new M3MenuItem("Open");
        M3MenuItem save = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(export, open, save);

        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(open, menu.getSelectedItem());
        assertFalse(export.isSelected());
        assertThrows(IllegalArgumentException.class, () -> menu.select(export));
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(0));

        menu.selectNext();

        assertEquals(save, menu.getSelectedItem());

        export.setSelected(true);

        assertFalse(export.isSelected());
        assertEquals(save, menu.getSelectedItem());
    }

    /// Verifies that submenu item actions open locally and submenu child actions bubble to an owning menu.
    @Test
    void subMenuItemConsumesOwnActionAndForwardsNestedActions() {
        M3MenuItem pdf = new M3MenuItem("PDF");
        M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
        M3Menu menu = new M3Menu(export);
        AtomicInteger menuActions = new AtomicInteger();
        menu.addEventHandler(ActionEvent.ACTION, event -> menuActions.incrementAndGet());

        export.fire();

        assertEquals(0, menuActions.get());

        pdf.fire();

        assertEquals(1, menuActions.get());
    }

    /// Verifies that submenu hover opens and closes the submenu popup after Material delays.
    @Test
    void subMenuItemHoverOpensAndClosesPopup() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3SubMenuItem> itemReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(260.0), () -> {
                Stage stage = new Stage();
                M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3Menu menu = new M3Menu(export);
                Pane root = new Pane(menu);
                Scene scene = new Scene(root, 320.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(320.0, 220.0);
                root.layout();

                stageReference.set(stage);
                itemReference.set(export);
                export.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 8.0, 8.0, false));
            }, () -> {
                M3SubMenuItem item = itemReference.get();
                assertTrue(item.isSubMenuShowing());

                M3Menu subMenu = item.getSubMenu();
                subMenu.applyCss();
                subMenu.resize(subMenu.prefWidth(-1.0), subMenu.prefHeight(-1.0));
                subMenu.layout();
                assertFalse(subMenu.getItems().isEmpty());
                assertTrue(subMenu.getWidth() > 0.0);
                assertTrue(subMenu.getHeight() > 0.0);

                WritableImage image = snapshotImageOnFxThread(subMenu);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-submenu-popup.png"
                ));
            });

            runOnFxThreadAfterDelay(Duration.millis(420.0), () ->
                    itemReference.get().fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 8.0, 8.0, false)),
                    () -> assertFalse(itemReference.get().isSubMenuShowing()));
        } finally {
            runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that popup positioning flips submenus away from screen edges.
    @Test
    void popupPositioningFlipsSubMenusAwayFromScreenEdges() {
        Rectangle2D screen = new Rectangle2D(0.0, 0.0, 300.0, 200.0);
        M3PopupPositioning.Placement rightPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(40.0, 40.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0
        );
        M3PopupPositioning.Placement leftPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(250.0, 40.0, 40.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0
        );
        M3PopupPositioning.Placement bottomClampedPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(40.0, 180.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0
        );

        assertFalse(rightPlacement.opensToLeft());
        assertEquals(119.0, rightPlacement.x(), 0.0001);
        assertTrue(leftPlacement.opensToLeft());
        assertEquals(131.0, leftPlacement.x(), 0.0001);
        assertEquals(120.0, bottomClampedPlacement.y(), 0.0001);
    }

    /// Verifies that popup positioning prefers the logical leading side for right-to-left submenus.
    @Test
    void popupPositioningPrefersLeftForRightToLeftSubMenus() {
        Rectangle2D screen = new Rectangle2D(0.0, 0.0, 360.0, 200.0);
        M3PopupPositioning.Placement rightToLeftPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(160.0, 40.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0,
                true
        );
        M3PopupPositioning.Placement fallbackPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(16.0, 40.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0,
                true
        );

        assertTrue(rightToLeftPlacement.opensToLeft());
        assertEquals(41.0, rightToLeftPlacement.x(), 0.0001);
        assertFalse(fallbackPlacement.opensToLeft());
    }

    /// Verifies that popup positioning flips menu popups above their owner near the bottom edge.
    @Test
    void popupPositioningFlipsMenusAboveBottomEdges() {
        Rectangle2D screen = new Rectangle2D(0.0, 0.0, 300.0, 200.0);
        M3PopupPositioning.Placement belowPlacement = M3PopupPositioning.menuBelowOrAbove(
                new BoundingBox(40.0, 40.0, 80.0, 32.0),
                screen,
                140.0,
                80.0,
                4.0
        );
        M3PopupPositioning.Placement abovePlacement = M3PopupPositioning.menuBelowOrAbove(
                new BoundingBox(40.0, 170.0, 80.0, 24.0),
                screen,
                140.0,
                80.0,
                4.0
        );

        assertFalse(belowPlacement.opensAbove());
        assertEquals(76.0, belowPlacement.y(), 0.0001);
        assertTrue(abovePlacement.opensAbove());
        assertEquals(86.0, abovePlacement.y(), 0.0001);
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
        menuButton.addItems(first, second);

        menuButton.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menuButton.setAllowEmptySelection(false);
        menuButton.selectIndex(1);

        assertEquals(M3MenuSelectionMode.SINGLE, menuButton.getSelectionMode());
        assertFalse(menuButton.isAllowEmptySelection());
        assertEquals(second, menuButton.getSelectedItem());
        assertEquals(java.util.List.of(second), menuButton.getSelectedItems());
        assertEquals(1, menuButton.getSelectedIndex());

        menuButton.selectPrevious();

        assertEquals(first, menuButton.getSelectedItem());

        menuButton.selectNext();

        assertEquals(second, menuButton.getSelectedItem());

        menuButton.selectLast();

        assertEquals(second, menuButton.getSelectedItem());

        menuButton.clearSelection();

        assertEquals(second, menuButton.getSelectedItem());
        assertTrue(second.isSelected());

        menuButton.clearItems();

        assertTrue(menuButton.getItems().isEmpty());
    }

    /// Verifies that menu button keyboard shortcuts are safe before the button is attached to a scene.
    @Test
    void menuButtonKeyboardShortcutsAreSafeBeforeSceneAttachment() {
        M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("First"));

        menuButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        menuButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
        menuButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertFalse(menuButton.isShowing());
    }

    /// Verifies that popup menus activate focused items and reveal requested menu content.
    @Test
    void popupMenusActivateFocusedItemsAndRevealRequestedContent() {
        runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3Menu menu = new M3Menu(open, export);
            AtomicInteger actions = new AtomicInteger();
            open.setOnAction(event -> actions.incrementAndGet());
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(menu), 240.0, 160.0));
                stage.show();

                open.requestFocus();
                open.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

                assertEquals(1, actions.get());

                export.requestFocus();
                export.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(export.isSubMenuShowing());
                assertTrue(pdf.isFocused());
            } finally {
                export.hideSubMenu();
                stage.close();
            }
        });

        runOnFxThread(() -> {
            M3MenuItem first = new M3MenuItem("First");
            M3MenuItem second = new M3MenuItem("Second");
            M3MenuButton menuButton = new M3MenuButton("More", first, second);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(menuButton), 240.0, 120.0));
                stage.show();

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, second);

                assertTrue(menuButton.isShowing());
                assertTrue(second.isFocused());
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that popup menu buttons preserve menu type-ahead navigation across show and hide cycles.
    @Test
    void menuButtonPopupSupportsTypeAheadKeyboardNavigation() {
        runOnFxThread(() -> {
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem settings = new M3MenuItem("Settings");
            M3MenuItem share = new M3MenuItem("Share");
            M3MenuButton menuButton = new M3MenuButton("More", archive, settings, share);
            menuButton.setSelectionMode(M3MenuSelectionMode.SINGLE);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 320.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                menuButton.getMenu().fireEvent(keyTypedEvent("s"));
                menuButton.getMenu().fireEvent(keyTypedEvent("e"));

                assertTrue(settings.isFocused());
                assertEquals(settings, menuButton.getSelectedItem());

                menuButton.hideMenu();
                menuButton.showMenu();
                menuButton.getMenu().fireEvent(keyTypedEvent("a"));

                assertTrue(archive.isFocused());
                assertEquals(archive, menuButton.getSelectedItem());
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu button popups inherit a locally installed parent theme.
    @Test
    void menuButtonPopupInheritsLocalParentThemeContext() {
        runOnFxThread(() -> {
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("More", archive);
            M3Theme localTheme = M3Theme.defaultTheme();
            Stage stage = new Stage();
            try {
                Pane localRoot = new Pane(menuButton);
                Pane root = new Pane(localRoot);
                M3ThemeManager.install(localRoot, localTheme);
                stage.setScene(new Scene(root, 320.0, 160.0));
                stage.show();
                root.applyCss();
                localRoot.resizeRelocate(0.0, 0.0, 320.0, 160.0);
                menuButton.resizeRelocate(24.0, 24.0, 120.0, 40.0);
                root.layout();

                menuButton.showMenu();

                assertTrue(menuButton.isShowing());
                assertSame(localTheme, M3ThemeManager.getTheme(menuButton.getMenu()));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu button accessibility focus requests follow the active popup focus branch.
    @Test
    void menuButtonRoutesAccessibleFocusAcrossPopupBranches() {
        runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem save = new M3MenuItem("Save");
            M3MenuButton menuButton = new M3MenuButton("More", open, save);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 260.0, 140.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(menuButton.isFocused());

                menuButton.showMenu();
                assertEquals(menuButton, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(open.isFocused());
                assertEquals(open, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                save.requestFocus();
                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(save.isFocused());
                assertEquals(save, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that accessibility item requests can open nested menu branches and focus descendants.
    @Test
    void popupMenusOpenNestedBranchesForAccessibleTargets() {
        runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3MenuButton menuButton = new M3MenuButton("More", export);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, pdf);

                assertTrue(menuButton.isShowing());
                assertTrue(export.isSubMenuShowing());
                assertTrue(pdf.isFocused());
                assertEquals(pdf, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(pdf.isFocused());
            } finally {
                export.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that popup menu branches inherit theme color lookups without CSS conversion warnings.
    @Test
    void popupMenuBranchesResolveThemeColorLookups() {
        Logger logger = Logger.getLogger("javafx.scene.CssStyleHelper");
        List<LogRecord> cssWarnings = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    cssWarnings.add(record);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        logger.addHandler(handler);
        try {
            runOnFxThread(() -> {
                M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));
                M3MenuItem archive = new M3MenuItem("Archive");
                M3MenuItem inbox = new M3MenuItem("Inbox");
                M3SubMenuItem moveTo = new M3SubMenuItem("Move to", archive, inbox);
                M3MenuButton menuButton = new M3MenuButton(
                        "Open menu",
                        new M3MenuSectionHeader("Document"),
                        new M3MenuItem("Duplicate"),
                        moveTo
                );
                menuButton.setVariant(M3ButtonVariant.OUTLINED);
                Pane root = new Pane(menuButton);
                Scene scene = new Scene(root, 420.0, 260.0);
                Stage stage = new Stage();

                try {
                    M3ThemeManager.install(scene, theme);
                    stage.setScene(scene);
                    stage.show();
                    root.applyCss();
                    root.layout();

                    menuButton.showMenu();
                    moveTo.showSubMenu();

                    M3Menu menu = menuButton.getMenu();
                    M3Menu subMenu = moveTo.getSubMenu();
                    menu.applyCss();
                    menu.layout();
                    subMenu.applyCss();
                    subMenu.layout();

                    assertTrue(menuButton.isShowing());
                    assertTrue(moveTo.isSubMenuShowing());
                    assertTrue(subMenu.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                    assertTrue(subMenu.getStylesheets().contains(M3ThemeManager.themeStylesheetUrl(theme)));
                    assertResolvedBackgroundFill(menu, "menu");
                    assertResolvedBackgroundFill(subMenu, "submenu");
                    assertResolvedListItemTextFill(moveTo);
                    assertResolvedListItemTextFill(archive);
                    assertResolvedListItemTextFill(inbox);
                } finally {
                    moveTo.hideSubMenu();
                    menuButton.hideMenu();
                    stage.close();
                }
            });
        } finally {
            logger.removeHandler(handler);
        }

        assertTrue(cssWarnings.isEmpty(), () -> cssWarnings.stream()
                .map(record -> record.getLevel() + ": " + record.getMessage())
                .collect(Collectors.joining("\n")));
    }

    /// Verifies that static lists support printable-key type-ahead focus and single selection.
    @Test
    void listPaneSupportsTypeAheadKeyboardNavigation() {
        runOnFxThread(() -> {
            M3ListItem archive = new M3ListItem("Archive");
            M3ListItem settings = new M3ListItem("Settings");
            M3ListItem search = new M3ListItem("Search");
            settings.setDisable(true);
            M3ListPane listPane = new M3ListPane(archive, settings, search);
            listPane.setSelectionMode(M3ListSelectionMode.SINGLE);
            Pane root = new Pane(listPane);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 180.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listPane.requestFocus();

                listPane.fireEvent(keyTypedEvent("s"));

                assertTrue(search.isFocused());
                assertSame(search, listPane.getSelectedItem());
                assertSame(search, listPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listPane.fireEvent(keyTypedEvent("a"));

                assertTrue(archive.isFocused());
                assertSame(archive, listPane.getSelectedItem());
                assertSame(archive, listPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized lists support printable-key type-ahead focus and single selection.
    @Test
    void listViewSupportsTypeAheadKeyboardNavigation() {
        runOnFxThread(() -> {
            M3ListView<String> listView = new M3ListView<>("Archive", "Settings", "Search", "Reports");
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            Pane root = new Pane(listView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 180.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                listView.resize(280.0, 160.0);
                root.applyCss();
                root.layout();
                listView.requestFocus();

                listView.fireEvent(keyTypedEvent("s"));
                listView.fireEvent(keyTypedEvent("e"));

                assertEquals(2, listView.getFocusedIndex());
                assertEquals(2, listView.getSelectedIndex());
                assertEquals("Search", listView.getSelectedItem());

                listView.fireEvent(keyTypedEvent("r"));

                assertEquals(3, listView.getFocusedIndex());
                assertEquals(3, listView.getSelectedIndex());
                assertEquals("Reports", listView.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list type-ahead uses M3ListItem headline text when data items are list items.
    @Test
    void listViewTypeAheadUsesListItemHeadlineText() {
        runOnFxThread(() -> {
            M3ListItem overview = new M3ListItem("Overview");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem disabled = new M3ListItem("Search");
            disabled.setDisable(true);
            M3ListItem sheets = new M3ListItem("Sheets");
            M3ListView<M3ListItem> listView = new M3ListView<>(overview, buttons, disabled, sheets);
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            Pane root = new Pane(listView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                listView.resize(280.0, 180.0);
                root.applyCss();
                root.layout();
                listView.requestFocus();

                listView.fireEvent(keyTypedEvent("s"));

                assertEquals(3, listView.getFocusedIndex());
                assertEquals(sheets, listView.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized lists expose the visible focused row as their accessibility focus node.
    @Test
    void listViewReportsVisibleFocusedRowForAccessibility() {
        runOnFxThread(() -> {
            M3ListView<String> listView = new M3ListView<>("Archive", "Settings", "Search");
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            Pane root = new Pane(listView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                listView.resize(280.0, 180.0);
                root.applyCss();
                root.layout();

                listView.focusIndex(1);
                root.applyCss();
                root.layout();

                Object focusNode = listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);

                assertInstanceOf(M3ListItem.class, focusNode);
                assertEquals("Settings", ((M3ListItem) focusNode).getHeadlineText());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers support printable-key type-ahead across expanded groups.
    @Test
    void navigationDrawerSupportsTypeAheadKeyboardNavigation() {
        runOnFxThread(() -> {
            M3ListItem overview = new M3ListItem("Overview");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem segmentedButtons = new M3ListItem("Segmented buttons");
            M3ListItem splitButtons = new M3ListItem("Split buttons");
            group.addItems(buttons, segmentedButtons, splitButtons);
            group.setExpanded(true);
            segmentedButtons.setDisable(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(overview, group);
            Pane root = new Pane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 260.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.requestFocus();

                drawer.fireEvent(keyTypedEvent("s"));

                assertTrue(splitButtons.isFocused());
                assertSame(splitButtons, drawer.getSelectedItem());
                assertSame(splitButtons, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                drawer.fireEvent(keyTypedEvent("o"));

                assertTrue(overview.isFocused());
                assertSame(overview, drawer.getSelectedItem());
                assertSame(overview, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that drawer group disclosure keys keep focus on visible rows.
    @Test
    void navigationDrawerGroupDisclosureKeysRestoreHeaderFocus() {
        runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem iconButtons = new M3ListItem("Icon buttons");
            group.addItems(buttons, iconButtons);
            group.setExpanded(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
            Pane root = new Pane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 260.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                iconButtons.requestFocus();
                assertTrue(iconButtons.isFocused());

                iconButtons.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertFalse(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());
                assertSame(group.getHeaderItem(), drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                group.getHeaderItem().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());

                drawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                iconButtons.requestFocus();
                drawer.select(iconButtons);

                iconButtons.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertFalse(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());

                group.getHeaderItem().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that menu keyboard focus can land on submenu items without corrupting menu selection.
    @Test
    void menuKeyboardNavigationFocusesSubMenuItemsWithoutChangingSelection() {
        runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem save = new M3MenuItem("Save");
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3Menu menu = new M3Menu(export, open, save);
            menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
            menu.select(open);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menu);
                stage.setScene(new Scene(root, 240.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                open.requestFocus();
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));

                assertTrue(export.isFocused());
                assertEquals(open, menu.getSelectedItem());
                assertEquals(export, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(open.isFocused());
                assertEquals(open, menu.getSelectedItem());

                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(save.isFocused());
                assertEquals(save, menu.getSelectedItem());

                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(export.isSubMenuShowing());
                assertTrue(pdf.isFocused());
                assertEquals(pdf, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(pdf, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                export.hideSubMenu();
                stage.close();
            }
        });
    }

    /// Verifies that submenu keyboard open and close keys mirror in right-to-left menus.
    @Test
    void menuKeyboardNavigationMirrorsSubMenuKeysForRightToLeft() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3SubMenuItem> exportReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(220.0), () -> {
                M3MenuItem pdf = new M3MenuItem("PDF");
                M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
                M3Menu menu = new M3Menu(export);
                menu.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                Stage stage = new Stage();
                Pane root = new Pane(menu);
                stage.setScene(new Scene(root, 240.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                exportReference.set(export);
                export.requestFocus();
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(export.isSubMenuShowing());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, export.getSubMenu().getNodeOrientation());
                assertTrue(pdf.isFocused());

                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
            }, () -> {
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());

                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3SubMenuItem export = exportReference.get();
                if (export != null) {
                    export.hideSubMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that opening one submenu closes sibling submenus and keeps focus in the active branch.
    @Test
    void siblingSubMenusAreMutuallyExclusive() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3SubMenuItem> exportReference = new AtomicReference<>();
        AtomicReference<M3SubMenuItem> shareReference = new AtomicReference<>();
        AtomicReference<M3MenuItem> emailReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(220.0), () -> {
                M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3MenuItem email = new M3MenuItem("Email");
                M3SubMenuItem share = new M3SubMenuItem("Share", email);
                M3Menu menu = new M3Menu(export, share);
                Stage stage = new Stage();
                Pane root = new Pane(menu);
                stage.setScene(new Scene(root, 320.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                exportReference.set(export);
                shareReference.set(share);
                emailReference.set(email);

                assertTrue(export.showSubMenuAndFocusFirstItem());
                assertTrue(export.isSubMenuShowing());
                assertTrue(share.showSubMenuAndFocusFirstItem());
            }, () -> {
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());
                M3SubMenuItem share = Objects.requireNonNull(shareReference.get());
                M3MenuItem email = Objects.requireNonNull(emailReference.get());

                assertFalse(export.isSubMenuShowing());
                assertTrue(share.isSubMenuShowing());
                assertTrue(email.isFocused());
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3SubMenuItem export = exportReference.get();
                if (export != null) {
                    export.hideSubMenu();
                }
                @Nullable M3SubMenuItem share = shareReference.get();
                if (share != null) {
                    share.hideSubMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that submenu dismissal returns focus to the owning item without closing the parent popup.
    @Test
    void subMenuEscapeReturnsFocusToOwnerItem() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3MenuButton> buttonReference = new AtomicReference<>();
        AtomicReference<M3SubMenuItem> exportReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(220.0), () -> {
                M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3MenuButton menuButton = new M3MenuButton("More", export);
                Stage stage = new Stage();
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                buttonReference.set(menuButton);
                exportReference.set(export);

                menuButton.showMenu();
                assertTrue(export.showSubMenuAndFocusFirstItem());
                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            }, () -> {
                M3MenuButton menuButton = Objects.requireNonNull(buttonReference.get());
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());

                assertTrue(menuButton.isShowing());
                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertEquals(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3MenuButton menuButton = buttonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that submenu actions close the owning popup menu and return focus to the menu button.
    @Test
    void subMenuActionClosesMenuButtonPopup() throws InterruptedException {
        AtomicReference<Stage> stageReference = new AtomicReference<>();
        AtomicReference<M3MenuButton> buttonReference = new AtomicReference<>();
        AtomicReference<M3SubMenuItem> exportReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(260.0), () -> {
                M3MenuItem pdf = new M3MenuItem("PDF");
                M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
                M3MenuButton menuButton = new M3MenuButton("More", export);
                Stage stage = new Stage();
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                buttonReference.set(menuButton);
                exportReference.set(export);

                menuButton.showMenu();
                assertTrue(export.showSubMenuAndFocusFirstItem());
                assertEquals(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pdf.fire();
            }, () -> {
                M3MenuButton menuButton = Objects.requireNonNull(buttonReference.get());
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());

                assertFalse(export.isSubMenuShowing());
                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3MenuButton menuButton = buttonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that search bars delegate text and action APIs to their embedded editor.
    @Test
    void searchBarDelegatesTextAndActions() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        Label clear = new Label("Clear");
        Label filter = new Label("Filter");
        AtomicInteger actions = new AtomicInteger();
        searchBar.setOnAction(event -> actions.incrementAndGet());

        searchBar.setText("M3FX");
        searchBar.addTrailingAction(clear);
        searchBar.setTrailingActions(filter);
        searchBar.fire();

        assertEquals("M3FX", searchBar.getText());
        assertEquals("M3FX", searchBar.getEditor().getText());
        assertEquals("Search", searchBar.getPromptText());
        assertEquals(java.util.List.of(filter), searchBar.getTrailingActions());
        assertEquals(1, actions.get());

        searchBar.clearTrailingActions();

        assertTrue(searchBar.getTrailingActions().isEmpty());
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

    /// Verifies that search bars leave active input state from the Escape key.
    @Test
    void searchBarHandlesEscapeAndClearDeactivate() {
        M3SearchBar searchBar = new M3SearchBar("Search");

        searchBar.setText("M3FX");
        searchBar.activate();
        searchBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertEquals("M3FX", searchBar.getText());
        assertFalse(searchBar.isActive());

        searchBar.activate();
        searchBar.clearAndDeactivate();

        assertEquals("", searchBar.getText());
        assertFalse(searchBar.isActive());
    }

    /// Verifies that search bars expose their slots as indexed accessibility items.
    @Test
    void searchBarExposesIndexedAccessibleSlots() {
        runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            Label leading = new Label("S");
            M3Button clear = new M3Button("Clear");
            M3Button filter = new M3Button("Filter");
            searchBar.setLeading(leading);
            searchBar.setTrailingActions(clear, filter);

            Pane root = new Pane(searchBar);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 120.0));
                stage.show();
                root.applyCss();

                assertEquals(searchBar.getEditor(), searchBar.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(4, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(leading, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(searchBar.getEditor(), searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals(clear, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
                assertEquals(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 3));
                assertNull(searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 4));

                searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(searchBar.isActive());
                assertTrue(searchBar.getEditor().isFocused());
                assertSame(searchBar.getEditor(), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

                assertTrue(searchBar.getEditor().isFocused());
                assertSame(searchBar.getEditor(), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, filter);

                assertTrue(filter.isFocused());
                assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.COLLAPSE);
                assertFalse(searchBar.isActive());
                searchBar.executeAccessibleAction(AccessibleAction.EXPAND);
                assertTrue(searchBar.isActive());
            } finally {
                stage.close();
            }
        });
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
        assertInstanceOf(M3SearchBarSkin.class, searchBar.getSkin());
        assertInstanceOf(M3SearchViewSkin.class, searchView.getSkin());
        assertFalse(HBox.class.isAssignableFrom(M3SearchBar.class));
        assertFalse(VBox.class.isAssignableFrom(M3SearchView.class));
        assertEquals(56.0, result.getOneLineHeight(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(64.0, searchBar.getPrefHeight(), 0.0001);
        assertEquals(20.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(20.0, searchBar.getPadding().getRight(), 0.0001);
        assertEquals(64.0, result.getOneLineHeight(), 0.0001);
        assertEquals(20.0, result.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, result.getContentSpacing(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getBottom(), 0.0001);
    }

    /// Verifies that search views propagate right-to-left orientation to their embedded content.
    @Test
    void searchViewPropagatesRightToLeftOrientationToSearchBarAndResults() {
        M3ListItem result = new M3ListItem("Result");
        M3SearchView searchView = new M3SearchView("Search", result);
        searchView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(searchView);
        Scene scene = new Scene(root, 360.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        searchView.resize(320.0, 120.0);
        searchView.layout();

        assertEquals(NodeOrientation.RIGHT_TO_LEFT, searchView.getSearchBar().getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, result.getEffectiveNodeOrientation());
    }

    /// Verifies that search bars keep their token height when a flow row contains taller controls.
    @Test
    void searchBarKeepsPreferredHeightInTallFlowRows() {
        runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setPrefWidth(260.0);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            FlowPane root = new FlowPane(16.0, 16.0, searchBar, datePicker);
            Scene scene = new Scene(root, 760.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            root.resize(760.0, 420.0);
            root.layout();

            assertTrue(datePicker.getHeight() > searchBar.getPrefHeight() * 2.0);
            assertEquals(64.0, searchBar.getPrefHeight(), 0.0001);
            assertEquals(64.0, searchBar.getHeight(), 0.0001);
        });
    }

    /// Verifies that search views own a search bar and mutable result list.
    @Test
    void searchViewOwnsSearchBarAndResults() {
        M3ListItem result = new M3ListItem("Result");
        M3ListItem replacement = new M3ListItem("Replacement");
        M3SearchView searchView = new M3SearchView("Find", result);
        Label leading = new Label("S");
        M3IconButton trailing = createIconButton("C");
        AtomicInteger actions = new AtomicInteger();

        searchView.setLeading(leading);
        searchView.addTrailingAction(trailing);
        searchView.addResult(replacement);
        searchView.setResults(result);
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

        searchView.clearAndDeactivate();
        searchView.clearResults();
        searchView.clearTrailingActions();

        assertEquals("", searchView.getText());
        assertEquals("", searchView.getEditor().getText());
        assertFalse(searchView.isActive());
        assertTrue(searchView.getResults().isEmpty());
        assertTrue(searchView.getTrailingActions().isEmpty());
    }

    /// Verifies that search views use active state to show or hide results.
    @Test
    void searchViewActiveStateControlsResultsVisibility() throws InterruptedException {
        M3SearchView searchView = new M3SearchView("Find");
        M3ListItem result = new M3ListItem("Result");
        searchView.getResults().add(result);
        applyCss(searchView);

        assertTrue(searchView.isActive());
        assertTrue(searchView.getSearchBar().isActive());

        searchView.deactivate();

        Node results = searchView.lookup("." + M3SearchView.RESULTS_STYLE_CLASS);
        assertFalse(searchView.isActive());
        assertTrue(results.isVisible());
        assertTrue(results.isManaged());
        assertTrue(results.getOpacity() <= 1.0);

        runOnFxThreadAfterDelay(Duration.millis(180.0), () -> {
        }, () -> {
            assertFalse(results.isVisible());
            assertFalse(results.isManaged());
            assertEquals(0.0, results.getOpacity(), 0.0001);
            assertTrue(results.getTranslateY() < 0.0);
        });

        searchView.activate();

        assertTrue(searchView.isActive());
        assertTrue(results.isVisible());
        assertTrue(results.isManaged());
    }

    /// Verifies that search views move keyboard focus between the editor and results.
    @Test
    void searchViewKeyboardNavigatesResults() {
        runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("First");
            M3ListItem second = new M3ListItem("Second");
            M3SearchView searchView = new M3SearchView("Search", first, second);

            Pane root = new Pane(searchView);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 240.0));
                stage.show();
                root.applyCss();

                searchView.getEditor().requestFocus();
                assertTrue(searchView.getEditor().isFocused());
                assertSame(searchView.getEditor(), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.getEditor().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(second.isFocused());
                assertSame(second, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(searchView.getEditor().isFocused());
                assertSame(searchView.getEditor(), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, second);
                assertTrue(second.isFocused());
                assertSame(second, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
                assertFalse(searchView.isActive());
                assertTrue(searchView.getSearchBar().isFocused());

                searchView.getSearchBar().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                assertTrue(searchView.isActive());
                assertTrue(searchView.getEditor().isFocused());
            } finally {
                stage.close();
            }
        });
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

    /// Verifies that modal sheets dismiss from the Escape key.
    @Test
    void modalSheetsHideFromEscapeKey() {
        M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Side"));
        M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Bottom"));
        M3SideSheet standardSideSheet = new M3SideSheet("Pinned", new Label("Side"));
        M3BottomSheet standardBottomSheet = new M3BottomSheet("Pinned", new Label("Bottom"));
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
        runOnFxThread(() -> {
            M3Button trigger = new M3Button("Open");
            M3Button sideAction = new M3Button("Side action");
            M3Button bottomAction = new M3Button("Bottom action");
            M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Side"), sideAction);
            M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Bottom"), bottomAction);
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

    /// Verifies that sheets expose accessible state, content, and visibility actions.
    @Test
    void sheetsExposeAccessibleStateAndActions() {
        Label sideContent = new Label("Side");
        Label bottomContent = new Label("Bottom");
        M3SideSheet sideSheet = new M3SideSheet("Details", sideContent);
        M3BottomSheet bottomSheet = new M3BottomSheet("Queue", bottomContent);

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
        runOnFxThread(() -> {
            M3Button sideContent = new M3Button("Side content");
            M3Button sideAction = new M3Button("Side action");
            M3SideSheet sideSheet = new M3SideSheet("Details", sideContent, sideAction);

            M3Button bottomContent = new M3Button("Bottom content");
            M3Button bottomAction = new M3Button("Bottom action");
            M3BottomSheet bottomSheet = new M3BottomSheet("Queue", bottomContent, bottomAction);

            Stage stage = new Stage();
            try {
                sideSheet.hide();
                bottomSheet.hide();
                stage.setScene(new Scene(new VBox(sideSheet, bottomSheet), 480.0, 360.0));
                stage.show();

                assertEquals(sideContent, sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(bottomContent, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

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
    @Test
    void sheetControlsSupportAnimatedShownState() throws InterruptedException {
        M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Side"));
        M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Bottom"));
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

        runOnFxThreadAfterDelay(Duration.millis(280.0), () -> {
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

        runOnFxThreadAfterDelay(Duration.millis(380.0), () -> {
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
        M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Content"));
        M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Content"));
        Pane root = new Pane(sideSheet, bottomSheet);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(360.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(320.0, bottomSheet.getPrefHeight(), 0.0001);
        assertInstanceOf(M3SideSheetSkin.class, sideSheet.getSkin());
        assertInstanceOf(M3BottomSheetSkin.class, bottomSheet.getSkin());
        assertFalse(javafx.scene.layout.BorderPane.class.isAssignableFrom(M3SideSheet.class));
        assertFalse(javafx.scene.layout.BorderPane.class.isAssignableFrom(M3BottomSheet.class));
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

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(384.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(360.0, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(
                28.0,
                lookupRegion(sideSheet, "." + M3SideSheet.CONTENT_STYLE_CLASS).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                28.0,
                lookupRegion(bottomSheet, "." + M3BottomSheet.CONTENT_STYLE_CLASS).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                36.0,
                lookupRegion(bottomSheet, "." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS).getPrefWidth(),
                0.0001
        );
        assertEquals(
                5.0,
                lookupRegion(bottomSheet, "." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS).getPrefHeight(),
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

    /// Verifies configurable scrim opacity and pointer dismissal behavior.
    @Test
    void scrimSupportsVisibilityOptions() {
        M3Scrim scrim = new M3Scrim();
        AtomicInteger actions = new AtomicInteger();
        scrim.setOnAction(event -> actions.incrementAndGet());

        scrim.setVisibleOpacity(0.48);
        assertEquals(0.48, scrim.getVisibleOpacity(), 0.0001);
        assertEquals(0.48, scrim.getOpacity(), 0.0001);

        scrim.hide();
        assertEquals(0.0, scrim.getOpacity(), 0.0001);
        scrim.show();
        assertEquals(0.48, scrim.getOpacity(), 0.0001);

        scrim.setDismissOnClick(false);
        scrim.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        assertEquals(0, actions.get());

        scrim.setDismissOnClick(true);
        scrim.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        assertEquals(1, actions.get());

        assertThrows(IllegalArgumentException.class, () -> scrim.setVisibleOpacity(-0.1));
        assertThrows(IllegalArgumentException.class, () -> scrim.setVisibleOpacity(1.1));
    }

    /// Verifies that scrims expose visibility actions and keyboard dismissal.
    @Test
    void scrimSupportsAccessibleVisibilityAndKeyboardActions() {
        M3Scrim scrim = new M3Scrim();
        AtomicInteger actions = new AtomicInteger();
        scrim.setOnAction(event -> actions.incrementAndGet());

        assertEquals(true, scrim.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        scrim.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertFalse(scrim.isShown());
        assertEquals(false, scrim.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        scrim.executeAccessibleAction(AccessibleAction.EXPAND);
        assertTrue(scrim.isShown());
        assertEquals(scrim, scrim.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        KeyEvent enter = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER);
        KeyEvent space = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE);
        KeyEvent escape = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE);
        scrim.fireEvent(enter);
        scrim.fireEvent(space);
        scrim.fireEvent(escape);

        assertEquals(3, actions.get());
    }

    /// Verifies that custom actionable surfaces respond to accessibility fire actions.
    @Test
    void actionableSurfacesExecuteAccessibleFire() {
        AtomicInteger cardActions = new AtomicInteger();
        M3Card card = new M3Card(new Label("Card"));
        card.setOnAction(event -> cardActions.incrementAndGet());

        card.executeAccessibleAction(AccessibleAction.FIRE);
        card.setDisable(true);
        card.executeAccessibleAction(AccessibleAction.FIRE);

        assertEquals(1, cardActions.get());

        AtomicInteger scrimActions = new AtomicInteger();
        M3Scrim scrim = new M3Scrim();
        scrim.setOnAction(event -> scrimActions.incrementAndGet());

        scrim.executeAccessibleAction(AccessibleAction.FIRE);
        scrim.setDisable(true);
        scrim.executeAccessibleAction(AccessibleAction.FIRE);

        assertEquals(1, scrimActions.get());

        AtomicInteger snackbarActions = new AtomicInteger();
        M3Snackbar snackbar = new M3Snackbar("Saved", "Undo", event -> snackbarActions.incrementAndGet());

        snackbar.executeAccessibleAction(AccessibleAction.FIRE);
        snackbar.setActionText("");
        snackbar.executeAccessibleAction(AccessibleAction.FIRE);

        assertEquals(1, snackbarActions.get());
    }

    /// Verifies that scrims expose animated shown state changes.
    @Test
    void scrimSupportsAnimatedShownState() throws InterruptedException {
        M3Scrim scrim = new M3Scrim();

        applyCss(scrim);
        assertTrue(scrim.isShown());
        assertTrue(scrim.isVisible());
        assertTrue(scrim.isManaged());

        scrim.hide();

        assertFalse(scrim.isShown());
        assertTrue(scrim.isVisible());
        assertTrue(scrim.isManaged());

        runOnFxThreadAfterDelay(Duration.millis(180.0), () -> {
        }, () -> {
            assertFalse(scrim.isVisible());
            assertFalse(scrim.isManaged());
            assertEquals(0.0, scrim.getOpacity(), 0.0001);
        });

        scrim.show();

        assertTrue(scrim.isShown());
        assertTrue(scrim.isVisible());
        assertTrue(scrim.isManaged());

        runOnFxThreadAfterDelay(Duration.millis(260.0), () -> {
        }, () -> assertEquals(0.32, scrim.getOpacity(), 0.0001));
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
        M3Chip chip = createChip("Assist", icon, M3ChipVariant.INPUT, true);

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

        group.selectNext();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, group.getSelectedChip());

        group.selectPrevious();

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

    /// Verifies that chip group layout gaps are styleable from CSS.
    @Test
    void chipGroupGapTokensAreStyleable() {
        M3ChipGroup group = new M3ChipGroup(new M3Chip("First"), new M3Chip("Second"));
        group.setStyle("-m3-chip-group-horizontal-gap: 12px; -m3-chip-group-vertical-gap: 14px;");

        applyCss(group);

        assertEquals(12.0, group.getHorizontalGap(), 0.0001);
        assertEquals(14.0, group.getVerticalGap(), 0.0001);
    }

    /// Verifies that segmented button component token properties are styleable from CSS.
    @Test
    void segmentedButtonTokensAreStyleable() {
        M3SegmentedButton button = createSegmentedButton("Week", true);
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

        group.selectNext();

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());

        group.selectPrevious();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
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

    /// Verifies that segmented button group spacing is styleable from CSS.
    @Test
    void segmentedButtonGroupSpacingTokenIsStyleable() {
        M3SegmentedButtonGroup group =
                new M3SegmentedButtonGroup(new M3SegmentedButton("A"), new M3SegmentedButton("B"));
        group.setStyle("-m3-segmented-button-group-spacing: -2px;");

        applyCss(group);

        assertEquals(-2.0, group.getSpacing(), 0.0001);
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
        root.layout();

        assertLabeledColors(day, Color.TRANSPARENT, Color.rgb(34, 35, 36));
        assertBorderColor(day, Color.rgb(13, 14, 15));
        assertLabeledColors(month, Color.TRANSPARENT, Color.rgb(10, 11, 12));
        assertRegionFill(segmentedButtonSelectionContainer(month), Color.rgb(7, 8, 9));
        assertBorderColor(month, Color.rgb(13, 14, 15));
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

        group.getItems().remove(second);

        assertFalse(second.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));

        group.getItems().remove(first);

        assertFalse(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS));
    }

    /// Verifies that segmented button groups mirror physical edge style classes for right-to-left layout.
    @Test
    void segmentedButtonGroupMirrorsPositionStyleClassesForRightToLeft() {
        M3SegmentedButton first = new M3SegmentedButton("Day");
        M3SegmentedButton second = new M3SegmentedButton("Week");
        M3SegmentedButton third = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second, third);

        group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));
        assertTrue(second.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));

        group.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));
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

    /// Verifies that segmented button corners and feedback layers match right-to-left visual order.
    @Test
    void segmentedButtonGroupUsesRightToLeftPositionSpecificShapes() {
        runOnFxThread(() -> {
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(day, week, month);
            group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
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

            assertRegionRoundedCorners(day, false, true, true, false);
            assertRegionRoundedCorners(week, false, false, false, false);
            assertRegionRoundedCorners(month, true, false, false, true);
            assertStateLayerRadii(day, 0.0, 20.0, 20.0, 0.0);
            assertStateLayerRadii(week, 0.0, 0.0, 0.0, 0.0);
            assertStateLayerRadii(month, 20.0, 0.0, 0.0, 20.0);
            assertRegionRadii(segmentedButtonSelectionContainer(month), 19.0, 0.0, 0.0, 19.0);
        });
    }

    /// Verifies that segmented button selected containers animate between selected states.
    @Test
    void segmentedButtonSelectionContainersAnimateBetweenStates() {
        runOnFxThread(() -> {
            M3SegmentedButton day = createSegmentedButton("Day", true);
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(day, week, month);
            group.setPrefSize(240.0, 40.0);
            FlowPane root = new FlowPane(group);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 280.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(280.0, 80.0);
            root.layout();

            group.select(month);
            root.applyCss();

            Timeline outgoingAnimation = skinTimeline(day.getSkin(), "selectionAnimation");
            Timeline incomingAnimation = skinTimeline(month.getSkin(), "selectionAnimation");
            outgoingAnimation.jumpTo(Duration.millis(80.0));
            incomingAnimation.jumpTo(Duration.millis(80.0));
            root.layout();

            Region outgoingSelection = segmentedButtonSelectionContainer(day);
            Region incomingSelection = segmentedButtonSelectionContainer(month);
            assertBetween(outgoingSelection.getOpacity(), 0.0, 1.0, "outgoing segment selection opacity");
            assertBetween(incomingSelection.getOpacity(), 0.0, 1.0, "incoming segment selection opacity");
            assertBetween(incomingSelection.getScaleX(), 0.96, 1.0, "incoming segment selection scale");
            assertRegionRadii(outgoingSelection, 19.0, 0.0, 0.0, 19.0);
            assertRegionRadii(incomingSelection, 0.0, 19.0, 19.0, 0.0);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, incomingSelection, Color.WHITE, 0.03);
            assertSnapshotNodeBorderContainsContrast(image, month, Color.WHITE, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-segmented-selection-animation-frame.png"
            ));

            outgoingAnimation.jumpTo(Duration.millis(200.0));
            incomingAnimation.jumpTo(Duration.millis(200.0));
            root.layout();

            assertEquals(0.0, outgoingSelection.getOpacity(), 0.0001);
            assertEquals(1.0, incomingSelection.getOpacity(), 0.0001);
            assertEquals(0.96, outgoingSelection.getScaleX(), 0.0001);
            assertEquals(1.0, incomingSelection.getScaleX(), 0.0001);
            stopTimelines(outgoingAnimation, incomingAnimation);
        });
    }

    /// Verifies that tab component token properties are styleable from CSS.
    @Test
    void tabTokensAreStyleable() {
        M3Tab tab = createTab("Overview", true);
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

        tabBar.selectNext();

        assertEquals(overview, tabBar.getSelectedTab());
        assertTrue(overview.isSelected());
        assertFalse(details.isSelected());

        tabBar.selectPrevious();

        assertEquals(details, tabBar.getSelectedTab());
        assertFalse(overview.isSelected());
        assertTrue(details.isSelected());

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
        M3CheckBox checkedCheckBox = createCheckBox("Checked", true);
        M3RadioButton uncheckedRadioButton = new M3RadioButton("Unchecked");
        M3RadioButton checkedRadioButton = createRadioButton("Checked", true);
        Pane root = new Pane(uncheckedCheckBox, checkedCheckBox, uncheckedRadioButton, checkedRadioButton);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        Region uncheckedMark = lookupRegion(uncheckedCheckBox, ".mark");
        Region checkedMark = lookupRegion(checkedCheckBox, ".mark");
        Shape uncheckedDot = lookupShape(uncheckedRadioButton, ".dot");
        Shape checkedDot = lookupShape(checkedRadioButton, ".dot");
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

    /// Verifies that selection indicator animations expose intermediate and final rendered states.
    @Test
    void selectionIndicatorAnimationsRenderIntermediateAndFinalStates() {
        runOnFxThread(() -> {
            M3CheckBox checkBox = new M3CheckBox("Check");
            M3RadioButton radioButton = new M3RadioButton("Radio");
            M3Switch switchControl = new M3Switch("Switch");
            FlowPane row = new FlowPane(18.0, 12.0, checkBox, radioButton, switchControl);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 420.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(420.0, 96.0);
            row.layout();

            checkBox.setSelected(true);
            radioButton.setSelected(true);
            switchControl.setSelected(true);

            Timeline checkBoxAnimation = skinTimeline(checkBox.getSkin(), "selectionAnimation");
            Timeline radioAnimation = skinTimeline(radioButton.getSkin(), "selectionAnimation");
            Timeline switchAnimation = skinTimeline(switchControl.getSkin(), "selectionAnimation");
            checkBoxAnimation.jumpTo(Duration.millis(30.0));
            radioAnimation.jumpTo(Duration.millis(30.0));
            switchAnimation.jumpTo(Duration.millis(50.0));
            row.layout();

            Region mark = lookupRegion(checkBox, ".mark");
            Shape dot = lookupShape(radioButton, ".dot");
            Region thumb = lookupRegion(switchControl, ".thumb");
            assertBetween(mark.getOpacity(), 0.0, 1.0, "checkbox mark opacity");
            assertBetween(mark.getScaleX(), 0.72, 1.0, "checkbox mark scale");
            assertBetween(dot.getOpacity(), 0.0, 1.0, "radio dot opacity");
            assertBetween(dot.getScaleX(), 0.64, 1.0, "radio dot scale");
            assertBetween(thumb.getLayoutX(), 4.0, 24.0, "switch thumb x");
            assertBetween(thumb.getWidth(), 16.0, 24.0, "switch thumb width");

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, mark, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, dot, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, thumb, Color.rgb(84, 50, 185), 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-selection-animation-frame.png"
            ));

            checkBoxAnimation.jumpTo(Duration.millis(100.0));
            radioAnimation.jumpTo(Duration.millis(100.0));
            switchAnimation.jumpTo(Duration.millis(150.0));
            row.layout();

            assertEquals(1.0, mark.getOpacity(), 0.0001);
            assertEquals(1.0, mark.getScaleX(), 0.0001);
            assertEquals(1.0, dot.getOpacity(), 0.0001);
            assertEquals(1.0, dot.getScaleX(), 0.0001);
            assertEquals(24.0, thumb.getLayoutX(), 0.0001);
            assertEquals(24.0, thumb.getWidth(), 0.0001);

            checkBox.setSelected(false);
            radioButton.setSelected(false);
            switchControl.setSelected(false);
            checkBoxAnimation.jumpTo(Duration.millis(30.0));
            radioAnimation.jumpTo(Duration.millis(30.0));
            switchAnimation.jumpTo(Duration.millis(50.0));
            row.layout();

            assertBetween(mark.getOpacity(), 0.0, 1.0, "checkbox mark reverse opacity");
            assertBetween(dot.getOpacity(), 0.0, 1.0, "radio dot reverse opacity");
            assertBetween(thumb.getLayoutX(), 8.0, 24.0, "switch thumb reverse x");
            assertBetween(thumb.getWidth(), 16.0, 24.0, "switch thumb reverse width");

            checkBoxAnimation.jumpTo(Duration.millis(100.0));
            radioAnimation.jumpTo(Duration.millis(100.0));
            switchAnimation.jumpTo(Duration.millis(150.0));
            row.layout();

            assertEquals(0.0, mark.getOpacity(), 0.0001);
            assertEquals(0.0, dot.getOpacity(), 0.0001);
            assertEquals(8.0, thumb.getLayoutX(), 0.0001);
            assertEquals(16.0, thumb.getWidth(), 0.0001);

            checkBoxAnimation.stop();
            radioAnimation.stop();
            switchAnimation.stop();
        });
    }

    /// Verifies that selection controls mirror indicator and label order in right-to-left mode.
    @Test
    void selectionControlsMirrorIndicatorAndLabelOrderForRightToLeft() {
        runOnFxThread(() -> {
            M3CheckBox checkBox = createCheckBox("Checkbox", true);
            M3RadioButton radioButton = createRadioButton("Radio", true);
            M3Switch switchControl = createSwitch("Switch", true);
            checkBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            radioButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            switchControl.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            FlowPane row = new FlowPane(24.0, 12.0, checkBox, radioButton, switchControl);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 460.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(460.0, 96.0);
            row.layout();

            assertSelectionIndicatorAfterLabel(checkBox);
            assertSelectionIndicatorAfterLabel(radioButton);

            Region track = lookupRegion(switchControl, ".box");
            Region thumb = lookupRegion(switchControl, ".thumb");
            double trackCenter = track.localToScene(track.getBoundsInLocal()).getCenterX();
            double thumbCenter = thumb.localToScene(thumb.getBoundsInLocal()).getCenterX();
            assertTrue(thumbCenter < trackCenter, () -> "thumbCenter=" + thumbCenter + ", trackCenter=" + trackCenter);

            writeVisualSnapshot(snapshotImageOnFxThread(row), java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-selection-rtl.png"
            ));
        });
    }

    /// Verifies that checkbox indeterminate-to-selected transitions replay the mark animation.
    @Test
    void checkBoxIndeterminateTransitionReplaysMarkAnimation() {
        runOnFxThread(() -> {
            M3CheckBox checkBox = new M3CheckBox("Three-state");
            checkBox.setAllowIndeterminate(true);
            FlowPane row = new FlowPane(checkBox);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 220.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(220.0, 80.0);
            row.layout();

            checkBox.fire();
            Timeline animation = skinTimeline(checkBox.getSkin(), "selectionAnimation");
            animation.jumpTo(Duration.millis(100.0));
            row.applyCss();
            row.layout();

            Region mark = lookupRegion(checkBox, ".mark");
            assertFalse(checkBox.isSelected());
            assertTrue(checkBox.isIndeterminate());
            assertEquals(12.0, mark.getLayoutBounds().getWidth(), 0.0001);
            assertEquals(2.0, mark.getLayoutBounds().getHeight(), 0.0001);
            assertEquals(1.0, mark.getOpacity(), 0.0001);

            checkBox.fire();
            row.applyCss();
            row.layout();

            assertTrue(checkBox.isSelected());
            assertFalse(checkBox.isIndeterminate());
            assertEquals(12.0, mark.getLayoutBounds().getWidth(), 0.0001);
            assertEquals(10.0, mark.getLayoutBounds().getHeight(), 0.0001);
            assertEquals(0.0, mark.getOpacity(), 0.0001);
            assertEquals(0.72, mark.getScaleX(), 0.0001);

            animation.jumpTo(Duration.millis(30.0));
            row.layout();
            assertBetween(mark.getOpacity(), 0.0, 1.0, "checkbox indeterminate-to-selected opacity");
            assertBetween(mark.getScaleX(), 0.72, 1.0, "checkbox indeterminate-to-selected scale");

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, mark, Color.WHITE, 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-checkbox-indeterminate-transition.png"
            ));

            animation.jumpTo(Duration.millis(100.0));
            row.layout();
            assertEquals(1.0, mark.getOpacity(), 0.0001);
            assertEquals(1.0, mark.getScaleX(), 0.0001);
            animation.stop();
        });
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
        M3Switch onSwitch = createSwitch("On", true);
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

    /// Verifies that switches mirror selected thumb geometry in right-to-left mode.
    @Test
    void switchSkinMirrorsThumbPositionForRightToLeft() {
        M3Switch switchControl = createSwitch("On", true);
        switchControl.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(switchControl);
        Scene scene = new Scene(root, 180.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        switchControl.resize(120.0, 40.0);
        switchControl.layout();

        Region track = lookupRegion(switchControl, ".box");
        Region thumb = lookupRegion(switchControl, ".thumb");
        Region stateLayer = lookupRegion(switchControl, ".m3-state-layer-container");
        double trackCenter = track.localToScene(track.getBoundsInLocal()).getCenterX();
        double thumbCenter = thumb.localToScene(thumb.getBoundsInLocal()).getCenterX();
        double stateLayerCenter = stateLayer.localToScene(stateLayer.getBoundsInLocal()).getCenterX();

        assertTrue(thumbCenter < trackCenter, () -> "thumbCenter=" + thumbCenter + ", trackCenter=" + trackCenter);
        assertEquals(24.0, thumb.getWidth(), 0.0001);
        assertEquals(thumbCenter, stateLayerCenter, 0.0001);
    }

    /// Verifies that switch hover and ripple feedback uses a circular thumb state layer.
    @Test
    void switchSkinUsesCircularThumbStateLayer() {
        M3Switch offSwitch = new M3Switch("Off");
        M3Switch onSwitch = createSwitch("On", true);
        Pane root = new Pane(offSwitch, onSwitch);
        Scene scene = new Scene(root, 260.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        offSwitch.resizeRelocate(0.0, 0.0, 120.0, 40.0);
        onSwitch.resizeRelocate(0.0, 48.0, 120.0, 40.0);
        offSwitch.layout();
        onSwitch.layout();
        root.applyCss();

        Region offStateLayer = lookupRegion(offSwitch, ".m3-state-layer-container");
        Region onStateLayer = lookupRegion(onSwitch, ".m3-state-layer-container");
        Region offTrack = lookupRegion(offSwitch, ".box");
        Region offThumb = lookupRegion(offSwitch, ".thumb");
        Pane stateLayerParent = assertInstanceOf(Pane.class, offStateLayer.getParent());

        assertEquals(40.0, offStateLayer.getWidth(), 0.0001);
        assertEquals(40.0, offStateLayer.getHeight(), 0.0001);
        assertEquals(40.0, onStateLayer.getWidth(), 0.0001);
        assertEquals(40.0, onStateLayer.getHeight(), 0.0001);
        assertEquals(-4.0, offStateLayer.getLayoutX(), 0.0001);
        assertEquals(16.0, onStateLayer.getLayoutX(), 0.0001);
        assertEquals(0.0, offStateLayer.getLayoutY(), 0.0001);
        assertEquals(0.0, onStateLayer.getLayoutY(), 0.0001);
        assertStateLayerRadii(offSwitch, 20.0, 20.0, 20.0, 20.0);
        assertStateLayerRadii(onSwitch, 20.0, 20.0, 20.0, 20.0);
        assertTrue(stateLayerParent.getChildren().indexOf(offTrack) < stateLayerParent.getChildren().indexOf(offStateLayer));
        assertTrue(stateLayerParent.getChildren().indexOf(offStateLayer) < stateLayerParent.getChildren().indexOf(offThumb));
    }

    /// Verifies that the switch hover state renders circular thumb feedback in snapshots.
    @Test
    void switchHoverStateLayerRendersCircularThumbFeedback() {
        runOnFxThread(() -> {
            M3Switch switchControl = new M3Switch("On");
            switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            Pane root = new Pane(switchControl);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 220.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(220.0, 96.0);
            switchControl.resizeRelocate(24.0, 24.0, 120.0, 40.0);
            switchControl.layout();
            root.layout();
            root.applyCss();

            Region stateLayer = lookupRegion(switchControl, ".m3-state-layer-container");
            WritableImage image = snapshotImageOnFxThread(root);

            assertEquals(40.0, stateLayer.getWidth(), 0.0001);
            assertEquals(40.0, stateLayer.getHeight(), 0.0001);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 20.0, 2.0), Color.WHITE) > 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 1.0, 1.0), Color.WHITE) < 0.01);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-switch-hover-state-layer.png"
            ));
        });
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
        M3CheckBox checkBox = createCheckBox("Check", true);
        M3RadioButton radioButton = createRadioButton("Radio", true);
        M3Switch switchControl = createSwitch("Switch", true);

        assertTrue(checkBox.isSelected());
        assertTrue(radioButton.isSelected());
        assertTrue(switchControl.isSelected());
    }

    /// Verifies that radio buttons keep JavaFX ToggleGroup semantics without inheriting RadioButton.
    @Test
    void radioButtonSupportsToggleGroupSelection() {
        javafx.scene.control.ToggleGroup group = new javafx.scene.control.ToggleGroup();
        M3RadioButton first = createRadioButton("First", true);
        M3RadioButton second = new M3RadioButton("Second");
        AtomicInteger secondActions = new AtomicInteger();
        second.setOnAction(event -> secondActions.incrementAndGet());

        first.setToggleGroup(group);
        second.setToggleGroup(group);

        assertEquals(first, group.getSelectedToggle());

        second.fire();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, group.getSelectedToggle());
        assertEquals(1, secondActions.get());

        second.fire();

        assertTrue(second.isSelected());
        assertEquals(1, secondActions.get());
    }

    /// Verifies that checkboxes support the indeterminate state after moving to ButtonBase.
    @Test
    void checkBoxSupportsIndeterminateState() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        checkBox.setAllowIndeterminate(true);

        checkBox.fire();

        assertFalse(checkBox.isSelected());
        assertTrue(checkBox.isIndeterminate());
        assertTrue(checkBox.getPseudoClassStates().contains(PseudoClass.getPseudoClass("indeterminate")));
        assertEquals(true, checkBox.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(AccessibleAttribute.ToggleState.INDETERMINATE,
                checkBox.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));

        checkBox.fire();

        assertTrue(checkBox.isSelected());
        assertFalse(checkBox.isIndeterminate());
        assertEquals(AccessibleAttribute.ToggleState.CHECKED,
                checkBox.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));

        checkBox.fire();

        assertFalse(checkBox.isSelected());
        assertFalse(checkBox.isIndeterminate());
        assertTrue(checkBox.getPseudoClassStates().contains(PseudoClass.getPseudoClass("determinate")));
        assertEquals(AccessibleAttribute.ToggleState.UNCHECKED,
                checkBox.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));
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
        root.resize(160.0, 80.0);
        radioButton.resizeRelocate(20.0, 20.0, 120.0, 40.0);
        radioButton.layout();
        root.layout();

        Region radio = radioIndicator(radioButton);
        Shape ring = radioRing(radioButton);
        Shape dot = radioDot(radioButton);
        assertEquals(20.0, radio.getWidth(), 0.0001);
        assertEquals(20.0, radio.getHeight(), 0.0001);
        assertShapeFill(ring, Color.TRANSPARENT);
        assertEquals(Color.rgb(4, 5, 6), ring.getStroke());
        assertEquals(2.0, ring.getStrokeWidth(), 0.0001);
        assertShapeFill(dot, Color.TRANSPARENT);

        radioButton.setSelected(true);
        root.applyCss();

        assertShapeFill(ring, Color.TRANSPARENT);
        assertEquals(Color.rgb(1, 2, 3), ring.getStroke());
        assertShapeFill(dot, Color.rgb(1, 2, 3));
    }

    /// Verifies that a selected radio button paints its dot at the visual center of the outer indicator.
    @Test
    void selectedRadioButtonDotRendersCenteredInIndicator() {
        runOnFxThread(() -> {
            M3RadioButton radioButton = createRadioButton("Radio", true);
            Pane root = new Pane(radioButton);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 180.0, 80.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.resize(180.0, 80.0);
                radioButton.resizeRelocate(20.0, 20.0, 120.0, 40.0);
                radioButton.layout();
                root.layout();

                Region radio = radioIndicator(radioButton);
                Shape dot = radioDot(radioButton);
                Point2D radioCenter = radio.localToScene(radio.getWidth() / 2.0, radio.getHeight() / 2.0);
                Point2D dotCenter = dot.localToScene(
                        dot.getBoundsInLocal().getCenterX(),
                        dot.getBoundsInLocal().getCenterY()
                );
                assertEquals(radioCenter.getX(), dotCenter.getX(), 0.0001);
                assertEquals(radioCenter.getY(), dotCenter.getY(), 0.0001);

                WritableImage image = snapshotImageOnFxThread(root);
                Point2D renderedDotCenter = contrastingPixelCentroid(image, dot, Color.WHITE, 0.08);
                assertEquals(radioCenter.getX(), renderedDotCenter.getX(), 0.8);
                assertEquals(radioCenter.getY(), renderedDotCenter.getY(), 0.8);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-radio-dot-centering.png"
                ));
            } finally {
                stage.close();
            }
        });
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

    /// Verifies that horizontal sliders mirror value geometry and arrow-key semantics in right-to-left mode.
    @Test
    void sliderMirrorsHorizontalValueAndKeysForRightToLeft() {
        M3Slider slider = new M3Slider(0.0, 100.0, 25.0);
        slider.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 280.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region track = lookupRegion(slider, ".track");
        Region thumb = lookupRegion(slider, ".thumb");
        double trackCenter = track.getLayoutX() + track.getWidth() / 2.0;
        double thumbCenter = thumb.getLayoutX() + thumb.getWidth() / 2.0;
        assertTrue(thumbCenter > trackCenter, () -> "thumbCenter=" + thumbCenter + ", trackCenter=" + trackCenter);

        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

        assertEquals(35.0, slider.getValue(), 0.0001);

        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertEquals(25.0, slider.getValue(), 0.0001);
    }

    /// Verifies that progress component token properties are styleable from CSS.
    @Test
    void progressTokensAreStyleable() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setStyle("-m3-track-thickness: 6px; "
                + "-m3-track-shape: 18px; "
                + "-m3-wave-amplitude: 3px; "
                + "-m3-wavelength: 36px; "
                + "-m3-track-gap: 7px; "
                + "-m3-stop-size: 8px;");

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setStyle("-m3-track-thickness: 6px; "
                + "-m3-indicator-size: 72px; "
                + "-m3-wave-amplitude: 2px; "
                + "-m3-wavelength: 18px; "
                + "-m3-track-gap: 5px;");

        applyCss(progressBar);
        applyCss(progressIndicator);

        assertEquals(6.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(18.0, progressBar.getTrackShape(), 0.0001);
        assertEquals(3.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(36.0, progressBar.getWavelength(), 0.0001);
        assertEquals(7.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(8.0, progressBar.getStopSize(), 0.0001);
        assertEquals(12.0, progressBar.getPrefHeight(), 0.0001);
        assertEquals(6.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(72.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(2.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(18.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(5.0, progressIndicator.getTrackGap(), 0.0001);
        assertEquals(72.0, progressIndicator.getPrefWidth(), 0.0001);
        assertEquals(72.0, progressIndicator.getPrefHeight(), 0.0001);
    }

    /// Verifies that loading indicator token properties are styleable from CSS.
    @Test
    void loadingIndicatorTokensAreStyleable() {
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        loadingIndicator.setStyle("-m3-container-size: 72px; "
                + "-m3-indicator-size: 48px;");

        applyCss(loadingIndicator);

        assertEquals(72.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(48.0, loadingIndicator.getIndicatorSize(), 0.0001);
        assertEquals(72.0, loadingIndicator.getPrefWidth(), 0.0001);
        assertEquals(72.0, loadingIndicator.getPrefHeight(), 0.0001);
    }

    /// Verifies that the expressive profile applies wavy progress defaults through generated CSS.
    @Test
    void expressiveProgressTokensApplyWavyDefaults() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        Pane root = new Pane(progressBar, progressIndicator, loadingIndicator);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(4.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(3.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(40.0, progressBar.getWavelength(), 0.0001);
        assertEquals(4.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(4.0, progressBar.getStopSize(), 0.0001);
        assertEquals(10.0, progressBar.getPrefHeight(), 0.0001);
        assertEquals(4.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(48.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(2.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(15.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(4.0, progressIndicator.getTrackGap(), 0.0001);
        assertEquals(64.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(48.0, loadingIndicator.getIndicatorSize(), 0.0001);
    }

    /// Verifies that m3fx progress controls create Material Design 3 skins.
    @Test
    void progressControlsCreateMaterialSkins() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();

        applyCss(progressBar);
        applyCss(progressIndicator);
        applyCss(loadingIndicator);

        assertInstanceOf(M3ProgressBarSkin.class, progressBar.getSkin());
        assertInstanceOf(M3ProgressIndicatorSkin.class, progressIndicator.getSkin());
        assertInstanceOf(M3LoadingIndicatorSkin.class, loadingIndicator.getSkin());
    }

    /// Verifies that indeterminate progress animations respond to runtime motion setting changes.
    @Test
    void indeterminateProgressAnimationsRefreshWhenMotionSettingsChange() {
        runOnFxThreadAndWait(() -> {
            M3ProgressBar progressBar = new M3ProgressBar();
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
            M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
            Pane root = new Pane(progressBar, progressIndicator, loadingIndicator);
            Scene scene = new Scene(root);

            M3MotionSettings.setAnimationsEnabled(root, true);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();

            Timeline progressBarAnimation = skinTimeline(progressBar.getSkin(), "indeterminateAnimation");
            Timeline progressIndicatorAnimation = skinTimeline(progressIndicator.getSkin(), "indeterminateAnimation");
            Timeline loadingIndicatorAnimation = skinTimeline(loadingIndicator.getSkin(), "indeterminateAnimation");
            Timeline loadingIndicatorRotation = skinTimeline(loadingIndicator.getSkin(), "globalRotationAnimation");

            assertEquals(javafx.animation.Animation.Status.RUNNING, progressBarAnimation.getStatus());
            assertEquals(javafx.animation.Animation.Status.RUNNING, progressIndicatorAnimation.getStatus());
            assertEquals(javafx.animation.Animation.Status.RUNNING, loadingIndicatorAnimation.getStatus());
            assertEquals(javafx.animation.Animation.Status.RUNNING, loadingIndicatorRotation.getStatus());

            M3MotionSettings.setAnimationsEnabled(root, false);

            assertFalse(M3MotionSettings.areAnimationsEnabled(progressBar));
            assertFalse(progressBarAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING);
            assertFalse(progressIndicatorAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING);
            assertFalse(loadingIndicatorAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING);
            assertFalse(loadingIndicatorRotation.getStatus() == javafx.animation.Animation.Status.RUNNING);

            M3MotionSettings.setAnimationsEnabled(root, true);

            assertEquals(javafx.animation.Animation.Status.RUNNING, progressBarAnimation.getStatus());
            assertEquals(javafx.animation.Animation.Status.RUNNING, progressIndicatorAnimation.getStatus());
            assertEquals(javafx.animation.Animation.Status.RUNNING, loadingIndicatorAnimation.getStatus());
            assertEquals(javafx.animation.Animation.Status.RUNNING, loadingIndicatorRotation.getStatus());
        });
    }

    /// Verifies that progress controls expose accessible value, range, and indeterminate state.
    @Test
    void progressControlsExposeAccessibleValues() {
        M3ProgressBar progressBar = new M3ProgressBar(0.42);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.65);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator(0.5);
        @Nullable AccessibleAttribute valueStringAttribute = M3Accessible.attribute("VALUE_STRING");

        assertEquals(false, progressBar.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(0.0, progressBar.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(1.0, progressBar.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(0.42, (Double) progressBar.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("42%", progressBar.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(Orientation.HORIZONTAL, progressBar.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

        assertEquals(false, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(0.0, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(1.0, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(0.65, (Double) progressIndicator.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("65%", progressIndicator.queryAccessibleAttribute(valueStringAttribute));
        }

        assertEquals(false, loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(0.0, loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(1.0, loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(0.5, (Double) loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("50%", loadingIndicator.queryAccessibleAttribute(valueStringAttribute));
        }

        progressBar.setProgress(-10.0);
        progressIndicator.setProgress(Double.NaN);
        loadingIndicator.setProgress(-0.1);

        assertEquals(true, progressBar.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(M3ProgressBar.INDETERMINATE_PROGRESS,
                (Double) progressBar.queryAccessibleAttribute(AccessibleAttribute.VALUE),
                0.0001);
        if (valueStringAttribute != null) {
            assertEquals("Indeterminate", progressBar.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(true, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(M3ProgressIndicator.INDETERMINATE_PROGRESS,
                (Double) progressIndicator.queryAccessibleAttribute(AccessibleAttribute.VALUE),
                0.0001);
        if (valueStringAttribute != null) {
            assertEquals("Indeterminate", progressIndicator.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(true, loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(M3LoadingIndicator.INDETERMINATE_PROGRESS,
                (Double) loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.VALUE),
                0.0001);
        if (valueStringAttribute != null) {
            assertEquals("Indeterminate", loadingIndicator.queryAccessibleAttribute(valueStringAttribute));
        }
    }

    /// Verifies that the loading indicator skin lays out one visible morphing shape.
    @Test
    void loadingIndicatorSkinLaysOutSingleMorphingShape() {
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator(0.5);
        Pane root = new Pane(loadingIndicator);
        Scene scene = new Scene(root, 120.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        loadingIndicator.resize(72.0, 72.0);
        loadingIndicator.layout();

        Set<Node> indicators = loadingIndicator.lookupAll(".m3-loading-indicator-indicator");
        assertEquals(1, indicators.size());
        Path indicator = assertInstanceOf(Path.class, indicators.iterator().next());
        assertTrue(indicator.isVisible());
        assertTrue(indicator.getElements().size() > 24);
    }

    /// Verifies that the contained loading indicator aligns the active shape with its container.
    @Test
    void containedLoadingIndicatorCentersShapeInContainer() {
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        loadingIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);
        loadingIndicator.setStyle("-m3-container-size: 72px; -m3-indicator-size: 54px;");
        Pane root = new Pane(loadingIndicator);
        Scene scene = new Scene(root, 120.0, 90.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        loadingIndicator.resize(72.0, 72.0);
        loadingIndicator.layout();

        Region container = assertInstanceOf(
                Region.class,
                loadingIndicator.lookup(".m3-loading-indicator-container")
        );
        Path indicator = assertInstanceOf(
                Path.class,
                loadingIndicator.lookup(".m3-loading-indicator-indicator")
        );
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertEquals(containerBounds.getCenterX(), indicatorBounds.getCenterX(), 0.75);
        assertEquals(containerBounds.getCenterY(), indicatorBounds.getCenterY(), 0.75);
        assertTrue(containerBounds.contains(indicatorBounds));
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
        assertTrue(bar.getBoundsInParent().getMaxX() > 0.0);
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

    /// Verifies that expressive progress bars render a wavy active path with separated track and stop indicator.
    @Test
    void expressiveProgressBarSkinDrawsWavyIndicatorAndStop() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 280.0, 60.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();
        progressBar.resize(240.0, 16.0);
        progressBar.layout();

        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
        Path wave = assertInstanceOf(Path.class, lookupShape(progressBar, ".m3-progress-bar-wave"));
        Rectangle track = (Rectangle) lookupShape(progressBar, ".track");
        javafx.scene.shape.Circle stop =
                assertInstanceOf(javafx.scene.shape.Circle.class, lookupShape(progressBar, ".m3-progress-stop"));

        assertFalse(bar.isVisible());
        assertTrue(wave.isVisible());
        assertTrue(wave.getElements().size() > 8);
        assertTrue(wave.getBoundsInLocal().getHeight() > progressBar.getTrackThickness());
        assertTrue(track.getX() > 120.0);
        assertTrue(stop.isVisible());
        assertEquals(2.0, stop.getRadius(), 0.0001);
    }

    /// Verifies that expressive indeterminate progress bars keep the track outside the moving wave.
    @Test
    void expressiveProgressBarSkinSeparatesIndeterminateWaveFromTrack() {
        M3ProgressBar progressBar = new M3ProgressBar();
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 280.0, 60.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();
        skinTimeline(progressBar.getSkin(), "indeterminateAnimation").stop();
        reflectedDoubleProperty(progressBar.getSkin(), "indeterminatePosition").set(0.5);
        progressBar.resize(240.0, 16.0);
        progressBar.layout();

        Path wave = assertInstanceOf(Path.class, lookupShape(progressBar, ".m3-progress-bar-wave"));
        Rectangle leadingTrack = (Rectangle) lookupShape(progressBar, ".track");
        Rectangle trailingTrack = (Rectangle) lookupShape(progressBar, ".m3-progress-bar-secondary-track");
        Point2D waveStart = firstPathPoint(wave);
        Point2D waveEnd = lastPathPoint(wave);

        assertTrue(wave.isVisible());
        assertTrue(leadingTrack.isVisible());
        assertTrue(trailingTrack.isVisible());
        assertTrue(leadingTrack.getX() + leadingTrack.getWidth() <= waveStart.getX() - progressBar.getTrackGap(),
                () -> "leadingTrack=" + leadingTrack.getBoundsInParent() + ", waveStart=" + waveStart);
        assertTrue(trailingTrack.getX() >= waveEnd.getX() + progressBar.getTrackGap(),
                () -> "trailingTrack=" + trailingTrack.getBoundsInParent() + ", waveEnd=" + waveEnd);
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

    /// Verifies that indeterminate circular progress uses a seamless phase cycle.
    @Test
    void progressIndicatorIndeterminateCycleHasNoPhaseJump() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator();

        applyCss(progressIndicator);

        Timeline animation = skinTimeline(progressIndicator.getSkin(), "indeterminateAnimation");
        assertEquals(2, animation.getKeyFrames().size());
        assertEquals(0.0, keyFrameEndNumber(animation, 0), 0.0001);
        assertEquals(1.0, keyFrameEndNumber(animation, 1), 0.0001);
    }

    /// Verifies that expressive circular progress uses wavy paths instead of the baseline arc geometry.
    @Test
    void expressiveProgressIndicatorSkinDrawsWavyIndicatorAndTrack() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 96.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();
        progressIndicator.resize(64.0, 64.0);
        progressIndicator.layout();

        javafx.scene.shape.Circle track = (javafx.scene.shape.Circle) lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        Path waveTrack = assertInstanceOf(
                Path.class,
                lookupShape(progressIndicator, ".m3-progress-indicator-track-wave")
        );
        Path waveIndicator = assertInstanceOf(
                Path.class,
                lookupShape(progressIndicator, ".m3-progress-indicator-wave")
        );

        assertFalse(track.isVisible());
        assertFalse(indicator.isVisible());
        assertTrue(waveTrack.isVisible());
        assertTrue(waveIndicator.isVisible());
        assertTrue(waveTrack.getElements().size() > 8);
        assertTrue(waveIndicator.getElements().size() > 8);
        assertTrue(waveIndicator.getBoundsInLocal().getHeight() > 0.0);

        Point2D activeStart = firstPathPoint(waveIndicator);
        Point2D activeEnd = lastPathPoint(waveIndicator);
        Point2D trackStart = firstPathPoint(waveTrack);
        Point2D trackEnd = lastPathPoint(waveTrack);
        double minimumCenterlineGap = progressIndicator.getTrackGap()
                + progressIndicator.getTrackThickness() / 2.0;
        assertTrue(activeEnd.distance(trackStart) > minimumCenterlineGap,
                () -> "activeEnd=" + activeEnd + ", trackStart=" + trackStart);
        assertTrue(activeStart.distance(trackEnd) > minimumCenterlineGap,
                () -> "activeStart=" + activeStart + ", trackEnd=" + trackEnd);
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

    /// Verifies that badges expose non-negative count convenience APIs.
    @Test
    void badgeSupportsCountConvenienceApi() {
        M3Badge badge = new M3Badge(1234);
        badge.setMaxCharacterCount(3);

        assertEquals("1234", badge.getText());
        assertEquals("123+", badge.getDisplayText());

        badge.setCount(0);

        assertEquals("0", badge.getText());
        assertEquals("0", badge.getDisplayText());
        assertThrows(IllegalArgumentException.class, () -> badge.setCount(-1));
        assertThrows(IllegalArgumentException.class, () -> new M3Badge(-1));
    }

    /// Verifies that badge skins animate rendered text changes.
    @Test
    void badgeSkinAnimatesDisplayTextChanges() {
        M3Badge badge = new M3Badge("1");
        applyCss(badge);

        Region label = lookupRegion(badge, ".m3-badge-label");
        assertEquals(1.0, label.getOpacity(), 0.0001);
        assertEquals(1.0, label.getScaleX(), 0.0001);

        badge.setText("2");

        assertEquals("2", badge.getDisplayText());
        assertEquals(0.0, label.getOpacity(), 0.0001);
        assertTrue(label.getScaleX() < 1.0);
    }

    /// Verifies that badged boxes overlay badges on content.
    @Test
    void badgedBoxOwnsContentAndBadge() {
        Label content = new Label("Inbox");
        M3Badge badge = new M3Badge("3");
        M3BadgedBox badgedBox = new M3BadgedBox(content, badge);
        badgedBox.setBadgeAlignment(Pos.TOP_LEFT);
        badgedBox.setBadgeOffsetX(3.0);
        badgedBox.setBadgeOffsetY(-2.0);

        applyCss(badgedBox);

        assertEquals(content, badgedBox.getContent());
        assertEquals(badge, badgedBox.getBadge());
        assertInstanceOf(M3BadgedBoxSkin.class, badgedBox.getSkin());
        assertEquals(Pos.TOP_LEFT, badgedBox.getBadgeAlignment());
        assertEquals(3.0, badgedBox.getBadgeOffsetX(), 0.0001);
        assertEquals(-2.0, badgedBox.getBadgeOffsetY(), 0.0001);
        assertTrue(content.getParent() != null);
        assertTrue(badge.getParent() != null);
        assertEquals(3.0, badge.getTranslateX(), 0.0001);
        assertEquals(-2.0, badge.getTranslateY(), 0.0001);

        badgedBox.setBadge(null);

        assertNull(badgedBox.getBadge());
        assertNull(badge.getParent());
        assertTrue(content.getParent() != null);

        M3Badge replacement = new M3Badge();
        badgedBox.setBadge(replacement);

        assertTrue(replacement.getParent() != null);
        assertEquals(3.0, replacement.getTranslateX(), 0.0001);
        assertEquals(-2.0, replacement.getTranslateY(), 0.0001);
    }

    /// Verifies that the default badge alignment maps to logical end in right-to-left layouts.
    @Test
    void badgedBoxMirrorsDefaultBadgeAlignmentForRightToLeft() {
        runOnFxThread(() -> {
            M3Avatar content = new M3Avatar("M");
            M3Badge badge = new M3Badge("9");
            M3BadgedBox badgedBox = new M3BadgedBox(content, badge);
            badgedBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Pane root = new Pane(badgedBox);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 140.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(140.0, 120.0);
            badgedBox.resizeRelocate(40.0, 36.0, 48.0, 48.0);
            root.layout();
            badgedBox.layout();

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, badgedBox.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, assertInstanceOf(
                    Node.class,
                    badge.getParent()
            ).getEffectiveNodeOrientation());
            assertEquals(Pos.TOP_RIGHT, badgedBox.getBadgeAlignment());

            assertBadgedBoxBadgeAnchoredToLogicalEnd(badgedBox, true);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, badgedBox, Color.WHITE, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-badged-box-rtl.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
        });
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

    /// Verifies that list items expose trailing supporting text without changing the row line count.
    @Test
    void listItemSupportsTrailingSupportingText() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setTrailingSupportingText("3 min");
        listItem.setTrailingIcon(">");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 280.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(260.0, 56.0);
        listItem.layout();

        assertListItemLineCount(listItem, M3ListItemLineCount.ONE_LINE);
        assertEquals("Headline 3 min", listItem.getAccessibleText());
        Node trailingText = listItem.lookup(".m3-list-item-trailing-supporting");
        assertInstanceOf(Label.class, trailingText);
        assertEquals("3 min", ((Label) trailingText).getText());
        assertTrue(trailingText.isVisible());
        assertTrue(trailingText.isManaged());
    }

    /// Verifies that list items mirror logical leading and trailing content in right-to-left layouts.
    @Test
    void listItemMirrorsLogicalSlotsForRightToLeft() {
        runOnFxThread(() -> {
            M3ListItem listItem = new M3ListItem("Headline");
            listItem.setSupportingText("Supporting");
            listItem.setLeadingIcon("L");
            listItem.setTrailingSupportingText("3 min");
            listItem.setTrailingIcon("T");
            listItem.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            StackPane root = new StackPane(listItem);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 360.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(360.0, 120.0);
            root.layout();
            listItem.resize(320.0, 72.0);
            listItem.layout();

            Node leadingSlot = Objects.requireNonNull(listItem.lookup(".m3-list-item-leading"));
            Node trailingSlot = Objects.requireNonNull(listItem.lookup(".m3-list-item-trailing"));
            VBox textBox = assertInstanceOf(VBox.class, listItem.lookup(".m3-list-item-text"));
            Bounds leadingBounds = leadingSlot.localToScene(leadingSlot.getBoundsInLocal());
            Bounds trailingBounds = trailingSlot.localToScene(trailingSlot.getBoundsInLocal());

            assertEquals(Pos.CENTER_RIGHT, textBox.getAlignment());
            assertTrue(leadingBounds.getMinX() > trailingBounds.getMaxX(),
                    () -> "leadingBounds=" + leadingBounds + ", trailingBounds=" + trailingBounds);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, leadingSlot, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, trailingSlot, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-item-rtl.png"
            ));
        });
    }

    /// Verifies that fixed list item slot sizes drive media layout and clipping.
    @Test
    void listItemSlotSizesDriveMediaLayout() {
        M3ListItem listItem = new M3ListItem("Headline");
        StackPane thumbnail = new StackPane(new Label("T"));
        listItem.setLeadingThumbnail(thumbnail);
        listItem.setTrailingIcon(">");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 320.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(300.0, 72.0);
        listItem.layout();

        Node leadingSlot = listItem.lookup(".m3-list-item-leading");
        Node trailingSlot = listItem.lookup(".m3-list-item-trailing");
        assertInstanceOf(Region.class, leadingSlot);
        assertInstanceOf(Region.class, trailingSlot);
        assertEquals(M3ListItemSlotSize.THUMBNAIL, listItem.getLeadingSlotSize());
        assertEquals(M3ListItemSlotSize.ICON, listItem.getTrailingSlotSize());
        assertEquals(56.0, ((Region) leadingSlot).getPrefWidth(), 0.0001);
        assertEquals(56.0, ((Region) leadingSlot).getPrefHeight(), 0.0001);
        assertEquals(24.0, ((Region) trailingSlot).getPrefWidth(), 0.0001);
        assertEquals(24.0, ((Region) trailingSlot).getPrefHeight(), 0.0001);
        assertInstanceOf(Rectangle.class, leadingSlot.getClip());
        Rectangle clip = (Rectangle) leadingSlot.getClip();
        assertEquals(56.0, clip.getWidth(), 0.0001);
        assertEquals(56.0, clip.getHeight(), 0.0001);
        assertEquals(8.0, clip.getArcWidth(), 0.0001);
    }

    /// Verifies that list item media slots render the supported fixed-size variants.
    @Test
    void listItemMediaSnapshotRendersSlotVariants() {
        runOnFxThread(() -> {
            M3ListItem iconItem = new M3ListItem("Icon row");
            iconItem.setLeadingIcon("I");
            iconItem.setTrailingSupportingText("Now");

            M3ListItem avatarItem = new M3ListItem("Avatar row");
            avatarItem.setSupportingText("Avatar-sized leading slot");
            avatarItem.setLeadingAvatar("A");
            avatarItem.setTrailingIcon(">");

            M3ListItem thumbnailItem = new M3ListItem("Thumbnail row");
            thumbnailItem.setSupportingText("Square thumbnail media");
            thumbnailItem.setLeadingThumbnail(visualListMedia("T"));
            thumbnailItem.setTrailingSupportingText("12:40");

            M3ListItem wideThumbnailItem = new M3ListItem("Wide thumbnail row");
            wideThumbnailItem.setSupportingText("Wide thumbnail media");
            wideThumbnailItem.setLeadingWideThumbnail(visualListMedia("W"));
            wideThumbnailItem.setTrailingIcon(">");

            M3ListPane list = new M3ListPane(
                    new M3ListSectionHeader("Media"),
                    iconItem,
                    avatarItem,
                    thumbnailItem,
                    wideThumbnailItem
            );
            list.setPrefWidth(420.0);
            list.setStyle("-fx-background-color: white; -fx-padding: 8px 0 8px 0; " + visualTestColors());
            Scene scene = new Scene(list, 440.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            list.applyCss();
            list.resize(420.0, 340.0);
            list.layout();

            WritableImage image = snapshotImageOnFxThread(list);
            assertSnapshotNodeContainsContrast(image, listItemContainer(iconItem), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, listItemContainer(avatarItem), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, listItemContainer(thumbnailItem), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, listItemContainer(wideThumbnailItem), Color.WHITE, 0.03);
            assertEquals(56.0, ((Region) thumbnailItem.lookup(".m3-list-item-leading")).getWidth(), 0.0001);
            assertEquals(64.0, ((Region) wideThumbnailItem.lookup(".m3-list-item-leading")).getWidth(), 0.0001);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-item-media.png"
            ));
        });
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
        M3ListPane list = new M3ListPane(first, new M3Divider(), second, third);

        applyCss(list);

        assertInstanceOf(M3ListPaneSkin.class, list.getSkin());
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

        list.selectNext();

        assertEquals(third, list.getSelectedItem());
        assertEquals(3, list.getSelectedIndex());

        list.selectPrevious();

        assertEquals(second, list.getSelectedItem());
        assertEquals(2, list.getSelectedIndex());

        list.selectLast();

        assertEquals(third, list.getSelectedItem());
        assertEquals(3, list.getSelectedIndex());

        list.setAllowEmptySelection(false);
        list.clearSelection();

        assertEquals(third, list.getSelectedItem());
        assertTrue(third.isSelected());

        third.setSelected(false);

        assertEquals(third, list.getSelectedItem());
        assertTrue(third.isSelected());

        list.getItems().remove(third);

        assertFalse(third.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first), list.getSelectedItems());
        assertEquals(0, list.getSelectedIndex());
    }

    /// Verifies that list section headers behave as non-selectable list content.
    @Test
    void listSectionHeadersAreNonSelectableContent() {
        M3ListSectionHeader primaryHeader = new M3ListSectionHeader("Primary");
        M3ListSectionHeader secondaryHeader = new M3ListSectionHeader("Secondary");
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListPane list = new M3ListPane(primaryHeader, first, new M3Divider(), secondaryHeader, second);

        list.setSelectionMode(M3ListSelectionMode.SINGLE);
        list.selectFirst();

        assertEquals(first, list.getSelectedItem());
        assertEquals(1, list.getSelectedIndex());
        assertFalse(primaryHeader.isFocusTraversable());
        assertFalse(secondaryHeader.isFocusTraversable());
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(3));

        list.selectLast();

        assertEquals(second, list.getSelectedItem());
        assertEquals(4, list.getSelectedIndex());
        assertEquals(5, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(primaryHeader, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(secondaryHeader, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 3));
    }

    /// Verifies that list section headers use list typography and spacing tokens.
    @Test
    void listSectionHeaderUsesListStylesheet() {
        M3ListSectionHeader header = new M3ListSectionHeader("Pinned");

        applyCss(header);

        assertEquals("Pinned", header.getText());
        assertTrue(header.getStyleClass().contains(M3ListSectionHeader.STYLE_CLASS));
        assertEquals(AccessibleRole.TEXT, header.getAccessibleRole());
        assertInstanceOf(M3TextSkin.class, header.getSkin());
        assertEquals(48.0, header.prefHeight(320.0), 0.0001);
    }

    /// Verifies that lists can use multiple selected items.
    @Test
    void listCanUseMultipleSelection() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3ListPane list = new M3ListPane(first, second, third);

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
        M3ListPane list = new M3ListPane(first, second, third);

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

    /// Verifies that virtualized list views expose data selection policies.
    @Test
    void listViewManagesDataSelectionPolicies() {
        M3ListView<String> listView = new M3ListView<>("First", "Second", "Third");
        listView.setSelectionMode(M3ListSelectionMode.SINGLE);

        listView.selectIndex(1);

        assertEquals(1, listView.getSelectedIndex());
        assertEquals("Second", listView.getSelectedItem());
        assertEquals(java.util.List.of(1), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Second"), listView.getSelectedItems());
        assertThrows(UnsupportedOperationException.class, () -> listView.getSelectedIndices().add(2));

        listView.setSelectionMode(M3ListSelectionMode.MULTIPLE);
        listView.setIndexSelected(2, true);

        assertEquals(java.util.List.of(1, 2), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Second", "Third"), listView.getSelectedItems());

        listView.clearSelection(1);

        assertEquals(java.util.List.of(2), listView.getSelectedIndices());
        assertEquals("Third", listView.getSelectedItem());

        listView.setAllowEmptySelection(false);
        listView.clearSelection();

        assertEquals(java.util.List.of(2), listView.getSelectedIndices());
        assertEquals("Third", listView.getSelectedItem());

        listView.getItems().remove("Third");

        assertEquals(java.util.List.of(0), listView.getSelectedIndices());
        assertEquals("First", listView.getSelectedItem());
    }

    /// Verifies that virtualized list views use VirtualFlow-backed cells instead of materializing every item.
    @Test
    void listViewVirtualizesRenderedItems() {
        runOnFxThread(() -> {
            AtomicInteger factoryCalls = new AtomicInteger();
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setCellFactory(value -> {
                factoryCalls.incrementAndGet();
                M3ListItem item = new M3ListItem("Row " + value);
                item.setLeadingIcon(Integer.toString(value % 10));
                return item;
            });
            listView.setPrefSize(260.0, 168.0);
            Pane root = new Pane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertInstanceOf(M3ListViewSkin.class, listView.getSkin());
                assertTrue(factoryCalls.get() < listView.getItems().size());
                M3ListItem visibleSecond = assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1)
                );
                visibleSecond.fire();

                assertEquals(1, listView.getSelectedIndex());
                assertTrue(visibleSecond.isSelected());

                listView.scrollTo(80);
                root.layout();

                M3ListItem visibleEightieth = assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 80)
                );
                assertEquals("Row 80", visibleEightieth.getHeadlineText());
                assertTrue(factoryCalls.get() < listView.getItems().size());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views animate wheel scrolling through Material motion.
    @Test
    void listViewSmoothScrollingAnimatesWheelScroll() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                M3MotionSettings.setAnimationsEnabled(listView, true);

                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(0.0, flow.getPosition(), 0.0001);
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that running virtualized list wheel scrolling settles when animations are disabled at runtime.
    @Test
    void listViewSmoothScrollingSettlesWhenAnimationsAreDisabledAtRuntime() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                M3MotionSettings.setAnimationsEnabled(listView, true);

                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                M3MotionSettings.setAnimationsEnabled(listView, false);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that disabled animation settings make virtualized list wheel scrolling finish synchronously.
    @Test
    void listViewSmoothScrollingHonorsDisabledAnimations() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                M3MotionSettings.setAnimationsEnabled(listView, false);

                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that programmatic virtualized list scrolling uses the animated scroll policy.
    @Test
    void listViewProgrammaticScrollUsesAnimatedPolicy() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );

                assertTrue(listView.isAnimatedScroll());
                M3MotionSettings.setAnimationsEnabled(listView, true);
                listView.scrollTo(80);

                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimatedScroll(false);
                assertFalse(listView.isAnimatedScroll());
                listView.scrollTo(80);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that running programmatic virtualized list scrolling settles when animations are disabled.
    @Test
    void listViewProgrammaticScrollSettlesWhenAnimationsAreDisabledAtRuntime() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                M3MotionSettings.setAnimationsEnabled(listView, true);

                listView.scrollTo(80);

                assertEquals(0.0, flow.getPosition(), 0.0001);

                M3MotionSettings.setAnimationsEnabled(listView, false);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that disabled animations make programmatic virtualized list scrolling finish synchronously.
    @Test
    void listViewProgrammaticScrollHonorsDisabledAnimations() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                M3MotionSettings.setAnimationsEnabled(listView, false);

                listView.scrollTo(80);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that keyboard focus scrolling also follows the animated scroll policy.
    @Test
    void listViewKeyboardFocusUsesAnimatedScrollPolicy() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.addItem(i);
            }
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                M3MotionSettings.setAnimationsEnabled(listView, true);

                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertEquals(99, listView.getFocusedIndex());
                assertEquals(99, listView.getSelectedIndex());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimatedScroll(false);
                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertEquals(0, listView.getFocusedIndex());
                assertEquals(0, listView.getSelectedIndex());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertEquals(99, listView.getFocusedIndex());
                assertEquals(99, listView.getSelectedIndex());
                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(listView);
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views expose keyboard focus and selection navigation.
    @Test
    void listViewSupportsKeyboardFocusAndSelectionNavigation() {
        M3ListView<String> listView = new M3ListView<>("First", "Second", "Third");
        listView.setSelectionMode(M3ListSelectionMode.SINGLE);

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(0, listView.getFocusedIndex());
        assertEquals("First", listView.getFocusedItem());
        assertEquals(0, listView.getSelectedIndex());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Second", listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

        assertEquals(2, listView.getFocusedIndex());
        assertEquals("Third", listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Second", listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

        assertEquals(0, listView.getFocusedIndex());
        assertEquals("First", listView.getSelectedItem());

        listView.setSelectionMode(M3ListSelectionMode.MULTIPLE);
        listView.clearFocus();
        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(1, listView.getFocusedIndex());
        assertEquals(java.util.List.of(0), listView.getSelectedIndices());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));

        assertEquals(java.util.List.of(0, 1), listView.getSelectedIndices());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

        assertEquals(java.util.List.of(0), listView.getSelectedIndices());
    }

    /// Verifies that virtualized list view keyboard and accessibility focus scrolls rows into view.
    @Test
    void listViewFocusesVirtualizedRowsFromKeyboardAndAccessibility() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListView<Integer>> listViewReference = new AtomicReference<>();

        try {
            runOnFxThread(() -> {
                M3ListView<Integer> listView = new M3ListView<>();
                for (int i = 0; i < 100; i++) {
                    listView.addItem(i);
                }
                listView.setSelectionMode(M3ListSelectionMode.SINGLE);
                listView.setFixedCellSize(56.0);
                listView.setPrefSize(260.0, 168.0);
                listView.setCellFactory(value -> new M3ListItem("Row " + value));
                Pane root = new StackPane(listView);
                Scene scene = new Scene(root, 300.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                Stage stage = new Stage();
                stageReference.set(stage);
                rootReference.set(root);
                listViewReference.set(listView);

                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                listView.requestFocus();
                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                root.layout();
            });

            runOnFxThread(() -> {
                Pane root = Objects.requireNonNull(rootReference.get(), "root");
                M3ListView<Integer> listView = Objects.requireNonNull(listViewReference.get(), "listView");
                root.layout();

                assertEquals(99, listView.getFocusedIndex());
                assertEquals(99, listView.getSelectedIndex());
                M3ListItem lastItem = Objects.requireNonNull(assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 99)
                ));
                assertEquals("Row 99", lastItem.getHeadlineText());
                assertSame(listView, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertTrue(listView.isFocused());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 40);
                root.layout();
            });

            runOnFxThread(() -> {
                Pane root = Objects.requireNonNull(rootReference.get(), "root");
                M3ListView<Integer> listView = Objects.requireNonNull(listViewReference.get(), "listView");
                root.layout();

                assertEquals(40, listView.getFocusedIndex());
                M3ListItem focusedItem = Objects.requireNonNull(assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 40)
                ));
                assertEquals("Row 40", focusedItem.getHeadlineText());
                assertSame(listView, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertTrue(listView.isFocused());
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that focused virtualized list rows update after item changes.
    @Test
    void listViewRefreshesFocusedItemAfterDataChanges() {
        M3ListView<String> listView = new M3ListView<>("First", "Second", "Third");

        listView.focusIndex(1);
        listView.getItems().set(1, "Updated");

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Updated", listView.getFocusedItem());

        listView.getItems().remove(2);

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Updated", listView.getFocusedItem());

        listView.getItems().remove(1);

        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());
    }

    /// Verifies that focus convenience methods wrap around list data.
    @Test
    void listViewFocusConvenienceMethodsWrapAroundData() {
        M3ListView<String> listView = new M3ListView<>("First", "Second", "Third");

        listView.focusFirst();

        assertEquals(0, listView.getFocusedIndex());

        listView.focusPrevious();

        assertEquals(2, listView.getFocusedIndex());

        listView.focusNext();

        assertEquals(0, listView.getFocusedIndex());

        listView.focusLast();

        assertEquals(2, listView.getFocusedIndex());

        listView.clearFocus();

        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());
    }

    /// Verifies that virtualized list view row content stays out of direct tab traversal.
    @Test
    void listViewKeepsVirtualRowsOutOfDirectTabTraversal() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 8; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(value -> new M3ListItem("Row " + value));
            Pane root = new Pane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                M3ListItem firstItem = Objects.requireNonNull(assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0)
                ));
                assertFalse(firstItem.isFocusTraversable());

                listView.focusIndex(0);
                root.layout();

                assertTrue(firstItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focus-visible")));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list view cells create skins that render row graphics.
    @Test
    void listViewCellCreatesMaterialSkin() {
        M3ListViewCell<String> cell = new M3ListViewCell<>(new M3ListView<>());

        applyCss(cell);

        assertInstanceOf(M3ListViewCellSkin.class, cell.getSkin());
    }

    /// Verifies that virtualized list view rows are actually visible in rendered snapshots.
    @Test
    void listViewSnapshotRendersVisibleRows() {
        runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 24; i++) {
                listView.addItem(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(320.0, 168.0);
            listView.setCellFactory(value -> {
                M3ListItem item = new M3ListItem("Visible row " + value);
                item.setLeadingIcon(Integer.toString(value % 10));
                return item;
            });
            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 360.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            WritableImage image = snapshotImageOnFxThread(listView);
            assertSnapshotAreaContainsContrast(
                    image,
                    12,
                    12,
                    240,
                    150,
                    Color.WHITE,
                    0.35,
                    "visible virtualized list rows"
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-view-virtualized.png"
            ));
        });
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
        assertRegionRadii(listItemSelectionContainer(listItem), 28.0, 28.0, 28.0, 28.0);
    }

    /// Verifies that list and drawer selected containers animate incoming and outgoing states.
    @Test
    void listItemSelectionContainerAnimationsRenderIntermediateAndFinalStates() {
        runOnFxThread(() -> {
            M3ListItem listFirst = new M3ListItem("Inbox");
            M3ListItem listSecond = new M3ListItem("Archive");
            M3ListPane list = new M3ListPane(listFirst, listSecond);
            list.setSelectionMode(M3ListSelectionMode.SINGLE);
            list.select(listFirst);
            list.setPrefWidth(280.0);

            M3ListItem drawerFirst = new M3ListItem("Home");
            M3ListItem drawerSecond = new M3ListItem("Search");
            M3NavigationDrawer drawer = new M3NavigationDrawer(drawerFirst, drawerSecond);
            drawer.select(drawerFirst);
            drawer.setPrefWidth(320.0);

            VBox root = new VBox(18.0, list, drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 260.0);
            root.layout();

            list.select(listSecond);
            drawer.select(drawerSecond);
            root.applyCss();

            Timeline outgoingListAnimation = skinTimeline(listFirst.getSkin(), "selectionAnimation");
            Timeline incomingListAnimation = skinTimeline(listSecond.getSkin(), "selectionAnimation");
            Timeline outgoingDrawerAnimation = skinTimeline(drawerFirst.getSkin(), "selectionAnimation");
            Timeline incomingDrawerAnimation = skinTimeline(drawerSecond.getSkin(), "selectionAnimation");
            outgoingListAnimation.jumpTo(Duration.millis(80.0));
            incomingListAnimation.jumpTo(Duration.millis(80.0));
            outgoingDrawerAnimation.jumpTo(Duration.millis(80.0));
            incomingDrawerAnimation.jumpTo(Duration.millis(80.0));
            root.layout();

            Region outgoingListSelection = listItemSelectionContainer(listFirst);
            Region incomingListSelection = listItemSelectionContainer(listSecond);
            Region outgoingDrawerSelection = listItemSelectionContainer(drawerFirst);
            Region incomingDrawerSelection = listItemSelectionContainer(drawerSecond);

            assertBetween(outgoingListSelection.getOpacity(), 0.0, 1.0, "outgoing list selection opacity");
            assertBetween(incomingListSelection.getOpacity(), 0.0, 1.0, "incoming list selection opacity");
            assertBetween(outgoingDrawerSelection.getOpacity(), 0.0, 1.0, "outgoing drawer selection opacity");
            assertBetween(incomingDrawerSelection.getOpacity(), 0.0, 1.0, "incoming drawer selection opacity");
            assertBetween(incomingListSelection.getScaleX(), 0.96, 1.0, "incoming list selection scale");
            assertBetween(incomingDrawerSelection.getScaleX(), 0.96, 1.0, "incoming drawer selection scale");
            assertBetween(incomingListSelection.getScaleY(), 0.96, 1.0, "incoming list selection vertical scale");
            assertBetween(incomingDrawerSelection.getScaleY(), 0.96, 1.0, "incoming drawer selection vertical scale");

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, incomingListSelection, Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, incomingDrawerSelection, Color.WHITE, 0.03);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-drawer-selection-animation-frame.png"
            ));

            outgoingListAnimation.jumpTo(Duration.millis(200.0));
            incomingListAnimation.jumpTo(Duration.millis(200.0));
            outgoingDrawerAnimation.jumpTo(Duration.millis(200.0));
            incomingDrawerAnimation.jumpTo(Duration.millis(200.0));
            root.layout();

            assertEquals(0.0, outgoingListSelection.getOpacity(), 0.0001);
            assertEquals(1.0, incomingListSelection.getOpacity(), 0.0001);
            assertEquals(0.0, outgoingDrawerSelection.getOpacity(), 0.0001);
            assertEquals(1.0, incomingDrawerSelection.getOpacity(), 0.0001);
            assertEquals(0.96, outgoingListSelection.getScaleX(), 0.0001);
            assertEquals(1.0, incomingListSelection.getScaleX(), 0.0001);
            assertEquals(0.96, outgoingDrawerSelection.getScaleX(), 0.0001);
            assertEquals(1.0, incomingDrawerSelection.getScaleX(), 0.0001);
            assertEquals(0.96, outgoingListSelection.getScaleY(), 0.0001);
            assertEquals(1.0, incomingListSelection.getScaleY(), 0.0001);
            assertEquals(0.96, outgoingDrawerSelection.getScaleY(), 0.0001);
            assertEquals(1.0, incomingDrawerSelection.getScaleY(), 0.0001);
            stopTimelines(
                    outgoingListAnimation,
                    incomingListAnimation,
                    outgoingDrawerAnimation,
                    incomingDrawerAnimation
            );
        });
    }

    /// Verifies that mouse selection keeps ripple feedback while the selected container transitions.
    @Test
    void navigationDrawerMouseSelectionKeepsRippleAndAnimatesSelection() {
        runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("Sheets");
            M3ListItem second = new M3ListItem("Bottom sheets");
            M3NavigationDrawer drawer = new M3NavigationDrawer(first, second);
            drawer.select(first);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 180.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 180.0);
            root.layout();
            drawer.applyCss();
            first.applyCss();
            second.applyCss();
            drawer.resize(320.0, 116.0);
            drawer.layout();
            first.layout();
            second.layout();

            double clickX = second.getWidth() / 2.0;
            double clickY = second.getHeight() / 2.0;
            second.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, clickX, clickY, true));
            Region ripple = lookupRegion(second, ".m3-ripple");
            assertTrue(ripple.getOpacity() > 0.0);
            second.fire();
            root.applyCss();

            Region selection = listItemSelectionContainer(second);
            Timeline selectionAnimation = skinTimeline(second.getSkin(), "selectionAnimation");

            assertTrue(second.isSelected());
            assertEquals(javafx.animation.Animation.Status.RUNNING, selectionAnimation.getStatus());
            second.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, clickX, clickY, false));
            assertTrue(ripple.getOpacity() > 0.0);

            selectionAnimation.jumpTo(Duration.millis(80.0));
            root.layout();

            assertBetween(selection.getOpacity(), 0.0, 1.0, "mouse-selected drawer item opacity");
            assertBetween(selection.getScaleX(), 0.96, 1.0, "mouse-selected drawer item horizontal scale");
            assertBetween(selection.getScaleY(), 0.96, 1.0, "mouse-selected drawer item vertical scale");

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, selection, Color.WHITE, 0.03);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-click-selection-frame.png"
            ));
            selectionAnimation.stop();
        });
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
        M3NavigationItem item = createNavigationItem("Inbox", new M3Icon("I"), badge, true);

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

    /// Verifies that navigation item badges stay anchored to logical end in right-to-left layouts.
    @Test
    void navigationItemMirrorsBadgeForRightToLeft() {
        M3Badge badge = new M3Badge("3");
        M3NavigationItem item = createNavigationItem("Inbox", new M3Icon("I"), badge, true);
        item.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(item);
        Scene scene = new Scene(root, 120.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        item.resize(80.0, 80.0);
        item.layout();

        StackPane badgeContainer = assertInstanceOf(
                StackPane.class,
                item.lookup(".m3-navigation-item-badge-container")
        );
        assertEquals(Pos.TOP_LEFT, badgeContainer.getAlignment());
        assertEquals(Pos.TOP_LEFT, StackPane.getAlignment(badge));
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

        navigationBar.selectNext();

        assertEquals(home, navigationBar.getSelectedItem());
        assertTrue(home.isSelected());
        assertFalse(search.isSelected());

        navigationBar.selectPrevious();

        assertEquals(search, navigationBar.getSelectedItem());
        assertFalse(home.isSelected());
        assertTrue(search.isSelected());

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

        navigationRail.selectNext();

        assertEquals(home, navigationRail.getSelectedItem());
        assertTrue(home.isSelected());
        assertFalse(search.isSelected());

        navigationRail.selectPrevious();

        assertEquals(search, navigationRail.getSelectedItem());
        assertFalse(home.isSelected());
        assertTrue(search.isSelected());

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

        topAppBar.clearActions();
        topAppBar.addAction(search);
        topAppBar.setActions(more);

        assertEquals(java.util.List.of(more), topAppBar.getActions());

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

        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);

        assertEquals(M3TopAppBarVariant.MEDIUM, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM.getStyleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.CENTER_ALIGNED.getStyleClass()));
        assertEquals(Region.USE_COMPUTED_SIZE, topAppBar.getPrefHeight(), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.LARGE);

        assertEquals(M3TopAppBarVariant.LARGE, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.LARGE.getStyleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM.getStyleClass()));
        assertEquals(Region.USE_COMPUTED_SIZE, topAppBar.getPrefHeight(), 0.0001);

        topAppBar.variantProperty().set(null);

        assertEquals(M3TopAppBarVariant.SMALL, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.getStyleClass()));
        assertEquals(Region.USE_COMPUTED_SIZE, topAppBar.getPrefHeight(), 0.0001);
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
        assertInstanceOf(M3TopAppBarSkin.class, topAppBar.getSkin());
        assertInstanceOf(Label.class, topAppBar.lookup("." + M3TopAppBar.TITLE_STYLE_CLASS));
        HBox actions = assertInstanceOf(HBox.class, topAppBar.lookup("." + M3TopAppBar.ACTIONS_STYLE_CLASS));
        assertEquals(8.0, actions.getSpacing(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(112.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(20.0, topAppBar.getPadding().getBottom(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(152.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(28.0, topAppBar.getPadding().getBottom(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.SMALL);
        root.applyCss();

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(72.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(24.0, topAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(12.0, actions.getSpacing(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(120.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(24.0, topAppBar.getPadding().getBottom(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(160.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(32.0, topAppBar.getPadding().getBottom(), 0.0001);
    }

    /// Verifies that top app bar icon buttons do not clip fallback glyphs.
    @Test
    void topAppBarFallbackIconButtonsDoNotClipGlyphs() {
        runOnFxThread(() -> {
            M3Icon navigationIcon = new M3Icon("M", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            M3Icon searchIcon = new M3Icon("S", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            M3Icon accountIcon = new M3Icon("A", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            M3TopAppBar topAppBar = new M3TopAppBar(
                    "Inbox",
                    new M3IconButton(navigationIcon),
                    new M3IconButton(searchIcon),
                    new M3IconButton(accountIcon)
            );
            topAppBar.setPrefWidth(560.0);

            StackPane root = new StackPane(topAppBar);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 640.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(640.0, 96.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            for (M3Icon icon : java.util.List.of(navigationIcon, searchIcon, accountIcon)) {
                assertSnapshotNodeContainsContrast(image, icon, Color.WHITE, 0.05);
                assertSnapshotNodeEdgesClear(image, icon, Color.WHITE, 0.05);
            }
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-top-app-bar-fallback-icons.png"
            ));
        });
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

        bottomAppBar.clearActions();
        bottomAppBar.addAction(search);
        bottomAppBar.setActions(more);

        assertEquals(java.util.List.of(more), bottomAppBar.getActions());

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
        assertInstanceOf(M3BottomAppBarSkin.class, bottomAppBar.getSkin());
        HBox actions = assertInstanceOf(HBox.class, bottomAppBar.lookup("." + M3BottomAppBar.ACTIONS_STYLE_CLASS));
        assertEquals(8.0, actions.getSpacing(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(88.0, bottomAppBar.getPrefHeight(), 0.0001);
        assertEquals(24.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(12.0, actions.getSpacing(), 0.0001);
    }

    /// Verifies that app bars and banners create Material Design 3 skins.
    @Test
    void appBarsAndBannersCreateMaterialSkins() {
        M3Banner banner = new M3Banner("Message");
        M3TopAppBar topAppBar = new M3TopAppBar("Inbox");
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        Pane root = new Pane(banner, topAppBar, bottomAppBar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3BannerSkin.class, banner.getSkin());
        assertInstanceOf(M3TopAppBarSkin.class, topAppBar.getSkin());
        assertInstanceOf(M3BottomAppBarSkin.class, bottomAppBar.getSkin());
        assertFalse(HBox.class.isAssignableFrom(M3Banner.class));
        assertFalse(HBox.class.isAssignableFrom(M3TopAppBar.class));
        assertFalse(HBox.class.isAssignableFrom(M3BottomAppBar.class));
    }

    /// Verifies that app bars and banners mirror logical leading and trailing slots in right-to-left layouts.
    @Test
    void appBarsAndBannersMirrorLogicalSlotsForRightToLeft() {
        runOnFxThread(() -> {
            Label topNavigation = new Label("N");
            Label topAction = new Label("A");
            M3TopAppBar topAppBar = new M3TopAppBar("Inbox", topNavigation, topAction);
            topAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Label bottomAction = new Label("B");
            Label floatingAction = new Label("F");
            M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomAction
            );
            bottomAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Label bannerIcon = new Label("I");
            Label bannerAction = new Label("C");
            M3Banner banner = createBanner("Message", bannerIcon, bannerAction);
            banner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Pane root = new Pane(topAppBar, bottomAppBar, banner);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 460.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            topAppBar.resizeRelocate(20.0, 20.0, 420.0, 64.0);
            bottomAppBar.resizeRelocate(20.0, 96.0, 420.0, 80.0);
            banner.resizeRelocate(20.0, 188.0, 420.0, 48.0);
            root.layout();
            topAppBar.layout();
            bottomAppBar.layout();
            banner.layout();

            assertTrue(
                    topNavigation.localToScene(topNavigation.getBoundsInLocal()).getMinX()
                            > topAction.localToScene(topAction.getBoundsInLocal()).getMaxX()
            );
            assertTrue(
                    floatingAction.localToScene(floatingAction.getBoundsInLocal()).getMaxX()
                            < bottomAction.localToScene(bottomAction.getBoundsInLocal()).getMinX()
            );
            assertTrue(
                    bannerIcon.localToScene(bannerIcon.getBoundsInLocal()).getMinX()
                            > bannerAction.localToScene(bannerAction.getBoundsInLocal()).getMaxX()
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, topNavigation, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, floatingAction, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, bannerIcon, Color.WHITE, 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-app-bars-banner-rtl.png"
            ));
        });
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

        navigationDrawer.selectNext();

        assertEquals(home, navigationDrawer.getSelectedItem());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertTrue(home.isSelected());
        assertFalse(search.isSelected());

        navigationDrawer.selectPrevious();

        assertEquals(search, navigationDrawer.getSelectedItem());
        assertEquals(2, navigationDrawer.getSelectedIndex());
        assertFalse(home.isSelected());
        assertTrue(search.isSelected());

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

    /// Verifies that navigation drawers support collapsible destination groups.
    @Test
    void navigationDrawerSupportsCollapsibleGroups() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem buttons = new M3ListItem("Buttons");
        M3ListItem fabs = new M3ListItem("FABs");
        group.addItems(buttons, fabs);
        M3ListItem overview = new M3ListItem("Overview");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(group, overview);

        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertEquals(2, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(group.getHeaderItem(), navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(overview, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertInstanceOf(M3DisclosureIcon.class, group.getHeaderItem().getTrailing());

        group.getHeaderItem().fire();

        assertTrue(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(4, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(buttons, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        buttons.fire();

        assertEquals(buttons, navigationDrawer.getSelectedItem());
        assertEquals(1, navigationDrawer.getSelectedIndex());
        assertTrue(buttons.isSelected());
        assertFalse(group.getHeaderItem().isSelected());

        group.setExpanded(false);

        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertTrue(group.getHeaderItem().isSelected());
        assertFalse(buttons.isSelected());
        navigationDrawer.selectNext();
        assertEquals(overview, navigationDrawer.getSelectedItem());

        group.setExpanded(false);
        navigationDrawer.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, fabs);

        assertTrue(group.isExpanded());
        assertEquals(fabs, navigationDrawer.getSelectedItem());
        assertEquals(2, navigationDrawer.getSelectedIndex());

        group.setExpanded(false);
        navigationDrawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, buttons);

        assertTrue(group.isExpanded());
    }

    /// Verifies that navigation drawers expand and collapse destination groups from keyboard disclosure keys.
    @Test
    void navigationDrawerHandlesGroupDisclosureKeys() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem commonButtons = new M3ListItem("Common buttons");
        M3ListItem floatingActions = new M3ListItem("Floating actions");
        group.addItems(commonButtons, floatingActions);
        M3ListItem overview = new M3ListItem("Overview");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(group, overview);

        KeyEvent expandEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
        navigationDrawer.fireEvent(expandEvent);

        assertTrue(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(4, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        navigationDrawer.select(commonButtons);
        KeyEvent collapseFromChildEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);
        navigationDrawer.fireEvent(collapseFromChildEvent);

        assertFalse(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(2, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        KeyEvent nextEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        navigationDrawer.fireEvent(nextEvent);

        assertEquals(overview, navigationDrawer.getSelectedItem());
    }

    /// Verifies that right-to-left navigation drawers mirror disclosure arrow keys.
    @Test
    void navigationDrawerMirrorsGroupDisclosureKeysForRightToLeft() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem commonButtons = new M3ListItem("Common buttons");
        M3ListItem floatingActions = new M3ListItem("Floating actions");
        group.addItems(commonButtons, floatingActions);
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(group);
        navigationDrawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        KeyEvent expandEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);
        navigationDrawer.fireEvent(expandEvent);

        assertTrue(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());

        navigationDrawer.select(commonButtons);
        KeyEvent collapseFromChildEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
        navigationDrawer.fireEvent(collapseFromChildEvent);

        assertFalse(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
    }

    /// Verifies that navigation drawer groups expose disclosure accessibility state and actions.
    @Test
    void navigationDrawerGroupExposesAccessibleDisclosureStateAndActions() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem commonButtons = new M3ListItem("Common buttons");
        M3ListItem floatingActions = new M3ListItem("Floating actions");
        group.addItems(commonButtons, floatingActions);

        applyCss(group);

        assertEquals("Buttons", group.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(false, group.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(group.getHeaderItem(), group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertEquals(1, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(group.getHeaderItem(), group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        group.executeAccessibleAction(AccessibleAction.FIRE);

        assertTrue(group.isExpanded());
        assertEquals(true, group.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(3, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(commonButtons, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(floatingActions, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

        group.executeAccessibleAction(AccessibleAction.COLLAPSE);

        assertFalse(group.isExpanded());
        assertEquals(1, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 2);

        assertTrue(group.isExpanded());
        assertEquals(floatingActions, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

        group.executeAccessibleAction(AccessibleAction.COLLAPSE);
        group.executeAccessibleAction(AccessibleAction.EXPAND);

        assertTrue(group.isExpanded());
    }

    /// Verifies that navigation drawer groups expose their skin and visible child structure.
    @Test
    void navigationDrawerGroupCreatesMaterialSkinAndTogglesChildren() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem buttons = new M3ListItem("Buttons");
        M3ListItem fabs = new M3ListItem("FABs");
        group.addItems(buttons, fabs);
        Pane root = new Pane(group);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3NavigationDrawerGroupSkin.class, group.getSkin());
        assertEquals(0, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());

        group.setExpanded(true);
        root.applyCss();
        group.layout();

        assertEquals(2, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());
        assertEquals(56.0, group.getHeaderItem().getOneLineHeight(), 0.0001);
        assertEquals(56.0, buttons.getOneLineHeight(), 0.0001);
        assertEquals(32.0, buttons.getHorizontalPadding(), 0.0001);
    }

    /// Verifies that selected child rows indent from the parent while aligning their right edge.
    @Test
    void navigationDrawerGroupChildSelectionPillIndentsAndAlignsRight() {
        runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3ListItem sideSheets = new M3ListItem("Side sheets");
            group.addItems(bottomSheets, sideSheets);
            group.setExpanded(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
            drawer.select(bottomSheets);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 260.0);
            root.layout();
            group.layout();
            bottomSheets.layout();

            Bounds headerBounds = group.getHeaderItem().localToScene(group.getHeaderItem().getBoundsInLocal());
            Bounds childBounds = bottomSheets.localToScene(bottomSheets.getBoundsInLocal());
            Region childSelection = listItemSelectionContainer(bottomSheets);

            assertEquals(headerBounds.getMaxX(), childBounds.getMaxX(), 0.0001);
            assertTrue(childBounds.getMinX() > headerBounds.getMinX() + 6.0);
            assertEquals(headerBounds.getHeight(), childBounds.getHeight(), 0.0001);
            assertEquals(childBounds.getWidth(), childSelection.getWidth(), 0.0001);
            assertEquals(56.0, bottomSheets.getOneLineHeight(), 0.0001);
            assertRegionRadii(childSelection, 28.0, 28.0, 28.0, 28.0);

            WritableImage image = snapshotImageOnFxThread(root);
            Color beforeChildPill = snapshotNodePixel(image, bottomSheets, -4.0, bottomSheets.getHeight() / 2.0);
            Color insideChildPill = snapshotNodePixel(image, bottomSheets, 12.0, bottomSheets.getHeight() / 2.0);
            assertTrue(colorDistance(beforeChildPill, insideChildPill) > 0.01);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-child-selection.png"
            ));
        });
    }

    /// Verifies that selected child rows mirror their indentation in right-to-left drawers.
    @Test
    void navigationDrawerGroupChildSelectionPillMirrorsIndentForRightToLeft() {
        runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3ListItem sideSheets = new M3ListItem("Side sheets");
            group.addItems(bottomSheets, sideSheets);
            group.setExpanded(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
            drawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            drawer.select(bottomSheets);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 260.0);
            root.layout();
            group.layout();
            bottomSheets.layout();

            Bounds headerBounds = group.getHeaderItem().localToScene(group.getHeaderItem().getBoundsInLocal());
            Bounds childBounds = bottomSheets.localToScene(bottomSheets.getBoundsInLocal());
            Region childSelection = listItemSelectionContainer(bottomSheets);

            assertEquals(headerBounds.getMinX(), childBounds.getMinX(), 0.0001);
            assertTrue(childBounds.getMaxX() < headerBounds.getMaxX() - 6.0);
            assertEquals(headerBounds.getHeight(), childBounds.getHeight(), 0.0001);
            assertEquals(childBounds.getWidth(), childSelection.getWidth(), 0.0001);
            assertRegionRadii(childSelection, 28.0, 28.0, 28.0, 28.0);

            WritableImage image = snapshotImageOnFxThread(root);
            Color afterChildPill =
                    snapshotNodePixel(image, bottomSheets, bottomSheets.getWidth() + 4.0, bottomSheets.getHeight() / 2.0);
            Color insideChildPill =
                    snapshotNodePixel(image, bottomSheets, bottomSheets.getWidth() - 12.0, bottomSheets.getHeight() / 2.0);
            assertTrue(colorDistance(afterChildPill, insideChildPill) > 0.01);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-child-selection-rtl.png"
            ));
        });
    }

    /// Verifies that navigation drawer groups animate child row expansion and collapse.
    @Test
    void navigationDrawerGroupAnimatesExpansionAndCollapse() throws InterruptedException {
        AtomicReference<M3NavigationDrawerGroup> groupReference = new AtomicReference<>();
        AtomicReference<Double> collapsedHeightReference = new AtomicReference<>();
        AtomicReference<Double> expandedHeightReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(
                Duration.millis(90.0),
                () -> {
                    M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
                    M3ListItem buttons = new M3ListItem("Buttons");
                    M3ListItem iconButtons = new M3ListItem("Icon buttons");
                    group.addItems(buttons, iconButtons);
                    Pane root = new Pane(group);
                    Scene scene = new Scene(root, 280.0, 200.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    root.resize(280.0, 200.0);
                    root.layout();

                    double collapsedHeight = group.prefHeight(240.0);
                    double expandedHeight = group.getHeaderItem().prefHeight(240.0)
                            + 4.0
                            + buttons.prefHeight(240.0)
                            + 4.0
                            + iconButtons.prefHeight(240.0);
                    groupReference.set(group);
                    collapsedHeightReference.set(collapsedHeight);
                    expandedHeightReference.set(expandedHeight);
                    group.setExpanded(true);
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());
                    double height = group.prefHeight(240.0);

                    assertTrue(height > collapsedHeightReference.get());
                    assertTrue(height < expandedHeightReference.get());
                    assertEquals(2, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());
                }
        );

        runOnFxThreadAfterDelay(
                Duration.millis(300.0),
                () -> {
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());

                    assertEquals(expandedHeightReference.get(), group.prefHeight(240.0), 0.5);
                    group.setExpanded(false);
                }
        );

        runOnFxThreadAfterDelay(
                Duration.millis(90.0),
                () -> {
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());
                    double height = group.prefHeight(240.0);

                    assertTrue(height > collapsedHeightReference.get());
                    assertTrue(height < expandedHeightReference.get());
                    assertEquals(2, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());
                }
        );

        runOnFxThreadAfterDelay(
                Duration.millis(300.0),
                () -> {
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());

                    assertEquals(collapsedHeightReference.get(), group.prefHeight(240.0), 0.5);
                    assertEquals(0, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());
                }
        );
    }

    /// Verifies that navigation containers delegate layout to Material Design 3 skins.
    @Test
    void navigationContainersCreateMaterialSkins() {
        M3NavigationItem barHome = new M3NavigationItem("Home");
        M3NavigationItem railHome = new M3NavigationItem("Home");
        M3ListItem drawerHome = new M3ListItem("Home");
        M3NavigationBar navigationBar = new M3NavigationBar(barHome);
        M3NavigationRail navigationRail = new M3NavigationRail(railHome);
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(drawerHome);
        Pane root = new Pane(navigationBar, navigationRail, navigationDrawer);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3NavigationBarSkin.class, navigationBar.getSkin());
        assertInstanceOf(M3NavigationRailSkin.class, navigationRail.getSkin());
        assertInstanceOf(M3NavigationDrawerSkin.class, navigationDrawer.getSkin());
        assertSame(barHome, navigationBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertSame(railHome, navigationRail.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertSame(drawerHome, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
    }

    /// Verifies that selection containers handle keyboard navigation and skip disabled children.
    @Test
    void selectionContainersHandleKeyboardNavigationAndSkipDisabledChildren() {
        M3IconToggleButton iconFirst = new M3IconToggleButton("A");
        M3IconToggleButton iconSecond = new M3IconToggleButton("B");
        M3IconToggleButton iconThird = new M3IconToggleButton("C");
        iconSecond.setDisable(true);
        M3IconToggleButtonGroup iconGroup = new M3IconToggleButtonGroup(iconFirst, iconSecond, iconThird);

        iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(iconFirst, iconGroup.getSelectedButton());
        iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(iconThird, iconGroup.getSelectedButton());

        M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
        M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
        segmentSecond.setDisable(true);
        M3SegmentedButtonGroup segmentedGroup =
                new M3SegmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);

        segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
        segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(segmentThird, segmentedGroup.getSelectedButton());
        segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());

        M3Chip chipFirst = new M3Chip("Input");
        M3Chip chipSecond = new M3Chip("Filter");
        M3Chip chipThird = new M3Chip("Assist");
        chipSecond.setDisable(true);
        M3ChipGroup chipGroup = new M3ChipGroup(chipFirst, chipSecond, chipThird);
        chipGroup.setSelectionMode(M3ChipSelectionMode.SINGLE);

        chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(chipFirst, chipGroup.getSelectedChip());
        chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(chipThird, chipGroup.getSelectedChip());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3Tab tabThird = new M3Tab("Activity");
        tabSecond.setDisable(true);
        M3TabBar tabBar = new M3TabBar(tabFirst, tabSecond, tabThird);

        tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(tabThird, tabBar.getSelectedTab());
        tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
        assertEquals(tabThird, tabBar.getSelectedTab());

        M3NavigationItem navFirst = new M3NavigationItem("Home");
        M3NavigationItem navSecond = new M3NavigationItem("Search");
        M3NavigationItem navThird = new M3NavigationItem("Inbox");
        navSecond.setDisable(true);
        M3NavigationBar navigationBar = new M3NavigationBar(navFirst, navSecond, navThird);

        navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(navThird, navigationBar.getSelectedItem());

        M3NavigationItem railFirst = new M3NavigationItem("Home");
        M3NavigationItem railSecond = new M3NavigationItem("Search");
        M3NavigationItem railThird = new M3NavigationItem("Inbox");
        railSecond.setDisable(true);
        M3NavigationRail navigationRail = new M3NavigationRail(railFirst, railSecond, railThird);
        navigationRail.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(railThird, navigationRail.getSelectedItem());

        M3ListItem drawerFirst = new M3ListItem("Home");
        M3ListItem drawerSecond = new M3ListItem("Search");
        M3ListItem drawerThird = new M3ListItem("Inbox");
        drawerSecond.setDisable(true);
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(drawerFirst, drawerSecond, drawerThird);

        navigationDrawer.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(drawerThird, navigationDrawer.getSelectedItem());

        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        listFirst.setDisable(true);
        M3ListPane list = new M3ListPane(listFirst, listSecond);
        list.setSelectionMode(M3ListSelectionMode.SINGLE);

        list.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(listSecond, list.getSelectedItem());

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        menuFirst.setDisable(true);
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);

        menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(menuSecond, menu.getSelectedItem());

        M3ListPane listWithoutSelection = new M3ListPane(new M3ListItem("Action"));
        KeyEvent listFocusEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        assertTrue(M3SelectionNavigation.handleKeyFocus(
                listFocusEvent,
                listWithoutSelection.getItems(),
                null,
                M3ListItem.class,
                false,
                true
        ));
        assertTrue(listFocusEvent.isConsumed());
        assertNull(listWithoutSelection.getSelectedItem());

        M3Menu menuWithoutSelection = new M3Menu(new M3MenuItem("Action"));
        KeyEvent menuFocusEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        assertTrue(M3SelectionNavigation.handleKeyFocus(
                menuFocusEvent,
                menuWithoutSelection.getItems(),
                null,
                M3MenuItem.class,
                false,
                true
        ));
        assertTrue(menuFocusEvent.isConsumed());
        assertNull(menuWithoutSelection.getSelectedItem());
    }

    /// Verifies that right-to-left controls mirror horizontal keyboard focus and selection.
    @Test
    void horizontalKeyboardNavigationMirrorsForRightToLeftLayouts() {
        runOnFxThread(() -> {
            M3Button groupFirst = new M3Button("First");
            M3Button groupSecond = new M3Button("Second");
            M3Button groupThird = new M3Button("Third");
            M3ButtonGroup buttonGroup = new M3ButtonGroup(groupFirst, groupSecond, groupThird);
            buttonGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButton iconThird = new M3IconToggleButton("C");
            M3IconToggleButtonGroup iconGroup =
                    new M3IconToggleButtonGroup(iconFirst, iconSecond, iconThird);
            iconGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            iconGroup.select(iconSecond);

            M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
            M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segmentedGroup =
                    new M3SegmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);
            segmentedGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            segmentedGroup.select(segmentSecond);

            M3Chip chipFirst = new M3Chip("Input");
            M3Chip chipSecond = new M3Chip("Filter");
            M3Chip chipThird = new M3Chip("Assist");
            M3ChipGroup chipGroup = new M3ChipGroup(chipFirst, chipSecond, chipThird);
            chipGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            chipGroup.setSelectionMode(M3ChipSelectionMode.SINGLE);
            chipGroup.select(chipSecond);

            M3Tab tabFirst = new M3Tab("Overview");
            M3Tab tabSecond = new M3Tab("Details");
            M3Tab tabThird = new M3Tab("Activity");
            M3TabBar tabBar = new M3TabBar(tabFirst, tabSecond, tabThird);
            tabBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            tabBar.select(tabSecond);

            M3NavigationItem navFirst = new M3NavigationItem("Home");
            M3NavigationItem navSecond = new M3NavigationItem("Search");
            M3NavigationItem navThird = new M3NavigationItem("Inbox");
            M3NavigationBar navigationBar = new M3NavigationBar(navFirst, navSecond, navThird);
            navigationBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            navigationBar.select(navSecond);

            M3SplitButton splitButton = new M3SplitButton("Create", new M3MenuItem("Draft"));
            splitButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Label carouselFirst = new Label("First");
            Label carouselSecond = new Label("Second");
            Label carouselThird = new Label("Third");
            M3Carousel carousel = new M3Carousel(carouselFirst, carouselSecond, carouselThird);
            carousel.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            carousel.select(carouselSecond);

            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 15));
            datePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 15),
                    LocalDate.of(2026, 5, 15)
            );
            dateRangePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Stage stage = new Stage();
            try {
                VBox root = new VBox(
                        buttonGroup,
                        iconGroup,
                        segmentedGroup,
                        chipGroup,
                        tabBar,
                        navigationBar,
                        splitButton,
                        carousel,
                        datePicker,
                        dateRangePicker
                );
                Scene scene = new Scene(root, 760.0, 640.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                groupSecond.requestFocus();
                buttonGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(groupFirst.isFocused());
                buttonGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(groupSecond.isFocused());

                iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(iconFirst, iconGroup.getSelectedButton());
                iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(iconSecond, iconGroup.getSelectedButton());

                segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
                segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(segmentSecond, segmentedGroup.getSelectedButton());

                chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(chipFirst, chipGroup.getSelectedChip());
                chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(chipSecond, chipGroup.getSelectedChip());

                tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(tabFirst, tabBar.getSelectedTab());
                tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(tabSecond, tabBar.getSelectedTab());

                navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(navFirst, navigationBar.getSelectedItem());
                navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(navSecond, navigationBar.getSelectedItem());

                splitButton.getMenuButton().requestFocus();
                splitButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(splitButton.getActionButton().isFocused());
                splitButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(splitButton.getMenuButton().isFocused());

                carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertSame(carouselFirst, carousel.getSelectedItem());
                carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertSame(carouselSecond, carousel.getSelectedItem());

                datePicker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(LocalDate.of(2026, 5, 14), datePicker.getValue());
                datePicker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(LocalDate.of(2026, 5, 15), datePicker.getValue());

                dateRangePicker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(LocalDate.of(2026, 5, 14), dateRangePicker.getStartDate());
                assertNull(dateRangePicker.getEndDate());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that single-selection containers use the focused child as their keyboard navigation anchor.
    @Test
    void singleSelectionContainersUseFocusedChildAsKeyboardAnchor() {
        runOnFxThread(() -> {
            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButton iconThird = new M3IconToggleButton("C");
            M3IconToggleButtonGroup iconGroup =
                    new M3IconToggleButtonGroup(iconFirst, iconSecond, iconThird);
            iconGroup.select(iconFirst);

            M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
            M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segmentedGroup =
                    new M3SegmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);
            segmentedGroup.select(segmentFirst);

            M3Chip chipFirst = new M3Chip("Input");
            M3Chip chipSecond = new M3Chip("Filter");
            M3Chip chipThird = new M3Chip("Assist");
            M3ChipGroup chipGroup = new M3ChipGroup(chipFirst, chipSecond, chipThird);
            chipGroup.setSelectionMode(M3ChipSelectionMode.SINGLE);
            chipGroup.select(chipFirst);

            M3ListItem listFirst = new M3ListItem("One");
            M3ListItem listSecond = new M3ListItem("Two");
            M3ListItem listThird = new M3ListItem("Three");
            M3ListPane list = new M3ListPane(listFirst, listSecond, listThird);
            list.setSelectionMode(M3ListSelectionMode.SINGLE);
            list.select(listFirst);

            M3Tab tabFirst = new M3Tab("Overview");
            M3Tab tabSecond = new M3Tab("Details");
            M3Tab tabThird = new M3Tab("Activity");
            M3TabBar tabBar = new M3TabBar(tabFirst, tabSecond, tabThird);
            tabBar.select(tabFirst);

            M3NavigationItem barFirst = new M3NavigationItem("Home");
            M3NavigationItem barSecond = new M3NavigationItem("Search");
            M3NavigationItem barThird = new M3NavigationItem("Inbox");
            M3NavigationBar navigationBar = new M3NavigationBar(barFirst, barSecond, barThird);
            navigationBar.select(barFirst);

            M3NavigationItem railFirst = new M3NavigationItem("Home");
            M3NavigationItem railSecond = new M3NavigationItem("Search");
            M3NavigationItem railThird = new M3NavigationItem("Inbox");
            M3NavigationRail navigationRail = new M3NavigationRail(railFirst, railSecond, railThird);
            navigationRail.select(railFirst);

            Stage stage = new Stage();
            try {
                VBox root = new VBox(iconGroup, segmentedGroup, chipGroup, list, tabBar, navigationBar, navigationRail);
                Scene scene = new Scene(root, 720.0, 520.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                iconSecond.requestFocus();
                iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(iconThird, iconGroup.getSelectedButton());

                segmentSecond.requestFocus();
                segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(segmentThird, segmentedGroup.getSelectedButton());

                chipSecond.requestFocus();
                chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(chipThird, chipGroup.getSelectedChip());

                listSecond.requestFocus();
                list.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertEquals(listThird, list.getSelectedItem());

                tabSecond.requestFocus();
                tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(tabThird, tabBar.getSelectedTab());

                barSecond.requestFocus();
                navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(barThird, navigationBar.getSelectedItem());

                railSecond.requestFocus();
                navigationRail.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertEquals(railThird, navigationRail.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that menus support printable-key type-ahead focus navigation.
    @Test
    void menuSupportsTypeAheadKeyboardNavigation() {
        runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem disabledSave = new M3MenuItem("Save");
            M3MenuItem settings = new M3MenuItem("Settings");
            disabledSave.setDisable(true);

            M3Menu menu = new M3Menu(open, archive, disabledSave, settings);
            menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
            menu.select(open);

            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(menu), 320.0, 220.0));
                stage.show();
                menu.applyCss();
                menu.layout();

                menu.fireEvent(keyTypedEvent("s"));
                assertEquals(settings, menu.getSelectedItem());
                assertTrue(settings.isFocused());
                assertFalse(disabledSave.isFocused());

                menu.fireEvent(keyTypedEvent("a"));
                menu.fireEvent(keyTypedEvent("r"));
                assertEquals(archive, menu.getSelectedItem());
                assertTrue(archive.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that selection containers expose a focusable child through accessibility focus routing.
    @Test
    void selectionContainersExposeAccessibleFocusTargets() {
        runOnFxThread(() -> {
            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButtonGroup iconGroup = new M3IconToggleButtonGroup(iconFirst, iconSecond);
            iconGroup.select(iconSecond);

            M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
            M3SegmentedButtonGroup segmentedGroup = new M3SegmentedButtonGroup(segmentFirst, segmentSecond);
            segmentedGroup.select(segmentSecond);

            M3Chip chipFirst = new M3Chip("Input");
            M3Chip chipSecond = new M3Chip("Filter");
            M3ChipGroup chipGroup = new M3ChipGroup(chipFirst, chipSecond);
            chipGroup.setSelectionMode(M3ChipSelectionMode.SINGLE);
            chipGroup.select(chipSecond);

            M3ListItem listFirst = new M3ListItem("One");
            M3ListItem listSecond = new M3ListItem("Two");
            M3ListPane list = new M3ListPane(listFirst, listSecond);
            list.setSelectionMode(M3ListSelectionMode.SINGLE);
            list.select(listSecond);

            M3ListItem passiveListFirst = new M3ListItem("Passive one");
            M3ListItem passiveListSecond = new M3ListItem("Passive two");
            M3ListPane passiveList = new M3ListPane(passiveListFirst, passiveListSecond);

            M3Tab tabFirst = new M3Tab("Overview");
            M3Tab tabSecond = new M3Tab("Details");
            M3TabBar tabBar = new M3TabBar(tabFirst, tabSecond);
            tabBar.select(tabSecond);

            M3NavigationItem barFirst = new M3NavigationItem("Home");
            M3NavigationItem barSecond = new M3NavigationItem("Search");
            M3NavigationBar navigationBar = new M3NavigationBar(barFirst, barSecond);
            navigationBar.select(barSecond);

            M3NavigationItem railFirst = new M3NavigationItem("Home");
            M3NavigationItem railSecond = new M3NavigationItem("Search");
            M3NavigationRail navigationRail = new M3NavigationRail(railFirst, railSecond);
            navigationRail.select(railSecond);

            M3ListItem drawerFirst = new M3ListItem("Inbox");
            M3ListItem drawerSecond = new M3ListItem("Archive");
            M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(drawerFirst, drawerSecond);
            navigationDrawer.select(drawerSecond);

            Stage stage = new Stage();
            try {
                VBox root = new VBox(
                        iconGroup,
                        segmentedGroup,
                        chipGroup,
                        list,
                        passiveList,
                        tabBar,
                        navigationBar,
                        navigationRail,
                        navigationDrawer
                );
                Scene scene = new Scene(root, 760.0, 640.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertAccessibleFocus(iconGroup, iconSecond);
                assertAccessibleFocus(segmentedGroup, segmentSecond);
                assertAccessibleFocus(chipGroup, chipSecond);
                assertAccessibleFocus(list, listSecond);
                assertAccessibleFocus(passiveList, passiveListFirst);
                assertAccessibleFocus(tabBar, tabSecond);
                assertAccessibleFocus(navigationBar, barSecond);
                assertAccessibleFocus(navigationRail, railSecond);
                assertAccessibleFocus(navigationDrawer, drawerSecond);

                iconSecond.setDisable(true);
                assertAccessibleFocus(iconGroup, iconFirst);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that multi-selection containers use arrow keys for focus movement without changing selection.
    @Test
    void multiSelectionContainersKeepSelectionDuringKeyboardFocusNavigation() {
        M3Chip chipFirst = createChip("Input", M3ChipVariant.INPUT, true);
        M3Chip chipSecond = createChip("Filter", M3ChipVariant.FILTER, false);
        M3ChipGroup chipGroup = new M3ChipGroup(chipFirst, chipSecond);
        KeyEvent chipEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

        chipGroup.fireEvent(chipEvent);

        assertEquals(java.util.List.of(chipFirst), chipGroup.getSelectedChips());
        assertFalse(chipSecond.isSelected());

        M3IconToggleButton iconFirst = createIconToggleButton(
                "edit",
                M3IconToggleButtonVariant.STANDARD,
                true
        );
        M3IconToggleButton iconSecond = createIconToggleButton(
                "done",
                M3IconToggleButtonVariant.STANDARD,
                false
        );
        M3IconToggleButtonGroup iconGroup = new M3IconToggleButtonGroup(iconFirst, iconSecond);
        iconGroup.setSelectionMode(M3IconToggleButtonSelectionMode.MULTIPLE);
        KeyEvent iconEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

        iconGroup.fireEvent(iconEvent);

        assertEquals(java.util.List.of(iconFirst), iconGroup.getSelectedButtons());
        assertFalse(iconSecond.isSelected());

        M3SegmentedButton segmentFirst = createSegmentedButton("Day", true);
        M3SegmentedButton segmentSecond = createSegmentedButton("Week", false);
        M3SegmentedButtonGroup segmentGroup = new M3SegmentedButtonGroup(segmentFirst, segmentSecond);
        segmentGroup.setSelectionMode(M3SegmentedButtonSelectionMode.MULTIPLE);
        KeyEvent segmentEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

        segmentGroup.fireEvent(segmentEvent);

        assertEquals(java.util.List.of(segmentFirst), segmentGroup.getSelectedButtons());
        assertFalse(segmentSecond.isSelected());

        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        M3ListPane list = new M3ListPane(listFirst, listSecond);
        list.setSelectionMode(M3ListSelectionMode.MULTIPLE);
        listFirst.setSelected(true);
        KeyEvent listEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);

        list.fireEvent(listEvent);

        assertEquals(java.util.List.of(listFirst), list.getSelectedItems());
        assertFalse(listSecond.isSelected());

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3MenuSelectionMode.MULTIPLE);
        menuFirst.setSelected(true);
        KeyEvent menuEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);

        menu.fireEvent(menuEvent);

        assertEquals(java.util.List.of(menuFirst), menu.getSelectedItems());
        assertFalse(menuSecond.isSelected());
    }

    /// Verifies that composite controls can focus indexed children through accessibility show-item actions.
    @Test
    void compositeControlsSupportAccessibleShowItemActions() {
        runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3ButtonGroup group = new M3ButtonGroup(first, second);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(group), 240.0, 80.0));
                stage.show();

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(second.isFocused());

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, first);
                assertTrue(first.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that structural indexed containers focus or reveal requested accessibility items.
    @Test
    void structuralIndexedContainersSupportAccessibleShowItemActions() {
        runOnFxThread(() -> {
            M3Button navigation = new M3Button("Navigation");
            M3Button topAction = new M3Button("Top action");
            M3TopAppBar topAppBar = new M3TopAppBar("Inbox", navigation, topAction);

            M3Button bottomAction = new M3Button("Bottom action");
            M3Button floatingAction = new M3Button("Floating action");
            M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomAction
            );

            M3Button bannerAction = new M3Button("Dismiss");
            M3Banner banner = createBanner("Network unavailable", new M3Icon("!"), bannerAction);
            M3Button surfaceAction = new M3Button("Surface action");
            M3Surface surface = new M3Surface(surfaceAction);
            M3Button badgedContent = new M3Button("Messages");
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, new M3Badge("3"));

            M3TextField textField = new M3TextField();
            M3Button leading = new M3Button("Leading");
            M3TextInputLayout inputLayout = new M3TextInputLayout(textField, "Helper text");
            inputLayout.setLeading(leading);

            M3ListItem firstResult = new M3ListItem("First result");
            M3ListItem secondResult = new M3ListItem("Second result");
            M3SearchView searchView = new M3SearchView("Search", firstResult, secondResult);
            M3Button fabAction = new M3Button("Create note");
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.addItem(fabAction);

            VBox root = new VBox(
                    topAppBar,
                    bottomAppBar,
                    banner,
                    surface,
                    badgedBox,
                    inputLayout,
                    searchView,
                    fabMenu
            );
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 560.0, 560.0));
                stage.show();

                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(topAction.isFocused());

                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(floatingAction.isFocused());

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(bannerAction.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(surfaceAction.isFocused());

                badgedBox.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(badgedContent.isFocused());

                inputLayout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(textField.isFocused());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(searchView.isActive());
                assertTrue(secondResult.isFocused());

                fabMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(fabMenu.isExpanded());
                assertTrue(fabAction.isFocused());
            } finally {
                stage.close();
            }
        });

        runOnFxThread(() -> {
            M3SnackbarHost snackbarHost = new M3SnackbarHost();
            M3Snackbar first = new M3Snackbar("First");
            M3Snackbar second = new M3Snackbar("Second");
            snackbarHost.show(first);
            snackbarHost.enqueue(second);

            snackbarHost.executeAccessibleAction(AccessibleAction.SHOW_ITEM, second);

            assertSame(second, snackbarHost.getSnackbar());
            assertTrue(snackbarHost.getQueue().isEmpty());
        });
    }

    /// Verifies that indexed structural containers expose default accessibility focus targets.
    @Test
    void structuralIndexedContainersExposeAccessibleFocusTargets() {
        runOnFxThread(() -> {
            M3Button groupFirst = new M3Button("First");
            M3Button groupSecond = new M3Button("Second");
            M3ButtonGroup buttonGroup = new M3ButtonGroup(groupFirst, groupSecond);

            M3Button badgedContent = new M3Button("Inbox");
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, new M3Badge("4"));

            M3Button bannerAction = new M3Button("Dismiss");
            M3Banner banner = createBanner("Offline", new M3Icon("!"), bannerAction);

            M3Button topNavigation = new M3Button("Menu");
            M3Button topAction = new M3Button("Search");
            M3TopAppBar topAppBar = new M3TopAppBar("Inbox", topNavigation, topAction);

            M3Button bottomAction = new M3Button("Archive");
            M3Button floatingAction = new M3Button("Create");
            M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomAction
            );

            M3Button surfaceAction = new M3Button("Surface action");
            M3Surface surface = new M3Surface(surfaceAction);

            M3SplitButton splitButton = new M3SplitButton("Create", new M3MenuItem("Draft"));

            M3Button formItem = new M3Button("Form item");
            M3FormPane formPane = new M3FormPane(formItem);

            M3Button sectionItem = new M3Button("Section item");
            M3FormSection formSection = new M3FormSection("Section", sectionItem);

            M3Button rowContent = new M3Button("Row content");
            M3Button rowTrailing = new M3Button("Row trailing");
            M3FormRow formRow = new M3FormRow("Row", "Helper", rowContent, rowTrailing);

            M3FabMenu fabMenu = new M3FabMenu(new M3FloatingActionButton(new M3Icon("+")));

            M3Button carouselFirst = new M3Button("One");
            M3Button carouselSecond = new M3Button("Two");
            M3Carousel carousel = new M3Carousel(carouselFirst, carouselSecond);
            carousel.select(carouselSecond);

            M3TextField invalidField = new M3TextField();
            M3TextInputLayout invalidLayout = new M3TextInputLayout(invalidField, "Name");
            invalidLayout.setValidator(M3TextInputValidators.required("Required"));
            M3FormValidator validator = new M3FormValidator(invalidLayout);
            M3ValidationSummary validationSummary = new M3ValidationSummary(validator);
            assertFalse(validator.validate());

            VBox root = new VBox(
                    buttonGroup,
                    badgedBox,
                    banner,
                    topAppBar,
                    bottomAppBar,
                    surface,
                    splitButton,
                    formPane,
                    formSection,
                    formRow,
                    fabMenu,
                    carousel,
                    invalidLayout,
                    validationSummary
            );
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 760.0, 860.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertAccessibleFocus(buttonGroup, groupFirst);
                assertAccessibleFocus(badgedBox, badgedContent);
                assertAccessibleFocus(banner, bannerAction);
                assertAccessibleFocus(topAppBar, topNavigation);
                assertAccessibleFocus(bottomAppBar, bottomAction);
                assertAccessibleFocus(surface, surfaceAction);
                assertAccessibleFocus(splitButton, splitButton.getActionButton());
                assertAccessibleFocus(formPane, formItem);
                assertAccessibleFocus(formSection, sectionItem);
                assertAccessibleFocus(formRow, rowContent);
                assertAccessibleFocus(fabMenu, fabMenu.getToggleButton());
                assertAccessibleFocus(carousel, carouselSecond);
                assertAccessibleFocus(validationSummary, invalidField);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that form rows mirror content and trailing slots in right-to-left layouts.
    @Test
    void formRowMirrorsLogicalSlotsForRightToLeft() {
        Label content = new Label("Content");
        Label trailing = new Label("Trailing");
        M3FormRow formRow = new M3FormRow("Label", "Helper", content, trailing);
        formRow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(formRow);
        Scene scene = new Scene(root, 520.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        formRow.resize(480.0, 72.0);
        formRow.layout();

        Node labelColumn = Objects.requireNonNull(formRow.lookup("." + M3FormRow.TEXT_COLUMN_STYLE_CLASS));
        Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
        Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
        Bounds labelBounds = labelColumn.localToScene(labelColumn.getBoundsInLocal());

        assertTrue(labelBounds.getMinX() > contentBounds.getMaxX(),
                () -> "labelBounds=" + labelBounds + ", contentBounds=" + contentBounds);
        assertTrue(contentBounds.getMinX() > trailingBounds.getMaxX(),
                () -> "contentBounds=" + contentBounds + ", trailingBounds=" + trailingBounds);
    }

    /// Verifies that picker skins propagate right-to-left orientation into their rendered layout nodes.
    @Test
    void pickerSkinsPropagateRightToLeftOrientation() {
        runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            datePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            dateRangePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));
            timePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            timePicker.setPrefWidth(360.0);

            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
            dateField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            dateField.setCommonPresets(LocalDate.of(2026, 5, 18));
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            rangeField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            rangeField.setCommonPresets(LocalDate.of(2026, 5, 18));
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
            timeField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            timeField.setCommonPresets(LocalTime.of(10, 30));

            Pane root = new Pane(datePicker, dateRangePicker, timePicker, dateField, rangeField, timeField);
            Scene scene = new Scene(root, 1100.0, 720.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1100.0, 720.0);
            datePicker.resizeRelocate(20.0, 20.0, 340.0, 320.0);
            dateRangePicker.resizeRelocate(390.0, 20.0, 340.0, 320.0);
            timePicker.resizeRelocate(20.0, 360.0, 440.0, 220.0);
            dateField.resizeRelocate(580.0, 360.0, 420.0, 96.0);
            rangeField.resizeRelocate(580.0, 460.0, 480.0, 96.0);
            timeField.resizeRelocate(580.0, 560.0, 420.0, 96.0);
            root.layout();
            datePicker.layout();
            dateRangePicker.layout();
            timePicker.layout();

            HBox dateHeader = assertInstanceOf(
                    HBox.class,
                    datePicker.lookup("." + M3DatePicker.HEADER_STYLE_CLASS)
            );
            HBox dateWeekdays = assertInstanceOf(
                    HBox.class,
                    datePicker.lookup("." + M3DatePicker.WEEKDAY_ROW_STYLE_CLASS)
            );
            HBox rangeHeader = assertInstanceOf(
                    HBox.class,
                    dateRangePicker.lookup("." + M3DatePicker.HEADER_STYLE_CLASS)
            );
            HBox timeDisplay = assertInstanceOf(
                    HBox.class,
                    timePicker.lookup("." + M3TimePicker.DISPLAY_STYLE_CLASS)
            );

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, dateHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, dateWeekdays.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, rangeHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, timeDisplay.getEffectiveNodeOrientation());
            assertEquals(Pos.CENTER_RIGHT, dateHeader.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, dateWeekdays.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, rangeHeader.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, timeDisplay.getAlignment());
            assertEquals(">", pickerHeaderNavigationIconText(dateHeader, 2));
            assertEquals("<", pickerHeaderNavigationIconText(dateHeader, 3));
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, dateField.getPicker().getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, rangeField.getPicker().getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, timeField.getPicker().getEffectiveNodeOrientation());
            assertPickerFieldPresetContentUsesLogicalStart(
                    assertInstanceOf(Node.class, dateField.getPicker().getParent()),
                    M3DatePickerField.PRESET_LIST_STYLE_CLASS
            );
            assertPickerFieldPresetContentUsesLogicalStart(
                    assertInstanceOf(Node.class, rangeField.getPicker().getParent()),
                    M3DateRangePickerField.PRESET_LIST_STYLE_CLASS
            );
            assertPickerFieldPresetContentUsesLogicalStart(
                    assertInstanceOf(Node.class, timeField.getPicker().getParent()),
                    M3TimePickerField.PRESET_LIST_STYLE_CLASS
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, dateRangePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, timePicker, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-pickers-rtl.png"
            ));
        });
    }

    /// Verifies that sheets, form sections, and validation summaries mirror logical slots in right-to-left layout.
    @Test
    void sheetFormAndValidationSkinsPropagateRightToLeftOrientation() {
        runOnFxThread(() -> {
            Label sideContent = new Label("Side content");
            M3Button sideAction = createButton("Close", M3ButtonVariant.TEXT);
            M3SideSheet sideSheet = new M3SideSheet("Details", sideContent, sideAction);
            sideSheet.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            sideSheet.setPrefSize(300.0, 160.0);

            Label bottomContent = new Label("Bottom content");
            M3Button bottomAction = createButton("Done", M3ButtonVariant.TEXT);
            M3BottomSheet bottomSheet = new M3BottomSheet("Queue", bottomContent, bottomAction);
            bottomSheet.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            bottomSheet.setPrefSize(360.0, 160.0);

            Label sectionContent = new Label("Section content");
            M3FormSection formSection = new M3FormSection("Account", sectionContent);
            formSection.setSupportingText("Preferences and profile settings");
            formSection.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            formSection.setPrefWidth(360.0);

            M3TextField invalidField = new M3TextField();
            M3TextInputLayout invalidLayout = new M3TextInputLayout(invalidField, "Display name", "Required");
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
                    sideSheet.lookup("." + M3SideSheet.HEADER_STYLE_CLASS)
            );
            StackPane sideContentSlot = assertInstanceOf(
                    StackPane.class,
                    sideSheet.lookup("." + M3SideSheet.CONTENT_STYLE_CLASS)
            );
            HBox bottomHeader = assertInstanceOf(
                    HBox.class,
                    bottomSheet.lookup("." + M3BottomSheet.HEADER_STYLE_CLASS)
            );
            StackPane bottomContentSlot = assertInstanceOf(
                    StackPane.class,
                    bottomSheet.lookup("." + M3BottomSheet.CONTENT_STYLE_CLASS)
            );
            VBox formHeader = assertInstanceOf(
                    VBox.class,
                    formSection.lookup("." + M3FormSection.HEADER_STYLE_CLASS)
            );
            Label sectionTitle = assertInstanceOf(
                    Label.class,
                    formSection.lookup("." + M3FormSection.TITLE_STYLE_CLASS)
            );
            VBox summaryItems = assertInstanceOf(
                    VBox.class,
                    summary.lookup("." + M3ValidationSummary.ITEMS_STYLE_CLASS)
            );
            Label summaryTitle = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + M3ValidationSummary.TITLE_STYLE_CLASS)
            );
            Label summaryItemLabel = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + M3ValidationSummary.ITEM_LABEL_STYLE_CLASS)
            );

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, sideHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, bottomHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, formHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, summaryItems.getEffectiveNodeOrientation());
            assertEquals(Pos.CENTER_RIGHT, sideHeader.getAlignment());
            assertEquals(Pos.TOP_LEFT, sideContentSlot.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, bottomHeader.getAlignment());
            assertEquals(Pos.TOP_LEFT, bottomContentSlot.getAlignment());
            assertEquals(Pos.TOP_LEFT, formHeader.getAlignment());
            assertEquals(Pos.CENTER_LEFT, summaryItems.getAlignment());

            Bounds sideContentBounds = sideContent.localToScene(sideContent.getBoundsInLocal());
            Bounds sideActionBounds = sideAction.localToScene(sideAction.getBoundsInLocal());
            assertTrue(sideContentBounds.getMinX() > sideActionBounds.getMaxX(),
                    () -> "sideContentBounds=" + sideContentBounds + ", sideActionBounds=" + sideActionBounds);
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

        assertEquals(
                navigationDrawer.localToScene(12.0, 0.0).getX(),
                home.localToScene(0.0, 0.0).getX(),
                0.0001
        );
        assertEquals(296.0, home.getWidth(), 0.0001);
        assertEquals(296.0, listItemContainer(home).getWidth(), 0.0001);
        assertEquals(296.0, listItemSelectionContainer(home).getWidth(), 0.0001);

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

    /// Verifies that navigation selection indicators animate both outgoing and incoming selected states.
    @Test
    void navigationIndicatorAnimationsRenderIntermediateAndFinalStates() {
        runOnFxThread(() -> {
            M3Tab overview = new M3Tab("Overview");
            M3Tab details = new M3Tab("Details");
            M3TabBar tabBar = new M3TabBar(overview, details);

            M3NavigationItem home = new M3NavigationItem("Home", new M3Icon("H"));
            M3NavigationItem search = new M3NavigationItem("Search", new M3Icon("S"));
            M3NavigationBar navigationBar = new M3NavigationBar(home, search);

            M3NavigationItem railHome = new M3NavigationItem("Home", new M3Icon("H"));
            M3NavigationItem railSearch = new M3NavigationItem("Search", new M3Icon("S"));
            M3NavigationRail navigationRail = new M3NavigationRail(railHome, railSearch);

            VBox root = new VBox(18.0, tabBar, navigationBar, navigationRail);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 360.0, 320.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(360.0, 320.0);
            root.layout();

            tabBar.select(details);
            navigationBar.select(search);
            navigationRail.select(railSearch);

            Timeline outgoingTabAnimation = skinTimeline(overview.getSkin(), "indicatorAnimation");
            Timeline incomingTabAnimation = skinTimeline(details.getSkin(), "indicatorAnimation");
            Timeline outgoingBarAnimation = skinTimeline(home.getSkin(), "indicatorAnimation");
            Timeline incomingBarAnimation = skinTimeline(search.getSkin(), "indicatorAnimation");
            Timeline outgoingRailAnimation = skinTimeline(railHome.getSkin(), "indicatorAnimation");
            Timeline incomingRailAnimation = skinTimeline(railSearch.getSkin(), "indicatorAnimation");
            jumpToNavigationIndicatorFrame(
                    Duration.millis(80.0),
                    outgoingTabAnimation,
                    incomingTabAnimation,
                    outgoingBarAnimation,
                    incomingBarAnimation,
                    outgoingRailAnimation,
                    incomingRailAnimation
            );
            root.layout();

            Region outgoingTabIndicator = lookupRegion(overview, ".m3-tab-active-indicator");
            Region incomingTabIndicator = lookupRegion(details, ".m3-tab-active-indicator");
            Region outgoingBarIndicator = lookupRegion(home, ".m3-navigation-item-indicator");
            Region incomingBarIndicator = lookupRegion(search, ".m3-navigation-item-indicator");
            Region outgoingRailIndicator = lookupRegion(railHome, ".m3-navigation-item-indicator");
            Region incomingRailIndicator = lookupRegion(railSearch, ".m3-navigation-item-indicator");

            assertBetween(outgoingTabIndicator.getOpacity(), 0.0, 1.0, "outgoing tab indicator opacity");
            assertBetween(incomingTabIndicator.getOpacity(), 0.0, 1.0, "incoming tab indicator opacity");
            assertBetween(outgoingBarIndicator.getOpacity(), 0.0, 1.0, "outgoing navigation bar indicator opacity");
            assertBetween(incomingBarIndicator.getOpacity(), 0.0, 1.0, "incoming navigation bar indicator opacity");
            assertBetween(outgoingRailIndicator.getOpacity(), 0.0, 1.0, "outgoing navigation rail indicator opacity");
            assertBetween(incomingRailIndicator.getOpacity(), 0.0, 1.0, "incoming navigation rail indicator opacity");
            assertBetween(outgoingTabIndicator.getScaleX(), 0.72, 1.0, "outgoing tab indicator scale");
            assertBetween(incomingTabIndicator.getScaleX(), 0.72, 1.0, "incoming tab indicator scale");
            assertBetween(outgoingBarIndicator.getScaleX(), 0.72, 1.0, "outgoing navigation bar indicator scale");
            assertBetween(incomingBarIndicator.getScaleX(), 0.72, 1.0, "incoming navigation bar indicator scale");

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, incomingTabIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, incomingBarIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, incomingRailIndicator, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-indicator-animation-frame.png"
            ));

            jumpToNavigationIndicatorFrame(
                    Duration.millis(200.0),
                    outgoingTabAnimation,
                    incomingTabAnimation,
                    outgoingBarAnimation,
                    incomingBarAnimation,
                    outgoingRailAnimation,
                    incomingRailAnimation
            );
            root.layout();

            assertEquals(0.0, outgoingTabIndicator.getOpacity(), 0.0001);
            assertEquals(1.0, incomingTabIndicator.getOpacity(), 0.0001);
            assertEquals(0.0, outgoingBarIndicator.getOpacity(), 0.0001);
            assertEquals(1.0, incomingBarIndicator.getOpacity(), 0.0001);
            assertEquals(0.0, outgoingRailIndicator.getOpacity(), 0.0001);
            assertEquals(1.0, incomingRailIndicator.getOpacity(), 0.0001);
            stopTimelines(
                    outgoingTabAnimation,
                    incomingTabAnimation,
                    outgoingBarAnimation,
                    incomingBarAnimation,
                    outgoingRailAnimation,
                    incomingRailAnimation
            );
        });
    }

    /// Verifies that generated state layer rules apply beyond button-like controls.
    @Test
    void generatedStateLayerRulesApplyToInteractiveControls() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        M3Tab tab = new M3Tab("Overview");
        M3NavigationItem navigationItem = new M3NavigationItem("Home");
        M3IconToggleButton iconToggleButton = new M3IconToggleButton("T");
        M3ListItem listItem = new M3ListItem("Headline");
        M3Card card = new M3Card();
        M3Card disabledCard = new M3Card();
        Pane root = new Pane(
                checkBox,
                slider,
                tab,
                navigationItem,
                iconToggleButton,
                listItem,
                card,
                disabledCard
        );
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        M3MotionSettings.setAnimationsEnabled(root, false);
        checkBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        slider.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        navigationItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        iconToggleButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
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
        assertEquals(1.0, iconToggleButton.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(iconToggleButton, ".m3-state-layer").getOpacity(), 0.0001);
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
        M3Tab tab = new M3Tab("Tab");
        M3NavigationItem navigationItem = new M3NavigationItem("Home");
        M3IconToggleButton iconToggleButton = new M3IconToggleButton("T");
        Pane root = new Pane(button, tab, navigationItem, iconToggleButton);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        M3MotionSettings.setAnimationsEnabled(root, false);
        M3ThemeManager.uninstallThemeStylesheet(scene);
        button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        navigationItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        iconToggleButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();

        assertEquals(0.08, lookupRegion(button, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.10, lookupRegion(tab, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.10, lookupRegion(navigationItem, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.08, lookupRegion(iconToggleButton, ".m3-state-layer").getOpacity(), 0.0001);
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

    /// Verifies that representative interactive states render visible state layer feedback.
    @Test
    void interactiveStateSnapshotRendersHoverFocusAndPressedFeedback() {
        runOnFxThread(() -> {
            M3Button normalButton = createButton("Normal", M3ButtonVariant.FILLED);
            M3Button hoverButton = createButton("Hover", M3ButtonVariant.FILLED);
            M3Button focusButton = createButton("Focus", M3ButtonVariant.FILLED);
            M3Button pressedButton = createButton("Pressed", M3ButtonVariant.FILLED);
            applyPseudoState(hoverButton, "hover");
            applyPseudoState(focusButton, "focus-visible");
            applyPseudoState(pressedButton, "pressed");

            M3CheckBox hoverCheckBox = createCheckBox("Checkbox hover", true);
            M3RadioButton focusRadioButton = createRadioButton("Radio focus", true);
            M3Switch pressedSwitch = createSwitch("Switch pressed", true);
            applyPseudoState(hoverCheckBox, "hover");
            applyPseudoState(focusRadioButton, "focus-visible");
            applyPseudoState(pressedSwitch, "pressed");

            M3Slider pressedSlider = new M3Slider(0.0, 100.0, 56.0);
            pressedSlider.setPrefWidth(180.0);
            M3Tab focusTab = createTab("Tab focus", true);
            M3NavigationItem hoverNavigationItem = createNavigationItem(
                    "Home",
                    new M3Icon("H"),
                    true
            );
            applyPseudoState(pressedSlider, "pressed");
            applyPseudoState(focusTab, "focus-visible");
            applyPseudoState(hoverNavigationItem, "hover");

            M3ListItem pressedListItem = new M3ListItem("Pressed list item");
            pressedListItem.setLeadingIcon("L");
            pressedListItem.setPrefWidth(220.0);
            M3Card hoverCard = new M3Card(
                    visualLabel("Hover card"),
                    M3CardVariant.ELEVATED,
                    event -> {
                    }
            );
            hoverCard.setPrefSize(160.0, 72.0);
            applyPseudoState(pressedListItem, "pressed");
            applyPseudoState(hoverCard, "hover");

            VBox root = new VBox(
                    18.0,
                    interactiveStateSection(
                            "Buttons",
                            interactiveStateSample("Normal", normalButton),
                            interactiveStateSample("Hover", hoverButton),
                            interactiveStateSample("Focus Visible", focusButton),
                            interactiveStateSample("Pressed", pressedButton)
                    ),
                    interactiveStateSection(
                            "Selection",
                            interactiveStateSample("Hover", hoverCheckBox),
                            interactiveStateSample("Focus Visible", focusRadioButton),
                            interactiveStateSample("Pressed", pressedSwitch)
                    ),
                    interactiveStateSection(
                            "Navigation",
                            interactiveStateSample("Pressed", pressedSlider),
                            interactiveStateSample("Focus Visible", focusTab),
                            interactiveStateSample("Hover", hoverNavigationItem)
                    ),
                    interactiveStateSection(
                            "Containers",
                            interactiveStateSample("Pressed", pressedListItem),
                            interactiveStateSample("Hover", hoverCard)
                    )
            );
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 920.0, 560.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(920.0, Math.ceil(root.prefHeight(920.0)));
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 18);
            assertStateLayerOpacity(normalButton, 0.0);
            assertStateLayerOpacity(hoverButton, 0.08);
            assertStateLayerOpacity(focusButton, 0.1);
            assertStateLayerOpacity(pressedButton, 0.1);
            assertStateLayerOpacity(hoverCheckBox, 0.08);
            assertStateLayerOpacity(focusRadioButton, 0.1);
            assertStateLayerOpacity(pressedSwitch, 0.1);
            assertStateLayerOpacity(pressedSlider, 0.1);
            assertStateLayerOpacity(focusTab, 0.1);
            assertStateLayerOpacity(hoverNavigationItem, 0.08);
            assertStateLayerOpacity(pressedListItem, 0.1);
            assertStateLayerOpacity(hoverCard, 0.08);
            assertSnapshotNodeContainsContrast(image, hoverButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, focusButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, pressedButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, hoverCheckBox, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, focusRadioButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, pressedSwitch, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, pressedSlider, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, focusTab, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, hoverNavigationItem, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, pressedListItem, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, hoverCard, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-interactive-states.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that a real button ripple remains visible after release and fades out through animation.
    @Test
    void buttonRippleReleaseAnimationFadesAfterPointerRelease() throws InterruptedException {
        M3Button button = createButton("Ripple", M3ButtonVariant.FILLED);
        Pane root = new Pane(button);

        runOnFxThreadAfterDelay(
                Duration.millis(520.0),
                () -> {
                    Scene scene = new Scene(root, 200.0, 100.0);
                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    button.resize(120.0, 40.0);
                    button.layout();

                    button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 24.0, 20.0, true));
                    Region ripple = lookupRegion(button, ".m3-ripple");
                    assertTrue(ripple.getOpacity() > 0.0);

                    button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 24.0, 20.0, false));
                    assertTrue(ripple.getOpacity() > 0.0);
                    assertFalse(button.isArmed());
                },
                () -> assertEquals(0.0, lookupRegion(button, ".m3-ripple").getOpacity(), 0.0001)
        );
    }

    /// Verifies that a representative control set renders non-blank visible output.
    @Test
    void visualSmokeSnapshotRendersCoreControlsWithContrast() {
        runOnFxThread(() -> {
            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3Button tonalButton = createButton("Tonal", M3ButtonVariant.TONAL);
            M3Button outlinedButton = createButton("Outlined", M3ButtonVariant.OUTLINED);
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = createSegmentedButton("Week", true);
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segments = new M3SegmentedButtonGroup(day, week, month);
            M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
            slider.setPrefWidth(220.0);
            M3ProgressBar progressBar = new M3ProgressBar(0.62);
            progressBar.setPrefWidth(220.0);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.72);
            M3CheckBox checkBox = createCheckBox("Check", true);
            M3RadioButton radioButton = createRadioButton("Radio", true);
            M3Switch switchControl = createSwitch("Switch", true);

            FlowPane buttons = new FlowPane(14.0, 14.0, filledButton, tonalButton, outlinedButton, segments);
            FlowPane controls = new FlowPane(18.0, 18.0, slider, progressBar, progressIndicator);
            FlowPane selections = new FlowPane(18.0, 18.0, checkBox, radioButton, switchControl);
            VBox root = new VBox(18.0, buttons, controls, selections);
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 620.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(620.0, 260.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 10);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-smoke.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that slider snapshots show distinct rendered track and thumb pixels.
    @Test
    void sliderSnapshotRendersTrackAndThumbPixels() {
        runOnFxThread(() -> {
            M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
            slider.setPrefSize(220.0, 56.0);
            FlowPane root = new FlowPane(slider);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 260.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(260.0, 80.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            var sliderBounds = slider.getBoundsInParent();
            int sliderX = (int) Math.round(sliderBounds.getMinX());
            int sliderY = (int) Math.round(sliderBounds.getMinY());
            Color track = image.getPixelReader().getColor(sliderX + 24, sliderY + 28);
            Color thumb = image.getPixelReader().getColor(sliderX + 110, sliderY + 28);
            Color emptyTouchTarget = image.getPixelReader().getColor(sliderX + 110, sliderY + 4);

            assertTrue(colorDistance(track, thumb) > 0.1, () -> "track=" + track + ", thumb=" + thumb);
            assertTrue(emptyTouchTarget.getOpacity() < 0.1
                    || colorDistance(emptyTouchTarget, thumb) > 0.1);
        });
    }

    /// Verifies that selected segmented button backgrounds keep rounded end caps in rendered output.
    @Test
    void segmentedButtonSnapshotKeepsSelectedEndRounded() {
        runOnFxThread(() -> {
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = createSegmentedButton("Month", true);
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(day, week, month);
            group.setPrefSize(240.0, 40.0);
            FlowPane root = new FlowPane(group);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 280.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(280.0, 80.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            var monthBounds = month.localToScene(month.getBoundsInLocal());
            int monthRight = (int) Math.round(monthBounds.getMaxX());
            int monthTop = (int) Math.round(monthBounds.getMinY());
            int monthCenterY = (int) Math.round((monthBounds.getMinY() + monthBounds.getMaxY()) / 2.0);
            Color selectedBody = image.getPixelReader().getColor(monthRight - 14, monthCenterY);
            Color roundedCorner = image.getPixelReader().getColor(monthRight - 2, monthTop + 2);

            assertTrue(selectedBody.getOpacity() > 0.4, () -> "selectedBody=" + selectedBody);
            assertSnapshotNodeBorderContainsContrast(image, month, selectedBody, 0.08);
            assertTrue(roundedCorner.getOpacity() < 0.4
                            || colorDistance(selectedBody, roundedCorner) > 0.1,
                    () -> "selectedBody=" + selectedBody + ", roundedCorner=" + roundedCorner);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-segmented-selected-border.png"
            ));
        });
    }

    /// Verifies that determinate progress snapshots show separated fill, track, and rounded caps.
    @Test
    void progressBarSnapshotRendersFillTrackAndRoundedCaps() {
        runOnFxThread(() -> {
            M3ProgressBar progressBar = new M3ProgressBar(0.5);
            progressBar.setPrefSize(200.0, 32.0);
            progressBar.setStyle("-m3-track-thickness: 8px; -m3-track-shape: 999px;");
            FlowPane root = new FlowPane(progressBar);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 240.0, 48.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(240.0, 48.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            Shape trackShape = lookupShape(progressBar, ".track");
            Shape barShape = lookupShape(progressBar, ".bar");
            var trackBounds = trackShape.localToScene(trackShape.getBoundsInLocal());
            var barBounds = barShape.localToScene(barShape.getBoundsInLocal());
            int trackCenterY = (int) Math.round((trackBounds.getMinY() + trackBounds.getMaxY()) / 2.0);
            int fillX = (int) Math.round((barBounds.getMinX() + barBounds.getMaxX()) / 2.0);
            int trackX = (int) Math.round(trackBounds.getMaxX() - 12.0);
            Color fill = image.getPixelReader().getColor(fillX, trackCenterY);
            Color track = image.getPixelReader().getColor(trackX, trackCenterY);
            Color roundedCorner = image.getPixelReader().getColor(
                    (int) Math.round(barBounds.getMinX()),
                    (int) Math.round(barBounds.getMinY())
            );

            assertTrue(fill.getOpacity() > 0.8, () -> "fill=" + fill);
            assertTrue(track.getOpacity() > 0.8, () -> "track=" + track);
            assertTrue(colorDistance(fill, track) > 0.1, () -> "fill=" + fill + ", track=" + track);
            assertTrue(roundedCorner.getOpacity() < 0.4
                            || colorDistance(fill, roundedCorner) > 0.1,
                    () -> "fill=" + fill + ", roundedCorner=" + roundedCorner);
        });
    }

    /// Verifies that circular progress snapshots show distinct track and indicator arc pixels.
    @Test
    void progressIndicatorSnapshotRendersTrackAndArcPixels() {
        runOnFxThread(() -> {
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
            progressIndicator.setPrefSize(72.0, 72.0);
            progressIndicator.setStyle("-m3-track-thickness: 8px; -m3-indicator-size: 72px;");
            FlowPane root = new FlowPane(progressIndicator);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 104.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(104.0, 96.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            Arc arc = (Arc) lookupShape(progressIndicator, ".indicator");
            javafx.scene.shape.Circle track = (javafx.scene.shape.Circle) lookupShape(progressIndicator, ".track");
            var arcCenter = arc.localToScene(arc.getCenterX(), arc.getCenterY());
            int centerX = (int) Math.round(arcCenter.getX());
            int centerY = (int) Math.round(arcCenter.getY());
            int radius = (int) Math.round(track.getRadius());
            Color arcPixel = image.getPixelReader().getColor(centerX, centerY - radius);
            Color trackPixel = image.getPixelReader().getColor(centerX - radius, centerY);

            assertTrue(arcPixel.getOpacity() > 0.4, () -> "arcPixel=" + arcPixel);
            assertTrue(trackPixel.getOpacity() > 0.4, () -> "trackPixel=" + trackPixel);
            assertTrue(colorDistance(arcPixel, trackPixel) > 0.1,
                    () -> "arcPixel=" + arcPixel + ", trackPixel=" + trackPixel);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-progress-indicator-arc.png"
            ));
        });
    }

    /// Verifies that expressive progress snapshots render wavy linear and circular indicators.
    @Test
    void expressiveProgressSnapshotRendersWavyIndicators() {
        runOnFxThread(() -> {
            M3ProgressBar determinateBar = new M3ProgressBar(0.55);
            determinateBar.setPrefWidth(260.0);
            M3ProgressBar indeterminateBar = new M3ProgressBar();
            indeterminateBar.setPrefWidth(260.0);
            M3ProgressIndicator determinateIndicator = new M3ProgressIndicator(0.62);
            M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
            FlowPane root = new FlowPane(24.0, 24.0, determinateBar, indeterminateBar,
                    determinateIndicator, indeterminateIndicator);
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 640.0, 160.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            root.resize(640.0, 160.0);
            root.layout();

            assertTrue(lookupShape(determinateBar, ".m3-progress-bar-wave").isVisible());
            assertTrue(lookupShape(indeterminateBar, ".m3-progress-bar-wave").isVisible());
            assertTrue(lookupShape(determinateIndicator, ".m3-progress-indicator-wave").isVisible());
            assertTrue(lookupShape(indeterminateIndicator, ".m3-progress-indicator-wave").isVisible());

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 6);
            assertSnapshotNodeContainsContrast(image, determinateBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, determinateIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateIndicator, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-expressive-progress-indicators.png"
            ));
        });
    }

    /// Verifies that inputs render filled, outlined, password, and multiline visual variants.
    @Test
    void inputSnapshotRendersFilledOutlinedPasswordAndTextAreaControls() {
        runOnFxThread(() -> {
            M3TextField filledField = new M3TextField("Filled text");
            filledField.setPrefWidth(180.0);
            M3TextField outlinedField = createTextField("Outlined text", M3TextInputVariant.OUTLINED);
            outlinedField.setPrefWidth(190.0);
            M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
            passwordField.setPrefWidth(160.0);
            M3TextArea textArea = createTextArea("Multiline\ncontent", M3TextInputVariant.FILLED);
            textArea.setPrefSize(240.0, 96.0);

            FlowPane row = new FlowPane(16.0, 16.0, filledField, outlinedField, passwordField, textArea);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 840.0, 180.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(840.0, 180.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, filledField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, passwordField, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, textArea, Color.WHITE, 0.04);
            Node textAreaContent = assertInstanceOf(Node.class, textArea.lookup(".content"));
            var textAreaContentBounds = textAreaContent.localToScene(textAreaContent.getBoundsInLocal());
            Color textAreaContentBackground = image.getPixelReader().getColor(
                    (int) Math.round(textAreaContentBounds.getMaxX() - 8.0),
                    (int) Math.round(textAreaContentBounds.getMaxY() - 8.0)
            );
            assertTrue(colorDistance(textAreaContentBackground, Color.WHITE) > 0.02,
                    () -> "textAreaContentBackground=" + textAreaContentBackground);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-inputs.png"
            ));
            assertRenderedTextNodesStayInsideLayout(row);
            assertOutlinedTextInputsKeepTextCentered(row);
            assertFixedTargetControlsKeepCenteredContent(row);
        });
    }

    /// Verifies that text input layouts render supporting text, validation, errors, and character counters.
    @Test
    void inputLayoutSnapshotRendersSupportingErrorAndCounterText() {
        runOnFxThread(() -> {
            M3TextField supportingField = new M3TextField("Alpha");
            supportingField.setPrefWidth(220.0);
            M3TextInputLayout supportingLayout = new M3TextInputLayout(supportingField, "Supporting text");
            supportingLayout.setLabelText("Project");
            supportingLayout.setLeading(new M3Icon("A"));
            supportingLayout.setPrefWidth(220.0);

            M3TextField counterField = createTextField("support@example.com", M3TextInputVariant.OUTLINED);
            counterField.setPrefWidth(320.0);
            M3TextInputLayout counterLayout = new M3TextInputLayout(counterField, "Email address");
            counterLayout.setLabelText("Email");
            counterLayout.setLeading(new M3Icon("E"));
            counterLayout.setClearButtonEnabled(true);
            counterLayout.setCharacterCounterVisible(true);
            counterLayout.setCharacterLimit(32);
            counterLayout.setPrefWidth(320.0);

            M3TextField errorField = createTextField("abcdef", M3TextInputVariant.OUTLINED);
            errorField.setPrefWidth(220.0);
            M3TextInputLayout errorLayout = new M3TextInputLayout(errorField, "Helper text");
            errorLayout.setLabelText("Code");
            errorLayout.setLeading(new M3Icon("!"));
            errorLayout.setCharacterCounterVisible(true);
            errorLayout.setCharacterLimit(4);
            errorLayout.setErrorText("Too long");
            errorLayout.setPrefWidth(220.0);

            M3TextField enforcedField = createTextField("Too many characters", M3TextInputVariant.OUTLINED);
            enforcedField.setPrefWidth(260.0);
            M3TextInputLayout enforcedLayout = new M3TextInputLayout(enforcedField, "Limit enforced");
            enforcedLayout.setLabelText("Limited");
            enforcedLayout.setCharacterCounterVisible(true);
            enforcedLayout.setCharacterLimit(8);
            enforcedLayout.setCharacterLimitEnforced(true);
            enforcedLayout.setPrefWidth(260.0);

            M3TextField validatedField = createTextField("support", M3TextInputVariant.OUTLINED);
            validatedField.setPrefWidth(260.0);
            M3TextInputLayout validatedLayout = new M3TextInputLayout(validatedField, "Email format");
            validatedLayout.setLabelText("Validated");
            validatedLayout.setValidator((input, text) -> text.contains("@") ? null : "Use an email address");
            validatedLayout.validate();
            validatedLayout.setPrefWidth(260.0);

            FlowPane row = new FlowPane(
                    18.0,
                    18.0,
                    supportingLayout,
                    counterLayout,
                    errorLayout,
                    enforcedLayout,
                    validatedLayout
            );
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 1220.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(1220.0, 220.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, supportingLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, counterLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, errorLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, enforcedLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, validatedLayout, Color.WHITE, 0.04);
            assertTrue(errorField.isError());
            assertFalse(enforcedField.isError());
            assertTrue(validatedField.isError());
            assertEquals("Too many", enforcedField.getText());
            assertTrue(counterLayout.isLabelFloating());
            assertEquals(counterLayout.getClearButton(), counterLayout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
            Label floatingCounterLabel = assertInstanceOf(
                    Label.class,
                    counterLayout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS)
            );
            var counterFieldBounds = counterField.localToScene(counterField.getBoundsInLocal());
            var floatingCounterLabelBounds = floatingCounterLabel.localToScene(floatingCounterLabel.getBoundsInLocal());
            assertTrue(
                    floatingCounterLabelBounds.getMinX() > counterFieldBounds.getMinX() + 32.0,
                    () -> "floating label background starts too far left: field="
                            + counterFieldBounds + ", label=" + floatingCounterLabelBounds
            );
            assertSnapshotAreaContainsContrast(
                    image,
                    (int) Math.floor(counterFieldBounds.getMinX() + 4.0),
                    (int) Math.floor(counterFieldBounds.getMinY()),
                    (int) Math.floor(floatingCounterLabelBounds.getMinX() - 2.0),
                    (int) Math.ceil(counterFieldBounds.getMinY() + 3.0),
                    Color.WHITE,
                    0.04,
                    "outlined text field leading top outline before floating label notch"
            );
            assertEquals("Too long", assertInstanceOf(
                    Label.class,
                    errorLayout.lookup("." + M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS)
            ).getText());
            assertEquals("Use an email address", assertInstanceOf(
                    Label.class,
                    validatedLayout.lookup("." + M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS)
            ).getText());
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-input-layouts.png"
            ));
            assertRenderedTextNodesStayInsideLayout(row);
            assertOutlinedTextInputsKeepTextCentered(row);
            assertFixedTargetControlsKeepCenteredContent(row);
        });
    }

    /// Verifies that outlined text input layouts cut a real outline notch instead of painting a label mask.
    @Test
    void outlinedInputLayoutSnapshotRendersNotchedOutlineWithoutLabelMask() {
        runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField, "Project name");
            layout.setLabelText("Outlined with text");
            layout.setLeading(new M3Icon("T"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setPrefWidth(360.0);

            Color surface = Color.rgb(248, 240, 249);
            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(430.0, 140.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            Label label = assertInstanceOf(Label.class, layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS));
            Path outline = assertInstanceOf(Path.class, layout.lookup("." + M3TextInputLayout.OUTLINE_STYLE_CLASS));
            Text inputText = renderedTextNode(textField, "M3FX");
            var labelBounds = label.localToScene(label.getBoundsInLocal());
            var fieldBounds = textField.localToScene(textField.getBoundsInLocal());
            var inputTextBounds = inputText.localToScene(inputText.getBoundsInLocal());
            double fieldCenterY = (fieldBounds.getMinY() + fieldBounds.getMaxY()) / 2.0;
            double inputTextCenterY = (inputTextBounds.getMinY() + inputTextBounds.getMaxY()) / 2.0;

            assertEquals(8.0, textField.getPadding().getTop(), 0.0001);
            assertEquals(fieldCenterY, inputTextCenterY, 2.0,
                    () -> "outlined input text should stay vertically centered: field="
                            + fieldBounds + ", text=" + inputTextBounds);
            assertTrue(label.getBackground() == null || label.getBackground().getFills().isEmpty());
            assertTrue(outlineNotchGap(outline) >= labelBounds.getWidth() - 1.0,
                    () -> "outline gap is narrower than the floating label: gap="
                            + outlineNotchGap(outline) + ", label=" + labelBounds);
            assertTrue(labelBounds.getMinY() < fieldBounds.getMinY(),
                    () -> "floating label should start above the outline top: field="
                            + fieldBounds + ", label=" + labelBounds);
            assertTrue(labelBounds.getMaxY() > fieldBounds.getMinY(),
                    () -> "floating label should straddle the outline top: field="
                            + fieldBounds + ", label=" + labelBounds);
            assertTrue(Math.abs(labelBounds.getCenterY() - fieldBounds.getMinY()) < 6.0,
                    () -> "floating label should be centered around the outline top: field="
                            + fieldBounds + ", label=" + labelBounds);

            Color labelPaddingPixel = image.getPixelReader().getColor(
                    (int) Math.floor(labelBounds.getMinX() + 1.0),
                    (int) Math.floor(labelBounds.getMinY() + labelBounds.getHeight() / 2.0)
            );
            Color notchPixel = image.getPixelReader().getColor(
                    (int) Math.floor(labelBounds.getMinX() + 1.0),
                    (int) Math.floor(fieldBounds.getMinY() + 1.0)
            );
            assertColorNear(labelPaddingPixel, surface, 0.015, "floating label padding pixel");
            assertColorNear(notchPixel, surface, 0.015, "outlined notch pixel");
            assertSnapshotAreaContainsContrast(
                    image,
                    (int) Math.floor(fieldBounds.getMinX() + 4.0),
                    (int) Math.floor(fieldBounds.getMinY()),
                    (int) Math.floor(labelBounds.getMinX() - 2.0),
                    (int) Math.ceil(fieldBounds.getMinY() + 3.0),
                    surface,
                    0.04,
                    "outlined text field top outline before floating label notch"
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-outlined-input-notch.png"
            ));
            assertOutlinedTextInputsKeepTextCentered(root);
        });
    }

    /// Verifies that validation summaries render invalid input rows in snapshots.
    @Test
    void validationSummarySnapshotRendersInvalidInputRows() {
        runOnFxThread(() -> {
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Display name", "Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Format");
            emailLayout.setValidator(M3TextInputValidators.pattern(
                    Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                    "Enter a valid email address"
            ));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setPrefWidth(420.0);
            assertFalse(validator.validate());

            VBox root = new VBox(summary);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 500.0, 190.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(500.0, 190.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, summary, Color.WHITE, 0.04);
            assertEquals(2, summary.lookupAll("." + M3ValidationSummary.ITEM_STYLE_CLASS).size());
            assertEquals("Display name", assertInstanceOf(
                    Label.class,
                    summary.lookup("." + M3ValidationSummary.ITEM_LABEL_STYLE_CLASS)
            ).getText());
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-validation-summary.png"
            ));
        });
    }

    /// Verifies that selection controls render selected, indeterminate, and disabled states.
    @Test
    void selectionSnapshotRendersStateMatrix() {
        runOnFxThread(() -> {
            M3CheckBox uncheckedCheckBox = new M3CheckBox("Unchecked");
            M3CheckBox checkedCheckBox = createCheckBox("Checked", true);
            M3CheckBox indeterminateCheckBox = new M3CheckBox("Indeterminate");
            indeterminateCheckBox.setIndeterminate(true);
            M3CheckBox disabledUncheckedCheckBox = new M3CheckBox("Disabled unchecked");
            M3CheckBox disabledCheckedCheckBox = createCheckBox("Disabled checked", true);
            M3CheckBox disabledIndeterminateCheckBox = new M3CheckBox("Disabled indeterminate");
            disabledIndeterminateCheckBox.setIndeterminate(true);
            disabledUncheckedCheckBox.setDisable(true);
            disabledCheckedCheckBox.setDisable(true);
            disabledIndeterminateCheckBox.setDisable(true);

            M3RadioButton uncheckedRadioButton = new M3RadioButton("Radio off");
            M3RadioButton selectedRadioButton = createRadioButton("Radio on", true);
            M3RadioButton disabledUncheckedRadioButton = new M3RadioButton("Disabled off");
            M3RadioButton disabledSelectedRadioButton = createRadioButton("Disabled on", true);
            disabledUncheckedRadioButton.setDisable(true);
            disabledSelectedRadioButton.setDisable(true);

            M3Switch offSwitch = new M3Switch("Switch off");
            M3Switch onSwitch = createSwitch("Switch on", true);
            M3Switch disabledOffSwitch = new M3Switch("Disabled off");
            M3Switch disabledOnSwitch = createSwitch("Disabled on", true);
            disabledOffSwitch.setDisable(true);
            disabledOnSwitch.setDisable(true);

            FlowPane row = new FlowPane(
                    20.0,
                    16.0,
                    uncheckedCheckBox,
                    checkedCheckBox,
                    indeterminateCheckBox,
                    disabledUncheckedCheckBox,
                    disabledCheckedCheckBox,
                    disabledIndeterminateCheckBox,
                    uncheckedRadioButton,
                    selectedRadioButton,
                    disabledUncheckedRadioButton,
                    disabledSelectedRadioButton,
                    offSwitch,
                    onSwitch,
                    disabledOffSwitch,
                    disabledOnSwitch
            );
            row.setPrefWrapLength(720.0);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 760.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(760.0, 220.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            Region checkedBox = lookupRegion(checkedCheckBox, ".box");
            Region indeterminateBox = lookupRegion(indeterminateCheckBox, ".box");
            Region indeterminateMark = lookupRegion(indeterminateCheckBox, ".mark");
            assertSnapshotNodeBorderContainsContrast(image, lookupRegion(uncheckedCheckBox, ".box"), Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, checkedBox, Color.WHITE, 0.1);
            assertSnapshotNodeContainsContrast(image, lookupRegion(checkedCheckBox, ".mark"), Color.rgb(84, 50, 185), 0.1);
            assertSnapshotNodeContainsContrast(image, indeterminateBox, Color.WHITE, 0.1);
            assertSnapshotNodeContainsContrast(
                    image,
                    indeterminateMark,
                    Color.rgb(84, 50, 185),
                    0.1
            );
            assertEquals(12.0, indeterminateMark.getLayoutBounds().getWidth(), 0.0001);
            assertEquals(2.0, indeterminateMark.getLayoutBounds().getHeight(), 0.0001);
            Color checkedBoxFill = snapshotNodePixel(image, checkedBox, checkedBox.getWidth() / 2.0, 3.0);
            Color indeterminateBoxTop = snapshotNodePixel(
                    image,
                    indeterminateBox,
                    indeterminateBox.getWidth() / 2.0,
                    5.0
            );
            Color indeterminateDash = snapshotNodePixel(
                    image,
                    indeterminateBox,
                    indeterminateBox.getWidth() / 2.0,
                    indeterminateBox.getHeight() / 2.0
            );
            Color indeterminateBoxBottom = snapshotNodePixel(
                    image,
                    indeterminateBox,
                    indeterminateBox.getWidth() / 2.0,
                    indeterminateBox.getHeight() - 5.0
            );
            assertColorNear(
                    indeterminateBoxTop,
                    checkedBoxFill,
                    0.24,
                    "indeterminate checkbox top fill"
            );
            assertColorNear(
                    indeterminateDash,
                    Color.WHITE,
                    0.18,
                    "indeterminate checkbox dash"
            );
            assertColorNear(
                    indeterminateBoxBottom,
                    checkedBoxFill,
                    0.24,
                    "indeterminate checkbox bottom fill"
            );
            assertTrue(colorDistance(indeterminateBoxTop, indeterminateDash) > 0.6);
            assertTrue(colorDistance(indeterminateBoxBottom, indeterminateDash) > 0.6);
            assertSnapshotNodeContainsContrast(image, lookupRegion(disabledUncheckedCheckBox, ".box"), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, lookupRegion(disabledCheckedCheckBox, ".box"), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(
                    image,
                    lookupRegion(disabledIndeterminateCheckBox, ".mark"),
                    Color.rgb(30, 28, 32),
                    0.1
            );
            assertSnapshotNodeContainsContrast(image, lookupShape(uncheckedRadioButton, ".ring"), Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, lookupShape(selectedRadioButton, ".dot"), Color.WHITE, 0.1);
            assertSnapshotNodeContainsContrast(image, lookupShape(disabledUncheckedRadioButton, ".ring"), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, lookupShape(disabledSelectedRadioButton, ".dot"), Color.WHITE, 0.03);
            assertSnapshotNodeBorderContainsContrast(image, lookupRegion(offSwitch, ".box"), Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, lookupRegion(onSwitch, ".box"), Color.WHITE, 0.1);
            assertSnapshotNodeContainsContrast(image, lookupRegion(onSwitch, ".thumb"), Color.rgb(84, 50, 185), 0.1);
            assertSnapshotNodeContainsContrast(image, lookupRegion(disabledOffSwitch, ".box"), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, lookupRegion(disabledOnSwitch, ".thumb"), Color.WHITE, 0.03);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-selection-states.png"
            ));
            assertRenderedTextNodesStayInsideLayout(row);
            assertFixedTargetControlsKeepCenteredContent(row);
        });
    }

    /// Verifies that containment, feedback, and navigation controls render visible surfaces.
    @Test
    void containmentFeedbackAndNavigationSnapshotRendersVisibleSurfaces() {
        runOnFxThread(() -> {
            M3Avatar avatar = createAvatar("AB", M3AvatarVariant.TERTIARY);
            M3BadgedBox badgedBox = new M3BadgedBox(new M3Avatar("M"), new M3Badge("7"));
            M3ListItem listItem = new M3ListItem("Inbox");
            listItem.setSupportingText("Latest updates");
            listItem.setSelected(true);
            M3ListSectionHeader listHeader = new M3ListSectionHeader("Recent");
            M3ListPane list = new M3ListPane(listHeader, listItem, new M3Divider(), new M3ListItem("Archive"));
            list.setPrefWidth(280.0);
            M3Card card = new M3Card(new Label("Elevated card"));
            card.setVariant(M3CardVariant.ELEVATED);
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            M3NavigationItem home = createNavigationItem("Home", new M3Icon("H"), true);
            M3NavigationItem search = new M3NavigationItem("Search", new M3Icon("S"));
            M3NavigationBar navigationBar = new M3NavigationBar(home, search);

            FlowPane topRow = new FlowPane(18.0, 18.0, avatar, badgedBox, list, card);
            VBox root = new VBox(18.0, topRow, snackbar, navigationBar);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 640.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(640.0, 420.0);
            root.layout();

            assertBadgedBoxBadgeAnchoredToLogicalEnd(badgedBox, false);
            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, avatar, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, badgedBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, list, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, listItemContainer(listItem), Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, lookupRegion(card, ".m3-card-container"), Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, lookupRegion(snackbar, ".m3-snackbar-container"), Color.WHITE, 0.1);
            assertSnapshotNodeContainsContrast(image, lookupRegion(home, ".m3-navigation-item-indicator"),
                    Color.WHITE,
                    0.08);
            assertSnapshotHasColorVariety(image, 14);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-containment-feedback-navigation.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that expanded navigation drawer groups render header and child rows.
    @Test
    void navigationDrawerGroupSnapshotRendersExpandedRows() {
        runOnFxThread(() -> {
            M3ListItem overview = new M3ListItem("Components overview");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem iconButtons = new M3ListItem("Icon buttons");
            group.addItems(buttons, iconButtons);
            group.setExpanded(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(overview, group);
            drawer.select(buttons);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 300.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 300.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertEquals(2, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());
            assertEquals(56.0, buttons.getOneLineHeight(), 0.0001);
            assertEquals(32.0, buttons.getHorizontalPadding(), 0.0001);
            assertTrue(assertInstanceOf(M3DisclosureIcon.class, group.getHeaderItem().getTrailing()).isExpanded());
            assertSnapshotNodeContainsContrast(image, drawer, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, listItemContainer(buttons), Color.WHITE, 0.05);
            assertSnapshotHasColorVariety(image, 8);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-group.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that carousel snapshots render the viewport and selected item.
    @Test
    void carouselSnapshotRendersViewportAndSelectedItem() {
        runOnFxThread(() -> {
            M3Card first = new M3Card(visualLabel("First"), M3CardVariant.FILLED);
            first.setPrefSize(150.0, 84.0);
            M3Card second = new M3Card(visualLabel("Selected"), M3CardVariant.ELEVATED);
            second.setPrefSize(160.0, 84.0);
            M3Card third = new M3Card(visualLabel("Third"), M3CardVariant.OUTLINED);
            third.setPrefSize(150.0, 84.0);
            M3Card fourth = new M3Card(visualLabel("Fourth"), M3CardVariant.FILLED);
            fourth.setPrefSize(150.0, 84.0);
            M3Carousel carousel = new M3Carousel(first, second, third, fourth);
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(360.0, 104.0);
            carousel.selectIndex(1);

            FlowPane root = new FlowPane(carousel);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 160.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(420.0, 160.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, carousel, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, second, Color.WHITE, 0.04);
            assertTrue(second.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS));
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-carousel.png"
            ));
        });
    }

    /// Verifies that date range dialog presets render beside the picker.
    @Test
    void dateRangePresetDialogSnapshotRendersPresetColumn() {
        runOnFxThread(() -> {
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            dialog.setCommonPresets(LocalDate.of(2026, 5, 19));
            M3DialogPane pane = dialog.getM3DialogPane();
            pane.setPrefWidth(660.0);

            StackPane root = new StackPane(pane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 720.0, 600.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(720.0, 600.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, pane, Color.WHITE, 0.04);
            assertEquals(6, pane.lookupAll("." + M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());
            assertSnapshotNodeContainsContrast(
                    image,
                    assertInstanceOf(Node.class, pane.lookup("." + M3DateRangePickerDialog.PRESET_LIST_STYLE_CLASS)),
                    Color.WHITE,
                    0.04
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-range-presets.png"
            ));
        });
    }

    /// Verifies that single-date dialog presets render beside the picker.
    @Test
    void datePresetDialogSnapshotRendersPresetColumn() {
        runOnFxThread(() -> {
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            dialog.setCommonPresets(LocalDate.of(2026, 5, 19));
            M3DialogPane pane = dialog.getM3DialogPane();
            pane.setPrefWidth(620.0);

            StackPane root = new StackPane(pane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 680.0, 600.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(680.0, 600.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, pane, Color.WHITE, 0.04);
            assertEquals(5, pane.lookupAll("." + M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());
            assertSnapshotNodeContainsContrast(
                    image,
                    assertInstanceOf(Node.class, pane.lookup("." + M3DatePickerDialog.PRESET_LIST_STYLE_CLASS)),
                    Color.WHITE,
                    0.04
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-presets.png"
            ));
        });
    }

    /// Verifies that time dialog presets render beside the picker.
    @Test
    void timePresetDialogSnapshotRendersPresetColumn() {
        runOnFxThread(() -> {
            M3TimePickerDialog dialog = new M3TimePickerDialog(LocalTime.of(10, 30));
            dialog.setUse24HourClock(true);
            dialog.setMinuteStep(15);
            dialog.setCommonPresets(LocalTime.of(10, 30));
            M3DialogPane pane = dialog.getM3DialogPane();
            pane.setPrefWidth(720.0);

            StackPane root = new StackPane(pane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 780.0, 620.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(780.0, 620.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, pane, Color.WHITE, 0.04);
            assertEquals(5, pane.lookupAll("." + M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());
            assertSnapshotNodeContainsContrast(
                    image,
                    assertInstanceOf(Node.class, pane.lookup("." + M3TimePickerDialog.PRESET_LIST_STYLE_CLASS)),
                    Color.WHITE,
                    0.04
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-time-presets.png"
            ));
        });
    }

    /// Verifies that picker dialog preset columns follow right-to-left layout direction.
    @Test
    void pickerPresetDialogsPropagateRightToLeftOrientation() {
        runOnFxThread(() -> {
            M3DatePickerDialog dateDialog = new M3DatePickerDialog(LocalDate.of(2026, 5, 19));
            dateDialog.setCommonPresets(LocalDate.of(2026, 5, 19));
            M3DialogPane datePane = dateDialog.getM3DialogPane();
            datePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog(
                    LocalDate.of(2026, 5, 19),
                    LocalDate.of(2026, 5, 24)
            );
            rangeDialog.setCommonPresets(LocalDate.of(2026, 5, 19));
            M3DialogPane rangePane = rangeDialog.getM3DialogPane();
            rangePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3TimePickerDialog timeDialog = new M3TimePickerDialog(LocalTime.of(10, 30));
            timeDialog.setUse24HourClock(true);
            timeDialog.setMinuteStep(15);
            timeDialog.setCommonPresets(LocalTime.of(10, 30));
            M3DialogPane timePane = timeDialog.getM3DialogPane();
            timePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Pane root = new Pane(datePane, rangePane, timePane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 1420.0, 1240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1420.0, 1240.0);
            datePane.resizeRelocate(20.0, 20.0, 620.0, 560.0);
            rangePane.resizeRelocate(680.0, 20.0, 660.0, 560.0);
            timePane.resizeRelocate(20.0, 620.0, 720.0, 560.0);
            root.layout();
            datePane.layout();
            rangePane.layout();
            timePane.layout();

            assertPickerPresetMirrorsRightToLeft(
                    datePane,
                    M3DatePickerDialog.PRESET_CONTENT_STYLE_CLASS,
                    M3DatePickerDialog.PRESET_LIST_STYLE_CLASS,
                    dateDialog.getPicker()
            );
            assertPickerPresetMirrorsRightToLeft(
                    rangePane,
                    M3DateRangePickerDialog.PRESET_CONTENT_STYLE_CLASS,
                    M3DateRangePickerDialog.PRESET_LIST_STYLE_CLASS,
                    rangeDialog.getPicker()
            );
            assertPickerPresetMirrorsRightToLeft(
                    timePane,
                    M3TimePickerDialog.PRESET_CONTENT_STYLE_CLASS,
                    M3TimePickerDialog.PRESET_LIST_STYLE_CLASS,
                    timeDialog.getPicker()
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, datePane, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, rangePane, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, timePane, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-picker-dialog-presets-rtl.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that date range field presets render beside the popup picker.
    @Test
    void dateRangePickerFieldPresetPopupSnapshotRendersPresetColumn() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> fieldReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
                M3DateRangePickerField field = new M3DateRangePickerField(
                        LocalDate.of(2026, 5, 19),
                        LocalDate.of(2026, 5, 25)
                );
                field.setCommonPresets(LocalDate.of(2026, 5, 19));
                field.setPrefWidth(680.0);

                Pane root = new Pane(field);
                Stage stage = new Stage();
                Scene scene = new Scene(root, 760.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                stageReference.set(stage);
                fieldReference.set(field);
                field.showPicker();
            }, () -> {
                M3DateRangePickerField field = Objects.requireNonNull(fieldReference.get());
                assertTrue(field.isShowing());
                Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                Node popupRoot = assertInstanceOf(Node.class, presetContent.getParent());
                popupRoot.applyCss();
                if (popupRoot instanceof Region region) {
                    region.layout();
                }

                WritableImage image = snapshotImageOnFxThread(popupRoot);
                assertEquals(
                        6,
                        presetContent.lookupAll("." + M3DateRangePickerField.PRESET_BUTTON_STYLE_CLASS).size()
                );
                assertSnapshotNodeContainsContrast(
                        image,
                        assertInstanceOf(Node.class, presetContent.lookup(
                                "." + M3DateRangePickerField.PRESET_LIST_STYLE_CLASS
                        )),
                        Color.WHITE,
                        0.04
                );
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-date-range-field-presets.png"
                ));
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3DateRangePickerField field = fieldReference.get();
                if (field != null) {
                    field.hidePicker();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that date range field preset popups mirror preset columns in right-to-left layout.
    @Test
    void dateRangePickerFieldPresetPopupMirrorsRightToLeft() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> fieldReference = new AtomicReference<>();

        try {
            runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
                M3DateRangePickerField field = new M3DateRangePickerField(
                        LocalDate.of(2026, 5, 19),
                        LocalDate.of(2026, 5, 25)
                );
                field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                field.setCommonPresets(LocalDate.of(2026, 5, 19));
                field.setPrefWidth(680.0);

                Pane root = new Pane(field);
                Stage stage = new Stage();
                Scene scene = new Scene(root, 760.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                stageReference.set(stage);
                fieldReference.set(field);
                field.showPicker();
            }, () -> {
                M3DateRangePickerField field = Objects.requireNonNull(fieldReference.get());
                assertTrue(field.isShowing());
                Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                Node popupRoot = assertInstanceOf(Node.class, presetContent.getParent());
                popupRoot.applyCss();
                if (popupRoot instanceof Region region) {
                    region.layout();
                }

                assertPickerPresetMirrorsRightToLeft(
                        popupRoot,
                        M3DateRangePickerField.PRESET_CONTENT_STYLE_CLASS,
                        M3DateRangePickerField.PRESET_LIST_STYLE_CLASS,
                        field.getPicker()
                );

                WritableImage image = snapshotImageOnFxThread(popupRoot);
                assertSnapshotNodeContainsContrast(image, presetContent, Color.WHITE, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-date-range-field-presets-rtl.png"
                ));
                assertRenderedTextNodesStayInsideLayout(popupRoot);
                assertFixedTargetControlsKeepCenteredContent(popupRoot);
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3DateRangePickerField field = fieldReference.get();
                if (field != null) {
                    field.hidePicker();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that date pickers expose rendered day cells to accessibility clients.
    @Test
    void datePickerExposesAccessibleDayCellsAndActions() {
        runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 19));
            datePicker.setDisplayedMonth(YearMonth.of(2026, 5));
            datePicker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            datePicker.setShowAdjacentMonthDays(false);

            Pane root = new Pane(datePicker);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 360.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(31, datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                Node firstDay = assertInstanceOf(Node.class,
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(LocalDate.of(2026, 5, 1), firstDay.getUserData());
                assertNull(datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 31));

                Node selectedDay = assertInstanceOf(Node.class,
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(LocalDate.of(2026, 5, 19), selectedDay.getUserData());
                assertEquals(java.util.List.of(LocalDate.of(2026, 5, 19)),
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

                datePicker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(selectedDay.isFocused());

                datePicker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 14);
                Node shownDay = assertInstanceOf(Node.class,
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 14));
                assertEquals(LocalDate.of(2026, 5, 15), shownDay.getUserData());
                assertTrue(shownDay.isFocused());

                datePicker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalDate.of(2026, 5, 21));

                assertEquals(LocalDate.of(2026, 5, 21), datePicker.getValue());
                assertEquals(java.util.List.of(LocalDate.of(2026, 5, 21)),
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that date picker day labels stay centered inside fixed day cells.
    @Test
    void datePickerCentersDayCellContent() {
        runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            datePicker.setDisplayedMonth(YearMonth.of(2026, 5));
            datePicker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            Pane root = new Pane(datePicker);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(420.0, 360.0);
            datePicker.resize(360.0, 320.0);
            root.layout();
            datePicker.layout();

            for (LocalDate date : java.util.List.of(
                    LocalDate.of(2026, 5, 8),
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 21)
            )) {
                ButtonBase dayCell = dateCellForDate(datePicker, date);
                Node textNode = Objects.requireNonNull(dayCell.lookup(".text"));

                assertEquals(Pos.CENTER, dayCell.getAlignment());
                assertNodeCentersAligned(dayCell, textNode, 0.75);
            }

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    dateCellForDate(datePicker, LocalDate.of(2026, 5, 18)),
                    Color.WHITE,
                    0.08
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-picker-centering.png"
            ));
        });
    }

    /// Verifies that time pickers expose rendered selectable cells to accessibility clients.
    @Test
    void timePickerExposesAccessibleCellsAndActions() {
        runOnFxThread(() -> {
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(9, 30));
            timePicker.setUse24HourClock(true);
            timePicker.setMinuteStep(15);

            Pane root = new Pane(timePicker);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 360.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(28, timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                Node firstHour = assertInstanceOf(Node.class,
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(LocalTime.of(0, 30), firstHour.getUserData());
                assertNull(timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 28));

                Node selectedTime = assertInstanceOf(Node.class,
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(LocalTime.of(9, 30), selectedTime.getUserData());
                assertEquals(java.util.List.of(LocalTime.of(9, 30)),
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

                timePicker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(selectedTime.isFocused());

                timePicker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 25);
                Node shownMinute = assertInstanceOf(Node.class,
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 25));
                assertEquals(LocalTime.of(9, 15), shownMinute.getUserData());
                assertTrue(shownMinute.isFocused());

                timePicker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalTime.of(14, 45, 12));

                assertEquals(LocalTime.of(14, 45), timePicker.getValue());
                assertEquals(java.util.List.of(LocalTime.of(14, 45)),
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that every implemented node-backed control family renders in a full visual gallery.
    @Test
    void allImplementedControlFamiliesRenderVisualGallery() {
        runOnFxThread(() -> {
            M3Text titleText = new M3Text("M3FX", M3TextRole.DISPLAY_SMALL);
            M3Icon primaryIcon = new M3Icon("A", M3IconSize.LARGE, M3IconVariant.PRIMARY);
            M3DisclosureIcon disclosureIcon = new M3DisclosureIcon(true);
            M3Avatar avatar = createAvatar("AB", M3AvatarVariant.PRIMARY);
            M3Badge badge = new M3Badge("9+");
            M3BadgedBox badgedBox = new M3BadgedBox(
                    new M3Icon("M", M3IconSize.LARGE, M3IconVariant.PRIMARY),
                    new M3Badge("3")
            );
            M3Divider horizontalDivider = new M3Divider();
            horizontalDivider.setPrefWidth(220.0);
            M3Divider verticalDivider = new M3Divider(Orientation.VERTICAL);
            verticalDivider.setPrefHeight(48.0);

            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3Button tonalButton = createButton("Tonal", M3ButtonVariant.TONAL);
            M3Button outlinedButton = createButton("Outlined", M3ButtonVariant.OUTLINED);
            M3Button textButton = createButton("Text", M3ButtonVariant.TEXT);
            M3Button elevatedButton = createButton("Elevated", M3ButtonVariant.ELEVATED);
            M3Button disabledButton = createButton("Disabled", M3ButtonVariant.FILLED);
            disabledButton.setDisable(true);
            M3ButtonGroup buttonGroup = new M3ButtonGroup(
                    createButton("Edit", M3ButtonVariant.TONAL),
                    createButton("Share", M3ButtonVariant.TONAL),
                    createButton("Done", M3ButtonVariant.TONAL)
            );
            M3SplitButton splitButton = createSplitButton(
                    "Create",
                    M3ButtonVariant.OUTLINED,
                    new M3MenuItem("Copy")
            );

            M3IconButton iconButton = new M3IconButton(new M3Icon("i"));
            M3IconToggleButton standardToggle = createIconToggleButton(
                    "S",
                    M3IconToggleButtonVariant.STANDARD,
                    true
            );
            M3IconToggleButton filledToggle = createIconToggleButton(
                    "F",
                    M3IconToggleButtonVariant.FILLED,
                    true
            );
            M3IconToggleButton tonalToggle = createIconToggleButton(
                    "T",
                    M3IconToggleButtonVariant.TONAL,
                    true
            );
            M3IconToggleButton outlinedToggle = createIconToggleButton(
                    "O",
                    M3IconToggleButtonVariant.OUTLINED,
                    true
            );
            M3IconToggleButtonGroup iconToggleGroup = new M3IconToggleButtonGroup(
                    standardToggle,
                    filledToggle,
                    tonalToggle,
                    outlinedToggle
            );

            M3FloatingActionButton smallFab = createGraphicFab(
                    new M3Icon("+"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.SMALL
            );
            M3FloatingActionButton regularFab = createGraphicFab(
                    new M3Icon("+"),
                    M3FloatingActionButtonVariant.SECONDARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3FloatingActionButton largeFab = createFab(
                    "*",
                    null,
                    M3FloatingActionButtonVariant.TERTIARY,
                    M3FloatingActionButtonSize.LARGE
            );
            M3FloatingActionButton extendedFab = createFab(
                    "Create",
                    new M3Icon("+"),
                    M3FloatingActionButtonVariant.SURFACE,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.addItems(
                    createGraphicFab(
                            new M3Icon("A"),
                            M3FloatingActionButtonVariant.PRIMARY,
                            M3FloatingActionButtonSize.SMALL
                    ),
                    createGraphicFab(
                            new M3Icon("B"),
                            M3FloatingActionButtonVariant.SECONDARY,
                            M3FloatingActionButtonSize.SMALL
                    )
            );
            fabMenu.setExpanded(true);

            M3TextField filledField = new M3TextField("Filled text field");
            filledField.setPrefWidth(190.0);
            M3TextField outlinedField = createTextField("Outlined text field", M3TextInputVariant.OUTLINED);
            outlinedField.setPrefWidth(210.0);
            M3PasswordField passwordField = createPasswordField("password", M3TextInputVariant.OUTLINED);
            passwordField.setPrefWidth(170.0);
            M3TextField errorField = createTextField("Error", M3TextInputVariant.OUTLINED);
            errorField.setError(true);
            errorField.setPrefWidth(150.0);
            M3TextArea textArea = createTextArea("Multiline\ntext area", M3TextInputVariant.FILLED);
            textArea.setPrefSize(260.0, 96.0);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));

            M3CheckBox selectedCheckBox = createCheckBox("Checkbox", true);
            M3CheckBox indeterminateCheckBox = new M3CheckBox("Mixed");
            indeterminateCheckBox.setIndeterminate(true);
            M3RadioButton selectedRadioButton = createRadioButton("Radio", true);
            M3Switch selectedSwitch = createSwitch("Switch", true);
            M3Switch disabledSwitch = createSwitch("Disabled", true);
            disabledSwitch.setDisable(true);
            M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
            slider.setPrefWidth(260.0);

            M3Chip assistChip = createChip("Assist", M3ChipVariant.ASSIST);
            M3Chip filterChip = createChip("Filter", M3ChipVariant.FILTER, true);
            M3Chip inputChip = createChip("Input", new M3Icon("x"), M3ChipVariant.INPUT);
            M3Chip suggestionChip = createChip("Suggestion", M3ChipVariant.SUGGESTION);
            M3ChipGroup chipGroup = new M3ChipGroup(assistChip, filterChip, inputChip, suggestionChip);
            M3SegmentedButtonGroup segmentedButtons = new M3SegmentedButtonGroup(
                    new M3SegmentedButton("Day"),
                    createSegmentedButton("Week", true),
                    new M3SegmentedButton("Month")
            );
            M3TabBar tabBar = new M3TabBar(
                    createTab("Overview", true),
                    new M3Tab("Details"),
                    new M3Tab("History")
            );

            M3ProgressBar progressBar = new M3ProgressBar(0.62);
            progressBar.setPrefWidth(260.0);
            M3ProgressBar indeterminateProgressBar = new M3ProgressBar();
            indeterminateProgressBar.setPrefWidth(180.0);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.72);
            M3ProgressIndicator indeterminateProgressIndicator = new M3ProgressIndicator();
            M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
            M3LoadingIndicator determinateLoadingIndicator = new M3LoadingIndicator(0.5);

            M3Surface surface = new M3Surface(visualLabel("Surface"));
            surface.setVariant(M3SurfaceVariant.SECONDARY_CONTAINER);
            surface.setElevation(M3SurfaceElevation.LEVEL2);
            surface.setPrefSize(170.0, 80.0);
            M3Card filledCard = new M3Card(visualLabel("Filled card"), M3CardVariant.FILLED);
            filledCard.setPrefSize(150.0, 80.0);
            M3Card elevatedCard = new M3Card(visualLabel("Elevated card"), M3CardVariant.ELEVATED);
            elevatedCard.setPrefSize(150.0, 80.0);
            M3Card outlinedCard = new M3Card(visualLabel("Outlined card"), M3CardVariant.OUTLINED);
            outlinedCard.setPrefSize(150.0, 80.0);
            M3Carousel carousel = new M3Carousel(
                    carouselTestItem("One"),
                    carouselTestItem("Two"),
                    carouselTestItem("Three"),
                    carouselTestItem("Four")
            );
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(420.0, 92.0);
            carousel.selectIndex(1);

            M3Banner banner = createBanner(
                    "Banner message with persistent inline feedback.",
                    new M3Icon("i", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                    createButton("Action", M3ButtonVariant.TEXT)
            );
            banner.setPrefWidth(520.0);
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            M3SnackbarHost snackbarHost = new M3SnackbarHost();
            M3Snackbar hostedSnackbar = new M3Snackbar("Hosted snackbar", "Dismiss");
            snackbarHost.setPrefSize(360.0, 88.0);
            snackbarHost.show(hostedSnackbar);
            hostedSnackbar.setOpacity(1.0);
            hostedSnackbar.setTranslateY(0.0);
            M3Scrim scrim = new M3Scrim();
            scrim.setPrefSize(180.0, 72.0);
            scrim.show();
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setHeaderText("Dialog title");
            dialogPane.setContentText("Dialog content");
            dialogPane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            dialogPane.setPrefWidth(320.0);

            M3ListItem selectedListItem = new M3ListItem("Selected list item");
            selectedListItem.setLeadingIcon("L");
            selectedListItem.setTrailing(new M3Badge("2"));
            selectedListItem.setSupportingText("Supporting text");
            selectedListItem.setTrailingSupportingText("Now");
            selectedListItem.setSelected(true);
            M3ListPane list = new M3ListPane(
                    new M3ListSectionHeader("Inbox"),
                    selectedListItem,
                    new M3Divider(),
                    new M3ListSectionHeader("Labels"),
                    new M3ListItem("List item")
            );
            list.setPrefWidth(300.0);
            M3MenuItem selectedMenuItem = new M3MenuItem("Selected menu item", new M3Icon("M"));
            selectedMenuItem.setSelected(true);
            M3Menu menu = new M3Menu(
                    new M3MenuSectionHeader("Actions"),
                    selectedMenuItem,
                    new M3MenuItem("Menu item")
            );
            menu.setPrefWidth(280.0);
            M3MenuButton menuButton = new M3MenuButton(
                    "Menu",
                    new M3MenuItem("First"),
                    new M3MenuItem("Second")
            );
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setPrefWidth(280.0);
            M3SearchView searchView = new M3SearchView(
                    "Search view",
                    new M3ListItem("First result"),
                    new M3ListItem("Second result")
            );
            searchView.setPrefWidth(340.0);

            M3TopAppBar topAppBar = new M3TopAppBar(
                    "Top app bar",
                    M3TopAppBarVariant.CENTER_ALIGNED,
                    new M3IconButton(new M3Icon("<")),
                    new M3IconButton(new M3Icon("S")),
                    new M3IconButton(new M3Icon("M"))
            );
            topAppBar.setPrefWidth(520.0);
            M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    createGraphicFab(
                            new M3Icon("+"),
                            M3FloatingActionButtonVariant.PRIMARY,
                            M3FloatingActionButtonSize.SMALL
                    ),
                    new M3IconButton(new M3Icon("H")),
                    new M3IconButton(new M3Icon("S"))
            );
            bottomAppBar.setPrefWidth(520.0);

            M3NavigationBar navigationBar = new M3NavigationBar(
                    createNavigationItem("Home", new M3Icon("H"), true),
                    new M3NavigationItem("Search", new M3Icon("S"), new M3Badge("1")),
                    new M3NavigationItem("Profile", new M3Icon("P"))
            );
            navigationBar.setPrefWidth(420.0);
            M3NavigationRail navigationRail = new M3NavigationRail(
                    createNavigationItem("Home", new M3Icon("H"), true),
                    new M3NavigationItem("Search", new M3Icon("S")),
                    new M3NavigationItem("Profile", new M3Icon("P"))
            );
            M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(
                    drawerItem("Inbox", true),
                    drawerItem("Sent", false),
                    drawerItem("Archive", false)
            );
            navigationDrawer.setPrefWidth(280.0);

            M3SideSheet sideSheet = new M3SideSheet(
                    "Side sheet",
                    visualLabel("Side sheet content"),
                    createButton("Action", M3ButtonVariant.TEXT)
            );
            sideSheet.setVariant(M3SheetVariant.MODAL);
            sideSheet.setPrefSize(280.0, 180.0);
            sideSheet.show();
            M3BottomSheet bottomSheet = new M3BottomSheet(
                    "Bottom sheet",
                    visualLabel("Bottom sheet content"),
                    createButton("Done", M3ButtonVariant.TEXT)
            );
            bottomSheet.setPrefSize(360.0, 180.0);
            bottomSheet.show();

            VBox root = new VBox(
                    20.0,
                    visualSection(
                            "Text, Icons, Badges",
                            titleText,
                            primaryIcon,
                            disclosureIcon,
                            avatar,
                            badge,
                            badgedBox,
                            horizontalDivider,
                            verticalDivider
                    ),
                    visualSection(
                            "Buttons",
                            filledButton,
                            tonalButton,
                            outlinedButton,
                            textButton,
                            elevatedButton,
                            disabledButton,
                            buttonGroup,
                            splitButton,
                            iconButton,
                            iconToggleGroup,
                            smallFab,
                            regularFab,
                            largeFab,
                            extendedFab,
                            fabMenu
                    ),
                    visualSection(
                            "Inputs",
                            filledField,
                            outlinedField,
                            passwordField,
                            errorField,
                            textArea,
                            datePicker,
                            dateRangePicker,
                            timePicker
                    ),
                    visualSection(
                            "Selection",
                            selectedCheckBox,
                            indeterminateCheckBox,
                            selectedRadioButton,
                            selectedSwitch,
                            disabledSwitch,
                            slider
                    ),
                    visualSection("Chips, Segments, Tabs", chipGroup, segmentedButtons, tabBar),
                    visualSection(
                            "Progress",
                            progressBar,
                            indeterminateProgressBar,
                            progressIndicator,
                            indeterminateProgressIndicator,
                            loadingIndicator,
                            determinateLoadingIndicator
                    ),
                    visualSection("Surfaces", surface, filledCard, elevatedCard, outlinedCard, carousel),
                    visualSection("Feedback", banner, snackbar, snackbarHost, scrim, dialogPane),
                    visualSection("Lists, Menus, Search", list, menu, menuButton, searchBar, searchView),
                    visualSection("App Bars", topAppBar, bottomAppBar),
                    visualSection("Navigation", navigationBar, navigationRail, navigationDrawer),
                    visualSection("Sheets", sideSheet, bottomSheet)
            );
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 1120.0, 1700.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1120.0, Math.ceil(root.prefHeight(1120.0)));
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 28);
            assertSnapshotNodeContainsContrast(image, titleText, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, disclosureIcon, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, avatar, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, badge, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, filledButton, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, outlinedButton, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, buttonGroup, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, splitButton, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, iconToggleGroup, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, fabMenu, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, filledField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, errorField, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, dateRangePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, timePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, selectedCheckBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, indeterminateCheckBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, selectedRadioButton, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, selectedSwitch, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, disabledSwitch, Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, slider, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, chipGroup, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, segmentedButtons, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, tabBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, progressBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateProgressBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, progressIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateProgressIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, loadingIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, determinateLoadingIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, surface, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, carousel, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    lookupRegion(elevatedCard, ".m3-card-container"),
                    Color.WHITE,
                    0.04
            );
            assertSnapshotNodeContainsContrast(image, banner, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    lookupRegion(snackbar, ".m3-snackbar-container"),
                    Color.WHITE,
                    0.08
            );
            assertSnapshotNodeContainsContrast(image, snackbarHost, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, scrim, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, list, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, menu, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, menuButton, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, searchBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, searchView, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, topAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationRail, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationDrawer, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, sideSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomSheet, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-all-controls.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that the expressive profile affects real rendered controls, not only generated token text.
    @Test
    void expressiveProfileVisualSnapshotRendersProfileSizedControls() {
        runOnFxThread(() -> {
            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3IconButton iconButton = new M3IconButton(new M3Icon("S"));
            M3IconToggleButton standardToggle = createIconToggleButton(
                    "B",
                    M3IconToggleButtonVariant.STANDARD,
                    false
            );
            M3IconToggleButton tonalToggle = createIconToggleButton(
                    "I",
                    M3IconToggleButtonVariant.TONAL,
                    true
            );
            M3IconToggleButton outlinedToggle = createIconToggleButton(
                    "U",
                    M3IconToggleButtonVariant.OUTLINED,
                    false
            );
            M3IconToggleButtonGroup toggleGroup =
                    new M3IconToggleButtonGroup(standardToggle, tonalToggle, outlinedToggle);
            M3FloatingActionButton regularFab = createGraphicFab(
                    new M3Icon("+"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = createSegmentedButton("Week", true);
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segments = new M3SegmentedButtonGroup(day, week, month);
            M3Tab selectedTab = createTab("Overview", true);
            M3TabBar tabBar = new M3TabBar(selectedTab, new M3Tab("Details"));
            M3TextField textField = createTextField("Outlined text", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(260.0);
            M3TextArea textArea = createTextArea("Multiline\ntext", M3TextInputVariant.FILLED);
            textArea.setPrefSize(300.0, 128.0);
            M3Chip filterChip = createChip("Filter", M3ChipVariant.FILTER, true);
            M3CheckBox checkBox = new M3CheckBox("Check");
            checkBox.setSelected(true);
            M3Slider slider = new M3Slider(0.0, 100.0, 54.0);
            slider.setPrefWidth(220.0);
            M3MenuItem menuOpen = new M3MenuItem("Open");
            M3MenuItem menuSave = new M3MenuItem("Save");
            M3Menu menu = new M3Menu(menuOpen, menuSave);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setPrefWidth(300.0);
            M3ListItem searchResult = new M3ListItem("Expressive result");
            M3SearchView searchView = new M3SearchView("Search", searchResult);
            searchView.setPrefWidth(340.0);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            M3ListItem listItem = new M3ListItem("Expressive list item");
            M3Avatar avatar = new M3Avatar("EX");
            M3Card card = new M3Card(new Label("Expressive card"), M3CardVariant.FILLED);
            card.setPrefSize(220.0, 96.0);
            M3Snackbar snackbar = new M3Snackbar("Expressive snackbar", "Action");
            M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Side sheet content"));
            sideSheet.setPrefHeight(160.0);
            M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Bottom sheet content"));
            bottomSheet.setPrefWidth(360.0);
            bottomSheet.setPrefHeight(180.0);
            M3NavigationItem navigationBarItem = createNavigationItem("Home", new M3Icon("H"), true);
            M3NavigationBar navigationBar = new M3NavigationBar(
                    navigationBarItem,
                    new M3NavigationItem("Search", new M3Icon("S"))
            );
            M3NavigationItem navigationRailItem = createNavigationItem("Home", new M3Icon("H"), true);
            M3NavigationRail navigationRail = new M3NavigationRail(
                    navigationRailItem,
                    new M3NavigationItem("Search", new M3Icon("S"))
            );
            M3ListItem drawerItem = new M3ListItem("Inbox");
            drawerItem.setLeadingIcon("I");
            drawerItem.setSelected(true);
            M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(drawerItem);
            navigationDrawer.setPrefHeight(120.0);
            M3TopAppBar topAppBar = new M3TopAppBar(
                    "Expressive app bar",
                    new M3IconButton(new M3Icon("<")),
                    new M3IconButton(new M3Icon("A"))
            );
            topAppBar.setPrefWidth(560.0);
            M3TopAppBar mediumTopAppBar = new M3TopAppBar(
                    "Medium expressive app bar",
                    M3TopAppBarVariant.MEDIUM,
                    new M3IconButton(new M3Icon("<")),
                    new M3IconButton(new M3Icon("A"))
            );
            mediumTopAppBar.setPrefWidth(560.0);

            VBox root = new VBox(
                    18.0,
                    visualSection(
                            "Actions",
                            filledButton,
                            iconButton,
                            toggleGroup,
                            regularFab,
                            segments,
                            tabBar
                    ),
                    visualSection(
                            "Inputs",
                            textField,
                            textArea,
                            filterChip,
                            checkBox,
                            slider,
                            menu,
                            searchBar,
                            searchView,
                            datePicker
                    ),
                    visualSection("Surfaces", card, snackbar),
                    visualSection(
                            "Navigation",
                            listItem,
                            avatar,
                            navigationBar,
                            navigationRail,
                            navigationDrawer,
                            topAppBar,
                            mediumTopAppBar,
                            sideSheet,
                            bottomSheet
                    )
            );
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 960.0, 1080.0);
            M3Theme expressiveTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );

            M3ThemeManager.install(scene, expressiveTheme);
            root.applyCss();
            root.resize(960.0, Math.ceil(root.prefHeight(960.0)));
            root.layout();

            assertTrue(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
            assertEquals(48.0, filledButton.getContainerHeight(), 0.0001);
            assertEquals(28.0, filledButton.getHorizontalPadding(), 0.0001);
            assertEquals(48.0, iconButton.getContainerHeight(), 0.0001);
            assertEquals(48.0, standardToggle.getContainerHeight(), 0.0001);
            assertEquals(64.0, regularFab.getContainerSize(), 0.0001);
            assertEquals(18.0, regularFab.getHorizontalPadding(), 0.0001);
            assertEquals(48.0, day.getContainerHeight(), 0.0001);
            assertEquals(16.0, day.getHorizontalPadding(), 0.0001);
            assertEquals(56.0, selectedTab.getContainerHeight(), 0.0001);
            assertEquals(20.0, selectedTab.getHorizontalPadding(), 0.0001);
            assertEquals(4.0, selectedTab.getActiveIndicatorHeight(), 0.0001);
            assertEquals(64.0, textField.getContainerHeight(), 0.0001);
            assertEquals(20.0, textField.getHorizontalPadding(), 0.0001);
            assertEquals(128.0, textArea.getContainerHeight(), 0.0001);
            assertEquals(20.0, textArea.getHorizontalPadding(), 0.0001);
            assertEquals(20.0, textArea.getVerticalPadding(), 0.0001);
            assertEquals(36.0, filterChip.getContainerHeight(), 0.0001);
            assertEquals(18.0, filterChip.getHorizontalPadding(), 0.0001);
            assertEquals(48.0, checkBox.getTouchTargetSize(), 0.0001);
            assertEquals(6.0, slider.getTrackThickness(), 0.0001);
            assertEquals(24.0, slider.getThumbSize(), 0.0001);
            assertEquals(56.0, slider.getTouchTargetSize(), 0.0001);
            assertEquals(10.0, menu.getPadding().getTop(), 0.0001);
            assertEquals(56.0, menuOpen.getOneLineHeight(), 0.0001);
            assertEquals(16.0, menuOpen.getHorizontalPadding(), 0.0001);
            assertEquals(16.0, menuOpen.getContentSpacing(), 0.0001);
            assertEquals(64.0, searchBar.getPrefHeight(), 0.0001);
            assertEquals(64.0, searchBar.getHeight(), 0.0001);
            assertEquals(20.0, searchBar.getPadding().getLeft(), 0.0001);
            assertEquals(12.0, searchView.getPadding().getBottom(), 0.0001);
            assertEquals(64.0, searchResult.getOneLineHeight(), 0.0001);
            assertEquals(20.0, searchResult.getHorizontalPadding(), 0.0001);
            assertEquals(16.0, searchResult.getContentSpacing(), 0.0001);
            assertEquals(64.0, listItem.getOneLineHeight(), 0.0001);
            assertEquals(44.0, avatar.getContainerSize(), 0.0001);
            assertEquals(24.0, card.getContainerShape(), 0.0001);
            assertEquals(20.0, card.getContentPadding(), 0.0001);
            assertEquals(16.0, snackbar.getContainerShape(), 0.0001);
            assertEquals(18.0, snackbar.getContentPadding(), 0.0001);
            assertEquals(384.0, sideSheet.getPrefWidth(), 0.0001);
            assertEquals(360.0, bottomSheet.getPrefHeight(), 0.0001);
            assertEquals(
                    28.0,
                    lookupRegion(sideSheet, "." + M3SideSheet.CONTENT_STYLE_CLASS).getPadding().getLeft(),
                    0.0001
            );
            assertEquals(
                    36.0,
                    lookupRegion(bottomSheet, "." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS).getPrefWidth(),
                    0.0001
            );
            assertEquals(88.0, navigationBar.getPrefHeight(), 0.0001);
            assertEquals(96.0, navigationBarItem.getItemWidth(), 0.0001);
            assertEquals(72.0, navigationBarItem.getIndicatorWidth(), 0.0001);
            assertEquals(6.0, navigationBarItem.getContentSpacing(), 0.0001);
            assertEquals(112.0, navigationRail.getPrefWidth(), 0.0001);
            assertEquals(96.0, navigationRailItem.getItemWidth(), 0.0001);
            assertEquals(6.0, navigationRailItem.getContentSpacing(), 0.0001);
            assertEquals(384.0, navigationDrawer.getPrefWidth(), 0.0001);
            assertEquals(64.0, drawerItem.getOneLineHeight(), 0.0001);
            assertEquals(24.0, drawerItem.getContainerShape(), 0.0001);
            assertEquals(20.0, drawerItem.getHorizontalPadding(), 0.0001);
            assertEquals(16.0, drawerItem.getContentSpacing(), 0.0001);
            assertEquals(72.0, topAppBar.getPrefHeight(), 0.0001);
            assertEquals(120.0, mediumTopAppBar.getPrefHeight(), 0.0001);
            assertEquals(24.0, mediumTopAppBar.getPadding().getBottom(), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 18);
            assertSnapshotNodeContainsContrast(image, filledButton, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, toggleGroup, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, menu, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, checkBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, slider, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, searchView, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, card, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, snackbar, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, sideSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationRail, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationDrawer, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, topAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, mediumTopAppBar, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-expressive-profile.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that actual dark expressive theme tokens render the main control families visibly.
    @Test
    void darkExpressiveVisualSnapshotRendersTokenDrivenControls() {
        runOnFxThread(() -> {
            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3Button tonalButton = createButton("Tonal", M3ButtonVariant.TONAL);
            M3Button outlinedButton = createButton("Outlined", M3ButtonVariant.OUTLINED);
            M3IconToggleButton selectedToggle = createIconToggleButton(
                    "B",
                    M3IconToggleButtonVariant.FILLED,
                    true
            );
            M3FloatingActionButton fab = createGraphicFab(
                    new M3Icon("+"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3TextField textField = createTextField("Dark field", M3TextInputVariant.OUTLINED);
            textField.setText("M3FX");
            textField.setPrefWidth(260.0);
            M3PasswordField passwordField = new M3PasswordField("Password");
            passwordField.setPrefWidth(260.0);
            M3CheckBox checkBox = new M3CheckBox("Checkbox");
            checkBox.setSelected(true);
            M3CheckBox indeterminateCheckBox = new M3CheckBox("Mixed");
            indeterminateCheckBox.setIndeterminate(true);
            M3RadioButton radioButton = new M3RadioButton("Radio");
            radioButton.setSelected(true);
            M3Switch controlSwitch = new M3Switch("Switch");
            controlSwitch.setSelected(true);
            M3Slider slider = new M3Slider(0.0, 100.0, 48.0);
            slider.setPrefWidth(260.0);
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = createSegmentedButton("Week", true);
            M3SegmentedButtonGroup segments = new M3SegmentedButtonGroup(day, week);
            M3Chip filterChip = createChip("Filter", M3ChipVariant.FILTER, true);
            M3ProgressBar progressBar = new M3ProgressBar(0.58);
            progressBar.setPrefWidth(280.0);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.7);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 21));
            M3SearchBar searchBar = new M3SearchBar("Search dark");
            searchBar.setPrefWidth(320.0);
            M3NavigationItem home = createNavigationItem("Home", new M3Icon("H"), true);
            M3NavigationBar navigationBar = new M3NavigationBar(
                    home,
                    new M3NavigationItem("Search", new M3Icon("S"))
            );
            M3Snackbar snackbar = new M3Snackbar("Dark snackbar", "Action");

            VBox root = new VBox(
                    18.0,
                    visualSection("Actions", filledButton, tonalButton, outlinedButton, selectedToggle, fab),
                    visualSection("Inputs", textField, passwordField, searchBar),
                    visualSection(
                            "Selection",
                            checkBox,
                            indeterminateCheckBox,
                            radioButton,
                            controlSwitch,
                            slider,
                            segments,
                            filterChip
                    ),
                    visualSection("Feedback", progressBar, progressIndicator, snackbar),
                    visualSection("Navigation", datePicker, navigationBar)
            );
            root.setStyle("-fx-background-color: -m3-color-surface; -fx-padding: 24px;");
            Scene scene = new Scene(root, 980.0, 880.0);
            M3Theme theme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );

            M3ThemeManager.install(scene, theme);
            root.applyCss();
            root.resize(980.0, Math.ceil(root.prefHeight(980.0)));
            root.layout();

            assertTrue(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            assertEquals(48.0, filledButton.getContainerHeight(), 0.0001);
            assertEquals(64.0, textField.getContainerHeight(), 0.0001);
            assertEquals(64.0, searchBar.getHeight(), 0.0001);
            assertEquals(48.0, selectedToggle.getContainerHeight(), 0.0001);
            assertEquals(64.0, fab.getContainerSize(), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 18);
            assertSnapshotNodeContainsContrast(image, filledButton, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, textField, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, indeterminateCheckBox, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, slider, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, progressBar, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, navigationBar, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, snackbar, Color.BLACK, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-dark-expressive-controls.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
            assertFixedTargetControlsKeepCenteredContent(root);
        });
    }

    /// Verifies that dark expressive popup roots inherit theme mode classes and render visible content.
    @Test
    void darkExpressivePopupVisualSnapshotInheritsThemeContext() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Theme theme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem save = new M3MenuItem("Save");
            M3MenuButton menuButton = new M3MenuButton("More", open, save);
            M3Button owner = new M3Button("Owner");
            M3Tooltip tooltip = new M3Tooltip("Dark expressive tooltip");
            try {
                VBox root = new VBox(16.0, menuButton, owner);
                root.setStyle("-fx-background-color: -m3-color-surface; -fx-padding: 24px;");
                Scene scene = new Scene(root, 360.0, 180.0);
                M3ThemeManager.install(scene, theme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(360.0, 180.0);
                root.layout();

                menuButton.showMenu();
                Timeline showAnimation = controlTimeline(menuButton, "showAnimation");
                showAnimation.jumpTo(showAnimation.getTotalDuration());
                showAnimation.stop();

                M3Menu menu = menuButton.getMenu();
                resizeToPreferredSize(menu);
                assertTrue(menu.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(menu.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertSame(theme, M3ThemeManager.getTheme(menu));
                assertTrue(menu.getStyle().contains("-m3-color-primary"));

                WritableImage menuImage = snapshotImageOnFxThread(menu);
                assertSnapshotHasColorVariety(menuImage, 4);
                assertSnapshotNodeContainsContrast(menuImage, menu, Color.BLACK, 0.08);
                writeVisualSnapshot(menuImage, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-dark-expressive-menu-popup.png"
                ));
                assertRenderedTextNodesStayInsideLayout(menu);
                assertFixedTargetControlsKeepCenteredContent(menu);

                tooltip.setTheme(theme);
                tooltip.show(owner, stage.getX() + 48.0, stage.getY() + 128.0);
                Parent tooltipRoot = tooltip.getScene().getRoot();
                tooltipRoot.applyCss();
                if (tooltipRoot instanceof Region region) {
                    resizeToPreferredSize(region);
                } else {
                    tooltipRoot.layout();
                }
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertSame(theme, tooltip.getTheme());
                assertTrue(tooltip.getStyle().contains("-m3-color-primary"));

                WritableImage tooltipImage = snapshotImageOnFxThread(tooltipRoot);
                assertSnapshotHasColorVariety(tooltipImage, 2);
                assertSnapshotNodeContainsContrast(tooltipImage, tooltipRoot, Color.BLACK, 0.08);
                writeVisualSnapshot(tooltipImage, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-dark-expressive-tooltip.png"
                ));
                assertRenderedTextNodesStayInsideLayout(tooltipRoot);
            } finally {
                tooltip.hide();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that tooltip popups render their inverse surface and text.
    @Test
    void tooltipSnapshotRendersPopupSurface() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Tooltip tooltip = new M3Tooltip("Tooltip text");
            try {
                M3Button owner = new M3Button("Owner");
                Pane root = new Pane(owner);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                Scene scene = new Scene(root, 240.0, 120.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(240.0, 120.0);
                root.layout();

                tooltip.setTheme(M3Theme.defaultTheme());
                tooltip.show(owner, stage.getX() + 40.0, stage.getY() + 96.0);

                var tooltipRoot = tooltip.getScene().getRoot();
                tooltipRoot.applyCss();
                tooltipRoot.layout();

                WritableImage image = snapshotImageOnFxThread(tooltipRoot);
                assertSnapshotHasColorVariety(image, 2);
                assertSnapshotNodeContainsContrast(image, tooltipRoot, Color.WHITE, 0.2);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-tooltip.png"
                ));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip popups render their surface, text, and actions.
    @Test
    void richTooltipSnapshotRendersPopupSurface() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3RichTooltip tooltip = new M3RichTooltip(
                    "Rich tooltip",
                    "A wider tooltip surface can include a title, supporting text, and actions.",
                    createButton("Action", M3ButtonVariant.TEXT)
            );
            try {
                M3Button owner = new M3Button("Owner");
                Pane root = new Pane(owner);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                Scene scene = new Scene(root, 420.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(420.0, 220.0);
                root.layout();

                tooltip.setTheme(M3Theme.defaultTheme());
                tooltip.show(owner, stage.getX() + 40.0, stage.getY() + 96.0);

                var tooltipRoot = tooltip.getScene().getRoot();
                tooltipRoot.applyCss();
                tooltipRoot.layout();

                WritableImage image = snapshotImageOnFxThread(tooltipRoot);
                assertSnapshotHasColorVariety(image, 4);
                assertSnapshotNodeContainsContrast(image, tooltipRoot, Color.WHITE, 0.08);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-rich-tooltip.png"
                ));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that m3fx sliders create the Material Design 3 slider skin.
    @Test
    void sliderCreatesMaterialSkin() {
        M3Slider slider = new M3Slider();

        applyCss(slider);

        assertInstanceOf(M3SliderSkin.class, slider.getSkin());
    }

    /// Verifies that sliders expose accessible value, range, orientation, and value adjustment actions.
    @Test
    void sliderExposesAccessibleValuesAndActions() {
        M3Slider slider = new M3Slider(0.0, 100.0, 40.0);
        slider.setBlockIncrement(10.0);
        @Nullable AccessibleAttribute valueStringAttribute = M3Accessible.attribute("VALUE_STRING");

        assertEquals(0.0, slider.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(100.0, slider.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(40.0, (Double) slider.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("40.0", slider.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(Orientation.HORIZONTAL, slider.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

        slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 75.0);
        assertEquals(75.0, slider.getValue(), 0.0001);
        slider.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(85.0, slider.getValue(), 0.0001);
        slider.executeAccessibleAction(AccessibleAction.BLOCK_DECREMENT);
        assertEquals(75.0, slider.getValue(), 0.0001);
        slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 120.0);
        assertEquals(100.0, slider.getValue(), 0.0001);

        slider.setOrientation(Orientation.VERTICAL);

        assertEquals(Orientation.VERTICAL, slider.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));
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

        M3Banner banner = new M3Banner("Message");
        M3Snackbar snackbar = new M3Snackbar("Message");
        M3SnackbarHost snackbarHost = new M3SnackbarHost();
        M3TopAppBar topAppBar = new M3TopAppBar();
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        M3SideSheet sideSheet = new M3SideSheet();
        M3BottomSheet bottomSheet = new M3BottomSheet();
        M3Scrim scrim = new M3Scrim();
        M3Carousel carousel = new M3Carousel();
        M3NavigationBar navigationBar = new M3NavigationBar();
        M3NavigationRail navigationRail = new M3NavigationRail();
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();
        M3NavigationDrawerGroup navigationDrawerGroup = new M3NavigationDrawerGroup("Group");
        M3ListItem navigationDrawerGroupChild = new M3ListItem("Child");
        navigationDrawerGroup.addItem(navigationDrawerGroupChild);

        assertTrue(new M3ButtonGroup().getStyleClass().contains(M3ButtonGroup.STYLE_CLASS));
        assertTrue(new M3SplitButton("Create").getStyleClass().contains(M3SplitButton.STYLE_CLASS));
        assertTrue(new M3FabMenu().getStyleClass().contains(M3FabMenu.STYLE_CLASS));
        assertTrue(card.getStyleClass().contains(M3Card.STYLE_CLASS));
        assertTrue(card.getStyleClass().contains(M3CardVariant.OUTLINED.getStyleClass()));
        assertTrue(banner.getStyleClass().contains(M3Banner.STYLE_CLASS));
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
        assertTrue(carousel.getStyleClass().contains(M3Carousel.STYLE_CLASS));
        assertTrue(navigationBar.getStyleClass().contains(M3NavigationBar.STYLE_CLASS));
        assertTrue(navigationRail.getStyleClass().contains(M3NavigationRail.STYLE_CLASS));
        assertTrue(navigationDrawer.getStyleClass().contains(M3NavigationDrawer.STYLE_CLASS));
        assertTrue(navigationDrawerGroup.getStyleClass().contains(M3NavigationDrawerGroup.STYLE_CLASS));
        assertTrue(navigationDrawerGroup.getHeaderItem().getStyleClass().contains(
                M3NavigationDrawerGroup.HEADER_STYLE_CLASS
        ));
        assertTrue(navigationDrawerGroupChild.getStyleClass().contains(
                M3NavigationDrawerGroup.CHILD_STYLE_CLASS
        ));
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
        assertTrue(new M3TextInputLayout().getStyleClass().contains(M3TextInputLayout.STYLE_CLASS));
        assertTrue(new M3TextArea().getStyleClass().contains(M3TextArea.STYLE_CLASS));
        assertTrue(new M3Tooltip().getStyleClass().contains(M3Tooltip.STYLE_CLASS));
        assertTrue(new M3RichTooltip().getStyleClass().contains(M3RichTooltip.STYLE_CLASS));
        assertTrue(new M3Avatar("A").getStyleClass().contains(M3Avatar.STYLE_CLASS));
        assertTrue(new M3Icon("A").getStyleClass().contains(M3Icon.STYLE_CLASS));
        assertTrue(new M3IconToggleButton("A").getStyleClass().contains(M3IconToggleButton.STYLE_CLASS));
        assertTrue(new M3IconToggleButtonGroup().getStyleClass().contains(M3IconToggleButtonGroup.STYLE_CLASS));
        assertTrue(new M3Text("Text").getStyleClass().contains(M3Text.STYLE_CLASS));
        assertTrue(new M3Surface().getStyleClass().contains(M3Surface.STYLE_CLASS));
        assertTrue(new M3ValidationSummary().getStyleClass().contains(M3ValidationSummary.STYLE_CLASS));
        assertTrue(new M3BadgedBox().getStyleClass().contains(M3BadgedBox.STYLE_CLASS));
        assertTrue(new M3Menu().getStyleClass().contains(M3Menu.STYLE_CLASS));
        assertTrue(new M3MenuItem("Open").getStyleClass().contains(M3MenuItem.STYLE_CLASS));
        assertTrue(new M3SubMenuItem("Export").getStyleClass().contains(M3SubMenuItem.STYLE_CLASS));
        assertTrue(new M3MenuSectionHeader("File").getStyleClass().contains(M3MenuSectionHeader.STYLE_CLASS));
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
        assertTrue(new M3DisclosureIcon().getStyleClass().contains(M3DisclosureIcon.STYLE_CLASS));
        assertTrue(new M3Badge("1").getStyleClass().contains(M3Badge.STYLE_CLASS));
        assertTrue(new M3NavigationItem("Home").getStyleClass().contains(M3NavigationItem.STYLE_CLASS));
        assertTrue(new M3ListPane().getStyleClass().contains(M3ListPane.STYLE_CLASS));
        assertTrue(new M3ListView<>().getStyleClass().contains(M3ListView.STYLE_CLASS));
        assertTrue(new M3ListViewCell<>(new M3ListView<>()).getStyleClass().contains(M3ListViewCell.STYLE_CLASS));
        assertTrue(new M3ListItem("Item").getStyleClass().contains(M3ListItem.STYLE_CLASS));
        assertTrue(new M3ListSectionHeader("Section").getStyleClass().contains(M3ListSectionHeader.STYLE_CLASS));
    }

    /// Verifies that M3FX controls avoid concrete JavaFX control inheritance.
    @Test
    void controlsDoNotExtendConcreteJavaFxControls() {
        assertFalse(javafx.scene.control.Button.class.isAssignableFrom(M3Button.class));
        assertFalse(javafx.scene.control.Button.class.isAssignableFrom(M3ButtonGroup.class));
        assertFalse(HBox.class.isAssignableFrom(M3ButtonGroup.class));
        assertFalse(javafx.scene.control.Button.class.isAssignableFrom(M3SplitButton.class));
        assertFalse(HBox.class.isAssignableFrom(M3SplitButton.class));
        assertFalse(javafx.scene.control.Button.class.isAssignableFrom(M3FabMenu.class));
        assertFalse(VBox.class.isAssignableFrom(M3FabMenu.class));
        assertFalse(javafx.scene.control.MenuButton.class.isAssignableFrom(M3SplitButton.class));
        assertFalse(javafx.scene.control.Button.class.isAssignableFrom(M3FloatingActionButton.class));
        assertFalse(javafx.scene.control.CheckBox.class.isAssignableFrom(M3CheckBox.class));
        assertFalse(javafx.scene.control.RadioButton.class.isAssignableFrom(M3RadioButton.class));
        assertFalse(javafx.scene.control.CheckBox.class.isAssignableFrom(M3Switch.class));
        assertFalse(javafx.scene.control.Slider.class.isAssignableFrom(M3Slider.class));
        assertFalse(javafx.scene.control.ProgressBar.class.isAssignableFrom(M3ProgressBar.class));
        assertFalse(javafx.scene.control.ProgressIndicator.class.isAssignableFrom(M3ProgressIndicator.class));
        assertFalse(javafx.scene.control.Tooltip.class.isAssignableFrom(M3Tooltip.class));
        assertFalse(VBox.class.isAssignableFrom(M3Menu.class));
        assertFalse(javafx.scene.control.Label.class.isAssignableFrom(M3DisclosureIcon.class));
        assertFalse(javafx.scene.control.Label.class.isAssignableFrom(M3Icon.class));
        assertFalse(javafx.scene.control.Label.class.isAssignableFrom(M3Text.class));
        assertFalse(javafx.scene.control.Label.class.isAssignableFrom(M3ListSectionHeader.class));
        assertFalse(StackPane.class.isAssignableFrom(M3Avatar.class));
        assertFalse(StackPane.class.isAssignableFrom(M3BadgedBox.class));
        assertFalse(StackPane.class.isAssignableFrom(M3Surface.class));
        assertFalse(StackPane.class.isAssignableFrom(M3SnackbarHost.class));
        assertFalse(ScrollPane.class.isAssignableFrom(M3Carousel.class));
        assertFalse(VBox.class.isAssignableFrom(M3ListPane.class));
        assertFalse(javafx.scene.control.ListView.class.isAssignableFrom(M3ListView.class));
        assertFalse(HBox.class.isAssignableFrom(M3NavigationBar.class));
        assertFalse(VBox.class.isAssignableFrom(M3NavigationRail.class));
        assertFalse(VBox.class.isAssignableFrom(M3NavigationDrawer.class));
        assertFalse(VBox.class.isAssignableFrom(M3NavigationDrawerGroup.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3Chip.class));
        assertFalse(FlowPane.class.isAssignableFrom(M3ChipGroup.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3IconToggleButton.class));
        assertFalse(HBox.class.isAssignableFrom(M3IconToggleButtonGroup.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3SegmentedButton.class));
        assertFalse(HBox.class.isAssignableFrom(M3SegmentedButtonGroup.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3Tab.class));
        assertFalse(HBox.class.isAssignableFrom(M3TabBar.class));
        assertFalse(javafx.scene.control.ToggleButton.class.isAssignableFrom(M3NavigationItem.class));
    }

    /// Verifies that custom selectable controls expose accessibility selection state.
    @Test
    void selectableControlsExposeAccessibleSelectionState() {
        M3Chip chip = createChip("Filter", M3ChipVariant.FILTER, true);
        M3IconToggleButton iconToggleButton = createIconToggleButton(
                "star",
                M3IconToggleButtonVariant.TONAL,
                true
        );
        M3SegmentedButton segmentedButton = createSegmentedButton("Day", true);
        M3Tab tab = createTab("Overview", true);
        M3NavigationItem navigationItem = createNavigationItem("Home", true);

        assertEquals(true, chip.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(AccessibleAttribute.ToggleState.CHECKED,
                chip.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));
        chip.setSelected(false);
        assertEquals(false, chip.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(AccessibleAttribute.ToggleState.UNCHECKED,
                chip.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));

        assertEquals(true, iconToggleButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(AccessibleAttribute.ToggleState.CHECKED,
                iconToggleButton.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));
        assertEquals(true, segmentedButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(AccessibleAttribute.ToggleState.CHECKED,
                segmentedButton.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE));
        assertEquals(true, tab.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(true, navigationItem.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
    }

    /// Verifies that list and menu items expose accessible text, selection, position, and fire actions.
    @Test
    void listAndMenuItemsExposeAccessibleStateAndActions() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setOverlineText("Overline");
        listItem.setSupportingText("Supporting");
        listItem.setSelected(true);
        AtomicInteger listActions = new AtomicInteger();
        listItem.setOnAction(event -> listActions.incrementAndGet());
        M3ListPane list = new M3ListPane(new M3Divider(), listItem);

        applyCss(list);

        assertEquals("Overline Headline Supporting", listItem.getAccessibleText());
        assertEquals(true, listItem.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(1, listItem.queryAccessibleAttribute(AccessibleAttribute.INDEX));
        listItem.executeAccessibleAction(AccessibleAction.FIRE);
        listItem.setDisable(true);
        listItem.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, listActions.get());

        M3MenuItem menuItem = new M3MenuItem("Open");
        menuItem.setSelected(true);
        AtomicInteger menuActions = new AtomicInteger();
        menuItem.setOnAction(event -> menuActions.incrementAndGet());
        new M3Menu(new M3Divider(), menuItem);

        assertEquals("Open", menuItem.getAccessibleText());
        assertEquals(true, menuItem.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(1, menuItem.queryAccessibleAttribute(AccessibleAttribute.INDEX));
        menuItem.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, menuActions.get());

        assertEquals(listItem, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
    }

    /// Verifies that selection containers expose accessible item collections and selected items.
    @Test
    void selectionContainersExposeAccessibleCollections() {
        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        M3ListPane list = new M3ListPane(listFirst, new M3Divider(), listSecond);
        list.setSelectionMode(M3ListSelectionMode.MULTIPLE);
        listFirst.setSelected(true);
        listSecond.setSelected(true);

        assertEquals(3, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(listFirst, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertNull(list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, -1));
        assertEquals(true, list.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(list.getSelectedItems(), list.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menu.select(menuSecond);

        assertEquals(2, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(menuSecond, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, menu.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(menu.getSelectedItems(), menu.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3Chip firstChip = createChip("Input", M3ChipVariant.INPUT, true);
        M3Chip secondChip = createChip("Filter", M3ChipVariant.FILTER, false);
        M3ChipGroup chipGroup = new M3ChipGroup(firstChip, secondChip);

        assertEquals(2, chipGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(firstChip, chipGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(true, chipGroup.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(chipGroup.getSelectedChips(), chipGroup.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3IconToggleButton iconFirst = createIconToggleButton("edit", M3IconToggleButtonVariant.STANDARD, true);
        M3IconToggleButton iconSecond = createIconToggleButton("done", M3IconToggleButtonVariant.STANDARD, false);
        M3IconToggleButtonGroup iconGroup = new M3IconToggleButtonGroup(iconFirst, iconSecond);

        assertEquals(iconFirst, iconGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(false, iconGroup.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(iconGroup.getSelectedButtons(), iconGroup.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3SegmentedButton segmentFirst = createSegmentedButton("Day", true);
        M3SegmentedButton segmentSecond = createSegmentedButton("Week", false);
        M3SegmentedButtonGroup segmentedGroup = new M3SegmentedButtonGroup(segmentFirst, segmentSecond);

        assertEquals(segmentSecond, segmentedGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, segmentedGroup.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(segmentedGroup.getSelectedButtons(),
                segmentedGroup.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3Tab tabFirst = createTab("Overview", true);
        M3Tab tabSecond = createTab("Details", false);
        M3TabBar tabBar = new M3TabBar(tabFirst, tabSecond);

        assertEquals(tabSecond, tabBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, tabBar.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(tabBar.getSelectedTabs(), tabBar.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3NavigationItem navFirst = createNavigationItem("Home", true);
        M3NavigationItem navSecond = createNavigationItem("Search", false);
        M3NavigationBar navigationBar = new M3NavigationBar(navFirst, navSecond);
        M3NavigationRail navigationRail = new M3NavigationRail(
                createNavigationItem("Home", true),
                createNavigationItem("Search", false)
        );

        assertEquals(navSecond, navigationBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, navigationBar.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(navigationBar.getSelectedItems(),
                navigationBar.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
        assertEquals(2, navigationRail.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(navigationRail.getSelectedItems(),
                navigationRail.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3ListItem drawerFirst = new M3ListItem("Inbox");
        M3ListItem drawerSecond = new M3ListItem("Archive");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(drawerFirst, drawerSecond);
        navigationDrawer.select(drawerSecond);

        assertEquals(drawerSecond, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(navigationDrawer.getSelectedItems(),
                navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
    }

    /// Verifies that selection containers apply selected items requested by accessibility clients.
    @Test
    void selectionContainersApplyAccessibleSelectionActions() {
        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        M3ListPane list = new M3ListPane(listFirst, listSecond);
        list.setSelectionMode(M3ListSelectionMode.MULTIPLE);

        list.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, java.util.List.of(listSecond));

        assertFalse(listFirst.isSelected());
        assertTrue(listSecond.isSelected());
        assertEquals(java.util.List.of(listSecond), list.getSelectedItems());

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);

        menu.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, menuSecond);

        assertEquals(menuSecond, menu.getSelectedItem());
        assertFalse(menuFirst.isSelected());
        assertTrue(menuSecond.isSelected());

        M3Chip chipFirst = new M3Chip("Input");
        M3Chip chipSecond = new M3Chip("Filter");
        M3ChipGroup chipGroup = new M3ChipGroup(chipFirst, chipSecond);

        chipGroup.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, chipFirst, chipSecond);

        assertEquals(java.util.List.of(chipFirst, chipSecond), chipGroup.getSelectedChips());

        M3IconToggleButton iconFirst = new M3IconToggleButton("edit");
        M3IconToggleButton iconSecond = new M3IconToggleButton("done");
        M3IconToggleButtonGroup iconGroup = new M3IconToggleButtonGroup(iconFirst, iconSecond);

        iconGroup.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, java.util.List.of(iconSecond));

        assertEquals(iconSecond, iconGroup.getSelectedButton());

        M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
        M3SegmentedButtonGroup segmentedGroup = new M3SegmentedButtonGroup(segmentFirst, segmentSecond);
        segmentedGroup.setSelectionMode(M3SegmentedButtonSelectionMode.MULTIPLE);

        segmentedGroup.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, segmentFirst, segmentSecond);

        assertEquals(java.util.List.of(segmentFirst, segmentSecond), segmentedGroup.getSelectedButtons());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3TabBar tabBar = new M3TabBar(tabFirst, tabSecond);

        tabBar.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, tabSecond);

        assertEquals(tabSecond, tabBar.getSelectedTab());

        M3NavigationItem navFirst = new M3NavigationItem("Home");
        M3NavigationItem navSecond = new M3NavigationItem("Search");
        M3NavigationBar navigationBar = new M3NavigationBar(navFirst, navSecond);

        navigationBar.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, navSecond);

        assertEquals(navSecond, navigationBar.getSelectedItem());

        M3NavigationItem railFirst = new M3NavigationItem("Home");
        M3NavigationItem railSecond = new M3NavigationItem("Search");
        M3NavigationRail navigationRail = new M3NavigationRail(railFirst, railSecond);

        navigationRail.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, railSecond);

        assertEquals(railSecond, navigationRail.getSelectedItem());

        M3ListItem drawerFirst = new M3ListItem("Inbox");
        M3ListItem drawerSecond = new M3ListItem("Archive");
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(drawerFirst, drawerSecond);

        navigationDrawer.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, drawerSecond);

        assertEquals(drawerSecond, navigationDrawer.getSelectedItem());
    }

    /// Verifies that menu buttons expose their popup menu to accessibility clients.
    @Test
    void menuButtonExposesAccessiblePopupState() {
        M3MenuItem first = new M3MenuItem("Open");
        M3MenuItem second = new M3MenuItem("Save");
        M3MenuButton menuButton = new M3MenuButton("More", first, second);
        menuButton.setSelectionMode(M3MenuSelectionMode.SINGLE);

        assertEquals(false, menuButton.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(menuButton.getMenu(), menuButton.queryAccessibleAttribute(AccessibleAttribute.SUBMENU));
        assertEquals(2, menuButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, menuButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, menuButton.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(menuButton.getSelectedItems(),
                menuButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        menuButton.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, second);
        assertEquals(second, menuButton.getSelectedItem());
        menuButton.executeAccessibleAction(AccessibleAction.SHOW_MENU);
        menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);
    }

    /// Verifies that search controls expose accessible text, focus, action, and result state.
    @Test
    void searchControlsExposeAccessibleStateAndActions() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        AtomicInteger searchActions = new AtomicInteger();
        searchBar.setOnAction(event -> searchActions.incrementAndGet());

        searchBar.executeAccessibleAction(AccessibleAction.SET_TEXT, "material");
        assertEquals("material", searchBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(searchBar.getEditor(), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        assertEquals(true, searchBar.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchBar.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, searchActions.get());

        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3SearchView searchView = new M3SearchView("Search", first, second);
        AtomicInteger viewActions = new AtomicInteger();
        searchView.setOnAction(event -> viewActions.incrementAndGet());

        assertEquals(true, searchView.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchView.executeAccessibleAction(AccessibleAction.SET_TEXT, "query");
        assertEquals("query", searchView.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(searchView.getEditor(), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertEquals(2, searchView.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, searchView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        searchView.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertEquals(false, searchView.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchView.executeAccessibleAction(AccessibleAction.EXPAND);
        assertEquals(true, searchView.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        searchView.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, viewActions.get());
    }

    /// Verifies that structural containers expose indexed accessibility content.
    @Test
    void structuralContainersExposeAccessibleCollections() {
        Label surfaceContent = new Label("Surface");
        Label surfaceExtra = new Label("Extra");
        M3Surface surface = new M3Surface(surfaceContent);

        assertEquals(surfaceContent, surface.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(1, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(surfaceContent, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        surface.getContent().add(surfaceExtra);
        assertNull(surface.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(2, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(surfaceExtra, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertNull(surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, -1));

        Label badgeContent = new Label("Inbox");
        M3Badge badge = new M3Badge("3");
        M3BadgedBox badgedBox = new M3BadgedBox(badgeContent, badge);

        assertEquals(badgeContent, badgedBox.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(2, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(badgeContent, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(badge, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        badgedBox.setContent(null);
        assertNull(badgedBox.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(1, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(badge, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        Label navigation = new Label("Menu");
        Label search = new Label("Search");
        Label more = new Label("More");
        M3TopAppBar topAppBar = new M3TopAppBar("Inbox", navigation, search, more);

        assertEquals("Inbox", topAppBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals("Inbox", topAppBar.getAccessibleText());
        assertEquals(3, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(navigation, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(search, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(more, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
        topAppBar.setTitle("Archive");
        topAppBar.setNavigation(null);
        assertEquals("Archive", topAppBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals("Archive", topAppBar.getAccessibleText());
        assertEquals(2, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(search, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        Label bottomAction = new Label("Search");
        Label floatingAction = new Label("Create");
        M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                M3BottomAppBarFloatingActionAlignment.END,
                floatingAction,
                bottomAction
        );

        assertEquals(2, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(bottomAction, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(floatingAction, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        bottomAppBar.setFloatingAction(null);
        assertEquals(1, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertNull(bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
    }

    /// Verifies that slot and container controls report the currently focused accessible child.
    @Test
    void slotContainersReportCurrentFocusedAccessibleChild() {
        runOnFxThread(() -> {
            M3Button topNavigation = new M3Button("Menu");
            M3Button topSearch = new M3Button("Search");
            M3Button topAccount = new M3Button("Account");
            M3TopAppBar topAppBar = new M3TopAppBar("Inbox", topNavigation, topSearch, topAccount);

            M3Button bottomSearch = new M3Button("Search");
            M3Button bottomMore = new M3Button("More");
            M3Button bottomCreate = new M3Button("Create");
            M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    bottomCreate,
                    bottomSearch,
                    bottomMore
            );

            M3Button bannerDismiss = new M3Button("Dismiss");
            M3Button bannerAction = new M3Button("Action");
            M3Banner banner = new M3Banner("Message", bannerDismiss, bannerAction);

            M3Button surfacePrimary = new M3Button("Primary");
            M3Button surfaceSecondary = new M3Button("Secondary");
            M3Surface surface = new M3Surface(surfacePrimary, surfaceSecondary);

            M3Button badgedContent = new M3Button("Inbox");
            M3Badge badge = new M3Badge("3");
            badge.setFocusTraversable(true);
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, badge);

            M3Button rowContent = new M3Button("Row content");
            M3Button rowTrailing = new M3Button("Row trailing");
            M3FormRow formRow = new M3FormRow("Name", "", rowContent, rowTrailing);

            M3Button paneFirst = new M3Button("Pane first");
            M3Button paneSecond = new M3Button("Pane second");
            M3FormPane formPane = new M3FormPane(paneFirst, paneSecond);

            M3Button sectionFirst = new M3Button("Section first");
            M3Button sectionSecond = new M3Button("Section second");
            M3FormSection formSection = new M3FormSection("Settings", sectionFirst, sectionSecond);

            M3Button groupFirst = new M3Button("Group first");
            M3Button groupSecond = new M3Button("Group second");
            M3ButtonGroup buttonGroup = new M3ButtonGroup(groupFirst, groupSecond);

            M3SplitButton splitButton = new M3SplitButton("Create");

            M3Button sideContent = new M3Button("Side content");
            M3Button sideAction = new M3Button("Side action");
            M3SideSheet sideSheet = new M3SideSheet("Side", sideContent, sideAction);

            M3Button bottomContent = new M3Button("Bottom content");
            M3Button bottomAction = new M3Button("Bottom action");
            M3BottomSheet bottomSheet = new M3BottomSheet("Bottom", bottomContent, bottomAction);

            VBox root = new VBox(
                    8.0,
                    topAppBar,
                    bottomAppBar,
                    banner,
                    surface,
                    badgedBox,
                    formRow,
                    formPane,
                    formSection,
                    buttonGroup,
                    splitButton,
                    sideSheet,
                    bottomSheet
            );
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 760.0, 960.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                topAccount.requestFocus();
                assertTrue(topAccount.isFocused());
                assertSame(topAccount, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomCreate.requestFocus();
                assertTrue(bottomCreate.isFocused());
                assertSame(bottomCreate, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bannerAction.requestFocus();
                assertTrue(bannerAction.isFocused());
                assertSame(bannerAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surfaceSecondary.requestFocus();
                assertTrue(surfaceSecondary.isFocused());
                assertSame(surfaceSecondary, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                badge.requestFocus();
                assertTrue(badge.isFocused());
                assertSame(badge, badgedBox.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                rowTrailing.requestFocus();
                assertTrue(rowTrailing.isFocused());
                assertSame(rowTrailing, formRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                paneSecond.requestFocus();
                assertTrue(paneSecond.isFocused());
                assertSame(paneSecond, formPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sectionSecond.requestFocus();
                assertTrue(sectionSecond.isFocused());
                assertSame(sectionSecond, formSection.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                groupSecond.requestFocus();
                assertTrue(groupSecond.isFocused());
                assertSame(groupSecond, buttonGroup.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                splitButton.getMenuButton().requestFocus();
                assertTrue(splitButton.getMenuButton().isFocused());
                assertSame(splitButton.getMenuButton(),
                        splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sideAction.requestFocus();
                assertTrue(sideAction.isFocused());
                assertSame(sideAction, sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomAction.requestFocus();
                assertTrue(bottomAction.isFocused());
                assertSame(bottomAction, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that composite controls actively notify when their accessible focus child changes.
    @Test
    void accessibleFocusNotifierReportsSceneFocusChanges() {
        runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Surface surface = new M3Surface(first, second);
            M3Button outside = new M3Button("Outside");
            AtomicInteger notifications = new AtomicInteger();
            M3AccessibleFocusNotifier notifier = new M3AccessibleFocusNotifier(
                    surface,
                    () -> M3Accessible.currentFocusTarget(surface, surface.getContent()),
                    notifications::incrementAndGet
            );
            Stage stage = new Stage();
            try {
                notifier.start();
                VBox root = new VBox(8.0, surface, outside);
                Scene scene = new Scene(root, 320.0, 160.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                notifier.refresh();
                notifications.set(0);

                first.requestFocus();

                assertTrue(first.isFocused());
                assertEquals(1, notifications.get());

                first.requestFocus();

                assertEquals(1, notifications.get());

                second.requestFocus();

                assertTrue(second.isFocused());
                assertEquals(2, notifications.get());

                outside.requestFocus();

                assertTrue(outside.isFocused());
                assertEquals(3, notifications.get());

                notifier.stop();
                first.requestFocus();

                assertTrue(first.isFocused());
                assertEquals(3, notifications.get());
            } finally {
                notifier.stop();
                stage.close();
            }
        });
    }

    /// Verifies that custom controls expose stable accessibility roles.
    @Test
    void controlsExposeAccessibilityRoles() {
        M3Badge badge = new M3Badge(1234);
        M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
        M3Card passiveCard = new M3Card(new Label("Card"));
        M3Card actionCard = new M3Card(new Label("Action"));
        actionCard.setOnAction(event -> {
        });

        assertEquals(AccessibleRole.BUTTON, new M3Button().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3ButtonGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3SplitButton().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3FabMenu().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3IconButton().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3FloatingActionButton().getAccessibleRole());
        assertEquals(AccessibleRole.CHECK_BOX, new M3CheckBox().getAccessibleRole());
        assertEquals(AccessibleRole.RADIO_BUTTON, new M3RadioButton().getAccessibleRole());
        assertEquals(AccessibleRole.CHECK_BOX, new M3Switch().getAccessibleRole());
        assertEquals(AccessibleRole.SLIDER, new M3Slider().getAccessibleRole());
        assertEquals(AccessibleRole.PROGRESS_INDICATOR, new M3ProgressBar().getAccessibleRole());
        assertEquals(AccessibleRole.PROGRESS_INDICATOR, new M3ProgressIndicator().getAccessibleRole());
        assertEquals(AccessibleRole.PROGRESS_INDICATOR, new M3LoadingIndicator().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT_FIELD, new M3TextField().getAccessibleRole());
        assertEquals(AccessibleRole.PASSWORD_FIELD, new M3PasswordField().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT_AREA, new M3TextArea().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3TextInputLayout().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3Text("Text").getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3Icon("info").getAccessibleRole());
        assertEquals(AccessibleRole.IMAGE_VIEW, new M3Avatar("A").getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3BadgedBox().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, badge.getAccessibleRole());
        assertEquals("123+", badge.getAccessibleText());
        badge.setMaxCharacterCount(2);
        assertEquals("12+", badge.getAccessibleText());
        assertEquals(AccessibleRole.NODE, new M3Divider().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3Surface().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3FormPane().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3FormSection().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3FormRow().getAccessibleRole());
        assertEquals(AccessibleRole.DIALOG, new M3DialogPane().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, passiveCard.getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, actionCard.getAccessibleRole());
        assertTrue(actionCard.isFocusTraversable());
        assertEquals(AccessibleRole.PARENT, new M3Banner().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, snackbar.getAccessibleRole());
        assertEquals("Saved Undo", snackbar.getAccessibleText());
        assertEquals(AccessibleRole.PARENT, new M3SnackbarHost().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3SideSheet().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3BottomSheet().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3Scrim().getAccessibleRole());
        assertEquals("Dismiss", new M3Scrim().getAccessibleText());
        assertEquals(AccessibleRole.LIST_VIEW, new M3Carousel().getAccessibleRole());
        assertEquals(AccessibleRole.MENU, new M3Menu().getAccessibleRole());
        assertEquals(AccessibleRole.MENU_BUTTON, new M3MenuButton().getAccessibleRole());
        assertEquals(AccessibleRole.MENU_ITEM, new M3MenuItem().getAccessibleRole());
        assertEquals(AccessibleRole.MENU_ITEM, new M3SubMenuItem().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3MenuSectionHeader().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3SearchBar().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3SearchView().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3ListPane().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3ListView<>().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_ITEM, new M3ListViewCell<>(new M3ListView<>()).getAccessibleRole());
        assertEquals(AccessibleRole.LIST_ITEM, new M3ListItem().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3ListSectionHeader().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3ChipGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOGGLE_BUTTON, new M3Chip().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3IconToggleButtonGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOGGLE_BUTTON, new M3IconToggleButton().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3SegmentedButtonGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOGGLE_BUTTON, new M3SegmentedButton().getAccessibleRole());
        assertEquals(AccessibleRole.TAB_PANE, new M3TabBar().getAccessibleRole());
        assertEquals(AccessibleRole.TAB_ITEM, new M3Tab().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3TopAppBar().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3BottomAppBar().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3NavigationBar().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3NavigationRail().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3NavigationDrawer().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3NavigationItem().getAccessibleRole());
    }

    /// Verifies that controls expose their default styles through user-agent stylesheets.
    @Test
    void controlsExposeUserAgentStylesheets() {
        assertUserAgentStylesheet(new M3Button(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3ButtonGroup(), "/styles/controls/button-group.css");
        assertUserAgentStylesheet(new M3SplitButton(), "/styles/controls/split-button.css");
        assertUserAgentStylesheet(new M3FabMenu(), "/styles/controls/fab-menu.css");
        assertUserAgentStylesheet(new M3IconButton(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3IconToggleButton(), "/styles/controls/icon-toggle-button.css");
        assertUserAgentStylesheet(new M3IconToggleButtonGroup(), "/styles/controls/icon-toggle-button.css");
        assertUserAgentStylesheet(new M3FloatingActionButton(), "/styles/controls/floating-action-button.css");
        assertUserAgentStylesheet(new M3TextField(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3PasswordField(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3TextArea(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3TextInputLayout(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3DatePicker(), "/styles/controls/date-picker.css");
        assertUserAgentStylesheet(new M3DatePickerField(), "/styles/controls/picker-field.css");
        assertUserAgentStylesheet(new M3DateRangePicker(), "/styles/controls/date-picker.css");
        assertUserAgentStylesheet(new M3DateRangePickerField(), "/styles/controls/picker-field.css");
        assertUserAgentStylesheet(new M3TimePicker(), "/styles/controls/time-picker.css");
        assertUserAgentStylesheet(new M3TimePickerField(), "/styles/controls/picker-field.css");
        assertUserAgentStylesheet(new M3Avatar(), "/styles/controls/avatar.css");
        assertUserAgentStylesheet(new M3DisclosureIcon(), "/styles/controls/disclosure-icon.css");
        assertUserAgentStylesheet(new M3Icon(), "/styles/controls/icon.css");
        assertUserAgentStylesheet(new M3Text(), "/styles/controls/text.css");
        assertUserAgentStylesheet(new M3Surface(), "/styles/controls/surface.css");
        assertUserAgentStylesheet(new M3FormPane(), "/styles/controls/form.css");
        assertUserAgentStylesheet(new M3FormSection(), "/styles/controls/form.css");
        assertUserAgentStylesheet(new M3FormRow(), "/styles/controls/form.css");
        assertUserAgentStylesheet(new M3ValidationSummary(), "/styles/controls/validation-summary.css");
        assertUserAgentStylesheet(new M3BadgedBox(), "/styles/controls/badge.css");
        assertUserAgentStylesheet(new M3Menu(), "/styles/controls/menu.css");
        assertUserAgentStylesheet(new M3MenuItem(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3SubMenuItem(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3MenuSectionHeader(), "/styles/controls/menu.css");
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
        assertUserAgentStylesheet(new M3LoadingIndicator(), "/styles/controls/loading-indicator.css");
        assertUserAgentStylesheet(new M3Divider(), "/styles/controls/divider.css");
        assertUserAgentStylesheet(new M3Badge(), "/styles/controls/badge.css");
        assertUserAgentStylesheet(new M3TopAppBar(), "/styles/controls/top-app-bar.css");
        assertUserAgentStylesheet(new M3BottomAppBar(), "/styles/controls/bottom-app-bar.css");
        assertUserAgentStylesheet(new M3NavigationBar(), "/styles/controls/navigation-bar.css");
        assertUserAgentStylesheet(new M3NavigationRail(), "/styles/controls/navigation-rail.css");
        assertUserAgentStylesheet(new M3NavigationDrawer(), "/styles/controls/navigation-drawer.css");
        assertUserAgentStylesheet(new M3NavigationDrawerGroup(), "/styles/controls/navigation-drawer-group.css");
        assertUserAgentStylesheet(new M3NavigationItem(), "/styles/controls/navigation-bar.css");
        assertUserAgentStylesheet(new M3ListPane(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListView<>(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListItem(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListSectionHeader(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3Card(), "/styles/controls/card.css");
        assertUserAgentStylesheet(new M3DialogPane(), "/styles/controls/dialog.css");
        assertUserAgentStylesheet(new M3Carousel(), "/styles/controls/carousel.css");
        assertUserAgentStylesheet(new M3Banner(), "/styles/controls/banner.css");
        assertUserAgentStylesheet(new M3Snackbar(), "/styles/controls/snackbar.css");
        assertUserAgentStylesheet(new M3SnackbarHost(), "/styles/controls/snackbar.css");
    }

    /// Verifies that Material scroll styling can be applied to JavaFX scroll panes.
    @Test
    void scrollPaneMaterialStyleAppliesScrollbarColors() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        M3ScrollPanes.style(scrollPane);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " -monet-surface-tint: rgb(51,52,53);");
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();

        ScrollBar scrollBar = lookupScrollBar(scrollPane, Orientation.VERTICAL);
        Region thumb = lookupRegion(scrollBar, ".thumb");
        Region track = lookupRegion(scrollBar, ".track");

        assertTrue(scrollPane.getStyleClass().contains(M3ScrollPanes.STYLE_CLASS));
        assertEquals(16.0, scrollBar.prefWidth(-1.0), 0.0001);
        assertRegionFill(track, Color.TRANSPARENT);
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.48, thumb.getOpacity(), 0.0001);

        scrollBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.64, thumb.getOpacity(), 0.0001);

        scrollBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        root.applyCss();
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.78, thumb.getOpacity(), 0.0001);
    }

    /// Verifies that Material scroll styling can be applied to standalone JavaFX scroll bars.
    @Test
    void standaloneScrollBarMaterialStyleAppliesScrollbarColors() {
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        M3ScrollPanes.style(scrollBar);
        StackPane root = new StackPane(scrollBar);
        Scene scene = new Scene(root, 80.0, 160.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " -monet-surface-tint: rgb(51,52,53);");
        root.applyCss();
        root.resize(80.0, 160.0);
        root.layout();

        Region thumb = lookupRegion(scrollBar, ".thumb");

        assertTrue(scrollBar.getStyleClass().contains(M3ScrollPanes.SCROLL_BAR_STYLE_CLASS));
        assertEquals(16.0, scrollBar.prefWidth(-1.0), 0.0001);
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.48, thumb.getOpacity(), 0.0001);
    }

    /// Verifies that Material smooth scrolling can be enabled for JavaFX scroll panes.
    @Test
    void scrollPaneSmoothScrollingAnimatesWheelScroll() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setAnimationsEnabled(scrollPane, true);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
        scrollPane.fireEvent(event);

        assertTrue(M3ScrollPanes.isSmoothScrollingEnabled(scrollPane));
        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPanes.disableSmoothScrolling(scrollPane);
        M3MotionSettings.clearAnimationsEnabled(scrollPane);
        assertFalse(M3ScrollPanes.isSmoothScrollingEnabled(scrollPane));
    }

    /// Verifies that a running smooth scroll settles when animations are disabled at runtime.
    @Test
    void scrollPaneSmoothScrollingSettlesWhenAnimationsAreDisabledAtRuntime() {
        runOnFxThreadAndWait(() -> {
            Region content = new Region();
            content.setPrefSize(160.0, 480.0);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 180.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(180.0, 140.0);
            root.layout();
            M3ScrollPanes.enableSmoothScrolling(scrollPane);
            M3MotionSettings.setAnimationsEnabled(scrollPane, true);
            try {
                ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(event);

                assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
                assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

                M3MotionSettings.setAnimationsEnabled(scrollPane, false);

                assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            } finally {
                M3ScrollPanes.disableSmoothScrolling(scrollPane);
                M3MotionSettings.clearAnimationsEnabled(scrollPane);
            }
        });
    }

    /// Verifies that disabled animation settings make smooth scrolling finish synchronously.
    @Test
    void scrollPaneSmoothScrollingHonorsDisabledAnimations() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setAnimationsEnabled(scrollPane, false);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());

        M3ScrollPanes.disableSmoothScrolling(scrollPane);
        M3MotionSettings.clearAnimationsEnabled(scrollPane);
    }

    /// Verifies that vertical wheel input scrolls horizontally when only the horizontal axis can scroll.
    @Test
    void scrollPaneSmoothScrollingMapsWheelToHorizontalOnlyContent() {
        Region content = new Region();
        content.setPrefSize(480.0, 80.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setAnimationsEnabled(scrollPane, false);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPanes.disableSmoothScrolling(scrollPane);
        M3MotionSettings.clearAnimationsEnabled(scrollPane);
    }

    /// Verifies that direct touch scroll events are left to JavaFX's native panning behavior.
    @Test
    void scrollPaneSmoothScrollingIgnoresDirectScrollEvents() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPanes.enableSmoothScrolling(scrollPane);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0, true);
        scrollPane.fireEvent(event);

        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPanes.disableSmoothScrolling(scrollPane);
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

    /// Returns high-contrast color tokens used by snapshot-based visual tests.
    private static String visualTestColors() {
        return "-m3-color-primary: rgb(84, 50, 185); "
                + "-m3-color-on-primary: white; "
                + "-m3-color-secondary-container: rgb(222, 214, 250); "
                + "-m3-color-on-secondary-container: rgb(40, 27, 92); "
                + "-m3-color-outline: rgb(95, 91, 105); "
                + "-m3-color-surface-container-low: rgb(247, 242, 250); "
                + "-m3-color-surface-container-high: rgb(236, 230, 240); "
                + "-m3-color-surface-container-highest: rgb(228, 221, 234); "
                + "-m3-color-surface-container: rgb(243, 237, 247); "
                + "-m3-color-surface: white; "
                + "-m3-color-outline-variant: rgb(202, 196, 208); "
                + "-m3-color-primary-container: rgb(226, 221, 255); "
                + "-m3-color-on-primary-container: rgb(36, 14, 110); "
                + "-m3-color-tertiary-container: rgb(255, 216, 228); "
                + "-m3-color-on-tertiary-container: rgb(95, 17, 48); "
                + "-m3-color-on-surface: rgb(30, 28, 32); "
                + "-m3-color-on-surface-variant: rgb(73, 69, 79); "
                + "-m3-color-inverse-surface: rgb(49, 48, 51); "
                + "-m3-color-inverse-on-surface: rgb(244, 239, 244); "
                + "-m3-color-inverse-primary: rgb(207, 189, 255); "
                + "-m3-color-error: rgb(186, 26, 26); "
                + "-m3-color-on-error: white; "
                + "-m3-color-error-container: rgb(255, 218, 214); "
                + "-m3-color-on-error-container: rgb(65, 0, 2);";
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

    /// Verifies that a node currently has a drop shadow effect.
    private static DropShadow assertDropShadow(Node node) {
        return assertInstanceOf(DropShadow.class, node.getEffect());
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(Node node, String selector) {
        Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
    }

    /// Returns a private skin timeline used by animation-focused tests.
    private static Timeline skinTimeline(Skin<?> skin, String fieldName) {
        return reflectedTimeline(skin, fieldName);
    }

    /// Returns a private control timeline used by animation-focused tests.
    private static Timeline controlTimeline(Object control, String fieldName) {
        return reflectedTimeline(control, fieldName);
    }

    /// Runs a test action on the JavaFX application thread and waits for completion.
    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
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
        @Nullable Throwable throwable = failure.get();
        if (throwable != null) {
            throw new AssertionError(throwable);
        }
    }

    /// Returns a private double property from a test target.
    private static DoubleProperty reflectedDoubleProperty(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = reflectedField(target.getClass(), fieldName);
            field.setAccessible(true);
            return assertInstanceOf(DoubleProperty.class, field.get(target));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /// Returns a private timeline field from a test target.
    private static Timeline reflectedTimeline(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = reflectedField(target.getClass(), fieldName);
            field.setAccessible(true);
            return assertInstanceOf(Timeline.class, field.get(target));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /// Returns the single numeric end value from a timeline key frame.
    private static double keyFrameEndNumber(Timeline timeline, int keyFrameIndex) {
        var values = timeline.getKeyFrames().get(keyFrameIndex).getValues();
        assertEquals(1, values.size());
        Object endValue = values.iterator().next().getEndValue();
        return assertInstanceOf(Number.class, endValue).doubleValue();
    }

    /// Returns a declared field from a class or one of its superclasses.
    private static java.lang.reflect.Field reflectedField(Class<?> type, String fieldName) throws NoSuchFieldException {
        @Nullable Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /// Presses a button base and advances the shared pressed-state animation to its pressed frame.
    private static void pressButtonAndJumpToPressedFrame(ButtonBase button) {
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        Timeline animation = skinTimeline(button.getSkin(), "animation");
        animation.jumpTo(Duration.millis(50.0));
        animation.stop();
    }

    /// Returns the current top outline gap generated for a floating text input label.
    private static double outlineNotchGap(Path outline) {
        if (outline.getElements().size() < 3) {
            return 0.0;
        }
        var notchStart = assertInstanceOf(javafx.scene.shape.LineTo.class, outline.getElements().get(1));
        var notchEnd = assertInstanceOf(javafx.scene.shape.MoveTo.class, outline.getElements().get(2));
        return notchEnd.getX() - notchStart.getX();
    }

    /// Moves all navigation indicator timelines to the same rendered frame.
    private static void jumpToNavigationIndicatorFrame(Duration frameTime, Timeline... timelines) {
        for (Timeline timeline : timelines) {
            timeline.jumpTo(frameTime);
        }
    }

    /// Stops all supplied timelines.
    private static void stopTimelines(Timeline... timelines) {
        for (Timeline timeline : timelines) {
            timeline.stop();
        }
    }

    /// Verifies that a value is between two exclusive bounds.
    private static void assertBetween(double actual, double lowerBound, double upperBound, String description) {
        assertTrue(actual > lowerBound && actual < upperBound,
                () -> description + ": actual=" + actual
                        + ", lowerBound=" + lowerBound
                        + ", upperBound=" + upperBound);
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

    /// Returns a scroll bar with the requested orientation from a parent node.
    private static ScrollBar lookupScrollBar(Parent parent, Orientation orientation) {
        for (Node node : parent.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == orientation) {
                return scrollBar;
            }
        }
        throw new AssertionError("Missing " + orientation + " scroll bar below " + parent);
    }

    /// Returns the first point in a path made from move and line elements.
    private static Point2D firstPathPoint(Path path) {
        assertFalse(path.getElements().isEmpty());
        return pathElementPoint(path.getElements().get(0));
    }

    /// Returns the last point in a path made from move and line elements.
    private static Point2D lastPathPoint(Path path) {
        assertFalse(path.getElements().isEmpty());
        return pathElementPoint(path.getElements().get(path.getElements().size() - 1));
    }

    /// Returns the point coordinates carried by a sampled path element.
    private static Point2D pathElementPoint(PathElement element) {
        if (element instanceof MoveTo moveTo) {
            return new Point2D(moveTo.getX(), moveTo.getY());
        }
        if (element instanceof LineTo lineTo) {
            return new Point2D(lineTo.getX(), lineTo.getY());
        }
        throw new AssertionError("Unsupported path element: " + element);
    }

    /// Returns the radio indicator region.
    private static Region radioIndicator(M3RadioButton radioButton) {
        Node radio = radioButton.lookup(".radio");
        assertInstanceOf(Region.class, radio);
        return (Region) radio;
    }

    /// Verifies that a right-to-left selection control paints its indicator after its text.
    private static void assertSelectionIndicatorAfterLabel(ButtonBase control) {
        Region indicator = lookupRegion(control, ".m3-selection-indicator");
        Labeled label = assertInstanceOf(Labeled.class, control.lookup(".m3-selection-label"));
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
        assertTrue(indicatorBounds.getMinX() > labelBounds.getMaxX(),
                () -> "indicatorBounds=" + indicatorBounds + ", labelBounds=" + labelBounds);
    }

    /// Returns the radio indicator ring shape.
    private static Shape radioRing(M3RadioButton radioButton) {
        return lookupShape(radioButton, ".ring");
    }

    /// Returns the radio indicator dot shape.
    private static Shape radioDot(M3RadioButton radioButton) {
        return lookupShape(radioButton, ".dot");
    }

    /// Verifies the first background fill for a region.
    private static void assertRegionFill(Region region, Color expectedFill) {
        assertEquals(1, region.getBackground().getFills().size());
        assertEquals(expectedFill, region.getBackground().getFills().get(0).getFill());
    }

    /// Verifies the fill color for a shape.
    private static void assertShapeFill(Shape shape, Color expectedFill) {
        assertEquals(expectedFill, shape.getFill());
    }

    /// Verifies that a region background resolved to a concrete color.
    private static void assertResolvedBackgroundFill(Region region, String description) {
        assertTrue(region.getBackground() != null, () -> description + " has no background");
        assertFalse(region.getBackground().getFills().isEmpty(), () -> description + " has no background fills");
        assertInstanceOf(
                Color.class,
                region.getBackground().getFills().get(0).getFill(),
                () -> description + " background fill did not resolve to a color"
        );
    }

    /// Verifies that a list item headline resolved to a concrete color.
    private static void assertResolvedListItemTextFill(M3ListItem item) {
        Node headline = item.lookup(".m3-list-item-headline");
        Label label = assertInstanceOf(Label.class, headline);
        assertInstanceOf(Color.class, label.getTextFill());
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

    /// Returns the list item selected container region.
    private static javafx.scene.layout.Region listItemSelectionContainer(M3ListItem listItem) {
        javafx.scene.Node container = listItem.lookup(".m3-list-item-selection-container");
        assertInstanceOf(javafx.scene.layout.Region.class, container);
        return (javafx.scene.layout.Region) container;
    }

    /// Returns the segmented button selected container region.
    private static javafx.scene.layout.Region segmentedButtonSelectionContainer(M3SegmentedButton button) {
        javafx.scene.Node container = button.lookup("." + M3SegmentedButtonSkin.SELECTION_CONTAINER_STYLE_CLASS);
        assertInstanceOf(javafx.scene.layout.Region.class, container);
        return (javafx.scene.layout.Region) container;
    }

    /// Verifies that a badged box badge is anchored to logical end and top of its content.
    private static void assertBadgedBoxBadgeAnchoredToLogicalEnd(M3BadgedBox badgedBox, boolean rightToLeft) {
        Node content = assertInstanceOf(Node.class, badgedBox.getContent());
        M3Badge badge = assertInstanceOf(M3Badge.class, badgedBox.getBadge());
        Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
        Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());

        if (rightToLeft) {
            assertTrue(badgeBounds.getCenterX() < contentBounds.getCenterX(),
                    () -> "badgeBounds=" + badgeBounds + ", contentBounds=" + contentBounds);
        } else {
            assertTrue(badgeBounds.getCenterX() > contentBounds.getCenterX(),
                    () -> "badgeBounds=" + badgeBounds + ", contentBounds=" + contentBounds);
        }
        assertTrue(badgeBounds.getCenterY() < contentBounds.getCenterY(),
                () -> "badgeBounds=" + badgeBounds + ", contentBounds=" + contentBounds);
        assertTrue(rangesOverlap(badgeBounds.getMinX(), badgeBounds.getMaxX(), contentBounds.getMinX(), contentBounds.getMaxX()),
                () -> "badge should overlap content horizontally: badgeBounds="
                        + badgeBounds + ", contentBounds=" + contentBounds);
        assertTrue(rangesOverlap(badgeBounds.getMinY(), badgeBounds.getMaxY(), contentBounds.getMinY(), contentBounds.getMaxY()),
                () -> "badge should overlap content vertically: badgeBounds="
                        + badgeBounds + ", contentBounds=" + contentBounds);
    }

    /// Returns whether two one-dimensional intervals overlap.
    private static boolean rangesOverlap(double firstMin, double firstMax, double secondMin, double secondMax) {
        return firstMax > secondMin && firstMin < secondMax;
    }

    /// Verifies that a picker preset column is on the right-to-left logical start side.
    private static void assertPickerPresetMirrorsRightToLeft(
            Node root,
            String presetContentStyleClass,
            String presetListStyleClass,
            Node picker
    ) {
        HBox presetContent = assertInstanceOf(HBox.class, root.lookup("." + presetContentStyleClass));
        VBox presetList = assertInstanceOf(VBox.class, root.lookup("." + presetListStyleClass));

        assertEquals(NodeOrientation.RIGHT_TO_LEFT, root.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, presetContent.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, presetList.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, picker.getEffectiveNodeOrientation());
        assertEquals(Pos.TOP_LEFT, presetContent.getAlignment());
        assertEquals(Pos.TOP_LEFT, presetList.getAlignment());

        Bounds pickerBounds = picker.localToScene(picker.getBoundsInLocal());
        Bounds presetListBounds = presetList.localToScene(presetList.getBoundsInLocal());
        assertTrue(presetListBounds.getMinX() > pickerBounds.getMaxX(),
                () -> "presetListBounds=" + presetListBounds + ", pickerBounds=" + pickerBounds);
    }

    /// Verifies that hidden picker field preset content uses the orientation-aware logical start alignment.
    private static void assertPickerFieldPresetContentUsesLogicalStart(Node presetContentNode, String presetListStyleClass) {
        HBox presetContent = assertInstanceOf(HBox.class, presetContentNode);
        VBox presetList = assertInstanceOf(VBox.class, presetContent.lookup("." + presetListStyleClass));

        assertEquals(NodeOrientation.RIGHT_TO_LEFT, presetContent.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, presetList.getEffectiveNodeOrientation());
        assertEquals(Pos.TOP_LEFT, presetContent.getAlignment());
        assertEquals(Pos.TOP_LEFT, presetList.getAlignment());
    }

    /// Verifies that rendered text nodes do not escape their nearest visual layout boundary.
    private static void assertRenderedTextNodesStayInsideLayout(Node root) {
        visitVisibleNodes(root, node -> {
            if (!(node instanceof Text text) || text.getText().isBlank() || !hasRenderableBounds(text)) {
                return;
            }

            @Nullable Node boundary = nearestVisualBoundary(text, root);
            if (boundary != null) {
                assertNodeInsideAncestor(boundary, text, 1.0);
            }
        });
    }

    /// Verifies that fixed Material touch targets keep their rendered content centered.
    private static void assertFixedTargetControlsKeepCenteredContent(Node root) {
        visitVisibleNodes(root, node -> {
            if (node instanceof M3IconButton button) {
                @Nullable Node graphic = button.getGraphic();
                if (graphic != null && (button.getText() == null || button.getText().isEmpty())) {
                    assertNodeCentersAligned(button, graphic, 1.0);
                }
            } else if (node instanceof M3IconToggleButton button) {
                @Nullable Node graphic = button.getGraphic();
                if (graphic != null) {
                    assertNodeCentersAligned(button, graphic, 1.0);
                }
            } else if (node instanceof ButtonBase button && isFixedTextCell(button)) {
                @Nullable Node textNode = button.lookup(".text");
                if (textNode != null && hasRenderableBounds(textNode)) {
                    assertNodeCentersAligned(button, textNode, 1.0);
                }
            }
        });
    }

    /// Verifies that an already visible supporting row is not in an entry-transition frame.
    private static void assertVisibleSupportingRowIsStable(HBox supportingRow) {
        assertTrue(supportingRow.isVisible());
        assertTrue(supportingRow.isManaged());
        assertEquals(1.0, supportingRow.getOpacity(), 0.0001);
        assertEquals(0.0, supportingRow.getTranslateY(), 0.0001);
    }

    /// Verifies that single-line outlined text inputs keep entered text vertically centered.
    private static void assertOutlinedTextInputsKeepTextCentered(Node root) {
        visitVisibleNodes(root, node -> {
            if (!(node instanceof TextInputControl input)
                    || input instanceof M3TextArea
                    || input instanceof M3PasswordField
                    || !(input instanceof M3TextInput textInput)
                    || textInput.getVariant() != M3TextInputVariant.OUTLINED
                    || input.getText().isBlank()
                    || !hasRenderableBounds(input)) {
                return;
            }

            Text text = renderedTextNode(input, input.getText());
            Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            double inputCenterY = (inputBounds.getMinY() + inputBounds.getMaxY()) / 2.0;
            double textCenterY = (textBounds.getMinY() + textBounds.getMaxY()) / 2.0;

            assertEquals(inputCenterY, textCenterY, 2.0,
                    () -> "outlined input text is vertically misaligned: input="
                            + inputBounds + ", text=" + textBounds);
        });
    }

    /// Visits visible nodes in a rendered hierarchy.
    private static void visitVisibleNodes(Node node, Consumer<Node> visitor) {
        if (!node.isVisible()) {
            return;
        }

        visitor.accept(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                visitVisibleNodes(child, visitor);
            }
        }
    }

    /// Returns whether a node has non-empty rendered layout bounds.
    private static boolean hasRenderableBounds(Node node) {
        Bounds bounds = node.getLayoutBounds();
        return bounds.getWidth() > 0.5 && bounds.getHeight() > 0.5;
    }

    /// Returns the first visible rendered text node with the supplied text.
    private static Text renderedTextNode(Node root, String value) {
        AtomicReference<@Nullable Text> result = new AtomicReference<>();
        visitVisibleNodes(root, node -> {
            if (result.get() == null
                    && node instanceof Text text
                    && value.equals(text.getText())
                    && hasRenderableBounds(text)) {
                result.set(text);
            }
        });

        Text text = result.get();
        if (text == null) {
            throw new AssertionError("No rendered text node found for " + value);
        }
        return text;
    }

    /// Returns the nearest ancestor that should constrain visible text.
    private static @Nullable Node nearestVisualBoundary(Node node, Node root) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent == root
                    || parent instanceof Control
                    || parent instanceof Region
                    || parent.getClip() != null) {
                return parent;
            }
            parent = parent.getParent();
        }
        return root;
    }

    /// Returns whether a button is a fixed-size date or time grid cell.
    private static boolean isFixedTextCell(ButtonBase button) {
        return button.getStyleClass().contains(M3DatePicker.DAY_CELL_STYLE_CLASS)
                || button.getStyleClass().contains(M3TimePicker.CELL_STYLE_CLASS);
    }

    /// Returns a date picker day cell for the supplied date.
    private static ButtonBase dateCellForDate(M3DatePicker picker, LocalDate date) {
        for (Node node : picker.lookupAll("." + M3DatePicker.DAY_CELL_STYLE_CLASS)) {
            if (node instanceof ButtonBase button && date.equals(button.getUserData())) {
                return button;
            }
        }
        throw new AssertionError("No date cell found for " + date);
    }

    /// Verifies that a child node stays inside an ancestor boundary.
    private static void assertNodeInsideAncestor(Node ancestor, Node child, double tolerance) {
        Bounds ancestorBounds = ancestor.localToScene(ancestor.getLayoutBounds());
        Bounds childBounds = child.localToScene(child.getLayoutBounds());
        assertTrue(
                childBounds.getMinX() >= ancestorBounds.getMinX() - tolerance
                        && childBounds.getMaxX() <= ancestorBounds.getMaxX() + tolerance
                        && childBounds.getMinY() >= ancestorBounds.getMinY() - tolerance
                        && childBounds.getMaxY() <= ancestorBounds.getMaxY() + tolerance,
                () -> "child escaped visual boundary: child=" + child
                        + ", ancestor=" + ancestor
                        + ", childBounds=" + childBounds
                        + ", ancestorBounds=" + ancestorBounds
        );
    }

    /// Verifies that a child node's visual center matches its container center.
    private static void assertNodeCentersAligned(Node container, Node child, double tolerance) {
        Bounds containerBounds = container.localToScene(container.getLayoutBounds());
        Bounds childBounds = child.localToScene(child.getLayoutBounds());
        double containerCenterX = (containerBounds.getMinX() + containerBounds.getMaxX()) / 2.0;
        double containerCenterY = (containerBounds.getMinY() + containerBounds.getMaxY()) / 2.0;
        double childCenterX = (childBounds.getMinX() + childBounds.getMaxX()) / 2.0;
        double childCenterY = (childBounds.getMinY() + childBounds.getMaxY()) / 2.0;

        assertEquals(containerCenterX, childCenterX, tolerance,
                () -> "containerBounds=" + containerBounds + ", childBounds=" + childBounds);
        assertEquals(containerCenterY, childCenterY, tolerance,
                () -> "containerBounds=" + containerBounds + ", childBounds=" + childBounds);
    }

    /// Verifies that a rendered child stays inside a rendered parent.
    private static void assertNodeInsideParent(Node parent, Node child) {
        Bounds parentBounds = parent.localToScene(parent.getBoundsInLocal());
        Bounds childBounds = child.localToScene(child.getBoundsInLocal());
        assertTrue(
                childBounds.getMinX() >= parentBounds.getMinX() - 0.0001
                        && childBounds.getMaxX() <= parentBounds.getMaxX() + 0.0001
                        && childBounds.getMinY() >= parentBounds.getMinY() - 0.0001
                        && childBounds.getMaxY() <= parentBounds.getMaxY() + 0.0001,
                () -> "childBounds=" + childBounds + ", parentBounds=" + parentBounds
        );
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

    /// Verifies that a control reports and focuses the expected accessibility focus node.
    private static void assertAccessibleFocus(Node control, Node expectedFocusNode) {
        assertSame(expectedFocusNode, control.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        control.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

        assertTrue(expectedFocusNode.isFocused());
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

    /// Creates a section container used by visual snapshot tests.
    private static VBox visualSection(String title, Node... nodes) {
        M3Text heading = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        FlowPane row = new FlowPane(16.0, 16.0, nodes);
        row.setPrefWrapLength(1010.0);
        row.setStyle("-fx-background-color: -m3-color-surface-container-low; "
                + "-fx-background-radius: 18px; "
                + "-fx-padding: 16px;");
        return new VBox(8.0, heading, row);
    }

    /// Creates a section for interactive-state visual snapshot tests.
    private static VBox interactiveStateSection(String title, Node... samples) {
        M3Text heading = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        FlowPane row = new FlowPane(16.0, 16.0, samples);
        row.setPrefWrapLength(860.0);
        row.setStyle("-fx-background-color: -m3-color-surface-container-low; "
                + "-fx-background-radius: 18px; "
                + "-fx-padding: 16px;");
        return new VBox(8.0, heading, row);
    }

    /// Creates a labeled sample for interactive-state visual snapshot tests.
    private static VBox interactiveStateSample(String title, Node node) {
        Label label = visualLabel(title);
        label.setStyle(label.getStyle() + " -fx-font-size: 11px;");
        VBox sample = new VBox(6.0, label, node);
        sample.setAlignment(Pos.CENTER_LEFT);
        return sample;
    }

    /// Applies a pseudo-class used by visual state snapshots.
    private static void applyPseudoState(Node node, String pseudoClass) {
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass(pseudoClass), true);
    }

    /// Verifies that the state layer overlay resolved to the expected opacity.
    private static void assertStateLayerOpacity(Node node, double expectedOpacity) {
        assertEquals(expectedOpacity, lookupRegion(node, ".m3-state-layer").getOpacity(), 0.0001);
    }

    /// Creates a text label that inherits the gallery's Material color tokens.
    private static Label visualLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: -m3-color-on-surface;");
        return label;
    }

    /// Creates a fixed-size carousel item for behavior tests.
    private static StackPane carouselTestItem(String text) {
        Label label = new Label(text);
        StackPane item = new StackPane(label);
        item.setPrefSize(140.0, 72.0);
        item.setStyle("-fx-background-color: -m3-color-secondary-container; "
                + "-fx-background-radius: 16px; "
                + "-fx-padding: 16px;");
        return item;
    }

    /// Creates a media node used by list item visual snapshot tests.
    private static StackPane visualListMedia(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: -m3-color-on-tertiary-container; -fx-font-weight: 700;");
        StackPane media = new StackPane(label);
        media.setStyle("-fx-background-color: -m3-color-tertiary-container; -fx-background-radius: 4px;");
        return media;
    }

    /// Creates a navigation drawer item for visual snapshot tests.
    private static M3ListItem drawerItem(String text, boolean selected) {
        M3ListItem item = new M3ListItem(text);
        item.setLeading(new M3Icon(text.substring(0, 1)));
        item.setSelected(selected);
        return item;
    }

    /// Returns a rendered pixel from a node snapshot on the FX thread.
    private static Color snapshotPixelOnFxThread(Node node, int x, int y) {
        return snapshotImageOnFxThread(node).getPixelReader().getColor(x, y);
    }

    /// Returns a rendered pixel from a node-local coordinate in a root snapshot.
    private static Color snapshotNodePixel(WritableImage image, Node node, double x, double y) {
        var point = node.localToScene(x, y);
        int pixelX = clampPixelCoordinate((int) Math.round(point.getX()), (int) image.getWidth());
        int pixelY = clampPixelCoordinate((int) Math.round(point.getY()), (int) image.getHeight());
        return image.getPixelReader().getColor(pixelX, pixelY);
    }

    /// Verifies that a rendered region has rounded or square top edge corners.
    private static void assertSnapshotEdgeCorners(
            WritableImage image,
            Region region,
            boolean roundedLeft,
            boolean roundedRight
    ) {
        Color background = Color.WHITE;
        double topY = 2.0;
        double leftDistance = colorDistance(snapshotNodePixel(image, region, 2.0, topY), background);
        double rightDistance =
                colorDistance(snapshotNodePixel(image, region, region.getWidth() - 3.0, topY), background);

        assertEquals(roundedLeft, leftDistance < 0.04,
                () -> "left corner distance=" + leftDistance + ", region=" + region);
        assertEquals(roundedRight, rightDistance < 0.04,
                () -> "right corner distance=" + rightDistance + ", region=" + region);
    }

    /// Clamps a pixel coordinate to a rendered snapshot dimension.
    private static int clampPixelCoordinate(int coordinate, int dimension) {
        return Math.max(0, Math.min(dimension - 1, coordinate));
    }

    /// Returns a rendered image snapshot from a node on the FX thread.
    private static WritableImage snapshotImageOnFxThread(Node node) {
        WritableImage image = new WritableImage(
                (int) Math.ceil(node.getLayoutBounds().getWidth()),
                (int) Math.ceil(node.getLayoutBounds().getHeight())
        );
        node.snapshot(null, image);
        return image;
    }

    /// Resizes a region to its preferred size before taking a detached popup snapshot.
    private static void resizeToPreferredSize(Region region) {
        double width = Math.ceil(region.prefWidth(-1.0));
        double height = Math.ceil(region.prefHeight(width));
        region.resize(width, height);
        region.applyCss();
        region.layout();
    }

    /// Verifies that a rendered snapshot contains enough distinct visible colors.
    private static void assertSnapshotHasColorVariety(WritableImage image, int minimumColorCount) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                int argb = image.getPixelReader().getArgb(x, y);
                if (((argb >>> 24) & 0xff) > 16) {
                    colors.add(quantizedArgb(argb));
                }
            }
        }

        assertTrue(colors.size() >= minimumColorCount,
                () -> "snapshotColorCount=" + colors.size() + ", minimum=" + minimumColorCount);
    }

    /// Verifies that a node's rendered bounds contain pixels that contrast with a reference color.
    private static void assertSnapshotNodeContainsContrast(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        assertSnapshotAreaContainsContrast(
                image,
                (int) Math.floor(bounds.getMinX()),
                (int) Math.floor(bounds.getMinY()),
                (int) Math.ceil(bounds.getMaxX()),
                (int) Math.ceil(bounds.getMaxY()),
                reference,
                minimumDistance,
                node.toString()
        );
    }

    /// Verifies that a node's rendered border band contains pixels that contrast with a reference color.
    private static void assertSnapshotNodeBorderContainsContrast(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        int minX = (int) Math.floor(bounds.getMinX());
        int minY = (int) Math.floor(bounds.getMinY());
        int maxX = (int) Math.ceil(bounds.getMaxX());
        int maxY = (int) Math.ceil(bounds.getMaxY());
        String description = node + " border";

        if (snapshotAreaContainsContrast(image, minX, minY, maxX, minY + 3, reference, minimumDistance)
                || snapshotAreaContainsContrast(image, minX, maxY - 3, maxX, maxY, reference, minimumDistance)
                || snapshotAreaContainsContrast(image, minX, minY, minX + 3, maxY, reference, minimumDistance)
                || snapshotAreaContainsContrast(image, maxX - 3, minY, maxX, maxY, reference, minimumDistance)) {
            return;
        }

        throw new AssertionError("No contrasting border pixels found for " + description);
    }

    /// Verifies that a node snapshot keeps all contrasting pixels away from its rendered edges.
    private static void assertSnapshotNodeEdgesClear(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        int minX = (int) Math.floor(bounds.getMinX());
        int minY = (int) Math.floor(bounds.getMinY());
        int maxX = (int) Math.ceil(bounds.getMaxX());
        int maxY = (int) Math.ceil(bounds.getMaxY());
        int edge = Math.max(1, Math.min(3, Math.min(maxX - minX, maxY - minY) / 6));

        boolean touchesTop = snapshotAreaContainsContrast(image, minX, minY, maxX, minY + edge,
                reference, minimumDistance);
        boolean touchesBottom = snapshotAreaContainsContrast(image, minX, maxY - edge, maxX, maxY,
                reference, minimumDistance);
        boolean touchesLeft = snapshotAreaContainsContrast(image, minX, minY, minX + edge, maxY,
                reference, minimumDistance);
        boolean touchesRight = snapshotAreaContainsContrast(image, maxX - edge, minY, maxX, maxY,
                reference, minimumDistance);

        assertFalse(
                touchesTop || touchesBottom || touchesLeft || touchesRight,
                () -> "Contrasting pixels reached the edge for " + node + ", bounds=" + bounds
        );
    }

    /// Verifies that a snapshot area contains pixels that contrast with a reference color.
    private static void assertSnapshotAreaContainsContrast(
            WritableImage image,
            int minX,
            int minY,
            int maxX,
            int maxY,
            Color reference,
            double minimumDistance,
            String description
    ) {
        assertTrue(snapshotAreaContainsContrast(image, minX, minY, maxX, maxY, reference, minimumDistance),
                () -> "No contrasting pixels found for " + description);
    }

    /// Returns whether a snapshot area contains pixels that contrast with a reference color.
    private static boolean snapshotAreaContainsContrast(
            WritableImage image,
            int minX,
            int minY,
            int maxX,
            int maxY,
            Color reference,
            double minimumDistance
    ) {
        int startX = Math.max(0, minX);
        int startY = Math.max(0, minY);
        int endX = Math.min((int) image.getWidth(), maxX);
        int endY = Math.min((int) image.getHeight(), maxY);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, reference) >= minimumDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns the centroid of rendered pixels inside a node that contrast with the reference color.
    private static Point2D contrastingPixelCentroid(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        double totalWeight = 0.0;
        double weightedX = 0.0;
        double weightedY = 0.0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                double distance = colorDistance(color, reference);
                if (color.getOpacity() > 0.1 && distance >= minimumDistance) {
                    double weight = color.getOpacity() * Math.min(1.0, distance);
                    totalWeight += weight;
                    weightedX += (x + 0.5) * weight;
                    weightedY += (y + 0.5) * weight;
                }
            }
        }

        assertTrue(totalWeight > 0.0, () -> "No contrasting pixels found for " + node);
        return new Point2D(weightedX / totalWeight, weightedY / totalWeight);
    }

    /// Returns the icon text rendered by a picker header navigation button.
    private static String pickerHeaderNavigationIconText(HBox header, int childIndex) {
        M3IconButton button = assertInstanceOf(M3IconButton.class, header.getChildren().get(childIndex));
        M3Icon icon = assertInstanceOf(M3Icon.class, button.getGraphic());
        return icon.getText();
    }

    /// Writes a rendered snapshot to a build report path for manual visual inspection.
    private static void writeVisualSnapshot(WritableImage image, java.nio.file.Path path) {
        try {
            java.nio.file.Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(toBufferedImage(image), "png", path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Converts a JavaFX image snapshot to a desktop image for report output.
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return bufferedImage;
    }

    /// Returns a quantized ARGB value that keeps color variety checks stable across renderers.
    private static int quantizedArgb(int argb) {
        return argb & 0xf0f0f0f0;
    }

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }

    /// Verifies that a sampled snapshot pixel is close to the expected color.
    private static void assertColorNear(Color actual, Color expected, double maximumDistance, String description) {
        assertTrue(colorDistance(actual, expected) <= maximumDistance,
                () -> description + ": actual=" + actual + ", expected=" + expected);
    }

    /// Creates a button with the requested variant.
    private static M3Button createButton(String text, M3ButtonVariant variant) {
        return new M3Button(text, variant);
    }

    /// Creates a button with the requested variant and action handler.
    private static M3Button createButton(
            String text,
            M3ButtonVariant variant,
            @Nullable javafx.event.EventHandler<ActionEvent> onAction
    ) {
        return new M3Button(text, variant, onAction);
    }

    /// Creates a split button with the requested variant and menu items.
    private static M3SplitButton createSplitButton(String text, M3ButtonVariant variant, Node... items) {
        M3SplitButton button = new M3SplitButton(text, items);
        button.setVariant(variant);
        return button;
    }

    /// Creates a split button with the requested variant, action handler, and menu items.
    private static M3SplitButton createSplitButton(
            String text,
            M3ButtonVariant variant,
            @Nullable javafx.event.EventHandler<ActionEvent> onAction,
            Node... items
    ) {
        M3SplitButton button = createSplitButton(text, variant, items);
        button.setOnAction(onAction);
        return button;
    }

    /// Creates a floating action button with text, variant, and size.
    private static M3FloatingActionButton createFab(
            String text,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        return createFab(text, null, variant, size);
    }

    /// Creates a floating action button with text, graphic content, variant, and size.
    private static M3FloatingActionButton createFab(
            String text,
            @Nullable Node graphic,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(text, graphic);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a floating action button with graphic content, variant, and size.
    private static M3FloatingActionButton createGraphicFab(
            @Nullable Node graphic,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(graphic);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a banner with a leading icon and trailing actions.
    private static M3Banner createBanner(String text, Node icon, Node... actions) {
        M3Banner banner = new M3Banner(text, actions);
        banner.setIcon(Objects.requireNonNull(icon, "icon"));
        return banner;
    }

    /// Creates a text field with the requested variant.
    private static M3TextField createTextField(String text, M3TextInputVariant variant) {
        M3TextField textField = new M3TextField(text);
        textField.setVariant(variant);
        return textField;
    }

    /// Creates a password field with the requested variant.
    private static M3PasswordField createPasswordField(String text, M3TextInputVariant variant) {
        M3PasswordField passwordField = new M3PasswordField(text);
        passwordField.setVariant(variant);
        return passwordField;
    }

    /// Creates a text area with the requested variant.
    private static M3TextArea createTextArea(String text, M3TextInputVariant variant) {
        M3TextArea textArea = new M3TextArea(text);
        textArea.setVariant(variant);
        return textArea;
    }

    /// Creates an avatar with the requested variant.
    private static M3Avatar createAvatar(String text, M3AvatarVariant variant) {
        M3Avatar avatar = new M3Avatar(text);
        avatar.setVariant(variant);
        return avatar;
    }

    /// Creates an avatar with graphic content and the requested variant.
    private static M3Avatar createAvatar(@Nullable Node graphic, M3AvatarVariant variant) {
        M3Avatar avatar = new M3Avatar(graphic);
        avatar.setVariant(variant);
        return avatar;
    }

    /// Creates an icon button with an M3FX icon label.
    private static M3IconButton createIconButton(String iconText) {
        return new M3IconButton(new M3Icon(iconText));
    }

    /// Creates an icon button with an M3FX icon label, size, and color variant.
    private static M3IconButton createIconButton(String iconText, M3IconSize iconSize, M3IconVariant iconVariant) {
        return new M3IconButton(new M3Icon(iconText, iconSize, iconVariant));
    }

    /// Creates a toggle icon button with graphic content, variant, and selected state.
    private static M3IconToggleButton createIconToggleButton(
            @Nullable Node graphic,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        M3IconToggleButton button = new M3IconToggleButton(graphic);
        button.setVariant(variant);
        button.setSelected(selected);
        return button;
    }

    /// Creates a toggle icon button with an M3FX icon label, variant, and selected state.
    private static M3IconToggleButton createIconToggleButton(
            String iconText,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        return createIconToggleButton(new M3Icon(iconText), variant, selected);
    }

    /// Creates a toggle icon button with an M3FX icon label, icon size, icon variant, button variant, and state.
    private static M3IconToggleButton createIconToggleButton(
            String iconText,
            M3IconSize iconSize,
            M3IconVariant iconVariant,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        return createIconToggleButton(new M3Icon(iconText, iconSize, iconVariant), variant, selected);
    }

    /// Creates a chip with the requested variant and selected state.
    private static M3Chip createChip(String text, M3ChipVariant variant, boolean selected) {
        M3Chip chip = new M3Chip(text);
        chip.setVariant(variant);
        chip.setSelected(selected);
        return chip;
    }

    /// Creates a chip with the requested variant.
    private static M3Chip createChip(String text, M3ChipVariant variant) {
        return createChip(text, variant, false);
    }

    /// Creates a chip with graphic content, variant, and selected state.
    private static M3Chip createChip(String text, @Nullable Node graphic, M3ChipVariant variant, boolean selected) {
        M3Chip chip = new M3Chip(text, graphic);
        chip.setVariant(variant);
        chip.setSelected(selected);
        return chip;
    }

    /// Creates a chip with graphic content and the requested variant.
    private static M3Chip createChip(String text, @Nullable Node graphic, M3ChipVariant variant) {
        return createChip(text, graphic, variant, false);
    }

    /// Creates a segmented button with the requested selected state.
    private static M3SegmentedButton createSegmentedButton(String text, boolean selected) {
        M3SegmentedButton button = new M3SegmentedButton(text);
        button.setSelected(selected);
        return button;
    }

    /// Creates a tab with the requested selected state.
    private static M3Tab createTab(String text, boolean selected) {
        M3Tab tab = new M3Tab(text);
        tab.setSelected(selected);
        return tab;
    }

    /// Creates a checkbox with the requested selected state.
    private static M3CheckBox createCheckBox(String text, boolean selected) {
        M3CheckBox checkBox = new M3CheckBox(text);
        checkBox.setSelected(selected);
        return checkBox;
    }

    /// Creates a radio button with the requested selected state.
    private static M3RadioButton createRadioButton(String text, boolean selected) {
        M3RadioButton radioButton = new M3RadioButton(text);
        radioButton.setSelected(selected);
        return radioButton;
    }

    /// Creates a switch with the requested selected state.
    private static M3Switch createSwitch(String text, boolean selected) {
        M3Switch switchControl = new M3Switch(text);
        switchControl.setSelected(selected);
        return switchControl;
    }

    /// Creates a navigation item with the requested selected state.
    private static M3NavigationItem createNavigationItem(String text, boolean selected) {
        M3NavigationItem item = new M3NavigationItem(text);
        item.setSelected(selected);
        return item;
    }

    /// Creates a navigation item with graphic content and the requested selected state.
    private static M3NavigationItem createNavigationItem(String text, @Nullable Node graphic, boolean selected) {
        M3NavigationItem item = new M3NavigationItem(text, graphic);
        item.setSelected(selected);
        return item;
    }

    /// Creates a navigation item with graphic content, a badge, and the requested selected state.
    private static M3NavigationItem createNavigationItem(
            String text,
            @Nullable Node graphic,
            @Nullable M3Badge badge,
            boolean selected
    ) {
        M3NavigationItem item = new M3NavigationItem(text, graphic, badge);
        item.setSelected(selected);
        return item;
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

    /// Creates an indirect scroll event for scroll behavior tests.
    private static ScrollEvent scrollEvent(Node target, double deltaX, double deltaY) {
        return scrollEvent(target, deltaX, deltaY, false);
    }

    /// Creates a scroll event for scroll behavior tests.
    private static ScrollEvent scrollEvent(Node target, double deltaX, double deltaY, boolean direct) {
        return new ScrollEvent(
                target,
                target,
                ScrollEvent.SCROLL,
                40.0,
                40.0,
                40.0,
                40.0,
                false,
                false,
                false,
                false,
                direct,
                false,
                deltaX,
                deltaY,
                deltaX,
                deltaY,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0.0,
                0,
                null
        );
    }

    /// Returns diagnostic geometry for scroll behavior assertions.
    private static String scrollPaneDebug(ScrollPane scrollPane, Region content, ScrollEvent event) {
        return "viewport=" + scrollPane.getViewportBounds()
                + ", contentBounds=" + content.getBoundsInLocal()
                + ", prefHeight=" + content.prefHeight(-1.0)
                + ", vmin=" + scrollPane.getVmin()
                + ", vmax=" + scrollPane.getVmax()
                + ", vvalue=" + scrollPane.getVvalue()
                + ", deltaY=" + event.getDeltaY()
                + ", textDeltaY=" + event.getTextDeltaY()
                + ", textDeltaYUnits=" + event.getTextDeltaYUnits();
    }

    /// Creates a key event for control behavior tests.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }

    /// Returns whether a JavaFX CSS warning indicates unresolved M3FX color tokens.
    private static boolean isColorTokenCssWarning(LogRecord record) {
        String message = record.getMessage();
        return message != null
                && (message.contains("-m3-color-")
                || message.contains("ClassCastException") && message.contains("-fx-background-color"));
    }

    /// Creates a typed key event for printable-key behavior tests.
    private static KeyEvent keyTypedEvent(String character) {
        return new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED, false, false, false, false);
    }
}
