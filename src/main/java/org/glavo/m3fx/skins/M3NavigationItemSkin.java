// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3NavigationItem].
///
/// The skin presents the item's icon, label, optional badge, active indicator, state layer, ripple, and focus
/// indicator. It adapts that content to bar, rail, and drawer layouts and implements pointer and keyboard activation
/// without transferring scene focus to decorative child nodes.
@NotNullByDefault
public class M3NavigationItemSkin extends SkinBase<M3NavigationItem> {
    /// The selected indicator hidden scale.
    private static final double HIDDEN_INDICATOR_SCALE = 0.72;

    /// The style class applied to the internal badge wrapper.
    private static final String BADGE_STYLE_CLASS = "m3-navigation-item-badge";

    /// The bounded state layer used for hover, focus, pressed, and ripple feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The visual content stack.
    private final Pane content = new Pane();

    /// The icon and selected indicator slot.
    private final StackPane iconContainer = new StackPane();

    /// The selected indicator background.
    private final Region indicator = new Region();

    /// The graphic content slot.
    private final StackPane graphicContainer = new StackPane();

    /// The optional badge slot.
    private final StackPane badgeContainer = new StackPane();

    /// The size-constrained wrapper that prevents a badge from filling the indicator.
    private final StackPane badgeSlot = new StackPane();

    /// The item text label.
    private final Label label = new Label();

    /// The selected indicator animation timeline.
    private final M3NodeTransition indicatorAnimation = new M3NodeTransition(indicator);

    /// The last shape radius applied to the selected indicator.
    private double appliedIndicatorShape = Double.NaN;

    /// The width currently used by the active indicator and state layer.
    private double laidOutIndicatorWidth;

    /// The height currently used by the active indicator and state layer.
    private double laidOutIndicatorHeight;

    /// The fade-through opacity supplied by an animating navigation rail.
    private double railTransitionOpacity = 1.0;

    /// Handles primary mouse presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary mouse releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles pointer entry while a mouse press is active.
    private final EventHandler<MouseEvent> mouseEnteredHandler = this::handleMouseEntered;

    /// Handles pointer exit while a mouse press is active.
    private final EventHandler<MouseEvent> mouseExitedHandler = this::handleMouseExited;

    /// Handles keyboard activation presses.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Handles keyboard activation releases.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Updates logical overlay placement when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateNodeOrientationLayout();

    /// Updates the displayed label text.
    private final ChangeListener<@Nullable String> textListener =
            (observable, oldValue, newValue) -> updateText(newValue);

    /// Updates the displayed graphic node.
    private final ChangeListener<@Nullable Node> graphicListener =
            (observable, oldValue, newValue) -> updateGraphic(newValue);

    /// Updates the displayed badge.
    private final ChangeListener<@Nullable M3Badge> badgeListener =
            (observable, oldValue, newValue) -> updateBadge(newValue);

    /// Requests layout when the icon and label arrangement changes.
    private final ChangeListener<M3NavigationItemLayout> itemLayoutListener =
            (observable, oldValue, newValue) -> getSkinnable().requestLayout();

    /// Animates the selected indicator when selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectedIndicator(newValue);

    /// Resets transient feedback when the control becomes disabled.
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

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Creates a navigation item skin.
    ///
    /// @param control the skinned navigation item
    public M3NavigationItemSkin(M3NavigationItem control) {
        super(control);
        content.getStyleClass().add("m3-navigation-item-content");
        iconContainer.getStyleClass().add("m3-navigation-item-icon-container");
        indicator.getStyleClass().add("m3-navigation-item-indicator");
        graphicContainer.getStyleClass().add("m3-navigation-item-graphic");
        badgeContainer.getStyleClass().add("m3-navigation-item-badge-container");
        badgeSlot.getStyleClass().add(BADGE_STYLE_CLASS);
        label.getStyleClass().add("m3-navigation-item-label");

        content.setManaged(false);
        iconContainer.setManaged(false);
        iconContainer.setAlignment(Pos.CENTER);
        graphicContainer.setAlignment(Pos.CENTER);
        graphicContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        // Alignment is already resolved to a physical edge, so the container must not mirror it again.
        badgeContainer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        updateNodeOrientationLayout();
        badgeContainer.setManaged(false);
        badgeContainer.setMouseTransparent(true);
        badgeSlot.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        badgeSlot.setMouseTransparent(true);
        indicator.setManaged(false);
        indicator.setMouseTransparent(true);
        label.setMouseTransparent(true);

        badgeContainer.getChildren().add(badgeSlot);
        iconContainer.getChildren().addAll(indicator, stateLayer, graphicContainer, badgeContainer);
        content.getChildren().addAll(iconContainer, label);
        getChildren().setAll(content);

        stateLayer.installStateTransitions(control);
        updateText(control.getText());
        updateGraphic(control.getGraphic());
        updateBadge(control.getBadge());
        updateIndicatorImmediate(control.isSelected());
        installInteractionHandlers(control);
        control.textProperty().addListener(textListener);
        control.graphicProperty().addListener(graphicListener);
        control.badgeProperty().addListener(badgeListener);
        control.itemLayoutProperty().addListener(itemLayoutListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        control.selectedProperty().addListener(selectedListener);
        control.disabledProperty().addListener(disabledListener);
        control.focusedProperty().addListener(focusedListener);
        control.sceneProperty().addListener(sceneInvalidation);
        layoutStateLayer();
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3NavigationItem item = getSkinnable();
        indicatorAnimation.stop();
        stateLayer.uninstallStateTransitions();
        item.textProperty().removeListener(textListener);
        item.graphicProperty().removeListener(graphicListener);
        item.badgeProperty().removeListener(badgeListener);
        item.itemLayoutProperty().removeListener(itemLayoutListener);
        item.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        item.selectedProperty().removeListener(selectedListener);
        item.disabledProperty().removeListener(disabledListener);
        item.focusedProperty().removeListener(focusedListener);
        item.sceneProperty().removeListener(sceneInvalidation);
        uninstallInteractionHandlers(item);
        resetInteractionState();
        updateGraphic(null);
        updateBadge(null);
        getChildren().remove(content);
        super.dispose();
    }

    /// Lays out the item content and state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3NavigationItem item = getSkinnable();
        content.resizeRelocate(x, y, width, height);
        if (item.getItemLayout() == M3NavigationItemLayout.HORIZONTAL) {
            layoutHorizontalContent(width, height);
        } else {
            layoutVerticalContent(width, height);
        }
        applyIndicatorShape(item.getIndicatorShape());
    }

    /// Lays out a compact item with the label below the active indicator.
    private void layoutVerticalContent(double width, double height) {
        M3NavigationItem item = getSkinnable();
        double indicatorWidth = item.getIndicatorWidth();
        double indicatorHeight = item.getIndicatorHeight();
        double labelWidth = label.isManaged() ? Math.min(width, label.prefWidth(-1.0)) : 0.0;
        double labelHeight = label.isManaged() ? label.prefHeight(labelWidth) : 0.0;
        double spacing = label.isManaged() ? item.getContentSpacing() : 0.0;
        double totalHeight = indicatorHeight + spacing + labelHeight;
        double indicatorX = snapPositionX((width - indicatorWidth) / 2.0);
        double indicatorY = snapPositionY((height - totalHeight) / 2.0);

        layoutIndicator(indicatorX, indicatorY, indicatorWidth, indicatorHeight);
        graphicContainer.setTranslateX(0.0);
        layoutBadgeOverGraphic(indicatorWidth / 2.0, indicatorHeight / 2.0);
        if (label.isManaged()) {
            label.resizeRelocate(
                    snapPositionX((width - labelWidth) / 2.0),
                    snapPositionY(indicatorY + indicatorHeight + spacing),
                    snapSizeX(labelWidth),
                    snapSizeY(labelHeight)
            );
        }
    }

    /// Lays out a medium-window item with the icon and label inside one active indicator.
    private void layoutHorizontalContent(double width, double height) {
        M3NavigationItem item = getSkinnable();
        double graphicWidth = Math.max(24.0, graphicContainer.prefWidth(-1.0));
        double spacing = label.isManaged() ? item.getContentSpacing() : 0.0;
        @Nullable M3NavigationRail rail = navigationRail(item);
        boolean expandedRailItem = rail != null && item.getItemLayout() == M3NavigationItemLayout.HORIZONTAL;
        @Nullable M3Badge badge = item.getBadge();
        boolean inlineBadge = expandedRailItem && badge != null;
        double badgeWidth = inlineBadge ? Math.max(6.0, badge.prefWidth(-1.0)) : 0.0;
        double badgeSpacing = inlineBadge ? 8.0 : 0.0;
        double horizontalInset = 16.0;
        double maximumIndicatorWidth = expandedRailItem
                ? Math.max(0.0, width - horizontalInset * 2.0)
                : width;
        double maximumLabelWidth = Math.max(
                0.0,
                maximumIndicatorWidth - horizontalInset * 2.0
                        - graphicWidth - spacing - badgeSpacing - badgeWidth
        );
        double labelWidth = label.isManaged()
                ? Math.min(label.prefWidth(-1.0), maximumLabelWidth)
                : 0.0;
        double labelHeight = label.isManaged() ? label.prefHeight(labelWidth) : 0.0;
        double combinedWidth = graphicWidth + spacing + labelWidth + badgeSpacing + badgeWidth;
        boolean fullWidthIndicator = expandedRailItem && rail.isFullWidthIndicator();
        double desiredIndicatorWidth = fullWidthIndicator
                ? maximumIndicatorWidth
                : combinedWidth + horizontalInset * 2.0;
        double indicatorWidth = Math.min(
                maximumIndicatorWidth,
                Math.max(item.getIndicatorWidth(), desiredIndicatorWidth)
        );
        double indicatorHeight = item.getIndicatorHeight();
        double indicatorX = snapPositionX(expandedRailItem
                ? horizontalInset
                : (width - indicatorWidth) / 2.0);
        double indicatorY = snapPositionY((height - indicatorHeight) / 2.0);
        double contentStart = expandedRailItem
                ? horizontalInset
                : (indicatorWidth - combinedWidth) / 2.0;
        double graphicCenterX = contentStart + graphicWidth / 2.0;
        double labelX = contentStart + graphicWidth + spacing;

        layoutIndicator(indicatorX, indicatorY, indicatorWidth, indicatorHeight);
        graphicContainer.setTranslateX(graphicCenterX - indicatorWidth / 2.0);
        if (inlineBadge) {
            double badgeCenterX = labelX + labelWidth + badgeSpacing + badgeWidth / 2.0;
            layoutBadgeInline(badgeCenterX, indicatorHeight / 2.0);
        } else {
            layoutBadgeOverGraphic(graphicCenterX, indicatorHeight / 2.0);
        }
        if (label.isManaged()) {
            label.resizeRelocate(
                    snapPositionX(indicatorX + labelX),
                    snapPositionY(indicatorY + (indicatorHeight - labelHeight) / 2.0),
                    snapSizeX(labelWidth),
                    snapSizeY(labelHeight)
            );
        }
    }

    /// Returns the containing navigation rail, when the item belongs to one.
    private static @Nullable M3NavigationRail navigationRail(M3NavigationItem item) {
        @Nullable Parent ancestor = item.getParent();
        while (ancestor != null) {
            if (ancestor instanceof M3NavigationRail rail) {
                return rail;
            }
            ancestor = ancestor.getParent();
        }
        return null;
    }

    /// Lays out the active indicator, feedback layer, and icon container.
    private void layoutIndicator(double x, double y, double width, double height) {
        laidOutIndicatorWidth = width;
        laidOutIndicatorHeight = height;
        iconContainer.resizeRelocate(x, y, width, height);
        indicator.resizeRelocate(0.0, 0.0, width, height);
        layoutStateLayer();
    }

    /// Positions the badge around the logical trailing top edge of the icon graphic.
    private void layoutBadgeOverGraphic(double graphicCenterX, double graphicCenterY) {
        badgeContainer.setAlignment(badgeAlignment());
        StackPane.setAlignment(badgeSlot, badgeAlignment());
        badgeSlot.setTranslateX(M3NodeLayout.isRightToLeft(getSkinnable()) ? -4.0 : 4.0);
        badgeSlot.setTranslateY(-2.0);
        double badgeAnchorSize = 24.0;
        badgeContainer.resizeRelocate(
                snapPositionX(graphicCenterX - badgeAnchorSize / 2.0),
                snapPositionY(graphicCenterY - badgeAnchorSize / 2.0),
                badgeAnchorSize,
                badgeAnchorSize
        );
    }

    /// Positions an expanded-rail badge directly after the destination label.
    private void layoutBadgeInline(double badgeCenterX, double badgeCenterY) {
        badgeContainer.setAlignment(Pos.CENTER);
        StackPane.setAlignment(badgeSlot, Pos.CENTER);
        badgeSlot.setTranslateX(0.0);
        badgeSlot.setTranslateY(0.0);
        double badgeAnchorSize = 24.0;
        badgeContainer.resizeRelocate(
                snapPositionX(badgeCenterX - badgeAnchorSize / 2.0),
                snapPositionY(badgeCenterY - badgeAnchorSize / 2.0),
                badgeAnchorSize,
                badgeAnchorSize
        );
    }

    /// Applies the active indicator radius only when its token changes.
    private void applyIndicatorShape(double indicatorShape) {
        if (Double.compare(appliedIndicatorShape, indicatorShape) != 0) {
            appliedIndicatorShape = indicatorShape;
            indicator.setStyle("-fx-background-radius: " + M3Css.pixels(indicatorShape) + ";");
        }
    }

    /// Installs mouse and keyboard behavior handlers.
    private void installInteractionHandlers(M3NavigationItem item) {
        item.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        item.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        item.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Removes mouse and keyboard behavior handlers.
    private void uninstallInteractionHandlers(M3NavigationItem item) {
        item.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        item.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        item.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Arms the item on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (item.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        M3FocusRequests.requestFocusIfTraversable(item);
        layoutStateLayer();
        @Nullable Point2D origin = stateLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        if (origin == null) {
            stateLayer.playCenteredRipple();
        } else {
            stateLayer.playRipple(
                    Math.max(0.0, Math.min(stateLayer.getWidth(), origin.getX())),
                    Math.max(0.0, Math.min(stateLayer.getHeight(), origin.getY()))
            );
        }
        item.arm();
        event.consume();
    }

    /// Fires the item when a primary mouse press is released inside the control.
    private void handleMouseReleased(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = item.isArmed() && item.contains(event.getX(), event.getY());
        mousePressed = false;
        if (!spaceKeyPressed) {
            stateLayer.releaseRipple();
            item.disarm();
        }
        if (shouldFire) {
            item.fire();
        }
        event.consume();
    }

    /// Re-arms the item when a pressed pointer re-enters the control.
    private void handleMouseEntered(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (mousePressed && !item.isDisabled()) {
            item.arm();
            event.consume();
        }
    }

    /// Disarms the item when a pressed pointer exits the control.
    private void handleMouseExited(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (mousePressed && !item.isDisabled()) {
            item.disarm();
            event.consume();
        }
    }

    /// Handles keyboard activation for enter and space.
    private void handleKeyPressed(KeyEvent event) {
        M3NavigationItem item = getSkinnable();
        if (item.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                layoutStateLayer();
                stateLayer.playCenteredRipple();
                item.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            layoutStateLayer();
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            item.fire();
            event.consume();
        }
    }

    /// Fires the item when a space key activation is released.
    private void handleKeyReleased(KeyEvent event) {
        M3NavigationItem item = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = item.isArmed() && !item.isDisabled();
        spaceKeyPressed = false;
        if (!mousePressed) {
            stateLayer.releaseRipple();
            item.disarm();
        }
        if (shouldFire) {
            item.fire();
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
            getSkinnable().disarm();
        }
    }

    /// Updates the label from the current item text.
    private void updateText(@Nullable String text) {
        boolean hasText = text != null && !text.isEmpty();
        label.setText(hasText ? text : "");
        label.setVisible(hasText);
        label.setManaged(hasText);
    }

    /// Updates the graphic slot from the current item graphic.
    private void updateGraphic(@Nullable Node graphic) {
        graphicContainer.getChildren().clear();
        if (graphic != null) {
            graphicContainer.getChildren().add(graphic);
        }
    }

    /// Updates the badge slot from the current item badge.
    private void updateBadge(@Nullable M3Badge badge) {
        badgeSlot.getChildren().clear();
        if (badge != null) {
            badgeSlot.getChildren().add(badge);
        }
        getSkinnable().requestLayout();
    }

    /// Updates alignment for overlay content that is anchored to logical end.
    private void updateNodeOrientationLayout() {
        badgeContainer.setAlignment(badgeAlignment());
        StackPane.setAlignment(badgeSlot, badgeAlignment());
        badgeSlot.setTranslateX(M3NodeLayout.isRightToLeft(getSkinnable()) ? -4.0 : 4.0);
        badgeSlot.setTranslateY(-2.0);
        getSkinnable().requestLayout();
    }

    /// Returns the badge alignment for the current logical end edge.
    private Pos badgeAlignment() {
        return M3NodeLayout.physicalEndTopAlignment(getSkinnable());
    }

    /// Animates the selected indicator to the requested state.
    private void animateSelectedIndicator(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_INDICATOR_SCALE;
        indicatorAnimation.stop();
        M3MotionSpec spec = M3Animation.defaultEffects(getSkinnable());
        indicatorAnimation.configure(
                spec,
                targetOpacity,
                targetScale,
                indicator.getScaleY(),
                indicator.getTranslateX(),
                indicator.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), indicatorAnimation);
    }

    /// Updates the selected indicator without animation.
    private void updateIndicatorImmediate(boolean selected) {
        indicator.setOpacity(selected ? 1.0 : 0.0);
        indicator.setScaleX(selected ? 1.0 : HIDDEN_INDICATOR_SCALE);
    }

    /// Clears armed state and transient feedback.
    private void resetInteractionState() {
        M3NavigationItem item = getSkinnable();
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.cancelRipple();
        item.disarm();
    }

    /// Lays out the state layer within the active-indicator container.
    private void layoutStateLayer() {
        M3NavigationItem item = getSkinnable();
        double width = laidOutIndicatorWidth > 0.0 ? laidOutIndicatorWidth : item.getIndicatorWidth();
        double height = laidOutIndicatorHeight > 0.0 ? laidOutIndicatorHeight : item.getIndicatorHeight();
        stateLayer.layoutLayer(0.0, 0.0, width, height, item.getIndicatorShape());
    }

    /// Applies fade-through opacity while an owning navigation rail changes item layout.
    ///
    /// @param opacity the content opacity from zero through one
    final void setRailTransitionOpacity(double opacity) {
        double boundedOpacity = Math.max(0.0, Math.min(1.0, opacity));
        if (Double.compare(railTransitionOpacity, boundedOpacity) == 0) {
            return;
        }
        railTransitionOpacity = boundedOpacity;
        content.setOpacity(boundedOpacity);
    }
}
