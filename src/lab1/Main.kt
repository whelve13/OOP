import java.util.Locale
import kotlin.math.sqrt
import kotlin.math.round
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Display(
    val model: String,
    private val width: Int,
    private val height: Int,
    private val ppi: Float
) {
    private fun diagonalInches(): Double {
        val w = width.toDouble()
        val h = height.toDouble()
        return round((sqrt(w * w + h * h) / ppi) * 10.0) / 10.0
    }
    private fun formatInches(value: Double): String {
        return String.format(Locale.US, "%.1f", value)
    }
    fun compareSize(other: Display) {
        val mySize = diagonalInches()
        val otherSize = other.diagonalInches()
        val myFormatted = formatInches(mySize)
        val otherFormatted = formatInches(otherSize)
        when {
            mySize > otherSize ->
                println("$model ($myFormatted\") is bigger than ${other.model} ($otherFormatted\").")
            mySize < otherSize ->
                println("$model ($myFormatted\") is smaller than ${other.model} ($otherFormatted\").")
            else ->
                println("$model ($myFormatted\") is the same size as ${other.model} ($otherFormatted\").")
        }
    }
    fun compareSharpness(other: Display) {
        when {
            ppi > other.ppi ->
                println("$model ($ppi ppi) is sharper than ${other.model} (${other.ppi} ppi).")
            ppi < other.ppi ->
                println("$model ($ppi ppi) is less sharp than ${other.model} (${other.ppi} ppi).")
            else ->
                println("$model ($ppi ppi) has the same sharpness as ${other.model} (${other.ppi} ppi).")
        }
    }
    fun compareWithMonitor(other: Display) {
        println("Comparing $model vs ${other.model}")
        compareSize(other)
        compareSharpness(other)
        val mySize = diagonalInches()
        val otherSize = other.diagonalInches()
        val verdict = when {
            mySize > otherSize && ppi > other.ppi -> "$model wins on both size and sharpness."
            mySize < otherSize && ppi < other.ppi -> "${other.model} wins on both size and sharpness."
            mySize == otherSize && ppi > other.ppi -> "$model wins on sharpness (same size)."
            mySize == otherSize && ppi < other.ppi -> "${other.model} wins on sharpness (same size)."
            mySize > otherSize && ppi == other.ppi -> "$model wins on size (same sharpness)."
            mySize < otherSize && ppi == other.ppi -> "${other.model} wins on size (same sharpness)."
            mySize == otherSize && ppi == other.ppi -> "Both monitors match in size and sharpness."
            else -> "Tradeoff is one is bigger while the other is sharper."
        }
        println("Verdict: $verdict")
    }
}

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
    val dell = Display("Dell-U2723QE", 3840, 2160, 163.0f)
    val lg = Display("LG-27GP850", 2560, 1440, 108.8f)
    val samsung = Display("Samsung-Odyssey-G5", 2560, 1440, 93.0f)
    samsung.compareSize(lg)
    dell.compareSharpness(lg)
    println()
    dell.compareWithMonitor(samsung)
    println()
    dell.compareWithMonitor(lg)

    
}