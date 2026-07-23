// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
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
            M3SettingItem isolation = cycleItem(
                    strings.get("settings.game.default_isolation"),
                    isolationLabel(strings, state.getDefaultIsolation()),
                    () -> {
                        String next = switch (state.getDefaultIsolation()) {
                            case "never" -> "always";
                            case "always" -> "modded";
                            default -> "never";
                        };
                        state.setDefaultIsolation(next);
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
        List<Node> rows = new ArrayList<>();
        rows.add(radioItem(
                strings.get("settings.game.java.auto"),
                strings.get("settings.game.java.auto.support"),
                "auto".equals(settings.javaMode()),
                () -> settingsConsumer.accept(settingsSupplier.get().withJava("auto", "", settings.javaVersion(), ""))
        ));
        rows.add(radioItem(
                strings.get("settings.game.java.version"),
                strings.format("settings.game.java.version.support", settings.javaVersion()),
                "version".equals(settings.javaMode()),
                () -> openTextDialog(
                        controller,
                        strings.get("settings.game.java.version"),
                        settings.javaVersion(),
                        value -> settingsConsumer.accept(
                                settingsSupplier.get().withJava("version", "", value.strip(), ""))
                )
        ));
        for (HMCLDemoJavaRuntime runtime : controller.state().getJavaRuntimes()) {
            boolean selected = "detected".equals(settings.javaMode()) && runtime.id().equals(settings.javaId());
            rows.add(radioItem(
                    runtime.name(),
                    runtime.version() + " · " + runtime.path(),
                    selected,
                    () -> settingsConsumer.accept(
                            settingsSupplier.get().withJava("detected", runtime.id(), settings.javaVersion(), ""))
            ));
        }
        rows.add(radioItem(
                strings.get("settings.game.java.custom"),
                settings.javaPath().isBlank()
                        ? strings.get("settings.game.java.custom.support")
                        : settings.javaPath(),
                "custom".equals(settings.javaMode()),
                () -> openTextDialog(
                        controller,
                        strings.get("settings.game.java.custom"),
                        settings.javaPath().isBlank() ? "C:\\Program Files\\Java\\bin\\java.exe" : settings.javaPath(),
                        value -> settingsConsumer.accept(
                                settingsSupplier.get().withJava("custom", "", settings.javaVersion(), value.strip()))
                )
        ));
        // FileChooser-backed path pick is deferred: M3FX has no path-selector control.
        return sublist(
                "java",
                strings.get("settings.game.java_directory"),
                javaSummary(strings, settings, controller.state()),
                rows.toArray(Node[]::new)
        );
    }

    private static String javaSummary(
            HMCLDemoStrings strings,
            HMCLDemoGameSettings settings,
            HMCLDemoState state
    ) {
        return switch (settings.javaMode()) {
            case "version" -> strings.format("settings.game.java.version.support", settings.javaVersion());
            case "custom" -> settings.javaPath().isBlank()
                    ? strings.get("settings.game.java.custom")
                    : settings.javaPath();
            case "detected" -> {
                for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
                    if (runtime.id().equals(settings.javaId())) {
                        yield runtime.name() + " · " + runtime.version();
                    }
                }
                yield strings.get("settings.game.java.auto");
            }
            default -> strings.get("settings.game.java.auto");
        };
    }

    private static Node memorySection(
            HMCLDemoController controller,
            Supplier<HMCLDemoGameSettings> settingsSupplier,
            Consumer<HMCLDemoGameSettings> settingsConsumer,
            HMCLDemoGameSettings settings
    ) {
        HMCLDemoStrings strings = controller.strings();
        M3RadioButtonSettingItem auto = radioItem(
                strings.get("settings.memory.auto"),
                strings.get("settings.memory.auto.support"),
                settings.autoMemory(),
                () -> settingsConsumer.accept(settingsSupplier.get().withMemory(
                        true, settings.maxMemoryMb(), settings.minMemoryMb(), settings.metaspaceMb()))
        );
        M3RadioButtonSettingItem manual = radioItem(
                strings.get("settings.memory.manual"),
                settings.maxMemoryMb() + " MiB",
                !settings.autoMemory(),
                () -> settingsConsumer.accept(settingsSupplier.get().withMemory(
                        false, settings.maxMemoryMb(), settings.minMemoryMb(), settings.metaspaceMb()))
        );

        M3Slider slider = new M3Slider();
        slider.setMin(1024);
        slider.setMax(16384);
        slider.setStepSize(256);
        slider.setValue(settings.maxMemoryMb());
        slider.setDisable(settings.autoMemory());
        slider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(slider, Priority.ALWAYS);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (settingsSupplier.get().autoMemory()) {
                return;
            }
            int mb = (int) Math.round(newValue.doubleValue() / 256.0) * 256;
            settingsConsumer.accept(settingsSupplier.get().withMemory(
                    false, mb, settingsSupplier.get().minMemoryMb(), settingsSupplier.get().metaspaceMb()));
        });

        M3Text maxLabel = new M3Text(settings.maxMemoryMb() + " MiB", M3TextRole.BODY_MEDIUM);
        HBox manualRow = new HBox(12.0, slider, maxLabel);
        manualRow.setAlignment(Pos.CENTER_LEFT);
        manualRow.setPadding(new Insets(0.0, 16.0, 8.0, 16.0));
        manualRow.setDisable(settings.autoMemory());

        M3SettingItem minMemory = cycleItem(
                strings.get("settings.memory.min"),
                settings.minMemoryMb() + " MiB",
                () -> {
                    int next = switch (settingsSupplier.get().minMemoryMb()) {
                        case 256 -> 512;
                        case 512 -> 1024;
                        case 1024 -> 2048;
                        default -> 256;
                    };
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    settingsConsumer.accept(current.withMemory(
                            current.autoMemory(), current.maxMemoryMb(), next, current.metaspaceMb()));
                }
        );
        M3SettingItem metaspace = cycleItem(
                strings.get("settings.memory.metaspace"),
                settings.metaspaceMb() + " MiB",
                () -> {
                    int next = switch (settingsSupplier.get().metaspaceMb()) {
                        case 128 -> 256;
                        case 256 -> 512;
                        case 512 -> 1024;
                        default -> 128;
                    };
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    settingsConsumer.accept(current.withMemory(
                            current.autoMemory(), current.maxMemoryMb(), current.minMemoryMb(), next));
                }
        );

        M3Text status = new M3Text(
                strings.format("settings.memory.status", "16384", String.valueOf(settings.maxMemoryMb())),
                M3TextRole.BODY_SMALL
        );
        status.setPadding(new Insets(0.0, 16.0, 0.0, 16.0));

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
        return sublist(
                "window",
                strings.get("settings.game.window_type"),
                summary,
                radioItem(strings.get("settings.game.window.windowed"), settings.resolution(),
                        "windowed".equals(settings.windowType()),
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("windowed", settings.resolution()))),
                radioItem(strings.get("settings.game.window.fullscreen"), "",
                        "fullscreen".equals(settings.windowType()),
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("fullscreen", settings.resolution()))),
                radioItem(strings.get("settings.game.window.borderless"), "",
                        "borderless".equals(settings.windowType()),
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("borderless", settings.resolution()))),
                radioItem(strings.get("settings.game.window.maximized"), "",
                        "maximized".equals(settings.windowType()),
                        () -> settingsConsumer.accept(settingsSupplier.get().withWindow("maximized", settings.resolution()))),
                cycleItem(strings.get("settings.game.resolution"), settings.resolution(), () -> {
                    HMCLDemoGameSettings current = settingsSupplier.get();
                    String next = RESOLUTIONS[0];
                    for (int index = 0; index < RESOLUTIONS.length; index++) {
                        if (RESOLUTIONS[index].equals(current.resolution())) {
                            next = RESOLUTIONS[(index + 1) % RESOLUTIONS.length];
                            break;
                        }
                    }
                    settingsConsumer.accept(current.withWindow(current.windowType(), next));
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
                cycleItem(strings.get("settings.advanced.launcher_visibility"),
                        visibilityLabel(strings, settings.launcherVisibility()),
                        () -> {
                            String next = switch (settingsSupplier.get().launcherVisibility()) {
                                case "hide" -> "keep";
                                case "keep" -> "close";
                                default -> "hide";
                            };
                            settingsConsumer.accept(settingsSupplier.get().withLauncherVisibility(next));
                        }),
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
        return sublist(
                "quick-play",
                strings.get("settings.game.quick_play"),
                summary,
                radioItem(strings.get("settings.game.quick_play.none"), "",
                        "none".equals(settings.quickPlayType()),
                        () -> settingsConsumer.accept(settingsSupplier.get().withQuickPlay(
                                "none", settings.quickPlayMultiplayer(), settings.quickPlaySingleplayer(),
                                settings.quickPlayRealms()))),
                radioItem(strings.get("settings.game.quick_play.multiplayer"),
                        settings.quickPlayMultiplayer().isBlank()
                                ? strings.get("settings.game.quick_play.multiplayer.support")
                                : settings.quickPlayMultiplayer(),
                        "multiplayer".equals(settings.quickPlayType()),
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
                cycleItem(strings.get("settings.advanced.process_priority"),
                        priorityLabel(strings, settings.processPriority()),
                        () -> {
                            String next = switch (settingsSupplier.get().processPriority()) {
                                case "high" -> "normal";
                                case "normal" -> "low";
                                default -> "high";
                            };
                            HMCLDemoGameSettings current = settingsSupplier.get();
                            settingsConsumer.accept(current.withAdvancedLaunch(
                                    current.runningDirectory(), current.gameArguments(),
                                    current.environmentVariables(), next));
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
                    || item instanceof M3RadioButtonSettingItem) {
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
            Runnable onSelect
    ) {
        M3RadioButtonSettingItem item = new M3RadioButtonSettingItem(title);
        if (!support.isBlank()) {
            item.setSupportingText(support);
        }
        item.setSelected(selected);
        item.setOnAction(event -> onSelect.run());
        return item;
    }

    private static M3SettingItem cycleItem(String title, String support, Runnable onAction) {
        M3SettingItem item = new M3SettingItem(title);
        item.setSupportingText(support);
        item.setOnAction(event -> onAction.run());
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
