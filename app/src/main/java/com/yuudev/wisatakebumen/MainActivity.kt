package com.yuudev.wisatakebumen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()

        setContent {
            MaterialTheme {
                var userLocation by remember { mutableStateOf<Location?>(null) }
                getUserLocation { userLocation = it }
                var selected by remember { mutableStateOf<Wisata?>(null) }
                val context = LocalContext.current

                var backPressedTime by remember { mutableStateOf(0L) }

                // 🔥 DOUBLE BACK EXIT
                BackHandler(enabled = selected == null) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < 2000) {
                        (context as? Activity)?.finish()
                    } else {
                        backPressedTime = currentTime
                        Toast.makeText(context, "Tekan lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    }
                }

                if (selected == null) {
                    WisataListScreen { selected = it }
                } else {
                    WisataDetailScreen(selected!!, userLocation) { selected = null }
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

// ================= LIST =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisataListScreen(onClick: (Wisata) -> Unit) {

    val context = LocalContext.current
    val ref = FirebaseDatabase.getInstance().getReference("wisata")
    val primary = Color(0xFF0F172A) // dark navy
    val accent = Color(0xFFE0E0E0) // green fresh
    val bgSoft = Color(0xFFF8FAFC)
    val softBlue = Color(0xFFE3F2FD)
    val kategoriList = listOf(
        "Semua",
        "Pantai",
        "Alam",
        "Sejarah"
    )

    var selectedKategori by remember { mutableStateOf("Semua") }
    var wisataList by remember { mutableStateOf(listOf<Wisata>()) }
    var search by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    var isAdmin by remember { mutableStateOf(false) }
    var clickCount by remember { mutableStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var editWisata by remember { mutableStateOf<Wisata?>(null) }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    getUserLocation { userLocation = it }

    // 🔥 realtime
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Wisata>()
                snapshot.children.forEach {
                    val data = it.getValue(Wisata::class.java)
                    if (data != null) list.add(data.copy(id = it.key ?: ""))
                }
                wisataList = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    //val filtered = wisataList.filter {
     //   it.nama.contains(search, true) &&
              //  (selectedKategori == "Semua" || it.kategori == selectedKategori)
   // }
    val filtered = wisataList.filter {
        it.nama.contains(search, true) &&
                (selectedKategori == "Semua" ||
                        it.kategori.trim().equals(selectedKategori, ignoreCase = true))
    }

    val favorite = wisataList.filter {
        FavoriteManager.isFavorite(context, it.nama)
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

    val display = if (selectedTab == 0) sorted else favorite

    Scaffold(
        topBar = {
            TopAppBar(
                    title = {
                        var showLoginDialog by remember { mutableStateOf(false) }

                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .height(36.dp)
                                .clickable {
                                    clickCount++
                                    if (clickCount >= 10) {
                                        showLoginDialog = true
                                        clickCount = 0
                                    }
                                }
                        )
                        //Image(
                          //  painter = painterResource(id = R.drawable.logo),
                          //  contentDescription = "Logo",
                          //  modifier = Modifier
                             //   .height(36.dp)
                                //.clickable {
                               //     clickCount++
                               //     if (clickCount >= 10) {
                               //         isAdmin = true
                               //         clickCount = 0
                                //        Toast.makeText(context, "Admin aktif 🔥", Toast.LENGTH_SHORT).show()
                               //     }
                              //  }


                        if (showLoginDialog) {

                            var password by remember { mutableStateOf("") }

                            AlertDialog(
                                onDismissRequest = { showLoginDialog = false },
                                confirmButton = {
                                    Button(onClick = {
                                        if (password == "242005") {
                                            isAdmin = true
                                            Toast.makeText(context, "Admin aktif 🔥", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Kode salah ❌", Toast.LENGTH_SHORT).show()
                                        }
                                        showLoginDialog = false
                                    }) {
                                        Text("Masuk")
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(onClick = { showLoginDialog = false }) {
                                        Text("Batal")
                                    }
                                },
                                title = { Text("Kode Admin") },
                                text = {
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Masukkan kode") }
                                    )
                                }
                            )
                        }
                    },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        }
    ) { pad ->

        Column(Modifier.padding(pad)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val activeColor = Color(0xFF2196F3)

                // 🔍 SEARCH
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Cari wisata...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 🔘 SEMUA
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selectedTab == 0) activeColor else Color(0xFFE0E0E0)
                        )
                        .clickable { selectedTab = 0 }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Semua",
                        color = if (selectedTab == 0) Color.White else Color.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // ⭐ FAVORIT
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selectedTab == 1) activeColor else Color(0xFFE0E0E0)
                        )
                        .clickable { selectedTab = 1 }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (selectedTab == 1) Color.White else Color.Red
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                kategoriList.forEach { kategori ->

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedKategori = kategori }
                    ) {

                        Icon(
                            imageVector = when (kategori) {
                                "Pantai" -> Icons.Default.Water
                                "Alam" -> Icons.Default.Landscape
                                "Sejarah" -> Icons.Default.AccountBalance
                                else -> Icons.Default.Explore
                            },
                            contentDescription = null,
                            tint = if (selectedKategori == kategori) Color.Blue else Color.Gray,
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            kategori,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            //OutlinedTextField(
           //     value = search,
            //    onValueChange = { search = it },
           //     placeholder = { Text("Cari tempat healing... 🌴") },
           //     leadingIcon = { Icon(Icons.Default.Search, null) },
              //  shape = RoundedCornerShape(30.dp),
            //    modifier = Modifier
           //         .fillMaxWidth()
           //         .padding(16.dp)
            //)
            val kategoriList = listOf(
                "Semua",
                "Pantai",
                "Alam",
                "Sejarah"
            )

           // var selectedKategori by remember { mutableStateOf("Semua") }

            LazyColumn {
                items(display) {
                    WisataItem(
                        wisata = it,
                        onClick = onClick,
                        isAdmin = isAdmin,
                        onEdit = { editWisata = it },
                        userLocation = userLocation
                    )
                }
            }
        }
    }

    // ================= TAMBAH =================
    if (showDialog) {
        var nama by remember { mutableStateOf("") }
        var deskripsi by remember { mutableStateOf("") }
        var lat by remember { mutableStateOf("") }
        var lng by remember { mutableStateOf("") }
        var image by remember { mutableStateOf("") }
        var kategori by remember { mutableStateOf("") }

        OutlinedTextField(kategori, { kategori = it })

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    val id = ref.push().key!!
                    ref.child(id).setValue(
                        Wisata(
                            id,
                            nama,
                            deskripsi,
                            lat.toDoubleOrNull() ?: 0.0,
                            lng.toDoubleOrNull() ?: 0.0,
                            image,
                            kategori
                        )
                    )
                    showDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) { Text("Batal") }
            },
            title = { Text("Tambah Wisata") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Nama Wisata")
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        placeholder = { Text("Masukkan nama wisata") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Deskripsi")
                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        placeholder = { Text("Masukkan deskripsi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Latitude")
                    OutlinedTextField(
                        value = lat,
                        onValueChange = { lat = it },
                        placeholder = { Text("Contoh: -7.12345") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Longitude")
                    OutlinedTextField(
                        value = lng,
                        onValueChange = { lng = it },
                        placeholder = { Text("Contoh: 109.12345") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Link Gambar")
                    OutlinedTextField(
                        value = image,
                        onValueChange = { image = it },
                        placeholder = { Text("Masukkan URL gambar") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Kategori")
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = { kategori = it },
                        placeholder = { Text("Pantai / Sejarah / Alam") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // ================= EDIT =================
    editWisata?.let { w ->

        var nama by remember { mutableStateOf(w.nama) }
        var deskripsi by remember { mutableStateOf(w.deskripsi) }
        var lat by remember { mutableStateOf(w.lat.toString()) }
        var lng by remember { mutableStateOf(w.lng.toString()) }
        var image by remember { mutableStateOf(w.image) }
        var kategori by remember { mutableStateOf(w.kategori) }

        AlertDialog(
            onDismissRequest = { editWisata = null },
            confirmButton = {
                Button(onClick = {
                    ref.child(w.id).setValue(
                        w.copy(
                            nama = nama,
                            deskripsi = deskripsi,
                            lat = lat.toDoubleOrNull() ?: 0.0,
                            lng = lng.toDoubleOrNull() ?: 0.0,
                            image = image,
                            kategori = kategori
                        )
                    )
                    editWisata = null
                }) { Text("Update") }
            },
            dismissButton = {
                OutlinedButton(onClick = { editWisata = null }) { Text("Batal") }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Nama Wisata")
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        placeholder = { Text("Masukkan nama wisata") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Deskripsi")
                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        placeholder = { Text("Masukkan deskripsi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Latitude")
                    OutlinedTextField(
                        value = lat,
                        onValueChange = { lat = it },
                        placeholder = { Text("Contoh: -7.12345") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Longitude")
                    OutlinedTextField(
                        value = lng,
                        onValueChange = { lng = it },
                        placeholder = { Text("Contoh: 109.12345") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Link Gambar")
                    OutlinedTextField(
                        value = image,
                        onValueChange = { image = it },
                        placeholder = { Text("Masukkan URL gambar") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Kategori")
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = { kategori = it },
                        placeholder = { Text("Pantai / Sejarah / Alam") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

// ================= ITEM =================

@Composable
fun WisataItem(
    wisata: Wisata,
    onClick: (Wisata) -> Unit,
    isAdmin: Boolean,
    onEdit: (Wisata) -> Unit,
    userLocation: Location?
) {

    val context = LocalContext.current
    var isFav by remember { mutableStateOf(FavoriteManager.isFavorite(context, wisata.nama)) }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clickable { onClick(wisata) },
        shape = RoundedCornerShape(20.dp), // 🔥 harus sama
        elevation = CardDefaults.cardElevation(0.dp), // biar ga double shadow
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ){
        Column {

            Box {

                AsyncImage(
                    model = wisata.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )

                // 🔥 GRADIENT OVERLAY
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(0.6f)
                                )
                            )
                        )
                )

                // 🔥 TEXT DI BAWAH (lebih aesthetic)
                Text(
                    wisata.nama,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.8f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Column(Modifier.padding(12.dp)) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                   // Text(wisata.nama, fontWeight = FontWeight.Bold)

                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFav) Color.Red else Color.Gray,
                        modifier = Modifier.clickable {
                            isFav = !isFav // 🔥 langsung update UI
                            FavoriteManager.toggle(context, wisata.nama)
                        }
                    )
                }

                Text(
                    wisata.deskripsi,
                    maxLines = 2,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                userLocation?.let {
                    val result = FloatArray(1)
                    Location.distanceBetween(
                        it.latitude,
                        it.longitude,
                        wisata.lat,
                        wisata.lng,
                        result
                    )

                    Text(
                        "📍 ${"%.2f".format(result[0] / 1000)} km dari kamu",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                if (isAdmin) {
                    Row {
                        Button(onClick = { onEdit(wisata) }) {
                            Text("Edit")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                FirebaseDatabase.getInstance()
                                    .getReference("wisata")
                                    .child(wisata.id)
                                    .removeValue()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Hapus")
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisataDetailScreen(
    w: Wisata,
    userLocation: Location?,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    BackHandler { onBack() }

    Scaffold(
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

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // 🔥 FIX KOSONG
        ) {

            // 🔥 HEADER (HOME STYLE)
            Box {

                AsyncImage(
                    model = w.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )

                // gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(0.6f)
                                )
                            )
                        )
                )

                // nama di gambar
                Text(
                    w.nama,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.85f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // 🔥 CONTENT
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                // nama ulang
                Text(
                    w.nama,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 🔥 JARAK
                userLocation?.let {
                    val result = FloatArray(1)
                    Location.distanceBetween(
                        it.latitude,
                        it.longitude,
                        w.lat,
                        w.lng,
                        result
                    )

                    Text(
                        "📍 ${"%.2f".format(result[0] / 1000)} km dari kamu",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // deskripsi
                Text(
                    w.deskripsi,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // tombol maps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // 🔵 LIHAT MAP
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${w.lat},${w.lng}?q=${w.lat},${w.lng}(${w.nama})")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Map")
                    }

                    // 🟢 NAVIGASI
                    Button(
                        onClick = {
                            val uri = Uri.parse("google.navigation:q=${w.lat},${w.lng}&mode=d")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigasi")
                    }
                }
                Spacer(modifier = Modifier.height(40.dp)) // 🔥 biar ga mepet bawah
            }
        }
    }
}
// ================= LOCATION =================

@SuppressLint("MissingPermission")
@Composable
fun getUserLocation(onLocation: (Location) -> Unit) {
    val context = LocalContext.current
    val client = LocationServices.getFusedLocationProviderClient(context)

    LaunchedEffect(true) {
        client.lastLocation.addOnSuccessListener {
            if (it != null) onLocation(it)
        }
    }
}