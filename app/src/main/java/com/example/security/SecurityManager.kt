package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.biometric.BiometricManager
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun isPinSet(): Boolean {
        return prefs.contains(KEY_PIN_HASH)
    }

    fun setPin(pin: String, recoveryQuestion: String, recoveryAnswer: String): Boolean {
        if (pin.length < 4) return false
        val pinHash = hashString(pin)
        val answerHash = hashString(recoveryAnswer.trim().lowercase())
        prefs.edit()
            .putString(KEY_PIN_HASH, pinHash)
            .putString(KEY_RECOVERY_QUESTION, recoveryQuestion)
            .putString(KEY_RECOVERY_ANSWER_HASH, answerHash)
            .apply()
        _isUnlocked.value = true
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val enteredHash = hashString(pin)
        val matches = storedHash == enteredHash
        if (matches) {
            _isUnlocked.value = true
        }
        return matches
    }

    fun unlockByBiometric() {
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun canUseBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getRecoveryQuestion(): String {
        return prefs.getString(KEY_RECOVERY_QUESTION, "What is your secret word?")
            ?: "What is your secret word?"
    }

    fun verifyRecoveryAnswer(answer: String): Boolean {
        val storedHash = prefs.getString(KEY_RECOVERY_ANSWER_HASH, null) ?: return false
        val enteredHash = hashString(answer.trim().lowercase())
        return storedHash == enteredHash
    }

    fun resetPinWithRecovery(newPin: String): Boolean {
        if (newPin.length < 4) return false
        val pinHash = hashString(newPin)
        prefs.edit().putString(KEY_PIN_HASH, pinHash).apply()
        _isUnlocked.value = true
        return true
    }

    fun vibrateFeedback(isError: Boolean = false) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isError) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80), -1))
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isError) 200 else 40)
                }
            }
        } catch (_: Exception) {
            // gracefully ignore if vibration not permitted
        }
    }

    private fun hashString(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "secret_vault_security_prefs"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_RECOVERY_QUESTION = "key_recovery_question"
        private const val KEY_RECOVERY_ANSWER_HASH = "key_recovery_answer_hash"
    }
}
