# Device Evidence Harness

Date: 2026-05-17

## Goal

Extend the repository harness beyond compile-time checks so provider handoff work
can be verified on the connected Android TV device and recorded without storing
sensitive provider data.

## Changes

- Added `.ignore` so local build and capture artifacts stay out of agent search.
- Added `scripts/smoke-device` for debug install, Rebobina launch, optional
  provider route probe, screenshot capture, and logcat capture.
- Extended `scripts/smoke-device` to drive Rebobina's focused TV button with a
  remote key event and verify app-side handoff logs.
- Added `docs/product-specs/provider-evidence.schema.json`.
- Added redacted provider evidence under `docs/product-specs/provider-evidence/`.
- Added lightweight sensitive-data checks to `scripts/validate`.
- Documented the smoke and evidence workflow in validation, reliability,
  security, product, and research docs.

## Validation

Ran:

```sh
scripts/validate
scripts/smoke-device --serial bravia-3:5555
scripts/validate --build
```

The smoke run wrote ignored local captures to
`captures/smoke-20260517T150745Z`. Its text result showed:

- debug APK install: `Success`
- Rebobina launch: `Status: ok`
- provider route probe: `digitv://catchup` opened
  `ro.digionline.tv/.MainActivity` with `Status: ok`

A later smoke run wrote `captures/smoke-20260517T151755Z` and verified the
remote-key UI path:

- Rebobina launch: `Status: ok`
- UI smoke status: `ok`
- focused button route: `digitv://u7d`
- `RebobinaHandoff` log: `handoff_result status=requested uri=digitv://u7d`
- direct `digitv://catchup` control probe: `Status: ok`

Another smoke run wrote `captures/smoke-20260517T152028Z` and tightened the
assertion:

- UI smoke status: `ok`
- UI foreground status: `ok`
- resumed activity: `ro.digionline.tv/.MainActivity`

The latest smoke run wrote `captures/smoke-20260517T152331Z` and added safe
destination text assertions:

- UI text status: `ok`
- markers: `Flashback`, `Filmes dos`

The restored final app state then wrote `captures/smoke-20260517T152616Z` and
repeated the same checks successfully.

Screenshots and logcat stay under ignored `captures/` and must be reviewed
before sharing.
