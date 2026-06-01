// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.FXCollections;
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
/// `M3ButtonGroup` lays out [M3Button] children as a connected row and applies first, middle, last, and single
/// style classes so skins and CSS can render shared outlines and joined shapes. Use it when commands should be
/// visually grouped but remain independent buttons.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/overview).
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

    /// The default spacing that lets adjacent grouped button borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

    /// The mutable button group content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between grouped buttons.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getItems()));

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
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        focusNotifier.refresh();
    };

    /// Updates physical edge style classes when the effective layout direction changes.
    private final ChangeListener<NodeOrientation> effectiveNodeOrientationListener =
            (observable, oldValue, newValue) -> updateButtonStyles();

    /// Creates an empty button group.
    public M3ButtonGroup() {
        initialize();
    }

    /// Creates a button group with the supplied buttons.
    ///
    /// @param buttons the buttons displayed by the group
    public M3ButtonGroup(M3Button... buttons) {
        initialize();
        addButtons(buttons);
    }

    /// Returns the mutable child list used as button group content.
    ///
    /// @return the mutable child list used as button group content
    public final ObservableList<Node> getItems() {
        return items;
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
            spacing = new StyleableDoubleProperty(DEFAULT_SPACING) {
                /// Validates updated spacing values.
                @Override
                protected void invalidated() {
                    M3Css.finite(get(), "spacing");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3ButtonGroup.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "spacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3ButtonGroup, Number> getCssMetaData() {
                    return StyleableProperties.SPACING;
                }
            };
        }
        return spacing;
    }

    /// Adds one button to the group.
    ///
    /// @param button the button to add
    public final void addButton(M3Button button) {
        getItems().add(Objects.requireNonNull(button, "button"));
    }

    /// Adds buttons to the group.
    ///
    /// @param buttons the buttons to add
    public final void addButtons(M3Button... buttons) {
        validateButtons(buttons);
        getItems().addAll(buttons);
    }

    /// Replaces all grouped buttons.
    ///
    /// @param buttons the replacement grouped buttons
    public final void setButtons(M3Button... buttons) {
        validateButtons(buttons);
        getItems().setAll(buttons);
    }

    /// Removes all button group content.
    public final void clearItems() {
        getItems().clear();
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
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showCurrentOrItem(this, getItems());
            case SHOW_ITEM -> M3Accessible.showCurrentOrItem(this, getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
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
                getItems(),
                M3SelectionNavigation.focused(getItems(), M3Button.class),
                M3Button.class,
                true,
                false,
                M3SelectionNavigation.isRightToLeft(this)
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

        boolean rightToLeft = getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        int buttonIndex = 0;
        for (Node child : getItems()) {
            if (child instanceof M3Button button) {
                int visualButtonIndex = rightToLeft ? buttonCount - buttonIndex - 1 : buttonIndex;
                M3ControlStyles.add(button, GROUPED_BUTTON_STYLE_CLASS);
                M3ControlStyles.replaceVariant(
                        button,
                        buttonStyleClass(visualButtonIndex, buttonCount),
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

    /// Validates a button array.
    private static void validateButtons(M3Button... buttons) {
        Objects.requireNonNull(buttons, "buttons");
        for (M3Button button : buttons) {
            Objects.requireNonNull(button, "button");
        }
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
