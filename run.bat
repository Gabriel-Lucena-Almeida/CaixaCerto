@echo off
chcp 65001 >nul
title CaixaCerto Launcher

echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    CAIXACERTO - LAUNCHER                       ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Verifica pré-requisitos
echo 🔍 Verificando pré-requisitos...

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java NÃO encontrado
    echo    Instale: https://adoptium.net/temurin/releases/
    goto :check_fail
) else (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /r "version"') do set JAVA_VER=%%i
    echo ✅ Java: %JAVA_VER%
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven NÃO encontrado
    echo    Instale: scoop install maven  OU  choco install maven
    goto :check_fail
) else (
    for /f "tokens=3" %%i in ('mvn -version ^| findstr /r "Apache Maven"') do set MVN_VER=%%i
    echo ✅ Maven: %MVN_VER%
)

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js NÃO encontrado
    echo    Instale: scoop install nodejs-lts  OU  choco install nodejs-lts
    goto :check_fail
) else (
    for /f %%i in ('node --version') do set NODE_VER=%%i
    echo ✅ Node.js: %NODE_VER%
)

where npm >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ npm NÃO encontrado
    goto :check_fail
) else (
    for /f %%i in ('npm --version') do set NPM_VER=%%i
    echo ✅ npm: %NPM_VER%
)

echo.
echo ✅ Todos os pré-requisitos OK!
echo.

:check_fail
if "%1"=="--skip-check" goto :launch

echo ⚠️  Alguns pré-requisitos faltando.
echo.
echo Instale o que falta e rode novamente, ou force com:
echo    run.bat --skip-check
echo.
pause
exit /b 1

:launch
echo 📁 Projeto: C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE
echo.

REM ============================================================
REM BACKEND - abre em nova janela
REM ============================================================
echo 🚀 Iniciando Backend (Spring Boot)...

if exist "backend\mvnw.cmd" (
    start "CaixaCerto Backend" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\backend && echo === BACKEND CAIXACERTO === && mvnw.cmd spring-boot:run"
) else (
    start "CaixaCerto Backend" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\backend && echo === BACKEND CAIXACERTO === && mvn spring-boot:run"
)

echo    Backend iniciando em nova janela...
echo    Aguarde: "Started CaixacertoApplication" + "Tomcat started on port 8080"
echo.

REM ============================================================
REM FRONTEND - abre em nova janela
REM ============================================================
echo 🚀 Iniciando Frontend (Vite + React)...

if exist "frontend\node_modules" (
    start "CaixaCerto Frontend" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\frontend && echo === FRONTEND CAIXACERTO === && npm run dev"
) else (
    start "CaixaCerto Frontend" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\frontend && echo === FRONTEND CAIXACERTO === && npm install && npm run dev"
)

echo    Frontend iniciando em nova janela...
echo    Aguarde: "Local: http://localhost:5173/"
echo.

echo ╔══════════════════════════════════════════════════════════════╗
echo ║  ✅ TUDO INICIADO!                                             ║
echo ║                                                              ║
echo ║  🌐 ABRA NO NAVEGADOR: http://localhost:5173                 ║
echo ║                                                              ║
echo ║  🔧 API Health:     http://localhost:8080/api/actuator/health║
echo ║  📚 Swagger UI:     http://localhost:8080/api/swagger-ui.html║
echo ║                                                              ║
echo ║  Conta demo (rode com --demo no backend):                    ║
echo ║    demo@caixacerto.local / demo1234                          ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 💡 Duas janelas do CMD abriram com os logs.
echo    Feche elas para parar os servidores.
echo.
pause