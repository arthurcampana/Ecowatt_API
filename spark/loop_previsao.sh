#!/bin/sh
# Mantém a previsão de consumo atualizada sem ninguém precisar abrir o terminal.
#
# Este é o modo "serviço" do pipeline: fica vivo e, a cada INTERVALO_PREVISAO
# segundos, roda o previsao.py (que lê a tabela `consumo`, calcula e grava em
# `previsao_consumo`). Assim, depois de popular dados (Ctrl+Shift+D no app), a
# previsão aparece sozinha ao recarregar a página, em até um intervalo.
#
# O pipeline de analytics (bronze/prata/ouro) NÃO entra no loop: ele depende da
# tabela evento_uso e continua disponível sob demanda via
#   docker compose run --rm spark sh -c "python bronze.py && python prata.py && python ouro.py"
#
# Uma falha numa rodada (ex.: banco ainda subindo, nenhum consumo registrado)
# é registrada e ignorada — o serviço nunca cai por causa disso.

INTERVALO="${INTERVALO_PREVISAO:-60}"

echo "[loop] Servico de previsao iniciado. Recalcula a cada ${INTERVALO}s."

while true; do
    echo "[loop] $(date '+%Y-%m-%d %H:%M:%S') rodando previsao.py..."
    python previsao.py || echo "[loop] previsao.py falhou nesta rodada (ignorado)."
    sleep "${INTERVALO}"
done
