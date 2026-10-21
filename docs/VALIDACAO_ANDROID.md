# Validação da versão Android — 21/10/2026

## Verificações executadas

- Compilação Kotlin/Java e empacotamento do APK debug com Gradle 8.13, AGP 8.11.1, JDK 21 e Android SDK 36.
- Nove testes JUnit: quatro sobre fusos, datas, lembretes e URLs; cinco sobre negação de edição a visitantes/membros/cargos desconhecidos, autorização de administradores e revogação de cargo.
- Android Lint sem erros. Há avisos de dependências com versões mais novas e sugestões de estilo KTX; as versões usadas estão fixadas no build.
- Endpoints públicos da API bíblica atual: 66 livros e Salmos 23 com 6 versículos. O endereço antigo retornou erro; o app usa `https://abibliadigital.api.br/api/`.
- XMLs de recursos e manifesto válidos. Backup em nuvem e transferência de dados do app desativados; tokens usam Android Keystore.
- BuildConfig sem credenciais Supabase. Nenhuma senha administrativa foi colocada no aplicativo ou no repositório.

## Pendências explicitamente adiadas

A configuração do Supabase foi adiada a pedido do responsável. O domínio informado não resolveu no DNS durante a verificação. Nenhuma migração foi aplicada e nenhum registro remoto foi alterado. A migração SQL está preparada para revisão/aplicação posterior.

Os fluxos de autenticação, inscrição, administração, oração e conteúdo remoto estão implementados, mas ainda precisam de validação integrada no Supabase ativo. Não foram executados testes de interface em emulador ou aparelho físico. Lembretes, intents de contato e recuperação de senha devem ser validados no Android após a configuração dos serviços.

As permissões foram reforçadas no código de acesso a dados e nas telas; os testes de cargo são locais. Ainda não foi comprovada a negação de chamadas REST no banco real, nem a revogação de uma sessão administradora conectada ao Supabase. Usuários podem enviar pedidos e gerenciar suas inscrições, mas só administradores podem modificar conteúdo da igreja.

## Artefatos

- Projeto: `android-native/`
- APK de teste: `android-native/app/build/outputs/apk/debug/app-debug.apk`
- Testes: `android-native/app/build/reports/tests/testDebugUnitTest/index.html`
- Lint: `android-native/app/build/reports/lint-results-debug.html`
- Configuração e roteiro funcional: `android-native/README.md`
