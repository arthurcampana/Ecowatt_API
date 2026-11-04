@echo off
chcp 65001 >nul
title EcoWatt - Painel de Controle
setlocal

REM Pasta onde este .bat esta (raiz do projeto), com barra final.
set "RAIZ=%~dp0"
cd /d "%RAIZ%"

echo ============================================
echo            EcoWatt - Inicializador (Docker)
echo ============================================
echo.

REM ---------- Verificacoes ----------
echo [1/2] Verificando o Docker...

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

REM ---------- Sobe o stack completo (postgres + backend + frontend) ----------
echo [2/2] Subindo EcoWatt via Docker Compose ^(build na primeira vez^)...
echo       Isso pode levar alguns minutos no primeiro uso.
echo.
docker compose up -d --build postgres backend frontend
if errorlevel 1 (
    echo       [ERRO] Falha ao subir o stack via Docker Compose.
    goto FIM_ERRO
)

echo.
echo ============================================
echo  Pronto! O EcoWatt esta no ar (containers Docker).
echo    - PostgreSQL ^(porta 5432^)
echo    - Backend    ^(porta 8080^)
echo    - Frontend   ^(porta 5500^)
echo.
echo  Acesse: http://localhost:5500
echo.
echo  Para gerar dados de exemplo: faca login e use Ctrl+Shift+D,
echo  ou chame POST http://localhost:8080/demo/popular/{usuarioId}.
echo.
echo  Pipeline de dados (PySpark): rode  rodar-pipeline-spark.bat
echo  Parar tudo: rode  parar-ecowatt.bat  (ou digite P abaixo).
echo ============================================
echo.

:MENU
echo --------------------------------------------
set "ESCOLHA="
set /p "ESCOLHA=Digite L para ver LOGS, P para PARAR tudo (ou feche a janela): "
if /i "%ESCOLHA%"=="P" goto PARAR
if /i "%ESCOLHA%"=="L" (
    echo.
    echo  Mostrando logs ^(Ctrl+C para voltar ao menu^)...
    docker compose logs -f --tail 40
    echo.
    goto MENU
)
goto MENU

:PARAR
echo.
echo Encerrando os containers do EcoWatt...
docker compose stop postgres backend frontend
echo.
echo EcoWatt encerrado. (Os dados do banco continuam salvos no volume.)
echo.
pause
endlocal
goto :EOF

:FIM_ERRO
echo.
echo ============================================
echo  Inicializacao INTERROMPIDA. Veja a mensagem acima.
echo ============================================
echo.
pause
endlocal
