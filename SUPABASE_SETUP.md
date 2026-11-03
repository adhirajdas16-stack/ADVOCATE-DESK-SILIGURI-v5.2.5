# Supabase setup — Advocate Desk Siliguri 5.1

## What the Android app now connects to
- Supabase email/password sign-up and sign-in using the project's publishable/anon key.
- Workspace-scoped case upload and download through PostgREST.
- Private Storage upload helper for case documents (the file-upload screen is not yet exposed in the Android UI; use the helper only from a trusted app flow).
- Workspace role policies and audit triggers supplied in SQL migrations.

## Create the backend
1. Create a Supabase project in the account owned by the law office. Choose a strong database password and keep it private.
2. In SQL Editor, run `supabase/migrations/0001_team_case_workspace.sql`, then `0002_storage_policies.sql`, then `0003_workspace_bootstrap_and_audit.sql` in that order.
3. In Storage, create a **private** bucket named `case-documents`.
4. In Authentication settings, enable Email provider. Decide whether email confirmation is required.
5. In the Android app, open **Team Cloud Sync / Login → Configure Cloud Project**. Enter Project URL, publishable/anon key, and workspace UUID. Never use the `service_role` key in the app.
6. Create the owner's account with **Create Account**. Verify the email if required, then sign in.
7. Create the first workspace and retrieve its UUID. The SQL function can be run while authenticated from the SQL editor only if you impersonate/authenticate properly; simplest safe route is to call RPC `create_first_workspace` from an authenticated client and store the returned UUID in app settings. The owner must then re-open cloud configuration with that UUID.
8. Colleagues create their own accounts. The owner/admin adds them using RPC `owner_add_workspace_member(wid, email, display_name, role)` from an authenticated SQL/RPC client. Roles: admin, advocate, staff, viewer.
9. Test with dummy cases first. One device uploads and another device downloads via **Sync Cases Now**.

## Important current limitations
- Sync currently uploads local case rows and downloads workspace rows. It is a basic first-pass sync, not conflict-resolving bidirectional replication. If two devices edit the same case, last upload may overwrite cloud fields. Back up first and avoid concurrent edits until conflict resolution is added.
- Existing local rows get a cloud UUID on first upload. Do not repeatedly import the same JSON backup on multiple devices before initial sync; that can create duplicate cases.
- The app currently exposes a sync button and account setup. The document upload helper exists in `SupabaseCloud`, but a user-facing attachment picker and document metadata registration still need completion before ordinary staff should upload documents.
- Court ingestion remains intentionally disabled. No authorised eCourts API/provider credentials have been supplied. Do not scrape CAPTCHA-protected pages or mark unreviewed data as verified.
- Do not use real client data until the project owner has reviewed access policies, tested backup/restore, tested role boundaries, and completed a successful Android build.
