import { api } from "./apiClient.js";

// Chamadas relacionadas a configuracao do usuario. O chamador trata o 404 de
// buscarPorUsuario como "ainda nao existe configuracao", nao como erro.
export const configuracaoService = {
  buscarPorUsuario: (usuarioId) => api.get(`/configuracao/usuario/${usuarioId}`),

  adicionar: (dados) => api.post("/configuracao/add", dados),

  alterar: (id, dados) => api.put(`/configuracao/alterar/${id}`, dados),
};
