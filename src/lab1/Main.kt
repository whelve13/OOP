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

class Logger(
    val name: String,
    private val formatter: Formatter,
    private val sink: ConsoleSink
) {
    // composition constructor
    constructor(name: String) : this(name, Formatter(), ConsoleSink())
    companion object {
        // level static
        var level: LogLevel = LogLevel.INFO
    }
    fun log(msgLevel: LogLevel, message: String) {
        if (msgLevel >= level) {
            val formattedLine = formatter.format(msgLevel, name, message)
            sink.write(formattedLine)
        }
    }
    fun trace(message: String) = log(LogLevel.TRACE, message)
    fun info(message: String) = log(LogLevel.INFO, message)
    fun warn(message: String) = log(LogLevel.WARN, message)
    fun error(message: String) = log(LogLevel.ERROR, message)
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

    val sharedFormatter = Formatter("[%t] [%l] %n: %m")
    val console = ConsoleSink()
    val parserLogger = Logger("Parser", sharedFormatter, console)
    val managerLogger = Logger("Manager", sharedFormatter, console)

    val specialLogger = Logger("Special", Formatter("%l >> %m"), console)
    println("Testing Aggregation")
    parserLogger.info("Reading commands")
    managerLogger.warn("Low stock")
    specialLogger.error("Custom format error")

    println("\nTesting ParenthesesValidator (with composition)")
    val validator = ParenthesesValidator()
    validator.isValid("()")
    validator.isValid(")(")


}