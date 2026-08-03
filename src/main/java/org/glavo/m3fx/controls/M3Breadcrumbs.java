// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BreadcrumbsSkin;
import org.jetbrains.annotations.NotNullByDefault;

/// Displays an ordered path through a navigable hierarchy.
///
/// Items are stored in root-to-current order, and the final item is automatically marked as
/// [current][M3BreadcrumbItem#currentProperty()]. When the path exceeds [maxVisibleItems][#maxVisibleItemsProperty()]
/// or the available width, earlier levels collapse into a keyboard-accessible overflow menu while the current
/// location remains visible. [keepRootVisible][#keepRootVisibleProperty()] preserves the first item whenever the
/// root, overflow control, and current item can fit together.
///
/// `M3Breadcrumbs` is a passive navigation container: actions are delivered by its [M3BreadcrumbItem] children.
/// It is not focus traversable, while visible items and the overflow menu participate in ordinary keyboard focus
/// traversal. The [compact][#compactProperty()] presentation reduces vertical space without changing hierarchy or
/// interaction. Item labels remain single-line and may be truncated with an ellipsis.
///
/// See [Adobe Spectrum breadcrumbs](https://opensource.adobe.com/spectrum-web-components/components/breadcrumbs/).
@NotNullByDefault
public final class M3Breadcrumbs extends Control {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-breadcrumbs";

    /// The pseudo-class applied in compact presentation.
    private static final PseudoClass COMPACT_PSEUDO_CLASS = PseudoClass.getPseudoClass("compact");

    /// The default maximum number of visible entries, including an overflow control when present.
    private static final int DEFAULT_MAX_VISIBLE_ITEMS = 4;

    /// The lowest supported visible-entry limit.
    private static final int MINIMUM_MAX_VISIBLE_ITEMS = 2;

    /// The live, mutable hierarchy in root-to-current order.
    ///
    /// The list rejects `null` and identity duplicates. Each item must also satisfy the JavaFX single-parent rule
    /// while displayed.
    private final ObservableList<M3BreadcrumbItem> items =
            M3ObservableLists.identityDistinctElementList("breadcrumbItem");

    /// Keeps the current-location state synchronized with the final list item.
    private final ListChangeListener<M3BreadcrumbItem> itemsListener = change -> {
        while (change.next()) {
            for (M3BreadcrumbItem removedItem : change.getRemoved()) {
                removedItem.setCurrent(false);
            }
        }
        updateCurrentItem();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
    };

    /// Creates empty breadcrumbs with the default presentation and overflow policy.
    public M3Breadcrumbs() {
        initialize();
    }

    /// Creates breadcrumbs containing the specified hierarchy.
    ///
    /// @param items the initial non-null, identity-distinct hierarchy items
    /// @throws NullPointerException if `items` or an element is `null`
    /// @throws IllegalArgumentException if the same item occurs more than once
    public M3Breadcrumbs(M3BreadcrumbItem... items) {
        this();
        getItems().addAll(items);
    }

    /// Whether compact vertical metrics are used.
    ///
    /// @defaultValue `false`
    private final BooleanProperty compact = new SimpleBooleanProperty(this, "compact") {
        /// Updates compact styling and layout after assignment.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(COMPACT_PSEUDO_CLASS, get());
            requestLayout();
        }
    };

    /// Returns whether compact vertical metrics are used.
    ///
    /// @return `true` for compact presentation
    public boolean isCompact() {
        return compact.get();
    }

    /// Sets whether compact vertical metrics are used.
    ///
    /// @param compact whether to use compact presentation
    public void setCompact(boolean compact) {
        this.compact.set(compact);
    }

    /// Returns the compact-presentation property.
    ///
    /// @return the compact property
    public BooleanProperty compactProperty() {
        return compact;
    }

    /// Whether the root item is retained when intermediate levels overflow.
    ///
    /// At very narrow widths, the root may still collapse so that the overflow control and current location remain
    /// reachable.
    ///
    /// @defaultValue `false`
    private final BooleanProperty keepRootVisible = new SimpleBooleanProperty(this, "keepRootVisible") {
        /// Requests a new overflow layout after assignment.
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    /// Returns whether the root item is retained during overflow.
    ///
    /// @return `true` when overflow should preserve the root when space permits
    public boolean isKeepRootVisible() {
        return keepRootVisible.get();
    }

    /// Sets whether the root item is retained during overflow.
    ///
    /// @param keepRootVisible whether to preserve the root when space permits
    public void setKeepRootVisible(boolean keepRootVisible) {
        this.keepRootVisible.set(keepRootVisible);
    }

    /// Returns the root-preservation property.
    ///
    /// @return the keep-root-visible property
    public BooleanProperty keepRootVisibleProperty() {
        return keepRootVisible;
    }

    /// The maximum number of visible path entries, counting the overflow control when present.
    ///
    /// [#setMaxVisibleItems(int)] rejects values below `2`. A smaller value supplied directly to the property or by
    /// a binding is treated as `2` by layout while the property retains the supplied value.
    ///
    /// @defaultValue `4`
    private final IntegerProperty maxVisibleItems = new IntegerPropertyBase(DEFAULT_MAX_VISIBLE_ITEMS) {
        /// Requests a new overflow layout after assignment.
        @Override
        protected void invalidated() {
            requestLayout();
        }

        /// Returns the owning breadcrumbs control.
        @Override
        public Object getBean() {
            return M3Breadcrumbs.this;
        }

        /// Returns the property name.
        @Override
        public String getName() {
            return "maxVisibleItems";
        }
    };

    /// Returns the effective maximum number of visible path entries.
    ///
    /// @return the configured value, clamped to at least `2`
    public int getMaxVisibleItems() {
        return Math.max(MINIMUM_MAX_VISIBLE_ITEMS, maxVisibleItems.get());
    }

    /// Sets the maximum number of visible path entries.
    ///
    /// @param maxVisibleItems the maximum, including the overflow control
    /// @throws IllegalArgumentException if `maxVisibleItems` is less than `2`
    public void setMaxVisibleItems(int maxVisibleItems) {
        if (maxVisibleItems < MINIMUM_MAX_VISIBLE_ITEMS) {
            throw new IllegalArgumentException("maxVisibleItems must be at least 2: " + maxVisibleItems);
        }
        this.maxVisibleItems.set(maxVisibleItems);
    }

    /// Returns the maximum-visible-items property.
    ///
    /// A binding should supply values of at least `2`; smaller values are retained by the property but treated as
    /// `2` during layout.
    ///
    /// @return the maximum-visible-items property
    public IntegerProperty maxVisibleItemsProperty() {
        return maxVisibleItems;
    }

    /// Returns the live hierarchy in root-to-current order.
    ///
    /// Changes immediately update current-location state and overflow presentation. The list rejects `null` and
    /// identity duplicates.
    ///
    /// @return the mutable breadcrumb-item list
    public ObservableList<M3BreadcrumbItem> getItems() {
        return items;
    }

    /// Creates the default breadcrumbs skin.
    ///
    /// @return the default breadcrumbs skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BreadcrumbsSkin(this);
    }

    /// Returns the user-agent stylesheet for breadcrumbs.
    ///
    /// @return the breadcrumbs stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("breadcrumbs.css");
    }

    /// Initializes style, accessibility, item observation, and current-location state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setAccessibleText("Breadcrumbs");
        setFocusTraversable(false);
        getItems().addListener(itemsListener);
        pseudoClassStateChanged(COMPACT_PSEUDO_CLASS, false);
        updateCurrentItem();
    }

    /// Marks only the final hierarchy item as the current location.
    private void updateCurrentItem() {
        int currentIndex = getItems().size() - 1;
        for (int index = 0; index < getItems().size(); index++) {
            getItems().get(index).setCurrent(index == currentIndex);
        }
        requestLayout();
    }
}
