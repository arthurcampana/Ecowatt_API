import { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar.jsx";
import DemoKonami from "./DemoKonami.jsx";
import LabKonami from "./LabKonami.jsx";
import { useClickTracking } from "../metricas/useClickTracking.js";
import HeatmapOverlay from "../metricas/HeatmapOverlay.jsx";
import "../styles/dashboard.css";

// Casca das paginas internas (layout "Opção C"): menu lateral fixo +
// area de conteudo. As rotas filhas sao renderizadas no <Outlet />.
export default function Layout() {
  // Rastreia os cliques de toda a area logada (camada bronze de metricas).
  useClickTracking();

  // Overlay de mapa de calor de cliques (liga/desliga pelo botao flutuante).
  const [heatmapAberto, setHeatmapAberto] = useState(false);

  return (
    <div className="app-shell-v2">
      <Sidebar />
      <main className="app-content-v2">
        <Outlet />
      </main>
      <DemoKonami />
      <LabKonami />

      {/* Botao flutuante para ligar o mapa de calor de cliques */}
      {!heatmapAberto && (
        <button
          type="button"
          onClick={() => setHeatmapAberto(true)}
          title="Mostrar mapa de calor de cliques"
          style={{
            position: "fixed",
            bottom: 20,
            right: 20,
            zIndex: 9997,
            width: 52,
            height: 52,
            borderRadius: "50%",
            border: "none",
            background: "#198754",
            color: "#fff",
            fontSize: 22,
            cursor: "pointer",
            boxShadow: "0 6px 20px rgba(0,0,0,.3)",
          }}
        >
          🔥
        </button>
      )}

      <HeatmapOverlay
        aberto={heatmapAberto}
        onFechar={() => setHeatmapAberto(false)}
      />
    </div>
  );
}
