package com.example.projet_android_m2.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projet_android_m2.data.db.PlaceDao
import com.example.projet_android_m2.data.db.PlacePersonality

@Database(
    entities = [PlacePersonality::class, PlaceCard::class, CardHistory::class],
    version = 2 // Migration pour base de donnée
)
// Prompt Claude Sonnet 4.6 : Donne moi le code pour initier une base de données Room en kotlin en te basant sur mes fichier fichier PlacePersonalitly.kt et PlaceDao.kt,
// car celle de la documentation ne marche pas dans mon cas "https://developer.android.com/training/data-storage/room?hl=fr"
abstract class PlaceDatabase: RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun placeCardDao(): PlaceCardDao
    abstract fun cardHistoryDao(): CardHistoryDao

    companion object {
        @Volatile private var INSTANCE: PlaceDatabase? = null

        fun getInstance(context: Context): PlaceDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java,
                    "places_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}