package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VaultCategory
import com.example.data.VaultDatabase
import com.example.data.VaultItem
import com.example.data.VaultRepository
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VaultDatabase.getInstance(application)
    private val repository = VaultRepository(application, database.vaultDao())
    val securityManager = SecurityManager(application)

    val isUnlocked: StateFlow<Boolean> = securityManager.isUnlocked

    private val _isPinSet = MutableStateFlow(securityManager.isPinSet())
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VaultCategory?>(null)
    val selectedCategory: StateFlow<VaultCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _activePreviewItem = MutableStateFlow<VaultItem?>(null)
    val activePreviewItem: StateFlow<VaultItem?> = _activePreviewItem.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showForgotPinDialog = MutableStateFlow(false)
    val showForgotPinDialog: StateFlow<Boolean> = _showForgotPinDialog.asStateFlow()

    private val _showAddNoteDialog = MutableStateFlow(false)
    val showAddNoteDialog: StateFlow<Boolean> = _showAddNoteDialog.asStateFlow()

    private val _editingNoteItem = MutableStateFlow<VaultItem?>(null)
    val editingNoteItem: StateFlow<VaultItem?> = _editingNoteItem.asStateFlow()

    private val _showImportSheet = MutableStateFlow(false)
    val showImportSheet: StateFlow<Boolean> = _showImportSheet.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val isBiometricSupported = securityManager.canUseBiometrics()
    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    val allItems: StateFlow<List<VaultItem>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredItems: StateFlow<List<VaultItem>> = combine(
        allItems,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        var list = items
        if (category != null) {
            list = list.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.originalFileName.lowercase().contains(q) ||
                        (it.noteContent?.lowercase()?.contains(q) == true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStorageFormatted: StateFlow<String> = repository.getTotalVaultSizeBytes().combine(
        allItems
    ) { size, _ ->
        formatFileSize(size ?: 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0 B")

    fun onDigitClick(digit: String) {
        if (_pinInput.value.length < 6) {
            _pinInput.value += digit
            _pinError.value = null
            securityManager.vibrateFeedback(isError = false)

            if (_pinInput.value.length in 4..6 && _isPinSet.value) {
                // If length reaches 4, check if it matches the stored PIN
                if (securityManager.verifyPin(_pinInput.value)) {
                    _pinInput.value = ""
                    _pinError.value = null
                } else if (_pinInput.value.length == 6) {
                    // Maximum length reached and didn't match
                    securityManager.vibrateFeedback(isError = true)
                    _pinError.value = "Incorrect PIN. Try again."
                    _pinInput.value = ""
                }
            }
        }
    }

    fun onDeleteClick() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _pinError.value = null
            securityManager.vibrateFeedback(isError = false)
        }
    }

    fun onClearPin() {
        _pinInput.value = ""
        _pinError.value = null
    }

    fun onBiometricSuccess() {
        securityManager.unlockByBiometric()
        _pinInput.value = ""
        _pinError.value = null
    }

    fun onLockApp() {
        securityManager.lock()
        _pinInput.value = ""
        _pinError.value = null
    }

    fun setupInitialPin(pin: String, confirmPin: String, question: String, answer: String): Boolean {
        if (pin.length < 4) {
            _pinError.value = "PIN must be at least 4 digits"
            return false
        }
        if (pin != confirmPin) {
            _pinError.value = "PINs do not match"
            return false
        }
        if (answer.isBlank()) {
            _pinError.value = "Please enter an answer to the recovery question"
            return false
        }
        val success = securityManager.setPin(pin, question, answer)
        if (success) {
            _isPinSet.value = true
            _pinInput.value = ""
            _pinError.value = null
            emitToast("Vault PIN created successfully!")
        }
        return success
    }

    fun selectCategory(category: VaultCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun importUris(uris: List<Uri>, forcedCategory: VaultCategory? = null) {
        viewModelScope.launch {
            var count = 0
            for (uri in uris) {
                val result = repository.importUri(uri, forcedCategory)
                if (result.isSuccess) count++
            }
            if (count > 0) {
                emitToast("Successfully imported $count item(s) to vault!")
            } else {
                emitToast("Failed to import selected file(s)")
            }
            _showImportSheet.value = false
        }
    }

    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            repository.createSecretNote(title, content)
            emitToast("Secret note saved to vault")
            _showAddNoteDialog.value = false
        }
    }

    fun updateNote(item: VaultItem, title: String, content: String) {
        viewModelScope.launch {
            repository.updateSecretNote(item, title, content)
            emitToast("Note updated")
            _editingNoteItem.value = null
            _activePreviewItem.value = null
        }
    }

    fun deleteItem(item: VaultItem) {
        viewModelScope.launch {
            val success = repository.deleteItem(item)
            if (success) {
                emitToast("Deleted ${item.title} from vault")
                if (_activePreviewItem.value?.id == item.id) {
                    _activePreviewItem.value = null
                }
            } else {
                emitToast("Failed to delete item")
            }
        }
    }

    fun unhideItem(item: VaultItem) {
        viewModelScope.launch {
            val result = repository.unhideToPublic(item)
            result.onSuccess { msg ->
                repository.deleteItem(item)
                emitToast(msg)
                if (_activePreviewItem.value?.id == item.id) {
                    _activePreviewItem.value = null
                }
            }.onFailure { err ->
                emitToast("Error unhiding item: ${err.localizedMessage}")
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
        emitToast(if (enabled) "Fingerprint unlock enabled" else "Fingerprint unlock disabled")
    }

    fun changePin(oldPin: String, newPin: String, confirmPin: String): Boolean {
        if (!securityManager.verifyPin(oldPin)) {
            emitToast("Current PIN is incorrect")
            return false
        }
        if (newPin.length < 4) {
            emitToast("New PIN must be at least 4 digits")
            return false
        }
        if (newPin != confirmPin) {
            emitToast("New PINs do not match")
            return false
        }
        val q = securityManager.getRecoveryQuestion()
        securityManager.resetPinWithRecovery(newPin)
        emitToast("PIN changed successfully")
        return true
    }

    fun recoverPin(answer: String, newPin: String, confirmPin: String): Boolean {
        if (!securityManager.verifyRecoveryAnswer(answer)) {
            emitToast("Incorrect recovery answer")
            return false
        }
        if (newPin.length < 4) {
            emitToast("New PIN must be at least 4 digits")
            return false
        }
        if (newPin != confirmPin) {
            emitToast("PINs do not match")
            return false
        }
        securityManager.resetPinWithRecovery(newPin)
        _isPinSet.value = true
        _showForgotPinDialog.value = false
        emitToast("PIN reset successfully! Vault unlocked.")
        return true
    }

    fun setActivePreview(item: VaultItem?) {
        _activePreviewItem.value = item
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setShowForgotPinDialog(show: Boolean) {
        _showForgotPinDialog.value = show
    }

    fun setShowAddNoteDialog(show: Boolean) {
        _showAddNoteDialog.value = show
    }

    fun setEditingNoteItem(item: VaultItem?) {
        _editingNoteItem.value = item
    }

    fun setShowImportSheet(show: Boolean) {
        _showImportSheet.value = show
    }

    fun getShareIntent(item: VaultItem) = repository.getShareIntent(item)

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val df = DecimalFormat("#,##0.#")
        return "${df.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
    }
}
