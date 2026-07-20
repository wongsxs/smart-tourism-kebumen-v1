package com.yuudev.wisatakebumen.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuudev.wisatakebumen.screen.settings.AboutSection
import com.yuudev.wisatakebumen.screen.settings.LocationSection
import com.yuudev.wisatakebumen.screen.settings.NotificationSection
import com.yuudev.wisatakebumen.screen.settings.PrivacySection
import com.yuudev.wisatakebumen.screen.settings.ThemeSection
import com.yuudev.wisatakebumen.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val themeState by viewModel.themeState.collectAsState()
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val tripNotificationEnabled by viewModel.tripNotificationEnabled.collectAsState()
    val weatherNotificationEnabled by viewModel.weatherNotificationEnabled.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚙️ Pengaturan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sesuaikan aplikasi sesuai preferensi Anda.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ThemeSection(
                currentTheme = themeState,
                onThemeSelected = { viewModel.setTheme(it) }
            )
        }

        item {
            LocationSection(
                locationEnabled = locationEnabled,
                onLocationEnabledChange = { viewModel.setLocationEnabled(it) }
            )
        }

        item {
            NotificationSection(
                tripNotificationEnabled = tripNotificationEnabled,
                weatherNotificationEnabled = weatherNotificationEnabled,
                onTripNotificationChange = { viewModel.setTripNotificationEnabled(it) },
                onWeatherNotificationChange = { viewModel.setWeatherNotificationEnabled(it) }
            )
        }

        item {
            PrivacySection(
                onPrivacyClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wisata-kebumen.app/privacy"))
                    context.startActivity(intent)
                }
            )
        }

        item {
            AboutSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
