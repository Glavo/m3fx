// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Builds the Dialogs component showcase page.
@NotNullByDefault
final class DialogsDemoPage extends DemoPageSupport {
    /// The Material maximum width used by inline dialog-pane previews.
    private static final double MAX_INLINE_DIALOG_PANE_WIDTH = 560.0;

    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    DialogsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the dialog component page.
    Node createContent() {
        M3Button basicButton = new M3Button("Open basic", M3ButtonVariant.FILLED);
        basicButton.setOnAction(event -> showBasicDialog());
        M3Button settingsButton = new M3Button("Open settings", M3ButtonVariant.TONAL);
        settingsButton.setOnAction(event -> showSettingsDialog());
        M3Button destructiveButton = new M3Button("Open destructive", M3ButtonVariant.OUTLINED);
        destructiveButton.setOnAction(event -> showDestructiveDialog());
        M3Button standaloneButton = new M3Button("Open standalone window", M3ButtonVariant.OUTLINED);
        standaloneButton.setOnAction(event -> showStandaloneDialog());

        M3Button basicCancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        basicCancel.setCancelButton(true);
        M3Button basicConfirm = new M3Button("OK", M3ButtonVariant.TEXT);
        basicConfirm.setDefaultButton(true);
        M3DialogPane basicPane = createDialogPreviewPane(
                "Dialog title",
                "The active theme is applied to this dialog pane.",
                basicCancel,
                basicConfirm
        );
        basicPane.setPrefWidth(420.0);

        M3Button settingsCancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        settingsCancel.setCancelButton(true);
        M3Button settingsApply = new M3Button("Apply", M3ButtonVariant.TEXT);
        settingsApply.setDefaultButton(true);
        M3DialogPane settingsPane = createDialogPreviewPane(
                "Project settings",
                null,
                settingsCancel,
                settingsApply
        );
        settingsPane.setContent(createDialogSettingsContent(false));
        settingsPane.setPrefWidth(520.0);

        M3Button close = new M3Button("Close", M3ButtonVariant.TEXT);
        close.setCancelButton(true);
        M3DialogPane longPane = createDialogPreviewPane(
                "Release notes",
                null,
                close
        );
        longPane.setContent(createScrollableDialogContent());
        longPane.setPrefWidth(520.0);

        return createGallery(
                createShowcaseGroup(
                        "Launchers",
                        basicButton,
                        settingsButton,
                        destructiveButton,
                        standaloneButton
                ),
                createFullWidthShowcaseGroup("Inline Panes", basicPane, settingsPane),
                createFullWidthShowcaseGroup("Scrollable Content", longPane)
        );
    }

    /// Opens the basic dialog sample.
    private void showBasicDialog() {
        M3Button confirm = new M3Button("OK", M3ButtonVariant.TEXT);
        confirm.setDefaultButton(true);
        context.showDialog(createDialog(
                "Dialog title",
                "This dialog uses the M3FX dialog pane style and active theme tokens.",
                confirm
        ));
    }

    /// Opens the form dialog sample.
    private void showSettingsDialog() {
        M3Button cancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button apply = new M3Button("Apply", M3ButtonVariant.TEXT);
        apply.setDefaultButton(true);
        M3Dialog dialog = createDialog("Project settings", null, cancel, apply);
        dialog.getDialogPane().setContent(createDialogSettingsContent(true));
        dialog.getDialogPane().setPrefWidth(460.0);
        context.showDialog(dialog);
    }

    /// Opens the destructive confirmation sample.
    private void showDestructiveDialog() {
        M3Button cancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button delete = new M3Button("Delete", M3ButtonVariant.TEXT);
        delete.setDefaultButton(true);
        M3Dialog dialog = createDialog(
                "Delete draft?",
                "Deleting this local draft cannot be undone. Published project files are not affected.",
                cancel,
                delete
        );
        dialog.getDialogPane().setGraphic(createWarningIcon());
        context.showDialog(dialog);
    }

    /// Opens the ownerless native-window sample.
    private void showStandaloneDialog() {
        M3Button close = new M3Button("Close", M3ButtonVariant.TEXT);
        close.setCancelButton(true);
        context.showStandaloneDialog(createDialog(
                "Standalone dialog",
                "This presentation owns a native Stage and does not require an application overlay pane.",
                close
        ));
    }

    /// Creates a dialog with the supplied text and actions.
    ///
    /// @param headerText  the dialog heading
    /// @param contentText the optional supporting text
    /// @param actions     the dialog actions
    /// @return the configured dialog
    private static M3Dialog createDialog(
            String headerText,
            @Nullable String contentText,
            M3Button... actions
    ) {
        M3Dialog dialog = new M3Dialog();
        M3DialogPane pane = dialog.getDialogPane();
        pane.setHeaderText(headerText);
        pane.setContentText(contentText == null ? "" : contentText);
        pane.getActions().addAll(actions);
        return dialog;
    }

    /// Creates the form-like content used by settings dialog samples.
    ///
    /// @param popup whether the content will be shown in a popup dialog
    /// @return the settings content
    private static Node createDialogSettingsContent(boolean popup) {
        M3TextField projectName = createTextField("Project name", "M3FX", M3TextInputVariant.OUTLINED, false);
        configureResponsiveWidth(projectName, popup ? 360.0 : 320.0);
        M3TextInputLayout projectLayout = createTextInputLayout(projectName, "Shown in generated artifacts");
        configureResponsiveWidth(projectLayout, projectName.getPrefWidth());

        M3Switch notifications = new M3Switch("Notify contributors");
        notifications.setSelected(true);
        M3CheckBox rememberChoice = new M3CheckBox("Remember this choice");
        rememberChoice.setSelected(true);

        VBox content = new VBox(12.0, projectLayout, notifications, rememberChoice);
        content.getStyleClass().add("demo-dialog-content");
        content.setMinWidth(0.0);
        content.setMaxWidth(Double.MAX_VALUE);
        return content;
    }

    /// Creates one inline dialog pane preview.
    private static M3DialogPane createDialogPreviewPane(
            String headerText,
            @Nullable String contentText,
            M3Button... actions
    ) {
        M3DialogPane pane = new M3DialogPane();
        pane.getStyleClass().add("demo-dialog-pane");
        pane.setMinWidth(0.0);
        pane.setMaxWidth(MAX_INLINE_DIALOG_PANE_WIDTH);
        pane.setMaxHeight(Region.USE_PREF_SIZE);
        pane.setHeaderText(headerText);
        if (contentText != null) {
            pane.setContentText(contentText);
        }
        pane.getActions().addAll(actions);
        return pane;
    }

    /// Creates scrollable long-form dialog content.
    private static Node createScrollableDialogContent() {
        VBox content = new VBox(8.0);
        content.getStyleClass().add("demo-dialog-scroll-content");
        content.getChildren().addAll(
                new Label("Review theme inheritance, popup focus restoration, and generated runtime packaging."),
                new Label("The dialog body can host regular JavaFX content while M3FX supplies the surrounding surface, actions, shape, and color tokens."),
                new Label("Use this form for dense supporting information that should stay inside a compact modal surface."),
                new Label("Scrolling keeps the action row visible while the body remains inspectable.")
        );
        for (Node node : content.getChildren()) {
            if (node instanceof Label label) {
                label.setWrapText(true);
                label.getStyleClass().add("demo-dialog-body-line");
            }
        }

        M3ScrollPane scrollPane = new M3ScrollPane(content);
        scrollPane.getStyleClass().add("demo-dialog-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(132.0);
        scrollPane.setMinWidth(0.0);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Region.USE_PREF_SIZE);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
