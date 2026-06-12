// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3FormSectionSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 form section with a heading, supporting text, and stacked content.
///
/// `M3FormSection` groups related form rows or controls under a heading. It provides a section-level title,
/// optional supporting text, configurable spacing, and an observable item list for composing larger forms.
///
/// See [Material Design](https://m3.material.io/) for the layout and hierarchy principles reflected by this
/// helper control.
@NotNullByDefault
public class M3FormSection extends Control {
    /// The base style class for M3FX form sections.
    public static final String STYLE_CLASS = "m3-form-section";

    /// The style class applied to the section header container.
    public static final String HEADER_STYLE_CLASS = "m3-form-section-header";

    /// The style class applied to the section title label.
    public static final String TITLE_STYLE_CLASS = "m3-form-section-title";

    /// The style class applied to the section supporting text label.
    public static final String SUPPORTING_TEXT_STYLE_CLASS = "m3-form-section-supporting-text";

    /// The style class applied to the section content container.
    public static final String CONTENT_STYLE_CLASS = "m3-form-section-content";

    /// The default vertical spacing between section content nodes.
    private static final double DEFAULT_CONTENT_SPACING = 12.0;

    // The section title text.
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

    // The section supporting text.
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

    /// The mutable section content list.
    private final ObservableList<Node> content = FXCollections.observableArrayList();

    /// The listener used to refresh accessibility state when section content changes.
    private final ListChangeListener<Node> contentListener = change -> handleContentChanged();

    /// Notifies accessibility clients when focus moves between section content children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getContent()));

    // The styleable content spacing token.
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Creates an empty form section.
    public M3FormSection() {
        initialize();
    }

    /// Creates a form section with a title and content.
    ///
    /// @param titleText the section title text
    /// @param content the initial section content nodes
    public M3FormSection(String titleText, Node... content) {
        initialize();
        setTitleText(titleText);
        addContent(content);
    }

    /// Creates a form section with a title, supporting text, and content.
    ///
    /// @param titleText the section title text
    /// @param supportingText the supporting text displayed below the title
    /// @param content the initial section content nodes
    public M3FormSection(String titleText, String supportingText, Node... content) {
        initialize();
        setTitleText(titleText);
        setSupportingText(supportingText);
        addContent(content);
    }

    /// Returns the section title.
    ///
    /// @return the section title
    public final String getTitleText() {
        return titleText.get();
    }

    /// Sets the section title.
    ///
    /// @param titleText the section title
    public final void setTitleText(String titleText) {
        this.titleText.set(titleText);
    }

    /// Returns the section title property.
    ///
    /// @return the section title property
    public final javafx.beans.property.StringProperty titleTextProperty() {
        return titleText;
    }

    /// Returns the section supporting text.
    ///
    /// @return the section supporting text
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the section supporting text.
    ///
    /// @param supportingText the section supporting text
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the section supporting text property.
    ///
    /// @return the section supporting text property
    public final javafx.beans.property.StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the mutable section content list.
    ///
    /// @return the mutable section content list
    public final ObservableList<Node> getContent() {
        return content;
    }

    /// Adds one section content node.
    ///
    /// @param content the section content node to add
    public final void addContent(Node content) {
        getContent().add(Objects.requireNonNull(content, "content"));
    }

    /// Adds section content nodes.
    ///
    /// @param content the section content nodes to add
    public final void addContent(Node... content) {
        validateContent(content);
        getContent().addAll(content);
    }

    /// Replaces all section content nodes.
    ///
    /// @param content the replacement section content nodes
    public final void setContent(Node... content) {
        validateContent(content);
        getContent().setAll(content);
    }

    /// Removes all section content nodes.
    public final void clearContent() {
        getContent().clear();
    }

    /// Returns the vertical spacing between section content nodes.
    ///
    /// @return the vertical spacing between section content nodes
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the vertical spacing between section content nodes.
    ///
    /// @param contentSpacing the vertical spacing between section content nodes
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the content spacing token property.
    ///
    /// @return the content spacing token property
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

    /// Returns the user-agent stylesheet for M3FX form sections.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("form.css");
    }

    /// Creates the default Material Design 3 form section skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FormSectionSkin(this);
    }

    /// Returns accessibility attributes for the form section.
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
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showCurrentOrItem(this, getContent());
            case SHOW_ITEM -> M3Accessible.showCurrentOrItem(this, getContent(), parameters);
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        getContent().addListener(contentListener);
        focusNotifier.start();
    }

    /// Notifies accessibility clients that indexed section content changed.
    private void handleContentChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Validates a varargs content array before mutation.
    private static void validateContent(Node... content) {
        Objects.requireNonNull(content, "content");
        for (Node node : content) {
            Objects.requireNonNull(node, "node");
        }
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
