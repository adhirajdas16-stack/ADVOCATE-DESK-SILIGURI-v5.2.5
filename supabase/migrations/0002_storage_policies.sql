-- Run after creating a PRIVATE bucket named case-documents.
-- Object path format: <workspace-uuid>/<case-uuid>/<random-file-name>
create policy case_docs_read on storage.objects for select to authenticated
using (bucket_id='case-documents' and public.is_workspace_member(((storage.foldername(name))[1])::uuid));
create policy case_docs_upload on storage.objects for insert to authenticated
with check (bucket_id='case-documents' and public.can_edit_workspace(((storage.foldername(name))[1])::uuid));
create policy case_docs_update on storage.objects for update to authenticated
using (bucket_id='case-documents' and public.can_edit_workspace(((storage.foldername(name))[1])::uuid))
with check (bucket_id='case-documents' and public.can_edit_workspace(((storage.foldername(name))[1])::uuid));
create policy case_docs_delete on storage.objects for delete to authenticated
using (bucket_id='case-documents' and public.workspace_role(((storage.foldername(name))[1])::uuid) in ('owner','admin'));
