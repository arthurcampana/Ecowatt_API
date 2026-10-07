import { api } from "./apiClient.js";

// Catalogo base de equipamentos (modelos disponiveis), compartilhado entre
// usuarios. Centraliza os endpoints para que as paginas nao montem URLs/headers.
export const equipamentoService = {
  listar: () => api.get("/equipamentos/listar"),

  adicionar: (dados) => api.post("/equipamentos/add", dados),
};
