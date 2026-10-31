-- Aplicar no SQL Editor depois da migration 20261029000100_acesso_autenticado.sql.
-- Cria apenas indicações e suas permissões; não altera eventos, inscrições ou cargos.
begin;
create table public.indicacoes (
  id uuid primary key default gen_random_uuid(),
  titulo text not null check (char_length(btrim(titulo)) between 2 and 160),
  descricao text not null check (char_length(btrim(descricao)) between 5 and 4000),
  contato text not null default '' check (char_length(contato) <= 200),
  link text not null default '' check (char_length(link) <= 2048 and (link = '' or link ~ '^https://[^[:space:]]+$')),
  criado_em timestamptz not null default now()
);
alter table public.indicacoes enable row level security;
revoke all on public.indicacoes from public, anon, authenticated;
grant select, insert, update, delete on public.indicacoes to authenticated;
create policy indicacoes_leitura on public.indicacoes for select to authenticated
  using (public.is_confirmed_user());
create policy indicacoes_criacao on public.indicacoes for insert to authenticated
  with check (public.is_admin());
create policy indicacoes_edicao on public.indicacoes for update to authenticated
  using (public.is_admin()) with check (public.is_admin());
create policy indicacoes_exclusao on public.indicacoes for delete to authenticated
  using (public.is_admin());
commit;
