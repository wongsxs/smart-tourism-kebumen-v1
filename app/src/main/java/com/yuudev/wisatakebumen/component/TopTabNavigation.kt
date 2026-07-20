package com.yuudev.wisatakebumen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopTabNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val activeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val inactiveColor = Color(0xFFE0E0E0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SEMUA (Home)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selectedTab == 0) activeColor else inactiveColor)
                .clickable { onTabSelected(0) }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                "Semua",
                color = if (selectedTab == 0) Color.White else Color.Black,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // â­ FAVORIT
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selectedTab == 1) activeColor else inactiveColor)
                .clickable { onTabSelected(1) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Rounded.Favorite,
                contentDescription = "Favorit",
                tint = if (selectedTab == 1) Color.White else Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // TIKET
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selectedTab == 2) activeColor else inactiveColor)
                .clickable { onTabSelected(2) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Rounded.ConfirmationNumber,
                contentDescription = "Tiket",
                tint = if (selectedTab == 2) Color.White else Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // AI TRIP
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selectedTab == 3) activeColor else inactiveColor)
                .clickable { onTabSelected(3) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = "AI Trip",
                tint = if (selectedTab == 3) Color.White else Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}



