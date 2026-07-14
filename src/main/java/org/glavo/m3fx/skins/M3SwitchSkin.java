// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Switch].
@NotNullByDefault
public class M3SwitchSkin extends M3SelectionControlSkinBase<M3Switch> {
    /// The style class applied temporarily to the currently displayed handle icon.
    private static final String HANDLE_ICON_STYLE_CLASS = "m3-switch-handle-icon";

    /// The visual switch track.
    private final StackPane box = new StackPane();

    /// The visual switch thumb.
    private final StackPane thumb = new StackPane();

    /// The fixed-size slot that centers the current handle icon.
    private final StackPane iconSlot = new StackPane();

    /// The application-provided icon currently mounted in the handle.
    private @Nullable Node displayedIcon;

    /// Whether this skin added the managed handle icon style class to [displayedIcon].
    private boolean displayedIconStyleClassAdded;

    /// The animated thumb position from off to on.
    private final DoubleProperty thumbPosition = new SimpleDoubleProperty(this, "thumbPosition");

    /// The thumb position animation.
    private final M3DoubleTransition selectionAnimation = new M3DoubleTransition(thumbPosition);

    /// The animated fraction of the pressed handle size.
    private final DoubleProperty pressedProgress = new SimpleDoubleProperty(this, "pressedProgress");

    /// The pressed handle size animation.
    private final M3DoubleTransition pressedAnimation = new M3DoubleTransition(pressedProgress);

    /// Applies animated thumb position changes directly to the internal nodes.
    private final InvalidationListener thumbPositionListener = observable -> layoutThumb();

    /// Applies animated pressed-size changes directly to the internal nodes.
    private final InvalidationListener pressedProgressListener = observable -> layoutThumb();

    /// Applies size token changes to the switch layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Applies track shape token changes to the switch track.
    private final InvalidationListener trackShapeInvalidation = observable -> updateTrackStyle();

    /// Starts a pressed handle size transition when armed state changes.
    private final InvalidationListener armedInvalidation = observable -> {
        pressedAnimation.configure(
                M3Animation.fastSpatial(getSkinnable()),
                getSkinnable().isArmed() ? 1.0 : 0.0
        );
        M3Animation.playFromStart(getSkinnable(), pressedAnimation);
    };

    /// Animates the thumb after selection changes.
    private final ChangeListener<Boolean> selectedListener = (observable, oldValue, newValue) -> {
        updateDisplayedIcon();
        animateThumbPosition(newValue);
    };

    /// Creates a switch skin.
    public M3SwitchSkin(M3Switch control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-switch-track");
        thumb.getStyleClass().addAll("thumb", "m3-switch-thumb");
        iconSlot.getStyleClass().add("m3-switch-icon-slot");
        thumb.getChildren().add(iconSlot);
        thumb.setManaged(false);
        indicatorSlot().getChildren().addAll(box, thumb);
        box.toBack();
        thumbPosition.set(control.isSelected() ? 1.0 : 0.0);
        thumbPosition.addListener(thumbPositionListener);
        pressedProgress.set(control.isArmed() ? 1.0 : 0.0);
        pressedProgress.addListener(pressedProgressListener);

        updateDisplayedIcon();
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.trackWidthProperty().addListener(metricsInvalidation);
        control.trackHeightProperty().addListener(metricsInvalidation);
        control.stateLayerSizeProperty().addListener(metricsInvalidation);
        control.unselectedHandleSizeProperty().addListener(metricsInvalidation);
        control.withIconHandleSizeProperty().addListener(metricsInvalidation);
        control.selectedHandleSizeProperty().addListener(metricsInvalidation);
        control.pressedHandleSizeProperty().addListener(metricsInvalidation);
        control.iconSizeProperty().addListener(metricsInvalidation);
        control.trackShapeProperty().addListener(trackShapeInvalidation);
        control.armedProperty().addListener(armedInvalidation);
        control.selectedProperty().addListener(selectedListener);
    }

    /// Stops animations before the skin is disposed.
    @Override
    public void dispose() {
        M3Switch control = getSkinnable();
        selectionAnimation.stop();
        pressedAnimation.stop();
        thumbPosition.removeListener(thumbPositionListener);
        pressedProgress.removeListener(pressedProgressListener);
        control.touchTargetSizeProperty().removeListener(metricsInvalidation);
        control.trackWidthProperty().removeListener(metricsInvalidation);
        control.trackHeightProperty().removeListener(metricsInvalidation);
        control.stateLayerSizeProperty().removeListener(metricsInvalidation);
        control.unselectedHandleSizeProperty().removeListener(metricsInvalidation);
        control.withIconHandleSizeProperty().removeListener(metricsInvalidation);
        control.selectedHandleSizeProperty().removeListener(metricsInvalidation);
        control.pressedHandleSizeProperty().removeListener(metricsInvalidation);
        control.iconSizeProperty().removeListener(metricsInvalidation);
        control.trackShapeProperty().removeListener(trackShapeInvalidation);
        control.armedProperty().removeListener(armedInvalidation);
        control.selectedProperty().removeListener(selectedListener);
        setDisplayedIcon(null);
        thumb.getChildren().clear();
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
        double iconSize = control.getIconSize();
        setFixedSize(iconSlot, iconSize, iconSize);
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
        updateDisplayedIcon();
        double position = thumbPosition.get();
        double trackWidth = control.getTrackWidth();
        double trackHeight = control.getTrackHeight();
        double touchTargetHeight = Math.max(Math.max(control.getTouchTargetSize(), trackHeight), control.getStateLayerSize());
        double withIconThumbSize = control.getWithIconHandleSize();
        double unselectedThumbSize = control.getUnselectedIcon() == null
                ? control.getUnselectedHandleSize()
                : withIconThumbSize;
        double selectedThumbSize = control.getSelectedIcon() == null
                ? control.getSelectedHandleSize()
                : withIconThumbSize;
        double restingThumbSize = unselectedThumbSize + (selectedThumbSize - unselectedThumbSize) * position;
        double thumbSize = restingThumbSize
                + (control.getPressedHandleSize() - restingThumbSize) * pressedProgress.get();
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
        updateIconOpacity(position);
    }

    /// Mounts the icon for the current selected state without retaining the inactive icon in the scene graph.
    private void updateDisplayedIcon() {
        M3Switch control = getSkinnable();
        setDisplayedIcon(control.isSelected() ? control.getSelectedIcon() : control.getUnselectedIcon());
    }

    /// Replaces the mounted handle icon and restores the previous node's application-owned style classes.
    private void setDisplayedIcon(@Nullable Node icon) {
        if (displayedIcon == icon) {
            return;
        }

        Node oldIcon = displayedIcon;
        if (oldIcon != null && displayedIconStyleClassAdded) {
            oldIcon.getStyleClass().remove(HANDLE_ICON_STYLE_CLASS);
        }
        iconSlot.getChildren().clear();
        displayedIcon = icon;
        displayedIconStyleClassAdded = false;
        if (icon != null) {
            if (!icon.getStyleClass().contains(HANDLE_ICON_STYLE_CLASS)) {
                icon.getStyleClass().add(HANDLE_ICON_STYLE_CLASS);
                displayedIconStyleClassAdded = true;
            }
            iconSlot.getChildren().add(icon);
        }
        iconSlot.setVisible(icon != null);
    }

    /// Fades the destination-state icon in as the handle moves between switch states.
    private void updateIconOpacity(double position) {
        Node icon = displayedIcon;
        if (icon == null) {
            return;
        }

        M3Switch control = getSkinnable();
        if (control.getSelectedIcon() == control.getUnselectedIcon()) {
            iconSlot.setOpacity(1.0);
        } else {
            iconSlot.setOpacity(control.isSelected() ? position : 1.0 - position);
        }
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }
}
