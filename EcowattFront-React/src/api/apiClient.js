import { API_URL } from "./config.js";

const TOKEN_KEY = "token";

// Callback chamado quando a API responde 401 (token ausente/expirado/invalido).
// O AuthContext registra aqui a sua funcao de logout, para centralizar o
// comportamento de "sessao expirou -> volta pro login".
let onUnauthorized = null;

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

// Erro de API com o status HTTP, para o chamador decidir como tratar.
export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

async function request(method, path, { body, auth = true, headers = {} } = {}) {
  const finalHeaders = { ...headers };

  if (body !== undefined) {
    finalHeaders["Content-Type"] = "application/json";
  }

  // O backend espera o header Authorization com o token (que ja vem com o
  // prefixo "Bearer " embutido, do jeito que o login do backend devolve).
  if (auth) {
    const token = getToken();
    if (token) {
      finalHeaders["Authorization"] = token;
    }
  }

  const response = await fetch(`${API_URL}${path}`, {
    method,
    headers: finalHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401 && onUnauthorized) {
    onUnauthorized();
  }

  // Tenta extrair o corpo como JSON; se nao for JSON, cai no texto.
  const raw = await response.text();
  let parsed = null;
  if (raw) {
    try {
      parsed = JSON.parse(raw);
    } catch {
      parsed = raw;
    }
  }

  if (!response.ok) {
    const message =
      (parsed && parsed.erro) ||
      (typeof parsed === "string" && parsed) ||
      `Erro ${response.status}`;
    throw new ApiError(message, response.status, parsed);
  }

  return parsed;
}

export const api = {
  get: (path, opts) => request("GET", path, opts),
  post: (path, body, opts) => request("POST", path, { ...opts, body }),
  put: (path, body, opts) => request("PUT", path, { ...opts, body }),
  delete: (path, opts) => request("DELETE", path, opts),
};
