// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.build.nativeimage;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/// Verifies and stages one host-platform Native Image executable for distribution.
@DisableCachingByDefault(because = "The task copies and validates a local platform executable")
@NotNullByDefault
public abstract class StageNativeExecutableTask extends DefaultTask {
    /// Creates a Native Image staging task.
    public StageNativeExecutableTask() {
    }

    /// Returns the executable emitted by `nativeCompile`.
    ///
    /// @return the source executable property
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getSourceExecutable();

    /// Returns the verified executable written to the distribution directory.
    ///
    /// @return the staged executable property
    @OutputFile
    public abstract RegularFileProperty getTargetExecutable();

    /// Returns whether the current host is Windows.
    ///
    /// @return the Windows host property
    @Input
    public abstract Property<Boolean> getWindowsHost();

    /// Returns whether a Windows executable must use the GUI subsystem.
    ///
    /// @return the Windows GUI verification property
    @Input
    public abstract Property<Boolean> getWindowsGuiApplication();

    /// Verifies the compiled executable and copies it to the configured distribution path.
    @TaskAction
    public final void stageExecutable() {
        File sourceExecutable = getSourceExecutable().get().getAsFile();
        if (!sourceExecutable.isFile() || sourceExecutable.length() == 0L) {
            throw new GradleException(
                    "The compiled Native Image is missing or empty: " + sourceExecutable.getAbsolutePath()
            );
        }

        boolean windowsHost = getWindowsHost().get();
        if (windowsHost && getWindowsGuiApplication().get()) {
            verifyWindowsGuiExecutable(sourceExecutable);
        }

        File targetExecutable = getTargetExecutable().get().getAsFile();
        try {
            Files.createDirectories(targetExecutable.toPath().getParent());
            Files.copy(
                    sourceExecutable.toPath(),
                    targetExecutable.toPath(),
                    StandardCopyOption.COPY_ATTRIBUTES,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new GradleException("Could not stage " + sourceExecutable.getAbsolutePath(), exception);
        }

        if (!targetExecutable.isFile() || targetExecutable.length() == 0L) {
            throw new GradleException(
                    "The staged Native Image is missing or empty: " + targetExecutable.getAbsolutePath()
            );
        }
        if (!windowsHost && !targetExecutable.setExecutable(true, false)) {
            throw new GradleException("Could not mark " + targetExecutable.getAbsolutePath() + " as executable");
        }
    }

    /// Verifies that one PE executable uses the Windows GUI subsystem.
    ///
    /// @param executableFile the compiled Windows executable
    private static void verifyWindowsGuiExecutable(File executableFile) {
        try (RandomAccessFile executable = new RandomAccessFile(executableFile, "r")) {
            if (executable.length() < 0x40L || readUnsignedShortLittleEndian(executable) != 0x5a4d) {
                throw new GradleException("The compiled Native Image is not a Windows PE executable");
            }
            executable.seek(0x3cL);
            long peHeaderOffset = readUnsignedIntLittleEndian(executable);
            if (peHeaderOffset > executable.length() - 94L) {
                throw new GradleException("The compiled Native Image has an invalid PE header offset");
            }
            executable.seek(peHeaderOffset);
            if (readUnsignedIntLittleEndian(executable) != 0x00004550L) {
                throw new GradleException("The compiled Native Image has an invalid PE signature");
            }
            executable.seek(peHeaderOffset + 24L);
            int optionalHeaderMagic = readUnsignedShortLittleEndian(executable);
            if (optionalHeaderMagic != 0x010b && optionalHeaderMagic != 0x020b) {
                throw new GradleException("The compiled Native Image has an unsupported PE optional header");
            }
            executable.seek(peHeaderOffset + 92L);
            int subsystem = readUnsignedShortLittleEndian(executable);
            if (subsystem != 2) {
                throw new GradleException(
                        "The Windows Native Image must use the GUI subsystem, but its PE subsystem is " + subsystem
                );
            }
        } catch (IOException exception) {
            throw new GradleException("Could not inspect " + executableFile.getAbsolutePath(), exception);
        }
    }

    /// Reads one unsigned little-endian 16-bit value.
    ///
    /// @param executable the executable stream
    /// @return the decoded value
    /// @throws IOException if the value cannot be read
    private static int readUnsignedShortLittleEndian(RandomAccessFile executable) throws IOException {
        return executable.readUnsignedByte() | (executable.readUnsignedByte() << 8);
    }

    /// Reads one unsigned little-endian 32-bit value.
    ///
    /// @param executable the executable stream
    /// @return the decoded value
    /// @throws IOException if the value cannot be read
    private static long readUnsignedIntLittleEndian(RandomAccessFile executable) throws IOException {
        return Integer.toUnsignedLong(
                readUnsignedShortLittleEndian(executable) | (readUnsignedShortLittleEndian(executable) << 16)
        );
    }
}
