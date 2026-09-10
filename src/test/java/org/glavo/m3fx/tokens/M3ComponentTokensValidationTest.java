// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies finite numeric inputs throughout the component token model.
@NotNullByDefault
final class M3ComponentTokensValidationTest {
    /// Rejects NaN and both infinities in every numeric field while retaining valid defaults.
    @Test
    void rejectsNonFiniteRecordComponents() throws ReflectiveOperationException {
        M3ComponentTokens defaults = M3ComponentTokens.builder(
                M3Profile.BASELINE_2021, M3ShapeTokens.baseline(), M3Density.standard()
        ).build();
        Map<Class<?>, Object> samples = new LinkedHashMap<>();
        collectRecords(defaults, samples);
        samples.remove(defaults.getClass());
        assertEquals(Arrays.stream(M3ComponentTokens.class.getDeclaredClasses())
                .filter(Class::isRecord).collect(Collectors.toSet()), samples.keySet());

        for (var entry : samples.entrySet()) {
            Class<?> type = entry.getKey();
            RecordComponent @Unmodifiable [] components = type.getRecordComponents();
            Class<?> @Unmodifiable [] parameterTypes = Arrays.stream(components)
                    .map(RecordComponent::getType).toArray(Class<?>[]::new);
            var constructor = type.getConstructor(parameterTypes);
            List<Object> arguments = new ArrayList<>();
            for (RecordComponent component : components) {
                arguments.add(Objects.requireNonNull(component.getAccessor().invoke(entry.getValue())));
            }
            assertEquals(entry.getValue(), constructor.newInstance(arguments.toArray()));

            for (int index = 0; index < components.length; index++) {
                if (components[index].getType() != double.class) {
                    continue;
                }
                for (double invalid : List.of(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
                    Object[] invalidArguments = arguments.toArray();
                    invalidArguments[index] = invalid;
                    String description = type.getSimpleName() + "." + components[index].getName() + "=" + invalid;
                    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                            () -> constructor.newInstance(invalidArguments), description);
                    assertInstanceOf(IllegalArgumentException.class, exception.getCause(), description);
                }
            }
        }
    }

    /// Collects one valid example of each record type reachable from the generated token graph.
    private static void collectRecords(Object value, Map<Class<?>, Object> samples)
            throws ReflectiveOperationException {
        Class<?> type = value.getClass();
        if (!type.isRecord() || samples.putIfAbsent(type, value) != null) {
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            collectRecords(Objects.requireNonNull(component.getAccessor().invoke(value)), samples);
        }
    }
}
