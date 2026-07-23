// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// Owns deterministic, offline state for the HMCL-inspired M3FX demo.
///
/// The state never reads a real HMCL configuration or game directory. Pages observe its properties and mutate it
/// through typed methods so the shell can demonstrate launcher navigation without external side effects.
@NotNullByDefault
public final class HMCLDemoState {
    /// The localization service shared with pages.
    private final HMCLDemoStrings strings;

    /// The mutable account list.
    private final ObservableList<HMCLDemoAccount> accounts = FXCollections.observableArrayList();

    /// The read-only account list.
    private final @UnmodifiableView ObservableList<HMCLDemoAccount> accountsView =
            FXCollections.unmodifiableObservableList(accounts);

    /// The selected account, or `null` when empty.
    private final ObjectProperty<@Nullable HMCLDemoAccount> selectedAccount =
            new SimpleObjectProperty<>(this, "selectedAccount");

    /// The mutable game-directory list.
    private final ObservableList<HMCLDemoGameDirectory> directories = FXCollections.observableArrayList();

    /// The read-only game-directory list.
    private final @UnmodifiableView ObservableList<HMCLDemoGameDirectory> directoriesView =
            FXCollections.unmodifiableObservableList(directories);

    /// The selected game directory.
    private final ObjectProperty<HMCLDemoGameDirectory> selectedDirectory =
            new SimpleObjectProperty<>(this, "selectedDirectory");

    /// The mutable instance list.
    private final ObservableList<HMCLDemoInstance> instances = FXCollections.observableArrayList();

    /// The read-only instance list.
    private final @UnmodifiableView ObservableList<HMCLDemoInstance> instancesView =
            FXCollections.unmodifiableObservableList(instances);

    /// Instances filtered by the selected game directory and instance search query.
    private final FilteredList<HMCLDemoInstance> filteredInstances = new FilteredList<>(instances);

    /// The read-only filtered instance list.
    private final @UnmodifiableView ObservableList<HMCLDemoInstance> filteredInstancesView =
            FXCollections.unmodifiableObservableList(filteredInstances);

    /// The selected instance, or `null` when empty.
    private final ObjectProperty<@Nullable HMCLDemoInstance> selectedInstance =
            new SimpleObjectProperty<>(this, "selectedInstance");

    /// The instance-list search query.
    private final StringProperty instanceSearchQuery =
            new SimpleStringProperty(this, "instanceSearchQuery", "");

    /// The immutable Minecraft version catalog.
    private final ObservableList<HMCLDemoMinecraftVersion> minecraftVersions = FXCollections.observableArrayList();

    /// The read-only Minecraft version catalog.
    private final @UnmodifiableView ObservableList<HMCLDemoMinecraftVersion> minecraftVersionsView =
            FXCollections.unmodifiableObservableList(minecraftVersions);

    /// Versions filtered by the download search query and channel filter.
    private final FilteredList<HMCLDemoMinecraftVersion> filteredMinecraftVersions =
            new FilteredList<>(minecraftVersions);

    /// The read-only filtered version catalog.
    private final @UnmodifiableView ObservableList<HMCLDemoMinecraftVersion> filteredMinecraftVersionsView =
            FXCollections.unmodifiableObservableList(filteredMinecraftVersions);

    /// The download-center search query.
    private final StringProperty versionSearchQuery =
            new SimpleStringProperty(this, "versionSearchQuery", "");

    /// Whether release versions are included by the download filter.
    private final ObjectProperty<Boolean> showReleaseVersions =
            new SimpleObjectProperty<>(this, "showReleaseVersions", true);

    /// Whether snapshot versions are included by the download filter.
    private final ObjectProperty<Boolean> showSnapshotVersions =
            new SimpleObjectProperty<>(this, "showSnapshotVersions", true);

    /// Whether old beta and alpha versions are included by the download filter.
    private final ObjectProperty<Boolean> showOldVersions =
            new SimpleObjectProperty<>(this, "showOldVersions", false);

    /// The theme seed color.
    private final ObjectProperty<Color> themeColor =
            new SimpleObjectProperty<>(this, "themeColor", Color.web("#5C6BC0"));

    /// The brightness mode.
    private final ObjectProperty<Brightness> brightness =
            new SimpleObjectProperty<>(this, "brightness", Brightness.LIGHT);

    /// The home wallpaper selection.
    private final ObjectProperty<Wallpaper> wallpaper =
            new SimpleObjectProperty<>(this, "wallpaper", Wallpaper.MEADOW);

    /// The active download installation title, or `null` when idle.
    private final ObjectProperty<@Nullable String> installingTitle =
            new SimpleObjectProperty<>(this, "installingTitle");

    /// The active download installation progress from `0.0` through `1.0`.
    private final DoubleProperty installProgress = new SimpleDoubleProperty(this, "installProgress", 0.0);

    /// The deterministic suffix counter for copied instances.
    private int nextCopyNumber = 1;

    /// The deterministic suffix counter for dummy accounts.
    private int nextAccountNumber = 1;

    /// The deterministic suffix counter for dummy instances.
    private int nextInstanceNumber = 1;

    /// Creates state using an internal string resolver.
    public HMCLDemoState() {
        this(new HMCLDemoStrings());
    }

    /// Creates state backed by the supplied string resolver.
    ///
    /// @param strings the localization service
    public HMCLDemoState(HMCLDemoStrings strings) {
        this.strings = strings;
        accounts.setAll(createAccounts());
        directories.setAll(createDirectories());
        instances.setAll(createInstances());
        minecraftVersions.setAll(createMinecraftVersions());
        selectedAccount.set(accounts.get(0));
        selectedDirectory.set(directories.get(0));
        selectedInstance.set(instances.get(0));
        instanceSearchQuery.addListener((observable, oldValue, newValue) -> updateInstanceFilter());
        selectedDirectory.addListener((observable, oldValue, newValue) -> updateInstanceFilter());
        versionSearchQuery.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        showReleaseVersions.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        showSnapshotVersions.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        showOldVersions.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        updateInstanceFilter();
        updateVersionFilter();
    }

    /// Returns the localization service.
    ///
    /// @return the string resolver
    public HMCLDemoStrings getStrings() {
        return strings;
    }

    /// Returns the immutable account list.
    ///
    /// @return the account list
    public @UnmodifiableView ObservableList<HMCLDemoAccount> getAccounts() {
        return accountsView;
    }

    /// Returns the selected account.
    ///
    /// @return the selected account, or `null` when empty
    public @Nullable HMCLDemoAccount getSelectedAccount() {
        return selectedAccount.get();
    }

    /// Returns the selected-account property.
    ///
    /// @return the selected-account property
    public ObjectProperty<@Nullable HMCLDemoAccount> selectedAccountProperty() {
        return selectedAccount;
    }

    /// Selects an account by identifier.
    ///
    /// @param id the account identifier
    /// @return whether a match was selected
    public boolean selectAccount(String id) {
        for (HMCLDemoAccount account : accounts) {
            if (account.id().equals(id)) {
                selectedAccount.set(account);
                return true;
            }
        }
        return false;
    }

    /// Removes the selected account.
    ///
    /// @return whether an account was removed
    public boolean removeSelectedAccount() {
        HMCLDemoAccount account = selectedAccount.get();
        if (account == null) {
            return false;
        }
        int index = accounts.indexOf(account);
        if (!accounts.remove(account)) {
            return false;
        }
        selectedAccount.set(accounts.isEmpty() ? null : accounts.get(Math.min(index, accounts.size() - 1)));
        return true;
    }

    /// Adds and selects a dummy account.
    ///
    /// @param type the provider type
    /// @return the new account
    public HMCLDemoAccount addDummyAccount(HMCLDemoAccount.AccountType type) {
        int number = nextAccountNumber++;
        String skin = switch (type) {
            case MICROSOFT -> "img/skin/wide/steve.png";
            case OFFLINE -> "img/skin/slim/alex.png";
            case EXTERNAL -> "img/skin/wide/noor.png";
        };
        HMCLDemoAccount account = new HMCLDemoAccount(
                "account-" + number,
                "Player " + number,
                type,
                skin
        );
        accounts.add(account);
        selectedAccount.set(account);
        return account;
    }

    /// Returns the immutable game-directory list.
    ///
    /// @return the directory list
    public @UnmodifiableView ObservableList<HMCLDemoGameDirectory> getDirectories() {
        return directoriesView;
    }

    /// Returns the selected game directory.
    ///
    /// @return the selected directory
    public HMCLDemoGameDirectory getSelectedDirectory() {
        return selectedDirectory.get();
    }

    /// Returns the selected-directory property.
    ///
    /// @return the selected-directory property
    public ObjectProperty<HMCLDemoGameDirectory> selectedDirectoryProperty() {
        return selectedDirectory;
    }

    /// Selects a game directory by identifier.
    ///
    /// @param id the directory identifier
    /// @return whether a match was selected
    public boolean selectDirectory(String id) {
        for (HMCLDemoGameDirectory directory : directories) {
            if (directory.id().equals(id)) {
                selectedDirectory.set(directory);
                return true;
            }
        }
        return false;
    }

    /// Returns the immutable instance list.
    ///
    /// @return the instance list
    public @UnmodifiableView ObservableList<HMCLDemoInstance> getInstances() {
        return instancesView;
    }

    /// Returns instances matching the selected directory and search query.
    ///
    /// @return the filtered instance list
    public @UnmodifiableView ObservableList<HMCLDemoInstance> getFilteredInstances() {
        return filteredInstancesView;
    }

    /// Returns the selected instance.
    ///
    /// @return the selected instance, or `null` when empty
    public @Nullable HMCLDemoInstance getSelectedInstance() {
        return selectedInstance.get();
    }

    /// Returns the selected-instance property.
    ///
    /// @return the selected-instance property
    public ObjectProperty<@Nullable HMCLDemoInstance> selectedInstanceProperty() {
        return selectedInstance;
    }

    /// Selects an instance by identifier.
    ///
    /// @param id the instance identifier
    /// @return whether a match was selected
    public boolean selectInstance(String id) {
        for (HMCLDemoInstance instance : instances) {
            if (instance.id().equals(id)) {
                selectedInstance.set(instance);
                if (!selectedDirectory.get().id().equals(instance.directoryId())) {
                    selectDirectory(instance.directoryId());
                }
                return true;
            }
        }
        return false;
    }

    /// Returns the instance search query.
    ///
    /// @return the search query
    public String getInstanceSearchQuery() {
        return instanceSearchQuery.get();
    }

    /// Sets the instance search query.
    ///
    /// @param value the search query
    public void setInstanceSearchQuery(String value) {
        instanceSearchQuery.set(value);
    }

    /// Returns the instance search-query property.
    ///
    /// @return the search-query property
    public StringProperty instanceSearchQueryProperty() {
        return instanceSearchQuery;
    }

    /// Copies the selected instance.
    ///
    /// @return the copy, or `null` when nothing is selected
    public @Nullable HMCLDemoInstance copySelectedInstance() {
        HMCLDemoInstance source = selectedInstance.get();
        if (source == null) {
            return null;
        }
        int copyNumber = nextCopyNumber++;
        HMCLDemoInstance copy = source.copyAs(
                source.id() + "-copy-" + copyNumber,
                source.name() + " (" + copyNumber + ")");
        instances.add(instances.indexOf(source) + 1, copy);
        selectedInstance.set(copy);
        return copy;
    }

    /// Deletes the selected instance.
    ///
    /// @return whether an instance was deleted
    public boolean deleteSelectedInstance() {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        int index = instances.indexOf(instance);
        if (!instances.remove(instance)) {
            return false;
        }
        selectedInstance.set(instances.isEmpty() ? null : instances.get(Math.min(index, instances.size() - 1)));
        return true;
    }

    /// Adds and selects a dummy vanilla instance in the selected directory.
    ///
    /// @return the new instance
    public HMCLDemoInstance addDemoInstance() {
        int number = nextInstanceNumber++;
        HMCLDemoInstance instance = new HMCLDemoInstance(
                "instance-" + number,
                "New Instance " + number,
                "1.21.11",
                "Vanilla",
                selectedDirectory.get().id(),
                "img/grass.png",
                List.of()
        );
        instances.add(0, instance);
        selectedInstance.set(instance);
        return instance;
    }

    /// Changes the enabled state of one mod in the selected instance.
    ///
    /// @param modId the mod identifier
    /// @param enabled the requested enabled state
    /// @return whether the mod was found
    public boolean setSelectedModEnabled(String modId, boolean enabled) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        List<HMCLDemoMod> updatedMods = new ArrayList<>(instance.mods().size());
        boolean found = false;
        for (HMCLDemoMod mod : instance.mods()) {
            if (mod.id().equals(modId)) {
                updatedMods.add(mod.withEnabled(enabled));
                found = true;
            } else {
                updatedMods.add(mod);
            }
        }
        if (!found) {
            return false;
        }
        HMCLDemoInstance updated = instance.withMods(updatedMods);
        instances.set(instances.indexOf(instance), updated);
        selectedInstance.set(updated);
        return true;
    }

    /// Returns the immutable Minecraft version catalog.
    ///
    /// @return the version catalog
    public @UnmodifiableView ObservableList<HMCLDemoMinecraftVersion> getMinecraftVersions() {
        return minecraftVersionsView;
    }

    /// Returns versions matching the download filters.
    ///
    /// @return the filtered version catalog
    public @UnmodifiableView ObservableList<HMCLDemoMinecraftVersion> getFilteredMinecraftVersions() {
        return filteredMinecraftVersionsView;
    }

    /// Returns the download search query.
    ///
    /// @return the search query
    public String getVersionSearchQuery() {
        return versionSearchQuery.get();
    }

    /// Sets the download search query.
    ///
    /// @param value the search query
    public void setVersionSearchQuery(String value) {
        versionSearchQuery.set(value);
    }

    /// Returns the download search-query property.
    ///
    /// @return the search-query property
    public StringProperty versionSearchQueryProperty() {
        return versionSearchQuery;
    }

    /// Returns whether release versions are shown.
    ///
    /// @return `true` when releases are included
    public boolean isShowReleaseVersions() {
        return showReleaseVersions.get();
    }

    /// Sets whether release versions are shown.
    ///
    /// @param value the visibility flag
    public void setShowReleaseVersions(boolean value) {
        showReleaseVersions.set(value);
    }

    /// Returns the release-visibility property.
    ///
    /// @return the property
    public ObjectProperty<Boolean> showReleaseVersionsProperty() {
        return showReleaseVersions;
    }

    /// Returns whether snapshot versions are shown.
    ///
    /// @return `true` when snapshots are included
    public boolean isShowSnapshotVersions() {
        return showSnapshotVersions.get();
    }

    /// Sets whether snapshot versions are shown.
    ///
    /// @param value the visibility flag
    public void setShowSnapshotVersions(boolean value) {
        showSnapshotVersions.set(value);
    }

    /// Returns the snapshot-visibility property.
    ///
    /// @return the property
    public ObjectProperty<Boolean> showSnapshotVersionsProperty() {
        return showSnapshotVersions;
    }

    /// Returns whether old versions are shown.
    ///
    /// @return `true` when old versions are included
    public boolean isShowOldVersions() {
        return showOldVersions.get();
    }

    /// Sets whether old versions are shown.
    ///
    /// @param value the visibility flag
    public void setShowOldVersions(boolean value) {
        showOldVersions.set(value);
    }

    /// Returns the old-version visibility property.
    ///
    /// @return the property
    public ObjectProperty<Boolean> showOldVersionsProperty() {
        return showOldVersions;
    }

    /// Starts a dummy installation for the supplied title.
    ///
    /// @param title the installation title shown by the progress UI
    public void beginInstallation(String title) {
        installingTitle.set(title);
        installProgress.set(0.0);
    }

    /// Updates installation progress and clears the installation when it reaches `1.0`.
    ///
    /// @param value the progress value
    /// @throws IllegalStateException if no installation is active
    public void setInstallProgress(double value) {
        if (installingTitle.get() == null) {
            throw new IllegalStateException("No installation is active");
        }
        double normalized = Math.max(0.0, Math.min(1.0, value));
        installProgress.set(normalized);
        if (normalized >= 1.0) {
            installingTitle.set(null);
        }
    }

    /// Cancels the active installation.
    ///
    /// @return whether an installation was cancelled
    public boolean cancelInstallation() {
        if (installingTitle.get() == null) {
            return false;
        }
        installingTitle.set(null);
        installProgress.set(0.0);
        return true;
    }

    /// Returns the active installation title.
    ///
    /// @return the title, or `null` when idle
    public @Nullable String getInstallingTitle() {
        return installingTitle.get();
    }

    /// Returns the installing-title property.
    ///
    /// @return the property
    public ObjectProperty<@Nullable String> installingTitleProperty() {
        return installingTitle;
    }

    /// Returns installation progress.
    ///
    /// @return a value from `0.0` through `1.0`
    public double getInstallProgress() {
        return installProgress.get();
    }

    /// Returns the install-progress property.
    ///
    /// @return the property
    public DoubleProperty installProgressProperty() {
        return installProgress;
    }

    /// Returns the theme seed color.
    ///
    /// @return the theme color
    public Color getThemeColor() {
        return themeColor.get();
    }

    /// Sets the theme seed color.
    ///
    /// @param value the theme color
    public void setThemeColor(Color value) {
        themeColor.set(value);
    }

    /// Returns the theme-color property.
    ///
    /// @return the property
    public ObjectProperty<Color> themeColorProperty() {
        return themeColor;
    }

    /// Returns the brightness mode.
    ///
    /// @return the brightness mode
    public Brightness getBrightness() {
        return brightness.get();
    }

    /// Sets the brightness mode.
    ///
    /// @param value the brightness mode
    public void setBrightness(Brightness value) {
        brightness.set(value);
    }

    /// Returns the brightness property.
    ///
    /// @return the property
    public ObjectProperty<Brightness> brightnessProperty() {
        return brightness;
    }

    /// Returns the selected wallpaper.
    ///
    /// @return the wallpaper
    public Wallpaper getWallpaper() {
        return wallpaper.get();
    }

    /// Sets the selected wallpaper.
    ///
    /// @param value the wallpaper
    public void setWallpaper(Wallpaper value) {
        wallpaper.set(value);
    }

    /// Returns the wallpaper property.
    ///
    /// @return the property
    public ObjectProperty<Wallpaper> wallpaperProperty() {
        return wallpaper;
    }

    /// Returns the runtime language.
    ///
    /// @return the locale
    public Locale getLanguage() {
        return strings.getLocale();
    }

    /// Sets the runtime language.
    ///
    /// @param value the locale
    public void setLanguage(Locale value) {
        strings.setLocale(value);
    }

    /// Returns the language property.
    ///
    /// @return the property
    public ObjectProperty<Locale> languageProperty() {
        return strings.localeProperty();
    }

    /// Recomputes the instance-list predicate.
    private void updateInstanceFilter() {
        String directoryId = selectedDirectory.get().id();
        String query = instanceSearchQuery.get().strip().toLowerCase(Locale.ROOT);
        filteredInstances.setPredicate(instance -> {
            if (!instance.directoryId().equals(directoryId)) {
                return false;
            }
            if (query.isEmpty()) {
                return true;
            }
            return instance.name().toLowerCase(Locale.ROOT).contains(query)
                    || instance.gameVersion().toLowerCase(Locale.ROOT).contains(query)
                    || instance.loader().toLowerCase(Locale.ROOT).contains(query);
        });
    }

    /// Recomputes the download-version predicate.
    private void updateVersionFilter() {
        String query = versionSearchQuery.get().strip().toLowerCase(Locale.ROOT);
        boolean release = Boolean.TRUE.equals(showReleaseVersions.get());
        boolean snapshot = Boolean.TRUE.equals(showSnapshotVersions.get());
        boolean old = Boolean.TRUE.equals(showOldVersions.get());
        filteredMinecraftVersions.setPredicate(version -> {
            boolean channelAllowed = switch (version.channel()) {
                case RELEASE -> release;
                case SNAPSHOT -> snapshot;
                case OLD_BETA, OLD_ALPHA -> old;
            };
            if (!channelAllowed) {
                return false;
            }
            if (query.isEmpty()) {
                return true;
            }
            return version.name().toLowerCase(Locale.ROOT).contains(query)
                    || version.releaseTime().toLowerCase(Locale.ROOT).contains(query);
        });
    }

    /// Creates account fixtures inspired by the HMCL screenshots.
    private static @Unmodifiable List<HMCLDemoAccount> createAccounts() {
        return List.of(
                new HMCLDemoAccount("glavo", "Glavo", HMCLDemoAccount.AccountType.MICROSOFT,
                        "img/skin/wide/steve.png"),
                new HMCLDemoAccount("alex", "Alex", HMCLDemoAccount.AccountType.OFFLINE,
                        "img/skin/slim/alex.png"),
                new HMCLDemoAccount("noor", "Noor", HMCLDemoAccount.AccountType.EXTERNAL,
                        "img/skin/wide/noor.png")
        );
    }

    /// Creates game-directory fixtures.
    private static @Unmodifiable List<HMCLDemoGameDirectory> createDirectories() {
        return List.of(
                new HMCLDemoGameDirectory("minecraft", ".minecraft", "D:\\Games\\Minecraft\\.minecraft"),
                new HMCLDemoGameDirectory("minecraft2", ".minecraft2", "D:\\Games\\Minecraft\\.minecraft2")
        );
    }

    /// Creates instance fixtures inspired by the HMCL screenshots.
    private static @Unmodifiable List<HMCLDemoInstance> createInstances() {
        return List.of(
                new HMCLDemoInstance(
                        "fabulously-optimized",
                        "Fabulously Optimized",
                        "1.21.11",
                        "Fabric",
                        "minecraft",
                        "img/command.png",
                        List.of(
                                new HMCLDemoMod("sodium", "Sodium", "sodium-fabric-0.7.3+mc1.21.11.jar", true),
                                new HMCLDemoMod("iris", "Iris", "iris-fabric-1.9.6+mc1.21.11.jar", true),
                                new HMCLDemoMod("modmenu", "Mod Menu", "modmenu-16.0.0-rc.1.jar", true),
                                new HMCLDemoMod("fabric-api", "Fabric API",
                                        "fabric-api-0.140.2+1.21.11.jar", true),
                                new HMCLDemoMod("dynamic-fps", "Dynamic FPS",
                                        "dynamic-fps-3.11.3+minecraft-1.21.11-fabric.jar", true)
                        )),
                new HMCLDemoInstance(
                        "snapshot-26-3-4",
                        "26.3 Snapshot 4",
                        "26.3-snapshot-4",
                        "Vanilla",
                        "minecraft",
                        "img/grass.png",
                        List.of()),
                new HMCLDemoInstance(
                        "forge-1-12-2",
                        "1.12.2-Forge_14.23.5.2860",
                        "1.12.2",
                        "Forge 14.23.5.2860",
                        "minecraft",
                        "img/furnace.png",
                        List.of(
                                new HMCLDemoMod("jei", "Just Enough Items", "jei_1.12.2-4.16.1.1000.jar", true)
                        )),
                new HMCLDemoInstance(
                        "release-1-21-11",
                        "1.21.11",
                        "1.21.11",
                        "Vanilla",
                        "minecraft",
                        "img/grass.png",
                        List.of()),
                new HMCLDemoInstance(
                        "lab-minecraft2",
                        "Modding Lab",
                        "1.20.1",
                        "NeoForge",
                        "minecraft2",
                        "img/command.png",
                        List.of(
                                new HMCLDemoMod("create", "Create", "create-1.20.1-0.5.1.jar", true),
                                new HMCLDemoMod("jei-neo", "Just Enough Items", "jei-1.20.1-15.20.0.jar", true)
                        ))
        );
    }

    /// Creates Minecraft version fixtures inspired by the HMCL screenshots.
    private static @Unmodifiable List<HMCLDemoMinecraftVersion> createMinecraftVersions() {
        return List.of(
                new HMCLDemoMinecraftVersion("26.1", "26.1", "2026/3/24 19:05:36",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.11", "1.21.11", "2025/12/10 1:36:31",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.10", "1.21.10", "2025/10/7 19:01:13",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.9", "1.21.9", "2025/9/30 17:38:40",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.8", "1.21.8", "2025/7/17 20:25:32",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.7", "1.21.7", "2025/6/30 19:24:33",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.6", "1.21.6", "2025/6/18 1:24:40",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("1.21.5", "1.21.5", "2025/3/25 23:04:38",
                        HMCLDemoMinecraftVersion.Channel.RELEASE),
                new HMCLDemoMinecraftVersion("26.3-snapshot-4", "26.3 Snapshot 4", "2026/4/2 18:12:00",
                        HMCLDemoMinecraftVersion.Channel.SNAPSHOT),
                new HMCLDemoMinecraftVersion("25w46a", "25w46a", "2025/11/12 14:22:10",
                        HMCLDemoMinecraftVersion.Channel.SNAPSHOT),
                new HMCLDemoMinecraftVersion("1.7.3-pre", "1.7.3-pre", "2013/10/1 12:00:00",
                        HMCLDemoMinecraftVersion.Channel.OLD_BETA),
                new HMCLDemoMinecraftVersion("a1.2.6", "a1.2.6", "2010/12/3 12:00:00",
                        HMCLDemoMinecraftVersion.Channel.OLD_ALPHA)
        );
    }

    /// Describes the selected application brightness mode.
    @NotNullByDefault
    public enum Brightness {
        /// Follows a light appearance for this offline demo.
        SYSTEM,

        /// Forces a light color scheme.
        LIGHT,

        /// Forces a dark color scheme.
        DARK
    }

    /// Identifies one generated demo wallpaper.
    @NotNullByDefault
    public enum Wallpaper {
        /// A bright green landscape treatment.
        MEADOW,

        /// A cool dark cave treatment.
        CAVES,

        /// A warm orange sunset treatment.
        SUNSET
    }
}
