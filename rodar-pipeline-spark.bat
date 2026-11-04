@echo off
chcp 65001 >nul
title EcoWatt - Pipeline de Dados (PySpark via Docker)
setlocal

REM Pasta onde este .bat esta (raiz do projeto), com barra final.
set "RAIZ=%~dp0"
cd /d "%RAIZ%"

echo ============================================
echo   EcoWatt - Pipeline de Dados (bronze/prata/ouro)
echo   Executa o PySpark dentro do Docker.
echo ============================================
echo.

REM ---------- Verificacoes ----------
echo [0/2] Verificando pre-requisitos...

where docker >nul 2>&1
if errorlevel 1 (
    echo       [ERRO] Docker nao encontrado no PATH. Instale o Docker Desktop.
    goto FIM_ERRO
)

docker info >nul 2>&1
if errorlevel 1 (
    echo       [ERRO] Docker nao esta rodando. Abra o Docker Desktop e tente de novo.
    goto FIM_ERRO
)

echo       OK: Docker encontrado e rodando.
echo.

REM ---------- 1. Garante o banco no ar (o pipeline le a tabela evento_uso) ----------
echo [1/2] Subindo o PostgreSQL (se ainda nao estiver no ar)...
docker compose up -d postgres
if errorlevel 1 (
    echo       [ERRO] Falha ao subir o PostgreSQL via Docker Compose.
    goto FIM_ERRO
)
echo.
echo       OBS: o banco precisa ter eventos. Rode o app e use /demo/popular/{id}
echo       ^(ou os atalhos Ctrl+Shift+D / Ctrl+Shift+F no front^) antes do pipeline.
echo.

REM ---------- 2. Roda o pipeline em lote (bronze -> prata -> ouro) ----------
echo [2/2] Rodando o pipeline PySpark (bronze -^> prata -^> ouro)...
docker compose run --rm spark
if errorlevel 1 (
    echo       [ERRO] Falha ao rodar o pipeline. O banco esta populado?
    goto FIM_ERRO
)
echo.

echo ============================================
echo  Pipeline concluido com sucesso!
echo.
echo  Tabelas geradas (Parquet) no volume ecowatt_spark_data,
echo  em /app/data/ouro/ dentro do container:
echo    - mapa_calor
echo    - ranking_endpoints
echo    - ranking_cliques
echo    - ranking_cliques_pagina
echo ============================================
echo.
pause
endlocal
goto :EOF

:FIM_ERRO
echo.
echo ============================================
echo  Pipeline INTERROMPIDO. Veja a mensagem acima.
echo ============================================
echo.
pause
endlocal
