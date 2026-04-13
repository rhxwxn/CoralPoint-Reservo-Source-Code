package com.example.coralpointreservo


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// We removed the word "private" so ALL screens can see this one single stepper!
@Composable
fun BookingStepper(activeStep: Int) {
    val steps = listOf("Booking", "Details", "Payment")
    val activeColor = Color(0xFF054659) // Brand Teal
    val inactiveColor = Color(0xFFCCCCCC) // Lighter gray for inactive steps
    val labelColorActive = Color(0xFF054659)
    val labelColorInactive = Color(0xFFBBBBBB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, label ->
            val isCompleted = index < activeStep
            val isActive = index == activeStep

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Left Line
                    if (index > 0) {
                        val lineColor = if (index <= activeStep) activeColor else inactiveColor
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .height(2.dp)
                                .fillMaxWidth(0.5f)
                                .padding(end = 12.dp)
                                .background(lineColor)
                        )
                    }

                    // Right Line
                    if (index < steps.size - 1) {
                        val lineColor = if (index < activeStep) activeColor else inactiveColor
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .height(2.dp)
                                .fillMaxWidth(0.5f)
                                .padding(start = 12.dp)
                                .background(lineColor)
                        )
                    }

                    // The Step Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        if (isCompleted || isActive) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.White, CircleShape)
                                    .border(2.dp, inactiveColor, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Step Label
                Text(
                    text = label,
                    color = if (isActive) labelColorActive else labelColorInactive,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}