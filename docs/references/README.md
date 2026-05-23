# References

Use this directory for safe, repo-local reference material that future agents may
need. Prefer short summaries and links over large copied documents.

Do not store provider account data, raw stream URLs, or private API captures.

- `synthetic-provider-metadata-shape.json` is generated from
  `app/src/main/assets/sample-provider-metadata.json` with
  `scripts/redact-json-shape`. It is safe because it stores paths, types, and
  synthetic examples, not scalar values from the source.
- `synthetic-provider-normalized-catalog.json` is generated from the same
  synthetic fixture with `scripts/normalize-synthetic-provider-metadata`. It is
  a host-side contract for the normalized catalog shape and should not be
  generated from private provider responses.
