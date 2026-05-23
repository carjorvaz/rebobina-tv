# Security

The main security posture is provider respect and data minimization.

## Sensitive Data

Do not commit:

- Provider account tokens or cookies.
- Personal account identifiers.
- Raw stream URLs.
- Private provider API responses.
- Screenshots showing account details.
- Device logs containing credentials or personal data.

## Provider Boundary

Allowed:

- Public Android intents.
- Provider-declared deep links.
- Package-manager metadata available on the local device.
- Redacted notes about observed handoff behavior.

Disallowed:

- DRM bypass.
- Authentication bypass.
- Stream extraction.
- Credential sharing.
- Scraping private APIs.

## Logging

Local logs should describe handoff attempts without storing sensitive payloads.
When logging URIs, prefer redacted shapes or user-controlled lab values.
Runtime handoff code must reject unexpected provider routes before constructing
an Android intent, even when validation already checks the fixture.

## Review Checklist

- Does this change keep playback inside the provider app?
- Does this change avoid storing sensitive provider data?
- Does this change make failures clear without leaking private details?
- Should a repeated security rule move into `scripts/validate`?

## Automated Checks

`scripts/validate` includes a lightweight scan for bearer tokens, token-like
assignments, credential-like assignments, playlist markers, and raw `.m3u8`
URLs. It also regression-tests `scripts/redact-json-shape` against temporary
sensitive-looking JSON values to make sure scalar values are not copied into
shape evidence, checks stdin redaction, compares redacted shape summaries, and
cross-checks runtime route allowlists against fixture and provider evidence
route roots. It is intentionally conservative; human review still owns
screenshots, logcat captures, and provider evidence notes.
