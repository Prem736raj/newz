# Copy into Google AI Studio — Suraksha Chronicle full improvement brief

Act as a senior Android engineer, editorial systems architect, designer, privacy reviewer and QA lead. Work directly on my existing repository `Prem736raj/newz` (Kotlin, Jetpack Compose Material 3, Room, Firebase Firestore, Google News RSS, Gemini translations). I am building an India-focused news and verified court-follow-up reader for girls, teenagers and women. It should be useful, safe, trustworthy, and readable without requiring an account. The product is NOT a sensational crime-news feed.

**Non-negotiable: preserve data and avoid unverified claims. Do not fabricate news stories, FIR registrations, court orders, verdicts, dates, statistics, source links, MP details, or legal citations. If unknown, display “Not verified / No official record linked”. Do not mark a source as verified merely because it appears in RSS.**

## Priority order

P0 first: eliminate direct client publishing to `/public_dispatches`, remove private `personalNotes` from all public serialization, keep attachments private until review, add editor-only server approval, durable correction/deletion request queue, child-identity redaction review, and unknown/default-safe court stage. Update Firestore rules and adversarial emulator tests. Do not release until these are fixed.

P1: create typed `Story`, `Source`, `CourtEvent`, `EditorialReview` models with per-event dates, source type, original URL, verified status, and review timestamp. Explicitly separate `reported`, `official document confirmed`, `disputed`, `corrected`, and `unknown` statuses. Implement publisher attribution, canonical dedup, source validation, feed retry/caching, and accurate dates. Never automatically infer POCSO, FIR or the responsible MP from keywords alone.

P2: redesign the UI using the supplied `Suraksha_Chronicle_UI_Design_Concepts.zip`: **Design A** for the main editorial Home and article, **Design B** for calm court timeline interactions, **Design C** for rights and explainers, and **Design D** as dark mode. Treat PNGs as design references, not screenshots or real case evidence. Deliver polished responsive Compose screens with custom components and accessible touch targets.

### UI specification

- Navigation: Today, Court Watch, Rights, Saved. Move MPs into a secondary Explore/Civic context page, not primary navigation.
- Home: balanced hero, one story lead, horizontal topic filters, compact two-line summaries, source + timestamp, veracity label, generous white space, clear loading/empty/error/offline states, optional region/language selection.
- Article: headline, context, “What was reported”, “What is confirmed”, “What did the court do?”, “What remains unknown”, sources, neutral case timeline, follow/save/report correction. Preserve source-language option on machine translations.
- Court Watch: update type, actual event date, source verified link, status; do not imply events are in a fixed linear progression. A detail screen must never show a made-up hearing or decision.
- Rights: approachable plain-language explainers on online privacy, school/campus issues, legal processes, reporting concerns and support resources. Show date last reviewed, jurisdiction and external official references.
- Safety: no graphic thumbnails or identifying information about minors; content advisories on sensitive reports; blur option on by default until user changes it; neutral push notification titles; no engagement streaks based on traumatic stories.
- Visual: warm paper `#F6F1E8`, ink `#202C39`, restrained terracotta `#BD5C4D`; supportive sage for positive statuses; Lato/serif editorial headings. Dark mode from concept D. No stereotypical pink-only design; avoid busy dashboards and fear-inducing large red crime counters.
- Accessibility: TalkBack semantics, minimum 48dp targets, font scale 0.85–1.6+ without clipping, contrast checks, RTL locales where supported, full Hindi/English tests, reduced-motion support, graceful error messages.

### Technical requirements

- Split giant UI files into clear feature folders: `feed`, `story`, `court`, `rights`, `saved`, `civic`, `settings`, `components`, `theme`.
- Use repositories, interfaces, use cases, ViewModels and immutable UI state; avoid business logic in Composables.
- Add Room migrations instead of destructive fallback, keep backups of sensitive notes restricted, enforce private data storage separation.
- Use server-side moderation and Firestore security rules; clients can propose stories but not publish verified public news.
- Add publisher validation and content ingestion service. Do not access captchas or protected court portals through unauthorized scraping. For court orders, store only human-verified official URLs or permitted feeds.
- Implement follow/unfollow persistence and real opt-in notifications only after an actual backend exists.
- Add test coverage: Firestore rules (privacy, direct public write denied), RSS parser, invalid date handling, non-linear status timelines, Hindi translations, Compose screenshots, TalkBack, dark mode, responsive screen heights.
- Configure CI (Gradle lint/test/build), release protection, crash observability with no personal sensitive data, App Check production provider, signed bundle and staged beta testing.

## Work procedure

1. Inspect the actual project and identify mismatches between this request and current code.
2. Produce a file-by-file plan and a requirement checklist; explain safety blockers.
3. Implement **P0 changes first**, including tests and evidence; do not spend time only on cosmetics.
4. Implement visual changes one screen at a time preserving functionality, with Compose previews and screenshots from a running build where possible.
5. Run builds, lint, unit tests, Firestore emulator tests, and instrumentation tests that the environment supports; report exact commands/results. Do not claim tests passed when not run.
6. Output a clear `DONE / NOT DONE / BLOCKED` matrix, screenshots, remaining launch blockers, and a final prioritized backlog. Do not silently change production Firestore rules without documenting deployment steps.

Quality bar: journalism-first, court-evidence-first, child-safe, privacy-first. The finished product should be calm enough for a teenage reader and rigorous enough that adults can trust its sourcing.
