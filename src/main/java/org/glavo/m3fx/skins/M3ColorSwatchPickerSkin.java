// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorSwatch;
import org.glavo.m3fx.controls.M3ColorSwatchPicker;
import org.glavo.m3fx.controls.M3ColorSwatchRounding;
import org.glavo.m3fx.internal.M3ScrollReveal;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/// The default wrapping, single-selection skin for [M3ColorSwatchPicker].
///
/// Each item is represented by one retained focusable cell containing a passive [M3ColorSwatch]. Cells expose
/// radio-button accessibility semantics while the skinnable control remains the list owner. Pointer and keyboard
/// activation applies the picker's user-selection policy. Arrow keys move focus without changing selection.
@NotNullByDefault
public final class M3ColorSwatchPickerSkin extends SkinBase<M3ColorSwatchPicker> {
    /// The pseudo-class used by the selected swatch cell.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The pseudo-class used while a keyboard activation key owns pressed feedback.
    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    /// The fallback padding used before cell CSS has been resolved.
    private static final double FALLBACK_CELL_PADDING = 4.0;

    /// The baseline corner radius of a default-rounded swatch.
    private static final double DEFAULT_SWATCH_RADIUS = 12.0;

    /// The wrapping visual container.
    private final FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);

    /// A single invisible CSS probe used to resolve the current on-surface state-layer paint.
    private final Region statePaintProbe = new Region();

    /// The retained cell list in item order.
    private final ArrayList<SwatchCell> cells = new ArrayList<>();

    /// Updates retained cells when the live picker item list changes.
    private final ListChangeListener<M3Color> itemsListener = this::handleItemsChanged;

    /// Updates only the cells affected by a selected-index change.
    private final ChangeListener<Number> selectedIndexListener =
            (observable, oldValue, newValue) -> updateSelectedIndex(
                    oldValue.intValue(),
                    newValue.intValue()
            );

    /// Updates all retained swatches when the picker swatch size changes.
    private final InvalidationListener swatchSizeInvalidation = observable -> {
        for (SwatchCell cell : cells) {
            cell.updateSwatchSize();
        }
        updateLayoutMetrics();
    };

    /// Updates all retained swatches and state-layer geometry when corner treatment changes.
    private final InvalidationListener roundingInvalidation = observable -> {
        for (SwatchCell cell : cells) {
            cell.updateSwatchRounding();
        }
        getSkinnable().requestLayout();
    };

    /// Updates the FlowPane horizontal gap.
    private final InvalidationListener horizontalGapInvalidation = observable -> {
        flowPane.setHgap(getSkinnable().getHorizontalGap());
        updatePreferredWrapLength();
        getSkinnable().requestLayout();
    };

    /// Updates the FlowPane vertical gap.
    private final InvalidationListener verticalGapInvalidation = observable -> {
        flowPane.setVgap(getSkinnable().getVerticalGap());
        getSkinnable().requestLayout();
    };

    /// Updates preferred wrapping after a column-count change.
    private final InvalidationListener columnCountInvalidation = observable -> {
        updatePreferredWrapLength();
        getSkinnable().requestLayout();
    };

    /// The concrete state-layer paint most recently resolved from CSS.
    private @Nullable Paint stateLayerPaint;

    /// The sole cell currently exposed as a focus-traversal stop.
    private int focusIndex = -1;

    /// Creates a color-swatch-picker skin.
    ///
    /// @param control the picker controlled by this skin
    public M3ColorSwatchPickerSkin(M3ColorSwatchPicker control) {
        super(control);

        flowPane.setManaged(false);
        flowPane.setAlignment(Pos.TOP_LEFT);
        flowPane.setHgap(control.getHorizontalGap());
        flowPane.setVgap(control.getVerticalGap());

        statePaintProbe.setManaged(false);
        statePaintProbe.setMouseTransparent(true);
        statePaintProbe.setOpacity(0.0);
        statePaintProbe.setStyle("-fx-background-color: -m3-color-on-surface;");

        getChildren().setAll(flowPane, statePaintProbe);

        control.getItems().addListener(itemsListener);
        control.selectedIndexProperty().addListener(selectedIndexListener);
        control.swatchSizeProperty().addListener(swatchSizeInvalidation);
        control.roundingProperty().addListener(roundingInvalidation);
        control.horizontalGapProperty().addListener(horizontalGapInvalidation);
        control.verticalGapProperty().addListener(verticalGapInvalidation);
        control.columnCountProperty().addListener(columnCountInvalidation);

        rebuildCells();
    }

    /// Removes listeners, interaction state, and retained cell references before disposal.
    @Override
    public void dispose() {
        M3ColorSwatchPicker control = getSkinnable();
        control.getItems().removeListener(itemsListener);
        control.selectedIndexProperty().removeListener(selectedIndexListener);
        control.swatchSizeProperty().removeListener(swatchSizeInvalidation);
        control.roundingProperty().removeListener(roundingInvalidation);
        control.horizontalGapProperty().removeListener(horizontalGapInvalidation);
        control.verticalGapProperty().removeListener(verticalGapInvalidation);
        control.columnCountProperty().removeListener(columnCountInvalidation);

        disposeCells();
        flowPane.getChildren().clear();
        getChildren().removeAll(flowPane, statePaintProbe);
        super.dispose();
    }

    /// Computes the minimum width needed by one swatch cell.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + preferredCellWidth() + rightInset;
    }

    /// Computes the minimum height needed by one swatch cell.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + preferredCellHeight() + bottomInset;
    }

    /// Computes the preferred width from the configured column count and retained cells.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        int columns = preferredColumnCount();
        double contentWidth = columns == 0
                ? 0.0
                : columns * preferredCellWidth() + (columns - 1) * getSkinnable().getHorizontalGap();
        return leftInset + contentWidth + rightInset;
    }

    /// Computes the preferred height for the number of rows implied by the available width.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        int columns = columnsForWidth(width, leftInset, rightInset);
        int rows = columns == 0 ? 0 : (cells.size() + columns - 1) / columns;
        double contentHeight = rows == 0
                ? 0.0
                : rows * preferredCellHeight() + (rows - 1) * getSkinnable().getVerticalGap();
        return topInset + contentHeight + bottomInset;
    }

    /// Lays out the wrapping pane and refreshes the retained state-layer paint after CSS resolution.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        flowPane.resizeRelocate(x, y, Math.max(0.0, width), Math.max(0.0, height));
        refreshStateLayerPaint();
    }

    /// Applies incremental item-list changes to retained cells.
    private void handleItemsChanged(ListChangeListener.Change<? extends M3Color> change) {
        int firstChangedIndex = Integer.MAX_VALUE;
        while (change.next()) {
            if (change.wasPermutated()) {
                rebuildCells();
                return;
            }

            if (change.wasUpdated()) {
                for (int index = change.getFrom(); index < change.getTo(); index++) {
                    cells.get(index).updateColor(getSkinnable().getItems().get(index));
                }
                firstChangedIndex = Math.min(firstChangedIndex, change.getFrom());
                continue;
            }

            int from = change.getFrom();
            for (int offset = 0; offset < change.getRemovedSize(); offset++) {
                SwatchCell removed = cells.remove(from);
                flowPane.getChildren().remove(removed);
                removed.dispose();
            }
            for (int offset = 0; offset < change.getAddedSize(); offset++) {
                int index = from + offset;
                SwatchCell added = createCell(index, getSkinnable().getItems().get(index));
                cells.add(index, added);
                flowPane.getChildren().add(index, added);
            }
            firstChangedIndex = Math.min(firstChangedIndex, from);
        }

        if (firstChangedIndex != Integer.MAX_VALUE) {
            updateCellIndices(firstChangedIndex);
            updateAllSelectedStates();
            updateRovingFocusTarget(preferredRovingIndex());
            updateLayoutMetrics();
        }
    }

    /// Recreates cells from the current item list after a wholesale or permuted change.
    private void rebuildCells() {
        disposeCells();
        flowPane.getChildren().clear();

        M3ColorSwatchPicker control = getSkinnable();
        for (int index = 0; index < control.getItems().size(); index++) {
            SwatchCell cell = createCell(index, control.getItems().get(index));
            cells.add(cell);
            flowPane.getChildren().add(cell);
        }
        updateRovingFocusTarget(preferredRovingIndex());
        updateLayoutMetrics();
    }

    /// Creates one retained cell for an item.
    private SwatchCell createCell(int index, M3Color color) {
        SwatchCell cell = new SwatchCell(index, color);
        Paint paint = stateLayerPaint;
        if (paint != null) {
            cell.setStateLayerPaint(paint);
        }
        return cell;
    }

    /// Disposes every retained cell and clears the cell list.
    private void disposeCells() {
        for (SwatchCell cell : cells) {
            cell.dispose();
        }
        cells.clear();
    }

    /// Updates cell indices after structural item-list changes.
    private void updateCellIndices(int fromIndex) {
        for (int index = Math.max(0, fromIndex); index < cells.size(); index++) {
            cells.get(index).updateIndex(index);
        }
    }

    /// Updates the selected pseudo-class on the old and new selected cells.
    private void updateSelectedIndex(int oldIndex, int newIndex) {
        if (oldIndex >= 0 && oldIndex < cells.size()) {
            cells.get(oldIndex).updateSelected(false);
        }
        if (newIndex >= 0 && newIndex < cells.size()) {
            cells.get(newIndex).updateSelected(true);
            updateRovingFocusTarget(newIndex);
        }
    }

    /// Synchronizes every cell with the picker's current selected index.
    private void updateAllSelectedStates() {
        int selectedIndex = getSkinnable().getSelectedIndex();
        for (int index = 0; index < cells.size(); index++) {
            cells.get(index).updateSelected(index == selectedIndex);
        }
    }

    /// Updates gaps and preferred wrapping after cell geometry changes.
    private void updateLayoutMetrics() {
        flowPane.setHgap(getSkinnable().getHorizontalGap());
        flowPane.setVgap(getSkinnable().getVerticalGap());
        updatePreferredWrapLength();
        getSkinnable().requestLayout();
    }

    /// Updates the preferred FlowPane line length from the configured column count.
    private void updatePreferredWrapLength() {
        int columns = preferredColumnCount();
        double preferredWrapLength = columns == 0
                ? 0.0
                : columns * preferredCellWidth() + (columns - 1) * getSkinnable().getHorizontalGap();
        flowPane.setPrefWrapLength(preferredWrapLength);
    }

    /// Returns the number of columns represented by the preferred size.
    private int preferredColumnCount() {
        return Math.min(getSkinnable().getColumnCount(), cells.size());
    }

    /// Returns the number of columns that fit within a width.
    private int columnsForWidth(double width, double leftInset, double rightInset) {
        if (cells.isEmpty()) {
            return 0;
        }
        if (width < 0.0) {
            return preferredColumnCount();
        }

        double availableWidth = Math.max(0.0, width - leftInset - rightInset);
        double cellWidth = preferredCellWidth();
        double gap = getSkinnable().getHorizontalGap();
        int columns = (int) Math.floor((availableWidth + gap) / (cellWidth + gap));
        return Math.max(1, Math.min(cells.size(), columns));
    }

    /// Returns the preferred width of one cell, including its CSS padding.
    private double preferredCellWidth() {
        if (!cells.isEmpty()) {
            double width = cells.get(0).prefWidth(-1.0);
            if (Double.isFinite(width) && width > 0.0) {
                return width;
            }
        }
        return getSkinnable().getSwatchSize().getSize() + FALLBACK_CELL_PADDING * 2.0;
    }

    /// Returns the preferred height of one cell, including its CSS padding.
    private double preferredCellHeight() {
        if (!cells.isEmpty()) {
            double height = cells.get(0).prefHeight(-1.0);
            if (Double.isFinite(height) && height > 0.0) {
                return height;
            }
        }
        return getSkinnable().getSwatchSize().getSize() + FALLBACK_CELL_PADDING * 2.0;
    }

    /// Resolves the inherited on-surface paint and applies it to all retained state layers.
    private void refreshStateLayerPaint() {
        Background background = statePaintProbe.getBackground();
        if (background == null || background.getFills().isEmpty()) {
            return;
        }

        Paint paint = background.getFills().get(0).getFill();
        if (paint.equals(stateLayerPaint)) {
            return;
        }

        stateLayerPaint = paint;
        for (SwatchCell cell : cells) {
            cell.setStateLayerPaint(paint);
        }
    }

    /// Returns the best focus-traversal entry after a structural item change.
    private int preferredRovingIndex() {
        if (cells.isEmpty()) {
            return -1;
        }
        for (int index = 0; index < cells.size(); index++) {
            if (cells.get(index).isFocused()) {
                return index;
            }
        }
        int selectedIndex = getSkinnable().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < cells.size()) {
            return selectedIndex;
        }
        return Math.max(0, Math.min(focusIndex, cells.size() - 1));
    }

    /// Makes one cell the sole focus-traversal stop.
    private void updateRovingFocusTarget(int index) {
        int normalizedIndex = cells.isEmpty()
                ? -1
                : Math.max(0, Math.min(index, cells.size() - 1));
        focusIndex = normalizedIndex;
        for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
            cells.get(cellIndex).setFocusTraversable(cellIndex == normalizedIndex);
        }
    }

    /// Moves focus in response to a navigation key.
    private boolean navigateFrom(int currentIndex, KeyCode code) {
        if (cells.isEmpty()) {
            return false;
        }

        boolean rightToLeft =
                getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        int targetIndex = switch (code) {
            case LEFT -> currentIndex + (rightToLeft ? 1 : -1);
            case RIGHT -> currentIndex + (rightToLeft ? -1 : 1);
            case UP -> currentIndex - navigationColumnCount();
            case DOWN -> currentIndex + navigationColumnCount();
            case HOME -> 0;
            case END -> cells.size() - 1;
            default -> -1;
        };
        if (targetIndex < 0 || targetIndex >= cells.size() || targetIndex == currentIndex) {
            return false;
        }

        updateRovingFocusTarget(targetIndex);
        M3ScrollReveal.requestFocusAndReveal(getSkinnable(), cells.get(targetIndex));
        return true;
    }

    /// Applies the user-activation empty-selection policy to one cell.
    ///
    /// @param index the activated cell index
    private void activate(int index) {
        M3ColorSwatchPicker control = getSkinnable();
        if (control.isAllowEmptySelection() && index == control.getSelectedIndex()) {
            control.clearSelection();
        } else {
            control.select(index);
        }
    }

    /// Returns the number of columns in the currently rendered visual grid.
    private int navigationColumnCount() {
        double width = flowPane.getWidth();
        return width > 0.0
                ? columnsForWidth(width, 0.0, 0.0)
                : Math.max(1, preferredColumnCount());
    }

    /// A focusable visual and accessible item for one immutable color.
    @NotNullByDefault
    private final class SwatchCell extends StackPane {
        /// The passive color preview rendered by this cell.
        private final M3ColorSwatch swatch = new M3ColorSwatch();

        /// The retained hover, focus, pressed, and ripple layer.
        private final M3StateLayer stateLayer = new M3StateLayer();

        /// The current item index.
        private int index;

        /// Handles primary pointer presses.
        private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

        /// Handles primary pointer releases.
        private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

        /// Handles keyboard activation and navigation presses.
        private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

        /// Handles keyboard activation releases.
        private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

        /// Clears keyboard interaction when focus leaves this cell.
        private final ChangeListener<Boolean> focusedListener = (observable, oldValue, focused) -> {
            if (focused) {
                updateRovingFocusTarget(index);
            } else {
                cancelKeyboardInteraction();
            }
        };

        /// Clears transient interaction when this cell becomes effectively disabled.
        private final InvalidationListener disabledInvalidation = observable -> {
            if (isDisabled()) {
                resetInteractionState();
            }
        };

        /// Whether a primary pointer gesture currently owns ripple feedback.
        private boolean pointerPressed;

        /// Whether an activation key currently owns centered ripple feedback.
        private boolean keyboardPressed;

        /// Creates a retained cell.
        ///
        /// @param index the current item index
        /// @param color the immutable color represented by the cell
        private SwatchCell(int index, M3Color color) {
            this.index = index;

            getStyleClass().add("color-swatch-cell");
            setAccessibleRole(AccessibleRole.RADIO_BUTTON);
            setFocusTraversable(false);
            setAlignment(Pos.CENTER);
            setPickOnBounds(true);

            swatch.setColor(color);
            swatch.setSize(getSkinnable().getSwatchSize());
            swatch.setRounding(getSkinnable().getRounding());
            swatch.setMouseTransparent(true);
            accessibleTextProperty().bind(swatch.accessibleTextProperty());

            stateLayer.installStateTransitions(this);
            getChildren().addAll(swatch, stateLayer);

            addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            focusedProperty().addListener(focusedListener);
            disabledProperty().addListener(disabledInvalidation);

            updateSelected(index == getSkinnable().getSelectedIndex());
        }

        /// Removes interaction listeners and retained child references.
        private void dispose() {
            removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            focusedProperty().removeListener(focusedListener);
            disabledProperty().removeListener(disabledInvalidation);

            resetInteractionState();
            stateLayer.uninstallStateTransitions();
            accessibleTextProperty().unbind();
            getChildren().clear();
        }

        /// Updates the item index after a structural list change.
        private void updateIndex(int index) {
            this.index = index;
        }

        /// Updates the immutable color represented by this cell.
        private void updateColor(M3Color color) {
            swatch.setColor(color);
        }

        /// Updates the passive preview to the current picker swatch size.
        private void updateSwatchSize() {
            swatch.setSize(getSkinnable().getSwatchSize());
            requestLayout();
        }

        /// Updates the passive preview to the current picker corner treatment.
        private void updateSwatchRounding() {
            swatch.setRounding(getSkinnable().getRounding());
            requestLayout();
        }

        /// Updates visual and accessible selection state.
        private void updateSelected(boolean selected) {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }

        /// Applies the concrete interaction paint resolved by the picker skin.
        private void setStateLayerPaint(Paint paint) {
            stateLayer.setContentPaint(paint);
        }

        /// Starts bounded pointer feedback and moves focus to this cell.
        private void handleMousePressed(MouseEvent event) {
            if (isDisabled() || event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            pointerPressed = true;
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, true);
            updateRovingFocusTarget(index);
            M3ScrollReveal.requestFocusAndReveal(getSkinnable(), this);
            stateLayer.playRipple(event.getX(), event.getY());
            event.consume();
        }

        /// Selects this cell when a primary pointer press is released inside its bounds.
        private void handleMouseReleased(MouseEvent event) {
            if (!pointerPressed || event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            boolean shouldSelect = !isDisabled() && contains(event.getX(), event.getY());
            pointerPressed = false;
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, keyboardPressed);
            if (!keyboardPressed) {
                stateLayer.releaseRipple();
            }
            if (shouldSelect) {
                activate(index);
            }
            event.consume();
        }

        /// Handles navigation and starts centered feedback for Space or Enter.
        private void handleKeyPressed(KeyEvent event) {
            if (isDisabled()) {
                return;
            }

            KeyCode code = event.getCode();
            if (code == KeyCode.SPACE || code == KeyCode.ENTER) {
                if (!keyboardPressed) {
                    keyboardPressed = true;
                    pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, true);
                    stateLayer.playCenteredRipple();
                }
                event.consume();
                return;
            }

            if (event.isAltDown() || event.isControlDown() || event.isMetaDown()) {
                return;
            }
            if (navigateFrom(index, code)) {
                event.consume();
            }
        }

        /// Selects this cell and releases centered feedback after Space or Enter.
        private void handleKeyReleased(KeyEvent event) {
            KeyCode code = event.getCode();
            if (!keyboardPressed || code != KeyCode.SPACE && code != KeyCode.ENTER) {
                return;
            }

            keyboardPressed = false;
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, pointerPressed);
            if (!pointerPressed) {
                stateLayer.releaseRipple();
            }
            if (!isDisabled()) {
                activate(index);
            }
            event.consume();
        }

        /// Cancels unfinished keyboard feedback without disturbing an active pointer press.
        private void cancelKeyboardInteraction() {
            if (!keyboardPressed) {
                return;
            }

            keyboardPressed = false;
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, pointerPressed);
            if (!pointerPressed) {
                stateLayer.releaseRipple();
            }
        }

        /// Clears all transient interaction feedback.
        private void resetInteractionState() {
            pointerPressed = false;
            keyboardPressed = false;
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, false);
            stateLayer.cancelRipple();
        }

        /// Lays out the state layer over the padded cell bounds.
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            double width = getWidth();
            double height = getHeight();
            stateLayer.layoutLayer(0.0, 0.0, width, height, resolvedCellRadius(width, height));
        }

        /// Returns the state-layer radius corresponding to the picker corner treatment.
        ///
        /// @param width  the cell width
        /// @param height the cell height
        /// @return the non-negative corner radius
        private double resolvedCellRadius(double width, double height) {
            double maximum = Math.max(0.0, Math.min(width, height) / 2.0);
            Background background = getBackground();
            if (background != null && !background.getFills().isEmpty()) {
                CornerRadii radii = background.getFills().get(0).getRadii();
                double radius = radii.getTopLeftHorizontalRadius();
                if (Double.isFinite(radius) && radius >= 0.0) {
                    return Math.min(maximum, radius);
                }
            }
            M3ColorSwatchRounding rounding = getSkinnable().getRounding();
            return switch (rounding) {
                case NONE -> Math.min(maximum, FALLBACK_CELL_PADDING);
                case DEFAULT -> Math.min(maximum, DEFAULT_SWATCH_RADIUS + FALLBACK_CELL_PADDING);
                case FULL -> maximum;
            };
        }

        /// Returns cell selection state to accessibility clients.
        @Override
        public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            if (attribute == AccessibleAttribute.SELECTED) {
                return index == getSkinnable().getSelectedIndex();
            }
            return super.queryAccessibleAttribute(attribute, parameters);
        }

        /// Executes accessible selection through the same picker operation used by pointer and keyboard input.
        @Override
        public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
            if (action == AccessibleAction.FIRE) {
                if (!isDisabled()) {
                    activate(index);
                }
                return;
            }
            super.executeAccessibleAction(action, parameters);
        }
    }
}
