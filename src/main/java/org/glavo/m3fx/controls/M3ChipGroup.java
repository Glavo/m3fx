// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3ScrollReveal;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3ChipGroupSkin;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 wrapping container that coordinates chip selection.
///
/// The group owns an ordered, live list of [M3Chip] nodes. Selectable chips participate in the configured
/// [selection mode][#selectionModeProperty()], while command-only chips remain in layout and keyboard traversal
/// without entering the selected-item views. Selection order always follows item order rather than the order in
/// which chips were selected.
///
/// Changing the mode enforces its invariant immediately: `NONE` clears selection, `SINGLE` retains at most the
/// first selected reachable chip, and `MULTIPLE` retains all reachable selections. When empty selection is
/// disallowed, the first reachable selectable chip is selected whenever necessary. Disabled or invisible chips do
/// not participate in selection.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public final class M3ChipGroup extends Control {
    /// The base style class for M3FX chip groups.
    public static final String STYLE_CLASS = "m3-chip-group";

    /// The default horizontal gap between chips.
    private static final double DEFAULT_HORIZONTAL_GAP = 8.0;

    /// The default vertical gap between wrapped chip rows.
    private static final double DEFAULT_VERTICAL_GAP = 8.0;

    /// The live, mutable list of chips in layout and traversal order.
    ///
    /// The list rejects `null`, preserves insertion order, and is observed for subsequent changes. Removing a
    /// selected chip clears its selected state. A chip cannot simultaneously be a child of another parent.
    private final ObservableList<M3Chip> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between chips.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(
                            this,
                            getItems(),
                            getSelectedChip(),
                            M3SelectableChip.class
                    ));

    /// The preferred width at which chip rows wrap, in logical pixels.
    ///
    /// The default value is `400.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `400.0`
    private final DoubleProperty prefWrapLength = new SimpleDoubleProperty(this, "prefWrapLength", 400.0) {
        /// Validates updated preferred wrap length values.
        @Override
        protected void invalidated() {
            set(M3Css.nonNegative(get(), "prefWrapLength"));
        }
    };

    /// The horizontal gap between adjacent chips, in logical pixels.
    ///
    /// The default value is `8.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty horizontalGap;

    /// The vertical gap between wrapped rows, in logical pixels.
    ///
    /// The default value is `8.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty verticalGap;

    /// The selection policy applied to selectable chips.
    ///
    /// The default value is [M3SelectionMode#MULTIPLE]. The property never reports `null`; a direct `null`
    /// assignment restores the default.
    ///
    /// @defaultValue [M3SelectionMode#MULTIPLE]
    private final ObjectProperty<M3SelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3SelectionMode.MULTIPLE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SelectionMode.MULTIPLE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// Whether the selected-chip set may be empty while selection is enabled.
    ///
    /// The default value is `true`. Changing it to `false` immediately selects the first reachable selectable
    /// chip when needed. It has no effect while the selection mode is [M3SelectionMode#NONE].
    ///
    /// @defaultValue `true`
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected chip when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstChipIfNeeded();
            }
        }
    };

    /// The currently selected chips in child order.
    private final ObservableList<M3SelectableChip> selectedChips =
            M3ObservableLists.nonNullElementList("selectedChip");

    /// The read-only view of currently selected chips.
    private final @UnmodifiableView ObservableList<M3SelectableChip> selectedChipsView =
            FXCollections.unmodifiableObservableList(selectedChips);

    /// The first selected chip in child order.
    private final ReadOnlyObjectWrapper<@Nullable M3SelectableChip> selectedChip =
            new ReadOnlyObjectWrapper<>(this, "selectedChip");

    /// Reusable storage for computing selected chips without allocating on every refresh.
    private final List<M3SelectableChip> selectedChipsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed chip.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3SelectableChip chip) {
            handleChipSelectedChanged(chip, chip.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed chip.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3SelectableChip chip) {
            handleChipReachabilityChanged(chip);
        }
    };

    /// Updates chip listeners and selection when children change.
    private final ListChangeListener<M3Chip> childrenListener = change -> {
        while (change.next()) {
            for (M3Chip child : change.getRemoved()) {
                if (child instanceof M3SelectableChip chip) {
                    uninstallChip(chip);
                    chip.setSelected(false);
                }
            }
            for (M3Chip child : change.getAddedSubList()) {
                if (child instanceof M3SelectableChip chip) {
                    installChip(chip);
                }
            }
        }
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Whether the group is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty multiple-selection chip group with empty selection allowed.
    public M3ChipGroup() {
        initialize();
    }

    /// Returns the live list of chips displayed by this group.
    ///
    /// Changes to the returned list are reflected immediately. The list preserves insertion order and rejects
    /// `null` elements.
    ///
    /// @return the live, mutable chip list
    public final ObservableList<M3Chip> getItems() {
        return items;
    }





    /// Returns the preferred wrapping width used by the chip flow layout.
    ///
    /// @return the preferred wrap length in logical pixels
    public final double getPrefWrapLength() {
        return prefWrapLength.get();
    }

    /// Sets the preferred wrapping width used by the chip flow layout.
    ///
    /// @param prefWrapLength the preferred wrap length in logical pixels
    /// @throws IllegalArgumentException if `prefWrapLength` is negative or not finite
    public final void setPrefWrapLength(double prefWrapLength) {
        this.prefWrapLength.set(M3Css.nonNegative(prefWrapLength, "prefWrapLength"));
    }

    public final DoubleProperty prefWrapLengthProperty() {
        return prefWrapLength;
    }

    /// Returns the horizontal gap between chips.
    ///
    /// @return the horizontal chip gap
    public final double getHorizontalGap() {
        return horizontalGap == null ? DEFAULT_HORIZONTAL_GAP : horizontalGap.get();
    }

    /// Sets the horizontal gap between chips.
    ///
    /// @param horizontalGap the horizontal chip gap in logical pixels
    /// @throws IllegalArgumentException if `horizontalGap` is negative or not finite
    public final void setHorizontalGap(double horizontalGap) {
        horizontalGapProperty().set(M3Css.nonNegative(horizontalGap, "horizontalGap"));
    }

    public final StyleableDoubleProperty horizontalGapProperty() {
        if (horizontalGap == null) {
            horizontalGap = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_GAP,
                    this,
                    "horizontalGap",
                    StyleableProperties.HORIZONTAL_GAP,
                    this::requestLayout
            );
        }
        return horizontalGap;
    }

    /// Returns the vertical gap between wrapped chip rows.
    ///
    /// @return the vertical chip row gap
    public final double getVerticalGap() {
        return verticalGap == null ? DEFAULT_VERTICAL_GAP : verticalGap.get();
    }

    /// Sets the vertical gap between wrapped chip rows.
    ///
    /// @param verticalGap the vertical chip row gap in logical pixels
    /// @throws IllegalArgumentException if `verticalGap` is negative or not finite
    public final void setVerticalGap(double verticalGap) {
        verticalGapProperty().set(M3Css.nonNegative(verticalGap, "verticalGap"));
    }

    public final StyleableDoubleProperty verticalGapProperty() {
        if (verticalGap == null) {
            verticalGap = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_VERTICAL_GAP,
                    this,
                    "verticalGap",
                    StyleableProperties.VERTICAL_GAP,
                    this::requestLayout
            );
        }
        return verticalGap;
    }

    /// Returns the chip selection mode.
    ///
    /// @return the chip selection mode
    public final M3SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the chip selection mode.
    ///
    /// @param selectionMode the chip selection mode
    /// @throws NullPointerException if `selectionMode` is `null`
    public final void setSelectionMode(M3SelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    public final ObjectProperty<M3SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns whether this group allows all chips to be unselected.
    ///
    /// @return `true` when the group allows empty selection
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this group allows all chips to be unselected.
    ///
    /// @param allowEmptySelection whether the group should allow empty selection
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns a read-only observable view of selected chips in item order.
    ///
    /// The returned view is live and may contain only [M3SelectableChip] instances that are currently reachable.
    /// Attempts to modify it fail with [UnsupportedOperationException].
    ///
    /// @return the live, read-only selected-chip view
    public final @UnmodifiableView ObservableList<M3SelectableChip> getSelectedChips() {
        return selectedChipsView;
    }

    /// Returns the first selected chip in child order.
    ///
    /// @return the first selected chip in child order, or `null` when selection is empty
    public final @Nullable M3SelectableChip getSelectedChip() {
        return selectedChip.get();
    }

    public final ReadOnlyObjectProperty<@Nullable M3SelectableChip> selectedChipProperty() {
        return selectedChip.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected chip, or `-1` when no chip is selected.
    ///
    /// @return the child index of the first selected chip, or `-1` when selection is empty
    public final int getSelectedIndex() {
        @Nullable M3SelectableChip chip = getSelectedChip();
        return chip == null ? -1 : getItems().indexOf(chip);
    }

    /// Selects a chip that belongs to this group.
    ///
    /// @param chip the chip to select
    /// @throws NullPointerException if `chip` is `null`
    /// @throws IllegalArgumentException if `chip` does not belong to this group or is not currently selectable
    public final void select(M3SelectableChip chip) {
        Objects.requireNonNull(chip, "chip");
        if (!getItems().contains(chip)) {
            throw new IllegalArgumentException("chip must belong to this chip group");
        }
        if (!isSelectableChip(chip)) {
            throw new IllegalArgumentException("chip must be selectable");
        }
        if (getSelectionMode() == M3SelectionMode.NONE) {
            return;
        }

        if (getSelectionMode() == M3SelectionMode.SINGLE) {
            selectOnly(chip);
        } else {
            setChipSelected(chip, true);
        }
    }

    /// Selects the chip at the given child index.
    ///
    /// @param index the child index to select
    /// @throws IndexOutOfBoundsException if `index` is outside the item list
    /// @throws IllegalArgumentException if the item at `index` does not support selection or is not selectable
    public final void selectIndex(int index) {
        M3Chip child = getItems().get(index);
        if (child instanceof M3SelectableChip chip) {
            select(chip);
            return;
        }
        throw new IllegalArgumentException("chip at index does not support selection");
    }

    /// Selects the first reachable selectable chip, if one exists.
    public final void selectFirst() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3SelectableChip firstChip = firstChip();
        if (firstChip != null) {
            select(firstChip);
        }
    }

    /// Selects the last reachable selectable chip, if one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3SelectableChip lastChip = selectableChipFrom(getItems().size() - 1, -1, false);
        if (lastChip != null) {
            select(lastChip);
        }
    }

    /// Selects the next chip after the current selected chip, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3SelectableChip nextChip = adjacentSelectableChip(getSelectedChip(), 1);
        if (nextChip != null) {
            select(nextChip);
        }
    }

    /// Selects the previous chip before the current selected chip, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3SelectableChip previousChip = adjacentSelectableChip(getSelectedChip(), -1);
        if (previousChip != null) {
            select(previousChip);
        }
    }

    /// Clears the current selection when permitted by the selection policy.
    ///
    /// If empty selection is disallowed while selection is enabled, this method preserves or restores the first
    /// reachable selection instead.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3SelectionMode.NONE) {
            selectFirstChipIfNeeded();
            return;
        }
        selectOnly(null);
    }

    /// Returns the user-agent stylesheet for M3FX chip groups.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("chip.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for chip group content and selection state.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedChip(),
                    M3SelectableChip.class
            );
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedChipsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for chips.
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleSelectionTarget();
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Requests focus on the current selected or focused accessibility target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleSelectionTarget() {
        if (M3Accessible.showItem(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedChip(),
                M3SelectableChip.class
        ))) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showItemOrDefault(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedChip(),
                M3SelectableChip.class
        ), getItems(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the group focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
        focusNotifier.start();
    }

    /// Applies keyboard navigation across enabled chips.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            return;
        }
        if (getSelectionMode() == M3SelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    this,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(
                            getItems(),
                            getSelectedChip(),
                            M3SelectableChip.class
                    ),
                    M3SelectableChip.class,
                    true,
                    true,
                    M3NodeLayout.isRightToLeft(this)
            );
            return;
        }

        handleSingleSelectionNavigation(event);
    }

    /// Selects and focuses the selectable chip implied by a single-selection navigation key.
    private void handleSingleSelectionNavigation(KeyEvent event) {
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        @Nullable M3SelectableChip anchor = focusedSelectableChip();
        if (anchor == null) {
            anchor = getSelectedChip();
        }

        boolean rightToLeft = M3NodeLayout.isRightToLeft(this);
        @Nullable M3SelectableChip target = switch (event.getCode()) {
            case LEFT -> horizontalSelectableChip(anchor, false, rightToLeft);
            case RIGHT -> horizontalSelectableChip(anchor, true, rightToLeft);
            case UP -> adjacentSelectableChip(anchor, -1);
            case DOWN -> adjacentSelectableChip(anchor, 1);
            case HOME -> firstChip();
            case END -> selectableChipFrom(getItems().size() - 1, -1, false);
            default -> null;
        };
        if (target == null) {
            return;
        }

        select(target);
        M3ScrollReveal.requestFocusAndReveal(this, target);
        event.consume();
    }

    /// Returns a selectable target for a logical horizontal arrow key.
    private @Nullable M3SelectableChip horizontalSelectableChip(
            @Nullable M3SelectableChip anchor,
            boolean rightKey,
            boolean rightToLeft
    ) {
        if (anchor == null) {
            return rightKey
                    ? firstChip()
                    : selectableChipFrom(getItems().size() - 1, -1, false);
        }
        boolean forward = rightToLeft != rightKey;
        return adjacentSelectableChip(anchor, forward ? 1 : -1);
    }

    /// Returns the focused selectable chip when focus currently belongs to one group child.
    private @Nullable M3SelectableChip focusedSelectableChip() {
        if (getScene() == null || !(getScene().getFocusOwner() instanceof M3SelectableChip chip)) {
            return null;
        }
        return getItems().contains(chip) && isSelectableChip(chip) ? chip : null;
    }

    /// Returns a selectable chip adjacent to the supplied anchor, wrapping at group boundaries.
    private @Nullable M3SelectableChip adjacentSelectableChip(
            @Nullable M3SelectableChip anchor,
            int direction
    ) {
        int childCount = getItems().size();
        if (childCount == 0) {
            return null;
        }
        if (anchor == null) {
            return selectableChipFrom(direction > 0 ? 0 : childCount - 1, direction, false);
        }
        int anchorIndex = getItems().indexOf(anchor);
        int startIndex = anchorIndex < 0
                ? (direction > 0 ? 0 : childCount - 1)
                : Math.floorMod(anchorIndex + direction, childCount);
        return selectableChipFrom(startIndex, direction, true);
    }

    /// Scans child indices for one selectable chip without allocating a filtered collection.
    private @Nullable M3SelectableChip selectableChipFrom(int startIndex, int direction, boolean wrap) {
        int childCount = getItems().size();
        if (startIndex < 0 || startIndex >= childCount) {
            return null;
        }

        int index = startIndex;
        int inspected = 0;
        while (index >= 0 && index < childCount && inspected < childCount) {
            M3Chip child = getItems().get(index);
            if (child instanceof M3SelectableChip chip && isSelectableChip(chip)) {
                return chip;
            }
            index += direction;
            inspected++;
            if (wrap) {
                index = Math.floorMod(index, childCount);
            }
        }
        return null;
    }

    /// Applies selected chips supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            selectOnly(null);
            return;
        }
        if (getSelectionMode() == M3SelectionMode.SINGLE) {
            @Nullable M3SelectableChip chip = firstAccessibleSelectableChip(parameters);
            if (chip == null) {
                clearSelection();
            } else {
                select(chip);
            }
            return;
        }

        updatingSelection = true;
        try {
            for (M3Chip child : getItems()) {
                if (child instanceof M3SelectableChip chip && isSelectableChip(chip)) {
                    chip.setSelected(M3Accessible.containsSelectionTarget(chip, parameters));
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedChips();
        if (!isAllowEmptySelection()) {
            selectFirstChipIfNeeded();
        }
    }

    /// Installs a selected-state listener on a chip.
    private void installChip(M3SelectableChip chip) {
        chip.selectedProperty().addListener(selectedInvalidation);
        chip.disabledProperty().addListener(reachabilityInvalidation);
        chip.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes the selected-state listener from a chip.
    private void uninstallChip(M3SelectableChip chip) {
        chip.selectedProperty().removeListener(selectedInvalidation);
        chip.disabledProperty().removeListener(reachabilityInvalidation);
        chip.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Keeps chip selected states consistent with the current group policy.
    private void handleChipSelectedChanged(M3SelectableChip chip, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (getSelectionMode() == M3SelectionMode.NONE || !isSelectableChip(chip)) {
            if (selected) {
                setChipSelected(chip, false);
                if (!isAllowEmptySelection()) {
                    selectFirstChipIfNeeded();
                }
            }
            return;
        }

        if (selected && getSelectionMode() == M3SelectionMode.SINGLE) {
            selectOnly(chip);
            return;
        }

        if (!selected && !isAllowEmptySelection()
                && selectedChips.size() == 1
                && selectedChips.get(0) == chip) {
            selectOnly(chip);
            return;
        }

        refreshSelectedChips();
        if (!isAllowEmptySelection()) {
            selectFirstChipIfNeeded();
        }
    }

    /// Keeps selection and accessibility state consistent when a chip becomes unreachable.
    private void handleChipReachabilityChanged(M3SelectableChip chip) {
        if (chip.isSelected() && !isSelectableChip(chip)) {
            setChipSelected(chip, false);
        }
        enforceSelectionPolicy();
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Enforces single selection and non-empty selection invariants.
    private void enforceSelectionPolicy() {
        refreshSelectedChips();
        if (getSelectionMode() == M3SelectionMode.NONE) {
            selectOnly(null);
            return;
        }
        if (getSelectionMode() == M3SelectionMode.SINGLE && selectedChips.size() > 1) {
            selectOnly(selectedChips.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstChipIfNeeded();
        }
    }

    /// Selects the first chip when the selection is empty and empty selection is disabled.
    private void selectFirstChipIfNeeded() {
        if (getSelectionMode() == M3SelectionMode.NONE || !selectedChips.isEmpty()) {
            return;
        }

        M3SelectableChip firstChip = firstChip();
        if (firstChip != null) {
            select(firstChip);
        }
    }

    /// Sets one chip's selected state and refreshes selected chip state.
    private void setChipSelected(M3SelectableChip chip, boolean selected) {
        setChipSelectedWithoutRefresh(chip, selected);
        refreshSelectedChips();
    }

    /// Sets one chip's selected state without refreshing the aggregate selected chip list.
    private void setChipSelectedWithoutRefresh(M3SelectableChip chip, boolean selected) {
        updatingSelection = true;
        try {
            chip.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects one chip and clears selection from the remaining chips.
    private void selectOnly(@Nullable M3SelectableChip chip) {
        updatingSelection = true;
        try {
            for (M3Chip child : getItems()) {
                if (child instanceof M3SelectableChip item) {
                    item.setSelected(item == chip);
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedChips();
    }

    /// Refreshes the selected chip list from current child states.
    private void refreshSelectedChips() {
        selectedChipsScratch.clear();
        for (M3Chip child : getItems()) {
            if (child instanceof M3SelectableChip chip && chip.isSelected()) {
                if (isSelectableChip(chip)) {
                    selectedChipsScratch.add(chip);
                } else {
                    setChipSelectedWithoutRefresh(chip, false);
                }
            }
        }
        boolean selectionChanged = !selectedChips.equals(selectedChipsScratch);
        if (selectionChanged) {
            selectedChips.setAll(selectedChipsScratch);
        }
        selectedChipsScratch.clear();

        selectedChip.set(selectedChips.isEmpty() ? null : selectedChips.get(0));
        if (selectionChanged) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Returns the first reachable chip whose variant supports selection.
    private @Nullable M3SelectableChip firstChip() {
        for (M3Chip child : getItems()) {
            if (child instanceof M3SelectableChip chip && isSelectableChip(chip)) {
                return chip;
            }
        }
        return null;
    }

    /// Returns the first selectable chip referenced by accessibility parameters.
    private @Nullable M3SelectableChip firstAccessibleSelectableChip(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (M3Chip child : getItems()) {
            if (child instanceof M3SelectableChip chip
                    && isSelectableChip(chip)
                    && M3Accessible.containsSelectionTarget(chip, parameters)) {
                return chip;
            }
        }
        return null;
    }

    /// Returns whether a chip can currently participate in selection.
    private boolean isSelectableChip(M3SelectableChip chip) {
        return M3Accessible.isEffectivelyReachable(this)
                && M3Accessible.isEffectivelyReachable(chip);
    }

    /// Creates the default Material Design 3 chip group skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ChipGroupSkin(this);
    }


    /// CSS metadata for chip group layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the horizontal chip gap.
        private static final CssMetaData<M3ChipGroup, Number> HORIZONTAL_GAP =
                new CssMetaData<>(
                        "-m3-chip-group-horizontal-gap",
                        SizeConverter.getInstance(),
                        DEFAULT_HORIZONTAL_GAP
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ChipGroup control) {
                        return M3Css.isSettable(control.horizontalGapProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ChipGroup control) {
                        return control.horizontalGapProperty();
                    }
                };

        /// CSS metadata for the vertical chip row gap.
        private static final CssMetaData<M3ChipGroup, Number> VERTICAL_GAP =
                new CssMetaData<>(
                        "-m3-chip-group-vertical-gap",
                        SizeConverter.getInstance(),
                        DEFAULT_VERTICAL_GAP
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ChipGroup control) {
                        return M3Css.isSettable(control.verticalGapProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ChipGroup control) {
                        return control.verticalGapProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(HORIZONTAL_GAP);
            styleables.add(VERTICAL_GAP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
