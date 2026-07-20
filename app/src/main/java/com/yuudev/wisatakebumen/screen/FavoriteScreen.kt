package com.yuudev.wisatakebumen.screen

import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuudev.wisatakebumen.component.TopTabNavigation
import com.yuudev.wisatakebumen.component.WisataItem
import com.yuudev.wisatakebumen.manajer.FavoriteManager
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.util.getUserLocation
import com.yuudev.wisatakebumen.viewmodel.WisataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onItemClick: (Wisata) -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val navyDark = com.yuudev.wisatakebumen.ui.theme.AppColors.Navy
    
    val vm: WisataViewModel = viewModel()
    val wisataList by vm.wisataList.collectAsState()
    
    var userLocation by remember { mutableStateOf<Location?>(null) }
    getUserLocation { userLocation = it }

    val favoriteList = wisataList.filter {
        FavoriteManager.isFavorite(context, it.nama)
    }

    // Tombol Back Android -> Kembali ke Home
    BackHandler {
        onTabSelected(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Favorit",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Wisata favorit Anda",
                            style = MaterialTheme.typography.labelSmall.copy(
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
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Navigation Tabs
            TopTabNavigation(selectedTab = selectedTab, onTabSelected = onTabSelected)

            if (favoriteList.isEmpty()) {
                com.yuudev.wisatakebumen.component.EmptyStateView(
                    icon = Icons.Rounded.FavoriteBorder,
                    title = "Belum Ada Favorit",
                    description = "Temukan destinasi wisata impian Anda dan tandai sebagai favorit.",
                    ctaText = "Cari Wisata",
                    onCtaClick = { onTabSelected(0) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(favoriteList) { wisata ->
                        WisataItem(
                            wisata = wisata,
                            onClick = onItemClick,
                            isAdmin = false,
                            onEdit = {},
                            userLocation = userLocation
                        )
                    }
                }
            }
        }
    }
}






