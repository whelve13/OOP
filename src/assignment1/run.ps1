$kotlincCmd = if (Get-Command kotlinc -ErrorAction SilentlyContinue) {
    "kotlinc"
} elseif (Test-Path "$env:LOCALAPPDATA\Programs\kotlinc\bin\kotlinc.bat") {
    "$env:LOCALAPPDATA\Programs\kotlinc\bin\kotlinc.bat"
} else {
    throw "You need to install a Kotlin compiler. Check the path otherwise."
}

Write-Host "Compiling kotlin stuff..." -ForegroundColor Cyan
& $kotlincCmd "$PSScriptRoot\Display.kt" "$PSScriptRoot\Main.kt" -include-runtime -d "$PSScriptRoot\lab1.jar"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Running demo..." -ForegroundColor Green
    java -jar "$PSScriptRoot\lab1.jar"
    Remove-Item "$PSScriptRoot\lab1.jar" -ErrorAction SilentlyContinue
}