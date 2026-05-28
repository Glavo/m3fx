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
import org.glavo.m3fx.skins.M3FormPaneSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 form container that stacks form rows, sections, and other content nodes.
///
/// `M3FormPane` is an M3FX composition control for building forms from [M3FormRow], [M3FormSection],
/// [M3TextInputLayout], and arbitrary JavaFX nodes. It exposes token-backed content padding and row spacing and
/// updates accessibility child information as form content changes.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview) and
/// [Material Design](https://m3.material.io/) for the form controls commonly used inside this pane.
@NotNullByDefault
public class M3FormPane extends Control {
    /// The base style class for M3FX form panes.
    public static final String STYLE_CLASS = "m3-form-pane";

    /// The style class applied to the internal form content container.
    public static final String CONTENT_STYLE_CLASS = "m3-form-pane-content";

    /// The default uniform content padding.
    private static final double DEFAULT_CONTENT_PADDING = 0.0;

    /// The default vertical spacing between top-level form items.
    private static final double DEFAULT_ROW_SPACING = 16.0;

    /// The mutable top-level form item list.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// The listener used to refresh accessibility state when form items change.
    private final ListChangeListener<Node> itemsListener = change -> handleItemsChanged();

    // The styleable content padding token.
    private @Nullable StyleableDoubleProperty contentPadding;

    // The styleable row spacing token.
    private @Nullable StyleableDoubleProperty rowSpacing;

    /// Creates an empty form pane.
    public M3FormPane() {
        initialize();
    }

    /// Creates a form pane containing the supplied items.
    ///
    /// @param items the initial top-level form items
    public M3FormPane(Node... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable top-level form item list.
    ///
    /// @return the mutable top-level form item list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Adds one form item.
    ///
    /// @param item the form item to add
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds form items in order.
    ///
    /// @param items the form items to add
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all form items.
    ///
    /// @param items the replacement form items
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all form items.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the uniform content padding used by the default skin.
    ///
    /// @return the uniform content padding used by the default skin
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the uniform content padding used by the default skin.
    ///
    /// @param contentPadding the uniform content padding used by the default skin
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the content padding token property.
    ///
    /// @return the content padding token property
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = new StyleableDoubleProperty(DEFAULT_CONTENT_PADDING) {
                /// Validates content padding changes and requests layout.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "contentPadding");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FormPane.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FormPane, Number> getCssMetaData() {
                    return StyleableProperties.CONTENT_PADDING;
                }
            };
        }
        return contentPadding;
    }

    /// Returns the vertical spacing between top-level form items.
    ///
    /// @return the vertical spacing between top-level form items
    public final double getRowSpacing() {
        return rowSpacing == null ? DEFAULT_ROW_SPACING : rowSpacing.get();
    }

    /// Sets the vertical spacing between top-level form items.
    ///
    /// @param rowSpacing the vertical spacing between top-level form items
    public final void setRowSpacing(double rowSpacing) {
        rowSpacingProperty().set(M3Css.nonNegative(rowSpacing, "rowSpacing"));
    }

    /// Returns the row spacing token property.
    ///
    /// @return the row spacing token property
    public final StyleableDoubleProperty rowSpacingProperty() {
        if (rowSpacing == null) {
            rowSpacing = new StyleableDoubleProperty(DEFAULT_ROW_SPACING) {
                /// Validates row spacing changes and requests layout.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "rowSpacing");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FormPane.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "rowSpacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FormPane, Number> getCssMetaData() {
                    return StyleableProperties.ROW_SPACING;
                }
            };
        }
        return rowSpacing;
    }

    /// Returns the user-agent stylesheet for M3FX form containers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("form.css");
    }

    /// Creates the default Material Design 3 form pane skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FormPaneSkin(this);
    }

    /// Returns accessibility attributes for the form item collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getItems());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed form items.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.firstFocusTarget(getItems()));
            case SHOW_ITEM -> M3Accessible.showItem(getItems(), parameters);
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
        getItems().addListener(itemsListener);
    }

    /// Notifies accessibility clients that indexed form items changed.
    private void handleItemsChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }

    /// Validates a varargs item array before mutation.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }

    /// CSS metadata for M3FX form pane layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3FormPane, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FormPane control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FormPane control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// CSS metadata for the row spacing token.
        private static final CssMetaData<M3FormPane, Number> ROW_SPACING =
                new CssMetaData<>("-m3-row-spacing", SizeConverter.getInstance(), DEFAULT_ROW_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FormPane control) {
                        return M3Css.isSettable(control.rowSpacingProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FormPane control) {
                        return control.rowSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTENT_PADDING);
            styleables.add(ROW_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
