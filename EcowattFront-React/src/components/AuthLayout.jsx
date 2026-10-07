import { useTheme } from "../context/ThemeContext.jsx";
import "../styles/auth.css";

// Casca visual compartilhada por Login e Cadastro: painel da marca (verde) a
// esquerda e a area do formulario (children) a direita. No mobile o painel da
// marca e ocultado. Inclui um botao para alternar o tema claro/escuro, ja que
// aqui nao ha a sidebar.
export default function AuthLayout({ children }) {
  const { tema, alternarTema } = useTheme();

  return (
    <div className="auth">
      <button
        type="button"
        className="auth-tema"
        onClick={alternarTema}
        title="Alternar tema claro/escuro"
      >
        {tema === "dark" ? "☀️ Tema claro" : "🌙 Tema escuro"}
      </button>

      <aside className="auth-brand">
        <div className="auth-brand-logo">🌱 EcoWatt</div>

        <div className="auth-brand-texto">
          <h2>Consumo consciente, conta mais leve.</h2>
          <p>
            Monitore o consumo de energia da sua casa, acompanhe metas e
            descubra onde economizar — tudo em um só lugar.
          </p>
        </div>

        <ul className="auth-brand-features">
          <li>
            <span className="check">✓</span> Acompanhe seu consumo mês a mês
          </li>
          <li>
            <span className="check">✓</span> Defina metas e evite surpresas
          </li>
          <li>
            <span className="check">✓</span> Relatórios e gráficos detalhados
          </li>
        </ul>
      </aside>

      <main className="auth-form-area">
        <div className="auth-card">{children}</div>
      </main>
    </div>
  );
}
