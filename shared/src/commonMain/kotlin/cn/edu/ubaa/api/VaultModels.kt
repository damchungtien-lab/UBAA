package cn.edu.ubaa.api

import kotlinx.serialization.Serializable

@Serializable
data class VaultEntry(
    val id: String,
    val title: String,
    val systemName: String = "",
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class VaultData(
    val entries: List<VaultEntry> = emptyList(),
)
