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
    val status: EjudgeRunStatus,
    /** Исходный код; `null`, если посылки загружены без кода. */
    val code: String?,
) {
    /**
     * Позиция задачи в контесте по букве в [problem], начиная с 0: `A` → 0, `B` → 1, ...
     * Регистр не важен. Возвращает `null`, если [problem] — не одна латинская буква.
     */
    fun problemPos(): Int? {
        val letter = problem.trim().singleOrNull()?.uppercaseChar() ?: return null
        return if (letter in 'A'..'Z') letter - 'A' else null
    }
}
