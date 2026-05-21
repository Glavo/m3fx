// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.tokens.M3MotionTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds Material Design 3 motion duration tokens in milliseconds.
@NotNullByDefault
public sealed interface M3MotionTokens permits M3MotionTokensImpl {
    /// Returns the short1 duration token.
    int short1();

    /// Returns the short2 duration token.
    int short2();

    /// Returns the short3 duration token.
    int short3();

    /// Returns the short4 duration token.
    int short4();

    /// Returns the medium1 duration token.
    int medium1();

    /// Returns the medium2 duration token.
    int medium2();

    /// Returns the medium3 duration token.
    int medium3();

    /// Returns the medium4 duration token.
    int medium4();

    /// Returns the long1 duration token.
    int long1();

    /// Returns the long2 duration token.
    int long2();

    /// Returns the long3 duration token.
    int long3();

    /// Returns the long4 duration token.
    int long4();

    /// Returns the extraLong1 duration token.
    int extraLong1();

    /// Returns the extraLong2 duration token.
    int extraLong2();

    /// Returns the extraLong3 duration token.
    int extraLong3();

    /// Returns the extraLong4 duration token.
    int extraLong4();

    /// Returns the semantic motion scheme used by this profile.
    M3MotionScheme scheme();

    /// Returns the fast effects motion spec.
    default M3MotionSpec fastEffects() {
        return scheme().fastEffects();
    }

    /// Returns the default effects motion spec.
    default M3MotionSpec defaultEffects() {
        return scheme().defaultEffects();
    }

    /// Returns the slow effects motion spec.
    default M3MotionSpec slowEffects() {
        return scheme().slowEffects();
    }

    /// Returns the fast spatial motion spec.
    default M3MotionSpec fastSpatial() {
        return scheme().fastSpatial();
    }

    /// Returns the default spatial motion spec.
    default M3MotionSpec defaultSpatial() {
        return scheme().defaultSpatial();
    }

    /// Returns the slow spatial motion spec.
    default M3MotionSpec slowSpatial() {
        return scheme().slowSpatial();
    }

    /// Returns the legacy short duration alias.
    default int shortDuration() {
        return short2();
    }

    /// Returns the legacy medium duration alias.
    default int mediumDuration() {
        return medium1();
    }

    /// Returns the legacy long duration alias.
    default int longDuration() {
        return long2();
    }

    /// Creates motion duration tokens from legacy coarse duration values.
    static M3MotionTokens create(int shortDuration, int mediumDuration, int longDuration) {
        return create(
                shortDuration,
                shortDuration,
                shortDuration,
                shortDuration,
                mediumDuration,
                mediumDuration,
                mediumDuration,
                mediumDuration,
                longDuration,
                longDuration,
                longDuration,
                longDuration,
                longDuration,
                longDuration,
                longDuration,
                longDuration,
                M3MotionScheme.standard()
        );
    }

    /// Creates motion duration tokens.
    static M3MotionTokens create(
            int short1,
            int short2,
            int short3,
            int short4,
            int medium1,
            int medium2,
            int medium3,
            int medium4,
            int long1,
            int long2,
            int long3,
            int long4,
            int extraLong1,
            int extraLong2,
            int extraLong3,
            int extraLong4
    ) {
        return create(
                short1,
                short2,
                short3,
                short4,
                medium1,
                medium2,
                medium3,
                medium4,
                long1,
                long2,
                long3,
                long4,
                extraLong1,
                extraLong2,
                extraLong3,
                extraLong4,
                M3MotionScheme.standard()
        );
    }

    /// Creates motion duration tokens with a semantic motion scheme.
    static M3MotionTokens create(
            int short1,
            int short2,
            int short3,
            int short4,
            int medium1,
            int medium2,
            int medium3,
            int medium4,
            int long1,
            int long2,
            int long3,
            int long4,
            int extraLong1,
            int extraLong2,
            int extraLong3,
            int extraLong4,
            M3MotionScheme scheme
    ) {
        Objects.requireNonNull(scheme, "scheme");
        return new M3MotionTokensImpl(
                short1,
                short2,
                short3,
                short4,
                medium1,
                medium2,
                medium3,
                medium4,
                long1,
                long2,
                long3,
                long4,
                extraLong1,
                extraLong2,
                extraLong3,
                extraLong4,
                scheme
        );
    }

    /// Returns baseline Material Design 3 motion tokens.
    static M3MotionTokens baseline() {
        return create(
                50,
                100,
                150,
                200,
                250,
                300,
                350,
                400,
                450,
                500,
                550,
                600,
                700,
                800,
                900,
                1000
        );
    }

    /// Returns expressive Material Design 3 motion tokens.
    static M3MotionTokens expressive() {
        return create(
                50,
                100,
                150,
                200,
                250,
                300,
                350,
                400,
                450,
                500,
                550,
                600,
                700,
                800,
                900,
                1000,
                M3MotionScheme.expressive()
        );
    }

    /// Converts motion tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        appendLegacyStyleDeclarations(builder);
        appendSchemeStyleDeclarations(builder);
        M3TokenCss.append(builder, "-m3-motion-duration-short1", short1() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-short2", short2() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-short3", short3() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-short4", short4() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium1", medium1() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium2", medium2() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium3", medium3() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium4", medium4() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long1", long1() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long2", long2() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long3", long3() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long4", long4() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-extra-long1", extraLong1() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-extra-long2", extraLong2() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-extra-long3", extraLong3() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-extra-long4", extraLong4() + "ms");
        return builder.toString().trim();
    }

    /// Appends legacy coarse duration declarations for compatibility.
    private void appendLegacyStyleDeclarations(StringBuilder builder) {
        M3TokenCss.append(builder, "-m3-motion-duration-short", shortDuration() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium", mediumDuration() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long", longDuration() + "ms");
    }

    /// Appends semantic motion scheme declarations.
    private void appendSchemeStyleDeclarations(StringBuilder builder) {
        appendSpec(builder, "fast-effects", fastEffects());
        appendSpec(builder, "default-effects", defaultEffects());
        appendSpec(builder, "slow-effects", slowEffects());
        appendSpec(builder, "fast-spatial", fastSpatial());
        appendSpec(builder, "default-spatial", defaultSpatial());
        appendSpec(builder, "slow-spatial", slowSpatial());
    }

    /// Appends declarations for one semantic motion spec.
    private static void appendSpec(StringBuilder builder, String name, M3MotionSpec spec) {
        M3TokenCss.append(builder, "-m3-motion-" + name + "-duration", M3TokenCss.format(spec.duration().toMillis()) + "ms");
        M3TokenCss.append(builder, "-m3-motion-" + name + "-easing", spec.easing().tokenName());
    }
}
