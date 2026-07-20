package com.yuudev.wisatakebumen.util

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission")
@Composable
fun getUserLocation(
    onLocation: (Location) -> Unit
) {
    val context = LocalContext.current
    val client =
        LocationServices
            .getFusedLocationProviderClient(context)

    LaunchedEffect(true) {
        client.lastLocation.addOnSuccessListener {
            if (it != null) {
                onLocation(it)
            }
        }
    }
}