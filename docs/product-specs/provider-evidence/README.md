# Provider Evidence

This directory holds redacted evidence for provider-declared deep-link behavior.
Use `../provider-evidence.schema.json` for the expected shape.

Rules:

- Record route shapes with placeholders, not private identifiers.
- Use `verified_exact` only when the target provider app opens the intended
  screen or event.
- Use `route_verified` when Android and the provider app accept the route shape
  but test values do not prove exact playable content.
- Keep screenshots, logcat output, and raw captures under ignored `captures/`.
- Never record raw streams, credentials, cookies, account IDs, or private API
  responses.
- Prefer `scripts/probe-route` for single-route evidence because its
  `summary.txt` records Android start status and foreground evidence without
  moving raw captures into docs.
