// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3RadioButton].
@NotNullByDefault
public class M3RadioButtonSkin extends M3SelectionControlSkinBase<M3RadioButton> {
    /// The hidden selected dot scale.
    private static final double HIDDEN_DOT_SCALE = 0.64;

    /// The visual radio indicator size.
    private static final double RADIO_SIZE = 20.0;

    /// The selected radio dot size.
    private static final double DOT_SIZE = 10.0;

    /// The visual radio indicator container.
    private final Pane radio = new Pane();

    /// The selected and unselected radio ring.
    private final Circle ring = new Circle();

    /// The selected radio dot.
    private final Circle dot = new Circle();

    /// The selected dot appearance animation.
    private final Timeline selectionAnimation = new Timeline();

    /// Applies touch target token changes to radio geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Settles running dot transitions when runtime motion settings change.
    private final InvalidationListener motionSettingsInvalidation =
            observable -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), selectionAnimation);

    /// Animates the selected dot after selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectedState(newValue);

    /// Creates a radio button skin.
    ///
    /// @param control the skinned radio button
    public M3RadioButtonSkin(M3RadioButton control) {
        super(control);
        radio.getStyleClass().addAll("radio", "m3-radio");
        ring.getStyleClass().addAll("ring", "m3-radio-ring");
        dot.getStyleClass().addAll("dot", "m3-radio-dot");
        configureCircle(ring);
        configureCircle(dot);
        ring.setStrokeType(StrokeType.INSIDE);
        radio.getChildren().addAll(ring, dot);
        indicatorSlot().getChildren().add(radio);

        applySelectedState(control.isSelected());
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        M3MotionSettings.addSettingsChangeListener(motionSettingsInvalidation);
        control.selectedProperty().addListener(selectedListener);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        M3MotionSettings.removeSettingsChangeListener(motionSettingsInvalidation);
        getSkinnable().selectedProperty().removeListener(selectedListener);
        super.dispose();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        setIndicatorSlotSize(touchTargetSize, touchTargetSize);
        setFixedSize(radio, RADIO_SIZE, RADIO_SIZE);
        layoutCircle(ring, RADIO_SIZE / 2.0, RADIO_SIZE / 2.0, RADIO_SIZE / 2.0);
        layoutCircle(dot, RADIO_SIZE / 2.0, RADIO_SIZE / 2.0, DOT_SIZE / 2.0);
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
        M3MotionSpec spec = M3Animation.fastEffects(getSkinnable());
        selectionAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(dot.opacityProperty(), selected ? 1.0 : 0.0, spec.interpolator()),
                new KeyValue(dot.scaleXProperty(), selected ? 1.0 : HIDDEN_DOT_SCALE, spec.interpolator()),
                new KeyValue(dot.scaleYProperty(), selected ? 1.0 : HIDDEN_DOT_SCALE, spec.interpolator())
        ));
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Configures a circle for unmanaged indicator painting.
    private static void configureCircle(Circle circle) {
        circle.setManaged(false);
        circle.setMouseTransparent(true);
        circle.setSmooth(true);
    }

    /// Positions a circle at the requested center.
    private static void layoutCircle(Circle circle, double centerX, double centerY, double radius) {
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
        circle.setRadius(radius);
    }
}
