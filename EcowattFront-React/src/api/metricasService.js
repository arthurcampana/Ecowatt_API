import { api } from "./apiClient.js";

// Chamadas de metricas de uso. Segue o mesmo padrao dos outros services:
// usa o api client central, que ja cuida de token, headers e 401.
//
// Obs.: o nome evita a palavra "analytics" nos arquivos do front porque
// bloqueadores de anuncio costumam barrar caminhos com esse termo
// (ERR_BLOCKED_BY_CLIENT), o que quebrava o carregamento da pagina.
export const metricasService = {
  // Registra um clique de interface (camada bronze: envia cru).
  // So e aceito pelo backend se houver usuario logado (JWT).
  registrarClique: (rotulo, pagina) =>
    api.post("/metricas/evento-clique", { rotulo, pagina }),

  // Mapa de calor de acessos (dia da semana x hora).
  mapaCalor: () => api.get("/metricas/mapa-calor"),

  // Ranking de endpoints de API mais usados.
  endpointsMaisUsados: () => api.get("/metricas/endpoints-mais-usados"),

  // Ranking de cliques de interface (onde os usuarios mais clicam).
  cliquesMaisUsados: () => api.get("/metricas/cliques-mais-usados"),
};
