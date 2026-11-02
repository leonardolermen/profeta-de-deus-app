# Próxima atualização — propostas de 01/11/2026

Status: planejamento solicitado pelo responsável. Nenhum destes recursos está implementado; a versão atual continua 2.5.0. A escolha visual e as regras financeiras precisam de aprovação antes da implementação. Não há cronograma fechado.

## 1. Ícones consistentes com o rodapé

Trocar emojis dos atalhos Oração, Projetos e Aniversários e da área de boas-vindas por vetores da mesma família visual do rodapé (atualmente Material Icons preenchidos). Manter rótulos visíveis, contraste, tema claro/escuro e área de toque de pelo menos 48 dp. Confirmar qual elemento de boas-vindas será substituído durante a revisão da tela.

Propostas ilustradas em `propostas/icones-proxima-atualizacao.png` (desenhos conceituais próprios, não assets finais):

- A — Preenchidos: azul, sem fundo adicional; maior proximidade com o rodapé atual. Opção recomendada para consistência.
- B — Contorno: traços azuis uniformes; aparência mais leve. Avaliar adequar também o rodapé para manter consistência.
- C — Em círculo: ícone azul sobre círculo discreto; maior destaque dos atalhos, mantendo os rótulos.

Símbolos sugeridos: coração acolhido/mãos para Oração; colaboração para Projetos; bolo para Aniversários; mão de saudação para boas-vindas. A prévia compara direção estética; os vetores finais serão escolhidos em conjunto, verificando disponibilidade, licença e avisos da família utilizada. Não usar emojis como substitutos finais. Não alterar o app antes da escolha do responsável.

## 2. Versículo do dia na tela inicial

### Comportamento solicitado

- Cartão com texto, livro, capítulo, versículo e identificação BLIVRE; abrir a passagem no leitor atual. Preservar créditos e licença da edição existente.
- Todos os membros confirmados consultam; só admin configura/publica.
- Modo manual: selecionar livro, capítulo e versículo válidos da BLIVRE e conferir uma prévia antes de publicar para todos.
- Modo automático: controle exclusivo de admin; a cada dia, às 05:00 de Brasília (`America/Sao_Paulo`), o servidor escolhe e publica uma passagem aleatória única para toda a igreja.
- O admin pode substituir a passagem publicada ou mantê-la. Proposta de regra explícita: publicar manualmente ou tocar Manter este versículo muda para manual e preserva a passagem até nova escolha. Reativar automático agenda a próxima troca para as próximas 05:00, com aviso desse horário. Confirmar essa regra com o responsável antes de implementar.

### Implementação proposta e decisões

- Guardar modo, referência, edição, revisão, data/hora de publicação, autor e histórico no servidor, com RLS para leitura de contas confirmadas e escrita administrativa por função validada.
- Usar Supabase Cron e função de publicação transacional/idempotente: uma publicação automática por data do período iniciado às 05:00 em Brasília. Considerar concorrência com edição manual e reexecuções; comparar revisão antes de gravar e não sobrescrever uma decisão mais recente do admin.
- Configurar/verificar o fuso efetivo do agendador. Uma alternativa é executar uma checagem periódica e calcular no servidor se a janela de 05:00 está vencida usando o fuso IANA, sem depender do relógio do telefone ou de um deslocamento UTC fixo.
- Sugestão editorial: sorteio entre passagens previamente aprovadas pela igreja, evitando textos isolados sem contexto e repetições recentes. O responsável deve decidir o conjunto elegível, necessidade de intervalos de versículos e regra de repetição. Não usar IA para reescrever ou traduzir a Bíblia.
- Armazenar catálogo de referências e texto permitido da edição oficial no servidor, validando referência/conteúdo; manter atribuição BLIVRE no app e nas notificações com texto bíblico. A configuração financeira não compartilha permissões com esse recurso.
- Ao abrir/retomar o app ou atualizar a tela, buscar a revisão oficial. Offline, mostrar a última passagem salva com data e indicação de conteúdo salvo; não sortear um texto diferente em cada telefone.
- Monitorar falhas do agendamento e permitir publicação manual. Se o serviço estiver indisponível, manter a última publicação identificada e executar recuperação idempotente; não prometer disponibilidade exata às 05:00 sem monitoramento e infraestrutura adequados.

### Notificações

- Integrar Firebase Cloud Messaging (FCM), envio pelo servidor após confirmar a publicação, com credenciais privadas apenas no servidor. Não incluir chaves de envio no APK/Git.
- Canal Versículo do dia, preferência individual e permissão do Android quando necessária. Tocar na notificação abre a passagem depois de validar login. Opção de prévia discreta na tela bloqueada.
- Publicação no servidor prevista para 05:00; entrega da notificação depende de internet, permissão, bateria e regras do Android, sem garantia de chegada pontual. Se o usuário negar a permissão, o cartão continua disponível no app.
- Desduplicar por identificador/revisão, limitar avisos de correções manuais, expirar alertas antigos e remover vínculos de dispositivos no logout/troca de conta. Cadastro e gerenciamento de tokens são dados a incluir na privacidade e Segurança dos dados da Play.
- Conteúdo autenticado: não usar tópico público como barreira de autorização. Preferir envio do servidor a dispositivos de sessões elegíveis; tokens de notificações não dão acesso ao conteúdo do banco. Restringir especialmente os alertas financeiros aos responsáveis autorizados.

## 3. Aba Ofertas / Dízimos e ofertas

### Experiência solicitada

- Acrescentar uma aba ao lado de Bíblia e Eventos. Sugestão de rótulo curto no rodapé: Ofertas; título da tela: Dízimos e ofertas. Proposta de ordem: Início, Bíblia, Ofertas, Eventos, Ajustes; confirmar escolha visual e legibilidade em fonte ampliada/aparelho estreito.
- Exibir chave Pix oficial ativa, tipo da chave, nome esperado do beneficiário e instrução para conferir o destinatário no banco. Botão Copiar chave e instruções simples para colar no aplicativo bancário e escolher livremente o valor.
- Não preencher uma chave provisória, não colocar dados bancários reais nesta documentação e não publicar o recurso sem validação do responsável financeiro.
- Permitir selecionar Dízimo, Oferta ou um projeto publicado pela igreja, usando a mesma chave. Na primeira versão, seleção apenas informativa: copiar a mesma chave não transmite a finalidade ao banco e não permite identificar automaticamente o projeto nem confirmar recebimento. Informar isso claramente; não apresentar status Pago/Recebido ou comprovante oficial sem integração real.
- Conciliação, QR Code com referência, comprovantes e integração com provedor financeiro ficam fora deste primeiro escopo e dependem de avaliação específica.

### Proteção proposta para alterações da chave

- Consultar a tela como membro não concede edição. Proposta: separar a capacidade de gerir Pix do cargo admin comum, concedida apenas aos responsáveis financeiros designados, com MFA/reauthentication recente verificada no servidor.
- Alteração deve criar uma solicitação pendente com chave anterior/proposta, beneficiário declarado, autor, motivo, data/hora e versão esperada. Exibir prévia e pedir confirmação reforçada. Verificar a titularidade por processo da igreja; texto digitado no app não comprova o titular real.
- Recomenda-se aprovação por outro responsável financeiro distinto do solicitante antes da ativação, inclusive no cadastro inicial. Com apenas um responsável habilitado, manter o recurso pendente até definir o segundo; não criar um bypass automático. Essa regra será discutida com a equipe antes da implementação.
- Manter a chave anterior ativa até aprovação da nova, ou tela indisponível se ainda não existir uma chave aprovada. Ativação atômica, validação de formato por tipo e proteção contra duas alterações concorrentes.
- Toda mutação passa por função de servidor; bloquear atualização direta da chave por membros e administradores comuns via RLS/privilégios. O mesmo responsável não pode solicitar e aprovar. Proteger também a concessão da permissão financeira; não oferecer autopromoção dentro do app.
- Histórico de auditoria de cadastro, solicitação, aprovação, rejeição e ativação, com autor, aprovador, horário e versões. Não permitir que clientes apaguem ou reescrevam esse histórico. Uma conta privilegiada que controla a infraestrutura ainda exige governança fora do app; não prometer proteção absoluta.
- Caixa de alertas persistente para administradores sobre cadastro/troca e alertas push opcionais; o evento de auditoria e a fila de alerta devem ser gravados na mesma transação da mudança. Push falho não pode apagar o aviso nem fazer parecer que todos foram notificados. Nas notificações, usar resumo e chave mascarada; detalhes sensíveis apenas na tela autorizada.
- Quando a chave mudar, invalidar o cache e exigir atualização da configuração antes de copiar. Se a chave atual não puder ser confirmada online, bloquear a cópia e explicar que é necessário conectar; não oferecer uma chave antiga como atual.
- Propostas operacionais: contatos oficiais de conferência, procedimento para suspender a exibição de Pix em incidente e recuperação de MFA/acesso sob responsabilidade da igreja. Confirmar regras antes de desenvolver.

### Publicação e limites

- O app somente informa/copia a chave; não movimenta dinheiro nem confirma pagamentos. Não condicionar a leitura bíblica ou outros recursos digitais a doações.
- Conferir o enquadramento da igreja e da arrecadação nas regras de pagamentos/doações da Google Play antes da publicação; a política distingue doações isentas de tributos. Não presumir que toda arrecadação religiosa tenha automaticamente esse enquadramento. Confirmar documentação aplicável com os responsáveis e, quando necessário, obter orientação especializada.
- Rever a política de privacidade, dados de dispositivos/notificações, registros administrativos e conteúdo financeiro apresentado, além dos bloqueios de publicação já existentes em `PUBLICACAO_PLAY_STORE.md`.

## Ordem proposta para o próximo trabalho

1. Escolher a direção dos ícones e o rótulo/posição da nova aba. Confirmar regras de manter versículo, conjunto aleatório, responsáveis financeiros e aprovação dupla.
2. Implementar os ícones e o cartão do versículo com publicação manual, validações e RLS.
3. Implementar agendamento automático, histórico, concorrência e recuperação de falhas.
4. Integrar notificações, permissões e preferências, atualizando a documentação de privacidade.
5. Implementar a tela de ofertas, gestão financeira restrita, aprovação, auditoria e alertas; cadastrar a chave real apenas após validação.
6. Testar no celular com membro, admin e responsáveis financeiros distintos; testar também tentativas diretas na API, permissões revogadas, duplo envio, offline, notificação negada, troca de conta e fonte ampliada. Atualizar documentação, revisar segredos e preparar novo APK.

Critérios para concluir: todos recebem a mesma revisão do versículo; mudanças manuais prevalecem conforme a regra aprovada; alertas são deduplicados; membros não publicam conteúdo nem editam Pix; nenhuma chave pendente é exibida para copiar; não existe autoaprovação financeira nem indicação falsa de pagamento confirmado. Não iniciar emulador no notebook.

Não estimar uma versão final apenas por estas funcionalidades: privacidade pública, exclusão de conta, testes integrados, licenças e assinatura/publicação continuam pendentes no quadro geral.

## Fontes consultadas em 01/11/2026

- Supabase Cron: https://supabase.com/docs/guides/cron
- Firebase Cloud Messaging Android e permissão de notificações: https://firebase.google.com/docs/cloud-messaging/android/get-started
- Recebimento de mensagens Android: https://firebase.google.com/docs/cloud-messaging/android/receive-messages
- Prioridade/entrega de mensagens: https://firebase.google.com/docs/cloud-messaging/customize-messages/setting-message-priority
- Política de pagamentos Google Play: https://support.google.com/googleplay/android-developer/answer/9858738?hl=pt-BR

As fontes descrevem capacidades e políticas; a arquitetura acima é uma proposta para este projeto, não uma implementação já validada.
