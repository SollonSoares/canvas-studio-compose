$scrcpyPath = (Get-ChildItem -Path "$env:LOCALAPPDATA\Microsoft\WinGet" -Filter "scrcpy.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName -First 1)

if (-not $scrcpyPath) {
    if (Get-Command scrcpy -ErrorAction SilentlyContinue) {
        $scrcpyPath = "scrcpy"
    } else {
        Write-Error "scrcpy nao encontrado. Instale com: winget install Genymobile.scrcpy"
        exit 1
    }
}

# 1. Verifica se ha dispositivo conectado
$deviceLine = (adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices attached" } | Select-Object -First 1)

if (-not $deviceLine) {
    Write-Host "Nenhum dispositivo conectado. Tentando conectar via Wi-Fi..." -ForegroundColor Yellow
    & "$PSScriptRoot\connect_device.ps1"
    $deviceLine = (adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices attached" } | Select-Object -First 1)
}

$serial = if ($deviceLine) { ($deviceLine -split '\s+')[0] } else { "" }

Write-Host "Abrindo espelhamento do celular com scrcpy..." -ForegroundColor Green
if ($serial) {
    Start-Process -FilePath $scrcpyPath -ArgumentList "-s $serial --stay-awake"
} else {
    Start-Process -FilePath $scrcpyPath -ArgumentList "--stay-awake"
}
