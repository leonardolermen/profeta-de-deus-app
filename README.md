# Profetas de Deus

Aplicativo Android da igreja Profetas de Deus: agenda de eventos e batismos, Bíblia, estudos, galeria de fotos,
aniversariantes, pedidos de oração, inscrições, contatos e uma área de administração do conteúdo. O app é nativo
(Kotlin + Jetpack Compose) e usa o Supabase como backend (Auth, Postgres com RLS e Storage).

O aplicativo Expo/React Native que existia antes foi removido depois da conversão. As capturas de tela dele
sobrevivem em `docs/referencia-visual/expo/` como referência visual.

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `android-native/` | O aplicativo Android. Tem o próprio `README.md` com modos de acesso, configuração e roteiro de testes |
| `supabase/` | Migrações, `seed.sql` e testes pgTAP do banco; `config.toml` do ambiente local |
| `docs/` | Validação, migração, referência visual e documentos históricos do plano antigo |

## Compilar o app Android

Requisitos: Android Studio com AGP 8.11.1, JDK 17 ou 21, SDK Android 36 e Build Tools 35.0.0. O Gradle Wrapper baixa
o Gradle 8.13. O caminho do SDK é configuração de cada máquina (`sdk.dir` em `android-native/local.properties`), não
um requisito do projeto.

```powershell
cd android-native
copy local.properties.example local.properties   # preencha sdk.dir, SUPABASE_URL e SUPABASE_ANON_KEY
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

O APK debug sai em `android-native/app/build/outputs/apk/debug/app-debug.apk`. Nunca coloque a chave `service_role`
no app: só a chave pública (anon).

## Backend local (Supabase)

Precisa de Docker. O Supabase CLI roda por `npx`, sem `package.json` neste repositório.

```powershell
npx supabase start       # sobe o stack local; imprime a URL e a chave anon para o local.properties
npx supabase db reset    # recria o banco: aplica as migrações e o seed
npx supabase test db     # roda os testes pgTAP de supabase/tests/
npx supabase stop
```

No emulador Android, a URL do host é `http://10.0.2.2:54321`.

O `supabase/seed.sql` existe só para o ambiente local: cria as contas `admin@local.test` e `membro@local.test` e dados
de exemplo. Ele não deve ser aplicado em produção.

## Possíveis melhorias futuras

- Separar a lista de livros da Bíblia em Antigo Testamento (39 livros) e Novo Testamento (27 livros), mantendo a pesquisa nos dois grupos.
- Permitir escolher a tradução bíblica, incluindo opções de linguagem mais antiga e mais atual, conforme as versões disponíveis na API e a autorização de uso de cada tradução. A tradução é independente da divisão por testamento: cada versão contém ambos.

Estas sugestões estão registradas para avaliação futura e ainda não foram implementadas.

## Documentação

- `docs/GUIA_TESTE_ANDROID.md`: roteiro da versão 2.2.1 para testar mostrar/ocultar senha, a Bíblia com lista e pesquisa, os pedidos recebidos pelos administradores e os atalhos de criação no celular. O teste manual em emulador no notebook foi descontinuado por consumo de recursos.
- `docs/PESQUISA_HARPA_CRISTA.md`: pesquisa de API e licença; integração adiada até comprovar autorização para redistribuir as letras.

- `android-native/README.md`: situação, pendências, modos de acesso, configuração e testes manuais.
- `docs/VALIDACAO_ANDROID.md`: o que foi executado e o resultado, incluindo a validação de 26/10/2026 no Supabase local.
- `docs/MIGRACAO_ANDROID.md`: organização do código Kotlin e decisões da conversão.
- `docs/03_TAREFAS.md`: o que está feito e o que falta.
- `docs/referencia-visual/`: capturas das telas do app Expo (`expo/`) e do app Kotlin (`kotlin/`).
- `docs/01_INTEGRACAO_BACKEND.md`, `docs/02_USER_STORIES.md`, `docs/supabase_setup.sql`: material histórico do plano
  Expo + API própria. Não descrevem o estado atual.
