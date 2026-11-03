-- ADVOCATE DESK – SILIGURI v5.0 backend foundation (Supabase/PostgreSQL)
-- Apply only to a NEW Supabase project after reviewing the security model.
create extension if not exists pgcrypto;

create table if not exists public.workspaces (
  id uuid primary key default gen_random_uuid(),
  name text not null default 'ADVOCATE DESK – SILIGURI',
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now()
);
create table if not exists public.workspace_members (
  workspace_id uuid not null references public.workspaces(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  display_name text not null,
  role text not null check (role in ('owner','admin','advocate','staff','viewer')),
  active boolean not null default true,
  created_at timestamptz not null default now(),
  primary key(workspace_id,user_id)
);
create table if not exists public.cases (
  id uuid primary key default gen_random_uuid(),
  workspace_id uuid not null references public.workspaces(id) on delete cascade,
  title text not null,
  case_no text, cnr text, petitioner text, respondent text,
  court text, location text, judge text, case_type text, advocate text,
  assigned_to uuid references auth.users(id),
  filing_date date, next_hearing date,
  status text not null default 'Pending', priority integer not null default 0,
  reminder_days integer not null default 1, notes text,
  outcome text not null default '', judgment_date date,
  judgment_summary text, relief text, amount_awarded text, observations text,
  appeal_filed text not null default 'Not recorded', appeal_number text,
  appeal_court text, appeal_status text, next_action text, action_deadline date,
  court_source text, court_source_url text, court_last_checked timestamptz,
  court_verification text not null default 'manual' check (court_verification in ('manual','needs_review','verified','rejected')),
  created_by uuid not null references auth.users(id),
  updated_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(), updated_at timestamptz not null default now(),
  deleted_at timestamptz
);
create unique index if not exists cases_workspace_cnr_unique on public.cases(workspace_id,cnr) where cnr is not null and cnr <> '' and deleted_at is null;
create index if not exists cases_workspace_hearing_idx on public.cases(workspace_id,next_hearing);
create index if not exists cases_workspace_status_idx on public.cases(workspace_id,status);

create table if not exists public.case_events (
  id uuid primary key default gen_random_uuid(), workspace_id uuid not null references public.workspaces(id) on delete cascade,
  case_id uuid not null references public.cases(id) on delete cascade,
  event_date date not null default current_date, event_type text not null, details text,
  created_by uuid not null references auth.users(id), created_at timestamptz not null default now()
);
create table if not exists public.audit_log (
  id bigint generated always as identity primary key,
  workspace_id uuid not null references public.workspaces(id) on delete cascade,
  actor_id uuid references auth.users(id), action text not null,
  entity_type text not null, entity_id uuid, summary text,
  created_at timestamptz not null default now()
);
create table if not exists public.case_documents (
  id uuid primary key default gen_random_uuid(), workspace_id uuid not null references public.workspaces(id) on delete cascade,
  case_id uuid not null references public.cases(id) on delete cascade,
  storage_path text not null, file_name text not null, mime_type text, size_bytes bigint,
  document_type text not null default 'other', uploaded_by uuid not null references auth.users(id),
  created_at timestamptz not null default now()
);
create table if not exists public.court_update_queue (
  id uuid primary key default gen_random_uuid(), workspace_id uuid not null references public.workspaces(id) on delete cascade,
  case_id uuid references public.cases(id) on delete cascade,
  source text not null, source_reference text, proposed_payload jsonb not null,
  review_status text not null default 'pending' check (review_status in ('pending','approved','rejected')),
  reviewed_by uuid references auth.users(id), reviewed_at timestamptz, review_note text,
  discovered_at timestamptz not null default now()
);

create or replace function public.is_workspace_member(wid uuid)
returns boolean language sql stable security definer set search_path=public
as $$ select exists(select 1 from public.workspace_members m where m.workspace_id=wid and m.user_id=auth.uid() and m.active); $$;
create or replace function public.workspace_role(wid uuid)
returns text language sql stable security definer set search_path=public
as $$ select m.role from public.workspace_members m where m.workspace_id=wid and m.user_id=auth.uid() and m.active limit 1; $$;
create or replace function public.can_edit_workspace(wid uuid)
returns boolean language sql stable security definer set search_path=public
as $$ select coalesce(public.workspace_role(wid) in ('owner','admin','advocate','staff'),false); $$;

alter table public.workspaces enable row level security;
alter table public.workspace_members enable row level security;
alter table public.cases enable row level security;
alter table public.case_events enable row level security;
alter table public.audit_log enable row level security;
alter table public.case_documents enable row level security;
alter table public.court_update_queue enable row level security;

create policy workspace_member_read on public.workspaces for select using (public.is_workspace_member(id));
create policy member_read on public.workspace_members for select using (public.is_workspace_member(workspace_id));
create policy cases_read on public.cases for select using (public.is_workspace_member(workspace_id));
create policy cases_insert on public.cases for insert with check (public.can_edit_workspace(workspace_id) and created_by=auth.uid() and updated_by=auth.uid());
create policy cases_update on public.cases for update using (public.can_edit_workspace(workspace_id)) with check (public.can_edit_workspace(workspace_id) and updated_by=auth.uid());
create policy cases_delete on public.cases for delete using (public.workspace_role(workspace_id) in ('owner','admin'));
create policy events_read on public.case_events for select using (public.is_workspace_member(workspace_id));
create policy events_insert on public.case_events for insert with check (public.can_edit_workspace(workspace_id) and created_by=auth.uid());
create policy audit_read on public.audit_log for select using (public.workspace_role(workspace_id) in ('owner','admin'));
create policy documents_read on public.case_documents for select using (public.is_workspace_member(workspace_id));
create policy documents_insert on public.case_documents for insert with check (public.can_edit_workspace(workspace_id) and uploaded_by=auth.uid());
create policy court_queue_read on public.court_update_queue for select using (public.is_workspace_member(workspace_id));
create policy court_queue_insert on public.court_update_queue for insert with check (public.can_edit_workspace(workspace_id));
create policy court_queue_review on public.court_update_queue for update using (public.workspace_role(workspace_id) in ('owner','admin','advocate')) with check (public.workspace_role(workspace_id) in ('owner','admin','advocate'));

-- Membership creation/invitations are intentionally admin-side only. Never expose service_role keys in the APK.
-- Create a private Storage bucket named case-documents in Supabase Dashboard; apply policies in 0002_storage_policies.sql.
