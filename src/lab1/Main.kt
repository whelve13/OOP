import java.util.Locale
import kotlin.math.sqrt
import kotlin.math.round
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.io.File
import java.io.IOException

class CommandReader(private val logger: Logger) {
    fun readLines(filePath: String): List<String>? {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            logger.error("Command file not found or is not a valid file: %", filePath)
            return null
        }
        return try {
            val lines = file.readLines()
            logger.trace("Successfully read % lines from %", lines.size, filePath)
            lines
        } catch (e: IOException) {
            logger.error("Failed to read file %: %", filePath, e.message ?: "unknown I/O error")
            null
        }
    }
}

class Display(
    val model: String,
    private val width: Int,
    private val height: Int,
    private val ppi: Float,
    private val logger: Logger
) {
    init {
        logger.trace("Display % created (%\", % ppi)", model, formatInches(diagonalInches()), ppi)
    }
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
        println("> Comparing $model vs ${other.model}")
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

class DisplayManager(
    val name: String,
    private val logger: Logger
) {
    private val assignedDisplays: MutableList<Display> = mutableListOf()

    init {
        logger.trace("Manager % created", name)
    }

    fun assignDisplay(d: Display) {
        if (assignedDisplays.contains(d)) {
            logger.warn("% already has % assigned; skipping duplicate", name, d.model)
            return
        }
        assignedDisplays.add(d)
        logger.trace("% assigned to %", d.model, name)
    }

    fun assist() {
        println("\n> Manager $name assisting")
        if (assignedDisplays.isEmpty()) {
            logger.warn("Manager % has no displays to assist with", name)
            return
        }
        if (assignedDisplays.size == 1) {
            logger.warn("Manager % only has 1 display (%); cannot compare", name, assignedDisplays[0].model)
            return
        }

        for (i in 0 until assignedDisplays.size - 1) {
            assignedDisplays[i].compareWithMonitor(assignedDisplays[i + 1])
        }
    }

    fun buyDisplay(d: Display): Display? {
        return if (assignedDisplays.remove(d)) {
            logger.info("% sold %", name, d.model)
            if (assignedDisplays.isEmpty()) {
                logger.warn("Manager % now has 0 displays left", name)
            }
            d
        } else {
            logger.error("Cannot buy % from %: display is not assigned to this manager", d.model, name)
            null
        }
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

fun findManager(name: String, managers: List<DisplayManager>): DisplayManager? {
    for (m in managers) {
        if (m.name == name) return m
    }
    return null
}

fun findDisplay(model: String, displays: List<Display>): Display? {
    for (d in displays) {
        if (d.model == model) return d
    }
    return null
}

fun parseCommand(
    lineNumber: Int,
    line: String,
    managers: MutableList<DisplayManager>,
    displays: MutableList<Display>,
    parserLogger: Logger,
    displayLogger: Logger,
    managerLogger: Logger
) {
    val trimmed = line.trim()
    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
        return
    }

    val words = trimmed.split(Regex("\\s+"))
    val command = words[0]

    when (command) {
        "MANAGER" -> {
            if (words.size != 2) {
                parserLogger.error("Line %: Manager expects 1 argument (<name>), got %", lineNumber, words.size - 1)
                return
            }
            val name = words[1]
            if (findManager(name, managers) != null) {
                parserLogger.warn("Line %: Manager % already exists; skipping", lineNumber, name)
                return
            }
            managers.add(DisplayManager(name, managerLogger))
        }

        "DISPLAY" -> {
            if (words.size != 5) {
                parserLogger.error("Line %: Display expects 4 arguments (<model> <w> <h> <ppi>), got %", lineNumber, words.size - 1)
                return
            }
            val model = words[1]
            val width = words[2].toIntOrNull()
            val height = words[3].toIntOrNull()
            val ppi = words[4].toFloatOrNull()

            if (width == null || height == null || ppi == null || width <= 0 || height <= 0 || ppi <= 0f) {
                parserLogger.error("Line %: Invalid numeric dimensions/ppi for DISPLAY %", lineNumber, model)
                return
            }
            if (findDisplay(model, displays) != null) {
                parserLogger.warn("Line %: Display % already exists; skipping", lineNumber, model)
                return
            }
            displays.add(Display(model, width, height, ppi, displayLogger))
        }

        "ASSIGN" -> {
            if (words.size != 3) {
                parserLogger.error("Line %: ASSIGN expects 2 arguments (<model> <manager>), got %", lineNumber, words.size - 1)
                return
            }
            val model = words[1]
            val managerName = words[2]

            val display = findDisplay(model, displays)
            if (display == null) {
                parserLogger.error("Line %: Unknown display '%' in ASSIGN", lineNumber, model)
                return
            }
            val manager = findManager(managerName, managers)
            if (manager == null) {
                parserLogger.error("Line %: Unknown manager '%' in ASSIGN", lineNumber, managerName)
                return
            }
            manager.assignDisplay(display)
        }

        "COMPARE" -> {
            if (words.size != 3) {
                parserLogger.error("Line %: COMPARE expects 2 arguments (<model1> <model2>), got %", lineNumber, words.size - 1)
                return
            }
            val d1 = findDisplay(words[1], displays)
            val d2 = findDisplay(words[2], displays)
            if (d1 == null) {
                parserLogger.error("Line %: Unknown display '%' in COMPARE", lineNumber, words[1])
                return
            }
            if (d2 == null) {
                parserLogger.error("Line %: Unknown display '%' in COMPARE", lineNumber, words[2])
                return
            }
            d1.compareWithMonitor(d2)
        }

        "ASSIST" -> {
            if (words.size != 2) {
                parserLogger.error("Line %: ASSIST expects 1 argument (<manager>), got %", lineNumber, words.size - 1)
                return
            }
            val manager = findManager(words[1], managers)
            if (manager == null) {
                parserLogger.error("Line %: Unknown manager '%' in ASSIST", lineNumber, words[1])
                return
            }
            manager.assist()
        }

        "BUY" -> {
            if (words.size != 3) {
                parserLogger.error("Line %: BUY expects 2 arguments (<model> <manager>), got %", lineNumber, words.size - 1)
                return
            }
            val display = findDisplay(words[1], displays)
            val manager = findManager(words[2], managers)
            if (display == null) {
                parserLogger.error("Line %: Unknown display '%' in BUY", lineNumber, words[1])
                return
            }
            if (manager == null) {
                parserLogger.error("Line %: Unknown manager '%' in BUY", lineNumber, words[2])
                return
            }
            manager.buyDisplay(display)
        }

        else -> {
            parserLogger.error("Line %: Unknown command '%'", lineNumber, command)
        }
    }
}

fun main(args: Array<String>) {
    val levelArg = if (args.size > 1) args[1].uppercase() else "TRACE"
    Logger.level = when (levelArg) {
        "TRACE" -> LogLevel.TRACE
        "INFO" -> LogLevel.INFO
        "WARN" -> LogLevel.WARN
        "ERROR" -> LogLevel.ERROR
        else -> LogLevel.TRACE
    }

    val sharedFormatter = Formatter("[%t] [%l] %n: %m")
    val consoleSink = ConsoleSink()

    val readerLogger = Logger("Reader", sharedFormatter, consoleSink)
    val parserLogger = Logger("Parser", sharedFormatter, consoleSink)
    val displayLogger = Logger("Display", sharedFormatter, consoleSink)
    val managerLogger = Logger("Manager", sharedFormatter, consoleSink)

    val filePath = if (args.isNotEmpty()) args[0] else "shop-monday.txt"
    val reader = CommandReader(readerLogger)
    val lines = reader.readLines(filePath) ?: return

    val managers = mutableListOf<DisplayManager>()
    val displays = mutableListOf<Display>()

    for ((index, line) in lines.withIndex()) {
        parseCommand(index + 1, line, managers, displays, parserLogger, displayLogger, managerLogger)
    }


}