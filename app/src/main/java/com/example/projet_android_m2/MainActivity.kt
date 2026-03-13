package com.example.projet_android_m2

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.example.projet_android_m2.ui.game.ShakeTreeGame
import com.example.projet_android_m2.ui.auth.RegisterPage
import com.example.projet_android_m2.ui.minigames.BombDefuseMiniGame


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                // No location access granted.
            }
        }

    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // instanciation du serveur
        val server = KtorServer()
        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions:
        // https://developer.android.com/training/permissions/requesting#request-permission
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
                    composable("Francois_game") {
                        BombDefuseMiniGame(
                            onSuccess = {
                                println("Mini-jeu réussi")
                                navController.popBackStack()
                            },
                            onFail = {
                                println("Mini-jeu échoué")
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
