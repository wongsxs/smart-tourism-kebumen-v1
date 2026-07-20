package com.yuudev.wisatakebumen.screen

import android.app.Activity
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuudev.wisatakebumen.R
import com.yuudev.wisatakebumen.component.TopTabNavigation
import com.yuudev.wisatakebumen.component.WisataItem
import com.yuudev.wisatakebumen.manajer.SessionManager
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.util.getUserLocation
import com.yuudev.wisatakebumen.viewmodel.WisataViewModel
import com.yuudev.wisatakebumen.viewmodel.WeatherViewModel
import androidx.compose.material.icons.rounded.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (Wisata) -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val primary = com.yuudev.wisatakebumen.ui.theme.AppColors.Navy // navy gelap
    val appBlue = androidx.compose.material3.MaterialTheme.colorScheme.primary
    
    val vm: WisataViewModel = viewModel()
    val wisataList by vm.wisataList.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    
    val weatherVm: WeatherViewModel = viewModel()
    
    var search by remember { mutableStateOf("") }
    val pullRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
    var selectedKategori by remember { mutableStateOf("Semua") }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    
    getUserLocation { userLocation = it }

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) {
            vm.refreshWisata(force = true)
            pullRefreshState.endRefresh()
        }
    }

    val kategoriList = listOf("Semua", "Pantai", "Alam", "Sejarah")

    val filtered = wisataList.filter {
        it.nama.contains(search, true) &&
                (selectedKategori == "Semua" ||
                        it.kategori.trim().equals(selectedKategori, ignoreCase = true))
    }

    val sorted = if (userLocation != null) {
        filtered.sortedBy {
            val result = FloatArray(1)
            Location.distanceBetween(
                userLocation!!.latitude,
                userLocation!!.longitude,
                it.lat,
                it.lng,
                result
            )
            result[0]
        }
    } else filtered

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Rounded.TravelExplore,
                            contentDescription = null,
                            tint = appBlue,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {

                            Text(
                                text = "Smart Tourism",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = appBlue
                            )

                            Text(
                                text = "Kebumen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    com.yuudev.wisatakebumen.component.ProfileMenu(
                        onSettingsClick = onSettingsClick,
                        onProfileClick = onProfileClick,
                        onAboutClick = onAboutClick,
                        onLogoutClick = {
                            SessionManager.logout(context)
                            (context as Activity).recreate()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        },
        containerColor = Color.White
    ) { pad ->
        Column(Modifier.padding(pad)) {
            // Navigation Tabs
            TopTabNavigation(selectedTab = selectedTab, onTabSelected = onTabSelected)

            // Search Bar
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Cari wisata terbaik...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Cari") },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appBlue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kategori row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                kategoriList.forEach { kategori ->
                    CategoryItem(
                        label = kategori,
                        isSelected = selectedKategori == kategori,
                        onClick = { selectedKategori = kategori }
                    )
                }
            }

            // List of Wisata
            if (sorted.isEmpty()) {
                com.yuudev.wisatakebumen.component.EmptyStateView(
                    icon = Icons.Rounded.SearchOff,
                    title = "Tidak Ada Hasil",
                    description = "Maaf, kami tidak dapat menemukan wisata yang Anda cari.",
                    ctaText = "Hapus Pencarian",
                    onCtaClick = { search = "" },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(sorted) { wisata ->
                        WisataItem(
                            wisata = wisata,
                            onClick = onItemClick,
                            isAdmin = false,
                            onEdit = {},
                            userLocation = userLocation,
                            weatherViewModel = weatherVm
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val activeBg = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val inactiveBg = Color.Transparent
    val activeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val inactiveColor = Color.Gray

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) activeBg else inactiveBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = when (label) {
                "Pantai" -> Icons.Rounded.Water
                "Alam" -> Icons.Rounded.Landscape
                "Sejarah" -> Icons.Rounded.AccountBalance
                else -> Icons.Rounded.Explore
            },
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label, 
            fontSize = 12.sp, 
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}







