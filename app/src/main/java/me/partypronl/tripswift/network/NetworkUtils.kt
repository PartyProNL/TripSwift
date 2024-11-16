package me.partypronl.tripswift.network

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.partypronl.tripswift.BuildConfig
import me.partypronl.tripswift.Departure
import me.partypronl.tripswift.Stop
import me.partypronl.tripswift.StopType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class NetworkUtils {
    val client = OkHttpClient()

    @SuppressLint("MissingPermission")
    fun getStations(locationProvider: FusedLocationProviderClient, callback: (List<Stop>) -> Unit) {
        getLocation(locationProvider) { location ->
            if(location == null) {
                callback.invoke(listOf())
                return@getLocation
            }

            val latitude = location.latitude
            val longitude = location.longitude

            val request = Request.Builder()
                .url("https://api.vertrektijd.info/stop/_geo/${latitude}/${longitude}/0.5")
                .get()
                .addHeader("X-Vertrektijd-Client-Api-Key", API_KEY)
                .build()

            val response = client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    val json = JsonParser.parseString(response.body?.string() ?: "").asJsonArray
                    var stops = mutableListOf<Stop>()

                    json.forEach {
                        val typeString = it.asJsonObject.get("Type").asString

                        val type = if (typeString == "TRAIN") {
                            StopType.TRAIN
                        } else {
                            StopType.BUS
                        }

                        stops.add(Stop(it.asJsonObject.get("StopName").asString, it.asJsonObject.get("Town").asString, type))
                    }

                    stops = stops.distinctBy { it.name + it.town + it.type.name }.toMutableList()

                    callback.invoke(stops)
                }
            })
        }
    }

    fun getDepartures(stopName: String, stopTown: String, callback: (List<Departure>) -> Unit) {
        val request = Request.Builder()
            .url("https://api.vertrektijd.info/departures/_nametown/${stopTown}/$stopName")
            .get()
            .addHeader("X-Vertrektijd-Client-Api-Key", API_KEY)
            .build()

        val response = client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val json = JsonParser.parseString(response.body?.string() ?: "").asJsonObject

                val departures = mutableListOf<Departure>()
                json.getAsJsonArray("BTMF").forEach { stopJson ->
                    stopJson.asJsonObject.getAsJsonArray("Departures").forEach { departureJson ->
                        departures.add(Departure.fromBTMFJSON(departureJson.asJsonObject))
                    }
                }

                json.getAsJsonArray("TRAIN").forEach { stopJson ->
                    stopJson.asJsonObject.getAsJsonArray("Departures").forEach { departureJson ->
                        departures.add(Departure.fromTrainJSON(departureJson.asJsonObject))
                    }
                }

                departures.sortBy { it.plannedDeparture.time }

                callback.invoke(departures)
            }
        })
    }

    fun getSearchResults(query: String, locationProvider: FusedLocationProviderClient?, callback: (List<Stop>) -> Unit) {
        if(locationProvider == null) {
            internalSearchResults(query, null, callback)
            return
        }

        getLocation(locationProvider) { location ->
            internalSearchResults(query, location, callback)
        }
    }

    private fun internalSearchResults(query: String, location: Location?, callback: (List<Stop>) -> Unit) {
        val request = Request.Builder()
            .url("https://api.vertrektijd.info/stop/_name/${query}")
            .get()
            .addHeader("X-Vertrektijd-Client-Api-Key", API_KEY)
            .build()

        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val json = JsonParser.parseString(response.body?.string() ?: "")
                val stops = mutableListOf<Stop>()

                if(json.isJsonObject) {
                    callback.invoke(stops)
                    return
                }

                json.asJsonArray.forEach {
                    val typeString = it.asJsonObject.get("Type").asString

                    val type = if (typeString == "TRAIN") {
                        StopType.TRAIN
                    } else {
                        StopType.BUS
                    }

                    val stop = Stop(
                        it.asJsonObject.get("StopName").asString,
                        it.asJsonObject.get("Town").asString,
                        type
                    )
                    stop.latitude = it.asJsonObject.get("Latitude").asDouble
                    stop.longitude = it.asJsonObject.get("Longitude").asDouble
                    stops.add(stop)
                }

                if(location != null) {
                    stops.sortBy {
                        val distance = FloatArray(1)
                        Location.distanceBetween(location.latitude, location.longitude, it.latitude ?: 0.0, it.longitude ?: 0.0, distance)
                        distance[0]
                    }
                }

                callback.invoke(stops)
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun getLocation(locationProvider: FusedLocationProviderClient, callback: (Location?) -> Unit) = runBlocking {
        launch {
            locationProvider.lastLocation.addOnSuccessListener { location ->
                if(location == null) {
                    callback.invoke(null)
                }

                callback.invoke(location)
            }
        }
    }

    companion object {
        const val API_KEY = BuildConfig.API_KEY
    }
}