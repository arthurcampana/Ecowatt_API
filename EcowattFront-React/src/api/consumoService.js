import { api } from "./apiClient.js";

// Chamadas relacionadas a consumo. Centraliza os endpoints para que as
// paginas nao precisem conhecer URLs nem montar headers.
export const consumoService = {
  listarPorUsuario: (usuarioId) => api.get(`/consumo/usuario/${usuarioId}`),

  buscar: (id) => api.get(`/consumo/${id}`),

  adicionar: (dados) => api.post("/consumo/add", dados),

  alterar: (id, dados) => api.put(`/consumo/${id}`, dados),

  remover: (id) => api.delete(`/consumo/${id}`),
};
