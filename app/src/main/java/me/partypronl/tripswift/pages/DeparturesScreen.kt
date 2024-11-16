package me.partypronl.tripswift.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import me.partypronl.tripswift.MainActivity
import me.partypronl.tripswift.R
import me.partypronl.tripswift.Stop
import me.partypronl.tripswift.network.NetworkUtils

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesScreen(
    navController: NavController,
    fusedLocationProviderClient: FusedLocationProviderClient?
) {
    val networkUtils = NetworkUtils()
    val stationsLoaded = remember { mutableStateOf(false) }
    val stations = remember { mutableStateOf(listOf<Stop>()) }

    LaunchedEffect(Unit) {
        if (fusedLocationProviderClient != null) {
            networkUtils.getStations(fusedLocationProviderClient) {
                stationsLoaded.value = true
                stations.value = it
            }
        }
    }

    var searchText by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(listOf<Stop>()) }

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SearchBar(query = searchText, onQueryChange = {
                searchText = it
                searching = false
            }, onSearch = {
                if(searchText.isEmpty()) {
                    return@SearchBar
                }

                searching = true
                networkUtils.getSearchResults(it, fusedLocationProviderClient) { results ->
                    searchResults = results
                }
            }, active = false, onActiveChange = {

            }, modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Halte zoeken")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Zoeken")
                },
            trailingIcon = {
                Icon(Icons.Default.Refresh, "Reload", modifier = Modifier.clickable {
                    stationsLoaded.value = false
                    stations.value = listOf()

                    networkUtils.getStations(fusedLocationProviderClient!!) {
                        stationsLoaded.value = true
                        stations.value = it
                    }
                })
            }
            ) {

        }

        if(!searching) {
            Text(
                text = "Opgeslagen haltes",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (MainActivity.settings == null || MainActivity.settings!!.savedStops.isEmpty()) {
                NoSavedStops()
            }

            MainActivity.settings?.savedStops?.forEach {
                it.RenderStop(navController)
            }

            Text(
                text = "In de buurt (< 500m)",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Column(modifier = Modifier.padding(bottom = 80.dp)) {
                if (stationsLoaded.value) {
                    stations.value.forEach { stop ->
                        stop.RenderStop(navController)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Text(text = "Haltes in de buurt worden geladen...", modifier = Modifier.padding(top = 40.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        } else {
            if(searchResults.isNotEmpty()) {
                Text(
                    text = "Zoekresultaten",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(modifier = Modifier.padding(bottom = 80.dp)) {
                    searchResults.forEach { stop ->
                        stop.RenderStop(navController)
                    }
                }
            } else {
                NoSearchResults()
            }
        }
    }
}

@Composable
fun NoSearchResults() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_sentiment_dissatisfied_24),
                contentDescription = "Sad",
                modifier = Modifier
                    .padding(8.dp)
                    .height(64.dp)
                    .width(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Geen zoekresultaten",
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoSavedStops() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.shapes.large
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painter = painterResource(id = R.drawable.baseline_star_24), contentDescription = "Favorite", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "Je hebt nog geen haltes opgeslagen. Selecteer een halte en druk op de ster rechtsboven om hem op te slaan.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview(showBackground = true)
fun DeparturesScreenPreview() {
    DeparturesScreen(navController = rememberNavController(), null)
}