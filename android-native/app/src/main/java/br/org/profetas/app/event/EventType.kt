package br.org.profetas.app.event

import androidx.annotation.DrawableRes
import br.org.profetas.app.R

enum class EventType(val databaseValue: String, val label: String, @DrawableRes val defaultImage: Int) {
    CULTO("culto", "Culto", R.drawable.card1),
    JOVENS("jovens", "Jovens", R.drawable.jovens),
    MULHERES("mulheres", "Mulheres", R.drawable.culto_mulheres),
    CAMPANHA("campanha", "Campanha", R.drawable.card1),
    BATISMO("batismo", "Batismo", R.drawable.batismo);

    companion object {

        // Tipo desconhecido ou em branco vira Culto: é o tipo de evento mais comum da igreja,
        // então uma categoria nova ou mal cadastrada no banco ainda aparece com uma imagem razoável.
        fun fromDatabase(value: String): EventType {
            return entries.firstOrNull { it.databaseValue.equals(value, ignoreCase = true) } ?: CULTO
        }
    }
}
