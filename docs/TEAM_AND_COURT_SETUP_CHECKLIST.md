# Team, cloud documents and court update checklist

## Team roles
- owner: workspace control, membership administration, delete and review rights
- admin: manage team workflow, delete and review rights
- advocate: add/edit cases, events and review court proposals
- staff: add/edit operational case information and events
- viewer: read-only access

Membership provisioning must be done by a trusted server/admin flow. Never place Supabase `service_role` credentials in the Android app.

## Sync behaviour still to implement in Android
- Auth sign-in and session refresh
- workspace selection and membership-aware screens
- push/pull sync with updated_at/version conflict handling
- offline mutation queue and retry/backoff
- audit events on create/update/archive and result changes
- user-facing sync state and last successful sync time
- explicit conflict resolution; never silently overwrite a newer judgment or hearing date

## Court review workflow
1. Obtain a documented, authorised court-data provider/API and permitted use.
2. Fetch only cases identified by stable identifiers (CNR/case number and court).
3. Store candidate update in `court_update_queue` with source and retrieval time.
4. An advocate/admin compares it with the official record and approves/rejects it.
5. Apply approved fields, write an audit event, and keep source metadata.
6. Display last checked time and verification status in the app.

The public eCourts portal remains a manual official link unless authorised programmatic access is available.
