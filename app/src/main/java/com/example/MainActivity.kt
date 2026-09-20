package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.VaultViewModel
import com.example.ui.components.ForgotPinDialog
import com.example.ui.components.MediaPreviewDialog
import com.example.ui.components.NoteEditDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.LockScreen
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Ignored or soft warning
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.securityManager.vibrateFeedback(isError = true)
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Secret Vault")
            .setSubtitle("Use your fingerprint to access private photos, videos & files")
            .setNegativeButtonText("Use PIN")
            .build()

        fun showBiometricPrompt() {
            try {
                if (viewModel.securityManager.canUseBiometrics() && viewModel.securityManager.isBiometricEnabled()) {
                    biometricPrompt.authenticate(promptInfo)
                }
            } catch (_: Exception) {
                // Ignore biometric prompt errors
            }
        }

        setContent {
            MyApplicationTheme {
                val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
                val isPinSet by viewModel.isPinSet.collectAsStateWithLifecycle()
                val pinInput by viewModel.pinInput.collectAsStateWithLifecycle()
                val pinError by viewModel.pinError.collectAsStateWithLifecycle()
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
                val allItems by viewModel.allItems.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val totalStorage by viewModel.totalStorageFormatted.collectAsStateWithLifecycle()

                val activePreview by viewModel.activePreviewItem.collectAsStateWithLifecycle()
                val showSettings by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
                val showForgotPin by viewModel.showForgotPinDialog.collectAsStateWithLifecycle()
                val showAddNote by viewModel.showAddNoteDialog.collectAsStateWithLifecycle()

                // Toast collector
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collect { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    if (!isUnlocked) {
                        LockScreen(
                            isPinSet = isPinSet,
                            pinInput = pinInput,
                            pinError = pinError,
                            isBiometricSupported = viewModel.isBiometricSupported,
                            isBiometricEnabled = isBiometricEnabled,
                            onDigitClick = { viewModel.onDigitClick(it) },
                            onDeleteClick = { viewModel.onDeleteClick() },
                            onTriggerBiometric = { showBiometricPrompt() },
                            onSetupPin = { pin, confirmPin, q, a ->
                                viewModel.setupInitialPin(pin, confirmPin, q, a)
                            },
                            onForgotPinClick = { viewModel.setShowForgotPinDialog(true) }
                        )
                    } else {
                        VaultHomeScreen(
                            items = filteredItems,
                            allItems = allItems,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            totalStorageFormatted = totalStorage,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onItemClick = { viewModel.setActivePreview(it) },
                            onUnhideItem = { viewModel.unhideItem(it) },
                            onDeleteItem = { viewModel.deleteItem(it) },
                            onShareItem = { item ->
                                val intent = viewModel.getShareIntent(item)
                                if (intent != null) {
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this@MainActivity, "Cannot share file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onLockApp = { viewModel.onLockApp() },
                            onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                            onOpenAddNote = { viewModel.setShowAddNoteDialog(true) },
                            onImportUris = { uris, category ->
                                viewModel.importUris(uris, category)
                            }
                        )
                    }

                    // Dialogs
                    if (activePreview != null) {
                        MediaPreviewDialog(
                            item = activePreview!!,
                            onDismiss = { viewModel.setActivePreview(null) },
                            onUnhide = { viewModel.unhideItem(activePreview!!) },
                            onDelete = { viewModel.deleteItem(activePreview!!) },
                            onShare = {
                                val intent = viewModel.getShareIntent(activePreview!!)
                                if (intent != null) {
                                    startActivity(intent)
                                }
                            },
                            onUpdateNote = { title, content ->
                                viewModel.updateNote(activePreview!!, title, content)
                            }
                        )
                    }

                    if (showSettings) {
                        SettingsDialog(
                            isBiometricSupported = viewModel.isBiometricSupported,
                            isBiometricEnabled = isBiometricEnabled,
                            onToggleBiometric = { viewModel.toggleBiometric(it) },
                            onChangePin = { oldPin, newPin, confirmPin ->
                                viewModel.changePin(oldPin, newPin, confirmPin)
                            },
                            onDismiss = { viewModel.setShowSettingsDialog(false) }
                        )
                    }

                    if (showAddNote) {
                        NoteEditDialog(
                            onDismiss = { viewModel.setShowAddNoteDialog(false) },
                            onSave = { title, content ->
                                viewModel.createNote(title, content)
                            }
                        )
                    }

                    if (showForgotPin) {
                        ForgotPinDialog(
                            securityQuestion = viewModel.securityManager.getRecoveryQuestion(),
                            onRecoverPin = { answer, newPin, confirmPin ->
                                viewModel.recoverPin(answer, newPin, confirmPin)
                            },
                            onDismiss = { viewModel.setShowForgotPinDialog(false) }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Automatically lock the vault when user leaves the app for security
        viewModel.onLockApp()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
