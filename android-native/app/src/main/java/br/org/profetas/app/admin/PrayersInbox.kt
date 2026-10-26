package br.org.profetas.app.admin

import br.org.profetas.app.event.displayDate
import br.org.profetas.app.prayer.Prayer

// Pedidos de oração chegam de qualquer pessoa, sem login: o admin só lê e exclui, não edita.
fun prayerItems(prayers: List<Prayer>, query: String, onDelete: (Prayer) -> Unit): List<AdminItem> =
    prayers
        .filter { prayer -> matchesQuery(query, prayer.name, prayer.text) }
        .map { prayer ->
            AdminItem(
                title = prayer.name,
                subtitle = displayDate(prayer.date),
                body = prayer.text,
                delete = DeleteAction("Excluir pedido", DEFAULT_DELETE_WARNING) { onDelete(prayer) },
            )
        }
