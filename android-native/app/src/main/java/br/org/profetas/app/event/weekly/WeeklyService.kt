package br.org.profetas.app.event.weekly

import br.org.profetas.app.support.http.textOrEmpty
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek

// Na mesma ordem de dia_semana no banco: 0 = domingo, como o getDay() do JavaScript que o Expo usava.
private val DAY_LABELS = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

data class WeeklyService(
    val id: String,
    val name: String,
    val dayOfWeek: DayOfWeek,
    val times: List<String>,
    val highlight: String,
    val place: String,
    val order: Int,
) {

    val dayLabel: String get() = DAY_LABELS[dayOfWeek.databaseDay]

    companion object {

        fun fromRow(row: JSONObject): WeeklyService {
            val times = row.optJSONArray("horarios") ?: JSONArray()

            return WeeklyService(
                id = row.getString("id"),
                name = row.getString("nome"),
                dayOfWeek = dayOfWeekFromDatabase(row.getInt("dia_semana")),
                times = (0 until times.length()).map { index -> times.getString(index) },
                highlight = row.textOrEmpty("destaque"),
                place = row.textOrEmpty("local"),
                order = row.optInt("ordem"),
            )
        }
    }
}

data class WeeklyServiceDraft(
    val name: String,
    val dayOfWeek: DayOfWeek,
    val times: List<String>,
    val highlight: String,
    val place: String,
    val order: Int,
)

// java.time conta de segunda (1) a domingo (7); o banco conta de domingo (0) a sábado (6).
fun dayOfWeekFromDatabase(databaseDay: Int): DayOfWeek = DayOfWeek.SUNDAY.plus(databaseDay.toLong())

val DayOfWeek.databaseDay: Int get() = value % 7
