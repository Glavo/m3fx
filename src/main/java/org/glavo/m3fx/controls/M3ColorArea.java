// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorAreaSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides a two-dimensional editor for two channels of a color.
///
/// The [color plane][#planeProperty()] defines the editing color space and assigns one channel to each axis. Direct
/// pointer or keyboard adjustment converts the current value to that color space, updates the configured axes, and
/// retains the remaining channel and alpha. Assigning [#valueProperty()] programmatically does not perform this
/// conversion.
///
/// A primary-button press begins continuous adjustment and updates both axes. Left and Right adjust the horizontal
/// channel; Up and Down adjust the vertical channel. Holding Shift selects each channel's block increment.
/// Page Up and Page Down adjust the vertical channel by its block increment, while Home and End set the horizontal
/// channel to its minimum or maximum. Horizontal geometry and Left/Right behavior mirror in right-to-left layouts.
///
/// [#valueChangingProperty()] is `true` while a recognized primary-button gesture is active. Applications may
/// observe its transition to `false` as a commit boundary while observing [#valueProperty()] for live updates.
/// This control does not fire a separate action event when an adjustment is completed.
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a corresponding standard component.
@NotNullByDefault
public final class M3ColorArea extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-area";

    /// The initial fully saturated red value.
    private static final M3Color DEFAULT_VALUE = new M3HsbColor(0.0, 1.0, 1.0);

    /// The optional formatted-value accessibility attribute provided by newer JavaFX releases.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// Creates an HSB saturation-and-brightness area initialized to opaque red.
    public M3ColorArea() {
        initialize();
    }

    /// Creates an HSB saturation-and-brightness area with an initial value.
    ///
    /// The supplied value is stored without conversion. It is converted to the configured color plane only when the
    /// user directly adjusts the area.
    ///
    /// @param value the initial color
    /// @throws NullPointerException if `value` is `null`
    public M3ColorArea(M3Color value) {
        this();
        setValue(value);
    }

    /// The current color.
    ///
    /// Direct adjustment stores a value in the color space selected by [#planeProperty()]. Programmatic assignment
    /// stores the supplied value without conversion.
    ///
    /// @defaultValue fully saturated opaque red in HSB
    private final ObjectProperty<M3Color> value = M3ColorProperties.nonNullObjectProperty(
            this,
            "value",
            DEFAULT_VALUE,
            () -> {
                requestLayout();
                M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
            }
    );

    /// Returns the current color.
    ///
    /// @return the current non-null color
    public M3Color getValue() {
        return value.get();
    }

    /// Sets the current color without converting its color space.
    ///
    /// @param value the non-null color to store
    /// @throws NullPointerException if `value` is `null`
    /// @throws RuntimeException if [#valueProperty()] is unidirectionally bound
    public void setValue(M3Color value) {
        this.value.set(value);
    }

    /// Returns the property containing the current color.
    ///
    /// The property is never `null` when changed through [#setValue(M3Color)]. A unidirectional binding must also
    /// supply non-null values. While the property is unidirectionally bound, direct pointer and keyboard
    /// adjustments cannot write their result and will fail according to the JavaFX bound-property contract.
    ///
    /// @return the current-color property
    public ObjectProperty<M3Color> valueProperty() {
        return value;
    }

    /// The color space and two axes used for direct adjustment.
    ///
    /// @defaultValue [M3ColorPlane#HSB_SATURATION_BRIGHTNESS]
    private final ObjectProperty<M3ColorPlane> plane = M3ColorProperties.nonNullObjectProperty(
            this,
            "plane",
            M3ColorPlane.HSB_SATURATION_BRIGHTNESS,
            () -> {
                requestLayout();
                M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
            }
    );

    /// Returns the color plane used for direct adjustment.
    ///
    /// @return the non-null color plane
    public M3ColorPlane getPlane() {
        return plane.get();
    }

    /// Sets the color plane used for direct adjustment.
    ///
    /// Changing the plane does not change [#getValue()]. The current color is converted to the new plane's color
    /// space only for presentation and when the next direct adjustment is stored.
    ///
    /// @param plane the non-null color plane
    /// @throws NullPointerException if `plane` is `null`
    /// @throws RuntimeException if [#planeProperty()] is unidirectionally bound
    public void setPlane(M3ColorPlane plane) {
        this.plane.set(plane);
    }

    /// Returns the property containing the color plane.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the color-plane property
    public ObjectProperty<M3ColorPlane> planeProperty() {
        return plane;
    }

    /// Whether a primary-button gesture is currently changing the value.
    ///
    /// @defaultValue `false`
    private final BooleanProperty valueChanging = new SimpleBooleanProperty(this, "valueChanging");

    /// Returns whether a primary-button adjustment is active.
    ///
    /// @return `true` during a primary-button drag
    public boolean isValueChanging() {
        return valueChanging.get();
    }

    /// Sets whether a continuous adjustment is active.
    ///
    /// This method changes only the interaction marker; it neither changes [#valueProperty()] nor fires an action
    /// event. Applications that set the property to `true` for an external gesture must restore it to `false` when
    /// the gesture ends. The control updates this property automatically for its primary-button interaction.
    ///
    /// @param valueChanging whether a direct interaction is active
    /// @throws RuntimeException if [#valueChangingProperty()] is unidirectionally bound
    public void setValueChanging(boolean valueChanging) {
        this.valueChanging.set(valueChanging);
    }

    /// Returns the property that marks a continuous primary-button adjustment.
    ///
    /// Applications normally observe rather than bind this property. A unidirectional binding prevents the control
    /// from maintaining the marker for its built-in pointer interaction.
    ///
    /// @return the `valueChanging` property
    public BooleanProperty valueChangingProperty() {
        return valueChanging;
    }

    /// Creates the default visual representation of this control.
    ///
    /// @return the non-null default skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ColorAreaSkin(this);
    }

    /// Returns accessibility information for the two edited channel values.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            M3ColorPlane currentPlane = getPlane();
            M3Color converted = getValue().toColorSpace(currentPlane.colorSpace());
            return M3ColorAccessibility.channelValue(
                    currentPlane.xChannel(),
                    converted.getChannel(currentPlane.xChannel())
            ) + ", " + M3ColorAccessibility.channelValue(
                    currentPlane.yChannel(),
                    converted.getChannel(currentPlane.yChannel())
            );
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    /// Executes accessibility focus requests for the two-dimensional editor.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (action == AccessibleAction.REQUEST_FOCUS && !isDisabled()) {
            M3Accessible.showDirectItem(this, this);
        } else {
            super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the user-agent stylesheet for color areas.
    ///
    /// @return the color-area stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-area.css");
    }

    /// Initializes style, accessibility, and traversal state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setAccessibleText("Color area");
        M3Accessible.installAccessibleActionRoute(this, () -> M3Accessible.showDirectItem(this, this), null);
        setFocusTraversable(true);
    }
}
