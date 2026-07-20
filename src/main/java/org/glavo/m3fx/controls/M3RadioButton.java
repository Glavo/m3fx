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
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3RadioButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// A Material Design 3 radio button for selecting one option from a set.
///
/// A radio button has a selected and an unselected state. When it belongs to a [ToggleGroup], selecting it clears
/// the previously selected toggle in that group. Activating an already selected radio button in a group has no
/// effect, so a group normally retains one selection. An ungrouped radio button may be selected and cleared by
/// repeated activation.
///
/// An [ActionEvent] is fired after a successful user or programmatic activation. The [#selectedProperty()] and
/// [#toggleGroupProperty()] properties also implement the standard JavaFX [Toggle] contract, allowing this control to
/// participate in a group containing other `Toggle` implementations. By default, the radio button is unselected
/// and is not assigned to a group.
///
/// Use radio buttons when all available options should remain visible. See
/// [Material Design radio buttons](https://m3.material.io/components/radio-button/overview).
@NotNullByDefault
public final class M3RadioButton extends ButtonBase implements Toggle {
    /// The default style class assigned to M3FX radio buttons.
    private static final String DEFAULT_STYLE_CLASS = "m3-radio-button";

    /// The selected pseudo-class used by radio buttons.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default radio button touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The default radio button state layer size.
    private static final double DEFAULT_STATE_LAYER_SIZE = 40.0;

    /// The default radio button indicator container size.
    private static final double DEFAULT_CONTAINER_SIZE = 20.0;

    /// The default selected radio dot size.
    private static final double DEFAULT_SELECTED_DOT_SIZE = 10.0;

    /// Creates an unselected radio button with an empty label and no toggle group.
    public M3RadioButton() {
        initialize();
    }

    /// Creates an unselected radio button with the specified label and no toggle group.
    ///
    /// @param text the radio button text
    public M3RadioButton(String text) {
        super(text);
        initialize();
    }

    /// Whether this radio button is selected.
    ///
    /// Setting this property to `true` selects this toggle in [#getToggleGroup()], if present, and clears the
    /// group's previous selection. Setting it to `false` clears the group's selection when this button is the
    /// selected toggle. Direct property mutation and binding participate in the same group synchronization.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty selected;

    /// Returns whether this radio button is selected.
    ///
    /// @return `true` when this radio button is selected
    @Override
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Sets whether this radio button is selected.
    ///
    /// If the button belongs to a toggle group, selecting it updates the group's selected toggle. Clearing the
    /// selected button also clears the group's selected-toggle property.
    ///
    /// @param selected whether this radio button is selected
    @Override
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns the observable, bindable selected-state property.
    ///
    /// The property is `false` by default. Changes update pseudo-class and accessibility state and synchronize the
    /// selected toggle of [#getToggleGroup()], when present.
    ///
    /// @return the selected-state property
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
                    // JavaFX 14 has no aggregate TOGGLE_STATE attribute; the helper is a no-op there.
                    M3Accessible.notifyToggleStateChanged(M3RadioButton.this);

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

    /// The toggle group that coordinates this radio button's selection.
    ///
    /// A `null` value leaves the radio button independent. Changing the property removes the button from the old
    /// group and adds it to the new group. The property remains synchronized when group membership is changed
    /// through [ToggleGroup#getToggles()].
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable ToggleGroup> toggleGroup;

    /// Returns the toggle group that manages this radio button.
    ///
    /// @return the toggle group that manages this radio button, or `null`
    @Override
    public final @Nullable ToggleGroup getToggleGroup() {
        return toggleGroup == null ? null : toggleGroup.get();
    }

    /// Sets the toggle group that manages this radio button.
    ///
    /// Assigning a new group updates membership in both the old and new groups. Passing `null` removes the radio
    /// button from its current group without changing its selected state.
    ///
    /// @param toggleGroup the toggle group that manages this radio button, or `null`
    @Override
    public final void setToggleGroup(@Nullable ToggleGroup toggleGroup) {
        toggleGroupProperty().set(toggleGroup);
    }

    /// Returns the observable, bindable toggle-group property.
    ///
    /// The property is `null` by default. Changes keep this toggle's membership synchronized with the old and new
    /// groups, including membership changes made through [ToggleGroup#getToggles()].
    ///
    /// @return the toggle-group property
    @Override
    public final ObjectProperty<@Nullable ToggleGroup> toggleGroupProperty() {
        if (toggleGroup == null) {
            toggleGroup = new ObjectPropertyBase<>() {
                /// The previously synchronized toggle group.
                private @Nullable ToggleGroup oldGroup;

                /// Guards against reentrant toggle-group membership synchronization.
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

    /// The preferred square touch-target size, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-touch-target-size`.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty touchTargetSize;

    /// Returns the preferred square touch-target size.
    ///
    /// @return the touch-target size in logical pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred square touch-target size.
    ///
    /// @param touchTargetSize the touch-target size in logical pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the observable, bindable, CSS-styleable touch-target size property.
    ///
    /// The property is `48.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-touch-target-size`.
    ///
    /// @return the touch-target size property
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

    /// The bounded state-layer size, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-state-layer-size`.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty stateLayerSize;

    /// Returns the bounded indicator state layer size token.
    ///
    /// @return the state layer size in logical pixels
    public final double getStateLayerSize() {
        return stateLayerSize == null ? DEFAULT_STATE_LAYER_SIZE : stateLayerSize.get();
    }

    /// Sets the bounded indicator state layer size token.
    ///
    /// @param stateLayerSize the state layer size in logical pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setStateLayerSize(double stateLayerSize) {
        stateLayerSizeProperty().set(M3Css.nonNegative(stateLayerSize, "stateLayerSize"));
    }

    /// Returns the observable, bindable, CSS-styleable state-layer size property.
    ///
    /// The property is `40.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-state-layer-size`.
    ///
    /// @return the state-layer size property
    public final StyleableDoubleProperty stateLayerSizeProperty() {
        if (stateLayerSize == null) {
            stateLayerSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_STATE_LAYER_SIZE,
                    this,
                    "stateLayerSize",
                    StyleableProperties.STATE_LAYER_SIZE,
                    this::updateMetrics
            );
        }
        return stateLayerSize;
    }

    /// The radio indicator container size, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-container-size`.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty containerSize;

    /// Returns the radio indicator container size token.
    ///
    /// @return the radio indicator container size in logical pixels
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the radio indicator container size token.
    ///
    /// @param containerSize the radio indicator container size in logical pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the observable, bindable, CSS-styleable indicator-container size property.
    ///
    /// The property is `20.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-container-size`.
    ///
    /// @return the indicator-container size property
    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = createSizeProperty(
                    DEFAULT_CONTAINER_SIZE,
                    "containerSize",
                    StyleableProperties.CONTAINER_SIZE
            );
        }
        return containerSize;
    }

    /// The selected-dot size, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-selected-dot-size`.
    ///
    /// @defaultValue `10.0`
    private @Nullable StyleableDoubleProperty selectedDotSize;

    /// Returns the selected radio dot size token.
    ///
    /// @return the selected radio dot size in logical pixels
    public final double getSelectedDotSize() {
        return selectedDotSize == null ? DEFAULT_SELECTED_DOT_SIZE : selectedDotSize.get();
    }

    /// Sets the selected radio dot size token.
    ///
    /// @param selectedDotSize the selected radio dot size in logical pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setSelectedDotSize(double selectedDotSize) {
        selectedDotSizeProperty().set(M3Css.nonNegative(selectedDotSize, "selectedDotSize"));
    }

    /// Returns the observable, bindable, CSS-styleable selected-dot size property.
    ///
    /// The property is `10.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-selected-dot-size`.
    ///
    /// @return the selected-dot size property
    public final StyleableDoubleProperty selectedDotSizeProperty() {
        if (selectedDotSize == null) {
            selectedDotSize = createSizeProperty(
                    DEFAULT_SELECTED_DOT_SIZE,
                    "selectedDotSize",
                    StyleableProperties.SELECTED_DOT_SIZE
            );
        }
        return selectedDotSize;
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

    /// Toggles this radio button when it is independent, or selects it when it belongs to a group.
    ///
    /// Activating an already selected member of a [ToggleGroup] has no effect and does not fire an action event.
    /// An independent radio button toggles between selected and unselected and fires after each successful change.
    @Override
    public void fire() {
        if (isDisabled() || (getToggleGroup() != null && isSelected())) {
            return;
        }
        setSelected(!isSelected());
        fireEvent(new ActionEvent(this, this));
    }

    /// Creates the default Material Design 3 radio button skin.
    ///
    /// @return the default Material Design 3 radio button skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3RadioButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Returns the initial alignment used before CSS or application code supplies another value.
    ///
    /// @return the initial alignment, [Pos#CENTER_LEFT]
    @Override
    protected Pos getInitialAlignment() {
        return Pos.CENTER_LEFT;
    }

    /// Returns accessibility attributes for radio button selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        // JavaFX 14 has no TOGGLE_STATE enum constant, so test the optional runtime value first.
        if (M3Accessible.isToggleStateAttribute(attribute)) {
            return M3Accessible.toggleState(isSelected());
        }
        if (attribute == AccessibleAttribute.SELECTED) {
            return isSelected();
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.RADIO_BUTTON);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        setPickOnBounds(true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = Math.max(getTouchTargetSize(), getStateLayerSize());
        M3Css.setMinHeightIfUnbound(this, size);
        M3Css.setPrefHeightIfUnbound(this, size);
    }

    /// Creates a non-negative styleable size token property.
    private StyleableDoubleProperty createSizeProperty(
            double initialValue,
            String name,
            CssMetaData<M3RadioButton, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(initialValue, this, name, cssMetaData, this::requestLayout);
    }

    /// CSS metadata for M3FX radio button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3RadioButton, Number> TOUCH_TARGET_SIZE = sizeCssMetaData(
                "-m3-touch-target-size",
                DEFAULT_TOUCH_TARGET_SIZE,
                M3RadioButton::touchTargetSizeProperty
        );

        /// CSS metadata for the state layer size token.
        private static final CssMetaData<M3RadioButton, Number> STATE_LAYER_SIZE = sizeCssMetaData(
                "-m3-state-layer-size",
                DEFAULT_STATE_LAYER_SIZE,
                M3RadioButton::stateLayerSizeProperty
        );

        /// CSS metadata for the radio indicator container size token.
        private static final CssMetaData<M3RadioButton, Number> CONTAINER_SIZE = sizeCssMetaData(
                "-m3-container-size",
                DEFAULT_CONTAINER_SIZE,
                M3RadioButton::containerSizeProperty
        );

        /// CSS metadata for the selected radio dot size token.
        private static final CssMetaData<M3RadioButton, Number> SELECTED_DOT_SIZE = sizeCssMetaData(
                "-m3-selected-dot-size",
                DEFAULT_SELECTED_DOT_SIZE,
                M3RadioButton::selectedDotSizeProperty
        );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            styleables.add(STATE_LAYER_SIZE);
            styleables.add(CONTAINER_SIZE);
            styleables.add(SELECTED_DOT_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for a non-negative size token.
        private static CssMetaData<M3RadioButton, Number> sizeCssMetaData(
                String property,
                double initialValue,
                Function<M3RadioButton, StyleableDoubleProperty> accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3RadioButton control) {
                    return M3Css.isSettable(accessor.apply(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3RadioButton control) {
                    return accessor.apply(control);
                }
            };
        }
    }
}
