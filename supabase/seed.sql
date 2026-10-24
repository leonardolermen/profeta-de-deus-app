-- Somente para o Supabase local de desenvolvimento. Nunca aplicar em producao.

-- Usuarios de teste: senha "senha-local-123" para os dois, hash via pgcrypto (bcrypt).
-- O trigger handle_new_user cria a linha correspondente em public.perfis.
-- confirmation_token/recovery_token/email_change_token_new/email_change nao tem default no
-- schema do GoTrue; deixar NULL quebra o login ("converting NULL to string is unsupported").
insert into auth.users (
  instance_id, id, aud, role, email, encrypted_password, email_confirmed_at,
  raw_app_meta_data, raw_user_meta_data, created_at, updated_at,
  confirmation_token, recovery_token, email_change_token_new, email_change
) values (
  '00000000-0000-0000-0000-000000000000', '11111111-1111-1111-1111-111111111111',
  'authenticated', 'authenticated', 'admin@local.test',
  extensions.crypt('senha-local-123', extensions.gen_salt('bf')), now(),
  '{"provider":"email","providers":["email"]}', '{"nome":"Administrador Local"}', now(), now(),
  '', '', '', ''
), (
  '00000000-0000-0000-0000-000000000000', '22222222-2222-2222-2222-222222222222',
  'authenticated', 'authenticated', 'membro@local.test',
  extensions.crypt('senha-local-123', extensions.gen_salt('bf')), now(),
  '{"provider":"email","providers":["email"]}', '{"nome":"Membro Local"}', now(), now(),
  '', '', '', ''
);

insert into auth.identities (
  provider_id, user_id, identity_data, provider, created_at, updated_at
) values (
  '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
  '{"sub":"11111111-1111-1111-1111-111111111111","email":"admin@local.test"}', 'email', now(), now()
), (
  '22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
  '{"sub":"22222222-2222-2222-2222-222222222222","email":"membro@local.test"}', 'email', now(), now()
);

-- Promove admin@local.test. Roda como postgres: protect_profile_role so bloqueia
-- troca de role vinda de anon/authenticated, nao do seed.
update public.perfis set role = 'admin' where id = '11111111-1111-1111-1111-111111111111';

-- Eventos: o que o app Expo mostrava fixo, agora como referencia visual e para testes manuais.
insert into public.eventos (titulo, descricao, data_evento, tipo) values
  ('Encontro de Mulheres', 'Noite de louvor e palavra para as mulheres da igreja.',
    now() + interval '10 days', 'mulheres'),
  ('Culto Jovem', 'Culto da juventude com banda e ministração.',
    now() + interval '5 days', 'jovens'),
  ('Batismo Sede', 'Batismo nas aguas na sede da igreja.',
    now() + interval '20 days', 'batismo'),
  ('Campanha de Milagres', 'Campanha especial de oracao e milagres.',
    now() + interval '30 days', 'campanha');

-- Cultos semanais fixos.
insert into public.cultos_semanais (nome, dia_semana, horarios, destaque, local, ordem) values
  ('Culto da Família', 0, array['08:00', '15:00', '18:00'], 'Escola Dominical Kids',
    'R. Marli, 191 - São Paulo', 0),
  ('Culto de Libertação', 3, array['08:00', '15:00', '18:00'], 'Campanha de Milagres',
    'R. Marli, 191 - São Paulo', 1);

-- Dados da igreja (a linha com id=1 ja existe, criada pela migracao inicial).
update public.igreja set
  endereco = 'R. Marli, 191 - São Paulo',
  historia = '(Exemplo para desenvolvimento local) Fundada por um pequeno grupo de familias em busca de um '
    || 'lugar de fe e comunhao, a igreja cresceu ao longo dos anos e hoje reune a comunidade em torno da '
    || 'palavra, da oracao e do servico ao proximo.'
where id = 1;

insert into public.estudos (titulo, descricao, categoria, conteudo) values
  ('Fé que Vence o Mundo', 'Estudo de exemplo para desenvolvimento local.', 'Fé',
    'A fé e a certeza das coisas que se esperam, a prova das coisas que se nao veem. Este e um conteudo de '
    || 'exemplo usado apenas no ambiente local de desenvolvimento.'),
  ('O Poder da Oração', 'Estudo de exemplo para desenvolvimento local.', 'Oração',
    'A oracao e o canal de comunicacao entre o crente e Deus. Este e um conteudo de exemplo usado apenas no '
    || 'ambiente local de desenvolvimento.');

-- Aniversariantes com consentimento, no mes corrente, para aparecer na tela de inicio local.
insert into public.aniversariantes (nome, dia, mes, consentimento) values
  ('Maria Aparecida dos Santos', 10, extract(month from now())::int, true),
  ('João Pedro Oliveira', 20, extract(month from now())::int, true);
