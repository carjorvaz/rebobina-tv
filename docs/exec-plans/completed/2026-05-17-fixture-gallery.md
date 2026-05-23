# Fixture-Backed Catch-Up Gallery

Date: 2026-05-17

## Goal

Move Rebobina from a route-form lab toward the actual sofa-friendly catch-up
gallery while keeping all metadata safe and local.

## Changes

- Used `app/src/main/assets/sample-catchup.json` as the local catch-up fixture.
- Kept fixture parsing in `CatchupCatalog.kt`.
- Made `MainActivity` render day, channel, programme, and detail columns.
- Started focus on the day rail and made the TV smoke drive the same
  day-to-channel-to-programme-to-Watch path a viewer would use.
- Added validation checks for the catalog loader and safe local fixture.
- Updated product and architecture docs to describe the fixture-backed gallery.

## Validation

Ran:

```sh
scripts/validate
scripts/validate --build
scripts/smoke-device --serial bravia-3:5555
```

The latest BRAVIA smoke wrote `captures/smoke-20260517T162025Z` and showed:

- local gallery markers before handoff: `Hoje`, `RTP 1`, `O Diabo Veste Prada`,
  `Ver`
- smoke asserts stable local gallery markers before the remote key sequence
- remote path: `DPAD_RIGHT` x4, then `DPAD_CENTER`
- handoff log: `digitv://u7d`
- runtime route guard still allowed the verified provider route
- provider foreground status: `ok` with activity/window evidence
- provider destination marker: `FLASHBACK`
- route probe status: `ok` for `digitv://catchup`

Captures remain ignored because screenshots and logcat may include provider UI.
