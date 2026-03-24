package com.example.projet_android_m2.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.projet_android_m2.data.db.PlacePersonality
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

    //Retourne les lieux dans |=| box autour de l'user.
    @Query("""
        SELECT * FROM places
        WHERE locationLat BETWEEN :minLat AND :maxLat
          AND locationLon BETWEEN :minLon AND :maxLon
    """)
    suspend fun getPlacesAround(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<PlacePersonality>
}