# Testing

M3FX separates tests by execution cost and environment sensitivity. Each tier is mutually exclusive, so a test runs in exactly one tier.

## Tier 1: Core

Tier 1 is the default test suite. It covers deterministic API contracts, tokens, control state, layout invariants, keyboard behavior, focus management, CSS integration, and rendering checks that do not require long-running animation sampling or real-window orchestration.

```shell
./gradlew test
```

`check` includes Tier 1 together with compilation, artifact, publication, and documentation verification. New tests belong in Tier 1 unless they require the capabilities reserved for a higher tier.

## Tier 2: Integration

Tier 2 covers tests that open real JavaFX windows or popups, sample rendered pixels or animation frames, coordinate asynchronous focus, or depend on platform window-system behavior.

```shell
./gradlew testTier2
```

Annotate Tier 2 test classes or methods with `@Tier2Test`. Prefer method-level placement when the rest of a class remains fast and deterministic.

## Tier 3: Visual Matrix

Tier 3 covers the complete demo visual matrix, real mouse interaction, animation-frame sequences, screenshots, and cross-state visual reports. These tests are intentionally comprehensive and can take several minutes.

```shell
./gradlew testTier3
```

Annotate Tier 3 test classes or methods with `@Tier3Test`. Tier 3 should contain broad visual journeys rather than small behavioral assertions that fit Tier 1 or Tier 2.

## Complete Verification

Run all test tiers for release candidates and deliberate full visual review:

```shell
./gradlew fullTest
```

`releaseCheck` includes `fullTest`, publication verification, the demo and catalog shadow jars, and the host-platform jlink runtime image. Push and pull-request CI use Tier 1; manually dispatched CI runs `releaseCheck`.

Gradle writes HTML reports below `build/reports/tests`, `demo/build/reports/tests`, and `catalog/build/reports/tests`, with XML results below the corresponding `build/test-results` directories. Visual suites additionally write reviewable snapshots and indexes below `build/reports/m3fx-visual`, `demo/build/reports/m3fx-demo-visual`, and `catalog/build/reports/catalog-snapshots`.
