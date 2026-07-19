// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests self-contained generated stylesheet URL encoding and compatibility protocol loading.
@NotNullByDefault
final class M3StylesheetUrlsTest {
    /// Verifies that runtime-selected stylesheet URLs retain their complete UTF-8 content.
    @Test
    void createsSelfContainedStylesheetUrl() throws IOException {
        String stylesheet = ".m3-root { -m3-test-text: 'M3FX'; }";
        String url = M3StylesheetUrls.create(stylesheet);

        assertTrue(url.startsWith("data:") || url.startsWith("m3fx-css:"));
        assertEquals(stylesheet, readStylesheet(url));
    }

    /// Verifies that the service-loaded compatibility handler decodes its direct payload and metadata.
    @Test
    void opensCustomStylesheetUrlThroughServiceProvider() throws IOException {
        String stylesheet = ".m3-root { -m3-test-number: 42; }";
        String url = M3StylesheetUrls.CUSTOM_PROTOCOL + ':'
                + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(stylesheet.getBytes(StandardCharsets.UTF_8));

        assertTrue(url.startsWith("m3fx-css:"));
        assertFalse(url.startsWith("m3fx-css:base64"));

        URLConnection connection = URI.create(url).toURL().openConnection();
        assertEquals("text/css; charset=UTF-8", connection.getContentType());
        assertEquals(stylesheet.getBytes(StandardCharsets.UTF_8).length, connection.getContentLength());
        try (InputStream input = connection.getInputStream()) {
            assertEquals(stylesheet, new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /// Decodes either generated stylesheet representation for an encoding assertion.
    ///
    /// @param url the self-contained stylesheet URL
    /// @return the decoded UTF-8 stylesheet
    /// @throws IOException if the compatibility URL cannot be opened
    private static String readStylesheet(String url) throws IOException {
        if (url.startsWith("data:")) {
            int payloadIndex = url.indexOf(',') + 1;
            return new String(Base64.getDecoder().decode(url.substring(payloadIndex)), StandardCharsets.UTF_8);
        }
        try (InputStream input = URI.create(url).toURL().openStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
