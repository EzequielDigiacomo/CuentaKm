package com.cuentakm.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuentakm.app.R
import com.cuentakm.app.domain.model.MaintenanceStatus
import com.cuentakm.app.ui.theme.StatusOk
import com.cuentakm.app.ui.theme.StatusOkSoft
import com.cuentakm.app.ui.theme.StatusOverdue
import com.cuentakm.app.ui.theme.StatusOverdueSoft
import com.cuentakm.app.ui.theme.StatusUnknown
import com.cuentakm.app.ui.theme.StatusUnknownSoft
import com.cuentakm.app.ui.theme.StatusUpcoming
import com.cuentakm.app.ui.theme.StatusUpcomingSoft
import com.cuentakm.app.ui.theme.Teal

@Composable
fun StatusBadge(status: MaintenanceStatus, modifier: Modifier = Modifier) {
    val (foreground, background) = when (status) {
        MaintenanceStatus.Ok -> StatusOk to StatusOkSoft
        MaintenanceStatus.Upcoming -> StatusUpcoming to StatusUpcomingSoft
        MaintenanceStatus.Overdue -> StatusOverdue to StatusOverdueSoft
        MaintenanceStatus.Unknown -> StatusUnknown to StatusUnknownSoft
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = foreground,
        )
    }
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Int = 72) {
    Image(
        painter = painterResource(id = R.drawable.brand_icon),
        contentDescription = "CuentaKm",
        modifier = modifier.size(size.dp),
    )
}

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StatusOverdueSoft,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = StatusOverdue,
                modifier = Modifier.weight(1f),
            )
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar aviso",
                        tint = StatusOverdue,
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingBox(text: String = "Cargando…", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = Teal)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
