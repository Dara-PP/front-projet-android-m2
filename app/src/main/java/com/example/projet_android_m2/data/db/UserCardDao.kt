package com.example.projet_android_m2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCardDao {

    @Query("SELECT * FROM user_cards")
    fun getAll(): Flow<List<UserCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<UserCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: UserCardEntity)

    @Query("SELECT * FROM user_cards WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserCardEntity?

    @Query("DELETE FROM user_cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM user_cards")
    suspend fun clearAll()

    // Upsert sans clear : les cartes déjà rencontrées restent en cache offline
    @Transaction
    suspend fun replaceAll(cards: List<UserCardEntity>) {
        insertAll(cards)
    }
}
