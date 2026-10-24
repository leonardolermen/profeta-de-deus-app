package br.org.profetas.app.support.storage

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class AndroidImageReader(private val contentResolver: ContentResolver) : ImageReader {

    // Decodificar e comprimir uma foto de 12 MB é trabalho de CPU pesado; fora do Default travaria a tela.
    override suspend fun readJpeg(source: Uri, maxSide: Int, quality: Int): ByteArray =
        withContext(Dispatchers.Default) {
            val decoded = decodeReduced(source, maxSide)
            val oriented = applyOrientation(decoded, readOrientation(source))
            val flattened = onWhiteBackground(oriented)

            val output = ByteArrayOutputStream()
            flattened.compress(Bitmap.CompressFormat.JPEG, quality, output)
            flattened.recycle()

            output.toByteArray()
        }

    private fun decodeReduced(source: Uri, maxSide: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(source).use { stream -> BitmapFactory.decodeStream(stream, null, bounds) }

        // inSampleSize primeiro: decodifica já menor e evita carregar os pixels originais inteiros na memória.
        // A escala exata vem depois, porque inSampleSize só reduz por potências de 2.
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSide)
        }
        val sampled = open(source).use { stream -> BitmapFactory.decodeStream(stream, null, options) }
            ?: throw IOException("Nao foi possivel ler a imagem")

        val target = targetSize(sampled.width, sampled.height, maxSide)

        if (target.width == sampled.width && target.height == sampled.height) {
            return sampled
        }

        val scaled = Bitmap.createScaledBitmap(sampled, target.width, target.height, true)
        sampled.recycle()

        return scaled
    }

    private fun readOrientation(source: Uri): Int =
        open(source).use { stream ->
            ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }

        val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()

        return oriented
    }

    // JPEG não tem canal alfa: sem o fundo branco, a transparência de um PNG viraria preto.
    private fun onWhiteBackground(bitmap: Bitmap): Bitmap {
        val flattened = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(flattened)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        bitmap.recycle()

        return flattened
    }

    private fun open(source: Uri) =
        contentResolver.openInputStream(source) ?: throw IOException("Nao foi possivel abrir a imagem")
}
