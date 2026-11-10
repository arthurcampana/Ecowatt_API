import { api } from "./apiClient.js";

// Chamadas relacionadas a previsao de consumo. Centraliza o endpoint para que
// as paginas nao precisem conhecer URLs nem montar headers.
export const previsaoService = {
  getPorUsuario: (usuarioId) => api.get(`/previsao/usuario/${usuarioId}`),
};
