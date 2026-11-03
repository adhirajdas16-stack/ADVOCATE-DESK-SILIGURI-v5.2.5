# Cloud sync and court-data integration readiness

## Cloud backup / sync — not active yet

This source is prepared for a future Firebase integration, but it does not contain Firebase SDK code, a Firebase project, authentication, Firestore rules, or a `google-services.json` file. Those cannot be generated safely without creating and configuring a real project owned by the app owner.

When a Firebase project is created:
1. Register Android app ID exactly as `in.advocatedesk.siliguri`.
2. Download that project's `google-services.json` into the `app/` directory.
3. Add the Google Services Gradle plugin and Firebase Authentication + Cloud Firestore dependencies using current Firebase Android setup instructions.
4. Enable sign-in for named authorised users only. Do not use public/anonymous database access.
5. Deploy restrictive Firestore rules so each authenticated account can access only its own records. Require TLS; never put service-account credentials in the Android app.
6. Implement encrypted client-side backup/sync with conflict handling and explicit user consent. Do not upload client case data until the lawyer approves the privacy, retention and access model.

Until those steps are completed, the app remains local-only and the encrypted local database plus manual JSON backup remain the available storage options.

## Automatic eCourts updates — not active

The app currently provides official portal links, not an automated court-data feed. No authorised, documented API endpoint or credentials have been provided. Do not scrape CAPTCHA-protected pages or treat an unverified result as an official case update. An integration can be added only after confirming an authorised data source and its terms. Any future result should show source, retrieval time, and a manual verification state; hearing dates must be checked against the official court record.

## Build and device testing

A GitHub Actions workflow is included at `.github/workflows/android-build.yml`. Push the project to a GitHub repository and run **Actions → Android build → Run workflow**. The workflow attempts a debug build and uploads the APK as an artifact if the build succeeds.

A successful CI build is not the same as physical-device testing. Install the APK on the target phone and test PIN setup/lock, case create/edit/delete, reminders, reboot reminder recovery, CSV/JSON export, restore, and encrypted database access. Test with dummy data first. Record Android version and any crash/logcat output before using real client information.
