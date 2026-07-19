// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3SnackbarPresenter;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A stable scene root for Material content and in-scene presentation layers.
///
/// `M3OverlayPane` owns one optional content node, an internal snackbar presenter, and ordered regular and modal
/// overlay layers. The visual order is content, regular overlays, snackbars, and modal overlays. The content node
/// fills the pane and is the only child considered when minimum and preferred sizes are computed; all presentation
/// layers fill the same client area without affecting those measurements.
///
/// Applications normally install one `M3OverlayPane` as the stable [javafx.scene.Scene] root and assign their
/// ordinary application scaffold with [#setContent(Node)]. In-scene Material dialogs are presented directly with
/// [#showDialog(M3Dialog)] and never replace the scene root; [M3DialogWindow] provides independent native-window
/// presentation when no scene overlay is available. Custom in-scene surfaces retain the [OverlayHandle]
/// returned by [#showOverlay(Node)] or [#showModalOverlay(Node)] and close only that presentation through
/// [OverlayHandle#hide()].
///
/// Modal overlays block pointer and keyboard input directed at lower layers, restrict the root accessibility view
/// to the uppermost modal layer, keep focus inside that layer when a reachable target exists, and suspend snackbar
/// interaction and timeout progress. The modal node remains responsible for rendering any required scrim and for
/// component-specific traversal or dismissal behavior. Native surfaces that must extend outside the owner scene
/// remain [javafx.stage.PopupWindow] based and are not hosted here.
///
/// See [Material Design](https://m3.material.io/) for modal surfaces, scrims, and transient feedback.
@NotNullByDefault
public final class M3OverlayPane extends Pane {
    /// The base style class for Material overlay panes.
    public static final String STYLE_CLASS = "m3-overlay-pane";

    /// The built-in snackbar presentation layer.
    private final M3SnackbarPresenter snackbarPresenter;

    /// Regular overlays ordered from bottom to top below the snackbar layer.
    private final ArrayList<OverlayHandle> regularOverlays = new ArrayList<>();

    /// Modal overlays ordered from bottom to top above the snackbar layer.
    private final ArrayList<OverlayHandle> modalOverlays = new ArrayList<>();

    /// The scene currently observed for modal focus containment.
    private @Nullable Scene observedFocusScene;

    /// Whether one deferred modal focus request is already queued.
    private boolean modalFocusRequestPending;

    /// The preferred target retained for the next deferred modal focus request.
    private @Nullable Node pendingModalFocusTarget;

    /// Redirects focus attempts that escape the uppermost modal overlay.
    private final ChangeListener<@Nullable Node> focusOwnerListener =
            (observable, oldFocusOwner, newFocusOwner) -> {
                @Nullable Node topModal = topModalNode();
                if (topModal != null
                        && (newFocusOwner == null || !M3Accessible.containsNode(topModal, newFocusOwner))) {
                    requestModalFocusLater(null);
                }
            };

    /// Creates an empty overlay pane with an idle snackbar presentation layer.
    public M3OverlayPane() {
        snackbarPresenter = new M3SnackbarPresenter(
                snackbarValue,
                snackbarShowingValue,
                snackbarDisplayDurationValue
        );
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setPickOnBounds(false);
        snackbarPresenter.setPickOnBounds(false);
        getChildren().add(snackbarPresenter);
        sceneProperty().addListener((observable, oldScene, newScene) -> observeFocusScene(newScene));
        observeFocusScene(getScene());
        addEventFilter(InputEvent.ANY, this::filterModalInput);
    }

    /// The ordinary application content.
    ///
    /// The content is owned by this pane while installed and may have only one parent.
    ///
    /// @defaultValue `null`
    private final ReadOnlyObjectWrapper<@Nullable Node> contentValue =
            new ReadOnlyObjectWrapper<>(this, "content");

    /// Returns the ordinary content shown below every presentation layer.
    ///
    /// @return the content node, or `null` when no content is installed
    public @Nullable Node getContent() {
        return contentValue.get();
    }

    /// Sets the ordinary content shown below every presentation layer.
    ///
    /// The new node is validated and attached before the previous content is removed, so a failed attachment leaves
    /// this pane's content and observable property unchanged. A non-null node must not already have a parent and
    /// must not be this pane, one of its ancestors, or one of its presentation layers.
    ///
    /// @param content the content node, or `null` to remove the current content
    /// @throws IllegalArgumentException if the node would violate scene-graph ownership or hierarchy constraints
    public void setContent(@Nullable Node content) {
        @Nullable Node previousContent = getContent();
        if (content == previousContent) {
            return;
        }
        validateContent(content);

        if (content != null) {
            getChildren().add(0, content);
        }
        if (previousContent != null) {
            getChildren().remove(previousContent);
        }
        this.contentValue.set(content);
        requestLayout();
        if (modalOverlays.isEmpty()) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        }
    }

    /// Returns the observable, read-only property containing the ordinary application content.
    ///
    /// Structural updates use [#setContent(Node)] so node validation and scene-graph mutation remain atomic.
    /// The property is `null` by default and cannot be written directly.
    ///
    /// @return the read-only content property
    public ReadOnlyObjectProperty<@Nullable Node> contentProperty() {
        return contentValue.getReadOnlyProperty();
    }

    /// The snackbar currently presented by this pane.
    ///
    /// @defaultValue `null`
    private final ReadOnlyObjectWrapper<@Nullable M3Snackbar> snackbarValue =
            new ReadOnlyObjectWrapper<>(this, "snackbar");

    /// Returns the snackbar currently presented by this pane.
    ///
    /// @return the current snackbar, or `null` while the snackbar layer is idle
    public @Nullable M3Snackbar getSnackbar() {
        return snackbarValue.get();
    }

    /// Returns the observable, read-only property containing the current snackbar.
    ///
    /// The property is `null` while the presenter is idle and cannot be written directly. It changes when a
    /// snackbar is shown, dismissed, replaced, or promoted from the queue.
    ///
    /// @return the current-snackbar property
    public ReadOnlyObjectProperty<@Nullable M3Snackbar> snackbarProperty() {
        return snackbarValue.getReadOnlyProperty();
    }

    /// Whether the current snackbar is in its visible display phase.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper snackbarShowingValue =
            new ReadOnlyBooleanWrapper(this, "snackbarShowing");

    /// Returns whether the current snackbar is in its visible display phase.
    ///
    /// @return `true` while a snackbar is being presented
    public boolean isSnackbarShowing() {
        return snackbarShowingValue.get();
    }

    /// Returns the observable, read-only property reporting snackbar presentation state.
    ///
    /// The property is `false` by default and cannot be written directly. It is `true` only during the current
    /// snackbar's visible display phase.
    ///
    /// @return the snackbar-showing property
    public ReadOnlyBooleanProperty snackbarShowingProperty() {
        return snackbarShowingValue.getReadOnlyProperty();
    }

    /// The optional explicit duration used for automatic snackbar dismissal.
    ///
    /// A `null` value uses the duration supplied by effective motion behavior. Finite negative durations are
    /// normalized to [Duration#ZERO].
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Duration> snackbarDisplayDurationValue =
            new SimpleObjectProperty<>(this, "snackbarDisplayDuration") {
                /// Normalizes finite negative durations to zero.
                @Override
                protected void invalidated() {
                    @Nullable Duration duration = get();
                    if (!isBound() && duration != null && duration.lessThan(Duration.ZERO)) {
                        set(Duration.ZERO);
                    }
                }
            };

    /// Returns the explicitly configured automatic snackbar dismissal duration.
    ///
    /// @return the explicit duration, or `null` when effective motion behavior supplies it
    public @Nullable Duration getSnackbarDisplayDuration() {
        return snackbarDisplayDurationValue.get();
    }

    /// Sets the duration used for automatic snackbar dismissal.
    ///
    /// A finite negative duration is normalized to zero. A zero, unknown, or indefinite duration disables automatic
    /// dismissal. Assign `null` to restore the effective motion-behavior duration.
    ///
    /// @param duration the explicit display duration, or `null` to use effective motion behavior
    public void setSnackbarDisplayDuration(@Nullable Duration duration) {
        snackbarDisplayDurationValue.set(duration);
    }

    /// Returns the observable, bindable optional snackbar display-duration property.
    ///
    /// The property is `null` by default. Finite negative values assigned directly are normalized to
    /// [Duration#ZERO]; zero, unknown, and indefinite durations disable automatic dismissal. A binding source must
    /// not provide a finite negative duration.
    ///
    /// @return the snackbar display-duration property
    public ObjectProperty<@Nullable Duration> snackbarDisplayDurationProperty() {
        return snackbarDisplayDurationValue;
    }

    /// Shows a regular node above the content and below snackbars and modal overlays.
    ///
    /// Resizable overlays fill this pane's client area. Non-resizable overlays retain their layout-bounds size and
    /// are relocated to the client area's upper-left corner. Callers that need alignment, margins, or multiple
    /// coordinated nodes should place them in a full-size layout container and show that container as one overlay.
    /// Retain the returned handle for the complete presentation lifetime.
    ///
    /// @param overlay the overlay node to show
    /// @return the unique handle controlling this presentation
    /// @throws IllegalArgumentException if the node already has a parent or is already owned by this pane
    /// @throws NullPointerException     if `overlay` is `null`
    public OverlayHandle showOverlay(Node overlay) {
        return showOverlay(overlay, false);
    }

    /// Shows a modal node above every non-modal presentation layer.
    ///
    /// Modal presentation blocks lower-layer input and accessibility, redirects escaped focus to the uppermost
    /// reachable modal target, and suspends snackbar interaction and timeout progress until the final modal handle
    /// is hidden. The overlay itself renders its scrim and supplies component-specific traversal and dismissal.
    /// Retain the returned handle for the complete presentation lifetime.
    ///
    /// @param overlay the modal overlay node to show
    /// @return the unique handle controlling this presentation
    /// @throws IllegalArgumentException if the node already has a parent or is already owned by this pane
    /// @throws NullPointerException     if `overlay` is `null`
    public OverlayHandle showModalOverlay(Node overlay) {
        return showOverlay(overlay, true);
    }

    /// Presents a Material dialog as a modal in-scene layer.
    ///
    /// The returned handle owns this specific presentation and remains valid while its accepted exit transition is
    /// running. Retain it to request programmatic closure or observe presentation state. The same dialog instance
    /// cannot be presented more than once concurrently. This method must run on the JavaFX Application Thread, and
    /// this pane must be attached to a showing window.
    ///
    /// @param dialog the dialog to present
    /// @return the unique handle controlling this presentation
    /// @throws IllegalStateException if called off the JavaFX Application Thread, if this pane is not attached to a
    ///                               showing window, if the dialog is already presented, or if its pane already has
    ///                               a scene-graph parent
    /// @throws NullPointerException  if `dialog` is `null`
    public M3DialogHandle showDialog(M3Dialog dialog) {
        return Objects.requireNonNull(dialog, "dialog").present(this);
    }

    /// Returns pending snackbars in FIFO order.
    ///
    /// The returned list is live, observable, and unmodifiable. It excludes the current snackbar. Changes to a
    /// pending message's own properties do not constitute list changes.
    ///
    /// @return the pending snackbar queue
    public @UnmodifiableView ObservableList<M3Snackbar> getSnackbarQueue() {
        return snackbarPresenter.getQueue();
    }

    /// Appends a snackbar to the FIFO presentation queue.
    ///
    /// If the snackbar layer is idle, the supplied snackbar becomes current immediately. The message is retained by
    /// identity; property changes made on the JavaFX Application Thread are shown immediately while current, or read
    /// when a pending message is promoted.
    ///
    /// @param snackbar the snackbar to enqueue
    /// @throws NullPointerException if `snackbar` is `null`
    public void enqueueSnackbar(M3Snackbar snackbar) {
        snackbarPresenter.enqueue(Objects.requireNonNull(snackbar, "snackbar"));
    }

    /// Shows a snackbar immediately without changing the pending queue.
    ///
    /// Any current snackbar is replaced. The supplied message remains observable, so subsequent property changes on
    /// the JavaFX Application Thread update the existing surface. Use [#enqueueSnackbar(M3Snackbar)] when existing
    /// FIFO order must be preserved.
    ///
    /// @param snackbar the snackbar to show
    /// @throws NullPointerException if `snackbar` is `null`
    public void showSnackbar(M3Snackbar snackbar) {
        snackbarPresenter.show(Objects.requireNonNull(snackbar, "snackbar"));
    }

    /// Dismisses the current snackbar and then promotes the first queued snackbar.
    ///
    /// The operation has no effect while the snackbar layer is idle or already leaving.
    public void dismissSnackbar() {
        snackbarPresenter.dismiss();
    }

    /// Removes every pending snackbar without changing the current snackbar.
    public void clearSnackbarQueue() {
        snackbarPresenter.clearQueue();
    }

    /// Clears the pending queue and dismisses the current snackbar.
    public void dismissAllSnackbars() {
        snackbarPresenter.dismissAll();
    }

    /// Returns accessibility attributes for ordinary or modal presentation state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute parameters supplied by JavaFX
    /// @return the resolved accessibility value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        @Nullable Node topModal = topModalNode();
        if (topModal == null) {
            return super.queryAccessibleAttribute(attribute, parameters);
        }

        return switch (attribute) {
            case CHILDREN -> FXCollections.singletonObservableList(topModal);
            case CONTENTS -> topModal;
            case FOCUS_NODE -> modalAccessibleFocus(topModal);
            case ITEM_COUNT -> 1;
            case ITEM_AT_INDEX -> M3Accessible.indexParameter(parameters) == 0 ? topModal : null;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Lays out content and every presentation layer in their defined visual order.
    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        double x = snapPositionX(insets.getLeft());
        double y = snapPositionY(insets.getTop());
        double width = snapSizeX(Math.max(0.0, getWidth() - insets.getLeft() - insets.getRight()));
        double height = snapSizeY(Math.max(0.0, getHeight() - insets.getTop() - insets.getBottom()));

        layoutLayer(getContent(), x, y, width, height);
        for (OverlayHandle overlay : regularOverlays) {
            layoutLayer(overlay.node(), x, y, width, height);
        }
        layoutLayer(snackbarPresenter, x, y, width, height);
        for (OverlayHandle overlay : modalOverlays) {
            layoutLayer(overlay.node(), x, y, width, height);
        }
    }

    /// Computes minimum width from the content without allowing presentation layers to resize the pane.
    @Override
    protected double computeMinWidth(double height) {
        Insets insets = getInsets();
        double contentHeight = height < 0.0
                ? -1.0
                : Math.max(0.0, height - insets.getTop() - insets.getBottom());
        return insets.getLeft() + insets.getRight() + contentWidth(getContent(), contentHeight, true);
    }

    /// Computes minimum height from the content without allowing presentation layers to resize the pane.
    @Override
    protected double computeMinHeight(double width) {
        Insets insets = getInsets();
        double contentWidth = width < 0.0
                ? -1.0
                : Math.max(0.0, width - insets.getLeft() - insets.getRight());
        return insets.getTop() + insets.getBottom() + contentHeight(getContent(), contentWidth, true);
    }

    /// Computes preferred width from the content without allowing presentation layers to resize the pane.
    @Override
    protected double computePrefWidth(double height) {
        Insets insets = getInsets();
        double contentHeight = height < 0.0
                ? -1.0
                : Math.max(0.0, height - insets.getTop() - insets.getBottom());
        return insets.getLeft() + insets.getRight() + contentWidth(getContent(), contentHeight, false);
    }

    /// Computes preferred height from the content without allowing presentation layers to resize the pane.
    @Override
    protected double computePrefHeight(double width) {
        Insets insets = getInsets();
        double contentWidth = width < 0.0
                ? -1.0
                : Math.max(0.0, width - insets.getLeft() - insets.getRight());
        return insets.getTop() + insets.getBottom() + contentHeight(getContent(), contentWidth, false);
    }

    /// Validates a prospective content node before this pane mutates its scene graph.
    private void validateContent(@Nullable Node candidate) {
        if (candidate == null) {
            return;
        }
        if (candidate == this || candidate == snackbarPresenter || containsOverlayNode(candidate)) {
            throw new IllegalArgumentException("content cannot also be a presentation layer or this pane");
        }
        rejectAncestor(candidate, "content");
        if (candidate.getParent() != null) {
            throw new IllegalArgumentException("content already has a parent");
        }
    }

    /// Validates a prospective overlay node before this pane mutates its scene graph.
    private void validateOverlay(Node candidate) {
        if (candidate == this || candidate == snackbarPresenter || candidate == getContent()) {
            throw new IllegalArgumentException("content, snackbar, and overlay pane nodes cannot be shown as overlays");
        }
        if (containsOverlayNode(candidate)) {
            throw new IllegalArgumentException("overlay is already shown");
        }
        rejectAncestor(candidate, "overlay");
        if (candidate.getParent() != null) {
            throw new IllegalArgumentException("overlay already has a parent");
        }
    }

    /// Rejects a node that is an ancestor of this pane and would create a scene-graph cycle.
    private void rejectAncestor(Node candidate, String role) {
        for (@Nullable Parent ancestor = getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            if (candidate == ancestor) {
                throw new IllegalArgumentException(role + " cannot be an ancestor of this pane");
            }
        }
    }

    /// Returns whether either presentation layer contains the supplied node by identity.
    private boolean containsOverlayNode(Node candidate) {
        return containsOverlayNode(regularOverlays, candidate) || containsOverlayNode(modalOverlays, candidate);
    }

    /// Returns whether one handle list contains the supplied node by identity.
    private static boolean containsOverlayNode(List<OverlayHandle> overlays, Node candidate) {
        for (OverlayHandle overlay : overlays) {
            if (overlay.node() == candidate) {
                return true;
            }
        }
        return false;
    }

    /// Installs one validated regular or modal overlay and returns its unforgeable lifecycle handle.
    private OverlayHandle showOverlay(Node overlay, boolean modal) {
        Node nonNullOverlay = Objects.requireNonNull(overlay, "overlay");
        validateOverlay(nonNullOverlay);
        @Nullable Scene scene = getScene();
        @Nullable Node previousFocusOwner = scene == null ? null : scene.getFocusOwner();
        OverlayHandle handle = new OverlayHandle(this, nonNullOverlay, modal, previousFocusOwner);

        ArrayList<OverlayHandle> targetList = modal ? modalOverlays : regularOverlays;
        int childIndex = modal ? getChildren().size() : getChildren().indexOf(snackbarPresenter);
        getChildren().add(childIndex, nonNullOverlay);
        boolean completed = false;
        try {
            targetList.add(handle);
            if (modal) {
                snackbarPresenter.setModalBlocked(true);
                notifyModalAccessibilityChanged();
                requestModalFocusLater(null);
            }
            requestLayout();
            completed = true;
            return handle;
        } finally {
            if (!completed) {
                targetList.remove(handle);
                getChildren().remove(nonNullOverlay);
                handle.detach();
                snackbarPresenter.setModalBlocked(!modalOverlays.isEmpty());
                notifyModalAccessibilityChanged();
            }
        }
    }

    /// Hides the presentation owned by one handle.
    private boolean hideOverlay(OverlayHandle handle) {
        ArrayList<OverlayHandle> ownerList = handle.modal ? modalOverlays : regularOverlays;
        int index = ownerList.indexOf(handle);
        if (handle.owner != this || index < 0) {
            return false;
        }

        Node node = handle.node();
        @Nullable Node restoreFocusOwner = handle.previousFocusOwner;
        boolean wasTopModal = handle.modal && index == modalOverlays.size() - 1;
        propagateRestoreFocusOwner(handle, node, restoreFocusOwner);
        ownerList.remove(index);
        getChildren().remove(node);
        handle.detach();

        if (handle.modal) {
            snackbarPresenter.setModalBlocked(!modalOverlays.isEmpty());
            notifyModalAccessibilityChanged();
            if (wasTopModal) {
                if (modalOverlays.isEmpty()) {
                    restoreFocusLater(restoreFocusOwner);
                } else {
                    requestModalFocusLater(restoreFocusOwner);
                }
            }
        }
        requestLayout();
        return true;
    }

    /// Rewrites restoration targets that would otherwise point into a removed overlay subtree.
    private void propagateRestoreFocusOwner(
            OverlayHandle removedHandle,
            Node removedNode,
            @Nullable Node replacement
    ) {
        for (OverlayHandle candidate : modalOverlays) {
            if (candidate == removedHandle) {
                continue;
            }
            @Nullable Node target = candidate.previousFocusOwner;
            if (target != null && M3Accessible.containsNode(removedNode, target)) {
                candidate.previousFocusOwner = replacement;
            }
        }
    }

    /// Returns the uppermost active modal node.
    private @Nullable Node topModalNode() {
        int size = modalOverlays.size();
        return size == 0 ? null : modalOverlays.get(size - 1).node();
    }

    /// Blocks input events whose target is outside the uppermost modal subtree.
    private void filterModalInput(InputEvent event) {
        @Nullable Node topModal = topModalNode();
        if (topModal == null || event.isConsumed()) {
            return;
        }
        Object target = event.getTarget();
        if (!(target instanceof Node targetNode) || !M3Accessible.containsNode(topModal, targetNode)) {
            event.consume();
        }
    }

    /// Moves the focus-owner listener when this pane enters another scene.
    private void observeFocusScene(@Nullable Scene scene) {
        if (observedFocusScene == scene) {
            return;
        }
        if (observedFocusScene != null) {
            observedFocusScene.focusOwnerProperty().removeListener(focusOwnerListener);
        }
        observedFocusScene = scene;
        if (scene != null) {
            scene.focusOwnerProperty().addListener(focusOwnerListener);
        }
    }

    /// Coalesces one deferred request to focus the uppermost modal layer.
    private void requestModalFocusLater(@Nullable Node preferredTarget) {
        if (preferredTarget != null) {
            pendingModalFocusTarget = preferredTarget;
        }
        if (modalFocusRequestPending) {
            return;
        }

        modalFocusRequestPending = true;
        Platform.runLater(() -> {
            modalFocusRequestPending = false;
            @Nullable Node target = pendingModalFocusTarget;
            pendingModalFocusTarget = null;
            requestModalFocus(target);
        });
    }

    /// Focuses a preferred or first reachable target inside the uppermost modal layer.
    private void requestModalFocus(@Nullable Node preferredTarget) {
        @Nullable Node topModal = topModalNode();
        @Nullable Scene scene = getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        if (topModal == null || scene == null || window == null || !window.isShowing() || !window.isFocused()) {
            return;
        }

        @Nullable Node currentFocusOwner = scene.getFocusOwner();
        if (currentFocusOwner != null && M3Accessible.containsNode(topModal, currentFocusOwner)) {
            return;
        }
        if (preferredTarget != null
                && M3Accessible.containsNode(topModal, preferredTarget)
                && M3Accessible.canReach(preferredTarget)) {
            preferredTarget.requestFocus();
            return;
        }

        List<Node> targets = M3FocusTraversal.focusTargetsInReachableTree(topModal);
        if (!targets.isEmpty()) {
            targets.get(0).requestFocus();
        } else if (topModal.isFocusTraversable()
                && topModal.isVisible()
                && !topModal.isDisabled()
                && topModal.getScene() == scene) {
            topModal.requestFocus();
        }
    }

    /// Restores focus after the final modal presentation has left this pane.
    private void restoreFocusLater(@Nullable Node focusOwner) {
        if (focusOwner == null) {
            return;
        }
        @Nullable Scene expectedScene = getScene();
        Platform.runLater(() -> {
            if (modalOverlays.isEmpty()
                    && expectedScene != null
                    && getScene() == expectedScene
                    && focusOwner.getScene() == expectedScene
                    && M3Accessible.canReach(focusOwner)) {
                focusOwner.requestFocus();
            }
        });
    }

    /// Returns the accessible focus target constrained to the uppermost modal subtree.
    private @Nullable Node modalAccessibleFocus(Node topModal) {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner != null
                && M3Accessible.containsNode(topModal, focusOwner)
                && M3Accessible.canReach(focusOwner)) {
            return focusOwner;
        }
        return M3Accessible.focusTarget(topModal);
    }

    /// Notifies accessibility clients after the modal presentation stack changes.
    private void notifyModalAccessibilityChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }

    /// Sizes one managed layer to the complete client area.
    private static void layoutLayer(@Nullable Node node, double x, double y, double width, double height) {
        if (node == null || !node.isManaged()) {
            return;
        }
        if (node.isResizable()) {
            node.resizeRelocate(x, y, width, height);
        } else {
            node.relocate(x, y);
        }
    }

    /// Resolves content width for one minimum or preferred size query.
    private static double contentWidth(@Nullable Node node, double height, boolean minimum) {
        if (node == null || !node.isManaged()) {
            return 0.0;
        }
        if (!node.isResizable()) {
            return node.getLayoutBounds().getWidth();
        }
        return minimum ? node.minWidth(height) : node.prefWidth(height);
    }

    /// Resolves content height for one minimum or preferred size query.
    private static double contentHeight(@Nullable Node node, double width, boolean minimum) {
        if (node == null || !node.isManaged()) {
            return 0.0;
        }
        if (!node.isResizable()) {
            return node.getLayoutBounds().getHeight();
        }
        return minimum ? node.minHeight(width) : node.prefHeight(width);
    }

    /// An unforgeable lifecycle token for one regular or modal presentation.
    ///
    /// A handle is created only by [M3OverlayPane#showOverlay(Node)] or
    /// [M3OverlayPane#showModalOverlay(Node)]. Retaining the handle lets an application hide exactly the layer it
    /// created without exposing framework-owned dialog layers or mutable presentation lists.
    @NotNullByDefault
    public static final class OverlayHandle {
        /// The pane currently owning this presentation, or `null` after it is hidden.
        private @Nullable M3OverlayPane owner;

        /// The presented node, or `null` after this handle releases it.
        private @Nullable Node node;

        /// Whether this presentation participates in the modal stack.
        private final boolean modal;

        /// The focus owner captured before this presentation was shown.
        private @Nullable Node previousFocusOwner;

        /// Creates one handle for an already validated presentation request.
        private OverlayHandle(
                M3OverlayPane owner,
                Node node,
                boolean modal,
                @Nullable Node previousFocusOwner
        ) {
            this.owner = owner;
            this.node = node;
            this.modal = modal;
            this.previousFocusOwner = previousFocusOwner;
        }

        /// Returns whether this handle still owns a visible presentation layer.
        ///
        /// @return `true` until the first successful [#hide()] call
        public boolean isShowing() {
            return owner != null;
        }

        /// Hides this presentation and releases its retained node and pane references.
        ///
        /// Repeated calls after the presentation has been hidden have no effect.
        ///
        /// @return `true` if this call removed the presentation; `false` if it was already hidden
        public boolean hide() {
            @Nullable M3OverlayPane currentOwner = owner;
            return currentOwner != null && currentOwner.hideOverlay(this);
        }

        /// Returns the retained presentation node while this handle is active.
        private Node node() {
            return Objects.requireNonNull(node, "overlay handle is detached");
        }

        /// Releases all references that are unnecessary after presentation removal.
        private void detach() {
            owner = null;
            node = null;
            previousFocusOwner = null;
        }
    }
}
