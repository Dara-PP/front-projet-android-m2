package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.data.KtorServer

@Composable
fun ProfilPage(navController: NavController) {
    val context = LocalContext.current
    val server = remember{ KtorServer() }
    var meInfos by remember { mutableStateOf("Profil0") }

    LaunchedEffect(Unit) {
        val result = server.me(context)
        if(result != null) meInfos = result else meInfos = "Problemes chargement profil"
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(meInfos)
        Button(onClick = {
            // appelle la fonction logout
            server.logout(context)
            // Redirection sur login page
            navController.navigate("login"){
                // popUpTo et einclusive clear tout l'historique de navigation,
                // Ca empeche l'user de revenir au home quand click retour
                popUpTo(0) {inclusive = true}
            }
        }) {Text("Se déconnecter")}
    }
}

@Preview(showBackground = true)
@Composable
fun TestRegisterPage(){
   ProfilPage(navController = rememberNavController())
}