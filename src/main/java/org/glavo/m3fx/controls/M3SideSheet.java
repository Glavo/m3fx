// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SideSheetSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 side sheet container.
///
/// `M3SideSheet` presents supporting content from a side edge of a view. It supports standard and modal sheet
/// variants, headline text, action nodes, scrim handling, keyboard dismissal, and Material entrance and exit
/// motion.
///
/// See [Material Design side sheets](https://m3.material.io/components/side-sheets/overview).
@NotNullByDefault
public class M3SideSheet extends Control {
    /// The base style class for M3FX side sheets.
    public static final String STYLE_CLASS = "m3-side-sheet";

    /// The shared sheet header style class.
    public static final String HEADER_STYLE_CLASS = "m3-sheet-header";

    /// The shared sheet title style class.
    public static final String TITLE_STYLE_CLASS = "m3-sheet-title";

    /// The shared sheet action container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-sheet-actions";

    /// The shared sheet content slot style class.
    public static final String CONTENT_STYLE_CLASS = "m3-sheet-content";

    // Backing property for the public sheet headline API.
    private final StringProperty headline = new SimpleStringProperty(this, "headline", "");

    // Backing property for the public sheet content API.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    // Backing property for the public sheet variant API.
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
                }
            };

    // Backing property for the public shown state API.
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", true) {
        /// Updates the sheet visibility when the property changes.
        @Override
        protected void invalidated() {
            handleShownChanged(get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
        }
    };

    // Backing property for the public focus restoration API.
    private final BooleanProperty restoreFocusOnHide =
            new SimpleBooleanProperty(this, "restoreFocusOnHide", true);

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between sheet content and action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> isShown()
                    ? M3Accessible.currentOrFirstFocusTarget(
                            this,
                            getContent(),
                            getActions()
                    )
                    : null);

    /// The sheet show and hide animation.
    private final Timeline visibilityAnimation = new Timeline();

    /// Observes runtime motion settings while this sheet is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    this,
                    () -> M3Animation.finishRunningAnimationsIfDisabled(this, visibilityAnimation)
            );

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

    /// Creates a side sheet with headline text, content, and trailing actions.
    ///
    /// @param headline the sheet headline text
    /// @param content the sheet content node, or `null` for none
    /// @param actions the trailing action nodes displayed in the sheet header
    public M3SideSheet(String headline, @Nullable Node content, Node... actions) {
        this(headline, content);
        validateActions(actions);
        getActions().addAll(actions);
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
    public final void setHeadline(String headline) {
        this.headline.set(Objects.requireNonNull(headline, "headline"));
    }

    /// Returns the sheet headline property.
    ///
    /// @return the sheet headline property
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

    /// Returns the sheet content node property.
    ///
    /// @return the sheet content node property
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
    public final void setVariant(M3SheetVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the sheet variant property.
    ///
    /// @return the sheet variant property
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

    /// Returns the shown property.
    ///
    /// @return the shown property
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

    /// Returns the focus restoration property.
    ///
    /// @return the focus restoration property
    public final BooleanProperty restoreFocusOnHideProperty() {
        return restoreFocusOnHide;
    }

    /// Returns the mutable trailing action node list.
    ///
    /// @return the mutable trailing action node list
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
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getContent();
            case EXPANDED -> isShown();
            case FOCUS_NODE -> isShown()
                    ? M3Accessible.currentOrFirstFocusTarget(this, getContent(), getActions())
                    : null;
            case ITEM_COUNT -> M3Accessible.itemCount(getContent(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getContent(), getActions(), parameters);
            case TEXT -> getHeadline();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by side sheets.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        headline.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        content.addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyFocusNodeChanged();
        });
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
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
        if (!isShown() && parameters.length > 0 && !M3Accessible.canShowItem(getContent(), getActions(), parameters)) {
            return false;
        }
        show();
        if (M3Accessible.showCurrentOrItem(this, getContent(), getActions(), parameters)) {
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
                && M3Accessible.showCurrentOrItem(this, getContent(), getActions())) {
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
            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    spec.duration(),
                    new KeyValue(opacityProperty(), 1.0, spec.interpolator()),
                    new KeyValue(translateXProperty(), 0.0, spec.interpolator())
            ));
            M3Animation.playFromStart(this, visibilityAnimation);
        } else {
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            M3MotionSpec spec = M3Animation.fastSpatial(this);
            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    spec.duration(),
                    event -> {
                        if (!isShown()) {
                            applyShownStateImmediately(false);
                        }
                    },
                    new KeyValue(opacityProperty(), 0.0, spec.interpolator()),
                    new KeyValue(translateXProperty(), hiddenTranslateX(), spec.interpolator())
            ));
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
                getVariant().getStyleClass(),
                M3SheetVariant.STANDARD.getStyleClass(),
                M3SheetVariant.MODAL.getStyleClass()
        );
    }

    /// Validates an action node array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }
}
