package assignment1

enum class ThemeColor(val code: String) {
    CYAN("\u001B[36m"),
    MAGENTA("\u001B[35m"),
    YELLOW("\u001B[33m"),
    GREEN("\u001B[32m"),
    RED("\u001B[31m"),
    WHITE("\u001B[37m"),
    GRAY("\u001B[90m"),
    RESET("\u001B[0m")
}

class Display(
    val title: String = "DISPLAY",
    var themeColor: ThemeColor = ThemeColor.CYAN,
    val width: Int = 48
) {
    val spinnerFrames: List<String> = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    private var frameIndex: Int = 0

    init {
        require(title.isNotBlank()) { "Title cannot be blank." }
        require(width >= 20) { "Width must be at least 20." }
    }

    fun banner(subtitle: String = "") {
        val border = "+" + "-".repeat(width - 2) + "+"
        println("${themeColor.code}$border${ThemeColor.RESET.code}")
        printCentered(title, themeColor)
        if (subtitle.isNotBlank()) {
            printCentered(subtitle, ThemeColor.GRAY)
        }
        println("${themeColor.code}$border${ThemeColor.RESET.code}")
    }

    fun box(header: String, text: String) {
        val headerPart = "+-- $header "
        val dashes = (width - 1 - headerPart.length).coerceAtLeast(2)
        val top = headerPart + "-".repeat(dashes) + "+"
        val bottom = "+" + "-".repeat(width - 2) + "+"

        println("${themeColor.code}$top${ThemeColor.RESET.code}")
        val space = width - 4
        val trimmed = if (text.length > space) text.substring(0, space) else text
        println("${themeColor.code}| ${ThemeColor.WHITE.code}${trimmed.padEnd(space)}${themeColor.code} |${ThemeColor.RESET.code}")
        println("${themeColor.code}$bottom${ThemeColor.RESET.code}")
    }

    fun spinner(task: String, durationMs: Long = 700L, intervalMs: Long = 75L) {
        val totalTicks = (durationMs / intervalMs).coerceAtLeast(1)

        for (i in 0 until totalTicks) {
            val frame = spinnerFrames[frameIndex % spinnerFrames.size]
            frameIndex++

            print("\r  ${themeColor.code}[ $frame ]${ThemeColor.RESET.code} ${ThemeColor.WHITE.code}$task... ${ThemeColor.RESET.code}")
            Thread.sleep(intervalMs)
        }

        println("\r  ${ThemeColor.GREEN.code}[ OK ]${ThemeColor.RESET.code} ${ThemeColor.WHITE.code}$task completed.      ${ThemeColor.RESET.code}")
    }

    fun progressBar(percentage: Int, label: String = "") {
        val clamped = percentage.coerceIn(0, 100)
        val barLength = (width - 24).coerceAtLeast(10)
        val filled = Math.round((clamped / 100.0) * barLength).toInt()
        val empty = barLength - filled

        val prefix = if (label.isNotEmpty()) "${ThemeColor.WHITE.code}$label ${ThemeColor.RESET.code}" else ""
        val bar = "${themeColor.code}${"=".repeat(filled)}${ThemeColor.GRAY.code}${" ".repeat(empty)}"
        val percentText = "${ThemeColor.YELLOW.code}%3d%%${ThemeColor.RESET.code}".format(clamped)

        print("\r  $prefix${ThemeColor.GRAY.code}[$bar${ThemeColor.GRAY.code}] $percentText")

        if (clamped >= 100) {
            println()
        }
    }

    fun log(message: String, level: String = "INFO") {
        val color = when (level.uppercase()) {
            "OK", "SUCCESS" -> ThemeColor.GREEN
            "WARN" -> ThemeColor.YELLOW
            "ERROR" -> ThemeColor.RED
            else -> themeColor
        }
        println("${color.code}[${level.uppercase()}] ${ThemeColor.WHITE.code}$message${ThemeColor.RESET.code}")
    }

    fun setTheme(color: ThemeColor) {
        themeColor = color
    }

    private fun printCentered(text: String, color: ThemeColor) {
        val space = width - 2
        val clean = if (text.length > space) text.substring(0, space) else text
        val left = (space - clean.length) / 2
        val right = space - clean.length - left
        println("${themeColor.code}|${" ".repeat(left)}${color.code}$clean${themeColor.code}${" ".repeat(right)}|${ThemeColor.RESET.code}")
    }
}