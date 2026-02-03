package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.viewmodel.SolicitudViewModel
import com.example.myapplication.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: SolicitudViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current
    var showTipoServicioDialog by remember { mutableStateOf(false) }

    val backButtonDescription = stringResource(R.string.btn_volver)

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

    if (showTipoServicioDialog) {
        TipoServicioDialog(
            onDismiss = { showTipoServicioDialog = false },
            onSelect = { tipo ->
                viewModel.updateTipoServicio(tipo)
                showTipoServicioDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditing) {
                            stringResource(R.string.title_editar_solicitud)
                        } else {
                            stringResource(R.string.title_nueva_solicitud)
                        },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_tipo_servicio),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )

            TipoServicioSelector(
                selectedTipo = formState.tipoServicio,
                onClick = { showTipoServicioDialog = true }
            )

            Text(
                text = stringResource(R.string.section_datos_cliente),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )

            OutlinedTextField(
                value = formState.nombreCliente,
                onValueChange = { viewModel.updateNombreCliente(it) },
                label = { Text(stringResource(R.string.label_nombre_cliente)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.telefono,
                onValueChange = { viewModel.updateTelefono(it) },
                label = { Text(stringResource(R.string.label_telefono)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.direccion,
                onValueChange = { viewModel.updateDireccion(it) },
                label = { Text(stringResource(R.string.label_direccion)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = stringResource(R.string.section_descripcion),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )

            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { viewModel.updateDescripcion(it) },
                label = { Text(stringResource(R.string.label_descripcion)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveSolicitud() },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (formState.isEditing) {
                            stringResource(R.string.btn_actualizar)
                        } else {
                            stringResource(R.string.btn_guardar)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TipoServicioSelector(
    selectedTipo: String,
    onClick: () -> Unit
) {
    val tipoTexto = when (selectedTipo.lowercase()) {
        "gasfiteria" -> stringResource(R.string.tipo_gasfiteria)
        "electricidad" -> stringResource(R.string.tipo_electricidad)
        "electrodomesticos" -> stringResource(R.string.tipo_electrodomesticos)
        else -> stringResource(R.string.seleccionar_tipo_servicio)
    }

    val selectorDescription = stringResource(R.string.selector_tipo_servicio_description)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = selectorDescription },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTipo.isNotBlank()) {
                val icon = when (selectedTipo.lowercase()) {
                    "gasfiteria" -> Icons.Default.Build
                    "electricidad" -> Icons.Default.Settings
                    "electrodomesticos" -> Icons.Default.Home
                    else -> Icons.Default.Build
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(8.dp))
            }
            Text(
                text = tipoTexto,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedTipo.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun TipoServicioDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_title_tipo_servicio),
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoServicioOption(
                    nombre = stringResource(R.string.tipo_gasfiteria),
                    descripcion = stringResource(R.string.tipo_gasfiteria_desc),
                    icon = Icons.Default.Build,
                    onClick = { onSelect("gasfiteria") }
                )
                TipoServicioOption(
                    nombre = stringResource(R.string.tipo_electricidad),
                    descripcion = stringResource(R.string.tipo_electricidad_desc),
                    icon = Icons.Default.Settings,
                    onClick = { onSelect("electricidad") }
                )
                TipoServicioOption(
                    nombre = stringResource(R.string.tipo_electrodomesticos),
                    descripcion = stringResource(R.string.tipo_electrodomesticos_desc),
                    icon = Icons.Default.Home,
                    onClick = { onSelect("electrodomesticos") }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancelar))
            }
        }
    )
}

@Composable
fun TipoServicioOption(
    nombre: String,
    descripcion: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$nombre: $descripcion" }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Column {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleSmall
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
