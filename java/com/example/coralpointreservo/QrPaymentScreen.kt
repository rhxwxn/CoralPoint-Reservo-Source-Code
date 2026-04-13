package com.example.coralpointreservo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPaymentScreen(
    onBackClick: () -> Unit,
    onUploadReceiptClick: (Uri) -> Unit,
    isProcessing: Boolean = false
) {
    // State to hold the selected image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // The launcher that opens the Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Payment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BookingStepper(activeStep = 2)

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Q", color = Color(0xFF0A3D91), fontWeight = FontWeight.Black, fontSize = 48.sp)
                Text("R", color = Color(0xFFE2262A), fontWeight = FontWeight.Black, fontSize = 48.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ph", color = Color(0xFF0A3D91), fontWeight = FontWeight.Black, fontSize = 48.sp)
            }

            Text("SCAN NA ALL!", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFFFFF000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("NO FEES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CORALPOINT SUITES", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("QRPH 552288", fontSize = 10.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(id = R.drawable.qr),
                        contentDescription = "Scan to Pay QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Basta kaya i-scan, pwede yan!", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MockIconBox("BANKS", Color(0xFF1B5A96))
                MockIconBox("E-WALLETS", Color(0xFFE63946))
                MockIconBox("AND MORE", Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Check the back for more details", fontSize = 10.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text("Powered by ", fontSize = 10.sp, color = Color.Gray)
                Text("maya", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 🚀 DYNAMIC BOTTOM SECTION
            if (selectedImageUri == null) {
                // Show standard "Upload" button if no image is picked
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload Receipt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Show preview and "Complete Booking" button if image is picked
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Receipt Attached", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Receipt Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, Color(0xFF054659), RoundedCornerShape(8.dp))
                    )

                    // Optional button to change the image if they picked the wrong one
                    TextButton(onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text("Change Image", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        // Prevent clicking again if it's already processing
                        onClick = { if (!isProcessing) onUploadReceiptClick(selectedImageUri!!) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            // Show a loading spinner if uploading
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            // Show the normal checkmark if not uploading
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Complete Booking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// 🚀 Helper function completely outside of the QrPaymentScreen!
@Composable
fun MockIconBox(label: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(bgColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}