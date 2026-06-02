# Architecture

Rebobina is a handoff-first Android TV catch-up browser. Its job is to give the
user a better local browsing surface for catch-up metadata, then hand playback
to the official provider app.

## Core Boundary

The app may:

- Build Android `ACTION_VIEW` intents from user-provided or documented provider
  deep links.
- Use package-manager information to check whether a provider app is installed.
- Launch official provider activities and report whether the handoff succeeded.
- Record local, non-sensitive validation notes that help compare deep-link
  shapes.
- Render normalized programme metadata from safe local fixtures or future
  reviewed metadata adapters.

The app must not:

- Fetch or play media streams directly.
- Store raw stream URLs or provider credentials.
- Circumvent DRM, authentication, geo restrictions, or provider UI.
- Scrape provider apps or private APIs.

## Current Shape

The project is intentionally small:

```text
app/
  src/main/
    AndroidManifest.xml
    assets/sample-catchup.json
    java/com/carjorvaz/rebobina/CatchupCatalog.kt
    java/com/carjorvaz/rebobina/MainActivity.kt
docs/
scripts/
```

`MainActivity` is an Android TV catch-up browser backed by a `CatchupCatalog`
from a `CatchupCatalogSource`. The current source is
`AssetCatchupCatalogSource`, which loads the safe local fixture. It renders day,
channel, programme, and detail panes, including previous/next episode
navigation when the normalized metadata has season neighbours.

`ProviderMetadataSnapshotSource` is a synthetic proof source. It parses
provider-like metadata fields into the same `CatchupCatalog` without exposing
tokens, raw responses, playback URLs, or DRM fields. It is not the default app
source. Debug/test launches can explicitly request it with the
`rebobina.catalog_source=provider-snapshot` intent extra; launcher/default app
starts still load `AssetCatchupCatalogSource`.

Watch and shortcut actions launch provider route shapes through a fixed package
and scheme:

- package: `ro.digionline.tv`
- scheme: `digitv`

Runtime handoff code also rejects malformed or unexpected provider routes before
building an Android intent. Validation still checks the fixture and evidence,
but launch-time guards remain the last local safety boundary.

Current handoffs use broad official sections such as `digitv://u7d`,
`digitv://epg`, and `digitv://search`. Exact event/content handoff remains a
research item until the safe metadata ID mapping is proven.

The adapter rules live in `docs/product-specs/metadata-adapter-boundary.md`.

## Target Packages

Use this package layout when the first real metadata behavior lands:

```text
com.carjorvaz.rebobina
  catalog/     Normalized catch-up metadata and fixture/provider loaders.
  config/      Provider catalogs, editable lab presets, environment flags.
  handoff/     Intent builders, package checks, launch results.
  telemetry/   Structured local logs and validation evidence.
  types/       Shared value objects and sealed outcomes.
  ui/          TV screens, focus behavior, and input adapters.
```

Allowed dependency direction:

```text
types -> catalog -> ui
types -> config -> handoff -> ui
types -> telemetry -> handoff -> ui
```

Rules:

- `types` has no Android UI dependencies.
- `catalog` normalizes provider-specific metadata into UI-safe models and never
  exposes playback URLs or credentials.
- `config` may depend on `types`, but not on `ui`.
- `handoff` owns Android intent construction and package-manager access.
- `ui` renders normalized state and invokes handoff services; it does not parse
  provider-specific API responses.
- `telemetry` records local evidence without sensitive data.

## Boundary Parsing

External data includes user-entered route parameters, package-manager responses,
saved presets, and future remote fixture data. Parse and validate it before
using it in a launch path.

For example, a deep-link launch should flow through:

```text
raw route fields -> encoded Uri path -> fixed package constraint -> launch result
```

Do not let guessed data shapes spread through the codebase. If a provider shape
is unknown, record it as unknown in the product spec or evidence log.

## Validation Loop

Every change should be checkable by an agent:

1. Run `scripts/validate`.
2. For Android source changes, run `scripts/validate --build`.
3. When an Android TV device or emulator is available, install the debug APK and
   capture the handoff result with `adb logcat` or screenshots.
4. Promote repeated manual checks into scripts.

See `docs/VALIDATION.md` for command details.
