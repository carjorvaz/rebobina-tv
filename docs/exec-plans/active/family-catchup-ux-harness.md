# Family Catch-Up UX Harness Plan

> **For Hermes:** Use `subagent-driven-development` for implementation work that changes Android UI, metadata parsing, or smoke-device behavior. Keep each task small and re-run the repo validation loop after every cohesive change.

**Goal:** Make Rebobina a family-friendly Android TV catch-up browser that uses Digi TV's official app capabilities for playback handoff while offering a substantially better local browsing UI for catch-up discovery.

**Architecture:** Rebobina remains metadata-and-handoff only. The UI renders a normalized `CatchupCatalog`; metadata sources normalize into that catalog; Android intents stay behind provider-route guards; playback stays in `ro.digionline.tv`. Harness work should make product intent, safety boundaries, UI behavior, and device evidence legible to future agents.

**Tech Stack:** Plain Android/Kotlin, local JSON fixtures, repo shell scripts, ADB smoke harness, Nix Android build wrapper.

---

## Current State Checked

- `README.md` already states the core goal: day, channel, programme rails; progress; previous/next episode navigation; official app handoff.
- `docs/product-specs/catch-up-handoff-gallery.md` now names the north star: closer to MEO/Vodafone-style Portuguese catch-up UX for a non-technical sofa viewer.
- `MainActivity.kt` currently renders a browse rail with day schedule, movie discovery, and series discovery entries; channel/show group and programme rails; detail panes with progress; previous/next episode buttons for series; and Watch handoff.
- `sample-catchup.json` includes movie, sport, news, and series fixture items with a synthetic E18/E19/E20-style neighbor proof.
- `ProviderMetadataSnapshotSource` proves a safe provider-shaped metadata parser into the same `CatchupCatalog` model, but the app still defaults to the local fixture.
- Provider evidence verifies broad official routes: `digitv://u7d`, `digitv://catchup`, `digitv://epg`, `digitv://search`, plus route-handler-only evidence for `content`, `livestream`, and `catchupstream`.
- Exact playable catch-up handoff is not proven. The active safe Watch target remains broad Flashback (`digitv://u7d`) until real field mapping evidence exists.

## Product Acceptance Criteria

- A non-technical Android TV remote user can answer: "what was on this channel?", "what is the next/previous episode of this show?", and "what films are available?" without scanning a provider day grid.
- The UI exposes channel history, series continuation, and movie discovery as first-class paths.
- The primary Watch action uses the best verified official app route and clearly falls back to broad Flashback/guide/search when exact item routes are unverified.
- The app never stores provider credentials, tokens, account data, APKs, raw stream URLs, playlists, or DRM/license details.
- Every new route or metadata claim has redacted evidence or is explicitly marked unverified.
- Device-facing UI changes include smoke evidence when a TV device is attached.

## Harness Engineering Alignment

This plan follows the OpenAI harness-engineering pattern:

1. **Humans steer, agents execute:** user/product judgment lives in specs and acceptance criteria; agents implement, validate, and update evidence.
2. **Repository knowledge is the system of record:** product intent belongs in `docs/product-specs/`, architecture in `ARCHITECTURE.md`, active work in `docs/exec-plans/active/`, and observations in `docs/research-notes.md` or provider evidence.
3. **Agent legibility over prompt memory:** future agents should recover context from `docs/index.md`, `docs/generated/source-map.md`, and validation scripts rather than chat history.
4. **Enforce invariants mechanically:** when a safety/product rule repeats, add it to `scripts/validate`, `scripts/check-doc-refs`, or the smoke harness.
5. **Application legibility:** expand `scripts/smoke-device` to prove focus paths, screenshots, destination text, and handoff logs for representative family workflows.
6. **Continuous garbage collection:** keep `docs/QUALITY.md` and `docs/exec-plans/tech-debt-tracker.md` honest as the UI and metadata boundaries evolve.

## Phase 1: Product Shape And Fixture Gallery

**Objective:** Make the fixture-backed gallery express the target UX before touching real provider metadata.

**Status:** Implemented on 2026-05-23 for the local fixture: the first rail now exposes **Filmes** and **Séries** discovery entries, films render as an all-movie programme list, and series render show groups with every available fixture episode visible in the programme rail.

**Files:**
- Modify: `app/src/main/assets/sample-catchup.json`
- Modify: `app/src/main/java/com/carjorvaz/rebobina/CatchupCatalog.kt`
- Modify: `app/src/main/java/com/carjorvaz/rebobina/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-pt/strings.xml`
- Modify: `docs/product-specs/catch-up-handoff-gallery.md`

**Tasks:**
1. Add explicit catalog affordances, if needed, for browse groups such as `movie`, `series`, `sport`, and `news` without provider-specific fields leaking into UI.
2. Add a fixture-backed movie discovery path so films can be found without scanning every day/channel pair.
3. Add a fixture-backed series continuation path that makes all available episodes for the selected show visible, not only previous/next buttons.
4. Keep the current per-day/per-channel/per-programme path intact; it is still the simplest mental model for catch-up.
5. Use Portuguese-first strings for family-facing labels; keep default and `values-pt` resource keys in lockstep.
6. Validate with `scripts/validate`; use `scripts/validate --build` for Kotlin/resource changes.

## Phase 2: Remote-First Android TV Interaction

**Objective:** Make the UI easier for a non-technical viewer to operate with only D-pad, OK, and Back.

**Status:** Harness presets implemented for the three representative Watch paths. `scripts/smoke-device --flow channel-watch`, `--flow movie-watch`, and `--flow series-watch` each capture a pre-handoff Rebobina checkpoint and then verify the official app handoff. Back now steps left through the remote-first browse levels — detail, programme, group/channel, first browse rail — and returns Movies/Séries discovery back to the normal day schedule before allowing top-level exit. Focus states now use density-aware rings, brighter focused fills, and a small focus lift so the selected remote target is easier to see at TV distance.

**Files:**
- Modify: `app/src/main/java/com/carjorvaz/rebobina/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-pt/strings.xml`
- Modify: `scripts/smoke-device`
- Modify: `docs/VALIDATION.md`

**Tasks:**
1. Define stable focus paths for: open app → choose channel → choose programme → Watch; open app → movie discovery → Watch; open app → series → next episode → Watch.
2. Make selected/focused states highly visible at TV distance.
3. Ensure Back behavior returns to the previous browse level instead of trapping the user.
4. Keep Watch and official shortcut actions visually distinct: Watch is for the selected item; shortcuts are fallback provider destinations.
5. Extend `scripts/smoke-device` with optional key sequences and expected text markers for movie and series flows.
6. Capture screenshots/logs under ignored `captures/` and summarize only safe evidence in docs.

## Phase 3: Metadata Adapter Proof Toward Real Catalogs

**Objective:** Prove that safe provider metadata can populate the richer UX without committing private responses.

**Status:** Synthetic provider metadata now includes a detail-derived RTP2 movie proof: the schedule event omits its discovery category, `eventDetails` supplies the movie classification/title/description, and the normalized reference shows the item in the same `CatchupCatalog` shape with broad Watch route plus separate exact-route candidate.

**Files:**
- Modify: `app/src/main/assets/sample-provider-metadata.json`
- Modify: `app/src/main/java/com/carjorvaz/rebobina/ProviderMetadataSnapshot.kt`
- Modify: `scripts/normalize-synthetic-provider-metadata`
- Modify: `docs/references/synthetic-provider-normalized-catalog.json`
- Modify: `docs/product-specs/metadata-adapter-boundary.md`
- Modify: `docs/exec-plans/active/metadata-adapter-proof.md`

**Tasks:**
1. Extend the synthetic provider fixture only with fields needed for movie/series/channel discovery.
2. Keep all playback-oriented fields out of `CatchupCatalog`.
3. Preserve conservative episode-neighbor inference: infer only when series title and season/episode labels are unambiguous.
4. Regenerate the synthetic normalized catalog reference.
5. Add validation checks for any new fixture invariants.
6. Keep the app default source on `AssetCatchupCatalogSource` until a reviewed provider source exists.

## Phase 4: Exact Handoff Evidence

**Objective:** Move from broad `digitv://u7d` fallback to exact item handoff only when field mapping is proven.

**Files:**
- Modify: `docs/product-specs/provider-evidence/2026-05-17-digionline-tv.json` or add a dated evidence file
- Modify: `docs/research-notes.md`
- Modify: `docs/product-specs/metadata-adapter-boundary.md`
- Modify: `app/src/main/java/com/carjorvaz/rebobina/ProviderRoutes.kt` only if route composition changes

**Tasks:**
1. Use `scripts/probe-route` for single-route evidence and cite generated `summary.txt`, not raw captures.
2. Use `scripts/redact-json-shape` for any metadata-shape evidence; prefer stdin for live/session data.
3. Treat provider route-handler success as weaker than exact field mapping success.
4. Keep `candidateProviderRoute` separate from active `providerRoute` until exact route evidence proves the mapping.
5. Add a dated evidence record for every verified, route-verified, failed, or rejected shape.

## Phase 5: Validation And Quality Gates

**Objective:** Keep the project aligned with harness engineering as the UX gets more ambitious.

**Commands:**

```sh
scripts/validate
scripts/validate --build
scripts/check-doc-refs
scripts/generate-source-map docs/generated/source-map.md
scripts/smoke-device
scripts/probe-route 'digitv://u7d'
scripts/probe-sections
```

**Tasks:**
1. Run `scripts/validate` after doc, fixture, or harness changes.
2. Run `scripts/validate --build` after Kotlin, resource, Gradle, manifest, or Nix Android changes.
3. Run `scripts/smoke-device` when a TV device is connected and UI/handoff behavior changed.
4. Update `docs/QUALITY.md` when product clarity, reliability, or architecture materially improves or degrades.
5. Regenerate `docs/generated/source-map.md` after source layout or doc-file inventory changes.

## Open Questions

- Which exact Digi metadata fields map to playable `catchupstream` route parameters in a real authenticated session?
- Does Digi expose enough non-sensitive metadata for movie grouping and series episode lists without touching playback endpoints?
- Should the first family-facing UI remain a four-column browser, or evolve into a top-level home screen with rows for "Canais", "Séries", and "Filmes"?
- What are the minimum provider destination markers that are safe and stable enough for smoke assertions across app versions?

## Non-Negotiables

- Do not implement playback, DRM handling, stream extraction, or player replacement.
- Do not commit provider credentials, tokens, account data, APKs, raw responses, or raw stream URLs.
- Do not add GitHub Actions workflows without explicit owner approval and billing review.
- Keep public naming provider-neutral even while the current lab target is Digi TV.
