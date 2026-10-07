import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

// Envolve rotas que exigem usuario autenticado. Sem sessao, redireciona
// para o login (substitui o "if (!token) window.location = login.html"
// que estava repetido em cada pagina do front antigo).
export default function RotaProtegida({ children }) {
  const { autenticado } = useAuth();

  if (!autenticado) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
