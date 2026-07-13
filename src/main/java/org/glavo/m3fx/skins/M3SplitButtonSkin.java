// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3SplitButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SplitButton].
@NotNullByDefault
public final class M3SplitButtonSkin extends SkinBase<M3SplitButton> {
    /// The armed interaction pseudo-class used by both button parts.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The pointer-hover pseudo-class used by both button parts.
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");

    /// The pointer-pressed pseudo-class used by both button parts.
    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    /// The popup-visible pseudo-class used by the trailing button part.
    private static final PseudoClass SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("showing");

    /// The non-shrinking internal button container.
    private final Pane container = new Pane();

    /// The primary action part.
    private final M3Button actionButton;

    /// The trailing menu part.
    private final M3MenuButton menuButton;

    /// Refreshes both part shapes when one owner token changes.
    private final InvalidationListener shapeTokenInvalidation = observable -> updatePartShapeStyles();

    /// Refreshes one part shape when an interaction pseudo-class changes.
    private final SetChangeListener<PseudoClass> partStateListener = change -> updatePartShapeStyles();

    /// Creates a split button skin.
    ///
    /// @param control the split button controlled by this skin
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
        installShapeListeners(control);
        updatePartShapeStyles();
        container.getChildren().setAll(actionButton, menuButton);
        getChildren().setAll(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        uninstallShapeListeners(getSkinnable());
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
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

    /// Applies owner-controlled stateful corner radii to both internal button surfaces.
    private void updatePartShapeStyles() {
        M3SplitButton splitButton = getSkinnable();
        double outerCorner = splitButton.getOuterCorner();
        setPartShapeStyle(
                actionButton,
                outerCorner,
                resolvedInnerCorner(splitButton, actionButton, false),
                false
        );
        setPartShapeStyle(
                menuButton,
                outerCorner,
                resolvedInnerCorner(splitButton, menuButton, true),
                true
        );
    }

    /// Resolves the active inner-corner radius for one button part.
    private static double resolvedInnerCorner(
            M3SplitButton splitButton,
            M3Button button,
            boolean menuPart
    ) {
        if (menuPart
                && (splitButton.isShowing()
                || button.getPseudoClassStates().contains(SHOWING_PSEUDO_CLASS))) {
            return splitButton.getSelectedInnerCorner();
        }
        if (button.isArmed()
                || button.isPressed()
                || button.getPseudoClassStates().contains(ARMED_PSEUDO_CLASS)
                || button.getPseudoClassStates().contains(PRESSED_PSEUDO_CLASS)) {
            return splitButton.getPressedInnerCorner();
        }
        if (button.isHover() || button.getPseudoClassStates().contains(HOVER_PSEUDO_CLASS)) {
            return splitButton.getHoveredInnerCorner();
        }
        return splitButton.getInnerCorner();
    }

    /// Writes asymmetric surface and outline radii for one internal button part.
    private static void setPartShapeStyle(
            M3Button button,
            double outerCorner,
            double innerCorner,
            boolean menuPart
    ) {
        String outer = formatPixels(outerCorner);
        String inner = formatPixels(innerCorner);
        String radii = menuPart
                ? inner + " " + outer + " " + outer + " " + inner
                : outer + " " + inner + " " + inner + " " + outer;
        String style = "-fx-background-radius: " + radii + "; -fx-border-radius: " + radii + ";";
        if (!style.equals(button.getStyle())) {
            button.setStyle(style);
        }
    }

    /// Formats one finite CSS pixel value.
    private static String formatPixels(double value) {
        return Double.toString(value) + "px";
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
}