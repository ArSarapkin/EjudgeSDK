package io.github.arsarapkin.ejudge

/** Ошибка при работе с ejudge: не удалось залогиниться или выполнить запрос. */
class EjudgeException(message: String) : RuntimeException(message)
