-- Apenas Supabase local de teste. Não executar estas fixtures no projeto de produção.
begin;
select plan(12);
insert into auth.users(instance_id,id,aud,role,email,email_confirmed_at,raw_app_meta_data,raw_user_meta_data,created_at,updated_at) values
('00000000-0000-0000-0000-000000000000','f0000000-0000-0000-0000-000000000001','authenticated','authenticated','member.indications@local.test',now(),'{}','{"nome":"Membro"}',now(),now()),
('00000000-0000-0000-0000-000000000000','f0000000-0000-0000-0000-000000000002','authenticated','authenticated','pending.indications@local.test',null,'{}','{"nome":"Pendente"}',now(),now()),
('00000000-0000-0000-0000-000000000000','f0000000-0000-0000-0000-000000000003','authenticated','authenticated','admin.indications@local.test',now(),'{}','{"nome":"Admin"}',now(),now());
update public.perfis set role='admin' where id='f0000000-0000-0000-0000-000000000003';
insert into public.indicacoes(id,titulo,descricao) values ('f1000000-0000-0000-0000-000000000001','Teste local','Indicação de teste local');
insert into public.eventos(id,titulo,descricao,data_evento,tipo) values ('f2000000-0000-0000-0000-000000000001','Evento local','Teste',now()+interval '1 day','culto');
insert into public.inscricoes(evento_id,usuario_id,nome,telefone) values
('f2000000-0000-0000-0000-000000000001','f0000000-0000-0000-0000-000000000001','Membro','51999990000'),
('f2000000-0000-0000-0000-000000000001','f0000000-0000-0000-0000-000000000003','Admin','51999990001');
set local role anon;
select throws_ok('select * from public.indicacoes','42501',null,'visitante_nao_le_indicacoes');
set local role authenticated;
set local request.jwt.claims='{"sub":"f0000000-0000-0000-0000-000000000001"}';
select is((select count(*)::integer from public.indicacoes where id='f1000000-0000-0000-0000-000000000001'),1,'membro_le_indicacoes');
select throws_ok($$insert into public.indicacoes(titulo,descricao) values('Membro','Tentativa negada')$$,'42501',null,'membro_nao_cria_indicacoes');
select results_eq($$with changed as (update public.indicacoes set titulo='Tentativa' where id='f1000000-0000-0000-0000-000000000001' returning id) select count(*)::integer from changed$$,array[0],'membro_nao_edita');
select results_eq($$with changed as (delete from public.indicacoes where id='f1000000-0000-0000-0000-000000000001' returning id) select count(*)::integer from changed$$,array[0],'membro_nao_exclui');
select is((select count(*)::integer from public.inscricoes where evento_id='f2000000-0000-0000-0000-000000000001'),1,'membro_so_le_propria_inscricao');
set local request.jwt.claims='{"sub":"f0000000-0000-0000-0000-000000000002"}';
select is((select count(*)::integer from public.indicacoes where id='f1000000-0000-0000-0000-000000000001'),0,'pendente_nao_le_indicacoes');
set local request.jwt.claims='{"sub":"f0000000-0000-0000-0000-000000000003"}';
select lives_ok($$insert into public.indicacoes(titulo,descricao,link) values('App','Aplicativo de teste','https://example.org')$$,'admin_cria_indicacao');
select results_eq($$with changed as (update public.indicacoes set titulo='Atualizada' where id='f1000000-0000-0000-0000-000000000001' returning id) select count(*)::integer from changed$$,array[1],'admin_edita');
select is((select count(*)::integer from public.inscricoes where evento_id='f2000000-0000-0000-0000-000000000001'),2,'admin_le_todos_inscritos');
select results_eq($$with changed as (delete from public.indicacoes where id='f1000000-0000-0000-0000-000000000001' returning id) select count(*)::integer from changed$$,array[1],'admin_exclui');
select throws_ok($$insert into public.indicacoes(titulo,descricao,link) values('App','Aplicativo de teste','javascript:alert(1)')$$,'23514',null,'banco_rejeita_esquema_inseguro');
select * from finish();
rollback;
