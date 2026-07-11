// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Insets;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ListItem].
@NotNullByDefault
public class M3ListItemSkin extends SkinBase<M3ListItem> {
    /// The pseudo-class mirrored to internal nodes when a menu item uses the vibrant color style.
    private static final PseudoClass VIBRANT_PSEUDO_CLASS = PseudoClass.getPseudoClass("vibrant");

    /// The pseudo-class mirrored to internal text nodes when the item is selected.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The style class applied to menu item selected-container nodes.
    private static final String MENU_ITEM_SELECTION_CONTAINER_STYLE_CLASS = "m3-menu-item-selection-container";

    /// The style class applied to menu item text nodes.
    private static final String MENU_ITEM_TEXT_STYLE_CLASS = "m3-menu-item-text";

    /// The hidden selected container scale.
    private static final double HIDDEN_SELECTION_SCALE = 0.96;

    /// The selected container background layer.
    private final Region selectionContainer = new Region();

    /// The root container that receives background styling.
    private final HBox container = new HBox();

    /// The bounded state layer for list item feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The text column.
    private final VBox textBox = new VBox();

    /// The overline text label.
    private final Label overlineLabel = new Label();

    /// The headline text label.
    private final Label headlineLabel = new Label();

    /// The supporting text label.
    private final Label supportingLabel = new Label();

    /// The trailing supporting text and trailing node row.
    private final HBox trailingBox = new HBox();

    /// The trailing supporting text label.
    private final Label trailingSupportingLabel = new Label();

    /// The leading node slot.
    private final StackPane leadingSlot = new StackPane();

    /// The trailing node slot.
    private final StackPane trailingSlot = new StackPane();

    /// The clip used by fixed-size leading media slots.
    private final Rectangle leadingClip = new Rectangle();

    /// The clip used by fixed-size trailing media slots.
    private final Rectangle trailingClip = new Rectangle();

    /// The selected container appearance animation.
    private final M3NodeTransition selectionAnimation = new M3NodeTransition(selectionContainer);

    /// The background radius currently applied to the state container.
    private double containerRadius = Double.NaN;

    /// Handles primary mouse presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary mouse releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard activation.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Handles keyboard activation release.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Updates text nodes and metrics after text changes.
    private final InvalidationListener textInvalidation = observable -> updateTextAndMetrics();

    /// Updates optional node slots after slot content changes.
    private final InvalidationListener slotInvalidation = observable -> updateSlots();

    /// Updates optional node slot metrics after slot size changes.
    private final InvalidationListener slotMetricsInvalidation = observable -> updateSlotMetrics();

    /// Settles running selected-container transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), selectionAnimation)
            );

    /// Applies metric token changes to the list item layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();


    /// Mirrors menu-owned pseudo-classes to internal skin nodes.
    private final SetChangeListener<PseudoClass> skinnablePseudoClassListener =
            change -> updateMenuColorStylePseudoClasses();

    /// Animates the selected container and mirrors selected state to internal text nodes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectionContainer(newValue);

    /// Clears transient interaction feedback when the item becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Whether a primary mouse press currently owns the active ripple.
    private boolean mousePressed;

    /// Whether the space key currently owns the active ripple.
    private boolean spaceKeyPressed;

    /// Creates a list item skin.
    ///
    /// @param control the list item controlled by this skin
    public M3ListItemSkin(M3ListItem control) {
        super(control);
        selectionContainer.getStyleClass().add("m3-list-item-selection-container");
        container.getStyleClass().add("m3-list-item-container");
        textBox.getStyleClass().add("m3-list-item-text");
        overlineLabel.getStyleClass().add("m3-list-item-overline");
        headlineLabel.getStyleClass().add("m3-list-item-headline");
        supportingLabel.getStyleClass().add("m3-list-item-supporting");
        trailingBox.getStyleClass().add("m3-list-item-trailing-box");
        trailingSupportingLabel.getStyleClass().add("m3-list-item-trailing-supporting");
        leadingSlot.getStyleClass().add("m3-list-item-leading");
        trailingSlot.getStyleClass().add("m3-list-item-trailing");
        if (control instanceof M3MenuItem) {
            selectionContainer.getStyleClass().add(MENU_ITEM_SELECTION_CONTAINER_STYLE_CLASS);
            overlineLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            headlineLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            supportingLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            trailingSupportingLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
        }

        selectionContainer.setManaged(false);
        selectionContainer.setMouseTransparent(true);
        textBox.getChildren().addAll(overlineLabel, headlineLabel, supportingLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        trailingBox.getChildren().addAll(trailingSupportingLabel, trailingSlot);
        container.getChildren().addAll(leadingSlot, textBox, trailingBox);
        getChildren().setAll(selectionContainer, container, stateLayer);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        textBox.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        trailingBox.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        textBox.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        trailingBox.alignmentProperty().bind(M3NodeLayout.createLogicalEndCenterAlignmentBinding(control));

        stateLayer.installStateTransitions(control);
        updateSelectionContainerImmediate(control.isSelected());
        updateSelectedChildPseudoClasses(control.isSelected());
        updateMenuColorStylePseudoClasses();
        updateText();
        updateSlots();
        updateSlotMetrics();
        updateMetrics();
        installBehaviorHandlers(control);
        control.overlineTextProperty().addListener(textInvalidation);
        control.headlineTextProperty().addListener(textInvalidation);
        control.supportingTextProperty().addListener(textInvalidation);
        control.trailingSupportingTextProperty().addListener(textInvalidation);
        control.leadingProperty().addListener(slotInvalidation);
        control.trailingProperty().addListener(slotInvalidation);
        control.leadingSlotSizeProperty().addListener(slotMetricsInvalidation);
        control.trailingSlotSizeProperty().addListener(slotMetricsInvalidation);
        control.lineCountProperty().addListener(metricsInvalidation);
        control.oneLineHeightProperty().addListener(metricsInvalidation);
        control.twoLineHeightProperty().addListener(metricsInvalidation);
        control.threeLineHeightProperty().addListener(metricsInvalidation);
        control.containerShapeProperty().addListener(metricsInvalidation);
        control.horizontalPaddingProperty().addListener(metricsInvalidation);
        control.verticalPaddingProperty().addListener(metricsInvalidation);
        control.contentSpacingProperty().addListener(metricsInvalidation);
        control.getPseudoClassStates().addListener(skinnablePseudoClassListener);
        control.selectedProperty().addListener(selectedListener);
        control.disabledProperty().addListener(disabledListener);
    }

    /// Removes behavior handlers before the skin is disposed.
    @Override
    public void dispose() {
        M3ListItem item = getSkinnable();
        selectionAnimation.stop();
        resetInteractionState();
        stateLayer.uninstallStateTransitions();
        item.overlineTextProperty().removeListener(textInvalidation);
        item.headlineTextProperty().removeListener(textInvalidation);
        item.supportingTextProperty().removeListener(textInvalidation);
        item.trailingSupportingTextProperty().removeListener(textInvalidation);
        item.leadingProperty().removeListener(slotInvalidation);
        item.trailingProperty().removeListener(slotInvalidation);
        item.leadingSlotSizeProperty().removeListener(slotMetricsInvalidation);
        item.trailingSlotSizeProperty().removeListener(slotMetricsInvalidation);
        item.lineCountProperty().removeListener(metricsInvalidation);
        item.oneLineHeightProperty().removeListener(metricsInvalidation);
        item.twoLineHeightProperty().removeListener(metricsInvalidation);
        item.threeLineHeightProperty().removeListener(metricsInvalidation);
        item.containerShapeProperty().removeListener(metricsInvalidation);
        item.horizontalPaddingProperty().removeListener(metricsInvalidation);
        item.verticalPaddingProperty().removeListener(metricsInvalidation);
        item.contentSpacingProperty().removeListener(metricsInvalidation);
        item.getPseudoClassStates().removeListener(skinnablePseudoClassListener);
        motionSettingsObserver.dispose();
        item.selectedProperty().removeListener(selectedListener);
        item.disabledProperty().removeListener(disabledListener);
        item.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        container.nodeOrientationProperty().unbind();
        container.alignmentProperty().unbind();
        textBox.nodeOrientationProperty().unbind();
        textBox.alignmentProperty().unbind();
        trailingBox.nodeOrientationProperty().unbind();
        trailingBox.alignmentProperty().unbind();
        getChildren().removeAll(selectionContainer, container, stateLayer);
        super.dispose();
    }

    /// Lays out the container and bounded state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double shapeRadius = getSkinnable().getContainerShape();
        selectionContainer.resizeRelocate(x, y, width, height);
        container.resizeRelocate(x, y, width, height);
        updateContainerShape(width, height, shapeRadius);
        stateLayer.layoutLayer(x, y, width, height, shapeRadius);
    }

    /// Updates text and layout after text content changes.
    private void updateTextAndMetrics() {
        updateText();
        updateMetrics();
    }

    /// Updates label text and visibility.
    private void updateText() {
        M3ListItem item = getSkinnable();
        updateLabel(overlineLabel, item.getOverlineText());
        updateLabel(headlineLabel, item.getHeadlineText());
        updateLabel(supportingLabel, item.getSupportingText());
        updateLabel(trailingSupportingLabel, item.getTrailingSupportingText());
        updateTrailingBoxVisibility();
    }

    /// Updates one label from a string value.
    private static void updateLabel(Label label, String text) {
        boolean visible = !text.isBlank();
        label.setText(text);
        label.setVisible(visible);
        label.setManaged(visible);
    }

    /// Updates leading and trailing slot content.
    private void updateSlots() {
        M3ListItem item = getSkinnable();
        updateSlot(leadingSlot, item.getLeading());
        updateSlot(trailingSlot, item.getTrailing());
        updateTrailingBoxVisibility();
    }

    /// Updates a slot with an optional node.
    private static void updateSlot(StackPane slot, @Nullable Node node) {
        if (node == null) {
            slot.getChildren().clear();
            slot.setVisible(false);
            slot.setManaged(false);
            return;
        }
        slot.getChildren().setAll(node);
        slot.setVisible(true);
        slot.setManaged(true);
    }

    /// Updates fixed metrics and clipping for optional node slots.
    private void updateSlotMetrics() {
        M3ListItem item = getSkinnable();
        updateSlotMetrics(leadingSlot, leadingClip, item.getLeadingSlotSize());
        updateSlotMetrics(trailingSlot, trailingClip, item.getTrailingSlotSize());
        item.requestLayout();
    }

    /// Updates one optional node slot from its configured size role.
    private static void updateSlotMetrics(StackPane slot, Rectangle clip, M3ListItemSlotSize slotSize) {
        if (!isFixedSlotSize(slotSize)) {
            slot.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            slot.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            slot.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            slot.setClip(null);
            return;
        }

        double width = slotWidth(slotSize);
        double height = slotHeight(slotSize);
        double arc = slotShapeRadius(slotSize) * 2.0;
        slot.setMinSize(width, height);
        slot.setPrefSize(width, height);
        slot.setMaxSize(width, height);
        clip.setWidth(width);
        clip.setHeight(height);
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        slot.setClip(clip);
    }

    /// Returns whether the slot size uses fixed slot metrics.
    private static boolean isFixedSlotSize(M3ListItemSlotSize slotSize) {
        return slotSize != M3ListItemSlotSize.AUTO;
    }

    /// Returns the fixed slot width for a slot size role.
    private static double slotWidth(M3ListItemSlotSize slotSize) {
        return switch (slotSize) {
            case AUTO -> Region.USE_COMPUTED_SIZE;
            case ICON -> 24.0;
            case AVATAR -> 40.0;
            case THUMBNAIL -> 56.0;
            case WIDE_THUMBNAIL -> 64.0;
        };
    }

    /// Returns the fixed slot height for a slot size role.
    private static double slotHeight(M3ListItemSlotSize slotSize) {
        return switch (slotSize) {
            case AUTO -> Region.USE_COMPUTED_SIZE;
            case ICON -> 24.0;
            case AVATAR -> 40.0;
            case THUMBNAIL, WIDE_THUMBNAIL -> 56.0;
        };
    }

    /// Returns the clipping radius for a fixed slot size role.
    private static double slotShapeRadius(M3ListItemSlotSize slotSize) {
        return switch (slotSize) {
            case AUTO, ICON -> 0.0;
            case AVATAR -> 20.0;
            case THUMBNAIL, WIDE_THUMBNAIL -> 4.0;
        };
    }

    /// Updates the trailing group visibility from its text and node slots.
    private void updateTrailingBoxVisibility() {
        boolean visible = trailingSupportingLabel.isVisible() || trailingSlot.isVisible();
        trailingBox.setVisible(visible);
        trailingBox.setManaged(visible);
    }

    /// Applies token-driven layout metrics.
    private void updateMetrics() {
        M3ListItem item = getSkinnable();
        double height = preferredHeight(item);
        double horizontalPadding = item.getHorizontalPadding();
        double verticalPadding = item.getVerticalPadding();
        double spacing = item.getContentSpacing();
        container.setSpacing(spacing);
        container.setMinHeight(height);
        container.setPrefHeight(height);
        container.setMaxHeight(height);
        container.setPadding(new Insets(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding));
        getSkinnable().requestLayout();
    }


    /// Returns the preferred height for the current text structure.
    private static double preferredHeight(M3ListItem item) {
        return switch (item.getLineCount()) {
            case ONE_LINE -> item.getOneLineHeight();
            case TWO_LINE -> item.getTwoLineHeight();
            case THREE_LINE -> item.getThreeLineHeight();
        };
    }

    /// Updates the selected container shape using a radius that fits the allocated bounds.
    private void updateContainerShape(double width, double height, double shapeRadius) {
        double radius = resolvedShapeRadius(width, height, shapeRadius);
        if (Double.compare(containerRadius, radius) == 0) {
            return;
        }
        containerRadius = radius;
        String style = "-fx-background-radius: " + formatPixels(radius) + ";";
        selectionContainer.setStyle(style);
        container.setStyle(style);
        if (getSkinnable().getScene() == null) {
            return;
        }
        selectionContainer.applyCss();
        container.applyCss();
    }

    /// Mirrors the selected pseudo-class to internal nodes that need direct CSS state selectors.
    private void updateSelectedChildPseudoClasses(boolean selected) {
        overlineLabel.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
        headlineLabel.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
        supportingLabel.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
        trailingSupportingLabel.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
    }

    /// Mirrors menu color style pseudo-classes to direct internal nodes.
    private void updateMenuColorStylePseudoClasses() {
        boolean vibrant = getSkinnable() instanceof M3MenuItem
                && getSkinnable().getPseudoClassStates().contains(VIBRANT_PSEUDO_CLASS);
        selectionContainer.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, vibrant);
        overlineLabel.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, vibrant);
        headlineLabel.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, vibrant);
        supportingLabel.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, vibrant);
        trailingSupportingLabel.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, vibrant);
        stateLayer.setContentPseudoClass(VIBRANT_PSEUDO_CLASS, vibrant);
    }

    /// Animates the selected container to the requested state.
    private void animateSelectionContainer(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_SELECTION_SCALE;
        selectionAnimation.stop();
        selectionAnimation.setOnFinished(null);
        if (!selected) {
            updateSelectedChildPseudoClasses(false);
        }
        M3MotionSpec spec = M3Animation.defaultEffects(getSkinnable());
        selectionAnimation.configure(
                spec,
                targetOpacity,
                targetScale,
                targetScale,
                selectionContainer.getTranslateX(),
                selectionContainer.getTranslateY()
        );
        selectionAnimation.setOnFinished(event -> {
            if (getSkinnable().isSelected() == selected) {
                updateSelectedChildPseudoClasses(selected);
            }
        });
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Updates the selected container without animation.
    private void updateSelectionContainerImmediate(boolean selected) {
        selectionContainer.setOpacity(selected ? 1.0 : 0.0);
        selectionContainer.setScaleX(selected ? 1.0 : HIDDEN_SELECTION_SCALE);
        selectionContainer.setScaleY(selected ? 1.0 : HIDDEN_SELECTION_SCALE);
    }

    /// Resolves a shape token to a radius that can be represented within the current bounds.
    private static double resolvedShapeRadius(double width, double height, double shapeRadius) {
        double maximumRadius = Math.max(0.0, height / 2.0);
        if (width > 0.0) {
            maximumRadius = Math.min(maximumRadius, width / 2.0);
        }
        return Math.min(Math.max(0.0, shapeRadius), maximumRadius);
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }

    /// Installs behavior handlers for pointer and keyboard activation.
    private void installBehaviorHandlers(M3ListItem item) {
        item.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Starts list item feedback on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        M3ListItem item = getSkinnable();
        if (item.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        M3FocusRequests.requestFocusIfTraversable(item);
        stateLayer.playRipple(event.getX(), event.getY());
        event.consume();
    }

    /// Releases list item feedback and fires when the primary mouse is released inside the item.
    private void handleMouseReleased(MouseEvent event) {
        M3ListItem item = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = !item.isDisabled() && item.contains(event.getX(), event.getY());
        mousePressed = false;
        stateLayer.releaseRipple();
        if (shouldFire) {
            item.fire();
        }
        event.consume();
    }

    /// Fires the list item on enter or starts activation feedback for space.
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (getSkinnable().isDisabled()) {
            return;
        }
        if ((code == KeyCode.ENTER || code == KeyCode.SPACE)
                && M3FocusGuards.focusOwnerInsideTextInput(getSkinnable())) {
            return;
        }

        if (code == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                stateLayer.playCenteredRipple();
            }
            event.consume();
        } else if (code == KeyCode.ENTER) {
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            getSkinnable().fire();
            event.consume();
        }
    }

    /// Releases space-key feedback and fires the list item.
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = !getSkinnable().isDisabled();
        spaceKeyPressed = false;
        stateLayer.releaseRipple();
        if (shouldFire) {
            getSkinnable().fire();
        }
        event.consume();
    }

    /// Clears transient pointer and keyboard feedback.
    private void resetInteractionState() {
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.reset();
    }

}
