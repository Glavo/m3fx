// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the selected-state graphic replacement contract of [M3SegmentedButtonSkin].
@NotNullByDefault
final class M3SegmentedButtonSkinTest {
    /// The private skin state applied to a graphic while the selected check replaces it.
    private static final PseudoClass GRAPHIC_REPLACED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("m3-segmented-button-graphic-replaced");

    /// Starts the JavaFX toolkit before tests create controls and real windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies replacement state, direct CSS invalidation, graphic transfer, and disposal cleanup.
    @Test
    void replacementStateFollowsSelectionConfigurationAndGraphicLifecycle() {
        FxTestUtils.runOnFxThread(() -> {
            Region originalGraphic = applicationGraphic("original-graphic");
            List<String> originalStyleClasses = List.copyOf(originalGraphic.getStyleClass());
            String originalStyle = originalGraphic.getStyle();
            Object applicationMarker = new Object();
            originalGraphic.getProperties().put("application-marker", applicationMarker);

            M3SegmentedButton button = new M3SegmentedButton("Low", originalGraphic);
            M3SegmentedButton textOnlyButton = new M3SegmentedButton("Medium");
            HBox root = new HBox(button, textOnlyButton);
            Scene scene = new Scene(root, 240.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            M3MotionSettings.setReducedMotionRequested(root, true);
            root.applyCss();
            root.layout();
            button.layout();

            M3SegmentedButtonSkin skin = assertInstanceOf(M3SegmentedButtonSkin.class, button.getSkin());
            double prefWidth = button.prefWidth(-1.0);
            assertFalse(isGraphicReplaced(originalGraphic));
            assertEquals(1.0, originalGraphic.getOpacity(), 0.0001);

            textOnlyButton.setSelected(true);
            assertEquals(1.0, selectionIndicator(textOnlyButton).getOpacity(), 0.0001);

            button.setSelected(true);
            originalGraphic.applyCss();
            root.layout();
            button.layout();

            Region indicator = selectionIndicator(button);
            Bounds graphicBounds = originalGraphic.getBoundsInParent();
            Bounds indicatorBounds = indicator.getBoundsInParent();
            assertTrue(isGraphicReplaced(originalGraphic));
            assertEquals(0.0, originalGraphic.getOpacity(), 0.0001);
            assertEquals(graphicBounds.getCenterX(), indicatorBounds.getCenterX(), 0.0001);
            assertEquals(graphicBounds.getCenterY(), indicatorBounds.getCenterY(), 0.0001);
            assertEquals(prefWidth, button.prefWidth(-1.0), 0.0001);
            assertApplicationStatePreserved(
                    originalGraphic,
                    originalStyleClasses,
                    originalStyle,
                    applicationMarker
            );

            button.setSelectionIndicatorEnabled(false);
            originalGraphic.applyCss();
            assertFalse(isGraphicReplaced(originalGraphic));
            assertEquals(1.0, originalGraphic.getOpacity(), 0.0001);

            button.setSelectionIndicatorEnabled(true);
            originalGraphic.applyCss();
            assertTrue(isGraphicReplaced(originalGraphic));
            assertEquals(0.0, originalGraphic.getOpacity(), 0.0001);

            Region replacementGraphic = applicationGraphic("replacement-graphic");
            List<String> replacementStyleClasses = List.copyOf(replacementGraphic.getStyleClass());
            button.setGraphic(replacementGraphic);
            assertFalse(isGraphicReplaced(originalGraphic));
            assertTrue(isGraphicReplaced(replacementGraphic));
            assertIterableEquals(originalStyleClasses, originalGraphic.getStyleClass());
            assertIterableEquals(replacementStyleClasses, replacementGraphic.getStyleClass());

            button.setGraphic(originalGraphic);
            assertFalse(isGraphicReplaced(replacementGraphic));
            assertTrue(isGraphicReplaced(originalGraphic));

            skin.dispose();
            assertFalse(isGraphicReplaced(originalGraphic));
            assertApplicationStatePreserved(
                    originalGraphic,
                    originalStyleClasses,
                    originalStyle,
                    applicationMarker
            );
        });
    }

    /// Verifies that a live scene applies replacement CSS after selection changes without a full root CSS pass.
    @Tier2Test
    @Test
    void liveSceneSelectionReplacesGraphicOnTheNextCssPulse() throws InterruptedException {
        LiveFixture fixture = createLiveFixture();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> isGraphicReplaced(fixture.graphic())
                            && fixture.graphic().getOpacity() == 0.0
                            && selectionIndicator(fixture.button()).getOpacity() == 1.0,
                    2,
                    () -> fixture.button().setSelected(true),
                    () -> {
                        Region indicator = selectionIndicator(fixture.button());
                        Bounds graphicBounds = fixture.graphic().getBoundsInParent();
                        Bounds indicatorBounds = indicator.getBoundsInParent();
                        assertEquals(graphicBounds.getCenterX(), indicatorBounds.getCenterX(), 0.0001);
                        assertEquals(graphicBounds.getCenterY(), indicatorBounds.getCenterY(), 0.0001);
                        assertEquals(fixture.buttonWidth(), fixture.button().getWidth(), 0.0001);
                        assertTrue(fixture.graphic().getStyleClass().contains("application-graphic"));
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> !isGraphicReplaced(fixture.graphic()) && fixture.graphic().getOpacity() == 1.0,
                    2,
                    () -> fixture.button().setSelected(false),
                    () -> {
                        assertEquals(fixture.buttonWidth(), fixture.button().getWidth(), 0.0001);
                        assertTrue(fixture.graphic().getStyleClass().contains("application-graphic"));
                    }
            );
        } finally {
            fixture.close();
        }
    }

    /// Creates an application-owned graphic used by replacement tests.
    ///
    /// @param id the application-owned node identifier
    /// @return the new graphic
    private static Region applicationGraphic(String id) {
        Region graphic = new Region();
        graphic.setId(id);
        graphic.getStyleClass().add("application-graphic");
        graphic.setStyle("-fx-min-width: 18px; -fx-min-height: 18px;");
        graphic.setPrefSize(18.0, 18.0);
        return graphic;
    }

    /// Returns whether a graphic carries the skin-managed replacement state.
    ///
    /// @param graphic the graphic to inspect
    /// @return whether the selected check currently replaces the graphic
    private static boolean isGraphicReplaced(Region graphic) {
        return graphic.getPseudoClassStates().contains(GRAPHIC_REPLACED_PSEUDO_CLASS);
    }

    /// Returns the built-in selected-state check for a segmented button.
    ///
    /// @param button the button whose indicator is requested
    /// @return the indicator region
    private static Region selectionIndicator(M3SegmentedButton button) {
        return assertInstanceOf(
                Region.class,
                button.lookup("." + M3SegmentedButtonSkin.SELECTION_INDICATOR_STYLE_CLASS)
        );
    }

    /// Verifies that skin state changes did not alter application-owned graphic state.
    ///
    /// @param graphic     the graphic to inspect
    /// @param styleClasses the expected style classes
    /// @param style        the expected inline style
    /// @param marker       the expected application property value
    private static void assertApplicationStatePreserved(
            Region graphic,
            List<String> styleClasses,
            String style,
            Object marker
    ) {
        assertIterableEquals(styleClasses, graphic.getStyleClass());
        assertEquals(style, graphic.getStyle());
        assertSame(marker, graphic.getProperties().get("application-marker"));
    }

    /// Creates and initially styles a real-window segmented button without selecting it.
    ///
    /// @return the live fixture
    private static LiveFixture createLiveFixture() {
        return FxTestUtils.callOnFxThread(() -> {
            Region graphic = applicationGraphic("live-graphic");
            M3SegmentedButton button = new M3SegmentedButton("Low", graphic);
            HBox root = new HBox(button);
            Scene scene = new Scene(root, 240.0, 80.0);
            Stage stage = new Stage();

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();
            button.layout();

            assertFalse(button.isSelected());
            assertFalse(isGraphicReplaced(graphic));
            assertEquals(1.0, graphic.getOpacity(), 0.0001);
            return new LiveFixture(stage, root, button, graphic, button.getWidth());
        });
    }

    /// Holds the nodes in a live-scene CSS invalidation test.
    ///
    /// @param stage       the real test window
    /// @param root        the scene root
    /// @param button      the segmented button under test
    /// @param graphic     the application-owned graphic
    /// @param buttonWidth the stable laid-out button width
    private record LiveFixture(
            Stage stage,
            HBox root,
            M3SegmentedButton button,
            Region graphic,
            double buttonWidth
    ) {
        /// Closes the test window and detaches its scene content.
        private void close() {
            FxTestUtils.runOnFxThread(() -> {
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.close();
            });
        }
    }
}
