# Quadro de Tarefas

Estado real do projeto em 26/10/2026. O plano antigo (app Expo + API própria) foi substituído pelo app Android
nativo com Supabase; ele permanece descrito, como história, em `01_INTEGRACAO_BACKEND.md` e `02_USER_STORIES.md`.

## Feito

- [x] Aplicativo Android em Kotlin + Jetpack Compose (`android-native/`) com agenda, batismo, campanhas, Bíblia,
  estudos, galeria, aniversariantes, oração, inscrições, contatos, tema claro/escuro e lembretes locais.
- [x] Conta e papéis: login, cadastro, perfil, recuperação de senha, modo usuário e modo administrador.
- [x] Administração do conteúdo pelo app, incluindo upload de fotos de eventos e da galeria para o Storage.
- [x] Backend Supabase em `supabase/`: esquema, RLS, políticas de Storage, seed local e testes pgTAP.
- [x] Validação no Supabase local e em emulador (API 36), com negação de escrita por RLS comprovada. Registro em
  `VALIDACAO_ANDROID.md`.
- [x] Remoção do aplicativo Expo; as capturas dele ficam em `referencia-visual/expo/`.

## Pendente

- [ ] Criar o projeto Supabase de produção e aplicar as migrações de `supabase/migrations/`.
- [ ] Configurar na produção o redirect de Auth `profetas://auth/recovery`, o Site URL e o SMTP.
- [ ] Promover a primeira conta administradora (por SQL, pelo operador do banco) e cadastrar o conteúdo oficial.
- [ ] Repetir o roteiro funcional contra o banco de produção.
- [ ] Testar em celular físico.
- [ ] Gerar uma versão release assinada (mesma chave do app antigo, se ele já foi publicado; versionCode acima do
  publicado).

## Fora do escopo atual

Bíblia offline, sincronização de favoritos e estudos concluídos entre aparelhos, notificações push, exclusão de conta
pelo app, pagamentos/dízimos, transmissão ao vivo, chat e anexos.
