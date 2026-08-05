// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NumberFieldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Objects;

/// A localized numeric text field with range-aware increment and decrement actions.
///
/// The committed [#valueProperty()] is separate from the raw [#textProperty()]. User edits may therefore be empty,
/// incomplete, or invalid without changing the committed value. Pressing Enter, leaving the editor, using a step
/// action, or calling [#commitEditorText()] attempts to commit the current text. Empty text clears the value.
///
/// [M3NumberFieldCommitBehavior#SNAP] clamps parsed text to the configured range and snaps it to the nearest step.
/// [M3NumberFieldCommitBehavior#VALIDATE] instead rejects values that are outside the range or are not aligned to a
/// step, leaving both the text and previous committed value unchanged. Step values are anchored at [#getMin()] when
/// the minimum is finite and at zero when the range is unbounded below.
///
/// Parsing and display use [#formatterProperty()]. The initial formatter is a number formatter for the default
/// format locale at construction time. The decrement and increment buttons can be hidden without disabling keyboard,
/// scrolling, or accessibility actions.
///
/// ```java
/// M3NumberField quantity = new M3NumberField(0.0, 100.0, 12.0);
/// quantity.setLabelText("Quantity");
/// quantity.setStep(2.0);
/// quantity.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);
/// ```
///
/// See [Adobe Spectrum 2 NumberField](https://react-spectrum.adobe.com/NumberField) and
/// [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public final class M3NumberField extends javafx.scene.control.Control implements M3FormInput {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-number-field";

    /// The style class applied to the horizontal stepper container.
    private static final String STEPPER_STYLE_CLASS = "m3-number-field-stepper";

    /// The style class applied to each step button.
    private static final String STEP_BUTTON_STYLE_CLASS = "m3-number-field-step-button";

    /// The style class applied to the decrement button.
    private static final String DECREMENT_BUTTON_STYLE_CLASS = "m3-number-field-decrement-button";

    /// The style class applied to the increment button.
    private static final String INCREMENT_BUTTON_STYLE_CLASS = "m3-number-field-increment-button";

    /// The default unbounded minimum.
    private static final double DEFAULT_MIN = Double.NEGATIVE_INFINITY;

    /// The default unbounded maximum.
    private static final double DEFAULT_MAX = Double.POSITIVE_INFINITY;

    /// The initial step size.
    private static final double DEFAULT_STEP = 1.0;

    /// The multiplier used for block accessibility actions.
    private static final int BLOCK_STEP_COUNT = 10;

    /// The optional accessibility attribute for a formatted value string.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// The editable text field containing raw numeric text.
    private final M3TextField editor = new M3TextField();

    /// The Material input layout containing the editor and adornments.
    private final M3TextInputLayout inputLayout = new M3TextInputLayout(editor);

    /// The button that decreases the committed value by one step.
    private final M3IconButton decrementButton = new M3IconButton(new M3InternalIcon(
            M3InternalIcon.Glyph.REMOVE,
            M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
    ));

    /// The button that increases the committed value by one step.
    private final M3IconButton incrementButton = new M3IconButton(new M3InternalIcon(
            M3InternalIcon.Glyph.ADD,
            M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
    ));

    /// The trailing container retaining both step buttons.
    private final HBox stepper = new HBox(decrementButton, incrementButton);

    /// The last finite committed value reflected by the editor and accessibility attributes.
    private @Nullable Double lastDisplayedValue;

    /// Whether the committed-value property currently has a unidirectional binding.
    private final ReadOnlyBooleanWrapper valueBound = new ReadOnlyBooleanWrapper(this, "valueBound");

    /// Creates an empty number field with an unrestricted range and a step of one.
    public M3NumberField() {
        initialize();
    }

    /// Creates a number field with an initial committed value.
    ///
    /// @param value the initial finite value
    /// @throws IllegalArgumentException if `value` is not finite
    public M3NumberField(double value) {
        this();
        setValue(value);
    }

    /// Creates a number field with an inclusive range and initial committed value.
    ///
    /// @param min   the inclusive minimum, which may be negative infinity for no lower bound
    /// @param max   the inclusive maximum, which may be positive infinity for no upper bound
    /// @param value the initial finite value
    /// @throws IllegalArgumentException if `value` is not finite, an endpoint is invalid, or `min` is greater than
    ///                                  `max`
    public M3NumberField(double min, double max, double value) {
        this();
        setMin(min);
        setMax(max);
        setValue(value);
    }

    /// The committed numeric value, or `null` when the field is empty.
    ///
    /// Direct assignments preserve finite values exactly; the range and step govern user commits and adjustment
    /// actions. A unidirectional binding makes user commits and adjustment actions read-only. Its source must supply
    /// `null` or finite values. A non-finite bound value remains observable from this property, but does not replace
    /// the last finite editor or accessibility value and activates generated validation error state.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Double> value = new SimpleObjectProperty<>(this, "value") {
        /// Installs a unidirectional value binding and updates user-action availability.
        @Override
        public void bind(ObservableValue<? extends @Nullable Double> observable) {
            super.bind(Objects.requireNonNull(observable, "observable"));
            valueBound.set(true);
            updateStepperState();
        }

        /// Removes a unidirectional value binding and restores user-action availability.
        @Override
        public void unbind() {
            boolean wasBound = isBound();
            super.unbind();
            if (wasBound) {
                valueBound.set(false);
                updateStepperState();
            }
        }

        /// Validates direct assignments before storing them.
        @Override
        public void set(@Nullable Double newValue) {
            super.set(newValue == null ? null : requireFinite(newValue, "value"));
        }

        /// Synchronizes editor text and accessibility state after a valid value change.
        @Override
        protected void invalidated() {
            @Nullable Double currentValue = get();
            if (currentValue != null && !Double.isFinite(currentValue)) {
                inputLayout.validate();
                updateStepperState();
                notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                M3Accessible.notifyAttribute(M3NumberField.this, VALUE_STRING_ATTRIBUTE);
                return;
            }
            lastDisplayedValue = currentValue;
            updateEditorFromValue();
            inputLayout.clearValidation();
            updateStepperState();
            notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
            M3Accessible.notifyAttribute(M3NumberField.this, VALUE_STRING_ATTRIBUTE);
        }
    };

    /// Returns the committed value, or `null` when the field is empty.
    ///
    /// A misbehaving unidirectional binding may expose a non-finite value even though direct assignments reject it.
    ///
    /// @return the committed value, or `null`
    public @Nullable Double getValue() {
        return value.get();
    }

    /// Sets the committed value exactly, or clears the field when `null` is supplied.
    ///
    /// @param value the finite value to commit, or `null` to clear the field
    /// @throws IllegalArgumentException if `value` is not finite
    /// @throws RuntimeException if [#valueProperty()] is unidirectionally bound
    public void setValue(@Nullable Double value) {
        this.value.set(value);
    }

    /// Returns the observable, bindable committed-value property.
    ///
    /// A unidirectional binding makes the field read-only for commits, stepping, adjustment methods, and value
    /// accessibility actions. Binding sources must provide `null` or finite values.
    ///
    /// @return the committed-value property
    public ObjectProperty<@Nullable Double> valueProperty() {
        return value;
    }

    /// The non-null raw editor text.
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "") {
        /// Rejects null editor text.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "text"));
        }
    };

    /// Returns the current raw editor text.
    ///
    /// @return the raw editor text
    public String getText() {
        return text.get();
    }

    /// Sets raw editor text without committing it.
    ///
    /// @param text the raw editor text
    /// @throws NullPointerException if `text` is `null`
    public void setText(String text) {
        this.text.set(text);
    }

    /// Returns the observable, bindable raw editor-text property.
    ///
    /// @return the raw editor-text property
    public StringProperty textProperty() {
        return text;
    }

    /// The inclusive minimum accepted by the field.
    ///
    /// @defaultValue `Double.NEGATIVE_INFINITY`
    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", DEFAULT_MIN) {
        /// Validates minimum assignments.
        @Override
        public void set(double newValue) {
            requireMinimum(newValue);
            if (newValue > getMax()) {
                throw new IllegalArgumentException("min must be less than or equal to max");
            }
            super.set(newValue);
        }

        /// Refreshes adjustment state after the minimum changes.
        @Override
        protected void invalidated() {
            updateStepperState();
            refreshActiveValidation();
            notifyAccessibleAttributeChanged(AccessibleAttribute.MIN_VALUE);
        }
    };

    /// Returns the inclusive minimum value.
    ///
    /// @return the inclusive minimum value, or negative infinity when the field has no lower bound
    public double getMin() {
        return min.get();
    }

    /// Sets the inclusive minimum value.
    ///
    /// @param min the inclusive minimum, or negative infinity for no lower bound
    /// @throws IllegalArgumentException if `min` is NaN, positive infinity, or greater than [#getMax()]
    public void setMin(double min) {
        this.min.set(min);
    }

    /// Returns the observable, bindable minimum-value property.
    ///
    /// Negative infinity represents an unbounded lower side; other values must be finite.
    ///
    /// @return the minimum-value property
    public DoubleProperty minProperty() {
        return min;
    }

    /// The inclusive maximum accepted by the field.
    ///
    /// @defaultValue `Double.POSITIVE_INFINITY`
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", DEFAULT_MAX) {
        /// Validates maximum assignments.
        @Override
        public void set(double newValue) {
            requireMaximum(newValue);
            if (newValue < getMin()) {
                throw new IllegalArgumentException("max must be greater than or equal to min");
            }
            super.set(newValue);
        }

        /// Refreshes adjustment state after the maximum changes.
        @Override
        protected void invalidated() {
            updateStepperState();
            refreshActiveValidation();
            notifyAccessibleAttributeChanged(AccessibleAttribute.MAX_VALUE);
        }
    };

    /// Returns the inclusive maximum value.
    ///
    /// @return the inclusive maximum value, or positive infinity when the field has no upper bound
    public double getMax() {
        return max.get();
    }

    /// Sets the inclusive maximum value.
    ///
    /// @param max the inclusive maximum, or positive infinity for no upper bound
    /// @throws IllegalArgumentException if `max` is NaN, negative infinity, or less than [#getMin()]
    public void setMax(double max) {
        this.max.set(max);
    }

    /// Returns the observable, bindable maximum-value property.
    ///
    /// Positive infinity represents an unbounded upper side; other values must be finite.
    ///
    /// @return the maximum-value property
    public DoubleProperty maxProperty() {
        return max;
    }

    /// The positive amount used by one increment or decrement.
    ///
    /// @defaultValue `1.0`
    private final DoubleProperty step = new SimpleDoubleProperty(this, "step", DEFAULT_STEP) {
        /// Validates step assignments.
        @Override
        public void set(double newValue) {
            requireFinite(newValue, "step");
            if (newValue <= 0.0) {
                throw new IllegalArgumentException("step must be greater than zero");
            }
            super.set(newValue);
        }

        /// Refreshes adjustment state after the step changes.
        @Override
        protected void invalidated() {
            updateStepperState();
            refreshActiveValidation();
        }
    };

    /// Returns the positive step size.
    ///
    /// @return the positive step size
    public double getStep() {
        return step.get();
    }

    /// Sets the positive step size.
    ///
    /// @param step the finite positive step size
    /// @throws IllegalArgumentException if `step` is not finite or is not positive
    public void setStep(double step) {
        this.step.set(step);
    }

    /// Returns the observable, bindable step-size property.
    ///
    /// @return the step-size property
    public DoubleProperty stepProperty() {
        return step;
    }

    /// The formatter used to parse and display numeric values.
    ///
    /// @defaultValue a number formatter for the default format locale at construction time
    private final ObjectProperty<NumberFormat> formatter = new SimpleObjectProperty<>(
            this,
            "formatter",
            NumberFormat.getNumberInstance(Locale.getDefault(Locale.Category.FORMAT))
    ) {
        /// Rejects null formatters.
        @Override
        public void set(NumberFormat newValue) {
            super.set(Objects.requireNonNull(newValue, "formatter"));
        }

        /// Rewrites committed value text after the formatter changes.
        @Override
        protected void invalidated() {
            updateEditorFromValue();
            M3Accessible.notifyAttribute(M3NumberField.this, VALUE_STRING_ATTRIBUTE);
        }
    };

    /// Returns the formatter used for parsing and display.
    ///
    /// @return the active formatter
    public NumberFormat getFormatter() {
        return formatter.get();
    }

    /// Sets the formatter used for parsing and display.
    ///
    /// The formatter is retained and used on the JavaFX Application Thread. Mutating it does not invalidate this
    /// property; call this method again to refresh displayed text.
    ///
    /// @param formatter the non-null formatter
    /// @throws NullPointerException if `formatter` is `null`
    public void setFormatter(NumberFormat formatter) {
        NumberFormat checkedFormatter = Objects.requireNonNull(formatter, "formatter");
        if (!this.formatter.isBound() && this.formatter.get() == checkedFormatter) {
            updateEditorFromValue();
            M3Accessible.notifyAttribute(this, VALUE_STRING_ATTRIBUTE);
            return;
        }
        this.formatter.set(checkedFormatter);
    }

    /// Returns the observable, bindable formatter property.
    ///
    /// @return the formatter property
    public ObjectProperty<NumberFormat> formatterProperty() {
        return formatter;
    }

    /// The behavior used to commit parsed text.
    ///
    /// @defaultValue [M3NumberFieldCommitBehavior#SNAP]
    private final ObjectProperty<M3NumberFieldCommitBehavior> commitBehavior = new SimpleObjectProperty<>(
            this,
            "commitBehavior",
            M3NumberFieldCommitBehavior.SNAP
    ) {
        /// Rejects null commit behavior values.
        @Override
        public void set(M3NumberFieldCommitBehavior newValue) {
            super.set(Objects.requireNonNull(newValue, "commitBehavior"));
        }

        /// Refreshes active validation when the commit policy changes.
        @Override
        protected void invalidated() {
            refreshActiveValidation();
        }
    };

    /// Returns the behavior used when parsed text is outside the value scale.
    ///
    /// @return the active commit behavior
    public M3NumberFieldCommitBehavior getCommitBehavior() {
        return commitBehavior.get();
    }

    /// Sets the behavior used when parsed text is outside the value scale.
    ///
    /// @param commitBehavior the non-null commit behavior
    /// @throws NullPointerException if `commitBehavior` is `null`
    public void setCommitBehavior(M3NumberFieldCommitBehavior commitBehavior) {
        this.commitBehavior.set(commitBehavior);
    }

    /// Returns the observable, bindable commit-behavior property.
    ///
    /// @return the commit-behavior property
    public ObjectProperty<M3NumberFieldCommitBehavior> commitBehaviorProperty() {
        return commitBehavior;
    }

    /// Whether the trailing step buttons are hidden.
    ///
    /// @defaultValue `false`
    private final BooleanProperty hideStepper = new SimpleBooleanProperty(this, "hideStepper") {
        /// Installs or removes the stepper adornment.
        @Override
        protected void invalidated() {
            updateStepperVisibility();
        }
    };

    /// Returns whether the trailing step buttons are hidden.
    ///
    /// @return `true` when the step buttons are hidden
    public boolean isHideStepper() {
        return hideStepper.get();
    }

    /// Shows or hides the trailing step buttons.
    ///
    /// @param hideStepper whether the step buttons are hidden
    public void setHideStepper(boolean hideStepper) {
        this.hideStepper.set(hideStepper);
    }

    /// Returns the observable, bindable stepper-visibility property.
    ///
    /// @return the stepper-visibility property
    public BooleanProperty hideStepperProperty() {
        return hideStepper;
    }

    /// Whether focused mouse-wheel scrolling is prevented from changing the value.
    ///
    /// @defaultValue `false`
    private final BooleanProperty wheelDisabled = new SimpleBooleanProperty(this, "wheelDisabled");

    /// Returns whether mouse-wheel value changes are disabled.
    ///
    /// @return `true` when mouse-wheel value changes are disabled
    public boolean isWheelDisabled() {
        return wheelDisabled.get();
    }

    /// Enables or disables mouse-wheel value changes while the editor is focused.
    ///
    /// @param wheelDisabled whether mouse-wheel value changes are disabled
    public void setWheelDisabled(boolean wheelDisabled) {
        this.wheelDisabled.set(wheelDisabled);
    }

    /// Returns the observable, bindable wheel-disable property.
    ///
    /// @return the wheel-disable property
    public BooleanProperty wheelDisabledProperty() {
        return wheelDisabled;
    }

    /// Whether users may edit text or change the value through step actions.
    ///
    /// @defaultValue `true`
    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true) {
        /// Refreshes step availability after the editable state changes.
        @Override
        protected void invalidated() {
            updateStepperState();
        }
    };

    /// Returns whether users may edit or step the value.
    ///
    /// @return `true` when the field is editable
    public boolean isEditable() {
        return editable.get();
    }

    /// Sets whether users may edit or step the value.
    ///
    /// @param editable whether the field is editable
    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    /// Returns the observable, bindable editable property.
    ///
    /// @return the editable property
    public BooleanProperty editableProperty() {
        return editable;
    }

    /// The optional non-interactive prefix displayed before editor text.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> prefix = new SimpleObjectProperty<>(this, "prefix");

    /// Returns the prefix node, or `null` when no prefix is displayed.
    ///
    /// @return the prefix node, or `null`
    public @Nullable Node getPrefix() {
        return prefix.get();
    }

    /// Sets the non-interactive prefix displayed before editor text.
    ///
    /// The node becomes part of this field's scene graph and is subject to the JavaFX single-parent rule.
    ///
    /// @param prefix the prefix node, or `null` to remove it
    public void setPrefix(@Nullable Node prefix) {
        this.prefix.set(prefix);
    }

    /// Returns the observable, bindable prefix-node property.
    ///
    /// @return the prefix-node property
    public ObjectProperty<@Nullable Node> prefixProperty() {
        return prefix;
    }

    /// The visual variant used by the internal text field.
    ///
    /// @defaultValue [M3TextInputVariant#FILLED]
    private final ObjectProperty<M3TextInputVariant> variant = new SimpleObjectProperty<>(
            this,
            "variant",
            M3TextInputVariant.FILLED
    ) {
        /// Rejects null text input variants.
        @Override
        public void set(M3TextInputVariant newValue) {
            super.set(Objects.requireNonNull(newValue, "variant"));
        }
    };

    /// Returns the visual text input variant.
    ///
    /// @return the visual text input variant
    public M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the visual text input variant.
    ///
    /// @param variant the non-null variant
    /// @throws NullPointerException if `variant` is `null`
    public void setVariant(M3TextInputVariant variant) {
        this.variant.set(variant);
    }

    /// Returns the observable, bindable text input variant property.
    ///
    /// @return the text input variant property
    public ObjectProperty<M3TextInputVariant> variantProperty() {
        return variant;
    }

    /// The non-null placeholder displayed while the editor is empty.
    ///
    /// @defaultValue `""`
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "") {
        /// Rejects null placeholder text.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "promptText"));
        }
    };

    /// Returns the editor placeholder text.
    ///
    /// @return the placeholder text
    public String getPromptText() {
        return promptText.get();
    }

    /// Sets the editor placeholder text.
    ///
    /// @param promptText the non-null placeholder text
    /// @throws NullPointerException if `promptText` is `null`
    public void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    /// Returns the observable, bindable placeholder-text property.
    ///
    /// @return the placeholder-text property
    public StringProperty promptTextProperty() {
        return promptText;
    }

    /// The non-null floating label text.
    ///
    /// @defaultValue `""`
    private final StringProperty labelText = nonNullStringProperty("labelText");

    /// Returns the floating label text.
    ///
    /// @return the floating label text
    @Override
    public String getLabelText() {
        return labelText.get();
    }

    /// Sets the floating label text.
    ///
    /// @param labelText the non-null label text
    /// @throws NullPointerException if `labelText` is `null`
    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    /// Returns the observable, bindable floating-label property.
    ///
    /// @return the floating-label property
    @Override
    public StringProperty labelTextProperty() {
        return labelText;
    }

    /// The non-null supporting text shown when no error is active.
    ///
    /// @defaultValue `""`
    private final StringProperty supportingText = nonNullStringProperty("supportingText");

    /// Returns the supporting text.
    ///
    /// @return the supporting text
    public String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text.
    ///
    /// @param supportingText the non-null supporting text
    /// @throws NullPointerException if `supportingText` is `null`
    public void setSupportingText(String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the observable, bindable supporting-text property.
    ///
    /// @return the supporting-text property
    public StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// The non-null explicit error text supplied by the application.
    ///
    /// Numeric parsing and range errors are reported separately through [#validationErrorTextProperty()] and never
    /// replace this value. Explicit error text takes precedence when both sources are non-empty.
    ///
    /// @defaultValue `""`
    private final StringProperty errorText = nonNullStringProperty("errorText");

    /// Returns the explicit application error text.
    ///
    /// @return the explicit application error text
    public String getErrorText() {
        return errorText.get();
    }

    /// Sets the explicit application error text.
    ///
    /// @param errorText the non-null error text, or an empty string to clear the error state
    /// @throws NullPointerException if `errorText` is `null`
    public void setErrorText(String errorText) {
        this.errorText.set(errorText);
    }

    /// Returns the observable, bindable explicit-error-text property.
    ///
    /// @return the explicit-error-text property
    public StringProperty errorTextProperty() {
        return errorText;
    }

    /// Returns the current error text produced by numeric validation.
    ///
    /// @return the generated validation error, or an empty string when validation succeeds or is inactive
    @Override
    public String getValidationErrorText() {
        return inputLayout.getValidationErrorText();
    }

    /// Returns the observable read-only numeric-validation-error property.
    ///
    /// The property is independent of [#errorTextProperty()]. It becomes non-empty after a failed commit and is
    /// refreshed as active editor text, range, step, or commit behavior changes.
    ///
    /// @return the read-only numeric-validation-error property
    @Override
    public ReadOnlyStringProperty validationErrorTextProperty() {
        return inputLayout.validationErrorTextProperty();
    }

    /// Returns whether numeric validation has been activated.
    ///
    /// @return `true` after validation has run and before it is cleared
    @Override
    public boolean isValidationActive() {
        return inputLayout.isValidationActive();
    }

    /// Returns the observable read-only validation-active property.
    ///
    /// @return the read-only validation-active property
    @Override
    public ReadOnlyBooleanProperty validationActiveProperty() {
        return inputLayout.validationActiveProperty();
    }

    /// Clears generated numeric validation without changing explicit application error text.
    @Override
    public void clearValidation() {
        inputLayout.clearValidation();
    }

    /// {@inheritDoc}
    @Override
    public boolean isValidationError() {
        return inputLayout.isValidationError();
    }

    /// The message shown when non-empty editor text cannot be parsed completely.
    ///
    /// @defaultValue `"Enter a valid number"`
    private final StringProperty invalidTextErrorText =
            nonNullStringProperty("invalidTextErrorText", "Enter a valid number");

    /// Returns the message used for unparseable text.
    ///
    /// @return the unparseable-text message
    public String getInvalidTextErrorText() {
        return invalidTextErrorText.get();
    }

    /// Sets the message used for unparseable text.
    ///
    /// @param invalidTextErrorText the non-null message
    /// @throws NullPointerException if `invalidTextErrorText` is `null`
    public void setInvalidTextErrorText(String invalidTextErrorText) {
        this.invalidTextErrorText.set(invalidTextErrorText);
    }

    /// Returns the observable, bindable unparseable-text message property.
    ///
    /// @return the unparseable-text message property
    public StringProperty invalidTextErrorTextProperty() {
        return invalidTextErrorText;
    }

    /// The message shown when validate commits reject range or step alignment.
    ///
    /// @defaultValue `"Enter a value in the allowed range and step"`
    private final StringProperty rangeErrorText =
            nonNullStringProperty("rangeErrorText", "Enter a value in the allowed range and step");

    /// Returns the message used for range and step errors.
    ///
    /// @return the range and step error message
    public String getRangeErrorText() {
        return rangeErrorText.get();
    }

    /// Sets the message used for range and step errors.
    ///
    /// @param rangeErrorText the non-null message
    /// @throws NullPointerException if `rangeErrorText` is `null`
    public void setRangeErrorText(String rangeErrorText) {
        this.rangeErrorText.set(rangeErrorText);
    }

    /// Returns the observable, bindable range and step error-message property.
    ///
    /// @return the range and step error-message property
    public StringProperty rangeErrorTextProperty() {
        return rangeErrorText;
    }

    /// The non-null accessible label for the decrement button.
    ///
    /// @defaultValue `"Decrement"`
    private final StringProperty decrementAccessibleText =
            nonNullStringProperty("decrementAccessibleText", "Decrement");

    /// Returns the decrement button's accessible text.
    ///
    /// @return the decrement button's accessible text
    public String getDecrementAccessibleText() {
        return decrementAccessibleText.get();
    }

    /// Sets the decrement button's accessible text.
    ///
    /// @param decrementAccessibleText the non-null accessible text
    /// @throws NullPointerException if `decrementAccessibleText` is `null`
    public void setDecrementAccessibleText(String decrementAccessibleText) {
        this.decrementAccessibleText.set(decrementAccessibleText);
    }

    /// Returns the observable, bindable decrement-button accessible-text property.
    ///
    /// @return the decrement-button accessible-text property
    public StringProperty decrementAccessibleTextProperty() {
        return decrementAccessibleText;
    }

    /// The non-null accessible label for the increment button.
    ///
    /// @defaultValue `"Increment"`
    private final StringProperty incrementAccessibleText =
            nonNullStringProperty("incrementAccessibleText", "Increment");

    /// Returns the increment button's accessible text.
    ///
    /// @return the increment button's accessible text
    public String getIncrementAccessibleText() {
        return incrementAccessibleText.get();
    }

    /// Sets the increment button's accessible text.
    ///
    /// @param incrementAccessibleText the non-null accessible text
    /// @throws NullPointerException if `incrementAccessibleText` is `null`
    public void setIncrementAccessibleText(String incrementAccessibleText) {
        this.incrementAccessibleText.set(incrementAccessibleText);
    }

    /// Returns the observable, bindable increment-button accessible-text property.
    ///
    /// @return the increment-button accessible-text property
    public StringProperty incrementAccessibleTextProperty() {
        return incrementAccessibleText;
    }

    /// Returns the live internal text editor.
    ///
    /// The field owns this editor. Its text, prompt text, variant, editable state, error presentation, disabled state,
    /// and layout are managed by the number field and must not be rebound independently.
    ///
    /// @return the internal text editor
    public M3TextField getEditor() {
        return editor;
    }

    /// {@inheritDoc}
    @Override
    public Node getValidationNode() {
        return this;
    }

    /// {@inheritDoc}
    @Override
    public Node getValidationFocusTarget() {
        return editor;
    }

    /// {@inheritDoc}
    @Override
    public ObservableValue<? extends @Nullable Node> validationFocusTargetProperty() {
        return inputLayout.inputProperty();
    }

    /// Returns the field-owned text input layout for default-skin construction.
    ///
    /// @return the field-owned text input layout
    final M3TextInputLayout getInputLayout() {
        return inputLayout;
    }

    /// Parses and commits the current editor text.
    ///
    /// Leading and trailing whitespace is ignored. Empty text clears the committed value. Parsing must consume the
    /// complete trimmed string and produce a finite number. A successful commit reformats editor text. A failed
    /// commit leaves the previous value and raw text unchanged and displays a generated error message without
    /// changing [#errorTextProperty()]. If [#valueProperty()] is unidirectionally bound, the editor is restored from
    /// the last finite bound display and this method returns `false` without attempting to write the binding target.
    ///
    /// @return `true` when the text was committed successfully
    public boolean commitEditorText() {
        if (value.isBound()) {
            updateEditorFromValue();
            return false;
        }
        if (!inputLayout.validate()) {
            return false;
        }

        String editorText = editor.getText() == null ? "" : editor.getText().trim();
        if (editorText.isEmpty()) {
            setValue(null);
            return true;
        }

        @Nullable Double parsedValue = parse(editorText);
        if (parsedValue == null) {
            return false;
        }

        setValue(getCommitBehavior() == M3NumberFieldCommitBehavior.SNAP
                ? normalizeForSnap(parsedValue)
                : parsedValue);
        updateEditorFromValue();
        return true;
    }

    /// Runs numeric validation and commits valid editor text when the value is writable.
    ///
    /// A bound value is not written; its formatted display is restored before the current numeric state is
    /// validated. For an unbound value this method has the same commit semantics as [#commitEditorText()].
    ///
    /// @return `true` when the current numeric state is valid
    @Override
    public boolean validate() {
        if (value.isBound()) {
            updateEditorFromValue();
            return inputLayout.validate();
        }
        return commitEditorText();
    }

    /// Increases the committed value by one step.
    public void increment() {
        increment(1);
    }

    /// Increases the committed value by a number of steps.
    ///
    /// For a positive count, pending editor text is committed first. If it cannot be committed or the value property
    /// is unidirectionally bound, the value is not changed. When the field is empty, stepping begins at zero before
    /// range clamping and step snapping. A zero count is a no-op and does not commit pending text.
    ///
    /// @param steps the non-negative number of steps
    /// @throws IllegalArgumentException if `steps` is negative
    public void increment(int steps) {
        if (steps < 0) {
            throw new IllegalArgumentException("steps must not be negative");
        }
        stepBy(steps);
    }

    /// Decreases the committed value by one step.
    public void decrement() {
        decrement(1);
    }

    /// Decreases the committed value by a number of steps.
    ///
    /// For a positive count, pending editor text is committed first. If it cannot be committed or the value property
    /// is unidirectionally bound, the value is not changed. When the field is empty, stepping begins at zero before
    /// range clamping and step snapping. A zero count is a no-op and does not commit pending text.
    ///
    /// @param steps the non-negative number of steps
    /// @throws IllegalArgumentException if `steps` is negative
    public void decrement(int steps) {
        if (steps < 0) {
            throw new IllegalArgumentException("steps must not be negative");
        }
        stepBy(-steps);
    }

    /// Adjusts the committed value to the nearest in-range step.
    ///
    /// This programmatic operation does not parse pending editor text. It has no effect while [#valueProperty()] is
    /// unidirectionally bound.
    ///
    /// @param value the finite target value
    /// @throws IllegalArgumentException if `value` is not finite
    public void adjustValue(double value) {
        double finiteValue = requireFinite(value, "value");
        if (!this.value.isBound()) {
            setValue(normalizeForSnap(finiteValue));
        }
    }

    /// Returns accessibility attributes for the numeric value and internal editor.
    ///
    /// @param attribute  the requested attribute
    /// @param parameters optional attribute parameters
    /// @return the requested value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            return formattedValue();
        }
        return switch (attribute) {
            case FOCUS_NODE -> editor;
            case MIN_VALUE -> accessibleBound(getMin());
            case MAX_VALUE -> accessibleBound(getMax());
            case VALUE -> displayedValue();
            case TEXT -> editor.getText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes focus, value assignment, and step accessibility actions.
    ///
    /// @param action     the requested action
    /// @param parameters optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled() || !isEditable()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusEditor();
            case INCREMENT -> increment();
            case DECREMENT -> decrement();
            case BLOCK_INCREMENT -> increment(BLOCK_STEP_COUNT);
            case BLOCK_DECREMENT -> decrement(BLOCK_STEP_COUNT);
            case SET_VALUE -> setAccessibleValue(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default number-field skin.
    ///
    /// @return a new Material number-field skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NumberFieldSkin(this, inputLayout);
    }

    /// Returns the user-agent stylesheet for number fields.
    ///
    /// @return the number-field stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("number-field.css");
    }

    /// Adds style identity, property wiring, actions, and input handlers.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.SPINNER);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusEditor, null);

        M3ControlStyles.add(stepper, STEPPER_STYLE_CLASS);
        M3ControlStyles.add(decrementButton, STEP_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(decrementButton, DECREMENT_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(incrementButton, STEP_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(incrementButton, INCREMENT_BUTTON_STYLE_CLASS);

        editor.textProperty().bindBidirectional(text);
        editor.promptTextProperty().bindBidirectional(promptText);
        editor.variantProperty().bindBidirectional(variant);
        editor.editableProperty().bind(Bindings.createBooleanBinding(
                () -> isEditable() && !valueBound.get(),
                editable,
                valueBound
        ));
        inputLayout.labelTextProperty().bindBidirectional(labelText);
        inputLayout.supportingTextProperty().bindBidirectional(supportingText);
        inputLayout.errorTextProperty().bindBidirectional(errorText);
        inputLayout.setValidator((input, editorText) -> numericValidationError(editorText));
        inputLayout.leadingProperty().bind(prefix);
        inputLayout.disableProperty().bind(disabledProperty());
        inputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        decrementButton.accessibleTextProperty().bind(decrementAccessibleText);
        incrementButton.accessibleTextProperty().bind(incrementAccessibleText);

        decrementButton.setOnAction(event -> decrement());
        incrementButton.setOnAction(event -> increment());
        editor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        editor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        editor.addEventFilter(ScrollEvent.SCROLL, this::handleEditorScroll);
        editor.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) {
                commitEditorText();
            }
        });
        invalidTextErrorText.addListener(observable -> refreshActiveValidation());
        rangeErrorText.addListener(observable -> refreshActiveValidation());
        disabledProperty().addListener(observable -> updateStepperState());

        updateStepperVisibility();
        updateStepperState();
    }

    /// Handles Enter commits from the internal editor.
    ///
    /// @param event the editor action event
    private void handleEditorAction(ActionEvent event) {
        commitEditorText();
        event.consume();
    }

    /// Handles keyboard stepping and cancellation from the internal editor.
    ///
    /// @param event the key event
    private void handleEditorKeyPressed(KeyEvent event) {
        if (!isEditable() || isDisabled()) {
            return;
        }
        switch (event.getCode()) {
            case UP -> {
                increment();
                event.consume();
            }
            case DOWN -> {
                decrement();
                event.consume();
            }
            case PAGE_UP -> {
                increment(BLOCK_STEP_COUNT);
                event.consume();
            }
            case PAGE_DOWN -> {
                decrement(BLOCK_STEP_COUNT);
                event.consume();
            }
            case ESCAPE -> {
                updateEditorFromValue();
                inputLayout.clearValidation();
                event.consume();
            }
            default -> {
            }
        }
    }

    /// Handles focused mouse-wheel stepping.
    ///
    /// @param event the scroll event
    private void handleEditorScroll(ScrollEvent event) {
        if (isWheelDisabled() || !isEditable() || isDisabled() || !editor.isFocused()) {
            return;
        }
        if (event.getDeltaY() > 0.0) {
            increment();
            event.consume();
        } else if (event.getDeltaY() < 0.0) {
            decrement();
            event.consume();
        }
    }

    /// Applies a signed number of steps after committing pending text.
    ///
    /// @param steps the signed step count
    private void stepBy(int steps) {
        if (steps == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("step count magnitude is too large");
        }
        if (steps == 0 || !isEditable() || isDisabled() || value.isBound()) {
            return;
        }
        if (!editorTextRepresentsDisplayedValue() && !commitEditorText()) {
            return;
        }
        @Nullable Double currentValue = getValue();
        double base = currentValue == null ? 0.0 : currentValue;
        double delta = getStep() * steps;
        double target = base + delta;
        if (!Double.isFinite(target)) {
            target = steps > 0
                    ? finiteUpperAdjustmentLimit()
                    : finiteLowerAdjustmentLimit();
        }
        adjustValue(target);
    }

    /// Returns whether raw editor text is the current formatted committed value.
    ///
    /// @return `true` when stepping can use the committed value without parsing pending text
    private boolean editorTextRepresentsDisplayedValue() {
        @Nullable String editorText = editor.getText();
        return formattedValue().equals(editorText == null ? "" : editorText);
    }

    /// Parses a complete finite numeric string with the active formatter.
    ///
    /// @param text the non-empty trimmed text
    /// @return the parsed finite value, or `null` when parsing fails or leaves trailing input
    private @Nullable Double parse(String text) {
        ParsePosition position = new ParsePosition(0);
        @Nullable Number parsed = getFormatter().parse(text, position);
        if (parsed == null || position.getIndex() != text.length()) {
            return null;
        }
        double value = parsed.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    /// Returns a generated error for the current raw editor text.
    ///
    /// @param editorText the non-null raw editor text supplied by the input layout
    /// @return the generated error text, or `null` when the text is valid
    private @Nullable String numericValidationError(String editorText) {
        @Nullable Double currentValue = getValue();
        if (value.isBound() && currentValue != null && !Double.isFinite(currentValue)) {
            return getInvalidTextErrorText();
        }

        String trimmedText = editorText.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }
        @Nullable Double parsedValue = parse(trimmedText);
        if (parsedValue == null) {
            return getInvalidTextErrorText();
        }
        return getCommitBehavior() == M3NumberFieldCommitBehavior.VALIDATE
                && (!isWithinRange(parsedValue) || !isOnStep(parsedValue))
                ? getRangeErrorText()
                : null;
    }

    /// Re-runs numeric validation when it is already active.
    private void refreshActiveValidation() {
        if (inputLayout.isValidationActive()) {
            inputLayout.validate();
        }
    }

    /// Returns whether a value lies inside the inclusive range.
    ///
    /// @param value the finite value to test
    /// @return `true` when the value is inside the inclusive range
    private boolean isWithinRange(double value) {
        return value >= getMin() && value <= getMax();
    }

    /// Returns whether a value is aligned to the configured step anchor within floating-point tolerance.
    ///
    /// @param value the finite value to test
    /// @return `true` when the value is aligned to a step
    private boolean isOnStep(double value) {
        double snapped = snapToStep(value);
        double tolerance = Math.max(Math.ulp(value) * 8.0, getStep() * 1.0e-10);
        return Math.abs(snapped - value) <= tolerance;
    }

    /// Clamps and snaps a finite value to the active value scale.
    ///
    /// @param value the finite value to normalize
    /// @return the normalized value
    private double normalizeForSnap(double value) {
        double clamped = Math.max(getMin(), Math.min(getMax(), value));
        double snapped = snapToStep(clamped);
        double normalized = snapped;
        double anchor = stepAnchor();
        if (normalized < getMin()) {
            normalized = anchor + Math.ceil((getMin() - anchor) / getStep()) * getStep();
        } else if (normalized > getMax()) {
            normalized = anchor + Math.floor((getMax() - anchor) / getStep()) * getStep();
        }
        if (!Double.isFinite(normalized) || normalized < getMin() || normalized > getMax()) {
            normalized = clamped;
        }
        return normalized == 0.0 ? 0.0 : normalized;
    }

    /// Snaps a finite value to the nearest step without applying range bounds.
    ///
    /// @param value the finite value to snap
    /// @return the snapped value, or the original value if arithmetic overflows
    private double snapToStep(double value) {
        double anchor = stepAnchor();
        double units = (value - anchor) / getStep();
        if (!Double.isFinite(units)) {
            return value;
        }
        double snapped = anchor + Math.rint(units) * getStep();
        return Double.isFinite(snapped) ? snapped : value;
    }

    /// Returns the step anchor derived from the configured minimum.
    ///
    /// @return the configured minimum, or zero when the range is unrestricted below
    private double stepAnchor() {
        return Double.isFinite(getMin()) ? getMin() : 0.0;
    }

    /// Returns the finite upper target used when positive step arithmetic overflows.
    ///
    /// @return the finite maximum, or the largest finite double for an unbounded maximum
    private double finiteUpperAdjustmentLimit() {
        return Double.isFinite(getMax()) ? getMax() : Double.MAX_VALUE;
    }

    /// Returns the finite lower target used when negative step arithmetic overflows.
    ///
    /// @return the finite minimum, or the smallest finite double for an unbounded minimum
    private double finiteLowerAdjustmentLimit() {
        return Double.isFinite(getMin()) ? getMin() : -Double.MAX_VALUE;
    }

    /// Rewrites editor text from the committed value and active formatter.
    private void updateEditorFromValue() {
        @Nullable Double currentValue = displayedValue();
        String formatted = currentValue == null ? "" : getFormatter().format(currentValue);
        editor.setText(formatted);
    }

    /// Returns the formatted committed value for accessibility clients.
    ///
    /// @return the formatted committed value, or an empty string
    private String formattedValue() {
        @Nullable Double currentValue = displayedValue();
        return currentValue == null ? "" : getFormatter().format(currentValue);
    }

    /// Returns the last finite value represented by the editor.
    ///
    /// @return the displayed finite value, or `null` when the displayed field is empty
    private @Nullable Double displayedValue() {
        @Nullable Double currentValue = getValue();
        return currentValue == null || Double.isFinite(currentValue) ? currentValue : lastDisplayedValue;
    }

    /// Shows or removes the trailing stepper according to [#hideStepperProperty()].
    private void updateStepperVisibility() {
        inputLayout.setTrailing(isHideStepper() ? null : stepper);
    }

    /// Updates local button disable states from editability and range endpoints.
    private void updateStepperState() {
        boolean unavailable = isDisabled() || !isEditable() || value.isBound();
        @Nullable Double currentValue = getValue();
        if (currentValue != null && !Double.isFinite(currentValue)) {
            unavailable = true;
            currentValue = null;
        }
        decrementButton.setDisable(unavailable || currentValue != null && currentValue <= getMin());
        incrementButton.setDisable(unavailable || currentValue != null && currentValue >= getMax());
    }

    /// Requests focus for the internal editor through the direct accessibility route.
    ///
    /// @return `true` when the editor accepted the focus request
    private boolean focusEditor() {
        return M3Accessible.showDirectItem(this, editor);
    }

    /// Applies the first finite numeric accessibility parameter.
    ///
    /// @param parameters the accessibility parameters
    private void setAccessibleValue(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (parameter instanceof Number number && Double.isFinite(number.doubleValue())) {
                adjustValue(number.doubleValue());
                return;
            }
        }
    }

    /// Returns a finite accessibility endpoint or `null` for an unbounded side.
    ///
    /// @param bound the configured range endpoint
    /// @return the finite endpoint, or `null` when the side is unbounded
    private static @Nullable Double accessibleBound(double bound) {
        return Double.isFinite(bound) ? bound : null;
    }

    /// Creates a non-null string property with an empty initial value.
    ///
    /// @param name the property name
    /// @return the new property
    private StringProperty nonNullStringProperty(String name) {
        return nonNullStringProperty(name, "");
    }

    /// Creates a non-null string property.
    ///
    /// @param name         the property name
    /// @param initialValue the initial property value
    /// @return the new property
    private StringProperty nonNullStringProperty(String name, String initialValue) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(initialValue, "initialValue");
        return new SimpleStringProperty(this, name, initialValue) {
            /// Rejects null string values.
            @Override
            public void set(String newValue) {
                super.set(Objects.requireNonNull(newValue, name));
            }
        };
    }

    /// Returns a finite value or throws for an invalid numeric configuration.
    ///
    /// @param value the value to validate
    /// @param name  the parameter or property name
    /// @return the validated finite value
    /// @throws IllegalArgumentException if `value` is not finite
    private static double requireFinite(double value, String name) {
        Objects.requireNonNull(name, "name");
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }

    /// Returns a valid lower endpoint or throws for an unusable minimum.
    ///
    /// @param value the lower endpoint to validate
    /// @return the validated endpoint
    /// @throws IllegalArgumentException if `value` is NaN or positive infinity
    private static double requireMinimum(double value) {
        if (Double.isNaN(value) || value == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("min must be finite or negative infinity");
        }
        return value;
    }

    /// Returns a valid upper endpoint or throws for an unusable maximum.
    ///
    /// @param value the upper endpoint to validate
    /// @return the validated endpoint
    /// @throws IllegalArgumentException if `value` is NaN or negative infinity
    private static double requireMaximum(double value) {
        if (Double.isNaN(value) || value == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("max must be finite or positive infinity");
        }
        return value;
    }
}
