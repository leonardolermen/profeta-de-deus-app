-- Execute depois de 20260427200500_initial_schema.sql. Nao aplica dados de demonstracao.
begin;

create or replace function public.is_admin() returns boolean
language sql stable security definer set search_path = '' as $$
  select exists(select 1 from public.perfis where id = (select auth.uid()) and role = 'admin');
$$;
revoke all on function public.is_admin() from public;
grant execute on function public.is_admin() to anon, authenticated;

create or replace function public.handle_new_user() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  insert into public.perfis(id, nome) values(new.id, new.raw_user_meta_data->>'nome');
  return new;
end;
$$;

-- Bloqueia promocao via API inclusive se existirem grants amplos antigos.
create or replace function public.protect_profile_role() returns trigger
language plpgsql set search_path = '' as $$
begin
  if new.role is distinct from old.role and current_user in ('anon', 'authenticated') then
    raise exception 'O cargo somente pode ser alterado pela administracao do banco';
  end if;
  return new;
end;
$$;
create trigger protect_profile_role before update on public.perfis
for each row execute function public.protect_profile_role();
revoke update on public.perfis from anon, authenticated;
grant update(nome) on public.perfis to authenticated;
grant select on public.perfis to authenticated;

-- Remove TODAS as politicas antigas de oracoes, incluindo o script legado permissivo.
do $$ declare p record; begin
  for p in select policyname from pg_policies where schemaname='public' and tablename='oracoes' loop
    execute format('drop policy %I on public.oracoes', p.policyname);
  end loop;
end $$;
create policy oracoes_envio on public.oracoes for insert to anon, authenticated with check(true);
create policy oracoes_admin_leitura on public.oracoes for select to authenticated using(public.is_admin());
create policy oracoes_admin_exclusao on public.oracoes for delete to authenticated using(public.is_admin());
grant insert on public.oracoes to anon, authenticated;
grant select, delete on public.oracoes to authenticated;
-- NOT VALID preserva pedidos legados fora dos limites; novas entradas sao verificadas.
alter table public.oracoes add constraint oracoes_nome_valido check(char_length(trim(nome)) between 2 and 120) not valid;
alter table public.oracoes add constraint oracoes_pedido_valido check(char_length(trim(pedido)) between 5 and 4000) not valid;

create table public.estudos (
  id uuid primary key default gen_random_uuid(), titulo text not null,
  descricao text not null default '', categoria text not null default '', conteudo text not null,
  criado_em timestamptz not null default now(),
  check(char_length(trim(titulo)) between 2 and 160), check(char_length(trim(conteudo)) between 5 and 50000)
);
create table public.fotos (
  id uuid primary key default gen_random_uuid(), titulo text not null, url text not null,
  album text not null default '', criado_em timestamptz not null default now(),
  check(char_length(trim(titulo)) between 2 and 160), check(url ~ '^https://[^/[:space:]]+')
);
-- Apenas dia/mes; evita divulgar ano de nascimento. Cadastro exige consentimento.
create table public.aniversariantes (
  id uuid primary key default gen_random_uuid(), nome text not null,
  dia integer not null, mes integer not null, consentimento boolean not null default false,
  criado_em timestamptz not null default now(),
  check(char_length(trim(nome)) between 2 and 120),
  check(mes between 1 and 12),
  check(dia between 1 and case when mes=2 then 29 when mes in (4,6,9,11) then 30 else 31 end)
);
create table public.igreja (
  id integer primary key default 1 check(id=1), nome text not null default 'Igreja Profetas de Deus',
  telefone text not null default '', whatsapp text not null default '', email text not null default '',
  endereco text not null default '', historia text not null default '', privacidade text not null default ''
);
insert into public.igreja(id) values(1);
create table public.inscricoes (
  id uuid primary key default gen_random_uuid(),
  evento_id uuid not null references public.eventos(id) on delete cascade,
  usuario_id uuid not null references auth.users(id) on delete cascade,
  nome text not null, telefone text not null, criado_em timestamptz not null default now(),
  unique(evento_id, usuario_id), check(char_length(trim(nome)) between 2 and 120),
  check(char_length(telefone) between 8 and 25)
);
create index inscricoes_usuario_idx on public.inscricoes(usuario_id);
create index inscricoes_evento_idx on public.inscricoes(evento_id);
create index eventos_data_idx on public.eventos(data_evento);
create index aniversariantes_mes_dia_idx on public.aniversariantes(mes,dia);

alter table public.estudos enable row level security;
alter table public.fotos enable row level security;
alter table public.aniversariantes enable row level security;
alter table public.igreja enable row level security;
alter table public.inscricoes enable row level security;

do $$ declare t text; begin
  foreach t in array array['estudos','fotos','igreja'] loop
    execute format('create policy leitura_publica on public.%I for select to anon, authenticated using(true)', t);
    execute format('create policy gestao_admin on public.%I for all to authenticated using(public.is_admin()) with check(public.is_admin())', t);
    execute format('grant select on public.%I to anon, authenticated', t);
    execute format('grant insert, update, delete on public.%I to authenticated', t);
  end loop;
end $$;
create policy aniversariantes_membros on public.aniversariantes for select to authenticated using(consentimento or public.is_admin());
create policy aniversariantes_admin on public.aniversariantes for all to authenticated using(public.is_admin()) with check(public.is_admin());
grant select, insert, update, delete on public.aniversariantes to authenticated;

create policy inscricoes_leitura on public.inscricoes for select to authenticated using(usuario_id=(select auth.uid()) or public.is_admin());
create policy inscricoes_envio on public.inscricoes for insert to authenticated with check(
  usuario_id=(select auth.uid()) and exists(select 1 from public.eventos e where e.id=evento_id and e.data_evento>now())
);
create policy inscricoes_cancelamento on public.inscricoes for delete to authenticated using(usuario_id=(select auth.uid()) or public.is_admin());
grant select, insert, delete on public.inscricoes to authenticated;
grant select on public.eventos to anon, authenticated;
grant insert, update, delete on public.eventos to authenticated;
commit;
