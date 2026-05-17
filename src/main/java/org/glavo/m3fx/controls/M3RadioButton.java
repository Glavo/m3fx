// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3RadioButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 radio button.
@NotNullByDefault
public class M3RadioButton extends RadioButton {
    /// The base style class for m3fx radio buttons.
    public static final String STYLE_CLASS = "m3-radio-button";

    /// The default radio button touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 40.0;

    /// The styleable touch target size token.
    private StyleableDoubleProperty touchTargetSize;

    /// Creates an empty radio button.
    public M3RadioButton() {
        initialize();
    }

    /// Creates a radio button with text.
    public M3RadioButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a radio button with text and the requested selected state.
    public static M3RadioButton withSelected(String text, boolean selected) {
        M3RadioButton radioButton = new M3RadioButton(text);
        radioButton.setSelected(selected);
        return radioButton;
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
                    return M3RadioButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "touchTargetSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3RadioButton, Number> getCssMetaData() {
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

    /// Creates the default Material Design 3 radio button skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3RadioButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.RADIO_BUTTON);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getTouchTargetSize();
        setMinHeight(size);
        setPrefHeight(size);
    }

    /// CSS metadata for m3fx radio button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3RadioButton, Number> TOUCH_TARGET_SIZE =
                new CssMetaData<>("-m3-touch-target-size", SizeConverter.getInstance(), DEFAULT_TOUCH_TARGET_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3RadioButton control) {
                        return M3Css.isSettable(control.touchTargetSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3RadioButton control) {
                        return control.touchTargetSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(RadioButton.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
