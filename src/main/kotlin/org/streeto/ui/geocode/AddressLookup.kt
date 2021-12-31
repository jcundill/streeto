package org.streeto.ui.geocode

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.json.Json


data class Address(
    val name: String,
    val lat: Double,
    val lon: Double
)

class AddressLookup {

    fun lookup(address: String): List<Address> {
        val formattedAddress = address.replace(" ", "+")
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://nominatim.openstreetmap.org/search?q=$formattedAddress&format=json&addressdetails=0"))
            .setHeader("User-Agent", "StreetO [github.com/jcundill/streeto]")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())

        return if (response.statusCode() == 200) {
            val jsonArray = Json.createReader(response.body()).readArray()
            jsonArray.map {
                val json = it.asJsonObject()
                Address(
                    json.getString("display_name"),
                    json.getString("lat").toDouble(),
                    json.getString("lon").toDouble()
                )
            }
        } else {
            emptyList()
        }
    }
}
