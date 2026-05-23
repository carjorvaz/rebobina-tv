# Metadata Adapter Boundary

## Goal

Define the boundary between provider-specific metadata and Rebobina's normalized
catch-up catalog.

This is not a playback adapter. It is a metadata-only contract for turning safe
programme information into the day, channel, programme, and episode navigation
that Rebobina renders.

## Allowed Inputs

A provider metadata adapter may normalize:

- day/date lists;
- channel IDs, names, numbers, and logos;
- programme IDs, titles, timing, category labels, descriptions, and progress;
- season and episode labels;
- availability windows;
- provider-declared deep-link route parameters when they are proven by evidence.

The adapter may not expose:

- bearer tokens, cookies, account IDs, or device secrets;
- raw media URLs or playlist content;
- DRM license URLs, keys, or entitlement responses;
- playback proxy URLs;
- private raw API responses in repository fixtures.

## Normalized Catalog

Adapters must produce the same shape as `sample-catchup.json`:

- `days`: stable `id`, viewer label, and subtitle;
- `channels`: stable `id`, display name, number, and compact badge;
- `programmes`: stable `id`, `dayId`, `channelId`, start/end, title, subtitle,
  kind, description, progress, optional season/episode, optional previous/next
  programme IDs, a provider route, and optional exact-route candidate.

The UI should not know whether the catalog came from the safe fixture or from a
future provider metadata source.

## Observed Provider Metadata Shapes

Static inspection suggests these metadata endpoints are relevant:

- `/api/catchup/home`
- `/api/catchup/byDate/<date>`
- `/api/epg/events?date=<date>`
- `/api/epg-events/<contentId>`
- `/api/channel?common_id=<channelId>`
- `/api/catchup/event?common_id=<channelId>&event_id=<eventId>`

Useful fields observed in the app bundle:

- `common_id`
- `start_time`
- `end_time`
- `title`
- `progress`
- `duration`
- `watchInfo`
- `seasonLabel`
- `episodeLabel`
- `available_start`
- `available_end`

Playback-oriented fields observed in nearby code paths remain out of scope and
must be dropped before data reaches Rebobina's catalog model.

## Redacted Shape Capture

Use `scripts/redact-json-shape` for any future provider metadata capture that
needs to be reviewed in the repository:

```sh
scripts/redact-json-shape local-provider-response.json > docs/references/provider-shape.json
```

Prefer stdin when handling a live/session response so the raw JSON does not need
to be written to disk:

```sh
some-command-that-emits-json | scripts/redact-json-shape - captures/provider-shape.redacted.json
```

Then compare the redacted shape against the synthetic baseline:

```sh
scripts/compare-redacted-shapes \
  docs/references/synthetic-provider-metadata-shape.json \
  captures/provider-shape.redacted.json
```

The output records JSON paths, field names, types, array lengths, string length
ranges, and synthetic examples. It must not include scalar values from the input
response.

## Episode Navigation

Previous/next episode links should be explicit when the provider metadata gives
stable episode neighbours. If it does not, a future adapter may infer neighbours
only after normalization by grouping programmes with the same series title and
ordered season/episode labels.

Inference must be conservative. If the season/episode labels are missing or
ambiguous, omit previous/next links instead of guessing.

## Handoff Strategy

Use broad verified routes while exact item routes are unproven:

- `digitv://u7d`
- `digitv://epg`
- `digitv://search`
- `digitv://catchup`

Exact content or catch-up event routes require separate evidence before being
used as Watch targets. Live-channel and catch-up event route handlers now have
fake-ID and synthetic-candidate evidence, but a successful route parse is not
enough; the evidence must show which normalized metadata fields map to the route
parameters.

`providerRoute` is the active Watch target. `candidateProviderRoute` is allowed
only as review evidence for exact route composition. It must not replace
`providerRoute` until the field mapping is backed by provider evidence.

## Current Implementation

`CatchupCatalogSource` is the source boundary. The current implementation is
`AssetCatchupCatalogSource`, which loads `sample-catchup.json`. The synthetic
provider proof uses `ProviderMetadataSnapshotSource`, which loads
`sample-provider-metadata.json` and normalizes it through
`ProviderMetadataSnapshotParser` into the same `CatchupCatalog` shape. The app
still defaults to the safe local fixture until a reviewed provider source is
ready.

`ProviderRouteComposer` builds official-app deep links with path-segment
encoding. The synthetic metadata proof now carries `routeFields.catchupstream`
values and emits `candidateProviderRoute` values, while keeping the active
`providerRoute` on `digitv://u7d`. The provider accepts the composed synthetic
candidate route shape but returns its own error state, so this remains
route-shape evidence rather than exact playable-item proof.

Validation keeps the synthetic proof honest by checking source-shape labels,
channel/event identity consistency, event-detail references, normalizable timing
and progress, the synthetic E18/E19/E20 neighbour chain, synthetic exact-route
candidate fields, and the checked-in host-side normalized catalog reference.

The synthetic parser deliberately keeps `digitv://u7d` as the default Watch
route until exact event/content routes have separate field-mapping evidence.
