// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies shared control contracts, nested input, and motion settings.
@NotNullByDefault
final class M3ControlCompositionTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }
    /// The style class shared by controls in the button family.
    private static final String BUTTON_BASE_STYLE_CLASS = "m3-button-base";

    /// The default style class for icon buttons.
    private static final String ICON_BUTTON_STYLE_CLASS = "m3-icon-button";

    /// The default style class for menu buttons.
    private static final String MENU_BUTTON_STYLE_CLASS = "m3-menu-button";

    /// Verifies that item-container skins keep their internal rows aligned to the logical start edge.
    @Test
    void itemContainerSkinsAlignToLogicalStartWhenOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3ButtonGroup buttonGroup = buttonGroup(new M3Button("One"), new M3Button("Two"));
            M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(
                    new M3IconToggleButton("A"),
                    new M3IconToggleButton("B")
            );
            M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(
                    new M3SegmentedButton("Day"),
                    new M3SegmentedButton("Week")
            );
            M3ChipGroup chipGroup = chipGroup(new M3AssistChip("Input"), new M3AssistChip("Filter"));
            M3TabBar tabBar = tabBar(new M3Tab("Overview"), new M3Tab("Details"));
            VBox root = new VBox(buttonGroup, iconGroup, segmentedGroup, chipGroup, tabBar);
            Scene scene = new Scene(root, 480.0, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            HBox iconContainer = firstDescendantOfType(iconGroup, HBox.class);
            HBox segmentedContainer = firstDescendantOfType(segmentedGroup, HBox.class);
            FlowPane chipContainer = firstDescendantOfType(chipGroup, FlowPane.class);
            HBox tabContainer = firstDescendantOfType(tabBar, HBox.class);

            assertTrue(buttonGroup.getItems().get(0)
                    .localToScene(buttonGroup.getItems().get(0).getBoundsInLocal()).getMinX()
                    < buttonGroup.getItems().get(1)
                    .localToScene(buttonGroup.getItems().get(1).getBoundsInLocal()).getMinX());
            assertEquals(Pos.CENTER_LEFT, iconContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, segmentedContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, chipContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, tabContainer.getAlignment());
            assertFirstChildAtLogicalStart(iconContainer, false);
            assertFirstChildAtLogicalStart(segmentedContainer, false);
            assertFirstChildAtLogicalStart(chipContainer, false);
            assertFirstChildAtLogicalStart(tabContainer, false);

            buttonGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            iconGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            segmentedGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            chipGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            tabBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();

            assertTrue(buttonGroup.getItems().get(0)
                    .localToScene(buttonGroup.getItems().get(0).getBoundsInLocal()).getMinX()
                    > buttonGroup.getItems().get(1)
                    .localToScene(buttonGroup.getItems().get(1).getBoundsInLocal()).getMinX());
            assertEquals(Pos.CENTER_LEFT, iconContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, segmentedContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, chipContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, tabContainer.getAlignment());
            assertFirstChildAtLogicalStart(iconContainer, true);
            assertFirstChildAtLogicalStart(segmentedContainer, true);
            assertFirstChildAtLogicalStart(chipContainer, true);
            assertFirstChildAtLogicalStart(tabContainer, true);

            buttonGroup.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            iconGroup.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            segmentedGroup.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            chipGroup.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            tabBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            root.layout();

            assertTrue(buttonGroup.getItems().get(0)
                    .localToScene(buttonGroup.getItems().get(0).getBoundsInLocal()).getMinX()
                    < buttonGroup.getItems().get(1)
                    .localToScene(buttonGroup.getItems().get(1).getBoundsInLocal()).getMinX());
            assertEquals(Pos.CENTER_LEFT, iconContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, segmentedContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, chipContainer.getAlignment());
            assertEquals(Pos.CENTER_LEFT, tabContainer.getAlignment());
            assertFirstChildAtLogicalStart(iconContainer, false);
            assertFirstChildAtLogicalStart(segmentedContainer, false);
            assertFirstChildAtLogicalStart(chipContainer, false);
            assertFirstChildAtLogicalStart(tabContainer, false);
        });
    }

    /// Verifies that composite control containers align to the logical start edge when orientation changes.
    @Test
    void compositeControlContainersAlignToLogicalStartWhenOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3CheckBox checkBox = createCheckBox("Checkbox", true);
            M3RadioButton radioButton = createRadioButton("Radio", true);
            M3Switch switchControl = createSwitch("Switch", true);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().setAll(new M3Button("Filter"));
            M3SplitButton splitButton = createSplitButton(
                    "Export",
                    M3ButtonVariant.TONAL,
                    new M3MenuItem("PDF")
            );
            VBox root = new VBox(checkBox, radioButton, switchControl, searchBar, splitButton);
            Scene scene = new Scene(root, 520.0, 320.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            assertCompositeControlContainerAlignment(
                    checkBox, radioButton, switchControl, searchBar, splitButton, Pos.CENTER_LEFT, false
            );

            checkBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            radioButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            switchControl.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            searchBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            splitButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();

            assertCompositeControlContainerAlignment(
                    checkBox, radioButton, switchControl, searchBar, splitButton, Pos.CENTER_LEFT, true
            );

            checkBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            radioButton.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            switchControl.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            searchBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            splitButton.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            root.layout();

            assertCompositeControlContainerAlignment(
                    checkBox, radioButton, switchControl, searchBar, splitButton, Pos.CENTER_LEFT, false
            );
        });
    }

    /// Verifies that public mutable control lists reject null direct mutations without partial insertion.
    @Test
    void publicMutableControlListsRejectNullDirectMutations() {
        FxTestUtils.runOnFxThread(() -> {
            assertMutableListRejectsNull(
                    new M3Toolbar().getItems(),
                    new M3Button("Toolbar one"),
                    new M3Button("Toolbar two")
            );
            assertMutableListRejectsNull(
                    new M3DialogPane().getActions(),
                    new M3Button("Dialog one"),
                    new M3Button("Dialog two")
            );
            assertMutableListRejectsNull(
                    topAppBar().getActions(),
                    new M3Button("A"),
                    new M3Button("B")
            );
            assertMutableListRejectsNull(
                    bottomAppBar().getActions(),
                    new M3Button("A"),
                    new M3Button("B")
            );
            assertMutableListRejectsNull(
                    new M3DatePickerDialog().getPresets(),
                    new M3DatePreset("Today", LocalDate.of(2026, 5, 1)),
                    new M3DatePreset("Tomorrow", LocalDate.of(2026, 5, 2))
            );
            assertMutableListRejectsNull(
                    new M3DatePickerField().getPresets(),
                    new M3DatePreset("Today", LocalDate.of(2026, 5, 1)),
                    new M3DatePreset("Tomorrow", LocalDate.of(2026, 5, 2))
            );
            assertMutableListRejectsNull(
                    new M3DateRangePickerDialog().getPresets(),
                    new M3DateRangePreset("Sprint", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 14)),
                    new M3DateRangePreset("Release", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 14))
            );
            assertMutableListRejectsNull(
                    new M3DateRangePickerField().getPresets(),
                    new M3DateRangePreset("Sprint", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 14)),
                    new M3DateRangePreset("Release", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 14))
            );
            assertMutableListRejectsNull(
                    new M3TimePickerDialog().getPresets(),
                    new M3TimePreset("Morning", LocalTime.of(9, 0)),
                    new M3TimePreset("Evening", LocalTime.of(18, 0))
            );
            assertMutableListRejectsNull(
                    new M3TimePickerField().getPresets(),
                    new M3TimePreset("Morning", LocalTime.of(9, 0)),
                    new M3TimePreset("Evening", LocalTime.of(18, 0))
            );
            assertMutableListRejectsNull(
                    banner().getActions(),
                    new M3Button("Dismiss"),
                    new M3Button("Retry")
            );
            assertMutableListRejectsNull(
                    bottomSheet().getActions(),
                    new M3Button("Cancel"),
                    new M3Button("Apply")
            );
            assertMutableListRejectsNull(
                    sideSheet().getActions(),
                    new M3Button("Cancel"),
                    new M3Button("Apply")
            );
            assertMutableListRejectsNull(
                    new M3Surface().getContent(),
                    new M3Text("Surface one"),
                    new M3Text("Surface two")
            );
            assertMutableListRejectsNull(
                    new M3FormPane().getItems(),
                    formSection("First"),
                    formSection("Second")
            );
            assertMutableListRejectsNull(
                    formSection("Section").getContent(),
                    new M3FormRow("First", new M3TextField()),
                    new M3FormRow("Second", new M3TextField())
            );
            assertMutableListRejectsNull(
                    new M3TextInputLayout().getValidators(),
                    M3TextInputValidators.required("Required"),
                    M3TextInputValidators.maxLength(8, "Too long")
            );
            assertMutableListRejectsNull(
                    new M3ButtonGroup().getItems(),
                    new M3Button("One"),
                    new M3Button("Two")
            );
            assertMutableListRejectsNull(
                    new M3FabMenu().getItems(),
                    new M3FloatingActionButton("A"),
                    new M3FloatingActionButton("B")
            );
            assertMutableListRejectsNull(
                    new M3SegmentedButtonGroup().getItems(),
                    new M3SegmentedButton("Day"),
                    new M3SegmentedButton("Week")
            );
            assertMutableListRejectsNull(
                    new M3ChipGroup().getItems(),
                    new M3AssistChip("Input"),
                    new M3AssistChip("Filter")
            );
            assertMutableListRejectsNull(
                    new M3IconToggleButtonGroup().getItems(),
                    new M3IconToggleButton("B"),
                    new M3IconToggleButton("I")
            );
            assertMutableListRejectsNull(
                    new M3NavigationBar().getItems(),
                    new M3NavigationItem("Home"),
                    new M3NavigationItem("Search")
            );
            assertMutableListRejectsNull(
                    new M3NavigationRail().getItems(),
                    new M3NavigationItem("Home"),
                    new M3NavigationItem("Search")
            );
            assertMutableListRejectsNull(
                    new M3NavigationDrawer().getItems(),
                    new M3ListItem("Inbox"),
                    new M3ListItem("Archive")
            );
            assertMutableListRejectsNull(
                    new M3ListPane().getItems(),
                    new M3ListItem("Inbox"),
                    new M3ListItem("Archive")
            );
            assertMutableListRejectsNull(
                    new M3ListView<String>().getItems(),
                    "First",
                    "Second"
            );
            assertMutableListRejectsNull(
                    new M3Carousel().getItems(),
                    new M3Card(new M3Text("One")),
                    new M3Card(new M3Text("Two"))
            );
            assertMutableListRejectsNull(
                    new M3TabBar().getTabs(),
                    new M3Tab("Overview"),
                    new M3Tab("Details")
            );
            assertMutableListRejectsNull(
                    new M3Menu().getItems(),
                    new M3MenuItem("Open"),
                    new M3MenuItem("Save")
            );
            assertMutableListRejectsNull(
                    new M3MenuButton("Menu").getItems(),
                    new M3MenuItem("Open"),
                    new M3MenuItem("Save")
            );
            assertMutableListRejectsNull(
                    richTooltip().getActions(),
                    new M3Button("Action"),
                    new M3Button("More")
            );
            assertMutableListRejectsNull(
                    new M3SubMenuItem("Move to").getItems(),
                    new M3MenuItem("Archive"),
                    new M3MenuItem("Inbox")
            );
            assertMutableListRejectsNull(
                    new M3SplitButton("More").getItems(),
                    new M3MenuItem("Open"),
                    new M3MenuItem("Save")
            );
            assertMutableListRejectsNull(
                    new M3SearchBar().getTrailingActions(),
                    new M3Button("A"),
                    new M3Button("B")
            );
            assertMutableListRejectsNull(
                    searchView().getResults(),
                    new M3ListItem("First"),
                    new M3ListItem("Second")
            );
            assertMutableListRejectsNull(
                    searchView().getTrailingActions(),
                    new M3Button("A"),
                    new M3Button("B")
            );
        });
    }

    /// Verifies that batch constructors reject null arrays and elements before storing content.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void publicBatchConstructorsRejectNullInputs() {
        M3MenuItem open = new M3MenuItem("Open");
        M3TextInputLayout input = new M3TextInputLayout(new M3TextField("Name"));

        assertThrows(NullPointerException.class, () -> new M3Menu((Node[]) null));
        assertThrows(NullPointerException.class, () -> new M3Menu(open, null));
        assertThrows(NullPointerException.class, () -> new M3MenuButton("Menu", (Node[]) null));
        assertThrows(NullPointerException.class, () -> new M3MenuButton("Menu", open, null));
        assertThrows(NullPointerException.class, () -> new M3SubMenuItem("Move to", (Node[]) null));
        assertThrows(NullPointerException.class, () -> new M3SubMenuItem("Move to", open, null));
        assertThrows(NullPointerException.class, () -> new M3FormValidator((M3TextInputLayout[]) null));
        assertThrows(NullPointerException.class, () -> new M3FormValidator(input, null));
        assertThrows(IllegalArgumentException.class, () -> new M3FormValidator(input, input));
    }

    /// Verifies one mutable public list rejects null insertion paths before mutating content.
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private static <T> void assertMutableListRejectsNull(ObservableList<T> list, T existing, T replacement) {
        list.add(existing);

        assertThrows(NullPointerException.class, () -> list.add(null));
        assertThrows(NullPointerException.class, () -> list.addAll(replacement, null));
        assertThrows(NullPointerException.class, () -> list.set(0, null));
        assertThrows(NullPointerException.class, () -> list.setAll(replacement, null));

        assertEquals(1, list.size());
        assertSame(existing, list.get(0));
    }

    /// Verifies that scroll styling is explicit for JavaFX controls and automatic for M3FX-owned viewports.
    @Test
    void materialScrollbarsInstallWithoutGlobalJavaFxOverrides() {
        FxTestUtils.runOnFxThread(() -> {
            VBox styledContent = new VBox();
            VBox plainContent = new VBox();
            for (int index = 0; index < 24; index++) {
                styledContent.getChildren().add(new Label("Styled row " + index));
                plainContent.getChildren().add(new Label("Plain row " + index));
            }

            ScrollPane styled = new ScrollPane(styledContent);
            styled.setPrefSize(180.0, 120.0);
            M3ScrollPane.style(styled);
            M3ScrollPane.style(styled);

            ScrollPane plain = new ScrollPane(plainContent);
            plain.setPrefSize(180.0, 120.0);

            M3TextArea textArea = new M3TextArea("One\nTwo\nThree\nFour\nFive\nSix\nSeven\nEight");
            textArea.setPrefSize(220.0, 96.0);

            M3ListView<String> listView = new M3ListView<>();
            for (int index = 0; index < 32; index++) {
                listView.getItems().add("List row " + index);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(220.0, 120.0);

            HBox root = new HBox(12.0, styled, plain, textArea, listView);
            Scene scene = new Scene(root, 880.0, 180.0);
            M3Theme theme = M3Theme.defaultTheme();
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, theme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertTrue(styled.getStyleClass().contains("m3-scroll-pane"));
                assertEquals(1, styled.getStylesheets().stream()
                        .filter(stylesheet -> stylesheet.endsWith("/scroll.css"))
                        .count());
                assertFalse(plain.getStyleClass().contains("m3-scroll-pane"));
                assertTrue(plain.getStylesheets().stream()
                        .noneMatch(stylesheet -> stylesheet.endsWith("/scroll.css")));

                ScrollBar styledVerticalBar = styled.lookupAll(".scroll-bar").stream()
                        .filter(ScrollBar.class::isInstance)
                        .map(ScrollBar.class::cast)
                        .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("styled vertical scrollbar"));
                Region styledThumb = assertInstanceOf(
                        Region.class,
                        styledVerticalBar.lookup(".thumb"),
                        "styled scrollbar thumb"
                );
                assertEquals(16.0, styledVerticalBar.getPrefWidth(), 0.0001);
                assertEquals(0.48, styledThumb.getOpacity(), 0.0001);
                assertEquals(
                        theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY),
                        styledThumb.getBackground().getFills().get(0).getFill(),
                        "scrollbar surface tint"
                );

                styledVerticalBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
                root.applyCss();
                assertEquals(0.64, styledThumb.getOpacity(), 0.0001);
                styledVerticalBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
                root.applyCss();
                assertEquals(0.78, styledThumb.getOpacity(), 0.0001);
                styledVerticalBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
                styledVerticalBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);

                ScrollPane textAreaViewport = assertInstanceOf(
                        ScrollPane.class,
                        textArea.lookup(".scroll-pane"),
                        "text area viewport"
                );
                ScrollBar textAreaVerticalBar = textAreaViewport.lookupAll(".scroll-bar").stream()
                        .filter(ScrollBar.class::isInstance)
                        .map(ScrollBar.class::cast)
                        .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("text area vertical scrollbar"));
                Region textAreaThumb = assertInstanceOf(
                        Region.class,
                        textAreaVerticalBar.lookup(".thumb"),
                        "text area scrollbar thumb"
                );
                assertEquals(16.0, textAreaVerticalBar.getPrefWidth(), 0.0001);
                assertEquals(0.48, textAreaThumb.getOpacity(), 0.0001);
                assertEquals(
                        theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY),
                        textAreaThumb.getBackground().getFills().get(0).getFill(),
                        "text area scrollbar surface tint"
                );

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow"),
                        "list view virtual flow"
                );
                List<ScrollBar> listScrollBars = flow.lookupAll(".scroll-bar").stream()
                        .filter(ScrollBar.class::isInstance)
                        .map(ScrollBar.class::cast)
                        .toList();
                assertFalse(listScrollBars.isEmpty(), "list view should materialize internal scrollbars");
                assertTrue(listScrollBars.stream().allMatch(scrollBar ->
                        scrollBar.getStyleClass().contains("m3-scroll-bar")));
                assertTrue(listScrollBars.stream().allMatch(scrollBar ->
                        scrollBar.getStylesheets().stream().filter(stylesheet ->
                                stylesheet.endsWith("/scroll.css")).count() == 1));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that shared directional traversal preserves embedded text input editing focus.
    @Test
    void focusTraversalHelperDoesNotStealEmbeddedTextInputKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField embeddedEditor = new M3TextField("Edit");
            M3Button next = new M3Button("Next");
            M3Surface owner = surface(embeddedEditor, next);
            M3TextField outsideEditor = new M3TextField("Outside");
            VBox root = new VBox(8.0, owner, outsideEditor);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                embeddedEditor.requestFocus();
                KeyEvent embeddedEvent = targetedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT, embeddedEditor);
                assertFalse(M3FocusTraversal.handleHorizontalKeyFocus(
                        owner,
                        embeddedEvent,
                        M3FocusTraversal.focusTargets(owner.getContent())
                ));

                assertTrue(embeddedEditor.isFocused());
                assertFalse(next.isFocused());
                assertFalse(embeddedEvent.isConsumed());

                KeyEvent ownerEvent = targetedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT, owner);
                assertFalse(M3FocusTraversal.handleHorizontalKeyFocus(
                        owner,
                        ownerEvent,
                        M3FocusTraversal.focusTargets(owner.getContent())
                ));

                assertTrue(embeddedEditor.isFocused());
                assertFalse(next.isFocused());
                assertTrue(ownerEvent.isConsumed());

                outsideEditor.requestFocus();
                KeyEvent outsideEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
                assertTrue(M3FocusTraversal.handleHorizontalKeyFocus(
                        owner,
                        outsideEvent,
                        M3FocusTraversal.focusTargets(owner.getContent())
                ));

                assertTrue(embeddedEditor.isFocused());
                assertTrue(outsideEvent.isConsumed());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that shared directional traversal prefers exact child targets before ancestor containers.
    @Test
    void focusTraversalHelperPrefersExactTargetsBeforeAncestorContainers() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button content = new M3Button("Card content");
            M3Card card = new M3Card(content, M3CardVariant.FILLED);
            card.setOnAction(event -> {
            });
            M3Button outside = new M3Button("Outside");
            VBox root = new VBox(8.0, card, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 420.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                List<Node> targets = List.of(card, content);
                content.requestFocus();
                KeyEvent focusedEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);
                assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                        card,
                        focusedEvent,
                        targets,
                        true,
                        false,
                        -1,
                        false,
                        content
                ));
                assertTrue(card.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertTrue(focusedEvent.isConsumed());

                outside.requestFocus();
                KeyEvent targetEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);
                assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                        card,
                        targetEvent,
                        targets,
                        true,
                        false,
                        -1,
                        false,
                        content
                ));
                assertTrue(card.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertTrue(targetEvent.isConsumed());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that shared directional traversal rejects stale, duplicate, and unreachable candidates.
    @Test
    void focusTraversalHelperFiltersUnreachableAndDuplicateTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button hidden = new M3Button("Hidden");
            M3Button disabled = new M3Button("Disabled");
            M3Button second = new M3Button("Second");
            M3Button outside = new M3Button("Outside");
            hidden.setVisible(false);
            disabled.setDisable(true);
            VBox owner = new VBox(8.0, first, hidden, disabled, second);
            VBox root = new VBox(8.0, owner, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 420.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                List<Node> staleTargets = List.of(
                        first,
                        hidden,
                        disabled,
                        second,
                        second
                );
                first.requestFocus();
                KeyEvent forwardEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

                assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                        owner,
                        forwardEvent,
                        staleTargets,
                        true,
                        false,
                        -1,
                        true
                ));

                assertTrue(second.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertTrue(forwardEvent.isConsumed());

                KeyEvent backwardEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);

                assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                        owner,
                        backwardEvent,
                        staleTargets,
                        true,
                        false,
                        -1,
                        true
                ));

                assertTrue(first.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertTrue(backwardEvent.isConsumed());

                first.setDisable(true);
                second.setDisable(true);
                outside.requestFocus();
                KeyEvent emptyEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

                assertFalse(M3FocusTraversal.handleDirectionalKeyFocus(
                        owner,
                        emptyEvent,
                        staleTargets,
                        true,
                        false,
                        -1,
                        true
                ));

                assertTrue(outside.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertFalse(emptyEvent.isConsumed());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that finite skin-owned state transitions settle when runtime motion is disabled.
    @Tier2Test
    @Test
    void skinOwnedStateTransitionsSettleWhenMotionIsDisabled() throws InterruptedException {
        AtomicReference<@Nullable SkinOwnedStateMotionScene> sceneReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> skinOwnedStateTransitionsAreAnimating(sceneReference),
                    () -> {
                        SkinOwnedStateMotionScene scene = showSkinOwnedStateMotionScene();
                        sceneReference.set(scene);
                        startSkinOwnedStateTransitions(scene);
                    },
                    () -> {
                        SkinOwnedStateMotionScene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                        FxTestUtils.setCssAnimationsEnabled(scene.root, false);
                        scene.root.applyCss();
                        scene.root.layout();
                        assertSkinOwnedStateTransitionsSettled(scene);
                    }
            );
        } finally {
            closeSkinOwnedStateMotionScene(sceneReference);
        }
    }

    /// Verifies that finite overlay and container-owned animations settle when runtime motion is disabled.
    @Tier2Test
    @Test
    void overlayOwnedStateTransitionsSettleWhenMotionIsDisabled() throws InterruptedException {
        AtomicReference<@Nullable OverlayOwnedStateMotionScene> sceneReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> overlayOwnedStateTransitionsAreAnimating(sceneReference),
                    () -> {
                        OverlayOwnedStateMotionScene scene = showOverlayOwnedStateMotionScene();
                        sceneReference.set(scene);
                        startOverlayOwnedStateTransitions(scene);
                    },
                    () -> {
                        OverlayOwnedStateMotionScene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                        assertOverlayOwnedStateTransitionsIntermediate(scene);

                        M3Snackbar snackbar = Objects.requireNonNull(scene.root.getSnackbar(), "snackbar");
                        Node snackbarSurface = snackbarSurface(scene.root);
                        double snackbarOpacity = snackbarSurface.getOpacity();
                        double snackbarTranslateY = snackbarSurface.getTranslateY();
                        double actionOpacity = scene.action.getOpacity();
                        double actionScaleX = scene.action.getScaleX();
                        double actionScaleY = scene.action.getScaleY();
                        double actionTranslateY = scene.action.getTranslateY();

                        scene.root.dismissSnackbar();
                        scene.root.showSnackbar(snackbar);
                        scene.fabMenu.hide();
                        scene.fabMenu.show();

                        assertEquals(snackbarOpacity, snackbarSurface.getOpacity(), 0.0001);
                        assertEquals(snackbarTranslateY, snackbarSurface.getTranslateY(), 0.0001);
                        assertEquals(actionOpacity, scene.action.getOpacity(), 0.0001);
                        assertEquals(actionScaleX, scene.action.getScaleX(), 0.0001);
                        assertEquals(actionScaleY, scene.action.getScaleY(), 0.0001);
                        assertEquals(actionTranslateY, scene.action.getTranslateY(), 0.0001);

                        FxTestUtils.setCssAnimationsEnabled(scene.root, false);
                        scene.root.layout();
                        assertOverlayOwnedStateTransitionsSettled(scene);
                    }
            );
        } finally {
            closeOverlayOwnedStateMotionScene(sceneReference);
        }
    }

    /// Verifies that representative interactive states render visible state layer feedback.
    @Tier2Test
    @Test
    void interactiveStateSnapshotRendersHoverFocusAndPressedFeedback() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button normalButton = createButton("Normal", M3ButtonVariant.FILLED);
            M3Button hoverButton = createButton("Hover", M3ButtonVariant.FILLED);
            M3Button focusButton = createButton("Focus", M3ButtonVariant.FILLED);
            M3Button pressedButton = createButton("Pressed", M3ButtonVariant.FILLED);
            applyPseudoState(hoverButton, "hover");
            applyPseudoState(pressedButton, "pressed");

            M3CheckBox hoverCheckBox = createCheckBox("Checkbox hover", true);
            M3RadioButton focusRadioButton = createRadioButton("Radio focus", true);
            M3Switch pressedSwitch = createSwitch("Switch pressed", true);
            applyPseudoState(hoverCheckBox, "hover");
            applyPseudoState(pressedSwitch, "pressed");

            M3Slider pressedSlider = new M3Slider(0.0, 100.0, 56.0);
            pressedSlider.setPrefWidth(180.0);
            M3Tab focusTab = createTab("Tab focus", true);
            M3NavigationItem hoverNavigationItem = createNavigationItem(
                    "Home",
                    visualIcon("home"),
                    true
            );
            applyPseudoState(pressedSlider, "pressed");
            applyPseudoState(hoverNavigationItem, "hover");

            M3ListItem pressedListItem = new M3ListItem("Pressed list item");
            pressedListItem.setLeading(visualIcon("inbox"));
            pressedListItem.setPrefWidth(220.0);
            M3Card hoverCard = new M3Card(visualLabel("Hover card"), M3CardVariant.ELEVATED);
            hoverCard.setOnAction(event -> {
            });
            hoverCard.setPrefSize(160.0, 72.0);
            applyPseudoState(pressedListItem, "pressed");
            applyPseudoState(hoverCard, "hover");

            VBox root = new VBox(
                    18.0,
                    interactiveStateSection(
                            "Buttons",
                            interactiveStateSample("Normal", normalButton),
                            interactiveStateSample("Hover", hoverButton),
                            interactiveStateSample("Focus Visible", focusButton),
                            interactiveStateSample("Pressed", pressedButton)
                    ),
                    interactiveStateSection(
                            "Selection",
                            interactiveStateSample("Hover", hoverCheckBox),
                            interactiveStateSample("Focus Visible", focusRadioButton),
                            interactiveStateSample("Pressed", pressedSwitch)
                    ),
                    interactiveStateSection(
                            "Navigation",
                            interactiveStateSample("Pressed", pressedSlider),
                            interactiveStateSample("Focus Visible", focusTab),
                            interactiveStateSample("Hover", hoverNavigationItem)
                    ),
                    interactiveStateSection(
                            "Containers",
                            interactiveStateSample("Pressed", pressedListItem),
                            interactiveStateSample("Hover", hoverCard)
                    )
            );
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 920.0, 560.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            applyPseudoState(focusButton, "focus-visible");
            applyPseudoState(focusRadioButton, "focus-visible");
            applyPseudoState(focusTab, "focus-visible");
            root.applyCss();
            root.resize(920.0, Math.ceil(root.prefHeight(920.0)));
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 18);
            assertStateLayerOpacity(normalButton, 0.0);
            assertStateLayerOpacity(hoverButton, 0.08);
            assertStateLayerOpacity(focusButton, 0.1);
            assertStateLayerOpacity(pressedButton, 0.1);
            assertStateLayerOpacity(hoverCheckBox, 0.08);
            assertStateLayerOpacity(focusRadioButton, 0.1);
            assertStateLayerOpacity(pressedSwitch, 0.1);
            assertStateLayerOpacity(pressedSlider, 0.0);
            assertStateLayerOpacity(focusTab, 0.1);
            assertStateLayerOpacity(hoverNavigationItem, 0.08);
            assertStateLayerOpacity(pressedListItem, 0.1);
            assertStateLayerOpacity(hoverCard, 0.08);
            Region focusIndicator = lookupRegion(focusButton, ".m3-focus-indicator");
            Bounds focusButtonBounds = focusButton.localToScene(focusButton.getLayoutBounds());
            Bounds focusIndicatorBounds = focusIndicator.localToScene(focusIndicator.getLayoutBounds());
            assertEquals(1.0, focusIndicator.getOpacity(), 0.0001);
            assertTrue(focusIndicatorBounds.getMinX() < focusButtonBounds.getMinX());
            assertTrue(focusIndicatorBounds.getMaxX() > focusButtonBounds.getMaxX());
            Color focusRingPixel = snapshotScenePixel(
                    image,
                    focusIndicatorBounds.getMinX() + 1.0,
                    focusIndicatorBounds.getCenterY()
            );
            Color focusRingBackground = snapshotScenePixel(
                    image,
                    focusIndicatorBounds.getMinX() - 2.0,
                    focusIndicatorBounds.getCenterY()
            );
            assertTrue(colorDistance(focusRingPixel, focusRingBackground) > 0.04,
                    () -> "outer focus indicator should render beyond the button bounds: ring="
                            + focusRingPixel + ", background=" + focusRingBackground
                            + ", indicatorBounds=" + focusIndicatorBounds
                            + ", buttonBounds=" + focusButtonBounds);
            assertSnapshotNodeContainsContrast(image, hoverButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, focusButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, pressedButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, hoverCheckBox, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, focusRadioButton, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, pressedSwitch, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, pressedSlider, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, focusTab, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, hoverNavigationItem, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, pressedListItem, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, hoverCard, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-interactive-states.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies style classes for action controls.
    @Test
    void actionControlsExposeStyleClasses() {
        M3Button button = new M3Button("Button");
        button.setVariant(M3ButtonVariant.OUTLINED);
        M3IconButton iconButton = new M3IconButton();
        M3MenuButton menuButton = new M3MenuButton("More");

        M3FloatingActionButton fab = new M3FloatingActionButton("+");
        fab.setVariant(M3FloatingActionButtonVariant.PRIMARY);
        fab.setSize(M3FloatingActionButtonSize.REGULAR);

        assertTrue(button.getStyleClass().contains(BUTTON_BASE_STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(BUTTON_STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3ButtonVariant.OUTLINED.styleClass()));
        assertTrue(iconButton.getStyleClass().contains(BUTTON_BASE_STYLE_CLASS));
        assertTrue(iconButton.getStyleClass().contains(ICON_BUTTON_STYLE_CLASS));
        assertFalse(iconButton.getStyleClass().contains(BUTTON_STYLE_CLASS));
        assertTrue(menuButton.getStyleClass().contains(BUTTON_BASE_STYLE_CLASS));
        assertTrue(menuButton.getStyleClass().contains(MENU_BUTTON_STYLE_CLASS));
        assertFalse(menuButton.getStyleClass().contains(BUTTON_STYLE_CLASS));
        assertTrue(fab.getStyleClass().contains("m3-fab"));
        assertTrue(fab.getStyleClass().contains(M3FloatingActionButtonVariant.PRIMARY.styleClass()));
        assertTrue(fab.getStyleClass().contains(M3FloatingActionButtonSize.REGULAR.styleClass()));
    }

    /// Verifies style classes for container controls.
    @Test
    void containerControlsExposeStyleClasses() {
        M3Card card = new M3Card();
        card.setVariant(M3CardVariant.OUTLINED);

        M3Banner banner = banner("Message");
        M3OverlayPane overlayPane = new M3OverlayPane();
        M3DialogPane dialogPane = new M3DialogPane();
        M3TopAppBar topAppBar = topAppBar();
        M3BottomAppBar bottomAppBar = bottomAppBar();
        M3Toolbar toolbar = new M3Toolbar();
        M3SideSheet sideSheet = sideSheet();
        M3BottomSheet bottomSheet = bottomSheet();
        M3Scrim scrim = new M3Scrim();
        M3Carousel carousel = new M3Carousel();
        M3FormPane formPane = new M3FormPane();
        M3FormSection formSection = formSection("Section");
        M3FormRow formRow = new M3FormRow();
        M3NavigationBar navigationBar = new M3NavigationBar();
        M3NavigationRail navigationRail = new M3NavigationRail();
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();
        M3NavigationDrawerGroup navigationDrawerGroup = new M3NavigationDrawerGroup("Group");
        M3ListItem navigationDrawerGroupChild = new M3ListItem("Child");
        navigationDrawerGroup.getItems().add(navigationDrawerGroupChild);

        assertTrue(new M3ButtonGroup().getStyleClass().contains("m3-button-group"));
        assertTrue(new M3SplitButton("Create").getStyleClass().contains("m3-split-button"));
        assertTrue(new M3FabMenu().getStyleClass().contains("m3-fab-menu"));
        assertTrue(card.getStyleClass().contains("m3-card"));
        assertTrue(card.getStyleClass().contains(M3CardVariant.OUTLINED.styleClass()));
        assertTrue(banner.getStyleClass().contains("m3-banner"));
        assertTrue(overlayPane.getStyleClass().contains("m3-overlay-pane"));
        assertTrue(dialogPane.getStyleClass().contains("m3-dialog-pane"));
        assertTrue(topAppBar.getStyleClass().contains("m3-top-app-bar"));
        assertTrue(topAppBar.getStyleClass().contains(M3TopAppBarVariant.SMALL.styleClass()));
        assertTrue(bottomAppBar.getStyleClass().contains("m3-bottom-app-bar"));
        assertTrue(bottomAppBar.getStyleClass().contains(
                M3BottomAppBarFloatingActionAlignment.END.styleClass()
        ));
        assertTrue(toolbar.getStyleClass().contains("m3-toolbar"));
        assertTrue(sideSheet.getStyleClass().contains("m3-side-sheet"));
        assertTrue(bottomSheet.getStyleClass().contains("m3-bottom-sheet"));
        assertTrue(scrim.getStyleClass().contains("m3-scrim"));
        assertTrue(carousel.getStyleClass().contains("m3-carousel"));
        assertTrue(formPane.getStyleClass().contains("m3-form-pane"));
        assertTrue(formSection.getStyleClass().contains("m3-form-section"));
        assertTrue(formRow.getStyleClass().contains("m3-form-row"));
        assertTrue(navigationBar.getStyleClass().contains("m3-navigation-bar"));
        assertTrue(navigationRail.getStyleClass().contains("m3-navigation-rail"));
        assertTrue(navigationDrawer.getStyleClass().contains("m3-navigation-drawer"));
        assertTrue(navigationDrawerGroup.getStyleClass().contains("m3-navigation-drawer-group"));
        assertTrue(navigationDrawerGroup.getHeaderItem().getStyleClass().contains(
                "m3-navigation-drawer-group-header"
        ));
        assertTrue(navigationDrawerGroupChild.getStyleClass().contains(
                "m3-navigation-drawer-group-child"
        ));
    }

    /// Verifies style classes for input and selection controls.
    @Test
    void inputAndSelectionControlsExposeStyleClasses() {
        M3TextField textField = new M3TextField();
        textField.setVariant(M3TextInputVariant.OUTLINED);

        M3FilterChip chip = new M3FilterChip("Chip");

        assertTrue(textField.getStyleClass().contains("m3-text-field"));
        assertTrue(textField.getStyleClass().contains(M3TextInputVariant.OUTLINED.styleClass()));
        assertTrue(new M3PasswordField().getStyleClass().contains("m3-password-field"));
        assertTrue(new M3TextInputLayout().getStyleClass().contains("m3-text-input-layout"));
        assertTrue(new M3TextArea().getStyleClass().contains("m3-text-area"));
        assertTrue(new M3Tooltip().getStyleClass().contains("m3-tooltip"));
        assertTrue(richTooltip().getStyleClass().contains("m3-rich-tooltip"));
        assertTrue(new M3Avatar("A").getStyleClass().contains("m3-avatar"));
        assertTrue(new M3Icon("A").getStyleClass().contains("m3-icon"));
        assertTrue(new M3IconButton(new M3Icon("A")).getStyleClass().contains(ICON_BUTTON_STYLE_CLASS));
        assertTrue(new M3IconToggleButton("A").getStyleClass().contains(ICON_TOGGLE_BUTTON_STYLE_CLASS));
        assertTrue(new M3IconToggleButtonGroup().getStyleClass().contains("m3-icon-toggle-button-group"));
        assertTrue(new M3Text("Text").getStyleClass().contains("m3-text"));
        assertTrue(new M3Surface().getStyleClass().contains("m3-surface"));
        assertTrue(new M3ValidationSummary().getStyleClass().contains("m3-validation-summary"));
        assertTrue(new M3BadgedBox().getStyleClass().contains("m3-badged-box"));
        assertTrue(new M3Menu().getStyleClass().contains("m3-menu"));
        assertTrue(new M3MenuItem("Open").getStyleClass().contains("m3-menu-item"));
        assertTrue(new M3SubMenuItem("Export").getStyleClass().contains("m3-sub-menu-item"));
        assertTrue(new M3MenuSectionHeader("File").getStyleClass().contains("m3-menu-section-header"));
        assertTrue(new M3MenuButton("More").getStyleClass().contains(MENU_BUTTON_STYLE_CLASS));
        assertTrue(new M3SearchBar().getStyleClass().contains("m3-search-bar"));
        assertTrue(searchView().getStyleClass().contains("m3-search-view"));
        assertTrue(new M3CheckBox().getStyleClass().contains("m3-checkbox"));
        assertTrue(new M3RadioButton().getStyleClass().contains("m3-radio-button"));
        assertTrue(new M3Switch().getStyleClass().contains("m3-switch"));
        assertTrue(new M3Slider().getStyleClass().contains("m3-slider"));
        assertTrue(new M3DatePicker().getStyleClass().contains("m3-date-picker"));
        assertTrue(new M3DatePickerField().getStyleClass().contains("m3-date-picker-field"));
        assertTrue(new M3DateRangePicker().getStyleClass().contains("m3-date-range-picker"));
        assertTrue(new M3DateRangePickerField().getStyleClass().contains("m3-date-range-picker-field"));
        assertTrue(new M3TimePicker().getStyleClass().contains("m3-time-picker"));
        assertTrue(new M3TimePickerField().getStyleClass().contains("m3-time-picker-field"));
        assertTrue(chip.getStyleClass().contains("m3-chip"));
        assertTrue(chip.getStyleClass().contains("m3-filter-chip"));
        assertTrue(new M3ChipGroup().getStyleClass().contains("m3-chip-group"));
        assertTrue(new M3SegmentedButton("Day").getStyleClass().contains("m3-segmented-button"));
        assertTrue(new M3SegmentedButtonGroup().getStyleClass().contains("m3-segmented-button-group"));
        assertTrue(new M3Tab("Overview").getStyleClass().contains("m3-tab"));
        assertTrue(new M3TabBar().getStyleClass().contains("m3-tab-bar"));
        assertTrue(new M3ProgressBar().getStyleClass().contains("m3-progress-bar"));
        assertTrue(new M3ProgressIndicator().getStyleClass().contains("m3-progress-indicator"));
        assertTrue(new M3LoadingIndicator().getStyleClass().contains("m3-loading-indicator"));
        assertTrue(new M3Divider().getStyleClass().contains("m3-divider"));
        assertTrue(new M3DisclosureIcon().getStyleClass().contains("m3-disclosure-icon"));
        assertTrue(new M3Badge("1").getStyleClass().contains("m3-badge"));
        assertTrue(new M3NavigationItem("Home").getStyleClass().contains("m3-navigation-item"));
        assertTrue(new M3ListPane().getStyleClass().contains("m3-list-pane"));
        assertTrue(new M3ListView<>().getStyleClass().contains("m3-list-view"));
        assertTrue(new M3ListCell<>(new M3ListView<>()).getStyleClass().contains("m3-list-view-cell"));
        assertTrue(new M3ListItem("Item").getStyleClass().contains("m3-list-item"));
        assertTrue(new M3ListSectionHeader("Section").getStyleClass().contains("m3-list-section-header"));
    }

    /// Returns the single reusable snackbar surface rendered by an overlay pane.
    private static Node snackbarSurface(M3OverlayPane overlayPane) {
        return Objects.requireNonNull(overlayPane.lookup(".m3-snackbar-container"), "snackbar surface");
    }

    /// Holds the real-window controls used by the skin-owned state-transition test.
    private static final class SkinOwnedStateMotionScene {
        /// The stage that owns the tested scene graph.
        private final Stage stage;

        /// The root node that carries local motion settings.
        private final VBox root;

        /// The checkbox used to verify checkmark settling.
        private final M3CheckBox checkBox;

        /// The radio button used to verify dot settling.
        private final M3RadioButton radioButton;

        /// The switch used to verify thumb-position settling.
        private final M3Switch switchControl;

        /// The segmented button used to verify selected-container settling.
        private final M3SegmentedButton segmentedButton;

        /// The slider used to verify value-position settling.
        private final M3Slider slider;

        /// The tab used to verify active-indicator settling.
        private final M3Tab tab;

        /// The navigation item used to verify active-indicator settling.
        private final M3NavigationItem navigationItem;

        /// The list item used to verify selected-container settling.
        private final M3ListItem listItem;

        /// The disclosure icon used to verify rotation settling.
        private final M3DisclosureIcon disclosureIcon;

        /// The badge used to verify content-change settling.
        private final M3Badge badge;

        /// Creates a skin-owned state-transition test scene holder.
        private SkinOwnedStateMotionScene(
                Stage stage,
                VBox root,
                M3CheckBox checkBox,
                M3RadioButton radioButton,
                M3Switch switchControl,
                M3SegmentedButton segmentedButton,
                M3Slider slider,
                M3Tab tab,
                M3NavigationItem navigationItem,
                M3ListItem listItem,
                M3DisclosureIcon disclosureIcon,
                M3Badge badge
        ) {
            this.stage = stage;
            this.root = root;
            this.checkBox = checkBox;
            this.radioButton = radioButton;
            this.switchControl = switchControl;
            this.segmentedButton = segmentedButton;
            this.slider = slider;
            this.tab = tab;
            this.navigationItem = navigationItem;
            this.listItem = listItem;
            this.disclosureIcon = disclosureIcon;
            this.badge = badge;
        }
    }

    /// Shows controls that exercise finite skin-owned state transitions with observable motion.
    private static SkinOwnedStateMotionScene showSkinOwnedStateMotionScene() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        M3SegmentedButton segmentedButton = new M3SegmentedButton("Segment");
        M3Slider slider = new M3Slider(0.0, 100.0, 25.0);
        M3Tab tab = new M3Tab("Tab");
        M3NavigationItem navigationItem = new M3NavigationItem("Home");
        M3ListItem listItem = new M3ListItem("List item");
        M3DisclosureIcon disclosureIcon = new M3DisclosureIcon();
        M3Badge badge = new M3Badge("1");
        VBox root = new VBox(
                checkBox,
                radioButton,
                switchControl,
                segmentedButton,
                slider,
                tab,
                navigationItem,
                listItem,
                disclosureIcon,
                badge
        );
        Scene scene = new Scene(root, 360.0, 520.0);
        Stage stage = new Stage();

        slider.setPrefWidth(240.0);
        root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, true);
        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.resize(360.0, 520.0);
        root.layout();

        return new SkinOwnedStateMotionScene(
                stage,
                root,
                checkBox,
                radioButton,
                switchControl,
                segmentedButton,
                slider,
                tab,
                navigationItem,
                listItem,
                disclosureIcon,
                badge
        );
    }

    /// Starts all finite skin-owned state transitions in the supplied scene.
    private static void startSkinOwnedStateTransitions(SkinOwnedStateMotionScene scene) {
        scene.checkBox.setSelected(true);
        scene.radioButton.setSelected(true);
        scene.switchControl.setSelected(true);
        scene.segmentedButton.setSelected(true);
        scene.slider.setValue(75.0);
        scene.tab.setSelected(true);
        scene.navigationItem.setSelected(true);
        scene.listItem.setSelected(true);
        scene.disclosureIcon.setExpanded(true);
        scene.badge.setText("2");
    }

    /// Returns whether all skin-owned transition samples have reached observable intermediate frames.
    private static boolean skinOwnedStateTransitionsAreAnimating(
            AtomicReference<@Nullable SkinOwnedStateMotionScene> sceneReference
    ) {
        @Nullable SkinOwnedStateMotionScene scene = sceneReference.get();
        if (scene == null) {
            return false;
        }

        scene.root.applyCss();
        scene.root.layout();
        Region checkMark = lookupRegion(scene.checkBox, ".m3-checkbox-mark");
        Node radioDot = radioDotAnimationLayer(scene.radioButton);
        Region switchThumb = lookupRegion(scene.switchControl, ".thumb");
        Region segmentSelection = segmentedButtonSelectionContainer(scene.segmentedButton);
        Region tabIndicator = lookupRegion(scene.tab, "." + "m3-tab-active-indicator");
        Region navigationIndicator = lookupRegion(scene.navigationItem, ".m3-navigation-item-indicator");
        Region listSelection = listItemSelectionContainer(scene.listItem);
        SVGPath disclosureArrow = assertInstanceOf(
                SVGPath.class,
                scene.disclosureIcon.lookup(".m3-disclosure-icon-shape")
        );
        Label badgeLabel = assertInstanceOf(Label.class, scene.badge.lookup(".m3-badge-label"));

        return isBetweenExclusive(checkMark.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(checkMark.getScaleX(), 0.72, 1.0)
                && isBetweenExclusive(radioDot.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(radioDot.getScaleX(), 0.64, 1.0)
                && isBetweenExclusive(switchThumb.getLayoutX(), 8.0, 24.0)
                && isBetweenExclusive(switchThumb.getWidth(), 16.0, 24.0)
                && singleContainerSelectionIsTransitioning(segmentSelection)
                && isBetweenExclusive(sliderRenderedPosition(scene.slider), 0.25, 0.75)
                && singleNavigationIndicatorIsTransitioning(tabIndicator)
                && singleNavigationIndicatorIsTransitioning(navigationIndicator)
                && singleContainerSelectionIsTransitioning(listSelection)
                && isBetweenExclusive(disclosureArrow.getRotate(), -90.0, 0.0)
                && isBetweenExclusive(badgeLabel.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(badgeLabel.getScaleX(), 0.86, 1.0);
    }

    /// Verifies that all skin-owned transitions reached their selected or updated final state.
    private static void assertSkinOwnedStateTransitionsSettled(SkinOwnedStateMotionScene scene) {
        Region checkMark = lookupRegion(scene.checkBox, ".m3-checkbox-mark");
        assertEquals(1.0, checkMark.getOpacity(), 0.0001);
        assertEquals(1.0, checkMark.getScaleX(), 0.0001);
        Node radioDot = radioDotAnimationLayer(scene.radioButton);
        assertEquals(1.0, radioDot.getOpacity(), 0.0001);
        assertEquals(1.0, radioDot.getScaleX(), 0.0001);
        Region switchThumb = lookupRegion(scene.switchControl, ".thumb");
        assertEquals(24.0, switchThumb.getLayoutX(), 0.0001);
        assertEquals(24.0, switchThumb.getWidth(), 0.0001);
        Region segmentSelection = lookupRegion(
                scene.segmentedButton,
                "." + "m3-segmented-button-selection-container"
        );
        assertEquals(1.0, segmentSelection.getOpacity(), 0.0001);
        assertEquals(1.0, segmentSelection.getScaleX(), 0.0001);
        assertEquals(0.75, sliderRenderedPosition(scene.slider), 0.0001);
        Region tabIndicator = lookupRegion(scene.tab, "." + "m3-tab-active-indicator");
        assertEquals(1.0, tabIndicator.getOpacity(), 0.0001);
        assertEquals(1.0, tabIndicator.getScaleX(), 0.0001);
        Region navigationIndicator = lookupRegion(scene.navigationItem, ".m3-navigation-item-indicator");
        assertEquals(1.0, navigationIndicator.getOpacity(), 0.0001);
        assertEquals(1.0, navigationIndicator.getScaleX(), 0.0001);
        Region listSelection = lookupRegion(scene.listItem, ".m3-list-item-selection-container");
        assertEquals(1.0, listSelection.getOpacity(), 0.0001);
        assertEquals(1.0, listSelection.getScaleX(), 0.0001);
        SVGPath disclosureArrow = assertInstanceOf(
                SVGPath.class,
                scene.disclosureIcon.lookup(".m3-disclosure-icon-shape")
        );
        assertEquals(0.0, disclosureArrow.getRotate(), 0.0001);
        Label badgeLabel = assertInstanceOf(Label.class, scene.badge.lookup(".m3-badge-label"));
        assertEquals("2", badgeLabel.getText());
        assertEquals(1.0, badgeLabel.getOpacity(), 0.0001);
        assertEquals(1.0, badgeLabel.getScaleX(), 0.0001);
    }

    /// Closes a skin-owned state-transition test scene and clears local motion overrides.
    private static void closeSkinOwnedStateMotionScene(
            AtomicReference<@Nullable SkinOwnedStateMotionScene> sceneReference
    ) {
        @Nullable SkinOwnedStateMotionScene scene = sceneReference.get();
        if (scene == null) {
            return;
        }

        FxTestUtils.runOnFxThread(() -> {
            FxTestUtils.setCssAnimationsEnabled(scene.root, true);
            FxTestUtils.clearMotionScheme(scene.root);
            scene.stage.close();
        });
    }

    /// Holds the real-window controls used by the overlay-owned state-transition test.
    private static final class OverlayOwnedStateMotionScene {
        /// The stage that owns the tested scene graph.
        private final Stage stage;

        /// The root node that carries local motion settings.
        private final M3OverlayPane root;

        /// The scrim used to verify opacity-only exit settling.
        private final M3Scrim scrim;

        /// The side sheet used to verify horizontal exit settling.
        private final M3SideSheet sideSheet;

        /// The bottom sheet used to verify vertical exit settling.
        private final M3BottomSheet bottomSheet;

        /// The search view used to verify results-container exit settling.
        private final M3SearchView searchView;

        /// The FAB menu used to verify action entrance settling.
        private final M3FabMenu fabMenu;

        /// The FAB menu action used to verify action entrance settling.
        private final M3FloatingActionButton action;

        /// Creates an overlay-owned state-transition test scene holder.
        private OverlayOwnedStateMotionScene(
                Stage stage,
                M3OverlayPane root,
                M3Scrim scrim,
                M3SideSheet sideSheet,
                M3BottomSheet bottomSheet,
                M3SearchView searchView,
                M3FabMenu fabMenu,
                M3FloatingActionButton action
        ) {
            this.stage = stage;
            this.root = root;
            this.scrim = scrim;
            this.sideSheet = sideSheet;
            this.bottomSheet = bottomSheet;
            this.searchView = searchView;
            this.fabMenu = fabMenu;
            this.action = action;
        }
    }

    /// Shows controls that exercise finite overlay-owned state transitions with observable motion.
    private static OverlayOwnedStateMotionScene showOverlayOwnedStateMotionScene() {
        M3Scrim scrim = new M3Scrim();
        M3SideSheet sideSheet = sideSheet("Details", new Label("Side content"));
        M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom content"));
        M3SearchView searchView = searchView("Search", new M3ListItem("Result"));
        M3OverlayPane overlayPane = new M3OverlayPane();
        M3FloatingActionButton action = new M3FloatingActionButton("A");
        M3FabMenu fabMenu = new M3FabMenu();
        VBox content = new VBox(12.0, scrim, sideSheet, bottomSheet, searchView, fabMenu);
        overlayPane.setContent(content);
        Scene scene = new Scene(overlayPane, 520.0, 680.0);
        Stage stage = new Stage();

        scrim.setPrefSize(180.0, 80.0);
        sideSheet.setPrefSize(260.0, 140.0);
        bottomSheet.setPrefSize(360.0, 140.0);
        searchView.setPrefSize(360.0, 160.0);
        fabMenu.setPrefSize(120.0, 160.0);
        fabMenu.getItems().add(action);
        overlayPane.setSnackbarDisplayDuration(Duration.INDEFINITE);
        content.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(overlayPane, true);
        FxTestUtils.setMotionScheme(overlayPane, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        overlayPane.applyCss();
        overlayPane.resize(520.0, 680.0);
        overlayPane.layout();

        return new OverlayOwnedStateMotionScene(
                stage,
                overlayPane,
                scrim,
                sideSheet,
                bottomSheet,
                searchView,
                fabMenu,
                action
        );
    }

    /// Starts all finite overlay-owned state transitions in the supplied scene.
    private static void startOverlayOwnedStateTransitions(OverlayOwnedStateMotionScene scene) {
        scene.scrim.hide();
        scene.sideSheet.hide();
        scene.bottomSheet.hide();
        scene.searchView.deactivate();
        scene.root.showSnackbar(new M3Snackbar("Saved"));
        scene.fabMenu.show();
    }

    /// Returns whether all overlay-owned transition samples have reached observable intermediate frames.
    private static boolean overlayOwnedStateTransitionsAreAnimating(
            AtomicReference<@Nullable OverlayOwnedStateMotionScene> sceneReference
    ) {
        @Nullable OverlayOwnedStateMotionScene scene = sceneReference.get();
        if (scene == null) {
            return false;
        }

        scene.root.layout();
        @Nullable M3Snackbar snackbar = scene.root.getSnackbar();
        return opacityExitNodeIsTransitioning(scene.scrim, scene.scrim.getVisibleOpacity())
                && spatialExitNodeIsTransitioning(scene.sideSheet)
                && spatialExitNodeIsTransitioning(scene.bottomSheet)
                && spatialExitNodeIsTransitioning(searchViewResultsContainer(scene.searchView))
                && snackbar != null
                && spatialEnterNodeIsTransitioning(snackbarSurface(scene.root))
                && fabActionEnterNodeIsTransitioning(scene.action);
    }

    /// Verifies that all overlay-owned transitions are rendering intermediate frames.
    private static void assertOverlayOwnedStateTransitionsIntermediate(OverlayOwnedStateMotionScene scene) {
        assertBetween(scene.scrim.getOpacity(), 0.0, scene.scrim.getVisibleOpacity(), "scrim opacity");
        assertSpatialExitNodeIntermediate(scene.sideSheet, "side sheet");
        assertSpatialExitNodeIntermediate(scene.bottomSheet, "bottom sheet");
        assertSpatialExitNodeIntermediate(searchViewResultsContainer(scene.searchView), "search results");
        assertNotNull(scene.root.getSnackbar());
        assertSpatialEnterNodeIntermediate(snackbarSurface(scene.root), "snackbar");
        assertFabActionEnterNodeIntermediate(scene.action);
    }

    /// Verifies that all overlay-owned transitions reached their visible or hidden final state.
    private static void assertOverlayOwnedStateTransitionsSettled(OverlayOwnedStateMotionScene scene) {
        assertFalse(scene.scrim.isVisible());
        assertFalse(scene.scrim.isManaged());
        assertEquals(0.0, scene.scrim.getOpacity(), 0.0001);
        assertFalse(scene.sideSheet.isVisible());
        assertFalse(scene.sideSheet.isManaged());
        assertEquals(0.0, scene.sideSheet.getOpacity(), 0.0001);
        assertFalse(scene.bottomSheet.isVisible());
        assertFalse(scene.bottomSheet.isManaged());
        assertEquals(0.0, scene.bottomSheet.getOpacity(), 0.0001);
        assertFalse(searchViewResultsContainer(scene.searchView).isVisible());
        assertFalse(searchViewResultsContainer(scene.searchView).isManaged());
        assertEquals(0.0, searchViewResultsContainer(scene.searchView).getOpacity(), 0.0001);

        assertNotNull(scene.root.getSnackbar());
        Node snackbarSurface = snackbarSurface(scene.root);
        assertTrue(snackbarSurface.isVisible());
        assertTrue(snackbarSurface.isManaged());
        assertEquals(1.0, snackbarSurface.getOpacity(), 0.0001);
        assertEquals(0.0, snackbarSurface.getTranslateY(), 0.0001);
        assertTrue(scene.action.isVisible());
        assertTrue(scene.action.isManaged());
        assertEquals(1.0, scene.action.getOpacity(), 0.0001);
        assertEquals(1.0, scene.action.getScaleX(), 0.0001);
        assertEquals(1.0, scene.action.getScaleY(), 0.0001);
        assertEquals(0.0, scene.action.getTranslateY(), 0.0001);
    }

    /// Closes an overlay-owned state-transition test scene and clears local motion overrides.
    private static void closeOverlayOwnedStateMotionScene(
            AtomicReference<@Nullable OverlayOwnedStateMotionScene> sceneReference
    ) {
        @Nullable OverlayOwnedStateMotionScene scene = sceneReference.get();
        if (scene == null) {
            return;
        }

        FxTestUtils.runOnFxThread(() -> {
            FxTestUtils.setCssAnimationsEnabled(scene.root, true);
            FxTestUtils.clearMotionScheme(scene.root);
            scene.stage.close();
        });
    }

    /// Returns whether an opacity-only exit node is rendering an intermediate transition.
    private static boolean opacityExitNodeIsTransitioning(Node node, double visibleOpacity) {
        return node.isVisible()
                && node.isManaged()
                && isBetweenExclusive(node.getOpacity(), 0.0, visibleOpacity);
    }

    /// Returns whether a spatial exit node is rendering an intermediate transition.
    private static boolean spatialExitNodeIsTransitioning(Node node) {
        return node.isVisible()
                && node.isManaged()
                && isBetweenExclusive(node.getOpacity(), 0.0, 1.0)
                && hasVisibleTranslation(node);
    }

    /// Returns whether a spatial entrance node is rendering an intermediate transition.
    private static boolean spatialEnterNodeIsTransitioning(Node node) {
        return node.isVisible()
                && node.isManaged()
                && isBetweenExclusive(node.getOpacity(), 0.0, 1.0)
                && hasVisibleTranslation(node);
    }

    /// Verifies that a spatial exit node is rendering an intermediate transition.
    private static void assertSpatialExitNodeIntermediate(Node node, String description) {
        assertTrue(node.isVisible(), () -> description + " should still be visible");
        assertTrue(node.isManaged(), () -> description + " should still be managed");
        assertBetween(node.getOpacity(), 0.0, 1.0, description + " opacity");
        assertTrue(hasVisibleTranslation(node), () -> description
                + " translateX=" + node.getTranslateX()
                + ", translateY=" + node.getTranslateY());
    }

    /// Returns whether a single selected-container node is rendering an intermediate transition.
    private static boolean singleContainerSelectionIsTransitioning(Region selection) {
        return isBetweenExclusive(selection.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(selection.getScaleX(), 0.96, 1.0);
    }

    /// Returns whether a single navigation indicator node is rendering an intermediate transition.
    private static boolean singleNavigationIndicatorIsTransitioning(Region indicator) {
        return isBetweenExclusive(indicator.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(indicator.getScaleX(), 0.72, 1.0);
    }

    /// Returns the horizontal slider thumb position normalized to the rendered track range.
    private static double sliderRenderedPosition(M3Slider slider) {
        slider.applyCss();
        slider.layout();
        Region thumb = lookupRegion(slider, ".thumb");
        double trackLength = Math.max(0.0, slider.getWidth() - thumb.getWidth());
        return trackLength == 0.0 ? 0.0 : thumb.getLayoutX() / trackLength;
    }

    /// Verifies the resolved logical container alignment of composite controls.
    private static void assertCompositeControlContainerAlignment(
            M3CheckBox checkBox,
            M3RadioButton radioButton,
            M3Switch switchControl,
            M3SearchBar searchBar,
            M3SplitButton splitButton,
            Pos alignment,
            boolean rightToLeft
    ) {
        assertEquals(alignment, assertInstanceOf(
                HBox.class,
                checkBox.lookup(".m3-selection-container")
        ).getAlignment());
        assertEquals(alignment, assertInstanceOf(
                HBox.class,
                radioButton.lookup(".m3-selection-container")
        ).getAlignment());
        assertEquals(alignment, assertInstanceOf(
                HBox.class,
                switchControl.lookup(".m3-selection-container")
        ).getAlignment());
        assertEquals(alignment, assertInstanceOf(
                HBox.class,
                searchBar.lookup("." + "m3-search-bar-content")
        ).getAlignment());
        M3Button actionButton = splitButtonActionButton(splitButton);
        M3MenuButton menuButton = splitButtonMenuButton(splitButton);
        Bounds actionBounds = actionButton.localToScene(actionButton.getLayoutBounds());
        Bounds menuBounds = menuButton.localToScene(menuButton.getLayoutBounds());
        double gap;
        if (rightToLeft) {
            assertTrue(actionBounds.getMinX() > menuBounds.getMinX());
            gap = actionBounds.getMinX() - menuBounds.getMaxX();
        } else {
            assertTrue(actionBounds.getMinX() < menuBounds.getMinX());
            gap = menuBounds.getMinX() - actionBounds.getMaxX();
        }
        assertEquals(splitButton.getSpacing(), gap, 1.0);
    }

    /// Verifies that the first child occupies a container's rendered logical start edge.
    private static void assertFirstChildAtLogicalStart(Region container, boolean rightToLeft) {
        Node firstChild = container.getChildrenUnmodifiable().get(0);
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds childBounds = firstChild.localToScene(firstChild.getBoundsInLocal());
        double expectedEdge = rightToLeft
                ? containerBounds.getMaxX() - container.getInsets().getLeft()
                : containerBounds.getMinX() + container.getInsets().getLeft();
        double actualEdge = rightToLeft ? childBounds.getMaxX() : childBounds.getMinX();

        assertEquals(expectedEdge, actualEdge, 1.0,
                () -> "containerBounds=" + containerBounds + ", childBounds=" + childBounds);
    }

    /// Creates a section for interactive-state visual snapshot tests.
    private static VBox interactiveStateSection(String title, Node... samples) {
        M3Text heading = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        FlowPane row = new FlowPane(16.0, 16.0, samples);
        row.setPrefWrapLength(860.0);
        row.setStyle("-fx-background-color: -m3-color-surface-container-low; "
                + "-fx-background-radius: 18px; "
                + "-fx-padding: 16px;");
        return new VBox(8.0, heading, row);
    }

    /// Creates a labeled sample for interactive-state visual snapshot tests.
    private static VBox interactiveStateSample(String title, Node node) {
        Label label = visualLabel(title);
        label.setStyle(label.getStyle() + " -fx-font-size: 11px;");
        VBox sample = new VBox(6.0, label, node);
        sample.setAlignment(Pos.CENTER_LEFT);
        return sample;
    }

    /// Verifies that the state layer overlay resolved to the expected opacity.
    private static void assertStateLayerOpacity(Node node, double expectedOpacity) {
        assertEquals(expectedOpacity, lookupRegion(node, ".m3-state-layer").getOpacity(), 0.0001);
    }
}
