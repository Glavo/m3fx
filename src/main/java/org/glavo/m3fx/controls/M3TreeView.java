// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a virtualized, expandable hierarchy with Material Design 3 styling.
///
/// Material Design 3 does not define a tree-view component. This M3FX extension adapts the hierarchy and density
/// options of Adobe Spectrum tree views to Material color, shape, type, and interaction roles. The inherited
/// [TreeView] model remains authoritative: [TreeItem] owns hierarchy and expansion state, and the inherited focus,
/// selection, editing, scrolling, accessibility, and keyboard contracts are unchanged.
///
/// A new tree view uses the [medium][M3TreeViewSize#MEDIUM] size, the
/// [standard][M3TreeViewStyle#STANDARD] containment style, and [M3TreeCell] as its cell factory. Applications may
/// replace the inherited cell factory to render richer rows. The inherited selection model initially uses single
/// selection and may be changed to multiple selection through [javafx.scene.control.MultipleSelectionModel]. An
/// explicit inherited `fixedCellSize` value or an author stylesheet may override the nominal size-role row height.
///
/// See [Spectrum tree views](https://spectrum.adobe.com/page/tree-view/).
///
/// @param <T> the value type stored by tree items
@NotNullByDefault
public final class M3TreeView<T> extends TreeView<T> {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-tree-view";

    /// The nominal row size.
    ///
    /// A direct `null` assignment restores [M3TreeViewSize#MEDIUM]. Bound values must be non-null.
    ///
    /// @defaultValue [M3TreeViewSize#MEDIUM]
    private final ObjectProperty<M3TreeViewSize> size =
            new SimpleObjectProperty<>(this, "size", M3TreeViewSize.MEDIUM) {
                /// Restores the default or updates size styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TreeViewSize.MEDIUM);
                        return;
                    }
                    updateSizeStyle();
                }
            };

    /// The row containment style.
    ///
    /// A direct `null` assignment restores [M3TreeViewStyle#STANDARD]. Bound values must be non-null.
    ///
    /// @defaultValue [M3TreeViewStyle#STANDARD]
    private final ObjectProperty<M3TreeViewStyle> treeStyle =
            new SimpleObjectProperty<>(this, "treeStyle", M3TreeViewStyle.STANDARD) {
                /// Restores the default or updates containment styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TreeViewStyle.STANDARD);
                        return;
                    }
                    updateTreeStyle();
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

    /// Returns the nominal row size.
    ///
    /// @return the non-null size role
    public M3TreeViewSize getSize() {
        return size.get();
    }

    /// Sets the nominal row size.
    ///
    /// @param size the size role
    /// @throws NullPointerException if `size` is `null`
    public void setSize(M3TreeViewSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the property containing the nominal row size.
    ///
    /// A direct `null` assignment restores [M3TreeViewSize#MEDIUM]. A unidirectional binding must supply non-null
    /// values.
    ///
    /// @return the size property
    public ObjectProperty<M3TreeViewSize> sizeProperty() {
        return size;
    }

    /// Returns the row containment style.
    ///
    /// @return the non-null containment style
    public M3TreeViewStyle getTreeStyle() {
        return treeStyle.get();
    }

    /// Sets the row containment style.
    ///
    /// @param treeStyle the containment style
    /// @throws NullPointerException if `treeStyle` is `null`
    public void setTreeStyle(M3TreeViewStyle treeStyle) {
        this.treeStyle.set(Objects.requireNonNull(treeStyle, "treeStyle"));
    }

    /// Returns the property containing the row containment style.
    ///
    /// A direct `null` assignment restores [M3TreeViewStyle#STANDARD]. A unidirectional binding must supply non-null
    /// values.
    ///
    /// @return the containment-style property
    public ObjectProperty<M3TreeViewStyle> treeStyleProperty() {
        return treeStyle;
    }

    /// Returns the user-agent stylesheet for Material tree views.
    ///
    /// @return the tree-view stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("tree-view.css");
    }

    /// Initializes styling, accessibility, and the default cell factory.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TREE_VIEW);
        setCellFactory(treeView -> new M3TreeCell<>());
        updateSizeStyle();
        updateTreeStyle();
    }

    /// Applies the style class for the current size role.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().styleClass(),
                M3TreeViewSize.SMALL.styleClass(),
                M3TreeViewSize.MEDIUM.styleClass(),
                M3TreeViewSize.LARGE.styleClass(),
                M3TreeViewSize.EXTRA_LARGE.styleClass()
        );
        requestLayout();
    }

    /// Applies the style class for the current containment style.
    private void updateTreeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getTreeStyle().styleClass(),
                M3TreeViewStyle.STANDARD.styleClass(),
                M3TreeViewStyle.DETACHED.styleClass()
        );
        requestLayout();
    }
}
