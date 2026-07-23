// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogHandle;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Multi-step install wizard presented as an in-scene Material dialog.
@NotNullByDefault
final class HMCLInstallWizard {
    /// Prevents utility-class instantiation.
    private HMCLInstallWizard() {
    }

    /// Shows the install wizard for `version`.
    ///
    /// @param controller the application controller
    /// @param version the Minecraft version to install
    static void show(HMCLDemoController controller, HMCLDemoMinecraftVersion version) {
        HMCLDemoStrings strings = controller.strings();
        HMCLDemoState state = controller.state();

        M3AnimatedContent stepHost = new M3AnimatedContent();
        stepHost.setFitToWidth(true);
        stepHost.setMinHeight(180.0);
        stepHost.setMaxWidth(Double.MAX_VALUE);

        M3TextField nameField = new M3TextField(version.name());
        M3TextInputLayout nameLayout = new M3TextInputLayout(nameField);
        nameLayout.setLabelText(strings.get("download.install.name"));
        nameLayout.setMaxWidth(Double.MAX_VALUE);

        M3FilterChip noneChip = new M3FilterChip("Vanilla");
        M3FilterChip fabricChip = new M3FilterChip("Fabric");
        M3FilterChip forgeChip = new M3FilterChip("Forge");
        M3FilterChip quiltChip = new M3FilterChip("Quilt");
        noneChip.setSelected(true);
        FlowPane loaders = new FlowPane(8.0, 8.0, noneChip, fabricChip, forgeChip, quiltChip);
        loaders.setMaxWidth(Double.MAX_VALUE);

        Runnable selectNone = () -> {
            noneChip.setSelected(true);
            fabricChip.setSelected(false);
            forgeChip.setSelected(false);
            quiltChip.setSelected(false);
        };
        noneChip.setOnAction(event -> selectNone.run());
        fabricChip.setOnAction(event -> {
            noneChip.setSelected(false);
            fabricChip.setSelected(true);
            forgeChip.setSelected(false);
            quiltChip.setSelected(false);
        });
        forgeChip.setOnAction(event -> {
            noneChip.setSelected(false);
            fabricChip.setSelected(false);
            forgeChip.setSelected(true);
            quiltChip.setSelected(false);
        });
        quiltChip.setOnAction(event -> {
            noneChip.setSelected(false);
            fabricChip.setSelected(false);
            forgeChip.setSelected(false);
            quiltChip.setSelected(true);
        });

        M3Text summary = new M3Text("", M3TextRole.BODY_MEDIUM);
        summary.setWrapText(true);
        summary.setMaxWidth(Double.MAX_VALUE);

        final int[] step = {0};
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.format("download.install.title", version.name()));
        dialog.getDialogPane().setContent(stepHost);
        dialog.setDismissOnScrimClick(false);

        M3Button back = new M3Button(strings.get("common.back"), M3ButtonVariant.TEXT);
        M3Button next = new M3Button(strings.get("wizard.next"), M3ButtonVariant.TEXT);
        next.setDefaultButton(true);
        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, back, next);

        M3DialogHandle handle = controller.overlay().showDialog(dialog);

        Runnable render = () -> {
            stepHost.setContentTransform(controller.state().isAnimationDisabled()
                    ? HMCLDemoTransitions.none()
                    : (step[0] == 0 ? HMCLDemoTransitions.none() : HMCLDemoTransitions.forward()));
            if (step[0] == 0) {
                stepHost.setContent(stepPane(
                        new M3Text(strings.format("download.install.title", version.name()), M3TextRole.TITLE_SMALL),
                        nameLayout
                ));
                back.setDisable(true);
                next.setText(strings.get("wizard.next"));
            } else if (step[0] == 1) {
                stepHost.setContent(stepPane(
                        new M3Text(strings.get("wizard.loader.title"), M3TextRole.TITLE_SMALL),
                        new M3Text(strings.get("wizard.loader.body"), M3TextRole.BODY_MEDIUM),
                        loaders
                ));
                back.setDisable(false);
                next.setText(strings.get("wizard.next"));
            } else {
                @Nullable String loaderId = selectedLoaderId(noneChip, fabricChip, forgeChip, quiltChip);
                String loaderLabel = loaderId == null ? "Vanilla" : loaderId;
                summary.setText(strings.format(
                        "wizard.summary.body",
                        nameField.getText().isBlank() ? version.name() : nameField.getText().strip(),
                        version.name(),
                        loaderLabel
                ));
                stepHost.setContent(stepPane(
                        new M3Text(strings.get("wizard.summary.title"), M3TextRole.TITLE_SMALL),
                        summary
                ));
                back.setDisable(false);
                next.setText(strings.get("wizard.install"));
            }
            if (controller.state().isAnimationDisabled()) {
                stepHost.snapToCurrentState();
            }
        };

        back.setOnAction(event -> {
            if (step[0] > 0) {
                step[0]--;
                stepHost.setContentTransform(controller.state().isAnimationDisabled()
                        ? HMCLDemoTransitions.none()
                        : HMCLDemoTransitions.backward());
                render.run();
            }
        });
        next.setOnAction(event -> {
            if (step[0] < 2) {
                step[0]++;
                render.run();
                return;
            }
            handle.requestClose();
            @Nullable String loaderId = selectedLoaderId(noneChip, fabricChip, forgeChip, quiltChip);
            @Nullable String loaderVersion = loaderId == null ? null : defaultLoaderVersion(loaderId);
            String instanceName = nameField.getText().isBlank() ? version.name() : nameField.getText().strip();
            controller.runTask(
                    strings.get("download.progress.title"),
                    java.util.List.of(
                            strings.get("wizard.step.client"),
                            strings.get("wizard.step.libraries"),
                            strings.get("wizard.step.assets"),
                            strings.get("wizard.step.finalize")
                    ),
                    () -> {
                        HMCLDemoInstance installed = state.installInstance(
                                instanceName,
                                version.name(),
                                loaderId,
                                loaderVersion
                        );
                        controller.showMessageKey("snackbar.installed", installed.name());
                        controller.openInstance(installed.id(), HMCLDemoRoute.InstanceSection.SETTINGS);
                    },
                    () -> controller.showMessageKey("snackbar.install_cancelled")
            );
        });
        cancel.setOnAction(event -> handle.requestClose());
        render.run();
    }

    /// Creates a vertical wizard step pane.
    ///
    /// @param children the step children
    /// @return the pane
    private static Node stepPane(Node... children) {
        VBox pane = new VBox(12.0, children);
        pane.setAlignment(Pos.TOP_LEFT);
        pane.setPadding(new Insets(4.0, 0.0, 0.0, 0.0));
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    /// Returns the selected loader id, or `null` for vanilla.
    private static @Nullable String selectedLoaderId(
            M3FilterChip none,
            M3FilterChip fabric,
            M3FilterChip forge,
            M3FilterChip quilt
    ) {
        if (fabric.isSelected()) {
            return "fabric";
        }
        if (forge.isSelected()) {
            return "forge";
        }
        if (quilt.isSelected()) {
            return "quilt";
        }
        none.setSelected(true);
        return null;
    }

    /// Returns a deterministic loader version label.
    ///
    /// @param loaderId the loader family
    /// @return the version label
    private static String defaultLoaderVersion(String loaderId) {
        return switch (loaderId) {
            case "fabric" -> "0.16.14";
            case "forge" -> "52.0.0";
            case "quilt" -> "0.27.0";
            default -> "1.0.0";
        };
    }
}
