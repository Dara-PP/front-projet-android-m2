package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.ui.map.OpenStreetMap

// Dataclasse de NavItem
data class NavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun NavigationBarUI(navController: NavController) {
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Apps", Icons.Default.Face),
        NavItem("Map", Icons.Default.LocationOn),
        NavItem("Cartes", Icons.Default.Star),
        NavItem("Collection", Icons.Default.Favorite),
        NavItem("Profil", Icons.Default.Person),
    )

    // Garde en memoire la valeur de l'onglet actuelle, 0 par defaut "home"
    var selectedDestination by remember { mutableStateOf(0) }

    // Scaffold avec bottomBar seulement
    Scaffold(
        containerColor = Color(0xFFF0F4F8),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                // Boucle sur notre list pour créer nos boutons de nav
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = { selectedDestination = index },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                        label = {
                            Text(text = navItem.label)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF27AE60),
                            selectedTextColor = Color(0xFF27AE60),
                            indicatorColor = Color(0xFF27AE60).copy(alpha = 0.15f),
                            unselectedIconColor = Color(0xFF2C3E50).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF2C3E50).copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Contenus de l'ecran selon l'onglet selectionné,
        // innerPadding pour que la fenetre ne soit pas en dessous du navBar
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedDestination = selectedDestination,
            navController = navController
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedDestination: Int,
    navController: NavController
) {
    when (selectedDestination) {
        0 -> HomePage(navController = navController)
        1 -> ApplicationsScreen(
            onDavidAppClick = {
                navController.navigate("david_game")
            },
            onFrancoisAppClick = {
                navController.navigate("Francois_game")
            },
            onDaraAppClick = {
                navController.navigate("Dara_game")
            },
            onFlorianAppClick = {
                navController.navigate("Florian_game")
            },
            onAxylAppClick = {
                navController.navigate("Axyl_game")
            },
        )
        2 -> OpenStreetMap()
        3 -> JsonDeroulo()
        4 -> CardList()
        5 -> ProfilPage(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun TestNavigationUI() {
    val navController = rememberNavController()
    NavigationBarUI(
        navController = navController
    )
}