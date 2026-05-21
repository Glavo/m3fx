// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import org.glavo.m3fx.skins.M3FormRowSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 form row with label text, supporting text, content, and optional trailing content.
///
/// `M3FormRow` is an M3FX layout helper for aligning one form control with its label, explanatory text, and
/// trailing affordance. It is designed for use inside [M3FormPane] and can host any JavaFX node as the main
/// content.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview) and
/// [Material Design](https://m3.material.io/) for related form guidance.
@NotNullByDefault
public class M3FormRow extends Control {
    /// The base style class for M3FX form rows.
    public static final String STYLE_CLASS = "m3-form-row";

    /// The style class applied to the internal row container.
    public static final String CONTAINER_STYLE_CLASS = "m3-form-row-container";

    /// The style class applied to the label text column.
    public static final String TEXT_COLUMN_STYLE_CLASS = "m3-form-row-text-column";

    /// The style class applied to the primary label.
    public static final String LABEL_STYLE_CLASS = "m3-form-row-label";

    /// The style class applied to the supporting text label.
    public static final String SUPPORTING_TEXT_STYLE_CLASS = "m3-form-row-supporting-text";

    /// The style class applied to the content slot.
    public static final String CONTENT_STYLE_CLASS = "m3-form-row-content";

    /// The style class applied to the trailing slot.
    public static final String TRAILING_STYLE_CLASS = "m3-form-row-trailing";

    /// The default width reserved for the label column.
    private static final double DEFAULT_LABEL_WIDTH = 180.0;

    /// The default horizontal spacing between form row columns.
    private static final double DEFAULT_COLUMN_SPACING = 24.0;

    /// The default minimum row height.
    private static final double DEFAULT_ROW_MIN_HEIGHT = 64.0;

    /// The row label text.
    private final StringProperty labelText = new SimpleStringProperty(this, "labelText", "") {
        /// Rejects null label text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "labelText"));
        }

        /// Notifies accessibility clients and requests layout after label text changes.
        @Override
        protected void invalidated() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            requestLayout();
        }
    };

    /// The row supporting text.
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "") {
        /// Rejects null supporting text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "supportingText"));
        }

        /// Requests layout after supporting text changes.
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    /// The primary row content node.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content") {
        /// Validates content ownership before setting the node.
        @Override
        public void set(@Nullable Node newValue) {
            validateDistinctSlots(newValue, getTrailing(), "content");
            super.set(newValue);
        }

        /// Requests layout after content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            requestLayout();
        }
    };

    /// The optional trailing row content node.
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        /// Validates trailing ownership before setting the node.
        @Override
        public void set(@Nullable Node newValue) {
            validateDistinctSlots(getContent(), newValue, "trailing");
            super.set(newValue);
        }

        /// Requests layout after trailing content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            requestLayout();
        }
    };

    /// The styleable label width token.
    private @Nullable StyleableDoubleProperty labelWidth;

    /// The styleable column spacing token.
    private @Nullable StyleableDoubleProperty columnSpacing;

    /// The styleable row minimum height token.
    private @Nullable StyleableDoubleProperty rowMinHeight;

    /// Creates an empty form row.
    public M3FormRow() {
        initialize();
    }

    /// Creates a form row with label text and content.
    public M3FormRow(String labelText, Node content) {
        initialize();
        setLabelText(labelText);
        setContent(content);
    }

    /// Creates a form row with label text, supporting text, and content.
    public M3FormRow(String labelText, String supportingText, Node content) {
        initialize();
        setLabelText(labelText);
        setSupportingText(supportingText);
        setContent(content);
    }

    /// Creates a form row with label text, supporting text, content, and trailing content.
    public M3FormRow(String labelText, String supportingText, Node content, @Nullable Node trailing) {
        initialize();
        setLabelText(labelText);
        setSupportingText(supportingText);
        setContent(content);
        setTrailing(trailing);
    }

    /// Returns the row label text.
    public final String getLabelText() {
        return labelText.get();
    }

    /// Sets the row label text.
    public final void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    /// Returns the row label text property.
    public final StringProperty labelTextProperty() {
        return labelText;
    }

    /// Returns the row supporting text.
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the row supporting text.
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the row supporting text property.
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the primary row content node.
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the primary row content node.
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the primary row content property.
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the optional trailing row content node.
    public final @Nullable Node getTrailing() {
        return trailing.get();
    }

    /// Sets the optional trailing row content node.
    public final void setTrailing(@Nullable Node trailing) {
        this.trailing.set(trailing);
    }

    /// Returns the optional trailing row content property.
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// Returns the width reserved for the label text column.
    public final double getLabelWidth() {
        return labelWidth == null ? DEFAULT_LABEL_WIDTH : labelWidth.get();
    }

    /// Sets the width reserved for the label text column.
    public final void setLabelWidth(double labelWidth) {
        labelWidthProperty().set(M3Css.nonNegative(labelWidth, "labelWidth"));
    }

    /// Returns the label width token property.
    public final StyleableDoubleProperty labelWidthProperty() {
        if (labelWidth == null) {
            labelWidth = new StyleableDoubleProperty(DEFAULT_LABEL_WIDTH) {
                /// Validates label width changes and requests layout.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "labelWidth");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FormRow.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "labelWidth";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FormRow, Number> getCssMetaData() {
                    return StyleableProperties.LABEL_WIDTH;
                }
            };
        }
        return labelWidth;
    }

    /// Returns the horizontal spacing between row columns.
    public final double getColumnSpacing() {
        return columnSpacing == null ? DEFAULT_COLUMN_SPACING : columnSpacing.get();
    }

    /// Sets the horizontal spacing between row columns.
    public final void setColumnSpacing(double columnSpacing) {
        columnSpacingProperty().set(M3Css.nonNegative(columnSpacing, "columnSpacing"));
    }

    /// Returns the column spacing token property.
    public final StyleableDoubleProperty columnSpacingProperty() {
        if (columnSpacing == null) {
            columnSpacing = new StyleableDoubleProperty(DEFAULT_COLUMN_SPACING) {
                /// Validates column spacing changes and requests layout.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "columnSpacing");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FormRow.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "columnSpacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FormRow, Number> getCssMetaData() {
                    return StyleableProperties.COLUMN_SPACING;
                }
            };
        }
        return columnSpacing;
    }

    /// Returns the minimum row height used by the default skin.
    public final double getRowMinHeight() {
        return rowMinHeight == null ? DEFAULT_ROW_MIN_HEIGHT : rowMinHeight.get();
    }

    /// Sets the minimum row height used by the default skin.
    public final void setRowMinHeight(double rowMinHeight) {
        rowMinHeightProperty().set(M3Css.nonNegative(rowMinHeight, "rowMinHeight"));
    }

    /// Returns the row minimum height token property.
    public final StyleableDoubleProperty rowMinHeightProperty() {
        if (rowMinHeight == null) {
            rowMinHeight = new StyleableDoubleProperty(DEFAULT_ROW_MIN_HEIGHT) {
                /// Validates row minimum height changes and requests layout.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "rowMinHeight");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FormRow.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "rowMinHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FormRow, Number> getCssMetaData() {
                    return StyleableProperties.ROW_MIN_HEIGHT;
                }
            };
        }
        return rowMinHeight;
    }

    /// Returns the user-agent stylesheet for M3FX form rows.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("form.css");
    }

    /// Creates the default Material Design 3 form row skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FormRowSkin(this);
    }

    /// Returns accessibility attributes for the form row.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> getLabelText();
            case CONTENTS -> getContent();
            case ITEM_COUNT -> accessibleItemCount();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case FOCUS_NODE -> M3Accessible.firstFocusTarget(getContent(), getTrailing());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for form row content.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.firstFocusTarget(getContent(), getTrailing()));
            case SHOW_ITEM -> M3Accessible.showItem(accessibleActionItem(parameters));
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the CSS metadata for this node class.
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
    }

    /// Returns the number of content nodes exposed through indexed accessibility queries.
    private int accessibleItemCount() {
        return (getContent() == null ? 0 : 1) + (getTrailing() == null ? 0 : 1);
    }

    /// Returns the indexed accessibility item.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        @Nullable Node currentContent = getContent();
        if (currentContent != null) {
            if (index == 0) {
                return currentContent;
            }
            index--;
        }

        return index == 0 ? getTrailing() : null;
    }

    /// Returns the row node referenced by accessibility action parameters.
    private @Nullable Node accessibleActionItem(Object... parameters) {
        if (parameters.length == 0) {
            @Nullable Node currentContent = getContent();
            return currentContent != null ? currentContent : getTrailing();
        }

        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return accessibleItemAt(parameters);
        }

        for (Object parameter : parameters) {
            @Nullable Node item = accessibleActionItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the row node referenced by one accessibility action parameter.
    private @Nullable Node accessibleActionItem(@Nullable Object parameter) {
        @Nullable Node currentContent = getContent();
        @Nullable Node currentTrailing = getTrailing();
        if (parameter instanceof Number number) {
            return accessibleItemAt(number);
        }
        if (parameter == currentContent || parameter == currentTrailing) {
            return (Node) parameter;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Validates that content and trailing slots do not reference the same node.
    private static void validateDistinctSlots(@Nullable Node content, @Nullable Node trailing, String propertyName) {
        if (content != null && content == trailing) {
            throw new IllegalArgumentException(propertyName + " must not already be used by another slot");
        }
    }

    /// CSS metadata for M3FX form row layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the label width token.
        private static final CssMetaData<M3FormRow, Number> LABEL_WIDTH =
                new CssMetaData<>("-m3-label-width", SizeConverter.getInstance(), DEFAULT_LABEL_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FormRow control) {
                        return M3Css.isSettable(control.labelWidthProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FormRow control) {
                        return control.labelWidthProperty();
                    }
                };

        /// CSS metadata for the column spacing token.
        private static final CssMetaData<M3FormRow, Number> COLUMN_SPACING =
                new CssMetaData<>("-m3-column-spacing", SizeConverter.getInstance(), DEFAULT_COLUMN_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FormRow control) {
                        return M3Css.isSettable(control.columnSpacingProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FormRow control) {
                        return control.columnSpacingProperty();
                    }
                };

        /// CSS metadata for the row minimum height token.
        private static final CssMetaData<M3FormRow, Number> ROW_MIN_HEIGHT =
                new CssMetaData<>("-m3-row-min-height", SizeConverter.getInstance(), DEFAULT_ROW_MIN_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FormRow control) {
                        return M3Css.isSettable(control.rowMinHeightProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FormRow control) {
                        return control.rowMinHeightProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(LABEL_WIDTH);
            styleables.add(COLUMN_SPACING);
            styleables.add(ROW_MIN_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
