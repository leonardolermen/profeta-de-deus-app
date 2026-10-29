# Bíblia Livre no aplicativo — 29/10/2026

## Decisão e origem do texto

A versão 2.3.0 substitui a leitura NVI pela Bíblia Livre (BLIVRE), edição Textus Receptus de fevereiro de 2018, release oficial 2018.2.0. O texto completo foi obtido do projeto dos autores, não de um banco de terceiros sem licença identificada:

- Release: https://github.com/blivre/BibliaLivre/releases/tag/2018.2.0
- Arquivo: https://github.com/blivre/BibliaLivre/releases/download/2018.2.0/bliv-tr_vpl.zip
- Licença dessa edição: https://github.com/blivre/BibliaLivre/blob/2018.2.0/LICENCA.md
- Permissão de uso em programas: https://sites.google.com/site/biblialivre/perguntas-e-respostas/posso-usar-a-b%C3%ADblia-livre

O arquivo VPL original está no APK, sem alteração de seus bytes. O leitor separa referências e versículos; não reescreve palavras, pontuação, colchetes ou notas que já estejam no texto. O índice contém nomes, abreviações e contagens de capítulos derivadas do texto oficial. A edição contém 66 livros, 1.189 capítulos e 31.102 entradas de versículos. A leitura funciona sem internet e sem token de API bíblica.

SHA-256 do ZIP original: `AF8D9876919C4CA67195B8D8ADCB9998A63F25EA4509750574AB6A7EEDB9E2F1`.

SHA-256 do VPL distribuído: `43E41DC7A979C2F668FCE941D10C2ECD564912A3A02554706F78D66602248D52`.

## Permissão e créditos

A edição adotada é licenciada sob Creative Commons Atribuição 3.0 Brasil, CC BY 3.0 BR: https://creativecommons.org/licenses/by/3.0/br/. A licença permite reprodução e redistribuição, inclusive comercial, cumpridas suas condições. Não há cobrança de royalties ou necessidade de solicitar autorização individual para o uso coberto pela licença. Isso inclui distribuir o texto no aplicativo público, mantendo a atribuição, a licença e as demais condições; não equivale a aprovação ou endosso dos autores ao aplicativo.

O projeto solicita ser avisado de reproduções para acompanhamento, esclarecendo que não é um pedido de autorização. O assistente não enviou mensagens aos autores. A eventual notificação pode ser feita pelo responsável.

Crédito usado: Bíblia Livre (BLIVRE), © Diego Santos, Mario Sérgio e Marco Teles, fevereiro de 2018. Fonte: https://sites.google.com/site/biblialivre/. Creative Commons Atribuição 3.0 Brasil. Texto dos versículos sem alterações, edição Textus Receptus.

A licença integral acompanha o APK em `assets/bible/LICENCA_BLIVRE.txt`. A tela Bíblia e o leitor oferecem Sobre a tradução e licença; o compartilhamento de versículos inclui os créditos e o endereço da licença. A licença é do texto, não exige que todo o código do aplicativo use CC BY. Não usar logos dos autores nem sugerir apoio ao aplicativo sem autorização.

Há cópias posteriores no eBible com declaração CC BY 4.0; esta implementação usa especificamente o download oficial 2018.2.0 e sua licença 3.0 BR. Não misturar as declarações de edições diferentes.

## Antigo/Novo Testamento e variantes

Antigo e Novo Testamento estão presentes. A separação visual em abas continua uma possível melhoria.

O projeto fornece TR (Textus Receptus) e N4 (Nestle 1904), que diferem na base textual do Novo Testamento. O Antigo Testamento é comum a ambas. Não são duas opções chamadas linguagem antiga e moderna. O aplicativo adota somente TR nesta atualização; não foi criado um seletor de versões.

Exemplo da edição adotada, Mateus 11:28: “Vinde a mim todos vós que estais cansados e sobrecarregados, e eu vos farei descansar.” Comparar com a NVI em https://www.biblegateway.com/passage/?search=Mateus%2011%3A28&version=NVI-PT. A Bíblia Livre conserva expressões como vós e vinde; a liderança deve avaliar se o público se identifica com essa linguagem. Os autores descrevem a edição como trabalho em andamento, portanto uso livre não significa revisão final ou equivalência textual à NVI.

## NVI utilizada anteriormente

A NVI é protegida. A política da Biblica exige autorização/licenciamento adequado para exibir o texto completo em um aplicativo: https://www.biblica.com/permissions/. Existem regras para citações limitadas e licenciamento expresso por parceiros oficiais, mas isso não comprova autorização para a API comunitária usada anteriormente. Não foi localizada licença que cobrisse a distribuição pública deste aplicativo com a NVI. A troca evita depender dessa autorização para o texto novo; não constitui parecer sobre usos anteriores.

As referências de favoritos antigos foram preservadas localmente. A tela não reapresenta nem compartilha seu texto antigo como BLIVRE; oferece Ler na Bíblia Livre, para consultar e favoritar o texto da nova edição. Os novos favoritos e compartilhamentos identificam BLIVRE.

## Validação no celular

Na versão atual, instalar `Profetas-de-Deus-2.4.0-Login.apk` como atualização e entrar com conta confirmada. Conferir Gênesis 1, Salmos 23, João 3:16 e Apocalipse 22; repetir em modo avião após login; abrir créditos e licença; compartilhar um versículo; testar favorito antigo e novo. O APK é debug de teste, não o pacote final assinado para publicação na Play Store.

O responsável deu feedback favorável sobre as modificações em 29/10/2026; manter o checklist detalhado em `GUIA_TESTE_ANDROID.md`. Para créditos também na ficha da Play Store, usar o texto-base e as instruções de `PUBLICACAO_PLAY_STORE.md`, sem substituir a atribuição dentro do aplicativo.
