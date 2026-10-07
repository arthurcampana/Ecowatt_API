import { api } from "./apiClient.js";

// Chamadas relacionadas a usuario. Centraliza os endpoints para que as
// paginas nao precisem conhecer URLs nem montar headers.
export const usuarioService = {
  // Cadastro e endpoint publico (nao envia Authorization).
  cadastrar: ({ nome, email, senha }) =>
    api.post("/usuario/add", { nome, email, senha }, { auth: false }),

  buscar: (id) => api.get(`/usuario/buscar/${id}`),

  alterar: (id, dados) => api.put(`/usuario/alterar/${id}`, dados),
};
