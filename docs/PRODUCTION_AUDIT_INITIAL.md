# Suraksha Chronicle — initial production-hardening audit

Audit baseline: `main` at `e45ad7573cb7f85f1c78cf8ba31c4704c158b5b2` (2026-11-01).
Review branch: `astra/newz-production-hardening`.

## Release decision

**NOT READY FOR DISTRIBUTION.** This is an initial, code-grounded hardening pass, not a verified production certification. No Android APK, Firebase emulator, instrumented device tests, editorial fact-check, or Google Play submission was executed in this audit environment. A CI workflow has been added but has not yet been observed passing.

The source brief referred to the earlier commit `3b15b1ad4866d38bc6f31644dab22226299399a9`. `main` was independently rechecked before branch work; it already contained a subsequent Indic-language typography/localization commit. All changes branch from the newer SHA.

## Verified source findings and changes

| ID | Severity | File / evidence | Action on review branch | Status |
| --- | --- | --- | --- | --- |
| NEWZ-001 | P0 | `app/build.gradle.kts`: Google Services plugin and missing `google-services.json` in tracked tree | No production credentials invented; Firebase setup still requires authorized owner configuration | EXTERNAL ACTION |
| NEWZ-002 | P1 | `gradle/wrapper/`: properties exist; wrapper scripts and JAR not tracked | CI installs official Gradle 9.3.1 directly as an interim build route | PARTIAL; wrapper remains missing |
| NEWZ-003 | P1 | No tracked Actions workflow at baseline | `.github/workflows/android.yml` now attempts unit tests, lint and APK creation | ADDED; result not yet verified |
| NEWZ-004 | P0 | `SurakshaDatabase.kt`: `fallbackToDestructiveMigration(true)` | Destructive fallback removed | CODE FIX; historical migration matrix untested |
| NEWZ-005 | P0 | Room had a process-global database with saved records lacking owner ID | New account/guest data use separate database files keyed by a SHA-256-derived account scope | CODE MITIGATION; legacy recovery required |
| NEWZ-006 | P1 | `SurakshaViewModel.kt`: automatically inserted an example dossier into private archive | Automatic pinning removed | CODE FIX |
| NEWZ-007 | P1 | Failed Firestore saves described as `cloud sync pending` without a durable queue | UI now explicitly states cloud sync failed and automatic retry is unavailable | HONEST LABEL; retry queue outstanding |
| NEWZ-008 | P1 | User-added follow-ups persisted only in cloud; base dossier saved on failure | Failure UI no longer claims local follow-up detail was stored | HONEST LABEL; durable follow-up table outstanding |
| NEWZ-009/010 | P1 | Following/notifications held only in memory | Default fake subscriptions removed; notifications default off; follow messaging states session-only and no notifications | HONEST LABEL; real delivery outstanding |
| NEWZ-011/035 | P1 | Correction/privacy report stored only in a ViewModel map with fabricated receipt ID | Response states local draft, not submitted; invented receipt removed | HONEST LABEL; real secure receiver outstanding |
| NEWZ-012 | P1 | RSS generated a synthetic incident time twelve hours before ingestion | Unknown incident date is now `0L`; publication time stored independently | CODE FIX |
| NEWZ-013/019 | P1 | Unknown legal status and every RSS item mapped to FIR | Explicit `STATUS_UNVERIFIED` introduced and used for RSS/unknown codes | CODE FIX |
| NEWZ-014 | P1 | Missing publisher appeared as `Verified News Publisher` | Neutral `Publisher not specified` fallback | CODE FIX |
| NEWZ-015/016 | P1 | District mention selected first city's representative; generic selection selected first district/city | RSS representative inference withheld; exact UI selection required | CODE MITIGATION; official mapping still requires audit |
| NEWZ-017/018 | P0/P1 | Seven detailed example dossiers use relative incident dates and unreviewed attributions | No seeded case is supplied to public feed pending evidence review | CONTENT EMBARGO; original fixtures remain for review/tests |
| NEWZ-020 | P1 | Case/timeline models use generic institutional homepages as fallback evidence | Default evidence URLs replaced by empty values | CODE FIX |
| NEWZ-023 | P1 | Parliament assets assert seat counts but lack record-by-record current-source verification | No counts presented as independently validated | REQUIRES RESEARCH |
| NEWZ-024 | P1 | `FirestoreErrorHandler.kt` logs account email, UID, provider email and exception content | Redacted error categories replace personal diagnostics | CODE FIX |
| NEWZ-025 | P1 | `AndroidManifest.xml` enables backups; extraction rules are placeholders | Backups disabled and root content excluded from cloud/device transfer policies | CODE MITIGATION; device matrix untested |
| NEWZ-026 | P1 | Public emergency access requires Firebase-backed app initialization; guest UI exists, but its behavior without configured Firebase is unproven | No false claim of offline/unauthenticated reliability | EXTERNAL TEST |
| NEWZ-029 | P2 | New locale resources and fonts exist on updated main | Preserved, plus Hindi/English unverified-stage resource | PARTIAL |
| NEWZ-031/032 | P2 | RSS date label truncation and unbounded response body | RFC-style publication-date parsing; response capped at 512 KiB | CODE FIX; network failure matrix not executed |

## Data migration safety note

`suraksha_chronicle_offline.db` existed without an account owner column. The new version opens a distinct hashed-name Room database per account/guest. **The old shared file is not deleted, overwritten, or automatically assigned to the next user.** An authenticated, consent-based recovery tool must be designed and tested before production upgrades. Existing users might not see legacy offline records until that migration is provided. This is a release blocker, not a completed migration.

Because no verified schema history is available, deleting the destructive migration fallback prevents silent data erasure, but older-schema databases can fail to open pending explicit Room migrations. Release testing must cover every previously shipped schema.

## Editorial publication safety

The bundled seven case examples have moving relative dates and source labels that are not evidence of actual reporting. The public feed now uses `buildPublishedCaseDossiers()`, which deliberately returns no case examples until a human editorial team approves a claim-level source ledger. The unreviewed examples remain accessible to test fixtures. Do not ship them as verified news, republish them to a cloud collection, or assume their dates/legal proceedings are real.

RSS content is **syndicated and unverified**: it must not automatically receive an FIR badge, victim-age classification, official representative or incident time. Current fixes enforce these distinctions but do not constitute verification of publisher usage rights or underlying reports.

The bundled geography and parliamentary records, city statistics, emergency service descriptions and legal summaries have **not** been independently verified in this code pass. Do not release them under a verified label until source-ledger review.

## Test execution

- Repository tree, current default-branch SHA, relevant source and JSON/test file presence: **inspected through authenticated GitHub access**.
- New `EditorialSafetyRegressionTest.kt`: **written but not executed**. Tests cover unknown statuses, syndicated publication date, unsafe source URLs, unverified representative mapping, segregated database naming and quarantined examples.
- `./gradlew`: **not runnable** because wrapper scripts/JAR are absent.
- Android Gradle build, Robolectric tests, lint, instrumentation, Firebase emulator, Login, app process-death, offline persistence and device checks: **not run** in this environment.
- GitHub Actions: workflow created; **do not assert green until its actual run succeeds**.

Official Android Gradle Plugin 9.1.1 compatibility lists Gradle 9.3.1 and Java 17 as appropriate: https://developer.android.com/build/releases/agp-9-1-0-release-notes

## Must-fix release blockers

1. Obtain authorized Firebase test and production configuration; verify OAuth/SHA identities, named Firestore database and actual sign-in.
2. Restore a genuine official Gradle 9.3.1 wrapper (including genuine binary); run the complete clean build and all CI jobs.
3. Design and test an explicit legacy Room archive recovery/migration path, including account switching, upgrades and corrupted schema handling.
4. Persist offline follow-up timeline entries and a retryable cloud operation queue, with conflict/deletion reconciliation.
5. Provide a real, secure correction/takedown submission backend and moderation process; prohibit fabricated receipts.
6. Either implement opt-in, tested notifications or remove nonfunctional notification controls from the app.
7. Independently verify all source records, child-privacy risks, court/status wording and official civic/statistical data. Conduct human editorial and legal review.
8. Test permissions, backups, device transfers, privacy policy, Data Safety declarations, accessibility, translations and Play release signing.

## Summary

This branch reduces **code-observable** risks and untruthful feature messages. It does **not** establish that the application is fully functional, safe for release, or factually verified. Never merge or publish until outstanding P0/P1 risks, external configuration, and automated/device tests are resolved.
