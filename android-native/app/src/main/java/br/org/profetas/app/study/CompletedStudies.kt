package br.org.profetas.app.study

import android.content.SharedPreferences
import br.org.profetas.app.support.storage.AccountScopedSet

// Estudos que o membro marcou como concluídos, guardados no aparelho por conta (o banco não registra isso).
class CompletedStudies(preferences: SharedPreferences, accountId: () -> String) {

    private val stored = AccountScopedSet(preferences, "studies", accountId)

    fun all(): Set<String> = stored.all()

    fun toggle(id: String) {
        stored.toggle(id)
    }
}
