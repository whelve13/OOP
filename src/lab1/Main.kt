import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ConsoleSink {
    fun write(line: String) {
        println(line)
    }
}

class Formatter(private var format: String = "[%t] [%l] %n: %m") {
    companion object {
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        private val randomMessages: Map<LogLevel, List<String>> = mapOf(
            LogLevel.TRACE to listOf(
                "Inspecting step",
                "Trace checkpoint reached",
                "Fine-grained event recorded"
            ),
            LogLevel.INFO to listOf(
                "Operation completed",
                "Everything looks good",
                "Here is your result"
            ),
            LogLevel.WARN to listOf(
                "Proceed with caution",
                "Unusual condition detected",
                "Non-optimal state encountered"
            ),
            LogLevel.ERROR to listOf(
                "Operation failed",
                "Invalid input detected",
                "Critical mismatch occurred"
            )
        )
    }
    private fun interpolateMessage(message: String, args: Array<out Any?>): String {
        if (args.isEmpty() && !message.contains("%%")) {
            return message
        }
        var argIndex = 0
        return Regex("%%|%").replace(message) { match ->
            if (match.value == "%%") {
                "%"
            } else if (argIndex < args.size) {
                args[argIndex++].toString()
            } else {
                "%"
            }
        }
    }
    fun format(level: LogLevel, name: String, message: String, vararg args: Any?): String {
        val currentTime = LocalTime.now().format(timeFormatter)
        val resolvedMessage = interpolateMessage(message, args)
        return Regex("%[tmlnr]").replace(format) { match ->
            when (match.value) {
                "%t" -> currentTime
                "%m" -> resolvedMessage
                "%n" -> name
                "%l" -> level.name
                "%r" -> randomMessages[level]?.random() ?: ""
                else -> match.value
            }
        }
    }
}

enum class LogLevel {
    TRACE, INFO, WARN, ERROR
}

class Logger(
    val name: String,
    private val formatter: Formatter,
    private val sink: ConsoleSink
) {
    constructor(name: String) : this(name, Formatter(), ConsoleSink())
    companion object {
        var level: LogLevel = LogLevel.INFO
    }
    fun log(msgLevel: LogLevel, message: String, vararg args: Any?) {
        // first check level
        if (msgLevel >= level) {
            val formattedLine = formatter.format(msgLevel, name, message, *args)
            sink.write(formattedLine)
        }
    }
    fun trace(message: String, vararg args: Any?) = log(LogLevel.TRACE, message, *args)
    fun info(message: String, vararg args: Any?) = log(LogLevel.INFO, message, *args)
    fun warn(message: String, vararg args: Any?) = log(LogLevel.WARN, message, *args)
    fun error(message: String, vararg args: Any?) = log(LogLevel.ERROR, message, *args)
}

class ParenthesesValidator {

    private val logger = Logger("Parentheses")

    fun isValid(input: String): Boolean {
        var openCount = 0

        for (char in input) {
            when (char) {
                '(' -> {
                    openCount++
                    logger.trace("Found opening parentheses '('. Currently unmatched: $openCount")
                }
                ')' -> {
                    if (openCount == 0) {
                        logger.warn("Closing parentheses ')' found with no matching opening parentheses.")
                        logger.error("Validation failed: string \"$input\" has unmatched closing parentheses.")
                        logger.info("Validation result for \"$input\": false")
                        return false
                    }
                    openCount--
                    logger.info("Matched pair '()' found. Remaining unmatched: $openCount")
                }
                else -> continue
            }
        }
        val result = (openCount == 0)

        if (!result) {
            logger.error("Validation failed: string \"$input\" has $openCount unclosed opening parentheses.")
        }

        logger.info("Validation result for \"$input\": $result")
        return result
    }
}

fun main() {
    Logger.level = LogLevel.TRACE
    val logger = Logger("BonusTest")
    println("Normal varargs")
    logger.info("Pair found at % and %", 3, 7)
    println("\nEscaped literal %%")
    logger.info("Progress is 100%% complete for %", "Lab2")
    println("\nEdge cases: mismatched counts")
    logger.warn("More placeholders: % and % and %", "one")
    logger.warn("More arguments: %", "first", "second", "third")
    println("\nPlaceholder safety with varargs")
    logger.info("User sent %", "%n %t")


}