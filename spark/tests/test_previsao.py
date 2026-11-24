"""
Testes da lógica de previsão (spark/previsao.py), com Spark local e sem banco.

Rodar a partir da raiz do repositório:
    python -m pytest spark/tests
"""

import math
import os
import sys

import pytest

# Permite "import previsao" e "import config" a partir de spark/.
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from pyspark.sql import SparkSession  # noqa: E402

import previsao  # noqa: E402
from previsao import (  # noqa: E402
    METODO_MEDIA,
    METODO_REGRESSAO,
    METODO_REGRESSAO_SAZONAL,
    meses_a_prever,
    meses_completos,
    prever_usuario,
)


@pytest.fixture(scope="session")
def spark():
    sessao = (
        SparkSession.builder
        .master("local[1]")
        .appName("EcoWatt-Previsao-Testes")
        .config("spark.ui.enabled", "false")
        .getOrCreate()
    )
    sessao.sparkContext.setLogLevel("ERROR")
    yield sessao
    sessao.stop()


def _serie(inicio, valores):
    """Meses consecutivos a partir de 'YYYY-MM' com os consumos dados."""
    base = previsao._indice_mes(inicio)
    return [
        {"ano_mes": previsao._ano_mes(base + i), "consumo": float(v)}
        for i, v in enumerate(valores)
    ]


# --- Seleção de meses -------------------------------------------------------

def test_mes_corrente_fica_fora_do_treino():
    meses = _serie("2026-08", [100, 110, 120, 40])  # nov/2026 ainda parcial
    completos = meses_completos(meses, "2026-11")
    assert [m["ano_mes"] for m in completos] == ["2026-08", "2026-09", "2026-10"]


def test_previsao_comeca_no_mes_corrente_sem_registro():
    meses = _serie("2026-08", [100, 110, 120])  # ultimo registro em out/2026
    assert meses_a_prever(meses, "2026-11") == ["2026-11", "2026-12", "2027-01"]


def test_previsao_pula_mes_corrente_ja_registrado():
    meses = _serie("2026-09", [100, 110, 120])  # inclui nov/2026
    assert meses_a_prever(meses, "2026-11") == ["2026-12", "2027-01", "2027-02"]


def test_previsao_nunca_aponta_para_o_passado():
    meses = _serie("2025-01", [100, 110, 120])  # parou de registrar em mar/2025
    assert meses_a_prever(meses, "2026-11")[0] == "2026-11"


# --- Escolha do método ------------------------------------------------------

def test_sem_mes_completo_nao_gera_previsao(spark):
    meses = _serie("2026-11", [80])  # so o mes corrente
    assert prever_usuario(spark, meses, "2026-11") is None


def test_poucos_meses_usa_media(spark):
    meses = _serie("2026-06", [100, 200, 300, 400, 500])  # 5 meses completos
    resultado = prever_usuario(spark, meses, "2026-11")
    assert resultado["metodo"] == METODO_MEDIA
    assert resultado["meses_usados"] == 5
    assert all(valor == pytest.approx(300.0) for _, valor in resultado["previsoes"])


def test_tendencia_clara_usa_regressao_e_segue_a_reta(spark):
    # 8 meses crescendo 10 kWh/mes: jan..ago/2026 = 100..170.
    meses = _serie("2026-01", [100 + 10 * i for i in range(8)])
    resultado = prever_usuario(spark, meses, "2026-09")
    assert resultado["metodo"] == METODO_REGRESSAO
    assert resultado["meses_usados"] == 8
    valores = [valor for _, valor in resultado["previsoes"]]
    assert valores == pytest.approx([180.0, 190.0, 200.0], abs=0.5)
    # A reta acerta o backtest; a media historica erra bastante.
    assert resultado["mae_modelo"] == pytest.approx(0.0, abs=0.5)
    assert resultado["mae_media"] > 20


def test_meses_sem_registro_nao_distorcem_a_tendencia(spark):
    # Mesma reta de 10 kWh/mes, mas sem registros em mar e abr/2026.
    completos = _serie("2026-01", [100 + 10 * i for i in range(9)])
    meses = [m for m in completos if m["ano_mes"] not in ("2026-03", "2026-04")]
    resultado = prever_usuario(spark, meses, "2026-10")
    assert resultado["metodo"] == METODO_REGRESSAO
    assert resultado["previsoes"][0] == ("2026-10", pytest.approx(190.0, abs=0.5))


def test_doze_meses_usa_sazonalidade(spark):
    # 24 meses com ciclo anual (verao alto, inverno baixo) e sem tendencia.
    valores = [
        300 + 50 * math.cos(2 * math.pi * ((i % 12) + 1) / 12.0)
        for i in range(24)
    ]
    meses = _serie("2024-11", valores)
    resultado = prever_usuario(spark, meses, "2026-11")
    assert resultado["metodo"] == METODO_REGRESSAO_SAZONAL
    assert resultado["mae_modelo"] < resultado["mae_media"]


def test_regressao_pior_que_media_cai_para_media(spark):
    # Subiu por 6 meses e estabilizou: a reta continua subindo e erra muito
    # os ultimos meses, enquanto a media fica perto do patamar atual.
    valores = [100, 150, 200, 250, 300, 350, 200, 200, 200]
    meses = _serie("2026-01", valores)
    resultado = prever_usuario(spark, meses, "2026-10")
    assert resultado["metodo"] == METODO_MEDIA
    assert resultado["mae_modelo"] == resultado["mae_media"]


def test_previsao_nunca_negativa(spark):
    # Queda forte: a reta cruzaria o zero.
    meses = _serie("2026-01", [400, 330, 260, 190, 120, 60, 20, 5])
    resultado = prever_usuario(spark, meses, "2026-09")
    assert all(valor >= 0.0 for _, valor in resultado["previsoes"])
