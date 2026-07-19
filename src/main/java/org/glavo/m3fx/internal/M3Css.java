// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/// Provides shared helpers for CSS-backed M3FX component tokens and helper-owned region metrics.
///
/// Metric writers remember values installed by M3FX in the target region's properties map. A later token refresh
/// may replace that value only while the application has not changed or bound the corresponding JavaFX property.
/// This preserves explicit application sizing while still allowing theme and density changes to update defaults.
@NotNullByDefault
public final class M3Css {
    /// Tracks the last minimum width written by M3FX metric helpers.
    private static final Object MIN_WIDTH_KEY = new Object();

    /// Tracks the last preferred width written by M3FX metric helpers.
    private static final Object PREF_WIDTH_KEY = new Object();

    /// Tracks the last maximum width written by M3FX metric helpers.
    private static final Object MAX_WIDTH_KEY = new Object();

    /// Tracks the last minimum height written by M3FX metric helpers.
    private static final Object MIN_HEIGHT_KEY = new Object();

    /// Tracks the last preferred height written by M3FX metric helpers.
    private static final Object PREF_HEIGHT_KEY = new Object();

    /// Tracks the last maximum height written by M3FX metric helpers.
    private static final Object MAX_HEIGHT_KEY = new Object();

    /// Tracks the last padding written by M3FX metric helpers.
    private static final Object PADDING_KEY = new Object();

    /// Tracks helper-owned metrics that were skipped while application bindings were active.
    private static final Object SUSPENDED_METRICS_KEY = new Object();

    /// Prevents utility class instantiation.
    private M3Css() {
    }

    /// Returns whether a styleable numeric property can be set by CSS.
    ///
    /// A property is settable when it is unbound and has not received an explicit application value through an
    /// M3FX component-token property.
    ///
    /// @param property the property to inspect
    /// @return `true` when CSS may assign the property
    /// @throws NullPointerException if `property` is `null`
    public static boolean isSettable(StyleableDoubleProperty property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableDoubleProperty m3Property) || !m3Property.isUserSet());
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

    /// Writes a region minimum width when application code has not taken ownership of it.
    ///
    /// @param region the region to update
    /// @param width  the minimum width value to write, in pixels
    /// @throws NullPointerException if `region` is `null`
    public static void setMinWidthIfUnbound(Region region, double width) {
        if (shouldWriteMetric(
                region,
                MIN_WIDTH_KEY,
                region.minWidthProperty().isBound(),
                region.getMinWidth(),
                Region.USE_COMPUTED_SIZE
        )) {
            region.setMinWidth(width);
            rememberMetric(region, MIN_WIDTH_KEY, width);
        }
    }

    /// Writes a region preferred width when application code has not taken ownership of it.
    ///
    /// @param region the region to update
    /// @param width  the preferred width value to write, in pixels
    /// @throws NullPointerException if `region` is `null`
    public static void setPrefWidthIfUnbound(Region region, double width) {
        if (shouldWriteMetric(
                region,
                PREF_WIDTH_KEY,
                region.prefWidthProperty().isBound(),
                region.getPrefWidth(),
                Region.USE_COMPUTED_SIZE
        )) {
            region.setPrefWidth(width);
            rememberMetric(region, PREF_WIDTH_KEY, width);
        }
    }

    /// Writes a region maximum width when application code has not taken ownership of it.
    ///
    /// @param region the region to update
    /// @param width  the maximum width value to write, in pixels
    /// @throws NullPointerException if `region` is `null`
    public static void setMaxWidthIfUnbound(Region region, double width) {
        if (shouldWriteMetric(
                region,
                MAX_WIDTH_KEY,
                region.maxWidthProperty().isBound(),
                region.getMaxWidth(),
                Region.USE_COMPUTED_SIZE
        )) {
            region.setMaxWidth(width);
            rememberMetric(region, MAX_WIDTH_KEY, width);
        }
    }

    /// Writes a region minimum height when application code has not taken ownership of it.
    ///
    /// @param region the region to update
    /// @param height the minimum height value to write, in pixels
    /// @throws NullPointerException if `region` is `null`
    public static void setMinHeightIfUnbound(Region region, double height) {
        if (shouldWriteMetric(
                region,
                MIN_HEIGHT_KEY,
                region.minHeightProperty().isBound(),
                region.getMinHeight(),
                Region.USE_COMPUTED_SIZE
        )) {
            region.setMinHeight(height);
            rememberMetric(region, MIN_HEIGHT_KEY, height);
        }
    }

    /// Writes a region preferred height when application code has not taken ownership of it.
    ///
    /// @param region the region to update
    /// @param height the preferred height value to write, in pixels
    /// @throws NullPointerException if `region` is `null`
    public static void setPrefHeightIfUnbound(Region region, double height) {
        if (shouldWriteMetric(
                region,
                PREF_HEIGHT_KEY,
                region.prefHeightProperty().isBound(),
                region.getPrefHeight(),
                Region.USE_COMPUTED_SIZE
        )) {
            region.setPrefHeight(height);
            rememberMetric(region, PREF_HEIGHT_KEY, height);
        }
    }

    /// Writes a region maximum height when application code has not taken ownership of it.
    ///
    /// @param region the region to update
    /// @param height the maximum height value to write, in pixels
    /// @throws NullPointerException if `region` is `null`
    public static void setMaxHeightIfUnbound(Region region, double height) {
        if (shouldWriteMetric(
                region,
                MAX_HEIGHT_KEY,
                region.maxHeightProperty().isBound(),
                region.getMaxHeight(),
                Region.USE_COMPUTED_SIZE
        )) {
            region.setMaxHeight(height);
            rememberMetric(region, MAX_HEIGHT_KEY, height);
        }
    }

    /// Writes region padding when application code has not taken ownership of it.
    ///
    /// @param region  the region to update
    /// @param padding the padding to install
    /// @throws NullPointerException if `region` is `null`
    public static void setPaddingIfUnbound(Region region, Insets padding) {
        if (shouldWriteMetric(
                region,
                PADDING_KEY,
                region.paddingProperty().isBound(),
                region.getPadding(),
                Insets.EMPTY
        )) {
            writePadding(region, padding);
            rememberMetric(region, PADDING_KEY, padding);
        }
    }

    /// Writes region padding without marking the metric as helper-owned.
    ///
    /// The value replaces any current unbound padding. Subsequent calls to [#setPaddingIfUnbound(Region, Insets)]
    /// treat it as application-owned and do not overwrite it.
    ///
    /// @param region  the region to update
    /// @param padding the padding to install
    /// @throws NullPointerException if `region` is `null`
    public static void setPaddingWithoutOwnershipIfUnbound(Region region, Insets padding) {
        if (region.paddingProperty().isBound()) {
            return;
        }

        writePadding(region, padding);
        if (region.hasProperties()) {
            region.getProperties().remove(PADDING_KEY);
        }
    }

    /// Writes region padding as an M3FX-owned metric.
    ///
    /// The value replaces any current unbound padding and becomes eligible for later replacement by
    /// [#setPaddingIfUnbound(Region, Insets)].
    ///
    /// @param region  the region to update
    /// @param padding the padding to install
    /// @throws NullPointerException if `region` is `null`
    public static void setPaddingAsHelperOwned(Region region, Insets padding) {
        if (region.paddingProperty().isBound()) {
            return;
        }

        writePadding(region, padding);
        rememberMetric(region, PADDING_KEY, padding);
    }

    /// Writes one Region padding value.
    private static void writePadding(Region region, Insets padding) {
        region.setPadding(padding);
    }

    /// Returns whether an M3FX helper still owns one numeric Region metric.
    private static boolean shouldWriteMetric(
            Region region,
            Object key,
            boolean bound,
            double currentValue,
            double defaultValue
    ) {
        @Nullable Object previousValue = region.hasProperties()
                ? region.getProperties().get(key)
                : null;
        if (bound) {
            rememberSuspendedMetricIfOwned(region, key,
                    previousValue instanceof Number || previousValue == null
                            && Double.compare(currentValue, defaultValue) == 0);
            return false;
        }
        if (consumeSuspendedMetric(region, key)) {
            return true;
        }

        if (previousValue instanceof Number previousNumber) {
            return Double.compare(previousNumber.doubleValue(), currentValue) == 0;
        }

        return previousValue == null && Double.compare(currentValue, defaultValue) == 0;
    }

    /// Returns whether an M3FX helper still owns one object-valued Region metric.
    private static boolean shouldWriteMetric(
            Region region,
            Object key,
            boolean bound,
            Object currentValue,
            Object defaultValue
    ) {
        @Nullable Object previousValue = region.hasProperties()
                ? region.getProperties().get(key)
                : null;
        if (bound) {
            rememberSuspendedMetricIfOwned(region, key,
                    previousValue != null || Objects.equals(currentValue, defaultValue));
            return false;
        }
        if (consumeSuspendedMetric(region, key)) {
            return true;
        }

        return previousValue == null
                ? Objects.equals(currentValue, defaultValue)
                : Objects.equals(previousValue, currentValue);
    }

    /// Records a skipped helper-owned metric while an application binding is active.
    @SuppressWarnings("unchecked")
    private static void rememberSuspendedMetricIfOwned(Region region, Object key, boolean owned) {
        if (!owned) {
            return;
        }
        Object value = region.getProperties().get(SUSPENDED_METRICS_KEY);
        Set<Object> suspendedMetrics;
        if (value instanceof Set<?> existing) {
            suspendedMetrics = (Set<Object>) existing;
        } else {
            suspendedMetrics = Collections.newSetFromMap(new IdentityHashMap<>());
            region.getProperties().put(SUSPENDED_METRICS_KEY, suspendedMetrics);
        }
        suspendedMetrics.add(key);
    }

    /// Returns and clears whether a skipped helper-owned metric should be restored after unbinding.
    private static boolean consumeSuspendedMetric(Region region, Object key) {
        if (!region.hasProperties()) {
            return false;
        }
        Object value = region.getProperties().get(SUSPENDED_METRICS_KEY);
        if (!(value instanceof Set<?> suspendedMetrics) || !suspendedMetrics.remove(key)) {
            return false;
        }
        if (suspendedMetrics.isEmpty()) {
            region.getProperties().remove(SUSPENDED_METRICS_KEY);
        }
        return true;
    }

    /// Records one numeric Region metric written by an M3FX helper.
    private static void rememberMetric(Region region, Object key, double value) {
        region.getProperties().put(key, value);
    }

    /// Records one object-valued Region metric written by an M3FX helper.
    private static void rememberMetric(Region region, Object key, Object value) {
        region.getProperties().put(key, value);
    }

    /// Creates a styleable non-negative component token property.
    ///
    /// Explicit application assignments prevent later CSS passes from replacing the value. Every changed value is
    /// required to be finite and non-negative before `invalidation` runs.
    ///
    /// @param initialValue the initial token value before CSS is applied
    /// @param bean         the object that owns the property
    /// @param name         the property name
    /// @param cssMetaData  the CSS metadata exposed by the property
    /// @param invalidation the callback invoked after a valid value changes
    /// @return the newly created styleable property
    /// @throws NullPointerException if `bean`, `name`, `cssMetaData`, or `invalidation` is `null`
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
    /// Explicit application assignments prevent later CSS passes from replacing the value. Every changed value is
    /// required to be finite before `invalidation` runs.
    ///
    /// @param initialValue the initial token value before CSS is applied
    /// @param bean         the object that owns the property
    /// @param name         the property name
    /// @param cssMetaData  the CSS metadata exposed by the property
    /// @param invalidation the callback invoked after a valid value changes
    /// @return the newly created styleable property
    /// @throws NullPointerException if `bean`, `name`, `cssMetaData`, or `invalidation` is `null`
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
