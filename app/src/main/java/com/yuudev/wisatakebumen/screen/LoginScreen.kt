package com.yuudev.wisatakebumen.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuudev.wisatakebumen.R
import com.yuudev.wisatakebumen.manajer.SessionManager
import com.yuudev.wisatakebumen.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val navyDark = com.yuudev.wisatakebumen.ui.theme.AppColors.Navy
    val appBlue = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val textGrey = Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Header Section (Navy Background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(navyDark)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(Modifier.width(12.dp))

                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Jelajahi wisata terbaik di Kebumen",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // 2. Scrollable Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(130.dp)) // Offset to overlap the header

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .animateContentSize()
                    ) {
                        // Header Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(appBlue.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = navyDark
                        )
                        Text(
                            text = "Masuk untuk menikmati pengalaman wisata terbaik.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textGrey,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Form
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = "Ikon Username", tint = appBlue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            enabled = !loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appBlue,
                                focusedLabelColor = appBlue,
                                cursorColor = appBlue,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = "Ikon Password", tint = appBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                        contentDescription = "Toggle Visibility Password",
                                        tint = textGrey
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            enabled = !loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appBlue,
                                focusedLabelColor = appBlue,
                                cursorColor = appBlue,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (username.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                loading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val json = """
                                        {
                                          "action":"login",
                                          "username":"$username",
                                          "password":"$password"
                                        }
                                        """.trimIndent()
                                        val body = json.toRequestBody("application/json".toMediaType())
                                        val result = RetrofitClient.api.login(body)
                                        if (result.success) {
                                            SessionManager.saveLogin(context, result.nama, result.username, result.role)
                                            kotlinx.coroutines.withContext(Dispatchers.Main) { onSuccess() }
                                        } else {
                                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = appBlue),
                            enabled = !loading,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.3f))
                            Text(
                                " Atau ",
                                style = MaterialTheme.typography.labelMedium,
                                color = textGrey,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.3f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Belum punya akun?", color = textGrey, fontSize = 14.sp)
                            TextButton(onClick = onRegister, enabled = !loading) {
                                Text("Daftar Sekarang", color = appBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Explore Beautiful Kebumen",
                    color = textGrey.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                /*Text(
                    text = "Made with â¤ï¸ in Kebumen",
                    color = textGrey.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )*/
            }
        }
    }
}



