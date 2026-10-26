# Conversão completa para Kotlin — design

Data: 23/10/2026

## Intenção

O app da Igreja Profetas de Deus passa a existir só como Android nativo (Kotlin + Jetpack Compose), em
`android-native/`. A decisão foi tomada pelo responsável depois de comparar com o Expo: o alvo é Android,
o admin usa o celular.

Hoje o app Kotlin já cobre as funcionalidades do Expo e foi além (administração, inscrições, lembretes,
recuperação de senha, sessão criptografada), mas tem a cara do Material padrão. O Expo tem a cara da
igreja: fotos, azul institucional, capa, atalhos, cards com sombra, eventos com foto e data em "ticket".

O objetivo é que quem abra o app Kotlin veja **a mesma igreja que vê no Expo** — literalmente todas as
telas convertidas, com fotos e cores — mantendo tudo o que o Kotlin ganhou. Depois disso, o Expo é
removido do repositório.

Melhorias são bem-vindas (palavras do responsável), desde que não descaracterizem o visual.

## Objetivos

1. **Paridade visual com o Expo, tela por tela.** Referência: `app/(tabs)/index.tsx`, `explore.tsx`
   (Bíblia), `eventos.tsx`, `settings.tsx`, `app/oracao.tsx`, `campanhas.tsx`, `batismo.tsx`,
   `estudos.tsx`, `aniversariantes.tsx`, `galeria.tsx`, `historia.tsx`, `contato.tsx`,
   `app/auth/login.tsx`, `app/auth/register.tsx`.
2. **Upload de foto pelo celular** para eventos (pedido explícito do responsável) e para a galeria.
3. **Manter todas as funcionalidades atuais do Kotlin** (ver `android-native/README.md`, seção
   "O que foi implementado").
4. **Código no padrão do `~/.claude/CLAUDE.md` do responsável** (nomes por extenso, pastas por conceito,
   respiro, chaves sempre, um comando por linha, até 120 colunas, comentários de "por quê").
5. **Remover o Expo** ao final.
6. **Ambiente de desenvolvimento e teste local** nesta máquina, sem ocupar o disco C.

## Não-objetivos

- iOS e web. Não há painel web: a administração é no app.
- Bíblia offline, notificações push, pagamentos/dízimos, chat, exclusão de conta pelo app (continuam fora,
  como já listado no README do Android).
- Configurar o Supabase de produção. O responsável vai trazer as variáveis depois; até lá tudo roda no
  Supabase local em Docker.
- Inventar conteúdo. Textos institucionais, contatos e eventos vêm do banco. Nada de dado fictício
  embutido no APK (como os eventos fixos "20 MAI – Encontro de Mulheres" do Expo).

## Anti-objetivos (falha mesmo que funcione)

- Ficar com cara de Material genérico: a conversão existe para ter a cara da igreja.
- Perder qualquer funcionalidade que o Kotlin já tem, ou afrouxar as permissões (RLS, cargo admin só
  pelo banco, inscrições privadas, aniversariantes com consentimento).
- Encher o disco C.
- Apagar o Expo antes de capturar a referência visual de todas as telas.

## Restrições

- **Disco:** C tem ~33 GB livres e não pode crescer. Android SDK, AVD e cache do Gradle vão no **E:**
  (~73 GB livres): `ANDROID_SDK_ROOT`, `ANDROID_AVD_HOME` e `GRADLE_USER_HOME` apontando para E.
  Instalação só por linha de comando (sem Android Studio). Estimativa: 12–15 GB.
  Atenção: o disco do Docker Desktop está hoje no C
  (`%LOCALAPPDATA%\Docker\wsl\disk\docker_data.vhdx`). As imagens do Supabase local ocupam alguns GB.
  O responsável aceitou manter o Docker no C.
- **Docker Desktop** está instalado mas parado; precisa ser iniciado.
- **Supabase local:** via Supabase CLI (`npx supabase` ou binário), usando `supabase/config.toml` e as
  migrações de `supabase/migrations/`. O emulador acessa o host em `http://10.0.2.2:54321`. Como é
  HTTP, o cleartext é liberado **apenas no build debug** (network security config de debug); release
  continua só HTTPS. A validação de URL de fotos (`validHttps` e o `check` da tabela `fotos`) precisa
  aceitar a URL do Storage local em debug sem abrir HTTP em produção.
- **Credenciais:** nunca `service_role` no app. `local.properties` e `.env` ficam fora do Git.
- **Ferramentas existentes:** AGP 8.11.1, Gradle 8.13, JDK 17 instalado (README aceita 17 ou 21),
  SDK 36. Manter as dependências atuais; adicionar só o necessário.
- **Commits:** renome/formatação nunca no mesmo commit que mudança de lógica (regra do `CLAUDE.md`).
  A reestruturação do código existente vira commits próprios, separados das telas novas.

## O que foi decidido

### Visual

- Paleta do Expo (`constants/Colors.ts` e cores inline das telas): primária `#0056B3`, destaque
  `#00A8E8`, texto `#0B132B`, superfície `#F4F7FA`, borda `#E1E9F0`, texto secundário `#6B7A90`, fundo
  branco. Vira o `ColorScheme` claro do tema Compose.
- Estrutura de cada tela segue o Expo: na Início, capa com o logo (`new-logo-v2.png`), título "Igreja
  Profetas de Deus", atalhos Oração / Projetos / Parabéns, card "Quem somos" com "Saiba mais sobre nós"
  (→ História), carrossel "Próximos Eventos" com cards de foto + overlay escuro + ticket de dia/mês +
  horário, card "Onde nos encontrar" com "Como chegar" (→ mapa), rodapé "© ano Profetas de Deus".
- Entram como recursos do APK as imagens que alguma tela do Expo usa: `batismo.jpg`, `jovens.jpg`,
  `culto-mulheres.jpg`, `card1.png` e o logo (`new-logo-v2.png`, já presente como `logo.png`). `pombo.jpg`,
  `biblia.jpg`, `mulher.png`, `card2.png` e as do template (`react-logo*`) não são usadas por nenhuma tela e
  ficam de fora.
- Textos institucionais que hoje estão fixos no Expo ("Quem somos", endereço) passam a vir da tabela
  `igreja` (história, endereço). Sem dado cadastrado, o card mostra um estado vazio decente, não texto
  inventado.
- **Tema escuro mantido** (melhoria, decisão do Claude): o Expo só tem claro, o Kotlin já tem a
  preferência. Fazer uma variante escura coerente com a paleta.

### Fotos de eventos e galeria

- Admin cria/edita evento → botão "Escolher foto" → seletor de fotos do Android (Photo Picker) → app
  redimensiona/comprime (decisão do Claude: lado maior ~1600 px, JPEG ~80%, para caber no plano gratuito
  do Supabase) → upload para o **Supabase Storage**.
- Bucket `eventos` (e `galeria` para a galeria — decisão do Claude): leitura pública, escrita/remoção só
  admin via `public.is_admin()` nas políticas de `storage.objects`.
- Nova migração adiciona a coluna de imagem em `eventos` (o caminho/URL da foto) e as políticas do
  Storage. Não alterar migrações já existentes.
- Evento sem foto → imagem padrão por tipo, embutida no APK: `batismo` → `batismo.jpg`, culto jovem →
  `jovens.jpg`, culto de mulheres → `culto-mulheres.jpg`, demais → `card1.png` (a mesma que o Expo usa em `eventos.tsx`).
- Galeria troca "colar URL HTTPS" por upload do celular, com álbum. A tabela `fotos` continua; ajustar
  o `check` de URL numa migração nova para aceitar a URL do Storage.
- Ao trocar ou excluir a foto de um evento/galeria, o arquivo antigo no Storage é removido.
- O banco guarda o **caminho no bucket**, não a URL (decisão do Claude): um `check` de banco não distingue
  debug de release, e o app monta a URL pública a partir de `SUPABASE_URL`. Assim a mesma linha funciona no
  Supabase local e no de produção.
- Tipos de evento passam a ser `culto`, `jovens`, `mulheres`, `campanha`, `batismo` (decisão do Claude),
  oferecidos no editor e nos filtros da agenda. `jovens` → `jovens.jpg`, `mulheres` → `culto-mulheres.jpg`.

### Supabase local em debug

- `Repository.configured` hoje exige HTTPS em `SUPABASE_URL`; no build debug precisa aceitar
  `http://10.0.2.2:54321`. Release continua exigindo HTTPS.
- Para capturar a referência do Expo web, `lib/supabase.ts` precisa apontar para o Supabase local (sem
  `.env` ele chama `createClient('')`).

### Navegação

- **Fiel ao Expo** (decisão do responsável): 4 abas — Início, Bíblia, Eventos, Ajustes. A aba Comunidade
  do Kotlin some. Os acessos seguem o Expo: Oração / Projetos (campanhas) / Parabéns (aniversariantes)
  nos atalhos da Início; "Saiba mais" → História; Estudos dentro da Bíblia ("Estudos e Trilhas");
  Galeria e Batismo dentro de Eventos; Contato e Privacidade dentro de Ajustes.
- O que o Kotlin tem e o Expo não (Minhas inscrições, Administração, perfil, lembretes) vai para Ajustes,
  onde o Expo já tem a seção Conta.

### Cultos semanais

- A aba Eventos do Expo tem um bloco fixo de cultos semanais (Culto da Família no domingo, Culto de
  Libertação na quarta, 08h/15h/18h, R. Marli, 191). **O admin cadastra** (decisão do responsável):
  nova tabela de cultos semanais (nome, dia da semana, horários, local), leitura pública, escrita só
  admin via RLS, e editor na Administração. Nada fica fixo no APK; sem cadastro, a seção mostra estado
  vazio.

### Campanhas e batismo

- No Expo, o atalho "Projetos" abre Campanhas ("Nossos Projetos") e Batismo tem tela própria. No
  Kotlin esses viraram filtros da agenda. **Voltam a ter telas próprias** com o visual do Expo,
  alimentadas pelos eventos de `tipo = 'campanha'` e `tipo = 'batismo'` (decisão do Claude).
  A agenda continua tendo os filtros também.
- Batismo mostra o próximo batismo futuro, com inscrição (que o Kotlin já tem) e o botão de contato
  que o Expo tem.

### Código

- Reescrever as telas existentes no padrão do `CLAUDE.md` em vez de só trocar estilos: hoje são telas
  inteiras em uma linha, nomes como `vm`, `s`, `nav`, `if` sem chaves.
- Pastas por conceito dentro de `br.org.profetas.app`: por exemplo `home/`, `event/`, `campaign/`,
  `baptism/`, `bible/`, `study/`, `prayer/`, `birthday/`, `gallery/`, `church/` (história, contato,
  privacidade), `account/`, `admin/`, `reminder/`, `theme/`, e `support/` só para o que é de todos
  (cliente HTTP, sessão, componentes visuais compartilhados). A divisão exata fica com o construtor.
- `AppViewModel` único e `Repository` único estão grandes; dividir por conceito (decisão do Claude),
  respeitando o limite de ~300 linhas por arquivo e ~7 dependências por construtor.
- Testes existentes (9 JUnit) continuam passando. Testes novos para regras novas (imagem padrão por
  tipo, redimensionamento, validação de URL do Storage, telas de campanha/batismo filtrando o tipo
  certo). Testes espelham a estrutura de main.

### Referência visual antes de apagar

- Antes de remover o Expo, rodar o Expo no navegador (`npx expo start --web`) e capturar uma imagem de
  cada tela em largura de celular, salvando em `docs/referencia-visual/`. Essas imagens são o gabarito
  da paridade e ficam no repositório depois que o Expo sair. O `npm install` vai para o C (node_modules
  dentro do repositório, que está no C): é temporário e sai junto com o Expo.
- Paridade é conferida comparando screenshot do emulador com a referência, tela por tela.

### Remoção do Expo

- Sai: `app/`, `components/`, `constants/`, `hooks/`, `lib/`, `services/`, `functions/`, `scripts/`,
  `assets/` (depois de copiar as imagens para o Android), `.expo/`, `.eas/`, `app.json`, `eas.json`,
  `package.json`, `tsconfig.json`, `.npmignore`, `.env.example` (do Expo), e o `README.md` do template.
- Fica: `android-native/`, `supabase/`, `docs/`.
- `README.md` da raiz passa a apontar para o Android e o Supabase local.
- `docs/01_INTEGRACAO_BACKEND.md`, `02_USER_STORIES.md` e `03_TAREFAS.md` descrevem o plano antigo (API
  própria, painel web). Atualizar `03_TAREFAS.md` para refletir o estado real e marcar as outras como
  histórico — não apagar.
- Remoção em commit próprio, depois que a paridade foi conferida.

## Ordem sugerida

1. Ambiente no E: SDK, emulador, Gradle; build e testes atuais passando aqui.
2. Supabase local no Docker com as migrações; usuário admin de teste local.
3. Capturar a referência visual do Expo.
4. Reestruturar o código Kotlin (sem mudar comportamento; commits de estrutura separados).
5. Tema e componentes visuais compartilhados.
6. Telas, uma a uma, comparando com a referência.
7. Upload de fotos (migração, Storage, editor de evento, galeria).
8. Validação funcional do README no Supabase local e no emulador.
9. Remover o Expo e atualizar docs.

## Deixado para o construtor

- Divisão exata de pacotes e classes, desde que siga o `CLAUDE.md`.
- Biblioteca de recorte de imagem (ou nenhuma, se o Photo Picker + redimensionamento bastar).
- Nome exato da coluna de imagem e formato guardado (caminho no bucket vs URL pública).
- Detalhes do tema escuro.
- Como capturar as screenshots do Expo web (navegador/automação).
