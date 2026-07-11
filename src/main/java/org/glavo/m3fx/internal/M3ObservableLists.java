// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

        /// Sorts the backing list atomically and reports one permutation change.
        ///
        /// @param comparator the comparator to use, or `null` for natural ordering
        @Override
        public void sort(@Nullable Comparator<? super E> comparator) {
            int size = backingList.size();
            if (size < 2) {
                return;
            }

            boolean alreadySorted = true;
            for (int index = 1; index < size; index++) {
                if (compare(backingList.get(index - 1), backingList.get(index), comparator) > 0) {
                    alreadySorted = false;
                    break;
                }
            }
            if (alreadySorted) {
                return;
            }

            ArrayList<E> previousOrder = new ArrayList<>(backingList);
            int[] sortedOldIndices = stableSortedIndices(previousOrder, comparator);
            int[] permutation = new int[size];
            boolean changed = false;
            for (int newIndex = 0; newIndex < size; newIndex++) {
                int oldIndex = sortedOldIndices[newIndex];
                permutation[oldIndex] = newIndex;
                changed |= oldIndex != newIndex;
            }
            if (!changed) {
                return;
            }

            beginChange();
            try {
                for (int newIndex = 0; newIndex < size; newIndex++) {
                    backingList.set(newIndex, previousOrder.get(sortedOldIndices[newIndex]));
                }
                nextPermutation(0, size, permutation);
            } finally {
                endChange();
            }
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

        /// Returns original element indices in stable sorted order.
        ///
        /// The primitive merge-sort buffers avoid allocating one wrapper object per element while retaining the
        /// stable ordering required by [List#sort(Comparator)].
        ///
        /// @param elements the immutable pre-sort element snapshot
        /// @param comparator the comparator to use, or `null` for natural ordering
        /// @param <E> the element type
        /// @return original indices arranged in their sorted order
        private static <E> int[] stableSortedIndices(
                List<E> elements,
                @Nullable Comparator<? super E> comparator
        ) {
            int size = elements.size();
            int[] source = new int[size];
            int[] target = new int[size];
            for (int index = 0; index < size; index++) {
                source[index] = index;
            }

            int width = 1;
            while (width < size) {
                int runLength = width > size - width ? size : width * 2;
                for (int left = 0; left < size; left += runLength) {
                    int middle = left > size - width ? size : left + width;
                    int right = left > size - runLength ? size : left + runLength;
                    mergeSortedIndices(elements, comparator, source, target, left, middle, right);
                }
                int[] swap = source;
                source = target;
                target = swap;
                width = width > size / 2 ? size : width * 2;
            }
            return source;
        }

        /// Merges two adjacent stable index runs into a target buffer.
        ///
        /// @param elements the element snapshot used for comparisons
        /// @param comparator the comparator to use, or `null` for natural ordering
        /// @param source the source index runs
        /// @param target the destination index buffer
        /// @param left the first index in the left run
        /// @param middle the first index in the right run
        /// @param right the exclusive end of the right run
        /// @param <E> the element type
        private static <E> void mergeSortedIndices(
                List<E> elements,
                @Nullable Comparator<? super E> comparator,
                int[] source,
                int[] target,
                int left,
                int middle,
                int right
        ) {
            int leftIndex = left;
            int rightIndex = middle;
            for (int outputIndex = left; outputIndex < right; outputIndex++) {
                if (leftIndex < middle
                        && (rightIndex >= right
                        || compare(elements.get(source[leftIndex]), elements.get(source[rightIndex]), comparator) <= 0)) {
                    target[outputIndex] = source[leftIndex++];
                } else {
                    target[outputIndex] = source[rightIndex++];
                }
            }
        }

        /// Compares two non-null list elements with an explicit or natural-order comparator.
        ///
        /// @param first the first element
        /// @param second the second element
        /// @param comparator the comparator to use, or `null` for natural ordering
        /// @param <E> the element type
        /// @return a negative, zero, or positive comparison result
        @SuppressWarnings("unchecked")
        private static <E> int compare(
                E first,
                E second,
                @Nullable Comparator<? super E> comparator
        ) {
            if (comparator != null) {
                return comparator.compare(first, second);
            }
            return ((Comparable<? super E>) first).compareTo(second);
        }
    }
}
