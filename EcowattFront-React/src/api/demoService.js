import { api } from "./apiClient.js";

// Chamada ao endpoint de demonstracao do backend (DemoController), que popula
// o banco do usuario com equipamentos, configuracao e historico de consumo.
export const demoService = {
  popular: (usuarioId) => api.post(`/demo/popular/${usuarioId}`),
};
