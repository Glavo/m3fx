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
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// The listener used to refresh accessibility state when form items change.
    private final ListChangeListener<Node> itemsListener = change -> handleItemsChanged();

    /// Notifies accessibility clients when focus moves between form items.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getItems()));

    // The styleable content padding token.
    private @Nullable StyleableDoubleProperty contentPadding;

    // The styleable row spacing token.
    private @Nullable StyleableDoubleProperty rowSpacing;

    /// Creates an empty form pane.
    public M3FormPane() {
        initialize();
    }

    /// Returns the mutable top-level form item list.
    ///
    /// @return the mutable top-level form item list
    public final ObservableList<Node> getItems() {
        return items;
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
            contentPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    this,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING,
                    this::requestLayout
            );
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
            rowSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ROW_SPACING,
                    this,
                    "rowSpacing",
                    StyleableProperties.ROW_SPACING,
                    this::requestLayout
            );
        }
        return rowSpacing;
    }

    /// Returns the user-agent stylesheet for M3FX form containers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("form.css");
    }

    /// Requests focus on the current or first accessibility item.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem() {
        if (M3Accessible.showCurrentOrItem(this, getItems())) {
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
        if (M3Accessible.showCurrentOrItem(this, getItems(), parameters)) {
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        getItems().addListener(itemsListener);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
    }

    /// Handles vertical keyboard traversal between top-level form items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (event.getEventType() != KeyEvent.KEY_PRESSED) {
            return;
        }
        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargetsInReachableTrees(getItems()),
                false,
                true,
                -1,
                false
        );
    }

    /// Notifies accessibility clients that indexed form items changed.
    private void handleItemsChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
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
