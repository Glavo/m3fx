// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.Styleable;
import javafx.scene.Parent;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/// Prepares stylesheet and token context for popup-hosted M3FX content roots.
///
/// Popup content is often styled before JavaFX creates the popup scene. This helper copies the owner stylesheet
/// list, installs popup-specific control stylesheets, applies the fallback JavaFX `root` style class, and copies
/// a local Material theme context when one is available. The fallback class is intentionally applied before
/// theme copying so standalone popup roots can resolve fallback tokens during early `applyCss()` calls.
@NotNullByDefault
public final class M3PopupStyles {
    /// The M3FX token root style class used by fallback token declarations outside JavaFX scene roots.
    public static final String FALLBACK_ROOT_STYLE_CLASS = "m3-fallback-root";

    /// The property key that stores a popup root style before copied theme declarations are applied.
    private static final String BASE_STYLE_PROPERTY_KEY = M3PopupStyles.class.getName() + ".baseStyle";

    /// The property key that stores the last source stylesheet snapshot copied into a popup root.
    private static final String SOURCE_STYLESHEETS_PROPERTY_KEY =
            M3PopupStyles.class.getName() + ".sourceStylesheets";

    /// The property key that stores the generated stylesheet added for a copied local theme.
    private static final String COPIED_THEME_STYLESHEET_PROPERTY_KEY =
            M3PopupStyles.class.getName() + ".copiedThemeStylesheet";

    /// Prevents utility class instantiation.
    private M3PopupStyles() {
    }

    /// Prepares a popup root with copied stylesheets, fallback tokens, and optional local theme context.
    ///
    /// @param popupRoot the popup-hosted root that will be styled before or after popup scene creation
    /// @param sourceStylesheets the owner scene or parent popup stylesheet list to copy
    /// @param themeRoot the local theme root to copy into the popup, or `null` for standalone fallback tokens
    /// @param controlStylesheet the popup-specific control stylesheet URL to append, or `null` for none
    public static void preparePopupRoot(
            Parent popupRoot,
            List<String> sourceStylesheets,
            @Nullable Parent themeRoot,
            @Nullable String controlStylesheet
    ) {
        Objects.requireNonNull(popupRoot, "popupRoot");
        Objects.requireNonNull(sourceStylesheets, "sourceStylesheets");

        synchronizeSourceStylesheets(popupRoot, sourceStylesheets);
        ensureFallbackStylesheet(popupRoot);
        if (controlStylesheet != null) {
            addStylesheet(popupRoot, controlStylesheet);
        }
        addFallbackRootStyleClass(popupRoot);
        preserveBaseStyle(popupRoot);
        @Nullable M3Theme theme = themeRoot == null ? null : M3ThemeMetadata.getTheme(themeRoot);
        if (themeRoot != null) {
            M3ThemeRuntime.copyThemeContext(themeRoot, popupRoot);
            updateCopiedThemeStylesheet(popupRoot, sourceStylesheets, theme);
        } else {
            restoreBaseStyle(popupRoot);
            updateCopiedThemeStylesheet(popupRoot, sourceStylesheets, null);
        }
    }

    /// Copies owner stylesheets only when the source list has changed since the previous synchronization.
    private static void synchronizeSourceStylesheets(Parent popupRoot, List<String> sourceStylesheets) {
        Object snapshot = popupRoot.getProperties().get(SOURCE_STYLESHEETS_PROPERTY_KEY);
        if (snapshot instanceof List<?> sourceSnapshot && sourceSnapshot.equals(sourceStylesheets)) {
            return;
        }

        popupRoot.getStylesheets().setAll(sourceStylesheets);
        popupRoot.getProperties().put(SOURCE_STYLESHEETS_PROPERTY_KEY, List.copyOf(sourceStylesheets));
    }

    /// Updates the generated stylesheet inserted for a copied local theme.
    private static void updateCopiedThemeStylesheet(
            Parent popupRoot,
            List<String> sourceStylesheets,
            @Nullable M3Theme theme
    ) {
        Object previousValue = popupRoot.getProperties().get(COPIED_THEME_STYLESHEET_PROPERTY_KEY);
        @Nullable String previousStylesheet = previousValue instanceof String stylesheet ? stylesheet : null;
        @Nullable String stylesheet = theme == null ? null : M3ThemeRuntime.themeStylesheetUrl(theme);
        if (previousStylesheet != null
                && !previousStylesheet.equals(stylesheet)
                && !sourceStylesheets.contains(previousStylesheet)) {
            popupRoot.getStylesheets().remove(previousStylesheet);
        }

        if (stylesheet == null) {
            if (previousStylesheet != null) {
                popupRoot.getProperties().remove(COPIED_THEME_STYLESHEET_PROPERTY_KEY);
            }
            return;
        }

        moveStylesheetToEnd(popupRoot, stylesheet);
        if (!stylesheet.equals(previousStylesheet)) {
            popupRoot.getProperties().put(COPIED_THEME_STYLESHEET_PROPERTY_KEY, stylesheet);
        }
    }

    /// Adds one stylesheet URL to a popup root when it is not already present.
    ///
    /// @param popupRoot the popup-hosted root that receives the stylesheet
    /// @param stylesheet the stylesheet URL to add
    public static void addStylesheet(Parent popupRoot, String stylesheet) {
        Objects.requireNonNull(popupRoot, "popupRoot");
        Objects.requireNonNull(stylesheet, "stylesheet");

        List<String> stylesheets = popupRoot.getStylesheets();
        if (!stylesheets.contains(stylesheet)) {
            stylesheets.add(stylesheet);
        }
    }

    /// Ensures the fallback token stylesheet is present at the lowest application stylesheet priority.
    ///
    /// @param popupRoot the popup-hosted root that receives fallback token declarations
    private static void ensureFallbackStylesheet(Parent popupRoot) {
        List<String> stylesheets = popupRoot.getStylesheets();
        String stylesheet = M3Stylesheets.fallbackStylesheet();
        int index = stylesheets.indexOf(stylesheet);
        if (index == 0) {
            return;
        }
        if (index > 0) {
            stylesheets.remove(index);
        }
        stylesheets.add(0, stylesheet);
    }

    /// Moves a stylesheet URL to the highest popup-root stylesheet priority.
    ///
    /// @param popupRoot the popup-hosted root that receives the stylesheet
    /// @param stylesheet the stylesheet URL to move or add
    private static void moveStylesheetToEnd(Parent popupRoot, String stylesheet) {
        List<String> stylesheets = popupRoot.getStylesheets();
        int currentIndex = stylesheets.indexOf(stylesheet);
        if (currentIndex >= 0 && currentIndex == stylesheets.size() - 1) {
            return;
        }
        if (currentIndex >= 0) {
            stylesheets.remove(currentIndex);
        }
        stylesheets.add(stylesheet);
    }

    /// Ensures fallback token declarations match a standalone scene or popup root.
    ///
    /// @param root the styleable root that should expose default JavaFX root token declarations
    public static void addFallbackRootStyleClass(Styleable root) {
        Objects.requireNonNull(root, "root");

        List<String> styleClasses = root.getStyleClass();
        if (!styleClasses.contains(FALLBACK_ROOT_STYLE_CLASS)) {
            styleClasses.add(FALLBACK_ROOT_STYLE_CLASS);
        }
    }

    /// Preserves the popup root style that should be restored when no copied theme context is available.
    ///
    /// @param popupRoot the popup-hosted root whose base style should be preserved
    private static void preserveBaseStyle(Parent popupRoot) {
        if (!popupRoot.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            popupRoot.getProperties().put(BASE_STYLE_PROPERTY_KEY, popupRoot.getStyle());
        }
    }

    /// Restores the popup root style and removes copied theme metadata.
    ///
    /// @param popupRoot the popup-hosted root whose copied theme context should be cleared
    private static void restoreBaseStyle(Parent popupRoot) {
        M3ThemeRuntime.clearThemeStyleClasses(popupRoot);
        M3ThemeMetadata.clearTheme(popupRoot);
        Object baseStyleValue = popupRoot.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String style ? style : "";
        if (!Objects.equals(popupRoot.getStyle(), baseStyle)) {
            popupRoot.setStyle(baseStyle);
        }
    }
}
