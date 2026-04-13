package com.example.coralpointreservo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// === REFINED PREMIUM COLOR PALETTE ===
val ResortPrimaryTeal = Color(0xFF054659)
val ScreenBackgroundColorWhite = Color(0xFFFFFFFF)
val FieldContainerColor = Color(0xFFFFFFFF)
val FieldUnfocusedBorderColor = Color(0xFFDDDDDD)
val FieldFocusedBorderColor = ResortPrimaryTeal
val FieldHintColor = Color(0xFF999999)
val TextTitleColor = Color(0xFF444444)
val InactiveStepColorLine = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestDetailsScreen(
    onBackClick: () -> Unit,
    onContinueClick: (firstName: String, lastName: String, email: String, phone: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+63 ") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = ResortPrimaryTeal
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, // 🚀 Changed back to Default!
                                contentDescription = "Back",
                                tint = ResortPrimaryTeal,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ScreenBackgroundColorWhite)
                )
                HorizontalDivider(color = InactiveStepColorLine, thickness = 0.5.dp)
            }
        },
        containerColor = ScreenBackgroundColorWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🚀 This will now safely use your ONE shared BookingStepper!
            BookingStepper(activeStep = 1)

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Guest Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextTitleColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            GuestInputField(value = firstName, onValueChange = { firstName = it }, labelText = "First Name *")
            GuestInputField(value = lastName, onValueChange = { lastName = it }, labelText = "Last Name *")
            GuestInputField(value = email, onValueChange = { email = it }, labelText = "Email Address *")
            GuestInputField(value = phone, onValueChange = { phone = it }, labelText = "Phone No. *")

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    if (firstName.isBlank() || lastName.isBlank() || phone.isBlank() || email.isBlank()) {
                        Toast.makeText(context, "Please fill in all details.", Toast.LENGTH_SHORT).show()
                    } else if (!isValidEmail) {
                        Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                    } else {
                        onContinueClick(firstName, lastName, email, phone)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResortPrimaryTeal),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Continue",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 🚀 UPDATED: Uses modern Material 3 OutlinedTextFieldDefaults
@Composable
fun GuestInputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = labelText,
                    color = FieldHintColor,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = if (labelText.contains("Phone")) androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone) else androidx.compose.foundation.text.KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FieldFocusedBorderColor,
                unfocusedBorderColor = FieldUnfocusedBorderColor,
                focusedContainerColor = FieldContainerColor,
                unfocusedContainerColor = FieldContainerColor,
                cursorColor = ResortPrimaryTeal
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}