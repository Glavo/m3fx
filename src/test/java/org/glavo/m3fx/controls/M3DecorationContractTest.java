// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.skins.M3AvatarSkin;
import org.glavo.m3fx.skins.M3BadgeSkin;
import org.glavo.m3fx.skins.M3BadgedBoxSkin;
import org.glavo.m3fx.skins.M3DisclosureIconSkin;
import org.glavo.m3fx.skins.M3DividerSkin;
import org.glavo.m3fx.skins.M3IconSkin;
import org.glavo.m3fx.skins.M3SVGIconSkin;
import org.glavo.m3fx.skins.M3SurfaceSkin;
import org.glavo.m3fx.skins.M3TextSkin;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies icons, typography, badges, avatars, and surface presentation.
@NotNullByDefault
final class M3DecorationContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that built-in affordance icons use stable vector geometry instead of font fallback text.
    @Test
    void builtInAffordancesUseInternalVectorIcons() {
        M3TextInputLayout textInputLayout = new M3TextInputLayout(new M3TextField("Text"));
        textInputLayout.setClearButtonEnabled(true);
        applyCss(textInputLayout);
        assertInternalIconButton(textInputClearButton(textInputLayout), M3InternalIcon.Glyph.CLOSE, "text field clear button");

        M3DatePickerField dateField = new M3DatePickerField();
        assertInternalIconButton(dateField.getInputLayout().getTrailing(), M3InternalIcon.Glyph.CALENDAR, "date field picker button");

        M3TimePickerField timeField = new M3TimePickerField();
        assertInternalIconButton(timeField.getInputLayout().getTrailing(), M3InternalIcon.Glyph.SCHEDULE, "time field picker button");

        M3DateRangePickerField rangeField = new M3DateRangePickerField();
        assertInternalIconButton(
                rangeField.getStartInputLayout().getTrailing(),
                M3InternalIcon.Glyph.CALENDAR,
                "range start picker button"
        );
        assertInternalIconButton(
                rangeField.getEndInputLayout().getTrailing(),
                M3InternalIcon.Glyph.CALENDAR,
                "range end picker button"
        );

        M3FabMenu fabMenu = new M3FabMenu();
        assertInternalIcon(fabMenu.getToggleButton().getGraphic(), M3InternalIcon.Glyph.ADD, "FAB menu toggle");
        assertInternalIcon(fabMenu.getCloseButton().getGraphic(), M3InternalIcon.Glyph.CLOSE, "FAB menu close");

        M3SearchBar searchBar = new M3SearchBar("Search");
        assertInternalIcon(searchBar.getLeading(), M3InternalIcon.Glyph.SEARCH, "search bar leading icon");

        M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
        assertInternalIcon(subMenuItem.getTrailing(), M3InternalIcon.Glyph.CHEVRON_RIGHT, "submenu indicator");

        subMenuItem.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        applyCss(subMenuItem);

        assertInternalIcon(subMenuItem.getTrailing(), M3InternalIcon.Glyph.CHEVRON_LEFT, "RTL submenu indicator");
    }

    /// Verifies that internal vector icons resolve color through style classes instead of inline CSS.
    @Test
    void internalVectorIconColorRolesUseStyleClasses() {
        M3InternalIcon icon = new M3InternalIcon(
                M3InternalIcon.Glyph.SEARCH,
                M3InternalIcon.ColorRole.PRIMARY
        );

        assertInternalIcon(icon, M3InternalIcon.Glyph.SEARCH, "primary internal icon");
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.PRIMARY);

        icon.setColorRole(M3InternalIcon.ColorRole.ON_SURFACE);
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.ON_SURFACE);

        icon.setColorRole(M3InternalIcon.ColorRole.ON_SURFACE_VARIANT);
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.ON_SURFACE_VARIANT);

        icon.setColorRole(M3InternalIcon.ColorRole.ON_PRIMARY_CONTAINER);
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.ON_PRIMARY_CONTAINER);

        icon.setColorRole(M3InternalIcon.ColorRole.ON_PRIMARY);
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.ON_PRIMARY);

        icon.setColorRole(M3InternalIcon.ColorRole.ON_SECONDARY);
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.ON_SECONDARY);

        icon.setColorRole(M3InternalIcon.ColorRole.ON_TERTIARY);
        assertInternalIconColorRole(icon, M3InternalIcon.ColorRole.ON_TERTIARY);
    }

    /// Verifies the state-specific elevation of elevated Material surfaces.
    @Test
    void materialSurfacesUseStateSpecificElevation() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Elevated", M3ButtonVariant.ELEVATED);
            M3Chip chip = new M3AssistChip("Elevated chip");
            chip.setChipStyle(M3ChipStyle.ELEVATED);
            M3FloatingActionButton fab = new M3FloatingActionButton("Create");
            M3Card card = new M3Card(new Label("Elevated card"), M3CardVariant.ELEVATED);
            card.setOnAction(event -> {
            });
            HBox root = new HBox(16.0, button, chip, fab, card);
            Scene scene = new Scene(root, 720.0, 180.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            root.layout();

            Region cardContainer = lookupRegion(card, ".m3-card-container");
            List<Node> effectTargets = List.of(button, chip, fab, cardContainer);
            List<DropShadow> enabledShadows = effectTargets.stream()
                    .map(ControlStyleAssertions::assertDropShadow)
                    .toList();
            List<Node> owners = List.of(button, chip, fab, card);
            PseudoClass focusVisible = PseudoClass.getPseudoClass("focus-visible");
            PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
            PseudoClass hover = PseudoClass.getPseudoClass("hover");

            for (Node owner : owners) {
                owner.pseudoClassStateChanged(focusVisible, true);
            }
            root.applyCss();
            for (int index = 0; index < effectTargets.size(); index++) {
                DropShadow focused = assertDropShadow(effectTargets.get(index));
                assertEquals(enabledShadows.get(index).getRadius(), focused.getRadius(), 0.0001);
                assertEquals(enabledShadows.get(index).getOffsetY(), focused.getOffsetY(), 0.0001);
            }

            for (Node owner : owners) {
                owner.pseudoClassStateChanged(focusVisible, false);
                owner.pseudoClassStateChanged(pressed, true);
            }
            root.applyCss();
            for (int index = 0; index < effectTargets.size(); index++) {
                DropShadow pressedShadow = assertDropShadow(effectTargets.get(index));
                assertEquals(enabledShadows.get(index).getRadius(), pressedShadow.getRadius(), 0.0001);
                assertEquals(enabledShadows.get(index).getOffsetY(), pressedShadow.getOffsetY(), 0.0001);
            }

            for (Node owner : owners) {
                owner.pseudoClassStateChanged(pressed, false);
                owner.pseudoClassStateChanged(hover, true);
            }
            root.applyCss();
            for (int index = 0; index < effectTargets.size(); index++) {
                DropShadow hovered = assertDropShadow(effectTargets.get(index));
                if (index == 0) {
                    assertEquals(enabledShadows.get(index).getRadius(), hovered.getRadius(), 0.0001);
                    assertEquals(enabledShadows.get(index).getOffsetY(), hovered.getOffsetY(), 0.0001);
                } else {
                    assertTrue(hovered.getRadius() > enabledShadows.get(index).getRadius());
                    assertTrue(hovered.getOffsetY() > enabledShadows.get(index).getOffsetY());
                }
            }
        });
    }

    /// Verifies that avatars swap between text and graphic content.
    @Test
    void avatarOwnsTextAndGraphicContent() {
        M3Avatar avatar = new M3Avatar("AB");
        Label graphic = new Label("G");
        graphic.setAccessibleText("Graphic avatar");

        applyCss(avatar);

        assertEquals("AB", avatar.getText());
        assertEquals("AB", avatar.getAccessibleText());
        assertInstanceOf(M3AvatarSkin.class, avatar.getSkin());
        assertInstanceOf(Label.class, avatar.lookup("." + "m3-avatar-label"));

        avatar.setText("CD");
        assertEquals("CD", avatar.getAccessibleText());

        avatar.setGraphic(graphic);

        assertEquals(graphic, avatar.getGraphic());
        assertInstanceOf(StackPane.class, graphic.getParent());
        assertNull(avatar.lookup("." + "m3-avatar-label"));
        assertEquals("Graphic avatar", avatar.getAccessibleText());

        avatar.setGraphic(null);

        assertNull(avatar.getGraphic());
        assertNull(graphic.getParent());
        assertInstanceOf(Label.class, avatar.lookup("." + "m3-avatar-label"));
        assertEquals("CD", avatar.getAccessibleText());
    }

    /// Verifies that avatar component token metrics apply through the active theme.
    @Test
    void avatarAppliesTokenMetrics() {
        M3Avatar avatar = new M3Avatar("A");
        avatar.setStyle("-m3-container-size: 48px;");

        applyCss(avatar);

        double preferredWidth = avatar.prefWidth(-1.0);
        double preferredHeight = avatar.prefHeight(-1.0);

        assertEquals(48.0, avatar.getContainerSize(), 0.0001);
        assertEquals(48.0, preferredWidth, 0.0001,
                () -> "containerSize=" + avatar.getContainerSize() + ", prefWidthProperty=" + avatar.getPrefWidth());
        assertEquals(48.0, preferredHeight, 0.0001,
                () -> "containerSize=" + avatar.getContainerSize() + ", prefHeightProperty=" + avatar.getPrefHeight());

        avatar.setContainerSize(32.0);

        assertEquals(32.0, avatar.prefWidth(-1.0), 0.0001);
        assertEquals(32.0, avatar.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that avatar variants update style classes.
    @Test
    void avatarVariantUpdatesStyleClasses() {
        M3Avatar avatar = createAvatar("A", M3AvatarVariant.SECONDARY);

        assertEquals(M3AvatarVariant.SECONDARY, avatar.getVariant());
        assertTrue(avatar.getStyleClass().contains(M3AvatarVariant.SECONDARY.styleClass()));

        avatar.setVariant(M3AvatarVariant.TERTIARY);

        assertEquals(M3AvatarVariant.TERTIARY, avatar.getVariant());
        assertTrue(avatar.getStyleClass().contains(M3AvatarVariant.TERTIARY.styleClass()));
        assertFalse(avatar.getStyleClass().contains(M3AvatarVariant.SECONDARY.styleClass()));
    }

    /// Verifies that icon size and color variants update style classes.
    @Test
    void iconSizeAndVariantUpdateStyleClasses() {
        M3Icon icon = new M3Icon("A");

        assertEquals(Control.class, M3Icon.class.getSuperclass());
        assertEquals("A", icon.getGlyph());
        assertEquals(M3IconSize.MEDIUM, icon.getSize());
        assertEquals(M3IconVariant.ON_SURFACE_VARIANT, icon.getVariant());
        assertFalse(icon.isFocusTraversable());
        assertTrue(icon.getStyleClass().contains("m3-icon"));
        assertTrue(icon.getStyleClass().contains(M3IconSize.MEDIUM.styleClass()));
        assertTrue(icon.getStyleClass().contains(M3IconVariant.ON_SURFACE_VARIANT.styleClass()));

        icon.setSize(M3IconSize.LARGE);
        icon.setVariant(M3IconVariant.PRIMARY);
        applyCss(icon);

        assertEquals(M3IconSize.LARGE, icon.getSize());
        assertEquals(M3IconVariant.PRIMARY, icon.getVariant());
        assertInstanceOf(M3IconSkin.class, icon.getSkin());
        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertTrue(icon.getStyleClass().contains(M3IconSize.LARGE.styleClass()));
        assertTrue(icon.getStyleClass().contains(M3IconVariant.PRIMARY.styleClass()));
        assertFalse(icon.getStyleClass().contains(M3IconSize.MEDIUM.styleClass()));
        assertFalse(icon.getStyleClass().contains(M3IconVariant.ON_SURFACE_VARIANT.styleClass()));
    }

    /// Verifies that icon font tokens apply through CSS.
    @Test
    void iconTokensAreStyleable() {
        M3Icon icon = new M3Icon("A", M3IconSize.EXTRA_LARGE, M3IconVariant.TERTIARY);
        icon.setStyle("-m3-icon-font-family: \"Serif\"; -m3-icon-size: 28px; -m3-icon-font-weight: 700;");

        applyCss(icon);

        assertEquals("serif", icon.getIconFontFamily());
        assertEquals(28.0, icon.getIconSize(), 0.0001);
        assertEquals(700.0, icon.getIconFontWeight(), 0.0001);
        assertEquals(28.0, icon.getIconFont().getSize(), 0.0001);
        assertEquals(42.0, icon.prefWidth(-1.0), 0.0001);
        assertEquals(42.0, icon.prefHeight(-1.0), 0.0001);
        assertTrue(icon.prefHeight(-1.0) > icon.getIconSize());
    }

    /// Verifies that fallback text glyphs keep a padded line box instead of touching clipped edges.
    @Tier2Test
    @Test
    void iconFallbackGlyphKeepsClearSnapshotEdges() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon icon = new M3Icon("M", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            StackPane root = new StackPane(icon);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 80.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(80.0, 80.0);
            root.layout();

            assertEquals(18.0, icon.getIconSize(), 0.0001);
            assertEquals(27.0, icon.prefWidth(-1.0), 0.0001);
            assertEquals(27.0, icon.prefHeight(-1.0), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, icon, Color.WHITE, 0.05);
            assertSnapshotNodeEdgesClear(image, icon, Color.WHITE, 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-icon-fallback-glyph.png"
            ));
        });
    }

    /// Verifies that icon size roles provide default size tokens through user-agent CSS.
    @Test
    void iconSizeRoleAppliesDefaultCssToken() {
        M3Icon icon = new M3Icon("A", M3IconSize.LARGE, M3IconVariant.PRIMARY);

        applyCss(icon);

        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(32.0, icon.getIconFont().getSize(), 0.0001);
    }

    /// Verifies that SVG icons accept source viewports independently of their rendered logical-pixel size.
    @Test
    void svgIconSupportsArbitraryViewBoxAndTokenRoles() {
        Rectangle2D materialSymbolViewBox = new Rectangle2D(0.0, -960.0, 960.0, 960.0);
        M3SVGIcon icon = new M3SVGIcon(
                "M440-120v-320H120v-80h320v-320h80v320h320v80H520v320h-80Z",
                materialSymbolViewBox
        );

        assertEquals(Control.class, M3SVGIcon.class.getSuperclass());
        assertEquals(materialSymbolViewBox, icon.getViewBox());
        assertEquals(FillRule.NON_ZERO, icon.getFillRule());
        assertEquals(M3IconSize.MEDIUM, icon.getSize());
        assertEquals(M3IconVariant.ON_SURFACE_VARIANT, icon.getVariant());
        assertFalse(icon.isAutoMirrored());
        assertFalse(icon.isFocusTraversable());
        assertTrue(icon.getStyleClass().contains("m3-svg-icon"));
        assertTrue(icon.getStyleClass().contains("m3-icon-graphic"));

        icon.setSize(M3IconSize.LARGE);
        icon.setVariant(M3IconVariant.PRIMARY);
        icon.setFillRule(FillRule.EVEN_ODD);
        icon.setAutoMirrored(true);
        applyCss(icon);

        assertInstanceOf(M3SVGIconSkin.class, icon.getSkin());
        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(32.0, icon.prefWidth(-1.0), 0.0001);
        assertEquals(32.0, icon.prefHeight(-1.0), 0.0001);
        assertEquals(FillRule.EVEN_ODD, icon.getFillRule());
        assertTrue(icon.isAutoMirrored());
        assertTrue(icon.getStyleClass().contains(M3IconSize.LARGE.styleClass()));
        assertTrue(icon.getStyleClass().contains(M3IconVariant.PRIMARY.styleClass()));

        icon.setViewBox(null);
        assertNull(icon.getViewBox());
        assertThrows(IllegalArgumentException.class,
                () -> icon.setViewBox(new Rectangle2D(0.0, 0.0, 0.0, 960.0)));
        assertThrows(NullPointerException.class, () -> icon.setContent(null));
        assertThrows(NullPointerException.class, () -> icon.setFillRule(null));
    }

    /// Verifies explicit view boxes preserve authored padding and directional icons mirror inside that viewport.
    @Tier2Test
    @Test
    void svgIconPreservesViewportGeometryAndMirrorsForRtl() {
        FxTestUtils.runOnFxThread(() -> {
            String halfWidthPath = "M0-960H480L0 0Z";
            Rectangle2D sourceViewBox = new Rectangle2D(0.0, -960.0, 960.0, 960.0);
            M3SVGIcon explicit = new M3SVGIcon(halfWidthPath, sourceViewBox);
            M3SVGIcon automatic = new M3SVGIcon(halfWidthPath);
            M3SVGIcon mirrored = new M3SVGIcon(halfWidthPath, sourceViewBox);
            explicit.setIconSize(40.0);
            automatic.setIconSize(40.0);
            mirrored.setIconSize(40.0);
            mirrored.setAutoMirrored(true);
            mirrored.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            HBox root = new HBox(20.0, explicit, automatic, mirrored);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 220.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(220.0, 80.0);
            root.layout();
            explicit.layout();
            automatic.layout();
            mirrored.layout();

            SVGPath explicitPath = assertInstanceOf(
                    SVGPath.class,
                    explicit.lookup("." + "m3-svg-icon-path")
            );
            SVGPath automaticPath = assertInstanceOf(
                    SVGPath.class,
                    automatic.lookup("." + "m3-svg-icon-path")
            );
            SVGPath mirroredPath = assertInstanceOf(
                    SVGPath.class,
                    mirrored.lookup("." + "m3-svg-icon-path")
            );
            Bounds explicitBounds = explicitPath.getBoundsInParent();
            Bounds automaticBounds = automaticPath.getBoundsInParent();
            Bounds mirroredBounds = mirroredPath.getBoundsInParent();

            assertEquals(0.0, explicitBounds.getMinX(), 0.75);
            assertEquals(20.0, explicitBounds.getMaxX(), 0.75);
            assertEquals(10.0, automaticBounds.getMinX(), 0.75);
            assertEquals(30.0, automaticBounds.getMaxX(), 0.75);
            assertEquals(20.0, mirroredBounds.getMinX(), 0.75);
            assertEquals(40.0, mirroredBounds.getMaxX(), 0.75);
            assertEquals(40.0, explicitBounds.getHeight(), 0.75);
            assertEquals(40.0, automaticBounds.getHeight(), 0.75);
            assertEquals(40.0, mirroredBounds.getHeight(), 0.75);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, explicit, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, automatic, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, mirrored, Color.WHITE, 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-svg-icon-viewports.png"
            ));
        });
    }

    /// Verifies component graphic slots apply their icon-size and contextual style contracts to SVG icons.
    @Test
    void svgIconParticipatesInComponentGraphicSlots() {
        M3SVGIcon buttonIcon = svgTestIcon();
        M3Button button = new M3Button("Save", buttonIcon, M3ButtonVariant.FILLED);
        button.setIconSize(28.0);
        button.setStyle(buttonStateTestColors());
        applyCss(button);

        assertEquals(28.0, buttonIcon.getIconSize(), 0.0001);
        assertTrue(buttonIcon.getPseudoClassStates().contains(PseudoClass.getPseudoClass("button-graphic")));
        assertEquals(
                Color.rgb(4, 5, 6),
                assertInstanceOf(SVGPath.class, buttonIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon toggleIcon = svgTestIcon();
        M3IconToggleButton toggleButton = new M3IconToggleButton(toggleIcon);
        toggleButton.setVariant(M3IconToggleButtonVariant.TONAL);
        toggleButton.setSelected(true);
        toggleButton.setIconSize(30.0);
        toggleButton.setStyle(buttonStateTestColors());
        applyCss(toggleButton);

        assertEquals(30.0, toggleIcon.getIconSize(), 0.0001);
        assertTrue(toggleIcon.getPseudoClassStates().contains(PseudoClass.getPseudoClass("button-graphic")));
        assertEquals(
                Color.rgb(49, 50, 51),
                assertInstanceOf(SVGPath.class, toggleIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon chipIcon = svgTestIcon();
        M3AssistChip chip = new M3AssistChip("Schedule", chipIcon);
        chip.setIconSize(22.0);
        chip.setStyle(buttonStateTestColors());
        applyCss(chip);

        assertEquals(22.0, chipIcon.getIconSize(), 0.0001);
        assertEquals(
                Color.rgb(1, 2, 3),
                assertInstanceOf(SVGPath.class, chipIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon dialogIcon = svgTestIcon();
        M3DialogPane dialogPane = new M3DialogPane();
        dialogPane.setGraphic(dialogIcon);
        dialogPane.setIconSize(36.0);
        dialogPane.setStyle(buttonStateTestColors());
        applyCss(dialogPane);

        assertEquals(36.0, dialogIcon.getIconSize(), 0.0001);
        assertTrue(dialogIcon.getStyleClass().contains("m3-dialog-graphic-icon"));
        assertEquals(
                Color.rgb(46, 47, 48),
                assertInstanceOf(SVGPath.class, dialogIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon selectedSwitchIcon = svgTestIcon();
        M3SVGIcon unselectedSwitchIcon = svgTestIcon();
        M3Switch switchControl = new M3Switch("Notifications");
        switchControl.setSelectedIcon(selectedSwitchIcon);
        switchControl.setUnselectedIcon(unselectedSwitchIcon);
        switchControl.setIconSize(16.0);
        switchControl.setSelected(true);
        switchControl.setStyle(buttonStateTestColors());
        applyCss(switchControl);

        assertEquals(16.0, selectedSwitchIcon.getIconSize(), 0.0001);
        assertEquals(16.0, unselectedSwitchIcon.getIconSize(), 0.0001);
        assertEquals(
                Color.rgb(1, 2, 3),
                assertInstanceOf(SVGPath.class, selectedSwitchIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon activeTrackIcon = svgTestIcon();
        M3SVGIcon inactiveTrackIcon = svgTestIcon();
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setActiveTrackGraphic(activeTrackIcon);
        slider.setInactiveTrackGraphic(inactiveTrackIcon);
        slider.setIconSize(32.0);
        slider.setStyle(buttonStateTestColors());
        applyCss(slider);

        assertEquals(32.0, activeTrackIcon.getIconSize(), 0.0001);
        assertEquals(32.0, inactiveTrackIcon.getIconSize(), 0.0001);
        assertEquals(
                Color.rgb(4, 5, 6),
                assertInstanceOf(SVGPath.class, activeTrackIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );
        assertEquals(
                Color.rgb(10, 11, 12),
                assertInstanceOf(SVGPath.class, inactiveTrackIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon fabIcon = svgTestIcon();
        M3FloatingActionButton fab = new M3FloatingActionButton(fabIcon);
        fab.setSize(M3FloatingActionButtonSize.LARGE);
        fab.setVariant(M3FloatingActionButtonVariant.PRIMARY);
        fab.setStyle(buttonStateTestColors());
        applyCss(fab);

        assertEquals(36.0, fabIcon.getIconSize(), 0.0001);
        assertEquals(
                Color.rgb(4, 5, 6),
                assertInstanceOf(SVGPath.class, fabIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );

        M3SVGIcon navigationIcon = svgTestIcon();
        M3NavigationItem navigationItem = new M3NavigationItem("Home", navigationIcon);
        navigationItem.setSelected(true);
        navigationItem.setStyle(buttonStateTestColors());
        applyCss(navigationItem);

        assertEquals(
                Color.rgb(10, 11, 12),
                assertInstanceOf(SVGPath.class, navigationIcon.lookup("." + "m3-svg-icon-path")).getFill()
        );
    }

    /// Verifies that disclosure icons expose expanded state and animate their arrow rotation.
    @Tier2Test
    @Test
    void disclosureIconAnimatesExpandedState() throws InterruptedException {
        AtomicReference<@Nullable SVGPath> shape = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> {
                    @Nullable SVGPath arrow = shape.get();
                    return arrow != null && Math.abs(arrow.getRotate()) <= 0.01;
                },
                () -> {
                    M3DisclosureIcon icon = new M3DisclosureIcon();
                    Pane root = new Pane(icon);
                    Scene scene = new Scene(root, 80.0, 80.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    icon.resize(24.0, 24.0);
                    icon.layout();

                    assertFalse(icon.isExpanded());
                    assertTrue(icon.getStyleClass().contains("m3-disclosure-icon"));
                    assertInstanceOf(M3DisclosureIconSkin.class, icon.getSkin());
                    assertEquals(24.0, icon.prefWidth(-1), 0.0001);
                    assertEquals(24.0, icon.prefHeight(-1), 0.0001);

                    SVGPath arrow = assertInstanceOf(SVGPath.class, icon.lookup(".m3-disclosure-icon-shape"));
                    assertEquals(-90.0, arrow.getRotate(), 0.0001);
                    shape.set(arrow);

                    icon.setExpanded(true);
                },
                () -> assertEquals(0.0, shape.get().getRotate(), 0.01)
        );
    }

    /// Verifies that vertical disclosure icons rotate inward from down to up in both layout directions.
    @Test
    void verticalDisclosureIconRotatesDownToUp() {
        FxTestUtils.runOnFxThread(() -> {
            M3DisclosureIcon leftToRightIcon = new M3DisclosureIcon();
            M3DisclosureIcon rightToLeftIcon = new M3DisclosureIcon();
            leftToRightIcon.setVertical(true);
            rightToLeftIcon.setVertical(true);
            rightToLeftIcon.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            HBox root = new HBox(leftToRightIcon, rightToLeftIcon);
            Scene scene = new Scene(root, 80.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            root.layout();

            SVGPath leftToRightArrow = assertInstanceOf(
                    SVGPath.class,
                    leftToRightIcon.lookup(".m3-disclosure-icon-shape")
            );
            SVGPath rightToLeftArrow = assertInstanceOf(
                    SVGPath.class,
                    rightToLeftIcon.lookup(".m3-disclosure-icon-shape")
            );
            assertEquals(0.0, leftToRightArrow.getRotate(), 0.0001);
            assertEquals(0.0, rightToLeftArrow.getRotate(), 0.0001);

            leftToRightIcon.setExpanded(true);
            rightToLeftIcon.setExpanded(true);
            assertEquals(180.0, leftToRightArrow.getRotate(), 0.0001);
            assertEquals(-180.0, rightToLeftArrow.getRotate(), 0.0001);

            leftToRightIcon.setExpanded(false);
            rightToLeftIcon.setExpanded(false);
            assertEquals(0.0, leftToRightArrow.getRotate(), 0.0001);
            assertEquals(0.0, rightToLeftArrow.getRotate(), 0.0001);
        });
    }

    /// Verifies that collapsed disclosure icons point toward logical child content in right-to-left layouts.
    @Test
    void disclosureIconMirrorsCollapsedDirectionForRightToLeft() {
        M3DisclosureIcon leftToRightIcon = new M3DisclosureIcon();
        M3DisclosureIcon rightToLeftIcon = new M3DisclosureIcon();
        rightToLeftIcon.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        HBox root = new HBox(leftToRightIcon, rightToLeftIcon);
        Scene scene = new Scene(root, 80.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.layout();

        SVGPath leftToRightArrow = assertInstanceOf(
                SVGPath.class,
                leftToRightIcon.lookup(".m3-disclosure-icon-shape")
        );
        SVGPath rightToLeftArrow = assertInstanceOf(
                SVGPath.class,
                rightToLeftIcon.lookup(".m3-disclosure-icon-shape")
        );
        assertEquals(-90.0, leftToRightArrow.getRotate(), 0.0001);
        assertEquals(90.0, rightToLeftArrow.getRotate(), 0.0001);
    }

    /// Verifies that disclosure icons render inside fixed list item icon slots without clipping.
    @Test
    void disclosureIconFitsNavigationDrawerSlot() {
        FxTestUtils.runOnFxThread(() -> {
            M3DisclosureIcon disclosure = new M3DisclosureIcon(true);
            M3ListItem groupItem = new M3ListItem("Date & time pickers");
            groupItem.setTrailingMedia(disclosure, M3ListItemSlotSize.ICON);
            groupItem.setSelected(true);
            groupItem.setStyle("-m3-one-line-height: 56px; -m3-container-shape: 28px; -m3-horizontal-padding: 24px;");

            M3ListItem firstChild = new M3ListItem("Date pickers");
            firstChild.setStyle("-m3-one-line-height: 56px; -m3-container-shape: 28px; -m3-horizontal-padding: 32px;");
            M3ListItem secondChild = new M3ListItem("Time pickers");
            secondChild.setStyle("-m3-one-line-height: 56px; -m3-container-shape: 28px; -m3-horizontal-padding: 32px;");

            M3NavigationDrawer drawer = navigationDrawer(groupItem, firstChild, secondChild);
            drawer.setAllowEmptySelection(true);
            drawer.setPrefWidth(320.0);

            Pane root = new Pane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 220.0);
            root.layout();

            StackPane trailingSlot = assertInstanceOf(
                    StackPane.class,
                    groupItem.lookup(".m3-list-item-trailing")
            );
            SVGPath arrow = assertInstanceOf(
                    SVGPath.class,
                    disclosure.lookup(".m3-disclosure-icon-shape")
            );

            assertEquals(24.0, trailingSlot.getLayoutBounds().getWidth(), 0.0001);
            assertEquals(24.0, trailingSlot.getLayoutBounds().getHeight(), 0.0001);
            assertEquals(0.0, arrow.getRotate(), 0.0001);
            assertNodeInsideParent(trailingSlot, arrow);

            Node groupHeadline = Objects.requireNonNull(groupItem.lookup(".m3-list-item-headline"));
            Node childHeadline = Objects.requireNonNull(firstChild.lookup(".m3-list-item-headline"));
            assertTrue(
                    childHeadline.localToScene(childHeadline.getBoundsInLocal()).getMinX()
                            > groupHeadline.localToScene(groupHeadline.getBoundsInLocal()).getMinX() + 6.0
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, disclosure, Color.WHITE, 0.05);
        });
    }

    /// Verifies that text typography roles update style classes.
    @Test
    void textRoleUpdatesStyleClasses() {
        M3Text text = new M3Text("Title");

        applyCss(text);

        assertEquals(M3TextRole.BODY_LARGE, text.getRole());
        assertTrue(text.getStyleClass().contains("m3-text"));
        assertInstanceOf(M3TextSkin.class, text.getSkin());
        assertTrue(text.getStyleClass().contains(M3TextRole.BODY_LARGE.styleClass()));

        text.setRole(M3TextRole.TITLE_LARGE);

        assertEquals(M3TextRole.TITLE_LARGE, text.getRole());
        assertTrue(text.getStyleClass().contains(M3TextRole.TITLE_LARGE.styleClass()));
        assertFalse(text.getStyleClass().contains(M3TextRole.BODY_LARGE.styleClass()));

        text.setRole(M3TextRole.LABEL_SMALL);

        assertEquals(M3TextRole.LABEL_SMALL, text.getRole());
        assertTrue(text.getStyleClass().contains(M3TextRole.LABEL_SMALL.styleClass()));
        assertFalse(text.getStyleClass().contains(M3TextRole.TITLE_LARGE.styleClass()));
    }

    /// Verifies that text typography roles read font size tokens from the active theme.
    @Test
    void textRoleUsesTypographyTokens() {
        M3Text text = new M3Text("Display", M3TextRole.DISPLAY_LARGE);

        applyCss(text);

        assertEquals(57.0, text.getFont().getSize(), 0.0001);
        assertEquals(64.0, text.getTypographyLineHeight(), 0.0001);
        assertEquals(7.0, text.getLineSpacing(), 0.0001);
    }

    /// Verifies that typography font weight tokens support unitless CSS values.
    @Test
    void textFontWeightTokenAcceptsUnitlessCssValue() {
        M3Text text = new M3Text("Title", M3TextRole.TITLE_MEDIUM);
        text.setStyle(
                "-m3-typography-font-family: \"Serif\"; "
                        + "-m3-typography-font-size: 18px; "
                        + "-m3-typography-line-height: 24px; "
                        + "-m3-typography-font-weight: 700;"
        );

        applyCss(text);

        assertEquals("serif", text.getTypographyFontFamily());
        assertEquals(18.0, text.getTypographyFontSize(), 0.0001);
        assertEquals(24.0, text.getTypographyLineHeight(), 0.0001);
        assertEquals(700.0, text.getTypographyFontWeight(), 0.0001);
        assertEquals(18.0, text.getFont().getSize(), 0.0001);
    }

    /// Verifies that surfaces expose variant, elevation, and metric tokens.
    @Test
    void surfaceVariantElevationAndMetricsAreStyleable() {
        M3Surface surface = surface(new Label("Surface"));
        surface.setVariant(M3SurfaceVariant.PRIMARY_CONTAINER);
        surface.setElevation(M3SurfaceElevation.LEVEL3);
        surface.setStyle("-m3-container-shape: 20px; -m3-content-padding: 18px;");

        applyCss(surface);

        assertEquals(M3SurfaceVariant.PRIMARY_CONTAINER, surface.getVariant());
        assertEquals(M3SurfaceElevation.LEVEL3, surface.getElevation());
        assertTrue(surface.getStyleClass().contains(M3SurfaceVariant.PRIMARY_CONTAINER.styleClass()));
        assertTrue(surface.getStyleClass().contains(M3SurfaceElevation.LEVEL3.styleClass()));
        assertEquals(20.0, surface.getContainerShape(), 0.0001);
        assertEquals(18.0, surface.getContentPadding(), 0.0001);
        assertEquals(18.0, surface.getPadding().getTop(), 0.0001);
        assertEquals(1, surface.getContent().size());
        assertInstanceOf(M3SurfaceSkin.class, surface.getSkin());
        Region container = lookupRegion(surface, ".m3-surface-container");
        assertFalse(container.getBackground().getFills().isEmpty());
        assertEquals(
                20.0,
                container.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                0.0001
        );
        assertDropShadow(container);
    }

    /// Verifies that divider component token properties are styleable from CSS.
    @Test
    void dividerTokensAreStyleable() {
        M3Divider divider = new M3Divider(Orientation.VERTICAL);
        divider.setStyle("-m3-thickness: 2px; -m3-inset-start: 12px; -m3-inset-end: 8px;");

        applyCss(divider);

        assertEquals(2.0, divider.getThickness(), 0.0001);
        assertEquals(12.0, divider.getInsetStart(), 0.0001);
        assertEquals(8.0, divider.getInsetEnd(), 0.0001);
        assertInstanceOf(M3DividerSkin.class, divider.getSkin());
    }

    /// Verifies that default divider constraints fill only the axis selected by the current orientation.
    @Test
    void dividerDefaultConstraintsFollowOrientationWithoutMutatingRawProperties() {
        M3Divider divider = new M3Divider();

        applyCss(divider);

        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMinHeight(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMaxHeight(), 0.0001);
        assertEquals(Double.MAX_VALUE, divider.maxWidth(-1.0), 0.0);
        assertTrue(divider.maxHeight(-1.0) < Double.MAX_VALUE);

        divider.setOrientation(Orientation.VERTICAL);

        assertEquals(Double.MAX_VALUE, divider.maxHeight(-1.0), 0.0);
        assertTrue(divider.maxWidth(-1.0) < Double.MAX_VALUE);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMinHeight(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, divider.getMaxHeight(), 0.0001);
    }

    /// Verifies that horizontal divider insets follow logical start and end orientation.
    @Test
    void horizontalDividerInsetsMirrorInRightToLeftLayout() {
        M3Divider divider = new M3Divider();
        divider.setInsetStart(12.0);
        divider.setInsetEnd(4.0);
        Pane root = new Pane(divider);
        Scene scene = new Scene(root, 140.0, 40.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        divider.resizeRelocate(20.0, 20.0, 100.0, divider.prefHeight(100.0));
        divider.layout();

        Region container = lookupRegion(divider, ".m3-divider-container");
        Region line = lookupRegion(divider, ".m3-divider-line");
        assertEquals(new Insets(0.0, 4.0, 0.0, 12.0), container.getPadding());
        assertHorizontalDividerPhysicalInsets(divider, line, 12.0, 4.0);

        divider.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        divider.layout();

        assertEquals(new Insets(0.0, 4.0, 0.0, 12.0), container.getPadding());
        assertHorizontalDividerPhysicalInsets(divider, line, 4.0, 12.0);
    }

    /// Verifies that badge component token properties are styleable from CSS.
    @Test
    void badgeTokensAreStyleable() {
        M3Badge badge = new M3Badge("1234");
        badge.setMaxCharacterCount(2);
        badge.setStyle(
                "-m3-small-size: 8px; "
                        + "-m3-large-height: 18px; "
                        + "-m3-large-min-width: 20px; "
                        + "-m3-container-shape: 9px; "
                        + "-m3-horizontal-padding: 6px;"
        );

        applyCss(badge);

        assertEquals("12+", badge.getDisplayText());
        assertEquals(8.0, badge.getSmallSize(), 0.0001);
        assertEquals(18.0, badge.getLargeHeight(), 0.0001);
        assertEquals(20.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(9.0, badge.getContainerShape(), 0.0001);
        assertEquals(6.0, badge.getHorizontalPadding(), 0.0001);
        assertInstanceOf(M3BadgeSkin.class, badge.getSkin());
    }

    /// Verifies that badges accept non-negative count constructors.
    @Test
    void badgeSupportsCountConstructor() {
        M3Badge badge = new M3Badge(1234);
        badge.setMaxCharacterCount(3);

        assertEquals("1234", badge.getText());
        assertEquals("123+", badge.getDisplayText());

        badge.setText("0");

        assertEquals("0", badge.getText());
        assertEquals("0", badge.getDisplayText());
        assertThrows(IllegalArgumentException.class, () -> new M3Badge(-1));
    }

    /// Verifies that badge skins animate rendered text changes.
    @Test
    void badgeSkinAnimatesDisplayTextChanges() {
        M3Badge badge = new M3Badge("1");
        applyCss(badge);

        Region label = lookupRegion(badge, ".m3-badge-label");
        assertEquals(1.0, label.getOpacity(), 0.0001);
        assertEquals(1.0, label.getScaleX(), 0.0001);

        badge.setText("2");

        assertEquals("2", badge.getDisplayText());
        assertEquals(0.0, label.getOpacity(), 0.0001);
        assertTrue(label.getScaleX() < 1.0);
    }

    /// Verifies that badged boxes overlay badges on content.
    @Test
    void badgedBoxOwnsContentAndBadge() {
        Label content = new Label("Inbox");
        M3Badge badge = new M3Badge("3");
        M3BadgedBox badgedBox = new M3BadgedBox(content, badge);
        badgedBox.setBadgeAlignment(Pos.TOP_LEFT);
        badgedBox.setBadgeOffsetX(3.0);
        badgedBox.setBadgeOffsetY(-2.0);

        applyCss(badgedBox);

        assertEquals(content, badgedBox.getContent());
        assertEquals(badge, badgedBox.getBadge());
        assertInstanceOf(M3BadgedBoxSkin.class, badgedBox.getSkin());
        assertEquals(Pos.TOP_LEFT, badgedBox.getBadgeAlignment());
        assertEquals(3.0, badgedBox.getBadgeOffsetX(), 0.0001);
        assertEquals(-2.0, badgedBox.getBadgeOffsetY(), 0.0001);
        assertTrue(content.getParent() != null);
        assertTrue(badge.getParent() != null);
        assertEquals(3.0, badge.getTranslateX(), 0.0001);
        assertEquals(-2.0, badge.getTranslateY(), 0.0001);

        badgedBox.setBadge(null);

        assertNull(badgedBox.getBadge());
        assertNull(badge.getParent());
        assertTrue(content.getParent() != null);

        M3Badge replacement = new M3Badge();
        badgedBox.setBadge(replacement);

        assertTrue(replacement.getParent() != null);
        assertEquals(3.0, replacement.getTranslateX(), 0.0001);
        assertEquals(-2.0, replacement.getTranslateY(), 0.0001);
    }

    /// Verifies that the default badge alignment maps to logical end in right-to-left layouts.
    @Test
    void badgedBoxMirrorsDefaultBadgeAlignmentForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3Avatar content = new M3Avatar("M");
            M3Badge badge = new M3Badge("9");
            M3BadgedBox badgedBox = new M3BadgedBox(content, badge);

            Pane root = new Pane(badgedBox);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 140.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(140.0, 120.0);
            badgedBox.resizeRelocate(40.0, 36.0, 48.0, 48.0);
            root.layout();
            badgedBox.layout();

            assertEquals(NodeOrientation.LEFT_TO_RIGHT, badgedBox.getEffectiveNodeOrientation());
            assertBadgedBoxBadgeAnchoredToLogicalEnd(badgedBox, false);

            badgedBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            badgedBox.layout();

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, badgedBox.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, assertInstanceOf(
                    Node.class,
                    badge.getParent()
            ).getEffectiveNodeOrientation());
            assertEquals(Pos.TOP_RIGHT, badgedBox.getBadgeAlignment());

            assertBadgedBoxBadgeAnchoredToLogicalEnd(badgedBox, true);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, badgedBox, Color.WHITE, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-badged-box-rtl.png"
            ));
            assertRenderedTextNodesStayInsideLayout(root);
        });
    }

    /// Verifies the physical left and right gaps around a rendered horizontal divider line.
    private static void assertHorizontalDividerPhysicalInsets(
            M3Divider divider,
            Region line,
            double expectedLeft,
            double expectedRight
    ) {
        Bounds dividerBounds = divider.localToScene(divider.getBoundsInLocal());
        Bounds lineBounds = line.localToScene(line.getBoundsInLocal());
        assertEquals(expectedLeft, lineBounds.getMinX() - dividerBounds.getMinX(), 1.0,
                () -> "dividerBounds=" + dividerBounds + ", lineBounds=" + lineBounds);
        assertEquals(expectedRight, dividerBounds.getMaxX() - lineBounds.getMaxX(), 1.0,
                () -> "dividerBounds=" + dividerBounds + ", lineBounds=" + lineBounds);
    }

    /// Verifies that a rendered child stays inside a rendered parent.
    private static void assertNodeInsideParent(Node parent, Node child) {
        Bounds parentBounds = parent.localToScene(parent.getBoundsInLocal());
        Bounds childBounds = child.localToScene(child.getBoundsInLocal());
        assertTrue(
                childBounds.getMinX() >= parentBounds.getMinX() - 0.0001
                        && childBounds.getMaxX() <= parentBounds.getMaxX() + 0.0001
                        && childBounds.getMinY() >= parentBounds.getMinY() - 0.0001
                        && childBounds.getMaxY() <= parentBounds.getMaxY() + 0.0001,
                () -> "childBounds=" + childBounds + ", parentBounds=" + parentBounds
        );
    }

    /// Verifies that a node is an icon button carrying the expected internal icon glyph.
    private static void assertInternalIconButton(
            @Nullable Node node,
            M3InternalIcon.Glyph expectedGlyph,
            String description
    ) {
        M3IconButton button = assertInstanceOf(
                M3IconButton.class,
                Objects.requireNonNull(node, description),
                description
        );
        assertInternalIcon(button.getGraphic(), expectedGlyph, description);
        assertTrue(button.getText().isEmpty(), description);
    }
}
