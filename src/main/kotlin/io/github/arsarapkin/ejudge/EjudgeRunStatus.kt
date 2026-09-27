package io.github.arsarapkin.ejudge

/** Статус посылки в ejudge. [code] — короткое обозначение, которое показывает ejudge. */
enum class EjudgeRunStatus(val code: String) {
    OK("OK"),
    COMPILATION_ERROR("CE"),
    RUNTIME_ERROR("RT"),
    TIME_LIMIT_EXCEEDED("TL"),
    PRESENTATION_ERROR("PE"),
    WRONG_ANSWER("WA"),
    CHECK_FAILED("CF"),
    PARTIAL_SOLUTION("PT"),
    ACCEPTED_FOR_TESTING("AC"),
    IGNORED("IG"),
    DISQUALIFIED("DQ"),
    PENDING("PD"),
    MEMORY_LIMIT_EXCEEDED("ML"),
    SECURITY_VIOLATION("SE"),
    STYLE_VIOLATION("SV"),
    WALL_TIME_LIMIT_EXCEEDED("WT"),
    PENDING_REVIEW("PR"),
    REJECTED("RJ"),
    SKIPPED("SK"),
    SYNC_ERROR("SY"),
    SUMMONED("SM"),
    RUNNING("RU"),
    COMPILED("CD"),
    COMPILING("CG"),
    AVAILABLE("AV"),
    EMPTY("EM"),
    VIRTUAL_START("VS"),
    VIRTUAL_STOP("VT"),

    /** Статус, которого нет в этом списке. */
    UNKNOWN(""),
    ;

    companion object {
        private val byCode = entries.filter { it != UNKNOWN }.associateBy { it.code }

        /** Возвращает статус по короткому обозначению или [UNKNOWN], если оно не распознано. */
        fun fromCode(code: String): EjudgeRunStatus = byCode[code.trim()] ?: UNKNOWN
    }
}
