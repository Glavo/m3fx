// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3TooltipRegistry;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies tooltip lifetime, focus, interaction, and presentation.
@NotNullByDefault
final class M3TooltipContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// The pulse count used after a rich tooltip popup takes pointer ownership.
    private static final int RICH_TOOLTIP_POPUP_OWNERSHIP_STABLE_PULSES = 2;

    /// Verifies that m3fx tooltips expose style and timing defaults.
    @Test
    void tooltipUsesMaterialDefaults() {
        M3Tooltip tooltip = new M3Tooltip("Details");

        assertTrue(tooltip.getStyleClass().contains("m3-tooltip"));
        assertTrue(tooltip.isWrapText());
        assertEquals(Duration.millis(500.0), tooltip.getShowDelay());
        assertEquals(Duration.millis(1500.0), tooltip.getHideDelay());
        assertEquals(Duration.seconds(5.0), tooltip.getShowDuration());
    }

    /// Verifies that tooltips provide Material Design 3 install helpers.
    @Test
    void tooltipInstallsOnNodes() {
        Label target = new Label("Target");
        M3Tooltip tooltip = installTooltip(target, "Installed");

        assertEquals("Installed", tooltip.getText());
        assertTrue(tooltip.getStyleClass().contains("m3-tooltip"));
        assertEquals("Installed", target.getAccessibleHelp());

        tooltip.setText("Updated");
        assertEquals("Updated", target.getAccessibleHelp());

        M3Tooltip.uninstall(target, tooltip);
        assertNull(target.getAccessibleHelp());

        Label targetWithHelp = new Label("Target");
        targetWithHelp.setAccessibleHelp("Existing help");
        M3Tooltip restoredTooltip = installTooltip(targetWithHelp, "Temporary help");

        assertEquals("Temporary help", targetWithHelp.getAccessibleHelp());

        M3Tooltip.uninstall(targetWithHelp, restoredTooltip);
        assertEquals("Existing help", targetWithHelp.getAccessibleHelp());
    }

    /// Verifies that uninstalling a replaced tooltip cannot tear down the current node installation.
    @Test
    void tooltipReplacementRetainsCurrentInstallationOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            target.setAccessibleHelp("Existing help");
            Pane root = new Pane(target);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            M3ThemeManager.install(root, baselineTheme);
            M3Tooltip replacedTooltip = new M3Tooltip("Replaced");
            M3Tooltip currentTooltip = new M3Tooltip("Current");

            M3Tooltip.install(target, replacedTooltip);
            M3Tooltip.install(target, currentTooltip);

            assertEquals("Current", target.getAccessibleHelp());
            assertSame(baselineTheme, currentTooltip.getTheme());

            replacedTooltip.setText("Stale replacement");

            assertEquals("Current", target.getAccessibleHelp());

            M3Tooltip.uninstall(target, replacedTooltip);
            currentTooltip.setText("Updated current");
            M3ThemeManager.install(root, expressiveTheme);

            assertEquals("Updated current", target.getAccessibleHelp());
            assertSame(expressiveTheme, currentTooltip.getTheme());

            M3Tooltip.uninstall(target, currentTooltip);

            assertEquals("Existing help", target.getAccessibleHelp());
            currentTooltip.setText("Detached current");
            assertEquals("Existing help", target.getAccessibleHelp());
            M3ThemeManager.uninstall(root);
        });
    }

    /// Verifies that installed tooltips open from keyboard focus and close from Escape.
    @Tier2Test
    @Test
    void tooltipInstalledOnFocusSupportsKeyboardDismissal() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> tooltipShowingWithFocus(tooltipReference, targetReference),
                () -> {
                    Label target = new Label("Target");
                    target.setFocusTraversable(true);
                    M3Tooltip tooltip = installTooltip(target, "Installed");
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    FxTestUtils.setCssAnimationsEnabled(target, false);

                    Stage stage = new Stage();
                    Pane root = new Pane(target);
                    stage.setScene(new Scene(root, 240.0, 120.0));
                    stage.show();
                    assertTrue(M3FocusTraversal.handleCyclicTabKeyFocus(
                            root,
                            keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB),
                            List.of(target)
                    ));

                    stageReference.set(stage);
                    targetReference.set(target);
                    tooltipReference.set(tooltip);
                },
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    Label target = Objects.requireNonNull(targetReference.get(), "target");
                    M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                    try {
                        assertTrue(target.isFocused());
                        assertTrue(tooltip.isShowing());

                        target.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                        assertFalse(tooltip.isShowing());
                    } finally {
                        tooltip.hide();
                        FxTestUtils.setCssAnimationsEnabled(target, true);
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that installed tooltip popups inherit the owner's local motion settings.
    @Tier2Test
    @Test
    void tooltipPopupInheritsOwnerMotionSettings() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> actionReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> tooltipShowing(tooltipReference),
                () -> {
                    Stage stage = new Stage();
                    Label target = new Label("Target");
                    M3Button action = createButton("Action", M3ButtonVariant.TEXT);
                    M3RichTooltip tooltip = installRichTooltip(
                            target,
                            "Title",
                            "Supporting text",
                            action
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);

                    Pane root = new Pane(target);
                    FxTestUtils.setCssAnimationsEnabled(root, false);
                    FxTestUtils.setMotionScheme(root, M3MotionScheme.expressive());
                    FxTestUtils.setMotionBehavior(root, M3MotionBehavior.expressive());
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
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                    M3Button action = Objects.requireNonNull(actionReference.get(), "action");
                    try {
                        assertTrue(tooltip.isShowing());
                        Node tooltipRoot = tooltip.getScene().getRoot();

                        assertTrue(M3MotionSettings.shouldReduceMotion(tooltipRoot));
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

    /// Verifies that installed tooltip popups follow owner node orientation changes while showing.
    @Test
    void tooltipPopupSynchronizesOwnerNodeOrientationWhileShowing() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            target.setMinSize(96.0, 32.0);
            M3Button action = createButton("Action", M3ButtonVariant.TEXT);
            M3RichTooltip tooltip = installRichTooltip(target, "Title", "Supporting text", action);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(target);
                Scene scene = new Scene(root, 320.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                tooltip.show(target, stage.getX() + 80.0, stage.getY() + 80.0);
                Node tooltipRoot = Objects.requireNonNull(tooltip.getScene(), "tooltip scene").getRoot();

                assertEquals(NodeOrientation.LEFT_TO_RIGHT, tooltipRoot.getNodeOrientation());
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, action.getEffectiveNodeOrientation());

                target.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                tooltipRoot.applyCss();

                assertEquals(NodeOrientation.RIGHT_TO_LEFT, tooltipRoot.getNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, action.getEffectiveNodeOrientation());

                target.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                tooltipRoot.applyCss();

                assertEquals(NodeOrientation.LEFT_TO_RIGHT, tooltipRoot.getNodeOrientation());
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, action.getEffectiveNodeOrientation());
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(target, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies that installed tooltip show delays refresh while a pointer-triggered open is pending.
    @Tier2Test
    @Test
    void tooltipShowDelayRefreshUsesThemeBehavior() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> tooltipShowing(tooltipReference),
                    () -> {
                        Label target = new Label("Target");
                        target.setMinSize(80.0, 32.0);
                        M3Tooltip tooltip = installTooltip(target, "Installed");
                        VBox root = new VBox(target);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 240.0, 120.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setMotionBehavior(root, tooltipBehavior(
                                Duration.seconds(30.0),
                                Duration.ZERO,
                                Duration.seconds(30.0)
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();

                        target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));
                        assertFalse(tooltip.isShowing());

                        stageReference.set(stage);
                        rootReference.set(root);
                        targetReference.set(target);
                        tooltipReference.set(tooltip);
                        FxTestUtils.setMotionBehavior(root, tooltipBehavior(
                                Duration.millis(40.0),
                                Duration.ZERO,
                                Duration.seconds(30.0)
                        ));
                    },
                    () -> assertTrue(Objects.requireNonNull(tooltipReference.get(), "tooltip").isShowing())
            );
        } finally {
            closeInstalledTooltipScene(stageReference, rootReference, targetReference, tooltipReference);
        }
    }

    /// Verifies that installed tooltip visible durations refresh while a pointer-triggered tooltip is showing.
    @Tier2Test
    @Test
    void tooltipShowDurationRefreshUsesThemeBehavior() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> tooltipShowing(tooltipReference),
                    () -> {
                        Label target = new Label("Target");
                        target.setMinSize(80.0, 32.0);
                        M3Tooltip tooltip = installTooltip(target, "Installed");
                        VBox root = new VBox(target);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 240.0, 120.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setMotionBehavior(root, tooltipBehavior(
                                Duration.ZERO,
                                Duration.ZERO,
                                Duration.seconds(30.0)
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();

                        stageReference.set(stage);
                        rootReference.set(root);
                        targetReference.set(target);
                        tooltipReference.set(tooltip);
                        target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                        assertTrue(tooltip.isShowing());
                        FxTestUtils.setMotionBehavior(root, tooltipBehavior(
                                Duration.ZERO,
                                Duration.ZERO,
                                Duration.millis(40.0)
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> tooltipHidden(tooltipReference),
                    () -> {
                    },
                    () -> assertFalse(Objects.requireNonNull(tooltipReference.get(), "tooltip").isShowing())
            );
        } finally {
            closeInstalledTooltipScene(stageReference, rootReference, targetReference, tooltipReference);
        }
    }

    /// Verifies that installed tooltip hide delays refresh while a pointer-triggered close is pending.
    @Tier2Test
    @Test
    void tooltipHideDelayRefreshUsesThemeBehavior() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> tooltipShowing(tooltipReference),
                    () -> {
                        Label target = new Label("Target");
                        target.setMinSize(80.0, 32.0);
                        M3Tooltip tooltip = installTooltip(target, "Installed");
                        VBox root = new VBox(target);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 240.0, 120.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setMotionBehavior(root, tooltipBehavior(
                                Duration.ZERO,
                                Duration.seconds(30.0),
                                Duration.seconds(30.0)
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();

                        stageReference.set(stage);
                        rootReference.set(root);
                        targetReference.set(target);
                        tooltipReference.set(tooltip);
                        target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        Label target = Objects.requireNonNull(targetReference.get(), "target");
                        M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                        assertTrue(tooltip.isShowing());

                        target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 4.0, 4.0, false));
                        assertTrue(tooltip.isShowing());
                        FxTestUtils.setMotionBehavior(root, tooltipBehavior(
                                Duration.ZERO,
                                Duration.millis(40.0),
                                Duration.seconds(30.0)
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> tooltipHidden(tooltipReference),
                    () -> {
                    },
                    () -> assertFalse(Objects.requireNonNull(tooltipReference.get(), "tooltip").isShowing())
            );
        } finally {
            closeInstalledTooltipScene(stageReference, rootReference, targetReference, tooltipReference);
        }
    }

    /// Verifies that rich tooltips expose graphic-only Material content.
    @Test
    void richTooltipUsesMaterialGraphicContent() {
        Label action = new Label("Action");
        M3RichTooltip tooltip = richTooltip("Title", "Supporting text", action);

        assertTrue(tooltip.getStyleClass().contains("m3-tooltip"));
        assertTrue(tooltip.getStyleClass().contains("m3-rich-tooltip"));
        assertEquals(ContentDisplay.GRAPHIC_ONLY, tooltip.getContentDisplay());
        assertEquals(Duration.millis(1500.0), tooltip.getHideDelay());
        assertEquals("Title", tooltip.getTitle());
        assertEquals("Supporting text", tooltip.getSupportingText());
        assertEquals("Title Supporting text", tooltip.getText());
        assertEquals(action, tooltip.getActions().get(0));
        assertInstanceOf(VBox.class, tooltip.getGraphic());
        assertFalse(tooltip.isPersistent());
        tooltip.setPersistent(true);
        assertTrue(tooltip.isPersistent());
        assertTrue(tooltip.persistentProperty().get());

        tooltip.setTitle("");
        assertEquals("Supporting text", tooltip.getText());
        tooltip.setSupportingText("");
        assertEquals("", tooltip.getText());

        Label replacement = new Label("Replacement");
        tooltip.getActions().setAll(replacement);
        assertEquals(replacement, tooltip.getActions().get(0));
        tooltip.getActions().clear();
        assertTrue(tooltip.getActions().isEmpty());

        assertThrows(NullPointerException.class, () -> tooltip.setTitle(null));
        assertThrows(NullPointerException.class, () -> tooltip.setSupportingText(null));
        assertThrows(NullPointerException.class, () -> tooltip.getActions().add(null));
    }

    /// Verifies that rich tooltip token CSS leaves caller-provided button styles untouched.
    @Tier2Test
    @Test
    void richTooltipTokenCssDoesNotRewriteActionButtonInlineStyle() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Button action = new M3Button("Action");
            action.setStyle("-m3-horizontal-padding: 20px;");
            M3RichTooltip tooltip = richTooltip("Title", "Supporting text", action);
            M3Theme expressiveTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );

            try {
                Label owner = new Label("Owner");
                StackPane ownerRoot = new StackPane(owner);
                Scene scene = new Scene(ownerRoot, 320.0, 160.0);
                M3ThemeManager.install(scene, expressiveTheme);
                stage.setScene(scene);
                stage.show();

                tooltip.setTheme(expressiveTheme);
                tooltip.show(owner, stage.getX() + 24.0, stage.getY() + 48.0);
                tooltip.getScene().getRoot().applyCss();

                assertEquals("-m3-horizontal-padding: 20px;", action.getStyle());
                assertEquals(32.0, action.getContainerHeight(), 0.0001);
                assertEquals(20.0, action.getHorizontalPadding(), 0.0001);

                tooltip.setTheme(null);
                assertEquals("-m3-horizontal-padding: 20px;", action.getStyle());

                tooltip.setTheme(expressiveTheme);
                tooltip.getActions().clear();
                assertEquals("-m3-horizontal-padding: 20px;", action.getStyle());
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that tooltip component tokens retain the Material metrics in an Expressive theme.
    @Test
    void tooltipRetainsMaterialMetricsInExpressiveTheme() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Tooltip plainTooltip = new M3Tooltip("Details");
            M3RichTooltip richTooltip = richTooltip("Title", "Supporting text", new M3Button("Action"));
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
                Parent plainSkinRoot = assertInstanceOf(Parent.class, plainTooltip.getSkin().getNode());
                Region plainRoot = assertInstanceOf(
                        Region.class,
                        plainSkinRoot.lookup("." + "m3-tooltip")
                );
                assertEquals(4.0, plainRoot.getPadding().getTop(), 0.0001);
                assertEquals(8.0, plainRoot.getPadding().getLeft(), 0.0001);
                assertTrue(plainTooltip.getHeight() >= 24.0);
                assertEquals(
                        6.0,
                        plainRoot.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                        0.0001
                );

                richTooltip.setTheme(expressiveTheme);
                richTooltip.show(owner, stage.getX() + 24.0, stage.getY() + 88.0);
                richTooltip.getScene().getRoot().applyCss();
                Parent richRoot = assertInstanceOf(Parent.class, richTooltip.getSkin().getNode());
                VBox richContainer = assertInstanceOf(
                        VBox.class,
                        richRoot.lookup("." + "m3-rich-tooltip-container")
                );
                assertEquals(12.0, richContainer.getPadding().getTop(), 0.0001);
                assertEquals(16.0, richContainer.getPadding().getLeft(), 0.0001);
                assertEquals(8.0, richContainer.getSpacing(), 0.0001);
                assertEquals(320.0, richContainer.getPrefWidth(), 0.0001);
                assertEquals(
                        16.0,
                        richContainer.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                        0.0001
                );
                HBox actions = assertInstanceOf(HBox.class, richContainer.lookup("." + "m3-rich-tooltip-actions"));
                M3Button actionButton = assertInstanceOf(M3Button.class, richTooltip.getActions().get(0));
                assertEquals(8.0, actions.getSpacing(), 0.0001);
                assertEquals(32.0, actionButton.getContainerHeight(), 0.0001);
                assertEquals(12.0, actionButton.getHorizontalPadding(), 0.0001);
            } finally {
                plainTooltip.hide();
                richTooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltips install on nodes and expose combined accessible help.
    @Test
    void richTooltipInstallsOnNodes() {
        Label target = new Label("Target");
        M3RichTooltip tooltip = installRichTooltip(target, "Title", "Supporting text");

        assertEquals("Title Supporting text", target.getAccessibleHelp());

        tooltip.setTitle("Updated");
        assertEquals("Updated Supporting text", target.getAccessibleHelp());

        tooltip.setSupportingText("");
        assertEquals("Updated", target.getAccessibleHelp());

        M3Tooltip.uninstall(target, tooltip);
        assertNull(target.getAccessibleHelp());
    }

    /// Verifies explicit persistent activation, pointer-exit retention, and the one-tooltip-at-a-time rule.
    @Tier2Test
    @Test
    void persistentRichTooltipUsesExplicitExclusiveActivation() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Button firstOwner = new M3Button("First");
            M3Button secondOwner = new M3Button("Second");
            M3RichTooltip firstTooltip = new M3RichTooltip("First details", "Persistent help");
            M3RichTooltip secondTooltip = new M3RichTooltip("Second details", "Persistent help");
            firstTooltip.setPersistent(true);
            secondTooltip.setPersistent(true);
            M3Tooltip.install(firstOwner, firstTooltip);
            M3Tooltip.install(secondOwner, secondTooltip);

            HBox root = new HBox(240.0, firstOwner, secondOwner);
            root.setPadding(new Insets(120.0));
            FxTestUtils.setCssAnimationsEnabled(root, false);
            try {
                stage.setScene(new Scene(root, 960.0, 420.0));
                stage.show();
                root.applyCss();
                root.layout();

                firstOwner.fireEvent(primaryMouseEvent(
                        firstOwner,
                        MouseEvent.MOUSE_ENTERED,
                        firstOwner.getWidth() / 2.0,
                        firstOwner.getHeight() / 2.0,
                        false
                ));
                assertFalse(firstTooltip.isShowing(), "persistent rich tooltip must ignore hover");

                firePrimaryClick(firstOwner);
                assertTrue(firstTooltip.isShowing());
                Bounds firstOwnerBounds = Objects.requireNonNull(
                        firstOwner.localToScreen(firstOwner.getBoundsInLocal()),
                        "first owner screen bounds"
                );
                assertFalse(
                        popupOverlaps(firstTooltip, firstOwnerBounds),
                        "rich tooltip must not cover its owner"
                );

                firstOwner.fireEvent(primaryMouseEvent(
                        firstOwner,
                        MouseEvent.MOUSE_EXITED,
                        firstOwner.getWidth() / 2.0,
                        firstOwner.getHeight() / 2.0,
                        false
                ));
                assertTrue(firstTooltip.isShowing(), "persistent rich tooltip must survive pointer exit");

                firePrimaryClick(secondOwner);
                assertFalse(firstTooltip.isShowing(), "opening another tooltip must close the previous tooltip");
                assertTrue(secondTooltip.isShowing());

                firePrimaryClick(secondOwner);
                assertFalse(secondTooltip.isShowing(), "activating the same owner again must dismiss its tooltip");
            } finally {
                M3Tooltip.uninstall(firstOwner, firstTooltip);
                M3Tooltip.uninstall(secondOwner, secondTooltip);
                firstTooltip.hide();
                secondTooltip.hide();
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip popup hover keeps action content reachable.
    @Tier2Test
    @Test
    void richTooltipStaysOpenWhilePopupIsHovered() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> tooltipRootReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    @Nullable Label target = targetReference.get();
                    @Nullable M3RichTooltip tooltip = tooltipReference.get();
                    @Nullable Node tooltipRoot = tooltipRootReference.get();
                    return target != null
                            && tooltip != null
                            && tooltipRoot != null
                            && tooltip.isShowing()
                            && M3TooltipRegistry.activeInstalledTooltipPopupOwnsInteraction(target);
                },
                RICH_TOOLTIP_POPUP_OWNERSHIP_STABLE_PULSES,
                () -> {
                    Stage stage = new Stage();
                    Label target = new Label("Target");
                    M3RichTooltip tooltip = installRichTooltip(
                            target,
                            "Title",
                            "Supporting text",
                            createButton("Action", M3ButtonVariant.TEXT)
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    FxTestUtils.setCssAnimationsEnabled(target, false);

                    Pane root = new Pane(target);
                    root.setFocusTraversable(true);
                    stage.setScene(new Scene(root, 240.0, 120.0));
                    stage.show();
                    root.applyCss();
                    root.layout();
                    root.requestFocus();

                    tooltip.show(target, stage.getX() + 32.0, stage.getY() + 72.0);
                    Node tooltipRoot = tooltip.getScene().getRoot();
                    target.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 4.0, 4.0, false));
                    tooltipRoot.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));

                    stageReference.set(stage);
                    targetReference.set(target);
                    tooltipReference.set(tooltip);
                    tooltipRootReference.set(tooltipRoot);
                },
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    Label target = Objects.requireNonNull(targetReference.get(), "target");
                    M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                    Node tooltipRoot = Objects.requireNonNull(tooltipRootReference.get(), "tooltipRoot");
                    try {
                        assertTrue(tooltip.isShowing());
                        assertTrue(M3TooltipRegistry.activeInstalledTooltipPopupOwnsInteraction(target));

                        tooltipRoot.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 4.0, 4.0, false));
                    } finally {
                        tooltip.hide();
                        FxTestUtils.setCssAnimationsEnabled(target, true);
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that a rich tooltip popup action remains clickable after pointer transfer from the owner.
    @Test
    void richTooltipPopupActionFiresAfterPointerTransfer() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            Label target = new Label("Target");
            AtomicInteger actionCount = new AtomicInteger();
            M3Button action = createButton(
                    "Action",
                    M3ButtonVariant.TEXT,
                    event -> actionCount.incrementAndGet()
            );
            M3RichTooltip tooltip = installRichTooltip(
                    target,
                    "Title",
                    "Supporting text",
                    action
            );
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setHideDelay(Duration.ZERO);
            tooltip.setShowDuration(Duration.INDEFINITE);

            Pane root = new Pane(target);
            try {
                stage.setScene(new Scene(root, 240.0, 120.0));
                stage.show();
                root.applyCss();
                root.layout();

                tooltip.show(target, stage.getX() + 32.0, stage.getY() + 72.0);
                Node tooltipRoot = Objects.requireNonNull(tooltip.getScene(), "tooltip scene").getRoot();
                tooltipRoot.applyCss();
                if (tooltipRoot instanceof Parent parent) {
                    parent.layout();
                }
                target.fireEvent(primaryMouseEvent(target, MouseEvent.MOUSE_EXITED, 4.0, 4.0, false));
                tooltipRoot.fireEvent(primaryMouseEvent(tooltipRoot, MouseEvent.MOUSE_ENTERED, 4.0, 4.0, false));

                Bounds actionBounds = action.getLayoutBounds();
                double actionX = actionBounds.getMinX() + actionBounds.getWidth() / 2.0;
                double actionY = actionBounds.getMinY() + actionBounds.getHeight() / 2.0;
                assertTrue(action.contains(actionX, actionY),
                        () -> "Rich tooltip action should contain its layout center: bounds=" + actionBounds);
                assertTrue(M3TooltipRegistry.activeInstalledTooltipPopupOwnsInteraction(target));

                action.fireEvent(primaryMouseEvent(action, MouseEvent.MOUSE_ENTERED, actionX, actionY, false));
                action.fireEvent(primaryMouseEvent(action, MouseEvent.MOUSE_PRESSED, actionX, actionY, true));
                assertTrue(action.isArmed());
                action.fireEvent(primaryMouseEvent(action, MouseEvent.MOUSE_RELEASED, actionX, actionY, false));

                assertFalse(action.isArmed());
                assertEquals(1, actionCount.get());
                assertTrue(tooltip.isShowing());
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip popup focus keeps action content reachable from the keyboard.
    @Tier2Test
    @Test
    void richTooltipStaysOpenWhilePopupActionHasKeyboardFocus() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> actionReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> tooltipShowingWithFocus(tooltipReference, targetReference),
                () -> {
                    Stage stage = new Stage();
                    Label target = new Label("Target");
                    target.setFocusTraversable(true);
                    M3Button action = createButton("Action", M3ButtonVariant.TEXT);
                    M3RichTooltip tooltip = installRichTooltip(
                            target,
                            "Title",
                            "Supporting text",
                            action
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    FxTestUtils.setCssAnimationsEnabled(target, false);

                    Pane root = new Pane(target);
                    stage.setScene(new Scene(root, 240.0, 120.0));
                    stage.show();
                    root.applyCss();
                    root.layout();
                    assertTrue(M3FocusTraversal.handleCyclicTabKeyFocus(
                            root,
                            keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB),
                            List.of(target)
                    ));

                    stageReference.set(stage);
                    targetReference.set(target);
                    tooltipReference.set(tooltip);
                    actionReference.set(action);
                },
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    Label target = Objects.requireNonNull(targetReference.get(), "target");
                    M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                    M3Button action = Objects.requireNonNull(actionReference.get(), "action");
                    try {
                        assertTrue(tooltip.isShowing());
                        assertTrue(tooltip.focusFirstInteractiveTarget());
                        assertTrue(action.isFocused());

                        action.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                        assertFalse(tooltip.isShowing());
                        assertTrue(target.isFocused());
                    } finally {
                        tooltip.hide();
                        FxTestUtils.setCssAnimationsEnabled(target, true);
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that rich tooltip keyboard traversal connects the owner and popup actions.
    @Tier2Test
    @Test
    void richTooltipKeyboardTraversalConnectsOwnerAndActions() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> firstActionReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> disabledActionReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> secondActionReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> tooltipShowingWithFocus(tooltipReference, targetReference),
                () -> {
                    Stage stage = new Stage();
                    Label target = new Label("Target");
                    target.setFocusTraversable(true);
                    M3Button firstAction = createButton("First", M3ButtonVariant.TEXT);
                    M3Button disabledAction = createButton("Disabled", M3ButtonVariant.TEXT);
                    M3Button secondAction = createButton("Second", M3ButtonVariant.TEXT);
                    disabledAction.setDisable(true);
                    M3RichTooltip tooltip = installRichTooltip(
                            target,
                            "Title",
                            "Supporting text",
                            firstAction,
                            disabledAction,
                            secondAction
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    FxTestUtils.setCssAnimationsEnabled(target, false);

                    Pane root = new Pane(target);
                    stage.setScene(new Scene(root, 260.0, 140.0));
                    stage.show();
                    root.applyCss();
                    root.layout();
                    assertTrue(M3FocusTraversal.handleCyclicTabKeyFocus(
                            root,
                            keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB),
                            List.of(target)
                    ));

                    stageReference.set(stage);
                    targetReference.set(target);
                    tooltipReference.set(tooltip);
                    firstActionReference.set(firstAction);
                    disabledActionReference.set(disabledAction);
                    secondActionReference.set(secondAction);
                },
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    Label target = Objects.requireNonNull(targetReference.get(), "target");
                    M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                    M3Button firstAction = Objects.requireNonNull(firstActionReference.get(), "firstAction");
                    M3Button disabledAction = Objects.requireNonNull(disabledActionReference.get(), "disabledAction");
                    M3Button secondAction = Objects.requireNonNull(secondActionReference.get(), "secondAction");
                    try {
                        assertTrue(tooltip.isShowing());
                        assertTrue(target.isFocused());

                        assertTrue(tooltip.focusFirstInteractiveTarget());
                        assertTrue(firstAction.isFocused());
                        assertTrue(tooltip.isShowing());

                        assertTrue(tooltip.traverseInteractiveFocus(firstAction, target, false));

                        assertFalse(disabledAction.isFocused());
                        assertTrue(secondAction.isFocused());

                        assertTrue(tooltip.traverseInteractiveFocus(secondAction, target, true));

                        assertTrue(firstAction.isFocused());

                        assertTrue(tooltip.traverseInteractiveFocus(firstAction, target, true));

                        assertTrue(target.isFocused());
                        assertTrue(tooltip.isShowing());

                        assertTrue(tooltip.focusFirstInteractiveTarget());
                        assertTrue(firstAction.isFocused());

                        firstAction.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                        assertFalse(tooltip.isShowing());
                        assertTrue(target.isFocused());
                    } finally {
                        tooltip.hide();
                        FxTestUtils.setCssAnimationsEnabled(target, true);
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that tooltip themes preserve application inline style and expose semantic context classes.
    @Test
    void tooltipAppliesAndClearsTheme() {
        M3Tooltip tooltip = new M3Tooltip("Details");
        M3Theme theme = M3Theme.defaultTheme();

        tooltip.setStyle("-fx-opacity: 0.9;");
        tooltip.setTheme(theme);

        assertEquals(theme, tooltip.getTheme());
        assertEquals("-fx-opacity: 0.9;", tooltip.getStyle());
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

        M3Tooltip attachedTooltip = installTooltip(attachedTarget, "Installed");
        M3Tooltip delayedTooltip = installTooltip(delayedTarget, "Delayed");
        M3Tooltip uninstalledTooltip = installTooltip(uninstalledTarget, "Uninstalled");
        M3Tooltip.uninstall(uninstalledTarget, uninstalledTooltip);

        assertEquals(theme, attachedTooltip.getTheme());
        assertTrue(attachedTooltip.getStyle().isEmpty());
        assertTrue(attachedTooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(attachedTooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertNull(delayedTooltip.getTheme());

        root.getChildren().add(delayedTarget);
        root.getChildren().add(uninstalledTarget);

        assertEquals(theme, delayedTooltip.getTheme());
        assertTrue(delayedTooltip.getStyle().isEmpty());
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

        M3Tooltip tooltip = installTooltip(target, "Installed");

        assertSame(localTheme, tooltip.getTheme());
        assertTrue(tooltip.getStyle().isEmpty());
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

        M3Tooltip tooltip = installTooltip(target, "Installed");

        assertSame(localTheme, tooltip.getTheme());

        Pane root = new Pane(localRoot);
        new Scene(root);

        assertSame(localTheme, tooltip.getTheme());
        assertTrue(tooltip.getStyle().isEmpty());
    }

    /// Verifies that installed tooltips follow scene theme changes after installation.
    @Test
    void tooltipReinheritsRuntimeSceneThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            Pane root = new Pane(target);
            Scene scene = new Scene(root, 240.0, 120.0);
            M3Tooltip tooltip = installTooltip(target, "Installed");
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertNull(tooltip.getTheme());

                M3ThemeManager.install(scene, baselineTheme);

                assertSame(baselineTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));

                tooltip.show(target, stage.getX() + 48.0, stage.getY() + 96.0);

                M3ThemeManager.install(scene, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTooltipThemeStylesheet(tooltip, expressiveDarkTheme);
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(target, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies that visible installed tooltip popups mirror owner stylesheet and theme context changes.
    @Test
    void tooltipPopupContextSynchronizesOwnerStylesheetsAndRuntimeTheme() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> popupRootReference = new AtomicReference<>();
        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );
        String extraStylesheet = M3Stylesheets.controlStylesheet("button.css");
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> Objects.requireNonNull(stageReference.get()).isFocused(),
                    2,
                    () -> {
                        Label target = new Label("Target");
                        Pane root = new Pane(target);
                        Scene scene = new Scene(root, 280.0, 140.0);
                        M3Tooltip tooltip = installTooltip(target, "Installed");
                        Stage stage = new Stage();
                        stageReference.set(stage);
                        sceneReference.set(scene);
                        targetReference.set(target);
                        tooltipReference.set(tooltip);
                        M3ThemeManager.install(scene, baselineTheme);
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();
                    },
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get());
                        Scene scene = Objects.requireNonNull(sceneReference.get());
                        Label target = Objects.requireNonNull(targetReference.get());
                        M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get());
                        tooltip.show(target, stage.getX() + 48.0, stage.getY() + 96.0);
                        Parent popupRoot = tooltip.getScene().getRoot();
                        popupRootReference.set(popupRoot);

                        assertSame(baselineTheme, M3ThemeManager.getTheme(popupRoot));
                        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
                        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
                        assertTrue(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(baselineTheme)));

                        scene.getStylesheets().add(extraStylesheet);
                    }
            );

            FxTestUtils.runOnFxThread(() -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent popupRoot = Objects.requireNonNull(popupRootReference.get(), "popupRoot");
                assertTrue(Objects.requireNonNull(tooltipReference.get()).isShowing(),
                        "Tooltip must remain visible while owner stylesheets change");
                assertTrue(popupRoot.getStylesheets().contains(extraStylesheet),
                        () -> "Popup stylesheets: " + popupRoot.getStylesheets());

                M3ThemeManager.install(scene, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(popupRoot));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupRoot.getStylesheets().contains(extraStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)
                ));

                M3ThemeManager.uninstall(scene);

                assertNull(M3ThemeManager.getTheme(popupRoot));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupRoot.getStylesheets().contains(extraStylesheet));
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3Tooltip tooltip = tooltipReference.get();
                if (tooltip != null) {
                    tooltip.hide();
                }
                @Nullable Label target = targetReference.get();
                if (target != null && tooltip != null) {
                    M3Tooltip.uninstall(target, tooltip);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that explicitly themed tooltip popup roots are not overridden by owner theme changes.
    @Test
    void tooltipPopupContextKeepsExplicitThemeAboveOwnerTheme() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            Pane root = new Pane(target);
            Scene scene = new Scene(root, 280.0, 140.0);
            M3Theme ownerTheme = M3Theme.defaultTheme();
            M3Theme ownerReplacementTheme = M3Theme.fromSeed(
                    Color.web("#ba1a1a"),
                    M3Profile.BASELINE_2021,
                    Brightness.LIGHT
            );
            M3Theme explicitTooltipTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            M3Tooltip tooltip = new M3Tooltip("Explicit");
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, ownerTheme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                tooltip.setTheme(explicitTooltipTheme);
                tooltip.show(target, stage.getX() + 48.0, stage.getY() + 96.0);
                Parent popupRoot = tooltip.getScene().getRoot();

                assertSame(explicitTooltipTheme, M3ThemeManager.getTheme(popupRoot));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupRoot.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(explicitTooltipTheme)
                ));

                M3ThemeManager.install(scene, ownerReplacementTheme);

                assertSame(explicitTooltipTheme, M3ThemeManager.getTheme(popupRoot));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupRoot.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(explicitTooltipTheme)
                ));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that installed tooltips follow scene root replacement while the target remains attached.
    @Test
    void tooltipReinheritsRuntimeSceneRootReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            Pane targetContainer = new Pane(target);
            Pane firstRoot = new Pane(targetContainer);
            Scene scene = new Scene(firstRoot, 280.0, 140.0);
            M3Tooltip tooltip = installTooltip(target, "Installed");
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, baselineTheme);
                stage.setScene(scene);
                stage.show();
                firstRoot.applyCss();
                firstRoot.layout();

                assertSame(baselineTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));

                tooltip.show(target, stage.getX() + 48.0, stage.getY() + 96.0);

                Pane secondRoot = new Pane();
                firstRoot.getChildren().remove(targetContainer);
                secondRoot.getChildren().add(targetContainer);
                scene.setRoot(secondRoot);

                assertSame(baselineTheme, tooltip.getTheme());

                M3ThemeManager.install(scene, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTooltipThemeStylesheet(tooltip, expressiveDarkTheme);

                M3ThemeManager.uninstall(scene);

                assertNull(tooltip.getTheme());
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                Pane unthemedRoot = new Pane();
                secondRoot.getChildren().remove(targetContainer);
                unthemedRoot.getChildren().add(targetContainer);
                scene.setRoot(unthemedRoot);

                assertNull(tooltip.getTheme());
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(target, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies that installed tooltips follow local parent theme changes after installation.
    @Test
    void tooltipReinheritsRuntimeLocalParentThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            Pane localRoot = new Pane(target);
            Pane root = new Pane(localRoot);
            Scene scene = new Scene(root, 240.0, 120.0);
            M3Tooltip tooltip = installTooltip(target, "Installed");
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertNull(tooltip.getTheme());

                M3ThemeManager.install(localRoot, baselineTheme);

                assertSame(baselineTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));

                tooltip.show(target, stage.getX() + 48.0, stage.getY() + 96.0);

                M3ThemeManager.install(localRoot, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTooltipThemeStylesheet(tooltip, expressiveDarkTheme);
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(target, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies that installed tooltips follow owner subtree reparenting between local theme roots.
    @Test
    void tooltipReinheritsRuntimeLocalParentThemeAfterReparenting() {
        FxTestUtils.runOnFxThread(() -> {
            Label target = new Label("Target");
            Pane firstLocalRoot = new Pane(target);
            Pane secondLocalRoot = new Pane();
            Pane root = new Pane(firstLocalRoot, secondLocalRoot);
            Scene scene = new Scene(root, 320.0, 160.0);
            M3Tooltip tooltip = installTooltip(target, "Installed");
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(firstLocalRoot, baselineTheme);
                M3ThemeManager.install(secondLocalRoot, expressiveDarkTheme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertSame(baselineTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));

                tooltip.show(target, stage.getX() + 48.0, stage.getY() + 96.0);

                firstLocalRoot.getChildren().remove(target);
                secondLocalRoot.getChildren().add(target);

                assertSame(expressiveDarkTheme, tooltip.getTheme());
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTooltipThemeStylesheet(tooltip, expressiveDarkTheme);

                secondLocalRoot.getChildren().remove(target);
                root.getChildren().add(target);

                assertNull(tooltip.getTheme());
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(target, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies that tooltip popups render their inverse surface and text.
    @Tier2Test
    @Test
    void tooltipSnapshotRendersPopupSurface() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Tooltip tooltip = new M3Tooltip("Tooltip text");
            try {
                M3Button owner = new M3Button("Owner");
                Pane root = new Pane(owner);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                FxTestUtils.setCssAnimationsEnabled(root, false);
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

                assertPopupRootUsesThemeClasses(tooltipRoot, M3Theme.defaultTheme());
                SnapshotParameters snapshotParameters = new SnapshotParameters();
                snapshotParameters.setFill(Color.TRANSPARENT);
                WritableImage image = new WritableImage(
                        (int) Math.ceil(tooltipRoot.getLayoutBounds().getWidth()),
                        (int) Math.ceil(tooltipRoot.getLayoutBounds().getHeight())
                );
                tooltipRoot.snapshot(snapshotParameters, image);
                assertSnapshotHasColorVariety(image, 2);
                assertSnapshotNodeContainsContrast(image, tooltipRoot, Color.WHITE, 0.2);
                assertTrue(tooltipRoot.getStyleClass().contains("m3-tooltip-popup-root"));
                int lastX = (int) image.getWidth() - 1;
                int lastY = (int) image.getHeight() - 1;
                Color topLeftCorner = image.getPixelReader().getColor(0, 0);
                assertTrue(topLeftCorner.getOpacity() <= 0.05,
                        () -> "top-left tooltip corner should be transparent: " + topLeftCorner);
                assertTrue(image.getPixelReader().getColor(lastX, 0).getOpacity() <= 0.05);
                assertTrue(image.getPixelReader().getColor(0, lastY).getOpacity() <= 0.05);
                assertTrue(image.getPixelReader().getColor(lastX, lastY).getOpacity() <= 0.05);
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

    /// Verifies that long tooltip popups keep their preferred wrap width when the popup is sized by JavaFX.
    @Tier2Test
    @Test
    void tooltipPopupWrapsLongTextAtPreferredWidth() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3Tooltip tooltip = new M3Tooltip(
                    "Use tooltips for brief contextual labels when a control needs clarification."
            );
            try {
                M3Button owner = new M3Button("Owner");
                Pane root = new Pane(owner);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                Scene scene = new Scene(root, 420.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(420.0, 180.0);
                root.layout();

                tooltip.setPrefWidth(260.0);
                tooltip.show(owner, stage.getX() + 40.0, stage.getY() + 96.0);

                Parent tooltipRoot = tooltip.getScene().getRoot();
                tooltipRoot.applyCss();
                tooltipRoot.layout();

                Parent tooltipSkinRoot = assertInstanceOf(Parent.class, tooltip.getSkin().getNode());
                Label renderedTooltip = assertInstanceOf(
                        Label.class,
                        tooltipSkinRoot.lookup("." + "m3-tooltip")
                );
                Bounds popupBounds = tooltipRoot.getLayoutBounds();
                Bounds renderedBounds = renderedTooltip.getLayoutBounds();
                assertPopupRootUsesThemeClasses(tooltipRoot, M3Theme.defaultTheme());
                assertTrue(renderedTooltip.isWrapText(), "long tooltips should wrap text");
                assertEquals(4.0, renderedTooltip.getPadding().getTop(), 0.0001);
                assertEquals(4.0, renderedTooltip.getPadding().getBottom(), 0.0001);
                assertEquals(8.0, renderedTooltip.getPadding().getLeft(), 0.0001);
                assertEquals(8.0, renderedTooltip.getPadding().getRight(), 0.0001);
                assertRegionRadii(renderedTooltip, 4.0, 4.0, 4.0, 4.0);
                assertTrue(popupBounds.getWidth() >= 250.0 && popupBounds.getWidth() <= 270.0,
                        () -> "popup should honor the requested wrap width: " + popupBounds);
                assertTrue(renderedBounds.getHeight() >= 38.0,
                        () -> "wrapped tooltip should be taller than a one-line popup: " + renderedBounds);
                assertTrue(renderedBounds.getHeight() <= 96.0,
                        () -> "wrapped tooltip should remain compact: " + renderedBounds);

                WritableImage image = snapshotImageOnFxThread(renderedTooltip);
                assertSnapshotHasColorVariety(image, 2);
                assertRenderedTextNodesStayInsideLayout(tooltipRoot);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-tooltip-long.png"
                ));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip popups render their surface, text, and actions.
    @Tier2Test
    @Test
    void richTooltipSnapshotRendersPopupSurface() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3RichTooltip tooltip = richTooltip(
                    "Rich tooltip",
                    "A wider tooltip surface can include a title, supporting text, and actions.",
                    createButton("Action", M3ButtonVariant.TEXT)
            );
            try {
                M3Button owner = new M3Button("Owner");
                Pane root = new Pane(owner);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                FxTestUtils.setCssAnimationsEnabled(root, false);
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

                assertPopupRootUsesThemeClasses(tooltipRoot, M3Theme.defaultTheme());
                VBox container = assertInstanceOf(
                        VBox.class,
                        tooltipRoot.lookup("." + "m3-rich-tooltip-container")
                );
                HBox actions = assertInstanceOf(
                        HBox.class,
                        tooltipRoot.lookup("." + "m3-rich-tooltip-actions")
                );
                assertRegionFill(container, Color.rgb(242, 236, 244));
                assertRegionRadii(container, 12.0, 12.0, 12.0, 12.0);
                assertEquals(new Insets(12.0, 16.0, 8.0, 16.0), container.getPadding());
                assertTrue(
                        tooltip.getHeight() < 160.0,
                        () -> "rich tooltip should size to content instead of forcing a 160px minimum: "
                                + tooltip.getHeight()
                );
                Label titleLabel = assertInstanceOf(
                        Label.class,
                        tooltipRoot.lookup("." + "m3-rich-tooltip-title")
                );
                Label supportingTextLabel = assertInstanceOf(
                        Label.class,
                        tooltipRoot.lookup("." + "m3-rich-tooltip-supporting-text")
                );
                assertEquals(Color.rgb(73, 69, 78), titleLabel.getTextFill());
                assertEquals(Color.rgb(73, 69, 78), supportingTextLabel.getTextFill());
                Node action = tooltip.getActions().get(0);
                assertNodeInsideAncestor(container, action, 0.5);
                assertNodeInsideAncestor(container, actions, 0.5);

                WritableImage image = snapshotImageOnFxThread(tooltipRoot);
                assertSnapshotHasColorVariety(image, 4);
                assertSnapshotNodeContainsContrast(image, tooltipRoot, Color.WHITE, 0.08);
                assertRenderedTextNodesStayInsideLayout(tooltipRoot);
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

    /// Creates motion behavior that only overrides plain tooltip timings from the standard profile.
    private static M3MotionBehavior tooltipBehavior(Duration showDelay, Duration hideDelay, Duration showDuration) {
        return M3MotionBehavior.builder()
                .tooltipShowDelay(Objects.requireNonNull(showDelay, "showDelay"))
                .tooltipHideDelay(Objects.requireNonNull(hideDelay, "hideDelay"))
                .tooltipShowDuration(Objects.requireNonNull(showDuration, "showDuration"))
                .build();
    }

    /// Returns whether a referenced tooltip has been created and is currently hidden.
    private static boolean tooltipHidden(AtomicReference<? extends @Nullable M3Tooltip> tooltipReference) {
        @Nullable M3Tooltip tooltip = tooltipReference.get();
        return tooltip != null && !tooltip.isShowing();
    }

    /// Closes a real-window installed tooltip test scene and clears inherited motion settings.
    private static void closeInstalledTooltipScene(
            AtomicReference<? extends @Nullable Stage> stageReference,
            AtomicReference<? extends @Nullable Node> rootReference,
            AtomicReference<? extends @Nullable Node> targetReference,
            AtomicReference<? extends @Nullable M3Tooltip> tooltipReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable Node target = targetReference.get();
            @Nullable M3Tooltip tooltip = tooltipReference.get();
            if (target != null && tooltip != null) {
                M3Tooltip.uninstall(target, tooltip);
            } else if (tooltip != null) {
                tooltip.hide();
            }

            @Nullable Node root = rootReference.get();
            if (root != null) {
                FxTestUtils.clearMotionBehavior(root);
            }

            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Verifies that a popup scene root mirrors the expected owner theme style classes.
    private static void assertPopupRootUsesThemeClasses(Parent popupRoot, M3Theme theme) {
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS),
                () -> "popup root is missing " + M3ThemeManager.ROOT_STYLE_CLASS + ": " + popupRoot.getStyleClass());
        assertTrue(popupRoot.getStyleClass().contains(
                theme.profile() == M3Profile.EXPRESSIVE_2025
                        ? M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS
                        : M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS
        ), () -> "popup root is missing the expected profile class: " + popupRoot.getStyleClass());
        assertTrue(popupRoot.getStyleClass().contains(
                theme.brightness() == Brightness.DARK
                        ? M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS
                        : M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS
        ), () -> "popup root is missing the expected brightness class: " + popupRoot.getStyleClass());
    }

    /// Fires one complete synthetic primary-button click at the center of a node.
    private static void firePrimaryClick(Node node) {
        Bounds bounds = node.getLayoutBounds();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_PRESSED, x, y, true));
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_RELEASED, x, y, false));
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_CLICKED, x, y, false));
    }

    /// Returns whether a tooltip popup overlaps one owner rectangle in screen coordinates.
    private static boolean popupOverlaps(M3Tooltip tooltip, Bounds ownerBounds) {
        double popupMinX = tooltip.getX();
        double popupMinY = tooltip.getY();
        double popupMaxX = popupMinX + tooltip.getWidth();
        double popupMaxY = popupMinY + tooltip.getHeight();
        return popupMaxX > ownerBounds.getMinX()
                && popupMinX < ownerBounds.getMaxX()
                && popupMaxY > ownerBounds.getMinY()
                && popupMinY < ownerBounds.getMaxY();
    }

    /// Creates and installs a tooltip for tests that need installed tooltip behavior.
    private static M3Tooltip installTooltip(Node node, String text) {
        M3Tooltip tooltip = new M3Tooltip(text);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }
}
