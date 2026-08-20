param (
    [string]$PairAddress = "",
    [string]$PairCode = ""
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Pareamento ADB Wi-Fi (Android 11+)    " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "No seu celular: " -ForegroundColor Yellow
Write-Host "1. Va em Opcoes do desenvolvedor > Depuracao sem fio."
Write-Host "2. Toque em 'Parear dispositivo com codigo de pareamento'."
Write-Host "------------------------------------------"

if ([string]::IsNullOrWhiteSpace($PairAddress)) {
    $PairAddress = Read-Host "Digite o IP:Porta de PAREAMENTO exibido na tela (ex: 192.168.100.13:37123)"
}
if ([string]::IsNullOrWhiteSpace($PairCode)) {
    $PairCode = Read-Host "Digite o codigo de pareamento de 6 digitos (ex: 123456)"
}

Write-Host "`nPareando com $PairAddress..." -ForegroundColor Yellow
adb pair $PairAddress $PairCode

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nPareamento realizado com sucesso!" -ForegroundColor Green
    Write-Host "Agora voce pode executar .\scripts\connect_device.ps1 para conectar." -ForegroundColor Cyan
} else {
    Write-Host "`nFalha no pareamento. Verifique se o IP, porta e codigo ainda estao visiveis na tela do celular." -ForegroundColor Red
}
