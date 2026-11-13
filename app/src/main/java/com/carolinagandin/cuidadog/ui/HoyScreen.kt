package com.carolinagandin.cuidadog.ui

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import com.carolinagandin.cuidadog.util.MapUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carolinagandin.cuidadog.R
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import com.carolinagandin.cuidadog.data.local.PerroEntity
import com.carolinagandin.cuidadog.ui.components.AlimentacionCuidadoCard
import com.carolinagandin.cuidadog.ui.components.CuidadoVisualCard
import com.carolinagandin.cuidadog.ui.components.DraggableFloatingActionButton
import com.carolinagandin.cuidadog.ui.components.FotoPerroAvatar
import com.carolinagandin.cuidadog.util.MascotaPhotoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("NewApi")
@Composable
fun HoyScreen(
    nombrePerro: String,
    fotoUri: String? = null,
    listaMascotas: List<PerroEntity> = emptyList(),
    mascotaActiva: PerroEntity? = null,
    onSeleccionarMascota: (Long) -> Unit = {},
    onAgregarMascotaClick: () -> Unit = {},
    cuidadosHoy: List<CuidadoEntity> = emptyList(),
    cuidadosProximos10Dias: List<CuidadoEntity> = emptyList(),
    onAgregarCuidadoClick: () -> Unit = {},
    onVerTodosCuidadosClick: () -> Unit = {},
    onCambiarFotoPerro: (String?) -> Unit = {},
    onToggleRealizado: (CuidadoEntity) -> Unit = {},
    onMarcarTodosRealizados: (List<CuidadoEntity>) -> Unit = {},
    onEditarCuidado: (CuidadoEntity) -> Unit = {}
) {
    val context = LocalContext.current

    var mostrarDialogoFoto by remember { mutableStateOf(false) }
    var tempCameraPair by remember { mutableStateOf<Pair<Uri, File>?>(null) }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val savedUri = MascotaPhotoStorage.guardarFotoPermanente(context, uri)
                if (savedUri != null) {
                    onCambiarFotoPerro(savedUri)
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
                    onCambiarFotoPerro(savedUri)
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

    val accPantallaHoyDesc = stringResource(R.string.acc_pantalla_hoy, nombrePerro)
    val accAgregarCuidadoDesc = stringResource(R.string.acc_agregar_cuidado)
    val btnCambiarFotoStr = stringResource(R.string.btn_cambiar_foto, nombrePerro)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.semantics {
            contentDescription = accPantallaHoyDesc
        },
        floatingActionButton = {
            DraggableFloatingActionButton(
                onClick = onAgregarCuidadoClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .semantics {
                        contentDescription = accAgregarCuidadoDesc
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Agregar nuevo cuidado", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
                bottom = 108.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SELECTOR COMPACTO DE MASCOTAS
            if (listaMascotas.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listaMascotas.forEach { mascota ->
                            val esActiva = mascotaActiva?.id == mascota.id
                            val bgCard = if (esActiva) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            val textColor = if (esActiva) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onSeleccionarMascota(mascota.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = bgCard),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (esActiva) 2.dp else 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FotoPerroAvatar(
                                        fotoUriString = mascota.fotoUri,
                                        nombrePerro = mascota.nombre,
                                        reqWidth = 120,
                                        reqHeight = 120,
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = mascota.nombre,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = mascota.especie,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onAgregarMascotaClick,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("+ Agregar mascota", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // BANNER EMERGENCIA VETERINARIAS 24HS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { MapUtils.abrirVeterinariasGuardia(context) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚨 ¿Emergencia? Buscar Guardia 24 hs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Encontrá clínicas veterinarias abiertas cerca de tu ubicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // 1. HERO INTEGRADO
            item {
                val tieneFoto = !fotoUri.isNullOrBlank()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (tieneFoto) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Column {
                                    FotoPerroAvatar(
                                        fotoUriString = fotoUri,
                                        nombrePerro = nombrePerro,
                                        reqWidth = 1200,
                                        reqHeight = 675,
                                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = stringResource(R.string.acc_foto_perro, nombrePerro),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                    )
                                    Spacer(modifier = Modifier.height(28.dp))
                                }

                                FotoPerroAvatar(
                                    fotoUriString = fotoUri,
                                    nombrePerro = null,
                                    reqWidth = 256,
                                    reqHeight = 256,
                                    shape = CircleShape,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .offset(x = 16.dp, y = (0).dp)
                                        .size(60.dp)
                                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🐶",
                                        fontSize = 36.sp,
                                        modifier = Modifier.clearAndSetSemantics {}
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Agregá una foto de $nombrePerro",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Así vas a personalizar sus cuidados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // SALUDO Y ACCIÓN FOTOGRÁFICA
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hoy — $nombrePerro 🐾",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.semantics { heading() }
                                )
                                Text(
                                    text = "Organicemos sus cuidados cotidianos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { mostrarDialogoFoto = true },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text(
                                        text = if (tieneFoto) "Cambiar foto" else "Agregar foto",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. ENCABEZADO DE SECCIÓN CUIDADOS DE HOY + NAVEGACIÓN SECUNDARIA "VER TODOS"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.seccion_cuidados_hoy),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { heading() }
                        )
                    }

                    TextButton(
                        onClick = onVerTodosCuidadosClick,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Ver todos ›",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (cuidadosHoy.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.empty_cuidados_hoy),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onAgregarCuidadoClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_agregar_primer_cuidado),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                val (alimentacionHoy, otrosHoy) = cuidadosHoy.partition { it.tipo == "Alimentación" }
                val alimentacionHoyAgrupada = alimentacionHoy.groupBy { it.titulo }

                items(alimentacionHoyAgrupada.entries.toList(), key = { "ali_hoy_${it.key}" }) { entry ->
                    val tituloGrupo = entry.key.removePrefix("Alimentación:").trim()
                    AlimentacionCuidadoCard(
                        tituloGrupo = tituloGrupo,
                        cuidadosDelDia = entry.value,
                        onToggleRealizado = { c -> onToggleRealizado(c) },
                        onRealizarTodo = { todos -> onMarcarTodosRealizados(todos) },
                        accionEditar = {
                            IconButton(onClick = { onEditarCuidado(entry.value.first()) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }

                items(otrosHoy, key = { "hoy_${it.id}" }) { cuidado ->
                    CuidadoVisualCard(
                        cuidado = cuidado,
                        mostrarFecha = false,
                        onToggleRealizado = { c -> onToggleRealizado(c) },
                        accionEditar = {
                            IconButton(onClick = { onEditarCuidado(cuidado) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }

            // 3. ENCABEZADO DE SECCIÓN PRÓXIMOS 10 DÍAS
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.seccion_proximos_10_dias),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }

            if (cuidadosProximos10Dias.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.empty_proximos_10_dias),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val (alimentacionProx, otrosProx) = cuidadosProximos10Dias.partition { it.tipo == "Alimentación" }
                val alimentacionProxAgrupada = alimentacionProx.groupBy { it.fecha to it.titulo }

                items(alimentacionProxAgrupada.entries.toList(), key = { "ali_prox_${it.key.first}_${it.key.second}" }) { entry ->
                    val (fecha, titulo) = entry.key
                    val tituloGrupo = "$fecha — ${titulo.removePrefix("Alimentación:").trim()}"
                    AlimentacionCuidadoCard(
                        tituloGrupo = tituloGrupo,
                        cuidadosDelDia = entry.value,
                        onToggleRealizado = { c -> onToggleRealizado(c) },
                        onRealizarTodo = { todos -> onMarcarTodosRealizados(todos) },
                        accionEditar = {
                            IconButton(onClick = { onEditarCuidado(entry.value.first()) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }

                items(otrosProx, key = { "proximo_${it.id}" }) { cuidado ->
                    CuidadoVisualCard(
                        cuidado = cuidado,
                        mostrarFecha = true,
                        onToggleRealizado = { c -> onToggleRealizado(c) },
                        accionEditar = {
                            IconButton(onClick = { onEditarCuidado(cuidado) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }

        // Diálogo para Cambiar Foto
        if (mostrarDialogoFoto) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoFoto = false },
                title = { Text(btnCambiarFotoStr) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                mostrarDialogoFoto = false
                                val pair = MascotaPhotoStorage.crearTempCamaraUri(context)
                                if (pair != null) {
                                    tempCameraPair = pair
                                    camaraLauncher.launch(pair.first)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text(stringResource(R.string.btn_tomar_foto))
                        }

                        OutlinedButton(
                            onClick = {
                                mostrarDialogoFoto = false
                                galeriaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text(stringResource(R.string.btn_elegir_galeria))
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoFoto = false },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }
    }
}
