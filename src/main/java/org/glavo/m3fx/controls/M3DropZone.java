// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3DropZoneSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/// Presents a Material-themed target for drag-and-drop content.
///
/// `M3DropZone` is an M3FX extension inspired by the drop-zone pattern used by desktop design systems. Material
/// Design 3 does not define a drop-zone component. The control owns one optional [content][#contentProperty()] node
/// and automatically accepts matching drag-over events using [#preferredTransferModeProperty()]. Applications
/// inspect and process dropped data with ordinary JavaFX drag handlers such as [Node#setOnDragDropped].
///
/// The nullable [acceptance predicate][#acceptancePredicateProperty()] is evaluated for each drag-over event. A
/// `null` predicate accepts every drag that supports the preferred transfer mode. A rejected or disabled zone does
/// not call [DragEvent#acceptTransferModes(TransferMode...)]. [#dragActiveProperty()] is read-only and reports when
/// a currently supported drag is over the zone; the state clears when the drag leaves, drops, finishes, the control
/// is disabled, or its acceptance configuration changes.
///
/// The application remains responsible for calling [DragEvent#setDropCompleted(boolean)] from its drop handler.
/// [#filledProperty()] is an application-controlled presentation state indicating that the zone already contains
/// accepted content; it is not changed automatically after a drop. Because pointer drag and drop is not an
/// accessible replacement for file selection, content should include an equivalent keyboard action when one is
/// available.
///
/// ```java
/// M3DropZone zone = new M3DropZone(new M3Text("Drop a file here", M3TextRole.TITLE_MEDIUM));
/// zone.setAcceptancePredicate(event -> event.getDragboard().hasFiles());
/// zone.setOnDragDropped(event -> {
///     boolean imported = importFiles(event.getDragboard().getFiles());
///     event.setDropCompleted(imported);
///     zone.setFilled(imported);
/// });
/// ```
///
/// See [Spectrum Web Components drop zones](https://opensource.adobe.com/spectrum-web-components/components/dropzone/).
@NotNullByDefault
public final class M3DropZone extends Control {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-drop-zone";

    /// The pseudo-class used while an acceptable drag is over the zone.
    private static final javafx.css.PseudoClass DRAG_ACTIVE_PSEUDO_CLASS =
            javafx.css.PseudoClass.getPseudoClass("drag-active");

    /// The pseudo-class used when the application marks the zone as containing accepted content.
    private static final javafx.css.PseudoClass FILLED_PSEUDO_CLASS =
            javafx.css.PseudoClass.getPseudoClass("filled");

    /// The default minimum container width in logical pixels.
    private static final double DEFAULT_CONTAINER_MIN_WIDTH = 240.0;

    /// The default minimum container height in logical pixels.
    private static final double DEFAULT_CONTAINER_MIN_HEIGHT = 160.0;

    /// The default uniform content padding in logical pixels.
    private static final double DEFAULT_CONTENT_PADDING = 24.0;

    /// Creates an empty drop zone that prefers copy operations.
    public M3DropZone() {
        this(null);
    }

    /// Creates a drop zone with the specified content and a preferred copy operation.
    ///
    /// @param content the content node, or `null` for an empty zone
    public M3DropZone(@Nullable Node content) {
        initialize();
        setContent(content);
    }

    /// The node displayed inside the drop-zone container.
    ///
    /// The default value is `null`. A non-null node becomes a child of this control and therefore cannot
    /// simultaneously belong to another parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// Returns the optional content node.
    ///
    /// @return the content node, or `null` when the zone is empty
    public @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the optional content node.
    ///
    /// @param content the content node, or `null` to clear the zone
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable property that stores the optional content node.
    ///
    /// Replacing or clearing the content requests layout and updates the accessibility child collection.
    ///
    /// @return the content property
    public ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Whether the zone is presented as already containing accepted content.
    ///
    /// This application-controlled state does not change automatically after a successful drop.
    ///
    /// @defaultValue `false`
    private final BooleanProperty filled = new SimpleBooleanProperty(this, "filled") {
        /// Updates the filled pseudo-class after the value changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(FILLED_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether the zone is presented as containing accepted content.
    ///
    /// @return `true` when the filled presentation is active
    public boolean isFilled() {
        return filled.get();
    }

    /// Sets whether the zone is presented as containing accepted content.
    ///
    /// @param filled whether the filled presentation is active
    public void setFilled(boolean filled) {
        this.filled.set(filled);
    }

    /// Returns the observable application-controlled filled-state property.
    ///
    /// @return the filled-state property
    public BooleanProperty filledProperty() {
        return filled;
    }

    /// The transfer mode requested for acceptable drag-over events.
    ///
    /// The default value is [TransferMode#COPY]. Direct assignments reject `null`; if a binding supplies `null`,
    /// the effective value falls back to `COPY` until the binding supplies a non-null mode.
    ///
    /// @defaultValue [TransferMode#COPY]
    private final ObjectProperty<@Nullable TransferMode> preferredTransferMode =
            new SimpleObjectProperty<>(this, "preferredTransferMode", TransferMode.COPY) {
                /// Rejects direct null assignments while preserving nullable binding mechanics.
                @Override
                public void set(@Nullable TransferMode newValue) {
                    if (newValue == null && !isBound()) {
                        throw new NullPointerException("preferredTransferMode");
                    }
                    super.set(newValue);
                }

                /// Clears transient drag state when the requested transfer mode changes.
                @Override
                protected void invalidated() {
                    clearDragActive();
                }
            };

    /// Returns the effective transfer mode requested for acceptable drags.
    ///
    /// @return the preferred transfer mode, or `COPY` while a binding supplies `null`
    public TransferMode getPreferredTransferMode() {
        return Objects.requireNonNullElse(preferredTransferMode.get(), TransferMode.COPY);
    }

    /// Sets the transfer mode requested for acceptable drags.
    ///
    /// @param preferredTransferMode the preferred transfer mode
    /// @throws NullPointerException if `preferredTransferMode` is `null`
    public void setPreferredTransferMode(TransferMode preferredTransferMode) {
        this.preferredTransferMode.set(Objects.requireNonNull(preferredTransferMode, "preferredTransferMode"));
    }

    /// Returns the observable preferred-transfer-mode property.
    ///
    /// The property is declared nullable so a binding cannot violate JavaFX property mechanics. Its effective
    /// value, returned by [#getPreferredTransferMode()], is always non-null.
    ///
    /// @return the preferred-transfer-mode property
    public ObjectProperty<@Nullable TransferMode> preferredTransferModeProperty() {
        return preferredTransferMode;
    }

    /// The optional predicate that decides whether one drag-over event is acceptable.
    ///
    /// A `null` predicate accepts every drag that supports [#getPreferredTransferMode()]. The predicate is not
    /// invoked while the control is disabled. Exceptions thrown by the predicate propagate through JavaFX event
    /// dispatch, and the zone remains inactive for that event.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Predicate<DragEvent>> acceptancePredicate =
            new SimpleObjectProperty<>(this, "acceptancePredicate") {
                /// Clears transient drag state when the acceptance policy changes.
                @Override
                protected void invalidated() {
                    clearDragActive();
                }
            };

    /// Returns the optional drag acceptance predicate.
    ///
    /// @return the predicate, or `null` when every supported drag is acceptable
    public @Nullable Predicate<DragEvent> getAcceptancePredicate() {
        return acceptancePredicate.get();
    }

    /// Sets the optional drag acceptance predicate.
    ///
    /// @param predicate the predicate, or `null` to accept every drag supporting the preferred transfer mode
    public void setAcceptancePredicate(@Nullable Predicate<DragEvent> predicate) {
        acceptancePredicate.set(predicate);
    }

    /// Returns the observable property containing the optional drag acceptance predicate.
    ///
    /// @return the acceptance-predicate property
    public ObjectProperty<@Nullable Predicate<DragEvent>> acceptancePredicateProperty() {
        return acceptancePredicate;
    }

    /// Whether a currently acceptable drag is over the zone.
    ///
    /// The value is maintained by the control and cannot be assigned by applications.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper dragActive = new ReadOnlyBooleanWrapper(this, "dragActive") {
        /// Updates the drag-active pseudo-class after the value changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(DRAG_ACTIVE_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether a currently acceptable drag is over the zone.
    ///
    /// @return `true` while an acceptable drag is over the zone
    public boolean isDragActive() {
        return dragActive.get();
    }

    /// Returns the read-only observable drag-active property.
    ///
    /// @return the read-only drag-active property
    public ReadOnlyBooleanProperty dragActiveProperty() {
        return dragActive.getReadOnlyProperty();
    }

    /// The minimum drop-zone width in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `240.0`
    private @Nullable StyleableDoubleProperty containerMinWidth;

    /// Returns the minimum drop-zone width.
    ///
    /// @return the minimum container width in logical pixels
    public double getContainerMinWidth() {
        return containerMinWidth == null ? DEFAULT_CONTAINER_MIN_WIDTH : containerMinWidth.get();
    }

    /// Sets the minimum drop-zone width.
    ///
    /// @param containerMinWidth the minimum container width in logical pixels
    /// @throws IllegalArgumentException if the value is negative or not finite
    public void setContainerMinWidth(double containerMinWidth) {
        containerMinWidthProperty().set(M3Css.nonNegative(containerMinWidth, "containerMinWidth"));
    }

    /// Returns the styleable minimum-container-width property.
    ///
    /// CSS exposes this property as `-m3-container-min-width`.
    ///
    /// @return the minimum-container-width property
    public StyleableDoubleProperty containerMinWidthProperty() {
        if (containerMinWidth == null) {
            containerMinWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MIN_WIDTH,
                    this,
                    "containerMinWidth",
                    StyleableProperties.CONTAINER_MIN_WIDTH,
                    this::updateMetrics
            );
        }
        return containerMinWidth;
    }

    /// The minimum drop-zone height in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `160.0`
    private @Nullable StyleableDoubleProperty containerMinHeight;

    /// Returns the minimum drop-zone height.
    ///
    /// @return the minimum container height in logical pixels
    public double getContainerMinHeight() {
        return containerMinHeight == null ? DEFAULT_CONTAINER_MIN_HEIGHT : containerMinHeight.get();
    }

    /// Sets the minimum drop-zone height.
    ///
    /// @param containerMinHeight the minimum container height in logical pixels
    /// @throws IllegalArgumentException if the value is negative or not finite
    public void setContainerMinHeight(double containerMinHeight) {
        containerMinHeightProperty().set(M3Css.nonNegative(containerMinHeight, "containerMinHeight"));
    }

    /// Returns the styleable minimum-container-height property.
    ///
    /// CSS exposes this property as `-m3-container-min-height`.
    ///
    /// @return the minimum-container-height property
    public StyleableDoubleProperty containerMinHeightProperty() {
        if (containerMinHeight == null) {
            containerMinHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MIN_HEIGHT,
                    this,
                    "containerMinHeight",
                    StyleableProperties.CONTAINER_MIN_HEIGHT,
                    this::updateMetrics
            );
        }
        return containerMinHeight;
    }

    /// The uniform padding around drop-zone content in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty contentPadding;

    /// Returns the uniform content padding.
    ///
    /// @return the content padding in logical pixels
    public double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the uniform content padding.
    ///
    /// @param contentPadding the content padding in logical pixels
    /// @throws IllegalArgumentException if the value is negative or not finite
    public void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the styleable content-padding property.
    ///
    /// CSS exposes this property as `-m3-content-padding`.
    ///
    /// @return the content-padding property
    public StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    this,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING,
                    this::updateMetrics
            );
        }
        return contentPadding;
    }

    /// Returns the user-agent stylesheet for M3FX drop zones.
    ///
    /// @return the drop-zone stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("drop-zone.css");
    }

    /// Creates the default Material-themed drop-zone skin.
    ///
    /// @return the default drop-zone skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DropZoneSkin(this);
    }

    /// Returns accessibility attributes for the optional content subtree.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case CONTENTS -> getContent();
            case ITEM_COUNT -> getContent() == null ? 0 : 1;
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getContent(), (@Nullable Node) null);
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility focus and reveal actions for the optional content subtree.
    ///
    /// @param action the requested accessibility action
    /// @param parameters optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleContent();
            case SHOW_ITEM -> showAccessibleContent(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the CSS metadata for drop-zone component metrics.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this drop zone.
    ///
    /// @return the CSS metadata for this drop zone
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes styling, accessibility, metrics, and drag-event routing.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleContent, this::showAccessibleContent);
        content.addListener(observable -> handleContentChanged());
        disabledProperty().addListener(observable -> clearDragActive());
        sceneProperty().addListener(observable -> {
            if (getScene() == null) {
                clearDragActive();
            }
        });
        addEventFilter(DragEvent.DRAG_OVER, this::handleDragOver);
        addEventFilter(DragEvent.DRAG_EXITED_TARGET, this::handleDragExited);
        addEventFilter(DragEvent.DRAG_DROPPED, event -> clearDragActive());
        addEventFilter(DragEvent.DRAG_DONE, event -> clearDragActive());
        focusNotifier.start();
        updateMetrics();
    }

    /// Accepts a supported drag-over event and updates transient presentation state.
    private void handleDragOver(DragEvent event) {
        if (isDisabled()) {
            clearDragActive();
            return;
        }

        @Nullable Predicate<DragEvent> predicate = getAcceptancePredicate();
        if (predicate != null) {
            boolean accepted;
            try {
                accepted = predicate.test(event);
            } catch (RuntimeException | Error exception) {
                clearDragActive();
                throw exception;
            }
            if (!accepted) {
                clearDragActive();
                return;
            }
        }

        event.acceptTransferModes(getPreferredTransferMode());
        dragActive.set(event.getAcceptedTransferMode() != null);
    }

    /// Clears transient presentation when a drag leaves the control bounds.
    private void handleDragExited(DragEvent event) {
        if (!contains(event.getX(), event.getY())) {
            clearDragActive();
        }
    }

    /// Clears the read-only drag-active state.
    private void clearDragActive() {
        dragActive.set(false);
    }

    /// Applies the current minimum size and padding metrics without replacing caller bindings.
    private void updateMetrics() {
        M3Css.setMinWidthIfUnbound(this, getContainerMinWidth());
        M3Css.setMinHeightIfUnbound(this, getContainerMinHeight());
        M3Css.setPaddingIfUnbound(this, new Insets(getContentPadding()));
        requestLayout();
    }

    /// Requests layout and updates accessibility after content changes.
    private void handleContentChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the optional content for accessibility index zero.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        return M3Accessible.indexParameter(parameters) == 0 ? getContent() : null;
    }

    /// Requests focus for the current or first focusable node in the content subtree.
    ///
    /// @return `true` when the target accepted focus
    private boolean focusAccessibleContent() {
        if (M3Accessible.showCurrentOrItem(this, getContent(), (@Nullable Node) null)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Reveals the accessibility target requested within the content subtree.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when a target was revealed
    private boolean showAccessibleContent(Object... parameters) {
        if (M3Accessible.showCurrentOrItem(this, getContent(), (@Nullable Node) null, parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients after the routed focus target changes.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Tracks focus changes within the optional content subtree.
    private final M3AccessibleFocusNotifier focusNotifier = new M3AccessibleFocusNotifier(
            this,
            () -> M3Accessible.currentOrFirstFocusTarget(this, getContent(), (@Nullable Node) null)
    );

    /// CSS metadata for drop-zone component metrics.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the minimum container width.
        private static final CssMetaData<M3DropZone, Number> CONTAINER_MIN_WIDTH = new CssMetaData<>(
                "-m3-container-min-width",
                SizeConverter.getInstance(),
                DEFAULT_CONTAINER_MIN_WIDTH
        ) {
            /// Returns whether CSS may set the property.
            @Override
            public boolean isSettable(M3DropZone control) {
                return M3Css.isSettable(control.containerMinWidthProperty());
            }

            /// Returns the styleable property for a control.
            @Override
            public StyleableProperty<Number> getStyleableProperty(M3DropZone control) {
                return control.containerMinWidthProperty();
            }
        };

        /// CSS metadata for the minimum container height.
        private static final CssMetaData<M3DropZone, Number> CONTAINER_MIN_HEIGHT = new CssMetaData<>(
                "-m3-container-min-height",
                SizeConverter.getInstance(),
                DEFAULT_CONTAINER_MIN_HEIGHT
        ) {
            /// Returns whether CSS may set the property.
            @Override
            public boolean isSettable(M3DropZone control) {
                return M3Css.isSettable(control.containerMinHeightProperty());
            }

            /// Returns the styleable property for a control.
            @Override
            public StyleableProperty<Number> getStyleableProperty(M3DropZone control) {
                return control.containerMinHeightProperty();
            }
        };

        /// CSS metadata for uniform content padding.
        private static final CssMetaData<M3DropZone, Number> CONTENT_PADDING = new CssMetaData<>(
                "-m3-content-padding",
                SizeConverter.getInstance(),
                DEFAULT_CONTENT_PADDING
        ) {
            /// Returns whether CSS may set the property.
            @Override
            public boolean isSettable(M3DropZone control) {
                return M3Css.isSettable(control.contentPaddingProperty());
            }

            /// Returns the styleable property for a control.
            @Override
            public StyleableProperty<Number> getStyleableProperty(M3DropZone control) {
                return control.contentPaddingProperty();
            }
        };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_MIN_WIDTH);
            styleables.add(CONTAINER_MIN_HEIGHT);
            styleables.add(CONTENT_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }
}
