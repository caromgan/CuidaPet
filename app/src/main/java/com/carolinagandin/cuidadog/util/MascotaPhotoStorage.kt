package com.carolinagandin.cuidadog.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MascotaPhotoStorage {

    private fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getCameraTempDir(context: Context): File {
        val dir = File(context.cacheDir, "images/camera")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun guardarFotoPermanente(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = getImagesDir(context)
                val destFile = File(imagesDir, "foto_perro_${System.currentTimeMillis()}.jpg")

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    Uri.fromFile(destFile).toString()
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun crearTempCamaraUri(context: Context): Pair<Uri, File>? {
        return try {
            val cameraDir = getCameraTempDir(context)
            val tempFile = File(cameraDir, "temp_camara_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            Pair(uri, tempFile)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun limpiarTemporales(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val cameraDir = getCameraTempDir(context)
                cameraDir.listFiles()?.forEach { file ->
                    if (file.isFile) file.delete()
                }
            } catch (_: Exception) {
            }
        }
    }
}
