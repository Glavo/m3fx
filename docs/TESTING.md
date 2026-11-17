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

## Running Locally

Run commands from the repository root. Unqualified task selectors include matching tasks in the library, demo,
catalog, and HMCL MD3 demo. Use a path such as `:demo:testTier2` to target one project. For a full review:

```shell
./gradlew test testTier2 testTier3
```

JavaFX tests require a working display environment, including Tier 1 tests that start the toolkit. On Linux
without a desktop session, use Xvfb, for example `xvfb-run -a ./gradlew test`.
Run packaging separately as described in [PACKAGING.md](PACKAGING.md).

## CI

The [Build and Test workflow](../.github/workflows/demo-shadow-jar.yml) runs on JDK 21 with Xvfb. Every invocation
first runs Tier 1 against JavaFX 17 (`-Pm3fx.javafx.version=17`). Push and pull-request builds then run the default
checks and build all sample Shadow JARs with the default JavaFX 21 dependencies. Manual dispatch additionally
runs Tier 2 and Tier 3 and builds the host-platform demo jlink image. The workflow is the source for exact commands
and artifact uploads.

## Reports

Each project's HTML reports are in `build/reports/tests/<task>/`, with XML results in
`build/test-results/<task>/`, where `<task>` is `test`, `testTier2`, or `testTier3`.
Visual suites also write snapshots and, where supported, HTML indexes to:

- `build/reports/m3fx-visual`
- `demo/build/reports/m3fx-demo-visual`
- `catalog/build/reports/catalog-snapshots`
- `hmcl-md3-demo/build/reports/hmcl-snapshots`
