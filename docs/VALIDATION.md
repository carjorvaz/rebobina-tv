# Validation

Validation should be quick enough for every agent run and strict enough to catch
drift before it spreads.

## Command Menu

```sh
just --list
```

`just` is the ergonomic command menu. It delegates to the canonical scripts
rather than replacing them, and also exposes `jj-status`, `jj-diff`, and
`jj-ops` for local-history inspection.

## Fast Check

```sh
scripts/validate
```

This checks required repository knowledge files, the Android manifest shape,
basic source expectations, safe-data scans, and structured JSON invariants for
the local catch-up fixture, synthetic provider metadata, the host-side
normalized catalog reference, and provider evidence. It also cross-checks the
runtime provider-route allowlist against fixture routes and recorded provider
evidence, without requiring a full Android build.

The fast check delegates documentation freshness to:

```sh
scripts/check-doc-refs
scripts/generate-source-map docs/generated/source-map.md
```

The first check verifies local Markdown links plus inline documented repo paths
such as `scripts/...`, `docs/...`, and `app/...`. The second keeps the generated
source map in `docs/generated/source-map.md` reproducible.

## Build Check

```sh
scripts/validate --build
```

This runs the fast check and then delegates to `scripts/build-debug`, which uses
the Nix Android shell and Gradle to build the debug APK.

Use the build check when changing:

- Gradle files.
- Android manifest or resources.
- Kotlin source.
- Nix Android tooling.

## CI

GitHub Actions workflows are intentionally absent so this repository cannot
spend Actions minutes by accident. `scripts/validate` fails if a workflow file
appears under `.github/workflows/`. If CI is deliberately enabled later, keep it
explicitly accepted by the repository owner and do not run `scripts/smoke-device`
there because that requires a connected Android TV.

## Device Smoke

When an Android TV device is connected:

```sh
scripts/smoke-device
```

This builds and installs the debug APK, opens Rebobina, verifies stable local
gallery text, drives a named TV focus path to the Watch action, captures a
pre-handoff Rebobina checkpoint for that flow, verifies the `RebobinaHandoff`
log for `digitv://u7d`, waits for the official provider app to settle, asserts
that the provider app becomes the visible TV window, probes stable
non-sensitive destination text, asserts that the direct `digitv://catchup`
control probe returns `Status: ok`, and writes screenshots plus logcat under
ignored `captures/`.

The default UI flow is `channel-watch`. Representative family-facing presets:

```sh
scripts/smoke-device --flow channel-watch
scripts/smoke-device --flow movie-watch --skip-build --no-route-probe
scripts/smoke-device --flow series-watch --skip-build --no-route-probe
```

Useful variants:

```sh
scripts/smoke-device --skip-build
scripts/smoke-device --no-ui-smoke
scripts/smoke-device --app-expect-text Rebobina
scripts/smoke-device --no-app-text-assert
scripts/smoke-device --flow-expect-text Domingão
scripts/smoke-device --no-flow-text-assert
scripts/smoke-device --clear-ui-keyevents --ui-keyevent 23
scripts/smoke-device --handoff-wait-seconds 12
scripts/smoke-device --ui-route 'digitv://u7d'
scripts/smoke-device --ui-expect-text 'Bem-vindo à DIGI TV'
scripts/smoke-device --no-ui-text-assert
scripts/smoke-device --route 'digitv://content/<contentId>/<channelId>'
scripts/smoke-device --no-route-probe
```

Review captured files before sharing them. Screenshots and logs may include
account UI from the provider app.

## Direct Route Probe

For focused route evidence:

```sh
scripts/probe-route --serial bravia-3:5555 'digitv://u7d' u7d
```

The probe writes ignored captures plus `summary.txt`, including Android start
status, foreground evidence, and the captured file list. Use this for route
evidence notes when the full Rebobina UI smoke is unnecessary.

## Redacted Shape Capture

For metadata-shape evidence, use:

```sh
scripts/redact-json-shape INPUT_JSON [OUTPUT_JSON]
some-command-that-emits-json | scripts/redact-json-shape - captures/live-shape.redacted.json
scripts/compare-redacted-shapes docs/references/synthetic-provider-metadata-shape.json captures/live-shape.redacted.json
```

The output records paths, field names, types, counts, and synthetic examples
without copying scalar values from the input.

Use `-` for stdin whenever the source is a live/session provider response. Keep
the raw response out of the repository and review the redacted shape before
promoting it from ignored `captures/` into `docs/references/`.

## Synthetic Normalization Reference

For the safe synthetic fixture only:

```sh
scripts/normalize-synthetic-provider-metadata app/src/main/assets/sample-provider-metadata.json
```

The output mirrors the provider-metadata-to-catalog contract so agents can
inspect normalized days, channels, programmes, and inferred episode neighbours
without launching Android.
