import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bar, Doughnut } from "react-chartjs-2";
import "../charts/registerCharts.js";
import { useAuth } from "../context/AuthContext.jsx";
import { consumoService } from "../api/consumoService.js";
import { equipamentoUsuarioService } from "../api/equipamentoUsuarioService.js";
import { configuracaoService } from "../api/configuracaoService.js";
import { CORES } from "../utils/chartPalette.js";
import { useChartTheme } from "../charts/useChartTheme.js";
import LegendaEquipamentos from "../components/LegendaEquipamentos.jsx";
import Icon from "../components/Icon.jsx";

// Abreviados para o eixo do grafico (espaco limitado).
const MESES = [
  "Jan",
  "Fev",
  "Mar",
  "Abr",
  "Mai",
  "Jun",
  "Jul",
  "Ago",
  "Set",
  "Out",
  "Nov",
  "Dez",
];

// Nomes completos para os textos da interface.
const MESES_COMPLETOS = [
  "Janeiro",
  "Fevereiro",
  "Março",
  "Abril",
  "Maio",
  "Junho",
  "Julho",
  "Agosto",
  "Setembro",
  "Outubro",
  "Novembro",
  "Dezembro",
];

// Dashboard (rota /): cards de metrica com variacao mes a mes, card de meta
// com barra de progresso, custo estimado (a partir da tarifa configurada),
// graficos e um estado de boas-vindas quando a conta ainda nao tem dados.
export default function Dashboard() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const temaChart = useChartTheme();

  const [consumoMes, setConsumoMes] = useState(() => new Array(12).fill(0));
  const [equipamentos, setEquipamentos] = useState([]);
  const [config, setConfig] = useState(null); // { meta, valorTarifa }
  const [carregando, setCarregando] = useState(true);
  const [mensagem, setMensagem] = useState(null);

  useEffect(() => {
    let ativo = true;

    async function carregar() {
      setCarregando(true);
      try {
        const consumos = await consumoService.listarPorUsuario(usuario.id);
        if (ativo) {
          const anoAtual = new Date().getFullYear();
          const totais = new Array(12).fill(0);
          (consumos || []).forEach((c) => {
            if (!c.dataRegistro) {
              return;
            }
            const [ano, mes] = c.dataRegistro.split("T")[0].split("-");
            if (Number(ano) === anoAtual) {
              totais[Number(mes) - 1] += c.consumoKwh;
            }
          });
          setConsumoMes(totais);
        }
      } catch (error) {
        if (ativo) {
          setMensagem({
            texto: error?.message || "Erro ao carregar consumos",
            tipo: "erro",
          });
        }
      }

      try {
        const equips = await equipamentoUsuarioService.listarPorUsuario(
          usuario.id
        );
        if (ativo) {
          setEquipamentos(equips || []);
        }
      } catch (error) {
        if (ativo) {
          setMensagem({
            texto: error?.message || "Erro ao carregar equipamentos",
            tipo: "erro",
          });
        }
      }

      try {
        const cfg = await configuracaoService.buscarPorUsuario(usuario.id);
        if (ativo) {
          setConfig(cfg);
        }
      } catch (error) {
        // 404 = usuario ainda nao configurou; nao e erro.
        if (ativo && error?.status !== 404) {
          setConfig(null);
        }
      }

      if (ativo) {
        setCarregando(false);
      }
    }

    carregar();
    return () => {
      ativo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  const mesAtual = new Date().getMonth();

  // Metricas: mes atual, media mensal, total do ano e variacao vs. mes anterior.
  const { totalMesAtual, mediaMensal, totalAno, variacao } = useMemo(() => {
    const total = consumoMes.reduce((acc, v) => acc + v, 0);
    const mesesComConsumo = consumoMes.filter((v) => v > 0).length || 1;
    const atual = consumoMes[mesAtual];
    const anterior = mesAtual > 0 ? consumoMes[mesAtual - 1] : 0;

    let variacao = null;
    if (anterior > 0) {
      variacao = ((atual - anterior) / anterior) * 100;
    }

    return {
      totalMesAtual: atual,
      mediaMensal: total / mesesComConsumo,
      totalAno: total,
      variacao,
    };
  }, [consumoMes, mesAtual]);

  const nomeMesAtual = MESES_COMPLETOS[mesAtual];
  const primeiroNome = (usuario?.nome || "").split(" ")[0];

  const meta = config ? Number(config.meta) : 0;
  const tarifa = config ? Number(config.valorTarifa) : 0;
  const custoMes = totalMesAtual * tarifa;
  const progressoMeta = meta > 0 ? (totalMesAtual / meta) * 100 : 0;
  const acimaMeta = meta > 0 && totalMesAtual > meta;

  const semDados =
    !carregando &&
    totalAno === 0 &&
    equipamentos.length === 0 &&
    !config;

  const labelsEquip = equipamentos.map((e) => e.nomeIdentificacao);
  const valoresEquip = equipamentos.map((e) => Number(e.consumoEsperado));

  const dadosConsumo = {
    labels: MESES,
    datasets: [
      {
        label: "Consumo por mês (kWh)",
        data: consumoMes,
        backgroundColor: "#198754",
        borderWidth: 2,
        borderRadius: 8,
      },
    ],
  };

  const opcoesConsumo = { responsive: true, maintainAspectRatio: false };

  const dadosEquipamentos = {
    labels: labelsEquip,
    datasets: [
      {
        data: valoresEquip,
        backgroundColor: CORES,
        borderColor: "#fff",
        borderWidth: 3,
      },
    ],
  };

  const opcoesEquipamentos = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => ctx.label + ": " + ctx.raw.toFixed(2) + " kWh/dia",
        },
      },
    },
  };

  // ----- Loading: skeletons -----
  if (carregando) {
    return (
      <div className="dash">
        <div className="dash-head">
          <h1>Olá{primeiroNome ? `, ${primeiroNome}` : ""}</h1>
          <p>Carregando seu resumo...</p>
        </div>
        <section className="dash-metrics">
          <div className="dash-skel-card" />
          <div className="dash-skel-card" />
          <div className="dash-skel-card" />
        </section>
        <div className="dash-skel-panel" />
      </div>
    );
  }

  // ----- Conta nova: boas-vindas -----
  if (semDados) {
    return (
      <div className="dash">
        <div className="dash-head">
          <h1>Bem-vindo ao EcoWatt{primeiroNome ? `, ${primeiroNome}` : ""}</h1>
          <p>Vamos começar a monitorar seu consumo de energia.</p>
        </div>

        <div className="dash-welcome">
          <div className="dash-welcome-ico">
            <Icon name="leaf" size={34} />
          </div>
          <h3>Sua jornada começa aqui</h3>
          <p>
            Cadastre seus equipamentos e registre seu consumo para ver gráficos,
            metas e estimativas de custo neste painel.
          </p>
          <div className="dash-welcome-acoes">
            <button
              type="button"
              className="dash-welcome-btn primario"
              onClick={() => navigate("/consumo")}
            >
              <Icon name="bolt" size={16} /> Registrar consumo
            </button>
            <button
              type="button"
              className="dash-welcome-btn"
              onClick={() => navigate("/equipamentos")}
            >
              <Icon name="plug" size={16} /> Adicionar equipamento
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dash">
      <div className="dash-head">
        <h1>Olá{primeiroNome ? `, ${primeiroNome}` : ""}</h1>
        <p>Resumo do seu consumo de energia — {nomeMesAtual}</p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      {/* Cards de metrica */}
      <section className="dash-metrics">
        <article className="dash-metric verde">
          <div className="dash-metric-ico">
            <Icon name="calendar" />
          </div>
          <div className="dash-metric-valor">
            {totalMesAtual.toFixed(2)} <em>kWh</em>
          </div>
          <div className="dash-metric-label">
            Consumo em {nomeMesAtual}
            {variacao !== null && (
              <span
                className={`dash-variacao ${
                  variacao <= 0 ? "baixa" : "alta"
                }`}
              >
                {variacao <= 0 ? "▼" : "▲"} {Math.abs(variacao).toFixed(0)}% vs.
                mês anterior
              </span>
            )}
          </div>
        </article>

        <article className="dash-metric azul">
          <div className="dash-metric-ico">
            <Icon name="trending" />
          </div>
          <div className="dash-metric-valor">
            {mediaMensal.toFixed(2)} <em>kWh</em>
          </div>
          <div className="dash-metric-label">Média mensal no ano</div>
        </article>

        <article className="dash-metric ambar">
          <div className="dash-metric-ico">
            <Icon name="battery" />
          </div>
          <div className="dash-metric-valor">
            {totalAno.toFixed(2)} <em>kWh</em>
          </div>
          <div className="dash-metric-label">Total no ano</div>
        </article>
      </section>

      {/* Linha: meta (progresso) + custo estimado */}
      <section className="dash-row2">
        <div className="dash-panel dash-meta">
          <div className="dash-panel-head">
            <div>
              <h3>Meta do mês</h3>
              <p>
                {meta > 0
                  ? `${totalMesAtual.toFixed(0)} de ${meta.toFixed(0)} kWh`
                  : "Nenhuma meta definida"}
              </p>
            </div>
            {meta > 0 && (
              <span
                className={`dash-tag ${acimaMeta ? "perigo" : ""}`}
              >
                {acimaMeta ? "Acima da meta" : "Dentro da meta"}
              </span>
            )}
          </div>

          {meta > 0 ? (
            <div className="dash-progress">
              <div
                className={`dash-progress-fill ${
                  acimaMeta ? "perigo" : progressoMeta > 80 ? "alerta" : ""
                }`}
                style={{ width: `${Math.min(progressoMeta, 100)}%` }}
              />
            </div>
          ) : (
            <button
              type="button"
              className="dash-link-btn"
              onClick={() => navigate("/configuracoes")}
            >
              Definir meta em Configurações →
            </button>
          )}
        </div>

        <div className="dash-panel dash-custo">
          <div className="dash-panel-head">
            <div>
              <h3>Custo estimado</h3>
              <p>Mês de {nomeMesAtual}</p>
            </div>
            <span className="dash-metric-ico verde-ico">
              <Icon name="info" size={18} />
            </span>
          </div>
          <div className="dash-custo-valor">
            {tarifa > 0 ? (
              <>
                R$ {custoMes.toFixed(2)}
                <span>a R$ {tarifa.toFixed(2)}/kWh</span>
              </>
            ) : (
              <button
                type="button"
                className="dash-link-btn"
                onClick={() => navigate("/configuracoes")}
              >
                Defina a tarifa em Configurações →
              </button>
            )}
          </div>
        </div>
      </section>

      {/* Graficos */}
      <div className="dash-graficos">
        <section className="dash-panel">
          <div className="dash-panel-head">
            <div>
              <h3>Consumo por mês</h3>
              <p>Evolução do consumo registrado ao longo do ano</p>
            </div>
            <span className="dash-tag">kWh / mês</span>
          </div>
          <div className="dash-chart">
            <Bar key={temaChart} data={dadosConsumo} options={opcoesConsumo} />
          </div>
        </section>

        <section className="dash-panel">
          <div className="dash-panel-head">
            <div>
              <h3>Consumo esperado por equipamento</h3>
              <p>Distribuição estimada entre os seus equipamentos</p>
            </div>
            <span className="dash-tag">kWh / dia</span>
          </div>
          {equipamentos.length === 0 ? (
            <p className="dash-aviso">Nenhum equipamento cadastrado.</p>
          ) : (
            <>
              <div className="dash-chart">
                <Doughnut
                  key={temaChart}
                  data={dadosEquipamentos}
                  options={opcoesEquipamentos}
                />
              </div>
              <LegendaEquipamentos
                labels={labelsEquip}
                valores={valoresEquip}
                cores={CORES}
              />
            </>
          )}
        </section>
      </div>
    </div>
  );
}
