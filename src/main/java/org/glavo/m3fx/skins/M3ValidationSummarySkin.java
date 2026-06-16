// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3ValidationSummary;
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

    /// Moves invalid input listeners when the summary validator changes.
    private final ChangeListener<@Nullable M3FormValidator> validatorListener =
            (observable, oldValue, newValue) -> updateValidator(newValue);

    /// The validator currently observed by this skin.
    private @Nullable M3FormValidator observedValidator;

    /// Creates a validation summary skin.
    public M3ValidationSummarySkin(M3ValidationSummary control) {
        super(control);

        container.setManaged(false);
        container.setAlignment(Pos.CENTER_LEFT);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        items.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        titleLabel.getStyleClass().add(M3ValidationSummary.TITLE_STYLE_CLASS);
        emptyLabel.getStyleClass().add(M3ValidationSummary.EMPTY_TEXT_STYLE_CLASS);
        items.getStyleClass().add(M3ValidationSummary.ITEMS_STYLE_CLASS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        items.setMaxWidth(Double.MAX_VALUE);

        control.titleTextProperty().addListener(summaryListener);
        control.emptyTextProperty().addListener(summaryListener);
        control.showWhenValidProperty().addListener(summaryListener);
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
        control.validatorProperty().removeListener(validatorListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        items.nodeOrientationProperty().unbind();
        updateValidator(null);
        items.getChildren().clear();
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
        items.getChildren().clear();

        if (!showingSummary) {
            container.getChildren().clear();
            control.requestLayout();
            return;
        }

        titleLabel.setText(control.getTitleText());
        int invalidInputCount = control.getInvalidInputCount();
        if (invalidInputCount == 0) {
            emptyLabel.setText(control.getEmptyText());
            container.getChildren().setAll(titleLabel, emptyLabel);
        } else {
            for (int index = 0; index < invalidInputCount; index++) {
                @Nullable M3TextInputLayout input = control.getInvalidInput(index);
                if (input != null) {
                    items.getChildren().add(createItem(input));
                }
            }
            container.getChildren().setAll(titleLabel, items);
        }
        control.requestLayout();
    }

    /// Creates one clickable invalid input item row.
    private Node createItem(M3TextInputLayout input) {
        StackPane item = new StackPane();
        item.getStyleClass().add(M3ValidationSummary.ITEM_STYLE_CLASS);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setAccessibleRole(AccessibleRole.BUTTON);
        item.setAccessibleText(itemAccessibleText(input));
        item.setFocusTraversable(true);

        Label label = new Label(itemLabel(input));
        label.getStyleClass().add(M3ValidationSummary.ITEM_LABEL_STYLE_CLASS);
        label.setMaxWidth(Double.MAX_VALUE);
        Label error = new Label(itemError(input));
        error.getStyleClass().add(M3ValidationSummary.ITEM_ERROR_STYLE_CLASS);
        error.setWrapText(true);
        error.setMaxWidth(Double.MAX_VALUE);

        VBox text = new VBox(ITEM_TEXT_SPACING, label, error);
        text.setMaxWidth(Double.MAX_VALUE);
        text.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        text.setAlignment(textAlignment());
        StackPane.setAlignment(text, textAlignment());
        item.getChildren().add(text);

        item.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                getSkinnable().focusInput(input);
                event.consume();
            }
        });
        item.setOnKeyPressed(event -> handleItemKeyPressed(item, input, event));
        return item;
    }

    /// Handles activation and in-summary keyboard traversal for one invalid item row.
    private void handleItemKeyPressed(Node item, M3TextInputLayout input, KeyEvent event) {
        switch (event.getCode()) {
            case ENTER, SPACE -> {
                getSkinnable().focusInput(input);
                event.consume();
            }
            case UP -> {
                if (focusAdjacentItem(item, false)) {
                    event.consume();
                }
            }
            case DOWN -> {
                if (focusAdjacentItem(item, true)) {
                    event.consume();
                }
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
            item.requestFocus();
            return item.isFocused();
        }
        return false;
    }

    /// Updates orientation-dependent summary alignments.
    private void updateNodeOrientationLayout() {
        container.setAlignment(textAlignment());
        items.setAlignment(textAlignment());
        titleLabel.setAlignment(textAlignment());
        emptyLabel.setAlignment(textAlignment());
    }

    /// Returns the current logical text alignment.
    private Pos textAlignment() {
        return Pos.CENTER_LEFT;
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

    /// Returns the accessibility text shown for one invalid input item.
    private static String itemAccessibleText(M3TextInputLayout input) {
        return itemLabel(input) + ": " + itemError(input);
    }
}
