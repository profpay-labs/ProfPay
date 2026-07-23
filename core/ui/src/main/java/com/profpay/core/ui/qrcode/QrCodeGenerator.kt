package com.profpay.core.ui.qrcode

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Генерирует QR-код как Bitmap с использованием цветов Material Theme.
 *
 * @param text текст для кодирования
 * @param size размер QR-кода в пикселях
 * @param foregroundColor цвет QR-модулей (по умолчанию primary)
 * @param backgroundColor цвет фона (по умолчанию onPrimary)
 * @return State<Bitmap?> — QR-код или null при ошибке
 */
@Composable
fun rememberQrCodeBitmap(
    text: String,
    size: Int = 400,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
): State<Bitmap?> {
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }

    val fgArgb = foregroundColor.toArgb()
    val bgArgb = backgroundColor.toArgb()

    LaunchedEffect(text, fgArgb, bgArgb) {
        // Не генерируем QR-код для пустого текста
        bitmapState.value = if (text.isBlank()) {
            null
        } else {
            generateQrCodeBitmap(text, size, fgArgb, bgArgb)
        }
    }

    return bitmapState
}

/**
 * Генерирует QR-код синхронно (вызывать из background thread).
 */
suspend fun generateQrCodeBitmap(
    text: String,
    size: Int,
    foregroundArgb: Int,
    backgroundArgb: Int,
): Bitmap? = withContext(Dispatchers.Default) {
    if (text.isBlank()) {
        return@withContext null
    }

    try {
        val bitMatrix = QRCodeWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size,
        )

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (bitMatrix[x, y]) foregroundArgb else backgroundArgb
            }
        }

        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    } catch (e: WriterException) {
        null
    }
}
