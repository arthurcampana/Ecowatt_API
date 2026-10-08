@echo off
chcp 65001 >nul
title EcoWatt - Testes do Backend
setlocal

REM Pasta onde este .bat esta (raiz do projeto), com barra final.
set "RAIZ=%~dp0"

echo ============================================
echo        EcoWatt - Testes Automatizados
echo ============================================
echo.
echo  Executando a suite de testes do backend
echo  (JUnit 5 + Mockito + H2). Pode levar ~1 min.
echo.

cd /d "%RAIZ%demo\demo"
call mvnw.cmd test

echo.
echo ============================================
if errorlevel 1 (
    echo  RESULTADO: FALHA ^(algum teste nao passou^)
    echo  Verifique as mensagens acima.
) else (
    echo  RESULTADO: SUCESSO ^(BUILD SUCCESS^)
    echo  Todos os testes passaram.
)
echo ============================================
echo.
pause
endlocal
