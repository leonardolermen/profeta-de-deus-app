# Conversão completa para Kotlin — plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** o app Android (`android-native/`) fica com o visual do Expo em todas as telas, ganha upload de fotos e
cultos semanais cadastrados pelo admin, segue o padrão de código do responsável, e o Expo é removido.

**Architecture:** o `Repository` e o `AppViewModel` únicos são quebrados por conceito (um pacote por conceito
do domínio, cada um com modelo, repositório, ViewModel e telas). A camada HTTP/sessão vai para `support/`. Cada
tela é reescrita em Compose a partir do arquivo Expo correspondente e conferida por screenshot contra a referência
capturada do Expo. Backend: Supabase local em Docker com uma migração nova (fotos, tipos, cultos semanais,
Storage).

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2025.04.01), Material 3, Navigation Compose 2.9.0, OkHttp 4.12.0,
Coil 2.7.0, WorkManager 2.10.1, JUnit 4, OkHttp MockWebServer 4.12.0, org.json (testes JVM), Supabase CLI
(local), pgTAP (`supabase test db`).

**Spec:** `docs/superpowers/specs/2026-10-23-conversao-kotlin-design.md`

## Global Constraints

- Código segue `C:\Users\leona\.claude\CLAUDE.md`: nomes por extenso (`viewModel`, `navController`, `state`,
  nunca `vm`, `nav`, `s`, `e`, `b`), chaves sempre, um comando por linha, linha ≤ 120 colunas, continuação com
  4 espaços, linha em branco entre blocos lógicos, comentário só de "por quê" (os existentes de "por quê" viajam
  com o código), arquivo ≤ ~300 linhas, construtor ≤ ~7 dependências, sem parâmetro booleano que muda
  comportamento, `when` sobre enum em um lugar só.
- Pastas por conceito dentro de `br.org.profetas.app`; `support/` só para o que serve a todos. Testes espelham
  main arquivo por arquivo.
- Paleta (claro), exata: primária `#0056B3`, destaque `#00A8E8`, texto `#0B132B`, superfície `#F4F7FA`, borda
  `#E1E9F0`, texto secundário `#6B7A90`, fundo `#FFFFFF`, branco sobre foto `#FFFFFF`, badge de dia `#EBF4FA`.
- Textos de interface em português do Brasil, copiados do Expo quando a tela existe lá.
- Nenhum dado fictício no APK. Dados de exemplo só em `supabase/seed.sql` (usado apenas pelo Supabase local).
- Nunca `service_role` no app. `local.properties` e `.env` fora do Git.
- Android SDK, AVD e Gradle no E: (`ANDROID_HOME=E:\Android\Sdk`, `ANDROID_AVD_HOME=E:\Android\avd`,
  `ANDROID_USER_HOME=E:\Android\user`, `GRADLE_USER_HOME=E:\Android\gradle`, já configurados como variáveis do
  usuário). AVD: `profetas`. Docker pode usar o C (autorizado).
- Release continua só HTTPS; HTTP para `10.0.2.2` só no build debug.
- Migrações existentes não são editadas; mudanças de banco entram em migração nova.
- Commits: estrutura/renome/formatação nunca no mesmo commit que lógica. Cada commit termina com
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Trabalho num branch `conversao-kotlin`, não em `main`.
- Comando de verificação padrão (em `android-native/`):
  `.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` → `BUILD SUCCESSFUL`, sem falhas.

## Review Focus

1. **Foto escolhida enorme ou em HEIC/PNG com transparência** → o app reduz para lado maior 1600 px, grava JPEG
   80% e envia; nunca trava a UI nem envia o original de 12 MB. Teste em Task 16 (`ImageResizeTest`).
2. **Admin troca a foto de um evento e o upload falha no meio** → o evento mantém a foto antiga e o arquivo novo
   órfão não fica referenciado; a foto antiga só é apagada depois que o `PATCH` do evento deu certo. Teste em
   Task 17 (`EventPhotoReplacementTest`).
3. **Evento com `tipo` legado desconhecido no banco** (ex.: `"Culto"` ou `"retiro"`) → aparece como culto, com a
   imagem padrão, sem quebrar a lista. Teste em Task 5 (`EventTypeTest.unknownTypeFallsBackToCulto`).
4. **Membro comum tentando gravar no Storage ou em `cultos_semanais` pela API** → negado pela RLS. Teste pgTAP em
   Task 1 (`supabase/tests/fotos_e_cultos_test.sql`).
5. **Sessão expira durante o upload** (401) → renova o token uma vez e repete; se a renovação falhar, sai da conta
   com a mensagem "Sua sessão expirou. Entre novamente." Teste em Task 3 (`SupabaseClientTest`).

---

## Estrutura de arquivos (alvo)

Raiz do código: `android-native/app/src/main/java/br/org/profetas/app/`. Testes no mesmo caminho em
`src/test/java/...`.

```
MainActivity.kt
ProfetasApplication.kt          # cria o AppContainer
AppContainer.kt                 # instancia SupabaseClient e repositórios (DI manual)
navigation/
  ProfetasApp.kt                # Scaffold, barra de 4 abas, NavHost
  Route.kt                      # rotas tipadas (sealed) e parsing do deep link de debug
theme/
  Palette.kt                    # cores exatas claro/escuro
  ProfetasTheme.kt
support/
  http/SupabaseConfig.kt  http/SupabaseClient.kt  http/ApiException.kt  http/SecureUrl.kt
  session/Session.kt  session/SessionStorage.kt  session/KeystoreSessionStorage.kt
  storage/ImageResize.kt  storage/StorageBucket.kt  storage/ImageUploader.kt
  ui/ScreenColumn.kt  ui/SectionCard.kt  ui/QuickAction.kt  ui/SearchBox.kt  ui/StatusViews.kt
  ui/AppTextField.kt  ui/PrimaryButton.kt
  intent/ExternalIntents.kt     # abrir link, compartilhar
  message/UserMessages.kt       # fila de snackbar compartilhada
account/   Profile.kt ContentAccess.kt AccountRepository.kt AccountViewModel.kt
           LoginScreen.kt RegisterScreen.kt RecoveryScreen.kt ProfileScreen.kt
event/     Event.kt EventType.kt EventDates.kt EventRepository.kt EventsViewModel.kt
           EventsScreen.kt EventDetailScreen.kt PhotoEventCard.kt DateTicket.kt
event/registration/  Registration.kt RegistrationRepository.kt RegistrationForm.kt MyRegistrationsScreen.kt
event/reminder/      EventReminderWorker.kt ReminderScheduler.kt ReminderDelay.kt
event/weekly/        WeeklyService.kt WeeklyServiceRepository.kt WeeklyServicesSection.kt
campaign/  CampaignsScreen.kt
baptism/   BaptismScreen.kt
home/      HomeScreen.kt
bible/     Book.kt Chapter.kt BibleRepository.kt BibleViewModel.kt BibleScreen.kt FavoriteVerses.kt
study/     Study.kt StudyRepository.kt StudiesViewModel.kt StudiesScreen.kt StudyScreen.kt CompletedStudies.kt
prayer/    Prayer.kt PrayerRepository.kt PrayerViewModel.kt PrayerScreen.kt
birthday/  Birthday.kt BirthdayRepository.kt BirthdaysViewModel.kt BirthdaysScreen.kt
gallery/   Photo.kt GalleryRepository.kt GalleryViewModel.kt GalleryScreen.kt PhotoScreen.kt
church/    Church.kt ChurchRepository.kt ChurchViewModel.kt HistoryScreen.kt ContactScreen.kt PrivacyScreen.kt
settings/  ThemePreference.kt SettingsScreen.kt
admin/     AdminViewModel.kt AdminScreen.kt EventEditor.kt StudyEditor.kt PhotoEditor.kt BirthdayEditor.kt
           ChurchEditor.kt WeeklyServiceEditor.kt PrayersInbox.kt RegistrationsInbox.kt PhotoPickerField.kt
```

O que sai ao longo do plano: `AppViewModel.kt`, `EventReminder.kt`, `data/` e `ui/` inteiros.

Recursos: `res/drawable/` recebe `batismo.jpg`, `jovens.jpg`, `culto_mulheres.jpg`, `card1.png` (padrão de
culto/campanha, como no Expo `eventos.tsx`), e `logo.png` já existe (é o `new-logo-v2.png`). `pombo.jpg`,
`biblia.jpg`, `mulher.png`, `card2.png` não são usados por nenhuma tela do Expo e não entram.

---

### Task 1: Branch, ambiente, Supabase local e migração nova

**Files:**
- Create: `supabase/migrations/20261023000100_fotos_tipos_cultos.sql`
- Create: `supabase/seed.sql`
- Create: `supabase/tests/fotos_e_cultos_test.sql`
- Modify: `supabase/config.toml` (só se `[db.seed]` não apontar para `./seed.sql`)

**Interfaces:**
- Produces (banco):
  - `public.eventos.imagem text null`: caminho no bucket `eventos` (ex.: `2026/abc.jpg`), nunca URL.
  - `public.eventos.tipo`: `check (tipo in ('culto','jovens','mulheres','campanha','batismo')) not valid`.
  - `public.fotos.caminho text null`: caminho no bucket `galeria`. `url` vira nullable. Novo check: exatamente
    um de `url` (HTTPS, regra antiga) ou `caminho` preenchido. O check antigo de `url` é removido e recriado.
  - `public.cultos_semanais(id uuid pk, nome text not null 2..120, dia_semana int not null 0..6 (0 = domingo),
    horarios text[] not null default '{}' (cada item `HH:MM`), destaque text not null default '',
    local text not null default '', ordem int not null default 0, criado_em timestamptz default now())`.
    RLS: leitura `anon, authenticated`; escrita `authenticated` com `public.is_admin()`.
  - Buckets `eventos` e `galeria` (`public = true`, limite 5 MB, `image/jpeg` apenas). Políticas em
    `storage.objects`: insert/update/delete só com `bucket_id in ('eventos','galeria') and public.is_admin()`.

- [ ] **Step 1: Criar o branch**

Run (raiz do repo): `git switch -c conversao-kotlin`
Expected: `Switched to a new branch 'conversao-kotlin'`.

- [ ] **Step 2: Conferir o build atual nesta máquina**

Run (em `android-native/`): comando de verificação padrão.
Expected: `BUILD SUCCESSFUL`, 9 testes passando. Se falhar, parar e reportar: é o ponto de partida.

- [ ] **Step 3: Subir o Supabase local**

Iniciar o Docker Desktop (`& "C:\Program Files\Docker\Docker\Docker Desktop.exe"`) e esperar `docker info`
responder. Run (raiz): `npx supabase start`.
Expected: imprime `API URL: http://127.0.0.1:54321` e a `anon key`. As duas migrações existentes aplicam sem erro.

- [ ] **Step 4: Escrever o teste pgTAP que falha**

`supabase/tests/fotos_e_cultos_test.sql`, com `begin; select plan(N); ... select * from finish(); rollback;`.
Cria dois usuários em `auth.users` (um promovido a `admin` em `public.perfis`), e com
`set local role authenticated; set local request.jwt.claims = '{"sub":"<id>"}'` afirma:
- `cultos_semanais_admin_insere`: admin insere `('Culto da Família', 0, '{08:00,15:00,18:00}', ...)` → `lives_ok`.
- `cultos_semanais_membro_nao_insere`: membro insere → `throws_ok` (violação de RLS, `42501`).
- `cultos_semanais_visitante_le`: como `anon`, `select count(*)` ≥ 1.
- `storage_membro_nao_envia`: membro faz `insert into storage.objects(bucket_id, name) values ('eventos','x.jpg')`
  → `throws_ok`.
- `storage_admin_envia`: admin faz o mesmo → `lives_ok`.
- `eventos_tipo_invalido`: admin insere evento com `tipo = 'retiro'` → `throws_ok` (`23514`).
- `fotos_exige_url_ou_caminho`: admin insere foto com `url` e `caminho` nulos → `throws_ok` (`23514`).

- [ ] **Step 5: Rodar e ver falhar**

Run: `npx supabase test db`
Expected: FAIL (`relation "public.cultos_semanais" does not exist`).

- [ ] **Step 6: Escrever a migração `20261023000100_fotos_tipos_cultos.sql`**

Conforme o bloco Interfaces, em `begin; ... commit;`, no estilo da migração de 21/10 (comentários de "por quê"
em português, sem acentos como a anterior). Inclui `grant` correspondentes. Comentário obrigatório na coluna
`imagem`: guarda o caminho, não a URL, porque o check do banco não distingue debug de release.

- [ ] **Step 7: Rodar e ver passar**

Run: `npx supabase db reset` e depois `npx supabase test db`
Expected: todas as migrações aplicam; testes PASS.

- [ ] **Step 8: Escrever `supabase/seed.sql` (só local)**

Cabeçalho: `-- Somente para o Supabase local de desenvolvimento. Nunca aplicar em producao.` Conteúdo: o que o
Expo mostrava fixo, para a referência visual e os testes manuais — eventos "Encontro de Mulheres" (`mulheres`),
"Culto Jovem" (`jovens`), "Batismo Sede" (`batismo`) em datas futuras relativas a `now()`, uma `campanha`;
cultos semanais "Culto da Família" (0, `{08:00,15:00,18:00}`, destaque "Escola Dominical Kids",
local "R. Marli, 191 - São Paulo") e "Culto de Libertação" (3, mesmos horários, "Campanha de Milagres");
linha de `igreja` com endereço "R. Marli, 191 - São Paulo" e história de exemplo marcada como exemplo; dois
estudos; dois aniversariantes com consentimento no mês corrente. Usuários: `admin@local.test` (admin) e
`membro@local.test`, senha `senha-local-123`, criados via `auth.users` + `auth.identities`.

Run: `npx supabase db reset`
Expected: seed aplica; login de `admin@local.test` funciona via
`curl -s -X POST "http://127.0.0.1:54321/auth/v1/token?grant_type=password" -H "apikey: <anon>" -d '{"email":"admin@local.test","password":"senha-local-123"}'`
retornando `access_token`.

- [ ] **Step 9: Commit**

```bash
git add supabase/
git commit -m "feat(supabase): fotos por caminho no Storage, tipos de evento e cultos semanais"
```

---

### Task 2: Referência visual do Expo

**Files:**
- Create: `docs/referencia-visual/expo/*.png` (uma por tela, nomes abaixo)
- Create: `docs/referencia-visual/README.md`
- Temporário, não commitado: `.env` da raiz apontando para o Supabase local.

**Interfaces:**
- Produces: gabarito usado nas Tasks 7–15: `inicio.png`, `biblia.png`, `biblia-capitulo.png`, `eventos.png`,
  `ajustes.png`, `oracao.png`, `campanhas.png`, `batismo.png`, `estudos.png`, `aniversariantes.png`,
  `galeria.png`, `historia.png`, `contato.png`, `login.png`, `cadastro.png`.

- [ ] **Step 1: Apontar o Expo para o Supabase local**

Criar `.env` na raiz (já ignorado pelo Git) com `EXPO_PUBLIC_SUPABASE_URL=http://127.0.0.1:54321` e
`EXPO_PUBLIC_SUPABASE_ANON_KEY=<anon local>`. Conferir `git status`: `.env` não aparece.

- [ ] **Step 2: Rodar o Expo web**

Run: `npm install` e `npx expo start --web --port 8081`
Expected: `http://localhost:8081` abre a Início. Se o build web falhar por dependência nativa, usar o fallback:
Expo Go no emulador `profetas` (`npx expo start --android`) e capturar com `adb exec-out screencap -p`.

- [ ] **Step 3: Capturar cada tela em 390×844**

Com Playwright (`PLAYWRIGHT_BROWSERS_PATH=E:\Android\playwright`, `npx playwright screenshot
--viewport-size=390,844 --full-page <url> <arquivo>`), ou pelo fallback. Rotas: `/`, `/explore`, `/eventos`,
`/settings`, `/oracao`, `/campanhas`, `/batismo`, `/estudos`, `/aniversariantes`, `/galeria`, `/historia`,
`/contato`, `/auth/login`, `/auth/register`. `biblia-capitulo.png`: na Bíblia, abrir Salmos e o capítulo 23.
Expected: 15 PNGs não vazios, cada um mostrando a tela certa (abrir e conferir).

- [ ] **Step 4: README da referência**

`docs/referencia-visual/README.md`: o que são, data, que os dados vêm do `supabase/seed.sql`, e que as
imagens Kotlin equivalentes vão em `docs/referencia-visual/kotlin/` com o mesmo nome.

- [ ] **Step 5: Commit**

```bash
git add docs/referencia-visual/
git commit -m "docs: captura referencia visual das telas do Expo"
```

---

### Task 3: Camada HTTP e sessão em `support/` (sem mudança de comportamento)

**Files:**
- Create: `support/http/SupabaseConfig.kt`, `support/http/SupabaseClient.kt`, `support/http/ApiException.kt`,
  `support/http/SecureUrl.kt`, `support/session/Session.kt`, `support/session/SessionStorage.kt`,
  `support/session/KeystoreSessionStorage.kt`
- Modify: `data/Repository.kt` (passa a delegar `send`/`request`/sessão ao `SupabaseClient`)
- Delete: `data/SessionStore.kt` (vira `KeystoreSessionStorage`)
- Modify: `app/build.gradle.kts` (deps de teste)
- Test: `support/http/SupabaseClientTest.kt`, `support/http/SecureUrlTest.kt`

**Interfaces:**
- Produces:
  - `data class SupabaseConfig(val url: String, val anonKey: String)` com `val isUsable: Boolean`
    (`isSecureUrl(url) && anonKey.isNotBlank()`) e `companion fun fromBuildConfig(): SupabaseConfig`.
  - `fun isSecureUrl(value: String): Boolean` (o atual `validHttps`, renomeado, mesmo corpo).
  - `class ApiException(val status: Int, message: String) : Exception(message)` (mesmo de hoje).
  - `data class Session(val access: String, val refresh: String, val userId: String, val email: String)`.
  - `interface SessionStorage { fun load(): Session?; fun save(session: Session); fun clear() }`
    — `save(null)` vira `clear()` (regra: sem parâmetro que muda comportamento).
  - `class KeystoreSessionStorage(context: Context) : SessionStorage` (código atual do `SessionStore`).
  - `class SupabaseClient(config: SupabaseConfig, storage: SessionStorage, http: OkHttpClient)`:
    `val session: Session?`, `val isConfigured: Boolean`,
    `suspend fun get(path: String): String`, `suspend fun post(path: String, body: JSONObject): String`,
    `suspend fun patch(path: String, body: JSONObject): String`, `suspend fun delete(path: String): String`,
    `suspend fun upload(path: String, bytes: ByteArray, contentType: String): String` (POST binário, header
    `x-upsert: false`), `suspend fun deleteWithBody(path: String, body: JSONObject): String`,
    `suspend fun anonymousPost(path: String, body: JSONObject): String` (auth sem token de usuário),
    `fun adoptSession(json: JSONObject)`, `fun adoptSession(session: Session)`, `fun dropSession()`,
    `suspend fun external(url: String, token: String): String` (API bíblica: sem `apikey`, mensagem
    "Não foi possível consultar a Bíblia (código).").
  - As mensagens de erro por status (401, 403, 409, 429, demais) e a renovação com `Mutex` são copiadas do
    `Repository.send`/`request` atuais, com os comentários de "por quê".

- [ ] **Step 1: Dependências de teste**

Em `build.gradle.kts`: `testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")`,
`testImplementation("org.json:json:20240303")` (o `org.json` do Android é stub na JVM),
`testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")`.

- [ ] **Step 2: Mover o teste de URL**

`EventRulesTest.photosRequireAnActualHttpsHost` vai para `support/http/SecureUrlTest.kt` como
`secureUrlRequiresAnActualHttpsHost`, mesmas cinco asserções, chamando `isSecureUrl`. (Mudança só de
nome/local por exigência de "teste espelha main"; asserções intactas.)

- [ ] **Step 3: Escrever `SupabaseClientTest` (falha: classe não existe)**

Com `MockWebServer` e um `InMemorySessionStorage` de teste:
- `sendsApiKeyAndBearerOfCurrentSession`: sessão `access="A"` → request tem `apikey: anon` e
  `Authorization: Bearer A`.
- `refreshesOnceOn401AndRetries`: respostas 401, depois 200 do `/auth/v1/token?grant_type=refresh_token` com
  `{"access_token":"B","refresh_token":"R2","user":{"id":"u","email":"e"}}`, depois 200 `[]` → resultado `[]`,
  terceira request com `Bearer B`, storage salvo com `refresh = "R2"`.
- `failedRefreshDropsSession`: 401 e depois 400 no refresh → lança `ApiException` com status 400;
  `session == null`; storage limpo.
- `mapsStatusToFriendlyMessage`: 403 → "Você não tem permissão para esta ação."; 409 → "Este cadastro já
  existe."; 429 → "Muitas tentativas. Aguarde e tente novamente."
- `uploadSendsRawBytesWithContentType`: `upload("/storage/v1/object/eventos/a.jpg", bytes, "image/jpeg")` →
  método POST, `Content-Type: image/jpeg`, corpo igual a `bytes`.
- `refusesWhenNotConfigured`: config com URL `http://` → `IllegalStateException` com
  "O serviço da comunidade ainda não está disponível."

- [ ] **Step 4: Rodar e ver falhar**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*SupabaseClientTest*"`
Expected: FAIL de compilação (`Unresolved reference: SupabaseClient`).

- [ ] **Step 5: Implementar as classes de `support/http` e `support/session`**

Mover o corpo atual; `Repository` passa a receber um `SupabaseClient` e chamar seus métodos. Nada de
comportamento novo além do `upload`/`deleteWithBody`.

- [ ] **Step 6: Rodar tudo**

Run: comando de verificação padrão.
Expected: `BUILD SUCCESSFUL`; 9 testes antigos + 6 novos passam.

- [ ] **Step 7: Commit**

```bash
git add android-native/
git commit -m "refactor(android): extrai cliente Supabase e sessao para support"
```

---

### Task 4: Supabase local no build debug e abertura de rota por deep link

**Files:**
- Modify: `support/http/SupabaseConfig.kt`
- Modify: `app/build.gradle.kts` (campo `ALLOW_LOCAL_HTTP` por buildType)
- Create: `app/src/debug/AndroidManifest.xml`, `app/src/debug/res/xml/network_security_config.xml`
- Test: `support/http/SupabaseConfigTest.kt`

**Interfaces:**
- Produces: `SupabaseConfig(url, anonKey, allowLocalHttp: Boolean)`; `isUsable` aceita `http://10.0.2.2:<porta>`
  e `http://127.0.0.1:<porta>` apenas se `allowLocalHttp`. `BuildConfig.ALLOW_LOCAL_HTTP` = `true` em debug,
  `false` em release. Deep link de debug `profetas://open/<rota>` (consumido pela Task 7).

- [ ] **Step 1: Teste que falha** — `SupabaseConfigTest`:
  - `releaseRejectsLocalHttp`: `SupabaseConfig("http://10.0.2.2:54321", "k", allowLocalHttp = false).isUsable`
    é `false`.
  - `debugAcceptsEmulatorHost`: mesmo com `true` → `true`.
  - `debugStillRejectsArbitraryHttp`: `"http://example.org"` com `true` → `false`.
  - `httpsAlwaysAccepted`: `"https://abc.supabase.co"` com `false` → `true`.
- [ ] **Step 2: Rodar e ver falhar** — `--tests "*SupabaseConfigTest*"` → FAIL de compilação.
- [ ] **Step 3: Implementar** — `allowLocalHttp` só libera host `10.0.2.2`/`127.0.0.1` em `http`. Network security
  config de debug: `cleartextTrafficPermitted="true"` apenas para `10.0.2.2`; manifest de debug referencia o
  arquivo e adiciona o `intent-filter` de `profetas://open`. O manifest de main não muda.
- [ ] **Step 4: Rodar e ver passar** — comando de verificação padrão → `BUILD SUCCESSFUL`.
- [ ] **Step 5: `local.properties` local** — `SUPABASE_URL=http://10.0.2.2:54321` e a anon local (arquivo
  ignorado; não commitar). Instalar no emulador (`emulator -avd profetas`, `adb install -r
  app/build/outputs/apk/debug/app-debug.apk`) e conferir que a agenda lista os eventos do seed.
- [ ] **Step 6: Commit** — `feat(android): permite Supabase local apenas no build debug`.

---

### Task 5: Tema, componentes visuais e tipos de evento

**Files:**
- Create: `theme/Palette.kt`, `theme/ProfetasTheme.kt`, `support/ui/*.kt` (lista da estrutura),
  `event/EventType.kt`, `event/DateTicket.kt`, `event/PhotoEventCard.kt`
- Create: `res/drawable/batismo.jpg`, `jovens.jpg`, `culto_mulheres.jpg`, `card1.png` (copiados de
  `assets/images/`)
- Test: `event/EventTypeTest.kt`, `event/DateTicketTest.kt`

**Interfaces:**
- Produces:
  - `object Palette` com `Primary`, `Accent`, `Text`, `Surface`, `Border`, `Muted`, `Background`, `OnPhoto`,
    `DayBadge` (valores das Global Constraints) e as equivalentes escuras (`DarkBackground` etc., escolha do
    construtor, mantendo `Primary`/`Accent` reconhecíveis).
  - `@Composable fun ProfetasTheme(isDark: Boolean, content: @Composable () -> Unit)`.
  - `enum class EventType(val databaseValue: String, val label: String, @DrawableRes val defaultImage: Int)`:
    `CULTO("culto","Culto",R.drawable.card1)`, `JOVENS("jovens","Jovens",R.drawable.jovens)`,
    `MULHERES("mulheres","Mulheres",R.drawable.culto_mulheres)`, `CAMPANHA("campanha","Campanha",R.drawable.card1)`,
    `BATISMO("batismo","Batismo",R.drawable.batismo)`; `companion fun fromDatabase(value: String): EventType`.
  - `data class TicketDate(val day: String, val month: String, val time: String)` e
    `fun ticketDate(instant: Instant, zone: ZoneId): TicketDate` — dia com 2 dígitos, mês `JAN..DEZ`, hora `HH:mm`
    no fuso do aparelho (o Expo usava UTC por engano; corrigido de propósito).
  - Componentes (todos com nomes por extenso): `ScreenColumn`, `SectionCard(title, content)` (título azul 18sp
    ExtraBold + sublinhado 60×3dp), `QuickAction(emoji, label, onClick)`, `SearchBox(value, onChange,
    placeholder)`, `EmptyState(text)`, `LoadFailure(message, onRetry)`, `AppTextField`, `PrimaryButton`,
    `DateTicket(ticket)`, `PhotoEventCard(title, subtitle, ticket, image: Any, onClick)` (220dp de altura, cantos
    16dp, overlay `#000` 30%, ticket no canto superior esquerdo, hora no direito, título 20sp ExtraBold branco).
    Medidas copiadas de `app/(tabs)/index.tsx` `styles`.

- [ ] **Step 1: Testes que falham**
  - `EventTypeTest.knownTypesMapToTheirImages`: `fromDatabase("batismo") == BATISMO`, `.defaultImage ==
    R.drawable.batismo`; idem `jovens`, `mulheres`.
  - `EventTypeTest.unknownTypeFallsBackToCulto`: `fromDatabase("retiro") == CULTO`; `fromDatabase("Culto") ==
    CULTO` (comparação sem diferenciar maiúsculas); `fromDatabase("") == CULTO`.
  - `DateTicketTest.usesDeviceZoneNotUtc`: `ticketDate(Instant.parse("2026-06-25T01:30:00Z"),
    ZoneId.of("America/Sao_Paulo")) == TicketDate("24","JUN","22:30")`.
  - `DateTicketTest.padsDay`: `2026-05-03T12:00:00-03:00` → `day == "03"`, `month == "MAI"`.
- [ ] **Step 2: Rodar e ver falhar** — FAIL de compilação.
- [ ] **Step 3: Implementar** os arquivos acima.
- [ ] **Step 4: Rodar e ver passar** — comando de verificação padrão.
- [ ] **Step 5: Commit** — `feat(android): tema da igreja, componentes visuais e tipos de evento`.

---

### Task 6: Repositórios e modelos por conceito (sem mudança de comportamento)

**Files:**
- Create: os `*Repository.kt` e modelos de `account/`, `event/`, `event/registration/`, `bible/`, `study/`,
  `prayer/`, `birthday/`, `gallery/`, `church/`; `event/EventDates.kt`; `event/reminder/ReminderDelay.kt`;
  `AppContainer.kt`, `ProfetasApplication.kt`, `support/message/UserMessages.kt`
- Modify: `AppViewModel.kt` (passa a usar os repositórios do `AppContainer`; continua existindo até a Task 15)
- Modify: `AndroidManifest.xml` (`android:name=".ProfetasApplication"`)
- Delete: `data/Repository.kt`, `data/Models.kt`, `data/ContentAccess.kt`
- Test: mover `ContentAccessTest` → `account/ContentAccessTest.kt`; `EventRulesTest` → `event/EventDatesTest.kt`
  (3 primeiros testes) e `event/reminder/ReminderDelayTest.kt` (`reminderUsesEventInstantRatherThanWallClock`).
  Asserções intactas; só pacote/nome de arquivo mudam.

**Interfaces:**
- Consumes: `SupabaseClient` (Task 3), `EventType` (Task 5).
- Produces (assinaturas usadas pelas telas):
  - `data class Event(id, title, description, date: String, type: EventType, imagePath: String?)`.
  - `EventRepository(client)`: `suspend fun all(): List<Event>`; `suspend fun save(id: String?, draft:
    EventDraft)`; `suspend fun delete(id: String)`. `data class EventDraft(title, description, date: String,
    type: EventType, imagePath: String?)`. `imagePath` lê/grava a coluna `imagem`.
  - `WeeklyServiceRepository` fica para a Task 8.
  - `RegistrationRepository(client)`: `mine()`, `all()`, `enroll(eventId, name, phone)`, `cancel(id)`.
  - `AccountRepository(client)`: `login`, `register(name,email,password): Boolean`, `requestRecovery(email)`,
    `acceptRecovery(link)`, `updatePassword`, `logout`, `profile(): Profile?`, `saveName(name)`,
    `val currentProfile: StateFlow<Profile?>`, `val isLoggedIn: StateFlow<Boolean>`.
  - `StudyRepository`, `BirthdayRepository`, `GalleryRepository`, `ChurchRepository`, `PrayerRepository`
    (`send(name, text)`, `all()`, `delete(id)`), `BibleRepository(client)` (`books()`, `chapter(book, number)`).
  - Toda escrita de conteúdo passa por `ContentAccess.requireAdmin(accountRepository.profile())` antes do request
    (mesma regra e comentário de hoje).
  - `class UserMessages { val messages: SharedFlow<String>; fun show(text: String) }`.
  - `class AppContainer(context: Context)` com uma propriedade por repositório + `userMessages`.

- [ ] **Step 1: Mover os testes** conforme Files e rodar `testDebugUnitTest` → FAIL de compilação (pacotes novos).
- [ ] **Step 2: Implementar** os repositórios copiando o mapeamento JSON do `Repository` atual (mesmos campos,
  mesmas ordens de consulta); `Event.type` via `EventType.fromDatabase`.
- [ ] **Step 3: Teste novo de mapeamento** — `event/EventRepositoryTest.readsImagePathAndType` (MockWebServer):
  resposta `[{"id":"1","titulo":"T","descricao":null,"data_evento":"2026-06-25T10:00:00-03:00","tipo":"jovens",
  "imagem":"2026/a.jpg"}]` → `Event(type = JOVENS, imagePath = "2026/a.jpg", description = "")`; `imagem` nulo →
  `imagePath == null`.
- [ ] **Step 4: Rodar** — comando de verificação padrão → passa; app no emulador continua funcionando como antes.
- [ ] **Step 5: Commit** — `refactor(android): separa repositorios e modelos por conceito`.

---

### Task 7: Navegação fiel ao Expo e tela Início

**Files:**
- Create: `navigation/ProfetasApp.kt`, `navigation/Route.kt`, `home/HomeScreen.kt`, `event/EventsViewModel.kt`,
  `church/ChurchViewModel.kt`, `church/Church.kt` (se não veio na Task 6)
- Modify: `MainActivity.kt`
- Delete: `ui/ProfetasApp.kt`; de `ui/CommunityScreens.kt` saem `HomeScreen` e `CommunityScreen`
- Test: `navigation/RouteTest.kt`, `event/EventsViewModelTest.kt`

**Interfaces:**
- Consumes: repositórios (Task 6), componentes (Task 5).
- Produces:
  - `sealed interface Route { val path: String }` com objetos/classes para todas as telas: `Home`, `Bible`,
    `Events`, `Settings` (as 4 abas, ícones `Home`, `MenuBook`, `Event`, `Settings`, rótulos "Início", "Bíblia",
    "Eventos", "Ajustes"), `Prayer`, `Campaigns`, `Baptism`, `Birthdays`, `Studies`, `Study(id)`, `Gallery`,
    `Photo(id)`, `History`, `Contact`, `Privacy`, `Login`, `Register`, `Recovery`, `Profile`, `EventDetail(id)`,
    `MyRegistrations`, `Admin`, `Editor(section, id)`.
  - `fun routeFromDebugLink(link: String): Route?` (`profetas://open/events` → `Route.Events`,
    `profetas://open/event/abc` → `Route.EventDetail("abc")`, outro esquema → `null`).
  - `class EventsViewModel(events: EventRepository, registrations: RegistrationRepository, weekly:
    WeeklyServiceRepository?, reminders: ReminderScheduler?, messages: UserMessages)` com
    `val state: StateFlow<EventsState>`; `EventsState(loading, failure: String?, events: List<Event>, ...)`;
    `fun reload()`; `fun upcoming(limit: Int): List<Event>` (futuros, por data). (Os parâmetros anuláveis são
    preenchidos nas Tasks 8–9; até lá passam `null`. Remover a anulabilidade na Task 9.)
  - `ChurchViewModel(church: ChurchRepository)` com `state.church` e `reload()`.
  - Barra superior: título da tela em azul, voltar fora das abas; botão de atualizar chama `reload()` do
    ViewModel da tela visível.
- Tela Início, a partir de `app/(tabs)/index.tsx`: logo no topo (220dp de área), badge "Igreja", título "Igreja
  Profetas de Deus", atalhos `🙏 Oração` → `Prayer`, `🤝 Projetos` → `Campaigns`, `🎂 Parabéns` → `Birthdays`;
  `SectionCard("Quem somos")` com o início de `church.history` (sem história: "A história da comunidade ainda não
  foi publicada.") e "Saiba mais sobre nós" → `History`; "Próximos Eventos" + "Ver todos" → `Events`, carrossel
  horizontal de `PhotoEventCard` com `upcoming(5)` (foto: URL pública do `imagePath` ou `type.defaultImage`);
  `SectionCard("Onde nos encontrar")` com `church.address` e "Como chegar" (abre `geo:`); rodapé "© <ano>
  Profetas de Deus". Saudação com o nome do perfil, se logado (o Kotlin já fazia).

- [ ] **Step 1: Testes que falham**
  - `RouteTest.parsesDebugLinks` (3 casos acima) e `RouteTest.tabsAreTheFourFromExpo`: `Route.tabs ==
    listOf(Home, Bible, Events, Settings)`.
  - `EventsViewModelTest.upcomingSkipsPastAndSortsByDate` (fake `EventRepository` com evento passado, dois
    futuros fora de ordem; `kotlinx-coroutines-test`) → `upcoming(5)` só os futuros, em ordem.
  - `EventsViewModelTest.failureKeepsPreviousEvents`: segunda carga lança → `failure` com a mensagem, `events`
    mantidos.
- [ ] **Step 2: Rodar e ver falhar.**
- [ ] **Step 3: Implementar** navegação e Início. Telas ainda não convertidas continuam chamando os composables
  antigos de `ui/` com o `AppViewModel`.
- [ ] **Step 4: Rodar** — comando de verificação padrão.
- [ ] **Step 5: Conferir a tela** — instalar, `adb shell am start -d "profetas://open/home"`,
  `adb exec-out screencap -p > docs/referencia-visual/kotlin/inicio.png`; comparar com `expo/inicio.png`
  (estrutura, ordem, cores, cantos, tamanhos). Divergência visual que não seja de dado → corrigir antes do commit.
- [ ] **Step 6: Commit** — `feat(android): navegacao de quatro abas e tela Inicio no visual do Expo`.

Todas as tasks de tela seguintes repetem os Steps 4–5 com o nome do PNG indicado.

---

### Task 8: Aba Eventos com cultos semanais

**Files:**
- Create: `event/EventsScreen.kt`, `event/weekly/WeeklyService.kt`, `event/weekly/WeeklyServiceRepository.kt`,
  `event/weekly/WeeklyServicesSection.kt`
- Modify: `event/EventsViewModel.kt`, `AppContainer.kt`
- Delete: `EventsScreen`/`EventCard` de `ui/CommunityScreens.kt`
- Test: `event/weekly/WeeklyServiceTest.kt`, `event/EventsViewModelTest.kt` (casos novos)

**Interfaces:**
- Produces: `data class WeeklyService(id, name, dayOfWeek: DayOfWeek, times: List<String>, highlight, place,
  order: Int)` com `val dayLabel: String` (`Dom`, `Seg`, `Ter`, `Qua`, `Qui`, `Sex`, `Sáb`); mapeamento
  `dia_semana` 0 = domingo. `WeeklyServiceRepository(client)`: `all()` ordenado por `ordem, dia_semana`,
  `save(id, draft)`, `delete(id)`. `EventsViewModel.filtered(query: String, type: EventType?, includePast:
  Boolean)` — o booleano aqui é filtro de dado, não muda comportamento do método.
- Tela a partir de `app/(tabs)/eventos.tsx`: "Fique por dentro" / "Agenda e Eventos"; botões `📸 Fotos` →
  `Gallery` e `💧 Batismo` → `Baptism`; `SearchBox` "Buscar por culto ou retiro..." (no Expo não filtrava;
  aqui filtra — melhoria); "Cultos Semanais" com os cards do Expo (badge do dia `#EBF4FA`, nome, `⭐ destaque` em
  `#00A8E8`, divisor, `⏰` + pílulas de horário, local); depois os eventos com `PhotoEventCard`. Chips de tipo
  (Todos + `EventType.entries`) e "Incluir eventos anteriores" ficam abaixo da busca (o Kotlin já tinha).

- [ ] **Step 1: Testes que falham**
  - `WeeklyServiceTest.zeroIsSunday`: JSON `dia_semana = 0` → `DayOfWeek.SUNDAY`, `dayLabel == "Dom"`;
    `3` → `"Qua"`; `6` → `"Sáb"`.
  - `EventsViewModelTest.filterMatchesTitleOrDescriptionIgnoringCase`, `filterByType`,
    `pastHiddenUnlessIncluded`.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `eventos.png`.
- [ ] **Step 6: Commit** — `feat(android): aba Eventos com cultos semanais no visual do Expo`.

---

### Task 9: Detalhe do evento, inscrição, lembretes, Campanhas e Batismo

**Files:**
- Create: `event/EventDetailScreen.kt`, `event/registration/RegistrationForm.kt`,
  `event/registration/MyRegistrationsScreen.kt`, `event/reminder/EventReminderWorker.kt`,
  `event/reminder/ReminderScheduler.kt`, `campaign/CampaignsScreen.kt`, `baptism/BaptismScreen.kt`
- Delete: `EventReminder.kt`; `EventScreen`/`RegistrationsScreen` de `ui/CommunityScreens.kt`
- Modify: `event/EventsViewModel.kt` (inscrição, cancelamento, lembretes; parâmetros deixam de ser anuláveis)
- Test: `event/EventsViewModelTest.kt` (casos novos), `event/reminder/ReminderSchedulerTest.kt` não (WorkManager);
  a lógica pura já está em `ReminderDelayTest`.

**Interfaces:**
- Produces: `EventsViewModel.enroll(eventId, name, phone)`, `cancelRegistration(id)`,
  `setReminder(event)`, `removeReminder(eventId)`, `removeAllReminders()`, `nextBaptism(): Event?`,
  `campaigns(): List<Event>`. `ReminderScheduler(context, preferences)`: `schedule(event)`, `cancel(eventId)`,
  `enabledIds(): Set<String>`. Worker mantém a revalidação no servidor e o comentário de "por quê". O
  `MainActivity` continua aceitando o extra `event_id`. Nomes de trabalho único (`event:<id>`) e a chave
  `"reminders"` nas preferências não mudam.
- Detalhe: foto do evento no topo (220dp), ticket, título, descrição, compartilhar, lembrete (permissão no
  Android 13+), inscrição com consentimento (textos e regras do Kotlin atual).
- Campanhas, de `app/campanhas.tsx`: "Nossos Projetos" + subtítulo do Expo, lista de eventos `CAMPANHA` futuros com
  data `dd 'de' MMMM`; vazio → texto do Expo.
- Batismo, de `app/batismo.tsx`: imagem `batismo.jpg` no topo, próximo batismo futuro com data e hora; vazio →
  texto do Expo; botão de contato → `Contact`; inscrição "Quero me batizar" → `EventDetail(id)`.

- [ ] **Step 1: Testes que falham**
  - `nextBaptismIsEarliestFutureBaptism`: dois batismos futuros e um passado → o futuro mais próximo.
  - `campaignsOnlyFutureCampaigns`.
  - `enrollShowsSuccessAndReloadsRegistrations`: fake repo → `userMessages` recebe "Inscrição confirmada!" e
    `mine()` chamado de novo.
  - `enrollFailureShowsRepositoryMessage`: repo lança `ApiException(409, "Este cadastro já existe.")` →
    mensagem igual.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `campanhas.png`, `batismo.png` (detalhe e minhas inscrições não existem no Expo:
  conferir só a coerência visual com os demais).
- [ ] **Step 6: Commit** — `feat(android): detalhe de evento, campanhas e batismo no visual do Expo`.

---

### Task 10: Bíblia e Estudos

**Files:**
- Create: `bible/BibleViewModel.kt`, `bible/BibleScreen.kt`, `bible/FavoriteVerses.kt`,
  `study/StudiesViewModel.kt`, `study/StudiesScreen.kt`, `study/StudyScreen.kt`, `study/CompletedStudies.kt`
- Delete: `ui/BibleScreen.kt`; `StudiesScreen`/`StudyScreen` de `ui/CommunityScreens.kt`
- Test: `bible/BibleViewModelTest.kt`, `bible/FavoriteVersesTest.kt`, `study/CompletedStudiesTest.kt`

**Interfaces:**
- Produces: `BibleViewModel(bible: BibleRepository, favorites: FavoriteVerses)`: `loadBooks()`,
  `openChapter(book, number)` (descarta resposta antiga: mantém a lógica de geração do `AppViewModel`),
  `toggleFavorite(verseKey)`. `FavoriteVerses(preferences, accountId: () -> String)` e
  `CompletedStudies(preferences, accountId)`: `all(): Set<String>`, `toggle(id)` — chaves `"favorites:<conta>"` e
  `"studies:<conta>"` como hoje (`visitante` sem login), preservando os dados já salvos.
- Bíblia, de `app/(tabs)/explore.tsx`: "Bíblia Sagrada" / "Selecione um livro para começar a leitura", botão
  `📚 Estudos e Trilhas` → `Studies`, lista de livros que expande para "ESCOLHA O CAPÍTULO" (grade de números,
  ativo em azul), leitura com versículos (badge do versículo em azul). Favoritos e compartilhar continuam
  (melhoria do Kotlin), num chip "Favoritos".
- Estudos, de `app/estudos.tsx`.

- [ ] **Step 1: Testes que falham**
  - `FavoriteVersesTest.separatedByAccount`: favoritar com conta `a`, trocar para `b` → `all()` vazio; voltar
    para `a` → contém.
  - `FavoriteVersesTest.keepsExistingKeyFormat`: preferências com `"favorites:visitante" = {"Salmos 23:1\nO
    Senhor..."}` → `all()` contém (compatibilidade com dados de quem já usa o app).
  - `CompletedStudiesTest.toggleTwiceRemoves`.
  - `BibleViewModelTest.staleChapterIsDiscarded`: abrir capítulo 1 (resposta atrasada) e depois 2 → estado final
    é o capítulo 2.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `biblia.png`, `biblia-capitulo.png`, `estudos.png`.
- [ ] **Step 6: Commit** — `feat(android): Biblia e estudos no visual do Expo`.

---

### Task 11: Oração e Aniversariantes

**Files:**
- Create: `prayer/PrayerViewModel.kt`, `prayer/PrayerScreen.kt`, `birthday/BirthdaysViewModel.kt`,
  `birthday/BirthdaysScreen.kt`
- Delete: `PrayerScreen`/`BirthdaysScreen` de `ui/CommunityScreens.kt`
- Test: `prayer/PrayerViewModelTest.kt`, `birthday/BirthdaysViewModelTest.kt`

**Interfaces:**
- Produces: `PrayerViewModel(prayers, messages)`: `send(name, text)`, `state.sending`, `state.sent`.
  Regras do Kotlin atual: nome 2..120, pedido 5..4000, contador `n/4000`. `BirthdaysViewModel(birthdays,
  account)`: `byMonth(month: Int)` ordenado por dia; visitante → estado "entrar para ver".
- Oração, de `app/oracao.tsx`; Aniversariantes, de `app/aniversariantes.tsx` (seletor de mês mantido).

- [ ] **Step 1: Testes que falham**
  - `PrayerViewModelTest.rejectsShortRequestWithoutCallingRepository`: pedido `"oi"` → repositório não chamado.
  - `PrayerViewModelTest.successMessageIsTheExistingOne`: "Pedido enviado à equipe pastoral."
  - `BirthdaysViewModelTest.byMonthSortedByDay`.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `oracao.png`, `aniversariantes.png`.
- [ ] **Step 6: Commit** — `feat(android): oracao e aniversariantes no visual do Expo`.

---

### Task 12: Galeria (leitura)

**Files:**
- Create: `gallery/GalleryViewModel.kt`, `gallery/GalleryScreen.kt`, `gallery/PhotoScreen.kt`,
  `support/storage/StorageBucket.kt`
- Modify: `gallery/Photo.kt`, `gallery/GalleryRepository.kt` (lê `caminho`)
- Delete: `GalleryScreen`/`PhotoScreen` de `ui/CommunityScreens.kt`
- Test: `support/storage/StorageBucketTest.kt`, `gallery/PhotoTest.kt`

**Interfaces:**
- Produces: `enum class StorageBucket(val id: String) { EVENTS("eventos"), GALLERY("galeria") }` com
  `fun publicUrl(baseUrl: String, path: String): String` → `<base sem barra final>/storage/v1/object/public/<id>/
  <path com segmentos codificados>`. `data class Photo(id, title, album, url: String?, path: String?)` com
  `fun imageUrl(baseUrl: String): String` (path → `GALLERY.publicUrl`, senão `url`). O mesmo `publicUrl` com
  `EVENTS` é o que a Início/Eventos usam para `Event.imagePath` (ajustar lá se ainda não usam).
- Tela de `app/galeria.tsx` (grade de fotos, filtro por álbum mantido).

- [ ] **Step 1: Testes que falham**
  - `StorageBucketTest.buildsPublicUrl`: `GALLERY.publicUrl("http://10.0.2.2:54321/", "2026/a b.jpg") ==
    "http://10.0.2.2:54321/storage/v1/object/public/galeria/2026/a%20b.jpg"`.
  - `PhotoTest.pathWinsOverLegacyUrl` e `legacyUrlStillWorks`.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `galeria.png`.
- [ ] **Step 6: Commit** — `feat(android): galeria no visual do Expo com fotos do Storage`.

---

### Task 13: História, Contato e Privacidade

**Files:**
- Create: `church/HistoryScreen.kt`, `church/ContactScreen.kt`, `church/PrivacyScreen.kt`,
  `support/intent/ExternalIntents.kt`
- Delete: `ContactScreen` de `ui/CommunityScreens.kt`; rotas inline de história/privacidade
- Test: `church/ContactLinksTest.kt`

**Interfaces:**
- Produces: `object ContactLinks { fun whatsapp(number: String): String; fun phone(number: String): String;
  fun email(address: String): String; fun map(address: String): String }` (mesmas URLs do Kotlin atual:
  `https://wa.me/<dígitos>`, `tel:`, `mailto:`, `geo:0,0?q=`). `openLink(context, url)`, `share(context, text)`
  movidos de `ui/Components.kt`.
- Telas de `app/historia.tsx` (logo no topo) e `app/contato.tsx` (cards WhatsApp/Telefone/E-mail e botão de mapa).

- [ ] **Step 1: Teste que falha** — `ContactLinksTest.whatsappKeepsOnlyDigits`: `"+55 (11) 9 8765-4321"` →
  `"https://wa.me/5511987654321"`; `mapEncodesAddress`: `"R. Marli, 191"` → `"geo:0,0?q=R.%20Marli%2C%20191"`.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `historia.png`, `contato.png`.
- [ ] **Step 6: Commit** — `feat(android): historia, contato e privacidade no visual do Expo`.

---

### Task 14: Conta (login, cadastro, recuperação, perfil)

**Files:**
- Create: `account/AccountViewModel.kt`, `account/LoginScreen.kt`, `account/RegisterScreen.kt`,
  `account/RecoveryScreen.kt`, `account/ProfileScreen.kt`, `account/Credentials.kt`
- Delete: `ui/AccountScreens.kt` (`AuthScreen`, `ProfileScreen`)
- Test: `account/CredentialsTest.kt`, `account/AccountViewModelTest.kt`

**Interfaces:**
- Produces: value objects `Email.of(raw)` (trim, valida formato, lança `IllegalArgumentException("Informe um
  e-mail válido.")`) e `NewPassword.of(raw)` (≥ 8, mensagem "Use pelo menos 8 caracteres."), `PersonName.of(raw)`
  (2..120 após trim, "Informe seu nome completo."), em `Credentials.kt` — substituem a cadeia de condições do
  `AuthScreen`. Validação de e-mail sem `android.util.Patterns` (para rodar na JVM). `AccountViewModel(account,
  messages)`: `login`, `register`, `requestRecovery`, `acceptRecovery(link)`, `saveName`, `updatePassword`,
  `logout`; mensagens de sucesso iguais às de hoje.
- Login de `app/auth/login.tsx`, cadastro de `app/auth/register.tsx` (telas separadas, como no Expo);
  recuperação é tela própria a partir de "Esqueci minha senha".

- [ ] **Step 1: Testes que falham**
  - `CredentialsTest.emailIsTrimmedAndValidated`: `" a@b.co "` → `"a@b.co"`; `"a@"` lança com a mensagem.
  - `CredentialsTest.passwordNeedsEight`, `nameNeedsTwoLetters`.
  - `AccountViewModelTest.logoutClearsProfileEvenWhenServerFails`: repo `logout` lança → `currentProfile == null`.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Conferir** `login.png`, `cadastro.png`. Testar no emulador login com `membro@local.test`.
- [ ] **Step 6: Commit** — `feat(android): login, cadastro e perfil no visual do Expo`.

---

### Task 15: Ajustes e remoção do `AppViewModel`

**Files:**
- Create: `settings/SettingsScreen.kt`, `settings/ThemePreference.kt`
- Delete: `AppViewModel.kt`, `SettingsScreen` de `ui/AccountScreens.kt`, `ui/Components.kt`,
  `ui/CommunityScreens.kt` (o que restar)
- Test: `settings/ThemePreferenceTest.kt`

**Interfaces:**
- Produces: `ThemePreference(preferences)`: `val isDark: StateFlow<Boolean>`, `fun useDark()`, `fun useLight()`
  (dois métodos em vez de `setDark(Boolean)`), chave `"dark"` preservada.
- Tela de `app/(tabs)/settings.tsx`: seções "CONTA" (nome/e-mail, Entrar/Sair; modo Usuário/Administrador;
  Meu perfil; Minhas inscrições), "PREFERÊNCIAS" (Tema escuro; Lembretes de eventos com "Desativar todos" — o
  "Habilitar Notificações" do Expo vira esse item), "SOBRE O APP" (texto do Expo com versão `BuildConfig
  .VERSION_NAME`, "Termos de Serviço e Privacidade" → `Privacy`, "Entre em contato conosco" → `Contact`);
  "Administração" só para admin.

- [ ] **Step 1: Teste que falha** — `ThemePreferenceTest.readsExistingKey`: preferências com `dark = true` →
  `isDark.value == true`; `useLight()` grava `false`.
- [ ] **Step 2–4:** falhar, implementar, passar. `grep -r "AppViewModel\|br.org.profetas.app.ui" app/src` vazio.
- [ ] **Step 5: Conferir** `ajustes.png`.
- [ ] **Step 6: Commit** — `feat(android): ajustes no visual do Expo e remove o AppViewModel`.

---

### Task 16: Upload de imagem para o Storage

**Files:**
- Create: `support/storage/ImageResize.kt`, `support/storage/ImageUploader.kt`
- Test: `support/storage/ImageResizeTest.kt`, `support/storage/ImageUploaderTest.kt`

**Interfaces:**
- Consumes: `SupabaseClient.upload/deleteWithBody` (Task 3), `StorageBucket` (Task 12).
- Produces:
  - `data class TargetSize(val width: Int, val height: Int)`; `fun targetSize(width: Int, height: Int, maxSide:
    Int = 1600): TargetSize` (mantém proporção; nunca amplia); `fun sampleSizeFor(width, height, maxSide): Int`
    (maior potência de 2 com lado ainda ≥ `maxSide`).
  - `class ImageUploader(client: SupabaseClient, reader: ImageReader)`:
    `suspend fun upload(bucket: StorageBucket, source: Uri): String` (retorna o caminho `yyyy/<uuid>.jpg`) e
    `suspend fun remove(bucket: StorageBucket, path: String)` (`DELETE /storage/v1/object/<bucket>` com
    `{"prefixes":[path]}`). `interface ImageReader { suspend fun readJpeg(source: Uri, maxSide: Int, quality:
    Int): ByteArray }`; implementação Android com `BitmapFactory` + `inSampleSize` + escala final + correção de
    orientação EXIF (`androidx.exifinterface:exifinterface:1.4.1`) e fundo branco para PNG com transparência,
    tudo em `Dispatchers.Default`. Qualidade 80.
- [ ] **Step 1: Testes que falham**
  - `ImageResizeTest.landscapeLimitsWidth`: `targetSize(4000, 3000) == TargetSize(1600, 1200)`.
  - `portraitLimitsHeight`: `targetSize(3000, 4000) == TargetSize(1200, 1600)`.
  - `smallImageIsNotEnlarged`: `targetSize(800, 600) == TargetSize(800, 600)`.
  - `sampleSizeKeepsAtLeastMaxSide`: `sampleSizeFor(6000, 4000, 1600) == 2`; `(1000, 800, 1600) == 1`.
  - `ImageUploaderTest.uploadsJpegUnderYearFolder` (MockWebServer + `ImageReader` fake): request
    `POST /storage/v1/object/eventos/<ano>/<uuid>.jpg`, `Content-Type: image/jpeg`; retorno começa com `"<ano>/"`.
  - `ImageUploaderTest.removeSendsPrefixes`: corpo `{"prefixes":["2026/a.jpg"]}`.
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Commit** — `feat(android): reduz e envia imagens ao Supabase Storage`.

---

### Task 17: Administração (editores, fotos, cultos semanais)

**Files:**
- Create: `admin/AdminViewModel.kt`, `admin/AdminScreen.kt`, `admin/EventEditor.kt`, `admin/StudyEditor.kt`,
  `admin/PhotoEditor.kt`, `admin/BirthdayEditor.kt`, `admin/ChurchEditor.kt`, `admin/WeeklyServiceEditor.kt`,
  `admin/PrayersInbox.kt`, `admin/RegistrationsInbox.kt`, `admin/PhotoPickerField.kt`, `admin/EventPhotoChange.kt`
- Delete: `ui/AdminScreens.kt`
- Test: `admin/EventPhotoReplacementTest.kt`, `admin/EventDateInputTest.kt`, `event/weekly/WeeklyTimesTest.kt`

**Interfaces:**
- Consumes: repositórios (Tasks 6, 8), `ImageUploader` (Task 16), `ContentAccess`.
- Produces:
  - `AdminViewModel` com no máximo 7 dependências (agrupar repositórios de conteúdo num `ContentRepositories`
    se passar).
  - `class EventPhotoChange(uploader: ImageUploader, events: EventRepository)`:
    `suspend fun save(id: String?, draft: EventDraft, newPhoto: Uri?, removePhoto: Boolean)` — ordem obrigatória:
    1) se há `newPhoto`, envia e obtém o caminho novo; 2) grava o evento com o caminho novo (ou `null` se
    `removePhoto`); 3) só depois remove o arquivo antigo; se o passo 2 falhar, remove o arquivo novo e relança.
    Mesma classe/regra reaproveitada para fotos da galeria (generalizar com o bucket como parâmetro, se ficar
    simples). Comentário de "por quê" sobre a ordem.
  - `fun parseEventDate(input: String, zone: ZoneId): OffsetDateTime` (formato `uuuu-MM-dd HH:mm`, STRICT, mensagem
    "Informe a data e hora no formato aaaa-MM-dd HH:mm.").
  - `fun parseTimes(input: String): List<String>` para cultos semanais: separa por vírgula/espaço, cada item
    `HH:MM` válido, ordena, sem repetidos; inválido → `IllegalArgumentException("Horário inválido: <item>")`.
- Editor de evento: campos atuais + chips de `EventType.entries` + `PhotoPickerField` (Photo Picker do Android
  `ActivityResultContracts.PickVisualMedia(ImageOnly)`, prévia, "Trocar foto", "Remover foto", progresso
  durante o envio). Editor de foto da galeria: título, álbum (sugere álbuns existentes), `PhotoPickerField`;
  some o campo de URL. Editor de culto semanal: nome, dia (chips Dom..Sáb), horários (`08:00, 15:00, 18:00`),
  destaque, local, ordem. Seções do Admin: Agenda, Cultos, Estudos, Fotos, Aniversários, Igreja, Orações,
  Inscrições. Visual: mesmos componentes da Task 5.
- [ ] **Step 1: Testes que falham**
  - `EventPhotoReplacementTest.oldPhotoRemovedOnlyAfterSave`: fakes registram a ordem → `["upload","save",
    "remove:old.jpg"]`.
  - `EventPhotoReplacementTest.failedSaveRemovesNewUploadAndKeepsOld`: `save` lança → ordem `["upload","save",
    "remove:new.jpg"]`, exceção relançada, `old.jpg` não removido.
  - `EventPhotoReplacementTest.removePhotoClearsPathThenDeletes`.
  - `EventDateInputTest.strictRejectsFebruary30` e `keepsDeviceZone`.
  - `WeeklyTimesTest.parsesSortsAndDedupes`: `"18:00, 08:00 15:00,08:00"` → `["08:00","15:00","18:00"]`;
    `"25:00"` lança "Horário inválido: 25:00".
- [ ] **Step 2–4:** falhar, implementar, passar.
- [ ] **Step 5: Teste no emulador** — logado como `admin@local.test`: criar evento com foto da galeria do
  emulador (enviar uma imagem antes com `adb push <jpg> /sdcard/Pictures/` e `adb shell am broadcast -a
  android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/<jpg>`), conferir a foto no card da
  Início e no Storage local (`http://127.0.0.1:54323`, Studio); trocar a foto e conferir que o arquivo antigo
  sumiu; criar um culto semanal e vê-lo na aba Eventos. Como `membro@local.test`, Administração não aparece.
- [ ] **Step 6: Commit** — `feat(android): administracao com upload de fotos e cultos semanais`.

---

### Task 18: Validação funcional completa

**Files:**
- Modify: `docs/VALIDACAO_ANDROID.md` (nova seção datada)
- Create: `docs/referencia-visual/kotlin/*.png` restantes (se faltou algum)

- [ ] **Step 1:** Rodar o comando de verificação padrão e `npx supabase test db`. Expected: tudo passa.
- [ ] **Step 2:** Executar no emulador os 11 itens de "Validação funcional no seu Supabase" do
  `android-native/README.md` contra o Supabase local, mais: tema escuro em todas as abas, foto enorme
  (> 4000 px) enviada sem travar, rede desligada durante upload (mensagem de erro, evento intacto). Item 10
  (negação pela API): `curl` com o token do membro tentando `POST /rest/v1/eventos`,
  `POST /storage/v1/object/eventos/x.jpg` e `POST /rest/v1/cultos_semanais` → 401/403.
- [ ] **Step 3:** Cada falha encontrada vira correção com teste antes do commit (seguir
  superpowers:systematic-debugging). Não ajustar teste para passar.
- [ ] **Step 4:** Registrar em `docs/VALIDACAO_ANDROID.md` o que foi executado, contra o quê (Supabase local) e o
  que continua pendente (Supabase de produção, aparelho físico, release assinado).
- [ ] **Step 5: Commit** — `docs: registra validacao da versao Kotlin no Supabase local`.

---

### Task 19: Remover o Expo e atualizar a documentação

**Files:**
- Delete: `app/`, `components/`, `constants/`, `hooks/`, `lib/`, `services/`, `functions/`, `scripts/`, `assets/`,
  `.expo/`, `.eas/`, `app.json`, `eas.json`, `package.json`, `package-lock.json` (se houver), `tsconfig.json`,
  `.npmignore`, `.env.example`, `node_modules/` (não versionado; apagar do disco)
- Modify: `README.md` (raiz), `.gitignore` (tirar entradas só do Expo/Node), `android-native/README.md`,
  `docs/03_TAREFAS.md`, `docs/MIGRACAO_ANDROID.md`
- Modify: `docs/01_INTEGRACAO_BACKEND.md`, `docs/02_USER_STORIES.md` (aviso de histórico no topo, conteúdo
  preservado)

- [ ] **Step 1:** Conferir que `docs/referencia-visual/expo/` tem as 15 imagens e que `android-native/` não
  referencia nada fora dele (`grep -r "\.\./assets\|\.\./app" android-native` vazio; as imagens já foram copiadas
  na Task 5).
- [ ] **Step 2:** Apagar os arquivos listados (`git rm -r`), e `node_modules`/`.env` do disco (o `.env` do Expo
  local só tinha as chaves do Supabase local).
- [ ] **Step 3:** `README.md` da raiz: o que é o projeto, `android-native/` (como compilar, SDK no E é
  configuração desta máquina, não requisito), `supabase/` (como subir local: `npx supabase start`,
  `db reset`, `test db`; seed é só local), `docs/`. Remover do `android-native/README.md` o que falava do Expo e
  do `.env`; atualizar "O que falta" (fotos por upload agora existem) e a forma de configurar URL/chave.
  `03_TAREFAS.md`: reescrever para o estado real (feito / pendente: Supabase de produção, aparelho físico, release
  assinado). `01`/`02`: aviso "Documento histórico do plano Expo + API própria; a implementação atual usa Supabase
  e Android nativo".
- [ ] **Step 4:** Comando de verificação padrão e `npx supabase test db` → passam sem o Expo.
- [ ] **Step 5: Commit** (só remoção + docs, nenhuma linha de Kotlin) —
  `chore: remove o aplicativo Expo apos a conversao para Kotlin`.
- [ ] **Step 6:** Usar superpowers:finishing-a-development-branch.
