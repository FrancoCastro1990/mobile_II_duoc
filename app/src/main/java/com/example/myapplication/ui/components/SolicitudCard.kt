package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.SolicitudEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SolicitudCard(
    solicitud: SolicitudEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tipoServicioInfo = getTipoServicioInfo(solicitud.tipoServicio)
    val estadoInfo = getEstadoInfo(solicitud.estado)
    val fechaFormateada = formatDate(solicitud.fechaSolicitud)

    val cardDescription = stringResource(
        R.string.card_description,
        tipoServicioInfo.nombre,
        solicitud.nombreCliente,
        estadoInfo.nombre
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = cardDescription },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tipoServicioInfo.icon,
                contentDescription = tipoServicioInfo.nombre,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tipoServicioInfo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = solicitud.nombreCliente,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            EstadoBadge(estado = solicitud.estado, estadoInfo = estadoInfo)
        }
    }
}

@Composable
fun EstadoBadge(
    estado: String,
    estadoInfo: EstadoInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics {
            contentDescription = "Estado: ${estadoInfo.nombre}"
        },
        color = estadoInfo.color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = estadoInfo.nombre,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = estadoInfo.textColor
        )
    }
}

data class TipoServicioInfo(
    val nombre: String,
    val icon: ImageVector
)

data class EstadoInfo(
    val nombre: String,
    val color: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color
)

@Composable
fun getTipoServicioInfo(tipo: String): TipoServicioInfo {
    return when (tipo.lowercase()) {
        "gasfiteria" -> TipoServicioInfo(
            nombre = stringResource(R.string.tipo_gasfiteria),
            icon = Icons.Default.Build
        )
        "electricidad" -> TipoServicioInfo(
            nombre = stringResource(R.string.tipo_electricidad),
            icon = Icons.Default.Settings
        )
        "electrodomesticos" -> TipoServicioInfo(
            nombre = stringResource(R.string.tipo_electrodomesticos),
            icon = Icons.Default.Home
        )
        else -> TipoServicioInfo(
            nombre = tipo,
            icon = Icons.Default.Build
        )
    }
}

@Composable
fun getEstadoInfo(estado: String): EstadoInfo {
    return when (estado.lowercase()) {
        "pendiente" -> EstadoInfo(
            nombre = stringResource(R.string.estado_pendiente),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        "en_proceso" -> EstadoInfo(
            nombre = stringResource(R.string.estado_en_proceso),
            color = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        "completado" -> EstadoInfo(
            nombre = stringResource(R.string.estado_completado),
            color = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> EstadoInfo(
            nombre = estado,
            color = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
