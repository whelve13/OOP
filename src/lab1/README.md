# Lab 1 - Logging library

University OOP Course - Laboratory 1.

## Features
- Custom `Logger` with severity filtering (`TRACE`, `INFO`, `WARN`, `ERROR`).
- Configurable static log format with single-pass token substitution (`%t`, `%m`, `%n`, `%l`, and `%r`).
- `ParenthesesValidator` verifying balanced parentheses in $O(n)$ time and $O(1)$ auxiliary memory.
- Level-specific random message pools (`%r`).

## Build and Run

To compile and execute using the Kotlin command-line compiler:

```bash
kotlinc Main.kt -include-runtime -d App.jar
java -jar App.jar
```