package com.example.projet_android_m2.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.PlacePersonality
import com.example.projet_android_m2.data.PlaceRepository
import kotlinx.coroutines.flow.filterNotNull
import org.maplibre.spatialk.geojson.Position
import org.maplibre.compose.camera.CameraPosition
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
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.layers.LineLayer

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RememberReturnType")
@Composable
fun OpenStreetMap (){
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    var places by remember { mutableStateOf<List<PlacePersonality>>(emptyList()) }
    // Point sélectionné au clic
    var selectedName  by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf("") }
    var selectedType  by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Localisation
    val local = rememberDefaultLocationProvider()
    val locationState = rememberUserLocationState(local)
    val cameraState = rememberCameraState()
    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLon by remember { mutableDoubleStateOf(0.0) }
    val radiusKm = 2.0

    // Get les points dans places avec call vers room via getPerson()
    LaunchedEffect(Unit) {
        // test avec seulement 1000 pour l'instant !
        // Lat - lon affichage test
        //repo.getPerson(offset = 0, size = 1000).collect { pointList ->
        //    places = pointList
        //    if (pointList.isNotEmpty()) {
        //        val pointTest = pointList[2]
        //        Log.d("PointId${pointTest.id}", "Lat: ${pointTest.locationLat}, Lon: ${pointTest.locationLon}")
        //    }
        snapshotFlow { locationState.location }
            .filterNotNull()
            .collect { userLoc ->
                userLat = userLoc.position.latitude
                userLon = userLoc.position.longitude
                places = repo.getPlacesAroundGps(userLat, userLon, radiusKm)
                // Zoom sur l'user centrage
                cameraState.position = CameraPosition(
                    target = Position(longitude = userLon, latitude = userLat),
                    zoom = 12.0
                )
            }
    }

    // GeoJSON du cercle (avec 36 points)
    val circleJson = remember(userLat, userLon) {
        val points = (0..360 step 10)
            .map { angle ->
                val rad = Math.toRadians(angle.toDouble())
                val pLat = userLat + (radiusKm / 111.0) * kotlin.math.sin(rad)
                val pLon = userLon + (radiusKm / 111.0) * kotlin.math.cos(rad)
                "[$pLon, $pLat]" // On crée d'abord une liste de strings
            }
            .joinToString(",") // On les assemble à la fin
        """{
            "type":"Feature",
            "geometry":{
                "type":"Polygon",
                "coordinates":[[$points]]
                }
            }
        """.trimMargin()
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
              "properties": {
                "personName": "${(pointMap.personNameFr ?: pointMap.personNameEn ?: "?")
                    .replace("\"", "'")}",
                "placeName":  "${(pointMap.nameFr ?: pointMap.nameEn ?: "?")
                    .replace("\"", "'")}",
                "type":       "${if (pointMap.zone) "Zone" else "Lieu"}"
              },
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


    // Info Point
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nom : $selectedName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lieu : $selectedPlace",
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Type : $selectedType",
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }
        }
    }
    MaplibreMap(
        modifier = Modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
        cameraState = cameraState,
    ){
        // Transformation du json en source pour maplibre
        val source1 = rememberGeoJsonSource(
            data = GeoJsonData.JsonString(featureMapJson)
        )
        CircleLayer(
            id = "test-1",
            source = source1,
            radius = const(7.dp),
            color = const(Color.Red),
            // Onclick pour get les infos du points
            onClick = { features ->
                val props = features.firstOrNull()?.properties
                if (props != null) {
                    selectedName  = props.get("personName")?.toString()?: "?"
                    selectedPlace = props.get("placeName")?.toString()?: "?"
                    selectedType  = props.get("type")?.toString()?: "?"
                    showSheet = true
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            }
        )

        val circleSource = rememberGeoJsonSource(
            data = GeoJsonData.JsonString(circleJson)
        )
        // Layer du contour, hitbox user
        LineLayer(
            id = "hitBox",
            source = circleSource,
            color = const(Color.Blue),
        )
        // crach sur emulateur si pas de position set !!
        if (locationState.location != null){
            LocationPuck(
                idPrefix = "user",
                locationState = locationState,
                cameraState = cameraState,
                onClick = {
                    selectedName = "John"
                    selectedPlace = "A faire get gps"
                    selectedType = "Stats joueur"
                    showSheet = true
                }
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
