// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TableViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a virtualized data table using Material Design 3 list and surface conventions.
///
/// Material Design 3 does not define a data-table component. This extension therefore derives its visual contract
/// from Material lists and surfaces: rows use the one-line list height, selection uses Material color roles, and
/// hover, focus, disabled, and pointer feedback use the same state vocabulary as other M3FX collection controls.
/// Adobe Spectrum 2 informs only table-specific capabilities such as scanning, sorting, comparing, and resizing
/// tabular data.
///
/// The inherited [TableView] model remains authoritative. Columns own cell values, sorting, sizing, and editing;
/// the inherited selection and focus models own row or cell selection; and JavaFX retains virtualization, keyboard
/// navigation, accessibility, and scrolling behavior. A new table creates [M3TableRow] instances. Applications may
/// replace the inherited row factory for richer rows; custom rows must extend [M3TableRow] to retain the Material
/// row state layer and ripple. An explicit inherited `fixedCellSize` value or an author stylesheet may override the
/// default Material one-line row height.
///
/// See [Spectrum 2 TableView](https://react-spectrum.adobe.com/TableView).
///
/// @param <T> the row-item type
@NotNullByDefault
public final class M3TableView<T> extends TableView<T> {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-table-view";

    /// Creates an empty table with a Material row factory.
    public M3TableView() {
        initialize();
    }

    /// Creates a table backed by the specified observable item list.
    ///
    /// The list instance is retained by the inherited items property. Subsequent list mutations are reflected by
    /// the table. Replacing the inherited items property later remains supported.
    ///
    /// @param items the observable row items
    /// @throws NullPointerException if `items` is `null`
    public M3TableView(ObservableList<T> items) {
        super(Objects.requireNonNull(items, "items"));
        initialize();
    }

    /// Returns the user-agent stylesheet for Material tables.
    ///
    /// @return the table-view stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("table-view.css");
    }

    /// Creates the default Material table-view skin.
    ///
    /// @return a skin that preserves JavaFX table behavior and styles its virtualized rows and scrollbars
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TableViewSkin<>(this);
    }

    /// Initializes styling, accessibility, the default row factory, and traversal focus restoration.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TABLE_VIEW);
        setFocusTraversable(true);
        setRowFactory(tableView -> new M3TableRow<>());
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                ensureTraversalFocus();
            }
        });
    }

    /// Ensures keyboard traversal into a non-empty table has one logical focused row without changing selection.
    private void ensureTraversalFocus() {
        @Nullable TableViewFocusModel<T> focusModel = getFocusModel();
        @Nullable ObservableList<T> items = getItems();
        int itemCount = items == null ? 0 : items.size();
        if (focusModel == null || itemCount <= 0) {
            return;
        }

        int index = focusModel.getFocusedIndex();
        if (index >= 0 && index < itemCount) {
            return;
        }
        @Nullable TableViewSelectionModel<T> selectionModel = getSelectionModel();
        int selectedIndex = selectionModel == null ? -1 : selectionModel.getSelectedIndex();
        focusModel.focus(selectedIndex >= 0 && selectedIndex < itemCount ? selectedIndex : 0);
    }
}
