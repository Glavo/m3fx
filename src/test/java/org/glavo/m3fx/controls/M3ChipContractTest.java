// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3ChipSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies chip variants, selection, dismissal, and content layout.
@NotNullByDefault
final class M3ChipContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that flat chip focus outlines use each variant's Material content color role.
    @Test
    void flatChipFocusOutlinesUseVariantSpecificColorRoles() {
        M3Theme theme = M3Theme.defaultTheme();
        Color onSurface = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE);
        Color onSurfaceVariant = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE_VARIANT);

        M3Chip assist = new M3AssistChip("Assist");
        M3Chip suggestion = new M3SuggestionChip("Suggestion");
        M3Chip filter = new M3FilterChip("Filter");
        M3Icon inputLeading = new M3Icon("person");
        M3InputChip input = new M3InputChip("Input", inputLeading);
        M3FilterChip selectedFilter = new M3FilterChip("Selected");
        selectedFilter.setSelected(true);

        HBox root = new HBox(assist, suggestion, filter, input, selectedFilter);
        Scene scene = new Scene(root, 720.0, 120.0);
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        assertEquals(onSurfaceVariant, iconFill(inputLeading));

        for (M3Chip chip : List.of(assist, suggestion, filter, input, selectedFilter)) {
            chip.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        }
        root.applyCss();

        assertBorderColor(assist, onSurface);
        assertBorderColor(suggestion, onSurfaceVariant);
        assertBorderColor(filter, onSurfaceVariant);
        assertBorderColor(input, onSurfaceVariant);
        assertBorderColor(selectedFilter, Color.TRANSPARENT);

        input.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();

        assertEquals(theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY), iconFill(inputLeading));

        input.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);

        input.setSelected(true);
        root.applyCss();

        assertEquals(theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY), iconFill(inputLeading));
    }

    /// Verifies that chip component token properties are styleable from CSS.
    @Test
    void chipTokensAreStyleable() {
        M3Icon icon = new M3Icon("A");
        M3Chip chip = new M3AssistChip("Chip", icon);
        chip.setStyle("-m3-container-height: 36px; -m3-container-shape: 16px; "
                + "-m3-horizontal-padding: 14px; -m3-icon-horizontal-padding: 6px; "
                + "-m3-chip-icon-size: 22px;");

        applyCss(chip);

        assertEquals(36.0, chip.getContainerHeight(), 0.0001);
        assertEquals(16.0, chip.getContainerShape(), 0.0001);
        assertEquals(14.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(6.0, chip.getIconHorizontalPadding(), 0.0001);
        assertEquals(36.0, chip.prefHeight(-1.0), 0.0001);
        assertEquals(6.0, chip.getPadding().getLeft(), 0.0001);
        assertEquals(14.0, chip.getPadding().getRight(), 0.0001);
        assertEquals(22.0, icon.getIconSize(), 0.0001);

        chip.setGraphic(null);
        applyCss(chip);

        assertEquals(14.0, chip.getPadding().getLeft(), 0.0001);
        assertEquals(14.0, chip.getPadding().getRight(), 0.0001);
    }

    /// Verifies that m3fx chips create the Material Design 3 skin.
    @Test
    void chipCreatesMaterialSkin() {
        M3Chip chip = new M3AssistChip("Filter");

        applyCss(chip);

        assertInstanceOf(M3ChipSkin.class, chip.getSkin());
    }

    /// Verifies that chips can be created with graphic content.
    @Test
    void chipSupportsGraphicContent() {
        M3Icon icon = new M3Icon("A");
        M3InputChip chip = new M3InputChip("Assist", icon);
        chip.setSelected(true);

        assertEquals("Assist", chip.getText());
        assertEquals(icon, chip.getGraphic());
        assertInstanceOf(M3InputChip.class, chip);
        assertTrue(chip.isSelected());
    }

    /// Verifies command and selectable chip semantics together with logical leading and trailing slots.
    @Test
    void chipVariantsExposeSemanticSelectionAndLogicalGraphicSlots() {
        AtomicInteger actions = new AtomicInteger();
        M3AssistChip assist = new M3AssistChip("Assist");
        assist.setOnAction(event -> actions.incrementAndGet());

        assist.fire();

        assertEquals(1, actions.get());
        assertEquals(AccessibleRole.BUTTON, assist.getAccessibleRole());

        M3FilterChip filter = new M3FilterChip("Filter");
        filter.fire();

        assertTrue(filter.isSelected());
        assertEquals(AccessibleRole.TOGGLE_BUTTON, filter.getAccessibleRole());

        M3Icon leading = new M3Icon("P");
        M3Icon trailing = new M3Icon("X");
        M3InputChip input = new M3InputChip("Alex Morgan", leading);
        input.setTrailingGraphic(trailing);
        HBox root = new HBox(input);
        Scene scene = new Scene(root, 240.0, 80.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.layout();

        assertSame(trailing, input.getTrailingGraphic());
        assertTrue(leading.getStyleClass().contains("m3-chip-leading-graphic"));
        assertTrue(trailing.getStyleClass().contains("m3-chip-trailing-graphic"));
        assertEquals(input.getIconHorizontalPadding(), input.getPadding().getLeft(), 0.0001);
        assertEquals(input.getIconHorizontalPadding(), input.getPadding().getRight(), 0.0001);
        Bounds leadingBounds = leading.localToScene(leading.getBoundsInLocal());
        Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
        assertTrue(leadingBounds.getMaxX() <= trailingBounds.getMinX());

        input.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        root.layout();

        leadingBounds = leading.localToScene(leading.getBoundsInLocal());
        trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
        assertTrue(trailingBounds.getMaxX() <= leadingBounds.getMinX());

        input.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        input.setTrailingGraphic(null);
        input.setIconHorizontalPadding(8.0);
        input.setHorizontalPadding(18.0);
        root.applyCss();
        root.layout();

        Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
        leadingBounds = leading.localToScene(leading.getBoundsInLocal());
        assertEquals(8.0, leadingBounds.getMinX() - inputBounds.getMinX(), 1.0);
        assertEquals(8.0, input.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, input.getPadding().getRight(), 0.0001);

        input.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        root.layout();

        inputBounds = input.localToScene(input.getBoundsInLocal());
        leadingBounds = leading.localToScene(leading.getBoundsInLocal());
        assertEquals(8.0, inputBounds.getMaxX() - leadingBounds.getMaxX(), 1.0);
        assertEquals(8.0, input.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, input.getPadding().getRight(), 0.0001);
    }

    /// Verifies that chips expose flat and elevated container style classes.
    @Test
    void chipSupportsContainerStyles() {
        M3Chip chip = new M3AssistChip("Assist");

        assertEquals(M3ChipStyle.FLAT, chip.getChipStyle());
        assertTrue(chip.getStyleClass().contains(M3ChipStyle.FLAT.styleClass()));
        assertFalse(chip.getStyleClass().contains(M3ChipStyle.ELEVATED.styleClass()));

        chip.setChipStyle(M3ChipStyle.ELEVATED);

        assertEquals(M3ChipStyle.ELEVATED, chip.getChipStyle());
        assertTrue(chip.getStyleClass().contains(M3ChipStyle.ELEVATED.styleClass()));
        assertFalse(chip.getStyleClass().contains(M3ChipStyle.FLAT.styleClass()));
    }

    /// Verifies that chip interaction states keep Material colors.
    @Test
    void chipStateStylesPreserveVariantColors() {
        M3Chip assistChip = new M3AssistChip("Assist");
        M3FilterChip filterChip = new M3FilterChip("Filter");
        filterChip.setSelected(true);
        Pane root = new Pane(assistChip, filterChip);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        applyInteractivePseudoClasses(assistChip);
        applyInteractivePseudoClasses(filterChip);
        root.applyCss();

        assertLabeledColors(assistChip, Color.TRANSPARENT, Color.rgb(34, 35, 36));
        assertBorderColor(assistChip, Color.rgb(43, 44, 45));
        assertLabeledColors(filterChip, Color.rgb(7, 8, 9), Color.rgb(10, 11, 12));
        assertBorderColor(filterChip, Color.TRANSPARENT);
    }
}
