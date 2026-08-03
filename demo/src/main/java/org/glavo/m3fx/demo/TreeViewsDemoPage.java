// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.glavo.m3fx.controls.M3TreeView;
import org.glavo.m3fx.controls.M3TreeViewSelectionStyle;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// Builds the Tree Views extension showcase page.
@NotNullByDefault
final class TreeViewsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TreeViewsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the tree-view extension page.
    ///
    /// @return the complete tree-view showcase
    Node createContent() {
        M3TreeView<String> hierarchy = createHierarchy(
                "demo-tree-view-highlight",
                M3TreeViewSelectionStyle.HIGHLIGHT,
                true,
                false,
                true
        );
        M3TreeView<String> checkboxSelection = createHierarchy(
                "demo-tree-view-checkbox",
                M3TreeViewSelectionStyle.CHECKBOX,
                true,
                true,
                true
        );
        M3TreeView<String> hiddenRoot = createHierarchy(
                "demo-tree-view-hidden-root",
                M3TreeViewSelectionStyle.HIGHLIGHT,
                false,
                false,
                false
        );

        return createGallery(
                createFullWidthShowcaseGroup("Material Hierarchy", hierarchy),
                createFullWidthShowcaseGroup("Checkbox Selection", checkboxSelection),
                createFullWidthShowcaseGroup("Hidden Root", hiddenRoot)
        );
    }

    /// Creates one representative expandable project hierarchy.
    ///
    /// @param styleClass the demo style class identifying the sample role
    /// @param selectionStyle the selected-item presentation
    /// @param graphics whether items display vector graphics
    /// @param multipleSelection whether multiple selection is enabled and demonstrated
    /// @param showRoot whether the root is presented as a row
    /// @return the configured tree view
    private static M3TreeView<String> createHierarchy(
            String styleClass,
            M3TreeViewSelectionStyle selectionStyle,
            boolean graphics,
            boolean multipleSelection,
            boolean showRoot
    ) {
        TreeItem<String> workspace = treeItem("M3FX workspace", graphics, "work");
        TreeItem<String> sources = treeItem("Source packages", graphics, "folder");
        sources.getChildren().addAll(List.of(
                treeItem("Controls", graphics, "task"),
                treeItem("Layouts", graphics, "dashboard"),
                treeItem("Themes", graphics, "palette")
        ));
        TreeItem<String> documentation = treeItem("Documentation", graphics, "folder");
        documentation.getChildren().addAll(List.of(
                treeItem("API contracts", graphics, "text"),
                treeItem("Design guidance", graphics, "bookmark")
        ));
        workspace.getChildren().addAll(List.of(
                treeItem(
                        "A deliberately long generated-resources directory whose complete name appears in a tooltip",
                        graphics,
                        "settings"
                ),
                sources,
                documentation
        ));
        workspace.setExpanded(true);
        sources.setExpanded(true);

        M3TreeView<String> treeView = new M3TreeView<>(workspace);
        treeView.getStyleClass().addAll("demo-tree-view", styleClass);
        treeView.setSelectionStyle(selectionStyle);
        treeView.setShowRoot(showRoot);
        treeView.setPrefHeight(showRoot ? 392.0 : 336.0);
        treeView.setMaxHeight(showRoot ? 392.0 : 336.0);
        configureResponsiveWidth(treeView, 560.0);
        treeView.setMaxWidth(560.0);
        if (multipleSelection) {
            treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            treeView.getSelectionModel().selectIndices(1, 3, 6);
        } else {
            treeView.getSelectionModel().select(showRoot ? 2 : 1);
        }
        return treeView;
    }

    /// Creates a tree item with an optional demo vector graphic.
    ///
    /// @param text the item text
    /// @param graphic whether to create a graphic
    /// @param iconName the demo icon name used when graphics are enabled
    /// @return the tree item
    private static TreeItem<String> treeItem(String text, boolean graphic, String iconName) {
        return new TreeItem<>(
                text,
                graphic ? createIconViewport(DemoIcons.onSurfaceVariant(iconName), "demo-tree-view-icon") : null
        );
    }
}
