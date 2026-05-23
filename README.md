# Rebobina

Standalone Android TV catch-up browser for exploring a better sofa UX while
keeping playback inside the user's official provider app.

Rebobina is independent, provider-neutral software. It is not affiliated with,
endorsed by, or sponsored by Digi or any TV provider.

## Boundary

- Uses Android intents and provider-declared deep links only.
- Does not extract, store, display, or play raw stream URLs.
- Does not bypass DRM, authentication, or provider playback controls.
- Keeps provider tokens and account data out of the project tree.

## Current Goal

Prove the interaction shape for a catch-up browser that is meaningfully better
than a provider day-by-day grid:

- day, channel, and programme rails;
- programme details with progress;
- previous and next episode navigation for series;
- official app handoff for playback.

The current app surface uses a safe local fixture at
`app/src/main/assets/sample-catchup.json` to exercise day, channel, programme,
and episode navigation without storing provider account data or raw stream data.
`app/src/main/assets/sample-provider-metadata.json` is a separate synthetic
provider-like fixture used to prove metadata field mapping before any real
provider metadata is considered.

## Build

```sh
scripts/build-debug
scripts/install-debug
```

For a direct ADB route probe:

```sh
scripts/probe-route 'digitv://catchup'
scripts/probe-sections
```

For a device smoke run with screenshots and logcat:

```sh
scripts/smoke-device
```

By default this verifies the local gallery, drives a remote-key sequence from
the day rail to Watch, verifies the app-side handoff log, and then runs the
direct route probe.

The current Android lab surface targets the `digitv` scheme and the
`ro.digionline.tv` package while the metadata and route shapes are being proven.
The first focused control is the day rail; broad official shortcuts remain
available for Flashback, guide, and search.

## Harness

This repo is set up for agent-first development:

- `AGENTS.md` is the short working map for Codex.
- `docs/` is the source of truth for product, architecture, validation, and
  operating rules.
- `scripts/validate` runs fast repository checks.
- `scripts/validate --build` also builds the debug APK through the Nix Android
  shell.
- `scripts/check-doc-refs` catches stale local documentation links and
  documented repo paths.
- `scripts/generate-source-map` refreshes the checked-in source orientation map.
- GitHub Actions workflows are intentionally absent so nothing can spend Actions
  minutes from this repository.
- `scripts/redact-json-shape` summarizes JSON metadata shapes without copying
  scalar values into repository evidence.
- `scripts/compare-redacted-shapes` compares a redacted live/session shape
  against the synthetic provider baseline.
- `scripts/smoke-device` records local device evidence under ignored `captures/`.

Start with `docs/index.md` when making non-trivial changes.

## License

AGPL-3.0. See `LICENSE`.
