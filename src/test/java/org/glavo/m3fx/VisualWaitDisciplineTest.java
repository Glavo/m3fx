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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that visual and animation tests avoid fragile fixed-time waiting primitives.
@NotNullByDefault
final class VisualWaitDisciplineTest {
    /// Test source roots searched for rendered visual tests.
    private static final @Unmodifiable List<Path> TEST_SOURCE_ROOTS = List.of(
            Path.of("src", "test", "java"),
            Path.of("demo", "src", "test", "java")
    );

    /// Shared visual wait helpers that must not reintroduce fixed-time waiting behind visual tests.
    private static final @Unmodifiable List<Path> VISUAL_WAIT_HELPER_SOURCES = List.of(
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "FxTestUtils.java"),
            Path.of("demo", "src", "test", "java", "org", "glavo", "m3fx", "demo", "DemoFxTestUtils.java")
    );

    /// The demo visual smoke test source that owns page-level rendered assertions.
    private static final Path DEMO_VISUAL_TEST_SOURCE = Path.of(
            "demo",
            "src",
            "test",
            "java",
            "org",
            "glavo",
            "m3fx",
            "demo",
            "M3FXDemoVisualSmokeTest.java"
    );

    /// Demo visual helper methods whose component scans should only inspect the current page content.
    private static final @Unmodifiable List<String> PAGE_OWNED_DEMO_VISUAL_HELPERS = List.of(
            "assertVisibleMaterialControlsInsideScene",
            "assertLabeledControlTextInkGeometry",
            "assertListItemHeadlineTextInkGeometry",
            "assertListItemTextSegmentGeometry",
            "assertListItemSlotGeometry",
            "assertFixedTargetGlyphsCentered",
            "assertNavigationItemIconSlotsCentered",
            "assertSingleLineTextInputsHaveVerticalRoom",
            "assertSelectionIndicatorsCentered",
            "assertNavigationBadgesStayCompact"
    );

    /// Root-level scan calls that make page-owned demo visual assertions vulnerable to stale or outer UI nodes.
    private static final @Unmodifiable List<String> PAGE_OWNED_ROOT_SCAN_PATTERNS = List.of(
            "visitVisibleNodes(scene.getRoot()",
            "visibleNodesOfType(scene.getRoot()",
            "visibleNodesWithStyle(scene.getRoot()",
            "assertDemoVectorIcons(scene.getRoot()"
    );

    /// Source markers that identify tests doing rendered-image, pixel, or snapshot verification.
    private static final @Unmodifiable List<String> VISUAL_SOURCE_MARKERS = List.of(
            "WritableImage",
            "snapshot(",
            "snapshotImage",
            "snapshotNode",
            "snapshotPixel",
            "writeVisualSnapshot",
            "assertSnapshot",
            "getPixelReader",
            "renderedTextInkBounds",
            "assertCellTextInkCentered",
            "contrastingPixelBounds"
    );

    /// Fixed-time waiting primitives that should not appear in broad visual tests or wait helpers.
    private static final @Unmodifiable List<String> BANNED_FIXED_WAIT_PATTERNS = List.of(
            "Thread.sleep",
            "TimeUnit.MILLISECONDS.sleep",
            "TimeUnit.SECONDS.sleep",
            "LockSupport.parkNanos",
            "LockSupport.parkUntil",
            "new PauseTransition",
            "new javafx.animation.PauseTransition",
            "waitForFxMillis",
            "sleepForVisual"
    );

    /// Ad-hoc animation wait primitives that should not appear directly in visual tests.
    private static final @Unmodifiable List<String> BANNED_VISUAL_TEST_ANIMATION_WAIT_PATTERNS = List.of(
            "new Timeline",
            "new javafx.animation.Timeline",
            "new KeyFrame",
            "new javafx.animation.KeyFrame",
            "new AnimationTimer",
            "new javafx.animation.AnimationTimer"
    );

    /// Ad-hoc FX-thread scheduling primitives that should not appear directly in visual tests.
    private static final @Unmodifiable List<String> BANNED_VISUAL_TEST_ASYNC_WAIT_PATTERNS = List.of(
            "Platform.runLater",
            "new CountDownLatch",
            "new java.util.concurrent.CountDownLatch",
            ".await("
    );

    /// All wait patterns that visual test sources are not allowed to use directly.
    private static final @Unmodifiable List<String> BANNED_VISUAL_TEST_WAIT_PATTERNS = Stream.of(
            BANNED_FIXED_WAIT_PATTERNS,
            BANNED_VISUAL_TEST_ANIMATION_WAIT_PATTERNS,
            BANNED_VISUAL_TEST_ASYNC_WAIT_PATTERNS
    ).flatMap(List::stream).toList();

    /// Verifies that rendered visual tests wait for semantic state or rendered pixels instead of elapsed time.
    @Test
    void visualTestsDoNotUseFixedTimeWaits() throws IOException {
        Path root = workspaceRoot();
        List<Path> visualTestSources = discoverVisualTestSources(root);
        List<String> violations = new ArrayList<>();
        for (Path relativePath : visualTestSources) {
            assertNoFixedTimeWaits(root.resolve(relativePath), relativePath, BANNED_VISUAL_TEST_WAIT_PATTERNS,
                    violations);
        }

        assertTrue(violations.isEmpty(), () -> "Visual and animation tests must wait for semantic state, "
                + "stable pulses, or rendered pixel changes instead of fixed elapsed time: " + violations);
    }

    /// Verifies that shared FX wait helpers do not hide fixed-time visual waits behind reusable APIs.
    @Test
    void visualWaitHelpersDoNotUseFixedTimeWaits() throws IOException {
        Path root = workspaceRoot();
        List<String> violations = new ArrayList<>();
        for (Path relativePath : VISUAL_WAIT_HELPER_SOURCES) {
            assertTrue(Files.isRegularFile(root.resolve(relativePath)),
                    () -> "Visual wait helper source is missing: " + relativePath);
            assertNoFixedTimeWaits(root.resolve(relativePath), relativePath, BANNED_FIXED_WAIT_PATTERNS, violations);
        }

        assertTrue(violations.isEmpty(), () -> "Visual wait helpers must stay pulse-driven and must not "
                + "hide fixed elapsed-time waits behind reusable utilities: " + violations);
    }

    /// Verifies that page-owned demo visual scans use the current page instead of the whole scene tree.
    @Test
    void demoPageOwnedVisualScansUseCurrentPageRoots() throws IOException {
        Path root = workspaceRoot();
        Path sourceFile = root.resolve(DEMO_VISUAL_TEST_SOURCE);
        assertTrue(Files.isRegularFile(sourceFile), () -> "Demo visual test source is missing: "
                + DEMO_VISUAL_TEST_SOURCE);

        String source = Files.readString(sourceFile);
        List<String> violations = new ArrayList<>();
        for (String helperName : PAGE_OWNED_DEMO_VISUAL_HELPERS) {
            String body = requireMethodBody(source, helperName);
            if (!body.contains("currentDemoPage(scene, pageTitle)")) {
                violations.add(helperName + " does not resolve the current demo page");
            }
            for (String pattern : PAGE_OWNED_ROOT_SCAN_PATTERNS) {
                if (body.contains(pattern)) {
                    violations.add(helperName + " contains root-scoped page scan pattern `" + pattern + "`");
                }
            }
        }

        assertTrue(violations.isEmpty(), () -> "Page-owned demo visual scans must start from the current "
                + "demo page so header, sidebar, popup, and stale page nodes cannot satisfy page assertions: "
                + violations);
    }

    /// Discovers test source files that own rendered visual assertions.
    private static @Unmodifiable List<Path> discoverVisualTestSources(Path root) throws IOException {
        List<Path> result = new ArrayList<>();
        for (Path sourceRoot : TEST_SOURCE_ROOTS) {
            Path absoluteSourceRoot = root.resolve(sourceRoot);
            if (!Files.isDirectory(absoluteSourceRoot)) {
                continue;
            }

            List<Path> javaSources;
            try (Stream<Path> stream = Files.walk(absoluteSourceRoot)) {
                javaSources = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .map(root::relativize)
                        .toList();
            }

            for (Path relativePath : javaSources) {
                String source = Files.readString(root.resolve(relativePath));
                if (isVisualTestSource(relativePath, source)) {
                    result.add(relativePath);
                }
            }
        }
        result.sort(Comparator.comparing(Path::toString));
        return List.copyOf(result);
    }

    /// Returns whether a source file is a rendered visual test.
    private static boolean isVisualTestSource(Path relativePath, String source) {
        if (relativePath.endsWith(Path.of("VisualWaitDisciplineTest.java"))) {
            return false;
        }
        if (!source.contains("@Test")) {
            return false;
        }
        for (String marker : VISUAL_SOURCE_MARKERS) {
            if (source.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    /// Records banned wait usage found in one source file.
    private static void assertNoFixedTimeWaits(
            Path sourceFile,
            Path relativePath,
            List<String> bannedPatterns,
            List<String> violations
    ) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            for (String bannedPattern : bannedPatterns) {
                if (line.contains(bannedPattern)) {
                    violations.add(relativePath + ":" + (index + 1) + " contains " + bannedPattern);
                }
            }
        }
    }

    /// Returns the source body for one method name.
    private static String requireMethodBody(String source, String methodName) {
        int signatureIndex = source.indexOf("private static void " + methodName + "(");
        assertTrue(signatureIndex >= 0, () -> "Source method is missing: " + methodName);
        int openBraceIndex = source.indexOf('{', signatureIndex);
        assertTrue(openBraceIndex >= 0, () -> "Source method has no body: " + methodName);

        int depth = 0;
        for (int index = openBraceIndex; index < source.length(); index++) {
            char value = source.charAt(index);
            if (value == '{') {
                depth++;
            } else if (value == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(openBraceIndex, index + 1);
                }
            }
        }
        throw new AssertionError("Source method body is not closed: " + methodName);
    }

    /// Returns the repository root for source-file checks.
    private static Path workspaceRoot() {
        @org.jetbrains.annotations.Nullable Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate settings.gradle.kts from the test working directory");
    }
}
