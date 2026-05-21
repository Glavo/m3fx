// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/// A JavaFX dialog that uses an [M3DialogPane] by default.
///
/// `M3Dialog` keeps the standard JavaFX [Dialog] lifecycle, result conversion, modality, ownership, and button
/// handling while installing a Material Design 3 dialog pane. It can inherit the theme from an owner window or
/// accept an explicit [org.glavo.m3fx.theme.M3Theme] so dialogs opened from popups or secondary windows retain
/// the same color and typography context.
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
///
/// @param <R> the dialog result type
@NotNullByDefault
public class M3Dialog<R> extends Dialog<R> {
    /// The property key that stores the dialog pane style before theme declarations were added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3Dialog.class.getName() + ".baseStyle";

    /// The property key that stores the generated theme stylesheet installed on the dialog pane.
    private static final String THEME_STYLESHEET_PROPERTY_KEY =
            M3Dialog.class.getName() + ".themeStylesheet";

    // The explicit theme applied directly to the dialog pane.
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies theme declarations to the Material dialog pane.
        @Override
        protected void invalidated() {
            applyEffectiveTheme();
        }
    };

    /// Creates a Material Design 3 dialog.
    public M3Dialog() {
        installDialogPane(new M3DialogPane());
        addEventFilter(DialogEvent.DIALOG_SHOWING, event -> applyEffectiveTheme());
    }

    /// Creates a Material Design 3 dialog with a title.
    ///
    /// @param title the dialog window title
    public M3Dialog(String title) {
        this();
        setTitle(Objects.requireNonNull(title, "title"));
    }

    /// Creates a Material Design 3 dialog with title, header text, content text, and button types.
    ///
    /// @param title the dialog window title
    /// @param headerText the dialog pane header text
    /// @param contentText the dialog pane content text
    /// @param buttonTypes the button types installed in the dialog pane
    public M3Dialog(
            String title,
            String headerText,
            String contentText,
            ButtonType... buttonTypes
    ) {
        this(title);
        Objects.requireNonNull(buttonTypes, "buttonTypes");
        for (ButtonType buttonType : buttonTypes) {
            Objects.requireNonNull(buttonType, "buttonType");
        }

        M3DialogPane pane = getM3DialogPane();
        pane.setHeaderText(Objects.requireNonNull(headerText, "headerText"));
        pane.setContentText(Objects.requireNonNull(contentText, "contentText"));
        pane.getButtonTypes().addAll(buttonTypes);
    }

    /// Returns the Material Design 3 dialog pane.
    ///
    /// @return the Material Design 3 dialog pane
    public final M3DialogPane getM3DialogPane() {
        DialogPane pane = getDialogPane();
        if (pane instanceof M3DialogPane materialPane) {
            return materialPane;
        }
        throw new IllegalStateException("dialog pane is not an M3DialogPane");
    }

    /// Returns the explicit theme applied directly to this dialog.
    ///
    /// When this value is null, the dialog inherits the owner scene theme when it is shown.
    ///
    /// @return the explicit theme applied to this dialog, or `null` to inherit from the owner scene
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied directly to this dialog.
    ///
    /// Passing null clears the explicit override and allows owner scene theme inheritance.
    ///
    /// @param theme the explicit theme to apply, or `null` to inherit from the owner scene
    public final void setTheme(@Nullable M3Theme theme) {
        this.theme.set(theme);
    }

    /// Returns the explicit theme property.
    ///
    /// @return the explicit theme property
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Installs the Material dialog pane and its shared stylesheet.
    private void installDialogPane(M3DialogPane pane) {
        installStylesheet(pane);
        setDialogPane(pane);
    }

    /// Applies the explicit theme or the current owner scene theme to the dialog pane.
    private void applyEffectiveTheme() {
        M3Theme effectiveTheme = getTheme();
        if (effectiveTheme == null) {
            effectiveTheme = getOwnerTheme();
        }
        applyTheme(getM3DialogPane(), effectiveTheme);
    }

    /// Returns the theme installed on the owner scene when one is available.
    private @Nullable M3Theme getOwnerTheme() {
        Window owner = getOwner();
        if (owner == null) {
            return null;
        }

        @Nullable Scene ownerScene = owner.getScene();
        return ownerScene == null ? null : M3ThemeManager.getTheme(ownerScene);
    }

    /// Adds the shared M3FX stylesheet to the dialog pane.
    private static void installStylesheet(M3DialogPane pane) {
        String stylesheet = M3ThemeManager.stylesheetUrl();
        moveOrAdd(pane.getStylesheets(), stylesheet, 0);
    }

    /// Applies or clears theme declarations on the dialog pane.
    private static void applyTheme(M3DialogPane pane, @Nullable M3Theme theme) {
        if (theme == null) {
            uninstallThemeStylesheet(pane);
            Object baseStyleValue = pane.getProperties().remove(BASE_STYLE_PROPERTY_KEY);
            pane.setStyle(baseStyleValue instanceof String baseStyle ? baseStyle : "");
            return;
        }

        installStylesheet(pane);
        installThemeStylesheet(pane, theme);

        if (!pane.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            pane.getProperties().put(BASE_STYLE_PROPERTY_KEY, pane.getStyle());
        }

        Object baseStyleValue = pane.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String ? (String) baseStyleValue : "";
        pane.setStyle(mergeStyles(baseStyle, theme.toRootStyleDeclarations()));
    }

    /// Adds the generated theme stylesheet for the supplied theme.
    private static void installThemeStylesheet(M3DialogPane pane, M3Theme theme) {
        String stylesheet = M3ThemeManager.themeStylesheetUrl(theme);
        Object previousStylesheet = pane.getProperties().put(THEME_STYLESHEET_PROPERTY_KEY, stylesheet);
        if (previousStylesheet instanceof String previous && !previous.equals(stylesheet)) {
            pane.getStylesheets().remove(previous);
        }
        moveOrAdd(pane.getStylesheets(), stylesheet, themeStylesheetIndex(pane.getStylesheets()));
    }

    /// Removes the generated theme stylesheet from the dialog pane.
    private static void uninstallThemeStylesheet(M3DialogPane pane) {
        Object previousStylesheet = pane.getProperties().remove(THEME_STYLESHEET_PROPERTY_KEY);
        if (previousStylesheet instanceof String previous) {
            pane.getStylesheets().remove(previous);
        }
    }

    /// Moves an existing stylesheet or adds a new stylesheet at the requested index.
    private static void moveOrAdd(List<String> stylesheets, String stylesheet, int index) {
        int targetIndex = Math.min(Math.max(0, index), stylesheets.size());
        int currentIndex = stylesheets.indexOf(stylesheet);
        if (currentIndex == targetIndex) {
            return;
        }
        if (currentIndex >= 0) {
            stylesheets.remove(currentIndex);
            if (currentIndex < targetIndex) {
                targetIndex--;
            }
        }
        stylesheets.add(Math.min(targetIndex, stylesheets.size()), stylesheet);
    }

    /// Returns the insertion index for the generated theme stylesheet.
    private static int themeStylesheetIndex(List<String> stylesheets) {
        int baseStylesheetIndex = stylesheets.indexOf(M3ThemeManager.stylesheetUrl());
        return baseStylesheetIndex >= 0 ? baseStylesheetIndex + 1 : 0;
    }

    /// Merges existing pane style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }
}
