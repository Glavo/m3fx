// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorSliderSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides a linear editor for one channel of a color.
///
/// Direct adjustment stores [#channelProperty()] in a compatible color space. Red, green, and blue use RGB;
/// lightness uses HSL; and brightness uses HSB. Hue and saturation preserve HSL when the current value is HSL and
/// otherwise use HSB. Alpha preserves the current color space. Assigning [#valueProperty()] programmatically does
/// not perform a conversion.
///
/// For a horizontal slider, the channel increases from the logical start edge to the logical end edge and therefore
/// mirrors in right-to-left layouts. For a vertical slider, the minimum is at the bottom and the maximum is at the
/// top. Home and End select the channel minimum and maximum. Arrow keys use the channel's unit increment, while
/// Page Up and Page Down use its block increment.
///
/// [#valueChangingProperty()] is `true` while a recognized primary-button gesture is active. Applications may
/// observe its transition to `false` as a commit boundary while observing [#valueProperty()] for live updates.
/// This control does not fire a separate action event when an adjustment is completed.
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a corresponding standard component.
@NotNullByDefault
public final class M3ColorSlider extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-slider";

    /// The initial fully saturated red value.
    private static final M3Color DEFAULT_VALUE = new M3HsbColor(0.0, 1.0, 1.0);

    /// The optional formatted-value accessibility attribute provided by newer JavaFX releases.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// Creates a horizontal hue slider initialized to opaque red.
    public M3ColorSlider() {
        initialize();
    }

    /// Creates a horizontal slider for one channel, initialized to opaque red.
    ///
    /// @param channel the edited channel
    /// @throws NullPointerException if `channel` is `null`
    public M3ColorSlider(M3ColorChannel channel) {
        this();
        setChannel(channel);
    }

    /// The current color.
    ///
    /// Direct adjustment stores a value in the color space compatible with [#channelProperty()]. Programmatic
    /// assignment stores the supplied value without conversion.
    ///
    /// @defaultValue fully saturated opaque red in HSB
    private final ObjectProperty<M3Color> value =
            M3ColorProperties.nonNullObjectProperty(this, "value", DEFAULT_VALUE, this::handleValueInvalidated);

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
    /// supply non-null values. While the property is unidirectionally bound, direct pointer, keyboard, and
    /// accessibility adjustments cannot write their result and will fail according to the JavaFX bound-property
    /// contract.
    ///
    /// @return the current-color property
    public ObjectProperty<M3Color> valueProperty() {
        return value;
    }

    /// The channel edited by direct interaction.
    ///
    /// @defaultValue [M3ColorChannel#HUE]
    private final ObjectProperty<M3ColorChannel> channel = M3ColorProperties.nonNullObjectProperty(
            this,
            "channel",
            M3ColorChannel.HUE,
            this::handleChannelInvalidated
    );

    /// Returns the channel edited by this slider.
    ///
    /// @return the non-null edited channel
    public M3ColorChannel getChannel() {
        return channel.get();
    }

    /// Sets the channel edited by this slider.
    ///
    /// Changing the channel does not change [#getValue()]. The current color is converted to a compatible color
    /// space only for presentation and when the next direct adjustment is stored.
    ///
    /// @param channel the non-null channel to edit
    /// @throws NullPointerException if `channel` is `null`
    /// @throws RuntimeException if [#channelProperty()] is unidirectionally bound
    public void setChannel(M3ColorChannel channel) {
        this.channel.set(channel);
    }

    /// Returns the property containing the edited channel.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the edited-channel property
    public ObjectProperty<M3ColorChannel> channelProperty() {
        return channel;
    }

    /// The slider orientation.
    ///
    /// @defaultValue [Orientation#HORIZONTAL]
    private final ObjectProperty<Orientation> orientation = M3ColorProperties.nonNullObjectProperty(
            this,
            "orientation",
            Orientation.HORIZONTAL,
            this::handleOrientationInvalidated
    );

    /// Returns the orientation of this slider.
    ///
    /// @return the non-null orientation
    public Orientation getOrientation() {
        return orientation.get();
    }

    /// Sets the orientation of this slider.
    ///
    /// This operation changes geometry but does not change [#getValue()].
    ///
    /// @param orientation the non-null orientation
    /// @throws NullPointerException if `orientation` is `null`
    /// @throws RuntimeException if [#orientationProperty()] is unidirectionally bound
    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    /// Returns the property containing the slider orientation.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the orientation property
    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
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
        return new M3ColorSliderSkin(this);
    }

    /// Returns accessibility information for the edited channel.
    ///
    /// The minimum, maximum, and current value use the unit of [#getChannel()]. The orientation attribute is
    /// [#getOrientation()].
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            return M3ColorAccessibility.channelValue(getChannel(), accessibleChannelValue());
        }
        M3ColorChannel currentChannel = getChannel();
        return switch (attribute) {
            case MIN_VALUE -> currentChannel.getMinimum();
            case MAX_VALUE -> currentChannel.getMaximum();
            case VALUE -> accessibleChannelValue();
            case ORIENTATION -> getOrientation();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for channel adjustment.
    ///
    /// Increment and decrement actions use the channel's unit increment; block actions use its block increment.
    /// A set-value action uses the first numeric parameter and constrains it to the channel range; it has no effect
    /// when no numeric parameter is present. Adjustment actions have no effect while this control is disabled.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if `action` is `null`, or if `parameters` is `null` for a set-value action
    /// @throws IllegalArgumentException if a numeric set-value parameter is non-finite
    /// @throws RuntimeException if an adjustment writes to a unidirectionally bound [#valueProperty()]
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        M3ColorChannel currentChannel = getChannel();
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showDirectItem(this, this);
            case INCREMENT -> adjustAccessibleValue(currentChannel.getUnitIncrement());
            case DECREMENT -> adjustAccessibleValue(-currentChannel.getUnitIncrement());
            case BLOCK_INCREMENT -> adjustAccessibleValue(currentChannel.getBlockIncrement());
            case BLOCK_DECREMENT -> adjustAccessibleValue(-currentChannel.getBlockIncrement());
            case SET_VALUE -> setAccessibleValue(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the user-agent stylesheet for color sliders.
    ///
    /// @return the color-slider stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-slider.css");
    }

    /// Initializes style, accessibility, and traversal state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.SLIDER);
        setAccessibleText("Color channel");
        M3Accessible.installAccessibleActionRoute(this, () -> M3Accessible.showDirectItem(this, this), null);
        setFocusTraversable(true);
    }

    /// Requests presentation and accessibility updates after the color value changes.
    private void handleValueInvalidated() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
        M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
    }

    /// Requests presentation and accessibility updates after the edited channel changes.
    private void handleChannelInvalidated() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.MIN_VALUE);
        notifyAccessibleAttributeChanged(AccessibleAttribute.MAX_VALUE);
        notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
        M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
    }

    /// Requests presentation and accessibility updates after the orientation changes.
    private void handleOrientationInvalidated() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.ORIENTATION);
    }

    /// Returns the current channel value in its compatible editing color space.
    private double accessibleChannelValue() {
        M3ColorChannel currentChannel = getChannel();
        M3ColorSpace editingSpace = M3ColorMath.editingSpace(getValue(), currentChannel);
        return getValue().toColorSpace(editingSpace).getChannel(currentChannel);
    }

    /// Adds an accessibility increment and constrains the result to the channel range.
    private void adjustAccessibleValue(double increment) {
        M3ColorChannel currentChannel = getChannel();
        setAccessibleChannelValue(currentChannel.constrain(accessibleChannelValue() + increment));
    }

    /// Stores one channel value in its compatible editing color space.
    private void setAccessibleChannelValue(double channelValue) {
        M3ColorChannel currentChannel = getChannel();
        M3ColorSpace editingSpace = M3ColorMath.editingSpace(getValue(), currentChannel);
        setValue(M3ColorMath.withChannel(
                getValue(),
                editingSpace,
                currentChannel,
                currentChannel.constrain(channelValue)
        ));
    }

    /// Applies the first numeric value supplied by an accessibility client.
    private void setAccessibleValue(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (parameter instanceof Number number) {
                setAccessibleChannelValue(number.doubleValue());
                return;
            }
        }
    }
}
