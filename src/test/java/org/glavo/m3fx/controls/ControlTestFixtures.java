// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Provides control fixtures and input events shared by behavioral test suites.
@NotNullByDefault
final class ControlTestFixtures {
    /// Prevents instantiation of this utility class.
    private ControlTestFixtures() {
    }

    /// The default style class for command buttons.
    static final String BUTTON_STYLE_CLASS = "m3-button";

    /// The default style class for toggle icon buttons.
    static final String ICON_TOGGLE_BUTTON_STYLE_CLASS = "m3-icon-toggle-button";

    /// Applies the m3fx stylesheet to a control in a scene.
    static void applyCss(javafx.scene.Node node) {
        Pane root = new Pane(node);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }

    /// Creates a compact SVG icon used by component graphic-slot contract tests.
    ///
    /// @return a 24-unit SVG icon
    static M3SVGIcon svgTestIcon() {
        return new M3SVGIcon("M4 4H20V20H4Z", new Rectangle2D(0.0, 0.0, 24.0, 24.0));
    }

    /// Returns deterministic color tokens used by button state style tests.
    static String buttonStateTestColors() {
        return "-m3-color-primary: rgb(1,2,3); "
                + "-m3-color-on-primary: rgb(4,5,6); "
                + "-m3-color-secondary: rgb(46,47,48); "
                + "-m3-color-on-secondary: rgb(49,50,51); "
                + "-m3-color-tertiary: rgb(52,53,54); "
                + "-m3-color-on-tertiary: rgb(55,56,57); "
                + "-m3-color-secondary-container: rgb(7,8,9); "
                + "-m3-color-on-secondary-container: rgb(10,11,12); "
                + "-m3-color-outline: rgb(13,14,15); "
                + "-m3-color-outline-variant: rgb(43,44,45); "
                + "-m3-color-surface-container-low: rgb(16,17,18); "
                + "-m3-color-surface-container-high: rgb(19,20,21); "
                + "-m3-color-primary-container: rgb(22,23,24); "
                + "-m3-color-on-primary-container: rgb(25,26,27); "
                + "-m3-color-tertiary-container: rgb(28,29,30); "
                + "-m3-color-on-tertiary-container: rgb(31,32,33); "
                + "-m3-color-on-surface: rgb(34,35,36); "
                + "-m3-color-surface-container-highest: rgb(37,38,39); "
                + "-m3-color-on-surface-variant: rgb(40,41,42);";
    }

    /// Applies an interactive pseudo-class combination that must keep M3FX button styles authoritative.
    static void applyInteractivePseudoClasses(Node node) {
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("armed"), true);
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
    }

    /// Returns a region looked up below a node.
    static Region lookupRegion(Node node, String selector) {
        Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
    }

    /// Returns the built-in clear button currently installed in a text input layout.
    static M3IconButton textInputClearButton(M3TextInputLayout layout) {
        layout.applyCss();
        layout.layout();
        Node child = layout.lookup("." + "m3-text-input-clear-button");
        assertInstanceOf(M3IconButton.class, child);
        return (M3IconButton) child;
    }

    /// Returns the rendered search result container for a search view.
    static VBox searchViewResultsContainer(M3SearchView searchView) {
        searchView.applyCss();
        searchView.layout();
        Node child = searchView.lookup("." + "m3-search-view-results");
        assertInstanceOf(VBox.class, child);
        return (VBox) child;
    }

    /// Returns the radio indicator dot shape.
    static Shape radioDot(M3RadioButton radioButton) {
        return lookupShape(radioButton, ".dot");
    }

    /// Returns the transform layer that animates a radio dot independently from its CSS opacity.
    static Node radioDotAnimationLayer(M3RadioButton radioButton) {
        return Objects.requireNonNull(radioDot(radioButton).getParent(), "radio dot animation layer");
    }

    /// Returns the headline label rendered by a list item skin.
    static Label listItemHeadlineLabel(M3ListItem item) {
        return assertInstanceOf(
                Label.class,
                item.lookup(".m3-list-item-headline"),
                () -> "list item headline label: " + item
        );
    }

    /// Returns the list item skin container region.
    static javafx.scene.layout.Region listItemContainer(M3ListItem listItem) {
        javafx.scene.Node container = listItem.lookup(".m3-list-item-container");
        assertInstanceOf(javafx.scene.layout.Region.class, container);
        return (javafx.scene.layout.Region) container;
    }

    /// Returns the list item selected container region.
    static javafx.scene.layout.Region listItemSelectionContainer(M3ListItem listItem) {
        javafx.scene.Node container = listItem.lookup(".m3-list-item-selection-container");
        assertInstanceOf(javafx.scene.layout.Region.class, container);
        return (javafx.scene.layout.Region) container;
    }

    /// Returns the segmented button selected container region.
    static javafx.scene.layout.Region segmentedButtonSelectionContainer(M3SegmentedButton button) {
        javafx.scene.Node container = button.lookup("." + "m3-segmented-button-selection-container");
        assertInstanceOf(javafx.scene.layout.Region.class, container);
        return (javafx.scene.layout.Region) container;
    }

    /// Verifies that visual-gallery icon slots use vector graphics instead of text fallback glyphs.
    static void assertVisualIconSlotsUseVectorGraphics(Node root) {
        visitVisibleNodes(root, node -> {
            if (node instanceof M3IconButton button) {
                assertIconSlotUsesVectorGraphic(button, button.getGraphic());
            } else if (node instanceof M3IconToggleButton button) {
                assertIconSlotUsesVectorGraphic(button, button.getGraphic());
            } else if (node instanceof M3FloatingActionButton button) {
                assertIconSlotUsesVectorGraphic(button, button.getGraphic());
            } else if (node instanceof M3NavigationItem item) {
                assertIconSlotUsesVectorGraphic(item, item.getGraphic());
            } else if (node instanceof M3Chip chip) {
                assertIconSlotUsesVectorGraphic(chip, chip.getGraphic());
            } else if (node instanceof M3TextInputLayout inputLayout) {
                assertIconSlotUsesVectorGraphic(inputLayout, inputLayout.getLeading());
            } else if (node instanceof M3ListItem item) {
                assertIconSlotUsesVectorGraphic(item, item.getLeading());
            }
        });
    }

    /// Returns the first descendant of the requested node type.
    ///
    /// @param root the root node to search
    /// @param type the descendant type to find
    /// @param <T>  the descendant node type
    /// @return the first matching descendant, including the root itself
    static <T extends Node> T firstDescendantOfType(Node root, Class<T> type) {
        @Nullable T descendant = findFirstDescendantOfType(root, type);
        if (descendant != null) {
            return descendant;
        }
        throw new AssertionError("Expected descendant of type " + type.getSimpleName() + " under " + root);
    }

    /// Finds the first descendant of the requested node type.
    ///
    /// @param root the root node to search
    /// @param type the descendant type to find
    /// @param <T>  the descendant node type
    /// @return the first matching descendant, or `null` when none exists
    static <T extends Node> @Nullable T findFirstDescendantOfType(Node root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable T descendant = findFirstDescendantOfType(child, type);
                if (descendant != null) {
                    return descendant;
                }
            }
        }
        return null;
    }

    /// Verifies that an icon slot graphic contains SVG geometry when present.
    static void assertIconSlotUsesVectorGraphic(Node owner, @Nullable Node graphic) {
        if (graphic == null) {
            return;
        }

        assertTrue(
                containsSvgPath(graphic),
                () -> "visual icon slot should use SVG geometry: owner="
                        + owner.getClass().getSimpleName()
                        + ", graphic="
                        + graphic.getClass().getSimpleName()
        );
    }

    /// Returns whether a node hierarchy contains SVG path geometry.
    static boolean containsSvgPath(Node node) {
        if (node instanceof SVGPath) {
            return true;
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsSvgPath(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /// Verifies the aggregate accessibility state exposed for a binary toggle when supported by the runtime.
    static void assertAggregateToggleState(Node control, boolean selected) {
        @Nullable AccessibleAttribute toggleStateAttribute = M3Accessible.attribute("TOGGLE_STATE");
        if (toggleStateAttribute == null) {
            assertNull(M3Accessible.toggleState(selected));
        } else {
            assertEquals(M3Accessible.toggleState(selected),
                    control.queryAccessibleAttribute(toggleStateAttribute));
        }
    }

    /// Verifies the aggregate accessibility state exposed for a three-state toggle when supported by the runtime.
    static void assertAggregateToggleState(
            Node control,
            boolean selected,
            boolean indeterminate
    ) {
        @Nullable AccessibleAttribute toggleStateAttribute = M3Accessible.attribute("TOGGLE_STATE");
        if (toggleStateAttribute == null) {
            assertNull(M3Accessible.toggleState(selected, indeterminate));
        } else {
            assertEquals(M3Accessible.toggleState(selected, indeterminate),
                    control.queryAccessibleAttribute(toggleStateAttribute));
        }
    }

    /// Verifies that a control reports and focuses the expected accessibility focus node.
    static void assertAccessibleFocus(Node control, Node expectedFocusNode) {
        assertSame(expectedFocusNode, control.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        control.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

        assertTrue(expectedFocusNode.isFocused());
    }

    /// Verifies that a container keeps the child that currently owns scene focus for default focus actions.
    static void assertCurrentAccessibleFocus(Node control, Node focusedChild) {
        focusedChild.requestFocus();
        assertTrue(focusedChild.isFocused());
        assertSame(focusedChild, control.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        control.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

        assertTrue(focusedChild.isFocused());

        control.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

        assertTrue(focusedChild.isFocused());
        assertSame(focusedChild, control.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that a structural owner exposes focus from a nested popup menu target.
    static void assertNestedPopupAccessibleFocus(
            Node owner,
            M3MenuButton menuButton,
            M3SubMenuItem subMenuItem,
            M3MenuItem target
    ) {
        assertTrue(menuButton.isShowing());
        assertTrue(subMenuItem.isSubMenuShowing());
        assertTrue(target.isFocused());
        assertSame(target, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(target, owner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Applies a pseudo-class used by visual state snapshots.
    static void applyPseudoState(Node node, String pseudoClass) {
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass(pseudoClass), true);
    }

    /// Creates a text label that inherits the gallery's Material color tokens.
    static Label visualLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: -m3-color-on-surface;");
        return label;
    }

    /// Creates a fixed-size carousel item for behavior tests.
    static StackPane carouselTestItem(String text) {
        Label label = new Label(text);
        StackPane item = new StackPane(label);
        item.setPrefSize(140.0, 72.0);
        item.setStyle("-fx-background-color: -m3-color-secondary-container; "
                + "-fx-background-radius: 16px; "
                + "-fx-padding: 16px;");
        return item;
    }

    /// Creates a navigation drawer item for visual snapshot tests.
    static M3ListItem drawerItem(String text, boolean selected) {
        M3ListItem item = new M3ListItem(text);
        String iconName = switch (text) {
            case "Sent" -> "share";
            case "Archive" -> "bookmark";
            default -> "inbox";
        };
        item.setLeading(visualIcon(iconName));
        item.setSelected(selected);
        return item;
    }

    /// Creates a button with the requested variant.
    static M3Button createButton(String text, M3ButtonVariant variant) {
        return new M3Button(text, variant);
    }

    /// Creates a button with the requested variant and action handler.
    static M3Button createButton(
            String text,
            M3ButtonVariant variant,
            @Nullable javafx.event.EventHandler<ActionEvent> onAction
    ) {
        M3Button button = new M3Button(text, variant);
        button.setOnAction(onAction);
        return button;
    }

    /// Returns the primary action part exposed by a split button.
    static M3Button splitButtonActionButton(M3SplitButton splitButton) {
        return assertInstanceOf(
                M3Button.class,
                splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0)
        );
    }

    /// Returns the menu part exposed by a split button.
    static M3MenuButton splitButtonMenuButton(M3SplitButton splitButton) {
        return assertInstanceOf(
                M3MenuButton.class,
                splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1)
        );
    }

    /// Creates a split button with the requested variant and menu items.
    static M3SplitButton createSplitButton(String text, M3ButtonVariant variant, Node... items) {
        M3SplitButton button = new M3SplitButton(text);
        button.getItems().addAll(items);
        button.setVariant(variant);
        return button;
    }

    /// Creates a floating action button with text, graphic content, variant, and size.
    static M3FloatingActionButton createFab(
            String text,
            @Nullable Node graphic,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(text, graphic);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a floating action button with graphic content, variant, and size.
    static M3FloatingActionButton createGraphicFab(
            @Nullable Node graphic,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(graphic);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a banner with a leading icon and trailing actions.
    static M3Banner createBanner(String text, Node icon, Node... actions) {
        M3Banner banner = banner(text, actions);
        banner.setIcon(Objects.requireNonNull(icon, "icon"));
        return banner;
    }

    /// Creates a text field with the requested variant.
    static M3TextField createTextField(String text, M3TextInputVariant variant) {
        M3TextField textField = new M3TextField(text);
        textField.setVariant(variant);
        return textField;
    }

    /// Creates a password field with the requested variant.
    static M3PasswordField createPasswordField(String text, M3TextInputVariant variant) {
        M3PasswordField passwordField = new M3PasswordField(text);
        passwordField.setVariant(variant);
        return passwordField;
    }

    /// Creates a text area with the requested variant.
    static M3TextArea createTextArea(String text, M3TextInputVariant variant) {
        M3TextArea textArea = new M3TextArea(text);
        textArea.setVariant(variant);
        return textArea;
    }

    /// Creates an avatar with the requested variant.
    static M3Avatar createAvatar(String text, M3AvatarVariant variant) {
        M3Avatar avatar = new M3Avatar(text);
        avatar.setVariant(variant);
        return avatar;
    }

    /// Creates an icon button with an M3FX icon label.
    static M3IconButton createIconButton(String iconText) {
        return new M3IconButton(new M3Icon(iconText));
    }

    /// Creates an icon button with Material Expressive sizing roles.
    static M3IconButton createIconButton(
            M3Icon icon,
            M3ButtonSize size,
            M3IconButtonWidth widthRole,
            M3ButtonShape shape
    ) {
        M3IconButton button = new M3IconButton(icon);
        button.setSize(size);
        button.setWidthRole(widthRole);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a fixed-size SVG icon viewport for visual snapshot fixtures.
    static StackPane visualIcon(String name) {
        return visualIcon(name, 24.0);
    }

    /// Creates a fixed-size SVG icon viewport for visual snapshot fixtures.
    static StackPane visualIcon(String name, double size) {
        SVGPath path = new SVGPath();
        path.setContent(visualIconPath(name));
        path.setFillRule(FillRule.EVEN_ODD);
        path.setStyle("-fx-fill: -m3-color-primary;");
        path.setMouseTransparent(true);

        StackPane viewport = new StackPane(path);
        viewport.setAlignment(Pos.CENTER);
        viewport.setMinSize(size, size);
        viewport.setPrefSize(size, size);
        viewport.setMaxSize(size, size);
        viewport.setMouseTransparent(true);
        return viewport;
    }

    /// Returns SVG path data for visual snapshot fixture icons.
    static String visualIconPath(String name) {
        return switch (Objects.requireNonNull(name, "name")) {
            case "add" -> "M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z";
            case "back" -> "M20 11H7.8l5.6-5.6L12 4l-8 8 8 8 1.4-1.4L7.8 13H20z";
            case "bold" ->
                    "M15.6 10.8c1-.7 1.7-1.8 1.7-2.8 0-2.3-1.8-4-4-4H7v14h7c2.1 0 3.8-1.7 3.8-3.8 0-1.5-.9-2.8-2.2-3.4zM10 6.5h3c.8 0 1.5.7 1.5 1.5S13.8 9.5 13 9.5h-3zm3.5 9H10v-3h3.5c.8 0 1.5.7 1.5 1.5s-.7 1.5-1.5 1.5z";
            case "bookmark" -> "M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z";
            case "close" ->
                    "M18.3 5.7 12 12l6.3 6.3-1.4 1.4-6.3-6.3-6.3 6.3-1.4-1.4L9.2 12 2.9 5.7l1.4-1.4 6.3 6.3 6.3-6.3z";
            case "done" -> "M9 16.2 4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z";
            case "edit" ->
                    "M3 17.3V21h3.8L17.9 9.9l-3.8-3.8zM20.7 7c.4-.4.4-1 0-1.4l-2.3-2.3c-.4-.4-1-.4-1.4 0l-1.8 1.8 3.8 3.8z";
            case "folder" -> "M10 4l2 2h8c1.1 0 2 .9 2 2v10c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z";
            case "home" -> "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
            case "inbox" ->
                    "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 14h-4c0 1.7-1.3 3-3 3s-3-1.3-3-3H5V5h14z";
            case "info" -> "M11 17h2v-6h-2zm1-14a9 9 0 1 0 0 18 9 9 0 0 0 0-18zm-1 6h2V7h-2z";
            case "italic" -> "M10 4v3h2.2l-3.4 8H6v3h8v-3h-2.2l3.4-8H18V4z";
            case "mail" ->
                    "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5-8-5V6l8 5 8-5z";
            case "menu" -> "M3 6h18v2H3zm0 5h18v2H3zm0 5h18v2H3z";
            case "more" ->
                    "M12 8a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm0 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm0 6a2 2 0 1 0 0 4 2 2 0 0 0 0-4z";
            case "person" ->
                    "M12 12c2.2 0 4-1.8 4-4s-1.8-4-4-4-4 1.8-4 4 1.8 4 4 4zm0 2c-2.7 0-8 1.3-8 4v2h16v-2c0-2.7-5.3-4-8-4z";
            case "save" ->
                    "M17 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7zm-5 16a3 3 0 1 1 0-6 3 3 0 0 1 0 6zM6 8V5h9v3z";
            case "search" ->
                    "M9.5 3a6.5 6.5 0 0 1 5.1 10.5l4.4 4.4-1.4 1.4-4.4-4.4A6.5 6.5 0 1 1 9.5 3zm0 2a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9z";
            case "share" ->
                    "M18 16.1c-.8 0-1.5.3-2 .9L8.9 12.9c.1-.3.1-.6.1-.9s0-.6-.1-.9L16 7c.5.5 1.2.9 2 .9a3 3 0 1 0-3-3c0 .3 0 .6.1.9L8 9.9A3 3 0 1 0 8 14l7.1 4.2c-.1.3-.1.5-.1.8a3 3 0 1 0 3-2.9z";
            case "star" -> "M12 17.3 18.2 21l-1.6-7L22 9.2l-7.2-.6L12 2 9.2 8.6 2 9.2 7.5 14 5.8 21z";
            case "text" -> "M5 4v3h5v13h4V7h5V4z";
            case "underline" ->
                    "M12 17c3.3 0 6-2.7 6-6V3h-2.5v8c0 1.9-1.6 3.5-3.5 3.5S8.5 12.9 8.5 11V3H6v8c0 3.3 2.7 6 6 6zM5 19v2h14v-2z";
            case "warning" -> "M12 2 1 21h22zM13 18h-2v-2h2zm0-4h-2v-4h2z";
            case "work" ->
                    "M20 6h-4V4c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6 0h-4V4h4z";
            default -> throw new IllegalArgumentException("Unknown visual icon: " + name);
        };
    }

    /// Creates a toggle icon button with graphic content, variant, and selected state.
    static M3IconToggleButton createIconToggleButton(
            @Nullable Node graphic,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        M3IconToggleButton button = new M3IconToggleButton(graphic);
        button.setVariant(variant);
        button.setSelected(selected);
        return button;
    }

    /// Creates a toggle icon button with an M3FX icon label, variant, and selected state.
    static M3IconToggleButton createIconToggleButton(
            String iconText,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        return createIconToggleButton(new M3Icon(iconText), variant, selected);
    }

    /// Creates a toggle icon button with Material Expressive sizing roles.
    static M3IconToggleButton createIconToggleButton(
            M3Icon icon,
            M3ButtonSize size,
            M3IconButtonWidth widthRole,
            M3ButtonShape shape,
            boolean selected
    ) {
        M3IconToggleButton button = createIconToggleButton(icon, M3IconToggleButtonVariant.FILLED, selected);
        button.setSize(size);
        button.setWidthRole(widthRole);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a filter chip with the requested selected state.
    static M3FilterChip createFilterChip(String text, boolean selected) {
        M3FilterChip chip = new M3FilterChip(text);
        chip.setSelected(selected);
        return chip;
    }

    /// Creates an unselected filter chip.
    static M3FilterChip createFilterChip(String text) {
        return createFilterChip(text, false);
    }

    /// Creates a segmented button with the requested selected state.
    static M3SegmentedButton createSegmentedButton(String text, boolean selected) {
        M3SegmentedButton button = new M3SegmentedButton(text);
        button.setSelected(selected);
        return button;
    }

    /// Creates a tab with the requested selected state.
    static M3Tab createTab(String text, boolean selected) {
        M3Tab tab = new M3Tab(text);
        tab.setSelected(selected);
        return tab;
    }

    /// Creates a checkbox with the requested selected state.
    static M3CheckBox createCheckBox(String text, boolean selected) {
        M3CheckBox checkBox = new M3CheckBox(text);
        checkBox.setSelected(selected);
        return checkBox;
    }

    /// Creates a radio button with the requested selected state.
    static M3RadioButton createRadioButton(String text, boolean selected) {
        M3RadioButton radioButton = new M3RadioButton(text);
        radioButton.setSelected(selected);
        return radioButton;
    }

    /// Creates a switch with the requested selected state.
    static M3Switch createSwitch(String text, boolean selected) {
        M3Switch switchControl = new M3Switch(text);
        switchControl.setSelected(selected);
        return switchControl;
    }

    /// Creates a navigation item with the requested selected state.
    static M3NavigationItem createNavigationItem(String text, boolean selected) {
        M3NavigationItem item = new M3NavigationItem(text);
        item.setSelected(selected);
        return item;
    }

    /// Creates a navigation item with graphic content and the requested selected state.
    static M3NavigationItem createNavigationItem(String text, @Nullable Node graphic, boolean selected) {
        M3NavigationItem item = new M3NavigationItem(text, graphic);
        item.setSelected(selected);
        return item;
    }

    /// Creates a navigation item with graphic content, a badge, and the requested selected state.
    static M3NavigationItem createNavigationItem(
            String text,
            @Nullable Node graphic,
            @Nullable M3Badge badge,
            boolean selected
    ) {
        M3NavigationItem item = new M3NavigationItem(text, graphic, badge);
        item.setSelected(selected);
        return item;
    }

    /// Creates a primary mouse event for control behavior tests.
    static MouseEvent primaryMouseEvent(
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        return new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                null
        );
    }

    /// Creates a primary mouse event at one local point of a concrete node.
    static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                scenePoint.getX(),
                scenePoint.getY(),
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }

    /// Creates an indirect scroll event for scroll behavior tests.
    static ScrollEvent scrollEvent(Node target, double deltaX, double deltaY) {
        return scrollEvent(target, deltaX, deltaY, false);
    }

    /// Returns a value clamped to the unit interval.
    static double clampToUnit(double value) {
        return clamp(value, 0.0, 1.0);
    }

    /// Returns a value clamped into a closed range.
    static double clamp(double value, double minValue, double maxValue) {
        if (value <= minValue) {
            return minValue;
        }
        return Math.min(value, maxValue);
    }

    /// Creates a scroll event for scroll behavior tests.
    static ScrollEvent scrollEvent(Node target, double deltaX, double deltaY, boolean direct) {
        return scrollEvent(target, deltaX, deltaY, direct, false);
    }

    /// Creates a scroll event for scroll behavior tests.
    static ScrollEvent scrollEvent(
            Node target,
            double deltaX,
            double deltaY,
            boolean direct,
            boolean shiftDown
    ) {
        return scrollEvent(
                target,
                deltaX,
                deltaY,
                direct,
                shiftDown,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0.0
        );
    }

    /// Creates a scroll event with explicit platform text-scroll units for scroll behavior tests.
    static ScrollEvent scrollEvent(
            Node target,
            double deltaX,
            double deltaY,
            boolean direct,
            boolean shiftDown,
            ScrollEvent.HorizontalTextScrollUnits horizontalUnits,
            double textDeltaX,
            ScrollEvent.VerticalTextScrollUnits verticalUnits,
            double textDeltaY
    ) {
        return new ScrollEvent(
                target,
                target,
                ScrollEvent.SCROLL,
                40.0,
                40.0,
                40.0,
                40.0,
                shiftDown,
                false,
                false,
                false,
                direct,
                false,
                deltaX,
                deltaY,
                deltaX,
                deltaY,
                horizontalUnits,
                textDeltaX,
                verticalUnits,
                textDeltaY,
                0,
                null
        );
    }

    /// Asserts that container-level navigation does not move focus out of an embedded text input.
    static void assertContainerNavigationDoesNotStealTextInputFocus(
            Parent root,
            Node navigationOwner,
            TextInputControl editor,
            Node adjacentTarget,
            KeyCode keyCode
    ) {
        root.applyCss();
        root.layout();
        editor.requestFocus();
        assertTrue(editor.isFocused(), "embedded text input should own focus before the navigation key");

        navigationOwner.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, keyCode));

        assertTrue(editor.isFocused(), "container navigation should not steal focus from embedded text input");
        assertFalse(adjacentTarget.isFocused(), "adjacent action should not receive focus from text editing keys");
    }

    /// Creates a reusable virtualized list cell driven by an item updater.
    static <T> M3ListCell<T> listCell(
            M3ListView<T> listView,
            BiConsumer<M3ListItem, T> itemUpdater
    ) {
        return new M3ListCell<>(listView) {
            /// Updates the retained row for the current data item.
            @Override
            protected void updateListItem(M3ListItem listItem, T item) {
                itemUpdater.accept(listItem, item);
            }
        };
    }

    /// Creates a reusable virtualized list cell with a caller-supplied retained row.
    static <T> M3ListCell<T> listCell(M3ListView<T> listView, M3ListItem listItem) {
        return new M3ListCell<>(listView) {
            /// Creates the retained row owned by this cell.
            @Override
            protected M3ListItem createListItem() {
                return listItem;
            }
        };
    }

    /// Creates a key event for control behavior tests.
    static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return keyEvent(eventType, code, false);
    }

    /// Creates a key event for control behavior tests with an optional Shift modifier.
    static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code, boolean shiftDown) {
        return keyEvent(eventType, code, shiftDown, false, false, false);
    }

    /// Creates a key event for control behavior tests with explicit keyboard modifiers.
    static KeyEvent keyEvent(
            EventType<KeyEvent> eventType,
            KeyCode code,
            boolean shiftDown,
            boolean controlDown,
            boolean altDown,
            boolean metaDown
    ) {
        return new KeyEvent(eventType, "", "", code, shiftDown, controlDown, altDown, metaDown);
    }

    /// Creates a key event with an explicit source and target node.
    static KeyEvent targetedKeyEvent(EventType<KeyEvent> eventType, KeyCode code, Node target) {
        return new KeyEvent(target, target, eventType, "", "", code, false, false, false, false);
    }

    /// Creates a typed key event for printable-key behavior tests.
    static KeyEvent keyTypedEvent(String character) {
        return new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED, false, false, false, false);
    }

    /// Verifies that the rendered tooltip content has its current local theme stylesheet.
    static void assertTooltipThemeStylesheet(M3Tooltip tooltip, M3Theme theme) {
        Label content = assertInstanceOf(Label.class, tooltip.getSkin().getNode().lookup(".m3-tooltip"));
        assertTrue(content.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));
    }

    /// Creates and installs a rich tooltip for tests that need installed tooltip behavior.
    static M3RichTooltip installRichTooltip(
            Node node,
            String title,
            String supportingText,
            Node... actions
    ) {
        M3RichTooltip tooltip = richTooltip(title, supportingText, actions);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }
}
