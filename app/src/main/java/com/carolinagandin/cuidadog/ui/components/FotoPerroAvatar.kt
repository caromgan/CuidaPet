package com.carolinagandin.cuidadog.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun decodificarBitmapConDownsampling(
    context: Context,
    fotoUriString: String?,
    reqWidth: Int,
    reqHeight: Int
): ImageBitmap? {
    if (fotoUriString.isNullOrBlank()) return null
    return try {
        val uri = Uri.parse(fotoUriString)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (!file.exists()) return null
            BitmapFactory.decodeFile(file.absolutePath, options)
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        }

        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }

        val bitmap = if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }

        bitmap?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

@Composable
fun FotoPerroAvatar(
    fotoUriString: String?,
    nombrePerro: String?,
    modifier: Modifier = Modifier,
    reqWidth: Int = 256,
    reqHeight: Int = 256,
    shape: Shape = CircleShape,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    emojiSize: TextUnit = 36.sp,
    fallbackBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val context = LocalContext.current

    val imageBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = fotoUriString,
        key2 = reqWidth,
        key3 = reqHeight
    ) {
        value = withContext(Dispatchers.IO) {
            decodificarBitmapConDownsampling(context, fotoUriString, reqWidth, reqHeight)
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (imageBitmap != null) Color.Transparent else fallbackBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            val accDesc = contentDescription ?: if (!nombrePerro.isNullOrBlank()) "Foto de $nombrePerro" else null
            Image(
                bitmap = imageBitmap!!,
                contentDescription = accDesc,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "🐶",
                fontSize = emojiSize,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}
