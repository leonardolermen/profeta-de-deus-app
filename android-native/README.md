# Profetas de Deus — Android nativo

Aplicativo Kotlin + Jetpack Compose. Abra **esta pasta** no Android Studio. Não é necessário Node, Expo ou uma conta Apple para compilar o Android. O backend fica em `../supabase/` (veja o README da raiz).

## Situação atual

O app foi validado de ponta a ponta contra o **Supabase local** (Docker) em emulador Android API 36, em 26/10/2026. O registro item a item, com evidências e a falha encontrada e corrigida, está em `../docs/VALIDACAO_ANDROID.md`.

Verificações já realizadas:

- `gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: build ok, 96 testes JUnit sem falhas, lint sem erros (apenas avisos de versões de dependências e estilo).
- `npx supabase test db`: testes pgTAP das políticas de fotos, cultos e Storage.
- Roteiro funcional de 11 itens mais extras no emulador: visitante, membro e administrador, inscrições, orações, upload de fotos, recuperação de senha, lembretes, tema escuro e negação de escrita pela API (RLS) com o token de um membro.
- Consulta à API bíblica: 66 livros e Salmos 23 com 6 versículos.

Nada disso foi feito contra um Supabase de produção, nem em celular físico.

## O que falta configurar e validar

| Pendência | Tipo | Situação |
|---|---|---|
| Criar o projeto Supabase de produção e aplicar as migrações de `../supabase/migrations/` | Configuração | Pendente |
| Configurar no Supabase de produção o redirect `profetas://auth/recovery`, o Site URL e o SMTP | Configuração | Pendente |
| Promover a primeira conta administradora e cadastrar os conteúdos oficiais | Configuração | Pendente |
| Repetir o roteiro funcional contra o banco de produção | Teste integrado | Validado apenas no Supabase local |
| Testar em celular físico (teclado, imagens, desempenho, bateria nos lembretes) | Teste de interface | Validado apenas no emulador |
| Gerar uma versão release assinada para distribuição | Entrega | Apenas APK debug gerado |

Recursos que **não foram implementados** e podem entrar em uma próxima etapa:

- Bíblia offline: atualmente a leitura depende da API e de internet.
- Sincronização de favoritos e conclusões de estudos entre aparelhos: atualmente são locais e separados por conta.
- Notificações push: atualmente há lembretes locais de eventos selecionados.
- Exclusão de conta pelo aplicativo, pagamentos/dízimos, transmissão ao vivo, chat e anexos.

Fotos de eventos e da galeria são enviadas pelo seletor do aparelho para o Storage do Supabase (a imagem é reduzida a 1600 px no maior lado antes do envio). Os itens opcionais acima não têm implementação agendada.

## Modos de acesso: administrador e usuário

O modo é definido pelo cargo da conta na tabela protegida `public.perfis`. Não existe botão para se promover a administrador; criar uma conta gera um membro comum. O app não usa nome, e-mail ou metadados enviados no cadastro para conceder administração.

| Ação | Usuário comum | Administrador |
|---|---|---|
| Consultar agenda, Bíblia, estudos, fotos e informações da igreja | Sim | Sim |
| Consultar aniversariantes autorizados, após entrar | Sim | Sim |
| Enviar pedido de oração | Sim | Sim |
| Inscrever-se e cancelar a própria inscrição | Sim | Sim |
| Alterar o próprio nome/senha e preferências pessoais | Sim | Sim |
| Cadastrar, editar ou excluir conteúdo da igreja | Não | Sim |
| Ler pedidos de oração recebidos | Não | Sim |
| Consultar e cancelar inscrições de outras pessoas | Não | Sim |
| Alterar o cargo da própria conta pelo aplicativo | Não | Não |

Em Ajustes, a conta mostra **Modo usuário** ou **Modo administrador**. Apenas administradores veem a entrada Administração. As telas de edição verificam o cargo, e o acesso a dados reconsulta a permissão antes de salvar ou excluir conteúdo, inclusive para detectar acesso revogado. A proteção definitiva é a RLS do Supabase, que nega chamadas diretas feitas fora do app (comprovado no Supabase local com o token de um membro). Em produção, ela só existe depois de aplicadas as migrações.

O usuário comum mantém as interações pessoais autorizadas: oração, inscrições, perfil, favoritos, conclusão de estudos e preferências. Nenhuma delas permite alterar o conteúdo publicado pela igreja. Visitantes sem login consultam o conteúdo público; inscrições e dados pessoais exigem autenticação.

O administrador gerencia o conteúdo e os cadastros disponíveis no painel. Alterações no código, layout estrutural ou funcionalidades do aplicativo exigem desenvolvimento e uma nova versão do APK. A promoção de administradores fica restrita à manutenção segura do banco.

## Como testar sem configurar um Supabase

### Instalar o APK já gerado

1. Encontre `app/build/outputs/apk/debug/app-debug.apk`, dentro desta pasta `android-native`.
2. Copie o APK para um celular Android 8 ou superior e abra o arquivo. Se o Android solicitar, permita a instalação por esse aplicativo de arquivos.
3. Abra **Profetas de Deus**. Se houver conflito com uma versão instalada, prefira um emulador ou outro aparelho de teste; não desinstale uma versão com dados importantes sem preservar esses dados.
4. Navegue pelas abas Início, Bíblia, Eventos e Ajustes. Confirme que o aplicativo abre e que é possível avançar e voltar entre as telas.
5. Em Ajustes, altere o tema, feche e abra o app novamente. A escolha deve ser preservada.
6. Com internet, abra Bíblia, escolha um livro e um capítulo e confira a leitura. Favorite um versículo, abra Favoritos, feche e reabra o app e confira a persistência. Teste Compartilhar.
7. Desative a internet, tente carregar um capítulo diferente e confira se aparece um erro com opção de tentar novamente. Reative a rede e repita.

Sem Supabase, agenda, estudos, fotos e demais conteúdos da igreja ficarão vazios ou indisponíveis. Login, oração, inscrições e administração não poderão ser validados. Não há dados fictícios para substituir o banco. Contatos exigem dados cadastrados, e lembretes exigem um evento real.

### Executar pelo Android Studio

1. Abra a pasta `android-native` como projeto, não a raiz do repositório.
2. Instale os requisitos da seção Preparação e aguarde a sincronização do Gradle.
3. Para testar apenas os recursos locais e a Bíblia, as credenciais Supabase podem continuar vazias.
4. Crie e inicie um dispositivo virtual pelo Device Manager, ou conecte um celular com depuração USB habilitada.
5. Selecione o dispositivo e execute a configuração `app` pelo botão Run.
6. Repita o roteiro acima. Se ocorrer erro, registre a tela, os passos e a mensagem; consulte também o Logcat do Android Studio.

### Repetir as verificações automatizadas

No terminal, dentro de `android-native`, com JDK e SDK configurados:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Resultados esperados: `BUILD SUCCESSFUL`, todos os testes JUnit sem falhas e relatório de lint sem erros. Os relatórios ficam em:

- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/lint-results-debug.html`

Esses comandos não executam as telas em um dispositivo nem comprovam os fluxos com o backend. Para isso, use o roteiro manual e, após configurar o banco, a seção Validação funcional no seu Supabase.

## Preparação

1. Use Android Studio compatível com AGP 8.11.1, JDK 17 ou 21, SDK Android 36 e Build Tools 35.0.0. O Gradle Wrapper baixa o Gradle 8.13.
2. Copie `local.properties.example` para `local.properties`. Informe `sdk.dir`, `SUPABASE_URL` e `SUPABASE_ANON_KEY` (chave pública/anon; nunca `service_role`). As mesmas duas variáveis também podem ser fornecidas no ambiente de build. Para o Supabase local no emulador, use `http://10.0.2.2:54321` e a chave anon que `npx supabase status` imprime. O build não lê nenhum `.env`.
3. Em um projeto Supabase novo, aplique todas as migrações de `../supabase/migrations/` em ordem de nome (`npx supabase db push`, ou pelo SQL Editor). Em um banco existente, confira quais já foram aplicadas e faça backup antes. Nada é aplicado automaticamente pelo build.
   - `20261023000100_fotos_tipos_cultos.sql` remove a constraint `fotos_url_check` pelo nome que o Postgres gerou para ela em `20261021000100`. Num banco de produção criado de outro jeito (por exemplo, pelo script legado), o nome pode ser outro e a migração falha: confira antes com `\d public.fotos`. As migrações existentes não são editadas; se o nome divergir, ajuste o banco antes de aplicar.
4. Não execute `docs/supabase_setup.sql`: é o script legado com permissões divergentes. A nova migração substitui as políticas de orações e restringe atualizações de cargo.
5. Crie uma conta pelo app. Para promover a primeira conta, execute no SQL Editor, com o UUID correto: `update public.perfis set role='admin' where id='UUID-DO-USUARIO';`. A aplicação não pode promover usuários.
6. No Supabase Auth > URL Configuration, autorize `profetas://auth/recovery` nos Redirect URLs. Mantenha o template de recuperação usando o link de confirmação do Supabase. Abra o e-mail no Android para retornar ao perfil e trocar a senha. Confirmação de cadastro depende da configuração de e-mail do projeto; configure Site URL e SMTP conforme o ambiente.
7. Cadastre os dados oficiais em Ajustes > Administração: contatos, endereço, história, política de privacidade, estudos, eventos, fotos e aniversários com consentimento. Nenhum telefone, pessoa ou estudo fictício é inserido.

**Sobre as chaves:** `SUPABASE_URL` e `SUPABASE_ANON_KEY` vêm de `local.properties` (ignorado pelo Git) ou do ambiente. A string de conexão direta ao banco serve só para manutenção: nunca deve entrar no aplicativo, no BuildConfig ou em um commit. Alterar as chaves do build exige gerar e instalar um novo APK.

## Executar e verificar

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Instale `app/build/outputs/apk/debug/app-debug.apk` no aparelho (Android 8 ou superior). APK debug é para testes; distribuição exige uma versão release assinada com chave própria. Se já há uma versão antiga publicada (do aplicativo Expo), use a mesma chave de assinatura dela. O applicationId antigo foi preservado, mas o versionCode (2) deve superar o publicado para permitir a atualização.

## O que foi implementado

Os itens desta seção descrevem código existente, validado no Supabase local (veja Situação atual).

- Início com os mesmos eventos reais da agenda, ordenados por data.
- Agenda com busca, filtro por tipo, histórico, detalhes e compartilhamento.
- Inscrição em eventos e batismos, com consentimento de contato, bloqueio de duplicidade e cancelamento pelo usuário.
- Pedidos de oração para visitantes e membros, visíveis somente a administradores.
- Bíblia NVI com livros/capítulos e texto real da API A Bíblia Digital; favoritos e compartilhamento.
- Estudos publicados no banco, leitura completa, busca, conclusão local e compartilhamento.
- Aniversariantes por mês atual, acessíveis a membros, somente com consentimento de divulgação.
- Galeria por álbum, detalhe de imagem, erros de carregamento e compartilhamento de link.
- Contatos por intents reais de WhatsApp, discador, e-mail e mapa.
- Login, cadastro, logout, perfil, alteração e recuperação de senha.
- Tema claro/escuro persistente e lembretes locais de eventos escolhidos.
- Administração com criação/edição/exclusão de conteúdo, inscrições recebidas e pedidos de oração.

## Limites explícitos

- Supabase e API bíblica exigem internet. A API bíblica usa `https://abibliadigital.api.br/api/`, o domínio oficial atual (o endereço antigo do Expo retornou erro na verificação). Os endpoints públicos de livros e capítulo foram verificados: 66 livros e Salmos 23 com 6 versículos. Disponibilidade, autenticação, limites e uso da tradução dependem do [provedor](https://abibliadigital.api.br/). Se exigido, configure `BIBLE_API_TOKEN` no build. Não há Bíblia offline nem texto simulado de fallback.
- Fotos são enviadas pelo seletor do aparelho para buckets do Supabase Storage (`eventos`, `galeria`) e exibidas por URL pública. Não há armazenamento privado de fotos.
- Conclusões de estudos e versículos favoritos ficam neste aparelho, separados por conta, sem sincronização entre aparelhos. Sessões são criptografadas com Android Keystore; backups de dados do app estão desativados.
- Lembretes são locais (WorkManager), não notificações push. Precisam da permissão de notificações no Android 13+, rede para revalidar o evento e podem atrasar por otimização de bateria. Eventos alterados/cancelados não geram o lembrete antigo; para uma nova data, desative e reative o lembrete. Abrir pelo lembrete navega ao evento.
- Sem credenciais do projeto, o app abre com estados vazios e instruções de configuração. O banco não é modificado pelo build.
- Exclusão de conta, pagamentos/dízimos, transmissão ao vivo, chat, notificações push e anexos não fazem parte desta implementação.

## Validação funcional no seu Supabase

1. Visitante: abra agenda e estudos, envie uma oração; não deve conseguir ler orações ou aniversariantes.
2. Membro: crie conta, confirme e-mail se solicitado, entre e altere o nome; tentativas REST de alterar `role` devem falhar.
3. Admin: cadastre eventos com data futura, estudo com conteúdo, foto de galeria e aniversário autorizado. Confirme a exibição nas telas correspondentes.
4. Membro: filtre eventos, inscreva-se e confira Minhas inscrições. Repita para verificar duplicidade; cancele. Outro membro não pode consultar suas inscrições.
5. Confira que somente admin pode ler pedidos de oração, administrar conteúdo e consultar inscrições de todos.
6. Teste leitura bíblica, erro de rede, favoritos, estudos concluídos e persistência após fechar o app; verifique separação ao trocar de conta.
7. Cadastre contatos oficiais e abra cada aplicativo. Teste recuperação de senha no mesmo Android.
8. Autorize notificações e selecione um evento futuro; valide lembrete, cancelamento e abertura do evento.
9. Confirme em Ajustes os modos Usuário e Administrador com duas contas diferentes. O membro não deve ver Administração nem acessar uma tela de edição por navegação direta.
10. Com uma conta membro, tente inserir, atualizar e excluir conteúdo pela API: a RLS deve negar as operações. Tente ler pedidos de oração e inscrições de outra conta: nenhum dado deve ser exposto. Não use credencial administrativa para simular esse teste.
11. Revogue o cargo de uma conta administradora pela manutenção do banco e tente salvar conteúdo pela sessão antiga. A operação deve ser negada, mesmo se um botão antigo ainda estiver visível até atualizar a tela.
