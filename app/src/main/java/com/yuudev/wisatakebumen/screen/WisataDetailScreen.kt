package com.yuudev.wisatakebumen.screen

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.yuudev.wisatakebumen.viewmodel.WeatherState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuudev.wisatakebumen.viewmodel.WeatherViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yuudev.wisatakebumen.manajer.SessionManager
import com.yuudev.wisatakebumen.model.Review
import com.yuudev.wisatakebumen.model.ReviewResponse
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisataDetailScreen(
    wisata: Wisata,
    userLocation: Location?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherStateMap by weatherViewModel.weatherData.collectAsState(initial = emptyMap())
    val weatherKey = "${wisata.lat},${wisata.lng}"
    val weatherState = weatherStateMap[weatherKey] ?: WeatherState.Idle

    val reviewViewModel: com.yuudev.wisatakebumen.viewmodel.ReviewViewModel = viewModel()
    val reviews by reviewViewModel.reviews.collectAsState()
    val isReviewLoading by reviewViewModel.isLoading.collectAsState()
    val reviewError by reviewViewModel.errorMessage.collectAsState()

    val pullRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) {
            weatherViewModel.fetchWeather(wisata.lat, wisata.lng, force = true)
            reviewViewModel.loadReviews(wisata.id, force = true)
            pullRefreshState.endRefresh()
        }
    }

    LaunchedEffect(weatherKey) {
        if (weatherState is WeatherState.Idle && wisata.lat != 0.0 && wisata.lng != 0.0) {
            weatherViewModel.fetchWeather(wisata.lat, wisata.lng)
        }
    }

    LaunchedEffect(wisata.id) {
        reviewViewModel.loadReviews(wisata.id)
    }
    
    var showPesanDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    
    var isSubmittingReview by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(reviewError) {
        reviewError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            reviewViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // HEADER IMAGE
            item {
                Box {
                    AsyncImage(
                        model = wisata.image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.6f))
                                )
                            )
                    )

                    Text(
                        wisata.nama,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // WEATHER SECTION
            item {
                WeatherDetailSection(weatherState = weatherState)
            }

            // CONTENT INFO
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        wisata.nama,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // RATING SUMMARY
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${wisata.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "(${wisata.totalReview} Review)",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    userLocation?.let {
                        val result = FloatArray(1)
                        Location.distanceBetween(
                            it.latitude,
                            it.longitude,
                            wisata.lat,
                            wisata.lng,
                            result
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("${"%.2f".format(result[0] / 1000)} km dari kamu", color = Color.Gray, fontSize = 13.sp) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        wisata.deskripsi,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(text = "Tiket Masuk: ${wisata.harga}", color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse("geo:${wisata.lat},${wisata.lng}?q=${wisata.lat},${wisata.lng}(${wisata.nama})")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.LocationOn, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Map")
                        }

                        Button(
                            onClick = {
                                val uri = Uri.parse("google.navigation:q=${wisata.lat},${wisata.lng}&mode=d")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Navigation, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Navigasi")
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showPesanDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Icon(Icons.Rounded.ConfirmationNumber, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pesan Tiket", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // REVIEW SECTION HEADER
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Reviews, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Ulasan Pengunjung", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        TextButton(onClick = { showReviewDialog = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tulis Ulasan")
                        }
                    }
                }
            }

            // REVIEW LIST
            if (isReviewLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                if (reviews.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.RateReview, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(8.dp))
                            Text("Belum ada ulasan.", color = Color.Gray)
                            Text("Jadilah yang pertama memberikan ulasan.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(reviews) { review ->
                        ReviewItem(review)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        androidx.compose.material3.pulltorefresh.PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        }
    }

    if (showPesanDialog) {
        PesanTiketDialog(
            wisata = wisata,
            onDismiss = { showPesanDialog = false },
            onPesan = { _, _, _ -> showPesanDialog = false }
        )
    }

    if (showReviewDialog) {
        WriteReviewDialog(
            isSubmitting = isSubmittingReview,
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, komentar ->
                scope.launch {
                    isSubmittingReview = true
                    try {
                        val json = """
                        {
                            "action": "tambahReview",
                            "wisataId": "${wisata.id}",
                            "username": "${SessionManager.getUsername(context)}",
                            "nama": "${SessionManager.getNama(context)}",
                            "rating": $rating,
                            "komentar": "$komentar"
                        }
                        """.trimIndent()
                        val body = json.toRequestBody("application/json".toMediaType())
                        val response = RetrofitClient.api.tambahReview(body)
                        if (response.success) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Ulasan terkirim", Toast.LENGTH_SHORT).show()
                                showReviewDialog = false
                                reviewViewModel.loadReviews(wisata.id, force = true)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Gagal mengirim ulasan", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        isSubmittingReview = false
                    }
                }
            }
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = review.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Text(text = review.tanggal, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                RatingStars(rating = review.rating)
            }
            Spacer(Modifier.height(12.dp))
            Text(text = review.komentar, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}

@Composable
fun RatingStars(rating: Int) {
    Row {

        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray
            )
        }
    }
}

@Composable
fun WriteReviewDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var komentar by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tulis Ulasan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bagaimana pengalaman Anda?", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                Row {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            enabled = !isSubmitting
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = komentar,
                    onValueChange = { komentar = it },
                    label = { Text("Ceritakan pengalaman Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, komentar) },
                enabled = komentar.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Kirim")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal")
            }
        }
    )
}




@Composable
fun WeatherDetailSection(weatherState: WeatherState) {
    when (weatherState) {
        is WeatherState.Idle -> {}
        is WeatherState.Loading -> {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WeatherState.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Error, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("Data cuaca tidak tersedia", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
        is WeatherState.Success -> {
            val data = weatherState.data
            val current = data.current
            val (weatherDesc, weatherIcon) = WeatherViewModel.getWeatherInfo(current.weatherCode)

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Current Weather Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Cuaca Saat Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(weatherIcon, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(weatherDesc, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("${current.temperature}°C", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            WeatherInfoItem(Icons.Rounded.WaterDrop, "${current.relativeHumidity}%")
                            WeatherInfoItem(Icons.Rounded.Air, "${current.windSpeed} km/j")
                            
                            val sunrise = data.daily?.sunrise?.firstOrNull()?.substringAfter("T") ?: "--:--"
                            val sunset = data.daily?.sunset?.firstOrNull()?.substringAfter("T") ?: "--:--"
                            
                            WeatherInfoItem(Icons.Rounded.WbSunny, sunrise)
                            WeatherInfoItem(Icons.Rounded.NightsStay, sunset)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Forecast 24 Jam
                if (data.hourly != null) {
                    Text("Forecast 24 Jam", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                    
                    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
                    val now = java.util.Date()
                    val startIndex = data.hourly.time.indexOfFirst { timeStr ->
                        try {
                            val date = formatter.parse(timeStr)
                            date != null && date.after(now)
                        } catch(e: Exception) { false }
                    }.takeIf { it != -1 } ?: 0
                    
                    val endIndex = minOf(startIndex + 24, data.hourly.time.size)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(endIndex - startIndex) { i ->
                            val index = startIndex + i
                            val timeStr = data.hourly.time[index].substringAfter("T")
                            val temp = data.hourly.temperature2m[index]
                            val code = data.hourly.weatherCode[index]
                            val pop = data.hourly.precipitationProbability[index]
                            val (_, icon) = WeatherViewModel.getWeatherInfo(code)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.width(70.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(timeStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(Modifier.height(4.dp))
                                    Icon(icon, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text("${temp}°C", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (pop > 0) {
                                        Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.WaterDrop,null,Modifier.size(10.dp),tint=androidx.compose.material3.MaterialTheme.colorScheme.primary);Spacer(Modifier.width(2.dp));Text(text = "$pop%",fontSize=10.sp,color=androidx.compose.material3.MaterialTheme.colorScheme.primary)}
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun WeatherInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(value, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}








