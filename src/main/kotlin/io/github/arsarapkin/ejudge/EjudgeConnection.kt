package io.github.arsarapkin.ejudge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest

private const val CODE_FETCH_CONCURRENCY = 50

private const val ACTION_RUNS_CSV = "152"
private const val ACTION_RUN_SOURCE = "91"

/**
 * Авторизованная сессия судьи в одном контесте. Создаётся через [EjudgeClient.connect].
 */
class EjudgeConnection internal constructor(
    val contestId: Long,
    val sid: String,
    val ejsid: String,
    private val baseUrl: String,
    private val judgeUrl: String,
    private val httpClient: HttpClient,
) {
    private val noRedirectClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
    }

    /**
     * Возвращает посылки контеста с `runId >= firstRunId`.
     * Если [loadCodes] равен `false`, исходный код не скачивается и `code` у всех посылок равен `null`.
     */
    fun getRuns(loadCodes: Boolean = true, firstRunId: Int = 0): List<EjudgeRun> {
        val rows = fetchRunRows().filter { it.runId >= firstRunId }
        if (!loadCodes) return rows
        val codeByRunId = fetchCodesAsync(rows.map { it.runId })
        return rows.map { it.copy(code = codeByRunId[it.runId] ?: "") }
    }

    /** Ставит посылке статус OK. */
    fun accept(run: EjudgeRun): EjudgeRun {
        val query = buildQuery(
            "SID" to sid,
            "run_id" to run.runId.toString(),
            "action_241" to "Just OK the run",
        )
        sendWithRetry(noRedirectClient, buildRequest("$judgeUrl?$query"))
        return run.copy(status = "OK")
    }

    /** Оставляет комментарий [message] к посылке и вызывает её на защиту (статус SM). */
    fun summon(run: EjudgeRun, message: String): EjudgeRun {
        val query = buildQuery(
            "SID" to sid,
            "msg_text" to message,
            "run_id" to run.runId.toString(),
            "action_239" to "Send run comment and SUMMON run",
        )
        sendWithRetry(noRedirectClient, buildRequest("$judgeUrl?$query"))
        return run.copy(status = "SM")
    }

    private fun fetchRunRows(): List<EjudgeRun> {
        val query = buildQuery("SID" to sid, "action" to ACTION_RUNS_CSV)
        val csvText = sendWithRetry(httpClient, buildRequest("$judgeUrl?$query")).body()
        return parseRunsCsv(csvText, baseUrl, contestId)
    }

    private fun fetchCodesAsync(runIds: List<Int>): Map<Int, String> = runBlocking(Dispatchers.IO) {
        val semaphore = Semaphore(CODE_FETCH_CONCURRENCY)
        runIds.map { runId ->
            async {
                semaphore.withPermit { runId to fetchCode(runId) }
            }
        }.awaitAll().toMap()
    }

    private fun fetchCode(runId: Int): String {
        val query = buildQuery("SID" to sid, "action" to ACTION_RUN_SOURCE, "run_id" to runId.toString())
        return sendWithRetry(httpClient, buildRequest("$judgeUrl?$query")).body()
    }

    private fun buildRequest(url: String): HttpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Cookie", "EJSID=$ejsid")
            .GET()
            .build()
}
