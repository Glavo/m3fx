// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.CheckBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 checkbox.
@NotNullByDefault
public class M3CheckBox extends CheckBox {
    /// The base style class for m3fx checkboxes.
    public static final String STYLE_CLASS = "m3-checkbox";

    /// The default checkbox touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 40.0;

    /// The styleable touch target size token.
    private StyleableDoubleProperty touchTargetSize;

    /// Creates an empty checkbox.
    public M3CheckBox() {
        initialize();
    }

    /// Creates a checkbox with text.
    public M3CheckBox(String text) {
        super(text);
        initialize();
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

    /// Returns the user-agent stylesheet for m3fx selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
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
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(CheckBox.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
