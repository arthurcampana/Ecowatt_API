import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import { api, setUnauthorizedHandler } from "../api/apiClient.js";

const AuthContext = createContext(null);

const USER_KEY = "usuario";
const TOKEN_KEY = "token";

function lerUsuarioSalvo() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(lerUsuarioSalvo);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUsuario(null);
  }, []);

  // Atualiza parcialmente o usuario em memoria (ex.: nome alterado no Perfil)
  // mantendo o localStorage em sincronia, sem exigir novo login.
  const atualizarUsuario = useCallback((parcial) => {
    setUsuario((prev) => {
      const novo = { ...prev, ...parcial };
      localStorage.setItem(USER_KEY, JSON.stringify(novo));
      return novo;
    });
  }, []);

  // Registra o logout como handler global de 401: se qualquer chamada da API
  // responder "nao autorizado", a sessao e encerrada de forma consistente.
  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  async function login(email, senha) {
    // Endpoint publico; nao envia Authorization.
    const resposta = await api.post(
      "/usuario/login",
      { email, senha },
      { auth: false }
    );

    const dadosUsuario = {
      id: resposta.id,
      nome: resposta.nome,
      email: resposta.email,
    };

    localStorage.setItem(USER_KEY, JSON.stringify(dadosUsuario));
    localStorage.setItem(TOKEN_KEY, resposta.token);
    setUsuario(dadosUsuario);

    return dadosUsuario;
  }

  const value = {
    usuario,
    autenticado: Boolean(usuario),
    login,
    logout,
    atualizarUsuario,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth deve ser usado dentro de <AuthProvider>");
  }
  return ctx;
}
