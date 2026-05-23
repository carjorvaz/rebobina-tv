# Reliability

Rebobina reliability is about predictable handoff behavior and clear failure
reporting, not direct playback reliability.

## Expected Failure Modes

- Provider app is not installed.
- Provider app does not claim a URI shape.
- Provider app opens but falls back to a generic screen.
- Provider app requires sign-in or entitlement.
- URI shape changes across provider app versions.
- Android TV focus or input behavior makes recovery awkward.

## Local Validation

Fast harness:

```sh
scripts/validate
```

Build harness:

```sh
scripts/validate --build
```

Device validation, once available:

```sh
scripts/smoke-device
```

The smoke script installs the debug APK, launches Rebobina, verifies stable
local gallery text, drives the TV focus path to Watch with remote key events,
asserts that the provider app becomes the visible TV window, checks stable
non-sensitive destination text, asserts the direct provider route probe when it
is enabled, and captures local evidence under ignored `captures/`.

## Evidence Rules

- Record the exact date of provider observations.
- Record app versions when available.
- Redact account identifiers, tokens, and private URLs.
- Treat "opens provider app" and "opens exact catch-up event" as different
  evidence levels.
- Record durable route findings in `docs/product-specs/provider-evidence/`.
