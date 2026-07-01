// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

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
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies project source style requirements that are not covered by compiler checks.
@NotNullByDefault
final class SourceStyleRequirementsTest {
    /// Production source roots scanned for repository source style requirements.
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

    /// The public control source root scanned for M3FX control API shape constraints.
    private static final Path CONTROLS_SOURCE_ROOT =
            Path.of("src", "main", "java", "org", "glavo", "m3fx", "controls");

    /// The public skin source root scanned for M3FX skin inheritance constraints.
    private static final Path SKINS_SOURCE_ROOT =
            Path.of("src", "main", "java", "org", "glavo", "m3fx", "skins");

    /// The public token source root scanned for M3FX token API shape constraints.
    private static final Path TOKENS_SOURCE_ROOT =
            Path.of("src", "main", "java", "org", "glavo", "m3fx", "tokens");

    /// The source call that lets sparse Material shapes receive pointer events across their layout bounds.
    private static final String FULL_BOUNDS_PICK_ON_BOUNDS_CALL = "setPickOnBounds(true)";

    /// The bundled stylesheet resource root.
    private static final Path STYLESHEET_RESOURCE_ROOT =
            Path.of("src", "main", "resources", "org", "glavo", "m3fx", "styles");

    /// The bundled control stylesheet resource root.
    private static final Path CONTROL_STYLESHEET_RESOURCE_ROOT =
            STYLESHEET_RESOURCE_ROOT.resolve("controls");

    /// The demo stylesheet resource root.
    private static final Path DEMO_STYLESHEET_RESOURCE_ROOT =
            Path.of("demo", "src", "main", "resources", "org", "glavo", "m3fx", "demo");

    /// The shared logical layout helper that owns physical orientation checks.
    private static final Path LOGICAL_LAYOUT_HELPER_SOURCE =
            Path.of("src", "main", "java", "org", "glavo", "m3fx", "internal", "M3NodeLayout.java");

    /// Matches Java type declarations that must declare their nullability default locally.
    private static final Pattern TYPE_DECLARATION = Pattern.compile(
            "^\\s*(?:(?:public|protected|private)\\s+)?"
                    + "(?:(?:static|final|abstract|sealed|non-sealed|strictfp)\\s+)*"
                    + "(?:class|interface|enum|record)\\s+"
    );

    /// Matches Java `Optional` type usage, including primitive optional variants.
    private static final Pattern OPTIONAL_USAGE = Pattern.compile(
            "\\b(?:java\\.util\\.)?Optional(?:Int|Long|Double)?\\b|\\bOptional\\s*<"
    );

    /// Matches public control class declarations with one direct superclass.
    private static final Pattern PUBLIC_CONTROL_EXTENDS_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "class\\s+(\\w+)\\s*(?:<[^>{}]+>\\s*)?extends\\s+(?:[\\w.]+\\.)?(\\w+)\\b"
    );

    /// Matches public control class declarations.
    private static final Pattern PUBLIC_CONTROL_CLASS_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?class\\s+\\w+\\b"
    );

    /// Matches concrete public control classes and captures their direct superclass.
    private static final Pattern PUBLIC_CONCRETE_CONTROL_EXTENDS_DECLARATION = Pattern.compile(
            "^\\s*public\\s+(?:final\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "class\\s+(\\w+)\\s*(?:<[^>{}]+>\\s*)?extends\\s+(?:[\\w.]+\\.)?(\\w+)\\b"
    );

    /// Matches public static convenience factory names that controls should avoid.
    private static final Pattern PUBLIC_CONTROL_FACTORY_METHOD = Pattern.compile(
            "^\\s*public\\s+(?=[\\w\\s<>,.?@\\[\\]]*\\bstatic\\b)"
                    + "(?:(?:static|final|synchronized)\\s+)*[^;=]+\\b(?:with|of|create)\\s*\\("
    );

    /// Matches concrete M3FX type references inside generic accessibility dispatch paths.
    private static final Pattern CONCRETE_M3_DISPATCH_REFERENCE = Pattern.compile("\\bM3(?!Accessible\\b)[A-Z]\\w*\\b");

    /// Matches public non-canonical convenience constructors on nested token records.
    private static final Pattern PUBLIC_TOKEN_RECORD_CONVENIENCE_CONSTRUCTOR = Pattern.compile(
            "^\\s*public\\s+\\w+Tokens\\s*\\("
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

    /// The core control style test source used to enforce matrix coverage discipline.
    private static final Path CONTROL_STYLE_TEST_SOURCE =
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "controls", "M3ControlStyleTest.java");

    /// Matches `M3Stylesheets.controlStylesheet` references in production sources.
    private static final Pattern CONTROL_STYLESHEET_REFERENCE = Pattern.compile(
            "controlStylesheet\\(\"([^\"]+\\.css)\"\\)"
    );

    /// Matches control instances asserted by the core user-agent stylesheet matrix.
    private static final Pattern USER_AGENT_STYLESHEET_ASSERTION = Pattern.compile(
            "assertUserAgentStylesheet\\(new\\s+(M3\\w+)(?:\\s*<[^>]*>)?\\s*\\("
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

    /// Matches direct physical right-to-left orientation checks outside the shared logical layout helper.
    private static final Pattern DIRECT_RIGHT_TO_LEFT_ORIENTATION_CHECK = Pattern.compile(
            "getEffectiveNodeOrientation\\(\\)\\s*==\\s*NodeOrientation\\.RIGHT_TO_LEFT"
                    + "|NodeOrientation\\.RIGHT_TO_LEFT\\s*==\\s*getEffectiveNodeOrientation\\(\\)"
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

    /// Matches direct Region layout-property writes that public control metric code must guard.
    private static final Pattern DIRECT_REGION_LAYOUT_WRITE = Pattern.compile(
            "\\bset(?:MinWidth|PrefWidth|MaxWidth|MinHeight|PrefHeight|MaxHeight|Padding)\\s*\\("
    );

    /// Exact direct Region layout-property writes allowed for shared helpers and private implementation nodes.
    private static final @Unmodifiable Map<String, @Unmodifiable Set<String>> ALLOWED_DIRECT_REGION_LAYOUT_WRITES = Map.of(
            "M3Css.java", Set.of(
                    "region.setMinWidth(width);",
                    "region.setPrefWidth(width);",
                    "region.setMaxWidth(width);",
                    "region.setMinHeight(height);",
                    "region.setPrefHeight(height);",
                    "region.setMaxHeight(height);",
                    "region.setPadding(padding);"
            ),
            "M3TextInputLayout.java", Set.of(
                    "label.setPadding(new Insets(",
                    "label.setPadding(Insets.EMPTY);"
            )
    );

    /// Direct control writes to public child layout properties that must check bound properties first.
    private static final @Unmodifiable Map<String, @Unmodifiable Map<String, String>>
            BOUND_GUARDED_CONTROL_LAYOUT_WRITES = Map.of(
                    "M3ListViewCell.java", Map.of(
                            "itemNode.setMinWidth(0.0);", "itemNode.minWidthProperty().isBound()",
                            "itemNode.setMaxWidth(Double.MAX_VALUE);", "itemNode.maxWidthProperty().isBound()",
                            "listItem.setPrefWidth(width);", "listItem.prefWidthProperty().isBound()"
                    )
            );

    /// Direct skin writes to public child layout properties that must check bound properties first.
    private static final @Unmodifiable Map<String, @Unmodifiable Map<String, String>> BOUND_GUARDED_SKIN_LAYOUT_WRITES =
            Map.of(
                    "M3NavigationDrawerSkin.java", Map.of(
                            "region.setMinWidth(0.0);", "region.minWidthProperty().isBound()",
                            "region.setMaxWidth(itemWidth);", "region.maxWidthProperty().isBound()"
                    ),
                    "M3NavigationDrawerGroupSkin.java", Map.of(
                            "item.setMinWidth(0.0);", "item.minWidthProperty().isBound()",
                            "item.setMaxWidth(itemWidth);", "item.maxWidthProperty().isBound()"
                    )
            );

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
            "ProgressBar",
            "ProgressIndicator",
            "RadioButton",
            "Slider",
            "SplitMenuButton",
            "ToggleButton",
            "Tooltip"
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

    /// Verifies that every production source type declares a local nullability default.
    @Test
    void productionTypesDeclareNotNullByDefault() throws IOException {
        List<String> missingAnnotations = new ArrayList<>();
        for (Path sourceFile : productionJavaSourceFiles()) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (TYPE_DECLARATION.matcher(line).find()
                        && !hasLeadingNotNullByDefaultAnnotation(lines, lineIndex)) {
                    missingAnnotations.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(missingAnnotations.isEmpty(),
                () -> "Production source types missing @NotNullByDefault: " + missingAnnotations);
    }

    /// Verifies that production APIs and implementation code do not use Java `Optional`.
    @Test
    void productionSourcesDoNotUseOptional() throws IOException {
        List<String> optionalUsages = new ArrayList<>();
        for (Path sourceFile : productionJavaSourceFiles()) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (OPTIONAL_USAGE.matcher(line).find()) {
                    optionalUsages.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(optionalUsages.isEmpty(),
                () -> "Production sources must use @Nullable instead of Optional: " + optionalUsages);
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
                    SourceStyleRequirementsTest.class.getClassLoader()
            );
            if (Modifier.isPublic(type.getModifiers())) {
                collectUnexportedM3fxTypeLeaks(type, exportedPackages, leakedTypes);
            }
        }

        assertTrue(leakedTypes.isEmpty(),
                () -> "Exported APIs must not expose types from unexported M3FX packages: " + leakedTypes);
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
                if (matcher.find() && FORBIDDEN_CONCRETE_CONTROL_SUPERCLASSES.contains(matcher.group(2))) {
                    forbiddenInheritances.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(forbiddenInheritances.isEmpty(),
                () -> "M3FX controls must not extend concrete JavaFX controls: " + forbiddenInheritances);
    }

    /// Verifies that public controls expose constructors and properties instead of narrow static factories.
    @Test
    void publicControlsDoNotExposeConvenienceFactories() throws IOException {
        List<String> factoryMethods = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (PUBLIC_CONTROL_FACTORY_METHOD.matcher(line).find()) {
                    factoryMethods.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(factoryMethods.isEmpty(),
                () -> "Public controls should use constructors and properties instead of with/of/create factories: "
                        + factoryMethods);
    }

    /// Verifies that token records expose only their canonical record construction API.
    @Test
    void tokenRecordsDoNotExposeConvenienceConstructors() throws IOException {
        List<String> constructors = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(TOKENS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (PUBLIC_TOKEN_RECORD_CONVENIENCE_CONSTRUCTOR.matcher(line).find()) {
                    constructors.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(constructors.isEmpty(),
                () -> "Token records should expose canonical record constructors instead of convenience overloads: "
                        + constructors);
    }

    /// Verifies that public controls with custom CSS metadata expose class and instance metadata entry points.
    @Test
    void publicStyleableControlsExposeCssMetaDataEntrypoints() throws IOException {
        List<String> missingEntrypoints = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            String source = Files.readString(sourceFile);
            if (!hasMatchingLine(lines, PUBLIC_CONTROL_CLASS_DECLARATION)
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

    /// Verifies that custom CSS metadata respects API-assigned styleable property values.
    @Test
    void customCssMetaDataSettableChecksRespectUserAssignedValues() throws IOException {
        List<String> unsafeSettableChecks = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (line.contains("public boolean isSettable(")
                        && !settableBlockUsesM3Css(lines, lineIndex)) {
                    unsafeSettableChecks.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(unsafeSettableChecks.isEmpty(),
                () -> "Custom CssMetaData isSettable implementations must delegate to M3Css.isSettable: "
                        + unsafeSettableChecks);
    }

    /// Verifies that public control metric writes preserve application-bound layout properties.
    @Test
    void publicControlMetricWritesUseBoundSafeHelpers() throws IOException {
        Map<String, Set<String>> unusedAllowedWrites = new HashMap<>();
        for (Map.Entry<String, @Unmodifiable Set<String>> entry : ALLOWED_DIRECT_REGION_LAYOUT_WRITES.entrySet()) {
            unusedAllowedWrites.put(entry.getKey(), new TreeSet<>(entry.getValue()));
        }

        List<String> directWrites = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            String sourceName = sourceFile.getFileName().toString();
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex).trim();
                if (DIRECT_REGION_LAYOUT_WRITE.matcher(line).find()
                        && !removeAllowedDirectRegionLayoutWrite(unusedAllowedWrites, sourceName, line)
                        && !isBoundGuardedControlLayoutWrite(sourceName, line)) {
                    directWrites.add(sourceFile + ":" + (lineIndex + 1) + ": " + line);
                }
            }
        }

        List<String> staleAllowedWrites = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : unusedAllowedWrites.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                staleAllowedWrites.add(entry.getKey() + ": " + entry.getValue());
            }
        }

        assertTrue(directWrites.isEmpty(),
                () -> "Public controls must write Region layout metrics through M3Css bound-safe helpers: "
                        + directWrites);
        assertTrue(staleAllowedWrites.isEmpty(),
                () -> "Allowed direct Region layout writes should stay exact and current: " + staleAllowedWrites);
    }

    /// Verifies that implementation code guards layout writes made to public child controls.
    @Test
    void publicChildLayoutWritesCheckBoundProperties() throws IOException {
        List<String> unguardedWrites = new ArrayList<>();
        collectUnguardedPublicChildLayoutWrites(
                CONTROLS_SOURCE_ROOT,
                BOUND_GUARDED_CONTROL_LAYOUT_WRITES,
                unguardedWrites
        );
        collectUnguardedPublicChildLayoutWrites(
                SKINS_SOURCE_ROOT,
                BOUND_GUARDED_SKIN_LAYOUT_WRITES,
                unguardedWrites
        );

        assertTrue(unguardedWrites.isEmpty(),
                () -> "Layout writes to public child properties must check bound properties first: "
                        + unguardedWrites);
    }

    /// Verifies that public button-like controls enable full layout bounds as pointer hit targets.
    @Test
    void publicButtonLikeControlsEnableFullBoundsHitTargets() throws IOException {
        Map<String, String> superclasses = publicControlSuperclasses();
        Map<String, String> sources = publicControlSources();
        Set<String> expectedClasses = publicConcreteControlsInheritingFrom("ButtonBase", superclasses);
        expectedClasses.addAll(publicConcreteControlsInheritingFrom("M3ListItem", superclasses));

        List<String> missingClasses = new ArrayList<>();
        for (String className : expectedClasses) {
            if (!inheritsSourceSnippet(className, superclasses, sources, FULL_BOUNDS_PICK_ON_BOUNDS_CALL)) {
                missingClasses.add(className);
            }
        }

        assertTrue(missingClasses.isEmpty(),
                () -> "Public button-like controls must enable or inherit full-bounds pointer picking: "
                        + missingClasses);
    }

    /// Verifies that control-owned accessibility actions are reachable through installed route handlers.
    @Test
    void controlAccessibleActionsInstallM3AccessibleRoutes() throws IOException {
        List<String> missingFocusRoutes = new ArrayList<>();
        List<String> missingShowRoutes = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            String sourceName = sourceFile.getFileName().toString();
            if ("M3Accessible.java".equals(sourceName)) {
                continue;
            }

            String className = sourceName.substring(0, sourceName.length() - ".java".length());
            String source = Files.readString(sourceFile);
            @org.jetbrains.annotations.Nullable String route = accessibleActionRouteInstallation(source);
            if (declaresAccessibleAction(source, "REQUEST_FOCUS") && !routeHasHandler(route, 1)) {
                missingFocusRoutes.add(className);
            }
            if (declaresAccessibleAction(source, "SHOW_ITEM") && !routeHasHandler(route, 2)) {
                missingShowRoutes.add(className);
            }
        }

        assertTrue(missingFocusRoutes.isEmpty(),
                () -> "Controls with REQUEST_FOCUS actions must install M3Accessible focus routes: "
                        + missingFocusRoutes);
        assertTrue(missingShowRoutes.isEmpty(),
                () -> "Controls with SHOW_ITEM actions must install M3Accessible reveal routes: " + missingShowRoutes);
    }

    /// Verifies that generic accessibility action dispatch stays decoupled from concrete controls.
    @Test
    void m3AccessibleActionDispatchDoesNotDependOnConcreteControls() throws IOException {
        String source = Files.readString(CONTROLS_SOURCE_ROOT.resolve("M3Accessible.java"));
        List<String> concreteReferences = new ArrayList<>();
        addConcreteM3References(concreteReferences, "requestAccessibleFocus",
                sourceMethodBody(source, "static boolean requestAccessibleFocus(@Nullable Node item)"));
        addConcreteM3References(concreteReferences, "showOwnAccessibleActionTarget",
                sourceMethodBody(source, "private static boolean showOwnAccessibleActionTarget("));

        assertTrue(concreteReferences.isEmpty(),
                () -> "Generic accessibility action dispatch must use installed routes, not concrete controls: "
                        + concreteReferences);
    }

    /// Verifies that non-node accessibility reveal targets stay owned by installed routes, not generic helpers.
    @Test
    void m3AccessibleNonNodeRevealTargetsUseInstalledRoutes() throws IOException {
        String source = Files.readString(CONTROLS_SOURCE_ROOT.resolve("M3Accessible.java"));
        List<String> forbiddenReferences = List.of(
                "containsPickerValueTarget",
                "LocalDate",
                "LocalTime",
                "M3DatePicker",
                "M3DateRangePicker",
                "M3DatePickerField",
                "M3DateRangePickerField",
                "M3TimePicker",
                "M3TimePickerField"
        );
        List<String> presentReferences = forbiddenReferences.stream()
                .filter(source::contains)
                .toList();

        assertTrue(presentReferences.isEmpty(),
                () -> "Non-node reveal target matching must be supplied by installed accessibility routes: "
                        + presentReferences);
    }
    /// Verifies that installed accessibility routes keep the same action entry points as JavaFX actions.
    @Test
    void controlAccessibleActionRoutesMirrorExecuteAccessibleActions() throws IOException {
        List<String> mismatchedRoutes = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            String sourceName = sourceFile.getFileName().toString();
            if ("M3Accessible.java".equals(sourceName)) {
                continue;
            }

            String source = Files.readString(sourceFile);
            @org.jetbrains.annotations.Nullable String route = accessibleActionRouteInstallation(source);
            if (route == null) {
                continue;
            }

            String className = sourceName.substring(0, sourceName.length() - ".java".length());
            List<String> routeArguments = topLevelArguments(route);
            verifyAccessibleRouteMatchesAction(mismatchedRoutes, className, "REQUEST_FOCUS", routeArguments, 1, source);
            verifyAccessibleRouteMatchesAction(mismatchedRoutes, className, "SHOW_ITEM", routeArguments, 2, source);
        }

        assertTrue(mismatchedRoutes.isEmpty(),
                () -> "Accessibility route handlers must mirror their executeAccessibleAction entry points: "
                        + mismatchedRoutes);
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

    /// Verifies that user-agent-styled controls are covered by the standalone fallback CSS matrix.
    @Test
    void standaloneFallbackMatrixCoversUserAgentStylesheetControls() throws IOException {
        String controlStyleTestSource = Files.readString(CONTROL_STYLE_TEST_SOURCE);
        Set<String> userAgentControls = userAgentStylesheetMatrixControls(controlStyleTestSource);
        String standaloneFallbackMethod = testMethodBody(
                controlStyleTestSource,
                "standaloneControlStylesheetsResolveFallbackColorTokens"
        );

        List<String> missingControls = new ArrayList<>();
        for (String className : userAgentControls) {
            if (!hasConstructorReference(standaloneFallbackMethod, className)) {
                missingControls.add(className);
            }
        }

        assertTrue(missingControls.isEmpty(),
                () -> "Standalone fallback CSS coverage must instantiate every user-agent-styled public control: "
                        + missingControls);
    }

    /// Verifies that popup-only tooltip controls are covered by standalone fallback CSS tests.
    @Test
    void popupOnlyTooltipControlsHaveStandaloneFallbackCoverage() throws IOException {
        String controlStyleTestSource = Files.readString(CONTROL_STYLE_TEST_SOURCE);
        String standaloneTooltipMethod = testMethodBody(
                controlStyleTestSource,
                "standaloneTooltipPopupsResolveFallbackColorTokens"
        );

        List<String> missingControls = new ArrayList<>();
        for (String className : List.of("M3Tooltip", "M3RichTooltip")) {
            if (!hasConstructorReference(standaloneTooltipMethod, className)) {
                missingControls.add(className);
            }
        }

        assertTrue(missingControls.isEmpty(),
                () -> "Popup-only tooltip controls must stay in standalone fallback CSS coverage: "
                        + missingControls);
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

    /// Verifies that physical right-to-left checks stay centralized in the logical layout helper.
    @Test
    void rightToLeftOrientationChecksUseLogicalLayoutHelper() throws IOException {
        List<String> directChecks = new ArrayList<>();
        for (Path sourceFile : productionJavaSourceFiles()) {
            if (sourceFile.equals(LOGICAL_LAYOUT_HELPER_SOURCE)) {
                continue;
            }

            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                if (DIRECT_RIGHT_TO_LEFT_ORIENTATION_CHECK.matcher(line).find()) {
                    directChecks.add(sourceFile + ":" + (lineIndex + 1) + ": " + line.trim());
                }
            }
        }

        assertTrue(directChecks.isEmpty(),
                () -> "Production sources must use M3NodeLayout for right-to-left orientation checks: "
                        + directChecks);
    }

    /// Returns every public control class covered by the user-agent stylesheet matrix.
    private static @Unmodifiable Set<String> userAgentStylesheetMatrixControls(String controlStyleTestSource) {
        Set<String> classNames = new TreeSet<>();
        Matcher matcher = USER_AGENT_STYLESHEET_ASSERTION.matcher(controlStyleTestSource);
        while (matcher.find()) {
            classNames.add(matcher.group(1));
        }
        assertTrue(!classNames.isEmpty(), "The user-agent stylesheet matrix should cover public controls");
        return Set.copyOf(classNames);
    }

    /// Returns the source body of a test method.
    private static String testMethodBody(String source, String methodName) {
        return sourceMethodBody(source, "void " + methodName + "(");
    }

    /// Returns the body of a source method identified by a stable signature snippet.
    private static String sourceMethodBody(String source, String methodSignature) {
        int methodIndex = source.indexOf(methodSignature);
        assertTrue(methodIndex >= 0, () -> "Missing source method: " + methodSignature);
        int bodyStart = source.indexOf('{', methodIndex);
        assertTrue(bodyStart >= 0, () -> "Missing source method body: " + methodSignature);

        return source.substring(bodyStart + 1, blockEnd(source, bodyStart));
    }

    /// Returns whether source declares an accessibility switch case for the requested action.
    private static boolean declaresAccessibleAction(String source, String actionName) {
        return Pattern.compile(
                "\\bcase\\s+[^\\n]*\\b" + Pattern.quote(actionName) + "\\b\\s*->"
        ).matcher(source).find();
    }

    /// Adds concrete M3FX type references found in a generic accessibility dispatch method body.
    private static void addConcreteM3References(List<String> references, String methodName, String methodBody) {
        Matcher matcher = CONCRETE_M3_DISPATCH_REFERENCE.matcher(methodBody);
        while (matcher.find()) {
            references.add(methodName + ": " + matcher.group());
        }
    }

    /// Verifies one installed accessibility route handler against the matching action branch.
    private static void verifyAccessibleRouteMatchesAction(
            List<String> mismatches,
            String className,
            String actionName,
            List<String> routeArguments,
            int routeArgumentIndex,
            String source
    ) {
        if (routeArgumentIndex >= routeArguments.size()) {
            return;
        }
        String handler = routeArguments.get(routeArgumentIndex).trim();
        if ("null".equals(handler)) {
            return;
        }

        @org.jetbrains.annotations.Nullable String actionBody = accessibleActionCaseBody(source, actionName);
        if (actionBody != null && !routeHandlerMatchesActionBody(handler, actionBody)) {
            mismatches.add(className + " " + actionName + " route=" + handler + " action=" + actionBody.trim());
        }
    }

    /// Returns the body of an accessibility action switch case.
    private static @org.jetbrains.annotations.Nullable String accessibleActionCaseBody(String source, String actionName) {
        Matcher matcher = Pattern.compile(
                "\\bcase\\s+[^\\n]*\\b" + Pattern.quote(actionName) + "\\b[^\\n]*->"
        ).matcher(source);
        if (!matcher.find()) {
            return null;
        }

        int bodyStart = skipWhitespace(source, matcher.end());
        if (bodyStart < source.length() && source.charAt(bodyStart) == '{') {
            return source.substring(bodyStart + 1, blockEnd(source, bodyStart));
        }

        int bodyEnd = statementEnd(source, bodyStart);
        assertTrue(bodyEnd >= 0, () -> "Unclosed accessibility action case: " + actionName);
        return source.substring(bodyStart, bodyEnd);
    }

    /// Returns whether an installed route handler mirrors an accessibility action branch.
    private static boolean routeHandlerMatchesActionBody(String handler, String actionBody) {
        if (handler.startsWith("this::")) {
            String methodName = handler.substring("this::".length()).trim();
            return containsMethodCall(actionBody, methodName);
        }

        int arrowIndex = handler.indexOf("->");
        if (arrowIndex >= 0) {
            String expression = handler.substring(arrowIndex + "->".length()).trim();
            if (actionBody.contains(expression)) {
                return true;
            }
            if (expression.contains("null") && !actionBody.contains("null")) {
                return false;
            }
            List<String> methodNames = methodCalls(expression);
            return !methodNames.isEmpty()
                    && methodNames.stream().allMatch(methodName -> containsMethodCall(actionBody, methodName));
        }

        return actionBody.contains(handler);
    }

    /// Returns top-level whitespace skip position.
    private static int skipWhitespace(String source, int startIndex) {
        int index = startIndex;
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        return index;
    }

    /// Returns the closing brace index for a block that starts with `{`.
    private static int blockEnd(String source, int blockStart) {
        return matchingDelimiterEnd(source, blockStart, '{', '}');
    }

    /// Returns the end index of a Java statement that starts at `startIndex`.
    private static int statementEnd(String source, int startIndex) {
        int roundDepth = 0;
        int squareDepth = 0;
        int braceDepth = 0;
        for (int index = startIndex; index < source.length(); index++) {
            int skippedIndex = skipLiteralOrComment(source, index);
            if (skippedIndex != index) {
                index = skippedIndex - 1;
                continue;
            }

            char ch = source.charAt(index);
            if (ch == '(') {
                roundDepth++;
            } else if (ch == ')' && roundDepth > 0) {
                roundDepth--;
            } else if (ch == '[') {
                squareDepth++;
            } else if (ch == ']' && squareDepth > 0) {
                squareDepth--;
            } else if (ch == '{') {
                braceDepth++;
            } else if (ch == '}' && braceDepth > 0) {
                braceDepth--;
            } else if (ch == ';' && roundDepth == 0 && squareDepth == 0 && braceDepth == 0) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the matching delimiter index while skipping Java strings, character literals, and comments.
    private static int matchingDelimiterEnd(String source, int startIndex, char opening, char closing) {
        assertTrue(startIndex >= 0 && startIndex < source.length() && source.charAt(startIndex) == opening,
                () -> "Missing opening delimiter: " + opening);
        int depth = 0;
        for (int index = startIndex; index < source.length(); index++) {
            int skippedIndex = skipLiteralOrComment(source, index);
            if (skippedIndex != index) {
                index = skippedIndex - 1;
                continue;
            }

            char ch = source.charAt(index);
            if (ch == opening) {
                depth++;
            } else if (ch == closing) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        throw new AssertionError("Unclosed source delimiter: " + opening);
    }

    /// Returns the index after a Java string, character literal, text block, or comment at `index`.
    private static int skipLiteralOrComment(String source, int index) {
        char ch = source.charAt(index);
        if (ch == '"') {
            if (source.startsWith("\"\"\"", index)) {
                int end = source.indexOf("\"\"\"", index + 3);
                return end < 0 ? source.length() : end + 3;
            }
            return skipQuotedLiteral(source, index, '"');
        }
        if (ch == '\'') {
            return skipQuotedLiteral(source, index, '\'');
        }
        if (ch == '/' && index + 1 < source.length()) {
            char next = source.charAt(index + 1);
            if (next == '/') {
                int end = source.indexOf('\n', index + 2);
                return end < 0 ? source.length() : end + 1;
            }
            if (next == '*') {
                int end = source.indexOf("*/", index + 2);
                return end < 0 ? source.length() : end + 2;
            }
        }
        return index;
    }

    /// Returns the index after a quoted Java literal.
    private static int skipQuotedLiteral(String source, int startIndex, char quote) {
        for (int index = startIndex + 1; index < source.length(); index++) {
            char ch = source.charAt(index);
            if (ch == '\\') {
                index++;
            } else if (ch == quote) {
                return index + 1;
            }
        }
        return source.length();
    }

    /// Returns whether a source body contains a call to a method name.
    private static boolean containsMethodCall(String source, String methodName) {
        return Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(").matcher(source).find();
    }

    /// Returns method names called by an expression.
    private static @Unmodifiable List<String> methodCalls(String expression) {
        List<String> methodNames = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\b([A-Za-z_]\\w*)\\s*\\(").matcher(expression);
        while (matcher.find()) {
            methodNames.add(matcher.group(1));
        }
        return List.copyOf(methodNames);
    }

    /// Returns the first direct `M3Accessible` route installation call in source text.
    private static @org.jetbrains.annotations.Nullable String accessibleActionRouteInstallation(String source) {
        int startIndex = source.indexOf("M3Accessible.installAccessibleActionRoute(");
        if (startIndex < 0) {
            return null;
        }
        int argumentStart = source.indexOf('(', startIndex);
        return source.substring(argumentStart + 1, matchingDelimiterEnd(source, argumentStart, '(', ')'));
    }

    /// Returns whether the route argument at the requested index is a non-null handler.
    private static boolean routeHasHandler(@org.jetbrains.annotations.Nullable String route, int argumentIndex) {
        if (route == null) {
            return false;
        }
        List<String> arguments = topLevelArguments(route);
        return argumentIndex < arguments.size() && !"null".equals(arguments.get(argumentIndex).trim());
    }

    /// Splits a comma-separated argument list without splitting nested expressions or lambdas.
    private static @Unmodifiable List<String> topLevelArguments(String arguments) {
        List<String> result = new ArrayList<>();
        int roundDepth = 0;
        int squareDepth = 0;
        int braceDepth = 0;
        int argumentStart = 0;
        for (int index = 0; index < arguments.length(); index++) {
            int skippedIndex = skipLiteralOrComment(arguments, index);
            if (skippedIndex != index) {
                index = skippedIndex - 1;
                continue;
            }

            char ch = arguments.charAt(index);
            if (ch == '(') {
                roundDepth++;
            } else if (ch == ')' && roundDepth > 0) {
                roundDepth--;
            } else if (ch == '[') {
                squareDepth++;
            } else if (ch == ']' && squareDepth > 0) {
                squareDepth--;
            } else if (ch == '{') {
                braceDepth++;
            } else if (ch == '}' && braceDepth > 0) {
                braceDepth--;
            } else if (ch == ',' && roundDepth == 0 && squareDepth == 0 && braceDepth == 0) {
                result.add(arguments.substring(argumentStart, index));
                argumentStart = index + 1;
            }
        }
        result.add(arguments.substring(argumentStart));
        return List.copyOf(result);
    }

    /// Returns whether source text constructs the requested class.
    private static boolean hasConstructorReference(String source, String className) {
        return Pattern.compile(
                "\\bnew\\s+" + Pattern.quote(className) + "(?:\\s*<[^>]*>)?\\s*\\("
        ).matcher(source).find();
    }

    /// Removes and accepts one exact allowed direct Region layout-property write.
    private static boolean removeAllowedDirectRegionLayoutWrite(
            Map<String, Set<String>> unusedAllowedWrites,
            String sourceName,
            String line
    ) {
        Set<String> allowedLines = unusedAllowedWrites.get(sourceName);
        return allowedLines != null && allowedLines.remove(line);
    }

    /// Returns whether a direct layout write is a registered public-child write with a separate bound guard check.
    private static boolean isBoundGuardedControlLayoutWrite(String sourceName, String line) {
        Map<String, String> guardedWrites = BOUND_GUARDED_CONTROL_LAYOUT_WRITES.get(sourceName);
        return guardedWrites != null && guardedWrites.containsKey(line);
    }

    /// Collects public-child layout writes that are missing their bound-property guards.
    private static void collectUnguardedPublicChildLayoutWrites(
            Path sourceRoot,
            Map<String, @Unmodifiable Map<String, String>> guardedWrites,
            List<String> unguardedWrites
    ) throws IOException {
        for (Map.Entry<String, @Unmodifiable Map<String, String>> sourceEntry : guardedWrites.entrySet()) {
            Path sourceFile = sourceRoot.resolve(sourceEntry.getKey());
            List<String> lines = Files.readAllLines(sourceFile);
            for (Map.Entry<String, String> writeEntry : sourceEntry.getValue().entrySet()) {
                collectUnguardedPublicChildLayoutWrite(
                        sourceFile,
                        lines,
                        writeEntry.getKey(),
                        writeEntry.getValue(),
                        unguardedWrites
                );
            }
        }
    }

    /// Adds a diagnostic when a public-child layout write is missing its bound-property guard.
    private static void collectUnguardedPublicChildLayoutWrite(
            Path sourceFile,
            List<String> lines,
            String writeLine,
            String guardSnippet,
            List<String> unguardedWrites
    ) {
        boolean found = false;
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            if (lines.get(lineIndex).trim().equals(writeLine)) {
                found = true;
                if (!nearbyLinesContain(lines, lineIndex, guardSnippet)) {
                    unguardedWrites.add(sourceFile + ":" + (lineIndex + 1) + ": " + writeLine);
                }
            }
        }

        if (!found) {
            unguardedWrites.add(sourceFile + ": missing write `" + writeLine + "`");
        }
    }

    /// Returns whether a nearby line contains the requested source snippet.
    private static boolean nearbyLinesContain(List<String> lines, int lineIndex, String snippet) {
        int start = Math.max(0, lineIndex - 8);
        for (int index = start; index <= lineIndex; index++) {
            if (lines.get(index).contains(snippet)) {
                return true;
            }
        }
        return false;
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
                if (!startsPublicOrProtectedApiDeclaration(line)) {
                    continue;
                }
                declaration.setLength(0);
                declarationLine = index + 1;
            }

            String code = stripTrailingLineComment(line);
            if (!code.isEmpty() && !code.startsWith("///") && !code.startsWith("*")) {
                if (!declaration.isEmpty()) {
                    declaration.append(' ');
                }
                declaration.append(code);
            }

            if (apiDeclarationEnds(code)) {
                String exportedDeclaration = stripSealedPermitsClause(declaration.toString());
                if (EXPORTED_INTERNAL_API_REFERENCE.matcher(exportedDeclaration).find()) {
                    leaks.add(sourceFile + ":" + declarationLine + ": " + exportedDeclaration);
                }
                declarationLine = -1;
                declaration.setLength(0);
            }
        }
    }

    /// Returns whether a source line begins a public or protected API declaration.
    private static boolean startsPublicOrProtectedApiDeclaration(String line) {
        return line.startsWith("public ") || line.startsWith("protected ");
    }

    /// Returns the source line without a trailing line comment.
    private static String stripTrailingLineComment(String line) {
        int commentIndex = line.indexOf("//");
        return commentIndex >= 0 ? line.substring(0, commentIndex).stripTrailing() : line;
    }

    /// Returns the declaration without a sealed `permits` clause.
    private static String stripSealedPermitsClause(String declaration) {
        int permitsIndex = declaration.indexOf(" permits ");
        return permitsIndex >= 0 ? declaration.substring(0, permitsIndex) : declaration;
    }

    /// Returns whether the currently scanned API declaration has ended.
    private static boolean apiDeclarationEnds(String line) {
        return line.endsWith(";") || line.endsWith("{");
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

    /// Returns whether a reflected member is public or protected.
    private static boolean isPublicOrProtected(Member member) {
        int modifiers = member.getModifiers();
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
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
            List<String> lines = Files.readAllLines(sourceFile);
            @org.jetbrains.annotations.Nullable String className = publicControlClassName(lines);
            if (className == null || !inheritsFromJavaFxControl(className, superclasses)) {
                continue;
            }

            Matcher matcher = STYLE_CLASS_DECLARATION.matcher(Files.readString(sourceFile));
            if (matcher.find()) {
                styleClasses.put(matcher.group(1), inheritsFromJavaFxAlignedControl(className, superclasses));
            }
        }
        return styleClasses;
    }

    /// Returns whether a source-declared control class inherits from a JavaFX control base class.
    private static boolean inheritsFromJavaFxControl(String className, Map<String, String> superclasses) {
        return inheritsFromAnyJavaFxBase(className, superclasses, JAVA_FX_CONTROL_BASE_CLASSES);
    }

    /// Returns whether a source-declared control class inherits a JavaFX alignment CSS property.
    private static boolean inheritsFromJavaFxAlignedControl(String className, Map<String, String> superclasses) {
        return inheritsFromAnyJavaFxBase(className, superclasses, JAVA_FX_ALIGNED_CONTROL_BASE_CLASSES);
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

    /// Returns concrete public controls that inherit from the requested superclass.
    private static Set<String> publicConcreteControlsInheritingFrom(
            String targetSuperclass,
            Map<String, String> superclasses
    ) throws IOException {
        Set<String> concreteClasses = new TreeSet<>();

        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            @org.jetbrains.annotations.Nullable String className = publicConcreteControlClassName(lines);
            if (className != null) {
                concreteClasses.add(className);
            }
        }

        Set<String> classNames = new TreeSet<>();
        for (String className : concreteClasses) {
            if (className.equals(targetSuperclass) || inheritsFromClass(className, superclasses, targetSuperclass)) {
                classNames.add(className);
            }
        }
        return classNames;
    }

    /// Returns direct superclass names for public control source files.
    private static Map<String, String> publicControlSuperclasses() throws IOException {
        Map<String, String> superclasses = new HashMap<>();

        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            @org.jetbrains.annotations.Nullable String className = publicControlClassName(lines);
            @org.jetbrains.annotations.Nullable String superclassName = publicControlSuperclassName(lines);
            if (className == null || superclassName == null) {
                continue;
            }

            superclasses.put(className, superclassName);
        }

        return superclasses;
    }

    /// Returns complete source text for public control source files.
    private static Map<String, String> publicControlSources() throws IOException {
        Map<String, String> sources = new HashMap<>();

        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            @org.jetbrains.annotations.Nullable String className = publicControlClassName(lines);
            if (className != null) {
                sources.put(className, String.join("\n", lines));
            }
        }

        return sources;
    }

    /// Returns the public control class name declared by a source file.
    private static @org.jetbrains.annotations.Nullable String publicControlClassName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = PUBLIC_CONTROL_EXTENDS_DECLARATION.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /// Returns the direct superclass name declared by a public control source file.
    private static @org.jetbrains.annotations.Nullable String publicControlSuperclassName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = PUBLIC_CONTROL_EXTENDS_DECLARATION.matcher(line);
            if (matcher.find()) {
                return matcher.group(2);
            }
        }
        return null;
    }

    /// Returns the concrete public control class name declared by a source file.
    private static @org.jetbrains.annotations.Nullable String publicConcreteControlClassName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = PUBLIC_CONCRETE_CONTROL_EXTENDS_DECLARATION.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /// Returns whether a concrete class inherits from the requested superclass.
    private static boolean inheritsFromClass(
            String className,
            Map<String, String> superclasses,
            String targetSuperclass
    ) {
        Set<String> visitedClasses = new TreeSet<>();
        @org.jetbrains.annotations.Nullable String currentClass = className;
        while (currentClass != null && visitedClasses.add(currentClass)) {
            @org.jetbrains.annotations.Nullable String superclassName = superclasses.get(currentClass);
            if (targetSuperclass.equals(superclassName)) {
                return true;
            }
            currentClass = superclassName;
        }
        return false;
    }

    /// Returns whether a concrete class declares or inherits the requested source snippet.
    private static boolean inheritsSourceSnippet(
            String className,
            Map<String, String> superclasses,
            Map<String, String> sources,
            String sourceSnippet
    ) {
        Set<String> visitedClasses = new TreeSet<>();
        @org.jetbrains.annotations.Nullable String currentClass = className;
        while (currentClass != null && visitedClasses.add(currentClass)) {
            @org.jetbrains.annotations.Nullable String source = sources.get(currentClass);
            if (source != null && source.contains(sourceSnippet)) {
                return true;
            }
            currentClass = superclasses.get(currentClass);
        }
        return false;
    }

    /// Returns a stable slash-separated relative resource path.
    private static String resourceRelativePath(Path root, Path resource) {
        return root.relativize(resource).toString().replace('\\', '/');
    }

    /// Returns whether any line matches a pattern.
    private static boolean hasMatchingLine(List<String> lines, Pattern pattern) {
        for (String line : lines) {
            if (pattern.matcher(line).find()) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether an `isSettable` override delegates through `M3Css.isSettable`.
    private static boolean settableBlockUsesM3Css(List<String> lines, int settableLineIndex) {
        for (int lineIndex = settableLineIndex; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            if (line.contains("M3Css.isSettable(")) {
                return true;
            }
            if (lineIndex > settableLineIndex && line.contains("public StyleableProperty<")) {
                return false;
            }
        }
        return false;
    }

    /// Returns whether a type declaration is preceded by a local `@NotNullByDefault` annotation.
    private static boolean hasLeadingNotNullByDefaultAnnotation(List<String> lines, int declarationLineIndex) {
        for (int lineIndex = declarationLineIndex - 1; lineIndex >= 0; lineIndex--) {
            String trimmed = lines.get(lineIndex).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("///")) {
                continue;
            }
            if (trimmed.startsWith("@")) {
                if (trimmed.contains("@NotNullByDefault")) {
                    return true;
                }
                continue;
            }
            return false;
        }
        return false;
    }
}
