# Testing

M3FX groups tests by execution cost and environment sensitivity. The tasks in
[build.gradle.kts](../build.gradle.kts) select tests by JUnit tags:

| Tier | Command | Selection and scope |
| --- | --- | --- |
| 1: Core | `./gradlew test` | Excludes `tier2` and `tier3`; fast API, token, state, layout, keyboard, focus, CSS, and rendering checks. Some tests briefly open windows or popups. |
| 2: Integration | `./gradlew testTier2` | Includes `tier2`; focused rendering, animation sampling, asynchronous focus, and window-system integration. |
| 3: Visual Matrix | `./gradlew testTier3` | Includes `tier3`; broad visual journeys, real mouse interaction, animation-frame sequences, and cross-state reports. These can take several minutes. |

`check` runs Tier 1 by default. New tests belong there unless their cost or environment sensitivity warrants a
higher tier. Use `@Tier2Test` or `@Tier3Test` from `org.glavo.m3fx.testing` on classes or methods; prefer method-level
placement when other tests in the class remain fast. Keep the tags mutually exclusive: the Gradle filters do not
prevent a test carrying both tags from running in both higher tiers.

## Test Organization

Library suites follow the production packages and group related contracts by control family. Fast property,
selection, layout, and CSS assertions stay with that family; focused motion, focus, and rendering suites cover
behaviors that require a running window. `M3ControlGalleryTest` owns cross-control theme galleries in Tier 3.
Demo suites cover application navigation, adaptive layouts, and presentation matrices rather
than repeating each library contract.

Keep fixtures local to their suite unless several suites use them. Shared control helpers separate construction
and input, resolved styles, layout geometry, motion observation, and pixel assertions. Do not add source scans,
API-name whitelists, or tests of documentation and test infrastructure. Test observable contracts instead.
Use bounded waits for asynchronous behavior, restore global settings, and close windows in `finally`, including
when initialization fails. A snapshot should support a behavioral or pixel assertion, not merely create a file.

Register windows as test-owned before showing them and close only those windows, including hidden ones. Cancel
pulse callbacks on timeout or interruption. Make global motion changes on the FX thread and restore the previous
value. For dispatched input, verify delivery to an application handler rather than the consumed flag of an event
that JavaFX may have copied. Assert visible geometry and state instead of scheduler-dependent layout-pass counts.

## Running Locally

Run commands from the repository root. Unqualified task selectors include matching tasks in the library
and demo. Use a path such as `:demo:testTier2` to target one project. For a full review:

```shell
./gradlew test testTier2 testTier3
```

JavaFX tests require a working display environment, including Tier 1 tests that start the toolkit. On Linux
without a desktop session, use Xvfb, for example `xvfb-run -a ./gradlew test`.
Run packaging separately as described in [PACKAGING.md](PACKAGING.md).

## CI

The [Build and Test workflow](../.github/workflows/demo-shadow-jar.yml) runs on JDK 21 with Xvfb. Every invocation
first runs Tier 1 against JavaFX 17 (`-Pm3fx.javafx.version=17`). Push and pull-request builds then run the default
checks and build the demo Shadow JAR with the default JavaFX 21 dependencies. Manual dispatch additionally
runs Tier 2 and Tier 3 and builds the host-platform demo jlink image. The workflow is the source for exact commands
and artifact uploads.

## Reports

Each project's HTML reports are in `build/reports/tests/<task>/`, with XML results in
`build/test-results/<task>/`, where `<task>` is `test`, `testTier2`, or `testTier3`.
Visual suites also write snapshots and, where supported, HTML indexes to:

- `build/reports/m3fx-visual`
- `demo/build/reports/m3fx-demo-visual`
