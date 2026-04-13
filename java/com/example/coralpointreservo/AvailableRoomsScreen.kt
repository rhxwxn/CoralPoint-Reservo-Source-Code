package com.example.coralpointreservo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Helper data class to read Firebase bookings
data class RoomBooking(
    val roomId: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val bookedQuantity: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableRoomsScreen(
    checkIn: LocalDate,
    checkOut: LocalDate,
    guests: Int,
    onDatesChanged: (LocalDate, LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onRoomSelect: (RoomOffer) -> Unit
) {
    var rooms by remember { mutableStateOf<List<RoomOffer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCalendarModal by remember { mutableStateOf(false) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    // This fetches data every time the dates change
    LaunchedEffect(checkIn, checkOut, guests) {
        val db = FirebaseFirestore.getInstance()

        // 1. Fetch Rooms safely
        db.collection("rooms").get()
            .addOnSuccessListener { roomsSnapshot ->
                val allRooms = roomsSnapshot.documents.mapNotNull { doc ->
                    val room = doc.toObject(RoomOffer::class.java)
                    room?.id = doc.id

                    // Failsafe: If your Firebase doesn't have a totalQuantity, give it 1 so it doesn't hide!
                    if (room?.totalQuantity == 0) {
                        room.copy(totalQuantity = 1)
                    } else {
                        room
                    }
                }

                // 2. Fetch Bookings
                db.collection("bookings").get()
                    .addOnSuccessListener { bookingsSnapshot ->
                        val allBookings = bookingsSnapshot.documents.mapNotNull {
                            it.toObject(RoomBooking::class.java)
                        }

                        // 3. Do the availability math
                        val availableRoomsList = allRooms.mapNotNull { room ->
                            val overlappingBookings = allBookings.filter { booking ->
                                booking.roomId == room.id && isDateOverlapping(
                                    searchStart = checkIn,
                                    searchEnd = checkOut,
                                    bookStart = try { LocalDate.parse(booking.checkInDate) } catch (e: Exception) { LocalDate.now() },
                                    bookEnd = try { LocalDate.parse(booking.checkOutDate) } catch (e: Exception) { LocalDate.now() }
                                )
                            }

                            val totalBooked = overlappingBookings.sumOf { it.bookedQuantity }
                            val remainingQuantity = room.totalQuantity - totalBooked

                            if (remainingQuantity > 0) {
                                room.copy(totalQuantity = remainingQuantity)
                            } else {
                                null // Hide room entirely if 0 left for those dates
                            }
                        }

                        rooms = availableRoomsList
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        errorMessage = "Bookings Error: ${e.message}"
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                errorMessage = "Rooms Error: ${e.message}"
                isLoading = false
            }
    }

    if (showCalendarModal) {
        DateSelectionComponent(
            initialCheckIn = checkIn,
            initialCheckOut = checkOut,
            onCloseClick = { showCalendarModal = false },
            onApplyDatesClick = { newCheckIn, newCheckOut ->
                if (newCheckIn != null && newCheckOut != null) {
                    onDatesChanged(newCheckIn, newCheckOut)
                }
                showCalendarModal = false
            }
        )
    } else {
        val cfg = LocalConfiguration.current
        val compact = cfg.screenWidthDp <= 430
        val gap = if (compact) 12.dp else 16.dp
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.White)
                                    .clickable { showCalendarModal = true }
                                    .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Nested Circular Icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFF054659)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.DateRange, 
                                        contentDescription = null, 
                                        tint = Color.White, 
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(gap))
                                Text(
                                    text = checkIn.format(dateFormatter), 
                                    fontSize = if (compact) 14.sp else 15.sp, 
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(gap))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "To",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(gap))
                                Text(
                                    text = checkOut.format(dateFormatter), 
                                    fontSize = if (compact) 14.sp else 15.sp, 
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF054659))
                )
            },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // ==========================================
            // UI STATE MANAGER
            // ==========================================
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF054659)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = "Error loading rooms: $errorMessage",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                rooms.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No rooms available for these dates.", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Try changing your search dates.", fontSize = 14.sp, color = Color.LightGray)
                    }
                }
                else -> {
                    val exactMatches = rooms.filter { it.maxGuests >= guests }
                    val fallbackRooms = rooms.filter { it.maxGuests < guests }
                    val nights = ChronoUnit.DAYS.between(checkIn, checkOut).coerceAtLeast(1)

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (exactMatches.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Search results for $guests guests",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                                )
                            }
                            items(exactMatches) { room ->
                                AvailableRoomCard(room = room, nights = nights, onClick = { onRoomSelect(room) })
                            }
                        } else {
                            // Info Banner
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F7F9)),
                                    border = BorderStroke(1.dp, Color(0xFFBACBDA)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2E4057), modifier = Modifier.padding(top = 2.dp).size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Your filters returned no results, but there are other rooms available for your dates below.",
                                            color = Color(0xFF2E4057),
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }

                            if (fallbackRooms.isNotEmpty()) {
                                item {
                                    Text(
                                        "Discover more options",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(fallbackRooms) { room ->
                                    AvailableRoomCard(room = room, nights = nights, onClick = { onRoomSelect(room) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// 🚀 Helper function to check if dates overlap
fun isDateOverlapping(searchStart: LocalDate, searchEnd: LocalDate, bookStart: LocalDate, bookEnd: LocalDate): Boolean {
    return searchStart.isBefore(bookEnd) && searchEnd.isAfter(bookStart)
}

// ==========================================
// BEAUTIFUL UI COMPONENTS 
// ==========================================

@Composable
fun AvailableRoomCard(room: RoomOffer, nights: Long, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Image 
            AsyncImage(
                model = room.images.firstOrNull(),
                contentDescription = room.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.LightGray)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                val shortName = room.name.split("w/").firstOrNull()?.trim() ?: room.name
                Text(shortName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(6.dp))

                // Guest and Bed text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${room.maxGuests} guests", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Bed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(getBedText(room.name), fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Icons Row (wifi, ac, tv, bathroom, minibar)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AmenityItem(Icons.Default.Wifi, "Wi-fi")
                    AmenityItem(Icons.Default.AcUnit, "A/C")
                    AmenityItem(Icons.Default.Tv, "TV")
                    AmenityItem(Icons.Default.Bathtub, "Bathroom")
                    AmenityItem(Icons.Default.Kitchen, "Minibar")
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Scarcity text
                val scarcityText = if (room.totalQuantity == 1) "Only 1 room left" else "Only ${room.totalQuantity} rooms left"
                Text(scarcityText, color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.height(4.dp))

                // Price & Button
                val totalPrice = room.price * nights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Php ${String.format("%,.0f", totalPrice)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Text("for $nights night(s)", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Select this room", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AmenityItem(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(12.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

fun getBedText(roomName: String): String {
    return if (roomName.contains("3 Bedroom", ignoreCase = true)) "3 beds"
    else if (roomName.contains("2 Bedroom", ignoreCase = true)) "2 beds"
    else "1 king bed"
}
