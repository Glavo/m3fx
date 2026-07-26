// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorPickerSkin;
import org.jetbrains.annotations.NotNullByDefault;

/// Provides a composed editor for an [M3Color].
///
/// The picker presents a two-dimensional [M3ColorArea] and a hue [M3ColorSlider]. An alpha slider, hue wheel,
/// hexadecimal field, and preset palette can be shown or hidden independently. Direct adjustment through any
/// editor updates [#valueProperty()] and the other visible representations.
///
/// Programmatic assignment retains the supplied color space. Each direct editor applies its own documented
/// conversion rule when it changes a channel. For example, hexadecimal input is converted back to the current
/// color space, while adjusting an RGB channel stores an RGB value. The picker is an inline control: it does not
/// open a popup, persist colors, or maintain a recent-colors history.
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a canonical color picker.
@NotNullByDefault
public final class M3ColorPicker extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-picker";

    /// The initial opaque red value.
    private static final M3Color DEFAULT_VALUE = new M3HsbColor(0.0, 1.0, 1.0);

    /// Creates a picker initialized to opaque red in HSB.
    public M3ColorPicker() {
        initialize();
    }

    /// Creates a picker initialized to a color.
    ///
    /// The supplied value is stored without conversion.
    ///
    /// @param value the initial color
    /// @throws NullPointerException if `value` is `null`
    public M3ColorPicker(M3Color value) {
        this();
        setValue(value);
    }

    /// The color shared by all editors.
    ///
    /// @defaultValue opaque red in HSB
    private final ObjectProperty<M3Color> value =
            M3ColorProperties.nonNullObjectProperty(this, "value", DEFAULT_VALUE);

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
    /// supply non-null values. While the property is unidirectionally bound, direct editor interaction cannot write
    /// a new color and will fail according to the JavaFX bound-property contract.
    ///
    /// @return the color property
    public ObjectProperty<M3Color> valueProperty() {
        return value;
    }

    /// The plane edited by the two-dimensional area.
    ///
    /// @defaultValue [M3ColorPlane#HSB_SATURATION_BRIGHTNESS]
    private final ObjectProperty<M3ColorPlane> plane = M3ColorProperties.nonNullObjectProperty(
            this,
            "plane",
            M3ColorPlane.HSB_SATURATION_BRIGHTNESS
    );

    /// Returns the color plane edited by the area.
    ///
    /// @return the non-null color plane
    public M3ColorPlane getPlane() {
        return plane.get();
    }

    /// Sets the color plane edited by the area.
    ///
    /// This operation does not change [#getValue()]. The area converts the current color when the next direct area
    /// adjustment is stored.
    ///
    /// @param plane the non-null color plane
    /// @throws NullPointerException if `plane` is `null`
    /// @throws RuntimeException if [#planeProperty()] is unidirectionally bound
    public void setPlane(M3ColorPlane plane) {
        this.plane.set(plane);
    }

    /// Returns the property containing the area color plane.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the area-plane property
    public ObjectProperty<M3ColorPlane> planeProperty() {
        return plane;
    }

    /// Whether the alpha-channel slider participates in presentation and layout.
    ///
    /// @defaultValue `true`
    private final BooleanProperty showAlpha = new SimpleBooleanProperty(this, "showAlpha", true);

    /// Returns whether the alpha slider is shown.
    ///
    /// @return `true` when the alpha slider is shown
    public boolean isShowAlpha() {
        return showAlpha.get();
    }

    /// Sets whether the alpha slider is shown.
    ///
    /// Hiding the slider does not change the value's alpha channel.
    ///
    /// @param showAlpha whether to show the alpha slider
    /// @throws RuntimeException if [#showAlphaProperty()] is unidirectionally bound
    public void setShowAlpha(boolean showAlpha) {
        this.showAlpha.set(showAlpha);
    }

    /// Returns the property controlling alpha-slider visibility.
    ///
    /// @return the `showAlpha` property
    public BooleanProperty showAlphaProperty() {
        return showAlpha;
    }

    /// Whether the hue wheel participates in presentation and layout.
    ///
    /// @defaultValue `false`
    private final BooleanProperty showColorWheel = new SimpleBooleanProperty(this, "showColorWheel");

    /// Returns whether the hue wheel is shown.
    ///
    /// @return `true` when the wheel is shown
    public boolean isShowColorWheel() {
        return showColorWheel.get();
    }

    /// Sets whether the hue wheel is shown.
    ///
    /// Hiding the wheel does not change the current color.
    ///
    /// @param showColorWheel whether to show the wheel
    /// @throws RuntimeException if [#showColorWheelProperty()] is unidirectionally bound
    public void setShowColorWheel(boolean showColorWheel) {
        this.showColorWheel.set(showColorWheel);
    }

    /// Returns the property controlling hue-wheel visibility.
    ///
    /// @return the `showColorWheel` property
    public BooleanProperty showColorWheelProperty() {
        return showColorWheel;
    }

    /// Whether the hexadecimal field participates in presentation and layout.
    ///
    /// @defaultValue `true`
    private final BooleanProperty showColorField = new SimpleBooleanProperty(this, "showColorField", true);

    /// Returns whether the hexadecimal field is shown.
    ///
    /// @return `true` when the field is shown
    public boolean isShowColorField() {
        return showColorField.get();
    }

    /// Sets whether the hexadecimal field is shown.
    ///
    /// Hiding the field does not change the current color.
    ///
    /// @param showColorField whether to show the field
    /// @throws RuntimeException if [#showColorFieldProperty()] is unidirectionally bound
    public void setShowColorField(boolean showColorField) {
        this.showColorField.set(showColorField);
    }

    /// Returns the property controlling hexadecimal-field visibility.
    ///
    /// @return the `showColorField` property
    public BooleanProperty showColorFieldProperty() {
        return showColorField;
    }

    /// Whether the preset palette is shown when presets are available.
    ///
    /// @defaultValue `true`
    private final BooleanProperty showPresets = new SimpleBooleanProperty(this, "showPresets", true);

    /// Returns whether the preset palette may be shown.
    ///
    /// @return `true` when presets are enabled
    public boolean isShowPresets() {
        return showPresets.get();
    }

    /// Sets whether the preset palette may be shown.
    ///
    /// The palette is hidden when [#getPresets()] is empty regardless of this value. Changing this property does not
    /// modify the preset list or the current color.
    ///
    /// @param showPresets whether to show presets
    /// @throws RuntimeException if [#showPresetsProperty()] is unidirectionally bound
    public void setShowPresets(boolean showPresets) {
        this.showPresets.set(showPresets);
    }

    /// Returns the property controlling preset-palette visibility.
    ///
    /// @return the `showPresets` property
    public BooleanProperty showPresetsProperty() {
        return showPresets;
    }

    /// The live ordered preset list.
    private final ObservableList<M3Color> presets =
            M3ObservableLists.distinctElementList("preset color", M3Color::isEquivalentTo);

    /// Returns the live list of preset colors.
    ///
    /// Mutations update the preset palette. The list is empty by default and rejects `null` elements and any color
    /// whose [canonical RGBA representation][M3Color#isEquivalentTo(M3Color)] equals another preset. Color-space
    /// identity and latent achromatic channel values do not make two presets distinct.
    ///
    /// The list's direct `addAll`, `setAll`, and `replaceAll` operations validate the complete candidate result
    /// before modifying the list. A failure in one of those operations therefore leaves the existing presets
    /// unchanged. A mutating operation throws [NullPointerException] for a `null` element and
    /// [IllegalArgumentException] if its result would contain equivalent canonical RGBA representations.
    ///
    /// Operations defined by [java.util.List] in terms of [Object#equals(Object)], such as `contains`, `indexOf`,
    /// and removal by object, use the structural equality of the color records rather than rendered equivalence.
    ///
    /// @return the live mutable preset list
    public ObservableList<M3Color> getPresets() {
        return presets;
    }

    /// Creates the default visual representation of this control.
    ///
    /// @return the non-null default skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ColorPickerSkin(this);
    }

    /// Returns the user-agent stylesheet for composed color pickers.
    ///
    /// @return the color-picker stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-picker.css");
    }

    /// Initializes styling and accessibility state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setAccessibleText("Color picker");
        setFocusTraversable(false);
    }
}
