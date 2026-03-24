package com.example.projet_android_m2.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
enum class CardHistoryAction(val value: Int) {
    CAPTURED(0),
    TRADED(1),
    WON_BATTLE(2)
}
@Entity(tableName = "card_history")
data class CardHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val userId: String,
    val action: Int,
    val timestamp: Long = System.currentTimeMillis()
)