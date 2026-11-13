package com.carolinagandin.cuidadog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carolinagandin.cuidadog.R
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import com.carolinagandin.cuidadog.ui.components.getEmojiForTipo
import com.carolinagandin.cuidadog.util.DateUtils
import java.time.LocalDate

val TIPOS_CUIDADO_CON_EMOJI = listOf(
    "Medicamento" to "💊",
    "Vacuna" to "💉",
    "Pipeta" to "💧",
    "Desparasitación" to "🛡️",
    "Baño" to "🛁",
    "Peluquería" to "✂️",
    "Control veterinario" to "🩺",
    "Peso" to "⚖️",
    "Otro" to "🐾"
)

val TIPOS_CUIDADO = TIPOS_CUIDADO_CON_EMOJI.map { it.first }

val UNIDADES_REPETICION = listOf("días", "semanas", "meses", "años")
val DIAS_SEMANA = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarCuidadoScreen(
    onVolver: () -> Unit,
    onGuardar: (CuidadoEntity) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val hoyStr = remember { LocalDate.now().toString() }

    var tipoSeleccionado by remember { mutableStateOf(TIPOS_CUIDADO.first()) }
    var menuTipoExpanded by remember { mutableStateOf(false) }

    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(hoyStr) }
    var hora by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var valorPesoText by remember { mutableStateOf("") }

    var tieneRepeticion by remember { mutableStateOf(false) }
    var repeticionCantidadText by remember { mutableStateOf("1") }
    var repeticionUnidad by remember { mutableStateOf(UNIDADES_REPETICION.first()) }

    // Días de la semana seleccionados
    val diasSeleccionados = remember { mutableStateOf(setOf<String>()) }

    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val accVolverDesc = stringResource(R.string.acc_volver)
    val accSeleccionarTipoDesc = stringResource(R.string.acc_seleccionar_tipo_cuidado)
    val labelRepetirStr = stringResource(R.string.label_repetir_cuidado)

    val errTituloStr = stringResource(R.string.error_titulo_obligatorio)
    val errFechaStr = stringResource(R.string.error_fecha_obligatoria)
    val errPesoStr = stringResource(R.string.error_peso_invalido)
    val errRepStr = stringResource(R.string.error_repeticion_invalida)

    val guardarAccion: () -> Unit = {
        val tituloClean = titulo.trim()
        val fechaClean = fecha.trim()
        val horaClean = hora.trim().ifEmpty { null }
        val notasClean = notas.trim().ifEmpty { null }

        if (tituloClean.isEmpty()) {
            errorMensaje = errTituloStr
        } else if (fechaClean.isEmpty()) {
            errorMensaje = errFechaStr
        } else if (!DateUtils.esFechaHoraValida(fechaClean, horaClean)) {
            errorMensaje = "La fecha y hora del cuidado deben ser futuras."
        } else {
            var pesoVal: Double? = null
            var errorEncontrado = false
            if (tipoSeleccionado == "Peso") {
                val parsed = valorPesoText.replace(',', '.').toDoubleOrNull()
                if (parsed == null || parsed <= 0) {
                    errorMensaje = errPesoStr
                    errorEncontrado = true
                } else {
                    pesoVal = parsed
                }
            }

            var repCant: Int? = null
            var repUnid: String? = null
            var diasStr: String? = null
            if (!errorEncontrado && tieneRepeticion) {
                val parsedCant = repeticionCantidadText.toIntOrNull()
                if (parsedCant == null || parsedCant <= 0) {
                    errorMensaje = errRepStr
                    errorEncontrado = true
                } else {
                    repCant = parsedCant
                    repUnid = repeticionUnidad
                    if (diasSeleccionados.value.isNotEmpty()) {
                        diasStr = diasSeleccionados.value.joinToString(",")
                    }
                }
            }

            if (!errorEncontrado) {
                errorMensaje = null
                val nuevoCuidado = CuidadoEntity(
                    tipo = tipoSeleccionado,
                    titulo = tituloClean,
                    fecha = fechaClean,
                    hora = horaClean,
                    tieneRepeticion = tieneRepeticion,
                    repeticionCantidad = repCant,
                    repeticionUnidad = repUnid,
                    diasSemana = diasStr,
                    notas = notasClean,
                    valorPeso = pesoVal,
                    realizado = false
                )
                onGuardar(nuevoCuidado)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✨", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.titulo_nuevo_cuidado),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text(
                                text = "¡Planificá un momento feliz y saludable!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                contentDescription = accVolverDesc
                            }
                    ) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = guardarAccion,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                    ) {
                        Text(
                            text = "🎉 ${stringResource(R.string.btn_guardar_cuidado)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // SECCIÓN 1: INFORMACIÓN GENERAL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp, 20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🐾 Información General",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.semantics { heading() }
                            )
                        }

                        // Seleccionar Tipo con Emoji destacado
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = "${getEmojiForTipo(tipoSeleccionado)}  $tipoSeleccionado",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.campo_tipo_cuidado)) },
                                trailingIcon = {
                                    Text(
                                        text = "▼",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.clearAndSetSemantics {}
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .semantics {
                                        contentDescription = "$accSeleccionarTipoDesc. $tipoSeleccionado"
                                    }
                                    .clickable { menuTipoExpanded = true }
                            )
                            DropdownMenu(
                                expanded = menuTipoExpanded,
                                onDismissRequest = { menuTipoExpanded = false }
                            ) {
                                TIPOS_CUIDADO_CON_EMOJI.forEach { (option, emoji) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(emoji, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(option, fontWeight = FontWeight.SemiBold)
                                            }
                                        },
                                        onClick = {
                                            tipoSeleccionado = option
                                            menuTipoExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Título
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text(stringResource(R.string.campo_titulo)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Fecha y Hora en Fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = fecha,
                                onValueChange = { fecha = it },
                                label = { Text(stringResource(R.string.campo_fecha)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = hora,
                                onValueChange = { hora = it },
                                label = { Text(stringResource(R.string.campo_hora)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (tipoSeleccionado == "Peso" || tieneRepeticion) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (tipoSeleccionado == "Peso") {
                            OutlinedTextField(
                                value = valorPesoText,
                                onValueChange = { valorPesoText = it },
                                label = { Text(stringResource(R.string.campo_peso)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = if (tieneRepeticion) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // SECCIÓN 2: FRECUENCIA / REPETICIÓN
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp, 20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔄 Frecuencia / Repetición",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.semantics { heading() }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .toggleable(
                                    value = tieneRepeticion,
                                    onValueChange = { tieneRepeticion = it },
                                    role = Role.Checkbox
                                )
                        ) {
                            Checkbox(
                                checked = tieneRepeticion,
                                onCheckedChange = null,
                                modifier = Modifier.clearAndSetSemantics {}
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(labelRepetirStr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        if (tieneRepeticion) {
                            OutlinedTextField(
                                value = repeticionCantidadText,
                                onValueChange = { repeticionCantidadText = it },
                                label = { Text(stringResource(R.string.campo_repetir_cantidad)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.label_frecuencia),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                UNIDADES_REPETICION.forEach { unidad ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .clickable { repeticionUnidad = unidad }
                                    ) {
                                        RadioButton(
                                            selected = (repeticionUnidad == unidad),
                                            onClick = null,
                                            modifier = Modifier.clearAndSetSemantics {}
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(unidad, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Días específicos de la semana (opcional):",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DIAS_SEMANA.forEach { dia ->
                                val seleccionado = diasSeleccionados.value.contains(dia)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 40.dp)
                                        .clickable {
                                            val actual = diasSeleccionados.value.toMutableSet()
                                            if (seleccionado) actual.remove(dia) else actual.add(dia)
                                            diasSeleccionados.value = actual
                                        }
                                ) {
                                    Checkbox(
                                        checked = seleccionado,
                                        onCheckedChange = { checked ->
                                            val actual = diasSeleccionados.value.toMutableSet()
                                            if (checked) actual.add(dia) else actual.remove(dia)
                                            diasSeleccionados.value = actual
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(dia, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN 3: OBSERVACIONES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp, 20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.tertiary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📝 Observaciones",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.semantics { heading() }
                            )
                        }

                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text(stringResource(R.string.campo_notas)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }

            if (errorMensaje != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚠️ $errorMensaje",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                                .semantics {
                                    liveRegion = LiveRegionMode.Polite
                                }
                        )
                    }
                }
            }
        }
    }
}
