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
    }

    private fun formatMessage(messageLevel: LogLevel, message: String): String {
        val currentTime = LocalTime.now().format(timeFormatter)
        return Regex("%[tmln]").replace(format) { match ->
            when (match.value) {
                "%t" -> currentTime
                "%m" -> message
                "%n" -> name
                "%l" -> messageLevel.name
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
    fun isValid(input: String): Boolean {
        var openCount = 0

        for (char in input) {
            when (char) {
                '(' -> openCount++
                ')' -> {
                    if (openCount == 0) {
                        return false
                    }
                    openCount--
                }
                else -> continue
            }
        }
        return openCount == 0
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


    val validator = ParenthesesValidator()

    val testCases = listOf(
        "()" to true,
        ")((()))" to false,
        "(" to false,
        "(())((()())())" to true,

        "" to true,
        "(()" to false,
        "())" to false
    )

    println("ParenthesesValidator Tests")
    var allPassed = true
    for ((input, expected) in testCases) {
        val actual = validator.isValid(input)
        val passed = (actual == expected)
        if (!passed) allPassed = false

        val status = if (passed) "Passed" else "Failed"
        println("[$status] Input: \"$input\" -> Expected: $expected, Actual: $actual")
    }
    println("All tests passed: $allPassed")


}