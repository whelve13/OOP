import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    val redLogger = Logger("Red")
    val blueLogger = Logger("Blue")

    println("Default level is ${Logger.level}. Default format is ${Logger.format}")
    redLogger.trace("Trace mesage") // no output
    redLogger.info("Info message") // yes
    blueLogger.warn("Warning message") // yes
    blueLogger.error("Error message") // yes

    println("Change Logger.level to WARN")
    Logger.level = LogLevel.WARN

    redLogger.trace("Trace message") // no
    blueLogger.info("Info message") // no
    redLogger.warn("Warn message") // yes
    blueLogger.error("Error message") // yes

    println("Change Logger.level to TRACE")
    Logger.level = LogLevel.TRACE

    redLogger.trace("Trace") // works

    println("Change Logger.format to [%l] %m")
    Logger.format = "[%l] %m"
    redLogger.warn("Be warned!")

    println("Change Logger.format to %n: %m")
    Logger.format = "%n: %m"
    blueLogger.error("Error message")

    println("Change Logger.format to %l - %n - %m")
    Logger.format = "%l - %n - %m"
    redLogger.info("Info Info Info")

    redLogger.info("test %t %m %n %l")
    println("\n")


    val validator = ParenthesesValidator()

    val cases = listOf(
        "()",
        ")((()))",
        "(",
        "(())((()())())",

        "",
        "(()",
        "())"
    )

    Logger.format = "[%t] [%l] %n: %m"

    println("Logger.level = TRACE")
    Logger.level = LogLevel.TRACE
    for (test in cases) {
        println("\nTesting: \"$test\"")
        validator.isValid(test)
    }

    println("\nLogger.level = INFO")
    Logger.level = LogLevel.INFO
    for (test in cases) {
        println("\nTesting: \"$test\"")
        validator.isValid(test)
    }

    println("\nLogger.level = ERROR")
    Logger.level = LogLevel.ERROR
    for (test in cases) {
        println("\nTesting: \"$test\"")
        val result = validator.isValid(test)
        println("Returned boolean: $result")
    }

    println("\nDemonstrating %r")
    Logger.format = "[%l] (%r) %n: %m"
    Logger.level = LogLevel.TRACE

    println("\nTesting '()' 3 times")
    validator.isValid("()")
    validator.isValid("()")
    validator.isValid("()")

    println("\nTesting invalid ')' to see WARN and ERROR messages pool")
    validator.isValid(")")

    println("\nTesting invalid '(' to see unclosed ERROR pool")
    validator.isValid("(")


}