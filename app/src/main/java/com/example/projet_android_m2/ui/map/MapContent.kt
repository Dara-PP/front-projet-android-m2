package com.example.projet_android_m2.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.UserLocationState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult

@Composable
fun MapContent(
    cardsJson: String,
    circleJson: String,
    locationState: UserLocationState,
    cameraState: CameraState,
    onPointClick: (id: String) -> Unit, // On ne passe plus que l'ID pour plus de fiabilité
    onUserPuckClick: () -> Unit,
) {
    val cardsSource = rememberGeoJsonSource(data = GeoJsonData.JsonString(cardsJson))

    CircleLayer(
        id = "cards",
        source = cardsSource,
        radius = const(15.dp), // Zone de clic agrandie pour le confort
        color = const(Color.Red),
        onClick = { features ->
            val topFeature = features.firstOrNull() as? org.maplibre.spatialk.geojson.Feature
            if (topFeature != null) {
                // On récupère l'ID défini dans buildCardsGeoJson
                val id = topFeature.properties?.get("id")?.toString()
                if (id != null) {
                    Log.d("MapClick", "ID détecté : $id")
                    onPointClick(id)
                }
            }
            ClickResult.Consume
        }
    )

    LineLayer(
        id = "hitBox",
        source = rememberGeoJsonSource(data = GeoJsonData.JsonString(circleJson)),
        color = const(Color.Blue),
    )

    if (locationState.location != null) {
        LocationPuck(
            idPrefix = "user",
            locationState = locationState,
            cameraState = cameraState,
            onClick = { _ -> onUserPuckClick() }
        )
    }
}