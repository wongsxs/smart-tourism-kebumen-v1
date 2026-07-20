package com.yuudev.wisatakebumen

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yuudev.wisatakebumen.manajer.SessionManager
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.screen.*
import com.yuudev.wisatakebumen.util.getUserLocation
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuudev.wisatakebumen.viewmodel.WisataViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()

        setContent {
            com.yuudev.wisatakebumen.ui.theme.WisataKebumenTheme {
                val context = LocalContext.current
                val vm: WisataViewModel = viewModel()
                val wisataList by vm.wisataList.collectAsState()
                
                var userLocation by remember { mutableStateOf<Location?>(null) }
                getUserLocation { userLocation = it }
                
                var selectedWisata by remember { mutableStateOf<Wisata?>(null) }
                var selectedBookingId by remember { mutableStateOf<String?>(null) }
                
                var isLogin by remember { mutableStateOf(SessionManager.isLogin(context)) }
                var isRegistering by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                
                // State Navigasi Utama (0: Home, 1: Favorit, 2: Tiket)
                var selectedTab by remember { mutableStateOf(0) }
                val roleUser = SessionManager.getRole(context)
                val isAdmin = roleUser == "admin"

                var backPressedTime by remember { mutableStateOf(0L) }

                // ðŸ”¥ DOUBLE BACK EXIT
                BackHandler(enabled = selectedWisata == null && selectedBookingId == null && !showSettings) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < 2000) {
                        (context as? Activity)?.finish()
                    } else {
                        backPressedTime = currentTime
                        Toast.makeText(context, "Tekan lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    }
                }

                if (!isLogin) {
                    if (isRegistering) {
                        RegisterScreen(onBack = { isRegistering = false })
                    } else {
                        LoginScreen(
                            onSuccess = { isLogin = SessionManager.isLogin(context) },
                            onRegister = { isRegistering = true }
                        )
                    }
                } else {
                    if (showSettings) {
                        SettingsScreen()
                        BackHandler { showSettings = false }
                    } else if (selectedWisata != null) {
                        WisataDetailScreen(
                            wisata = selectedWisata!!,
                            userLocation = userLocation,
                            onBack = { selectedWisata = null }
                        )
                    } else if (selectedBookingId != null) {
                        TicketDetailScreen(
                            bookingId = selectedBookingId!!,
                            onBack = { selectedBookingId = null }
                        )
                    } else if (isAdmin) {
                        AdminScreen(
                            wisataList = wisataList,
                            onItemClick = { selectedWisata = it },
                            onRefresh = { vm.refreshWisata(false) }
                        )
                    } else {
                        // Switch Screen berdasarkan Tab
                        when (selectedTab) {
                            0 -> HomeScreen(
                                onItemClick = { selectedWisata = it },
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                onSettingsClick = { showSettings = true },
                                onProfileClick = { Toast.makeText(context, "Fitur Profil segera hadir", Toast.LENGTH_SHORT).show() },
                                onAboutClick = { Toast.makeText(context, "Fitur Tentang segera hadir", Toast.LENGTH_SHORT).show() }
                            )
                            1 -> FavoriteScreen(
                                onItemClick = { selectedWisata = it },
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                            2 -> TiketSayaScreen(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                onDetailClick = { selectedBookingId = it }
                            )
                            3 -> TripPlannerScreen(
                                wisataList = wisataList,
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                onDestinationClick = { selectedWisata = it }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
        }
    }
}

fun uriToBase64(
    context: android.content.Context,
    uri: Uri
): String {
    val input = context.contentResolver.openInputStream(uri)
    val bytes = input?.readBytes() ?: ByteArray(0)
    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64"
}

