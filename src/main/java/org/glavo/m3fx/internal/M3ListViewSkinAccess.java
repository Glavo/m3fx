// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3ListView;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/// Internal access point between [M3ListView] and its installed skin.
@NotNullByDefault
public final class M3ListViewSkinAccess {
    /// Opaque control property key for the currently installed skin accessor.
    private static final Object ACCESS_KEY = new Object();

    /// Prevents utility class instantiation.
    private M3ListViewSkinAccess() {
    }

    /// Registers a skin accessor for a list view.
    ///
    /// @param listView the owning list view
    /// @param access the accessor supplied by the installed skin
    public static void register(M3ListView<?> listView, Access access) {
        Objects.requireNonNull(listView, "listView")
                .getProperties()
                .put(ACCESS_KEY, Objects.requireNonNull(access, "access"));
    }

    /// Unregisters a skin accessor when it still belongs to the supplied list view.
    ///
    /// @param listView the owning list view
    /// @param access the accessor supplied by the disposed skin
    public static void unregister(M3ListView<?> listView, Access access) {
        Objects.requireNonNull(listView, "listView");
        Objects.requireNonNull(access, "access");
        if (listView.hasProperties() && listView.getProperties().get(ACCESS_KEY) == access) {
            listView.getProperties().remove(ACCESS_KEY);
        }
    }

    /// Requests the installed skin to refresh item count.
    ///
    /// @param listView the owning list view
    public static void refreshItemCount(M3ListView<?> listView) {
        @Nullable Access access = access(listView);
        if (access != null) {
            access.refreshItemCount();
        }
    }

    /// Requests the installed skin to refresh visible rows.
    ///
    /// @param listView the owning list view
    public static void refreshCells(M3ListView<?> listView) {
        @Nullable Access access = access(listView);
        if (access != null) {
            access.refreshCells();
        }
    }

    /// Requests the installed skin to rebuild visible rows after the cell factory changes.
    ///
    /// @param listView the owning list view
    public static void rebuildCells(M3ListView<?> listView) {
        @Nullable Access access = access(listView);
        if (access != null) {
            access.rebuildCells();
        }
    }

    /// Requests the installed skin to refresh focused-row state.
    ///
    /// @param listView the owning list view
    /// @param requestNodeFocus whether the materialized row should request keyboard focus
    /// @param animated whether scrolling the focused row should animate
    public static void refreshFocus(M3ListView<?> listView, boolean requestNodeFocus, boolean animated) {
        @Nullable Access access = access(listView);
        if (access != null) {
            access.refreshFocus(requestNodeFocus, animated);
        }
    }

    /// Requests the installed skin to scroll to a data index.
    ///
    /// @param listView the owning list view
    /// @param index the data item index to reveal
    /// @param animated whether the scroll should animate when animations are enabled
    public static void scrollTo(M3ListView<?> listView, int index, boolean animated) {
        @Nullable Access access = access(listView);
        if (access != null) {
            access.scrollTo(index, animated);
        }
    }

    /// Returns the visible or reusable row node for an item index.
    ///
    /// @param listView the owning list view
    /// @param index the data item index to query
    /// @return the rendered row node, or `null` when no installed skin can provide one
    public static @Nullable Node visibleItem(M3ListView<?> listView, int index) {
        @Nullable Access access = access(listView);
        return access == null ? null : access.visibleItem(index);
    }

    /// Returns the attached row node for an item index.
    ///
    /// @param listView the owning list view
    /// @param index the data item index to query
    /// @return the attached row node, or `null` when the row is not materialized
    public static @Nullable Node attachedVisibleItem(M3ListView<?> listView, int index) {
        @Nullable Access access = access(listView);
        return access == null ? null : access.attachedVisibleItem(index);
    }

    /// Returns the data index of the attached row containing a node.
    ///
    /// @param listView the owning list view
    /// @param node the node to find inside attached rows
    /// @return the containing row index, or `-1` when none is attached
    public static int attachedVisibleItemIndex(M3ListView<?> listView, Node node) {
        @Nullable Access access = access(listView);
        return access == null ? -1 : access.attachedVisibleItemIndex(Objects.requireNonNull(node, "node"));
    }

    /// Returns the first attached row accepted by a predicate.
    ///
    /// @param listView the owning list view
    /// @param predicate the predicate used to select a row node
    /// @return the matching attached row node, or `null` when none matches
    public static @Nullable Node findAttachedVisibleItem(
            M3ListView<?> listView,
            Predicate<? super Node> predicate
    ) {
        @Nullable Access access = access(listView);
        return access == null ? null : access.findAttachedVisibleItem(Objects.requireNonNull(predicate, "predicate"));
    }

    /// Returns the registered skin accessor for a list view.
    private static @Nullable Access access(M3ListView<?> listView) {
        M3ListView<?> checkedListView = Objects.requireNonNull(listView, "listView");
        if (!checkedListView.hasProperties()) {
            return null;
        }
        Object value = checkedListView.getProperties().get(ACCESS_KEY);
        return value instanceof Access access ? access : null;
    }

    /// Internal operations supplied by an installed list view skin.
    @NotNullByDefault
    public interface Access {
        /// Refreshes the virtualized item count.
        void refreshItemCount();

        /// Refreshes attached row state.
        void refreshCells();

        /// Rebuilds attached rows after the cell factory changes.
        void rebuildCells();

        /// Refreshes focused-row state.
        ///
        /// @param requestNodeFocus whether the materialized row should request keyboard focus
        /// @param animated whether scrolling the focused row should animate
        void refreshFocus(boolean requestNodeFocus, boolean animated);

        /// Scrolls to a data item index.
        ///
        /// @param index the data item index to reveal
        /// @param animated whether the scroll should animate when animations are enabled
        void scrollTo(int index, boolean animated);

        /// Returns the visible or reusable row node for an item index.
        ///
        /// @param index the data item index to query
        /// @return the rendered row node, or `null` when the index is outside the data list
        @Nullable Node visibleItem(int index);

        /// Returns the currently attached row node for an item index.
        ///
        /// @param index the data item index to query
        /// @return the attached row node, or `null` when the row is not materialized
        @Nullable Node attachedVisibleItem(int index);

        /// Returns the data index of the attached row containing a node.
        ///
        /// @param node the node to find inside attached rows
        /// @return the containing row index, or `-1` when none is attached
        int attachedVisibleItemIndex(Node node);

        /// Returns the first attached row accepted by a predicate.
        ///
        /// @param predicate the predicate used to select a row node
        /// @return the matching attached row node, or `null` when none matches
        @Nullable Node findAttachedVisibleItem(Predicate<? super Node> predicate);
    }
}
