// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/// Provides shared helpers for CSS-backed M3FX component tokens and control padding.
@NotNullByDefault
public final class M3Css {
    /// Stores padding ownership and any update deferred by an application binding.
    private static final IdentityKey PADDING_KEY = new IdentityKey(M3Css.class.getName() + ".padding");

    /// Records the last helper value or an explicit release of padding ownership.
    ///
    /// @param value the last helper value, or `null` when ownership was explicitly released
    /// @param suspended whether a helper update was skipped while the padding was bound
    @NotNullByDefault
    private record PaddingState(@Nullable Insets value, boolean suspended) {
    }

    /// Prevents utility class instantiation.
    private M3Css() {
    }

    /// Formats a finite logical-pixel value for use in JavaFX CSS.
    ///
    /// Integral values representable as `long` omit the decimal point. Other values use [Double#toString(double)]
    /// so the result is locale-independent and preserves the supplied value without formatter allocation or
    /// precision loss.
    ///
    /// @param value the finite value to format
    /// @return the CSS pixel value
    /// @throws IllegalArgumentException if `value` is infinite or NaN
    public static String pixels(double value) {
        finite(value, "value");
        return Math.rint(value) == value && value >= -0x1.0p63 && value < 0x1.0p63
                ? (long) value + "px"
                : value + "px";
    }

    /// Returns whether a styleable numeric property can be set by CSS.
    ///
    /// A property is settable when it is unbound and has not received an explicit application value through an
    /// M3FX component-token property or a value supplied by a containing control.
    ///
    /// @param property the property to inspect
    /// @return `true` when CSS may assign the property
    /// @throws NullPointerException if `property` is `null`
    public static boolean isSettable(StyleableDoubleProperty property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableDoubleProperty m3Property)
                || m3Property.isCssSettable());
    }

    /// Creates non-inheriting CSS metadata for a numeric size property.
    ///
    /// CSS sizes are decoded with [SizeConverter]. The accessor supplies the property for both writability checks
    /// and CSS assignments; it must return the same non-null property for each supplied styleable instance.
    /// Writability follows [#isSettable(StyleableDoubleProperty)], including M3FX token ownership. Value validation
    /// remains the responsibility of the supplied property.
    ///
    /// @param property the CSS property name
    /// @param initialValue the initial numeric CSS value
    /// @param accessor the function that returns the size property for a styleable instance
    /// @param <S> the styleable type
    /// @return a new metadata descriptor
    /// @throws NullPointerException if `property` or `accessor` is `null`
    public static <S extends Styleable> CssMetaData<S, Number> sizeMetaData(
            String property,
            double initialValue,
            Function<? super S, ? extends StyleableDoubleProperty> accessor
    ) {
        Objects.requireNonNull(property, "property");
        Objects.requireNonNull(accessor, "accessor");
        return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
            /// Returns whether CSS may assign the accessed size property.
            @Override
            public boolean isSettable(S styleable) {
                return M3Css.isSettable(accessor.apply(styleable));
            }

            /// Returns the size property for the supplied styleable instance.
            @Override
            public StyleableProperty<Number> getStyleableProperty(S styleable) {
                return accessor.apply(styleable);
            }
        };
    }

    /// Applies a containing control's value to an M3FX numeric token.
    ///
    /// Application assignments and bindings take precedence. Otherwise, the value replaces stylesheet sizing
    /// and remains eligible for later container updates. Use [#clearControlToken(StyleableDoubleProperty)] when
    /// the containing control stops managing the token.
    ///
    /// @param property a token created by this class's numeric property factories
    /// @param value the token value, subject to the property's validation
    /// @throws ClassCastException if `property` was not created by an M3FX numeric property factory
    /// @throws NullPointerException if `property` is `null`
    /// @throws IllegalArgumentException if `value` violates the token's constraints
    public static void setControlToken(StyleableDoubleProperty property, double value) {
        ((M3StyleableDoubleProperty) property).setFromControl(value);
    }

    /// Releases an M3FX numeric token from its containing control.
    ///
    /// The current value remains until CSS resolves it again. Application assignments and bindings are preserved.
    /// Repeated calls have no effect after ownership has been released.
    ///
    /// @param property a token created by this class's numeric property factories
    /// @throws ClassCastException if `property` was not created by an M3FX numeric property factory
    /// @throws NullPointerException if `property` is `null`
    public static void clearControlToken(StyleableDoubleProperty property) {
        ((M3StyleableDoubleProperty) property).clearControlValue();
    }

    /// Returns whether a styleable object property can be set by CSS.
    ///
    /// @param property the property to inspect
    /// @return `true` when CSS may assign the property
    /// @throws NullPointerException if `property` is `null`
    public static boolean isSettable(StyleableObjectProperty<?> property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableObjectProperty<?> m3Property) || !m3Property.isUserSet());
    }

    /// Updates padding while its value still matches the last helper assignment.
    ///
    /// Untracked empty padding is initially eligible. Equal application assignments cannot be distinguished
    /// from helper values. A bound property is not written; if it was helper-owned, a skipped update allows
    /// the next call after unbinding to resume ownership. Explicitly released ownership remains released.
    ///
    /// @param region the region to update
    /// @param padding the padding to install
    /// @throws NullPointerException if `region` is `null`
    public static void setPaddingIfUnbound(Region region, Insets padding) {
        @Nullable PaddingState state = region.hasProperties()
                ? (PaddingState) region.getProperties().get(PADDING_KEY) : null;
        Insets current = region.getPadding();
        if (region.paddingProperty().isBound()) {
            if (state == null ? Insets.EMPTY.equals(current) : state.value() != null) {
                region.getProperties().put(PADDING_KEY,
                        new PaddingState(state == null ? current : state.value(), true));
            }
            return;
        }
        if (state == null ? Insets.EMPTY.equals(current)
                : state.value() != null && (state.suspended() || state.value().equals(current))) {
            setPaddingAsHelperOwned(region, padding);
        }
    }

    /// Restores unbound padding and explicitly releases helper ownership.
    ///
    /// Subsequent calls to [#setPaddingIfUnbound(Region, Insets)] preserve the restored value, including empty
    /// padding. Pending updates from a previous binding are discarded. A bound property is left unchanged.
    ///
    /// @param region the region to update
    /// @param padding the padding to restore
    /// @throws NullPointerException if `region` is `null`
    public static void setPaddingWithoutOwnershipIfUnbound(Region region, Insets padding) {
        if (!region.paddingProperty().isBound()) {
            region.setPadding(padding);
            region.getProperties().put(PADDING_KEY, new PaddingState(null, false));
        }
    }

    /// Replaces any unbound padding and resumes helper ownership.
    ///
    /// Subsequent calls to [#setPaddingIfUnbound(Region, Insets)] may update the installed value. A bound
    /// property is left unchanged. The assignment takes precedence over user-agent padding declarations.
    ///
    /// @param region the region to update
    /// @param padding the padding to install
    /// @throws NullPointerException if `region` is `null`
    public static void setPaddingAsHelperOwned(Region region, Insets padding) {
        if (!region.paddingProperty().isBound()) {
            region.setPadding(padding);
            region.getProperties().put(PADDING_KEY, new PaddingState(padding, false));
        }
    }

    /// Creates a styleable non-negative component token property.
    ///
    /// Accepted application assignments prevent later CSS passes from replacing the value. Initial values and
    /// direct Java or CSS assignments must be finite and non-negative; invalid values are rejected before changing
    /// the property or its CSS eligibility. Binding sources must supply valid values: source changes cannot be
    /// rolled back by this property. Values are also checked before `invalidation` runs.
    ///
    /// @param initialValue the initial token value before CSS is applied
    /// @param bean         the object that owns the property
    /// @param name         the property name
    /// @param cssMetaData  the CSS metadata exposed by the property
    /// @param invalidation the callback invoked after a valid value changes
    /// @return the newly created styleable property
    /// @throws NullPointerException if `bean`, `name`, `cssMetaData`, or `invalidation` is `null`
    /// @throws IllegalArgumentException if `initialValue` is negative, infinite, or NaN
    public static StyleableDoubleProperty nonNegativeStyleableDoubleProperty(
            double initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, Number> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableDoubleProperty(
                initialValue,
                bean,
                name,
                cssMetaData,
                value -> nonNegative(value, name),
                invalidation
        );
    }

    /// Creates a styleable finite component token property.
    ///
    /// Accepted application assignments prevent later CSS passes from replacing the value. Initial values and
    /// direct Java or CSS assignments must be finite; invalid values are rejected before changing the property or
    /// its CSS eligibility. Binding sources must supply valid values: source changes cannot be rolled back by this
    /// property. Values are also checked before `invalidation` runs.
    ///
    /// @param initialValue the initial token value before CSS is applied
    /// @param bean         the object that owns the property
    /// @param name         the property name
    /// @param cssMetaData  the CSS metadata exposed by the property
    /// @param invalidation the callback invoked after a valid value changes
    /// @return the newly created styleable property
    /// @throws NullPointerException if `bean`, `name`, `cssMetaData`, or `invalidation` is `null`
    /// @throws IllegalArgumentException if `initialValue` is infinite or NaN
    public static StyleableDoubleProperty finiteStyleableDoubleProperty(
            double initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, Number> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableDoubleProperty(
                initialValue,
                bean,
                name,
                cssMetaData,
                value -> finite(value, name),
                invalidation
        );
    }

    /// Creates a styleable object component token property.
    ///
    /// Explicit application assignments prevent later CSS passes from replacing the value. The initial and later
    /// values may be `null`; interpretation belongs to the owning component.
    ///
    /// @param <T>          the property value type
    /// @param initialValue the initial token value before CSS is applied, or `null`
    /// @param bean         the object that owns the property
    /// @param name         the property name
    /// @param cssMetaData  the CSS metadata exposed by the property
    /// @param invalidation the callback invoked after the value changes
    /// @return the newly created styleable property
    /// @throws NullPointerException if `bean`, `name`, `cssMetaData`, or `invalidation` is `null`
    public static <T> StyleableObjectProperty<@Nullable T> styleableObjectProperty(
            @Nullable T initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, @Nullable T> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableObjectProperty<>(initialValue, bean, name, cssMetaData, invalidation);
    }

    /// Validates that a CSS size token is finite and not negative.
    ///
    /// @param value the value to validate
    /// @param name  the token name used in an exception message
    /// @return `value` unchanged
    /// @throws IllegalArgumentException if `value` is negative, infinite, or NaN
    public static double nonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and not negative");
        }
        return value;
    }

    /// Validates that a CSS numeric token is finite.
    ///
    /// @param value the value to validate
    /// @param name  the token name used in an exception message
    /// @return `value` unchanged
    /// @throws IllegalArgumentException if `value` is infinite or NaN
    public static double finite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }
}
