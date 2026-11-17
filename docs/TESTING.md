# Testing

M3FX separates tests by execution cost and environment sensitivity. Each tier is mutually exclusive, so a test runs in exactly one tier.

## Tier 1: Core

Tier 1 is the default test suite. It covers deterministic API contracts, tokens, control state, layout invariants, keyboard behavior, focus management, CSS integration, and rendering checks that do not require long-running animation sampling or real-window orchestration.

```shell
./gradlew test
```

`check` runs Tier 1 by default. Run packaging tasks separately as described in [PACKAGING.md](PACKAGING.md).
New tests belong in Tier 1 unless they require the capabilities reserved for a higher tier.

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

## Full Test Suite And CI

Run all test tiers for release candidates and deliberate full visual review:

```shell
./gradlew test testTier2 testTier3
```

Run commands from the repository root. Unqualified task selectors include matching tasks in all subprojects, so this
command covers the library, demo, catalog, and HMCL MD3 demo. Use a path such as `:demo:testTier2` to target one project.

Push and pull-request CI run Tier 1 and build the sample shadow jars:

```shell
./gradlew check shadowJar
```

Manually dispatched CI also runs Tier 2 and Tier 3 and builds the host-platform demo runtime image:

```shell
./gradlew check testTier2 testTier3 shadowJar :demo:jlinkRuntime
```

Gradle writes HTML reports below each project's `build/reports/tests` directory, with XML results below the
corresponding `build/test-results` directory. Visual suites additionally write reviewable snapshots and indexes
below `build/reports/m3fx-visual`, `demo/build/reports/m3fx-demo-visual`,
`catalog/build/reports/catalog-snapshots`, and `hmcl-md3-demo/build/reports/hmcl-snapshots`.
