// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 tooltip.
@NotNullByDefault
public class M3Tooltip extends Tooltip {
    /// The base style class for M3FX tooltips.
    public static final String STYLE_CLASS = "m3-tooltip";

    /// The node property key used to store theme inheritance listeners.
    private static final String THEME_INHERITANCE_LISTENER_KEY =
            M3Tooltip.class.getName() + ".themeInheritanceListener";

    /// The explicit theme applied directly to this tooltip.
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies theme declarations to the tooltip style.
        @Override
        protected void invalidated() {
            themeInherited = applyingInheritedTheme;
            applyTheme(get());
        }
    };

    /// The tooltip style before theme declarations were added.
    private @Nullable String baseStyle;

    /// Whether the current theme value was inherited from the target node scene.
    private boolean themeInherited;

    /// Whether the current theme mutation is applying an inherited value.
    private boolean applyingInheritedTheme;

    /// Creates an empty tooltip.
    public M3Tooltip() {
        initialize();
    }

    /// Creates a tooltip with text.
    public M3Tooltip(String text) {
        super(text);
        initialize();
    }

    /// Installs a Material Design 3 tooltip with the supplied text on a node.
    public static M3Tooltip install(Node node, String text) {
        M3Tooltip tooltip = new M3Tooltip(text);
        install(node, tooltip);
        return tooltip;
    }

    /// Installs a Material Design 3 tooltip on a node.
    public static void install(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        installThemeInheritance(node, tooltip);
        tooltip.inheritThemeFrom(node);
        Tooltip.install(node, tooltip);
    }

    /// Uninstalls a Material Design 3 tooltip from a node.
    public static void uninstall(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        uninstallThemeInheritance(node);
        Tooltip.uninstall(
                node,
                tooltip
        );
    }

    /// Returns the explicit theme applied directly to this tooltip.
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied directly to this tooltip.
    public final void setTheme(@Nullable M3Theme theme) {
        applyingInheritedTheme = false;
        this.theme.set(theme);
    }

    /// Returns the explicit theme property.
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Adds base style classes and Material timing defaults.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setWrapText(true);
        setShowDelay(Duration.millis(500.0));
        setHideDelay(Duration.millis(0.0));
        setShowDuration(Duration.seconds(5.0));
    }

    /// Applies the node's scene theme when this tooltip has no explicit theme.
    private void inheritThemeFrom(Node node) {
        inheritThemeFrom(node.getScene());
    }

    /// Applies the scene theme when this tooltip has no explicit theme.
    private void inheritThemeFrom(@Nullable Scene scene) {
        if (getTheme() != null && !themeInherited) {
            return;
        }

        if (scene == null) {
            if (themeInherited) {
                setInheritedTheme(null);
            }
            return;
        }

        @Nullable M3Theme inheritedTheme = M3ThemeManager.getTheme(scene);
        if (inheritedTheme != null) {
            setInheritedTheme(inheritedTheme);
        } else if (themeInherited) {
            setInheritedTheme(null);
        }
    }

    /// Sets a theme value that came from the target node scene.
    private void setInheritedTheme(@Nullable M3Theme theme) {
        applyingInheritedTheme = true;
        try {
            this.theme.set(theme);
        } finally {
            applyingInheritedTheme = false;
        }
    }

    /// Applies or clears inline theme declarations on the tooltip.
    private void applyTheme(@Nullable M3Theme theme) {
        if (theme == null) {
            String currentBaseStyle = baseStyle;
            if (currentBaseStyle != null) {
                setStyle(currentBaseStyle);
                baseStyle = null;
            }
            return;
        }

        if (baseStyle == null) {
            baseStyle = getStyle();
        }
        setStyle(mergeStyles(baseStyle, theme.toRootStyleDeclarations()));
    }

    /// Merges existing tooltip style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }

    /// Installs a listener that keeps inherited tooltip themes in sync with the target node scene.
    private static void installThemeInheritance(Node node, M3Tooltip tooltip) {
        uninstallThemeInheritance(node);

        SceneThemeListener listener = new SceneThemeListener(tooltip);
        node.sceneProperty().addListener(listener);
        node.getProperties().put(THEME_INHERITANCE_LISTENER_KEY, listener);
    }

    /// Removes any previously installed theme inheritance listener from a node.
    private static void uninstallThemeInheritance(Node node) {
        Object listener = node.getProperties().remove(THEME_INHERITANCE_LISTENER_KEY);
        if (listener instanceof SceneThemeListener sceneThemeListener) {
            node.sceneProperty().removeListener(sceneThemeListener);
        }
    }

    /// Listens for target node scene changes and reapplies inherited tooltip themes.
    @NotNullByDefault
    private static final class SceneThemeListener implements ChangeListener<@Nullable Scene> {
        /// The tooltip receiving inherited theme values.
        private final M3Tooltip tooltip;

        /// Creates a scene listener for a tooltip.
        private SceneThemeListener(M3Tooltip tooltip) {
            this.tooltip = tooltip;
        }

        /// Applies the new scene theme when the target node enters a scene.
        @Override
        public void changed(
                ObservableValue<? extends @Nullable Scene> observable,
                @Nullable Scene oldValue,
                @Nullable Scene newValue
        ) {
            tooltip.inheritThemeFrom(newValue);
        }
    }
}
