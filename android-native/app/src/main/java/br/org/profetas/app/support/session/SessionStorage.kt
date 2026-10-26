package br.org.profetas.app.support.session

interface SessionStorage {
    fun load(): Session?
    fun save(session: Session)
    fun clear()
}
