// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3ColorAreaSkin;
import org.glavo.m3fx.skins.M3ColorFieldSkin;
import org.glavo.m3fx.skins.M3ColorPickerSkin;
import org.glavo.m3fx.skins.M3ColorSliderSkin;
import org.glavo.m3fx.skins.M3ColorSwatchPickerSkin;
import org.glavo.m3fx.skins.M3ColorSwatchSkin;
import org.glavo.m3fx.skins.M3ColorWheelSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotHasColorVariety;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotNodeContainsContrast;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.snapshotImageOnFxThread;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.visualTestColors;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.writeVisualSnapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the rendered presentation and interactive geometry of the complete M3FX color-control family.
///
/// These higher-cost tests exercise the retained skins as a coherent editor rather than repeating the pure color
/// model contracts covered by [M3ColorControlsTest].
@Tier2Test
@NotNullByDefault
final class M3ColorPresentationTest {
    /// The tolerance used for normalized pointer-derived channels.
    private static final double CHANNEL_TOLERANCE = 0.04;

    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that every primitive and the composed picker render as one complete Material-themed matrix.
    @Test
    void colorControlMatrixRendersEveryPrimitive() {
        FxTestUtils.runOnFxThread(() -> {
            M3Color initial = new M3HsbColor(268.0, 0.62, 0.76, 0.72);

            M3ColorArea area = new M3ColorArea(initial);
            area.setPrefSize(320.0, 220.0);
            M3ColorArea hslArea = new M3ColorArea(initial.toColorSpace(M3ColorSpace.HSL));
            hslArea.setPlane(M3ColorPlane.HSL_SATURATION_LIGHTNESS);
            hslArea.setPrefSize(320.0, 180.0);
            M3ColorArea rgbArea = new M3ColorArea(initial.toColorSpace(M3ColorSpace.RGB));
            rgbArea.setPlane(M3ColorPlane.RGB_RED_GREEN);
            rgbArea.setPrefSize(320.0, 180.0);
            M3ColorWheel wheel = new M3ColorWheel(initial);
            wheel.setPrefSize(220.0, 220.0);

            M3ColorSlider hue = new M3ColorSlider(M3ColorChannel.HUE);
            hue.setValue(initial);
            hue.setPrefWidth(360.0);
            M3ColorSlider alpha = new M3ColorSlider(M3ColorChannel.ALPHA);
            alpha.setValue(initial);
            alpha.setPrefWidth(360.0);

            M3ColorField field = new M3ColorField(initial);
            field.setIncludeAlpha(true);
            M3ColorSwatch translucent = new M3ColorSwatch(initial);
            translucent.setSize(M3ColorSwatchSize.LARGE);
            M3ColorSwatch noColor = new M3ColorSwatch();
            noColor.setSize(M3ColorSwatchSize.LARGE);

            M3ColorSwatchPicker swatches = new M3ColorSwatchPicker();
            swatches.getItems().setAll(testPalette());
            swatches.select(3);
            swatches.setColumnCount(8);

            M3ColorPicker picker = new M3ColorPicker(initial);
            picker.setShowColorWheel(true);
            picker.getPresets().setAll(testPalette());
            picker.setPrefWidth(620.0);

            HBox areaRow = new HBox(24.0, area, wheel);
            HBox genericAreaRow = new HBox(24.0, hslArea, rgbArea);
            HBox valueRow = new HBox(16.0, field, translucent, noColor);
            VBox root = new VBox(20.0, areaRow, genericAreaRow, hue, alpha, valueRow, swatches, picker);
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 920.0, 1260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());

            root.applyCss();
            root.resize(920.0, 1260.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 36);
            assertSnapshotNodeContainsContrast(image, area, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, hslArea, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, rgbArea, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, wheel, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, hue, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, alpha, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, field, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, swatches, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, picker, Color.WHITE, 0.08);
            writeVisualSnapshot(image, Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-color-controls.png"
            ));
        });
    }

    /// Verifies CSS geometry, narrow composed layout, and roving focus across both node orientations.
    @Test
    void cssMetricsResponsiveCompositionAndRovingFocusStayCoherent() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorArea area = new M3ColorArea();
            area.setStyle("-m3-color-area-thumb-size: 34px;");
            area.resizeRelocate(20.0, 20.0, 240.0, 200.0);

            M3ColorSlider slider = new M3ColorSlider(M3ColorChannel.HUE);
            slider.setStyle(
                    "-m3-color-slider-thumb-size: 32px;"
                            + "-m3-color-slider-track-thickness: 18px;"
            );
            slider.resizeRelocate(20.0, 240.0, 280.0, 48.0);

            M3ColorPicker picker = new M3ColorPicker();
            picker.setShowColorWheel(true);
            picker.getPresets().setAll(testPalette());
            picker.resizeRelocate(20.0, 308.0, 280.0, 760.0);

            M3ColorSwatchPicker swatches = new M3ColorSwatchPicker();
            swatches.getItems().setAll(testPalette());
            swatches.select(3);
            swatches.resizeRelocate(340.0, 20.0, 300.0, 120.0);

            Pane root = new Pane(area, slider, picker, swatches);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 680.0, 1100.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(680.0, 1100.0);
            root.layout();
            area.layout();
            slider.layout();
            picker.layout();
            swatches.layout();

            Node areaThumb = area.lookup(".color-area-thumb");
            Node sliderThumb = slider.lookup(".color-slider-thumb");
            Node sliderTrack = slider.lookup(".color-slider-track");
            assertEquals(34.0, areaThumb.getBoundsInParent().getWidth(), 0.01);
            assertEquals(34.0, areaThumb.getBoundsInParent().getHeight(), 0.01);
            assertEquals(32.0, sliderThumb.getBoundsInParent().getWidth(), 0.01);
            assertEquals(32.0, sliderThumb.getBoundsInParent().getHeight(), 0.01);
            assertEquals(18.0, sliderTrack.getBoundsInParent().getHeight(), 0.01);

            assertNodeWithin(picker, picker.lookup(".m3-color-picker-area"));
            assertNodeWithin(picker, picker.lookup(".m3-color-picker-wheel"));
            assertNodeWithin(picker, picker.lookup(".m3-color-picker-hue-slider"));
            assertNodeWithin(picker, picker.lookup(".m3-color-picker-alpha-slider"));
            assertNodeWithin(picker, picker.lookup(".m3-color-picker-field"));
            assertNodeWithin(picker, picker.lookup(".m3-color-picker-presets"));

            List<Node> cells = renderedSwatchCells(swatches);
            assertEquals(1, cells.stream().filter(Node::isFocusTraversable).count());
            assertTrue(cells.get(3).isFocusTraversable());

            swatches.select(5);
            assertEquals(1, cells.stream().filter(Node::isFocusTraversable).count());
            assertTrue(cells.get(5).isFocusTraversable());

            cells.get(5).fireEvent(keyPressed(KeyCode.RIGHT));
            assertTrue(cells.get(6).isFocusTraversable());

            swatches.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            cells.get(6).fireEvent(keyPressed(KeyCode.RIGHT));
            assertTrue(cells.get(5).isFocusTraversable());
        });
    }

    /// Verifies pointer commit state, RTL mirroring for linear editors, and the wheel's fixed physical hue plane.
    @Test
    void directManipulationUsesSpectrumDirectionAndCommitSemantics() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorArea area = new M3ColorArea(new M3HsbColor(0.0, 0.0, 0.0));
            area.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            area.setPrefSize(320.0, 220.0);

            M3ColorSlider slider = new M3ColorSlider(M3ColorChannel.HUE);
            slider.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            slider.setPrefSize(280.0, 48.0);

            M3ColorWheel wheel = new M3ColorWheel();
            wheel.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            wheel.setPrefSize(200.0, 200.0);

            HBox root = new HBox(20.0, area, slider, wheel);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 900.0, 280.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(900.0, 280.0);
            root.layout();

            PseudoClass focusVisible = PseudoClass.getPseudoClass("focus-visible");
            slider.pseudoClassStateChanged(focusVisible, true);
            root.applyCss();
            root.layout();
            assertEquals(1, slider.lookupAll(".m3-focus-indicator").size());
            slider.pseudoClassStateChanged(focusVisible, false);

            Bounds areaTrack = area.lookup(".color-area-track").getBoundsInParent();
            double areaX = areaTrack.getMinX() + 1.0;
            double areaY = areaTrack.getMinY() + areaTrack.getHeight() * 0.25;
            area.fireEvent(primaryMouseEvent(
                    area,
                    MouseEvent.MOUSE_PRESSED,
                    areaX,
                    areaY,
                    true
            ));
            assertTrue(area.isValueChanging());
            M3HsbColor areaValue = (M3HsbColor) area.getValue();
            assertEquals(1.0, areaValue.saturation(), CHANNEL_TOLERANCE);
            assertEquals(0.75, areaValue.brightness(), CHANNEL_TOLERANCE);
            area.fireEvent(primaryMouseEvent(
                    area,
                    MouseEvent.MOUSE_RELEASED,
                    areaX,
                    areaY,
                    false
            ));
            assertFalse(area.isValueChanging());
            area.fireEvent(keyPressed(KeyCode.RIGHT));
            assertEquals(
                    0.99,
                    area.getValue().getChannel(M3ColorChannel.SATURATION),
                    CHANNEL_TOLERANCE
            );

            Bounds sliderTrack = slider.lookup(".color-slider-track").getBoundsInParent();
            double sliderX = sliderTrack.getMinX() + 1.0;
            double sliderY = sliderTrack.getMinY() + sliderTrack.getHeight() / 2.0;
            slider.fireEvent(primaryMouseEvent(
                    slider,
                    MouseEvent.MOUSE_PRESSED,
                    sliderX,
                    sliderY,
                    true
            ));
            assertTrue(slider.isValueChanging());
            assertEquals(360.0, slider.getValue().getChannel(M3ColorChannel.HUE), 2.0);
            slider.fireEvent(primaryMouseEvent(
                    slider,
                    MouseEvent.MOUSE_RELEASED,
                    sliderX,
                    sliderY,
                    false
            ));
            assertFalse(slider.isValueChanging());
            double sliderHueBeforeKey = slider.getValue().getChannel(M3ColorChannel.HUE);
            slider.fireEvent(keyPressed(KeyCode.RIGHT));
            assertEquals(
                    sliderHueBeforeKey - M3ColorChannel.HUE.getUnitIncrement(),
                    slider.getValue().getChannel(M3ColorChannel.HUE),
                    0.01
            );

            Node wheelVisual = wheel.lookup(".color-wheel-visual");
            double centerX = wheelVisual.getLayoutBounds().getWidth() / 2.0;
            double centerY = wheelVisual.getLayoutBounds().getHeight() / 2.0;
            fireClick(wheelVisual, centerX, 1.0);
            assertEquals(0.0, wheel.getValue().getChannel(M3ColorChannel.HUE), 2.0);
            fireClick(wheelVisual, wheelVisual.getLayoutBounds().getWidth() - 1.0, centerY);
            assertEquals(90.0, wheel.getValue().getChannel(M3ColorChannel.HUE), 2.0);
            wheel.fireEvent(keyPressed(KeyCode.RIGHT));
            assertEquals(91.0, wheel.getValue().getChannel(M3ColorChannel.HUE), 2.0);
        });
    }

    /// Verifies field validation and swatch-cell activation through their rendered skins.
    @Test
    void fieldAndSwatchPickerCommitThroughRenderedChildren() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorField field = new M3ColorField(new M3HslColor(210.0, 0.50, 0.40, 0.60));
            field.setIncludeAlpha(true);
            M3ColorSwatchPicker picker = new M3ColorSwatchPicker();
            picker.getItems().setAll(testPalette());
            picker.setColumnCount(4);

            VBox root = new VBox(16.0, field, picker);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(420.0, 260.0);
            root.layout();

            TextField editor = (TextField) field.lookup(".m3-color-field-editor");
            editor.setText("#33669980");
            editor.fireEvent(new ActionEvent());
            assertEquals(M3ColorSpace.HSL, field.getValue().getColorSpace());
            assertEquals(128.0 / 255.0, field.getValue().getAlpha(), 1.0 / 255.0);
            assertFalse(field.getPseudoClassStates().contains(PseudoClass.getPseudoClass("invalid")));

            M3Color committed = field.getValue();
            editor.setText("#NOT-A-COLOR");
            editor.fireEvent(new ActionEvent());
            assertSame(committed, field.getValue());
            assertEquals("#NOT-A-COLOR", field.getText());
            assertTrue(field.isInvalid());
            assertTrue(field.getPseudoClassStates().contains(PseudoClass.getPseudoClass("invalid")));

            field.setSkin(new M3ColorFieldSkin(field));
            root.applyCss();
            root.layout();
            editor = (TextField) field.lookup(".m3-color-field-editor");
            assertEquals("#NOT-A-COLOR", editor.getText());
            assertTrue(editor.getPseudoClassStates().contains(PseudoClass.getPseudoClass("invalid")));

            editor.setText("#33669980");
            assertEquals("#33669980", field.getText());
            assertFalse(field.isInvalid());
            assertFalse(field.getPseudoClassStates().contains(PseudoClass.getPseudoClass("invalid")));
            editor.setText("#NOT-A-COLOR");
            editor.fireEvent(new ActionEvent());
            editor.fireEvent(keyPressed(KeyCode.ESCAPE));
            assertFalse(field.getPseudoClassStates().contains(PseudoClass.getPseudoClass("invalid")));

            List<Node> cells = renderedSwatchCells(picker);
            PseudoClass focusVisible = PseudoClass.getPseudoClass("focus-visible");
            cells.get(0).pseudoClassStateChanged(focusVisible, true);
            root.applyCss();
            root.layout();
            assertEquals(1, cells.get(0).lookupAll(".m3-focus-indicator").size());
            cells.get(0).pseudoClassStateChanged(focusVisible, false);

            cells.get(1).executeAccessibleAction(AccessibleAction.FIRE);
            assertEquals(1, picker.getSelectedIndex());
            assertSame(picker.getItems().get(1), picker.getSelectedColor());
        });
    }

    /// Verifies that replacing any color-control skin does not let the disposed skin remove the new presentation.
    @Test
    void colorControlSkinsCanBeReplacedWithoutLosingRenderedChildren() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorArea area = new M3ColorArea();
            M3ColorSlider slider = new M3ColorSlider();
            M3ColorWheel wheel = new M3ColorWheel();
            M3ColorField field = new M3ColorField();
            M3ColorSwatch swatch = new M3ColorSwatch(new M3RgbColor(0.2, 0.4, 0.8));
            M3ColorSwatchPicker swatchPicker = new M3ColorSwatchPicker();
            swatchPicker.getItems().setAll(testPalette());
            M3ColorPicker picker = new M3ColorPicker();
            picker.getPresets().setAll(testPalette());

            VBox root = new VBox(12.0, area, slider, wheel, field, swatch, swatchPicker, picker);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 720.0, 1_260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            area.setSkin(new M3ColorAreaSkin(area));
            slider.setSkin(new M3ColorSliderSkin(slider));
            wheel.setSkin(new M3ColorWheelSkin(wheel));
            field.setSkin(new M3ColorFieldSkin(field));
            swatch.setSkin(new M3ColorSwatchSkin(swatch));
            swatchPicker.setSkin(new M3ColorSwatchPickerSkin(swatchPicker));
            picker.setSkin(new M3ColorPickerSkin(picker));
            root.applyCss();
            root.layout();

            assertFalse(area.lookupAll(".color-area-track").isEmpty());
            assertFalse(slider.lookupAll(".color-slider-track").isEmpty());
            assertFalse(wheel.lookupAll(".color-wheel-track").isEmpty());
            assertFalse(field.lookupAll(".m3-color-field-editor").isEmpty());
            assertFalse(swatch.lookupAll(".color-swatch-fill").isEmpty());
            assertEquals(testPalette().length, renderedSwatchCells(swatchPicker).size());
            assertFalse(picker.lookupAll(".m3-color-picker-content").isEmpty());
        });
    }

    /// Verifies that the composed picker synchronizes editors without replacing an equivalent editing-space value.
    @Test
    void composedPickerPreservesEditingSpaceAndAppliesPresetActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3HslColor initial = new M3HslColor(0.0, 1.0, 0.5);
            M3RgbColor equivalentPreset = new M3RgbColor(1.0, 0.0, 0.0);
            M3RgbColor secondPreset = new M3RgbColor(0.0, 0.45, 0.90);
            M3ColorPicker picker = new M3ColorPicker(initial);
            picker.setShowColorWheel(true);
            picker.getPresets().setAll(equivalentPreset, secondPreset);

            Pane root = new Pane(picker);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 680.0, 620.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resizeRelocate(20.0, 20.0, 620.0, 560.0);
            root.resize(680.0, 620.0);
            root.layout();
            picker.layout();

            assertSame(initial, picker.getValue());
            assertEquals(M3ColorSpace.HSL, picker.getValue().getColorSpace());

            M3ColorSwatchPicker presetPicker =
                    (M3ColorSwatchPicker) picker.lookup(".m3-color-picker-presets");
            assertEquals(0, presetPicker.getSelectedIndex());
            List<Node> cells = renderedSwatchCells(presetPicker);
            cells.get(1).executeAccessibleAction(AccessibleAction.FIRE);
            assertSame(secondPreset, picker.getValue());

            M3ColorField field = (M3ColorField) picker.lookup(".m3-color-picker-field");
            M3ColorWheel wheel = (M3ColorWheel) picker.lookup(".m3-color-picker-wheel");
            picker.setShowColorField(false);
            picker.setShowColorWheel(false);
            assertFalse(field.isManaged());
            assertFalse(field.isVisible());
            assertFalse(wheel.isManaged());
            assertFalse(wheel.isVisible());
        });
    }

    /// Returns a stable palette shared by presentation scenarios.
    ///
    /// @return a new ordered palette
    private static M3Color[] testPalette() {
        return new M3Color[]{
                new M3HsbColor(0.0, 0.82, 0.88),
                new M3HsbColor(32.0, 0.80, 0.96),
                new M3HsbColor(58.0, 0.72, 0.96),
                new M3HsbColor(132.0, 0.60, 0.66),
                new M3HsbColor(178.0, 0.72, 0.70),
                new M3HsbColor(218.0, 0.72, 0.88),
                new M3HsbColor(270.0, 0.58, 0.78),
                new M3HsbColor(328.0, 0.64, 0.84)
        };
    }

    /// Returns swatch cells in visual row-major order.
    ///
    /// @param picker the rendered picker
    /// @return the ordered cell list
    private static List<Node> renderedSwatchCells(M3ColorSwatchPicker picker) {
        ArrayList<Node> cells = new ArrayList<>(picker.lookupAll(".color-swatch-cell"));
        cells.sort(Comparator
                .comparingDouble((Node node) -> node.getBoundsInParent().getMinY())
                .thenComparingDouble(node -> node.getBoundsInParent().getMinX()));
        return cells;
    }

    /// Fires a primary-button press and release at one local point.
    ///
    /// @param node the gesture target
    /// @param x    the local x-coordinate
    /// @param y    the local y-coordinate
    private static void fireClick(Node node, double x, double y) {
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_PRESSED, x, y, true));
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_RELEASED, x, y, false));
    }

    /// Verifies that one rendered descendant remains inside an ancestor's scene bounds.
    ///
    /// @param ancestor   the expected containing node
    /// @param descendant the visible descendant to verify
    private static void assertNodeWithin(Node ancestor, Node descendant) {
        Bounds ancestorBounds = ancestor.localToScene(ancestor.getBoundsInLocal());
        Bounds descendantBounds = descendant.localToScene(descendant.getBoundsInLocal());
        double tolerance = 0.01;
        assertTrue(descendantBounds.getMinX() >= ancestorBounds.getMinX() - tolerance);
        assertTrue(descendantBounds.getMinY() >= ancestorBounds.getMinY() - tolerance);
        assertTrue(descendantBounds.getMaxX() <= ancestorBounds.getMaxX() + tolerance);
        assertTrue(descendantBounds.getMaxY() <= ancestorBounds.getMaxY() + tolerance);
    }

    /// Creates a primary-button mouse event at one local point.
    ///
    /// @param node              the event target
    /// @param eventType         the mouse event type
    /// @param x                 the local x-coordinate
    /// @param y                 the local y-coordinate
    /// @param primaryButtonDown whether the primary button is held
    /// @return the mouse event
    private static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                x,
                y,
                screenX,
                screenY,
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
                new PickResult(node, new Point3D(x, y, 0.0), 1.0)
        );
    }

    /// Creates a key-pressed event for field keyboard behavior.
    ///
    /// @param code the key code
    /// @return the key event
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                false,
                false,
                false,
                false
        );
    }
}
