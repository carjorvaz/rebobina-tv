# Metadata Adapter Proof

Date opened: 2026-05-17

## Goal

Prove whether Rebobina can populate its normalized catch-up catalog from
provider metadata while keeping playback, credentials, and account state inside
the official provider app.

## Non-Goals

- Do not fetch or store playback URLs.
- Do not parse DRM, license, proxy, or entitlement responses.
- Do not commit authenticated raw responses, cookies, tokens, or account IDs.
- Do not replace the provider player.

## Work Items

1. Keep the fixture source as the default.
2. Create a redacted metadata capture workflow that records only field names,
   shapes, counts, and synthetic examples. Added `scripts/redact-json-shape` for
   this; it supports stdin so raw provider responses do not need to be written
   to disk. Added `scripts/compare-redacted-shapes` to compare a redacted
   live/session shape against the synthetic baseline.
3. Prove channel identity mapping from provider metadata into
   `CatchupChannel`. Synthetic proof added through
   `ProviderMetadataSnapshotSource`; validation checks parent/event channel
   consistency.
4. Prove date and programme mapping into `CatchupDay` and `CatchupProgramme`.
   Synthetic proof added through `ProviderMetadataSnapshotParser`; validation
   checks timing, progress, event-detail references, detail-derived discovery
   category overrides, and the host-side normalized catalog reference.
5. Prove whether season and episode neighbours are explicit or must be inferred.
   Synthetic proof currently infers neighbours by series title, season, and
   episode labels; validation requires the E18/E19/E20 chain and regenerated
   normalized output.
6. Prove exact item handoff only if the metadata fields map cleanly to a
   verified provider route. Fake-ID probes now prove the route handlers exist,
   and the synthetic metadata proof emits `candidateProviderRoute` values from
   `routeFields.catchupstream`. A synthetic candidate probe proved the composed
   route shape is accepted by Android/provider routing but still lands on a
   provider error state; the active Watch route remains `digitv://u7d` until
   real field mapping is evidenced.

## Acceptance

- A future adapter implements `CatchupCatalogSource`.
- The UI remains unchanged when swapping fixture data for normalized provider
  metadata. This is now exercisable through the explicit test launch extra
  `rebobina.catalog_source=provider-snapshot` and the smoke harness
  `--catalog-source provider-snapshot` option; the launcher default remains the
  safe local fixture.
- Validation rejects raw streams, tokens, playlists, and credential-like data.
- Evidence documents route and field mapping without private response bodies.
- The synthetic normalized catalog reference stays reproducible with
  `scripts/normalize-synthetic-provider-metadata`.
- Exact-route candidates remain separate from active provider routes until
  field mapping evidence is available.
