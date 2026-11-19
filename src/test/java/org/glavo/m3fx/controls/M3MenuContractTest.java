// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.skins.M3MenuSkin;
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

/// Verifies menu selection, nested navigation, actions, and popup interaction.
@NotNullByDefault
final class M3MenuContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Browsing a dropdown is tentative until the user commits a choice.
    @Test
    void exposedDropdownCommitsOnlyConfirmedChoices() {
        FxTestUtils.runOnFxThread(() -> {
            var field = new M3ComboBox<Integer>();
            field.getItems().addAll(10, 20, 30);
            field.setValue(20);
            field.setConverter(n -> n + " min");
            var changes = new AtomicInteger();
            field.valueProperty().addListener((obs, old, next) -> changes.incrementAndGet());
            var stage = new Stage();
            try {
                stage.setScene(new Scene(new VBox(field), 320, 180));
                stage.show();
                field.requestFocus();
                assertEquals("20 min", ((Label) field.lookup(".label")).getText());
                field.fireEvent(primaryMouseEvent(field, MouseEvent.MOUSE_PRESSED, 8, 20, true));
                field.fireEvent(primaryMouseEvent(field, MouseEvent.MOUSE_RELEASED, 8, 20, false));
                assertTrue(field.isShowing());
                field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
                assertFalse(field.isShowing());
                assertEquals(0, changes.get());

                FxTestUtils.assertNoCssWarnings(() -> field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN)));
                field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertEquals(20, field.getValue());
                field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                assertEquals(30, field.getValue());
                assertFalse(field.isShowing());
                assertEquals(1, changes.get());
                assertEquals("30 min", ((Label) field.lookup(".label")).getText());

                var model = new SimpleObjectProperty<>(10);
                field.valueProperty().bind(model);
                field.show();
                field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                field.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                assertFalse(field.isShowing());
                assertEquals(10, field.getValue());
                field.show();
                field.getItems().clear();
                assertFalse(field.isShowing());
                field.show();
                assertFalse(field.isShowing());
            } finally {
                stage.hide();
            }
        });
    }

    /// Verifies visible transition frames, reversal on the same popup, and synchronous reduced-motion dismissal.
    @Tier2Test
    @Test
    void exposedDropdownReversesAndSettlesMenuMotion() throws InterruptedException {
        Stage stage = FxTestUtils.callOnFxThread(Stage::new);
        M3ComboBox<String> field = FxTestUtils.callOnFxThread(M3ComboBox::new);
        AtomicReference<ListView<?>> menu = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(() -> menu.get().getOpacity() > 0 && menu.get().getOpacity() < 1, () -> {
                field.getItems().addAll("First", "Second", "Third");
                stage.setScene(new Scene(new VBox(field), 280, 160));
                stage.show();
                field.show();
                menu.set(assertInstanceOf(ListView.class, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)));
            }, () -> assertTrue(menu.get().getScaleY() < 1));

            ListView<?> list = menu.get();
            Window window = FxTestUtils.callOnFxThread(() -> list.getScene().getWindow());
            FxTestUtils.runOnFxThreadWhen(() -> list.getOpacity() == 1 && list.getScaleY() == 1,
                    () -> {}, () -> assertEquals(0, list.getTranslateY(), 0.001));
            FxTestUtils.runOnFxThreadWhen(() -> list.getOpacity() > 0 && list.getOpacity() < 1, field::hide, () -> {
                assertFalse(field.isShowing());
                assertTrue(window.isShowing());
                double opacity = list.getOpacity();
                double scale = list.getScaleY();
                field.show();
                assertTrue(field.isShowing());
                assertSame(window, list.getScene().getWindow());
                assertEquals(opacity, list.getOpacity(), 0.001);
                assertEquals(scale, list.getScaleY(), 0.001);
                field.setAnimationsEnabled(false);
                assertEquals(1, list.getOpacity());
                assertEquals(1, list.getScaleY());
                field.hide();
                assertFalse(window.isShowing());
                field.show();
                assertEquals(1, list.getOpacity());
                field.setAnimationsEnabled(true);
            });
            FxTestUtils.runOnFxThreadWhen(() -> !window.isShowing(), field::hide, () -> assertFalse(field.isShowing()));
        } finally {
            FxTestUtils.runOnFxThread(stage::close);
        }
    }

    /// A scrollable, field-width menu inherits direction and accepts a click on a rendered option.
    @Test
    void exposedDropdownAnchorsAndPicksRenderedOptions() {
        FxTestUtils.runOnFxThread(() -> {
            var field = new M3ComboBox<Integer>();
            field.setVariant(M3TextInputVariant.OUTLINED);
            field.setPromptText("Choose a time");
            field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            for (int i = 0; i < 100; i++) {
                field.getItems().add(i);
            }
            field.setValue(62);
            var stage = new Stage();
            try {
                stage.setScene(new Scene(new VBox(field), 320, 180));
                stage.show();
                field.executeAccessibleAction(AccessibleAction.EXPAND);
                ListView<?> list = assertInstanceOf(ListView.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                list.applyCss();
                list.layout();
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, list.getEffectiveNodeOrientation());
                assertEquals(field.getWidth(), list.getWidth(), 1);
                assertEquals(62, list.getSelectionModel().getSelectedIndex());
                assertTrue(list.getHeight() < list.getFixedCellSize() * field.getItems().size());
                ListCell<?> cell = list.lookupAll(".list-cell").stream()
                        .filter(n -> n instanceof ListCell<?> c && !c.isEmpty() && c.getIndex() != 62)
                        .map(n -> (ListCell<?>) n).findFirst().orElseThrow();
                int index = cell.getIndex();
                cell.fireEvent(primaryMouseEvent(cell, MouseEvent.MOUSE_CLICKED, 8, 8, false));
                assertFalse(field.isShowing());
                assertEquals(index, field.getValue());
            } finally {
                stage.hide();
            }
        });
    }

    /// Verifies that menu buttons open their popup from a full-bounds pointer click near the layout edge.
    @Test
    void menuButtonPicksFullLayoutBoundsToShowPopup() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuButton button = new M3MenuButton("Open", new M3MenuItem("First"));
            Pane root = new Pane(button);
            Scene scene = new Scene(root, 280.0, 180.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                button.resizeRelocate(20.0, 20.0, 180.0, Math.max(56.0, button.prefHeight(180.0)));
                root.layout();
                button.layout();

                double x = Math.max(1.0, button.getLayoutBounds().getWidth() - 2.0);
                double y = Math.max(1.0, button.getLayoutBounds().getHeight() - 2.0);
                assertTrue(button.isPickOnBounds(), "M3MenuButton should pick on full layout bounds");
                assertTrue(button.contains(x, y),
                        () -> "M3MenuButton should contain a pointer near the layout edge: x=" + x + ", y=" + y);

                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_PRESSED, x, y, true));
                assertTrue(button.isArmed(), "M3MenuButton should arm from a full-bounds pointer press");
                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_RELEASED, x, y, false));

                assertFalse(button.isArmed(), "M3MenuButton should disarm after pointer release");
                assertTrue(button.isShowing(), "M3MenuButton should show its menu from a full-bounds pointer click");
            } finally {
                button.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu tokens apply to menu surfaces and items.
    @Test
    void menuAppliesItemMetrics() {
        M3MenuItem open = new M3MenuItem("Open");
        M3MenuItem save = new M3MenuItem("Save");
        M3MenuItem close = new M3MenuItem("Close");
        M3MenuItem archive = new M3MenuItem("Archive");
        M3Menu menu = new M3Menu(open, save, close, new M3Divider(), archive);
        Pane root = new Pane(menu);
        Scene scene = new Scene(root);
        PseudoClass firstItem = PseudoClass.getPseudoClass("first-menu-item");
        PseudoClass lastItem = PseudoClass.getPseudoClass("last-menu-item");

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3MenuSkin.class, menu.getSkin());
        VBox container = assertInstanceOf(VBox.class, menu.lookup("." + "m3-menu-container"));
        assertTrue(container.getStyleClass().contains("m3-menu-container"));
        assertTrue(open.getPseudoClassStates().contains(firstItem));
        assertFalse(open.getPseudoClassStates().contains(lastItem));
        assertFalse(save.getPseudoClassStates().contains(firstItem));
        assertFalse(save.getPseudoClassStates().contains(lastItem));
        assertFalse(close.getPseudoClassStates().contains(firstItem));
        assertTrue(close.getPseudoClassStates().contains(lastItem));
        assertTrue(archive.getPseudoClassStates().contains(firstItem));
        assertTrue(archive.getPseudoClassStates().contains(lastItem));
        assertEquals(0.0, container.getSpacing(), 0.0001);
        assertEquals(8.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(48.0, open.getOneLineHeight(), 0.0001);
        assertEquals(4.0, open.getContainerShape(), 0.0001);
        assertEquals(4.0, save.getContainerShape(), 0.0001);
        assertEquals(4.0, close.getContainerShape(), 0.0001);
        assertEquals(4.0, open.getInnerCornerShape(), 0.0001);
        assertEquals(4.0, archive.getInnerCornerShape(), 0.0001);
        assertEquals(12.0, open.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, open.getContentSpacing(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, open.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, open.getMaxWidth(), 0.0001);

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.select(save);
        root.applyCss();
        assertEquals(4.0, save.getContainerShape(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        menu.clearSelection();
        root.applyCss();

        assertEquals(2.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(2.0, container.getSpacing(), 0.0001);
        assertEquals(44.0, open.getOneLineHeight(), 0.0001);
        assertEquals(16.0, open.getContainerShape(), 0.0001);
        assertEquals(6.0, save.getContainerShape(), 0.0001);
        assertEquals(16.0, close.getContainerShape(), 0.0001);
        assertEquals(16.0, archive.getContainerShape(), 0.0001);
        assertEquals(6.0, open.getInnerCornerShape(), 0.0001);
        assertEquals(6.0, archive.getInnerCornerShape(), 0.0001);
        assertEquals(16.0, open.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, open.getContentSpacing(), 0.0001);

        menu.select(save);
        root.applyCss();
        assertEquals(16.0, save.getContainerShape(), 0.0001);

        menu.getItems().remove(open);
        root.applyCss();
        assertFalse(open.getPseudoClassStates().contains(firstItem));
        assertFalse(open.getPseudoClassStates().contains(lastItem));
        assertTrue(save.getPseudoClassStates().contains(firstItem));
        assertFalse(save.getPseudoClassStates().contains(lastItem));
        assertFalse(close.getPseudoClassStates().contains(firstItem));
        assertTrue(close.getPseudoClassStates().contains(lastItem));
        assertTrue(archive.getPseudoClassStates().contains(firstItem));
        assertTrue(archive.getPseudoClassStates().contains(lastItem));

        close.setVisible(false);
        root.applyCss();
        assertTrue(save.getPseudoClassStates().contains(lastItem));
        assertFalse(close.getPseudoClassStates().contains(lastItem));

        close.setVisible(true);
        root.applyCss();
        assertFalse(save.getPseudoClassStates().contains(lastItem));
        assertTrue(close.getPseudoClassStates().contains(lastItem));
    }

    /// Verifies that menu layout fills default Material rows without rewriting application-owned constraints.
    @Test
    void menuSkinPreservesApplicationOwnedChildWidthConstraints() {
        M3MenuItem defaultItem = new M3MenuItem("Default");
        M3Divider defaultDivider = new M3Divider();
        M3MenuItem constrainedItem = new M3MenuItem("Constrained");
        constrainedItem.setMinWidth(40.0);
        constrainedItem.setMaxWidth(144.0);
        Region customRegion = new Region();
        customRegion.setMinWidth(36.0);
        customRegion.setMaxWidth(132.0);
        M3Menu menu = new M3Menu(defaultItem, defaultDivider, constrainedItem, customRegion);
        Pane root = new Pane(menu);
        Scene scene = new Scene(root, 320.0, 260.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        menu.resize(280.0, menu.prefHeight(280.0));
        menu.layout();

        VBox container = assertInstanceOf(VBox.class, menu.lookup("." + "m3-menu-container"));
        assertEquals(container.getWidth(), defaultItem.getWidth(), 0.0001);
        assertEquals(container.getWidth(), defaultDivider.getWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, defaultItem.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, defaultItem.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, defaultDivider.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, defaultDivider.getMaxWidth(), 0.0001);
        assertEquals(40.0, constrainedItem.getMinWidth(), 0.0001);
        assertEquals(144.0, constrainedItem.getMaxWidth(), 0.0001);
        assertEquals(36.0, customRegion.getMinWidth(), 0.0001);
        assertEquals(132.0, customRegion.getMaxWidth(), 0.0001);

        menu.getItems().remove(customRegion);
        assertNull(customRegion.getParent());
        assertEquals(36.0, customRegion.getMinWidth(), 0.0001);
        assertEquals(132.0, customRegion.getMaxWidth(), 0.0001);

        PseudoClass firstItem = PseudoClass.getPseudoClass("first-menu-item");
        PseudoClass lastItem = PseudoClass.getPseudoClass("last-menu-item");
        assertTrue(defaultItem.getPseudoClassStates().contains(firstItem));
        assertTrue(constrainedItem.getPseudoClassStates().contains(lastItem));

        FxTestUtils.replaceSkin(menu, M3MenuSkin::new);
        root.applyCss();
        menu.layout();
        assertEquals(40.0, constrainedItem.getMinWidth(), 0.0001);
        assertEquals(144.0, constrainedItem.getMaxWidth(), 0.0001);
        assertTrue(defaultItem.getPseudoClassStates().contains(firstItem));
        assertTrue(constrainedItem.getPseudoClassStates().contains(lastItem));

        menu.getSkin().dispose();
        assertNull(defaultItem.getParent());
        assertNull(defaultDivider.getParent());
        assertNull(constrainedItem.getParent());
        assertFalse(defaultItem.getPseudoClassStates().contains(firstItem));
        assertFalse(constrainedItem.getPseudoClassStates().contains(lastItem));
        assertEquals(40.0, constrainedItem.getMinWidth(), 0.0001);
        assertEquals(144.0, constrainedItem.getMaxWidth(), 0.0001);
    }

    /// Verifies that menu color styles follow baseline and Expressive color mappings.
    @Test
    void menuColorStylesResolveMaterialColorMappings() {
        assertMenuColorMapping(
                M3Theme.defaultTheme(),
                M3MenuColorStyle.STANDARD,
                Color.rgb(1, 2, 3),
                Color.rgb(7, 8, 9),
                Color.rgb(10, 11, 12),
                Color.rgb(25, 26, 27)
        );
        assertMenuColorMapping(
                M3Theme.fromSeed(Color.web("#006a6a"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT),
                M3MenuColorStyle.STANDARD,
                Color.rgb(4, 5, 6),
                Color.rgb(13, 14, 15),
                Color.rgb(16, 17, 18),
                Color.rgb(25, 26, 27)
        );
        assertMenuColorMapping(
                M3Theme.fromSeed(Color.web("#006a6a"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT),
                M3MenuColorStyle.VIBRANT,
                Color.rgb(13, 14, 15),
                Color.rgb(19, 20, 21),
                Color.rgb(22, 23, 24),
                Color.rgb(16, 17, 18)
        );
    }

    /// Verifies that menu color styles propagate through submenu branches and are cleared on removal.
    @Test
    void menuColorStylePropagatesThroughSubMenuBranches() {
        M3MenuItem leafItem = new M3MenuItem("Leaf");
        M3SubMenuItem nestedSubMenuItem = new M3SubMenuItem("Nested", leafItem);
        M3MenuItem childItem = new M3MenuItem("Child");
        M3SubMenuItem parentSubMenuItem = new M3SubMenuItem("Move to", childItem, nestedSubMenuItem);
        M3Menu menu = new M3Menu(new M3MenuItem("Open"), parentSubMenuItem);
        PseudoClass vibrantPseudoClass = PseudoClass.getPseudoClass("vibrant");

        menu.setColorStyle(M3MenuColorStyle.VIBRANT);

        assertEquals(M3MenuColorStyle.VIBRANT, parentSubMenuItem.getSubMenu().getColorStyle());
        assertEquals(M3MenuColorStyle.VIBRANT, nestedSubMenuItem.getSubMenu().getColorStyle());
        assertTrue(parentSubMenuItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertTrue(childItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertTrue(nestedSubMenuItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertTrue(leafItem.getPseudoClassStates().contains(vibrantPseudoClass));

        menu.setColorStyle(M3MenuColorStyle.STANDARD);

        assertEquals(M3MenuColorStyle.STANDARD, parentSubMenuItem.getSubMenu().getColorStyle());
        assertEquals(M3MenuColorStyle.STANDARD, nestedSubMenuItem.getSubMenu().getColorStyle());
        assertFalse(parentSubMenuItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertFalse(childItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertFalse(nestedSubMenuItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertFalse(leafItem.getPseudoClassStates().contains(vibrantPseudoClass));

        menu.setColorStyle(M3MenuColorStyle.VIBRANT);
        menu.getItems().remove(parentSubMenuItem);

        assertEquals(M3MenuColorStyle.STANDARD, parentSubMenuItem.getSubMenu().getColorStyle());
        assertEquals(M3MenuColorStyle.STANDARD, nestedSubMenuItem.getSubMenu().getColorStyle());
        assertFalse(parentSubMenuItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertFalse(childItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertFalse(nestedSubMenuItem.getPseudoClassStates().contains(vibrantPseudoClass));
        assertFalse(leafItem.getPseudoClassStates().contains(vibrantPseudoClass));
    }

    /// Verifies one menu color mapping against resolved JavaFX paints.
    private static void assertMenuColorMapping(
            M3Theme theme,
            M3MenuColorStyle colorStyle,
            Color expectedMenuFill,
            Color expectedSelectedFill,
            Color expectedSelectedTextFill,
            Color expectedStateLayerFill
    ) {
        M3MenuItem open = new M3MenuItem("Open");
        M3SVGIcon selectedIcon = svgTestIcon();
        M3MenuItem save = new M3MenuItem("Save", selectedIcon);
        M3MenuItem disabled = new M3MenuItem("Unavailable");
        disabled.setDisable(true);
        M3Menu menu = new M3Menu(open, save, disabled);
        menu.setColorStyle(colorStyle);
        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.select(save);

        VBox root = new VBox(menu);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, theme);
        String colorTokens = menuColorStyleTestColors();
        root.setStyle(root.getStyle() + " " + colorTokens);
        menu.setStyle(colorTokens);
        open.setStyle(colorTokens);
        save.setStyle(colorTokens);
        root.applyCss();

        PseudoClass vibrantPseudoClass = PseudoClass.getPseudoClass("vibrant");
        boolean vibrant = colorStyle == M3MenuColorStyle.VIBRANT;
        Region selectedContainer = lookupRegion(save, ".m3-menu-item-selection-container");
        assertEquals(colorStyle, menu.getColorStyle());
        assertEquals(vibrant, menu.getPseudoClassStates().contains(vibrantPseudoClass));
        assertEquals(vibrant, save.getPseudoClassStates().contains(vibrantPseudoClass));
        assertEquals(vibrant, selectedContainer.getPseudoClassStates().contains(vibrantPseudoClass));
        assertRegionFill(menu, expectedMenuFill);
        assertRegionFill(selectedContainer, expectedSelectedFill);
        Label selectedHeadline = assertInstanceOf(Label.class, save.lookup(".m3-list-item-headline"));
        assertEquals(expectedSelectedTextFill, selectedHeadline.getTextFill());
        assertEquals(
                expectedSelectedTextFill,
                assertInstanceOf(SVGPath.class, selectedIcon.lookup(".m3-svg-icon-path")).getFill()
        );
        assertRegionFill(lookupRegion(open, ".m3-state-layer"), expectedStateLayerFill);
        assertRegionFill(lookupRegion(disabled, ".m3-list-item-container"), Color.TRANSPARENT);
    }

    /// Returns deterministic color tokens used by menu color style tests.
    private static String menuColorStyleTestColors() {
        return "-m3-color-surface-container: rgb(1,2,3); "
                + "-m3-color-surface-container-low: rgb(4,5,6); "
                + "-m3-color-secondary-container: rgb(7,8,9); "
                + "-m3-color-on-secondary-container: rgb(10,11,12); "
                + "-m3-color-tertiary-container: rgb(13,14,15); "
                + "-m3-color-on-tertiary-container: rgb(16,17,18); "
                + "-m3-color-tertiary: rgb(19,20,21); "
                + "-m3-color-on-tertiary: rgb(22,23,24); "
                + "-m3-color-on-surface: rgb(25,26,27); "
                + "-m3-color-on-surface-variant: rgb(28,29,30);";
    }

    /// Verifies that menu item action events bubble through the menu.
    @Test
    void menuItemActionsBubbleThroughMenu() {
        M3MenuItem open = new M3MenuItem("Open");
        M3Menu menu = new M3Menu(open);
        AtomicInteger actions = new AtomicInteger();
        menu.addEventHandler(javafx.event.ActionEvent.ACTION, event -> actions.incrementAndGet());

        open.fire();

        assertEquals(1, actions.get());
    }

    /// Verifies that menu item constructors install slots while action handlers remain explicit properties.
    @Test
    void menuItemConstructorInstallsSlotsAndActionPropertyFires() {
        Label leading = new Label("L");
        Label trailing = new Label("T");
        AtomicInteger actions = new AtomicInteger();
        M3MenuItem item = new M3MenuItem("Open", leading, trailing);
        item.setOnAction(event -> actions.incrementAndGet());

        item.fire();

        assertEquals("Open", item.getHeadlineText());
        assertEquals(leading, item.getLeading());
        assertEquals(trailing, item.getTrailing());
        assertEquals(1, actions.get());
    }

    /// Verifies that menu section headers are non-selectable menu content.
    @Test
    void menuSectionHeadersAreNonSelectableContent() {
        M3MenuSectionHeader fileHeader = new M3MenuSectionHeader("File");
        M3Divider divider = new M3Divider();
        M3MenuItem open = new M3MenuItem("Open");
        M3MenuItem save = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(fileHeader, open, divider, save);

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals("File", fileHeader.getText());
        assertFalse(fileHeader.isFocusTraversable());
        assertEquals(4, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(fileHeader, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(open, menu.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(2));
        menu.selectIndex(1);
        assertEquals(open, menu.getSelectedItem());
        menu.selectIndex(3);
        assertEquals(save, menu.getSelectedItem());

        menu.selectNext();

        assertEquals(open, menu.getSelectedItem());

        menu.selectPrevious();

        assertEquals(save, menu.getSelectedItem());
    }

    /// Verifies that menu section headers receive menu-specific metrics.
    @Test
    void menuSectionHeaderUsesMenuStylesheet() {
        M3MenuSectionHeader header = new M3MenuSectionHeader("Recent");
        M3Menu menu = new M3Menu(header);
        Pane root = new Pane(menu);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertTrue(header.getStyleClass().contains("m3-menu-section-header"));
        assertEquals(32.0, header.getPrefHeight(), 0.0001);
        assertEquals(12.0, header.getPadding().getTop(), 0.0001);
        assertEquals(4.0, header.getPadding().getBottom(), 0.0001);
    }

    /// Verifies that menu section headers and dividers render as grouped menu content.
    @Tier2Test
    @Test
    void menuSectionHeaderSnapshotRendersGroupedContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuSectionHeader fileHeader = new M3MenuSectionHeader("File");
            M3MenuItem open = new M3MenuItem("Open", visualIcon("folder"));
            M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
            M3MenuItem save = new M3MenuItem("Save", visualIcon("save"));
            M3MenuSectionHeader recentHeader = new M3MenuSectionHeader("Recent");
            M3MenuItem project = new M3MenuItem("Project Alpha", visualIcon("work"));
            project.setSelected(true);
            M3Menu menu = new M3Menu(
                    fileHeader,
                    open,
                    export,
                    save,
                    new M3Divider(),
                    recentHeader,
                    project
            );
            menu.setSelectionMode(M3SelectionMode.SINGLE);
            menu.setPrefWidth(280.0);
            FlowPane root = new FlowPane(menu);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 340.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(340.0, 260.0);
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, fileHeader, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, export, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, recentHeader, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, project, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-menu-section-headers.png"
            ));
        });
    }

    /// Verifies that action menus do not auto-select items by default.
    @Test
    void menuDefaultSelectionModeDoesNotSelectOnAction() {
        M3MenuItem open = new M3MenuItem("Open");
        M3Menu menu = new M3Menu(open);

        open.fire();

        assertEquals(M3SelectionMode.NONE, menu.getSelectionMode());
        assertFalse(open.isSelected());
        assertNull(menu.getSelectedItem());
        assertTrue(menu.getSelectedItems().isEmpty());
    }

    /// Verifies that menus track manual item selections in child order.
    @Test
    void menuTracksManualSelections() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3MenuItem third = new M3MenuItem("Third");
        M3Menu menu = new M3Menu(first, second, third);

        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), menu.getSelectedItems());
        assertEquals(first, menu.getSelectedItem());
        assertEquals(0, menu.getSelectedIndex());

        first.setSelected(false);

        assertEquals(java.util.List.of(third), menu.getSelectedItems());
        assertEquals(third, menu.getSelectedItem());
        assertEquals(2, menu.getSelectedIndex());
    }

    /// Verifies that menus can enforce single selected item behavior.
    @Test
    void menuCanUseSingleSelection() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        first.setSelected(true);
        second.setSelected(true);

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, menu.getSelectedItem());
        assertEquals(java.util.List.of(second), menu.getSelectedItems());

        menu.selectIndex(0);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertEquals(first, menu.getSelectedItem());
        assertEquals(0, menu.getSelectedIndex());

        menu.selectNext();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, menu.getSelectedItem());
        assertEquals(1, menu.getSelectedIndex());

        menu.selectPrevious();

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertEquals(first, menu.getSelectedItem());
        assertEquals(0, menu.getSelectedIndex());
    }

    /// Verifies that menus can use multiple selected item behavior.
    @Test
    void menuCanUseMultipleSelection() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        menu.setSelectionMode(M3SelectionMode.MULTIPLE);
        first.fire();
        second.fire();

        assertTrue(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(java.util.List.of(first, second), menu.getSelectedItems());

        first.fire();

        assertFalse(first.isSelected());
        assertEquals(java.util.List.of(second), menu.getSelectedItems());
    }

    /// Verifies that menus can require a selected item.
    @Test
    void menuCanRequireSelection() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(first, menu.getSelectedItem());
        assertTrue(first.isSelected());

        menu.clearSelection();

        assertEquals(first, menu.getSelectedItem());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, menu.getSelectedItem());
        assertTrue(second.isSelected());
    }

    /// Verifies that submenu items own nested menu content and expose submenu accessibility state.
    @Test
    void subMenuItemOwnsNestedMenuContentAndAccessibleState() {
        M3MenuItem exportPdf = new M3MenuItem("PDF");
        M3MenuItem exportHtml = new M3MenuItem("HTML");
        M3SubMenuItem export = new M3SubMenuItem("Export", exportPdf, exportHtml);

        assertTrue(export.getStyleClass().contains("m3-sub-menu-item"));
        assertEquals(2, export.getItems().size());
        assertEquals(exportPdf, export.getItems().get(0));
        assertEquals(export.getSubMenu(), export.queryAccessibleAttribute(AccessibleAttribute.SUBMENU));
        assertEquals(false, export.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(2, export.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(exportHtml, export.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertInternalIcon(export.getTrailing(), M3InternalIcon.Glyph.CHEVRON_RIGHT, "submenu indicator");

        export.getItems().clear();

        assertTrue(export.getItems().isEmpty());
    }

    /// Verifies that the default submenu indicator mirrors in right-to-left layouts.
    @Test
    void subMenuItemMirrorsDefaultIndicatorForRightToLeft() {
        M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
        export.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        applyCss(export);

        assertInternalIcon(export.getTrailing(), M3InternalIcon.Glyph.CHEVRON_LEFT, "RTL submenu indicator");
    }

    /// Verifies that submenu items are focusable menu items but do not participate in parent menu selection.
    @Test
    void menuSelectionSkipsSubMenuItems() {
        M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
        M3MenuItem open = new M3MenuItem("Open");
        M3MenuItem save = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(export, open, save);

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(open, menu.getSelectedItem());
        assertFalse(export.isSelected());
        assertThrows(IllegalArgumentException.class, () -> menu.select(export));
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(0));

        menu.selectNext();

        assertEquals(save, menu.getSelectedItem());

        export.setSelected(true);

        assertFalse(export.isSelected());
        assertEquals(save, menu.getSelectedItem());
    }

    /// Verifies that menus reject disabled and hidden selection targets from every selection entry point.
    @Test
    void menuSelectionRejectsDisabledAndHiddenItems() {
        M3MenuItem disabled = new M3MenuItem("Disabled");
        M3MenuItem hidden = new M3MenuItem("Hidden");
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        disabled.setDisable(true);
        hidden.setVisible(false);
        M3Menu menu = new M3Menu(disabled, hidden, first, second);
        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(first, menu.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> menu.select(disabled));
        assertThrows(IllegalArgumentException.class, () -> menu.select(hidden));
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> menu.selectIndex(1));

        disabled.setSelected(true);
        hidden.setSelected(true);

        assertFalse(disabled.isSelected());
        assertFalse(hidden.isSelected());
        assertEquals(first, menu.getSelectedItem());
        assertEquals(List.of(first), menu.getSelectedItems());

        first.setDisable(true);

        assertFalse(first.isSelected());
        assertEquals(second, menu.getSelectedItem());
        assertEquals(List.of(second), menu.getSelectedItems());

        second.setVisible(false);

        assertFalse(second.isSelected());
        assertNull(menu.getSelectedItem());
        assertTrue(menu.getSelectedItems().isEmpty());
    }

    /// Verifies that submenu item actions open locally and submenu child actions bubble to an owning menu.
    @Test
    void subMenuItemConsumesOwnActionAndForwardsNestedActions() {
        M3MenuItem pdf = new M3MenuItem("PDF");
        M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
        M3Menu menu = new M3Menu(export);
        AtomicInteger menuActions = new AtomicInteger();
        menu.addEventHandler(ActionEvent.ACTION, event -> menuActions.incrementAndGet());

        export.fire();

        assertEquals(0, menuActions.get());

        pdf.fire();

        assertEquals(1, menuActions.get());
    }

    /// Verifies that submenu hover opens and closes the submenu popup after Material delays.
    @Tier2Test
    @Test
    void subMenuItemHoverOpensAndClosesPopup() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> itemReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(() -> subMenuPopupSurfaceHasSettled(itemReference), () -> {
                Stage stage = new Stage();
                M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3Menu menu = new M3Menu(export);
                Pane root = new Pane(menu);
                Scene scene = new Scene(root, 320.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.fromSeed(
                        Color.web("#006a6a"),
                        M3Profile.EXPRESSIVE_2025,
                        Brightness.LIGHT
                ));
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(320.0, 220.0);
                root.layout();

                stageReference.set(stage);
                itemReference.set(export);
                export.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 8.0, 8.0, false));
            }, () -> {
                M3SubMenuItem item = Objects.requireNonNull(itemReference.get(), "submenu item");
                assertTrue(item.isSubMenuShowing());
                assertTrue(item.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")));
                item.applyCss();
                assertEquals(24.0, item.getContainerShape(), 0.0001);

                M3Menu subMenu = item.getSubMenu();
                subMenu.applyCss();
                subMenu.resize(subMenu.prefWidth(-1.0), subMenu.prefHeight(-1.0));
                subMenu.layout();
                assertFalse(subMenu.getItems().isEmpty());
                assertTrue(subMenu.getWidth() > 0.0);
                assertTrue(subMenu.getHeight() > 0.0);
                assertPopupSurfaceSettled(subMenu, "submenu popup");

                WritableImage image = snapshotImageOnFxThread(subMenu);
                Text pdfText = renderedTextNode(subMenu, "PDF");
                requiredRenderedTextInkBounds(image, pdfText);
                assertSnapshotHasColorVariety(image, 2);
                assertRenderedTextNodesStayInsideLayout(subMenu);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-submenu-popup.png"
                ));
            });

            FxTestUtils.runOnFxThreadWhen(() -> {
                @Nullable M3SubMenuItem item = itemReference.get();
                return item != null && !item.isSubMenuShowing();
            }, () -> {
                M3SubMenuItem item = Objects.requireNonNull(itemReference.get(), "submenu item");
                item.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 8.0, 8.0, false));
            }, () -> {
                M3SubMenuItem item = Objects.requireNonNull(itemReference.get(), "submenu item");
                assertFalse(item.isSubMenuShowing());
                assertFalse(item.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")));
                item.applyCss();
                assertEquals(16.0, item.getContainerShape(), 0.0001);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that popup positioning flips submenus away from screen edges.
    @Test
    void popupPositioningFlipsSubMenusAwayFromScreenEdges() {
        Rectangle2D screen = new Rectangle2D(0.0, 0.0, 300.0, 200.0);
        M3PopupPositioning.Placement rightPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(40.0, 40.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0
        );
        M3PopupPositioning.Placement leftPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(250.0, 40.0, 40.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0
        );
        M3PopupPositioning.Placement bottomClampedPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(40.0, 180.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0
        );

        assertFalse(rightPlacement.opensToLeft());
        assertEquals(119.0, rightPlacement.x(), 0.0001);
        assertTrue(leftPlacement.opensToLeft());
        assertEquals(131.0, leftPlacement.x(), 0.0001);
        assertEquals(120.0, bottomClampedPlacement.y(), 0.0001);
    }

    /// Verifies that popup positioning prefers the logical leading side for right-to-left submenus.
    @Test
    void popupPositioningPrefersLeftForRightToLeftSubMenus() {
        Rectangle2D screen = new Rectangle2D(0.0, 0.0, 360.0, 200.0);
        M3PopupPositioning.Placement rightToLeftPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(160.0, 40.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0,
                true
        );
        M3PopupPositioning.Placement fallbackPlacement = M3PopupPositioning.subMenuBeside(
                new BoundingBox(16.0, 40.0, 80.0, 32.0),
                screen,
                120.0,
                80.0,
                -1.0,
                true
        );

        assertTrue(rightToLeftPlacement.opensToLeft());
        assertEquals(41.0, rightToLeftPlacement.x(), 0.0001);
        assertFalse(fallbackPlacement.opensToLeft());
    }

    /// Verifies that popup positioning flips menu popups above their owner near the bottom edge.
    @Test
    void popupPositioningFlipsMenusAboveBottomEdges() {
        Rectangle2D screen = new Rectangle2D(0.0, 0.0, 300.0, 200.0);
        M3PopupPositioning.Placement belowPlacement = M3PopupPositioning.menuBelowOrAbove(
                new BoundingBox(40.0, 40.0, 80.0, 32.0),
                screen,
                140.0,
                80.0,
                4.0
        );
        M3PopupPositioning.Placement abovePlacement = M3PopupPositioning.menuBelowOrAbove(
                new BoundingBox(40.0, 170.0, 80.0, 24.0),
                screen,
                140.0,
                80.0,
                4.0
        );

        assertFalse(belowPlacement.opensAbove());
        assertEquals(76.0, belowPlacement.y(), 0.0001);
        assertTrue(abovePlacement.opensAbove());
        assertEquals(86.0, abovePlacement.y(), 0.0001);
    }

    /// Verifies that menus update selection when items are removed.
    @Test
    void menuUpdatesSelectionWhenChildrenChange() {
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(first, second);

        second.setSelected(true);
        menu.getItems().remove(second);

        assertTrue(menu.getSelectedItems().isEmpty());
        assertNull(menu.getSelectedItem());
        assertFalse(second.isSelected());

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        assertEquals(first, menu.getSelectedItem());
        assertTrue(first.isSelected());
    }

    /// Verifies that menu buttons expose their menu and still fire action events.
    @Test
    void menuButtonOwnsMenuItemsAndFiresActions() {
        M3MenuItem item = new M3MenuItem("Open");
        M3MenuButton menuButton = new M3MenuButton("More", item);
        AtomicInteger actions = new AtomicInteger();
        menuButton.setOnAction(event -> actions.incrementAndGet());

        menuButton.fire();

        assertEquals(item, menuButton.getItems().get(0));
        assertEquals(1, actions.get());
        assertFalse(menuButton.isShowing());
    }

    /// Verifies that menu buttons keep selection behavior on their owned menu.
    @Test
    void menuButtonKeepsSelectionOnOwnedMenu() {
        M3MenuButton menuButton = new M3MenuButton("More");
        M3MenuItem first = new M3MenuItem("First");
        M3MenuItem second = new M3MenuItem("Second");
        menuButton.getItems().addAll(first, second);

        menuButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
        menuButton.getMenu().setAllowEmptySelection(false);
        menuButton.getMenu().selectIndex(1);

        assertEquals(M3SelectionMode.SINGLE, menuButton.getMenu().getSelectionMode());
        assertFalse(menuButton.getMenu().isAllowEmptySelection());
        assertEquals(second, menuButton.getMenu().getSelectedItem());
        assertEquals(java.util.List.of(second), menuButton.getMenu().getSelectedItems());
        assertEquals(1, menuButton.getMenu().getSelectedIndex());

        menuButton.getMenu().selectPrevious();

        assertEquals(first, menuButton.getMenu().getSelectedItem());

        menuButton.getMenu().selectNext();

        assertEquals(second, menuButton.getMenu().getSelectedItem());

        menuButton.getMenu().selectLast();

        assertEquals(second, menuButton.getMenu().getSelectedItem());

        menuButton.getMenu().clearSelection();

        assertEquals(second, menuButton.getMenu().getSelectedItem());
        assertTrue(second.isSelected());

        menuButton.getItems().clear();

        assertTrue(menuButton.getItems().isEmpty());
    }

    /// Verifies that menu button keyboard shortcuts are safe before the button is attached to a scene.
    @Test
    void menuButtonKeyboardShortcutsAreSafeBeforeSceneAttachment() {
        M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("First"));

        menuButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        menuButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
        menuButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertFalse(menuButton.isShowing());
    }

    /// Verifies that popup menu surfaces preserve application-bound minimum widths.
    @Test
    void popupMenuSurfacesPreserveBoundMinWidths() {
        FxTestUtils.runOnFxThread(() -> {
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Duplicate"), subMenuItem);
            SimpleDoubleProperty menuMinWidth = new SimpleDoubleProperty(136.0);
            SimpleDoubleProperty subMenuMinWidth = new SimpleDoubleProperty(148.0);
            menuButton.getMenu().minWidthProperty().bind(menuMinWidth);
            subMenuItem.getSubMenu().minWidthProperty().bind(subMenuMinWidth);
            HBox root = new HBox(16.0, menuButton);
            root.setPadding(new Insets(20.0));
            Stage stage = new Stage();

            try {
                stage.setScene(new Scene(root, 420.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                subMenuItem.showSubMenu();

                assertTrue(menuButton.isShowing());
                assertTrue(subMenuItem.isSubMenuShowing());
                assertEquals(136.0, menuButton.getMenu().getMinWidth(), 0.0001);
                assertEquals(148.0, subMenuItem.getSubMenu().getMinWidth(), 0.0001);

                menuMinWidth.set(164.0);
                subMenuMinWidth.set(176.0);
                menuButton.hideMenu();
                subMenuItem.hideSubMenu();
                menuButton.showMenu();
                subMenuItem.showSubMenu();

                assertEquals(164.0, menuButton.getMenu().getMinWidth(), 0.0001);
                assertEquals(176.0, subMenuItem.getSubMenu().getMinWidth(), 0.0001);
            } finally {
                menuButton.hideMenu();
                subMenuItem.hideSubMenu();
                menuButton.getMenu().minWidthProperty().unbind();
                subMenuItem.getSubMenu().minWidthProperty().unbind();
                stage.close();
            }
        });
    }

    /// Verifies that popup menus activate focused items and reveal requested menu content.
    @Test
    void popupMenusActivateFocusedItemsAndRevealRequestedContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3Menu menu = new M3Menu(open, export);
            AtomicInteger actions = new AtomicInteger();
            open.setOnAction(event -> actions.incrementAndGet());
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(menu), 240.0, 160.0));
                stage.show();

                open.requestFocus();
                open.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

                assertEquals(1, actions.get());

                export.requestFocus();
                export.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(export.isSubMenuShowing());
                assertTrue(pdf.isFocused());
            } finally {
                export.hideSubMenu();
                stage.close();
            }
        });

        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem first = new M3MenuItem("First");
            M3MenuItem second = new M3MenuItem("Second");
            M3MenuButton menuButton = new M3MenuButton("More", first, second);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(menuButton), 240.0, 120.0));
                stage.show();

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, second);

                assertTrue(menuButton.isShowing());
                assertTrue(second.isFocused());
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that popup menu buttons preserve menu type-ahead navigation across show and hide cycles.
    @Test
    void menuButtonPopupSupportsTypeAheadKeyboardNavigation() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem settings = new M3MenuItem("Settings");
            M3MenuItem share = new M3MenuItem("Share");
            M3MenuButton menuButton = new M3MenuButton("More", archive, settings, share);
            menuButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 320.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                menuButton.getMenu().fireEvent(keyTypedEvent("s"));
                menuButton.getMenu().fireEvent(keyTypedEvent("e"));

                assertTrue(settings.isFocused());
                assertEquals(settings, menuButton.getMenu().getSelectedItem());

                menuButton.hideMenu();
                menuButton.showMenu();
                menuButton.getMenu().fireEvent(keyTypedEvent("a"));

                assertTrue(archive.isFocused());
                assertEquals(archive, menuButton.getMenu().getSelectedItem());
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu button popups inherit a locally installed parent theme.
    @Test
    void menuButtonPopupInheritsLocalParentThemeContext() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("More", archive);
            M3Theme localTheme = M3Theme.defaultTheme();
            Stage stage = new Stage();
            try {
                Pane localRoot = new Pane(menuButton);
                Pane root = new Pane(localRoot);
                M3ThemeManager.install(localRoot, localTheme);
                stage.setScene(new Scene(root, 320.0, 160.0));
                stage.show();
                root.applyCss();
                localRoot.resizeRelocate(0.0, 0.0, 320.0, 160.0);
                menuButton.resizeRelocate(24.0, 24.0, 120.0, 40.0);
                root.layout();

                menuButton.showMenu();

                assertTrue(menuButton.isShowing());
                assertSame(localTheme, M3ThemeManager.getTheme(menuButton.getMenu()));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that an already open menu button popup follows runtime scene theme changes.
    @Test
    void menuButtonPopupReinheritsRuntimeSceneThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Archive"));
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                Scene scene = new Scene(root, 360.0, 180.0);
                M3ThemeManager.install(scene, baselineTheme);
                stage.setScene(scene);
                stage.show();
                menuButton.resizeRelocate(24.0, 24.0, 128.0, 40.0);
                root.applyCss();
                root.layout();

                menuButton.showMenu();

                assertTrue(menuButton.isShowing());
                assertSame(baselineTheme, M3ThemeManager.getTheme(menuButton.getMenu()));

                M3ThemeManager.install(scene, expressiveDarkTheme);

                M3Menu menu = menuButton.getMenu();
                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(menu));
                assertTrue(menu.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(menu.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(menu.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that an already open menu button popup mirrors runtime owner scene stylesheet changes.
    @Test
    void menuButtonPopupMirrorsRuntimeOwnerSceneStylesheetChanges() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        M3Theme theme = M3Theme.defaultTheme();
        String firstStylesheet = M3Stylesheets.controlStylesheet("button.css");
        String secondStylesheet = M3Stylesheets.controlStylesheet("text-field.css");
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> Objects.requireNonNull(stageReference.get()).isFocused(),
                    2,
                    () -> {
                        M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Archive"));
                        Stage stage = new Stage();
                        stageReference.set(stage);
                        menuButtonReference.set(menuButton);
                        Pane root = new Pane(menuButton);
                        Scene scene = new Scene(root, 360.0, 180.0);
                        M3ThemeManager.install(scene, theme);
                        scene.getStylesheets().add(firstStylesheet);
                        stage.setScene(scene);
                        stage.show();
                        menuButton.resizeRelocate(24.0, 24.0, 128.0, 40.0);
                        root.applyCss();
                        root.layout();
                    },
                    () -> {
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get());
                        Scene scene = Objects.requireNonNull(menuButton.getScene());
                        menuButton.showMenu();

                        M3Menu menu = menuButton.getMenu();
                        assertTrue(menuButton.isShowing());
                        assertTrue(menu.getStylesheets().contains(firstStylesheet));
                        assertTrue(menu.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));

                        scene.getStylesheets().remove(firstStylesheet);
                        scene.getStylesheets().add(secondStylesheet);
                    }
            );

            FxTestUtils.runOnFxThread(() -> {
                M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                M3Menu menu = menuButton.getMenu();
                assertTrue(menuButton.isShowing(), "Menu must remain visible while owner stylesheets change");
                assertFalse(menu.getStylesheets().contains(firstStylesheet),
                        () -> "Menu stylesheets: " + menu.getStylesheets());
                assertTrue(menu.getStylesheets().contains(secondStylesheet));
                assertTrue(menu.getStylesheets().contains(M3Stylesheets.controlStylesheet("menu.css")));
                assertSame(theme, M3ThemeManager.getTheme(menu));
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3MenuButton menuButton = menuButtonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that an already open submenu popup follows runtime parent menu theme changes.
    @Test
    void subMenuPopupReinheritsRuntimeParentMenuThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Duplicate"), subMenuItem);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                Scene scene = new Scene(root, 460.0, 260.0);
                M3ThemeManager.install(scene, baselineTheme);
                stage.setScene(scene);
                stage.show();
                menuButton.resizeRelocate(24.0, 24.0, 144.0, 40.0);
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                subMenuItem.showSubMenu();

                assertTrue(menuButton.isShowing());
                assertTrue(subMenuItem.isSubMenuShowing());
                assertSame(baselineTheme, M3ThemeManager.getTheme(subMenuItem.getSubMenu()));

                M3ThemeManager.install(scene, expressiveDarkTheme);

                M3Menu subMenu = subMenuItem.getSubMenu();
                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(subMenu));
                assertTrue(subMenu.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(subMenu.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(subMenu.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)));
            } finally {
                subMenuItem.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu button accessibility focus requests follow the active popup focus branch.
    @Test
    void menuButtonRoutesAccessibleFocusAcrossPopupBranches() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem save = new M3MenuItem("Save");
            M3MenuButton menuButton = new M3MenuButton("More", open, save);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 260.0, 140.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(menuButton.isFocused());

                menuButton.showMenu();
                assertEquals(menuButton, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(open.isFocused());
                assertEquals(open, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                save.requestFocus();
                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(save.isFocused());
                assertEquals(save, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that popup menus prefer selected items for default accessibility focus requests.
    @Test
    void popupMenusPreferSelectedAccessibleFocusTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem menuOpen = new M3MenuItem("Open");
            M3MenuItem menuSave = new M3MenuItem("Save");
            M3Menu menu = new M3Menu(menuOpen, menuSave);
            menu.setSelectionMode(M3SelectionMode.SINGLE);
            menu.select(menuSave);

            M3MenuItem buttonOpen = new M3MenuItem("Button open");
            M3MenuItem buttonSave = new M3MenuItem("Button save");
            M3MenuButton menuButton = new M3MenuButton("More", buttonOpen, buttonSave);
            menuButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
            menuButton.getMenu().select(buttonSave);

            M3MenuItem pdf = new M3MenuItem("PDF");
            M3MenuItem html = new M3MenuItem("HTML");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf, html);
            export.getSubMenu().setSelectionMode(M3SelectionMode.SINGLE);
            export.getSubMenu().select(html);

            M3Button outsideFocus = new M3Button("Outside");
            Stage stage = new Stage();
            try {
                VBox root = new VBox(outsideFocus, menu, menuButton, export);
                stage.setScene(new Scene(root, 360.0, 260.0));
                stage.show();
                root.applyCss();
                root.layout();

                outsideFocus.requestFocus();
                menu.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(menuSave.isFocused());
                assertSame(menuSave, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                outsideFocus.requestFocus();
                menu.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(menuSave.isFocused());

                outsideFocus.requestFocus();
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(menuButton.isShowing());
                assertTrue(buttonSave.isFocused());
                assertSame(buttonSave, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                outsideFocus.requestFocus();
                export.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(export.isSubMenuShowing());
                assertTrue(html.isFocused());
                assertSame(html, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                export.hideSubMenu();
                stage.close();
            }
        });
    }

    /// Verifies that accessibility item requests can open nested menu branches and focus descendants.
    @Test
    void popupMenusOpenNestedBranchesForAccessibleTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3MenuButton menuButton = new M3MenuButton("More", export);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, pdf);

                assertTrue(menuButton.isShowing());
                assertTrue(export.isSubMenuShowing());
                assertTrue(pdf.isFocused());
                assertEquals(pdf, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(pdf.isFocused());
            } finally {
                export.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that nested submenu keyboard traversal and accessible focus routing stay in the active popup branch.
    @Test
    void nestedSubMenuKeyboardTraversalPreservesAccessibleFocusBranch() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3MenuItem html = new M3MenuItem("HTML");
            M3SubMenuItem recent = new M3SubMenuItem("Recent", pdf, html);
            M3SubMenuItem export = new M3SubMenuItem("Export", recent);
            M3MenuButton menuButton = new M3MenuButton("More", export);
            Stage stage = new Stage();

            try {
                menuButton.setAnimationsEnabled(false);
                export.setAnimationsEnabled(false);
                recent.setAnimationsEnabled(false);

                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 360.0, 220.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                assertTrue(menuButton.isShowing());
                assertSame(menuButton, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(export.isFocused());
                assertSame(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(export.isSubMenuShowing());
                assertTrue(recent.isFocused());
                assertSame(recent, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(recent, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                recent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(recent.isSubMenuShowing());
                assertTrue(pdf.isFocused());
                assertSame(pdf, recent.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdf, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(pdf.isFocused());
                assertSame(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pdf.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(html.isFocused());
                assertSame(html, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                recent.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertFalse(recent.isSubMenuShowing());
                assertTrue(recent.isFocused());
                assertTrue(export.isSubMenuShowing());
                assertSame(recent, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertTrue(menuButton.isShowing());
                assertSame(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                export.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(export.isSubMenuShowing());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, export.getSubMenu().getNodeOrientation());
                assertTrue(recent.isFocused());

                recent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(recent.isSubMenuShowing());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, recent.getSubMenu().getNodeOrientation());
                assertTrue(pdf.isFocused());
                assertSame(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                recent.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertFalse(recent.isSubMenuShowing());
                assertTrue(recent.isFocused());
                assertTrue(export.isSubMenuShowing());

                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertTrue(menuButton.isShowing());
            } finally {
                menuButton.setAnimationsEnabled(true);
                export.setAnimationsEnabled(true);
                recent.setAnimationsEnabled(true);
                recent.hideSubMenu();
                export.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that open menu popup branches mirror owner direction changes and keep routed focus.
    @Test
    void openNestedMenuPopupBranchesMirrorRuntimeInheritedOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3MenuItem html = new M3MenuItem("HTML");
            M3SubMenuItem recent = new M3SubMenuItem("Recent", pdf, html);
            M3SubMenuItem export = new M3SubMenuItem("Export", recent);
            M3MenuButton menuButton = new M3MenuButton("More", export);
            Pane root = new Pane(menuButton);
            Stage stage = new Stage();

            try {
                menuButton.setAnimationsEnabled(false);
                export.setAnimationsEnabled(false);
                recent.setAnimationsEnabled(false);

                stage.setScene(new Scene(root, 360.0, 220.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(export.isFocused());

                export.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(export.isSubMenuShowing());
                assertTrue(recent.isFocused());

                recent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(recent.isSubMenuShowing());
                assertTrue(pdf.isFocused());
                assertSame(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                menuButton.getMenu().layout();
                export.getSubMenu().layout();
                recent.getSubMenu().layout();

                assertEquals(NodeOrientation.RIGHT_TO_LEFT, menuButton.getEffectiveNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, menuButton.getMenu().getNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, export.getEffectiveNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, export.getSubMenu().getNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, recent.getEffectiveNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, recent.getSubMenu().getNodeOrientation());
                assertTrue(pdf.isFocused());
                assertSame(pdf, recent.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdf, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                recent.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertFalse(recent.isSubMenuShowing());
                assertTrue(recent.isFocused());
                assertTrue(export.isSubMenuShowing());
                assertSame(recent, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertTrue(menuButton.isShowing());
                assertSame(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.setAnimationsEnabled(true);
                export.setAnimationsEnabled(true);
                recent.setAnimationsEnabled(true);
                recent.hideSubMenu();
                export.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that popup menu branches inherit theme color lookups without CSS conversion warnings.
    @Test
    void popupMenuBranchesResolveThemeColorLookups() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem inbox = new M3MenuItem("Inbox");
            M3SubMenuItem moveTo = new M3SubMenuItem("Move to", archive, inbox);
            M3MenuButton menuButton = new M3MenuButton(
                    "Open menu",
                    new M3MenuSectionHeader("Document"),
                    new M3MenuItem("Duplicate"),
                    moveTo
            );
            menuButton.setVariant(M3ButtonVariant.OUTLINED);
            Pane root = new Pane(menuButton);
            Scene scene = new Scene(root, 420.0, 260.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, theme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                moveTo.showSubMenu();

                M3Menu menu = menuButton.getMenu();
                M3Menu subMenu = moveTo.getSubMenu();
                menu.applyCss();
                menu.layout();
                subMenu.applyCss();
                subMenu.layout();

                assertTrue(menuButton.isShowing());
                assertTrue(moveTo.isSubMenuShowing());
                assertTrue(subMenu.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertTrue(subMenu.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));
                assertResolvedBackgroundFill(menu, "menu");
                assertResolvedBackgroundFill(subMenu, "submenu");
                assertResolvedListItemTextFill(moveTo);
                assertResolvedListItemTextFill(archive);
                assertResolvedListItemTextFill(inbox);
            } finally {
                moveTo.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        }));
    }

    /// Verifies that popup-owned menu content has fallback tokens before its owner enters a scene.
    @Test
    void popupMenuContentResolvesFallbackBeforeOwnerAttachment() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3MenuButton menuButton = new M3MenuButton(
                    "Open menu",
                    new M3MenuItem("Duplicate"),
                    new M3MenuItem("Archive")
            );
            M3Menu menu = menuButton.getMenu();

            menu.applyCss();
            menu.layout();

            assertTrue(menu.getStyleClass().contains(M3PopupStyles.FALLBACK_ROOT_STYLE_CLASS));
            assertTrue(menu.getStylesheets().contains(M3Stylesheets.fallbackStylesheet()));
            assertResolvedListItemTextFill(assertInstanceOf(M3MenuItem.class, menu.getItems().get(0)));
        }));
    }

    /// Verifies that submenu hover open and close delays refresh while hover transitions are pending.
    @Tier2Test
    @Test
    void subMenuHoverDelayRefreshUsesThemeBehavior() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                        return subMenuItem != null && subMenuItem.isSubMenuShowing();
                    },
                    () -> {
                        M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
                        subMenuItem.setMinSize(160.0, 48.0);
                        VBox root = new VBox(subMenuItem);
                        FxTestUtils.setCssAnimationsEnabled(root, false);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 320.0, 160.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setMotionBehavior(root, interactionTimingBehavior(
                                Duration.seconds(30.0),
                                Duration.seconds(30.0),
                                Duration.seconds(30.0)
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();

                        subMenuItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_ENTERED, 8.0, 8.0, false));
                        assertFalse(subMenuItem.isSubMenuShowing());

                        stageReference.set(stage);
                        rootReference.set(root);
                        subMenuItemReference.set(subMenuItem);
                        FxTestUtils.setMotionBehavior(root, interactionTimingBehavior(
                                Duration.seconds(30.0),
                                Duration.millis(40.0),
                                Duration.seconds(30.0)
                        ));
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        M3SubMenuItem subMenuItem =
                                Objects.requireNonNull(subMenuItemReference.get(), "subMenuItem");
                        assertTrue(subMenuItem.isSubMenuShowing());

                        subMenuItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_EXITED, 8.0, 8.0, false));
                        assertTrue(subMenuItem.isSubMenuShowing());
                        FxTestUtils.setMotionBehavior(root, interactionTimingBehavior(
                                Duration.seconds(30.0),
                                Duration.millis(40.0),
                                Duration.millis(40.0)
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                        return subMenuItem != null && !subMenuItem.isSubMenuShowing();
                    },
                    () -> {
                    },
                    () -> assertFalse(Objects.requireNonNull(
                            subMenuItemReference.get(),
                            "subMenuItem"
                    ).isSubMenuShowing())
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                if (subMenuItem != null) {
                    subMenuItem.hideSubMenu();
                }
            });
            closeMotionTestScene(stageReference, rootReference);
        }
    }

    /// Verifies that menu keyboard focus can land on submenu items without corrupting menu selection.
    @Test
    void menuKeyboardNavigationFocusesSubMenuItemsWithoutChangingSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem save = new M3MenuItem("Save");
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3Menu menu = new M3Menu(export, open, save);
            menu.setSelectionMode(M3SelectionMode.SINGLE);
            menu.select(open);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(menu);
                stage.setScene(new Scene(root, 240.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                open.requestFocus();
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));

                assertTrue(export.isFocused());
                assertEquals(open, menu.getSelectedItem());
                assertEquals(export, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(open.isFocused());
                assertEquals(open, menu.getSelectedItem());

                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(save.isFocused());
                assertEquals(save, menu.getSelectedItem());

                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(export.isSubMenuShowing());
                assertTrue(pdf.isFocused());
                assertEquals(pdf, export.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(pdf, menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                export.hideSubMenu();
                stage.close();
            }
        });
    }

    /// Verifies that submenu keyboard open and close keys mirror in right-to-left menus.
    @Tier2Test
    @Test
    void menuKeyboardNavigationMirrorsSubMenuKeysForRightToLeft() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> exportReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(() -> {
                @Nullable M3SubMenuItem export = exportReference.get();
                return export != null && !export.isSubMenuShowing() && export.isFocused();
            }, () -> {
                M3MenuItem pdf = new M3MenuItem("PDF");
                M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
                M3Menu menu = new M3Menu(export);
                menu.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                Stage stage = new Stage();
                Pane root = new Pane(menu);
                stage.setScene(new Scene(root, 240.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                exportReference.set(export);
                export.requestFocus();
                menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(export.isSubMenuShowing());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, export.getSubMenu().getNodeOrientation());
                assertTrue(pdf.isFocused());

                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
            }, () -> {
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());

                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3SubMenuItem export = exportReference.get();
                if (export != null) {
                    export.hideSubMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that opening one submenu closes sibling submenus and keeps focus in the active branch.
    @Tier2Test
    @Test
    void siblingSubMenusAreMutuallyExclusive() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> exportReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> shareReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuItem> emailReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(() -> {
                @Nullable M3SubMenuItem export = exportReference.get();
                @Nullable M3SubMenuItem share = shareReference.get();
                @Nullable M3MenuItem email = emailReference.get();
                return export != null
                        && share != null
                        && email != null
                        && !export.isSubMenuShowing()
                        && share.isSubMenuShowing()
                        && email.isFocused();
            }, () -> {
                M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3MenuItem email = new M3MenuItem("Email");
                M3SubMenuItem share = new M3SubMenuItem("Share", email);
                M3Menu menu = new M3Menu(export, share);
                Stage stage = new Stage();
                Pane root = new Pane(menu);
                stage.setScene(new Scene(root, 320.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                exportReference.set(export);
                shareReference.set(share);
                emailReference.set(email);

                assertTrue(export.showSubMenuAndFocusFirstItem());
                assertTrue(export.isSubMenuShowing());
                assertTrue(share.showSubMenuAndFocusFirstItem());
            }, () -> {
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());
                M3SubMenuItem share = Objects.requireNonNull(shareReference.get());
                M3MenuItem email = Objects.requireNonNull(emailReference.get());

                assertFalse(export.isSubMenuShowing());
                assertTrue(share.isSubMenuShowing());
                assertTrue(email.isFocused());
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3SubMenuItem export = exportReference.get();
                if (export != null) {
                    export.hideSubMenu();
                }
                @Nullable M3SubMenuItem share = shareReference.get();
                if (share != null) {
                    share.hideSubMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that submenu dismissal returns focus to the owning item without closing the parent popup.
    @Tier2Test
    @Test
    void subMenuEscapeReturnsFocusToOwnerItem() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> buttonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> exportReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(() -> {
                @Nullable M3MenuButton menuButton = buttonReference.get();
                @Nullable M3SubMenuItem export = exportReference.get();
                return menuButton != null
                        && export != null
                        && menuButton.isShowing()
                        && !export.isSubMenuShowing()
                        && export.isFocused();
            }, () -> {
                M3SubMenuItem export = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3MenuButton menuButton = new M3MenuButton("More", export);
                Stage stage = new Stage();
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                buttonReference.set(menuButton);
                exportReference.set(export);

                menuButton.showMenu();
                assertTrue(export.showSubMenuAndFocusFirstItem());
                export.getSubMenu().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            }, () -> {
                M3MenuButton menuButton = Objects.requireNonNull(buttonReference.get());
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());

                assertTrue(menuButton.isShowing());
                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertEquals(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3MenuButton menuButton = buttonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that submenu actions close the owning popup menu and return focus to the menu button.
    @Tier2Test
    @Test
    void subMenuActionClosesMenuButtonPopup() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> buttonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> exportReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(() -> {
                @Nullable M3MenuButton menuButton = buttonReference.get();
                @Nullable M3SubMenuItem export = exportReference.get();
                return menuButton != null
                        && export != null
                        && !export.isSubMenuShowing()
                        && !menuButton.isShowing()
                        && menuButton.isFocused();
            }, () -> {
                M3MenuItem pdf = new M3MenuItem("PDF");
                M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
                M3MenuButton menuButton = new M3MenuButton("More", export);
                Stage stage = new Stage();
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                buttonReference.set(menuButton);
                exportReference.set(export);

                menuButton.showMenu();
                assertTrue(export.showSubMenuAndFocusFirstItem());
                assertEquals(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pdf.fire();
            }, () -> {
                M3MenuButton menuButton = Objects.requireNonNull(buttonReference.get());
                M3SubMenuItem export = Objects.requireNonNull(exportReference.get());

                assertFalse(export.isSubMenuShowing());
                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3MenuButton menuButton = buttonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that menus support printable-key type-ahead focus navigation.
    @Test
    void menuSupportsTypeAheadKeyboardNavigation() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem open = new M3MenuItem("Open");
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem disabledSave = new M3MenuItem("Save");
            M3MenuItem settings = new M3MenuItem("Settings");
            disabledSave.setDisable(true);

            M3Menu menu = new M3Menu(open, archive, disabledSave, settings);
            menu.setSelectionMode(M3SelectionMode.SINGLE);
            menu.select(open);

            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Pane(menu), 320.0, 220.0));
                stage.show();
                menu.applyCss();
                menu.layout();

                menu.fireEvent(keyTypedEvent("s"));
                assertEquals(settings, menu.getSelectedItem());
                assertTrue(settings.isFocused());
                assertFalse(disabledSave.isFocused());

                menu.fireEvent(keyTypedEvent("a"));
                menu.fireEvent(keyTypedEvent("r"));
                assertEquals(archive, menu.getSelectedItem());
                assertTrue(archive.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that menu buttons expose their popup menu to accessibility clients.
    @Test
    void menuButtonExposesAccessiblePopupState() {
        M3MenuItem first = new M3MenuItem("Open");
        M3MenuItem second = new M3MenuItem("Save");
        M3MenuButton menuButton = new M3MenuButton("More", first, second);
        menuButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);

        assertEquals(false, menuButton.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(menuButton.getMenu(), menuButton.queryAccessibleAttribute(AccessibleAttribute.SUBMENU));
        assertEquals(2, menuButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, menuButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, menuButton.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(menuButton.getMenu().getSelectedItems(),
                menuButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        menuButton.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, second);
        assertEquals(second, menuButton.getMenu().getSelectedItem());
        menuButton.executeAccessibleAction(AccessibleAction.SHOW_MENU);
        menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);
    }

    /// Returns whether the submenu popup surface has reached its final identity transform.
    private static boolean subMenuPopupSurfaceHasSettled(
            AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference
    ) {
        @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
        return subMenuItem != null
                && subMenuItem.isSubMenuShowing()
                && popupSurfaceIsSettled(subMenuItem.getSubMenu());
    }

    /// Verifies that a region background resolved to a concrete color.
    private static void assertResolvedBackgroundFill(Region region, String description) {
        assertTrue(region.getBackground() != null, () -> description + " has no background");
        assertFalse(region.getBackground().getFills().isEmpty(), () -> description + " has no background fills");
        assertInstanceOf(
                Color.class,
                region.getBackground().getFills().get(0).getFill(),
                () -> description + " background fill did not resolve to a color"
        );
    }

    /// Verifies that a row-item headline resolved to a concrete color.
    private static void assertResolvedListItemTextFill(M3ListItemBase item) {
        Node headline = item.lookup(".m3-list-item-headline");
        Label label = assertInstanceOf(Label.class, headline);
        assertInstanceOf(Color.class, label.getTextFill());
    }
}
