# Testar no celular — versão 2.2.1

## Mostrar ou ocultar senha

No login, cadastro e alteração de senha, toque em Mostrar ao lado do campo para conferir o que digitou. O botão passa a Ocultar e esconde novamente o texto ao tocar. Nova senha e confirmação têm controles independentes. Os campos começam ocultos; a opção de exibição não é salva ao sair da tela.

Testar digitação, alternância sem perder caracteres, confirmação de senha e fonte ampliada do Android. APK atual: `android-native/app/build/outputs/apk/debug/Profetas-de-Deus-2.2.1.apk`.

## Novidades da versão 2.2.0

- Os livros da Bíblia aparecem diretamente na tela, em ordem, com nomes grandes. Pesquisar livro filtra essa mesma lista; tocar em um livro abre os campos de capítulo e versículo. Trocar livro volta à lista completa. A leitura por versículo e os controles de fonte da versão 2.1.0 continuam disponíveis.
- Início > Oração: membros e visitantes continuam vendo somente o formulário de pedido. Administradores também veem Pedidos recebidos, com nome, data e texto, além de Atualizar pedidos. Pedir oração permanece disponível para a própria conta administradora.
- Conferir com uma conta administradora e outra membro. Sair ou trocar de conta deve limpar a lista privada. A consulta revalida o cargo e continua sujeita à RLS do Supabase. Não foi criada uma permissão nova no banco.
- APK desta versão: `android-native/app/build/outputs/apk/debug/Profetas-de-Deus-2.2.0.apk`. Instalar como atualização, sem desinstalar o aplicativo.

O roteiro abaixo descreve as funções introduzidas na 2.1.0, preservadas nesta atualização, exceto pela lista de livros que agora fica visível na própria tela.

Os testes manuais serão feitos no Android físico, por escolha do responsável em 29/10/2026. O emulador do computador não faz parte deste fluxo, devido ao consumo de CPU e memória. A compilação foi limitada a dois workers e heap de 1536 MB; processos Gradle não permanecem abertos entre os comandos. Não há alteração de dados do aplicativo por essa configuração.

## Instalar a versão correta

Envie como documento o arquivo `android-native/app/build/outputs/apk/debug/Profetas-de-Deus-2.2.1.apk` e instale como atualização. Confira a versão 2.2.1 nos Ajustes. Não desinstale nem limpe os dados para atualizar: isso pode remover preferências e sessão locais.

## Bíblia

1. Abra Bíblia e escreva o nome em Pesquisar livro. A busca aceita nomes sem acento (Joao encontra João), nomes com números (1 Joao) e abreviações. Toque no resultado para selecionar o livro. Ver todos os livros também oferece busca.
2. Informe Capítulo e, se quiser ir direto a um trecho, Versículo. Exemplo: João, 3, 16. Toque em Ler passagem.
3. A leitura abre separada da seleção. Não há uma grade de dezenas de capítulos antes do texto.
4. Por versículo mostra um versículo de cada vez. Anterior e Próximo ficam disponíveis sem rolar toda a página. Um versículo excepcionalmente longo ainda pode exigir rolagem do texto; os controles ficam fora dessa rolagem.
5. Capítulo inteiro mostra todos os versículos em texto contínuo. Ao pesquisar uma referência, a lista posiciona no versículo pedido.
6. A+ e A− ajustam a fonte entre 18 e 32 sp. Tamanho e modo de leitura ficam salvos no aparelho. A configuração de tamanho de texto do próprio Android continua sendo respeitada.
7. Buscar livro, capítulo e versículo permite trocar a referência diretamente. Capítulo anterior e Próximo capítulo mudam de capítulo sem voltar à busca.
8. Favoritar e Compartilhar continuam disponíveis. Os favoritos anteriores são preservados, separados por conta.

### Conferências importantes

- Pesquisar Salmos 119:105 sem percorrer os 118 capítulos anteriores.
- Digitar capítulo zero, capítulo além do livro e versículo inexistente: deve haver explicação, sem fechamento do app.
- Avançar e voltar versículos, mudar o capítulo e voltar para a busca usando o botão Voltar do Android.
- Aumentar a fonte e fechar/reabrir o app; conferir persistência e botões legíveis no S23.
- Testar os temas claro e escuro e os dois modos de leitura.
- Sem internet, conferir erro e Tentar novamente. Não existe Bíblia offline nesta versão.

## Administração e criação de eventos

1. Entre com a conta já promovida no Supabase. Em Ajustes, confira Modo Administrador.
2. No painel destacado de Ajustes, toque Criar novo evento. O mesmo atalho existe na aba Eventos. Em Administração > Agenda, o botão também passou a se chamar Criar novo evento.
3. Preencha título, descrição e data/hora no formato `aaaa-MM-dd HH:mm` (ex.: `2026-11-15 19:00`), escolha o tipo e, opcionalmente, selecione uma foto.
4. Toque em Salvar. Se houver erro, o formulário deve continuar preenchido e a mensagem deve explicar a falha. A agenda é recarregada após salvar evento ou culto semanal.
5. Para outros conteúdos, use Ver todas as funções: Agenda, Cultos, Estudos, Fotos, Aniversários, Igreja, Orações e Inscrições. Deslize a faixa de seções quando necessário.

O APK conectado grava no Supabase real. Cadastre conteúdo oficial ou identifique explicitamente os registros de teste. O assistente não publicou eventos fictícios no projeto hospedado.

## Permissões

Os atalhos de criação aparecem somente para administrador. Abrir uma rota de editor continua passando pelo AdminGate, os repositórios revalidam o cargo e o Supabase aplica RLS. Não há troca livre de modo nem uma conta administrativa embutida no APK.

Com uma conta membro, testar consulta, pedido de oração e inscrição própria, sem acesso aos botões de criação. A nova interface não amplia as permissões do usuário comum.

## Desenvolvimento e credenciais

Para gerar outro APK, executar em android-native: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`, com JDK 17/21. O SDK é configuração local em local.properties, e a URL/chave pública do Supabase continuam nesse arquivo ignorado pelo Git. Nunca incluir senha do banco ou service_role no aplicativo.

Ainda é necessário validar esta interface no celular; compilação, testes de JVM e lint não substituem essa verificação.
