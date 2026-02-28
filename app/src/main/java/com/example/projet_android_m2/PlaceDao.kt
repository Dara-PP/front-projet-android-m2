package com.example.projet_android_m2

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Upsert
    suspend fun insertAll(places: List<PlacePersonality>)

    @Delete
    suspend fun deletePlace(places: List<PlacePersonality>)

    @Query("SELECT * FROM places ORDER BY personNameFr ASC LIMIT :size OFFSET :offset ")
    fun getPersonOrderByNameFr(offset: Int, size: Int): Flow<List<PlacePersonality>>

    @Query("SELECT * FROM places LIMIT :size OFFSET :offset")
    fun getPerson(offset: Int, size: Int): Flow<List<PlacePersonality>>

    @Query("SELECT COUNT(*) FROM places")
    suspend fun count(): Long

    @Query("DELETE FROM places")
    suspend fun clearAll()
}