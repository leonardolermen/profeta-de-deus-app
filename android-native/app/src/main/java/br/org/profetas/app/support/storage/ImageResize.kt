package br.org.profetas.app.support.storage

data class TargetSize(val width: Int, val height: Int)

const val DEFAULT_MAX_SIDE = 1600

/** Mantém a proporção e nunca amplia: uma foto pequena sobe do jeito que está. */
fun targetSize(width: Int, height: Int, maxSide: Int = DEFAULT_MAX_SIDE): TargetSize {
    val longestSide = maxOf(width, height)

    if (longestSide <= maxSide) {
        return TargetSize(width, height)
    }

    val ratio = maxSide.toDouble() / longestSide

    return TargetSize(
        width = maxOf(1, Math.round(width * ratio).toInt()),
        height = maxOf(1, Math.round(height * ratio).toInt()),
    )
}

/**
 * Maior potência de 2 que ainda deixa o lado maior com pelo menos [maxSide]. Nunca passa disso para a escala
 * final partir de uma imagem com resolução suficiente, em vez de ampliar um decode já pequeno demais.
 */
fun sampleSizeFor(width: Int, height: Int, maxSide: Int): Int {
    val longestSide = maxOf(width, height)
    var sampleSize = 1

    while (longestSide / (sampleSize * 2) >= maxSide) {
        sampleSize *= 2
    }

    return sampleSize
}
