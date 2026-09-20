package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultCardDark
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary

@Composable
fun ForgotPinDialog(
    securityQuestion: String,
    onRecoverPin: (String, String, String) -> Boolean,
    onDismiss: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VaultCardDark,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Help, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Vault PIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = VaultTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VaultTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Security Question:",
                            fontSize = 11.sp,
                            color = VaultTextSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = securityQuestion,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VaultTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = answer,
                    onValueChange = {
                        answer = it
                        errorMessage = null
                    },
                    label = { Text("Your Recovery Answer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recovery_answer_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VaultPrimary,
                        unfocusedBorderColor = VaultBorder,
                        focusedTextColor = VaultTextPrimary,
                        unfocusedTextColor = VaultTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 6) newPin = it
                        errorMessage = null
                    },
                    label = { Text("New PIN (4-6 digits)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_new_pin_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VaultPrimary,
                        unfocusedBorderColor = VaultBorder,
                        focusedTextColor = VaultTextPrimary,
                        unfocusedTextColor = VaultTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 6) confirmPin = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_confirm_pin_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VaultPrimary,
                        unfocusedBorderColor = VaultBorder,
                        focusedTextColor = VaultTextPrimary,
                        unfocusedTextColor = VaultTextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF4D4F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (answer.isBlank()) {
                            errorMessage = "Please enter your recovery answer"
                            return@Button
                        }
                        if (newPin.length < 4) {
                            errorMessage = "New PIN must be at least 4 digits"
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            errorMessage = "PINs do not match"
                            return@Button
                        }
                        val success = onRecoverPin(answer, newPin, confirmPin)
                        if (!success) {
                            errorMessage = "Incorrect recovery answer"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_recovery_button")
                ) {
                    Text("Reset PIN & Unlock", fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                }
            }
        }
    }
}
