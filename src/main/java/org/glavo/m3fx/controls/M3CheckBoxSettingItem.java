// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// An M3FX settings row with a trailing checkbox value.
///
/// Material Design 3 does not define a checkbox settings-row component. This extension combines list-item
/// presentation with checkbox state and is styled with Material tokens.
///
/// The inherited [#selectedProperty()] and [#indeterminateProperty()] represent the checkbox state. When
/// [#allowIndeterminateProperty()] is `false`, activation toggles selected and clears indeterminate. When it is
/// `true`, activation cycles unchecked, indeterminate, checked, and unchecked. Each accepted activation updates the
/// state before delivering one action event.
///
/// The visible checkbox is part of the row's presentation and is not an independent focus or pointer target. The
/// error state changes presentation only; it neither validates application data nor changes the selected or
/// indeterminate values. Applications may bind the properties to their own persisted state.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview) and
/// [Material Design checkboxes](https://m3.material.io/components/checkbox/overview).
@NotNullByDefault
public final class M3CheckBoxSettingItem extends M3SettingItemBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-checkbox-setting-item";

    /// The pseudo-class used while the checkbox is indeterminate.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS = PseudoClass.getPseudoClass("indeterminate");

    /// The pseudo-class used while the checkbox uses its error presentation.
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    /// The trailing checkbox presentation owned by this row.
    private final M3CheckBox indicator = new M3CheckBox();

    /// Whether this setting is in its indeterminate state.
    ///
    /// Programmatic changes are permitted regardless of [#allowIndeterminateProperty()].
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty indeterminate;

    /// Returns whether this setting is in its indeterminate state.
    ///
    /// @return `true` when the setting is indeterminate
    public boolean isIndeterminate() {
        return indeterminate != null && indeterminate.get();
    }

    /// Sets whether this setting is in its indeterminate state.
    ///
    /// @param indeterminate whether the setting is indeterminate
    public void setIndeterminate(boolean indeterminate) {
        indeterminateProperty().set(indeterminate);
    }

    /// Returns the observable, bindable indeterminate-state property.
    ///
    /// The property defaults to `false`. It may be set independently of [#selectedProperty()] and is reflected by
    /// the trailing checkbox and accessibility state.
    ///
    /// @return the indeterminate-state property
    public BooleanProperty indeterminateProperty() {
        if (indeterminate == null) {
            indeterminate = new BooleanPropertyBase(false) {
                /// Updates presentation and accessibility state after the value changes.
                @Override
                protected void invalidated() {
                    boolean active = get();
                    pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, active);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                    M3Accessible.notifyToggleStateChanged(M3CheckBoxSettingItem.this);
                }

                /// Returns the owning setting row.
                @Override
                public Object getBean() {
                    return M3CheckBoxSettingItem.this;
                }

                /// Returns the JavaFX property name.
                @Override
                public String getName() {
                    return "indeterminate";
                }
            };
        }
        return indeterminate;
    }

    /// Whether row activation includes indeterminate in its state cycle.
    ///
    /// Changing this property does not clear an existing indeterminate value.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty allowIndeterminate;

    /// Returns whether row activation includes the indeterminate state.
    ///
    /// @return `true` when activation cycles through indeterminate
    public boolean isAllowIndeterminate() {
        return allowIndeterminate != null && allowIndeterminate.get();
    }

    /// Sets whether row activation includes the indeterminate state.
    ///
    /// @param allowIndeterminate whether activation cycles through indeterminate
    public void setAllowIndeterminate(boolean allowIndeterminate) {
        allowIndeterminateProperty().set(allowIndeterminate);
    }

    /// Returns the observable, bindable property controlling indeterminate activation.
    ///
    /// The property defaults to `false`. It affects only future row activation and does not constrain direct changes
    /// to [#indeterminateProperty()].
    ///
    /// @return the allow-indeterminate property
    public BooleanProperty allowIndeterminateProperty() {
        if (allowIndeterminate == null) {
            allowIndeterminate = new SimpleBooleanProperty(this, "allowIndeterminate", false);
        }
        return allowIndeterminate;
    }

    /// Whether this setting uses its error presentation.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty error;

    /// Returns whether this setting uses its error presentation.
    ///
    /// @return `true` when the setting uses its error presentation
    public boolean isError() {
        return error != null && error.get();
    }

    /// Sets whether this setting uses its error presentation.
    ///
    /// The value changes presentation only. It does not perform validation or modify the checkbox state.
    ///
    /// @param error whether the setting should use its error presentation
    public void setError(boolean error) {
        if (this.error != null || error) {
            errorProperty().set(error);
        }
    }

    /// Returns the observable, bindable error-presentation property.
    ///
    /// The property defaults to `false`. It changes the row pseudo-class and trailing checkbox presentation without
    /// performing validation or modifying the checkbox state.
    ///
    /// @return the error-presentation property
    public BooleanProperty errorProperty() {
        if (error == null) {
            error = new BooleanPropertyBase(false) {
                /// Updates the Material error pseudo-class after the value changes.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(ERROR_PSEUDO_CLASS, get());
                }

                /// Returns the owning setting row.
                @Override
                public Object getBean() {
                    return M3CheckBoxSettingItem.this;
                }

                /// Returns the JavaFX property name.
                @Override
                public String getName() {
                    return "error";
                }
            };
        }
        return error;
    }

    /// Creates an unchecked, determinate checkbox setting row with an empty headline.
    public M3CheckBoxSettingItem() {
        this("");
    }

    /// Creates an unchecked, determinate checkbox setting row with the specified headline text.
    ///
    /// @param headlineText the primary row text
    /// @throws NullPointerException if `headlineText` is `null`
    public M3CheckBoxSettingItem(String headlineText) {
        super(headlineText, AccessibleRole.CHECK_BOX);
        addSettingStyleClass(DEFAULT_STYLE_CLASS);
        installTrailingIndicator(indicator);
        indicator.selectedProperty().bindBidirectional(selectedProperty());
        indicator.indeterminateProperty().bindBidirectional(indeterminateProperty());
        indicator.allowIndeterminateProperty().bindBidirectional(allowIndeterminateProperty());
        indicator.errorProperty().bindBidirectional(errorProperty());
        selectedProperty().addListener((observable, oldValue, newValue) ->
                M3Accessible.notifyToggleStateChanged(this));
    }

    /// Advances the checkbox state before dispatching the row action.
    ///
    /// @return always `true`, because an enabled checkbox setting always has a value transition
    @Override
    boolean prepareAction() {
        if (isAllowIndeterminate()) {
            if (!isSelected() && !isIndeterminate()) {
                setIndeterminate(true);
            } else if (isSelected() && !isIndeterminate()) {
                setSelected(false);
            } else if (isIndeterminate()) {
                setSelected(true);
                setIndeterminate(false);
            }
        } else {
            setSelected(!isSelected());
            setIndeterminate(false);
        }
        return true;
    }

    /// Returns accessibility attributes for the checkbox state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when unsupported
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (M3Accessible.isToggleStateAttribute(attribute)) {
            return M3Accessible.toggleState(isSelected(), isIndeterminate());
        }
        return switch (attribute) {
            case INDETERMINATE -> isIndeterminate();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }
}
