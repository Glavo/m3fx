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
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies source-level documentation invariants for published M3FX APIs.
@NotNullByDefault
final class DocumentationCoverageTest {
    /// The main source directory scanned by Markdown Javadoc style tests.
    private static final Path MAIN_SOURCE_ROOT = Path.of("src", "main", "java");

    /// User-facing API package roots whose public types should link to Material documentation.
    private static final @Unmodifiable List<Path> USER_API_SOURCE_ROOTS = List.of(
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "animation")),
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "controls")),
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "theme")),
            MAIN_SOURCE_ROOT.resolve(Path.of("org", "glavo", "m3fx", "tokens"))
    );

    /// The Material documentation URL prefix expected in public API source files.
    private static final String MATERIAL_DOCUMENTATION_URL = "https://m3.material.io/";

    /// Matches top-level public Java type declarations.
    private static final Pattern PUBLIC_TOP_LEVEL_TYPE = Pattern.compile(
            "(?m)^public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)?"
                    + "(?:class|interface|enum|record)\\s+"
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
