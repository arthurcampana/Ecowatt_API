// URL base da API. Vem da variavel de ambiente VITE_API_URL,
// com fallback para o backend local em desenvolvimento.
export const API_URL =
  import.meta.env.VITE_API_URL ?? "http://localhost:8080";
