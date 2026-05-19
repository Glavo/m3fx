// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3Switch;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Switch].
@NotNullByDefault
public class M3SwitchSkin extends M3SelectionControlSkinBase<M3Switch> {
    /// The switch state transition duration.
    private static final Duration SELECTION_DURATION = M3Motion.SHORT3;

    /// The switch track width.
    private static final double TRACK_WIDTH = 52.0;

    /// The switch track height.
    private static final double TRACK_HEIGHT = 32.0;

    /// The off-state thumb center within the track.
    private static final double OFF_THUMB_CENTER_X = 16.0;

    /// The on-state thumb center within the track.
    private static final double ON_THUMB_CENTER_X = 36.0;

    /// The visual switch track.
    private final StackPane box = new StackPane();

    /// The visual switch thumb.
    private final StackPane thumb = new StackPane();

    /// The animated thumb position from off to on.
    private final DoubleProperty thumbPosition = new SimpleDoubleProperty(this, "thumbPosition");

    /// The thumb position animation.
    private final Timeline selectionAnimation = new Timeline();

    /// Requests layout after thumb position changes.
    private final InvalidationListener thumbPositionListener = observable -> getSkinnable().requestLayout();

    /// Applies size token changes to the switch layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Applies track shape token changes to the switch track.
    private final InvalidationListener trackShapeInvalidation = observable -> updateTrackStyle();

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
        thumbPosition.set(control.isSelected() ? 1.0 : 0.0);
        thumbPosition.addListener(thumbPositionListener);

        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.trackShapeProperty().addListener(trackShapeInvalidation);
        control.selectedProperty().addListener(selectedListener);
    }

    /// Stops animations before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        thumbPosition.removeListener(thumbPositionListener);
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().trackShapeProperty().removeListener(trackShapeInvalidation);
        getSkinnable().selectedProperty().removeListener(selectedListener);
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
        double touchTargetHeight = Math.max(getSkinnable().getTouchTargetSize(), TRACK_HEIGHT);
        setIndicatorSlotSize(TRACK_WIDTH, touchTargetHeight);
        setFixedSize(box, TRACK_WIDTH, TRACK_HEIGHT);
        updateTrackStyle();
    }

    /// Applies the switch track shape token to the visual track.
    private void updateTrackStyle() {
        String shape = formatPixels(getSkinnable().getTrackShape());
        box.setStyle("-fx-background-radius: " + shape + "; -fx-border-radius: " + shape + ";");
    }

    /// Animates the thumb to the selected or unselected position.
    private void animateThumbPosition(boolean selected) {
        selectionAnimation.stop();
        selectionAnimation.getKeyFrames().setAll(new KeyFrame(
                SELECTION_DURATION,
                new KeyValue(thumbPosition, selected ? 1.0 : 0.0, M3Motion.STANDARD)
        ));
        selectionAnimation.playFromStart();
    }

    /// Lays out the thumb from the animated position value.
    private void layoutThumb() {
        double thumbWidth = thumb.prefWidth(-1.0);
        double thumbHeight = thumb.prefHeight(-1.0);
        double thumbCenterX = OFF_THUMB_CENTER_X + (ON_THUMB_CENTER_X - OFF_THUMB_CENTER_X) * thumbPosition.get();
        double thumbX = thumbCenterX - thumbWidth / 2.0;
        double touchTargetHeight = Math.max(getSkinnable().getTouchTargetSize(), TRACK_HEIGHT);
        double thumbY = (touchTargetHeight - thumbHeight) / 2.0;
        thumb.resizeRelocate(thumbX, thumbY, thumbWidth, thumbHeight);
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
