// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies connected button geometry and coordinated interaction.
@NotNullByDefault
final class M3ButtonGroupContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that button groups assign connected position style classes.
    @Test
    void buttonGroupAssignsPositionStyleClasses() {
        M3Button first = new M3Button("First");
        M3Button second = new M3Button("Second");
        M3Button third = new M3Button("Third");
        M3ButtonGroup group = buttonGroup(first, second, third);

        assertTrue(group.getStyleClass().contains("m3-button-group"));
        assertTrue(first.getStyleClass().contains("m3-grouped-button"));
        assertTrue(first.getStyleClass().contains("m3-button-group-first"));
        assertTrue(second.getStyleClass().contains("m3-button-group-middle"));
        assertTrue(third.getStyleClass().contains("m3-button-group-last"));
        assertEquals(3, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        group.getItems().remove(second);

        assertFalse(second.getStyleClass().contains("m3-grouped-button"));
        assertFalse(second.getStyleClass().contains("m3-button-group-middle"));
        assertTrue(first.getStyleClass().contains("m3-button-group-first"));
        assertTrue(third.getStyleClass().contains("m3-button-group-last"));

        group.getItems().remove(first);

        assertFalse(first.getStyleClass().contains("m3-grouped-button"));
        assertTrue(third.getStyleClass().contains("m3-button-group-single"));
    }

    /// Verifies that connected button groups support selectable icon-toggle children and clean reused nodes.
    @Test
    void buttonGroupSupportsSelectableIconToggleChildren() {
        M3IconToggleButton first = new M3IconToggleButton(new Label("A"));
        M3IconToggleButton second = new M3IconToggleButton(new Label("B"));
        M3IconToggleButton third = new M3IconToggleButton(new Label("C"));
        first.setSelected(true);
        M3ButtonGroup group = new M3ButtonGroup();
        group.setSize(M3ButtonSize.MEDIUM);
        group.getItems().addAll(first, second, third);

        assertEquals(M3ButtonSize.MEDIUM, first.getSize());
        assertTrue(first.getStyleClass().contains("m3-grouped-button"));
        assertTrue(first.getStyleClass().contains("m3-button-group-first"));
        assertTrue(second.getStyleClass().contains("m3-button-group-middle"));
        assertTrue(third.getStyleClass().contains("m3-button-group-last"));

        group.getItems().remove(second);

        assertFalse(second.getStyleClass().contains("m3-grouped-button"));
        assertFalse(second.getStyleClass().contains("m3-button-group-middle"));
        assertTrue(first.getStyleClass().contains("m3-button-group-first"));
        assertTrue(third.getStyleClass().contains("m3-button-group-last"));
    }

    /// Verifies that button groups keep logical edge style classes for right-to-left painting.
    @Test
    void buttonGroupKeepsLogicalPositionStyleClassesForRightToLeft() {
        M3Button first = new M3Button("Archive");
        M3Button second = new M3Button("Share");
        M3Button third = new M3Button("Edit");
        M3ButtonGroup group = buttonGroup(first, second, third);

        group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        assertTrue(first.getStyleClass().contains("m3-button-group-first"));
        assertTrue(second.getStyleClass().contains("m3-button-group-middle"));
        assertTrue(third.getStyleClass().contains("m3-button-group-last"));

        group.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        assertTrue(first.getStyleClass().contains("m3-button-group-first"));
        assertTrue(third.getStyleClass().contains("m3-button-group-last"));
    }

    /// Verifies that button group spacing is styleable and rejects invalid application values.
    @Test
    void buttonGroupSpacingTokenIsStyleable() {
        M3ButtonGroup group = buttonGroup(new M3Button("A"), new M3Button("B"));
        group.setStyle("-m3-button-group-spacing: 6px;");

        applyCss(group);

        assertEquals(6.0, group.getSpacing(), 0.0001);
        assertThrows(IllegalArgumentException.class, () -> group.setSpacing(-0.01));
        assertThrows(IllegalArgumentException.class, () -> group.setSpacing(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> group.setSpacing(Double.POSITIVE_INFINITY));
    }

    /// Verifies that the standard-group activated width multiplier is styleable and rejects invalid values.
    @Test
    void buttonGroupStandardPressedWidthMultiplierIsStyleableAndValidated() {
        M3ButtonGroup group = buttonGroup(new M3Button("A"), new M3Button("B"));
        group.setStyle("-m3-button-group-standard-pressed-width-multiplier: 0.25;");

        applyCss(group);

        assertEquals(0.25, group.getStandardPressedWidthMultiplier(), 0.0001);
        assertThrows(IllegalArgumentException.class, () -> group.setStandardPressedWidthMultiplier(-0.01));
        assertThrows(IllegalArgumentException.class, () -> group.setStandardPressedWidthMultiplier(Double.NaN));
        assertThrows(
                IllegalArgumentException.class,
                () -> group.setStandardPressedWidthMultiplier(Double.POSITIVE_INFINITY)
        );
    }

    /// Verifies that button group variant and size properties maintain matching style classes.
    @Test
    void buttonGroupVariantAndSizePropertiesMaintainStyleClasses() {
        M3ButtonGroup group = buttonGroup(new M3Button("A"), new M3Button("B"));

        assertEquals(M3ButtonGroupVariant.CONNECTED, group.getVariant());
        assertEquals(M3ButtonSize.SMALL, group.getSize());
        assertTrue(group.getStyleClass().contains(M3ButtonGroupVariant.CONNECTED.styleClass()));
        assertTrue(group.getStyleClass().contains("m3-button-group-small"));

        group.setVariant(M3ButtonGroupVariant.STANDARD);
        group.setSize(M3ButtonSize.MEDIUM);

        assertTrue(group.getStyleClass().contains(M3ButtonGroupVariant.STANDARD.styleClass()));
        assertFalse(group.getStyleClass().contains(M3ButtonGroupVariant.CONNECTED.styleClass()));
        assertTrue(group.getStyleClass().contains("m3-button-group-medium"));
        assertFalse(group.getStyleClass().contains("m3-button-group-small"));

        group.variantProperty().set(null);
        group.sizeProperty().set(null);

        assertEquals(M3ButtonGroupVariant.CONNECTED, group.getVariant());
        assertEquals(M3ButtonSize.SMALL, group.getSize());
        assertTrue(group.getStyleClass().contains(M3ButtonGroupVariant.CONNECTED.styleClass()));
        assertTrue(group.getStyleClass().contains("m3-button-group-small"));
    }

    /// Verifies that button group variant and size CSS tokens resolve without overriding each other.
    @Test
    void buttonGroupVariantAndSizeTokensResolveTogether() {
        M3Button first = new M3Button("Archive");
        M3Button second = new M3Button("Share");
        M3ButtonGroup group = buttonGroup(first, second);
        group.setVariant(M3ButtonGroupVariant.STANDARD);
        group.setSize(M3ButtonSize.EXTRA_SMALL);

        applyCss(group);

        assertEquals(18.0, group.getSpacing(), 0.0001);
        assertEquals(32.0, first.getContainerHeight(), 0.0001);
        assertEquals(12.0, first.getHorizontalPadding(), 0.0001);
        assertRegionRoundedCorners(first, true, true, true, true);
        assertRegionRoundedCorners(second, true, true, true, true);

        group.setSize(M3ButtonSize.MEDIUM);
        applyCss(group);

        assertEquals(8.0, group.getSpacing(), 0.0001);
        assertEquals(56.0, first.getContainerHeight(), 0.0001);
        assertEquals(24.0, first.getHorizontalPadding(), 0.0001);

        group.setVariant(M3ButtonGroupVariant.CONNECTED);
        group.setSize(M3ButtonSize.LARGE);
        applyCss(group);

        assertEquals(2.0, group.getSpacing(), 0.0001);
        assertEquals(96.0, first.getContainerHeight(), 0.0001);
        assertEquals(48.0, first.getHorizontalPadding(), 0.0001);
        assertRegionRadii(first, 48.0, 16.0, 16.0, 48.0);
        assertRegionRadii(second, 16.0, 48.0, 48.0, 16.0);
    }

    /// Verifies standard-group activated and selected width transfer, including multi-selection.
    @Test
    void standardButtonGroupRedistributesActivatedAndSelectedItemWidth() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("Action");
            M3Button middle = new M3Button("Action");
            M3Button last = new M3Button("Action");
            M3ButtonGroup group = buttonGroup(first, middle, last);
            group.setVariant(M3ButtonGroupVariant.STANDARD);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 420.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            resizeButtonGroupToPreferredSize(group);

            double restingFirstWidth = first.getWidth();
            double restingMiddleWidth = middle.getWidth();
            double restingLastWidth = last.getWidth();
            double restingChildrenWidth = restingFirstWidth + restingMiddleWidth + restingLastWidth;
            double expectedGrowth = restingMiddleWidth * group.getStandardPressedWidthMultiplier();

            middle.arm();
            group.layout();

            assertEquals(restingChildrenWidth, first.getWidth() + middle.getWidth() + last.getWidth(), 0.0001);
            assertEquals(restingMiddleWidth + expectedGrowth, middle.getWidth(), 0.0001);
            assertEquals(restingFirstWidth - expectedGrowth / 2.0, first.getWidth(), 0.0001);
            assertEquals(restingLastWidth - expectedGrowth / 2.0, last.getWidth(), 0.0001);

            middle.disarm();
            group.layout();

            assertEquals(restingFirstWidth, first.getWidth(), 0.0001);
            assertEquals(restingMiddleWidth, middle.getWidth(), 0.0001);
            assertEquals(restingLastWidth, last.getWidth(), 0.0001);

            middle.setDisable(true);
            middle.arm();
            group.layout();
            assertEquals(restingFirstWidth, first.getWidth(), 0.0001);
            assertEquals(restingMiddleWidth, middle.getWidth(), 0.0001);
            assertEquals(restingLastWidth, last.getWidth(), 0.0001);
            middle.setDisable(false);

            first.arm();
            group.layout();

            double edgeGrowth = restingFirstWidth * group.getStandardPressedWidthMultiplier();
            assertEquals(restingChildrenWidth, first.getWidth() + middle.getWidth() + last.getWidth(), 0.0001);
            assertEquals(restingFirstWidth + edgeGrowth, first.getWidth(), 0.0001);
            assertEquals(restingMiddleWidth - edgeGrowth, middle.getWidth(), 0.0001);
            assertEquals(restingLastWidth, last.getWidth(), 0.0001);
            first.disarm();

            M3IconToggleButton selectedFirst = new M3IconToggleButton("A");
            M3IconToggleButton selectedMiddle = new M3IconToggleButton("B");
            M3IconToggleButton selectedLast = new M3IconToggleButton("C");
            M3ButtonGroup selectionGroup = new M3ButtonGroup();
            selectionGroup.setVariant(M3ButtonGroupVariant.STANDARD);
            selectionGroup.getItems().addAll(selectedFirst, selectedMiddle, selectedLast);
            Pane selectionRoot = new Pane(selectionGroup);
            Scene selectionScene = new Scene(selectionRoot, 320.0, 100.0);

            M3ThemeManager.install(selectionScene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(selectionRoot, false);
            selectionRoot.applyCss();
            resizeButtonGroupToPreferredSize(selectionGroup);

            double selectionRestingWidth = selectedMiddle.getWidth();
            double selectionChildrenWidth =
                    selectedFirst.getWidth() + selectionRestingWidth + selectedLast.getWidth();

            selectedMiddle.setSelected(true);
            selectionGroup.layout();

            assertEquals(
                    selectionChildrenWidth,
                    selectedFirst.getWidth() + selectedMiddle.getWidth() + selectedLast.getWidth(),
                    0.0001
            );
            assertEquals(selectionRestingWidth, selectedMiddle.getWidth(), 0.0001);
            assertEquals(selectionRestingWidth, selectedFirst.getWidth(), 0.0001);
            assertEquals(selectionRestingWidth, selectedLast.getWidth(), 0.0001);
            assertTrue(selectedFirst.getWidth() >= 48.0);
            assertTrue(selectedMiddle.getWidth() >= 48.0);
            assertTrue(selectedLast.getWidth() >= 48.0);

            selectedMiddle.setSelected(false);
            selectionGroup.layout();
            assertEquals(selectionRestingWidth, selectedFirst.getWidth(), 0.0001);
            assertEquals(selectionRestingWidth, selectedMiddle.getWidth(), 0.0001);
            assertEquals(selectionRestingWidth, selectedLast.getWidth(), 0.0001);

            selectedFirst.setSelected(true);
            selectedMiddle.setSelected(true);
            selectionGroup.layout();
            assertEquals(
                    selectionChildrenWidth,
                    selectedFirst.getWidth() + selectedMiddle.getWidth() + selectedLast.getWidth(),
                    0.0001
            );
            assertEquals(selectionRestingWidth, selectedFirst.getWidth(), 0.0001);
            assertEquals(selectionRestingWidth, selectedMiddle.getWidth(), 0.0001);
            assertEquals(selectionRestingWidth, selectedLast.getWidth(), 0.0001);

            selectionGroup.setVariant(M3ButtonGroupVariant.CONNECTED);
            selectionRoot.applyCss();
            resizeButtonGroupToPreferredSize(selectionGroup);
            double compactConnectedWidth = Math.max(48.0, selectionRestingWidth);
            assertEquals(compactConnectedWidth, selectedFirst.getWidth(), 0.0001);
            assertEquals(compactConnectedWidth, selectedMiddle.getWidth(), 0.0001);
            assertEquals(compactConnectedWidth, selectedLast.getWidth(), 0.0001);
        });
    }

    /// Verifies that standard-group expansion and release expose real intermediate animation frames.
    @Tier2Test
    @Test
    void standardButtonGroupAnimatesArmedWidthAndRelease() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ButtonGroup> groupReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> middleReference = new AtomicReference<>();
        AtomicReference<Double> restingWidthReference = new AtomicReference<>(0.0);
        AtomicReference<Double> targetWidthReference = new AtomicReference<>(0.0);
        AtomicReference<Double> restingChildrenWidthReference = new AtomicReference<>(0.0);
        AtomicBoolean expansionIntermediateObserved = new AtomicBoolean();
        AtomicBoolean releaseIntermediateObserved = new AtomicBoolean();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3ButtonGroup group = groupReference.get();
                        M3Button middle = middleReference.get();
                        if (group == null || middle == null) {
                            return false;
                        }
                        group.layout();
                        double width = middle.getWidth();
                        double restingWidth = restingWidthReference.get();
                        double targetWidth = targetWidthReference.get();
                        if (width > restingWidth + 0.5 && width < targetWidth - 0.5) {
                            expansionIntermediateObserved.set(true);
                        }
                        return width >= targetWidth - 0.25;
                    },
                    () -> "Standard button group did not finish its armed-item expansion",
                    () -> {
                        M3Button first = new M3Button("Action");
                        M3Button middle = new M3Button("Action");
                        M3Button last = new M3Button("Action");
                        M3ButtonGroup group = buttonGroup(first, middle, last);
                        group.setVariant(M3ButtonGroupVariant.STANDARD);
                        Pane root = new Pane(group);
                        Scene scene = new Scene(root, 420.0, 100.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        resizeButtonGroupToPreferredSize(group);

                        double restingWidth = middle.getWidth();
                        stageReference.set(stage);
                        groupReference.set(group);
                        middleReference.set(middle);
                        restingWidthReference.set(restingWidth);
                        targetWidthReference.set(
                                restingWidth * (1.0 + group.getStandardPressedWidthMultiplier())
                        );
                        restingChildrenWidthReference.set(first.getWidth() + middle.getWidth() + last.getWidth());
                        middle.arm();
                    },
                    () -> {
                        M3ButtonGroup group = Objects.requireNonNull(groupReference.get(), "group");
                        M3Button middle = Objects.requireNonNull(middleReference.get(), "middle");
                        double restingChildrenWidth = restingChildrenWidthReference.get();

                        assertTrue(expansionIntermediateObserved.get());
                        assertEquals(targetWidthReference.get(), middle.getWidth(), 0.25);
                        assertEquals(
                                restingChildrenWidth,
                                group.getItems().stream().mapToDouble(node -> node.getLayoutBounds().getWidth()).sum(),
                                0.25
                        );
                        middle.disarm();
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3ButtonGroup group = groupReference.get();
                        M3Button middle = middleReference.get();
                        if (group == null || middle == null) {
                            return false;
                        }
                        group.layout();
                        double width = middle.getWidth();
                        double restingWidth = restingWidthReference.get();
                        double targetWidth = targetWidthReference.get();
                        if (width > restingWidth + 0.5 && width < targetWidth - 0.5) {
                            releaseIntermediateObserved.set(true);
                        }
                        return width <= restingWidth + 0.25;
                    },
                    () -> "Standard button group did not finish its release transition",
                    () -> {
                    },
                    () -> {
                        M3Button middle = Objects.requireNonNull(middleReference.get(), "middle");
                        assertTrue(releaseIntermediateObserved.get());
                        assertEquals(restingWidthReference.get(), middle.getWidth(), 0.25);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that connected groups keep fixed widths and standard groups preserve logical order in RTL.
    @Test
    void buttonGroupWidthInteractionHonorsVariantAndRightToLeftLayout() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("Action");
            M3Button middle = new M3Button("Action");
            M3Button last = new M3Button("Action");
            M3ButtonGroup group = buttonGroup(first, middle, last);
            group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 420.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            resizeButtonGroupToPreferredSize(group);

            double connectedFirstWidth = first.getWidth();
            double connectedMiddleWidth = middle.getWidth();
            double connectedLastWidth = last.getWidth();
            assertTrue(first.localToScene(first.getBoundsInLocal()).getMinX()
                    > middle.localToScene(middle.getBoundsInLocal()).getMinX());
            assertTrue(middle.localToScene(middle.getBoundsInLocal()).getMinX()
                    > last.localToScene(last.getBoundsInLocal()).getMinX());

            middle.arm();
            group.layout();

            assertEquals(connectedFirstWidth, first.getWidth(), 0.0001);
            assertEquals(connectedMiddleWidth, middle.getWidth(), 0.0001);
            assertEquals(connectedLastWidth, last.getWidth(), 0.0001);
            middle.disarm();

            group.setVariant(M3ButtonGroupVariant.STANDARD);
            root.applyCss();
            resizeButtonGroupToPreferredSize(group);
            double restingChildrenWidth = first.getWidth() + middle.getWidth() + last.getWidth();
            double restingMiddleWidth = middle.getWidth();

            middle.arm();
            group.layout();

            assertTrue(middle.getWidth() > restingMiddleWidth);
            assertEquals(restingChildrenWidth, first.getWidth() + middle.getWidth() + last.getWidth(), 0.0001);
            assertTrue(first.localToScene(first.getBoundsInLocal()).getMinX()
                    > middle.localToScene(middle.getBoundsInLocal()).getMinX());
            assertTrue(middle.localToScene(middle.getBoundsInLocal()).getMinX()
                    > last.localToScene(last.getBoundsInLocal()).getMinX());
            middle.disarm();
        });
    }

    /// Verifies that item changes clear stale button-group interaction state and attach new children.
    @Test
    void buttonGroupWidthInteractionTracksItemChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("Action");
            M3Button middle = new M3Button("Action");
            M3Button last = new M3Button("Action");
            M3ButtonGroup group = buttonGroup(first, middle, last);
            group.setVariant(M3ButtonGroupVariant.STANDARD);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 420.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            resizeButtonGroupToPreferredSize(group);

            middle.arm();
            group.layout();
            assertTrue(middle.getWidth() > first.getWidth());

            group.getItems().remove(middle);
            root.applyCss();
            resizeButtonGroupToPreferredSize(group);
            assertEquals(first.getWidth(), last.getWidth(), 0.0001);

            middle.disarm();
            group.getItems().add(1, middle);
            root.applyCss();
            resizeButtonGroupToPreferredSize(group);
            double restingMiddleWidth = middle.getWidth();

            middle.arm();
            group.layout();
            assertTrue(middle.getWidth() > restingMiddleWidth);
            middle.disarm();
        });
    }

    /// Verifies Expressive connected button-group geometry for every shared button size.
    @Test
    void expressiveButtonGroupSizeTokensResolveConnectedShapesAndFeedback() {
        M3Theme theme = M3Theme.fromSeed(
                Color.web("#6750a4"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        );
        VBox root = new VBox(8.0);
        root.setStyle("-fx-background-color: white;");
        List<M3ButtonGroup> groups = new ArrayList<>();
        for (M3ButtonSize size : M3ButtonSize.values()) {
            M3ButtonGroup group = buttonGroup(new M3Button(size.name()), new M3Button("Action"));
            group.setSize(size);
            groups.add(group);
            root.getChildren().add(group);
        }
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, theme);
        root.applyCss();
        root.layout();
        for (M3ButtonGroup group : groups) {
            group.resize(group.prefWidth(-1.0), group.prefHeight(-1.0));
            group.layout();
            for (Node item : group.getItems()) {
                item.applyCss();
            }
        }
        root.applyCss();

        M3ComponentTokens componentTokens = theme.tokens().componentTokens();
        List<M3ComponentTokens.ButtonGroupSizeTokens> groupTokens = List.of(
                componentTokens.buttonGroup().extraSmall(),
                componentTokens.buttonGroup().small(),
                componentTokens.buttonGroup().medium(),
                componentTokens.buttonGroup().large(),
                componentTokens.buttonGroup().extraLarge()
        );
        List<M3ComponentTokens.ButtonSizeTokens> buttonTokens = List.of(
                componentTokens.buttonSizing().extraSmall(),
                componentTokens.buttonSizing().small(),
                componentTokens.buttonSizing().medium(),
                componentTokens.buttonSizing().large(),
                componentTokens.buttonSizing().extraLarge()
        );

        for (int index = 0; index < groups.size(); index++) {
            M3ButtonGroup group = groups.get(index);
            M3Button first = (M3Button) group.getItems().get(0);
            M3Button last = (M3Button) group.getItems().get(1);
            M3ComponentTokens.ButtonGroupSizeTokens expectedGroup = groupTokens.get(index);
            M3ComponentTokens.ButtonSizeTokens expectedButton = buttonTokens.get(index);
            first.layout();
            last.layout();

            assertEquals(expectedGroup.connectedSpacing(), group.getSpacing(), 0.0001);
            assertEquals(expectedButton.containerHeight(), first.getContainerHeight(), 0.0001);
            assertEquals(expectedButton.horizontalPadding(), first.getHorizontalPadding(), 0.0001);
            double outerFeedbackRadius = expectedGroup.containerHeight() / 2.0;
            assertRegionRadii(
                    first,
                    outerFeedbackRadius,
                    expectedGroup.connectedInnerCorner(),
                    expectedGroup.connectedInnerCorner(),
                    outerFeedbackRadius
            );
            assertRegionRadii(
                    last,
                    expectedGroup.connectedInnerCorner(),
                    outerFeedbackRadius,
                    outerFeedbackRadius,
                    expectedGroup.connectedInnerCorner()
            );
            assertStateLayerRadii(
                    first,
                    outerFeedbackRadius,
                    expectedGroup.connectedInnerCorner(),
                    expectedGroup.connectedInnerCorner(),
                    outerFeedbackRadius
            );
        }

        M3ButtonGroup mediumGroup = groups.get(M3ButtonSize.MEDIUM.ordinal());
        M3Button pressed = (M3Button) mediumGroup.getItems().get(0);
        M3ComponentTokens.ButtonGroupSizeTokens mediumTokens = groupTokens.get(M3ButtonSize.MEDIUM.ordinal());
        pressed.arm();
        root.applyCss();
        resizeButtonGroupToPreferredSize(mediumGroup);
        pressed.layout();

        assertRegionRadii(
                pressed,
                pressed.getContainerHeight() / 2.0,
                mediumTokens.connectedPressedInnerCorner(),
                mediumTokens.connectedPressedInnerCorner(),
                pressed.getContainerHeight() / 2.0
        );
        assertStateLayerRadii(
                pressed,
                pressed.getContainerHeight() / 2.0,
                mediumTokens.connectedPressedInnerCorner(),
                mediumTokens.connectedPressedInnerCorner(),
                pressed.getContainerHeight() / 2.0
        );
        pressed.disarm();
    }

    /// Verifies the official flexible connected layout and compact accessible target minimum.
    @Test
    void connectedButtonGroupsFillAvailableWidthAndKeepCompactTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButton first = new M3IconToggleButton("First");
            M3IconToggleButton middle = new M3IconToggleButton("Middle");
            M3IconToggleButton last = new M3IconToggleButton("Last");
            M3ButtonGroup connected = new M3ButtonGroup();
            connected.getItems().addAll(first, middle, last);
            connected.setVariant(M3ButtonGroupVariant.CONNECTED);
            connected.setSize(M3ButtonSize.EXTRA_SMALL);

            M3ButtonGroup standard = buttonGroup(
                    new M3Button("Archive", M3ButtonVariant.TONAL),
                    new M3Button("Share", M3ButtonVariant.TONAL)
            );
            standard.setVariant(M3ButtonGroupVariant.STANDARD);
            standard.setSize(M3ButtonSize.SMALL);

            VBox root = new VBox(16.0, connected, standard);
            root.setPadding(new Insets(20.0));
            root.setFillWidth(true);
            Scene scene = new Scene(root, 520.0, 180.0);
            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(480.0, connected.getWidth(), 0.01);
                assertEquals(
                        connected.getWidth(),
                        first.getWidth() + middle.getWidth() + last.getWidth() + 2.0 * connected.getSpacing(),
                        0.01
                );
                assertTrue(first.getWidth() >= 48.0);
                assertTrue(middle.getWidth() >= 48.0);
                assertTrue(last.getWidth() >= 48.0);
                double standardPrefWidth = standard.prefWidth(-1.0);
                assertEquals(standardPrefWidth, standard.maxWidth(-1.0), 0.01);
                assertTrue(
                        standard.getWidth() <= standardPrefWidth + 1.0,
                        () -> "standard group expanded beyond its preferred width: width="
                                + standard.getWidth()
                                + ", prefWidth=" + standardPrefWidth
                                + ", maxWidth=" + standard.maxWidth(-1.0)
                );
                assertTrue(standard.getWidth() < connected.getWidth());

                connected.setSize(M3ButtonSize.SMALL);
                connected.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                connected.layout();

                assertEquals(480.0, connected.getWidth(), 0.01);
                assertTrue(first.getWidth() >= 48.0);
                assertTrue(first.localToScene(first.getBoundsInLocal()).getMinX()
                        > last.localToScene(last.getBoundsInLocal()).getMinX());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies rendered connected-group surfaces and state layers for action and toggle-icon children.
    @Tier2Test
    @Test
    void connectedButtonGroupsRenderStateSpecificInnerCorners() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button actionFirst = new M3Button("Archive", M3ButtonVariant.TONAL);
            M3Button actionLast = new M3Button("Share", M3ButtonVariant.TONAL);
            M3ButtonGroup actionGroup = buttonGroup(actionFirst, actionLast);
            actionGroup.setSize(M3ButtonSize.MEDIUM);

            M3IconToggleButton toggleFirst = new M3IconToggleButton("A");
            M3IconToggleButton toggleLast = new M3IconToggleButton("B");
            toggleFirst.setVariant(M3IconToggleButtonVariant.TONAL);
            toggleLast.setVariant(M3IconToggleButtonVariant.TONAL);
            toggleFirst.setSelected(true);
            M3ButtonGroup toggleGroup = new M3ButtonGroup();
            toggleGroup.getItems().addAll(toggleFirst, toggleLast);
            toggleGroup.setSize(M3ButtonSize.MEDIUM);

            VBox root = new VBox(12.0, actionGroup, toggleGroup);
            root.setStyle("-fx-background-color: white; -fx-padding: 16px;");
            Scene scene = new Scene(root, 360.0, 180.0);
            M3Theme theme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );
            M3ThemeManager.install(scene, theme);
            root.applyCss();
            root.resize(360.0, 180.0);
            root.layout();
            actionGroup.layout();
            toggleGroup.layout();
            actionFirst.layout();
            actionLast.layout();
            toggleFirst.layout();
            toggleLast.layout();
            root.applyCss();

            M3ComponentTokens.ButtonGroupSizeTokens tokens =
                    theme.tokens().componentTokens().buttonGroup().medium();
            double outerCorner = tokens.containerHeight() / 2.0;
            double restingInnerCorner = tokens.connectedInnerCorner();
            double selectedInnerCorner = tokens.connectedSelectedInnerCorner();
            assertRegionRadii(
                    actionFirst,
                    outerCorner,
                    restingInnerCorner,
                    restingInnerCorner,
                    outerCorner
            );
            assertRegionRadii(
                    actionLast,
                    restingInnerCorner,
                    outerCorner,
                    outerCorner,
                    restingInnerCorner
            );
            assertStateLayerRadii(
                    actionFirst,
                    outerCorner,
                    restingInnerCorner,
                    restingInnerCorner,
                    outerCorner
            );
            assertStateLayerRadii(
                    actionLast,
                    restingInnerCorner,
                    outerCorner,
                    outerCorner,
                    restingInnerCorner
            );
            assertRegionRadii(
                    toggleFirst,
                    outerCorner,
                    selectedInnerCorner,
                    selectedInnerCorner,
                    outerCorner
            );
            assertRegionRadii(
                    toggleLast,
                    restingInnerCorner,
                    outerCorner,
                    outerCorner,
                    restingInnerCorner
            );
            assertStateLayerRadii(
                    toggleFirst,
                    outerCorner,
                    selectedInnerCorner,
                    selectedInnerCorner,
                    outerCorner
            );
            assertStateLayerRadii(
                    toggleLast,
                    restingInnerCorner,
                    outerCorner,
                    outerCorner,
                    restingInnerCorner
            );

            PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
            toggleLast.pseudoClassStateChanged(pressed, true);
            root.applyCss();
            root.layout();
            toggleLast.layout();

            double pressedInnerCorner = tokens.connectedPressedInnerCorner();
            assertRegionRadii(
                    toggleLast,
                    pressedInnerCorner,
                    outerCorner,
                    outerCorner,
                    pressedInnerCorner
            );
            assertStateLayerRadii(
                    toggleLast,
                    pressedInnerCorner,
                    outerCorner,
                    outerCorner,
                    pressedInnerCorner
            );
            toggleLast.pseudoClassStateChanged(pressed, false);

            M3IconToggleButton standaloneFirst = new M3IconToggleButton("A");
            M3IconToggleButton standaloneLast = new M3IconToggleButton("B");
            standaloneFirst.setVariant(M3IconToggleButtonVariant.TONAL);
            standaloneLast.setVariant(M3IconToggleButtonVariant.TONAL);
            standaloneFirst.setSelected(true);
            M3ButtonGroup standaloneGroup = new M3ButtonGroup();
            standaloneGroup.getItems().addAll(standaloneFirst, standaloneLast);
            standaloneGroup.setSize(M3ButtonSize.MEDIUM);
            Pane standaloneRoot = new Pane(standaloneGroup);
            standaloneRoot.setStyle("-fx-background-color: white;");
            new Scene(standaloneRoot, 240.0, 80.0);
            standaloneRoot.applyCss();
            standaloneGroup.resize(
                    standaloneGroup.prefWidth(-1.0),
                    standaloneGroup.prefHeight(-1.0)
            );
            standaloneGroup.layout();
            standaloneFirst.layout();
            standaloneLast.layout();
            standaloneRoot.applyCss();
            standaloneFirst.requestLayout();
            standaloneLast.requestLayout();
            standaloneRoot.layout();
            standaloneGroup.layout();
            standaloneFirst.layout();
            standaloneLast.layout();

            assertTrue(standaloneFirst.getPseudoClassStates().stream()
                    .anyMatch(state -> state.getPseudoClassName().equals("connected-group")));
            assertRegionRadii(standaloneFirst, 28.0, 8.0, 8.0, 28.0);
            assertRegionRadii(standaloneLast, 8.0, 28.0, 28.0, 8.0);
            assertStateLayerRadii(standaloneFirst, 28.0, 8.0, 8.0, 28.0);
            assertStateLayerRadii(standaloneLast, 8.0, 28.0, 28.0, 8.0);
        });
    }

    /// Verifies that button group corners and feedback layers keep logical CSS shapes in right-to-left layout.
    @Test
    void buttonGroupUsesRightToLeftPositionSpecificShapes() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("Archive");
            M3Button second = new M3Button("Share");
            M3Button third = new M3Button("Edit");
            M3ButtonGroup group = buttonGroup(first, second, third);
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

            assertRegionRadii(first, 20.0, 8.0, 8.0, 20.0);
            assertRegionRadii(second, 8.0, 8.0, 8.0, 8.0);
            assertRegionRadii(third, 8.0, 20.0, 20.0, 8.0);
            assertStateLayerRadii(first, 20.0, 8.0, 8.0, 20.0);
            assertStateLayerRadii(second, 8.0, 8.0, 8.0, 8.0);
            assertStateLayerRadii(third, 8.0, 20.0, 20.0, 8.0);
        });
    }

    /// Verifies that right-to-left joined buttons render mirrored edge shapes in snapshots.
    @Tier2Test
    @Test
    void rightToLeftJoinedButtonsRenderMirroredCornersInSnapshot() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstGroupButton = createButton("Archive", M3ButtonVariant.TONAL);
            M3Button secondGroupButton = createButton("Share", M3ButtonVariant.TONAL);
            M3Button thirdGroupButton = createButton("Edit", M3ButtonVariant.TONAL);
            M3ButtonGroup buttonGroup =
                    buttonGroup(firstGroupButton, secondGroupButton, thirdGroupButton);
            buttonGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(day, week, month);
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
            assertSnapshotEdgeCorners(image, splitButtonActionButton(splitButton), false, true);
            assertSnapshotEdgeCorners(image, splitButtonMenuButton(splitButton), true, false);
        });
    }

    /// Verifies that a rendered region has large outer or compact inner physical top-edge corners.
    private static void assertSnapshotEdgeCorners(
            WritableImage image,
            Region region,
            boolean outerLeft,
            boolean outerRight
    ) {
        Color background = Color.WHITE;
        Bounds bounds = region.localToScene(region.getLayoutBounds());
        Insets insets = firstBackgroundInsets(region);
        double topY = bounds.getMinY() + insets.getTop() + 2.0;
        double leftDistance = colorDistance(
                snapshotScenePixel(image, bounds.getMinX() + insets.getLeft() + 6.0, topY),
                background
        );
        double rightDistance = colorDistance(
                snapshotScenePixel(image, bounds.getMaxX() - insets.getRight() - 7.0, topY),
                background
        );

        assertEquals(outerLeft, leftDistance < 0.04,
                () -> "left corner distance=" + leftDistance + ", region=" + region);
        assertEquals(outerRight, rightDistance < 0.04,
                () -> "right corner distance=" + rightDistance + ", region=" + region);
    }

    /// Resizes a button group to its preferred dimensions and lays out its skin container and children.
    private static void resizeButtonGroupToPreferredSize(M3ButtonGroup group) {
        group.resize(group.prefWidth(-1.0), group.prefHeight(-1.0));
        group.layout();
    }
}
