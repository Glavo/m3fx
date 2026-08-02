// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconButtonWidth;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;
import java.util.Objects;

/// Supplies reusable layout, icon, and interaction builders to demo pages.
///
/// Page classes inherit these helpers to keep sample construction consistent without depending on the application
/// shell. Operations that require the active scene are delegated through [DemoPageContext].
@NotNullByDefault
abstract class DemoPageSupport {
    /// The fixed icon viewport style used by interactive SVG icon samples.
    protected static final String DEMO_VECTOR_ICON_VIEWPORT_STYLE_CLASS = "demo-vector-icon-viewport";

    /// The authored viewport used by the demo's compact SVG path set.
    protected static final Rectangle2D DEMO_ICON_VIEW_BOX = new Rectangle2D(0.0, 0.0, 24.0, 24.0);

    /// Application-level actions available to interactive page samples.
    protected final DemoPageContext context;

    /// Creates a shared page builder.
    ///
    /// @param context the application-level actions available to interactive samples
    DemoPageSupport(DemoPageContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /// Creates a page gallery.
    protected static VBox createGallery(Node... groups) {
        VBox gallery = new VBox(18.0);
        gallery.getStyleClass().add("demo-gallery");
        gallery.setMinWidth(0.0);
        gallery.setMaxWidth(Double.MAX_VALUE);
        gallery.getChildren().addAll(groups);
        return gallery;
    }

    /// Creates one showcase group.
    protected static VBox createShowcaseGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");
        label.setMinWidth(0.0);
        label.setMaxWidth(Double.MAX_VALUE);

        FlowPane flow = new FlowPane(16.0, 16.0);
        flow.getStyleClass().add("demo-flow");
        flow.setAlignment(Pos.CENTER_LEFT);
        flow.setMinWidth(0.0);
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.getChildren().addAll(nodes);

        VBox group = new VBox(10.0, label, flow);
        group.getStyleClass().add("demo-showcase-group");
        group.setMinWidth(0.0);
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /// Creates one showcase group whose samples use the full content width.
    protected static VBox createFullWidthShowcaseGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");
        label.setMinWidth(0.0);
        label.setMaxWidth(Double.MAX_VALUE);

        VBox stack = new VBox(16.0);
        stack.getStyleClass().add("demo-stacked-flow");
        stack.setFillWidth(true);
        stack.setMinWidth(0.0);
        stack.setMaxWidth(Double.MAX_VALUE);
        stack.getChildren().addAll(nodes);

        VBox group = new VBox(10.0, label, stack);
        group.getStyleClass().add("demo-showcase-group");
        group.setMinWidth(0.0);
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /// Creates a split button configured with the requested variant.
    protected M3SplitButton createSplitButton(String text, M3ButtonVariant variant) {
        M3SplitButton splitButton = new M3SplitButton(text);
        splitButton.getItems().addAll(
                new M3MenuItem("Duplicate"),
                new M3MenuItem("Move"),
                new M3MenuItem("Delete")
        );
        splitButton.setVariant(variant);
        splitButton.setOnAction(event -> context.showSnackbar("Theme-aware snackbar"));
        return splitButton;
    }

    /// Creates a text field for the page gallery.
    protected static M3TextField createTextField(
            String prompt,
            String text,
            M3TextInputVariant variant,
            boolean disabled
    ) {
        M3TextField textField = new M3TextField(text);
        textField.setVariant(variant);
        textField.setPromptText(prompt);
        textField.setDisable(disabled);
        return configureResponsiveWidth(textField, 280.0);
    }

    /// Creates a text input layout for the page gallery.
    protected static M3TextInputLayout createTextInputLayout(TextInputControl input, String supportingText) {
        M3TextInputLayout layout = new M3TextInputLayout(input);
        layout.setSupportingText(supportingText);
        layout.setLabelText(input.getPromptText());
        input.setPromptText("");
        configureResponsiveWidth(layout, input.getPrefWidth());
        if (input.isDisabled()) {
            layout.setDisable(true);
        }
        return layout;
    }

    /// Returns the demo icon name that best matches a list row title.
    protected static String overviewIconName(String title) {
        String normalized = title.toLowerCase(Locale.ROOT);
        if (normalized.contains("app bar") || normalized.contains("toolbar")) {
            return "menu";
        } else if (normalized.contains("button") || normalized.contains("fab")) {
            return "add";
        } else if (normalized.contains("input")
                || normalized.contains("text")
                || normalized.contains("form")
                || normalized.contains("typography")) {
            return "text";
        } else if (normalized.contains("selection")
                || normalized.contains("checkbox")
                || normalized.contains("chip")
                || normalized.contains("radio")
                || normalized.contains("segment")
                || normalized.contains("switch")) {
            return "check";
        } else if (normalized.contains("navigation")) {
            return "home";
        } else if (normalized.contains("loading") || normalized.contains("progress")) {
            return "schedule";
        } else if (normalized.contains("date") || normalized.contains("time")) {
            return "calendar";
        } else if (normalized.contains("dialog") || normalized.contains("sheet")) {
            return "info";
        } else if (normalized.contains("list")
                || normalized.contains("surface")
                || normalized.contains("badge")) {
            return "label";
        } else if (normalized.contains("card")) {
            return "dashboard";
        } else if (normalized.contains("carousel")) {
            return "image";
        } else if (normalized.contains("slider")) {
            return "tune";
        } else if (normalized.contains("color")) {
            return "palette";
        } else if (normalized.contains("drop")) {
            return "upload";
        } else if (normalized.contains("snackbar")
                || normalized.contains("banner")
                || normalized.contains("tooltip")) {
            return "info";
        } else if (normalized.contains("menu")) {
            return "menu";
        } else if (normalized.contains("search")) {
            return "search";
        } else if (normalized.contains("profile") || normalized.contains("avatar")) {
            return "person";
        } else if (normalized.contains("settings")) {
            return "settings";
        } else if (normalized.contains("motion")) {
            return "move";
        } else if (normalized.contains("icon")) {
            return "star";
        } else if (normalized.contains("scrim")) {
            return "visibility";
        }
        return "bookmark";
    }

    /// Creates a button group sample with initial buttons.
    protected static M3ButtonGroup createButtonGroup(ButtonBase... buttons) {
        M3ButtonGroup group = new M3ButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a segmented button group sample with initial buttons.
    protected static M3SegmentedButtonGroup createSegmentedButtonGroup(M3SegmentedButton... buttons) {
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Configures a resizable sample to use a preferred width without forcing that width in narrow page panes.
    ///
    /// The returned region can shrink to the width assigned by its containing showcase group and may grow when the
    /// group allocates additional width. Callers retain ownership of any more specific height constraint.
    ///
    /// @param sample         the sample region to configure
    /// @param preferredWidth the preferred width in logical pixels
    /// @param <T>            the concrete sample type
    /// @return the configured sample
    /// @throws NullPointerException     if `sample` is `null`
    /// @throws IllegalArgumentException if `preferredWidth` is negative or not finite
    protected static <T extends Region> T configureResponsiveWidth(T sample, double preferredWidth) {
        Objects.requireNonNull(sample, "sample");
        if (!Double.isFinite(preferredWidth) || preferredWidth < 0.0) {
            throw new IllegalArgumentException("preferredWidth must be finite and non-negative: " + preferredWidth);
        }
        sample.setMinWidth(0.0);
        sample.setPrefWidth(preferredWidth);
        sample.setMaxWidth(Double.MAX_VALUE);
        return sample;
    }

    /// Creates an action row that wraps controls instead of forcing a page-wide horizontal overflow.
    ///
    /// @param nodes the controls in display order
    /// @return the responsive action row
    /// @throws NullPointerException if `nodes` or an element is `null`
    protected static FlowPane createResponsiveActionRow(Node... nodes) {
        Objects.requireNonNull(nodes, "nodes");
        FlowPane row = new FlowPane(12.0, 12.0);
        row.getStyleClass().add("demo-action-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinWidth(0.0);
        row.setMaxWidth(Double.MAX_VALUE);
        for (Node node : nodes) {
            row.getChildren().add(Objects.requireNonNull(node, "nodes element"));
        }
        return row;
    }

    /// Creates an icon toggle button group sample with initial buttons.
    protected static M3IconToggleButtonGroup createIconToggleButtonGroup(M3IconToggleButton... buttons) {
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates the shared Day, Week, and Month segmented button sample.
    protected static M3SegmentedButtonGroup createDayWeekMonthSegmentedGroup() {
        M3SegmentedButton firstButton = new M3SegmentedButton("Day");
        M3SegmentedButton secondButton = new M3SegmentedButton("Week");
        secondButton.setSelected(true);
        M3SegmentedButton thirdButton = new M3SegmentedButton("Month");
        return createSegmentedButtonGroup(firstButton, secondButton, thirdButton);
    }

    /// Creates the shared app bar preview container.
    protected static VBox createAppBarPreview() {
        VBox preview = new VBox();
        preview.getStyleClass().add("demo-app-bar-preview");
        preview.setFillWidth(true);
        preview.setMinWidth(0.0);
        preview.setPrefWidth(760.0);
        preview.setMaxWidth(Double.MAX_VALUE);
        return preview;
    }

    /// Creates a sample navigation item.
    protected static M3NavigationItem createNavigationItem(String text, String iconName) {
        return new M3NavigationItem(text, createNavigationIcon(iconName));
    }

    /// Creates a sample navigation icon.
    protected static Node createNavigationIcon(String iconName) {
        StackPane icon = createSurfaceVariantIcon(iconName);
        icon.getStyleClass().add("demo-navigation-icon");
        return icon;
    }

    /// Creates the sample icon button.
    protected static M3IconButton createIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.primary(iconName));
        return new M3IconButton(icon);
    }

    /// Creates a sample icon button for trailing app bar action slots.
    protected static M3IconButton createTrailingAppBarIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant(iconName), "demo-app-bar-icon");
        M3IconButton button = new M3IconButton(icon);
        button.setAccessibleText(appBarIconAccessibleText(iconName));
        return button;
    }

    /// Returns the accessible action text used by app bar icon buttons.
    protected static String appBarIconAccessibleText(String iconName) {
        return switch (iconName) {
            case "add" -> "Add";
            case "back" -> "Back";
            case "favorite" -> "Favorites";
            case "menu" -> "Menu";
            case "more" -> "More options";
            case "search" -> "Search";
            default -> throw new IllegalArgumentException("Unknown app bar icon: " + iconName);
        };
    }

    /// Creates the sample toggle icon button.
    protected static M3IconToggleButton createIconToggleButton(
            String iconName,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant(iconName));
        M3IconToggleButton button = new M3IconToggleButton(icon);
        button.setVariant(variant);
        button.setSelected(selected);
        return button;
    }

    /// Creates a sample single-selection toggle icon button group.
    protected static M3IconToggleButtonGroup createIconToggleGroup(
            M3IconToggleButtonVariant variant,
            String first,
            String second,
            String third,
            String... rest
    ) {
        M3IconToggleButtonGroup group = createIconToggleButtonGroup(
                createIconToggleButton(first, variant, false),
                createIconToggleButton(second, variant, false),
                createIconToggleButton(third, variant, false)
        );
        for (String iconName : rest) {
            group.getItems().add(createIconToggleButton(iconName, variant, false));
        }
        group.setAllowEmptySelection(false);
        group.selectIndex(0);
        return group;
    }

    /// Creates a sample floating action button.
    protected static M3FloatingActionButton createFab(
            String iconName,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        Node icon = createIconViewport(DemoIcons.fab(iconName), switch (size) {
            case SMALL, REGULAR -> 24.0;
            case MEDIUM -> 28.0;
            case LARGE -> 36.0;
        });
        M3FloatingActionButton button = new M3FloatingActionButton(icon);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a fixed viewport for an on-surface-variant icon slot.
    protected static StackPane createSurfaceVariantIcon(String iconName) {
        return createIconViewport(DemoIcons.onSurfaceVariant(iconName));
    }

    /// Creates a fixed viewport for an error-colored icon slot.
    protected static StackPane createWarningIcon() {
        return createIconViewport(DemoIcons.error("warning"));
    }

    /// Wraps a demo SVG icon in a stable 24 dp viewport.
    protected static StackPane createIconViewport(Node icon, String... styleClasses) {
        return createIconViewport(icon, defaultIconGlyphSize(M3IconSize.MEDIUM), styleClasses);
    }

    /// Creates a floating action button menu sample.
    protected M3FabMenu createFabMenu() {
        return createFabMenu(
                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                M3FloatingActionButtonVariant.PRIMARY_CONTAINER
        );
    }

    /// Creates a sample standalone icon.
    protected static M3SVGIcon createDemoIcon(String iconName, M3IconSize size, M3IconVariant variant) {
        M3SVGIcon icon = new M3SVGIcon(DemoIcons.path(iconName), DEMO_ICON_VIEW_BOX);
        icon.setSize(size);
        icon.setVariant(variant);
        icon.getProperties().put(DemoIcons.ICON_NAME_PROPERTY, iconName);
        icon.getStyleClass().add("demo-sample-icon");
        icon.setMouseTransparent(true);
        return icon;
    }

    /// Returns the default demo glyph size for an icon size role.
    protected static double defaultIconGlyphSize(M3IconSize size) {
        return switch (size) {
            case SMALL -> 18.0;
            case MEDIUM -> 24.0;
            case LARGE -> 32.0;
            case EXTRA_LARGE -> 40.0;
        };
    }

    /// Returns the default demo glyph size for an icon button size role.
    protected static double defaultIconButtonGlyphSize(M3ButtonSize size) {
        return switch (size) {
            case EXTRA_SMALL -> 20.0;
            case SMALL, MEDIUM -> 24.0;
            case LARGE -> 32.0;
            case EXTRA_LARGE -> 40.0;
        };
    }

    /// Creates a sample extended floating action button with a variant.
    protected static M3FloatingActionButton createExtendedFab(
            String text,
            String iconName,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(
                text,
                createIconViewport(DemoIcons.fab(iconName), switch (size) {
                    case SMALL, REGULAR -> 24.0;
                    case MEDIUM -> 28.0;
                    case LARGE -> 36.0;
                })
        );
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates sample sheet content.
    protected static VBox createSheetContent() {
        M3ListItem first = new M3ListItem("Overview");
        first.setSupportingText("Primary sheet content");
        first.setLeading(createNavigationIcon("info"));
        M3ListItem second = new M3ListItem("Activity");
        second.setSupportingText("Recent updates and state");
        second.setLeading(createNavigationIcon("schedule"));
        M3ListItem third = new M3ListItem("Settings");
        third.setLeading(createNavigationIcon("settings"));

        VBox content = new VBox(first, second, third);
        content.getStyleClass().add("demo-sheet-content");
        return content;
    }

    /// Creates a sample icon button with Material Expressive sizing roles.
    protected static M3IconButton createIconButton(
            String iconName,
            M3ButtonSize size,
            M3IconButtonWidth widthRole,
            M3ButtonShape shape
    ) {
        Node icon = createIconViewport(DemoIcons.primary(iconName), defaultIconButtonGlyphSize(size));
        M3IconButton button = new M3IconButton(icon);
        button.setSize(size);
        button.setWidthRole(widthRole);
        button.setButtonShape(shape);
        return button;
    }

    /// Wraps a demo SVG icon in a stable viewport with the requested icon size.
    protected static StackPane createIconViewport(Node icon, double iconSize, String... styleClasses) {
        StackPane viewport = new StackPane(icon);
        viewport.getStyleClass().add(DEMO_VECTOR_ICON_VIEWPORT_STYLE_CLASS);
        viewport.getStyleClass().addAll(styleClasses);
        viewport.setMouseTransparent(true);
        viewport.setStyle(String.format(
                Locale.ROOT,
                "-fx-min-width: %.1fpx; -fx-min-height: %.1fpx; -fx-pref-width: %.1fpx; "
                        + "-fx-pref-height: %.1fpx; -fx-max-width: %.1fpx; -fx-max-height: %.1fpx;",
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        ));
        if (icon instanceof SVGPath svgIcon) {
            double scale = iconSize / defaultIconGlyphSize(M3IconSize.MEDIUM);
            svgIcon.setScaleX(scale);
            svgIcon.setScaleY(scale);
        }
        return viewport;
    }

    /// Creates a floating action button menu sample using one paired color family.
    protected M3FabMenu createFabMenu(
            M3FloatingActionButtonVariant actionVariant,
            M3FloatingActionButtonVariant toggleVariant
    ) {
        M3FloatingActionButton create = createExtendedFab(
                "Create",
                "create",
                actionVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        M3FloatingActionButton edit = createExtendedFab(
                "Edit",
                "edit",
                actionVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        M3FloatingActionButton share = createExtendedFab(
                "Share",
                "share",
                actionVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        create.setOnAction(event -> context.showSnackbar("Created"));
        edit.setOnAction(event -> context.showSnackbar("Edited"));
        share.setOnAction(event -> context.showSnackbar("Shared"));
        M3FloatingActionButton toggle = createFab(
                "add",
                toggleVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        M3FabMenu menu = new M3FabMenu(toggle);
        menu.getItems().addAll(create, edit, share);
        return menu;
    }
}
