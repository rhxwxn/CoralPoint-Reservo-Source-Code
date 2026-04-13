package com.example.coralpointreservo

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object EmailJSManager {

    private val client = OkHttpClient()
    private val gson = Gson()

    private const val SERVICE_ID = "service_x96v12t"
    private const val TEMPLATE_ID = "template_akuthar"
    private const val PUBLIC_KEY = "j4lVRUA5gLPmjT5G2"

    fun sendBookingEmail(
        userEmail: String,
        userName: String,
        roomName: String,
        checkIn: String,
        checkOut: String,
        totalCost: String,
        bookingRef: String,
        addOns: List<AddOn>,
        onResult: (Boolean, String?) -> Unit
    ) {
        val url = "https://api.emailjs.com/api/v1.0/email/send"

        val recipientEmail = userEmail.trim()
        val addOnsSummary = addOns.filter { it.quantity > 0 }
            .joinToString(", ") { "${it.name} x${it.quantity}" }
            .ifBlank { "None" }

        val cleanTotal = totalCost.replace(Regex("[^\\d.]"), "")
        val totalDouble = cleanTotal.toDoubleOrNull() ?: 0.0
        val depositAmount = totalDouble * 0.5
        val formattedDeposit = "PHP ${String.format("%,.2f", depositAmount)}"

        val templateParams = mapOf(
            "to_email" to recipientEmail,
            "recipient_email" to recipientEmail,
            "recipient" to recipientEmail,
            "email" to recipientEmail,
            "to" to recipientEmail,
            "to_name" to userName,
            "user_email" to recipientEmail,
            "user_name" to userName,
            "room_name" to roomName,
            "check_in" to checkIn,
            "check_out" to checkOut,
            "total_cost" to totalCost,
            "deposit_amount" to formattedDeposit,
            "status" to "Pending Confirmation",
            "booking_reference" to bookingRef,
            "add_ons" to addOnsSummary,
            "reply_to" to recipientEmail,
            "from_name" to "Coralpoint Gardens Suites and Residences"
        )

        val bodyMap = mapOf(
            "service_id" to SERVICE_ID,
            "template_id" to TEMPLATE_ID,
            "user_id" to PUBLIC_KEY,
            "template_params" to templateParams
        )

        val json = gson.toJson(bodyMap)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("origin", "http://localhost")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val message = e.message ?: e.toString()
                Log.e("EmailJS", "Network failure: $message")
                onResult(false, message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("EmailJS", "Email sent successfully")
                    onResult(true, null)
                } else {
                    val body = response.body?.string() ?: ""
                    Log.e("EmailJS", "Error ${response.code}: $body")
                    onResult(false, "Error: ${response.code} - $body")
                }
            }
        })
    }
}
