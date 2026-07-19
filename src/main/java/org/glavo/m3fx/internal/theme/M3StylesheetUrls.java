// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import javafx.scene.Scene;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.module.ModuleDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/// Creates self-contained URLs for generated M3FX stylesheets.
///
/// JavaFX 17 and later parse CSS data URLs directly. Earlier supported JavaFX releases receive an equivalent
/// `m3fx-css` URL whose handler is supplied by [M3StylesheetUrlStreamHandlerProvider]. Both representations retain
/// their complete stylesheet content and require neither temporary files nor a process-wide content registry.
@NotNullByDefault
final class M3StylesheetUrls {
    /// The private URL protocol used when the JavaFX runtime does not parse CSS data URLs.
    static final String CUSTOM_PROTOCOL = "m3fx-css";

    /// The prefix for UTF-8 CSS encoded as a standard Base64 data URL.
    private static final String DATA_URL_PREFIX = "data:text/css;charset=UTF-8;base64,";

    /// The prefix for an M3FX stylesheet URL followed directly by its encoded payload.
    private static final String CUSTOM_URL_PREFIX = CUSTOM_PROTOCOL + ':';

    /// The encoder used by data URLs, whose decoder expects the standard Base64 alphabet.
    private static final Base64.Encoder DATA_URL_ENCODER = Base64.getEncoder();

    /// The unpadded URL-safe encoder used by the custom protocol payload.
    private static final Base64.Encoder CUSTOM_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    /// The first JavaFX feature release whose stylesheet manager parses data URIs.
    private static final int DATA_STYLESHEET_URL_MINIMUM_VERSION = 17;

    /// Whether this JavaFX runtime contains the native data-URI parser used by its stylesheet manager.
    private static final boolean DATA_STYLESHEET_URLS_SUPPORTED = supportsDataStylesheetUrls();

    /// Prevents utility class instantiation.
    private M3StylesheetUrls() {
    }

    /// Encodes a generated stylesheet using the representation supported by the current JavaFX runtime.
    ///
    /// @param stylesheet the complete UTF-16 Java stylesheet text to encode as UTF-8
    /// @return a self-contained external-form URL suitable for a JavaFX stylesheet list
    /// @throws NullPointerException if `stylesheet` is `null`
    static String create(String stylesheet) {
        Objects.requireNonNull(stylesheet, "stylesheet");

        byte @Unmodifiable [] content = stylesheet.getBytes(StandardCharsets.UTF_8);
        if (DATA_STYLESHEET_URLS_SUPPORTED) {
            return DATA_URL_PREFIX + DATA_URL_ENCODER.encodeToString(content);
        }
        return CUSTOM_URL_PREFIX + CUSTOM_URL_ENCODER.encodeToString(content);
    }

    /// Detects the JavaFX stylesheet implementation that recognizes data URIs.
    ///
    /// Module metadata is available before toolkit startup. Package metadata and the standard JavaFX version property
    /// cover classpath deployments whose module descriptor is not visible at runtime.
    ///
    /// @return `true` when native CSS data-URI parsing is available
    private static boolean supportsDataStylesheetUrls() {
        @Nullable ModuleDescriptor descriptor = Scene.class.getModule().getDescriptor();
        if (descriptor != null) {
            String nameAndVersion = descriptor.toNameAndVersion();
            int separator = nameAndVersion.lastIndexOf('@');
            if (separator >= 0) {
                return featureVersion(nameAndVersion.substring(separator + 1))
                        >= DATA_STYLESHEET_URL_MINIMUM_VERSION;
            }
        }

        @Nullable String implementationVersion = Scene.class.getPackage().getImplementationVersion();
        if (implementationVersion != null) {
            int featureVersion = featureVersion(implementationVersion);
            if (featureVersion >= 0) {
                return featureVersion >= DATA_STYLESHEET_URL_MINIMUM_VERSION;
            }
        }

        return featureVersion(System.getProperty("javafx.version", ""))
                >= DATA_STYLESHEET_URL_MINIMUM_VERSION;
    }

    /// Extracts the leading feature number from a JavaFX version string.
    ///
    /// @param version a module, package, or system-property version
    /// @return the leading non-negative feature number, or `-1` when no feature number is present
    private static int featureVersion(String version) {
        int end = 0;
        while (end < version.length() && Character.isDigit(version.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(version.substring(0, end));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
