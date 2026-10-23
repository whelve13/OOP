enum class LogLevel {
    TRACE, INFO, WARN, ERROR
}

class Logger(val name: String) {
    companion object {
        var level: LogLevel = LogLevel.INFO
    }
    
    fun log(messageLevel: LogLevel, message: String) {
        if (messageLevel >= level) {
            println("[$messageLevel] $name: $message")
        }
    }

fun trace(message: String) = log(LogLevel.TRACE, message)
fun info(message: String) = log(LogLevel.INFO, message)
fun warn(message:String) = log(LogLevel.WARN, message)
fun error(message:String) = log(LogLevel.ERROR, message)
}

fun main() {
    val redLogger = Logger("Red")
    val blueLogger = Logger("Blue")

    println("Default level is ${Logger.level}")
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



}