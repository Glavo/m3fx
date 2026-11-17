// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies icon button shape targets across sizes, profiles, and interaction states.
@NotNullByDefault
final class M3IconButtonShapeTest {
    /// Starts JavaFX before applying CSS to detached scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Supplies independent expected radii for fallback, baseline, and Expressive styling.
    private static Stream<Arguments> shapes() {
        return Stream.<@Nullable M3Profile>of(null, M3Profile.BASELINE_2021, M3Profile.EXPRESSIVE_2025)
                .flatMap(profile -> Stream.of(
                        Arguments.of(profile, M3ButtonSize.EXTRA_SMALL, 12.0, 8.0),
                        Arguments.of(profile, M3ButtonSize.SMALL, 12.0, 8.0),
                        Arguments.of(profile, M3ButtonSize.MEDIUM, 16.0, 12.0),
                        Arguments.of(profile, M3ButtonSize.LARGE, 28.0, 16.0),
                        Arguments.of(profile, M3ButtonSize.EXTRA_LARGE, 28.0, 16.0)
                ));
    }

    /// Verifies resting, pressed, selected, released, and theme-replaced CSS targets.
    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("shapes")
    void shapeTargetsFollowInteractionState(
            @Nullable M3Profile profile,
            M3ButtonSize size,
            double squareShape,
            double expressivePressedShape
    ) {
        FxTestUtils.runOnFxThread(() -> {
            M3IconButton round = new M3IconButton();
            M3IconButton square = new M3IconButton();
            M3IconToggleButton roundToggle = new M3IconToggleButton();
            M3IconToggleButton squareToggle = new M3IconToggleButton();
            round.setSize(size);
            square.setSize(size);
            roundToggle.setSize(size);
            squareToggle.setSize(size);
            square.setButtonShape(M3ButtonShape.SQUARE);
            squareToggle.setButtonShape(M3ButtonShape.SQUARE);
            round.setAnimationsEnabled(false);
            square.setAnimationsEnabled(false);
            roundToggle.setAnimationsEnabled(false);
            squareToggle.setAnimationsEnabled(false);
            HBox root = new HBox(round, square, roundToggle, squareToggle);
            Scene scene = new Scene(root);
            try {
                if (profile != null) {
                    M3ThemeManager.install(scene, M3Theme.fromSeed(Color.web("#6750a4"), profile, Brightness.LIGHT));
                }
                root.applyCss();
                assertEquals(999.0, round.getContainerShape(), 0.0001);
                assertEquals(squareShape, square.getContainerShape(), 0.0001);
                assertEquals(999.0, roundToggle.getContainerShape(), 0.0001);
                assertEquals(squareShape, squareToggle.getContainerShape(), 0.0001);

                boolean expressive = profile == M3Profile.EXPRESSIVE_2025;
                double pressedRound = expressive ? expressivePressedShape : 999.0;
                double pressedSquare = expressive ? expressivePressedShape : squareShape;
                List<ButtonBase> buttons = List.of(round, square, roundToggle, squareToggle);
                for (boolean selected : new boolean[]{false, true, false}) {
                    roundToggle.setSelected(selected);
                    squareToggle.setSelected(selected);
                    root.applyCss();
                    double selectedRound = expressive && selected ? squareShape : 999.0;
                    double selectedSquare = expressive && selected ? 999.0 : squareShape;
                    assertEquals(selectedRound, roundToggle.getContainerShape(), 0.0001);
                    assertEquals(selectedSquare, squareToggle.getContainerShape(), 0.0001);

                    buttons.forEach(ButtonBase::arm);
                    root.applyCss();
                    assertTrue(buttons.stream().allMatch(ButtonBase::isArmed));
                    assertEquals(pressedRound, round.getContainerShape(), 0.0001);
                    assertEquals(pressedSquare, square.getContainerShape(), 0.0001);
                    assertEquals(pressedRound, roundToggle.getContainerShape(), 0.0001);
                    assertEquals(pressedSquare, squareToggle.getContainerShape(), 0.0001);

                    buttons.forEach(ButtonBase::disarm);
                    root.applyCss();
                    assertEquals(999.0, round.getContainerShape(), 0.0001);
                    assertEquals(squareShape, square.getContainerShape(), 0.0001);
                    assertEquals(selectedRound, roundToggle.getContainerShape(), 0.0001);
                    assertEquals(selectedSquare, squareToggle.getContainerShape(), 0.0001);
                }

                roundToggle.setSelected(true);
                squareToggle.setSelected(true);
                buttons.forEach(ButtonBase::arm);
                root.applyCss();
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                root.applyCss();
                assertEquals(999.0, round.getContainerShape(), 0.0001);
                assertEquals(squareShape, square.getContainerShape(), 0.0001);
                assertEquals(999.0, roundToggle.getContainerShape(), 0.0001);
                assertEquals(squareShape, squareToggle.getContainerShape(), 0.0001);
                buttons.forEach(ButtonBase::disarm);
                root.applyCss();
                assertEquals(999.0, roundToggle.getContainerShape(), 0.0001);
                assertEquals(squareShape, squareToggle.getContainerShape(), 0.0001);
            } finally {
                M3ThemeManager.uninstall(scene);
                scene.setRoot(new Pane());
            }
        });
    }
}
