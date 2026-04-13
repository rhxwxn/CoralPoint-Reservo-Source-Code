package com.example.coralpointreservo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationScreen(
    bookingReference: String,
    onBackHomeClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFFFFFFF)
    ) { paddingValues ->
        val cfg = LocalConfiguration.current
        val compact = cfg.screenWidthDp <= 430
        val maxCardWidth = if (compact) 420.dp else 520.dp
        val headSize = if (compact) 16.sp else 18.sp
        val subHeadSize = if (compact) 16.sp else 18.sp
        val refSize = if (compact) 24.sp else 28.sp
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxCardWidth)
                .padding(horizontal = if (compact) 16.dp else 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BookingStepper(activeStep = 3)

            Spacer(modifier = Modifier.height(28.dp))

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF054659),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Thank you for your order!",
                fontSize = headSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The reservation number is",
                fontSize = subHeadSize,
                color = Color(0xFF0F5A73),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF054659))
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "#$bookingReference",
                                color = Color.White,
                            fontSize = refSize,
                                fontWeight = FontWeight.ExtraBold,
                                textDecoration = TextDecoration.Underline
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.logo_white),
                                contentDescription = "Coralpoint logo",
                            modifier = Modifier.height(if (compact) 18.dp else 20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Reservation pending confirmation.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111),
                    fontSize = if (compact) 13.sp else 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Check your e‑mail or contact the property to confirm your booking.",
                    fontSize = if (compact) 11.sp else 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBackHomeClick,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Back to Home",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}
