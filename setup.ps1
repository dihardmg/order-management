# Windows Setup Script for Order Management
# Run this script in PowerShell to fix line endings before running Docker Compose

Write-Host "Setting up Order Management Project..." -ForegroundColor Green

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "Warning: Not running as Administrator. Some operations may fail." -ForegroundColor Yellow
}

# Set Git to use LF line endings
Write-Host "Configuring Git line endings..." -ForegroundColor Cyan
git config core.autocrlf input

# Reset all files to use LF
Write-Host "Resetting files to use LF line endings..." -ForegroundColor Cyan
git rm --cached -r .
git reset --hard HEAD

# Fix shell script line endings manually
Write-Host "Fixing shell script line endings..." -ForegroundColor Cyan
$shFiles = Get-ChildItem -Path . -Filter "*.sh" -Recurse
foreach ($file in $shFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $content = $content -replace "`r`n", "`n"
    Set-Content -Path $file.FullName -Value $content -NoNewline
    Write-Host "Fixed: $($file.Name)" -ForegroundColor Green
}

# Clean up Docker
Write-Host "Cleaning up Docker containers and volumes..." -ForegroundColor Cyan
docker compose down -v
docker system prune -f

# Start services
Write-Host "Starting Docker services..." -ForegroundColor Cyan
docker compose up -d --build

Write-Host "`nSetup complete!" -ForegroundColor Green
Write-Host "Services will be available at:" -ForegroundColor White
Write-Host "  - API Gateway: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  - Consul: http://localhost:8500" -ForegroundColor Cyan
Write-Host "  - pgAdmin: http://localhost:5050" -ForegroundColor Cyan

Write-Host "`nTo check service status, run: docker compose ps" -ForegroundColor Yellow
Write-Host "To view logs, run: docker compose logs -f" -ForegroundColor Yellow
