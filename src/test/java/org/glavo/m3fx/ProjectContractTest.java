// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies public API, compatibility, and stylesheet resource contracts that are not covered by compiler checks.
@NotNullByDefault
final class ProjectContractTest {
    /// Production source roots scanned for project contract checks.
    private static final @Unmodifiable List<Path> PRODUCTION_SOURCE_ROOTS = List.of(
            Path.of("src", "main", "java"),
            Path.of("demo", "src", "main", "java")
    );

    /// The main module descriptor used to discover exported API packages.
    private static final Path MAIN_MODULE_INFO = Path.of("src", "main", "java", "module-info.java");

    /// Matches exported package declarations in the main module descriptor.
    private static final Pattern MODULE_EXPORT_DECLARATION = Pattern.compile("^\\s*exports\\s+([\\w.]+)\\s*;");

    /// Matches implementation type references that exported APIs must not expose through members.
    private static final Pattern EXPORTED_INTERNAL_API_REFERENCE =
            Pattern.compile("\\borg\\.glavo\\.m3fx\\.internal\\b|\\bM3Internal\\w*\\b");

    /// The package set intentionally exported as the public JPMS API surface.
    private static final @Unmodifiable Set<String> EXPECTED_EXPORTED_API_PACKAGES = Set.of(
            "org.glavo.m3fx.animation",
            "org.glavo.m3fx.controls",
            "org.glavo.m3fx.theme",
            "org.glavo.m3fx.tokens"
    );

    /// The expected public top-level types exported by each API package.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>> EXPECTED_EXPORTED_TOP_LEVEL_TYPES =
            Map.of(
                    "org.glavo.m3fx.animation",
                    typeNames("""
                            M3Motion
                            M3MotionBehavior
                            M3MotionEasing
                            M3MotionScheme
                            M3MotionSettings
                            M3MotionSpec
                            """),
                    "org.glavo.m3fx.controls",
                    typeNames("""
                            M3Avatar
                            M3AvatarVariant
                            M3Badge
                            M3BadgedBox
                            M3Banner
                            M3BottomAppBar
                            M3BottomAppBarFloatingActionAlignment
                            M3BottomSheet
                            M3Button
                            M3ButtonGroup
                            M3ButtonGroupSize
                            M3ButtonGroupVariant
                            M3ButtonVariant
                            M3Card
                            M3CardVariant
                            M3Carousel
                            M3CheckBox
                            M3Chip
                            M3ChipGroup
                            M3ChipSelectionMode
                            M3ChipStyle
                            M3ChipVariant
                            M3DatePicker
                            M3DatePickerDialog
                            M3DatePickerField
                            M3DatePreset
                            M3DatePresets
                            M3DateRange
                            M3DateRangePicker
                            M3DateRangePickerDialog
                            M3DateRangePickerField
                            M3DateRangePreset
                            M3DateRangePresets
                            M3Dialog
                            M3DialogPane
                            M3Divider
                            M3FabMenu
                            M3FloatingActionButton
                            M3FloatingActionButtonSize
                            M3FloatingActionButtonVariant
                            M3FormPane
                            M3FormRow
                            M3FormSection
                            M3FormValidator
                            M3Icon
                            M3IconButton
                            M3IconButtonShape
                            M3IconButtonSize
                            M3IconButtonWidth
                            M3IconSize
                            M3IconToggleButton
                            M3IconToggleButtonGroup
                            M3IconToggleButtonSelectionMode
                            M3IconToggleButtonVariant
                            M3IconVariant
                            M3ListItem
                            M3ListItemLineCount
                            M3ListItemSlotSize
                            M3ListPane
                            M3ListSectionHeader
                            M3ListSelectionMode
                            M3ListView
                            M3LoadingIndicator
                            M3LoadingIndicatorVariant
                            M3Menu
                            M3MenuButton
                            M3MenuColorStyle
                            M3MenuItem
                            M3MenuSectionHeader
                            M3MenuSelectionMode
                            M3NavigationBar
                            M3NavigationDrawer
                            M3NavigationDrawerGroup
                            M3NavigationItem
                            M3NavigationRail
                            M3PasswordField
                            M3PickerField
                            M3ProgressBar
                            M3ProgressIndicator
                            M3RadioButton
                            M3RichTooltip
                            M3Scrim
                            M3ScrollPanes
                            M3SearchBar
                            M3SearchView
                            M3SegmentedButton
                            M3SegmentedButtonGroup
                            M3SegmentedButtonSelectionMode
                            M3SheetVariant
                            M3SideSheet
                            M3Slider
                            M3Snackbar
                            M3SnackbarHost
                            M3SplitButton
                            M3SplitButtonSize
                            M3SubMenuItem
                            M3Surface
                            M3SurfaceElevation
                            M3SurfaceVariant
                            M3Switch
                            M3Tab
                            M3TabBar
                            M3Text
                            M3TextArea
                            M3TextField
                            M3TextInput
                            M3TextInputLayout
                            M3TextInputValidator
                            M3TextInputValidators
                            M3TextInputVariant
                            M3TextRole
                            M3TimePicker
                            M3TimePickerDialog
                            M3TimePickerField
                            M3TimePreset
                            M3TimePresets
                            M3Toolbar
                            M3ToolbarVariant
                            M3Tooltip
                            M3TopAppBar
                            M3TopAppBarVariant
                            M3ValidationSummary
                            """),
                    "org.glavo.m3fx.theme",
                    typeNames("""
                            M3Theme
                            M3ThemeManager
                            """),
                    "org.glavo.m3fx.tokens",
                    typeNames("""
                            M3ColorTokens
                            M3ComponentTokens
                            M3Density
                            M3ElevationTokens
                            M3MotionTokens
                            M3Profile
                            M3ShapeTokens
                            M3StateLayerTokens
                            M3TextStyle
                            M3TokenSet
                            M3TypographyTokens
                            """)
            );

    /// The public control source root scanned for M3FX control API shape constraints.
    private static final Path CONTROLS_SOURCE_ROOT =
            Path.of("src", "main", "java", "org", "glavo", "m3fx", "controls");

    /// The public skin source root scanned for M3FX skin inheritance constraints.
    private static final Path SKINS_SOURCE_ROOT =
            Path.of("src", "main", "java", "org", "glavo", "m3fx", "skins");

    /// Direct calls to JavaFX APIs introduced after the JavaFX 14 compatibility baseline.
    private static final @Unmodifiable List<String> NEWER_JAVA_FX_API_DIRECT_CALL_FRAGMENTS = List.of(
            ".focusVisibleProperty(",
            ".isFocusVisible(",
            ".focusWithinProperty(",
            ".isFocusWithin(",
            "Platform.getPreferences("
    );

    /// The bundled stylesheet resource root.
    private static final Path STYLESHEET_RESOURCE_ROOT =
            Path.of("src", "main", "resources", "org", "glavo", "m3fx", "styles");

    /// The bundled control stylesheet resource root.
    private static final Path CONTROL_STYLESHEET_RESOURCE_ROOT =
            STYLESHEET_RESOURCE_ROOT.resolve("controls");

    /// The demo stylesheet resource root.
    private static final Path DEMO_STYLESHEET_RESOURCE_ROOT =
            Path.of("demo", "src", "main", "resources", "org", "glavo", "m3fx", "demo");

    /// Matches public control class declarations with one direct superclass.
    private static final Pattern PUBLIC_CONTROL_EXTENDS_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "class\\s+(\\w+)\\s*(?:<[^>{}]+>\\s*)?extends\\s+(?:[\\w.]+\\.)?(\\w+)\\b"
    );

    /// Matches public control class declarations.
    private static final Pattern PUBLIC_CONTROL_CLASS_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?class\\s+\\w+\\b"
    );

    /// Matches public top-level type declarations and captures their simple name.
    private static final Pattern PUBLIC_TOP_LEVEL_TYPE_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:(?:abstract|final|sealed|non-sealed)\\s+)*"
                    + "(?:class|interface|enum|record)\\s+(\\w+)\\b"
    );

    /// Matches Java type declarations in production source files.
    private static final Pattern TYPE_DECLARATION = Pattern.compile(
            "^\\s*(?:public\\s+)?(?:(?:abstract|final|sealed|non-sealed)\\s+)*"
                    + "(?:class|interface|enum|record)\\s+\\w+\\b",
            Pattern.MULTILINE
    );

    /// Public control type names that would expose implementation nodes rather than user-facing controls.
    private static final @Unmodifiable Set<String> FORBIDDEN_PUBLIC_CONTROL_TYPE_NAMES = Set.of(
            "M3DisclosureIcon",
            "M3ListViewCell"
    );

    /// Public control type suffixes reserved for implementation details.
    private static final @Unmodifiable List<String> FORBIDDEN_PUBLIC_CONTROL_TYPE_SUFFIXES = List.of(
            "Cell",
            "Context",
            "Helper",
            "Impl",
            "Installation",
            "Resolver",
            "Skin",
            "State",
            "Stylesheets",
            "Synchronizer"
    );

    /// Matches public skin class declarations with one direct superclass.
    private static final Pattern PUBLIC_SKIN_EXTENDS_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "class\\s+(\\w+)\\s*(?:<[^>{}]+>\\s*)?extends\\s+(?:[\\w.]+\\.)?(\\w+)\\b"
    );

    /// Matches public skin classes that directly implement the JavaFX `Skin` interface.
    private static final Pattern PUBLIC_SKIN_DIRECT_IMPLEMENTATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "class\\s+(\\w+)\\s*(?:<[^>{}]+>\\s*)?"
                    + "(?:(?:extends\\s+[^{}]+?)\\s+)?implements\\s+(?:[\\w.]+\\.)?Skin\\b"
    );

    /// Matches `M3Stylesheets.controlStylesheet` references in production sources.
    private static final Pattern CONTROL_STYLESHEET_REFERENCE = Pattern.compile(
            "controlStylesheet\\(\"([^\"]+\\.css)\"\\)"
    );

    /// Matches stylesheet imports in bundled CSS resources.
    private static final Pattern STYLESHEET_IMPORT_REFERENCE = Pattern.compile(
            "@import\\s+\"([^\"]+\\.css)\"\\s*;"
    );

    /// Matches M3FX and MonetFX CSS custom properties in bundled CSS resources.
    private static final Pattern TOKEN_REFERENCE = Pattern.compile(
            "(?<![\\w-])-(?:m3|monet)-[a-z0-9-]+\\b"
    );

    /// Matches M3FX and MonetFX CSS custom property declarations.
    private static final Pattern TOKEN_DECLARATION = Pattern.compile(
            "(?<![\\w-])(-(?:m3|monet)-[a-z0-9-]+)\\s*:"
    );

    /// Matches CSS declarations that accidentally turn token lookups into strings.
    private static final Pattern QUOTED_TOKEN_VALUE = Pattern.compile(
            "^\\s*[\\w-]+\\s*:\\s*[\"']-(?:m3|monet)-[a-z0-9-]+[\"']"
    );

    /// Matches public `STYLE_CLASS` string declarations.
    private static final Pattern STYLE_CLASS_DECLARATION = Pattern.compile(
            "\\bSTYLE_CLASS\\s*=\\s*\"([^\"]+)\""
    );

    /// Matches CSS rules in generated component stylesheets.
    private static final Pattern CSS_RULE = Pattern.compile("(?s)([^{}]+)\\{([^{}]*)}");

    /// Matches class selectors inside a single CSS selector.
    private static final Pattern CSS_CLASS_SELECTOR = Pattern.compile("\\.([A-Za-z][A-Za-z0-9_-]*)");

    /// Matches selector structure that targets descendants, siblings, or children rather than one root node.
    private static final Pattern SELECTOR_HAS_DESCENDANT_OR_COMBINATOR = Pattern.compile("[\\s>+~]");

    /// Matches custom CSS metadata property declarations in public controls.
    private static final Pattern CUSTOM_CSS_METADATA_PROPERTY = Pattern.compile(
            "(?:new\\s+CssMetaData<>|createSizeCssMetaData)\\(\\s*\"([^\"]+)\""
    );

    /// Matches public `ObservableList` getter declarations in public control sources.
    private static final Pattern PUBLIC_OBSERVABLE_LIST_GETTER_DECLARATION = Pattern.compile(
            "public\\s+(?:final\\s+)?(@UnmodifiableView\\s+)?ObservableList\\s*<[^>]+>\\s+(get\\w+)\\s*\\("
    );

    /// The supported public static utility methods intentionally exported from the `controls` package.
    private static final @Unmodifiable Set<String> SUPPORTED_PUBLIC_CONTROL_PACKAGE_STATIC_METHODS = Set.of(
            "org.glavo.m3fx.controls.M3DatePresets#common(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DatePresets#daysFrom(java.time.LocalDate,int)",
            "org.glavo.m3fx.controls.M3DatePresets#nextMonthStart(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DatePresets#today(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DatePresets#tomorrow(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DatePresets#thisMonthStart(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DatePresets#yesterday(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DateRangePresets#common(java.time.LocalDate,java.time.DayOfWeek)",
            "org.glavo.m3fx.controls.M3DateRangePresets#nextDays(java.time.LocalDate,int)",
            "org.glavo.m3fx.controls.M3DateRangePresets#nextMonth(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DateRangePresets#nextWeek(java.time.LocalDate,java.time.DayOfWeek)",
            "org.glavo.m3fx.controls.M3DateRangePresets#previousDays(java.time.LocalDate,int)",
            "org.glavo.m3fx.controls.M3DateRangePresets#thisMonth(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DateRangePresets#thisWeek(java.time.LocalDate,java.time.DayOfWeek)",
            "org.glavo.m3fx.controls.M3DateRangePresets#today(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3DateRangePresets#tomorrow(java.time.LocalDate)",
            "org.glavo.m3fx.controls.M3ScrollPanes#disableSmoothScrolling(javafx.scene.control.ScrollPane)",
            "org.glavo.m3fx.controls.M3ScrollPanes#enableSmoothScrolling(javafx.scene.control.ScrollPane)",
            "org.glavo.m3fx.controls.M3ScrollPanes#isSmoothScrollingEnabled(javafx.scene.control.ScrollPane)",
            "org.glavo.m3fx.controls.M3ScrollPanes#style(javafx.scene.control.ScrollBar)",
            "org.glavo.m3fx.controls.M3ScrollPanes#style(javafx.scene.control.ScrollPane)",
            "org.glavo.m3fx.controls.M3TextInputValidators#all(org.glavo.m3fx.controls.M3TextInputValidator[])",
            "org.glavo.m3fx.controls.M3TextInputValidators#lengthBetween(int,int,java.lang.String,java.lang.String)",
            "org.glavo.m3fx.controls.M3TextInputValidators#maxLength(int,java.lang.String)",
            "org.glavo.m3fx.controls.M3TextInputValidators#minLength(int,java.lang.String)",
            "org.glavo.m3fx.controls.M3TextInputValidators#none()",
            "org.glavo.m3fx.controls.M3TextInputValidators#pattern(java.util.regex.Pattern,java.lang.String)",
            "org.glavo.m3fx.controls.M3TextInputValidators#predicate(java.util.function.BiPredicate,java.lang.String)",
            "org.glavo.m3fx.controls.M3TextInputValidators#required(java.lang.String)",
            "org.glavo.m3fx.controls.M3TimePresets#afternoon()",
            "org.glavo.m3fx.controls.M3TimePresets#common(java.time.LocalTime)",
            "org.glavo.m3fx.controls.M3TimePresets#evening()",
            "org.glavo.m3fx.controls.M3TimePresets#midnight()",
            "org.glavo.m3fx.controls.M3TimePresets#minutesFrom(java.time.LocalTime,int)",
            "org.glavo.m3fx.controls.M3TimePresets#morning()",
            "org.glavo.m3fx.controls.M3TimePresets#noon()",
            "org.glavo.m3fx.controls.M3TimePresets#now(java.time.LocalTime)",
            "org.glavo.m3fx.controls.M3Tooltip#install(javafx.scene.Node,org.glavo.m3fx.controls.M3Tooltip)",
            "org.glavo.m3fx.controls.M3Tooltip#uninstall(javafx.scene.Node,org.glavo.m3fx.controls.M3Tooltip)"
    );

    /// Public `controls` package utility types that must remain stateless static entry points.
    private static final @Unmodifiable Set<String> PUBLIC_CONTROL_UTILITY_TYPES = Set.of(
            "org.glavo.m3fx.controls.M3DatePresets",
            "org.glavo.m3fx.controls.M3DateRangePresets",
            "org.glavo.m3fx.controls.M3ScrollPanes",
            "org.glavo.m3fx.controls.M3TextInputValidators",
            "org.glavo.m3fx.controls.M3TimePresets"
    );

    /// The supported public static utility methods intentionally exported outside the `controls` package.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>>
            SUPPORTED_PUBLIC_STATIC_METHODS_BY_NON_CONTROL_PACKAGE = Map.of(
            "org.glavo.m3fx.animation",
            typeNames("""
                    org.glavo.m3fx.animation.M3MotionBehavior#create(javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration,javafx.util.Duration)
                    org.glavo.m3fx.animation.M3MotionBehavior#expressive()
                    org.glavo.m3fx.animation.M3MotionBehavior#standard()
                    org.glavo.m3fx.animation.M3MotionScheme#create(org.glavo.m3fx.animation.M3MotionSpec,org.glavo.m3fx.animation.M3MotionSpec,org.glavo.m3fx.animation.M3MotionSpec,org.glavo.m3fx.animation.M3MotionSpec,org.glavo.m3fx.animation.M3MotionSpec,org.glavo.m3fx.animation.M3MotionSpec)
                    org.glavo.m3fx.animation.M3MotionScheme#expressive()
                    org.glavo.m3fx.animation.M3MotionScheme#standard()
                    org.glavo.m3fx.animation.M3MotionSettings#addSettingsChangeListener(javafx.beans.InvalidationListener)
                    org.glavo.m3fx.animation.M3MotionSettings#animationsEnabledProperty()
                    org.glavo.m3fx.animation.M3MotionSettings#areAnimationsEnabled()
                    org.glavo.m3fx.animation.M3MotionSettings#areAnimationsEnabled(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#clearAnimationsEnabled(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#clearMotionBehavior(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#clearMotionScheme(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#getAnimationsEnabled(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#getMotionBehavior()
                    org.glavo.m3fx.animation.M3MotionSettings#getMotionBehavior(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#getMotionScheme()
                    org.glavo.m3fx.animation.M3MotionSettings#getMotionScheme(javafx.scene.Node)
                    org.glavo.m3fx.animation.M3MotionSettings#motionBehaviorProperty()
                    org.glavo.m3fx.animation.M3MotionSettings#motionSchemeProperty()
                    org.glavo.m3fx.animation.M3MotionSettings#removeSettingsChangeListener(javafx.beans.InvalidationListener)
                    org.glavo.m3fx.animation.M3MotionSettings#revisionProperty()
                    org.glavo.m3fx.animation.M3MotionSettings#setAnimationsEnabled(boolean)
                    org.glavo.m3fx.animation.M3MotionSettings#setAnimationsEnabled(javafx.scene.Node,java.lang.Boolean)
                    org.glavo.m3fx.animation.M3MotionSettings#setMotionBehavior(org.glavo.m3fx.animation.M3MotionBehavior)
                    org.glavo.m3fx.animation.M3MotionSettings#setMotionBehavior(javafx.scene.Node,org.glavo.m3fx.animation.M3MotionBehavior)
                    org.glavo.m3fx.animation.M3MotionSettings#setMotionScheme(org.glavo.m3fx.animation.M3MotionScheme)
                    org.glavo.m3fx.animation.M3MotionSettings#setMotionScheme(javafx.scene.Node,org.glavo.m3fx.animation.M3MotionScheme)
                    org.glavo.m3fx.animation.M3MotionSpec#create(javafx.util.Duration,org.glavo.m3fx.animation.M3MotionEasing)
                    """),
            "org.glavo.m3fx.theme",
            typeNames("""
                    org.glavo.m3fx.theme.M3Theme#defaultTheme()
                    org.glavo.m3fx.theme.M3Theme#fromColorScheme(org.glavo.monetfx.ColorScheme)
                    org.glavo.m3fx.theme.M3Theme#fromColorScheme(org.glavo.m3fx.tokens.M3Profile,org.glavo.monetfx.ColorScheme)
                    org.glavo.m3fx.theme.M3Theme#fromColorScheme(org.glavo.m3fx.tokens.M3Profile,org.glavo.monetfx.ColorScheme,org.glavo.m3fx.tokens.M3Density)
                    org.glavo.m3fx.theme.M3Theme#fromSeed(javafx.scene.paint.Color)
                    org.glavo.m3fx.theme.M3Theme#fromSeed(javafx.scene.paint.Color,org.glavo.monetfx.Brightness)
                    org.glavo.m3fx.theme.M3Theme#fromSeed(javafx.scene.paint.Color,org.glavo.m3fx.tokens.M3Profile,org.glavo.monetfx.Brightness)
                    org.glavo.m3fx.theme.M3Theme#fromSeed(javafx.scene.paint.Color,org.glavo.m3fx.tokens.M3Profile,org.glavo.monetfx.Brightness,org.glavo.m3fx.tokens.M3Density)
                    org.glavo.m3fx.theme.M3Theme#fromTokenSet(org.glavo.m3fx.tokens.M3Profile,org.glavo.monetfx.ColorScheme,org.glavo.m3fx.tokens.M3Density,org.glavo.m3fx.tokens.M3TokenSet)
                    org.glavo.m3fx.theme.M3ThemeManager#applyThemeStyleClasses(javafx.css.Styleable,org.glavo.m3fx.theme.M3Theme)
                    org.glavo.m3fx.theme.M3ThemeManager#clearThemeStyleClasses(javafx.css.Styleable)
                    org.glavo.m3fx.theme.M3ThemeManager#copyThemeContext(javafx.scene.Parent,javafx.scene.Parent)
                    org.glavo.m3fx.theme.M3ThemeManager#getTheme(javafx.scene.Parent)
                    org.glavo.m3fx.theme.M3ThemeManager#getTheme(javafx.scene.Scene)
                    org.glavo.m3fx.theme.M3ThemeManager#install(javafx.scene.Parent,org.glavo.m3fx.theme.M3Theme)
                    org.glavo.m3fx.theme.M3ThemeManager#install(javafx.scene.Scene,org.glavo.m3fx.theme.M3Theme)
                    org.glavo.m3fx.theme.M3ThemeManager#installStylesheet(javafx.scene.Scene)
                    org.glavo.m3fx.theme.M3ThemeManager#installThemeStylesheet(javafx.scene.Scene,org.glavo.m3fx.theme.M3Theme)
                    org.glavo.m3fx.theme.M3ThemeManager#installUserAgentStylesheet()
                    org.glavo.m3fx.theme.M3ThemeManager#stylesheetUrl()
                    org.glavo.m3fx.theme.M3ThemeManager#themeStylesheetUrl(org.glavo.m3fx.theme.M3Theme)
                    org.glavo.m3fx.theme.M3ThemeManager#uninstall(javafx.scene.Parent)
                    org.glavo.m3fx.theme.M3ThemeManager#uninstall(javafx.scene.Scene)
                    org.glavo.m3fx.theme.M3ThemeManager#uninstallStylesheet(javafx.scene.Scene)
                    org.glavo.m3fx.theme.M3ThemeManager#uninstallThemeStylesheet(javafx.scene.Scene)
                    """),
            "org.glavo.m3fx.tokens",
            typeNames("""
                    org.glavo.m3fx.tokens.M3ColorTokens#create(org.glavo.monetfx.ColorScheme)
                    org.glavo.m3fx.tokens.M3ComponentTokens#create(org.glavo.m3fx.tokens.M3ComponentTokens$ButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$IconButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$FabTokens,org.glavo.m3fx.tokens.M3ComponentTokens$IconTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ButtonGroupTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SplitButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ButtonTokens,org.glavo.m3fx.tokens.M3ComponentTokens$TabTokens,org.glavo.m3fx.tokens.M3ComponentTokens$FieldTokens,org.glavo.m3fx.tokens.M3ComponentTokens$TextAreaTokens,org.glavo.m3fx.tokens.M3ComponentTokens$FormTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ValidationSummaryTokens,org.glavo.m3fx.tokens.M3ComponentTokens$MenuTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SearchTokens,org.glavo.m3fx.tokens.M3ComponentTokens$PickerFieldTokens,org.glavo.m3fx.tokens.M3ComponentTokens$DatePickerTokens,org.glavo.m3fx.tokens.M3ComponentTokens$TimePickerTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SheetTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ScrimTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SelectionTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SliderTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ChipTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ProgressTokens,org.glavo.m3fx.tokens.M3ComponentTokens$LoadingIndicatorTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SurfaceTokens,org.glavo.m3fx.tokens.M3ComponentTokens$CarouselTokens,org.glavo.m3fx.tokens.M3ComponentTokens$CardTokens,org.glavo.m3fx.tokens.M3ComponentTokens$DialogTokens,org.glavo.m3fx.tokens.M3ComponentTokens$SnackbarTokens,org.glavo.m3fx.tokens.M3ComponentTokens$BannerTokens,org.glavo.m3fx.tokens.M3ComponentTokens$TooltipTokens,org.glavo.m3fx.tokens.M3ComponentTokens$DividerTokens,org.glavo.m3fx.tokens.M3ComponentTokens$BadgeTokens,org.glavo.m3fx.tokens.M3ComponentTokens$AvatarTokens,org.glavo.m3fx.tokens.M3ComponentTokens$TopAppBarTokens,org.glavo.m3fx.tokens.M3ComponentTokens$BottomAppBarTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ToolbarTokens,org.glavo.m3fx.tokens.M3ComponentTokens$NavigationBarTokens,org.glavo.m3fx.tokens.M3ComponentTokens$NavigationRailTokens,org.glavo.m3fx.tokens.M3ComponentTokens$NavigationDrawerTokens,org.glavo.m3fx.tokens.M3ComponentTokens$ListItemTokens)
                    org.glavo.m3fx.tokens.M3ComponentTokens#create(org.glavo.m3fx.tokens.M3Profile,org.glavo.m3fx.tokens.M3ShapeTokens,org.glavo.m3fx.tokens.M3Density)
                    org.glavo.m3fx.tokens.M3Density#of(double)
                    org.glavo.m3fx.tokens.M3Density#standard()
                    org.glavo.m3fx.tokens.M3ElevationTokens#baseline()
                    org.glavo.m3fx.tokens.M3ElevationTokens#create(double,double,double,double,double,double)
                    org.glavo.m3fx.tokens.M3MotionTokens#baseline()
                    org.glavo.m3fx.tokens.M3MotionTokens#create(int,int,int)
                    org.glavo.m3fx.tokens.M3MotionTokens#create(int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int)
                    org.glavo.m3fx.tokens.M3MotionTokens#create(int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,org.glavo.m3fx.animation.M3MotionScheme)
                    org.glavo.m3fx.tokens.M3MotionTokens#create(int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,org.glavo.m3fx.animation.M3MotionScheme,org.glavo.m3fx.animation.M3MotionBehavior)
                    org.glavo.m3fx.tokens.M3MotionTokens#expressive()
                    org.glavo.m3fx.tokens.M3ShapeTokens#baseline()
                    org.glavo.m3fx.tokens.M3ShapeTokens#create(double,double,double,double,double,double)
                    org.glavo.m3fx.tokens.M3ShapeTokens#create(double,double,double,double,double,double,double,double,double,double)
                    org.glavo.m3fx.tokens.M3ShapeTokens#expressive()
                    org.glavo.m3fx.tokens.M3StateLayerTokens#baseline()
                    org.glavo.m3fx.tokens.M3StateLayerTokens#create(double,double,double,double,double,double)
                    org.glavo.m3fx.tokens.M3StateLayerTokens#create(double,double,double,double,double,double,double,double,double)
                    org.glavo.m3fx.tokens.M3TextStyle#create(java.lang.String,double,double,int)
                    org.glavo.m3fx.tokens.M3TextStyle#create(java.lang.String,double,double,int,double)
                    org.glavo.m3fx.tokens.M3TokenSet#create(org.glavo.m3fx.tokens.M3Profile,org.glavo.m3fx.tokens.M3ColorTokens,org.glavo.m3fx.tokens.M3TypographyTokens,org.glavo.m3fx.tokens.M3ShapeTokens,org.glavo.m3fx.tokens.M3ElevationTokens,org.glavo.m3fx.tokens.M3MotionTokens,org.glavo.m3fx.tokens.M3StateLayerTokens,org.glavo.m3fx.tokens.M3ComponentTokens)
                    org.glavo.m3fx.tokens.M3TokenSet#create(org.glavo.m3fx.tokens.M3Profile,org.glavo.monetfx.ColorScheme,org.glavo.m3fx.tokens.M3Density)
                    org.glavo.m3fx.tokens.M3TypographyTokens#baseline()
                    org.glavo.m3fx.tokens.M3TypographyTokens#create(org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle,org.glavo.m3fx.tokens.M3TextStyle)
                    org.glavo.m3fx.tokens.M3TypographyTokens#expressive()
                    """)
    );

    /// Public instance methods intentionally exported by public enums in each API package.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>>
            SUPPORTED_PUBLIC_ENUM_INSTANCE_METHODS_BY_PACKAGE = Map.of(
            "org.glavo.m3fx.animation",
            typeNames("""
                    org.glavo.m3fx.animation.M3MotionEasing#interpolator()
                    org.glavo.m3fx.animation.M3MotionEasing#tokenName()
                    """),
            "org.glavo.m3fx.controls",
            Set.of(),
            "org.glavo.m3fx.theme",
            Set.of(),
            "org.glavo.m3fx.tokens",
            typeNames("""
                    org.glavo.m3fx.tokens.M3Profile#getColorSpecVersion()
                    org.glavo.m3fx.tokens.M3Profile#getColorStyle()
                    """)
    );

    /// Picker wrapper methods that duplicate the embedded picker API instead of the wrapper value API.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>> FORBIDDEN_PICKER_WRAPPER_METHODS = Map.of(
            "org.glavo.m3fx.controls.M3DatePickerDialog",
            Set.of(
                    "applyPreset",
                    "clearValue",
                    "displayedMonthProperty",
                    "firstDayOfWeekProperty",
                    "getDisplayedMonth",
                    "getFirstDayOfWeek",
                    "getMaxDate",
                    "getMinDate",
                    "isDateDisabled",
                    "isShowAdjacentMonthDays",
                    "maxDateProperty",
                    "minDateProperty",
                    "selectDate",
                    "selectToday",
                    "setDisplayedMonth",
                    "setFirstDayOfWeek",
                    "setMaxDate",
                    "setMinDate",
                    "setShowAdjacentMonthDays",
                    "showAdjacentMonthDaysProperty",
                    "showMonth",
                    "showNextMonth",
                    "showPreviousMonth",
                    "showToday"
            ),
            "org.glavo.m3fx.controls.M3DatePickerField",
            Set.of(
                    "applyPreset",
                    "clearValue",
                    "selectDate",
                    "selectToday"
            ),
            "org.glavo.m3fx.controls.M3DateRangePickerDialog",
            Set.of(
                    "applyPreset",
                    "clearRange",
                    "displayedMonthProperty",
                    "endDateProperty",
                    "firstDayOfWeekProperty",
                    "getDisplayedMonth",
                    "getEndDate",
                    "getFirstDayOfWeek",
                    "getMaxDate",
                    "getMinDate",
                    "getRange",
                    "getStartDate",
                    "isDateDisabled",
                    "isDateInSelectedRange",
                    "isRangeComplete",
                    "isShowAdjacentMonthDays",
                    "maxDateProperty",
                    "minDateProperty",
                    "selectDate",
                    "selectToday",
                    "setDisplayedMonth",
                    "setEndDate",
                    "setFirstDayOfWeek",
                    "setMaxDate",
                    "setMinDate",
                    "setRange",
                    "setShowAdjacentMonthDays",
                    "setStartDate",
                    "showAdjacentMonthDaysProperty",
                    "showMonth",
                    "showNextMonth",
                    "showPreviousMonth",
                    "showToday",
                    "startDateProperty"
            ),
            "org.glavo.m3fx.controls.M3DateRangePickerField",
            Set.of(
                    "applyPreset"
            ),
            "org.glavo.m3fx.controls.M3TimePickerDialog",
            Set.of(
                    "applyPreset",
                    "clearValue",
                    "getMaxTime",
                    "getMinTime",
                    "getMinuteStep",
                    "isTimeDisabled",
                    "isUse24HourClock",
                    "maxTimeProperty",
                    "minTimeProperty",
                    "minuteStepProperty",
                    "selectNow",
                    "setMaxTime",
                    "setMinTime",
                    "setMinuteStep",
                    "setTime",
                    "setUse24HourClock",
                    "use24HourClockProperty"
            ),
            "org.glavo.m3fx.controls.M3TimePickerField",
            Set.of(
                    "applyPreset",
                    "clearValue",
                    "setTime",
                    "selectNow"
            )
    );

    /// Menu host methods that duplicate the embedded menu selection API instead of the popup host API.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>> FORBIDDEN_MENU_HOST_WRAPPER_METHODS = Map.of(
            "org.glavo.m3fx.controls.M3MenuButton",
            Set.of(
                    "allowEmptySelectionProperty",
                    "clearSelection",
                    "getSelectedIndex",
                    "getSelectedItem",
                    "getSelectedItems",
                    "getSelectionMode",
                    "isAllowEmptySelection",
                    "select",
                    "selectFirst",
                    "selectIndex",
                    "selectLast",
                    "selectNext",
                    "selectPrevious",
                    "selectedItemProperty",
                    "selectionModeProperty",
                    "setAllowEmptySelection",
                    "setSelectionMode"
            ),
            "org.glavo.m3fx.controls.M3SplitButton",
            Set.of(
                    "allowEmptySelectionProperty",
                    "clearSelection",
                    "getSelectedIndex",
                    "getSelectedItem",
                    "getSelectedItems",
                    "getSelectionMode",
                    "isAllowEmptySelection",
                    "select",
                    "selectIndex",
                    "selectedItemProperty",
                    "selectionModeProperty",
                    "setAllowEmptySelection",
                    "setSelectionMode"
            )
    );

    /// Public batch constructors intentionally kept as stable convenience API.
    private static final @Unmodifiable Set<String> EXPECTED_PUBLIC_CONTROL_BATCH_CONSTRUCTORS = Set.of(
            "org.glavo.m3fx.controls.M3Dialog(String,String,String,ButtonType[])",
            "org.glavo.m3fx.controls.M3FormValidator(M3TextInputLayout[])",
            "org.glavo.m3fx.controls.M3Menu(Node[])",
            "org.glavo.m3fx.controls.M3MenuButton(String,Node[])",
            "org.glavo.m3fx.controls.M3SubMenuItem(String,Node[])"
    );

    /// Composite controls whose public API keeps action population on mutable list properties.
    private static final @Unmodifiable Set<String> COMPOSITE_CONTROLS_WITHOUT_PUBLIC_BATCH_CONSTRUCTORS = Set.of(
            "org.glavo.m3fx.controls.M3Banner",
            "org.glavo.m3fx.controls.M3BottomAppBar",
            "org.glavo.m3fx.controls.M3BottomSheet",
            "org.glavo.m3fx.controls.M3FormSection",
            "org.glavo.m3fx.controls.M3RichTooltip",
            "org.glavo.m3fx.controls.M3SearchView",
            "org.glavo.m3fx.controls.M3SideSheet",
            "org.glavo.m3fx.controls.M3SplitButton",
            "org.glavo.m3fx.controls.M3TopAppBar"
    );

    /// Pure mutable-list controls that expose population through their mutable list properties.
    private static final @Unmodifiable Set<String> PURE_MUTABLE_LIST_CONTROLS_WITHOUT_BATCH_CONSTRUCTORS = Set.of(
            "org.glavo.m3fx.controls.M3ButtonGroup",
            "org.glavo.m3fx.controls.M3Carousel",
            "org.glavo.m3fx.controls.M3ChipGroup",
            "org.glavo.m3fx.controls.M3FormPane",
            "org.glavo.m3fx.controls.M3IconToggleButtonGroup",
            "org.glavo.m3fx.controls.M3ListPane",
            "org.glavo.m3fx.controls.M3ListView",
            "org.glavo.m3fx.controls.M3NavigationBar",
            "org.glavo.m3fx.controls.M3NavigationDrawer",
            "org.glavo.m3fx.controls.M3NavigationRail",
            "org.glavo.m3fx.controls.M3SegmentedButtonGroup",
            "org.glavo.m3fx.controls.M3Surface",
            "org.glavo.m3fx.controls.M3TabBar",
            "org.glavo.m3fx.controls.M3Toolbar"
    );

    /// Internal node accessors that must not be part of the public control API.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>> FORBIDDEN_INTERNAL_NODE_ACCESSORS = Map.of(
            "org.glavo.m3fx.controls.M3FabMenu",
            Set.of("getActionsContainer", "getToggleButton"),
            "org.glavo.m3fx.controls.M3PickerField",
            Set.of("getEditor", "getInputLayout"),
            "org.glavo.m3fx.controls.M3SearchBar",
            Set.of("getEditor"),
            "org.glavo.m3fx.controls.M3SearchView",
            Set.of("getEditor", "getResultsContainer", "getSearchBar"),
            "org.glavo.m3fx.controls.M3SplitButton",
            Set.of("getActionButton", "getMenuButton"),
            "org.glavo.m3fx.controls.M3DateRangePickerField",
            Set.of("getEndEditor", "getEndInputLayout", "getStartEditor", "getStartInputLayout"),
            "org.glavo.m3fx.controls.M3TextInputLayout",
            Set.of("getClearButton", "getInputContainer", "getSupportingRow")
    );

    /// Public getters that intentionally expose user-supplied slots, selections, or embedded configuration controls.
    private static final @Unmodifiable Set<String> EXPECTED_PUBLIC_NODE_GETTERS = typeNames("""
            org.glavo.m3fx.controls.M3Avatar#getGraphic()
            org.glavo.m3fx.controls.M3BadgedBox#getBadge()
            org.glavo.m3fx.controls.M3BadgedBox#getContent()
            org.glavo.m3fx.controls.M3Banner#getActions()
            org.glavo.m3fx.controls.M3Banner#getIcon()
            org.glavo.m3fx.controls.M3BottomAppBar#getActions()
            org.glavo.m3fx.controls.M3BottomAppBar#getFloatingAction()
            org.glavo.m3fx.controls.M3BottomSheet#getActions()
            org.glavo.m3fx.controls.M3BottomSheet#getContent()
            org.glavo.m3fx.controls.M3ButtonGroup#getItems()
            org.glavo.m3fx.controls.M3Card#getContent()
            org.glavo.m3fx.controls.M3Carousel#getItems()
            org.glavo.m3fx.controls.M3Carousel#getSelectedItem()
            org.glavo.m3fx.controls.M3Carousel#getSelectedItems()
            org.glavo.m3fx.controls.M3ChipGroup#getItems()
            org.glavo.m3fx.controls.M3ChipGroup#getSelectedChip()
            org.glavo.m3fx.controls.M3ChipGroup#getSelectedChips()
            org.glavo.m3fx.controls.M3DatePickerDialog#getPicker()
            org.glavo.m3fx.controls.M3DateRangePickerDialog#getPicker()
            org.glavo.m3fx.controls.M3DateRangePickerField#getPicker()
            org.glavo.m3fx.controls.M3Dialog#getM3DialogPane()
            org.glavo.m3fx.controls.M3FabMenu#getItems()
            org.glavo.m3fx.controls.M3FormPane#getItems()
            org.glavo.m3fx.controls.M3FormRow#getContent()
            org.glavo.m3fx.controls.M3FormRow#getTrailing()
            org.glavo.m3fx.controls.M3FormSection#getContent()
            org.glavo.m3fx.controls.M3FormValidator#getFirstInvalidInput()
            org.glavo.m3fx.controls.M3FormValidator#getInputs()
            org.glavo.m3fx.controls.M3FormValidator#getInvalidInputs()
            org.glavo.m3fx.controls.M3IconToggleButtonGroup#getItems()
            org.glavo.m3fx.controls.M3IconToggleButtonGroup#getSelectedButton()
            org.glavo.m3fx.controls.M3IconToggleButtonGroup#getSelectedButtons()
            org.glavo.m3fx.controls.M3ListItem#getLeading()
            org.glavo.m3fx.controls.M3ListItem#getTrailing()
            org.glavo.m3fx.controls.M3ListPane#getItems()
            org.glavo.m3fx.controls.M3ListPane#getSelectedItem()
            org.glavo.m3fx.controls.M3ListPane#getSelectedItems()
            org.glavo.m3fx.controls.M3Menu#getItems()
            org.glavo.m3fx.controls.M3Menu#getSelectedItem()
            org.glavo.m3fx.controls.M3Menu#getSelectedItems()
            org.glavo.m3fx.controls.M3MenuButton#getItems()
            org.glavo.m3fx.controls.M3MenuButton#getMenu()
            org.glavo.m3fx.controls.M3NavigationBar#getItems()
            org.glavo.m3fx.controls.M3NavigationBar#getSelectedItem()
            org.glavo.m3fx.controls.M3NavigationBar#getSelectedItems()
            org.glavo.m3fx.controls.M3NavigationDrawer#getItems()
            org.glavo.m3fx.controls.M3NavigationDrawer#getSelectedItem()
            org.glavo.m3fx.controls.M3NavigationDrawer#getSelectedItems()
            org.glavo.m3fx.controls.M3NavigationDrawerGroup#getHeaderItem()
            org.glavo.m3fx.controls.M3NavigationDrawerGroup#getItems()
            org.glavo.m3fx.controls.M3NavigationItem#getBadge()
            org.glavo.m3fx.controls.M3NavigationRail#getItems()
            org.glavo.m3fx.controls.M3NavigationRail#getSelectedItem()
            org.glavo.m3fx.controls.M3NavigationRail#getSelectedItems()
            org.glavo.m3fx.controls.M3PickerField#getPicker()
            org.glavo.m3fx.controls.M3RichTooltip#getActions()
            org.glavo.m3fx.controls.M3SearchBar#getLeading()
            org.glavo.m3fx.controls.M3SearchBar#getTrailingActions()
            org.glavo.m3fx.controls.M3SearchView#getLeading()
            org.glavo.m3fx.controls.M3SearchView#getResults()
            org.glavo.m3fx.controls.M3SearchView#getTrailingActions()
            org.glavo.m3fx.controls.M3SegmentedButtonGroup#getItems()
            org.glavo.m3fx.controls.M3SegmentedButtonGroup#getSelectedButton()
            org.glavo.m3fx.controls.M3SegmentedButtonGroup#getSelectedButtons()
            org.glavo.m3fx.controls.M3SideSheet#getActions()
            org.glavo.m3fx.controls.M3SideSheet#getContent()
            org.glavo.m3fx.controls.M3SnackbarHost#getQueue()
            org.glavo.m3fx.controls.M3SnackbarHost#getSnackbar()
            org.glavo.m3fx.controls.M3SplitButton#getGraphic()
            org.glavo.m3fx.controls.M3SplitButton#getItems()
            org.glavo.m3fx.controls.M3SplitButton#getMenu()
            org.glavo.m3fx.controls.M3SubMenuItem#getItems()
            org.glavo.m3fx.controls.M3SubMenuItem#getSubMenu()
            org.glavo.m3fx.controls.M3Surface#getContent()
            org.glavo.m3fx.controls.M3TabBar#getSelectedTab()
            org.glavo.m3fx.controls.M3TabBar#getSelectedTabs()
            org.glavo.m3fx.controls.M3TabBar#getTabs()
            org.glavo.m3fx.controls.M3TextInputLayout#getInput()
            org.glavo.m3fx.controls.M3TextInputLayout#getLeading()
            org.glavo.m3fx.controls.M3TextInputLayout#getTrailing()
            org.glavo.m3fx.controls.M3TimePickerDialog#getPicker()
            org.glavo.m3fx.controls.M3Toolbar#getItems()
            org.glavo.m3fx.controls.M3Tooltip#getGraphic()
            org.glavo.m3fx.controls.M3TopAppBar#getActions()
            org.glavo.m3fx.controls.M3TopAppBar#getNavigation()
            """);

    /// Public properties that intentionally expose user-supplied slots, selections, or embedded configuration controls.
    private static final @Unmodifiable Set<String> EXPECTED_PUBLIC_NODE_PROPERTIES = typeNames("""
            org.glavo.m3fx.controls.M3Avatar#graphicProperty()
            org.glavo.m3fx.controls.M3BadgedBox#badgeProperty()
            org.glavo.m3fx.controls.M3BadgedBox#contentProperty()
            org.glavo.m3fx.controls.M3Banner#iconProperty()
            org.glavo.m3fx.controls.M3BottomAppBar#floatingActionProperty()
            org.glavo.m3fx.controls.M3BottomSheet#contentProperty()
            org.glavo.m3fx.controls.M3Card#contentProperty()
            org.glavo.m3fx.controls.M3Carousel#selectedItemProperty()
            org.glavo.m3fx.controls.M3ChipGroup#selectedChipProperty()
            org.glavo.m3fx.controls.M3FormRow#contentProperty()
            org.glavo.m3fx.controls.M3FormRow#trailingProperty()
            org.glavo.m3fx.controls.M3FormValidator#firstInvalidInputProperty()
            org.glavo.m3fx.controls.M3IconToggleButtonGroup#selectedButtonProperty()
            org.glavo.m3fx.controls.M3ListItem#leadingProperty()
            org.glavo.m3fx.controls.M3ListItem#trailingProperty()
            org.glavo.m3fx.controls.M3ListPane#selectedItemProperty()
            org.glavo.m3fx.controls.M3Menu#selectedItemProperty()
            org.glavo.m3fx.controls.M3NavigationBar#selectedItemProperty()
            org.glavo.m3fx.controls.M3NavigationDrawer#selectedItemProperty()
            org.glavo.m3fx.controls.M3NavigationItem#badgeProperty()
            org.glavo.m3fx.controls.M3NavigationRail#selectedItemProperty()
            org.glavo.m3fx.controls.M3SearchBar#leadingProperty()
            org.glavo.m3fx.controls.M3SearchView#leadingProperty()
            org.glavo.m3fx.controls.M3SegmentedButtonGroup#selectedButtonProperty()
            org.glavo.m3fx.controls.M3SideSheet#contentProperty()
            org.glavo.m3fx.controls.M3SnackbarHost#snackbarProperty()
            org.glavo.m3fx.controls.M3SplitButton#graphicProperty()
            org.glavo.m3fx.controls.M3TabBar#selectedTabProperty()
            org.glavo.m3fx.controls.M3TextInputLayout#inputProperty()
            org.glavo.m3fx.controls.M3TextInputLayout#leadingProperty()
            org.glavo.m3fx.controls.M3TextInputLayout#trailingProperty()
            org.glavo.m3fx.controls.M3Tooltip#graphicProperty()
            org.glavo.m3fx.controls.M3TopAppBar#navigationProperty()
            """);

    /// Matches public static class CSS metadata entry points.
    private static final Pattern CLASS_CSS_METADATA_METHOD = Pattern.compile(
            "public\\s+static\\s+(?:@\\w+(?:\\([^)]*\\))?\\s+)*List\\s*<\\s*CssMetaData\\s*<\\s*"
                    + "\\?\\s+extends\\s+Styleable\\s*,\\s*\\?\\s*>\\s*>\\s+getClassCssMetaData\\s*\\("
    );

    /// Matches public instance CSS metadata entry points that delegate to class metadata.
    private static final Pattern INSTANCE_CSS_METADATA_METHOD = Pattern.compile(
            "public\\s+(?:@\\w+(?:\\([^)]*\\))?\\s+)*List\\s*<\\s*CssMetaData\\s*<\\s*"
                    + "\\?\\s+extends\\s+Styleable\\s*,\\s*\\?\\s*>\\s*>\\s+"
                    + "(?:getControlCssMetaData|getCssMetaData)\\s*\\([^)]*\\)\\s*\\{\\s*"
                    + "return\\s+getClassCssMetaData\\s*\\(\\s*\\)\\s*;\\s*}"
            , Pattern.DOTALL
    );

    /// JavaFX base classes that are controls for source-level inheritance checks.
    private static final @Unmodifiable Set<String> JAVA_FX_CONTROL_BASE_CLASSES = Set.of(
            "Control",
            "ButtonBase",
            "Labeled",
            "TextInputControl",
            "TextField",
            "PasswordField",
            "TextArea",
            "IndexedCell"
    );

    /// JavaFX control bases whose roots support `-fx-alignment` directly.
    private static final @Unmodifiable Set<String> JAVA_FX_ALIGNED_CONTROL_BASE_CLASSES = Set.of(
            "ButtonBase",
            "Labeled",
            "TextField",
            "PasswordField",
            "IndexedCell"
    );

    /// Concrete JavaFX controls that M3FX controls must not inherit from directly.
    private static final @Unmodifiable List<String> FORBIDDEN_CONCRETE_CONTROL_SUPERCLASSES = List.of(
            "Button",
            "CheckBox",
            "ComboBoxBase",
            "ChoiceBox",
            "ListView",
            "MenuButton",
            "PasswordField",
            "ProgressBar",
            "ProgressIndicator",
            "RadioButton",
            "Slider",
            "SplitMenuButton",
            "TextArea",
            "TextField",
            "ToggleButton",
            "Tooltip"
    );

    /// Public controls that intentionally keep a concrete JavaFX text editing implementation.
    private static final @Unmodifiable Map<String, String> ALLOWED_CONCRETE_CONTROL_SUPERCLASSES = Map.of(
            "M3PasswordField", "PasswordField",
            "M3TextArea", "TextArea",
            "M3TextField", "TextField"
    );

    /// Direct concrete JavaFX superclass exceptions intentionally kept in the exported controls package.
    private static final @Unmodifiable Map<String, String> ALLOWED_EXPORTED_CONTROL_DIRECT_JAVA_FX_SUPERCLASSES =
            Map.of(
                    "org.glavo.m3fx.controls.M3Dialog", "javafx.scene.control.Dialog",
                    "org.glavo.m3fx.controls.M3DialogPane", "javafx.scene.control.DialogPane",
                    "org.glavo.m3fx.controls.M3PasswordField", "javafx.scene.control.PasswordField",
                    "org.glavo.m3fx.controls.M3TextArea", "javafx.scene.control.TextArea",
                    "org.glavo.m3fx.controls.M3TextField", "javafx.scene.control.TextField",
                    "org.glavo.m3fx.controls.M3Tooltip", "javafx.scene.control.PopupControl"
            );

    /// Concrete JavaFX skins that M3FX skins must not inherit from directly.
    private static final @Unmodifiable List<String> FORBIDDEN_CONCRETE_SKIN_SUPERCLASSES = List.of(
            "ButtonSkin",
            "CheckBoxSkin",
            "ChoiceBoxSkin",
            "ComboBoxBaseSkin",
            "LabeledSkin",
            "ListViewSkin",
            "MenuButtonSkin",
            "ProgressBarSkin",
            "ProgressIndicatorSkin",
            "RadioButtonSkin",
            "SliderSkin",
            "SplitMenuButtonSkin",
            "TextAreaSkin",
            "TextFieldSkin",
            "TextInputControlSkin",
            "ToggleButtonSkin"
    );

    /// Verifies that JavaFX APIs newer than the runtime compatibility baseline stay behind guarded access.
    @Test
    void newerJavaFxApisStayBehindRuntimeGuards() throws IOException {
        List<String> directCalls = new ArrayList<>();
        for (Path sourceFile : productionJavaSourceFiles()) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                for (String fragment : NEWER_JAVA_FX_API_DIRECT_CALL_FRAGMENTS) {
                    if (line.contains(fragment)) {
                        directCalls.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                    }
                }
            }
        }

        assertTrue(directCalls.isEmpty(),
                () -> "JavaFX APIs newer than the compatibility baseline must use guarded runtime access: "
                        + directCalls);
    }

    /// Verifies that production type sources keep explicit default nullability.
    @Test
    void productionTypesDeclareDefaultNullability() throws IOException {
        List<String> missingAnnotations = new ArrayList<>();
        for (Path sourceFile : productionJavaSourceFiles()) {
            String fileName = sourceFile.getFileName().toString();
            if (fileName.equals("module-info.java") || fileName.equals("package-info.java")) {
                continue;
            }

            String source = Files.readString(sourceFile);
            if (!source.contains("@NotNullByDefault") && TYPE_DECLARATION.matcher(source).find()) {
                missingAnnotations.add(sourceFile.toString());
            }
        }

        assertTrue(missingAnnotations.isEmpty(),
                () -> "Production type sources must declare @NotNullByDefault: " + missingAnnotations);
    }

    /// Verifies that the module export surface matches the public package contract.
    @Test
    void moduleExportsOnlyExpectedPublicApiPackages() throws IOException {
        assertEquals(EXPECTED_EXPORTED_API_PACKAGES, exportedPackageNames(),
                "The public JPMS export surface must match the expected API packages");
    }

    /// Verifies that every exported public top-level type is part of the expected API surface.
    @Test
    void exportedPackagesExposeOnlyExpectedTopLevelTypes() throws IOException {
        assertEquals(EXPECTED_EXPORTED_TOP_LEVEL_TYPES, exportedPublicTopLevelTypeNamesByPackage(),
                "Exported public top-level API types must match the expected API surface");
    }

    /// Verifies that exported controls do not publish implementation helper node types.
    @Test
    void exportedControlsDoNotPublishImplementationTypes() throws IOException {
        List<String> implementationTypes = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                Matcher matcher = PUBLIC_TOP_LEVEL_TYPE_DECLARATION.matcher(lines.get(lineIndex));
                if (matcher.find()) {
                    String typeName = matcher.group(1);
                    if (isForbiddenPublicControlImplementationType(typeName)) {
                        implementationTypes.add(sourceFile + ":" + (lineIndex + 1) + ": " + typeName);
                    }
                }
            }
        }

        assertTrue(implementationTypes.isEmpty(),
                () -> "Exported controls must not publish implementation helper node types: "
                        + implementationTypes);
    }

    /// Verifies that exported public and protected APIs do not expose internal implementation member types.
    @Test
    void exportedApisDoNotExposeInternalMemberTypes() throws IOException {
        List<String> internalApiLeaks = new ArrayList<>();
        for (Path sourceRoot : exportedPackageSourceRoots()) {
            for (Path sourceFile : javaSourceFiles(sourceRoot)) {
                collectInternalApiLeaks(sourceFile, internalApiLeaks);
            }
        }

        assertTrue(internalApiLeaks.isEmpty(),
                () -> "Exported public/protected member APIs must not expose internal implementation types: "
                        + internalApiLeaks);
    }

    /// Verifies that compiled exported APIs do not expose unexported M3FX package types.
    @Test
    void exportedBytecodeApisDoNotExposeUnexportedM3fxTypes() throws Exception {
        Set<String> exportedPackages = exportedPackageNames();
        List<String> leakedTypes = new ArrayList<>();
        for (String className : exportedTopLevelClassNames(exportedPackages)) {
            Class<?> type = Class.forName(
                    className,
                    false,
                    ProjectContractTest.class.getClassLoader()
            );
            if (Modifier.isPublic(type.getModifiers())) {
                collectUnexportedM3fxTypeLeaks(type, exportedPackages, leakedTypes);
            }
        }

        assertTrue(leakedTypes.isEmpty(),
                () -> "Exported APIs must not expose types from unexported M3FX packages: " + leakedTypes);
    }

    /// Verifies that exported APIs do not expose internal JavaFX properties-map storage keys.
    @Test
    void exportedApisDoNotExposePropertiesMapStorageKeys() throws Exception {
        List<String> exposedKeys = new ArrayList<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            if (!Modifier.isPublic(type.getModifiers())) {
                continue;
            }

            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && field.getName().endsWith("_KEY")) {
                    exposedKeys.add(type.getName() + '#' + field.getName());
                }
            }
        }

        assertTrue(exposedKeys.isEmpty(),
                () -> "Exported APIs must not expose JavaFX properties-map storage keys: " + exposedKeys);
    }

    /// Verifies that exported APIs expose fields only as immutable constants.
    @Test
    void exportedApisExposeOnlyConstantFields() throws Exception {
        List<String> fieldViolations = new ArrayList<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            if (Modifier.isPublic(type.getModifiers())) {
                collectExportedFieldMutabilityViolations(type, fieldViolations);
            }
        }

        assertTrue(fieldViolations.isEmpty(),
                () -> "Exported APIs must expose fields only as immutable constants: " + fieldViolations);
    }

    /// Verifies that public controls avoid inheriting from concrete JavaFX controls.
    @Test
    void publicControlsDoNotExtendConcreteJavaFxControls() throws IOException {
        List<String> forbiddenInheritances = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                var matcher = PUBLIC_CONTROL_EXTENDS_DECLARATION.matcher(line);
                if (matcher.find()) {
                    String typeName = matcher.group(1);
                    String superclassName = matcher.group(2);
                    if (FORBIDDEN_CONCRETE_CONTROL_SUPERCLASSES.contains(superclassName)
                            && !isAllowedConcreteControlInheritance(typeName, superclassName)) {
                        forbiddenInheritances.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                    }
                }
            }
        }

        assertTrue(forbiddenInheritances.isEmpty(),
                () -> "M3FX controls must not extend concrete JavaFX controls: " + forbiddenInheritances);
    }

    /// Verifies that concrete JavaFX superclass exceptions in exported controls match the allowed set.
    @Test
    void exportedControlsKeepConcreteJavaFxSuperclassExceptionsExplicit() throws Exception {
        Map<String, String> concreteJavaFxSuperclasses = new HashMap<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            if (!type.getPackageName().equals("org.glavo.m3fx.controls")
                    || !Modifier.isPublic(type.getModifiers())) {
                continue;
            }

            Class<?> superclass = type.getSuperclass();
            if (superclass != null
                    && superclass.getPackageName().equals("javafx.scene.control")
                    && !Modifier.isAbstract(superclass.getModifiers())) {
                concreteJavaFxSuperclasses.put(type.getName(), superclass.getName());
            }
        }

        assertEquals(ALLOWED_EXPORTED_CONTROL_DIRECT_JAVA_FX_SUPERCLASSES, concreteJavaFxSuperclasses,
                "Concrete JavaFX superclass exceptions in exported controls must match the allowed set");
    }

    /// Verifies that concrete public controls expose constructors and properties instead of static factories.
    @Test
    void concretePublicControlsDoNotExposeStaticConvenienceMethods() throws Exception {
        List<String> staticMethods = new ArrayList<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            int typeModifiers = type.getModifiers();
            if (!type.getPackageName().equals("org.glavo.m3fx.controls")
                    || !Modifier.isPublic(typeModifiers)
                    || Modifier.isAbstract(typeModifiers)
                    || !Control.class.isAssignableFrom(type)) {
                continue;
            }

            for (Method method : type.getDeclaredMethods()) {
                int methodModifiers = method.getModifiers();
                if (Modifier.isPublic(methodModifiers)
                        && Modifier.isStatic(methodModifiers)
                        && !method.getName().equals("getClassCssMetaData")) {
                    staticMethods.add(type.getName() + "#" + method.getName());
                }
            }
        }

        assertTrue(staticMethods.isEmpty(),
                () -> "Concrete public controls must not expose static convenience methods: " + staticMethods);
    }

    /// Verifies that public component types do not expose static factories returning component instances.
    @Test
    void publicControlPackageTypesDoNotExposeStaticSelfFactories() throws Exception {
        List<String> staticFactories = new ArrayList<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            int typeModifiers = type.getModifiers();
            if (!type.getPackageName().equals("org.glavo.m3fx.controls")
                    || !Modifier.isPublic(typeModifiers)
                    || Modifier.isAbstract(typeModifiers)
                    || type.isEnum()) {
                continue;
            }

            for (Method method : type.getDeclaredMethods()) {
                int methodModifiers = method.getModifiers();
                if (Modifier.isPublic(methodModifiers)
                        && Modifier.isStatic(methodModifiers)
                        && type.isAssignableFrom(method.getReturnType())) {
                    staticFactories.add(type.getName() + "#" + method.getName());
                }
            }
        }

        assertTrue(staticFactories.isEmpty(),
                () -> "Public component types must use constructors instead of static self factories: "
                        + staticFactories);
    }

    /// Verifies that exported `controls` package static utility methods match the supported API surface.
    @Test
    void publicControlPackageStaticUtilityMethodsStayStable() throws Exception {
        Set<String> staticMethods = new TreeSet<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            int typeModifiers = type.getModifiers();
            if (!type.getPackageName().equals("org.glavo.m3fx.controls")
                    || !Modifier.isPublic(typeModifiers)
                    || type.isEnum()) {
                continue;
            }

            for (Method method : type.getDeclaredMethods()) {
                int methodModifiers = method.getModifiers();
                if (Modifier.isPublic(methodModifiers)
                        && Modifier.isStatic(methodModifiers)
                        && !method.isSynthetic()
                        && !method.getName().equals("getClassCssMetaData")) {
                    staticMethods.add(publicMethodSignature(type, method));
                }
            }
        }

        assertEquals(SUPPORTED_PUBLIC_CONTROL_PACKAGE_STATIC_METHODS, staticMethods,
                "Public controls package static utility methods must match the supported API surface");
    }

    /// Verifies that public control utility types stay stateless and constructor-free.
    @Test
    void publicControlUtilityTypesStayStateless() throws Exception {
        List<String> violations = new ArrayList<>();
        for (String className : PUBLIC_CONTROL_UTILITY_TYPES) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            if (!Modifier.isFinal(type.getModifiers())) {
                violations.add(className + " must be final");
            }

            boolean hasPrivateNoArgConstructor = false;
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                int modifiers = constructor.getModifiers();
                if (constructor.getParameterCount() == 0 && Modifier.isPrivate(modifiers)) {
                    hasPrivateNoArgConstructor = true;
                }
                if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
                    violations.add(className + " exposes " + constructor.toGenericString());
                }
                if (constructor.getParameterCount() > 0) {
                    violations.add(className + " declares parameterized constructor " + constructor.toGenericString());
                }
            }
            if (!hasPrivateNoArgConstructor) {
                violations.add(className + " must declare one private no-arg constructor");
            }

            for (Field field : type.getDeclaredFields()) {
                if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                    violations.add(className + " declares instance field " + field.getName());
                }
            }

            for (Method method : type.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !method.isSynthetic()) {
                    violations.add(className + " exposes instance method " + method.getName());
                }
            }
        }

        assertTrue(violations.isEmpty(),
                () -> "Public control utility types must remain stateless static APIs: " + violations);
    }

    /// Verifies that static utility methods exported outside `controls` match the supported API surface.
    @Test
    void publicNonControlPackageStaticUtilityMethodsStayStable() throws Exception {
        Map<String, Set<String>> staticMethodsByPackage = new TreeMap<>();
        for (String packageName : EXPECTED_EXPORTED_API_PACKAGES) {
            if (packageName.equals("org.glavo.m3fx.controls")) {
                continue;
            }

            Set<String> staticMethods = new TreeSet<>();
            for (String typeName : EXPECTED_EXPORTED_TOP_LEVEL_TYPES.get(packageName)) {
                Class<?> type = Class.forName(packageName + '.' + typeName, false,
                        ProjectContractTest.class.getClassLoader());
                if (type.isEnum()) {
                    continue;
                }

                for (Method method : type.getDeclaredMethods()) {
                    int methodModifiers = method.getModifiers();
                    if (Modifier.isPublic(methodModifiers)
                            && Modifier.isStatic(methodModifiers)
                            && !method.isSynthetic()) {
                        staticMethods.add(publicMethodSignature(type, method));
                    }
                }
            }
            staticMethodsByPackage.put(packageName, Set.copyOf(staticMethods));
        }

        assertEquals(SUPPORTED_PUBLIC_STATIC_METHODS_BY_NON_CONTROL_PACKAGE, staticMethodsByPackage,
                "Public non-control package static utility methods must match the supported API surface");
    }

    /// Verifies that public enum instance methods match the reviewed API surface.
    @Test
    void publicEnumInstanceMethodsStayReviewed() throws Exception {
        Map<String, Set<String>> methodsByPackage = new TreeMap<>();
        for (String packageName : EXPECTED_EXPORTED_API_PACKAGES) {
            Set<String> enumMethods = new TreeSet<>();
            for (String typeName : EXPECTED_EXPORTED_TOP_LEVEL_TYPES.get(packageName)) {
                Class<?> type = Class.forName(packageName + '.' + typeName, false,
                        ProjectContractTest.class.getClassLoader());
                if (!type.isEnum()) {
                    continue;
                }

                for (Method method : type.getDeclaredMethods()) {
                    int modifiers = method.getModifiers();
                    if (Modifier.isPublic(modifiers)
                            && !Modifier.isStatic(modifiers)
                            && !method.isSynthetic()
                            && !method.isBridge()) {
                        enumMethods.add(publicMethodSignature(type, method));
                    }
                }
            }
            methodsByPackage.put(packageName, Set.copyOf(enumMethods));
        }

        assertEquals(SUPPORTED_PUBLIC_ENUM_INSTANCE_METHODS_BY_PACKAGE, methodsByPackage,
                "Public enum instance methods must match the reviewed API surface");
    }

    /// Verifies that preset records remain immutable data carriers instead of publishing convenience actions.
    @Test
    void publicPresetRecordsExposeOnlyRecordAccessors() throws Exception {
        List<String> actionMethods = new ArrayList<>();
        for (String className : exportedTopLevelClassNames(exportedPackageNames())) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            if (!type.getPackageName().equals("org.glavo.m3fx.controls")
                    || !Modifier.isPublic(type.getModifiers())
                    || !type.isRecord()
                    || !type.getSimpleName().endsWith("Preset")) {
                continue;
            }

            Set<String> componentNames = recordComponentNames(type);
            for (Method method : type.getDeclaredMethods()) {
                if (isRecordInfrastructureMethod(method) || componentNames.contains(method.getName())) {
                    continue;
                }

                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                    actionMethods.add(type.getName() + "#" + method.getName());
                }
            }
        }

        assertTrue(actionMethods.isEmpty(),
                () -> "Public preset records must not expose action-style convenience methods: " + actionMethods);
    }

    /// Verifies that mutable-list controls expose list properties instead of duplicate list mutator methods.
    @Test
    void publicMutableListControlsDoNotExposeCollectionConvenienceMethods() throws Exception {
        List<String> duplicateMethods = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : publicMutableObservableListGetterNamesByClass().entrySet()) {
            Class<?> type = Class.forName(entry.getKey(), false, ProjectContractTest.class.getClassLoader());
            int typeModifiers = type.getModifiers();
            if (!Modifier.isPublic(typeModifiers) || Modifier.isAbstract(typeModifiers)) {
                continue;
            }

            Set<String> forbiddenNames = new TreeSet<>();
            for (String getterName : entry.getValue()) {
                forbiddenNames.addAll(listMutatorsForGetter(getterName));
            }

            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && forbiddenNames.contains(method.getName())) {
                    duplicateMethods.add(type.getName() + '#' + method.getName());
                }
            }
        }

        duplicateMethods.sort(Comparator.naturalOrder());
        assertTrue(duplicateMethods.isEmpty(),
                () -> "Mutable-list controls must use their get*() ObservableList APIs instead of duplicate "
                        + "collection convenience mutators: " + duplicateMethods);
    }

    /// Verifies that the remaining public batch constructors are explicitly intentional.
    @Test
    void publicControlBatchConstructorsStayStable() throws Exception {
        Set<String> batchConstructors = new TreeSet<>();
        for (String typeName : EXPECTED_EXPORTED_TOP_LEVEL_TYPES.get("org.glavo.m3fx.controls")) {
            Class<?> type = Class.forName("org.glavo.m3fx.controls." + typeName, false,
                    ProjectContractTest.class.getClassLoader());
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (!Modifier.isPublic(constructor.getModifiers())) {
                    continue;
                }
                if (hasBatchParameter(constructor.getParameterTypes())) {
                    batchConstructors.add(batchConstructorSignature(constructor));
                }
            }
        }

        assertEquals(EXPECTED_PUBLIC_CONTROL_BATCH_CONSTRUCTORS, batchConstructors,
                "Public control batch constructors must be reviewed before becoming stable API");
    }

    /// Verifies that composite controls keep action population out of their constructor surface.
    @Test
    void compositeControlsDoNotDeclareBatchConstructors() throws Exception {
        List<String> batchConstructors = new ArrayList<>();
        for (String className : COMPOSITE_CONTROLS_WITHOUT_PUBLIC_BATCH_CONSTRUCTORS) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (hasBatchParameter(constructor.getParameterTypes())) {
                    batchConstructors.add(constructor.toGenericString());
                }
            }
        }

        batchConstructors.sort(Comparator.naturalOrder());
        assertTrue(batchConstructors.isEmpty(),
                () -> "Composite controls must use get*() list APIs instead of batch constructors: "
                        + batchConstructors);
    }

    /// Verifies that pure mutable-list controls do not duplicate list population through constructors.
    @Test
    void pureMutableListControlsDoNotExposeBatchConstructors() throws Exception {
        List<String> batchConstructors = new ArrayList<>();
        for (String className : PURE_MUTABLE_LIST_CONTROLS_WITHOUT_BATCH_CONSTRUCTORS) {
            Class<?> type = Class.forName(className, false, ProjectContractTest.class.getClassLoader());
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (!Modifier.isPublic(constructor.getModifiers())) {
                    continue;
                }

                if (hasBatchParameter(constructor.getParameterTypes())) {
                    batchConstructors.add(constructor.toGenericString());
                }
            }
        }

        batchConstructors.sort(Comparator.naturalOrder());
        assertTrue(batchConstructors.isEmpty(),
                () -> "Pure mutable-list controls must use get*() list APIs instead of batch constructors: "
                        + batchConstructors);
    }

    /// Verifies that picker wrapper controls keep picker actions on the embedded picker API.
    @Test
    void publicPickerWrappersDoNotExposeDuplicatePickerActions() throws Exception {
        List<String> duplicateMethods = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : FORBIDDEN_PICKER_WRAPPER_METHODS.entrySet()) {
            Class<?> type = Class.forName(entry.getKey(), false, ProjectContractTest.class.getClassLoader());
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && entry.getValue().contains(method.getName())) {
                    duplicateMethods.add(type.getName() + '#' + method.getName());
                }
            }
        }

        duplicateMethods.sort(Comparator.naturalOrder());
        assertTrue(duplicateMethods.isEmpty(),
                () -> "Picker wrapper controls must expose value/picker access instead of duplicate picker actions: "
                        + duplicateMethods);
    }

    /// Verifies that menu host controls keep selection behavior on the embedded menu API.
    @Test
    void publicMenuHostsDoNotExposeDuplicateMenuSelectionActions() throws Exception {
        List<String> duplicateMethods = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : FORBIDDEN_MENU_HOST_WRAPPER_METHODS.entrySet()) {
            Class<?> type = Class.forName(entry.getKey(), false, ProjectContractTest.class.getClassLoader());
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && entry.getValue().contains(method.getName())) {
                    duplicateMethods.add(type.getName() + '#' + method.getName());
                }
            }
        }

        duplicateMethods.sort(Comparator.naturalOrder());
        assertTrue(duplicateMethods.isEmpty(),
                () -> "Menu host controls must expose popup-host APIs and keep selection on getMenu(): "
                        + duplicateMethods);
    }

    /// Verifies that public getters returning nodes are explicitly reviewed user-facing slots or selections.
    @Test
    void publicNodeGettersStayReviewed() throws Exception {
        Set<String> nodeGetters = new TreeSet<>();
        for (String typeName : EXPECTED_EXPORTED_TOP_LEVEL_TYPES.get("org.glavo.m3fx.controls")) {
            Class<?> type = Class.forName("org.glavo.m3fx.controls." + typeName, false,
                    ProjectContractTest.class.getClassLoader());
            if (!Modifier.isPublic(type.getModifiers())) {
                continue;
            }

            for (Method method : type.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers)
                        && method.getParameterCount() == 0
                        && method.getName().startsWith("get")
                        && !method.isSynthetic()
                        && returnsNodeSurface(method)) {
                    nodeGetters.add(publicMethodSignature(type, method));
                }
            }
        }

        assertEquals(EXPECTED_PUBLIC_NODE_GETTERS, nodeGetters,
                "Public node-returning getters must be reviewed as stable slots, selections, or embedded controls");
    }

    /// Verifies that public properties returning nodes are explicitly reviewed user-facing slots or selections.
    @Test
    void publicNodePropertiesStayReviewed() throws Exception {
        Set<String> nodeProperties = new TreeSet<>();
        for (String typeName : EXPECTED_EXPORTED_TOP_LEVEL_TYPES.get("org.glavo.m3fx.controls")) {
            Class<?> type = Class.forName("org.glavo.m3fx.controls." + typeName, false,
                    ProjectContractTest.class.getClassLoader());
            if (!Modifier.isPublic(type.getModifiers())) {
                continue;
            }

            for (Method method : type.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers)
                        && method.getParameterCount() == 0
                        && method.getName().endsWith("Property")
                        && !method.isSynthetic()
                        && returnsNodeProperty(method)) {
                    nodeProperties.add(publicMethodSignature(type, method));
                }
            }
        }

        assertEquals(EXPECTED_PUBLIC_NODE_PROPERTIES, nodeProperties,
                "Public node-returning properties must be reviewed as stable slots, selections, or embedded controls");
    }

    /// Verifies that controls do not expose implementation nodes as public API shortcuts for skins or tests.
    @Test
    void publicControlsDoNotExposeInternalNodeAccessors() throws Exception {
        List<String> exposedMethods = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : FORBIDDEN_INTERNAL_NODE_ACCESSORS.entrySet()) {
            Class<?> type = Class.forName(entry.getKey(), false, ProjectContractTest.class.getClassLoader());
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && entry.getValue().contains(method.getName())) {
                    exposedMethods.add(type.getName() + '#' + method.getName());
                }
            }
        }

        exposedMethods.sort(Comparator.naturalOrder());
        assertTrue(exposedMethods.isEmpty(),
                () -> "Public controls must keep default-skin implementation nodes out of the API surface: "
                        + exposedMethods);
    }

    /// Verifies that public controls with custom CSS metadata expose class and instance metadata entry points.
    @Test
    void publicStyleableControlsExposeCssMetaDataEntrypoints() throws IOException {
        List<String> missingEntrypoints = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            String source = Files.readString(sourceFile);
            if (lines.stream().noneMatch(line -> PUBLIC_CONTROL_CLASS_DECLARATION.matcher(line).find())
                    || !CUSTOM_CSS_METADATA_PROPERTY.matcher(source).find()) {
                continue;
            }

            if (!CLASS_CSS_METADATA_METHOD.matcher(source).find()) {
                missingEntrypoints.add(sourceFile + ": missing public static getClassCssMetaData()");
            }
            if (!INSTANCE_CSS_METADATA_METHOD.matcher(source).find()) {
                missingEntrypoints.add(sourceFile + ": missing instance CSS metadata delegation");
            }
        }

        assertTrue(missingEntrypoints.isEmpty(),
                () -> "Public controls with custom CssMetaData must expose class and instance metadata: "
                        + missingEntrypoints);
    }

    /// Verifies that custom M3FX CSS metadata uses project-owned property names.
    @Test
    void customCssMetaDataPropertiesUseM3Prefix() throws IOException {
        List<String> invalidProperties = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            String source = Files.readString(sourceFile);
            Matcher matcher = CUSTOM_CSS_METADATA_PROPERTY.matcher(source);
            while (matcher.find()) {
                String property = matcher.group(1);
                if (!property.startsWith("-m3-")) {
                    invalidProperties.add(sourceFile + ": " + property);
                }
            }
        }

        assertTrue(invalidProperties.isEmpty(),
                () -> "Custom M3FX CssMetaData properties must use the -m3- prefix: " + invalidProperties);
    }

    /// Verifies that generated control stylesheets do not assign unsupported layout properties to public roots.
    @Test
    void generatedControlStylesDoNotWriteUnsupportedLayoutPropertiesToControlRoots() throws IOException {
        List<String> invalidRules = new ArrayList<>();
        collectUnsupportedControlRootLayoutRules(
                "generated",
                M3Theme.defaultTheme().toControlStyleRules(),
                publicControlRootAlignmentSupport(),
                invalidRules
        );

        assertTrue(invalidRules.isEmpty(),
                () -> "Generated component stylesheets must reserve layout-only CSS properties for internal "
                        + "layout nodes and use styleable -m3-* properties on public Control roots: "
                        + invalidRules);
    }

    /// Verifies that bundled control stylesheets do not assign unsupported layout properties to public roots.
    @Test
    void bundledControlStylesDoNotWriteUnsupportedLayoutPropertiesToControlRoots() throws IOException {
        Map<String, Boolean> controlRootAlignmentSupport = publicControlRootAlignmentSupport();
        List<String> invalidRules = new ArrayList<>();
        for (Path stylesheet : stylesheetFiles(CONTROL_STYLESHEET_RESOURCE_ROOT)) {
            collectUnsupportedControlRootLayoutRules(
                    resourceRelativePath(CONTROL_STYLESHEET_RESOURCE_ROOT, stylesheet),
                    Files.readString(stylesheet),
                    controlRootAlignmentSupport,
                    invalidRules
            );
        }

        assertTrue(invalidRules.isEmpty(),
                () -> "Bundled control stylesheets must reserve layout-only CSS properties for internal "
                        + "layout nodes and use styleable -m3-* properties on public Control roots: "
                        + invalidRules);
    }

    /// Verifies that public skins inherit JavaFX base skin classes or project skin bases.
    @Test
    void publicSkinsDoNotExtendConcreteJavaFxSkins() throws IOException {
        List<String> forbiddenInheritances = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(SKINS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                var matcher = PUBLIC_SKIN_EXTENDS_DECLARATION.matcher(line);
                if (matcher.find() && FORBIDDEN_CONCRETE_SKIN_SUPERCLASSES.contains(matcher.group(2))) {
                    forbiddenInheritances.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(forbiddenInheritances.isEmpty(),
                () -> "M3FX skins must extend JavaFX base skins or project skin bases, not concrete skins: "
                        + forbiddenInheritances);
    }

    /// Verifies that public skins do not bypass `SkinBase` by implementing JavaFX `Skin` directly.
    @Test
    void publicSkinsDoNotImplementSkinDirectly() throws IOException {
        List<String> directImplementations = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(SKINS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (PUBLIC_SKIN_DIRECT_IMPLEMENTATION.matcher(line).find()) {
                    directImplementations.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(directImplementations.isEmpty(),
                () -> "M3FX skins must use SkinBase, LabeledSkinBase, or project skin bases: "
                        + directImplementations);
    }

    /// Verifies that public skins do not expose rendered implementation nodes through getter methods.
    @Test
    void publicSkinsDoNotExposeImplementationNodeGetters() throws Exception {
        List<String> nodeGetters = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(SKINS_SOURCE_ROOT)) {
            @org.jetbrains.annotations.Nullable String typeName = null;
            for (String line : Files.readAllLines(sourceFile)) {
                Matcher matcher = PUBLIC_TOP_LEVEL_TYPE_DECLARATION.matcher(line);
                if (matcher.find()) {
                    typeName = matcher.group(1);
                    break;
                }
            }
            if (typeName == null) {
                continue;
            }

            Class<?> type = Class.forName(packageName(sourceFile) + '.' + typeName, false,
                    ProjectContractTest.class.getClassLoader());
            for (Method method : type.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers)
                        && method.getParameterCount() == 0
                        && method.getName().startsWith("get")
                        && !method.isSynthetic()
                        && returnsNodeSurface(method)) {
                    nodeGetters.add(publicMethodSignature(type, method));
                }
            }
        }

        nodeGetters.sort(Comparator.naturalOrder());
        assertTrue(nodeGetters.isEmpty(),
                () -> "Public skins must keep rendered implementation nodes out of public getters: "
                        + nodeGetters);
    }

    /// Verifies that every Java `controlStylesheet` reference resolves to a bundled CSS resource.
    @Test
    void controlStylesheetReferencesResolveToBundledResources() throws IOException {
        Set<String> referencedStylesheets = controlStylesheetReferences();
        List<String> missingResources = new ArrayList<>();
        for (String stylesheet : referencedStylesheets) {
            Path stylesheetPath = CONTROL_STYLESHEET_RESOURCE_ROOT.resolve(stylesheet);
            if (!Files.isRegularFile(stylesheetPath)) {
                missingResources.add("controls/" + stylesheet);
            }
        }

        assertTrue(!referencedStylesheets.isEmpty(), "Production sources should reference bundled control CSS");
        assertTrue(missingResources.isEmpty(),
                () -> "controlStylesheet references must resolve to bundled resources: " + missingResources);
    }

    /// Verifies that bundled control stylesheets are reachable from Java source or base stylesheet imports.
    @Test
    void bundledControlStylesheetsAreReferenced() throws IOException {
        Set<String> referencedStylesheets = controlStylesheetReferences();
        for (String importPath : stylesheetImportReferences()) {
            if (importPath.startsWith("controls/")) {
                referencedStylesheets.add(importPath.substring("controls/".length()));
            }
        }

        List<String> unreferencedResources = new ArrayList<>();
        for (Path stylesheet : stylesheetFiles(CONTROL_STYLESHEET_RESOURCE_ROOT)) {
            String relativePath = resourceRelativePath(CONTROL_STYLESHEET_RESOURCE_ROOT, stylesheet);
            if (!referencedStylesheets.contains(relativePath)) {
                unreferencedResources.add("controls/" + relativePath);
            }
        }

        assertTrue(unreferencedResources.isEmpty(),
                () -> "Bundled control stylesheets must be referenced by source or imported from base.css: "
                        + unreferencedResources);
    }

    /// Verifies that bundled control stylesheets do not declare rules for unrelated public control roots.
    @Test
    void bundledControlStylesheetsOnlyTargetOwnedControlRoots() throws IOException {
        Map<String, Set<String>> rootStyleClassesByStylesheet = publicControlRootStyleClassesByStylesheet();
        Set<String> controlRootStyleClasses = rootStyleClassesByStylesheet.values().stream()
                .collect(TreeSet::new, Set::addAll, Set::addAll);

        List<String> invalidRules = new ArrayList<>();
        for (Path stylesheet : stylesheetFiles(CONTROL_STYLESHEET_RESOURCE_ROOT)) {
            String stylesheetName = resourceRelativePath(CONTROL_STYLESHEET_RESOURCE_ROOT, stylesheet);
            Set<String> ownedStyleClasses = rootStyleClassesByStylesheet.getOrDefault(stylesheetName, Set.of());
            Matcher matcher = CSS_RULE.matcher(Files.readString(stylesheet));
            while (matcher.find()) {
                String[] selectors = matcher.group(1).split(",");
                for (String selector : selectors) {
                    String trimmedSelector = selector.trim();
                    @org.jetbrains.annotations.Nullable String rootStyleClass =
                            controlRootStyleClassInSelectorSubject(trimmedSelector, controlRootStyleClasses);
                    if (rootStyleClass != null && !ownedStyleClasses.contains(rootStyleClass)) {
                        invalidRules.add(stylesheetName + ": " + trimmedSelector + " targets ."
                                + rootStyleClass + " but the stylesheet owns " + ownedStyleClasses);
                    }
                }
            }
        }

        assertTrue(invalidRules.isEmpty(),
                () -> "Control stylesheets must keep direct root rules in the stylesheet owned by that control: "
                        + invalidRules);
    }

    /// Verifies that project CSS only looks up generated or locally declared token properties.
    @Test
    void stylesheetTokenLookupsResolveToGeneratedOrLocalDeclarations() throws IOException {
        Set<String> generatedTokens = tokenDeclarations(M3Theme.defaultTheme().tokens().toRootStyleDeclarations());
        assertTrue(!generatedTokens.isEmpty(), "Generated root token declarations should not be empty");

        List<String> missingTokens = new ArrayList<>();
        for (Path stylesheet : projectStylesheetFiles()) {
            Set<String> declaredTokens = new TreeSet<>(generatedTokens);
            declaredTokens.addAll(tokenDeclarations(Files.readString(stylesheet)));

            List<String> lines = Files.readAllLines(stylesheet);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                Matcher matcher = TOKEN_REFERENCE.matcher(lines.get(lineIndex));
                while (matcher.find()) {
                    String token = matcher.group();
                    if (!declaredTokens.contains(token)) {
                        missingTokens.add(stylesheet + ":" + (lineIndex + 1) + ": " + token);
                    }
                }
            }
        }

        assertTrue(missingTokens.isEmpty(),
                () -> "Project CSS must only reference generated or locally declared token properties: "
                        + missingTokens);
    }

    /// Verifies that CSS token lookups are not quoted as string literals.
    @Test
    void stylesheetTokenLookupsAreNotQuotedStrings() throws IOException {
        List<String> quotedTokens = new ArrayList<>();
        for (Path stylesheet : projectStylesheetFiles()) {
            List<String> lines = Files.readAllLines(stylesheet);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (QUOTED_TOKEN_VALUE.matcher(line).find()) {
                    quotedTokens.add(stylesheet + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(quotedTokens.isEmpty(),
                () -> "CSS token lookups must remain paint/size lookups, not quoted string values: " + quotedTokens);
    }

    /// Returns whether one public control type name looks like an implementation helper.
    private static boolean isForbiddenPublicControlImplementationType(String typeName) {
        if (FORBIDDEN_PUBLIC_CONTROL_TYPE_NAMES.contains(typeName)) {
            return true;
        }
        for (String suffix : FORBIDDEN_PUBLIC_CONTROL_TYPE_SUFFIXES) {
            if (typeName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one concrete JavaFX superclass is an allowed public control API exception.
    private static boolean isAllowedConcreteControlInheritance(String typeName, String superclassName) {
        return superclassName.equals(ALLOWED_CONCRETE_CONTROL_SUPERCLASSES.get(typeName));
    }

    /// Returns the declared record component names for one record type.
    private static @Unmodifiable Set<String> recordComponentNames(Class<?> type) {
        Set<String> names = new TreeSet<>();
        var components = type.getRecordComponents();
        if (components != null) {
            for (var component : components) {
                names.add(component.getName());
            }
        }
        return Set.copyOf(names);
    }

    /// Returns whether a method is generated record infrastructure.
    private static boolean isRecordInfrastructureMethod(Method method) {
        String name = method.getName();
        if (method.getParameterCount() == 0) {
            return name.equals("toString") || name.equals("hashCode");
        }
        return name.equals("equals") && method.getParameterCount() == 1;
    }

    /// Returns every production Java source file in a stable order.
    private static @Unmodifiable List<Path> productionJavaSourceFiles() throws IOException {
        List<Path> sourceFiles = new ArrayList<>();
        for (Path sourceRoot : PRODUCTION_SOURCE_ROOTS) {
            sourceFiles.addAll(javaSourceFiles(sourceRoot));
        }
        sourceFiles.sort(Comparator.comparing(Path::toString));
        return List.copyOf(sourceFiles);
    }

    /// Returns a sorted immutable set from a text block containing one type name per line.
    private static @Unmodifiable Set<String> typeNames(String names) {
        Set<String> typeNames = new TreeSet<>();
        names.lines()
                .map(String::strip)
                .filter(name -> !name.isEmpty())
                .forEach(typeNames::add);
        return Set.copyOf(typeNames);
    }

    /// Returns a stable public method signature string.
    private static String publicMethodSignature(Class<?> owner, Method method) {
        StringBuilder signature = new StringBuilder(owner.getName())
                .append('#')
                .append(method.getName())
                .append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            if (index > 0) {
                signature.append(',');
            }
            signature.append(typeName(parameterTypes[index]));
        }
        return signature.append(')').toString();
    }

    /// Returns a stable source-like type name for method signatures.
    private static String typeName(Class<?> type) {
        if (type.isArray()) {
            return typeName(type.getComponentType()) + "[]";
        }
        return type.getName();
    }

    /// Returns whether a public method exposes a node or a list of nodes.
    private static boolean returnsNodeSurface(Method method) {
        if (Node.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        if (!ObservableList.class.isAssignableFrom(method.getReturnType())
                && !List.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            return false;
        }
        Type[] arguments = parameterizedType.getActualTypeArguments();
        return arguments.length == 1 && isNodeSurfaceType(arguments[0]);
    }

    /// Returns whether a public method exposes a property whose value is a node type.
    private static boolean returnsNodeProperty(Method method) {
        if (!ReadOnlyProperty.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            return false;
        }
        Type[] arguments = parameterizedType.getActualTypeArguments();
        return arguments.length == 1 && isNodeSurfaceType(arguments[0]);
    }

    /// Returns whether a reflected generic type denotes a node type.
    private static boolean isNodeSurfaceType(Type type) {
        if (type instanceof Class<?> typeClass) {
            return Node.class.isAssignableFrom(typeClass);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return isNodeSurfaceType(parameterizedType.getRawType());
        }
        if (type instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (isNodeSurfaceType(upperBound)) {
                    return true;
                }
            }
        }
        return false;
    }
    /// Returns public mutable `ObservableList` getter names grouped by declaring class name.
    private static Map<String, Set<String>> publicMutableObservableListGetterNamesByClass() throws IOException {
        Map<String, Set<String>> gettersByClass = new TreeMap<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            Set<String> getterNames = new TreeSet<>();
            for (String line : Files.readAllLines(sourceFile)) {
                Matcher matcher = PUBLIC_OBSERVABLE_LIST_GETTER_DECLARATION.matcher(line);
                if (matcher.find() && matcher.group(1) == null) {
                    getterNames.add(matcher.group(2));
                }
            }
            if (!getterNames.isEmpty()) {
                String fileName = sourceFile.getFileName().toString();
                String typeName = fileName.substring(0, fileName.length() - ".java".length());
                gettersByClass.put(packageName(sourceFile) + '.' + typeName, getterNames);
            }
        }
        return gettersByClass;
    }

    /// Returns collection mutator names that duplicate the list returned by one getter.
    private static @Unmodifiable Set<String> listMutatorsForGetter(String getterName) {
        String pluralName = getterName.substring("get".length());
        String singularName = singularListElementName(pluralName);
        Set<String> names = new TreeSet<>();
        names.add("add" + singularName);
        names.add("add" + pluralName);
        names.add("remove" + singularName);
        names.add("remove" + pluralName);
        names.add("set" + pluralName);
        names.add("clear" + pluralName);
        return Set.copyOf(names);
    }

    /// Returns the conventional singular element name for one list property name.
    private static String singularListElementName(String pluralName) {
        if (pluralName.endsWith("ies") && pluralName.length() > "ies".length()) {
            return pluralName.substring(0, pluralName.length() - "ies".length()) + 'y';
        }
        if (pluralName.endsWith("s") && !pluralName.endsWith("ss") && pluralName.length() > 1) {
            return pluralName.substring(0, pluralName.length() - 1);
        }
        return pluralName;
    }

    /// Returns public top-level type names grouped by exported package.
    private static Map<String, Set<String>> exportedPublicTopLevelTypeNamesByPackage() throws IOException {
        Map<String, Set<String>> typesByPackage = new TreeMap<>();
        for (String packageName : exportedPackageNames()) {
            typesByPackage.put(packageName, new TreeSet<>());
        }

        for (Path sourceRoot : exportedPackageSourceRoots()) {
            for (Path sourceFile : javaSourceFiles(sourceRoot)) {
                String packageName = packageName(sourceFile);
                Set<String> packageTypes = typesByPackage.get(packageName);
                if (packageTypes == null) {
                    continue;
                }

                for (String line : Files.readAllLines(sourceFile)) {
                    Matcher matcher = PUBLIC_TOP_LEVEL_TYPE_DECLARATION.matcher(line);
                    if (matcher.find()) {
                        packageTypes.add(matcher.group(1));
                        break;
                    }
                }
            }
        }

        return typesByPackage;
    }

    /// Returns every exported package name declared by the main module descriptor.
    private static Set<String> exportedPackageNames() throws IOException {
        Set<String> packages = new TreeSet<>();
        for (String line : Files.readAllLines(MAIN_MODULE_INFO)) {
            Matcher matcher = MODULE_EXPORT_DECLARATION.matcher(line);
            if (matcher.find()) {
                packages.add(matcher.group(1));
            }
        }
        assertTrue(!packages.isEmpty(), "The main module descriptor should export API packages");
        return packages;
    }

    /// Returns every exported source package root declared by the main module descriptor.
    private static @Unmodifiable List<Path> exportedPackageSourceRoots() throws IOException {
        List<Path> roots = new ArrayList<>();
        for (String packageName : exportedPackageNames()) {
            roots.add(Path.of("src", "main", "java").resolve(packageName.replace('.', '/')));
        }
        return List.copyOf(roots);
    }

    /// Returns every top-level class name declared in exported package source roots.
    private static @Unmodifiable List<String> exportedTopLevelClassNames(Set<String> exportedPackages)
            throws IOException {
        List<String> classNames = new ArrayList<>();
        for (Path sourceRoot : exportedPackageSourceRoots()) {
            for (Path sourceFile : javaSourceFiles(sourceRoot)) {
                if (sourceFile.getFileName().toString().equals("package-info.java")) {
                    continue;
                }

                String packageName = packageName(sourceFile);
                if (exportedPackages.contains(packageName)) {
                    String fileName = sourceFile.getFileName().toString();
                    classNames.add(packageName + '.' + fileName.substring(0, fileName.length() - ".java".length()));
                }
            }
        }
        classNames.sort(Comparator.naturalOrder());
        return List.copyOf(classNames);
    }

    /// Returns the declared package name for a Java source file.
    private static String packageName(Path sourceFile) throws IOException {
        for (String line : Files.readAllLines(sourceFile)) {
            String strippedLine = line.strip();
            if (strippedLine.startsWith("package ") && strippedLine.endsWith(";")) {
                return strippedLine.substring("package ".length(), strippedLine.length() - 1).strip();
            }
        }
        throw new AssertionError("Missing package declaration in " + sourceFile);
    }

    /// Returns every Java source file below a root.
    private static @Unmodifiable List<Path> javaSourceFiles(Path sourceRoot) throws IOException {
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("module-info.java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    /// Collects exported member declarations that expose internal implementation types.
    private static void collectInternalApiLeaks(Path sourceFile, List<String> leaks) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile);
        StringBuilder declaration = new StringBuilder();
        int declarationLine = -1;
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).strip();
            if (declarationLine < 0) {
                if (!(line.startsWith("public ") || line.startsWith("protected "))) {
                    continue;
                }
                declaration.setLength(0);
                declarationLine = index + 1;
            }

            int commentIndex = line.indexOf("//");
            String code = commentIndex >= 0 ? line.substring(0, commentIndex).stripTrailing() : line;
            if (!code.isEmpty() && !code.startsWith("///") && !code.startsWith("*")) {
                if (!declaration.isEmpty()) {
                    declaration.append(' ');
                }
                declaration.append(code);
            }

            if (code.endsWith(";") || code.endsWith("{")) {
                String exportedDeclaration = declaration.toString();
                int permitsIndex = exportedDeclaration.indexOf(" permits ");
                if (permitsIndex >= 0) {
                    exportedDeclaration = exportedDeclaration.substring(0, permitsIndex);
                }
                if (EXPORTED_INTERNAL_API_REFERENCE.matcher(exportedDeclaration).find()) {
                    leaks.add(sourceFile + ":" + declarationLine + ": " + exportedDeclaration);
                }
                declarationLine = -1;
                declaration.setLength(0);
            }
        }
    }

    /// Collects unexported M3FX types exposed by one public exported class.
    private static void collectUnexportedM3fxTypeLeaks(
            Class<?> owner,
            Set<String> exportedPackages,
            List<String> leaks
    ) {
        collectTypeLeaks(owner.getGenericSuperclass(), owner.getName() + " superclass", exportedPackages, leaks);
        for (Type type : owner.getGenericInterfaces()) {
            collectTypeLeaks(type, owner.getName() + " interface", exportedPackages, leaks);
        }
        collectTypeParameterLeaks(owner.getTypeParameters(), owner.getName() + " type parameter", exportedPackages, leaks);

        for (Constructor<?> constructor : owner.getDeclaredConstructors()) {
            if (isPublicOrProtected(constructor) && !constructor.isSynthetic()) {
                String description = owner.getName() + " constructor";
                collectTypeParameterLeaks(constructor.getTypeParameters(), description, exportedPackages, leaks);
                for (Type type : constructor.getGenericParameterTypes()) {
                    collectTypeLeaks(type, description + " parameter", exportedPackages, leaks);
                }
                for (Type type : constructor.getGenericExceptionTypes()) {
                    collectTypeLeaks(type, description + " throws", exportedPackages, leaks);
                }
            }
        }

        for (Method method : owner.getDeclaredMethods()) {
            if (isPublicOrProtected(method) && !method.isSynthetic()) {
                String description = owner.getName() + '#' + method.getName() + "()";
                collectTypeParameterLeaks(method.getTypeParameters(), description, exportedPackages, leaks);
                collectTypeLeaks(method.getGenericReturnType(), description + " return", exportedPackages, leaks);
                for (Type type : method.getGenericParameterTypes()) {
                    collectTypeLeaks(type, description + " parameter", exportedPackages, leaks);
                }
                for (Type type : method.getGenericExceptionTypes()) {
                    collectTypeLeaks(type, description + " throws", exportedPackages, leaks);
                }
            }
        }

        for (Field field : owner.getDeclaredFields()) {
            if (isPublicOrProtected(field) && !field.isSynthetic()) {
                collectTypeLeaks(
                        field.getGenericType(),
                        owner.getName() + '#' + field.getName() + " field",
                        exportedPackages,
                        leaks
                );
            }
        }

        for (Class<?> nestedClass : owner.getDeclaredClasses()) {
            if (Modifier.isPublic(nestedClass.getModifiers()) || Modifier.isProtected(nestedClass.getModifiers())) {
                collectUnexportedM3fxTypeLeaks(nestedClass, exportedPackages, leaks);
            }
        }
    }

    /// Collects exported field declarations that are not immutable constants.
    private static void collectExportedFieldMutabilityViolations(Class<?> owner, List<String> violations) {
        for (Field field : owner.getDeclaredFields()) {
            if (!isPublicOrProtected(field) || field.isSynthetic() || field.isEnumConstant()) {
                continue;
            }

            int modifiers = field.getModifiers();
            String fieldName = owner.getName() + '#' + field.getName();
            if (!Modifier.isStatic(modifiers)) {
                violations.add(fieldName + " is an instance field");
            } else if (!Modifier.isFinal(modifiers)) {
                violations.add(fieldName + " is a mutable static field");
            } else if (field.getType().isArray()) {
                violations.add(fieldName + " is an array constant");
            }
        }

        for (Class<?> nestedClass : owner.getDeclaredClasses()) {
            int modifiers = nestedClass.getModifiers();
            if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
                collectExportedFieldMutabilityViolations(nestedClass, violations);
            }
        }
    }

    /// Returns whether a reflected member is public or protected.
    private static boolean isPublicOrProtected(Member member) {
        int modifiers = member.getModifiers();
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
    }

    /// Returns whether one constructor parameter is a batch collection input.
    private static boolean hasBatchParameter(Class<?>[] parameterTypes) {
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isArray() || Iterable.class.isAssignableFrom(parameterType)) {
                return true;
            }
        }
        return false;
    }

    /// Returns a stable public batch constructor signature for contract checks.
    private static String batchConstructorSignature(Constructor<?> constructor) {
        StringBuilder signature = new StringBuilder(constructor.getDeclaringClass().getName()).append('(');
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            if (index > 0) {
                signature.append(',');
            }
            signature.append(simpleTypeName(parameterTypes[index]));
        }
        return signature.append(')').toString();
    }

    /// Returns a compact type name for public constructor signature checks.
    private static String simpleTypeName(Class<?> type) {
        if (type.isArray()) {
            return simpleTypeName(type.getComponentType()) + "[]";
        }
        return type.getSimpleName();
    }

    /// Collects unexported M3FX types referenced by type parameter bounds.
    private static void collectTypeParameterLeaks(
            TypeVariable<?>[] typeParameters,
            String description,
            Set<String> exportedPackages,
            List<String> leaks
    ) {
        for (TypeVariable<?> typeParameter : typeParameters) {
            for (Type bound : typeParameter.getBounds()) {
                collectTypeLeaks(bound, description + ' ' + typeParameter.getName() + " bound", exportedPackages, leaks);
            }
        }
    }

    /// Collects unexported M3FX package references from a reflected type.
    private static void collectTypeLeaks(
            Type type,
            String description,
            Set<String> exportedPackages,
            List<String> leaks
    ) {
        if (type instanceof Class<?> typeClass) {
            collectClassTypeLeaks(typeClass, description, exportedPackages, leaks);
        } else if (type instanceof ParameterizedType parameterizedType) {
            collectTypeLeaks(parameterizedType.getRawType(), description + " raw", exportedPackages, leaks);
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                collectTypeLeaks(argument, description + " argument", exportedPackages, leaks);
            }
            Type ownerType = parameterizedType.getOwnerType();
            if (ownerType != null) {
                collectTypeLeaks(ownerType, description + " owner", exportedPackages, leaks);
            }
        } else if (type instanceof GenericArrayType arrayType) {
            collectTypeLeaks(arrayType.getGenericComponentType(), description + " component", exportedPackages, leaks);
        } else if (type instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                collectTypeLeaks(upperBound, description + " upper bound", exportedPackages, leaks);
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                collectTypeLeaks(lowerBound, description + " lower bound", exportedPackages, leaks);
            }
        } else if (type instanceof TypeVariable<?>) {
            // Type variable bounds are checked at the class, constructor, or method declaration site.
        }
    }

    /// Collects unexported M3FX package references from a reflected class type.
    private static void collectClassTypeLeaks(
            Class<?> typeClass,
            String description,
            Set<String> exportedPackages,
            List<String> leaks
    ) {
        if (typeClass.isArray()) {
            collectClassTypeLeaks(typeClass.getComponentType(), description + " component", exportedPackages, leaks);
            return;
        }

        String packageName = typeClass.getPackageName();
        if (packageName.startsWith("org.glavo.m3fx") && !exportedPackages.contains(packageName)) {
            leaks.add(description + " exposes " + typeClass.getName());
        }
    }

    /// Returns public control root style classes and whether each root supports `-fx-alignment` directly.
    private static Map<String, Boolean> publicControlRootAlignmentSupport() throws IOException {
        Map<String, String> superclasses = publicControlSuperclasses();
        Map<String, Boolean> styleClasses = new HashMap<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            @org.jetbrains.annotations.Nullable String className = null;
            for (String line : Files.readAllLines(sourceFile)) {
                Matcher matcher = PUBLIC_CONTROL_EXTENDS_DECLARATION.matcher(line);
                if (matcher.find()) {
                    className = matcher.group(1);
                    break;
                }
            }
            if (className == null || !inheritsFromAnyJavaFxBase(className, superclasses, JAVA_FX_CONTROL_BASE_CLASSES)) {
                continue;
            }

            Matcher matcher = STYLE_CLASS_DECLARATION.matcher(Files.readString(sourceFile));
            if (matcher.find()) {
                styleClasses.put(
                        matcher.group(1),
                        inheritsFromAnyJavaFxBase(className, superclasses, JAVA_FX_ALIGNED_CONTROL_BASE_CLASSES)
                );
            }
        }
        return styleClasses;
    }

    /// Returns whether a source-declared class inherits from any requested JavaFX base name.
    private static boolean inheritsFromAnyJavaFxBase(
            String className,
            Map<String, String> superclasses,
            Set<String> baseClassNames
    ) {
        Set<String> visitedClasses = new TreeSet<>();
        @org.jetbrains.annotations.Nullable String currentClass = className;
        while (currentClass != null && visitedClasses.add(currentClass)) {
            @org.jetbrains.annotations.Nullable String superclassName = superclasses.get(currentClass);
            if (superclassName == null) {
                return baseClassNames.contains(currentClass);
            }
            if (baseClassNames.contains(superclassName)) {
                return true;
            }
            currentClass = superclassName;
        }
        return false;
    }

    /// Collects CSS rules that write unsupported layout properties to public control root selectors.
    private static void collectUnsupportedControlRootLayoutRules(
            String sourceName,
            String stylesheet,
            Map<String, Boolean> controlRootAlignmentSupport,
            List<String> invalidRules
    ) {
        Matcher matcher = CSS_RULE.matcher(stylesheet);
        while (matcher.find()) {
            String declarations = matcher.group(2);
            boolean hasSpacing = declarations.contains("-fx-spacing:");
            boolean hasHorizontalGap = declarations.contains("-fx-hgap:");
            boolean hasVerticalGap = declarations.contains("-fx-vgap:");
            boolean hasAlignment = declarations.contains("-fx-alignment:");
            if (!hasSpacing && !hasHorizontalGap && !hasVerticalGap && !hasAlignment) {
                continue;
            }

            String[] selectors = matcher.group(1).split(",");
            for (String selector : selectors) {
                String trimmedSelector = selector.trim();
                @org.jetbrains.annotations.Nullable String rootStyleClass = controlRootStyleClassInSelector(
                        trimmedSelector,
                        controlRootAlignmentSupport.keySet()
                );
                if (rootStyleClass == null) {
                    continue;
                }

                List<String> invalidProperties = new ArrayList<>();
                if (hasSpacing) {
                    invalidProperties.add("-fx-spacing");
                }
                if (hasHorizontalGap) {
                    invalidProperties.add("-fx-hgap");
                }
                if (hasVerticalGap) {
                    invalidProperties.add("-fx-vgap");
                }
                if (hasAlignment && !controlRootAlignmentSupport.get(rootStyleClass)) {
                    invalidProperties.add("-fx-alignment");
                }
                if (!invalidProperties.isEmpty()) {
                    invalidRules.add(sourceName + ": " + trimmedSelector + " { "
                            + String.join(", ", invalidProperties) + " }");
                }
            }
        }
    }

    /// Returns the public control root style class directly targeted by a selector.
    private static @org.jetbrains.annotations.Nullable String controlRootStyleClassInSelector(
            String selector,
            Set<String> controlRootStyleClasses
    ) {
        if (selector.isEmpty() || SELECTOR_HAS_DESCENDANT_OR_COMBINATOR.matcher(selector).find()) {
            return null;
        }

        Matcher matcher = CSS_CLASS_SELECTOR.matcher(selector);
        while (matcher.find()) {
            String styleClass = matcher.group(1);
            if (controlRootStyleClasses.contains(styleClass)) {
                return styleClass;
            }
        }
        return null;
    }

    /// Returns the public control root style class in the leftmost compound selector.
    private static @org.jetbrains.annotations.Nullable String controlRootStyleClassInSelectorSubject(
            String selector,
            Set<String> controlRootStyleClasses
    ) {
        if (selector.isEmpty()) {
            return null;
        }

        int subjectEnd = 0;
        while (subjectEnd < selector.length()) {
            char ch = selector.charAt(subjectEnd);
            if (Character.isWhitespace(ch) || ch == '>' || ch == '+' || ch == '~') {
                break;
            }
            subjectEnd++;
        }

        if (subjectEnd == 0) {
            return null;
        }

        Matcher matcher = CSS_CLASS_SELECTOR.matcher(selector.substring(0, subjectEnd));
        while (matcher.find()) {
            String styleClass = matcher.group(1);
            if (controlRootStyleClasses.contains(styleClass)) {
                return styleClass;
            }
        }
        return null;
    }

    /// Returns every project CSS stylesheet that can reference generated Material tokens.
    private static @Unmodifiable List<Path> projectStylesheetFiles() throws IOException {
        List<Path> stylesheets = new ArrayList<>();
        stylesheets.addAll(stylesheetFiles(STYLESHEET_RESOURCE_ROOT));
        stylesheets.addAll(stylesheetFiles(DEMO_STYLESHEET_RESOURCE_ROOT));
        stylesheets.sort(Comparator.comparing(Path::toString));
        return List.copyOf(stylesheets);
    }

    /// Returns every CSS stylesheet below a root.
    private static @Unmodifiable List<Path> stylesheetFiles(Path sourceRoot) throws IOException {
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            return files.filter(path -> path.toString().endsWith(".css"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    /// Returns every control stylesheet referenced by production Java sources.
    private static Set<String> controlStylesheetReferences() throws IOException {
        Set<String> references = new TreeSet<>();
        for (Path sourceFile : productionJavaSourceFiles()) {
            Matcher matcher = CONTROL_STYLESHEET_REFERENCE.matcher(Files.readString(sourceFile));
            while (matcher.find()) {
                references.add(matcher.group(1));
            }
        }
        return references;
    }

    /// Returns public control root style classes grouped by the bundled control stylesheet that owns them.
    private static Map<String, Set<String>> publicControlRootStyleClassesByStylesheet() throws IOException {
        Map<String, String> superclasses = publicControlSuperclasses();
        Map<String, Set<String>> styleClassesByStylesheet = new TreeMap<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            @org.jetbrains.annotations.Nullable String className = null;
            for (String line : Files.readAllLines(sourceFile)) {
                Matcher matcher = PUBLIC_CONTROL_EXTENDS_DECLARATION.matcher(line);
                if (matcher.find()) {
                    className = matcher.group(1);
                    break;
                }
            }
            if (className == null || !inheritsFromAnyJavaFxBase(className, superclasses, JAVA_FX_CONTROL_BASE_CLASSES)) {
                continue;
            }

            String source = Files.readString(sourceFile);
            Matcher styleClassMatcher = STYLE_CLASS_DECLARATION.matcher(source);
            if (!styleClassMatcher.find()) {
                continue;
            }

            String rootStyleClass = styleClassMatcher.group(1);
            Matcher stylesheetMatcher = CONTROL_STYLESHEET_REFERENCE.matcher(source);
            while (stylesheetMatcher.find()) {
                styleClassesByStylesheet.computeIfAbsent(stylesheetMatcher.group(1), ignored -> new TreeSet<>())
                        .add(rootStyleClass);
            }
        }
        return styleClassesByStylesheet;
    }

    /// Returns every stylesheet path imported by bundled CSS resources.
    private static Set<String> stylesheetImportReferences() throws IOException {
        Set<String> references = new TreeSet<>();
        for (Path stylesheet : stylesheetFiles(STYLESHEET_RESOURCE_ROOT)) {
            Matcher matcher = STYLESHEET_IMPORT_REFERENCE.matcher(Files.readString(stylesheet));
            while (matcher.find()) {
                references.add(matcher.group(1));
            }
        }
        return references;
    }

    /// Returns every M3FX or MonetFX token property declared by generated root style declarations.
    private static Set<String> tokenDeclarations(String declarations) {
        Set<String> tokens = new TreeSet<>();
        Matcher matcher = TOKEN_DECLARATION.matcher(declarations);
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return tokens;
    }

    /// Returns direct superclass names for public control source files.
    private static Map<String, String> publicControlSuperclasses() throws IOException {
        Map<String, String> superclasses = new HashMap<>();

        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            for (String line : Files.readAllLines(sourceFile)) {
                Matcher matcher = PUBLIC_CONTROL_EXTENDS_DECLARATION.matcher(line);
                if (matcher.find()) {
                    superclasses.put(matcher.group(1), matcher.group(2));
                    break;
                }
            }
        }

        return superclasses;
    }

    /// Returns a stable slash-separated relative resource path.
    private static String resourceRelativePath(Path root, Path resource) {
        return root.relativize(resource).toString().replace('\\', '/');
    }
}
