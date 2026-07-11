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

/// Provides shared helpers for M3FX CSS-backed component tokens.
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

    /// Returns whether a styleable property can be set by CSS.
    public static boolean isSettable(StyleableDoubleProperty property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableDoubleProperty m3Property) || !m3Property.isUserSet());
    }

    /// Returns whether a styleable object property can be set by CSS.
    public static boolean isSettable(StyleableObjectProperty<?> property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableObjectProperty<?> m3Property) || !m3Property.isUserSet());
    }

    /// Writes a region minimum width when application code has not taken ownership of it.
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
    public static <T> StyleableObjectProperty<@Nullable T> styleableObjectProperty(
            @Nullable T initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, @Nullable T> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableObjectProperty<>(initialValue, bean, name, cssMetaData, invalidation);
    }

    /// Validates that a CSS size token is not negative.
    public static double nonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    /// Validates that a CSS numeric token is finite.
    public static double finite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }
}
