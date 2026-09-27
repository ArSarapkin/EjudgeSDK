package io.github.arsarapkin.ejudge

import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal const val JUDGE_PATH = "/cgi-bin/new-judge"
internal const val MAX_RETRIES = 5

internal fun buildQuery(vararg params: Pair<String, String>): String =
    params.joinToString("&") { (k, v) -> "${encode(k)}=${encode(v)}" }

private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8)

internal fun sendWithRetry(httpClient: HttpClient, request: HttpRequest): HttpResponse<String> {
    repeat(MAX_RETRIES) { attempt ->
        runCatching {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() < 400) return response
        }
        if (attempt < MAX_RETRIES - 1) Thread.sleep(1000)
    }
    throw EjudgeException("Не удалось выполнить запрос к ejudge после $MAX_RETRIES попыток")
}
