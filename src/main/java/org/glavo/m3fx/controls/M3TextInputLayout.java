// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3TextInputLayoutPresentation;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// A Material Design 3 text input container with label, adornment, supporting text, validation, and counter slots.
///
/// `M3TextInputLayout` wraps a JavaFX [TextInputControl] and supplies the component structure that Material
/// text fields require: filled or outlined container, floating label, leading and trailing content, error state,
/// supporting text, character limit, and optional clear button. The wrapped input remains responsible for text
/// editing, selection, clipboard, IME, and accessibility behavior.
///
/// The outlined variant opens a notch in its border instead of covering the border with a label background,
/// allowing the floating-label transition to match the
/// [Material Design text fields](https://m3.material.io/components/text-fields/overview) model.
///
/// Validation is inactive until [#validate()] is called or the wrapped input loses focus while
/// [#validateOnFocusLostProperty()] is enabled. Once active, edits are revalidated when
/// [#validateOnTextChangeProperty()] is enabled. A minimal validated field can be configured as follows:
///
/// ```java
/// M3TextField emailField = new M3TextField();
/// M3TextInputLayout emailLayout = new M3TextInputLayout(emailField);
/// emailLayout.setLabelText("Email");
/// emailLayout.setValidator(M3TextInputValidators.required("Email is required"));
/// emailLayout.setValidateOnFocusLost(true);
/// boolean valid = emailLayout.validate();
/// ```
@NotNullByDefault
public final class M3TextInputLayout extends Control implements M3FormInput {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-text-input-layout";

    /// The pseudo-class used while the wrapped input is effectively disabled.
    private static final PseudoClass INPUT_DISABLED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("input-disabled");

    /// The character limit value used when no limit is active.
    private static final int NO_CHARACTER_LIMIT = -1;

    /// The additional validators applied after the primary validator.
    private final ObservableList<M3TextInputValidator> validators = M3ObservableLists.nonNullElementList("validator");

    /// Whether the layout is currently truncating text to the active character limit.
    private boolean enforcingCharacterLimit = false;

    /// The listener used to track text length changes in the wrapped input.
    private final ChangeListener<String> textListener =
            (observable, oldValue, newValue) -> {
                enforceCharacterLimit();
                if (isValidationActive() && isValidateOnTextChange()) {
                    updateValidation();
                } else {
                    updateInputErrorState();
                }
                updateClearButtonActive();
                notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                updateLabelFloating();
            };

    /// The listener used to update label state when the wrapped input focus changes.
    private final ChangeListener<Boolean> focusListener =
            (observable, oldValue, newValue) -> handleInputFocusChanged(newValue);

    /// The listener used to mirror the wrapped input disabled state onto this layout.
    private final ChangeListener<Boolean> disabledListener =
            (observable, oldValue, disabled) -> updateInputDisabledState(disabled);

    /// The listener used to refresh clear-button availability when input editability changes.
    private final ChangeListener<Boolean> editableListener =
            (observable, oldValue, editable) -> updateClearButtonActive();

    /// The listener used to mirror the wrapped input variant onto this layout.
    private final ChangeListener<M3TextInputVariant> variantListener =
            (observable, oldValue, newValue) -> updateInputVariantStyle();

    /// Weak wrapper used when observing the externally owned input text.
    private final WeakChangeListener<String> weakTextListener = new WeakChangeListener<>(textListener);

    /// Weak wrapper used when observing the externally owned input focus state.
    private final WeakChangeListener<Boolean> weakFocusListener = new WeakChangeListener<>(focusListener);

    /// Weak wrapper used when observing the externally owned input disabled state.
    private final WeakChangeListener<Boolean> weakDisabledListener = new WeakChangeListener<>(disabledListener);

    /// Weak wrapper used when observing the externally owned input editability.
    private final WeakChangeListener<Boolean> weakEditableListener = new WeakChangeListener<>(editableListener);

    /// Weak wrapper used when observing the externally owned input variant.
    private final WeakChangeListener<M3TextInputVariant> weakVariantListener =
            new WeakChangeListener<>(variantListener);

    /// Refreshes validation when the additional validator list changes.
    private final ListChangeListener<M3TextInputValidator> validatorsListener = change -> {
        validateValidatorChanges(change);
        if (isValidationActive()) {
            updateValidation();
        }
    };

    /// Notifies accessibility clients when focus moves between the input and adornment slots.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// The previously installed input node.
    private @Nullable TextInputControl installedInput = null;

    /// Whether this layout applied the current error state to the installed input.
    private boolean inputErrorWasApplied = false;

    /// Whether the current semantic state permits the built-in clear button to be shown.
    private boolean clearButtonActive = false;

    /// Creates an empty text input layout.
    ///
    /// The layout initially has no input, label, adornments, supporting text, explicit error, validator, character
    /// limit, counter, or clear button. Focus-loss and active text-change validation are enabled.
    public M3TextInputLayout() {
        initialize();
    }

    /// Creates a text input layout wrapping the supplied Material text input.
    ///
    /// @param input the text input to own; it must implement [M3TextInput]
    /// @throws NullPointerException     if `input` is `null`
    /// @throws IllegalArgumentException if `input` does not implement [M3TextInput]
    public M3TextInputLayout(TextInputControl input) {
        this();
        setInput(Objects.requireNonNull(input, "input"));
    }

    /// The wrapped text input control.
    ///
    /// The default is `null`. A non-null value must implement [M3TextInput], must differ from the leading and
    /// trailing nodes, and must satisfy the JavaFX single-parent rule while displayed. Replacing or clearing the
    /// value detaches semantic listeners, clears active validation, and restores an error state written to the
    /// previous input by this layout.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable TextInputControl> input =
            new SimpleObjectProperty<>(this, "input") {
                /// Validates text input ownership before setting the value.
                @Override
                public void set(@Nullable TextInputControl newValue) {
                    validateInputAssignment(newValue);
                    super.set(newValue);
                }

                /// Replaces the input observed by this control.
                @Override
                protected void invalidated() {
                    TextInputControl newValue = get();
                    validateInputAssignment(newValue);
                    updateInput(newValue);
                }
            };

    /// Returns the wrapped text input control.
    ///
    /// @return the owned input, or `null` when this layout is empty
    public final @Nullable TextInputControl getInput() {
        return input.get();
    }

    /// Sets the wrapped text input control.
    ///
    /// A non-null input must implement [M3TextInput], must not occupy either adornment slot, and must satisfy normal
    /// JavaFX parent ownership rules while displayed. Passing `null` removes the current input, clears active
    /// validation, and restores an error state written to the previous input by this layout.
    ///
    /// @param input the Material text input to own, or `null` to remove the current input
    /// @throws IllegalArgumentException if `input` does not implement [M3TextInput]
    public final void setInput(@Nullable TextInputControl input) {
        this.input.set(input);
    }

    /// Returns the `input` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. Every value supplied through
    /// a binding is subject to the same constraints as [#setInput(TextInputControl)]. An invalid bound value is not
    /// installed and leaves the last successfully installed input active.
    ///
    /// @return the `input` property
    public final ObjectProperty<@Nullable TextInputControl> inputProperty() {
        return input;
    }

    /// {@inheritDoc}
    @Override
    public Node getValidationNode() {
        return this;
    }

    /// {@inheritDoc}
    @Override
    public @Nullable Node getValidationFocusTarget() {
        return getInput();
    }

    /// {@inheritDoc}
    @Override
    public ObservableValue<? extends @Nullable Node> validationFocusTargetProperty() {
        return inputProperty();
    }

    /// The field label displayed inside or above the wrapped input.
    ///
    /// The value cannot be `null`; blank text suppresses the label.
    ///
    /// @defaultValue `""`
    private final StringProperty labelText = new SimpleStringProperty(this, "labelText", "") {
        /// Rejects null label text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "labelText"));
        }

        /// Updates label content and accessibility text.
        @Override
        protected void invalidated() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            updateLabelFloating();
        }
    };

    /// Returns the label text displayed inside or above the input.
    ///
    /// @return the label text; never `null`
    @Override
    public final String getLabelText() {
        return labelText.get();
    }

    /// Sets the label text displayed inside or above the input.
    ///
    /// @param labelText the label text, or blank text to suppress the label
    /// @throws NullPointerException if `labelText` is `null`
    public final void setLabelText(String labelText) {
        this.labelText.set(Objects.requireNonNull(labelText, "labelText"));
    }

    /// Returns the `labelText` property.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the `labelText` property
    @Override
    public final StringProperty labelTextProperty() {
        return labelText;
    }

    /// The leading adornment node.
    ///
    /// The default is `null`. A non-null value occupies the logical leading slot, must differ from the input and
    /// trailing nodes, and must satisfy the JavaFX single-parent rule while displayed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Validates the leading slot before setting the node.
        @Override
        public void set(@Nullable Node newValue) {
            validateLeadingAssignment(newValue);
            super.set(newValue);
        }

        /// Updates the leading slot when the node changes.
        @Override
        protected void invalidated() {
            validateLeadingAssignment(get());
            notifyAccessibleItemsChanged();
        }
    };

    /// Returns the leading adornment node.
    ///
    /// @return the leading node, or `null` when the slot is empty
    public final @Nullable Node getLeading() {
        return leading.get();
    }

    /// Sets the leading adornment node.
    ///
    /// @param leading the node to place before the editable text in logical reading order, or `null` to clear it
    /// @throws IllegalArgumentException if `leading` is this control, an ancestor of this control, or already
    ///                                  occupies another input-layout slot
    public final void setLeading(@Nullable Node leading) {
        this.leading.set(leading);
    }

    /// Returns the `leading` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. Every value supplied through
    /// a binding is subject to the same constraints as [#setLeading(Node)].
    ///
    /// @return the `leading` property
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// The trailing adornment node.
    ///
    /// The default is `null`. A non-null value occupies the logical trailing slot, takes precedence over the
    /// built-in clear button, must differ from the input and leading nodes, and must satisfy the JavaFX
    /// single-parent rule while displayed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        /// Validates the trailing slot before setting the node.
        @Override
        public void set(@Nullable Node newValue) {
            validateTrailingAssignment(newValue);
            super.set(newValue);
        }

        /// Updates the trailing slot when the node changes.
        @Override
        protected void invalidated() {
            validateTrailingAssignment(get());
            refreshClearButtonActive();
            notifyAccessibleItemsChanged();
        }
    };

    /// Returns the trailing adornment node.
    ///
    /// @return the trailing node, or `null` when the slot is empty
    public final @Nullable Node getTrailing() {
        return trailing.get();
    }

    /// Sets the trailing adornment node.
    ///
    /// A custom trailing node takes precedence over the built-in clear button.
    ///
    /// @param trailing the node to place after the editable text in logical reading order, or `null` to clear it
    /// @throws IllegalArgumentException if `trailing` is this control, an ancestor of this control, or already
    ///                                  occupies another input-layout slot
    public final void setTrailing(@Nullable Node trailing) {
        this.trailing.set(trailing);
    }

    /// Returns the `trailing` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. Every value supplied through
    /// a binding is subject to the same constraints as [#setTrailing(Node)].
    ///
    /// @return the `trailing` property
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// The supporting text displayed when no error is active.
    ///
    /// The value cannot be `null`; an empty string suppresses supporting text.
    ///
    /// @defaultValue `""`
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "") {
        /// Rejects null supporting text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "supportingText"));
        }

        /// Updates the supporting row when the text changes.
        @Override
        protected void invalidated() {
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    };

    /// Returns the supporting text shown when no error is active.
    ///
    /// @return the supporting text; never `null`
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text shown when no error is active.
    ///
    /// @param supportingText the supporting text, or an empty string to hide it
    /// @throws NullPointerException if `supportingText` is `null`
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the `supportingText` property.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the `supportingText` property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// The explicit error text displayed instead of supporting text when it is not blank.
    ///
    /// Explicit error text takes precedence over validator output. The value cannot be `null`; an empty string
    /// removes the explicit error.
    ///
    /// @defaultValue `""`
    private final StringProperty errorText = new SimpleStringProperty(this, "errorText", "") {
        /// Rejects null error text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "errorText"));
        }

        /// Updates the supporting row and wrapped input error state.
        @Override
        protected void invalidated() {
            updateInputErrorState();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    };

    /// Returns the error text shown in the supporting row.
    ///
    /// @return the explicit error text; never `null`
    public final String getErrorText() {
        return errorText.get();
    }

    /// Sets the error text shown in the supporting row.
    ///
    /// Non-empty explicit error text takes visual precedence over supporting text and validator output.
    ///
    /// @param errorText the explicit error text, or an empty string to clear it
    /// @throws NullPointerException if `errorText` is `null`
    public final void setErrorText(String errorText) {
        this.errorText.set(Objects.requireNonNull(errorText, "errorText"));
    }

    /// Returns the `errorText` property.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the `errorText` property
    public final StringProperty errorTextProperty() {
        return errorText;
    }

    /// The primary validator that can derive error text from the wrapped input value.
    ///
    /// The default is `null`. The primary validator runs before the validators in [#getValidators()]. Changing it
    /// revalidates immediately only when validation is already active.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3TextInputValidator> validator =
            new SimpleObjectProperty<>(this, "validator") {
                /// Refreshes validation when a validator changes during an active validation cycle.
                @Override
                protected void invalidated() {
                    if (isValidationActive()) {
                        updateValidation();
                    }
                }
            };

    /// Returns the validator used to derive error text from the wrapped input value.
    ///
    /// @return the primary validator, or `null` when validation is not configured
    public final @Nullable M3TextInputValidator getValidator() {
        return validator.get();
    }

    /// Sets the validator used to derive error text from the wrapped input value.
    ///
    /// Changing the validator refreshes the validator-produced state only after validation has become active.
    ///
    /// @param validator the primary validator, or `null` to remove it
    public final void setValidator(@Nullable M3TextInputValidator validator) {
        this.validator.set(validator);
    }

    /// Returns the `validator` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `validator` property
    public final ObjectProperty<@Nullable M3TextInputValidator> validatorProperty() {
        return validator;
    }

    /// The last error text produced by the validator.
    private final ReadOnlyStringWrapper validationErrorText =
            new ReadOnlyStringWrapper(this, "validationErrorText", "");

    /// Returns the last error text produced by the validator.
    ///
    /// @return the current validator-produced error text, or an empty string when validation succeeds or is inactive
    @Override
    public final String getValidationErrorText() {
        return validationErrorText.get();
    }

    /// Updates validator-owned error text and returns whether its value changed.
    private boolean setValidationErrorText(String errorText) {
        String validatedErrorText = Objects.requireNonNull(errorText, "errorText");
        String oldErrorText = getValidationErrorText();
        if (oldErrorText.equals(validatedErrorText)) {
            return false;
        }
        validationErrorText.set(validatedErrorText);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        return true;
    }

    /// Returns the `validationErrorText` property.
    ///
    /// The returned property is observable and read-only. Its default value is `""`.
    ///
    /// @return the `validationErrorText` property
    @Override
    public final ReadOnlyStringProperty validationErrorTextProperty() {
        return validationErrorText.getReadOnlyProperty();
    }

    /// Whether validation has been explicitly run or activated by focus loss.
    private final ReadOnlyBooleanWrapper validationActive =
            new ReadOnlyBooleanWrapper(this, "validationActive");

    /// Returns whether validation has been explicitly run or activated by focus loss.
    ///
    /// @return whether validator output currently participates in the visual error state
    @Override
    public final boolean isValidationActive() {
        return validationActive.get();
    }

    /// Returns the `validationActive` property.
    ///
    /// The returned property is observable and read-only. Its default value is `false`.
    ///
    /// @return the `validationActive` property
    @Override
    public final ReadOnlyBooleanProperty validationActiveProperty() {
        return validationActive.getReadOnlyProperty();
    }

    /// Whether validation becomes active after the wrapped input loses focus.
    ///
    /// @defaultValue `true`
    private final BooleanProperty validateOnFocusLost =
            new SimpleBooleanProperty(this, "validateOnFocusLost", true);

    /// Returns whether validation runs after the wrapped input loses focus.
    ///
    /// @return whether focus loss activates validation; the default is `true`
    public final boolean isValidateOnFocusLost() {
        return validateOnFocusLost.get();
    }

    /// Sets whether validation runs after the wrapped input loses focus.
    ///
    /// @param validateOnFocusLost whether focus loss should activate validation
    public final void setValidateOnFocusLost(boolean validateOnFocusLost) {
        this.validateOnFocusLost.set(validateOnFocusLost);
    }

    /// Returns the `validateOnFocusLost` property.
    ///
    /// The returned property is observable and bindable. Its default value is `true`.
    ///
    /// @return the `validateOnFocusLost` property
    public final BooleanProperty validateOnFocusLostProperty() {
        return validateOnFocusLost;
    }

    /// Whether validation refreshes on edits after validation has become active.
    ///
    /// This property does not activate validation by itself.
    ///
    /// @defaultValue `true`
    private final BooleanProperty validateOnTextChange =
            new SimpleBooleanProperty(this, "validateOnTextChange", true);

    /// Returns whether validation refreshes on edits after validation has become active.
    ///
    /// @return whether active validation is refreshed after text changes; the default is `true`
    public final boolean isValidateOnTextChange() {
        return validateOnTextChange.get();
    }

    /// Sets whether validation refreshes on edits after validation has become active.
    ///
    /// This setting does not activate validation by itself.
    ///
    /// @param validateOnTextChange whether edits should refresh already-active validation
    public final void setValidateOnTextChange(boolean validateOnTextChange) {
        this.validateOnTextChange.set(validateOnTextChange);
    }

    /// Returns the `validateOnTextChange` property.
    ///
    /// The returned property is observable and bindable. Its default value is `true`.
    ///
    /// @return the `validateOnTextChange` property
    public final BooleanProperty validateOnTextChangeProperty() {
        return validateOnTextChange;
    }

    /// Whether the character counter label is visible.
    ///
    /// @defaultValue `false`
    private final BooleanProperty characterCounterVisible =
            new SimpleBooleanProperty(this, "characterCounterVisible") {
                /// Updates the supporting row when counter visibility changes.
                @Override
                protected void invalidated() {
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                }
            };

    /// Returns whether the character counter is visible.
    ///
    /// @return whether the counter is enabled; the default is `false`
    public final boolean isCharacterCounterVisible() {
        return characterCounterVisible.get();
    }

    /// Sets whether the character counter is visible.
    ///
    /// @param characterCounterVisible whether to display the current count in the supporting row
    public final void setCharacterCounterVisible(boolean characterCounterVisible) {
        this.characterCounterVisible.set(characterCounterVisible);
    }

    /// Returns the `characterCounterVisible` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `characterCounterVisible` property
    public final BooleanProperty characterCounterVisibleProperty() {
        return characterCounterVisible;
    }

    /// Whether text is truncated to the active character limit.
    ///
    /// Enabling the property immediately truncates writable text when a non-negative limit is active.
    ///
    /// @defaultValue `false`
    private final BooleanProperty characterLimitEnforced =
            new SimpleBooleanProperty(this, "characterLimitEnforced") {
                /// Enforces the active character limit when the policy changes.
                @Override
                protected void invalidated() {
                    enforceCharacterLimit();
                    updateInputErrorState();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                    updateLabelFloating();
                }
            };

    /// Returns whether text is truncated to the active character limit.
    ///
    /// @return whether character-limit enforcement is enabled; the default is `false`
    public final boolean isCharacterLimitEnforced() {
        return characterLimitEnforced.get();
    }

    /// Sets whether text is truncated to the active character limit.
    ///
    /// Enabling enforcement immediately truncates mutable input text that already exceeds the current limit.
    ///
    /// @param characterLimitEnforced whether mutable input text should be limited
    public final void setCharacterLimitEnforced(boolean characterLimitEnforced) {
        this.characterLimitEnforced.set(characterLimitEnforced);
    }

    /// Returns the `characterLimitEnforced` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `characterLimitEnforced` property
    public final BooleanProperty characterLimitEnforcedProperty() {
        return characterLimitEnforced;
    }

    /// Whether the built-in clear button may occupy the trailing slot.
    ///
    /// The button appears only for non-empty editable input with an unbound text property when no custom trailing
    /// node is present.
    ///
    /// @defaultValue `false`
    private final BooleanProperty clearButtonEnabled =
            new SimpleBooleanProperty(this, "clearButtonEnabled") {
                /// Updates the trailing slot when clear-button enablement changes.
                @Override
                protected void invalidated() {
                    updateClearButtonActive();
                }
            };

    /// Returns whether the built-in clear button may occupy an empty trailing slot.
    ///
    /// @return whether the clear button is enabled; the default is `false`
    public final boolean isClearButtonEnabled() {
        return clearButtonEnabled.get();
    }

    /// Sets whether the built-in clear button may occupy an empty trailing slot.
    ///
    /// @param clearButtonEnabled whether a clear button may be shown for non-empty editable, unbound input
    public final void setClearButtonEnabled(boolean clearButtonEnabled) {
        this.clearButtonEnabled.set(clearButtonEnabled);
    }

    /// Returns the `clearButtonEnabled` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `clearButtonEnabled` property
    public final BooleanProperty clearButtonEnabledProperty() {
        return clearButtonEnabled;
    }

    /// The maximum character count, or `-1` when no maximum is active.
    ///
    /// Counts use UTF-16 code units. Values below `-1` are rejected. Changing the value updates the counter and, if
    /// enforcement is enabled, may truncate writable text immediately.
    ///
    /// @defaultValue `-1`
    private final IntegerProperty characterLimit = new SimpleIntegerProperty(this, "characterLimit", NO_CHARACTER_LIMIT) {
        /// Validates the character limit before setting it.
        @Override
        public void set(int newValue) {
            super.set(validateCharacterLimit(newValue));
        }

        /// Updates counter text and error state when the limit changes.
        @Override
        protected void invalidated() {
            enforceCharacterLimit();
            updateInputErrorState();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            updateLabelFloating();
        }
    };

    /// Returns the active character limit, or `-1` when no limit is active.
    ///
    /// @return the maximum character count, or `-1` when unlimited
    public final int getCharacterLimit() {
        return characterLimit.get();
    }

    /// Sets the active character limit, or `-1` to disable the limit.
    ///
    /// @param characterLimit the maximum number of UTF-16 code units, or `-1` for no limit
    /// @throws IllegalArgumentException if `characterLimit` is less than `-1`
    public final void setCharacterLimit(int characterLimit) {
        this.characterLimit.set(characterLimit);
    }

    /// Returns the `characterLimit` property.
    ///
    /// The returned property is observable and bindable. Its default value is `-1`.
    ///
    /// @return the `characterLimit` property
    public final IntegerProperty characterLimitProperty() {
        return characterLimit;
    }

    /// Returns the wrapped input as the shared M3 text input API.
    ///
    /// @return the wrapped input's [M3TextInput] view, or `null` when no input is installed
    public final @Nullable M3TextInput getTextInput() {
        TextInputControl input = getInput();
        return input instanceof M3TextInput textInput ? textInput : null;
    }

    /// Whether the label is currently minimized above the editable content.
    ///
    /// This state changes synchronously with input focus and content. It describes the semantic target state rather
    /// than an intermediate animation frame.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper labelFloating =
            new ReadOnlyBooleanWrapper(this, "labelFloating");

    /// Returns whether the label is currently minimized above the editable content.
    ///
    /// This read-only presentation state is `true` while the input is focused or has content and a non-blank
    /// label is configured. During an animated transition, this method reports the destination state immediately;
    /// it does not wait for the visual motion to settle.
    ///
    /// @return whether the label's target state is minimized
    public final boolean isLabelFloating() {
        return labelFloating.get();
    }

    /// Returns the `labelFloating` property.
    ///
    /// The returned property is observable and read-only. Its default value is `false`.
    ///
    /// @return the `labelFloating` property
    public final ReadOnlyBooleanProperty labelFloatingProperty() {
        return labelFloating.getReadOnlyProperty();
    }

    /// Returns the mutable additional validator pipeline applied after the primary validator.
    ///
    /// Validators run in list order and evaluation stops at the first non-empty error. The list rejects
    /// `null` elements. Mutations refresh validation immediately when validation is active.
    ///
    /// @return the live mutable validator list
    public final ObservableList<M3TextInputValidator> getValidators() {
        return validators;
    }

    /// Runs the configured validator and returns whether the current input is valid.
    ///
    /// Calling this method activates validation. An empty layout and a layout without validators are valid.
    /// Validators run synchronously in primary-then-list order and stop at the first non-empty message. Validator
    /// exceptions are propagated to the caller.
    ///
    /// @return `true` when every configured validator accepts the current value; otherwise `false`
    @Override
    public final boolean validate() {
        validationActive.set(true);
        return updateValidation();
    }

    /// Clears validator-produced error state without changing configured validators or explicit error text.
    ///
    /// Validation becomes inactive until [#validate()] or a configured focus-loss trigger activates it again.
    @Override
    public final void clearValidation() {
        validationActive.set(false);
        if (setValidationErrorText("")) {
            updateInputErrorState();
        }
    }

    /// Returns whether the configured validator currently contributes an error.
    ///
    /// @return whether active validator output is non-empty
    @Override
    public final boolean isValidationError() {
        return !getValidationErrorText().isEmpty();
    }

    /// Clears the wrapped input text when one is installed and writable.
    public final void clearText() {
        TextInputControl input = installedInput;
        if (input != null && canMutateInputText(input)) {
            boolean restoreInputFocus = isFocusInside(effectiveTrailing());
            input.clear();
            if (restoreInputFocus) {
                M3Accessible.showItem(this, input);
            }
            notifyFocusNodeChanged();
        }
    }

    /// Returns the current character count for the wrapped text input.
    ///
    /// The count uses the Java string length and therefore measures UTF-16 code units.
    ///
    /// @return the current count, or zero when no input is installed
    public final int getCharacterCount() {
        TextInputControl input = installedInput;
        return input == null ? 0 : textLength(input);
    }

    /// Returns whether the wrapped input currently exceeds the active character limit.
    ///
    /// @return `true` when a limit is active and the current character count is greater than that limit
    public final boolean isCharacterLimitExceeded() {
        int limit = getCharacterLimit();
        return limit >= 0 && getCharacterCount() > limit;
    }

    /// Returns the user-agent stylesheet for M3FX text input layouts.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text-field.css");
    }

    /// Returns accessibility attributes for the wrapped input and supporting text.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> accessibleItemCount();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions that can be forwarded to the wrapped input.
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Initializes semantic state, validation, keyboard traversal, and accessibility routing.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleSlotNavigationKey);
        validators.addListener(validatorsListener);
        focusNotifier.start();

        updateInputDisabledState(false);
        updateInputVariantStyle();
        updateLabelFloating();
    }

    /// Creates the default Material Design 3 text input layout skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TextInputLayoutSkin(this);
    }

    /// Replaces the input observed by the control and restores state written to the previous input.
    private void updateInput(@Nullable TextInputControl newInput) {
        TextInputControl oldInput = installedInput;
        if (oldInput != null) {
            oldInput.textProperty().removeListener(weakTextListener);
            oldInput.focusedProperty().removeListener(weakFocusListener);
            oldInput.disabledProperty().removeListener(weakDisabledListener);
            oldInput.editableProperty().removeListener(weakEditableListener);
            if (oldInput instanceof M3TextInput textInput) {
                textInput.variantProperty().removeListener(weakVariantListener);
                if (inputErrorWasApplied) {
                    setInputError(textInput, false);
                }
            }
        }

        installedInput = null;
        inputErrorWasApplied = false;
        validationActive.set(false);
        setValidationErrorText("");

        if (newInput != null) {
            installedInput = newInput;
            enforceCharacterLimit();
            newInput.textProperty().addListener(weakTextListener);
            newInput.focusedProperty().addListener(weakFocusListener);
            newInput.disabledProperty().addListener(weakDisabledListener);
            newInput.editableProperty().addListener(weakEditableListener);
            ((M3TextInput) newInput).variantProperty().addListener(weakVariantListener);
        }

        updateInputDisabledState(newInput != null && newInput.isDisabled());
        updateInputVariantStyle();
        updateInputErrorState();
        refreshClearButtonActive();
        notifyAccessibleItemsChanged();
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);

        // Publish the derived target only after every related semantic cache has reached its final state.
        updateLabelFloating();
    }

    /// Handles keyboard focus traversal between input layout adornment slots.
    private void handleSlotNavigationKey(KeyEvent event) {
        M3FocusTraversal.handleHorizontalKeyFocus(this, event, slotFocusTargets());
    }

    /// Mirrors the wrapped input disabled state for component-level container styling.
    private void updateInputDisabledState(boolean disabled) {
        pseudoClassStateChanged(INPUT_DISABLED_PSEUDO_CLASS, disabled);
    }

    /// Handles input focus changes and publishes the resulting label target after validation settles.
    private void handleInputFocusChanged(boolean focused) {
        notifyFocusNodeChanged();
        if (!focused && isValidateOnFocusLost()) {
            validate();
        }
        updateLabelFloating();
    }

    /// Mirrors the wrapped input variant onto this layout for component-level styling.
    private void updateInputVariantStyle() {
        M3TextInput textInput = installedTextInput();
        M3TextInputVariant variant = textInput == null ? M3TextInputVariant.FILLED : textInput.getVariant();
        M3ControlStyles.replaceVariant(
                this,
                variant.styleClass(),
                M3TextInputVariant.FILLED.styleClass(),
                M3TextInputVariant.OUTLINED.styleClass()
        );
    }

    /// Updates the synchronous semantic target for the floating label.
    private void updateLabelFloating() {
        TextInputControl input = installedInput;
        boolean floating = input != null
                && !getLabelText().isBlank()
                && (input.isFocused() || textLength(input) > 0);
        labelFloating.set(floating);
    }

    /// Returns the node that currently occupies the trailing adornment slot.
    private @Nullable Node effectiveTrailing() {
        Node trailing = getTrailing();
        if (trailing != null) {
            return trailing;
        }

        if (!clearButtonActive) {
            return null;
        }
        Skin<?> skin = getSkin();
        return skin instanceof M3TextInputLayoutPresentation presentation
                ? presentation.clearButton()
                : null;
    }

    /// Recomputes whether the built-in clear button belongs to the semantic child set.
    private void updateClearButtonActive() {
        if (refreshClearButtonActive()) {
            notifyAccessibleItemsChanged();
        }
    }

    /// Refreshes the built-in clear-button cache.
    ///
    /// @return whether the cached value changed
    private boolean refreshClearButtonActive() {
        TextInputControl input = installedInput;
        boolean active = isClearButtonEnabled()
                && getTrailing() == null
                && input != null
                && input.isEditable()
                && canMutateInputText(input)
                && textLength(input) > 0;
        if (clearButtonActive == active) {
            return false;
        }

        clearButtonActive = active;
        return true;
    }

    /// Applies the layout-owned error state to the wrapped input.
    private void updateInputErrorState() {
        M3TextInput textInput = installedTextInput();
        if (textInput == null) {
            inputErrorWasApplied = false;
            return;
        }

        boolean error = hasErrorState();
        if (error) {
            if (!textInput.isError()) {
                inputErrorWasApplied = setInputError(textInput, true);
            }
        } else if (inputErrorWasApplied) {
            setInputError(textInput, false);
            inputErrorWasApplied = false;
        }
    }

    /// Writes the wrapped input error state when application code has not bound it.
    private static boolean setInputError(M3TextInput textInput, boolean error) {
        if (textInput.errorProperty().isBound()) {
            return false;
        }

        textInput.setError(error);
        return true;
    }

    /// Returns the error text currently displayed by the supporting row.
    private String displayedErrorText() {
        String explicitErrorText = getErrorText();
        return explicitErrorText.isEmpty() ? getValidationErrorText() : explicitErrorText;
    }

    /// Returns the formatted character counter text.
    private String characterCounterText() {
        int count = getCharacterCount();
        int limit = getCharacterLimit();
        return limit >= 0 ? count + " / " + limit : Integer.toString(count);
    }

    /// Returns whether error text or character overflow should render the error state.
    private boolean hasErrorState() {
        return !displayedErrorText().isEmpty() || isCharacterLimitExceeded();
    }

    /// Runs validation and updates validator-owned error state.
    private boolean updateValidation() {
        TextInputControl input = installedInput;
        if (input == null) {
            setValidationErrorText("");
            updateInputErrorState();
            return true;
        }

        @Nullable String text = input.getText();
        @Nullable String errorText = firstValidationError(input, text == null ? "" : text);
        setValidationErrorText(errorText == null ? "" : errorText);
        updateInputErrorState();
        return getValidationErrorText().isEmpty();
    }

    /// Returns the first error from the primary validator and additional validators.
    private @Nullable String firstValidationError(TextInputControl input, String text) {
        @Nullable M3TextInputValidator primaryValidator = getValidator();
        if (primaryValidator != null) {
            @Nullable String primaryError = primaryValidator.validate(input, text);
            if (primaryError != null && !primaryError.isEmpty()) {
                return primaryError;
            }
        }
        return M3TextInputValidators.firstError(input, text, validators);
    }

    /// Returns whether this layout may write to the wrapped input text property.
    private static boolean canMutateInputText(TextInputControl input) {
        return input.isEditable() && !input.textProperty().isBound();
    }

    /// Validates additional validator list changes.
    private static void validateValidatorChanges(ListChangeListener.Change<? extends M3TextInputValidator> change) {
        while (change.next()) {
            for (M3TextInputValidator validator : change.getAddedSubList()) {
                Objects.requireNonNull(validator, "validator");
            }
        }
    }

    /// Applies the active character limit by truncating wrapped input text when requested.
    private void enforceCharacterLimit() {
        if (enforcingCharacterLimit || !isCharacterLimitEnforced() || getCharacterLimit() < 0) {
            return;
        }

        TextInputControl input = installedInput;
        if (input == null) {
            return;
        }

        @Nullable String text = input.getText();
        if (text == null || text.length() <= getCharacterLimit()) {
            return;
        }

        if (!canMutateInputText(input)) {
            return;
        }

        int caretPosition = input.getCaretPosition();
        String truncatedText = text.substring(0, getCharacterLimit());
        enforcingCharacterLimit = true;
        try {
            input.setText(truncatedText);
            input.positionCaret(Math.min(caretPosition, truncatedText.length()));
        } finally {
            enforcingCharacterLimit = false;
        }
    }

    /// Returns text exposed to assistive technologies.
    private String accessibleText() {
        String label = getLabelText();
        String supporting = getSupportingText();
        String error = displayedErrorText();
        String counter = isCharacterCounterVisible() ? characterCounterText() : "";
        StringBuilder builder = new StringBuilder();
        appendAccessibleText(builder, label);
        appendAccessibleText(builder, supporting);
        if (!error.equals(supporting)) {
            appendAccessibleText(builder, error);
        }
        appendAccessibleText(builder, counter);
        return builder.toString();
    }

    /// Appends one accessible text part when it is non-blank.
    private static void appendAccessibleText(StringBuilder builder, String text) {
        if (text.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(text);
    }

    /// Returns the number of accessible input layout child items.
    private int accessibleItemCount() {
        int count = 0;
        if (getLeading() != null) {
            count++;
        }
        if (installedInput != null) {
            count++;
        }
        if (effectiveTrailing() != null) {
            count++;
        }
        return count;
    }

    /// Returns one accessible input layout child item by index.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        Node leading = getLeading();
        if (leading != null) {
            if (index == 0) {
                return leading;
            }
            index--;
        }

        TextInputControl input = installedInput;
        if (input != null) {
            if (index == 0) {
                return input;
            }
            index--;
        }

        return index == 0 ? effectiveTrailing() : null;
    }

    /// Returns the current accessibility focus node.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node focusNode = currentFocusNode();
        return focusNode == null ? defaultFocusItem() : focusNode;
    }

    /// Returns the current focused input or adornment target, or `null` when focus is outside this layout.
    private @Nullable Node currentFocusNode() {
        if (!M3Accessible.canReach(this)) {
            return null;
        }
        if (isFocused()) {
            return this;
        }
        return M3Accessible.currentFocusTarget(this, getLeading(), installedInput, effectiveTrailing());
    }

    /// Returns the current reachable focus targets in logical input layout slot order.
    private @Unmodifiable List<Node> slotFocusTargets() {
        return M3FocusTraversal.focusTargets(getLeading(), installedInput, effectiveTrailing());
    }

    /// Returns the preferred focus item for this layout.
    private @Nullable Node defaultFocusItem() {
        TextInputControl input = installedInput;
        if (input != null) {
            return input;
        }

        @Nullable Node leading = getLeading();
        return leading != null ? leading : effectiveTrailing();
    }

    /// Requests focus for the current or default accessibility focus target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        return focusAccessibleItem(accessibleFocusNode());
    }

    /// Focuses an accessible child item and reports the changed focus target.
    ///
    /// @param item the item to focus, or `null` when no item is available
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem(@Nullable Node item) {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (M3Accessible.showItem(this, item)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Shows and focuses the requested accessible child or a descendant popup target.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested target
    final boolean showAccessibleItem(Object... parameters) {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (M3Accessible.showCurrentOrItem(this, getLeading(), installedInput, effectiveTrailing(), parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns whether keyboard focus belongs to the supplied node or one of its descendants.
    private boolean isFocusInside(@Nullable Node node) {
        if (node == null || getScene() == null) {
            return false;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(node, focusOwner);
    }

    /// Notifies accessibility clients that indexed child items changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the successfully installed input through the shared Material text input API.
    private @Nullable M3TextInput installedTextInput() {
        return installedInput instanceof M3TextInput textInput ? textInput : null;
    }

    /// Validates that a wrapped input can render Material text input state and occupies a distinct slot.
    private void validateInputAssignment(@Nullable TextInputControl input) {
        if (input != null && !(input instanceof M3TextInput)) {
            throw new IllegalArgumentException("input must implement M3TextInput");
        }
        validateDistinctSlot(input, getLeading(), "input");
        validateDistinctSlot(input, getTrailing(), "input");
    }

    /// Validates the leading adornment assignment.
    private void validateLeadingAssignment(@Nullable Node leading) {
        validateStructuralSlot(leading, "leading");
        validateDistinctSlot(leading, getInput(), "leading");
        validateDistinctSlot(leading, getTrailing(), "leading");
    }

    /// Validates the trailing adornment assignment.
    private void validateTrailingAssignment(@Nullable Node trailing) {
        validateStructuralSlot(trailing, "trailing");
        validateDistinctSlot(trailing, getInput(), "trailing");
        validateDistinctSlot(trailing, getLeading(), "trailing");
    }

    /// Rejects one node reused across two logical slots.
    private static void validateDistinctSlot(
            @Nullable Node candidate,
            @Nullable Node other,
            String propertyName
    ) {
        if (candidate != null && candidate == other) {
            throw new IllegalArgumentException(propertyName + " must not already be used by another slot");
        }
    }

    /// Rejects a slot node that would create a scene-graph cycle.
    private void validateStructuralSlot(@Nullable Node candidate, String propertyName) {
        if (candidate == null) {
            return;
        }
        if (candidate == this) {
            throw new IllegalArgumentException(propertyName + " must not reference this control");
        }
        for (@Nullable Node ancestor = getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            if (candidate == ancestor) {
                throw new IllegalArgumentException(propertyName + " must not reference an ancestor of this control");
            }
        }
    }

    /// Validates a character limit value.
    private static int validateCharacterLimit(int characterLimit) {
        if (characterLimit < NO_CHARACTER_LIMIT) {
            throw new IllegalArgumentException("characterLimit must be -1 or greater");
        }
        return characterLimit;
    }

    /// Returns the text length of a JavaFX text input control.
    private static int textLength(TextInputControl input) {
        @Nullable String text = input.getText();
        return text == null ? 0 : text.length();
    }
}
