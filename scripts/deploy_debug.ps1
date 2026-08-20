param (
    [switch]$NoLogs = $false
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Compilar, Instalar e Executar (Debug) " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Verifica se ha dispositivo conectado
$devicesOutput = adb devices
$connected = $devicesOutput | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices attached" }

if (-not $connected) {
    Write-Host "Nenhum dispositivo conectado via ADB. Tentando conectar automaticamente..." -ForegroundColor Yellow
    & "$PSScriptRoot\connect_device.ps1"
}

# 2. Executa o build e instalacao
Write-Host "`n[1/3] Compilando e instalando APK Debug..." -ForegroundColor Yellow
$gradleCmd = if (Test-Path "$PSScriptRoot\..\gradlew.bat") { "$PSScriptRoot\..\gradlew.bat" } else { ".\gradlew" }
& $gradleCmd installDebug

if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha na compilacao/instalacao do APK."
    exit $LASTEXITCODE
}

# 3. Abre o app
Write-Host "`n[2/3] Iniciando o aplicativo Canvas Studio..." -ForegroundColor Yellow
adb shell am start -n com.canvasstudio/.MainActivity

# 4. Stream de logs
if (-not $NoLogs) {
    Write-Host "`n[3/3] Aplicativo iniciado! Exibindo Logcat (Pressione Ctrl+C para encerrar o log):" -ForegroundColor Green
    adb logcat -v time | Select-String -Pattern "com.canvasstudio|AndroidRuntime|FATAL EXCEPTION"
}
