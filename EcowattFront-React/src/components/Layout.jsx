import { Outlet } from "react-router-dom";
import Navbar from "./Navbar.jsx";

// Casca das paginas internas: navbar fixa no topo + area de conteudo.
// As rotas filhas sao renderizadas no <Outlet />.
export default function Layout() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}
