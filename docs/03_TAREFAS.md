# Implementado e pendências — versão 2.4.0

Atualizado em 29/10/2026. Histórico por versão em `ATUALIZACOES.md`, evidências em `VALIDACAO_ANDROID.md` e roteiro de publicação em `PUBLICACAO_PLAY_STORE.md`. Documentos do plano Expo são históricos, não representam o aplicativo atual.

## Implementado

- [x] Aplicativo Android Kotlin/Compose, sem dependência de Expo.
- [x] Bíblia com lista visível de livros e pesquisa por livro, capítulo e versículo; leitura separada, navegação entre versículos/capítulos, modo contínuo, fonte ajustável e preferências locais.
- [x] Bíblia Livre TR 2018.2.0 completa no APK, leitura offline após login, créditos, licença CC BY 3.0 BR e compartilhamento identificado. Referências de favoritos anteriores preservadas para consulta na nova edição.
- [x] Login inicial obrigatório, cadastro, confirmação de e-mail, recuperação, perfil e logout; Mostrar/Ocultar nos quatro campos de senha.
- [x] Separação membro/admin. Qualquer conta confirmada pode entrar, sem aprovação manual, conforme decisão do responsável; isto não comprova filiação à igreja.
- [x] Descarte da navegação/dados privados e cache de imagens ao sair ou trocar de conta.
- [x] Oração somente com sessão e usuário vinculado; membros enviam pedidos pessoais e administradores também consultam pedidos recebidos dentro de Início > Oração.
- [x] Eventos, inscrições pessoais, cultos, estudos, fotos, aniversariantes, contatos, tema e lembretes locais. Atalhos Criar novo evento em Eventos e Ajustes para admin.
- [x] Administração do conteúdo e upload de fotos; fotos do Supabase por endpoint autenticado. Compartilhamento da galeria envia referência textual, sem URL privada/token.
- [x] Supabase existente reativado/configurado, contas administradoras já utilizadas pelo responsável; não é necessário criar outro projeto ou promover novamente a primeira conta.
- [x] Migração `20261029000100_acesso_autenticado.sql` executada pelo responsável com sucesso. Conferência posterior: consultas anônimas às nove tabelas negadas com HTTP 401, função de confirmação false sem usuário.
- [x] APK debug 2.4.0, 147 testes JUnit aprovados e lint com zero erros/30 avisos.
- [x] Feedback favorável do responsável sobre as modificações no celular. Não substitui a conferência detalhada de cada fluxo abaixo.
- [x] Roteiro de publicação com licenças, créditos, texto-base para a ficha da loja, privacidade, acesso do avaliador e assinatura.

## Concluir antes da Play Store

- [ ] Publicar política de privacidade HTTPS com responsável e contato reais; oferecer acesso antes do cadastro e dentro do app.
- [ ] Implementar e testar exclusão de conta e tratamento dos dados associados pelo app e por recurso externo, incluindo retenções justificadas. É pendência de publicação, não recurso descartado do escopo.
- [ ] Concluir inventário de dependências diretas/transitivas e avisos LICENSE/NOTICE no pacote final.
- [ ] Revisar autorizações de marca, imagens/pessoas, fontes, ícones e outros conteúdos. Guardar comprovantes privados.
- [ ] Conferir os créditos BLIVRE no AAB final e na ficha da loja, conforme `PUBLICACAO_PLAY_STORE.md`; créditos na loja não substituem os do aplicativo.
- [ ] Corrigir/verificar Site URL do Supabase para eliminar localhost, redirect de recuperação, confirmação e SMTP. Testar os links reais de ponta a ponta.
- [ ] Gerar AAB release assinado, guardar chaves fora do Git e configurar Play App Signing.
- [ ] Preencher Segurança dos dados, classificação, público-alvo, suporte, URLs e acesso privado para o avaliador, com dados de demonstração seguros.
- [ ] Realizar a trilha de testes exigida para a conta e reconferir regras/API alvo vigentes na data do envio.

## Validações ainda necessárias

- [ ] Registrar teste no celular por item: login/Voltar/logout, cadastro confirmado e não confirmado, recuperação, oração, inscrição e fotos com membro/admin.
- [ ] Conferir upload/leitura/exclusão de fotos privadas, sessão expirada, troca de conta e chamadas de escrita negadas no Supabase hospedado. O teste REST anônimo já executado comprova leitura negada, não todos esses fluxos.
- [ ] Rodar os testes pgTAP da migração no Supabase local quando houver ambiente disponível. Nesta etapa só a sintaxe SQL foi analisada; Docker permaneceu parado.
- [ ] Conferir modo avião após login, acessibilidade/fonte ampliada, notificações, teclado e desempenho nos aparelhos de teste. Dispositivos mencionados pelo responsável: Samsung S23 e Redmi A3x; o feedback geral não é um relatório completo por aparelho.
- [ ] Cadastrar/revisar o conteúdo oficial da igreja e remover dados de demonstração antes de publicar.

## Melhorias futuras, ainda não implementadas

- Separar os livros em Antigo/Novo Testamento mantendo a pesquisa.
- Seletor de outras traduções, somente com licença e edição verificadas. TR/N4 da BLIVRE não são opções de linguagem antiga/moderna.
- Convite/aprovação para permitir apenas membros conhecidos: adiado por decisão do responsável.
- Harpa Cristã: integração adiada; autorização de redistribuição do catálogo não foi comprovada.
- Sincronização de favoritos e estudos entre aparelhos, notificações push, pagamentos/dízimos, transmissão ao vivo, chat e anexos.
