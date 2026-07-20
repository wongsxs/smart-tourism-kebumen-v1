package com.yuudev.wisatakebumen.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuudev.wisatakebumen.BuildConfig

@Composable
fun ProfileMenu(
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = "Profile Menu",
            tint = Color.White
        )
    }

    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = 4.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Pengaturan") },
                leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = "Ikon Pengaturan") },
                onClick = {
                    expanded = false
                    onSettingsClick()
                }
            )
            
            DropdownMenuItem(
                text = { Text("Profil") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = "Ikon Profil") },
                onClick = {
                    expanded = false
                    onProfileClick()
                }
            )
            
            DropdownMenuItem(
                text = { Text("Tentang Aplikasi") },
                leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = "Ikon Tentang Aplikasi") },
                onClick = {
                    expanded = false
                    onAboutClick()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            DropdownMenuItem(
                text = { Text("Logout", color = Color(0xFFE53935), fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Ikon Logout", tint = Color(0xFFE53935)) },
                onClick = {
                    expanded = false
                    onLogoutClick()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }
}



