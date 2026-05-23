# Legal And Publication Notes

Last reviewed: 2026-05-23

This is a practical project-risk note, not legal advice. The safest public
posture for Rebobina is to publish only the independently written handoff UX,
synthetic fixtures, validation harnesses, and redacted route evidence while
leaving playback, credentials, provider account state, raw provider responses,
APK artifacts, and stream/DRM material out of the repository.

## Sources Checked

Primary Portugal sources:

- DIGI Portugal `Condições Gerais e Específicas do Contrato de Prestação de
  Serviços de Comunicações Eletrónicas`, at
  `https://www.digi.pt/legal/general-terms`.
  - PDF observed from DIGI Portugal legal page.
  - Publication date shown in the PDF: 2024-11-04.
  - Update dates shown in the PDF: 2025-06-26 and 2026-04-27.
- DIGI Portugal FAQ, `Como posso descarregar e instalar a aplicação DIGI TV?`,
  at
  `https://www.digi.pt/perguntas-frequentes/tv/como-posso-descarregar-e-instalar-a-aplicacao-digi-tv`.
- DIGI Portugal blog posts about the DIGI TV app:
  - `https://blog.digi.pt/lancamos-a-app-digi-tv/`
  - `https://blog.digi.pt/ver-tv-online-e-possivel-com-a-digi-tv/`

General platform/interoperability sources:

- Google Play listing for `ro.digionline.tv`, `DIGI TV`, with Portuguese locale,
  at `https://play.google.com/store/apps/details?id=ro.digionline.tv&hl=pt_PT`.
- Google Play Terms of Service, dated 2024-07-01, at
  `https://play.google.com/about/play-terms.html`.
- Android Developers documentation for deep links at
  `https://developer.android.com/training/app-links/create-deeplinks`.
- Directive 2009/24/EC on the legal protection of computer programs at
  `https://eur-lex.europa.eu/eli/dir/2009/24/oj/eng`.

Romanian DigiOnline terms are not the primary source for this project. They can
explain app lineage and package naming, but a Portugal subscriber-facing public
posture should be checked against DIGI Portugal terms first.

## Practical Reading

The current public path looks defensible only because Rebobina is a handoff-only
browser:

- It uses Android's normal Intent/deep-link mechanism to ask the user's installed
  official app to open a route.
- It does not copy provider code, bundle APKs, extract streams, bypass DRM,
  impersonate an account, or replay private authenticated responses.
- It keeps real screenshots, logcat, dumps, and device captures under ignored
  `captures/` and records only redacted route/shape evidence in docs.
- It uses synthetic metadata fixtures for the public app surface.

Important cautions from DIGI Portugal sources:

- The Portugal FAQ says the DIGI TV app can be downloaded for Android from Google
  Play, for iOS from the App Store, and from compatible Samsung, LG, and Android
  smart-TV app stores. It also says compatible TV devices include Android TV 6.0+
  and that Android devices should be Google-certified.
- The Portugal FAQ says app credentials are sent to the customer email address
  associated with the DIGI customer account. Those credentials and any resulting
  session material must stay outside this repository.
- The Portugal FAQ says the app is available in EU countries with 3G/4G/5G
  access, but the public repository should not depend on making legal conclusions
  about roaming/portability rights; it should only hand off to the user's
  official installed app.
- The Portugal contract terms require customers to respect intellectual-property
  rights in content accessed through the services and not make illegal or
  unauthorized use of those contents.
- The Portugal contract terms require customers to preserve the confidentiality
  of access codes, passwords, and usage codes associated with the services.
- The Portugal contract terms prohibit violations or attempted violations of
  authentication/security systems protecting accounts, access, networks, servers,
  or services.
- The Portugal TV terms describe the TV service as supplied to the customer and
  prohibit resale/commercialization/resource sharing outside the installation
  address. Rebobina must therefore remain a private-user handoff UX, not a shared
  service, gateway, proxy, or redistribution tool.
- The Portugal TV terms say channels are supplied as broadcast and as long as the
  signal is available for retransmission, and that channel/programming changes or
  rights-holder restrictions can affect features.
- The Portugal TV terms say recordings and TV contents are protected by copyright
  and related rights; recorded content cannot be accessed, kept, or transferred
  by other means, and uses outside private use require authorization.
- The Portugal TV terms explicitly mention limitations on features such as
  `Recomeçar`, `Gravações`, and `EPG Reverso` due to rights-holder restrictions.
  Rebobina should treat catch-up availability as metadata/capability that may be
  absent, not as a right to extract or reconstruct playback.
- Google Play grants a personal, non-commercial license to use Content and
  reserves rights not expressly granted. It also prohibits stream-ripping/stream
  capture, circumvention of security features, and modification of Content.

The Android deep-link documentation supports the narrow technical mechanism: deep
linking is a general Android capability using Intents, and Android can route a
URI to an app that declared a matching intent filter. That supports testing and
using declared routes, but it does not override a provider's service terms.

EU software law is not the same as US-style "fair use". Directive 2009/24/EC
recognizes that ideas and principles underlying interfaces are not protected by
copyright, permits observation/study/testing of a program's functioning by a
lawful user, and has a limited decompilation exception for interoperability when
necessary and when the information is not otherwise readily available. Those
exceptions are conditional and do not permit copying provider expression,
publishing proprietary material, building a substantially similar program, or
using information for unrelated purposes.

## Public-Repo Boundary

Public repository is acceptable for this project only if all of the following
remain true:

1. Repository name and positioning stay provider-neutral.
2. Provider names are used only factually/nominatively in documentation and test
   evidence.
3. Portugal-specific legal checks are kept as the primary service-terms reference
   for this use case; Romanian DigiOnline terms are not treated as governing
   terms for a Portugal subscriber.
4. No provider credentials, cookies, tokens, account identifiers, device secrets,
   raw provider responses, screenshots/logs with account UI, APKs, decompiled
   source/assets, raw stream URLs, playlists, DRM license URLs, keys, or
   entitlement responses are committed.
5. Playback stays entirely inside the official provider app.
6. Exact item-level handoff is enabled only after safe evidence proves the route
   mapping without copying private response bodies or sensitive identifiers.
7. Validation continues to reject sensitive-looking values and GitHub Actions
   workflows remain absent unless the owner explicitly reviews billing and scope.

## When To Stop Public Work

Move any deeper exploration to a separate private lab, or stop entirely, if the
next step requires any of these:

- Capturing or replaying authenticated provider API responses.
- Publishing decompiled app code, resources, strings, screenshots, account UI, or
  proprietary database/EPG content.
- Extracting stream URLs, DRM/license data, entitlement data, or player state.
- Circumventing geo, account, app-integrity, DRM, or playback restrictions.
- Shipping a provider-specific clone rather than a provider-neutral handoff UX.

Even in a private lab, the same no-playback/no-DRM/no-credential boundary should
remain the default unless the owner gets separate legal advice.

## Recommendation

Publish the current AGPL repository publicly only after a clean local commit and
validation pass. Keep the public GitHub repo focused on the harness, synthetic
fixtures, normalized metadata UI, and official-app handoff experiments. For this
Portugal use case, keep DIGI Portugal terms and FAQ pages as the primary public
posture references, and do not publish captured device artifacts, provider
private API research, app credentials, private app/session responses, or deeper
reverse-engineering material.
