package br.org.profetas.app.admin

import br.org.profetas.app.account.Email
import br.org.profetas.app.birthday.BirthdayDraft
import br.org.profetas.app.church.Church
import br.org.profetas.app.event.EventDraft
import br.org.profetas.app.event.EventType
import br.org.profetas.app.event.weekly.WeeklyServiceDraft
import br.org.profetas.app.event.weekly.parseTimes
import br.org.profetas.app.study.StudyDraft
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.MonthDay
import java.time.ZoneId

// Faixas do editor anterior: título de evento, estudo e foto até 160; nome de pessoa, igreja e culto até 120.
private val TITLE_LENGTH = 2..160
private val NAME_LENGTH = 2..120
private val WHATSAPP_DIGITS = 10..15
private const val MIN_STUDY_CONTENT_LENGTH = 5

@JvmInline
value class ContentTitle private constructor(val value: String) {

    companion object {

        fun of(raw: String): ContentTitle {
            val trimmed = raw.trim()
            require(trimmed.length in TITLE_LENGTH) { "Informe um título entre 2 e 160 caracteres." }

            return ContentTitle(trimmed)
        }
    }
}

@JvmInline
value class ContentName private constructor(val value: String) {

    companion object {

        fun of(raw: String): ContentName {
            val trimmed = raw.trim()
            require(trimmed.length in NAME_LENGTH) { "Informe um nome entre 2 e 120 caracteres." }

            return ContentName(trimmed)
        }
    }
}

// O texto que o admin digitou em cada editor; toDraft valida e devolve o que o repositório grava, ou lança com a
// mensagem que o admin vê.
data class EventForm(
    val title: String,
    val description: String,
    val date: String,
    val type: EventType,
    val imagePath: String?,
) {

    fun toDraft(zone: ZoneId): EventDraft = EventDraft(
        title = ContentTitle.of(title).value,
        description = description.trim(),
        date = parseEventDate(date, zone).toString(),
        type = type,
        imagePath = imagePath,
    )
}

data class WeeklyServiceForm(
    val name: String,
    val dayOfWeek: DayOfWeek,
    val times: String,
    val highlight: String,
    val place: String,
    val order: String,
) {

    fun toDraft(): WeeklyServiceDraft {
        val parsedTimes = parseTimes(times)
        require(parsedTimes.isNotEmpty()) { "Informe ao menos um horário." }

        // Ordem em branco é 0: os cultos sem ordem definida aparecem primeiro, por dia da semana.
        val position = if (order.isBlank()) 0 else order.trim().toIntOrNull()
        requireNotNull(position) { "Informe a ordem com um número inteiro." }

        return WeeklyServiceDraft(
            name = ContentName.of(name).value,
            dayOfWeek = dayOfWeek,
            times = parsedTimes,
            highlight = highlight.trim(),
            place = place.trim(),
            order = position,
        )
    }
}

data class StudyForm(val title: String, val description: String, val category: String, val content: String) {

    fun toDraft(): StudyDraft {
        val trimmedContent = content.trim()
        require(trimmedContent.length >= MIN_STUDY_CONTENT_LENGTH) { "Escreva o conteúdo do estudo." }

        return StudyDraft(
            title = ContentTitle.of(title).value,
            description = description.trim(),
            category = category.trim(),
            content = trimmedContent,
        )
    }
}

data class BirthdayForm(val name: String, val day: String, val month: String, val consent: Boolean) {

    fun toDraft(): BirthdayDraft {
        val date = try {
            MonthDay.of(month.trim().toInt(), day.trim().toInt())
        } catch (e: NumberFormatException) {
            null
        } catch (e: DateTimeException) {
            null
        }
        requireNotNull(date) { "Informe um dia e um mês válidos." }

        return BirthdayDraft(
            name = ContentName.of(name).value,
            day = date.dayOfMonth,
            month = date.monthValue,
            consent = consent,
        )
    }
}

data class ChurchForm(
    val name: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val address: String,
    val history: String,
    val privacy: String,
) {

    // E-mail e WhatsApp podem ficar em branco: a tela Contato só mostra o que estiver preenchido.
    fun toChurch(): Church {
        val validEmail = if (email.isBlank()) "" else Email.of(email).value

        val whatsappDigits = whatsapp.count { character -> character.isDigit() }
        require(whatsapp.isBlank() || whatsappDigits in WHATSAPP_DIGITS) { "Informe o WhatsApp com país e DDD." }

        return Church(
            name = ContentName.of(name).value,
            phone = phone.trim(),
            whatsapp = whatsapp.trim(),
            email = validEmail,
            address = address.trim(),
            history = history.trim(),
            privacy = privacy.trim(),
        )
    }
}
