package br.org.profetas.app.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.theme.Palette
import java.time.Instant
import java.time.ZoneId

private val monthAbbreviations =
    arrayOf("JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ")

data class TicketDate(val day: String, val month: String, val time: String)

// O Expo formatava esse selo em UTC (ver app/(tabs)/eventos.tsx), o que mostrava o dia errado perto
// da meia-noite para quem está no fuso de São Paulo. Corrigido de propósito: usa o fuso do aparelho.
fun ticketDate(instant: Instant, zone: ZoneId): TicketDate {
    val local = instant.atZone(zone)

    val day = local.dayOfMonth.toString().padStart(2, '0')
    val month = monthAbbreviations[local.monthValue - 1]
    val time = "%02d:%02d".format(local.hour, local.minute)

    return TicketDate(day, month, time)
}

// Selo branco sobre foto de evento (dateTicket/ticketDay/ticketMonth de app/(tabs)/index.tsx).
// As cores vêm direto da Palette, não do MaterialTheme: o selo fica sempre sobre uma foto, então
// mantém a mesma aparência nos temas claro e escuro, igual ao app Expo original.
@Composable
fun DateTicket(ticket: TicketDate, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Palette.OnPhoto)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = ticket.day,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Primary,
            lineHeight = 22.sp,
        )

        Text(
            text = ticket.month,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Palette.Text.copy(alpha = 0.7f),
        )
    }
}
