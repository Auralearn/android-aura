package findit.edversity.auralearn

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import findit.edversity.auralearn.screens.HomeScreen
import findit.edversity.auralearn.screens.MaterialListScreen

@Composable
fun AuralearnApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("materialList") {
            MaterialListScreen(navController = navController)
        }
    }
}