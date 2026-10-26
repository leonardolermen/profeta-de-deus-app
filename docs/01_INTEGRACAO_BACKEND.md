> **Documento histórico** do plano Expo + API própria. A implementação atual usa Supabase e Android nativo (Kotlin); veja o `README.md` da raiz e `docs/MIGRACAO_ANDROID.md`. O conteúdo abaixo foi preservado como registro e não descreve o estado real.

# Plano de Integração com o Backend

Este documento descreve a visão geral de como o aplicativo (front-end) se conectará ao futuro Back-end (API) para tornar os dados dinâmicos e persistir as informações geradas pelos usuários.

## Módulos do Aplicativo

### 1. Pedidos de Oração (`oracao.tsx`)
Atualmente a página possui um formulário em tela simulando um envio.
- **Como integrar:** O App enviará um `POST` para a API `/api/oracao` contendo `nome` e `pedido`.
- **Retorno:** Confirmação de recebimento. (Opcional: Listar pedidos antigos caso o usuário esteja logado).

### 2. Eventos e Campanhas (`campanhas.tsx`, `batismo.tsx`)
Essas telas informam os fiéis sobre eventos e datas.
- **Como integrar:** O App fará um `GET` em `/api/eventos` para listar as próximas datas de batismo, campanhas de oração, festividades, etc.
- **Atualização:** As datas (ex: "25 de Junho, 10:00" em `batismo.tsx`) deixarão de ser estáticas e virão do backend.

### 3. Conteúdo Dinâmico (`estudos.tsx`, `historia.tsx`, `aniversariantes.tsx`, `galeria.tsx`)
Todo o conteúdo que precisa ser atualizado frequentemente.
- **Como integrar:** Endpoints de `GET` específicos:
  - `/api/estudos`: Buscará os estudos bíblicos e devocionais.
  - `/api/membros/aniversariantes`: Buscará a lista de membros que fazem aniversário no mês atual.
  - `/api/galeria`: Retornará links (URLs) das imagens hospedadas na nuvem (AWS S3, Cloudinary, etc) para a galeria.

### 4. Perfil e Autenticação (`profile.tsx`, `settings.tsx`)
- **Como integrar:** Autenticação (JWT ou Sessão). Endpoints de `/api/auth/login`, `/api/auth/register`, e `/api/usuarios/me` para pegar preferências do usuário.

### 5. Contato (`contato.tsx`)
A tela de contato redireciona para o WhatsApp ou E-mail.
- **Como integrar:** Podemos manter abrir o WhatsApp, mas buscar os telefones e endereço dinamicamente (GET `/api/configuracoes`), para que se o telefone da secretaria mudar, não seja necessário atualizar o App nas lojas, apenas alterar no painel administrativo.
