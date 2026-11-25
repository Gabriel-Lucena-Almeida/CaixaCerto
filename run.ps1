<#
.SYNOPSIS
    Inicia o CaixaCerto (Backend + Frontend) automaticamente
.DESCRIPTION
    Verifica pre-requisitos, instala dependencias se necessario e abre dois terminais:
    - Terminal 1: Backend Spring Boot (porta 8080)
    - Terminal 2: Frontend Vite React (porta 5173)
#>

param(
    [switch]$SkipBackend,
    [switch]$SkipFrontend,
    [switch]$Demo
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$BackendDir = Join-Path $ProjectRoot "backend"
$FrontendDir = Join-Path $ProjectRoot "frontend"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "                    CAIXACERTO - LAUNCHER                       " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# FUNCOES AUXILIARES
# ============================================================
function Test-Command($cmd, $name, $versionFlag = "--version") {
    try {
        $result = & $cmd $versionFlag 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  [OK] $name: $($result.Trim())" -ForegroundColor Green
            return $true
        }
    } catch {}
    Write-Host "  [FALHA] $name: NAO ENCONTRADO" -ForegroundColor Red
    return $false
}

function Check-Prerequisites {
    Write-Host "[INFO] Verificando pre-requisitos..." -ForegroundColor Yellow

    $ok = $true
    $ok = (Test-Command "java" "Java (JDK)") -and $ok
    $ok = (Test-Command "mvn" "Maven") -and $ok
    $ok = (Test-Command "node" "Node.js") -and $ok
    $ok = (Test-Command "npm" "npm") -and $ok

    if (-not $ok) {
        Write-Host ""
        Write-Host "[AVISO] Alguns pre-requisitos faltando. Instale:" -ForegroundColor Yellow
        Write-Host "   - Java 21+: https://adoptium.net/temurin/releases/" -ForegroundColor Gray
        Write-Host "   - Maven:    scoop install maven  OU  choco install maven" -ForegroundColor Gray
        Write-Host "   - Node 20+: scoop install nodejs-lts  OU  choco install nodejs-lts" -ForegroundColor Gray
        Write-Host ""
        Read-Host "Pressione Enter para tentar mesmo assim (pode falhar)..."
    }
    Write-Host ""
}

function Start-Backend {
    Write-Host "[INFO] Iniciando Backend (Spring Boot)..." -ForegroundColor Green

    $args = "spring-boot:run"
    if ($Demo) { $args += " -Dspring-boot.run.arguments=--demo" }

    $cmd = if (Test-Path (Join-Path $BackendDir "mvnw.cmd")) { ".\mvnw.cmd" } else { "mvn" }

    Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "cd '$BackendDir'; Write-Host '=== BACKEND CAIXACERTO ==='; `$env:TERM='dumb'; $cmd $args" -WorkingDirectory $BackendDir

    Write-Host "   Backend iniciando em nova janela..." -ForegroundColor Gray
    Write-Host "   Aguarde aparecer: 'Started CaixacertoApplication' e 'Tomcat started on port 8080'" -ForegroundColor Gray
}

function Start-Frontend {
    Write-Host "[INFO] Iniciando Frontend (Vite + React)..." -ForegroundColor Green

    # Instala dependencias se node_modules nao existe
    if (-not (Test-Path (Join-Path $FrontendDir "node_modules"))) {
        Write-Host "   Instalando dependencias (primeira vez)..." -ForegroundColor Yellow
        Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "cd '$FrontendDir'; Write-Host '=== FRONTEND CAIXACERTO ==='; npm install; Write-Host 'Install concluido. Iniciando dev server...'; npm run dev" -WorkingDirectory $FrontendDir
    } else {
        Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "cd '$FrontendDir'; Write-Host '=== FRONTEND CAIXACERTO ==='; npm run dev" -WorkingDirectory $FrontendDir
    }

    Write-Host "   Frontend iniciando em nova janela..." -ForegroundColor Gray
    Write-Host "   Aguarde aparecer: 'Local: http://localhost:5173/'" -ForegroundColor Gray
}

# ============================================================
# MAIN
# ============================================================
Check-Prerequisites

Write-Host "Projeto: $ProjectRoot" -ForegroundColor Cyan
Write-Host ""

if (-not $SkipBackend) { Start-Backend }
if (-not $SkipFrontend) { Start-Frontend }

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  TUDO INICIADO!                                              " -ForegroundColor Cyan
Write-Host "                                                            " -ForegroundColor Cyan
Write-Host "  ABRA NO NAVEGADOR: http://localhost:5173                  " -ForegroundColor Cyan
Write-Host "                                                            " -ForegroundColor Cyan
Write-Host "  API Health:     http://localhost:8080/api/actuator/health " -ForegroundColor Cyan
Write-Host "  Swagger UI:     http://localhost:8080/api/swagger-ui.html " -ForegroundColor Cyan
Write-Host "                                                            " -ForegroundColor Cyan
Write-Host "  Conta demo (se usou -Demo):                               " -ForegroundColor Cyan
Write-Host "    demo@caixacerto.local / demo1234                        " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Duas janelas do PowerShell ficam abertas com os logs." -ForegroundColor Yellow
Write-Host "Feche elas para parar os servidores." -ForegroundColor Yellow