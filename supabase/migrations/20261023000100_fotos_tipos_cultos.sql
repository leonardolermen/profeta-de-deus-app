-- Fotos por caminho no Storage, tipos fixos de evento e cultos semanais.
-- Execute depois de 20261021000100_android_features.sql.
begin;

-- Caminho no bucket, nao URL: o app Android envia para o Storage e guarda so o caminho
-- retornado. Mantem compatibilidade com o campo existente em public.fotos (abaixo).
alter table public.eventos add column imagem text null;
comment on column public.eventos.imagem is
  'Caminho no bucket eventos (ex.: 2026/abc.jpg), nunca URL: o check do banco nao distingue debug de release.';

-- NOT VALID preserva eventos legados com tipo fora da lista fixa; novas entradas sao verificadas.
alter table public.eventos add constraint eventos_tipo_valido
  check (tipo in ('culto', 'jovens', 'mulheres', 'campanha', 'batismo')) not valid;

-- url deixa de ser obrigatoria: fotos novas guardam caminho no bucket galeria, nao URL publica.
alter table public.fotos alter column url drop not null;
alter table public.fotos add column caminho text null;
comment on column public.fotos.caminho is 'Caminho no bucket galeria, nunca URL.';

-- Troca o check antigo (so url) por um que aceita exatamente uma das duas origens.
alter table public.fotos drop constraint fotos_url_check;
alter table public.fotos add constraint fotos_url_ou_caminho check (
  (url is not null and caminho is null and url ~ '^https://[^/[:space:]]+')
  or (caminho is not null and url is null)
);

create table public.cultos_semanais (
  id uuid primary key default gen_random_uuid(),
  nome text not null,
  dia_semana int not null,
  horarios text[] not null default '{}',
  destaque text not null default '',
  local text not null default '',
  ordem int not null default 0,
  criado_em timestamptz not null default now(),
  check(char_length(trim(nome)) between 2 and 120),
  check(dia_semana between 0 and 6),
  -- Check nao pode ter subquery; junta o array em texto e valida tudo com uma regex so.
  check(
    array_to_string(horarios, ',') = ''
    or array_to_string(horarios, ',') ~ '^([01][0-9]|2[0-3]):[0-5][0-9](,([01][0-9]|2[0-3]):[0-5][0-9])*$'
  )
);

alter table public.cultos_semanais enable row level security;
create policy cultos_semanais_leitura on public.cultos_semanais
  for select to anon, authenticated using(true);
create policy cultos_semanais_gestao on public.cultos_semanais
  for all to authenticated using(public.is_admin()) with check(public.is_admin());
grant select on public.cultos_semanais to anon, authenticated;
grant insert, update, delete on public.cultos_semanais to authenticated;

-- Buckets publicos (URL de leitura direta), mas so admin envia, atualiza ou remove objeto.
-- Limite 5 MB e so JPEG: fotos do app, nao arquivo generico.
insert into storage.buckets(id, name, public, file_size_limit, allowed_mime_types) values
  ('eventos', 'eventos', true, 5242880, array['image/jpeg']),
  ('galeria', 'galeria', true, 5242880, array['image/jpeg']);

create policy eventos_e_galeria_envio_admin on storage.objects
  for insert to authenticated with check(bucket_id in ('eventos', 'galeria') and public.is_admin());
create policy eventos_e_galeria_atualizacao_admin on storage.objects
  for update to authenticated
  using(bucket_id in ('eventos', 'galeria') and public.is_admin())
  with check(bucket_id in ('eventos', 'galeria') and public.is_admin());
create policy eventos_e_galeria_exclusao_admin on storage.objects
  for delete to authenticated using(bucket_id in ('eventos', 'galeria') and public.is_admin());

commit;
