package com.example.projet_android_m2.ui.map

import com.example.projet_android_m2.data.NearCard
import kotlin.math.cos
import kotlin.math.sin

private const val RADIUS_KM = 5.0

fun buildCircleGeoJson(lat: Double, lon: Double, radiusKm: Double = RADIUS_KM): String {
    val pointsList = mutableListOf<String>()
    for (angle in 0..360 step 10) {
        val rad = Math.toRadians(angle.toDouble())
        val pLat = lat + (radiusKm / 111.32) * sin(rad)
        val pLon = lon + (radiusKm / (111.32 * cos(Math.toRadians(lat)))) * cos(rad)
        pointsList.add("[$pLon, $pLat]")
    }
    val points = pointsList.joinToString(",")
    return """{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[$points]]},"properties":{}}"""
}

fun buildCardsGeoJson(cards: List<NearCard>): String {
    val features = cards.joinToString(",") { card ->
        val name = card.person_name.replace("\"", "'")
        """
        {
          "type": "Feature",
          "properties": {
            "id":         "${card.id}",
            "personName": "$name",
            "power":      ${card.power},
            "distanceKm": ${card.distance_km}
          },
          "geometry": {
            "type": "Point",
            "coordinates": [${card.lon}, ${card.lat}]
          }
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}