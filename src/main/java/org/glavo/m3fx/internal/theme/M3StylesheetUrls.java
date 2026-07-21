// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/// Creates self-contained URLs for generated M3FX stylesheets.
///
/// JavaFX 17 and later parse CSS data URLs directly. Each returned URL retains the complete stylesheet content and
/// requires neither a temporary file nor a process-wide content registry.
@NotNullByDefault
final class M3StylesheetUrls {
    /// The prefix for UTF-8 CSS encoded as a standard Base64 data URL.
    private static final String DATA_URL_PREFIX = "data:text/css;charset=UTF-8;base64,";

    /// The encoder used by data URLs, whose decoder expects the standard Base64 alphabet.
    private static final Base64.Encoder DATA_URL_ENCODER = Base64.getEncoder();

    /// Prevents utility class instantiation.
    private M3StylesheetUrls() {
    }

    /// Encodes a generated stylesheet as a Base64 data URL understood by JavaFX 17 and later.
    ///
    /// @param stylesheet the complete UTF-16 Java stylesheet text to encode as UTF-8
    /// @return a self-contained external-form URL suitable for a JavaFX stylesheet list
    /// @throws NullPointerException if `stylesheet` is `null`
    static String create(String stylesheet) {
        Objects.requireNonNull(stylesheet, "stylesheet");

        byte @Unmodifiable [] content = stylesheet.getBytes(StandardCharsets.UTF_8);
        return DATA_URL_PREFIX + DATA_URL_ENCODER.encodeToString(content);
    }
}
