-- Apenas Supabase local de teste; transação descartável, nunca dados reais de produção.
begin;
select plan(10);
insert into auth.users(instance_id,id,aud,role,email,email_confirmed_at,raw_app_meta_data,raw_user_meta_data,created_at,updated_at) values
('00000000-0000-0000-0000-000000000000','e0000000-0000-0000-0000-000000000001','authenticated','authenticated','member.access@local.test',now(),'{}','{"nome":"Membro"}',now(),now()),
('00000000-0000-0000-0000-000000000000','e0000000-0000-0000-0000-000000000002','authenticated','authenticated','pending.access@local.test',null,'{}','{"nome":"Pendente"}',now(),now()),
('00000000-0000-0000-0000-000000000000','e0000000-0000-0000-0000-000000000003','authenticated','authenticated','admin.access@local.test',now(),'{}','{"nome":"Admin"}',now(),now());
update public.perfis set role='admin' where id='e0000000-0000-0000-0000-000000000003';
set local role anon;
select throws_ok('select * from public.eventos','42501',null,'visitante_nao_le_eventos');
select throws_ok($$insert into public.oracoes(nome,pedido) values('Visitante','Pedido sem login')$$,'42501',null,'visitante_nao_envia_oracao');
set local role authenticated;
set local request.jwt.claims='{"sub":"e0000000-0000-0000-0000-000000000001"}';
select lives_ok($$insert into public.oracoes(nome,pedido,usuario_id) values('Membro','Pedido de teste local','e0000000-0000-0000-0000-000000000001')$$,'membro_confirmado_envia');
select throws_ok($$insert into public.oracoes(nome,pedido,usuario_id) values('Membro','Tentativa em outra conta','e0000000-0000-0000-0000-000000000003')$$,'42501',null,'pedido_nao_forja_autor');
select is((select count(*)::integer from public.oracoes),0,'membro_nao_le_pedidos_recebidos');
select throws_ok($$update public.perfis set role='admin' where id='e0000000-0000-0000-0000-000000000001'$$,'42501',null,'membro_nao_se_promove');
set local request.jwt.claims='{"sub":"e0000000-0000-0000-0000-000000000002"}';
select is((select count(*)::integer from public.eventos),0,'conta_sem_confirmacao_nao_le_eventos');
select throws_ok($$insert into public.oracoes(nome,pedido,usuario_id) values('Pendente','Pedido sem confirmacao','e0000000-0000-0000-0000-000000000002')$$,'42501',null,'conta_sem_confirmacao_nao_envia');
set local request.jwt.claims='{"sub":"e0000000-0000-0000-0000-000000000003"}';
select is((select count(*)::integer from public.oracoes where usuario_id='e0000000-0000-0000-0000-000000000001'),1,'admin_le_pedido_enviado');
reset role;
select is((select count(*)::integer from storage.buckets where id in ('eventos','galeria') and public=false),2,'buckets_privados');
select * from finish();
rollback;
