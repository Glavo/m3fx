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
import org.glavo.m3fx.controls.M3CheckBox;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3CheckBox].
@NotNullByDefault
public class M3CheckBoxSkin extends M3SelectionControlSkinBase<M3CheckBox> {
    /// The checkbox selected mark transition duration.
    private static final Duration SELECTION_DURATION = M3Motion.SHORT2;

    /// The hidden selected mark scale.
    private static final double HIDDEN_MARK_SCALE = 0.72;

    /// The visual checkbox container size.
    private static final double BOX_SIZE = 18.0;

    /// The visual check mark width.
    private static final double MARK_WIDTH = 12.0;

    /// The visual check mark height.
    private static final double MARK_HEIGHT = 10.0;

    /// The visual checkbox container.
    private final StackPane box = new StackPane();

    /// The visual selected check mark.
    private final Region mark = new Region();

    /// The selected mark appearance animation.
    private final Timeline selectionAnimation = new Timeline();

    /// Applies touch target token changes to checkbox geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Animates the mark after selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateMarkState();

    /// Animates the mark after indeterminate state changes.
    private final ChangeListener<Boolean> indeterminateListener =
            (observable, oldValue, newValue) -> animateMarkState();

    /// Creates a checkbox skin.
    public M3CheckBoxSkin(M3CheckBox control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-checkbox-box");
        mark.getStyleClass().addAll("mark", "m3-checkbox-mark");
        indicatorSlot().getChildren().addAll(box, mark);

        applyMarkState(control.isSelected() || control.isIndeterminate());
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.selectedProperty().addListener(selectedListener);
        control.indeterminateProperty().addListener(indeterminateListener);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedProperty().removeListener(selectedListener);
        getSkinnable().indeterminateProperty().removeListener(indeterminateListener);
        super.dispose();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        double touchTargetSize = getSkinnable().getTouchTargetSize();
        setIndicatorSlotSize(touchTargetSize, touchTargetSize);
        setFixedSize(box, BOX_SIZE, BOX_SIZE);
        setFixedSize(mark, MARK_WIDTH, MARK_HEIGHT);
    }

    /// Applies the selected mark state without animation.
    private void applyMarkState(boolean visible) {
        mark.setOpacity(visible ? 1.0 : 0.0);
        mark.setScaleX(visible ? 1.0 : HIDDEN_MARK_SCALE);
        mark.setScaleY(visible ? 1.0 : HIDDEN_MARK_SCALE);
    }

    /// Animates the selected or indeterminate mark state.
    private void animateMarkState() {
        boolean visible = getSkinnable().isSelected() || getSkinnable().isIndeterminate();
        selectionAnimation.stop();
        selectionAnimation.getKeyFrames().setAll(new KeyFrame(
                SELECTION_DURATION,
                new KeyValue(mark.opacityProperty(), visible ? 1.0 : 0.0, M3Motion.STANDARD),
                new KeyValue(mark.scaleXProperty(), visible ? 1.0 : HIDDEN_MARK_SCALE, M3Motion.STANDARD),
                new KeyValue(mark.scaleYProperty(), visible ? 1.0 : HIDDEN_MARK_SCALE, M3Motion.STANDARD)
        ));
        selectionAnimation.playFromStart();
    }
}
