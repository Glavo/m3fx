// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorFieldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Provides a single-line hexadecimal editor for an [M3Color].
///
/// The field accepts `#RGB`, `#RGBA`, `#RRGGBB`, and `#RRGGBBAA`, with an optional leading `#`. Hexadecimal digits
/// are case-insensitive, short-form digits are expanded by repetition, and leading and trailing whitespace is
/// ignored when parsing. Enter or focus loss attempts to commit the transient text. Escape cancels the edit.
/// Applications may invoke the same operations through [#commit()] and [#cancelEdit()]. Text is not considered
/// invalid until a commit attempt fails; such a failure leaves the committed value unchanged and applies the
/// `:invalid` pseudo-class.
///
/// Three- and six-digit input preserves the current alpha channel. Four- and eight-digit input replaces it. A
/// successful commit converts the parsed RGB channels to the color space of the previously committed value, so an
/// HSL or HSB editing model is retained. Canonical text is uppercase and rounds each rendered channel to the
/// nearest unsigned 8-bit value; formatting alone does not modify [#valueProperty()].
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a corresponding standard component.
@NotNullByDefault
public final class M3ColorField extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-field";

    /// The pseudo-class applied while a commit has found invalid hexadecimal text.
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    /// The initial fully saturated red value.
    private static final M3Color DEFAULT_VALUE = new M3HsbColor(0.0, 1.0, 1.0);

    /// Creates an editable hexadecimal field initialized to opaque red.
    public M3ColorField() {
        initialize();
    }

    /// Creates an editable hexadecimal field with an initial value.
    ///
    /// The initial text is the uppercase canonical hexadecimal representation of `value`. Alpha is included when
    /// `value` is not fully opaque.
    ///
    /// @param value the initial color
    /// @throws NullPointerException if `value` is `null`
    public M3ColorField(M3Color value) {
        this();
        setValue(value);
    }

    /// The committed color.
    ///
    /// Changing this property reformats unbound [#textProperty()] from the new value and clears the invalid state.
    /// A bound text property is not replaced.
    ///
    /// @defaultValue fully saturated opaque red in HSB
    private final ObjectProperty<M3Color> value =
            M3ColorProperties.nonNullObjectProperty(
                    this,
                    "value",
                    DEFAULT_VALUE,
                    this::synchronizeTextFromValue
            );

    /// Returns the committed color.
    ///
    /// @return the current non-null color
    public M3Color getValue() {
        return value.get();
    }

    /// Sets the committed color.
    ///
    /// When this assignment changes [#valueProperty()] and [#textProperty()] is not bound, the transient text is
    /// replaced with the uppercase canonical hexadecimal representation of `value` and [#invalidProperty()] is
    /// cleared. As with other JavaFX properties, assigning the property's current value need not produce an
    /// invalidation.
    ///
    /// @param value the non-null color to commit
    /// @throws NullPointerException if `value` is `null`
    /// @throws RuntimeException if [#valueProperty()] is unidirectionally bound
    public void setValue(M3Color value) {
        this.value.set(value);
    }

    /// Returns the property containing the committed color.
    ///
    /// The property is never `null` when changed through [#setValue(M3Color)]. A unidirectional binding must also
    /// supply non-null values. While this property is unidirectionally bound, [#commit()] cannot store a parsed
    /// value.
    ///
    /// @return the committed-color property
    public ObjectProperty<M3Color> valueProperty() {
        return value;
    }

    /// The transient hexadecimal editor text.
    ///
    /// The text may differ from [#getValue()] until it is committed. A direct `null` assignment is normalized to an
    /// empty string. A unidirectional binding may supply `null`; [#getText()] presents that value as an empty string.
    ///
    /// @defaultValue `"#FF0000"`
    private final StringProperty text = new SimpleStringProperty(
            this,
            "text",
            M3ColorMath.formatHex(DEFAULT_VALUE, false)
    ) {
        /// Treats a direct null assignment as an empty edit.
        ///
        /// @param value the replacement text, or `null`
        @Override
        public void set(@Nullable String value) {
            super.set(value == null ? "" : value);
        }

        /// Clears a stale invalid state when the current edit becomes parseable.
        @Override
        protected void invalidated() {
            if (isInvalid() && M3ColorMath.parseHex(getText()) != null) {
                setInvalid(false);
            }
        }
    };

    /// Returns the transient hexadecimal editor text.
    ///
    /// @return the non-null editor text, or an empty string when the property value is `null`
    public String getText() {
        @Nullable String current = text.get();
        return current == null ? "" : current;
    }

    /// Replaces the transient editor text without committing it.
    ///
    /// Passing `null` is equivalent to passing an empty string. Parseable text clears an existing invalid state,
    /// but the committed [#getValue()] changes only after [#commit()].
    ///
    /// @param text the replacement text, or `null`
    /// @throws RuntimeException if [#textProperty()] is unidirectionally bound
    public void setText(@Nullable String text) {
        this.text.set(text);
    }

    /// Returns the property containing the transient editor text.
    ///
    /// A bound `null` value is presented by [#getText()] as an empty string. While this property is bound, changes
    /// to [#valueProperty()] and [#includeAlphaProperty()] do not replace the bound text, user edits are rejected by
    /// the editor, and [#cancelEdit()] has no effect.
    ///
    /// @return the transient-text property
    public StringProperty textProperty() {
        return text;
    }

    /// Whether the current transient text has failed a commit attempt.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper invalid = new ReadOnlyBooleanWrapper(this, "invalid");

    /// Returns whether the current editor text has failed a commit attempt.
    ///
    /// Invalid text does not change [#getValue()]. The state clears when the text becomes parseable, when an
    /// unbound edit is canceled, or when an unbound text value is reformatted from [#valueProperty()].
    ///
    /// @return `true` when the current edit is invalid
    public boolean isInvalid() {
        return invalid.get();
    }

    /// Returns the read-only property that reports a failed commit attempt.
    ///
    /// @return the invalid-state property
    public ReadOnlyBooleanProperty invalidProperty() {
        return invalid.getReadOnlyProperty();
    }

    /// Whether the text editor accepts user changes.
    ///
    /// @defaultValue `true`
    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);

    /// Returns whether user editing is enabled.
    ///
    /// This property affects user input only. [#setText(String)], [#commit()], and [#cancelEdit()] remain available
    /// to applications.
    ///
    /// @return `true` when the user can modify the editor text
    public boolean isEditable() {
        return editable.get();
    }

    /// Sets whether user editing is enabled.
    ///
    /// This operation does not change the current text or committed value.
    ///
    /// @param editable whether text can be modified
    /// @throws RuntimeException if [#editableProperty()] is unidirectionally bound
    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    /// Returns the property controlling whether the user can edit the text.
    ///
    /// @return the editable property
    public BooleanProperty editableProperty() {
        return editable;
    }

    /// Whether canonical editor text always includes an alpha byte.
    ///
    /// When `false`, alpha is omitted for fully opaque values and included for all other values. Changing this
    /// property immediately reformats unbound [#textProperty()] and clears the invalid state. It does not change
    /// [#valueProperty()].
    ///
    /// @defaultValue `false`
    private final BooleanProperty includeAlpha = new SimpleBooleanProperty(this, "includeAlpha") {
        /// Re-formats the transient text after the formatting policy changes.
        @Override
        protected void invalidated() {
            synchronizeTextFromValue();
        }
    };

    /// Returns whether canonical text always includes an alpha byte.
    ///
    /// @return `true` when alpha is always included
    public boolean isIncludeAlpha() {
        return includeAlpha.get();
    }

    /// Sets whether canonical text always includes an alpha byte.
    ///
    /// @param includeAlpha whether alpha is always included
    /// @throws RuntimeException if [#includeAlphaProperty()] is unidirectionally bound
    public void setIncludeAlpha(boolean includeAlpha) {
        this.includeAlpha.set(includeAlpha);
    }

    /// Returns the property controlling canonical alpha formatting.
    ///
    /// @return the `includeAlpha` property
    public BooleanProperty includeAlphaProperty() {
        return includeAlpha;
    }

    /// The prompt displayed while the editor text is empty.
    ///
    /// @defaultValue `"Hex color"`
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "Hex color");

    /// Returns the editor prompt.
    ///
    /// @return the prompt, or `null`
    public @Nullable String getPromptText() {
        return promptText.get();
    }

    /// Sets the editor prompt.
    ///
    /// @param promptText the prompt, or `null`
    /// @throws RuntimeException if [#promptTextProperty()] is unidirectionally bound
    public void setPromptText(@Nullable String promptText) {
        this.promptText.set(promptText);
    }

    /// Returns the property containing the editor prompt.
    ///
    /// @return the prompt-text property
    public StringProperty promptTextProperty() {
        return promptText;
    }

    /// Parses and, if valid, commits the current transient text.
    ///
    /// A successful commit preserves the color space of the previously committed value. Three- and six-digit forms
    /// preserve its alpha channel; four- and eight-digit forms replace it. If [#textProperty()] is not bound, the
    /// successful result is reformatted as uppercase `#RRGGBB` or `#RRGGBBAA`. A failed parse leaves both the
    /// committed value and transient text unchanged and sets [#invalidProperty()].
    ///
    /// If [#valueProperty()] is unidirectionally bound, a valid edit causes this method to throw before changing the
    /// committed value, transient text, or invalid state.
    ///
    /// @return `true` if the text was committed, or `false` if it was invalid
    /// @throws RuntimeException if [#valueProperty()] is unidirectionally bound
    public boolean commit() {
        @Nullable M3RgbColor parsed = M3ColorMath.parseHex(getText());
        if (parsed == null) {
            setInvalid(true);
            return false;
        }

        M3Color current = getValue();
        int digitCount = hexadecimalDigitCount(getText());
        double alpha = digitCount == 3 || digitCount == 6 ? current.getAlpha() : parsed.alpha();
        M3RgbColor committedRgb = new M3RgbColor(parsed.red(), parsed.green(), parsed.blue(), alpha);
        setValue(committedRgb.toColorSpace(current.getColorSpace()));
        setInvalid(false);
        synchronizeTextFromValue();
        return true;
    }

    /// Cancels the current edit and restores the canonical representation of the committed value.
    ///
    /// When [#textProperty()] is not bound, this method also clears [#invalidProperty()]. It has no effect while
    /// [#textProperty()] is bound.
    public void cancelEdit() {
        synchronizeTextFromValue();
    }

    /// Creates the default visual representation of this control.
    ///
    /// @return the non-null default skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ColorFieldSkin(this);
    }

    /// Returns the user-agent stylesheet for color fields.
    ///
    /// @return the color-field stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-field.css");
    }

    /// Initializes style and accessibility state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setAccessibleText("Color value");
        setFocusTraversable(false);
    }

    /// Replaces unbound transient text with the canonical committed representation.
    private void synchronizeTextFromValue() {
        if (text.isBound()) {
            return;
        }
        boolean withAlpha = isIncludeAlpha() || getValue().getAlpha() < 1.0;
        String formatted = M3ColorMath.formatHex(getValue(), withAlpha);
        if (!formatted.equals(getText())) {
            text.set(formatted);
        }
        setInvalid(false);
    }

    /// Applies or clears the invalid editing state.
    ///
    /// @param invalid whether the current edit is invalid
    private void setInvalid(boolean invalid) {
        if (this.invalid.get() == invalid) {
            return;
        }
        this.invalid.set(invalid);
        pseudoClassStateChanged(INVALID_PSEUDO_CLASS, invalid);
    }

    /// Returns the number of hexadecimal digits after trimming an optional `#`.
    ///
    /// @param text the hexadecimal input
    /// @return the number of hexadecimal digits
    private static int hexadecimalDigitCount(String text) {
        String trimmed = text.trim();
        return trimmed.startsWith("#") ? trimmed.length() - 1 : trimmed.length();
    }
}
