// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.skins.M3SplitButtonSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.M3TestControls.*;
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

/// Verifies split button actions, menus, geometry, and state styling.
@NotNullByDefault
final class M3SplitButtonContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that split buttons expose primary action behavior and their owned menu.
    @Test
    void splitButtonDelegatesActionAndMenuApis() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3SplitButton splitButton = splitButton("Create", first, second);
        AtomicInteger actions = new AtomicInteger();

        splitButton.setOnAction(event -> actions.incrementAndGet());
        splitButton.fire();
        splitButton.setVariant(M3ButtonVariant.OUTLINED);
        splitButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
        splitButton.getMenu().setAllowEmptySelection(false);
        splitButton.getMenu().selectIndex(1);

        assertEquals(1, actions.get());
        assertEquals("Create", splitButton.getText());
        assertEquals(splitButtonActionButton(splitButton).getText(), splitButton.textProperty().get());
        assertEquals(M3ButtonVariant.OUTLINED, splitButtonActionButton(splitButton).getVariant());
        assertEquals(M3ButtonVariant.OUTLINED, splitButtonMenuButton(splitButton).getVariant());
        assertEquals(second, splitButton.getMenu().getSelectedItem());
        assertEquals(1, splitButton.getMenu().getSelectedIndex());
        assertEquals(splitButton.getMenu(), splitButton.queryAccessibleAttribute(AccessibleAttribute.SUBMENU));
        assertEquals(2, splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(splitButtonMenuButton(splitButton), splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertFalse(splitButton.isShowing());
    }

    /// Verifies that split buttons route accessibility focus through their attached popup menu.
    @Test
    void splitButtonRoutesAccessibleFocusThroughPopupMenu() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem draft = new M3MenuItem("Draft");
            M3MenuItem publish = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draft, publish);
            Stage stage = new Stage();
            try {
                splitButton.setAnimationsEnabled(false);
                Pane root = new Pane(splitButton);
                stage.setScene(new Scene(root, 320.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                SVGPath menuIndicator = assertInstanceOf(
                        SVGPath.class,
                        splitButton.lookup(".m3-disclosure-icon-shape")
                );
                assertEquals(0.0, menuIndicator.getRotate(), 0.0001);

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publish);

                assertTrue(splitButton.isShowing());
                assertEquals(180.0, menuIndicator.getRotate(), 0.0001);
                assertTrue(publish.isFocused());
                assertSame(publish, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                splitButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(publish.isFocused());
                assertSame(publish, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                splitButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(splitButton.isShowing());
                assertEquals(0.0, menuIndicator.getRotate(), 0.0001);
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
                assertSame(
                        splitButtonMenuButton(splitButton),
                        splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(splitButtonActionButton(splitButton).isFocused());

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
            } finally {
                splitButton.setAnimationsEnabled(true);
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that split button accessibility selection actions operate on the attached menu.
    @Test
    void splitButtonDelegatesAccessibleSelectionToPopupMenu() {
        M3MenuItem draft = new M3MenuItem("Draft");
        M3MenuItem publish = new M3MenuItem("Publish");
        M3SplitButton splitButton = splitButton("Create", draft, publish);

        splitButton.getMenu().setSelectionMode(M3SelectionMode.MULTIPLE);
        splitButton.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, java.util.List.of(draft, publish));

        assertEquals(Boolean.TRUE, splitButton.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(java.util.List.of(draft, publish),
                splitButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
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

        assertTrue(splitButton.getStyleClass().contains("m3-split-button"));
        assertTrue(splitButtonActionButton(splitButton).getStyleClass().contains("m3-split-button-action"));
        assertTrue(splitButtonMenuButton(splitButton).getStyleClass().contains("m3-split-button-menu"));
        assertTrue(splitButtonActionButton(splitButton).getPseudoClassStates().contains(leftEdge));
        assertTrue(splitButtonMenuButton(splitButton).getPseudoClassStates().contains(rightEdge));
        assertFalse(splitButtonActionButton(splitButton).getStyleClass().contains("m3-split-button-left"));
        assertFalse(splitButtonActionButton(splitButton).getStyleClass().contains("m3-split-button-right"));
        assertFalse(splitButtonMenuButton(splitButton).getStyleClass().contains("m3-split-button-left"));
        assertFalse(splitButtonMenuButton(splitButton).getStyleClass().contains("m3-split-button-right"));

        applyCss(splitButton);

        assertInstanceOf(M3SplitButtonSkin.class, splitButton.getSkin());
        M3DisclosureIcon menuIndicator = assertInstanceOf(
                M3DisclosureIcon.class,
                splitButtonMenuButton(splitButton).getGraphic()
        );
        assertTrue(menuIndicator.isVertical());
        assertEquals(48.0, splitButtonMenuButton(splitButton).prefWidth(-1.0), 0.0001);
        assertEquals(0.0, splitButtonMenuButton(splitButton).getHorizontalPadding(), 0.0001);
    }

    /// Verifies that the menu indicator uses the same foreground token as its split-button part.
    @Test
    void splitButtonMenuIndicatorFollowsVariantForegroundColor() {
        M3SplitButton filled = createSplitButton("Filled", M3ButtonVariant.FILLED, new M3MenuItem("Item"));
        M3SplitButton tonal = createSplitButton("Tonal", M3ButtonVariant.TONAL, new M3MenuItem("Item"));
        M3SplitButton outlined = createSplitButton("Outlined", M3ButtonVariant.OUTLINED, new M3MenuItem("Item"));
        M3SplitButton elevated = createSplitButton("Elevated", M3ButtonVariant.ELEVATED, new M3MenuItem("Item"));
        M3SplitButton disabled = createSplitButton("Disabled", M3ButtonVariant.TONAL, new M3MenuItem("Item"));
        disabled.setDisable(true);
        VBox root = new VBox(filled, tonal, outlined, elevated, disabled);
        Scene scene = new Scene(root, 480.0, 320.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        for (M3SplitButton splitButton : List.of(filled, tonal, outlined, elevated, disabled)) {
            M3MenuButton menuButton = splitButtonMenuButton(splitButton);
            Shape indicator = lookupShape(splitButton, ".m3-disclosure-icon-shape");
            assertEquals(menuButton.getTextFill(), indicator.getFill(),
                    () -> "split menu indicator should match its foreground for " + splitButton.getVariant()
                            + ", disabled=" + splitButton.isDisabled());
        }
    }

    /// Verifies that split button sizes resolve Material Expressive metrics from CSS.
    @Test
    void splitButtonSizesResolveMaterialExpressiveMetrics() {
        FxTestUtils.runOnFxThread(() -> {
            M3SplitButton extraSmall = createSizedSplitButton(M3ButtonSize.EXTRA_SMALL);
            M3SplitButton small = createSizedSplitButton(M3ButtonSize.SMALL);
            M3SplitButton medium = createSizedSplitButton(M3ButtonSize.MEDIUM);
            M3SplitButton large = createSizedSplitButton(M3ButtonSize.LARGE);
            M3SplitButton extraLarge = createSizedSplitButton(M3ButtonSize.EXTRA_LARGE);
            VBox root = new VBox(8.0, extraSmall, small, medium, large, extraLarge);
            Scene scene = new Scene(root, 520.0, 520.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT,
                    M3Density.standard()
            ));
            root.applyCss();

            assertSplitButtonMetrics(extraSmall, M3ButtonSize.EXTRA_SMALL, 32.0, 12.0, 10.0, 48.0, 22.0, 2.0);
            assertSplitButtonShapeMetrics(extraSmall, 16.0, 4.0, 8.0, 8.0, 16.0);
            assertSplitButtonMetrics(small, M3ButtonSize.SMALL, 40.0, 16.0, 12.0, 48.0, 22.0, 2.0);
            assertSplitButtonShapeMetrics(small, 20.0, 4.0, 12.0, 12.0, 20.0);
            assertSplitButtonMetrics(medium, M3ButtonSize.MEDIUM, 56.0, 24.0, 24.0, 56.0, 26.0, 2.0);
            assertSplitButtonShapeMetrics(medium, 28.0, 4.0, 12.0, 12.0, 28.0);
            assertSplitButtonMetrics(large, M3ButtonSize.LARGE, 96.0, 48.0, 48.0, 96.0, 38.0, 2.0);
            assertSplitButtonShapeMetrics(large, 48.0, 8.0, 20.0, 20.0, 48.0);
            assertSplitButtonMetrics(extraLarge, M3ButtonSize.EXTRA_LARGE, 136.0, 64.0, 64.0, 136.0, 50.0, 2.0);
            assertSplitButtonShapeMetrics(extraLarge, 68.0, 12.0, 20.0, 20.0, 68.0);
        });
    }

    /// Verifies that split-button surfaces and state layers use the resting and interactive inner-corner tokens.
    @Test
    void splitButtonInnerCornersAndFeedbackFollowInteractionTokens() {
        FxTestUtils.runOnFxThread(() -> {
            M3SplitButton splitButton = createSplitButton(
                    "Export",
                    M3ButtonVariant.TONAL,
                    new M3MenuItem("PDF")
            );
            Pane root = new Pane(splitButton);
            root.setStyle("-fx-background-color: white;");
            Scene scene = new Scene(root, 240.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            splitButton.resize(splitButton.prefWidth(-1.0), splitButton.prefHeight(-1.0));
            splitButton.layout();

            M3Button actionButton = splitButtonActionButton(splitButton);
            M3MenuButton menuButton = splitButtonMenuButton(splitButton);
            assertNull(actionButton.getClip());
            assertNull(menuButton.getClip());
            actionButton.layout();
            menuButton.layout();
            root.applyCss();
            Path menuSurfaceShape = assertInstanceOf(Path.class, menuButton.getShape());
            List<PathElement> menuSurfaceElements = List.copyOf(menuSurfaceShape.getElements());
            Region menuOverlay = lookupRegion(menuButton, ".m3-state-layer");
            Path menuStateClip = assertInstanceOf(
                    Path.class,
                    Objects.requireNonNull(menuOverlay.getParent(), "clipped state content").getClip()
            );
            List<PathElement> menuStateClipElements = List.copyOf(menuStateClip.getElements());
            assertRegionRadii(actionButton, 20.0, 4.0, 4.0, 20.0);
            assertRegionRadii(menuButton, 4.0, 20.0, 20.0, 4.0);
            assertStateLayerRadii(actionButton, 20.0, 4.0, 4.0, 20.0);
            assertStateLayerRadii(menuButton, 4.0, 20.0, 20.0, 4.0);
            assertRenderedTopLeftCorner(menuButton, snapshotImageOnFxThread(root), false, Color.WHITE);

            PseudoClass focused = PseudoClass.getPseudoClass("focused");
            PseudoClass focusVisible = PseudoClass.getPseudoClass("focus-visible");
            menuButton.pseudoClassStateChanged(focused, true);
            menuButton.pseudoClassStateChanged(focusVisible, true);
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            assertStateLayerRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            menuButton.pseudoClassStateChanged(focused, false);
            menuButton.pseudoClassStateChanged(focusVisible, false);

            PseudoClass hover = PseudoClass.getPseudoClass("hover");
            menuButton.pseudoClassStateChanged(hover, true);
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            assertStateLayerRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            assertRenderedTopLeftCorner(menuButton, snapshotImageOnFxThread(root), true, Color.WHITE);

            menuButton.pseudoClassStateChanged(hover, false);
            menuButton.arm();
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            assertStateLayerRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            menuButton.disarm();

            PseudoClass showing = PseudoClass.getPseudoClass("showing");
            menuButton.pseudoClassStateChanged(showing, true);
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 20.0, 20.0, 20.0, 20.0);
            assertStateLayerRadii(menuButton, 20.0, 20.0, 20.0, 20.0);
            assertEquals(0.10, lookupRegion(menuButton, ".m3-state-layer").getOpacity(), 0.0001);

            menuButton.arm();
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            assertStateLayerRadii(menuButton, 12.0, 20.0, 20.0, 12.0);
            assertEquals(0.19, lookupRegion(menuButton, ".m3-state-layer").getOpacity(), 0.0001);

            menuButton.disarm();
            menuButton.pseudoClassStateChanged(showing, false);
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 4.0, 20.0, 20.0, 4.0);
            assertStateLayerRadii(menuButton, 4.0, 20.0, 20.0, 4.0);
            assertEquals(0.0, lookupRegion(menuButton, ".m3-state-layer").getOpacity(), 0.0001);
            assertSame(menuSurfaceShape, menuButton.getShape());
            assertSamePathElements(menuSurfaceShape, menuSurfaceElements, "split button surface");
            assertSamePathElements(menuStateClip, menuStateClipElements, "split button state clip");
        });
    }

    /// Verifies that owner-level shape tokens drive both split-button surfaces and feedback clips.
    @Test
    void splitButtonOwnerTokensControlEveryPartShapeLayer() {
        FxTestUtils.runOnFxThread(() -> {
            M3SplitButton splitButton = createSplitButton(
                    "Export",
                    M3ButtonVariant.TONAL,
                    new M3MenuItem("PDF")
            );
            splitButton.setStyle(
                    "-m3-split-button-outer-corner: 18px;"
                            + " -m3-split-button-inner-corner: 6px;"
                            + " -m3-split-button-hovered-inner-corner: 10px;"
                            + " -m3-split-button-pressed-inner-corner: 14px;"
                            + " -m3-split-button-selected-inner-corner: 18px;"
            );
            Pane root = new Pane(splitButton);
            Scene scene = new Scene(root, 260.0, 100.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            splitButton.resize(splitButton.prefWidth(-1.0), splitButton.prefHeight(-1.0));
            splitButton.layout();

            M3Button actionButton = splitButtonActionButton(splitButton);
            M3MenuButton menuButton = splitButtonMenuButton(splitButton);
            actionButton.layout();
            menuButton.layout();
            root.applyCss();
            actionButton.layout();
            menuButton.layout();
            assertSplitButtonShapeMetrics(splitButton, 18.0, 6.0, 10.0, 14.0, 18.0);
            assertRegionRadii(actionButton, 18.0, 6.0, 6.0, 18.0);
            assertRegionRadii(menuButton, 6.0, 18.0, 18.0, 6.0);
            assertStateLayerRadii(actionButton, 18.0, 6.0, 6.0, 18.0);
            assertStateLayerRadii(menuButton, 6.0, 18.0, 18.0, 6.0);

            PseudoClass hover = PseudoClass.getPseudoClass("hover");
            actionButton.pseudoClassStateChanged(hover, true);
            root.applyCss();
            actionButton.layout();
            assertRegionRadii(actionButton, 18.0, 10.0, 10.0, 18.0);
            assertStateLayerRadii(actionButton, 18.0, 10.0, 10.0, 18.0);

            actionButton.pseudoClassStateChanged(hover, false);
            actionButton.arm();
            root.applyCss();
            actionButton.layout();
            assertRegionRadii(actionButton, 18.0, 14.0, 14.0, 18.0);
            assertStateLayerRadii(actionButton, 18.0, 14.0, 14.0, 18.0);
            actionButton.disarm();

            menuButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("showing"), true);
            root.applyCss();
            menuButton.layout();
            assertRegionRadii(menuButton, 18.0, 18.0, 18.0, 18.0);
            assertStateLayerRadii(menuButton, 18.0, 18.0, 18.0, 18.0);
        });
    }

    /// Verifies equal part heights, exact between-space, uncovered gap pixels, and disabled alpha composition.
    @Test
    void splitButtonLayoutPreservesTwoIndependentSurfaces() {
        FxTestUtils.runOnFxThread(() -> {
            List<M3SplitButton> splitButtons = new ArrayList<>();
            for (M3ButtonVariant variant : List.of(
                    M3ButtonVariant.TONAL,
                    M3ButtonVariant.OUTLINED,
                    M3ButtonVariant.FILLED,
                    M3ButtonVariant.ELEVATED
            )) {
                splitButtons.add(createSplitButton(variant.name(), variant, new M3MenuItem("Item")));
            }
            M3SplitButton disabledSplitButton =
                    createSplitButton("Disabled", M3ButtonVariant.TONAL, new M3MenuItem("Item"));
            disabledSplitButton.setDisable(true);
            splitButtons.add(disabledSplitButton);
            Color rootBackground = Color.web("#f8f2fa");
            HBox root = new HBox(20.0);
            root.setPadding(new Insets(20.0));
            root.setStyle("-fx-background-color: #f8f2fa;");
            root.getChildren().addAll(splitButtons);
            Scene scene = new Scene(root, 900.0, 100.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(900.0, 100.0);
            root.layout();
            WritableImage image = snapshotImageOnFxThread(root);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-split-button-surfaces.png"
            ));

            for (M3SplitButton splitButton : splitButtons) {
                M3Button actionButton = splitButtonActionButton(splitButton);
                M3MenuButton menuButton = splitButtonMenuButton(splitButton);
                Bounds actionBounds = actionButton.localToScene(actionButton.getLayoutBounds());
                Bounds menuBounds = menuButton.localToScene(menuButton.getLayoutBounds());
                assertEquals(actionBounds.getMinY(), menuBounds.getMinY(), 0.01);
                assertEquals(actionBounds.getHeight(), menuBounds.getHeight(), 0.01);
                assertNull(actionButton.getClip());
                assertNull(menuButton.getClip());
                assertEquals(
                        splitButton.getSpacing(),
                        menuBounds.getMinX() - actionBounds.getMaxX(),
                        0.25,
                        () -> "Unexpected rendered split-button gap for " + splitButton.getVariant()
                                + ": actionMaxX=" + actionBounds.getMaxX()
                                + ", menuMinX=" + menuBounds.getMinX()
                );
                if (splitButton.getVariant() != M3ButtonVariant.ELEVATED) {
                    Color gapPixel = snapshotScenePixel(
                            image,
                            (actionBounds.getMaxX() + menuBounds.getMinX()) / 2.0,
                            actionBounds.getMinY() + actionBounds.getHeight() / 2.0
                    );
                    assertTrue(colorDistance(gapPixel, rootBackground) < 0.04);
                } else {
                    assertNotNull(actionButton.getEffect());
                    assertNotNull(menuButton.getEffect());
                    assertNull(Objects.requireNonNull(actionButton.getParent(), "split button part parent").getEffect());
                }
                if (splitButton.getVariant() == M3ButtonVariant.TONAL
                        || splitButton.getVariant() == M3ButtonVariant.FILLED) {
                    WritableImage actionImage = snapshotImageOnFxThread(actionButton);
                    WritableImage menuImage = snapshotImageOnFxThread(menuButton);
                    Color actionInnerCorner = actionImage.getPixelReader().getColor(
                            (int) actionImage.getWidth() - 1,
                            0
                    );
                    Color menuInnerCorner = menuImage.getPixelReader().getColor(0, 0);
                    assertTrue(
                            colorDistance(actionInnerCorner, Color.WHITE) < 0.12,
                            () -> "Action inner corner color=" + actionInnerCorner + " for " + splitButton.getVariant()
                    );
                    assertTrue(
                            colorDistance(menuInnerCorner, Color.WHITE) < 0.12,
                            () -> "Menu inner corner color=" + menuInnerCorner + " for " + splitButton.getVariant()
                    );
                }
                if (splitButton.isDisabled()) {
                    Color disabledContainerPaint =
                            assertInstanceOf(Color.class, actionButton.getContainerColor());
                    assertEquals(disabledContainerPaint, menuButton.getContainerColor());
                    double containerOpacity = disabledContainerPaint.getOpacity();
                    Color expectedContainerPixel = Color.color(
                            disabledContainerPaint.getRed() * containerOpacity
                                    + rootBackground.getRed() * (1.0 - containerOpacity),
                            disabledContainerPaint.getGreen() * containerOpacity
                                    + rootBackground.getGreen() * (1.0 - containerOpacity),
                            disabledContainerPaint.getBlue() * containerOpacity
                                    + rootBackground.getBlue() * (1.0 - containerOpacity)
                    );
                    Color actionContainerPixel = snapshotScenePixel(
                            image,
                            actionBounds.getMinX() + 12.0,
                            actionBounds.getCenterY()
                    );
                    Color menuContainerPixel = snapshotScenePixel(
                            image,
                            menuBounds.getMinX() + 8.0,
                            menuBounds.getCenterY()
                    );
                    assertTrue(
                            colorDistance(actionContainerPixel, expectedContainerPixel) < 0.04,
                            () -> "Disabled split action container was composited more than once: actual="
                                    + actionContainerPixel + ", expected=" + expectedContainerPixel
                    );
                    assertTrue(
                            colorDistance(menuContainerPixel, expectedContainerPixel) < 0.04,
                            () -> "Disabled split menu container was composited more than once: actual="
                                    + menuContainerPixel + ", expected=" + expectedContainerPixel
                    );
                }
            }
        });
    }

    /// Verifies that split buttons keep logical part shapes for right-to-left painting.
    @Test
    void splitButtonKeepsLogicalPartShapesForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
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
            splitButtonActionButton(splitButton).layout();
            splitButtonMenuButton(splitButton).layout();
            root.applyCss();

            PseudoClass leftEdge = PseudoClass.getPseudoClass("left-edge");
            PseudoClass rightEdge = PseudoClass.getPseudoClass("right-edge");
            assertTrue(splitButtonActionButton(splitButton).getPseudoClassStates().contains(leftEdge));
            assertFalse(splitButtonActionButton(splitButton).getPseudoClassStates().contains(rightEdge));
            assertFalse(splitButtonMenuButton(splitButton).getPseudoClassStates().contains(leftEdge));
            assertTrue(splitButtonMenuButton(splitButton).getPseudoClassStates().contains(rightEdge));
            assertFalse(splitButtonActionButton(splitButton).getStyleClass().contains("m3-split-button-left"));
            assertFalse(splitButtonActionButton(splitButton).getStyleClass().contains("m3-split-button-right"));
            assertFalse(splitButtonMenuButton(splitButton).getStyleClass().contains("m3-split-button-left"));
            assertFalse(splitButtonMenuButton(splitButton).getStyleClass().contains("m3-split-button-right"));
            assertRegionRoundedCorners(splitButtonActionButton(splitButton), true, true, true, true);
            assertRegionRoundedCorners(splitButtonMenuButton(splitButton), true, true, true, true);
            assertStateLayerRadii(splitButtonActionButton(splitButton), 20.0, 4.0, 4.0, 20.0);
            assertStateLayerRadii(splitButtonMenuButton(splitButton), 4.0, 20.0, 20.0, 4.0);
        });
    }

    /// Verifies whether a region's top-left surface corner excludes its two-pixel inset sample.
    private static void assertRenderedTopLeftCorner(
            Region region,
            WritableImage image,
            boolean excludesInset,
            Color background
    ) {
        Bounds bounds = region.localToScene(region.getLayoutBounds());
        Insets insets = firstBackgroundInsets(region);
        Color sample = snapshotScenePixel(
                image,
                bounds.getMinX() + insets.getLeft() + 2.0,
                bounds.getMinY() + insets.getTop() + 2.0
        );
        double distance = colorDistance(sample, background);
        if (excludesInset) {
            assertTrue(distance < 0.05, () -> "expected rounded-away corner sample: distance=" + distance);
        } else {
            assertTrue(distance > 0.05, () -> "expected filled corner sample: distance=" + distance);
        }
    }

    /// Creates a split button with the requested size.
    private static M3SplitButton createSizedSplitButton(M3ButtonSize size) {
        M3SplitButton splitButton = createSplitButton("Create", M3ButtonVariant.TONAL, new M3MenuItem("Draft"));
        splitButton.setSize(size);
        return splitButton;
    }

    /// Asserts split button metrics resolved through CSS.
    private static void assertSplitButtonMetrics(
            M3SplitButton splitButton,
            M3ButtonSize size,
            double containerHeight,
            double actionLeadingSpace,
            double actionTrailingSpace,
            double menuButtonWidth,
            double menuIconSize,
            double spacing
    ) {
        M3Button actionButton = splitButtonActionButton(splitButton);
        M3MenuButton menuButton = splitButtonMenuButton(splitButton);
        M3DisclosureIcon menuIcon = assertInstanceOf(M3DisclosureIcon.class, menuButton.getGraphic());
        assertTrue(splitButton.getStyleClass().contains("m3-split-button-" + size.cssSuffix()));
        assertEquals(containerHeight, actionButton.getContainerHeight(), 0.0001);
        assertEquals(containerHeight, menuButton.getContainerHeight(), 0.0001);
        assertEquals(actionLeadingSpace, actionButton.getPadding().getLeft(), 0.0001);
        assertEquals(actionTrailingSpace, actionButton.getPadding().getRight(), 0.0001);
        assertEquals(menuButtonWidth, menuButton.prefWidth(-1.0), 0.0001);
        assertEquals(menuIconSize, menuIcon.getIconSize(), 0.0001);
        assertEquals(0.0, menuButton.getHorizontalPadding(), 0.0001);
        assertEquals(spacing, splitButton.getSpacing(), 0.0001);
    }

    /// Asserts split-button stateful shape tokens resolved through CSS.
    private static void assertSplitButtonShapeMetrics(
            M3SplitButton splitButton,
            double outerCorner,
            double innerCorner,
            double hoveredInnerCorner,
            double pressedInnerCorner,
            double selectedInnerCorner
    ) {
        assertEquals(outerCorner, splitButton.getOuterCorner(), 0.0001);
        assertEquals(innerCorner, splitButton.getInnerCorner(), 0.0001);
        assertEquals(hoveredInnerCorner, splitButton.getHoveredInnerCorner(), 0.0001);
        assertEquals(pressedInnerCorner, splitButton.getPressedInnerCorner(), 0.0001);
        assertEquals(selectedInnerCorner, splitButton.getSelectedInnerCorner(), 0.0001);
    }
}
