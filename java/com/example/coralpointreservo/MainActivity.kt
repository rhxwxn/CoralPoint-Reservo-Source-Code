package com.example.coralpointreservo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import android.os.Handler
import android.os.Looper

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.coralpointreservo.ui.theme.CoralPointReservoTheme
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.util.Patterns

import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CloudinaryManager.initialize(this)

        // 🚀 We added this line for you:
        // Run this ONCE to update your Firebase! You can delete this line after running the app.
        DatabaseSeeder.uploadInitialRoomsToFirebase()

        setContent {
            CoralPointReservoTheme {
                CoralPointApp()
            }
        }
    }
}

@Composable
fun CoralPointApp() {
    // 1. Navigation & System State
    var currentScreen by rememberSaveable { mutableStateOf("splash") }
    var confirmedBookingRef by rememberSaveable { mutableStateOf("") }

    // Using a strict type (RoomOffer?) instead of Any? to prevent crashes
    var selectedRoom by remember { mutableStateOf<RoomOffer?>(null) }

    // 2. Booking Search State
    var searchCheckIn by remember { mutableStateOf(LocalDate.now()) }
    var searchCheckOut by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var searchGuests by rememberSaveable { mutableStateOf(2) }
    var isDateSelected by rememberSaveable { mutableStateOf(false) }

    // 3. Guest Details State
    var guestFirstName by rememberSaveable { mutableStateOf("") }
    var guestLastName by rememberSaveable { mutableStateOf("") }
    var guestEmail by rememberSaveable { mutableStateOf("") }
    var guestPhone by rememberSaveable { mutableStateOf("") }
    var addOns by remember { mutableStateOf<List<AddOn>>(emptyList()) }
    var emailSendError by rememberSaveable { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "splash" -> {
            SplashScreen(onTimeout = { currentScreen = "booking_overlay" })
        }

        "booking_overlay" -> {
            BookingOverlayComponent(
                onCheckAvailabilityClick = { checkIn, checkOut, guests ->
                    searchCheckIn = checkIn
                    searchCheckOut = checkOut
                    searchGuests = guests
                    isDateSelected = true
                    currentScreen = "available_rooms"
                },
                onCloseClick = { currentScreen = "home" },
                onRoomClick = { room ->
                    selectedRoom = room
                    isDateSelected = false
                    currentScreen = "details"
                }
            )
        }

        "home" -> {
            /* Insert your Home Screen Composable here */
        }

        "available_rooms" -> {
            AvailableRoomsScreen(
                checkIn = searchCheckIn,
                checkOut = searchCheckOut,
                guests = searchGuests,
                onDatesChanged = { newIn, newOut ->
                    searchCheckIn = newIn
                    searchCheckOut = newOut
                },
                onBackClick = { currentScreen = "booking_overlay" },
                onRoomSelect = { roomAny ->
                    selectedRoom = if (roomAny is RoomOffer) {
                        roomAny
                    } else {
                        val model = roomAny as RoomModel
                        RoomOffer(
                            id = model.id,
                            name = model.name,
                            price = model.price,
                            maxGuests = 2,
                            images = listOf(model.imageUrl)
                        )
                    }
                    currentScreen = "details"
                }
            )
        }

        "details" -> {
            selectedRoom?.let { room ->
                RoomDetailScreen(
                    room = room,
                    isDateSelected = isDateSelected,
                    checkIn = searchCheckIn,
                    checkOut = searchCheckOut,
                    onBackClick = { currentScreen = "booking_overlay" },
                    onSelectDatesClick = { 
                        if (isDateSelected) {
                            currentScreen = "guest_details"
                        } else {
                            currentScreen = "select_dates" 
                        }
                    }
                )
            }
        }

        "select_dates" -> {
            DateSelectionComponent(
                room = selectedRoom,
                onCloseClick = { currentScreen = "details" },
                onApplyDatesClick = { newCheckIn, newCheckOut ->
                    if (newCheckIn != null) searchCheckIn = newCheckIn
                    if (newCheckOut != null) searchCheckOut = newCheckOut
                    isDateSelected = true
                    currentScreen = "guest_details"
                }
            )
        }

        "guest_details" -> {
            if (selectedRoom != null) {
                GuestDetailsScreen(
                    onBackClick = { currentScreen = "details" },
                    onContinueClick = { firstName, lastName, email, phone ->
                        guestFirstName = firstName
                        guestLastName = lastName
                        guestEmail = email
                        guestPhone = phone

                        currentScreen = "reservation_details"
                    }
                )
            }
        }

        "reservation_details" -> {
            selectedRoom?.let { room ->
                ReservationDetailsScreen(
                    room = room,
                    checkIn = searchCheckIn,
                    checkOut = searchCheckOut,
                    onBackClick = { currentScreen = "guest_details" },
                    onContinueClick = { selectedAddOns ->
                        addOns = selectedAddOns
                        currentScreen = "payment"
                    }
                )
            }
        }

        "payment" -> {
            selectedRoom?.let { room ->
                PaymentScreen(
                    room = room,
                    checkIn = searchCheckIn,
                    checkOut = searchCheckOut,
                    addOns = addOns,
                    onBackClick = { currentScreen = "reservation_details" },
                    onContinueClick = { currentScreen = "qr_payment" }
                )
            }
        }

        "qr_payment" -> {
            var isUploading by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            QrPaymentScreen(
                onBackClick = { currentScreen = "payment" },
                isProcessing = isUploading,
                onUploadReceiptClick = { receiptUri ->
                    isUploading = true
                    scope.launch {
                        val imageUrl = CloudinaryManager.uploadImage(receiptUri)
                        if (imageUrl != null) {
                            val totalNights = java.time.temporal.ChronoUnit.DAYS.between(searchCheckIn, searchCheckOut).coerceAtLeast(1).toInt()
                            val roomCost = (selectedRoom?.price ?: 0.0) * totalNights
                            val addOnsCost = addOns.sumOf { it.price * it.quantity }
                            val totalCost = roomCost + addOnsCost

                            val bookingId = FirebaseManager.uploadBookingDetails(
                                roomName = selectedRoom?.name ?: "Unknown",
                                checkIn = searchCheckIn.toString(),
                                checkOut = searchCheckOut.toString(),
                                guestFirstName = guestFirstName,
                                guestLastName = guestLastName,
                                guestEmail = guestEmail,
                                guestPhone = guestPhone,
                                totalCost = totalCost,
                                receiptUrl = imageUrl,
                                addOns = addOns
                            )

                            // 🚀 Send Email Notification via EmailJS
                            val normalizedGuestEmail = guestEmail.trim()
                            if (!Patterns.EMAIL_ADDRESS.matcher(normalizedGuestEmail).matches()) {
                                emailSendError = "Email failed for $guestEmail\n\nInvalid email address. Please check the Guest Details email field."
                            } else {
                            EmailJSManager.sendBookingEmail(
                                userEmail = normalizedGuestEmail,
                                userName = "$guestFirstName $guestLastName",
                                roomName = selectedRoom?.name ?: "Unknown",
                                checkIn = searchCheckIn.toString(),
                                checkOut = searchCheckOut.toString(),
                                totalCost = "PHP ${String.format("%,.2f", totalCost)}",
                                bookingRef = bookingId,
                                addOns = addOns,
                                onResult = { success, error ->
                                    if (success) {
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(context, "We emailed your booking details.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        android.util.Log.e("EmailJS", "Error: $error")
                                        Handler(Looper.getMainLooper()).post {
                                            val details = error ?: "Unknown error"
                                            emailSendError = "Email failed for $normalizedGuestEmail\n\n$details"
                                        }
                                    }
                                }
                            )
                            }

                            confirmedBookingRef = bookingId
                            currentScreen = "confirmation"
                        } else {
                            Toast.makeText(context, "Failed to upload receipt.", Toast.LENGTH_SHORT).show()
                        }
                        isUploading = false
                    }
                }
            )
        }

        "booking_request_sent" -> {
            BookingRequestSentScreen(
                onBackClick = { currentScreen = "qr_payment" },
                onBackHomeClick = { 
                    selectedRoom = null
                    guestFirstName = ""
                    guestLastName = ""
                    guestEmail = ""
                    guestPhone = ""
                    confirmedBookingRef = ""
                    searchCheckIn = LocalDate.now()
                    searchCheckOut = LocalDate.now().plusDays(1)
                    currentScreen = "booking_overlay" 
                }
            )
        }

        "confirmation" -> {
            ConfirmationScreen(
                bookingReference = confirmedBookingRef,
                onBackHomeClick = {
                    selectedRoom = null
                    guestFirstName = ""
                    guestLastName = ""
                    guestEmail = ""
                    guestPhone = ""
                    confirmedBookingRef = ""
                    searchCheckIn = LocalDate.now()
                    searchCheckOut = LocalDate.now().plusDays(1)
                    currentScreen = "booking_overlay"
                }
            )
        }
    }

    val clipboardManager = LocalClipboardManager.current
    val errorMessage = emailSendError
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { emailSendError = null },
            title = { Text("Email failed") },
            text = {
                SelectionContainer {
                    Text(errorMessage)
                }
            },
            confirmButton = {
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(errorMessage)) }) {
                    Text("Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { emailSendError = null }) {
                    Text("Close")
                }
            }
        )
    }
}
