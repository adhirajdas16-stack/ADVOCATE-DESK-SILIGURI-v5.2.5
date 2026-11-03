# ADVOCATE DESK – SILIGURI | Premium Team Edition plan

## Product direction
- Visual direction: premium black / charcoal interface, white typography, restrained cyan-neon accents, high-contrast cards, clear status indicators.
- Main navigation areas: Dashboard, My Cases, Team Cases, Hearings, Court Updates, Documents, Reports, Team & Access, Settings.
- Dashboard metrics: total active cases, today's hearings, upcoming hearings, overdue matters, priority matters, recent team activity.
- Team workspace: multiple named lawyer/staff accounts, role-based permissions, shared case records, activity/audit history, assignment and notification controls.
- Case intake: a case created by any authorised team member is shared with the team after cloud sync; show who created/edited it and when.
- Reliability: local encrypted storage remains available offline; cloud sync should show explicit states (Synced / Pending / Failed) and conflict resolution rather than silently overwriting edits.

## Firebase setup required
The Android package ID is `in.advocatedesk.siliguri`.

1. Create a Firebase project in the Firebase Console.
2. Add an Android app using exactly `in.advocatedesk.siliguri`.
3. Download `google-services.json` and place it in `app/google-services.json`.
4. Enable Firebase Authentication (Email/Password initially; accounts should be individually assigned to each colleague).
5. Create a Cloud Firestore database.
6. Deploy reviewed Firestore security rules. Never use public read/write rules for legal case records.
7. Decide the team membership and roles (Owner/Admin, Advocate, Clerk/Staff, Read-only) before inviting colleagues.

Do not commit service-account private keys, passwords, or other credentials to source control. The Android `google-services.json` is app configuration, not a substitute for database security rules.

## Suggested Firestore data model
- `firms/{firmId}`: firm name, jurisdiction, owner UID, created timestamp.
- `firms/{firmId}/members/{uid}`: display name, email, role, active flag.
- `firms/{firmId}/cases/{caseId}`: case title, CNR, case number, court, parties, advocate, next hearing, status, priority, createdBy, updatedBy, updatedAt, source and verification metadata.
- `firms/{firmId}/cases/{caseId}/events/{eventId}`: hearing/order/adjournment/note event, author UID, timestamp.
- `firms/{firmId}/audit/{eventId}`: append-only record of sensitive changes.

Rules must ensure a signed-in user is an active member of that exact firm, and only permitted roles can create, edit, delete, export, or invite. For production, enforce critical permissions in trusted backend code as well as client UI.

## Court-data integration: important limitation
The official eCourts Services portal exposes case status, cause lists, orders and judgments. Its public case-status search instructions include CAPTCHA for party-name and case-number searches. This source project therefore does not claim that it can lawfully or reliably scrape the portal or automatically discover every newly registered matter for an advocate.

Before implementing automatic updates, obtain an authorised data feed/API or written integration permission from the relevant eCourts / district-court authority. Then implement a backend connector that:
- queries only the permitted district/court/advocate identifiers;
- stores the source URL/reference, fetched-at time and source status;
- compares court records against the team's case register;
- places newly discovered or changed matters into a **Review queue**;
- requires a human to confirm the match before linking it to a client matter;
- preserves the official source values and a full audit trail;
- displays stale-feed and failed-sync warnings.

Do not auto-merge a same-name case solely because the advocate or party name matches. Use CNR/case number, court establishment and registration year to disambiguate. Never silently overwrite manually verified client details.

## Release gates before real legal-office use
- Firebase project connected and security rules independently reviewed.
- Team invite, role and account-revocation tests passed.
- Offline edits, reconnect, duplicate prevention and concurrent-edit tests passed.
- Encrypted local database and backup/restore tested on supported Android versions.
- Court integration authorised and verified against official case records.
- Data protection, client confidentiality, retention, device-loss and backup policies approved by the advocate/firm.
- Signed release APK built and tested on the actual devices used by the office.

This document is an implementation plan, not a claim that cloud sync or official court-feed integration is already active.


## Case outcome, judgment and appeal tracking
Each case record must distinguish procedural status (for example Pending, Listed, Disposed) from the substantive result. A disposed matter must never be assumed to be won or lost. The current local app includes editable fields for outcome, judgment date, judgment/order summary, relief granted or rejected, amount awarded, observations, appeal filed, appeal number, appeal court, appeal status, next legal action and its deadline. Outcome is user-entered and should be checked against the signed judgment/order. Dashboard result counts are indicative summaries, not legal conclusions.

Suggested controlled outcomes: In favour of client; Against client; Partially in favour; Partially against; Settled / Compromised; Dismissed; Withdrawn; Pending judgment; Other / not classified. Store the result in relation to the represented client's position, not the advocate personally. Keep the original order document and record who verified the result and when in the production audit trail.

## Other agreed product areas
- Hearing calendar, date search, reminders and adjournment history.
- Case documents, signed judgments and orders.
- CSV and JSON export/restore with duplicate warnings.
- Secure local PIN access and encrypted SQLite storage.
- Team roles, case assignment, shared updates and audit history (cloud phase).
- Court-update review queue with source and last-checked timestamp (authorised-source phase).
- Android APK build, installation and physical-device test before office deployment.
