package br.org.profetas.app.bible

object BibleEdition {
    const val NAME = "Bíblia Livre (BLIVRE) · TR · fevereiro de 2018"
    const val SOURCE = "https://github.com/blivre/BibliaLivre/releases/tag/2018.2.0"
    const val LICENSE_URL = "https://creativecommons.org/licenses/by/3.0/br/"
    const val CREDIT = "Bíblia Livre (BLIVRE), © Diego Santos, Mario Sérgio e Marco Teles, fevereiro de 2018. " +
        "Fonte: https://sites.google.com/site/biblialivre/. Licença Creative Commons Atribuição 3.0 Brasil (CC BY 3.0 BR). " +
        "Reprodução permitida com indicação da fonte e dos autores. Texto dos versículos sem alterações, edição Textus Receptus."

    fun shareText(verseKey: String) = "$verseKey\n\n$CREDIT\n$LICENSE_URL"
}
