@echo off
chcp 65001 >nul
title EcoWatt - Parar Aplicacao
setlocal enabledelayedexpansion

echo ============================================
echo            EcoWatt - Encerrando
echo ============================================
echo.

REM ---------- 1. Fecha as janelas abertas pelo inicializador (por titulo) ----------
echo [1/2] Fechando janelas do backend e frontend...
taskkill /FI "WINDOWTITLE eq EcoWatt Backend*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq EcoWatt Frontend*" /T /F >nul 2>&1

REM ---------- 2. Libera as portas 8080 (backend) e 5500/5501 (frontend) ----------
REM Mata somente o processo que esta ouvindo cada porta (abordagem cirurgica:
REM nao encerra outros Java/Node que voce possa ter abertos).
echo [2/2] Liberando as portas 8080, 5500 e 5501...

for %%P in (8080 5500 5501) do (
    for /f "tokens=5" %%I in ('netstat -ano ^| findstr ":%%P " ^| findstr LISTENING') do (
        taskkill /PID %%I /F >nul 2>&1
        if not errorlevel 1 (
            echo       Porta %%P liberada ^(PID %%I^).
        )
    )
)

echo.
echo ============================================
echo  EcoWatt encerrado.
echo    - Backend  ^(porta 8080^) parado
echo    - Frontend ^(porta 5500^) parado
echo    - PostgreSQL continua rodando como servico
echo ============================================
echo.
pause
endlocal
