# v5.0 implementation & honest status

This release adds a deployable backend foundation and deployment material to the existing Android v4 local app. It is **not** a claim that a cloud project, user accounts, live sync, document upload or court feed is already running.

## In this ZIP
- Existing Android local app: PIN gate, encrypted SQLCipher local database, case diary, hearing reminders/history, judgment/outcome/appeal fields, dashboard, CSV and JSON export/restore, official portal links.
- Supabase PostgreSQL migration for workspaces, team membership roles, cases, case events, audit log, document metadata and court-update review queue.
- Row Level Security policies for workspace-scoped access and role-limited write/delete/review actions.
- Private Storage bucket policies for case documents.
- Fail-closed court-ingest Edge Function placeholder. It returns HTTP 501 until an authorised provider adapter is implemented.
- GitHub Actions debug APK workflow and setup documentation.

## Not live until you configure it
1. Create a Supabase project owned by the law office.
2. Run `supabase/migrations/0001_team_case_workspace.sql` in the SQL editor.
3. Create a **private** Storage bucket called `case-documents`, then run `0002_storage_policies.sql`.
4. Enable an appropriate authenticated sign-in method. Invite named colleagues; never enable public/anonymous access.
5. Create the first workspace and owner membership using a trusted admin process. Do not insert service-role keys into Android or GitHub source.
6. The current Android app does not yet call Supabase. The schema is backend foundation only; Android sign-in, sync queue, conflict resolution and document-upload UI still need integration and testing before team sync is operational.

## Court data
There is no authorised eCourts API credential/provider supplied in this project. The endpoint is intentionally disabled. Do not scrape CAPTCHA pages. When an authorised source is available, implement its adapter, retain source URL and retrieval timestamp, queue proposed changes for human review, and only mark them verified after checking official records. Never auto-link by party name alone; match CNR/case number plus court and confirm identity.

## Required pre-production safeguards
- Lawyer's approval of data controller, lawful basis, retention, access, backup, breach response and colleague access policy.
- HTTPS/TLS, MFA where available, least-privilege roles, private bucket, tested RLS, backup/restore drills.
- Avoid real client data during initial testing. Use dummy cases.
- Android app build and physical-device test remain required. Do not rely on reminders until exact-alarm and notification permissions are tested on the target Android version.
- Keep exported backups encrypted and out of public GitHub repositories.
