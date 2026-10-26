> **Documento histórico** do plano Expo + API própria. A implementação atual usa Supabase e Android nativo (Kotlin); veja o `README.md` da raiz e `docs/MIGRACAO_ANDROID.md`. O conteúdo abaixo foi preservado como registro e não descreve o estado real.

# User Stories (US)

Este documento detalha as Histórias de Usuário (User Stories) necessárias para a integração e construção do Back-end. Elas seguem o padrão: *Como [ator], eu quero [ação] para que [motivo]*.

## Épico 1: Autenticação e Perfis
- **US01:** Como visitante, eu quero poder me cadastrar no aplicativo informando meus dados básicos (nome, e-mail, senha, data de nascimento), para ter um perfil como membro da igreja.
- **US02:** Como membro, eu quero poder fazer login no aplicativo para acessar recursos exclusivos (como meu histórico de dízimos/ofertas ou pedidos de oração).
- **US03:** Como membro, eu quero poder editar meu perfil (foto, endereço) para manter meus dados atualizados com a secretaria.

## Épico 2: Interação e Comunicação
- **US04:** Como usuário do app, eu quero preencher e enviar um formulário com meu pedido de oração, para que a equipe pastoral interceda por mim.
- **US05:** Como usuário do app, eu quero ver quais são os telefones de contato e endereços atualizados da igreja, para conseguir entrar em contato ou visitá-la sem depender de atualizações nas lojas de apps.

## Épico 3: Eventos e Vida na Igreja
- **US06:** Como usuário do app, eu quero visualizar as datas e locais dos próximos Batismos nas águas, para poder me programar e participar.
- **US07:** Como usuário do app, eu quero ver as Campanhas ativas da igreja, para saber o tema, horário e dias da semana dos cultos de campanha.
- **US08:** Como usuário, eu quero ver a lista de aniversariantes do mês, para poder parabenizar os irmãos em Cristo.

## Épico 4: Palavra e Mídia
- **US09:** Como usuário do app, eu quero acessar os Estudos disponibilizados pela igreja, para aprender mais da palavra de Deus durante a semana.
- **US10:** Como usuário do app, eu quero acessar a Galeria de Fotos, para reviver momentos especiais de eventos passados e cultos.

## Épico 5: Administração (Painel CMS do Backend)
*(Nota: O painel é uma ferramenta à parte do aplicativo móvel, usado pela secretaria e pastores)*
- **US11:** Como secretaria/pastor, eu quero acessar um painel web com login para visualizar todos os pedidos de oração recebidos no app e marcá-los como "orado".
- **US12:** Como secretaria/pastor, eu quero poder criar e editar Campanhas, Estudos e Datas de Batismo pelo painel web, para que sejam refletidos instantaneamente no aplicativo.
