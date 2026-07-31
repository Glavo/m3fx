// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ButtonBase;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PresentationActivity;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Base skin for M3FX controls that combine [ButtonBase] behavior with labeled content.
///
/// The skin supplies pointer and keyboard arming, bounded state-layer and ripple feedback, CSS-resolved elevation
/// transitions, and container-shape motion. Subclasses may override [#pressedScale(boolean)] to add depth-style
/// scale motion without replacing the common interaction lifecycle.
@NotNullByDefault
abstract class M3LabeledButtonSkinBase<C extends ButtonBase> extends LabeledSkinBase<C> {
    /// The internal single-segment style class.
    private static final String SINGLE_SEGMENT_STYLE_CLASS = "m3-segmented-button-single";

    /// The internal first-segment style class.
    private static final String FIRST_SEGMENT_STYLE_CLASS = "m3-segmented-button-first";

    /// The internal middle-segment style class.
    private static final String MIDDLE_SEGMENT_STYLE_CLASS = "m3-segmented-button-middle";

    /// The internal last-segment style class.
    private static final String LAST_SEGMENT_STYLE_CLASS = "m3-segmented-button-last";

    /// The split-button action part style class.
    private static final String SPLIT_ACTION_BUTTON_STYLE_CLASS = "m3-split-button-action";

    /// The split-button menu part style class.
    private static final String SPLIT_MENU_BUTTON_STYLE_CLASS = "m3-split-button-menu";

    /// The time-picker cell style class.
    private static final String TIME_CELL_STYLE_CLASS = "m3-time-picker-cell";

    /// The internal date-cell style class used for picker-specific optical centering.
    private static final String DATE_PICKER_DAY_CELL_STYLE_CLASS = "m3-date-picker-day-cell";

    /// The internal single-button group style class.
    private static final String SINGLE_BUTTON_STYLE_CLASS = "m3-button-group-single";

    /// The internal first-button group style class.
    private static final String FIRST_BUTTON_STYLE_CLASS = "m3-button-group-first";

    /// The internal middle-button group style class.
    private static final String MIDDLE_BUTTON_STYLE_CLASS = "m3-button-group-middle";

    /// The internal last-button group style class.
    private static final String LAST_BUTTON_STYLE_CLASS = "m3-button-group-last";

    /// The scale applied by controls that opt into depth-style pressed motion.
    private static final double PRESSED_SCALE = 0.98;

    /// The press-scale transition, created when a button first needs scale motion.
    private @Nullable M3NodeTransition pressedAnimation;

    /// The bounded state layer used for hover, focus, pressed, and ripple feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// Animates CSS-resolved elevation changes.
    private final M3CssEffectTransition effectTransition;

    /// The last CSS corner target observed by this skin.
    private @Nullable CornerRadii containerShapeTarget;

    /// The reusable corner transition, allocated only after the first animated shape change.
    private @Nullable ContainerShapeTransition containerShapeTransition;

    /// Retargets shape motion after CSS changes a background or border radius.
    private final InvalidationListener containerShapeTargetInvalidation = observable -> updateContainerShapeTarget();

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

    /// Keeps the state layer sized when controls are resized outside a layout pass.
    private final InvalidationListener stateLayerLayoutInvalidation = observable -> {
        @Nullable ContainerShapeTransition transition = containerShapeTransition;
        if (transition != null) {
            transition.refreshGeometry();
        }
        layoutStateLayer();
    };

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Animates the pressed scale when the armed state changes.
    private final ChangeListener<Boolean> armedListener =
            (observable, oldValue, newValue) -> animatePressedState(newValue);

    /// Resets transient feedback when the control becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Cancels keyboard ownership when focus moves away before the Space key is released.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            cancelKeyboardInteraction();
        }
    };

    /// Clears pointer and keyboard ownership when the control leaves its scene.
    private final InvalidationListener sceneInvalidation = observable -> {
        if (getSkinnable().getScene() == null) {
            resetInteractionState();
        }
    };

    /// Creates an animated labeled button skin and installs its interaction observers.
    ///
    /// @param control the button controlled by this skin
    M3LabeledButtonSkinBase(C control) {
        super(control);
        effectTransition = new M3CssEffectTransition(control, control);
        getChildren().add(0, stateLayer);
        stateLayer.installStateTransitions(control);
        effectTransition.install();
        control.setScaleX(1.0);
        control.setScaleY(1.0);
        installInteractionHandlers(control);
        control.widthProperty().addListener(stateLayerLayoutInvalidation);
        control.heightProperty().addListener(stateLayerLayoutInvalidation);
        control.layoutBoundsProperty().addListener(stateLayerLayoutInvalidation);
        control.backgroundProperty().addListener(stateLayerLayoutInvalidation);
        control.borderProperty().addListener(stateLayerLayoutInvalidation);
        control.backgroundProperty().addListener(containerShapeTargetInvalidation);
        control.borderProperty().addListener(containerShapeTargetInvalidation);
        control.armedProperty().addListener(armedListener);
        control.disabledProperty().addListener(disabledListener);
        control.focusedProperty().addListener(focusedListener);
        control.sceneProperty().addListener(sceneInvalidation);
        updateContainerShapeTarget();
        layoutStateLayer();
    }

    /// Applies a concrete content paint to this skin's state layer and ripple.
    ///
    /// @param paint the paint used by state and ripple overlays
    protected final void setStateLayerPaint(Paint paint) {
        stateLayer.setContentPaint(paint);
    }

    /// Applies a concrete container paint beneath labeled content and interaction feedback.
    ///
    /// @param paint the paint clipped to the current button shape, or `null` to expose the CSS background
    protected final void setContainerPaint(@Nullable Paint paint) {
        stateLayer.setContainerPaint(paint);
    }

    /// Stops the animation before the skin is disposed.
    @Override
    public void dispose() {
        getSkinnable().widthProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().heightProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().layoutBoundsProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().backgroundProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().borderProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().backgroundProperty().removeListener(containerShapeTargetInvalidation);
        getSkinnable().borderProperty().removeListener(containerShapeTargetInvalidation);
        getSkinnable().armedProperty().removeListener(armedListener);
        getSkinnable().disabledProperty().removeListener(disabledListener);
        getSkinnable().focusedProperty().removeListener(focusedListener);
        getSkinnable().sceneProperty().removeListener(sceneInvalidation);
        @Nullable ContainerShapeTransition transition = containerShapeTransition;
        if (transition != null) {
            transition.dispose();
            containerShapeTransition = null;
        }
        containerShapeTarget = null;
        resetInteractionState();
        pressedAnimation = null;
        stateLayer.uninstallStateTransitions();
        effectTransition.uninstall();
        uninstallInteractionHandlers(getSkinnable());
        super.dispose();
    }

    /// Lays out labeled content and the bounded state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        @Nullable Insets stableInsets = stableContainerInsets();
        double contentX = stableInsets == null ? x : stableInsets.getLeft();
        double contentY = stableInsets == null ? y : stableInsets.getTop();
        double contentWidth = stableInsets == null
                ? width
                : Math.max(0.0, getSkinnable().getWidth()
                - stableInsets.getLeft()
                - stableInsets.getRight());
        double contentHeight = stableInsets == null
                ? height
                : Math.max(0.0, getSkinnable().getHeight()
                - stableInsets.getTop()
                - stableInsets.getBottom());
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        centerFixedTargetContent(contentX, contentY, contentWidth, contentHeight);
        @Nullable ContainerShapeTransition transition = containerShapeTransition;
        if (transition != null) {
            transition.refreshGeometry();
        }
        layoutStateLayer();
    }

    /// Computes the minimum width without allowing a temporary Region shape to remove border insets.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Insets stableInsets = stableContainerInsets();
        if (stableInsets == null) {
            return super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
        }
        return super.computeMinWidth(
                height,
                stableInsets.getTop(),
                stableInsets.getRight(),
                stableInsets.getBottom(),
                stableInsets.getLeft()
        );
    }

    /// Computes the minimum height without allowing a temporary Region shape to remove border insets.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Insets stableInsets = stableContainerInsets();
        if (stableInsets == null) {
            return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
        }
        return super.computeMinHeight(
                width,
                stableInsets.getTop(),
                stableInsets.getRight(),
                stableInsets.getBottom(),
                stableInsets.getLeft()
        );
    }

    /// Computes the preferred width without allowing a temporary Region shape to remove border insets.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Insets stableInsets = stableContainerInsets();
        if (stableInsets == null) {
            return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        }
        return super.computePrefWidth(
                height,
                stableInsets.getTop(),
                stableInsets.getRight(),
                stableInsets.getBottom(),
                stableInsets.getLeft()
        );
    }

    /// Computes the preferred height without allowing a temporary Region shape to remove border insets.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Insets stableInsets = stableContainerInsets();
        if (stableInsets == null) {
            return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }
        return super.computePrefHeight(
                width,
                stableInsets.getTop(),
                stableInsets.getRight(),
                stableInsets.getBottom(),
                stableInsets.getLeft()
        );
    }

    /// Computes the baseline without allowing a temporary Region shape to remove border insets.
    @Override
    public double computeBaselineOffset(
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Insets stableInsets = stableContainerInsets();
        if (stableInsets == null) {
            return super.computeBaselineOffset(topInset, rightInset, bottomInset, leftInset);
        }
        return super.computeBaselineOffset(
                stableInsets.getTop(),
                stableInsets.getRight(),
                stableInsets.getBottom(),
                stableInsets.getLeft()
        );
    }

    /// Returns the snapped padding and border insets preserved across temporary Region-shape ownership.
    private @Nullable Insets stableContainerInsets() {
        @Nullable ContainerShapeTransition transition = containerShapeTransition;
        if (transition == null || !transition.stabilizesRegionInsets()) {
            return null;
        }
        return transition.stableRegionInsets();
    }

    /// Installs mouse and keyboard behavior handlers.
    private void installInteractionHandlers(C button) {
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        button.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        button.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        button.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Removes mouse and keyboard behavior handlers.
    private void uninstallInteractionHandlers(C button) {
        button.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        button.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        button.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        button.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        button.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        button.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Arms the button on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        C button = getSkinnable();
        if (button.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        M3FocusRequests.requestFocusIfTraversable(button);
        layoutStateLayer();
        double rippleX = Math.max(0.0, Math.min(stateLayer.getWidth(),
                event.getX() - stateLayer.getLayoutX()));
        double rippleY = Math.max(0.0, Math.min(stateLayer.getHeight(),
                event.getY() - stateLayer.getLayoutY()));
        stateLayer.playRipple(
                rippleX,
                rippleY
        );
        button.arm();
        event.consume();
    }

    /// Fires the button when a primary mouse press is released inside the control.
    private void handleMouseReleased(MouseEvent event) {
        C button = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = button.isArmed() && button.contains(event.getX(), event.getY());
        mousePressed = false;
        if (!spaceKeyPressed) {
            stateLayer.releaseRipple();
            button.disarm();
        }
        if (shouldFire) {
            button.fire();
        }
        event.consume();
    }

    /// Re-arms the button when a pressed pointer re-enters the control.
    private void handleMouseEntered(MouseEvent event) {
        C button = getSkinnable();
        if (mousePressed && !button.isDisabled()) {
            button.arm();
            event.consume();
        }
    }

    /// Disarms the button when a pressed pointer exits the control.
    private void handleMouseExited(MouseEvent event) {
        C button = getSkinnable();
        if (mousePressed && !button.isDisabled()) {
            button.disarm();
            event.consume();
        }
    }

    /// Handles keyboard activation for enter and space.
    private void handleKeyPressed(KeyEvent event) {
        C button = getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                layoutStateLayer();
                stateLayer.playCenteredRipple();
                button.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            layoutStateLayer();
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            button.fire();
            event.consume();
        }
    }

    /// Fires the button when a space key activation is released.
    private void handleKeyReleased(KeyEvent event) {
        C button = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = button.isArmed() && !button.isDisabled();
        spaceKeyPressed = false;
        if (!mousePressed) {
            stateLayer.releaseRipple();
            button.disarm();
        }
        if (shouldFire) {
            button.fire();
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

    /// Animates the skinnable button into or out of the pressed state.
    private void animatePressedState(boolean pressed) {
        C button = getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        double scale = pressedScale(pressed);
        M3NodeTransition animation = pressedAnimation;
        if (Double.compare(button.getScaleX(), scale) == 0
                && Double.compare(button.getScaleY(), scale) == 0
                && (animation == null || animation.getStatus() != Animation.Status.RUNNING)) {
            return;
        }
        if (animation == null) {
            animation = new M3NodeTransition(button);
            pressedAnimation = animation;
        }
        M3MotionSpec spec = pressed ? M3Animation.fastEffects(button) : M3Animation.defaultEffects(button);
        animation.stop();
        animation.configure(
                spec,
                button.getOpacity(),
                scale,
                scale,
                button.getTranslateX(),
                button.getTranslateY()
        );
        M3Animation.playFromStart(button, animation);
    }

    /// Returns the skinnable scale used for the requested pressed state.
    ///
    /// @param pressed whether the skinnable is currently pressed
    /// @return the scale applied while transitioning to the requested pressed state
    protected double pressedScale(boolean pressed) {
        return 1.0;
    }

    /// Returns the skinnable scale for controls that opt into depth-style pressed motion.
    ///
    /// @param pressed whether the skinnable is currently pressed
    /// @return the depth-style pressed scale, or `1.0` when released
    protected final double depthPressedScale(boolean pressed) {
        return pressed ? PRESSED_SCALE : 1.0;
    }

    /// Centers content whose touch target is fixed and square.
    private void centerFixedTargetContent(double x, double y, double width, double height) {
        C button = getSkinnable();
        @Nullable Node graphic = button.getGraphic();
        @Nullable String text = button.getText();
        if (graphic != null
                && (button.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY
                || text == null
                || text.isEmpty())) {
            centerGraphicOnlyContent(graphic, x, y, width, height);
            return;
        }

        if (graphic == null && isFixedPickerCell(button)) {
            centerFixedPickerCellTextContent(x, y, width, height);
        }
    }

    /// Returns whether a button is a picker cell whose numeric text needs optical centering.
    private static boolean isFixedPickerCell(ButtonBase button) {
        return button.getStyleClass().contains(DATE_PICKER_DAY_CELL_STYLE_CLASS)
                || button.getStyleClass().contains(TIME_CELL_STYLE_CLASS);
    }

    /// Centers fixed picker-cell text by visual glyph bounds so rendered digits stay optically centered.
    private void centerFixedPickerCellTextContent(double x, double y, double width, double height) {
        @Nullable Node textNode = firstTextNode();
        if (textNode == null) {
            return;
        }

        if (textNode instanceof Text text) {
            text.setBoundsType(TextBoundsType.VISUAL);
        }
        centerNodeInArea(textNode, x, y, width, height);
    }

    /// Centers graphic-only content in the correct Material visual container.
    private void centerGraphicOnlyContent(Node graphic, double x, double y, double width, double height) {
        C button = getSkinnable();
        if (button instanceof M3FloatingActionButton) {
            double controlWidth = button.getWidth();
            double controlHeight = button.getHeight();
            if (controlWidth <= 0.0) {
                controlWidth = button.getLayoutBounds().getWidth();
            }
            if (controlHeight <= 0.0) {
                controlHeight = button.getLayoutBounds().getHeight();
            }
            centerNodeInArea(graphic, 0.0, 0.0, controlWidth, controlHeight);
            return;
        }

        centerNodeInArea(graphic, x, y, width, height);
    }

    /// Centers a child node inside the supplied skin layout area.
    private void centerNodeInArea(Node node, double x, double y, double width, double height) {
        Bounds bounds = node.getLayoutBounds();
        double targetX = x + (width - bounds.getWidth()) / 2.0;
        double targetY = y + (height - bounds.getHeight()) / 2.0;
        node.relocate(snapPositionX(targetX), snapPositionY(targetY));
    }

    /// Returns the first direct or nested text node owned by this skin.
    private @Nullable Node firstTextNode() {
        for (Node child : getChildren()) {
            @Nullable Node textNode = firstTextNode(child);
            if (textNode != null) {
                return textNode;
            }
        }
        return null;
    }

    /// Returns the first text node inside a child hierarchy.
    private static @Nullable Node firstTextNode(Node node) {
        if (node instanceof Text) {
            return node;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node textNode = firstTextNode(child);
                if (textNode != null) {
                    return textNode;
                }
            }
        }
        return null;
    }

    /// Clears armed state, scale animation, and transient feedback.
    private void resetInteractionState() {
        C control = getSkinnable();
        mousePressed = false;
        spaceKeyPressed = false;
        M3NodeTransition animation = pressedAnimation;
        if (animation != null) {
            animation.stop();
        }
        stateLayer.cancelRipple();
        control.disarm();
        control.setScaleX(1.0);
        control.setScaleY(1.0);
    }

    /// Retargets the rendered button outline after CSS resolves a different corner shape.
    private void updateContainerShapeTarget() {
        C button = getSkinnable();
        @Nullable CornerRadii targetRadii = resolvedCornerRadii(button);
        @Nullable CornerRadii previousTarget = containerShapeTarget;
        if (targetRadii == null || targetRadii.equals(previousTarget)) {
            return;
        }
        containerShapeTarget = targetRadii;
        if (previousTarget == null) {
            return;
        }

        @Nullable ContainerShapeTransition transition = containerShapeTransition;
        boolean canAnimate = M3PresentationActivity.isRenderActive(button)
                && M3Animation.areAnimationsEnabled(button)
                && (button.getShape() == null
                || transition != null && transition.isSurfaceShapeActive());
        if (!canAnimate) {
            if (transition != null) {
                transition.snapTo(targetRadii);
            }
            return;
        }

        if (transition == null) {
            transition = new ContainerShapeTransition(previousTarget);
            containerShapeTransition = transition;
        }
        if (!transition.installSurfaceShape()) {
            transition.snapTo(targetRadii);
            return;
        }
        transition.configure(M3Animation.fastSpatial(button), targetRadii);
        M3Animation.playFromStart(button, transition);
    }

    /// Lays out the state layer over the visible component container.
    private void layoutStateLayer() {
        C control = getSkinnable();
        double width = control.getWidth();
        double height = control.getHeight();
        if (width <= 0.0) {
            width = control.getLayoutBounds().getWidth();
        }
        if (height <= 0.0) {
            height = control.getLayoutBounds().getHeight();
        }
        if (layoutGroupedButtonStateLayer(control, width, height)) {
            stateLayer.animateOverlayOpacityFromOwnerState();
            return;
        }
        if (control instanceof M3IconButton iconButton) {
            layoutCenteredIconStateLayer(
                    width,
                    height,
                    iconButton.getContainerWidth(),
                    iconButton.getContainerHeight()
            );
            return;
        }
        if (control instanceof M3IconToggleButton iconToggleButton) {
            layoutCenteredIconStateLayer(
                    width,
                    height,
                    iconToggleButton.getContainerWidth(),
                    iconToggleButton.getContainerHeight()
            );
            return;
        }
        if (control instanceof M3SegmentedButton segmentedButton) {
            layoutSegmentedButtonStateLayer(segmentedButton, width, height);
            stateLayer.animateOverlayOpacityFromOwnerState();
            return;
        }
        if (control.getStyleClass().contains(DATE_PICKER_DAY_CELL_STYLE_CLASS)) {
            Background background = control.getBackground();
            double inset = 0.0;
            if (background != null && !background.getFills().isEmpty()) {
                javafx.geometry.Insets fillInsets =
                        background.getFills().get(background.getFills().size() - 1).getInsets();
                inset = Math.max(
                        Math.max(fillInsets.getTop(), fillInsets.getRight()),
                        Math.max(fillInsets.getBottom(), fillInsets.getLeft())
                );
            }
            double targetWidth = Math.max(0.0, width - 2.0 * inset);
            double targetHeight = Math.max(0.0, height - 2.0 * inset);
            stateLayer.layoutLayer(
                    (width - targetWidth) / 2.0,
                    (height - targetHeight) / 2.0,
                    targetWidth,
                    targetHeight,
                    Math.min(targetWidth, targetHeight) / 2.0
            );
            stateLayer.animateOverlayOpacityFromOwnerState();
            return;
        }
        if (control instanceof M3ButtonBase button) {
            layoutCenteredButtonStateLayer(width, height, button.getContainerHeight());
            return;
        }
        stateLayer.layoutLayer(0.0, 0.0, width, height, stateLayerShapeRadius());
        stateLayer.animateOverlayOpacityFromOwnerState();
    }

    /// Centers ordinary button feedback inside the minimum interaction target.
    private void layoutCenteredButtonStateLayer(
            double availableWidth,
            double availableHeight,
            double containerHeight
    ) {
        double height = Math.min(availableHeight, containerHeight);
        stateLayer.layoutLayer(
                0.0,
                (availableHeight - height) / 2.0,
                availableWidth,
                height,
                stateLayerShapeRadius()
        );
        stateLayer.animateOverlayOpacityFromOwnerState();
    }

    /// Centers icon-button feedback inside its independent interaction target.
    private void layoutCenteredIconStateLayer(
            double availableWidth,
            double availableHeight,
            double containerWidth,
            double containerHeight
    ) {
        double width = Math.min(availableWidth, containerWidth);
        double height = Math.min(availableHeight, containerHeight);
        stateLayer.layoutLayer(
                (availableWidth - width) / 2.0,
                (availableHeight - height) / 2.0,
                width,
                height,
                stateLayerShapeRadius()
        );
        stateLayer.animateOverlayOpacityFromOwnerState();
    }

    /// Lays out segmented button feedback with position-specific corner radii.
    private void layoutSegmentedButtonStateLayer(M3SegmentedButton button, double width, double height) {
        double radius = button.getContainerShape();
        if (button.getStyleClass().contains(SINGLE_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius);
        } else if (button.getStyleClass().contains(FIRST_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius, 0.0, 0.0, radius);
        } else if (button.getStyleClass().contains(MIDDLE_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, 0.0);
        } else if (button.getStyleClass().contains(LAST_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, 0.0, radius, radius, 0.0);
        } else {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius);
        }
    }

    /// Lays out button feedback with button-group or split-button corner radii.
    private boolean layoutGroupedButtonStateLayer(ButtonBase button, double width, double height) {
        boolean groupedShape = button.getStyleClass().contains(SINGLE_BUTTON_STYLE_CLASS)
                || button.getStyleClass().contains(FIRST_BUTTON_STYLE_CLASS)
                || button.getStyleClass().contains(MIDDLE_BUTTON_STYLE_CLASS)
                || button.getStyleClass().contains(LAST_BUTTON_STYLE_CLASS)
                || button.getStyleClass().contains(SPLIT_ACTION_BUTTON_STYLE_CLASS)
                || button.getStyleClass().contains(SPLIT_MENU_BUTTON_STYLE_CLASS);
        if (!groupedShape) {
            return false;
        }

        double layerHeight = button instanceof M3ButtonBase m3Button
                ? Math.min(height, m3Button.getContainerHeight())
                : height;
        double layerY = (height - layerHeight) / 2.0;

        @Nullable M3SplitButton splitButton = splitButtonOwner(button);
        if (splitButton != null) {
            M3SplitButtonSkin.layoutPartStateLayer(
                    splitButton,
                    button,
                    stateLayer,
                    0.0,
                    layerY,
                    width,
                    layerHeight
            );
            return true;
        }

        @Nullable CornerRadii resolvedRadii = resolvedCornerRadii(button);
        @Nullable ContainerShapeTransition activeShapeTransition = activeContainerShapeTransition();
        if (activeShapeTransition != null) {
            stateLayer.layoutLayer(
                    0.0,
                    layerY,
                    width,
                    layerHeight,
                    activeShapeTransition.currentTopLeftRadius(),
                    activeShapeTransition.currentTopRightRadius(),
                    activeShapeTransition.currentBottomRightRadius(),
                    activeShapeTransition.currentBottomLeftRadius()
            );
            return true;
        }
        if (resolvedRadii != null) {
            stateLayer.layoutLayer(
                    0.0,
                    layerY,
                    width,
                    layerHeight,
                    resolvedRadii.getTopLeftHorizontalRadius(),
                    resolvedRadii.getTopRightHorizontalRadius(),
                    resolvedRadii.getBottomRightHorizontalRadius(),
                    resolvedRadii.getBottomLeftHorizontalRadius()
            );
            return true;
        }

        double radius = stateLayerShapeRadius();
        if (button.getStyleClass().contains(SINGLE_BUTTON_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, layerY, width, layerHeight, radius);
        } else if (button.getStyleClass().contains(FIRST_BUTTON_STYLE_CLASS)
                || button.getStyleClass().contains(SPLIT_ACTION_BUTTON_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, layerY, width, layerHeight, radius, 0.0, 0.0, radius);
        } else if (button.getStyleClass().contains(MIDDLE_BUTTON_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, layerY, width, layerHeight, 0.0);
        } else {
            stateLayer.layoutLayer(0.0, layerY, width, layerHeight, 0.0, radius, radius, 0.0);
        }
        return true;
    }

    /// Returns the split button that owns one internal button part.
    private static @Nullable M3SplitButton splitButtonOwner(ButtonBase button) {
        @Nullable Parent parent = button.getParent();
        while (parent != null) {
            if (parent instanceof M3SplitButton splitButton) {
                return splitButton;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns the CSS-resolved corner radii used by a grouped button surface.
    ///
    /// Transparent outlined backgrounds still expose their CSS radii. The border is used as a fallback for
    /// application styles that remove all background fills.
    private static @Nullable CornerRadii resolvedCornerRadii(ButtonBase button) {
        Background background = button.getBackground();
        if (background != null && !background.getFills().isEmpty()) {
            BackgroundFill fill = background.getFills().get(0);
            return fill.getRadii();
        }

        Border border = button.getBorder();
        if (border != null && !border.getStrokes().isEmpty()) {
            BorderStroke stroke = border.getStrokes().get(0);
            return stroke.getRadii();
        }
        return null;
    }

    /// Returns the shape radius used to clip state layer feedback.
    private double stateLayerShapeRadius() {
        C button = getSkinnable();
        @Nullable ContainerShapeTransition activeShapeTransition = activeContainerShapeTransition();
        if (activeShapeTransition != null) {
            return activeShapeTransition.currentTopLeftRadius();
        }
        if (button instanceof M3FloatingActionButton floatingActionButton) {
            return floatingActionButton.getContainerShape();
        }
        if (button instanceof M3ButtonBase m3Button) {
            return m3Button.getContainerShape();
        }
        if (button instanceof M3IconToggleButton iconToggleButton) {
            return iconToggleButton.getContainerShape();
        }
        if (button instanceof M3Chip chip) {
            return chip.getContainerShape();
        }
        return Math.min(button.getWidth(), button.getHeight()) / 2.0;
    }

    /// Returns the active shape transition, or `null` while CSS owns the rendered outline.
    private @Nullable ContainerShapeTransition activeContainerShapeTransition() {
        @Nullable ContainerShapeTransition transition = containerShapeTransition;
        return transition != null && transition.isSurfaceShapeActive() ? transition : null;
    }

    /// Animates the effective CSS corner radii through one retained region shape.
    @NotNullByDefault
    private final class ContainerShapeTransition extends org.glavo.m3fx.internal.M3FiniteTransition {
        /// The index of the top-left horizontal radius.
        private static final int TOP_LEFT_HORIZONTAL = 0;

        /// The index of the top-left vertical radius.
        private static final int TOP_LEFT_VERTICAL = 1;

        /// The index of the top-right horizontal radius.
        private static final int TOP_RIGHT_HORIZONTAL = 2;

        /// The index of the top-right vertical radius.
        private static final int TOP_RIGHT_VERTICAL = 3;

        /// The index of the bottom-right horizontal radius.
        private static final int BOTTOM_RIGHT_HORIZONTAL = 4;

        /// The index of the bottom-right vertical radius.
        private static final int BOTTOM_RIGHT_VERTICAL = 5;

        /// The index of the bottom-left horizontal radius.
        private static final int BOTTOM_LEFT_HORIZONTAL = 6;

        /// The index of the bottom-left vertical radius.
        private static final int BOTTOM_LEFT_VERTICAL = 7;

        /// The number of independently interpolated corner coordinates.
        private static final int RADIUS_COUNT = 8;

        /// The effective radii currently rendered by the surface shape.
        private final double[] currentRadii = new double[RADIUS_COUNT];

        /// The effective radii captured at the beginning of the current run.
        private final double[] startRadii = new double[RADIUS_COUNT];

        /// The effective radii requested at the end of the current run.
        private final double[] targetRadii = new double[RADIUS_COUNT];

        /// The CSS radii represented by the current target.
        private @Nullable CornerRadii cssTarget;

        /// The lazily allocated shape installed only while a morph is active.
        private @Nullable M3RoundedRectangleShape surfaceShape;

        /// The cached snapped insets that keep measurement independent of temporary Region-shape ownership.
        private @Nullable Insets stableInsets;

        /// The scale-shape value restored after a morph.
        private boolean previousScaleShape;

        /// The center-shape value restored after a morph.
        private boolean previousCenterShape;

        /// The cache-shape value restored after a morph.
        private boolean previousCacheShape;

        /// Creates a reusable container-shape transition at the supplied resting CSS radii.
        ///
        /// @param initialRadii the CSS radii rendered before the first animated shape change
        private ContainerShapeTransition(CornerRadii initialRadii) {
            setOnFinished(event -> releaseSurfaceShape());
            cssTarget = initialRadii;
            resolveRadii(initialRadii, controlWidth(), controlHeight(), currentRadii);
            System.arraycopy(currentRadii, 0, startRadii, 0, RADIUS_COUNT);
            System.arraycopy(currentRadii, 0, targetRadii, 0, RADIUS_COUNT);
        }

        /// Returns whether this transition currently owns the skinnable region shape.
        private boolean isSurfaceShapeActive() {
            @Nullable M3RoundedRectangleShape shape = surfaceShape;
            return shape != null && getSkinnable().getShape() == shape;
        }

        /// Returns whether measurements should preserve the insets used before this transition installed a shape.
        private boolean stabilizesRegionInsets() {
            @Nullable M3RoundedRectangleShape shape = surfaceShape;
            return shape != null
                    && (getSkinnable().getShape() == null || getSkinnable().getShape() == shape);
        }

        /// Returns cached snapped padding and border insets, rebuilding them only when their values change.
        private Insets stableRegionInsets() {
            C button = getSkinnable();
            Insets padding = button.getPadding();
            @Nullable Border border = button.getBorder();
            Insets borderInsets = border == null ? Insets.EMPTY : border.getInsets();
            double top = button.snapSpaceY(padding.getTop() + borderInsets.getTop());
            double right = button.snapSpaceX(padding.getRight() + borderInsets.getRight());
            double bottom = button.snapSpaceY(padding.getBottom() + borderInsets.getBottom());
            double left = button.snapSpaceX(padding.getLeft() + borderInsets.getLeft());

            @Nullable Insets cached = stableInsets;
            if (cached == null
                    || Double.compare(cached.getTop(), top) != 0
                    || Double.compare(cached.getRight(), right) != 0
                    || Double.compare(cached.getBottom(), bottom) != 0
                    || Double.compare(cached.getLeft(), left) != 0) {
                cached = new Insets(top, right, bottom, left);
                stableInsets = cached;
            }
            return cached;
        }

        /// Installs the retained shape without replacing an application- or owner-provided shape.
        private boolean installSurfaceShape() {
            if (isSurfaceShapeActive()) {
                return true;
            }

            C button = getSkinnable();
            if (button.getShape() != null) {
                return false;
            }

            M3RoundedRectangleShape shape = surfaceShape;
            if (shape == null) {
                shape = new M3RoundedRectangleShape();
                surfaceShape = shape;
            }
            previousScaleShape = button.isScaleShape();
            previousCenterShape = button.isCenterShape();
            previousCacheShape = button.isCacheShape();
            button.setShape(shape);
            button.setScaleShape(false);
            button.setCenterShape(false);
            button.setCacheShape(false);
            refreshGeometry();
            return true;
        }

        /// Removes this transition's temporary shape and restores region-shape flags.
        private void releaseSurfaceShape() {
            @Nullable M3RoundedRectangleShape shape = surfaceShape;
            C button = getSkinnable();
            if (shape == null || button.getShape() != shape) {
                return;
            }
            button.setShape(null);
            button.setScaleShape(previousScaleShape);
            button.setCenterShape(previousCenterShape);
            button.setCacheShape(previousCacheShape);
        }

        /// Settles immediately at the effective form of the supplied CSS radii.
        private void snapTo(CornerRadii radii) {
            stop();
            cssTarget = radii;
            resolveRadii(radii, controlWidth(), controlHeight(), currentRadii);
            System.arraycopy(currentRadii, 0, startRadii, 0, RADIUS_COUNT);
            System.arraycopy(currentRadii, 0, targetRadii, 0, RADIUS_COUNT);
            refreshGeometry();
            releaseSurfaceShape();
            layoutStateLayer();
        }

        /// Configures a run from the currently rendered radii to a new CSS target.
        private void configure(M3MotionSpec spec, CornerRadii radii) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            System.arraycopy(currentRadii, 0, startRadii, 0, RADIUS_COUNT);
            cssTarget = radii;
            resolveRadii(radii, controlWidth(), controlHeight(), targetRadii);
            refreshGeometry();
        }

        /// Updates the retained path after a layout or animation change.
        private void refreshGeometry() {
            @Nullable CornerRadii radii = cssTarget;
            if (radii == null) {
                return;
            }

            if (!isSurfaceShapeActive() && getStatus() != Animation.Status.RUNNING) {
                resolveRadii(radii, controlWidth(), controlHeight(), currentRadii);
            }
            @Nullable M3RoundedRectangleShape shape = surfaceShape;
            if (shape != null && getSkinnable().getShape() == shape) {
                shape.update(
                        controlWidth(),
                        controlHeight(),
                        currentRadii[TOP_LEFT_HORIZONTAL],
                        currentRadii[TOP_LEFT_VERTICAL],
                        currentRadii[TOP_RIGHT_HORIZONTAL],
                        currentRadii[TOP_RIGHT_VERTICAL],
                        currentRadii[BOTTOM_RIGHT_HORIZONTAL],
                        currentRadii[BOTTOM_RIGHT_VERTICAL],
                        currentRadii[BOTTOM_LEFT_HORIZONTAL],
                        currentRadii[BOTTOM_LEFT_VERTICAL]
                );
            }
        }

        /// Returns the effective control width used to resolve percentage and pill radii.
        private double controlWidth() {
            C button = getSkinnable();
            double width = button.getWidth();
            return width > 0.0 ? width : Math.max(0.0, button.getLayoutBounds().getWidth());
        }

        /// Returns the effective control height used to resolve percentage and pill radii.
        private double controlHeight() {
            C button = getSkinnable();
            double height = button.getHeight();
            return height > 0.0 ? height : Math.max(0.0, button.getLayoutBounds().getHeight());
        }

        /// Returns the circular radius used by the state layer at the top-left corner.
        private double currentTopLeftRadius() {
            return Math.min(currentRadii[TOP_LEFT_HORIZONTAL], currentRadii[TOP_LEFT_VERTICAL]);
        }

        /// Returns the circular radius used by the state layer at the top-right corner.
        private double currentTopRightRadius() {
            return Math.min(currentRadii[TOP_RIGHT_HORIZONTAL], currentRadii[TOP_RIGHT_VERTICAL]);
        }

        /// Returns the circular radius used by the state layer at the bottom-right corner.
        private double currentBottomRightRadius() {
            return Math.min(currentRadii[BOTTOM_RIGHT_HORIZONTAL], currentRadii[BOTTOM_RIGHT_VERTICAL]);
        }

        /// Returns the circular radius used by the state layer at the bottom-left corner.
        private double currentBottomLeftRadius() {
            return Math.min(currentRadii[BOTTOM_LEFT_HORIZONTAL], currentRadii[BOTTOM_LEFT_VERTICAL]);
        }

        /// Stops motion and releases the temporary region shape.
        private void dispose() {
            stop();
            releaseSurfaceShape();
            surfaceShape = null;
            stableInsets = null;
            cssTarget = null;
        }

        /// Applies one eased interpolation step without allocating geometry objects.
        @Override
        protected void interpolate(double fraction) {
            for (int index = 0; index < RADIUS_COUNT; index++) {
                currentRadii[index] = startRadii[index]
                        + (targetRadii[index] - startRadii[index]) * fraction;
            }
            refreshGeometry();
            layoutStateLayer();
        }

        /// Resolves JavaFX percentage radii and scales over-constrained corners to the supplied bounds.
        private static void resolveRadii(
                CornerRadii radii,
                double width,
                double height,
                double[] resolved
        ) {
            resolved[TOP_LEFT_HORIZONTAL] = resolveRadius(
                    radii.getTopLeftHorizontalRadius(),
                    radii.isTopLeftHorizontalRadiusAsPercentage(),
                    width
            );
            resolved[TOP_LEFT_VERTICAL] = resolveRadius(
                    radii.getTopLeftVerticalRadius(),
                    radii.isTopLeftVerticalRadiusAsPercentage(),
                    height
            );
            resolved[TOP_RIGHT_HORIZONTAL] = resolveRadius(
                    radii.getTopRightHorizontalRadius(),
                    radii.isTopRightHorizontalRadiusAsPercentage(),
                    width
            );
            resolved[TOP_RIGHT_VERTICAL] = resolveRadius(
                    radii.getTopRightVerticalRadius(),
                    radii.isTopRightVerticalRadiusAsPercentage(),
                    height
            );
            resolved[BOTTOM_RIGHT_HORIZONTAL] = resolveRadius(
                    radii.getBottomRightHorizontalRadius(),
                    radii.isBottomRightHorizontalRadiusAsPercentage(),
                    width
            );
            resolved[BOTTOM_RIGHT_VERTICAL] = resolveRadius(
                    radii.getBottomRightVerticalRadius(),
                    radii.isBottomRightVerticalRadiusAsPercentage(),
                    height
            );
            resolved[BOTTOM_LEFT_HORIZONTAL] = resolveRadius(
                    radii.getBottomLeftHorizontalRadius(),
                    radii.isBottomLeftHorizontalRadiusAsPercentage(),
                    width
            );
            resolved[BOTTOM_LEFT_VERTICAL] = resolveRadius(
                    radii.getBottomLeftVerticalRadius(),
                    radii.isBottomLeftVerticalRadiusAsPercentage(),
                    height
            );

            double scale = 1.0;
            scale = constrainedScale(scale, width,
                    resolved[TOP_LEFT_HORIZONTAL] + resolved[TOP_RIGHT_HORIZONTAL]);
            scale = constrainedScale(scale, width,
                    resolved[BOTTOM_LEFT_HORIZONTAL] + resolved[BOTTOM_RIGHT_HORIZONTAL]);
            scale = constrainedScale(scale, height,
                    resolved[TOP_LEFT_VERTICAL] + resolved[BOTTOM_LEFT_VERTICAL]);
            scale = constrainedScale(scale, height,
                    resolved[TOP_RIGHT_VERTICAL] + resolved[BOTTOM_RIGHT_VERTICAL]);
            if (scale < 1.0) {
                for (int index = 0; index < RADIUS_COUNT; index++) {
                    resolved[index] *= scale;
                }
            }
        }

        /// Resolves one absolute or percentage radius to a finite non-negative pixel value.
        private static double resolveRadius(double value, boolean percentage, double dimension) {
            double resolved = percentage ? value * dimension : value;
            return Double.isFinite(resolved) ? Math.max(0.0, resolved) : 0.0;
        }

        /// Narrows a shared corner scale when one pair exceeds its available dimension.
        private static double constrainedScale(double currentScale, double dimension, double radiusSum) {
            if (radiusSum <= 0.0 || radiusSum <= dimension) {
                return currentScale;
            }
            return Math.min(currentScale, Math.max(0.0, dimension) / radiusSum);
        }
    }
}
