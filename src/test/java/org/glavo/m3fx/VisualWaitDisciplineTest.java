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
    /// Test source roots searched for visual regression tests.
    private static final @Unmodifiable List<Path> TEST_SOURCE_ROOTS = List.of(
            Path.of("src", "test", "java"),
            Path.of("demo", "src", "test", "java")
    );

    /// Visual regression sources that must remain covered by the automatic source discovery.
    private static final @Unmodifiable List<Path> REQUIRED_VISUAL_TEST_SOURCES = List.of(
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "controls", "M3ControlStyleTest.java"),
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "controls", "M3DatePickerTest.java"),
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "controls", "M3DateRangePickerTest.java"),
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "controls", "M3TimePickerTest.java"),
            Path.of("demo", "src", "test", "java", "org", "glavo", "m3fx", "demo", "M3FXDemoVisualSmokeTest.java")
    );

    /// Demo visual smoke test source that owns full-page rendered sweeps.
    private static final Path DEMO_VISUAL_SMOKE_TEST_SOURCE =
            Path.of("demo", "src", "test", "java", "org", "glavo", "m3fx", "demo", "M3FXDemoVisualSmokeTest.java");

    /// Full-page demo visual sweep methods that must run page-specific rendered-state checks.
    private static final @Unmodifiable List<String> DEMO_FULL_PAGE_VISUAL_SWEEP_METHODS = List.of(
            "allDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets",
            "darkExpressiveDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets",
            "rightToLeftDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets"
    );

    /// Required source markers for every full-page demo visual sweep.
    private static final @Unmodifiable List<String> DEMO_FULL_PAGE_VISUAL_SWEEP_MARKERS = List.of(
            "showPageWhenSidebarSelectionSettled(",
            "assertCurrentPageTitle(scene, pageTitle)",
            "assertSidebarSelectionMatchesCurrentPage(app, pageTitle)",
            "writeVisualSnapshot(image, Path.of(",
            "assertSnapshotHasVisibleContent(image, pageTitle)",
            "assertDemoPageVisualGeometry(scene, pageTitle)",
            "assertDemoPageSpecificVisualState(scene, pageTitle)"
    );

    /// Shared visual wait helpers that must not reintroduce fixed-time waiting behind visual tests.
    private static final @Unmodifiable List<Path> VISUAL_WAIT_HELPER_SOURCES = List.of(
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "FxTestUtils.java"),
            Path.of("demo", "src", "test", "java", "org", "glavo", "m3fx", "demo", "DemoFxTestUtils.java")
    );

    /// Test sources that verify visual wait helper timeout diagnostics.
    private static final @Unmodifiable List<Path> VISUAL_WAIT_HELPER_TEST_SOURCES = List.of(
            Path.of("src", "test", "java", "org", "glavo", "m3fx", "FxTestUtilsTest.java"),
            Path.of("demo", "src", "test", "java", "org", "glavo", "m3fx", "demo", "DemoFxTestUtilsTest.java")
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

    /// Fixed-time waiting primitives that should not appear in broad visual regression tests or wait helpers.
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

    /// Ad-hoc animation wait primitives that should not appear directly in visual regression tests.
    private static final @Unmodifiable List<String> BANNED_VISUAL_TEST_ANIMATION_WAIT_PATTERNS = List.of(
            "new Timeline",
            "new javafx.animation.Timeline",
            "new KeyFrame",
            "new javafx.animation.KeyFrame",
            "new AnimationTimer",
            "new javafx.animation.AnimationTimer"
    );

    /// Ad-hoc FX-thread scheduling primitives that should not appear directly in visual regression tests.
    private static final @Unmodifiable List<String> BANNED_VISUAL_TEST_ASYNC_WAIT_PATTERNS = List.of(
            "Platform.runLater",
            "new CountDownLatch",
            "new java.util.concurrent.CountDownLatch",
            ".await("
    );

    /// All wait patterns that visual test sources are not allowed to use directly.
    private static final @Unmodifiable List<String> BANNED_VISUAL_TEST_WAIT_PATTERNS =
            combinedWaitPatterns(
                    combinedWaitPatterns(BANNED_FIXED_WAIT_PATTERNS, BANNED_VISUAL_TEST_ANIMATION_WAIT_PATTERNS),
                    BANNED_VISUAL_TEST_ASYNC_WAIT_PATTERNS
            );

    /// The maximum number of lines read to validate one visual snapshot report path.
    private static final int SNAPSHOT_WRITE_CALL_MAX_LINES = 12;

    /// Source markers that keep demo visual snapshots easy to review after the test run.
    private static final @Unmodifiable List<String> VISUAL_SNAPSHOT_INDEX_MARKERS = List.of(
            "writeVisualSnapshotIndex(path)",
            "Files.list(directory)",
            "directory.resolve(\"index.html\")",
            "M3FX Demo Visual Snapshots",
            "escapeHtml(fileName)"
    );

    /// Verifies that broad visual tests wait for semantic state or rendered pixels instead of elapsed time.
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

    /// Verifies that automatic source discovery keeps the known visual regression suites under discipline checks.
    @Test
    void visualTestDiscoveryCoversKnownVisualSources() throws IOException {
        List<Path> visualTestSources = discoverVisualTestSources(workspaceRoot());

        for (Path requiredSource : REQUIRED_VISUAL_TEST_SOURCES) {
            assertTrue(visualTestSources.contains(requiredSource),
                    () -> "Visual wait discipline discovery missed " + requiredSource
                            + "; update markers or the source classification");
        }
    }

    /// Verifies that known visual test suites keep writing reviewable PNG report artifacts.
    @Test
    void knownVisualSuitesWriteReviewableSnapshots() throws IOException {
        Path root = workspaceRoot();
        List<String> missingSnapshotReports = new ArrayList<>();
        for (Path relativePath : REQUIRED_VISUAL_TEST_SOURCES) {
            String source = Files.readString(root.resolve(relativePath));
            if (!source.contains("writeVisualSnapshot(")) {
                missingSnapshotReports.add(relativePath.toString());
            }
        }

        assertTrue(missingSnapshotReports.isEmpty(),
                () -> "Known visual suites must write reviewable PNG report snapshots: "
                        + missingSnapshotReports);
    }

    /// Verifies that demo visual snapshots keep a reviewable HTML index.
    @Test
    void demoVisualSnapshotsMaintainReviewIndex() throws IOException {
        Path root = workspaceRoot();
        String source = Files.readString(root.resolve(DEMO_VISUAL_SMOKE_TEST_SOURCE));
        List<String> missingMarkers = new ArrayList<>();
        for (String marker : VISUAL_SNAPSHOT_INDEX_MARKERS) {
            if (!source.contains(marker)) {
                missingMarkers.add(marker);
            }
        }

        assertTrue(missingMarkers.isEmpty(), () -> "Demo visual snapshots must keep a generated HTML index "
                + "with PNG enumeration and escaped file names: " + missingMarkers);
    }

    /// Verifies that visual snapshot report writes target the build report tree and PNG files.
    @Test
    void visualSnapshotReportsUseBuildReportsPngPaths() throws IOException {
        Path root = workspaceRoot();
        List<String> violations = new ArrayList<>();
        for (Path relativePath : discoverVisualTestSources(root)) {
            assertVisualSnapshotReportPaths(root.resolve(relativePath), relativePath, violations);
        }

        assertTrue(violations.isEmpty(), () -> "Visual snapshot reports must use build/reports PNG paths: "
                + violations);
    }

    /// Verifies that full-page demo sweeps keep the page-specific rendered-state contract in every mode.
    @Test
    void fullPageDemoVisualSweepsKeepPageSpecificStateChecks() throws IOException {
        Path root = workspaceRoot();
        String source = Files.readString(root.resolve(DEMO_VISUAL_SMOKE_TEST_SOURCE));
        List<String> violations = new ArrayList<>();

        for (String methodName : DEMO_FULL_PAGE_VISUAL_SWEEP_METHODS) {
            String methodBlock = methodBlock(source, methodName);
            for (String marker : DEMO_FULL_PAGE_VISUAL_SWEEP_MARKERS) {
                if (!methodBlock.contains(marker)) {
                    violations.add(DEMO_VISUAL_SMOKE_TEST_SOURCE + "#" + methodName + " is missing " + marker);
                }
            }
        }

        assertTrue(violations.isEmpty(), () -> "Full-page demo visual sweeps must keep stable page switching, "
                + "reviewable snapshots, shared geometry checks, and page-specific visual-state checks in every mode: "
                + violations);
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

    /// Verifies that visual wait helpers keep tests for condition-specific timeout diagnostics.
    @Test
    void visualWaitHelperTimeoutDiagnosticsStayCovered() throws IOException {
        Path root = workspaceRoot();
        List<String> violations = new ArrayList<>();
        for (Path relativePath : VISUAL_WAIT_HELPER_TEST_SOURCES) {
            Path sourceFile = root.resolve(relativePath);
            assertTrue(Files.isRegularFile(sourceFile),
                    () -> "Visual wait helper diagnostic test source is missing: " + relativePath);
            String source = Files.readString(sourceFile);
            assertContains(source, relativePath, "runOnFxThreadWhenWithTimeoutForTesting", violations);
            assertContains(source, relativePath, "runOnFxThreadWhenStableWithTimeoutForTesting", violations);
            assertContains(source, relativePath, "condition stayed false", violations);
            assertContains(source, relativePath, "condition never stayed stable", violations);
            assertContains(source, relativePath, "wait diagnostics:", violations);
            assertContains(source, relativePath, "pulseCount=", violations);
            assertContains(source, relativePath, "conditionEvaluations=", violations);
            assertContains(source, relativePath, "lastCondition=false", violations);
            assertContains(source, relativePath, "lastCondition=true", violations);
            assertContains(source, relativePath, "stablePulses=", violations);
            assertContains(source, relativePath, "maxStablePulses=", violations);
            assertContains(source, relativePath, "requiredStablePulses=", violations);
        }

        assertTrue(violations.isEmpty(), () -> "Visual wait helper timeout diagnostics must stay covered: "
                + violations);
    }

    /// Verifies that the source scanner rejects ad-hoc animation timers in visual tests.
    @Test
    void fixedWaitScannerDetectsAdHocAnimationWaitPrimitives() throws IOException {
        Path sourceFile = Files.createTempFile("m3fx-visual-wait-discipline", ".java");
        try {
            Files.writeString(sourceFile, """
                    class SampleVisualTest {
                        void fragileWaits() {
                            new javafx.animation.PauseTransition();
                            new Timeline();
                            new KeyFrame(null);
                            new AnimationTimer() {
                                @Override
                                public void handle(long now) {
                                }
                            };
                            Platform.runLater(() -> {
                            });
                            new CountDownLatch(1).await();
                        }
                    }
                    """);

            List<String> violations = new ArrayList<>();
            assertNoFixedTimeWaits(sourceFile, Path.of("SampleVisualTest.java"), BANNED_VISUAL_TEST_WAIT_PATTERNS,
                    violations);

            assertTrue(violations.stream().anyMatch(violation -> violation.contains("new javafx.animation.PauseTransition")),
                    () -> "Scanner did not report fully-qualified PauseTransition usage: " + violations);
            assertTrue(violations.stream().anyMatch(violation -> violation.contains("new Timeline")),
                    () -> "Scanner did not report direct Timeline usage: " + violations);
            assertTrue(violations.stream().anyMatch(violation -> violation.contains("new KeyFrame")),
                    () -> "Scanner did not report direct KeyFrame usage: " + violations);
            assertTrue(violations.stream().anyMatch(violation -> violation.contains("new AnimationTimer")),
                    () -> "Scanner did not report direct AnimationTimer usage: " + violations);
            assertTrue(violations.stream().anyMatch(violation -> violation.contains("Platform.runLater")),
                    () -> "Scanner did not report direct Platform.runLater usage: " + violations);
            assertTrue(violations.stream().anyMatch(violation -> violation.contains("new CountDownLatch")),
                    () -> "Scanner did not report direct CountDownLatch usage: " + violations);
            assertTrue(violations.stream().anyMatch(violation -> violation.contains(".await(")),
                    () -> "Scanner did not report direct await usage: " + violations);
        } finally {
            Files.deleteIfExists(sourceFile);
        }
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

    /// Returns whether a source file is a rendered visual regression test.
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

    /// Records visual snapshot report path violations from one source file.
    private static void assertVisualSnapshotReportPaths(
            Path sourceFile,
            Path relativePath,
            List<String> violations
    ) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (!line.contains("writeVisualSnapshot(") || isVisualSnapshotHelperLine(line)) {
                continue;
            }

            String callBlock = snapshotWriteCallBlock(lines, index);
            if (callBlock.contains("fileName")) {
                continue;
            }
            if (callBlock.contains("Path.of(") || callBlock.contains("java.nio.file.Path.of(")) {
                if (!callBlock.contains("\"build\"")
                        || !callBlock.contains("\"reports\"")
                        || !callBlock.contains(".png\"")) {
                    violations.add(relativePath + ":" + (index + 1) + " uses non-report snapshot path: "
                            + compact(callBlock));
                }
            } else {
                violations.add(relativePath + ":" + (index + 1)
                        + " writes a visual snapshot without an explicit Path.of report path: "
                        + compact(callBlock));
            }
        }

        assertPageSnapshotHelperCallsUsePngNames(lines, relativePath, violations);
    }

    /// Returns whether a `writeVisualSnapshot` line belongs to a helper implementation instead of a report write.
    private static boolean isVisualSnapshotHelperLine(String line) {
        return line.contains("private static void writeVisualSnapshot(")
                || line.contains("writeVisualSnapshot(toBufferedImage(image), path)");
    }

    /// Records helper call violations for full-page demo snapshot writers.
    private static void assertPageSnapshotHelperCallsUsePngNames(
            List<String> lines,
            Path relativePath,
            List<String> violations
    ) {
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (!line.contains("writePageSnapshot(") || line.contains("private static void writePageSnapshot(")) {
                continue;
            }

            String callBlock = snapshotWriteCallBlock(lines, index);
            if (!callBlock.contains(".png\"")) {
                violations.add(relativePath + ":" + (index + 1)
                        + " calls writePageSnapshot without a PNG file name: " + compact(callBlock));
            }
        }
    }

    /// Records a missing required source fragment.
    private static void assertContains(
            String source,
            Path relativePath,
            String expectedFragment,
            List<String> violations
    ) {
        if (!source.contains(expectedFragment)) {
            violations.add(relativePath + " is missing " + expectedFragment);
        }
    }

    /// Returns the source block for one `writeVisualSnapshot` call.
    private static String snapshotWriteCallBlock(List<String> lines, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int index = startIndex;
             index < lines.size() && index < startIndex + SNAPSHOT_WRITE_CALL_MAX_LINES;
             index++) {
            String line = lines.get(index).trim();
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(line);
            if (line.endsWith(");") || line.endsWith("));")) {
                break;
            }
        }
        return builder.toString();
    }

    /// Combines two wait-pattern lists into one immutable list.
    private static @Unmodifiable List<String> combinedWaitPatterns(
            List<String> first,
            List<String> second
    ) {
        ArrayList<String> result = new ArrayList<>(first.size() + second.size());
        result.addAll(first);
        result.addAll(second);
        return List.copyOf(result);
    }

    /// Compacts a multi-line source block for assertion diagnostics.
    private static String compact(String sourceBlock) {
        return sourceBlock.replaceAll("\\s+", " ").trim();
    }

    /// Returns one method body block from Java source text.
    private static String methodBlock(String source, String methodName) {
        int methodIndex = source.indexOf("void " + methodName + "(");
        if (methodIndex < 0) {
            throw new AssertionError("Could not find method " + methodName);
        }

        int openingBraceIndex = source.indexOf('{', methodIndex);
        if (openingBraceIndex < 0) {
            throw new AssertionError("Could not find opening brace for method " + methodName);
        }

        int closingBraceIndex = closingBraceIndex(source, openingBraceIndex);
        return source.substring(openingBraceIndex, closingBraceIndex + 1);
    }

    /// Returns the matching closing brace index for one source block.
    private static int closingBraceIndex(String source, int openingBraceIndex) {
        int depth = 0;
        for (int index = openingBraceIndex; index < source.length(); index++) {
            char character = source.charAt(index);
            if (character == '{') {
                depth++;
            } else if (character == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        throw new AssertionError("Could not find closing brace after index " + openingBraceIndex);
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
