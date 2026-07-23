// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogHandle;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// Presents a multi-step offline task dialog used by install and update flows.
@NotNullByDefault
final class HMCLTaskDialogs {
    /// Prevents utility-class instantiation.
    private HMCLTaskDialogs() {
    }

    /// Runs a cancellable multi-step progress dialog.
    ///
    /// @param overlay the presentation host
    /// @param strings the localization service
    /// @param title the dialog title
    /// @param steps ordered step labels
    /// @param onCompleted called when every step finishes, or `null`
    /// @param onCancelled called when the user cancels, or `null`
    static void run(
            M3OverlayPane overlay,
            HMCLDemoStrings strings,
            String title,
            List<String> steps,
            @Nullable Runnable onCompleted,
            @Nullable Runnable onCancelled
    ) {
        List<String> stepLabels = List.copyOf(steps);
        if (stepLabels.isEmpty()) {
            if (onCompleted != null) {
                onCompleted.run();
            }
            return;
        }

        M3ProgressBar progressBar = new M3ProgressBar();
        progressBar.setProgress(0.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        M3Text status = new M3Text(stepLabels.get(0), M3TextRole.BODY_MEDIUM);
        status.setWrapText(true);
        status.setMaxWidth(Double.MAX_VALUE);

        VBox stepList = new VBox(4.0);
        List<M3ListItem> stepItems = new ArrayList<>(stepLabels.size());
        for (String step : stepLabels) {
            M3ListItem item = new M3ListItem(step);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setMouseTransparent(true);
            item.setFocusTraversable(false);
            stepItems.add(item);
            stepList.getChildren().add(item);
        }

        VBox content = new VBox(12.0, status, progressBar, stepList);
        content.setPadding(new Insets(4.0, 0.0, 0.0, 0.0));
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(Double.MAX_VALUE);

        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(title);
        dialog.getDialogPane().setContent(content);
        dialog.setDismissOnScrimClick(false);

        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        dialog.getDialogPane().getActions().setAll(cancel);

        final boolean[] cancelled = {false};
        final boolean[] completed = {false};
        Timeline timeline = new Timeline();

        M3DialogHandle handle = overlay.showDialog(dialog);
        cancel.setOnAction(event -> {
            cancelled[0] = true;
            timeline.stop();
            handle.requestClose();
            if (onCancelled != null) {
                onCancelled.run();
            }
        });

        double stepSpan = 1.0 / stepLabels.size();
        for (int index = 0; index < stepLabels.size(); index++) {
            int stepIndex = index;
            double start = stepIndex * stepSpan;
            double end = (stepIndex + 1) * stepSpan;
            Duration at = Duration.millis(350.0 + stepIndex * 700.0);
            timeline.getKeyFrames().add(new KeyFrame(at, event -> {
                if (cancelled[0]) {
                    return;
                }
                status.setText(stepLabels.get(stepIndex));
                progressBar.setProgress(end);
                for (int itemIndex = 0; itemIndex < stepItems.size(); itemIndex++) {
                    stepItems.get(itemIndex).setSelected(itemIndex <= stepIndex);
                }
                if (stepIndex == 0) {
                    progressBar.setProgress(Math.max(progressBar.getProgress(), start + 0.05));
                }
            }));
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(350.0 + stepLabels.size() * 700.0), event -> {
            if (cancelled[0]) {
                return;
            }
            completed[0] = true;
            progressBar.setProgress(1.0);
            handle.requestClose();
            if (onCompleted != null) {
                onCompleted.run();
            }
        }));
        timeline.play();

        dialog.setOnHidden(event -> {
            timeline.stop();
            if (!completed[0] && !cancelled[0] && onCancelled != null) {
                onCancelled.run();
            }
        });
    }
}
