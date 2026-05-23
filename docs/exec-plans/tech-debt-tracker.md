# Tech Debt Tracker

Keep this file focused on cleanup that improves future agent work.

| Item | Status | Why It Matters | Next Step |
| --- | --- | --- | --- |
| Device smoke harness | Added | Agents need to verify TV UI and launch behavior, not only compile. | Use `scripts/smoke-device`; next expand destination assertions per route. |
| Remote-key UI smoke | Added | The harness should prove Rebobina's own buttons fire handoffs. | Keep default focus stable and expand to other buttons as routes become exact. |
| Provider evidence schema | Added | Deep-link claims need structured proof. | Add one evidence record per newly verified route. |
| Route probe summaries | Added | Single-route captures need a compact, citable status file. | Cite `summary.txt` from ignored captures when updating evidence records. |
| Fixture gallery | Added | The app needs a sofa-friendly surface, not only a route probe. | Replace fixture routes with exact verified event routes once IDs are known. |
| Fixture JSON invariants | Added | Fixture drift can break TV navigation or handoff claims silently. | Keep route evidence cross-checks current as new providers are added. |
| Route allowlist cross-check | Added | Runtime launch guards should not drift from fixtures or evidence. | Update evidence before adding allowed provider route roots. |
| Redacted shape capture | Added | Future metadata research needs field evidence without raw provider responses. | Use `scripts/redact-json-shape` before committing shape evidence. |
| Synthetic provider source | Added | Adapter work needs a safe source that normalizes provider-like metadata. | Keep the app default on the local fixture until real metadata is reviewed. |
| Synthetic normalization reference | Added | Agents need to inspect provider-shaped metadata output without launching Android. | Regenerate with `scripts/normalize-synthetic-provider-metadata` when the synthetic fixture changes. |
| Generated source map | Added | Future agents need a quick repo-shape snapshot without re-discovering every file. | Regenerate with `scripts/generate-source-map` when source layout changes. |
| Package-boundary lint | Waiting | The package tree is not large enough yet. | Add checks after `config/`, `handoff/`, `types/`, and `ui/` exist. |
| Secret scanning | Added | Provider data must stay out of the repo. | Expand patterns only when real misses appear. |
| CI | Deferred | Build validation should be available away from one machine, but Actions can spend minutes. | Add workflows only after explicit owner approval and billing review. |
