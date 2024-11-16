package me.partypronl.tripswift

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import me.partypronl.tripswift.pages.Screen
import me.partypronl.tripswift.pages.SetupNavGraph
import me.partypronl.tripswift.settings.Settings
import me.partypronl.tripswift.ui.theme.ComposeTutorialTheme

class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        var settings: Settings? = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            create()
        } else {
            askForLocationPermissions()
        }
    }

    private fun askForLocationPermissions() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settings = Settings(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForLocationPermissions()
            return
        }

        create()
    }

    private fun create() {
        setContent {
            ComposeTutorialTheme(
                darkTheme = false,
                dynamicColor = true
            ) {
                navController = rememberNavController()
                MainPage(navHostController = navController, fusedLocationClient)
            }
        }
    }
}

@Composable
fun CustomNavigationBar(navController: NavController, selectedPage: Int = 0) {
    var selectedItem by remember { mutableIntStateOf(selectedPage) }

    NavigationBar() {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedItem == 0,
            onClick = {
                selectedItem = 0
                navController.navigate(route = Screen.Home.route)
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.baseline_access_time_filled_24), contentDescription = "Vertrektijden") },
            label = { Text("Vertrektijden") },
            selected = selectedItem == 1,
            onClick = {
                selectedItem = 1
                navController.navigate(route = Screen.Departures.route)
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.baseline_route_24), contentDescription = "Route") },
            label = { Text("Route") },
            selected = selectedItem == 2,
            onClick = { selectedItem = 2 },
            enabled = false
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainPage(navHostController: NavHostController, fusedLocationProviderClient: FusedLocationProviderClient) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = {
            CustomNavigationBar(navController = navHostController, 0)
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        content = {
            SetupNavGraph(navController = navHostController, fusedLocationProviderClient, snackbarHostState)
        }
    )
}