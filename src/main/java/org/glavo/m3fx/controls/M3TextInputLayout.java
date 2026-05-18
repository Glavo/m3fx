// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 text input container with supporting text, error text, and a character counter.
@NotNullByDefault
public class M3TextInputLayout extends VBox {
    /// The base style class for M3FX text input layouts.
    public static final String STYLE_CLASS = "m3-text-input-layout";

    /// The style class applied to the wrapped text input control.
    public static final String INPUT_STYLE_CLASS = "m3-text-input-layout-input";

    /// The style class applied to the input and adornment container.
    public static final String INPUT_CONTAINER_STYLE_CLASS = "m3-text-input-container";

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

    /// The character limit value used when no limit is active.
    private static final int NO_CHARACTER_LIMIT = -1;

    /// The horizontal input padding reserved for each active adornment slot.
    private static final double ADORNED_HORIZONTAL_PADDING = 48.0;

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
                updateTrailing();
                updateInputErrorState();
                updateSupportingRow();
            };

    /// The listener used to track input padding changes from CSS or application code.
    private final ChangeListener<Insets> paddingListener =
            (observable, oldValue, newValue) -> {
                if (!applyingInputPadding) {
                    installedInputBasePadding = Objects.requireNonNull(newValue, "newValue");
                    updateInputPadding();
                }
            };

    /// The container that overlays leading and trailing adornments over the input.
    private final StackPane inputContainer = new StackPane();

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
    private final M3IconButton clearButton = M3IconButton.withIcon(
            "x",
            M3IconSize.SMALL,
            M3IconVariant.ON_SURFACE_VARIANT
    );

    /// The previously installed input node.
    private @Nullable TextInputControl installedInput = null;

    /// The input padding captured before adornment padding is applied.
    private @Nullable Insets installedInputBasePadding = null;

    /// Whether this layout applied the current error state to the installed input.
    private boolean inputErrorWasApplied = false;

    /// The node currently installed in the trailing slot.
    private @Nullable Node installedTrailing = null;

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
            input.clear();
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

    /// Returns accessibility attributes for the wrapped input and supporting text.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> getInput();
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
        if (action == AccessibleAction.REQUEST_FOCUS) {
            TextInputControl input = getInput();
            if (input != null) {
                input.requestFocus();
                return;
            }
        }
        super.executeAccessibleAction(action, parameters);
    }

    /// Adds base style classes and supporting row children.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFillWidth(true);

        inputContainer.getStyleClass().add(INPUT_CONTAINER_STYLE_CLASS);
        leadingSlot.getStyleClass().add(LEADING_STYLE_CLASS);
        trailingSlot.getStyleClass().add(TRAILING_STYLE_CLASS);
        StackPane.setAlignment(leadingSlot, Pos.CENTER_LEFT);
        StackPane.setAlignment(trailingSlot, Pos.CENTER_RIGHT);
        inputContainer.getChildren().setAll(leadingSlot, trailingSlot);

        supportingRow.getStyleClass().add(SUPPORTING_ROW_STYLE_CLASS);
        supportingLabel.getStyleClass().add(SUPPORTING_TEXT_STYLE_CLASS);
        supportingLabel.setWrapText(true);
        counterLabel.getStyleClass().add(COUNTER_STYLE_CLASS);
        M3ControlStyles.add(clearButton, CLEAR_BUTTON_STYLE_CLASS);
        clearButton.setAccessibleText("Clear text");
        clearButton.setOnAction(event -> clearText());

        HBox.setHgrow(supportingSpacer, Priority.ALWAYS);
        supportingRow.getChildren().setAll(supportingLabel, supportingSpacer, counterLabel);
        getChildren().addAll(inputContainer, supportingRow);
        updateInputContainer();
        updateLeading();
        updateTrailing();
        updateSupportingRow();
    }

    /// Installs the current input node and removes state from the previous input.
    private void updateInput() {
        TextInputControl oldInput = installedInput;
        supportingRow.disableProperty().unbind();
        leadingSlot.disableProperty().unbind();
        trailingSlot.disableProperty().unbind();
        supportingRow.setDisable(false);
        leadingSlot.setDisable(false);
        trailingSlot.setDisable(false);
        if (oldInput != null) {
            oldInput.textProperty().removeListener(textListener);
            oldInput.paddingProperty().removeListener(paddingListener);
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

        TextInputControl newInput = getInput();
        if (newInput != null) {
            M3ControlStyles.add(newInput, INPUT_STYLE_CLASS);
            newInput.textProperty().addListener(textListener);
            supportingRow.disableProperty().bind(newInput.disabledProperty());
            leadingSlot.disableProperty().bind(newInput.disabledProperty());
            trailingSlot.disableProperty().bind(newInput.disabledProperty());
            installedInput = newInput;
            installedInputBasePadding = newInput.getPadding();
            newInput.paddingProperty().addListener(paddingListener);
            inputContainer.getChildren().add(0, newInput);
        }

        updateInputContainer();
        enforceCharacterLimit();
        updateInputPadding();
        updateTrailing();
        updateInputErrorState();
        updateSupportingRow();
    }

    /// Updates the input container visibility.
    private void updateInputContainer() {
        boolean visible = getInput() != null;
        inputContainer.setVisible(visible);
        inputContainer.setManaged(visible);
    }

    /// Updates the leading adornment slot.
    private void updateLeading() {
        Node leading = getLeading();
        leadingSlot.getChildren().clear();
        if (leading != null) {
            leadingSlot.getChildren().add(leading);
        }
        updateAdornmentSlot(leadingSlot, leading);
        updateInputPadding();
    }

    /// Updates the trailing adornment slot.
    private void updateTrailing() {
        @Nullable Node trailing = effectiveTrailing();
        @Nullable Node previousTrailing = installedTrailing;
        installedTrailing = trailing;
        trailingSlot.getChildren().clear();
        if (trailing != null) {
            trailingSlot.getChildren().add(trailing);
        }
        updateAdornmentSlot(trailingSlot, trailing);
        updateInputPadding();
        if (previousTrailing != trailing) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
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

        double left = getLeading() == null
                ? basePadding.getLeft()
                : Math.max(basePadding.getLeft(), ADORNED_HORIZONTAL_PADDING);
        double right = effectiveTrailing() == null
                ? basePadding.getRight()
                : Math.max(basePadding.getRight(), ADORNED_HORIZONTAL_PADDING);
        applyingInputPadding = true;
        try {
            input.setPadding(new Insets(basePadding.getTop(), right, basePadding.getBottom(), left));
        } finally {
            applyingInputPadding = false;
        }
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

        supportingLabel.setText(message);
        supportingLabel.setVisible(showMessage);
        supportingLabel.setManaged(showMessage);
        supportingLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);

        supportingSpacer.setVisible(showCounter);
        supportingSpacer.setManaged(showCounter);

        counterLabel.setText(characterCounterText());
        counterLabel.setVisible(showCounter);
        counterLabel.setManaged(showCounter);
        counterLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);

        supportingRow.setVisible(showRow);
        supportingRow.setManaged(showRow);
        supportingRow.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
    }

    /// Returns the supporting row text that should be visible.
    private String displayedSupportingText() {
        String errorText = getErrorText();
        return errorText.isEmpty() ? getSupportingText() : errorText;
    }

    /// Returns the formatted character counter text.
    private String characterCounterText() {
        int count = getCharacterCount();
        int limit = getCharacterLimit();
        return limit >= 0 ? count + " / " + limit : Integer.toString(count);
    }

    /// Returns whether error text or character overflow should render the error state.
    private boolean hasErrorState() {
        return !getErrorText().isEmpty() || isCharacterLimitExceeded();
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
        String message = displayedSupportingText();
        String counter = isCharacterCounterVisible() ? characterCounterText() : "";
        if (message.isEmpty()) {
            return counter;
        }
        return counter.isEmpty() ? message : message + " " + counter;
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

    /// Returns the text length of a JavaFX text input control.
    private static int textLength(TextInputControl input) {
        @Nullable String text = input.getText();
        return text == null ? 0 : text.length();
    }
}
