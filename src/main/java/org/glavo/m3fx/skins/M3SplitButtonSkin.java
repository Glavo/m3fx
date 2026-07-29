// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonBase;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SplitButton].
///
/// The skin lays out the action and menu parts as one connected or gapped control according to the split-button
/// properties. Each part retains its own action, focus, and accessible item identity while coordinated corner-shape
/// transitions preserve the visual relationship between both parts.
@NotNullByDefault
public final class M3SplitButtonSkin extends SkinBase<M3SplitButton> {
    /// The trailing menu part style class.
    private static final String MENU_BUTTON_STYLE_CLASS = "m3-split-button-menu";

    /// The armed interaction pseudo-class used by both button parts.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The pointer-hover pseudo-class used by both button parts.
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");

    /// The keyboard-visible focus pseudo-class used by both button parts.
    private static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The pointer-pressed pseudo-class used by both button parts.
    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    /// The popup-visible pseudo-class used by the trailing button part.
    private static final PseudoClass SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("showing");

    /// The persistent trailing-button state-layer opacity while its menu is showing.
    private static final double SELECTED_STATE_LAYER_OPACITY = 0.10;

    /// The non-shrinking internal button container.
    private final Pane container = new Pane();

    /// The primary action part.
    private final M3Button actionButton;

    /// The trailing menu part.
    private final M3MenuButton menuButton;

    /// The reusable action-part surface shape.
    private final M3RoundedRectangleShape actionShape = new M3RoundedRectangleShape();

    /// The reusable menu-part surface shape.
    private final M3RoundedRectangleShape menuShape = new M3RoundedRectangleShape();

    /// The action-part inner-corner transition.
    private final PartShapeTransition actionShapeTransition;

    /// The menu-part inner-corner transition.
    private final PartShapeTransition menuShapeTransition;


    /// Refreshes both part shapes when one owner token changes.
    private final InvalidationListener shapeTokenInvalidation = observable -> synchronizePartShapes();

    /// Animates one part shape when an interaction pseudo-class changes.
    private final SetChangeListener<PseudoClass> partStateListener = change -> animatePartShapes();

    /// Creates a split button skin.
    ///
    /// @param control the split button controlled by this skin
    /// @throws IllegalStateException if the split button does not expose both of its button parts
    public M3SplitButtonSkin(M3SplitButton control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        Object actionPart = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0);
        Object menuPart = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1);
        if (!(actionPart instanceof M3Button resolvedActionButton)) {
            throw new IllegalStateException("Split button action part must be an M3Button");
        }
        if (!(menuPart instanceof M3MenuButton resolvedMenuButton)) {
            throw new IllegalStateException("Split button menu part must be an M3MenuButton");
        }
        actionButton = resolvedActionButton;
        menuButton = resolvedMenuButton;
        actionButton.setManaged(false);
        menuButton.setManaged(false);
        configurePartShape(actionButton, actionShape);
        configurePartShape(menuButton, menuShape);
        actionShapeTransition = new PartShapeTransition(actionButton, actionShape, false);
        menuShapeTransition = new PartShapeTransition(menuButton, menuShape, true);
        installShapeListeners(control);
        synchronizePartShapes();
        container.getChildren().setAll(actionButton, menuButton);
        getChildren().setAll(container);
    }

    /// Removes listeners, shape objects, and animations before disposal.
    @Override
    public void dispose() {
        actionShapeTransition.stop();
        menuShapeTransition.stop();
        uninstallShapeListeners(getSkinnable());
        resetPartShape(actionButton, actionShape);
        resetPartShape(menuButton, menuShape);
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Configures one internal region to render its background and outline through a mutable shape.
    private static void configurePartShape(M3ButtonBase button, M3RoundedRectangleShape surfaceShape) {
        button.setShape(surfaceShape);
        button.setScaleShape(false);
        button.setCenterShape(false);
        button.setCacheShape(false);
    }

    /// Removes the mutable shape installed by this skin.
    private static void resetPartShape(M3ButtonBase button, M3RoundedRectangleShape surfaceShape) {
        if (button.getShape() == surfaceShape) {
            button.setShape(null);
            button.setScaleShape(true);
            button.setCenterShape(true);
            button.setCacheShape(true);
        }
    }

    /// Installs listeners for owner shape tokens and part interaction states.
    private void installShapeListeners(M3SplitButton control) {
        control.outerCornerProperty().addListener(shapeTokenInvalidation);
        control.innerCornerProperty().addListener(shapeTokenInvalidation);
        control.hoveredInnerCornerProperty().addListener(shapeTokenInvalidation);
        control.pressedInnerCornerProperty().addListener(shapeTokenInvalidation);
        control.selectedInnerCornerProperty().addListener(shapeTokenInvalidation);
        actionButton.getPseudoClassStates().addListener(partStateListener);
        menuButton.getPseudoClassStates().addListener(partStateListener);
    }

    /// Removes listeners installed for owner shape tokens and part interaction states.
    private void uninstallShapeListeners(M3SplitButton control) {
        control.outerCornerProperty().removeListener(shapeTokenInvalidation);
        control.innerCornerProperty().removeListener(shapeTokenInvalidation);
        control.hoveredInnerCornerProperty().removeListener(shapeTokenInvalidation);
        control.pressedInnerCornerProperty().removeListener(shapeTokenInvalidation);
        control.selectedInnerCornerProperty().removeListener(shapeTokenInvalidation);
        actionButton.getPseudoClassStates().removeListener(partStateListener);
        menuButton.getPseudoClassStates().removeListener(partStateListener);
    }

    /// Settles both part shapes immediately after token or stylesheet changes.
    private void synchronizePartShapes() {
        M3SplitButton splitButton = getSkinnable();
        actionShapeTransition.snapTo(resolvedInnerCorner(splitButton, actionButton, false));
        menuShapeTransition.snapTo(resolvedInnerCorner(splitButton, menuButton, true));
        splitButton.requestLayout();
    }

    /// Animates both part shapes toward values resolved from their current interaction states.
    private void animatePartShapes() {
        M3SplitButton splitButton = getSkinnable();
        animatePartShape(
                actionShapeTransition,
                resolvedInnerCorner(splitButton, actionButton, false)
        );
        animatePartShape(
                menuShapeTransition,
                resolvedInnerCorner(splitButton, menuButton, true)
        );
    }

    /// Starts one reusable part-shape transition when its target has changed.
    private void animatePartShape(PartShapeTransition transition, double targetInnerCorner) {
        if (!transition.configure(M3Animation.fastSpatial(getSkinnable()), targetInnerCorner)) {
            return;
        }
        M3Animation.playFromStart(getSkinnable(), transition);
    }

    /// Resolves the active inner-corner radius for one button part.
    static double resolvedInnerCorner(
            M3SplitButton splitButton,
            ButtonBase button,
            boolean menuPart
    ) {
        if (isPressedLike(button)) {
            return splitButton.getPressedInnerCorner();
        }
        if (menuPart && isSelectedMenuPart(splitButton, button)) {
            return splitButton.getSelectedInnerCorner();
        }
        if (button.isHover()
                || button.getPseudoClassStates().contains(HOVER_PSEUDO_CLASS)
                || button.getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS)) {
            return splitButton.getHoveredInnerCorner();
        }
        return splitButton.getInnerCorner();
    }

    /// Returns whether a button part is in a pressed interaction state.
    private static boolean isPressedLike(ButtonBase button) {
        return button.isArmed()
                || button.isPressed()
                || button.getPseudoClassStates().contains(ARMED_PSEUDO_CLASS)
                || button.getPseudoClassStates().contains(PRESSED_PSEUDO_CLASS);
    }

    /// Returns whether the trailing menu part is selected because its popup is showing.
    private static boolean isSelectedMenuPart(M3SplitButton splitButton, ButtonBase button) {
        return splitButton.isShowing()
                || button.getPseudoClassStates().contains(SHOWING_PSEUDO_CLASS);
    }

    /// Lays out one part state layer from the same animated shape used by its rendered surface.
    static void layoutPartStateLayer(
            M3SplitButton splitButton,
            ButtonBase button,
            M3StateLayer stateLayer,
            double x,
            double y,
            double width,
            double height
    ) {
        boolean menuPart = button.getStyleClass().contains(MENU_BUTTON_STYLE_CLASS);
        double innerCorner = resolvedInnerCorner(splitButton, button, menuPart);
        if (splitButton.getSkin() instanceof M3SplitButtonSkin skin) {
            innerCorner = skin.currentInnerCorner(button, innerCorner);
        }

        stateLayer.setRestingOverlayOpacity(
                menuPart && isSelectedMenuPart(splitButton, button)
                        ? SELECTED_STATE_LAYER_OPACITY
                        : 0.0
        );
        double outerCorner = splitButton.getOuterCorner();
        if (menuPart) {
            stateLayer.layoutLayer(
                    x,
                    y,
                    width,
                    height,
                    innerCorner,
                    outerCorner,
                    outerCorner,
                    innerCorner
            );
        } else {
            stateLayer.layoutLayer(
                    x,
                    y,
                    width,
                    height,
                    outerCorner,
                    innerCorner,
                    innerCorner,
                    outerCorner
            );
        }
    }

    /// Returns the animated inner-corner radius for one internal part.
    private double currentInnerCorner(ButtonBase button, double fallback) {
        if (button == actionButton) {
            return actionShapeTransition.currentInnerCorner();
        }
        if (button == menuButton) {
            return menuShapeTransition.currentInnerCorner();
        }
        return fallback;
    }

    /// Computes the fixed minimum width required by both button parts and their between-space.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the minimum height required by the taller button part.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the width of both preferred button parts and the exact between-space.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset
                + actionButton.prefWidth(height)
                + getSkinnable().getSpacing()
                + menuButton.prefWidth(height)
                + rightInset;
    }

    /// Computes the preferred height of the taller button part.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset
                + Math.max(actionButton.prefHeight(-1.0), menuButton.prefHeight(-1.0))
                + bottomInset;
    }

    /// Computes the fixed maximum width used by this fixed-format control.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the fixed maximum height used by this fixed-format control.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Lays out both parts without shrinking them; JavaFX node orientation mirrors the container when needed.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
        double availableHeight = snapSizeY(height);
        double preferredHeight = snapSizeY(Math.max(
                actionButton.prefHeight(-1.0),
                menuButton.prefHeight(-1.0)
        ));
        double partHeight = Math.min(availableHeight, preferredHeight);
        double actionWidth = snapSizeX(actionButton.prefWidth(partHeight));
        double menuWidth = snapSizeX(menuButton.prefWidth(partHeight));
        actionButton.resize(actionWidth, partHeight);
        menuButton.resize(menuWidth, partHeight);
        actionShapeTransition.refreshGeometry();
        menuShapeTransition.refreshGeometry();

        double spacing = getSkinnable().getSpacing();
        double contentWidth = actionButton.getWidth() + spacing + menuButton.getWidth();
        double startX = snapPositionX(Math.max(0.0, (width - contentWidth) / 2.0));
        double partY = snapPositionY((height - partHeight) / 2.0);

        actionButton.setLayoutX(startX - actionButton.getLayoutBounds().getMinX());
        actionButton.setLayoutY(partY - actionButton.getLayoutBounds().getMinY());
        menuButton.setLayoutX(
                startX
                        + actionButton.getWidth()
                        + spacing
                        - menuButton.getLayoutBounds().getMinX()
        );
        menuButton.setLayoutY(partY - menuButton.getLayoutBounds().getMinY());
    }

    /// A reusable transition for one split-button part's inner corners.
    @NotNullByDefault
    private final class PartShapeTransition extends M3FiniteTransition {
        /// The button surface updated by the transition.
        private final M3ButtonBase button;

        /// The mutable shape installed on the button surface.
        private final M3RoundedRectangleShape shape;

        /// Whether this transition controls the trailing menu part.
        private final boolean menuPart;

        /// The current inner-corner radius.
        private double currentInnerCorner;

        /// The starting inner-corner radius for the current transition.
        private double startInnerCorner;

        /// The target inner-corner radius for the current transition.
        private double targetInnerCorner = Double.NaN;

        /// Creates a reusable inner-corner transition.
        private PartShapeTransition(
                M3ButtonBase button,
                M3RoundedRectangleShape shape,
                boolean menuPart
        ) {
            this.button = button;
            this.shape = shape;
            this.menuPart = menuPart;
        }

        /// Returns the current animated inner-corner radius.
        private double currentInnerCorner() {
            return currentInnerCorner;
        }

        /// Settles the shape immediately at one inner-corner radius.
        private void snapTo(double innerCorner) {
            stop();
            currentInnerCorner = innerCorner;
            startInnerCorner = innerCorner;
            targetInnerCorner = innerCorner;
            refreshGeometry();
            button.requestLayout();
        }

        /// Configures a transition from the currently rendered radius.
        private boolean configure(M3MotionSpec spec, double innerCorner) {
            if (Double.compare(targetInnerCorner, innerCorner) == 0
                    && (getStatus() == Animation.Status.RUNNING
                    || Double.compare(currentInnerCorner, innerCorner) == 0)) {
                return false;
            }
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            startInnerCorner = currentInnerCorner;
            targetInnerCorner = innerCorner;
            return true;
        }

        /// Updates the shape geometry after a size or outer-corner change.
        private void refreshGeometry() {
            double width = button.getWidth();
            double height = button.getHeight();
            double outerCorner = getSkinnable().getOuterCorner();
            if (menuPart) {
                shape.update(
                        width,
                        height,
                        currentInnerCorner,
                        outerCorner,
                        outerCorner,
                        currentInnerCorner
                );
            } else {
                shape.update(
                        width,
                        height,
                        outerCorner,
                        currentInnerCorner,
                        currentInnerCorner,
                        outerCorner
                );
            }
        }

        /// Applies the eased inner-corner radius without allocating pulse-local objects.
        @Override
        protected void interpolate(double fraction) {
            currentInnerCorner = startInnerCorner
                    + (targetInnerCorner - startInnerCorner) * fraction;
            refreshGeometry();
            button.requestLayout();
        }
    }

}
