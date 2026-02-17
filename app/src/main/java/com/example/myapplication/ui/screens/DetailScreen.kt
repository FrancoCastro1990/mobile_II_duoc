package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.components.EstadoBadge
import com.example.myapplication.ui.components.formatDate
import com.example.myapplication.ui.components.getEstadoInfo
import com.example.myapplication.viewmodel.SolicitudViewModel
import com.example.myapplication.viewmodel.UiEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    solicitudId: Long,
    viewModel: SolicitudViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToTecnicos: (String) -> Unit = {}
) {
    val solicitud by viewModel.selectedSolicitud.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEstadoBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val backButtonDescription = stringResource(R.string.btn_volver)
    val editButtonDescription = stringResource(R.string.btn_editar)
    val deleteButtonDescription = stringResource(R.string.btn_eliminar)

    LaunchedEffect(solicitudId) {
        viewModel.loadSolicitudDetail(solicitudId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.NavigateBack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_eliminar_titulo),
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Text(stringResource(R.string.dialog_eliminar_mensaje))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSolicitud(solicitudId)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_eliminar))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }

    if (showEstadoBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEstadoBottomSheet = false },
            sheetState = sheetState
        ) {
            EstadoBottomSheetContent(
                currentEstado = solicitud?.estado ?: "",
                onEstadoSelected = { nuevoEstado ->
                    viewModel.updateEstado(solicitudId, nuevoEstado)
                    scope.launch {
                        sheetState.hide()
                        showEstadoBottomSheet = false
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_detalle_solicitud),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = backButtonDescription }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(solicitudId) },
                        modifier = Modifier.semantics { contentDescription = editButtonDescription }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.semantics { contentDescription = deleteButtonDescription }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        solicitud?.let { sol ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tipo de servicio card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (sol.tipoServicio.lowercase()) {
                            "gasfiteria" -> Icons.Default.Build
                            "electricidad" -> Icons.Default.Settings
                            "electrodomesticos" -> Icons.Default.Home
                            else -> Icons.Default.Build
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = when (sol.tipoServicio.lowercase()) {
                                    "gasfiteria" -> stringResource(R.string.tipo_gasfiteria)
                                    "electricidad" -> stringResource(R.string.tipo_electricidad)
                                    "electrodomesticos" -> stringResource(R.string.tipo_electrodomesticos)
                                    else -> sol.tipoServicio
                                },
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { heading() }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            EstadoBadge(
                                estado = sol.estado,
                                estadoInfo = getEstadoInfo(sol.estado)
                            )
                        }
                    }
                }

                // Datos del cliente
                Text(
                    text = stringResource(R.string.section_datos_cliente),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailItem(
                            label = stringResource(R.string.label_nombre_cliente),
                            value = sol.nombreCliente
                        )
                        DetailItem(
                            label = stringResource(R.string.label_telefono),
                            value = sol.telefono
                        )
                        DetailItem(
                            label = stringResource(R.string.label_direccion),
                            value = sol.direccion
                        )
                    }
                }

                // Descripcion
                Text(
                    text = stringResource(R.string.section_descripcion),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = sol.descripcion.ifBlank { stringResource(R.string.sin_descripcion) },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Fecha
                Text(
                    text = stringResource(R.string.label_fecha_solicitud),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = formatDate(sol.fechaSolicitud),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Boton cambiar estado
                Button(
                    onClick = { showEstadoBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.btn_cambiar_estado))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Boton ver tecnicos disponibles
                Button(
                    onClick = { onNavigateToTecnicos(sol.tipoServicio) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.btn_ver_tecnicos))
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EstadoBottomSheetContent(
    currentEstado: String,
    onEstadoSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.bottomsheet_titulo_estado),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .semantics { heading() }
        )

        EstadoOption(
            estado = "pendiente",
            nombre = stringResource(R.string.estado_pendiente),
            descripcion = stringResource(R.string.estado_pendiente_desc),
            isSelected = currentEstado == "pendiente",
            onSelect = { onEstadoSelected("pendiente") }
        )

        EstadoOption(
            estado = "en_proceso",
            nombre = stringResource(R.string.estado_en_proceso),
            descripcion = stringResource(R.string.estado_en_proceso_desc),
            isSelected = currentEstado == "en_proceso",
            onSelect = { onEstadoSelected("en_proceso") }
        )

        EstadoOption(
            estado = "completado",
            nombre = stringResource(R.string.estado_completado),
            descripcion = stringResource(R.string.estado_completado_desc),
            isSelected = currentEstado == "completado",
            onSelect = { onEstadoSelected("completado") }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EstadoOption(
    estado: String,
    nombre: String,
    descripcion: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val estadoInfo = getEstadoInfo(estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$nombre: $descripcion" },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                estadoInfo.color
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
