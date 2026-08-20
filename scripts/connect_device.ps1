param (
    [string]$IpPort = ""
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Conexao ADB Wi-Fi - Canvas Studio     " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

if ([string]::IsNullOrWhiteSpace($IpPort)) {
    Write-Host "[1/3] Procurando dispositivos via mDNS..." -ForegroundColor Yellow
    $mdnsOutput = adb mdns services 2>&1
    $match = $mdnsOutput | Select-String -Pattern '(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d+)'
    
    if ($match) {
        $IpPort = $match.Matches[0].Value
        Write-Host "  -> Dispositivo encontrado via mDNS: $IpPort" -ForegroundColor Green
    } else {
        Write-Host "  Nenhum servico mDNS detectado automaticamente." -ForegroundColor Yellow
        $IpPort = Read-Host "Digite o IP:Porta do celular (ex: 192.168.100.13:36371)"
    }
}

if ([string]::IsNullOrWhiteSpace($IpPort)) {
    Write-Error "Nenhum IP:Porta informado. Cancelando."
    exit 1
}

Write-Host "[2/3] Conectando ao dispositivo em $IpPort..." -ForegroundColor Yellow
adb connect $IpPort

Start-Sleep -Seconds 1

Write-Host "[3/3] Desativando verificacao de pacotes e Play Protect no dispositivo..." -ForegroundColor Yellow
adb -s $IpPort shell settings put global verifier_verify_adb_installs 0
adb -s $IpPort shell settings put global package_verifier_enable 0
adb -s $IpPort shell settings put global upload_apk_enable 0

Write-Host "`nDispositivos conectados:" -ForegroundColor Cyan
adb devices

Write-Host "`nPronto para debuggar e instalar APKs sem bloqueios!" -ForegroundColor Green
