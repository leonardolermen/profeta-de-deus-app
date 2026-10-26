# Conexão ao Supabase hospedado — 26/10/2026

As três migrações posteriores ao esquema inicial foram executadas manualmente pelo responsável no SQL Editor, com retorno de sucesso. Não executar novamente o esquema inicial ou o seed local neste banco.

## Verificações realizadas

- Auth e consultas REST responderam HTTP 200 usando a chave pública já fornecida.
- As nove tabelas esperadas estão acessíveis pela API: perfis, eventos, oracoes, estudos, fotos, aniversariantes, igreja, inscricoes e cultos_semanais.
- Consultas sem linhas confirmaram as colunas novas: imagem de evento, caminho de foto, horários semanais, conteúdo de estudos, consentimento de aniversário e vínculo da inscrição ao usuário/evento.
- Os buckets públicos eventos e galeria responderam como existentes (uma consulta de objeto inexistente retornou Object not found).
- Consultas anônimas de perfis, orações, aniversariantes e inscrições retornaram listas vazias. Isso não substitui os testes com contas membro e administradora.
- Agenda, estudos, galeria e cultos semanais estão vazios. Contato, história e privacidade ainda não estão preenchidos na linha de configuração da igreja.
- Cadastro por e-mail está habilitado; a confirmação de e-mail está ativada.

Somente consultas de leitura foram feitas durante essa inspeção. Nenhum usuário, evento ou dado de exemplo foi criado pelo assistente.

## Configuração local

A URL e a chave pública anon foram colocadas em android-native/local.properties, ignorado pelo Git. O banco continua separado do APK: a senha de manutenção, DATABASE_URL e credenciais administrativas não foram copiadas para o build.

O APK precisa ser recompilado e reinstalado quando essas configurações mudam. O APK anterior, gerado sem a conexão, não passa a usar o banco automaticamente.

## Progresso confirmado em 26/10/2026

- APK conectado recompilado com sucesso, 114 testes JUnit sem falhas e lint sem erros (com avisos).
- Cópia para envio ao celular: Profetas-de-Deus-Supabase-26-10-2026.apk, na pasta de saída debug. É uma cópia do APK conectado, com nome diferente para evitar confusão com o arquivo anterior.
- Cadastro da conta responsável apareceu no Supabase e o e-mail foi confirmado, conforme resultado consultado pelo responsável no SQL Editor.
- O responsável informou que conseguiu entrar como administrador no aparelho físico.
- O link de confirmação não abriu a página final, embora a confirmação tenha sido concluída. Ainda falta revisar o redirecionamento.
- O responsável não localizou o cadastro de eventos; a orientação de uso e a validação das operações administrativas no aparelho ficaram para a próxima sessão.

Não foram registradas neste documento senhas, chaves, e-mail pessoal ou identificadores da conta.

## Retomar o teste no aparelho

O caminho implementado para criar eventos é Ajustes > Administração > Agenda > Novo cadastro. As seções da administração aparecem em uma faixa horizontal: Agenda, Cultos, Estudos, Fotos, Aniversários, Igreja, Orações e Inscrições. Algumas podem exigir deslizar a faixa para o lado. Mostrar Modo Administrador não abre automaticamente o painel: é preciso tocar na entrada Administração.

Na próxima sessão, confirmar esse caminho no aparelho junto ao responsável. Se a entrada ou o botão não aparecer, coletar uma captura da tela antes de concluir se é dificuldade de navegação, carregamento ou defeito.

## O que falta

1. Em Authentication > URL Configuration, conferir o Site URL e adicionar profetas://auth/recovery à lista de Redirect URLs.
2. Conferir o SMTP. Sem SMTP próprio, o serviço padrão do Supabase restringe os e-mails aos membros da organização. Referência: https://supabase.com/docs/guides/auth/auth-smtp.
3. Cadastro e confirmação da conta responsável: concluídos conforme resultado apresentado pelo responsável. Validar o fluxo completo para outras contas depois de revisar e-mails e redirects.
4. Acesso administrativo: o responsável informou que já conseguiu entrar como administrador. Novas promoções continuam restritas ao operador do banco; não promover contas desconhecidas nem conceder esse privilégio pelo aplicativo.
5. Localizar o painel no aparelho e cadastrar as informações oficiais da igreja, um evento, um estudo e uma foto.
6. Testar com uma conta membro: login, consulta, oração, inscrição e cancelamento próprios. Conferir que ela não acessa administração nem altera conteúdo ou cargo.
7. Validar recuperação de senha, envio/exclusão de fotos, lembretes e comportamento sem rede no aparelho físico.

As configurações de redirect/SMTP e as operações autenticadas de escrita ainda não foram verificadas nesse projeto hospedado.

## Histórico das migrações

Executar SQL manualmente não registra necessariamente as versões no histórico usado pelo Supabase CLI. Antes de usar db push neste banco, reconciliar o histórico com a estrutura realmente aplicada, para não tentar recriar tabelas já existentes. O registro manual informado foi:

- 20260427200500_initial_schema.sql: estrutura antiga já existente.
- 20261021000100_android_features.sql: aplicada pelo responsável.
- 20261023000100_fotos_tipos_cultos.sql: aplicada pelo responsável.
- 20261025000100_storage_leitura_admin.sql: aplicada pelo responsável.
