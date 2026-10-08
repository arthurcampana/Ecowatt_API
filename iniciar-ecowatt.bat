@echo off
chcp 65001 >nul
title EcoWatt - Painel de Controle
setlocal

REM Pasta onde este .bat esta (raiz do projeto), com barra final.
set "RAIZ=%~dp0"

echo ============================================
echo            EcoWatt - Inicializador
echo ============================================
echo.

REM ---------- 1. PostgreSQL ----------
echo [1/3] Verificando PostgreSQL...
sc query postgresql-x64-18 | find "RUNNING" >nul
if errorlevel 1 (
    echo       Servico parado. Tentando iniciar...
    net start postgresql-x64-18 >nul 2>&1
    if errorlevel 1 (
        echo       [AVISO] Nao foi possivel iniciar o PostgreSQL automaticamente.
        echo       Inicie manualmente ^(precisa de permissao de administrador^) e rode de novo.
        echo.
    ) else (
        echo       PostgreSQL iniciado.
    )
) else (
    echo       PostgreSQL ja esta rodando.
)
echo.

REM ---------- 2. Backend ----------
echo [2/3] Iniciando backend ^(Spring Boot^) em nova janela...
start "EcoWatt Backend" cmd /k "cd /d "%RAIZ%demo\demo" && mvnw.cmd spring-boot:run"
echo       Backend subindo em http://localhost:8080
echo.

REM ---------- Esperar o backend responder na porta 8080 ----------
echo.
echo       Aguardando o backend ficar pronto ^(pode levar ate ~40s^)...
set /a TENTATIVAS=0
:ESPERA_BACKEND
set /a TENTATIVAS+=1
powershell -NoProfile -Command "try { (New-Object Net.Sockets.TcpClient).Connect('localhost',8080); exit 0 } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 goto BACKEND_PRONTO
if %TENTATIVAS% geq 40 (
    echo       [AVISO] Backend demorou a responder. Abrindo o front mesmo assim.
    echo       Se der erro de conexao, aguarde e recarregue a pagina ^(F5^).
    goto BACKEND_PRONTO
)
timeout /t 1 /nobreak >nul
goto ESPERA_BACKEND

:BACKEND_PRONTO
echo       Backend pronto.
echo.

REM ---------- 3. Frontend ----------
echo [3/3] Preparando frontend ^(React/Vite^)...
if not exist "%RAIZ%EcowattFront-React\node_modules" (
    echo       node_modules nao encontrado. Rodando npm install ^(primeira vez^)...
    pushd "%RAIZ%EcowattFront-React"
    call npm install
    popd
)
echo       Iniciando frontend em nova janela...
start "EcoWatt Frontend" cmd /k "cd /d "%RAIZ%EcowattFront-React" && npm run dev"
echo       Frontend subindo em http://localhost:5500
echo.

echo ============================================
echo  Pronto! O EcoWatt esta subindo.
echo    - Backend  ^(porta 8080^)
echo    - Frontend ^(porta 5500^)
echo.
echo  Aguarde alguns segundos e acesse:
echo    http://localhost:5500
echo ============================================
echo.
echo  Esta janela e o PAINEL DE CONTROLE.
echo  Deixe-a aberta enquanto usa o sistema.
echo.

:MENU
echo --------------------------------------------
set "ESCOLHA="
set /p "ESCOLHA=Digite P e Enter para PARAR tudo (ou feche a janela): "
if /i "%ESCOLHA%"=="P" goto PARAR
goto MENU

:PARAR
echo.
echo Encerrando backend e frontend...

REM Fecha as janelas abertas por este script (pelo titulo).
taskkill /FI "WINDOWTITLE eq EcoWatt Backend*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq EcoWatt Frontend*" /T /F >nul 2>&1

REM Garante o encerramento dos processos que seguram as portas.
REM Backend: processos java; Frontend: node (Vite).
taskkill /IM java.exe /F >nul 2>&1
taskkill /IM node.exe /F >nul 2>&1

echo.
echo EcoWatt encerrado. As portas 8080 e 5500 foram liberadas.
echo (O PostgreSQL continua rodando como servico do Windows.)
echo.
pause
endlocal
