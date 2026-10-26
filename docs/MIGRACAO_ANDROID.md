# Migração para Android Kotlin

A aplicação está em `android-native/`. O aplicativo Expo que a precedeu foi removido do repositório; as capturas de tela dele ficam em `docs/referencia-visual/expo/` como referência visual, e `docs/referencia-visual/kotlin/` traz as equivalentes da versão nativa.

## Organização nativa

O código fica em `android-native/app/src/main/java/br/org/profetas/app/`, em pacotes por conceito do domínio. Cada pacote reúne tela, ViewModel e repositório do seu conceito:

- `account/`: login, cadastro, perfil, recuperação de senha e regras de acesso ao conteúdo por cargo.
- `admin/`: painel e editores de conteúdo, caixas de entrada de orações e inscrições, escolha de fotos.
- `baptism/`, `campaign/`, `church/`: batismo, campanhas, informações, história, privacidade e contatos da igreja.
- `bible/`: livros, capítulos, leitura e favoritos (API A Bíblia Digital).
- `birthday/`, `prayer/`, `study/`, `gallery/`: aniversariantes, pedidos de oração, estudos e galeria.
- `event/`: agenda e detalhe, com sub-pacotes `registration/` (inscrições), `reminder/` (lembretes locais com
  WorkManager, revalidando o evento) e `weekly/` (cultos semanais).
- `home/`, `settings/`, `theme/`: início, ajustes e tema.
- `navigation/`: rotas, barra de abas e destinos por área.
- `support/`: o que serve a todos os conceitos: cliente HTTP do Supabase, sessão criptografada pelo Android Keystore,
  envio de imagens ao Storage, intents externas, mensagens de erro e componentes de interface comuns.

`AppContainer.kt` monta as dependências; `ProfetasApplication.kt` e `MainActivity.kt` são os pontos de entrada.

O banco é definido em `supabase/migrations/`: esquema inicial, recursos do Android (inscrições, orações, proteção de
cargos), tipos de foto e cultos semanais, e leitura de Storage para administradores.

## Mapa de telas

O app tem quatro abas: Início, Bíblia, Eventos e Ajustes.

Início → atalhos para Oração, Projetos e Parabéns (aniversariantes); próximos eventos → detalhe de evento; "Quem
somos" → história da igreja.

Bíblia → livros → capítulos → leitura → favoritos/compartilhamento; Estudos → leitura/conclusão.

Eventos → agenda com busca, filtros e cultos semanais → detalhe de evento → lembrete e inscrição → minhas inscrições;
Fotos (galeria) → foto; Batismo.

Ajustes → conta (entrar, cadastro, recuperação de senha), perfil, tema, lembretes, Contato, Privacidade, Minhas
inscrições e Administração (só para administradores).

Administração → agenda, cultos, estudos, fotos, aniversários, informações da igreja, pedidos de oração e inscrições.

## Decisões

Android é a plataforma de destino. Layouts TypeScript/React foram reescritos em Compose. O Supabase pode ser reutilizado após revisão/aplicação das migrações; isso não transfere automaticamente as preferências locais do aplicativo Expo.

O banco guarda apenas dia e mês de aniversário e exige consentimento para divulgação a membros. Inscrições pertencem a uma conta e não podem ser lidas por outros membros. Administradores são promovidos pelo operador do banco, nunca pelo aplicativo.

Fotos (enviadas pelo app ao Storage) e textos precisam ser publicados pela igreja. Não foram inventados contatos nem inseridos cadastros de demonstração no app ou nas migrações; só `supabase/seed.sql` insere dados de exemplo, e apenas no ambiente local. A documentação em `android-native/README.md` lista configuração e testes manuais com o backend real.
