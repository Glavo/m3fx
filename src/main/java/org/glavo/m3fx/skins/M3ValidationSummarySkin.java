// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3ValidationSummary;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ScrollReveal;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3ValidationSummary].
@NotNullByDefault
public final class M3ValidationSummarySkin extends SkinBase<M3ValidationSummary> {
    /// The spacing between top-level summary content rows.
    private static final double CONTENT_SPACING = 10.0;

    /// The spacing between invalid item rows.
    private static final double ITEM_SPACING = 4.0;

    /// The spacing between the invalid item field label and error text.
    private static final double ITEM_TEXT_SPACING = 2.0;

    /// The internal content container.
    private final VBox container = new VBox(CONTENT_SPACING);

    /// The label that renders the summary title.
    private final Label titleLabel = new Label();

    /// The label that renders the valid empty state.
    private final Label emptyLabel = new Label();

    /// The container that renders invalid item rows.
    private final VBox items = new VBox(ITEM_SPACING);

    /// Updates skin content when simple summary properties change.
    private final InvalidationListener summaryListener = observable -> updateContent();

    /// Rebuilds orientation-sensitive item rows when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> {
        updateNodeOrientationLayout();
        updateContent();
    };

    /// Updates skin content when the validator invalid input list changes.
    private final ListChangeListener<M3TextInputLayout> invalidInputsListener = change -> updateContent();

    /// Updates skin content when a rendered invalid input changes display text.
    private final InvalidationListener invalidInputContentListener = observable -> updateContent();

    /// Invalid input layouts currently observed for row text changes.
    private final Set<M3TextInputLayout> observedInvalidInputs = Collections.newSetFromMap(new IdentityHashMap<>());

    /// Wrapped text inputs currently observed for prompt fallback changes.
    private final Map<M3TextInputLayout, TextInputControl> observedInvalidInputControls = new IdentityHashMap<>();

    /// Cached rendered item rows keyed by invalid input layout identity.
    private final Map<M3TextInputLayout, Node> itemRows = new IdentityHashMap<>();

    /// Moves invalid input listeners when the summary validator changes.
    private final ChangeListener<@Nullable M3FormValidator> validatorListener =
            (observable, oldValue, newValue) -> updateValidator(newValue);

    /// The validator currently observed by this skin.
    private @Nullable M3FormValidator observedValidator;

    /// Creates a validation summary skin.
    public M3ValidationSummarySkin(M3ValidationSummary control) {
        super(control);

        container.setManaged(false);
        container.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        items.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        titleLabel.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        emptyLabel.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        items.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        titleLabel.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        emptyLabel.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        titleLabel.getStyleClass().add(M3ValidationSummary.TITLE_STYLE_CLASS);
        emptyLabel.getStyleClass().add(M3ValidationSummary.EMPTY_TEXT_STYLE_CLASS);
        items.getStyleClass().add(M3ValidationSummary.ITEMS_STYLE_CLASS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        items.setMaxWidth(Double.MAX_VALUE);

        control.titleTextProperty().addListener(summaryListener);
        control.emptyTextProperty().addListener(summaryListener);
        control.showWhenValidProperty().addListener(summaryListener);
        control.visibleInvalidInputCountProperty().addListener(summaryListener);
        control.validatorProperty().addListener(validatorListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);

        updateValidator(control.getValidator());
        updateNodeOrientationLayout();
        updateContent();
        getChildren().add(container);
    }

    /// Removes listeners, bindings, and child references before disposal.
    @Override
    public void dispose() {
        M3ValidationSummary control = getSkinnable();
        control.titleTextProperty().removeListener(summaryListener);
        control.emptyTextProperty().removeListener(summaryListener);
        control.showWhenValidProperty().removeListener(summaryListener);
        control.visibleInvalidInputCountProperty().removeListener(summaryListener);
        control.validatorProperty().removeListener(validatorListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        container.alignmentProperty().unbind();
        items.nodeOrientationProperty().unbind();
        items.alignmentProperty().unbind();
        titleLabel.alignmentProperty().unbind();
        emptyLabel.alignmentProperty().unbind();
        updateValidator(null);
        updateObservedInvalidInputs(List.of());
        clearInvalidItemRows();
        container.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the internal content container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        if (!getSkinnable().isShowingSummary()) {
            return leftInset + rightInset;
        }
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the internal content container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        if (!getSkinnable().isShowingSummary()) {
            return topInset + bottomInset;
        }
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the internal content container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        if (!getSkinnable().isShowingSummary()) {
            return leftInset + rightInset;
        }
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the internal content container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        if (!getSkinnable().isShowingSummary()) {
            return topInset + bottomInset;
        }
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Lays out the internal content container in the summary content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Moves the invalid input listener to a new validator.
    private void updateValidator(@Nullable M3FormValidator validator) {
        if (observedValidator != null) {
            observedValidator.getInvalidInputs().removeListener(invalidInputsListener);
        }
        observedValidator = validator;
        if (observedValidator != null) {
            observedValidator.getInvalidInputs().addListener(invalidInputsListener);
        }
        updateContent();
    }

    /// Rebuilds visible summary content from the current validator state.
    private void updateContent() {
        M3ValidationSummary control = getSkinnable();
        boolean showingSummary = control.isShowingSummary();
        container.setVisible(showingSummary);
        container.setManaged(showingSummary);

        if (!showingSummary) {
            updateObservedInvalidInputs(List.of());
            clearInvalidItemRows();
            container.getChildren().clear();
            control.requestLayout();
            return;
        }

        titleLabel.setText(control.getTitleText());
        List<M3TextInputLayout> invalidInputs = shownInvalidInputs(control);
        updateObservedInvalidInputs(invalidInputs);
        if (invalidInputs.isEmpty()) {
            clearInvalidItemRows();
            emptyLabel.setText(control.getEmptyText());
            setContainerChildren(titleLabel, emptyLabel);
        } else {
            updateInvalidItemRows(invalidInputs);
            setContainerChildren(titleLabel, items);
        }
        control.requestLayout();
    }

    /// Updates listeners for invalid input row text and fallback prompt changes.
    private void updateObservedInvalidInputs(List<M3TextInputLayout> invalidInputs) {
        Set<M3TextInputLayout> nextInputs = Collections.newSetFromMap(new IdentityHashMap<>());
        nextInputs.addAll(invalidInputs);

        for (M3TextInputLayout input : List.copyOf(observedInvalidInputs)) {
            if (!nextInputs.contains(input)) {
                removeObservedInvalidInput(input);
            }
        }

        for (M3TextInputLayout input : invalidInputs) {
            if (observedInvalidInputs.add(input)) {
                input.labelTextProperty().addListener(invalidInputContentListener);
                input.validationErrorTextProperty().addListener(invalidInputContentListener);
                input.inputProperty().addListener(invalidInputContentListener);
            }
            updateObservedInvalidInputControl(input);
        }
    }

    /// Removes listeners from one invalid input row source.
    private void removeObservedInvalidInput(M3TextInputLayout input) {
        if (!observedInvalidInputs.remove(input)) {
            return;
        }
        input.labelTextProperty().removeListener(invalidInputContentListener);
        input.validationErrorTextProperty().removeListener(invalidInputContentListener);
        input.inputProperty().removeListener(invalidInputContentListener);
        @Nullable TextInputControl textInput = observedInvalidInputControls.remove(input);
        if (textInput != null) {
            textInput.promptTextProperty().removeListener(invalidInputContentListener);
        }
    }

    /// Updates the prompt listener for the wrapped input used as row-label fallback text.
    private void updateObservedInvalidInputControl(M3TextInputLayout input) {
        @Nullable TextInputControl oldInput = observedInvalidInputControls.get(input);
        @Nullable TextInputControl newInput = input.getInput();
        if (oldInput == newInput) {
            return;
        }
        if (oldInput != null) {
            oldInput.promptTextProperty().removeListener(invalidInputContentListener);
            observedInvalidInputControls.remove(input);
        }
        if (newInput != null) {
            newInput.promptTextProperty().addListener(invalidInputContentListener);
            observedInvalidInputControls.put(input, newInput);
        }
    }

    /// Returns invalid inputs that should be rendered by this summary.
    private static List<M3TextInputLayout> shownInvalidInputs(M3ValidationSummary control) {
        int invalidInputCount = control.getInvalidInputCount();
        ArrayList<M3TextInputLayout> inputs = new ArrayList<>(invalidInputCount);
        for (int index = 0; index < invalidInputCount; index++) {
            @Nullable M3TextInputLayout input = control.getInvalidInput(index);
            if (input != null && control.isInvalidInputShown(input)) {
                inputs.add(input);
            }
        }
        return inputs;
    }

    /// Updates top-level summary children without publishing redundant list changes.
    private void setContainerChildren(Node... children) {
        ObservableList<Node> currentChildren = container.getChildren();
        if (!sameNodes(currentChildren, List.of(children))) {
            currentChildren.setAll(children);
        }
    }

    /// Updates rendered invalid item rows while preserving existing row nodes when possible.
    private void updateInvalidItemRows(List<M3TextInputLayout> invalidInputs) {
        Set<M3TextInputLayout> nextInputs = Collections.newSetFromMap(new IdentityHashMap<>());
        nextInputs.addAll(invalidInputs);
        itemRows.keySet().removeIf(input -> !nextInputs.contains(input));

        ArrayList<Node> rows = new ArrayList<>(invalidInputs.size());
        for (M3TextInputLayout input : invalidInputs) {
            Node row = itemRows.get(input);
            if (row == null) {
                row = createItem(input);
                itemRows.put(input, row);
            } else {
                updateItem(row, input);
            }
            rows.add(row);
        }

        ObservableList<Node> children = items.getChildren();
        if (!sameNodes(children, rows)) {
            children.setAll(rows);
        }
    }

    /// Removes rendered invalid item rows and cached row references.
    private void clearInvalidItemRows() {
        items.getChildren().clear();
        itemRows.clear();
    }

    /// Returns whether two node lists contain the same nodes in the same order.
    private static boolean sameNodes(ObservableList<Node> first, List<Node> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int index = 0; index < first.size(); index++) {
            if (first.get(index) != second.get(index)) {
                return false;
            }
        }
        return true;
    }

    /// Updates text and orientation-sensitive alignment for one cached invalid item row.
    private void updateItem(Node item, M3TextInputLayout input) {
        item.setAccessibleText(itemLabel(input) + ": " + itemError(input));
        if (!(item instanceof StackPane stackPane)) {
            return;
        }

        for (Node child : stackPane.getChildren()) {
            if (child instanceof VBox text) {
                text.setAlignment(textAlignment());
                StackPane.setAlignment(text, textAlignment());
                updateItemTextLabels(text, input);
                return;
            }
        }
    }

    /// Updates labels hosted by one cached invalid item row text container.
    private void updateItemTextLabels(VBox text, M3TextInputLayout input) {
        for (Node child : text.getChildren()) {
            if (child instanceof Label label) {
                label.setAlignment(textAlignment());
                label.setTextAlignment(textTextAlignment());
                if (label.getStyleClass().contains(M3ValidationSummary.ITEM_LABEL_STYLE_CLASS)) {
                    label.setText(itemLabel(input));
                } else if (label.getStyleClass().contains(M3ValidationSummary.ITEM_ERROR_STYLE_CLASS)) {
                    label.setText(itemError(input));
                }
            }
        }
    }

    /// Creates one clickable invalid input item row.
    private Node createItem(M3TextInputLayout input) {
        M3StateLayer stateLayer = new M3StateLayer();
        StackPane item = new StackPane() {
            /// Lays out the state layer after ordinary stack-pane content has been positioned.
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                stateLayer.layoutLayer(0.0, 0.0, getWidth(), getHeight(), 8.0);
            }
        };
        item.getStyleClass().add(M3ValidationSummary.ITEM_STYLE_CLASS);
        item.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setAccessibleRole(AccessibleRole.BUTTON);
        item.setAccessibleText(itemLabel(input) + ": " + itemError(input));
        item.setFocusTraversable(true);
        item.setPickOnBounds(true);
        stateLayer.installStateTransitions(item);

        Label label = new Label(itemLabel(input));
        label.getStyleClass().add(M3ValidationSummary.ITEM_LABEL_STYLE_CLASS);
        label.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        label.setAlignment(textAlignment());
        label.setTextAlignment(textTextAlignment());
        label.setMaxWidth(Double.MAX_VALUE);
        Label error = new Label(itemError(input));
        error.getStyleClass().add(M3ValidationSummary.ITEM_ERROR_STYLE_CLASS);
        error.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        error.setAlignment(textAlignment());
        error.setTextAlignment(textTextAlignment());
        error.setWrapText(true);
        error.setMaxWidth(Double.MAX_VALUE);

        VBox text = new VBox(ITEM_TEXT_SPACING, label, error);
        text.setMaxWidth(Double.MAX_VALUE);
        text.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        text.setAlignment(textAlignment());
        StackPane.setAlignment(text, textAlignment());
        item.getChildren().addAll(stateLayer, text);

        item.setOnMousePressed(event -> handleItemMousePressed(stateLayer, event));
        item.setOnMouseReleased(event -> handleItemMouseReleased(item, stateLayer, input, event));
        item.setOnKeyPressed(event -> handleItemKeyPressed(item, input, event));
        return item;
    }

    /// Plays pointer feedback for an invalid item press.
    private void handleItemMousePressed(M3StateLayer stateLayer, MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            stateLayer.playRipple(event.getX(), event.getY());
            event.consume();
        }
    }

    /// Releases pointer feedback and focuses the related invalid input when release stays inside the row.
    private void handleItemMouseReleased(Node item, M3StateLayer stateLayer, M3TextInputLayout input, MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            stateLayer.releaseRipple();
            if (item.contains(event.getX(), event.getY())) {
                getSkinnable().focusInput(input);
            }
            event.consume();
        }
    }

    /// Returns the state layer hosted by one invalid item row.
    private static @Nullable M3StateLayer itemStateLayer(Node item) {
        if (item instanceof StackPane stackPane && !stackPane.getChildren().isEmpty()) {
            Node firstChild = stackPane.getChildren().get(0);
            if (firstChild instanceof M3StateLayer stateLayer) {
                return stateLayer;
            }
        }
        return null;
    }

    /// Handles activation and in-summary keyboard traversal for one invalid item row.
    private void handleItemKeyPressed(Node item, M3TextInputLayout input, KeyEvent event) {
        switch (event.getCode()) {
            case ENTER, SPACE -> {
                @Nullable M3StateLayer stateLayer = itemStateLayer(item);
                if (stateLayer != null) {
                    stateLayer.playCenteredRipple();
                    stateLayer.releaseRipple();
                }
                getSkinnable().focusInput(input);
                event.consume();
            }
            case UP -> {
                focusAdjacentItem(item, false);
                event.consume();
            }
            case DOWN -> {
                focusAdjacentItem(item, true);
                event.consume();
            }
            case HOME -> {
                if (focusIndexedItem(0)) {
                    event.consume();
                }
            }
            case END -> {
                if (focusIndexedItem(items.getChildren().size() - 1)) {
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Moves focus to the adjacent invalid item row when one exists.
    private boolean focusAdjacentItem(Node item, boolean forward) {
        int currentIndex = items.getChildren().indexOf(item);
        if (currentIndex < 0) {
            return false;
        }
        return focusIndexedItem(currentIndex + (forward ? 1 : -1));
    }

    /// Moves focus to one invalid item row by rendered row index.
    private boolean focusIndexedItem(int index) {
        if (index < 0 || index >= items.getChildren().size()) {
            return false;
        }

        Node item = items.getChildren().get(index);
        if (!item.isDisabled() && item.isVisible()) {
            return M3ScrollReveal.requestFocusAndReveal(getSkinnable(), item);
        }
        return false;
    }

    /// Updates orientation-dependent summary text alignment.
    private void updateNodeOrientationLayout() {
        titleLabel.setTextAlignment(textTextAlignment());
        emptyLabel.setTextAlignment(textTextAlignment());
    }

    /// Returns the current logical label-node alignment.
    private Pos textAlignment() {
        return M3NodeLayout.logicalStartCenterAlignment(getSkinnable());
    }

    /// Returns the current logical multi-line text alignment.
    private TextAlignment textTextAlignment() {
        return M3NodeLayout.isRightToLeft(getSkinnable()) ? TextAlignment.RIGHT : TextAlignment.LEFT;
    }

    /// Returns the field label shown for one invalid input item.
    private static String itemLabel(M3TextInputLayout input) {
        String label = input.getLabelText();
        if (!label.isBlank()) {
            return label;
        }

        @Nullable TextInputControl textInput = input.getInput();
        if (textInput != null && !textInput.getPromptText().isBlank()) {
            return textInput.getPromptText();
        }
        return "Field";
    }

    /// Returns the error text shown for one invalid input item.
    private static String itemError(M3TextInputLayout input) {
        String errorText = input.getValidationErrorText();
        return errorText.isBlank() ? "Invalid value" : errorText;
    }

}
