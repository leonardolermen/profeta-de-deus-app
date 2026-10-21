package br.org.profetas.app.data

/** Cargo obtido de public.perfis; nunca de uma escolha de modo ou dos metadados do cadastro. */
object ContentAccess {
    fun canManage(profile: Profile?): Boolean = profile?.role == "admin"

    fun requireAdmin(profile: Profile?) {
        if (!canManage(profile)) throw SecurityException("Somente administradores podem modificar o conteúdo da igreja.")
    }
}
