// Formatadores de data compartilhados entre as telas, portados do frontend
// legado (consumo.js e perfil.js) para manter o mesmo comportamento.

// Data + hora (ex.: lista de consumos). "Sem data" quando nao ha valor,
// igual ao legado consumo.js.
export function formatarDataHora(iso) {
  if (!iso) {
    return "Sem data";
  }

  return new Date(iso).toLocaleString("pt-BR");
}

// Apenas data (ex.: perfil). "--" quando nao ha valor, igual ao legado
// perfil.js.
export function formatarData(iso) {
  if (!iso) {
    return "--";
  }

  return new Date(iso).toLocaleDateString("pt-BR");
}
