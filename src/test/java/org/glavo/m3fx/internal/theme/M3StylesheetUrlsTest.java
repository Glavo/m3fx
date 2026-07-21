// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests self-contained generated stylesheet data URL encoding.
@NotNullByDefault
final class M3StylesheetUrlsTest {
    /// Verifies that stylesheet data URLs retain their complete UTF-8 content.
    @Test
    void createsSelfContainedStylesheetUrl() {
        String stylesheet = ".m3-root { -m3-test-text: 'M3FX'; }";
        String url = M3StylesheetUrls.create(stylesheet);

        assertTrue(url.startsWith("data:text/css;charset=UTF-8;base64,"));
        int payloadIndex = url.indexOf(',') + 1;
        assertEquals(stylesheet,
                new String(Base64.getDecoder().decode(url.substring(payloadIndex)), StandardCharsets.UTF_8));
    }
}
