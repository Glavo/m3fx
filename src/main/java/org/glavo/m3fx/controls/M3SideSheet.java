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
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3ModalFocusTrap;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SideSheetSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/// A Material Design 3 side sheet container.
///
/// `M3SideSheet` presents supporting content from a side edge of a view. It supports standard and modal sheet
/// variants, docked and detached container shapes, headline text, header icon actions, bottom action buttons, scrim
/// handling, keyboard dismissal,
/// and Material entrance and exit motion. The parent layout owns the sheet's edge placement and the 16-pixel outer
/// margin required by the detached configuration.
///
/// See [Material Design side sheets](https://m3.material.io/components/side-sheets/overview).
@NotNullByDefault
public final class M3SideSheet extends Control {
    /// The pseudo-class applied when the sheet is separated from the adjacent content edge.
    private static final PseudoClass DETACHED_PSEUDO_CLASS = PseudoClass.getPseudoClass("detached");

    /// The pseudo-class applied while the effective node orientation is right-to-left.
    private static final PseudoClass RTL_PSEUDO_CLASS = PseudoClass.getPseudoClass("rtl");

    /// The base style class for M3FX side sheets.
    public static final String STYLE_CLASS = "m3-side-sheet";

    /// The shared sheet header style class.
    public static final String HEADER_STYLE_CLASS = "m3-sheet-header";

    /// The shared sheet title style class.
    public static final String TITLE_STYLE_CLASS = "m3-sheet-title";

    /// The header icon action container style class.
    public static final String HEADER_ACTIONS_STYLE_CLASS = "m3-side-sheet-header-actions";

    /// The bottom action button container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-side-sheet-actions";

    /// The shared sheet content slot style class.
    public static final String CONTENT_STYLE_CLASS = "m3-sheet-content";

    /// The sheet headline property.
    private final StringProperty headline = new SimpleStringProperty(this, "headline", "");

    /// The sheet content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The sheet variant property.
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

    /// The shown property.
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

    /// The focus restoration property.
    private final BooleanProperty restoreFocusOnHide =
            new SimpleBooleanProperty(this, "restoreFocusOnHide", true);

    /// Whether the sheet uses the detached container shape.
    private final BooleanProperty detachedState = new SimpleBooleanProperty(this, "detached") {
        /// Synchronizes the detached container pseudo-class.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(DETACHED_PSEUDO_CLASS, get());
        }
    };

    /// The mutable header icon action list, in logical start-to-end order.
    private final ObservableList<Node> headerActions = M3ObservableLists.nonNullElementList("header action");

    /// The mutable bottom action button list, in logical start-to-end order.
    private final ObservableList<Node> actions = M3ObservableLists.nonNullElementList("action");

    /// The ordered sheet items used by accessibility and modal focus traversal.
    private final ObservableList<Node> accessibleItems = M3ObservableLists.nonNullElementList("accessible item");

    /// Notifies accessibility clients when focus moves between sheet content and action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> isShown()
                    ? M3Accessible.currentOrFirstFocusTarget(this, accessibleItems)
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

    /// Creates an empty side sheet.
    public M3SideSheet() {
        this("", null);
    }

    /// Creates a side sheet with headline text.
    ///
    /// @param headline the sheet headline text
    public M3SideSheet(String headline) {
        this(headline, null);
    }

    /// Creates a side sheet with headline text and content.
    ///
    /// @param headline the sheet headline text
    /// @param content the sheet content node, or `null` for none
    public M3SideSheet(String headline, @Nullable Node content) {
        initialize();
        setHeadline(headline);
        setContent(content);
    }

    /// Returns the sheet headline.
    ///
    /// @return the sheet headline text
    public final String getHeadline() {
        return headline.get();
    }

    /// Sets the sheet headline.
    ///
    /// @param headline the sheet headline text
    /// @throws NullPointerException if any required argument is `null`
    public final void setHeadline(String headline) {
        this.headline.set(Objects.requireNonNull(headline, "headline"));
    }

    public final StringProperty headlineProperty() {
        return headline;
    }

    /// Returns the sheet content node.
    ///
    /// @return the sheet content node, or `null` if none is set
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the sheet content node.
    ///
    /// @param content the sheet content node, or `null` to clear it
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the sheet variant.
    ///
    /// @return the sheet variant
    public final M3SheetVariant getVariant() {
        return variant.get();
    }

    /// Sets the sheet variant.
    ///
    /// @param variant the sheet variant
    /// @throws NullPointerException if any required argument is `null`
    public final void setVariant(M3SheetVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    public final ObjectProperty<M3SheetVariant> variantProperty() {
        return variant;
    }

    /// Returns whether this sheet is shown.
    ///
    /// @return `true` if this sheet is shown
    public final boolean isShown() {
        return shown.get();
    }

    /// Sets whether this sheet is shown.
    ///
    /// @param shown whether this sheet is shown
    public final void setShown(boolean shown) {
        this.shown.set(shown);
    }

    public final BooleanProperty shownProperty() {
        return shown;
    }

    /// Returns whether modal sheet hiding restores focus to the previous focus owner.
    ///
    /// @return `true` if modal sheet hiding restores focus to the previous focus owner
    public final boolean isRestoreFocusOnHide() {
        return restoreFocusOnHide.get();
    }

    /// Sets whether modal sheet hiding restores focus to the previous focus owner.
    ///
    /// @param restoreFocusOnHide whether modal sheet hiding restores focus to the previous focus owner
    public final void setRestoreFocusOnHide(boolean restoreFocusOnHide) {
        this.restoreFocusOnHide.set(restoreFocusOnHide);
    }

    public final BooleanProperty restoreFocusOnHideProperty() {
        return restoreFocusOnHide;
    }

    /// Returns whether this sheet is visually detached from the adjacent content edge.
    ///
    /// A detached sheet uses rounded corners on all four edges. Its layout owner remains responsible for the
    /// 16-pixel outer margin specified by Material Design because that margin belongs to the surrounding layout,
    /// not to the sheet's content box.
    ///
    /// @return `true` if this sheet uses the detached container shape
    public final boolean isDetached() {
        return detachedState.get();
    }

    /// Sets whether this sheet is visually detached from the adjacent content edge.
    ///
    /// @param detached whether the detached container shape is used
    public final void setDetached(boolean detached) {
        detachedState.set(detached);
    }

    /// Returns the detached presentation property.
    ///
    /// @return the detached presentation property
    public final BooleanProperty detachedProperty() {
        return detachedState;
    }

    /// Returns the mutable header icon action list.
    ///
    /// Header actions are rendered beside the headline. Use this slot for the optional back and close icon buttons
    /// from the Material side-sheet anatomy. Bottom action buttons belong in [getActions].
    ///
    /// @return the mutable header icon action list
    public final ObservableList<Node> getHeaderActions() {
        return headerActions;
    }

    /// Returns the mutable bottom action button list.
    ///
    /// The skin places these actions in the 72-pixel bottom action area and aligns them to the logical start edge.
    /// This list must not contain `null` elements.
    ///
    /// @return the mutable bottom action button list
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Shows this side sheet using the Material visibility motion.
    public final void show() {
        setShown(true);
    }

    /// Hides this side sheet using the Material visibility motion.
    public final void hide() {
        setShown(false);
    }

    /// Returns the user-agent stylesheet for M3FX sheets.
    ///
    /// @return the sheet user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("sheet.css");
    }

    /// Returns accessibility attributes for the sheet state and content.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if any required argument is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getContent();
            case EXPANDED -> isShown();
            case FOCUS_NODE -> isShown() ? M3Accessible.currentOrFirstFocusTarget(this, accessibleItems) : null;
            case ITEM_COUNT -> accessibleItems.size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(accessibleItems, parameters);
            case TEXT -> getHeadline();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by side sheets.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if any required argument is `null`
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

    /// Creates the default Material Design 3 side sheet skin.
    ///
    /// @return the default Material Design 3 side sheet skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SideSheetSkin(this);
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.DIALOG);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        headline.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        content.addListener((observable, oldValue, newValue) -> {
            rebuildAccessibleItems();
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyFocusNodeChanged();
        });
        headerActions.addListener((ListChangeListener<Node>) change -> {
            rebuildAccessibleItems();
            notifyAccessibleItemsChanged();
        });
        actions.addListener((ListChangeListener<Node>) change -> {
            rebuildAccessibleItems();
            notifyAccessibleItemsChanged();
        });
        effectiveNodeOrientationProperty().addListener(observable -> updateOrientationPseudoClass());
        visibleProperty().addListener((observable, oldValue, newValue) -> focusTrap.update());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        visibilityAnimation.setOnFinished(event -> {
            if (!isShown()) {
                applyShownStateImmediately(false);
            }
        });
        focusTrap.install();
        focusNotifier.start();
        rebuildAccessibleItems();
        updateVariantStyle();
        updateOrientationPseudoClass();
        updateAccessibleText();
    }

    /// Updates the pseudo-class used for the modal sheet's logical start corners.
    private void updateOrientationPseudoClass() {
        pseudoClassStateChanged(RTL_PSEUDO_CLASS, M3NodeLayout.isRightToLeft(this));
    }

    /// Handles keyboard dismissal for modal sheets.
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            if (isShown() && getVariant() == M3SheetVariant.MODAL) {
                hide();
                event.consume();
            }
        } else {
            handleActionNavigationKey(event);
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
        return M3FocusTraversal.focusTargetsInReachableTrees(accessibleItems);
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
        if (!isShown() && parameters.length > 0 && !M3Accessible.canShowItem(null, accessibleItems, parameters)) {
            return false;
        }
        show();
        if (M3Accessible.showCurrentOrItem(this, accessibleItems, parameters)) {
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
                && M3Accessible.showCurrentOrItem(this, accessibleItems)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Rebuilds the stable traversal order after a public content slot changes.
    private void rebuildAccessibleItems() {
        accessibleItems.clear();
        accessibleItems.addAll(headerActions);
        @Nullable Node currentContent = getContent();
        if (currentContent != null) {
            accessibleItems.add(currentContent);
        }
        accessibleItems.addAll(actions);
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
                    0.0,
                    getTranslateY()
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
                    hiddenTranslateX(),
                    getTranslateY()
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
        setTranslateX(shown ? 0.0 : hiddenTranslateX());
    }

    /// Returns the off-screen horizontal translation used when the sheet is hidden.
    private double hiddenTranslateX() {
        double width = getWidth();
        if (width <= 0.0) {
            width = prefWidth(-1.0);
        }
        double offset = Math.max(0.0, width);
        return M3NodeLayout.isRightToLeft(this) ? -offset : offset;
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
