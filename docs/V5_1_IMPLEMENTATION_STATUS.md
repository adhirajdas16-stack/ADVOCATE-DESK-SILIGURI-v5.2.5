# v5.1 implementation status

Implemented in source: Supabase configuration UI, email sign-up/sign-in, first workspace creation, basic manual local-to-cloud case upsert and cloud-to-local download, private Storage upload helper, workspace bootstrap/member administration SQL, audit trigger, Internet permission.

Requires user setup: Supabase project URL and publishable anon key, SQL migrations, private `case-documents` bucket, owner account, workspace creation, colleague accounts and membership.

Not yet production-complete: real-time subscriptions, conflict resolution, deletion tombstones, event-history sync, attachment picker + metadata registration UI, robust refresh-token lifecycle, formal security review, successful release build and device tests.

Court updates: disabled fail-closed endpoint remains. An authorised provider/API contract and credentials are mandatory. The public eCourts portal must not be scraped around CAPTCHA or access controls.
