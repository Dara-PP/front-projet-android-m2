package com.example.projet_android_m2.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// nom table
@Entity(tableName = "places")
// tout en ? nullable au cas ou
data class PlacePersonality(
    val personId : Long, // id person

    val personNameEn: String?, // name person
    val personNameFr: String?,
    val nameEn: String?, // name lieux
    val nameFr: String?,

    val relationId: Long?, // type relation person <-> lieux
    val relationNameEn: String?, // type de relation selon id
    val relationNameFr: String?,

    val locationLat: Double, // coordonnées
    val locationLon: Double,
    val zone: Boolean, // si zone ou point précis je pense

    @PrimaryKey val id: Long, // id du lieux
)