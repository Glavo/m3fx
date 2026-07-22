// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3SegmentedButton].
///
/// The skin augments labeled-button content with an animated selection container and optional selected-state
/// indicator. Group position and node orientation determine the outer edge shape; content and selection geometry are
/// updated without changing the button's requested size during state transitions.
@NotNullByDefault
public class M3SegmentedButtonSkin extends M3LabeledButtonSkinBase<M3SegmentedButton> {
    /// The selected container style class.
    public static final String SELECTION_CONTAINER_STYLE_CLASS = "m3-segmented-button-selection-container";

    /// The built-in selected-state check indicator style class.
    public static final String SELECTION_INDICATOR_STYLE_CLASS = "m3-segmented-button-selection-indicator";

    /// The skin-managed class that hides an application graphic while the selected check replaces it.
    private static final String REPLACED_GRAPHIC_STYLE_CLASS =
            "m3-segmented-button-replaced-graphic";

    /// The checkmark layer inside the selected-state indicator.
    private static final String SELECTION_INDICATOR_MARK_STYLE_CLASS =
            "m3-segmented-button-selection-indicator-mark";

    /// The hidden selected container scale.
    private static final double HIDDEN_SELECTION_SCALE = 0.96;

    /// The hidden selected-state indicator scale.
    private static final double HIDDEN_INDICATOR_SCALE = 0.8;

    /// The Material segmented-button icon size.
    private static final double INDICATOR_SIZE = 18.0;

    /// The Material spacing between an icon and label.
    private static final double INDICATOR_GAP = 8.0;

    /// The selected container background layer.
    private final Region selectionContainer = new Region();

    /// The selected container appearance animation.
    private final M3NodeTransition selectionAnimation = new M3NodeTransition(selectionContainer);

    /// The built-in selected-state check indicator.
    private final StackPane selectionIndicator = new StackPane();

    /// The selected-state check indicator appearance animation.
    private final M3NodeTransition selectionIndicatorAnimation = new M3NodeTransition(selectionIndicator);

    /// Whether the built-in indicator currently participates in the centered content row.
    private boolean selectionIndicatorOccupiesContentSlot;

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
            (observable, oldValue, newValue) -> animateSelection(newValue);

    /// Refreshes the built-in indicator when its eligibility changes.
    private final InvalidationListener selectionIndicatorInvalidation = observable -> refreshSelectionIndicator();

    /// Transfers skin-managed replacement styling when the application graphic changes.
    private final ChangeListener<@Nullable Node> graphicListener = (observable, oldValue, newValue) -> {
        updateManagedGraphic(oldValue, newValue);
        animateSelectionIndicator(shouldDisplaySelectionIndicator());
        getSkinnable().requestLayout();
    };

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

        selectionIndicator.getStyleClass().add(SELECTION_INDICATOR_STYLE_CLASS);
        Region selectionIndicatorMark = new Region();
        selectionIndicatorMark.getStyleClass().add(SELECTION_INDICATOR_MARK_STYLE_CLASS);
        selectionIndicator.getChildren().setAll(selectionIndicatorMark);
        selectionIndicator.setManaged(false);
        selectionIndicator.setMouseTransparent(true);
        getChildren().add(selectionIndicator);
        selectionIndicatorAnimation.setOnFinished(event -> finishSelectionIndicatorAnimation());

        updateSelectionContainerImmediate(control.isSelected());
        updateSelectionIndicatorImmediate();
        updateManagedGraphic(null, control.getGraphic());
        control.selectedProperty().addListener(selectedListener);
        control.selectionIndicatorEnabledProperty().addListener(selectionIndicatorInvalidation);
        control.graphicProperty().addListener(graphicListener);
        control.getStyleClass().addListener(styleClassListener);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3SegmentedButton button = getSkinnable();
        selectionAnimation.stop();
        selectionIndicatorAnimation.stop();
        button.selectedProperty().removeListener(selectedListener);
        button.selectionIndicatorEnabledProperty().removeListener(selectionIndicatorInvalidation);
        button.graphicProperty().removeListener(graphicListener);
        button.getStyleClass().removeListener(styleClassListener);
        updateManagedGraphic(button.getGraphic(), null);
        super.dispose();
    }

    /// Adds the optional selected-state indicator slot to the minimum width without changing it on selection.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset)
                + reservedSelectionIndicatorWidth();
    }

    /// Adds the optional selected-state indicator slot to the preferred width without changing it on selection.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
                + reservedSelectionIndicatorWidth();
    }

    /// Lays out labeled content and the selected container.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        layoutSelectionContainer();
        layoutSelectionIndicator();
    }

    /// Lays out the built-in check and the inherited label as one centered logical content row.
    private void layoutSelectionIndicator() {
        M3SegmentedButton button = getSkinnable();
        double controlWidth = button.getWidth();
        double controlHeight = button.getHeight();
        if (controlWidth <= 0.0) {
            controlWidth = button.getLayoutBounds().getWidth();
        }
        if (controlHeight <= 0.0) {
            controlHeight = button.getLayoutBounds().getHeight();
        }

        double iconY = snapPositionY((controlHeight - INDICATOR_SIZE) / 2.0);
        @Nullable Text label = labelTextNode();
        if (label != null) {
            label.setTranslateX(0.0);
        }
        @Nullable Node graphic = button.getGraphic();
        if (selectionIndicatorOccupiesContentSlot && graphic != null) {
            Bounds graphicBounds = graphic.getBoundsInParent();
            double iconX = snapPositionX(graphicBounds.getCenterX() - INDICATOR_SIZE / 2.0);
            double graphicIconY = snapPositionY(graphicBounds.getCenterY() - INDICATOR_SIZE / 2.0);
            selectionIndicator.resizeRelocate(iconX, graphicIconY, INDICATOR_SIZE, INDICATOR_SIZE);
            return;
        }
        if (!selectionIndicatorOccupiesContentSlot || label == null || label.getText().isEmpty()) {
            double iconX = snapPositionX((controlWidth - INDICATOR_SIZE) / 2.0);
            selectionIndicator.resizeRelocate(iconX, iconY, INDICATOR_SIZE, INDICATOR_SIZE);
            return;
        }

        Bounds labelBounds = label.getBoundsInParent();
        double labelShift = (INDICATOR_SIZE + INDICATOR_GAP) / 2.0;
        label.setTranslateX(labelShift);
        double iconX = snapPositionX(labelBounds.getMinX() + labelShift - INDICATOR_GAP - INDICATOR_SIZE);
        selectionIndicator.resizeRelocate(iconX, iconY, INDICATOR_SIZE, INDICATOR_SIZE);
    }

    /// Returns the label text node installed by the labeled skin foundation.
    private @Nullable Text labelTextNode() {
        for (Node child : getChildren()) {
            if (child instanceof Text text) {
                return text;
            }
        }
        return null;
    }

    /// Returns the stable width reserved for a possible built-in selection indicator.
    private double reservedSelectionIndicatorWidth() {
        M3SegmentedButton button = getSkinnable();
        if (!button.isSelectionIndicatorEnabled() || button.getGraphic() != null) {
            return 0.0;
        }
        @Nullable String text = button.getText();
        return INDICATOR_SIZE + (text == null || text.isEmpty() ? 0.0 : INDICATOR_GAP);
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

    /// Animates all selected-state visuals and keeps content measurement stable.
    private void animateSelection(boolean selected) {
        animateSelectionContainer(selected);
        animateSelectionIndicator(shouldDisplaySelectionIndicator(selected));
        getSkinnable().requestLayout();
    }

    /// Animates the built-in check indicator to the requested visibility.
    private void animateSelectionIndicator(boolean visible) {
        double targetOpacity = visible ? 1.0 : 0.0;
        double targetScale = visible ? 1.0 : HIDDEN_INDICATOR_SCALE;
        selectionIndicatorAnimation.stop();
        if (visible) {
            selectionIndicatorOccupiesContentSlot = true;
        }
        selectionIndicatorAnimation.configure(
                M3Animation.defaultEffects(getSkinnable()),
                targetOpacity,
                targetScale,
                targetScale,
                selectionIndicator.getTranslateX(),
                selectionIndicator.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), selectionIndicatorAnimation);
    }

    /// Releases the indicator's content slot after its exit transition has completed.
    private void finishSelectionIndicatorAnimation() {
        if (!shouldDisplaySelectionIndicator()) {
            selectionIndicatorOccupiesContentSlot = false;
            getSkinnable().requestLayout();
        }
    }

    /// Refreshes indicator visibility after its configuration or application graphic changes.
    private void refreshSelectionIndicator() {
        M3SegmentedButton button = getSkinnable();
        updateManagedGraphic(button.getGraphic(), button.getGraphic());
        animateSelectionIndicator(shouldDisplaySelectionIndicator());
        button.requestLayout();
    }

    /// Transfers the class that lets CSS replace the application graphic without painting over it.
    private void updateManagedGraphic(@Nullable Node oldGraphic, @Nullable Node newGraphic) {
        if (oldGraphic != null) {
            oldGraphic.getStyleClass().remove(REPLACED_GRAPHIC_STYLE_CLASS);
        }
        if (newGraphic != null && getSkinnable().isSelectionIndicatorEnabled()) {
            if (!newGraphic.getStyleClass().contains(REPLACED_GRAPHIC_STYLE_CLASS)) {
                newGraphic.getStyleClass().add(REPLACED_GRAPHIC_STYLE_CLASS);
            }
        }
    }

    /// Updates the selected container without animation.
    private void updateSelectionContainerImmediate(boolean selected) {
        selectionContainer.setOpacity(selected ? 1.0 : 0.0);
        selectionContainer.setScaleX(selected ? 1.0 : HIDDEN_SELECTION_SCALE);
    }

    /// Updates the built-in selection indicator without animation.
    private void updateSelectionIndicatorImmediate() {
        boolean visible = shouldDisplaySelectionIndicator();
        selectionIndicatorOccupiesContentSlot = visible;
        selectionIndicator.setOpacity(visible ? 1.0 : 0.0);
        selectionIndicator.setScaleX(visible ? 1.0 : HIDDEN_INDICATOR_SCALE);
        selectionIndicator.setScaleY(visible ? 1.0 : HIDDEN_INDICATOR_SCALE);
    }

    /// Returns whether the built-in selected-state indicator should currently be displayed.
    private boolean shouldDisplaySelectionIndicator() {
        return shouldDisplaySelectionIndicator(getSkinnable().isSelected());
    }

    /// Returns whether the built-in selected-state indicator should be displayed for a selection state.
    private boolean shouldDisplaySelectionIndicator(boolean selected) {
        M3SegmentedButton button = getSkinnable();
        return selected && button.isSelectionIndicatorEnabled();
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
