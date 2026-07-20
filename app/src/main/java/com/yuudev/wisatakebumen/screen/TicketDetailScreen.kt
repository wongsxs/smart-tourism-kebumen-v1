package com.yuudev.wisatakebumen.screen

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.yuudev.wisatakebumen.model.Tiket
import com.yuudev.wisatakebumen.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    bookingId: String,
    onBack: () -> Unit
) {
    var tiket by remember { mutableStateOf<Tiket?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val navyDark = com.yuudev.wisatakebumen.ui.theme.AppColors.Navy
    val appBlue = androidx.compose.material3.MaterialTheme.colorScheme.primary

    BackHandler { onBack() }

    LaunchedEffect(bookingId) {
        scope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.api.getDetailTiket(bookingId = bookingId)
                tiket = response
                isError = false
            } catch (e: Exception) {
                e.printStackTrace()
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Tiket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = appBlue)
            } else if (isError || tiket == null) {
                ErrorTicketState(onBack)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            Text(
                                text = tiket!!.namaWisata,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = navyDark,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // QR Code
                            val qrBitmap = remember(tiket!!.bookingId) {
                                generateQRCode(tiket!!.bookingId)
                            }
                            
                            qrBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR Code Tiket",
                                    modifier = Modifier.size(200.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = tiket!!.bookingId,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = appBlue
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // Ticket Info
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                TicketDetailInfoItem(Icons.Outlined.Person, "Nama Pemesan", tiket!!.namaPemesan)
                                TicketDetailInfoItem(Icons.Outlined.CalendarMonth, "Tanggal Kunjungan", tiket!!.tanggalKunjungan)
                                TicketDetailInfoItem(Icons.Outlined.Groups, "Jumlah Tiket", "${tiket!!.jumlahTiket} Tiket")
                                TicketDetailInfoItem(Icons.Outlined.Payments, "Total Bayar", formatPriceRupiah(tiket!!.totalBayar))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Info, null, modifier = Modifier.size(20.dp), tint = appBlue)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Status", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        TiketStatusBadgeDetail(tiket!!.status)
                                    }
                                }
                                
                                TicketDetailInfoItem(Icons.Outlined.History, "Waktu Pembelian", tiket!!.createdAt)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guidance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = appBlue.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lightbulb, null, tint = appBlue)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Silakan tunjukkan QR Code ini kepada petugas untuk dilakukan proses validasi sebelum memasuki area wisata.",
                                style = MaterialTheme.typography.bodySmall,
                                color = navyDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                  /*  Button(
                        onClick = { /* Placeholder */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = appBlue)
                    ) {
                        Icon(Icons.Rounded.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bagikan Tiket", fontWeight = FontWeight.Bold)
                    }*/
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun TicketDetailInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TiketStatusBadgeDetail(status: String) {
    val color = when (status.uppercase()) {
        "LUNAS", "SUCCESS", "BERHASIL", "PAID" -> Color(0xFF22C55E)
        "SUDAH DIGUNAKAN" -> Color.Gray
        "PENDING", "MENUNGGU" -> Color(0xFFF59E0B)
        "BATAL", "CANCEL" -> Color(0xFFEF4444)
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, RoundedCornerShape(50))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ErrorTicketState(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = Color.Red)
        Spacer(Modifier.height(16.dp))
        Text("Tiket tidak ditemukan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Maaf, data tiket tidak tersedia atau terjadi kesalahan.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Kembali")
        }
    }
}

fun generateQRCode(text: String): Bitmap? {
    return try {
        val size = 512
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun formatPriceRupiah(value: String): String {
    return try {
        val number = value.toDoubleOrNull() ?: 0.0
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.format(number).replace(",00", "").replace("Rp", "Rp ")
    } catch (e: Exception) {
        "Rp $value"
    }
}




