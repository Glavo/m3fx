// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/// A styleable double property that preserves explicit API values against later stylesheet passes.
///
/// Ordinary `set` calls mark explicit application values. CSS-origin `applyStyle` calls remain eligible for later
/// stylesheet updates. A containing control may temporarily supply the value through [#setFromControl(double)],
/// suspending stylesheet updates without preventing application assignments or later container updates.
@NotNullByDefault
final class M3StyleableDoubleProperty extends StyleableDoubleProperty {
    /// The bean that owns this property.
    private final Object bean;

    /// The property name exposed to JavaFX.
    private final String name;

    /// The CSS metadata returned from this property.
    private final CssMetaData<? extends Styleable, Number> cssMetaData;

    /// Validates incoming token values.
    private final DoubleUnaryOperator validator;

    /// Runs after a valid value changes.
    private final Runnable invalidation;

    /// Whether the current assignment is being applied by JavaFX CSS.
    private boolean applyingStyle;

    /// Whether application code has explicitly assigned this property.
    private boolean userSet;

    /// Whether a containing control currently supplies this token instead of CSS.
    private boolean controlSet;

    /// Creates a styleable double property for one M3FX component token.
    ///
    /// @param initialValue the initial value before CSS is applied
    /// @param bean the owning bean
    /// @param name the property name
    /// @param cssMetaData the CSS metadata for this property
    /// @param validator the token validator
    /// @param invalidation the callback run after value changes
    M3StyleableDoubleProperty(
            double initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, Number> cssMetaData,
            DoubleUnaryOperator validator,
            Runnable invalidation
    ) {
        super(Objects.requireNonNull(validator, "validator").applyAsDouble(initialValue));
        this.bean = Objects.requireNonNull(bean, "bean");
        this.name = Objects.requireNonNull(name, "name");
        this.cssMetaData = Objects.requireNonNull(cssMetaData, "cssMetaData");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.invalidation = Objects.requireNonNull(invalidation, "invalidation");
    }

    /// Validates a value before assignment and marks an unbound property as explicitly configured.
    ///
    /// @param newValue the new token value
    @Override
    public void set(double newValue) {
        validator.applyAsDouble(newValue);
        if (!applyingStyle && !isBound()) {
            userSet = true;
        }
        super.set(newValue);
    }

    /// Validates a CSS value before applying it without taking application ownership.
    ///
    /// @param origin the CSS origin, or `null` to clear the origin
    /// @param value the value resolved from CSS
    @Override
    public void applyStyle(@Nullable StyleOrigin origin, @Nullable Number value) {
        // Validate the target before JavaFX can schedule a CSS transition toward it.
        if (value != null) {
            validator.applyAsDouble(value.doubleValue());
        }
        applyingStyle = true;
        try {
            super.applyStyle(origin, value);
        } finally {
            applyingStyle = false;
        }
    }

    /// Returns whether neither an application nor a containing control supplies the value.
    ///
    /// Binding eligibility is checked separately by [M3Css#isSettable(StyleableDoubleProperty)].
    boolean isCssSettable() {
        return !userSet && !controlSet;
    }

    /// Applies a containing control's token unless an application assignment or binding takes precedence.
    ///
    /// @param value the validated token value to install
    void setFromControl(double value) {
        validator.applyAsDouble(value);
        if (!userSet && !isBound()) {
            controlSet = true;
            // Bypass the application setter while retaining its priority over stylesheet defaults.
            super.set(value);
        }
    }

    /// Releases container sizing so subsequent CSS passes may resolve the token again.
    ///
    /// The current value is retained until restyled. Application assignments and bindings are preserved.
    void clearControlValue() {
        if (controlSet) {
            controlSet = false;
            if (!userSet && !isBound()) {
                applyStyle(null, get());
            }
        }
    }

    /// Validates values received from bindings or CSS interpolation before running the callback.
    @Override
    protected void invalidated() {
        validator.applyAsDouble(get());
        invalidation.run();
    }

    /// Returns the owning bean.
    @Override
    public Object getBean() {
        return bean;
    }

    /// Returns the property name.
    @Override
    public String getName() {
        return name;
    }

    /// Returns the CSS metadata for this property.
    @Override
    public CssMetaData<? extends Styleable, Number> getCssMetaData() {
        return cssMetaData;
    }
}
