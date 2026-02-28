package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projet_android_m2.data.CarteInfos
import com.example.projet_android_m2.data.PlaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JsonDeroulo(modifier: Modifier = Modifier){
    val context = LocalContext.current
    val repo = remember{ PlaceRepository(context) }
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Chargement...") }
    var listCartes by remember { mutableStateOf<List<CarteInfos>>(emptyList()) }

    // Refresh init
    suspend fun refresh(){
        status= "Chargement Json - 1"
        delay(1000)
        status = "Chargement Json - 2"
        delay(1000)
        listCartes = repo.checkInit {message ->
            status = message
        }
        //status = "Chargement complet"
    }

    // Coroutine simple ici pour pas bloquer le chargement du json
    LaunchedEffect(Unit) {
        refresh()
    }

    Column(modifier = modifier) {
        Button(
            onClick={
                scope.launch {
                    status = "Clear et refresh en cours"
                    delay(1000)
                    repo.clearDao()
                    delay(1000)
                    refresh()
                }
            }
        ){
            Text("Refresh le DAO")
        }
        //status actuel
        Text(
            text = status,
            modifier = modifier
        )
        listCartes.forEach { item ->
            Text(
                text = "${item.personName}, ${item.placeName}, ${item.type}",
                modifier = Modifier.padding(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestJsonDeroulo(){
    JsonDeroulo()
}