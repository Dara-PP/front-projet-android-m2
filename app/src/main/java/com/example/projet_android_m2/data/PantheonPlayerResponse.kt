package com.example.projet_android_m2.data

import kotlinx.serialization.Serializable

@Serializable
data class PantheonPlayerResponse(
    val rank: Int,
    val username: String,
    val score: Long
)