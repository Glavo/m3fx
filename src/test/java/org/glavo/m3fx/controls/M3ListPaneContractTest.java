// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3ListItemSkin;
import org.glavo.m3fx.skins.M3ListPaneSkin;
import org.glavo.m3fx.skins.M3ListViewSkin;
import org.glavo.m3fx.skins.M3TextSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3TextStyle;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.m3fx.tokens.M3TypographyTokens;
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

/// Verifies static list layout, grouping, selection, and item presentation.
@NotNullByDefault
final class M3ListPaneContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Wrapped supporting text adds space below the headline rather than displacing or clipping it.
    @Test
    void wrappedSupportingTextGrowsBelowHeadline() {
        FxTestUtils.runOnFxThread(() -> {
            for (M3ListItemBase item : List.of(new M3ListItem("Download"), new M3ExpandableSettingItem("Download"))) {
                String description = "Use the download mirror appropriate for your region and network.";
                item.setSupportingText(description);
                item.setLeading(new Rectangle(28, 28));
                item.setTrailing(new Rectangle(18, 18));
                M3ListItem next = new M3ListItem("Next");
                M3ListPane list = listPane(item, next);
                list.setPadding(new Insets(0, 18, 0, 18));
                VBox root = new VBox(list);
                Scene scene = new Scene(root, 800, 400);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                root.applyCss();
                Label supporting = (Label) item.lookup(".m3-list-item-supporting");
                supporting.setWrapText(true);
                root.resize(800, 400);
                root.layout();
                Text headline = (Text) item.lookup(".m3-list-item-headline .text");
                Text body = (Text) supporting.lookup(".text");
                WritableImage wide = snapshotImageOnFxThread(root);
                double titleY = requiredRenderedTextInkBounds(wide, headline).getMinY();
                double singleLineInkHeight = requiredRenderedTextInkBounds(wide, body).getHeight();
                double wideHeight = item.getHeight();
                root.resize(330, 400);
                root.layout();
                WritableImage narrow = snapshotImageOnFxThread(root);
                assertTrue(item.getHeight() > wideHeight, "wrapped lines must grow the row");
                assertEquals(titleY, requiredRenderedTextInkBounds(narrow, headline).getMinY(), 1.0);
                assertEquals(description, body.getText());
                var narrowBody = requiredRenderedTextInkBounds(narrow, body);
                assertTrue(narrowBody.getHeight() > singleLineInkHeight * 1.5);
                assertTrue(narrowBody.getMaxY() < next.localToScene(0, 0).getY());
                assertEquals(next.localToScene(0, next.getHeight()).getY(), list.getHeight(), 1.0);
                root.resize(800, 400);
                root.layout();
                assertEquals(wideHeight, item.getHeight());
            }
        });
    }

    /// Structural CSS states follow all displayed nodes through reordering, removal, and skin disposal.
    @Test
    void listPaneStylesFirstLastAndOnlyChildren() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListSectionHeader header = new M3ListSectionHeader("Section");
            M3ListItem row = new M3ListItem("Row");
            M3Divider divider = new M3Divider();
            M3ListPane list = listPane(header, row, divider);
            Pane root = new Pane(list);
            Scene scene = new Scene(root);
            scene.getStylesheets().add("data:text/css,.m3-list-pane%20.m3-list-item:only-child%7B-fx-rotate:7;%7D");
            root.applyCss();
            assertItemPosition(header, true, false);
            assertItemPosition(row, false, false);
            assertItemPosition(divider, false, true);

            list.getItems().setAll(divider, row, header);
            assertItemPosition(divider, true, false);
            assertItemPosition(header, false, true);
            list.getItems().setAll(row);
            assertItemPosition(divider, false, false);
            assertItemPosition(header, false, false);
            assertItemPosition(row, true, true);
            root.applyCss();
            assertEquals(7.0, row.getRotate());

            list.getItems().add(header);
            root.applyCss();
            assertEquals(0.0, row.getRotate());
            assertItemPosition(row, true, false);
            assertItemPosition(header, false, true);
            list.getItems().clear();
            assertItemPosition(row, false, false);
            assertItemPosition(header, false, false);
            list.getItems().add(row);
            list.setSkin(null);
            assertItemPosition(row, false, false);
        });
    }

    /// Checks all three edge states, including those that must be absent.
    private static void assertItemPosition(Node node, boolean first, boolean last) {
        var states = node.getPseudoClassStates();
        assertEquals(first, states.contains(PseudoClass.getPseudoClass("first-child")));
        assertEquals(last, states.contains(PseudoClass.getPseudoClass("last-child")));
        assertEquals(first && last, states.contains(PseudoClass.getPseudoClass("only-child")));
    }

    /// Verifies that static lists support printable-key type-ahead focus and single selection.
    @Test
    void listPaneSupportsTypeAheadKeyboardNavigation() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem archive = new M3ListItem("Archive");
            M3ListItem settings = new M3ListItem("Settings");
            M3ListItem search = new M3ListItem("Search");
            settings.setDisable(true);
            M3ListPane listPane = listPane(archive, settings, search);
            listPane.setSelectionMode(M3SelectionMode.SINGLE);
            Pane root = new Pane(listPane);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 180.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listPane.requestFocus();

                listPane.fireEvent(keyTypedEvent("s"));

                assertTrue(search.isFocused());
                assertSame(search, listPane.getSelectedItem());
                assertSame(search, listPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listPane.fireEvent(keyTypedEvent("a"));

                assertTrue(archive.isFocused());
                assertSame(archive, listPane.getSelectedItem());
                assertSame(archive, listPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that list navigation and type-ahead keys do not steal focus from embedded text inputs.
    @Test
    void listContainersDoNotStealEmbeddedTextInputKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField paneEditor = createTextField("Pane", M3TextInputVariant.OUTLINED);
            paneEditor.setPrefWidth(150.0);
            M3ListItem paneEditable = new M3ListItem("Editable");
            paneEditable.setTrailing(paneEditor);
            M3ListItem paneSearch = new M3ListItem("Search");
            M3ListPane listPane = listPane(paneEditable, paneSearch);
            listPane.setSelectionMode(M3SelectionMode.SINGLE);
            listPane.select(paneEditable);
            M3TextField outsideEditor = createTextField("Outside", M3TextInputVariant.OUTLINED);

            M3ListView<String> listView = listView("Editable", "Search");
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(72.0);
            listView.setPrefSize(340.0, 150.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText(value);
                if ("Editable".equals(value)) {
                    if (!(item.getTrailing() instanceof M3TextField)) {
                        M3TextField viewEditor = createTextField("View", M3TextInputVariant.OUTLINED);
                        viewEditor.setPrefWidth(150.0);
                        item.setTrailing(viewEditor);
                    }
                } else {
                    item.setTrailing(null);
                }
            }));
            listView.selectIndex(0);

            VBox root = new VBox(12.0, listPane, listView, outsideEditor);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 460.0, 390.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertContainerNavigationDoesNotStealTextInputFocus(root, listPane, paneEditor, paneSearch, KeyCode.DOWN);
                listPane.fireEvent(keyTypedEvent("s"));
                assertTrue(paneEditor.isFocused(), "static list type-ahead should not steal embedded text input focus");
                assertSame(paneEditable, listPane.getSelectedItem());

                assertInstanceOf(M3ListViewSkin.class, listView.getSkin());
                M3ListItem visibleEditable = assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0)
                );
                M3TextField viewEditor = assertInstanceOf(M3TextField.class, visibleEditable.getTrailing());
                assertContainerNavigationDoesNotStealTextInputFocus(root, listView, viewEditor, listView, KeyCode.DOWN);
                listView.fireEvent(keyTypedEvent("s"));
                assertTrue(viewEditor.isFocused(), "virtualized list type-ahead should not steal embedded text input focus");
                assertEquals(0, listView.getSelectedIndex());
                assertEquals("Editable", listView.getSelectedItem());

                listPane.select(paneEditable);
                outsideEditor.requestFocus();
                assertTrue(outsideEditor.isFocused());
                listPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertSame(paneSearch, listPane.getSelectedItem());
                assertTrue(paneSearch.isFocused());

                listView.selectIndex(0);
                outsideEditor.requestFocus();
                assertTrue(outsideEditor.isFocused());
                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertEquals(1, listView.getSelectedIndex());
                assertEquals("Search", listView.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that list item component token properties are styleable from CSS.
    @Test
    void listItemTokensAreStyleable() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setSupportingText("Supporting");
        listItem.setStyle(
                "-m3-one-line-height: 60px; "
                        + "-m3-two-line-height: 76px; "
                        + "-m3-three-line-height: 92px; "
                        + "-m3-container-shape: 12px; "
                        + "-m3-horizontal-padding: 20px; "
                        + "-m3-vertical-padding: 10px; "
                        + "-m3-content-spacing: 18px;"
        );

        applyCss(listItem);

        assertEquals(60.0, listItem.getOneLineHeight(), 0.0001);
        assertEquals(76.0, listItem.getTwoLineHeight(), 0.0001);
        assertEquals(92.0, listItem.getThreeLineHeight(), 0.0001);
        assertEquals(12.0, listItem.getContainerShape(), 0.0001);
        assertEquals(20.0, listItem.getHorizontalPadding(), 0.0001);
        assertEquals(10.0, listItem.getVerticalPadding(), 0.0001);
        assertEquals(18.0, listItem.getContentSpacing(), 0.0001);
        assertInstanceOf(M3ListItemSkin.class, listItem.getSkin());
    }

    /// Verifies that list items expose trailing supporting text without changing the row line count.
    @Test
    void listItemSupportsTrailingSupportingText() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setTrailingSupportingText("3 min");
        listItem.setTrailingIcon(">");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 280.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(260.0, 56.0);
        listItem.layout();

        assertListItemLineCount(listItem, M3ListItemLineCount.ONE_LINE);
        assertEquals("Headline 3 min", listItem.getAccessibleText());
        Node trailingText = listItem.lookup(".m3-list-item-trailing-supporting");
        assertInstanceOf(Label.class, trailingText);
        assertEquals("3 min", ((Label) trailingText).getText());
        assertTrue(trailingText.isVisible());
        assertTrue(trailingText.isManaged());
    }

    /// Verifies that list and menu presentation labels use shared semantic typography classes.
    @Test
    void listAndMenuLabelsUseSemanticTypographyClasses() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setOverlineText("Overline");
        listItem.setSupportingText("Supporting");
        listItem.setTrailingSupportingText("Trailing");
        applyCss(listItem);

        assertTrue(listItem.lookup(".m3-list-item-overline").getStyleClass().contains("m3-label-small-text"));
        assertTrue(listItem.lookup(".m3-list-item-headline").getStyleClass().contains("m3-body-large-text"));
        assertTrue(listItem.lookup(".m3-list-item-supporting").getStyleClass().contains("m3-body-medium-text"));
        assertTrue(listItem.lookup(".m3-list-item-trailing-supporting")
                .getStyleClass()
                .contains("m3-label-small-text"));

        M3MenuItem menuItem = new M3MenuItem("Open");
        menuItem.setSupportingText("Recent document");
        menuItem.setTrailingSupportingText("Ctrl+O");
        applyCss(menuItem);

        assertTrue(menuItem.lookup(".m3-list-item-headline").getStyleClass().contains("m3-label-large-text"));
        assertTrue(menuItem.lookup(".m3-list-item-supporting").getStyleClass().contains("m3-body-small-text"));
        assertTrue(menuItem.lookup(".m3-list-item-trailing-supporting")
                .getStyleClass()
                .contains("m3-label-large-text"));
    }

    /// Verifies that shared typography classes resolve custom theme roles for list-derived controls.
    @Test
    void listDerivedControlsResolveCustomTypographyRoles() {
        M3Theme baseTheme = M3Theme.defaultTheme();
        M3TypographyTokens typography = M3TypographyTokens.builder(baseTheme.tokens().typographyTokens())
                .labelLarge(M3TextStyle.of("System", 18.0, 24.0, 500))
                .labelSmall(M3TextStyle.of("System", 9.0, 12.0, 500))
                .bodyLarge(M3TextStyle.of("System", 20.0, 28.0, 400))
                .bodyMedium(M3TextStyle.of("System", 16.0, 24.0, 400))
                .bodySmall(M3TextStyle.of("System", 12.0, 16.0, 400))
                .build();
        M3Theme theme = M3Theme.fromTokenSet(
                M3TokenSet.builder(baseTheme.tokens()).typographyTokens(typography).build()
        );

        M3ListItem listItem = new M3ListItem("List headline");
        listItem.setOverlineText("List overline");
        listItem.setSupportingText("List supporting");
        listItem.setTrailingSupportingText("List trailing");

        M3MenuItem menuItem = new M3MenuItem("Menu headline");
        menuItem.setSupportingText("Menu supporting");
        menuItem.setTrailingSupportingText("Menu trailing");

        M3ListItem drawerItem = new M3ListItem("Drawer headline");
        M3NavigationDrawer drawer = navigationDrawer(drawerItem);
        VBox root = new VBox(listItem, menuItem, drawer);
        Scene scene = new Scene(root, 420.0, 280.0);

        M3ThemeManager.install(scene, theme);
        root.applyCss();

        assertEquals(9.0, ((Label) listItem.lookup(".m3-list-item-overline")).getFont().getSize(), 0.0001);
        assertEquals(20.0, ((Label) listItem.lookup(".m3-list-item-headline")).getFont().getSize(), 0.0001);
        assertEquals(16.0, ((Label) listItem.lookup(".m3-list-item-supporting")).getFont().getSize(), 0.0001);
        assertEquals(
                9.0,
                ((Label) listItem.lookup(".m3-list-item-trailing-supporting")).getFont().getSize(),
                0.0001
        );
        assertEquals(18.0, ((Label) menuItem.lookup(".m3-list-item-headline")).getFont().getSize(), 0.0001);
        assertEquals(12.0, ((Label) menuItem.lookup(".m3-list-item-supporting")).getFont().getSize(), 0.0001);
        assertEquals(
                18.0,
                ((Label) menuItem.lookup(".m3-list-item-trailing-supporting")).getFont().getSize(),
                0.0001
        );
        assertEquals(18.0, listItemHeadlineLabel(drawerItem).getFont().getSize(), 0.0001);
    }

    /// Verifies that list items mirror logical leading and trailing content in right-to-left layouts.
    @Test
    void listItemMirrorsLogicalSlotsForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem listItem = new M3ListItem("Headline");
            listItem.setSupportingText("Supporting");
            listItem.setLeadingIcon("L");
            listItem.setTrailingSupportingText("3 min");
            listItem.setTrailingIcon("T");
            listItem.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            StackPane root = new StackPane(listItem);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 360.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(360.0, 120.0);
            root.layout();
            listItem.resize(320.0, 72.0);
            listItem.layout();

            Node leadingSlot = Objects.requireNonNull(listItem.lookup(".m3-list-item-leading"));
            Node trailingSlot = Objects.requireNonNull(listItem.lookup(".m3-list-item-trailing"));
            VBox textBox = assertInstanceOf(VBox.class, listItem.lookup(".m3-list-item-text"));
            Node headline = Objects.requireNonNull(listItem.lookup(".m3-list-item-headline"));
            Bounds leadingBounds = leadingSlot.localToScene(leadingSlot.getBoundsInLocal());
            Bounds trailingBounds = trailingSlot.localToScene(trailingSlot.getBoundsInLocal());
            Bounds textBoxBounds = textBox.localToScene(textBox.getBoundsInLocal());
            Bounds headlineBounds = headline.localToScene(headline.getBoundsInLocal());

            assertEquals(Pos.CENTER_LEFT, textBox.getAlignment());
            assertTrue(leadingBounds.getMinX() > trailingBounds.getMaxX(),
                    () -> "leadingBounds=" + leadingBounds + ", trailingBounds=" + trailingBounds);
            assertEquals(textBoxBounds.getMaxX(), headlineBounds.getMaxX(), 1.0,
                    () -> "headline should align to the RTL logical start: textBoxBounds="
                            + textBoxBounds + ", headlineBounds=" + headlineBounds);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, leadingSlot, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, trailingSlot, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-item-rtl.png"
            ));
        });
    }

    /// Verifies that fixed list item slot sizes drive media layout and clipping.
    @Test
    void listItemSlotSizesDriveMediaLayout() {
        M3ListItem listItem = new M3ListItem("Headline");
        StackPane thumbnail = new StackPane(new Label("T"));
        listItem.setLeadingThumbnail(thumbnail);
        listItem.setTrailingIcon(">");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 320.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(300.0, 72.0);
        listItem.layout();

        Node leadingSlot = listItem.lookup(".m3-list-item-leading");
        Node trailingSlot = listItem.lookup(".m3-list-item-trailing");
        assertInstanceOf(Region.class, leadingSlot);
        assertInstanceOf(Region.class, trailingSlot);
        assertEquals(M3ListItemSlotSize.THUMBNAIL, listItem.getLeadingSlotSize());
        assertEquals(M3ListItemSlotSize.ICON, listItem.getTrailingSlotSize());
        assertEquals(56.0, ((Region) leadingSlot).getPrefWidth(), 0.0001);
        assertEquals(56.0, ((Region) leadingSlot).getPrefHeight(), 0.0001);
        assertEquals(24.0, ((Region) trailingSlot).getPrefWidth(), 0.0001);
        assertEquals(24.0, ((Region) trailingSlot).getPrefHeight(), 0.0001);
        assertInstanceOf(Rectangle.class, leadingSlot.getClip());
        Rectangle clip = (Rectangle) leadingSlot.getClip();
        assertEquals(56.0, clip.getWidth(), 0.0001);
        assertEquals(56.0, clip.getHeight(), 0.0001);
        assertEquals(8.0, clip.getArcWidth(), 0.0001);
    }

    /// Verifies that list item media slots render the supported fixed-size variants.
    @Tier2Test
    @Test
    void listItemMediaSnapshotRendersSlotVariants() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem iconItem = new M3ListItem("Icon row");
            iconItem.setLeadingIcon("I");
            iconItem.setTrailingSupportingText("Now");

            M3ListItem avatarItem = new M3ListItem("Avatar row");
            avatarItem.setSupportingText("Avatar-sized leading slot");
            avatarItem.setLeadingAvatar("A");
            avatarItem.setTrailingIcon(">");

            M3ListItem thumbnailItem = new M3ListItem("Thumbnail row");
            thumbnailItem.setSupportingText("Square thumbnail media");
            thumbnailItem.setLeadingThumbnail(visualListMedia("T"));
            thumbnailItem.setTrailingSupportingText("12:40");

            M3ListItem wideThumbnailItem = new M3ListItem("Wide thumbnail row");
            wideThumbnailItem.setSupportingText("Wide thumbnail media");
            wideThumbnailItem.setLeadingWideThumbnail(visualListMedia("W"));
            wideThumbnailItem.setTrailingIcon(">");

            M3ListPane list = listPane(
                    new M3ListSectionHeader("Media"),
                    iconItem,
                    avatarItem,
                    thumbnailItem,
                    wideThumbnailItem
            );
            list.setPrefWidth(420.0);
            list.setStyle("-fx-background-color: white; -fx-padding: 8px 0 8px 0; " + visualTestColors());
            Scene scene = new Scene(list, 440.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            list.applyCss();
            list.resize(420.0, 340.0);
            list.layout();

            WritableImage image = snapshotImageOnFxThread(list);
            assertSnapshotNodeContainsContrast(image, listItemContainer(iconItem), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, listItemContainer(avatarItem), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, listItemContainer(thumbnailItem), Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, listItemContainer(wideThumbnailItem), Color.WHITE, 0.03);
            assertEquals(56.0, ((Region) thumbnailItem.lookup(".m3-list-item-leading")).getWidth(), 0.0001);
            assertEquals(64.0, ((Region) wideThumbnailItem.lookup(".m3-list-item-leading")).getWidth(), 0.0001);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-item-media.png"
            ));
        });
    }

    /// Verifies that list item line count state follows text content.
    @Test
    void listItemLineCountTracksTextContent() {
        M3ListItem listItem = new M3ListItem("Headline");

        assertListItemLineCount(listItem, M3ListItemLineCount.ONE_LINE);

        listItem.setSupportingText("Supporting");
        assertListItemLineCount(listItem, M3ListItemLineCount.TWO_LINE);

        listItem.setOverlineText("Overline");
        assertListItemLineCount(listItem, M3ListItemLineCount.THREE_LINE);

        listItem.setSupportingText("");
        assertListItemLineCount(listItem, M3ListItemLineCount.TWO_LINE);

        listItem.setOverlineText("");
        assertListItemLineCount(listItem, M3ListItemLineCount.ONE_LINE);
    }

    /// Verifies that list item skins read the control line count when selecting height tokens.
    @Test
    void listItemLineCountDrivesSkinHeight() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setStyle(
                "-m3-one-line-height: 60px; "
                        + "-m3-two-line-height: 76px; "
                        + "-m3-three-line-height: 92px;"
        );
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());

        root.applyCss();
        assertEquals(60.0, listItem.prefHeight(240.0), 0.0001);

        listItem.setSupportingText("Supporting");
        root.applyCss();
        assertEquals(76.0, listItem.prefHeight(240.0), 0.0001);

        listItem.setOverlineText("Overline");
        root.applyCss();
        assertEquals(92.0, listItem.prefHeight(240.0), 0.0001);
    }

    /// Verifies that list items expose selected state and action behavior.
    @Test
    void listItemSupportsSelectionAndAction() {
        M3ListItem listItem = new M3ListItem("Headline");
        AtomicInteger fireCount = new AtomicInteger();
        listItem.setOnAction(event -> fireCount.incrementAndGet());
        listItem.setSelected(true);

        applyCss(listItem);
        listItem.resize(220.0, 56.0);
        listItem.layout();

        assertTrue(listItem.isSelected());
        listItem.fire();
        assertEquals(1, fireCount.get());
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertEquals(2, fireCount.get());
        listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertEquals(3, fireCount.get());
        listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        listItem.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertEquals(4, fireCount.get());
    }

    /// Verifies that list items expose leading and trailing slot focus targets.
    @Test
    void listItemAccessibleFocusIncludesLeadingAndTrailingSlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button leading = new M3Button("Leading");
            M3Button trailing = new M3Button("Trailing");
            M3ListItem listItem = new M3ListItem("Headline");
            listItem.setLeading(leading);
            listItem.setTrailing(trailing);
            Pane root = new Pane(listItem);
            Stage stage = new Stage();

            try {
                Scene scene = new Scene(root, 360.0, 160.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listItem.resizeRelocate(24.0, 24.0, 300.0, 72.0);
                root.layout();

                assertEquals(2, listItem.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(leading, listItem.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(trailing, listItem.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(listItem, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listItem.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(listItem.isFocused());
                assertSame(listItem, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                leading.requestFocus();

                assertTrue(leading.isFocused());
                assertSame(leading, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(leading.isFocused());
                assertSame(leading, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, trailing);

                assertTrue(trailing.isFocused());
                assertSame(trailing, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listItem.setLeading(null);

                assertEquals(1, listItem.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(trailing, listItem.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertNull(listItem.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

                listItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

                assertTrue(trailing.isFocused());
                assertSame(trailing, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies Standard and Segmented static-list spacing, including non-item boundaries.
    @Test
    void listPaneAppliesSegmentedSpacingOnlyBetweenAdjacentItems() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("First");
            M3ListItem second = new M3ListItem("Second");
            M3ListSectionHeader header = new M3ListSectionHeader("Section");
            M3ListItem afterHeader = new M3ListItem("After header");
            M3Divider divider = new M3Divider();
            M3ListItem afterDivider = new M3ListItem("After divider");
            M3ListItem last = new M3ListItem("Last");

            M3ListPane list = listPane(first, second, header, afterHeader, divider, afterDivider, last);
            list.setListStyle(M3ListStyle.SEGMENTED);
            list.setPrefWidth(360.0);
            VBox root = new VBox(list);
            Scene scene = new Scene(root, 400.0, 520.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, M3Theme.fromSeed(
                        Color.web("#6750a4"),
                        M3Profile.EXPRESSIVE_2025,
                        Brightness.LIGHT
                ));
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(M3ListStyle.SEGMENTED, list.getListStyle());
                assertTrue(list.getStyleClass().contains(M3ListStyle.SEGMENTED.styleClass()));
                assertFalse(list.getStyleClass().contains(M3ListStyle.STANDARD.styleClass()));
                assertEquals(2.0, list.getItemSpacing(), 0.0001);
                assertEquals(2.0, second.getLayoutY() - (first.getLayoutY() + first.getHeight()), 0.0001);
                assertEquals(0.0, header.getLayoutY() - (second.getLayoutY() + second.getHeight()), 0.0001);
                assertEquals(0.0, afterHeader.getLayoutY() - (header.getLayoutY() + header.getHeight()), 0.0001);
                assertEquals(0.0, divider.getLayoutY() - (afterHeader.getLayoutY() + afterHeader.getHeight()), 0.0001);
                assertEquals(0.0, afterDivider.getLayoutY() - (divider.getLayoutY() + divider.getHeight()), 0.0001);
                assertEquals(2.0, last.getLayoutY() - (afterDivider.getLayoutY() + afterDivider.getHeight()), 0.0001);

                list.setItemSpacing(6.0);
                root.applyCss();
                root.layout();
                assertEquals(6.0, list.getItemSpacing(), 0.0001);
                assertEquals(6.0, second.getLayoutY() - (first.getLayoutY() + first.getHeight()), 0.0001);
                assertEquals(0.0, header.getLayoutY() - (second.getLayoutY() + second.getHeight()), 0.0001);

                M3ListPane standard = listPane(new M3ListItem("A"), new M3ListItem("B"));
                root.getChildren().add(standard);
                root.applyCss();
                root.layout();
                assertEquals(M3ListStyle.STANDARD, standard.getListStyle());
                assertEquals(0.0, standard.getItemSpacing(), 0.0001);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that segmented-list shapes follow the active profile and official interaction-state tokens.
    @Test
    void segmentedListShapesFollowProfileAndInteractionTokens() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem standardItem = new M3ListItem("Standard");
            M3ListPane standardList = listPane(standardItem);
            M3ListItem segmentedItem = new M3ListItem("Segmented");
            M3ListPane segmentedList = listPane(segmentedItem);
            segmentedList.setListStyle(M3ListStyle.SEGMENTED);

            VBox root = new VBox(8.0, standardList, segmentedList);
            Scene scene = new Scene(root, 360.0, 180.0);
            M3Theme baseline = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.BASELINE_2021,
                    Brightness.LIGHT
            );
            M3Theme expressive = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, baseline);
                stage.show();
                root.applyCss();
                root.layout();
                assertEquals(0.0, standardItem.getContainerShape(), 0.0001);
                assertEquals(0.0, segmentedItem.getContainerShape(), 0.0001);

                M3ThemeManager.install(scene, expressive);
                root.applyCss();
                root.layout();
                assertEquals(0.0, standardItem.getContainerShape(), 0.0001);
                assertEquals(expressive.tokens().shapeTokens().extraSmall(), segmentedItem.getContainerShape(), 0.0001);

                PseudoClass hover = PseudoClass.getPseudoClass("hover");
                segmentedItem.pseudoClassStateChanged(hover, true);
                root.applyCss();
                assertEquals(expressive.tokens().shapeTokens().medium(), segmentedItem.getContainerShape(), 0.0001);

                segmentedItem.setDisable(true);
                root.applyCss();
                assertEquals(expressive.tokens().shapeTokens().extraSmall(), segmentedItem.getContainerShape(), 0.0001);

                segmentedItem.setDisable(false);
                segmentedItem.pseudoClassStateChanged(hover, false);
                segmentedList.setSelectionMode(M3SelectionMode.SINGLE);
                segmentedList.select(segmentedItem);
                root.applyCss();
                assertTrue(segmentedItem.isSelected());
                assertTrue(segmentedItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")));
                assertEquals(expressive.tokens().shapeTokens().large(), segmentedItem.getContainerShape(), 0.0001);

                segmentedItem.setSelected(false);
                segmentedItem.setDisable(false);
                segmentedItem.pseudoClassStateChanged(hover, false);
                segmentedItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
                root.applyCss();
                assertEquals(expressive.tokens().shapeTokens().large(), segmentedItem.getContainerShape(), 0.0001);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that lists expose item selection policies.
    @Test
    void listGroupsItemsAndKeepsSelectionPolicy() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3ListPane list = listPane(first, new M3Divider(), second, third);

        applyCss(list);

        assertInstanceOf(M3ListPaneSkin.class, list.getSkin());
        assertEquals(M3SelectionMode.NONE, list.getSelectionMode());
        first.fire();

        assertTrue(list.getSelectedItems().isEmpty());
        assertNull(list.getSelectedItem());

        list.setSelectionMode(M3SelectionMode.SINGLE);
        first.fire();
        second.fire();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, list.getSelectedItem());
        assertEquals(java.util.List.of(second), list.getSelectedItems());
        assertEquals(2, list.getSelectedIndex());
        assertThrows(UnsupportedOperationException.class, () -> list.getSelectedItems().add(third));
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(1));

        list.selectNext();

        assertEquals(third, list.getSelectedItem());
        assertEquals(3, list.getSelectedIndex());

        list.selectPrevious();

        assertEquals(second, list.getSelectedItem());
        assertEquals(2, list.getSelectedIndex());

        list.selectLast();

        assertEquals(third, list.getSelectedItem());
        assertEquals(3, list.getSelectedIndex());

        list.setAllowEmptySelection(false);
        list.clearSelection();

        assertEquals(third, list.getSelectedItem());
        assertTrue(third.isSelected());

        third.setSelected(false);

        assertEquals(third, list.getSelectedItem());
        assertTrue(third.isSelected());

        list.getItems().remove(third);

        assertFalse(third.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first), list.getSelectedItems());
        assertEquals(0, list.getSelectedIndex());
    }

    /// Verifies that static lists reject disabled and hidden selection targets from every selection entry point.
    @Test
    void listPaneSelectionRejectsDisabledAndHiddenItems() {
        M3ListItem disabled = new M3ListItem("Disabled");
        M3ListItem hidden = new M3ListItem("Hidden");
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        disabled.setDisable(true);
        hidden.setVisible(false);
        M3ListPane list = listPane(disabled, hidden, first, second);
        list.setSelectionMode(M3SelectionMode.SINGLE);
        list.setAllowEmptySelection(false);

        assertEquals(first, list.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> list.select(disabled));
        assertThrows(IllegalArgumentException.class, () -> list.select(hidden));
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(1));

        disabled.setSelected(true);
        hidden.setSelected(true);

        assertFalse(disabled.isSelected());
        assertFalse(hidden.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(List.of(first), list.getSelectedItems());

        first.setDisable(true);

        assertFalse(first.isSelected());
        assertEquals(second, list.getSelectedItem());
        assertEquals(List.of(second), list.getSelectedItems());

        second.setVisible(false);

        assertFalse(second.isSelected());
        assertNull(list.getSelectedItem());
        assertTrue(list.getSelectedItems().isEmpty());
    }

    /// Verifies that list section headers behave as non-selectable list content.
    @Test
    void listSectionHeadersAreNonSelectableContent() {
        M3ListSectionHeader primaryHeader = new M3ListSectionHeader("Primary");
        M3ListSectionHeader secondaryHeader = new M3ListSectionHeader("Secondary");
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListPane list = listPane(primaryHeader, first, new M3Divider(), secondaryHeader, second);

        list.setSelectionMode(M3SelectionMode.SINGLE);
        list.selectFirst();

        assertEquals(first, list.getSelectedItem());
        assertEquals(1, list.getSelectedIndex());
        assertFalse(primaryHeader.isFocusTraversable());
        assertFalse(secondaryHeader.isFocusTraversable());
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> list.selectIndex(3));

        list.selectLast();

        assertEquals(second, list.getSelectedItem());
        assertEquals(4, list.getSelectedIndex());
        assertEquals(5, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(primaryHeader, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(secondaryHeader, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 3));
    }

    /// Verifies that list section headers use list typography and spacing tokens.
    @Test
    void listSectionHeaderUsesListStylesheet() {
        M3ListSectionHeader header = new M3ListSectionHeader("Pinned");

        applyCss(header);

        assertEquals("Pinned", header.getText());
        assertTrue(header.getStyleClass().contains("m3-list-section-header"));
        assertTrue(header.getStyleClass().contains("m3-label-large-text"));
        assertEquals(AccessibleRole.TEXT, header.getAccessibleRole());
        assertInstanceOf(M3TextSkin.class, header.getSkin());
        assertEquals(48.0, header.prefHeight(320.0), 0.0001);
    }

    /// Verifies that lists can use multiple selected items.
    @Test
    void listCanUseMultipleSelection() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3ListPane list = listPane(first, second, third);

        list.setSelectionMode(M3SelectionMode.MULTIPLE);
        first.fire();
        third.fire();

        assertTrue(first.isSelected());
        assertTrue(third.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first, third), list.getSelectedItems());
        assertEquals(0, list.getSelectedIndex());

        first.fire();

        assertFalse(first.isSelected());
        assertEquals(java.util.List.of(third), list.getSelectedItems());
        assertEquals(2, list.getSelectedIndex());

        list.selectIndex(1);

        assertEquals(java.util.List.of(second, third), list.getSelectedItems());
        assertEquals(1, list.getSelectedIndex());
    }

    /// Verifies that lists collapse multiple selected items when switching to single selection.
    @Test
    void listCollapsesMultipleSelectionWhenModeChanges() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3ListPane list = listPane(first, second, third);

        list.setSelectionMode(M3SelectionMode.MULTIPLE);
        first.setSelected(true);
        second.setSelected(true);
        third.setSelected(true);

        list.setSelectionMode(M3SelectionMode.SINGLE);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertFalse(third.isSelected());
        assertEquals(first, list.getSelectedItem());
        assertEquals(java.util.List.of(first), list.getSelectedItems());
    }

    /// Verifies that static list-style containers support page keyboard navigation.
    @Test
    void listStyleContainersSupportPageKeyboardNavigation() {
        M3ListItem listFirst = new M3ListItem("First");
        M3ListItem listDisabled = new M3ListItem("Disabled");
        M3ListItem listHidden = new M3ListItem("Hidden");
        M3ListItem listFourth = new M3ListItem("Fourth");
        M3ListItem listFifth = new M3ListItem("Fifth");
        M3ListItem listSixth = new M3ListItem("Sixth");
        listDisabled.setDisable(true);
        listHidden.setVisible(false);
        resizeRow(listFirst);
        resizeRow(listDisabled);
        resizeRow(listHidden);
        resizeRow(listFourth);
        resizeRow(listFifth);
        resizeRow(listSixth);
        M3ListPane listPane = listPane(
                listFirst,
                listDisabled,
                listHidden,
                listFourth,
                listFifth,
                listSixth
        );
        listPane.setSelectionMode(M3SelectionMode.SINGLE);
        listPane.resize(240.0, 120.0);

        listPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
        assertSame(listFirst, listPane.getSelectedItem());

        listPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
        assertSame(listSixth, listPane.getSelectedItem());

        listPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
        assertSame(listSixth, listPane.getSelectedItem());

        listPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));
        assertSame(listFirst, listPane.getSelectedItem());

        M3MenuItem menuFirst = new M3MenuItem("First");
        M3MenuItem menuDisabled = new M3MenuItem("Disabled");
        M3MenuItem menuHidden = new M3MenuItem("Hidden");
        M3MenuItem menuFourth = new M3MenuItem("Fourth");
        M3MenuItem menuFifth = new M3MenuItem("Fifth");
        M3MenuItem menuSixth = new M3MenuItem("Sixth");
        menuDisabled.setDisable(true);
        menuHidden.setVisible(false);
        resizeRow(menuFirst);
        resizeRow(menuDisabled);
        resizeRow(menuHidden);
        resizeRow(menuFourth);
        resizeRow(menuFifth);
        resizeRow(menuSixth);
        M3Menu menu = new M3Menu(menuFirst, menuDisabled, menuHidden, menuFourth, menuFifth, menuSixth);
        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.resize(240.0, 120.0);

        menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
        assertSame(menuFirst, menu.getSelectedItem());

        menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
        assertSame(menuSixth, menu.getSelectedItem());

        menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));
        assertSame(menuFirst, menu.getSelectedItem());

        M3ListItem drawerFirst = new M3ListItem("First");
        M3ListItem drawerChildFirst = new M3ListItem("Child first");
        M3ListItem drawerChildDisabled = new M3ListItem("Disabled child");
        M3ListItem drawerChildThird = new M3ListItem("Child third");
        M3ListItem drawerLast = new M3ListItem("Last");
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
        group.setExpanded(true);
        drawerChildDisabled.setDisable(true);
        resizeRow(drawerFirst);
        resizeRow(group.getHeaderItem());
        resizeRow(drawerChildFirst);
        resizeRow(drawerChildDisabled);
        resizeRow(drawerChildThird);
        resizeRow(drawerLast);
        group.getItems().addAll(drawerChildFirst, drawerChildDisabled, drawerChildThird);
        M3NavigationDrawer drawer = navigationDrawer(drawerFirst, group, drawerLast);
        drawer.resize(240.0, 120.0);

        assertSame(drawerFirst, drawer.getSelectedItem());

        drawer.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
        assertSame(drawerChildThird, drawer.getSelectedItem());

        drawer.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));
        assertSame(drawerFirst, drawer.getSelectedItem());
    }

    /// Verifies that list item skins expose bounded ripple feedback.
    @Test
    void listItemSkinPlaysBoundedRippleOnActivation() {
        M3ListItem listItem = new M3ListItem("Headline");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(220.0, 56.0);
        listItem.layout();

        assertInstanceOf(Region.class, listItem.lookup(".m3-state-layer"));
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));

        assertTrue(lookupRegion(listItem, ".m3-ripple").getOpacity() > 0.0);
    }

    /// Verifies that one-line list item text is centered within its allocated row height.
    @Test
    void listItemSkinCentersOneLineText() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setLeading(new Label("H"));
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(220.0, 56.0);
        listItem.layout();

        Node textBoxNode = listItem.lookup(".m3-list-item-text");
        assertInstanceOf(VBox.class, textBoxNode);
        VBox textBox = (VBox) textBoxNode;
        Node headline = listItem.lookup(".m3-list-item-headline");
        assertInstanceOf(Label.class, headline);

        assertEquals(Pos.CENTER_LEFT, textBox.getAlignment());
        assertTrue(headline.getLayoutY() > 0.0);
    }

    /// Verifies that full list item shapes are resolved to the allocated row bounds.
    @Test
    void listItemSkinClampsFullContainerShape() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setSelected(true);
        listItem.setStyle("-m3-container-shape: 999px;");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        listItem.resize(220.0, 56.0);
        listItem.layout();

        assertRegionRadii(listItemContainer(listItem), 28.0, 28.0, 28.0, 28.0);
        assertRegionRadii(listItemSelectionContainer(listItem), 28.0, 28.0, 28.0, 28.0);
    }

    /// Verifies that list and drawer selected containers animate incoming and outgoing states.
    @Tier2Test
    @Test
    void listItemSelectionContainerAnimationsRenderIntermediateAndFinalStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> listFirstReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> listSecondReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> drawerFirstReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> drawerSecondReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> {
                M3ListItem listFirst = new M3ListItem("Inbox");
                M3ListItem listSecond = new M3ListItem("Archive");
                M3ListPane list = listPane(listFirst, listSecond);
                list.setSelectionMode(M3SelectionMode.SINGLE);
                list.select(listFirst);
                list.setPrefWidth(280.0);

                M3ListItem drawerFirst = new M3ListItem("Home");
                M3ListItem drawerSecond = new M3ListItem("Search");
                M3NavigationDrawer drawer = navigationDrawer(drawerFirst, drawerSecond);
                drawer.select(drawerFirst);
                drawer.setPrefWidth(320.0);

                VBox root = new VBox(18.0, list, drawer);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                Scene scene = new Scene(root, 380.0, 260.0);
                Stage stage = new Stage();

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(380.0, 260.0);
                root.layout();

                stageReference.set(stage);
                rootReference.set(root);
                listFirstReference.set(listFirst);
                listSecondReference.set(listSecond);
                drawerFirstReference.set(drawerFirst);
                drawerSecondReference.set(drawerSecond);
                list.select(listSecond);
                drawer.select(drawerSecond);
            });

            FxTestUtils.runOnFxThreadWhen(
                    () -> listAndDrawerSelectionContainersAreTransitioning(
                            rootReference,
                            listFirstReference,
                            listSecondReference,
                            drawerFirstReference,
                            drawerSecondReference
                    ),
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3ListItem listFirst = Objects.requireNonNull(listFirstReference.get(), "listFirst");
                        M3ListItem listSecond = Objects.requireNonNull(listSecondReference.get(), "listSecond");
                        M3ListItem drawerFirst = Objects.requireNonNull(drawerFirstReference.get(), "drawerFirst");
                        M3ListItem drawerSecond = Objects.requireNonNull(drawerSecondReference.get(), "drawerSecond");
                        Region outgoingListSelection = listItemSelectionContainer(listFirst);
                        Region incomingListSelection = listItemSelectionContainer(listSecond);
                        Region outgoingDrawerSelection = listItemSelectionContainer(drawerFirst);
                        Region incomingDrawerSelection = listItemSelectionContainer(drawerSecond);

                        assertBetween(outgoingListSelection.getOpacity(), 0.0, 1.0, "outgoing list selection opacity");
                        assertBetween(incomingListSelection.getOpacity(), 0.25, 1.0, "incoming list selection opacity");
                        assertBetween(
                                outgoingDrawerSelection.getOpacity(),
                                0.0,
                                1.0,
                                "outgoing drawer selection opacity"
                        );
                        assertBetween(
                                incomingDrawerSelection.getOpacity(),
                                0.25,
                                1.0,
                                "incoming drawer selection opacity"
                        );
                        assertBetween(incomingListSelection.getScaleX(), 0.96, 1.0, "incoming list selection scale");
                        assertBetween(
                                incomingDrawerSelection.getScaleX(),
                                0.96,
                                1.0,
                                "incoming drawer selection scale"
                        );
                        assertBetween(
                                incomingListSelection.getScaleY(),
                                0.96,
                                1.0,
                                "incoming list selection vertical scale"
                        );
                        assertBetween(
                                incomingDrawerSelection.getScaleY(),
                                0.96,
                                1.0,
                                "incoming drawer selection vertical scale"
                        );

                        WritableImage image = snapshotImageOnFxThread(root);
                        assertSnapshotNodeContainsContrast(image, incomingListSelection, Color.WHITE, 0.03);
                        assertSnapshotNodeContainsContrast(image, incomingDrawerSelection, Color.WHITE, 0.03);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-list-drawer-selection-animation-frame.png"
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> listAndDrawerSelectionContainersHaveSettled(
                            rootReference,
                            listFirstReference,
                            listSecondReference,
                            drawerFirstReference,
                            drawerSecondReference
                    ),
                    () -> {
                    },
                    () -> {
                        M3ListItem listFirst = Objects.requireNonNull(listFirstReference.get(), "listFirst");
                        M3ListItem listSecond = Objects.requireNonNull(listSecondReference.get(), "listSecond");
                        M3ListItem drawerFirst = Objects.requireNonNull(drawerFirstReference.get(), "drawerFirst");
                        M3ListItem drawerSecond = Objects.requireNonNull(drawerSecondReference.get(), "drawerSecond");
                        Region outgoingListSelection = listItemSelectionContainer(listFirst);
                        Region incomingListSelection = listItemSelectionContainer(listSecond);
                        Region outgoingDrawerSelection = listItemSelectionContainer(drawerFirst);
                        Region incomingDrawerSelection = listItemSelectionContainer(drawerSecond);

                        assertEquals(0.0, outgoingListSelection.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingListSelection.getOpacity(), 0.0001);
                        assertEquals(0.0, outgoingDrawerSelection.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingDrawerSelection.getOpacity(), 0.0001);
                        assertEquals(0.96, outgoingListSelection.getScaleX(), 0.0001);
                        assertEquals(1.0, incomingListSelection.getScaleX(), 0.0001);
                        assertEquals(0.96, outgoingDrawerSelection.getScaleX(), 0.0001);
                        assertEquals(1.0, incomingDrawerSelection.getScaleX(), 0.0001);
                        assertEquals(0.96, outgoingListSelection.getScaleY(), 0.0001);
                        assertEquals(1.0, incomingListSelection.getScaleY(), 0.0001);
                        assertEquals(0.96, outgoingDrawerSelection.getScaleY(), 0.0001);
                        assertEquals(1.0, incomingDrawerSelection.getScaleY(), 0.0001);
                    }
            );
        } finally {
            closeObservableMotionWindow(stageReference, rootReference);
        }
    }

    /// Verifies that list controls inherit dark scene tokens instead of declaring standalone fallback tokens.
    @Test
    void darkThemeListControlsInheritSceneTextTokens() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem standaloneItem = new M3ListItem("Standalone row");
            M3ListView<String> listView = listView("Virtual row");
            listView.setCellFactory(view -> listCell(view, M3ListItem::setHeadlineText));
            listView.setPrefSize(280.0, 72.0);
            M3NavigationDrawer drawer = new M3NavigationDrawer();
            M3ListItem drawerItem = new M3ListItem("Drawer row");
            drawer.getItems().add(drawerItem);
            drawer.setPrefWidth(320.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setHeadline("Dialog title");
            dialogPane.setSupportingText("Dialog body");
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            dialogPane.getActions().add(okAction);

            VBox root = new VBox(16.0, standaloneItem, listView, drawer, dialogPane);
            root.setStyle("-fx-background-color: -m3-color-surface; -fx-padding: 24px;");
            Scene scene = new Scene(root, 520.0, 520.0);
            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            ));
            root.applyCss();
            root.resize(520.0, 520.0);
            root.layout();
            listView.applyCss();
            listView.layout();

            assertDarkThemeLabelFill(listItemHeadlineLabel(standaloneItem), "standalone list item headline");
            assertDarkThemeLabelFill(listItemHeadlineLabel(drawerItem), "navigation drawer item headline");

            M3ListCell<?> cell = assertInstanceOf(
                    M3ListCell.class,
                    listView.lookup("." + "m3-list-view-cell"),
                    "virtualized list cell"
            );
            M3ListItem virtualItem = Objects.requireNonNull(cell.getListItem(), "virtualized list item");
            assertDarkThemeLabelFill(listItemHeadlineLabel(virtualItem), "virtualized list item headline");

            Label dialogHeader = assertInstanceOf(
                    Label.class,
                    dialogPane.lookup(".header-panel .label"),
                    "embedded dialog header label"
            );
            Label dialogContent = assertInstanceOf(
                    Label.class,
                    dialogPane.lookup(".content-label"),
                    "embedded dialog content label"
            );
            assertDarkThemeLabelFill(dialogHeader, "embedded dialog header label");
            assertDarkThemeLabelFill(dialogContent, "embedded dialog content label");
        });
    }

    /// Returns whether list and drawer selected containers are rendering intermediate transitions.
    private static boolean listAndDrawerSelectionContainersAreTransitioning(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3ListItem> outgoingListReference,
            AtomicReference<@Nullable M3ListItem> incomingListReference,
            AtomicReference<@Nullable M3ListItem> outgoingDrawerReference,
            AtomicReference<@Nullable M3ListItem> incomingDrawerReference
    ) {
        @Nullable M3ListItem outgoingList = outgoingListReference.get();
        @Nullable M3ListItem incomingList = incomingListReference.get();
        @Nullable M3ListItem outgoingDrawer = outgoingDrawerReference.get();
        @Nullable M3ListItem incomingDrawer = incomingDrawerReference.get();
        if (outgoingList == null
                || incomingList == null
                || outgoingDrawer == null
                || incomingDrawer == null
                || observableRootUnavailable(rootReference)) {
            return false;
        }

        return listSelectionPairIsTransitioning(outgoingList, incomingList)
                && listSelectionPairIsTransitioning(outgoingDrawer, incomingDrawer);
    }

    /// Returns whether list and drawer selected containers have settled after a selection change.
    private static boolean listAndDrawerSelectionContainersHaveSettled(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3ListItem> outgoingListReference,
            AtomicReference<@Nullable M3ListItem> incomingListReference,
            AtomicReference<@Nullable M3ListItem> outgoingDrawerReference,
            AtomicReference<@Nullable M3ListItem> incomingDrawerReference
    ) {
        @Nullable M3ListItem outgoingList = outgoingListReference.get();
        @Nullable M3ListItem incomingList = incomingListReference.get();
        @Nullable M3ListItem outgoingDrawer = outgoingDrawerReference.get();
        @Nullable M3ListItem incomingDrawer = incomingDrawerReference.get();
        if (outgoingList == null
                || incomingList == null
                || outgoingDrawer == null
                || incomingDrawer == null
                || observableRootUnavailable(rootReference)) {
            return false;
        }

        return listSelectionPairHasSettled(outgoingList, incomingList)
                && listSelectionPairHasSettled(outgoingDrawer, incomingDrawer);
    }

    /// Returns whether one outgoing and incoming list-selection pair is in an intermediate transition.
    private static boolean listSelectionPairIsTransitioning(M3ListItem outgoing, M3ListItem incoming) {
        Region outgoingSelection = listItemSelectionContainer(outgoing);
        Region incomingSelection = listItemSelectionContainer(incoming);
        return isBetweenExclusive(outgoingSelection.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(incomingSelection.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(incomingSelection.getScaleX(), 0.96, 1.0)
                && isBetweenExclusive(incomingSelection.getScaleY(), 0.96, 1.0);
    }

    /// Returns whether one outgoing and incoming list-selection pair has reached its final state.
    private static boolean listSelectionPairHasSettled(M3ListItem outgoing, M3ListItem incoming) {
        Region outgoingSelection = listItemSelectionContainer(outgoing);
        Region incomingSelection = listItemSelectionContainer(incoming);
        return Math.abs(outgoingSelection.getOpacity()) < 0.0001
                && Math.abs(incomingSelection.getOpacity() - 1.0) < 0.0001
                && Math.abs(outgoingSelection.getScaleX() - 0.96) < 0.0001
                && Math.abs(incomingSelection.getScaleX() - 1.0) < 0.0001
                && Math.abs(outgoingSelection.getScaleY() - 0.96) < 0.0001
                && Math.abs(incomingSelection.getScaleY() - 1.0) < 0.0001;
    }

    /// Verifies a list item's line count property and pseudo-class state.
    private static void assertListItemLineCount(M3ListItem listItem, M3ListItemLineCount lineCount) {
        assertEquals(lineCount, listItem.getLineCount());
        assertEquals(lineCount, listItem.lineCountProperty().get());
        assertEquals(lineCount.getLineCount(), listItem.getLineCount().getLineCount());
        assertEquals(lineCount == M3ListItemLineCount.ONE_LINE,
                listItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("one-line")));
        assertEquals(lineCount == M3ListItemLineCount.TWO_LINE,
                listItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("two-line")));
        assertEquals(lineCount == M3ListItemLineCount.THREE_LINE,
                listItem.getPseudoClassStates().contains(PseudoClass.getPseudoClass("three-line")));
    }

    /// Creates a media node used by list item visual snapshot tests.
    private static StackPane visualListMedia(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: -m3-color-on-tertiary-container; -fx-font-weight: 700;");
        StackPane media = new StackPane(label);
        media.setStyle("-fx-background-color: -m3-color-tertiary-container; -fx-background-radius: 4px;");
        return media;
    }
}
