// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3Tooltip;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Creates focused icon, progress, snackbar, tooltip, and typography samples.
@NotNullByDefault
final class CatalogFeedbackSamples {
    /// Prevents instantiation of this factory class.
    private CatalogFeedbackSamples() {
    }

    /// Creates one semantic icon size and color combination.
    ///
    /// @param size the semantic icon size
    /// @param variant the semantic color role
    /// @param disabled whether the icon is disabled
    /// @return the configured SVG icon
    static Node icon(M3IconSize size, M3IconVariant variant, boolean disabled) {
        M3SVGIcon icon = CatalogIcons.create(CatalogIcons.FAVORITE);
        icon.setSize(size);
        icon.setVariant(variant);
        icon.setDisable(disabled);
        return icon;
    }

    /// Creates icons with local tint overrides.
    ///
    /// @return the locally colored icons
    static Node locallyColoredIcons() {
        M3SVGIcon teal = CatalogIcons.create(CatalogIcons.FAVORITE);
        teal.setSize(M3IconSize.MEDIUM);
        teal.setTint(Color.web("#006A6A"));
        M3SVGIcon red = CatalogIcons.create(CatalogIcons.FAVORITE);
        red.setSize(M3IconSize.MEDIUM);
        red.setTint(Color.web("#9C4146"));
        return CatalogSamples.row(teal, red);
    }

    /// Creates one loading-indicator variant and geometry.
    ///
    /// @param variant the default or contained treatment
    /// @param large whether the large showcase geometry is used
    /// @return the configured loading indicator
    static Node loadingIndicator(M3LoadingIndicatorVariant variant, boolean large) {
        M3LoadingIndicator indicator = new M3LoadingIndicator();
        indicator.setVariant(variant);
        if (large) {
            indicator.setStyle("-m3-container-size: 112px; -m3-indicator-size: 89px;");
            indicator.setMinSize(112.0, 112.0);
            indicator.setPrefSize(112.0, 112.0);
            indicator.setMaxSize(112.0, 112.0);
        }
        return indicator;
    }

    /// Creates one standard or expressive linear or circular progress state.
    ///
    /// @param circular whether the indicator is circular
    /// @param indeterminate whether progress is indeterminate
    /// @param expressive whether wavy expressive geometry is enabled
    /// @return the configured progress indicator
    static Node progress(boolean circular, boolean indeterminate, boolean expressive) {
        if (circular) {
            M3ProgressIndicator indicator = indeterminate
                    ? new M3ProgressIndicator()
                    : new M3ProgressIndicator(0.62);
            indicator.setStyle(expressive
                    ? "-m3-wave-amplitude: 1.6px; -m3-wave-indicator-size: 48px; "
                            + "-m3-wavelength: 15px; -m3-track-gap: 4px;"
                    : "-m3-wave-amplitude: 0px;");
            return indicator;
        }

        M3ProgressBar bar = indeterminate ? new M3ProgressBar() : new M3ProgressBar(0.62);
        bar.setStyle(expressive
                ? "-m3-wave-amplitude: 3px; -m3-wavelength: 40px; "
                        + "-m3-indeterminate-wavelength: 20px; -m3-track-gap: 4px; -m3-stop-size: 4px;"
                : "-m3-wave-amplitude: 0px;");
        return CatalogSamples.configureResponsiveWidth(bar, 420.0);
    }

    /// Creates a snackbar with optional action, close button, and long text.
    ///
    /// @param action whether the snackbar includes a contextual action
    /// @param close whether the explicit close button is visible
    /// @param longText whether the message uses the longer layout
    /// @return the configured snackbar
    static Node snackbar(boolean action, boolean close, boolean longText) {
        M3Snackbar snackbar = new M3Snackbar(longText
                ? "The selected files were archived and remain available from project history."
                : "Message archived");
        if (action) {
            snackbar.setActionText("Undo");
            snackbar.setAction(() -> {
            });
        }
        snackbar.setCloseButtonVisible(close);
        M3OverlayPane overlay = new M3OverlayPane();
        overlay.setContent(new M3Text("Application content", M3TextRole.BODY_MEDIUM));
        overlay.setPrefSize(520.0, 140.0);
        overlay.setMinWidth(0.0);
        overlay.setMaxWidth(520.0);
        overlay.showSnackbar(snackbar);
        return overlay;
    }

    /// Creates a plain tooltip anchor.
    ///
    /// @param longText whether the tooltip contains a longer label
    /// @param iconAnchor whether the anchor is an icon button
    /// @return the tooltip anchor
    static Node plainTooltip(boolean longText, boolean iconAnchor) {
        Node anchor = iconAnchor
                ? CatalogSamples.iconButton(CatalogIcons.NOTIFICATIONS, "Information")
                : new M3Button(longText ? "Long tooltip" : "Hover me", M3ButtonVariant.OUTLINED);
        M3Tooltip tooltip = new M3Tooltip(longText
                ? "Use tooltips for brief contextual labels when a control needs clarification."
                : "Tooltip");
        if (longText) {
            tooltip.setPrefWidth(260.0);
        }
        M3Tooltip.install(anchor, tooltip);
        return anchor;
    }

    /// Creates a transient or persistent rich tooltip anchor.
    ///
    /// @param action whether the rich tooltip includes an action
    /// @param persistent whether the tooltip remains open for interaction
    /// @return the tooltip anchor
    static Node richTooltip(boolean action, boolean persistent) {
        M3Button anchor = new M3Button(
                persistent ? "Persistent rich tooltip" : action ? "Rich action" : "Rich tooltip",
                M3ButtonVariant.TONAL
        );
        M3RichTooltip tooltip = new M3RichTooltip(
                persistent ? "Try keyboard shortcuts" : "Generated theme",
                "Rich tooltips can include a title, supporting text, and optional actions."
        );
        tooltip.setPersistent(persistent);
        if (action) {
            tooltip.getActions().add(new M3Button("Learn more", M3ButtonVariant.TEXT));
        }
        M3Tooltip.install(anchor, tooltip);
        return anchor;
    }

    /// Creates a stack of text roles from one type-scale family.
    ///
    /// @param roles the roles to render
    /// @return the typography stack
    static Node typography(M3TextRole... roles) {
        VBox stack = new VBox(10.0);
        for (M3TextRole role : roles) {
            M3Text text = new M3Text(roleLabel(role), role);
            text.setWrapText(true);
            text.setMinWidth(0.0);
            text.setMaxWidth(Double.MAX_VALUE);
            stack.getChildren().add(text);
        }
        return stack;
    }

    /// Returns a readable title for one type role.
    ///
    /// @param role the type role
    /// @return the display title
    private static String roleLabel(M3TextRole role) {
        String lower = role.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
