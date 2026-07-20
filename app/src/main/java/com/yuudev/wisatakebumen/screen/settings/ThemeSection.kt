package com.yuudev.wisatakebumen.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuudev.wisatakebumen.viewmodel.ThemeOption

@Composable
fun ThemeSection(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit
) {
    val appBlue = androidx.compose.material3.MaterialTheme.colorScheme.primary

    SettingsItem {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = appBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tema",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            ThemeOption.values().forEach { themeOption ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeSelected(themeOption) }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = currentTheme == themeOption,
                        onClick = { onThemeSelected(themeOption) },
                        colors = RadioButtonDefaults.colors(selectedColor = appBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = themeOption.title,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}



