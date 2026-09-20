package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultKeypadButton
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary
import kotlin.math.roundToInt

@Composable
fun PinDotsIndicator(
    length: Int,
    maxDigits: Int = 4,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(hasError) {
        if (hasError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    Row(
        modifier = modifier
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalDots = if (maxDigits <= 4) 4 else maxDigits
        for (i in 0 until totalDots) {
            val isFilled = i < length
            val color = when {
                hasError -> MaterialTheme.colorScheme.error
                isFilled -> VaultPrimary
                else -> Color.Transparent
            }
            val borderColor = when {
                hasError -> MaterialTheme.colorScheme.error
                isFilled -> VaultPrimary
                else -> VaultBorder
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        Modifier.background(
                            color = borderColor,
                            shape = CircleShape
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isFilled) 16.dp else 12.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) color else VaultKeypadButton)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    showBiometricButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    val keypadRows = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        for (row in keypadRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for ((digit, letters) in row) {
                    KeypadButton(
                        digit = digit,
                        letters = letters,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }

        // Bottom row: Biometric / empty, 0, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBiometricButton && onBiometricClick != null) {
                Surface(
                    onClick = onBiometricClick,
                    shape = CircleShape,
                    color = VaultKeypadButton.copy(alpha = 0.8f),
                    contentColor = VaultPrimary,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("biometric_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Unlock",
                            modifier = Modifier.size(36.dp),
                            tint = VaultPrimary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            KeypadButton(
                digit = "0",
                letters = "+",
                onClick = { onDigitClick("0") }
            )

            Surface(
                onClick = onDeleteClick,
                shape = CircleShape,
                color = VaultKeypadButton.copy(alpha = 0.8f),
                contentColor = VaultTextSecondary,
                modifier = Modifier
                    .size(72.dp)
                    .testTag("delete_pin_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        modifier = Modifier.size(28.dp),
                        tint = VaultTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = VaultKeypadButton,
        contentColor = VaultTextPrimary,
        tonalElevation = 4.dp,
        modifier = Modifier
            .size(72.dp)
            .testTag("keypad_digit_$digit")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.size(72.dp)
        ) {
            Text(
                text = digit,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = VaultTextPrimary
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = VaultTextSecondary
                )
            }
        }
    }
}
