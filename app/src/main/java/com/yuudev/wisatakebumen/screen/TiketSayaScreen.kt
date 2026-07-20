package com.yuudev.wisatakebumen.screen

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuudev.wisatakebumen.component.EmptyStateView
import com.yuudev.wisatakebumen.component.TopTabNavigation
import com.yuudev.wisatakebumen.manajer.SessionManager
import com.yuudev.wisatakebumen.model.Tiket
import com.yuudev.wisatakebumen.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiketSayaScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onDetailClick: (String) -> Unit
) {
    val context = LocalContext.current
    var tiketList by remember { mutableStateOf<List<Tiket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val navyDark = Color(0xFF0F172A)

    LaunchedEffect(Unit) {
        val username = SessionManager.getUsername(context)

        if (!username.isNullOrEmpty()) {
            try {
                val response = RetrofitClient.api.getPemesanan(username = username)
                tiketList = response
            } catch (e: Exception) {
                tiketList = emptyList()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            tiketList = emptyList()
        }
    }

    BackHandler {
        onTabSelected(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.ConfirmationNumber, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Tiket Saya",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Text(
                            "Riwayat tiket wisata Anda",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onTabSelected(0) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali ke Home",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navyDark,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopTabNavigation(selectedTab = selectedTab, onTabSelected = onTabSelected)
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (tiketList.isEmpty()) {
                EmptyTiketContent(
                    onExploreClick = { onTabSelected(0) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tiketList) { tiket ->
                        TiketCard(tiket = tiket, onDetailClick = onDetailClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiketCard(tiket: Tiket, onDetailClick: (String) -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val totalHarga = currencyFormat.format((tiket.totalBayar.toDoubleOrNull() ?: 0.0)).replace("Rp", "Rp ")

    Card(
        onClick = { onDetailClick(tiket.bookingId) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tiket.namaWisata ?: "Wisata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TiketItemStatusBadge(status = tiket.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            TiketInfoRowItem(
                icon = Icons.Outlined.CalendarMonth,
                label = "Tanggal",
                value = tiket.tanggalKunjungan
            )
            Spacer(modifier = Modifier.height(8.dp))
            TiketInfoRowItem(
                icon = Icons.Outlined.Groups,
                label = "Jumlah Tiket",
                value = "${tiket.jumlahTiket} Orang"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Pembayaran",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = totalHarga,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TiketInfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TiketItemStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase(Locale.ROOT)) {
        "LUNAS", "SUCCESS", "BERHASIL", "PAID" -> Pair(Color(0xFF22C55E).copy(alpha = 0.1f), Color(0xFF22C55E))
        "PENDING", "MENUNGGU" -> Pair(Color(0xFFF59E0B).copy(alpha = 0.1f), Color(0xFFF59E0B))
        "DIBATALKAN", "FAILED", "CANCELLED" -> Pair(Color(0xFFEF4444).copy(alpha = 0.1f), Color(0xFFEF4444))
        else -> Pair(Color.Gray.copy(alpha = 0.1f), Color.Gray)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EmptyTiketContent(onExploreClick: () -> Unit) {
    EmptyStateView(
        icon = Icons.Rounded.ConfirmationNumber,
        title = "Belum Ada Tiket",
        description = "Anda belum memiliki riwayat pemesanan tiket wisata. Ayo jelajahi wisata sekarang!",
        ctaText = "Jelajahi Wisata",
        onCtaClick = onExploreClick,
        modifier = Modifier.fillMaxSize()
    )
}


