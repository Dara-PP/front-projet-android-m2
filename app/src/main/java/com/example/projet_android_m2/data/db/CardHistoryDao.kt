package com.example.projet_android_m2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CardHistoryDao {

    @Insert
    suspend fun insert(event: CardHistory)

    // Historique d'une carte
    @Query("SELECT * FROM card_history WHERE cardId = :cardId ORDER BY timestamp ASC")
    suspend fun getHistoryForCard(cardId: Long): List<CardHistory>

    // Historique de l'user
    @Query("SELECT * FROM card_history WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getHistoryForUser(userId: String): List<CardHistory>
}