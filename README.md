# ADVOCATE DESK – SILIGURI v5.2.2

Android source project (`in.advocatedesk.siliguri`).

## Included in this source
- Premium dark black/charcoal interface, white typography and cyan accents.
- PIN-gated local app access.
- SQLCipher encrypted local SQLite case database with Android Keystore-protected database key.
- Case registration/edit/delete, party and court details, CNR/case number, advocate, filing/hearing dates, status, priority and notes.
- Case outcome and judgment fields: result, judgment date, order summary, relief, amount awarded, observations, appeal details, next action and deadline.
- Dashboard counts including indicative result categories; separate case-status and case-outcome fields.
- Search, hearing filters, case history, hearing reminders and reboot reminder receiver.
- CSV export and JSON backup/restore.
- Official court resource links.
- GitHub Actions workflow to build a debug APK after pushing to GitHub.
- Supabase backend foundation: workspace/team roles, cases, case events, audit log, document metadata and court review queue.
- Row Level Security policies and private document-storage policies.
- Disabled-by-default court-ingest Edge Function that fails closed until an authorised provider is configured.
- Team/cloud/court implementation and deployment checklists.

## Not yet implemented / not active
- Live multi-user cloud synchronisation. A Firebase (or other backend) project, authentication, database, security rules and integration code are still required.
- Automatic eCourts new-case discovery or live status ingestion. This requires an authorised and technically accessible data source; public portal CAPTCHA prevents promising reliable scraping.
- Document upload/secure cloud document storage.
- This source has not been confirmed by a successful Android build or physical-device test in this environment. Build and test before using it for actual client matters.

## Build using GitHub Actions
1. Upload the contents of this project folder to the root of the private GitHub repository.
2. Open the repository's **Actions** tab.
3. Run **Android build** (or push to `main` / `master`).
4. Download the `advocate-desk-debug-apk` artifact from the successful workflow run.

The workflow builds a debug APK. A production release needs a securely managed signing key and a release build configuration.

## Local build
Open this folder in Android Studio with JDK 17 and Android SDK 35 installed. Sync Gradle and run `:app:assembleDebug`.

## Data and upgrade warning
This is a development source package, not a validated legal-office release. Keep independent encrypted backups. Test restore and upgrade using copies of data before migrating any real case database. Do not publish PINs, credentials, signing keys, client records or service-account keys in GitHub.

## Backend and court integration plan
See `docs/PRODUCT_PLAN_PREMIUM_TEAM_AND_COURT_SYNC.md` and `docs/CLOUD_AND_COURT_INTEGRATION.md`.


## Version 4.0 additions
- Expanded dashboard outcome reporting: favourable, against, partial, settled, pending-judgment and unrecorded-result counts.
- Displays a transparent favourable-outcome rate calculated from recorded decided outcomes (wins count as 1, partial outcomes as 0.5, losses as 0). This is a simple diary metric, not a legal-performance assessment.
- Updated app version metadata to 4.0.0.

## Still requires configuration / implementation
- Cloud team sync is not active until a backend project, authentication, security rules and app configuration are supplied and tested.
- Automatic court updates are not active. They require an authorised, technically accessible data source; CAPTCHA-protected public search pages must not be scraped or represented as a live API.
- No successful Android build or physical-device test is claimed by this source package.


## v5.0 honest status
See `docs/V5_IMPLEMENTATION_STATUS.md`. The Supabase schema is supplied, but no Supabase project or credentials exist yet and the Android app is not yet wired to the backend. Live team sync and cloud uploads are therefore not active. Court ingestion is deliberately disabled until an authorised provider is supplied.

## v5.1 cloud integration update
- Android app now includes a Team Cloud screen for Supabase project configuration, email sign-up/sign-in, first-workspace creation, and manual case upload/download.
- `SupabaseCloud.java` implements Supabase Auth, PostgREST case sync and a private Storage upload helper.
- Added `INTERNET` permission and SQL migration `0003_workspace_bootstrap_and_audit.sql` for safe first-owner setup, owner/admin member addition and case audit triggers.
- Setup instructions: `SUPABASE_SETUP.md`.
- Cloud is not live until the user creates/configures their Supabase project, runs migrations, creates the private bucket and signs in. Sync is an initial manual sync, not conflict-safe real-time replication.


## v5.2.1 official court portal shortcut update
- Added separate shortcuts for the official district-court case-status/CNR page, district eCourts home, High Court services, Darjeeling District Judiciary, and the Supreme Court e-Committee's official eCourts information page.
- Added an in-app note explaining CAPTCHA, manual verification, and that opening the portal is not automatic court-data sync.
- Android version metadata updated to 5.2.1.
- Court links are verified official destinations; no CAPTCHA bypass, scraping, or automatic case import is implemented.

## Build and release status (important)
This remains an Android source project. It is not yet a published/installed APK and has not been built or device-tested in this environment. The included GitHub Actions workflow can produce a debug APK after the project is pushed to a GitHub repository and the workflow succeeds. Cloud/team sync is still inactive until a law-office-owned Supabase project is configured and tested. Do not enter real client data until the go-live checklist and security review are completed.


## v5.2.2 — Siliguri and Jalpaiguri official court access
- Added in-app WebView access to the official Darjeeling Judgeship website (Siliguri Sub-Division), Siliguri court list, Jalpaiguri District Court website, central eCourts CNR search, central case-status search, and High Court services.
- Added a simple in-app guide for searching cases and verifying hearing dates/orders. Official pages may require CAPTCHA and user interaction.
- No CAPTCHA bypass, scraping, automatic case import, or background court-data sync is implemented. The official websites do not provide this app with a confirmed free public live-update API.
- Version metadata updated to 5.2.2 (versionCode 11).

## Build status
This ZIP is source code, not an APK. It has not been compiled or tested on a physical Android phone in this environment. Use the included GitHub Actions workflow to build a debug APK, then test with dummy case data before using real client records.


## App launcher icon (v5.2.3)

The ADVOCATE DESK launcher icon has been added to Android mipmap resources at mdpi, hdpi, xhdpi, xxhdpi and xxxhdpi densities. The manifest now references `@mipmap/ic_launcher` for both standard and round launcher icons. The high-resolution master artwork is retained at `app/src/main/assets/advocate_desk_app_icon_master.png`.
