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
}
