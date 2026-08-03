// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TreeViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a virtualized, expandable hierarchy using Material Design 3 list-row conventions.
///
/// Material Design 3 does not define a tree-view component. This extension therefore derives its visual contract
/// from Material lists: one-line rows use the Material list height, selection uses Material color roles, and hover,
/// focus, disabled, and pointer feedback use the same state vocabulary as other M3FX collection controls. Adobe
/// Spectrum 2 informs only tree-specific capabilities such as expandable hierarchy, optional item graphics,
/// highlight or checkbox selection presentation, and full-text help for truncated labels.
///
/// The inherited [TreeView] model remains authoritative. [TreeItem] owns hierarchy and expansion state, while the
/// inherited selection, focus, editing, scrolling, accessibility, and keyboard contracts remain available. A new
/// tree view uses [highlight selection][M3TreeViewSelectionStyle#HIGHLIGHT] and creates [M3TreeCell] instances.
/// Applications may replace the cell factory for richer rows. An explicit inherited `fixedCellSize` value or an
/// author stylesheet may override the default Material one-line row height.
///
/// See [Spectrum 2 TreeView](https://react-spectrum.adobe.com/TreeView).
///
/// @param <T> the value type stored by tree items
@NotNullByDefault
public final class M3TreeView<T> extends TreeView<T> {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-tree-view";

    /// The way selected tree items are presented.
    ///
    /// A direct `null` assignment restores [M3TreeViewSelectionStyle#HIGHLIGHT]. Bound values must be non-null.
    ///
    /// @defaultValue [M3TreeViewSelectionStyle#HIGHLIGHT]
    private final ObjectProperty<M3TreeViewSelectionStyle> selectionStyle =
            new SimpleObjectProperty<>(this, "selectionStyle", M3TreeViewSelectionStyle.HIGHLIGHT) {
                /// Restores the default or updates selection styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TreeViewSelectionStyle.HIGHLIGHT);
                        return;
                    }
                    updateSelectionStyle();
                }
            };

    /// Creates an empty tree view with no root item.
    public M3TreeView() {
        initialize();
    }

    /// Creates a tree view with the specified root item.
    ///
    /// A `null` root represents an empty tree and may later be replaced through the inherited root property.
    ///
    /// @param root the root item, or `null` for an empty tree
    public M3TreeView(@Nullable TreeItem<T> root) {
        super(root);
        initialize();
    }

    /// Returns the way selected tree items are presented.
    ///
    /// @return the non-null selection style
    public M3TreeViewSelectionStyle getSelectionStyle() {
        return selectionStyle.get();
    }

    /// Sets the way selected tree items are presented.
    ///
    /// Checkbox presentation is most useful with the inherited selection model configured for multiple selection.
    /// This method changes presentation only and does not change the selection mode or selected items.
    ///
    /// @param selectionStyle the selection style
    /// @throws NullPointerException if `selectionStyle` is `null`
    public void setSelectionStyle(M3TreeViewSelectionStyle selectionStyle) {
        this.selectionStyle.set(Objects.requireNonNull(selectionStyle, "selectionStyle"));
    }

    /// Returns the property containing the selection presentation.
    ///
    /// A direct `null` assignment restores [M3TreeViewSelectionStyle#HIGHLIGHT]. A unidirectional binding must
    /// supply non-null values. Changing the property does not change the inherited selection mode or selected items.
    ///
    /// @return the selection-style property
    public ObjectProperty<M3TreeViewSelectionStyle> selectionStyleProperty() {
        return selectionStyle;
    }

    /// Returns the user-agent stylesheet for Material tree views.
    ///
    /// @return the tree-view stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("tree-view.css");
    }

    /// Creates the default Material tree-view skin.
    ///
    /// @return a skin that preserves JavaFX tree behavior and styles its virtualized scrollbars
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TreeViewSkin<>(this);
    }

    /// Initializes styling, accessibility, and the default cell factory.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TREE_VIEW);
        setCellFactory(treeView -> new M3TreeCell<>());
        updateSelectionStyle();
    }

    /// Applies the style class for the current selection presentation.
    private void updateSelectionStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSelectionStyle().styleClass(),
                M3TreeViewSelectionStyle.HIGHLIGHT.styleClass(),
                M3TreeViewSelectionStyle.CHECKBOX.styleClass()
        );
        requestLayout();
    }
}
