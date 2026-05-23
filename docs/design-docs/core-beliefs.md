# Core Beliefs

## Provider Respect Is Non-Negotiable

Rebobina improves discovery and handoff. Playback, entitlement checks, DRM, and
account state stay inside the official provider app.

## Evidence Beats Guessing

Deep-link behavior should be proven on device or emulator when possible. If a
handoff shape is speculative, mark it as unverified instead of letting it become
implicit truth.

## Sofa-Friendly First

The target user is holding a TV remote. UI should favor large focus targets,
predictable navigation, readable text, and quick recovery from failed handoffs.

## Agent Legibility Compounds

Decisions belong in versioned files, not in chat memory. When an agent struggles,
look for missing docs, missing validation, or unclear structure before adding
more prompt text.

## Small Tools Over Big Rituals

Prefer executable checks and narrow docs that agents can run or inspect quickly.
If a rule matters repeatedly, encode it in `scripts/validate` or a focused test.
