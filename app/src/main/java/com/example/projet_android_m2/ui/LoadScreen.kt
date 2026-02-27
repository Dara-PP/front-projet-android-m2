package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.projet_android_m2.data.PlaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoadScreen(onLoadComplete: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("Initilisation") }
    var isLoading by remember { mutableStateOf(true) }
    var errorLoad by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val count = repo.countDao()
        if (count > 0) {
            statusMessage = "Base de donnée déja chargé \n $count lieux dispnibles"
            progress = 100
            isLoading = false
            delay(2000)
            onLoadComplete()
        } else {
            // Lancement du chargement
            repo.jsonInsertRoomChunk(
                chunkSize = 1000,
                status = { current, total, message ->
                    progress = current
                    statusMessage = message
                }
            ).onSuccess { resultMessage ->
                isLoading = false
                statusMessage = resultMessage
                delay(1500) // Pour voir le message sinon trop rapide
                onLoadComplete()
            }.onFailure { error ->
                isLoading = false
                errorLoad = true
                statusMessage = "Erreur ${error.message}"
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = if (isLoading) "Chargement des données" else if (errorLoad) "Erreur" else "Chargement terminé",
        )

        Spacer(modifier = Modifier.height(7.dp))

        //Barre de progression
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.height(7.dp))
        }
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(7.dp))

        //Status message
        Column(modifier = Modifier.padding(15.dp)) {
            Text(text = statusMessage)
            Spacer(modifier = Modifier.height(7.dp))
        }
        if(!isLoading) {
            if(errorLoad){
                Button(
                    onClick = {
                        errorLoad = false
                        isLoading = true
                        progress = 0
                        scope.launch { repo.jsonInsertRoomChunk(
                            chunkSize = 1000,
                            status = {current, total, message ->
                                progress = current
                                statusMessage = message
                            }
                            ).onSuccess {
                                isLoading = false
                                kotlinx.coroutines.delay(1500)
                                onLoadComplete()
                            }.onFailure { error ->
                                isLoading = false
                                errorLoad = true
                                statusMessage = "Erreur ${error.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Réessayer")
                }
            } else if (progress == 100){
                Button(onClick = onLoadComplete,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Continuer")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun testLoadScreen(){
    LoadScreen(
        onLoadComplete = {}
    )
}


