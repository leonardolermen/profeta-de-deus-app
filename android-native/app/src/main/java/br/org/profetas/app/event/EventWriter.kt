package br.org.profetas.app.event

// A parte do EventRepository que a troca de foto do admin usa. Interface para que os testes registrem a ordem
// entre gravar o evento e mexer no Storage com um falso, sem rede.
interface EventWriter {

    suspend fun save(id: String?, draft: EventDraft)

    suspend fun delete(id: String)
}
