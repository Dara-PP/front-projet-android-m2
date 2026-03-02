package com.example.projet_android_m2.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projet_android_m2.PlacePersonality
import com.example.projet_android_m2.data.PlaceRepository
import kotlinx.coroutines.flow.first
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle

@SuppressLint("RememberReturnType")
@Composable
fun OpenStreetMap (){
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    var places by remember { mutableStateOf<List<PlacePersonality>>(emptyList()) }

    // Get les points dans places avec call vers room via getPerson()
    LaunchedEffect(Unit) {
        // test avec seulement 1000 pour l'instant !
        places = repo.getPerson(offset = 0, size = 1000).first()
    }

    val featureMapJson = remember(places) {
        """
        {
          "type": "FeatureCollection",
          "features": [
            ${places.joinToString(","){ pointMap ->
            """
            {
              "type": "Feature",
              "properties": {},
              "geometry": {
                "type": "Point",
                "coordinates": [${pointMap.locationLon}, ${pointMap.locationLat}]
              }
            }
            """.trimIndent()
            }}
          ]
        }
        """.trimIndent()
    }

    // Localisation
    val local = rememberDefaultLocationProvider()
    val locationState = rememberUserLocationState(local)
    val cameraState = rememberCameraState()

    MaplibreMap(
        modifier = Modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
        cameraState = cameraState
    ){
        // Transformation du json en source pour maplibre
        val source1 = rememberGeoJsonSource(
            data = GeoJsonData.JsonString(featureMapJson)
        )
        CircleLayer(
            id = "test-1",
            source = source1,
            radius = const(7.dp),
            color = const(Color.Red)
        )
        // crach sur emulateur si pas de position set !!
        if (locationState.location != null){
            LocationPuck(
                idPrefix = "user",
                locationState = locationState,
                cameraState = cameraState
            )
        }
        else{
            println("Pas de signal GPS ou indisponible")
        }
    }
}

// A run sur le telephone pas sur preview !
// redemarre le telephone si bug
@Preview
@Composable
fun TestMap(){
    OpenStreetMap()
}
