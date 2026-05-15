package org.glavo.m3fx.skins;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.skin.ButtonSkin;
import javafx.util.Duration;
import org.glavo.m3fx.controls.M3Button;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for {@link M3Button}.
@NotNullByDefault
public class M3ButtonSkin extends ButtonSkin {
    /// The scale applied while the button is pressed.
    private static final double PRESSED_SCALE = 0.98;

    /// The duration used when entering the pressed state.
    private static final Duration PRESS_DURATION = Duration.millis(80.0);

    /// The duration used when leaving the pressed state.
    private static final Duration RELEASE_DURATION = Duration.millis(140.0);

    /// The press animation timeline.
    private final Timeline animation = new Timeline();

    /// Creates a button skin.
    public M3ButtonSkin(M3Button control) {
        super(control);
        control.setScaleX(1.0);
        control.setScaleY(1.0);
        control.armedProperty().addListener((observable, oldValue, newValue) -> animatePressedState(newValue));
        control.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                animation.stop();
                control.setScaleX(1.0);
                control.setScaleY(1.0);
            }
        });
    }

    /// Stops the animation before the skin is disposed.
    @Override
    public void dispose() {
        animation.stop();
        super.dispose();
    }

    /// Animates the skinnable button into or out of the pressed state.
    private void animatePressedState(boolean pressed) {
        M3Button button = (M3Button) getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        double scale = pressed ? PRESSED_SCALE : 1.0;
        Duration duration = pressed ? PRESS_DURATION : RELEASE_DURATION;
        animation.stop();
        animation.getKeyFrames().setAll(new KeyFrame(
                duration,
                new KeyValue(button.scaleXProperty(), scale, Interpolator.EASE_BOTH),
                new KeyValue(button.scaleYProperty(), scale, Interpolator.EASE_BOTH)
        ));
        animation.playFromStart();
    }
}
