import { api } from "./apiClient.js";

// Chamadas relacionadas a usuario. Centraliza os endpoints para que as
// paginas nao precisem conhecer URLs nem montar headers.
export const usuarioService = {
  // Cadastro e endpoint publico (nao envia Authorization).
  // aceiteTermos (LGPD) e obrigatorio: o backend recusa o cadastro sem ele.
  cadastrar: ({ nome, email, senha, aceiteTermos }) =>
    api.post("/usuario/add", { nome, email, senha, aceiteTermos }, { auth: false }),

  buscar: (id) => api.get(`/usuario/buscar/${id}`),

  alterar: (id, dados) => api.put(`/usuario/alterar/${id}`, dados),
};
