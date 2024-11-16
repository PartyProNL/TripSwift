package me.partypronl.tripswift.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.partypronl.tripswift.MainActivity
import me.partypronl.tripswift.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "TRIPSWIFT",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 5.0.sp),
                    )
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        // TODO Open settings
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Instellingen")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "TripSwift is nog in ontwikkeling. Houd rekening met bugs en andere problemen.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Text(
                text = "Opgeslagen haltes",
                style = MaterialTheme.typography.titleMedium
            )

            if (MainActivity.settings == null || MainActivity.settings!!.savedStops.isEmpty()) {
                NoSavedStops()
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MainActivity.settings!!.savedStops.forEach { stop ->
                        stop.RenderSavedStop(navController)
                    }
                }
            }

            Text(
                text = "Jouw aankomende ritten",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            NoSavedRides()
        }
    }
}

@Composable
fun NoSavedRides() {
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
            text = "Je hebt nog geen ritten opgeslagen.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController()
    )
}