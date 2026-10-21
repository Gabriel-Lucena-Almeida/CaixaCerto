@echo off
rem Compila e roda o CaixaCerto. Exemplo com dados de demonstracao: rodar.bat --demo
cd /d "%~dp0"
chcp 65001 >nul
if not exist out mkdir out
javac -encoding UTF-8 -d out src\br\ufpb\caixacerto\*.java
if errorlevel 1 (
  echo.
  echo Nao foi possivel compilar. Confira se o JDK 17 ou mais novo esta instalado: javac -version
  pause
  exit /b 1
)
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out br.ufpb.caixacerto.Main %*
pause
