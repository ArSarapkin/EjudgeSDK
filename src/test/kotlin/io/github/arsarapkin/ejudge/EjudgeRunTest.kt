package io.github.arsarapkin.ejudge

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EjudgeRunTest {
    private fun runWithProblem(problem: String) = EjudgeRun(
        baseUrl = "u",
        contestId = 1,
        runId = 1,
        time = LocalDateTime.of(2026, 9, 27, 17, 5, 3),
        login = "",
        author = "",
        problem = problem,
        language = "",
        status = EjudgeRunStatus.OK,
        score = null,
        code = null,
    )

    @Test
    fun `problemPos maps letters to zero-based positions`() {
        assertEquals(0, runWithProblem("A").problemPos())
        assertEquals(1, runWithProblem("b").problemPos())
        assertEquals(25, runWithProblem("Z").problemPos())
    }

    @Test
    fun `problemPos returns null for non-letter problems`() {
        assertNull(runWithProblem("").problemPos())
        assertNull(runWithProblem("1").problemPos())
        assertNull(runWithProblem("A1").problemPos())
        assertNull(runWithProblem("Я").problemPos())
    }
}
