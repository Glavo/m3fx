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
import javafx.collections.ObservableSet;
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
/// The state is intentionally independent from user accounts, local game directories, and network repositories.
/// Its observable properties let pages demonstrate selection, search, installation, instance mutation, appearance,
/// and runtime language changes without external side effects.
@NotNullByDefault
public final class HMCLDemoState {
    /// Resolves strings and owns the runtime language property.
    private final HMCLDemoStrings strings;

    /// The mutable dummy-account source list.
    private final ObservableList<HMCLDemoAccount> accounts = FXCollections.observableArrayList();

    /// The read-only account-list view exposed to pages.
    private final @UnmodifiableView ObservableList<HMCLDemoAccount> accountsView =
            FXCollections.unmodifiableObservableList(accounts);

    /// The currently selected account, or `null` after the last account is removed.
    private final ObjectProperty<@Nullable HMCLDemoAccount> selectedAccount =
            new SimpleObjectProperty<>(this, "selectedAccount");

    /// The mutable dummy-instance source list.
    private final ObservableList<HMCLDemoInstance> instances = FXCollections.observableArrayList();

    /// The read-only instance-list view exposed to pages.
    private final @UnmodifiableView ObservableList<HMCLDemoInstance> instancesView =
            FXCollections.unmodifiableObservableList(instances);

    /// The currently selected instance, or `null` after the last instance is removed.
    private final ObjectProperty<@Nullable HMCLDemoInstance> selectedInstance =
            new SimpleObjectProperty<>(this, "selectedInstance");

    /// The fixed Discover catalog.
    private final ObservableList<HMCLDemoContent> contents = FXCollections.observableArrayList();

    /// The read-only catalog view exposed to pages.
    private final @UnmodifiableView ObservableList<HMCLDemoContent> contentsView =
            FXCollections.unmodifiableObservableList(contents);

    /// The catalog view filtered by [#searchQueryProperty()].
    private final FilteredList<HMCLDemoContent> filteredContents = new FilteredList<>(contents);

    /// The read-only filtered catalog view exposed to pages.
    private final @UnmodifiableView ObservableList<HMCLDemoContent> filteredContentsView =
            FXCollections.unmodifiableObservableList(filteredContents);

    /// The currently selected Discover item.
    private final ObjectProperty<@Nullable HMCLDemoContent> selectedContent =
            new SimpleObjectProperty<>(this, "selectedContent");

    /// The case-insensitive Discover search query.
    private final StringProperty searchQuery = new SimpleStringProperty(this, "searchQuery", "");

    /// The current foreground installation state.
    private final ObjectProperty<InstallState> installState =
            new SimpleObjectProperty<>(this, "installState", InstallState.AVAILABLE);

    /// The content associated with the foreground installation.
    private final ObjectProperty<@Nullable HMCLDemoContent> installingContent =
            new SimpleObjectProperty<>(this, "installingContent");

    /// The current foreground installation progress in the range from `0.0` through `1.0`.
    private final DoubleProperty installProgress = new SimpleDoubleProperty(this, "installProgress", 0.0);

    /// The stable identifiers of content already installed by the fixture or completed interaction.
    private final ObservableSet<String> installedContentIds = FXCollections.observableSet();

    /// The selected Material theme seed color.
    private final ObjectProperty<Color> themeColor =
            new SimpleObjectProperty<>(this, "themeColor", Color.web("#5C6BC0"));

    /// The selected light, dark, or system brightness mode.
    private final ObjectProperty<Brightness> brightness =
            new SimpleObjectProperty<>(this, "brightness", Brightness.SYSTEM);

    /// The selected dummy wallpaper.
    private final ObjectProperty<Wallpaper> wallpaper =
            new SimpleObjectProperty<>(this, "wallpaper", Wallpaper.MEADOW);

    /// The deterministic suffix counter used by [#copySelectedInstance()].
    private int nextCopyNumber = 1;

    /// The deterministic suffix counter used by [#addDummyAccount(HMCLDemoAccount.AccountType)].
    private int nextAccountNumber = 1;

    /// The deterministic suffix counter used by [#addDemoInstance()].
    private int nextInstanceNumber = 1;

    /// Creates a state object with its own locale-aware string resolver.
    public HMCLDemoState() {
        this(new HMCLDemoStrings());
    }

    /// Creates a state object backed by the supplied string resolver.
    ///
    /// @param strings the runtime localization service shared with the pages
    public HMCLDemoState(HMCLDemoStrings strings) {
        this.strings = strings;
        accounts.setAll(createAccounts());
        instances.setAll(createInstances());
        contents.setAll(createContents());
        installedContentIds.add("sodium-skies");
        selectedAccount.set(accounts.get(0));
        selectedInstance.set(instances.get(0));
        selectedContent.set(contents.get(0));
        searchQuery.addListener((observable, oldValue, newValue) -> updateContentFilter());
        updateContentFilter();
    }

    /// Returns the localization service used by this state.
    ///
    /// @return the string resolver
    public HMCLDemoStrings getStrings() {
        return strings;
    }

    /// Returns the immutable observable account list.
    ///
    /// @return the account-list view
    public @UnmodifiableView ObservableList<HMCLDemoAccount> getAccounts() {
        return accountsView;
    }

    /// Returns the selected account.
    ///
    /// @return the selected account, or `null` when the account list is empty
    public @Nullable HMCLDemoAccount getSelectedAccount() {
        return selectedAccount.get();
    }

    /// Selects an account by stable identifier.
    ///
    /// @param id the requested account identifier
    /// @return whether a matching account was selected
    public boolean selectAccount(String id) {
        for (HMCLDemoAccount account : accounts) {
            if (account.id().equals(id)) {
                selectedAccount.set(account);
                return true;
            }
        }
        return false;
    }

    /// Returns the selected-account property.
    ///
    /// @return the selected-account property
    public ObjectProperty<@Nullable HMCLDemoAccount> selectedAccountProperty() {
        return selectedAccount;
    }

    /// Removes the selected account and selects the nearest remaining account.
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

    /// Adds and selects a fictional account of the requested provider type.
    ///
    /// @param type the provider category shown by the new account
    /// @return the newly added account
    public HMCLDemoAccount addDummyAccount(HMCLDemoAccount.AccountType type) {
        int number = nextAccountNumber++;
        String provider = switch (type) {
            case MICROSOFT -> "Microsoft";
            case OFFLINE -> "Offline";
            case EXTERNAL -> "Community";
        };
        HMCLDemoAccount account = new HMCLDemoAccount(
                "demo-account-" + number,
                "Demo Player " + number,
                type,
                provider + " dummy profile",
                "D" + number
        );
        accounts.add(account);
        selectedAccount.set(account);
        return account;
    }

    /// Returns the immutable observable instance list.
    ///
    /// @return the instance-list view
    public @UnmodifiableView ObservableList<HMCLDemoInstance> getInstances() {
        return instancesView;
    }

    /// Returns the selected instance.
    ///
    /// @return the selected instance, or `null` when the instance list is empty
    public @Nullable HMCLDemoInstance getSelectedInstance() {
        return selectedInstance.get();
    }

    /// Selects an instance by stable identifier.
    ///
    /// @param id the requested instance identifier
    /// @return whether a matching instance was selected
    public boolean selectInstance(String id) {
        for (HMCLDemoInstance instance : instances) {
            if (instance.id().equals(id)) {
                selectedInstance.set(instance);
                return true;
            }
        }
        return false;
    }

    /// Returns the selected-instance property.
    ///
    /// @return the selected-instance property
    public ObjectProperty<@Nullable HMCLDemoInstance> selectedInstanceProperty() {
        return selectedInstance;
    }

    /// Copies the selected instance and selects the copy.
    ///
    /// @return the new copy, or `null` when no instance is selected
    public @Nullable HMCLDemoInstance copySelectedInstance() {
        HMCLDemoInstance source = selectedInstance.get();
        if (source == null) {
            return null;
        }
        int copyNumber = nextCopyNumber++;
        HMCLDemoInstance copy = source.copyAs(
                source.id() + "-copy-" + copyNumber,
                source.name() + " (Copy " + copyNumber + ")");
        int insertionIndex = instances.indexOf(source) + 1;
        instances.add(insertionIndex, copy);
        selectedInstance.set(copy);
        return copy;
    }

    /// Deletes the selected instance and selects the nearest remaining instance.
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

    /// Adds and selects a new fictional vanilla game instance.
    ///
    /// @return the newly added instance
    public HMCLDemoInstance addDemoInstance() {
        int number = nextInstanceNumber++;
        HMCLDemoInstance instance = new HMCLDemoInstance(
                "demo-instance-" + number,
                "New Profile " + number,
                "1.21.1",
                "Vanilla",
                "Never",
                "A locally generated profile used only by the UI demo.",
                "grass",
                HMCLDemoInstance.InstanceStatus.READY,
                List.of()
        );
        instances.add(0, instance);
        selectedInstance.set(instance);
        return instance;
    }

    /// Changes the enabled state of one mod in the selected instance.
    ///
    /// @param modId the stable mod identifier
    /// @param enabled the requested enabled state
    /// @return whether the selected instance contained that mod
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
        HMCLDemoInstance updatedInstance = instance.withMods(updatedMods);
        int index = instances.indexOf(instance);
        instances.set(index, updatedInstance);
        selectedInstance.set(updatedInstance);
        return true;
    }

    /// Returns the immutable full Discover catalog.
    ///
    /// @return the complete catalog view
    public @UnmodifiableView ObservableList<HMCLDemoContent> getContents() {
        return contentsView;
    }

    /// Returns the immutable catalog view matching the current search query.
    ///
    /// @return the filtered catalog view
    public @UnmodifiableView ObservableList<HMCLDemoContent> getFilteredContents() {
        return filteredContentsView;
    }

    /// Returns the current Discover search query.
    ///
    /// @return the search query
    public String getSearchQuery() {
        return searchQuery.get();
    }

    /// Sets the Discover search query.
    ///
    /// A blank query matches every item.
    ///
    /// @param value the new query
    public void setSearchQuery(String value) {
        searchQuery.set(value);
    }

    /// Returns the Discover search-query property.
    ///
    /// @return the search-query property
    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    /// Returns the selected Discover item.
    ///
    /// @return the selected content, or `null` if no item is selected
    public @Nullable HMCLDemoContent getSelectedContent() {
        return selectedContent.get();
    }

    /// Selects a Discover item by stable identifier.
    ///
    /// @param id the requested content identifier
    /// @return whether a matching item was selected
    public boolean selectContent(String id) {
        for (HMCLDemoContent content : contents) {
            if (content.id().equals(id)) {
                selectedContent.set(content);
                return true;
            }
        }
        return false;
    }

    /// Returns the selected-content property.
    ///
    /// @return the selected-content property
    public ObjectProperty<@Nullable HMCLDemoContent> selectedContentProperty() {
        return selectedContent;
    }

    /// Returns the installation state applicable to a catalog item.
    ///
    /// @param content the catalog item
    /// @return the current installation state for that item
    public InstallState installStateFor(HMCLDemoContent content) {
        if (installedContentIds.contains(content.id())) {
            return InstallState.INSTALLED;
        }
        return content.equals(installingContent.get()) ? installState.get() : InstallState.AVAILABLE;
    }

    /// Starts or retries installation of a catalog item.
    ///
    /// @param content the item to install
    /// @return `false` when the item is already installed, otherwise `true`
    public boolean startInstallation(HMCLDemoContent content) {
        if (installedContentIds.contains(content.id())) {
            return false;
        }
        selectedContent.set(content);
        installingContent.set(content);
        installProgress.set(0.0);
        installState.set(InstallState.INSTALLING);
        return true;
    }

    /// Updates foreground installation progress and completes installation at `1.0`.
    ///
    /// Values outside the supported range are clamped.
    ///
    /// @param value the requested progress
    /// @throws IllegalStateException if no installation is active
    public void setInstallProgress(double value) {
        if (installingContent.get() == null || installState.get() != InstallState.INSTALLING) {
            throw new IllegalStateException("No installation is active");
        }
        double normalized = Math.max(0.0, Math.min(1.0, value));
        installProgress.set(normalized);
        if (normalized >= 1.0) {
            completeInstallation();
        }
    }

    /// Marks the foreground installation as failed while retaining it for retry.
    ///
    /// @return whether an active installation was marked failed
    public boolean failInstallation() {
        if (installingContent.get() == null || installState.get() != InstallState.INSTALLING) {
            return false;
        }
        installState.set(InstallState.FAILED);
        return true;
    }

    /// Cancels the foreground installation or failure.
    ///
    /// @return whether an installation state was cleared
    public boolean cancelInstallation() {
        if (installingContent.get() == null) {
            return false;
        }
        installingContent.set(null);
        installProgress.set(0.0);
        installState.set(InstallState.AVAILABLE);
        return true;
    }

    /// Returns the foreground install-state property.
    ///
    /// @return the install-state property
    public ObjectProperty<InstallState> installStateProperty() {
        return installState;
    }

    /// Returns the content associated with the foreground installation.
    ///
    /// @return the installing content, or `null` when no installation is active
    public @Nullable HMCLDemoContent getInstallingContent() {
        return installingContent.get();
    }

    /// Returns the installing-content property.
    ///
    /// @return the installing-content property
    public ObjectProperty<@Nullable HMCLDemoContent> installingContentProperty() {
        return installingContent;
    }

    /// Returns foreground installation progress.
    ///
    /// @return a value from `0.0` through `1.0`
    public double getInstallProgress() {
        return installProgress.get();
    }

    /// Returns the foreground install-progress property.
    ///
    /// @return the progress property
    public DoubleProperty installProgressProperty() {
        return installProgress;
    }

    /// Returns the selected theme seed color.
    ///
    /// @return the theme color
    public Color getThemeColor() {
        return themeColor.get();
    }

    /// Sets the selected theme seed color.
    ///
    /// @param value the theme color
    public void setThemeColor(Color value) {
        themeColor.set(value);
    }

    /// Returns the theme-color property.
    ///
    /// @return the theme-color property
    public ObjectProperty<Color> themeColorProperty() {
        return themeColor;
    }

    /// Returns the selected brightness mode.
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
    /// @return the brightness property
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
    /// @return the wallpaper property
    public ObjectProperty<Wallpaper> wallpaperProperty() {
        return wallpaper;
    }

    /// Returns the selected runtime language.
    ///
    /// @return the normalized locale
    public Locale getLanguage() {
        return strings.getLocale();
    }

    /// Sets the runtime language.
    ///
    /// @param value the requested locale
    public void setLanguage(Locale value) {
        strings.setLocale(value);
    }

    /// Returns the runtime language property owned by the string resolver.
    ///
    /// @return the language property
    public ObjectProperty<Locale> languageProperty() {
        return strings.localeProperty();
    }

    /// Recomputes the Discover predicate from the current query.
    private void updateContentFilter() {
        String normalizedQuery = searchQuery.get().strip().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            filteredContents.setPredicate(content -> true);
            return;
        }
        filteredContents.setPredicate(content ->
                content.title().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || content.author().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || content.summary().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || content.kind().name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || content.gameVersions().stream()
                        .anyMatch(version -> version.toLowerCase(Locale.ROOT).contains(normalizedQuery)));
    }

    /// Completes and records the foreground installation.
    private void completeInstallation() {
        HMCLDemoContent content = installingContent.get();
        if (content == null) {
            throw new IllegalStateException("No installation is active");
        }
        installedContentIds.add(content.id());
        installProgress.set(1.0);
        installState.set(InstallState.INSTALLED);
    }

    /// Creates the three deterministic account fixtures.
    private static @Unmodifiable List<HMCLDemoAccount> createAccounts() {
        return List.of(
                new HMCLDemoAccount(
                        "river", "River Chen", HMCLDemoAccount.AccountType.MICROSOFT,
                        "Microsoft account", "RC"),
                new HMCLDemoAccount(
                        "maple", "MapleFox", HMCLDemoAccount.AccountType.OFFLINE,
                        "Offline profile", "MF"),
                new HMCLDemoAccount(
                        "orbit", "Orbit Builder", HMCLDemoAccount.AccountType.EXTERNAL,
                        "Community authentication", "OB")
        );
    }

    /// Creates the six deterministic instance fixtures.
    private static @Unmodifiable List<HMCLDemoInstance> createInstances() {
        return List.of(
                new HMCLDemoInstance(
                        "creative-workshop", "Creative Workshop", "1.21.4", "Fabric 0.16.10", "Today, 09:42",
                        "A lightweight building workspace with visual utilities.", "palette",
                        HMCLDemoInstance.InstanceStatus.READY,
                        List.of(
                                new HMCLDemoMod("worldedit", "WorldEdit", "7.3.10", true),
                                new HMCLDemoMod("sodium", "Sodium", "0.6.7", true),
                                new HMCLDemoMod("iris", "Iris Shaders", "1.8.1", false)
                        )),
                new HMCLDemoInstance(
                        "vanilla-evening", "Vanilla Evening", "1.21.4", "Vanilla", "Yesterday, 21:18",
                        "A clean survival profile for relaxed evening sessions.", "home",
                        HMCLDemoInstance.InstanceStatus.RUNNING,
                        List.of()),
                new HMCLDemoInstance(
                        "engineering-lab", "Engineering Lab", "1.20.1", "NeoForge 47.1", "Monday, 18:06",
                        "Automation experiments with a carefully curated mod set.", "settings",
                        HMCLDemoInstance.InstanceStatus.UPDATE_AVAILABLE,
                        List.of(
                                new HMCLDemoMod("create", "Create", "0.5.1", true),
                                new HMCLDemoMod("jei", "Just Enough Items", "15.20.0", true),
                                new HMCLDemoMod("journeymap", "JourneyMap", "5.10.3", true)
                        )),
                new HMCLDemoInstance(
                        "sky-islands", "Sky Islands", "1.20.4", "Quilt 0.26", "May 18, 14:33",
                        "A compact adventure pack designed around floating islands.", "navigation",
                        HMCLDemoInstance.InstanceStatus.READY,
                        List.of(
                                new HMCLDemoMod("trinkets", "Trinkets", "3.8.1", true),
                                new HMCLDemoMod("emi", "EMI", "1.1.13", true)
                        )),
                new HMCLDemoInstance(
                        "redstone-archive", "Redstone Archive", "1.19.2", "Forge 43.4", "April 02, 10:15",
                        "Archived technical worlds and compatibility test maps.", "archive",
                        HMCLDemoInstance.InstanceStatus.NEEDS_REPAIR,
                        List.of(
                                new HMCLDemoMod("architectury", "Architectury API", "6.6.92", true),
                                new HMCLDemoMod("configured", "Configured", "2.1.1", false)
                        )),
                new HMCLDemoInstance(
                        "snapshot-playground", "Snapshot Playground", "25w18a", "Vanilla", "March 27, 16:48",
                        "A disposable profile for previewing upcoming game changes.", "spark",
                        HMCLDemoInstance.InstanceStatus.READY,
                        List.of())
        );
    }

    /// Creates the eight deterministic Discover fixtures.
    private static @Unmodifiable List<HMCLDemoContent> createContents() {
        return List.of(
                new HMCLDemoContent(
                        "sodium-skies", "Sodium Skies", "Northwind Labs",
                        "Smooth rendering defaults for modest hardware.",
                        HMCLDemoContent.ContentKind.MOD, List.of("1.21.4", "1.21.1"),
                        "spark", 12_480_000L, true),
                new HMCLDemoContent(
                        "builders-compass", "Builder's Compass", "Cedar Works",
                        "Fast navigation tools for large creative projects.",
                        HMCLDemoContent.ContentKind.MOD, List.of("1.21.4", "1.20.6"),
                        "navigation", 2_870_000L, true),
                new HMCLDemoContent(
                        "copper-horizons", "Copper Horizons", "Amber Studio",
                        "A warm exploration pack with compact automation.",
                        HMCLDemoContent.ContentKind.MODPACK, List.of("1.21.1"),
                        "work", 864_000L, true),
                new HMCLDemoContent(
                        "quiet-blocks", "Quiet Blocks", "Moss Collective",
                        "Soft, readable textures for focused building sessions.",
                        HMCLDemoContent.ContentKind.RESOURCE_PACK, List.of("1.21.4", "1.21.1", "1.20.4"),
                        "image", 4_310_000L, false),
                new HMCLDemoContent(
                        "aurora-path", "Aurora Path", "Prism Workshop",
                        "Colorful skies and restrained cinematic lighting.",
                        HMCLDemoContent.ContentKind.SHADER_PACK, List.of("1.21.4", "1.20.1"),
                        "visibility", 6_920_000L, true),
                new HMCLDemoContent(
                        "inventory-notes", "Inventory Notes", "Paper Crane",
                        "Pins short reminders beside frequently used items.",
                        HMCLDemoContent.ContentKind.MOD, List.of("1.21.4"),
                        "task", 740_000L, false),
                new HMCLDemoContent(
                        "weekend-vanilla-plus", "Weekend Vanilla Plus", "Campfire Team",
                        "A small cooperative pack that stays close to vanilla.",
                        HMCLDemoContent.ContentKind.MODPACK, List.of("1.20.1"),
                        "group", 1_920_000L, false),
                new HMCLDemoContent(
                        "paper-cut-ui", "Paper Cut UI", "Mono Lake",
                        "High-contrast interface textures with clear item silhouettes.",
                        HMCLDemoContent.ContentKind.RESOURCE_PACK, List.of("1.21.4", "1.21.1"),
                        "dashboard", 525_000L, false)
        );
    }

    /// Describes installation state for one Discover item.
    @NotNullByDefault
    public enum InstallState {
        /// The item can be installed.
        AVAILABLE,

        /// The item is currently installing.
        INSTALLING,

        /// The item is installed.
        INSTALLED,

        /// The most recent installation attempt failed and may be retried.
        FAILED
    }

    /// Describes the selected application brightness mode.
    @NotNullByDefault
    public enum Brightness {
        /// Follows the host operating-system preference.
        SYSTEM,

        /// Forces a light color scheme.
        LIGHT,

        /// Forces a dark color scheme.
        DARK
    }

    /// Identifies one generated, non-branded demo wallpaper.
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
