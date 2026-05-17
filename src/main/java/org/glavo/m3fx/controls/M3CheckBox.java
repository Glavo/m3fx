// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3CheckBoxSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 checkbox.
@NotNullByDefault
public class M3CheckBox extends ButtonBase {
    /// The base style class for m3fx checkboxes.
    public static final String STYLE_CLASS = "m3-checkbox";

    /// The selected pseudo-class used by checkboxes.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The determinate pseudo-class used by checkboxes.
    private static final PseudoClass DETERMINATE_PSEUDO_CLASS = PseudoClass.getPseudoClass("determinate");

    /// The indeterminate pseudo-class used by checkboxes.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS = PseudoClass.getPseudoClass("indeterminate");

    /// The default checkbox touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 40.0;

    /// The styleable touch target size token.
    private StyleableDoubleProperty touchTargetSize;

    /// The selected state property.
    private BooleanProperty selected;

    /// The indeterminate state property.
    private BooleanProperty indeterminate;

    /// Whether user activation cycles through the indeterminate state.
    private BooleanProperty allowIndeterminate;

    /// Creates an empty checkbox.
    public M3CheckBox() {
        initialize();
    }

    /// Creates a checkbox with text.
    public M3CheckBox(String text) {
        super(text);
        initialize();
    }

    /// Creates a checkbox with text and the requested selected state.
    public static M3CheckBox withSelected(String text, boolean selected) {
        M3CheckBox checkBox = new M3CheckBox(text);
        checkBox.setSelected(selected);
        return checkBox;
    }

    /// Sets whether this checkbox is selected.
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns whether this checkbox is selected.
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Returns the selected state property.
    public final BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new BooleanPropertyBase(false) {
                /// Updates selected visual and accessibility state.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3CheckBox.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return selected;
    }

    /// Sets whether this checkbox is in its indeterminate state.
    public final void setIndeterminate(boolean indeterminate) {
        indeterminateProperty().set(indeterminate);
    }

    /// Returns whether this checkbox is in its indeterminate state.
    public final boolean isIndeterminate() {
        return indeterminate != null && indeterminate.get();
    }

    /// Returns the indeterminate state property.
    public final BooleanProperty indeterminateProperty() {
        if (indeterminate == null) {
            indeterminate = new BooleanPropertyBase(false) {
                /// Updates indeterminate visual and accessibility state.
                @Override
                protected void invalidated() {
                    boolean active = get();
                    pseudoClassStateChanged(DETERMINATE_PSEUDO_CLASS, !active);
                    pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, active);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3CheckBox.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indeterminate";
                }
            };
        }
        return indeterminate;
    }

    /// Sets whether user activation cycles through the indeterminate state.
    public final void setAllowIndeterminate(boolean allowIndeterminate) {
        allowIndeterminateProperty().set(allowIndeterminate);
    }

    /// Returns whether user activation cycles through the indeterminate state.
    public final boolean isAllowIndeterminate() {
        return allowIndeterminate != null && allowIndeterminate.get();
    }

    /// Returns the allow-indeterminate state property.
    public final BooleanProperty allowIndeterminateProperty() {
        if (allowIndeterminate == null) {
            allowIndeterminate = new SimpleBooleanProperty(this, "allowIndeterminate", false);
        }
        return allowIndeterminate;
    }

    /// Returns the preferred touch target size token.
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the preferred touch target size token property.
    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = new StyleableDoubleProperty(DEFAULT_TOUCH_TARGET_SIZE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "touchTargetSize"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3CheckBox.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "touchTargetSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3CheckBox, Number> getCssMetaData() {
                    return StyleableProperties.TOUCH_TARGET_SIZE;
                }
            };
        }
        return touchTargetSize;
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Toggles this checkbox and fires its action handler.
    @Override
    public void fire() {
        if (isDisabled()) {
            return;
        }

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
        fireEvent(new ActionEvent(this, this));
    }

    /// Creates the default Material Design 3 checkbox skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3CheckBoxSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Returns accessibility attributes for checkbox selection state.
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case INDETERMINATE -> isIndeterminate();
            case TOGGLE_STATE -> {
                if (isIndeterminate()) {
                    yield AccessibleAttribute.ToggleState.INDETERMINATE;
                }
                yield isSelected()
                        ? AccessibleAttribute.ToggleState.CHECKED
                        : AccessibleAttribute.ToggleState.UNCHECKED;
            }
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.CHECK_BOX);
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        pseudoClassStateChanged(DETERMINATE_PSEUDO_CLASS, true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getTouchTargetSize();
        setMinHeight(size);
        setPrefHeight(size);
    }

    /// CSS metadata for m3fx checkbox component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3CheckBox, Number> TOUCH_TARGET_SIZE =
                new CssMetaData<>("-m3-touch-target-size", SizeConverter.getInstance(), DEFAULT_TOUCH_TARGET_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3CheckBox control) {
                        return M3Css.isSettable(control.touchTargetSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3CheckBox control) {
                        return control.touchTargetSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
