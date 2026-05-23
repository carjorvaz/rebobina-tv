# Harness Engineering For Rebobina

This repo applies the harness-engineering approach from OpenAI's Codex write-up:
humans steer, agents execute, and the repository carries the context needed for
future work.

## What That Means Here

- `AGENTS.md` is a short map.
- `docs/` is the system of record.
- `scripts/validate` is the default feedback loop.
- `scripts/check-doc-refs` keeps local docs and documented repo paths from
  silently drifting.
- `docs/generated/source-map.md` is the generated orientation snapshot for app
  code, harness scripts, fixtures, and knowledge files.
- Architecture rules are written in `ARCHITECTURE.md` and should move into
  mechanical checks when they start repeating.
- Product acceptance criteria live in `docs/product-specs/`, not only in prompts.
- Synthetic metadata normalization has a host-side reference in
  `docs/references/` so agents can inspect adapter output without Android.

## Practical Rules

1. Keep instructions close to the code they govern.
2. Prefer stable Android and Kotlin primitives unless a dependency clearly buys
   enough simplicity to justify itself.
3. Turn recurring review comments into docs or validation checks.
4. Keep validation commands runnable from a clean checkout.
5. Record provider findings without sensitive account or stream data.

## Next Harness Investments

- Add package-boundary checks once the target packages exist.
- Keep expanding `scripts/smoke-device` as provider routes become exact enough
  to assert more destinations.
- Use `scripts/probe-route` for focused route evidence; cite its generated
  `summary.txt` rather than raw captures.
- Keep provider evidence records current for verified, unverified, and rejected
  handoff shapes.
- Keep synthetic normalization references reproducible from local scripts when
  provider-shaped fixtures change.
- Generate an architecture snapshot when the package tree becomes non-trivial.
