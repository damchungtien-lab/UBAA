package cn.edu.ubaa.api

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object VaultStore {
    private const val VAULT_DATA_KEY = "vault_encrypted_data"
    private const val VAULT_HASH_KEY = "vault_master_hash"

    private var _settings: Settings? = null
    var settings: Settings
        get() = _settings ?: Settings().also { _settings = it }
        set(value) {
            _settings = value
        }

    private val json = Json { ignoreUnknownKeys = true }

    fun isConfigured(): Boolean {
        return settings.getStringOrNull(VAULT_HASH_KEY) != null
    }

    fun setMasterPassword(password: String) {
        settings.putString(VAULT_HASH_KEY, VaultCrypto.hashPassword(password))
        val emptyVault = VaultData()
        val encrypted = VaultCrypto.encrypt(json.encodeToString(emptyVault), password)
        settings.putString(VAULT_DATA_KEY, encrypted)
    }

    fun verifyMasterPassword(password: String): Boolean {
        val storedHash = settings.getStringOrNull(VAULT_HASH_KEY) ?: return false
        return VaultCrypto.hashPassword(password) == storedHash
    }

    fun changeMasterPassword(oldPassword: String, newPassword: String): Boolean {
        if (!verifyMasterPassword(oldPassword)) return false
        val entries = getAllEntriesInternal(oldPassword)
        val newEncrypted = VaultCrypto.encrypt(json.encodeToString(VaultData(entries)), newPassword)
        settings.putString(VAULT_DATA_KEY, newEncrypted)
        settings.putString(VAULT_HASH_KEY, VaultCrypto.hashPassword(newPassword))
        return true
    }

    fun getAllEntries(password: String): List<VaultEntry>? {
        if (!verifyMasterPassword(password)) return null
        return getAllEntriesInternal(password)
    }

    fun saveEntries(entries: List<VaultEntry>, password: String): Boolean {
        if (!verifyMasterPassword(password)) return false
        val encrypted = VaultCrypto.encrypt(json.encodeToString(VaultData(entries)), password)
        settings.putString(VAULT_DATA_KEY, encrypted)
        return true
    }

    fun addEntry(entry: VaultEntry, password: String): Boolean {
        val entries = getAllEntries(password) ?: return false
        return saveEntries(entries + entry, password)
    }

    fun updateEntry(updated: VaultEntry, password: String): Boolean {
        val entries = getAllEntries(password) ?: return false
        val newEntries = entries.map { if (it.id == updated.id) updated else it }
        return saveEntries(newEntries, password)
    }

    fun deleteEntry(entryId: String, password: String): Boolean {
        val entries = getAllEntries(password) ?: return false
        return saveEntries(entries.filter { it.id != entryId }, password)
    }

    fun resetVault() {
        settings.remove(VAULT_DATA_KEY)
        settings.remove(VAULT_HASH_KEY)
    }

    private fun getAllEntriesInternal(password: String): List<VaultEntry> {
        val encrypted = settings.getStringOrNull(VAULT_DATA_KEY) ?: return emptyList()
        return try {
            val decrypted = VaultCrypto.decrypt(encrypted, password)
            json.decodeFromString<VaultData>(decrypted).entries
        } catch (_: Exception) {
            emptyList()
        }
    }
}
