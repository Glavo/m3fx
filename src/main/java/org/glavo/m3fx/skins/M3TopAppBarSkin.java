// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3TopAppBar].
///
/// The skin keeps the 64-logical-pixel navigation and action row independent from the expanded flexible title area.
/// Flexible variants interpolate title presentation and bar height from the control's collapse progress; compact and
/// fixed variants omit the expanded title area.
@NotNullByDefault
public final class M3TopAppBarSkin extends SkinBase<M3TopAppBar> {
    /// The minimum Material hit slot used by top app bar navigation and trailing action icons.
    private static final double MINIMUM_ACTION_SLOT_SIZE = 48.0;

    /// The collapse progress at which the expanded title has completely faded.
    private static final double EXPANDED_TITLE_FADE_END = 0.60;

    /// The collapse progress at which the compact title begins to appear.
    private static final double COMPACT_TITLE_FADE_START = 0.25;

    /// The slot that hosts the optional navigation node.
    private final SlotPane navigationSlot = new SlotPane();

    /// The label that renders expanded and non-flexible title text.
    private final Label titleLabel = new Label();

    /// The label that renders expanded and small-app-bar subtitle text.
    private final Label subtitleLabel = new Label();

    /// The label that renders the compact title during flexible collapse.
    private final Label compactTitleLabel = new Label();

    /// The label that renders the compact subtitle during flexible collapse.
    private final Label compactSubtitleLabel = new Label();

    /// The trailing action node container.
    private final HBox actions = new HBox();

    /// The reusable transition that updates the control collapse-progress property.
    private final M3DoubleTransition collapseAnimation;

    /// The custom expanded title node currently installed by the control.
    private @Nullable Node titleContent;

    /// Updates the visual action list when public actions change.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Requests a second skin layout after a parent changes the control size in the current pulse.
    private final InvalidationListener sizeInvalidation = observable -> getSkinnable().requestLayout();

    /// Updates the navigation slot when the public node changes.
    private final ChangeListener<@Nullable Node> navigationListener =
            (observable, oldValue, newValue) -> updateNavigation(newValue);

    /// Replaces the expanded title node when custom title content changes.
    private final ChangeListener<@Nullable Node> titleContentListener =
            (observable, oldValue, newValue) -> updateTitleContent(oldValue, newValue);

    /// Updates geometry and collapse state when the app bar variant changes.
    private final InvalidationListener variantInvalidation = observable -> {
        updateVariantLayout();
        updateCollapseTarget(false);
    };

    /// Starts the flexible transformation when scroll-under state changes.
    private final InvalidationListener scrolledUnderInvalidation = observable -> updateCollapseTarget(true);

    /// Updates opacity and scale while direct scrolling or animation changes collapse progress.
    private final InvalidationListener collapseProgressInvalidation = observable -> updateTransitionVisuals();

    /// Updates subtitle visibility and layout after text changes.
    private final InvalidationListener subtitleInvalidation = observable -> updateVariantLayout();

    /// Creates a top app bar skin.
    ///
    /// @param control the top app bar controlled by this skin
    public M3TopAppBarSkin(M3TopAppBar control) {
        super(control);
        collapseAnimation = new M3DoubleTransition(
                control.collapseProgressProperty(),
                M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                0.0,
                1.0
        );

        navigationSlot.setManaged(false);
        titleLabel.setManaged(false);
        subtitleLabel.setManaged(false);
        compactTitleLabel.setManaged(false);
        compactSubtitleLabel.setManaged(false);
        actions.setManaged(false);

        actions.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        navigationSlot.getStyleClass().add(M3TopAppBar.NAVIGATION_STYLE_CLASS);
        titleLabel.getStyleClass().add(M3TopAppBar.TITLE_STYLE_CLASS);
        subtitleLabel.getStyleClass().add(M3TopAppBar.SUBTITLE_STYLE_CLASS);
        compactTitleLabel.getStyleClass().add(M3TopAppBar.COMPACT_TITLE_STYLE_CLASS);
        compactSubtitleLabel.getStyleClass().add(M3TopAppBar.COMPACT_SUBTITLE_STYLE_CLASS);
        actions.getStyleClass().add(M3TopAppBar.ACTIONS_STYLE_CLASS);

        actions.spacingProperty().bind(control.actionSpacingProperty());
        titleLabel.textProperty().bind(control.titleProperty());
        compactTitleLabel.textProperty().bind(control.titleProperty());
        subtitleLabel.textProperty().bind(control.subtitleProperty());
        compactSubtitleLabel.textProperty().bind(control.subtitleProperty());
        control.navigationProperty().addListener(navigationListener);
        control.titleContentProperty().addListener(titleContentListener);
        control.getActions().addListener(actionsListener);
        control.variantProperty().addListener(variantInvalidation);
        control.scrolledUnderProperty().addListener(scrolledUnderInvalidation);
        control.collapseProgressProperty().addListener(collapseProgressInvalidation);
        control.subtitleProperty().addListener(subtitleInvalidation);
        control.widthProperty().addListener(sizeInvalidation);
        control.heightProperty().addListener(sizeInvalidation);

        updateNavigation(control.getNavigation());
        updateActions();
        getChildren().addAll(
                navigationSlot,
                titleLabel,
                subtitleLabel,
                compactTitleLabel,
                compactSubtitleLabel,
                actions
        );
        updateTitleContent(null, control.getTitleContent());
        updateVariantLayout();
        updateCollapseTarget(false);
    }

    /// Removes listeners, bindings, animation, and child references before disposal.
    @Override
    public void dispose() {
        M3TopAppBar control = getSkinnable();
        collapseAnimation.stop();
        titleLabel.textProperty().unbind();
        compactTitleLabel.textProperty().unbind();
        subtitleLabel.textProperty().unbind();
        compactSubtitleLabel.textProperty().unbind();
        control.getActions().removeListener(actionsListener);
        control.navigationProperty().removeListener(navigationListener);
        control.titleContentProperty().removeListener(titleContentListener);
        control.variantProperty().removeListener(variantInvalidation);
        control.scrolledUnderProperty().removeListener(scrolledUnderInvalidation);
        control.collapseProgressProperty().removeListener(collapseProgressInvalidation);
        control.subtitleProperty().removeListener(subtitleInvalidation);
        control.widthProperty().removeListener(sizeInvalidation);
        control.heightProperty().removeListener(sizeInvalidation);
        actions.nodeOrientationProperty().unbind();
        actions.spacingProperty().unbind();
        clearActionSlots();
        navigationSlot.getChildren().clear();
        Node currentTitleContent = titleContent;
        if (currentTitleContent != null) {
            currentTitleContent.getStyleClass().remove(M3TopAppBar.TITLE_CONTENT_STYLE_CLASS);
        }
        titleContent = null;
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width required by navigation, title, and action slots.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + computeContentMinWidth(height) + rightInset;
    }

    /// Computes the minimum height from the current variant and collapse progress.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + computeVariantHeight() + bottomInset;
    }

    /// Computes the preferred width required by navigation, title, and action slots.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + computeContentMinWidth(height) + rightInset;
    }

    /// Computes the preferred height from the current variant and collapse progress.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + computeVariantHeight() + bottomInset;
    }

    /// Allows a top app bar to fill the available horizontal space.
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

    /// Restricts maximum height to the active app bar geometry.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + computeVariantHeight() + bottomInset;
    }

    /// Lays out the action row, expanded title area, and compact flexible title according to logical direction.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3TopAppBar control = getSkinnable();
        M3TopAppBarVariant variant = control.getVariant();
        double edgePadding = control.getEdgePadding();
        double navigationWidth = navigationSlot.isManaged() ? snappedPrefWidth(navigationSlot, height) : 0.0;
        double navigationHeight = navigationSlot.isManaged() ? snappedPrefHeight(navigationSlot, navigationWidth) : 0.0;
        double actionsWidth = snappedPrefWidth(actions, height);
        double actionsHeight = snappedPrefHeight(actions, actionsWidth);
        double rowHeight = Math.min(height, control.getContainerHeight());
        double navigationY = y + snappedOffset((rowHeight - navigationHeight) / 2.0);
        double actionsY = y + snappedOffset((rowHeight - actionsHeight) / 2.0);

        // JavaFX mirrors child coordinates for RTL parents, so slot positions remain in logical LTR space.
        if (navigationSlot.isManaged()) {
            navigationSlot.resizeRelocate(x + edgePadding, navigationY, navigationWidth, navigationHeight);
            navigationSlot.layout();
        }
        actions.resizeRelocate(x + width - edgePadding - actionsWidth, actionsY, actionsWidth, actionsHeight);
        actions.layout();

        switch (variant) {
            case CENTER_ALIGNED -> layoutCenterAlignedTitle(
                    x,
                    y,
                    width,
                    rowHeight,
                    navigationWidth + edgePadding,
                    actionsWidth + edgePadding
            );
            case MEDIUM, LARGE -> layoutBaselineTallTitle(x, y, width, height, variant);
            case MEDIUM_FLEXIBLE, LARGE_FLEXIBLE -> layoutFlexibleTitles(
                    x,
                    y,
                    width,
                    height,
                    rowHeight,
                    navigationWidth + edgePadding,
                    actionsWidth + edgePadding
            );
            case SMALL -> layoutSmallTitle(
                    x,
                    y,
                    width,
                    rowHeight,
                    navigationWidth + edgePadding,
                    actionsWidth + edgePadding,
                    expandedTitleNode(),
                    subtitleLabel
            );
        }
    }

    /// Updates the optional navigation slot.
    private void updateNavigation(@Nullable Node node) {
        navigationSlot.getChildren().clear();
        navigationSlot.setVisible(node != null);
        navigationSlot.setManaged(node != null);
        if (node != null) {
            navigationSlot.getChildren().add(node);
        }
    }

    /// Updates the trailing action container.
    private void updateActions() {
        clearActionSlots();
        for (Node action : getSkinnable().getActions()) {
            actions.getChildren().add(createActionSlot(action));
        }
        getSkinnable().requestLayout();
    }

    /// Removes child references from generated action slots before rebuilding or disposing the skin.
    private void clearActionSlots() {
        for (Node child : actions.getChildren()) {
            if (child instanceof SlotPane slot) {
                slot.getChildren().clear();
            }
        }
        actions.getChildren().clear();
    }

    /// Creates a fixed Material action slot for a public trailing action node.
    private static SlotPane createActionSlot(Node action) {
        SlotPane slot = new SlotPane();
        slot.getStyleClass().add(M3TopAppBar.ACTION_SLOT_STYLE_CLASS);
        slot.getChildren().add(action);
        return slot;
    }

    /// Replaces custom expanded title content without retaining the previous node.
    private void updateTitleContent(@Nullable Node oldValue, @Nullable Node newValue) {
        if (oldValue != null) {
            getChildren().remove(oldValue);
            oldValue.getStyleClass().remove(M3TopAppBar.TITLE_CONTENT_STYLE_CLASS);
        }
        titleContent = newValue;
        if (newValue != null) {
            if (!newValue.getStyleClass().contains(M3TopAppBar.TITLE_CONTENT_STYLE_CLASS)) {
                newValue.getStyleClass().add(M3TopAppBar.TITLE_CONTENT_STYLE_CLASS);
            }
            newValue.setManaged(false);
            if (!getChildren().contains(newValue)) {
                getChildren().add(newValue);
            }
        }
        updateTransitionVisuals();
        getSkinnable().requestLayout();
    }

    /// Updates label alignment, visibility, and layout details that depend on the active variant.
    private void updateVariantLayout() {
        M3TopAppBarVariant variant = getSkinnable().getVariant();
        boolean centerAligned = variant == M3TopAppBarVariant.CENTER_ALIGNED;
        boolean flexible = variant == M3TopAppBarVariant.MEDIUM_FLEXIBLE
                || variant == M3TopAppBarVariant.LARGE_FLEXIBLE;
        Pos titleAlignment = centerAligned ? Pos.CENTER : leadingTextAlignment();
        actions.setAlignment(Pos.CENTER_RIGHT);
        titleLabel.setWrapText(flexible);
        titleLabel.setAlignment(titleAlignment);
        subtitleLabel.setAlignment(titleAlignment);
        compactTitleLabel.setAlignment(leadingTextAlignment());
        compactSubtitleLabel.setAlignment(leadingTextAlignment());
        updateTransitionVisuals();
        getSkinnable().requestLayout();
    }

    /// Starts or settles the flexible collapse transition for the current scroll-under state.
    private void updateCollapseTarget(boolean animate) {
        M3TopAppBar control = getSkinnable();
        if (control.collapseProgressProperty().isBound()) {
            collapseAnimation.stop();
            updateTransitionVisuals();
            return;
        }

        M3TopAppBarVariant variant = control.getVariant();
        boolean flexible = variant == M3TopAppBarVariant.MEDIUM_FLEXIBLE
                || variant == M3TopAppBarVariant.LARGE_FLEXIBLE;
        double target = flexible && control.isScrolledUnder() ? 1.0 : 0.0;
        if (!animate || !isVisibleInWindow() || Double.compare(control.getCollapseProgress(), target) == 0) {
            collapseAnimation.stop();
            control.setCollapseProgress(target);
            updateTransitionVisuals();
            return;
        }

        M3MotionSpec motionSpec = M3Animation.defaultSpatial(control);
        collapseAnimation.configure(motionSpec, target);
        M3Animation.playFromStart(control, collapseAnimation);
    }

    /// Returns whether animation pulses can currently be observed in a showing window.
    private boolean isVisibleInWindow() {
        @Nullable Scene scene = getSkinnable().getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window != null && window.isShowing();
    }

    /// Updates expanded and compact title opacity, scale, and visibility for the current transition frame.
    private void updateTransitionVisuals() {
        M3TopAppBar control = getSkinnable();
        M3TopAppBarVariant variant = control.getVariant();
        boolean flexible = variant == M3TopAppBarVariant.MEDIUM_FLEXIBLE
                || variant == M3TopAppBarVariant.LARGE_FLEXIBLE;
        boolean small = variant == M3TopAppBarVariant.SMALL;
        boolean subtitlePresent = !control.getSubtitle().isEmpty();
        double progress = flexible ? control.getCollapseProgress() : 0.0;
        Node customTitle = titleContent;
        boolean morphTextTitle = flexible && customTitle == null;
        double expandedOpacity = morphTextTitle
                ? 1.0 - progress
                : flexible
                ? Math.max(0.0, Math.min(1.0, (EXPANDED_TITLE_FADE_END - progress) / EXPANDED_TITLE_FADE_END))
                : 1.0;

        boolean showExpandedTitle = !flexible || morphTextTitle || expandedOpacity > 0.001;
        titleLabel.setVisible(customTitle == null);
        titleLabel.setOpacity(expandedOpacity);
        if (customTitle != null) {
            customTitle.setVisible(showExpandedTitle);
            customTitle.setOpacity(expandedOpacity);
        }
        subtitleLabel.setVisible(subtitlePresent && (small || flexible && expandedOpacity > 0.001));
        subtitleLabel.setOpacity(flexible ? expandedOpacity : 1.0);

        double compactOpacity = morphTextTitle
                ? progress
                : Math.max(
                0.0,
                Math.min(1.0, (progress - COMPACT_TITLE_FADE_START) / (1.0 - COMPACT_TITLE_FADE_START))
        );
        boolean showCompact = flexible && (morphTextTitle || compactOpacity > 0.001);
        compactTitleLabel.setVisible(showCompact);
        compactTitleLabel.setOpacity(compactOpacity);
        compactSubtitleLabel.setVisible(flexible && subtitlePresent);
        compactSubtitleLabel.setOpacity(compactOpacity);
        if (!flexible) {
            titleLabel.setScaleX(1.0);
            titleLabel.setScaleY(1.0);
            titleLabel.setTranslateX(0.0);
            titleLabel.setTranslateY(0.0);
            subtitleLabel.setScaleX(1.0);
            subtitleLabel.setScaleY(1.0);
            subtitleLabel.setTranslateX(0.0);
            subtitleLabel.setTranslateY(0.0);
            compactTitleLabel.setScaleX(1.0);
            compactTitleLabel.setScaleY(1.0);
            compactTitleLabel.setTranslateX(0.0);
            compactTitleLabel.setTranslateY(0.0);
            compactSubtitleLabel.setScaleX(1.0);
            compactSubtitleLabel.setScaleY(1.0);
            compactSubtitleLabel.setTranslateX(0.0);
            compactSubtitleLabel.setTranslateY(0.0);
        }
    }

    /// Computes the minimum content width needed by app bar slots and compact title text.
    private double computeContentMinWidth(double height) {
        M3TopAppBar control = getSkinnable();
        double navigationWidth = navigationSlot.isManaged() ? snappedPrefWidth(navigationSlot, height) : 0.0;
        double actionsWidth = snappedPrefWidth(actions, height);
        double titleWidth = snappedPrefWidth(expandedTitleNode(), height);
        return control.getEdgePadding() * 2.0
                + navigationWidth
                + actionsWidth
                + titleWidth
                + spacingAfter(navigationWidth)
                + spacingAfter(actionsWidth);
    }

    /// Computes the current top app bar height, including flexible subtitle and collapse states.
    private double computeVariantHeight() {
        M3TopAppBar control = getSkinnable();
        double expandedHeight = switch (control.getVariant()) {
            case MEDIUM -> control.getMediumContainerHeight();
            case LARGE -> control.getLargeContainerHeight();
            case MEDIUM_FLEXIBLE -> control.getSubtitle().isEmpty()
                    ? control.getMediumFlexibleContainerHeight()
                    : control.getMediumFlexibleSubtitleContainerHeight();
            case LARGE_FLEXIBLE -> control.getSubtitle().isEmpty()
                    ? control.getLargeFlexibleContainerHeight()
                    : control.getLargeFlexibleSubtitleContainerHeight();
            case SMALL, CENTER_ALIGNED -> control.getContainerHeight();
        };
        return switch (control.getVariant()) {
            case MEDIUM_FLEXIBLE, LARGE_FLEXIBLE -> expandedHeight
                    + (control.getContainerHeight() - expandedHeight) * control.getCollapseProgress();
            case SMALL, CENTER_ALIGNED, MEDIUM, LARGE -> expandedHeight;
        };
    }

    /// Lays out the centered title variant while avoiding overlap with navigation and action slots.
    private void layoutCenterAlignedTitle(
            double x,
            double y,
            double width,
            double rowHeight,
            double navigationReserved,
            double actionsReserved
    ) {
        Node title = expandedTitleNode();
        double titleHeight = snappedPrefHeight(title, width);
        double spacing = getSkinnable().getContentSpacing();
        double maximumTitleWidth = Math.max(0.0, width - navigationReserved - actionsReserved - spacing * 2.0);
        double titleWidth = Math.min(snappedPrefWidth(title, titleHeight), maximumTitleWidth);
        double titleX = x + (width - titleWidth) / 2.0;
        double leadingReserved = navigationReserved + spacingAfter(navigationReserved);
        double trailingReserved = actionsReserved + spacingAfter(actionsReserved);
        titleX = clamp(titleX, x + leadingReserved, x + width - trailingReserved - titleWidth);
        double titleY = y + snappedOffset((rowHeight - titleHeight) / 2.0);
        title.resizeRelocate(titleX, titleY, titleWidth, titleHeight);
    }

    /// Lays out a baseline medium or large title at the bottom edge of the app bar.
    private void layoutBaselineTallTitle(
            double x,
            double y,
            double width,
            double height,
            M3TopAppBarVariant variant
    ) {
        M3TopAppBar control = getSkinnable();
        Node title = expandedTitleNode();
        double horizontalPadding = control.getHorizontalPadding();
        double availableWidth = Math.max(0.0, width - horizontalPadding * 2.0);
        double titleHeight = snappedPrefHeight(title, availableWidth);
        double titleWidth = Math.min(snappedPrefWidth(title, titleHeight), availableWidth);
        double bottomPadding = variant == M3TopAppBarVariant.MEDIUM
                ? control.getMediumBottomPadding()
                : control.getLargeBottomPadding();
        title.resizeRelocate(
                x + horizontalPadding,
                y + height - bottomPadding - titleHeight,
                titleWidth,
                titleHeight
        );
    }

    /// Lays out expanded and compact title representations for a flexible app bar.
    private void layoutFlexibleTitles(
            double x,
            double y,
            double width,
            double height,
            double rowHeight,
            double navigationReserved,
            double actionsReserved
    ) {
        M3TopAppBar control = getSkinnable();
        Node expandedTitle = expandedTitleNode();
        double horizontalPadding = control.getHorizontalPadding();
        double availableWidth = Math.max(0.0, width - horizontalPadding * 2.0);
        double subtitleHeight = subtitleLabel.isVisible() ? snappedPrefHeight(subtitleLabel, availableWidth) : 0.0;
        double titleHeight = snappedPrefHeight(expandedTitle, availableWidth);
        double subtitleWidth = subtitleHeight > 0.0
                ? Math.min(snappedPrefWidth(subtitleLabel, subtitleHeight), availableWidth)
                : 0.0;
        double titleWidth = Math.min(snappedPrefWidth(expandedTitle, titleHeight), availableWidth);
        double contentBottom = y + height - control.getFlexibleBottomPadding();

        if (subtitleHeight > 0.0) {
            subtitleLabel.resizeRelocate(
                    x + horizontalPadding,
                    contentBottom - subtitleHeight,
                    subtitleWidth,
                    subtitleHeight
            );
        }
        expandedTitle.resizeRelocate(
                x + horizontalPadding,
                contentBottom - subtitleHeight - titleHeight,
                titleWidth,
                titleHeight
        );

        layoutSmallTitle(
                x,
                y,
                width,
                rowHeight,
                navigationReserved,
                actionsReserved,
                compactTitleLabel,
                compactSubtitleLabel
        );

        double progress = control.getCollapseProgress();
        if (titleContent == null) {
            positionMorphLabels(titleLabel, compactTitleLabel, progress);
            if (!control.getSubtitle().isEmpty()) {
                positionMorphLabels(subtitleLabel, compactSubtitleLabel, progress);
            }
        } else {
            compactTitleLabel.setScaleX(1.0);
            compactTitleLabel.setScaleY(1.0);
            compactTitleLabel.setTranslateX(0.0);
            compactTitleLabel.setTranslateY(0.0);
            compactSubtitleLabel.setScaleX(1.0);
            compactSubtitleLabel.setScaleY(1.0);
            compactSubtitleLabel.setTranslateX(0.0);
            compactSubtitleLabel.setTranslateY(0.0);
        }
    }

    /// Aligns expanded and compact labels to one interpolated visible anchor with matching rendered type size.
    private static void positionMorphLabels(Label expanded, Label compact, double progress) {
        double expandedFontSize = Math.max(1.0, expanded.getFont().getSize());
        double compactFontSize = Math.max(1.0, compact.getFont().getSize());
        double expandedEndScale = compactFontSize / expandedFontSize;
        double compactStartScale = expandedFontSize / compactFontSize;
        double expandedScale = 1.0 + (expandedEndScale - 1.0) * progress;
        double compactScale = compactStartScale + (1.0 - compactStartScale) * progress;
        double anchorX = expanded.getLayoutX() + (compact.getLayoutX() - expanded.getLayoutX()) * progress;
        double expandedVisibleX = expanded.getLayoutX() + expanded.getWidth() * (1.0 - expandedScale) / 2.0;
        double compactVisibleX = compact.getLayoutX() + compact.getWidth() * (1.0 - compactScale) / 2.0;
        double expandedCenterY = expanded.getLayoutY() + expanded.getHeight() / 2.0;
        double compactCenterY = compact.getLayoutY() + compact.getHeight() / 2.0;
        double anchorCenterY = expandedCenterY + (compactCenterY - expandedCenterY) * progress;

        expanded.setScaleX(expandedScale);
        expanded.setScaleY(expandedScale);
        expanded.setTranslateX(anchorX - expandedVisibleX);
        expanded.setTranslateY(anchorCenterY - expandedCenterY);
        compact.setScaleX(compactScale);
        compact.setScaleY(compactScale);
        compact.setTranslateX(anchorX - compactVisibleX);
        compact.setTranslateY(anchorCenterY - compactCenterY);
    }

    /// Lays out a small title and optional subtitle between leading navigation and trailing actions.
    private void layoutSmallTitle(
            double x,
            double y,
            double width,
            double rowHeight,
            double navigationReserved,
            double actionsReserved,
            Node title,
            Label subtitle
    ) {
        M3TopAppBar control = getSkinnable();
        double titleX = navigationSlot.isManaged()
                ? x + navigationReserved + spacingAfter(navigationReserved)
                : x + control.getHorizontalPadding();
        double trailingEdge = actions.getChildren().isEmpty()
                ? x + width - control.getHorizontalPadding()
                : x + width - actionsReserved - spacingAfter(actionsReserved);
        double availableTitleWidth = Math.max(0.0, trailingEdge - titleX);
        double subtitleHeight = subtitle.isVisible() ? snappedPrefHeight(subtitle, availableTitleWidth) : 0.0;
        double titleHeight = snappedPrefHeight(title, availableTitleWidth);
        double subtitleWidth = subtitleHeight > 0.0
                ? Math.min(snappedPrefWidth(subtitle, subtitleHeight), availableTitleWidth)
                : 0.0;
        double titleWidth = Math.min(snappedPrefWidth(title, titleHeight), availableTitleWidth);
        double contentHeight = titleHeight + subtitleHeight;
        double titleY = y + snappedOffset((rowHeight - contentHeight) / 2.0);
        title.resizeRelocate(titleX, titleY, titleWidth, titleHeight);
        if (subtitleHeight > 0.0) {
            subtitle.resizeRelocate(titleX, titleY + titleHeight, subtitleWidth, subtitleHeight);
        }
    }

    /// Returns the node used for expanded and non-flexible title layout.
    private Node expandedTitleNode() {
        Node customTitle = titleContent;
        return customTitle == null ? titleLabel : customTitle;
    }

    /// Returns logical spacing after a visible slot.
    private double spacingAfter(double slotWidth) {
        return slotWidth > 0.0 ? getSkinnable().getContentSpacing() : 0.0;
    }

    /// Returns the physical alignment used for logical leading text.
    private Pos leadingTextAlignment() {
        // The parent control mirrors child content in RTL, so a second direction-dependent alignment would mirror twice.
        return Pos.CENTER_LEFT;
    }

    /// Returns a child node's snapped preferred width.
    private double snappedPrefWidth(Node node, double height) {
        return snapSizeX(node.prefWidth(height));
    }

    /// Returns a child node's snapped preferred height.
    private double snappedPrefHeight(Node node, double width) {
        return snapSizeY(node.prefHeight(width));
    }

    /// Snaps a non-negative offset to the vertical pixel grid.
    private double snappedOffset(double value) {
        return snapPositionY(Math.max(0.0, value));
    }

    /// Restricts a value to a closed range.
    private static double clamp(double value, double minimum, double maximum) {
        if (maximum < minimum) {
            return minimum;
        }
        return Math.max(minimum, Math.min(value, maximum));
    }

    /// A slot pane that preserves the 48-pixel Material action target while centering smaller controls.
    @NotNullByDefault
    private static final class SlotPane extends StackPane {
        /// Creates a top app bar action slot.
        private SlotPane() {
            setAlignment(Pos.CENTER);
        }

        /// Computes the minimum slot width.
        @Override
        protected double computeMinWidth(double height) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computeMinWidth(height));
        }

        /// Computes the minimum slot height.
        @Override
        protected double computeMinHeight(double width) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computeMinHeight(width));
        }

        /// Computes the preferred slot width.
        @Override
        protected double computePrefWidth(double height) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computePrefWidth(height));
        }

        /// Computes the preferred slot height.
        @Override
        protected double computePrefHeight(double width) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computePrefHeight(width));
        }
    }
}
