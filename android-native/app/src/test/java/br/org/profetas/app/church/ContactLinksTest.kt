package br.org.profetas.app.church

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactLinksTest {

    @Test fun whatsappKeepsOnlyDigits() {
        assertEquals("https://wa.me/5511987654321", ContactLinks.whatsapp("+55 (11) 9 8765-4321"))
    }

    @Test fun mapEncodesAddress() {
        assertEquals("geo:0,0?q=R.%20Marli%2C%20191", ContactLinks.map("R. Marli, 191"))
    }

    @Test fun phoneAndEmailAreEncoded() {
        assertEquals("tel:%2B55%2011%203456-7890", ContactLinks.phone("+55 11 3456-7890"))
        assertEquals("mailto:contato%40exemplo.local", ContactLinks.email("contato@exemplo.local"))
    }
}
