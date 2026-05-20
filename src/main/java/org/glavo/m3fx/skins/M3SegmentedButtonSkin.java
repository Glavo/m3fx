// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3SegmentedButton].
@NotNullByDefault
public class M3SegmentedButtonSkin extends M3LabeledButtonSkinBase<M3SegmentedButton> {
    /// The selected container style class.
    public static final String SELECTION_CONTAINER_STYLE_CLASS = "m3-segmented-button-selection-container";

    /// The selected container animation duration.
    private static final Duration SELECTION_DURATION = M3Motion.SHORT4;

    /// The hidden selected container scale.
    private static final double HIDDEN_SELECTION_SCALE = 0.96;

    /// The selected container background layer.
    private final Region selectionContainer = new Region();

    /// The selected container appearance animation.
    private final Timeline selectionAnimation = new Timeline();

    /// Animates the selected container when selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectionContainer(newValue);

    /// Requests layout when group position style classes change.
    private final ListChangeListener<String> styleClassListener = change -> getSkinnable().requestLayout();

    /// Creates a segmented button skin.
    public M3SegmentedButtonSkin(M3SegmentedButton control) {
        super(control);
        selectionContainer.getStyleClass().add(SELECTION_CONTAINER_STYLE_CLASS);
        selectionContainer.setManaged(false);
        selectionContainer.setMouseTransparent(true);
        getChildren().add(0, selectionContainer);

        updateSelectionContainerImmediate(control.isSelected());
        control.selectedProperty().addListener(selectedListener);
        control.getStyleClass().addListener(styleClassListener);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3SegmentedButton button = getSkinnable();
        selectionAnimation.stop();
        button.selectedProperty().removeListener(selectedListener);
        button.getStyleClass().removeListener(styleClassListener);
        super.dispose();
    }

    /// Lays out labeled content and the selected container.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        layoutSelectionContainer();
    }

    /// Lays out the selected container inside the outline border.
    private void layoutSelectionContainer() {
        M3SegmentedButton button = getSkinnable();
        double width = button.getWidth();
        double height = button.getHeight();
        if (width <= 0.0) {
            width = button.getLayoutBounds().getWidth();
        }
        if (height <= 0.0) {
            height = button.getLayoutBounds().getHeight();
        }

        Insets borderInsets = borderInsets(button);
        double x = borderInsets.getLeft();
        double y = borderInsets.getTop();
        double selectionWidth = Math.max(0.0, width - borderInsets.getLeft() - borderInsets.getRight());
        double selectionHeight = Math.max(0.0, height - borderInsets.getTop() - borderInsets.getBottom());
        selectionContainer.resizeRelocate(x, y, selectionWidth, selectionHeight);
        updateSelectionContainerShape(button, selectionWidth, selectionHeight, borderInsets);
    }

    /// Updates the selected container shape for the current segment position.
    private void updateSelectionContainerShape(
            M3SegmentedButton button,
            double width,
            double height,
            Insets borderInsets
    ) {
        double topLeft = hasTopLeftCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), borderInsets.getLeft(), borderInsets.getTop())
                : 0.0;
        double topRight = hasTopRightCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), borderInsets.getRight(), borderInsets.getTop())
                : 0.0;
        double bottomRight = hasBottomRightCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), borderInsets.getRight(), borderInsets.getBottom())
                : 0.0;
        double bottomLeft = hasBottomLeftCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), borderInsets.getLeft(), borderInsets.getBottom())
                : 0.0;

        selectionContainer.setStyle("-fx-background-radius: "
                + formatPixels(topLeft) + " "
                + formatPixels(topRight) + " "
                + formatPixels(bottomRight) + " "
                + formatPixels(bottomLeft) + ";");
        selectionContainer.applyCss();
    }

    /// Animates the selected container to the requested state.
    private void animateSelectionContainer(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_SELECTION_SCALE;
        selectionAnimation.stop();
        selectionAnimation.getKeyFrames().setAll(new KeyFrame(
                SELECTION_DURATION,
                new KeyValue(selectionContainer.opacityProperty(), targetOpacity, M3Motion.STANDARD),
                new KeyValue(selectionContainer.scaleXProperty(), targetScale, M3Motion.STANDARD)
        ));
        selectionAnimation.playFromStart();
    }

    /// Updates the selected container without animation.
    private void updateSelectionContainerImmediate(boolean selected) {
        selectionContainer.setOpacity(selected ? 1.0 : 0.0);
        selectionContainer.setScaleX(selected ? 1.0 : HIDDEN_SELECTION_SCALE);
    }

    /// Returns the first border stroke insets for the skinnable button.
    private static Insets borderInsets(M3SegmentedButton button) {
        Border border = button.getBorder();
        if (border == null || border.getStrokes().isEmpty()) {
            return Insets.EMPTY;
        }

        BorderWidths widths = border.getStrokes().get(0).getWidths();
        return new Insets(widths.getTop(), widths.getRight(), widths.getBottom(), widths.getLeft());
    }

    /// Returns whether the current segment has a rounded top-left corner.
    private static boolean hasTopLeftCorner(M3SegmentedButton button) {
        return button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS)
                || !hasKnownSegmentPosition(button);
    }

    /// Returns whether the current segment has a rounded top-right corner.
    private static boolean hasTopRightCorner(M3SegmentedButton button) {
        return button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS)
                || !hasKnownSegmentPosition(button);
    }

    /// Returns whether the current segment has a rounded bottom-right corner.
    private static boolean hasBottomRightCorner(M3SegmentedButton button) {
        return hasTopRightCorner(button);
    }

    /// Returns whether the current segment has a rounded bottom-left corner.
    private static boolean hasBottomLeftCorner(M3SegmentedButton button) {
        return hasTopLeftCorner(button);
    }

    /// Returns whether the current button has one of the segment position classes.
    private static boolean hasKnownSegmentPosition(M3SegmentedButton button) {
        return button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS);
    }

    /// Resolves a rounded inner corner radius after removing the outline stroke.
    private static double innerCornerRadius(
            double width,
            double height,
            double shapeRadius,
            double horizontalInset,
            double verticalInset
    ) {
        double innerShapeRadius = Math.max(0.0, shapeRadius - Math.max(horizontalInset, verticalInset));
        return Math.min(innerShapeRadius, Math.max(0.0, Math.min(width, height) / 2.0));
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
