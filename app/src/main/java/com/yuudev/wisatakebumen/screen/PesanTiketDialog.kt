package com.yuudev.wisatakebumen.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yuudev.wisatakebumen.R
import com.yuudev.wisatakebumen.model.Wisata
import java.text.SimpleDateFormat
import java.util.*
import com.yuudev.wisatakebumen.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.yuudev.wisatakebumen.manajer.SessionManager
import androidx.compose.ui.platform.LocalContext

// Enum untuk mengatur step alur booking secara elegan dalam satu dialog
enum class BookingStep {
    FORM_ISI,
    PEMBAYARAN_QRIS,
    E_TICKET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesanTiketDialog(
    wisata: Wisata,
    onDismiss: () -> Unit,
    onPesan: (nama: String, tanggal: String, jumlah: Int) -> Unit
) {
    // States untuk Input
    val context = LocalContext.current
    var nama by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("1") }
    var showDatePicker by remember { mutableStateOf(false) }

    // State Alur Aliran Antarmuka
    var currentStep by remember { mutableStateOf(BookingStep.FORM_ISI) }

    // State Data Final untuk E-Ticket
    var namaPemesan by remember { mutableStateOf("") }
    var tanggalKunjungan by remember { mutableStateOf("") }
    var jumlahTiket by remember { mutableStateOf(1) }
    val bookingId = remember { "WST-${(100000..999999).random()}" }

    // Parsing Harga Tiket
    val hargaTiket = wisata.harga
        .replace("Rp", "")
        .replace(".", "")
        .replace(",", "")
        .trim()
        .toIntOrNull() ?: 0

    val totalHarga = (jumlah.toIntOrNull() ?: 0) * hargaTiket

    // Base Dialog dengan Properti Modern (Gunakan edge-to-edge look)
    Dialog(
        onDismissRequest = { if (currentStep == BookingStep.FORM_ISI) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // Memberikan kesan modern bottom-sheet / modal card
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // STEP 1: FORM PENGISIAN DATA DATA
                if (currentStep == BookingStep.FORM_ISI) {
                    Text(
                        text = "Detail Pemesanan",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Card Wisata yang Dituju
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(wisata.nama, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Harga Tiket: ${wisata.harga} / pax", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Input Nama Lengkap
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Tanggal Kunjungan (Elegant Button Style)
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (tanggal.isEmpty()) "Pilih Tanggal Kunjungan" else tanggal,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Jumlah Tiket
                    OutlinedTextField(
                        value = jumlah,
                        onValueChange = { jumlah = it },
                        label = { Text("Jumlah Tiket") },
                        leadingIcon = { Icon(Icons.Rounded.ShoppingCart, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // Total Harga Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)) // Soft emerald green
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Pembayaran", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF16A34A))
                            Text("Rp ${"%,d".format(totalHarga)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF15803D))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (nama.isNotBlank() && tanggal.isNotBlank()) {
                                    namaPemesan = nama
                                    tanggalKunjungan = tanggal
                                    jumlahTiket = jumlah.toIntOrNull() ?: 1
                                    currentStep = BookingStep.PEMBAYARAN_QRIS
                                }
                            },
                            enabled = nama.isNotBlank() && tanggal.isNotBlank() && (jumlah.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lanjut ke Pembayaran")
                        }
                    }
                }

                // STEP 2: HALAMAN SCAN QRIS MODERN
                if (currentStep == BookingStep.PEMBAYARAN_QRIS) {
                    Text(
                        text = "Selesaikan Pembayaran",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Silakan scan kode QRIS di bawah ini sebelum batas waktu habis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // QRIS Box Wrapper
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.size(260.dp).padding(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.qris),
                                contentDescription = "QRIS Code",
                                modifier = Modifier.size(220.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Mendukung: Dana, OVO, GoPay, LinkAja, ShopeePay & Mobile Banking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { currentStep = BookingStep.E_TICKET },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Saya Sudah Membayar", color = Color.White)
                    }

                    TextButton(
                        onClick = { currentStep = BookingStep.FORM_ISI },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali", color = MaterialTheme.colorScheme.primary)
                    }
                }

                // STEP 3: TAMPILAN E-TICKET KELAS DUNIA
                if (currentStep == BookingStep.E_TICKET) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🎫 E-TICKET",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // Badge LUNAS / PAID Modern
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(50.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF16A34A))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LUNAS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF15803D))
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xE0E2E8F0))

                            // Booking ID Header
                            Text("BOOKING ID", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(bookingId, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)

                            Spacer(modifier = Modifier.height(20.dp))

                            // Data Rows
                            DetailRowModern("Destinasi Wisata", wisata.nama)
                            DetailRowModern("Nama Pemesan", namaPemesan)
                            DetailRowModern("Tanggal Kunjungan", tanggalKunjungan)
                            DetailRowModern("Jumlah Tiket", "$jumlahTiket Pax")

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xE0E2E8F0))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Transaksi", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text("Rp ${"%,d".format(totalHarga)}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF111827))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {

                            CoroutineScope(Dispatchers.IO).launch {

                                try {

                                    val json = """
{
  "action":"pesanTiket",
  "bookingId":"$bookingId",
  "username":"${SessionManager.getUsername(context)}",
  "namaWisata":"${wisata.nama}",
  "namaPemesan":"$namaPemesan",
  "tanggalKunjungan":"$tanggalKunjungan",
  "jumlahTiket":"$jumlahTiket",
  "totalBayar":"$totalHarga"
}
""".trimIndent()

                                    val body = json.toRequestBody(
                                        "application/json".toMediaType()
                                    )

                                    RetrofitClient.api.postData(body)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            onDismiss()

                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Selesai & Simpan")
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog terpisah
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                        tanggal = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DetailRowModern(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF1E293B))
    }
}

