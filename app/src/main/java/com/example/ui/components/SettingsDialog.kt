package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.VaultSecondary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary

@Composable
fun SettingsDialog(
    isBiometricSupported: Boolean,
    isBiometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onChangePin: (String, String, String) -> Boolean,
    onDismiss: () -> Unit
) {
    var showChangePinSection by remember { mutableStateOf(false) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var changePinError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = VaultCardDark,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VaultPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = VaultPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Vault Security Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = VaultTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VaultTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Biometric / Fingerprint Option
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = if (isBiometricSupported) VaultPrimary else VaultTextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Fingerprint / Biometric",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = VaultTextPrimary
                                )
                                Text(
                                    text = if (isBiometricSupported) {
                                        "Unlock vault using fingerprint sensor"
                                    } else {
                                        "Biometrics not supported or enrolled on device"
                                    },
                                    fontSize = 11.sp,
                                    color = VaultTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isBiometricEnabled && isBiometricSupported,
                            onCheckedChange = { onToggleBiometric(it) },
                            enabled = isBiometricSupported,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00363D),
                                checkedTrackColor = VaultPrimary,
                                uncheckedThumbColor = VaultTextSecondary,
                                uncheckedTrackColor = VaultCardDark
                            ),
                            modifier = Modifier.testTag("biometric_toggle_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Change PIN section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Password,
                                    contentDescription = null,
                                    tint = VaultSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Change PIN Code",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = VaultTextPrimary
                                )
                            }
                            TextButton(
                                onClick = { showChangePinSection = !showChangePinSection },
                                modifier = Modifier.testTag("change_pin_toggle")
                            ) {
                                Text(if (showChangePinSection) "Cancel" else "Modify", color = VaultPrimary)
                            }
                        }

                        if (showChangePinSection) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = currentPin,
                                onValueChange = { if (it.length <= 6) currentPin = it },
                                label = { Text("Current PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VaultPrimary,
                                    unfocusedBorderColor = VaultBorder,
                                    focusedTextColor = VaultTextPrimary,
                                    unfocusedTextColor = VaultTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { if (it.length <= 6) newPin = it },
                                label = { Text("New PIN (4-6 digits)") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VaultPrimary,
                                    unfocusedBorderColor = VaultBorder,
                                    focusedTextColor = VaultTextPrimary,
                                    unfocusedTextColor = VaultTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPin,
                                onValueChange = { if (it.length <= 6) confirmPin = it },
                                label = { Text("Confirm New PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VaultPrimary,
                                    unfocusedBorderColor = VaultBorder,
                                    focusedTextColor = VaultTextPrimary,
                                    unfocusedTextColor = VaultTextPrimary
                                )
                            )

                            if (changePinError != null) {
                                Text(
                                    text = changePinError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val success = onChangePin(currentPin, newPin, confirmPin)
                                    if (success) {
                                        showChangePinSection = false
                                        currentPin = ""
                                        newPin = ""
                                        confirmPin = ""
                                        changePinError = null
                                    } else {
                                        changePinError = "PIN update failed. Verify inputs."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_new_pin_button")
                            ) {
                                Text("Update PIN", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vault Protection Architecture", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VaultTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Photos, videos, and files are quarantined in sandbox internal storage.\n• Android media scanners cannot index or display hidden items in regular galleries.\n• Protected by cryptographic SHA-256 and Biometric hardware Keystore.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = VaultTextSecondary
                        )
                    }
                }
            }
        }
    }
}
