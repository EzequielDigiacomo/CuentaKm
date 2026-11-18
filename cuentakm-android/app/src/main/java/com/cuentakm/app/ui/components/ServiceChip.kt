package com.cuentakm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuentakm.app.domain.model.MaintenanceType

/**
 * Tono + ícono por servicio. Los colores son los mismos `--color-*` de
 * `cuentakm-web/src/styles/tokens.css`, para que la app no invente una paleta.
 */
data class ServiceVisuals(
    val accent: Color,
    val soft: Color,
    val icon: ImageVector,
)

fun MaintenanceType.visuals(): ServiceVisuals = when (this) {
    MaintenanceType.Oil ->
        ServiceVisuals(Color(0xFFB45309), Color(0xFFFDE7C9), Icons.Filled.Opacity)

    MaintenanceType.OilFilter ->
        ServiceVisuals(Color(0xFF9A3412), Color(0xFFFFEDE0), Icons.Filled.FilterAlt)

    MaintenanceType.FuelFilter ->
        ServiceVisuals(Color(0xFF0369A1), Color(0xFFE0F2FE), Icons.Filled.LocalGasStation)

    MaintenanceType.CabinFilter ->
        ServiceVisuals(Color(0xFF475569), Color(0xFFE2E8F0), Icons.Filled.Air)

    MaintenanceType.Tires ->
        ServiceVisuals(Color(0xFF334155), Color(0xFFE2E8F0), Icons.Filled.DonutLarge)

    MaintenanceType.Belt ->
        ServiceVisuals(Color(0xFFA16207), Color(0xFFFEF3C7), Icons.Filled.Settings)

    MaintenanceType.TransmissionFluid ->
        ServiceVisuals(Color(0xFF6D28D9), Color(0xFFEDE9FE), Icons.Filled.Build)

    MaintenanceType.Coolant ->
        ServiceVisuals(Color(0xFF0F766E), Color(0xFFCCFBF1), Icons.Filled.AcUnit)
}

/** Chip espejo de `.ck-chip` de la web: pill, borde 1dp y tono propio cuando está activo. */
@Composable
fun ServiceChip(
    type: MaintenanceType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visuals = type.visuals()
    val shape = RoundedCornerShape(percent = 50)

    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (selected) visuals.soft else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (selected) visuals.accent else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = visuals.icon,
            contentDescription = null,
            tint = if (selected) visuals.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) visuals.accent else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Borde punteado: Compose no lo trae de fábrica y la web lo usa para los bloques anidados. */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 16.dp,
    strokeWidth: Dp = 1.dp,
    dash: Dp = 6.dp,
    gap: Dp = 5.dp,
): Modifier = drawBehind {
    val stroke = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2f, stroke / 2f),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx())),
        ),
    )
}
