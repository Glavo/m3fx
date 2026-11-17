// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3BannerSkin;
import org.glavo.m3fx.skins.M3BottomAppBarSkin;
import org.glavo.m3fx.skins.M3TopAppBarSkin;
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

/// Verifies app bar actions, scrolling, geometry, and adaptive presentation.
@NotNullByDefault
final class M3AppBarContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that top app bars expose navigation, title, subtitle, custom title, and action slots.
    @Test
    void topAppBarExposesSlots() {
        Label navigation = new Label("Menu");
        Label search = new Label("Search");
        Label more = new Label("More");
        M3TopAppBar topAppBar = topAppBar(
                "Inbox",
                M3TopAppBarVariant.CENTER_ALIGNED,
                navigation,
                search,
                more
        );

        assertEquals("Inbox", topAppBar.getTitle());
        assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, topAppBar.getVariant());
        assertEquals(navigation, topAppBar.getNavigation());
        assertTrue(topAppBar.getActions().contains(search));
        assertTrue(topAppBar.getActions().contains(more));

        Label customTitle = new Label("Workspace logo");
        topAppBar.setSubtitle("3 unread messages");
        topAppBar.setTitleContent(customTitle);

        assertEquals("3 unread messages", topAppBar.getSubtitle());
        assertSame(customTitle, topAppBar.getTitleContent());
        assertEquals("Inbox, 3 unread messages", topAppBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertTrue(navigation.getPseudoClassStates().contains(PseudoClass.getPseudoClass("top-app-bar-leading")));
        assertTrue(search.getPseudoClassStates().contains(PseudoClass.getPseudoClass("top-app-bar-trailing")));

        topAppBar.getActions().clear();
        topAppBar.getActions().add(search);
        topAppBar.getActions().setAll(more);

        assertEquals(java.util.List.of(more), topAppBar.getActions());

        topAppBar.setTitle("Archive");
        topAppBar.setSubtitle("");
        topAppBar.setTitleContent(null);
        topAppBar.setNavigation(null);

        assertEquals("Archive", topAppBar.getTitle());
        assertEquals("Archive", topAppBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertNull(topAppBar.getNavigation());
        assertNull(topAppBar.getTitleContent());
        assertFalse(navigation.getPseudoClassStates().contains(PseudoClass.getPseudoClass("top-app-bar-leading")));
        assertFalse(search.getPseudoClassStates().contains(PseudoClass.getPseudoClass("top-app-bar-trailing")));
        assertTrue(more.getPseudoClassStates().contains(PseudoClass.getPseudoClass("top-app-bar-trailing")));
    }

    /// Verifies that top app bar variants update style classes and layout state.
    @Test
    void topAppBarVariantsUpdateStyleClassesAndLayout() {
        M3TopAppBar topAppBar = topAppBar("Inbox");

        assertEquals(M3TopAppBarVariant.SMALL, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.styleClass()));

        topAppBar.setVariant(M3TopAppBarVariant.CENTER_ALIGNED);

        assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.CENTER_ALIGNED.styleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.styleClass()));
        assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);

        assertEquals(M3TopAppBarVariant.MEDIUM, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM.styleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.CENTER_ALIGNED.styleClass()));
        assertEquals(112.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.LARGE);

        assertEquals(M3TopAppBarVariant.LARGE, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.LARGE.styleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM.styleClass()));
        assertEquals(152.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM_FLEXIBLE);
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM_FLEXIBLE.styleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.LARGE.styleClass()));
        assertEquals(112.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setSubtitle("Supporting context");
        assertEquals(136.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setCollapseProgress(0.5);
        assertEquals(100.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setCollapseProgress(0.0);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE_FLEXIBLE);
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.LARGE_FLEXIBLE.styleClass()));
        assertFalse(topAppBar.getStyleClass().contains(M3TopAppBarVariant.MEDIUM_FLEXIBLE.styleClass()));
        assertEquals(152.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setSubtitle("");
        assertEquals(120.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setCollapseProgress(1.0);
        assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.variantProperty().set(null);

        assertEquals(M3TopAppBarVariant.SMALL, topAppBar.getVariant());
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.styleClass()));
        assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that top app bar token rules apply container metrics.
    @Test
    void topAppBarAppliesMetrics() {
        M3TopAppBar topAppBar = topAppBar("Inbox");
        Pane root = new Pane(topAppBar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);
        assertEquals(64.0, topAppBar.getContainerHeight(), 0.0001);
        assertEquals(112.0, topAppBar.getMediumContainerHeight(), 0.0001);
        assertEquals(152.0, topAppBar.getLargeContainerHeight(), 0.0001);
        assertEquals(112.0, topAppBar.getMediumFlexibleContainerHeight(), 0.0001);
        assertEquals(136.0, topAppBar.getMediumFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(120.0, topAppBar.getLargeFlexibleContainerHeight(), 0.0001);
        assertEquals(152.0, topAppBar.getLargeFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(4.0, topAppBar.getEdgePadding(), 0.0001);
        assertEquals(16.0, topAppBar.getHorizontalPadding(), 0.0001);
        assertEquals(20.0, topAppBar.getMediumBottomPadding(), 0.0001);
        assertEquals(28.0, topAppBar.getLargeBottomPadding(), 0.0001);
        assertEquals(12.0, topAppBar.getFlexibleBottomPadding(), 0.0001);
        assertEquals(0.0, topAppBar.getContentSpacing(), 0.0001);
        assertEquals(0.0, topAppBar.getActionSpacing(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        assertInstanceOf(M3TopAppBarSkin.class, topAppBar.getSkin());
        assertInstanceOf(Label.class, topAppBar.lookup("." + "m3-top-app-bar-title"));
        HBox actions = assertInstanceOf(HBox.class, topAppBar.lookup("." + "m3-top-app-bar-actions"));
        assertEquals(0.0, actions.getSpacing(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(112.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(152.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.SMALL);
        root.applyCss();

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);
        assertEquals(64.0, topAppBar.getContainerHeight(), 0.0001);
        assertEquals(112.0, topAppBar.getMediumContainerHeight(), 0.0001);
        assertEquals(152.0, topAppBar.getLargeContainerHeight(), 0.0001);
        assertEquals(112.0, topAppBar.getMediumFlexibleContainerHeight(), 0.0001);
        assertEquals(136.0, topAppBar.getMediumFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(120.0, topAppBar.getLargeFlexibleContainerHeight(), 0.0001);
        assertEquals(152.0, topAppBar.getLargeFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(4.0, topAppBar.getEdgePadding(), 0.0001);
        assertEquals(16.0, topAppBar.getHorizontalPadding(), 0.0001);
        assertEquals(20.0, topAppBar.getMediumBottomPadding(), 0.0001);
        assertEquals(28.0, topAppBar.getLargeBottomPadding(), 0.0001);
        assertEquals(12.0, topAppBar.getFlexibleBottomPadding(), 0.0001);
        assertEquals(0.0, topAppBar.getContentSpacing(), 0.0001);
        assertEquals(0.0, topAppBar.getActionSpacing(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        assertEquals(0.0, actions.getSpacing(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(112.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(152.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setActionSpacing(18.0);
        assertEquals(18.0, topAppBar.getActionSpacing(), 0.0001);
        assertEquals(18.0, actions.getSpacing(), 0.0001);
    }

    /// Verifies that app bar metric tokens are exposed through CSS metadata.
    @Test
    void appBarMetricTokensAreExposedThroughCssMetadata() {
        List<CssMetaData<? extends Styleable, ?>> topAppBarCssMetaData = M3TopAppBar.getClassCssMetaData();
        assertCssProperty(topAppBarCssMetaData, "-m3-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-medium-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-large-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-medium-flexible-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-medium-flexible-subtitle-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-large-flexible-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-large-flexible-subtitle-container-height");
        assertCssProperty(topAppBarCssMetaData, "-m3-edge-padding");
        assertCssProperty(topAppBarCssMetaData, "-m3-horizontal-padding");
        assertCssProperty(topAppBarCssMetaData, "-m3-medium-bottom-padding");
        assertCssProperty(topAppBarCssMetaData, "-m3-large-bottom-padding");
        assertCssProperty(topAppBarCssMetaData, "-m3-flexible-bottom-padding");
        assertCssProperty(topAppBarCssMetaData, "-m3-content-spacing");
        assertCssProperty(topAppBarCssMetaData, "-m3-action-spacing");

        List<CssMetaData<? extends Styleable, ?>> bottomAppBarCssMetaData = M3BottomAppBar.getClassCssMetaData();
        assertCssProperty(bottomAppBarCssMetaData, "-m3-container-height");
        assertCssProperty(bottomAppBarCssMetaData, "-m3-horizontal-padding");
        assertCssProperty(bottomAppBarCssMetaData, "-m3-content-spacing");
        assertCssProperty(bottomAppBarCssMetaData, "-m3-action-spacing");
    }

    /// Verifies that app bar metric tokens are directly styleable from control CSS.
    @Test
    void appBarMetricTokensAreStyleable() {
        M3TopAppBar topAppBar = topAppBar("Inbox");
        topAppBar.setStyle(
                "-m3-container-height: 70px; "
                        + "-m3-medium-container-height: 118px; "
                        + "-m3-large-container-height: 158px; "
                        + "-m3-medium-flexible-container-height: 124px; "
                        + "-m3-medium-flexible-subtitle-container-height: 144px; "
                        + "-m3-large-flexible-container-height: 132px; "
                        + "-m3-large-flexible-subtitle-container-height: 164px; "
                        + "-m3-edge-padding: 6px; "
                        + "-m3-horizontal-padding: 22px; "
                        + "-m3-medium-bottom-padding: 24px; "
                        + "-m3-large-bottom-padding: 32px; "
                        + "-m3-flexible-bottom-padding: 10px; "
                        + "-m3-content-spacing: 18px; "
                        + "-m3-action-spacing: 11px;"
        );

        M3BottomAppBar bottomAppBar = bottomAppBar();
        bottomAppBar.setStyle(
                "-m3-container-height: 86px; "
                        + "-m3-horizontal-padding: 20px; "
                        + "-m3-content-spacing: 14px; "
                        + "-m3-action-spacing: 9px;"
        );

        Pane root = new Pane(topAppBar, bottomAppBar);
        new Scene(root);
        root.applyCss();

        assertEquals(70.0, topAppBar.prefHeight(-1.0), 0.0001);
        assertEquals(70.0, topAppBar.getContainerHeight(), 0.0001);
        assertEquals(118.0, topAppBar.getMediumContainerHeight(), 0.0001);
        assertEquals(158.0, topAppBar.getLargeContainerHeight(), 0.0001);
        assertEquals(124.0, topAppBar.getMediumFlexibleContainerHeight(), 0.0001);
        assertEquals(144.0, topAppBar.getMediumFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(132.0, topAppBar.getLargeFlexibleContainerHeight(), 0.0001);
        assertEquals(164.0, topAppBar.getLargeFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(6.0, topAppBar.getEdgePadding(), 0.0001);
        assertEquals(22.0, topAppBar.getHorizontalPadding(), 0.0001);
        assertEquals(24.0, topAppBar.getMediumBottomPadding(), 0.0001);
        assertEquals(32.0, topAppBar.getLargeBottomPadding(), 0.0001);
        assertEquals(10.0, topAppBar.getFlexibleBottomPadding(), 0.0001);
        assertEquals(18.0, topAppBar.getContentSpacing(), 0.0001);
        assertEquals(11.0, topAppBar.getActionSpacing(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        assertEquals(11.0, assertInstanceOf(
                HBox.class,
                topAppBar.lookup("." + "m3-top-app-bar-actions")
        ).getSpacing(), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(118.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(158.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM_FLEXIBLE);
        root.applyCss();
        assertEquals(124.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setSubtitle("Subtitle");
        assertEquals(144.0, topAppBar.prefHeight(-1.0), 0.0001);

        topAppBar.setVariant(M3TopAppBarVariant.LARGE_FLEXIBLE);
        root.applyCss();
        assertEquals(164.0, topAppBar.prefHeight(-1.0), 0.0001);
        topAppBar.setSubtitle("");
        assertEquals(132.0, topAppBar.prefHeight(-1.0), 0.0001);

        assertEquals(86.0, bottomAppBar.prefHeight(-1.0), 0.0001);
        assertEquals(86.0, bottomAppBar.getContainerHeight(), 0.0001);
        assertEquals(20.0, bottomAppBar.getHorizontalPadding(), 0.0001);
        assertEquals(14.0, bottomAppBar.getContentSpacing(), 0.0001);
        assertEquals(9.0, bottomAppBar.getActionSpacing(), 0.0001);
        assertEquals(20.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(9.0, assertInstanceOf(
                HBox.class,
                bottomAppBar.lookup("." + "m3-bottom-app-bar-actions")
        ).getSpacing(), 0.0001);
    }

    /// Verifies that top app bar keyboard traversal follows direction and skips unreachable action slots.
    @Test
    void topAppBarKeyboardTraversalFollowsDirectionAndReachability() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button navigation = new M3Button("Menu");
            M3Button hidden = new M3Button("Hidden");
            M3Button disabled = new M3Button("Disabled");
            M3Button search = new M3Button("Search");
            M3Button more = new M3Button("More");
            M3Button outside = new M3Button("Outside");
            M3TopAppBar topAppBar = topAppBar("Inbox", navigation, hidden, disabled, search, more);
            VBox root = new VBox(8.0, topAppBar, outside);
            Stage stage = new Stage();

            hidden.setVisible(false);
            disabled.setDisable(true);
            try {
                stage.setScene(new Scene(root, 560.0, 180.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(navigation.isFocused());

                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(search.isFocused());
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(more.isFocused());
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(navigation.isFocused());
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(more.isFocused());

                topAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                navigation.requestFocus();

                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(more.isFocused());
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(navigation.isFocused());

                outside.requestFocus();
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                assertTrue(more.isFocused());
                topAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                assertTrue(navigation.isFocused());
                assertSame(navigation, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertFalse(hidden.isFocused());
                assertFalse(disabled.isFocused());

                M3TextField editor = createTextField("Search", M3TextInputVariant.OUTLINED);
                M3Button afterEditor = new M3Button("After editor");
                M3TopAppBar editorAppBar = topAppBar("Editable");
                editorAppBar.getActions().addAll(editor, afterEditor);
                root.getChildren().add(editorAppBar);
                assertContainerNavigationDoesNotStealTextInputFocus(root, editorAppBar, editor, afterEditor, KeyCode.RIGHT);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that bottom app bar keyboard traversal includes floating actions and mirrors in RTL layouts.
    @Test
    void bottomAppBarKeyboardTraversalIncludesFloatingActionAndMirrorsDirection() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button archive = new M3Button("Archive");
            M3Button hidden = new M3Button("Hidden");
            M3Button disabled = new M3Button("Disabled");
            M3Button share = new M3Button("Share");
            M3Button create = new M3Button("Create");
            M3Button outside = new M3Button("Outside");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    create,
                    archive,
                    hidden,
                    disabled,
                    share
            );
            VBox root = new VBox(8.0, bottomAppBar, outside);
            Stage stage = new Stage();

            hidden.setVisible(false);
            disabled.setDisable(true);
            try {
                stage.setScene(new Scene(root, 560.0, 200.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(archive.isFocused());

                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(share.isFocused());
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(create.isFocused());
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(archive.isFocused());
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(create.isFocused());

                bottomAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                archive.requestFocus();

                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(create.isFocused());
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(archive.isFocused());

                outside.requestFocus();
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                assertTrue(create.isFocused());
                bottomAppBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                assertTrue(archive.isFocused());
                assertSame(archive, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertFalse(hidden.isFocused());
                assertFalse(disabled.isFocused());

                M3TextField editor = createTextField("Quick action", M3TextInputVariant.OUTLINED);
                M3Button afterEditor = new M3Button("After editor");
                M3BottomAppBar editorAppBar = bottomAppBar(editor, afterEditor);
                root.getChildren().add(editorAppBar);
                assertContainerNavigationDoesNotStealTextInputFocus(root, editorAppBar, editor, afterEditor, KeyCode.RIGHT);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that app bars can reveal popup-backed action targets and keep accessibility focus synchronized.
    @Test
    void appBarsRevealNestedMenuPopupTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem topOpen = new M3MenuItem("Open");
            M3MenuItem topSave = new M3MenuItem("Save");
            M3MenuButton topMenuButton = new M3MenuButton("Top menu", topOpen, topSave);
            topMenuButton.setAnimationsEnabled(false);
            M3Button topNavigation = new M3Button("Menu");
            M3TopAppBar topAppBar = topAppBar("Inbox", topNavigation, topMenuButton);

            M3MenuItem bottomArchive = new M3MenuItem("Archive");
            M3MenuItem bottomDelete = new M3MenuItem("Delete");
            M3MenuButton bottomMenuButton = new M3MenuButton("Bottom menu", bottomArchive, bottomDelete);
            bottomMenuButton.setAnimationsEnabled(false);
            M3Button floatingAction = new M3Button("Create");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomMenuButton
            );

            M3Button outside = new M3Button("Outside");
            VBox root = new VBox(8.0, topAppBar, bottomAppBar, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 620.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertFalse(topMenuButton.isShowing());
                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, topSave);

                assertTrue(topMenuButton.isShowing());
                assertTrue(topSave.isFocused());
                assertSame(topSave, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                topAppBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(topSave.isFocused());
                assertSame(topSave, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                topMenuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(topMenuButton.isShowing());
                assertTrue(topMenuButton.isFocused());
                assertSame(topMenuButton, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertFalse(bottomMenuButton.isShowing());
                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomDelete);

                assertTrue(bottomMenuButton.isShowing());
                assertTrue(bottomDelete.isFocused());
                assertSame(bottomDelete, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomAppBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(bottomDelete.isFocused());
                assertSame(bottomDelete, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomMenuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(bottomMenuButton.isShowing());
                assertTrue(bottomMenuButton.isFocused());
                assertSame(bottomMenuButton, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                outside.requestFocus();
                topAppBar.setVisible(false);
                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, topOpen);

                assertFalse(topMenuButton.isShowing());
                assertTrue(outside.isFocused());

                bottomAppBar.setVisible(false);
                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomArchive);

                assertFalse(bottomMenuButton.isShowing());
                assertTrue(outside.isFocused());
            } finally {
                topMenuButton.setAnimationsEnabled(true);
                bottomMenuButton.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that top app bars expose the Material scroll-under visual state through a pseudo-class.
    @Test
    void topAppBarScrolledUnderStateUsesPseudoClassAndCss() {
        M3TopAppBar topAppBar = topAppBar("Inbox");
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM_FLEXIBLE);
        PseudoClass scrolledUnder = PseudoClass.getPseudoClass("scrolled-under");
        PseudoClass collapsed = PseudoClass.getPseudoClass("collapsed");

        StackPane root = new StackPane(topAppBar);
        Scene scene = new Scene(root, 360.0, 120.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.layout();

        assertFalse(topAppBar.isScrolledUnder());
        assertFalse(topAppBar.getPseudoClassStates().contains(scrolledUnder));
        assertFalse(topAppBar.getPseudoClassStates().contains(collapsed));
        assertEquals(0.0, topAppBar.getCollapseProgress(), 0.0001);
        assertEquals(112.0, topAppBar.prefHeight(-1.0), 0.0001);
        assertNull(topAppBar.getEffect());

        topAppBar.setScrolledUnder(true);
        root.applyCss();
        root.layout();

        assertTrue(topAppBar.isScrolledUnder());
        assertTrue(topAppBar.getPseudoClassStates().contains(scrolledUnder));
        assertTrue(topAppBar.getPseudoClassStates().contains(collapsed));
        assertEquals(1.0, topAppBar.getCollapseProgress(), 0.0001);
        assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);
        assertDropShadow(topAppBar);

        topAppBar.setScrolledUnder(false);
        root.applyCss();
        root.layout();

        assertFalse(topAppBar.getPseudoClassStates().contains(scrolledUnder));
        assertFalse(topAppBar.getPseudoClassStates().contains(collapsed));
        assertEquals(0.0, topAppBar.getCollapseProgress(), 0.0001);
        assertEquals(112.0, topAppBar.prefHeight(-1.0), 0.0001);
        assertNull(topAppBar.getEffect());
    }

    /// Verifies the observable intermediate and settled geometry of a flexible app bar in a real window.
    @Tier2Test
    @Test
    void flexibleTopAppBarCollapseAnimationPreservesMaterialGeometry() throws InterruptedException {
        double geometryTolerance = 1.0;
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3TopAppBar> appBarReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        @Nullable Parent root = rootReference.get();
                        if (appBar == null || root == null) {
                            return false;
                        }
                        root.applyCss();
                        root.layout();
                        double progress = appBar.getCollapseProgress();
                        return progress > 0.30 && progress < 0.55;
                    },
                    () -> {
                        M3TopAppBar appBar = createGeometryTopAppBar(
                                "Collections",
                                M3TopAppBarVariant.LARGE_FLEXIBLE
                        );
                        appBar.setSubtitle("Recently updated");
                        VBox root = new VBox(appBar, new Region());
                        VBox.setVgrow(root.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
                        root.setStyle("-fx-background-color: white; " + visualTestColors());
                        Scene scene = new Scene(root, 680.0, 360.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();

                        assertEquals(152.0, appBar.getHeight(), geometryTolerance);
                        stageReference.set(stage);
                        rootReference.set(root);
                        appBarReference.set(appBar);
                        appBar.setScrolledUnder(true);
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        double progress = appBar.getCollapseProgress();
                        double expectedHeight = 152.0 + (64.0 - 152.0) * progress;
                        Label expandedTitle = assertInstanceOf(
                                Label.class,
                                appBar.lookup("." + "m3-top-app-bar-title")
                        );
                        Label compactTitle = assertInstanceOf(
                                Label.class,
                                appBar.lookup("." + "m3-top-app-bar-compact-title")
                        );

                        assertEquals(expectedHeight, appBar.prefHeight(-1.0), geometryTolerance);
                        assertEquals(expectedHeight, appBar.getHeight(), geometryTolerance);
                        assertEquals(1.0 - progress, expandedTitle.getOpacity(), 0.02);
                        assertEquals(progress, compactTitle.getOpacity(), 0.02);
                        assertTrue(expandedTitle.isVisible());
                        assertTrue(compactTitle.isVisible());
                        assertTopAppBarMaterialSlotGeometry(appBar);

                        WritableImage image = snapshotImageOnFxThread(root);
                        assertSnapshotNodeContainsContrast(image, appBar, Color.WHITE, 0.04);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-top-app-bar-collapse-intermediate.png"
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        return appBar != null && Math.abs(appBar.getCollapseProgress() - 1.0) < 0.0001;
                    },
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        root.applyCss();
                        root.layout();

                        assertEquals(64.0, appBar.prefHeight(-1.0), geometryTolerance);
                        assertEquals(64.0, appBar.getHeight(), geometryTolerance);
                        assertTrue(appBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("collapsed")));
                        assertTopAppBarMaterialSlotGeometry(appBar);
                        appBar.setScrolledUnder(false);
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        if (appBar == null) {
                            return false;
                        }
                        double progress = appBar.getCollapseProgress();
                        return progress > 0.30 && progress < 0.55;
                    },
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        root.applyCss();
                        root.layout();

                        double progress = appBar.getCollapseProgress();
                        double expectedHeight = 152.0 + (64.0 - 152.0) * progress;
                        assertEquals(expectedHeight, appBar.getHeight(), geometryTolerance);
                        assertTopAppBarMaterialSlotGeometry(appBar);
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        return appBar != null && Math.abs(appBar.getCollapseProgress()) < 0.0001;
                    },
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        root.applyCss();
                        root.layout();

                        assertEquals(152.0, appBar.prefHeight(-1.0), geometryTolerance);
                        assertEquals(152.0, appBar.getHeight(), geometryTolerance);
                        assertFalse(appBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("collapsed")));
                        assertTopAppBarMaterialSlotGeometry(appBar);
                        appBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                        appBar.setScrolledUnder(true);
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        if (appBar == null) {
                            return false;
                        }
                        double progress = appBar.getCollapseProgress();
                        return progress > 0.30 && progress < 0.55;
                    },
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        root.applyCss();
                        root.layout();

                        assertEquals(NodeOrientation.RIGHT_TO_LEFT, appBar.getEffectiveNodeOrientation());
                        assertTopAppBarMaterialSlotGeometry(appBar);
                        WritableImage image = snapshotImageOnFxThread(root);
                        assertSnapshotNodeContainsContrast(image, appBar, Color.WHITE, 0.04);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-top-app-bar-collapse-intermediate-rtl.png"
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        return appBar != null && Math.abs(appBar.getCollapseProgress() - 1.0) < 0.0001;
                    },
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        root.applyCss();
                        root.layout();

                        assertEquals(64.0, appBar.getHeight(), geometryTolerance);
                        assertTopAppBarMaterialSlotGeometry(appBar);
                        appBar.setScrolledUnder(false);
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3TopAppBar appBar = appBarReference.get();
                        return appBar != null && Math.abs(appBar.getCollapseProgress()) < 0.0001;
                    },
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TopAppBar appBar = Objects.requireNonNull(appBarReference.get(), "appBar");
                        root.applyCss();
                        root.layout();

                        assertEquals(152.0, appBar.getHeight(), geometryTolerance);
                        assertTopAppBarMaterialSlotGeometry(appBar);
                    }
            );
        } finally {
            closeObservableMotionWindow(stageReference, rootReference);
        }
    }

    /// Verifies that top app bar variants apply distinct Material title type scale roles.
    @Test
    void topAppBarVariantsApplyTitleTypeScale() {
        M3TopAppBar small = topAppBar("Inbox");
        M3TopAppBar centerAligned = topAppBar("Calendar");
        centerAligned.setVariant(M3TopAppBarVariant.CENTER_ALIGNED);
        M3TopAppBar medium = topAppBar("Project");
        medium.setVariant(M3TopAppBarVariant.MEDIUM);
        M3TopAppBar large = topAppBar("Workspace");
        large.setVariant(M3TopAppBarVariant.LARGE);
        M3TopAppBar mediumFlexible = topAppBar("Library");
        mediumFlexible.setVariant(M3TopAppBarVariant.MEDIUM_FLEXIBLE);
        mediumFlexible.setSubtitle("12 collections");
        M3TopAppBar largeFlexible = topAppBar("Discover");
        largeFlexible.setVariant(M3TopAppBarVariant.LARGE_FLEXIBLE);
        largeFlexible.setSubtitle("Recommended for you");

        VBox root = new VBox(small, centerAligned, medium, large, mediumFlexible, largeFlexible);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertTopAppBarTitleFontSize(small, 22.0);
        assertTopAppBarTitleFontSize(centerAligned, 22.0);
        assertTopAppBarTitleFontSize(medium, 24.0);
        assertTopAppBarTitleFontSize(large, 28.0);
        assertTopAppBarTitleFontSize(mediumFlexible, 28.0);
        assertTopAppBarTitleFontSize(largeFlexible, 36.0);
        assertFalse(assertInstanceOf(Label.class, small.lookup("." + "m3-top-app-bar-title")).isWrapText());
        assertFalse(assertInstanceOf(Label.class, centerAligned.lookup("." + "m3-top-app-bar-title")).isWrapText());
        assertFalse(assertInstanceOf(Label.class, medium.lookup("." + "m3-top-app-bar-title")).isWrapText());
        assertFalse(assertInstanceOf(Label.class, large.lookup("." + "m3-top-app-bar-title")).isWrapText());
        assertTrue(assertInstanceOf(Label.class, mediumFlexible.lookup("." + "m3-top-app-bar-title")).isWrapText());
        assertTrue(assertInstanceOf(Label.class, largeFlexible.lookup("." + "m3-top-app-bar-title")).isWrapText());
        assertEquals(14.0, assertInstanceOf(
                Label.class,
                mediumFlexible.lookup("." + "m3-top-app-bar-subtitle")
        ).getFont().getSize(), 0.0001);
        assertEquals(16.0, assertInstanceOf(
                Label.class,
                largeFlexible.lookup("." + "m3-top-app-bar-subtitle")
        ).getFont().getSize(), 0.0001);
    }

    /// Verifies that all top app bar variants use Material row, title, and action slot geometry.
    @Test
    void topAppBarVariantsLayOutMaterialSlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3TopAppBar small = createGeometryTopAppBar("Inbox", M3TopAppBarVariant.SMALL);
            M3TopAppBar centerAligned = createGeometryTopAppBar("Calendar", M3TopAppBarVariant.CENTER_ALIGNED);
            M3TopAppBar medium = createGeometryTopAppBar("Project", M3TopAppBarVariant.MEDIUM);
            M3TopAppBar large = createGeometryTopAppBar("Workspace", M3TopAppBarVariant.LARGE);
            M3TopAppBar mediumFlexible = createGeometryTopAppBar("Library", M3TopAppBarVariant.MEDIUM_FLEXIBLE);
            M3TopAppBar mediumFlexibleSubtitle = createGeometryTopAppBar(
                    "Messages",
                    M3TopAppBarVariant.MEDIUM_FLEXIBLE
            );
            mediumFlexibleSubtitle.setSubtitle("4 unread conversations");
            M3TopAppBar largeFlexible = createGeometryTopAppBar("Discover", M3TopAppBarVariant.LARGE_FLEXIBLE);
            M3TopAppBar largeFlexibleSubtitle = createGeometryTopAppBar(
                    "Collections",
                    M3TopAppBarVariant.LARGE_FLEXIBLE
            );
            largeFlexibleSubtitle.setSubtitle("Recently updated");

            List<M3TopAppBar> appBars = List.of(
                    small,
                    centerAligned,
                    medium,
                    large,
                    mediumFlexible,
                    mediumFlexibleSubtitle,
                    largeFlexible,
                    largeFlexibleSubtitle
            );
            VBox root = new VBox(8.0);
            root.getChildren().addAll(appBars);
            root.setFillWidth(true);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 680.0, 1040.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(680.0, 1040.0);
            root.layout();

            for (M3TopAppBar appBar : appBars) {
                assertTopAppBarMaterialSlotGeometry(appBar);
                assertTopAppBarIconButtonTargets(appBar);
            }

            WritableImage image = snapshotImageOnFxThread(root);
            for (M3TopAppBar appBar : appBars) {
                assertSnapshotNodeContainsContrast(image, appBar, Color.WHITE, 0.04);
            }
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-top-app-bar-variants.png"
            ));
        });
    }

    /// Verifies that top app bar variants mirror logical leading and trailing slots in right-to-left layouts.
    @Test
    void topAppBarVariantsLayOutMaterialSlotsInRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3TopAppBar small = createGeometryTopAppBar("Inbox", M3TopAppBarVariant.SMALL);
            M3TopAppBar centerAligned = createGeometryTopAppBar("Calendar", M3TopAppBarVariant.CENTER_ALIGNED);
            M3TopAppBar medium = createGeometryTopAppBar("Project", M3TopAppBarVariant.MEDIUM);
            M3TopAppBar large = createGeometryTopAppBar("Workspace", M3TopAppBarVariant.LARGE);
            M3TopAppBar mediumFlexible = createGeometryTopAppBar("Library", M3TopAppBarVariant.MEDIUM_FLEXIBLE);
            mediumFlexible.setSubtitle("12 collections");
            M3TopAppBar largeFlexible = createGeometryTopAppBar("Discover", M3TopAppBarVariant.LARGE_FLEXIBLE);
            largeFlexible.setSubtitle("Recommended for you");

            List<M3TopAppBar> appBars = List.of(
                    small,
                    centerAligned,
                    medium,
                    large,
                    mediumFlexible,
                    largeFlexible
            );
            VBox root = new VBox(8.0);
            root.getChildren().addAll(appBars);
            root.setFillWidth(true);
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 680.0, 800.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(680.0, 800.0);
            root.layout();

            for (M3TopAppBar appBar : appBars) {
                assertTopAppBarMaterialSlotGeometry(appBar);
                assertTopAppBarIconButtonTargets(appBar);
            }

            writeVisualSnapshot(snapshotImageOnFxThread(root), java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-top-app-bar-variants-rtl.png"
            ));
        });
    }

    /// Verifies that generated top app bar action slots track action-list mutations without stale parents.
    @Test
    void topAppBarActionSlotsTrackActionListMutations() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconButton navigation = createGeometryTopAppBarIcon("menu");
            M3IconButton search = createGeometryTopAppBarIcon("search");
            M3IconButton more = createGeometryTopAppBarIcon("more");
            M3IconButton account = createGeometryTopAppBarIcon("person");
            M3TopAppBar topAppBar = topAppBar("Inbox", navigation, search, more);
            Pane root = new Pane(topAppBar);
            Scene scene = new Scene(root, 360.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            topAppBar.resizeRelocate(0.0, 0.0, 360.0, 64.0);
            root.applyCss();
            root.layout();

            assertEquals(2, topAppBar.lookupAll("." + "m3-top-app-bar-action-slot").size());

            topAppBar.getActions().setAll(more, account);
            root.applyCss();
            root.layout();

            assertEquals(2, topAppBar.lookupAll("." + "m3-top-app-bar-action-slot").size());
            assertNull(search.getParent(), "removed app bar action should not keep an old slot parent");
            assertTrue(more.getParent() instanceof StackPane, "reused app bar action should be reparented into a slot");
            assertTrue(account.getParent() instanceof StackPane, "new app bar action should be parented into a slot");

            topAppBar.getActions().clear();
            root.applyCss();
            root.layout();

            assertEquals(0, topAppBar.lookupAll("." + "m3-top-app-bar-action-slot").size());
            assertNull(more.getParent(), "cleared app bar action should not keep a slot parent");
            assertNull(account.getParent(), "cleared app bar action should not keep a slot parent");
        });
    }

    /// Asserts the rendered font size used by a top app bar title label.
    private static void assertTopAppBarTitleFontSize(M3TopAppBar appBar, double expectedSize) {
        Label title = assertInstanceOf(Label.class, appBar.lookup("." + "m3-top-app-bar-title"));

        assertEquals(expectedSize, title.getFont().getSize(), 0.0001,
                () -> "top app bar title font size mismatch: variant=" + appBar.getVariant());
    }

    /// Creates a top app bar with action targets suitable for deterministic layout assertions.
    private static M3TopAppBar createGeometryTopAppBar(String title, M3TopAppBarVariant variant) {
        M3TopAppBar appBar = topAppBar(
                title,
                variant,
                createGeometryTopAppBarIcon("menu"),
                createGeometryTopAppBarIcon("search"),
                createGeometryTopAppBarIcon("more")
        );
        appBar.setMaxWidth(Double.MAX_VALUE);
        return appBar;
    }

    /// Creates an icon button used only by top app bar geometry tests.
    private static M3IconButton createGeometryTopAppBarIcon(String iconName) {
        return new M3IconButton(visualIcon(iconName));
    }

    /// Asserts Material slot geometry for one rendered top app bar.
    private static void assertTopAppBarMaterialSlotGeometry(M3TopAppBar appBar) {
        StackPane navigation = assertInstanceOf(
                StackPane.class,
                appBar.lookup("." + "m3-top-app-bar-navigation")
        );
        Label title = assertInstanceOf(Label.class, appBar.lookup("." + "m3-top-app-bar-title"));
        Label subtitle = assertInstanceOf(Label.class, appBar.lookup("." + "m3-top-app-bar-subtitle"));
        HBox actions = assertInstanceOf(HBox.class, appBar.lookup("." + "m3-top-app-bar-actions"));
        Text titleText = assertInstanceOf(Text.class, title.lookup(".text"));

        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds navigationBounds = navigation.localToScene(navigation.getLayoutBounds());
        Bounds titleBounds = title.localToScene(title.getBoundsInLocal());
        Bounds titleTextBounds = titleText.localToScene(titleText.getBoundsInLocal());
        Bounds subtitleBounds = subtitle.localToScene(subtitle.getBoundsInLocal());
        Bounds actionsBounds = actions.localToScene(actions.getBoundsInLocal());
        List<Node> actionSlots = appBar.lookupAll("." + "m3-top-app-bar-action-slot").stream()
                .sorted(java.util.Comparator.comparingDouble(node ->
                        node.localToScene(node.getBoundsInLocal()).getMinX()))
                .toList();
        double horizontalPadding = appBar.getHorizontalPadding();
        double edgePadding = appBar.getEdgePadding();
        double rowCenterY = appBarBounds.getMinY() + appBar.getContainerHeight() / 2.0;
        boolean rightToLeft = appBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        assertEquals(expectedTopAppBarHeight(appBar), appBarBounds.getHeight(), 1.0,
                () -> "top app bar rendered height drifted: variant=" + appBar.getVariant()
                        + ", bounds=" + appBarBounds);
        assertEquals(rowCenterY, navigationBounds.getCenterY(), 1.0,
                () -> "top app bar navigation target is not centered in the icon row: variant="
                        + appBar.getVariant() + ", navigation=" + navigationBounds
                        + ", appBar=" + appBarBounds);
        assertEquals(rowCenterY, actionsBounds.getCenterY(), 1.0,
                () -> "top app bar action targets are not centered in the icon row: variant="
                        + appBar.getVariant() + ", actions=" + actionsBounds
                        + ", appBar=" + appBarBounds);
        assertTopAppBarActionSlots(appBar, navigation, actionSlots, rowCenterY);
        if (rightToLeft) {
            assertEquals(appBarBounds.getMaxX() - edgePadding, navigationBounds.getMaxX(), 1.0,
                    () -> "RTL top app bar navigation target ignores leading padding: variant="
                            + appBar.getVariant() + ", navigation=" + navigationBounds
                            + ", appBar=" + appBarBounds);
            assertEquals(appBarBounds.getMinX() + edgePadding, actionsBounds.getMinX(), 1.0,
                    () -> "RTL top app bar action targets ignore trailing padding: variant="
                            + appBar.getVariant() + ", actions=" + actionsBounds
                            + ", appBar=" + appBarBounds);
        } else {
            assertEquals(appBarBounds.getMinX() + edgePadding, navigationBounds.getMinX(), 1.0,
                    () -> "top app bar navigation target ignores leading padding: variant="
                            + appBar.getVariant() + ", navigation=" + navigationBounds
                            + ", appBar=" + appBarBounds);
            assertEquals(appBarBounds.getMaxX() - edgePadding, actionsBounds.getMaxX(), 1.0,
                    () -> "top app bar action targets ignore trailing padding: variant="
                            + appBar.getVariant() + ", actions=" + actionsBounds
                            + ", appBar=" + appBarBounds);
        }

        switch (appBar.getVariant()) {
            case CENTER_ALIGNED -> {
                assertEquals(appBarBounds.getCenterX(), titleTextBounds.getCenterX(), 1.0,
                        () -> "center-aligned top app bar title is not horizontally centered: title="
                                + titleTextBounds + ", appBar=" + appBarBounds);
                assertEquals(rowCenterY, titleTextBounds.getCenterY(), 1.0,
                        () -> "center-aligned top app bar title is not centered in the icon row: title="
                                + titleTextBounds + ", appBar=" + appBarBounds);
            }
            case MEDIUM, LARGE -> {
                double bottomPadding = appBar.getVariant() == M3TopAppBarVariant.MEDIUM
                        ? appBar.getMediumBottomPadding()
                        : appBar.getLargeBottomPadding();
                if (rightToLeft) {
                    assertEquals(appBarBounds.getMaxX() - horizontalPadding, titleTextBounds.getMaxX(), 1.0,
                            () -> "RTL tall top app bar title ignores leading padding: variant="
                                    + appBar.getVariant() + ", titleText=" + titleTextBounds
                                    + ", title=" + titleBounds + ", appBar=" + appBarBounds);
                } else {
                    assertEquals(appBarBounds.getMinX() + horizontalPadding, titleBounds.getMinX(), 1.0,
                            () -> "tall top app bar title ignores leading padding: variant="
                                    + appBar.getVariant() + ", title=" + titleBounds
                                    + ", appBar=" + appBarBounds);
                }
                assertEquals(appBarBounds.getMaxY() - bottomPadding, titleBounds.getMaxY(), 1.0,
                        () -> "tall top app bar title ignores bottom padding: variant="
                                + appBar.getVariant() + ", title=" + titleBounds
                                + ", appBar=" + appBarBounds);
                assertTrue(titleTextBounds.getCenterY() > rowCenterY,
                        () -> "tall top app bar title should sit below the icon row: variant="
                                + appBar.getVariant() + ", title=" + titleTextBounds
                                + ", rowCenterY=" + rowCenterY);
            }
            case MEDIUM_FLEXIBLE, LARGE_FLEXIBLE -> {
                double collapseProgress = appBar.getCollapseProgress();
                if (collapseProgress >= 0.999) {
                    Label compactTitle = assertInstanceOf(
                            Label.class,
                            appBar.lookup("." + "m3-top-app-bar-compact-title")
                    );
                    Label compactSubtitle = assertInstanceOf(
                            Label.class,
                            appBar.lookup("." + "m3-top-app-bar-compact-subtitle")
                    );
                    Text compactTitleText = assertInstanceOf(Text.class, compactTitle.lookup(".text"));
                    Bounds compactTitleBounds = compactTitle.localToScene(compactTitle.getBoundsInLocal());
                    Bounds compactTitleTextBounds = compactTitleText.localToScene(compactTitleText.getBoundsInLocal());
                    Bounds compactSubtitleBounds = compactSubtitle.localToScene(compactSubtitle.getBoundsInLocal());
                    double contentMinY = compactTitleBounds.getMinY();
                    double contentMaxY = appBar.getSubtitle().isEmpty()
                            ? compactTitleBounds.getMaxY()
                            : compactSubtitleBounds.getMaxY();

                    assertEquals(rowCenterY, (contentMinY + contentMaxY) / 2.0, 1.0,
                            () -> "collapsed flexible app bar title content is not centered in the small row: variant="
                                    + appBar.getVariant() + ", title=" + compactTitleBounds
                                    + ", subtitle=" + compactSubtitleBounds + ", appBar=" + appBarBounds);
                    if (rightToLeft) {
                        assertTrue(compactTitleTextBounds.getMaxX() <= navigationBounds.getMinX() + 1.0,
                                () -> "RTL collapsed flexible title overlaps the navigation target: title="
                                        + compactTitleTextBounds + ", navigation=" + navigationBounds);
                    } else {
                        assertTrue(compactTitleBounds.getMinX() >= navigationBounds.getMaxX() - 1.0,
                                () -> "collapsed flexible title overlaps the navigation target: title="
                                        + compactTitleBounds + ", navigation=" + navigationBounds);
                    }
                    break;
                }

                if (collapseProgress > 0.001) {
                    Label compactTitle = assertInstanceOf(
                            Label.class,
                            appBar.lookup("." + "m3-top-app-bar-compact-title")
                    );
                    Label compactSubtitle = assertInstanceOf(
                            Label.class,
                            appBar.lookup("." + "m3-top-app-bar-compact-subtitle")
                    );
                    Bounds compactTitleBounds = compactTitle.localToScene(compactTitle.getBoundsInLocal());
                    Bounds compactSubtitleBounds = compactSubtitle.localToScene(compactSubtitle.getBoundsInLocal());
                    double expandedLeading = rightToLeft
                            ? appBarBounds.getMaxX() - horizontalPadding
                            : appBarBounds.getMinX() + horizontalPadding;
                    double compactLeading = rightToLeft
                            ? navigationBounds.getMinX() - appBar.getContentSpacing()
                            : navigationBounds.getMaxX() + appBar.getContentSpacing();
                    double expectedLeading = expandedLeading
                            + (compactLeading - expandedLeading) * collapseProgress;

                    if (rightToLeft) {
                        assertEquals(expectedLeading, titleBounds.getMaxX(), 1.0,
                                () -> "RTL flexible title does not follow its logical collapse path: title="
                                        + titleBounds + ", progress=" + collapseProgress);
                        assertEquals(titleBounds.getMaxX(), compactTitleBounds.getMaxX(), 1.0,
                                () -> "RTL flexible title representations diverged during collapse: expanded="
                                        + titleBounds + ", compact=" + compactTitleBounds);
                    } else {
                        assertEquals(expectedLeading, titleBounds.getMinX(), 1.0,
                                () -> "flexible title does not follow its logical collapse path: title="
                                        + titleBounds + ", progress=" + collapseProgress);
                        assertEquals(titleBounds.getMinX(), compactTitleBounds.getMinX(), 1.0,
                                () -> "flexible title representations diverged during collapse: expanded="
                                        + titleBounds + ", compact=" + compactTitleBounds);
                    }
                    assertEquals(titleBounds.getCenterY(), compactTitleBounds.getCenterY(), 1.0,
                            () -> "flexible title representations diverged vertically during collapse: expanded="
                                    + titleBounds + ", compact=" + compactTitleBounds);
                    assertTrue(titleBounds.getCenterY() >= rowCenterY - 1.0,
                            () -> "flexible title moved above the compact app-bar row: title="
                                    + titleBounds + ", rowCenterY=" + rowCenterY);
                    if (!appBar.getSubtitle().isEmpty()) {
                        if (rightToLeft) {
                            assertEquals(subtitleBounds.getMaxX(), compactSubtitleBounds.getMaxX(), 1.0,
                                    () -> "RTL flexible subtitle representations diverged during collapse: expanded="
                                            + subtitleBounds + ", compact=" + compactSubtitleBounds);
                        } else {
                            assertEquals(subtitleBounds.getMinX(), compactSubtitleBounds.getMinX(), 1.0,
                                    () -> "flexible subtitle representations diverged during collapse: expanded="
                                            + subtitleBounds + ", compact=" + compactSubtitleBounds);
                        }
                        assertEquals(subtitleBounds.getCenterY(), compactSubtitleBounds.getCenterY(), 1.0,
                                () -> "flexible subtitle representations diverged vertically during collapse: expanded="
                                        + subtitleBounds + ", compact=" + compactSubtitleBounds);
                    }
                    break;
                }

                if (rightToLeft) {
                    assertEquals(appBarBounds.getMaxX() - horizontalPadding, titleTextBounds.getMaxX(), 1.0,
                            () -> "RTL flexible app bar title ignores leading padding: variant="
                                    + appBar.getVariant() + ", titleText=" + titleTextBounds
                                    + ", appBar=" + appBarBounds);
                } else {
                    assertEquals(appBarBounds.getMinX() + horizontalPadding, titleBounds.getMinX(), 1.0,
                            () -> "flexible app bar title ignores leading padding: variant="
                                    + appBar.getVariant() + ", title=" + titleBounds
                                    + ", appBar=" + appBarBounds);
                }
                Bounds bottomContentBounds = appBar.getSubtitle().isEmpty() ? titleBounds : subtitleBounds;
                assertEquals(
                        appBarBounds.getMaxY() - appBar.getFlexibleBottomPadding(),
                        bottomContentBounds.getMaxY(),
                        1.0,
                        () -> "flexible app bar content ignores bottom padding: variant="
                                + appBar.getVariant() + ", content=" + bottomContentBounds
                                + ", appBar=" + appBarBounds
                );
                assertTrue(titleTextBounds.getCenterY() > rowCenterY,
                        () -> "expanded flexible title should sit below the icon row: variant="
                                + appBar.getVariant() + ", title=" + titleTextBounds
                                + ", rowCenterY=" + rowCenterY);
            }
            case SMALL -> {
                assertEquals(rowCenterY, titleTextBounds.getCenterY(), 1.0,
                        () -> "small top app bar title is not centered in the icon row: title="
                                + titleTextBounds + ", appBar=" + appBarBounds);
                if (rightToLeft) {
                    assertTrue(titleTextBounds.getMaxX() <= navigationBounds.getMinX() + 1.0,
                            () -> "RTL small top app bar title overlaps the navigation target: title="
                                    + titleTextBounds + ", navigation=" + navigationBounds);
                } else {
                    assertTrue(titleBounds.getMinX() >= navigationBounds.getMaxX() - 1.0,
                            () -> "small top app bar title overlaps the navigation target: title="
                                    + titleBounds + ", navigation=" + navigationBounds);
                }
            }
        }
    }

    /// Asserts that top app bar navigation and trailing actions are hosted in stable 48 dp Material slots.
    private static void assertTopAppBarActionSlots(
            M3TopAppBar appBar,
            StackPane navigation,
            List<Node> actionSlots,
            double rowCenterY
    ) {
        Bounds navigationBounds = navigation.localToScene(navigation.getLayoutBounds());
        assertEquals(48.0, navigationBounds.getWidth(), 1.0,
                () -> "top app bar navigation slot width is not 48 dp: variant="
                        + appBar.getVariant() + ", navigation=" + navigationBounds);
        assertEquals(48.0, navigationBounds.getHeight(), 1.0,
                () -> "top app bar navigation slot height is not 48 dp: variant="
                        + appBar.getVariant() + ", navigation=" + navigationBounds);

        assertEquals(appBar.getActions().size(), actionSlots.size(),
                () -> "top app bar action slot count does not match action nodes: variant="
                        + appBar.getVariant());
        @Nullable Bounds previousBounds = null;
        for (Node slot : actionSlots) {
            Bounds slotBounds = slot.localToScene(slot.getLayoutBounds());
            assertEquals(48.0, slotBounds.getWidth(), 1.0,
                    () -> "top app bar action slot width is not 48 dp: variant="
                            + appBar.getVariant() + ", slot=" + slotBounds);
            assertEquals(48.0, slotBounds.getHeight(), 1.0,
                    () -> "top app bar action slot height is not 48 dp: variant="
                            + appBar.getVariant() + ", slot=" + slotBounds);
            assertEquals(rowCenterY, slotBounds.getCenterY(), 1.0,
                    () -> "top app bar action slot is not centered in the icon row: variant="
                            + appBar.getVariant() + ", slot=" + slotBounds);
            if (previousBounds != null) {
                Bounds previousSlotBounds = previousBounds;
                assertEquals(appBar.getActionSpacing(), slotBounds.getMinX() - previousSlotBounds.getMaxX(), 1.0,
                        () -> "top app bar action slots do not use action spacing: variant="
                                + appBar.getVariant() + ", previous=" + previousSlotBounds + ", slot=" + slotBounds);
            }
            previousBounds = slotBounds;
        }
    }

    /// Returns the expected top app bar height for the active variant.
    private static double expectedTopAppBarHeight(M3TopAppBar appBar) {
        return switch (appBar.getVariant()) {
            case MEDIUM -> appBar.getMediumContainerHeight();
            case LARGE -> appBar.getLargeContainerHeight();
            case MEDIUM_FLEXIBLE -> (appBar.getSubtitle().isEmpty()
                    ? appBar.getMediumFlexibleContainerHeight()
                    : appBar.getMediumFlexibleSubtitleContainerHeight())
                    + (appBar.getContainerHeight() - (appBar.getSubtitle().isEmpty()
                    ? appBar.getMediumFlexibleContainerHeight()
                    : appBar.getMediumFlexibleSubtitleContainerHeight()))
                    * appBar.getCollapseProgress();
            case LARGE_FLEXIBLE -> (appBar.getSubtitle().isEmpty()
                    ? appBar.getLargeFlexibleContainerHeight()
                    : appBar.getLargeFlexibleSubtitleContainerHeight())
                    + (appBar.getContainerHeight() - (appBar.getSubtitle().isEmpty()
                    ? appBar.getLargeFlexibleContainerHeight()
                    : appBar.getLargeFlexibleSubtitleContainerHeight()))
                    * appBar.getCollapseProgress();
            case SMALL, CENTER_ALIGNED -> appBar.getContainerHeight();
        };
    }

    /// Asserts that rendered top app bar icon buttons keep Material action target sizing.
    private static void assertTopAppBarIconButtonTargets(M3TopAppBar appBar) {
        assertTopAppBarIconButtonTarget(Objects.requireNonNull(appBar.getNavigation(), "navigation"));
        for (Node action : appBar.getActions()) {
            assertTopAppBarIconButtonTarget(action);
        }
    }

    /// Asserts one rendered top app bar icon button target.
    private static void assertTopAppBarIconButtonTarget(Node action) {
        M3IconButton iconButton = assertInstanceOf(M3IconButton.class, action);
        Bounds bounds = iconButton.localToScene(iconButton.getBoundsInLocal());
        assertEquals(48.0, bounds.getWidth(), 1.0,
                () -> "top app bar icon button target width drifted: " + bounds);
        assertEquals(48.0, bounds.getHeight(), 1.0,
                () -> "top app bar icon button target height drifted: " + bounds);
        assertTopAppBarIconGraphicIsVector(iconButton);
    }

    /// Asserts that a top app bar icon button uses a vector fixture instead of a fallback text glyph.
    private static void assertTopAppBarIconGraphicIsVector(M3IconButton iconButton) {
        Node graphic = Objects.requireNonNull(iconButton.getGraphic(), "top app bar icon graphic");
        assertFalse(graphic instanceof M3Icon, "top app bar geometry fixture should not use text icon placeholders");
        assertTrue(containsSvgPath(graphic), () -> "top app bar icon graphic should contain SVG vector content: "
                + graphic);
    }

    /// Verifies that top app bar icon buttons do not clip fallback glyphs.
    @Test
    void topAppBarFallbackIconButtonsDoNotClipGlyphs() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon navigationIcon = new M3Icon("M", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            M3Icon searchIcon = new M3Icon("S", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            M3Icon accountIcon = new M3Icon("A", M3IconSize.SMALL, M3IconVariant.PRIMARY);
            M3IconButton navigationButton = new M3IconButton(navigationIcon);
            M3IconButton searchButton = new M3IconButton(searchIcon);
            M3IconButton accountButton = new M3IconButton(accountIcon);
            M3TopAppBar topAppBar = topAppBar(
                    "Inbox",
                    navigationButton,
                    searchButton,
                    accountButton
            );
            topAppBar.setPrefWidth(560.0);

            StackPane root = new StackPane(topAppBar);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 640.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(640.0, 96.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            for (M3Icon icon : java.util.List.of(navigationIcon, searchIcon, accountIcon)) {
                assertSnapshotNodeContainsContrast(image, icon, Color.WHITE, 0.05);
                assertSnapshotNodeEdgesClear(image, icon, Color.WHITE, 0.05);
            }
            for (M3IconButton button : java.util.List.of(navigationButton, searchButton, accountButton)) {
                assertAppBarIconButtonTargetGeometry(button);
            }
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-top-app-bar-fallback-icons.png"
            ));
        });
    }

    /// Verifies that an app bar icon button keeps its fallback glyph centered in the Material action target.
    private static void assertAppBarIconButtonTargetGeometry(M3IconButton iconButton) {
        Node graphic = Objects.requireNonNull(iconButton.getGraphic(), "graphic");
        Bounds buttonBounds = iconButton.localToScene(iconButton.getLayoutBounds());
        Bounds graphicBounds = graphic.localToScene(graphic.getBoundsInLocal());

        assertEquals(48.0, buttonBounds.getWidth(), 1.0,
                () -> "top app bar fallback icon button width drifted: " + buttonBounds);
        assertEquals(48.0, buttonBounds.getHeight(), 1.0,
                () -> "top app bar fallback icon button height drifted: " + buttonBounds);
        assertTrue(graphicBounds.getMinX() >= buttonBounds.getMinX() - 1.0
                        && graphicBounds.getMaxX() <= buttonBounds.getMaxX() + 1.0
                        && graphicBounds.getMinY() >= buttonBounds.getMinY() - 1.0
                        && graphicBounds.getMaxY() <= buttonBounds.getMaxY() + 1.0,
                () -> "top app bar fallback icon leaves its button target: button="
                        + buttonBounds + ", graphic=" + graphicBounds);
        assertEquals(buttonBounds.getCenterX(), graphicBounds.getCenterX(), 1.0,
                () -> "top app bar fallback icon is horizontally off-center: button="
                        + buttonBounds + ", graphic=" + graphicBounds);
        assertEquals(buttonBounds.getCenterY(), graphicBounds.getCenterY(), 1.0,
                () -> "top app bar fallback icon is vertically off-center: button="
                        + buttonBounds + ", graphic=" + graphicBounds);
    }

    /// Verifies that bottom app bars expose action and floating action slots.
    @Test
    void bottomAppBarExposesSlots() {
        Label search = new Label("Search");
        Label more = new Label("More");
        Label create = new Label("Create");
        M3BottomAppBar bottomAppBar = bottomAppBar(
                M3BottomAppBarFloatingActionAlignment.CENTER,
                create,
                search
        );

        bottomAppBar.getActions().add(more);

        assertEquals(M3BottomAppBarFloatingActionAlignment.CENTER, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getActions().contains(search));
        assertTrue(bottomAppBar.getActions().contains(more));
        assertEquals(create, bottomAppBar.getFloatingAction());

        bottomAppBar.getActions().clear();
        bottomAppBar.getActions().add(search);
        bottomAppBar.getActions().setAll(more);

        assertEquals(java.util.List.of(more), bottomAppBar.getActions());

        bottomAppBar.setFloatingAction(null);

        assertNull(bottomAppBar.getFloatingAction());
    }

    /// Verifies that bottom app bars expose floating action alignment modes.
    @Test
    void bottomAppBarFloatingActionAlignmentUpdatesStyleClasses() {
        M3BottomAppBar bottomAppBar = bottomAppBar(new Label("Search"));

        assertEquals(M3BottomAppBarFloatingActionAlignment.END, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.styleClass()
        ));

        bottomAppBar.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.CENTER);

        assertEquals(M3BottomAppBarFloatingActionAlignment.CENTER, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.CENTER.styleClass()
        ));
        assertFalse(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.styleClass()
        ));

        bottomAppBar.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.START);

        assertEquals(M3BottomAppBarFloatingActionAlignment.START, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.START.styleClass()
        ));
        assertFalse(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.CENTER.styleClass()
        ));

        bottomAppBar.floatingActionAlignmentProperty().set(null);

        assertEquals(M3BottomAppBarFloatingActionAlignment.END, bottomAppBar.getFloatingActionAlignment());
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.styleClass()
        ));
    }

    /// Verifies that bottom app bar token rules apply container metrics.
    @Test
    void bottomAppBarAppliesMetrics() {
        M3BottomAppBar bottomAppBar = bottomAppBar();
        Pane root = new Pane(bottomAppBar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(80.0, bottomAppBar.prefHeight(-1.0), 0.0001);
        assertEquals(80.0, bottomAppBar.getContainerHeight(), 0.0001);
        assertEquals(16.0, bottomAppBar.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, bottomAppBar.getContentSpacing(), 0.0001);
        assertEquals(0.0, bottomAppBar.getActionSpacing(), 0.0001);
        assertEquals(16.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertInstanceOf(M3BottomAppBarSkin.class, bottomAppBar.getSkin());
        HBox actions = assertInstanceOf(HBox.class, bottomAppBar.lookup("." + "m3-bottom-app-bar-actions"));
        assertEquals(0.0, actions.getSpacing(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(80.0, bottomAppBar.prefHeight(-1.0), 0.0001);
        assertEquals(80.0, bottomAppBar.getContainerHeight(), 0.0001);
        assertEquals(16.0, bottomAppBar.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, bottomAppBar.getContentSpacing(), 0.0001);
        assertEquals(16.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(0.0, bottomAppBar.getActionSpacing(), 0.0001);
        assertEquals(0.0, actions.getSpacing(), 0.0001);
        bottomAppBar.setActionSpacing(18.0);
        assertEquals(18.0, bottomAppBar.getActionSpacing(), 0.0001);
        assertEquals(18.0, actions.getSpacing(), 0.0001);
    }

    /// Verifies that generated bottom app bar action slots track action-list mutations without stale parents.
    @Test
    void bottomAppBarActionSlotsTrackActionListMutations() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconButton search = createGeometryTopAppBarIcon("search");
            M3IconButton more = createGeometryTopAppBarIcon("more");
            M3IconButton account = createGeometryTopAppBarIcon("person");
            M3BottomAppBar bottomAppBar = bottomAppBar(search, more);
            Pane root = new Pane(bottomAppBar);
            Scene scene = new Scene(root, 360.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            bottomAppBar.resizeRelocate(0.0, 0.0, 360.0, 80.0);
            root.applyCss();
            root.layout();

            assertEquals(2, bottomAppBar.lookupAll("." + "m3-bottom-app-bar-action-slot").size());

            bottomAppBar.getActions().setAll(more, account);
            root.applyCss();
            root.layout();

            assertEquals(2, bottomAppBar.lookupAll("." + "m3-bottom-app-bar-action-slot").size());
            assertNull(search.getParent(), "removed bottom app bar action should not keep an old slot parent");
            assertTrue(more.getParent() instanceof StackPane,
                    "reused bottom app bar action should be reparented into a slot");
            assertTrue(account.getParent() instanceof StackPane,
                    "new bottom app bar action should be parented into a slot");

            bottomAppBar.getActions().clear();
            root.applyCss();
            root.layout();

            assertEquals(0, bottomAppBar.lookupAll("." + "m3-bottom-app-bar-action-slot").size());
            assertNull(more.getParent(), "cleared bottom app bar action should not keep a slot parent");
            assertNull(account.getParent(), "cleared bottom app bar action should not keep a slot parent");
        });
    }

    /// Verifies that bottom app bars align floating action slots against the full bar instead of action width.
    @Test
    void bottomAppBarLaysOutFloatingActionSlotFromMaterialGeometry() {
        FxTestUtils.runOnFxThread(() -> {
            for (M3BottomAppBarFloatingActionAlignment alignment : M3BottomAppBarFloatingActionAlignment.values()) {
                M3BottomAppBar bottomAppBar = createGeometryBottomAppBar(alignment);
                Pane root = new Pane(bottomAppBar);
                root.setStyle("-fx-background-color: white; " + visualTestColors());
                Scene scene = new Scene(root, 540.0, 128.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                root.applyCss();
                bottomAppBar.resizeRelocate(30.0, 24.0, 480.0, 80.0);
                root.layout();
                bottomAppBar.layout();
                assertBottomAppBarMaterialSlotGeometry(bottomAppBar, false);

                bottomAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                bottomAppBar.layout();
                assertBottomAppBarMaterialSlotGeometry(bottomAppBar, true);
            }
        });
    }

    /// Verifies that app bars reflow logical slots without losing current action focus after runtime orientation changes.
    @Test
    void appBarsReflowSlotsAndPreserveFocusedActionsAfterRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconButton topNavigation = createGeometryTopAppBarIcon("menu");
            M3IconButton topSearch = createGeometryTopAppBarIcon("search");
            M3IconButton topMore = createGeometryTopAppBarIcon("more");
            M3TopAppBar topAppBar = topAppBar("Inbox", topNavigation, topSearch, topMore);
            topAppBar.setManaged(false);

            M3IconButton bottomSearch = createGeometryTopAppBarIcon("search");
            M3IconButton bottomMore = createGeometryTopAppBarIcon("more");
            M3IconButton bottomCreate = createGeometryTopAppBarIcon("add");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    bottomCreate,
                    bottomSearch,
                    bottomMore
            );
            bottomAppBar.setManaged(false);

            Pane root = new Pane(topAppBar, bottomAppBar);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 500.0, 220.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                topAppBar.resizeRelocate(20.0, 20.0, 420.0, 64.0);
                bottomAppBar.resizeRelocate(20.0, 104.0, 420.0, 80.0);
                root.layout();
                topAppBar.layout();
                bottomAppBar.layout();

                assertTopAppBarMaterialSlotGeometry(topAppBar);
                assertBottomAppBarMaterialSlotGeometry(bottomAppBar, false);

                topMore.requestFocus();
                assertTrue(topMore.isFocused());

                topAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                bottomAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                topAppBar.layout();
                bottomAppBar.layout();

                assertTopAppBarMaterialSlotGeometry(topAppBar);
                assertBottomAppBarMaterialSlotGeometry(bottomAppBar, true);
                assertTrue(topMore.isFocused());
                assertSame(topMore, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomMore.requestFocus();
                assertTrue(bottomMore.isFocused());

                topAppBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                bottomAppBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();
                topAppBar.layout();
                bottomAppBar.layout();

                assertTopAppBarMaterialSlotGeometry(topAppBar);
                assertBottomAppBarMaterialSlotGeometry(bottomAppBar, false);
                assertTrue(bottomMore.isFocused());
                assertSame(bottomMore, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomCreate.requestFocus();
                assertTrue(bottomCreate.isFocused());

                topAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                bottomAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                topAppBar.layout();
                bottomAppBar.layout();

                assertTopAppBarMaterialSlotGeometry(topAppBar);
                assertBottomAppBarMaterialSlotGeometry(bottomAppBar, true);
                assertTrue(bottomCreate.isFocused());
                assertSame(bottomCreate, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Creates a bottom app bar with deterministic slot sizes for geometry tests.
    private static M3BottomAppBar createGeometryBottomAppBar(M3BottomAppBarFloatingActionAlignment alignment) {
        return bottomAppBar(
                alignment,
                createFixedGeometryRegion(56.0, 56.0),
                createFixedGeometryRegion(48.0, 48.0),
                createFixedGeometryRegion(48.0, 48.0)
        );
    }

    /// Creates a fixed-size region used only by slot geometry tests.
    private static Region createFixedGeometryRegion(double width, double height) {
        Region region = new Region();
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        region.setMaxSize(width, height);
        return region;
    }

    /// Asserts Material slot geometry for one rendered bottom app bar.
    private static void assertBottomAppBarMaterialSlotGeometry(M3BottomAppBar appBar, boolean rightToLeft) {
        HBox actions = assertInstanceOf(HBox.class, appBar.lookup("." + "m3-bottom-app-bar-actions"));
        StackPane floatingActionSlot = assertInstanceOf(
                StackPane.class,
                appBar.lookup("." + "m3-bottom-app-bar-floating-action")
        );
        Bounds appBarBounds = appBar.localToScene(appBar.getBoundsInLocal());
        Bounds actionsBounds = actions.localToScene(actions.getBoundsInLocal());
        Bounds floatingActionBounds = floatingActionSlot.localToScene(floatingActionSlot.getBoundsInLocal());
        List<Node> actionSlots = appBar.lookupAll("." + "m3-bottom-app-bar-action-slot").stream()
                .sorted(java.util.Comparator.comparingDouble(node ->
                        node.localToScene(node.getBoundsInLocal()).getMinX()))
                .toList();
        double contentMinX = appBarBounds.getMinX() + appBar.getHorizontalPadding();
        double contentMaxX = appBarBounds.getMaxX() - appBar.getHorizontalPadding();
        double contentCenterX = (contentMinX + contentMaxX) / 2.0;
        double contentSpacing = appBar.getContentSpacing();

        assertEquals(appBarBounds.getCenterY(), actionsBounds.getCenterY(), 1.0,
                () -> "bottom app bar actions are not vertically centered: actions=" + actionsBounds
                        + ", appBar=" + appBarBounds);
        assertEquals(appBarBounds.getCenterY(), floatingActionBounds.getCenterY(), 1.0,
                () -> "bottom app bar floating action slot is not vertically centered: floatingAction="
                        + floatingActionBounds + ", appBar=" + appBarBounds);
        assertBottomAppBarActionSlots(appBar, actionSlots, appBarBounds.getCenterY());

        switch (appBar.getFloatingActionAlignment()) {
            case START -> {
                if (rightToLeft) {
                    assertEquals(contentMaxX, floatingActionBounds.getMaxX(), 1.0,
                            () -> "RTL start bottom app bar floating action should be on the physical right edge: "
                                    + floatingActionBounds + ", appBar=" + appBarBounds);
                    assertTrue(actionsBounds.getMaxX() <= floatingActionBounds.getMinX() - contentSpacing + 1.0,
                            () -> "RTL start bottom app bar actions should follow the floating action logically: actions="
                                    + actionsBounds + ", floatingAction=" + floatingActionBounds);
                } else {
                    assertEquals(contentMinX, floatingActionBounds.getMinX(), 1.0,
                            () -> "start bottom app bar floating action should be on the leading edge: "
                                    + floatingActionBounds + ", appBar=" + appBarBounds);
                    assertTrue(actionsBounds.getMinX() >= floatingActionBounds.getMaxX() + contentSpacing - 1.0,
                            () -> "start bottom app bar actions should follow the floating action logically: actions="
                                    + actionsBounds + ", floatingAction=" + floatingActionBounds);
                }
            }
            case CENTER -> {
                assertEquals(contentCenterX, floatingActionBounds.getCenterX(), 1.0,
                        () -> "center bottom app bar floating action should be centered in the full content area: "
                                + floatingActionBounds + ", appBar=" + appBarBounds);
                assertLogicalStart(actionsBounds, contentMinX, contentMaxX, rightToLeft, "center");
            }
            case END -> {
                if (rightToLeft) {
                    assertEquals(contentMinX, floatingActionBounds.getMinX(), 1.0,
                            () -> "RTL end bottom app bar floating action should be on the physical left edge: "
                                    + floatingActionBounds + ", appBar=" + appBarBounds);
                } else {
                    assertEquals(contentMaxX, floatingActionBounds.getMaxX(), 1.0,
                            () -> "end bottom app bar floating action should be on the trailing edge: "
                                    + floatingActionBounds + ", appBar=" + appBarBounds);
                }
                assertLogicalStart(actionsBounds, contentMinX, contentMaxX, rightToLeft, "end");
            }
        }
    }

    /// Asserts that bottom app bar regular actions are hosted in stable 48 dp Material slots.
    private static void assertBottomAppBarActionSlots(
            M3BottomAppBar appBar,
            List<Node> actionSlots,
            double rowCenterY
    ) {
        assertEquals(appBar.getActions().size(), actionSlots.size(),
                "bottom app bar action slot count does not match action nodes");
        @Nullable Bounds previousBounds = null;
        for (Node slot : actionSlots) {
            Bounds slotBounds = slot.localToScene(slot.getLayoutBounds());
            assertEquals(48.0, slotBounds.getWidth(), 1.0,
                    () -> "bottom app bar action slot width is not 48 dp: " + slotBounds);
            assertEquals(48.0, slotBounds.getHeight(), 1.0,
                    () -> "bottom app bar action slot height is not 48 dp: " + slotBounds);
            assertEquals(rowCenterY, slotBounds.getCenterY(), 1.0,
                    () -> "bottom app bar action slot is not centered in the bar row: " + slotBounds);
            if (previousBounds != null) {
                Bounds previousSlotBounds = previousBounds;
                assertEquals(appBar.getActionSpacing(), slotBounds.getMinX() - previousSlotBounds.getMaxX(), 1.0,
                        () -> "bottom app bar action slots do not use action spacing: previous="
                                + previousSlotBounds + ", slot=" + slotBounds);
            }
            previousBounds = slotBounds;
        }
    }

    /// Asserts that a slot is placed at the logical start edge.
    private static void assertLogicalStart(
            Bounds bounds,
            double contentMinX,
            double contentMaxX,
            boolean rightToLeft,
            String description
    ) {
        if (rightToLeft) {
            assertEquals(contentMaxX, bounds.getMaxX(), 1.0,
                    () -> "RTL " + description + " bottom app bar actions should start on the physical right edge: "
                            + bounds);
        } else {
            assertEquals(contentMinX, bounds.getMinX(), 1.0,
                    () -> description + " bottom app bar actions should start on the physical left edge: " + bounds);
        }
    }

    /// Verifies that app bars and banners create Material Design 3 skins.
    @Test
    void appBarsAndBannersCreateMaterialSkins() {
        M3Banner banner = banner("Message");
        M3TopAppBar topAppBar = topAppBar("Inbox");
        M3BottomAppBar bottomAppBar = bottomAppBar();
        Pane root = new Pane(banner, topAppBar, bottomAppBar);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3BannerSkin.class, banner.getSkin());
        assertInstanceOf(M3TopAppBarSkin.class, topAppBar.getSkin());
        assertInstanceOf(M3BottomAppBarSkin.class, bottomAppBar.getSkin());
    }

    /// Verifies that banner leading icons remain centered with single-line and wrapping messages.
    @Test
    void bannersCenterLeadingIconsWithMessageContent() {
        FxTestUtils.runOnFxThread(() -> {
            Region singleLineIcon = new Region();
            singleLineIcon.setPrefSize(24.0, 24.0);
            singleLineIcon.setMinSize(24.0, 24.0);
            singleLineIcon.setMaxSize(24.0, 24.0);
            M3Banner singleLine = createBanner(
                    "A concise banner message.",
                    singleLineIcon,
                    new M3Button("Action", M3ButtonVariant.TEXT)
            );

            Region wrappingIcon = new Region();
            wrappingIcon.setPrefSize(24.0, 24.0);
            wrappingIcon.setMinSize(24.0, 24.0);
            wrappingIcon.setMaxSize(24.0, 24.0);
            M3Banner wrapping = createBanner(
                    "A longer banner message that wraps onto a second line at this width.",
                    wrappingIcon,
                    new M3Button("Action", M3ButtonVariant.TEXT)
            );

            VBox root = new VBox(singleLine, wrapping);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 480.0, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            assertBannerIconCenteredWithText(singleLine, singleLineIcon);
            assertBannerIconCenteredWithText(wrapping, wrappingIcon);
        });
    }

    /// Verifies that app bars and banners mirror logical leading and trailing slots in right-to-left layouts.
    @Test
    void appBarsAndBannersMirrorLogicalSlotsForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            Label topNavigation = new Label("N");
            Label topAction = new Label("A");
            M3TopAppBar topAppBar = topAppBar("Inbox", topNavigation, topAction);
            topAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            topAppBar.setPrefWidth(420.0);

            Label bottomAction = new Label("B");
            Label floatingAction = new Label("F");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomAction
            );
            bottomAppBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            bottomAppBar.setPrefWidth(420.0);

            Label bannerIcon = new Label("I");
            Label bannerAction = new Label("C");
            M3Banner banner = createBanner("Message", bannerIcon, bannerAction);
            banner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            banner.setPrefWidth(420.0);

            Pane root = new Pane(topAppBar, bottomAppBar, banner);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 460.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            topAppBar.resizeRelocate(20.0, 20.0, 420.0, 64.0);
            bottomAppBar.resizeRelocate(20.0, 96.0, 420.0, 80.0);
            banner.resizeRelocate(20.0, 188.0, 420.0, 48.0);
            root.layout();
            topAppBar.layout();
            bottomAppBar.layout();
            banner.layout();

            Bounds topNavigationBounds = topNavigation.localToScene(topNavigation.getBoundsInLocal());
            Bounds topActionBounds = topAction.localToScene(topAction.getBoundsInLocal());
            Bounds topAppBarBounds = topAppBar.localToScene(topAppBar.getBoundsInLocal());
            Label topTitle = assertInstanceOf(Label.class, topAppBar.lookup("." + "m3-top-app-bar-title"));
            Text topTitleText = assertInstanceOf(Text.class, topTitle.lookup(".text"));
            Bounds topTitleTextBounds = topTitleText.localToScene(topTitleText.getBoundsInLocal());
            Parent topNavigationParent = Objects.requireNonNull(topNavigation.getParent(), "topNavigationParent");
            Parent topActionParent = Objects.requireNonNull(topAction.getParent(), "topActionParent");
            Bounds topNavigationParentBounds = topNavigationParent.localToScene(topNavigationParent.getBoundsInLocal());
            Bounds topActionParentBounds = topActionParent.localToScene(topActionParent.getBoundsInLocal());
            assertTrue(
                    topNavigationBounds.getMinX() > topActionBounds.getMaxX(),
                    () -> "top app bar RTL slots are not mirrored: navigation="
                            + topNavigationBounds + ", action=" + topActionBounds
                            + ", appBar=" + topAppBarBounds
                            + ", orientation=" + topAppBar.getNodeOrientation()
                            + ", effectiveOrientation=" + topAppBar.getEffectiveNodeOrientation()
                            + ", navigationParent=" + topNavigationParentBounds
                            + ", actionParent=" + topActionParentBounds
            );
            assertTrue(
                    topTitleTextBounds.getCenterX() > topAppBarBounds.getCenterX()
                            && topTitleTextBounds.getMaxX() <= topNavigationBounds.getMinX(),
                    () -> "top app bar RTL title text is not aligned to the navigation side: title="
                            + topTitleTextBounds + ", navigation=" + topNavigationBounds
                            + ", action=" + topActionBounds + ", appBar=" + topAppBarBounds
            );
            assertTrue(
                    floatingAction.localToScene(floatingAction.getBoundsInLocal()).getMaxX()
                            < bottomAction.localToScene(bottomAction.getBoundsInLocal()).getMinX()
            );
            assertTrue(
                    bannerIcon.localToScene(bannerIcon.getBoundsInLocal()).getMinX()
                            > bannerAction.localToScene(bannerAction.getBoundsInLocal()).getMaxX()
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, topNavigation, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, floatingAction, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, bannerIcon, Color.WHITE, 0.05);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-app-bars-banner-rtl.png"
            ));
        });
    }

    /// Verifies that a banner icon and its rendered message share one vertical center.
    private static void assertBannerIconCenteredWithText(M3Banner banner, Node icon) {
        Label text = assertInstanceOf(Label.class, banner.lookup("." + "m3-banner-text"));
        Bounds iconBounds = icon.localToScene(icon.getBoundsInLocal());
        Bounds textBounds = text.localToScene(text.getBoundsInLocal());
        assertEquals(textBounds.getCenterY(), iconBounds.getCenterY(), 0.5,
                () -> "banner icon should be centered with its message: icon="
                        + iconBounds + ", text=" + textBounds);
    }
}
