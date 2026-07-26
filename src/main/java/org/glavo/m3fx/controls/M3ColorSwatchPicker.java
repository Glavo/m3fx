// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorSwatchPickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides a wrapping, single-selection palette of color swatches.
///
/// [#getItems()] contains immutable color values rather than scene-graph nodes. It does not permit two colors with
/// equal [canonical RGBA representations][M3Color#isEquivalentTo(M3Color)], including equivalent colors represented
/// in different color spaces. Selection is exposed through read-only index and color properties.
///
/// Arrow keys move focus through the visual grid without changing selection. Home and End move focus to the first
/// and last item. Space or Enter activates the focused swatch. Horizontal arrow meaning mirrors under
/// right-to-left orientation. User activation of the selected swatch clears selection only when
/// [#allowEmptySelectionProperty()] is `true`. Programmatic selection is deterministic and does not apply that
/// toggle policy.
///
/// Selection changes are reported through [#selectedIndexProperty()] and [#selectedColorProperty()]; this control
/// does not fire a separate action event.
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a corresponding standard component.
@NotNullByDefault
public final class M3ColorSwatchPicker extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-swatch-picker";

    /// Creates an empty picker with no selection.
    public M3ColorSwatchPicker() {
        initialize();
    }

    /// The live ordered color list.
    private final ObservableList<M3Color> items =
            M3ObservableLists.distinctElementList("color item", M3Color::isEquivalentTo);

    /// Preserves selection across item-list mutations.
    private final ListChangeListener<M3Color> itemsListener = this::handleItemsChanged;

    /// Returns the live ordered color list.
    ///
    /// Mutations are reflected by the control immediately. The list rejects `null` elements and any color whose
    /// [canonical RGBA representation][M3Color#isEquivalentTo(M3Color)] equals another item. Color-space identity
    /// and latent achromatic channel values do not make two items distinct.
    ///
    /// Inserting or removing items before the selected item preserves that item's selection by adjusting the
    /// selected index. Replacing the selected item selects the corresponding replacement; removing it without a
    /// replacement clears selection.
    ///
    /// The list's direct `addAll`, `setAll`, and `replaceAll` operations validate the complete candidate result
    /// before modifying the list. A failure in one of those operations therefore leaves the existing items
    /// unchanged. A mutating operation throws [NullPointerException] for a `null` element and
    /// [IllegalArgumentException] if its result would contain equivalent canonical RGBA representations.
    ///
    /// Operations defined by [java.util.List] in terms of [Object#equals(Object)], such as `contains`, `indexOf`,
    /// and removal by object, use the structural equality of the color records rather than rendered equivalence.
    ///
    /// @return the live mutable color list
    public ObservableList<M3Color> getItems() {
        return items;
    }

    /// The read-only selected item index, or `-1` for no selection.
    ///
    /// @defaultValue `-1`
    private final ReadOnlyIntegerWrapper selectedIndex =
            new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);

    /// Returns the selected item index.
    ///
    /// @return an index in the current item list, or `-1` when selection is empty
    public int getSelectedIndex() {
        return selectedIndex.get();
    }

    /// Returns the read-only property containing the selected item index.
    ///
    /// The property value is always `-1` or a valid index in [#getItems()].
    ///
    /// @return the selected-index property
    public ReadOnlyIntegerProperty selectedIndexProperty() {
        return selectedIndex.getReadOnlyProperty();
    }

    /// The read-only selected color, or `null` for no selection.
    ///
    /// @defaultValue `null`
    private final ReadOnlyObjectWrapper<@Nullable M3Color> selectedColor =
            new ReadOnlyObjectWrapper<>(this, "selectedColor");

    /// Returns the selected color.
    ///
    /// @return the exact item object at [#getSelectedIndex()], or `null` when selection is empty
    public @Nullable M3Color getSelectedColor() {
        return selectedColor.get();
    }

    /// Returns the read-only property containing the selected color.
    ///
    /// The property contains the item object at [#getSelectedIndex()] and updates when that item is replaced.
    ///
    /// @return the selected-color property
    public ReadOnlyObjectProperty<@Nullable M3Color> selectedColorProperty() {
        return selectedColor.getReadOnlyProperty();
    }

    /// Selects the first item that renders equivalently to a color.
    ///
    /// Selection is cleared when no equivalent item exists.
    ///
    /// @param color the non-null color to match
    /// @throws NullPointerException if `color` is `null`
    public void selectColor(M3Color color) {
        Objects.requireNonNull(color, "color");
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).isEquivalentTo(color)) {
                updateSelection(index);
                return;
            }
        }
        clearSelection();
    }

    /// Clears the current selection.
    ///
    /// This method has no effect when selection is already empty.
    public void clearSelection() {
        updateSelection(-1);
    }

    /// The preferred maximum number of columns in the wrapping grid.
    ///
    /// @defaultValue `6`
    private final IntegerProperty columnCount =
            M3ColorProperties.positiveIntegerProperty(this, "columnCount", 6, this::requestLayout);

    /// Returns the preferred maximum number of columns.
    ///
    /// @return the positive column count
    public int getColumnCount() {
        return columnCount.get();
    }

    /// Sets the preferred maximum number of columns.
    ///
    /// The rendered grid may contain fewer columns when there are fewer items or when the available width is
    /// insufficient.
    ///
    /// @param columnCount the positive column count
    /// @throws IllegalArgumentException if `columnCount` is not positive
    /// @throws RuntimeException if [#columnCountProperty()] is unidirectionally bound
    public void setColumnCount(int columnCount) {
        if (columnCount <= 0) {
            throw new IllegalArgumentException("columnCount must be positive: " + columnCount);
        }
        this.columnCount.set(columnCount);
    }

    /// Returns the property containing the preferred maximum column count.
    ///
    /// A unidirectional binding must supply positive values.
    ///
    /// @return the column-count property
    public IntegerProperty columnCountProperty() {
        return columnCount;
    }

    /// The nominal swatch content size used by every picker cell.
    ///
    /// @defaultValue [M3ColorSwatchSize#MEDIUM]
    private final ObjectProperty<M3ColorSwatchSize> swatchSize = M3ColorProperties.nonNullObjectProperty(
            this,
            "swatchSize",
            M3ColorSwatchSize.MEDIUM,
            this::requestLayout
    );

    /// Returns the picker swatch size.
    ///
    /// @return the non-null swatch size
    public M3ColorSwatchSize getSwatchSize() {
        return swatchSize.get();
    }

    /// Sets the picker swatch size.
    ///
    /// @param swatchSize the non-null size
    /// @throws NullPointerException if `swatchSize` is `null`
    /// @throws RuntimeException if [#swatchSizeProperty()] is unidirectionally bound
    public void setSwatchSize(M3ColorSwatchSize swatchSize) {
        this.swatchSize.set(swatchSize);
    }

    /// Returns the property containing the picker swatch size.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the swatch-size property
    public ObjectProperty<M3ColorSwatchSize> swatchSizeProperty() {
        return swatchSize;
    }

    /// The corner treatment applied to every swatch and its interaction region.
    ///
    /// @defaultValue [M3ColorSwatchRounding#NONE]
    private final ObjectProperty<M3ColorSwatchRounding> rounding = M3ColorProperties.nonNullObjectProperty(
            this,
            "rounding",
            M3ColorSwatchRounding.NONE,
            () -> {
                updateRoundingStyle();
                requestLayout();
            }
    );

    /// Returns the picker swatch corner treatment.
    ///
    /// @return the non-null corner treatment
    public M3ColorSwatchRounding getRounding() {
        return rounding.get();
    }

    /// Sets the picker swatch corner treatment.
    ///
    /// @param rounding the non-null corner treatment
    /// @throws NullPointerException if `rounding` is `null`
    /// @throws RuntimeException if [#roundingProperty()] is unidirectionally bound
    public void setRounding(M3ColorSwatchRounding rounding) {
        this.rounding.set(rounding);
    }

    /// Returns the property containing the picker swatch corner treatment.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the corner-treatment property
    public ObjectProperty<M3ColorSwatchRounding> roundingProperty() {
        return rounding;
    }

    /// The horizontal gap between adjacent cells, in JavaFX logical pixels.
    ///
    /// @defaultValue `8.0`
    private final DoubleProperty horizontalGap =
            M3ColorProperties.nonNegativeDoubleProperty(this, "horizontalGap", 8.0, this::requestLayout);

    /// Returns the horizontal cell gap.
    ///
    /// @return the finite, non-negative gap in JavaFX logical pixels
    public double getHorizontalGap() {
        return horizontalGap.get();
    }

    /// Sets the horizontal cell gap.
    ///
    /// @param horizontalGap the finite, non-negative gap in JavaFX logical pixels
    /// @throws IllegalArgumentException if the value is negative or not finite
    /// @throws RuntimeException if [#horizontalGapProperty()] is unidirectionally bound
    public void setHorizontalGap(double horizontalGap) {
        this.horizontalGap.set(horizontalGap);
    }

    /// Returns the property containing the horizontal cell gap.
    ///
    /// A unidirectional binding must supply finite, non-negative values.
    ///
    /// @return the horizontal-gap property
    public DoubleProperty horizontalGapProperty() {
        return horizontalGap;
    }

    /// The vertical gap between adjacent cell rows, in JavaFX logical pixels.
    ///
    /// @defaultValue `8.0`
    private final DoubleProperty verticalGap =
            M3ColorProperties.nonNegativeDoubleProperty(this, "verticalGap", 8.0, this::requestLayout);

    /// Returns the vertical cell gap.
    ///
    /// @return the finite, non-negative gap in JavaFX logical pixels
    public double getVerticalGap() {
        return verticalGap.get();
    }

    /// Sets the vertical cell gap.
    ///
    /// @param verticalGap the finite, non-negative gap in JavaFX logical pixels
    /// @throws IllegalArgumentException if the value is negative or not finite
    /// @throws RuntimeException if [#verticalGapProperty()] is unidirectionally bound
    public void setVerticalGap(double verticalGap) {
        this.verticalGap.set(verticalGap);
    }

    /// Returns the property containing the vertical row gap.
    ///
    /// A unidirectional binding must supply finite, non-negative values.
    ///
    /// @return the vertical-gap property
    public DoubleProperty verticalGapProperty() {
        return verticalGap;
    }

    /// Whether user activation of the selected item clears selection.
    ///
    /// @defaultValue `false`
    private final BooleanProperty allowEmptySelection =
            new SimpleBooleanProperty(this, "allowEmptySelection");

    /// Returns whether user activation may clear selection.
    ///
    /// @return `true` when user interaction may clear selection
    public boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether user activation of the selected item clears selection.
    ///
    /// Changing this policy does not change the current selection.
    ///
    /// @param allowEmptySelection whether user interaction may clear selection
    /// @throws RuntimeException if [#allowEmptySelectionProperty()] is unidirectionally bound
    public void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the property controlling whether user activation may clear selection.
    ///
    /// @return the empty-selection property
    public BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects an item by index.
    ///
    /// Repeatedly selecting the same index has no additional effect. The [#isAllowEmptySelection()] policy applies
    /// only when a user activates an already selected swatch; it does not change this method's deterministic
    /// programmatic behavior.
    ///
    /// @param index the item index
    /// @throws IndexOutOfBoundsException if `index` is outside the item list
    public void select(int index) {
        Objects.checkIndex(index, items.size());
        updateSelection(index);
    }

    /// Creates the default visual representation of this control.
    ///
    /// @return the non-null default skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ColorSwatchPickerSkin(this);
    }

    /// Returns the user-agent stylesheet for swatch pickers.
    ///
    /// @return the swatch-picker stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-swatch-picker.css");
    }

    /// Initializes style, accessibility, and list invariants.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setFocusTraversable(false);
        updateRoundingStyle();
        items.addListener(itemsListener);
    }

    /// Applies the style class that identifies the picker cell corner treatment.
    private void updateRoundingStyle() {
        M3ControlStyles.replaceVariant(
                this,
                roundingStyleClass(getRounding()),
                roundingStyleClass(M3ColorSwatchRounding.DEFAULT),
                roundingStyleClass(M3ColorSwatchRounding.NONE),
                roundingStyleClass(M3ColorSwatchRounding.FULL)
        );
    }

    /// Returns the style class associated with a picker-cell corner treatment.
    ///
    /// @param rounding the corner treatment
    /// @return the associated style class
    private static String roundingStyleClass(M3ColorSwatchRounding rounding) {
        return switch (rounding) {
            case DEFAULT -> "m3-color-swatch-picker-rounding-default";
            case NONE -> "m3-color-swatch-picker-rounding-no";
            case FULL -> "m3-color-swatch-picker-rounding-full";
        };
    }

    /// Remaps the selected index through structural item-list changes.
    ///
    /// Insertions and removals before the selected item retain that item's selection. Removing the selected item
    /// clears selection unless the same change replaces it with one or more new items, in which case the
    /// corresponding replacement index remains selected.
    private void handleItemsChanged(ListChangeListener.Change<? extends M3Color> change) {
        int selected = getSelectedIndex();
        while (change.next()) {
            if (selected < 0) {
                continue;
            }
            if (change.wasPermutated()) {
                if (selected >= change.getFrom() && selected < change.getTo()) {
                    selected = change.getPermutation(selected);
                }
                continue;
            }
            if (change.wasUpdated() || selected < change.getFrom()) {
                continue;
            }

            int removedEnd = change.getFrom() + change.getRemovedSize();
            if (selected >= removedEnd) {
                selected += change.getAddedSize() - change.getRemovedSize();
            } else if (change.getAddedSize() > 0) {
                selected = change.getFrom()
                        + Math.min(selected - change.getFrom(), change.getAddedSize() - 1);
            } else {
                selected = -1;
            }
        }
        updateSelection(selected);
        requestLayout();
    }

    /// Updates the read-only selection properties from one normalized index.
    ///
    /// @param index the selected item index, or `-1`
    private void updateSelection(int index) {
        int normalized = index >= 0 && index < items.size() ? index : -1;
        selectedIndex.set(normalized);
        selectedColor.set(normalized < 0 ? null : items.get(normalized));
    }
}
