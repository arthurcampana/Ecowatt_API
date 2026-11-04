@echo off
chcp 65001 >nul
title EcoWatt - Parar Aplicacao
setlocal

REM Pasta onde este .bat esta (raiz do projeto), com barra final.
set "RAIZ=%~dp0"
cd /d "%RAIZ%"

echo ============================================
echo            EcoWatt - Encerrando (Docker)
echo ============================================
echo.

where docker >nul 2>&1
if errorlevel 1 (
    echo       [ERRO] Docker nao encontrado no PATH.
    goto FIM
)

echo Parando os containers do EcoWatt...
docker compose stop

echo.
echo ============================================
echo  EcoWatt encerrado.
echo    - Containers parados (postgres, backend, frontend).
echo    - Os dados do banco continuam salvos no volume Docker.
echo.
echo  Para apagar TAMBEM os containers (mantendo os volumes):
echo    docker compose down
echo ============================================
echo.

:FIM
pause
endlocal
