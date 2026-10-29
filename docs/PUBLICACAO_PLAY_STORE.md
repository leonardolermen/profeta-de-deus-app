# Roteiro de publicação na Google Play

Responsável pela publicação: preencher nome, contato e data na cópia de trabalho. Este roteiro serve tanto ao proprietário quanto a outra pessoa autorizada a publicar. Fontes consultadas em 29/10/2026: reconferir as regras oficiais antes de cada envio. O roteiro reduz riscos e organiza evidências; não garante aprovação da loja nem ausência de reclamações.

## 1. Conferir o que ainda impede a publicação

| Item | Situação nesta etapa |
|---|---|
| Bíblia Livre TR 2018.2.0 e licença no APK | Implementado; conferir no APK release |
| Login inicial, conta confirmada e oração vinculada ao usuário | Código implementado na 2.4.0; teste no celular pendente |
| Bloqueio no Supabase e fotos privadas | Responsável informou sucesso da migração em 29/10/2026; REST anônimo negado nas nove tabelas. Testes com sessões membro/admin e fotos privadas ainda pendentes |
| Aprovação de novos membros | Não exigida por decisão do responsável: qualquer conta confirmada pode entrar |
| Política de privacidade pública, com responsável e contato reais | Pendente |
| Exclusão de conta e dados pelo aplicativo e recurso externo | Pendente; o app permite cadastro e isto deve ser resolvido antes de publicar |
| AAB release assinado e guarda das chaves | Pendente; APKs distribuídos até aqui são debug de teste |
| Validação da 2.4.0 no celular e trilha de testes da Play | Feedback geral favorável do responsável em 29/10/2026; checklist completo por cenário e trilha da Play ainda pendentes |

Não considerar este aplicativo pronto para publicação enquanto os itens obrigatórios estiverem pendentes. Registrar a evidência e a data de conclusão de cada item; não marcar só porque existe um texto descrevendo a função.

## 2. Congelar a versão e guardar evidências

1. Atualizar o repositório, revisar as alterações, registrar o commit, versionName e versionCode do que será publicado.
2. Executar os testes e lint descritos em `VALIDACAO_ANDROID.md`. Guardar relatórios referentes à versão exata, não resultados históricos de outra versão.
3. Revisar fontes, assets, histórico relevante e pacote final procurando credenciais, dados pessoais e material de teste. `.env`, `local.properties`, senhas, strings de banco, service_role, tokens de sessão, keystores e senhas de assinatura não entram no Git nem em anexos públicos.
4. A chave anon/publishable do Supabase é pública por finalidade; a proteção deve estar na autenticação e nas políticas RLS, não no segredo dessa chave. Nunca substituí-la por service_role.
5. Guardar os comprovantes de autorização em local privado compartilhado apenas com os responsáveis. No repositório, guardar licenças públicas e referências sem dados pessoais de terceiros.

## 3. Texto bíblico e atribuições

1. Usar somente a edição documentada em `BIBLIA_LIVRE.md`: BLIVRE TR, fevereiro de 2018, release oficial 2018.2.0, licença CC BY 3.0 Brasil.
2. Conferir origem e hashes indicados nesse documento. Não trocar por um JSON de outro site mantendo o mesmo crédito por suposição.
3. Abrir Bíblia > Sobre a tradução e licença no aplicativo release. Conferir título, autores Diego Santos, Mario Sérgio e Marco Teles, data da edição, fonte e link da licença. A licença integral deve acompanhar o pacote.
4. Compartilhar um versículo e conferir que a identificação BLIVRE e os créditos acompanham o texto. Verificar que favoritos antigos não são apresentados como texto da nova edição.
5. Se houver alterações futuras no texto, identificá-las e revisar as condições da licença. A conversão de formato não autoriza atribuir uma adaptação aos autores originais nem sugerir endosso deles ao aplicativo.
6. O projeto pede um aviso de reprodução para acompanhamento, esclarecendo que não se trata de pedir autorização. O responsável pode enviar esse aviso, guardar cópia e registrar a data. Nenhuma mensagem foi enviada automaticamente nesta etapa.
7. Não incluir NVI, ACF, Harpa Cristã ou outra edição protegida sem uma autorização/licença que cubra especificamente este produto e sua distribuição. Gratuidade da API e licença MIT de um software não provam licença do texto. Para cada edição, conferir titular, território, canais, uso comercial, offline, compartilhamento, áudio e prazo.

Fontes: [permissão do projeto](https://sites.google.com/site/biblialivre/perguntas-e-respostas/posso-usar-a-b%C3%ADblia-livre), [release oficial](https://github.com/blivre/BibliaLivre/releases/tag/2018.2.0), [CC BY 3.0 BR](https://creativecommons.org/licenses/by/3.0/br/), [regras da Biblica](https://www.biblica.com/permissions/).

### Créditos na ficha da Play Store

Além dos créditos e da licença dentro do aplicativo e nos compartilhamentos, incluir uma atribuição clara na descrição completa da loja é uma medida recomendada para identificar a origem do conteúdo. O texto na loja não substitui os avisos exigidos no APK. Não foi identificada na licença adotada uma exigência específica de um campo separado da Play Store: o dever é dar a atribuição apropriada em cada distribuição, de acordo com a licença.

Texto-base para conferir e copiar na descrição completa, enquanto a edição distribuída continuar sendo a mesma:

```text
Texto bíblico: Bíblia Livre (BLIVRE), edição Textus Receptus, fevereiro de 2018.
© 2018 Diego Santos, Mario Sérgio e Marco Teles.
Distribuído sob Creative Commons Atribuição 3.0 Brasil (CC BY 3.0 BR).
Fonte: https://sites.google.com/site/biblialivre/
Licença: https://creativecommons.org/licenses/by/3.0/br/
Texto dos versículos sem alterações. A licença e os créditos também estão disponíveis no aplicativo.
```

Conferir esse texto contra o AAB final. Não atribuir BLIVRE a uma edição diferente nem usar nomes/logos de autores ou editoras como se endossassem o app. Na descrição curta, manter a proposta real do aplicativo; usar a descrição completa para os créditos e os campos próprios do Play Console para URLs de privacidade, exclusão e suporte.

Os termos do aplicativo devem preservar os direitos que a CC BY concede sobre a Bíblia Livre. Não aplicar ao texto bíblico uma proibição genérica de reprodução de todo o conteúdo, nem medidas que impeçam os usos permitidos pela licença. O login protege o acesso à comunidade e aos dados pessoais; não transfere a autoria da Bíblia Livre para a igreja ou para o desenvolvedor.

Para outros materiais que venham a ser incluídos, registrar onde a respectiva licença exige o aviso: aplicativo, tela de créditos, arquivos LICENSE/NOTICE, descrição da loja ou outros locais. Se uma autorização exigir texto específico, usar exatamente a redação aprovada e guardar o comprovante privado. Não publicar assinaturas, e-mails de autorização, documentos pessoais ou credenciais na ficha.

## 4. Fotos, identidade visual, músicas e bibliotecas

1. Confirmar a autorização da igreja para usar nome, marca e identidade visual; não sugerir vínculo oficial com editoras ou autores da Bíblia.
2. Para fotos e pessoas identificáveis, verificar direitos do fotógrafo e a autorização adequada das pessoas retratadas, especialmente menores. Registrar origem e alcance da autorização. Uma imagem encontrada na internet não é automaticamente livre.
3. Revisar letras, áudios, vídeos, fontes, ícones e imagens de terceiros. Não presumir que um hino inteiro ou uma gravação está livre pela antiguidade da composição.
4. Levantar as dependências diretas e transitivas da versão release com `gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath`. Guardar o relatório fora do Git quando contiver dados locais. Conferir os arquivos LICENSE/NOTICE das bibliotecas efetivamente distribuídas, incluindo AndroidX/Compose, Kotlin/coroutines, OkHttp, Coil e WorkManager; preparar os avisos exigidos antes da publicação. O inventário completo de terceiros ainda precisa ser concluído.
5. Manter um registro: material, autor/titular, fonte, versão, licença ou autorização, restrições, data da conferência e local privado do comprovante. Não colocar autorização com e-mail, assinatura ou documentos pessoais em página pública.

Se o alcance da autorização não estiver claro, obter esclarecimento do titular ou avaliação jurídica antes de distribuir aquele material. Não interpretar silêncio como autorização.

## 5. Privacidade e dados pessoais

1. Identificar o responsável real pelo tratamento dos dados, canal de atendimento e quem opera Supabase e Play Console. Definir finalidade, fundamento adequado, retenção, acesso, exclusão e resposta a incidentes.
2. Considerar o que o app trata: nome e e-mail da conta; nome/telefone nas inscrições; texto e nome nos pedidos de oração; fotos e aniversariantes, quando cadastrados; identificadores de sessão e registros técnicos dos serviços. Conferir o comportamento dos SDKs e serviços na versão final.
3. Pedidos podem conter dados de saúde e convicção religiosa; a LGPD classifica essas categorias como sensíveis. Não publicar pedidos, expor em logs ou fornecer conteúdo pastoral real para avaliadores da loja. A leitura é exclusiva dos administradores.
4. Redigir uma política verdadeira e específica, informando coleta, finalidades, operadores/serviços, compartilhamento, retenção e canais para direitos do titular. Não copiar uma política genérica nem afirmar que não há coleta: o Supabase recebe dados.
5. Publicar a política em URL HTTPS pública, funcional e acessível sem login. Oferecer acesso a ela no aplicativo, inclusive antes do cadastro, e informar a URL na loja. O texto interno atualmente cadastrado na igreja não substitui, sozinho, uma política pública pronta para publicação.
6. Preencher Segurança dos dados com o comportamento real, diferenciando armazenamento local de transmissão ao servidor. Conferir novamente se adicionar analytics, anúncios, pagamentos ou novos SDKs.
7. Implementar e testar a solicitação de exclusão de conta/dados dentro do aplicativo e por recurso externo acessível. Explicar retenções justificadas; não chamar logout de exclusão e não prometer que apagar auth.users remove automaticamente nomes ou textos de oração antigos. Definir a limpeza/anonimização necessária em cada tabela.

Fontes: [dados do usuário na Google Play](https://support.google.com/googleplay/android-developer/answer/10144311?hl=pt-BR), [exclusão de contas](https://support.google.com/googleplay/android-developer/answer/13327111?hl=pt-BR), [glossário da ANPD](https://www.gov.br/anpd/pt-br/documentos-e-publicacoes/glossario-anpd).

## 6. Preparar o Supabase de produção

1. Fazer backup/snapshot conforme disponibilidade do projeto. Conferir o projeto correto e as migrações já aplicadas. Nunca rodar seed de demonstração ou db reset em produção.
2. Aplicar no SQL Editor `supabase/migrations/20261029000100_acesso_autenticado.sql`, depois das migrações anteriores. A migração preserva registros, troca políticas permissivas, exige conta confirmada, vincula novos pedidos à conta e torna os buckets eventos/galeria privados. Não promove usuários a administrador.
3. Confirmar Authentication > configuração de e-mail: confirmação habilitada, SMTP funcional, Site URL HTTPS real e redirects adequados. Eliminar o redirecionamento localhost antes da publicação. O link de recuperação do Android é `profetas://auth/recovery`; não divulgar links contendo tokens.
4. Manter desabilitado o login anônimo no Auth. Cadastro por e-mail continua disponível, conforme escolha do responsável. Qualquer pessoa que confirme uma conta pode entrar; isto não comprova filiação à igreja. Para acesso exclusivamente de membros conhecidos, será necessária aprovação/convite em outra etapa.
5. Promover somente contas conhecidas pelo operador autorizado; não colocar uma escolha de cargo no formulário de cadastro. Membro não altera conteúdo, não lê pedidos recebidos e não altera inscrições de terceiros.
6. Validar com chamada REST anônima e contas separadas: acesso anônimo negado; conta sem confirmação negada; membro lê conteúdo e envia pedidos próprios, mas não edita conteúdo; admin mantém suas funções. Conferir policies, grants e quaisquer políticas antigas adicionais no Storage.
7. Testar fotos no endpoint autenticado. URLs públicas antigas dos buckets devem deixar de entregar o arquivo após a mudança e a propagação dos caches do serviço. URLs externas legadas continuam sob controle do provedor externo e devem ser revisadas antes de expor conteúdo privado.
8. Testar logout, sessão expirada, troca de conta e abertura por lembrete/link sem login. A navegação e os dados em memória da conta anterior não devem reaparecer. Para conteúdo privado, não usar links públicos nem deixar dados reais em builds de teste.

## 7. Preparar e testar a versão release

1. Definir responsável e acesso da conta de desenvolvedor da Play; não compartilhar senha por README/chat. Usar acessos individuais autorizados.
2. Configurar assinatura release e Play App Signing. No Android Studio, usar Build > Generate Signed Bundle / APK > Android App Bundle > módulo app; selecionar a chave de upload existente ou criar uma somente se for a primeira publicação e após decidir a configuração do Play App Signing. Guardar upload key, alias, senhas e recuperação em armazenamento privado com cópia segura. Não gerar ou substituir chaves sem conferir o aplicativo já existente. No fluxo CLI, preparar signingConfig com segredos externos ao Git; o Gradle atual ainda não tem uma assinatura release configurada, portanto rodar bundleRelease sozinho não comprova assinatura válida.
3. Conferir applicationId, versionCode maior que o publicado e versionName. Revisar os requisitos atuais de API alvo na documentação da Google Play; não assumir que a exigência de hoje será a mesma no envio futuro.
4. Gerar `gradlew.bat :app:bundleRelease` com configuração de assinatura adequada e revisar o AAB final em `android-native/app/build/outputs/bundle/release/`. APK debug, mesmo funcionando no celular, não é o pacote final de produção. Confirmar a assinatura do AAB antes de enviar; não publicar o keystore junto dele.
5. Conferir ausência de HTTP em release, segredos e conteúdo de teste; validar licença e assets no pacote que será enviado. Registrar hash do AAB e commit correspondente.
6. Testar no S23 e em outro aparelho quando possível: login/confirmar e-mail/recuperar senha, Mostrar/Ocultar, logout, oração, inscrições, administrador, fotos privadas e Bíblia offline após login. Validar fonte ampliada, leitores de tela e erros sem internet.
7. Usar uma trilha de teste na Play Console e cumprir os testes/verificações específicos exigidos para a conta de desenvolvedor. Só promover a produção após resolver falhas.

Fonte: [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756?hl=pt-BR).

## 8. Preencher a ficha e permitir a avaliação

1. Preparar nome, descrição, ícone, screenshots sem dados pessoais, categoria, classificação indicativa, público-alvo, e-mail de suporte e URL de privacidade. Não anunciar funcionalidades ainda pendentes.
2. Preencher Acesso ao app, Segurança dos dados, exclusão de conta, anúncios e demais declarações aplicáveis. Não declarar ausência de dados ou acesso livre se o app exige login.
3. Fornecer credenciais/instruções de avaliação no campo privado apropriado do Play Console, com acesso funcional e dados de demonstração seguros. Conferir o que o avaliador precisa para todas as funções; usar ambiente/contas apropriados para não expor pedidos pastorais reais. Não colocar essas credenciais em código, screenshots, documentação pública ou descrição da loja.
4. Conferir suporte e exclusão fora do app, com URLs acessíveis sem conta. Testar links em navegador sem sessão.
5. Revisar o checklist com a pessoa responsável pela igreja e registrar a aprovação da versão. Se houver licença/autorização pendente, conteúdo sem origem ou dúvida jurídica relevante, resolver antes do envio.

Fontes: [preparar para revisão](https://support.google.com/googleplay/android-developer/answer/9859455?hl=pt-BR), [dados de login para revisão](https://support.google.com/googleplay/android-developer/answer/15748846?hl=pt-BR).

## 9. Depois de enviar

Guardar o AAB, commit, relatórios, licenças e comprovantes da versão em local privado. Acompanhar mensagens da Play Console, solicitações de privacidade/exclusão, incidentes e alterações nos termos dos serviços. Repetir a revisão a cada atualização de conteúdo, SDK, licença ou forma de monetização. Não publicar credenciais para resolver uma reprovação e não desligar RLS para fazer o app funcionar.
