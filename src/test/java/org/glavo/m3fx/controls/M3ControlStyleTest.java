package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    }
}
