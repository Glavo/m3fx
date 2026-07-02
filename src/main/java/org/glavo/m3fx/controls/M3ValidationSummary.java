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
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
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
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview) for the error and
/// supporting-text model that this summary complements.
@NotNullByDefault
public class M3ValidationSummary extends Control {
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

    // The form validator that supplies invalid input layouts.
    private final ObjectProperty<@Nullable M3FormValidator> validator =
            new SimpleObjectProperty<>(this, "validator");

    // The title displayed above invalid field entries.
    private final StringProperty titleText = new SimpleStringProperty(this, "titleText", "Fix the following fields") {
        /// Rejects null title text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "titleText"));
        }
    };

    // The text displayed when the summary is configured to render while valid.
    private final StringProperty emptyText = new SimpleStringProperty(this, "emptyText", "No validation issues") {
        /// Rejects null empty text values.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "emptyText"));
        }
    };

    // Whether the summary renders an empty state when no invalid inputs exist.
    private final BooleanProperty showWhenValid = new SimpleBooleanProperty(this, "showWhenValid", false);

    // The number of invalid input layouts that currently have a visible and enabled ancestor chain.
    private final ReadOnlyIntegerWrapper visibleInvalidInputCount =
            new ReadOnlyIntegerWrapper(this, "visibleInvalidInputCount");

    /// Updates summary state when the validator invalid input list changes.
    private final ListChangeListener<M3TextInputLayout> invalidInputsListener = change -> {
        updateReachabilityObservers();
        updateSummaryState();
    };

    /// Updates summary state when an invalid input or ancestor visibility chain changes.
    private final InvalidationListener reachabilityListener = observable -> {
        updateReachabilityObservers();
        updateSummaryState();
    };

    /// Nodes in current summary and invalid input ancestry chains observed for row visibility changes.
    private final Set<Node> observedReachabilityNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    /// Notifies accessibility clients when focus moves between invalid input layouts.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// Creates a validation summary with no validator.
    public M3ValidationSummary() {
        initialize();
    }

    /// Creates a validation summary for the supplied form validator.
    public M3ValidationSummary(M3FormValidator validator) {
        this();
        setValidator(validator);
    }

    /// Returns the form validator that supplies invalid fields.
    public final @Nullable M3FormValidator getValidator() {
        return validator.get();
    }

    /// Sets the form validator that supplies invalid fields.
    public final void setValidator(@Nullable M3FormValidator validator) {
        this.validator.set(validator);
    }

    /// Returns the form validator property.
    public final ObjectProperty<@Nullable M3FormValidator> validatorProperty() {
        return validator;
    }

    /// Returns the title displayed above invalid field entries.
    public final String getTitleText() {
        return titleText.get();
    }

    /// Sets the title displayed above invalid field entries.
    public final void setTitleText(String titleText) {
        this.titleText.set(Objects.requireNonNull(titleText, "titleText"));
    }

    /// Returns the summary title text property.
    public final StringProperty titleTextProperty() {
        return titleText;
    }

    /// Returns the text displayed when the summary renders a valid empty state.
    public final String getEmptyText() {
        return emptyText.get();
    }

    /// Sets the text displayed when the summary renders a valid empty state.
    public final void setEmptyText(String emptyText) {
        this.emptyText.set(Objects.requireNonNull(emptyText, "emptyText"));
    }

    /// Returns the valid empty-state text property.
    public final StringProperty emptyTextProperty() {
        return emptyText;
    }

    /// Returns whether the summary renders an empty state when no invalid inputs exist.
    public final boolean isShowWhenValid() {
        return showWhenValid.get();
    }

    /// Sets whether the summary renders an empty state when no invalid inputs exist.
    public final void setShowWhenValid(boolean showWhenValid) {
        this.showWhenValid.set(showWhenValid);
    }

    /// Returns the show-when-valid property.
    public final BooleanProperty showWhenValidProperty() {
        return showWhenValid;
    }

    /// Returns whether the summary currently has visible content.
    public final boolean isShowingSummary() {
        return isShowWhenValid() || getVisibleInvalidInputCount() > 0;
    }

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

    /// Returns the visible invalid input count property.
    ///
    /// @return the visible invalid input count property
    public final ReadOnlyIntegerProperty visibleInvalidInputCountProperty() {
        return visibleInvalidInputCount.getReadOnlyProperty();
    }

    /// Returns the number of invalid inputs currently reported by the validator.
    public final int getInvalidInputCount() {
        @Nullable M3FormValidator validator = getValidator();
        return validator == null ? 0 : validator.getInvalidInputs().size();
    }

    /// Returns the invalid input at the requested index.
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
    public final boolean isInvalidInputReachable(M3TextInputLayout input) {
        return isAccessibleInvalidInput(Objects.requireNonNull(input, "input"));
    }

    /// Requests focus for one invalid input layout if it belongs to the current validator.
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
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> accessibleText();
            case ITEM_COUNT -> accessibleInvalidInputCount();
            case ITEM_AT_INDEX -> accessibleInvalidInputAt(M3Accessible.indexParameter(parameters));
            case FOCUS_NODE -> accessibleFocusNode();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed invalid inputs.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleInput(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Initializes style classes, accessibility metadata, and validator listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
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
            oldValidator.getInvalidInputs().removeListener(invalidInputsListener);
        }
        if (newValidator != null) {
            newValidator.getInvalidInputs().addListener(invalidInputsListener);
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
                current.visibleProperty().addListener(reachabilityListener);
                current.disabledProperty().addListener(reachabilityListener);
                current.parentProperty().addListener(reachabilityListener);
            }
            current = current.getParent();
        }
    }

    /// Removes all summary and invalid input ancestry listeners.
    private void removeReachabilityObservers() {
        for (Node node : observedReachabilityNodes) {
            node.visibleProperty().removeListener(reachabilityListener);
            node.disabledProperty().removeListener(reachabilityListener);
            node.parentProperty().removeListener(reachabilityListener);
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

        if (isSummaryIndexRequest(parameters) || isDirectInvalidInputRequest(input, parameters)) {
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

    /// Returns the number of invalid inputs currently exposed to accessibility clients.
    private int accessibleInvalidInputCount() {
        return reachableInvalidInputCount();
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

    /// Returns whether the first action parameter is a summary invalid-input index.
    private static boolean isSummaryIndexRequest(Object... parameters) {
        return parameters.length > 0 && parameters[0] instanceof Number;
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
