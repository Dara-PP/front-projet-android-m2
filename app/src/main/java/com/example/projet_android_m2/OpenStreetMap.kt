package com.example.projet_android_m2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

@Composable
fun OpenStreetMap (){
    MaplibreMap(
        modifier = Modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"))
}

// A run sur le telephone pas sur preview !
// redemarre le telephone si bug
@Preview
@Composable
fun TestMap(){
    OpenStreetMap()
}
