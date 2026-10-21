# Migração para Android Kotlin

A nova aplicação está em `android-native/`. O código Expo foi preservado na raiz como referência de conteúdo e comportamento; não é dependência da versão Android.

## Organização nativa

- `data/Models.kt`: modelos e regras de datas/URLs.
- `data/Repository.kt`: acesso REST ao Supabase Auth/PostgREST e API bíblica, renovação de sessão e mensagens de falha.
- `data/SessionStore.kt`: tokens criptografados pelo Android Keystore.
- `AppViewModel.kt`: estado das telas, ações e preferências locais.
- `ui/`: navegação Compose, componentes, comunidade, Bíblia, conta e administração.
- `EventReminder.kt`: lembretes locais com revalidação do evento.
- `supabase/migrations/20261021000100_android_features.sql`: novas tabelas, RLS, inscrições e proteção de cargos.

## Mapa de telas

Início → agenda → detalhe de evento → inscrição → minhas inscrições.

Bíblia → livros → capítulos → leitura → favoritos/compartilhamento.

Comunidade → oração, estudos → leitura/conclusão, aniversariantes, álbuns → foto, história e contatos.

Ajustes → conta/cadastro/recuperação, perfil, tema, lembretes, privacidade e administração.

Administração → agenda, estudos, fotos, aniversários, informações da igreja, pedidos de oração e inscrições.

## Decisões

Android é a plataforma de destino. Layouts TypeScript/React foram reescritos em Compose. O Supabase existente pode ser reutilizado após revisão/aplicação das migrações; isso não transfere automaticamente as preferências locais do aplicativo Expo.

O banco guarda apenas dia e mês de aniversário e exige consentimento para divulgação a membros. Inscrições pertencem a uma conta e não podem ser lidas por outros membros. Administradores são promovidos pelo operador do banco, nunca pelo aplicativo.

Fotos e textos precisam ser publicados pela igreja. Não foram inventados contatos nem inseridos cadastros de demonstração. A documentação em `android-native/README.md` lista configuração e testes manuais com o backend real.
