package com.carolinagandin.cuidadog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carolinagandin.cuidadog.R
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import com.carolinagandin.cuidadog.ui.components.AlimentacionCuidadoCard
import com.carolinagandin.cuidadog.ui.components.CuidadoVisualCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosCuidadosScreen(
    todosLosCuidados: List<CuidadoEntity>,
    onVolver: () -> Unit,
    onEliminarCuidado: (CuidadoEntity) -> Unit,
    onToggleRealizado: (CuidadoEntity) -> Unit = {},
    onMarcarTodosRealizados: (List<CuidadoEntity>) -> Unit = {},
    onEditarCuidado: (CuidadoEntity) -> Unit = {}
) {
    val hoyStr = remember { LocalDate.now().toString() }
    val accVolverDesc = stringResource(R.string.acc_volver)
    val listState = rememberLazyListState()

    var cuidadoAEliminar by remember { mutableStateOf<CuidadoEntity?>(null) }

    val cuidadosHoy = remember(todosLosCuidados, hoyStr) {
        todosLosCuidados.filter { it.fecha == hoyStr }
    }

    val cuidadosFuturos = remember(todosLosCuidados, hoyStr) {
        todosLosCuidados.filter { it.fecha > hoyStr }
            .sortedWith(compareBy<CuidadoEntity> { it.fecha }.thenBy { it.hora ?: "" })
    }

    val cuidadosPasados = remember(todosLosCuidados, hoyStr) {
        todosLosCuidados.filter { it.fecha < hoyStr }
            .sortedWith(compareByDescending<CuidadoEntity> { it.fecha }.thenByDescending { it.hora ?: "" })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.titulo_todos_cuidados),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = "Historial y próximos registros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        }
    ) { innerPadding ->
        if (todosLosCuidados.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_todos_cuidados),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 48.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sección Hoy
                if (cuidadosHoy.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.seccion_hoy),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    val (alimentacionHoy, otrosHoy) = cuidadosHoy.partition { it.tipo == "Alimentación" }
                    val alimentacionHoyAgrupada = alimentacionHoy.groupBy { it.titulo }

                    items(alimentacionHoyAgrupada.entries.toList(), key = { "todos_ali_hoy_${it.key}" }) { entry ->
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
                            },
                            accionEliminar = {
                                TextButton(
                                    onClick = { cuidadoAEliminar = entry.value.firstOrNull() },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_eliminar),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }

                    items(otrosHoy, key = { "todos_hoy_${it.id}" }) { cuidado ->
                        val accEliminarDesc = stringResource(R.string.acc_eliminar_cuidado, cuidado.titulo)
                        CuidadoVisualCard(
                            cuidado = cuidado,
                            mostrarFecha = false,
                            onToggleRealizado = { c -> onToggleRealizado(c) },
                            accionEditar = {
                                IconButton(onClick = { onEditarCuidado(cuidado) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            accionEliminar = {
                                TextButton(
                                    onClick = { cuidadoAEliminar = cuidado },
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        .semantics {
                                            contentDescription = accEliminarDesc
                                        }
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_eliminar),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }
                }

                // Sección Futuros
                if (cuidadosFuturos.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.seccion_proximos),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    val (alimentacionFuturos, otrosFuturos) = cuidadosFuturos.partition { it.tipo == "Alimentación" }
                    val alimentacionFuturosAgrupada = alimentacionFuturos.groupBy { it.fecha to it.titulo }

                    items(alimentacionFuturosAgrupada.entries.toList(), key = { "todos_ali_futuro_${it.key.first}_${it.key.second}" }) { entry ->
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
                            },
                            accionEliminar = {
                                TextButton(
                                    onClick = { cuidadoAEliminar = entry.value.firstOrNull() },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_eliminar),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }

                    items(otrosFuturos, key = { "todos_futuro_${it.id}" }) { cuidado ->
                        val accEliminarDesc = stringResource(R.string.acc_eliminar_cuidado, cuidado.titulo)
                        CuidadoVisualCard(
                            cuidado = cuidado,
                            mostrarFecha = true,
                            onToggleRealizado = { c -> onToggleRealizado(c) },
                            accionEditar = {
                                IconButton(onClick = { onEditarCuidado(cuidado) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            accionEliminar = {
                                TextButton(
                                    onClick = { cuidadoAEliminar = cuidado },
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        .semantics {
                                            contentDescription = accEliminarDesc
                                        }
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_eliminar),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }
                }

                // Sección Pasados
                if (cuidadosPasados.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.seccion_anteriores),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    val (alimentacionPasados, otrosPasados) = cuidadosPasados.partition { it.tipo == "Alimentación" }
                    val alimentacionPasadosAgrupada = alimentacionPasados.groupBy { it.fecha to it.titulo }

                    items(alimentacionPasadosAgrupada.entries.toList(), key = { "todos_ali_pasado_${it.key.first}_${it.key.second}" }) { entry ->
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
                            },
                            accionEliminar = {
                                TextButton(
                                    onClick = { cuidadoAEliminar = entry.value.firstOrNull() },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_eliminar),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }

                    items(otrosPasados, key = { "todos_pasado_${it.id}" }) { cuidado ->
                        val accEliminarDesc = stringResource(R.string.acc_eliminar_cuidado, cuidado.titulo)
                        CuidadoVisualCard(
                            cuidado = cuidado,
                            mostrarFecha = true,
                            onToggleRealizado = { c -> onToggleRealizado(c) },
                            accionEditar = {
                                IconButton(onClick = { onEditarCuidado(cuidado) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            accionEliminar = {
                                TextButton(
                                    onClick = { cuidadoAEliminar = cuidado },
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        .semantics {
                                            contentDescription = accEliminarDesc
                                        }
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_eliminar),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Diálogo de Confirmación de Eliminación
        cuidadoAEliminar?.let { cuidado ->
            AlertDialog(
                onDismissRequest = { cuidadoAEliminar = null },
                title = { Text(stringResource(R.string.dialog_eliminar_titulo)) },
                text = { Text(stringResource(R.string.dialog_eliminar_mensaje, cuidado.titulo)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEliminarCuidado(cuidado)
                            cuidadoAEliminar = null
                        },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_eliminar),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { cuidadoAEliminar = null },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }
    }
}
