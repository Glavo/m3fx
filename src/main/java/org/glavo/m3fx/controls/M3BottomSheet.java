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
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BottomSheetSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 bottom sheet container.
@NotNullByDefault
public class M3BottomSheet extends Control {
    /// The base style class for M3FX bottom sheets.
    public static final String STYLE_CLASS = "m3-bottom-sheet";

    /// The shared sheet header style class.
    public static final String HEADER_STYLE_CLASS = M3SideSheet.HEADER_STYLE_CLASS;

    /// The shared sheet title style class.
    public static final String TITLE_STYLE_CLASS = M3SideSheet.TITLE_STYLE_CLASS;

    /// The shared sheet action container style class.
    public static final String ACTIONS_STYLE_CLASS = M3SideSheet.ACTIONS_STYLE_CLASS;

    /// The shared sheet content slot style class.
    public static final String CONTENT_STYLE_CLASS = M3SideSheet.CONTENT_STYLE_CLASS;

    /// The drag handle container style class.
    public static final String DRAG_HANDLE_CONTAINER_STYLE_CLASS = "m3-bottom-sheet-drag-handle-container";

    /// The drag handle style class.
    public static final String DRAG_HANDLE_STYLE_CLASS = "m3-bottom-sheet-drag-handle";

    /// The duration used when a bottom sheet enters.
    private static final Duration SHOW_DURATION = M3Motion.MEDIUM2;

    /// The duration used when a bottom sheet exits.
    private static final Duration HIDE_DURATION = M3Motion.SHORT4;

    /// The sheet headline text property.
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
                }
            };

    /// Whether this sheet is shown.
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", true) {
        /// Updates the sheet visibility when the property changes.
        @Override
        protected void invalidated() {
            handleShownChanged(get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }
    };

    /// Whether focus returns to the previously focused node when a modal sheet hides.
    private final BooleanProperty restoreFocusOnHide =
            new SimpleBooleanProperty(this, "restoreFocusOnHide", true);

    /// Whether the drag handle is visible.
    private final BooleanProperty dragHandleVisible =
            new SimpleBooleanProperty(this, "dragHandleVisible", true) {
                /// Requests skin layout when handle visibility changes.
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = FXCollections.observableArrayList();

    /// The sheet show and hide animation.
    private final Timeline visibilityAnimation = new Timeline();

    /// The node focused before this modal sheet was shown.
    private @Nullable Node focusOwnerBeforeShown;

    /// The last processed shown state.
    private boolean lastShown = true;

    /// Creates an empty bottom sheet.
    public M3BottomSheet() {
        this("", null);
    }

    /// Creates a bottom sheet with headline text.
    public M3BottomSheet(String headline) {
        this(headline, null);
    }

    /// Creates a bottom sheet with headline text and content.
    public M3BottomSheet(String headline, @Nullable Node content) {
        initialize();
        setHeadline(headline);
        setContent(content);
    }

    /// Creates a bottom sheet with headline text, content, and trailing actions.
    public M3BottomSheet(String headline, @Nullable Node content, Node... actions) {
        this(headline, content);
        validateActions(actions);
        getActions().addAll(actions);
    }

    /// Returns the sheet headline.
    public final String getHeadline() {
        return headline.get();
    }

    /// Sets the sheet headline.
    public final void setHeadline(String headline) {
        this.headline.set(Objects.requireNonNull(headline, "headline"));
    }

    /// Returns the sheet headline property.
    public final StringProperty headlineProperty() {
        return headline;
    }

    /// Returns the sheet content node.
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the sheet content node.
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the sheet content node property.
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the sheet variant.
    public final M3SheetVariant getVariant() {
        return variant.get();
    }

    /// Sets the sheet variant.
    public final void setVariant(M3SheetVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the sheet variant property.
    public final ObjectProperty<M3SheetVariant> variantProperty() {
        return variant;
    }

    /// Returns whether this sheet is shown.
    public final boolean isShown() {
        return shown.get();
    }

    /// Sets whether this sheet is shown.
    public final void setShown(boolean shown) {
        this.shown.set(shown);
    }

    /// Returns the shown property.
    public final BooleanProperty shownProperty() {
        return shown;
    }

    /// Returns whether modal sheet hiding restores focus to the previous focus owner.
    public final boolean isRestoreFocusOnHide() {
        return restoreFocusOnHide.get();
    }

    /// Sets whether modal sheet hiding restores focus to the previous focus owner.
    public final void setRestoreFocusOnHide(boolean restoreFocusOnHide) {
        this.restoreFocusOnHide.set(restoreFocusOnHide);
    }

    /// Returns the focus restoration property.
    public final BooleanProperty restoreFocusOnHideProperty() {
        return restoreFocusOnHide;
    }

    /// Returns whether the drag handle is visible.
    public final boolean isDragHandleVisible() {
        return dragHandleVisible.get();
    }

    /// Sets whether the drag handle is visible.
    public final void setDragHandleVisible(boolean dragHandleVisible) {
        this.dragHandleVisible.set(dragHandleVisible);
    }

    /// Returns the drag handle visibility property.
    public final BooleanProperty dragHandleVisibleProperty() {
        return dragHandleVisible;
    }

    /// Returns the mutable trailing action node list.
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Shows this bottom sheet using the Material visibility motion.
    public final void show() {
        setShown(true);
    }

    /// Hides this bottom sheet using the Material visibility motion.
    public final void hide() {
        setShown(false);
    }

    /// Returns the user-agent stylesheet for M3FX sheets.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("sheet.css");
    }

    /// Returns accessibility attributes for the sheet state and content.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getContent();
            case EXPANDED -> isShown();
            case FOCUS_NODE -> M3Accessible.focusTarget(M3Accessible.itemAt(getContent(), getActions(), 0));
            case ITEM_COUNT -> M3Accessible.itemCount(getContent(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getContent(), getActions(), parameters);
            case TEXT -> getHeadline();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by bottom sheets.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case EXPAND -> show();
            case REQUEST_FOCUS -> M3Accessible.showItem(getContent(), getActions());
            case SHOW_ITEM -> {
                show();
                M3Accessible.showItem(getContent(), getActions(), parameters);
            }
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        headline.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        content.addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        });
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
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
            default -> {
            }
        }
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
            focusOwner.requestFocus();
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

            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    SHOW_DURATION,
                    new KeyValue(opacityProperty(), 1.0, M3Motion.EMPHASIZED_DECELERATE),
                    new KeyValue(translateYProperty(), 0.0, M3Motion.EMPHASIZED_DECELERATE)
            ));
            visibilityAnimation.playFromStart();
        } else {
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    HIDE_DURATION,
                    event -> {
                        if (!isShown()) {
                            applyShownStateImmediately(false);
                        }
                    },
                    new KeyValue(opacityProperty(), 0.0, M3Motion.EMPHASIZED_ACCELERATE),
                    new KeyValue(translateYProperty(), hiddenTranslateY(), M3Motion.EMPHASIZED_ACCELERATE)
            ));
            visibilityAnimation.playFromStart();
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
