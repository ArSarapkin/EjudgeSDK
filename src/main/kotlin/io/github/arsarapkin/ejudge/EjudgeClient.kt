package io.github.arsarapkin.ejudge

import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private const val ROLE = "5"
private const val LOCALE_ID = "0"

private val SID_REGEX = Regex("SID=([A-Za-z0-9]+)&")
private val EJSID_REGEX = Regex("EJSID=([^;]+)")

/**
 * Точка входа в SDK: хранит адрес ejudge и учётные данные судьи.
 * Для работы с конкретным контестом вызовите [connect].
 */
class EjudgeClient(
    val baseUrl: String,
    val username: String,
    private val password: String,
) {
    /**
     * Логинится в контест [contestId] и возвращает сессию для работы с ним.
     *
     * @throws EjudgeException если ejudge не вернул SID/EJSID (например, неверный логин или нет доступа).
     */
    fun connect(contestId: Long): EjudgeConnection {
        val cookieManager = CookieManager(null, CookiePolicy.ACCEPT_ALL)
        val httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildLoginUrl(contestId)))
            .GET()
            .build()

        val response = sendWithRetry(httpClient, request)

        return EjudgeConnection(
            contestId = contestId,
            sid = extractSid(response.uri().toString()),
            ejsid = extractEjsid(response, cookieManager),
            baseUrl = baseUrl,
            judgeUrl = "$baseUrl$JUDGE_PATH",
            httpClient = httpClient,
        )
    }

    private fun buildLoginUrl(contestId: Long): String {
        val query = buildQuery(
            "login" to username,
            "password" to password,
            "contest_id" to contestId.toString(),
            "role" to ROLE,
            "locale_id" to LOCALE_ID,
            "action_2" to "Submit",
        )
        return "$baseUrl$JUDGE_PATH?$query"
    }

    private fun extractSid(url: String): String =
        SID_REGEX.find(url)?.groupValues?.get(1)
            ?: throw EjudgeException("SID не найден в ответе ejudge")

    private fun extractEjsid(response: HttpResponse<String>, cookieManager: CookieManager): String {
        val setCookie = response.headers().firstValue("Set-Cookie").orElse("")
        return EJSID_REGEX.find(setCookie)?.groupValues?.get(1)
            ?: cookieManager.cookieStore.cookies.firstOrNull { it.name == "EJSID" }?.value
            ?: throw EjudgeException("EJSID не найден в ответе ejudge")
    }
}
