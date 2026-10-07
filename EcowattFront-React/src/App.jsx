import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Cadastro from "./pages/Cadastro.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Perfil from "./pages/Perfil.jsx";
import Consumo from "./pages/Consumo.jsx";
import Configuracoes from "./pages/Configuracoes.jsx";
import Equipamentos from "./pages/Equipamentos.jsx";
import Relatorios from "./pages/Relatorios.jsx";
import Layout from "./components/Layout.jsx";
import RotaProtegida from "./components/RotaProtegida.jsx";

export default function App() {
  return (
    <Routes>
      {/* Rotas publicas */}
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />

      {/* Rotas protegidas: compartilham a navbar via Layout */}
      <Route
        element={
          <RotaProtegida>
            <Layout />
          </RotaProtegida>
        }
      >
        <Route path="/" element={<Dashboard />} />
        <Route path="/consumo" element={<Consumo />} />
        <Route path="/equipamentos" element={<Equipamentos />} />
        <Route path="/relatorios" element={<Relatorios />} />
        <Route path="/configuracoes" element={<Configuracoes />} />
        <Route path="/perfil" element={<Perfil />} />
      </Route>

      {/* Qualquer rota desconhecida cai no inicio */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
