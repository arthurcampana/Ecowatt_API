import { useState, useEffect } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";

const links = [
  { to: "/", label: "Início", icon: "⌂", end: true },
  { to: "/consumo", label: "Consumo", icon: "⚡" },
  { to: "/equipamentos", label: "Equipamentos", icon: "🔌" },
  { to: "/relatorios", label: "Relatórios", icon: "📈" },
  { to: "/configuracoes", label: "Configurações", icon: "⚙" },
  { to: "/perfil", label: "Perfil", icon: "👤" },
];

const COLLAPSE_KEY = "ecowatt-sidebar-collapsed";

function lerColapsoSalvo() {
  try {
    return localStorage.getItem(COLLAPSE_KEY) === "true";
  } catch {
    return false;
  }
}

// Menu lateral das telas internas (layout "Opção C"), retratil:
// expande (icone + texto) ou colapsa para uma faixa estreita so com icones.
// O estado tambem e refletido em <html data-sidebar="..."> para que outras
// telas (ex.: Dashboard) possam reagir ao espaco disponivel. Preferencia
// persistida em localStorage.
export default function Sidebar() {
  const { logout, usuario } = useAuth();
  const { tema, alternarTema } = useTheme();
  const navigate = useNavigate();

  const [colapsada, setColapsada] = useState(lerColapsoSalvo);

  useEffect(() => {
    document.documentElement.setAttribute(
      "data-sidebar",
      colapsada ? "collapsed" : "expanded"
    );
    try {
      localStorage.setItem(COLLAPSE_KEY, String(colapsada));
    } catch {
      /* ignora */
    }
  }, [colapsada]);

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const iniciais = (usuario?.nome || "?")
    .split(" ")
    .map((p) => p[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <aside className={colapsada ? "sidebar is-colapsada" : "sidebar"}>
      <div className="sidebar-topo">
        <NavLink to="/" className="sidebar-brand">
          <span className="sidebar-brand-ico">🌱</span>
          <span className="sidebar-brand-txt">EcoWatt</span>
        </NavLink>
        <button
          type="button"
          className="sidebar-toggle"
          onClick={() => setColapsada((v) => !v)}
          title={colapsada ? "Expandir menu" : "Recolher menu"}
          aria-label={colapsada ? "Expandir menu" : "Recolher menu"}
        >
          {colapsada ? "»" : "«"}
        </button>
      </div>

      <nav className="sidebar-nav">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.end}
            className={({ isActive }) =>
              isActive ? "sidebar-link active" : "sidebar-link"
            }
            title={colapsada ? link.label : undefined}
          >
            <span className="sidebar-link-icon">{link.icon}</span>
            <span className="sidebar-link-txt">{link.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-rodape">
        <button
          type="button"
          className="sidebar-tema"
          onClick={alternarTema}
          title="Alternar tema claro/escuro"
        >
          <span className="sidebar-tema-ico">
            {tema === "dark" ? "☀️" : "🌙"}
          </span>
          <span className="sidebar-tema-txt">
            {tema === "dark" ? "Tema claro" : "Tema escuro"}
          </span>
        </button>

        <div className="sidebar-user">
          <span className="sidebar-avatar">{iniciais}</span>
          <span className="sidebar-user-nome">{usuario?.nome}</span>
        </div>

        <button
          type="button"
          className="sidebar-sair"
          onClick={handleLogout}
          title={colapsada ? "Sair" : undefined}
        >
          <span className="sidebar-sair-ico">↪</span>
          <span className="sidebar-sair-txt">Sair</span>
        </button>
      </div>
    </aside>
  );
}
