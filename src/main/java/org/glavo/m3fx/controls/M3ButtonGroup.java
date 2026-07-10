// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ButtonGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 button group for adjacent related action buttons.
///
/// `M3ButtonGroup` lays out [M3Button] children as a standard separated group or as a connected group with
/// coordinated outer and inner corners. The [variant][M3ButtonGroupVariant] controls whether grouped buttons keep
/// their own rounded containers or join into a single visual set, and the [size][M3ButtonSize] controls
/// container height and group spacing through CSS tokens.
///
/// See [Material Design button groups](https://m3.material.io/components/button-groups/overview).
@NotNullByDefault
public class M3ButtonGroup extends Control {
    /// The base style class for M3FX button groups.
    public static final String STYLE_CLASS = "m3-button-group";

    /// The style class applied to each button managed by the group.
    public static final String GROUPED_BUTTON_STYLE_CLASS = "m3-grouped-button";

    /// The style class applied when a button is the only grouped button.
    public static final String SINGLE_BUTTON_STYLE_CLASS = "m3-button-group-single";

    /// The style class applied to the first grouped button.
    public static final String FIRST_BUTTON_STYLE_CLASS = "m3-button-group-first";

    /// The style class applied to middle grouped buttons.
    public static final String MIDDLE_BUTTON_STYLE_CLASS = "m3-button-group-middle";

    /// The style class applied to the last grouped button.
    public static final String LAST_BUTTON_STYLE_CLASS = "m3-button-group-last";

    /// The default button group variant.
    private static final M3ButtonGroupVariant DEFAULT_VARIANT = M3ButtonGroupVariant.CONNECTED;

    /// The default button group size.
    private static final M3ButtonSize DEFAULT_SIZE = M3ButtonSize.SMALL;

    /// The default spacing that lets adjacent grouped button borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

    /// The mutable button group content.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between grouped buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getItems()));

    // The button group visual variant property.
    private final ObjectProperty<M3ButtonGroupVariant> variant =
            new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_VARIANT);
                        return;
                    }
                    updateVariantStyle();
                    updateButtonStyles();
                    requestLayout();
                }
            };

    // The button group size property.
    private final ObjectProperty<M3ButtonSize> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
                /// Updates size style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_SIZE);
                        return;
                    }
                    updateSizeStyle();
                    updateButtonStyles();
                    requestLayout();
                }
            };

    // The styleable spacing between grouped buttons.
    private @Nullable StyleableDoubleProperty spacing;

    /// Updates grouped button position style classes when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3Button button) {
                    clearButtonStyle(button);
                }
            }
        }
        updateButtonStyles();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Updates physical edge style classes when the effective layout direction changes.
    private final ChangeListener<NodeOrientation> effectiveNodeOrientationListener =
            (observable, oldValue, newValue) -> updateButtonStyles();

    /// Creates an empty button group.
    public M3ButtonGroup() {
        initialize();
    }

    /// Returns the mutable child list used as button group content.
    ///
    /// @return the mutable child list used as button group content
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Returns the visual button group variant.
    ///
    /// @return the visual button group variant
    public final M3ButtonGroupVariant getVariant() {
        return variant.get();
    }

    /// Sets the visual button group variant.
    ///
    /// @param variant the visual button group variant
    public final void setVariant(M3ButtonGroupVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the visual button group variant property.
    ///
    /// @return the visual button group variant property
    public final ObjectProperty<M3ButtonGroupVariant> variantProperty() {
        return variant;
    }

    /// Returns the Material Expressive button group size.
    ///
    /// @return the Material Expressive button group size
    public final M3ButtonSize getSize() {
        return size.get();
    }

    /// Sets the Material Expressive button group size.
    ///
    /// @param size the Material Expressive button group size
    public final void setSize(M3ButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the Material Expressive button group size property.
    ///
    /// @return the Material Expressive button group size property
    public final ObjectProperty<M3ButtonSize> sizeProperty() {
        return size;
    }

    /// Returns the spacing between grouped buttons.
    ///
    /// @return the child spacing in pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between grouped buttons.
    ///
    /// @param spacing the child spacing in pixels
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.finite(spacing, "spacing"));
    }

    /// Returns the spacing property.
    ///
    /// @return the styleable child spacing property
    public final StyleableDoubleProperty spacingProperty() {
        if (spacing == null) {
            spacing = M3Css.finiteStyleableDoubleProperty(
                    DEFAULT_SPACING,
                    this,
                    "spacing",
                    StyleableProperties.SPACING,
                    () -> {
                    }
            );
        }
        return spacing;
    }

    /// Returns the user-agent stylesheet for M3FX button groups.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("button-group.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for grouped button content.
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

    /// Executes accessibility actions for grouped button content.
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

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        updateVariantStyle();
        updateSizeStyle();
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        effectiveNodeOrientationProperty().addListener(effectiveNodeOrientationListener);
        getItems().addListener(childrenListener);
        focusNotifier.start();
        updateButtonStyles();
    }

    /// Applies keyboard focus navigation across enabled grouped buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeyFocus(
                event,
                this,
                getItems(),
                M3SelectionNavigation.focused(getItems(), M3Button.class),
                M3Button.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this)
        );
    }

    /// Applies first, middle, last, or single button style classes.
    private void updateButtonStyles() {
        int buttonCount = 0;
        for (Node child : getItems()) {
            if (child instanceof M3Button) {
                buttonCount++;
            }
        }

        int buttonIndex = 0;
        for (Node child : getItems()) {
            if (child instanceof M3Button button) {
                button.setSize(getSize());
                M3ControlStyles.add(button, GROUPED_BUTTON_STYLE_CLASS);
                M3ControlStyles.replaceVariant(
                        button,
                        buttonStyleClass(buttonIndex, buttonCount),
                        SINGLE_BUTTON_STYLE_CLASS,
                        FIRST_BUTTON_STYLE_CLASS,
                        MIDDLE_BUTTON_STYLE_CLASS,
                        LAST_BUTTON_STYLE_CLASS
                );
                button.requestLayout();
                buttonIndex++;
            }
        }
    }

    /// Returns the position style class for a grouped button index.
    private static String buttonStyleClass(int index, int count) {
        if (count == 1) {
            return SINGLE_BUTTON_STYLE_CLASS;
        }
        if (index == 0) {
            return FIRST_BUTTON_STYLE_CLASS;
        }
        if (index == count - 1) {
            return LAST_BUTTON_STYLE_CLASS;
        }
        return MIDDLE_BUTTON_STYLE_CLASS;
    }

    /// Applies the variant style class to this button group.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3ButtonGroupVariant.STANDARD.styleClass(),
                M3ButtonGroupVariant.CONNECTED.styleClass()
        );
    }

    /// Applies the size style class to this button group.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                sizeStyleClass(getSize()),
                sizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                sizeStyleClass(M3ButtonSize.SMALL),
                sizeStyleClass(M3ButtonSize.MEDIUM),
                sizeStyleClass(M3ButtonSize.LARGE),
                sizeStyleClass(M3ButtonSize.EXTRA_LARGE)
        );
    }

    /// Returns the button-group style class for one Material button size.
    ///
    /// @param size the Material button size
    /// @return the button-group size style class
    private static String sizeStyleClass(M3ButtonSize size) {
        return "m3-button-group-" + size.cssSuffix();
    }

    /// Removes all button group style classes from a button.
    private static void clearButtonStyle(M3Button button) {
        button.getStyleClass().remove(GROUPED_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(SINGLE_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(FIRST_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(MIDDLE_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(LAST_BUTTON_STYLE_CLASS);
        button.requestLayout();
    }

    /// Creates the default Material Design 3 button group skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ButtonGroupSkin(this);
    }

    /// CSS metadata for button group layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for grouped button spacing.
        private static final CssMetaData<M3ButtonGroup, Number> SPACING =
                new CssMetaData<>("-m3-button-group-spacing", SizeConverter.getInstance(), DEFAULT_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ButtonGroup control) {
                        return M3Css.isSettable(control.spacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ButtonGroup control) {
                        return control.spacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
