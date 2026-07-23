// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.build.nativeimage;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/// Verifies that Native Image compilation uses a JavaFX-enabled Liberica Native Image Kit.
@DisableCachingByDefault(because = "The task validates the active external Native Image installation")
@NotNullByDefault
public abstract class VerifyNativeImageToolchainTask extends DefaultTask {
    /// Creates a Native Image toolchain verification task.
    public VerifyNativeImageToolchainTask() {
    }

    /// Returns the JDK module names required from the Native Image Kit.
    ///
    /// @return the required module names
    @Input
    public abstract ListProperty<String> getRequiredJmods();

    /// Verifies the configured Native Image executable, runtime identity, and required JMOD files.
    @TaskAction
    public final void verifyToolchain() {
        @Nullable String configuredHome = System.getenv("GRAALVM_HOME");
        if (configuredHome != null) {
            configuredHome = configuredHome.trim();
            if (configuredHome.isEmpty()) {
                configuredHome = null;
            }
        }

        @Nullable String javaHome = System.getProperty("java.home");
        if (configuredHome == null && (javaHome == null || javaHome.isBlank())) {
            throw new GradleException("Neither GRAALVM_HOME nor java.home identifies a Native Image installation.");
        }

        String resolvedHome = configuredHome != null ? configuredHome : Objects.requireNonNull(javaHome);
        File nativeImageHome = new File(resolvedHome);
        @Nullable File nativeImageExecutable = findNativeImageExecutable(nativeImageHome);
        if (nativeImageExecutable == null) {
            throw new GradleException(
                    "No native-image executable was found under " + nativeImageHome.getAbsolutePath()
                            + ". Set GRAALVM_HOME to a Liberica NIK Full installation."
            );
        }

        for (String moduleName : getRequiredJmods().get()) {
            File jmod = new File(new File(nativeImageHome, "jmods"), moduleName + ".jmod");
            if (!jmod.isFile()) {
                throw new GradleException(
                        "The Native Image toolchain at " + nativeImageHome.getAbsolutePath()
                                + " does not include " + moduleName + ". Use the Full distribution of Liberica NIK."
                );
            }
        }

        String versionOutput = readVersionOutput(nativeImageExecutable);
        if (!versionOutput.contains("Liberica-NIK")) {
            throw new GradleException(
                    "Unsupported Native Image toolchain at " + nativeImageHome.getAbsolutePath()
                            + ". Use Liberica NIK Full. Detected output:\n" + versionOutput
            );
        }
    }

    /// Locates the platform-specific Native Image launcher under one installation.
    ///
    /// @param nativeImageHome the candidate Native Image home directory
    /// @return the executable, or `null` when no supported launcher exists
    private static @Nullable File findNativeImageExecutable(File nativeImageHome) {
        for (String executableName : List.of("native-image", "native-image.cmd", "native-image.exe")) {
            File executable = new File(new File(nativeImageHome, "bin"), executableName);
            if (executable.isFile()) {
                return executable;
            }
        }
        return null;
    }

    /// Runs `native-image --version` and returns its combined output.
    ///
    /// @param nativeImageExecutable the executable to inspect
    /// @return the version command output
    private static String readVersionOutput(File nativeImageExecutable) {
        Process process;
        try {
            process = new ProcessBuilder(nativeImageExecutable.getAbsolutePath(), "--version")
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException exception) {
            throw new GradleException("Could not start " + nativeImageExecutable.getAbsolutePath(), exception);
        }

        String versionOutput;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            versionOutput = reader.lines().reduce("", (left, right) -> left + right + System.lineSeparator());
        } catch (IOException exception) {
            throw new GradleException("Could not read Native Image version output", exception);
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GradleException("Native Image toolchain verification was interrupted", exception);
        }
        if (exitCode != 0) {
            throw new GradleException(
                    "Native Image version detection failed with exit code " + exitCode + ":\n" + versionOutput
            );
        }
        return versionOutput;
    }
}
