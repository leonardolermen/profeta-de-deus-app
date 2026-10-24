package br.org.profetas.app.event.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.support.ui.EmptyState

private val cardShape = RoundedCornerShape(16.dp)

// Cartões de "Cultos Semanais" de app/(tabs)/eventos.tsx (serviceCardVertical e vizinhos); as medidas são as do
// StyleSheet de lá. No Expo os cultos eram fixos no código; aqui vêm da tabela cultos_semanais.
@Composable
fun WeeklyServicesSection(services: List<WeeklyService>, isLoading: Boolean) {
    if (services.isEmpty()) {
        if (!isLoading) {
            EmptyState("Os horários dos cultos ainda não foram publicados.")
        }

        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        services.forEach { service ->
            WeeklyServiceCard(service)
        }
    }
}

@Composable
private fun WeeklyServiceCard(service: WeeklyService) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = cardShape)
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline, cardShape)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DayBadge(service.dayLabel)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 2.dp),
                )

                if (service.highlight.isNotBlank()) {
                    Text(
                        text = "⭐ ${service.highlight}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (service.times.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DetailIcon("⏰")

                    TimePills(service.times)
                }
            }

            if (service.place.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DetailIcon("📍")

                    Text(
                        text = service.place,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayBadge(label: String) {
    Box(
        modifier = Modifier
            .padding(end = 14.dp)
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DetailIcon(emoji: String) {
    Text(
        text = emoji,
        fontSize = 14.sp,
        modifier = Modifier
            .padding(end = 8.dp)
            .alpha(0.8f),
    )
}

@Composable
private fun TimePills(times: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        times.forEach { time ->
            Text(
                text = time,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
