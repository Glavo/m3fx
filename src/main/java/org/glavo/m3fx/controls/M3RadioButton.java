// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3RadioButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 radio button for selecting one option from a set.
///
/// `M3RadioButton` implements JavaFX [Toggle] so it can be grouped with a standard [ToggleGroup] while keeping
/// an M3FX skin and API surface. Activating a selected radio button keeps it selected, matching the usual radio
/// group behavior. The control updates JavaFX accessibility toggle attributes and renders Material state layers,
/// focus indication, and ripple feedback.
///
/// Use radio buttons when all available options should remain visible. See
/// [Material Design radio buttons](https://m3.material.io/components/radio-button/overview).
@NotNullByDefault
public class M3RadioButton extends ButtonBase implements Toggle {
    /// The base style class for m3fx radio buttons.
    public static final String STYLE_CLASS = "m3-radio-button";

    /// The selected pseudo-class used by radio buttons.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default radio button touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 40.0;

    // The styleable touch target size token.
    private @Nullable StyleableDoubleProperty touchTargetSize;

    // The selected state property.
    private @Nullable BooleanProperty selected;

    // The toggle group this radio button belongs to.
    private @Nullable ObjectProperty<@Nullable ToggleGroup> toggleGroup;

    /// Creates an empty radio button.
    public M3RadioButton() {
        initialize();
    }

    /// Creates a radio button with text.
    ///
    /// @param text the radio button text
    public M3RadioButton(String text) {
        super(text);
        initialize();
    }

    /// Sets whether this radio button is selected.
    ///
    /// @param selected whether this radio button is selected
    @Override
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns whether this radio button is selected.
    ///
    /// @return `true` when this radio button is selected
    @Override
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Returns the selected state property.
    ///
    /// @return the writable selected state property
    @Override
    public final BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new BooleanPropertyBase(false) {
                /// Updates selected visual state and keeps the toggle group synchronized.
                @Override
                protected void invalidated() {
                    boolean selected = get();
                    pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);

                    ToggleGroup group = getToggleGroup();
                    if (group != null) {
                        if (selected) {
                            group.selectToggle(M3RadioButton.this);
                        } else if (group.getSelectedToggle() == M3RadioButton.this) {
                            group.selectToggle(null);
                        }
                    }
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3RadioButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return selected;
    }

    /// Sets the toggle group that manages this radio button.
    ///
    /// @param toggleGroup the toggle group that manages this radio button, or `null`
    @Override
    public final void setToggleGroup(@Nullable ToggleGroup toggleGroup) {
        toggleGroupProperty().set(toggleGroup);
    }

    /// Returns the toggle group that manages this radio button.
    ///
    /// @return the toggle group that manages this radio button, or `null`
    @Override
    public final @Nullable ToggleGroup getToggleGroup() {
        return toggleGroup == null ? null : toggleGroup.get();
    }

    /// Returns the toggle group property.
    ///
    /// @return the writable toggle group property
    @Override
    public final ObjectProperty<@Nullable ToggleGroup> toggleGroupProperty() {
        if (toggleGroup == null) {
            toggleGroup = new ObjectPropertyBase<>() {
                private @Nullable ToggleGroup oldGroup;
                private boolean updatingGroup;

                /// Keeps JavaFX ToggleGroup membership synchronized with this property.
                @Override
                protected void invalidated() {
                    if (updatingGroup) {
                        return;
                    }

                    @Nullable ToggleGroup newGroup = get();
                    if (newGroup == oldGroup) {
                        return;
                    }

                    updatingGroup = true;
                    try {
                        if (oldGroup != null) {
                            oldGroup.getToggles().remove(M3RadioButton.this);
                        }

                        if (newGroup != null && !newGroup.getToggles().contains(M3RadioButton.this)) {
                            newGroup.getToggles().add(M3RadioButton.this);
                        }
                    } finally {
                        updatingGroup = false;
                        oldGroup = newGroup;
                    }
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3RadioButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "toggleGroup";
                }
            };
        }
        return toggleGroup;
    }

    /// Returns the preferred touch target size token.
    ///
    /// @return the preferred touch target size in pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    ///
    /// @param touchTargetSize the preferred touch target size in pixels
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the preferred touch target size token property.
    ///
    /// @return the styleable preferred touch target size property
    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TOUCH_TARGET_SIZE,
                    this,
                    "touchTargetSize",
                    StyleableProperties.TOUCH_TARGET_SIZE,
                    this::updateMetrics
            );
        }
        return touchTargetSize;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3RadioButton`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Toggles this radio button and fires its action handler.
    @Override
    public void fire() {
        if (isDisabled()) {
            return;
        }

        if (getToggleGroup() == null || !isSelected()) {
            setSelected(!isSelected());
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 radio button skin.
    ///
    /// @return the default Material Design 3 radio button skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3RadioButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Returns accessibility attributes for radio button selection state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case TOGGLE_STATE -> isSelected()
                    ? AccessibleAttribute.ToggleState.CHECKED
                    : AccessibleAttribute.ToggleState.UNCHECKED;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.RADIO_BUTTON);
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        setPickOnBounds(true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getTouchTargetSize();
        setMinHeight(size);
        setPrefHeight(size);
    }

    /// CSS metadata for m3fx radio button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3RadioButton, Number> TOUCH_TARGET_SIZE =
                new CssMetaData<>("-m3-touch-target-size", SizeConverter.getInstance(), DEFAULT_TOUCH_TARGET_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3RadioButton control) {
                        return M3Css.isSettable(control.touchTargetSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3RadioButton control) {
                        return control.touchTargetSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
