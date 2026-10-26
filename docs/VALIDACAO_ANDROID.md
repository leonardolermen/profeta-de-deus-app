# Validação da versão Android — 21/10/2026

> Superado pela seção "Validação funcional da versão Kotlin — 26/10/2026", mais abaixo: os testes em emulador e a negação de chamadas REST no banco, tratados como pendentes aqui, foram executados contra o Supabase local. O texto desta seção fica como registro do estado em 21/10.

## Verificações executadas

- Compilação Kotlin/Java e empacotamento do APK debug com Gradle 8.13, AGP 8.11.1, JDK 21 e Android SDK 36.
- Nove testes JUnit: quatro sobre fusos, datas, lembretes e URLs; cinco sobre negação de edição a visitantes/membros/cargos desconhecidos, autorização de administradores e revogação de cargo.
- Android Lint sem erros. Há avisos de dependências com versões mais novas e sugestões de estilo KTX; as versões usadas estão fixadas no build.
- Endpoints públicos da API bíblica atual: 66 livros e Salmos 23 com 6 versículos. O endereço antigo retornou erro; o app usa `https://abibliadigital.api.br/api/`.
- XMLs de recursos e manifesto válidos. Backup em nuvem e transferência de dados do app desativados; tokens usam Android Keystore.
- BuildConfig sem credenciais Supabase. Nenhuma senha administrativa foi colocada no aplicativo ou no repositório.

## Pendências explicitamente adiadas

A configuração do Supabase foi adiada a pedido do responsável. O domínio informado não resolveu no DNS durante a verificação. Nenhuma migração foi aplicada e nenhum registro remoto foi alterado. A migração SQL está preparada para revisão/aplicação posterior.

Os fluxos de autenticação, inscrição, administração, oração e conteúdo remoto estão implementados, mas ainda precisam de validação integrada no Supabase ativo. Não foram executados testes de interface em emulador ou aparelho físico. Lembretes, intents de contato e recuperação de senha devem ser validados no Android após a configuração dos serviços.

As permissões foram reforçadas no código de acesso a dados e nas telas; os testes de cargo são locais. Ainda não foi comprovada a negação de chamadas REST no banco real, nem a revogação de uma sessão administradora conectada ao Supabase. Usuários podem enviar pedidos e gerenciar suas inscrições, mas só administradores podem modificar conteúdo da igreja.

## Artefatos

- Projeto: `android-native/`
- APK de teste: `android-native/app/build/outputs/apk/debug/app-debug.apk`
- Testes: `android-native/app/build/reports/tests/testDebugUnitTest/index.html`
- Lint: `android-native/app/build/reports/lint-results-debug.html`
- Configuração e roteiro funcional: `android-native/README.md`

---

# Validação funcional da versão Kotlin — 26/10/2026

## Contra o quê

- Supabase local (Docker, `npx supabase start`, banco recriado com `npx supabase db reset` e semeado com `supabase/seed.sql`). Serviços de analytics, realtime, edge runtime, vector, pooler e imgproxy ficaram parados.
- Emulador: AVD `profetas`, Android API 36, APK debug apontando para `http://10.0.2.2:54321`.
- Contas do seed (`admin@local.test`, `membro@local.test`) e uma conta criada no app (`novo@local.test`). A chave `service_role` não foi usada em nenhum teste de membro; o banco só foi tocado por SQL nos itens 8 e 11, como manutenção.
- API bíblica real (`abibliadigital.api.br`) pela internet do emulador.

## Verificações automáticas

- `gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: build ok, 96 testes JUnit sem falhas, lint com 0 erros e 28 avisos (versões de dependências e estilo).
- `npx supabase test db`: 2 arquivos, 10 asserções pgTAP, todas passam (antes e depois do `db reset`).

## Roteiro do README (11 itens) e extras

| # | Item | Resultado | Evidência |
|---|------|-----------|-----------|
| 1 | Visitante: agenda, estudos, enviar oração; sem ler orações/aniversariantes | Passou | Pedido gravado em `oracoes`; tela Parabéns mostra "Entre para consultar…"; API anônima: `oracoes` e `aniversariantes` retornam `[]` (200), insert de oração 201 |
| 2 | Membro: criar conta, entrar, alterar nome; REST em `role` falha | Passou | Conta criada no app (sem confirmação de e-mail no local) e nome alterado em `perfis`; `PATCH perfis role` como membro: 403 `42501` |
| 3 | Admin: evento futuro, estudo, foto, aniversário autorizado | Passou | Evento com foto enviada do seletor (1200x800 recomprimida, bucket `eventos`), estudo, foto de galeria e aniversário gravados e exibidos em Agenda, Estudos, Galeria e Parabéns. A "URL de foto" virou upload pelo seletor (Task 17) |
| 4 | Membro: filtrar, inscrever, duplicidade, cancelar; outro membro não vê | Passou | Filtro "Culto" mostra só o evento do tipo; inscrição gravada; segunda tentativa: tela mostra "Inscrição confirmada" e a API devolve 409 (`inscricoes_evento_id_usuario_id_key`); cancelamento zera a tabela; membro do seed lê `inscricoes` de outra conta: `[]` |
| 5 | Só admin lê orações, administra, vê inscrições de todos | Passou | Admin lê as 3 orações e a inscrição do outro usuário; membro: `[]` nas duas |
| 6 | Bíblia, erro de rede, favoritos, estudos concluídos, persistência, separação por conta | Passou (com observação) | Gênesis 1 carregado; favorito em Gênesis 1:3 e estudo concluído sobrevivem a `force-stop`; a outra conta vê favoritos e estudos vazios. Sem rede: tela "Não foi possível carregar" com "Tentar novamente", que recupera com a rede de volta. Observação: o texto vem em inglês cru (`Unable to resolve host "abibliadigital.api.br"…`), pendência já registrada |
| 7 | Contatos oficiais e recuperação de senha | Passou | Telefone e WhatsApp editados em Administração e refletidos em Contato; WhatsApp sem app instalado cai no navegador (Chrome), telefone abre o discador, e-mail abre o Gmail, mapa abre o Maps. Recuperação de ponta a ponta com `membro@local.test`: pedido no app, e-mail no Mailpit, link aberto no emulador (host trocado de `127.0.0.1` para `10.0.2.2`), o Chrome seguiu o `/auth/v1/verify` e abriu o app direto em Meu perfil com sessão; a senha foi trocada e o login no app com a nova senha funcionou (a senha do seed foi restaurada depois). Isso depende do `profetas://auth/recovery` em `additional_redirect_urls` do `supabase/config.toml` (commit `8cb848d`); antes dele o e-mail local redirecionava para `http://127.0.0.1:3000` |
| 8 | Notificações: lembrete, cancelamento, abertura do evento | Passou | Lembrete agendado (WorkManager, ~40 dias de atraso para evento de dezembro), "Ajustes" mostra "1 lembrete(s) ativo(s)", remover cancela o job. Evento criado por SQL para daqui a 61 min: a notificação chegou, e tocar nela com o processo morto (`am kill`) abriu o detalhe do evento. A primeira execução do worker pediu nova tentativa (rede do emulador logo após o job) e a segunda notificou |
| 9 | Modos Usuário/Administrador com duas contas | Passou | Membro: modo "Usuário", sem "Administração"; `profetas://open/admin`, `admin/event/new` e `admin/church` mostram "Acesso restrito à administração." Admin: modo "Administrador" e acesso às telas |
| 10 | Negação de escrita/leitura pela API como membro | Passou | Ver tabela abaixo |
| 11 | Revogar cargo e salvar com a sessão antiga | Passou | Com o formulário de evento aberto, `perfis.role` do admin foi trocado para `membro` por SQL: ao salvar o app mostrou "Acesso restrito à administração." e nada foi gravado. Pela API, com o token da conta já revogada, `POST /rest/v1/eventos` deu 403 e `POST /storage/v1/object/eventos/rev.jpg` deu 403. O cargo foi restaurado depois |
| E1 | Tema escuro em todas as abas | Falhou, corrigido | Início, Bíblia, Eventos e Ajustes ficam escuros, mas o relógio e os ícones da barra de status ficavam escuros sobre fundo escuro. Corrigido (ver abaixo) |
| E2 | Foto > 4000 px | Passou | JPEG 4500x4500 enviado em ~3 s sem travar a tela; objeto na galeria com 1600x1600 (reduzido para 1600 px no maior lado) |
| E3 | Rede desligada durante o upload | Passou | Wi-Fi e dados desligados: "Não foi possível enviar a foto. Verifique a conexão ou escolha outra imagem."; o formulário continua preenchido, nenhum evento novo e nenhum objeto órfão no Storage; ao trocar a foto de um evento existente, linha de `eventos` e objetos do Storage ficaram idênticos |

### Item 10 — respostas da API com o token do membro

| Chamada | Resposta |
|---------|----------|
| `POST /rest/v1/eventos` | 403 `42501` (RLS) |
| `POST /storage/v1/object/eventos/x.jpg` | 400 com corpo `statusCode 403` (RLS) |
| `PUT` e `DELETE` em objeto existente de `eventos` | 400 com corpo `statusCode 403` |
| `POST /storage/v1/object/galeria/x.jpg` | 400 com corpo `statusCode 403` |
| `POST /rest/v1/cultos_semanais`, `/estudos`, `/fotos`, `/aniversariantes` | 403 `42501` |
| `PATCH` e `DELETE` em `eventos`, `cultos_semanais`, `fotos`; `PATCH igreja` | 200 com `[]` (RLS filtra, nenhuma linha afetada; o telefone da igreja não mudou) |
| `PATCH /rest/v1/perfis` com `role` | 403 `42501` (sem privilégio de UPDATE) |
| `GET /rest/v1/oracoes` | 200 `[]` |
| `GET /rest/v1/inscricoes` (de outra conta) | 200 `[]` |

## Falha encontrada e correção

- **Ícones da barra de status ilegíveis no tema escuro do app** (E1). Causa raiz: `enableEdgeToEdge()` decide o estilo das barras pelo modo escuro do sistema, enquanto o tema escuro do app é uma preferência própria. Correção: `SystemBarIcons` aplica a aparência das barras a partir do tema do app. Teste `SystemBarsTest` escrito antes (falhou por compilação), commit `b37fe26` (`fix(android): icones da barra de status acompanham o tema escuro do app`). Conferido no emulador depois da correção (app escuro com sistema claro): captura em `docs/referencia-visual/kotlin/inicio-escuro.png`.

## Observações (não bloqueiam)

- Mensagem de rede em inglês cru na Bíblia e telas de administração sem tratamento de falha na primeira carga: pendências já registradas das revisões, não corrigidas aqui.
- O texto da tela de cadastro ainda diz "eventos e dízimos", herdado do Expo; dízimos estão fora do escopo da versão.
- O logo da Início tem fundo branco no tema escuro (mesmo recurso do Expo).
- Dados de teste permanecem no banco local (eventos "Vigilia_Validacao" e "Lembrete_Teste", estudo, foto, aniversariante, conta `novo@local.test`); `npx supabase db reset` devolve ao estado do seed.
- `docs/referencia-visual/kotlin/`: as capturas das telas já existiam; só `inicio-escuro.png` (tema escuro, item E1) foi acrescentada.

## Pendências

- Supabase de produção (projeto real, SMTP, migrações aplicadas e verificadas lá). Em especial, autorizar `profetas://auth/recovery` em Auth > URL Configuration > Redirect URLs do projeto real (README, passo 6) e repetir a recuperação de senha clicando no e-mail; no ambiente local isso já está configurado e validado.
- Aparelho físico: lembretes sob otimização de bateria, permissão de notificação no Android 13+ em aparelho real, WhatsApp instalado, desempenho do upload com fotos reais de câmera.
- APK de release assinado (assinatura, minificação, comportamento com R8).
- Não executável no emulador: abertura do WhatsApp nativo (só o fallback de navegador foi visto).
