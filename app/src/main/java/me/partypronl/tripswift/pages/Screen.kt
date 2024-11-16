package me.partypronl.tripswift.pages

sealed class Screen(val route: String) {
    object Home: Screen("home")
    object Departures: Screen("departures")
    object StopDepartures: Screen("stop-departures/{stopName}/{stopTown}")
    object Route: Screen("route")
}