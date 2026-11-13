package com.carolinagandin.cuidadog.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

fun getEmojiForTipo(tipo: String): String {
    return when (tipo) {
        "Medicamento" -> "💊"
        "Vacuna" -> "💉"
        "Pipeta" -> "💧"
        "Desparasitación" -> "🛡️"
        "Baño" -> "🛁"
        "Peluquería" -> "✂️"
        "Control veterinario" -> "🩺"
        "Peso" -> "⚖️"
        else -> "🐾"
    }
}

@Composable
fun TipoCuidadoLabel(
    tipo: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tipo == "Alimentación") {
            AlimentacionIcon(
                size = 18.dp,
                modifier = Modifier.clearAndSetSemantics {}
            )
        } else {
            Text(
                text = getEmojiForTipo(tipo),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = tipo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
