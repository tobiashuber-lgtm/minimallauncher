package com.minimal.launcher

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Nutzt Open-Meteo (open-meteo.com) - komplett kostenlos, kein API-Key
// noetig. Alle Netzwerkaufrufe laufen in einem Hintergrund-Thread, das
// Ergebnis kommt per Handler zurueck auf den Main-Thread.
object WeatherHelper {

    private val handler = Handler(Looper.getMainLooper())

    data class GeocodeResult(val displayName: String, val lat: Float, val lon: Float)
    data class WeatherResult(val temperatureC: Int, val weatherCode: Int)

    fun geocodeCity(cityName: String, callback: (GeocodeResult?) -> Unit) {
        Thread {
            val result = try {
                val encoded = URLEncoder.encode(cityName, "UTF-8")
                val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=de")
                val json = fetchJson(url)
                val results = json?.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val first = results.getJSONObject(0)
                    val name = first.optString("name", cityName)
                    val country = first.optString("country", "")
                    val displayName = if (country.isNotBlank()) "$name, $country" else name
                    GeocodeResult(
                        displayName,
                        first.optDouble("latitude").toFloat(),
                        first.optDouble("longitude").toFloat()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
            handler.post { callback(result) }
        }.start()
    }

    fun fetchWeather(lat: Float, lon: Float, callback: (WeatherResult?) -> Unit) {
        Thread {
            val result = try {
                val url = URL(
                    "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
                )
                val json = fetchJson(url)
                val current = json?.optJSONObject("current_weather")
                if (current != null) {
                    WeatherResult(
                        current.optDouble("temperature").let { Math.round(it).toInt() },
                        current.optInt("weathercode", -1)
                    )
                } else null
            } catch (e: Exception) {
                null
            }
            handler.post { callback(result) }
        }.start()
    }

    private fun fetchJson(url: URL): JSONObject? {
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(text)
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
