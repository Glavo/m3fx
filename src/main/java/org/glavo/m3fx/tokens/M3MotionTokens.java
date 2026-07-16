// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.tokens.M3MotionTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds Material Design 3 motion duration tokens in milliseconds.
///
/// Motion tokens provide the duration ladder, semantic [M3MotionScheme], and [M3MotionBehavior] used by M3FX
/// controls for state feedback, popup transitions, smooth scrolling, and progress animation. Baseline and
/// expressive profiles can provide different durations and curves while sharing the same public API.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionTokens permits M3MotionTokensImpl {
    /// Returns the short1 duration token.
    ///
    /// @return the short1 duration token in milliseconds
    int short1();

    /// Returns the short2 duration token.
    ///
    /// @return the short2 duration token in milliseconds
    int short2();

    /// Returns the short3 duration token.
    ///
    /// @return the short3 duration token in milliseconds
    int short3();

    /// Returns the short4 duration token.
    ///
    /// @return the short4 duration token in milliseconds
    int short4();

    /// Returns the medium1 duration token.
    ///
    /// @return the medium1 duration token in milliseconds
    int medium1();

    /// Returns the medium2 duration token.
    ///
    /// @return the medium2 duration token in milliseconds
    int medium2();

    /// Returns the medium3 duration token.
    ///
    /// @return the medium3 duration token in milliseconds
    int medium3();

    /// Returns the medium4 duration token.
    ///
    /// @return the medium4 duration token in milliseconds
    int medium4();

    /// Returns the long1 duration token.
    ///
    /// @return the long1 duration token in milliseconds
    int long1();

    /// Returns the long2 duration token.
    ///
    /// @return the long2 duration token in milliseconds
    int long2();

    /// Returns the long3 duration token.
    ///
    /// @return the long3 duration token in milliseconds
    int long3();

    /// Returns the long4 duration token.
    ///
    /// @return the long4 duration token in milliseconds
    int long4();

    /// Returns the extraLong1 duration token.
    ///
    /// @return the extraLong1 duration token in milliseconds
    int extraLong1();

    /// Returns the extraLong2 duration token.
    ///
    /// @return the extraLong2 duration token in milliseconds
    int extraLong2();

    /// Returns the extraLong3 duration token.
    ///
    /// @return the extraLong3 duration token in milliseconds
    int extraLong3();

    /// Returns the extraLong4 duration token.
    ///
    /// @return the extraLong4 duration token in milliseconds
    int extraLong4();

    /// Returns the semantic motion scheme used by this profile.
    ///
    /// @return the semantic motion scheme used by this profile
    M3MotionScheme scheme();

    /// Returns the motion-adjacent interaction timings used by this profile.
    ///
    /// @return the motion-adjacent interaction timings used by this profile
    M3MotionBehavior behavior();

    /// Returns the fast effects motion spec.
    ///
    /// @return the fast effects motion spec
    default M3MotionSpec fastEffects() {
        return scheme().fastEffects();
    }

    /// Returns the default effects motion spec.
    ///
    /// @return the default effects motion spec
    default M3MotionSpec defaultEffects() {
        return scheme().defaultEffects();
    }

    /// Returns the slow effects motion spec.
    ///
    /// @return the slow effects motion spec
    default M3MotionSpec slowEffects() {
        return scheme().slowEffects();
    }

    /// Returns the fast spatial motion spec.
    ///
    /// @return the fast spatial motion spec
    default M3MotionSpec fastSpatial() {
        return scheme().fastSpatial();
    }

    /// Returns the default spatial motion spec.
    ///
    /// @return the default spatial motion spec
    default M3MotionSpec defaultSpatial() {
        return scheme().defaultSpatial();
    }

    /// Returns the slow spatial motion spec.
    ///
    /// @return the slow spatial motion spec
    default M3MotionSpec slowSpatial() {
        return scheme().slowSpatial();
    }

    /// Returns the coarse short duration alias.
    ///
    /// @return the coarse short duration alias in milliseconds
    default int shortDuration() {
        return short2();
    }

    /// Returns the coarse medium duration alias.
    ///
    /// @return the coarse medium duration alias in milliseconds
    default int mediumDuration() {
        return medium1();
    }

    /// Returns the coarse long duration alias.
    ///
    /// @return the coarse long duration alias in milliseconds
    default int longDuration() {
        return long2();
    }

    /// Creates a builder initialized from an existing motion token set.
    ///
    /// @param tokens the motion tokens to copy
    /// @return a mutable motion-token builder
    static M3MotionTokensBuilder builder(M3MotionTokens tokens) {
        return new M3MotionTokensBuilder(tokens);
    }

    /// Creates motion duration tokens from coarse duration values.
    ///
    /// @param shortDuration the duration applied to all short duration tokens
    /// @param mediumDuration the duration applied to all medium duration tokens
    /// @param longDuration the duration applied to all long and extra-long duration tokens
    /// @return immutable motion duration tokens
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
                M3MotionScheme.standard(),
                M3MotionBehavior.standard()
        );
    }

    /// Creates motion duration tokens.
    ///
    /// @param short1 the short1 duration token in milliseconds
    /// @param short2 the short2 duration token in milliseconds
    /// @param short3 the short3 duration token in milliseconds
    /// @param short4 the short4 duration token in milliseconds
    /// @param medium1 the medium1 duration token in milliseconds
    /// @param medium2 the medium2 duration token in milliseconds
    /// @param medium3 the medium3 duration token in milliseconds
    /// @param medium4 the medium4 duration token in milliseconds
    /// @param long1 the long1 duration token in milliseconds
    /// @param long2 the long2 duration token in milliseconds
    /// @param long3 the long3 duration token in milliseconds
    /// @param long4 the long4 duration token in milliseconds
    /// @param extraLong1 the extraLong1 duration token in milliseconds
    /// @param extraLong2 the extraLong2 duration token in milliseconds
    /// @param extraLong3 the extraLong3 duration token in milliseconds
    /// @param extraLong4 the extraLong4 duration token in milliseconds
    /// @return immutable motion duration tokens
    private static M3MotionTokens create(
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
                M3MotionScheme.standard(),
                M3MotionBehavior.standard()
        );
    }

    /// Creates motion duration tokens with a semantic motion scheme.
    ///
    /// @param short1 the short1 duration token in milliseconds
    /// @param short2 the short2 duration token in milliseconds
    /// @param short3 the short3 duration token in milliseconds
    /// @param short4 the short4 duration token in milliseconds
    /// @param medium1 the medium1 duration token in milliseconds
    /// @param medium2 the medium2 duration token in milliseconds
    /// @param medium3 the medium3 duration token in milliseconds
    /// @param medium4 the medium4 duration token in milliseconds
    /// @param long1 the long1 duration token in milliseconds
    /// @param long2 the long2 duration token in milliseconds
    /// @param long3 the long3 duration token in milliseconds
    /// @param long4 the long4 duration token in milliseconds
    /// @param extraLong1 the extraLong1 duration token in milliseconds
    /// @param extraLong2 the extraLong2 duration token in milliseconds
    /// @param extraLong3 the extraLong3 duration token in milliseconds
    /// @param extraLong4 the extraLong4 duration token in milliseconds
    /// @param scheme the semantic motion scheme
    /// @return immutable motion duration tokens
    private static M3MotionTokens create(
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
                scheme,
                M3MotionBehavior.standard()
        );
    }

    /// Creates motion duration tokens with semantic motion and behavior timings.
    ///
    /// @param short1 the short1 duration token in milliseconds
    /// @param short2 the short2 duration token in milliseconds
    /// @param short3 the short3 duration token in milliseconds
    /// @param short4 the short4 duration token in milliseconds
    /// @param medium1 the medium1 duration token in milliseconds
    /// @param medium2 the medium2 duration token in milliseconds
    /// @param medium3 the medium3 duration token in milliseconds
    /// @param medium4 the medium4 duration token in milliseconds
    /// @param long1 the long1 duration token in milliseconds
    /// @param long2 the long2 duration token in milliseconds
    /// @param long3 the long3 duration token in milliseconds
    /// @param long4 the long4 duration token in milliseconds
    /// @param extraLong1 the extraLong1 duration token in milliseconds
    /// @param extraLong2 the extraLong2 duration token in milliseconds
    /// @param extraLong3 the extraLong3 duration token in milliseconds
    /// @param extraLong4 the extraLong4 duration token in milliseconds
    /// @param scheme the semantic motion scheme
    /// @param behavior the motion-adjacent interaction timings
    /// @return immutable motion duration tokens
    private static M3MotionTokens create(
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
            M3MotionScheme scheme,
            M3MotionBehavior behavior
    ) {
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(behavior, "behavior");
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
                scheme,
                behavior
        );
    }

    /// Returns baseline Material Design 3 motion tokens.
    ///
    /// @return baseline Material Design 3 motion tokens
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
    ///
    /// @return expressive Material Design 3 motion tokens
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
                M3MotionScheme.expressive(),
                M3MotionBehavior.expressive()
        );
    }

    /// Converts motion tokens into inline JavaFX CSS declarations.
    ///
    /// @return inline JavaFX CSS declarations for these motion tokens
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        appendCoarseDurationStyleDeclarations(builder);
        appendSchemeStyleDeclarations(builder);
        appendBehaviorStyleDeclarations(builder);
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

    /// Appends coarse duration alias declarations.
    private void appendCoarseDurationStyleDeclarations(StringBuilder builder) {
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

    /// Appends interaction timing declarations.
    private void appendBehaviorStyleDeclarations(StringBuilder builder) {
        M3MotionBehavior behavior = behavior();
        appendDuration(builder, "-m3-motion-tooltip-show-delay", behavior.tooltipShowDelay());
        appendDuration(builder, "-m3-motion-tooltip-hide-delay", behavior.tooltipHideDelay());
        appendDuration(builder, "-m3-motion-tooltip-show-duration", behavior.tooltipShowDuration());
        appendDuration(builder, "-m3-motion-rich-tooltip-show-duration", behavior.richTooltipShowDuration());
        appendDuration(builder, "-m3-motion-sub-menu-hover-open-delay", behavior.subMenuHoverOpenDelay());
        appendDuration(builder, "-m3-motion-sub-menu-hover-close-delay", behavior.subMenuHoverCloseDelay());
        appendDuration(
                builder,
                "-m3-motion-linear-progress-indeterminate-cycle-duration",
                behavior.linearProgressIndeterminateCycleDuration()
        );
        appendDuration(
                builder,
                "-m3-motion-circular-progress-indeterminate-cycle-duration",
                behavior.circularProgressIndeterminateCycleDuration()
        );
    }

    /// Appends declarations for one semantic motion spec.
    private static void appendSpec(StringBuilder builder, String name, M3MotionSpec spec) {
        appendDuration(builder, "-m3-motion-" + name + "-duration", spec.duration());
        M3TokenCss.append(builder, "-m3-motion-" + name + "-easing", spec.easing().tokenName());
    }

    /// Appends one duration declaration in milliseconds.
    private static void appendDuration(StringBuilder builder, String name, javafx.util.Duration duration) {
        M3TokenCss.append(builder, name, M3TokenCss.format(duration.toMillis()) + "ms");
    }
}
