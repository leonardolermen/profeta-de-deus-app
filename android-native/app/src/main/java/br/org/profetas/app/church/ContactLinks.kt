package br.org.profetas.app.church

import br.org.profetas.app.support.http.encodeUrlComponent

// Os contatos da igreja viram links que o Android resolve para o aplicativo certo (discador, e-mail, mapas).
object ContactLinks {

    // O wa.me só aceita o número com código do país e sem máscara.
    fun whatsapp(number: String): String = "https://wa.me/" + number.filter { character -> character.isDigit() }

    fun phone(number: String): String = "tel:" + encodeUrlComponent(number)

    fun email(address: String): String = "mailto:" + encodeUrlComponent(address)

    fun map(address: String): String = "geo:0,0?q=" + encodeUrlComponent(address)
}
