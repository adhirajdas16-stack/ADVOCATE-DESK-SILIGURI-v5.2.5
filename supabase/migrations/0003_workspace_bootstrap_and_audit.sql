-- Run after 0001 and 0002. Safe first-workspace bootstrap and auditable case changes.
create or replace function public.create_first_workspace(workspace_name text default 'ADVOCATE DESK – SILIGURI')
returns uuid language plpgsql security definer set search_path=public,auth
as $$
declare wid uuid;
begin
  if auth.uid() is null then raise exception 'Authentication required'; end if;
  if exists(select 1 from public.workspace_members where user_id=auth.uid() and active) then
    raise exception 'This account already belongs to a workspace';
  end if;
  insert into public.workspaces(name,created_by) values(workspace_name,auth.uid()) returning id into wid;
  insert into public.workspace_members(workspace_id,user_id,display_name,role)
  values(wid,auth.uid(),coalesce((select raw_user_meta_data->>'name' from auth.users where id=auth.uid()),'Workspace Owner'),'owner');
  return wid;
end; $$;
revoke all on function public.create_first_workspace(text) from public;
grant execute on function public.create_first_workspace(text) to authenticated;

create or replace function public.owner_add_workspace_member(wid uuid, member_email text, member_name text, member_role text default 'advocate')
returns void language plpgsql security definer set search_path=public,auth
as $$
declare target_id uuid;
begin
  if public.workspace_role(wid) not in ('owner','admin') then raise exception 'Owner/admin permission required'; end if;
  if member_role not in ('admin','advocate','staff','viewer') then raise exception 'Invalid role'; end if;
  select id into target_id from auth.users where lower(email)=lower(member_email) limit 1;
  if target_id is null then raise exception 'Create the colleague account first'; end if;
  insert into public.workspace_members(workspace_id,user_id,display_name,role,active)
  values(wid,target_id,member_name,member_role,true)
  on conflict(workspace_id,user_id) do update set display_name=excluded.display_name,role=excluded.role,active=true;
end; $$;
revoke all on function public.owner_add_workspace_member(uuid,text,text,text) from public;
grant execute on function public.owner_add_workspace_member(uuid,text,text,text) to authenticated;

create or replace function public.log_case_change() returns trigger
language plpgsql security definer set search_path=public
as $$
begin
  insert into public.audit_log(workspace_id,actor_id,action,entity_type,entity_id,summary)
  values(coalesce(new.workspace_id,old.workspace_id),auth.uid(),lower(tg_op),'case',coalesce(new.id,old.id),
    case when tg_op='INSERT' then 'Case created: '||coalesce(new.title,'')
         when tg_op='UPDATE' then 'Case updated: '||coalesce(new.title,'')
         else 'Case deleted' end);
  return coalesce(new,old);
end; $$;
drop trigger if exists cases_audit_trigger on public.cases;
create trigger cases_audit_trigger after insert or update or delete on public.cases
for each row execute function public.log_case_change();
