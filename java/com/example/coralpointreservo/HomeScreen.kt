package com.example.coralpointreservo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate // 🚀 ADDED: Import for dates!

// ==========================================
// 1. DATABASE MODEL
// ==========================================
data class RoomModel(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val discount: String = "",
    val imageUrl: String = ""
)

// ==========================================
// 2. VIEWMODEL
// ==========================================
class HomeViewModel : ViewModel() {
    private val _recommendedRooms = MutableStateFlow<List<RoomModel>>(emptyList())
    val recommendedRooms: StateFlow<List<RoomModel>> = _recommendedRooms

    private val _uiState = MutableStateFlow("Loading rooms...")
    val uiState: StateFlow<String> = _uiState

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchRoomsFromFirebase()
    }

    private fun fetchRoomsFromFirebase() {
        db.collection("rooms")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    _uiState.value = "Connected! But the 'rooms' collection is empty in Firebase."
                    return@addOnSuccessListener
                }

                val roomList = mutableListOf<RoomModel>()
                for (document in result) {
                    try {
                        val room = document.toObject(RoomModel::class.java)
                        val roomWithId = room.copy(id = document.id)
                        roomList.add(roomWithId)
                    } catch (e: Exception) {
                        _uiState.value = "Data Error: Make sure price/rating are Numbers, not Strings!"
                        return@addOnSuccessListener
                    }
                }
                _recommendedRooms.value = roomList
                _uiState.value = "Success"
            }
            .addOnFailureListener { exception ->
                _uiState.value = "Firebase Error: ${exception.message}"
            }
    }
}

// ==========================================
// 3. UI: HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    // 🚀 FIX 1: Tell the screen to expect the Check-in and Check-out dates
    onCheckAvailabilityClick: (LocalDate, LocalDate, Int) -> Unit,
    onRoomClick: (RoomModel) -> Unit
) {
    val recommendedRooms by viewModel.recommendedRooms.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Controls whether our glassy popup is visible!
    var showBookingOverlay by remember { mutableStateOf(false) }

    // Wrapping everything in a Box so the overlay can float on top
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF8F9FA),
            bottomBar = { HomeBottomNavBar() }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(Icons.Default.Menu, contentDescription = "Menu", modifier = Modifier.size(32.dp), tint = Color.Black)
                }

                item {
                    Text("Rooms & Rates", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // --- SEARCH BAR (TRIGGERS OVERLAY) ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .clickable { showBookingOverlay = true } // Triggers the popup
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Search Availability...", color = Color.LightGray, fontSize = 16.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Filter", tint = Color(0xFF0A4D68))
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        item { FeaturedHotelCard("Bedroom Suite", "4.3", "-15%") }
                        item { FeaturedHotelCard("Family Studio", "4.3", "-20%") }
                    }
                }

                item {
                    Text("Recommended", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // --- RECOMMENDED ROOMS LIST ---
                if (uiState != "Success") {
                    item {
                        Text(text = uiState, color = if (uiState == "Loading rooms...") Color.Gray else Color.Red, modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    items(recommendedRooms) { room ->
                        val formattedPrice = "Php ${String.format("%.2f", room.price)}"

                        RecommendedHotelCard(
                            name = room.name,
                            price = formattedPrice,
                            rating = room.rating.toString(),
                            onClick = { onRoomClick(room) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // --- THE POPUP OVERLAY ---
        if (showBookingOverlay) {
            BookingOverlayComponent(
                // 🚀 FIX 2: Catch the dates from the overlay and send them to MainActivity
                onCheckAvailabilityClick = { checkIn, checkOut, guests ->
                    showBookingOverlay = false
                    onCheckAvailabilityClick(checkIn, checkOut, guests)
                },
                // 🚀 FIX 3: Added the missing close action so users can exit the overlay
                onCloseClick = {
                    showBookingOverlay = false
                },
                // 🚀 FIX 4: Handle room clicks from the new overlay
                onRoomClick = { roomOffer ->
                    showBookingOverlay = false
                    // Convert RoomOffer to RoomModel if needed for HomeScreen's callback
                    val model = RoomModel(
                        id = roomOffer.id,
                        name = roomOffer.name,
                        price = roomOffer.price,
                        imageUrl = roomOffer.images.firstOrNull() ?: ""
                    )
                    onRoomClick(model)
                }
            )
        }
    }
}

// ==========================================
// 4. UI: COMPONENTS
// ==========================================
@Composable
fun FeaturedHotelCard(name: String, rating: String, discount: String) {
    Box(modifier = Modifier.width(200.dp).height(260.dp).clip(RoundedCornerShape(24.dp))) {
        Image(painter = painterResource(id = R.drawable.room), contentDescription = "Hotel Image", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)), startY = 300f)))
        Box(modifier = Modifier.padding(16.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(text = discount, color = Color(0xFF8B7355), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = rating, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RecommendedHotelCard(
    name: String,
    price: String,
    rating: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = R.drawable.room), contentDescription = "Hotel", contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF8B7355), fontWeight = FontWeight.Bold)) { append(price) }
                withStyle(style = SpanStyle(color = Color.Gray)) { append("/night") }
            }, fontSize = 14.sp)
        }
        Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF0A4D68)).padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = rating, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun HomeBottomNavBar() {
    NavigationBar(containerColor = Color.White, modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
        NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(28.dp)) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0A4D68), unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.LocationOn, contentDescription = "Cities", modifier = Modifier.size(28.dp)) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0A4D68), unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Favorites", modifier = Modifier.size(28.dp)) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0A4D68), unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Offers", modifier = Modifier.size(28.dp)) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0A4D68), unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent))
    }
}