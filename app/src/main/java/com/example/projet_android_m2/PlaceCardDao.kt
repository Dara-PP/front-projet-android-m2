package com.example.projet_android_m2

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceCardDao {

    @Upsert
    suspend fun insertAll(cards: List<PlaceCard>)

    @Query("SELECT COUNT(*) FROM places_cards")
    suspend fun count(): Long

    @Query("DELETE FROM places_cards")
    suspend fun clearAll()

    // Retourne les cartes non collectées
    @Query("""
        SELECT * FROM places_cards
        WHERE iscatch = 0
          AND locationRandomLat BETWEEN :minLat AND :maxLat
          AND locationRandomLon BETWEEN :minLon AND :maxLon
    """)
    suspend fun getCardsAround(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<PlaceCard>

    // Marquer une carte comme attrapée
    @Query("UPDATE places_cards SET iscatch = 1 WHERE id = :cardId")
    suspend fun catchCard(cardId: Long)

    @Query("""
    SELECT * FROM places_cards
    WHERE iscatch = 0
      AND zone = 1
      AND locationRandomLat BETWEEN :minLat AND :maxLat
      AND locationRandomLon BETWEEN :minLon AND :maxLon
""")
    suspend fun getZoneCardsAround(
        minLat: Double, maxLat: Double,
        minLon: Double, maxLon: Double
    ): List<PlaceCard>

    @Query("""
    SELECT * FROM places_cards
    WHERE iscatch = 0
      AND zone = 0
      AND locationRandomLat BETWEEN :minLat AND :maxLat
      AND locationRandomLon BETWEEN :minLon AND :maxLon
""")
    suspend fun getLieuCardsAround(
        minLat: Double, maxLat: Double,
        minLon: Double, maxLon: Double
    ): List<PlaceCard>

    // Reset les cartes catch pour test
    @Query("UPDATE places_cards SET iscatch = 0")
    suspend fun resetAllCatch()
}