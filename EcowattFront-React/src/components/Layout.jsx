import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar.jsx";
import DemoKonami from "./DemoKonami.jsx";
import LabKonami from "./LabKonami.jsx";
import "../styles/dashboard.css";

// Casca das paginas internas (layout "Opção C"): menu lateral fixo +
// area de conteudo. As rotas filhas sao renderizadas no <Outlet />.
export default function Layout() {
  return (
    <div className="app-shell-v2">
      <Sidebar />
      <main className="app-content-v2">
        <Outlet />
      </main>
      <DemoKonami />
      <LabKonami />
    </div>
  );
}
