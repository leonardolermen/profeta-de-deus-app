# Referência visual

Capturas de tela de cada rota do app Expo, usadas como gabarito visual para a reescrita em
Kotlin. Capturadas em 2026-10-23 com `npx expo start --web` + Playwright (Chromium),
viewport 390×844, `fullPage`, apontando para o Supabase local. Os dados mostrados vêm de
`supabase/seed.sql` (dados de exemplo, nunca usados no APK final).

As imagens Kotlin equivalentes, para comparação lado a lado, vão em
`docs/referencia-visual/kotlin/` com o mesmo nome de arquivo.

## expo/

| Arquivo | Rota Expo | Observação |
| --- | --- | --- |
| `inicio.png` | `/` | |
| `biblia.png` | `/explore` | Lista de livros; dados locais (`functions/versiculos/versiculosPorCapitulo`), sem API externa. |
| `biblia-capitulo.png` | `/explore` | Salmos expandido, capítulo 23 selecionado. O texto do versículo é só um placeholder estático no Expo ("mensagem de leitura simulada"), não vem de nenhuma API nem do Supabase — não há seleção de versículo nesta captura porque o número do versículo colide com o número do capítulo na busca por texto do Playwright, e o capítulo já é o que a task pede. |
| `eventos.png` | `/eventos` | |
| `ajustes.png` | `/settings` | Capturada **logada** como `membro@local.test`, mostrando "Logado como Membro Local" / "Sair". |
| `oracao.png` | `/oracao` | |
| `campanhas.png` | `/campanhas` | |
| `batismo.png` | `/batismo` | |
| `estudos.png` | `/estudos` | |
| `aniversariantes.png` | `/aniversariantes` | Tela com lista fixa no código-fonte do Expo (não depende de login nem do Supabase). |
| `galeria.png` | `/galeria` | Uma das 6 fotos (a segunda, "Igreja cheia") aparece como placeholder cinza: a URL do Unsplash usada no Expo (`photo-1544427920-c49ccfaf8c56`) responde 404. Comportamento do código-fonte do Expo, não da captura. |
| `historia.png` | `/historia` | |
| `contato.png` | `/contato` | |
| `login.png` | `/auth/login` | Capturada **antes** do login (formulário vazio). |
| `cadastro.png` | `/auth/register` | Capturada antes do login (formulário vazio). |

## kotlin/

Reservado para as capturas equivalentes do app Android Kotlin, uma por arquivo com o mesmo
nome da tabela acima, conforme as Tasks 7–15 forem implementando cada tela.
