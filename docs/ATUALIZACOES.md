# Histórico das atualizações Android

Consolidado em 29/10/2026. Versão atual: **2.4.0**, versionCode **7**. Este histórico descreve o que existe; pendências e sugestões não devem ser anunciadas como funções prontas. O quadro atual está em `03_TAREFAS.md`.

## 2.4.0 — acesso autenticado e publicação

- Login como primeira tela sem sessão; cadastro/recuperação continuam acessíveis. Conta confirmada entra sem aprovação manual, conforme escolha do responsável. Membros não podem editar o conteúdo da igreja.
- Logout/troca de conta descartam navegação, ViewModels privados e cache de imagens em memória. Abertura por link/lembrete passa pela entrada autenticada.
- Pedidos de oração exigem sessão e gravam usuario_id. O banco verifica que o autor corresponde à conta autenticada; somente admin lê pedidos recebidos.
- Migração de segurança remove políticas públicas antigas, exige conta confirmada, nega consultas anônimas e torna eventos/galeria privados no Storage, preservando os registros e cargos existentes.
- Fotos usam cabeçalhos autenticados somente no projeto configurado, sem cache em disco e com renovação de sessão. A galeria compartilha uma referência textual em vez de URL privada.
- Responsável executou a migração com sucesso no Supabase hospedado. Verificação REST: HTTP 401 nas nove tabelas para chamadas anônimas e false na função de confirmação sem usuário.
- Documentação de publicação, direitos, privacidade, dados e assinatura; política pública, exclusão de conta, avisos completos de terceiros e AAB assinado ainda pendentes.
- 147 testes JVM aprovados; lint com zero erros e 30 avisos. Testes pgTAP preparados, sem execução nesta etapa; validação detalhada no celular ainda deve ser registrada. O responsável informou que gostou das modificações: feedback geral favorável, sem afirmar que todos os cenários foram testados.

## 2.3.0 — Bíblia Livre oficial e offline

- NVI substituída pela Bíblia Livre TR de fevereiro de 2018, release oficial 2018.2.0, com licença CC BY 3.0 BR.
- Texto completo original incluído no APK, sem alteração dos bytes; 66 livros, 1.189 capítulos, 31.102 entradas. Leitura sem API/token; na versão atual o usuário precisa ter sessão para abrir o app.
- Sobre a tradução e licença no leitor; licença integral no APK e créditos no compartilhamento.
- Favoritos anteriores mantêm suas referências, com opção de consultar/favoritar na BLIVRE; não apresentar o texto anterior como se fosse BLIVRE.
- Testes detectaram e corrigiram a interpretação do marcador BOM antes de Gênesis 1:1, preservando o texto. 138 testes aprovados na versão.
- Origem, hashes e limites de uso em `BIBLIA_LIVRE.md`. TR/N4 diferem na base textual do Novo Testamento, não no estilo antigo/moderno de linguagem.

## 2.2.1 — Mostrar/Ocultar senha

- Botões textuais Mostrar/Ocultar no login, cadastro, nova senha e confirmação, com área de toque de 48 dp e descrição acessível.
- Cada campo começa oculto; a escolha de exibição não persiste ao sair da tela. Senhas continuam fora do estado salvo da Activity.
- 131 testes aprovados e lint sem erros na versão.

## 2.2.0 — lista de livros e oração no lugar esperado

- Lista de livros visível, filtrada pela pesquisa, combinando a navegação anterior com a busca direta. Escolher um livro mostra capítulo/versículo; a leitura permanece separada da seleção.
- Início > Oração ganhou Pedir oração e Pedidos recebidos para admin; membros viam somente o formulário. Desde a 2.4.0 o envio também exige login.
- Consulta de pedidos revalida o cargo; logout e troca de conta limpam os pedidos locais.
- Pesquisa da Harpa Cristã registrada, sem integrar conteúdo cuja permissão não foi comprovada.
- 131 testes aprovados e lint sem erros na versão.

## 2.1.0 — leitura acessível e administração visível

- Pesquisa de livro sem depender de acentos, capítulo e versículo diretos, validação de referências e mensagem para versículo inexistente.
- Leitor separado da seleção; modos Por versículo/Capítulo inteiro, navegação, favoritos e compartilhamento. Fonte ajustável entre 18 e 32 sp, preferências locais e controles maiores.
- Criar novo evento destacado em Eventos, Ajustes e Administração > Agenda somente para admin. Agenda recarrega após salvar evento ou culto.
- Testes manuais passaram a ser no celular; emulador encerrado por consumo de recursos. Compilação limitada a dois workers, heap de 1536 MB e encerramento do Gradle após o build.
- 127 testes aprovados e lint sem erros na versão.

## Base anterior

Projeto convertido para Kotlin/Compose com Supabase; Expo removido, referências visuais preservadas. Validação histórica de 26/10/2026 em Supabase local/emulador está em `VALIDACAO_ANDROID.md`. Ela não comprova automaticamente a interface ou políticas das versões posteriores.
