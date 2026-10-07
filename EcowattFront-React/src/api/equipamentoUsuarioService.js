import { api } from "./apiClient.js";

// Equipamentos vinculados a um usuario (instancias do catalogo com horas de uso
// e consumo esperado). Centraliza os endpoints para as paginas.
export const equipamentoUsuarioService = {
  listarPorUsuario: (usuarioId) =>
    api.get(`/equipamentousuario/listar/${usuarioId}`),

  buscar: (id) => api.get(`/equipamentousuario/${id}`),

  adicionar: (dados) => api.post("/equipamentousuario/add", dados),

  alterar: (id, dados) => api.put(`/equipamentousuario/${id}`, dados),

  remover: (id) => api.delete(`/equipamentousuario/${id}`),
};
