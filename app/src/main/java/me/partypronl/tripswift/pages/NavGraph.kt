package me.partypronl.tripswift.pages

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    fusedLocationProviderClient: FusedLocationProviderClient,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Departures.route) {
            DeparturesScreen(navController = navController, fusedLocationProviderClient)
        }

        composable(Screen.Route.route) {
            RouteScreen(navController = navController)
        }

        composable(
            Screen.StopDepartures.route,
            arguments = listOf(
                navArgument("stopName") { type = NavType.StringType },
                navArgument("stopTown") { type = NavType.StringType }
            )
        ) {
            StopDeparturesScreen(navController = navController, it.arguments!!.getString("stopName")!!, it.arguments!!.getString("stopTown")!!, snackbarHostState)
        }
    }
}