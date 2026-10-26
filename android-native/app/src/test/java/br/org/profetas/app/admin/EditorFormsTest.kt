package br.org.profetas.app.admin

import br.org.profetas.app.event.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId

class EditorFormsTest {

    @Test fun eventDraftKeepsPhotoPathAndSavesOffset() {
        val form = EventForm(" Vigília ", "", "2026-11-07 22:00", EventType.CULTO, imagePath = "2026/a.jpg")

        val draft = form.toDraft(ZoneId.of("America/Sao_Paulo"))

        assertEquals("Vigília", draft.title)
        assertEquals("2026-11-07T22:00-03:00", draft.date)
        assertEquals("2026/a.jpg", draft.imagePath)
    }

    @Test fun weeklyServiceNeedsTimesAndNumericOrder() {
        val form = WeeklyServiceForm("Culto da Família", DayOfWeek.SUNDAY, "18:00, 09:00", "", "", "")

        val draft = form.toDraft()

        assertEquals(listOf("09:00", "18:00"), draft.times)
        assertEquals(0, draft.order)
        assertThrows(IllegalArgumentException::class.java) { form.copy(times = " ").toDraft() }
        assertThrows(IllegalArgumentException::class.java) { form.copy(order = "primeiro").toDraft() }
    }

    @Test fun birthdayRejectsImpossibleDate() {
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            BirthdayForm("Maria", day = "30", month = "2", consent = true).toDraft()
        }

        assertEquals("Informe um dia e um mês válidos.", thrown.message)
        assertEquals(29, BirthdayForm("Maria", day = "29", month = "2", consent = true).toDraft().day)
    }

    @Test fun churchAcceptsBlankContactsButNotShortWhatsapp() {
        val blank = ChurchForm("Igreja Profetas de Deus", "", "", "", "", "", "")

        assertEquals("", blank.toChurch().email)

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            blank.copy(whatsapp = "51 9999").toChurch()
        }
        assertEquals("Informe o WhatsApp com país e DDD.", thrown.message)
    }
}
