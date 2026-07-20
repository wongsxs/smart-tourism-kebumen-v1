package com.yuudev.wisatakebumen.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuudev.wisatakebumen.component.TopTabNavigation
import com.yuudev.wisatakebumen.model.DestinationPlan
import com.yuudev.wisatakebumen.model.TripRequest
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.viewmodel.TripPlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    wisataList: List<Wisata>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onDestinationClick: (Wisata) -> Unit
) {
    val vm: TripPlannerViewModel = viewModel()
    val weatherViewModel: com.yuudev.wisatakebumen.viewmodel.WeatherViewModel = viewModel()
    val weatherStateMap by weatherViewModel.weatherData.collectAsState(initial = emptyMap())
    val context = LocalContext.current
    val primary = com.yuudev.wisatakebumen.ui.theme.AppColors.Navy
    val appBlue = androidx.compose.material3.MaterialTheme.colorScheme.primary

    var budget by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("1") }
    var people by remember { mutableStateOf("1") }
    var vehicle by remember { mutableStateOf("Mobil") }
    var interests by remember { mutableStateOf("") }
    var withChildren by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("08:00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Trip Planner", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        },
        containerColor = Color.White
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            TopTabNavigation(selectedTab = selectedTab, onTabSelected = onTabSelected)

            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = appBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI sedang menyusun rute terbaik...")
                    }
                }
            } else if (vm.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(vm.errorMessage ?: "", color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { vm.resetPlan() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            } else if (vm.tripPlanResult != null) {
                val result = vm.tripPlanResult!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(result.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(result.summary, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.MonetizationOn, contentDescription = null, tint = appBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Estimasi Biaya: Rp ${result.estimatedCost}", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sisa Budget: Rp ${result.remainingBudget}", color = Color.DarkGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = appBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Durasi & Jarak: ${result.estimatedDuration} (${result.estimatedDistance})", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Cuaca: ${result.weatherRecommendation}", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Rute Perjalanan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(result.destinations) { dest ->
                        val wisataObj = wisataList.find { it.id == dest.idWisata }
                        val weatherKey = wisataObj?.let { "${it.lat},${it.lng}" } ?: ""
                        val weatherState = weatherStateMap[weatherKey] ?: com.yuudev.wisatakebumen.viewmodel.WeatherState.Idle

                        DestinationCard(dest = dest, weatherState = weatherState, onOpenMap = {
                            val uri = Uri.parse("google.navigation:q=${dest.latitude},${dest.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                Toast.makeText(context, "Google Maps tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }, onBook = {
                            val wisataObj = wisataList.find { it.id == dest.idWisata }
                            if (wisataObj != null) {
                                onDestinationClick(wisataObj)
                            } else {
                                Toast.makeText(context, "Data wisata tidak sinkron", Toast.LENGTH_SHORT).show()
                            }
                        })
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { vm.resetPlan() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buat Rencana Baru")
                        }
                    }
                }
            } else {
                // INPUT FORM
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Buat Rencana Perjalanan Pintar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Budget (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.MonetizationOn, contentDescription = "Budget") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Durasi (Jam)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = people,
                            onValueChange = { people = it },
                            label = { Text("Jumlah Orang") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vehicle,
                        onValueChange = { vehicle = it },
                        label = { Text("Kendaraan (Motor/Mobil/Bus)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.DirectionsCar, contentDescription = "Kendaraan") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = interests,
                        onValueChange = { interests = it },
                        label = { Text("Minat (Pantai, Alam, Sejarah)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.Explore, contentDescription = "Minat") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = withChildren, onCheckedChange = { withChildren = it })
                        Text("Bawa Anak-anak")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val req = TripRequest(
                                budget = budget.toIntOrNull() ?: 0,
                                duration = duration.toIntOrNull() ?: 1,
                                people = people.toIntOrNull() ?: 1,
                                vehicle = vehicle,
                                startLatitude = -7.671398, // Dummy start lat (Kebumen center)
                                startLongitude = 109.664448, // Dummy start lng
                                interests = interests.split(",").map { it.trim() },
                                withChildren = withChildren,
                                startTime = startTime
                            )
                            vm.generateTrip(req, wisataList, weatherViewModel)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = appBlue)
                    ) {
                        Text("Generate Rencana Trip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DestinationCard(dest: DestinationPlan, weatherState: com.yuudev.wisatakebumen.viewmodel.WeatherState, onOpenMap: () -> Unit, onBook: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${dest.arrivalTime} - ${dest.departureTime}", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                if (weatherState is com.yuudev.wisatakebumen.viewmodel.WeatherState.Success) {
                    val code = weatherState.data.current.weatherCode
                    val (badgeText, badgeColor) = getWeatherBadge(code)
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(dest.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            
            when (weatherState) {
                is com.yuudev.wisatakebumen.viewmodel.WeatherState.Success -> {
                    val current = weatherState.data.current
                    val (desc, icon) = com.yuudev.wisatakebumen.viewmodel.WeatherViewModel.getWeatherInfo(current.weatherCode)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$desc ${current.temperature}°C", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${current.relativeHumidity}%", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Air, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${current.windSpeed} km/j", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
                is com.yuudev.wisatakebumen.viewmodel.WeatherState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Loading Cuaca...", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                is com.yuudev.wisatakebumen.viewmodel.WeatherState.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Data cuaca tidak tersedia", fontSize = 12.sp, color = Color.Red, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(dest.deskripsi, fontSize = 12.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dest.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text("Tiket: Rp ${dest.ticketPrice}", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Peta", fontSize = 12.sp)
                }
                Button(
                    onClick = onBook,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pesan", fontSize = 12.sp)
                }
            }
        }
    }
}

fun getWeatherBadge(code: Int): Pair<String, Color> {
    return when (code) {
        0, 1, 2, 3 -> "Sangat Direkomendasikan" to Color(0xFF4CAF50)
        45, 48, 51, 53, 55, 56, 57 -> "Cukup Direkomendasikan" to Color(0xFFFF9800)
        else -> "Kurang Direkomendasikan" to Color(0xFFF44336)
    }
}




