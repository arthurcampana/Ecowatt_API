import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

const links = [
  { to: "/", label: "Início", end: true },
  { to: "/consumo", label: "Registrar Consumo" },
  { to: "/equipamentos", label: "Equipamentos" },
  { to: "/relatorios", label: "Relatórios" },
  { to: "/configuracoes", label: "Configurações" },
  { to: "/perfil", label: "Perfil" },
];

export default function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <NavLink to="/" className="navbar-brand">
          EcoWatt
        </NavLink>

        <ul className="navbar-links">
          {links.map((link) => (
            <li key={link.to}>
              <NavLink
                to={link.to}
                end={link.end}
                className={({ isActive }) =>
                  isActive ? "nav-link active" : "nav-link"
                }
              >
                {link.label}
              </NavLink>
            </li>
          ))}
          <li>
            <button className="nav-link nav-sair" onClick={handleLogout}>
              Sair
            </button>
          </li>
        </ul>
      </div>
    </nav>
  );
}
