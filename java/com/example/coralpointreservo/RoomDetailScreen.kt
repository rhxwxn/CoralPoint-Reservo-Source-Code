package com.example.coralpointreservo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// New Paginated Pager Indicator component
@Composable
fun PaginatedPagerIndicator(
    pagerState: androidx.compose.foundation.pager.PagerState,
    visibleDots: Int = 5,
    modifier: Modifier = Modifier
) {
    if (pagerState.pageCount > 1) {
        val currentPage = pagerState.currentPage
        val pageCount = pagerState.pageCount
        val effectiveVisibleDots = minOf(visibleDots, pageCount)

        // Calculate the window of visible dot indices
        var startVisibleIndex = currentPage - (effectiveVisibleDots / 2)
        startVisibleIndex = startVisibleIndex.coerceIn(0, (pageCount - effectiveVisibleDots).coerceAtLeast(0))
        val endVisibleIndex = startVisibleIndex + effectiveVisibleDots

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Worm" effect dot that moves smoothy across visible dots
            // (Simplified from-scratch worm effect without dedicated library)
            // Instead of dynamic dots, we'll implement a clean, fixed-max pagination
            // where dots 1-max are displayed, and as you scroll, their interpretation shifts.
            // This satisfies "too many circles" by limiting the visual footprint.

            for (i in 0 until effectiveVisibleDots) {
                // The logical index this dot represents in the window
                val currentLogicalIndex = startVisibleIndex + i
                val isSelected = currentLogicalIndex == currentPage
                val color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                val size = if (isSelected) 10.dp else 8.dp

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(size)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun RoomDetailScreen(
    room: RoomOffer,
    isDateSelected: Boolean = false,
    checkIn: LocalDate? = null,
    checkOut: LocalDate? = null,
    onBackClick: () -> Unit,
    onSelectDatesClick: () -> Unit // Replaces onBookNowClick
) {
    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    val nights = if (isDateSelected && checkIn != null && checkOut != null) {
                        ChronoUnit.DAYS.between(checkIn, checkOut).coerceAtLeast(1)
                    } else {
                        1L
                    }
                    val totalPrice = room.price * nights
                    val formattedPrice = String.format("%,.2f", totalPrice)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("P $formattedPrice", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            if (isDateSelected && nights > 1) {
                                Text("for $nights night(s)", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            } else {
                                Text("/night base rate", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onSelectDatesClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(if (isDateSelected) "Book Now" else "Select Dates", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ) {
                // ==========================================
                // 1. HEADER IMAGE SLIDER & OVERLAYS
                // ==========================================
                Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {

                    // Setup the Pager State based on the number of images
                    val imageCount = if (room.images.isNotEmpty()) room.images.size else 1
                    val pagerState = rememberPagerState(pageCount = { imageCount })

                    // Horizontal Pager for the Slider
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            // Load Cloudinary URL from the list, or fallback to drawable if empty
                            model = if (room.images.isNotEmpty()) room.images[page] else R.drawable.room,
                            contentDescription = "Room Image ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gradient for better text and button visibility
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = 1000f
                        )
                    ))

                    // Back button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(top = 40.dp, start = 16.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Room Title Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp, bottom = 48.dp, end = 24.dp) // Added end padding so text doesn't hit the dots
                    ) {
                        val parts = room.name.split("w/")
                        val mainTitle = parts.firstOrNull()?.trim() ?: room.name
                        val subTitle = if (parts.size > 1) "w/ ${parts[1].trim()}" else ""

                        Text(text = mainTitle, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        if (subTitle.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = subTitle, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = Color.White.copy(alpha = 0.9f))
                        }
                    }

                    // *** MODIFIED PAGINATED INDICATOR ***
                    PaginatedPagerIndicator(
                        pagerState = pagerState,
                        visibleDots = 5, // Show max 5 dots for visual cleanliness
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 56.dp, end = 24.dp)
                    )
                }

                // ==========================================
                // 2. WHITE OVERLAPPING CARD CONTENT
                // ==========================================
                Column(modifier = Modifier
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(24.dp)
                ) {

                    // Display up to 4 accommodation amenities in a 2x2 grid
                    if (room.accommodationAmenities.isNotEmpty()) {
                        val topAmenities = room.accommodationAmenities.take(4)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (topAmenities.size > 0) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AmenityRowItem(icon = getIconForAmenity(topAmenities[0]), text = topAmenities[0])
                                    }
                                }
                                if (topAmenities.size > 1) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AmenityRowItem(icon = getIconForAmenity(topAmenities[1]), text = topAmenities[1])
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (topAmenities.size > 2) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AmenityRowItem(icon = getIconForAmenity(topAmenities[2]), text = topAmenities[2])
                                    }
                                }
                                if (topAmenities.size > 3) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AmenityRowItem(icon = getIconForAmenity(topAmenities[3]), text = topAmenities[3])
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    Text("About this Room", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = room.description.ifEmpty { "No description available." },
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )

                    if (room.roomAmenities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Room Amenities", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        room.roomAmenities.forEach { amenity ->
                            Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                                Text("• ", fontSize = 14.sp, color = Color.DarkGray)
                                Text(amenity, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                            }
                        }
                    }

                    if (room.propertyAmenities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Property Amenities", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        room.propertyAmenities.forEach { amenity ->
                            Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                                Text("• ", fontSize = 14.sp, color = Color.DarkGray)
                                Text(amenity, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                            }
                        }
                    }

                    if (room.extraGuestCharge.isNotEmpty() || room.breakfastRate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        if (room.extraGuestCharge.isNotEmpty()) {
                            Row {
                                Text("Extra guest charge: ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(room.extraGuestCharge, fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (room.breakfastRate.isNotEmpty()) {
                            Row {
                                Text("Breakfast Rate: ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(room.breakfastRate, fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp)) // Extra padding for bottom bar
                }
            }
        }
    }
}

// Helper component
@Composable
fun AmenityRowItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = text, tint = Color(0xFF666666), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 13.sp, color = Color(0xFF666666))
    }
}

fun getIconForAmenity(amenity: String): ImageVector {
    val lower = amenity.lowercase()
    return when {
        lower.contains("air") -> Icons.Default.AcUnit
        lower.contains("wifi") || lower.contains("internet") -> Icons.Default.Wifi
        lower.contains("tv") || lower.contains("television") -> Icons.Default.Tv
        lower.contains("coffee") -> Icons.Default.Coffee
        lower.contains("microwave") -> Icons.Default.Kitchen
        lower.contains("hair") -> Icons.Default.Face
        lower.contains("pool") -> Icons.Default.Pool
        lower.contains("concierge") -> Icons.Default.Person
        lower.contains("elevator") -> Icons.Default.Elevator
        else -> Icons.Default.Check
    }
}