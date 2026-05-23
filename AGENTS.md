# Agent Notes

Rebobina is a standalone Android TV handoff lab. It exists to test whether a
better catch-up browsing UX can open the user's official provider app at the
right place.

## Rules

- Keep this repository separate from `cl-ott-tv`.
- Do not commit provider credentials, tokens, APKs, account data, or raw stream
  URLs.
- Do not implement playback, DRM handling, stream extraction, or player
  replacement.
- Use Android intents and declared deep links for handoff experiments.
- Keep public naming provider-neutral.
- Do not add GitHub Actions workflows without explicit owner approval and
  billing review.

## Useful Commands

```sh
scripts/validate
scripts/validate --build
scripts/check-doc-refs
scripts/generate-source-map docs/generated/source-map.md
scripts/build-debug
scripts/install-debug
scripts/smoke-device
scripts/redact-json-shape app/src/main/assets/sample-catchup.json
scripts/redact-json-shape - captures/live-shape.redacted.json
scripts/compare-redacted-shapes docs/references/synthetic-provider-metadata-shape.json captures/live-shape.redacted.json
scripts/normalize-synthetic-provider-metadata app/src/main/assets/sample-provider-metadata.json
scripts/probe-route 'digitv://catchup'
scripts/probe-sections
```
