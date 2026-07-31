// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.internal.M3NavigationDrawerPresentation;
import org.glavo.m3fx.internal.M3ScrollReveal;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// The default Material Design 3 skin for [M3NavigationDrawer].
///
/// The skin arranges drawer content in observable-list order inside a vertically scrollable viewport. Item spacing
/// follows the control property, and the horizontal scroll bar remains disabled while content is fitted to the
/// available drawer width.
@NotNullByDefault
public final class M3NavigationDrawerSkin extends M3ItemContainerSkinBase<M3NavigationDrawer, VBox, Node>
        implements M3NavigationDrawerPresentation {
    /// The vertically scrollable viewport containing the drawer destinations.
    private final M3ScrollPane viewport = new M3ScrollPane();

    /// Reusable post-layout callback that follows a target through disclosure motion.
    private final Runnable revealPulseListener = this::revealPendingTargetAfterLayout;

    /// Moves a pending reveal request when the drawer enters another scene.
    private final ChangeListener<@Nullable Scene> sceneListener =
            (observable, oldScene, newScene) -> moveRevealPulseListener(oldScene, newScene);

    /// Cancels disclosure tracking when the user takes control of the viewport.
    private final EventHandler<ScrollEvent> userScrollHandler = event -> clearPendingReveal();

    /// The latest destination requested through [M3NavigationDrawer#scrollTo(M3ListItem)].
    private @Nullable M3ListItem pendingRevealTarget;

    /// The scene currently retaining [#revealPulseListener], or `null` when no request is scheduled.
    private @Nullable Scene revealScene;

    /// Creates a navigation drawer skin.
    ///
    /// @param control the skinned navigation drawer
    public M3NavigationDrawerSkin(M3NavigationDrawer control) {
        super(control, control.getItems(), new VBox());
        VBox container = getContainer();
        container.setManaged(true);
        container.spacingProperty().bind(control.itemSpacingProperty());

        getChildren().clear();
        viewport.setContent(container);
        viewport.setFitToWidth(true);
        viewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.setPannable(true);
        viewport.setFocusTraversable(false);
        viewport.getStyleClass().add("m3-navigation-drawer-viewport");
        viewport.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        viewport.addEventFilter(ScrollEvent.SCROLL, userScrollHandler);
        control.sceneProperty().addListener(sceneListener);
        getChildren().setAll(viewport);
    }

    /// Reveals a drawer destination and follows any active group expansion until its final layout.
    ///
    /// Repeated calls replace the pending target without allocating pulse-local callbacks.
    ///
    /// @param item the destination to reveal
    /// @throws NullPointerException if `item` is `null`
    @Override
    public void revealItem(M3ListItem item) {
        pendingRevealTarget = Objects.requireNonNull(item, "item");
        M3ScrollReveal.revealTargetInScrollPane(viewport, item);
        installRevealPulseListener(getSkinnable().getScene());
    }

    /// Removes viewport behavior, bindings, and content references before disposal.
    @Override
    public void dispose() {
        clearPendingReveal();
        getSkinnable().sceneProperty().removeListener(sceneListener);
        viewport.removeEventFilter(ScrollEvent.SCROLL, userScrollHandler);
        M3ScrollPane.disableSmoothScrolling(viewport);
        viewport.nodeOrientationProperty().unbind();
        viewport.setContent(null);
        getChildren().remove(viewport);
        getContainer().spacingProperty().unbind();
        super.dispose();
    }

    /// Allows the drawer to shrink below its content height so excess destinations can scroll.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + bottomInset;
    }

    /// Lays out the scroll viewport in the drawer content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        viewport.resizeRelocate(x, y, width, height);
    }

    /// Repositions the pending destination after one disclosure-driven layout pass.
    private void revealPendingTargetAfterLayout() {
        @Nullable M3ListItem target = pendingRevealTarget;
        if (target == null) {
            clearPendingReveal();
            return;
        }

        M3NavigationDrawer drawer = getSkinnable();
        @Nullable Scene scene = drawer.getScene();
        if (scene == null || viewport.getScene() != scene || target.getScene() != scene) {
            clearPendingReveal();
            return;
        }

        boolean needsLayout = M3ScrollReveal.revealTargetInScrollPane(viewport, target);
        if (!needsLayout && !hasRunningGroupExpansion()) {
            clearPendingReveal();
            return;
        }
        Platform.requestNextPulse();
    }

    /// Returns whether any child group is still changing the drawer's scroll geometry.
    private boolean hasRunningGroupExpansion() {
        for (Node item : getSkinnable().getItems()) {
            if (item instanceof M3NavigationDrawerGroup group) {
                Skin<?> skin = group.getSkin();
                if (skin instanceof M3NavigationDrawerGroupSkin groupSkin
                        && groupSkin.isExpansionAnimationRunning()) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Installs the reusable reveal callback on the supplied scene.
    private void installRevealPulseListener(@Nullable Scene scene) {
        if (scene == null) {
            return;
        }
        if (revealScene != scene) {
            removeRevealPulseListener();
            revealScene = scene;
            scene.addPostLayoutPulseListener(revealPulseListener);
        }
        Platform.requestNextPulse();
    }

    /// Moves an active reveal callback between scenes without changing its target.
    private void moveRevealPulseListener(@Nullable Scene oldScene, @Nullable Scene newScene) {
        if (oldScene != null && oldScene == revealScene) {
            oldScene.removePostLayoutPulseListener(revealPulseListener);
            revealScene = null;
        }
        if (pendingRevealTarget != null) {
            installRevealPulseListener(newScene);
        }
    }

    /// Clears the pending target and unregisters its post-layout callback.
    private void clearPendingReveal() {
        pendingRevealTarget = null;
        removeRevealPulseListener();
    }

    /// Removes the post-layout callback from its current scene.
    private void removeRevealPulseListener() {
        @Nullable Scene scene = revealScene;
        if (scene != null) {
            scene.removePostLayoutPulseListener(revealPulseListener);
            revealScene = null;
        }
    }
}
