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
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

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
    /// @throws NullPointerException if `elementName` is `null`
    public static <E> ObservableList<E> nonNullElementList(String elementName) {
        return new NonNullObservableList<>(elementName, null, null);
    }

    /// Creates a mutable observable list that rejects `null` and repeated object identities.
    ///
    /// This factory is intended for ordered control slots whose elements become scene-graph children. Two equal but
    /// distinct objects may coexist; the same object reference may occur at most once. Bulk mutations validate the
    /// complete candidate result before changing the list.
    ///
    /// @param elementName the name used in validation exception messages
    /// @param <E> the list element type
    /// @return a mutable observable list containing no null or repeated object identities
    /// @throws NullPointerException if `elementName` is `null`
    public static <E> ObservableList<E> identityDistinctElementList(String elementName) {
        Objects.requireNonNull(elementName, "elementName");
        return new NonNullObservableList<>(
                elementName,
                (first, second) -> first == second,
                elementName + " must not occur more than once"
        );
    }

    /// Creates a mutable observable list that rejects `null` and pairwise-equivalent elements.
    ///
    /// Direct bulk mutations on the returned list validate their complete candidate result before changing the
    /// backing list. In particular, `addAll`, `setAll`, and `replaceAll` either complete in full or leave the list
    /// unchanged. The supplied predicate defines equivalence only for uniqueness enforcement; it does not replace
    /// [Object#equals(Object)] for lookup or removal operations.
    ///
    /// @param elementName the name used in validation exception messages
    /// @param equivalent  the predicate that identifies elements which must not coexist
    /// @param <E> the list element type
    /// @return a mutable observable list containing no null or pairwise-equivalent elements
    /// @throws NullPointerException if `elementName` or `equivalent` is `null`
    public static <E> ObservableList<E> distinctElementList(
            String elementName,
            BiPredicate<? super E, ? super E> equivalent
    ) {
        return new NonNullObservableList<>(
                elementName,
                Objects.requireNonNull(equivalent, "equivalent"),
                elementName + " must not be equivalent to another element"
        );
    }

    /// Mutable observable list implementation that validates elements before mutating the backing list.
    @NotNullByDefault
    private static final class NonNullObservableList<E> extends ModifiableObservableListBase<E> {
        /// The stored list elements.
        private final ArrayList<E> backingList = new ArrayList<>();

        /// The name used in null-check exception messages.
        private final String elementName;

        /// The optional predicate used to reject equivalent elements.
        private final @Nullable BiPredicate<? super E, ? super E> equivalent;

        /// The exception message used when the equivalence predicate rejects an element.
        private final @Nullable String duplicateMessage;

        /// Creates a non-null list.
        ///
        /// @param elementName the name used in null-check exception messages
        /// @param equivalent the uniqueness predicate, or `null` to allow duplicates
        /// @param duplicateMessage the uniqueness error message, or `null` when duplicates are allowed
        private NonNullObservableList(
                String elementName,
                @Nullable BiPredicate<? super E, ? super E> equivalent,
                @Nullable String duplicateMessage
        ) {
            this.elementName = Objects.requireNonNull(elementName, "elementName");
            this.equivalent = equivalent;
            this.duplicateMessage = duplicateMessage;
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
            return addValidated(size(), validatedAdditionCopy(elements));
        }

        /// Adds all elements at the requested index after validating the collection and its contents.
        @Override
        public boolean addAll(int index, Collection<? extends E> elements) {
            return addValidated(index, validatedAdditionCopy(elements));
        }

        /// Adds all elements after validating the array and its contents.
        @Override
        @SafeVarargs
        public final boolean addAll(E... elements) {
            return addValidated(size(), validatedAdditionCopy(elements));
        }

        /// Replaces the list contents after validating the collection and its contents.
        @Override
        public boolean setAll(Collection<? extends E> elements) {
            return replaceContents(validatedCopy(elements));
        }

        /// Replaces the list contents after validating the array and its contents.
        @Override
        @SafeVarargs
        public final boolean setAll(E... elements) {
            return replaceContents(validatedCopy(elements));
        }

        /// Applies a unary replacement after validating the complete replacement snapshot.
        ///
        /// @param operator the operator applied to every current element
        /// @throws NullPointerException if `operator` or a replacement element is `null`
        /// @throws IllegalArgumentException if two replacement elements are equivalent
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            Objects.requireNonNull(operator, "operator");
            ArrayList<E> replacements = new ArrayList<>(backingList.size());
            for (E element : backingList) {
                replacements.add(requireElement(operator.apply(element)));
            }
            requireDistinctElements(replacements);
            replaceContents(replacements);
        }

        /// Adds one already-validated element to the backing list.
        @Override
        protected void doAdd(int index, E element) {
            E validatedElement = requireElement(element);
            requireDistinctFromBacking(validatedElement, -1);
            backingList.add(index, validatedElement);
        }

        /// Replaces one element in the backing list.
        @Override
        protected E doSet(int index, E element) {
            E validatedElement = requireElement(element);
            requireDistinctFromBacking(validatedElement, index);
            return backingList.set(index, validatedElement);
        }

        /// Removes one element from the backing list.
        @Override
        protected E doRemove(int index) {
            return backingList.remove(index);
        }

        /// Inserts one fully validated snapshot and reports it as a single addition.
        private boolean addValidated(int index, List<E> additions) {
            Objects.checkFromToIndex(index, index, backingList.size());
            if (additions.isEmpty()) {
                return false;
            }

            beginChange();
            try {
                backingList.addAll(index, additions);
                nextAdd(index, index + additions.size());
            } finally {
                endChange();
            }
            return true;
        }

        /// Replaces the complete backing list with one fully validated snapshot.
        private boolean replaceContents(List<E> replacement) {
            boolean unchanged = backingList.size() == replacement.size();
            for (int index = 0; unchanged && index < backingList.size(); index++) {
                unchanged = backingList.get(index) == replacement.get(index);
            }
            if (unchanged) {
                return false;
            }

            ArrayList<E> removed = new ArrayList<>(backingList);
            beginChange();
            try {
                backingList.clear();
                backingList.addAll(replacement);
                if (removed.isEmpty()) {
                    nextAdd(0, replacement.size());
                } else if (replacement.isEmpty()) {
                    nextRemove(0, removed);
                } else {
                    nextReplace(0, replacement.size(), removed);
                }
            } finally {
                endChange();
            }
            return true;
        }

        /// Returns a validated snapshot of the supplied collection.
        private List<E> validatedCopy(Collection<? extends E> elements) {
            Objects.requireNonNull(elements, "elements");
            ArrayList<E> copy = new ArrayList<>(elements.size());
            for (E element : elements) {
                copy.add(requireElement(element));
            }
            requireDistinctElements(copy);
            return copy;
        }

        /// Returns a validated snapshot of the supplied array.
        private List<E> validatedCopy(E[] elements) {
            Objects.requireNonNull(elements, "elements");
            ArrayList<E> copy = new ArrayList<>(elements.length);
            for (E element : elements) {
                copy.add(requireElement(element));
            }
            requireDistinctElements(copy);
            return copy;
        }

        /// Returns an addition snapshot validated against itself and the current backing list.
        private List<E> validatedAdditionCopy(Collection<? extends E> elements) {
            List<E> copy = validatedCopy(elements);
            requireDistinctFromBacking(copy);
            return copy;
        }

        /// Returns an array addition snapshot validated against itself and the current backing list.
        private List<E> validatedAdditionCopy(E[] elements) {
            List<E> copy = validatedCopy(elements);
            requireDistinctFromBacking(copy);
            return copy;
        }

        /// Returns a non-null element or throws with the configured element name.
        private E requireElement(E element) {
            return Objects.requireNonNull(element, elementName);
        }

        /// Rejects equivalent pairs within a candidate list.
        private void requireDistinctElements(List<E> elements) {
            if (equivalent == null) {
                return;
            }
            for (int firstIndex = 0; firstIndex < elements.size(); firstIndex++) {
                E first = elements.get(firstIndex);
                for (int secondIndex = firstIndex + 1; secondIndex < elements.size(); secondIndex++) {
                    if (equivalent.test(first, elements.get(secondIndex))) {
                        throw duplicateElementException();
                    }
                }
            }
        }

        /// Rejects candidates equivalent to an element already stored in the backing list.
        private void requireDistinctFromBacking(List<E> elements) {
            if (equivalent == null) {
                return;
            }
            for (E element : elements) {
                requireDistinctFromBacking(element, -1);
            }
        }

        /// Rejects one candidate equivalent to a stored element other than an optional replaced index.
        private void requireDistinctFromBacking(E element, int excludedIndex) {
            if (equivalent == null) {
                return;
            }
            for (int index = 0; index < backingList.size(); index++) {
                if (index != excludedIndex && equivalent.test(element, backingList.get(index))) {
                    throw duplicateElementException();
                }
            }
        }

        /// Creates the exception used when a uniqueness predicate finds equivalent elements.
        private IllegalArgumentException duplicateElementException() {
            return new IllegalArgumentException(Objects.requireNonNull(duplicateMessage, "duplicateMessage"));
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
