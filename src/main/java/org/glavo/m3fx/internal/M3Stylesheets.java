// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.jetbrains.annotations.NotNullByDefault;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/// Resolves bundled m3fx stylesheet resources.
@NotNullByDefault
public final class M3Stylesheets {
    /// The bundled stylesheet resource directory.
    private static final String STYLESHEET_DIRECTORY = "/org/glavo/m3fx/styles/";

    /// Cached stylesheet URLs keyed by resource path.
    private static final Map<String, String> STYLESHEETS = new ConcurrentHashMap<>();

    /// Prevents utility class instantiation.
    private M3Stylesheets() {
    }

    /// Returns the base m3fx stylesheet URL.
    public static String baseStylesheet() {
        return stylesheet("base.css");
    }

    /// Returns a control-specific m3fx stylesheet URL.
    public static String controlStylesheet(String name) {
        Objects.requireNonNull(name, "name");
        return stylesheet("controls/" + name);
    }

    /// Returns a bundled stylesheet URL.
    private static String stylesheet(String path) {
        return STYLESHEETS.computeIfAbsent(path, M3Stylesheets::resolveStylesheet);
    }

    /// Resolves a bundled stylesheet resource URL.
    private static String resolveStylesheet(String path) {
        URL url = M3Stylesheets.class.getResource(STYLESHEET_DIRECTORY + path);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet resource: " + STYLESHEET_DIRECTORY + path);
        }
        return url.toExternalForm();
    }
}
