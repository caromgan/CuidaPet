package com.carolinagandin.cuidadog.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

@SuppressLint("NewApi")
class CuidadoAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Cuidado pendiente"
        val tipo = intent.getStringExtra("tipo") ?: "Cuidado"
        val hora = intent.getStringExtra("hora") ?: "08:00 AM"
        val notas = intent.getStringExtra("notas") ?: ""
        val mascotaNombre = intent.getStringExtra("mascotaNombre") ?: "Mascota"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "clan_pet_channel_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios Clan Pet",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de cuidados para el Clan Pet"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notasClean = notas.trim()
        val tieneNotas = notasClean.isNotBlank() &&
                !notasClean.equals("sin nada", ignoreCase = true) &&
                !notasClean.equals("ninguna", ignoreCase = true) &&
                !notasClean.equals("sin observaciones", ignoreCase = true)

        val lineaObservaciones = if (tieneNotas) "📝 *Observaciones:* $notasClean" else ""

        val whatsappText = buildString {
            append("🐾 *¡Hola Clan Pet!*\n\n")
            append("Les recuerdo que hoy tenemos programado el siguiente cuidado para *")
            append(mascotaNombre)
            append("*:\n\n")
            append("📌 *Cuidado:* ")
            append(tipo)
            append(" (")
            append(titulo)
            append(")\n")
            append("⏰ *Horario:* ")
            append(hora)
            append(" hs")
            if (lineaObservaciones.isNotBlank()) {
                append("\n")
                append(lineaObservaciones)
            }
            append("\n\n¡Muchas gracias por ayudar en su cuidado! 🧡")
        }

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, whatsappText)
        }

        val chooserIntent = Intent.createChooser(whatsappIntent, "Avisar al Clan Pet").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val actionPendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            chooserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("🐾 cuidaPET: Recordatorio del Clan Pet")
            .setContentText("Mascota: $mascotaNombre | Cuidado: $tipo ($titulo) | Hora: $hora")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Mascota: $mascotaNombre | Cuidado: $tipo ($titulo) | Hora: $hora\nDetalle: $notas")
            )
            .addAction(
                android.R.drawable.ic_menu_send,
                "📲 Avisar al Clan Pet",
                actionPendingIntent
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
