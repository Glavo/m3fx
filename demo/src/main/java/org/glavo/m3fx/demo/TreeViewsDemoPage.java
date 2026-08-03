// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.glavo.m3fx.controls.M3TreeView;
import org.glavo.m3fx.controls.M3TreeViewSize;
import org.glavo.m3fx.controls.M3TreeViewStyle;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;
import java.util.Locale;

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
        M3TreeView<String> standard = createHierarchy(
                "demo-tree-view-standard",
                M3TreeViewStyle.STANDARD,
                false,
                false
        );
        M3TreeView<String> detached = createHierarchy(
                "demo-tree-view-detached",
                M3TreeViewStyle.DETACHED,
                true,
                false
        );
        M3TreeView<String> multiple = createHierarchy(
                "demo-tree-view-multiple",
                M3TreeViewStyle.STANDARD,
                true,
                true
        );

        return createGallery(
                createShowcaseGroup("Hierarchy", standard, detached),
                createFullWidthShowcaseGroup("Multiple Selection", multiple),
                createShowcaseGroup(
                        "Size Scale",
                        createSizeSample(M3TreeViewSize.SMALL),
                        createSizeSample(M3TreeViewSize.MEDIUM),
                        createSizeSample(M3TreeViewSize.LARGE),
                        createSizeSample(M3TreeViewSize.EXTRA_LARGE)
                )
        );
    }

    /// Creates one representative expandable project hierarchy.
    ///
    /// @param styleClass the demo style class identifying the sample role
    /// @param treeStyle the row containment style
    /// @param graphics whether items display vector graphics
    /// @param multipleSelection whether multiple selection is enabled and demonstrated
    /// @return the configured tree view
    private static M3TreeView<String> createHierarchy(
            String styleClass,
            M3TreeViewStyle treeStyle,
            boolean graphics,
            boolean multipleSelection
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
        documentation.setExpanded(true);

        M3TreeView<String> treeView = new M3TreeView<>(workspace);
        treeView.getStyleClass().addAll("demo-tree-view", styleClass);
        treeView.setTreeStyle(treeStyle);
        treeView.setPrefHeight(360.0);
        configureResponsiveWidth(treeView, 400.0);
        treeView.setMaxWidth(400.0);
        if (multipleSelection) {
            treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            treeView.getSelectionModel().selectIndices(1, 3, 6);
        } else {
            treeView.getSelectionModel().select(2);
        }
        return treeView;
    }

    /// Creates a shallow tree that demonstrates one row-size role.
    ///
    /// @param size the row-size role
    /// @return the configured size sample
    private static M3TreeView<String> createSizeSample(M3TreeViewSize size) {
        TreeItem<String> hiddenRoot = new TreeItem<>(size.name());
        hiddenRoot.getChildren().addAll(List.of(
                new TreeItem<>(size.name().replace('_', ' ') + " rows"),
                new TreeItem<>((int) size.getRowHeight() + " logical pixels")
        ));
        hiddenRoot.setExpanded(true);

        M3TreeView<String> treeView = new M3TreeView<>(hiddenRoot);
        treeView.getStyleClass().addAll(
                "demo-tree-view",
                "demo-tree-view-size-" + size.name().toLowerCase(Locale.ROOT)
        );
        treeView.setShowRoot(false);
        treeView.setSize(size);
        treeView.setPrefHeight(size.getRowHeight() * 2.0 + 2.0);
        treeView.setMaxHeight(size.getRowHeight() * 2.0 + 2.0);
        configureResponsiveWidth(treeView, 260.0);
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
