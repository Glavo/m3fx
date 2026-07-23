// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3RadioButtonSettingItem;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.controls.M3SelectSettingItem;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// Builds an HMCL-aligned game-settings form from offline state using available M3FX controls.
@NotNullByDefault
final class HMCLGameSettingsForm {
    /// Common windowed resolutions offered by HMCL.
    private static final String @org.jetbrains.annotations.Unmodifiable [] RESOLUTIONS = {
            "854x480", "1280x720", "1600x900", "1920x1080", "2560x1440"
    };

    /// Expanded HMCL-style sublist keys retained across form rebuilds.
    private static final Set<String> EXPANDED_SUBLISTS = ConcurrentHashMap.newKeySet();

    /// Creates a form host.
    private HMCLGameSettingsForm() {
    }

    /// Creates a scrollable game-settings form.
    ///
    /// @param controller the application controller
    /// @param settingsSupplier current settings supplier
    /// @param settingsConsumer settings mutator
    /// @param instanceMode whether the form is for an instance (shows isolation / follow-global)
    /// @return the form root
    static ScrollPane create(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            boolean instanceMode
    ) {
        HMCLDemoStrings strings = controller.strings();
        HMCLDemoState state = controller.state();
        HMCLDemoGameSettings settings = settingsSupplier.get();

        List<Node> blocks = new ArrayList<>();

        if (instanceMode) {
            M3SwitchSettingItem followGlobal = switchItem(
                    strings.get("settings.game.follow_global"),
                    strings.get("settings.game.follow_global.support"),
                    settings.useGlobalPreset(),
                    selected -> settingsConsumer.accept(settingsSupplier.get().withUseGlobalPreset(selected))
            );
            M3SwitchSettingItem isolation = switchItem(
                    strings.get("settings.game.isolation"),
                    strings.get("settings.game.isolation.support"),
                    settings.isolated(),
                    selected -> settingsConsumer.accept(settingsSupplier.get().withIsolated(selected))
            );
            blocks.add(section(strings.get("settings.game.section.basic"), followGlobal, isolation));
        } else {
            M3SelectSettingItem<String> isolation = selectItem(
                    strings.get("settings.game.default_isolation"),
                    List.of("never", "always", "modded"),
                    state.getDefaultIsolation(),
                    value -> isolationLabel(strings, value),
                    value -> {
                        state.setDefaultIsolation(value);
                        settingsConsumer.accept(settingsSupplier.get());
                    }
            );
            blocks.add(section(strings.get("settings.game.section.basic"), isolation));
        }

        blocks.add(javaSection(controller, settingsSupplier, settingsConsumer, settings));
        blocks.add(memorySection(controller, settingsSupplier, settingsConsumer, settings));
        blocks.add(windowSection(controller, settingsSupplier, settingsConsumer, settings));
        blocks.add(launcherSection(controller, settingsSupplier, settingsConsumer, settings));
        blocks.add(quickPlaySection(controller, settingsSupplier, settingsConsumer, settings));
        blocks.add(advancedLaunchSection(controller, settingsSupplier, settingsConsumer, settings));
        blocks.add(jvmSection(controller, settingsSupplier, settingsConsumer, settings));

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().setAll(blocks);
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    private static Node javaSection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        HMCLDemoState state = controller.state();
        List<JavaChoice> choices = buildJavaChoices(strings, state, settings);
        JavaChoice current = resolveJavaChoice(choices, settings);

        M3SelectSettingItem<JavaChoice> select = new M3SelectSettingItem<>(strings.get("settings.game.java_directory"));
        select.setItems(choices);
        select.setConverter(JavaChoice::title);
        select.setDescriptionConverter(JavaChoice::description);
        select.setValue(current);

        // Selecting a special option may open a dialog; keep the menu value and trailing label in sync afterwards.
        select.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || sameJavaChoice(oldValue, newValue)) {
                return;
            }
            applyJavaChoice(controller, settingsSupplier, settingsConsumer, select, newValue);
        });

        return section(strings.get("settings.game.java_directory"), select);
    }

    /// Builds the Java dropdown options: auto, detected runtimes, required major version, and custom path.
    private static List<JavaChoice> buildJavaChoices(
            HMCLDemoStrings strings,
            HMCLDemoState state,
            HMCLDemoGameSettings settings
    ) {
        List<JavaChoice> choices = new ArrayList<>();
        choices.add(new JavaChoice(
                "auto",
                "",
                strings.get("settings.game.java.auto"),
                strings.get("settings.game.java.auto.support")
        ));
        for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
            choices.add(new JavaChoice(
                    "detected",
                    runtime.id(),
                    runtime.name(),
                    runtime.version() + " · " + runtime.path()
            ));
        }
        String versionDescription = settings.javaVersion().isBlank()
                ? strings.get("settings.game.java.version")
                : strings.format("settings.game.java.version.support", settings.javaVersion());
        choices.add(new JavaChoice(
                "version",
                "",
                strings.get("settings.game.java.version"),
                versionDescription
        ));
        String customDescription = settings.javaPath().isBlank()
                ? strings.get("settings.game.java.custom.support")
                : settings.javaPath();
        choices.add(new JavaChoice(
                "custom",
                "",
                strings.get("settings.game.java.custom"),
                customDescription
        ));
        return choices;
    }

    /// Resolves the dropdown value matching the current game-settings Java fields.
    private static JavaChoice resolveJavaChoice(List<JavaChoice> choices, HMCLDemoGameSettings settings) {
        for (JavaChoice choice : choices) {
            if (!choice.mode().equals(settings.javaMode())) {
                continue;
            }
            if ("detected".equals(choice.mode()) && !choice.runtimeId().equals(settings.javaId())) {
                continue;
            }
            return choice;
        }
        return choices.get(0);
    }

    /// Applies a Java dropdown selection, prompting for version or path when needed.
    private static void applyJavaChoice(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            M3SelectSettingItem<JavaChoice> select,
            JavaChoice choice
    ) {
        HMCLDemoStrings strings = controller.strings();
        HMCLDemoGameSettings current = settingsSupplier.get();
        switch (choice.mode()) {
            case "auto" -> settingsConsumer.accept(current.withJava("auto", "", current.javaVersion(), ""));
            case "detected" -> settingsConsumer.accept(
                    current.withJava("detected", choice.runtimeId(), current.javaVersion(), ""));
            case "version" -> openTextDialog(
                    controller,
                    strings.get("settings.game.java.version"),
                    current.javaVersion().isBlank() ? "21" : current.javaVersion(),
                    value -> {
                        String version = value.strip();
                        settingsConsumer.accept(settingsSupplier.get().withJava("version", "", version, ""));
                        refreshJavaChoiceDescriptions(controller, select, settingsSupplier.get());
                    }
            );
            case "custom" -> openTextDialog(
                    controller,
                    strings.get("settings.game.java.custom"),
                    current.javaPath().isBlank()
                            ? "C:\\Program Files\\Java\\bin\\java.exe"
                            : current.javaPath(),
                    value -> {
                        String path = value.strip();
                        settingsConsumer.accept(settingsSupplier.get().withJava("custom", "", current.javaVersion(), path));
                        refreshJavaChoiceDescriptions(controller, select, settingsSupplier.get());
                    }
            );
            default -> {
            }
        }
    }

    /// Rebuilds menu descriptions after a version/path dialog so trailing labels stay current.
    private static void refreshJavaChoiceDescriptions(
            HMCLDemoController controller,
            M3SelectSettingItem<JavaChoice> select,
            HMCLDemoGameSettings settings
    ) {
        List<JavaChoice> choices = buildJavaChoices(controller.strings(), controller.state(), settings);
        JavaChoice current = resolveJavaChoice(choices, settings);
        select.setItems(choices);
        select.setValue(current);
    }

    /// Returns whether two Java menu choices refer to the same logical option.
    private static boolean sameJavaChoice(@Nullable JavaChoice left, JavaChoice right) {
        return left != null
                && left.mode().equals(right.mode())
                && left.runtimeId().equals(right.runtimeId());
    }

    /// One entry in the Java runtime select menu.
    ///
    /// @param mode       `auto`, `detected`, `version`, or `custom`
    /// @param runtimeId  detected runtime id when `mode` is `detected`
    /// @param title      primary menu label
    /// @param description supporting menu text
    private record JavaChoice(String mode, String runtimeId, String title, String description) {
    }

    private static Node memorySection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        ToggleGroup memoryGroup = new ToggleGroup();
        M3Slider slider = new M3Slider();
        slider.setMin(1024);
        slider.setMax(16384);
        slider.setStepSize(256);
        slider.setValue(settings.maxMemoryMb());
        slider.setDisable(settings.autoMemory());
        slider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(slider, Priority.ALWAYS);

        M3Text maxLabel = new M3Text(settings.maxMemoryMb() + " MiB", M3TextRole.BODY_MEDIUM);
        HBox manualRow = new HBox(12.0, slider, maxLabel);
        manualRow.setAlignment(Pos.CENTER_LEFT);
        manualRow.setPadding(new Insets(0.0, 16.0, 8.0, 16.0));
        manualRow.setDisable(settings.autoMemory());

        M3Text status = new M3Text(
                strings.format("settings.memory.status", "16384", String.valueOf(settings.maxMemoryMb())),
                M3TextRole.BODY_SMALL
        );
        status.setPadding(new Insets(0.0, 16.0, 0.0, 16.0));

        Runnable refreshManualUi = () -> {
            HMCLDemoGameSettings current = settingsSupplier.get();
            boolean auto = current.autoMemory();
            slider.setDisable(auto);
            manualRow.setDisable(auto);
            maxLabel.setText(current.maxMemoryMb() + " MiB");
            status.setText(strings.format("settings.memory.status", "16384", String.valueOf(current.maxMemoryMb())));
        };

        M3RadioButtonSettingItem auto = radioItem(
                strings.get("settings.memory.auto"),
                strings.get("settings.memory.auto.support"),
                settings.autoMemory(),
                memoryGroup,
                () -> {
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    settingsConsumer.accept(current.withMemory(
                            true, current.maxMemoryMb(), current.minMemoryMb(), current.metaspaceMb()));
                    refreshManualUi.run();
                }
        );
        M3RadioButtonSettingItem manual = radioItem(
                strings.get("settings.memory.manual"),
                settings.maxMemoryMb() + " MiB",
                !settings.autoMemory(),
                memoryGroup,
                () -> {
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    settingsConsumer.accept(current.withMemory(
                            false, current.maxMemoryMb(), current.minMemoryMb(), current.metaspaceMb()));
                    refreshManualUi.run();
                }
        );

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (settingsSupplier.get().autoMemory()) {
                return;
            }
            int mb = (int) Math.round(newValue.doubleValue() / 256.0) * 256;
            HMCLDemoGameSettings current = settingsSupplier.get();
            settingsConsumer.accept(current.withMemory(
                    false, mb, current.minMemoryMb(), current.metaspaceMb()));
            maxLabel.setText(mb + " MiB");
            manual.setSupportingText(mb + " MiB");
            status.setText(strings.format("settings.memory.status", "16384", String.valueOf(mb)));
        });

        M3SelectSettingItem<Integer> minMemory = selectItem(
                strings.get("settings.memory.min"),
                List.of(256, 512, 1024, 2048),
                settings.minMemoryMb(),
                value -> value + " MiB",
                value -> {
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    settingsConsumer.accept(current.withMemory(
                            current.autoMemory(), current.maxMemoryMb(), value, current.metaspaceMb()));
                }
        );
        M3SelectSettingItem<Integer> metaspace = selectItem(
                strings.get("settings.memory.metaspace"),
                List.of(128, 256, 512, 1024),
                settings.metaspaceMb(),
                value -> value + " MiB",
                value -> {
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    settingsConsumer.accept(current.withMemory(
                            current.autoMemory(), current.maxMemoryMb(), current.minMemoryMb(), value));
                }
        );

        String summary = settings.autoMemory()
                ? strings.get("settings.memory.auto")
                : settings.maxMemoryMb() + " MiB";
        return sublist(
                "memory",
                strings.get("settings.memory"),
                summary,
                auto,
                manual,
                manualRow,
                status,
                minMemory,
                metaspace
        );
    }

    private static Node windowSection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        String summary = switch (settings.windowType()) {
            case "fullscreen" -> strings.get("settings.game.window.fullscreen");
            case "borderless" -> strings.get("settings.game.window.borderless");
            case "maximized" -> strings.get("settings.game.window.maximized");
            default -> strings.get("settings.game.window.windowed") + " · " + settings.resolution();
        };
        ToggleGroup windowGroup = new ToggleGroup();
        return sublist(
                "window",
                strings.get("settings.game.window_type"),
                summary,
                radioItem(strings.get("settings.game.window.windowed"), settings.resolution(),
                        "windowed".equals(settings.windowType()),
                        windowGroup,
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("windowed", settings.resolution()))),
                radioItem(strings.get("settings.game.window.fullscreen"), "",
                        "fullscreen".equals(settings.windowType()),
                        windowGroup,
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("fullscreen", settings.resolution()))),
                radioItem(strings.get("settings.game.window.borderless"), "",
                        "borderless".equals(settings.windowType()),
                        windowGroup,
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("borderless", settings.resolution()))),
                radioItem(strings.get("settings.game.window.maximized"), "",
                        "maximized".equals(settings.windowType()),
                        windowGroup,
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("maximized", settings.resolution()))),
                selectItem(
                        strings.get("settings.game.resolution"),
                        List.of(RESOLUTIONS),
                        settings.resolution(),
                        value -> value,
                        value -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withWindow(current.windowType(), value));
                        })
        );
    }

    private static Node launcherSection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        return section(
                strings.get("settings.launcher"),
                selectItem(
                        strings.get("settings.advanced.launcher_visibility"),
                        List.of("hide", "keep", "close"),
                        settings.launcherVisibility(),
                        value -> visibilityLabel(strings, value),
                        value -> settingsConsumer.accept(settingsSupplier.get().withLauncherVisibility(value))),
                switchItem(strings.get("settings.launcher.allow_auto_agent"),
                        strings.get("settings.launcher.allow_auto_agent.support"),
                        settings.allowAutoAgent(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withFlag(
                                    selected, current.disableAutoGameOptions(), current.showLogs(),
                                    current.enableDebugLog(), current.skipGameCheck()));
                        }),
                switchItem(strings.get("settings.launcher.disable_auto_game_options"),
                        strings.get("settings.launcher.disable_auto_game_options.support"),
                        settings.disableAutoGameOptions(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withFlag(
                                    current.allowAutoAgent(), selected, current.showLogs(),
                                    current.enableDebugLog(), current.skipGameCheck()));
                        }),
                switchItem(strings.get("settings.show_log"),
                        strings.get("settings.show_log.support"),
                        settings.showLogs(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withFlag(
                                    current.allowAutoAgent(), current.disableAutoGameOptions(), selected,
                                    current.enableDebugLog(), current.skipGameCheck()));
                        }),
                switchItem(strings.get("settings.enable_debug_log_output"),
                        strings.get("settings.enable_debug_log_output.support"),
                        settings.enableDebugLog(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withFlag(
                                    current.allowAutoAgent(), current.disableAutoGameOptions(), current.showLogs(),
                                    selected, current.skipGameCheck()));
                        }),
                switchItem(strings.get("settings.advanced.dont_check_game_completeness"),
                        strings.get("settings.advanced.dont_check_game_completeness.support"),
                        settings.skipGameCheck(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withFlag(
                                    current.allowAutoAgent(), current.disableAutoGameOptions(), current.showLogs(),
                                    current.enableDebugLog(), selected));
                        })
        );
    }

    private static Node quickPlaySection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        String summary = switch (settings.quickPlayType()) {
            case "multiplayer" -> settings.quickPlayMultiplayer().isBlank()
                    ? strings.get("settings.game.quick_play.multiplayer")
                    : settings.quickPlayMultiplayer();
            case "singleplayer" -> settings.quickPlaySingleplayer().isBlank()
                    ? strings.get("settings.game.quick_play.singleplayer")
                    : settings.quickPlaySingleplayer();
            case "realms" -> settings.quickPlayRealms().isBlank()
                    ? strings.get("settings.game.quick_play.realms")
                    : settings.quickPlayRealms();
            default -> strings.get("settings.game.quick_play.none");
        };
        ToggleGroup quickPlayGroup = new ToggleGroup();
        return sublist(
                "quick-play",
                strings.get("settings.game.quick_play"),
                summary,
                radioItem(strings.get("settings.game.quick_play.none"), "",
                        "none".equals(settings.quickPlayType()),
                        quickPlayGroup,
                        () -> settingsConsumer.accept(settingsSupplier.get().withQuickPlay(
                                "none", settings.quickPlayMultiplayer(), settings.quickPlaySingleplayer(),
                                settings.quickPlayRealms()))),
                radioItem(strings.get("settings.game.quick_play.multiplayer"),
                        settings.quickPlayMultiplayer().isBlank()
                                ? strings.get("settings.game.quick_play.multiplayer.support")
                                : settings.quickPlayMultiplayer(),
                        "multiplayer".equals(settings.quickPlayType()),
                        quickPlayGroup,
                        () -> openTextDialog(controller, strings.get("settings.game.quick_play.multiplayer"),
                                settings.quickPlayMultiplayer().isBlank() ? "localhost:25565" : settings.quickPlayMultiplayer(),
                                value -> settingsConsumer.accept(settingsSupplier.get().withQuickPlay(
                                        "multiplayer", value.strip(), settings.quickPlaySingleplayer(),
                                        settings.quickPlayRealms())))),
                radioItem(strings.get("settings.game.quick_play.singleplayer"),
                        settings.quickPlaySingleplayer().isBlank()
                                ? strings.get("settings.game.quick_play.singleplayer.support")
                                : settings.quickPlaySingleplayer(),
                        "singleplayer".equals(settings.quickPlayType()),
                        quickPlayGroup,
                        () -> openTextDialog(controller, strings.get("settings.game.quick_play.singleplayer"),
                                settings.quickPlaySingleplayer().isBlank() ? "New World" : settings.quickPlaySingleplayer(),
                                value -> settingsConsumer.accept(settingsSupplier.get().withQuickPlay(
                                        "singleplayer", settings.quickPlayMultiplayer(), value.strip(),
                                        settings.quickPlayRealms())))),
                radioItem(strings.get("settings.game.quick_play.realms"),
                        settings.quickPlayRealms().isBlank()
                                ? strings.get("settings.game.quick_play.realms.support")
                                : settings.quickPlayRealms(),
                        "realms".equals(settings.quickPlayType()),
                        quickPlayGroup,
                        () -> openTextDialog(controller, strings.get("settings.game.quick_play.realms"),
                                settings.quickPlayRealms().isBlank() ? "realm-id" : settings.quickPlayRealms(),
                                value -> settingsConsumer.accept(settingsSupplier.get().withQuickPlay(
                                        "realms", settings.quickPlayMultiplayer(), settings.quickPlaySingleplayer(),
                                        value.strip()))))
        );
    }

    private static Node advancedLaunchSection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        return sublist(
                "advanced-launch",
                strings.get("settings.advanced.launch_options"),
                strings.get("settings.advanced.launch_options.subtitle"),
                // Path picker deferred: M3FX has no directory FileSelector control; text dialog is used instead.
                textAction(strings.get("settings.game.running_directory"),
                        settings.runningDirectory().isBlank()
                                ? strings.get("settings.game.running_directory.support")
                                : settings.runningDirectory(),
                        () -> openTextDialog(controller, strings.get("settings.game.running_directory"),
                                settings.runningDirectory(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withAdvancedLaunch(
                                            value.strip(), current.gameArguments(), current.environmentVariables(),
                                            current.processPriority()));
                                })),
                textAction(strings.get("settings.advanced.minecraft_arguments"),
                        settings.gameArguments().isBlank()
                                ? strings.get("settings.advanced.minecraft_arguments.support")
                                : settings.gameArguments(),
                        () -> openTextDialog(controller, strings.get("settings.advanced.minecraft_arguments"),
                                settings.gameArguments(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withAdvancedLaunch(
                                            current.runningDirectory(), value, current.environmentVariables(),
                                            current.processPriority()));
                                })),
                textAction(strings.get("settings.advanced.environment_variables"),
                        settings.environmentVariables().isBlank()
                                ? strings.get("settings.advanced.environment_variables.support")
                                : settings.environmentVariables(),
                        () -> openTextDialog(controller, strings.get("settings.advanced.environment_variables"),
                                settings.environmentVariables(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withAdvancedLaunch(
                                            current.runningDirectory(), current.gameArguments(), value,
                                            current.processPriority()));
                                })),
                selectItem(
                        strings.get("settings.advanced.process_priority"),
                        List.of("high", "normal", "low"),
                        settings.processPriority(),
                        value -> priorityLabel(strings, value),
                        value -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withAdvancedLaunch(
                                    current.runningDirectory(), current.gameArguments(),
                                    current.environmentVariables(), value));
                        })
        );
    }

    private static Node jvmSection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        return section(
                strings.get("settings.advanced.jvm"),
                switchItem(strings.get("settings.advanced.no_jvm_args"),
                        strings.get("settings.advanced.no_jvm_args.support"),
                        settings.noJvmArgs(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withJvm(
                                    selected, current.noOptimizingJvmArgs(), current.skipJvmCheck(),
                                    current.jvmArgs(), current.wrapper(), current.preLaunch(), current.postExit()));
                        }),
                switchItem(strings.get("settings.advanced.no_optimizing_jvm_args"),
                        strings.get("settings.advanced.no_optimizing_jvm_args.support"),
                        settings.noOptimizingJvmArgs(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withJvm(
                                    current.noJvmArgs(), selected, current.skipJvmCheck(),
                                    current.jvmArgs(), current.wrapper(), current.preLaunch(), current.postExit()));
                        }),
                switchItem(strings.get("settings.advanced.dont_check_jvm_validity"),
                        strings.get("settings.advanced.dont_check_jvm_validity.support"),
                        settings.skipJvmCheck(),
                        selected -> {
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withJvm(
                                    current.noJvmArgs(), current.noOptimizingJvmArgs(), selected,
                                    current.jvmArgs(), current.wrapper(), current.preLaunch(), current.postExit()));
                        }),
                textAction(strings.get("settings.advanced.jvm_args"),
                        settings.jvmArgs().isBlank()
                                ? strings.get("settings.advanced.jvm_args.support")
                                : settings.jvmArgs(),
                        () -> openTextDialog(controller, strings.get("settings.advanced.jvm_args"),
                                settings.jvmArgs(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withJvm(
                                            current.noJvmArgs(), current.noOptimizingJvmArgs(), current.skipJvmCheck(),
                                            value, current.wrapper(), current.preLaunch(), current.postExit()));
                                })),
                textAction(strings.get("settings.global.wrapper"),
                        settings.wrapper().isBlank()
                                ? strings.get("settings.global.wrapper.support")
                                : settings.wrapper(),
                        () -> openTextDialog(controller, strings.get("settings.global.wrapper"),
                                settings.wrapper(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withJvm(
                                            current.noJvmArgs(), current.noOptimizingJvmArgs(), current.skipJvmCheck(),
                                            current.jvmArgs(), value, current.preLaunch(), current.postExit()));
                                })),
                textAction(strings.get("settings.global.pre_launch"),
                        settings.preLaunch().isBlank()
                                ? strings.get("settings.global.pre_launch.support")
                                : settings.preLaunch(),
                        () -> openTextDialog(controller, strings.get("settings.global.pre_launch"),
                                settings.preLaunch(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withJvm(
                                            current.noJvmArgs(), current.noOptimizingJvmArgs(), current.skipJvmCheck(),
                                            current.jvmArgs(), current.wrapper(), value, current.postExit()));
                                })),
                textAction(strings.get("settings.global.post_exit"),
                        settings.postExit().isBlank()
                                ? strings.get("settings.global.post_exit.support")
                                : settings.postExit(),
                        () -> openTextDialog(controller, strings.get("settings.global.post_exit"),
                                settings.postExit(),
                                value -> {
                                    HMCLDemoGameSettings current = settingsSupplier.get();
                                    settingsConsumer.accept(current.withJvm(
                                            current.noJvmArgs(), current.noOptimizingJvmArgs(), current.skipJvmCheck(),
                                            current.jvmArgs(), current.wrapper(), current.preLaunch(), value));
                                }))
        );
    }

    private static VBox section(String title, Node... items) {
        M3ListSectionHeader header = new M3ListSectionHeader(title);
        VBox body = settingBody(items);
        VBox block = new VBox(8.0, header, body);
        block.setMinHeight(0.0);
        return block;
    }

    /// Creates an HMCL `ComponentSublist`-style block: collapsed header with summary, expandable body.
    ///
    /// @param key stable expand-state key
    /// @param title the sublist title
    /// @param subtitle the collapsed summary text
    /// @param items expanded body children
    /// @return the sublist node
    private static VBox sublist(String key, String title, String subtitle, Node... items) {
        boolean expanded = EXPANDED_SUBLISTS.contains(key);
        M3SVGIcon chevron = HMCLDemoIcons.create(HMCLDemoIcons.EXPAND_MORE);
        chevron.setRotate(expanded ? 180.0 : 0.0);

        M3ListItem header = new M3ListItem(title);
        header.getStyleClass().add("hmcl-settings-sublist-header");
        header.setSupportingText(subtitle);
        header.setTrailing(chevron);
        header.setMaxWidth(Double.MAX_VALUE);

        VBox body = settingBody(items);
        body.getStyleClass().add("hmcl-settings-sublist-body");
        body.setVisible(expanded);
        body.setManaged(expanded);

        header.setOnAction(event -> {
            boolean next = !body.isVisible();
            body.setVisible(next);
            body.setManaged(next);
            chevron.setRotate(next ? 180.0 : 0.0);
            if (next) {
                EXPANDED_SUBLISTS.add(key);
            } else {
                EXPANDED_SUBLISTS.remove(key);
            }
        });

        VBox block = new VBox(0.0, header, body);
        block.getStyleClass().add("hmcl-settings-sublist");
        block.setMinHeight(0.0);
        block.setMaxWidth(Double.MAX_VALUE);
        return block;
    }

    /// Builds the body of a settings section or expanded sublist.
    ///
    /// @param items mixed setting rows and free-form nodes
    /// @return the body container
    private static VBox settingBody(Node... items) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        list.setMinHeight(0.0);
        List<Node> listItems = new ArrayList<>();
        List<Node> extra = new ArrayList<>();
        for (Node item : items) {
            if (item instanceof M3SettingItem
                    || item instanceof M3SwitchSettingItem
                    || item instanceof M3RadioButtonSettingItem
                    || item instanceof M3SelectSettingItem<?>) {
                listItems.add(item);
            } else {
                extra.add(item);
            }
        }
        list.getItems().setAll(listItems);
        VBox body = new VBox(8.0, list);
        body.getChildren().addAll(extra);
        body.setMinHeight(0.0);
        body.setMaxWidth(Double.MAX_VALUE);
        return body;
    }

    private static M3SwitchSettingItem switchItem(
            String title,
            String support,
            boolean selected,
            Consumer<Boolean> onChange
    ) {
        M3SwitchSettingItem item = new M3SwitchSettingItem(title);
        item.setSupportingText(support);
        item.setSelected(selected);
        item.selectedProperty().addListener((observable, oldValue, newValue) ->
                onChange.accept(Boolean.TRUE.equals(newValue)));
        return item;
    }

    private static M3RadioButtonSettingItem radioItem(
            String title,
            String support,
            boolean selected,
            ToggleGroup group,
            Runnable onSelect
    ) {
        M3RadioButtonSettingItem item = new M3RadioButtonSettingItem(title);
        if (!support.isBlank()) {
            item.setSupportingText(support);
        }
        item.setToggleGroup(group);
        item.setSelected(selected);
        item.setOnAction(event -> onSelect.run());
        return item;
    }

    private static M3SettingItem cycleItem(
            String title,
            String support,
            Consumer<M3SettingItem> onAction
    ) {
        M3SettingItem item = new M3SettingItem(title);
        item.setSupportingText(support);
        item.setOnAction(event -> onAction.accept(item));
        return item;
    }

    private static M3SettingItem cycleItem(String title, String support, Runnable onAction) {
        return cycleItem(title, support, item -> onAction.run());
    }

    private static <T> M3SelectSettingItem<T> selectItem(
            String title,
            List<T> choices,
            T value,
            java.util.function.Function<T, String> converter,
            Consumer<T> onChange
    ) {
        M3SelectSettingItem<T> item = new M3SelectSettingItem<>(title);
        item.setItems(choices);
        item.setConverter(converter);
        item.setValue(value);
        item.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !java.util.Objects.equals(oldValue, newValue)) {
                onChange.accept(newValue);
            }
        });
        return item;
    }

    private static M3SettingItem textAction(String title, String support, Runnable onAction) {
        return cycleItem(title, support, onAction);
    }

    private static void openTextDialog(
            HMCLDemoController controller,
            String title,
            String initial,
            Consumer<String> onApply
    ) {
        M3TextField field = new M3TextField(initial);
        M3TextInputLayout layout = new M3TextInputLayout(field);
        layout.setLabelText(title);
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(title);
        dialog.getDialogPane().setContent(layout);
        M3Button cancel = new M3Button(controller.strings().get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button apply = new M3Button(controller.strings().get("common.apply"), M3ButtonVariant.TEXT);
        apply.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, apply);
        apply.setOnAction(event -> onApply.accept(field.getText()));
        controller.overlay().showDialog(dialog);
    }

    private static String isolationLabel(HMCLDemoStrings strings, String isolation) {
        return strings.get(switch (isolation) {
            case "never" -> "settings.isolation.never";
            case "always" -> "settings.isolation.always";
            default -> "settings.isolation.modded";
        });
    }

    private static String visibilityLabel(HMCLDemoStrings strings, String visibility) {
        return strings.get(switch (visibility) {
            case "keep" -> "settings.visibility.keep";
            case "close" -> "settings.visibility.close";
            default -> "settings.visibility.hide";
        });
    }

    private static String priorityLabel(HMCLDemoStrings strings, String priority) {
        return strings.get(switch (priority) {
            case "high" -> "settings.priority.high";
            case "low" -> "settings.priority.low";
            default -> "settings.priority.normal";
        });
    }
}
