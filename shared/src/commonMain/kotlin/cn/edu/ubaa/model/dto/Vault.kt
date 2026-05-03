package cn.edu.ubaa.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class VaultCipherParamsDto(
    val algorithm: String = "AES-256-GCM",
    val kdf: String = "PBKDF2-HMAC-SHA256",
    val iterations: Int = 600_000,
    val salt: String,
    val nonce: String,
)

@Serializable
data class VaultRecordDto(
    val revision: Long,
    val updatedAt: String,
    val cipherText: String,
    val params: VaultCipherParamsDto,
)

@Serializable
data class VaultSaveRequest(
    val baseRevision: Long?,
    val cipherText: String,
    val params: VaultCipherParamsDto,
)

@Serializable data class VaultSaveResponse(val record: VaultRecordDto)

@Serializable
data class VaultPlainEntryDto(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val url: String? = null,
    val note: String? = null,
    val updatedAt: String,
)

@Serializable
data class VaultPlainStateDto(
    val entries: List<VaultPlainEntryDto> = emptyList(),
)
