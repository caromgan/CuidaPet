package com.carolinagandin.cuidadog.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object MapUtils {
    fun abrirVeterinariasGuardia(context: Context) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=veterinaria+de+guardia+24+horas")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/veterinaria+de+guardia+24+horas"))
                context.startActivity(webIntent)
            } catch (_: Exception) {}
        }
    }
}
