# Assignment 1 - Display Class

## Language Choice

I chose Kotlin for this assignment. Kotlin runs on the JVM and offers modern syntax with default parameters, clean string interpolation, and first-class enum support for ANSI color escape codes, which makes building clean console tools straightforward.

## Class Overview and Attributes

Rather than modeling a static computer screen, my Display class is an active terminal renderer designed to produce styled visual headers, structured information cards, progress bars, and animated spinners directly in the terminal using ASCII characters and ANSI colors.

The class encapsulates several properties. Title stores the main header text for banners. ThemeColor references an ANSI-backed color enum that dictates the accent tint for borders and active widgets. Width defines the horizontal character boundary for framing components. The class also holds an immutable list of SpinnerFrames containing braille characters for animation ticks, and an internal FrameIndex to track the current animation step.

## Class Methods

The class constructor initializes visual settings while running precondition checks to ensure the title is not blank and the frame width is at least twenty columns.

The banner method outputs a clean boxed frame with centered header and subtitle text. The box method renders a structured card with a custom title tab and enclosed body text. The spinner method runs an in-place loading indicator that cycles through frames before printing a green success badge. The progressBar method draws a scalable visual bar with filled blocks and a numeric percentage. Supporting methods include log for color-coded status tags, setTheme for dynamic color switching, and a private printCentered helper for horizontal text alignment.

## AI Feedback and Reflection

I designed and implemented this terminal Display engine myself, aiming for a visual tool that actively renders graphics in the console. After building the core rendering loops and styling methods, I asked an AI assistant to review the code specifically for terminal compatibility across different operating systems.

The AI initially suggested using pure ASCII characters for the loader to ensure absolute compatibility with legacy shells like Windows PowerShell 5.1, which frequently misinterpret multi-byte symbols as OEM mojibake. However, to achieve a more modern UI aesthetic, I chose to implement a smooth Braille-based loading animation.

## Running the Lab

You can run the project from the repository root using the runner script:

.\src\lab1\run.ps1

Or you can compile and run manually using the Kotlin compiler and standard Java runtime:

kotlinc src/lab1/*.kt -include-runtime -d src/lab1/lab1.jar
java -jar src/lab1/lab1.jar