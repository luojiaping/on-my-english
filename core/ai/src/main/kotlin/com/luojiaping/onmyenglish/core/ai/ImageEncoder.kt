package com.luojiaping.onmyenglish.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import com.luojiaping.onmyenglish.core.common.AppError
import com.luojiaping.onmyenglish.core.common.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class ImageEncoder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun encode(uriString: String): AppResult<String> {
        val encoded = runCatching {
            val uri = Uri.parse(uriString)
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                val scale = min(MAX_DIMENSION.toDouble() / width, MAX_DIMENSION.toDouble() / height)
                if (scale < 1.0) {
                    decoder.setTargetSize(
                        (width * scale).roundToInt(),
                        (height * scale).roundToInt(),
                    )
                }
            }
            val bytes = ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output))
                output.toByteArray()
            }
            bitmap.recycle()
            "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
        }
        return encoded.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { error ->
                AppResult.Failure(
                    AppError.Validation(error.message ?: "The selected image could not be read"),
                )
            },
        )
    }

    private companion object {
        const val JPEG_QUALITY = 82
        const val MAX_DIMENSION = 1_600
    }
}
