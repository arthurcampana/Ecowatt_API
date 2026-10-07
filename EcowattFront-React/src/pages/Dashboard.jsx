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
    <div>
      <div className="page-header">
        <h2>Dashboard</h2>
        <p>
          Bem-vindo, <strong>{usuario?.nome}</strong>. Visualize seu consumo
          energético e acompanhe sua evolução mensal.
        </p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <div className="dashboard-cards">
        <div className="dashboard-card">
          <h6>Consumo Atual (Mês)</h6>
          <h2>{totalMesAtual.toFixed(2)} kWh</h2>
          <small>Total acumulado no mês atual</small>
        </div>

        <div className="dashboard-card">
          <h6>Média Mensal</h6>
          <h2>{mediaMensal.toFixed(2)} kWh</h2>
          <small>Média de consumo no ano</small>
        </div>

        <div className="dashboard-card">
          <h6>Total no Ano</h6>
          <h2>{totalAno.toFixed(2)} kWh</h2>
          <small>Soma total registrada</small>
        </div>
      </div>

      <div className="chart-card">
        <h5>Consumo por mês (kWh)</h5>
        <div className="chart-container">
          <Bar data={dadosConsumo} options={opcoesConsumo} />
        </div>
      </div>

      <div className="chart-card">
        <h5>Consumo esperado por equipamento</h5>
        {equipamentos.length === 0 ? (
          <p className="aviso">Nenhum equipamento cadastrado.</p>
        ) : (
          <>
            <div className="chart-container">
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
      </div>
    </div>
  );
}
