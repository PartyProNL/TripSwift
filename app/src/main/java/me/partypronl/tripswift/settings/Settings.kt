package me.partypronl.tripswift.settings

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.partypronl.tripswift.Stop
import java.io.File

class Settings(context: Context) {
    val file = File(context.filesDir, "settings.json")

    val savedStops = mutableListOf<Stop>()

    init {
        if(!file.exists()) {
            file.createNewFile()
            save()
        }

        load()
    }

    fun load() {
        try {
            val json = JsonParser.parseString(file.readText()).asJsonObject

            savedStops.clear()
            json.getAsJsonArray("savedStops").forEach {
                savedStops.add(Stop.fromJSON(it.asJsonObject))
            }
        } catch (e: Exception) {
            save()
        }
    }

    fun save() {
        val json = JsonObject()

        val savedStopsArray = JsonArray()
        savedStops.forEach {
            savedStopsArray.add(it.toJSON())
        }
        json.add("savedStops", savedStopsArray)

        file.writeText(json.toString())
    }
}