// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3FormSectionSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// An M3FX form section styled with Material tokens.
///
/// Material Design 3 does not define a form-section component. This extension groups a heading, supporting text,
/// and stacked application content.
///
/// A section provides heading and supporting text followed by a live ordered content list. It is not focus
/// traversable; Up and Down move focus among reachable descendants of its content. Content nodes are parented by
/// this control while displayed, so each node must occur at most once and must not simultaneously belong to another
/// parent.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview) and
/// [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public final class M3FormSection extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-form-section";

    /// The default vertical spacing between section content nodes.
    private static final double DEFAULT_CONTENT_SPACING = 12.0;

    /// Creates an empty form section with empty title and supporting text.
    public M3FormSection() {
        initialize();
    }

    /// Creates a form section with a title.
    ///
    /// @param titleText the section title text
    /// @throws NullPointerException if `titleText` is `null`
    public M3FormSection(String titleText) {
        initialize();
        setTitleText(titleText);
    }

    /// Creates a form section with a title and supporting text.
    ///
    /// @param titleText      the section title text
    /// @param supportingText the supporting text displayed below the title
    /// @throws NullPointerException if `titleText` or `supportingText` is `null`
    public M3FormSection(String titleText, String supportingText) {
        this(titleText);
        setSupportingText(supportingText);
    }

    /// The section title text.
    ///
    /// `null` is not permitted.
    ///
    /// @defaultValue `""`
    private final javafx.beans.property.StringProperty titleText =
            new javafx.beans.property.SimpleStringProperty(this, "titleText", "") {
                /// Rejects null titles and notifies accessibility clients.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "titleText"));
                }

                /// Requests skin updates after title changes.
                @Override
                protected void invalidated() {
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                    requestLayout();
                }
            };

    /// Returns the section title.
    ///
    /// @return the section title
    public final String getTitleText() {
        return titleText.get();
    }

    /// Sets the section title.
    ///
    /// @param titleText the section title
    /// @throws NullPointerException if `titleText` is `null`
    public final void setTitleText(String titleText) {
        this.titleText.set(titleText);
    }

    /// Returns the observable, bindable section title-text property.
    ///
    /// The property defaults to an empty string and rejects `null` values.
    ///
    /// @return the section title-text property
    public final javafx.beans.property.StringProperty titleTextProperty() {
        return titleText;
    }

    /// The section supporting text.
    ///
    /// `null` is not permitted.
    ///
    /// @defaultValue `""`
    private final javafx.beans.property.StringProperty supportingText =
            new javafx.beans.property.SimpleStringProperty(this, "supportingText", "") {
                /// Rejects null supporting text values.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "supportingText"));
                }

                /// Requests skin updates after supporting text changes.
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };

    /// Returns the section supporting text.
    ///
    /// @return the section supporting text
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the section supporting text.
    ///
    /// @param supportingText the section supporting text
    /// @throws NullPointerException if `supportingText` is `null`
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the observable, bindable section supporting-text property.
    ///
    /// The property defaults to an empty string and rejects `null` values.
    ///
    /// @return the section supporting-text property
    public final javafx.beans.property.StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// The vertical spacing between section content nodes in logical pixels.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Returns the vertical spacing between section content nodes in logical pixels.
    ///
    /// @return the vertical spacing between section content nodes
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the vertical spacing between section content nodes in logical pixels.
    ///
    /// @param contentSpacing the vertical spacing between section content nodes
    /// @throws IllegalArgumentException if `contentSpacing` is negative or not finite
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the observable, bindable, styleable section content-spacing property.
    ///
    /// The property defaults to `12.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the section content-spacing property
    public final StyleableDoubleProperty contentSpacingProperty() {
        if (contentSpacing == null) {
            contentSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_SPACING,
                    this,
                    "contentSpacing",
                    StyleableProperties.CONTENT_SPACING,
                    this::requestLayout
            );
        }
        return contentSpacing;
    }

    /// The live, mutable, ordered section content list.
    ///
    /// The list initially is empty, rejects `null`, and observes additions, removals, replacements, and reordering.
    /// Nodes are parented by this control while displayed. Duplicate node instances and nodes retained by another
    /// parent do not satisfy the scene-graph ownership contract.
    private final ObservableList<Node> content = M3ObservableLists.identityDistinctElementList("content");

    /// The listener used to refresh accessibility state when section content changes.
    private final ListChangeListener<Node> contentListener = change -> handleContentChanged();

    /// Notifies accessibility clients when focus moves between section content children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getContent()));

    /// Returns the live, mutable section content list in layout order.
    ///
    /// The list rejects `null` elements and repeated occurrences of the same node instance. Bulk mutations are
    /// validated before the list changes. Each node must satisfy the JavaFX single-parent rule while displayed.
    ///
    /// @return the live, mutable section content list
    public final ObservableList<Node> getContent() {
        return content;
    }

    /// Returns the user-agent stylesheet for M3FX form sections.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("form.css");
    }

    /// Returns the horizontal content bias used to propagate available width to responsive section rows.
    ///
    /// @return the horizontal orientation
    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    /// Requests focus on the current or first accessibility item.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem() {
        if (M3Accessible.showCurrentOrItem(this, getContent())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showCurrentOrItem(this, getContent(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the container focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Creates the default skin for this control.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FormSectionSkin(this);
    }

    /// Returns accessibility attributes for the form section.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> getTitleText();
            case ITEM_COUNT -> getContent().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getContent(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getContent());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed section content.
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
            case REQUEST_FOCUS -> focusAccessibleItem();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the CSS metadata for this node class.
    ///
    /// @return the CSS metadata for this node class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this node.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes style classes and accessibility metadata.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        getContent().addListener(contentListener);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
    }

    /// Handles vertical keyboard traversal between section content nodes.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargetsInReachableTrees(getContent()),
                false,
                true,
                -1,
                false
        );
    }

    /// Notifies accessibility clients that indexed section content changed.
    private void handleContentChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }


    /// CSS metadata for M3FX form section layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the content spacing token.
        private static final CssMetaData<M3FormSection, Number> CONTENT_SPACING =
                new CssMetaData<>("-m3-content-spacing", SizeConverter.getInstance(), DEFAULT_CONTENT_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FormSection control) {
                        return M3Css.isSettable(control.contentSpacingProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FormSection control) {
                        return control.contentSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTENT_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
