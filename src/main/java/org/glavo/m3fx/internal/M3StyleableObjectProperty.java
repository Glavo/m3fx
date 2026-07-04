// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A styleable object property that preserves explicit API values against later stylesheet passes.
///
/// This mirrors [M3StyleableDoubleProperty] for component tokens whose CSS value type is an object, such as font
/// families and font weights. Ordinary `set` calls mark the token as an application value, while JavaFX CSS
/// `applyStyle` calls remain refreshable until application code explicitly sets the property.
@NotNullByDefault
final class M3StyleableObjectProperty<T> extends StyleableObjectProperty<@Nullable T> {
    /// The bean that owns this property.
    private final Object bean;

    /// The property name exposed to JavaFX.
    private final String name;

    /// The CSS metadata returned from this property.
    private final CssMetaData<? extends Styleable, @Nullable T> cssMetaData;

    /// Runs after a value changes.
    private final Runnable invalidation;

    /// Whether the current assignment is being applied by JavaFX CSS.
    private boolean applyingStyle;

    /// Whether application code has explicitly assigned this property.
    private boolean userSet;

    /// Creates a styleable object property for one M3FX component token.
    ///
    /// @param initialValue the initial value before CSS is applied
    /// @param bean the owning bean
    /// @param name the property name
    /// @param cssMetaData the CSS metadata for this property
    /// @param invalidation the callback run after value changes
    M3StyleableObjectProperty(
            @Nullable T initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, @Nullable T> cssMetaData,
            Runnable invalidation
    ) {
        super(initialValue);
        this.bean = Objects.requireNonNull(bean, "bean");
        this.name = Objects.requireNonNull(name, "name");
        this.cssMetaData = Objects.requireNonNull(cssMetaData, "cssMetaData");
        this.invalidation = Objects.requireNonNull(invalidation, "invalidation");
    }

    /// Sets a value from application code and marks this property as explicitly configured.
    ///
    /// @param newValue the new token value
    @Override
    public void set(@Nullable T newValue) {
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
    public void applyStyle(StyleOrigin origin, @Nullable T value) {
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

    /// Runs the invalidation callback.
    @Override
    protected void invalidated() {
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
    public CssMetaData<? extends Styleable, @Nullable T> getCssMetaData() {
        return cssMetaData;
    }
}
