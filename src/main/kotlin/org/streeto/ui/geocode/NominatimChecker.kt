package org.streeto.ui.geocode

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class NominatimChecker {
    fun isOkToUseNominatim(): Boolean {
        return try {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.streeto.org/check-nominatim.html"))
                .setHeader("User-Agent", "Streeto")
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.statusCode() == 200 && response.body().startsWith("OK")
        } catch (e: Exception) {
            false
        }
    }
}