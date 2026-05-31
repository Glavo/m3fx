// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// A Material Design 3 text input container with label, adornment, supporting text, validation, and counter slots.
///
/// `M3TextInputLayout` wraps a JavaFX [TextInputControl] and supplies the component structure that Material
/// text fields require: filled or outlined container, floating label, leading and trailing content, error state,
/// supporting text, character limit, and optional clear button. The wrapped input remains responsible for text
/// editing, selection, clipboard, IME, and accessibility behavior.
///
/// The outlined variant draws its outline notch as part of the control geometry instead of covering the border
/// with a label background, allowing the floating-label transition to match the
/// [Material Design text fields](https://m3.material.io/components/text-fields/overview) model.
@NotNullByDefault
public class M3TextInputLayout extends Control {
    /// The base style class for M3FX text input layouts.
    public static final String STYLE_CLASS = "m3-text-input-layout";

    /// The style class applied to the wrapped text input control.
    public static final String INPUT_STYLE_CLASS = "m3-text-input-layout-input";

    /// The style class applied to the input and adornment container.
    public static final String INPUT_CONTAINER_STYLE_CLASS = "m3-text-input-container";

    /// The style class applied to the outlined input border path.
    public static final String OUTLINE_STYLE_CLASS = "m3-text-input-outline";

    /// The style class applied to the floating or resting label.
    public static final String LABEL_STYLE_CLASS = "m3-text-input-label";

    /// The style class applied to the leading adornment slot.
    public static final String LEADING_STYLE_CLASS = "m3-text-input-leading";

    /// The style class applied to the trailing adornment slot.
    public static final String TRAILING_STYLE_CLASS = "m3-text-input-trailing";

    /// The style class applied to the built-in clear button.
    public static final String CLEAR_BUTTON_STYLE_CLASS = "m3-text-input-clear-button";

    /// The style class applied to the supporting text and counter row.
    public static final String SUPPORTING_ROW_STYLE_CLASS = "m3-text-input-supporting-row";

    /// The style class applied to the supporting or error text label.
    public static final String SUPPORTING_TEXT_STYLE_CLASS = "m3-text-input-supporting-text";

    /// The style class applied to the character counter label.
    public static final String COUNTER_STYLE_CLASS = "m3-text-input-counter";

    /// The pseudo-class used while the supporting row renders an error state.
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    /// The pseudo-class used while the wrapped input is focused.
    private static final PseudoClass FOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("focused");

    /// The pseudo-class used while the label is floating above entered text.
    private static final PseudoClass FLOATING_PSEUDO_CLASS = PseudoClass.getPseudoClass("floating");

    /// The character limit value used when no limit is active.
    private static final int NO_CHARACTER_LIMIT = -1;

    /// The horizontal input padding reserved for each active adornment slot.
    private static final double ADORNED_HORIZONTAL_PADDING = 48.0;

    /// The default horizontal text inset when no adornment is active.
    private static final double TEXT_HORIZONTAL_PADDING = 16.0;

    /// The horizontal notch padding used around floating labels.
    private static final double FLOATING_LABEL_HORIZONTAL_PADDING = 4.0;

    /// The top margin used for filled floating labels.
    private static final double FILLED_FLOATING_LABEL_TOP_MARGIN = 4.0;

    /// The top margin used to make outlined floating labels straddle the outline.
    private static final double OUTLINED_FLOATING_LABEL_TOP_MARGIN = -8.0;

    /// The minimum horizontal gap animated into an outlined field notch.
    private static final double MINIMUM_NOTCH_GAP = 0.5;

    /// The top input padding used when a single-line field has a floating label.
    private static final double LABELED_SINGLE_LINE_TOP_PADDING = 20.0;

    /// The top input padding used when a multiline field has a floating label.
    private static final double LABELED_MULTILINE_TOP_PADDING = 28.0;

    /// The label transition start opacity.
    private static final double LABEL_TRANSITION_START_OPACITY = 0.72;

    /// The label transition start offset.
    private static final double LABEL_TRANSITION_OFFSET_Y = 4.0;

    /// The supporting row transition start offset.
    private static final double SUPPORTING_ROW_TRANSITION_OFFSET_Y = -4.0;

    /// The clear-button transition start scale.
    private static final double TRAILING_TRANSITION_START_SCALE = 0.86;

    /// The wrapped text input control.
    private final ObjectProperty<@Nullable TextInputControl> input =
            new SimpleObjectProperty<>(this, "input") {
                /// Validates text input ownership before setting the value.
                @Override
                public void set(@Nullable TextInputControl newValue) {
                    validateInput(newValue);
                    super.set(newValue);
                }

                /// Rebuilds layout children when the input changes.
                @Override
                protected void invalidated() {
                    updateInput();
                }
            };

    /// The field label displayed inside or above the wrapped input.
    private final StringProperty labelText = new SimpleStringProperty(this, "labelText", "") {
        /// Rejects null label text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "labelText"));
        }

        /// Updates label content and accessibility text.
        @Override
        protected void invalidated() {
            updateLabel();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    };

    /// The leading adornment node.
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Updates the leading slot when the node changes.
        @Override
        protected void invalidated() {
            updateLeading();
        }
    };

    /// The trailing adornment node.
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        /// Updates the trailing slot when the node changes.
        @Override
        protected void invalidated() {
            updateTrailing();
        }
    };

    /// The supporting text displayed when no error is active.
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "") {
        /// Rejects null supporting text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "supportingText"));
        }

        /// Updates the supporting row when the text changes.
        @Override
        protected void invalidated() {
            updateSupportingRow();
        }
    };

    /// The error text displayed above the supporting text when it is not blank.
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
            updateSupportingRow();
        }
    };

    /// The validator that can derive error text from the wrapped input value.
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

    /// The additional validators applied after the primary validator.
    private final ObservableList<M3TextInputValidator> validators = FXCollections.observableArrayList();

    /// The read-only additional validator list.
    private final @UnmodifiableView ObservableList<M3TextInputValidator> validatorsView =
            FXCollections.unmodifiableObservableList(validators);

    /// The last error text produced by the validator.
    private final ReadOnlyStringWrapper validationErrorText =
            new ReadOnlyStringWrapper(this, "validationErrorText", "");

    /// Whether validation has been explicitly run or activated by focus loss.
    private final ReadOnlyBooleanWrapper validationActive =
            new ReadOnlyBooleanWrapper(this, "validationActive");

    /// Whether validation runs after the wrapped input loses focus.
    private final BooleanProperty validateOnFocusLost =
            new SimpleBooleanProperty(this, "validateOnFocusLost", true);

    /// Whether validation refreshes on edits after validation has become active.
    private final BooleanProperty validateOnTextChange =
            new SimpleBooleanProperty(this, "validateOnTextChange", true);

    /// Whether the character counter label is visible.
    private final BooleanProperty characterCounterVisible =
            new SimpleBooleanProperty(this, "characterCounterVisible") {
                /// Updates the supporting row when counter visibility changes.
                @Override
                protected void invalidated() {
                    updateSupportingRow();
                }
            };

    /// Whether text is truncated to the active character limit.
    private final BooleanProperty characterLimitEnforced =
            new SimpleBooleanProperty(this, "characterLimitEnforced") {
                /// Enforces the active character limit when the policy changes.
                @Override
                protected void invalidated() {
                    enforceCharacterLimit();
                    updateInputErrorState();
                    updateSupportingRow();
                }
            };

    /// Whether the built-in clear button may occupy the trailing slot.
    private final BooleanProperty clearButtonEnabled =
            new SimpleBooleanProperty(this, "clearButtonEnabled") {
                /// Updates the trailing slot when clear-button enablement changes.
                @Override
                protected void invalidated() {
                    updateTrailing();
                }
            };

    /// The maximum character count, or `-1` when no maximum is active.
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
            updateSupportingRow();
        }
    };

    /// Whether the layout is currently applying adornment-aware input padding.
    private boolean applyingInputPadding = false;

    /// Whether the layout is currently truncating text to the active character limit.
    private boolean enforcingCharacterLimit = false;

    /// The listener used to track text length changes in the wrapped input.
    private final ChangeListener<String> textListener =
            (observable, oldValue, newValue) -> {
                enforceCharacterLimit();
                updateLabel();
                updateTrailing();
                if (isValidationActive() && isValidateOnTextChange()) {
                    updateValidation();
                } else {
                    updateInputErrorState();
                    updateSupportingRow();
                }
            };

    /// The listener used to update label state when the wrapped input focus changes.
    private final ChangeListener<Boolean> focusListener =
            (observable, oldValue, newValue) -> handleInputFocusChanged(newValue);

    /// The listener used to mirror the wrapped input variant onto this layout.
    private final ChangeListener<M3TextInputVariant> variantListener =
            (observable, oldValue, newValue) -> updateInputVariantStyle();

    /// The listener used to update outline presentation when a wrapped input error state changes.
    private final ChangeListener<Boolean> errorListener =
            (observable, oldValue, newValue) -> {
                updateLabelErrorState();
                updateOutlineState();
            };

    /// The listener used to update outline geometry when wrapped input metrics change.
    private final ChangeListener<Number> inputMetricListener =
            (observable, oldValue, newValue) -> updateOutlinePath();

    /// The listener used to track input padding changes from CSS or application code.
    private final ChangeListener<Insets> paddingListener =
            (observable, oldValue, newValue) -> {
                if (!applyingInputPadding) {
                    installedInputBasePadding = Objects.requireNonNull(newValue, "newValue");
                    updateInputPadding();
                }
            };

    /// Refreshes validation when the additional validator list changes.
    private final ListChangeListener<M3TextInputValidator> validatorsListener = change -> {
        validateValidatorChanges(change);
        if (isValidationActive()) {
            updateValidation();
        }
    };

    /// The listener used to rebuild the outlined border path when layout geometry changes.
    private final InvalidationListener outlineGeometryListener = observable -> updateOutlinePath();

    /// The listener used to mirror logical leading and trailing geometry when layout direction changes.
    private final InvalidationListener nodeOrientationListener = observable -> updateNodeOrientationLayout();

    /// Observes runtime motion settings while this layout is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// The container that overlays leading and trailing adornments over the input.
    private final StackPane inputContainer = new StackPane();

    /// The animated outline path rendered by outlined input layouts.
    private final Path outlinePath = new Path();

    /// The progress of the floating-label outline notch opening animation.
    private final DoubleProperty outlineNotchProgress =
            new SimpleDoubleProperty(this, "outlineNotchProgress") {
                /// Rebuilds the outlined border when the animated notch progress changes.
                @Override
                protected void invalidated() {
                    updateOutlinePath();
                }
            };

    /// The label rendered over the wrapped input.
    private final Label label = new Label();

    /// The slot that renders the leading adornment.
    private final StackPane leadingSlot = new StackPane();

    /// The slot that renders the trailing adornment.
    private final StackPane trailingSlot = new StackPane();

    /// The row containing supporting text, spacer, and counter labels.
    private final HBox supportingRow = new HBox();

    /// The label that renders supporting text or error text.
    private final Label supportingLabel = new Label();

    /// The spacer that pushes the counter toward the trailing edge.
    private final Region supportingSpacer = new Region();

    /// The label that renders the current character count.
    private final Label counterLabel = new Label();

    /// The built-in trailing clear button.
    private final M3IconButton clearButton = new M3IconButton(new M3Icon(
            "x",
            M3IconSize.SMALL,
            M3IconVariant.ON_SURFACE_VARIANT
    ));

    /// Notifies accessibility clients when focus moves between the input and adornment slots.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// The animation used when the label changes between resting and floating states.
    private final Timeline labelAnimation = new Timeline();

    /// The animation used when supporting row content appears or changes.
    private final Timeline supportingRowAnimation = new Timeline();

    /// The animation used when the built-in clear button enters the trailing slot.
    private final Timeline trailingAnimation = new Timeline();

    /// The previously installed input node.
    private @Nullable TextInputControl installedInput = null;

    /// The input padding captured before adornment padding is applied.
    private @Nullable Insets installedInputBasePadding = null;

    /// Whether this layout applied the current error state to the installed input.
    private boolean inputErrorWasApplied = false;

    /// Whether the label is currently floating.
    private boolean labelFloating = false;

    /// Whether the label was visible during the last label update.
    private boolean labelVisible = false;

    /// Whether label motion has been initialized.
    private boolean labelMotionInitialized = false;

    /// The node currently installed in the trailing slot.
    private @Nullable Node installedTrailing = null;

    /// Whether the supporting row was visible during the last supporting row update.
    private boolean supportingRowVisible = false;

    /// Whether supporting row motion has been initialized.
    private boolean supportingRowMotionInitialized = false;

    /// Creates an empty text input layout.
    public M3TextInputLayout() {
        initialize();
    }

    /// Creates a text input layout wrapping the supplied Material text input.
    public M3TextInputLayout(TextInputControl input) {
        this();
        setInput(input);
    }

    /// Creates a text input layout wrapping the supplied input and showing supporting text.
    public M3TextInputLayout(TextInputControl input, String supportingText) {
        this(input);
        setSupportingText(supportingText);
    }

    /// Creates a text input layout wrapping the supplied input and showing label and supporting text.
    public M3TextInputLayout(TextInputControl input, String labelText, String supportingText) {
        this(input, supportingText);
        setLabelText(labelText);
    }

    /// Returns the wrapped text input control.
    public final @Nullable TextInputControl getInput() {
        return input.get();
    }

    /// Sets the wrapped text input control.
    public final void setInput(@Nullable TextInputControl input) {
        this.input.set(input);
    }

    /// Returns the wrapped text input control property.
    public final ObjectProperty<@Nullable TextInputControl> inputProperty() {
        return input;
    }

    /// Returns the wrapped input as the shared M3 text input API.
    public final @Nullable M3TextInput getTextInput() {
        TextInputControl input = getInput();
        return input instanceof M3TextInput textInput ? textInput : null;
    }

    /// Returns the label text displayed inside or above the input.
    public final String getLabelText() {
        return labelText.get();
    }

    /// Sets the label text displayed inside or above the input.
    public final void setLabelText(String labelText) {
        this.labelText.set(Objects.requireNonNull(labelText, "labelText"));
    }

    /// Returns the label text property.
    public final StringProperty labelTextProperty() {
        return labelText;
    }

    /// Returns whether the label is currently floating.
    public final boolean isLabelFloating() {
        return labelFloating;
    }

    /// Returns the leading adornment node.
    public final @Nullable Node getLeading() {
        return leading.get();
    }

    /// Sets the leading adornment node.
    public final void setLeading(@Nullable Node leading) {
        this.leading.set(leading);
    }

    /// Returns the leading adornment node property.
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// Returns the trailing adornment node.
    public final @Nullable Node getTrailing() {
        return trailing.get();
    }

    /// Sets the trailing adornment node.
    public final void setTrailing(@Nullable Node trailing) {
        this.trailing.set(trailing);
    }

    /// Returns the trailing adornment node property.
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// Returns the supporting text shown when no error is active.
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text shown when no error is active.
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the supporting text property.
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the error text shown in the supporting row.
    public final String getErrorText() {
        return errorText.get();
    }

    /// Sets the error text shown in the supporting row.
    public final void setErrorText(String errorText) {
        this.errorText.set(Objects.requireNonNull(errorText, "errorText"));
    }

    /// Returns the error text property.
    public final StringProperty errorTextProperty() {
        return errorText;
    }

    /// Returns the validator used to derive error text from the wrapped input value.
    public final @Nullable M3TextInputValidator getValidator() {
        return validator.get();
    }

    /// Sets the validator used to derive error text from the wrapped input value.
    public final void setValidator(@Nullable M3TextInputValidator validator) {
        this.validator.set(validator);
    }

    /// Returns the validator property.
    public final ObjectProperty<@Nullable M3TextInputValidator> validatorProperty() {
        return validator;
    }

    /// Returns the additional validators applied after the primary validator.
    public final @UnmodifiableView ObservableList<M3TextInputValidator> getValidators() {
        return validatorsView;
    }

    /// Adds an additional validator to the end of the validation pipeline.
    public final void addValidator(M3TextInputValidator validator) {
        validators.add(Objects.requireNonNull(validator, "validator"));
    }

    /// Adds additional validators to the end of the validation pipeline.
    public final void addValidators(M3TextInputValidator... validators) {
        validateValidators(validators);
        this.validators.addAll(validators);
    }

    /// Replaces the additional validation pipeline.
    public final void setValidators(M3TextInputValidator... validators) {
        validateValidators(validators);
        this.validators.setAll(validators);
    }

    /// Removes an additional validator from the validation pipeline.
    public final void removeValidator(M3TextInputValidator validator) {
        validators.remove(Objects.requireNonNull(validator, "validator"));
    }

    /// Removes all additional validators from the validation pipeline.
    public final void clearValidators() {
        validators.clear();
    }

    /// Returns the last error text produced by the validator.
    public final String getValidationErrorText() {
        return validationErrorText.get();
    }

    /// Returns the validator-produced error text property.
    public final ReadOnlyStringProperty validationErrorTextProperty() {
        return validationErrorText.getReadOnlyProperty();
    }

    /// Returns whether validation has been explicitly run or activated by focus loss.
    public final boolean isValidationActive() {
        return validationActive.get();
    }

    /// Returns the validation active state property.
    public final ReadOnlyBooleanProperty validationActiveProperty() {
        return validationActive.getReadOnlyProperty();
    }

    /// Returns whether validation runs after the wrapped input loses focus.
    public final boolean isValidateOnFocusLost() {
        return validateOnFocusLost.get();
    }

    /// Sets whether validation runs after the wrapped input loses focus.
    public final void setValidateOnFocusLost(boolean validateOnFocusLost) {
        this.validateOnFocusLost.set(validateOnFocusLost);
    }

    /// Returns the focus-loss validation property.
    public final BooleanProperty validateOnFocusLostProperty() {
        return validateOnFocusLost;
    }

    /// Returns whether validation refreshes on edits after validation has become active.
    public final boolean isValidateOnTextChange() {
        return validateOnTextChange.get();
    }

    /// Sets whether validation refreshes on edits after validation has become active.
    public final void setValidateOnTextChange(boolean validateOnTextChange) {
        this.validateOnTextChange.set(validateOnTextChange);
    }

    /// Returns the active validation text-change refresh property.
    public final BooleanProperty validateOnTextChangeProperty() {
        return validateOnTextChange;
    }

    /// Runs the configured validator and returns whether the current input is valid.
    public final boolean validate() {
        validationActive.set(true);
        return updateValidation();
    }

    /// Clears validator-produced error state without changing the configured validator.
    public final void clearValidation() {
        validationActive.set(false);
        setValidationErrorText("");
        updateInputErrorState();
        updateSupportingRow();
    }

    /// Returns whether the configured validator currently contributes an error.
    public final boolean isValidationError() {
        return !getValidationErrorText().isEmpty();
    }

    /// Returns whether the character counter is visible.
    public final boolean isCharacterCounterVisible() {
        return characterCounterVisible.get();
    }

    /// Sets whether the character counter is visible.
    public final void setCharacterCounterVisible(boolean characterCounterVisible) {
        this.characterCounterVisible.set(characterCounterVisible);
    }

    /// Returns the character counter visibility property.
    public final BooleanProperty characterCounterVisibleProperty() {
        return characterCounterVisible;
    }

    /// Returns whether text is truncated to the active character limit.
    public final boolean isCharacterLimitEnforced() {
        return characterLimitEnforced.get();
    }

    /// Sets whether text is truncated to the active character limit.
    public final void setCharacterLimitEnforced(boolean characterLimitEnforced) {
        this.characterLimitEnforced.set(characterLimitEnforced);
    }

    /// Returns the character limit enforcement property.
    public final BooleanProperty characterLimitEnforcedProperty() {
        return characterLimitEnforced;
    }

    /// Returns whether the built-in clear button may occupy an empty trailing slot.
    public final boolean isClearButtonEnabled() {
        return clearButtonEnabled.get();
    }

    /// Sets whether the built-in clear button may occupy an empty trailing slot.
    public final void setClearButtonEnabled(boolean clearButtonEnabled) {
        this.clearButtonEnabled.set(clearButtonEnabled);
    }

    /// Returns the clear button enablement property.
    public final BooleanProperty clearButtonEnabledProperty() {
        return clearButtonEnabled;
    }

    /// Returns the built-in clear button used when clear-button support is enabled.
    public final M3IconButton getClearButton() {
        return clearButton;
    }

    /// Clears the wrapped input text when one is installed.
    public final void clearText() {
        TextInputControl input = getInput();
        if (input != null) {
            boolean restoreInputFocus = isFocusInside(effectiveTrailing());
            input.clear();
            if (restoreInputFocus) {
                input.requestFocus();
            }
            notifyFocusNodeChanged();
        }
    }

    /// Returns the active character limit, or `-1` when no limit is active.
    public final int getCharacterLimit() {
        return characterLimit.get();
    }

    /// Sets the active character limit, or `-1` to disable the limit.
    public final void setCharacterLimit(int characterLimit) {
        this.characterLimit.set(characterLimit);
    }

    /// Returns the character limit property.
    public final IntegerProperty characterLimitProperty() {
        return characterLimit;
    }

    /// Returns the current character count for the wrapped text input.
    public final int getCharacterCount() {
        TextInputControl input = getInput();
        return input == null ? 0 : textLength(input);
    }

    /// Returns whether the wrapped input currently exceeds the active character limit.
    public final boolean isCharacterLimitExceeded() {
        int limit = getCharacterLimit();
        return limit >= 0 && getCharacterCount() > limit;
    }

    /// Returns the user-agent stylesheet for M3FX text input layouts.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text-field.css");
    }

    /// Returns the input and adornment container used by the default skin.
    public final StackPane getInputContainer() {
        return inputContainer;
    }

    /// Returns the supporting text and counter row used by the default skin.
    public final HBox getSupportingRow() {
        return supportingRow;
    }

    /// Returns accessibility attributes for the wrapped input and supporting text.
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
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleItem(defaultFocusItem());
            case SHOW_ITEM -> focusAccessibleItem(accessibleActionItem(parameters));
            default -> {
                super.executeAccessibleAction(action, parameters);
            }
        }
    }

    /// Adds base style classes and supporting row children.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);

        inputContainer.getStyleClass().add(INPUT_CONTAINER_STYLE_CLASS);
        outlinePath.getStyleClass().add(OUTLINE_STYLE_CLASS);
        outlinePath.setFill(null);
        outlinePath.setManaged(false);
        outlinePath.setMouseTransparent(true);
        label.getStyleClass().add(LABEL_STYLE_CLASS);
        label.setMouseTransparent(true);
        leadingSlot.getStyleClass().add(LEADING_STYLE_CLASS);
        trailingSlot.getStyleClass().add(TRAILING_STYLE_CLASS);
        label.setOpacity(0.0);
        supportingRow.setOpacity(0.0);
        inputContainer.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        supportingRow.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        inputContainer.widthProperty().addListener(outlineGeometryListener);
        inputContainer.heightProperty().addListener(outlineGeometryListener);
        label.boundsInParentProperty().addListener(outlineGeometryListener);
        outlinePath.strokeWidthProperty().addListener(outlineGeometryListener);
        effectiveNodeOrientationProperty().addListener(nodeOrientationListener);
        inputContainer.getChildren().setAll(outlinePath, label, leadingSlot, trailingSlot);

        supportingRow.getStyleClass().add(SUPPORTING_ROW_STYLE_CLASS);
        supportingLabel.getStyleClass().add(SUPPORTING_TEXT_STYLE_CLASS);
        supportingLabel.setWrapText(true);
        counterLabel.getStyleClass().add(COUNTER_STYLE_CLASS);
        M3ControlStyles.add(clearButton, CLEAR_BUTTON_STYLE_CLASS);
        clearButton.setAccessibleText("Clear text");
        clearButton.setOnAction(event -> clearText());
        validators.addListener(validatorsListener);
        focusNotifier.start();

        HBox.setHgrow(supportingSpacer, Priority.ALWAYS);
        supportingRow.getChildren().setAll(supportingLabel, supportingSpacer, counterLabel);
        updateNodeOrientationLayout();
        updateInputContainer();
        updateInputVariantStyle();
        updateLabel();
        updateLeading();
        updateTrailing();
        updateSupportingRow();
    }

    /// Creates the default Material Design 3 text input layout skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TextInputLayoutSkin(this);
    }

    /// Installs the current input node and removes state from the previous input.
    private void updateInput() {
        TextInputControl oldInput = installedInput;
        boolean restoreInputFocus = isFocusInside(oldInput);
        supportingRow.disableProperty().unbind();
        outlinePath.disableProperty().unbind();
        label.disableProperty().unbind();
        leadingSlot.disableProperty().unbind();
        trailingSlot.disableProperty().unbind();
        supportingRow.setDisable(false);
        outlinePath.setDisable(false);
        label.setDisable(false);
        leadingSlot.setDisable(false);
        trailingSlot.setDisable(false);
        if (oldInput != null) {
            oldInput.textProperty().removeListener(textListener);
            oldInput.focusedProperty().removeListener(focusListener);
            oldInput.paddingProperty().removeListener(paddingListener);
            oldInput.boundsInParentProperty().removeListener(outlineGeometryListener);
            if (oldInput instanceof M3TextInput textInput) {
                textInput.variantProperty().removeListener(variantListener);
                textInput.errorProperty().removeListener(errorListener);
                textInput.containerShapeProperty().removeListener(inputMetricListener);
            }
            oldInput.getStyleClass().remove(INPUT_STYLE_CLASS);
            restoreInputPadding(oldInput);
            if (inputErrorWasApplied && oldInput instanceof M3TextInput textInput) {
                textInput.setError(false);
            }
            inputContainer.getChildren().remove(oldInput);
        }

        inputErrorWasApplied = false;
        installedInput = null;
        installedInputBasePadding = null;
        validationActive.set(false);
        labelMotionInitialized = false;
        supportingRowMotionInitialized = false;
        setValidationErrorText("");

        TextInputControl newInput = getInput();
        if (newInput != null) {
            M3ControlStyles.add(newInput, INPUT_STYLE_CLASS);
            newInput.textProperty().addListener(textListener);
            newInput.focusedProperty().addListener(focusListener);
            newInput.boundsInParentProperty().addListener(outlineGeometryListener);
            if (newInput instanceof M3TextInput textInput) {
                textInput.variantProperty().addListener(variantListener);
                textInput.errorProperty().addListener(errorListener);
                textInput.containerShapeProperty().addListener(inputMetricListener);
            }
            supportingRow.disableProperty().bind(newInput.disabledProperty());
            outlinePath.disableProperty().bind(newInput.disabledProperty());
            label.disableProperty().bind(newInput.disabledProperty());
            leadingSlot.disableProperty().bind(newInput.disabledProperty());
            trailingSlot.disableProperty().bind(newInput.disabledProperty());
            installedInput = newInput;
            installedInputBasePadding = newInput.getPadding();
            newInput.paddingProperty().addListener(paddingListener);
            inputContainer.getChildren().add(1, newInput);
        }

        updateInputContainer();
        updateInputVariantStyle();
        updateLabel();
        enforceCharacterLimit();
        updateInputPadding();
        updateTrailing();
        updateInputErrorState();
        updateSupportingRow();
        if (restoreInputFocus && newInput != null) {
            newInput.requestFocus();
        }
        notifyAccessibleItemsChanged();
    }

    /// Updates floating label state and optionally validates when focus leaves the input.
    private void handleInputFocusChanged(boolean focused) {
        updateLabel();
        notifyFocusNodeChanged();
        if (!focused && isValidateOnFocusLost()) {
            validate();
        }
    }

    /// Updates the input container visibility.
    private void updateInputContainer() {
        boolean visible = getInput() != null;
        inputContainer.setVisible(visible);
        inputContainer.setManaged(visible);
        updateOutlineState();
    }

    /// Mirrors the wrapped input variant onto this layout for label styling.
    private void updateInputVariantStyle() {
        M3TextInput textInput = getTextInput();
        M3TextInputVariant variant = textInput == null ? M3TextInputVariant.FILLED : textInput.getVariant();
        M3ControlStyles.replaceVariant(
                this,
                variant.getStyleClass(),
                M3TextInputVariant.FILLED.getStyleClass(),
                M3TextInputVariant.OUTLINED.getStyleClass()
        );
        updateOutlineState();
    }

    /// Updates label content, floating state, and placement.
    private void updateLabel() {
        TextInputControl input = getInput();
        String text = getLabelText();
        boolean visible = input != null && !text.isBlank();
        boolean floating = visible && shouldFloatLabel(input);

        label.setText(text);
        label.setVisible(visible);
        label.setManaged(visible);
        StackPane.setAlignment(label, labelAlignment(floating));
        label.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, floating);
        updateLabelErrorState();
        updateLabelMotion(visible, floating);
        labelFloating = floating;
        labelVisible = visible;
        updateLabelPadding();
        updateInputPadding();
        updateOutlineState();
    }

    /// Returns whether the label should float above input content.
    private static boolean shouldFloatLabel(TextInputControl input) {
        return input.isFocused() || textLength(input) > 0;
    }

    /// Updates the leading adornment slot.
    private void updateLeading() {
        @Nullable Node previousLeading = installedLeading();
        @Nullable Node leading = getLeading();
        boolean restoreInputFocus = previousLeading != leading && isFocusInside(previousLeading);
        leadingSlot.getChildren().clear();
        if (leading != null) {
            leadingSlot.getChildren().add(leading);
        }
        updateAdornmentSlot(leadingSlot, leading);
        updateLabelPadding();
        updateInputPadding();
        if (restoreInputFocus) {
            restoreInputFocus();
        }
        if (previousLeading != leading) {
            notifyAccessibleItemsChanged();
        }
    }

    /// Updates the trailing adornment slot.
    private void updateTrailing() {
        @Nullable Node trailing = effectiveTrailing();
        @Nullable Node previousTrailing = installedTrailing;
        boolean restoreInputFocus = previousTrailing != trailing && isFocusInside(previousTrailing);
        installedTrailing = trailing;
        trailingSlot.getChildren().clear();
        if (trailing != null) {
            trailingSlot.getChildren().add(trailing);
        }
        updateAdornmentSlot(trailingSlot, trailing);
        updateTrailingMotion(previousTrailing, trailing);
        updateLabelPadding();
        updateInputPadding();
        if (restoreInputFocus) {
            restoreInputFocus();
        }
        if (previousTrailing != trailing) {
            notifyAccessibleItemsChanged();
        }
    }

    /// Returns the node that currently occupies the trailing adornment slot.
    private @Nullable Node effectiveTrailing() {
        Node trailing = getTrailing();
        if (trailing != null) {
            return trailing;
        }
        return isClearButtonActive() ? clearButton : null;
    }

    /// Returns the node that currently occupies the leading adornment slot.
    private @Nullable Node installedLeading() {
        return leadingSlot.getChildren().isEmpty() ? null : leadingSlot.getChildren().get(0);
    }

    /// Returns whether the built-in clear button should be visible.
    private boolean isClearButtonActive() {
        return isClearButtonEnabled() && getInput() != null && getCharacterCount() > 0;
    }

    /// Updates common adornment slot visibility.
    private static void updateAdornmentSlot(StackPane slot, @Nullable Node content) {
        boolean visible = content != null;
        slot.setVisible(visible);
        slot.setManaged(visible);
        slot.setMouseTransparent(content == null);
    }

    /// Restores the input padding captured before adornments were installed.
    private void restoreInputPadding(TextInputControl input) {
        Insets basePadding = installedInputBasePadding;
        if (basePadding != null) {
            applyingInputPadding = true;
            try {
                input.setPadding(basePadding);
            } finally {
                applyingInputPadding = false;
            }
        }
    }

    /// Updates the wrapped input padding so text does not overlap adornments.
    private void updateInputPadding() {
        TextInputControl input = installedInput;
        Insets basePadding = installedInputBasePadding;
        if (input == null || basePadding == null) {
            return;
        }

        double leading = getLeading() == null
                ? leadingInset(basePadding)
                : Math.max(leadingInset(basePadding), ADORNED_HORIZONTAL_PADDING);
        double trailing = effectiveTrailing() == null
                ? trailingInset(basePadding)
                : Math.max(trailingInset(basePadding), ADORNED_HORIZONTAL_PADDING);
        double left = physicalLeftInset(leading, trailing);
        double right = physicalRightInset(leading, trailing);
        // Outlined floating labels occupy the outline notch, not the input content area.
        double top = isLabelFloating() && !isOutlinedInput()
                ? Math.max(basePadding.getTop(), labeledTopPadding(input))
                : basePadding.getTop();
        applyingInputPadding = true;
        try {
            input.setPadding(new Insets(top, right, basePadding.getBottom(), left));
        } finally {
            applyingInputPadding = false;
        }
    }

    /// Updates label placement and floating-label notch padding.
    private void updateLabelPadding() {
        double leadingInset = getLeading() == null ? TEXT_HORIZONTAL_PADDING : ADORNED_HORIZONTAL_PADDING;
        double trailingInset = effectiveTrailing() == null ? TEXT_HORIZONTAL_PADDING : ADORNED_HORIZONTAL_PADDING;
        double textLeft = physicalLeftInset(leadingInset, trailingInset);
        double textRight = physicalRightInset(leadingInset, trailingInset);
        if (isLabelFloating()) {
            label.setPadding(new Insets(
                    0.0,
                    FLOATING_LABEL_HORIZONTAL_PADDING,
                    0.0,
                    FLOATING_LABEL_HORIZONTAL_PADDING
            ));
            StackPane.setMargin(label, new Insets(
                    isOutlinedInput() ? OUTLINED_FLOATING_LABEL_TOP_MARGIN : FILLED_FLOATING_LABEL_TOP_MARGIN,
                    Math.max(0.0, textRight - FLOATING_LABEL_HORIZONTAL_PADDING),
                    0.0,
                    Math.max(0.0, textLeft - FLOATING_LABEL_HORIZONTAL_PADDING)
            ));
        } else {
            label.setPadding(Insets.EMPTY);
            StackPane.setMargin(label, new Insets(0.0, textRight, 0.0, textLeft));
        }
    }

    /// Updates child alignments and geometry that depend on logical layout direction.
    private void updateNodeOrientationLayout() {
        StackPane.setAlignment(leadingSlot, Pos.CENTER_LEFT);
        StackPane.setAlignment(trailingSlot, Pos.CENTER_RIGHT);
        StackPane.setAlignment(label, labelAlignment(isLabelFloating()));
        updateLabelPadding();
        updateInputPadding();
        updateOutlinePath();
        requestLayout();
    }

    /// Returns the logical floating or resting label alignment.
    private Pos labelAlignment(boolean floating) {
        return floating ? Pos.TOP_LEFT : Pos.CENTER_LEFT;
    }

    /// Returns the leading inset from physical input padding.
    private double leadingInset(Insets padding) {
        return isRightToLeft() ? padding.getRight() : padding.getLeft();
    }

    /// Returns the trailing inset from physical input padding.
    private double trailingInset(Insets padding) {
        return isRightToLeft() ? padding.getLeft() : padding.getRight();
    }

    /// Converts a logical leading/trailing inset pair to a physical left inset.
    private double physicalLeftInset(double leading, double trailing) {
        return isRightToLeft() ? trailing : leading;
    }

    /// Converts a logical leading/trailing inset pair to a physical right inset.
    private double physicalRightInset(double leading, double trailing) {
        return isRightToLeft() ? leading : trailing;
    }

    /// Returns whether this layout is currently displayed right-to-left.
    private boolean isRightToLeft() {
        return getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }

    /// Returns the top padding required while a label is floating.
    private static double labeledTopPadding(TextInputControl input) {
        return input instanceof M3TextArea ? LABELED_MULTILINE_TOP_PADDING : LABELED_SINGLE_LINE_TOP_PADDING;
    }

    /// Applies the layout-owned error state to the wrapped input.
    private void updateInputErrorState() {
        M3TextInput textInput = getTextInput();
        if (textInput == null) {
            inputErrorWasApplied = false;
            return;
        }

        boolean error = hasErrorState();
        if (error) {
            textInput.setError(true);
            inputErrorWasApplied = true;
        } else if (inputErrorWasApplied) {
            textInput.setError(false);
            inputErrorWasApplied = false;
        }
    }

    /// Updates supporting text, counter text, visibility, and error pseudo-classes.
    private void updateSupportingRow() {
        String message = displayedSupportingText();
        boolean showMessage = !message.isEmpty();
        boolean showCounter = isCharacterCounterVisible() && getInput() != null;
        boolean showRow = showMessage || showCounter;
        boolean error = hasErrorState();
        String counterText = characterCounterText();
        boolean rowVisibilityChanged = supportingRowVisible != showRow;

        supportingLabel.setText(message);
        supportingLabel.setVisible(showMessage);
        supportingLabel.setManaged(showMessage);
        supportingLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        label.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);

        supportingSpacer.setVisible(showCounter);
        supportingSpacer.setManaged(showCounter);

        counterLabel.setText(counterText);
        counterLabel.setVisible(showCounter);
        counterLabel.setManaged(showCounter);
        counterLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);

        supportingRow.setVisible(showRow);
        supportingRow.setManaged(showRow);
        supportingRow.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        updateLabelErrorState();
        updateOutlineState();
        updateSupportingRowMotion(showRow, rowVisibilityChanged);
        supportingRowVisible = showRow;
    }

    /// Updates label transition state when the label appears or changes floating state.
    private void updateLabelMotion(boolean visible, boolean floating) {
        boolean changed = labelVisible != visible || labelFloating != floating;
        if (!visible) {
            labelAnimation.stop();
            label.setOpacity(0.0);
            label.setTranslateY(0.0);
            outlineNotchProgress.set(0.0);
            labelMotionInitialized = false;
            return;
        }

        if (!labelMotionInitialized || getScene() == null) {
            label.setOpacity(1.0);
            label.setTranslateY(0.0);
            outlineNotchProgress.set(floating ? 1.0 : 0.0);
            labelMotionInitialized = true;
            return;
        }

        if (!changed) {
            outlineNotchProgress.set(floating ? 1.0 : 0.0);
            return;
        }

        labelAnimation.stop();
        label.setOpacity(LABEL_TRANSITION_START_OPACITY);
        label.setTranslateY(floating ? LABEL_TRANSITION_OFFSET_Y : -LABEL_TRANSITION_OFFSET_Y);
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        labelAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(label.opacityProperty(), 1.0, spec.interpolator()),
                new KeyValue(label.translateYProperty(), 0.0, spec.interpolator()),
                new KeyValue(outlineNotchProgress, floating ? 1.0 : 0.0, spec.interpolator())
        ));
        M3Animation.playFromStart(this, labelAnimation);
    }

    /// Updates the label error pseudo-class from both layout-owned and wrapped input error state.
    private void updateLabelErrorState() {
        label.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, hasVisualErrorState());
    }

    /// Updates outline visibility, pseudo-classes, and geometry.
    private void updateOutlineState() {
        boolean visible = isOutlinedInput();
        outlinePath.setVisible(visible);
        outlinePath.setManaged(false);
        outlinePath.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, isInputFocused());
        outlinePath.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, hasVisualErrorState());
        updateOutlinePath();
    }

    /// Rebuilds the animated outlined border path and floating-label notch.
    private void updateOutlinePath() {
        if (!outlinePath.isVisible()) {
            outlinePath.getElements().clear();
            return;
        }

        TextInputControl input = installedInput;
        if (input == null) {
            outlinePath.getElements().clear();
            return;
        }

        var inputBounds = input.getBoundsInParent();
        double width = inputBounds.getWidth();
        double height = inputBounds.getHeight();
        if (width <= 0.0 || height <= 0.0) {
            outlinePath.getElements().clear();
            return;
        }

        double strokeWidth = Math.max(1.0, outlinePath.getStrokeWidth());
        double strokeInset = strokeWidth / 2.0;
        double left = inputBounds.getMinX() + strokeInset;
        double top = inputBounds.getMinY() + strokeInset;
        double right = inputBounds.getMaxX() - strokeInset;
        double bottom = inputBounds.getMaxY() - strokeInset;
        double radius = outlineRadius(right - left, bottom - top);
        double topStartX = left + radius;
        double topEndX = right - radius;
        double notchStart = topEndX;
        double notchEnd = topEndX;

        if (isLabelFloating() && label.isVisible()) {
            var labelBounds = label.getBoundsInParent();
            double targetStart = clamp(labelBounds.getMinX(), topStartX, topEndX);
            double targetEnd = clamp(labelBounds.getMaxX(), targetStart, topEndX);
            double targetCenter = (targetStart + targetEnd) / 2.0;
            double progress = clamp(outlineNotchProgress.get(), 0.0, 1.0);
            notchStart = interpolate(targetCenter, targetStart, progress);
            notchEnd = interpolate(targetCenter, targetEnd, progress);
            if (notchEnd - notchStart < MINIMUM_NOTCH_GAP) {
                double center = (notchStart + notchEnd) / 2.0;
                notchStart = clamp(center - MINIMUM_NOTCH_GAP / 2.0, topStartX, topEndX);
                notchEnd = clamp(center + MINIMUM_NOTCH_GAP / 2.0, notchStart, topEndX);
            }
        }

        outlinePath.getElements().setAll(
                new MoveTo(topStartX, top),
                new LineTo(notchStart, top),
                new MoveTo(notchEnd, top),
                new LineTo(topEndX, top),
                new QuadCurveTo(right, top, right, top + radius),
                new LineTo(right, bottom - radius),
                new QuadCurveTo(right, bottom, right - radius, bottom),
                new LineTo(left + radius, bottom),
                new QuadCurveTo(left, bottom, left, bottom - radius),
                new LineTo(left, top + radius),
                new QuadCurveTo(left, top, topStartX, top)
        );
    }

    /// Returns whether the wrapped input should be outlined by the layout.
    private boolean isOutlinedInput() {
        M3TextInput textInput = getTextInput();
        return textInput != null && textInput.getVariant() == M3TextInputVariant.OUTLINED;
    }

    /// Returns whether the wrapped input is focused.
    private boolean isInputFocused() {
        TextInputControl input = getInput();
        return input != null && input.isFocused();
    }

    /// Returns whether either the layout or wrapped input contributes an error visual state.
    private boolean hasVisualErrorState() {
        M3TextInput textInput = getTextInput();
        return hasErrorState() || textInput != null && textInput.isError();
    }

    /// Returns the outline corner radius clamped to the current input size.
    private double outlineRadius(double width, double height) {
        M3TextInput textInput = getTextInput();
        double radius = textInput == null ? M3TextInputSupport.DEFAULT_CONTAINER_SHAPE : textInput.getContainerShape();
        return clamp(radius, 0.0, Math.min(width, height) / 2.0);
    }

    /// Returns a value clamped to the supplied inclusive range.
    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /// Interpolates linearly between two values.
    private static double interpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }

    /// Updates the built-in clear-button entry transition when it occupies the trailing slot.
    private void updateTrailingMotion(@Nullable Node previousTrailing, @Nullable Node trailing) {
        if (previousTrailing == trailing || trailing != clearButton) {
            return;
        }

        if (getScene() == null) {
            clearButton.setOpacity(1.0);
            clearButton.setScaleX(1.0);
            clearButton.setScaleY(1.0);
            return;
        }

        trailingAnimation.stop();
        clearButton.setOpacity(0.0);
        clearButton.setScaleX(TRAILING_TRANSITION_START_SCALE);
        clearButton.setScaleY(TRAILING_TRANSITION_START_SCALE);
        M3MotionSpec spec = M3Animation.fastEffects(this);
        trailingAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(clearButton.opacityProperty(), 1.0, spec.interpolator()),
                new KeyValue(clearButton.scaleXProperty(), 1.0, spec.interpolator()),
                new KeyValue(clearButton.scaleYProperty(), 1.0, spec.interpolator())
        ));
        M3Animation.playFromStart(this, trailingAnimation);
    }

    /// Updates supporting row transition state when the row appears or disappears.
    private void updateSupportingRowMotion(boolean showRow, boolean rowVisibilityChanged) {
        if (!supportingRowMotionInitialized || getScene() == null) {
            supportingRow.setOpacity(showRow ? 1.0 : 0.0);
            supportingRow.setTranslateY(0.0);
            supportingRowMotionInitialized = true;
            return;
        }

        if (!rowVisibilityChanged) {
            supportingRowAnimation.stop();
            supportingRow.setOpacity(showRow ? 1.0 : 0.0);
            supportingRow.setTranslateY(0.0);
            return;
        }

        if (!showRow) {
            supportingRowAnimation.stop();
            supportingRow.setOpacity(0.0);
            supportingRow.setTranslateY(0.0);
            return;
        }

        supportingRowAnimation.stop();
        supportingRow.setOpacity(0.0);
        supportingRow.setTranslateY(SUPPORTING_ROW_TRANSITION_OFFSET_Y);
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        supportingRowAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(supportingRow.opacityProperty(), 1.0, spec.interpolator()),
                new KeyValue(supportingRow.translateYProperty(), 0.0, spec.interpolator())
        ));
        M3Animation.playFromStart(this, supportingRowAnimation);
    }

    /// Applies changed runtime motion settings to currently running text input animations.
    private void refreshMotionSettings() {
        M3Animation.finishRunningAnimationsIfDisabled(this, labelAnimation, trailingAnimation, supportingRowAnimation);
    }

    /// Returns the supporting row text that should be visible.
    private String displayedSupportingText() {
        String errorText = displayedErrorText();
        return errorText.isEmpty() ? getSupportingText() : errorText;
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
        TextInputControl input = getInput();
        if (input == null) {
            setValidationErrorText("");
            updateInputErrorState();
            updateSupportingRow();
            return true;
        }

        @Nullable String text = input.getText();
        @Nullable String errorText = firstValidationError(input, text == null ? "" : text);
        setValidationErrorText(errorText == null ? "" : errorText);
        updateInputErrorState();
        updateSupportingRow();
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

    /// Validates additional validator list changes.
    private static void validateValidatorChanges(ListChangeListener.Change<? extends M3TextInputValidator> change) {
        while (change.next()) {
            for (M3TextInputValidator validator : change.getAddedSubList()) {
                Objects.requireNonNull(validator, "validator");
            }
        }
    }

    /// Updates validator-owned error text.
    private void setValidationErrorText(String errorText) {
        String validatedErrorText = Objects.requireNonNull(errorText, "errorText");
        String oldErrorText = getValidationErrorText();
        validationErrorText.set(validatedErrorText);
        if (!Objects.equals(oldErrorText, validatedErrorText)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    }

    /// Applies the active character limit by truncating wrapped input text when requested.
    private void enforceCharacterLimit() {
        if (enforcingCharacterLimit || !isCharacterLimitEnforced() || getCharacterLimit() < 0) {
            return;
        }

        TextInputControl input = getInput();
        if (input == null) {
            return;
        }

        @Nullable String text = input.getText();
        if (text == null || text.length() <= getCharacterLimit()) {
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
        String message = displayedSupportingText();
        String counter = isCharacterCounterVisible() ? characterCounterText() : "";
        StringBuilder builder = new StringBuilder();
        appendAccessibleText(builder, label);
        appendAccessibleText(builder, message);
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
        if (getInput() != null) {
            count++;
        }
        if (getTrailing() != null) {
            count++;
        } else if (isClearButtonActive()) {
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

        TextInputControl input = getInput();
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
        if (getScene() == null) {
            return null;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        if (focusOwner == null) {
            return null;
        }
        if (focusOwner == this) {
            return this;
        }

        int count = accessibleItemCount();
        for (int index = 0; index < count; index++) {
            @Nullable Node item = accessibleItemAt(index);
            if (item != null && M3Accessible.containsNode(item, focusOwner)) {
                @Nullable Node focusTarget = M3Accessible.focusTarget(item);
                if (focusTarget != null) {
                    return focusTarget;
                }
            }
        }
        return null;
    }

    /// Returns the preferred focus item for this layout.
    private @Nullable Node defaultFocusItem() {
        TextInputControl input = getInput();
        if (input != null) {
            return input;
        }

        @Nullable Node leading = getLeading();
        return leading != null ? leading : effectiveTrailing();
    }

    /// Focuses an accessible child item and reports the changed focus target.
    private void focusAccessibleItem(@Nullable Node item) {
        M3Accessible.showItem(item);
        notifyFocusNodeChanged();
    }

    /// Returns the child item referenced by accessibility action parameters.
    private @Nullable Node accessibleActionItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return defaultFocusItem();
        }
        if (parameters[0] instanceof Number) {
            return accessibleItemAt(parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = accessibleActionItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child item referenced by one accessibility action parameter.
    private @Nullable Node accessibleActionItem(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return accessibleItemAt(number);
        }
        if (parameter instanceof Node node && containsAccessibleItem(node)) {
            return node;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns whether the node is an input, leading, or effective trailing accessibility item.
    private boolean containsAccessibleItem(Node node) {
        return node == getLeading() || node == getInput() || node == effectiveTrailing();
    }

    /// Returns whether keyboard focus belongs to the supplied node or one of its descendants.
    private boolean isFocusInside(@Nullable Node node) {
        if (node == null || getScene() == null) {
            return false;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(node, focusOwner);
    }

    /// Restores keyboard focus to the wrapped input when it can be reached.
    private void restoreInputFocus() {
        TextInputControl input = getInput();
        if (M3Accessible.canReach(input)) {
            input.requestFocus();
        }
    }

    /// Notifies accessibility clients that indexed child items changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        focusNotifier.refresh();
    }

    /// Validates that a wrapped input can render Material text input error state.
    private static void validateInput(@Nullable TextInputControl input) {
        if (input != null && !(input instanceof M3TextInput)) {
            throw new IllegalArgumentException("input must implement M3TextInput");
        }
    }

    /// Validates a character limit value.
    private static int validateCharacterLimit(int characterLimit) {
        if (characterLimit < NO_CHARACTER_LIMIT) {
            throw new IllegalArgumentException("characterLimit must be -1 or greater");
        }
        return characterLimit;
    }

    /// Validates additional validators before installing them.
    private static void validateValidators(M3TextInputValidator... validators) {
        Objects.requireNonNull(validators, "validators");
        for (M3TextInputValidator validator : validators) {
            Objects.requireNonNull(validator, "validator");
        }
    }

    /// Returns the text length of a JavaFX text input control.
    private static int textLength(TextInputControl input) {
        @Nullable String text = input.getText();
        return text == null ? 0 : text.length();
    }
}
