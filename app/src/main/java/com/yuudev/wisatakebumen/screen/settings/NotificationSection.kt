package com.yuudev.wisatakebumen.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationSection(
    tripNotificationEnabled: Boolean,
    weatherNotificationEnabled: Boolean,
    onTripNotificationChange: (Boolean) -> Unit,
    onWeatherNotificationChange: (Boolean) -> Unit
) {
    val appBlue = androidx.compose.material3.MaterialTheme.colorScheme.primary

    SettingsItem {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null,
                tint = appBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notifikasi",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifikasi Perjalanan", fontSize = 14.sp)
            Switch(
                checked = tripNotificationEnabled,
                onCheckedChange = onTripNotificationChange,
                colors = SwitchDefaults.colors(checkedTrackColor = appBlue)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifikasi Cuaca", fontSize = 14.sp)
            Switch(
                checked = weatherNotificationEnabled,
                onCheckedChange = onWeatherNotificationChange,
                colors = SwitchDefaults.colors(checkedTrackColor = appBlue)
            )
        }
    }
}



