package io.github.arsarapkin.ejudge

import java.time.LocalDateTime

internal fun parseRunsCsv(csvText: String, baseUrl: String, contestId: Long): List<EjudgeRun> {
    val lines = csvText.trimStart('﻿').lines().filter { it.isNotBlank() }
    if (lines.size < 2) return emptyList()
    val headers = lines[0].split(";")
    return lines.drop(1).mapNotNull { line ->
        buildRun(headers.zip(line.split(";")).toMap(), baseUrl, contestId)
    }
}

private fun buildRun(row: Map<String, String>, baseUrl: String, contestId: Long): EjudgeRun? {
    val runId = row["RunId"]?.trim()?.toIntOrNull() ?: return null
    val year = row["Year"]?.trim()?.toIntOrNull() ?: return null
    val month = row["Mon"]?.trim()?.toIntOrNull() ?: return null
    val day = row["Day"]?.trim()?.toIntOrNull() ?: return null
    val hour = row["Hour"]?.trim()?.toIntOrNull() ?: return null
    val min = row["Min"]?.trim()?.toIntOrNull() ?: return null
    val sec = row["Sec"]?.trim()?.toIntOrNull() ?: return null
    return EjudgeRun(
        baseUrl = baseUrl,
        contestId = contestId,
        runId = runId,
        time = LocalDateTime.of(year, month, day, hour, min, sec),
        login = row["Login"]?.trim() ?: "",
        author = row["Name"]?.trim() ?: "",
        problem = row["Problem"]?.trim() ?: "",
        language = row["Language"]?.trim() ?: "",
        status = row["Stat_Short"]?.trim() ?: "",
        code = null,
    )
}
