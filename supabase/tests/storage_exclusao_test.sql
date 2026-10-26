-- A remocao de fotos do Storage pelo admin precisa enxergar o objeto: sem politica de select,
-- o delete do RLS nao acha linha nenhuma e o Storage responde 200 sem apagar nada.
-- Roda isolado em transacao; nao depende do seed.sql.
begin;
select plan(3);

insert into auth.users (
  instance_id, id, aud, role, email, encrypted_password, email_confirmed_at,
  raw_app_meta_data, raw_user_meta_data, created_at, updated_at
) values (
  '00000000-0000-0000-0000-000000000000', 'c0000000-0000-0000-0000-000000000003',
  'authenticated', 'authenticated', 'admin.storage@local.test',
  extensions.crypt('senha-teste-123', extensions.gen_salt('bf')), now(),
  '{"provider":"email","providers":["email"]}', '{"nome":"Admin Storage"}', now(), now()
), (
  '00000000-0000-0000-0000-000000000000', 'd0000000-0000-0000-0000-000000000004',
  'authenticated', 'authenticated', 'membro.storage@local.test',
  extensions.crypt('senha-teste-123', extensions.gen_salt('bf')), now(),
  '{"provider":"email","providers":["email"]}', '{"nome":"Membro Storage"}', now(), now()
);

update public.perfis set role = 'admin' where id = 'c0000000-0000-0000-0000-000000000003';

insert into storage.objects(bucket_id, name) values
  ('eventos', 'teste/antiga.jpg'),
  ('galeria', 'teste/membro-tenta.jpg');

-- O Storage API liga esta flag antes do delete; sem ela o trigger protect_delete barra qualquer um.
set local storage.allow_delete_query to 'true';
set local role authenticated;

set local request.jwt.claims to '{"sub":"d0000000-0000-0000-0000-000000000004"}';
delete from storage.objects where bucket_id = 'galeria' and name = 'teste/membro-tenta.jpg';

set local request.jwt.claims to '{"sub":"c0000000-0000-0000-0000-000000000003"}';

select ok(
  exists(select 1 from storage.objects where bucket_id = 'eventos' and name = 'teste/antiga.jpg'),
  'storage_admin_enxerga_objeto'
);

delete from storage.objects where bucket_id = 'eventos' and name = 'teste/antiga.jpg';

-- Confere como postgres: o que o RLS esconde de quem apagou nao conta como apagado.
reset role;

select ok(
  exists(select 1 from storage.objects where bucket_id = 'galeria' and name = 'teste/membro-tenta.jpg'),
  'storage_membro_nao_remove'
);

select ok(
  not exists(select 1 from storage.objects where bucket_id = 'eventos' and name = 'teste/antiga.jpg'),
  'storage_admin_remove'
);

select * from finish();
rollback;
