// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.testing;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a rendering or window-integration test that is excluded from the default Tier 1 task.
@NotNullByDefault
@Tag("tier2")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Tier2Test {
}
