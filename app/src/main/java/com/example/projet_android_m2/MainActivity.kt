package com.example.projet_android_m2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.ui.auth.LoginPage
import com.example.projet_android_m2.data.KtorServer
import com.example.projet_android_m2.ui.LoadScreen
import com.example.projet_android_m2.ui.NavigationBarUI
import com.example.projet_android_m2.ui.ShakeTreeGame
import com.example.projet_android_m2.ui.auth.RegisterPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // instanciation du serveur
        val server = KtorServer()
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val token = server.getToken(context)
            // check si utilisateur login ou pas
            var page = if (token != null) "home" else "login"
            // Etat du chargement
            var isLoadingDone by remember { mutableStateOf(false) }

            if (!isLoadingDone){
                LoadScreen(
                    onLoadComplete = {
                        isLoadingDone = true
                    }
                )
            } else{
                NavHost(
                    navController = navController,
                    startDestination = page,
                    modifier = Modifier.padding(10.dp)
                ){
                    composable("home"){
                        //La bar de navigation a scaffold donc il gere lui meme ca place
                        NavigationBarUI(navController = navController)
                    }
                    composable ("login" ){
                        LoginPage(navController = navController)
                    }
                    composable ("register" ){
                        RegisterPage(navController = navController)
                    }
                    composable("david_game") {
                        ShakeTreeGame(
                            onGameFinished = { score ->
                                println("Partie terminée avec un score de $score !")
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
