package com.carolinagandin.cuidadog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carolinagandin.cuidadog.data.local.CuidadoEntity

@Composable
fun CuidadoVisualCard(
    cuidado: CuidadoEntity,
    mostrarFecha: Boolean = false,
    onToggleRealizado: ((CuidadoEntity) -> Unit)? = null,
    accionEditar: @Composable (() -> Unit)? = null,
    accionEliminar: @Composable (() -> Unit)? = null
) {
    val accAnuncioCard = buildString {
        append("${cuidado.tipo}. ")
        append("${cuidado.titulo}. ")
        if (cuidado.realizado) {
            append("Realizado. ")
        } else {
            append("Pendiente. ")
        }
        if (mostrarFecha) {
            append("Fecha: ${cuidado.fecha}. ")
        }
        if (!cuidado.hora.isNullOrBlank()) {
            append("A las ${cuidado.hora} horas. ")
        }
        if (cuidado.valorPeso != null) {
            append("Peso: ${cuidado.valorPeso} kg. ")
        }
        if (!cuidado.notas.isNullOrBlank()) {
            append("Notas: ${cuidado.notas}.")
        }
    }

    val fechaHoraTexto = buildString {
        if (mostrarFecha) {
            append(cuidado.fecha)
        }
        if (!cuidado.hora.isNullOrBlank()) {
            if (isNotEmpty()) append(" · ")
            append(cuidado.hora)
        }
    }

    val (containerColor, accentColor) = when (cuidado.tipo) {
        "Medicamento", "Desparasitación" ->
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) to MaterialTheme.colorScheme.primary
        "Vacuna", "Control veterinario" ->
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f) to MaterialTheme.colorScheme.tertiary
        "Pipeta", "Baño", "Peluquería", "Peso" ->
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f) to MaterialTheme.colorScheme.secondary
        else ->
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Contenido informativo unificado (frase única para TalkBack)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {
                        contentDescription = accAnuncioCard
                    },
                verticalAlignment = Alignment.Top
            ) {
                // Círculo tonal de 48dp con emoji decorativo
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getEmojiForTipo(cuidado.tipo),
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cuidado.tipo.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (cuidado.realizado) {
                            Text(
                                text = "✓ Realizado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = cuidado.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (fechaHoraTexto.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = fechaHoraTexto,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (cuidado.valorPeso != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Peso: ${cuidado.valorPeso} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!cuidado.notas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cuidado.notas,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Acciones inferiores: Marcar realizado, Editar y Eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onToggleRealizado != null) {
                    TextButton(
                        onClick = { onToggleRealizado(cuidado) },
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Text(
                            text = if (cuidado.realizado) "✓ Realizado (Marcar pendiente)" else "Marcar realizado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cuidado.realizado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (accionEditar != null) {
                        accionEditar()
                    }
                    if (accionEliminar != null) {
                        accionEliminar()
                    }
                }
            }
        }
    }
}
