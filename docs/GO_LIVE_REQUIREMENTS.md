# Go-live requirements — ADVOCATE DESK – SILIGURI 5.2.0

This package is a development build source, not a deployed live service or a tested production legal-office release.

## Required to activate cloud services
1. Create a Supabase project owned by the law office.
2. Apply migrations in `supabase/migrations/` in numeric order.
3. Create the private `case-documents` storage bucket and verify its policies.
4. Configure email authentication and verify owner and colleague accounts.
5. Set the Project URL, publishable/anon key, and workspace UUID in the Android app. Never use or commit a service-role key.
6. Create the first workspace and add colleagues with the least privilege needed.
7. Test owner/admin/advocate/staff/viewer access with dummy data on two or more devices.

## Required before real client data
- Complete conflict-safe sync and deletion/tombstone handling.
- Complete user-facing document picker, upload, metadata, download and access checks.
- Review SQL RLS and storage policies with a qualified developer/security reviewer.
- Test backup, restore, account recovery, device loss and access revocation.
- Build and install the APK on the target Android devices; test all flows.
- Establish a privacy notice, retention policy, consent process and secure backup process suitable for the law office.

## Court data
The Android app includes shortcuts to the official district-court eCourts case-status page, district eCourts home, High Court services, Darjeeling District Judiciary, and the e-Committee information page. These are portal links only; they do not import case data or provide background alerts.

Court automation is disabled. Activate it only after obtaining a documented authorised data source/API and complying with its terms. Keep source, retrieval timestamp and review status for each imported update. Do not automate CAPTCHA bypass or silently link ambiguous party-name matches.

## Current integration limitations
- Cloud case sync is an initial manual upload/download implementation, not realtime or conflict-safe replication.
- Document upload helper exists, but a complete user-facing document workflow is not present.
- Team membership and policies require backend configuration and owner setup.
- No APK build, production deployment, or physical-device test is claimed by this package.
