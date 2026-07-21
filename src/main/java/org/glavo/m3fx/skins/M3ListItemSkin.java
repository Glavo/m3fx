// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ListItemBase;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SubMenuItem;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ListItemBase].
///
/// The skin lays out the item's text and optional leading and trailing slots, presents selected and interaction
/// feedback, and implements pointer and keyboard activation. Menu-item subclasses additionally receive grouped
/// corner treatment and persistent active feedback while a submenu is showing.
@NotNullByDefault
public class M3ListItemSkin extends SkinBase<M3ListItemBase> {
    /// The pseudo-class applied to the first item in a visible menu group.
    private static final PseudoClass FIRST_MENU_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("first-menu-item");

    /// The pseudo-class applied to the last item in a visible menu group.
    private static final PseudoClass LAST_MENU_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("last-menu-item");

    /// The pseudo-class applied while a submenu owner keeps its submenu open.
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

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

    /// Fallback state opacity tokens used without an installed theme.
    private static final M3StateLayerTokens FALLBACK_STATE_LAYER_TOKENS = M3StateLayerTokens.baseline();

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

    /// The reusable clip that shapes the selected container without per-pulse background allocation.
    private final @Nullable RoundedRectangleClip selectionContainerClip;

    /// The reusable clip that shapes the base container without per-pulse background allocation.
    private final @Nullable RoundedRectangleClip baseContainerClip;

    /// The reusable transition for expressive menu-item corner morphing.
    private final @Nullable ContainerShapeTransition containerShapeAnimation;

    /// The selected state targeted by the currently configured selection animation.
    private boolean selectionAnimationTargetSelected;

    /// The top-left background radius currently applied to the state container.
    private double containerTopLeftRadius = Double.NaN;

    /// The top-right background radius currently applied to the state container.
    private double containerTopRightRadius = Double.NaN;

    /// The bottom-right background radius currently applied to the state container.
    private double containerBottomRightRadius = Double.NaN;

    /// The bottom-left background radius currently applied to the state container.
    private double containerBottomLeftRadius = Double.NaN;

    /// The target top-left radius for the current shape transition.
    private double targetContainerTopLeftRadius = Double.NaN;

    /// The target top-right radius for the current shape transition.
    private double targetContainerTopRightRadius = Double.NaN;

    /// The target bottom-right radius for the current shape transition.
    private double targetContainerBottomRightRadius = Double.NaN;

    /// The target bottom-left radius for the current shape transition.
    private double targetContainerBottomLeftRadius = Double.NaN;

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

    /// Applies metric token changes to the list item layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();


    /// Mirrors menu-owned pseudo-classes to internal skin nodes.
    private final SetChangeListener<PseudoClass> skinnablePseudoClassListener = change -> {
        updateMenuColorStylePseudoClasses();
        @Nullable PseudoClass changed = change.wasAdded() ? change.getElementAdded() : change.getElementRemoved();
        if (changed == FIRST_MENU_ITEM_PSEUDO_CLASS
                || changed == LAST_MENU_ITEM_PSEUDO_CLASS
                || changed == ACTIVE_PSEUDO_CLASS) {
            getSkinnable().requestLayout();
        }
    };

    /// Animates the selected container and mirrors selected state to internal text nodes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectionContainer(newValue);

    /// Clears transient interaction feedback when the item becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Cancels keyboard ownership when focus moves away before Space is released.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            cancelKeyboardInteraction();
        }
    };

    /// Clears gesture ownership when the item leaves its scene before release.
    private final InvalidationListener sceneInvalidation = observable -> {
        if (getSkinnable().getScene() == null) {
            resetInteractionState();
        }
    };

    /// Updates persistent active feedback while a submenu remains open.
    private final ChangeListener<Boolean> subMenuShowingListener =
            (observable, oldValue, newValue) -> updateSubMenuActiveState();

    /// Whether a primary mouse press currently owns the active ripple.
    private boolean mousePressed;

    /// Whether the space key currently owns the active ripple.
    private boolean spaceKeyPressed;

    /// Creates a list item skin.
    ///
    /// @param control the list item controlled by this skin
    public M3ListItemSkin(M3ListItemBase control) {
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
            selectionContainerClip = new RoundedRectangleClip();
            baseContainerClip = new RoundedRectangleClip();
            containerShapeAnimation = new ContainerShapeTransition();
            selectionContainer.getStyleClass().add(MENU_ITEM_SELECTION_CONTAINER_STYLE_CLASS);
            overlineLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            headlineLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            supportingLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            trailingSupportingLabel.getStyleClass().add(MENU_ITEM_TEXT_STYLE_CLASS);
            selectionContainer.setClip(selectionContainerClip.path());
            container.setClip(baseContainerClip.path());
        } else {
            selectionContainerClip = null;
            baseContainerClip = null;
            containerShapeAnimation = null;
        }

        selectionContainer.setManaged(false);
        selectionContainer.setMouseTransparent(true);
        selectionAnimation.setOnFinished(event -> finishSelectionAnimation());
        textBox.getChildren().addAll(overlineLabel, headlineLabel, supportingLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        trailingBox.getChildren().addAll(trailingSupportingLabel, trailingSlot);
        container.getChildren().addAll(leadingSlot, textBox, trailingBox);
        getChildren().setAll(selectionContainer, container, stateLayer);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        textBox.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        trailingBox.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        // JavaFX mirrors layout coordinates for RTL subtrees. Keep alignments in local LTR coordinates so logical
        // start and end are mirrored exactly once with the rest of the item.
        container.setAlignment(Pos.CENTER_LEFT);
        textBox.setAlignment(Pos.CENTER_LEFT);
        trailingBox.setAlignment(Pos.CENTER_RIGHT);

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
        control.focusedProperty().addListener(focusedListener);
        control.sceneProperty().addListener(sceneInvalidation);
        if (control instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.subMenuShowingProperty().addListener(subMenuShowingListener);
            updateSubMenuActiveState();
        }
    }

    /// Removes behavior handlers before the skin is disposed.
    @Override
    public void dispose() {
        M3ListItemBase item = getSkinnable();
        selectionAnimation.stop();
        @Nullable ContainerShapeTransition shapeAnimation = containerShapeAnimation;
        if (shapeAnimation != null) {
            shapeAnimation.stop();
        }
        selectionAnimation.setOnFinished(null);
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
        item.selectedProperty().removeListener(selectedListener);
        item.disabledProperty().removeListener(disabledListener);
        item.focusedProperty().removeListener(focusedListener);
        item.sceneProperty().removeListener(sceneInvalidation);
        if (item instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.subMenuShowingProperty().removeListener(subMenuShowingListener);
        }
        item.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        container.nodeOrientationProperty().unbind();
        textBox.nodeOrientationProperty().unbind();
        trailingBox.nodeOrientationProperty().unbind();
        selectionContainer.setClip(null);
        container.setClip(null);
        getChildren().removeAll(selectionContainer, container, stateLayer);
        super.dispose();
    }

    /// Computes a flexible minimum width so list rows can shrink inside their owning container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + rightInset;
    }

    /// Allows list rows with default constraints to fill the available container width.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return Double.MAX_VALUE;
    }

    /// Lays out the container and bounded state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ListItemBase item = getSkinnable();
        double outerRadius = resolvedShapeRadius(width, height, item.getContainerShape());
        selectionContainer.resizeRelocate(x, y, width, height);
        container.resizeRelocate(x, y, width, height);
        if (!(item instanceof M3MenuItem menuItem)) {
            updateStandardContainerShape(outerRadius);
            stateLayer.layoutLayer(x, y, width, height, outerRadius);
            return;
        }

        double topLeftRadius = outerRadius;
        double topRightRadius = outerRadius;
        double bottomRightRadius = outerRadius;
        double bottomLeftRadius = outerRadius;

        if (!item.isSelected()
                && !item.getPseudoClassStates().contains(ACTIVE_PSEUDO_CLASS)) {
            boolean first = item.getPseudoClassStates().contains(FIRST_MENU_ITEM_PSEUDO_CLASS);
            boolean last = item.getPseudoClassStates().contains(LAST_MENU_ITEM_PSEUDO_CLASS);
            if (first != last) {
                double innerRadius = resolvedShapeRadius(width, height, menuItem.getInnerCornerShape());
                if (first) {
                    bottomRightRadius = innerRadius;
                    bottomLeftRadius = innerRadius;
                } else {
                    topLeftRadius = innerRadius;
                    topRightRadius = innerRadius;
                }
            }
        }

        updateContainerShapeTarget(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        @Nullable RoundedRectangleClip selectedClip = selectionContainerClip;
        @Nullable RoundedRectangleClip baseClip = baseContainerClip;
        if (selectedClip != null && baseClip != null) {
            selectedClip.update(
                    width,
                    height,
                    containerTopLeftRadius,
                    containerTopRightRadius,
                    containerBottomRightRadius,
                    containerBottomLeftRadius
            );
            baseClip.update(
                    width,
                    height,
                    containerTopLeftRadius,
                    containerTopRightRadius,
                    containerBottomRightRadius,
                    containerBottomLeftRadius
            );
        }
        stateLayer.layoutLayer(
                x,
                y,
                width,
                height,
                containerTopLeftRadius,
                containerTopRightRadius,
                containerBottomRightRadius,
                containerBottomLeftRadius
        );
    }

    /// Updates the uniform background radius used by non-menu list items.
    private void updateStandardContainerShape(double radius) {
        if (Double.compare(containerTopLeftRadius, radius) == 0
                && Double.compare(containerTopRightRadius, radius) == 0
                && Double.compare(containerBottomRightRadius, radius) == 0
                && Double.compare(containerBottomLeftRadius, radius) == 0) {
            return;
        }

        setContainerShape(radius, radius, radius, radius);
        setContainerShapeTarget(radius, radius, radius, radius);
        String style = "-fx-background-radius: " + formatPixels(radius) + ";";
        selectionContainer.setStyle(style);
        container.setStyle(style);
        if (getSkinnable().getScene() != null) {
            selectionContainer.applyCss();
            container.applyCss();
        }
    }

    /// Updates text and layout after text content changes.
    private void updateTextAndMetrics() {
        updateText();
        updateMetrics();
    }

    /// Updates label text and visibility.
    private void updateText() {
        M3ListItemBase item = getSkinnable();
        updateLabel(overlineLabel, item.getOverlineText());
        updateLabel(headlineLabel, item.getHeadlineText());
        updateLabel(supportingLabel, item.getSupportingText());
        updateLabel(trailingSupportingLabel, item.getTrailingSupportingText());
        updateTrailingBoxVisibility();
    }

    /// Updates one label from a string value.
    private static void updateLabel(Label label, @Nullable String text) {
        boolean visible = text != null && !text.isBlank();
        label.setText(text);
        label.setVisible(visible);
        label.setManaged(visible);
    }

    /// Updates leading and trailing slot content.
    private void updateSlots() {
        M3ListItemBase item = getSkinnable();
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
        M3ListItemBase item = getSkinnable();
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
        M3ListItemBase item = getSkinnable();
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
    private static double preferredHeight(M3ListItemBase item) {
        return switch (item.getLineCount()) {
            case ONE_LINE -> item.getOneLineHeight();
            case TWO_LINE -> item.getTwoLineHeight();
            case THREE_LINE -> item.getThreeLineHeight();
        };
    }

    /// Updates or animates the independently resolved state-container corner radii.
    private void updateContainerShapeTarget(
            double topLeftRadius,
            double topRightRadius,
            double bottomRightRadius,
            double bottomLeftRadius
    ) {
        if (Double.isNaN(containerTopLeftRadius)) {
            setContainerShape(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
            setContainerShapeTarget(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
            return;
        }

        if (Double.compare(targetContainerTopLeftRadius, topLeftRadius) == 0
                && Double.compare(targetContainerTopRightRadius, topRightRadius) == 0
                && Double.compare(targetContainerBottomRightRadius, bottomRightRadius) == 0
                && Double.compare(targetContainerBottomLeftRadius, bottomLeftRadius) == 0) {
            return;
        }

        setContainerShapeTarget(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        @Nullable ContainerShapeTransition shapeAnimation = containerShapeAnimation;
        if (shapeAnimation == null || getSkinnable().getScene() == null) {
            if (shapeAnimation != null) {
                shapeAnimation.stop();
            }
            setContainerShape(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
            return;
        }

        shapeAnimation.configure(
                M3Animation.defaultSpatial(getSkinnable()),
                topLeftRadius,
                topRightRadius,
                bottomRightRadius,
                bottomLeftRadius
        );
        M3Animation.playFromStart(getSkinnable(), shapeAnimation);
    }

    /// Stores the current rendered state-container corner radii.
    private void setContainerShape(
            double topLeftRadius,
            double topRightRadius,
            double bottomRightRadius,
            double bottomLeftRadius
    ) {
        containerTopLeftRadius = topLeftRadius;
        containerTopRightRadius = topRightRadius;
        containerBottomRightRadius = bottomRightRadius;
        containerBottomLeftRadius = bottomLeftRadius;
    }

    /// Stores the requested state-container corner radii.
    private void setContainerShapeTarget(
            double topLeftRadius,
            double topRightRadius,
            double bottomRightRadius,
            double bottomLeftRadius
    ) {
        targetContainerTopLeftRadius = topLeftRadius;
        targetContainerTopRightRadius = topRightRadius;
        targetContainerBottomRightRadius = bottomRightRadius;
        targetContainerBottomLeftRadius = bottomLeftRadius;
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

    /// Applies the menu active-state opacity while a submenu remains visible.
    private void updateSubMenuActiveState() {
        M3ListItemBase item = getSkinnable();
        if (!(item instanceof M3SubMenuItem subMenuItem) || !subMenuItem.isSubMenuShowing()) {
            stateLayer.setRestingOverlayOpacity(0.0);
            return;
        }

        @Nullable M3Theme theme = M3ThemeResolver.findTheme(item);
        M3StateLayerTokens tokens = theme == null
                ? FALLBACK_STATE_LAYER_TOKENS
                : theme.tokens().stateLayerTokens();
        stateLayer.setRestingOverlayOpacity(tokens.hoverOpacity());
    }

    /// Animates the selected container to the requested state.
    private void animateSelectionContainer(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_SELECTION_SCALE;
        selectionAnimation.stop();
        if (!selected) {
            updateSelectedChildPseudoClasses(false);
        }
        selectionAnimationTargetSelected = selected;
        M3MotionSpec spec = M3Animation.defaultEffects(getSkinnable());
        selectionAnimation.configure(
                spec,
                targetOpacity,
                targetScale,
                targetScale,
                selectionContainer.getTranslateX(),
                selectionContainer.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Mirrors the settled selected state after the reusable selection animation completes.
    private void finishSelectionAnimation() {
        boolean selected = selectionAnimationTargetSelected;
        if (getSkinnable().isSelected() == selected) {
            updateSelectedChildPseudoClasses(selected);
        }
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
    private void installBehaviorHandlers(M3ListItemBase item) {
        item.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Starts list item feedback on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        M3ListItemBase item = getSkinnable();
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
        M3ListItemBase item = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = !item.isDisabled() && item.contains(event.getX(), event.getY());
        mousePressed = false;
        if (!spaceKeyPressed) {
            stateLayer.releaseRipple();
        }
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
        if (!mousePressed) {
            stateLayer.releaseRipple();
        }
        if (shouldFire) {
            getSkinnable().fire();
        }
        event.consume();
    }

    /// Ends an unfinished Space activation without disturbing an active pointer gesture.
    private void cancelKeyboardInteraction() {
        if (!spaceKeyPressed) {
            return;
        }

        spaceKeyPressed = false;
        if (!mousePressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Clears transient pointer and keyboard feedback.
    private void resetInteractionState() {
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.cancelRipple();
    }

    /// Reuses one transition while independently interpolating all four menu-item corners.
    @NotNullByDefault
    private final class ContainerShapeTransition extends M3FiniteTransition {
        /// The starting top-left radius for the current run.
        private double startTopLeftRadius;

        /// The starting top-right radius for the current run.
        private double startTopRightRadius;

        /// The starting bottom-right radius for the current run.
        private double startBottomRightRadius;

        /// The starting bottom-left radius for the current run.
        private double startBottomLeftRadius;

        /// The target top-left radius for the current run.
        private double targetTopLeftRadius;

        /// The target top-right radius for the current run.
        private double targetTopRightRadius;

        /// The target bottom-right radius for the current run.
        private double targetBottomRightRadius;

        /// The target bottom-left radius for the current run.
        private double targetBottomLeftRadius;

        /// Configures a run from the currently rendered shape to the requested shape.
        private void configure(
                M3MotionSpec spec,
                double topLeftRadius,
                double topRightRadius,
                double bottomRightRadius,
                double bottomLeftRadius
        ) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            startTopLeftRadius = containerTopLeftRadius;
            startTopRightRadius = containerTopRightRadius;
            startBottomRightRadius = containerBottomRightRadius;
            startBottomLeftRadius = containerBottomLeftRadius;
            targetTopLeftRadius = topLeftRadius;
            targetTopRightRadius = topRightRadius;
            targetBottomRightRadius = bottomRightRadius;
            targetBottomLeftRadius = bottomLeftRadius;
        }

        /// Applies the eased corner values without allocating pulse-local geometry objects.
        @Override
        protected void interpolate(double fraction) {
            setContainerShape(
                    startTopLeftRadius + (targetTopLeftRadius - startTopLeftRadius) * fraction,
                    startTopRightRadius + (targetTopRightRadius - startTopRightRadius) * fraction,
                    startBottomRightRadius + (targetBottomRightRadius - startBottomRightRadius) * fraction,
                    startBottomLeftRadius + (targetBottomLeftRadius - startBottomLeftRadius) * fraction
            );
            getSkinnable().requestLayout();
        }
    }

    /// A reusable asymmetric rounded rectangle used to clip menu-item surfaces.
    @NotNullByDefault
    private static final class RoundedRectangleClip extends Path {
        /// The top-right corner element index.
        private static final int TOP_RIGHT_CORNER_INDEX = 2;

        /// The bottom-right corner element index.
        private static final int BOTTOM_RIGHT_CORNER_INDEX = 4;

        /// The bottom-left corner element index.
        private static final int BOTTOM_LEFT_CORNER_INDEX = 6;

        /// The top-left corner element index.
        private static final int TOP_LEFT_CORNER_INDEX = 8;

        /// The path starting point.
        private final MoveTo start = new MoveTo();

        /// The top edge.
        private final LineTo topEdge = new LineTo();

        /// The rounded top-right corner.
        private final ArcTo topRightArc = new ArcTo();

        /// The square top-right corner.
        private final LineTo topRightLine = new LineTo();

        /// The right edge.
        private final LineTo rightEdge = new LineTo();

        /// The rounded bottom-right corner.
        private final ArcTo bottomRightArc = new ArcTo();

        /// The square bottom-right corner.
        private final LineTo bottomRightLine = new LineTo();

        /// The bottom edge.
        private final LineTo bottomEdge = new LineTo();

        /// The rounded bottom-left corner.
        private final ArcTo bottomLeftArc = new ArcTo();

        /// The square bottom-left corner.
        private final LineTo bottomLeftLine = new LineTo();

        /// The left edge.
        private final LineTo leftEdge = new LineTo();

        /// The rounded top-left corner.
        private final ArcTo topLeftArc = new ArcTo();

        /// The square top-left corner.
        private final LineTo topLeftLine = new LineTo();

        /// The width represented by the current path.
        private double width = Double.NaN;

        /// The height represented by the current path.
        private double height = Double.NaN;

        /// The current top-left radius.
        private double topLeftRadius = Double.NaN;

        /// The current top-right radius.
        private double topRightRadius = Double.NaN;

        /// The current bottom-right radius.
        private double bottomRightRadius = Double.NaN;

        /// The current bottom-left radius.
        private double bottomLeftRadius = Double.NaN;

        /// Creates an empty reusable rounded rectangle.
        private RoundedRectangleClip() {
            setFill(Color.BLACK);
            setStroke(null);
            getElements().addAll(
                    start,
                    topEdge,
                    topRightArc,
                    rightEdge,
                    bottomRightArc,
                    bottomEdge,
                    bottomLeftArc,
                    leftEdge,
                    topLeftArc,
                    new ClosePath()
            );
        }

        /// Returns this path for installation as a node clip.
        private Path path() {
            return this;
        }

        /// Updates the path to the supplied bounds and corner radii.
        private void update(
                double width,
                double height,
                double topLeftRadius,
                double topRightRadius,
                double bottomRightRadius,
                double bottomLeftRadius
        ) {
            if (Double.compare(this.width, width) == 0
                    && Double.compare(this.height, height) == 0
                    && Double.compare(this.topLeftRadius, topLeftRadius) == 0
                    && Double.compare(this.topRightRadius, topRightRadius) == 0
                    && Double.compare(this.bottomRightRadius, bottomRightRadius) == 0
                    && Double.compare(this.bottomLeftRadius, bottomLeftRadius) == 0) {
                return;
            }

            this.width = width;
            this.height = height;
            this.topLeftRadius = topLeftRadius;
            this.topRightRadius = topRightRadius;
            this.bottomRightRadius = bottomRightRadius;
            this.bottomLeftRadius = bottomLeftRadius;

            start.setX(topLeftRadius);
            start.setY(0.0);
            topEdge.setX(width - topRightRadius);
            topEdge.setY(0.0);
            updateCorner(
                    TOP_RIGHT_CORNER_INDEX,
                    topRightArc,
                    topRightLine,
                    topRightRadius,
                    width,
                    topRightRadius
            );
            rightEdge.setX(width);
            rightEdge.setY(height - bottomRightRadius);
            updateCorner(
                    BOTTOM_RIGHT_CORNER_INDEX,
                    bottomRightArc,
                    bottomRightLine,
                    bottomRightRadius,
                    width - bottomRightRadius,
                    height
            );
            bottomEdge.setX(bottomLeftRadius);
            bottomEdge.setY(height);
            updateCorner(
                    BOTTOM_LEFT_CORNER_INDEX,
                    bottomLeftArc,
                    bottomLeftLine,
                    bottomLeftRadius,
                    0.0,
                    height - bottomLeftRadius
            );
            leftEdge.setX(0.0);
            leftEdge.setY(topLeftRadius);
            updateCorner(
                    TOP_LEFT_CORNER_INDEX,
                    topLeftArc,
                    topLeftLine,
                    topLeftRadius,
                    topLeftRadius,
                    0.0
            );
        }

        /// Updates one corner and selects its rounded or square path element.
        private void updateCorner(
                int index,
                ArcTo arc,
                LineTo line,
                double radius,
                double x,
                double y
        ) {
            arc.setRadiusX(radius);
            arc.setRadiusY(radius);
            arc.setX(x);
            arc.setY(y);
            arc.setSweepFlag(true);
            line.setX(x);
            line.setY(y);
            PathElement element = radius <= 0.0 ? line : arc;
            if (getElements().get(index) != element) {
                getElements().set(index, element);
            }
        }
    }

}
