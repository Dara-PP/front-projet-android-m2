package com.example.projet_android_m2.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projet_android_m2.data.db.PlaceDao
import com.example.projet_android_m2.data.db.PlacePersonality

@Database(
    entities = [PlacePersonality::class, CardHistory::class, UserCardEntity::class],
    version = 5
)
abstract class PlaceDatabase: RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun cardHistoryDao(): CardHistoryDao
    abstract fun userCardDao(): UserCardDao

    companion object {
        @Volatile private var INSTANCE: PlaceDatabase? = null

        fun getInstance(context: Context): PlaceDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java,
                    "places_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}