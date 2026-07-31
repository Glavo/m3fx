// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3BottomSheetPresentation;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3ModalFocusTrap;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BottomSheetSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/// A Material Design 3 surface that presents supporting content from the bottom edge of a view.
///
/// A bottom sheet is an ordinary scene-graph control rather than a popup. The application places it in a layout
/// and controls presentation through [shown][#shownProperty()]. A shown modal sheet traps keyboard focus within
/// its reachable content, drag handle, and actions; Escape hides it. When configured, hiding a modal sheet returns
/// focus to the node that owned focus before it was shown. A standard sheet does not trap focus.
///
/// Sheet height remains the responsibility of the surrounding layout. The optional drag-handle action can be used
/// by an application to move between supported heights. The default sheet is shown, uses the standard variant,
/// has a visible non-actionable drag handle, restores focus when a modal presentation hides, and has no content or
/// actions.
///
/// See [Material Design bottom sheets](https://m3.material.io/components/bottom-sheets/overview).
@NotNullByDefault
public final class M3BottomSheet extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-bottom-sheet";

    /// Creates a shown standard sheet with empty headline text and no content or actions.
    public M3BottomSheet() {
        this("", null);
    }

    /// Creates a shown standard sheet with the specified headline and no content or actions.
    ///
    /// @param headline the sheet headline text
    /// @throws NullPointerException if `headline` is `null`
    public M3BottomSheet(String headline) {
        this(headline, null);
    }

    /// Creates a shown standard sheet with the specified headline and content.
    ///
    /// @param headline the sheet headline text
    /// @param content  the sheet content node, or `null` for no content
    /// @throws NullPointerException if `headline` is `null`
    public M3BottomSheet(String headline, @Nullable Node content) {
        initialize();
        setHeadline(headline);
        setContent(content);
    }

    /// The headline displayed by the sheet.
    ///
    /// The default value is the empty string. [setHeadline][#setHeadline(String)] rejects `null`.
    ///
    /// @defaultValue `""`
    private final StringProperty headline = new SimpleStringProperty(this, "headline", "");

    /// Returns the sheet headline.
    ///
    /// @return the sheet headline text
    public final String getHeadline() {
        return headline.get();
    }

    /// Sets the sheet headline.
    ///
    /// @param headline the sheet headline text
    /// @throws NullPointerException if `headline` is `null`
    public final void setHeadline(String headline) {
        this.headline.set(Objects.requireNonNull(headline, "headline"));
    }

    /// Returns the observable property that stores the sheet headline.
    ///
    /// The property can be observed and bound. Its default value is the empty string, and headline values are
    /// required to be non-null.
    ///
    /// @return the headline property
    public final StringProperty headlineProperty() {
        return headline;
    }

    /// The primary content node displayed by the sheet.
    ///
    /// The default value is `null`. The node cannot simultaneously be a child of another parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// Returns the sheet content node.
    ///
    /// @return the sheet content node, or `null` when no content is set
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the sheet content node.
    ///
    /// @param content the sheet content node, or `null` to clear it
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable property that stores the optional sheet content.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the content property
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// The interaction and presentation variant of the sheet.
    ///
    /// The default value is [M3SheetVariant#STANDARD]. A direct `null` assignment restores the default; bound values
    /// must be non-null.
    ///
    /// @defaultValue [M3SheetVariant#STANDARD]
    private final ObjectProperty<M3SheetVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3SheetVariant.STANDARD) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SheetVariant.STANDARD);
                        return;
                    }
                    updateVariantStyle();
                    focusTrap.update();
                }
            };

    /// Returns the sheet variant.
    ///
    /// @return the sheet variant
    public final M3SheetVariant getVariant() {
        return variant.get();
    }

    /// Sets the sheet variant.
    ///
    /// @param variant the sheet variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3SheetVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the observable property that stores the sheet variant.
    ///
    /// The property can be observed and bound. Its default value is [M3SheetVariant#STANDARD], and a direct
    /// `null` assignment restores that default.
    ///
    /// @return the sheet variant property
    public final ObjectProperty<M3SheetVariant> variantProperty() {
        return variant;
    }

    /// Whether this sheet is shown and participates in layout.
    ///
    /// Changing the value updates `visible` and `managed` as presentation completes. Showing a modal sheet
    /// establishes its focus scope; hiding it releases that scope. The default value is `true`.
    ///
    /// @defaultValue `true`
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", true) {
        /// Updates the sheet visibility when the property changes.
        @Override
        protected void invalidated() {
            handleShownChanged(get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
            focusTrap.update();
        }
    };

    /// Returns whether this sheet is shown.
    ///
    /// @return `true` when the sheet is shown and participates in layout
    public final boolean isShown() {
        return shown.get();
    }

    /// Sets whether this sheet is shown.
    ///
    /// @param shown whether the sheet should be shown
    public final void setShown(boolean shown) {
        this.shown.set(shown);
    }

    /// Returns the observable property that controls whether the sheet is shown.
    ///
    /// The property can be observed and bound. Its default value is `true`. Changes drive the sheet's visibility,
    /// layout participation, focus scope, and presentation transition.
    ///
    /// @return the shown property
    public final BooleanProperty shownProperty() {
        return shown;
    }

    /// Whether hiding a modal sheet attempts to restore its previous external focus owner.
    ///
    /// This property has no effect for the standard variant. Restoration is attempted only while the remembered
    /// node remains reachable. The default value is `true`.
    ///
    /// @defaultValue `true`
    private final BooleanProperty restoreFocusOnHide =
            new SimpleBooleanProperty(this, "restoreFocusOnHide", true);

    /// Returns whether modal sheet hiding restores focus to the previous focus owner.
    ///
    /// @return `true` when hiding a modal sheet restores focus to the previous focus owner
    public final boolean isRestoreFocusOnHide() {
        return restoreFocusOnHide.get();
    }

    /// Sets whether modal sheet hiding restores focus to the previous focus owner.
    ///
    /// @param restoreFocusOnHide whether hiding a modal sheet restores focus
    public final void setRestoreFocusOnHide(boolean restoreFocusOnHide) {
        this.restoreFocusOnHide.set(restoreFocusOnHide);
    }

    /// Returns the observable property that controls focus restoration after a modal sheet hides.
    ///
    /// The property can be observed and bound. Its default value is `true`, and it has no effect for
    /// [M3SheetVariant#STANDARD].
    ///
    /// @return the focus restoration property
    public final BooleanProperty restoreFocusOnHideProperty() {
        return restoreFocusOnHide;
    }

    /// Whether the drag handle is visible.
    ///
    /// A visible handle becomes focus traversable only when [onDragHandleAction][#onDragHandleActionProperty()] is
    /// non-null. The default value is `true`.
    ///
    /// @defaultValue `true`
    private final BooleanProperty dragHandleVisible =
            new SimpleBooleanProperty(this, "dragHandleVisible", true) {
                /// Requests skin layout when handle visibility changes.
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };

    /// Returns whether the drag handle is visible.
    ///
    /// @return `true` when the drag handle is visible
    public final boolean isDragHandleVisible() {
        return dragHandleVisible.get();
    }

    /// Sets whether the drag handle is visible.
    ///
    /// @param dragHandleVisible whether the drag handle should be visible
    public final void setDragHandleVisible(boolean dragHandleVisible) {
        this.dragHandleVisible.set(dragHandleVisible);
    }

    /// Returns the observable property that controls drag-handle visibility.
    ///
    /// The property can be observed and bound. Its default value is `true`.
    ///
    /// @return the drag-handle visibility property
    public final BooleanProperty dragHandleVisibleProperty() {
        return dragHandleVisible;
    }

    /// The action invoked when the user activates the visible drag handle.
    ///
    /// The default value is `null`. A non-null handler makes the handle actionable by pointer, keyboard, and
    /// accessibility clients; the handler is responsible for changing sheet height or state.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> dragHandleActionHandler =
            new SimpleObjectProperty<>(this, "onDragHandleAction") {
                /// Updates the registered drag-handle event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Returns the action handler invoked when the drag handle is selected.
    ///
    /// A non-null handler makes the visible drag handle focus traversable and exposes it as an accessibility button.
    /// The handler is responsible for applying the next supported sheet height to this control or its layout owner.
    ///
    /// @return the drag-handle action handler, or `null` when the handle is not actionable
    public final @Nullable EventHandler<ActionEvent> getOnDragHandleAction() {
        return dragHandleActionHandler.get();
    }

    /// Sets the action handler invoked when the drag handle is selected.
    ///
    /// @param onDragHandleAction the drag-handle action handler, or `null` to make the handle non-actionable
    public final void setOnDragHandleAction(@Nullable EventHandler<ActionEvent> onDragHandleAction) {
        dragHandleActionHandler.set(onDragHandleAction);
    }

    /// Returns the observable property that stores the drag-handle action handler.
    ///
    /// The property can be observed and bound. Its default value is `null`; a non-null value makes the visible
    /// drag handle actionable.
    ///
    /// @return the drag-handle action property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onDragHandleActionProperty() {
        return dragHandleActionHandler;
    }

    /// The live, mutable list of trailing action nodes.
    ///
    /// The list preserves insertion order, rejects `null`, and is observed for subsequent changes. Nodes in the
    /// list cannot simultaneously be children of another parent.
    private final ObservableList<Node> actions = M3ObservableLists.identityDistinctElementList("action");

    /// Notifies accessibility clients when focus moves between sheet content and action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> isShown()
                    ? M3Accessible.currentOrFirstFocusTarget(
                    this,
                    dragHandleFocusTarget(),
                    getContent(),
                    getActions()
            )
                    : null);

    /// Keeps keyboard traversal inside this sheet while it is shown as a modal surface.
    private final M3ModalFocusTrap focusTrap = new M3ModalFocusTrap(
            this,
            this::isModalFocusTrapActive,
            this::modalFocusTargets,
            this::hide
    );

    /// The sheet show and hide animation.
    private final M3NodeTransition visibilityAnimation = new M3NodeTransition(this);

    /// The node focused before this modal sheet was shown.
    private @Nullable Node focusOwnerBeforeShown;

    /// The last processed shown state.
    private boolean lastShown = true;

    /// Fires the drag-handle action if the sheet can currently accept it.
    ///
    /// No event is fired when this control is disabled, the handle is hidden, or no handler is installed. The
    /// generated [ActionEvent] uses this sheet as both source and target.
    public final void fireDragHandleAction() {
        if (!isDisabled() && isDragHandleVisible() && getOnDragHandleAction() != null) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Returns the live list of trailing action nodes.
    ///
    /// Changes to the returned list are reflected immediately by this sheet. The list preserves insertion order
    /// and rejects `null` elements or repeated occurrences of the same node instance. Bulk mutations are validated
    /// before the list changes.
    ///
    /// @return the live, mutable action list
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Shows this bottom sheet.
    ///
    /// This method is equivalent to `setShown(true)`. Calling it while the sheet is already shown has no effect.
    public final void show() {
        setShown(true);
    }

    /// Hides this bottom sheet.
    ///
    /// This method is equivalent to `setShown(false)`. Calling it while the sheet is already hidden has no
    /// effect.
    public final void hide() {
        setShown(false);
    }

    /// Returns the user-agent stylesheet for M3FX sheets.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("sheet.css");
    }

    /// Returns accessibility attributes for the sheet state and content.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getContent();
            case EXPANDED -> isShown();
            case FOCUS_NODE -> isShown()
                    ? M3Accessible.currentOrFirstFocusTarget(
                    this,
                    dragHandleFocusTarget(),
                    getContent(),
                    getActions()
            )
                    : null;
            case ITEM_COUNT -> M3Accessible.itemCount(dragHandleFocusTarget(), getContent(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(
                    dragHandleFocusTarget(),
                    getContent(),
                    getActions(),
                    parameters
            );
            case TEXT -> getHeadline();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by bottom sheets.
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case EXPAND -> {
                if (M3Accessible.canReveal(this)) {
                    show();
                }
            }
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            case COLLAPSE -> hide();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 bottom sheet skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BottomSheetSkin(this);
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        headline.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        content.addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyFocusNodeChanged();
        });
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        dragHandleVisible.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        dragHandleActionHandler.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        visibleProperty().addListener((observable, oldValue, newValue) -> focusTrap.update());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        visibilityAnimation.setOnFinished(event -> {
            if (!isShown()) {
                applyShownStateImmediately(false);
            }
        });
        focusTrap.install();
        focusNotifier.start();
        updateVariantStyle();
        updateAccessibleText();
    }

    /// Handles keyboard dismissal for modal sheets.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (isShown() && getVariant() == M3SheetVariant.MODAL) {
                    hide();
                    event.consume();
                }
            }
            default -> handleActionNavigationKey(event);
        }
    }

    /// Handles keyboard traversal between focusable sheet actions without stealing content editing keys.
    private void handleActionNavigationKey(KeyEvent event) {
        if (M3FocusTraversal.focusOwnerInside(this, getContent())) {
            return;
        }
        M3FocusTraversal.handleHorizontalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getActions())
        );
    }

    /// Returns whether modal keyboard focus should currently stay inside this sheet.
    private boolean isModalFocusTrapActive() {
        return isShown() && getVariant() == M3SheetVariant.MODAL && M3Accessible.canReach(this);
    }

    /// Returns the focus targets contained by this modal sheet in traversal order.
    private List<Node> modalFocusTargets() {
        ArrayList<Node> roots = new ArrayList<>(getActions().size() + 1);
        @Nullable Node contentNode = getContent();
        if (contentNode != null) {
            roots.add(contentNode);
        }
        roots.addAll(getActions());
        return M3FocusTraversal.focusTargetsInReachableTrees(dragHandleFocusTarget(), roots);
    }

    /// Processes shown state transitions and related focus bookkeeping.
    private void handleShownChanged(boolean shown) {
        if (shown == lastShown) {
            updateShownState(shown);
            return;
        }
        if (shown) {
            rememberFocusOwnerBeforeShown();
        } else {
            restoreFocusAfterHide();
        }
        lastShown = shown;
        updateShownState(shown);
    }

    /// Updates the accessibility label from the sheet headline.
    private void updateAccessibleText() {
        String text = getHeadline();
        setAccessibleText(text.isBlank() ? null : text);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that indexed sheet items changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    }

    /// Shows this sheet and focuses the requested accessible content or action target.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the requested or current target
    final boolean showAccessibleItem(Object... parameters) {
        if (!M3Accessible.canReveal(this)) {
            return false;
        }
        @Nullable Node dragHandleTarget = dragHandleFocusTarget();
        if (!isShown() && parameters.length > 0
                && !M3Accessible.canShowItem(dragHandleTarget, getContent(), getActions(), parameters)) {
            return false;
        }
        show();
        if (M3Accessible.showCurrentOrItem(this, dragHandleTarget, getContent(), getActions(), parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Requests focus for the current accessible focus target when this sheet is visible.
    ///
    /// @return `true` when the current target accepted focus
    final boolean focusAccessibleNode() {
        if (isShown() && M3Accessible.canReach(this)
                && M3Accessible.showCurrentOrItem(
                this,
                dragHandleFocusTarget(),
                getContent(),
                getActions()
        )) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the focus target changed.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the actionable drag-handle focus target, if available.
    private @Nullable Node dragHandleFocusTarget() {
        Skin<?> skin = getSkin();
        return skin instanceof M3BottomSheetPresentation presentation
                ? presentation.dragHandleFocusTarget()
                : null;
    }

    /// Stores the current scene focus owner before a modal sheet takes interaction.
    private void rememberFocusOwnerBeforeShown() {
        if (getVariant() != M3SheetVariant.MODAL) {
            focusOwnerBeforeShown = null;
            return;
        }

        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        focusOwnerBeforeShown =
                focusOwner == null || M3Accessible.containsNode(this, focusOwner) ? null : focusOwner;
    }

    /// Restores focus to the node that owned focus before this modal sheet was shown.
    private void restoreFocusAfterHide() {
        @Nullable Node focusOwner = focusOwnerBeforeShown;
        focusOwnerBeforeShown = null;
        if (getVariant() == M3SheetVariant.MODAL && isRestoreFocusOnHide() && M3Accessible.canReach(focusOwner)) {
            M3Accessible.showDirectItem(focusOwner, focusOwner);
        }
    }

    /// Updates shown state with motion when the sheet is attached to a scene.
    private void updateShownState(boolean shown) {
        visibilityAnimation.stop();
        if (shown) {
            setVisible(true);
            setManaged(true);
            if (getScene() == null) {
                applyShownStateImmediately(true);
                return;
            }

            M3MotionSpec spec = M3Animation.defaultSpatial(this);
            visibilityAnimation.configure(
                    spec,
                    1.0,
                    getScaleX(),
                    getScaleY(),
                    getTranslateX(),
                    0.0
            );
            M3Animation.playFromStart(this, visibilityAnimation);
        } else {
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            M3MotionSpec spec = M3Animation.fastSpatial(this);
            visibilityAnimation.configure(
                    spec,
                    0.0,
                    getScaleX(),
                    getScaleY(),
                    getTranslateX(),
                    hiddenTranslateY()
            );
            M3Animation.playFromStart(this, visibilityAnimation);
        }
    }

    /// Applies the shown state without animation.
    private void applyShownStateImmediately(boolean shown) {
        visibilityAnimation.stop();
        setVisible(shown);
        setManaged(shown);
        setOpacity(shown ? 1.0 : 0.0);
        setTranslateY(shown ? 0.0 : hiddenTranslateY());
    }

    /// Returns the off-screen vertical translation used when the sheet is hidden.
    private double hiddenTranslateY() {
        double height = getHeight();
        if (height <= 0.0) {
            height = prefHeight(-1.0);
        }
        return Math.max(0.0, height);
    }

    /// Updates the active variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3SheetVariant.STANDARD.styleClass(),
                M3SheetVariant.MODAL.styleClass()
        );
    }

}
