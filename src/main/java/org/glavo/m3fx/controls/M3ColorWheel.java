// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorWheelSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides a circular editor for the hue channel of a color.
///
/// Direct adjustment preserves an HSL value as HSL and edits any other value in HSB. Assigning
/// [#valueProperty()] programmatically does not perform a conversion. Hue increases clockwise: zero degrees is at
/// the top, 90 degrees at the right, 180 degrees at the bottom, and 270 degrees at the left. This physical
/// arrangement and arrow-key meaning do not mirror in right-to-left layouts.
///
/// Arrow keys adjust hue by one degree and wrap at the endpoints. Holding Shift changes the arrow-key increment to
/// ten degrees; Page Up and Page Down also adjust by ten degrees. Home selects zero degrees and End selects
/// 360 degrees. Although zero and 360 degrees render identically, the selected endpoint is retained in the color
/// value.
///
/// [#valueChangingProperty()] is `true` while a recognized primary-button gesture is active. Applications may
/// observe its transition to `false` as a commit boundary while observing [#valueProperty()] for live updates.
/// This control does not fire a separate action event when an adjustment is completed.
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a corresponding standard component.
@NotNullByDefault
public final class M3ColorWheel extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-wheel";

    /// The initial fully saturated red value.
    private static final M3Color DEFAULT_VALUE = new M3HsbColor(0.0, 1.0, 1.0);

    /// The default hue-track thickness.
    private static final double DEFAULT_TRACK_THICKNESS = 24.0;

    /// The optional formatted-value accessibility attribute provided by newer JavaFX releases.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// Creates a color wheel initialized to opaque red.
    public M3ColorWheel() {
        initialize();
    }

    /// Creates a color wheel with an initial value.
    ///
    /// The supplied value is stored without conversion. It is converted to HSL or HSB only when the user directly
    /// adjusts the wheel.
    ///
    /// @param value the initial color
    /// @throws NullPointerException if `value` is `null`
    public M3ColorWheel(M3Color value) {
        this();
        setValue(value);
    }

    /// The current color.
    ///
    /// Direct adjustment preserves HSL and otherwise stores HSB. Programmatic assignment stores the supplied value
    /// without conversion.
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

    /// The visible hue-ring thickness in JavaFX logical pixels.
    ///
    /// @defaultValue `24.0`
    private final DoubleProperty trackThickness = M3ColorProperties.nonNegativeDoubleProperty(
            this,
            "trackThickness",
            DEFAULT_TRACK_THICKNESS,
            this::requestLayout
    );

    /// Returns the hue-ring thickness.
    ///
    /// @return the finite, non-negative thickness in JavaFX logical pixels
    public double getTrackThickness() {
        return trackThickness.get();
    }

    /// Sets the hue-ring thickness.
    ///
    /// A value of zero is permitted and produces a zero-thickness hue ring.
    ///
    /// @param trackThickness the finite, non-negative thickness in JavaFX logical pixels
    /// @throws IllegalArgumentException if the value is negative or not finite
    /// @throws RuntimeException if [#trackThicknessProperty()] is unidirectionally bound
    public void setTrackThickness(double trackThickness) {
        this.trackThickness.set(trackThickness);
    }

    /// Returns the property containing the hue-ring thickness.
    ///
    /// A unidirectional binding must supply finite, non-negative values.
    ///
    /// @return the track-thickness property
    public DoubleProperty trackThicknessProperty() {
        return trackThickness;
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
        return new M3ColorWheelSkin(this);
    }

    /// Returns accessibility information for the hue value.
    ///
    /// The reported minimum and maximum are zero and 360 degrees.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            return M3ColorAccessibility.channelValue(M3ColorChannel.HUE, hue());
        }
        return switch (attribute) {
            case MIN_VALUE -> M3ColorChannel.HUE.getMinimum();
            case MAX_VALUE -> M3ColorChannel.HUE.getMaximum();
            case VALUE -> hue();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for hue adjustment.
    ///
    /// Increment and decrement actions adjust by one degree and wrap at the endpoints; block actions adjust by ten
    /// degrees and also wrap. A set-value action uses the first numeric parameter and constrains it to zero through
    /// 360 degrees; it has no effect when no numeric parameter is present. Adjustment actions have no effect while
    /// this control is disabled.
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

        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showDirectItem(this, this);
            case INCREMENT -> adjustAccessibleHue(M3ColorChannel.HUE.getUnitIncrement());
            case DECREMENT -> adjustAccessibleHue(-M3ColorChannel.HUE.getUnitIncrement());
            case BLOCK_INCREMENT -> adjustAccessibleHue(M3ColorChannel.HUE.getBlockIncrement());
            case BLOCK_DECREMENT -> adjustAccessibleHue(-M3ColorChannel.HUE.getBlockIncrement());
            case SET_VALUE -> setAccessibleHue(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the user-agent stylesheet for color wheels.
    ///
    /// @return the color-wheel stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-wheel.css");
    }

    /// Initializes style, accessibility, and traversal state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.SLIDER);
        setAccessibleText("Hue");
        M3Accessible.installAccessibleActionRoute(this, () -> M3Accessible.showDirectItem(this, this), null);
        setFocusTraversable(true);
    }

    /// Requests presentation and accessibility updates after the color value changes.
    private void handleValueInvalidated() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
        M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
    }

    /// Returns the current hue while preserving latent HSL or HSB state.
    private double hue() {
        M3Color value = getValue();
        M3ColorSpace editingSpace = value.getColorSpace() == M3ColorSpace.HSL
                ? M3ColorSpace.HSL
                : M3ColorSpace.HSB;
        return value.toColorSpace(editingSpace).getChannel(M3ColorChannel.HUE);
    }

    /// Adds an accessibility increment and wraps the hue around the physical wheel.
    private void adjustAccessibleHue(double increment) {
        setHue(M3ColorMath.wrapHue(hue() + increment));
    }

    /// Applies the first numeric hue supplied by an accessibility client.
    private void setAccessibleHue(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (parameter instanceof Number number) {
                setHue(M3ColorChannel.HUE.constrain(number.doubleValue()));
                return;
            }
        }
    }

    /// Stores hue in HSL when the current value is HSL and in HSB otherwise.
    private void setHue(double hue) {
        M3Color value = getValue();
        M3ColorSpace editingSpace = value.getColorSpace() == M3ColorSpace.HSL
                ? M3ColorSpace.HSL
                : M3ColorSpace.HSB;
        setValue(M3ColorMath.withChannel(
                value,
                editingSpace,
                M3ColorChannel.HUE,
                hue
        ));
    }
}
