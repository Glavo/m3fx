// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3IconToggleButtonSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies icon button graphics, selection, size, and state styling.
@NotNullByDefault
final class M3IconButtonContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that icon button component token properties are styleable from CSS.
    @Test
    void iconButtonTokensAreStyleable() {
        M3Icon icon = new M3Icon("A");
        M3IconButton button = new M3IconButton(icon);
        button.setStyle("-m3-container-height: 48px; -m3-container-width: 64px; "
                + "-m3-container-shape: 16px; -m3-button-icon-size: 32px;");

        applyCss(button);

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(64.0, button.getContainerWidth(), 0.0001);
        assertEquals(16.0, button.getContainerShape(), 0.0001);
        assertEquals(32.0, button.getIconSize(), 0.0001);
        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(64.0, button.prefWidth(-1.0), 0.0001);
        assertEquals(48.0, button.prefHeight(-1.0), 0.0001);
        assertEquals(64.0, button.maxWidth(-1.0), 0.0001);
        assertEquals(48.0, button.maxHeight(-1.0), 0.0001);
    }

    /// Verifies that icon button size, width, and shape roles resolve to Material metrics.
    @Test
    void iconButtonRolesResolveMaterialMetrics() {
        M3Icon extraSmallIcon = new M3Icon("XS");
        M3Icon smallIcon = new M3Icon("S");
        M3Icon mediumIcon = new M3Icon("M");
        M3Icon largeIcon = new M3Icon("L");
        M3Icon extraLargeIcon = new M3Icon("XL");
        M3IconButton extraSmall = createIconButton(extraSmallIcon, M3ButtonSize.EXTRA_SMALL,
                M3IconButtonWidth.NARROW, M3ButtonShape.ROUND);
        M3IconButton small = createIconButton(smallIcon, M3ButtonSize.SMALL,
                M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND);
        M3IconButton medium = createIconButton(mediumIcon, M3ButtonSize.MEDIUM,
                M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE);
        M3IconButton large = createIconButton(largeIcon, M3ButtonSize.LARGE,
                M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE);
        M3IconButton extraLarge = createIconButton(extraLargeIcon, M3ButtonSize.EXTRA_LARGE,
                M3IconButtonWidth.NARROW, M3ButtonShape.ROUND);
        HBox root = new HBox(extraSmall, small, medium, large, extraLarge);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertIconButtonMetrics(extraSmall, extraSmallIcon, 28.0, 32.0, 20.0, 999.0);
        assertIconButtonMetrics(small, smallIcon, 40.0, 40.0, 24.0, 999.0);
        assertIconButtonMetrics(medium, mediumIcon, 72.0, 56.0, 24.0, 16.0);
        assertIconButtonMetrics(large, largeIcon, 128.0, 96.0, 32.0, 28.0);
        assertIconButtonMetrics(extraLarge, extraLargeIcon, 104.0, 136.0, 40.0, 999.0);

        List<M3IconButton> buttons = List.of(extraSmall, small, medium, large, extraLarge);
        for (M3IconButton button : buttons) {
            double restingShape = button.getContainerShape();
            button.arm();
            root.applyCss();
            assertEquals(restingShape, button.getContainerShape(), 0.0001);
            button.disarm();
            root.applyCss();
            assertEquals(restingShape, button.getContainerShape(), 0.0001);
        }

        FxTestUtils.runOnFxThread(() -> {
            List<M3IconButton> expressiveRoundButtons = new ArrayList<>();
            List<M3IconButton> expressiveSquareButtons = new ArrayList<>();
            for (M3ButtonSize size : M3ButtonSize.values()) {
                expressiveRoundButtons.add(createIconButton(
                        new M3Icon("R"),
                        size,
                        M3IconButtonWidth.DEFAULT,
                        M3ButtonShape.ROUND
                ));
                expressiveSquareButtons.add(createIconButton(
                        new M3Icon("S"),
                        size,
                        M3IconButtonWidth.DEFAULT,
                        M3ButtonShape.SQUARE
                ));
            }
            HBox expressiveRoot = new HBox();
            expressiveRoot.getChildren().addAll(expressiveRoundButtons);
            expressiveRoot.getChildren().addAll(expressiveSquareButtons);
            Scene expressiveScene = new Scene(expressiveRoot, 1800.0, 180.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(expressiveScene, M3Theme.fromSeed(
                        Color.web("#6750a4"),
                        M3Profile.EXPRESSIVE_2025,
                        Brightness.LIGHT
                ));
                stage.setScene(expressiveScene);
                stage.show();
                expressiveRoot.applyCss();
                expressiveRoot.layout();

                double[] expressivePressedShapes = {8.0, 8.0, 12.0, 16.0, 16.0};
                for (int index = 0; index < M3ButtonSize.values().length; index++) {
                    M3IconButton expressiveRound = expressiveRoundButtons.get(index);
                    M3IconButton expressiveSquare = expressiveSquareButtons.get(index);
                    double roundRestingShape = expressiveRound.getContainerShape();
                    double squareRestingShape = expressiveSquare.getContainerShape();

                    expressiveRound.arm();
                    expressiveSquare.arm();
                    expressiveRoot.applyCss();
                    expressiveRoot.layout();
                    assertEquals(expressivePressedShapes[index], expressiveRound.getContainerShape(), 0.0001);
                    assertEquals(expressivePressedShapes[index], expressiveSquare.getContainerShape(), 0.0001);

                    expressiveRound.disarm();
                    expressiveSquare.disarm();
                    expressiveRoot.applyCss();
                    expressiveRoot.layout();
                    assertEquals(roundRestingShape, expressiveRound.getContainerShape(), 0.0001);
                    assertEquals(squareRestingShape, expressiveSquare.getContainerShape(), 0.0001);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that toggle icon button selected and pressed shapes resolve from Material metrics.
    @Test
    void iconToggleButtonRolesResolveMaterialMetrics() {
        M3Icon extraSmallIcon = new M3Icon("XS");
        M3Icon mediumIcon = new M3Icon("M");
        M3Icon largeIcon = new M3Icon("L");
        M3IconToggleButton extraSmall = createIconToggleButton(extraSmallIcon, M3ButtonSize.EXTRA_SMALL,
                M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND, true);
        M3IconToggleButton medium = createIconToggleButton(mediumIcon, M3ButtonSize.MEDIUM,
                M3IconButtonWidth.WIDE, M3ButtonShape.ROUND, true);
        M3IconToggleButton largeSquare = createIconToggleButton(largeIcon, M3ButtonSize.LARGE,
                M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE, true);
        M3IconToggleButton pressedSmall = createIconToggleButton(new M3Icon("P"), M3ButtonSize.SMALL,
                M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND, false);
        pressedSmall.arm();
        HBox root = new HBox(extraSmall, medium, largeSquare, pressedSmall);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#6750a4"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertIconToggleButtonMetrics(extraSmall, extraSmallIcon, 32.0, 32.0, 20.0, 12.0);
        assertIconToggleButtonMetrics(medium, mediumIcon, 72.0, 56.0, 24.0, 16.0);
        assertIconToggleButtonMetrics(largeSquare, largeIcon, 128.0, 96.0, 32.0, 999.0);
        assertEquals(8.0, pressedSmall.getContainerShape(), 0.0001);

        extraSmall.arm();
        largeSquare.arm();
        root.applyCss();
        assertEquals(8.0, extraSmall.getContainerShape(), 0.0001);
        assertEquals(16.0, largeSquare.getContainerShape(), 0.0001);
        extraSmall.disarm();
        largeSquare.disarm();
        root.applyCss();
        assertEquals(12.0, extraSmall.getContainerShape(), 0.0001);
        assertEquals(999.0, largeSquare.getContainerShape(), 0.0001);

        pressedSmall.disarm();
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        assertEquals(999.0, extraSmall.getContainerShape(), 0.0001);
        assertEquals(999.0, medium.getContainerShape(), 0.0001);
        assertEquals(28.0, largeSquare.getContainerShape(), 0.0001);
        assertEquals(999.0, pressedSmall.getContainerShape(), 0.0001);

        pressedSmall.arm();
        root.applyCss();
        assertEquals(999.0, pressedSmall.getContainerShape(), 0.0001);
        pressedSmall.disarm();
        root.applyCss();
        assertEquals(999.0, pressedSmall.getContainerShape(), 0.0001);
    }

    /// Verifies selected and pressed Toggle icon-button shapes across every size and Material profile.
    @Test
    void iconToggleButtonShapesFollowProfilesAcrossEverySize() {
        FxTestUtils.runOnFxThread(() -> {
            List<M3IconToggleButton> roundButtons = new ArrayList<>();
            List<M3IconToggleButton> squareButtons = new ArrayList<>();
            for (M3ButtonSize size : M3ButtonSize.values()) {
                roundButtons.add(createIconToggleButton(
                        new M3Icon("R"),
                        size,
                        M3IconButtonWidth.DEFAULT,
                        M3ButtonShape.ROUND,
                        true
                ));
                squareButtons.add(createIconToggleButton(
                        new M3Icon("S"),
                        size,
                        M3IconButtonWidth.DEFAULT,
                        M3ButtonShape.SQUARE,
                        true
                ));
            }

            HBox root = new HBox(8.0);
            root.getChildren().addAll(roundButtons);
            root.getChildren().addAll(squareButtons);
            Scene scene = new Scene(root, 1800.0, 180.0);
            List<M3Theme> themes = List.of(
                    M3Theme.fromSeed(
                            Color.web("#6750a4"),
                            M3Profile.EXPRESSIVE_2025,
                            Brightness.LIGHT
                    ),
                    M3Theme.defaultTheme()
            );
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                for (M3Theme theme : themes) {
                    M3ThemeManager.install(scene, theme);
                    root.applyCss();
                    root.layout();
                    boolean expressive = theme.profile() == M3Profile.EXPRESSIVE_2025;

                    for (int index = 0; index < M3ButtonSize.values().length; index++) {
                        M3ButtonSize size = M3ButtonSize.values()[index];
                        M3ComponentTokens.IconButtonSizeTokens tokens = switch (size) {
                            case EXTRA_SMALL -> theme.tokens().componentTokens().iconButton().extraSmall();
                            case SMALL -> theme.tokens().componentTokens().iconButton().small();
                            case MEDIUM -> theme.tokens().componentTokens().iconButton().medium();
                            case LARGE -> theme.tokens().componentTokens().iconButton().large();
                            case EXTRA_LARGE -> theme.tokens().componentTokens().iconButton().extraLarge();
                        };
                        M3IconToggleButton round = roundButtons.get(index);
                        M3IconToggleButton square = squareButtons.get(index);
                        double selectedRoundShape = tokens.selectedRoundContainerShape();
                        double selectedSquareShape = tokens.selectedSquareContainerShape();

                        assertTrue(round.isSelected());
                        assertTrue(square.isSelected());
                        assertEquals(selectedRoundShape, round.getContainerShape(), 0.0001);
                        assertEquals(selectedSquareShape, square.getContainerShape(), 0.0001);

                        round.arm();
                        square.arm();
                        root.applyCss();
                        double pressedRoundShape = tokens.pressedRoundContainerShape();
                        double pressedSquareShape = tokens.pressedSquareContainerShape();
                        assertEquals(pressedRoundShape, round.getContainerShape(), 0.0001);
                        assertEquals(pressedSquareShape, square.getContainerShape(), 0.0001);

                        round.disarm();
                        square.disarm();
                        root.applyCss();
                        assertEquals(selectedRoundShape, round.getContainerShape(), 0.0001);
                        assertEquals(selectedSquareShape, square.getContainerShape(), 0.0001);
                    }
                }
            } finally {
                stage.close();
            }
        });
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
        assertTrue(button.getStyleClass().contains(ICON_TOGGLE_BUTTON_STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3IconToggleButtonVariant.TONAL.styleClass()));

        button.setVariant(M3IconToggleButtonVariant.OUTLINED);
        button.setSelected(false);

        assertEquals(M3IconToggleButtonVariant.OUTLINED, button.getVariant());
        assertFalse(button.isSelected());
        assertTrue(button.getStyleClass().contains(M3IconToggleButtonVariant.OUTLINED.styleClass()));
        assertFalse(button.getStyleClass().contains(M3IconToggleButtonVariant.TONAL.styleClass()));
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
        M3Icon icon = new M3Icon("A");
        M3IconToggleButton button = new M3IconToggleButton(icon);
        button.setStyle("-m3-container-height: 48px; -m3-container-width: 64px; "
                + "-m3-container-shape: 12px; -m3-icon-button-icon-size: 32px;");

        applyCss(button);

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(64.0, button.getContainerWidth(), 0.0001);
        assertEquals(12.0, button.getContainerShape(), 0.0001);
        assertEquals(32.0, button.getIconSize(), 0.0001);
        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(64.0, button.prefWidth(-1.0), 0.0001);
        assertEquals(48.0, button.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that toggle icon button graphics stay centered in their fixed touch target.
    @Test
    void iconToggleButtonCentersGraphicContent() {
        FxTestUtils.runOnFxThread(() -> {
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
            assertFixedTargetControlsKeepCenteredContent(row);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-icon-toggle-centering.png"
            ));
        });
    }

    /// Verifies resolved icon button geometry and icon metrics.
    private static void assertIconButtonMetrics(
            M3IconButton button,
            M3Icon icon,
            double expectedWidth,
            double expectedHeight,
            double expectedIconSize,
            double expectedShape
    ) {
        assertEquals(expectedWidth, button.getContainerWidth(), 0.0001);
        assertEquals(expectedHeight, button.getContainerHeight(), 0.0001);
        assertEquals(expectedIconSize, button.getIconSize(), 0.0001);
        assertEquals(expectedIconSize, icon.getIconSize(), 0.0001);
        assertEquals(expectedShape, button.getContainerShape(), 0.0001);
        assertEquals(Math.max(48.0, expectedWidth), button.prefWidth(-1.0), 0.0001);
        assertEquals(Math.max(48.0, expectedHeight), button.prefHeight(-1.0), 0.0001);
    }

    /// Verifies resolved toggle icon button geometry and icon metrics.
    private static void assertIconToggleButtonMetrics(
            M3IconToggleButton button,
            M3Icon icon,
            double expectedWidth,
            double expectedHeight,
            double expectedIconSize,
            double expectedShape
    ) {
        assertEquals(expectedWidth, button.getContainerWidth(), 0.0001);
        assertEquals(expectedHeight, button.getContainerHeight(), 0.0001);
        assertEquals(expectedIconSize, button.getIconSize(), 0.0001);
        assertEquals(expectedIconSize, icon.getIconSize(), 0.0001);
        assertEquals(expectedShape, button.getContainerShape(), 0.0001);
        assertEquals(Math.max(48.0, expectedWidth), button.prefWidth(-1.0), 0.0001);
        assertEquals(Math.max(48.0, expectedHeight), button.prefHeight(-1.0), 0.0001);
    }
}
