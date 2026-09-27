package io.github.arsarapkin.ejudge

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class RunsCsvTest {
    @Test
    fun `parses rows and skips malformed ones`() {
        val csv = "﻿RunId;Year;Mon;Day;Hour;Min;Sec;Login;Name;Problem;Language;Stat_Short;Score\n" +
            "7;2026;9;27;17;5;3;ivanov;Ivan Ivanov;A;g++;OK;-1\n" +
            "broken;2026;9;27;17;5;3;x;x;B;g++;WA;-1\n"

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
                    status = EjudgeRunStatus.OK,
                    score = null,
                    code = null,
                ),
            ),
            runs,
        )
    }

    @Test
    fun `maps unrecognized status to UNKNOWN`() {
        val csv = "RunId;Year;Mon;Day;Hour;Min;Sec;Stat_Short\n" +
            "1;2026;9;27;17;5;3;SM\n" +
            "2;2026;9;27;17;5;3;XX\n"

        val statuses = parseRunsCsv(csv, "u", 1).map { it.status }

        assertEquals(listOf(EjudgeRunStatus.SUMMONED, EjudgeRunStatus.UNKNOWN), statuses)
    }

    @Test
    fun `reads score and treats -1 as no score`() {
        val csv = "RunId;Year;Mon;Day;Hour;Min;Sec;Stat_Short;Score\n" +
            "1;2026;9;27;17;5;3;PT;42\n" +
            "2;2026;9;27;17;5;3;OK;100\n" +
            "3;2026;9;27;17;5;3;OK;0\n" +
            "4;2026;9;27;17;5;3;OK;-1\n"

        val scores = parseRunsCsv(csv, "u", 1).map { it.score }

        assertEquals(listOf(42, 100, 0, null), scores)
    }

    @Test
    fun `returns empty list without data rows`() {
        assertEquals(emptyList(), parseRunsCsv("RunId;Year\n", "u", 1))
    }
}
