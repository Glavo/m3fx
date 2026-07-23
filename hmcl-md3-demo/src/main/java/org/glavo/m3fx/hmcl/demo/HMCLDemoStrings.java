// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/// Resolves localized text for the HMCL-inspired M3FX demo.
///
/// The English root bundle is the final fallback. Any Chinese locale selects the Simplified Chinese bundle, while
/// every other locale selects English. Locale changes are observable at runtime through [#localeProperty()].
@NotNullByDefault
public final class HMCLDemoStrings {
    /// The base name shared by the English and Simplified Chinese resource bundles.
    public static final String BUNDLE_BASE_NAME = "org.glavo.m3fx.hmcl.demo.messages";

    /// The English locale exposed by the demo language selector.
    public static final Locale ENGLISH = Locale.ENGLISH;

    /// The Simplified Chinese locale exposed by the demo language selector.
    public static final Locale SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE;

    /// The supported demo locales in selector order.
    private static final @Unmodifiable List<Locale> SUPPORTED_LOCALES =
            List.of(ENGLISH, SIMPLIFIED_CHINESE);

    /// The currently selected, normalized locale.
    private final ObjectProperty<Locale> locale;

    /// Creates a string resolver using the supported locale nearest to the JVM default locale.
    public HMCLDemoStrings() {
        this(Locale.getDefault());
    }

    /// Creates a string resolver using the supported locale nearest to `initialLocale`.
    ///
    /// @param initialLocale the requested initial locale
    public HMCLDemoStrings(Locale initialLocale) {
        locale = new SimpleObjectProperty<>(this, "locale", normalize(initialLocale));
    }

    /// Returns the supported demo locales.
    ///
    /// @return an immutable locale list
    public static @Unmodifiable List<Locale> supportedLocales() {
        return SUPPORTED_LOCALES;
    }

    /// Returns the currently selected normalized locale.
    ///
    /// @return either [#ENGLISH] or [#SIMPLIFIED_CHINESE]
    public Locale getLocale() {
        return locale.get();
    }

    /// Selects the supported locale nearest to `value`.
    ///
    /// @param value the requested locale
    public void setLocale(Locale value) {
        locale.set(normalize(value));
    }

    /// Returns the writable runtime locale property.
    ///
    /// Callers should prefer [#setLocale(Locale)] when assigning a value. Text lookup still normalizes values assigned
    /// directly through this property.
    ///
    /// @return the locale property
    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    /// Returns the localized string for `key`.
    ///
    /// @param key the resource key
    /// @return the localized value
    /// @throws java.util.MissingResourceException if the key is absent from the English fallback bundle
    public String get(String key) {
        return bundle().getString(key);
    }

    /// Formats the localized pattern for `key` using [MessageFormat].
    ///
    /// @param key the resource key
    /// @param arguments the positional format arguments
    /// @return the formatted localized value
    /// @throws java.util.MissingResourceException if the key is absent from the English fallback bundle
    /// @throws IllegalArgumentException if the localized value is not a valid [MessageFormat] pattern
    public String format(String key, Object... arguments) {
        return new MessageFormat(get(key), normalizedLocale()).format(arguments);
    }

    /// Creates a binding that recomputes a localized string whenever the locale changes.
    ///
    /// The supplied arguments are captured by reference and are not observed.
    ///
    /// @param key the resource key
    /// @param arguments the positional format arguments
    /// @return a binding backed by [#localeProperty()]
    public StringBinding bind(String key, Object... arguments) {
        return Bindings.createStringBinding(() -> format(key, arguments), locale);
    }

    /// Returns the resource bundle for the current normalized locale.
    private ResourceBundle bundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, normalizedLocale());
    }

    /// Returns the current locale normalized to the two-locale demo set.
    private Locale normalizedLocale() {
        return normalize(locale.get());
    }

    /// Maps a locale to the two-locale demo set.
    private static Locale normalize(Locale value) {
        return "zh".equalsIgnoreCase(value.getLanguage()) ? SIMPLIFIED_CHINESE : ENGLISH;
    }
}
