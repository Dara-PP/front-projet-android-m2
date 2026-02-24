package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.data.KtorServer

@Composable
fun HomePage(navController: NavController) {
    val context = LocalContext.current
    // Remember le server afin de le garder en mémoire et éviter de le recréer à chaque fois
    val server = remember{ KtorServer() }
    val username = server.getUsername(context)
    Column(modifier = Modifier.fillMaxSize()) {
        //Affiche id quand login et username quand via register backend a changer
        Text("Bonjour $username")
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
fun TestHomePage(){
    HomePage(navController = rememberNavController())
}
