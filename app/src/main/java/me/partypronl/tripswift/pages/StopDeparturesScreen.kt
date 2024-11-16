package me.partypronl.tripswift.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.partypronl.tripswift.Departure
import me.partypronl.tripswift.MainActivity
import me.partypronl.tripswift.R
import me.partypronl.tripswift.Stop
import me.partypronl.tripswift.StopType
import me.partypronl.tripswift.network.NetworkUtils

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StopDeparturesScreen(
    navController: NavController,
    stopName: String,
    stopTown: String,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val departuresLoaded = remember { mutableStateOf(false) }
    val departuresData = remember {
        mutableStateOf(listOf<Departure>())
    }

    val networkUtils = NetworkUtils()
    LaunchedEffect(Unit) {
        networkUtils.getDepartures(stopName, stopTown) { departures ->
            departuresLoaded.value = true
            departuresData.value = departures
        }
    }


    var isFavorited by remember { mutableStateOf(false) }
    if(MainActivity.settings != null) {
        isFavorited = MainActivity.settings!!.savedStops.any { it.name == stopName && it.town == stopTown }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
                title = { Text(
                    text = stopName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )},
                navigationIcon = {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.popBackStack()
                            }
                    )
                },
                actions = {
                    IconButton(onClick = {
                        departuresData.value = listOf()
                        departuresLoaded.value = false
                        networkUtils.getDepartures(stopName, stopTown) { departures ->
                            departuresData.value = departures
                            departuresLoaded.value = true
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                        )
                    }

                    if(isFavorited) {
                        IconButton(onClick = {
                            isFavorited = false
                            MainActivity.settings?.savedStops?.removeIf { it.name == stopName && it.town == stopTown }
                            MainActivity.settings?.save()

                            scope.launch {
                                snackbarHostState.showSnackbar("Halte verwijderd")
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_star_24),
                                contentDescription = "Favorite",
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            var stopType = StopType.BUS
                            if (departuresLoaded.value && departuresData.value.any { it.transportType == Departure.TransportType.TRAIN }) {
                                stopType = StopType.TRAIN
                            }

                            isFavorited = true
                            MainActivity.settings?.savedStops?.add(
                                Stop(
                                    stopName,
                                    stopTown,
                                    stopType
                                )
                            )
                            MainActivity.settings?.save()

                            scope.launch {
                                snackbarHostState.showSnackbar("Halte opgeslagen")
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_star_outline_24),
                                contentDescription = "Favorite",
                            )
                        }
                    }
                }
            )
        }
    ) {

        if(departuresLoaded.value) {
            val selectedTab = remember { mutableIntStateOf(0) }
            val transportTypes = departuresData.value.map { it.transportType }.distinct().sortedBy { it.tabName }

            val pagerState = rememberPagerState {
                transportTypes.size
            }

            LaunchedEffect(selectedTab.intValue) {
                pagerState.animateScrollToPage(selectedTab.intValue)
            }

            LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                if(!pagerState.isScrollInProgress) selectedTab.intValue = pagerState.currentPage
            }

            Column(modifier = Modifier.padding(top = 64.dp)) {
                if (transportTypes.size > 1) {
                    TabRow(
                        selectedTabIndex = selectedTab.intValue,
                        indicator = { tabPositions ->
                            if (selectedTab.intValue < tabPositions.size) {
                                TabRowDefaults.PrimaryIndicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab.intValue]),
                                    shape = RoundedCornerShape(
                                        topStart = 3.dp,
                                        topEnd = 3.dp,
                                        bottomEnd = 0.dp,
                                        bottomStart = 0.dp,
                                    ),
                                )
                            }
                        },
                    ) {
                        transportTypes.forEach {
                            Tab(
                                selected = selectedTab.intValue == transportTypes.indexOf(it),
                                onClick = {
                                    selectedTab.intValue = transportTypes.indexOf(it)
                                },
                                text = {
                                    Text(
                                        text = it.tabName,
                                        color = if(selectedTab.intValue == transportTypes.indexOf(it)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = it.iconId),
                                        contentDescription = it.tabName,
                                        tint = if(selectedTab.intValue == transportTypes.indexOf(it)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.Top
                ) { pageIndex ->
                    Column(modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 80.dp)) {
                        for((index, value) in departuresData.value.filter { it.transportType == transportTypes[pageIndex] }.withIndex()) {
                            if(index != 0) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }

                            value.RenderDeparture()
                        }
                    }
                }

                if(departuresData.value.isEmpty()) {
                    NoDepartures()
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(text = "Vertrektijden worden geladen...", modifier = Modifier.padding(top = 40.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun NoDepartures() {
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
                text = "Geen vertrektijden gevonden",
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StopDeparturesScreenPreview() {
    StopDeparturesScreen(navController = rememberNavController(), "De Buurt", "Middenbeemster", SnackbarHostState())
}