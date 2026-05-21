// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3RadioButton].
@NotNullByDefault
public class M3RadioButtonSkin extends M3SelectionControlSkinBase<M3RadioButton> {
    /// The radio selected dot transition duration.
    private static final Duration SELECTION_DURATION = M3Motion.SHORT2;

    /// The hidden selected dot scale.
    private static final double HIDDEN_DOT_SCALE = 0.64;

    /// The visual radio indicator size.
    private static final double RADIO_SIZE = 20.0;

    /// The selected radio dot size.
    private static final double DOT_SIZE = 10.0;

    /// The visual radio indicator.
    private final StackPane radio = new StackPane();

    /// The selected radio dot.
    private final Region dot = new Region();

    /// The selected dot appearance animation.
    private final Timeline selectionAnimation = new Timeline();

    /// Applies touch target token changes to radio geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Animates the selected dot after selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectedState(newValue);

    /// Creates a radio button skin.
    public M3RadioButtonSkin(M3RadioButton control) {
        super(control);
        radio.getStyleClass().addAll("radio", "m3-radio");
        dot.getStyleClass().addAll("dot", "m3-radio-dot");
        radio.getChildren().add(dot);
        indicatorSlot().getChildren().add(radio);

        applySelectedState(control.isSelected());
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.selectedProperty().addListener(selectedListener);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedProperty().removeListener(selectedListener);
        super.dispose();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        setIndicatorSlotSize(touchTargetSize, touchTargetSize);
        setFixedSize(radio, RADIO_SIZE, RADIO_SIZE);
        setFixedSize(dot, DOT_SIZE, DOT_SIZE);
    }

    /// Applies the selected dot state without animation.
    private void applySelectedState(boolean selected) {
        dot.setOpacity(selected ? 1.0 : 0.0);
        dot.setScaleX(selected ? 1.0 : HIDDEN_DOT_SCALE);
        dot.setScaleY(selected ? 1.0 : HIDDEN_DOT_SCALE);
    }

    /// Animates the selected dot state.
    private void animateSelectedState(boolean selected) {
        selectionAnimation.stop();
        selectionAnimation.getKeyFrames().setAll(new KeyFrame(
                SELECTION_DURATION,
                new KeyValue(dot.opacityProperty(), selected ? 1.0 : 0.0, M3Motion.STANDARD),
                new KeyValue(dot.scaleXProperty(), selected ? 1.0 : HIDDEN_DOT_SCALE, M3Motion.STANDARD),
                new KeyValue(dot.scaleYProperty(), selected ? 1.0 : HIDDEN_DOT_SCALE, M3Motion.STANDARD)
        ));
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }
}
