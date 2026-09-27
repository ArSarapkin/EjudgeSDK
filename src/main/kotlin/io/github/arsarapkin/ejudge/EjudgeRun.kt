package io.github.arsarapkin.ejudge

import java.time.LocalDateTime

/** Посылка в ejudge. */
data class EjudgeRun(
    val baseUrl: String,
    val contestId: Long,
    val runId: Int,
    val time: LocalDateTime,
    val login: String,
    val author: String,
    val problem: String,
    val language: String,
    val status: String,
    /** Исходный код; `null`, если посылки загружены без кода. */
    val code: String?,
)
