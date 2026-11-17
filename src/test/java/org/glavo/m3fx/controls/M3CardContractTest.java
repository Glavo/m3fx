// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3CardSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies card actions, nested controls, elevation, and content layout.
@NotNullByDefault
final class M3CardContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
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
        M3Card card = new M3Card(new Label("Content"), M3CardVariant.OUTLINED);
        card.setOnAction(event -> actionCount.incrementAndGet());
        card.addEventHandler(ActionEvent.ACTION, event -> eventCount.incrementAndGet());

        card.fire();

        assertEquals(M3CardVariant.OUTLINED, card.getVariant());
        assertFalse(card.isDragged());
        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());

        card.setDragged(true);

        assertTrue(card.isDragged());
        assertTrue(card.getPseudoClassStates().contains(PseudoClass.getPseudoClass("dragged")));

        card.setDisable(true);
        card.fire();

        assertEquals(1, actionCount.get());
        assertEquals(1, eventCount.get());
    }

    /// Verifies that card action state controls accessibility semantics without leaking focus traversal.
    @Test
    void cardActionStateControlsAccessibleRoleAndFocusTraversal() {
        M3Card card = new M3Card(new Label("Content"));

        assertEquals(AccessibleRole.PARENT, card.getAccessibleRole());
        assertFalse(card.isFocusTraversable());

        card.setOnAction(event -> {
        });

        assertEquals(AccessibleRole.BUTTON, card.getAccessibleRole());
        assertTrue(card.isFocusTraversable());

        card.setOnAction(event -> {
        });
        card.setOnAction(null);

        assertEquals(AccessibleRole.PARENT, card.getAccessibleRole());
        assertFalse(card.isFocusTraversable());

        M3Card explicitlyFocusableCard = new M3Card(new Label("Focusable content"));
        explicitlyFocusableCard.setFocusTraversable(true);
        explicitlyFocusableCard.setOnAction(event -> {
        });
        explicitlyFocusableCard.setOnAction(null);

        assertEquals(AccessibleRole.PARENT, explicitlyFocusableCard.getAccessibleRole());
        assertTrue(explicitlyFocusableCard.isFocusTraversable());
    }

    /// Verifies that cards expose content focus while preserving actionable surface focus.
    @Test
    void cardAccessibleFocusIncludesContentAndActionSurface() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button passiveContent = new M3Button("Open");
            M3Card passiveCard = new M3Card(passiveContent);
            M3Button actionContent = new M3Button("Nested action");
            M3Card actionCard = new M3Card(actionContent, M3CardVariant.FILLED);
            actionCard.setOnAction(event -> {
            });
            Pane root = new Pane(passiveCard, actionCard);
            Stage stage = new Stage();

            try {
                Scene scene = new Scene(root, 560.0, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                passiveCard.resizeRelocate(24.0, 24.0, 220.0, 96.0);
                actionCard.resizeRelocate(280.0, 24.0, 220.0, 96.0);
                root.layout();

                assertEquals(1, passiveCard.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(passiveContent, passiveCard.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(passiveContent, passiveCard.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertSame(passiveContent, passiveCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                passiveCard.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(passiveContent.isFocused());
                assertSame(passiveContent, passiveCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                actionCard.requestFocus();

                assertTrue(actionCard.isFocused());
                assertSame(actionCard, actionCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                actionCard.executeAccessibleAction(AccessibleAction.SHOW_ITEM, actionContent);

                assertTrue(actionContent.isFocused());
                assertSame(actionContent, actionCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                actionCard.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(actionContent.isFocused());
                assertSame(actionContent, actionCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                actionCard.requestFocus();
                actionCard.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(actionCard.isFocused());
                assertSame(actionCard, actionCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                actionCard.setContent(null);

                assertEquals(0, actionCard.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertNull(actionCard.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(actionCard, actionCard.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
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

        assertNull(card.lookup(".m3-ripple"));
        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 20.0, 20.0, true));
        assertNull(card.lookup(".m3-ripple"),
                "passive cards should not expose press feedback");

        card.setOnAction(event -> {
        });
        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 20.0, 20.0, true));

        Region ripple = lookupRegion(card, ".m3-ripple");
        assertTrue(ripple.getOpacity() > 0.0, "actionable cards should expose bounded ripple feedback");
    }

    /// Verifies that card skins route passive surface content and keyboard activation without stealing nested actions.
    @Test
    void cardSkinRoutesSurfaceAndKeyboardActions() {
        Label passiveContent = new Label("Open project");
        M3Text passiveM3Text = new M3Text("Open details", M3TextRole.BODY_MEDIUM);
        M3Button nestedAction = new M3Button("Nested action");
        VBox content = new VBox(passiveContent, passiveM3Text, nestedAction);
        M3Card card = new M3Card(content);
        AtomicInteger actionCount = new AtomicInteger();
        AtomicInteger nestedActionCount = new AtomicInteger();
        card.setOnAction(event -> actionCount.incrementAndGet());
        nestedAction.setOnAction(event -> nestedActionCount.incrementAndGet());
        Pane root = new Pane(card);
        Scene scene = new Scene(root, 220.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        card.resize(180.0, 80.0);
        card.layout();

        passiveContent.fireEvent(primaryMouseEvent(
                passiveContent,
                MouseEvent.MOUSE_PRESSED,
                4.0,
                4.0,
                true
        ));
        passiveContent.fireEvent(primaryMouseEvent(
                passiveContent,
                MouseEvent.MOUSE_RELEASED,
                4.0,
                4.0,
                false
        ));
        Node renderedText = Objects.requireNonNull(passiveM3Text.lookup(".text"), "rendered M3 text");
        renderedText.fireEvent(primaryMouseEvent(
                renderedText,
                MouseEvent.MOUSE_PRESSED,
                1.0,
                1.0,
                true
        ));
        assertTrue(card.getPseudoClassStates().contains(PseudoClass.getPseudoClass("armed")));
        renderedText.fireEvent(primaryMouseEvent(
                renderedText,
                MouseEvent.MOUSE_RELEASED,
                1.0,
                1.0,
                false
        ));
        assertFalse(card.getPseudoClassStates().contains(PseudoClass.getPseudoClass("armed")));
        nestedAction.fireEvent(primaryMouseEvent(
                nestedAction,
                MouseEvent.MOUSE_PRESSED,
                4.0,
                4.0,
                true
        ));
        nestedAction.fireEvent(primaryMouseEvent(
                nestedAction,
                MouseEvent.MOUSE_RELEASED,
                4.0,
                4.0,
                false
        ));
        card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

        assertEquals(3, actionCount.get());
        assertEquals(1, nestedActionCount.get());

        card.setDisable(true);
        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 20.0, 20.0, true));
        card.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 20.0, 20.0, false));
        card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        card.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));

        assertEquals(3, actionCount.get());
        assertEquals(1, nestedActionCount.get());
    }

    /// Verifies that card variants apply Material state elevation and outline treatments.
    @Test
    void cardVariantsApplyMaterialStateElevationAndOutlineTreatments() {
        FxTestUtils.runOnFxThread(() -> {
            M3Card filled = new M3Card(new Label("Filled"), M3CardVariant.FILLED);
            M3Card elevated = new M3Card(new Label("Elevated"), M3CardVariant.ELEVATED);
            M3Card outlined = new M3Card(new Label("Outlined"), M3CardVariant.OUTLINED);
            Pane root = new Pane(filled, elevated, outlined);
            Scene scene = new Scene(root, 640.0, 180.0);
            PseudoClass hover = PseudoClass.getPseudoClass("hover");
            PseudoClass focusVisible = PseudoClass.getPseudoClass("focus-visible");
            PseudoClass pressed = PseudoClass.getPseudoClass("pressed");

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            filled.resizeRelocate(16.0, 16.0, 180.0, 96.0);
            elevated.resizeRelocate(220.0, 16.0, 180.0, 96.0);
            outlined.resizeRelocate(424.0, 16.0, 180.0, 96.0);
            root.applyCss();
            root.layout();

            Region filledContainer = lookupRegion(filled, ".m3-card-container");
            Region elevatedContainer = lookupRegion(elevated, ".m3-card-container");
            Region outlinedContainer = lookupRegion(outlined, ".m3-card-container");
            DropShadow elevatedDefault = assertDropShadow(elevatedContainer);
            Paint outlinedDefaultBorder = firstBorderTopPaint(outlinedContainer);

            assertNull(filledContainer.getEffect(), "filled cards should start at level0");
            assertNull(outlinedContainer.getEffect(), "outlined cards should start at level0");

            filled.pseudoClassStateChanged(hover, true);
            root.applyCss();
            assertNull(filledContainer.getEffect(), "passive cards should not expose hover elevation");
            filled.pseudoClassStateChanged(hover, false);
            filled.setOnAction(event -> {
            });
            elevated.setOnAction(event -> {
            });
            outlined.setOnAction(event -> {
            });
            outlined.pseudoClassStateChanged(hover, true);
            filled.pseudoClassStateChanged(hover, true);
            root.applyCss();
            DropShadow filledHover = assertDropShadow(filledContainer);
            DropShadow outlinedHover = assertDropShadow(outlinedContainer);
            assertEquals(outlinedDefaultBorder, firstBorderTopPaint(outlinedContainer));

            filled.pseudoClassStateChanged(pressed, true);
            outlined.pseudoClassStateChanged(pressed, true);
            root.applyCss();
            assertNull(filledContainer.getEffect(), "pressed filled cards should return to level0");
            assertNull(outlinedContainer.getEffect(), "pressed outlined cards should return to level0");
            assertEquals(outlinedDefaultBorder, firstBorderTopPaint(outlinedContainer));

            filled.pseudoClassStateChanged(hover, false);
            filled.pseudoClassStateChanged(pressed, false);
            outlined.pseudoClassStateChanged(hover, false);
            outlined.pseudoClassStateChanged(pressed, false);
            filled.pseudoClassStateChanged(focusVisible, true);
            outlined.pseudoClassStateChanged(focusVisible, true);
            root.applyCss();
            assertNull(filledContainer.getEffect(), "focused filled cards should stay at level0");
            assertNull(outlinedContainer.getEffect(), "focused outlined cards should stay at level0");
            assertFalse(outlinedDefaultBorder.equals(firstBorderTopPaint(outlinedContainer)),
                    "focused outlined cards should use a distinct focus outline color");

            elevated.pseudoClassStateChanged(hover, true);
            root.applyCss();
            DropShadow elevatedHover = assertDropShadow(elevatedContainer);
            assertTrue(elevatedHover.getRadius() > elevatedDefault.getRadius(),
                    "hovered elevated cards should increase elevation");
            assertTrue(elevatedHover.getOffsetY() > elevatedDefault.getOffsetY(),
                    "hovered elevated cards should increase shadow offset");

            elevated.pseudoClassStateChanged(pressed, true);
            root.applyCss();
            DropShadow elevatedPressed = assertDropShadow(elevatedContainer);
            assertEquals(elevatedDefault.getRadius(), elevatedPressed.getRadius(), 0.0001,
                    "pressed elevated cards should return to the enabled elevation");
            assertEquals(elevatedDefault.getOffsetY(), elevatedPressed.getOffsetY(), 0.0001,
                    "pressed elevated cards should return to the enabled shadow offset");

            filled.pseudoClassStateChanged(focusVisible, false);
            outlined.pseudoClassStateChanged(focusVisible, false);
            elevated.pseudoClassStateChanged(hover, false);
            elevated.pseudoClassStateChanged(pressed, false);
            filled.setDragged(true);
            elevated.setDragged(true);
            outlined.setDragged(true);
            root.applyCss();

            assertTrue(assertDropShadow(filledContainer).getRadius() > filledHover.getRadius(),
                    "dragged filled cards should use level3 rather than hover level1");
            assertTrue(assertDropShadow(elevatedContainer).getRadius() > elevatedHover.getRadius(),
                    "dragged elevated cards should use level4 rather than hover level2");
            assertTrue(assertDropShadow(outlinedContainer).getRadius() > outlinedHover.getRadius(),
                    "dragged outlined cards should use level3 rather than hover level1");
        });
    }

    /// Verifies that disabled card variants style their containers without compounding content opacity.
    @SuppressWarnings("deprecation")
    @Tier2Test
    @Test
    void disabledCardsRenderVariantSpecificContainersWithoutCompoundingContentOpacity() {
        M3Theme theme = M3Theme.defaultTheme();
        Color filledDisabledContainer = withOpacity(
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE_VARIANT),
                0.38
        );
        Color elevatedDisabledContainer = withOpacity(
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE),
                0.38
        );
        Color outlinedDisabledOutline = withOpacity(
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.OUTLINE),
                0.12
        );

        Label filledContent = new Label("Filled content");
        Label elevatedContent = new Label("Elevated content");
        Label outlinedContent = new Label("Outlined content");
        M3Card filled = new M3Card(filledContent, M3CardVariant.FILLED);
        M3Card elevated = new M3Card(elevatedContent, M3CardVariant.ELEVATED);
        M3Card outlined = new M3Card(outlinedContent, M3CardVariant.OUTLINED);
        filled.setOnAction(event -> {
        });
        elevated.setOnAction(event -> {
        });
        outlined.setOnAction(event -> {
        });
        HBox root = new HBox(filled, elevated, outlined);
        Scene scene = new Scene(root, 720.0, 180.0);
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        root.layout();

        Region filledContainer = lookupRegion(filled, ".m3-card-container");
        Region elevatedContainer = lookupRegion(elevated, ".m3-card-container");
        Region outlinedContainer = lookupRegion(outlined, ".m3-card-container");
        DropShadow elevatedDefault = assertDropShadow(elevatedContainer);

        PseudoClass hover = PseudoClass.getPseudoClass("hover");
        filled.pseudoClassStateChanged(hover, true);
        elevated.pseudoClassStateChanged(hover, true);
        outlined.pseudoClassStateChanged(hover, true);
        root.applyCss();
        assertDropShadow(filledContainer);
        assertTrue(assertDropShadow(elevatedContainer).getRadius() > elevatedDefault.getRadius());
        assertDropShadow(outlinedContainer);

        filled.setDisable(true);
        elevated.setDisable(true);
        outlined.setDisable(true);

        assertNull(filledContainer.getEffect(), "newly disabled filled card elevation");
        assertDropShadow(elevatedContainer);
        assertNull(outlinedContainer.getEffect(), "newly disabled outlined card elevation");
        root.applyCss();

        assertEquals(1.0, filled.getOpacity(), 0.0001);
        assertEquals(1.0, elevated.getOpacity(), 0.0001);
        assertEquals(1.0, outlined.getOpacity(), 0.0001);
        assertEquals(0.4, filledContent.getOpacity(), 0.0001);
        assertEquals(0.4, elevatedContent.getOpacity(), 0.0001);
        assertEquals(0.4, outlinedContent.getOpacity(), 0.0001);

        assertRegionFill(filledContainer, filledDisabledContainer);
        assertNull(filledContainer.getEffect(), "disabled filled card elevation");
        assertRegionFill(elevatedContainer, elevatedDisabledContainer);
        DropShadow elevatedDisabled = assertDropShadow(elevatedContainer);
        assertEquals(elevatedDefault.getRadius(), elevatedDisabled.getRadius(), 0.0001,
                "disabled elevated card radius");
        assertEquals(elevatedDefault.getOffsetY(), elevatedDisabled.getOffsetY(), 0.0001,
                "disabled elevated card offset");
        assertRegionFill(outlinedContainer, theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE));
        assertBorderColor(outlinedContainer, outlinedDisabledOutline);
        assertNull(outlinedContainer.getEffect(), "disabled outlined card elevation");
    }
}
