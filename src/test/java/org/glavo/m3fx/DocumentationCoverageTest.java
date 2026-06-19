// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies source-level documentation invariants for published M3FX APIs.
@NotNullByDefault
final class DocumentationCoverageTest {
    /// The main source directory scanned by Markdown Javadoc style tests.
    private static final Path MAIN_SOURCE_ROOT = Path.of("src", "main", "java");

    /// The controls source directory scanned by component documentation contract tests.
    private static final Path CONTROLS_SOURCE_ROOT =
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "controls"));

    /// The root Gradle build script that configures published Javadocs.
    private static final Path ROOT_BUILD_SCRIPT = Path.of("build.gradle.kts");

    /// The module descriptor whose exported packages define the published JPMS surface.
    private static final Path MODULE_INFO = MAIN_SOURCE_ROOT.resolve("module-info.java");

    /// User-facing API package roots whose public types should link to Material documentation.
    private static final @Unmodifiable List<Path> USER_API_SOURCE_ROOTS = List.of(
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "animation")),
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "controls")),
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "theme")),
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "tokens"))
    );

    /// The Material documentation URL prefix expected in public API source files.
    private static final String MATERIAL_DOCUMENTATION_URL = "https://m3.material.io/";

    /// The Material component documentation URL prefix expected in component API source files.
    private static final String MATERIAL_COMPONENT_DOCUMENTATION_URL = MATERIAL_DOCUMENTATION_URL + "components/";

    /// Official Material component documentation URLs required by concrete component API Javadocs.
    private static final @Unmodifiable Map<String, @Unmodifiable List<String>>
            PUBLIC_COMPONENT_MATERIAL_DOCUMENTATION_URLS = Map.ofEntries(
                    Map.entry("M3Badge", List.of(componentUrl("badges"))),
                    Map.entry("M3BadgedBox", List.of(componentUrl("badges"))),
                    Map.entry("M3BottomAppBar", List.of(componentUrl("bottom-app-bar"))),
                    Map.entry("M3BottomAppBarFloatingActionAlignment", List.of(componentUrl("bottom-app-bar"))),
                    Map.entry("M3BottomSheet", List.of(componentUrl("bottom-sheets"))),
                    Map.entry("M3Button", List.of(componentUrl("buttons"))),
                    Map.entry("M3ButtonGroup", List.of(componentUrl("button-groups"))),
                    Map.entry("M3ButtonVariant", List.of(componentUrl("buttons"))),
                    Map.entry("M3Card", List.of(componentUrl("cards"))),
                    Map.entry("M3CardVariant", List.of(componentUrl("cards"))),
                    Map.entry("M3Carousel", List.of(componentUrl("carousel"))),
                    Map.entry("M3CheckBox", List.of(componentUrl("checkbox"))),
                    Map.entry("M3Chip", List.of(componentUrl("chips"))),
                    Map.entry("M3ChipGroup", List.of(componentUrl("chips"))),
                    Map.entry("M3ChipSelectionMode", List.of(componentUrl("chips"))),
                    Map.entry("M3ChipVariant", List.of(componentUrl("chips"))),
                    Map.entry("M3DatePicker", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DatePickerDialog", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DatePickerField", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DatePreset", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DatePresets", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DateRange", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DateRangePicker", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DateRangePickerDialog", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DateRangePickerField", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DateRangePreset", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3DateRangePresets", List.of(componentUrl("date-pickers"))),
                    Map.entry("M3Dialog", List.of(componentUrl("dialogs"))),
                    Map.entry("M3DialogPane", List.of(componentUrl("dialogs"))),
                    Map.entry("M3DisclosureIcon", List.of(componentUrl("navigation-drawer"))),
                    Map.entry("M3Divider", List.of(componentUrl("divider"))),
                    Map.entry("M3FabMenu", List.of(componentUrl("fab-menu"))),
                    Map.entry("M3FloatingActionButton", List.of(componentUrl("floating-action-button"))),
                    Map.entry("M3FloatingActionButtonSize", List.of(componentUrl("floating-action-button"))),
                    Map.entry("M3FloatingActionButtonVariant", List.of(componentUrl("floating-action-button"))),
                    Map.entry("M3FormPane", List.of(componentUrl("text-fields"))),
                    Map.entry("M3FormRow", List.of(componentUrl("text-fields"))),
                    Map.entry("M3FormValidator", List.of(componentUrl("text-fields"))),
                    Map.entry("M3IconButton", List.of(componentUrl("icon-buttons"))),
                    Map.entry("M3IconToggleButton", List.of(componentUrl("icon-buttons"))),
                    Map.entry("M3IconToggleButtonGroup", List.of(componentUrl("icon-buttons"))),
                    Map.entry("M3IconToggleButtonSelectionMode", List.of(componentUrl("icon-buttons"))),
                    Map.entry("M3IconToggleButtonVariant", List.of(componentUrl("icon-buttons"))),
                    Map.entry("M3ListItem", List.of(componentUrl("lists"))),
                    Map.entry("M3ListItemLineCount", List.of(componentUrl("lists"))),
                    Map.entry("M3ListItemSlotSize", List.of(componentUrl("lists"))),
                    Map.entry("M3ListPane", List.of(componentUrl("lists"))),
                    Map.entry("M3ListSectionHeader", List.of(componentUrl("lists"))),
                    Map.entry("M3ListSelectionMode", List.of(componentUrl("lists"))),
                    Map.entry("M3ListView", List.of(componentUrl("lists"))),
                    Map.entry("M3ListViewCell", List.of(componentUrl("lists"))),
                    Map.entry("M3LoadingIndicator", List.of(componentUrl("loading-indicator"))),
                    Map.entry("M3LoadingIndicatorVariant", List.of(componentUrl("loading-indicator"))),
                    Map.entry("M3Menu", List.of(componentUrl("menus"))),
                    Map.entry("M3MenuButton", List.of(componentUrl("menus"))),
                    Map.entry("M3MenuItem", List.of(componentUrl("menus"))),
                    Map.entry("M3MenuSectionHeader", List.of(componentUrl("menus"))),
                    Map.entry("M3MenuSelectionMode", List.of(componentUrl("menus"))),
                    Map.entry("M3SubMenuItem", List.of(componentUrl("menus"))),
                    Map.entry("M3NavigationBar", List.of(componentUrl("navigation-bar"))),
                    Map.entry("M3NavigationDrawer", List.of(componentUrl("navigation-drawer"))),
                    Map.entry("M3NavigationDrawerGroup", List.of(componentUrl("navigation-drawer"))),
                    Map.entry("M3NavigationItem", List.of(componentUrl("navigation-bar"))),
                    Map.entry("M3NavigationRail", List.of(componentUrl("navigation-rail"))),
                    Map.entry("M3PasswordField", List.of(componentUrl("text-fields"))),
                    Map.entry(
                            "M3PickerField",
                            List.of(
                                    componentUrl("date-pickers"),
                                    componentUrl("time-pickers"),
                                    componentUrl("text-fields"))
                    ),
                    Map.entry("M3ProgressBar", List.of(componentUrl("progress-indicators"))),
                    Map.entry("M3ProgressIndicator", List.of(componentUrl("progress-indicators"))),
                    Map.entry("M3RadioButton", List.of(componentUrl("radio-button"))),
                    Map.entry("M3RichTooltip", List.of(componentUrl("tooltips"))),
                    Map.entry("M3SearchBar", List.of(componentUrl("search"))),
                    Map.entry("M3SearchView", List.of(componentUrl("search"))),
                    Map.entry("M3SegmentedButton", List.of(componentUrl("segmented-buttons"))),
                    Map.entry("M3SegmentedButtonGroup", List.of(componentUrl("segmented-buttons"))),
                    Map.entry("M3SegmentedButtonSelectionMode", List.of(componentUrl("segmented-buttons"))),
                    Map.entry("M3SheetVariant", List.of(componentUrl("bottom-sheets"), componentUrl("side-sheets"))),
                    Map.entry("M3SideSheet", List.of(componentUrl("side-sheets"))),
                    Map.entry("M3Slider", List.of(componentUrl("sliders"))),
                    Map.entry("M3Snackbar", List.of(componentUrl("snackbar"))),
                    Map.entry("M3SnackbarHost", List.of(componentUrl("snackbar"))),
                    Map.entry("M3SplitButton", List.of(componentUrl("split-button"))),
                    Map.entry("M3Switch", List.of(componentUrl("switch"))),
                    Map.entry("M3Tab", List.of(componentUrl("tabs"))),
                    Map.entry("M3TabBar", List.of(componentUrl("tabs"))),
                    Map.entry("M3TextArea", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TextField", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TextInput", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TextInputLayout", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TextInputValidator", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TextInputValidators", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TextInputVariant", List.of(componentUrl("text-fields"))),
                    Map.entry("M3TimePicker", List.of(componentUrl("time-pickers"))),
                    Map.entry("M3TimePickerDialog", List.of(componentUrl("time-pickers"))),
                    Map.entry("M3TimePickerField", List.of(componentUrl("time-pickers"))),
                    Map.entry("M3TimePreset", List.of(componentUrl("time-pickers"))),
                    Map.entry("M3TimePresets", List.of(componentUrl("time-pickers"))),
                    Map.entry("M3Toolbar", List.of(componentUrl("toolbars"))),
                    Map.entry("M3ToolbarVariant", List.of(componentUrl("toolbars"))),
                    Map.entry("M3Tooltip", List.of(componentUrl("tooltips"))),
                    Map.entry("M3TopAppBar", List.of(componentUrl("app-bars"))),
                    Map.entry("M3TopAppBarVariant", List.of(componentUrl("app-bars"))),
                    Map.entry("M3ValidationSummary", List.of(componentUrl("text-fields")))
            );

    /// External API documentation roots expected in the generated Javadoc configuration.
    private static final @Unmodifiable List<String> JAVADOC_EXTERNAL_LINKS = List.of(
            "https://docs.oracle.com/en/java/javase/17/docs/api/",
            "https://openjfx.io/javadoc/$javafxVersion/",
            "https://javadoc.io/doc/org.glavo/MonetFX/0.4.0/"
    );

    /// Matches top-level public Java type declarations.
    private static final Pattern PUBLIC_TOP_LEVEL_TYPE = Pattern.compile(
            "(?m)^public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "(?:class|interface|enum|record)\\s+(\\w+)\\b"
    );

    /// Matches public or protected source declarations that belong to the public API surface.
    private static final Pattern PUBLIC_OR_PROTECTED_DECLARATION = Pattern.compile(
            "^(?:public|protected)\\b.*"
    );

    /// Matches Java type declarations that can introduce a nested public API scope.
    private static final Pattern TYPE_DECLARATION = Pattern.compile(
            "^(?:(?:public|protected|private)\\s+)?"
                    + "(?:(?:static|final|abstract|sealed|non-sealed|strictfp)\\s+)*"
                    + "(?:class|interface|enum|record)\\s+"
    );

    /// Matches Java interface declarations.
    private static final Pattern INTERFACE_DECLARATION = Pattern.compile(
            "^(?:(?:public|protected|private)\\s+)?"
                    + "(?:(?:static|sealed|non-sealed|strictfp)\\s+)*"
                    + "interface\\s+"
    );

    /// Matches declarations that are private implementation details even inside interfaces.
    private static final Pattern PRIVATE_DECLARATION = Pattern.compile(
            "^private\\b.*"
    );

    /// Matches exported package declarations in the module descriptor.
    private static final Pattern EXPORTED_PACKAGE = Pattern.compile(
            "(?m)^\\s*exports\\s+([\\w.]+)\\s*;"
    );

    /// Verifies that public source files point readers to Material Design guidance.
    @Test
    void publicApiSourceFilesReferenceMaterialDocumentation() throws IOException {
        List<Path> missingLinks = new ArrayList<>();
        for (Path sourceFile : userApiJavaSourceFiles()) {
            String source = Files.readString(sourceFile);
            if (PUBLIC_TOP_LEVEL_TYPE.matcher(source).find() && !source.contains(MATERIAL_DOCUMENTATION_URL)) {
                missingLinks.add(sourceFile);
            }
        }

        assertTrue(missingLinks.isEmpty(), () -> "Public API source files missing Material documentation links: "
                + relativePaths(missingLinks));
    }

    /// Verifies that each public top-level API type links readers to Material Design guidance.
    @Test
    void publicTopLevelTypeJavadocsReferenceMaterialDocumentation() throws IOException {
        List<String> missingLinks = new ArrayList<>();
        for (Path sourceFile : userApiJavaSourceFiles()) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String trimmed = lines.get(lineIndex).trim();
                if (PUBLIC_TOP_LEVEL_TYPE.matcher(trimmed).find()
                        && !leadingMarkdownJavadocBlock(lines, lineIndex).contains(MATERIAL_DOCUMENTATION_URL)) {
                    missingLinks.add(sourceFile + ":" + (lineIndex + 1) + ": " + trimmed);
                }
            }
        }

        assertTrue(missingLinks.isEmpty(),
                () -> "Public top-level API type Javadocs missing Material documentation links: " + missingLinks);
    }

    /// Verifies that component API type Javadocs link to their exact Material component pages.
    @Test
    void publicComponentTypeJavadocsReferenceSpecificMaterialDocumentation() throws IOException {
        List<String> missingLinks = new ArrayList<>();
        Set<String> missingTypes = new TreeSet<>(PUBLIC_COMPONENT_MATERIAL_DOCUMENTATION_URLS.keySet());
        for (Path sourceFile : userApiJavaSourceFiles()) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String trimmed = lines.get(lineIndex).trim();
                Matcher matcher = PUBLIC_TOP_LEVEL_TYPE.matcher(trimmed);
                if (!matcher.find()) {
                    continue;
                }

                String typeName = matcher.group(1);
                @org.jetbrains.annotations.Nullable List<String> expectedUrls =
                        PUBLIC_COMPONENT_MATERIAL_DOCUMENTATION_URLS.get(typeName);
                if (expectedUrls == null) {
                    continue;
                }

                missingTypes.remove(typeName);
                String javadoc = leadingMarkdownJavadocBlock(lines, lineIndex);
                for (String expectedUrl : expectedUrls) {
                    if (!javadoc.contains(expectedUrl)) {
                        missingLinks.add(sourceFile + ":" + (lineIndex + 1)
                                + ": " + typeName + " missing " + expectedUrl);
                    }
                }
            }
        }

        assertTrue(missingTypes.isEmpty(),
                () -> "Component documentation URL contract references missing public API types: " + missingTypes);
        assertTrue(missingLinks.isEmpty(),
                () -> "Component API type Javadocs missing exact Material component documentation links: "
                        + missingLinks);
    }

    /// Verifies that component-linked public control Javadocs stay covered by the exact URL contract.
    @Test
    void publicComponentTypeJavadocsStayCoveredBySpecificDocumentationContract() throws IOException {
        List<String> missingContracts = new ArrayList<>();
        for (Path sourceFile : javaSourceFiles(CONTROLS_SOURCE_ROOT)) {
            List<String> lines = Files.readAllLines(sourceFile);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String trimmed = lines.get(lineIndex).trim();
                Matcher matcher = PUBLIC_TOP_LEVEL_TYPE.matcher(trimmed);
                if (!matcher.find()) {
                    continue;
                }

                String typeName = matcher.group(1);
                String javadoc = leadingMarkdownJavadocBlock(lines, lineIndex);
                if (javadoc.contains(MATERIAL_COMPONENT_DOCUMENTATION_URL)
                        && !PUBLIC_COMPONENT_MATERIAL_DOCUMENTATION_URLS.containsKey(typeName)) {
                    missingContracts.add(sourceFile + ":" + (lineIndex + 1) + ": " + typeName);
                }
            }
        }

        assertTrue(missingContracts.isEmpty(),
                () -> "Public control types with Material component links must be covered by "
                        + "PUBLIC_COMPONENT_MATERIAL_DOCUMENTATION_URLS: " + missingContracts);
    }

    /// Verifies that Markdown Javadocs use Markdown links instead of inline Javadoc link tags.
    @Test
    void markdownJavadocsDoNotUseInlineLinkTags() throws IOException {
        List<Path> filesWithInlineLinks = new ArrayList<>();
        for (Path sourceFile : mainJavaSourceFiles()) {
            String source = Files.readString(sourceFile);
            if (source.contains("{@link") || source.contains("@link ")) {
                filesWithInlineLinks.add(sourceFile);
            }
        }

        assertTrue(filesWithInlineLinks.isEmpty(), () -> "Markdown Javadocs must use [] links instead of @link: "
                + relativePaths(filesWithInlineLinks));
    }

    /// Verifies that generated Javadocs link to the external API surfaces used by public M3FX APIs.
    @Test
    void generatedJavadocsLinkExternalApiDocumentation() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);
        List<String> missingLinks = JAVADOC_EXTERNAL_LINKS.stream()
                .filter(link -> !buildScript.contains(link))
                .toList();

        assertTrue(missingLinks.isEmpty(), () -> "Generated Javadocs missing external API links: " + missingLinks);
    }

    /// Verifies that every exported package has package-level Markdown Javadoc.
    @Test
    void exportedPackagesHaveDocumentedPackageInfo() throws IOException {
        List<String> packageInfoProblems = new ArrayList<>();
        for (String packageName : exportedPackages()) {
            Path packageInfo = packageInfoPath(packageName);
            if (!Files.isRegularFile(packageInfo)) {
                packageInfoProblems.add(packageName + ": missing package-info.java");
                continue;
            }

            String source = Files.readString(packageInfo);
            if (!source.contains("///")) {
                packageInfoProblems.add(packageName + ": package-info.java missing Markdown Javadoc");
            }
            if (!source.contains("@NotNullByDefault")) {
                packageInfoProblems.add(packageName + ": package-info.java missing @NotNullByDefault");
            }
            if (!source.contains(MATERIAL_DOCUMENTATION_URL)) {
                packageInfoProblems.add(packageName + ": package-info.java missing Material documentation link");
            }
            if (!source.contains("package " + packageName + ";")) {
                packageInfoProblems.add(packageName + ": package-info.java package declaration does not match export");
            }
        }

        assertTrue(packageInfoProblems.isEmpty(),
                () -> "Exported packages must have documented package-info.java files: " + packageInfoProblems);
    }

    /// Verifies that every public API declaration is preceded by Markdown Javadoc.
    @Test
    void publicApiDeclarationsUseMarkdownJavadocs() throws IOException {
        List<String> missingJavadocs = new ArrayList<>();
        for (Path sourceFile : userApiJavaSourceFiles()) {
            List<String> lines = Files.readAllLines(sourceFile);
            List<Integer> typeBodyDepths = new ArrayList<>();
            List<Boolean> apiTypeScopes = new ArrayList<>();
            List<Boolean> interfaceTypeScopes = new ArrayList<>();
            int braceDepth = 0;
            boolean hasPendingType = false;
            boolean pendingApiType = false;
            boolean pendingInterfaceType = false;
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                String trimmed = line.trim();

                while (!typeBodyDepths.isEmpty() && braceDepth < lastInt(typeBodyDepths)) {
                    removeLast(typeBodyDepths);
                    removeLast(apiTypeScopes);
                    removeLast(interfaceTypeScopes);
                }

                boolean inTypeBody = !typeBodyDepths.isEmpty() && braceDepth == lastInt(typeBodyDepths);
                boolean inApiTypeBody = inTypeBody && lastBoolean(apiTypeScopes);
                boolean inApiInterfaceBody = inApiTypeBody && lastBoolean(interfaceTypeScopes);
                if (isPublicApiDeclaration(braceDepth, trimmed, inApiTypeBody, inApiInterfaceBody)
                        && !hasLeadingMarkdownJavadoc(lines, lineIndex)) {
                    missingJavadocs.add(sourceFile + ":" + (lineIndex + 1) + ": " + trimmed);
                }

                boolean declaresType = isTypeDeclaration(trimmed);
                boolean declaresInterface = declaresType && isInterfaceDeclaration(trimmed);
                boolean declaresApiType = declaresType
                        && (isVisibleApiDeclaration(trimmed)
                        || inApiInterfaceBody && !PRIVATE_DECLARATION.matcher(trimmed).matches());
                boolean hasOpeningBrace = containsSourceOpeningBrace(line);
                if (declaresType) {
                    hasPendingType = !hasOpeningBrace;
                    pendingApiType = declaresApiType;
                    pendingInterfaceType = declaresInterface;
                } else if (hasPendingType && hasOpeningBrace) {
                    declaresType = true;
                    declaresApiType = pendingApiType;
                    declaresInterface = pendingInterfaceType;
                    hasPendingType = false;
                }

                int bodyDepth = braceDepth + 1;
                braceDepth += braceDelta(line);
                if (declaresType && hasOpeningBrace && braceDepth >= bodyDepth) {
                    typeBodyDepths.add(bodyDepth);
                    apiTypeScopes.add(declaresApiType);
                    interfaceTypeScopes.add(declaresInterface);
                }
            }
        }

        assertTrue(missingJavadocs.isEmpty(), () -> "Public API declarations missing Markdown Javadocs: "
                + missingJavadocs);
    }

    /// Returns all user-facing API Java source files in a stable order.
    private static @Unmodifiable List<Path> userApiJavaSourceFiles() throws IOException {
        List<Path> sourceFiles = new ArrayList<>();
        for (Path sourceRoot : USER_API_SOURCE_ROOTS) {
            sourceFiles.addAll(javaSourceFiles(sourceRoot));
        }
        sourceFiles.sort(Comparator.comparing(Path::toString));
        return List.copyOf(sourceFiles);
    }

    /// Returns all main Java source files in a stable order.
    private static @Unmodifiable List<Path> mainJavaSourceFiles() throws IOException {
        return javaSourceFiles(MAIN_SOURCE_ROOT);
    }

    /// Returns exported JPMS package names in a stable order.
    private static @Unmodifiable List<String> exportedPackages() throws IOException {
        List<String> packages = new ArrayList<>();
        Matcher matcher = EXPORTED_PACKAGE.matcher(Files.readString(MODULE_INFO));
        while (matcher.find()) {
            packages.add(matcher.group(1));
        }
        packages.sort(String::compareTo);
        return List.copyOf(packages);
    }

    /// Returns the `package-info.java` source path for an exported package.
    private static Path packageInfoPath(String packageName) {
        return MAIN_SOURCE_ROOT.resolve(packageName.replace('.', '/')).resolve("package-info.java");
    }

    /// Returns all Java source files below a source root in a stable order.
    private static @Unmodifiable List<Path> javaSourceFiles(Path sourceRoot) throws IOException {
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    /// Returns source paths relative to the project root for assertion messages.
    private static @Unmodifiable List<String> relativePaths(List<Path> paths) {
        return paths.stream()
                .map(Path::toString)
                .toList();
    }

    /// Returns a Material Design component documentation overview URL.
    private static String componentUrl(String slug) {
        return MATERIAL_DOCUMENTATION_URL + "components/" + slug + "/overview";
    }

    /// Returns whether a source line declares a public API type or member.
    private static boolean isPublicApiDeclaration(
            int braceDepth,
            String trimmedLine,
            boolean inApiTypeBody,
            boolean inApiInterfaceBody
    ) {
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("//")) {
            return false;
        }
        if (braceDepth == 0) {
            return isVisibleApiDeclaration(trimmedLine);
        }
        return inApiTypeBody
                && (isVisibleApiDeclaration(trimmedLine)
                || inApiInterfaceBody && isImplicitPublicInterfaceMember(trimmedLine));
    }

    /// Returns whether a line starts a Java type declaration.
    private static boolean isTypeDeclaration(String trimmedLine) {
        return TYPE_DECLARATION.matcher(trimmedLine).find();
    }

    /// Returns whether a line starts a Java interface declaration.
    private static boolean isInterfaceDeclaration(String trimmedLine) {
        return INTERFACE_DECLARATION.matcher(trimmedLine).find();
    }

    /// Returns whether a declaration has explicit public or protected visibility.
    private static boolean isVisibleApiDeclaration(String trimmedLine) {
        return PUBLIC_OR_PROTECTED_DECLARATION.matcher(trimmedLine).matches();
    }

    /// Returns whether a declaration is implicitly public because it belongs to a public interface body.
    private static boolean isImplicitPublicInterfaceMember(String trimmedLine) {
        if (trimmedLine.startsWith("@")
                || trimmedLine.startsWith("}")
                || PRIVATE_DECLARATION.matcher(trimmedLine).matches()) {
            return false;
        }
        return isTypeDeclaration(trimmedLine)
                || trimmedLine.endsWith(";")
                || trimmedLine.contains("(");
    }

    /// Returns whether a declaration line has a Markdown Javadoc block immediately above its annotations.
    private static boolean hasLeadingMarkdownJavadoc(List<String> lines, int declarationLineIndex) {
        for (int lineIndex = declarationLineIndex - 1; lineIndex >= 0; lineIndex--) {
            String trimmed = lines.get(lineIndex).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("@")) {
                continue;
            }
            return trimmed.startsWith("///");
        }
        return false;
    }

    /// Returns the Markdown Javadoc block immediately above a declaration and its annotations.
    private static String leadingMarkdownJavadocBlock(List<String> lines, int declarationLineIndex) {
        StringBuilder builder = new StringBuilder();
        for (int lineIndex = declarationLineIndex - 1; lineIndex >= 0; lineIndex--) {
            String trimmed = lines.get(lineIndex).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("@")) {
                continue;
            }
            if (!trimmed.startsWith("///")) {
                break;
            }

            builder.insert(0, trimmed).insert(0, '\n');
        }
        return builder.toString();
    }

    /// Returns whether a source line contains an opening brace outside string literals and line comments.
    private static boolean containsSourceOpeningBrace(String line) {
        boolean inString = false;
        boolean inCharacter = false;
        boolean escaped = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            char next = index + 1 < line.length() ? line.charAt(index + 1) : '\0';
            if (!inString && !inCharacter && character == '/' && next == '/') {
                return false;
            }
            if (escaped) {
                escaped = false;
                continue;
            }
            if ((inString || inCharacter) && character == '\\') {
                escaped = true;
                continue;
            }
            if (!inCharacter && character == '"') {
                inString = !inString;
                continue;
            }
            if (!inString && character == '\'') {
                inCharacter = !inCharacter;
                continue;
            }
            if (!inString && !inCharacter && character == '{') {
                return true;
            }
        }
        return false;
    }

    /// Counts opening and closing braces in one source line, ignoring string literals and line comments.
    private static int braceDelta(String line) {
        int delta = 0;
        boolean inString = false;
        boolean inCharacter = false;
        boolean escaped = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            char next = index + 1 < line.length() ? line.charAt(index + 1) : '\0';
            if (!inString && !inCharacter && character == '/' && next == '/') {
                break;
            }
            if (escaped) {
                escaped = false;
                continue;
            }
            if ((inString || inCharacter) && character == '\\') {
                escaped = true;
                continue;
            }
            if (!inCharacter && character == '"') {
                inString = !inString;
                continue;
            }
            if (!inString && character == '\'') {
                inCharacter = !inCharacter;
                continue;
            }
            if (!inString && !inCharacter) {
                if (character == '{') {
                    delta++;
                } else if (character == '}') {
                    delta--;
                }
            }
        }
        return delta;
    }

    /// Returns the final element from an integer list.
    private static int lastInt(List<Integer> values) {
        return values.get(values.size() - 1);
    }

    /// Returns the final element from a boolean list.
    private static boolean lastBoolean(List<Boolean> values) {
        return values.get(values.size() - 1);
    }

    /// Removes the final element from a list.
    private static void removeLast(List<?> values) {
        values.remove(values.size() - 1);
    }
}
