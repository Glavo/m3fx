// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Switch].
@NotNullByDefault
public class M3SwitchSkin extends M3SelectionControlSkinBase<M3Switch> {
    /// The visual switch track.
    private final StackPane box = new StackPane();

    /// The visual switch thumb.
    private final StackPane thumb = new StackPane();

    /// The animated thumb position from off to on.
    private final DoubleProperty thumbPosition = new SimpleDoubleProperty(this, "thumbPosition");

    /// The thumb position animation.
    private final M3DoubleTransition selectionAnimation = new M3DoubleTransition(thumbPosition);

    /// Requests layout after thumb position changes.
    private final InvalidationListener thumbPositionListener = observable -> getSkinnable().requestLayout();

    /// Applies size token changes to the switch layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Applies track shape token changes to the switch track.
    private final InvalidationListener trackShapeInvalidation = observable -> updateTrackStyle();

    /// Relayouts the pressed handle size.
    private final InvalidationListener armedInvalidation = observable -> getSkinnable().requestLayout();

    /// Settles running thumb transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), selectionAnimation)
            );

    /// Animates the thumb after selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateThumbPosition(newValue);

    /// Creates a switch skin.
    public M3SwitchSkin(M3Switch control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-switch-track");
        thumb.getStyleClass().addAll("thumb", "m3-switch-thumb");
        thumb.setManaged(false);
        indicatorSlot().getChildren().addAll(box, thumb);
        box.toBack();
        thumbPosition.set(control.isSelected() ? 1.0 : 0.0);
        thumbPosition.addListener(thumbPositionListener);

        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.trackWidthProperty().addListener(metricsInvalidation);
        control.trackHeightProperty().addListener(metricsInvalidation);
        control.stateLayerSizeProperty().addListener(metricsInvalidation);
        control.unselectedHandleSizeProperty().addListener(metricsInvalidation);
        control.selectedHandleSizeProperty().addListener(metricsInvalidation);
        control.pressedHandleSizeProperty().addListener(metricsInvalidation);
        control.trackShapeProperty().addListener(trackShapeInvalidation);
        control.armedProperty().addListener(armedInvalidation);
        control.selectedProperty().addListener(selectedListener);
    }

    /// Stops animations before the skin is disposed.
    @Override
    public void dispose() {
        M3Switch control = getSkinnable();
        selectionAnimation.stop();
        thumbPosition.removeListener(thumbPositionListener);
        control.touchTargetSizeProperty().removeListener(metricsInvalidation);
        control.trackWidthProperty().removeListener(metricsInvalidation);
        control.trackHeightProperty().removeListener(metricsInvalidation);
        control.stateLayerSizeProperty().removeListener(metricsInvalidation);
        control.unselectedHandleSizeProperty().removeListener(metricsInvalidation);
        control.selectedHandleSizeProperty().removeListener(metricsInvalidation);
        control.pressedHandleSizeProperty().removeListener(metricsInvalidation);
        control.trackShapeProperty().removeListener(trackShapeInvalidation);
        control.armedProperty().removeListener(armedInvalidation);
        motionSettingsObserver.dispose();
        control.selectedProperty().removeListener(selectedListener);
        super.dispose();
    }

    /// Lays out the selection control and switch thumb.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        layoutThumb();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        M3Switch control = getSkinnable();
        double trackWidth = control.getTrackWidth();
        double trackHeight = control.getTrackHeight();
        double touchTargetHeight = Math.max(Math.max(control.getTouchTargetSize(), trackHeight), control.getStateLayerSize());
        setIndicatorSlotSize(trackWidth, touchTargetHeight);
        setFixedSize(box, trackWidth, trackHeight);
        updateTrackStyle();
        control.requestLayout();
    }

    /// Applies the switch track shape token to the visual track.
    private void updateTrackStyle() {
        String shape = formatPixels(getSkinnable().getTrackShape());
        box.setStyle("-fx-background-radius: " + shape + "; -fx-border-radius: " + shape + ";");
    }

    /// Animates the thumb to the selected or unselected position.
    private void animateThumbPosition(boolean selected) {
        selectionAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        selectionAnimation.configure(spec, selected ? 1.0 : 0.0);
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Lays out the thumb from the animated position value.
    private void layoutThumb() {
        M3Switch control = getSkinnable();
        double position = thumbPosition.get();
        double trackWidth = control.getTrackWidth();
        double trackHeight = control.getTrackHeight();
        double touchTargetHeight = Math.max(Math.max(control.getTouchTargetSize(), trackHeight), control.getStateLayerSize());
        double unselectedThumbSize = control.getUnselectedHandleSize();
        double selectedThumbSize = control.getSelectedHandleSize();
        double thumbSize = control.isArmed()
                ? control.getPressedHandleSize()
                : unselectedThumbSize + (selectedThumbSize - unselectedThumbSize) * position;
        double offThumbCenterX = trackHeight / 2.0;
        double onThumbCenterX = trackWidth - trackHeight / 2.0;
        double thumbCenterX = offThumbCenterX + (onThumbCenterX - offThumbCenterX) * position;
        double thumbX = thumbCenterX - thumbSize / 2.0;
        double thumbY = (touchTargetHeight - thumbSize) / 2.0;
        double stateLayerSize = control.getStateLayerSize();
        double stateLayerX = thumbCenterX - stateLayerSize / 2.0;
        double stateLayerY = (touchTargetHeight - stateLayerSize) / 2.0;
        layoutIndicatorStateLayer(
                stateLayerX,
                stateLayerY,
                stateLayerSize,
                stateLayerSize,
                stateLayerSize / 2.0
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbSize);
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }
}
