# Profetas de Deus — Android nativo

Aplicativo Kotlin + Jetpack Compose, separado do aplicativo Expo na raiz. Abra **esta pasta** no Android Studio. Não é necessário Node, Expo ou uma conta Apple para compilar o Android.

## Situação atual

O código das funcionalidades abaixo foi implementado e o APK debug foi gerado. **Isso não significa que todos os fluxos foram testados no Android.** Ainda não foram executados testes de interface em celular ou emulador, nem testes integrados com o Supabase.

Verificações já realizadas:

- Compilação Kotlin/Java e geração do APK debug.
- Nove testes automatizados: quatro sobre fusos, datas, lembretes e URLs; cinco sobre permissões de conteúdo para visitantes, membros, administradores e cargos revogados. Consulte `docs/VALIDACAO_ANDROID.md` na raiz do repositório para o resultado da execução.
- Android Lint sem erros, com avisos de versões de dependências e sugestões de estilo.
- Validação dos XMLs de recursos e manifesto.
- Consulta direta à API bíblica: 66 livros e Salmos 23 com 6 versículos. Foi uma verificação dos endpoints, não um teste da tela no aparelho.
- Conferência de que o `.env` está ignorado pelo Git e não está versionado.

A configuração do Supabase foi adiada a pedido do responsável. O domínio informado não resolveu no DNS durante a tentativa de conexão. Nenhuma migração foi aplicada e nenhum registro remoto foi alterado. O APK atual não contém as credenciais Supabase.

## O que falta implementar, configurar e validar

| Pendência | Tipo | Situação |
|---|---|---|
| Conectar um projeto Supabase ativo e configurar as chaves públicas no build | Configuração | Pendente |
| Revisar e aplicar as migrações no banco escolhido | Configuração | SQL preparado; ainda não aplicado |
| Configurar e-mails de autenticação e retorno da recuperação de senha | Configuração | Pendente no Supabase |
| Criar a primeira conta administradora e cadastrar os conteúdos oficiais | Configuração | Pendente |
| Validar login, cadastro, perfil, oração, inscrições e administração com dados reais | Teste integrado | Código implementado; ainda não validado com o banco |
| Testar navegação, teclado, imagens, tema e persistência em celular/emulador | Teste de interface | Ainda não executado |
| Validar lembretes, permissão de notificações e abertura de aplicativos de contato | Teste no Android | Código implementado; ainda não executado no aparelho |
| Gerar uma versão release assinada para distribuição | Entrega | Apenas APK debug gerado |

Recursos que **não foram implementados** e podem entrar em uma próxima etapa:

- Upload de fotos pelo aplicativo e armazenamento privado: atualmente o administrador cadastra uma URL pública HTTPS.
- Bíblia offline: atualmente a leitura depende da API e de internet.
- Sincronização de favoritos e conclusões de estudos entre aparelhos: atualmente são locais e separados por conta.
- Notificações push: atualmente há lembretes locais de eventos selecionados.
- Exclusão de conta pelo aplicativo, pagamentos/dízimos, transmissão ao vivo, chat e anexos.

Essa lista distingue recursos ausentes de recursos já escritos que ainda precisam de configuração ou teste. Os itens opcionais não têm implementação agendada.

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

Em Ajustes, a conta mostra **Modo usuário** ou **Modo administrador**. Apenas administradores veem a entrada Administração. As telas de edição verificam o cargo, e o acesso a dados reconsulta a permissão antes de salvar ou excluir conteúdo, inclusive para detectar acesso revogado. A proteção definitiva é a RLS do Supabase, que também precisa negar chamadas diretas feitas fora do app. Essa proteção remota depende da aplicação/revisão das migrações e dos testes com o banco ativo, ainda adiados.

O usuário comum mantém as interações pessoais autorizadas: oração, inscrições, perfil, favoritos, conclusão de estudos e preferências. Nenhuma delas permite alterar o conteúdo publicado pela igreja. Visitantes sem login consultam o conteúdo público; inscrições e dados pessoais exigem autenticação.

O administrador gerencia o conteúdo e os cadastros disponíveis no painel. Alterações no código, layout estrutural ou funcionalidades do aplicativo exigem desenvolvimento e uma nova versão do APK. A promoção de administradores fica restrita à manutenção segura do banco.

## Como testar agora, sem Supabase

### Instalar o APK já gerado

1. Encontre `app/build/outputs/apk/debug/app-debug.apk`, dentro desta pasta `android-native`.
2. Copie o APK para um celular Android 8 ou superior e abra o arquivo. Se o Android solicitar, permita a instalação por esse aplicativo de arquivos.
3. Abra **Profetas de Deus**. Se houver conflito com uma versão instalada, prefira um emulador ou outro aparelho de teste; não desinstale uma versão com dados importantes sem preservar esses dados.
4. Navegue pelas abas Início, Bíblia, Agenda, Comunidade e Ajustes. Confirme que o aplicativo abre e que é possível avançar e voltar entre as telas.
5. Em Ajustes, altere o tema, feche e abra o app novamente. A escolha deve ser preservada.
6. Com internet, abra Bíblia, escolha um livro e um capítulo e confira a leitura. Favorite um versículo, abra Favoritos, feche e reabra o app e confira a persistência. Teste Compartilhar.
7. Desative a internet, tente carregar um capítulo diferente e confira se aparece um erro com opção de tentar novamente. Reative a rede e repita.

Sem Supabase, agenda, estudos, fotos e demais conteúdos da igreja ficarão vazios ou indisponíveis. Login, oração, inscrições e administração não poderão ser validados. Não há dados fictícios para substituir o banco. Contatos exigem dados cadastrados, e lembretes exigem um evento real.

### Executar pelo Android Studio

1. Abra a pasta `android-native` como projeto, não a raiz do aplicativo Expo.
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

Resultados esperados: `BUILD SUCCESSFUL`, nove testes sem falhas e relatório de lint sem erros. Os relatórios ficam em:

- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/lint-results-debug.html`

Esses comandos não executam as telas em um dispositivo nem comprovam os fluxos com o backend. Para isso, use o roteiro manual e, após configurar o banco, a seção Validação funcional no seu Supabase.

## Preparação

1. Use Android Studio compatível com AGP 8.11.1, JDK 17 ou 21, SDK Android 36 e Build Tools 35.0.0. O Gradle Wrapper baixa o Gradle 8.13.
2. Copie `local.properties.example` para `local.properties`. Informe `sdk.dir`, `SUPABASE_URL` e `SUPABASE_ANON_KEY` (chave pública/anon; nunca `service_role`). As mesmas duas variáveis também podem ser fornecidas no ambiente de build.
3. Em um projeto Supabase novo, execute primeiro `../supabase/migrations/20260427200500_initial_schema.sql` e depois `../supabase/migrations/20261021000100_android_features.sql`. No banco existente, verifique se a migração inicial já foi aplicada; não a execute novamente. Faça backup antes de alterar o banco existente. O novo SQL não foi aplicado automaticamente.
4. Não execute `docs/supabase_setup.sql`: é o script legado com permissões divergentes. A nova migração substitui as políticas de orações e restringe atualizações de cargo.
5. Crie uma conta pelo app. Para promover a primeira conta, execute no SQL Editor, com o UUID correto: `update public.perfis set role='admin' where id='UUID-DO-USUARIO';`. A aplicação não pode promover usuários.
6. No Supabase Auth > URL Configuration, autorize `profetas://auth/recovery` nos Redirect URLs. Mantenha o template de recuperação usando o link de confirmação do Supabase. Abra o e-mail no Android para retornar ao perfil e trocar a senha. Confirmação de cadastro depende da configuração de e-mail do projeto; configure Site URL e SMTP conforme o ambiente.
7. Cadastre os dados oficiais em Ajustes > Administração: contatos, endereço, história, política de privacidade, estudos, eventos, fotos e aniversários com consentimento. Nenhum telefone, pessoa ou estudo fictício é inserido.

**Sobre o `.env`:** as credenciais fornecidas estão guardadas no `.env` da raiz do repositório, ignorado pelo Git. O build Kotlin atual **não lê esse arquivo automaticamente**. Quando a configuração for retomada, use a URL e a chave pública em `local.properties` ou nas variáveis de ambiente indicadas acima. As chaves `EXPO_PUBLIC_SUPABASE_URL` e `EXPO_PUBLIC_SUPABASE_ANON_KEY` do `.env` correspondem a `SUPABASE_URL` e `SUPABASE_ANON_KEY` no Android. `DATABASE_URL` é apenas para manutenção do banco: nunca deve entrar no aplicativo, no BuildConfig ou no commit. Alterar as chaves do build exige gerar e instalar um novo APK.

## Executar e verificar

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Instale `app/build/outputs/apk/debug/app-debug.apk` no aparelho (Android 8 ou superior). APK debug é para testes; distribuição exige uma versão release assinada com chave própria. Se já há uma versão Expo publicada, confira a versão de produção e use a mesma chave de assinatura antes de atualizá-la. O applicationId antigo foi preservado, mas versionCode 2 deve superar o publicado para permitir atualização.

## O que foi implementado

Os itens desta seção descrevem código existente. Os recursos que usam Supabase continuam pendentes de validação integrada, conforme a seção Situação atual.

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
- Fotos são cadastradas por URL pública HTTPS. Upload de arquivos e armazenamento privado não foram incluídos.
- Conclusões de estudos e versículos favoritos ficam neste aparelho, separados por conta, sem sincronização entre aparelhos. Sessões são criptografadas com Android Keystore; backups de dados do app estão desativados.
- Lembretes são locais (WorkManager), não notificações push. Precisam da permissão de notificações no Android 13+, rede para revalidar o evento e podem atrasar por otimização de bateria. Eventos alterados/cancelados não geram o lembrete antigo; para uma nova data, desative e reative o lembrete. Abrir pelo lembrete navega ao evento.
- Sem credenciais do projeto, o app abre com estados vazios e instruções de configuração. O banco não é modificado pelo build.
- Exclusão de conta, pagamentos/dízimos, transmissão ao vivo, chat, notificações push e anexos não fazem parte desta implementação.

## Validação funcional no seu Supabase

1. Visitante: abra agenda e estudos, envie uma oração; não deve conseguir ler orações ou aniversariantes.
2. Membro: crie conta, confirme e-mail se solicitado, entre e altere o nome; tentativas REST de alterar `role` devem falhar.
3. Admin: cadastre eventos com data futura, estudo com conteúdo, URL de foto e aniversário autorizado. Confirme a exibição nas telas correspondentes.
4. Membro: filtre eventos, inscreva-se e confira Minhas inscrições. Repita para verificar duplicidade; cancele. Outro membro não pode consultar suas inscrições.
5. Confira que somente admin pode ler pedidos de oração, administrar conteúdo e consultar inscrições de todos.
6. Teste leitura bíblica, erro de rede, favoritos, estudos concluídos e persistência após fechar o app; verifique separação ao trocar de conta.
7. Cadastre contatos oficiais e abra cada aplicativo. Teste recuperação de senha no mesmo Android.
8. Autorize notificações e selecione um evento futuro; valide lembrete, cancelamento e abertura do evento.
9. Confirme em Ajustes os modos Usuário e Administrador com duas contas diferentes. O membro não deve ver Administração nem acessar uma tela de edição por navegação direta.
10. Com uma conta membro, tente inserir, atualizar e excluir conteúdo pela API: a RLS deve negar as operações. Tente ler pedidos de oração e inscrições de outra conta: nenhum dado deve ser exposto. Não use credencial administrativa para simular esse teste.
11. Revogue o cargo de uma conta administradora pela manutenção do banco e tente salvar conteúdo pela sessão antiga. A operação deve ser negada, mesmo se um botão antigo ainda estiver visível até atualizar a tela.
