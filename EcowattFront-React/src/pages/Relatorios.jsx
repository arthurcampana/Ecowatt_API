import { useEffect, useMemo, useRef, useState } from "react";
import { Bar, Doughnut, Line } from "react-chartjs-2";
import html2canvas from "html2canvas";
import { jsPDF } from "jspdf";
import "../charts/registerCharts.js";
import { useAuth } from "../context/AuthContext.jsx";
import { consumoService } from "../api/consumoService.js";
import { configuracaoService } from "../api/configuracaoService.js";
import { equipamentoUsuarioService } from "../api/equipamentoUsuarioService.js";
import { formatarData } from "../utils/formato.js";
import { CORES } from "../utils/chartPalette.js";
import LegendaEquipamentos from "../components/LegendaEquipamentos.jsx";

// Tela de relatorios (rota /relatorios): filtra os consumos por intervalo de
// datas e mostra cards, tabela, um grafico de barras/linha alternavel, os dois
// graficos de equipamentos (doughnut + custo estimado) e exportacao em PDF via
// html2canvas + jsPDF. Portado do legado relatorios.js, usando sempre a lista
// unica de consumos carregada (o legado tinha uma mistura consumos/todosConsumos).
export default function Relatorios() {
  const { usuario } = useAuth();

  const [consumos, setConsumos] = useState([]);
  const [config, setConfig] = useState({ meta: 0, valorTarifa: 0 });
  const [equipamentos, setEquipamentos] = useState([]);

  const [dataInicial, setDataInicial] = useState("");
  const [dataFinal, setDataFinal] = useState("");
  const [tipoGrafico, setTipoGrafico] = useState("bar"); // 'bar' | 'line'
  const [painelVisivel, setPainelVisivel] = useState(false);
  const [consumosFiltrados, setConsumosFiltrados] = useState([]);

  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  const painelRef = useRef(null);

  useEffect(() => {
    async function carregar() {
      try {
        const cfg = await configuracaoService.buscarPorUsuario(usuario.id);
        setConfig(cfg);
      } catch (error) {
        if (error?.status === 404) {
          setConfig({ meta: 0, valorTarifa: 0 });
        } else {
          setMensagem({
            texto: error?.message || "Erro ao carregar configuração",
            tipo: "erro",
          });
        }
      }

      try {
        const dados = await consumoService.listarPorUsuario(usuario.id);
        setConsumos(dados || []);
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

  function gerarRelatorio() {
    const filtrados = consumos.filter((item) => {
      const data = new Date(item.dataRegistro);

      if (dataInicial) {
        if (data < new Date(dataInicial)) {
          return false;
        }
      }

      if (dataFinal) {
        const fim = new Date(dataFinal);
        fim.setHours(23, 59, 59, 999);
        if (data > fim) {
          return false;
        }
      }

      return true;
    });

    setConsumosFiltrados(filtrados);
    setPainelVisivel(true);
  }

  function trocarGrafico() {
    setTipoGrafico((prev) => (prev === "bar" ? "line" : "bar"));
  }

  // Cards do painel.
  const cards = useMemo(() => {
    const total = consumosFiltrados.reduce(
      (soma, item) => soma + Number(item.consumoKwh),
      0
    );
    const len = consumosFiltrados.length;
    const media = len ? total / len : 0;
    const maior = len
      ? Math.max(...consumosFiltrados.map((c) => Number(c.consumoKwh)))
      : 0;
    const menor = len
      ? Math.min(...consumosFiltrados.map((c) => Number(c.consumoKwh)))
      : 0;
    const custo = total * Number(config.valorTarifa);
    const statusMeta =
      media <= config.meta
        ? { texto: "Dentro da Meta", cls: "status-ok" }
        : { texto: "Meta Ultrapassada", cls: "status-alerta" };

    return {
      consumoTotal: total.toFixed(2) + " kWh",
      consumoMedio: media.toFixed(2) + " kWh",
      maiorConsumo: maior.toFixed(2) + " kWh",
      menorConsumo: menor.toFixed(2) + " kWh",
      custoTotal: "R$ " + custo.toFixed(2),
      metaAtual: config.meta + " kWh",
      statusMeta,
    };
  }, [consumosFiltrados, config]);

  // Tabela: ordenada de forma crescente por data.
  const tabela = useMemo(
    () =>
      [...consumosFiltrados].sort(
        (a, b) => new Date(a.dataRegistro) - new Date(b.dataRegistro)
      ),
    [consumosFiltrados]
  );

  // Grafico principal: agrupa por dia ou por mes quando o intervalo passa de
  // 31 dias.
  const graficoPrincipal = useMemo(() => {
    const agruparPorMes =
      dataInicial &&
      dataFinal &&
      (new Date(dataFinal) - new Date(dataInicial)) / 86400000 > 31;

    const mapa = {};
    consumosFiltrados.forEach((c) => {
      const chave = agruparPorMes
        ? new Date(c.dataRegistro).toLocaleDateString("pt-BR", {
            month: "short",
            year: "numeric",
          })
        : formatarData(c.dataRegistro);
      mapa[chave] = (mapa[chave] || 0) + Number(c.consumoKwh);
    });

    return {
      labels: Object.keys(mapa),
      datasets: [
        {
          label: "Consumo (kWh)",
          data: Object.values(mapa),
          backgroundColor: "#198754",
          borderColor: "#198754",
          borderWidth: 2,
          borderRadius: 8,
          fill: false,
        },
      ],
    };
  }, [consumosFiltrados, dataInicial, dataFinal]);

  const opcoesGraficoPrincipal = {
    responsive: true,
    maintainAspectRatio: false,
  };

  // Equipamentos: doughnut do consumo esperado.
  const labelsEquip = equipamentos.map((e) => e.nomeIdentificacao);
  const valoresEquip = equipamentos.map((e) => Number(e.consumoEsperado));

  const dadosDoughnut = {
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

  const opcoesDoughnut = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
  };

  // Equipamentos: custo estimado por equipamento.
  const graficoCusto = useMemo(() => {
    const somaEsperado = equipamentos.reduce(
      (soma, e) => soma + Number(e.consumoEsperado),
      0
    );
    const consumoTotalRef =
      consumosFiltrados.length > 0
        ? Number(consumosFiltrados[consumosFiltrados.length - 1].consumoKwh)
        : 0;
    const tarifa = Number(config.valorTarifa);

    const custos = equipamentos.map((e) => {
      const percentual = somaEsperado
        ? Number(e.consumoEsperado) / somaEsperado
        : 0;
      const consumoEstimado = consumoTotalRef * percentual;
      return consumoEstimado * tarifa;
    });

    return {
      labels: equipamentos.map((e) => e.nomeIdentificacao),
      datasets: [
        {
          label: "Custo estimado (R$)",
          data: custos,
          backgroundColor: "#0d6efd",
          borderWidth: 2,
          borderRadius: 8,
        },
      ],
    };
  }, [equipamentos, consumosFiltrados, config]);

  const opcoesGraficoCusto = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      tooltip: {
        callbacks: {
          label: (ctx) => "R$ " + ctx.raw.toFixed(2),
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (v) => "R$ " + v,
        },
      },
    },
  };

  async function exportarPDF() {
    if (consumosFiltrados.length === 0) {
      alert("Gere um relatório primeiro.");
      return;
    }

    const canvas = await html2canvas(painelRef.current, {
      scale: 2,
      useCORS: true,
    });
    const imgData = canvas.toDataURL("image/png");

    const pdf = new jsPDF("p", "mm", "a4");

    const pageWidth = 210;
    const pageHeight = 297;
    const margin = 10;
    const imgWidth = pageWidth - margin * 2;
    const imgHeight = (canvas.height * imgWidth) / canvas.width;

    let heightLeft = imgHeight;
    let position = margin;

    pdf.setFontSize(18);
    pdf.text("Relatório EcoWatt", 60, 15);

    pdf.setFontSize(11);
    pdf.text("Data: " + new Date().toLocaleDateString("pt-BR"), margin, 25);

    position = 35;

    pdf.addImage(imgData, "PNG", margin, position, imgWidth, imgHeight);
    heightLeft -= pageHeight - position;

    while (heightLeft > 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, "PNG", margin, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    pdf.save("Relatorio_EcoWatt.pdf");
  }

  return (
    <div>
      <div className="page-header">
        <h2>Relatórios</h2>
        <p>Gere relatórios de consumo por período e exporte em PDF.</p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <div className="config-card filtro-card">
        <h5>Relatório de Consumo</h5>

        <div className="filtro-grid">
          <div className="campo">
            <label className="form-label" htmlFor="dataInicial">
              Data Inicial
            </label>
            <input
              id="dataInicial"
              type="date"
              className="form-control"
              value={dataInicial}
              onChange={(e) => setDataInicial(e.target.value)}
            />
          </div>

          <div className="campo">
            <label className="form-label" htmlFor="dataFinal">
              Data Final
            </label>
            <input
              id="dataFinal"
              type="date"
              className="form-control"
              value={dataFinal}
              onChange={(e) => setDataFinal(e.target.value)}
            />
          </div>
        </div>

        <div className="relatorio-acoes">
          <button type="button" className="btn-add" onClick={gerarRelatorio}>
            Gerar Relatório
          </button>
          <button type="button" className="btn-save" onClick={exportarPDF}>
            Exportar PDF
          </button>
        </div>
      </div>

      {painelVisivel && (
        <div ref={painelRef} className="painel-relatorio">
          <div className="indicadores-grid">
            <div className="indicador">
              <h6>Consumo Total</h6>
              <h3>{cards.consumoTotal}</h3>
            </div>
            <div className="indicador">
              <h6>Consumo Médio</h6>
              <h3>{cards.consumoMedio}</h3>
            </div>
            <div className="indicador">
              <h6>Maior Consumo</h6>
              <h3>{cards.maiorConsumo}</h3>
            </div>
            <div className="indicador">
              <h6>Menor Consumo</h6>
              <h3>{cards.menorConsumo}</h3>
            </div>
          </div>

          <div className="indicadores-grid indicadores-grid-3">
            <div className="indicador">
              <h6>Custo Estimado</h6>
              <h3>{cards.custoTotal}</h3>
            </div>
            <div className="indicador">
              <h6>Meta</h6>
              <h3>{cards.metaAtual}</h3>
            </div>
            <div className="indicador">
              <h6>Status</h6>
              <h3 className={cards.statusMeta.cls}>{cards.statusMeta.texto}</h3>
            </div>
          </div>

          <div className="chart-card">
            <div className="chart-card-header">
              <h5>Evolução do Consumo</h5>
              <button
                type="button"
                className="btn-trocar-grafico"
                onClick={trocarGrafico}
              >
                Alterar gráfico
              </button>
            </div>
            <div className="chart-container">
              {tipoGrafico === "bar" ? (
                <Bar
                  data={graficoPrincipal}
                  options={opcoesGraficoPrincipal}
                />
              ) : (
                <Line
                  data={graficoPrincipal}
                  options={opcoesGraficoPrincipal}
                />
              )}
            </div>
          </div>

          <div className="chart-card">
            <h5>Histórico</h5>
            <div className="tabela-relatorio">
              <table className="table">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Consumo (kWh)</th>
                  </tr>
                </thead>
                <tbody>
                  {tabela.map((item) => (
                    <tr key={item.id}>
                      <td>{formatarData(item.dataRegistro)}</td>
                      <td>{Number(item.consumoKwh).toFixed(2)} kWh</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="chart-card">
            <h5>Consumo por Equipamentos</h5>
            {equipamentos.length === 0 ? (
              <p className="aviso">Nenhum equipamento cadastrado.</p>
            ) : (
              <>
                <div className="chart-container">
                  <Doughnut data={dadosDoughnut} options={opcoesDoughnut} />
                </div>
                <LegendaEquipamentos
                  labels={labelsEquip}
                  valores={valoresEquip}
                  cores={CORES}
                />
              </>
            )}
          </div>

          <div className="chart-card">
            <h5>Estimativa de custo por equipamento</h5>
            {equipamentos.length === 0 ? (
              <p className="aviso">Nenhum equipamento cadastrado.</p>
            ) : (
              <div className="chart-container">
                <Bar data={graficoCusto} options={opcoesGraficoCusto} />
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
