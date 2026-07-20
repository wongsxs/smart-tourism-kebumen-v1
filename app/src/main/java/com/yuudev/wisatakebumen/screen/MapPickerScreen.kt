package com.yuudev.wisatakebumen.screen

import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.yuudev.wisatakebumen.model.LocationResult
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLocation: LatLng? = null,
    onLocationSelected: (LocationResult) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Default location: Kebumen Center
    val kebumenCenter = LatLng(-7.6706, 109.6625)
    val startLocation = if (initialLocation != null && initialLocation.latitude != 0.0) initialLocation else kebumenCenter
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, 15f)
    }
    
    var selectedLocation by remember { mutableStateOf<LatLng?>(if (initialLocation?.latitude != 0.0) initialLocation else null) }
    var addressText by remember { mutableStateOf("") }

    // Reverse Geocoding function
    fun updateAddress(latLng: LatLng) {
        scope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addressText = addresses[0].getAddressLine(0) ?: "Alamat tidak ditemukan"
                } else {
                    addressText = "Alamat tidak ditemukan"
                }
            } catch (e: Exception) {
                addressText = "Gagal mengambil alamat"
            }
        }
    }

    // Initial address update if editing
    LaunchedEffect(initialLocation) {
        if (initialLocation != null && initialLocation.latitude != 0.0) {
            updateAddress(initialLocation)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Lokasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val userLatLng = LatLng(it.latitude, it.longitude)
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                                    )
                                    selectedLocation = userLatLng
                                    updateAddress(userLatLng)
                                }
                            }
                        }
                    } catch (e: SecurityException) {
                        // Handle permission not granted
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = "Lokasi Saya", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    updateAddress(latLng)
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLng(latLng)
                        )
                    }
                }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Lokasi Terpilih"
                    )
                }
            }

            // Bottom info and action
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedLocation != null) {
                    Text(
                        text = "Koordinat: ${"%.5f".format(selectedLocation!!.latitude)}, ${"%.5f".format(selectedLocation!!.longitude)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (addressText.isNotEmpty()) {
                        Text(
                            text = addressText,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
                    Button(
                        onClick = {
                            selectedLocation?.let {
                                onLocationSelected(
                                    LocationResult(
                                        it.latitude,
                                        it.longitude,
                                        addressText
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Gunakan Lokasi Ini")
                    }
                } else {
                    Text(
                        text = "Silakan ketuk peta untuk memilih lokasi",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


