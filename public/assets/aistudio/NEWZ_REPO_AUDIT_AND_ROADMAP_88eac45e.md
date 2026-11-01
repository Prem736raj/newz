# Suraksha Chronicle / `Prem736raj/newz` — Repository Review and Improvement Plan

**Review date:** 1 November 2026  
**Repository:** https://github.com/Prem736raj/newz  
**Scope:** Source-based review of the default `main` tree, Android Compose UI, ViewModel state, Firebase Firestore and rules, RSS ingestion, translations, local storage, MP directory, tests, packaging, design system. The APK was **not built, installed, interacted with, performance-profiled, accessibility-scanned, or visually captured** in this review. Ratings of current UI are provisional. Eight separate design concepts are in the design ZIP.

## 1. Product definition

You're building **an India-focused news and court-accountability app for girls, teenagers, and women**. The distinctive product is not another crime-headline aggregator. It's a calm, evidence-based path from **what was reported → what is confirmed → what police or authorities did → what a court did → what remains uncertain → what users can learn from it**.

A useful product promise: **“India's stories, explained. Court outcomes, tracked. Facts before fear.”**

Target readers may include children and teens. Age-appropriate content defaults, privacy of affected children, precise attribution, and a non-sensational tone are essential rather than optional.

## 2. What exists in source

- **Android native project** in Kotlin / Jetpack Compose / Material 3, `minSdk 24`, `targetSdk 36` (`app/build.gradle.kts`).
- **Five bottom tabs**: Home, Track, Saved, Safety, MPs (`MainActivity.kt`).
- **RSS news**: Google News query with women's safety / POCSO keywords; local-language native search for some languages; max 12 feed items per parse (`IndianCrimeNewsRssClient.kt`).
- **Featured-card news UI** with region filters, query, six category chips and load-more (`SurakshaScreens.kt`).
- **Case detail screens**: summaries, reported details, status timeline, sources, attached media, representatives, follow/save, corrections (`CaseDossierDetailSheet.kt`).
- **Offline storage** in Room for saved dossiers and translation caches (`SurakshaDatabase.kt`).
- **Cloud storage** in Firebase Firestore: user saved cases, user follow-ups, public case dispatches (`SurakshaFirestoreRepository.kt`, `firestore.rules`).
- **Languages**: English + numerous Indian languages using resources, RSS, local caching, and Gemini translation (`AppLanguage`, `SurakshaTranslationService.kt`).
- **Legal and safety hub**: legal explainers, emergency resources (`SurakshaArchiveSeedData.kt`, `SurakshaScreens.kt`).
- **MP directory**: bundled Lok Sabha and Rajya Sabha JSON plus search/detail UI (`MpsRepository.kt`, `MpsOfIndiaScreen.kt`).
- **Security tests** exist for Firestore rules and selected Compose and RSS behavior. Those tests were **not executed** here.

## 3. Ratings — prototype, source-based

| Dimension | Score / 10 | Basis |
|---|---:|---|
| Vision and distinctiveness | 9.0 | Court follow-through is compelling, especially with verified primary sources |
| Feature foundation | 7.0 | Many screens, offline storage, filters, languages, case tracking |
| Current UI/UX | 6.0 provisional | Thoughtful theme/components, but 5 tabs, dense dossier flows, too much emphasis on processes and crime |
| Content accuracy infrastructure | 3.0 | RSS is not court verification; many fallback/assumed fields |
| Child safety/privacy readiness | 2.0 | Public user-submission path and personalNotes/media sharing are critical blockers |
| Accessibility/internationalization | 6.5 | Font scaling, Indic resources and dark mode; no end-to-end accessibility evidence |
| Architecture/maintainability | 5.0 | Structure exists, but giant single files and mixed responsibilities |
| Testing/release readiness | 3.0 | Some tests; no verified build, end-to-end editorial pipeline or deploy safeguards |
| Overall **current prototype** | **5.0** | Promising product still far from safe public launch |

These are qualitative judgments, not measured usability-study scores.

## 4. Critical issues with code references

### P0 / MUST FIX BEFORE PUBLIC RELEASE

1. **User-authored content can be published directly to shared public feed.** `archiveNewIncidentForLife()` saves privately then unconditionally invokes `publishPublicDispatch` with `visibility = PUBLIC`. Source: [SurakshaViewModel.kt lines 819–848](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/ui/viewmodel/SurakshaViewModel.kt#L819-L848).
2. **Attaching a URL to an existing dossier can also publish it.** Source: [SurakshaViewModel.kt lines 601–652](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/ui/viewmodel/SurakshaViewModel.kt#L601-L652). The caption assumes legal compliance rather than verifying it. The `public_dispatches` Firestore rules permit signed-in user creation of public records with no moderator/role gate: [firestore.rules](https://github.com/Prem736raj/newz/blob/main/firestore.rules).
3. **`personalNotes` travels in public payloads.** Public dispatch payload includes the note field: [SurakshaFirestoreRepository.kt lines 176–218](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/repository/SurakshaFirestoreRepository.kt#L176-L218). Public records are read back into the merged feed: [SurakshaViewModel.kt lines 1187–1265](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/ui/viewmodel/SurakshaViewModel.kt#L1187-L1265). This defeats the user's expectation of private notes.
4. **No verified survivor / minor-identification review step.** Free-form headlines, city, district, user-provided image/video URLs can be public. Exclude identifying material about children, including details that can identify someone in combination. Check POCSO Section 23 and BNS Section 72 with qualified counsel before public publishing. Never treat user-provided claims of compliance as validation.
5. **News-source headlines are turned into legal case-status claims.** Every RSS item is assigned `CaseStage.FIR_FILED` even when RSS provides no police record: [IndianCrimeNewsRssClient.kt lines 207–246](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/remote/IndianCrimeNewsRssClient.kt#L207-L246). An unknown code also defaults to FIR: [SurakshaModels.kt](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/model/SurakshaModels.kt). Replace this with `UNKNOWN / NOT_VERIFIED`.
6. **Correction requests are in memory only.** `submitCorrectionOrPrivacyReport()` updates a StateFlow map; there is no persisted moderation or actual editorial inbox visible in that method: [SurakshaViewModel.kt lines 448–455](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/ui/viewmodel/SurakshaViewModel.kt#L448-L455). Build an actionable backend intake + acknowledgement + escalation/deletion path.

### P1 / TRUST & DATA QUALITY

7. **Hardcoded apparently verified seed records.** Seed cases and regional stats are bundled with the APK and some timeline/model defaults imply verification. Need real URLs, event dates and independent editorial checks per record. Replace generic “Verified Oct 2026” defaults with missing/null values. [SurakshaArchiveSeedData.kt](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/local/SurakshaArchiveSeedData.kt), [SurakshaModels.kt](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/model/SurakshaModels.kt).
8. **Dates are misrepresented for RSS content.** `incidentEpochMillis` is set to current time minus 12 hours regardless of incident date, while `lastVerifiedDate` uses 'Syndicated Today'. Separate `publishedAt`, `incidentAt`, `courtEventAt`, `lastCheckedAt`. [IndianCrimeNewsRssClient.kt lines 219–247](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/remote/IndianCrimeNewsRssClient.kt#L219-L247).
9. **Generic law references for every RSS story.** Automatically appending BNSS and POCSO sections can mislead readers when law applicability is not established. Require a legal review for relevant-law tags. [IndianCrimeNewsRssClient.kt lines 223–227](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/remote/IndianCrimeNewsRssClient.kt#L223-L227).
10. **Location and elected representative inferred from headline text.** String-match heuristics can attach a wrong city or MP/MLA and cite the ECI without checking the specific location. Use structured gazetteers and verified constituency maps with `unknown` fallback and 'as of' dates. [IndianCrimeNewsRssClient.kt lines 298 onward](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/remote/IndianCrimeNewsRssClient.kt#L298-L360).
11. **Missing court-document identity model.** Timeline has a generic source URL default, not a strongly typed citation to an order/case ID, and can claim verified by default. Replace with `CourtEvent` including CNR/case no. where lawful, court, type, event date, published date, source URL, source type, verifiedBy, reviewedAt, confidence. [SurakshaModels.kt lines 303–321](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/model/SurakshaModels.kt#L303-L321).
12. **Headlines do not equal stories.** Google News RSS can contain duplicate syndications, outdated, irrelevant or inaccurate reports; dedup by first eight title tokens is brittle. Build canonical URL normalization, clustering, source whitelist, publication-time checks, relevance and provenance. [IndianCrimeNewsRssClient.kt](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/remote/IndianCrimeNewsRssClient.kt).
13. **Translation alone does not guarantee legal precision.** Gemini does generated translation via client-side Firebase AI, with silent exception handling; enforce a source-language toggle, legal entity protection, reviewer sampling, cost controls, caching TTL and retranslation on source corrections. [SurakshaTranslationService.kt](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/remote/SurakshaTranslationService.kt).

### P1 / APP QUALITY & SECURITY

14. **Notification setting is not proof of push alerts.** `toggleNotificationsEnabled()` changes local in-memory state and shows a verified-alert banner; no push pipeline is demonstrated by this path. Followed case IDs are also preseeded in memory. Implement actual opt-in FCM events/quiet hours and persist followed IDs. [SurakshaViewModel.kt lines 76–89 and 425–433](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/ui/viewmodel/SurakshaViewModel.kt#L76-L89).
15. **Release App Check not installed by shown config.** `configureAppCheck` immediately returns if not debuggable; production provider/integration needs explicit testing. App Check is not a substitute for auth, authorization, moderation, and server validations. [MainActivity.kt lines 136–155](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/MainActivity.kt#L136-L155).
16. **Room migration destroys saved data.** `fallbackToDestructiveMigration(true)` can delete saved cases when DB schema changes. Write migrations and tests. [SurakshaDatabase.kt](https://github.com/Prem736raj/newz/blob/main/app/src/main/java/com/example/data/local/SurakshaDatabase.kt).
17. **Backup/export policy not configured for sensitive notes.** Manifest `allowBackup=true`, and backup/data-extraction rules are templates; review exclusions, encryption and privacy policy before release. [AndroidManifest.xml](https://github.com/Prem736raj/newz/blob/main/app/src/main/AndroidManifest.xml), [backup_rules.xml](https://github.com/Prem736raj/newz/blob/main/app/src/main/res/xml/backup_rules.xml).
18. **No demonstrated automated CI pipeline.** The repository contains unit tests, Robolectric, emulator tests, but no `.github/workflows` in the inspected tree. A real build, device tests, accessibility audit, source-correction workflow, and Play release checks remain unverified.
19. **Maintainability:** large files (SurakshaScreens ~2,389 lines; CaseDossierDetailSheet ~1,791; SurakshaComponents ~1,723; MainActivity ~1,268) increase regression risk. Split by navigation screen, reusable card, court timeline, editor/admin and feature modules; adopt repositories/use cases with testable contracts.

## 5. Better information architecture

**Bottom nav (four):** Today · Court Watch · Know Your Rights · Saved. Use search/profile/settings at the top. MP directory goes under Explore → Civic context, not a top-level destination. A location selector belongs near Today filters; region personalization must be optional.

### Home / Today

- Quiet hierarchy: top brand and language switch; short intro; one featured verified/clearly unverified report; row of Latest, Court Watch, Campus, Cyber Safety, Policy / Rights; then an editorial digest.
- Card: headline + short neutral summary; publisher; publication timestamp; verification status; location (only when confidence is established); next court update (only if verified).
- Use an editorial illustration, not an identifying photo of children. Allow control of sensitive media and content from first use.

### Story / dossier detail

1. What was reported (with source and date).
2. What has been independently confirmed (with evidence).
3. What authorities / court did (orders and distinct procedural events, cited).
4. What remains unknown / unverified.
5. Case timeline; citation list; accessible plain-language explanation; save / follow / report correction.

### Court Watch

Court events are **not always a linear ladder**. Distinguish registration, FIR, investigation, remand, chargesheet, bail, hearing, interim order, final judgment, appeal, stay, remand/rehearing, etc., as applicable. Never imply a verdict where none exists. When the record is unavailable, say “No verified court record linked.” Keep primary sources and reporting as separate source types.

### Rights Room

Plain-language explainers of reporting a problem, digital privacy, public safety, school or campus rights, help-seeking, court-process terms, and the limits of reports. Publish only after reviewing laws, jurisdiction, eligibility and numbers. Keep explanatory content respectful and age-appropriate.

## 6. Editorial data pipeline to build

1. **Ingest:** publisher APIs/feeds with permission; article URL; publisher; timestamp; canonical URL; full provenance. Do not scrape protected/captcha-gated court pages automatically or pretend arbitrary RSS is a case database.
2. **Cluster:** duplicate articles into a story or issue; retain every supporting source separately.
3. **Classify:** public policy / legal rights / education / safety / justice, with low confidence routed to review.
4. **Extract:** dates, names and entities to a structured *draft*, never directly to verified public fields.
5. **Verify:** editor checks source material; link official order/judgment when available, review legal classification and redact identifiers.
6. **Publish:** only server-side moderator/admin role can publish `PUBLIC` docs; clients can submit to private `reports_pending` with restrictive access; user notes never copied to published records.
7. **Update:** court and case status verified against the correct case record; publish dated editorial revisions and correction history.
8. **Notify:** opt-in followers get updates only for verified changes, with neutral notification copy and quiet-hour settings.
9. **Audit:** who reviewed, what changed, source hash/version, action timestamp; immediately unpublish unsafe or incorrect material.

A suggested typed record: `Story(id, category, headline, summary, publishedAt, occurredAt?, status, originalSourceIds[], verificationLevel, reviewInfo, language, region?, lastCheckedAt)`; `CourtEvent(id, storyId, courtId, caseReference?, eventKind, eventDate, verificationLevel, officialDocumentUrl?, sourceTitle, checkedAt, reviewedBy)`; `UserNote` stored separately per-user; `Submission` never public until moderation.

## 7. Phased plan

**Week 1 / safety freeze:** Disable public direct user publishing. Separate private notes from public schema. Add unknown stages. Remove unjustified “verified” wording. Make correction requests durable. Audit all seeded reports for sources. This comes before polishing images.

**Week 2 / credible content:** Add source registry, editorial review status, dated court events, source references and corrected dates; separate demo seed content from production feed. Filter live RSS by relevance, dated provenance, and explicit attribution.

**Weeks 3–4 / UI and navigation:** Implement Design A's warm editorial Home and story, Design B's timeline, Design C's rights education, Design D's dark mode. Four bottom tabs; reduce dense extras and show optional MP context only on relevant articles.

**Weeks 5–6 / reliable engineering:** Compose Navigation, feature modules, DataStore for saved filter state/follows, Room migrations, encrypted/restricted backup handling, pagination and image-loading resilience, network retry/cache, accessibility including large text/Hindi, performance profiling.

**Weeks 7–8 / verification and beta:** Firestore emulator adversarial tests for public publishing, privacy notes, user roles; CI on pull requests; release signing/App Check; legal/editorial review; expert review of information accuracy; staged beta with readers and adult reviewers for teen-facing safety; production telemetry with minimal personal data; Play listing / Data Safety review.

### Release gate

- [ ] No direct client writes to public news / court timeline.
- [ ] Every court claim backed by correct primary document or explicitly described as unconfirmed.
- [ ] No identifying content about child victims or unsafe identifying combinations.
- [ ] Private user notes never readable by other users.
- [ ] Corrections and removal requests reliably reach an actual review queue.
- [ ] No fake timestamps, misleading generic citations or default `FIR_FILED` status.
- [ ] Functional opt-in notifications and unsubscribe / delete-data flow.
- [ ] Data privacy and children-specific obligations reviewed for launch jurisdictions.
- [ ] Unit, Firestore rules, instrumentation, accessibility, crash and low-memory tests passing.
- [ ] Signed release AAB tested on real devices and Google Play compliance checked.

## 8. Reference official resources

- eCourts district case status: https://services.ecourts.gov.in/ecourtindia_v6/casestatus/
- eCourts district orders: https://services.ecourts.gov.in/ecourtindia_v6/?p=courtorder/index
- eCourts High Court case status: https://hcservices.ecourts.gov.in/hcservices/main.php
- India Code POCSO Act: https://www.indiacode.nic.in/indiacode/handle/123456789/2079?view_type=browse
- India Code Bharatiya Nyaya Sanhita: https://www.indiacode.nic.in/indiacode/handle/123456789/20062?view_type=search
- MeitY DPDP Rules and phased enforcement documents: https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa

This is a technical/product audit, not legal advice. An India-qualified media/privacy lawyer should review content publication, children’s privacy and the rules applicable on your planned launch date.
