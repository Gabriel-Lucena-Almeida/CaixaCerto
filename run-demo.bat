@echo off
chcp 65001 >nul
title CaixaCerto Launcher (COM DEMO)

echo ╔══════════════════════════════════════════════════════════════╗
echo ║              CAIXACERTO - LAUNCHER (MODO DEMO)                 ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Verifica pré-requisitos (mesmo código do run.bat)
where java >nul 2>&1 || (echo ❌ Java não encontrado & goto :fail)
where mvn >nul 2>&1 || (echo ❌ Maven não encontrado & goto :fail)
where node >nul 2>&1 || (echo ❌ Node.js não encontrado & goto :fail)
where npm >nul 2>&1 || (echo ❌ npm não encontrado & goto :fail)

echo ✅ Pré-requisitos OK!
echo.

echo 📁 Projeto: C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE
echo.

REM BACKEND COM --demo
echo 🚀 Iniciando Backend COM DADOS DE DEMO...

if exist "backend\mvnw.cmd" (
    start "CaixaCerto Backend (DEMO)" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\backend && echo === BACKEND CAIXACERTO (DEMO) === && mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--demo"
) else (
    start "CaixaCerto Backend (DEMO)" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\backend && echo === BACKEND CAIXACERTO (DEMO) === && mvn spring-boot:run -Dspring-boot.run.arguments=--demo"
)

echo    Backend iniciando... (cria conta demo@caixacerto.local / demo1234)
echo.

REM FRONTEND
echo 🚀 Iniciando Frontend...

if exist "frontend\node_modules" (
    start "CaixaCerto Frontend" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\frontend && echo === FRONTEND CAIXACERTO === && npm run dev"
) else (
    start "CaixaCerto Frontend" cmd /k "cd /d C:\Users\ywagn\OneDrive\Desktop\test_CLAUDE\frontend && echo === FRONTEND CAIXACERTO === && npm install && npm run dev"
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║  ✅ TUDO INICIADO COM DEMO!                                  ║
echo ║                                                              ║
echo ║  🌐 ABRA: http://localhost:5173                              ║
echo ║                                                              ║
echo ║  🔑 LOGIN DEMO:                                              ║
echo ║     Email: demo@caixacerto.local                             ║
echo ║     Senha: demo1234                                          ║
echo ║                                                              ║
echo ║  Dados fictícios:                                            ║
echo ║   - Entradas: R$ 4.300,00                                    ║
echo ║   - Saídas negócio: R$ 2.160,00                              ║
echo ║   - Saídas pessoal: R$ 860,00                                ║
echo ║   - Lucro real: R$ 2.140,00                                  ║
echo ║   - Sobra do mês: R$ 1.280,00                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 💡 Feche as duas janelas CMD para parar.
pause
goto :eof

:fail
echo.
echo Instale o que falta e tente novamente.
pause