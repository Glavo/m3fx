// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3MeterSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a labeled visual measurement of a quantity or achievement from zero to one.
///
/// `M3Meter` is a passive M3FX extension inspired by Adobe Spectrum meters. Material Design 3 does not define a
/// corresponding component. Unlike [M3ProgressBar], a meter represents a value determined by user actions or a
/// measured state rather than the progress of a system operation. It is always determinate.
///
/// The [label][#labelProperty()] describes the measured quantity, while the optional
/// [value text][#valueTextProperty()] can present a localized percentage, ratio, or unit. The label wraps when
/// horizontal space is constrained; value text remains on one line. Applications should provide a non-empty
/// label before presenting the control. Semantic [variants][M3MeterVariant] must not be used as the only signal
/// for the measured state.
///
/// See [Adobe Spectrum meters](https://spectrum.adobe.com/page/meter/).
@NotNullByDefault
public final class M3Meter extends Control {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-meter";

    /// The pseudo class applied while labels are positioned beside the track.
    private static final PseudoClass SIDE_LABEL_PSEUDO_CLASS = PseudoClass.getPseudoClass("side-label");

    /// The minimum accessible value.
    private static final double ACCESSIBLE_MIN_VALUE = 0.0;

    /// The maximum accessible value.
    private static final double ACCESSIBLE_MAX_VALUE = 1.0;

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// Creates an empty informative meter with value zero and the large size role.
    public M3Meter() {
        this("", 0.0);
    }

    /// Creates an informative meter with the specified label and value.
    ///
    /// @param label the text describing the measured quantity
    /// @param value the initial value; values outside `0.0..1.0` are normalized to that range and `NaN` becomes zero
    /// @throws NullPointerException if `label` is `null`
    public M3Meter(String label, double value) {
        initialize();
        setLabel(label);
        setValue(value);
    }

    /// The value represented by the fill.
    ///
    /// Direct assignments are normalized to the inclusive range `0.0..1.0`; `NaN` is normalized to zero. A
    /// binding source must provide a value in that range.
    ///
    /// @defaultValue `0.0`
    private final DoubleProperty value = new DoublePropertyBase(0.0) {
        /// Normalizes direct assignments and publishes value changes.
        @Override
        protected void invalidated() {
            double normalizedValue = normalizeValue(get());
            if (Double.compare(normalizedValue, get()) != 0 && !isBound()) {
                set(normalizedValue);
                return;
            }
            notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
            M3Accessible.notifyAttribute(M3Meter.this, VALUE_STRING_ATTRIBUTE);
            requestLayout();
        }

        /// Returns the owning meter.
        @Override
        public Object getBean() {
            return M3Meter.this;
        }

        /// Returns the property name.
        @Override
        public String getName() {
            return "value";
        }
    };

    /// Returns the current meter value.
    ///
    /// @return the value in `0.0..1.0` when the property contract is respected
    public double getValue() {
        return value.get();
    }

    /// Sets the current meter value.
    ///
    /// Values below zero are normalized to zero, values above one are normalized to one, and `NaN` is normalized
    /// to zero.
    ///
    /// @param value the value to represent
    public void setValue(double value) {
        this.value.set(value);
    }

    /// Returns the observable, bindable meter-value property.
    ///
    /// Direct assignments are normalized to `0.0..1.0`. A unidirectional binding must supply a value in that
    /// range; rendering and accessibility clamp an invalid bound value without changing the binding source.
    ///
    /// @return the meter-value property
    public DoubleProperty valueProperty() {
        return value;
    }

    /// The text describing the measured quantity.
    ///
    /// The empty string is permitted for JavaFX property and FXML compatibility. A `null` supplied directly to
    /// the property or by a binding is exposed and rendered as the empty string.
    ///
    /// @defaultValue `""`
    private final StringProperty label = new SimpleStringProperty(this, "label", "") {
        /// Updates accessibility and layout after the label changes.
        @Override
        protected void invalidated() {
            updateAccessibleText();
            requestLayout();
        }
    };

    /// Returns the text describing the measured quantity.
    ///
    /// @return the label, or the empty string while the property contains `null`
    public String getLabel() {
        return Objects.requireNonNullElse(label.get(), "");
    }

    /// Sets the text describing the measured quantity.
    ///
    /// @param label the descriptive label
    /// @throws NullPointerException if `label` is `null`
    public void setLabel(String label) {
        this.label.set(Objects.requireNonNull(label, "label"));
    }

    /// Returns the property containing the descriptive label.
    ///
    /// Applications should keep the effective label non-empty so assistive technologies can identify the
    /// measured quantity.
    ///
    /// @return the label property
    public StringProperty labelProperty() {
        return label;
    }

    /// The optional text that describes the current value.
    ///
    /// This may contain a localized percentage, ratio, or measurement unit. `null` and the empty string both hide
    /// the visual value label; accessibility falls back to a rounded percentage.
    ///
    /// @defaultValue `null`
    private final StringProperty valueText = new SimpleStringProperty(this, "valueText");

    /// Returns the optional text describing the current value.
    ///
    /// @return the value text, or `null` when no visual value label is requested
    public @Nullable String getValueText() {
        return valueText.get();
    }

    /// Sets the optional text describing the current value.
    ///
    /// @param valueText the value text, or `null` to hide it
    public void setValueText(@Nullable String valueText) {
        this.valueText.set(valueText);
    }

    /// Returns the property containing the optional value text.
    ///
    /// Changes update the visual label and the accessible value string.
    ///
    /// @return the value-text property
    public StringProperty valueTextProperty() {
        return valueText;
    }

    /// The semantic meaning conveyed by the fill color.
    ///
    /// A direct `null` assignment restores [M3MeterVariant#INFORMATIVE]. Bound values must be non-null.
    ///
    /// @defaultValue [M3MeterVariant#INFORMATIVE]
    private final ObjectProperty<M3MeterVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3MeterVariant.INFORMATIVE) {
                /// Restores the default or updates semantic styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3MeterVariant.INFORMATIVE);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// Returns the semantic meter variant.
    ///
    /// @return the non-null semantic variant
    public M3MeterVariant getVariant() {
        return variant.get();
    }

    /// Sets the semantic meter variant.
    ///
    /// @param variant the semantic variant
    /// @throws NullPointerException if `variant` is `null`
    public void setVariant(M3MeterVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the property containing the semantic meter variant.
    ///
    /// @return the variant property
    public ObjectProperty<M3MeterVariant> variantProperty() {
        return variant;
    }

    /// The visual meter size.
    ///
    /// A direct `null` assignment restores [M3MeterSize#LARGE]. Bound values must be non-null.
    ///
    /// @defaultValue [M3MeterSize#LARGE]
    private final ObjectProperty<M3MeterSize> size =
            new SimpleObjectProperty<>(this, "size", M3MeterSize.LARGE) {
                /// Restores the default or updates size styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3MeterSize.LARGE);
                        return;
                    }
                    updateSizeStyle();
                    requestLayout();
                }
            };

    /// Returns the visual meter size.
    ///
    /// @return the non-null size role
    public M3MeterSize getSize() {
        return size.get();
    }

    /// Sets the visual meter size.
    ///
    /// @param size the size role
    /// @throws NullPointerException if `size` is `null`
    public void setSize(M3MeterSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the property containing the visual meter size.
    ///
    /// @return the size property
    public ObjectProperty<M3MeterSize> sizeProperty() {
        return size;
    }

    /// Whether labels are positioned beside the track instead of above it.
    ///
    /// When enabled, the descriptive label appears at logical leading, the track occupies the flexible center,
    /// and optional value text appears at logical trailing.
    ///
    /// @defaultValue `false`
    private final BooleanProperty sideLabel = new SimpleBooleanProperty(this, "sideLabel", false) {
        /// Updates pseudo-class state and layout after placement changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SIDE_LABEL_PSEUDO_CLASS, get());
            requestLayout();
        }
    };

    /// Returns whether labels are positioned beside the track.
    ///
    /// @return `true` for side-label layout
    public boolean isSideLabel() {
        return sideLabel.get();
    }

    /// Sets whether labels are positioned beside the track.
    ///
    /// @param sideLabel `true` for side-label layout
    public void setSideLabel(boolean sideLabel) {
        this.sideLabel.set(sideLabel);
    }

    /// Returns the property controlling label placement.
    ///
    /// @return the side-label property
    public BooleanProperty sideLabelProperty() {
        return sideLabel;
    }

    /// Returns the value clamped for rendering and accessibility.
    ///
    /// This method differs from [#getValue()] only when an invalid value is supplied by a binding.
    ///
    /// @return the effective value in `0.0..1.0`
    public double getEffectiveValue() {
        return normalizeValue(getValue());
    }

    /// Creates the default retained meter skin.
    ///
    /// @return the default meter skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3MeterSkin(this);
    }

    /// Returns accessibility attributes for the measured value and orientation.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            @Nullable String effectiveValueText = getValueText();
            return effectiveValueText == null || effectiveValueText.isEmpty()
                    ? Math.round(getEffectiveValue() * 100.0) + "%"
                    : effectiveValueText;
        }
        return switch (attribute) {
            case MIN_VALUE -> ACCESSIBLE_MIN_VALUE;
            case MAX_VALUE -> ACCESSIBLE_MAX_VALUE;
            case VALUE -> getEffectiveValue();
            case ORIENTATION -> Orientation.HORIZONTAL;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Returns the user-agent stylesheet for meters.
    ///
    /// @return the meter stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("meter.css");
    }

    /// Initializes style, accessibility, and semantic state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        setFocusTraversable(false);
        updateVariantStyle();
        updateSizeStyle();
        updateAccessibleText();
        valueText.addListener(observable -> {
            M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
            requestLayout();
        });
    }

    /// Applies the style class for the current semantic variant.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3MeterVariant.INFORMATIVE.styleClass(),
                M3MeterVariant.POSITIVE.styleClass(),
                M3MeterVariant.NOTICE.styleClass(),
                M3MeterVariant.NEGATIVE.styleClass()
        );
    }

    /// Applies the style class for the current size role.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().styleClass(),
                M3MeterSize.SMALL.styleClass(),
                M3MeterSize.LARGE.styleClass()
        );
    }

    /// Updates the text exposed to assistive technologies.
    private void updateAccessibleText() {
        setAccessibleText(getLabel());
    }

    /// Normalizes a value to the meter range.
    ///
    /// @param value the candidate value
    /// @return the normalized value
    private static double normalizeValue(double value) {
        if (Double.isNaN(value) || value <= 0.0) {
            return 0.0;
        }
        return value >= 1.0 ? 1.0 : value;
    }
}
