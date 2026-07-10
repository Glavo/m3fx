// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3RadioButton].
@NotNullByDefault
public class M3RadioButtonSkin extends M3SelectionControlSkinBase<M3RadioButton> {
    /// The hidden selected dot scale.
    private static final double HIDDEN_DOT_SCALE = 0.64;


    /// The visual radio indicator container.
    private final Pane radio = new Pane();

    /// The selected and unselected radio ring.
    private final Circle ring = new Circle();

    /// The selected radio dot.
    private final Circle dot = new Circle();

    /// The selected dot appearance animation.
    private final M3NodeTransition selectionAnimation = new M3NodeTransition(dot);

    /// Applies radio geometry token changes to skin nodes.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Settles running dot transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), selectionAnimation)
            );

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
        control.stateLayerSizeProperty().addListener(metricsInvalidation);
        control.containerSizeProperty().addListener(metricsInvalidation);
        control.selectedDotSizeProperty().addListener(metricsInvalidation);
        control.selectedProperty().addListener(selectedListener);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().stateLayerSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().containerSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedDotSizeProperty().removeListener(metricsInvalidation);
        motionSettingsObserver.dispose();
        getSkinnable().selectedProperty().removeListener(selectedListener);
        super.dispose();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        M3RadioButton control = getSkinnable();
        double touchTargetSize = control.getTouchTargetSize();
        double stateLayerSize = control.getStateLayerSize();
        double slotSize = Math.max(touchTargetSize, stateLayerSize);
        double layerOffset = (slotSize - stateLayerSize) / 2.0;
        double containerSize = control.getContainerSize();
        double center = containerSize / 2.0;

        setIndicatorSlotSize(slotSize, slotSize);
        layoutIndicatorStateLayer(layerOffset, layerOffset, stateLayerSize, stateLayerSize, stateLayerSize / 2.0);
        setFixedSize(radio, containerSize, containerSize);
        layoutCircle(ring, center, center, containerSize / 2.0);
        layoutCircle(dot, center, center, control.getSelectedDotSize() / 2.0);
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
        double targetScale = selected ? 1.0 : HIDDEN_DOT_SCALE;
        selectionAnimation.configure(spec, selected ? 1.0 : 0.0, targetScale, targetScale);
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
