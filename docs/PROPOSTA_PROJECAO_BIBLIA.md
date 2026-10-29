# Possível implementação: Bíblia no notebook e projeção

Registrada em 29/10/2026 por solicitação do responsável. **Sugestão futura, ainda não implementada e sem aprovação de execução, prazo ou orçamento.** A equipe e a igreja decidirão se desejam seguir e como viabilizar.

## Objetivo

Permitir que uma pessoa opere a Bíblia no notebook durante cultos ou reuniões e exiba as passagens em uma TV ou projetor, com leitura confortável para a congregação.

## Caminho a avaliar

Criar uma interface web acessível pelo navegador, com login e permissões adequadas. Reaproveitar o texto oficial da Bíblia Livre e, quando necessário, os dados do Supabase, desenvolvendo a interface própria para computador. O aplicativo Android continuaria disponível para os membros; publicar o APK na Play Store não cria automaticamente essa versão para Windows.

O notebook poderia usar a TV/projetor como segunda tela, por HDMI ou outra conexão compatível. Avaliar duas visualizações:

- **Operador no notebook:** pesquisa de livro/capítulo/versículo, passagem atual e próxima, seleção do trecho e controles.
- **Projeção:** somente a passagem selecionada, referência e identificação da tradução, em tela cheia, com letras grandes, alto contraste e opção de ocultar temporariamente a passagem.

Possíveis recursos: avançar/voltar pelo teclado, escolher um ou vários versículos, ajustar tamanho das letras e manter os controles do operador fora da projeção. Pedidos de oração, dados de contas e telas administrativas não devem aparecer na visualização da congregação. Preservar os créditos e as condições da licença da Bíblia Livre, inclusive na apresentação e distribuição pertinentes.

O espelhamento de um celular em tela compatível também pode ser avaliado como alternativa operacional. Compatibilidade e acessórios dependem dos aparelhos reais; não presumir suporte a HDMI ou transmissão sem fio em todo celular ou TV.

## Decisões de viabilidade antes de implementar

1. Confirmar a necessidade com a equipe responsável pelos cultos e definir quem operaria a projeção.
2. Levantar sistema do notebook, memória disponível, resolução, portas/cabos e modelo da TV/projetor. Validar o uso de tela estendida e a distância de leitura.
3. Testar a conexão física e a legibilidade com texto de demonstração antes de desenvolver a integração.
4. Definir se o uso será somente de Bíblia ou também de outros conteúdos, e quais usuários terão permissão para operar.
5. Avaliar necessidade de funcionar sem internet, hospedagem, custos, manutenção, atualização do texto e autenticação. A versão web não deve depender de executar um emulador Android no notebook.
6. Definir a sincronização entre operador/projeção e o comportamento ao perder conexão, fechar a segunda janela ou sair da conta.
7. Revisar licença, atribuição e privacidade, seguindo `BIBLIA_LIVRE.md` e `PUBLICACAO_PLAY_STORE.md` quando aplicáveis.
8. Após aprovação, detalhar o escopo, estimar esforço/custos e realizar um piloto com o equipamento da igreja antes de adotar em cultos.

Esta proposta não altera o aplicativo atual, não cria um site publicado e não autoriza compras, contratação de hospedagem ou desenvolvimento automático. A decisão permanece com a equipe e a igreja.
