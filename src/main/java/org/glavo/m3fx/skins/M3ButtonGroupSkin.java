// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3ButtonGroup].
@NotNullByDefault
public final class M3ButtonGroupSkin extends M3ItemContainerSkinBase<
        M3ButtonGroup,
        M3ButtonGroupSkin.ButtonGroupPane
> {
    /// Creates a button group skin.
    ///
    /// @param control the button group controlled by this skin
    public M3ButtonGroupSkin(M3ButtonGroup control) {
        super(control, control.getItems(), new ButtonGroupPane(control));
        getContainer().start();
    }

    /// Removes interaction listeners, motion observation, and child references before disposal.
    @Override
    public void dispose() {
        getContainer().dispose();
        super.dispose();
    }

    /// Lays out standard button groups with the Material Expressive activated-item width interaction.
    @NotNullByDefault
    static final class ButtonGroupPane extends Pane {
        /// The smallest width retained for a neighboring button during width redistribution.
        private static final double MINIMUM_INTERACTION_WIDTH = 24.0;

        /// The button group whose items are laid out by this pane.
        private final M3ButtonGroup control;

        /// The current activated-item expansion progress from resting 0 to pressed 1.
        private final DoubleProperty pressProgress = new SimpleDoubleProperty(this, "pressProgress") {
            /// Requests a child layout pulse when the animated expansion changes.
            @Override
            protected void invalidated() {
                requestLayout();
            }
        };

        /// The reusable transition that animates the activated-item width multiplier.
        private final M3DoubleTransition pressTransition = new M3DoubleTransition(pressProgress);

        /// Re-evaluates the activated item when a child armed state changes.
        private final InvalidationListener armedListener = observable -> updateArmedButton();

        /// Attaches and detaches the shared armed-state listener as group items change.
        private final ListChangeListener<Node> itemsListener = this::itemsChanged;

        /// Settles width motion when the group leaves the standard variant.
        private final ChangeListener<M3ButtonGroupVariant> variantListener =
                (observable, oldValue, newValue) -> updateArmedButton();

        /// Settles a running width transition when motion is disabled at runtime.
        private final M3MotionSettingsObserver motionSettingsObserver;

        /// The button whose width currently owns the expansion progress.
        private @Nullable ButtonBase activeButton;

        /// The target value of the current width transition.
        private double targetProgress;

        /// Whether listeners have been installed.
        private boolean started;

        /// Creates a layout pane for one button group.
        ///
        /// @param control the button group whose items are laid out
        ButtonGroupPane(M3ButtonGroup control) {
            this.control = control;
            pressTransition.setOnFinished(event -> finishPressTransition());
            motionSettingsObserver = new M3MotionSettingsObserver(
                    control,
                    () -> M3Animation.finishRunningAnimationsIfDisabled(control, pressTransition)
            );
        }

        /// Starts observing current and future button children.
        void start() {
            if (started) {
                return;
            }
            started = true;
            for (Node item : control.getItems()) {
                attachArmedListener(item);
            }
            control.getItems().addListener(itemsListener);
            control.variantProperty().addListener(variantListener);
            updateArmedButton();
        }

        /// Removes all listeners and stops the reusable transition.
        void dispose() {
            if (!started) {
                motionSettingsObserver.dispose();
                pressTransition.stop();
                pressTransition.setOnFinished(null);
                return;
            }

            started = false;
            control.getItems().removeListener(itemsListener);
            control.variantProperty().removeListener(variantListener);
            for (Node item : control.getItems()) {
                detachArmedListener(item);
            }
            motionSettingsObserver.dispose();
            pressTransition.stop();
            pressTransition.setOnFinished(null);
            activeButton = null;
            pressProgress.set(0.0);
        }

        /// Returns the minimum width needed by managed children and spacing.
        @Override
        protected double computeMinWidth(double height) {
            return summedChildWidth(height, WidthMetric.MINIMUM);
        }

        /// Returns the preferred width needed by managed children and spacing.
        @Override
        protected double computePrefWidth(double height) {
            return summedChildWidth(height, WidthMetric.PREFERRED);
        }

        /// Returns the preferred content width as the default maximum width.
        @Override
        protected double computeMaxWidth(double height) {
            return computePrefWidth(height);
        }

        /// Returns the largest minimum child height.
        @Override
        protected double computeMinHeight(double width) {
            return maximumChildHeight(width, HeightMetric.MINIMUM);
        }

        /// Returns the largest preferred child height.
        @Override
        protected double computePrefHeight(double width) {
            return maximumChildHeight(width, HeightMetric.PREFERRED);
        }

        /// Returns the preferred content height as the default maximum height.
        @Override
        protected double computeMaxHeight(double width) {
            return computePrefHeight(width);
        }

        /// Lays out children in logical order and redistributes width around the activated standard-group item.
        @Override
        protected void layoutChildren() {
            ObservableList<Node> children = getChildren();
            int managedCount = managedChildCount(children);
            if (managedCount == 0) {
                return;
            }

            double width = getWidth();
            double height = getHeight();
            double spacing = control.getSpacing();
            double totalSpacing = spacing * Math.max(0, managedCount - 1);
            double preferredChildrenWidth = summedManagedWidths(children, height, WidthMetric.PREFERRED);
            double minimumChildrenWidth = summedManagedWidths(children, height, WidthMetric.MINIMUM);
            double availableChildrenWidth = Math.max(0.0, width - totalSpacing);
            double shrinkRatio = shrinkRatio(
                    preferredChildrenWidth,
                    minimumChildrenWidth,
                    availableChildrenWidth
            );
            double contentWidth = fittedChildrenWidth(
                    preferredChildrenWidth,
                    minimumChildrenWidth,
                    shrinkRatio
            ) + totalSpacing;

            int activeIndex = activeChildIndex(children);
            int previousIndex = previousManagedIndex(children, activeIndex);
            int nextIndex = nextManagedIndex(children, activeIndex);
            double previousReduction = 0.0;
            double nextReduction = 0.0;
            double activeGrowth = 0.0;
            if (activeIndex >= 0 && pressProgress.get() > 0.0) {
                double activeWidth = fittedWidth(children.get(activeIndex), height, shrinkRatio);
                double requestedGrowth = activeWidth
                        * control.getStandardPressedWidthMultiplier()
                        * pressProgress.get();
                double previousCapacity = reductionCapacity(children, previousIndex, height, shrinkRatio);
                double nextCapacity = reductionCapacity(children, nextIndex, height, shrinkRatio);
                previousReduction = Math.min(requestedGrowth / 2.0, previousCapacity);
                nextReduction = Math.min(requestedGrowth / 2.0, nextCapacity);
                double remainder = requestedGrowth - previousReduction - nextReduction;
                if (remainder > 0.0) {
                    double additionalPrevious = Math.min(remainder, previousCapacity - previousReduction);
                    previousReduction += additionalPrevious;
                    remainder -= additionalPrevious;
                    nextReduction += Math.min(remainder, nextCapacity - nextReduction);
                }
                activeGrowth = previousReduction + nextReduction;
            }

            boolean rightToLeft = getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
            double x = rightToLeft ? Math.max(0.0, width - contentWidth) : 0.0;
            int start = rightToLeft ? children.size() - 1 : 0;
            int end = rightToLeft ? -1 : children.size();
            int step = rightToLeft ? -1 : 1;
            int laidOutCount = 0;
            for (int index = start; index != end; index += step) {
                Node child = children.get(index);
                if (!child.isManaged()) {
                    continue;
                }

                double childWidth = fittedWidth(child, height, shrinkRatio);
                if (index == activeIndex) {
                    childWidth += activeGrowth;
                } else if (index == previousIndex) {
                    childWidth -= previousReduction;
                } else if (index == nextIndex) {
                    childWidth -= nextReduction;
                }

                double childHeight = fittedHeight(child, childWidth, height);
                double childY = Math.max(0.0, (height - childHeight) / 2.0);
                if (child.isResizable()) {
                    child.resizeRelocate(x, childY, childWidth, childHeight);
                } else {
                    child.relocate(
                            x - child.getLayoutBounds().getMinX(),
                            childY - child.getLayoutBounds().getMinY()
                    );
                }

                x += childWidth;
                if (++laidOutCount < managedCount) {
                    x += spacing;
                }
            }
        }

        /// Updates armed-state listeners after the public item list changes.
        private void itemsChanged(ListChangeListener.Change<? extends Node> change) {
            boolean activeRemoved = false;
            while (change.next()) {
                for (Node removed : change.getRemoved()) {
                    detachArmedListener(removed);
                    activeRemoved |= removed == activeButton;
                }
                for (Node added : change.getAddedSubList()) {
                    attachArmedListener(added);
                }
            }

            if (activeRemoved) {
                settlePressInteraction();
            } else {
                updateArmedButton();
            }
            requestLayout();
        }

        /// Attaches the shared armed-state listener to a supported button child.
        private void attachArmedListener(Node item) {
            if (item instanceof ButtonBase button) {
                button.armedProperty().addListener(armedListener);
            }
        }

        /// Detaches the shared armed-state listener from a supported button child.
        private void detachArmedListener(Node item) {
            if (item instanceof ButtonBase button) {
                button.armedProperty().removeListener(armedListener);
            }
        }

        /// Starts expansion for the currently armed standard-group child or releases the previous child.
        private void updateArmedButton() {
            @Nullable ButtonBase armedButton = control.getVariant() == M3ButtonGroupVariant.STANDARD
                    ? findArmedButton()
                    : null;
            if (armedButton != null && armedButton != activeButton) {
                pressTransition.stop();
                pressProgress.set(0.0);
                activeButton = armedButton;
                targetProgress = 0.0;
            }

            if (armedButton != null) {
                animatePressProgress(1.0);
            } else if (activeButton != null) {
                animatePressProgress(0.0);
            } else {
                settlePressInteraction();
            }
        }

        /// Returns the first armed, enabled button child.
        private @Nullable ButtonBase findArmedButton() {
            for (Node item : control.getItems()) {
                if (item instanceof ButtonBase button && button.isArmed() && !button.isDisabled()) {
                    return button;
                }
            }
            return null;
        }

        /// Animates the shared width progress toward the requested interaction state.
        private void animatePressProgress(double target) {
            if (Double.compare(targetProgress, target) == 0
                    && pressTransition.getStatus() == Animation.Status.RUNNING) {
                return;
            }
            if (Double.compare(pressProgress.get(), target) == 0) {
                targetProgress = target;
                finishPressTransition();
                return;
            }

            targetProgress = target;
            pressTransition.configure(M3Animation.fastSpatial(control), target);
            M3Animation.playFromStart(control, pressTransition);
        }

        /// Clears the active child after the release transition reaches rest.
        private void finishPressTransition() {
            if (Double.compare(targetProgress, 0.0) == 0
                    && Double.compare(pressProgress.get(), 0.0) == 0) {
                activeButton = null;
            }
            requestLayout();
        }

        /// Stops width motion and restores every child to its resting width.
        private void settlePressInteraction() {
            pressTransition.stop();
            targetProgress = 0.0;
            pressProgress.set(0.0);
            activeButton = null;
        }

        /// Returns the active child index when it still belongs to this pane.
        private int activeChildIndex(ObservableList<Node> children) {
            ButtonBase current = activeButton;
            if (current == null || control.getVariant() != M3ButtonGroupVariant.STANDARD) {
                return -1;
            }
            int index = children.indexOf(current);
            return index >= 0 && current.isManaged() ? index : -1;
        }

        /// Returns the previous managed child index.
        private static int previousManagedIndex(ObservableList<Node> children, int index) {
            for (int candidate = index - 1; candidate >= 0; candidate--) {
                if (children.get(candidate).isManaged()) {
                    return candidate;
                }
            }
            return -1;
        }

        /// Returns the next managed child index.
        private static int nextManagedIndex(ObservableList<Node> children, int index) {
            for (int candidate = index + 1; candidate < children.size(); candidate++) {
                if (children.get(candidate).isManaged()) {
                    return candidate;
                }
            }
            return -1;
        }

        /// Returns how much width a neighboring child can yield without becoming unusably narrow.
        private static double reductionCapacity(
                ObservableList<Node> children,
                int index,
                double height,
                double shrinkRatio
        ) {
            if (index < 0) {
                return 0.0;
            }
            double width = fittedWidth(children.get(index), height, shrinkRatio);
            return Math.max(0.0, width - MINIMUM_INTERACTION_WIDTH);
        }

        /// Returns the number of managed children.
        private static int managedChildCount(ObservableList<Node> children) {
            int count = 0;
            for (Node child : children) {
                if (child.isManaged()) {
                    count++;
                }
            }
            return count;
        }

        /// Returns the sum of child widths plus current spacing.
        private double summedChildWidth(double height, WidthMetric metric) {
            ObservableList<Node> children = getChildren();
            int managedCount = managedChildCount(children);
            return summedManagedWidths(children, height, metric)
                    + control.getSpacing() * Math.max(0, managedCount - 1);
        }

        /// Returns the sum of one width metric across managed children.
        private static double summedManagedWidths(
                ObservableList<Node> children,
                double height,
                WidthMetric metric
        ) {
            double width = 0.0;
            for (Node child : children) {
                if (child.isManaged()) {
                    width += metric.width(child, height);
                }
            }
            return width;
        }

        /// Returns the maximum child height for one sizing metric.
        private double maximumChildHeight(double width, HeightMetric metric) {
            double height = 0.0;
            for (Node child : getChildren()) {
                if (child.isManaged()) {
                    height = Math.max(height, metric.height(child, width));
                }
            }
            return height;
        }

        /// Returns the proportional compression needed to fit preferred children into available width.
        private static double shrinkRatio(
                double preferredWidth,
                double minimumWidth,
                double availableWidth
        ) {
            double capacity = preferredWidth - minimumWidth;
            if (capacity <= 0.0 || availableWidth >= preferredWidth) {
                return 0.0;
            }
            return Math.min(1.0, Math.max(0.0, (preferredWidth - availableWidth) / capacity));
        }

        /// Returns the sum of fitted child widths after proportional compression.
        private static double fittedChildrenWidth(
                double preferredWidth,
                double minimumWidth,
                double shrinkRatio
        ) {
            return preferredWidth - (preferredWidth - minimumWidth) * shrinkRatio;
        }

        /// Returns one child's width after proportional compression.
        private static double fittedWidth(Node child, double height, double shrinkRatio) {
            double preferred = WidthMetric.PREFERRED.width(child, height);
            double minimum = WidthMetric.MINIMUM.width(child, height);
            return preferred - (preferred - minimum) * shrinkRatio;
        }

        /// Returns one child height bounded by its sizing contract and available height.
        private static double fittedHeight(Node child, double width, double availableHeight) {
            if (child instanceof Region region) {
                double minimum = region.minHeight(width);
                double preferred = region.prefHeight(width);
                double maximum = region.maxHeight(width);
                return Math.max(minimum, Math.min(Math.min(preferred, maximum), availableHeight));
            }
            return Math.min(child.getLayoutBounds().getHeight(), availableHeight);
        }

        /// Supplies one child width metric without allocation during layout.
        @NotNullByDefault
        private enum WidthMetric {
            /// The minimum width metric.
            MINIMUM {
                /// Returns a child's minimum width.
                @Override
                double width(Node child, double height) {
                    return child instanceof Region region
                            ? region.minWidth(height)
                            : child.getLayoutBounds().getWidth();
                }
            },

            /// The preferred width metric.
            PREFERRED {
                /// Returns a child's preferred width.
                @Override
                double width(Node child, double height) {
                    if (!(child instanceof Region region)) {
                        return child.getLayoutBounds().getWidth();
                    }
                    double minimum = region.minWidth(height);
                    double preferred = region.prefWidth(height);
                    double maximum = region.maxWidth(height);
                    return Math.max(minimum, Math.min(preferred, maximum));
                }
            };

            /// Returns the requested child width.
            abstract double width(Node child, double height);
        }

        /// Supplies one child height metric without allocation during layout.
        @NotNullByDefault
        private enum HeightMetric {
            /// The minimum height metric.
            MINIMUM {
                /// Returns a child's minimum height.
                @Override
                double height(Node child, double width) {
                    return child instanceof Region region
                            ? region.minHeight(width)
                            : child.getLayoutBounds().getHeight();
                }
            },

            /// The preferred height metric.
            PREFERRED {
                /// Returns a child's preferred height.
                @Override
                double height(Node child, double width) {
                    if (!(child instanceof Region region)) {
                        return child.getLayoutBounds().getHeight();
                    }
                    double minimum = region.minHeight(width);
                    double preferred = region.prefHeight(width);
                    double maximum = region.maxHeight(width);
                    return Math.max(minimum, Math.min(preferred, maximum));
                }
            };

            /// Returns the requested child height.
            abstract double height(Node child, double width);
        }
    }
}