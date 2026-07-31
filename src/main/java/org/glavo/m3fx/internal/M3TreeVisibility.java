// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

/// Provides best-effort access to JavaFX effective tree visibility.
///
/// JavaFX maintains a non-public `Node.treeVisibleProperty()` that already tracks the visible state of a node and its
/// ancestors. This utility uses that property when the JavaFX module is open to M3FX. When access is unavailable, it
/// silently approximates the state through public node and parent properties. The fallback covers ordinary [Parent]
/// ancestry but cannot cross a [javafx.scene.SubScene] root because JavaFX does not expose that relationship through
/// its public [Node] API.
@NotNullByDefault
final class M3TreeVisibility {
    /// Adapted getter for the non-public JavaFX tree-visible property, or `null` when access is unavailable.
    private static final @Nullable MethodHandle TREE_VISIBLE_PROPERTY_GETTER = findTreeVisiblePropertyGetter();

    /// Prevents utility class instantiation.
    private M3TreeVisibility() {
    }

    /// Returns the native effective-visibility property when JavaFX permits access.
    ///
    /// @param node the node whose property should be resolved
    /// @return the native property, or `null` when the runtime does not expose it to M3FX
    /// @throws NullPointerException if `node` is `null`
    static @Nullable ObservableBooleanValue treeVisibleProperty(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable MethodHandle getter = TREE_VISIBLE_PROPERTY_GETTER;
        if (getter == null) {
            return null;
        }

        try {
            return (ObservableBooleanValue) getter.invokeExact(node);
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            return null;
        }
    }

    /// Returns the best available indication of whether the node and its tree ancestors are visible.
    ///
    /// @param node the node whose visible ancestor chain should be inspected, or `null`
    /// @return `true` when every ancestor visible to the selected native or fallback implementation is visible
    static boolean isTreeVisible(@Nullable Node node) {
        if (node == null) {
            return false;
        }

        @Nullable ObservableBooleanValue property = treeVisibleProperty(node);
        if (property != null) {
            return property.get();
        }

        @Nullable Node current = node;
        while (current != null) {
            if (!current.isVisible()) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    /// Resolves and adapts the non-public JavaFX tree-visible property getter without requiring module access.
    ///
    /// @return the adapted getter, or `null` when lookup or signature validation fails
    private static @Nullable MethodHandle findTreeVisiblePropertyGetter() {
        try {
            Method method = Node.class.getDeclaredMethod("treeVisibleProperty");
            if (!ObservableBooleanValue.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }

            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Node.class, MethodHandles.lookup());
            return lookup.unreflect(method).asType(
                    MethodType.methodType(ObservableBooleanValue.class, Node.class)
            );
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            return null;
        }
    }
}
