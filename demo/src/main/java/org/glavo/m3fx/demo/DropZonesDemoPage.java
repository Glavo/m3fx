// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3DropZone;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;

/// Builds the Drop Zones extension showcase page.
@NotNullByDefault
final class DropZonesDemoPage extends DemoPageSupport {
    /// The maximum accepted launcher-profile size in bytes.
    private static final long MAX_PROFILE_SIZE = 10L * 1024L * 1024L;

    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    DropZonesDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the drop-zone extension page.
    Node createContent() {
        M3DropZone empty = createDropZone(
                "Drop a launcher profile here",
                "JSON files up to 10 MB",
                "Choose file",
                false,
                false,
                "demo-drop-zone-empty"
        );
        M3DropZone filled = createDropZone(
                "Launcher profile imported",
                "profile.json is ready to use",
                "Replace file",
                true,
                false,
                "demo-drop-zone-filled"
        );
        M3DropZone disabled = createDropZone(
                "Drop zone unavailable",
                "File import is disabled",
                "Choose file",
                false,
                true,
                "demo-drop-zone-disabled"
        );

        return createGallery(
                createShowcaseGroup("States", empty, filled),
                createShowcaseGroup("Unavailable", disabled)
        );
    }

    /// Creates one file-oriented drop-zone sample.
    ///
    /// @param titleText the initial title
    /// @param supportingText the initial supporting text
    /// @param actionText the alternative keyboard action label
    /// @param filled whether the zone initially presents accepted content
    /// @param disabled whether the zone rejects interaction
    /// @param stateStyleClass the demo style class identifying the initial state
    /// @return the configured drop zone
    private M3DropZone createDropZone(
            String titleText,
            String supportingText,
            String actionText,
            boolean filled,
            boolean disabled,
            String stateStyleClass
    ) {
        Node icon = createIconViewport(DemoIcons.primary(filled ? "done" : "upload"));
        M3Text title = new M3Text(titleText, M3TextRole.TITLE_MEDIUM);
        M3Text supporting = new M3Text(supportingText, M3TextRole.BODY_MEDIUM);
        M3Button action = new M3Button(actionText, M3ButtonVariant.TONAL);

        VBox message = new VBox(10.0, icon, title, supporting, action);
        message.getStyleClass().add("demo-drop-zone-message");
        message.setAlignment(Pos.CENTER);

        M3DropZone dropZone = new M3DropZone(message);
        dropZone.getStyleClass().addAll("demo-drop-zone", stateStyleClass);
        dropZone.setAcceptancePredicate(event -> event.getDragboard().hasFiles()
                && event.getDragboard().getFiles().stream().allMatch(DropZonesDemoPage::isSupportedProfile));
        dropZone.setFilled(filled);
        dropZone.setDisable(disabled);
        configureResponsiveWidth(dropZone, 400.0);
        action.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose launcher profile");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
            @Nullable File file = chooser.showOpenDialog(action.getScene().getWindow());
            if (file != null && isSupportedProfile(file)) {
                showImportedFiles(dropZone, title, supporting, action, 1, file.getName() + " is ready to use");
                context.showSnackbar("Imported " + file.getName());
            } else if (file != null) {
                context.showSnackbar("Choose a JSON file no larger than 10 MB");
            }
        });
        dropZone.setOnDragDropped(event -> {
            int fileCount = event.getDragboard().getFiles().size();
            boolean accepted = fileCount > 0
                    && event.getDragboard().getFiles().stream().allMatch(DropZonesDemoPage::isSupportedProfile);
            if (accepted) {
                showImportedFiles(
                        dropZone,
                        title,
                        supporting,
                        action,
                        fileCount,
                        "Drop completed successfully"
                );
                context.showSnackbar(title.getText());
            }
            event.setDropCompleted(accepted);
        });
        return dropZone;
    }

    /// Returns whether one file satisfies the launcher-profile demo policy.
    ///
    /// @param file the candidate file
    /// @return `true` for a regular JSON file no larger than 10 MiB
    private static boolean isSupportedProfile(File file) {
        return file.isFile()
                && file.length() <= MAX_PROFILE_SIZE
                && file.getName().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    /// Updates one zone after files are selected or dropped successfully.
    ///
    /// @param dropZone the zone that accepted the files
    /// @param title the mutable status title
    /// @param supporting the mutable supporting text
    /// @param action the alternative file-selection action
    /// @param fileCount the positive number of accepted files
    /// @param supportingText the new supporting message
    /// @throws IllegalArgumentException if `fileCount` is not positive
    private static void showImportedFiles(
            M3DropZone dropZone,
            M3Text title,
            M3Text supporting,
            M3Button action,
            int fileCount,
            String supportingText
    ) {
        if (fileCount <= 0) {
            throw new IllegalArgumentException("fileCount must be positive: " + fileCount);
        }
        title.setText(fileCount == 1 ? "1 file imported" : fileCount + " files imported");
        supporting.setText(supportingText);
        action.setText("Replace file");
        dropZone.setFilled(true);
    }
}
