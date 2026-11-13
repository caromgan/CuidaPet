package com.carolinagandin.cuidadog.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carolinagandin.cuidadog.R
import com.carolinagandin.cuidadog.data.local.PerroEntity
import com.carolinagandin.cuidadog.ui.components.CeloDatePickerDialog
import com.carolinagandin.cuidadog.ui.components.FotoPerroAvatar
import com.carolinagandin.cuidadog.util.CeloUtils
import com.carolinagandin.cuidadog.util.MascotaPhotoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMascotaScreen(
    mascotaInicial: PerroEntity,
    onVolver: () -> Unit,
    onGuardarCambios: (PerroEntity) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var nombre by remember { mutableStateOf(mascotaInicial.nombre) }

    val esEspecieComun = mascotaInicial.especie in listOf("Perro", "Gato")
    var especieSeleccionada by remember {
        mutableStateOf(if (esEspecieComun) mascotaInicial.especie else "Otra")
    }
    var especiePersonalizada by remember {
        mutableStateOf(if (esEspecieComun) "" else mascotaInicial.especie)
    }
    var menuEspecieExpanded by remember { mutableStateOf(false) }

    var raza by remember { mutableStateOf(mascotaInicial.raza) }
    var sexoSeleccionado by remember { mutableStateOf(mascotaInicial.sexo) }
    var fechaNacimientoIso by remember { mutableStateOf(mascotaInicial.fechaNacimientoIso ?: "") }
    var edadAproximadaText by remember { mutableStateOf(mascotaInicial.edadAproximadaAnios?.toString() ?: "") }
    var tallaSeleccionada by remember { mutableStateOf(mascotaInicial.talla) }
    var pesoKgText by remember { mutableStateOf(if (mascotaInicial.pesoKg > 0.0) mascotaInicial.pesoKg.toString() else "") }

    var color by remember { mutableStateOf(mascotaInicial.color ?: "") }
    var castrado by remember { mutableStateOf(mascotaInicial.castrado ?: false) }
    var numeroMicrochip by remember { mutableStateOf(mascotaInicial.numeroMicrochip ?: "") }

    // Celo y Menstruación (Condicional: Hembra no castrada)
    var historialCelosFechas by remember { mutableStateOf(CeloUtils.parsearHistorial(mascotaInicial.historialCelos)) }
    var mostrarDatePickerCelo by remember { mutableStateOf(false) }

    val mostrarCeloSeccion = sexoSeleccionado.equals("Hembra", true) && !castrado
    if (!mostrarCeloSeccion && historialCelosFechas.isNotEmpty()) {
        historialCelosFechas = emptyList()
    }

    // Historial médico
    var vacunasInfo by remember { mutableStateOf(mascotaInicial.vacunasInfo ?: "") }
    var desparasitacionInfo by remember { mutableStateOf(mascotaInicial.desparasitacionInfo ?: "") }
    var enfermedadesCronicas by remember { mutableStateOf(mascotaInicial.enfermedadesCronicas ?: "") }
    var alergias by remember { mutableStateOf(mascotaInicial.alergias ?: "") }
    var medicacionHabitual by remember { mutableStateOf(mascotaInicial.medicacionHabitual ?: "") }

    // Alimentación y rutina
    val dietaInicial = mascotaInicial.tipoDieta
    val esOtraDieta = dietaInicial != null && !TIPOS_DIETA.contains(dietaInicial)
    var tipoDietaSeleccionada by remember { mutableStateOf(if (esOtraDieta) "Otra" else (dietaInicial ?: TIPOS_DIETA.first())) }
    var otroTipoDieta by remember { mutableStateOf(if (esOtraDieta) dietaInicial!! else "") }
    var marcaAlimento by remember { mutableStateOf(mascotaInicial.marcaAlimento ?: "") }
    var cantidadPorRacion by remember { mutableStateOf(mascotaInicial.cantidadPorRacion ?: "") }
    var racionesPorDiaText by remember { mutableStateOf(mascotaInicial.racionesPorDia?.toString() ?: "") }
    var horariosAlimentacion by remember { mutableStateOf(mascotaInicial.horariosAlimentacion ?: "") }
    var restriccionesAlimentarias by remember { mutableStateOf(mascotaInicial.restriccionesAlimentarias ?: "") }
    var observaciones by remember { mutableStateOf(mascotaInicial.observaciones ?: "") }

    // Contactos de emergencia
    var veterinario by remember { mutableStateOf(mascotaInicial.veterinario ?: "") }
    var urgencias24h by remember { mutableStateOf(mascotaInicial.urgencias24h ?: "") }
    var contactoPrincipal by remember { mutableStateOf(mascotaInicial.contactoPrincipal ?: "") }
    var contactoSecundario by remember { mutableStateOf(mascotaInicial.contactoSecundario ?: "") }
    var contactoAlternativo by remember { mutableStateOf(mascotaInicial.contactoAlternativo ?: "") }

    var fotoUriString by remember { mutableStateOf(mascotaInicial.fotoUri) }
    var tempCameraPair by remember { mutableStateOf<Pair<Uri, File>?>(null) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val accTomarFotoDesc = stringResource(R.string.acc_tomar_foto)
    val accElegirGaleriaDesc = stringResource(R.string.acc_elegir_galeria)

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val savedUri = MascotaPhotoStorage.guardarFotoPermanente(context, uri)
                if (savedUri != null) {
                    fotoUriString = savedUri
                }
            }
        }
    }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val cameraPair = tempCameraPair
        if (success && cameraPair != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val savedUri = MascotaPhotoStorage.guardarFotoPermanente(context, cameraPair.first)
                if (savedUri != null) {
                    fotoUriString = savedUri
                }
                MascotaPhotoStorage.limpiarTemporales(context)
                tempCameraPair = null
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                MascotaPhotoStorage.limpiarTemporales(context)
                tempCameraPair = null
            }
        }
    }

    val guardarAccion: () -> Unit = {
        val nClean = nombre.trim()
        val rClean = raza.trim()
        val pVal = pesoKgText.replace(',', '.').toDoubleOrNull() ?: 0.0
        val espFinal = if (especieSeleccionada == "Otra") especiePersonalizada.trim() else especieSeleccionada

        if (nClean.isEmpty()) {
            errorMensaje = "El nombre es obligatorio."
        } else if (especieSeleccionada == "Otra" && espFinal.isEmpty()) {
            errorMensaje = "Indicá qué especie es."
        } else if (rClean.isEmpty()) {
            errorMensaje = "La raza es obligatoria."
        } else if (fechaNacimientoIso.isBlank() && edadAproximadaText.toIntOrNull() == null) {
            errorMensaje = "Completá la fecha de nacimiento o una edad aproximada válida."
        } else if (pVal <= 0.0) {
            errorMensaje = "Ingresá un peso válido mayor a 0."
        } else {
            errorMensaje = null
            val edadInt = edadAproximadaText.toIntOrNull()
            val fNac = fechaNacimientoIso.trim().ifEmpty { null }
            val racionesInt = racionesPorDiaText.toIntOrNull()

            val tipoDietaFinal = if (tipoDietaSeleccionada == "Otra") otroTipoDieta.trim().ifEmpty { "Otra" } else tipoDietaSeleccionada
            val historialCelosStr = CeloUtils.serializarHistorial(historialCelosFechas).ifBlank { null }

            val mascotaActualizada = mascotaInicial.copy(
                nombre = nClean,
                especie = espFinal,
                raza = rClean,
                sexo = sexoSeleccionado,
                fechaNacimientoIso = fNac,
                edadAproximadaAnios = edadInt,
                talla = tallaSeleccionada,
                pesoKg = pVal,
                color = color.trim().ifEmpty { null },
                castrado = castrado,
                numeroMicrochip = numeroMicrochip.trim().ifEmpty { null },
                alergias = alergias.trim().ifEmpty { null },
                medicacionHabitual = medicacionHabitual.trim().ifEmpty { null },
                veterinario = veterinario.trim().ifEmpty { null },
                observaciones = observaciones.trim().ifEmpty { null },
                fotoUri = fotoUriString,
                tipoDieta = tipoDietaFinal,
                marcaAlimento = marcaAlimento.trim().ifEmpty { null },
                cantidadPorRacion = cantidadPorRacion.trim().ifEmpty { null },
                racionesPorDia = racionesInt,
                horariosAlimentacion = horariosAlimentacion.trim().ifEmpty { null },
                restriccionesAlimentarias = restriccionesAlimentarias.trim().ifEmpty { null },
                urgencias24h = urgencias24h.trim().ifEmpty { null },
                contactoPrincipal = contactoPrincipal.trim().ifEmpty { null },
                contactoSecundario = contactoSecundario.trim().ifEmpty { null },
                contactoAlternativo = contactoAlternativo.trim().ifEmpty { null },
                vacunasInfo = vacunasInfo.trim().ifEmpty { null },
                desparasitacionInfo = desparasitacionInfo.trim().ifEmpty { null },
                enfermedadesCronicas = enfermedadesCronicas.trim().ifEmpty { null },
                historialCelos = historialCelosStr
            )
            onGuardarCambios(mascotaActualizada)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar perfil de mascota",
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
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Guardar cambios",
                            style = MaterialTheme.typography.labelMedium,
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
            // FOTO & ZONA FOTOGRÁFICA
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!fotoUriString.isNullOrBlank()) {
                                FotoPerroAvatar(
                                    fotoUriString = fotoUriString,
                                    nombrePerro = if (nombre.isNotBlank()) nombre else null,
                                    reqWidth = 1200,
                                    reqHeight = 675,
                                    shape = RoundedCornerShape(20.dp),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🐾",
                                        style = MaterialTheme.typography.headlineLarge,
                                        modifier = Modifier.clearAndSetSemantics {}
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Agregá una foto de tu mascota",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                val pair = MascotaPhotoStorage.crearTempCamaraUri(context)
                                if (pair != null) {
                                    tempCameraPair = pair
                                    camaraLauncher.launch(pair.first)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .semantics { contentDescription = accTomarFotoDesc }
                        ) {
                            Text(stringResource(R.string.btn_tomar_foto))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedButton(
                            onClick = {
                                galeriaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .semantics { contentDescription = accElegirGaleriaDesc }
                        ) {
                            Text(stringResource(R.string.btn_elegir_galeria))
                        }
                    }
                }
            }

            // SECCIÓN 1: IDENTIFICACIÓN
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. Identificación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Especie Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = especieSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Especie *") },
                            trailingIcon = { Text("▼") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { menuEspecieExpanded = true }
                        )
                        DropdownMenu(
                            expanded = menuEspecieExpanded,
                            onDismissRequest = { menuEspecieExpanded = false }
                        ) {
                            ESPECIES_COMUNES.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        especieSeleccionada = opt
                                        menuEspecieExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (especieSeleccionada == "Otra") {
                        OutlinedTextField(
                            value = especiePersonalizada,
                            onValueChange = { especiePersonalizada = it },
                            label = { Text("¿Qué especie es? *") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Sexo *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SEXOS_MASCOTA.forEach { s ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clickable { sexoSeleccionado = s }
                            ) {
                                RadioButton(selected = (sexoSeleccionado == s), onClick = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(s)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = fechaNacimientoIso,
                            onValueChange = { fechaNacimientoIso = it },
                            label = { Text("F. Nac (AAAA-MM-DD)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = edadAproximadaText,
                            onValueChange = { edadAproximadaText = it },
                            label = { Text("Edad aprox (años)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = numeroMicrochip,
                        onValueChange = { numeroMicrochip = it },
                        label = { Text("Número de Microchip") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Señas particulares / Color") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Talla *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TALLAS_MASCOTA.forEach { t ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clickable { tallaSeleccionada = t }
                            ) {
                                RadioButton(selected = (tallaSeleccionada == t), onClick = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(t)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pesoKgText,
                        onValueChange = { pesoKgText = it },
                        label = { Text("Peso en kg *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable { castrado = !castrado }
                    ) {
                        Checkbox(checked = castrado, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Castrado / Esterilizado")
                    }

                    // SECCIÓN CONDICIONAL: REGISTRO DE CELO Y MENSTRUACIÓN
                    if (mostrarCeloSeccion) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🩸 Registro de Celo y Menstruación",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Registrá las fechas de inicio de celo para llevar un control preciso.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (historialCelosFechas.isEmpty()) {
                                    Text(
                                        text = "Sin fechas de celo registradas aún.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    historialCelosFechas.sortedDescending().forEach { fecha ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• Inicio: ${CeloUtils.formatearFecha(fecha)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TextButton(onClick = {
                                                historialCelosFechas = historialCelosFechas - fecha
                                            }) {
                                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = { mostrarDatePickerCelo = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+ Agregar fecha de celo")
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN 2: HISTORIAL MÉDICO
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. Historial médico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    OutlinedTextField(
                        value = vacunasInfo,
                        onValueChange = { vacunasInfo = it },
                        label = { Text("Vacunas (nombre, última aplicación, próxima)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = desparasitacionInfo,
                        onValueChange = { desparasitacionInfo = it },
                        label = { Text("Desparasitación (tipo y fechas)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = enfermedadesCronicas,
                        onValueChange = { enfermedadesCronicas = it },
                        label = { Text("Enfermedades crónicas") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = alergias,
                        onValueChange = { alergias = it },
                        label = { Text("Alergias") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medicacionHabitual,
                        onValueChange = { medicacionHabitual = it },
                        label = { Text("Medicamentos actuales (nombre, dosis, horario)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // SECCIÓN 3: ALIMENTACIÓN Y RUTINA
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. Alimentación y rutina",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    var menuDietaExp by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = tipoDietaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de alimentación") },
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
                            TIPOS_DIETA.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        tipoDietaSeleccionada = d
                                        menuDietaExp = false
                                    }
                                )
                            }
                        }
                    }

                    if (tipoDietaSeleccionada == "Otra") {
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

                    OutlinedTextField(
                        value = marcaAlimento,
                        onValueChange = { marcaAlimento = it },
                        label = { Text("Marca o tipo de alimento") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = cantidadPorRacion,
                            onValueChange = { input -> cantidadPorRacion = input.filter { it.isDigit() || it == '.' } },
                            label = { Text("Cant. por ración (ej. 120)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = racionesPorDiaText,
                            onValueChange = { racionesPorDiaText = it },
                            label = { Text("Raciones por día") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = horariosAlimentacion,
                        onValueChange = { horariosAlimentacion = it },
                        label = { Text("Horarios habituales") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restriccionesAlimentarias,
                        onValueChange = { restriccionesAlimentarias = it },
                        label = { Text("Restricciones o alimentos prohibidos") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones de rutina / Hábitos de baño") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // SECCIÓN 4: CLAN PET
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "4. Clan Pet (Contactos de apoyo)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    OutlinedTextField(
                        value = veterinario,
                        onValueChange = { veterinario = it },
                        label = { Text("Veterinario (nombre, clínica y teléfono)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = urgencias24h,
                        onValueChange = { urgencias24h = it },
                        label = { Text("Urgencias 24 h (nombre, dirección, tel)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contactoPrincipal,
                        onValueChange = { contactoPrincipal = it },
                        label = { Text("Clan Pet - Contacto principal (Nombre, rol y tel/email)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contactoSecundario,
                        onValueChange = { contactoSecundario = it },
                        label = { Text("Clan Pet - Contacto secundario") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contactoAlternativo,
                        onValueChange = { contactoAlternativo = it },
                        label = { Text("Clan Pet - Contacto alternativo") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (errorMensaje != null) {
                item {
                    Text(
                        text = errorMensaje!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        }
    }

    if (mostrarDatePickerCelo) {
        CeloDatePickerDialog(
            onDismiss = { mostrarDatePickerCelo = false },
            onDateSelected = { date ->
                if (!historialCelosFechas.contains(date)) {
                    historialCelosFechas = historialCelosFechas + date
                }
            }
        )
    }
}
