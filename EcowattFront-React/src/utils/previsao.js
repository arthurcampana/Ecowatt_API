// Utilitarios da previsao de consumo (saida do pipeline Spark, previsao.py).

// 'YYYY-MM' do mes corrente, no fuso do navegador.
export function anoMesAtual(data = new Date()) {
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  return `${data.getFullYear()}-${mes}`;
}

// A previsao so e recalculada quando o pipeline roda. Se ele nao rodar ha
// algum tempo, a tabela pode ter meses que ja passaram: eles sao descartados
// para a tela nunca mostrar uma "previsao" do passado. Devolve ordenado.
export function previsoesFuturas(lista, data = new Date()) {
  const atual = anoMesAtual(data);
  return (lista || [])
    .filter((p) => p && typeof p.anoMes === "string" && p.anoMes >= atual)
    .sort((a, b) => a.anoMes.localeCompare(b.anoMes));
}

// 'YYYY-MM' -> nome do mes por extenso em pt-BR (ex.: 'dezembro de 2026').
export function nomeAnoMes(anoMes, opcoes = { month: "long", year: "numeric" }) {
  const [ano, mes] = String(anoMes).split("-");
  return new Date(Number(ano), Number(mes) - 1, 1).toLocaleDateString(
    "pt-BR",
    opcoes
  );
}
