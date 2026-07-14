// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3SegmentedButton].
@NotNullByDefault
public class M3SegmentedButtonSkin extends M3LabeledButtonSkinBase<M3SegmentedButton> {
    /// The selected container style class.
    public static final String SELECTION_CONTAINER_STYLE_CLASS = "m3-segmented-button-selection-container";

    /// The hidden selected container scale.
    private static final double HIDDEN_SELECTION_SCALE = 0.96;

    /// The selected container background layer.
    private final Region selectionContainer = new Region();

    /// The selected container appearance animation.
    private final M3NodeTransition selectionAnimation = new M3NodeTransition(selectionContainer);

    /// The last top-left radius applied to the selected container.
    private double selectionTopLeftRadius = Double.NaN;

    /// The last top-right radius applied to the selected container.
    private double selectionTopRightRadius = Double.NaN;

    /// The last bottom-right radius applied to the selected container.
    private double selectionBottomRightRadius = Double.NaN;

    /// The last bottom-left radius applied to the selected container.
    private double selectionBottomLeftRadius = Double.NaN;

    /// Animates the selected container when selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectionContainer(newValue);

    /// Requests layout when group position style classes change.
    private final ListChangeListener<String> styleClassListener = change -> getSkinnable().requestLayout();

    /// Creates a segmented button skin.
    ///
    /// @param control the segmented button controlled by this skin
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

        @Nullable BorderWidths borderWidths = null;
        Border border = button.getBorder();
        if (border != null && !border.getStrokes().isEmpty()) {
            borderWidths = border.getStrokes().get(0).getWidths();
        }
        double top = borderWidths == null ? 0.0 : borderWidths.getTop();
        double right = borderWidths == null ? 0.0 : borderWidths.getRight();
        double bottom = borderWidths == null ? 0.0 : borderWidths.getBottom();
        double left = borderWidths == null ? 0.0 : borderWidths.getLeft();
        double selectionWidth = Math.max(0.0, width - left - right);
        double selectionHeight = Math.max(0.0, height - top - bottom);
        selectionContainer.resizeRelocate(left, top, selectionWidth, selectionHeight);
        updateSelectionContainerShape(
                button,
                selectionWidth,
                selectionHeight,
                top,
                right,
                bottom,
                left
        );
    }

    /// Updates the selected container shape for the current segment position.
    private void updateSelectionContainerShape(
            M3SegmentedButton button,
            double width,
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double topLeft = hasTopLeftCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), leftInset, topInset)
                : 0.0;
        double topRight = hasTopRightCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), rightInset, topInset)
                : 0.0;
        double bottomRight = hasTopRightCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), rightInset, bottomInset)
                : 0.0;
        double bottomLeft = hasTopLeftCorner(button)
                ? innerCornerRadius(width, height, button.getContainerShape(), leftInset, bottomInset)
                : 0.0;

        if (Double.compare(selectionTopLeftRadius, topLeft) == 0
                && Double.compare(selectionTopRightRadius, topRight) == 0
                && Double.compare(selectionBottomRightRadius, bottomRight) == 0
                && Double.compare(selectionBottomLeftRadius, bottomLeft) == 0) {
            return;
        }
        selectionTopLeftRadius = topLeft;
        selectionTopRightRadius = topRight;
        selectionBottomRightRadius = bottomRight;
        selectionBottomLeftRadius = bottomLeft;
        String style = "-fx-background-radius: "
                + formatPixels(topLeft) + " "
                + formatPixels(topRight) + " "
                + formatPixels(bottomRight) + " "
                + formatPixels(bottomLeft) + ";";
        selectionContainer.setStyle(style);
        selectionContainer.applyCss();
    }

    /// Animates the selected container to the requested state.
    private void animateSelectionContainer(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_SELECTION_SCALE;
        selectionAnimation.stop();
        M3MotionSpec spec = M3Animation.defaultEffects(getSkinnable());
        selectionAnimation.configure(
                spec,
                targetOpacity,
                targetScale,
                selectionContainer.getScaleY(),
                selectionContainer.getTranslateX(),
                selectionContainer.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Updates the selected container without animation.
    private void updateSelectionContainerImmediate(boolean selected) {
        selectionContainer.setOpacity(selected ? 1.0 : 0.0);
        selectionContainer.setScaleX(selected ? 1.0 : HIDDEN_SELECTION_SCALE);
    }

    /// Returns whether the current segment has a rounded top-left corner.
    private static boolean hasTopLeftCorner(M3SegmentedButton button) {
        return button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS)
                || hasNoSegmentPosition(button);
    }

    /// Returns whether the current segment has a rounded top-right corner.
    private static boolean hasTopRightCorner(M3SegmentedButton button) {
        return button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)
                || button.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS)
                || hasNoSegmentPosition(button);
    }

    /// Returns whether the current button has no segment position class.
    private static boolean hasNoSegmentPosition(M3SegmentedButton button) {
        return !button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)
                && !button.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS)
                && !button.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS)
                && !button.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS);
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
            return (long) value + "px";
        }
        return value + "px";
    }
}
