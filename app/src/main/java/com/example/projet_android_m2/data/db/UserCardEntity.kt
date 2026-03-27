package com.example.projet_android_m2.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_cards")
data class UserCardEntity(
    @PrimaryKey val id: String,
    val person_name: String,
    val lat: Double,
    val lon: Double,
    val power: Double,
    val acquired_at: String?
)
