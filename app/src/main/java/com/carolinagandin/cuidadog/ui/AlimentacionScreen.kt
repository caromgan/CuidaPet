package com.carolinagandin.cuidadog.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.carolinagandin.cuidadog.R
import com.carolinagandin.cuidadog.ui.components.AlimentacionIcon
import com.carolinagandin.cuidadog.ui.components.DraggableFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carolinagandin.cuidadog.data.local.AlimentacionEvolucionEntity
import com.carolinagandin.cuidadog.data.local.AlimentacionHistorialEntity
import com.carolinagandin.cuidadog.data.local.AlimentacionPruebaEntity
import com.carolinagandin.cuidadog.data.local.PerroEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentacionScreen(
    mascota: PerroEntity,
    pruebas: List<AlimentacionPruebaEntity>,
    ultimaPrueba: AlimentacionPruebaEntity?,
    historial: List<AlimentacionHistorialEntity>,
    onActualizarEstadoPrueba: (AlimentacionPruebaEntity, String, Boolean) -> Unit,
    onActualizarAlimentacionPerfil: (String?, String?, String?, Int?, String?, String?) -> Unit,
    onAgregarEvolucion: (Long, String, String, String?) -> Unit,
    getEvoluciones: (Long) -> Flow<List<AlimentacionEvolucionEntity>>,
    onAgregarPruebaClick: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var pruebaSeleccionadaParaEvolucion by remember { mutableStateOf<AlimentacionPruebaEntity?>(null) }
    var pruebaPendienteActualizarPerfil by remember { mutableStateOf<AlimentacionPruebaEntity?>(null) }

    val pruebasEnCurso = pruebas.filter { it.estado == "En curso" }
    val pruebasHistorial = pruebas.filter { it.estado != "En curso" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alimentación - ${mascota.nombre}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        floatingActionButton = {
            DraggableFloatingActionButton(
                onClick = onAgregarPruebaClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.defaultMinSize(minHeight = 56.dp)
            ) {
                Text("+ Probar nueva marca de alimento", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. PLAN ACTUAL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AlimentacionIcon(size = 24.dp)
                            Text(
                                text = "Plan actual de alimentación",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (mascota.tipoDieta.isNullOrBlank() && mascota.marcaAlimento.isNullOrBlank()) {
                            Text(
                                text = "No hay un plan de alimentación configurado en el perfil. Podés iniciar una prueba o actualizar el perfil.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text("Tipo de dieta: ${mascota.tipoDieta ?: "No especificada"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Marca / Alimento: ${mascota.marcaAlimento ?: "No especificada"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Cantidad: ${mascota.cantidadPorRacion ?: "-"} (${mascota.racionesByDiaText()})", style = MaterialTheme.typography.bodyMedium)
                            if (!mascota.horariosAlimentacion.isNullOrBlank()) {
                                Text("Horarios: ${mascota.horariosAlimentacion}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!mascota.restriccionesAlimentarias.isNullOrBlank()) {
                                Text("Restricciones: ${mascota.restriccionesAlimentarias}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // 2. PRUEBAS EN CURSO
            item {
                Text(
                    text = "Pruebas en curso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
            }

            if (pruebasEnCurso.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No hay pruebas de alimentación en curso actualmente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(pruebasEnCurso) { prueba ->
                    PruebaCard(
                        prueba = prueba,
                        getEvoluciones = getEvoluciones,
                        onAgregarEvolucionClick = { pruebaSeleccionadaParaEvolucion = prueba },
                        onCambiarEstado = { nuevoEstado ->
                            if (nuevoEstado == "Aprobada") {
                                pruebaPendienteActualizarPerfil = prueba
                            } else {
                                onActualizarEstadoPrueba(prueba, nuevoEstado, false)
                            }
                        }
                    )
                }
            }

            // 3. HISTORIAL DE PRUEBAS / REGISTROS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Historial de pruebas y cambios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
            }

            if (pruebasHistorial.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "Aún no hay pruebas finalizadas, aprobadas o descartadas en el historial.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(pruebasHistorial) { prueba ->
                    PruebaCard(
                        prueba = prueba,
                        getEvoluciones = getEvoluciones,
                        onAgregarEvolucionClick = null,
                        onCambiarEstado = null
                    )
                }
            }
        }
    }

    // DIÁLOGO: AGREGAR EVOLUCIÓN
    if (pruebaSeleccionadaParaEvolucion != null) {
        val prueba = pruebaSeleccionadaParaEvolucion!!
        DialogNuevaEvolucion(
            onDismiss = { pruebaSeleccionadaParaEvolucion = null },
            onGuardar = { fecha, valo, obs ->
                onAgregarEvolucion(prueba.id, fecha, valo, obs)
                pruebaSeleccionadaParaEvolucion = null
            }
        )
    }

    // ALERT DIALOG: ACTUALIZAR PERFIL AL APROBAR
    if (pruebaPendienteActualizarPerfil != null) {
        val prueba = pruebaPendienteActualizarPerfil!!
        AlertDialog(
            onDismissRequest = { pruebaPendienteActualizarPerfil = null },
            title = { Text("Aprobar prueba de alimentación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Querés seguir registrando esta alimentación como cuidado diario?") },
            confirmButton = {
                Button(
                    onClick = {
                        onActualizarAlimentacionPerfil(
                            prueba.tipoDieta,
                            prueba.marcaAlimento,
                            "${prueba.cantidadPorRacion} ${prueba.unidad}",
                            prueba.racionesPorDia,
                            prueba.horarios,
                            prueba.observaciones
                        )
                        onActualizarEstadoPrueba(prueba, "Aprobada", true)
                        Toast.makeText(context, "Prueba aprobada y rutina de cuidados mantenida", Toast.LENGTH_SHORT).show()
                        pruebaPendienteActualizarPerfil = null
                    }
                ) {
                    Text("SÍ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onActualizarAlimentacionPerfil(
                            prueba.tipoDieta,
                            prueba.marcaAlimento,
                            "${prueba.cantidadPorRacion} ${prueba.unidad}",
                            prueba.racionesPorDia,
                            prueba.horarios,
                            prueba.observaciones
                        )
                        onActualizarEstadoPrueba(prueba, "Aprobada", false)
                        Toast.makeText(context, "Prueba aprobada sin cuidados futuros recurrentes", Toast.LENGTH_SHORT).show()
                        pruebaPendienteActualizarPerfil = null
                    }
                ) {
                    Text("NO")
                }
            }
        )
    }
}

private fun PerroEntity.racionesByDiaText(): String {
    val r = racionesPorDia ?: 0
    return if (r == 1) "1 ración al día" else "$r raciones al día"
}

@Composable
fun PruebaCard(
    prueba: AlimentacionPruebaEntity,
    getEvoluciones: (Long) -> Flow<List<AlimentacionEvolucionEntity>>,
    onAgregarEvolucionClick: (() -> Unit)?,
    onCambiarEstado: ((String) -> Unit)?
) {
    val evoluciones by getEvoluciones(prueba.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${prueba.tipoDieta} - ${prueba.marcaAlimento}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                EstadoBadge(estado = prueba.estado)
            }

            Text("Cantidad: ${prueba.cantidadPorRacion} ${prueba.unidad} (${prueba.racionesPorDia} raciones/día)", style = MaterialTheme.typography.bodyMedium)
            if (!prueba.horarios.isNullOrBlank()) {
                Text("Horarios: ${prueba.horarios}", style = MaterialTheme.typography.bodySmall)
            }
            Text("Fecha de inicio: ${prueba.fechaInicio}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!prueba.observaciones.isNullOrBlank()) {
                Text("Observaciones: ${prueba.observaciones}", style = MaterialTheme.typography.bodySmall)
            }

            if (evoluciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Evoluciones (${evoluciones.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                evoluciones.forEach { evo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(evo.fecha, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(evo.valoracion, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            if (!evo.observacion.isNullOrBlank()) {
                                Text(evo.observacion!!, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (onAgregarEvolucionClick != null || onCambiarEstado != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onAgregarEvolucionClick != null) {
                        OutlinedButton(onClick = onAgregarEvolucionClick, modifier = Modifier.weight(1f)) {
                            Text("+ Evolución")
                        }
                    }
                    if (onCambiarEstado != null) {
                        var mostrarMenuEstados by remember { mutableStateOf(false) }
                        OutlinedButton(onClick = { mostrarMenuEstados = true }, modifier = Modifier.weight(1f)) {
                            Text("Estado...")
                        }
                        if (mostrarMenuEstados) {
                            AlertDialog(
                                onDismissRequest = { mostrarMenuEstados = false },
                                title = { Text("Cambiar estado de prueba") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Aprobada", "Descartada", "Finalizada").forEach { est ->
                                            TextButton(
                                                onClick = {
                                                    mostrarMenuEstados = false
                                                    onCambiarEstado(est)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(est, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { mostrarMenuEstados = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: String) {
    val containerColor = when (estado) {
        "Aprobada" -> MaterialTheme.colorScheme.primaryContainer
        "Descartada" -> MaterialTheme.colorScheme.errorContainer
        "Finalizada" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@SuppressLint("NewApi")
@Composable
fun DialogNuevaEvolucion(
    onDismiss: () -> Unit,
    onGuardar: (String, String, String?) -> Unit
) {
    var fecha by remember { mutableStateOf(LocalDate.now().toString()) }
    var valoracion by remember { mutableStateOf("Acepta bien") }
    var observacion by remember { mutableStateOf("") }

    val valoraciones = listOf("Acepta bien", "Regular", "No acepta")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva evolución de prueba", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Valoración:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                valoraciones.forEach { valo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { valoracion = valo },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = valoracion == valo,
                            onClick = { valoracion = valo }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(valo, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it },
                    label = { Text("Observación (heces, apetito, energía...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(fecha, valoracion, observacion.ifBlank { null })
                }
            ) {
                Text("Guardar evolución")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
