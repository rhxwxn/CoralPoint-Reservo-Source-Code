package com.example.coralpointreservo

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {

    private val db = FirebaseFirestore.getInstance()

    suspend fun uploadBookingDetails(
    roomName: String,
    checkIn: String,
    checkOut: String,
    guestFirstName: String,
    guestLastName: String,
    guestEmail: String,
    guestPhone: String,
    totalCost: Double,
    receiptUrl: String,
    addOns: List<AddOn>
): String {
    val bookingData = hashMapOf(
        "roomName" to roomName,
        "checkIn" to checkIn,
        "checkOut" to checkOut,
        "guestFirstName" to guestFirstName,
        "guestLastName" to guestLastName,
        "guestEmail" to guestEmail,
        "guestPhone" to guestPhone,
        "totalCost" to totalCost,
        "receiptUrl" to receiptUrl,
        "status" to "pending",
        "addOns" to addOns.map { mapOf("name" to it.name, "price" to it.price, "quantity" to it.quantity) }
    )

        val documentReference = db.collection("bookings").add(bookingData).await()
        return documentReference.id
    }
}
