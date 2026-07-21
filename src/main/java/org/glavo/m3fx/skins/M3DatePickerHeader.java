// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/// Shared month and year navigation header for calendar date pickers.
///
/// The header keeps a fixed set of month menu items and a bounded reusable window of year menu items. This avoids
/// allocating popup content whenever the displayed month changes while still allowing the previous and next year
/// controls to traverse the complete configured date range.
@NotNullByDefault
final class M3DatePickerHeader extends HBox {
    /// The number of years exposed around the displayed year in the reusable year menu.
    private static final int YEAR_MENU_ITEM_COUNT = 11;

    /// The calendar control whose orientation and date bounds are represented.
    private final Control owner;

    /// The displayed calendar month property shared with the owning picker.
    private final ObjectProperty<YearMonth> displayedMonth;

    /// The optional inclusive lower date bound.
    private final ObjectProperty<@Nullable LocalDate> minDate;

    /// The optional inclusive upper date bound.
    private final ObjectProperty<@Nullable LocalDate> maxDate;

    /// The button that moves to the previous month.
    private final M3IconButton previousMonthButton = createNavigationButton();

    /// The button that opens the month selection menu.
    private final M3MenuButton monthButton = createMenuButton();

    /// The button that moves to the next month.
    private final M3IconButton nextMonthButton = createNavigationButton();

    /// The button that moves to the previous year.
    private final M3IconButton previousYearButton = createNavigationButton();

    /// The button that opens the year selection menu.
    private final M3MenuButton yearButton = createMenuButton();

    /// The button that moves to the next year.
    private final M3IconButton nextYearButton = createNavigationButton();

    /// The persistent month menu items in calendar order.
    private final List<M3MenuItem> monthItems = new ArrayList<>(Month.values().length);

    /// The persistent year menu items centered around the displayed year.
    private final List<M3MenuItem> yearItems = new ArrayList<>(YEAR_MENU_ITEM_COUNT);

    /// Refreshes text, menu values, disabled states, and direction-dependent icons.
    private final InvalidationListener stateInvalidation = observable -> refresh();

    /// Creates a calendar header bound to picker state.
    ///
    /// @param owner          the picker control represented by this header
    /// @param displayedMonth the displayed month property
    /// @param minDate        the optional inclusive lower date bound
    /// @param maxDate        the optional inclusive upper date bound
    M3DatePickerHeader(
            Control owner,
            ObjectProperty<YearMonth> displayedMonth,
            ObjectProperty<@Nullable LocalDate> minDate,
            ObjectProperty<@Nullable LocalDate> maxDate
    ) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.displayedMonth = Objects.requireNonNull(displayedMonth, "displayedMonth");
        this.minDate = Objects.requireNonNull(minDate, "minDate");
        this.maxDate = Objects.requireNonNull(maxDate, "maxDate");

        getStyleClass().add(M3DatePicker.HEADER_STYLE_CLASS);
        nodeOrientationProperty().bind(owner.effectiveNodeOrientationProperty());
        setAlignment(Pos.CENTER_LEFT);

        HBox monthSection = new HBox(previousMonthButton, monthButton, nextMonthButton);
        monthSection.getStyleClass().add(M3DatePicker.HEADER_SECTION_STYLE_CLASS);
        monthSection.setAlignment(Pos.CENTER);

        HBox yearSection = new HBox(previousYearButton, yearButton, nextYearButton);
        yearSection.getStyleClass().add(M3DatePicker.HEADER_SECTION_STYLE_CLASS);
        yearSection.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(monthSection, spacer, yearSection);

        monthButton.getStyleClass().add(M3DatePicker.MONTH_MENU_BUTTON_STYLE_CLASS);
        yearButton.getStyleClass().add(M3DatePicker.YEAR_MENU_BUTTON_STYLE_CLASS);
        initializeMenus();

        previousMonthButton.setOnAction(event -> displayedMonth.set(displayedMonth.get().minusMonths(1)));
        nextMonthButton.setOnAction(event -> displayedMonth.set(displayedMonth.get().plusMonths(1)));
        previousYearButton.setOnAction(event -> displayedMonth.set(displayedMonth.get().minusYears(1)));
        nextYearButton.setOnAction(event -> displayedMonth.set(displayedMonth.get().plusYears(1)));

        displayedMonth.addListener(stateInvalidation);
        minDate.addListener(stateInvalidation);
        maxDate.addListener(stateInvalidation);
        owner.effectiveNodeOrientationProperty().addListener(stateInvalidation);
        refresh();
    }

    /// Releases bindings and listeners owned by this header.
    void dispose() {
        displayedMonth.removeListener(stateInvalidation);
        minDate.removeListener(stateInvalidation);
        maxDate.removeListener(stateInvalidation);
        owner.effectiveNodeOrientationProperty().removeListener(stateInvalidation);
        nodeOrientationProperty().unbind();
    }

    /// Synchronizes localized labels, menu entries, navigation availability, and logical arrow direction.
    void refresh() {
        YearMonth month = displayedMonth.get();
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        @Nullable LocalDate minimum = minDate.get();
        @Nullable LocalDate maximum = maxDate.get();

        monthButton.setText(month.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, locale));
        yearButton.setText(Integer.toString(month.getYear()));

        for (int index = 0; index < monthItems.size(); index++) {
            Month itemMonth = Month.of(index + 1);
            M3MenuItem item = monthItems.get(index);
            item.setHeadlineText(itemMonth.getDisplayName(TextStyle.FULL_STANDALONE, locale));
            item.setSelected(itemMonth == month.getMonth());
            item.setDisable(monthFallsOutsideBounds(YearMonth.of(month.getYear(), itemMonth), minimum, maximum));
        }

        int minimumYear = minimum == null ? Integer.MIN_VALUE : minimum.getYear();
        int maximumYear = maximum == null ? Integer.MAX_VALUE : maximum.getYear();
        long firstYear = (long) month.getYear() - YEAR_MENU_ITEM_COUNT / 2L;
        if (firstYear < minimumYear) {
            firstYear = minimumYear;
        }
        long latestFirstYear = (long) maximumYear - YEAR_MENU_ITEM_COUNT + 1L;
        if (firstYear > latestFirstYear) {
            firstYear = latestFirstYear;
        }

        for (int index = 0; index < yearItems.size(); index++) {
            long candidate = firstYear + index;
            M3MenuItem item = yearItems.get(index);
            if (candidate < minimumYear || candidate > maximumYear
                    || candidate < Year.MIN_VALUE
                    || candidate > Year.MAX_VALUE) {
                item.setVisible(false);
                item.setManaged(false);
                item.setSelected(false);
                item.setUserData(null);
                continue;
            }

            int year = (int) candidate;
            item.setVisible(true);
            item.setManaged(true);
            item.setHeadlineText(Integer.toString(year));
            item.setUserData(year);
            item.setSelected(year == month.getYear());
            item.setDisable(yearFallsOutsideBounds(year, minimum, maximum));
        }

        boolean hasPreviousMonth = month.getYear() > Year.MIN_VALUE || month.getMonthValue() > 1;
        boolean hasNextMonth = month.getYear() < Year.MAX_VALUE || month.getMonthValue() < 12;
        boolean hasPreviousYear = month.getYear() > Year.MIN_VALUE;
        boolean hasNextYear = month.getYear() < Year.MAX_VALUE;
        previousMonthButton.setDisable(!hasPreviousMonth
                || monthFallsOutsideBounds(month.minusMonths(1), minimum, maximum));
        nextMonthButton.setDisable(!hasNextMonth
                || monthFallsOutsideBounds(month.plusMonths(1), minimum, maximum));
        previousYearButton.setDisable(!hasPreviousYear
                || monthFallsOutsideBounds(month.minusYears(1), minimum, maximum));
        nextYearButton.setDisable(!hasNextYear
                || monthFallsOutsideBounds(month.plusYears(1), minimum, maximum));

        boolean rightToLeft = M3NodeLayout.isRightToLeft(owner);
        setNavigationIcon(previousMonthButton, rightToLeft
                ? M3InternalIcon.Glyph.CHEVRON_RIGHT
                : M3InternalIcon.Glyph.CHEVRON_LEFT);
        setNavigationIcon(nextMonthButton, rightToLeft
                ? M3InternalIcon.Glyph.CHEVRON_LEFT
                : M3InternalIcon.Glyph.CHEVRON_RIGHT);
        setNavigationIcon(previousYearButton, rightToLeft
                ? M3InternalIcon.Glyph.CHEVRON_RIGHT
                : M3InternalIcon.Glyph.CHEVRON_LEFT);
        setNavigationIcon(nextYearButton, rightToLeft
                ? M3InternalIcon.Glyph.CHEVRON_LEFT
                : M3InternalIcon.Glyph.CHEVRON_RIGHT);
    }

    /// Creates persistent month and year menu items and connects their actions to the displayed month.
    private void initializeMenus() {
        monthButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
        monthButton.getMenu().setAllowEmptySelection(false);
        for (Month month : Month.values()) {
            M3MenuItem item = new M3MenuItem();
            item.setUserData(month);
            item.setOnAction(event -> {
                if (item.getUserData() instanceof Month selectedMonth) {
                    displayedMonth.set(displayedMonth.get().withMonth(selectedMonth.getValue()));
                }
            });
            monthItems.add(item);
        }
        monthButton.getItems().setAll(monthItems);

        yearButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);
        yearButton.getMenu().setAllowEmptySelection(false);
        for (int index = 0; index < YEAR_MENU_ITEM_COUNT; index++) {
            M3MenuItem item = new M3MenuItem();
            item.setOnAction(event -> {
                if (item.getUserData() instanceof Integer selectedYear) {
                    displayedMonth.set(displayedMonth.get().withYear(selectedYear));
                }
            });
            yearItems.add(item);
        }
        yearButton.getItems().setAll(yearItems);
    }

    /// Returns whether every date in a month falls outside the optional inclusive bounds.
    private static boolean monthFallsOutsideBounds(
            YearMonth month,
            @Nullable LocalDate minimum,
            @Nullable LocalDate maximum
    ) {
        return (minimum != null && month.atEndOfMonth().isBefore(minimum))
                || (maximum != null && month.atDay(1).isAfter(maximum));
    }

    /// Returns whether every date in a year falls outside the optional inclusive bounds.
    private static boolean yearFallsOutsideBounds(
            int year,
            @Nullable LocalDate minimum,
            @Nullable LocalDate maximum
    ) {
        return (minimum != null && year < minimum.getYear())
                || (maximum != null && year > maximum.getYear());
    }

    /// Creates a text-style menu button with the standard trailing disclosure icon.
    private static M3MenuButton createMenuButton() {
        M3MenuButton button = new M3MenuButton();
        button.getStyleClass().add(M3DatePicker.MENU_BUTTON_STYLE_CLASS);
        button.setVariant(M3ButtonVariant.TEXT);
        button.setGraphic(new M3InternalIcon(
                M3InternalIcon.Glyph.EXPAND_MORE,
                M3InternalIcon.ColorRole.ON_SURFACE_VARIANT,
                18.0
        ));
        button.setContentDisplay(ContentDisplay.RIGHT);
        button.setGraphicTextGap(4.0);
        return button;
    }

    /// Creates an icon button used for month or year navigation.
    private static M3IconButton createNavigationButton() {
        M3IconButton button = new M3IconButton(new M3InternalIcon(
                M3InternalIcon.Glyph.CHEVRON_LEFT,
                M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
        ));
        button.getStyleClass().add(M3DatePicker.NAVIGATION_BUTTON_STYLE_CLASS);
        return button;
    }

    /// Updates the glyph rendered by a navigation button.
    private static void setNavigationIcon(M3IconButton button, M3InternalIcon.Glyph glyph) {
        if (button.getGraphic() instanceof M3InternalIcon icon) {
            icon.setGlyph(glyph);
        }
    }
}
