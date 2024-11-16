package me.partypronl.tripswift

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Departure(val lineNumber: String, val destination: String, val transportType: TransportType, val plannedDeparture: Date, val expectedDeparture: Date, val platform: String? = null, val hasPlatformChanged: Boolean = false, val tips: List<String> = listOf(), val cancelled: Boolean = false) {
    enum class TransportType(val identificationString: String, val tabName: String, val iconId: Int) {
        BUS("Bus", "Bus", R.drawable.baseline_directions_bus_24),
        METRO("Metro", "Metro", R.drawable.baseline_directions_subway_24),
        TRAM("Tram", "Tram", R.drawable.baseline_tram_24),
        FERRY("Veer", "Veerpont", R.drawable.baseline_directions_boat_filled_24),
        TRAIN("Trein", "Trein", R.drawable.baseline_train_24);

        companion object {
            fun fromIdentificationString(string: String): TransportType {
                return entries.first { it.identificationString == string }
            }
        }
    }

    companion object {
        fun fromBTMFJSON(json: JsonObject): Departure {
            val lineNumber = json.get("LineNumber").asString
            val destination = if(json.has("Destination")) json.get("Destination").asString else json.get("LineName").asString
            val transportType = TransportType.fromIdentificationString(json.get("TransportType").asString)

            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val plannedDeparture = format.parse(json.get("PlannedDeparture").asString)!!
            val expectedDeparture = format.parse(json.get("ExpectedDeparture").asString)!!

            val platform = if(json.has("Platform")) json.get("Platform").asString else null
            val cancelled = if(json.has("VehicleStatus")) {
                json.get("VehicleStatus").asString.equals("CANCEL", true)
            } else {
                false
            }

            return Departure(lineNumber, destination, transportType, plannedDeparture, expectedDeparture, platform, false, listOf(), cancelled)
        }

        fun fromTrainJSON(json: JsonObject): Departure {
            val lineNumber = json.get("TransportTypeCode").asString
            var destination = json.get("Destination").asString
            if(json.has("Via") && !json.get("Via").isJsonNull) destination += " (via " + json.get("Via").asString + ")"
            val tips = mutableListOf<String>()

            if(json.has("Tips")) {
                json.get("Tips").asJsonArray.forEach {
                    tips.add(it.asString)
                }
            }

            if(json.has("Comments")) {
                json.get("Comments").asJsonArray.forEach {
                    if(!it.asString.equals("gewijzigd vertrekspoor", true)) tips.add(it.asString)
                }
            }

            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val plannedDeparture = format.parse(json.get("PlannedDeparture").asString)!!
            val delayMinutes = json.get("Delay").asInt
            val expectedDeparture = Date(plannedDeparture.time + (delayMinutes * 60 * 1000))

            val platform = if(json.has("Platform")) json.get("Platform").asString else null
            val hasPlatformChanged = if(json.has("PlatformChange")) json.get("PlatformChange").asBoolean else false

            val cancelled = tips.any { it.contains("rijdt niet", true) }

            return Departure(lineNumber, destination, TransportType.TRAIN, plannedDeparture, expectedDeparture, platform, hasPlatformChanged, tips, cancelled)
        }
    }

    @Composable
    fun RenderDeparture() {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 4.dp)) {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                if(plannedDeparture.minutes != expectedDeparture.minutes) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(plannedDeparture),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                        color = MaterialTheme.colorScheme.error
                    )

                    val tempExpectedDeparture = expectedDeparture.clone() as Date
                    if(tempExpectedDeparture.seconds > 0 && (expectedDeparture.minutes >= plannedDeparture.minutes)) {
                        tempExpectedDeparture.seconds = 0
                        tempExpectedDeparture.minutes += 1
                    }

                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(tempExpectedDeparture),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(plannedDeparture),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment =  Alignment.CenterVertically
                    ) {
                        Text(
                            text = lineNumber,
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    MaterialTheme.shapes.medium
                                )
                                .padding(8.dp, 4.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = destination,
                            modifier = Modifier.padding(start = 8.dp),
                            style = if(!cancelled) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                            color = if(!cancelled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if(platform != null && platform != "-") {
                    if(!hasPlatformChanged) {
                        Text(
                            "Platform $platform",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning, "Platform gewijzigd",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .width(20.dp)
                            )

                            Text(
                                "Platform gewijzigd naar $platform",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if(cancelled) {
                    Tip("Vervallen")
                }

                if(tips.isNotEmpty()) {
                    tips.forEach {
                        Tip(it)
                    }
                }
            }
        }
    }
}

@Composable
fun Tip(tip: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Warning, "Pas op",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(start = 4.dp)
                .width(20.dp)
        )

        Text(
            tip,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RenderDeparturePreview() {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    val plannedDeparture = format.parse("2024-01-27T13:34:00")
    val expectedDeparture = format.parse("2024-01-27T13:35:19")
    Departure("129", "Purmerend Tramplein maar dan met een hele lange tekst erachter voor tekst wrapping", Departure.TransportType.BUS, plannedDeparture, expectedDeparture, "2b", false, listOf("Tip 1", "Tip 2"), true).RenderDeparture()
}