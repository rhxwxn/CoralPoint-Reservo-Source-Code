package com.example.coralpointreservo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionComponent(
    room: RoomOffer? = null,
    initialCheckIn: LocalDate? = null,
    initialCheckOut: LocalDate? = null,
    onCloseClick: () -> Unit,
    onApplyDatesClick: (checkIn: LocalDate?, checkOut: LocalDate?) -> Unit
) {
    // === STATE MANAGEMENT ===
    var checkInDate by remember { mutableStateOf<LocalDate?>(initialCheckIn) }
    var checkOutDate by remember { mutableStateOf<LocalDate?>(initialCheckOut) }
    var bookedDates by remember { mutableStateOf<List<LocalDate>>(emptyList()) }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }

    // === FIREBASE BOOKINGS ===
    LaunchedEffect(room) {
        if (room != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("bookings").whereEqualTo("roomId", room.id).get()
                .addOnSuccessListener { snapshot ->
                    val bookings = snapshot.documents.mapNotNull { it.toObject(RoomBooking::class.java) }
                    
                    val fullyBooked = mutableListOf<LocalDate>()
                    val maxDate = today.plusYears(1)
                    
                    var currDate = today
                    while(currDate.isBefore(maxDate)) {
                        val count = bookings.filter { b ->
                            val s = try { LocalDate.parse(b.checkInDate) } catch(e: Exception) { today }
                            val e = try { LocalDate.parse(b.checkOutDate) } catch(ex: Exception) { today }
                            !currDate.isBefore(s) && currDate.isBefore(e)
                        }.sumOf { it.bookedQuantity }
                        
                        // Wait, a fully booked date means we cannot START or END our booking on this date?
                        // If it's fully booked, users cannot select it.
                        if (count >= room.totalQuantity) {
                            fullyBooked.add(currDate)
                        }
                        currDate = currDate.plusDays(1)
                    }
                    bookedDates = fullyBooked
                }
        }
    }

    // Handle date selection
    fun onDateClicked(selectedDate: LocalDate) {
        if (bookedDates.contains(selectedDate) || selectedDate.isBefore(today)) return

        if (checkInDate == null) {
            checkInDate = selectedDate
        } else if (checkOutDate == null) {
            if (selectedDate.isBefore(checkInDate)) {
                checkInDate = selectedDate
            } else {
                val hasConflict = bookedDates.any { it.isAfter(checkInDate) && it.isBefore(selectedDate) }
                if (hasConflict) {
                    checkInDate = selectedDate
                } else {
                    checkOutDate = selectedDate
                }
            }
        } else {
            checkInDate = selectedDate
            checkOutDate = null
        }
    }

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Header (Left/Right Navigation + Month/Year)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isPast = currentMonth == YearMonth.now()
                    Box(modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, if (isPast) Color.LightGray else Color(0xFFBACBDA), RoundedCornerShape(8.dp))
                        .clickable(enabled = !isPast) { 
                            currentMonth = currentMonth.minusMonths(1) 
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month", tint = if (isPast) Color.LightGray else Color(0xFF2E4057), modifier = Modifier.size(20.dp))
                    }

                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF054659)
                    )

                    val isTooFar = currentMonth.isAfter(YearMonth.now().plusMonths(11))
                    Box(modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, if (isTooFar) Color.LightGray else Color(0xFFBACBDA), RoundedCornerShape(8.dp))
                        .clickable(enabled = !isTooFar) { 
                            currentMonth = currentMonth.plusMonths(1) 
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month", tint = if (isTooFar) Color.LightGray else Color(0xFF2E4057), modifier = Modifier.size(20.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Days of Week Header
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day, 
                            color = Color(0xFF8B92A0), 
                            fontSize = 14.sp, 
                            modifier = Modifier.weight(1f), 
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calendar Grid
                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfMonth = currentMonth.atDay(1)
                // Sunday = 7 in ISO, so mod 7 makes Sunday 0
                val startOffset = firstDayOfMonth.dayOfWeek.value % 7
                val totalSlots = startOffset + daysInMonth
                val rows = (totalSlots + 6) / 7

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val dayIndex = row * 7 + col
                            val dayNumber = dayIndex - startOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val currentDate = currentMonth.atDay(dayNumber)
                                val isPastDate = currentDate.isBefore(today)
                                val isBooked = bookedDates.contains(currentDate)
                                val isDisabled = isPastDate || isBooked

                                val isCheckIn = currentDate == checkInDate
                                val isCheckOut = currentDate == checkOutDate
                                val isBetween = checkInDate != null && checkOutDate != null &&
                                        currentDate.isAfter(checkInDate) && currentDate.isBefore(checkOutDate)

                                // Mockup specific coloring: solid dark teal for edges, light teal for middle elements
                                val bgColor = when {
                                    isCheckIn || isCheckOut -> Color(0xFF2EBB99)
                                    isBetween -> Color(0xFF67D5B9).copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }

                                val textColor = when {
                                    isCheckIn || isCheckOut || isBetween -> Color.Black
                                    isDisabled -> Color(0xFFBACBDA) // Grayed out logic matching mockup
                                    else -> Color.Black
                                }
                                
                                val shape = when {
                                    // if it's the exact one day checked in and no checkout yet
                                    isCheckIn && checkOutDate == null -> RoundedCornerShape(8.dp)
                                    // continuous layout shapes
                                    isCheckIn -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                    isCheckOut -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                    // the ones in the middle are completely rectangular so they touch edges!
                                    isBetween -> RoundedCornerShape(0.dp)
                                    else -> RoundedCornerShape(4.dp) // empty click bounding box
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(shape) // continuous corners
                                        .background(bgColor)
                                        .clickable(enabled = !isDisabled) { onDateClicked(currentDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = textColor,
                                        fontSize = 16.sp,
                                        fontWeight = if (isCheckIn || isCheckOut || isBetween) FontWeight.Bold else FontWeight.Normal,
                                        textDecoration = if (isBooked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).height(44.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Apply Button
                val isComplete = checkInDate != null && checkOutDate != null
                Button(
                    onClick = { onApplyDatesClick(checkInDate, checkOutDate) },
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .height(48.dp)
                        .widthIn(min = 160.dp),
                    enabled = isComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF054659), disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }