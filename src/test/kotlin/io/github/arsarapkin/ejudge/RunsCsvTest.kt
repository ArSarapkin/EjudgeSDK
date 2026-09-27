package io.github.arsarapkin.ejudge

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class RunsCsvTest {
    @Test
    fun `parses rows and skips malformed ones`() {
        val csv = "﻿RunId;Year;Mon;Day;Hour;Min;Sec;Login;Name;Problem;Language;Stat_Short\n" +
            "7;2026;9;27;17;5;3;ivanov;Ivan Ivanov;A;g++;OK\n" +
            "broken;2026;9;27;17;5;3;x;x;B;g++;WA\n"

        val runs = parseRunsCsv(csv, "https://ejudge.example", 42)

        assertEquals(
            listOf(
                EjudgeRun(
                    baseUrl = "https://ejudge.example",
                    contestId = 42,
                    runId = 7,
                    time = LocalDateTime.of(2026, 9, 27, 17, 5, 3),
                    login = "ivanov",
                    author = "Ivan Ivanov",
                    problem = "A",
                    language = "g++",
                    status = "OK",
                    code = null,
                ),
            ),
            runs,
        )
    }

    @Test
    fun `returns empty list without data rows`() {
        assertEquals(emptyList(), parseRunsCsv("RunId;Year\n", "u", 1))
    }
}
