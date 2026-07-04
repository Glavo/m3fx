// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/// A styleable double property that preserves explicit API values against later stylesheet passes.
///
/// JavaFX styleable properties do not expose whether a value came from the public setter or from CSS. M3FX
/// component token properties need that distinction because controls install user-agent token defaults, while
/// applications may still configure one control through its Java property API. This property marks ordinary
/// `set` calls as explicit user values but keeps CSS-origin `applyStyle` calls styleable for later stylesheet
/// updates until the application sets the property directly.
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
        super(initialValue);
        this.bean = Objects.requireNonNull(bean, "bean");
        this.name = Objects.requireNonNull(name, "name");
        this.cssMetaData = Objects.requireNonNull(cssMetaData, "cssMetaData");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.invalidation = Objects.requireNonNull(invalidation, "invalidation");
    }

    /// Sets a value from application code and marks this property as explicitly configured.
    ///
    /// @param newValue the new token value
    @Override
    public void set(double newValue) {
        if (!applyingStyle) {
            userSet = true;
        }
        super.set(newValue);
    }

    /// Applies a CSS value without marking the property as explicitly configured by application code.
    ///
    /// @param origin the CSS origin
    /// @param value the value resolved from CSS
    @Override
    public void applyStyle(StyleOrigin origin, Number value) {
        applyingStyle = true;
        try {
            super.applyStyle(origin, value);
        } finally {
            applyingStyle = false;
        }
    }

    /// Returns whether application code has explicitly assigned this property.
    ///
    /// @return `true` when this property has a user-assigned value
    boolean isUserSet() {
        return userSet;
    }

    /// Validates the current value and runs the invalidation callback.
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
