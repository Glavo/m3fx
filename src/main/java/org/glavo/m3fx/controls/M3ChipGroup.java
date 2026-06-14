// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ChipGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 chip group that lays chips out as a wrapping set.
///
/// The group manages [M3Chip] selection according to [M3ChipSelectionMode], wraps chips across rows, and exposes
/// read-only selected-chip views. Use it when chips form one interactive set rather than unrelated standalone
/// actions.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public class M3ChipGroup extends Control {
    /// The base style class for M3FX chip groups.
    public static final String STYLE_CLASS = "m3-chip-group";

    /// The default horizontal gap between chips.
    private static final double DEFAULT_HORIZONTAL_GAP = 8.0;

    /// The default vertical gap between wrapped chip rows.
    private static final double DEFAULT_VERTICAL_GAP = 8.0;

    /// The mutable chip group content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between chips.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(this, getItems(), getSelectedChip(), M3Chip.class));

    // The preferred wrapping width used by the internal flow layout.
    private final DoubleProperty prefWrapLength = new SimpleDoubleProperty(this, "prefWrapLength", 400.0) {
        /// Validates updated preferred wrap length values.
        @Override
        protected void invalidated() {
            set(M3Css.nonNegative(get(), "prefWrapLength"));
        }
    };

    // The styleable horizontal gap between chips.
    private @Nullable StyleableDoubleProperty horizontalGap;

    // The styleable vertical gap between wrapped chip rows.
    private @Nullable StyleableDoubleProperty verticalGap;

    // The chip selection mode.
    private final ObjectProperty<M3ChipSelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3ChipSelectionMode.MULTIPLE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ChipSelectionMode.MULTIPLE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    // Whether the group allows all chips to be unselected.
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
    private final ObservableList<M3Chip> selectedChips = FXCollections.observableArrayList();

    /// The read-only view of currently selected chips.
    private final @UnmodifiableView ObservableList<M3Chip> selectedChipsView =
            FXCollections.unmodifiableObservableList(selectedChips);

    // The first selected chip in child order.
    private final ReadOnlyObjectWrapper<@Nullable M3Chip> selectedChip =
            new ReadOnlyObjectWrapper<>(this, "selectedChip");

    /// The selected-state listeners installed on chips.
    private final Map<M3Chip, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// The reachability listeners installed on chips.
    private final Map<M3Chip, ChangeListener<Boolean>> reachabilityListeners = new HashMap<>();

    /// Updates chip listeners and selection when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3Chip chip) {
                    uninstallChip(chip);
                    chip.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3Chip chip) {
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

    /// Creates an empty chip group.
    public M3ChipGroup() {
        initialize();
    }

    /// Creates a chip group containing the supplied chips.
    ///
    /// @param chips the chips displayed by the group
    public M3ChipGroup(M3Chip... chips) {
        initialize();
        addChips(chips);
    }

    /// Returns the mutable child list used as chip group content.
    ///
    /// @return the mutable child list used as chip group content
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Adds one chip.
    ///
    /// @param chip the chip to add
    public final void addChip(M3Chip chip) {
        getItems().add(Objects.requireNonNull(chip, "chip"));
    }

    /// Adds chips.
    ///
    /// @param chips the chips to add
    public final void addChips(M3Chip... chips) {
        validateChips(chips);
        getItems().addAll(chips);
    }

    /// Replaces all chip nodes.
    ///
    /// @param chips the replacement chip nodes
    public final void setChips(M3Chip... chips) {
        validateChips(chips);
        getItems().setAll(chips);
    }

    /// Removes all chip group content.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the preferred wrapping width used by the chip flow layout.
    ///
    /// @return the preferred wrap length in pixels
    public final double getPrefWrapLength() {
        return prefWrapLength.get();
    }

    /// Sets the preferred wrapping width used by the chip flow layout.
    ///
    /// @param prefWrapLength the preferred wrap length in pixels
    public final void setPrefWrapLength(double prefWrapLength) {
        this.prefWrapLength.set(M3Css.nonNegative(prefWrapLength, "prefWrapLength"));
    }

    /// Returns the preferred wrapping width property.
    ///
    /// @return the preferred wrap length property
    public final DoubleProperty prefWrapLengthProperty() {
        return prefWrapLength;
    }

    /// Returns the horizontal gap between chips in pixels.
    ///
    /// @return the horizontal chip gap
    public final double getHorizontalGap() {
        return horizontalGap == null ? DEFAULT_HORIZONTAL_GAP : horizontalGap.get();
    }

    /// Sets the horizontal gap between chips.
    ///
    /// @param horizontalGap the horizontal chip gap in pixels
    public final void setHorizontalGap(double horizontalGap) {
        horizontalGapProperty().set(M3Css.nonNegative(horizontalGap, "horizontalGap"));
    }

    /// Returns the horizontal gap property.
    ///
    /// @return the styleable horizontal chip gap property
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

    /// Returns the vertical gap between wrapped chip rows in pixels.
    ///
    /// @return the vertical chip row gap
    public final double getVerticalGap() {
        return verticalGap == null ? DEFAULT_VERTICAL_GAP : verticalGap.get();
    }

    /// Sets the vertical gap between wrapped chip rows.
    ///
    /// @param verticalGap the vertical chip row gap in pixels
    public final void setVerticalGap(double verticalGap) {
        verticalGapProperty().set(M3Css.nonNegative(verticalGap, "verticalGap"));
    }

    /// Returns the vertical gap property.
    ///
    /// @return the styleable vertical chip row gap property
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
    public final M3ChipSelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the chip selection mode.
    ///
    /// @param selectionMode the chip selection mode
    public final void setSelectionMode(M3ChipSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the chip selection mode property.
    ///
    /// @return the chip selection mode property
    public final ObjectProperty<M3ChipSelectionMode> selectionModeProperty() {
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

    /// Returns the empty-selection policy property.
    ///
    /// @return the empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns the currently selected chips in child order.
    ///
    /// @return an immutable observable view of selected chips in child order
    public final @UnmodifiableView ObservableList<M3Chip> getSelectedChips() {
        return selectedChipsView;
    }

    /// Returns the first selected chip in child order.
    ///
    /// @return the first selected chip in child order, or `null` when selection is empty
    public final @Nullable M3Chip getSelectedChip() {
        return selectedChip.get();
    }

    /// Returns the first selected chip property.
    ///
    /// @return the read-only first selected chip property
    public final ReadOnlyObjectProperty<@Nullable M3Chip> selectedChipProperty() {
        return selectedChip.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected chip, or `-1` when no chip is selected.
    ///
    /// @return the child index of the first selected chip, or `-1` when selection is empty
    public final int getSelectedIndex() {
        @Nullable M3Chip chip = getSelectedChip();
        return chip == null ? -1 : getItems().indexOf(chip);
    }

    /// Selects a chip that belongs to this group.
    ///
    /// @param chip the chip to select
    public final void select(M3Chip chip) {
        Objects.requireNonNull(chip, "chip");
        if (!getItems().contains(chip)) {
            throw new IllegalArgumentException("chip must belong to this chip group");
        }
        if (!isSelectableChip(chip)) {
            throw new IllegalArgumentException("chip must be selectable");
        }

        if (getSelectionMode() == M3ChipSelectionMode.SINGLE) {
            selectOnly(chip);
        } else {
            setChipSelected(chip, true);
        }
    }

    /// Selects the chip at the given child index.
    ///
    /// @param index the child index to select
    public final void selectIndex(int index) {
        Node child = getItems().get(index);
        if (child instanceof M3Chip chip) {
            select(chip);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3Chip");
    }

    /// Selects the first chip when one exists.
    public final void selectFirst() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3Chip firstChip = firstChip();
        if (firstChip != null) {
            select(firstChip);
        }
    }

    /// Selects the last chip when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3Chip lastChip = M3SelectionNavigation.last(getItems(), M3Chip.class);
        if (lastChip != null) {
            select(lastChip);
        }
    }

    /// Selects the next chip after the current selected chip, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3Chip nextChip = M3SelectionNavigation.next(getItems(), getSelectedChip(), M3Chip.class);
        if (nextChip != null) {
            select(nextChip);
        }
    }

    /// Selects the previous chip before the current selected chip, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3Chip previousChip =
                M3SelectionNavigation.previous(getItems(), getSelectedChip(), M3Chip.class);
        if (previousChip != null) {
            select(previousChip);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
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
                    M3Chip.class
            );
            case MULTIPLE_SELECTION -> getSelectionMode() == M3ChipSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedChipsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for chips.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedChip(),
                    M3Chip.class
            ));
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> M3Accessible.showItemOrDefault(M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedChip(),
                    M3Chip.class
            ), getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
        focusNotifier.start();
    }

    /// Applies keyboard navigation across enabled chips.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3ChipSelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(getItems(), getSelectedChip(), M3Chip.class),
                    M3Chip.class,
                    true,
                    true,
                    M3SelectionNavigation.isRightToLeft(this)
            );
            return;
        }

        M3SelectionNavigation.handleKeySelection(
                event,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedChip(), M3Chip.class),
                M3Chip.class,
                true,
                true,
                M3SelectionNavigation.isRightToLeft(this),
                this::select
        );
    }

    /// Applies selected chips supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3ChipSelectionMode.SINGLE) {
            @Nullable M3Chip chip = firstAccessibleSelectableChip(parameters);
            if (chip == null) {
                clearSelection();
            } else {
                select(chip);
            }
            return;
        }

        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3Chip chip && isSelectableChip(chip)) {
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
    private void installChip(M3Chip chip) {
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleChipSelectedChanged(chip, newValue);
        selectedListeners.put(chip, listener);
        chip.selectedProperty().addListener(listener);
        ChangeListener<Boolean> reachabilityListener = (observable, oldValue, newValue) ->
                handleChipReachabilityChanged(chip);
        reachabilityListeners.put(chip, reachabilityListener);
        chip.disabledProperty().addListener(reachabilityListener);
        chip.visibleProperty().addListener(reachabilityListener);
    }

    /// Removes the selected-state listener from a chip.
    private void uninstallChip(M3Chip chip) {
        ChangeListener<Boolean> listener = selectedListeners.remove(chip);
        if (listener != null) {
            chip.selectedProperty().removeListener(listener);
        }
        ChangeListener<Boolean> reachabilityListener = reachabilityListeners.remove(chip);
        if (reachabilityListener != null) {
            chip.disabledProperty().removeListener(reachabilityListener);
            chip.visibleProperty().removeListener(reachabilityListener);
        }
    }

    /// Keeps chip selected states consistent with the current group policy.
    private void handleChipSelectedChanged(M3Chip chip, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (!isSelectableChip(chip)) {
            if (selected) {
                setChipSelected(chip, false);
                if (!isAllowEmptySelection()) {
                    selectFirstChipIfNeeded();
                }
            }
            return;
        }

        if (selected && getSelectionMode() == M3ChipSelectionMode.SINGLE) {
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
    private void handleChipReachabilityChanged(M3Chip chip) {
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
        if (getSelectionMode() == M3ChipSelectionMode.SINGLE && selectedChips.size() > 1) {
            selectOnly(selectedChips.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstChipIfNeeded();
        }
    }

    /// Selects the first chip when the selection is empty and empty selection is disabled.
    private void selectFirstChipIfNeeded() {
        if (!selectedChips.isEmpty()) {
            return;
        }

        M3Chip firstChip = firstChip();
        if (firstChip != null) {
            select(firstChip);
        }
    }

    /// Sets one chip's selected state and refreshes selected chip state.
    private void setChipSelected(M3Chip chip, boolean selected) {
        setChipSelectedWithoutRefresh(chip, selected);
        refreshSelectedChips();
    }

    /// Sets one chip's selected state without refreshing the aggregate selected chip list.
    private void setChipSelectedWithoutRefresh(M3Chip chip, boolean selected) {
        updatingSelection = true;
        try {
            chip.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects one chip and clears selection from the remaining chips.
    private void selectOnly(@Nullable M3Chip chip) {
        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3Chip item) {
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
        List<M3Chip> previousSelection = List.copyOf(selectedChips);
        selectedChips.clear();
        for (Node child : getItems()) {
            if (child instanceof M3Chip chip && chip.isSelected()) {
                if (isSelectableChip(chip)) {
                    selectedChips.add(chip);
                } else {
                    setChipSelectedWithoutRefresh(chip, false);
                }
            }
        }
        selectedChip.set(selectedChips.isEmpty() ? null : selectedChips.get(0));
        if (!selectedChips.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Returns the first chip child.
    private @Nullable M3Chip firstChip() {
        return M3SelectionNavigation.first(getItems(), M3Chip.class);
    }

    /// Returns the first selectable chip referenced by accessibility parameters.
    private @Nullable M3Chip firstAccessibleSelectableChip(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            if (child instanceof M3Chip chip
                    && isSelectableChip(chip)
                    && M3Accessible.containsSelectionTarget(chip, parameters)) {
                return chip;
            }
        }
        return null;
    }

    /// Returns whether a chip can currently participate in selection.
    private boolean isSelectableChip(M3Chip chip) {
        return M3Accessible.isEffectivelyReachable(this) && M3Accessible.isEffectivelyReachable(chip);
    }

    /// Creates the default Material Design 3 chip group skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ChipGroupSkin(this);
    }

    /// Validates a chip array.
    private static void validateChips(M3Chip... chips) {
        Objects.requireNonNull(chips, "chips");
        for (M3Chip chip : chips) {
            Objects.requireNonNull(chip, "chip");
        }
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
