package me.partypronl.tripswift

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.JsonObject

class Stop(val name: String, val town: String, val type: StopType) {
    var latitude: Double? = null
    var longitude: Double? = null

    @Composable
    fun RenderStop(navController: NavController? = null) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    navController?.navigate("stop-departures/$name/$town")
                }
        ) {
            Box(Modifier.background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)) {
                Icon(
                    painter = painterResource(id = type.iconId),
                    contentDescription = type.typeName,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "$name, $town", style = MaterialTheme.typography.bodyLarge, color =  MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = type.typeName, style = MaterialTheme.typography.bodyMedium, color =  MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }

    @Composable
    fun RenderSavedStop(navController: NavController? = null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp).clickable {
                navController?.navigate("stop-departures/$name/$town")
            }
        ) {
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = type.iconId),
                    contentDescription = type.typeName,
                    modifier = Modifier.width(32.dp).height(32.dp),
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }

            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 4.dp).width(64.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }

    fun toJSON(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("town", town)
        jsonObject.addProperty("type", type.name)
        return jsonObject
    }

    companion object {
        fun fromJSON(jsonObject: JsonObject): Stop {
            val name = jsonObject.get("name").asString
            val town = jsonObject.get("town").asString
            val type = StopType.valueOf(jsonObject.get("type").asString)
            return Stop(name, town, type)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopPreview() {
    Stop(town = "Eindhoven", name = "De Buurt langereeeeeeee", type = StopType.BUS).RenderSavedStop()
}