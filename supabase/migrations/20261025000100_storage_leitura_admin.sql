-- O Storage remove objetos com um DELETE sob RLS, e o Postgres so apaga linhas que a politica de
-- select deixa ver. Sem esta politica a remocao do admin respondia 200 sem apagar nada, e a foto
-- antiga de um evento ficava no bucket depois de trocada. A leitura publica das fotos nao passa por
-- aqui (buckets publicos), entao basta o admin enxergar os objetos.
begin;

create policy eventos_e_galeria_leitura_admin on storage.objects
  for select to authenticated using(bucket_id in ('eventos', 'galeria') and public.is_admin());

commit;
