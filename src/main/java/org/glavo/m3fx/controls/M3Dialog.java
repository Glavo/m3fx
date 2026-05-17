// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A JavaFX dialog that uses an [M3DialogPane] by default.
@NotNullByDefault
public class M3Dialog<R> extends Dialog<R> {
    /// The property key that stores the dialog pane style before theme declarations were added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3Dialog.class.getName() + ".baseStyle";

    /// The theme applied directly to the dialog pane.
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies theme declarations to the Material dialog pane.
        @Override
        protected void invalidated() {
            applyTheme(getM3DialogPane(), get());
        }
    };

    /// Creates a Material Design 3 dialog.
    public M3Dialog() {
        installDialogPane(new M3DialogPane());
    }

    /// Creates a Material Design 3 dialog with a title.
    public M3Dialog(String title) {
        this();
        setTitle(Objects.requireNonNull(title, "title"));
    }

    /// Creates a Material Design 3 dialog with title, header text, content text, and button types.
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
    public final M3DialogPane getM3DialogPane() {
        DialogPane pane = getDialogPane();
        if (pane instanceof M3DialogPane materialPane) {
            return materialPane;
        }
        throw new IllegalStateException("dialog pane is not an M3DialogPane");
    }

    /// Returns the theme applied directly to this dialog.
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the theme applied directly to this dialog.
    public final void setTheme(@Nullable M3Theme theme) {
        this.theme.set(theme);
    }

    /// Returns the theme property.
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Installs the Material dialog pane and its shared stylesheet.
    private void installDialogPane(M3DialogPane pane) {
        installStylesheet(pane);
        setDialogPane(pane);
    }

    /// Adds the shared M3FX stylesheet to the dialog pane.
    private static void installStylesheet(M3DialogPane pane) {
        String stylesheet = M3ThemeManager.stylesheetUrl();
        if (!pane.getStylesheets().contains(stylesheet)) {
            pane.getStylesheets().add(stylesheet);
        }
    }

    /// Applies or clears theme declarations on the dialog pane.
    private static void applyTheme(M3DialogPane pane, @Nullable M3Theme theme) {
        if (theme == null) {
            Object baseStyleValue = pane.getProperties().remove(BASE_STYLE_PROPERTY_KEY);
            pane.setStyle(baseStyleValue instanceof String baseStyle ? baseStyle : "");
            return;
        }

        if (!pane.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            pane.getProperties().put(BASE_STYLE_PROPERTY_KEY, pane.getStyle());
        }

        Object baseStyleValue = pane.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String ? (String) baseStyleValue : "";
        pane.setStyle(mergeStyles(baseStyle, theme.toRootStyleDeclarations()));
    }

    /// Merges existing pane style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }
}
