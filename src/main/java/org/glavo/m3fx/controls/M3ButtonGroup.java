// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
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

/// A Material Design 3 group of adjacent, related action buttons.
///
/// A group owns an ordered, live list of JavaFX [ButtonBase] nodes. The [variant][#variantProperty()] selects
/// separated or connected presentation, while [size][#sizeProperty()] applies a common Material size to compatible
/// M3FX buttons. Standard groups may redistribute width around an armed or selected item according to
/// [standardPressedWidthMultiplier][#standardPressedWidthMultiplierProperty()]; connected groups coordinate the
/// outer and inner shapes of adjacent buttons.
///
/// The group itself is not focus traversable. Arrow-key traversal and accessibility navigation operate on
/// reachable buttons in visual order and respect node orientation. A button can belong to only one scene-graph
/// parent and therefore cannot simultaneously be displayed in another container.
///
/// See [Material Design button groups](https://m3.material.io/components/button-groups/overview).
@NotNullByDefault
public final class M3ButtonGroup extends Control {
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

    /// The default proportional width increase for an activated standard-group button.
    private static final double DEFAULT_STANDARD_PRESSED_WIDTH_MULTIPLIER = 0.15;

    /// Marks grouped buttons whose container shape is controlled by a connected button group.
    private static final PseudoClass CONNECTED_GROUP_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("connected-group");

    /// The live, mutable list of buttons in visual order.
    ///
    /// The list rejects `null`, preserves insertion order, and is observed for subsequent changes. Removing a
    /// button also removes grouping-specific state from that button.
    private final ObservableList<ButtonBase> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between grouped buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getItems()));

    /// The visual grouping model used for the items.
    ///
    /// The default value is [M3ButtonGroupVariant#CONNECTED]. The property never reports `null`; a direct `null`
    /// assignment restores the default.
    ///
    /// @defaultValue [M3ButtonGroupVariant#CONNECTED]
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

    /// The common Material size applied to compatible grouped buttons.
    ///
    /// The default value is [M3ButtonSize#SMALL]. The property never reports `null`; a direct `null` assignment
    /// restores the default.
    ///
    /// @defaultValue [M3ButtonSize#SMALL]
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

    /// The spacing between adjacent button bounds, in logical pixels.
    ///
    /// The default value is `-1.0`, allowing adjacent borders to overlap. Any finite value is accepted, including
    /// negative values.
    ///
    /// @defaultValue `-1.0`
    private @Nullable StyleableDoubleProperty spacing;

    /// The proportional width increase assigned to the active item in a standard group.
    ///
    /// The default value is `0.15`. Values must be finite and non-negative. This property has no effect on a
    /// connected group.
    ///
    /// @defaultValue `0.15`
    private @Nullable StyleableDoubleProperty standardPressedWidthMultiplierStyleable;

    /// Updates grouped button position style classes when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof ButtonBase button) {
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

    /// Creates an empty, connected, small button group.
    public M3ButtonGroup() {
        initialize();
    }

    /// Returns the live list of buttons displayed by this group.
    ///
    /// Changes to the returned list are reflected immediately. The list preserves insertion order and rejects
    /// `null` elements.
    ///
    /// @return the live, mutable button list
    public final ObservableList<ButtonBase> getItems() {
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
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3ButtonGroupVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

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
    /// @throws NullPointerException if `size` is `null`
    public final void setSize(M3ButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    public final ObjectProperty<M3ButtonSize> sizeProperty() {
        return size;
    }

    /// Returns the spacing between grouped buttons.
    ///
    /// @return the child spacing in logical pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between grouped buttons.
    ///
    /// @param spacing the child spacing in logical pixels
    /// @throws IllegalArgumentException if `spacing` is not finite
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.finite(spacing, "spacing"));
    }

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

    /// Returns the proportional width increase applied to an activated button in a standard group.
    ///
    /// @return the pressed width multiplier
    public final double getStandardPressedWidthMultiplier() {
        return standardPressedWidthMultiplierStyleable == null
                ? DEFAULT_STANDARD_PRESSED_WIDTH_MULTIPLIER
                : standardPressedWidthMultiplierStyleable.get();
    }

    /// Sets the proportional width increase applied to an activated button in a standard group.
    ///
    /// @param multiplier the non-negative pressed width multiplier
    /// @throws IllegalArgumentException if `multiplier` is negative or not finite
    public final void setStandardPressedWidthMultiplier(double multiplier) {
        standardPressedWidthMultiplierProperty().set(
                M3Css.nonNegative(multiplier, "standardPressedWidthMultiplier")
        );
    }

    /// Returns the standard-group pressed width multiplier property.
    ///
    /// @return the styleable pressed width multiplier property
    public final StyleableDoubleProperty standardPressedWidthMultiplierProperty() {
        if (standardPressedWidthMultiplierStyleable == null) {
            standardPressedWidthMultiplierStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_STANDARD_PRESSED_WIDTH_MULTIPLIER,
                    this,
                    "standardPressedWidthMultiplier",
                    StyleableProperties.STANDARD_PRESSED_WIDTH_MULTIPLIER,
                    this::requestLayout
            );
        }
        return standardPressedWidthMultiplierStyleable;
    }

    /// Returns a content-hugging maximum width for standard groups and a flexible maximum for connected groups.
    ///
    /// @param height the height constraint, or `-1` when unconstrained
    /// @return the computed maximum width
    @Override
    protected double computeMaxWidth(double height) {
        return getVariant() == M3ButtonGroupVariant.STANDARD
                ? prefWidth(height)
                : super.computeMaxWidth(height);
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
    ///
    /// @throws NullPointerException if `attribute` is `null`
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
        M3ControlStyles.initialize(this, STYLE_CLASS);
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
                M3SelectionNavigation.focused(getItems(), ButtonBase.class),
                ButtonBase.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this)
        );
    }

    /// Applies first, middle, last, or single button style classes.
    private void updateButtonStyles() {
        int buttonCount = 0;
        for (Node child : getItems()) {
            if (child instanceof ButtonBase) {
                buttonCount++;
            }
        }

        int buttonIndex = 0;
        for (Node child : getItems()) {
            if (child instanceof ButtonBase button) {
                applyButtonSize(button);
                M3ControlStyles.add(button, GROUPED_BUTTON_STYLE_CLASS);
                M3ControlStyles.replaceVariant(
                        button,
                        sizeStyleClass(getSize()),
                        sizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                        sizeStyleClass(M3ButtonSize.SMALL),
                        sizeStyleClass(M3ButtonSize.MEDIUM),
                        sizeStyleClass(M3ButtonSize.LARGE),
                        sizeStyleClass(M3ButtonSize.EXTRA_LARGE)
                );
                button.pseudoClassStateChanged(
                        CONNECTED_GROUP_PSEUDO_CLASS,
                        getVariant() == M3ButtonGroupVariant.CONNECTED
                );
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

    /// Applies the group size to a supported Material button child.
    private void applyButtonSize(ButtonBase button) {
        if (button instanceof M3Button materialButton) {
            materialButton.setSize(getSize());
        } else if (button instanceof M3IconToggleButton toggleButton) {
            toggleButton.setSize(getSize());
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
    private static void clearButtonStyle(ButtonBase button) {
        button.pseudoClassStateChanged(CONNECTED_GROUP_PSEUDO_CLASS, false);
        button.getStyleClass().remove(GROUPED_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(SINGLE_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(FIRST_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(MIDDLE_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(LAST_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(sizeStyleClass(M3ButtonSize.EXTRA_SMALL));
        button.getStyleClass().remove(sizeStyleClass(M3ButtonSize.SMALL));
        button.getStyleClass().remove(sizeStyleClass(M3ButtonSize.MEDIUM));
        button.getStyleClass().remove(sizeStyleClass(M3ButtonSize.LARGE));
        button.getStyleClass().remove(sizeStyleClass(M3ButtonSize.EXTRA_LARGE));
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

        /// CSS metadata for the standard-group pressed width multiplier.
        private static final CssMetaData<M3ButtonGroup, Number> STANDARD_PRESSED_WIDTH_MULTIPLIER =
                new CssMetaData<>(
                        "-m3-button-group-standard-pressed-width-multiplier",
                        SizeConverter.getInstance(),
                        DEFAULT_STANDARD_PRESSED_WIDTH_MULTIPLIER
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ButtonGroup control) {
                        return M3Css.isSettable(control.standardPressedWidthMultiplierProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ButtonGroup control) {
                        return control.standardPressedWidthMultiplierProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SPACING);
            styleables.add(STANDARD_PRESSED_WIDTH_MULTIPLIER);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
