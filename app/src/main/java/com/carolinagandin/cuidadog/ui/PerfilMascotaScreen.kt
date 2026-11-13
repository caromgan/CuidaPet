package com.carolinagandin.cuidadog.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Icon
import com.carolinagandin.cuidadog.util.MapUtils
import com.carolinagandin.cuidadog.util.DateUtils
import com.carolinagandin.cuidadog.util.CeloUtils
import com.carolinagandin.cuidadog.ui.components.CeloDatePickerDialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.carolinagandin.cuidadog.ui.components.AlimentacionIcon
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carolinagandin.cuidadog.data.local.AlimentacionPruebaEntity
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import com.carolinagandin.cuidadog.data.local.PerroEntity
import com.carolinagandin.cuidadog.ui.components.FotoPerroAvatar
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilMascotaScreen(
    mascota: PerroEntity,
    cuidadosRealizados: List<CuidadoEntity> = emptyList(),
    ultimaPruebaAlimentacion: AlimentacionPruebaEntity? = null,
    onVolver: () -> Unit,
    onEditarClick: () -> Unit = {},
    onActualizarHistorialCelos: (PerroEntity, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var mostrarDialogoExportar by remember { mutableStateOf(false) }
    var modoExportacionPdf by remember { mutableStateOf(false) } // false = compartir texto, true = PDF

    var incluirHistorial by remember { mutableStateOf(true) }
    var filtroRangoDias by remember { mutableStateOf(30) } // 7, 30, 90, 365 (Todos)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Perfil de mascota",
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
            },
            actions = {
                TextButton(
                    onClick = {
                        modoExportacionPdf = false
                        mostrarDialogoExportar = true
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(text = "📤 Compartir", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = {
                        modoExportacionPdf = true
                        mostrarDialogoExportar = true
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(text = "📄 PDF", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = onEditarClick,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = "Editar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CABECERA CON FOTO Y DATOS PRINCIPALES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FotoPerroAvatar(
                            fotoUriString = mascota.fotoUri,
                            nombrePerro = mascota.nombre,
                            reqWidth = 256,
                            reqHeight = 256,
                            shape = CircleShape,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = mascota.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${mascota.especie} · ${mascota.raza.ifBlank { "Sin raza" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // SECCIÓN 1: IDENTIFICACIÓN
            item {
                PerfilSeccionCard(titulo = "1. Identificación") {
                    PerfilItem(label = "Nombre", valor = mascota.nombre)
                    PerfilItem(label = "Especie", valor = mascota.especie)
                    PerfilItem(label = "Raza", valor = mascota.raza)
                    PerfilItem(label = "Sexo", valor = mascota.sexo)
                    PerfilItem(label = "Fecha de nacimiento", valor = DateUtils.obtenerFechaNacimientoTexto(mascota.fechaNacimientoIso))
                    PerfilItem(label = "Edad", valor = DateUtils.obtenerEdadTexto(mascota.fechaNacimientoIso, mascota.edadAproximadaAnios))
                    PerfilItem(label = "Número de microchip", valor = mascota.numeroMicrochip)
                    PerfilItem(label = "Señas particulares / Color", valor = mascota.color)
                    PerfilItem(label = "Talla", valor = mascota.talla)
                    PerfilItem(label = "Peso", valor = if (mascota.pesoKg > 0.0) "${mascota.pesoKg} kg" else "Sin información")
                    PerfilItem(label = "Castrado / Esterilizado", valor = when (mascota.castrado) { true -> "Sí"; false -> "No"; null -> "Sin información" })
                }
            }

            // SECCIÓN CELO (Condicional: Hembra no castrada)
            if (mascota.sexo.equals("Hembra", ignoreCase = true) && mascota.castrado != true) {
                item {
                    var mostrarDialogoNuevoCelo by remember { mutableStateOf(false) }
                    val historialCelosList = remember(mascota.historialCelos) { CeloUtils.parsearHistorial(mascota.historialCelos) }
                    val proximoCelo = remember(mascota.historialCelos) { CeloUtils.calcularProximoCelo(mascota.historialCelos) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🩸 Seguimiento de Celo y Menstruación",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }

                            if (proximoCelo != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "📅 Próximo celo estimado:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = CeloUtils.formatearFecha(proximoCelo),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Historial de celos registrados:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (historialCelosList.isEmpty()) {
                                Text(
                                    text = "No hay registros anteriores.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                historialCelosList.forEach { fecha ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${CeloUtils.formatearFecha(fecha)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        TextButton(
                                            onClick = {
                                                val nuevaLista = historialCelosList.filter { it != fecha }
                                                val nuevoStr = CeloUtils.serializarHistorial(nuevaLista).ifBlank { null } ?: ""
                                                onActualizarHistorialCelos(mascota, nuevoStr)
                                            }
                                        ) {
                                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = { mostrarDialogoNuevoCelo = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+ Registrar nuevo celo")
                            }
                        }
                    }

                    if (mostrarDialogoNuevoCelo) {
                        CeloDatePickerDialog(
                            onDismiss = { mostrarDialogoNuevoCelo = false },
                            onDateSelected = { date ->
                                val nuevaLista = (historialCelosList + date).distinct()
                                val nuevoStr = CeloUtils.serializarHistorial(nuevaLista)
                                onActualizarHistorialCelos(mascota, nuevoStr)
                            }
                        )
                    }
                }
            }

            // SECCIÓN 2: SALUD ACTUAL
            item {
                PerfilSeccionCard(titulo = "2. Salud actual") {
                    PerfilItem(label = "Alergias", valor = mascota.alergias)
                    PerfilItem(label = "Enfermedades crónicas", valor = mascota.enfermedadesCronicas)
                    PerfilItem(label = "Medicamentos actuales", valor = mascota.medicacionHabitual)
                }
            }

            // SECCIÓN 3: ALIMENTACIÓN Y RUTINA
            item {
                PerfilSeccionCard(
                    titulo = "3. Alimentación y rutina",
                    icon = { AlimentacionIcon(size = 20.sp) }
                ) {
                    PerfilItem(label = "Tipo de alimentación", valor = mascota.tipoDieta)
                    PerfilItem(label = "Marca / Alimento", valor = mascota.marcaAlimento)
                    PerfilItem(label = "Cantidad por ración", valor = mascota.cantidadPorRacion)
                    PerfilItem(label = "Raciones por día", valor = mascota.racionesPorDia?.toString())
                    PerfilItem(label = "Horarios", valor = mascota.horariosAlimentacion)
                    PerfilItem(label = "Restricciones", valor = mascota.restriccionesAlimentarias)
                    PerfilItem(label = "Observaciones", valor = mascota.observaciones)
                }
            }

            // SECCIÓN 4: CLAN PET
            item {
                PerfilSeccionCard(titulo = "4. Clan Pet") {
                    PerfilItem(label = "Veterinario / Clínica", valor = mascota.veterinario)
                    PerfilItem(label = "Urgencias 24 h", valor = mascota.urgencias24h)
                    PerfilItem(label = "Contacto principal", valor = mascota.contactoPrincipal)
                    PerfilItem(label = "Contacto secundario", valor = mascota.contactoSecundario)
                    PerfilItem(label = "Contacto alternativo", valor = mascota.contactoAlternativo)

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { MapUtils.abrirVeterinariasGuardia(context) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚨 Buscar Veterinarias de Guardia 24hs", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECCIÓN 5: SEGUIMIENTOS
            item {
                PerfilSeccionCard(titulo = "5. Seguimientos") {
                    PerfilItem(label = "Vacunas", valor = mascota.vacunasInfo ?: "Seguimiento no configurado")
                    PerfilItem(label = "Desparasitación", valor = mascota.desparasitacionInfo ?: "Seguimiento no configurado")
                }
            }
        }
    }

    if (mostrarDialogoExportar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExportar = false },
            title = { Text(text = if (modoExportacionPdf) "Exportar perfil a PDF" else "Exportar perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Seleccioná el rango de historial de cuidados a incluir:")

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = incluirHistorial,
                            onCheckedChange = { incluirHistorial = it }
                        )
                        Text(text = "Incluir historial de cuidados")
                    }

                    if (incluirHistorial) {
                        Text(text = "Rango de días:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(7, 30, 90, 365).forEach { dias ->
                                val label = if (dias == 365) "1 año" else "$dias d"
                                OutlinedButton(
                                    onClick = { filtroRangoDias = dias },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (filtroRangoDias == dias) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 36.dp),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoExportar = false
                        if (modoExportacionPdf) {
                            exportarPerfilPdf(context, mascota, cuidadosRealizados.filter { incluirHistorial }, filtroRangoDias)
                        } else {
                            compartirPerfilTexto(context, mascota, cuidadosRealizados.filter { incluirHistorial }, filtroRangoDias)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(text = "Generar y compartir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoExportar = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
fun PerfilSeccionCard(
    titulo: String,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.invoke()
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )
            }
            content()
        }
    }
}

@Composable
fun PerfilItem(label: String, valor: String?) {
    val displayVal = if (valor.isNullOrBlank()) "Sin información" else valor
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = displayVal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun compartirPerfilTexto(context: Context, mascota: PerroEntity, cuidados: List<CuidadoEntity>, filtroDias: Int) {
    val sb = StringBuilder()
    sb.append("🐾 *Perfil de Mascota - cuidaPET* 🐾\n\n")
    sb.append("📌 *1. Identificación*\n")
    sb.append("• Nombre: ${mascota.nombre}\n")
    sb.append("• Especie: ${mascota.especie}\n")
    sb.append("• Raza: ${mascota.raza}\n")
    sb.append("• Sexo: ${mascota.sexo}\n")
    sb.append("• F. Nacimiento: ${DateUtils.obtenerFechaNacimientoTexto(mascota.fechaNacimientoIso)}\n")
    sb.append("• Edad: ${DateUtils.obtenerEdadTexto(mascota.fechaNacimientoIso, mascota.edadAproximadaAnios)}\n")
    if (!mascota.numeroMicrochip.isNullOrBlank()) sb.append("• Microchip: ${mascota.numeroMicrochip}\n")
    if (!mascota.color.isNullOrBlank()) sb.append("• Señas/Color: ${mascota.color}\n")
    sb.append("• Talla: ${mascota.talla} | Peso: ${mascota.pesoKg} kg\n")
    sb.append("• Castrado: ${when(mascota.castrado) { true -> "Sí"; false -> "No"; else -> "No especificado" }}\n\n")

    sb.append("💉 *2. Salud Actual*\n")
    sb.append("• Alergias: ${mascota.alergias ?: "Ninguna"}\n")
    sb.append("• Enf. Crónicas: ${mascota.enfermedadesCronicas ?: "Ninguna"}\n")
    sb.append("• Medicación: ${mascota.medicacionHabitual ?: "Ninguna"}\n\n")

    sb.append("🥣 *3. Alimentación y Rutina*\n")
    sb.append("• Dieta: ${mascota.tipoDieta ?: "No especificada"} (${mascota.marcaAlimento ?: ""})\n")
    sb.append("• Raciones: ${mascota.cantidadPorRacion ?: "-"} (${mascota.racionesPorDia ?: 0}x/día)\n\n")

    sb.append("🆘 *4. Clan Pet (Contactos)*\n")
    sb.append("• Vet: ${mascota.veterinario ?: "No especificado"}\n")
    sb.append("• Urgencias 24h: ${mascota.urgencias24h ?: "No especificado"}\n")
    sb.append("• Principal: ${mascota.contactoPrincipal ?: "No especificado"}\n\n")

    if (mascota.sexo.equals("Hembra", true) && mascota.castrado != true) {
        val listaCelos = CeloUtils.parsearHistorial(mascota.historialCelos)
        val proxCelo = CeloUtils.calcularProximoCelo(mascota.historialCelos)
        if (listaCelos.isNotEmpty() || proxCelo != null) {
            sb.append("🩸 *Seguimiento de Celo*\n")
            if (proxCelo != null) {
                sb.append("• Próximo celo estimado: ${CeloUtils.formatearFecha(proxCelo)}\n")
            }
            if (listaCelos.isNotEmpty()) {
                sb.append("• Historial: ${listaCelos.joinToString(", ") { CeloUtils.formatearFecha(it) }}\n")
            }
            sb.append("\n")
        }
    }

    if (cuidados.isNotEmpty()) {
        val limiteFecha = LocalDate.now().minusDays(filtroDias.toLong())
        val filtrados = cuidados.filter {
            try { LocalDate.parse(it.fecha).isAfter(limiteFecha) || LocalDate.parse(it.fecha).isEqual(limiteFecha) } catch (_: Exception) { true }
        }
        if (filtrados.isNotEmpty()) {
            sb.append("📋 *Historial de Cuidados (últimos $filtroDias días):*\n")
            filtrados.forEach { c ->
                sb.append("• [${c.fecha}] ${c.tipo}: ${c.titulo} ${if (c.realizado) "✓" else ""}\n")
            }
        }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartir perfil"))
}

private fun exportarPerfilPdf(context: Context, mascota: PerroEntity, cuidados: List<CuidadoEntity>, filtroDias: Int) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        var y = 40f
        paint.isFakeBoldText = true
        canvas.drawText("PERFIL DE MASCOTA - CUIDAPET", 40f, y, paint)
        y += 30f
        paint.isFakeBoldText = false
        canvas.drawText("Nombre: ${mascota.nombre}", 40f, y, paint)
        y += 20f
        canvas.drawText("Especie: ${mascota.especie} | Raza: ${mascota.raza} | Sexo: ${mascota.sexo}", 40f, y, paint)
        y += 20f
        canvas.drawText("Peso: ${mascota.pesoKg} kg | Talla: ${mascota.talla}", 40f, y, paint)
        y += 30f

        paint.isFakeBoldText = true
        canvas.drawText("Salud y Alimentación:", 40f, y, paint)
        y += 20f
        paint.isFakeBoldText = false
        canvas.drawText("Alergias: ${mascota.alergias ?: "Ninguna"}", 40f, y, paint)
        y += 20f
        canvas.drawText("Dieta: ${mascota.tipoDieta ?: "No especificada"}", 40f, y, paint)
        y += 30f

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "perfil_${mascota.nombre}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Ver PDF de mascota"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
