package com.carolinagandin.cuidadog.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private val TIPOS_DIETA_ALIMENTO = listOf("Balanceado", "BARF-Comida fresca", "Mixta", "Otra")

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgregarPruebaAlimentacionScreen(
    onVolver: () -> Unit,
    onGuardar: (String, String, String, String, Int, String?, String, String, String?, String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var tipoDieta by remember { mutableStateOf(TIPOS_DIETA_ALIMENTO.first()) }
    var otroTipoDieta by remember { mutableStateOf("") }
    var menuDietaExp by remember { mutableStateOf(false) }

    var marcaAlimento by remember { mutableStateOf("") }
    var cantidadPorRacion by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("gramos") }

    val hoy = LocalDate.now()
    var fechaInicio by remember { mutableStateOf(hoy.toString()) }
    var fechaFin by remember { mutableStateOf(hoy.plusDays(7).toString()) }

    val horariosPredefinidos = listOf("08:00", "12:00", "13:00", "17:00", "20:00", "21:00")
    var horariosSeleccionados by remember { mutableStateOf(setOf("08:00", "20:00")) }
    var customHoraInput by remember { mutableStateOf("") }

    val todosLosHorarios = remember(horariosSeleccionados) {
        (horariosPredefinidos + horariosSeleccionados).distinct().sorted()
    }

    var observaciones by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("En curso") }

    var errorMensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Probar nueva marca de alimento",
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Registrá una nueva prueba de alimento y sus horarios para generar automáticamente los cuidados diarios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (errorMensaje != null) {
                item {
                    Text(
                        text = errorMensaje!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Selector desplegable Tipo de Dieta unificado con el Perfil de Mascota
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tipoDieta,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de dieta") },
                        trailingIcon = { Text("▼") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { menuDietaExp = true }
                    )
                    DropdownMenu(
                        expanded = menuDietaExp,
                        onDismissRequest = { menuDietaExp = false }
                    ) {
                        TIPOS_DIETA_ALIMENTO.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    tipoDieta = d
                                    menuDietaExp = false
                                }
                            )
                        }
                    }
                }
            }

            if (tipoDieta == "Otra") {
                item {
                    OutlinedTextField(
                        value = otroTipoDieta,
                        onValueChange = { otroTipoDieta = it },
                        label = { Text("Especificar tipo de dieta") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = marcaAlimento,
                    onValueChange = { marcaAlimento = it },
                    label = { Text("Marca / Alimento *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cantidadPorRacion,
                        onValueChange = { input -> cantidadPorRacion = input.filter { it.isDigit() || it == '.' } },
                        label = { Text("Cantidad por ración") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = { unidad = it },
                        label = { Text("Unidad (g, tazas...)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it },
                        label = { Text("Fecha inicio (AAAA-MM-DD) *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = { fechaFin = it },
                        label = { Text("Fecha vencimiento *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }
            }

            // SELECTOR DE HORARIOS EN GRILLA (CHIPS)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Horarios de alimentación (${horariosSeleccionados.size} raciones/día):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Seleccioná los horarios habituales de toma:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        todosLosHorarios.forEach { hora ->
                            val seleccionado = horariosSeleccionados.contains(hora)
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        horariosSeleccionados = if (seleccionado) {
                                            horariosSeleccionados - hora
                                        } else {
                                            horariosSeleccionados + hora
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = hora,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Agregar horario personalizado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customHoraInput,
                            onValueChange = { customHoraInput = it },
                            label = { Text("Otro horario (ej. 15:30)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        OutlinedButton(
                            onClick = {
                                val trimmed = customHoraInput.trim()
                                if (trimmed.isNotBlank()) {
                                    horariosSeleccionados = horariosSeleccionados + trimmed
                                    customHoraInput = ""
                                }
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Añadir")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones iniciales") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val raciones = horariosSeleccionados.size.coerceAtLeast(1)
                        val horariosStr = horariosSeleccionados.sorted().joinToString(", ")
                        val tipoDietaFinal = if (tipoDieta == "Otra") otroTipoDieta.trim().ifEmpty { "Otra" } else tipoDieta

                        val startDt = try { LocalDate.parse(fechaInicio.trim()) } catch (_: Exception) { null }
                        val endDt = try { LocalDate.parse(fechaFin.trim()) } catch (_: Exception) { null }
                        val hoy = LocalDate.now()

                        if (marcaAlimento.isBlank()) {
                            errorMensaje = "Por favor ingresa la marca o nombre del alimento."
                        } else if (fechaInicio.isBlank() || fechaFin.isBlank()) {
                            errorMensaje = "Las fechas de inicio y vencimiento son obligatorias."
                        } else if (startDt == null || endDt == null) {
                            errorMensaje = "Formato de fecha inválido."
                        } else if (startDt.isBefore(hoy)) {
                            errorMensaje = "La fecha de inicio no puede ser anterior al día de hoy."
                        } else if (endDt.isBefore(startDt)) {
                            errorMensaje = "La fecha de vencimiento debe ser posterior o igual a la fecha de inicio."
                        } else if (horariosSeleccionados.isEmpty()) {
                            errorMensaje = "Seleccioná al menos un horario de alimentación."
                        } else {
                            errorMensaje = null
                            onGuardar(
                                tipoDietaFinal.trim(),
                                marcaAlimento.trim(),
                                cantidadPorRacion.trim(),
                                unidad.trim(),
                                raciones,
                                horariosStr,
                                fechaInicio.trim(),
                                fechaFin.trim(),
                                observaciones.ifBlank { null },
                                estado
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Guardar registro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
