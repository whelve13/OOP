import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Formatter(private var format: String = "[%t] [%l] %n: %m") {
    companion object {
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        private val randomMessages: Map<LogLevel, List<String>> = mapOf(
            LogLevel.TRACE to listOf(
                "Inspecting step",
                "Trace checkpoint reached",
                "Event recorded"
            ),
            LogLevel.INFO to listOf(
                "Operation completed",
                "Everything looks good",
                "Here is your result"
            ),
            LogLevel.WARN to listOf(
                "Proceed with caution",
                "Unusual condition detected",
                "Bad state encountered"
            ),
            LogLevel.ERROR to listOf(
                "Operation failed",
                "Invalid input detected",
                "Critical mismatch occured"
            )
        )
    }
    fun format(level: LogLevel, name: String, message: String): String {
        val currentTime = LocalTime.now().format(timeFormatter)
        return Regex("%[tmlnr]").replace(format) { match ->
            when (match.value) {
                "%t" -> currentTime
                "%m" -> message
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

class Logger(val name: String) {
    companion object {
        var level: LogLevel = LogLevel.INFO

        var format: String = "[%t] [%l] %n: %m"

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        private val randomMessages: Map<LogLevel, List<String>> = mapOf(
            LogLevel.TRACE to listOf(
                "Inspecting step",
                "Trace checkpoint reached",
                "Event recorded"
            ),
            LogLevel.INFO to listOf(
                "Operation completed",
                "Everything looks good",
                "Here is your result"
            ),
            LogLevel.WARN to listOf(
                "Proceed with caution",
                "Unusual condition detected",
                "Bad state encountered"
            ),
            LogLevel.ERROR to listOf(
                "Operation failed",
                "Invalid input detected",
                "Critical mismatch occured"
            )
        )
    }

    private fun formatMessage(messageLevel: LogLevel, message: String): String {
        val currentTime = LocalTime.now().format(timeFormatter)
        return Regex("%[tmlnr]").replace(format) { match ->
            when (match.value) {
                "%t" -> currentTime
                "%m" -> message
                "%n" -> name
                "%l" -> messageLevel.name
                "%r" -> randomMessages[messageLevel]?.random() ?: ""
                else -> match.value
            }
        }
    }
    
    fun log(messageLevel: LogLevel, message: String) {
        if (messageLevel >= level) {
            println(formatMessage(messageLevel, message))
        }
    }

fun trace(message: String) = log(LogLevel.TRACE, message)
fun info(message: String) = log(LogLevel.INFO, message)
fun warn(message:String) = log(LogLevel.WARN, message)
fun error(message:String) = log(LogLevel.ERROR, message)
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

    val defaultFormatter = Formatter()
    val shortFormatter = Formatter("[%l] %n >> %m")
    val randomFormatter = Formatter("[%l] (%r) %n: %m")
    println("Testing Default Formatter")
    println(defaultFormatter.format(LogLevel.INFO, "Shop", "System initialized"))
    println("\nTesting Short Formatter")
    println(shortFormatter.format(LogLevel.WARN, "Parser", "Empty line skipped"))
    println("\nTesting Random Formatter")
    println(randomFormatter.format(LogLevel.ERROR, "Reader", "File not found"))
    println("\nTesting Placeholder Safety")
    println(defaultFormatter.format(LogLevel.INFO, "Math", "Ratio is 100%n"))


}