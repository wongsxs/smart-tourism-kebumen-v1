package com.yuudev.wisatakebumen.component

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yuudev.wisatakebumen.manajer.FavoriteManager
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.viewmodel.WeatherViewModel
import com.yuudev.wisatakebumen.viewmodel.WeatherState

@Composable
fun WisataItem(
    wisata: Wisata,
    onClick: (Wisata) -> Unit,
    isAdmin: Boolean = false,
    onEdit: (Wisata) -> Unit = {},
    onDelete: (Wisata) -> Unit = {},
    userLocation: Location? = null,
    weatherViewModel: WeatherViewModel? = null
) {
    val context = LocalContext.current
    var isFav by remember { mutableStateOf(FavoriteManager.isFavorite(context, wisata.nama)) }
    
    val weatherStateMap by weatherViewModel?.weatherData?.collectAsState(initial = emptyMap()) ?: remember { mutableStateOf(emptyMap()) }
    val weatherKey = "${wisata.lat},${wisata.lng}"
    val weatherState = weatherStateMap[weatherKey] ?: WeatherState.Idle

    LaunchedEffect(weatherKey) {
        if (weatherState is WeatherState.Idle && wisata.lat != 0.0 && wisata.lng != 0.0) {
            weatherViewModel?.fetchWeather(wisata.lat, wisata.lng)
        }
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick(wisata) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(180.dp)) {
                AsyncImage(
                    model = wisata.image,
                    contentDescription = wisata.nama,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                Text(
                    text = wisata.nama,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )

                if (!isAdmin) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .clickable {
                                isFav = !isFav
                                FavoriteManager.toggle(context, wisata.nama)
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // ⭐ Rating Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (wisata.rating == 0.0 || wisata.totalReview == 0) {
                        Text(
                            text = "Baru",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    } else {
                        Text(
                            text = "${wisata.rating} (${wisata.totalReview})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                // ⭐ Weather Section
                if (weatherViewModel != null && wisata.lat != 0.0 && wisata.lng != 0.0) {
                    when (weatherState) {
                        is WeatherState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                        is WeatherState.Success -> {
                            val data = (weatherState as WeatherState.Success).data.current
                            val (weatherDesc, weatherIcon) = WeatherViewModel.getWeatherInfo(data.weatherCode)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = weatherIcon, contentDescription = weatherDesc, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$weatherDesc ${data.temperature}°C",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "💧${data.relativeHumidity}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🌬${data.windSpeed} km/j",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        is WeatherState.Error -> {
                            Text(
                                text = "Gagal memuat cuaca",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red
                            )
                        }
                        else -> {}
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = wisata.deskripsi,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mulai dari",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Rp ${wisata.harga}",
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (isAdmin) {
                        Row {
                            IconButton(onClick = { onEdit(wisata) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Blue)
                            }
                            IconButton(onClick = { onDelete(wisata) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = Color.Red)
                            }
                        }
                    } else {
                        userLocation?.let {
                            val result = FloatArray(1)
                            Location.distanceBetween(
                                it.latitude, it.longitude,
                                wisata.lat, wisata.lng,
                                result
                            )
                            Text(
                                text = "📍 ${"%.1f".format(result[0] / 1000)} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}


