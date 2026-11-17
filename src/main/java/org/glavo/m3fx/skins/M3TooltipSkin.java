// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.css.StyleableBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Tooltip].
///
/// The skin renders tooltip text and graphic content in the popup scene, mirrors style classes and sizing constraints
/// from the tooltip, and installs the tooltip's optional theme on the content label. Theme resources remain local
/// to that label, including while it is detached from a scene. Theme changes resize an already showing popup after
/// CSS has been applied. Disposal releases the local installation while preserving application-owned stylesheets.
@NotNullByDefault
public final class M3TooltipSkin extends M3PopupSkinBase<M3Tooltip> implements M3Animated {
    /// The minimum plain tooltip container height from the Material specification.
    private static final double MIN_CONTAINER_HEIGHT = 24.0;

    /// The root label that renders tooltip text and graphic content.
    private final Label root = new Label();

    /// Keeps the root style classes aligned with the tooltip style classes.
    private final ListChangeListener<String> styleClassListener = change -> syncStyleClasses();

    /// Updates the content's local theme when the tooltip theme changes.
    private final ChangeListener<@Nullable M3Theme> themeListener =
            (observable, oldValue, newValue) -> updateTheme();

    /// Whether a deferred showing-popup resize is already queued.
    private boolean popupResizePending;

    /// Whether this skin has released its bindings and must ignore deferred work.
    private boolean disposed;

    /// Creates a tooltip skin.
    ///
    /// @param tooltip the tooltip rendered by this skin
    public M3TooltipSkin(M3Tooltip tooltip) {
        super(tooltip);

        getChildren().setAll(root);
        syncStyleClasses();
        // Keep base styling on the skin so content-local theme and application styles take precedence.
        getStyleClass().add(M3PopupStyles.FALLBACK_ROOT_STYLE_CLASS);
        M3PopupStyles.addStylesheet(this, M3Stylesheets.fallbackStylesheet());
        M3PopupStyles.addStylesheet(this, M3Stylesheets.controlStylesheet("tooltip.css"));
        tooltip.getStyleClass().addListener(styleClassListener);
        tooltip.themeProperty().addListener(themeListener);
        root.textProperty().bind(tooltip.textProperty());
        root.graphicProperty().bind(tooltip.graphicProperty());
        root.contentDisplayProperty().bind(tooltip.contentDisplayProperty());
        root.wrapTextProperty().bind(tooltip.wrapTextProperty());
        root.styleProperty().bind(tooltip.styleProperty());
        root.prefWidthProperty().bind(tooltip.prefWidthProperty());
        root.minWidthProperty().bind(tooltip.minWidthProperty());
        root.maxWidthProperty().bind(tooltip.maxWidthProperty());
        root.prefHeightProperty().bind(tooltip.prefHeightProperty());
        root.minHeightProperty().bind(tooltip.minHeightProperty());
        root.maxHeightProperty().bind(tooltip.maxHeightProperty());
        updateTheme();
    }

    /// Returns the tooltip's resolved local animation preference.
    @Override
    public boolean isAnimationsEnabled() {
        return getSkinnable().isAnimationsEnabled();
    }

    /// Updates the tooltip's animation preference.
    @Override
    public void setAnimationsEnabled(boolean enabled) {
        getSkinnable().setAnimationsEnabled(enabled);
    }

    /// Shares the tooltip property so presentation transitions observe the same setting.
    @Override
    public StyleableBooleanProperty animationsEnabledProperty() {
        return getSkinnable().animationsEnabledProperty();
    }

    /// Returns the preferred width of the rendered tooltip content.
    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + root.prefWidth(height) + snappedRightInset();
    }

    /// Returns the preferred height of the rendered tooltip content.
    @Override
    protected double computePrefHeight(double width) {
        double horizontalInsets = snappedLeftInset() + snappedRightInset();
        double contentWidth = width == -1.0
                ? preferredContentWidth()
                : Math.max(0.0, width - horizontalInsets);
        double contentHeight = snappedTopInset() + root.prefHeight(contentWidth) + snappedBottomInset();
        return Math.max(MIN_CONTAINER_HEIGHT, contentHeight);
    }

    /// Lays out the rendered tooltip content.
    @Override
    protected void layoutChildren() {
        double contentX = snappedLeftInset();
        double contentY = snappedTopInset();
        double contentWidth = Math.max(0.0, getWidth() - contentX - snappedRightInset());
        double contentHeight = Math.max(0.0, getHeight() - contentY - snappedBottomInset());
        root.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    /// Releases bindings and listeners installed by this skin.
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        M3Tooltip tooltip = getSkinnable();
        tooltip.getStyleClass().removeListener(styleClassListener);
        tooltip.themeProperty().removeListener(themeListener);
        M3ThemeRuntime.uninstall(root);
        root.textProperty().unbind();
        root.graphicProperty().unbind();
        root.contentDisplayProperty().unbind();
        root.wrapTextProperty().unbind();
        root.styleProperty().unbind();
        root.prefWidthProperty().unbind();
        root.minWidthProperty().unbind();
        root.maxWidthProperty().unbind();
        root.prefHeightProperty().unbind();
        root.minHeightProperty().unbind();
        root.maxHeightProperty().unbind();
        root.setGraphic(null);
        super.dispose();
    }

    /// Copies current tooltip style classes onto the rendered root node.
    private void syncStyleClasses() {
        M3Tooltip tooltip = getSkinnable();
        root.getStyleClass().setAll(tooltip.getStyleClass());
        if (tooltip.getTheme() == null) {
            root.getStyleClass().add(M3PopupStyles.FALLBACK_ROOT_STYLE_CLASS);
        }
    }

    /// Returns the preferred width of wrapped or graphic-only tooltip content.
    private double preferredContentWidth() {
        double preferredWrapWidth = preferredWrapWidth();
        if (preferredWrapWidth != -1.0) {
            return preferredWrapWidth;
        }

        if (root.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY) {
            @Nullable Node graphic = root.getGraphic();
            if (graphic != null) {
                double graphicWidth = graphic.prefWidth(-1.0);
                if (graphicWidth > 0.0
                        && !Double.isNaN(graphicWidth)
                        && !Double.isInfinite(graphicWidth)
                        && graphicWidth != USE_COMPUTED_SIZE) {
                    return graphicWidth;
                }
            }
        }

        return -1.0;
    }

    /// Returns the explicit wrap width used when popup sizing asks for unconstrained height.
    private double preferredWrapWidth() {
        if (!root.isWrapText()) {
            return -1.0;
        }

        double preferredWidth = root.getPrefWidth();
        if (preferredWidth <= 0.0
                || Double.isNaN(preferredWidth)
                || Double.isInfinite(preferredWidth)
                || preferredWidth == USE_COMPUTED_SIZE) {
            return -1.0;
        }
        return preferredWidth;
    }

    /// Applies the content theme through the shared local-theme lifecycle and restores mirrored classes.
    private void updateTheme() {
        @Nullable M3Theme theme = getSkinnable().getTheme();
        if (theme == null) {
            M3ThemeRuntime.uninstall(root);
        } else {
            M3ThemeRuntime.install(root, theme);
        }
        syncStyleClasses();
        requestShowingPopupResize();
    }

    /// Coalesces popup resize requests until current CSS and window lifecycle changes have settled.
    private void requestShowingPopupResize() {
        if (disposed || popupResizePending) {
            return;
        }

        popupResizePending = true;
        Platform.runLater(() -> {
            popupResizePending = false;
            if (!disposed) {
                resizeShowingPopup();
            }
        });
    }

    /// Resizes an already visible popup after theme CSS changes its preferred content size.
    private void resizeShowingPopup() {
        M3Tooltip tooltip = getSkinnable();
        @Nullable Window ownerWindow = tooltip.getOwnerWindow();
        if (!tooltip.isShowing() || ownerWindow == null || !ownerWindow.isShowing()) {
            return;
        }
        root.applyCss();
        root.autosize();
        tooltip.sizeToScene();
    }
}
