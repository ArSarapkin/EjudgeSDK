# EjudgeSDK

Kotlin SDK для работы с [ejudge](https://ejudge.ru) от имени судьи: получение посылок контеста вместе с исходным кодом, принятие посылок и вызов на защиту.

SDK работает через судейский интерфейс `/cgi-bin/new-judge`, поэтому учётной записи нужны права судьи в контесте.

## Установка

Библиотека публикуется в GitHub Packages. GitHub требует токен даже для чтения пакетов, поэтому сначала создайте [Personal Access Token (classic)](https://github.com/settings/tokens) с правом `read:packages` и добавьте его в `~/.gradle/gradle.properties`:

```properties
gpr.user=<ваш логин на GitHub>
gpr.key=<токен>
```

Затем подключите репозиторий и зависимость в `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/arsarapkin/EjudgeSDK")
        credentials {
            username = providers.gradleProperty("gpr.user").get()
            password = providers.gradleProperty("gpr.key").get()
        }
    }
}

dependencies {
    implementation("io.github.arsarapkin:ejudge-sdk:0.2.0")
}
```

Требуется JDK 26.

## Быстрый старт

```kotlin
import io.github.arsarapkin.ejudge.EjudgeClient

val client = EjudgeClient(
    baseUrl = "https://ejudge.example.com",
    username = "judge",
    password = "secret",
)

val connection = client.connect(contestId = 42)

val runs = connection.getRuns()
for (run in runs) {
    println("${run.runId} ${run.login} ${run.problem} ${run.status}")
}
```

## API

### `EjudgeClient`

Хранит адрес ejudge и учётные данные.

```kotlin
EjudgeClient(baseUrl: String, username: String, password: String)
```

- `baseUrl` — адрес сервера без завершающего `/`, например `https://ejudge.example.com`.
- `connect(contestId: Long): EjudgeConnection` — логинится в контест и возвращает сессию. Для каждого контеста нужен свой вызов `connect`.

### `EjudgeConnection`

Авторизованная сессия в одном контесте. Содержит `contestId`, `sid` и `ejsid`.

| Метод | Что делает |
|---|---|
| `getRuns(loadCodes: Boolean = true, firstRunId: Int = 0): List<EjudgeRun>` | Возвращает посылки с `runId >= firstRunId`. При `loadCodes = false` исходный код не скачивается |
| `accept(run: EjudgeRun): EjudgeRun` | Ставит посылке статус `OK` |
| `summon(run: EjudgeRun, message: String): EjudgeRun` | Оставляет комментарий к посылке и вызывает её на защиту (статус `SM`) |

`accept` и `summon` возвращают копию посылки с новым статусом, исходный объект не меняется.

`getRuns` сначала скачивает список посылок, а потом исходный код каждой из них, до 50 запросов параллельно. На больших контестах это долго, поэтому, если посылки уже сохранены у вас, передавайте `firstRunId`, чтобы скачать только новые. Если код не нужен, передайте `loadCodes = false`: тогда выполняется один запрос, а `code` у всех посылок равен `null`.

### `EjudgeRun`

```kotlin
data class EjudgeRun(
    val baseUrl: String,
    val contestId: Long,
    val runId: Int,
    val time: LocalDateTime,
    val login: String,
    val author: String,     // имя участника
    val problem: String,    // короткое имя задачи
    val language: String,
    val status: String,     // короткий статус: OK, WA, SM, ...
    val code: String?,      // исходный код; null, если getRuns(loadCodes = false)
)
```

## Ошибки

Если запрос к ejudge не удался (сетевая ошибка или HTTP-код 400 и выше), SDK повторяет его до 5 раз с паузой в 1 секунду. Если все попытки неудачны, бросается `EjudgeException`.

`EjudgeException` также бросается из `connect`, если ejudge не вернул SID или EJSID. Обычно это значит, что логин или пароль неверны или у учётной записи нет доступа к контесту.

```kotlin
try {
    client.connect(contestId)
} catch (e: EjudgeException) {
    println("Не удалось подключиться: ${e.message}")
}
```

Все методы блокирующие. В корутинах вызывайте их внутри `withContext(Dispatchers.IO)`.

## Сборка

```bash
./gradlew build
```
