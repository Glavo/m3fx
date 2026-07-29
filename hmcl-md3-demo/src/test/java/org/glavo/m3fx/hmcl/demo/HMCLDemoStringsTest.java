// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.beans.binding.StringBinding;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies the locale bundle and runtime string-resolution contracts.
@NotNullByDefault
final class HMCLDemoStringsTest {
    /// Matches a positional argument at the start of a [MessageFormat] placeholder.
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)");

    /// The English resource location used for direct bundle-contract inspection.
    private static final String ENGLISH_RESOURCE = "/org/glavo/m3fx/hmcl/demo/messages.properties";

    /// The Simplified Chinese resource location used for direct bundle-contract inspection.
    private static final String CHINESE_RESOURCE = "/org/glavo/m3fx/hmcl/demo/messages_zh_CN.properties";

    /// Verifies that both bundles declare identical keys and positional placeholders.
    @Test
    void resourceBundlesDeclareSameKeysAndPlaceholders() throws IOException {
        Map<String, String> english = loadProperties(ENGLISH_RESOURCE);
        Map<String, String> chinese = loadProperties(CHINESE_RESOURCE);

        assertEquals(english.keySet(), chinese.keySet());
        for (String key : english.keySet()) {
            String englishPattern = english.get(key);
            String chinesePattern = chinese.get(key);
            new MessageFormat(englishPattern, HMCLDemoStrings.ENGLISH);
            new MessageFormat(chinesePattern, HMCLDemoStrings.SIMPLIFIED_CHINESE);
            assertEquals(
                    placeholderIndexes(englishPattern),
                    placeholderIndexes(chinesePattern),
                    () -> "Placeholder mismatch for key " + key
            );
        }
    }

    /// Verifies English fallback, formatting, and live locale bindings.
    @Test
    void resolvesAndRebindsRuntimeStrings() {
        HMCLDemoStrings strings = new HMCLDemoStrings(Locale.FRENCH);
        StringBinding title = strings.bind("app.title");

        assertEquals(HMCLDemoStrings.ENGLISH, strings.getLocale());
        assertEquals("Hello Minecraft! Launcher", title.get());
        assertEquals("Home", strings.get("nav.home"));

        strings.setLocale(Locale.SIMPLIFIED_CHINESE);
        assertEquals("Hello Minecraft! Launcher", title.get());
        assertEquals("主页", strings.get("nav.home"));
        assertEquals("正在以 Glavo 启动 Creative Workshop", strings.format("snackbar.launching", "Creative Workshop", "Glavo"));
    }

    /// Verifies an explicit English selection cannot fall back to the JVM default Chinese locale.
    @Test
    @ResourceLock("java.util.Locale.default")
    void resolvesEnglishWithoutDefaultLocaleFallback() {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(HMCLDemoStrings.SIMPLIFIED_CHINESE);
            HMCLDemoStrings strings = new HMCLDemoStrings(HMCLDemoStrings.ENGLISH);

            assertEquals(HMCLDemoStrings.ENGLISH, strings.getLocale());
            assertEquals("Home", strings.get("nav.home"));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    /// Loads a UTF-8 properties resource as an immutable map.
    ///
    /// @param resourceName the absolute class-path resource name
    /// @return all properties declared by the resource
    /// @throws IOException if the resource cannot be read
    private static @Unmodifiable Map<String, String> loadProperties(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Objects.requireNonNull(
                HMCLDemoStringsTest.class.getResourceAsStream(resourceName),
                () -> "Missing test resource " + resourceName
        ); InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        Map<String, String> values = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            values.put(key, properties.getProperty(key));
        }
        return Map.copyOf(values);
    }

    /// Extracts the positional argument indexes referenced by a message pattern.
    ///
    /// @param pattern the [MessageFormat] pattern
    /// @return the immutable set of referenced indexes
    private static @Unmodifiable Set<Integer> placeholderIndexes(String pattern) {
        Matcher matcher = PLACEHOLDER.matcher(pattern);
        Set<Integer> indexes = new HashSet<>();
        while (matcher.find()) {
            indexes.add(Integer.parseInt(matcher.group(1)));
        }
        return Set.copyOf(indexes);
    }
}
