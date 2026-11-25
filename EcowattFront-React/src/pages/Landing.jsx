import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import Icon from "../components/Icon.jsx";
import "../styles/landing.css";

// Pagina inicial publica (rota /). Apresenta o EcoWatt e leva para login/cadastro.
export default function Landing() {
  const { autenticado } = useAuth();
  const { alternarTema } = useTheme();

  const recursos = [
    {
      icon: "calendar",
      titulo: "Registre seu consumo",
      texto: "Lance o consumo mensal em kWh e acompanhe o histórico mês a mês.",
    },
    {
      icon: "plug",
      titulo: "Cadastre equipamentos",
      texto: "Saiba o consumo esperado de cada aparelho e onde o gasto se concentra.",
    },
    {
      icon: "chart",
      titulo: "Dashboard e relatórios",
      texto: "Indicadores, gráficos e relatórios em PDF para enxergar tudo de forma clara.",
    },
    {
      icon: "trending",
      titulo: "Previsão com IA",
      texto: "Um modelo projeta seu consumo dos próximos meses a partir do histórico.",
    },
    {
      icon: "battery",
      titulo: "Metas e alertas",
      texto: "Defina uma meta mensal e receba o alerta quando o consumo se aproximar dela.",
    },
    {
      icon: "leaf",
      titulo: "Consumo consciente",
      texto: "Transforme dados em decisões de economia e reduza o desperdício de energia.",
    },
  ];

  return (
    <div className="landing">
      {/* Cabeçalho */}
      <header className="landing-header">
        <div className="landing-logo">
          <Icon name="leaf" size={24} /> EcoWatt
        </div>
        <nav className="landing-nav">
          <button
            type="button"
            className="landing-tema"
            onClick={alternarTema}
            title="Alternar tema"
          >
            <Icon name="sun" size={16} />
          </button>
          {autenticado ? (
            <Link to="/dashboard" className="landing-btn">
              Ir ao painel
            </Link>
          ) : (
            <>
              <Link to="/login" className="landing-link">
                Entrar
              </Link>
              <Link to="/cadastro" className="landing-btn">
                Criar conta
              </Link>
            </>
          )}
        </nav>
      </header>

      {/* Destaque principal */}
      <section className="landing-hero">
        <div className="landing-hero-texto">
          <span className="landing-eyebrow">Monitoramento de energia</span>
          <h1>
            Entenda e controle o seu <span>consumo de energia</span>.
          </h1>
          <p>
            O EcoWatt reúne o registro do seu consumo, o gasto de cada
            equipamento, metas, relatórios e uma previsão com inteligência
            artificial — tudo em um só lugar, para a conta de luz deixar de ser
            uma surpresa no fim do mês.
          </p>
          <div className="landing-hero-acoes">
            <Link to="/cadastro" className="landing-btn grande">
              Começar agora
            </Link>
            <Link to="/login" className="landing-link grande">
              Já tenho conta →
            </Link>
          </div>
        </div>
        <div className="landing-hero-arte" aria-hidden="true">
          <div className="landing-card-flutuante">
            <div className="lcf-linha">
              <span>Consumo do mês</span>
              <strong>182 kWh</strong>
            </div>
            <div className="lcf-barra">
              <div className="lcf-barra-fill" style={{ width: "72%" }} />
            </div>
            <div className="lcf-linha muted">
              <span>Meta: 250 kWh</span>
              <span className="lcf-ok">dentro da meta</span>
            </div>
            <div className="lcf-previsao">
              <Icon name="trending" size={16} />
              Previsão (IA): ~190 kWh no próximo mês
            </div>
          </div>
        </div>
      </section>

      {/* Recursos */}
      <section className="landing-recursos">
        <h2>O que você consegue fazer</h2>
        <div className="landing-grid">
          {recursos.map((r) => (
            <article key={r.titulo} className="landing-recurso">
              <div className="landing-recurso-ico">
                <Icon name={r.icon} size={22} />
              </div>
              <h3>{r.titulo}</h3>
              <p>{r.texto}</p>
            </article>
          ))}
        </div>
      </section>

      {/* Chamada final */}
      <section className="landing-cta">
        <h2>Comece a economizar hoje</h2>
        <p>Crie sua conta gratuita e acompanhe seu consumo em minutos.</p>
        <Link to="/cadastro" className="landing-btn grande">
          Criar conta gratuita
        </Link>
      </section>

      <footer className="landing-footer">
        <span>
          <Icon name="leaf" size={16} /> EcoWatt
        </span>
        <span>Projeto Aplicado IV · UniSENAI Santa Catarina · 2026</span>
      </footer>
    </div>
  );
}
