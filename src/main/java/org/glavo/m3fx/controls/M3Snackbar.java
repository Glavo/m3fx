package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.skins.M3SnackbarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 snackbar.
@NotNullByDefault
public class M3Snackbar extends Control {
    /// The base style class for m3fx snackbars.
    public static final String STYLE_CLASS = "m3-snackbar";

    /// The snackbar message text property.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// The action button text property.
    private final StringProperty actionText = new SimpleStringProperty(this, "actionText", "");

    /// The action event handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");

    /// Creates an empty snackbar.
    public M3Snackbar() {
        this("");
    }

    /// Creates a snackbar with message text.
    public M3Snackbar(String text) {
        M3ControlStyles.add(this, STYLE_CLASS);
        setText(text);
    }

    /// Returns the snackbar message text.
    public final String getText() {
        return text.get();
    }

    /// Sets the snackbar message text.
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the snackbar message text property.
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the action button text.
    public final String getActionText() {
        return actionText.get();
    }

    /// Sets the action button text.
    public final void setActionText(String actionText) {
        this.actionText.set(Objects.requireNonNull(actionText, "actionText"));
    }

    /// Returns the action button text property.
    public final StringProperty actionTextProperty() {
        return actionText;
    }

    /// Returns the action event handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action event handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action event handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Creates the default snackbar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SnackbarSkin(this);
    }
}
