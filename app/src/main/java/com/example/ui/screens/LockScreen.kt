package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PinDotsIndicator
import com.example.ui.components.PinKeypad
import com.example.ui.theme.VaultBgDark
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultCardDark
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultSecondary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    isPinSet: Boolean,
    pinInput: String,
    pinError: String?,
    isBiometricSupported: Boolean,
    isBiometricEnabled: Boolean,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onTriggerBiometric: () -> Unit,
    onSetupPin: (String, String, String, String) -> Boolean,
    onForgotPinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Setup mode states
    var setupStep by remember { mutableStateOf(1) } // 1: choose PIN, 2: confirm PIN, 3: recovery question
    var initialPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var recoveryQuestion by remember { mutableStateOf("What is your favorite secret word?") }
    var recoveryAnswer by remember { mutableStateOf("") }
    var setupError by remember { mutableStateOf<String?>(null) }
    var questionDropdownExpanded by remember { mutableStateOf(false) }

    val recoveryQuestions = listOf(
        "What is your favorite secret word?",
        "What is your childhood pet's name?",
        "What city were you born in?",
        "What is your mother's maiden name?",
        "What was your first school name?"
    )

    // Trigger biometric automatically on screen open if PIN is already set & biometric enabled
    LaunchedEffect(isPinSet) {
        if (isPinSet && isBiometricSupported && isBiometricEnabled) {
            onTriggerBiometric()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VaultBgDark)
    ) {
        if (!isPinSet) {
            // First time setup wizard
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Shield Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VaultPrimary.copy(alpha = 0.2f), VaultSecondary.copy(alpha = 0.2f))
                            )
                        )
                        .border(1.5.dp, VaultPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = VaultPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Secret Vault",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = VaultTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Keep your photos, videos, and private files protected with PIN and fingerprint security.",
                    fontSize = 13.sp,
                    color = VaultTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Wizard Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultCardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (setupStep) {
                            1 -> {
                                Text(
                                    text = "Step 1 of 3: Set Your Vault PIN",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VaultPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Enter 4 to 6 digits to lock your vault",
                                    fontSize = 12.sp,
                                    color = VaultTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                PinDotsIndicator(
                                    length = initialPin.length,
                                    maxDigits = 4,
                                    hasError = setupError != null
                                )

                                if (setupError != null) {
                                    Text(
                                        text = setupError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                PinKeypad(
                                    onDigitClick = { digit ->
                                        if (initialPin.length < 6) {
                                            initialPin += digit
                                            setupError = null
                                            if (initialPin.length >= 4) {
                                                // auto proceed on 4 digits or button
                                            }
                                        }
                                    },
                                    onDeleteClick = {
                                        if (initialPin.isNotEmpty()) {
                                            initialPin = initialPin.dropLast(1)
                                            setupError = null
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (initialPin.length < 4) {
                                            setupError = "PIN must be at least 4 digits"
                                        } else {
                                            setupStep = 2
                                            setupError = null
                                        }
                                    },
                                    enabled = initialPin.length >= 4,
                                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("step1_continue_button")
                                ) {
                                    Text("Continue", fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                                }
                            }

                            2 -> {
                                Text(
                                    text = "Step 2 of 3: Confirm Vault PIN",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VaultPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Re-enter your PIN to confirm",
                                    fontSize = 12.sp,
                                    color = VaultTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                PinDotsIndicator(
                                    length = confirmPin.length,
                                    maxDigits = initialPin.length,
                                    hasError = setupError != null
                                )

                                if (setupError != null) {
                                    Text(
                                        text = setupError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                PinKeypad(
                                    onDigitClick = { digit ->
                                        if (confirmPin.length < initialPin.length) {
                                            confirmPin += digit
                                            setupError = null
                                            if (confirmPin.length == initialPin.length) {
                                                if (confirmPin == initialPin) {
                                                    setupStep = 3
                                                    setupError = null
                                                } else {
                                                    setupError = "PINs do not match. Try again."
                                                    confirmPin = ""
                                                }
                                            }
                                        }
                                    },
                                    onDeleteClick = {
                                        if (confirmPin.isNotEmpty()) {
                                            confirmPin = confirmPin.dropLast(1)
                                            setupError = null
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                TextButton(onClick = {
                                    setupStep = 1
                                    confirmPin = ""
                                    initialPin = ""
                                }) {
                                    Text("Back to Step 1", color = VaultTextSecondary)
                                }
                            }

                            3 -> {
                                Text(
                                    text = "Step 3 of 3: Security Question",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VaultPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Used to recover your PIN if you ever forget it",
                                    fontSize = 12.sp,
                                    color = VaultTextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                ExposedDropdownMenuBox(
                                    expanded = questionDropdownExpanded,
                                    onExpandedChange = { questionDropdownExpanded = !questionDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = recoveryQuestion,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Choose Question") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionDropdownExpanded) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = VaultPrimary,
                                            unfocusedBorderColor = VaultBorder,
                                            focusedTextColor = VaultTextPrimary,
                                            unfocusedTextColor = VaultTextPrimary
                                        ),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = questionDropdownExpanded,
                                        onDismissRequest = { questionDropdownExpanded = false }
                                    ) {
                                        for (q in recoveryQuestions) {
                                            DropdownMenuItem(
                                                text = { Text(q) },
                                                onClick = {
                                                    recoveryQuestion = q
                                                    questionDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = recoveryAnswer,
                                    onValueChange = {
                                        recoveryAnswer = it
                                        setupError = null
                                    },
                                    label = { Text("Your Secret Answer") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("recovery_answer_setup_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VaultPrimary,
                                        unfocusedBorderColor = VaultBorder,
                                        focusedTextColor = VaultTextPrimary,
                                        unfocusedTextColor = VaultTextPrimary
                                    )
                                )

                                if (setupError != null) {
                                    Text(
                                        text = setupError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                Button(
                                    onClick = {
                                        if (recoveryAnswer.isBlank()) {
                                            setupError = "Please enter an answer to recover your PIN"
                                        } else {
                                            val success = onSetupPin(initialPin, confirmPin, recoveryQuestion, recoveryAnswer)
                                            if (!success) {
                                                setupError = "Failed to initialize PIN"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("finish_setup_button")
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00363D))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Complete Setup & Open Vault", fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Standard Unlock Lockscreen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header & status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(VaultCardDark)
                            .border(1.5.dp, VaultPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = VaultPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Secret Vault",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = VaultTextPrimary
                    )

                    Text(
                        text = "Enter your PIN or use fingerprint",
                        fontSize = 13.sp,
                        color = VaultTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dots
                    PinDotsIndicator(
                        length = pinInput.length,
                        maxDigits = 4,
                        hasError = pinError != null
                    )

                    // Error text if any
                    AnimatedVisibility(
                        visible = pinError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = pinError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                // Keypad
                PinKeypad(
                    onDigitClick = onDigitClick,
                    onDeleteClick = onDeleteClick,
                    onBiometricClick = onTriggerBiometric,
                    showBiometricButton = isBiometricSupported && isBiometricEnabled,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Forgot PIN option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onForgotPinClick,
                        modifier = Modifier.testTag("forgot_pin_button")
                    ) {
                        Text(
                            text = "Forgot PIN?",
                            color = VaultTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
