// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ValidationSummarySkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/// Displays the invalid fields reported by an [M3FormValidator].
///
/// `M3ValidationSummary` observes an [M3FormValidator] and renders a compact list of invalid
/// [M3TextInputLayout] controls. It can show an empty valid state, include field labels and error text, and move
/// focus to the related field when an invalid entry is activated.
///
/// The summary observes the form validator but does not initiate validation. Application code normally validates
/// the form in its submission path:
///
/// ```java
/// M3TextInputLayout name = new M3TextInputLayout(new M3TextField(), "Name", "");
/// name.setValidator(M3TextInputValidators.required("Name is required"));
/// M3FormValidator form = new M3FormValidator(name);
/// M3ValidationSummary summary = new M3ValidationSummary(form);
/// boolean valid = form.validate();
/// ```
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview) for the error and
/// supporting-text model that this summary complements.
@NotNullByDefault
public final class M3ValidationSummary extends Control {
    /// The base style class for M3FX validation summaries.
    public static final String STYLE_CLASS = "m3-validation-summary";

    /// The style class applied to the summary title label.
    public static final String TITLE_STYLE_CLASS = "m3-validation-summary-title";

    /// The style class applied to the empty-state label.
    public static final String EMPTY_TEXT_STYLE_CLASS = "m3-validation-summary-empty-text";

    /// The style class applied to the internal invalid item container.
    public static final String ITEMS_STYLE_CLASS = "m3-validation-summary-items";

    /// The style class applied to each invalid item row.
    public static final String ITEM_STYLE_CLASS = "m3-validation-summary-item";

    /// The style class applied to the invalid item field label.
    public static final String ITEM_LABEL_STYLE_CLASS = "m3-validation-summary-item-label";

    /// The style class applied to the invalid item error label.
    public static final String ITEM_ERROR_STYLE_CLASS = "m3-validation-summary-item-error";

    /// The pseudo-class used while the summary has no rendered content.
    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");

    /// Updates summary state when the validator invalid input list changes.
    private final ListChangeListener<M3TextInputLayout> invalidInputsListener = change -> {
        updateReachabilityObservers();
        updateSummaryState();
    };

    /// Weak invalid-input listener that avoids retaining the summary through a long-lived validator.
    private final WeakListChangeListener<M3TextInputLayout> weakInvalidInputsListener =
            new WeakListChangeListener<>(invalidInputsListener);

    /// Updates summary state when an observed node changes visibility or disabled state.
    private final InvalidationListener reachabilityStateListener = observable -> updateSummaryState();

    /// Weak state listener that avoids retaining the summary through observed external nodes.
    private final WeakInvalidationListener weakReachabilityStateListener =
            new WeakInvalidationListener(reachabilityStateListener);

    /// Rebuilds reachability observation when an invalid input or ancestor changes parent.
    private final InvalidationListener reachabilityParentListener = observable -> {
        updateReachabilityObservers();
        updateSummaryState();
    };

    /// Weak parent listener that avoids retaining the summary through observed external nodes.
    private final WeakInvalidationListener weakReachabilityParentListener =
            new WeakInvalidationListener(reachabilityParentListener);

    /// Nodes in current summary and invalid input ancestry chains observed for row visibility changes.
    private final Set<Node> observedReachabilityNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    /// Notifies accessibility clients when focus moves between invalid input layouts.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// Creates a validation summary with no validator.
    ///
    /// The new summary uses the default title and valid-state text and remains visually empty until a validator with
    /// invalid inputs is assigned, unless [#showWhenValidProperty()] is enabled.
    public M3ValidationSummary() {
        initialize();
    }

    /// Creates a validation summary for the supplied form validator.
    ///
    /// @param validator the validator whose invalid-input list should be observed
    /// @throws NullPointerException if `validator` is `null`
    public M3ValidationSummary(M3FormValidator validator) {
        this();
        setValidator(Objects.requireNonNull(validator, "validator"));
    }

    /// The form validator that supplies invalid input layouts.
    ///
    /// The default is `null`. Replacing the value detaches observation from the previous validator and immediately
    /// reflects the replacement's current invalid-input list. It does not run validation.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3FormValidator> validator =
            new SimpleObjectProperty<>(this, "validator");

    /// Returns the form validator that supplies invalid fields.
    ///
    /// @return the observed validator, or `null` when this summary is detached
    public final @Nullable M3FormValidator getValidator() {
        return validator.get();
    }

    /// Sets the form validator that supplies invalid fields.
    ///
    /// The summary stops observing the previous validator before observing the replacement.
    /// Passing `null` detaches the summary and clears its rendered invalid rows.
    ///
    /// @param validator the validator to observe, or `null` to detach the summary
    public final void setValidator(@Nullable M3FormValidator validator) {
        this.validator.set(validator);
    }

    /// Returns the `validator` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `validator` property
    public final ObjectProperty<@Nullable M3FormValidator> validatorProperty() {
        return validator;
    }

    /// The title displayed above invalid field entries.
    ///
    /// An empty string suppresses the title. The value cannot be `null`.
    ///
    /// @defaultValue `"Fix the following fields"`
    private final StringProperty titleText = new SimpleStringProperty(this, "titleText", "Fix the following fields") {
        /// Rejects null title text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "titleText"));
        }
    };

    /// Returns the title displayed above invalid field entries.
    ///
    /// @return the title text; never `null`
    public final String getTitleText() {
        return titleText.get();
    }

    /// Sets the title displayed above invalid field entries.
    ///
    /// @param titleText the title text, or an empty string to suppress it
    /// @throws NullPointerException if `titleText` is `null`
    public final void setTitleText(String titleText) {
        this.titleText.set(Objects.requireNonNull(titleText, "titleText"));
    }

    /// Returns the `titleText` property.
    ///
    /// The returned property is observable and bindable. Its default value is `"Fix the following fields"`.
    ///
    /// @return the `titleText` property
    public final StringProperty titleTextProperty() {
        return titleText;
    }

    /// The text displayed when the summary is configured to render while valid.
    ///
    /// An empty string suppresses the valid-state message. The value cannot be `null`.
    ///
    /// @defaultValue `"No validation issues"`
    private final StringProperty emptyText = new SimpleStringProperty(this, "emptyText", "No validation issues") {
        /// Rejects null empty text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "emptyText"));
        }
    };

    /// Returns the text displayed when the summary renders a valid empty state.
    ///
    /// @return the valid-state text; never `null`
    public final String getEmptyText() {
        return emptyText.get();
    }

    /// Sets the text displayed when the summary renders a valid empty state.
    ///
    /// @param emptyText the valid-state text, or an empty string for no message
    /// @throws NullPointerException if `emptyText` is `null`
    public final void setEmptyText(String emptyText) {
        this.emptyText.set(Objects.requireNonNull(emptyText, "emptyText"));
    }

    /// Returns the `emptyText` property.
    ///
    /// The returned property is observable and bindable. Its default value is `"No validation issues"`.
    ///
    /// @return the `emptyText` property
    public final StringProperty emptyTextProperty() {
        return emptyText;
    }

    /// Whether the summary renders an empty state when no invalid inputs exist.
    ///
    /// @defaultValue `false`
    private final BooleanProperty showWhenValid = new SimpleBooleanProperty(this, "showWhenValid", false);

    /// Returns whether the summary renders an empty state when no invalid inputs exist.
    ///
    /// @return whether a valid-state summary is shown; the default is `false`
    public final boolean isShowWhenValid() {
        return showWhenValid.get();
    }

    /// Sets whether the summary renders an empty state when no invalid inputs exist.
    ///
    /// @param showWhenValid whether the summary should remain visible while the form is valid
    public final void setShowWhenValid(boolean showWhenValid) {
        this.showWhenValid.set(showWhenValid);
    }

    /// Returns the `showWhenValid` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `showWhenValid` property
    public final BooleanProperty showWhenValidProperty() {
        return showWhenValid;
    }

    /// The number of invalid input layouts that currently have a visible and enabled ancestor chain.
    private final ReadOnlyIntegerWrapper visibleInvalidInputCount =
            new ReadOnlyIntegerWrapper(this, "visibleInvalidInputCount");

    /// Returns the number of invalid inputs currently shown by this summary.
    ///
    /// This count excludes invalid inputs hidden or disabled through their ancestor chain. It may include inputs that
    /// are not attached to the same scene as the summary so standalone summaries can render validation results before
    /// a form preview is mounted.
    ///
    /// @return the number of invalid inputs currently shown by this summary
    public final int getVisibleInvalidInputCount() {
        return visibleInvalidInputCount.get();
    }

    /// Returns the `visibleInvalidInputCount` property.
    ///
    /// The returned property is observable and read-only. Its default value is `0`.
    ///
    /// @return the `visibleInvalidInputCount` property
    public final ReadOnlyIntegerProperty visibleInvalidInputCountProperty() {
        return visibleInvalidInputCount.getReadOnlyProperty();
    }

    /// Returns whether the summary currently has visible content.
    ///
    /// @return whether invalid rows or the configured valid-state message should be rendered
    public final boolean isShowingSummary() {
        return isShowWhenValid() || getVisibleInvalidInputCount() > 0;
    }

    /// Returns the number of invalid inputs currently reported by the validator.
    ///
    /// Unlike [#getVisibleInvalidInputCount()], this count includes invalid inputs hidden or disabled by an ancestor.
    ///
    /// @return the validator's complete invalid-input count, or zero when no validator is installed
    public final int getInvalidInputCount() {
        @Nullable M3FormValidator validator = getValidator();
        return validator == null ? 0 : validator.getInvalidInputs().size();
    }

    /// Returns the invalid input at the requested index.
    ///
    /// @param index the zero-based index in the validator's complete invalid-input list
    /// @return the invalid input, or `null` when no validator is installed or the index is outside the list
    public final @Nullable M3TextInputLayout getInvalidInput(int index) {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || index < 0 || index >= validator.getInvalidInputs().size()) {
            return null;
        }
        return validator.getInvalidInputs().get(index);
    }

    /// Returns whether the supplied invalid input should be rendered by this summary.
    ///
    /// A shown invalid input belongs to the current validator and has a visible and enabled ancestor chain. The input
    /// does not have to share a scene with the summary, which allows standalone summaries and form previews to render
    /// validation results before the corresponding fields are attached to the same scene.
    ///
    /// @param input the invalid input layout to test
    /// @return `true` when the input should be rendered by this summary
    /// @throws NullPointerException if `input` is `null`
    public final boolean isInvalidInputShown(M3TextInputLayout input) {
        return isShownInvalidInput(Objects.requireNonNull(input, "input"));
    }

    /// Returns whether the supplied invalid input can currently be focused or revealed from this summary.
    ///
    /// A reachable invalid input is shown by this summary and can be reached from the current scene when the summary is
    /// attached to one.
    ///
    /// @param input the invalid input layout to test
    /// @return `true` when the input is reachable from this summary
    /// @throws NullPointerException if `input` is `null`
    public final boolean isInvalidInputReachable(M3TextInputLayout input) {
        return isAccessibleInvalidInput(Objects.requireNonNull(input, "input"));
    }

    /// Requests focus for one invalid input layout if it belongs to the current validator.
    ///
    /// The input is revealed through its containing controls before focus is requested.
    ///
    /// @param input the invalid input to reveal and focus
    /// @return `true` when the input was reachable and focus was requested; otherwise `false`
    /// @throws NullPointerException if `input` is `null`
    public final boolean focusInput(M3TextInputLayout input) {
        M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || !isAccessibleInvalidInput(validatedInput)) {
            return false;
        }

        @Nullable Node focusTarget = invalidFocusNode(validatedInput);
        if (focusTarget != null && M3Accessible.showItem(this, focusTarget)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns the user-agent stylesheet for M3FX validation summaries.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("validation-summary.css");
    }

    /// Creates the default Material Design 3 validation summary skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ValidationSummarySkin(this);
    }

    /// Returns accessibility attributes for the invalid input collection.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> accessibleText();
            case ITEM_COUNT -> reachableInvalidInputCount();
            case ITEM_AT_INDEX -> accessibleInvalidInputAt(M3Accessible.indexParameter(parameters));
            case FOCUS_NODE -> accessibleFocusNode();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed invalid inputs.
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
            case SHOW_ITEM -> showAccessibleInput(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Initializes style classes, accessibility metadata, and validator listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleInput);
        validator.addListener((observable, oldValue, newValue) -> updateValidator(oldValue, newValue));
        titleText.addListener(observable -> updateSummaryState());
        emptyText.addListener(observable -> updateSummaryState());
        showWhenValid.addListener(observable -> updateSummaryState());
        focusNotifier.start();
        updateSummaryState();
    }

    /// Moves invalid input listeners from the old validator to the new validator.
    private void updateValidator(@Nullable M3FormValidator oldValidator, @Nullable M3FormValidator newValidator) {
        if (oldValidator != null) {
            oldValidator.getInvalidInputs().removeListener(weakInvalidInputsListener);
        }
        if (newValidator != null) {
            newValidator.getInvalidInputs().addListener(weakInvalidInputsListener);
        }
        updateReachabilityObservers();
        updateSummaryState();
    }

    /// Updates listeners for summary and invalid input ancestry chains that affect row visibility.
    private void updateReachabilityObservers() {
        removeReachabilityObservers();
        observeReachabilityChain(this);

        @Nullable M3FormValidator validator = getValidator();
        if (validator == null) {
            return;
        }

        for (M3TextInputLayout input : validator.getInvalidInputs()) {
            observeReachabilityChain(input);
        }
    }

    /// Observes one invalid input and its current parent chain.
    private void observeReachabilityChain(Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (observedReachabilityNodes.add(current)) {
                current.visibleProperty().addListener(weakReachabilityStateListener);
                current.disabledProperty().addListener(weakReachabilityStateListener);
                current.parentProperty().addListener(weakReachabilityParentListener);
            }
            current = current.getParent();
        }
    }

    /// Removes all summary and invalid input ancestry listeners.
    private void removeReachabilityObservers() {
        for (Node node : observedReachabilityNodes) {
            node.visibleProperty().removeListener(weakReachabilityStateListener);
            node.disabledProperty().removeListener(weakReachabilityStateListener);
            node.parentProperty().removeListener(weakReachabilityParentListener);
        }
        observedReachabilityNodes.clear();
    }

    /// Updates pseudo-classes and accessibility notifications after summary content changes.
    private void updateSummaryState() {
        visibleInvalidInputCount.set(shownInvalidInputCount());
        boolean empty = !isShowingSummary();
        pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, empty);
        setAccessibleText(accessibleText());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
        requestLayout();
    }

    /// Returns the current accessibility focus node.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node focusNode = currentFocusNode();
        return focusNode == null ? firstInvalidFocusNode() : focusNode;
    }

    /// Returns the currently focused invalid input target, or `null` when focus is outside the invalid input list.
    private @Nullable Node currentFocusNode() {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || !M3Accessible.canReach(this)) {
            return null;
        }

        @Nullable Node externalFocusTarget = activeInvalidInputExternalFocusTarget(validator);
        if (externalFocusTarget != null) {
            return externalFocusTarget;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        if (!M3Accessible.canReach(focusOwner)) {
            return null;
        }

        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (!isAccessibleInvalidInput(invalidInput)) {
                continue;
            }
            if (M3Accessible.containsNode(invalidInput, focusOwner)) {
                @Nullable Node focusTarget = M3Accessible.accessibleFocusTarget(invalidInput);
                return focusTarget == null ? invalidFocusNode(invalidInput) : focusTarget;
            }
        }
        return null;
    }

    /// Returns an active popup or overlay focus target exposed by a reachable invalid input.
    private @Nullable Node activeInvalidInputExternalFocusTarget(M3FormValidator validator) {
        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (!isAccessibleInvalidInput(invalidInput)) {
                continue;
            }
            @Nullable Node focusTarget = M3Accessible.activeExternalFocusTarget(this, invalidInput);
            if (focusTarget != null) {
                return focusTarget;
            }
        }
        return null;
    }

    /// Returns the focus target for the first invalid input.
    private @Nullable Node firstInvalidFocusNode() {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null) {
            return null;
        }

        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            @Nullable Node focusNode = invalidFocusNode(invalidInput);
            if (focusNode != null) {
                return focusNode;
            }
        }
        return null;
    }

    /// Returns the preferred focus target for one invalid input layout.
    private @Nullable Node invalidFocusNode(M3TextInputLayout invalidInput) {
        if (!isAccessibleInvalidInput(invalidInput)) {
            return null;
        }
        @Nullable TextInputControl textInput = invalidInput.getInput();
        @Nullable Node textInputFocusTarget = M3Accessible.focusTarget(textInput);
        return textInputFocusTarget != null ? textInputFocusTarget : M3Accessible.focusTarget(invalidInput);
    }

    /// Requests focus for the current accessible invalid input target.
    final boolean focusAccessibleNode() {
        @Nullable Node focusNode = accessibleFocusNode();
        if (focusNode == null) {
            return false;
        }
        if (!M3Accessible.showItem(this, focusNode)) {
            return false;
        }
        notifyFocusNodeChanged();
        return true;
    }

    /// Shows and focuses the requested invalid input or one of its descendant accessibility targets.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the requested invalid input or descendant target
    final boolean showAccessibleInput(Object... parameters) {
        if (parameters.length == 0) {
            return focusAccessibleNode();
        }

        @Nullable M3TextInputLayout input = accessibleActionInput(parameters);
        if (input == null) {
            return false;
        }

        if (parameters[0] instanceof Number || isDirectInvalidInputRequest(input, parameters)) {
            return focusInput(input);
        }

        if (M3Accessible.showAccessibleActionTarget(this, input, parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns the invalid input referenced by accessibility action parameters.
    private @Nullable M3TextInputLayout accessibleActionInput(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return firstAccessibleInvalidInput();
        }
        if (parameters[0] instanceof Number) {
            return accessibleInvalidInputAt(M3Accessible.indexParameter(parameters));
        }
        for (Object parameter : parameters) {
            @Nullable M3TextInputLayout input = accessibleActionInput(parameter);
            if (input != null) {
                return input;
            }
        }
        return null;
    }

    /// Returns the invalid input referenced by one accessibility action parameter.
    private @Nullable M3TextInputLayout accessibleActionInput(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return accessibleInvalidInputAt(number.intValue());
        }
        if (parameter instanceof M3TextInputLayout input && isAccessibleInvalidInput(input)) {
            return input;
        }
        if (parameter instanceof Node node) {
            return invalidInputContaining(node);
        }
        @Nullable M3TextInputLayout exposingInput = invalidInputExposing(parameter);
        if (exposingInput != null) {
            return exposingInput;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable M3TextInputLayout input = accessibleActionInput(value);
                if (input != null) {
                    return input;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable M3TextInputLayout input = accessibleActionInput(value);
                if (input != null) {
                    return input;
                }
            }
        }
        return null;
    }

    /// Returns whether the supplied input is in the current invalid input list.
    private boolean containsInvalidInput(M3TextInputLayout input) {
        @Nullable M3FormValidator validator = getValidator();
        return validator != null && validator.getInvalidInputs().contains(input);
    }

    /// Returns the number of invalid inputs that should be rendered by this summary.
    private int shownInvalidInputCount() {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || !M3Accessible.isEffectivelyReachable(this)) {
            return 0;
        }

        int count = 0;
        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (isShownInvalidInput(invalidInput)) {
                count++;
            }
        }
        return count;
    }

    /// Returns the number of invalid inputs that can currently be focused from this summary.
    private int reachableInvalidInputCount() {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || !M3Accessible.isEffectivelyReachable(this)) {
            return 0;
        }

        int count = 0;
        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (isAccessibleInvalidInput(invalidInput)) {
                count++;
            }
        }
        return count;
    }

    /// Returns one accessibility-reachable invalid input by visible invalid-input index.
    private @Nullable M3TextInputLayout accessibleInvalidInputAt(int index) {
        if (index < 0) {
            return null;
        }
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || !M3Accessible.isEffectivelyReachable(this)) {
            return null;
        }

        int reachableIndex = 0;
        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (!isAccessibleInvalidInput(invalidInput)) {
                continue;
            }
            if (reachableIndex == index) {
                return invalidInput;
            }
            reachableIndex++;
        }
        return null;
    }

    /// Returns the first invalid input currently exposed to accessibility clients.
    private @Nullable M3TextInputLayout firstAccessibleInvalidInput() {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null || !M3Accessible.isEffectivelyReachable(this)) {
            return null;
        }

        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (isAccessibleInvalidInput(invalidInput)) {
                return invalidInput;
            }
        }
        return null;
    }

    /// Returns whether one invalid input belongs to this summary and has a visible ancestor chain.
    private boolean isShownInvalidInput(M3TextInputLayout input) {
        return containsInvalidInput(input)
                && M3Accessible.isEffectivelyReachable(this)
                && M3Accessible.isEffectivelyReachable(input);
    }

    /// Returns whether one invalid input belongs to this summary and has a reachable ancestor chain.
    private boolean isAccessibleInvalidInput(M3TextInputLayout input) {
        return isShownInvalidInput(input)
                && (getScene() == null || M3Accessible.canReach(input));
    }

    /// Returns the invalid input that owns or contains the supplied node.
    private @Nullable M3TextInputLayout invalidInputContaining(Node node) {
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null) {
            return null;
        }

        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (!isAccessibleInvalidInput(invalidInput)) {
                continue;
            }
            if (node == invalidInput
                    || node == invalidInput.getInput()
                    || M3Accessible.containsNode(invalidInput, node)
                    || M3Accessible.containsAccessibleActionTarget(invalidInput, node)) {
                return invalidInput;
            }
        }
        return null;
    }

    /// Returns the invalid input whose accessibility tree exposes the supplied action parameter.
    private @Nullable M3TextInputLayout invalidInputExposing(@Nullable Object parameter) {
        if (parameter == null) {
            return null;
        }
        @Nullable M3FormValidator validator = getValidator();
        if (validator == null) {
            return null;
        }

        for (M3TextInputLayout invalidInput : validator.getInvalidInputs()) {
            if (!isAccessibleInvalidInput(invalidInput)) {
                continue;
            }
            if (M3Accessible.containsAccessibleActionTarget(invalidInput, parameter)) {
                return invalidInput;
            }
        }
        return null;
    }

    /// Returns whether action parameters directly reference the invalid input itself.
    private static boolean isDirectInvalidInputRequest(M3TextInputLayout input, Object... parameters) {
        for (Object parameter : parameters) {
            if (isDirectInvalidInputRequest(input, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one action parameter directly references the invalid input itself.
    private static boolean isDirectInvalidInputRequest(M3TextInputLayout input, @Nullable Object parameter) {
        if (parameter == input) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (isDirectInvalidInputRequest(input, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (isDirectInvalidInputRequest(input, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the current accessibility summary text.
    private String accessibleText() {
        if (getVisibleInvalidInputCount() == 0 && isShowWhenValid()) {
            return getTitleText() + " " + getEmptyText();
        }
        return getTitleText();
    }
}
