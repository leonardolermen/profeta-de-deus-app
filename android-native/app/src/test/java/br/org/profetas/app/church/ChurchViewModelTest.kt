package br.org.profetas.app.church

import org.junit.Assert.assertEquals
import org.junit.Test

class ChurchViewModelTest {

    // Durante a primeira carga, dizer que a história "ainda não foi publicada" seria um vazio falso.
    @Test fun historyPreviewStaysBlankWhileLoading() {
        assertEquals("", ChurchState(loading = true).historyPreview)
    }

    @Test fun historyPreviewExplainsMissingHistoryAfterLoading() {
        val state = ChurchState(loading = false)

        assertEquals("A história da comunidade ainda não foi publicada.", state.historyPreview)
    }

    @Test fun historyPreviewShowsPublishedHistoryEvenDuringReload() {
        val state = ChurchState(loading = true, church = Church(history = "Fundada em 1990."))

        assertEquals("Fundada em 1990.", state.historyPreview)
    }
}
