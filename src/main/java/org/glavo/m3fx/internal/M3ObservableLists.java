// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/// Internal factories for observable lists with M3FX control invariants.
@NotNullByDefault
public final class M3ObservableLists {
    /// Prevents instantiation.
    private M3ObservableLists() {
    }

    /// Creates a mutable observable list that rejects `null` elements on every mutation path.
    ///
    /// @param elementName the name used in null-check exception messages
    /// @param <E> the list element type
    /// @return a mutable observable list that rejects `null` elements
    public static <E> ObservableList<E> nonNullElementList(String elementName) {
        return new NonNullObservableList<>(elementName);
    }

    /// Mutable observable list implementation that validates elements before mutating the backing list.
    @NotNullByDefault
    private static final class NonNullObservableList<E> extends ModifiableObservableListBase<E> {
        /// The stored list elements.
        private final ArrayList<E> backingList = new ArrayList<>();

        /// The name used in null-check exception messages.
        private final String elementName;

        /// Creates a non-null list.
        ///
        /// @param elementName the name used in null-check exception messages
        private NonNullObservableList(String elementName) {
            this.elementName = Objects.requireNonNull(elementName, "elementName");
        }

        /// Returns the element at the requested index.
        @Override
        public E get(int index) {
            return backingList.get(index);
        }

        /// Returns the number of stored elements.
        @Override
        public int size() {
            return backingList.size();
        }

        /// Adds all elements after validating the collection and its contents.
        @Override
        public boolean addAll(Collection<? extends E> elements) {
            return super.addAll(validatedCopy(elements));
        }

        /// Adds all elements at the requested index after validating the collection and its contents.
        @Override
        public boolean addAll(int index, Collection<? extends E> elements) {
            return super.addAll(index, validatedCopy(elements));
        }

        /// Adds all elements after validating the array and its contents.
        @Override
        @SafeVarargs
        public final boolean addAll(E... elements) {
            return super.addAll(validatedCopy(elements));
        }

        /// Replaces the list contents after validating the collection and its contents.
        @Override
        public boolean setAll(Collection<? extends E> elements) {
            return super.setAll(validatedCopy(elements));
        }

        /// Replaces the list contents after validating the array and its contents.
        @Override
        @SafeVarargs
        public final boolean setAll(E... elements) {
            return super.setAll(validatedCopy(elements));
        }

        /// Adds one already-validated element to the backing list.
        @Override
        protected void doAdd(int index, E element) {
            backingList.add(index, requireElement(element));
        }

        /// Replaces one element in the backing list.
        @Override
        protected E doSet(int index, E element) {
            return backingList.set(index, requireElement(element));
        }

        /// Removes one element from the backing list.
        @Override
        protected E doRemove(int index) {
            return backingList.remove(index);
        }

        /// Returns a validated snapshot of the supplied collection.
        private List<E> validatedCopy(Collection<? extends E> elements) {
            Objects.requireNonNull(elements, "elements");
            ArrayList<E> copy = new ArrayList<>(elements.size());
            for (E element : elements) {
                copy.add(requireElement(element));
            }
            return copy;
        }

        /// Returns a validated snapshot of the supplied array.
        private List<E> validatedCopy(E[] elements) {
            Objects.requireNonNull(elements, "elements");
            ArrayList<E> copy = new ArrayList<>(elements.length);
            for (E element : elements) {
                copy.add(requireElement(element));
            }
            return copy;
        }

        /// Returns a non-null element or throws with the configured element name.
        private E requireElement(E element) {
            return Objects.requireNonNull(element, elementName);
        }
    }
}
