package com.example.coralpointreservo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BookingOverlayComponent(
    onCheckAvailabilityClick: (checkIn: LocalDate, checkOut: LocalDate, guests: Int) -> Unit,
    onCloseClick: () -> Unit,
    onRoomClick: (RoomOffer) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    // Guest State
    var showGuestDialog by remember { mutableStateOf(false) }
    var adults by remember { mutableStateOf(2) }
    var children by remember { mutableStateOf(0) }

    // Date State
    var checkInDate by remember { mutableStateOf<LocalDate?>(null) }
    var checkOutDate by remember { mutableStateOf<LocalDate?>(null) }

    var showCalendarScreen by remember { mutableStateOf(false) }

    // Firebase Rooms State
    var roomsList by remember { mutableStateOf<List<RoomOffer>>(emptyList()) }
    var isLoadingRooms by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rooms").get()
            .addOnSuccessListener { snapshot ->
                roomsList = snapshot.documents.mapNotNull { it.toObject(RoomOffer::class.java)?.apply { id = it.id } }
                isLoadingRooms = false
            }
            .addOnFailureListener {
                isLoadingRooms = false
            }
    }

    if (showCalendarScreen) {
        DateSelectionComponent(
            initialCheckIn = checkInDate,
            initialCheckOut = checkOutDate,
            onCloseClick = { showCalendarScreen = false },
            onApplyDatesClick = { newCheckIn, newCheckOut ->
                if (newCheckIn != null) checkInDate = newCheckIn
                if (newCheckOut != null) checkOutDate = newCheckOut
                showCalendarScreen = false
            }
        )
    } else {
        Box(modifier = modifier.fillMaxSize().background(Color(0xFFFFFFFF))) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // TOP HEADER
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.coralbg),
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dimming overlay to make sure text is legible
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_white),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxWidth(0.85f).height(60.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        val dateText = if (checkInDate != null && checkOutDate != null) {
                            "${checkInDate!!.format(dateFormatter)} - ${checkOutDate!!.format(dateFormatter)}"
                        } else {
                            "Select Dates"
                        }

                        WhiteInputField(
                            label = "Check in - Check out",
                            value = dateText,
                            icon = Icons.Default.DateRange,
                            onClick = { showCalendarScreen = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        WhiteInputField(
                            label = "Guests",
                            value = "$adults adult/s $children kid/s",
                            icon = Icons.Default.Person,
                            onClick = { showGuestDialog = true }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (checkInDate != null && checkOutDate != null) {
                                    onCheckAvailabilityClick(checkInDate!!, checkOutDate!!, adults + children)
                                }
                            },
                            enabled = checkInDate != null && checkOutDate != null,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF054659),
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Check Availability", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // FIND ROOMS SECTION
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text(
                        text = "Our Rooms",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoadingRooms) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF054659))
                        }
                    } else if (roomsList.isEmpty()) {
                        Text(
                            text = "No rooms found.",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(roomsList) { room ->
                                MiniRoomCard(room = room, onClick = { onRoomClick(room) })
                            }
                        }
                    }
                }
            }

            if (showGuestDialog) {
                GuestSelectionDialog(adults, children, onDismiss = { showGuestDialog = false }, onSave = { a, c -> adults = a; children = c; showGuestDialog = false })
            }
        }
    }
}

// === Helper Components ===

@Composable
fun WhiteInputField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label on left
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(16.dp))
        // Value trailing to the right
        Text(text = value, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Spacer(modifier = Modifier.width(8.dp))
        // Icon at the very end
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun MiniRoomCard(room: RoomOffer, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(220.dp)
            .height(240.dp)
            .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            AsyncImage(
                model = room.images.firstOrNull(),
                contentDescription = room.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFE4E4E7))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = room.name.split("w/").firstOrNull()?.trim() ?: room.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Max Guests Chip
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp)).background(Color.White).padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF054659))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${room.maxGuests}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF054659))
                    }

                    Text(
                        text = "P ${String.format("%,.2f", room.price)} /night",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun GuestSelectionDialog(initialAdults: Int, initialChildren: Int, onDismiss: () -> Unit, onSave: (Int, Int) -> Unit) {
    var tempAdults by remember { mutableStateOf(initialAdults) }
    var tempChildren by remember { mutableStateOf(initialChildren) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).offset(x = 8.dp, y = (-8).dp).clickable { onDismiss() })
                Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(top = 8.dp)) {
                    CounterRow("Adults", "Ages 18 or above", tempAdults, 1, { tempAdults-- }, { tempAdults++ })
                    CounterRow("Children", "Ages 0-17", tempChildren, 0, { tempChildren-- }, { tempChildren++ })
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = { onSave(tempAdults, tempChildren) }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)), shape = RoundedCornerShape(8.dp)) {
                        Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CounterRow(title: String, subtitle: String?, value: Int, minValue: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            if (subtitle != null) { Spacer(modifier = Modifier.height(4.dp)); Text(subtitle, color = Color(0xFF8B92A0), fontSize = 12.sp) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularIconButton("−", value > minValue, onDecrement)
            Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            CircularIconButton("+", true, onIncrement)
        }
    }
}

@Composable
fun CircularIconButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val contentColor = if (enabled) Color(0xFF0B66C2) else Color(0xFFA1A1AA)
    val containerColor = if (enabled) Color.White else Color(0xFFF4F4F5)
    val borderColor = if (enabled) Color(0xFFE4E4E7) else Color.Transparent
    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(containerColor).border(1.dp, borderColor, CircleShape).clickable(enabled = enabled) { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = contentColor, fontSize = 24.sp, modifier = Modifier.offset(y = (-2).dp))
    }
}