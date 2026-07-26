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
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
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
/// The row aligns a label column, one primary content node, and an optional trailing node. It is intended for use
/// in an [M3FormPane] or [M3FormSection], but may be used independently. The row is not focus traversable; Left and
/// Right move focus between reachable content and trailing descendants using logical direction in RTL layouts.
///
/// Content and trailing nodes are parented by this control while displayed. The same node instance cannot occupy
/// both slots, and a displayed slot node must not simultaneously belong to another parent.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview) and
/// [Material Design](https://m3.material.io/) for related form guidance.
@NotNullByDefault
public final class M3FormRow extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-form-row";

    /// The default width reserved for the label column.
    private static final double DEFAULT_LABEL_WIDTH = 180.0;

    /// The default horizontal spacing between form row columns.
    private static final double DEFAULT_COLUMN_SPACING = 24.0;

    /// The default minimum row height.
    private static final double DEFAULT_ROW_MIN_HEIGHT = 64.0;

    /// Creates a form row with empty text and unoccupied content slots.
    public M3FormRow() {
        initialize();
    }

    /// Creates a form row with label text and content.
    ///
    /// @param labelText the label text displayed by the row
    /// @param content   the primary row content node
    /// @throws NullPointerException if `labelText` is `null`
    public M3FormRow(String labelText, Node content) {
        initialize();
        setLabelText(labelText);
        setContent(content);
    }

    /// Creates a form row with label text, supporting text, and content.
    ///
    /// @param labelText      the label text displayed by the row
    /// @param supportingText the supporting text displayed below the label
    /// @param content        the primary row content node
    /// @throws NullPointerException if `labelText` or `supportingText` is `null`
    public M3FormRow(String labelText, String supportingText, Node content) {
        initialize();
        setLabelText(labelText);
        setSupportingText(supportingText);
        setContent(content);
    }

    /// Creates a form row with label text, supporting text, content, and trailing content.
    ///
    /// @param labelText      the label text displayed by the row
    /// @param supportingText the supporting text displayed below the label
    /// @param content        the primary row content node
    /// @param trailing       the optional trailing content node, or `null`
    /// @throws NullPointerException     if `labelText` or `supportingText` is `null`
    /// @throws IllegalArgumentException if `content` and `trailing` are the same node instance
    public M3FormRow(String labelText, String supportingText, Node content, @Nullable Node trailing) {
        initialize();
        setLabelText(labelText);
        setSupportingText(supportingText);
        setContent(content);
        setTrailing(trailing);
    }

    /// The row label text.
    ///
    /// `null` is not permitted.
    ///
    /// @defaultValue `""`
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

    /// Returns the row label text.
    ///
    /// @return the row label text
    public final String getLabelText() {
        return labelText.get();
    }

    /// Sets the row label text.
    ///
    /// @param labelText the row label text
    /// @throws NullPointerException if `labelText` is `null`
    public final void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    /// Returns the observable, bindable row label-text property.
    ///
    /// The property defaults to an empty string and rejects `null` values.
    ///
    /// @return the row label-text property
    public final StringProperty labelTextProperty() {
        return labelText;
    }

    /// The explanatory text displayed below the row label.
    ///
    /// `null` is not permitted.
    ///
    /// @defaultValue `""`
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

    /// Returns the row supporting text.
    ///
    /// @return the row supporting text
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the row supporting text.
    ///
    /// @param supportingText the row supporting text
    /// @throws NullPointerException if `supportingText` is `null`
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the observable, bindable row supporting-text property.
    ///
    /// The property defaults to an empty string and rejects `null` values.
    ///
    /// @return the row supporting-text property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// The primary row content node, or `null` for an empty slot.
    ///
    /// The node must be distinct from [trailing][#trailingProperty()], must not create a scene-graph cycle, and is
    /// parented by this control while shown.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content") {
        /// Validates content ownership before setting the node.
        @Override
        public void set(@Nullable Node newValue) {
            validateSlotAssignment(newValue, getTrailing(), "content");
            super.set(newValue);
        }

        /// Requests layout after content changes.
        @Override
        protected void invalidated() {
            validateSlotAssignment(get(), getTrailing(), "content");
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            M3Accessible.notifyFocusNodeChanged(M3FormRow.this);
            focusNotifier.refresh();
            requestLayout();
        }
    };

    /// Returns the primary row content node.
    ///
    /// @return the primary row content node, or `null`
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the primary row content node.
    ///
    /// @param content the primary row content node, or `null`
    /// @throws IllegalArgumentException if `content` is this row, an ancestor of this row, or also the current
    ///                                  trailing node
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable, bindable primary content-node property.
    ///
    /// The property defaults to `null`. A non-null node is parented by this control while displayed, must differ
    /// from [#trailingProperty()], and must not create a scene-graph cycle. Values supplied through a binding are
    /// subject to the same constraints as [#setContent(Node)].
    ///
    /// @return the primary content-node property
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// The trailing row content node, or `null` for no trailing content.
    ///
    /// The node must be distinct from [content][#contentProperty()], must not create a scene-graph cycle, and is
    /// parented by this control while shown.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        /// Validates trailing ownership before setting the node.
        @Override
        public void set(@Nullable Node newValue) {
            validateSlotAssignment(newValue, getContent(), "trailing");
            super.set(newValue);
        }

        /// Requests layout after trailing content changes.
        @Override
        protected void invalidated() {
            validateSlotAssignment(get(), getContent(), "trailing");
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            M3Accessible.notifyFocusNodeChanged(M3FormRow.this);
            focusNotifier.refresh();
            requestLayout();
        }
    };

    /// Returns the optional trailing row content node.
    ///
    /// @return the optional trailing row content node, or `null`
    public final @Nullable Node getTrailing() {
        return trailing.get();
    }

    /// Sets the optional trailing row content node.
    ///
    /// @param trailing the optional trailing row content node, or `null`
    /// @throws IllegalArgumentException if `trailing` is this row, an ancestor of this row, or also the current
    ///                                  content node
    public final void setTrailing(@Nullable Node trailing) {
        this.trailing.set(trailing);
    }

    /// Returns the observable, bindable trailing content-node property.
    ///
    /// The property defaults to `null`. A non-null node is parented by this control while displayed, must differ
    /// from [#contentProperty()], and must not create a scene-graph cycle. Values supplied through a binding are
    /// subject to the same constraints as [#setTrailing(Node)].
    ///
    /// @return the trailing content-node property
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// The width reserved for the label column in logical pixels.
    ///
    /// @defaultValue `180.0`
    private @Nullable StyleableDoubleProperty labelWidth;

    /// Returns the width reserved for the label text column in logical pixels.
    ///
    /// @return the width reserved for the label text column
    public final double getLabelWidth() {
        return labelWidth == null ? DEFAULT_LABEL_WIDTH : labelWidth.get();
    }

    /// Sets the width reserved for the label text column in logical pixels.
    ///
    /// @param labelWidth the width reserved for the label text column
    /// @throws IllegalArgumentException if `labelWidth` is negative or not finite
    public final void setLabelWidth(double labelWidth) {
        labelWidthProperty().set(M3Css.nonNegative(labelWidth, "labelWidth"));
    }

    /// Returns the observable, bindable, styleable label-column width property.
    ///
    /// The property defaults to `180.0` logical pixels and accepts only finite, non-negative values. CSS cannot
    /// set the property while it is bound.
    ///
    /// @return the label-column width property
    public final StyleableDoubleProperty labelWidthProperty() {
        if (labelWidth == null) {
            labelWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_LABEL_WIDTH,
                    this,
                    "labelWidth",
                    StyleableProperties.LABEL_WIDTH,
                    this::requestLayout
            );
        }
        return labelWidth;
    }

    /// The horizontal spacing between row columns in logical pixels.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty columnSpacing;

    /// Returns the horizontal spacing between row columns in logical pixels.
    ///
    /// @return the horizontal spacing between row columns
    public final double getColumnSpacing() {
        return columnSpacing == null ? DEFAULT_COLUMN_SPACING : columnSpacing.get();
    }

    /// Sets the horizontal spacing between row columns in logical pixels.
    ///
    /// @param columnSpacing the horizontal spacing between row columns
    /// @throws IllegalArgumentException if `columnSpacing` is negative or not finite
    public final void setColumnSpacing(double columnSpacing) {
        columnSpacingProperty().set(M3Css.nonNegative(columnSpacing, "columnSpacing"));
    }

    /// Returns the observable, bindable, styleable column-spacing property.
    ///
    /// The property defaults to `24.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the column-spacing property
    public final StyleableDoubleProperty columnSpacingProperty() {
        if (columnSpacing == null) {
            columnSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_COLUMN_SPACING,
                    this,
                    "columnSpacing",
                    StyleableProperties.COLUMN_SPACING,
                    this::requestLayout
            );
        }
        return columnSpacing;
    }

    /// The minimum row height in logical pixels.
    ///
    /// @defaultValue `64.0`
    private @Nullable StyleableDoubleProperty rowMinHeight;

    /// Returns the minimum row height in logical pixels.
    ///
    /// @return the minimum row height in logical pixels
    public final double getRowMinHeight() {
        return rowMinHeight == null ? DEFAULT_ROW_MIN_HEIGHT : rowMinHeight.get();
    }

    /// Sets the minimum row height in logical pixels.
    ///
    /// @param rowMinHeight the minimum row height in logical pixels
    /// @throws IllegalArgumentException if `rowMinHeight` is negative or not finite
    public final void setRowMinHeight(double rowMinHeight) {
        rowMinHeightProperty().set(M3Css.nonNegative(rowMinHeight, "rowMinHeight"));
    }

    /// Returns the observable, bindable, styleable minimum row-height property.
    ///
    /// The property defaults to `64.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the minimum row-height property
    public final StyleableDoubleProperty rowMinHeightProperty() {
        if (rowMinHeight == null) {
            rowMinHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ROW_MIN_HEIGHT,
                    this,
                    "rowMinHeight",
                    StyleableProperties.ROW_MIN_HEIGHT,
                    this::requestLayout
            );
        }
        return rowMinHeight;
    }

    /// Notifies accessibility clients when focus moves between row content and trailing children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(
                    this,
                    getContent(),
                    getTrailing()
            ));

    /// Returns the user-agent stylesheet for M3FX form rows.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("form.css");
    }

    /// Requests focus on the current or first accessibility item.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem() {
        if (M3Accessible.showCurrentOrItem(this, getContent(), getTrailing())) {
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
        if (M3Accessible.showCurrentOrItem(this, getContent(), getTrailing(), parameters)) {
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

    /// Creates the default Material Design 3 form row skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FormRowSkin(this);
    }

    /// Returns accessibility attributes for the form row.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> getLabelText();
            case CONTENTS -> getContent();
            case ITEM_COUNT -> accessibleItemCount();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getContent(), getTrailing());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for form row content.
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
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
    }

    /// Handles horizontal keyboard traversal between content and trailing slots.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getContent(), getTrailing()),
                true,
                false,
                -1,
                false
        );
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

    /// Validates that a slot value is structurally usable and is not already assigned to the other slot.
    private void validateSlotAssignment(
            @Nullable Node candidate,
            @Nullable Node other,
            String propertyName
    ) {
        if (candidate == null) {
            return;
        }
        if (candidate == this) {
            throw new IllegalArgumentException(propertyName + " must not reference this row");
        }
        for (@Nullable Node ancestor = getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            if (candidate == ancestor) {
                throw new IllegalArgumentException(propertyName + " must not reference an ancestor of this row");
            }
        }
        if (candidate == other) {
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
