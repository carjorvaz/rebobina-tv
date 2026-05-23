# Repository Harness

Date: 2026-05-17

## Goal

Apply harness-engineering practices to Rebobina before the Android TV lab grows:
short agent map, docs as source of truth, mechanical validation, and a minimal
bootable app surface.

## Changes

- Added `AGENTS.md` as the concise agent entrypoint.
- Added a structured `docs/` knowledge base.
- Added architecture, validation, quality, reliability, and security docs.
- Added or preserved an Android TV route lab for launching provider deep links
  through the current `digitv`/`ro.digionline.tv` target.
- Added `scripts/validate` for fast checks and optional Android build checks.

## Validation

Run:

```sh
scripts/validate
scripts/validate --build
```

The build command depends on local Nix/Android SDK availability.
