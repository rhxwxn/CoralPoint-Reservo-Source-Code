package com.example.coralpointreservo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailsScreen(
    room: RoomOffer,
    checkIn: LocalDate,
    checkOut: LocalDate,
    onBackClick: () -> Unit,
    onContinueClick: (List<AddOn>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedAddOn by remember { mutableStateOf<AddOn?>(null) }
    val addOns = remember {
        mutableStateListOf(
            AddOn("Breakfast Package", 250.00),
            AddOn("Guest", 1200.00)
        )
    }

    // 1. Calculate Nights and Total Cost
    val totalNights = ChronoUnit.DAYS.between(checkIn, checkOut).coerceAtLeast(1).toInt()
    val roomCost = room.price * totalNights
    val addOnsCost = addOns.sumOf { it.price * it.quantity }
    val totalCost = roomCost + addOnsCost
    val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(room.price)
    val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(totalCost)

    // 2. Format Dates for the UI
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Reservation Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) }, // Balances the center title
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        // 🚀 Removed the bottomBar from here
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        val cfg = LocalConfiguration.current
        val compact = cfg.screenWidthDp <= 430
        val maxContent = if (compact) 420.dp else 520.dp
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContent)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (compact) 16.dp else 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==========================================
            // ROOM DETAILS CARD
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // Blue border top effect is handled by clipping the image and the card outline
                    AsyncImage(
                        model = room.images.firstOrNull(),
                        contentDescription = room.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.LightGray)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = room.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Guests & Beds
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${room.maxGuests} guests", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "1 king bed", fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Amenities
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AmenityItemMini(Icons.Default.Wifi, "Wi-Fi")
                                    AmenityItemMini(Icons.Default.AcUnit, "A/C")
                                    AmenityItemMini(Icons.Default.Tv, "TV")
                                    AmenityItemMini(Icons.Default.Bathtub, "Bathroom")
                                    AmenityItemMini(Icons.Default.LocalBar, "Minibar")
                                }
                                Row {
                                    AmenityItemMini(Icons.Default.Lock, "In - room safe")
                                }
                            }
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "More", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ==========================================
            // RESERVATION DATES
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Reservation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)

                // Check-In Card
                DateCard(
                    day = checkIn.format(dayFormatter),
                    month = checkIn.format(monthFormatter),
                    title = "Check-in",
                    subtitle = "${checkIn.format(dayOfWeekFormatter)} 3:00 PM"
                )

                // Check-Out Card
                DateCard(
                    day = checkOut.format(dayFormatter),
                    month = checkOut.format(monthFormatter),
                    title = "Check-out",
                    subtitle = "${checkOut.format(dayOfWeekFormatter)} 12:00 PM"
                )
            }

            // ==========================================
            // ADD ONS
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Add ons", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        addOns.forEach { addOn ->
                            AddOnItem(addOn = addOn, onAddClick = {
                                selectedAddOn = addOn
                                showDialog = true
                            })
                        }
                    }
                }
            }

            // ==========================================
            // PRICE SUMMARY & TOTAL
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Price Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                Text(text = "Php $formattedPrice × $totalNights night${if (totalNights > 1) "s" else ""}", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = "Total Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "PHP $formattedTotal", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.Black)
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

            // ==========================================
            // CONTINUE BUTTON
            // ==========================================
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onContinueClick(addOns.toList()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Continue to Payment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }

    if (showDialog && selectedAddOn != null) {
        var quantity by remember { mutableStateOf(selectedAddOn!!.quantity) }
        QuantityModal(
            quantity = quantity,
            onQuantityChange = { newQuantity ->
                if (newQuantity >= 0) {
                    quantity = newQuantity
                }
            },
            onConfirm = {
                val index = addOns.indexOf(selectedAddOn)
                if (index != -1) {
                    addOns[index] = selectedAddOn!!.copy(quantity = quantity)
                }
                showDialog = false
                selectedAddOn = null
            },
            onCancel = {
                showDialog = false
                selectedAddOn = null
            }
        )
    }
}

// Helper Composable for the Date Cards
@Composable
fun DateCard(day: String, month: String, title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
                Text(text = day, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.Black)
                Text(text = month, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }

            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = Color.Gray, modifier = Modifier.size(24.dp))
        }
    }
}

// Helper Composable for the tiny amenity text in the Room Card
@Composable
fun AmenityItemMini(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(10.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
    }
}

@Composable
fun AddOnItem(addOn: AddOn, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = addOn.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Php ${String.format("%,.2f", addOn.price)}", fontSize = 14.sp, color = Color.Gray)
        }
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.AddCircle, contentDescription = "Add")
        }
    }
}

@Composable
fun QuantityModal(quantity: Int, onQuantityChange: (Int) -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit) {
    // We use a raw Dialog instead of AlertDialog for exact padding control
    androidx.compose.ui.window.Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp) // Outer white padding
            ) {
                // 1. Gray Top Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E2E2), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // 2. White Pill Counter (Matches the image exactly!)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White, shape = RoundedCornerShape(50)) // Fully rounded pill
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "-",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { if (quantity > 0) onQuantityChange(quantity - 1) }
                                .padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = quantity.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onQuantityChange(quantity + 1) }
                                .padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 4.dp), // Aligned right with slight bottom breathing room
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF424242)
                        ),
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
