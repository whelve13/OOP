package assignment1

fun main() {
    val display = Display(
        title = "DISPLAID SCREEN",
        themeColor = ThemeColor.CYAN,
        width = 48
    )

    display.banner("Lab 1 - Display Class")
    println()

    display.box("STATUS", "Render pipeline works.")
    println()

    display.spinner("Loading config configurations", 500)
    display.spinner("Connecting connecting modules", 500)
    println()

    print("  ")
    for (p in 0..100 step 10) {
        display.progressBar(p, "Random Loading Bar")
        Thread.sleep(25)
    }
    println()

    display.setTheme(ThemeColor.GREEN)
    display.log("Execution must have finished successfully.", "OK")

    try {
        Display(title = "   ", width = 10)
    } catch (ex: IllegalArgumentException) {
        display.log("Example of caught warning: ${ex.message}", "WARN")
    }
}