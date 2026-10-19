@echo off
chcp 65001 >nul
title EcoWatt - Pipeline de Dados (PySpark)
setlocal

REM Pasta onde este .bat esta (raiz do projeto), com barra final.
set "RAIZ=%~dp0"
set "SPARK=%RAIZ%spark"
set "JDBC=postgresql-42.7.4.jar"

echo ============================================
echo   EcoWatt - Pipeline de Dados (bronze/prata/ouro)
echo ============================================
echo.

cd /d "%SPARK%"

REM ---------- Verificacoes ----------
echo [0/4] Verificando pre-requisitos...

where python >nul 2>&1
if errorlevel 1 (
    echo       [ERRO] Python nao encontrado no PATH. Instale o Python 3.9+.
    goto FIM_ERRO
)

python -c "import pyspark" >nul 2>&1
if errorlevel 1 (
    echo       [ERRO] PySpark nao instalado. Rode:  pip install -r spark\requirements.txt
    goto FIM_ERRO
)

if not exist "%SPARK%\%JDBC%" (
    echo       [ERRO] Driver JDBC nao encontrado: %JDBC%
    echo       Baixe de https://jdbc.postgresql.org/download/ e coloque em spark\
    goto FIM_ERRO
)

if not exist "%SPARK%\hadoop\bin\winutils.exe" (
    echo       [ERRO] winutils.exe nao encontrado em spark\hadoop\bin\
    echo       Baixe winutils.exe e hadoop.dll de:
    echo         https://github.com/cdarlint/winutils ^(pasta hadoop-3.3.6/bin^)
    echo       e coloque em spark\hadoop\bin\
    goto FIM_ERRO
)

echo       OK: Python, PySpark, driver JDBC e winutils encontrados.
echo.

REM ---------- Configura ambiente do Spark no Windows ----------
set "HADOOP_HOME=%SPARK%\hadoop"
set "PATH=%HADOOP_HOME%\bin;%PATH%"

REM ---------- 1. BRONZE (le o Postgres via JDBC) ----------
echo [1/4] BRONZE - lendo evento_uso do PostgreSQL...
set "PYSPARK_SUBMIT_ARGS=--jars %JDBC% pyspark-shell"
python bronze.py
if errorlevel 1 (
    echo       [ERRO] Falha na camada BRONZE. O PostgreSQL esta rodando e populado?
    goto FIM_ERRO
)
echo.

REM As proximas camadas leem Parquet; nao precisam do driver JDBC.
set "PYSPARK_SUBMIT_ARGS="

REM ---------- 2. PRATA (limpeza e padronizacao) ----------
echo [2/4] PRATA - limpando e padronizando...
python prata.py
if errorlevel 1 (
    echo       [ERRO] Falha na camada PRATA.
    goto FIM_ERRO
)
echo.

REM ---------- 3. OURO (agregacoes finais) ----------
echo [3/4] OURO - gerando mapa de calor e rankings...
python ouro.py
if errorlevel 1 (
    echo       [ERRO] Falha na camada OURO.
    goto FIM_ERRO
)
echo.

REM ---------- 4. Resultado ----------
echo [4/4] Pipeline concluido com sucesso!
echo.
echo ============================================
echo  Tabelas geradas em spark\data\ouro\:
echo    - mapa_calor
echo    - ranking_endpoints
echo    - ranking_cliques
echo  ^(formato Parquet; bronze e prata em spark\data\^)
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
