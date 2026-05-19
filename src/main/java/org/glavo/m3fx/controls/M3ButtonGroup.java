// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 button group for adjacent related action buttons.
@NotNullByDefault
public class M3ButtonGroup extends HBox {
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

    /// The default spacing that lets adjacent button borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

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
    };

    /// Creates an empty button group.
    public M3ButtonGroup() {
        initialize();
    }

    /// Creates a button group with the supplied buttons.
    public M3ButtonGroup(M3Button... buttons) {
        initialize();
        addButtons(buttons);
    }

    /// Returns the mutable child list used as button group content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Adds one button to the group.
    public final void addButton(M3Button button) {
        getItems().add(Objects.requireNonNull(button, "button"));
    }

    /// Adds buttons to the group.
    public final void addButtons(M3Button... buttons) {
        validateButtons(buttons);
        getItems().addAll(buttons);
    }

    /// Replaces all grouped buttons.
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

    /// Returns accessibility attributes for grouped button content.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for grouped button content.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SHOW_ITEM -> M3Accessible.showItem(getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setSpacing(DEFAULT_SPACING);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getChildren().addListener(childrenListener);
        updateButtonStyles();
    }

    /// Applies keyboard focus navigation across enabled grouped buttons.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeyFocus(
                event,
                getChildren(),
                M3SelectionNavigation.focused(getChildren(), M3Button.class),
                M3Button.class,
                true,
                false
        );
    }

    /// Applies first, middle, last, or single button style classes.
    private void updateButtonStyles() {
        int buttonCount = 0;
        for (Node child : getChildren()) {
            if (child instanceof M3Button) {
                buttonCount++;
            }
        }

        int buttonIndex = 0;
        for (Node child : getChildren()) {
            if (child instanceof M3Button button) {
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

    /// Removes all button group style classes from a button.
    private static void clearButtonStyle(M3Button button) {
        button.getStyleClass().remove(GROUPED_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(SINGLE_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(FIRST_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(MIDDLE_BUTTON_STYLE_CLASS);
        button.getStyleClass().remove(LAST_BUTTON_STYLE_CLASS);
        button.requestLayout();
    }

    /// Validates a button array.
    private static void validateButtons(M3Button... buttons) {
        Objects.requireNonNull(buttons, "buttons");
        for (M3Button button : buttons) {
            Objects.requireNonNull(button, "button");
        }
    }
}
