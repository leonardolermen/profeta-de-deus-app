-- Aplicar no SQL Editor do projeto hospedado antes de distribuir o APK 2.4.0.
-- Não remove cadastros nem promove contas. Qualquer conta com e-mail confirmado pode entrar.
begin;

create or replace function public.is_confirmed_user() returns boolean
language sql stable security definer set search_path = '' as $$
  select exists(select 1 from auth.users where id = (select auth.uid()) and email_confirmed_at is not null);
$$;
revoke all on function public.is_confirmed_user() from public;
grant execute on function public.is_confirmed_user() to anon, authenticated;

create or replace function public.is_admin() returns boolean
language sql stable security definer set search_path = '' as $$
  select exists(select 1 from public.perfis p join auth.users u on u.id=p.id
    where p.id=(select auth.uid()) and p.role='admin' and u.email_confirmed_at is not null);
$$;

-- Remove políticas legadas permissivas (políticas RLS são combinadas com OR).
do $$ declare p record; begin
  for p in select tablename, policyname from pg_policies where schemaname='public'
    and tablename in ('perfis','oracoes','eventos','estudos','fotos','igreja','cultos_semanais','aniversariantes','inscricoes') loop
    execute format('drop policy %I on public.%I', p.policyname, p.tablename);
  end loop;
end $$;

do $$ declare t text; begin
  foreach t in array array['perfis','oracoes','eventos','estudos','fotos','igreja','cultos_semanais','aniversariantes','inscricoes'] loop
    execute format('alter table public.%I enable row level security',t);
    execute format('revoke all on public.%I from anon',t);
  end loop;
  foreach t in array array['eventos','estudos','fotos','igreja','cultos_semanais'] loop
    execute format('create policy leitura_autenticada on public.%I for select to authenticated using(public.is_confirmed_user())',t);
    execute format('create policy gestao_admin on public.%I for all to authenticated using(public.is_admin()) with check(public.is_admin())',t);
  end loop;
end $$;

create policy perfis_leitura on public.perfis for select to authenticated
  using(public.is_confirmed_user() and (id=(select auth.uid()) or public.is_admin()));
create policy perfis_nome on public.perfis for update to authenticated
  using(public.is_confirmed_user() and id=(select auth.uid()))
  with check(public.is_confirmed_user() and id=(select auth.uid()));
revoke update on public.perfis from authenticated;
grant update(nome) on public.perfis to authenticated;

alter table public.oracoes add column if not exists usuario_id uuid references auth.users(id) on delete set null;
create policy oracoes_envio on public.oracoes for insert to authenticated
  with check(public.is_confirmed_user() and usuario_id=(select auth.uid()));
create policy oracoes_admin_leitura on public.oracoes for select to authenticated using(public.is_admin());
create policy oracoes_admin_exclusao on public.oracoes for delete to authenticated using(public.is_admin());

create policy aniversariantes_leitura on public.aniversariantes for select to authenticated
  using(public.is_confirmed_user() and (consentimento or public.is_admin()));
create policy aniversariantes_admin on public.aniversariantes for all to authenticated
  using(public.is_admin()) with check(public.is_admin());

create policy inscricoes_leitura on public.inscricoes for select to authenticated
  using(public.is_confirmed_user() and (usuario_id=(select auth.uid()) or public.is_admin()));
create policy inscricoes_envio on public.inscricoes for insert to authenticated with check(
  public.is_confirmed_user() and usuario_id=(select auth.uid())
  and exists(select 1 from public.eventos e where e.id=evento_id and e.data_evento>now())
);
create policy inscricoes_cancelamento on public.inscricoes for delete to authenticated
  using(public.is_confirmed_user() and (usuario_id=(select auth.uid()) or public.is_admin()));

-- Fotos deixam de ser entregues pelo endpoint público. O APK usa leitura autenticada.
update storage.buckets set public=false where id in ('eventos','galeria');
do $$ declare p record; begin
  for p in select policyname from pg_policies where schemaname='storage' and tablename='objects'
    and policyname in ('eventos_e_galeria_envio_admin','eventos_e_galeria_atualizacao_admin',
      'eventos_e_galeria_exclusao_admin','eventos_e_galeria_leitura_admin','fotos_leitura_autenticada') loop
    execute format('drop policy %I on storage.objects',p.policyname);
  end loop;
end $$;
drop policy if exists fotos_restricao_leitura on storage.objects;
drop policy if exists fotos_restricao_envio on storage.objects;
drop policy if exists fotos_restricao_atualizacao on storage.objects;
drop policy if exists fotos_restricao_exclusao on storage.objects;
-- RESTRICTIVE impede uma política permissiva antiga adicional de reabrir estes buckets.
create policy fotos_restricao_leitura on storage.objects as restrictive for select to public
  using(bucket_id not in ('eventos','galeria') or public.is_confirmed_user());
create policy fotos_restricao_envio on storage.objects as restrictive for insert to public
  with check(bucket_id not in ('eventos','galeria') or public.is_admin());
create policy fotos_restricao_atualizacao on storage.objects as restrictive for update to public
  using(bucket_id not in ('eventos','galeria') or public.is_admin())
  with check(bucket_id not in ('eventos','galeria') or public.is_admin());
create policy fotos_restricao_exclusao on storage.objects as restrictive for delete to public
  using(bucket_id not in ('eventos','galeria') or public.is_admin());
create policy fotos_leitura_autenticada on storage.objects for select to authenticated
  using(bucket_id in ('eventos','galeria') and public.is_confirmed_user());
create policy eventos_e_galeria_envio_admin on storage.objects for insert to authenticated
  with check(bucket_id in ('eventos','galeria') and public.is_admin());
create policy eventos_e_galeria_atualizacao_admin on storage.objects for update to authenticated
  using(bucket_id in ('eventos','galeria') and public.is_admin())
  with check(bucket_id in ('eventos','galeria') and public.is_admin());
create policy eventos_e_galeria_exclusao_admin on storage.objects for delete to authenticated
  using(bucket_id in ('eventos','galeria') and public.is_admin());

commit;
