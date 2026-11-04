import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

// Envolve rotas que exigem usuario autenticado. Sem sessao, redireciona
// para o login (substitui o "if (!token) window.location = login.html"
// que estava repetido em cada pagina do front antigo).
export default function RotaProtegida({ children, somenteAdmin = false }) {
  const { autenticado, isAdmin } = useAuth();

  if (!autenticado) {
    return <Navigate to="/login" replace />;
  }

  // Rotas exclusivas de ADMIN (ex.: Analytics): usuario logado sem perfil
  // de admin e redirecionado para o inicio.
  if (somenteAdmin && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
}
