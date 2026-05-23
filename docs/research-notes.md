# Research Notes

These notes capture local interoperability findings for a paid Android TV app
already installed on the test TV. Keep secrets, tokens, account data, APKs, and
raw stream URLs out of this repository.

## Observed Target

- Android package: `ro.digionline.tv`
- Exported activity: `ro.digionline.tv/.MainActivity`
- Declared link scheme: `digitv:`
- App shape: Expo / React Native Android TV app
- Observed runtime version on the test device: `2.7.0`

## Deep Links

The embedded React Navigation config exposed these route shapes:

- `digitv://livestream/:channelId`
- `digitv://content/:contentId/:channelId/:progress?/:duration?`
- `digitv://catchupstream/:epgItem?/:id/:name/:logo`
- `digitv://catchup`
- `digitv://u7d`
- `digitv://epg`
- `digitv://search`

ADB confirmed that Android resolves `digitv://catchup` to the target activity.
ADB also confirmed that `digitv://content/123456/7890` is parsed by the target
app and reaches an attempted playback/content state. With fake IDs, the target
app displayed an unavailable-channel message, which is useful evidence that the
route is real but not useful as playback data.

On 2026-05-17, fake item-route probes added the same level of evidence for the
remaining item routes. `digitv://livestream/fake-channel` reached the provider
app and displayed its own unavailable-channel state. A synthetic
`digitv://catchupstream/fake-epg/fake-channel/Fake%20Programme/fake-logo` route
reached the provider app and displayed its own generic error state. These probes
prove route-handler behavior only; they do not prove real channel/event field
mappings or any playable handoff.

On 2026-05-17, Rebobina debug build `0.1.0` was installed on a Sony BRAVIA
Android TV test device. Pressing the Catch-up handoff button opened the target
app's catch-up date/channel grid through `digitv://catchup`.

Later on 2026-05-17, `scripts/probe-sections` verified the broad section routes:

- `digitv://` opens the provider home screen.
- `digitv://catchup` opens the date/channel catch-up grid.
- `digitv://u7d` opens the provider Flashback gallery for films and series from
  the last 7 days.
- `digitv://epg` opens the TV guide grid.
- `digitv://search` opens the search screen with popular-title gallery.

The `u7d` route is the strongest immediate UX workaround because it opens an
official gallery instead of the day-by-day catch-up grid.

Structured, redacted evidence for these findings now lives in
`docs/product-specs/provider-evidence/`.

On 2026-05-17 at 15:07:54Z, `scripts/smoke-device --serial bravia-3:5555`
installed the debug APK, launched Rebobina, and opened
`ro.digionline.tv/.MainActivity` with `digitv://catchup` and `Status: ok`.

On 2026-05-17 at 15:18:11Z, the same smoke script drove Rebobina's focused TV
button with `DPAD_CENTER`. Rebobina logged `handoff_start` and
`handoff_result status=requested` for `digitv://u7d`, then the direct
`digitv://catchup` control probe also returned `Status: ok`.

On 2026-05-17 at 15:20:48Z, `scripts/smoke-device --serial bravia-3:5555`
added a system foreground assertion. After the Rebobina `DPAD_CENTER` handoff,
Android reported `ro.digionline.tv/.MainActivity` as `mResumedActivity`.

On 2026-05-17 at 15:23:51Z, the smoke script also verified non-sensitive
destination markers in the provider UI dump: `Flashback` and `Filmes dos`.

On 2026-05-17 at 15:26:36Z, the restored final app state repeated the same
BRAVIA smoke successfully: Rebobina handoff log ok, provider foreground ok, and
destination text ok.

On 2026-05-17 at 15:36:11Z, the fixture-backed gallery became the tested app
surface. The pre-handoff UI dump showed local fixture markers including `Hoje`,
`RTP 1`, `O Diabo Veste Prada`, and `Ver`. Pressing `DPAD_CENTER` on the
focused Watch action logged `digitv://u7d`, foregrounded the provider app, and
again verified `Flashback` plus `Filmes dos` in the provider UI.

On 2026-05-17 at 15:46:42Z, the smoke matched the current TV focus model: it
verified the local gallery markers, sent four `DPAD_RIGHT` events and
`DPAD_CENTER` to move from the day rail to Watch, recorded the `digitv://u7d`
handoff log, asserted provider visibility with activity/window evidence, and
again verified `Flashback` plus `Filmes dos`. The same run asserted the direct
`digitv://catchup` control probe with `Status: ok`.

On 2026-05-17 at 16:01:13Z, the smoke was repeated after the richer detail-pane
prototype and focus reset hardening. It verified local markers, sent
`DPAD_RIGHT DPAD_RIGHT DPAD_RIGHT DPAD_RIGHT DPAD_CENTER`, logged the
`digitv://u7d` handoff, asserted `ro.digionline.tv/.MainActivity` foreground,
found the provider `FLASHBACK` navigation marker, and confirmed the direct
`digitv://catchup` route probe with `Status: ok`.

On 2026-05-17 at 16:14:56Z, after adding an explicit `CatchupCatalogSource`
boundary and row-level left/right focus routing, the same smoke passed again.
This caught and fixed a focus-finder issue where cold launches could leave
focus on the day rail after `DPAD_RIGHT` events.

On 2026-05-17 at 16:20:49Z, after adding runtime provider-route guards, the
same smoke still recorded the `digitv://u7d` handoff, asserted
`ro.digionline.tv/.MainActivity` foreground, verified the provider `FLASHBACK`
navigation marker, and confirmed the direct `digitv://catchup` route probe with
`Status: ok`.

## Boundary

Rebobina is a handoff lab. It should compose metadata routes and ask Android to
open the official app. It should not fetch playback URLs, handle DRM licenses,
or replace the provider player.

The useful next proof is not stream playback. It is finding the exact metadata
ID fields that correspond to:

- channel IDs for live handoff;
- content IDs and channel IDs for content handoff;
- catch-up event IDs, names, logos, and optional EPG item values.

The fake `livestream` and `catchupstream` probes narrow the problem: Rebobina no
longer needs to prove that those route handlers exist. The remaining hard part
is metadata identity mapping, especially which safe catalog fields correspond to
`channelId`, `epgItem`, `id`, `name`, and `logo`.

## Metadata Surface

Static inspection of the installed provider app suggests a richer metadata
surface than the visible Flashback route exposes. Keep this as design input
only until an authenticated, reviewable adapter boundary exists.

Observed metadata endpoint shapes include:

- `/api/catchup/home`
- `/api/catchup/byDate/<date>`
- `/api/epg/events?date=<date>`
- `/api/epg-events/<contentId>`
- `/api/channel?common_id=<channelId>`
- `/api/catchup/event?common_id=<channelId>&event_id=<eventId>`

Common request parameters include device/platform/app/language/timestamp
metadata. Do not commit tokens, cookies, account identifiers, raw responses, or
playback URLs.

The useful fields for Rebobina's normalized model appear to be:

- channel identity: `common_id`, channel name, channel logo;
- programme timing: `start_time`, `end_time`, `duration`;
- programme display: `title`, category/labels, description-like event fields;
- continuation state: `progress`;
- series metadata: season and episode labels when present;
- detail joins: event, channel, and watch-info objects.

The important product conclusion: Flashback is only a broad official shortcut.
The better UX likely needs a local metadata gallery that can group by day,
channel, programme, and series, then hand off to the official app only at the
moment of playback.

On 2026-05-17, Rebobina added a synthetic provider metadata proof:
`sample-provider-metadata.json` plus `ProviderMetadataSnapshotSource`. It maps
safe provider-like field names into `CatchupCatalog`, including conservative
episode-neighbour inference from series title, season label, and episode label.
This is a parser proof only, not a real provider metadata capture.

Later on 2026-05-17, Rebobina added a host-side normalization reference for the
same synthetic fixture. `scripts/normalize-synthetic-provider-metadata` only
accepts the synthetic fixture marker and regenerates
`docs/references/synthetic-provider-normalized-catalog.json`, letting agents
inspect the normalized catalog contract without launching Android or touching
private provider responses.

Rebobina also added `scripts/generate-source-map` and
`docs/generated/source-map.md` so future agents can scan the current app files,
harness scripts, fixtures, and knowledge base from one reproducible local
snapshot.

On 2026-05-17 at 17:04:53Z, `scripts/probe-route --serial bravia-3:5555
'digitv://u7d' u7d-summary` verified the focused route-probe summary path. The
capture `captures/20260517T170453Z-u7d-summary/summary.txt` recorded route
probe status `ok`, Android start `Status: ok`, activity
`ro.digionline.tv/.MainActivity`, and provider foreground evidence from both
activity and window-manager focus.

On 2026-05-17 at 17:08:55Z, a synthetic exact-route candidate from the
normalized catalog was probed:
`digitv://catchupstream/epg-20260517-rtp1-1505/evt-rtp1-1505/O%20Diabo%20Veste%20Prada/rtp1-logo`.
The provider accepted the route shape, returned Android `Status: ok`, and
foregrounded `ro.digionline.tv/.MainActivity`, while the provider UI showed its
own generic error message. This is useful route-shape evidence, not proof that
synthetic metadata values map to a playable catch-up item.
