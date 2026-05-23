# Catch-Up Browser And Handoff

## Goal

Build a sofa-friendly catch-up browser with per-day, per-channel, per-programme,
and per-series navigation while keeping playback inside the official provider
app.

## Product North Star

Rebobina should make Digi TV catch-up feel closer to the stronger Portuguese TV
experiences from MEO and Vodafone: easy channel browsing, obvious movie and
series discovery, clear next/previous episode movement, and a simple Watch
handoff that opens the official provider app at the best available destination.

The primary usability benchmark is a non-technical sofa viewer using only an
Android TV remote. The UI should reduce the work needed to answer common
questions such as "what was on this channel?", "is there another episode of
this show?", and "what films are available?" without requiring provider-account
data, raw streams, or playback replacement in Rebobina.

The current prototype is fixture-backed. It proves the TV interaction model
without storing provider account data, playback URLs, or private backend
artifacts. The first rail now includes explicit discovery entries for films and
series before the normal day list: **Filmes** opens an all-film programme list,
and **Séries** opens show groups in the second rail with every fixture episode
for the selected show visible in the programme rail.

## In Scope

- Local metadata browsing by day, channel, and programme.
- Detail views with programme metadata, progress, and season/episode neighbors.
- Browse affordances for the catch-up tasks that matter most on TV: per-channel
  history, series continuation, and movie discovery.
- Broad official handoff routes when exact event handoff is unavailable.
- Clear local reporting for failed or unavailable handoffs.
- The current lab target is package `ro.digionline.tv` with scheme `digitv`.
- A future metadata adapter boundary that can be reviewed independently before
  any authenticated provider metadata access is attempted.

## Out Of Scope

- Direct playback.
- Raw stream extraction or display.
- Provider credential storage.
- DRM, entitlement, or geo-restriction bypass.
- Shipping code that scrapes private provider APIs.

## Acceptance Criteria

- A user can browse safe local catch-up metadata by day, channel, and programme
  from Android TV.
- Series details expose previous and next episode navigation when those
  neighbours are present in the metadata, and the series discovery path lists
  every available fixture episode for the selected show in the programme rail.
- Movie programmes can be discovered without scanning every channel/day row once
  the metadata marks them as `kind: movie`.
- The primary Watch action opens the official provider app through a
  provider-declared route.
- The app constrains handoffs to the current provider package.
- The app rejects unsafe or unexpected provider routes before launching an
  Android intent.
- Failed handoffs return readable local feedback.
- Verified provider deep-link shapes are recorded with the date, device/app
  version, and result under `provider-evidence/`.
- Unverified shapes remain clearly marked as unverified.

## Metadata Model

The local fixture uses the smallest useful product shape:

- days: stable IDs, labels, and display subtitles;
- channels: stable IDs, numbers, names, and compact badges;
- programmes: day/channel IDs, start/end times, title, subtitle, kind,
  description, progress, optional season/episode fields, optional previous/next
  programme IDs, and a safe provider route.

Real provider metadata must be normalized into this shape before the UI sees it.
Playback responses, raw URLs, licenses, cookies, and tokens stay outside this
model. `scripts/validate` enforces fixture invariants for unique IDs, valid
day/channel references, previous/next programme references, time/progress
format, safe `digitv://` routes, matching provider evidence records, and app
runtime allowlist coverage.

## Evidence Model

Each provider finding should record:

- Provider name.
- Provider app package.
- Deep-link purpose: live, content, catch-up event, or section.
- URI shape with sensitive identifiers redacted.
- Device or emulator used.
- Provider app version if available.
- Result: verified, failed, unavailable, or unknown.
- Date observed.
- Notes that do not include private account data.

Use `provider-evidence.schema.json` and keep raw captures under ignored
`captures/`.
