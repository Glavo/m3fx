// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.Styleable;
import javafx.scene.Parent;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
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

    /// Prevents utility class instantiation.
    private M3PopupStyles() {
    }

    /// Prepares a popup root with copied stylesheets, fallback tokens, and optional local theme context.
    ///
    /// @param popupRoot the popup-hosted root that will be styled before or after popup scene creation
    /// @param sourceStylesheets the owner scene or parent popup stylesheet list to copy
    /// @param themeRoot the local theme root to copy into the popup, or `null` for standalone fallback tokens
    /// @param controlStylesheets additional popup-specific control stylesheet URLs to append if missing
    public static void preparePopupRoot(
            Parent popupRoot,
            List<String> sourceStylesheets,
            @Nullable Parent themeRoot,
            String... controlStylesheets
    ) {
        Objects.requireNonNull(popupRoot, "popupRoot");
        Objects.requireNonNull(sourceStylesheets, "sourceStylesheets");
        Objects.requireNonNull(controlStylesheets, "controlStylesheets");

        popupRoot.getStylesheets().setAll(List.copyOf(sourceStylesheets));
        ensureFallbackStylesheet(popupRoot);
        for (String stylesheet : controlStylesheets) {
            addStylesheet(popupRoot, Objects.requireNonNull(stylesheet, "stylesheet"));
        }
        addFallbackRootStyleClass(popupRoot);
        preserveBaseStyle(popupRoot);
        @Nullable M3Theme theme = themeRoot == null ? null : M3ThemeMetadata.getTheme(themeRoot);
        if (themeRoot != null) {
            M3ThemeManager.copyThemeContext(themeRoot, popupRoot);
            if (theme != null) {
                moveStylesheetToEnd(popupRoot, M3ThemeManager.themeStylesheetUrl(theme));
            }
        } else {
            restoreBaseStyle(popupRoot);
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
        stylesheets.remove(stylesheet);
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
        M3ThemeManager.clearThemeStyleClasses(popupRoot);
        M3ThemeMetadata.clearTheme(popupRoot);
        Object baseStyleValue = popupRoot.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        popupRoot.setStyle(baseStyleValue instanceof String baseStyle ? baseStyle : "");
    }
}
