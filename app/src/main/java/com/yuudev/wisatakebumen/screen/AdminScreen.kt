package com.yuudev.wisatakebumen.screen

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.yuudev.wisatakebumen.component.WisataDialog
import com.yuudev.wisatakebumen.component.WisataItem
import com.yuudev.wisatakebumen.manajer.SessionManager
import com.yuudev.wisatakebumen.model.Statistik
import com.yuudev.wisatakebumen.model.Tiket
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import com.yuudev.wisatakebumen.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AdminScreen(
    wisataList: List<Wisata>,
    onItemClick: (Wisata) -> Unit,
    onRefresh: () -> Unit
) {
    var menuAdmin by rememberSaveable { mutableStateOf("dashboard") }
    var statistik by remember { mutableStateOf(Statistik()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form States Hoisted
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingWisataId by rememberSaveable { mutableStateOf<String?>(null) }
    var nama by rememberSaveable { mutableStateOf("") }
    var deskripsi by rememberSaveable { mutableStateOf("") }
    var harga by rememberSaveable { mutableStateOf("") }
    var kategori by rememberSaveable { mutableStateOf("") }
    var image by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableDoubleStateOf(0.0) }
    var lng by rememberSaveable { mutableDoubleStateOf(0.0) }
    var address by rememberSaveable { mutableStateOf("") }
    
    var isSaving by remember { mutableStateOf(false) }
    var showMapPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            statistik = RetrofitClient.api.getStatistik()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetForm() {
        editingWisataId = null
        nama = ""
        deskripsi = ""
        harga = ""
        kategori = ""
        image = ""
        lat = 0.0
        lng = 0.0
        address = ""
        isSaving = false
    }

    if (showMapPicker) {
        MapPickerScreen(
            initialLocation = if (lat != 0.0) LatLng(lat, lng) else null,
            onLocationSelected = { result ->
                lat = result.latitude
                lng = result.longitude
                address = result.address
                showMapPicker = false
                showDialog = true
            },
            onBack = {
                showMapPicker = false
                showDialog = true
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dashboard Admin",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Kelola data wisata & pemesanan",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        SessionManager.logout(context)
                        (context as? Activity)?.recreate()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.yuudev.wisatakebumen.ui.theme.AppColors.Navy,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (menuAdmin == "wisata") {
                FloatingActionButton(
                    onClick = {
                        resetForm()
                        showDialog = true
                    },
                    containerColor = AppColors.Success,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Tambah Wisata")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Tab Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AdminTabItem("Statistik", menuAdmin == "dashboard", Icons.Rounded.BarChart, Modifier.weight(1f)) { menuAdmin = "dashboard" }
                AdminTabItem("Wisata", menuAdmin == "wisata", Icons.Rounded.Landscape, Modifier.weight(1f)) { menuAdmin = "wisata" }
                AdminTabItem("Tiket", menuAdmin == "tiket", Icons.Rounded.ConfirmationNumber, Modifier.weight(1f)) { menuAdmin = "tiket" }
            }

            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f).animateContentSize()) {
                when (menuAdmin) {
                    "dashboard" -> DashboardContent(statistik)
                    "wisata" -> WisataListContent(
                        wisataList = wisataList,
                        onItemClick = onItemClick,
                        onEdit = { wisata ->
                            resetForm()
                            editingWisataId = wisata.id
                            nama = wisata.nama
                            deskripsi = wisata.deskripsi
                            harga = wisata.harga
                            kategori = wisata.kategori
                            image = wisata.image
                            lat = wisata.lat
                            lng = wisata.lng
                            showDialog = true
                        },
                        onDelete = { wisata ->
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val json = "{\"action\":\"delete\",\"id\":\"${wisata.id}\"}"
                                    val body = json.toRequestBody("application/json".toMediaType())
                                    RetrofitClient.api.postData(body)
                                    launch(Dispatchers.Main) { onRefresh() }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    )
                    "tiket" -> ValidasiTiketContent()
                }
            }
        }
    }

    if (showDialog) {
        WisataDialog(
            wisata = if (editingWisataId != null) Wisata(id = editingWisataId!!) else null,
            nama = nama,
            onNamaChange = { nama = it },
            deskripsi = deskripsi,
            onDeskripsiChange = { deskripsi = it },
            harga = harga,
            onHargaChange = { harga = it },
            kategori = kategori,
            onKategoriChange = { kategori = it },
            image = image,
            onImageChange = { image = it },
            lat = lat,
            lng = lng,
            address = address,
            isSaving = isSaving,
            onDismiss = {
                showDialog = false
                resetForm()
            },
            onPickLocation = {
                showDialog = false
                showMapPicker = true
            },
            onConfirm = {
                isSaving = true
                scope.launch(Dispatchers.IO) {
                    try {
                        val json = if (editingWisataId != null) {
                            """
                            {
                                "action": "update",
                                "id": "$editingWisataId",
                                "nama": "$nama",
                                "deskripsi": "$deskripsi",
                                "harga": "$harga",
                                "lat": $lat,
                                "lng": $lng,
                                "kategori": "$kategori",
                                "image": "$image"
                            }
                            """.trimIndent()
                        } else {
                            """
                            {
                                "action": "add",
                                "nama": "$nama",
                                "deskripsi": "$deskripsi",
                                "harga": "$harga",
                                "lat": $lat,
                                "lng": $lng,
                                "kategori": "$kategori",
                                "image": "$image"
                            }
                            """.trimIndent()
                        }
                        val body = json.toRequestBody("application/json".toMediaType())
                        RetrofitClient.api.postData(body)
                        
                        launch(Dispatchers.Main) {
                            onRefresh()
                            showDialog = false
                            resetForm()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        launch(Dispatchers.Main) { isSaving = false }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun ValidasiTiketContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showScanner by remember { mutableStateOf(false) }
    var scannedTicket by remember { mutableStateOf<Tiket?>(null) }
    var isSearchingTicket by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    var isValidating by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }
    var isValidationError by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk scan QR", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshScannedTicket() {
        scannedTicket?.let { current ->
            scope.launch {
                try {
                    val response = RetrofitClient.api.getDetailTiket(bookingId = current.bookingId)
                    if (response.bookingId.isNotEmpty()) {
                        scannedTicket = response
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (showScanner) {
        QRScannerDialog(
            onDismiss = { showScanner = false },
            onCodeScanned = { bookingId ->
                showScanner = false
                scope.launch {
                    isSearchingTicket = true
                    try {
                        val response = RetrofitClient.api.getDetailTiket(bookingId = bookingId)
                        if (response.bookingId.isNotEmpty()) {
                            scannedTicket = response
                        } else {
                            showErrorDialog = true
                        }
                    } catch (e: Exception) {
                        showErrorDialog = true
                    } finally {
                        isSearchingTicket = false
                    }
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Error, null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("Error")
                }
            },
            text = { Text("Tiket tidak ditemukan.") }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                refreshScannedTicket()
            },
            confirmButton = {
                Button(onClick = { 
                    showSuccessDialog = false 
                    refreshScannedTicket()
                }) {
                    Text("Tutup")
                }
            },
            icon = {
                Icon(Icons.Rounded.CheckCircle, null, tint = AppColors.Success, modifier = Modifier.size(48.dp))
            },
            title = { Text("Validasi Berhasil") },
            text = { Text("Tiket berhasil digunakan.") }
        )
    }

    if (isValidationError) {
        AlertDialog(
            onDismissRequest = { isValidationError = false },
            confirmButton = {
                Button(onClick = { isValidationError = false }) {
                    Text("OK")
                }
            },
            title = { Text("Validasi Gagal") },
            text = { Text(validationMessage) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Validasi Tiket",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Scan QR Code tiket pengunjung untuk melakukan proses validasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Tampilkan Loading jika sedang mencari tiket
        if (isSearchingTicket) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Tampilkan Detail Tiket jika sudah discan
        AnimatedVisibility(visible = scannedTicket != null && !isSearchingTicket) {
            scannedTicket?.let { tiket ->
                val isUsed = tiket.status.uppercase() == "SUDAH DIGUNAKAN"
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Detail Tiket Scanned",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { scannedTicket = null }) {
                                Icon(Icons.Rounded.Close, null)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        Text(tiket.namaWisata, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ScannedInfoItem(Icons.Rounded.ConfirmationNumber, "Booking ID", tiket.bookingId)
                            ScannedInfoItem(Icons.Rounded.Person, "Pemesan", tiket.namaPemesan)
                            ScannedInfoItem(Icons.Rounded.CalendarMonth, "Tanggal", tiket.tanggalKunjungan)
                            ScannedInfoItem(Icons.Rounded.Groups, "Jumlah", "${tiket.jumlahTiket} Tiket")
                            ScannedInfoItem(Icons.Rounded.Payments, "Total", formatCurrencyForAdmin(tiket.totalBayar))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                                StatusBadgeSimple(tiket.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isValidating = true
                                        try {
                                            val json = "{\"action\":\"validasiTiket\",\"bookingId\":\"${tiket.bookingId}\"}"
                                            val body = json.toRequestBody("application/json".toMediaType())
                                            val response = RetrofitClient.api.validasiTiket(body)
                                            if (response.success) {
                                                showSuccessDialog = true
                                            } else {
                                                validationMessage = response.message
                                                isValidationError = true
                                            }
                                        } catch (e: Exception) {
                                            validationMessage = "Terjadi kesalahan koneksi"
                                            isValidationError = true
                                        } finally {
                                            isValidating = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isUsed) Color.LightGray else Color(0xFF22C55E)
                                ),
                                enabled = !isUsed && !isValidating
                            ) {
                                if (isValidating) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text(if (isUsed) "Tiket Sudah Digunakan" else "Validasi Tiket", fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    scannedTicket = null
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        showScanner = true
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isValidating
                            ) {
                                Text("Scan Lagi")
                            }
                        }
                    }
                }
            }
        }

        // Card Utama: Tombol Scan (Tampil jika belum ada tiket discan)
        if (scannedTicket == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Scan QR Tiket",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Arahkan kamera ke QR Code milik pengunjung.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                showScanner = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan QR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card Kedua: Riwayat Validasi
    /*    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Riwayat Validasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.HistoryEdu, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Belum ada data validasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        // Card Ketiga: Statistik
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Statistik",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatRowItemAdmin("Total Tiket Hari Ini", "0")
                    StatRowItemAdmin("Tiket Sudah Digunakan", "0")
                    StatRowItemAdmin("Tiket Belum Digunakan", "0")
                }
            }
        }*/
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun ScannedInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadgeSimple(status: String) {
    val color = when (status.uppercase()) {
        "LUNAS", "SUCCESS", "BERHASIL" -> Color(0xFF22C55E)
        "SUDAH DIGUNAKAN" -> Color.Gray
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun QRScannerDialog(
    onDismiss: () -> Unit,
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        
                        val barcodeScanner = BarcodeScanning.getClient()
                        
                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(barcodeScanner, imageProxy, onCodeScanned)
                        }
                        
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                }
            )
            
            // Overlay UI
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Rounded.Close, null, tint = Color.White)
                }
                
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center)
                        .background(Color.Transparent)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    // Scanner Frame visual
                }
                
                Text(
                    "Arahkan kamera ke QR Code",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: androidx.camera.core.ImageProxy,
    onCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { code ->
                        onCodeScanned(code)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
fun DashboardContent(statistik: Statistik) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Total Tiket", statistik.totalTiket.toString(), Icons.Rounded.ConfirmationNumber,  MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatCard("Pendapatan", currencyFormat.format(statistik.totalPendapatan).replace("Rp", "Rp "), Icons.Rounded.Payments, Color(0xFFFFF3E0), Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = "Terpopuler", tint = AppColors.Warning, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Wisata Terpopuler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    if (statistik.topWisata.isEmpty()) {
                        Text("Data belum tersedia", modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), textAlign = TextAlign.Center, color = Color.Gray)
                    } else {
                        statistik.topWisata.forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(item.getOrNull(0)?.toString() ?: "-", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text("${item.getOrNull(1) ?: 0} Terjual", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            if (index < statistik.topWisata.size - 1) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WisataListContent(
    wisataList: List<Wisata>,
    onItemClick: (Wisata) -> Unit,
    onEdit: (Wisata) -> Unit,
    onDelete: (Wisata) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(wisataList) { wisata ->
            WisataItem(wisata, onItemClick, isAdmin = true, onEdit = onEdit, onDelete = onDelete, userLocation = null)
        }
    }
}

@Composable
fun AdminTabItem(label: String, isSelected: Boolean, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(44.dp).background(if (isSelected)  MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = if (isSelected) Color.White else Color.Gray, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = AppColors.TextPrimary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun StatRowItemAdmin(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

fun formatCurrencyForAdmin(value: String): String {
    return try {
        val number = value.toDoubleOrNull() ?: 0.0
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.format(number).replace(",00", "").replace("Rp", "Rp ")
    } catch (e: Exception) {
        "Rp $value"
    }
}




