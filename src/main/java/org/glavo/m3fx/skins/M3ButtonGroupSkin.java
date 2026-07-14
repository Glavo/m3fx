// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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

        /// The minimum accessible item width for extra-small and small connected groups.
        private static final double COMPACT_CONNECTED_ITEM_WIDTH = 48.0;

        /// Identifies selectable grouped buttons without depending on one concrete toggle-button implementation.
        private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

        /// Shared empty node storage used before startup and after disposal.
        private static final Node[] EMPTY_ITEMS = new Node[0];

        /// Shared empty numeric storage used before startup and after disposal.
        private static final double[] EMPTY_VALUES = new double[0];

        /// The button group whose items are laid out by this pane.
        private final M3ButtonGroup control;

        /// The reusable transition that animates every selected or activated item without per-item timelines.
        private final ButtonInteractionTransition interactionTransition = new ButtonInteractionTransition();

        /// Re-evaluates interaction targets when a child armed state changes.
        private final InvalidationListener armedListener = observable -> updateInteractionTargets();

        /// Re-evaluates interaction targets only when a child's selected pseudo-class changes.
        private final SetChangeListener<PseudoClass> pseudoClassListener = change -> {
            if (SELECTED_PSEUDO_CLASS.equals(change.getElementAdded())
                    || SELECTED_PSEUDO_CLASS.equals(change.getElementRemoved())) {
                updateInteractionTargets();
            }
        };

        /// Attaches and detaches shared interaction listeners as group items change.
        private final ListChangeListener<Node> itemsListener = this::itemsChanged;

        /// Retargets width motion when the group changes visual variant.
        private final ChangeListener<M3ButtonGroupVariant> variantListener =
                (observable, oldValue, newValue) -> updateInteractionTargets();


        /// Item identities aligned with the interaction arrays.
        private Node[] interactionItems = EMPTY_ITEMS;

        /// Current expansion progress for every group item.
        private double[] interactionProgress = EMPTY_VALUES;

        /// Expansion progress captured when the current transition starts.
        private double[] interactionStart = EMPTY_VALUES;

        /// Expansion targets for selected or activated items.
        private double[] interactionTarget = EMPTY_VALUES;

        /// Reusable width deltas accumulated during one layout pass.
        private double[] widthAdjustments = EMPTY_VALUES;

        /// Whether listeners have been installed.
        private boolean started;

        /// Creates a layout pane for one button group.
        ///
        /// @param control the button group whose items are laid out
        ButtonGroupPane(M3ButtonGroup control) {
            this.control = control;
        }

        /// Starts observing current and future button children.
        void start() {
            if (started) {
                return;
            }
            started = true;
            for (Node item : control.getItems()) {
                attachInteractionListeners(item);
            }
            control.getItems().addListener(itemsListener);
            control.variantProperty().addListener(variantListener);
            rebuildInteractionState();
            initializeInteractionTargets();
        }

        /// Removes all listeners and stops the reusable transition.
        void dispose() {
            if (!started) {
                interactionTransition.stop();
                return;
            }

            started = false;
            control.getItems().removeListener(itemsListener);
            control.variantProperty().removeListener(variantListener);
            for (Node item : control.getItems()) {
                detachInteractionListeners(item);
            }
            interactionTransition.stop();
            interactionItems = EMPTY_ITEMS;
            interactionProgress = EMPTY_VALUES;
            interactionStart = EMPTY_VALUES;
            interactionTarget = EMPTY_VALUES;
            widthAdjustments = EMPTY_VALUES;
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

        /// Returns an unbounded width for connected groups and a content-hugging width for standard groups.
        @Override
        protected double computeMaxWidth(double height) {
            return control.getVariant() == M3ButtonGroupVariant.CONNECTED
                    ? Double.MAX_VALUE
                    : computePrefWidth(height);
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
            double connectedGrowth = control.getVariant() == M3ButtonGroupVariant.CONNECTED
                    ? Math.max(0.0, availableChildrenWidth - preferredChildrenWidth) / managedCount
                    : 0.0;
            double adjustmentScale = prepareWidthAdjustments(children, height, shrinkRatio);

            // The pane inherits the control's orientation, so JavaFX mirrors these logical coordinates for RTL.
            double x = 0.0;
            int laidOutCount = 0;
            for (int index = 0; index < children.size(); index++) {
                Node child = children.get(index);
                if (!child.isManaged()) {
                    continue;
                }

                double childWidth = fittedWidth(child, height, shrinkRatio) + connectedGrowth;
                if (index < widthAdjustments.length) {
                    childWidth += widthAdjustments[index] * adjustmentScale;
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

        /// Updates interaction listeners and state storage after the public item list changes.
        private void itemsChanged(ListChangeListener.Change<? extends Node> change) {
            while (change.next()) {
                for (Node removed : change.getRemoved()) {
                    detachInteractionListeners(removed);
                }
                for (Node added : change.getAddedSubList()) {
                    attachInteractionListeners(added);
                }
            }

            rebuildInteractionState();
            updateInteractionTargets();
            requestLayout();
        }

        /// Attaches shared armed and selected-state listeners to a supported button child.
        private void attachInteractionListeners(Node item) {
            if (item instanceof ButtonBase button) {
                button.armedProperty().addListener(armedListener);
                button.getPseudoClassStates().addListener(pseudoClassListener);
            }
        }

        /// Detaches shared armed and selected-state listeners from a supported button child.
        private void detachInteractionListeners(Node item) {
            if (item instanceof ButtonBase button) {
                button.armedProperty().removeListener(armedListener);
                button.getPseudoClassStates().removeListener(pseudoClassListener);
            }
        }

        /// Preserves current interaction values while realigning storage with the public item list.
        private void rebuildInteractionState() {
            interactionTransition.stop();
            ObservableList<Node> items = control.getItems();
            int itemCount = items.size();
            Node[] oldItems = interactionItems;
            double[] oldProgress = interactionProgress;
            Node[] newItems = new Node[itemCount];
            double[] newProgress = new double[itemCount];

            for (int index = 0; index < itemCount; index++) {
                Node item = items.get(index);
                newItems[index] = item;
                for (int oldIndex = 0; oldIndex < oldItems.length; oldIndex++) {
                    if (oldItems[oldIndex] == item) {
                        newProgress[index] = oldProgress[oldIndex];
                        break;
                    }
                }
            }

            interactionItems = newItems;
            interactionProgress = newProgress;
            interactionStart = new double[itemCount];
            interactionTarget = new double[itemCount];
            widthAdjustments = new double[itemCount];
        }

        /// Applies initial selection and activation targets without playing an entrance animation.
        private void initializeInteractionTargets() {
            boolean standard = control.getVariant() == M3ButtonGroupVariant.STANDARD;
            @Nullable ButtonBase armedButton = standard ? findArmedButton() : null;
            for (int index = 0; index < interactionItems.length; index++) {
                Node item = interactionItems[index];
                double target = standard && (isSelected(item) || item == armedButton) ? 1.0 : 0.0;
                interactionProgress[index] = target;
                interactionStart[index] = target;
                interactionTarget[index] = target;
            }
            requestLayout();
        }

        /// Retargets the shared transition for every selected or activated standard-group item.
        private void updateInteractionTargets() {
            if (!started) {
                return;
            }
            if (interactionItems.length != control.getItems().size()) {
                rebuildInteractionState();
            }

            boolean standard = control.getVariant() == M3ButtonGroupVariant.STANDARD;
            @Nullable ButtonBase armedButton = standard ? findArmedButton() : null;
            boolean changed = false;
            for (int index = 0; index < interactionItems.length; index++) {
                Node item = interactionItems[index];
                double target = standard && (isSelected(item) || item == armedButton) ? 1.0 : 0.0;
                interactionStart[index] = interactionProgress[index];
                changed |= Double.compare(interactionTarget[index], target) != 0;
                interactionTarget[index] = target;
            }

            if (!changed) {
                requestLayout();
                return;
            }

            interactionTransition.configure(M3Animation.fastSpatial(control));
            M3Animation.playFromStart(control, interactionTransition);
        }

        /// Returns whether an item exposes the standard selected pseudo-class.
        private static boolean isSelected(Node item) {
            return item instanceof ButtonBase button
                    && button.getPseudoClassStates().contains(SELECTED_PSEUDO_CLASS);
        }

        /// Returns the first enabled armed button in logical item order.
        private @Nullable ButtonBase findArmedButton() {
            for (Node item : interactionItems) {
                if (item instanceof ButtonBase button && button.isArmed() && !button.isDisabled()) {
                    return button;
                }
            }
            return null;
        }

        /// Accumulates symmetric width transfers and returns a scale that preserves every child minimum width.
        private double prepareWidthAdjustments(
                ObservableList<Node> children,
                double height,
                double shrinkRatio
        ) {
            if (widthAdjustments.length != children.size()) {
                rebuildInteractionState();
            }
            Arrays.fill(widthAdjustments, 0.0);
            if (control.getVariant() != M3ButtonGroupVariant.STANDARD) {
                return 0.0;
            }

            double multiplier = control.getStandardPressedWidthMultiplier();
            for (int index = 0; index < children.size(); index++) {
                Node child = children.get(index);
                double progress = index < interactionProgress.length ? interactionProgress[index] : 0.0;
                if (!child.isManaged() || progress <= 0.0) {
                    continue;
                }

                int previousIndex = previousManagedIndex(children, index);
                int nextIndex = nextManagedIndex(children, index);
                if (previousIndex < 0 && nextIndex < 0) {
                    continue;
                }

                double requestedGrowth = fittedWidth(child, height, shrinkRatio) * multiplier * progress;
                widthAdjustments[index] += requestedGrowth;
                if (previousIndex >= 0 && nextIndex >= 0) {
                    widthAdjustments[previousIndex] -= requestedGrowth / 2.0;
                    widthAdjustments[nextIndex] -= requestedGrowth / 2.0;
                } else if (previousIndex >= 0) {
                    widthAdjustments[previousIndex] -= requestedGrowth;
                } else {
                    widthAdjustments[nextIndex] -= requestedGrowth;
                }
            }

            double scale = 1.0;
            for (int index = 0; index < children.size(); index++) {
                double adjustment = widthAdjustments[index];
                if (adjustment >= 0.0) {
                    continue;
                }
                Node child = children.get(index);
                double fittedWidth = fittedWidth(child, height, shrinkRatio);
                scale = Math.min(
                        scale,
                        Math.max(0.0, (fittedWidth - MINIMUM_INTERACTION_WIDTH) / -adjustment)
                );
            }
            return scale;
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
        private double summedManagedWidths(
                ObservableList<Node> children,
                double height,
                WidthMetric metric
        ) {
            double width = 0.0;
            for (Node child : children) {
                if (child.isManaged()) {
                    width += resolvedWidth(child, height, metric);
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
        private double fittedWidth(Node child, double height, double shrinkRatio) {
            double preferred = resolvedWidth(child, height, WidthMetric.PREFERRED);
            double minimum = resolvedWidth(child, height, WidthMetric.MINIMUM);
            return preferred - (preferred - minimum) * shrinkRatio;
        }

        /// Returns one width metric after applying the compact connected-group target minimum.
        private double resolvedWidth(Node child, double height, WidthMetric metric) {
            double width = metric.width(child, height);
            if (control.getVariant() == M3ButtonGroupVariant.CONNECTED
                    && (control.getSize() == M3ButtonSize.EXTRA_SMALL
                    || control.getSize() == M3ButtonSize.SMALL)) {
                return Math.max(COMPACT_CONNECTED_ITEM_WIDTH, width);
            }
            return width;
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

        /// Animates all interaction progress values with one reusable pulse source.
        @NotNullByDefault
        private final class ButtonInteractionTransition extends M3FiniteTransition {
            /// Creates the shared button interaction transition.
            private ButtonInteractionTransition() {
            }

            /// Reconfigures motion from the current progress values to the latest targets.
            private void configure(M3MotionSpec spec) {
                stop();
                setCycleDuration(spec.duration());
                setInterpolator(spec.interpolator());
                System.arraycopy(interactionProgress, 0, interactionStart, 0, interactionProgress.length);
            }

            /// Applies one eased interaction frame without allocating per-item animation objects.
            @Override
            protected void interpolate(double fraction) {
                for (int index = 0; index < interactionProgress.length; index++) {
                    double start = interactionStart[index];
                    interactionProgress[index] = start + (interactionTarget[index] - start) * fraction;
                }
                requestLayout();
            }
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