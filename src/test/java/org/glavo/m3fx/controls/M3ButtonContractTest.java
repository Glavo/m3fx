// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies button actions, state styling, shapes, and elevation.
@NotNullByDefault
final class M3ButtonContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that button variants update their style classes.
    @Test
    void buttonVariantUpdatesStyleClass() {
        AtomicInteger actions = new AtomicInteger();
        M3Button button = createButton("Button", M3ButtonVariant.FILLED, event -> actions.incrementAndGet());

        assertTrue(button.getStyleClass().contains(BUTTON_STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3ButtonVariant.FILLED.styleClass()));

        button.setVariant(M3ButtonVariant.OUTLINED);
        button.fire();

        assertEquals(1, actions.get());
        assertTrue(button.getStyleClass().contains(M3ButtonVariant.OUTLINED.styleClass()));
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

    /// Verifies that action-like Material controls use their full layout bounds as pointer hit targets.
    @Test
    void materialActionControlsPickFullLayoutBounds() {
        ButtonBase[] controls = {
                new M3Button("Button"),
                new M3IconButton(new M3Icon("I")),
                new M3FloatingActionButton(),
                new M3IconToggleButton("T"),
                new M3AssistChip("Chip"),
                new M3SegmentedButton("Segment"),
                new M3Tab("Tab"),
                new M3NavigationItem("Navigation"),
                new M3CheckBox("Check"),
                new M3RadioButton("Radio"),
                new M3Switch("Switch")
        };

        for (ButtonBase control : controls) {
            assertButtonBaseFullBoundsClick(control, control.getClass().getSimpleName());
        }

        assertListItemFullBoundsClick(new M3ListItem("List item"));
    }

    /// Verifies that grouped Material action controls preserve child hit targets near connected edges.
    @Test
    void groupedActionControlsPickChildFullLayoutBounds() {
        M3Button buttonFirst = new M3Button("First");
        M3Button buttonSecond = new M3Button("Second");
        M3Button buttonThird = new M3Button("Third");
        M3ButtonGroup buttonGroup = buttonGroup(buttonFirst, buttonSecond, buttonThird);
        assertGroupedButtonBaseFullBoundsClick(
                buttonGroup,
                buttonThird,
                "M3ButtonGroup child"
        );

        M3IconToggleButton iconFirst = new M3IconToggleButton("F");
        M3IconToggleButton iconSecond = new M3IconToggleButton("S");
        M3IconToggleButton iconThird = new M3IconToggleButton("T");
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond, iconThird);
        assertGroupedButtonBaseFullBoundsClick(
                iconGroup,
                iconThird,
                "M3IconToggleButtonGroup child"
        );
        assertEquals(iconThird, iconGroup.getSelectedButton());

        M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
        M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup segmentedGroup =
                segmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);
        assertGroupedButtonBaseFullBoundsClick(
                segmentedGroup,
                segmentThird,
                "M3SegmentedButtonGroup child"
        );
        assertEquals(segmentThird, segmentedGroup.getSelectedButton());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3Tab tabThird = new M3Tab("History");
        M3TabBar tabBar = tabBar(tabFirst, tabSecond, tabThird);
        assertGroupedButtonBaseFullBoundsClick(
                tabBar,
                tabThird,
                "M3TabBar child"
        );
        assertEquals(tabThird, tabBar.getSelectedTab());
    }

    /// Verifies that right-to-left grouped Material action controls preserve child hit targets near connected edges.
    @Test
    void groupedActionControlsPickChildFullLayoutBoundsInRightToLeft() {
        M3Button buttonFirst = new M3Button("First");
        M3Button buttonSecond = new M3Button("Second");
        M3Button buttonThird = new M3Button("Third");
        M3ButtonGroup buttonGroup = buttonGroup(buttonFirst, buttonSecond, buttonThird);
        buttonGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        assertGroupedButtonBaseFullBoundsClick(
                buttonGroup,
                buttonFirst,
                "right-to-left M3ButtonGroup child"
        );

        M3IconToggleButton iconFirst = new M3IconToggleButton("F");
        M3IconToggleButton iconSecond = new M3IconToggleButton("S");
        M3IconToggleButton iconThird = new M3IconToggleButton("T");
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond, iconThird);
        iconGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        assertGroupedButtonBaseFullBoundsClick(
                iconGroup,
                iconFirst,
                "right-to-left M3IconToggleButtonGroup child"
        );
        assertEquals(iconFirst, iconGroup.getSelectedButton());

        M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
        M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup segmentedGroup =
                segmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);
        segmentedGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        assertGroupedButtonBaseFullBoundsClick(
                segmentedGroup,
                segmentFirst,
                "right-to-left M3SegmentedButtonGroup child"
        );
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3Tab tabThird = new M3Tab("History");
        M3TabBar tabBar = tabBar(tabFirst, tabSecond, tabThird);
        tabBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        assertGroupedButtonBaseFullBoundsClick(
                tabBar,
                tabFirst,
                "right-to-left M3TabBar child"
        );
        assertEquals(tabFirst, tabBar.getSelectedTab());
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

    /// Retains paint and feedback nodes across labeled-content rebuilds and subsequent CSS motion changes.
    @Test
    void buttonSkinRetainsLayersAfterLabeledContentChanges() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);
            M3Theme theme = M3Theme.defaultTheme();
            M3ThemeManager.install(scene, theme);
            FxTestUtils.setCssAnimationsEnabled(root, true);
            button.resize(140.0, 40.0);
            button.layout();

            Skin<?> skin = Objects.requireNonNull(button.getSkin(), "button skin");
            Region stateLayer = lookupRegion(button, ".m3-state-layer-container");
            Region overlay = lookupRegion(button, ".m3-state-layer");
            Region containerPaint = lookupRegion(button, ".m3-container-paint");
            try {
                for (Runnable change : List.<Runnable>of(
                        () -> button.setGraphic(new M3Icon("A")),
                        () -> button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY),
                        () -> button.setGraphic(new M3Icon("B")),
                        () -> button.setContentDisplay(ContentDisplay.TEXT_ONLY),
                        () -> button.setText("Updated"),
                        () -> button.setGraphic(null),
                        () -> button.setContentDisplay(ContentDisplay.LEFT)
                )) {
                    change.run();
                    root.applyCss();
                    button.layout();
                    assertSame(skin, button.getSkin());
                    assertSame(button, stateLayer.getParent());
                    assertSame(stateLayer, lookupRegion(button, ".m3-state-layer-container"));
                    assertSame(overlay, lookupRegion(button, ".m3-state-layer"));
                    assertSame(containerPaint, lookupRegion(button, ".m3-container-paint"));
                    assertSame(scene, containerPaint.getScene());
                    assertTrue(containerPaint.isVisible());
                    assertRegionFill(containerPaint, assertInstanceOf(Color.class, button.getContainerColor()));
                }

                button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
                Region ripple = lookupRegion(button, ".m3-ripple");
                assertTrue(ripple.getOpacity() > 0.0);

                FxTestUtils.setCssAnimationsEnabled(root, false);
                assertFalse(button.isAnimationsEnabled());
                assertEquals(0.0, ripple.getOpacity(), 0.0001);
                button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
                button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
                root.applyCss();
                assertEquals(theme.tokens().stateLayerTokens().hoverOpacity(), overlay.getOpacity(), 0.0001);

                button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
                root.applyCss();
                assertEquals(0.0, overlay.getOpacity(), 0.0001);
                FxTestUtils.setCssAnimationsEnabled(root, true);
                assertTrue(button.isAnimationsEnabled());
                button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
                assertTrue(ripple.getOpacity() > 0.0);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, false);
                button.disarm();
                scene.setRoot(new Pane());
            }
        }));
    }

    /// Verifies that button feedback layers use the same resolved shape as the button surface.
    @Test
    void buttonStateLayerUsesResolvedContainerShape() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
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
        FxTestUtils.runOnFxThread(() -> {
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

    /// Verifies that disabled action controls render container and content opacity as separate Material layers.
    @Tier2Test
    @Test
    void disabledActionControlsDoNotCompoundContainerAndContentOpacity() {
        M3Theme theme = M3Theme.defaultTheme();
        Color onSurface = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE);
        Color outlineVariant = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.OUTLINE_VARIANT);
        Color disabledContainer = withOpacity(onSurface, 0.10);

        List<M3Button> buttons = new ArrayList<>();
        List<Region> buttonGraphics = new ArrayList<>();
        for (M3ButtonVariant variant : M3ButtonVariant.values()) {
            Region graphic = new Region();
            graphic.setPrefSize(12.0, 12.0);
            M3Button button = new M3Button(variant.name(), graphic, variant);
            button.setDisable(true);
            buttons.add(button);
            buttonGraphics.add(graphic);
        }

        Region iconGraphic = new Region();
        iconGraphic.setPrefSize(12.0, 12.0);
        M3IconButton iconButton = new M3IconButton(iconGraphic);
        iconButton.setVariant(M3ButtonVariant.FILLED);
        iconButton.setDisable(true);

        Region fabGraphic = new Region();
        fabGraphic.setPrefSize(12.0, 12.0);
        M3FloatingActionButton fab = new M3FloatingActionButton(fabGraphic);
        fab.setDisable(true);

        Region outlinedToggleGraphic = new Region();
        outlinedToggleGraphic.setPrefSize(12.0, 12.0);
        M3IconToggleButton outlinedToggle = new M3IconToggleButton(outlinedToggleGraphic);
        outlinedToggle.setVariant(M3IconToggleButtonVariant.OUTLINED);
        outlinedToggle.setDisable(true);

        Region selectedOutlinedToggleGraphic = new Region();
        selectedOutlinedToggleGraphic.setPrefSize(12.0, 12.0);
        M3IconToggleButton selectedOutlinedToggle = new M3IconToggleButton(selectedOutlinedToggleGraphic);
        selectedOutlinedToggle.setVariant(M3IconToggleButtonVariant.OUTLINED);
        selectedOutlinedToggle.setSelected(true);
        selectedOutlinedToggle.setDisable(true);

        M3Button dynamicallyDisabledElevated = new M3Button("Dynamic", M3ButtonVariant.ELEVATED);

        HBox root = new HBox();
        root.getChildren().addAll(buttons);
        root.getChildren().addAll(
                iconButton,
                fab,
                outlinedToggle,
                selectedOutlinedToggle,
                dynamicallyDisabledElevated
        );
        Scene scene = new Scene(root, 1200.0, 240.0);
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        root.resize(root.prefWidth(-1.0), root.prefHeight(-1.0));
        root.layout();

        for (int index = 0; index < buttons.size(); index++) {
            M3Button button = buttons.get(index);
            M3ButtonVariant variant = button.getVariant();
            assertEquals(1.0, button.getOpacity(), 0.0001, () -> variant + " control opacity");
            assertEquals(onSurface, button.getTextFill(), () -> variant + " disabled content color");
            assertEquals(0.38, buttonGraphics.get(index).getOpacity(), 0.0001,
                    () -> variant + " graphic opacity");
            assertNull(button.getEffect(), () -> variant + " disabled elevation");
            if (variant == M3ButtonVariant.OUTLINED) {
                assertRegionFill(button, Color.TRANSPARENT);
                assertButtonContainerFill(button, Color.TRANSPARENT);
                assertBorderColor(button, outlineVariant);
            } else {
                assertRegionFill(button, Color.TRANSPARENT);
                assertButtonContainerFill(button, disabledContainer);
                assertBorderColor(button, Color.TRANSPARENT);
            }
        }

        assertEquals(1.0, iconButton.getOpacity(), 0.0001);
        assertRegionFill(iconButton, Color.TRANSPARENT);
        assertButtonContainerFill(iconButton, disabledContainer);
        assertEquals(0.38, iconGraphic.getOpacity(), 0.0001);

        assertEquals(1.0, fab.getOpacity(), 0.0001);
        assertRegionFill(fab, disabledContainer);
        assertEquals(0.38, fabGraphic.getOpacity(), 0.0001);
        assertNull(fab.getEffect());

        assertEquals(1.0, outlinedToggle.getOpacity(), 0.0001);
        assertRegionFill(outlinedToggle, Color.TRANSPARENT);
        assertButtonContainerFill(outlinedToggle, Color.TRANSPARENT);
        assertBorderColor(outlinedToggle, outlineVariant);
        assertEquals(0.38, outlinedToggleGraphic.getOpacity(), 0.0001);

        assertEquals(1.0, selectedOutlinedToggle.getOpacity(), 0.0001);
        assertRegionFill(selectedOutlinedToggle, Color.TRANSPARENT);
        assertButtonContainerFill(selectedOutlinedToggle, disabledContainer);
        assertBorderColor(selectedOutlinedToggle, Color.TRANSPARENT);
        assertEquals(0.38, selectedOutlinedToggleGraphic.getOpacity(), 0.0001);

        assertInstanceOf(
                DropShadow.class,
                dynamicallyDisabledElevated.getEffect(),
                "enabled elevated button elevation"
        );
        dynamicallyDisabledElevated.setDisable(true);
        root.applyCss();
        assertNull(dynamicallyDisabledElevated.getEffect(), "dynamically disabled elevated button elevation");
    }

    /// Verifies that only the elevated button variant owns button elevation.
    @Test
    void buttonElevationDoesNotLeakIntoNonElevatedVariants() {
        FxTestUtils.runOnFxThread(() -> {
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
    @Tier2Test
    @Test
    void nonElevatedButtonVariantsDoNotScaleWhenPressed() throws InterruptedException {
        for (M3ButtonVariant variant : List.of(
                M3ButtonVariant.FILLED,
                M3ButtonVariant.TONAL,
                M3ButtonVariant.OUTLINED,
                M3ButtonVariant.TEXT
        )) {
            assertPressedScaleAfterRealPulse(
                    () -> createButton("Button", variant),
                    120.0,
                    40.0,
                    1.0,
                    variant + " button"
            );
        }

        assertPressedScaleAfterRealPulse(
                () -> createButton("Button", M3ButtonVariant.ELEVATED),
                120.0,
                40.0,
                0.98,
                "elevated button"
        );
    }

    /// Verifies that flat labeled button controls do not use depth-style pressed scaling.
    @Tier2Test
    @Test
    void flatLabeledButtonControlsDoNotScaleWhenPressed() throws InterruptedException {
        assertPressedScaleAfterRealPulse(() -> new M3AssistChip("Chip"), 120.0, 40.0, 1.0, "M3Chip");
        assertPressedScaleAfterRealPulse(() -> new M3SegmentedButton("Segment"), 120.0, 40.0, 1.0,
                "M3SegmentedButton");
        assertPressedScaleAfterRealPulse(() -> new M3Tab("Tab"), 120.0, 40.0, 1.0, "M3Tab");
        assertPressedScaleAfterRealPulse(() -> new M3IconToggleButton("star"), 120.0, 40.0, 1.0,
                "M3IconToggleButton");
    }

    /// Verifies that labeled button size and shape roles resolve to the Material specification metrics.
    @Test
    void buttonSizeAndShapeRolesResolveSpecificationMetrics() {
        FxTestUtils.runOnFxThread(() -> {
            List<M3Button> roundButtons = new ArrayList<>();
            List<M3Button> squareButtons = new ArrayList<>();
            for (M3ButtonSize size : M3ButtonSize.values()) {
                M3Button round = new M3Button(size.name(), new M3Icon("+"), M3ButtonVariant.OUTLINED);
                round.setSize(size);
                round.setButtonShape(M3ButtonShape.ROUND);
                roundButtons.add(round);

                M3Button square = new M3Button(size.name(), M3ButtonVariant.FILLED);
                square.setSize(size);
                square.setButtonShape(M3ButtonShape.SQUARE);
                squareButtons.add(square);
            }

            M3Button smallText = new M3Button("Text", M3ButtonVariant.TEXT);
            HBox root = new HBox(12.0);
            root.getChildren().addAll(roundButtons);
            root.getChildren().addAll(squareButtons);
            root.getChildren().add(smallText);
            Scene scene = new Scene(root, 2200.0, 320.0);
            M3Theme expressive = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );
            M3ThemeManager.install(scene, expressive);
            root.applyCss();
            root.layout();

            double[] heights = {32.0, 40.0, 56.0, 96.0, 136.0};
            double[] iconSizes = {20.0, 20.0, 24.0, 32.0, 40.0};
            double[] paddings = {12.0, 16.0, 24.0, 48.0, 64.0};
            double[] iconLabelSpaces = {8.0, 8.0, 8.0, 12.0, 16.0};
            double[] outlineWidths = {1.0, 1.0, 1.0, 2.0, 3.0};
            double[] squareShapes = {16.0, 16.0, 24.0, 32.0, 32.0};
            double[] pressedShapes = {10.0, 10.0, 16.0, 24.0, 24.0};
            String[] typographyClasses = {
                    "m3-label-large-text",
                    "m3-label-large-text",
                    "m3-title-medium-text",
                    "m3-headline-small-text",
                    "m3-headline-large-text"
            };

            for (int index = 0; index < M3ButtonSize.values().length; index++) {
                M3Button round = roundButtons.get(index);
                M3Button square = squareButtons.get(index);
                assertEquals(heights[index], round.getContainerHeight(), 0.0001);
                assertEquals(iconSizes[index], round.getIconSize(), 0.0001);
                assertEquals(paddings[index], round.getHorizontalPadding(), 0.0001);
                assertEquals(iconLabelSpaces[index], round.getGraphicTextGap(), 0.0001);
                assertEquals(999.0, round.getContainerShape(), 0.0001);
                assertTrue(round.getStyleClass().contains(typographyClasses[index]));
                assertEquals(
                        outlineWidths[index],
                        round.getBorder().getStrokes().get(0).getWidths().getTop(),
                        0.0001
                );
                assertEquals(squareShapes[index], square.getContainerShape(), 0.0001);

                round.arm();
                assertTrue(round.getPseudoClassStates().contains(PseudoClass.getPseudoClass("armed")));
                root.applyCss();
                assertEquals(
                        pressedShapes[index],
                        round.getContainerShape(),
                        0.0001,
                        () -> "pressed shape mismatch for " + round.getSize()
                );
                round.disarm();
                root.applyCss();
                assertEquals(999.0, round.getContainerShape(), 0.0001);

                square.arm();
                root.applyCss();
                assertEquals(
                        pressedShapes[index],
                        square.getContainerShape(),
                        0.0001,
                        () -> "pressed square shape mismatch for " + square.getSize()
                );
                square.disarm();
                root.applyCss();
                assertEquals(squareShapes[index], square.getContainerShape(), 0.0001);
            }

            assertEquals(16.0, roundButtons.get(1).getHorizontalPadding(), 0.0001);
            assertEquals(16.0, smallText.getHorizontalPadding(), 0.0001);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            for (int index = 0; index < M3ButtonSize.values().length; index++) {
                M3Button round = roundButtons.get(index);
                M3Button square = squareButtons.get(index);
                double roundRestingShape = round.getContainerShape();
                double squareRestingShape = square.getContainerShape();

                round.arm();
                square.arm();
                root.applyCss();
                assertEquals(roundRestingShape, round.getContainerShape(), 0.0001);
                assertEquals(squareRestingShape, square.getContainerShape(), 0.0001);

                round.disarm();
                square.disarm();
                root.applyCss();
                assertEquals(roundRestingShape, round.getContainerShape(), 0.0001);
                assertEquals(squareRestingShape, square.getContainerShape(), 0.0001);
            }
            assertEquals(24.0, roundButtons.get(1).getHorizontalPadding(), 0.0001);
            assertEquals(12.0, smallText.getHorizontalPadding(), 0.0001);
        });
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
        assertEquals(52.0, button.prefHeight(-1.0), 0.0001);
        assertEquals(18.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that state layer feedback changes rendered button pixels.
    @Tier2Test
    @Test
    void buttonStateLayerChangesRenderedPixels() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 200.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
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

    /// Verifies that a real button ripple remains visible after release and fades out through animation.
    @Tier2Test
    @Test
    void buttonRippleReleaseAnimationFadesAfterPointerRelease() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> buttonReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseStartOpacityReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> buttonRippleIsFullyExpanded(buttonReference),
                    () -> {
                        showObservableRippleButton(stageReference, rootReference, buttonReference);
                        Objects.requireNonNull(buttonReference.get(), "button")
                                .fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 24.0, 20.0, true));
                    },
                    () -> {
                        M3Button button = Objects.requireNonNull(buttonReference.get(), "button");
                        Region ripple = lookupRegion(button, ".m3-ripple");
                        double releaseStartOpacity = ripple.getOpacity();

                        assertTrue(releaseStartOpacity > 0.0);
                        releaseStartOpacityReference.set(releaseStartOpacity);

                        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 24.0, 20.0, false));

                        assertTrue(ripple.getOpacity() > 0.0, () -> "ripple opacity=" + ripple.getOpacity());
                        assertFalse(button.isArmed());
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> buttonRippleOpacityFadedBelow(buttonReference, releaseStartOpacityReference),
                    () -> {
                    },
                    () -> {
                        double releaseStartOpacity =
                                Objects.requireNonNull(releaseStartOpacityReference.get(), "releaseStartOpacity");
                        Region ripple = lookupRegion(
                                Objects.requireNonNull(buttonReference.get(), "button"),
                                ".m3-ripple"
                        );
                        double midReleaseOpacity = ripple.getOpacity();

                        assertTrue(midReleaseOpacity > 0.0, () -> "midReleaseOpacity=" + midReleaseOpacity);
                        assertTrue(midReleaseOpacity < releaseStartOpacity, () ->
                                "midReleaseOpacity=" + midReleaseOpacity
                                        + ", releaseStartOpacity=" + releaseStartOpacity);
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> buttonRippleOpacityCleared(buttonReference),
                    () -> {
                    },
                    () -> assertEquals(
                            0.0,
                            lookupRegion(
                                    Objects.requireNonNull(buttonReference.get(), "button"),
                                    ".m3-ripple"
                            ).getOpacity(),
                            0.0001
                    )
            );
        } finally {
            closeObservableRippleButton(stageReference, rootReference, buttonReference);
        }
    }

    /// Verifies that ripple opacity starts fading immediately when a quick pointer click is released.
    @Tier2Test
    @Test
    void buttonRippleReleaseAnimationFadesDuringExpansionAfterQuickClick() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> buttonReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseStartOpacityReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> buttonRippleOpacityFadedBelow(buttonReference, releaseStartOpacityReference),
                    () -> {
                        showObservableRippleButton(stageReference, rootReference, buttonReference);
                        M3Button button = Objects.requireNonNull(buttonReference.get(), "button");

                        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 24.0, 20.0, true));
                        Region ripple = lookupRegion(button, ".m3-ripple");
                        releaseStartOpacityReference.set(ripple.getOpacity());

                        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 24.0, 20.0, false));

                        assertTrue(ripple.getOpacity() > 0.0, () -> "ripple opacity=" + ripple.getOpacity());
                        assertFalse(button.isArmed());
                    },
                    () -> {
                        double releaseStartOpacity =
                                Objects.requireNonNull(releaseStartOpacityReference.get(), "releaseStartOpacity");
                        Region ripple = lookupRegion(
                                Objects.requireNonNull(buttonReference.get(), "button"),
                                ".m3-ripple"
                        );
                        double midReleaseOpacity = ripple.getOpacity();

                        assertTrue(midReleaseOpacity > 0.0, () -> "midReleaseOpacity=" + midReleaseOpacity);
                        assertTrue(midReleaseOpacity < releaseStartOpacity, () ->
                                "midReleaseOpacity=" + midReleaseOpacity
                                        + ", releaseStartOpacity=" + releaseStartOpacity);
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> buttonRippleOpacityCleared(buttonReference),
                    () -> {
                    },
                    () -> assertEquals(
                            0.0,
                            lookupRegion(
                                    Objects.requireNonNull(buttonReference.get(), "button"),
                                    ".m3-ripple"
                            ).getOpacity(),
                            0.0001
                    )
            );
        } finally {
            closeObservableRippleButton(stageReference, rootReference, buttonReference);
        }
    }

    /// Shows a button configured for observable ripple animation frames.
    private static void showObservableRippleButton(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Pane> rootReference,
            AtomicReference<@Nullable M3Button> buttonReference
    ) {
        M3Button button = createButton("Ripple", M3ButtonVariant.FILLED);
        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);
        Stage stage = new Stage();

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, true);
        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        button.resize(120.0, 40.0);
        button.layout();

        stageReference.set(stage);
        rootReference.set(root);
        buttonReference.set(button);
    }

    /// Closes a button test window and clears local motion overrides.
    private static void closeObservableRippleButton(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Pane> rootReference,
            AtomicReference<@Nullable M3Button> buttonReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable M3Button button = buttonReference.get();
            if (button != null && button.isArmed()) {
                button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 24.0, 20.0, false));
            }

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

    /// Returns whether the tested button ripple has reached its expanded pressed frame.
    private static boolean buttonRippleIsFullyExpanded(AtomicReference<@Nullable M3Button> buttonReference) {
        @Nullable Region ripple = buttonRipple(buttonReference);
        return ripple != null
                && ripple.getOpacity() > 0.0
                && ripple.getScaleX() >= 0.98
                && ripple.getScaleY() >= 0.98;
    }

    /// Returns whether the tested button ripple is visibly fading below a captured release opacity.
    private static boolean buttonRippleOpacityFadedBelow(
            AtomicReference<@Nullable M3Button> buttonReference,
            AtomicReference<@Nullable Double> baselineOpacityReference
    ) {
        @Nullable Region ripple = buttonRipple(buttonReference);
        @Nullable Double baselineOpacity = baselineOpacityReference.get();
        if (ripple == null || baselineOpacity == null) {
            return false;
        }

        double opacity = ripple.getOpacity();
        return opacity > 0.0 && opacity < baselineOpacity - 0.001;
    }

    /// Returns whether the tested button ripple has completed release fade-out.
    private static boolean buttonRippleOpacityCleared(AtomicReference<@Nullable M3Button> buttonReference) {
        @Nullable Region ripple = buttonRipple(buttonReference);
        return ripple != null && ripple.getOpacity() <= 0.0001;
    }

    /// Returns the tested button ripple region after applying current layout state.
    private static @Nullable Region buttonRipple(AtomicReference<@Nullable M3Button> buttonReference) {
        @Nullable M3Button button = buttonReference.get();
        if (button == null) {
            return null;
        }

        button.applyCss();
        button.layout();
        @Nullable Node rippleNode = button.lookup(".m3-ripple");
        return rippleNode instanceof Region ripple ? ripple : null;
    }

    /// Verifies the resolved shape radius used by a control's state layer.
    private static void assertStateLayerShape(Node node, double expectedRadius) {
        assertStateLayerRadii(node, expectedRadius, expectedRadius, expectedRadius, expectedRadius);
    }

    /// Verifies the concrete container paint rendered by an M3FX button skin.
    private static void assertButtonContainerFill(ButtonBase button, Color expectedFill) {
        assertRegionFill(lookupRegion(button, ".m3-container-paint"), expectedFill);
    }

    /// Verifies that one button-like control fires from a pointer click near its layout edge.
    private static void assertButtonBaseFullBoundsClick(ButtonBase control, String description) {
        AtomicInteger fireCount = new AtomicInteger();
        control.setOnAction(event -> fireCount.incrementAndGet());
        Pane root = new Pane(control);
        Scene scene = new Scene(root, 240.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        control.resize(180.0, Math.max(56.0, control.prefHeight(180.0)));
        control.layout();

        double x = Math.max(1.0, control.getLayoutBounds().getWidth() - 2.0);
        double y = Math.max(1.0, control.getLayoutBounds().getHeight() - 2.0);
        assertTrue(control.isPickOnBounds(), () -> description + " should pick on full layout bounds");
        assertTrue(control.contains(x, y),
                () -> description + " should contain a pointer near the layout edge: x=" + x + ", y=" + y);

        control.fireEvent(primaryMouseEvent(control, MouseEvent.MOUSE_PRESSED, x, y, true));
        assertTrue(control.isArmed(), () -> description + " should arm from a full-bounds pointer press");
        control.fireEvent(primaryMouseEvent(control, MouseEvent.MOUSE_RELEASED, x, y, false));

        assertFalse(control.isArmed(), () -> description + " should disarm after pointer release");
        assertEquals(1, fireCount.get(), () -> description + " should fire from a full-bounds pointer click");
    }

    /// Verifies that one child of a grouped action control fires from a pointer click near its layout edge.
    private static void assertGroupedButtonBaseFullBoundsClick(
            Control group,
            ButtonBase control,
            String description
    ) {
        AtomicInteger fireCount = new AtomicInteger();
        control.setOnAction(event -> fireCount.incrementAndGet());
        Pane root = new Pane(group);
        Scene scene = new Scene(root, 420.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        double width = Math.max(240.0, Math.ceil(group.prefWidth(-1.0)));
        double height = Math.max(72.0, Math.ceil(group.prefHeight(width)));
        group.resizeRelocate(20.0, 20.0, width, height);
        root.layout();
        group.layout();
        control.layout();

        double x = Math.max(1.0, control.getLayoutBounds().getWidth() - 2.0);
        double y = Math.max(1.0, control.getLayoutBounds().getHeight() / 2.0);
        assertTrue(control.isPickOnBounds(), () -> description + " should pick on full layout bounds");
        assertTrue(control.contains(x, y),
                () -> description + " should contain a pointer near the layout edge: x=" + x + ", y=" + y);

        control.fireEvent(primaryMouseEvent(control, MouseEvent.MOUSE_PRESSED, x, y, true));
        assertTrue(control.isArmed(), () -> description + " should arm from a grouped full-bounds pointer press");
        control.fireEvent(primaryMouseEvent(control, MouseEvent.MOUSE_RELEASED, x, y, false));

        assertFalse(control.isArmed(), () -> description + " should disarm after pointer release");
        assertEquals(1, fireCount.get(), () -> description + " should fire from a grouped full-bounds pointer click");
    }

    /// Verifies that one list item fires from a pointer click near its layout edge.
    private static void assertListItemFullBoundsClick(M3ListItem item) {
        AtomicInteger fireCount = new AtomicInteger();
        item.setOnAction(event -> fireCount.incrementAndGet());
        Pane root = new Pane(item);
        Scene scene = new Scene(root, 280.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        item.resize(240.0, 64.0);
        item.layout();

        double x = item.getLayoutBounds().getWidth() - 2.0;
        double y = item.getLayoutBounds().getHeight() - 2.0;
        assertTrue(item.isPickOnBounds(), "M3ListItem should pick on full layout bounds");
        assertTrue(item.contains(x, y),
                () -> "M3ListItem should contain a pointer near the layout edge: x=" + x + ", y=" + y);

        item.fireEvent(primaryMouseEvent(item, MouseEvent.MOUSE_PRESSED, x, y, true));
        item.fireEvent(primaryMouseEvent(item, MouseEvent.MOUSE_RELEASED, x, y, false));

        assertEquals(1, fireCount.get(), "M3ListItem should fire from a full-bounds pointer click");
    }
}
