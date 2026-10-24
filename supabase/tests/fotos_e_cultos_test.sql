-- Testes de RLS e checks para fotos por caminho, tipos de evento e cultos semanais.
-- Roda isolado em transacao; nao depende do seed.sql.
begin;
select plan(7);

-- Fixtures: dois usuarios, um promovido a admin. Insert direto em auth.users dispara
-- o trigger handle_new_user, que cria a linha em public.perfis.
insert into auth.users (
  instance_id, id, aud, role, email, encrypted_password, email_confirmed_at,
  raw_app_meta_data, raw_user_meta_data, created_at, updated_at
) values (
  '00000000-0000-0000-0000-000000000000', 'a0000000-0000-0000-0000-000000000001',
  'authenticated', 'authenticated', 'admin.teste@local.test',
  extensions.crypt('senha-teste-123', extensions.gen_salt('bf')), now(),
  '{"provider":"email","providers":["email"]}', '{"nome":"Admin Teste"}', now(), now()
), (
  '00000000-0000-0000-0000-000000000000', 'b0000000-0000-0000-0000-000000000002',
  'authenticated', 'authenticated', 'membro.teste@local.test',
  extensions.crypt('senha-teste-123', extensions.gen_salt('bf')), now(),
  '{"provider":"email","providers":["email"]}', '{"nome":"Membro Teste"}', now(), now()
);

-- Promove o primeiro usuario a admin. Roda como postgres: o trigger protect_profile_role
-- so bloqueia troca de role quando current_user e anon ou authenticated.
update public.perfis set role = 'admin' where id = 'a0000000-0000-0000-0000-000000000001';

set local role authenticated;
set local request.jwt.claims to '{"sub":"a0000000-0000-0000-0000-000000000001"}';

select lives_ok(
  $$insert into public.cultos_semanais(nome, dia_semana, horarios)
    values ('Culto da Familia', 0, array['08:00','15:00','18:00'])$$,
  'cultos_semanais_admin_insere'
);

select throws_ok(
  $$insert into public.eventos(titulo, data_evento, tipo)
    values ('Retiro de teste', now() + interval '1 day', 'retiro')$$,
  '23514',
  null,
  'eventos_tipo_invalido'
);

select throws_ok(
  $$insert into public.fotos(titulo, url, caminho) values ('Foto sem origem', null, null)$$,
  '23514',
  null,
  'fotos_exige_url_ou_caminho'
);

select lives_ok(
  $$insert into storage.objects(bucket_id, name) values ('eventos', 'admin.jpg')$$,
  'storage_admin_envia'
);

set local request.jwt.claims to '{"sub":"b0000000-0000-0000-0000-000000000002"}';

select throws_ok(
  $$insert into public.cultos_semanais(nome, dia_semana, horarios)
    values ('Culto do Membro', 1, array['09:00'])$$,
  '42501',
  null,
  'cultos_semanais_membro_nao_insere'
);

select throws_ok(
  $$insert into storage.objects(bucket_id, name) values ('eventos', 'membro.jpg')$$,
  '42501',
  null,
  'storage_membro_nao_envia'
);

reset request.jwt.claims;
set local role anon;

select ok(
  (select count(*) from public.cultos_semanais) >= 1,
  'cultos_semanais_visitante_le'
);

select * from finish();
rollback;
