package com.example.projet_android_m2

import androidx.room.Entity
import androidx.room.PrimaryKey
// nom table
@Entity(tableName = "places_cards")
// tout en ? nullable au cas ou
data class PlaceCard(
    val personId : Long, // id person

    val personNameEn: String?, // name person
    val personNameFr: String?,
    val nameEn: String?, // name lieux
    val nameFr: String?,

    val locationLat: Double, // coordonnées de base depuis PlacePersonality
    val locationLon: Double,
    val locationRandomLat: Double, // coordonnées random
    val locationRandomLon: Double,

    val zone: Boolean, // si zone ou point précis je pense
    val iscatch: Boolean = false,
    @PrimaryKey val id: Long, // id du lieux
)
