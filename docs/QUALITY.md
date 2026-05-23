# Quality

This scorecard makes gaps visible to humans and agents. Update it when a domain
meaningfully improves or degrades.

| Area | Current Grade | Evidence | Gap |
| --- | --- | --- | --- |
| Repository map | A | `AGENTS.md`, `docs/index.md`, `docs/generated/source-map.md`, `scripts/check-doc-refs` | Needs deeper package-boundary snapshots once package tree grows. |
| Product clarity | B | Catch-up handoff spec, fixture gallery, and provider evidence exist | Needs more exact route evidence entries. |
| Android build | B | Local build validation is reproducible through Nix | Needs release packaging checks later. |
| Architecture | B | Boundaries documented | Needs package-boundary checks once packages exist. |
| Security boundary | B | Security doc, README boundary, and basic scan | Needs review of captured device evidence before sharing. |
| Reliability | B | Validation and adb smoke scripts exist | Needs routine device evidence for each route. |

## Quality Bar

Before marking substantial work complete:

- The relevant product spec is updated.
- `scripts/validate` passes.
- Android changes have run `scripts/validate --build` or record a blocker.
- Device-facing changes run `scripts/smoke-device` when a TV device is attached.
- New provider behavior has evidence or is explicitly marked unverified.
- New recurring rules are added to docs or validation.
