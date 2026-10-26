package br.org.profetas.app.event.weekly

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class WeeklyServiceTest {

    private fun row(databaseDay: Int): JSONObject = JSONObject(
        """{"id":"1","nome":"Culto da Família","dia_semana":$databaseDay,"horarios":["08:00","18:00"],""" +
            """"destaque":"Escola Dominical Kids","local":"R. Marli, 191 - São Paulo","ordem":0}""",
    )

    @Test fun zeroIsSunday() {
        val sunday = WeeklyService.fromRow(row(0))
        val wednesday = WeeklyService.fromRow(row(3))
        val saturday = WeeklyService.fromRow(row(6))

        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)
        assertEquals("Dom", sunday.dayLabel)
        assertEquals(DayOfWeek.WEDNESDAY, wednesday.dayOfWeek)
        assertEquals("Qua", wednesday.dayLabel)
        assertEquals(DayOfWeek.SATURDAY, saturday.dayOfWeek)
        assertEquals("Sáb", saturday.dayLabel)
    }

    @Test fun readsAllColumns() {
        val expected = WeeklyService(
            id = "1",
            name = "Culto da Família",
            dayOfWeek = DayOfWeek.SUNDAY,
            times = listOf("08:00", "18:00"),
            highlight = "Escola Dominical Kids",
            place = "R. Marli, 191 - São Paulo",
            order = 0,
        )

        assertEquals(expected, WeeklyService.fromRow(row(0)))
    }
}
