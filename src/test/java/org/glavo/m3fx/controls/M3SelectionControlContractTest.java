// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3CheckBoxSkin;
import org.glavo.m3fx.skins.M3RadioButtonSkin;
import org.glavo.m3fx.skins.M3SwitchSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies checkbox, radio button, and switch state and presentation.
@NotNullByDefault
final class M3SelectionControlContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// The center tolerance used for rendered selection indicators.
    private static final double SELECTION_INDICATOR_RENDERED_CENTER_TOLERANCE = 1.0;

    /// Verifies that disabled chips, segmented buttons, and list items render independent Material layers.
    @Tier2Test
    @Test
    void disabledSelectionControlsRenderIndependentMaterialLayers() {
        M3Theme theme = M3Theme.defaultTheme();
        Color onSurface = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE);
        Color disabledContainer = withOpacity(onSurface, 0.12);
        Color disabledContent = withOpacity(onSurface, 0.38);
        Color disabledListStateLayer = withOpacity(onSurface, 0.10);

        Region flatChipGraphic = new Region();
        flatChipGraphic.setPrefSize(12.0, 12.0);
        M3Chip flatChip = new M3AssistChip("Flat", flatChipGraphic);
        flatChip.setDisable(true);

        Region elevatedChipGraphic = new Region();
        elevatedChipGraphic.setPrefSize(12.0, 12.0);
        M3Chip elevatedChip = new M3AssistChip("Elevated", elevatedChipGraphic);
        elevatedChip.setChipStyle(M3ChipStyle.ELEVATED);

        Region selectedChipGraphic = new Region();
        selectedChipGraphic.setPrefSize(12.0, 12.0);
        M3FilterChip selectedChip = new M3FilterChip("Selected", selectedChipGraphic);
        selectedChip.setSelected(true);
        selectedChip.setDisable(true);

        Region segmentGraphic = new Region();
        segmentGraphic.setPrefSize(12.0, 12.0);
        M3SegmentedButton segment = new M3SegmentedButton("Day", segmentGraphic);
        segment.setDisable(true);

        Region selectedSegmentGraphic = new Region();
        selectedSegmentGraphic.setPrefSize(12.0, 12.0);
        M3SegmentedButton selectedSegment = new M3SegmentedButton("Week", selectedSegmentGraphic);
        selectedSegment.setSelected(true);
        selectedSegment.setDisable(true);

        Region listLeading = new Region();
        listLeading.setPrefSize(24.0, 24.0);
        M3ListItem listItem = new M3ListItem("Disabled row");
        listItem.setSupportingText("Supporting text");
        listItem.setLeading(listLeading);
        listItem.setDisable(true);

        Region selectedListLeading = new Region();
        selectedListLeading.setPrefSize(24.0, 24.0);
        M3ListItem selectedListItem = new M3ListItem("Selected disabled row");
        selectedListItem.setSupportingText("Supporting text");
        selectedListItem.setLeading(selectedListLeading);
        selectedListItem.setSelected(true);
        selectedListItem.setDisable(true);

        VBox root = new VBox(
                new HBox(flatChip, elevatedChip, selectedChip),
                new HBox(segment, selectedSegment),
                listItem,
                selectedListItem
        );
        Scene scene = new Scene(root, 720.0, 360.0);
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        root.layout();

        assertEquals(1.0, flatChip.getOpacity(), 0.0001);
        assertRegionFill(flatChip, Color.TRANSPARENT);
        assertBorderColor(flatChip, disabledContainer);
        assertEquals(onSurface, flatChip.getTextFill());
        assertEquals(0.38, flatChipGraphic.getOpacity(), 0.0001);
        assertEquals(0.38, assertInstanceOf(Text.class, flatChip.lookup(".text")).getOpacity(), 0.0001);

        assertInstanceOf(DropShadow.class, elevatedChip.getEffect(), "enabled elevated chip elevation");
        elevatedChip.setDisable(true);
        root.applyCss();
        assertEquals(1.0, elevatedChip.getOpacity(), 0.0001);
        assertRegionFill(elevatedChip, disabledContainer);
        assertBorderColor(elevatedChip, Color.TRANSPARENT);
        assertEquals(onSurface, elevatedChip.getTextFill());
        assertEquals(0.38, elevatedChipGraphic.getOpacity(), 0.0001);
        assertNull(elevatedChip.getEffect(), "dynamically disabled elevated chip elevation");

        assertEquals(1.0, selectedChip.getOpacity(), 0.0001);
        assertRegionFill(selectedChip, disabledContainer);
        assertBorderColor(selectedChip, Color.TRANSPARENT);
        assertEquals(onSurface, selectedChip.getTextFill());
        assertEquals(0.38, selectedChipGraphic.getOpacity(), 0.0001);

        assertEquals(1.0, segment.getOpacity(), 0.0001);
        assertRegionFill(segment, Color.TRANSPARENT);
        assertBorderColor(segment, disabledContainer);
        assertEquals(onSurface, segment.getTextFill());
        assertEquals(0.38, segmentGraphic.getOpacity(), 0.0001);
        assertEquals(0.38, assertInstanceOf(Text.class, segment.lookup(".text")).getOpacity(), 0.0001);

        assertEquals(1.0, selectedSegment.getOpacity(), 0.0001);
        assertRegionFill(selectedSegment, Color.TRANSPARENT);
        assertBorderColor(selectedSegment, disabledContainer);
        assertEquals(onSurface, selectedSegment.getTextFill());
        assertEquals(0.0, selectedSegmentGraphic.getOpacity(), 0.0001);
        assertEquals(
                0.38,
                lookupRegion(selectedSegment, ".m3-segmented-button-selection-indicator-mark").getOpacity(),
                0.0001
        );
        assertRegionFill(segmentedButtonSelectionContainer(selectedSegment), disabledContainer);
        assertEquals(1.0, segmentedButtonSelectionContainer(selectedSegment).getOpacity(), 0.0001);

        assertEquals(1.0, listItem.getOpacity(), 0.0001);
        assertRegionFill(listItemContainer(listItem), disabledListStateLayer);
        assertEquals(disabledContent, listItemHeadlineLabel(listItem).getTextFill());
        Region listLeadingSlot = lookupRegion(listItem, ".m3-list-item-leading");
        assertEquals(0.38, listLeadingSlot.getOpacity(), 0.0001);
        assertEquals(1.0, listLeading.getOpacity(), 0.0001);

        assertEquals(1.0, selectedListItem.getOpacity(), 0.0001);
        assertRegionFill(listItemContainer(selectedListItem), disabledListStateLayer);
        assertRegionFill(listItemSelectionContainer(selectedListItem), disabledContent);
        assertEquals(1.0, listItemSelectionContainer(selectedListItem).getOpacity(), 0.0001);
        assertEquals(disabledContent, listItemHeadlineLabel(selectedListItem).getTextFill());
        Region selectedListLeadingSlot = lookupRegion(selectedListItem, ".m3-list-item-leading");
        assertEquals(0.38, selectedListLeadingSlot.getOpacity(), 0.0001);
        assertEquals(1.0, selectedListLeading.getOpacity(), 0.0001);
    }

    /// Verifies that selection component token properties are styleable from CSS.
    @Test
    void selectionTokensAreStyleable() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        checkBox.setStyle("""
                -m3-touch-target-size: 44px;
                -m3-state-layer-size: 38px;
                -m3-container-size: 20px;
                -m3-selected-mark-width: 13px;
                -m3-selected-mark-height: 11px;
                -m3-indeterminate-mark-width: 14px;
                -m3-indeterminate-mark-height: 3px;
                """);

        M3RadioButton radioButton = new M3RadioButton("Radio");
        radioButton.setStyle("""
                -m3-touch-target-size: 46px;
                -m3-state-layer-size: 36px;
                -m3-container-size: 22px;
                -m3-selected-dot-size: 12px;
                """);

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setStyle("""
                -m3-touch-target-size: 50px;
                -m3-track-shape: 18px;
                -m3-track-width: 60px;
                -m3-track-height: 36px;
                -m3-state-layer-size: 44px;
                -m3-unselected-handle-size: 18px;
                -m3-with-icon-handle-size: 27px;
                -m3-selected-handle-size: 26px;
                -m3-pressed-handle-size: 30px;
                -m3-icon-size: 15px;
                """);

        applyCss(checkBox);
        applyCss(radioButton);
        applyCss(switchControl);

        assertEquals(44.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(38.0, checkBox.getStateLayerSize(), 0.0001);
        assertEquals(20.0, checkBox.getContainerSize(), 0.0001);
        assertEquals(13.0, checkBox.getSelectedMarkWidth(), 0.0001);
        assertEquals(11.0, checkBox.getSelectedMarkHeight(), 0.0001);
        assertEquals(14.0, checkBox.getIndeterminateMarkWidth(), 0.0001);
        assertEquals(3.0, checkBox.getIndeterminateMarkHeight(), 0.0001);
        assertEquals(44.0, checkBox.prefHeight(-1.0), 0.0001);
        assertEquals(46.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(36.0, radioButton.getStateLayerSize(), 0.0001);
        assertEquals(22.0, radioButton.getContainerSize(), 0.0001);
        assertEquals(12.0, radioButton.getSelectedDotSize(), 0.0001);
        assertEquals(46.0, radioButton.prefHeight(-1.0), 0.0001);
        assertEquals(50.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(18.0, switchControl.getTrackShape(), 0.0001);
        assertEquals(60.0, switchControl.getTrackWidth(), 0.0001);
        assertEquals(36.0, switchControl.getTrackHeight(), 0.0001);
        assertEquals(44.0, switchControl.getStateLayerSize(), 0.0001);
        assertEquals(18.0, switchControl.getUnselectedHandleSize(), 0.0001);
        assertEquals(27.0, switchControl.getWithIconHandleSize(), 0.0001);
        assertEquals(26.0, switchControl.getSelectedHandleSize(), 0.0001);
        assertEquals(30.0, switchControl.getPressedHandleSize(), 0.0001);
        assertEquals(15.0, switchControl.getIconSize(), 0.0001);
        assertEquals(50.0, switchControl.prefHeight(-1.0), 0.0001);

        switchControl.setStyle(switchControl.getStyle() + " -m3-state-layer-size: 54px;");
        applyCss(switchControl);
        assertEquals(54.0, switchControl.getStateLayerSize(), 0.0001);
        assertEquals(54.0, switchControl.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that switch handle icons use the MD3 icon slot, handle size, color roles, and ownership lifecycle.
    @Test
    void switchSupportsSelectedAndUnselectedHandleIcons() {
        SVGPath selectedIcon = new SVGPath();
        selectedIcon.setContent("M 2 8 L 6 12 L 14 3 L 12 1 L 6 8 L 4 6 Z");
        selectedIcon.getStyleClass().add("application-selected-icon");
        SVGPath unselectedIcon = new SVGPath();
        unselectedIcon.setContent("M 3 3 L 13 13 M 13 3 L 3 13");
        M3Switch switchControl = new M3Switch("Notifications");
        switchControl.setSelectedIcon(selectedIcon);
        switchControl.setUnselectedIcon(unselectedIcon);
        switchControl.setAnimationsEnabled(false);

        Pane root = new Pane(switchControl);
        Scene scene = new Scene(root, 180.0, 80.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle("""
                -m3-color-primary: rgb(1,2,3);
                -m3-color-surface-container-highest: rgb(10,11,12);
                -m3-color-on-surface: rgb(20,21,22);
                """);
        root.applyCss();
        switchControl.resizeRelocate(20.0, 16.0, 140.0, 48.0);
        switchControl.layout();

        Region thumb = lookupRegion(switchControl, ".thumb");
        Region iconSlot = lookupRegion(switchControl, ".m3-switch-icon-slot");
        assertSame(iconSlot, unselectedIcon.getParent());
        assertNull(selectedIcon.getParent());
        assertEquals(24.0, thumb.getWidth(), 0.0001);
        assertEquals(24.0, thumb.getHeight(), 0.0001);
        assertEquals(16.0, iconSlot.getWidth(), 0.0001);
        assertEquals(16.0, iconSlot.getHeight(), 0.0001);
        assertEquals(Color.rgb(10, 11, 12), unselectedIcon.getFill());
        assertTrue(unselectedIcon.getStyleClass().contains("m3-switch-handle-icon"));

        switchControl.setDisable(true);
        root.applyCss();
        assertEquals(Color.rgb(10, 11, 12), unselectedIcon.getFill());
        assertEquals(0.38, unselectedIcon.getOpacity(), 0.0001);

        switchControl.setDisable(false);
        switchControl.setSelected(true);
        root.applyCss();
        switchControl.layout();

        assertSame(iconSlot, selectedIcon.getParent());
        assertNull(unselectedIcon.getParent());
        assertFalse(unselectedIcon.getStyleClass().contains("m3-switch-handle-icon"));
        assertTrue(selectedIcon.getStyleClass().contains("application-selected-icon"));
        assertTrue(selectedIcon.getStyleClass().contains("m3-switch-handle-icon"));
        assertEquals(Color.rgb(1, 2, 3), selectedIcon.getFill());
        assertEquals(1.0, iconSlot.getOpacity(), 0.0001);

        switchControl.setDisable(true);
        root.applyCss();
        assertEquals(Color.rgb(20, 21, 22), selectedIcon.getFill());
        assertEquals(0.38, selectedIcon.getOpacity(), 0.0001);

        M3SwitchSkin skin = assertInstanceOf(M3SwitchSkin.class, switchControl.getSkin());
        skin.dispose();

        assertNull(selectedIcon.getParent());
        assertFalse(selectedIcon.getStyleClass().contains("m3-switch-handle-icon"));
        assertTrue(selectedIcon.getStyleClass().contains("application-selected-icon"));
    }

    /// Verifies MD3 hover, focus, pressed, and disabled color mappings for selection controls.
    @Test
    void selectionControlStateColorsFollowMaterialTokens() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        Pane root = new Pane(checkBox, radioButton, switchControl);
        Scene scene = new Scene(root, 420.0, 80.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle("""
                -m3-color-primary: rgb(1,2,3);
                -m3-color-on-surface: rgb(4,5,6);
                -m3-color-on-surface-variant: rgb(7,8,9);
                -m3-color-surface-container-highest: rgb(10,11,12);
                -m3-color-error: rgb(13,14,15);
                -m3-color-on-error: rgb(16,17,18);
                -m3-color-surface: rgb(19,20,21);
                -m3-color-outline: rgb(22,23,24);
                -m3-color-on-primary: rgb(25,26,27);
                -m3-color-primary-container: rgb(28,29,30);
                """);
        root.applyCss();

        Region checkBoxContainer = lookupRegion(checkBox, ".box");
        Shape radioRing = radioRing(radioButton);
        Region checkBoxStateLayer = lookupRegion(checkBox, ".m3-state-layer");
        Region radioStateLayer = lookupRegion(radioButton, ".m3-state-layer");
        assertBorderColor(checkBoxContainer, Color.rgb(7, 8, 9));
        assertEquals(Color.rgb(7, 8, 9), radioRing.getStroke());

        checkBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        radioButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        root.applyCss();
        assertBorderColor(checkBoxContainer, Color.rgb(4, 5, 6));
        assertEquals(Color.rgb(4, 5, 6), radioRing.getStroke());

        checkBox.arm();
        radioButton.arm();
        root.applyCss();
        assertRegionFill(checkBoxStateLayer, Color.rgb(1, 2, 3));
        assertRegionFill(radioStateLayer, Color.rgb(1, 2, 3));

        checkBox.setSelected(true);
        radioButton.setSelected(true);
        root.applyCss();
        assertRegionFill(checkBoxStateLayer, Color.rgb(4, 5, 6));
        assertRegionFill(radioStateLayer, Color.rgb(4, 5, 6));
        assertNoBorder(checkBoxContainer);

        checkBox.disarm();
        checkBox.setSelected(false);
        checkBox.setError(true);
        root.applyCss();
        assertTrue(checkBox.getPseudoClassStates().contains(PseudoClass.getPseudoClass("error")));
        assertBorderColor(checkBoxContainer, Color.rgb(13, 14, 15));
        assertRegionFill(checkBoxStateLayer, Color.rgb(13, 14, 15));

        checkBox.setSelected(true);
        root.applyCss();
        assertRegionFill(checkBoxContainer, Color.rgb(13, 14, 15));
        assertRegionFill(lookupRegion(checkBox, ".mark"), Color.rgb(16, 17, 18));
        assertNoBorder(checkBoxContainer);

        checkBox.setSelected(false);
        checkBox.setIndeterminate(true);
        root.applyCss();
        assertRegionFill(checkBoxContainer, Color.rgb(13, 14, 15));
        assertRegionFill(lookupRegion(checkBox, ".mark"), Color.rgb(16, 17, 18));
        assertNoBorder(checkBoxContainer);

        checkBox.setDisable(true);
        root.applyCss();
        assertRegionFill(checkBoxContainer, Color.rgb(4, 5, 6));
        assertEquals(0.38, checkBoxContainer.getOpacity(), 0.0001);
        assertRegionFill(lookupRegion(checkBox, ".mark"), Color.rgb(19, 20, 21));

        Region switchUnselectedTrack = lookupRegion(switchControl, ".m3-switch-track-unselected");
        Region switchSelectedTrack = lookupRegion(switchControl, ".m3-switch-track-selected");
        Region switchUnselectedThumb = lookupRegion(switchControl, ".m3-switch-thumb-unselected");
        Region switchSelectedThumb = lookupRegion(switchControl, ".m3-switch-thumb-selected");
        Region switchStateLayer = lookupRegion(switchControl, ".m3-state-layer");
        assertRegionFill(switchUnselectedTrack, Color.rgb(10, 11, 12));
        assertBorderColor(switchUnselectedTrack, Color.rgb(22, 23, 24));
        assertRegionFill(switchUnselectedThumb, Color.rgb(22, 23, 24));
        assertRegionFill(switchStateLayer, Color.rgb(4, 5, 6));

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();
        assertRegionFill(switchUnselectedTrack, Color.rgb(10, 11, 12));
        assertBorderColor(switchUnselectedTrack, Color.rgb(22, 23, 24));
        assertRegionFill(switchUnselectedThumb, Color.rgb(7, 8, 9));
        assertRegionFill(switchStateLayer, Color.rgb(4, 5, 6));

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        root.applyCss();
        assertRegionFill(switchUnselectedThumb, Color.rgb(7, 8, 9));
        assertRegionFill(switchStateLayer, Color.rgb(4, 5, 6));

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), false);
        switchControl.arm();
        root.applyCss();
        assertRegionFill(switchUnselectedThumb, Color.rgb(7, 8, 9));
        assertRegionFill(switchStateLayer, Color.rgb(4, 5, 6));

        switchControl.disarm();
        switchControl.setSelected(true);
        root.applyCss();
        assertRegionFill(switchSelectedTrack, Color.rgb(1, 2, 3));
        assertNoBorder(switchSelectedTrack);
        assertRegionFill(switchSelectedThumb, Color.rgb(25, 26, 27));
        assertRegionFill(switchStateLayer, Color.rgb(1, 2, 3));

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();
        assertRegionFill(switchSelectedThumb, Color.rgb(28, 29, 30));
        assertRegionFill(switchStateLayer, Color.rgb(1, 2, 3));

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        switchControl.setDisable(true);
        root.applyCss();
        assertRegionFill(switchSelectedTrack, Color.rgb(4, 5, 6));
        assertEquals(0.12, switchSelectedTrack.getOpacity(), 0.0001);
        assertRegionFill(switchSelectedThumb, Color.rgb(19, 20, 21));
        assertEquals(1.0, switchSelectedThumb.getOpacity(), 0.0001);

        switchControl.setSelected(false);
        root.applyCss();
        assertRegionFill(switchUnselectedTrack, Color.rgb(10, 11, 12));
        assertBorderColor(switchUnselectedTrack, Color.rgb(4, 5, 6));
        assertEquals(0.12, switchUnselectedTrack.getOpacity(), 0.0001);
        assertRegionFill(switchUnselectedThumb, Color.rgb(4, 5, 6));
        assertEquals(0.38, switchUnselectedThumb.getOpacity(), 0.0001);
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
        Node uncheckedDotLayer = radioDotAnimationLayer(uncheckedRadioButton);
        Node checkedDotLayer = radioDotAnimationLayer(checkedRadioButton);
        assertEquals(0.0, uncheckedMark.getOpacity(), 0.0001);
        assertTrue(uncheckedMark.getScaleX() < 1.0);
        assertEquals(1.0, checkedMark.getOpacity(), 0.0001);
        assertEquals(1.0, checkedMark.getScaleX(), 0.0001);
        assertEquals(0.0, uncheckedDotLayer.getOpacity(), 0.0001);
        assertTrue(uncheckedDotLayer.getScaleX() < 1.0);
        assertEquals(1.0, checkedDotLayer.getOpacity(), 0.0001);
        assertEquals(1.0, checkedDotLayer.getScaleX(), 0.0001);
    }

    /// Verifies that selection-control skins consume component geometry and focus-indicator tokens.
    @Test
    void selectionIndicatorGeometryUsesComponentTokens() {
        M3CheckBox selectedCheckBox = createCheckBox("Checked", true);
        selectedCheckBox.setStyle("""
                -m3-touch-target-size: 52px;
                -m3-state-layer-size: 40px;
                -m3-container-size: 20px;
                -m3-selected-mark-width: 14px;
                -m3-selected-mark-height: 11px;
                -m3-indeterminate-mark-width: 16px;
                -m3-indeterminate-mark-height: 3px;
                """);

        M3CheckBox indeterminateCheckBox = new M3CheckBox("Mixed");
        indeterminateCheckBox.setIndeterminate(true);
        indeterminateCheckBox.setStyle(selectedCheckBox.getStyle());

        M3RadioButton selectedRadioButton = createRadioButton("Radio", true);
        selectedRadioButton.setStyle("""
                -m3-touch-target-size: 50px;
                -m3-state-layer-size: 36px;
                -m3-container-size: 22px;
                -m3-selected-dot-size: 12px;
                """);

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setStyle("""
                -m3-touch-target-size: 50px;
                -m3-track-shape: 18px;
                -m3-track-width: 60px;
                -m3-track-height: 36px;
                -m3-state-layer-size: 44px;
                """);

        Pane root = new Pane(selectedCheckBox, indeterminateCheckBox, selectedRadioButton, switchControl);
        Scene scene = new Scene(root, 560.0, 240.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        selectedCheckBox.resizeRelocate(0.0, 0.0, 180.0, 52.0);
        indeterminateCheckBox.resizeRelocate(0.0, 56.0, 180.0, 52.0);
        selectedRadioButton.resizeRelocate(0.0, 112.0, 180.0, 50.0);
        switchControl.resizeRelocate(0.0, 168.0, 180.0, 50.0);
        root.layout();
        applyPseudoState(selectedCheckBox, "focus-visible");
        applyPseudoState(selectedRadioButton, "focus-visible");
        applyPseudoState(switchControl, "focus-visible");
        root.applyCss();
        root.layout();
        M3StateLayerTokens stateTokens = M3Theme.defaultTheme().tokens().stateLayerTokens();
        double focusExpansion = stateTokens.focusIndicatorOuterOffset() + stateTokens.focusIndicatorThickness();

        Region selectedSlot = lookupRegion(selectedCheckBox, ".m3-selection-indicator");
        Region selectedLayer = lookupRegion(selectedCheckBox, ".m3-state-layer-container");
        Region selectedBox = lookupRegion(selectedCheckBox, ".m3-checkbox-box");
        Region selectedMark = lookupRegion(selectedCheckBox, ".m3-checkbox-mark");
        Region selectedFocusIndicator = lookupRegion(selectedCheckBox, ".m3-focus-indicator");
        Bounds selectedLayerBounds = selectedLayer.localToScene(selectedLayer.getLayoutBounds());
        Bounds selectedFocusBounds = selectedFocusIndicator.localToScene(selectedFocusIndicator.getBoundsInLocal());
        assertEquals(52.0, selectedSlot.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(52.0, selectedSlot.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(40.0, selectedLayer.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(40.0, selectedLayer.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(6.0, selectedLayer.getLayoutX(), 0.0001);
        assertEquals(6.0, selectedLayer.getLayoutY(), 0.0001);
        assertEquals(20.0, selectedBox.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(20.0, selectedBox.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(14.0, selectedMark.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(11.0, selectedMark.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(selectedLayerBounds.getWidth() + focusExpansion * 2.0,
                selectedFocusBounds.getWidth(), 0.0001);
        assertEquals(selectedLayerBounds.getHeight() + focusExpansion * 2.0,
                selectedFocusBounds.getHeight(), 0.0001);
        assertEquals(selectedLayerBounds.getCenterX(), selectedFocusBounds.getCenterX(), 0.0001,
                "Checkbox focus indicator should follow the circular state layer horizontally");
        assertEquals(selectedLayerBounds.getCenterY(), selectedFocusBounds.getCenterY(), 0.0001,
                "Checkbox focus indicator should follow the circular state layer vertically");

        Region indeterminateMark = lookupRegion(indeterminateCheckBox, ".m3-checkbox-mark");
        assertEquals(16.0, indeterminateMark.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(3.0, indeterminateMark.getLayoutBounds().getHeight(), 0.0001);

        Region radioSlot = lookupRegion(selectedRadioButton, ".m3-selection-indicator");
        Region radioLayer = lookupRegion(selectedRadioButton, ".m3-state-layer-container");
        Region radio = radioIndicator(selectedRadioButton);
        Shape ring = radioRing(selectedRadioButton);
        Shape dot = radioDot(selectedRadioButton);
        Region radioFocusIndicator = lookupRegion(selectedRadioButton, ".m3-focus-indicator");
        Bounds radioLayerBounds = radioLayer.localToScene(radioLayer.getLayoutBounds());
        Bounds radioFocusBounds = radioFocusIndicator.localToScene(radioFocusIndicator.getBoundsInLocal());
        assertEquals(50.0, radioSlot.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(50.0, radioSlot.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(36.0, radioLayer.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(36.0, radioLayer.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(7.0, radioLayer.getLayoutX(), 0.0001);
        assertEquals(7.0, radioLayer.getLayoutY(), 0.0001);
        assertEquals(22.0, radio.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(22.0, radio.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(22.0, ring.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(22.0, ring.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(12.0, dot.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(12.0, dot.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(radioLayerBounds.getWidth() + focusExpansion * 2.0,
                radioFocusBounds.getWidth(), 0.0001);
        assertEquals(radioLayerBounds.getHeight() + focusExpansion * 2.0,
                radioFocusBounds.getHeight(), 0.0001);
        assertEquals(radioLayerBounds.getCenterX(), radioFocusBounds.getCenterX(), 0.0001,
                "Radio-button focus indicator should follow the circular state layer horizontally");
        assertEquals(radioLayerBounds.getCenterY(), radioFocusBounds.getCenterY(), 0.0001,
                "Radio-button focus indicator should follow the circular state layer vertically");

        Region switchTrack = lookupRegion(switchControl, ".m3-switch-track");
        Region switchLayer = lookupRegion(switchControl, ".m3-state-layer-container");
        Region focusIndicator = lookupRegion(switchControl, ".m3-focus-indicator");
        Labeled switchLabel = assertInstanceOf(Labeled.class, switchControl.lookup(".m3-selection-label"));
        Bounds trackBounds = switchTrack.localToScene(switchTrack.getBoundsInLocal());
        Bounds focusBounds = focusIndicator.localToScene(focusIndicator.getBoundsInLocal());
        Bounds labelBounds = switchLabel.localToScene(switchLabel.getBoundsInLocal());
        assertEquals(60.0, trackBounds.getWidth(), 0.0001);
        assertEquals(36.0, trackBounds.getHeight(), 0.0001);
        assertEquals(44.0, switchLayer.getWidth(), 0.0001);
        assertEquals(44.0, switchLayer.getHeight(), 0.0001);
        assertEquals(trackBounds.getWidth() + focusExpansion * 2.0, focusBounds.getWidth(), 0.0001);
        assertEquals(trackBounds.getHeight() + focusExpansion * 2.0, focusBounds.getHeight(), 0.0001);
        assertEquals(
                (trackBounds.getMinX() + trackBounds.getMaxX()) / 2.0,
                (focusBounds.getMinX() + focusBounds.getMaxX()) / 2.0,
                0.0001,
                "Switch focus indicator should follow the complete track horizontally"
        );
        assertEquals(
                (trackBounds.getMinY() + trackBounds.getMaxY()) / 2.0,
                (focusBounds.getMinY() + focusBounds.getMaxY()) / 2.0,
                0.0001,
                "Switch focus indicator should follow the complete track vertically"
        );
        assertTrue(focusBounds.getMaxX() <= labelBounds.getMinX(),
                () -> "Switch focus indicator should not overlap its label: focus="
                        + focusBounds + ", label=" + labelBounds);
    }

    /// Verifies that selection control skins handle pointer and keyboard activation.
    @Test
    void selectionControlSkinsHandleActivationEvents() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio A");
        M3RadioButton nextRadioButton = new M3RadioButton("Radio B");
        M3RadioButton disabledRadioButton = new M3RadioButton("Radio C");
        M3Switch switchControl = new M3Switch("Switch");
        ToggleGroup radioGroup = new ToggleGroup();
        radioButton.setToggleGroup(radioGroup);
        nextRadioButton.setToggleGroup(radioGroup);
        disabledRadioButton.setToggleGroup(radioGroup);
        disabledRadioButton.setDisable(true);
        Pane root = new Pane(checkBox, radioButton, nextRadioButton, disabledRadioButton, switchControl);
        Scene scene = new Scene(root, 480.0, 160.0);

        M3Theme theme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        checkBox.resize(120.0, 48.0);
        radioButton.resize(120.0, 48.0);
        nextRadioButton.resize(120.0, 48.0);
        disabledRadioButton.resize(120.0, 48.0);
        switchControl.resize(120.0, 48.0);
        switchControl.setAnimationsEnabled(false);
        switchControl.layout();

        checkBox.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(checkBox.isArmed());
        checkBox.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertTrue(checkBox.isSelected());

        radioButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(radioButton.isArmed());
        radioButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertTrue(radioButton.isSelected());

        KeyEvent right = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
        radioButton.fireEvent(right);
        assertTrue(nextRadioButton.isSelected());

        KeyEvent down = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        nextRadioButton.fireEvent(down);
        assertTrue(radioButton.isSelected(), "directional navigation must skip disabled toggles and wrap");

        radioButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        radioButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(nextRadioButton.isSelected(), "right must move backward in RTL orientation");

        KeyEvent enter = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER);
        radioButton.fireEvent(enter);
        assertFalse(enter.isConsumed());
        assertFalse(radioButton.isSelected(), "Material radio buttons use Space rather than Enter for activation");

        switchControl.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        assertTrue(switchControl.isArmed());
        switchControl.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertTrue(switchControl.isSelected());

        AtomicInteger switchActions = new AtomicInteger();
        switchControl.setOnAction(event -> switchActions.incrementAndGet());
        Region switchTrack = lookupRegion(switchControl, ".m3-switch-track");
        Region unselectedTrackLayer = lookupRegion(switchControl, ".m3-switch-track-unselected-layer");
        Region selectedTrackLayer = lookupRegion(switchControl, ".m3-switch-track-selected-layer");
        Region unselectedThumbLayer = lookupRegion(switchControl, ".m3-switch-thumb-unselected-layer");
        Region selectedThumbLayer = lookupRegion(switchControl, ".m3-switch-thumb-selected-layer");
        Region unselectedTrackPaint = lookupRegion(switchControl, ".m3-switch-track-unselected");
        Region selectedTrackPaint = lookupRegion(switchControl, ".m3-switch-track-selected");
        Region unselectedThumbPaint = lookupRegion(switchControl, ".m3-switch-thumb-unselected");
        Region selectedThumbPaint = lookupRegion(switchControl, ".m3-switch-thumb-selected");
        double offCenter = switchControl.getTrackHeight() / 2.0;
        double onCenter = switchTrack.getWidth() - offCenter;
        double trackCenterY = switchTrack.getHeight() / 2.0;
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_PRESSED,
                onCenter,
                trackCenterY,
                true
        ));
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_DRAGGED,
                offCenter,
                trackCenterY,
                true
        ));
        root.applyCss();
        assertTrue(switchControl.isSelected(), "Dragging must not commit selection before release");
        assertEquals(1.0, unselectedTrackLayer.getOpacity(), 0.0001);
        assertEquals(0.0, selectedTrackLayer.getOpacity(), 0.0001);
        assertRegionFill(
                unselectedTrackPaint,
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE_CONTAINER_HIGHEST)
        );
        javafx.scene.layout.BorderStroke offPreviewBorder = unselectedTrackPaint.getBorder().getStrokes().get(0);
        assertEquals(theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.OUTLINE),
                offPreviewBorder.getTopStroke());
        assertEquals(2.0, offPreviewBorder.getWidths().getTop(), 0.0001);
        assertRegionFill(
                unselectedThumbPaint,
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE_VARIANT)
        );
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_RELEASED,
                offCenter,
                trackCenterY,
                false
        ));
        assertFalse(switchControl.isSelected(), "Dragging the handle to the off side should clear selection");
        assertEquals(1, switchActions.get(), "A committed handle drag should fire one action event");

        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_PRESSED,
                offCenter,
                trackCenterY,
                true
        ));
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_DRAGGED,
                offCenter + (onCenter - offCenter) * 0.49,
                trackCenterY,
                true
        ));
        assertEquals(0.51, unselectedTrackLayer.getOpacity(), 0.0001);
        assertEquals(0.49, selectedTrackLayer.getOpacity(), 0.0001);
        assertEquals(0.51, unselectedThumbLayer.getOpacity(), 0.0001);
        assertEquals(0.49, selectedThumbLayer.getOpacity(), 0.0001);
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_DRAGGED,
                offCenter + (onCenter - offCenter) * 0.51,
                trackCenterY,
                true
        ));
        assertEquals(0.49, unselectedTrackLayer.getOpacity(), 0.0001);
        assertEquals(0.51, selectedTrackLayer.getOpacity(), 0.0001);
        assertEquals(0.49, unselectedThumbLayer.getOpacity(), 0.0001);
        assertEquals(0.51, selectedThumbLayer.getOpacity(), 0.0001);
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_DRAGGED,
                onCenter,
                trackCenterY,
                true
        ));
        assertFalse(switchControl.isSelected(), "Dragging should not commit the selected state before release");
        assertEquals(0.0, unselectedTrackLayer.getOpacity(), 0.0001);
        assertEquals(1.0, selectedTrackLayer.getOpacity(), 0.0001);
        assertRegionFill(
                selectedTrackPaint,
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY)
        );
        assertNoBorder(selectedTrackPaint);
        assertRegionFill(
                selectedThumbPaint,
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY_CONTAINER)
        );
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_RELEASED,
                onCenter,
                trackCenterY,
                false
        ));
        assertTrue(switchControl.isSelected(), "Dragging the handle to the on side should select the switch");
        assertEquals(2, switchActions.get(), "Opposite handle drags should each commit one action event");

        switchControl.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        root.layout();
        switchControl.layout();
        Bounds rtlTrackBounds = switchTrack.localToScene(switchTrack.getBoundsInLocal());
        Bounds rtlSelectedThumbBounds = lookupRegion(switchControl, ".m3-switch-thumb")
                .localToScene(lookupRegion(switchControl, ".m3-switch-thumb").getBoundsInLocal());
        assertTrue(rtlSelectedThumbBounds.getCenterX() < rtlTrackBounds.getCenterX(),
                "The selected handle should occupy the logical end side in RTL orientation");

        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_PRESSED,
                onCenter,
                trackCenterY,
                true
        ));
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_DRAGGED,
                offCenter,
                trackCenterY,
                true
        ));
        switchTrack.fireEvent(primaryMouseEvent(
                switchTrack,
                MouseEvent.MOUSE_RELEASED,
                offCenter,
                trackCenterY,
                false
        ));
        assertFalse(switchControl.isSelected(), "Dragging toward the logical start should clear an RTL switch");
        assertEquals(3, switchActions.get(), "An RTL handle drag should fire one action event");
        Bounds rtlUnselectedThumbBounds = lookupRegion(switchControl, ".m3-switch-thumb")
                .localToScene(lookupRegion(switchControl, ".m3-switch-thumb").getBoundsInLocal());
        assertTrue(rtlUnselectedThumbBounds.getCenterX() > rtlTrackBounds.getCenterX(),
                "The unselected handle should occupy the logical start side in RTL orientation");

        switchControl.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        root.applyCss();
        root.layout();
        switchControl.layout();
        switchControl.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 100.0, 24.0, true));
        switchControl.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 100.0, 24.0, false));
        assertTrue(switchControl.isSelected(), "Clicking the adjacent switch label should still toggle selection");
        assertEquals(4, switchActions.get(), "Label activation should continue to fire one action event");
    }

    /// Verifies that selection indicator animations expose intermediate and final rendered states.
    @Tier2Test
    @Test
    void selectionIndicatorAnimationsRenderIntermediateAndFinalStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable FlowPane> rowReference = new AtomicReference<>();
        AtomicReference<@Nullable M3CheckBox> checkBoxReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RadioButton> radioButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Switch> switchReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> showObservableSelectionControls(
                    stageReference,
                    rowReference,
                    checkBoxReference,
                    radioButtonReference,
                    switchReference
            ));
            FxTestUtils.runOnFxThreadWhen(
                    () -> selectionIndicatorsSelecting(checkBoxReference, radioButtonReference, switchReference),
                    () -> {
                        Objects.requireNonNull(checkBoxReference.get(), "checkBox").setSelected(true);
                        Objects.requireNonNull(radioButtonReference.get(), "radioButton").setSelected(true);
                        Objects.requireNonNull(switchReference.get(), "switchControl").setSelected(true);
                    },
                    () -> {
                        FlowPane row = Objects.requireNonNull(rowReference.get(), "row");
                        M3CheckBox checkBox = Objects.requireNonNull(checkBoxReference.get(), "checkBox");
                        M3RadioButton radioButton =
                                Objects.requireNonNull(radioButtonReference.get(), "radioButton");
                        M3Switch switchControl = Objects.requireNonNull(switchReference.get(), "switchControl");

                        assertSelectionIndicatorsSelecting(checkBox, radioButton, switchControl);
                        WritableImage image = snapshotImageOnFxThread(row);
                        assertSnapshotNodeContainsContrast(image, lookupRegion(checkBox, ".mark"), Color.WHITE, 0.05);
                        assertSnapshotNodeContainsContrast(image, lookupShape(radioButton, ".dot"), Color.WHITE, 0.05);
                        assertSnapshotNodeContainsContrast(
                                image,
                                lookupRegion(switchControl, ".thumb"),
                                Color.rgb(84, 50, 185),
                                0.05
                        );
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-selection-animation-frame.png"
                        ));
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> selectionIndicatorsSelectedSettled(checkBoxReference, radioButtonReference, switchReference),
                    () -> {
                    },
                    () -> assertSelectionIndicatorsSelectedSettled(
                            Objects.requireNonNull(checkBoxReference.get(), "checkBox"),
                            Objects.requireNonNull(radioButtonReference.get(), "radioButton"),
                            Objects.requireNonNull(switchReference.get(), "switchControl")
                    )
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> selectionIndicatorsDeselecting(checkBoxReference, radioButtonReference, switchReference),
                    () -> {
                        Objects.requireNonNull(checkBoxReference.get(), "checkBox").setSelected(false);
                        Objects.requireNonNull(radioButtonReference.get(), "radioButton").setSelected(false);
                        Objects.requireNonNull(switchReference.get(), "switchControl").setSelected(false);
                    },
                    () -> assertSelectionIndicatorsDeselecting(
                            Objects.requireNonNull(checkBoxReference.get(), "checkBox"),
                            Objects.requireNonNull(radioButtonReference.get(), "radioButton"),
                            Objects.requireNonNull(switchReference.get(), "switchControl")
                    )
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> selectionIndicatorsUnselectedSettled(
                            checkBoxReference,
                            radioButtonReference,
                            switchReference
                    ),
                    () -> {
                    },
                    () -> assertSelectionIndicatorsUnselectedSettled(
                            Objects.requireNonNull(checkBoxReference.get(), "checkBox"),
                            Objects.requireNonNull(radioButtonReference.get(), "radioButton"),
                            Objects.requireNonNull(switchReference.get(), "switchControl")
                    )
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Switch switchControl = switchReference.get();
                        if (switchControl == null) {
                            return false;
                        }
                        double width = lookupRegion(switchControl, ".thumb").getWidth();
                        return width > 16.0 && width < 28.0;
                    },
                    () -> Objects.requireNonNull(switchReference.get(), "switchControl").arm(),
                    () -> {
                        FlowPane row = Objects.requireNonNull(rowReference.get(), "row");
                        M3Switch switchControl = Objects.requireNonNull(switchReference.get(), "switchControl");
                        double width = lookupRegion(switchControl, ".thumb").getWidth();
                        assertTrue(width > 16.0 && width < 28.0, () -> "pressed intermediate width=" + width);
                        writeVisualSnapshot(snapshotImageOnFxThread(row), java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-switch-press-animation-frame.png"
                        ));
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Switch switchControl = switchReference.get();
                        return switchControl != null
                                && Math.abs(lookupRegion(switchControl, ".thumb").getWidth() - 28.0) < 0.0001;
                    },
                    () -> {
                    },
                    () -> assertEquals(
                            28.0,
                            lookupRegion(
                                    Objects.requireNonNull(switchReference.get(), "switchControl"),
                                    ".thumb"
                            ).getWidth(),
                            0.0001
                    )
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Switch switchControl = switchReference.get();
                        if (switchControl == null) {
                            return false;
                        }
                        double width = lookupRegion(switchControl, ".thumb").getWidth();
                        return width > 16.0 && width < 28.0;
                    },
                    () -> Objects.requireNonNull(switchReference.get(), "switchControl").disarm(),
                    () -> {
                        double width = lookupRegion(
                                Objects.requireNonNull(switchReference.get(), "switchControl"),
                                ".thumb"
                        ).getWidth();
                        assertTrue(width > 16.0 && width < 28.0, () -> "released intermediate width=" + width);
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Switch switchControl = switchReference.get();
                        return switchControl != null
                                && Math.abs(lookupRegion(switchControl, ".thumb").getWidth() - 16.0) < 0.0001;
                    },
                    () -> {
                    },
                    () -> assertEquals(
                            16.0,
                            lookupRegion(
                                    Objects.requireNonNull(switchReference.get(), "switchControl"),
                                    ".thumb"
                            ).getWidth(),
                            0.0001
                    )
            );
        } finally {
            closeObservableSelectionControls(stageReference, rowReference);
        }
    }

    /// Verifies that selection controls mirror indicator and label order in right-to-left mode.
    @Test
    void selectionControlsMirrorIndicatorAndLabelOrderForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
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
    @Tier2Test
    @Test
    void checkBoxIndeterminateTransitionReplaysMarkAnimation() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable FlowPane> rowReference = new AtomicReference<>();
        AtomicReference<@Nullable M3CheckBox> checkBoxReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> showObservableIndeterminateCheckBox(
                    stageReference,
                    rowReference,
                    checkBoxReference
            ));
            FxTestUtils.runOnFxThreadWhen(
                    () -> checkBoxIndeterminateMarkSettled(checkBoxReference),
                    () -> Objects.requireNonNull(checkBoxReference.get(), "checkBox").fire(),
                    () -> assertCheckBoxIndeterminateMarkSettled(
                            Objects.requireNonNull(checkBoxReference.get(), "checkBox")
                    )
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> checkBoxSelectedMarkIsAnimating(checkBoxReference),
                    () -> Objects.requireNonNull(checkBoxReference.get(), "checkBox").fire(),
                    () -> {
                        FlowPane row = Objects.requireNonNull(rowReference.get(), "row");
                        M3CheckBox checkBox = Objects.requireNonNull(checkBoxReference.get(), "checkBox");
                        Region mark = lookupRegion(checkBox, ".mark");

                        assertTrue(checkBox.isSelected());
                        assertFalse(checkBox.isIndeterminate());
                        assertEquals(12.0, mark.getLayoutBounds().getWidth(), 0.0001);
                        assertEquals(10.0, mark.getLayoutBounds().getHeight(), 0.0001);
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
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> checkBoxSelectedMarkSettled(checkBoxReference),
                    () -> {
                    },
                    () -> assertCheckBoxSelectedMarkSettled(
                            Objects.requireNonNull(checkBoxReference.get(), "checkBox")
                    )
            );
        } finally {
            closeObservableSelectionControls(stageReference, rowReference);
        }
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

    /// Verifies that disposed selection skins release mirrored content and layout bindings.
    @Test
    void selectionControlSkinUnbindsLabelWhenDisposed() {
        M3CheckBox checkBox = new M3CheckBox("Check");

        applyCss(checkBox);

        Labeled label = assertInstanceOf(Labeled.class, checkBox.lookup(".m3-selection-label"));
        HBox container = assertInstanceOf(HBox.class, checkBox.lookup(".m3-selection-container"));
        assertTrue(label.textProperty().isBound());
        assertTrue(container.alignmentProperty().isBound());
        assertTrue(container.nodeOrientationProperty().isBound());

        checkBox.getSkin().dispose();

        assertFalse(label.textProperty().isBound());
        assertFalse(container.alignmentProperty().isBound());
        assertFalse(container.nodeOrientationProperty().isBound());
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
        offSwitch.resize(120.0, 48.0);
        onSwitch.resize(120.0, 48.0);
        offSwitch.layout();
        onSwitch.layout();

        Region offThumb = lookupRegion(offSwitch, ".thumb");
        Region onThumb = lookupRegion(onSwitch, ".thumb");
        assertFalse(offThumb.isManaged());
        assertFalse(onThumb.isManaged());
        assertTrue(onThumb.getLayoutX() > offThumb.getLayoutX());
        assertTrue(onThumb.getWidth() > offThumb.getWidth());
        assertEquals(8.0, offThumb.getLayoutX(), 0.0001);
        assertEquals(16.0, offThumb.getLayoutY(), 0.0001);
        assertEquals(16.0, offThumb.getWidth(), 0.0001);
        assertEquals(16.0, offThumb.getHeight(), 0.0001);
        assertEquals(24.0, onThumb.getLayoutX(), 0.0001);
        assertEquals(12.0, onThumb.getLayoutY(), 0.0001);
        assertEquals(24.0, onThumb.getWidth(), 0.0001);
        assertEquals(24.0, onThumb.getHeight(), 0.0001);
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
        switchControl.resize(120.0, 48.0);
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
        offSwitch.resizeRelocate(0.0, 0.0, 120.0, 48.0);
        onSwitch.resizeRelocate(0.0, 56.0, 120.0, 48.0);
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
        assertEquals(4.0, offStateLayer.getLayoutY(), 0.0001);
        assertEquals(4.0, onStateLayer.getLayoutY(), 0.0001);
        assertStateLayerRadii(offSwitch, 20.0, 20.0, 20.0, 20.0);
        assertStateLayerRadii(onSwitch, 20.0, 20.0, 20.0, 20.0);
        assertTrue(stateLayerParent.getChildren().indexOf(offTrack) < stateLayerParent.getChildren().indexOf(offStateLayer));
        assertTrue(stateLayerParent.getChildren().indexOf(offStateLayer) < stateLayerParent.getChildren().indexOf(offThumb));
    }

    /// Verifies that switch skins use the pressed handle size token while armed.
    @Test
    void switchSkinUsesPressedHandleToken() {
        M3Switch offSwitch = new M3Switch("Off");
        M3Switch onSwitch = createSwitch("On", true);
        Pane root = new Pane(offSwitch, onSwitch);
        FxTestUtils.setCssAnimationsEnabled(root, false);
        Scene scene = new Scene(root, 260.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        offSwitch.resizeRelocate(0.0, 0.0, 120.0, 48.0);
        onSwitch.resizeRelocate(0.0, 56.0, 120.0, 48.0);
        offSwitch.layout();
        onSwitch.layout();

        Region offThumb = lookupRegion(offSwitch, ".thumb");
        Region onThumb = lookupRegion(onSwitch, ".thumb");
        assertEquals(16.0, offThumb.getWidth(), 0.0001);
        assertEquals(24.0, onThumb.getWidth(), 0.0001);

        offSwitch.arm();
        onSwitch.arm();
        offSwitch.layout();
        onSwitch.layout();

        assertEquals(28.0, offThumb.getWidth(), 0.0001);
        assertEquals(28.0, offThumb.getHeight(), 0.0001);
        assertEquals(2.0, offThumb.getLayoutX(), 0.0001);
        assertEquals(10.0, offThumb.getLayoutY(), 0.0001);
        assertEquals(28.0, onThumb.getWidth(), 0.0001);
        assertEquals(28.0, onThumb.getHeight(), 0.0001);
        assertEquals(22.0, onThumb.getLayoutX(), 0.0001);
        assertEquals(10.0, onThumb.getLayoutY(), 0.0001);

        offSwitch.disarm();
        onSwitch.disarm();
        offSwitch.layout();
        onSwitch.layout();

        assertEquals(16.0, offThumb.getWidth(), 0.0001);
        assertEquals(24.0, onThumb.getWidth(), 0.0001);
    }

    /// Verifies that ordinary pointer focus does not retain the switch focus-state handle color.
    @Test
    void switchSelectedHandleUsesFocusVisibleInsteadOfOrdinaryFocus() {
        M3Switch switchControl = createSwitch("On", true);
        Pane root = new Pane(switchControl);
        root.setStyle(buttonStateTestColors());
        Scene scene = new Scene(root, 180.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        switchControl.resize(120.0, 48.0);
        switchControl.layout();

        Region selectedThumb = lookupRegion(switchControl, ".m3-switch-thumb-selected");
        Paint selectedHandleColor = selectedThumb.getBackground().getFills().get(0).getFill();

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();

        assertEquals(selectedHandleColor, selectedThumb.getBackground().getFills().get(0).getFill(),
                "ordinary focus retained after a pointer click must keep the selected handle color");

        switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        root.applyCss();

        assertFalse(selectedHandleColor.equals(selectedThumb.getBackground().getFills().get(0).getFill()),
                "keyboard-visible focus must use the selected focus-state handle color");
    }

    /// Verifies that the switch hover state renders circular thumb feedback in snapshots.
    @Tier2Test
    @Test
    void switchHoverStateLayerRendersCircularThumbFeedback() {
        FxTestUtils.runOnFxThread(() -> {
            M3Switch switchControl = new M3Switch("On");
            switchControl.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            Pane root = new Pane(switchControl);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 220.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(220.0, 96.0);
            switchControl.resizeRelocate(24.0, 24.0, 120.0, 48.0);
            switchControl.layout();
            root.layout();
            root.applyCss();

            Region stateLayer = lookupRegion(switchControl, ".m3-state-layer-container");
            Region track = lookupRegion(switchControl, ".box");
            Region thumb = lookupRegion(switchControl, ".thumb");
            for (String selector : List.of(".m3-switch-track-unselected", ".m3-switch-track-selected")) {
                Region trackLayer = lookupRegion(switchControl, selector);
                trackLayer.setStyle(trackLayer.getStyle()
                        + " -fx-background-color: white; -fx-border-color: white;");
            }
            for (String selector : List.of(".m3-switch-thumb-unselected", ".m3-switch-thumb-selected")) {
                Region thumbLayer = lookupRegion(switchControl, selector);
                thumbLayer.setStyle(thumbLayer.getStyle() + " -fx-background-color: white;");
            }
            root.applyCss();

            WritableImage image = snapshotImageOnFxThread(root);
            Rectangle2D feedbackBounds = contrastingPixelBounds(image, stateLayer, Color.WHITE, 0.01);
            Point2D feedbackCenter = new Point2D(
                    feedbackBounds.getMinX() + feedbackBounds.getWidth() / 2.0,
                    feedbackBounds.getMinY() + feedbackBounds.getHeight() / 2.0
            );
            Point2D stateLayerCenter = sceneCenter(stateLayer);

            assertEquals(40.0, stateLayer.getWidth(), 0.0001);
            assertEquals(40.0, stateLayer.getHeight(), 0.0001);
            assertNodeCentersAligned(stateLayer, thumb, 0.0001);
            assertEquals(stateLayerCenter.getX(), feedbackCenter.getX(), 0.75,
                    () -> "switch hover feedback pixels should stay centered on the thumb state layer: feedback="
                            + feedbackBounds + ", stateLayer=" + stateLayer.localToScene(stateLayer.getBoundsInLocal()));
            assertEquals(stateLayerCenter.getY(), feedbackCenter.getY(), 0.75,
                    () -> "switch hover feedback pixels should stay centered on the thumb state layer: feedback="
                            + feedbackBounds + ", stateLayer=" + stateLayer.localToScene(stateLayer.getBoundsInLocal()));
            assertEquals(feedbackBounds.getWidth(), feedbackBounds.getHeight(), 1.5,
                    () -> "switch hover feedback pixels should remain circular: " + feedbackBounds);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 2.0, 20.0), Color.WHITE) > 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 38.0, 20.0), Color.WHITE) > 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 20.0, 2.0), Color.WHITE) > 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 20.0, 38.0), Color.WHITE) > 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 1.0, 1.0), Color.WHITE) < 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 39.0, 1.0), Color.WHITE) < 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 1.0, 39.0), Color.WHITE) < 0.01);
            assertTrue(colorDistance(snapshotNodePixel(image, stateLayer, 39.0, 39.0), Color.WHITE) < 0.01);
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
        switchControl.resize(120.0, 48.0);
        switchControl.layout();

        Region track = lookupRegion(switchControl, ".m3-switch-track-unselected");
        assertEquals(12.0, track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
        assertEquals(12.0, track.getBorder().getStrokes().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
    }

    /// Verifies independent and grouped radio button activation semantics.
    @Test
    void radioButtonPreservesIndependentAndToggleGroupSelectionSemantics() {
        M3RadioButton independent = new M3RadioButton("Independent");
        AtomicInteger independentActions = new AtomicInteger();
        independent.setOnAction(event -> independentActions.incrementAndGet());

        independent.fire();
        assertTrue(independent.isSelected());
        assertEquals(1, independentActions.get());

        independent.fire();
        assertFalse(independent.isSelected());
        assertEquals(2, independentActions.get());

        independent.setDisable(true);
        independent.fire();
        assertFalse(independent.isSelected());
        assertEquals(2, independentActions.get());

        ToggleGroup group = new ToggleGroup();
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

    /// Verifies that directional radio navigation skips effectively hidden and disabled group members.
    @Test
    void radioButtonNavigationSkipsUnreachableGroupMembers() {
        ToggleGroup group = new ToggleGroup();
        M3RadioButton first = createRadioButton("First", true);
        M3RadioButton hidden = new M3RadioButton("Hidden");
        M3RadioButton disabled = new M3RadioButton("Disabled");
        M3RadioButton reachable = new M3RadioButton("Reachable");
        first.setToggleGroup(group);
        hidden.setToggleGroup(group);
        disabled.setToggleGroup(group);
        reachable.setToggleGroup(group);

        VBox hiddenParent = new VBox(hidden);
        hiddenParent.setVisible(false);
        VBox disabledParent = new VBox(disabled);
        disabledParent.setDisable(true);
        HBox root = new HBox(first, hiddenParent, disabledParent, reachable);
        Scene scene = new Scene(root, 480.0, 80.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.layout();

        KeyEvent right = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
        first.fireEvent(right);

        assertFalse(hidden.isSelected());
        assertFalse(disabled.isSelected());
        assertTrue(reachable.isSelected());
        assertEquals(reachable, group.getSelectedToggle());
    }

    /// Verifies that selection-control alignment remains CSS-styleable without rewriting the configured value.
    @Test
    void selectionControlAlignmentRemainsStyleableAndDirectionAware() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        VBox root = new VBox(checkBox, radioButton, switchControl);
        Scene scene = new Scene(root, 360.0, 180.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());

        for (ButtonBase control : new ButtonBase[]{checkBox, radioButton, switchControl}) {
            control.setStyle("-fx-alignment: bottom-right;");
            control.setPrefWidth(240.0);
        }
        root.applyCss();
        root.layout();

        for (ButtonBase control : new ButtonBase[]{checkBox, radioButton, switchControl}) {
            HBox container = assertInstanceOf(HBox.class, control.lookup(".m3-selection-container"));
            assertEquals(Pos.BOTTOM_RIGHT, control.getAlignment());
            assertEquals(Pos.BOTTOM_RIGHT, container.getAlignment());

            control.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            assertEquals(Pos.BOTTOM_RIGHT, container.getAlignment());

            control.setAlignment(Pos.TOP_CENTER);
            assertEquals(Pos.TOP_CENTER, container.getAlignment());
        }
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
        assertAggregateToggleState(checkBox, false, true);

        checkBox.fire();

        assertTrue(checkBox.isSelected());
        assertFalse(checkBox.isIndeterminate());
        assertAggregateToggleState(checkBox, true, false);

        checkBox.fire();

        assertFalse(checkBox.isSelected());
        assertFalse(checkBox.isIndeterminate());
        assertTrue(checkBox.getPseudoClassStates().contains(PseudoClass.getPseudoClass("determinate")));
        assertAggregateToggleState(checkBox, false, false);
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
                + " -m3-color-on-surface: rgb(4,5,6);");
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
        assertEquals(Color.TRANSPARENT, ring.getFill());
        assertEquals(Color.rgb(4, 5, 6), ring.getStroke());
        assertEquals(2.0, ring.getStrokeWidth(), 0.0001);
        assertEquals(Color.TRANSPARENT, dot.getFill());

        radioButton.setSelected(true);
        root.applyCss();

        assertEquals(Color.TRANSPARENT, ring.getFill());
        assertEquals(Color.rgb(1, 2, 3), ring.getStroke());
        assertEquals(Color.rgb(1, 2, 3), dot.getFill());
    }

    /// Verifies that a selected radio button paints its dot at the visual center of the outer indicator.
    @Tier2Test
    @Test
    void selectedRadioButtonDotRendersCenteredInIndicator() {
        FxTestUtils.runOnFxThread(() -> {
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
                Shape ring = radioRing(radioButton);
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
                Point2D renderedRingCenter = contrastingPixelCentroid(image, ring, Color.WHITE, 0.08);
                assertEquals(radioCenter.getX(), renderedDotCenter.getX(), 0.8);
                assertEquals(radioCenter.getY(), renderedDotCenter.getY(), 0.8);
                assertEquals(renderedRingCenter.getX(), renderedDotCenter.getX(), 0.8,
                        () -> "renderedRingCenter=" + renderedRingCenter
                                + ", renderedDotCenter=" + renderedDotCenter);
                assertEquals(renderedRingCenter.getY(), renderedDotCenter.getY(), 0.8,
                        () -> "renderedRingCenter=" + renderedRingCenter
                                + ", renderedDotCenter=" + renderedDotCenter);
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

    /// Verifies that selection controls render selected, indeterminate, and disabled states.
    @Tier2Test
    @Test
    void selectionSnapshotRendersStateMatrix() {
        FxTestUtils.runOnFxThread(() -> {
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
            Scene scene = new Scene(row, 760.0, 280.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(760.0, 280.0);
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
            Shape selectedRadioDot = lookupShape(selectedRadioButton, ".dot");
            Shape disabledSelectedRadioDot = lookupShape(disabledSelectedRadioButton, ".dot");
            assertSnapshotNodeContainsContrast(image, selectedRadioDot, Color.WHITE, 0.1);
            assertRadioButtonIndicatorRenderedCentersAligned(image, selectedRadioButton, "selected radio button");
            assertSnapshotNodeContainsContrast(image, lookupShape(disabledUncheckedRadioButton, ".ring"), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, disabledSelectedRadioDot, Color.WHITE, 0.03);
            Color selectedRadioDotPixel = snapshotNodePixel(
                    image,
                    selectedRadioDot,
                    selectedRadioDot.getBoundsInLocal().getCenterX(),
                    selectedRadioDot.getBoundsInLocal().getCenterY()
            );
            Color disabledSelectedRadioDotPixel = snapshotNodePixel(
                    image,
                    disabledSelectedRadioDot,
                    disabledSelectedRadioDot.getBoundsInLocal().getCenterX(),
                    disabledSelectedRadioDot.getBoundsInLocal().getCenterY()
            );
            assertTrue(
                    colorDistance(disabledSelectedRadioDotPixel, Color.WHITE)
                            < colorDistance(selectedRadioDotPixel, Color.WHITE) * 0.8,
                    () -> "disabled selected radio dot should composite toward the surface: enabled="
                            + selectedRadioDotPixel + ", disabled=" + disabledSelectedRadioDotPixel
            );
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

    /// Shows selection controls configured for observable indicator animation frames.
    private static void showObservableSelectionControls(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable FlowPane> rowReference,
            AtomicReference<@Nullable M3CheckBox> checkBoxReference,
            AtomicReference<@Nullable M3RadioButton> radioButtonReference,
            AtomicReference<@Nullable M3Switch> switchReference
    ) {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        FlowPane row = new FlowPane(18.0, 12.0, checkBox, radioButton, switchControl);
        Scene scene = new Scene(row, 420.0, 96.0);
        Stage stage = new Stage();

        row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(row, true);
        FxTestUtils.setMotionScheme(row, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        row.applyCss();
        row.resize(420.0, 96.0);
        row.layout();

        stageReference.set(stage);
        rowReference.set(row);
        checkBoxReference.set(checkBox);
        radioButtonReference.set(radioButton);
        switchReference.set(switchControl);
    }

    /// Shows a three-state checkbox configured for observable indicator animation frames.
    private static void showObservableIndeterminateCheckBox(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable FlowPane> rowReference,
            AtomicReference<@Nullable M3CheckBox> checkBoxReference
    ) {
        M3CheckBox checkBox = new M3CheckBox("Three-state");
        checkBox.setAllowIndeterminate(true);
        FlowPane row = new FlowPane(checkBox);
        Scene scene = new Scene(row, 220.0, 80.0);
        Stage stage = new Stage();

        row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(row, true);
        FxTestUtils.setMotionScheme(row, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        row.applyCss();
        row.resize(220.0, 80.0);
        row.layout();

        stageReference.set(stage);
        rowReference.set(row);
        checkBoxReference.set(checkBox);
    }

    /// Closes a selection animation test window and clears local motion overrides.
    private static void closeObservableSelectionControls(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable FlowPane> rowReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable FlowPane row = rowReference.get();
            if (row != null) {
                FxTestUtils.setCssAnimationsEnabled(row, true);
                FxTestUtils.clearMotionScheme(row);
            }

            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Returns whether selection indicators are rendering an intermediate selected transition.
    private static boolean selectionIndicatorsSelecting(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference,
            AtomicReference<@Nullable M3RadioButton> radioButtonReference,
            AtomicReference<@Nullable M3Switch> switchReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        @Nullable M3RadioButton radioButton = radioButtonReference.get();
        @Nullable M3Switch switchControl = switchReference.get();
        if (checkBox == null || radioButton == null || switchControl == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");
        return isBetweenExclusive(mark.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(mark.getScaleX(), 0.72, 1.0)
                && isBetweenExclusive(dot.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(dot.getScaleX(), 0.64, 1.0)
                && isBetweenExclusive(thumb.getLayoutX(), 8.0, 24.0)
                && isBetweenExclusive(thumb.getWidth(), 16.0, 24.0);
    }

    /// Returns whether selection indicators have settled in their selected state.
    private static boolean selectionIndicatorsSelectedSettled(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference,
            AtomicReference<@Nullable M3RadioButton> radioButtonReference,
            AtomicReference<@Nullable M3Switch> switchReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        @Nullable M3RadioButton radioButton = radioButtonReference.get();
        @Nullable M3Switch switchControl = switchReference.get();
        if (checkBox == null || radioButton == null || switchControl == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");
        return Math.abs(mark.getOpacity() - 1.0) < 0.0001
                && Math.abs(mark.getScaleX() - 1.0) < 0.0001
                && Math.abs(dot.getOpacity() - 1.0) < 0.0001
                && Math.abs(dot.getScaleX() - 1.0) < 0.0001
                && Math.abs(thumb.getLayoutX() - 24.0) < 0.0001
                && Math.abs(thumb.getWidth() - 24.0) < 0.0001;
    }

    /// Returns whether selection indicators are rendering an intermediate unselected transition.
    private static boolean selectionIndicatorsDeselecting(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference,
            AtomicReference<@Nullable M3RadioButton> radioButtonReference,
            AtomicReference<@Nullable M3Switch> switchReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        @Nullable M3RadioButton radioButton = radioButtonReference.get();
        @Nullable M3Switch switchControl = switchReference.get();
        if (checkBox == null || radioButton == null || switchControl == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");
        return isBetweenExclusive(mark.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(mark.getScaleX(), 0.72, 1.0)
                && isBetweenExclusive(dot.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(dot.getScaleX(), 0.64, 1.0)
                && isBetweenExclusive(thumb.getLayoutX(), 8.0, 24.0)
                && isBetweenExclusive(thumb.getWidth(), 16.0, 24.0);
    }

    /// Returns whether selection indicators have settled in their unselected state.
    private static boolean selectionIndicatorsUnselectedSettled(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference,
            AtomicReference<@Nullable M3RadioButton> radioButtonReference,
            AtomicReference<@Nullable M3Switch> switchReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        @Nullable M3RadioButton radioButton = radioButtonReference.get();
        @Nullable M3Switch switchControl = switchReference.get();
        if (checkBox == null || radioButton == null || switchControl == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");
        return Math.abs(mark.getOpacity()) < 0.0001
                && Math.abs(mark.getScaleX() - 0.72) < 0.0001
                && Math.abs(dot.getOpacity()) < 0.0001
                && Math.abs(dot.getScaleX() - 0.64) < 0.0001
                && Math.abs(thumb.getLayoutX() - 8.0) < 0.0001
                && Math.abs(thumb.getWidth() - 16.0) < 0.0001;
    }

    /// Verifies that selection indicators are in an intermediate selected transition.
    private static void assertSelectionIndicatorsSelecting(
            M3CheckBox checkBox,
            M3RadioButton radioButton,
            M3Switch switchControl
    ) {
        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");

        assertBetween(mark.getOpacity(), 0.25, 1.0, "checkbox mark opacity");
        assertBetween(mark.getScaleX(), 0.72, 1.0, "checkbox mark scale");
        assertBetween(dot.getOpacity(), 0.25, 1.0, "radio dot opacity");
        assertBetween(dot.getScaleX(), 0.64, 1.0, "radio dot scale");
        assertBetween(thumb.getLayoutX(), 8.0, 24.0, "switch thumb x");
        assertBetween(thumb.getWidth(), 16.0, 24.0, "switch thumb width");
    }

    /// Verifies that selection indicators have settled in their selected state.
    private static void assertSelectionIndicatorsSelectedSettled(
            M3CheckBox checkBox,
            M3RadioButton radioButton,
            M3Switch switchControl
    ) {
        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");

        assertEquals(1.0, mark.getOpacity(), 0.0001);
        assertEquals(1.0, mark.getScaleX(), 0.0001);
        assertEquals(1.0, dot.getOpacity(), 0.0001);
        assertEquals(1.0, dot.getScaleX(), 0.0001);
        assertEquals(24.0, thumb.getLayoutX(), 0.0001);
        assertEquals(24.0, thumb.getWidth(), 0.0001);
    }

    /// Verifies that selection indicators are in an intermediate unselected transition.
    private static void assertSelectionIndicatorsDeselecting(
            M3CheckBox checkBox,
            M3RadioButton radioButton,
            M3Switch switchControl
    ) {
        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");

        assertBetween(mark.getOpacity(), 0.25, 1.0, "checkbox mark reverse opacity");
        assertBetween(mark.getScaleX(), 0.72, 1.0, "checkbox mark reverse scale");
        assertBetween(dot.getOpacity(), 0.25, 1.0, "radio dot reverse opacity");
        assertBetween(dot.getScaleX(), 0.64, 1.0, "radio dot reverse scale");
        assertBetween(thumb.getLayoutX(), 8.0, 24.0, "switch thumb reverse x");
        assertBetween(thumb.getWidth(), 16.0, 24.0, "switch thumb reverse width");
    }

    /// Verifies that selection indicators have settled in their unselected state.
    private static void assertSelectionIndicatorsUnselectedSettled(
            M3CheckBox checkBox,
            M3RadioButton radioButton,
            M3Switch switchControl
    ) {
        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        Node dot = radioDotAnimationLayer(radioButton);
        Region thumb = lookupRegion(switchControl, ".thumb");

        assertEquals(0.0, mark.getOpacity(), 0.0001);
        assertEquals(0.72, mark.getScaleX(), 0.0001);
        assertEquals(0.0, dot.getOpacity(), 0.0001);
        assertEquals(0.64, dot.getScaleX(), 0.0001);
        assertEquals(8.0, thumb.getLayoutX(), 0.0001);
        assertEquals(16.0, thumb.getWidth(), 0.0001);
    }

    /// Returns whether a checkbox indeterminate dash has settled.
    private static boolean checkBoxIndeterminateMarkSettled(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        if (checkBox == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        return !checkBox.isSelected()
                && checkBox.isIndeterminate()
                && Math.abs(mark.getLayoutBounds().getWidth() - 12.0) < 0.0001
                && Math.abs(mark.getLayoutBounds().getHeight() - 2.0) < 0.0001
                && Math.abs(mark.getOpacity() - 1.0) < 0.0001
                && Math.abs(mark.getScaleX() - 1.0) < 0.0001;
    }

    /// Returns whether a checkbox selected check mark is animating in.
    private static boolean checkBoxSelectedMarkIsAnimating(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        if (checkBox == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        return checkBox.isSelected()
                && !checkBox.isIndeterminate()
                && Math.abs(mark.getLayoutBounds().getWidth() - 12.0) < 0.0001
                && Math.abs(mark.getLayoutBounds().getHeight() - 10.0) < 0.0001
                && isBetweenExclusive(mark.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(mark.getScaleX(), 0.72, 1.0);
    }

    /// Returns whether a checkbox selected check mark has settled.
    private static boolean checkBoxSelectedMarkSettled(
            AtomicReference<@Nullable M3CheckBox> checkBoxReference
    ) {
        @Nullable M3CheckBox checkBox = checkBoxReference.get();
        if (checkBox == null) {
            return false;
        }

        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");
        return checkBox.isSelected()
                && !checkBox.isIndeterminate()
                && Math.abs(mark.getLayoutBounds().getWidth() - 12.0) < 0.0001
                && Math.abs(mark.getLayoutBounds().getHeight() - 10.0) < 0.0001
                && Math.abs(mark.getOpacity() - 1.0) < 0.0001
                && Math.abs(mark.getScaleX() - 1.0) < 0.0001;
    }

    /// Verifies that a checkbox indeterminate dash has settled.
    private static void assertCheckBoxIndeterminateMarkSettled(M3CheckBox checkBox) {
        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");

        assertFalse(checkBox.isSelected());
        assertTrue(checkBox.isIndeterminate());
        assertEquals(12.0, mark.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(2.0, mark.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(1.0, mark.getOpacity(), 0.0001);
        assertEquals(1.0, mark.getScaleX(), 0.0001);
    }

    /// Verifies that a checkbox selected check mark has settled.
    private static void assertCheckBoxSelectedMarkSettled(M3CheckBox checkBox) {
        layoutSelectionIndicatorOwner(checkBox);
        Region mark = lookupRegion(checkBox, ".mark");

        assertTrue(checkBox.isSelected());
        assertFalse(checkBox.isIndeterminate());
        assertEquals(12.0, mark.getLayoutBounds().getWidth(), 0.0001);
        assertEquals(10.0, mark.getLayoutBounds().getHeight(), 0.0001);
        assertEquals(1.0, mark.getOpacity(), 0.0001);
        assertEquals(1.0, mark.getScaleX(), 0.0001);
    }

    /// Applies CSS and layout to the row that owns selection indicators.
    private static void layoutSelectionIndicatorOwner(Node node) {
        @Nullable Parent parent = node.getParent();
        if (parent != null) {
            parent.applyCss();
            parent.layout();
        } else {
            node.applyCss();
            if (node instanceof Parent parentNode) {
                parentNode.layout();
            }
        }
    }

    /// Verifies that selected radio dot pixels are visually centered inside the outer ring.
    private static void assertRadioButtonIndicatorRenderedCentersAligned(
            WritableImage image,
            M3RadioButton radioButton,
            String description
    ) {
        Shape ring = radioRing(radioButton);
        Shape dot = radioDot(radioButton);
        Point2D ringCenter = sceneCenter(ring);
        Point2D ringPixelCenter = contrastingPixelCentroid(image, ring, Color.WHITE, 0.08);
        Point2D dotPixelCenter = contrastingPixelCentroid(image, dot, Color.WHITE, 0.08);
        Rectangle2D dotPixelBounds = contrastingPixelBounds(image, dot, Color.WHITE, 0.08);

        assertEquals(ringCenter.getX(), ringPixelCenter.getX(), SELECTION_INDICATOR_RENDERED_CENTER_TOLERANCE,
                () -> description + " ring pixels are off-center: ringCenter="
                        + ringCenter + ", ringPixelCenter=" + ringPixelCenter);
        assertEquals(ringCenter.getY(), ringPixelCenter.getY(), SELECTION_INDICATOR_RENDERED_CENTER_TOLERANCE,
                () -> description + " ring pixels are off-center: ringCenter="
                        + ringCenter + ", ringPixelCenter=" + ringPixelCenter);
        assertEquals(ringPixelCenter.getX(), dotPixelCenter.getX(), SELECTION_INDICATOR_RENDERED_CENTER_TOLERANCE,
                () -> description + " dot pixels are not centered in the ring: ringPixelCenter="
                        + ringPixelCenter + ", dotPixelCenter=" + dotPixelCenter);
        assertEquals(ringPixelCenter.getY(), dotPixelCenter.getY(), SELECTION_INDICATOR_RENDERED_CENTER_TOLERANCE,
                () -> description + " dot pixels are not centered in the ring: ringPixelCenter="
                        + ringPixelCenter + ", dotPixelCenter=" + dotPixelCenter);
        assertEquals(dotPixelBounds.getWidth(), dotPixelBounds.getHeight(), 1.0,
                () -> description + " dot pixels are not circular: " + dotPixelBounds);
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
}
