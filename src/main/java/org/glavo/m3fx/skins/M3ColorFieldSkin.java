// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.controls.M3ColorField;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ColorField].
///
/// The control owns transient text, validation state, and commit behavior. This skin presents that model through an
/// embedded JavaFX text editor and forwards focus and keyboard gestures.
@NotNullByDefault
public final class M3ColorFieldSkin extends SkinBase<M3ColorField> {
    /// The pseudo-class applied while the editor contains an invalid hexadecimal value.
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    /// The embedded single-line hexadecimal editor.
    private final TextField editor = new TextField();

    /// Whether editor text is being synchronized from the control model.
    private boolean synchronizingEditorText;

    /// Restores the committed representation when Escape is pressed.
    private final EventHandler<KeyEvent> keyPressedHandler = event -> {
        if (event.getCode() == KeyCode.ESCAPE) {
            getSkinnable().cancelEdit();
            event.consume();
        }
    };

    /// Commits a pending edit when keyboard focus leaves the editor.
    private final ChangeListener<Boolean> focusListener = (observable, oldValue, newValue) -> {
        if (oldValue && !newValue) {
            getSkinnable().commit();
        }
    };

    /// Synchronizes the editor after a transient-text model change.
    private final InvalidationListener controlTextListener = observable -> synchronizeEditorText();

    /// Copies user edits into the control-owned transient-text model.
    private final InvalidationListener editorTextListener = observable -> {
        if (synchronizingEditorText) {
            return;
        }
        M3ColorField control = getSkinnable();
        if (control.textProperty().isBound()) {
            synchronizeEditorText();
        } else {
            control.setText(editor.getText());
        }
    };

    /// Mirrors the control's validation state onto the embedded editor.
    private final InvalidationListener invalidListener = observable -> updateInvalidState();

    /// Creates a color-field skin.
    ///
    /// @param control the control managed by this skin
    /// @throws NullPointerException if `control` is `null`
    public M3ColorFieldSkin(M3ColorField control) {
        super(control);

        editor.getStyleClass().add("m3-color-field-editor");
        editor.editableProperty().bind(control.editableProperty());
        editor.promptTextProperty().bind(control.promptTextProperty());
        editor.setOnAction(event -> control.commit());
        editor.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        editor.focusedProperty().addListener(focusListener);
        editor.textProperty().addListener(editorTextListener);

        control.textProperty().addListener(controlTextListener);
        control.invalidProperty().addListener(invalidListener);

        getChildren().add(editor);
        synchronizeEditorText();
        updateInvalidState();
    }

    /// Releases listeners and bindings installed by this skin.
    @Override
    public void dispose() {
        M3ColorField control = getSkinnable();
        control.textProperty().removeListener(controlTextListener);
        control.invalidProperty().removeListener(invalidListener);

        editor.editableProperty().unbind();
        editor.promptTextProperty().unbind();
        editor.setOnAction(null);
        editor.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        editor.focusedProperty().removeListener(focusListener);
        editor.textProperty().removeListener(editorTextListener);
        getChildren().remove(editor);
        super.dispose();
    }

    /// Returns the minimum width required by the embedded editor.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + editor.minWidth(height) + rightInset;
    }

    /// Returns the minimum height required by the embedded editor.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + editor.minHeight(width) + bottomInset;
    }

    /// Returns the preferred width of the embedded editor.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + editor.prefWidth(height) + rightInset;
    }

    /// Returns the preferred height of the embedded editor.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + editor.prefHeight(width) + bottomInset;
    }

    /// Sizes the editor to the complete content box.
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        editor.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    /// Replaces editor text with the control-owned transient representation.
    private void synchronizeEditorText() {
        String text = getSkinnable().getText();
        synchronizingEditorText = true;
        try {
            if (!text.equals(editor.getText())) {
                int caret = Math.min(editor.getCaretPosition(), text.length());
                editor.setText(text);
                editor.positionCaret(caret);
            }
        } finally {
            synchronizingEditorText = false;
        }
    }

    /// Mirrors the control's invalid pseudo-class onto the embedded editor.
    private void updateInvalidState() {
        editor.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, getSkinnable().isInvalid());
    }
}
