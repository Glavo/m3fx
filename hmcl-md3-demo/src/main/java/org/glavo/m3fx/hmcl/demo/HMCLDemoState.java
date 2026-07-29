// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.paint.Color;
import org.glavo.m3fx.tokens.M3Profile;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

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

    /// The download-center search query for Minecraft versions.
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

    /// The mutable download-catalog item list.
    private final ObservableList<HMCLDemoCatalogItem> catalogItems = FXCollections.observableArrayList();

    /// The read-only download-catalog item list.
    private final @UnmodifiableView ObservableList<HMCLDemoCatalogItem> catalogItemsView =
            FXCollections.unmodifiableObservableList(catalogItems);

    /// Catalog mods filtered by [#catalogSearchQuery].
    private final FilteredList<HMCLDemoCatalogItem> catalogMods = new FilteredList<>(catalogItems);

    /// Catalog modpacks filtered by [#catalogSearchQuery].
    private final FilteredList<HMCLDemoCatalogItem> catalogModpacks = new FilteredList<>(catalogItems);

    /// Catalog resource packs filtered by [#catalogSearchQuery].
    private final FilteredList<HMCLDemoCatalogItem> catalogResourcePacks = new FilteredList<>(catalogItems);

    /// Catalog shaders filtered by [#catalogSearchQuery].
    private final FilteredList<HMCLDemoCatalogItem> catalogShaders = new FilteredList<>(catalogItems);

    /// Catalog worlds filtered by [#catalogSearchQuery].
    private final FilteredList<HMCLDemoCatalogItem> catalogWorlds = new FilteredList<>(catalogItems);

    /// The read-only filtered catalog-mod list.
    private final @UnmodifiableView ObservableList<HMCLDemoCatalogItem> catalogModsView =
            FXCollections.unmodifiableObservableList(catalogMods);

    /// The read-only filtered catalog-modpack list.
    private final @UnmodifiableView ObservableList<HMCLDemoCatalogItem> catalogModpacksView =
            FXCollections.unmodifiableObservableList(catalogModpacks);

    /// The read-only filtered catalog-resource-pack list.
    private final @UnmodifiableView ObservableList<HMCLDemoCatalogItem> catalogResourcePacksView =
            FXCollections.unmodifiableObservableList(catalogResourcePacks);

    /// The read-only filtered catalog-shader list.
    private final @UnmodifiableView ObservableList<HMCLDemoCatalogItem> catalogShadersView =
            FXCollections.unmodifiableObservableList(catalogShaders);

    /// The read-only filtered catalog-world list.
    private final @UnmodifiableView ObservableList<HMCLDemoCatalogItem> catalogWorldsView =
            FXCollections.unmodifiableObservableList(catalogWorlds);

    /// The download-catalog search query.
    private final StringProperty catalogSearchQuery =
            new SimpleStringProperty(this, "catalogSearchQuery", "");

    /// The mutable discovered Java runtime list.
    private final ObservableList<HMCLDemoJavaRuntime> javaRuntimes = FXCollections.observableArrayList();

    /// The read-only Java runtime list.
    private final @UnmodifiableView ObservableList<HMCLDemoJavaRuntime> javaRuntimesView =
            FXCollections.unmodifiableObservableList(javaRuntimes);

    /// Global game-launch settings used by the settings page.
    private final ObjectProperty<HMCLDemoGameSettings> globalGameSettings =
            new SimpleObjectProperty<>(this, "globalGameSettings", HMCLDemoGameSettings.globalDefaults());

    /// Per-instance game settings keyed by instance id.
    private final Map<String, HMCLDemoGameSettings> instanceGameSettings = new HashMap<>();

    /// The theme seed color.
    private final ObjectProperty<Color> themeColor =
            new SimpleObjectProperty<>(this, "themeColor", Color.web("#5C6BC0"));

    /// The Material component profile.
    private final ObjectProperty<M3Profile> profile =
            new SimpleObjectProperty<>(this, "profile", M3Profile.BASELINE_2021);

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

    /// The multiplayer session phase.
    private final ObjectProperty<MultiplayerPhase> multiplayerPhase =
            new SimpleObjectProperty<>(this, "multiplayerPhase", MultiplayerPhase.WAITING);

    /// The multiplayer room code shown while hosting or joining.
    private final StringProperty multiplayerRoomCode =
            new SimpleStringProperty(this, "multiplayerRoomCode", "");

    /// The launcher update channel label (`stable` or `dev`).
    private final StringProperty updateChannel =
            new SimpleStringProperty(this, "updateChannel", "stable");

    /// Whether the launcher auto-allocates game memory.
    private final BooleanProperty autoAllocateMemory =
            new SimpleBooleanProperty(this, "autoAllocateMemory", true);

    /// The concurrent download thread count.
    private final IntegerProperty downloadThreads =
            new SimpleIntegerProperty(this, "downloadThreads", 64);

    /// The selected download source label.
    private final StringProperty downloadSource =
            new SimpleStringProperty(this, "downloadSource", "official");

    /// Whether launcher animations are disabled.
    private final BooleanProperty animationDisabled =
            new SimpleBooleanProperty(this, "animationDisabled", false);

    /// The global max memory in megabytes.
    private final IntegerProperty globalMaxMemoryMb =
            new SimpleIntegerProperty(this, "globalMaxMemoryMb", 4096);

    /// The default game window resolution label.
    private final StringProperty globalResolution =
            new SimpleStringProperty(this, "globalResolution", "854x480");

    /// The launcher visibility policy label.
    private final StringProperty launcherVisibility =
            new SimpleStringProperty(this, "launcherVisibility", "hide");

    /// The default isolation policy: `never`, `always`, or `modded`.
    private final StringProperty defaultIsolation =
            new SimpleStringProperty(this, "defaultIsolation", "modded");

    /// Whether the selected Java runtime id is forced; empty means auto.
    private final StringProperty selectedJavaId =
            new SimpleStringProperty(this, "selectedJavaId", "auto");

    /// Whether preview update builds are accepted.
    private final BooleanProperty acceptPreviewUpdate =
            new SimpleBooleanProperty(this, "acceptPreviewUpdate", false);

    /// Whether the automatic update dialog is suppressed.
    private final BooleanProperty disableAutoShowUpdateDialog =
            new SimpleBooleanProperty(this, "disableAutoShowUpdateDialog", false);

    /// Whether April Fools content is disabled.
    private final BooleanProperty disableAprilFools =
            new SimpleBooleanProperty(this, "disableAprilFools", false);

    /// Whether the title bar is transparent over the wallpaper.
    private final BooleanProperty titleBarTransparent =
            new SimpleBooleanProperty(this, "titleBarTransparent", false);

    /// Whether the outer window chrome is transparent.
    private final BooleanProperty windowTransparent =
            new SimpleBooleanProperty(this, "windowTransparent", false);

    /// Background opacity percentage from `20` through `100`.
    private final IntegerProperty backgroundOpacity =
            new SimpleIntegerProperty(this, "backgroundOpacity", 100);

    /// Version-list download source: `auto`, `official`, or `mirror`.
    private final StringProperty versionListSource =
            new SimpleStringProperty(this, "versionListSource", "auto");

    /// File download source: `auto`, `official`, or `mirror`.
    private final StringProperty fileDownloadSource =
            new SimpleStringProperty(this, "fileDownloadSource", "auto");

    /// Default addon catalog source: `modrinth` or `curseforge`.
    private final StringProperty defaultAddonSource =
            new SimpleStringProperty(this, "defaultAddonSource", "modrinth");

    /// Whether download thread count is chosen automatically.
    private final BooleanProperty autoDownloadThreads =
            new SimpleBooleanProperty(this, "autoDownloadThreads", true);

    /// Cache directory mode: `default` or `custom`.
    private final StringProperty cacheDirectoryType =
            new SimpleStringProperty(this, "cacheDirectoryType", "default");

    /// Proxy type: `system`, `none`, `http`, or `socks`.
    private final StringProperty proxyType =
            new SimpleStringProperty(this, "proxyType", "system");

    /// Custom proxy host.
    private final StringProperty proxyHost =
            new SimpleStringProperty(this, "proxyHost", "127.0.0.1");

    /// Custom proxy port.
    private final IntegerProperty proxyPort =
            new SimpleIntegerProperty(this, "proxyPort", 7890);

    /// Whether proxy authentication is enabled.
    private final BooleanProperty proxyAuthentication =
            new SimpleBooleanProperty(this, "proxyAuthentication", false);

    /// The deterministic suffix counter for copied instances.
    private int nextCopyNumber = 1;

    /// The deterministic suffix counter for dummy accounts.
    private int nextAccountNumber = 1;

    /// The deterministic suffix counter for dummy instances.
    private int nextInstanceNumber = 1;

    /// The deterministic suffix counter for dummy directories.
    private int nextDirectoryNumber = 1;

    /// The deterministic suffix counter for added content rows.
    private int nextContentNumber = 1;

    /// The deterministic suffix counter for multiplayer room codes.
    private int nextRoomNumber = 1;

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
        for (HMCLDemoInstance instance : instances) {
            instanceGameSettings.put(
                    instance.id(),
                    HMCLDemoGameSettings.instanceDefaults(globalGameSettings.get(), instance.isolated())
                            .withMemory(
                                    false,
                                    instance.maxMemoryMb(),
                                    512,
                                    256)
                            .withWindow(
                                    instance.fullscreen() ? "fullscreen" : "windowed",
                                    instance.resolution())
                            .withJava(
                                    "auto".equals(instance.javaId()) ? "auto" : "detected",
                                    "auto".equals(instance.javaId()) ? "" : instance.javaId(),
                                    "21",
                                    "")
            );
        }
        minecraftVersions.setAll(createMinecraftVersions());
        catalogItems.setAll(createCatalogItems());
        javaRuntimes.setAll(createJavaRuntimes());
        selectedAccount.set(accounts.get(0));
        selectedDirectory.set(directories.get(0));
        selectedInstance.set(instances.get(0));
        instanceSearchQuery.addListener((observable, oldValue, newValue) -> updateInstanceFilter());
        selectedDirectory.addListener((observable, oldValue, newValue) -> updateInstanceFilter());
        versionSearchQuery.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        showReleaseVersions.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        showSnapshotVersions.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        showOldVersions.addListener((observable, oldValue, newValue) -> updateVersionFilter());
        catalogSearchQuery.addListener((observable, oldValue, newValue) -> updateCatalogFilters());
        updateInstanceFilter();
        updateVersionFilter();
        updateCatalogFilters();
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

    /// Adds and selects a dummy game directory.
    ///
    /// @param name the displayed directory name
    /// @return the new directory
    public HMCLDemoGameDirectory addDirectory(String name) {
        int number = nextDirectoryNumber++;
        HMCLDemoGameDirectory directory = new HMCLDemoGameDirectory(
                "directory-" + number,
                name,
                "D:\\Games\\Minecraft\\" + name
        );
        directories.add(directory);
        selectedDirectory.set(directory);
        return directory;
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
    /// Also selects the instance game directory when it differs from the current selection.
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
        instanceGameSettings.put(copy.id(), getInstanceGameSettings(source.id()));
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
        return installInstance("New Instance " + nextInstanceNumber, "1.21.11", null, null);
    }

    /// Installs a deterministic instance from the download wizard.
    ///
    /// @param name the instance display name
    /// @param gameVersion the Minecraft version id
    /// @param loaderId the loader family id, or `null` for vanilla
    /// @param loaderVersion the loader version label when a loader is selected
    /// @return the new selected instance
    public HMCLDemoInstance installInstance(
            String name,
            String gameVersion,
            @Nullable String loaderId,
            @Nullable String loaderVersion
    ) {
        int number = nextInstanceNumber++;
        String loaderLabel = switch (loaderId == null ? "" : loaderId) {
            case "fabric" -> "Fabric " + Objects.requireNonNullElse(loaderVersion, "");
            case "forge" -> "Forge " + Objects.requireNonNullElse(loaderVersion, "");
            case "neoforge" -> "NeoForge " + Objects.requireNonNullElse(loaderVersion, "");
            case "quilt" -> "Quilt " + Objects.requireNonNullElse(loaderVersion, "");
            default -> "Vanilla";
        };
        HMCLDemoInstance instance = new HMCLDemoInstance(
                "instance-" + number,
                name.strip().isEmpty() ? "New Instance " + number : name.strip(),
                gameVersion,
                loaderLabel.strip(),
                selectedDirectory.get().id(),
                "img/grass.png",
                loaderId != null,
                getGlobalMaxMemoryMb(),
                getGlobalResolution(),
                false,
                getSelectedJavaId(),
                installers(gameVersion, loaderId, loaderVersion),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        instances.add(0, instance);
        instanceGameSettings.put(
                instance.id(),
                HMCLDemoGameSettings.instanceDefaults(globalGameSettings.get(), instance.isolated())
                        .withMemory(false, instance.maxMemoryMb(), 512, 256)
                        .withWindow(instance.fullscreen() ? "fullscreen" : "windowed", instance.resolution())
        );
        selectedInstance.set(instance);
        return instance;
    }

    /// Renames the selected instance.
    ///
    /// @param name the new display name
    /// @return whether the selected instance was renamed
    public boolean renameSelectedInstance(String name) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        replaceSelectedInstance(instance.withName(name));
        return true;
    }

    /// Updates settings fields on the selected instance.
    ///
    /// @param isolated whether the instance uses an isolated working directory
    /// @param maxMemoryMb configured max memory
    /// @param resolution window resolution label
    /// @param fullscreen whether fullscreen is preferred
    /// @param javaId selected Java runtime id, or `auto`
    /// @return whether the selected instance was updated
    public boolean updateSelectedInstanceSettings(
            boolean isolated,
            int maxMemoryMb,
            String resolution,
            boolean fullscreen,
            String javaId
    ) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        replaceSelectedInstance(instance.withSettings(isolated, maxMemoryMb, resolution, fullscreen, javaId));
        HMCLDemoGameSettings previous = getInstanceGameSettings(instance.id());
        instanceGameSettings.put(
                instance.id(),
                previous.withIsolated(isolated)
                        .withMemory(previous.autoMemory(), maxMemoryMb, previous.minMemoryMb(), previous.metaspaceMb())
                        .withWindow(fullscreen ? "fullscreen" : previous.windowType(), resolution)
                        .withJava(
                                "auto".equals(javaId) ? "auto" : "detected",
                                "auto".equals(javaId) ? "" : javaId,
                                previous.javaVersion(),
                                previous.javaPath())
        );
        return true;
    }

    /// Returns the global game-settings model.
    ///
    /// @return the global settings
    public HMCLDemoGameSettings getGlobalGameSettings() {
        return globalGameSettings.get();
    }

    /// Replaces the global game-settings model.
    ///
    /// @param value the new settings
    public void setGlobalGameSettings(HMCLDemoGameSettings value) {
        globalGameSettings.set(value);
        setAutoAllocateMemory(value.autoMemory());
        setGlobalMaxMemoryMb(value.maxMemoryMb());
        setGlobalResolution(value.resolution());
        setLauncherVisibility(value.launcherVisibility());
    }

    /// Returns the global game-settings property.
    ///
    /// @return the property
    public ObjectProperty<HMCLDemoGameSettings> globalGameSettingsProperty() {
        return globalGameSettings;
    }

    /// Returns game settings for one instance, creating defaults when absent.
    ///
    /// @param instanceId the instance id
    /// @return the settings
    public HMCLDemoGameSettings getInstanceGameSettings(String instanceId) {
        @Nullable HMCLDemoGameSettings existing = instanceGameSettings.get(instanceId);
        if (existing != null) {
            return existing;
        }
        HMCLDemoGameSettings created = HMCLDemoGameSettings.instanceDefaults(globalGameSettings.get(), false);
        instanceGameSettings.put(instanceId, created);
        return created;
    }

    /// Replaces game settings for one instance and keeps legacy instance fields in sync.
    ///
    /// @param instanceId the instance id
    /// @param value the new settings
    public void setInstanceGameSettings(String instanceId, HMCLDemoGameSettings value) {
        instanceGameSettings.put(instanceId, value);
        for (int index = 0; index < instances.size(); index++) {
            HMCLDemoInstance instance = instances.get(index);
            if (!instance.id().equals(instanceId)) {
                continue;
            }
            String javaId = switch (value.javaMode()) {
                case "detected", "custom", "version" -> value.javaId().isBlank() ? "auto" : value.javaId();
                default -> "auto";
            };
            HMCLDemoInstance updated = instance.withSettings(
                    value.isolated(),
                    value.maxMemoryMb(),
                    value.resolution(),
                    "fullscreen".equals(value.windowType()),
                    javaId
            );
            instances.set(index, updated);
            @Nullable HMCLDemoInstance selected = selectedInstance.get();
            if (instance.equals(selected) || selected != null && instanceId.equals(selected.id())) {
                selectedInstance.set(updated);
            }
            return;
        }
    }

    /// Changes the enabled state of one mod in the selected instance.
    ///
    /// @param modId the mod identifier
    /// @param enabled the requested enabled state
    /// @return whether the mod was found
    public boolean setSelectedModEnabled(String modId, boolean enabled) {
        return updateSelectedMods(mods -> {
            List<HMCLDemoMod> updated = new ArrayList<>(mods.size());
            boolean found = false;
            for (HMCLDemoMod mod : mods) {
                if (mod.id().equals(modId)) {
                    updated.add(mod.withEnabled(enabled));
                    found = true;
                } else {
                    updated.add(mod);
                }
            }
            return found ? updated : null;
        });
    }

    /// Adds a dummy mod to the selected instance.
    ///
    /// @return the new mod, or `null` when nothing is selected
    public @Nullable HMCLDemoMod addDemoMod() {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return null;
        }
        int number = nextContentNumber++;
        HMCLDemoMod mod = new HMCLDemoMod(
                "mod-" + number,
                "Demo Mod " + number,
                "demo-mod-" + number + ".jar",
                "1.0." + number,
                true
        );
        List<HMCLDemoMod> updated = new ArrayList<>(instance.mods());
        updated.add(mod);
        replaceSelectedInstance(instance.withMods(updated));
        return mod;
    }

    /// Removes one mod from the selected instance.
    ///
    /// @param modId the mod identifier
    /// @return whether the mod was removed
    public boolean removeMod(String modId) {
        return updateSelectedMods(mods -> {
            List<HMCLDemoMod> updated = new ArrayList<>(mods.size());
            boolean found = false;
            for (HMCLDemoMod mod : mods) {
                if (mod.id().equals(modId)) {
                    found = true;
                } else {
                    updated.add(mod);
                }
            }
            return found ? updated : null;
        });
    }

    /// Changes the enabled state of one resource pack in the selected instance.
    ///
    /// @param packId the pack identifier
    /// @param enabled the requested enabled state
    /// @return whether the pack was found
    public boolean setResourcePackEnabled(String packId, boolean enabled) {
        return updateSelectedResourcePacks(packs -> togglePack(packs, packId, enabled));
    }

    /// Adds a dummy resource pack to the selected instance.
    ///
    /// @return the new pack, or `null` when nothing is selected
    public @Nullable HMCLDemoPack addDemoResourcePack() {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return null;
        }
        int number = nextContentNumber++;
        HMCLDemoPack pack = new HMCLDemoPack(
                "resource-pack-" + number,
                "Demo Resource Pack " + number,
                "1.21 / " + number + " files",
                true
        );
        List<HMCLDemoPack> updated = new ArrayList<>(instance.resourcePacks());
        updated.add(pack);
        replaceSelectedInstance(instance.withResourcePacks(updated));
        return pack;
    }

    /// Changes the enabled state of one shader pack in the selected instance.
    ///
    /// @param packId the pack identifier
    /// @param enabled the requested enabled state
    /// @return whether the pack was found
    public boolean setShaderEnabled(String packId, boolean enabled) {
        return updateSelectedShaderPacks(packs -> togglePack(packs, packId, enabled));
    }

    /// Adds a dummy shader pack to the selected instance.
    ///
    /// @return the new pack, or `null` when nothing is selected
    public @Nullable HMCLDemoPack addDemoShader() {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return null;
        }
        int number = nextContentNumber++;
        HMCLDemoPack pack = new HMCLDemoPack(
                "shader-" + number,
                "Demo Shader " + number,
                "Iris / " + number,
                true
        );
        List<HMCLDemoPack> updated = new ArrayList<>(instance.shaderPacks());
        updated.add(pack);
        replaceSelectedInstance(instance.withShaderPacks(updated));
        return pack;
    }

    /// Adds a dummy world to the selected instance.
    ///
    /// @return the new world, or `null` when nothing is selected
    public @Nullable HMCLDemoWorld addDemoWorld() {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return null;
        }
        int number = nextContentNumber++;
        HMCLDemoWorld world = new HMCLDemoWorld(
                "world-" + number,
                "New World " + number,
                "Survival",
                "Just now"
        );
        List<HMCLDemoWorld> updated = new ArrayList<>(instance.worlds());
        updated.add(world);
        replaceSelectedInstance(instance.withWorlds(updated));
        return world;
    }

    /// Removes one world from the selected instance.
    ///
    /// @param worldId the world identifier
    /// @return whether the world was removed
    public boolean removeWorld(String worldId) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        List<HMCLDemoWorld> updated = new ArrayList<>(instance.worlds().size());
        boolean found = false;
        for (HMCLDemoWorld world : instance.worlds()) {
            if (world.id().equals(worldId)) {
                found = true;
            } else {
                updated.add(world);
            }
        }
        if (!found) {
            return false;
        }
        replaceSelectedInstance(instance.withWorlds(updated));
        return true;
    }

    /// Adds a dummy schematic to the selected instance.
    ///
    /// @return the new schematic, or `null` when nothing is selected
    public @Nullable HMCLDemoPack addDemoSchematic() {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return null;
        }
        int number = nextContentNumber++;
        HMCLDemoPack schematic = new HMCLDemoPack(
                "schematic-" + number,
                "Demo Schematic " + number,
                number + " blocks",
                true
        );
        List<HMCLDemoPack> updated = new ArrayList<>(instance.schematics());
        updated.add(schematic);
        replaceSelectedInstance(instance.withSchematics(updated));
        return schematic;
    }

    /// Sets the installed version of one installer slot on the selected instance.
    ///
    /// @param installerId the installer family id
    /// @param version the installed version, or `null` to clear
    /// @return whether the installer slot was found
    public boolean setInstallerVersion(String installerId, @Nullable String version) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        List<HMCLDemoInstaller> updated = new ArrayList<>(instance.installers().size());
        boolean found = false;
        for (HMCLDemoInstaller installer : instance.installers()) {
            if (installer.id().equals(installerId)) {
                updated.add(installer.withVersion(version));
                found = true;
            } else {
                updated.add(installer);
            }
        }
        if (!found) {
            return false;
        }
        replaceSelectedInstance(instance.withInstallers(updated));
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

    /// Returns the immutable download-catalog item list.
    ///
    /// @return the full catalog
    public @UnmodifiableView ObservableList<HMCLDemoCatalogItem> getCatalogItems() {
        return catalogItemsView;
    }

    /// Returns catalog rows matching `kind` and [#getCatalogSearchQuery()].
    ///
    /// @param kind the content kind
    /// @return the filtered catalog list for the kind
    public @UnmodifiableView ObservableList<HMCLDemoCatalogItem> getCatalog(HMCLDemoCatalogItem.Kind kind) {
        return switch (kind) {
            case MOD -> catalogModsView;
            case MODPACK -> catalogModpacksView;
            case RESOURCE_PACK -> catalogResourcePacksView;
            case SHADER -> catalogShadersView;
            case WORLD -> catalogWorldsView;
        };
    }

    /// Returns the catalog search query.
    ///
    /// @return the search query
    public String getCatalogSearchQuery() {
        return catalogSearchQuery.get();
    }

    /// Sets the catalog search query.
    ///
    /// @param value the search query
    public void setCatalogSearchQuery(String value) {
        catalogSearchQuery.set(value);
    }

    /// Returns the catalog search-query property.
    ///
    /// @return the search-query property
    public StringProperty catalogSearchQueryProperty() {
        return catalogSearchQuery;
    }

    /// Returns the immutable Java runtime list.
    ///
    /// @return the runtime list
    public @UnmodifiableView ObservableList<HMCLDemoJavaRuntime> getJavaRuntimes() {
        return javaRuntimesView;
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

    /// Returns the multiplayer session phase.
    ///
    /// @return the phase
    public MultiplayerPhase getMultiplayerPhase() {
        return multiplayerPhase.get();
    }

    /// Returns the multiplayer-phase property.
    ///
    /// @return the property
    public ObjectProperty<MultiplayerPhase> multiplayerPhaseProperty() {
        return multiplayerPhase;
    }

    /// Returns the multiplayer room code.
    ///
    /// @return the room code, or an empty string while waiting
    public String getMultiplayerRoomCode() {
        return multiplayerRoomCode.get();
    }

    /// Returns the multiplayer room-code property.
    ///
    /// @return the property
    public StringProperty multiplayerRoomCodeProperty() {
        return multiplayerRoomCode;
    }

    /// Starts hosting and assigns a deterministic room code.
    ///
    /// @return the generated room code
    public String startHost() {
        int number = nextRoomNumber++;
        String code = String.format(Locale.ROOT, "HMCL-%04d", number);
        multiplayerRoomCode.set(code);
        multiplayerPhase.set(MultiplayerPhase.HOSTING);
        return code;
    }

    /// Starts joining using the supplied room code.
    ///
    /// @param roomCode the room code to join
    public void startJoin(String roomCode) {
        multiplayerRoomCode.set(roomCode);
        multiplayerPhase.set(MultiplayerPhase.JOINING);
    }

    /// Clears multiplayer session state.
    public void resetMultiplayer() {
        multiplayerPhase.set(MultiplayerPhase.WAITING);
        multiplayerRoomCode.set("");
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

    /// Returns the Material component profile.
    ///
    /// @return the profile
    public M3Profile getProfile() {
        return profile.get();
    }

    /// Sets the Material component profile.
    ///
    /// @param value the profile
    public void setProfile(M3Profile value) {
        profile.set(value);
    }

    /// Returns the profile property.
    ///
    /// @return the property
    public ObjectProperty<M3Profile> profileProperty() {
        return profile;
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

    /// Returns the launcher update channel.
    ///
    /// @return `stable` or `dev`
    public String getUpdateChannel() {
        return updateChannel.get();
    }

    /// Sets the launcher update channel.
    ///
    /// @param value the channel label
    public void setUpdateChannel(String value) {
        updateChannel.set(value);
    }

    /// Returns the update-channel property.
    ///
    /// @return the property
    public StringProperty updateChannelProperty() {
        return updateChannel;
    }

    /// Returns whether auto memory allocation is enabled.
    ///
    /// @return `true` when enabled
    public boolean isAutoAllocateMemory() {
        return autoAllocateMemory.get();
    }

    /// Sets whether auto memory allocation is enabled.
    ///
    /// @param value the flag
    public void setAutoAllocateMemory(boolean value) {
        autoAllocateMemory.set(value);
    }

    /// Returns the auto-allocate-memory property.
    ///
    /// @return the property
    public BooleanProperty autoAllocateMemoryProperty() {
        return autoAllocateMemory;
    }

    /// Returns the download thread count.
    ///
    /// @return the thread count
    public int getDownloadThreads() {
        return downloadThreads.get();
    }

    /// Sets the download thread count.
    ///
    /// @param value the thread count
    public void setDownloadThreads(int value) {
        downloadThreads.set(value);
    }

    /// Returns the download-threads property.
    ///
    /// @return the property
    public IntegerProperty downloadThreadsProperty() {
        return downloadThreads;
    }

    /// Returns the download source label.
    ///
    /// @return the source label
    public String getDownloadSource() {
        return downloadSource.get();
    }

    /// Sets the download source label.
    ///
    /// @param value the source label
    public void setDownloadSource(String value) {
        downloadSource.set(value);
    }

    /// Returns the download-source property.
    ///
    /// @return the property
    public StringProperty downloadSourceProperty() {
        return downloadSource;
    }

    /// Returns whether animations are disabled.
    ///
    /// @return `true` when disabled
    public boolean isAnimationDisabled() {
        return animationDisabled.get();
    }

    /// Sets whether animations are disabled.
    ///
    /// @param value the flag
    public void setAnimationDisabled(boolean value) {
        animationDisabled.set(value);
    }

    /// Returns the animation-disabled property.
    ///
    /// @return the property
    public BooleanProperty animationDisabledProperty() {
        return animationDisabled;
    }

    /// Returns the global max memory in megabytes.
    ///
    /// @return the memory limit
    public int getGlobalMaxMemoryMb() {
        return globalMaxMemoryMb.get();
    }

    /// Sets the global max memory in megabytes.
    ///
    /// @param value the memory limit
    public void setGlobalMaxMemoryMb(int value) {
        globalMaxMemoryMb.set(value);
    }

    /// Returns the global max-memory property.
    ///
    /// @return the property
    public IntegerProperty globalMaxMemoryMbProperty() {
        return globalMaxMemoryMb;
    }

    /// Returns the default game resolution label.
    public String getGlobalResolution() {
        return globalResolution.get();
    }

    /// Sets the default game resolution label.
    public void setGlobalResolution(String value) {
        globalResolution.set(value);
    }

    /// Returns the global-resolution property.
    public StringProperty globalResolutionProperty() {
        return globalResolution;
    }

    /// Returns the launcher visibility policy label.
    public String getLauncherVisibility() {
        return launcherVisibility.get();
    }

    /// Sets the launcher visibility policy label.
    public void setLauncherVisibility(String value) {
        launcherVisibility.set(value);
    }

    /// Returns the launcher-visibility property.
    public StringProperty launcherVisibilityProperty() {
        return launcherVisibility;
    }

    /// Returns the default isolation policy.
    public String getDefaultIsolation() {
        return defaultIsolation.get();
    }

    /// Sets the default isolation policy.
    public void setDefaultIsolation(String value) {
        defaultIsolation.set(value);
    }

    /// Returns the default-isolation property.
    public StringProperty defaultIsolationProperty() {
        return defaultIsolation;
    }

    /// Returns the selected Java runtime id, or `auto`.
    public String getSelectedJavaId() {
        return selectedJavaId.get();
    }

    /// Sets the selected Java runtime id, or `auto`.
    public void setSelectedJavaId(String value) {
        selectedJavaId.set(value);
    }

    /// Returns the selected-java property.
    public StringProperty selectedJavaIdProperty() {
        return selectedJavaId;
    }

    /// Returns whether preview updates are accepted.
    public boolean isAcceptPreviewUpdate() {
        return acceptPreviewUpdate.get();
    }

    /// Sets whether preview updates are accepted.
    public void setAcceptPreviewUpdate(boolean value) {
        acceptPreviewUpdate.set(value);
    }

    /// Returns the accept-preview-update property.
    public BooleanProperty acceptPreviewUpdateProperty() {
        return acceptPreviewUpdate;
    }

    /// Returns whether the automatic update dialog is suppressed.
    public boolean isDisableAutoShowUpdateDialog() {
        return disableAutoShowUpdateDialog.get();
    }

    /// Sets whether the automatic update dialog is suppressed.
    public void setDisableAutoShowUpdateDialog(boolean value) {
        disableAutoShowUpdateDialog.set(value);
    }

    /// Returns the disable-auto-show-update-dialog property.
    public BooleanProperty disableAutoShowUpdateDialogProperty() {
        return disableAutoShowUpdateDialog;
    }

    /// Returns whether April Fools content is disabled.
    public boolean isDisableAprilFools() {
        return disableAprilFools.get();
    }

    /// Sets whether April Fools content is disabled.
    public void setDisableAprilFools(boolean value) {
        disableAprilFools.set(value);
    }

    /// Returns the disable-april-fools property.
    public BooleanProperty disableAprilFoolsProperty() {
        return disableAprilFools;
    }

    /// Returns whether the title bar is transparent.
    public boolean isTitleBarTransparent() {
        return titleBarTransparent.get();
    }

    /// Sets whether the title bar is transparent.
    public void setTitleBarTransparent(boolean value) {
        titleBarTransparent.set(value);
    }

    /// Returns the title-bar-transparent property.
    public BooleanProperty titleBarTransparentProperty() {
        return titleBarTransparent;
    }

    /// Returns whether the outer window is transparent.
    public boolean isWindowTransparent() {
        return windowTransparent.get();
    }

    /// Sets whether the outer window is transparent.
    public void setWindowTransparent(boolean value) {
        windowTransparent.set(value);
    }

    /// Returns the window-transparent property.
    public BooleanProperty windowTransparentProperty() {
        return windowTransparent;
    }

    /// Returns the background opacity percentage.
    public int getBackgroundOpacity() {
        return backgroundOpacity.get();
    }

    /// Sets the background opacity percentage.
    public void setBackgroundOpacity(int value) {
        backgroundOpacity.set(value);
    }

    /// Returns the background-opacity property.
    public IntegerProperty backgroundOpacityProperty() {
        return backgroundOpacity;
    }

    /// Returns the version-list source id.
    public String getVersionListSource() {
        return versionListSource.get();
    }

    /// Sets the version-list source id.
    public void setVersionListSource(String value) {
        versionListSource.set(value);
    }

    /// Returns the version-list-source property.
    public StringProperty versionListSourceProperty() {
        return versionListSource;
    }

    /// Returns the file-download source id.
    public String getFileDownloadSource() {
        return fileDownloadSource.get();
    }

    /// Sets the file-download source id.
    public void setFileDownloadSource(String value) {
        fileDownloadSource.set(value);
    }

    /// Returns the file-download-source property.
    public StringProperty fileDownloadSourceProperty() {
        return fileDownloadSource;
    }

    /// Returns the default addon source id.
    public String getDefaultAddonSource() {
        return defaultAddonSource.get();
    }

    /// Sets the default addon source id.
    public void setDefaultAddonSource(String value) {
        defaultAddonSource.set(value);
    }

    /// Returns the default-addon-source property.
    public StringProperty defaultAddonSourceProperty() {
        return defaultAddonSource;
    }

    /// Returns whether download threads are chosen automatically.
    public boolean isAutoDownloadThreads() {
        return autoDownloadThreads.get();
    }

    /// Sets whether download threads are chosen automatically.
    public void setAutoDownloadThreads(boolean value) {
        autoDownloadThreads.set(value);
    }

    /// Returns the auto-download-threads property.
    public BooleanProperty autoDownloadThreadsProperty() {
        return autoDownloadThreads;
    }

    /// Returns the cache directory type.
    public String getCacheDirectoryType() {
        return cacheDirectoryType.get();
    }

    /// Sets the cache directory type.
    public void setCacheDirectoryType(String value) {
        cacheDirectoryType.set(value);
    }

    /// Returns the cache-directory-type property.
    public StringProperty cacheDirectoryTypeProperty() {
        return cacheDirectoryType;
    }

    /// Returns the proxy type.
    public String getProxyType() {
        return proxyType.get();
    }

    /// Sets the proxy type.
    public void setProxyType(String value) {
        proxyType.set(value);
    }

    /// Returns the proxy-type property.
    public StringProperty proxyTypeProperty() {
        return proxyType;
    }

    /// Returns the proxy host.
    public String getProxyHost() {
        return proxyHost.get();
    }

    /// Sets the proxy host.
    public void setProxyHost(String value) {
        proxyHost.set(value);
    }

    /// Returns the proxy-host property.
    public StringProperty proxyHostProperty() {
        return proxyHost;
    }

    /// Returns the proxy port.
    public int getProxyPort() {
        return proxyPort.get();
    }

    /// Sets the proxy port.
    public void setProxyPort(int value) {
        proxyPort.set(value);
    }

    /// Returns the proxy-port property.
    public IntegerProperty proxyPortProperty() {
        return proxyPort;
    }

    /// Returns whether proxy authentication is enabled.
    public boolean isProxyAuthentication() {
        return proxyAuthentication.get();
    }

    /// Sets whether proxy authentication is enabled.
    public void setProxyAuthentication(boolean value) {
        proxyAuthentication.set(value);
    }

    /// Returns the proxy-authentication property.
    public BooleanProperty proxyAuthenticationProperty() {
        return proxyAuthentication;
    }

    /// Replaces the selected instance in the list and selection properties.
    ///
    /// @param updated the replacement instance
    private void replaceSelectedInstance(HMCLDemoInstance updated) {
        HMCLDemoInstance current = selectedInstance.get();
        if (current == null) {
            return;
        }
        int index = instances.indexOf(current);
        if (index >= 0) {
            instances.set(index, updated);
        }
        selectedInstance.set(updated);
    }

    /// Updates mods on the selected instance when the operator returns a non-null list.
    ///
    /// @param operator returns the replacement list, or `null` to abort
    /// @return whether the instance was updated
    private boolean updateSelectedMods(
            Function<@Unmodifiable List<HMCLDemoMod>, @Nullable @Unmodifiable List<HMCLDemoMod>> operator
    ) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        @Nullable List<HMCLDemoMod> updated = operator.apply(instance.mods());
        if (updated == null) {
            return false;
        }
        replaceSelectedInstance(instance.withMods(updated));
        return true;
    }

    /// Updates resource packs on the selected instance when the operator returns a non-null list.
    ///
    /// @param operator returns the replacement list, or `null` to abort
    /// @return whether the instance was updated
    private boolean updateSelectedResourcePacks(
            Function<@Unmodifiable List<HMCLDemoPack>, @Nullable @Unmodifiable List<HMCLDemoPack>> operator
    ) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        @Nullable List<HMCLDemoPack> updated = operator.apply(instance.resourcePacks());
        if (updated == null) {
            return false;
        }
        replaceSelectedInstance(instance.withResourcePacks(updated));
        return true;
    }

    /// Updates shader packs on the selected instance when the operator returns a non-null list.
    ///
    /// @param operator returns the replacement list, or `null` to abort
    /// @return whether the instance was updated
    private boolean updateSelectedShaderPacks(
            Function<@Unmodifiable List<HMCLDemoPack>, @Nullable @Unmodifiable List<HMCLDemoPack>> operator
    ) {
        HMCLDemoInstance instance = selectedInstance.get();
        if (instance == null) {
            return false;
        }
        @Nullable List<HMCLDemoPack> updated = operator.apply(instance.shaderPacks());
        if (updated == null) {
            return false;
        }
        replaceSelectedInstance(instance.withShaderPacks(updated));
        return true;
    }

    /// Returns a pack list with one enabled flag flipped, or `null` when the id is absent.
    ///
    /// @param packs the source packs
    /// @param packId the pack identifier
    /// @param enabled the requested enabled state
    /// @return the updated list, or `null` when not found
    private static @Nullable List<HMCLDemoPack> togglePack(
            @Unmodifiable List<HMCLDemoPack> packs,
            String packId,
            boolean enabled
    ) {
        List<HMCLDemoPack> updated = new ArrayList<>(packs.size());
        boolean found = false;
        for (HMCLDemoPack pack : packs) {
            if (pack.id().equals(packId)) {
                updated.add(pack.withEnabled(enabled));
                found = true;
            } else {
                updated.add(pack);
            }
        }
        return found ? updated : null;
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
        boolean release = showReleaseVersions.get();
        boolean snapshot = showSnapshotVersions.get();
        boolean old = showOldVersions.get();
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

    /// Recomputes catalog predicates for every content kind.
    private void updateCatalogFilters() {
        String query = catalogSearchQuery.get().strip().toLowerCase(Locale.ROOT);
        catalogMods.setPredicate(item -> matchesCatalog(item, HMCLDemoCatalogItem.Kind.MOD, query));
        catalogModpacks.setPredicate(item -> matchesCatalog(item, HMCLDemoCatalogItem.Kind.MODPACK, query));
        catalogResourcePacks.setPredicate(item -> matchesCatalog(item, HMCLDemoCatalogItem.Kind.RESOURCE_PACK, query));
        catalogShaders.setPredicate(item -> matchesCatalog(item, HMCLDemoCatalogItem.Kind.SHADER, query));
        catalogWorlds.setPredicate(item -> matchesCatalog(item, HMCLDemoCatalogItem.Kind.WORLD, query));
    }

    /// Returns whether a catalog item matches the kind and optional query.
    ///
    /// @param item the catalog item
    /// @param kind the required kind
    /// @param query the lower-case query, or empty for no text filter
    /// @return whether the item is visible
    private static boolean matchesCatalog(HMCLDemoCatalogItem item, HMCLDemoCatalogItem.Kind kind, String query) {
        if (item.kind() != kind) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        return item.title().toLowerCase(Locale.ROOT).contains(query)
                || item.author().toLowerCase(Locale.ROOT).contains(query)
                || item.summary().toLowerCase(Locale.ROOT).contains(query);
    }

    /// Creates the default installer slot list for a game version and optional loader install.
    ///
    /// @param gameVersion the installed game version
    /// @param loaderId the installed loader family id, or `null` for vanilla
    /// @param loaderVersion the installed loader version when `loaderId` is set
    /// @return the installer list
    private static @Unmodifiable List<HMCLDemoInstaller> installers(
            String gameVersion,
            @Nullable String loaderId,
            @Nullable String loaderVersion
    ) {
        return List.of(
                new HMCLDemoInstaller("game", "Game", gameVersion),
                new HMCLDemoInstaller("fabric", "Fabric", "fabric".equals(loaderId) ? loaderVersion : null),
                new HMCLDemoInstaller("forge", "Forge", "forge".equals(loaderId) ? loaderVersion : null),
                new HMCLDemoInstaller("neoforge", "NeoForge", "neoforge".equals(loaderId) ? loaderVersion : null),
                new HMCLDemoInstaller("quilt", "Quilt", "quilt".equals(loaderId) ? loaderVersion : null),
                new HMCLDemoInstaller("liteLoader", "LiteLoader", null),
                new HMCLDemoInstaller("optifine", "OptiFine", null)
        );
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
                        true,
                        6144,
                        "1920x1080",
                        false,
                        "java-21",
                        installers("1.21.11", "fabric", "0.16.14"),
                        List.of(
                                new HMCLDemoMod("sodium", "Sodium",
                                        "sodium-fabric-0.7.3+mc1.21.11.jar", "0.7.3+mc1.21.11", true),
                                new HMCLDemoMod("iris", "Iris",
                                        "iris-fabric-1.9.6+mc1.21.11.jar", "1.9.6+mc1.21.11", true),
                                new HMCLDemoMod("modmenu", "Mod Menu",
                                        "modmenu-16.0.0-rc.1.jar", "16.0.0-rc.1", true),
                                new HMCLDemoMod("fabric-api", "Fabric API",
                                        "fabric-api-0.140.2+1.21.11.jar", "0.140.2+1.21.11", true),
                                new HMCLDemoMod("dynamic-fps", "Dynamic FPS",
                                        "dynamic-fps-3.11.3+minecraft-1.21.11-fabric.jar", "3.11.3", true),
                                new HMCLDemoMod("lithium", "Lithium",
                                        "lithium-fabric-mc1.21.11-0.16.2.jar", "0.16.2", true),
                                new HMCLDemoMod("entityculling", "Entity Culling",
                                        "entityculling-fabric-1.8.2-mc1.21.11.jar", "1.8.2", true),
                                new HMCLDemoMod("ferritecore", "FerriteCore",
                                        "ferritecore-8.0.0-fabric.jar", "8.0.0", true),
                                new HMCLDemoMod("immediatelyfast", "ImmediatelyFast",
                                        "ImmediatelyFast-Fabric-1.12.1+1.21.11.jar", "1.12.1", true),
                                new HMCLDemoMod("zoomify", "Zoomify",
                                        "Zoomify-2.14.6+1.21.11.jar", "2.14.6", true)
                        ),
                        List.of(
                                new HMCLDemoPack("faithful", "Faithful 32x", "1.21.11 / 32x", true),
                                new HMCLDemoPack("continuity", "Continuity", "3.0.0 / connected textures", true),
                                new HMCLDemoPack("lambdabettergrass", "LambdaBetterGrass", "1.7.4 / grass", false)
                        ),
                        List.of(
                                new HMCLDemoPack("complementary", "Complementary Unbound", "r5.5.1 / Iris", true),
                                new HMCLDemoPack("bsl", "BSL Shaders", "v8.4 / medium", false)
                        ),
                        List.of(
                                new HMCLDemoWorld("fo-survival", "Fabulously Survival", "Survival", "Yesterday"),
                                new HMCLDemoWorld("fo-creative", "Shader Showcase", "Creative", "3 days ago"),
                                new HMCLDemoWorld("fo-superflat", "Flat Lab", "Creative", "Last week")
                        ),
                        List.of(
                                new HMCLDemoPack("fo-house", "Starter House", "4,812 blocks", true),
                                new HMCLDemoPack("fo-farm", "Auto Farm", "2,104 blocks", true)
                        )
                ),
                new HMCLDemoInstance(
                        "snapshot-26-3-4",
                        "26.3 Snapshot 4",
                        "26.3-snapshot-4",
                        "Vanilla",
                        "minecraft",
                        "img/grass.png",
                        false,
                        4096,
                        "854x480",
                        false,
                        "auto",
                        installers("26.3-snapshot-4", null, null),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(
                                new HMCLDemoWorld("snap-test", "Snapshot Test", "Creative", "Today")
                        ),
                        List.of()
                ),
                new HMCLDemoInstance(
                        "forge-1-12-2",
                        "1.12.2-Forge_14.23.5.2860",
                        "1.12.2",
                        "Forge 14.23.5.2860",
                        "minecraft",
                        "img/furnace.png",
                        true,
                        4096,
                        "1280x720",
                        false,
                        "java-8",
                        installers("1.12.2", "forge", "14.23.5.2860"),
                        List.of(
                                new HMCLDemoMod("jei", "Just Enough Items",
                                        "jei_1.12.2-4.16.1.1000.jar", "4.16.1.1000", true),
                                new HMCLDemoMod("hwyla", "HWYLA",
                                        "Hwyla-1.8.26-B41_1.12.2.jar", "1.8.26", true),
                                new HMCLDemoMod("journeymap", "JourneyMap",
                                        "journeymap-1.12.2-5.7.1.jar", "5.7.1", false)
                        ),
                        List.of(
                                new HMCLDemoPack("default-dark", "Default Dark Mode", "1.12.2", true)
                        ),
                        List.of(),
                        List.of(
                                new HMCLDemoWorld("legacy-base", "Legacy Base", "Survival", "2024/8/12")
                        ),
                        List.of()
                ),
                new HMCLDemoInstance(
                        "release-1-21-11",
                        "1.21.11",
                        "1.21.11",
                        "Vanilla",
                        "minecraft",
                        "img/grass.png",
                        false,
                        4096,
                        "854x480",
                        true,
                        "auto",
                        installers("1.21.11", null, null),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(
                                new HMCLDemoWorld("vanilla-world", "New World", "Survival", "2 hours ago")
                        ),
                        List.of()
                ),
                new HMCLDemoInstance(
                        "lab-minecraft2",
                        "Modding Lab",
                        "1.20.1",
                        "NeoForge",
                        "minecraft2",
                        "img/command.png",
                        true,
                        8192,
                        "1600x900",
                        false,
                        "java-17",
                        installers("1.20.1", "neoforge", "47.1.106"),
                        List.of(
                                new HMCLDemoMod("create", "Create",
                                        "create-1.20.1-0.5.1.jar", "0.5.1", true),
                                new HMCLDemoMod("jei-neo", "Just Enough Items",
                                        "jei-1.20.1-15.20.0.jar", "15.20.0", true),
                                new HMCLDemoMod("flywheel", "Flywheel",
                                        "flywheel-forge-1.20.1-0.6.10.jar", "0.6.10", true)
                        ),
                        List.of(
                                new HMCLDemoPack("create-style", "Create Style", "1.20.1", true)
                        ),
                        List.of(),
                        List.of(
                                new HMCLDemoWorld("create-factory", "Create Factory", "Creative", "Last month")
                        ),
                        List.of(
                                new HMCLDemoPack("train-station", "Train Station", "18,440 blocks", true)
                        )
                )
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

    /// Creates download-catalog fixtures for every content kind.
    private static @Unmodifiable List<HMCLDemoCatalogItem> createCatalogItems() {
        return List.of(
                new HMCLDemoCatalogItem(
                        "mod-sodium", "Sodium", "JellySquid",
                        "Modern rendering engine with a huge FPS boost.", "48.2M",
                        HMCLDemoCatalogItem.Kind.MOD),
                new HMCLDemoCatalogItem(
                        "mod-iris", "Iris Shaders", "IrisShaders",
                        "Shader loader compatible with Sodium.", "21.4M",
                        HMCLDemoCatalogItem.Kind.MOD),
                new HMCLDemoCatalogItem(
                        "mod-jei", "Just Enough Items", "mezz",
                        "Item and recipe browser for almost every pack.", "96.1M",
                        HMCLDemoCatalogItem.Kind.MOD),
                new HMCLDemoCatalogItem(
                        "mod-create", "Create", "simibubi",
                        "Aesthetic engineering with kinetic power.", "33.8M",
                        HMCLDemoCatalogItem.Kind.MOD),
                new HMCLDemoCatalogItem(
                        "mod-lithium", "Lithium", "JellySquid",
                        "General-purpose server and client optimization.", "40.5M",
                        HMCLDemoCatalogItem.Kind.MOD),
                new HMCLDemoCatalogItem(
                        "mod-modmenu", "Mod Menu", "Terraformers",
                        "Adds a mods button and configuration screen.", "52.0M",
                        HMCLDemoCatalogItem.Kind.MOD),

                new HMCLDemoCatalogItem(
                        "pack-fo", "Fabulously Optimized", "robotkoer",
                        "A simple Minecraft modpack focused on performance.", "8.9M",
                        HMCLDemoCatalogItem.Kind.MODPACK),
                new HMCLDemoCatalogItem(
                        "pack-atm", "All the Mods 10", "ATMTeam",
                        "Kitchen-sink progression with hundreds of mods.", "4.1M",
                        HMCLDemoCatalogItem.Kind.MODPACK),
                new HMCLDemoCatalogItem(
                        "pack-cobblemon", "Cobblemon", "Cobblemon",
                        "Pokemon-inspired adventure on modern Minecraft.", "6.3M",
                        HMCLDemoCatalogItem.Kind.MODPACK),
                new HMCLDemoCatalogItem(
                        "pack-create-astral", "Create: Astral", "Lasky",
                        "Space-age Create progression and automation.", "1.8M",
                        HMCLDemoCatalogItem.Kind.MODPACK),
                new HMCLDemoCatalogItem(
                        "pack-better-mc", "Better MC", "LunaPixel",
                        "Vanilla+ exploration with quality-of-life mods.", "12.7M",
                        HMCLDemoCatalogItem.Kind.MODPACK),

                new HMCLDemoCatalogItem(
                        "rp-faithful", "Faithful 32x", "Faithful Team",
                        "A classic higher-resolution vanilla look.", "19.4M",
                        HMCLDemoCatalogItem.Kind.RESOURCE_PACK),
                new HMCLDemoCatalogItem(
                        "rp-default-dark", "Default Dark Mode", "nebuIr",
                        "Dark UI textures without changing gameplay art.", "7.2M",
                        HMCLDemoCatalogItem.Kind.RESOURCE_PACK),
                new HMCLDemoCatalogItem(
                        "rp-continuity", "Continuity", "PepperCode1",
                        "Connected textures and emissive support.", "5.6M",
                        HMCLDemoCatalogItem.Kind.RESOURCE_PACK),
                new HMCLDemoCatalogItem(
                        "rp-fresh", "Fresh Animations", "FreshLX",
                        "Entity animation overhaul for vanilla mobs.", "11.0M",
                        HMCLDemoCatalogItem.Kind.RESOURCE_PACK),
                new HMCLDemoCatalogItem(
                        "rp-xray", "Xray Ultimate", "TheXray",
                        "Highlight ores and valuable blocks.", "3.3M",
                        HMCLDemoCatalogItem.Kind.RESOURCE_PACK),

                new HMCLDemoCatalogItem(
                        "shader-complementary", "Complementary Shaders", "EminGT",
                        "Balanced visuals from potato to ultra settings.", "15.8M",
                        HMCLDemoCatalogItem.Kind.SHADER),
                new HMCLDemoCatalogItem(
                        "shader-bsl", "BSL Shaders", "capttatsu",
                        "Clean lighting with configurable profiles.", "12.1M",
                        HMCLDemoCatalogItem.Kind.SHADER),
                new HMCLDemoCatalogItem(
                        "shader-sildurs", "Sildur's Vibrant", "Sildur",
                        "Colorful outdoor lighting and water effects.", "9.4M",
                        HMCLDemoCatalogItem.Kind.SHADER),
                new HMCLDemoCatalogItem(
                        "shader-potato", "Potato Shaders", "RRe36",
                        "Ultra-lightweight shaders for low-end devices.", "4.7M",
                        HMCLDemoCatalogItem.Kind.SHADER),
                new HMCLDemoCatalogItem(
                        "shader-makeup", "MakeUp - Ultra Fast", "XavierFST",
                        "Fast cinematic look with modest cost.", "6.0M",
                        HMCLDemoCatalogItem.Kind.SHADER),

                new HMCLDemoCatalogItem(
                        "world-skyblock", "Classic Skyblock", "SkyGen",
                        "One tree, one island, endless progression.", "2.9M",
                        HMCLDemoCatalogItem.Kind.WORLD),
                new HMCLDemoCatalogItem(
                        "world-parkour", "Parkour Spiral", "JumpCraft",
                        "A tall spiral course with checkpoint stages.", "1.1M",
                        HMCLDemoCatalogItem.Kind.WORLD),
                new HMCLDemoCatalogItem(
                        "world-city", "Modern City", "BuildHub",
                        "A dense city map ready for adventure maps.", "3.5M",
                        HMCLDemoCatalogItem.Kind.WORLD),
                new HMCLDemoCatalogItem(
                        "world-survival", "Hardcore Survival Island", "Islanders",
                        "Sparse resources and dangerous nights.", "2.2M",
                        HMCLDemoCatalogItem.Kind.WORLD),
                new HMCLDemoCatalogItem(
                        "world-redstone", "Redstone Lab", "Circuitry",
                        "Prebuilt contraptions for testing machines.", "1.6M",
                        HMCLDemoCatalogItem.Kind.WORLD)
        );
    }

    /// Creates discovered Java runtime fixtures.
    private static @Unmodifiable List<HMCLDemoJavaRuntime> createJavaRuntimes() {
        return List.of(
                new HMCLDemoJavaRuntime(
                        "java-21",
                        "Microsoft Build of OpenJDK 21",
                        "21.0.6",
                        "C:\\Program Files\\Microsoft\\jdk-21.0.6.7-hotspot",
                        "x86_64"),
                new HMCLDemoJavaRuntime(
                        "java-17",
                        "Eclipse Temurin 17",
                        "17.0.14",
                        "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.14.7-hotspot",
                        "x86_64"),
                new HMCLDemoJavaRuntime(
                        "java-8",
                        "Eclipse Temurin 8",
                        "1.8.0_442",
                        "C:\\Program Files\\Eclipse Adoptium\\jdk-8.0.442.6-hotspot",
                        "x86_64")
        );
    }

    /// Describes the multiplayer session lifecycle for the offline demo.
    @NotNullByDefault
    public enum MultiplayerPhase {
        /// No multiplayer session is active.
        WAITING,

        /// The local player is hosting a room.
        HOSTING,

        /// The local player is joining a room.
        JOINING
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
