package com.example.coralpointreservo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    room: RoomOffer,
    checkIn: LocalDate,
    checkOut: LocalDate,
    addOns: List<AddOn>,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val totalNights = ChronoUnit.DAYS.between(checkIn, checkOut).coerceAtLeast(1).toInt()
    val roomCost = room.price * totalNights
    val addOnsCost = addOns.sumOf { it.price * it.quantity }
    val totalCost = roomCost + addOnsCost
    val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(totalCost)

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    val dateString = "${checkIn.format(dateFormatter)} Check-in - ${checkOut.format(dateFormatter)} Checkout"
    var showTermsModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val cfg = LocalConfiguration.current
    val compact = cfg.screenWidthDp <= 430
    val maxContent = if (compact) 420.dp else 520.dp
    val imageSize = if (compact) 72.dp else 80.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Payment", fontWeight = FontWeight.Bold, fontSize = 18.sp) } },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp)) } },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContent)
                .padding(horizontal = if (compact) 16.dp else 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BookingStepper(activeStep = 2)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Your Booking", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = room.images.firstOrNull(),
                        contentDescription = room.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(imageSize).clip(RoundedCornerShape(16.dp)).background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = room.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = dateString, fontSize = 12.sp, color = Color.Gray)
                        Text(text = "${room.maxGuests} Guest", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            if (addOns.any { it.quantity > 0 }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Add-ons", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            addOns.filter { it.quantity > 0 }.forEach { addOn ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${addOn.name} x${addOn.quantity}", fontSize = 14.sp, color = Color.Gray)
                                    Text(text = "Php ${String.format("%,.2f", addOn.price * addOn.quantity)}", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Total Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "PHP $formattedTotal", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                        Text(text = "taxes and fees included *", fontSize = 10.sp, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE4E4E7))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val depositCost = totalCost * 0.5
                        val formattedDeposit = NumberFormat.getNumberInstance(Locale.US).format(depositCost)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Required Deposit (50%)", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text(text = "PHP $formattedDeposit", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF054659))
                        }
                    }
                }
            }

            // 🚀 Now simply redirects to the new QR screen
            Button(
                onClick = { showTermsModal = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        }
    }

    if (showTermsModal) {
        TermsAndConditionsModal(
            onDismiss = { showTermsModal = false },
            onProceed = {
                showTermsModal = false
                onContinueClick()
            }
        )
    }
}

@Composable
fun TermsAndConditionsModal(onDismiss: () -> Unit, onProceed: () -> Unit) {
    var isChecked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text("Terms and Conditions", fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Black)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Check-In and Check-Out Policies", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Check-in: 3:00 PM\n• Check-out: 12:00 PM", fontSize = 13.sp, color = Color.DarkGray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("IMPORTANT HOUSE RULES AND RESERVATION POLICIES", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text("House Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• No visitors, speakers, or pets are allowed inside the premises.\n• Security Deposit: A cash deposit of PHP 1,000.00 per room is required upon check-in to cover any incidental or security-related concerns.\n• Children Policy: Children 10 years old and below may stay free of charge.\n• Smoking Policy: Smoking is allowed only on room balconies and in designated smoking areas.\n• Valuables: Please do not leave valuables unattended in pool or beach areas. The hotel is not liable for any loss or damage.", fontSize = 13.sp, color = Color.DarkGray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Exclusive Amenities", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Infinity Pool\n• Saltwater Pool (currently under renovation until further notice)\n• Private Beach Access (2–3 minutes walking distance; across Coralpoint Residence)\n• Ocean Access\n• Private Lagoon", fontSize = 13.sp, color = Color.DarkGray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Reservation & Payment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• A 50% deposit is required to confirm your reservation.\n\n• Email a copy of your deposit slip to info@coralpointgardens.com for verification.", fontSize = 13.sp, color = Color.DarkGray)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Checkbox(checked = isChecked, onCheckedChange = { isChecked = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF054659)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("I have read and agreed to Terms and Condition", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.clickable { isChecked = !isChecked })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onProceed,
                    enabled = isChecked,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659), disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Proceed", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
