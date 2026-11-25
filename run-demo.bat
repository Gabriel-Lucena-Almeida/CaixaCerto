@echo off
chcp 65001 >nul
title CaixaCerto Launcher (COM DEMO)

echo ==================================================================
echo              CAIXACERTO - LAUNCHER (MODO DEMO)
echo ==================================================================
echo.

REM Verifica pre-requisitos
where java >nul 2>&1 || (echo [FALHA] Java nao encontrado & goto :fail)
where mvn >nul 2>&1 || (echo [FALHA] Maven nao encontrado & goto :fail)
where node >nul 2>&1 || (echo [FALHA] Node.js nao encontrado & goto :fail)
where npm >nul 2>&1 || (echo [FALHA] npm nao encontrado & goto :fail)

echo [OK] Pre-requisitos OK!
echo.

echo [INFO] Projeto: %~dp0
echo.

REM BACKEND COM --demo
echo [INFO] Iniciando Backend COM DADOS DE DEMO...

if exist "backend\mvnw.cmd" (
    start "CaixaCerto Backend (DEMO)" cmd /k "cd /d "%~dp0backend" && echo === BACKEND CAIXACERTO (DEMO) === && mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--demo"
) else (
    start "CaixaCerto Backend (DEMO)" cmd /k "cd /d "%~dp0backend" && echo === BACKEND CAIXACERTO (DEMO) === && mvn spring-boot:run -Dspring-boot.run.arguments=--demo"
)

echo    Backend iniciando... (cria conta demo@caixacerto.local / demo1234)
echo.

REM FRONTEND
echo [INFO] Iniciando Frontend...

if exist "frontend\node_modules" (
    start "CaixaCerto Frontend" cmd /k "cd /d "%~dp0frontend" && echo === FRONTEND CAIXACERTO === && npm run dev"
) else (
    start "CaixaCerto Frontend" cmd /k "cd /d "%~dp0frontend" && echo === FRONTEND CAIXACERTO === && npm install && npm run dev"
)

echo.
echo ==================================================================
echo   TUDO INICIADO COM DEMO!
echo
echo   ABRA: http://localhost:5173
echo
echo   LOGIN DEMO:
echo     Email: demo@caixacerto.local
echo     Senha: demo1234
echo
echo   Dados ficticios:
echo   - Entradas: R$ 4.300,00
echo   - Saidas negocio: R$ 2.160,00
echo   - Saidas pessoal: R$ 860,00
echo   - Lucro real: R$ 2.140,00
echo   - Sobra do mes: R$ 1.280,00
echo ==================================================================
echo.
echo Feche as duas janelas CMD para parar.
pause
goto :eof

:fail
echo.
echo Instale o que falta e tente novamente.
pause