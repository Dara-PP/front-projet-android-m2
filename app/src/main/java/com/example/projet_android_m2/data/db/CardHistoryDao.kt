package com.example.projet_android_m2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CardHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: CardHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<CardHistory>)

    // Flux live pour la Collection screen (se met a jour automatiquement après sync)
    @Query("SELECT * FROM card_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getForUserFlow(userId: String): Flow<List<CardHistory>>

    // Flux des cartes possédées (action 0=CAPTURED, 2=WON_BATTLE, exclut 1=TRADED)
    @Query("SELECT * FROM card_history WHERE userId = :userId AND action != 1 ORDER BY timestamp DESC")
    fun getOwnedCardsFlow(userId: String): Flow<List<CardHistory>>

    // Historique d'une carte
    @Query("SELECT * FROM card_history WHERE cardId = :cardId ORDER BY timestamp ASC")
    suspend fun getHistoryForCard(cardId: String): List<CardHistory>

    // Clear avant de réimporter depuis le serveur
    @Query("DELETE FROM card_history WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
