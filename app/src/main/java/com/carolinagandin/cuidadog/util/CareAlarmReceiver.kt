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
class CareAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Cuidado pendiente"
        val tipo = intent.getStringExtra("tipo") ?: "Cuidado"
        val hora = intent.getStringExtra("hora") ?: "Hoy"
        val notas = intent.getStringExtra("notas") ?: ""
        val mascotaNombre = intent.getStringExtra("mascotaNombre") ?: "Mascota"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "clan_pet_channel"

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

        val whatsappText = "🐾 Recordatorio de cuidaPET:\nHola Clan Pet, les recuerdo que hoy tenemos el cuidado de $titulo ($tipo) para $mascotaNombre a las $hora. Detalle: $notas"
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
