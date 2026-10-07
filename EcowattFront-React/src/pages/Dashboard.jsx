import { useEffect, useMemo, useState } from "react";
import { Bar, Doughnut } from "react-chartjs-2";
import "../charts/registerCharts.js";
import { useAuth } from "../context/AuthContext.jsx";
import { consumoService } from "../api/consumoService.js";
import { equipamentoUsuarioService } from "../api/equipamentoUsuarioService.js";
import { CORES } from "../utils/chartPalette.js";
import LegendaEquipamentos from "../components/LegendaEquipamentos.jsx";

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

// Dashboard real (rota /): tres cards calculados a partir dos totais mensais do
// ano atual, um grafico de barras de consumo por mes e um doughnut do consumo
// esperado por equipamento com a legenda customizada. Portado do legado
// index.js (o atalho oculto Ctrl+Shift+D foi removido por decisao do projeto).
export default function Dashboard() {
  const { usuario } = useAuth();

  const [consumoMes, setConsumoMes] = useState(() => new Array(12).fill(0));
  const [equipamentos, setEquipamentos] = useState([]);
  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  useEffect(() => {
    async function carregar() {
      try {
        const consumos = await consumoService.listarPorUsuario(usuario.id);
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
      } catch (error) {
        setMensagem({
          texto: error?.message || "Erro ao carregar consumos",
          tipo: "erro",
        });
      }

      try {
        const equips = await equipamentoUsuarioService.listarPorUsuario(
          usuario.id
        );
        setEquipamentos(equips || []);
      } catch (error) {
        setMensagem({
          texto: error?.message || "Erro ao carregar equipamentos",
          tipo: "erro",
        });
      }
    }

    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  // Cards: consumo do mes atual, media mensal (dividida pelos meses com
  // consumo) e total do ano.
  const { totalMesAtual, mediaMensal, totalAno } = useMemo(() => {
    const mesAtual = new Date().getMonth();
    const total = consumoMes.reduce((acc, valor) => acc + valor, 0);
    const mesesComConsumo = consumoMes.filter((v) => v > 0).length || 1;

    return {
      totalMesAtual: consumoMes[mesAtual],
      mediaMensal: total / mesesComConsumo,
      totalAno: total,
    };
  }, [consumoMes]);

  const nomeMesAtual = MESES[new Date().getMonth()];
  const primeiroNome = (usuario?.nome || "").split(" ")[0];

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

  const opcoesConsumo = {
    responsive: true,
    maintainAspectRatio: false,
  };

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

  return (
    <div className="dash">
      <div className="dash-head">
        <h1>Olá{primeiroNome ? `, ${primeiroNome}` : ""} 👋</h1>
        <p>Resumo do seu consumo de energia — {nomeMesAtual}</p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      {/* Cards de metrica pastel */}
      <section className="dash-metrics">
        <article className="dash-metric verde">
          <div className="dash-metric-ico">📅</div>
          <div className="dash-metric-valor">
            {totalMesAtual.toFixed(2)} <em>kWh</em>
          </div>
          <div className="dash-metric-label">Consumo em {nomeMesAtual}</div>
        </article>

        <article className="dash-metric azul">
          <div className="dash-metric-ico">📊</div>
          <div className="dash-metric-valor">
            {mediaMensal.toFixed(2)} <em>kWh</em>
          </div>
          <div className="dash-metric-label">Média mensal no ano</div>
        </article>

        <article className="dash-metric ambar">
          <div className="dash-metric-ico">🔋</div>
          <div className="dash-metric-valor">
            {totalAno.toFixed(2)} <em>kWh</em>
          </div>
          <div className="dash-metric-label">Total no ano</div>
        </article>
      </section>

      {/* Grid de graficos: lado a lado quando a sidebar esta retraida
          (mais espaco), empilhados quando expandida. Ver dashboard.css. */}
      <div className="dash-graficos">
        {/* Grafico de consumo por mes */}
        <section className="dash-panel">
          <div className="dash-panel-head">
            <div>
              <h3>Consumo por mês</h3>
              <p>Evolução do consumo registrado ao longo do ano</p>
            </div>
            <span className="dash-tag">kWh / mês</span>
          </div>
          <div className="dash-chart">
            <Bar data={dadosConsumo} options={opcoesConsumo} />
          </div>
        </section>

        {/* Grafico de equipamentos */}
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
