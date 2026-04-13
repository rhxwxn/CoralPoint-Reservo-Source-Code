package com.example.coralpointreservo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// 1. The Data Model (Must have default values for Firestore to work properly)
data class RoomOffer(
    var id: String ="",
    val name: String = "",
    val totalQuantity: Int = 0,
    val price: Double = 0.0,
    val maxGuests: Int = 0,
    val images: List<String> = emptyList(), // Ready for your image URLs later
    val description: String = "",
    val roomAmenities: List<String> = emptyList(),
    val propertyAmenities: List<String> = emptyList(),
    val extraGuestCharge: String = "",
    val breakfastRate: String = "",
    val accommodationAmenities: List<String> = emptyList()
)

// 2. The Auto-Updater Object
object DatabaseSeeder {

    fun uploadInitialRoomsToFirebase() {
        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()
        val roomsCollection = db.collection("rooms")

        // The exact data from your table
        val initialRooms = listOf(
            RoomOffer(
                name = "1 Bedroom Suite w/ Pool View (58sqm)",
                totalQuantity = 2,
                price = 4200.0,
                maxGuests = 4,
                images = (1..20).map { "android.resource://com.example.coralpointreservo/drawable/room_1bed_$it" },
                description = "Escape to our inviting One Bedroom Suite w/ Pool View, designed to provide the perfect blend of comfort and luxury for couples and small families. Accommodating up to 2 adults and 2 children, this suite offers ample space and modern amenities to ensure a relaxing stay.\n\nBook your stay in our One Bedroom Suite at Coralpoint Gardens and immerse yourself in a serene and luxurious escape. Discover the perfect setting for a memorable vacation.",
                roomAmenities = listOf(
                    "Spacious Living: Enjoy the generous room size of 58sqm, thoughtfully designed to provide comfort and relaxation.",
                    "Convenient Kitchenette: Prepare light meals and snacks with ease in the well-equipped kitchenette.",
                    "Private Balcony: Revel in direct pool access (ground floor only) or admire the serene pool views from your private balcony.",
                    "Modern Amenities: Stay comfortable with air conditioning, and stay connected with a Smart TV and free WiFi.",
                    "Bathroom Essentials: Freshen up with the provided basic toiletries in the well-appointed bathroom.",
                    "Complimentary Perks: Enjoy a complimentary coffee set and bottled water, adding a special touch to your stay."
                ),
                propertyAmenities = listOf(
                    "Private Beach Access & Lagoon: Step onto pristine sands and immerse yourself in the crystal-clear waters of our exclusive beach.",
                    "Infinity Pool: Experience endless relaxation and breathtaking views at our stunning infinity pool.",
                    "Saltwater Pool (Under Renovation): Dive into our beautifully maintained saltwater pool, offering a refreshing and rejuvenating experience.",
                    "Lush Gardens: Wander through our landscaped gardens, providing a peaceful oasis surrounded by nature.",
                    "Snackbar: Enjoy light bites and refreshing drinks at our convenient snackbar.",
                    "Exclusive Enclave: Nestled beside the original luxury condominiums of Mactan, our resort offers a tranquil, low-density environment for a serene retreat."
                ),
                extraGuestCharge = "P1,200/person (max of 4 adults)",
                breakfastRate = "Php 250/head (Plated)",
                accommodationAmenities = listOf(
                    "Air-conditioning",
                    "Cable television",
                    "Coffee maker",
                    "Hairdryer",
                    "Microwave",
                    "Minibar",
                    "Slippers",
                    "Wireless internet (WiFi)"
                )
            ),
            RoomOffer(
                name = "2 Bedroom Suite w/ Pool View (115+sqm)",
                totalQuantity = 16,
                price = 8400.0,
                maxGuests = 8,
                images = (1..20).map { "android.resource://com.example.coralpointreservo/drawable/room_2bed_$it" },
                description = "Escape to our luxurious Two Bedroom Suite with Pool View, designed to provide the perfect blend of comfort and elegance for families and small groups. Accommodating up to 4 adults and 3 children, this suite offers ample space and modern amenities to ensure a relaxing stay.\n\nBook your stay in our Two Bedroom Suite at Coralpoint Gardens and immerse yourself in a serene and luxurious escape. Discover the perfect setting for a memorable vacation.",
                roomAmenities = listOf(
                    "Spacious Living: Enjoy the generous room size of 115sqm, thoughtfully designed to provide comfort and relaxation.",
                    "Full Kitchen: Prepare delicious meals with ease in the well-equipped full kitchen.",
                    "Large Balcony: Revel in serene pool views from your spacious balcony.",
                    "Modern Amenities: Stay comfortable with air conditioning, and stay connected with a TV and free WiFi.",
                    "Bathroom Essentials: Freshen up with the provided basic toiletries in the well-appointed bathrooms.",
                    "Complimentary Perks: Enjoy a complimentary coffee set and bottled water, adding a special touch to your stay."
                ),
                propertyAmenities = listOf(
                    "Private Beach Access & Lagoon: Step onto pristine sands and immerse yourself in the crystal-clear waters of our exclusive beach.",
                    "Infinity Pool: Experience endless relaxation and breathtaking views at our stunning infinity pool.",
                    "Saltwater Pool (Under Renovation): Dive into our beautifully maintained saltwater pool, offering a refreshing and rejuvenating experience.",
                    "Lush Gardens: Wander through our landscaped gardens, providing a peaceful oasis surrounded by nature.",
                    "Snackbar: Enjoy light bites and refreshing drinks at our convenient snackbar.",
                    "Exclusive Enclave: Nestled beside the original luxury condominiums of Mactan, our resort offers a tranquil, low-density environment for a serene retreat."
                ),
                extraGuestCharge = "P1,200/person (max of 8 adults)",
                breakfastRate = "Php 250/head (Plated)",
                accommodationAmenities = listOf(
                    "110-120 volt circuits",
                    "Air-conditioning",
                    "Cable television",
                    "Coffee maker",
                    "Hairdryer",
                    "Microwave",
                    "Slippers",
                    "Wireless internet (WiFi)"
                )
            ),
            RoomOffer(
                name = "3 Bedroom Suite w/ Pool View (201sqm)",
                totalQuantity = 2,
                price = 12600.0,
                maxGuests = 10,
                images = (1..16).map { "android.resource://com.example.coralpointreservo/drawable/room_3bed_$it" },
                description = "Escape to our expansive Three Bedroom Suite with Pool View, designed to provide the perfect blend of comfort and luxury for larger families and groups. Accommodating up to 6 adults and 4 children, this suite offers ample space and modern amenities to ensure a relaxing stay.\n\nBook your stay in our Three Bedroom Suite at Coralpoint Gardens and immerse yourself in a serene and luxurious escape. Discover the perfect setting for a memorable vacation.",
                roomAmenities = listOf(
                    "Spacious Living: Enjoy the generous room size of 252sqm, thoughtfully designed to provide comfort and relaxation.",
                    "Full Kitchen + Kitchenette: Prepare delicious meals with ease in the well-equipped full kitchen and an additional kitchenette.",
                    "Spacious Balcony + Rooftop Gazebo: Revel in serene pool views from your private balcony and enjoy exclusive access to a rooftop gazebo (selected units only).",
                    "Modern Amenities: Stay comfortable with air conditioning, and stay connected with a TV and free WiFi.",
                    "Bathroom Essentials: Freshen up with the provided basic toiletries in the well-appointed bathrooms.",
                    "Complimentary Perks: Enjoy a complimentary coffee set and bottled water, adding a special touch to your stay."
                ),
                propertyAmenities = listOf(
                    "Private Beach Access & Lagoon: Step onto pristine sands and immerse yourself in the crystal-clear waters of our exclusive beach.",
                    "Infinity Pool: Experience endless relaxation and breathtaking views at our stunning infinity pool.",
                    "Saltwater Pool (Under Renovation): Dive into our beautifully maintained saltwater pool, offering a refreshing and rejuvenating experience.",
                    "Lush Gardens: Wander through our landscaped gardens, providing a peaceful oasis surrounded by nature.",
                    "Snackbar: Enjoy light bites and refreshing drinks at our convenient snackbar.",
                    "Exclusive Enclave: Nestled beside the original luxury condominiums of Mactan, our resort offers a tranquil, low-density environment for a serene retreat."
                ),
                extraGuestCharge = "P1,200/person (max of 10 adults)",
                breakfastRate = "Php 250/head (Plated)",
                accommodationAmenities = listOf(
                    "220-240 volt circuits",
                    "Air-conditioning",
                    "Coffee maker",
                    "Hairdryer",
                    "Microwave",
                    "Minibar",
                    "Slippers",
                    "Wireless internet (WiFi) - fee"
                )
            )
        )

        // Loop through and automatically write each one to the database
        for (room in initialRooms) {
            // Create a clean document ID by removing special characters
            val documentId = room.name.replace(Regex("[^A-Za-z0-9]"), "_")
                .replace(Regex("_+"), "_")
                .trim('_')
                .lowercase()

            roomsCollection.document(documentId).set(room)
                .addOnSuccessListener {
                    Log.d("FirebaseSeeder", "✅ Successfully uploaded to Firebase: ${room.name}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseSeeder", "❌ Error uploading ${room.name}", e)
                }
        }
    }
}