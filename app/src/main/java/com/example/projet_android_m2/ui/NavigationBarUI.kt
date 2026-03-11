package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.ui.map.OpenStreetMap

// Dataclasse de NavItem
data class NavItem(
    val label : String,
    val icon : ImageVector
)

@Composable
fun NavigationBarUI(navController: NavController) {
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Apps", Icons.Default.Face),
        NavItem("Map", Icons.Default.LocationOn),
        NavItem("Carte", Icons.Default.Favorite),
        NavItem("Profil", Icons.Default.Person),

        )
    // Garde en memoire la valeur de l'onglet actuelle, 0 par defaut "home"
    var selectedDestination by remember { mutableStateOf(0) }
    // Scaffold avec bottomBar seulement
    Scaffold(
        bottomBar = {
            NavigationBar {
                //Boucle sur notre list pour créer nos boutons de nav
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {selectedDestination = index},
                        icon = {Icon(imageVector = navItem.icon, contentDescription = "Icon")},
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Contenus de l'ecran selon l'onglet selectionné,
        // innerPadding pour que la fenetre ne soit pas en dessous du navBar
        ContentScreen(
            modifier =  Modifier.padding(innerPadding),
            selectedDestination = selectedDestination,
            navController = navController
        )
    }
}
@Composable
fun ContentScreen(modifier: Modifier = Modifier,
                  selectedDestination: Int,
                  navController: NavController){
    when(selectedDestination){
        0 -> HomePage(navController = navController)
        1 -> ApplicationsScreen(
            onAppClick = {
                navController.navigate("david_game")
            }
        )
        2 -> OpenStreetMap()
        3 -> JsonDeroulo()
        4 -> ProfilPage(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun TestNavigationUI(){
    val navController = rememberNavController()
    NavigationBarUI(
        navController = navController
    )
}