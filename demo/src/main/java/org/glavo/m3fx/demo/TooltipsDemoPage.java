// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3Tooltip;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Tooltips component showcase page.
@NotNullByDefault
final class TooltipsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TooltipsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the tooltip component page.
    Node createContent() {
        M3Button plain = new M3Button("Hover me", M3ButtonVariant.FILLED);
        M3Tooltip.install(plain, new M3Tooltip("Tooltip"));

        M3Button longText = new M3Button("Long tooltip", M3ButtonVariant.OUTLINED);
        M3Tooltip tooltip = new M3Tooltip("Use tooltips for brief contextual labels when a control needs clarification.");
        tooltip.setPrefWidth(260.0);
        M3Tooltip.install(longText, tooltip);

        M3IconButton iconButton = createIconButton("info");
        M3Tooltip.install(iconButton, new M3Tooltip("Icon button"));

        M3Button rich = new M3Button("Rich tooltip", M3ButtonVariant.TONAL);
        M3Tooltip.install(
                rich,
                new M3RichTooltip(
                        "Rich tooltip",
                        "Use rich tooltips when brief supporting context needs a title and a wider surface."
                )
        );

        M3Button actionButton = new M3Button("Open", M3ButtonVariant.TEXT);
        actionButton.setOnAction(event -> context.showSnackbar("Theme-aware snackbar"));
        M3Button richAction = new M3Button("Rich action", M3ButtonVariant.OUTLINED);
        M3RichTooltip richActionTooltip = new M3RichTooltip(
                "Generated theme",
                "The tooltip can inherit the owning scene theme and expose action nodes in the content surface."
        );
        richActionTooltip.getActions().add(actionButton);
        M3Tooltip.install(richAction, richActionTooltip);

        M3Button persistent = new M3Button("Persistent rich tooltip", M3ButtonVariant.FILLED);
        M3RichTooltip persistentTooltip = new M3RichTooltip(
                "Try keyboard shortcuts",
                "Persistent rich tooltips open from an explicit click and remain available while their actions are used."
        );
        persistentTooltip.setPersistent(true);
        M3Button learnMore = new M3Button("Learn more", M3ButtonVariant.TEXT);
        M3Button dismiss = new M3Button("Dismiss", M3ButtonVariant.TEXT);
        dismiss.setOnAction(event -> persistentTooltip.hide());
        persistentTooltip.getActions().addAll(learnMore, dismiss);
        M3Tooltip.install(persistent, persistentTooltip);

        return createGallery(
                createShowcaseGroup("Plain", plain, longText, iconButton),
                createShowcaseGroup("Transient Rich", rich, richAction),
                createShowcaseGroup("Persistent Rich", persistent)
        );
    }
}
