package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.skins.M3BadgeSkin;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.glavo.m3fx.skins.M3DividerSkin;
import org.glavo.m3fx.skins.M3FloatingActionButtonSkin;
import org.glavo.m3fx.skins.M3ListItemSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests style classes and skins for m3fx controls.
@NotNullByDefault
final class M3ControlStyleTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    /// Verifies that button variants update their style classes.
    @Test
    void buttonVariantUpdatesStyleClass() {
        M3Button button = new M3Button("Button");

        assertTrue(button.getStyleClass().contains(M3Button.STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3ButtonVariant.FILLED.getStyleClass()));

        button.setVariant(M3ButtonVariant.OUTLINED);

        assertTrue(button.getStyleClass().contains(M3ButtonVariant.OUTLINED.getStyleClass()));
    }

    /// Verifies that m3fx buttons create the animated button skin.
    @Test
    void buttonCreatesAnimatedSkin() {
        M3Button button = new M3Button("Button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3ButtonSkin.class, button.getSkin());
    }

    /// Verifies that the button skin handles mouse and keyboard activation.
    @Test
    void buttonSkinHandlesActivationEvents() {
        M3Button button = new M3Button("Button");
        AtomicInteger fireCount = new AtomicInteger();
        button.setOnAction(event -> fireCount.incrementAndGet());

        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(100.0, 40.0);

        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(button.isArmed());
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertEquals(1, fireCount.get());

        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        assertTrue(button.isArmed());
        button.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertEquals(2, fireCount.get());

        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertEquals(3, fireCount.get());
    }

    /// Verifies that m3fx floating action buttons create the animated floating action button skin.
    @Test
    void floatingActionButtonCreatesAnimatedSkin() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        Pane root = new Pane(button);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3FloatingActionButtonSkin.class, button.getSkin());
    }

    /// Verifies that the floating action button skin handles mouse and keyboard activation.
    @Test
    void floatingActionButtonSkinHandlesActivationEvents() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        AtomicInteger fireCount = new AtomicInteger();
        button.setOnAction(event -> fireCount.incrementAndGet());

        Pane root = new Pane(button);
        Scene scene = new Scene(root, 200.0, 100.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        button.resize(56.0, 56.0);

        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(button.isArmed());
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertEquals(1, fireCount.get());

        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
        assertTrue(button.isArmed());
        button.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
        assertEquals(2, fireCount.get());

        button.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertEquals(3, fireCount.get());
    }

    /// Verifies that button component token properties are styleable from CSS.
    @Test
    void buttonTokensAreStyleable() {
        M3Button button = new M3Button("Button");
        button.setStyle("-m3-container-height: 52px; -m3-container-shape: 14px; -m3-horizontal-padding: 18px;");

        applyCss(button);

        assertEquals(52.0, button.getContainerHeight(), 0.0001);
        assertEquals(14.0, button.getContainerShape(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(52.0, button.getPrefHeight(), 0.0001);
        assertEquals(18.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that icon buttons stay square when size tokens change.
    @Test
    void iconButtonSizeTracksContainerHeightToken() {
        M3IconButton button = new M3IconButton();
        button.setStyle("-m3-container-height: 48px;");

        applyCss(button);

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(48.0, button.getPrefWidth(), 0.0001);
        assertEquals(48.0, button.getPrefHeight(), 0.0001);
    }

    /// Verifies that floating action button component token properties are styleable from CSS.
    @Test
    void floatingActionButtonTokensAreStyleable() {
        M3FloatingActionButton button = new M3FloatingActionButton();
        button.setStyle("-m3-container-size: 64px; -m3-container-shape: 20px; -m3-horizontal-padding: 22px;");

        applyCss(button);

        assertEquals(64.0, button.getContainerSize(), 0.0001);
        assertEquals(20.0, button.getContainerShape(), 0.0001);
        assertEquals(22.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, button.getPrefWidth(), 0.0001);
        assertEquals(64.0, button.getPrefHeight(), 0.0001);
        assertEquals(0.0, button.getPadding().getLeft(), 0.0001);

        button.setText("Create");
        applyCss(button);

        assertEquals(javafx.scene.layout.Region.USE_COMPUTED_SIZE, button.getPrefWidth(), 0.0001);
        assertEquals(64.0, button.getPrefHeight(), 0.0001);
        assertEquals(22.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that floating action button variants and sizes update style classes.
    @Test
    void floatingActionButtonVariantAndSizeUpdateStyleClasses() {
        M3FloatingActionButton button = new M3FloatingActionButton();

        assertTrue(button.getStyleClass().contains(M3FloatingActionButton.STYLE_CLASS));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonVariant.PRIMARY.getStyleClass()));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonSize.REGULAR.getStyleClass()));

        button.setVariant(M3FloatingActionButtonVariant.TERTIARY);
        button.setSize(M3FloatingActionButtonSize.LARGE);

        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonVariant.TERTIARY.getStyleClass()));
        assertTrue(button.getStyleClass().contains(M3FloatingActionButtonSize.LARGE.getStyleClass()));
    }

    /// Verifies that card component token properties are styleable from CSS.
    @Test
    void cardTokensAreStyleable() {
        M3Card card = new M3Card();
        card.setStyle("-m3-container-shape: 18px; -m3-content-padding: 20px; -m3-outline-width: 2px;");

        applyCss(card);

        assertEquals(18.0, card.getContainerShape(), 0.0001);
        assertEquals(20.0, card.getContentPadding(), 0.0001);
        assertEquals(2.0, card.getOutlineWidth(), 0.0001);
    }

    /// Verifies that snackbar component token properties are styleable from CSS.
    @Test
    void snackbarTokensAreStyleable() {
        M3Snackbar snackbar = new M3Snackbar("Message");
        snackbar.setStyle("-m3-container-shape: 10px; -m3-content-padding: 24px;");

        applyCss(snackbar);

        assertEquals(10.0, snackbar.getContainerShape(), 0.0001);
        assertEquals(24.0, snackbar.getContentPadding(), 0.0001);
    }

    /// Verifies that dialog pane component token properties are styleable from CSS.
    @Test
    void dialogPaneTokensAreStyleable() {
        M3DialogPane dialogPane = new M3DialogPane();
        dialogPane.setStyle("-m3-container-shape: 20px; -m3-content-padding: 28px;");

        applyCss(dialogPane);

        assertEquals(20.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(28.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getTop(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getRight(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getBottom(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getLeft(), 0.0001);
    }

    /// Verifies that text field component token properties are styleable from CSS.
    @Test
    void textFieldTokensAreStyleable() {
        M3TextField textField = new M3TextField();
        textField.setStyle("-m3-container-height: 64px; -m3-container-shape: 12px; -m3-horizontal-padding: 22px;");

        applyCss(textField);

        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(12.0, textField.getContainerShape(), 0.0001);
        assertEquals(22.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, textField.getPrefHeight(), 0.0001);
        assertEquals(22.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, textField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that password field component token properties are styleable from CSS.
    @Test
    void passwordFieldTokensAreStyleable() {
        M3PasswordField passwordField = new M3PasswordField();
        passwordField.setStyle("-m3-container-height: 60px; -m3-container-shape: 10px; -m3-horizontal-padding: 20px;");

        applyCss(passwordField);

        assertEquals(60.0, passwordField.getContainerHeight(), 0.0001);
        assertEquals(10.0, passwordField.getContainerShape(), 0.0001);
        assertEquals(20.0, passwordField.getHorizontalPadding(), 0.0001);
        assertEquals(60.0, passwordField.getPrefHeight(), 0.0001);
        assertEquals(20.0, passwordField.getPadding().getLeft(), 0.0001);
        assertEquals(20.0, passwordField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that chip component token properties are styleable from CSS.
    @Test
    void chipTokensAreStyleable() {
        M3Chip chip = new M3Chip("Chip");
        chip.setStyle("-m3-container-height: 36px; -m3-container-shape: 16px; -m3-horizontal-padding: 14px;");

        applyCss(chip);

        assertEquals(36.0, chip.getContainerHeight(), 0.0001);
        assertEquals(16.0, chip.getContainerShape(), 0.0001);
        assertEquals(14.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(36.0, chip.getPrefHeight(), 0.0001);
        assertEquals(14.0, chip.getPadding().getLeft(), 0.0001);
        assertEquals(14.0, chip.getPadding().getRight(), 0.0001);
    }

    /// Verifies that segmented button component token properties are styleable from CSS.
    @Test
    void segmentedButtonTokensAreStyleable() {
        M3SegmentedButton button = new M3SegmentedButton("Week");
        button.setStyle("-m3-container-height: 44px; -m3-container-shape: 12px; -m3-horizontal-padding: 18px;");

        applyCss(button);

        assertEquals(44.0, button.getContainerHeight(), 0.0001);
        assertEquals(12.0, button.getContainerShape(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(44.0, button.getPrefHeight(), 0.0001);
        assertEquals(18.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that segmented buttons can participate in a single-selection toggle group.
    @Test
    void segmentedButtonSupportsToggleGroupSelection() {
        javafx.scene.control.ToggleGroup group = new javafx.scene.control.ToggleGroup();
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        first.setToggleGroup(group);
        second.setToggleGroup(group);

        first.setSelected(true);
        second.setSelected(true);

        assertTrue(second.isSelected());
        assertEquals(second, group.getSelectedToggle());
    }

    /// Verifies that segmented button groups assign segment position style classes.
    @Test
    void segmentedButtonGroupAssignsPositionStyleClasses() {
        M3SegmentedButton first = new M3SegmentedButton("Day");
        M3SegmentedButton second = new M3SegmentedButton("Week");
        M3SegmentedButton third = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup(first, second, third);

        assertTrue(group.getStyleClass().contains(M3SegmentedButtonGroup.STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(second.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));

        group.getChildren().remove(second);

        assertFalse(second.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS));
        assertTrue(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS));

        group.getChildren().remove(first);

        assertFalse(first.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS));
        assertTrue(third.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS));
    }

    /// Verifies that selection component token properties are styleable from CSS.
    @Test
    void selectionTokensAreStyleable() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        checkBox.setStyle("-m3-touch-target-size: 44px;");

        M3RadioButton radioButton = new M3RadioButton("Radio");
        radioButton.setStyle("-m3-touch-target-size: 46px;");

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setStyle("-m3-touch-target-size: 48px; -m3-track-shape: 18px;");

        applyCss(checkBox);
        applyCss(radioButton);
        applyCss(switchControl);

        assertEquals(44.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(44.0, checkBox.getPrefHeight(), 0.0001);
        assertEquals(46.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(46.0, radioButton.getPrefHeight(), 0.0001);
        assertEquals(48.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(18.0, switchControl.getTrackShape(), 0.0001);
        assertEquals(48.0, switchControl.getPrefHeight(), 0.0001);
    }

    /// Verifies that slider component token properties are styleable from CSS.
    @Test
    void sliderTokensAreStyleable() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setStyle(
                "-m3-track-thickness: 8px; "
                        + "-m3-track-shape: 12px; "
                        + "-m3-thumb-size: 28px; "
                        + "-m3-touch-target-size: 56px;"
        );

        applyCss(slider);

        assertEquals(8.0, slider.getTrackThickness(), 0.0001);
        assertEquals(12.0, slider.getTrackShape(), 0.0001);
        assertEquals(28.0, slider.getThumbSize(), 0.0001);
        assertEquals(56.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(56.0, slider.getPrefHeight(), 0.0001);
    }

    /// Verifies that progress component token properties are styleable from CSS.
    @Test
    void progressTokensAreStyleable() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setStyle("-m3-track-thickness: 6px; -m3-track-shape: 18px;");

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setStyle("-m3-indicator-size: 72px;");

        applyCss(progressBar);
        applyCss(progressIndicator);

        assertEquals(6.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(18.0, progressBar.getTrackShape(), 0.0001);
        assertEquals(6.0, progressBar.getPrefHeight(), 0.0001);
        assertEquals(72.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(72.0, progressIndicator.getPrefWidth(), 0.0001);
        assertEquals(72.0, progressIndicator.getPrefHeight(), 0.0001);
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

    /// Verifies that list items expose selected state and action behavior.
    @Test
    void listItemSupportsSelectionAndAction() {
        M3ListItem listItem = new M3ListItem("Headline");
        AtomicInteger fireCount = new AtomicInteger();
        listItem.setOnAction(event -> fireCount.incrementAndGet());
        listItem.setSelected(true);

        applyCss(listItem);

        assertTrue(listItem.isSelected());
        listItem.fire();
        assertEquals(1, fireCount.get());
        listItem.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        assertEquals(2, fireCount.get());
        listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertEquals(3, fireCount.get());
    }

    /// Verifies style classes for container controls.
    @Test
    void containerControlsExposeStyleClasses() {
        M3Card card = new M3Card();
        card.setVariant(M3CardVariant.OUTLINED);

        M3Snackbar snackbar = new M3Snackbar("Message");

        assertTrue(card.getStyleClass().contains(M3Card.STYLE_CLASS));
        assertTrue(card.getStyleClass().contains(M3CardVariant.OUTLINED.getStyleClass()));
        assertTrue(snackbar.getStyleClass().contains(M3Snackbar.STYLE_CLASS));
    }

    /// Verifies style classes for input and selection controls.
    @Test
    void inputAndSelectionControlsExposeStyleClasses() {
        M3TextField textField = new M3TextField();
        textField.setVariant(M3TextInputVariant.OUTLINED);

        M3Chip chip = new M3Chip("Chip");
        chip.setVariant(M3ChipVariant.FILTER);

        assertTrue(textField.getStyleClass().contains(M3TextField.STYLE_CLASS));
        assertTrue(textField.getStyleClass().contains(M3TextInputVariant.OUTLINED.getStyleClass()));
        assertTrue(new M3CheckBox().getStyleClass().contains(M3CheckBox.STYLE_CLASS));
        assertTrue(new M3RadioButton().getStyleClass().contains(M3RadioButton.STYLE_CLASS));
        assertTrue(new M3Switch().getStyleClass().contains(M3Switch.STYLE_CLASS));
        assertTrue(new M3Slider().getStyleClass().contains(M3Slider.STYLE_CLASS));
        assertTrue(chip.getStyleClass().contains(M3Chip.STYLE_CLASS));
        assertTrue(chip.getStyleClass().contains(M3ChipVariant.FILTER.getStyleClass()));
        assertTrue(new M3SegmentedButton("Day").getStyleClass().contains(M3SegmentedButton.STYLE_CLASS));
        assertTrue(new M3SegmentedButtonGroup().getStyleClass().contains(M3SegmentedButtonGroup.STYLE_CLASS));
        assertTrue(new M3Divider().getStyleClass().contains(M3Divider.STYLE_CLASS));
        assertTrue(new M3Badge("1").getStyleClass().contains(M3Badge.STYLE_CLASS));
        assertTrue(new M3ListItem("Item").getStyleClass().contains(M3ListItem.STYLE_CLASS));
    }

    /// Applies the m3fx stylesheet to a control in a scene.
    private static void applyCss(javafx.scene.Node node) {
        Pane root = new Pane(node);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }

    /// Creates a primary mouse event for control behavior tests.
    private static MouseEvent primaryMouseEvent(
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        return new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
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
                null
        );
    }

    /// Creates a key event for control behavior tests.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }
}
